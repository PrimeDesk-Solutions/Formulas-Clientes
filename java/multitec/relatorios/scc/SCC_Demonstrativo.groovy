package multitec.relatorios.scc

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.da.Daa01
import sam.model.entities.dc.Dcb01
import sam.model.entities.dc.Dcc01
import sam.model.entities.dc.Dcd01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

class SCC_Demonstrativo extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCC - Demonstrativo";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("periodo", DateUtils.getStartAndEndMonth(MDate.date()));
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idRepresentantes = getListLong("representantes");
		LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
		boolean isSaltarPagina = get("isSaltarPagina");
		
		params.put("TITULO_RELATORIO", "Demonstrativo das Comissões");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		params.put("StreamSub1", carregarArquivoRelatorio("SCC_Demonstrativo_S1"));
		params.put("StreamSub2", carregarArquivoRelatorio("SCC_Demonstrativo_S2"));
		
		List<TableMap> calculos = buscarCalculos(dataPeriodo, idRepresentantes);
		TableMapDataSource dsPrincipal = new TableMapDataSource(calculos);

		List<TableMap> documentos = new ArrayList();
		List<TableMap> lancamentos = new ArrayList();
		for (tm in calculos) {
			List<TableMap> documentosPorCalc = buscarDocumentosDoCalculo(tm.getLong("dcd01id"));
			List<TableMap> lancamentosPorCalc = buscarLancamentosDoCalculo(tm.getLong("dcd01id"));
			if (Utils.isEmpty(documentosPorCalc) && Utils.isEmpty(lancamentosPorCalc)) continue;
			
			if (!Utils.isEmpty(documentosPorCalc)) {
				for (tmDoc in documentosPorCalc) {
					String sqlDaa01 = " SELECT daa01dtVctoN " +
									  " FROM Daa01 " +
									  getSamWhere().getWherePadrao("WHERE", Daa01.class) +
									  " AND daa01central = :dcb01central ";
					
					TableMap daa01Tm = getAcessoAoBanco().buscarUnicoTableMap(sqlDaa01, Parametro.criar("dcb01central", tmDoc.getLong("dcb01central")));
					tmDoc.put("daa01dtvcton", daa01Tm != null ? daa01Tm.getDate("daa01dtVctoN") : null);
					tmDoc.put("dtcalc", tm.getDate("abb01data"));
				}
			}
			
			documentos.addAll(documentosPorCalc);
			lancamentos.addAll(lancamentosPorCalc);
		}
		
		dsPrincipal.addSubDataSource("DSSub1", documentos, "dcd01id", "dcb01calculo");
		dsPrincipal.addSubDataSource("DSSub2", lancamentos, "dcd01id", "dcc01calculo");
		
		return gerarPDF("SCC_Demonstrativo", dsPrincipal, "representante", isSaltarPagina);
	}

	private List<TableMap> buscarCalculos(LocalDate[] dataPeriodo, List<Long> idRepresentantes) {
		String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " AND abb01data >= '" + dataPeriodo[0] + "' AND abb01data <= '" + dataPeriodo[1] + "' ": "";
		String whereIdsRepresentante = Utils.isEmpty(idRepresentantes) ? "" : " AND abe01id IN (:idRepresentantes) ";
		Parametro parametro = Utils.isEmpty(idRepresentantes) ? null : Parametro.criar("idRepresentantes", idRepresentantes);

		String sql = " SELECT dcd01id, abe01codigo, abe01nome, abb01data, dcd01dtCredI, dcd01dtCredF, dcd01liquido, dcd01central " +
					 " FROM Dcd01 INNER JOIN Abb01 ON abb01id = dcd01central " +
					 " INNER JOIN Abe01 ON abe01id = abb01ent " +
					 getSamWhere().getWherePadrao("WHERE", Dcd01.class) +
					 wherePeriodoData +
					 whereIdsRepresentante +
					 " ORDER BY abe01codigo ";

		List<TableMap> calculos = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
		return calculos;
	}

	private List<TableMap> buscarDocumentosDoCalculo(Long dcd01id) {
		Parametro parametro = Parametro.criar("dcd01id", dcd01id);

		String sql = " SELECT aah01codigo, aah01nome, abb01num, abb01data, abb01parcela, abb01quita, abe01codigo, abe01nome, dcb01dtCredito, dcb01bc, dcb01taxa, dcb01valor, dcb01calculo, dcb01central " +
					 " FROM Dcb01 " +
					 " INNER JOIN Abb01 ON abb01id = dcb01central " +
					 " INNER JOIN Abe01 ON abe01id = abb01ent " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 getSamWhere().getWherePadrao("WHERE", Dcb01.class) +
					 " AND dcb01calculo = :dcd01id " +
					 " ORDER BY abe01codigo, abb01num";

		List<TableMap> documentos = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
		return documentos;
	}

	private List<TableMap> buscarLancamentosDoCalculo(Long dcd01id) {
		Parametro parametro = Parametro.criar("dcd01id", dcd01id);

		String sql = " SELECT aah01codigo, aah01nome, abb01num, abb01data, dcc01dtDisp, dcc01valor, dcc01historico, dcc01oper, dcc01calculo, CASE WHEN dcc01oper > 0 THEN 'D' ELSE 'S' END AS dcc01oper " +
					 " FROM Dcc01 INNER JOIN Abb01 ON abb01id = dcc01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 getSamWhere().getWherePadrao("WHERE", Dcc01.class) +
					 " AND dcc01calculo = :dcd01id " +
					 " ORDER BY aah01codigo, abb01num";

		List<TableMap> lancamentos = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
		return lancamentos;
	}
}
//meta-sis-eyJkZXNjciI6IlNDQyAtIERlbW9uc3RyYXRpdm8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=