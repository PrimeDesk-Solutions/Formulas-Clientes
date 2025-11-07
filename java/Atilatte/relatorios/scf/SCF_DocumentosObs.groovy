package Atilatte.relatorios.scf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import sam.model.entities.da.Daa01;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac1001
import br.com.multiorm.Query;
import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SCF_DocumentosObs extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Documentos Financeiros Obs - LCR";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(LocalDate.now());
		filtrosDefault.put("ordem", "0");
		filtrosDefault.put("classe","0");
		filtrosDefault.put("opcVcto","0");
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("tipoData","0");
		filtrosDefault.put("exportar","0");

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> Emprs = getListLong("Empresa");
		Integer ordem = getInteger("ordem");
		Integer classe = getInteger("classe");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> documento = getListLong("documento");
		List<Long> entidade = getListLong("entidade");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		Integer opcVcto = getInteger("opcVcto")
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Integer tipoData = getInteger("tipoData");
		LocalDate[] data = getIntervaloDatas("data");
		List<Long> port = getListLong("portador");
		List<Long> oper = getListLong("oper");
		Integer exportar = getInteger("exportar");

		switch (classe) {
			case 0: params.put("TITULO_RELATORIO", "Documentos à Receber");
				break;
			case 1: params.put("TITULO_RELATORIO", "Documentos Recebidos");
				break;
			case 2: params.put("TITULO_RELATORIO", "Documentos à Pagar");
				break;
			case 3: params.put("TITULO_RELATORIO", "Documentos Pagos");
				break;
		}

		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("OPCVCTO", opcVcto);
		
		if (dataEmissao != null) {
			params.put("PERIODO", "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		if (dataVenc != null) {
			params.put("PERIODO", "Período Vencimento: " + dataVenc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataVenc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		if (data != null) {
			if (tipoData == 0) {
				params.put("PERIODO", "Período Pagamento: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
			} else {
				params.put("PERIODO", "Período Recebimento: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
			}
		}

		List<TableMap> listaDocumentos = buscaDocumentos(Emprs, ordem, classe, numeroInicial, numeroFinal, documento, entidade, dataVenc, opcVcto, dataEmissao,  tipoData, data, port, oper);

		for (int i = 0; i < listaDocumentos.size(); i++) {
			String previsao = null;

			if (listaDocumentos.get(i).getInteger("daa01previsao").equals(1)) {
				previsao = "P";
			} else {
				previsao = "R";
			}
			listaDocumentos.get(i).put("previsao", previsao);
			
			Long dias = DateUtils.dateDiff(LocalDate.now(), listaDocumentos.get(i).getDate("daa01dtVctoN"), ChronoUnit.DAYS);
			listaDocumentos.get(i).put("dias", dias);
		}

		if(exportar == 0){
			return gerarPDF("SCF_DocumentosObs", listaDocumentos);
		}else{
			return gerarXLSX("SCF_DocumentosObs", listaDocumentos);
		}
	}

	private List<TableMap> buscaDocumentos(List<Long> Emprs, Integer ordem, Integer classe, Integer numeroInicial, Integer numeroFinal, List<Long> documento, List<Long> entidade, LocalDate[] dataVenc, Integer opcVcto, LocalDate[] dataEmissao, Integer tipoData, LocalDate[] data, List<Long> port, List<Long> oper) {

		List<Long> idsGc = obterGCbyEmpresa(Emprs, "Da");

		String whereIdEmpresa = idsGc != null && idsGc.size() > 0 ? " WHERE aac01id in(:idEmprs)" : getSamWhere().getWherePadrao(" WHERE ", Daa01.class);
		String whereEG = Emprs!= null && Emprs.size() > 0 ? " AND aac10.aac10id IN (:idEmprs)" : " AND Daa01eg = " + getVariaveis().getAac10().getAac10id();
		String whereRp = classe.equals(0)  || classe.equals(1)? " AND daa01.daa01rp = 0 " : " AND daa01.daa01rp = 1 ";
		String whereDtBaixa = classe.equals(0) || classe.equals(2) ? " AND daa01.daa01dtBaixa IS NULL " : classe.equals(1) || classe.equals(3) ? " AND daa01.daa01dtBaixa IS NOT NULL " : "";
		String whereTipoDoc = documento != null && documento.size() != 0 ? " AND aah01.aah01id IN (:idDocumentos) " : "";
		String whereEnt = entidade != null && entidade.size() != 0 ? " AND abe01.abe01id IN (:idEntidade) " : "";
		String whereVencimento = "";
		if(opcVcto == 0){
			whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " AND daa01.daa01dtVctoN >= '" + dataVenc[0] + "' AND daa01.daa01dtVctoN <= '" + dataVenc[1] + "'": "";
		}else{
			whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " AND daa01.daa01dtVctoR >= '" + dataVenc[0] + "' AND daa01.daa01dtVctoR <= '" + dataVenc[1] + "'": "";
		}
		String whereEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? getWhereDataInterval("AND", dataEmissao, "abb01.abb01data") : "";
		String wherePort = port != null && port.size() != 0 ?  "AND abf15.abf15id IN (:idPort) " : "";
		String whereOper = oper != null && oper.size() != 0 ?  "AND abf16.abf16id IN (:idOper) " : "";
		String whereData = data != null && data[0] != null && data[1] != null ?
																tipoData == 0 ? " AND daa01.daa01dtPgto >= '" + data[0] + "' and daa01.daa01dtPgto <= '" + data[1] + "' "
																: " AND daa01.daa01dtBaixa >= '" + data[0] + "' and daa01.daa01dtBaixa <= '" + data[1] + "' " : "";
		String orderBy = ordem.equals(0) ? " ORDER BY abb01.abb01num, abb01parcela" : opcVcto == 0 ? " ORDER BY daa01.daa01dtVctoN" : " ORDER BY daa01.daa01dtVctoR";
																
																
		String sql = "SELECT abe01.abe01codigo, abe01.abe01na, aah01.aah01codigo, aah01.aah01nome, abb01.abb01num, abb01.abb01serie, " +
				" abb01.abb01parcela, abb01.abb01quita, abb01.abb01data, daa01.daa01dtVctoN, daa01.daa01dtVctoR, daa01.daa01dtPgto, " +
				" daa01.daa01dtBaixa, daa01.daa01valor, daa01.daa01previsao, daa01obs, abf15.abf15codigo, abf15.abf15nome, abf16.abf16codigo, abf16.abf16nome " +
				" FROM daa01 AS daa01 " +
				" INNER JOIN aac01 as aac01 ON daa01gc = aac01id" +
				" INNER JOIN aac10 as aac10 ON daa01eg = aac10id" +
				" LEFT JOIN abb01 AS abb01 ON abb01id = daa01central " +
				" LEFT JOIN abe01 AS abe01 ON abe01id = abb01ent " +
				" LEFT JOIN abf15 AS abf15 ON abf15id = daa01port " +
				" LEFT JOIN abf16 AS abf16 ON abf16id = daa01oper " +
				" LEFT JOIN aah01 AS aah01 ON aah01id = abb01tipo " +
				whereIdEmpresa + whereEG + whereRp + whereDtBaixa + " AND abb01.abb01num >= :numeroInicial AND abb01.abb01num <= :numeroFinal " +
				whereTipoDoc + whereEnt + whereVencimento + whereEmissao + whereData + wherePort + whereOper + orderBy;

		Query query = getSession().createQuery(sql);
		query.setParameter("numeroInicial", numeroInicial);
		query.setParameter("numeroFinal", numeroFinal);
		query.setParameter("idEmprs",  Emprs != null && Emprs.size() > 0 ? Emprs : idsGc);

		if(documento != null && ! documento.isEmpty()) query.setParameter("idDocumentos", documento);
		if(entidade != null && ! entidade.isEmpty()) query.setParameter("idEntidade", entidade);
		if(port != null && ! port.isEmpty()) query.setParameter("idPort", port);
		if(oper != null && ! oper.isEmpty()) query.setParameter("idOper", oper);

		return query.getListTableMap();
	}

	private Long obterGC(){
		Long id = getSession().createCriteria(Aac10.class)
						   .addFields("aac1001gc")
						   .addJoin(Joins.join("Aac1001", "aac10id = aac1001empresa"))
						   .addWhere(Criterions.eq("aac1001tabela", "da"))
						   .addWhere(Criterions.eq("aac10id", obterEmpresaAtiva().aac10id).ignoreCase(false))
						   .get(ColumnType.LONG);
		return id;
	}

	public List<Long> obterGCbyEmpresa(List<Long> empresa, String tabela) {
		Criterion whereEmpresa = empresa != null ? Criterions.in("aac1001empresa", empresa) : null;
		Criterion whereTabela = tabela != null ? Criterions.eq("aac1001tabela", tabela) : null;

		return getSession().createCriteria(Aac1001.class)
				.addFields("aac1001gc")
				.addWhere(whereEmpresa)
				.addWhere(whereTabela)
				.getList(ColumnType.LONG);
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgRmluYW5jZWlyb3MgT2JzIC0gTENSIiwidGlwbyI6InJlbGF0b3JpbyJ9