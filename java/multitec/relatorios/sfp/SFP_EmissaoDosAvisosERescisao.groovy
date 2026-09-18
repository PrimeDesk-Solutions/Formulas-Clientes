package multitec.relatorios.sfp;

import java.math.RoundingMode;
import java.text.NumberFormat
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;

import org.springframework.http.MediaType;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.aa.Aap18
import sam.model.entities.ab.Abh20;
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Emissão dos Avisos e Rescisão
 * @author Lucas Eliel
 * @since 15/03/2019
 * @version 1.0
 */

public class SFP_EmissaoDosAvisosERescisao extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Emissão dos Avisos e Rescisão";
	}

	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		filtrosDefault.put("isRescisao", false);
		filtrosDefault.put("isTermoQuit", false);
		filtrosDefault.put("isTermoHomol", false);
		filtrosDefault.put("isMaiorRem", false);
		filtrosDefault.put("isDetalheMR", false);
		filtrosDefault.put("isPedidoDem", false);
		filtrosDefault.put("isNotDispensa", false);
		filtrosDefault.put("isNotDispensaJC", false);
		filtrosDefault.put("isAvPrevEmpresa", false);
		filtrosDefault.put("isAvPrevEmpregado", false);
		filtrosDefault.put("isImpVerbasZeradas", false);
		LocalDate data = MDate.date();
		filtrosDefault.put("dtDemissao", data);
		filtrosDefault.put("dtDispensa", data);
		filtrosDefault.put("dtDispensaJC", data);
		filtrosDefault.put("dtAvPrevEmpresa", data);
		filtrosDefault.put("dtAvPrevEmpregado", data);
		filtrosDefault.put("complemento", 0);
		filtrosDefault.put("diasFaltas", 0);
		filtrosDefault.put("avPrevio", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
		
	/**Método Principal
	 * @return String, byte[] (Dados para Download)
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhador = getListLong("trabalhadores");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		List<Long> idsDepartamento = getListLong("departamentos");
		Integer complemento = getInteger("complemento");
		Long caeRend = getLong("caeRend");
		Long caeDesc = getLong("caeDesc");
		Long caeRemMesAnt = getLong("caeRemMesAnt");
		Boolean isRescisao = get("isRescisao");
		Boolean isTermoQuit = get("isTermoQuit");
		Boolean isTermoHomol = get("isTermoHomol");
		Boolean isMaiorRem = get("isMaiorRem");
		Boolean isDetalheMR = get("isDetalheMR");
		Boolean isPedidoDem = get("isPedidoDem");
		Boolean isNotDispensa = get("isNotDispensa");
		Boolean isNotDispensaJC = get("isNotDispensaJC");
		Boolean isAvPrevEmpresa = get("isAvPrevEmpresa");
		Boolean isAvPrevEmpregado = get("isAvPrevEmpregado");
		Boolean isImpVerbasZeradas = get("isImpVerbasZeradas");
		String nomeEmpregador = getString("nomeEmpregador");
		String rgEmpregador = getString("rgEmpregador");
		String cpfEmpregador = getString("cpfEmpregador");
		LocalDate dtDemissao = getLocalDate("dtDemissao");
		LocalDate dtDispensa = getLocalDate("dtDispensa");
		LocalDate dtDispensaJC = getLocalDate("dtDispensaJC");
		LocalDate dtAvPrevEmpresa = getLocalDate("dtAvPrevEmpresa");
		LocalDate dtAvPrevEmpregado = getLocalDate("dtAvPrevEmpregado");
		Boolean rdoAvPrevReducao2Hor = getInteger("avPrevio") == 0 ? true : false;
		Integer diasFaltas = getInteger("diasFaltas");
		String eventosNaoImprimir = getString("eventos") == null ? "": getString("eventos");
		
		String[] listaEventos = null;
		if (!eventosNaoImprimir.isEmpty()) {
			listaEventos = eventosNaoImprimir.split(",");
		}
		
		if (listaEventos != null) {
			for(int i = 0; i < listaEventos.length; i++) {
				String evento = listaEventos[i];
				evento = evento.trim();
				listaEventos[i] = evento;
			}
		}
		
		Aac10 aac10 = getVariaveis().getAac10();

		String endereco = null;
		if(aac10.getAac10endereco() != null) {
			if(aac10.getAac10numero() != null) {
				endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
			}else {
				endereco = aac10.getAac10endereco();
			}
		}
		
		String representante = "";
		if(nomeEmpregador != null) {
			representante = nomeEmpregador;
			if(rgEmpregador != null) representante += "   RG: " + rgEmpregador;
			if(cpfEmpregador != null) representante += "   CPF: " + cpfEmpregador;
		}
		
		params.put("TITULO_RELATORIO", "Emissão dos Avisos e Rescisão");
		params.put("EMP_RS", aac10.getAac10rs());
		params.put("EMP_SFPTI", aac10.getAac10ti());
		params.put("EMP_NI", aac10.getAac10ni());
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_COMPLEM", aac10.getAac10complem());
		params.put("EMP_BAIRRO", aac10.getAac10bairro());
		params.put("EMP_CEP", aac10.getAac10cep() != null ? aac10.getAac10cep() : null);
		params.put("EMP_CIDADE", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
		params.put("EMP_UF", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
		params.put("EMP_CNAE", aac10.getAac10cnae());
		params.put("EMP_REPRESENTANTE", representante);
		params.put("DATA_PEDIDO_DISPENSA", dtDemissao.getDayOfMonth() +" de "+ dtDemissao.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "br")) +" de "+ dtDemissao.getYear());
		params.put("DATA_NOT_DISPENSA", dtDispensa.getDayOfMonth() +" de "+ dtDispensa.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "br")) +" de "+ dtDispensa.getYear());
		params.put("DATA_NOT_DISPENSAJC",  dtDispensaJC.getDayOfMonth() +" de "+ dtDispensaJC.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "br")) +" de "+ dtDispensaJC.getYear());
		params.put("DATA_AVPREV_EMPRESA", dtAvPrevEmpresa.getDayOfMonth() +" de "+ dtAvPrevEmpresa.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "br")) +" de "+ dtAvPrevEmpresa.getYear());
		params.put("DATA_AVPREV_EMPREGADO", dtAvPrevEmpregado.getDayOfMonth() +" de "+ dtAvPrevEmpregado.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "br")) +" de "+ dtAvPrevEmpregado.getYear());
		params.put("REDUZHORAS", rdoAvPrevReducao2Hor);
		params.put("DIASFALTA", diasFaltas);
		params.put("StreamSub1R1", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R1_S1"));
		params.put("StreamSub2R1", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R1_S2"));
		params.put("StreamSub3R1", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R1_S3"));
		params.put("StreamSub4R1", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R1_S4"));
		params.put("StreamSub1R8", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R8_S1"));
		params.put("StreamSub1R11", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R11_S1"));
		params.put("StreamSub2R11", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R11_S2"));
		params.put("StreamSub3R11", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R11_S3"));
		params.put("StreamSub4R11", carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R11_S4"));

		JasperPrint  emissaoDosAvisosERescisao = getDadosRescisao(idsTrabalhador, tiposTrab, idsDepartamento, complemento, caeRend, caeDesc, caeRemMesAnt, isRescisao, isTermoQuit, isTermoHomol, isMaiorRem, isDetalheMR, isPedidoDem, dtDemissao, isNotDispensa, dtDispensa, isNotDispensaJC, dtDispensaJC, isAvPrevEmpresa, dtAvPrevEmpresa, isAvPrevEmpregado, dtAvPrevEmpregado, isImpVerbasZeradas, listaEventos);

		byte[] bytes;
		try {
			bytes = JasperExportManager.exportReportToPdf(emissaoDosAvisosERescisao);
		} catch (JRException e) {
			throw new RuntimeException("Erro ao gerar o relatório da classe "+ this.getClass().getName(), e);
		}

		return new DadosParaDownload(bytes, this.getClass().getSimpleName() + ".pdf", MediaType.APPLICATION_PDF);
	}

	/**Método Diverso
	 * @return Set Integer (Tipo de Trabalhador)
	 */
	private Set<Integer> obterTipoTrabalhador(){
		Set<Integer> tiposTrab = new HashSet<>();
		
		if((boolean) get("isTrabalhador")) tiposTrab.add(0);
		if((boolean) get("isAutonomo")) tiposTrab.add(1);
		if((boolean) get("isProlabore")) tiposTrab.add(2);
		if((boolean) get("isTerceiros")) tiposTrab.add(3);
		
		if(tiposTrab.size() == 0) {
			tiposTrab.add(0);
			tiposTrab.add(1);
			tiposTrab.add(2);
			tiposTrab.add(3);
		}
		return tiposTrab;
	}
	
	/**Método Diverso
	 * @return 	Aag0201 (uf, municipio)
	 */
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addJoin(Joins.fetch("aag0201uf"))
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de Trabalhadores e Departamento)
	 */
	public List<TableMap> getFbd10sByTrabalhadorAndDepto(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Set<Integer> tiposTrab, int complemento) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamentos != null && !idsDepartamentos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
	
		String sql = "SELECT * FROM Fbd10 " +
					 "INNER JOIN Abh80 ON abh80id = fbd10trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "WHERE fbd10nc = :complemento AND abh80tipo IN (:tiposTrab) " +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbd10.class) +
					 " ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDepartamentos != null && !idsDepartamentos.isEmpty()) query.setParameter("idsDepartamentos", idsDepartamentos);
		query.setParameter("tiposTrab", tiposTrab);
		query.setParameter("complemento", complemento);
		
		return query.getListTableMap();
	}

	/**Método Diverso
	 * @return 	List - TableMap (Query de Trabalhadores, Departamento, Tipo Trabalhadores e Tipo MR)
	 */
	public List<TableMap> findFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Set<Integer> tiposTrab, int tipoFbd1001, String[] eventosNaoImprimir) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamentos != null && !idsDepartamentos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";
	
		String sql = "SELECT * FROM Fbd1001 " +
					 "INNER JOIN Fbd10 ON fbd10id = fbd1001res " +
					 "INNER JOIN Abh80 ON abh80id = fbd10trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh21 ON abh21id = fbd1001eve " +
					 "WHERE abh80tipo IN (:tiposTrab) AND fbd1001tipo = :tipoFbd1001 AND fbd1001valor > 0 " +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbd10.class) + whereEventosNaoImprimir +
					 " ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDepartamentos != null && !idsDepartamentos.isEmpty()) query.setParameter("idsDepartamentos", idsDepartamentos);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		query.setParameter("tiposTrab", tiposTrab);
		query.setParameter("tipoFbd1001", tipoFbd1001);
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de dados Trabalhadores, Departamento, Tipo Trabalhadores e Tipo MR)
	 */
	public List<TableMap> findDadosFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Set<Integer> tiposTrab, int tipoFbd1001, String[] eventosNaoImprimir) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamentos != null && !idsDepartamentos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";
	
		String sql = "SELECT fbd1001res, fbd1001tipo, fbd1001descr, fbd1001refHoras, fbd1001valor, abh21ori.abh21id as abh21id, abh21eve.abh21codigo as abh21codigo, abh21eve.abh21nome as abh21nome, " +
					 "abh21ori.abh21unidPagto as abh21unidPagto, abb11codigo, abb11nome, fbd10fvPai, fbd10fvPaf, fbd10fpPai, fbd10fpPaf, fbd10ssPi, fbd10ssPf, fbd10dtPi, fbd10dtPf, fbd10ssRem, abh80id " +
					 "FROM Fbd1001 " +
					 "INNER JOIN Fbd10 ON fbd10id = fbd1001res " +
					 "INNER JOIN Abh80 ON abh80id = fbd10trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh21 abh21eve ON abh21eve.abh21id = fbd1001eve " +
					 "LEFT JOIN Abh21 abh21ori ON abh21ori.abh21id = abh21eve.abh21origem " +
					 "WHERE abh80tipo IN (:tiposTrab) AND fbd1001tipo = :tipoFbd1001 AND fbd1001valor > 0 " + whereEventosNaoImprimir +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbd10.class) +
					 " ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDepartamentos != null && !idsDepartamentos.isEmpty()) query.setParameter("idsDepartamentos", idsDepartamentos);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		query.setParameter("tiposTrab", tiposTrab);
		query.setParameter("tipoFbd1001", tipoFbd1001);
		
		return query.getListTableMap();
	}

	/**Método Diverso
	 * @return 	List - TableMap (Query de Trabalhadores, Departamento, Tipo Trabalhadores)
	 */
	public List<TableMap> findAbh80sByDeptoAndTipo(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Set<Integer> tiposTrab) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamentos != null && !idsDepartamentos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
	
		String sql = "SELECT * FROM Abh80 " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "LEFT JOIN Aap03 ON aap03id = abh05cbo " +
					 "WHERE abh80tipo IN (:tiposTrab) " +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Abh80.class) +
					 "ORDER BY abh80codigo, abb11codigo";
			
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDepartamentos != null && !idsDepartamentos.isEmpty()) query.setParameter("idsDepartamentos", idsDepartamentos);
		query.setParameter("tiposTrab", tiposTrab);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de Contrato de Trabalho)
	 */
	public List<TableMap> findDadosFbd10sByContratoTrabalho(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Set<Integer> tiposTrab, int complemento) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamentos != null && !idsDepartamentos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
	
		String sql = "SELECT abh80id, abb11id, abh80nome, abh80endereco, abh80bairro, aag0201nome, abh80cpf, abh80nascData, abh80mae, abh80pis, abh80cep, aag02uf, abh80numero, abh80salario, abh80hs, abh80salTipo, " +
					 "abh80ctpsnum, abh80ctpsserie, abh80ctpsEe, abh80complem, abh80dtadmis, fbd10dtRes, fbd10apData, abh03nome, abh03codES, abh03cnpj, abh02ni, " +
					 "abh06trctCod, abh06causa,abh06trctdescr, fbd10dtAfast, fbd10trab, fbd10salario, aap14codigo as categ, aap14descr as descrCateg,  " +
					 "(CASE " +
					 "WHEN abh80prazoConTrab = 1 THEN 'Indeterminado' " +
					 "WHEN abh80prazoConTrab = 2 THEN 'Determinado em dias' "+
					 "ELSE 'Determinado a um fato' " +
					 "END) as tpcontrato, fbd10ssRem " +
					 "FROM Fbd10 " +
					 "INNER JOIN Abh80 ON abh80id = fbd10trab " +
					 "INNER JOIN Abh03 ON abh03id = abh80sindSindical " +
					 "LEFT JOIN Aap14 ON aap14id = abh80categ " +
					 "INNER JOIN Abh06 ON abh06id = fbd10causa " +
					 "LEFT JOIN Aag0201 ON aag0201id = abh80municipio " +
					 "LEFT JOIN Aag02 ON aag02id = aag0201uf " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "INNER JOIN Abh02 ON abh02id = abh80lotacao "+
					 "WHERE fbd10nc = :complemento AND abh80tipo IN (:tiposTrab) " +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbd10.class) +
					 " ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDepartamentos != null && !idsDepartamentos.isEmpty()) query.setParameter("idsDepartamentos", idsDepartamentos);
		query.setParameter("tiposTrab", tiposTrab);
		query.setParameter("complemento", complemento);
		
		return query.getListTableMap();

	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de CAE e Rescisão)
	 */
	public List<TableMap> findCAEsByRescisao(String codAbh20) {

		String sql = "SELECT abh20id, abh20codigo, abh20nome FROM Abh20 WHERE UPPER(abh20codigo) LIKE :codAbh20 AND LENGTH(abh20codigo) = 4 " + getSamWhere().getWherePadrao("AND", Abh20.class) + " ORDER BY abh20codigo";
		Query query = getSession().createQuery(sql);
		query.setParameter("codAbh20", codAbh20 + "%");
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	String (Query de Codigo do Agrupamento de Eventos)
	 */
	public String findCodigoByIdCAE(Long idAbh20) {

		String sql = "SELECT abh20codigo FROM Abh20 WHERE abh20id = :idAbh20 " + getSamWhere().getWherePadrao("AND", Abh20.class) + " ORDER BY abh20codigo";
		Query query = getSession().createQuery(sql);
		query.setParameter("idAbh20", idAbh20);
		
		return query.getUniqueResult(ColumnType.STRING);
	}

	/**Método Diverso
	 * @return 	List - TableMap (Query de dados CAE e Rescisão)
	 */
	public List<TableMap> findDadosFba01011sCAEByRescisao(Long abh80id, LocalDate data, String codAbh20, String[] eventosNaoImprimir) {
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";

		String sql = "SELECT abh2101cvr, (CASE WHEN fba01011refHoras > 0 THEN fba01011refHoras WHEN fba01011refDias > 0 THEN fba01011refDias ELSE fba01011refUnid END) as totalRef, fba01011valor " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abh2101 ON abh2101evento = abh21id " +
					 "INNER JOIN Abh20 ON abh20id = abh2101cae " +
					 "WHERE fba0101trab = :abh80id AND fba0101dtCalc = :data AND fba0101tpVlr = 4 AND fba01011valor > 0 AND abh20codigo = :codAbh20 " +
					 whereEventosNaoImprimir +
					 getSamWhere().getWherePadrao("AND", Abh21.class);

		Query query = getSession().createQuery(sql);
		query.setParameter("abh80id", abh80id);
		query.setParameter("data", data);
		query.setParameter("codAbh20", codAbh20);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query do Termo Homologação e Quitação)
	 */
	public List<TableMap> findDadosFbd10sByTermoHomologacaoAndQuitacao(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Set<Integer> tiposTrab, int complemento) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamentos != null && !idsDepartamentos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
	
		String sql = "SELECT abh80id, abh80nome, abh80pis, abh80nascData, abh80ctpsnum, abh80ctpsserie, abh80ctpsee, abh80mae, abh80cpf, abh80dtadmis, fbd10dtRes, fbd10dtAfast, fbd10apData, fbd10dtPagto, " +
					 "abh06trctCod, abh06codigo, abh06causa,abh06trctdescr, abh06saqueFgts, aap14codigo as codCateg, aap14descr as descrCateg, abh03codigo, abh03nome, abh03codES, abh03cnpj " +
					 "FROM Fbd10 " +
					 "INNER JOIN Abh80 ON abh80id = fbd10trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh03 ON abh03id = abh80sindSindical " +
					 "LEFT JOIN Aap14 ON aap14id = abh80categ " +
					 "INNER JOIN Abh06 ON abh06id = fbd10causa " +
					 "WHERE fbd10nc = :complemento AND abh80tipo IN (:tiposTrab) " +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbd10.class) +
					 " ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDepartamentos != null && !idsDepartamentos.isEmpty()) query.setParameter("idsDepartamentos", idsDepartamentos);
		query.setParameter("tiposTrab", tiposTrab);
		query.setParameter("complemento", complemento);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query dos dados Abh80 abh21)
	 */
	public List<TableMap> findDadosFba01011sByIdAbh80AndIdAbh21AndPeriodo(Long idAbh80, Long idAbh21, LocalDate dtInicial, LocalDate dtFinal, String[] eventosNaoImprimir) {
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";

		String sql = "SELECT abh21unidPagto as unid, DATE_PART('MONTH', fba0101dtCalc) as mes, DATE_PART('YEAR', fba0101dtCalc) as ano, SUM(fba01011refHoras) as ref, SUM(fba01011valor) as valor " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "WHERE fba0101trab = :idAbh80 AND abh21id = :idAbh21 AND fba0101dtCalc BETWEEN :dtInicial AND :dtFinal " +
					 getSamWhere().getWherePadrao("AND", Fba0101.class) + whereEventosNaoImprimir +
					 "GROUP BY abh21unidPagto, DATE_PART('MONTH', fba0101dtCalc), DATE_PART('YEAR', fba0101dtCalc) " +
					 "ORDER BY DATE_PART('YEAR', fba0101dtCalc), DATE_PART('MONTH', fba0101dtCalc)";
		
		Query query = getSession().createQuery(sql);
		
		query.setParameter("idAbh80", idAbh80);
		query.setParameter("idAbh21", idAbh21);
		query.setParameter("dtInicial", dtInicial);
		query.setParameter("dtFinal", dtFinal);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query dos dados de Trabalhadores e CAE Mês e Ano)
	 */
	public List<TableMap> findDadosFba01011sByIdTrabalhadorAndIdCAEAndMesAndAno(Long idAbh80, Long idAbh20, int mes, int ano, String[] eventosNaoImprimir) {
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";
	
		String sql = "SELECT abh2101cvr, fba01011valor " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abh2101 ON abh2101evento = abh21id " +
					 "WHERE fba01011tipo IN (0, 3, 4) AND fba0101trab = :idAbh80 AND abh2101cae = :idAbh20 AND DATE_PART('MONTH', fba0101dtCalc) = :mes AND DATE_PART('YEAR', fba0101dtCalc) = :ano " +
					 whereEventosNaoImprimir +
					 getSamWhere().getWherePadrao("AND", Fba0101.class);
		
		Query query = getSession().createQuery(sql);
		query.setParameter("idAbh80", idAbh80);
		query.setParameter("idAbh20", idAbh20);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	JasperPrint (Emissão dos Avisos e Rescisão)
	 */
	public JasperPrint getDadosRescisao(List<Long> idsTrabalhador, Set<Integer> tiposTrab, List<Long> idsDepartamento, int complemento, Long caeRend, Long caeDesc, Long caeRemMesAnt, boolean isRescisao, boolean isTermoQuit, boolean isTermoHomol, boolean isMaiorRem, boolean isDetalheMR, boolean isPedidoDem, LocalDate dtDemissao, boolean isNotDispensa, LocalDate dtDispensa, boolean isNotDispensaJC, LocalDate dtDispensaJC, boolean isAvPrevEmpresa, LocalDate dtAvPrevEmpresa, boolean isAvPrevEmpregado, LocalDate dtAvPrevEmpregado, boolean isImpVerbasZeradas, String[] eventosNaoImprimir) {
		List<TableMap> listDados = getFbd10sByTrabalhadorAndDepto(idsTrabalhador, idsDepartamento, tiposTrab, complemento);
		List<TableMap> listFinalDados = new ArrayList<>();
		
		for(TableMap dados : listDados) {
			
			BigDecimal salario = dados.getBigDecimal("abh80salario") == null ? new BigDecimal(0) : dados.getBigDecimal("abh80salario");
			int horasSem = dados.getInteger("abh80hs");
			def salarioMes = 0;
			def salarioDia = 0;
			def salarioHora = 0;
			def salarioSem = 0;
			
			String uniPagto = buscarUnidadeDePagamentoPorId(dados.get("abh80unidPagto")); // Unidade de pagamento - 1-hora / 2-dia / 5-mês
			
			if(uniPagto == "1") { 					// hora
				salarioSem = salario * horasSem;
				salarioMes = salarioSem * 5;
				salarioMes = round(salarioMes, 2);
			}else if(uniPagto == "2") { 			// dia
				salarioMes = salario * 30;
				salarioSem = round(salarioMes, 2);
			}else {                                 //Mês
				salarioMes = round(salario, 2);
			}
			
			dados.put("salarioMes", salarioMes);
			dados.put("salarioDia", salarioDia);
			dados.put("salarioHora", salarioHora);
			
			listFinalDados.add(dados);
		}
		
		JasperPrint print = null;

		//Relatório de solicitação de maior remuneração.
		JasperReport report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R1");
		if(isMaiorRem) {
			TableMapDataSource dsPrincipal = new TableMapDataSource(listFinalDados);

			//Sub-relatório de maior remuneração (SALÁRIOS).
			List<TableMap> mapSubFb201Sal = findFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 0, eventosNaoImprimir);
			dsPrincipal.addSubDataSource("DsSub1R1", mapSubFb201Sal, "fbd10id", "fbd1001res");

			//Sub-relatório de maior remuneração (FÉRIAS VENCIDAS).
			List<TableMap> mapSubFb201FV = findFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 1, eventosNaoImprimir);
			dsPrincipal.addSubDataSource("DsSub2R1", mapSubFb201FV, "fbd10id", "fbd1001res");

			//Sub-relatório de maior remuneração (FÉRIAS PROPORCIONAIS).
			List<TableMap> mapSubFb201FP = findFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 2, eventosNaoImprimir);
			dsPrincipal.addSubDataSource("DsSub3R1", mapSubFb201FP, "fbd10id", "fbd1001res");

			//Sub-relatório de maior remuneração (13º SALÁRIO).
			List<TableMap> mapSubFb20113 = findFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 3, eventosNaoImprimir);
			dsPrincipal.addSubDataSource("DsSub4R1", mapSubFb20113, "fbd10id", "fbd1001res");

			print = processarRelatorio(report, dsPrincipal);
		}
		
		//Relatório de detalhamento da composição da maior remuneração.
		if(isDetalheMR) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R11");
			
			//Sub-relatório de maior remuneração (SALÁRIOS).
			List<TableMap> listDadosFb201Sal = findDadosFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 0, eventosNaoImprimir);
			
			for(TableMap mapDadosFb201Sal : listDadosFb201Sal) {
				String detalhe = comporDetalhamentoDoEvento(mapDadosFb201Sal.getLong("abh80id"), mapDadosFb201Sal.getLong("abh21id"), mapDadosFb201Sal.getInteger("abh21unid"), mapDadosFb201Sal.getDate("fbd10ssPi"), mapDadosFb201Sal.getDate("fbd10ssPf"), eventosNaoImprimir);
				mapDadosFb201Sal.put("detalhe", detalhe);
				mapDadosFb201Sal.put("unidade", mapDadosFb201Sal.getInteger("abh21unidPagto") == null || mapDadosFb201Sal.getInteger("abh21unidPagto") == 0 ? "Valor" : "Horas");
			}
			
			//Sub-relatório de maior remuneração (FÉRIAS VENCIDAS).
			List<TableMap> listDadosFb201FV = findDadosFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 1, eventosNaoImprimir);
			
			for(TableMap mapDadosFb201FV : listDadosFb201FV) {
				String detalhe = comporDetalhamentoDoEvento(mapDadosFb201FV.getLong("abh80id"), mapDadosFb201FV.getLong("abh21id"), mapDadosFb201FV.getInteger("abh21unid"), mapDadosFb201FV.getDate("fbd10fvPai"), mapDadosFb201FV.getDate("fbd10fvPaf"), eventosNaoImprimir);
				mapDadosFb201FV.put("detalhe", detalhe);
				mapDadosFb201FV.put("unidade", mapDadosFb201FV.getInteger("abh21unidPagto") == null || mapDadosFb201FV.getInteger("abh21unidPagto") == 0 ? "Valor" : "Horas");
			}
			
			//Sub-relatório de maior remuneração (FÉRIAS PROPORCIONAIS).
			List<TableMap>  listDadosFb201FP = findDadosFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 2, eventosNaoImprimir);
			
			for(TableMap mapDadosFb201FP : listDadosFb201FP) {
				String detalhe = comporDetalhamentoDoEvento(mapDadosFb201FP.getLong("abh80id"), mapDadosFb201FP.getLong("abh21id"), mapDadosFb201FP.getInteger("abh21unidPagto"), mapDadosFb201FP.getDate("fbd10fpPai"), mapDadosFb201FP.getDate("fbd10fpPaf"), eventosNaoImprimir);
				mapDadosFb201FP.put("detalhe", detalhe);
				mapDadosFb201FP.put("unidade", mapDadosFb201FP.getInteger("abh21unidPagto") == null || mapDadosFb201FP.getInteger("abh21unidPagto") == 0 ? "Valor" : "Horas");
			}
			
			//Sub-relatório de maior remuneração (13º SALÁRIO).
			List<TableMap> listDadosFb20113 = findDadosFbd1001sByWhereTrabAndWhereDeptoAndTipoTrabAndTipoMR(idsTrabalhador, idsDepartamento, tiposTrab, 3, eventosNaoImprimir);
			
			for(TableMap mapDadosFb20113 : listDadosFb20113) {
				String detalhe = comporDetalhamentoDoEvento(mapDadosFb20113.getLong("abh80id"), mapDadosFb20113.getLong("abh21id"), mapDadosFb20113.getInteger("abh21unidPagto"), mapDadosFb20113.getDate("fbd10dtPi"), mapDadosFb20113.getDate("fbd10dtPf"), eventosNaoImprimir);
				mapDadosFb20113.put("detalhe", detalhe);
				mapDadosFb20113.put("unidade", mapDadosFb20113.getInteger("abh21unidPagto") == null || mapDadosFb20113.getInteger("abh21unidPagto") == 0 ? "Valor" : "Horas");
			}
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listFinalDados);
			dsPrincipal.addSubDataSource("DsSub1R11", listDadosFb201Sal, "fbd10id", "fbd1001res");
			dsPrincipal.addSubDataSource("DsSub2R11", listDadosFb201FV, "fbd10id", "fbd1001res");
			dsPrincipal.addSubDataSource("DsSub3R11", listDadosFb201FP, "fbd10id", "fbd1001res");
			dsPrincipal.addSubDataSource("DsSub4R11", listDadosFb20113, "fbd10id", "fbd1001res");
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		
		//Relatório de pedido de dispensa.
		if(isPedidoDem) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R2");

			List<TableMap> dsPrincipal = findAbh80sByDeptoAndTipo(idsTrabalhador, idsDepartamento, tiposTrab);

			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);

			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de notificação de dispensa.
		if(isNotDispensa) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R3");

			List<TableMap> dsPrincipal = findAbh80sByDeptoAndTipo(idsTrabalhador, idsDepartamento, tiposTrab);
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
		
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de notificação de dispensa por justa causa.
		if(isNotDispensaJC) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R4");

			List<TableMap> dsPrincipal = findAbh80sByDeptoAndTipo(idsTrabalhador, idsDepartamento, tiposTrab);

			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de aviso prévio do empregador.
		if(isAvPrevEmpresa) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R5");

			List<TableMap> dadosAbh80 = findAbh80sByDeptoAndTipo(idsTrabalhador, idsDepartamento, tiposTrab);

			for(int i = 0; i < dadosAbh80.size(); i++) {
				int diasAvPrevInd = 30;
				
				LocalDate calInicial = dadosAbh80.get(i).getDate("abh80dtadmis");
				LocalDate calFinal = dtAvPrevEmpresa;

				while(ChronoUnit.DAYS.between(calInicial, calFinal) >= 365) {
					diasAvPrevInd += 3;
					calInicial = calInicial.plusYears(1);
				}
			
				if(diasAvPrevInd > 90) diasAvPrevInd = 90;
				dadosAbh80.get(i).put("diasAvPrevInd", diasAvPrevInd);
			}

			TableMapDataSource dsPrincipal = new TableMapDataSource(dadosAbh80);
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);

			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de aviso prévio do empregado.
		if(isAvPrevEmpregado) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R6");

			List<TableMap> dsPrincipal = findAbh80sByDeptoAndTipo(idsTrabalhador, idsDepartamento, tiposTrab);

			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de rescisão.
		Map<Long, BigDecimal> valoresLiquidos = new HashMap<Long, BigDecimal>();
		if(isRescisao) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R8");

			//Dados da rescisão.
			List<TableMap> listDadosFbd10 = findDadosFbd10sByContratoTrabalho(idsTrabalhador, idsDepartamento, tiposTrab, complemento);

			//Pega todos os CAE's que fazem parte do grupo a partir do código de 2 digitos informados na tela.
			String codCaeRend = findCodigoByIdCAE(caeRend);
			String codCaeDesc = findCodigoByIdCAE(caeDesc);
			
			List<TableMap> listCAEsRend = findCAEsByRescisao(codCaeRend);
			List<TableMap> listCAEsDed = findCAEsByRescisao(codCaeDesc);

			//MultiMaps que irão armazenar os dados do sub-relatório de rendimentos e deduções.
			List<TableMap> mapDadosSubRend = new ArrayList<>();
			List<TableMap> mapDadosSubDed = new ArrayList<>();

			for(TableMap mapDadosFbd10 : listDadosFbd10) {

				//Obtêm a inscrição da empresa tomadora e adiciona no mapa.
				String ni = mapDadosFbd10.getString("abh02ni");
				mapDadosFbd10.put("tomadora", ni);

				Long abh80id = mapDadosFbd10.getLong("fbd10trab");
				LocalDate data = mapDadosFbd10.getDate("fbd10dtRes");
				BigDecimal totalRend = new BigDecimal(0);
				BigDecimal totalDed = new BigDecimal(0);

				//Rendimentos.
				for(TableMap rsCAEsRend : listCAEsRend) {

					BigDecimal ref = new BigDecimal(0);
					BigDecimal valor = new BigDecimal(0);

					List<TableMap> listValores = findDadosFba01011sCAEByRescisao(abh80id, data, rsCAEsRend.getString("abh20codigo"), eventosNaoImprimir);
					if(listValores != null && listValores.size() > 0) {
						for(TableMap rsValores : listValores) {
							int compValor = rsValores.getInteger("abh2101cvr");
							ref = compValor == 0 ? ref.add(rsValores.getBigDecimal("totalRef")) : ref.subtract(rsValores.getBigDecimal("totalRef"));
							valor = compValor == 0 ? valor.add(rsValores.getBigDecimal("fba01011valor")) : valor.subtract(rsValores.getBigDecimal("fba01011valor"));
						}
					}
					
					//Formata o CAE incluindo a referência onde constar '#'.
					String cae = rsCAEsRend.getString("abh20nome").replaceAll("#", ref.toString());
					
					//Seta os valores no Map de rendimentos.
					if(isImpVerbasZeradas || (valor.compareTo(new BigDecimal(0)) != 0)) {
						TableMap mapSubRend = new TableMap();
						mapSubRend.put("cae", cae);
						mapSubRend.put("valor", valor.abs());
						mapSubRend.put("idTrabRend", abh80id);
						totalRend = totalRend.add(valor);
						mapDadosSubRend.add(mapSubRend);
					}
				}

				//Deduções.
				for(TableMap rsCAEsDed : listCAEsDed) {
					BigDecimal ref = new BigDecimal(0);
					BigDecimal valor = new BigDecimal(0);

					List<TableMap> listValores = findDadosFba01011sCAEByRescisao(abh80id, data, rsCAEsDed.getString("abh20codigo"), eventosNaoImprimir);
					if(listValores != null && listValores.size() > 0) {
						for(TableMap rsValores : listValores) {;

							int compValor = rsValores.getInteger("abh2101cvr");
							ref = compValor == 0 ? ref.add(rsValores.getBigDecimal("totalRef")) : ref.subtract(rsValores.getBigDecimal("totalRef"));
							valor = compValor == 0 ? valor.add(rsValores.getBigDecimal("fba01011valor")) : valor.subtract(rsValores.getBigDecimal("fba01011valor"));
						}
					}
					//Formata o CAE incluindo a referência onde constar '#'.
					String cae = rsCAEsDed.getString("abh20nome").replaceAll("#", ref.toString());
					
					//Seta os valores no Map de deduções.
					if(isImpVerbasZeradas || (valor.compareTo(new BigDecimal(0)) != 0)) {
						TableMap mapSubDed = new TableMap();
						mapSubDed.put("cae", cae);
						mapSubDed.put("valor", valor.abs());
						mapSubDed.put("idTrabDed", abh80id);
						totalDed = totalDed.add(valor);
						mapDadosSubDed.add(mapSubDed);
					}
				}
				
				//Seta os totais no mapa principal.
				mapDadosFbd10.put("totalRend", totalRend);
				mapDadosFbd10.put("totalDed", totalDed);
				mapDadosFbd10.put("totalLiquido", totalRend.add(totalDed));
				valoresLiquidos.put(abh80id, totalRend.add(totalDed));
				
				//Remuneração do mês anterior.
				BigDecimal remMesAnt = new BigDecimal(0);
				if(caeRemMesAnt != null) {
					LocalDate calRemMesAnt = data;
					LocalDate calRemMesAntAjustado = calRemMesAnt.minusMonths(1);
					String codCaeRemMesAnt = findCodigoByIdCAE(caeRemMesAnt);
					List<TableMap> listCAEsRemMesAnt = findCAEsByRescisao(codCaeRemMesAnt);
					
					if(listCAEsRemMesAnt != null && listCAEsRemMesAnt.size() > 0) {
						for(TableMap rsCAEsRemMesAnt : listCAEsRemMesAnt) {
							List<TableMap> listSalarios = findDadosFba01011sByIdTrabalhadorAndIdCAEAndMesAndAno(abh80id, rsCAEsRemMesAnt.getLong("abh20id"), calRemMesAntAjustado.plusMonths(1).getMonthValue(), calRemMesAntAjustado.getYear(), eventosNaoImprimir);
							if(listSalarios != null && listSalarios.size() > 0) {
								for(TableMap rsSalarios : listSalarios) {
									if(rsSalarios.getInteger("abh2101cvr") == 0) {
										remMesAnt = remMesAnt.add(rsSalarios.getBigDecimal("fba01011valor"));
									}else {
										remMesAnt = remMesAnt.subtract(rsSalarios.getBigDecimal("fba01011valor"));
									}
								}
							}
						}
					}
				}
				
				if(remMesAnt.compareTo(new BigDecimal(0)) == 0){
					String uniPagto = buscarUnidadeDePagamentoPorId(mapDadosFbd10.getLong("abh80uniPagto"));
					if(uniPagto == "1") {
						remMesAnt = mapDadosFbd10.getBigDecimal("abh80salario").multiply(new BigDecimal(mapDadosFbd10.getInteger("abh80hs"))).multiply(new BigDecimal(5)).setScale(2, RoundingMode.HALF_EVEN);
					}else if(uniPagto == "2") {
						remMesAnt = mapDadosFbd10.getBigDecimal("abh80salario").multiply(new BigDecimal(30));
					}else {
						remMesAnt = mapDadosFbd10.getBigDecimal("abh80salario");
					}
				}
				mapDadosFbd10.put("remMesAnt", remMesAnt);
			}
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listDadosFbd10);
			dsPrincipal
			dsPrincipal.addSubDataSource("DsCAEsRend", mapDadosSubRend, "abh80id", "idTrabRend");
			dsPrincipal.addSubDataSource("DsCAEsDed", mapDadosSubDed, "abh80id", "idTrabDed");
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		
		//Relatório de termo de quitação.
		if(isTermoQuit) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R9");

			List<TableMap> dadosFbd10 = findDadosFbd10sByTermoHomologacaoAndQuitacao(idsTrabalhador, idsDepartamento, tiposTrab, complemento);
			
			for(int i = 0; i < dadosFbd10.size(); i++) {
				dadosFbd10.get(i).put("totalLiquido", valoresLiquidos.get(dadosFbd10.get(i).get("abh80id")));
			}
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(dadosFbd10);
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);

			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de termo de homologação.
		if(isTermoHomol) {
			report = carregarArquivoRelatorio("SFP_EmissaoDosAvisosERescisao_R10");

			List<TableMap> dadosFbd10 = findDadosFbd10sByTermoHomologacaoAndQuitacao(idsTrabalhador, idsDepartamento, tiposTrab, complemento);

			for(int i = 0; i < dadosFbd10.size(); i++) {
				dadosFbd10.get(i).put("totalLiquido", valoresLiquidos.get(dadosFbd10.get(i).get("abh80id")));
			}
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(dadosFbd10);
				
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);

			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		return print;
	}
	
	/**Método Diverso
	 * @return 	String (Detalhe do Evento)
	 */
	private String comporDetalhamentoDoEvento(Long idAbh80, Long idAbh21, Integer unidAbh21, LocalDate dtInicial, LocalDate dtFinal, String[] eventosNaoImprimir) {
		String retorno = "";
		
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		List<TableMap> listDadosFb011s = findDadosFba01011sByIdAbh80AndIdAbh21AndPeriodo(idAbh80, idAbh21, dtInicial, dtFinal, eventosNaoImprimir);
		if(listDadosFb011s != null && listDadosFb011s.size() > 0) {
			for(TableMap rsDadosFb011s : listDadosFb011s) {
				retorno += rsDadosFb011s.getInteger("mes") + "/" + rsDadosFb011s.getInteger("ano") + " = " + (unidAbh21 == null || unidAbh21 == 0 ? nb.format(rsDadosFb011s.getBigDecimal("valor").setScale(2, RoundingMode.HALF_EVEN)) : nb.format(rsDadosFb011s.getBigDecimal("ref").setScale(2,  RoundingMode.HALF_EVEN))) + "; ";
			}
		}
		return retorno;
	}
	
	private String buscarUnidadeDePagamentoPorId(Long aap18id) {
		return getSession().createCriteria(Aap18.class)
			   .addFields("aap18codigo")
			   .addWhere(Criterions.eq("aap18id", aap18id))
			   .get(ColumnType.STRING);
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIEVtaXNzw6NvIGRvcyBBdmlzb3MgZSBSZXNjaXPDo28iLCJ0aXBvIjoicmVsYXRvcmlvIn0=