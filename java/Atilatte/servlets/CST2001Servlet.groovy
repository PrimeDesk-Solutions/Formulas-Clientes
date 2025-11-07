package servlets;

import br.com.multiorm.ColumnType;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.ExceptionUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.exceptions.FullStackException;
import br.com.multitec.utils.jackson.JSonMapperCreator;
import br.com.multitec.utils.mq.MQMsgSender;
import br.com.multitec.utils.xml.ElementXml;
import br.com.multitec.utils.xml.XMLConverter;
import org.springframework.http.ResponseEntity;
import sam.dto.spp.SPP1002CriarDto;
import sam.dto.srf.FormulaSRFCalculoDocumentoDto;
import sam.model.entities.ab.Abd01;
import sam.model.entities.ab.Abd03;
import sam.model.entities.ab.Abf40;
import sam.model.entities.ab.Abm01;
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa0103;
import sam.model.entities.ea.Eaa0113;
import sam.model.entities.ea.Eaa01131;
import sam.server.samdev.formula.ExecutarFormulaService;
import sam.server.samdev.relatorio.ServletBase;
import sam.server.spp.service.SPP1002Service;
import sam.server.srf.service.SRFService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CST2001Servlet extends ServletBase {
	private static String BOM_CODIGO_FIXO_PARA_PRODUCAO = "21";
	private static String MSG_DOC_INTEGRADO = "Documento integrado com sucesso.";

	@Override
	public ResponseEntity<Object> executar() throws Exception {
		try {
			session.beginTransaction();

			String json = httpServletRequest.getHeader("cst2001Dto");
			if(json == null) throw new ValidacaoException("Cst2001Dto não informado");

			CST2001DTO dto = JSonMapperCreator.create().read(json, CST2001DTO.class);
			List<TableMap> cst101s;

			if(dto.getCst10id() != null) {
				cst101s = localizarLancamentosParaIntegrar(dto);
			}else {
				cst101s = localizarLancamentosParaIntegrarPorIds(dto);
			}

			if(cst101s.size() == 0){
				throw new ValidacaoException("Nenhuma CFe localizado para integração");
			}

			MQMsgSender sender = instanciarService(MQMsgSender.class);

			Map<Long, IntegracaoResult> mensagensDeIntegracao = new HashMap<>();
			String ossGeradas = "";
			try {
				sender.send("Gerando a ordem de produção");
				try {
					ossGeradas = executarOrdensDeProducao(dto, cst101s);
				} catch (ValidacaoException e) {
					throw new ValidacaoException("Erro ao gerar ordens de produção:\n" + e.getMessage());
				}

				Abd01 abd01 = session.get(Abd01.class, dto.getAbd01id());
				if(abd01.getAbd01ceFinanc() == null)throw new ValidacaoException("O PCD informado não possui PCD Financeiro");
				Abd03 abd03 = session.get(Abd03.class, abd01.getAbd01ceFinanc().getAbd03id());

				try {
					for (TableMap cst101 : cst101s) {
						sender.send("Integrando cupom número: " + cst101.getInteger("cst101numero"));
						integrarCupom(cst101, abd01, abd03, dto, mensagensDeIntegracao);
					}
				} catch (ValidacaoException e) {
					throw new ValidacaoException("Erro ao integrar cupom:\n" + e.getMessage());
				}
			} catch (Exception e) {
				rollback();
				session.beginTransaction();
				persistirStatusProcessamento(dto, mensagensDeIntegracao, e, null);
				session.commit();
				throw e;
			}
			persistirStatusProcessamento(dto, mensagensDeIntegracao, null, ossGeradas);

			return ResponseEntity.ok("");
		}catch (ValidacaoException e) {
			throw e;
		} catch (Exception e) {
			throw new FullStackException("Erro ao executar integração das CFe´s", e);
		}
	}

	private String executarOrdensDeProducao(CST2001DTO dto, List<TableMap> cst101s) {
		//Gerando produção
		Map<Long, BigDecimal> mapAbp20idAndQtdToProducao = new HashMap<Long, BigDecimal>();
		for(TableMap cst101 : cst101s) {
			String xmlCFe = cst101.getString("cst101json");
			ElementXml xml = XMLConverter.string2Element(xmlCFe);
			List<ElementXml> itens = xml.findChildNodes("infCFe.det");

			for(ElementXml item : itens) {
				String codProd = item.findChildValue("prod.cProd");
				Long abm01id = findAbm01idByCodigo(codProd.trim());


				String qtdXml = item.findChildValue("prod.qCom");
				BigDecimal qtd = new BigDecimal(qtdXml);
				if(qtd == null || qtd.compareTo(new BigDecimal(0)) == 0)continue;

				Long abp20id = findAbp20idByAbm01id(abm01id);
				if(abp20id != null) {
					mapAbp20idAndQtdToProducao.compute(abp20id, (id, sum) -> sum == null ? qtd : qtd.add(sum));
				}
			}
		}

		//Executando produção
		return gerarOrdensDeProducao(mapAbp20idAndQtdToProducao, dto.getAbe01id());
	}

	private String gerarOrdensDeProducao(Map<Long, BigDecimal> mapAbp20idAndQtdToProducao, Long abe01id) {
		if(mapAbp20idAndQtdToProducao.isEmpty())return "";
		SPP1002Service service = instanciarService(SPP1002Service.class);

		List<Long> idsOsGerados = new ArrayList<>();
		for(Long abp20id : mapAbp20idAndQtdToProducao.keySet()) {
			BigDecimal qtd = mapAbp20idAndQtdToProducao.get(abp20id);
			if(qtd == null || qtd.compareTo(new BigDecimal(0)) == 0) continue;

			SPP1002CriarDto dto = new SPP1002CriarDto(
				abp20id,
				qtd,
				LocalDate.now(),
				null,
				null,
				abe01id,
				null,
				null,
				null,
				2
			);

			idsOsGerados.addAll(service.criarOPsAvulsas(dto));
		}

		String oss = findOsNumeros(idsOsGerados).toString();

		return "O.S. criadas " + oss;
	}


	private Long integrarCupom(TableMap cst101, Abd01 abd01, Abd03 abd03, CST2001DTO dto, Map<Long, IntegracaoResult> mensagensDeIntegracao) throws Exception{
		try {
			SRFService service = instanciarService(SRFService.class);
			Eaa01 eaa01 = service.comporDocumentoPadrao(dto.getAbe01id(), dto.getAbd01id(), null);
			eaa01.getEaa01central().setAbb01num(cst101.getInteger("cst101numero"));

			String cfe = cst101.getString("cst101json");
			ElementXml xml = XMLConverter.string2Element(cfe);

			TableMap eaa01json = getOrCreateEaa01Json(eaa01);
			if(xml.findChildValue("ide.nserieSAT") != null){
				eaa01json.put("nseriesat", xml.findChildValue("ide.nserieSAT"));
			}

			String dEmiStr = xml.findChildValue("ide.dEmi");
			eaa01.getEaa01central().setAbb01data(DateUtils.parseDate(dEmiStr, "yyyyMMdd"));
			eaa01.setEaa01esData(LocalDate.now());

			List<ElementXml> itens = xml.findChildNodes("infCFe.det");

			Integer sequecia = 1;
			BigDecimal valorTotal = new BigDecimal(0);
			boolean hasItem = false;

			for(ElementXml item : itens) {
				String codProd = item.findChildValue("prod.cProd");
				Long abm01id = findAbm01idByCodigo(codProd.trim());

				String qtdXml = item.findChildValue("prod.qCom");
				BigDecimal qtd = new BigDecimal(qtdXml);
				if(qtd == null || qtd.compareTo(new BigDecimal(0)) == 0)continue;

				String unitXml = item.findChildValue("prod.vUnCom");
				BigDecimal unit = new BigDecimal(unitXml);

				hasItem = true;

				Eaa0103 eaa0103 = service.comporItemDoDocumentoPadrao(eaa01, abm01id);
				eaa0103.setEaa0103seq(sequecia);
				eaa0103.setEaa0103qtComl(qtd);
				eaa0103.setEaa0103unit(unit);
				if(item.findChildValue("prod.vDesc") != null){
					getOrCreateEaa0103Json(eaa0103).put("desconto",  new BigDecimal(item.findChildValue("prod.vDesc")));
				}
				BigDecimal troco = getTroco(xml);
				if(troco.compareTo(BigDecimal.ZERO) > 0){
					getOrCreateEaa0103Json(eaa0103).put("troco",  troco);
				}
				eaa01.addToEaa0103s(eaa0103);

				valorTotal = valorTotal.add(qtd.multiply(unit).subtract(troco).setScale(2, RoundingMode.HALF_EVEN));
				sequecia++;
			}
			if(!hasItem)return null;


			List<ElementXml> pagtos = xml.findChildNodes("infCFe.pgto");
			Map<String, BigDecimal> mapAbf40CodigoAndValor = new HashMap<>();
			BigDecimal valorTotalPgto = new BigDecimal(0);
			for(ElementXml pagto : pagtos) {
				List<ElementXml> mps = pagto.findChildNodes("MP");
				for(ElementXml mp : mps) {
					String vParcela = mp.findChildValue("vMP");
					String codMP = mp.findChildValue("cMP");
					BigDecimal valor = new BigDecimal(vParcela);
					// Abate o valor do troco e mantém o valor liquido da parcela
					if(codMP == "01"){
						for(ElementXml pgto : pagtos ){
							String txtTroco = pgto.findChildValue("vTroco");
							BigDecimal troco = new BigDecimal(txtTroco);
							valor -= troco
						}
					}
					
					valorTotalPgto = valorTotalPgto.add(valor);
					String abf40Codigo = getAbf40Codigo(mp);
					mapAbf40CodigoAndValor.merge(abf40Codigo, valor, BigDecimal::add);
				}
			}

			Eaa0113 eaa0113 = new Eaa0113();
			eaa01.addToEaa0113s(eaa0113);
			eaa0113.setEaa0113clasParc(Eaa0113.CLASPARC_PARCELA_DO_DOCUMENTO);
			eaa0113.setEaa0113dtVctoN(LocalDate.now());
			eaa0113.setEaa0113valor(valorTotalPgto);
			eaa0113.setEaa0113tipo(abd03.getAbd03tipo());
			eaa0113.setEaa0113docFin(Eaa0113.DOCFIN_CRIAR_A_QUITAR);

			eaa01.addToEaa0113s(eaa0113);

			for(String abf40Codigo : mapAbf40CodigoAndValor.keySet()) {
				definirParcelamento(xml, eaa01, dto, abd03, mapAbf40CodigoAndValor.get(abf40Codigo), abf40Codigo, eaa0113);
			}


			//Gravando documento Eaa01
			var formulaDto = new FormulaSRFCalculoDocumentoDto(abd01.getAbd01frmItem(), abd01.getAbd01frmDoc(), eaa01, null, "CST2001", true);
			service.executarFormulaSRFCalculoDocumento(formulaDto);
			eaa01.getEaa01central().setAbb01valor(eaa01.getEaa01totDoc());

			if(dto.getFormula() != null){
				var formulaService = instanciarService(ExecutarFormulaService.class);
				formulaService.executar(dto.getFormula(), Utils.map("eaa01", eaa01, "cfe", cfe));
			}

			System.out.println("JSON Eaa01:\n" + JSonMapperCreator.create().writeAsString(eaa01));
			Long eaa01id = service.gravarDocumento(eaa01);

			mensagensDeIntegracao.put(cst101.getLong("cst101id"), new IntegracaoResult(MSG_DOC_INTEGRADO + " Doc Número: " + eaa01.getEaa01central().getAbb01num(), eaa01id));
			return eaa01id;
		} catch (Exception e) {
			mensagensDeIntegracao.clear();
			mensagensDeIntegracao.put(cst101.getLong("cst101id"), montarMsgDeErro(e));
			throw e;
		}
	}

	private void definirParcelamento(ElementXml xml, Eaa01 eaa01, CST2001DTO dto, Abd03 abd03, BigDecimal valorPagto, String abf40codigo, Eaa0113 eaa0113) {
		Abf40 abf40 = findAbf40ByCustom(abf40codigo);
		BigDecimal troco = getTroco(xml);
		if(abf40codigo.equals("01999") && troco.compareTo(BigDecimal.ZERO) > 0){
			valorPagto = valorPagto.subtract(troco);
		}

		Eaa01131 eaa01131 = new Eaa01131();
		eaa01131.setEaa01131fp(abf40);
		eaa01131.setEaa01131valor(valorPagto);
		eaa01131.setEaa01131carPce(1);

		eaa0113.addToEaa01131s(eaa01131);
	}

	private String getAbf40Codigo(ElementXml mp) {
		String cMP = mp.findChildValue("cMP");
		String cAdmC = mp.findChildValue("cAdmC");

		if(cAdmC != null) {
			return cMP+"-"+cAdmC;
		}else {
			return cMP;
		}
	}

	private BigDecimal getTroco(ElementXml xml){
		BigDecimal troco = new BigDecimal(0);
		if(xml.findChildValue("pgto.vTroco") != null){
			troco = new BigDecimal(xml.findChildValue("pgto.vTroco"));
		}
		return troco;
	}

	private TableMap getOrCreateEaa0103Json(Eaa0103 eaa0103){
		if(eaa0103.getEaa0103json() == null){
			eaa0103.setEaa0103json(new TableMap());
		}

		return eaa0103.getEaa0103json();
	}

	private TableMap getOrCreateEaa01Json(Eaa01 eaa01){
		if(eaa01.getEaa01json() == null){
			eaa01.setEaa01json(new TableMap());
		}

		return eaa01.getEaa01json();
	}

	private void persistirStatusProcessamento(CST2001DTO dto, Map<Long, IntegracaoResult> mensagensDeIntegracao, Exception exception, String mensagem)throws Exception {
		List<String> updates = new ArrayList<>();

		String msg = exception == null ? "Integração efetuada com sucesso! " + mensagem : montarMsgDeErro(exception).msg;
		if(dto.getCst10id() != null) {
			updates.add("UPDATE Cst10 SET cst10msg = '" + msg.replace("'", "") + "', cst10status = " + (exception == null ? 1 : 9) + " WHERE cst10id = " + dto.getCst10id() + ";");
		}

		if(exception != null && dto.getCst10id() != null) {
			mensagensDeIntegracao = mensagensDeIntegracao.entrySet()
				.stream()
				.filter(entry -> entry.getValue().msg.startsWith("Erro:"))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

			updates.add("UPDATE Cst101 SET cst101msg = null WHERE cst101lote = " + dto.getCst10id() + ";");
		}

		for(Long cst101id : mensagensDeIntegracao.keySet()) {
			updates.add("UPDATE Cst101 SET cst101msg = '" + mensagensDeIntegracao.get(cst101id).msg.replace("'", "") + "', cst101doc = " + mensagensDeIntegracao.get(cst101id).eaa01id + " WHERE cst101id = " + cst101id + ";");
		}

		Statement statement = session.getConnection().createStatement();
		for(String sql : updates) {
			statement.addBatch(sql);
		}
		statement.executeBatch();
	}

	private Long findAbp20idByAbm01id(Long abm01id) {
		return session.createQuery(
				" SELECT abp20id " +
					" FROM Abp20 " +
					" WHERE abp20item = :abm01id AND abp20di IS NULL " +
					" AND abp20bomCodigo = :abp20bomCodigo"
			).setParametersMap(Utils.map("abm01id", abm01id))
			.setParametersMap(Utils.map("abp20bomCodigo", BOM_CODIGO_FIXO_PARA_PRODUCAO))
			.getUniqueResult(ColumnType.LONG);
	}

	private List<Integer> findOsNumeros(List<Long> bab01ids) {
		return session.createQuery(
				" SELECT abb01num " +
					" FROM Bab01 " +
					" INNER JOIN Abb01 ON abb01id = bab01central " +
					" WHERE bab01id IN (:bab01ids)" +
					" GROUP BY abb01num"
			).setParametersMap(Utils.map("bab01ids", bab01ids))
			.getList(ColumnType.INTEGER);
	}

	private Long findAbm01idByCodigo(String codigo) {
		Long result = session.createQuery(
				" SELECT abm01id FROM Abm0101 "+
					" INNER JOIN Abm01 ON abm01id = abm0101item " +
					" WHERE UPPER(jget(abm0101json.linx)) = :codigo " +
					getSamWhere().getWherePadrao("AND", Abm01.class)

			).setMaxResult(1)
			.setParametersMap(Utils.map("codigo", codigo.toUpperCase()))
			.getUniqueResult(ColumnType.LONG);

		if(result == null)throw new ValidacaoException("Não foi possível localizar no SAM, a partir do campo livre linx, o item com o código '" + codigo.trim() + "' vindo do cupom da CFe");
		return result;
	}

	private Abf40 findAbf40ByCustom(String codigo) {
		Abf40 result = session.createQuery(
				" SELECT * FROM Abf40 "+
					" WHERE UPPER(jget(abf40camposcustom.codcfe)) = :codcfe " +
					getSamWhere().getWherePadrao("AND", Abf40.class)

			).setMaxResult(1)
			.setParametersMap(Utils.map("codcfe", codigo.toUpperCase()))
			.getUniqueResult(ColumnType.ENTITY);

		if(result == null)throw new ValidacaoException("Não foi possível localizar no SAM, a partir do campo customizado 'codcfe', a Formas de Pagamento(Abf40) '" + codigo.trim() + "' vindo do cupom da CFe");
		return result;
	}

	private IntegracaoResult montarMsgDeErro(Exception e) {
		return new IntegracaoResult("Erro: " + e.getMessage() + "\n\n" +
			"Detalhe: " + getStackTrace(e) + "\n\n" +
			"Erro: " + e.getMessage(), null);
	}

	private String getStackTrace(Exception e) {
		try {
			try(StringWriter sw = new StringWriter()){
				try(PrintWriter pw = new PrintWriter(sw)){
					e.printStackTrace(pw);
					return sw.toString();
				}
			}
		} catch (Exception e2) {
			return ExceptionUtils.headerStackTrace(e);
		}
	}

	private List<TableMap> localizarLancamentosParaIntegrar(CST2001DTO dto){
		return session.createQuery(
				"""
          SELECT Cst101.* 
          FROM Cst10
          INNER JOIN Cst101 ON cst101lote = cst10id
          WHERE cst10status IN ('0', '9')
          AND cst10id = :cst10id
          AND cst101doc IS NULL
          ORDER BY cst101id
        """
			).setParametersMap(Utils.map("cst10id", dto.getCst10id(), "msgOk", MSG_DOC_INTEGRADO + "%"))
			.getListTableMap();
	}

	private List<TableMap> localizarLancamentosParaIntegrarPorIds(CST2001DTO dto){
		return session.createQuery(
			"""
				SELECT * 
				FROM Cst101
				WHERE cst101id IN (:cst101ids)
				AND cst101doc IS NULL
				ORDER BY cst101id
			"""
		).setParametersMap(Utils.map(
			"cst101ids", dto.getCst101ids(),
			"msgOk", MSG_DOC_INTEGRADO + "%"
		)).getListTableMap();
	}

	@Override
	public String getNome() throws Exception {
		return null;
	}

	private void rollback() throws SQLException {
		session.rollback();
		session.getConnection().setAutoCommit(true);
	}
}

class CST2001DTO {
	private Long cst10id;
	private Long abe01id;
	private Long abd01id;
	private List<Long> cst101ids;
	private String formula;

	public Long getCst10id() {
		return cst10id;
	}
	public void setCst10id(Long cst10id) {
		this.cst10id = cst10id;
	}
	public Long getAbe01id() {
		return abe01id;
	}
	public void setAbe01id(Long abe01id) {
		this.abe01id = abe01id;
	}
	public Long getAbd01id() {
		return abd01id;
	}
	public void setAbd01id(Long abd01id) {
		this.abd01id = abd01id;
	}
	public List<Long> getCst101ids() {
		return cst101ids;
	}
	public void setCst101ids(List<Long> cst101ids) {
		this.cst101ids = cst101ids;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}

}

class IntegracaoResult {
	public String msg;
	public Long eaa01id;
	public IntegracaoResult(String msg, Long eaa01id) {
		super();
		this.msg = msg;
		this.eaa01id = eaa01id;
	}
}
//meta-sis-eyJkZXNjciI6IkludGVncmHDp8OjbyBMaW54IiwidGlwbyI6InNlcnZsZXQifQ==