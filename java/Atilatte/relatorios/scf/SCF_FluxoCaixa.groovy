package Atilatte.relatorios.scf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac1001
import sam.model.entities.da.Daa01
import sam.model.entities.da.Dab01
import sam.model.entities.da.Dab0101

import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

class SCF_FluxoCaixa extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCF - Fluxo de Caixa - LCR";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(LocalDate.now());
		filtrosDefault.put("exportar","0");
		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		Integer exportar = getInteger("exportar");
		def idEmps = getListLong("emps");
		def idCcs = getListLong("ccs");
		def tipoDoc = getListLong("tipoDoc");
		def idEnts = getListLong("ents");
		def periodo = getIntervaloDatas("periodo");
		def saldoIni = get("saldoIni");
		saldoIni = !StringUtils.isNullOrEmpty(saldoIni) ? new BigDecimal(saldoIni.replace(",", ".")) : null;
		boolean sintetico = get("sintetico");

		adicionarParametro("TITULO_RELATORIO", "Fluxo de Caixa");
		adicionarParametro("EMPRESA", getVariaveis().getAac10().getAac10na());
		adicionarParametro("IMPRIMIR_SALDOINICIAL", true);
		adicionarParametro("SINTETICO", sintetico);

		if (periodo != null) {
			adicionarParametro("PERIODO", "Período Vencimento: " + periodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + periodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		List<TableMap> dados = buscarDadosRelatorio(idEmps, idCcs, idEnts, periodo, saldoIni, tipoDoc);

		if(exportar == 0 && !sintetico){
			return gerarPDF("SCF_FluxoCaixa", dados);
		}else if(exportar == 0 && sintetico){
			return gerarPDF("SCF_FluxoCaixaSintetico", dados);
		}else if(exportar == 1 && !sintetico) {
			return gerarXLSX("SCF_FluxoCaixa", dados);
		}else {
			return gerarXLSX("SCF_FluxoCaixaSintetico", dados);
		}
	}

	private List<TableMap> buscarDadosRelatorio(List<Long> idEmps, List<Long> idCcs, List<Long> idEnts, LocalDate[] periodo, BigDecimal saldoIni, List<Long> tipoDoc) {
		def dados = new ArrayList<TableMap>();

		def lctos = buscarLancamentosContas(idEmps, idCcs, idEnts, periodo, tipoDoc);

		def saldoInicial = saldoIni ?: buscarSaldoContasCorrentes(idCcs, idEmps);

		def saldoAtual = 0;

		for(lcto in lctos) {
			def tm = new TableMap();

			tm.put("saldoInicial", saldoInicial);

			tm.put("vencimento", lcto.get("daa01dtVctoR"));

			def entidade = lcto.getString("abe01codigo") + '-' + lcto.getString("abe01na");
			tm.put("entidade", entidade);

			def documento = lcto.getString("aah01codigo") + " " + lcto.getString("aah01nome") + " " + lcto.getInteger("abb01num");
			tm.put("documento", documento);

			def numDoc = lcto.getInteger("abb01num");
			tm.put("abb01num", numDoc)



			def parcela = lcto.getString("abb01parcela");
			tm.put("parcela", parcela);

			def rp = lcto.getInteger("daa01previsao") == 0 ? "R" : "P";
			tm.put("rp", rp);

			if(lcto.getInteger("daa01rp") == 1) {
				tm.put("pagar", lcto.getBigDecimal_Zero("daa01valor"));
				tm.put("receber", 0);
			}else {
				tm.put("pagar", 0);
				tm.put("receber", lcto.getBigDecimal_Zero("daa01valor"));
			}

			saldoAtual += (saldoInicial + tm.getBigDecimal_Zero("receber")) - tm.getBigDecimal_Zero("pagar");
			tm.put("saldoAtual", saldoAtual);

			tm.put("aab10user", lcto.getString("aab10user"));

			dados.add(tm);
		}

		return dados;
	}

	private List<TableMap> buscarLancamentosContas(List<Long> idEmps, List<Long> idCcs, List<Long> idEnts, LocalDate[] periodo, List<Long> tipoDoc) {
		List<Long> idsGc = obterGCbyEmpresa(idEmps, "Da");

		def whereGcs = idsGc != null && idsGc.size() > 0 ? " AND aac01id in(:idEmprs)" : getSamWhere().getWherePadrao(" AND ", Daa01.class);
		def whereEG = idEmps!= null && idEmps.size() > 0 ? " AND aac10.aac10id IN (:idEmprs)" : " AND Daa01eg = " + getVariaveis().getAac10().getAac10id();
		def whereEnt = idEnts != null && idEnts.size() > 0 ? " AND abb01ent IN (:idEnts) " : "";
		def whereVen = periodo != null ? " AND daa01dtVctoR BETWEEN :dtIni AND :dtFin " : "";
		def whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? " AND abb01tipo IN (:tipoDoc) " : "";

		def sql = " SELECT aab10user, daa01dtVctoR, abe01codigo, abe01na, abb01num, aah01codigo, aah01nome, daa01previsao, daa01rp, daa01valor, abb01parcela " +
				" FROM Daa01 " +
				" INNER JOIN Abb01 ON abb01id = daa01central " +
				" LEFT JOIN abb0103 ON abb0103central = abb01id "+
				" LEFT JOIN aab10 ON aab10id = abb0103user "+
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" INNER JOIN Aac01 ON aac01id = daa01gc "+
				" INNER JOIN aac10 as aac10 ON daa01eg = aac10id" +
				" LEFT JOIN Abe01 ON abe01id = abb01ent " +
				" WHERE TRUE " + whereGcs + whereEnt + whereVen + whereTipoDoc +
				" AND daa01dtPgto IS NULL "+
				" ORDER BY daa01dtVctoR,daa01id";

		def p1 = idEmps != null && idEmps.size() > 0 ? criarParametroSql("idEmprs", idEmps) : criarParametroSql("idEmprs", idsGc);
		def p2 = idCcs != null && idCcs.size() > 0 ? criarParametroSql("idCcs", idCcs) : null;
		def p3 = idEnts != null && idEnts.size() > 0 ? criarParametroSql("idEnts", idEnts) : null;
		def p4 = periodo != null ? criarParametroSql("dtIni", periodo[0]) : null;
		def p5 = periodo != null ? criarParametroSql("dtFin", periodo[1]) : null;
		def p6 = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;

		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4, p5, p6);
	}

	private BigDecimal buscarSaldoContasCorrentes(List<Long> idCcs, List<Long> idEmps) {
		List<Long> idsGc = obterGCbyEmpresa(idEmps, "Da");

		def whereCcs = idCcs != null && idCcs.size() > 0 ? " AND dab0101cc IN (:idCcs) " : "";
		def whereGcs = idsGc != null && idsGc.size() > 0 ? " AND dab01gc in(:idEmprs)" : getSamWhere().getWherePadrao(" AND ", Daa01.class);


		def sql = " SELECT SUM(dab0101saldo) FROM Dab0101 " +
				" INNER JOIN Dab01 ON dab01id = dab0101cc " +
				" WHERE dab0101ano = (SELECT MAX(dab0101ano) FROM Dab0101 WHERE TRUE " + whereCcs + ") " +
				" AND dab0101mes = (SELECT MAX(dab0101mes) FROM Dab0101 WHERE TRUE " + whereCcs + ") " +
				whereCcs + whereGcs;

		def p1 = idCcs != null && idCcs.size() > 0 ? criarParametroSql("idCcs", idCcs) : null;
		def p2 = idEmps != null && idEmps.size() > 0 ? criarParametroSql("idEmprs", idEmps) : criarParametroSql("idEmprs", idsGc);

		return getAcessoAoBanco().obterBigDecimal(sql, p1, p2);
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
//meta-sis-eyJkZXNjciI6IlNDRiAtIEZsdXhvIGRlIENhaXhhIC0gTENSIiwidGlwbyI6InJlbGF0b3JpbyJ9