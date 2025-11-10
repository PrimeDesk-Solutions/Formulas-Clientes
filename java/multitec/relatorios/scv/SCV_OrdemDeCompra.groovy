package multitec.relatorios.scv;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac10
import sam.model.entities.cb.Cbb01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCV_OrdemDeCompra extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCV - Ordem de Compra";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("isCriada", true);
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");

		Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		LocalDate[] dataEmissao = getIntervaloDatas("dtEmissao");
		List<Long> idUsuarios = getListLong("usuarios");
		List<Integer> mpm = getListInteger("mpm");
		List<Long> idItens = getListLong("itens");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		Integer aprovacaoOC = getInteger("aprovacaoOC");
		Integer finalizacaoOC = getInteger("finalizacaoOC");
		Set<Integer> status = obterStatusOC();
		
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());
		params.put("TITULO_RELATORIO", "Ordem de Compra");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", dataEmissao == null ? "": "Período: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

		List<TableMap> dados = buscarDadosRelatorio(dataEmissao, idUsuarios, idItens, mpm, numeroInicial, numeroFinal, aprovacaoOC, finalizacaoOC, status)
		
		return gerarPDF("SCV_OrdemDeCompra", dados)
	}

	private List<TableMap> buscarDadosRelatorio(LocalDate[] dataEmissao, List<Long> idUsuarios, List<Long> idItens, List<Integer> mpm, Integer numeroInicial, Integer numeroFinal, Integer aprovacaoOC, Integer finalizacaoOC, Set<Integer> status) {
		
		String whereDataEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? " and cdoc.abb01data >= '" + dataEmissao[0] + "' and cdoc.abb01data <= '" + dataEmissao[1] + "'": "";
		String whereNumero = numeroInicial != null && numeroFinal != null ? " and cdoc.abb01num >= '" + numeroInicial + "' and cdoc.abb01num <= '" + numeroFinal + "'": "";
		String whereIdUsuario = idUsuarios != null && idUsuarios.size() > 0 ? " and cdoc.abb01operUser IN (:idUsuarios)" : "";
		String whereTipo = (!mpm.contains(-1)) ? " and abm01tipo IN (:mpm)" : "";
		String whereItem  = idItens != null && idItens.size() > 0 ? " AND abm01id IN (:idItens) " : "";
		String whereStatus = status != null && status.size() > 0 ? " and cbb01status in (:status)": "";
		
		Parametro p1 = idUsuarios != null && idUsuarios.size() > 0 ? Parametro.criar("idUsuarios", idUsuarios) : null;
		Parametro p2 = idItens != null && idItens.size() > 0 ? Parametro.criar("idItens", idItens) : null;
		Parametro p3 = (!mpm.contains(-1)) ? Parametro.criar("mpm", mpm) : null;
		Parametro p4 = status != null && status.size() > 0 ? Parametro.criar("status", status) : null;
		
		String sql = "SELECT cdoc.abb01num AS numdoc, cdoc.abb01data AS dtemis, abb10codigo, abb10descr,cbb01dtConc, cbb01cancData, " + 
				"     (SELECT MAX(abb0103data) FROM Abb0103 WHERE cdoc.abb01id = abb0103central) AS abb0103data, " + 
				"     abm01tipo, abm01codigo, abm01na, cbb0101qt, " +
				"     (SELECT cdped.abb01data FROM Abb01 As cdped INNER JOIN Abb0102 ON cdped.abb01id = abb0102doc INNER JOIN Eaa01 ON eaa01central = cdped.abb01id WHERE abb0102central = cdoc.abb01id ORDER BY abb0102id LIMIT 1 ) AS dtped, " +
				"     (SELECT cdped.abb01num FROM Abb01 As cdped INNER JOIN Abb0102 ON cdped.abb01id = abb0102doc INNER JOIN Eaa01 ON eaa01central = cdped.abb01id WHERE abb0102central = cdoc.abb01id ORDER BY abb0102id LIMIT 1 ) AS numped " +
				"		FROM Cbb0101 " +
				"	   INNER JOIN Cbb01 ON cbb01id = cbb0101oc  " +
				"	   INNER JOIN Abb01 cdoc ON cdoc.abb01id = cbb01central  " + 	
				"	    LEFT JOIN Abe01 ON abe01id = cdoc.abb01ent  " +
				"	    LEFT JOIN Abb10 ON abb10id = cdoc.abb01operCod  " +
				"	    LEFT JOIN abb11 ON cdoc.abb01id = cbb0101depto " +
				"	   INNER JOIN Abm01 ON abm01id = cbb0101item" +
				getSamWhere().getWherePadrao(" WHERE ", Cbb01.class) +
				whereDataEmissao +
				whereNumero +
				whereIdUsuario +
				whereTipo +
				whereItem +
				whereStatus +
				" order by cdoc.abb01num, abm01codigo"

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4);
		return receberDadosRelatorio;
	}
	
	private Set<Integer> obterStatusOC(){
		Set<Integer> tiposItem = new HashSet<>();

		if((boolean) get("isCriada")) tiposItem.add(0);
		if((boolean) get("isAprovada")) tiposItem.add(1);
		if((boolean) get("isCotada")) tiposItem.add(2);
		if((boolean) get("isConcluida")) tiposItem.add(3);
		if((boolean) get("isCancelada")) tiposItem.add(4);

		if(tiposItem.size() == 0) {
			tiposItem = null;	
		}
		return tiposItem;
	}

}
//meta-sis-eyJkZXNjciI6IlNDViAtIE9yZGVtIGRlIENvbXByYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==