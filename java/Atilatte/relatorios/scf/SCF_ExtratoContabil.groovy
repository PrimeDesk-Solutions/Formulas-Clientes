/*

Ultima Alteração: 21/10/2025 17:15
Autor: LEONARDO

*/

package Atilatte.relatorios.scf

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

import java.time.LocalDate

class SCF_ExtratoContabil extends RelatorioBase {

	@Override
	String getNomeTarefa() {
		return "SCF - Extrato Contabil"
	}

	@Override
	Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("ordem", "0");
		filtrosDefault.put("classe","0");
		filtrosDefault.put("tp","0");
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("exportar","0");
		filtrosDefault.put("op","0");
		filtrosDefault.put("opEnt","1");
		filtrosDefault.put("dataPer", DateUtils.getStartAndEndMonth(LocalDate.now()));

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	DadosParaDownload executar() {
		Integer classe = getInteger("classe");
		Integer ordem = getInteger("ordem");
		Integer exportar = getInteger("exportar");
		Integer tipo = getInteger("tp");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> portador = getListLong("port");
		List<Long> documento = getListLong("documento");
		String entInicial = getString("entInicial");
		String entFinal = getString("entFinal");
		List<Long> empresa = getListLong("emps");
		LocalDate[] dataPer = getIntervaloDatas("dataPer");
		Integer op = getInteger("op");
		Integer opEnt = getInteger("opEnt");

		params.put("EMPRESA", empresa == null || empresa.size() == 0 ? obterEmpresaAtiva().getAac10rs() : buscarEmpresa(empresa))
		params.put("PERIODO", "Período: " + dataPer[0].format("dd/MM/yyyy") + " a " + dataPer[1].format("dd/MM/yyyy"))

		if(classe == 0) params.put("TITULO", "a receber/recebidos")
		if(classe == 1) params.put("TITULO", "a pagar/pagos")

		List<TableMap> dados = buscarDados(empresa, classe, ordem, tipo, numeroInicial, numeroFinal, portador, documento, entInicial, entFinal, dataPer, opEnt, op)

		List<TableMap> novosDados = ajusteDeDados(dados, dataPer)

		if(exportar == 0){
			if (op == 0){
				return gerarPDF("SCF_ExtratoContabil", novosDados);
			}
			else{
				return gerarPDF("SCF_ExtratoContabil_R1", novosDados);
			}
		}else{
			if (op == 0){
				return gerarXLSX("SCF_ExtratoContabil", novosDados);
			}
			else{
				return gerarXLSX("SCF_ExtratoContabil_R1", novosDados);
			}
		}
	}

	private List<TableMap> buscarDados(List<Long> empresa, Integer classe, Integer ordem, Integer tipo, Integer numeroInicial, Integer numeroFinal,
									   List<Long> portador, List<Long> documento, String entInicial, String entFinal, LocalDate[] dataPer, Integer opEnt, Integer op){

		/*FILTRO DE A RECEBER OU A PAGAR*/
		String whereReceberPagar = classe == 0 ? " AND daa01.daa01rp = 0 \n" : " AND daa01.daa01rp = 1 \n"

		/*FILTRO DE NUMERO INICIAL E FINAL*/
		String whereNumini = numeroInicial != null ? " AND abb01.abb01num >= " + numeroInicial  : ""
		String whereNumFim = numeroFinal != null ? "\n AND abb01.abb01num <= " + numeroFinal : ""

		/*FILTRO DE REAL OU PREVISÃO*/
		String whereTipo = tipo == 0 ? "" : " AND daa01previsao = 0 \n"

		/*FILTRO DE PORTADOR*/
		String wherePortador = portador != null ? " AND abf15.abf15id IN (:idPort) \n" : ""

		/*FILTRO DE TIPO DE DOCUMENTO*/
		String whereTipoDoc = documento != null ? " AND aah01.aah01id IN (:idDocumentos) \n" : ""

		/*FILTRO DE ENTIDADE*/
		String whereEntidade = entInicial != null  && entFinal != null ? " AND abe01.abe01codigo BETWEEN '"+entInicial+"' AND '"+entFinal+"' \n" : ""

		/*FILTRO DE EMPRESA*/
		String whereEmpresa = empresa != null && empresa.size() != 0 ? " AND aac10id IN (:idEmpresa)" : obterWherePadrao("daa01", "AND")

		String nomeEnt = opEnt == 0 ? " abe01na " : " abe01nome ";

		/*MONTA ORDER BY*/
		String orderBy = ""
		if (ordem == 0) {
			orderBy = " ORDER BY abe01codigo, daa01dtLcto, abb01num, abb01parcela";
		}
		else {
			orderBy = " ORDER BY "+ nomeEnt;
		}

		String JoinCta = classe == 0 ? "LEFT JOIN abe02 ON abe02.abe02ent = abe01.abe01id\n" +
				"INNER JOIN abe0201 ON abe0201.abe0201cli = abe02.abe02id\n " +
				"LEFT JOIN abc10 ON abc10.abc10id = abe0201.abe0201cta \n" : "LEFT JOIN Abe03 ON abe03ent = abe01id "+
				"INNER JOIN Abe0301 ON abe0301for = abe03id "+
				"LEFT JOIN abc10 ON abc10.abc10id = abe0301.abe0301cta \n";
		String WhereCta = classe == 0 ? " AND (abe0201seq = 1 OR abe0201seq ISNULL) AND abe02gc = daa01gc " : " AND (abe0301seq = 1 OR abe0301seq ISNULL) AND abe03gc = daa01gc";


		/*********************************** PARAMETROS *****************************************/

		Parametro paramPort = portador != null ? Parametro.criar("idPort", portador) : null
		Parametro paramDocumento = documento != null ? Parametro.criar("idDocumentos", documento) : null
		Parametro paramEmpresa = empresa != null && empresa.size() != 0 ? Parametro.criar("idEmpresa", empresa) : null

		/************************************** SQL *********************************************/

		String sql ="SELECT \n" +
				"abe01codigo as ent_cod, "+nomeEnt+" as ent_nome, abc10codigo as contabil,\n" +
				"abb01data as dt_emis, aah01codigo as tp, abb01num as doc_num, abb01operData as dt_lanc, \n" +
				"abb01serie as doc_serie, abb01parcela as doc_parc, abb01quita as doc_qta, \n" +
				"abf15nome as portador, daa01dtVctoN as dt_vcto, daa01dtBaixa as dt_baixa, \n" +
				"aac10codigo as emp_cod, aac10na as emp_nome, dab10historico as obs, daa01dtLcto,\n" +
				"\n" +
				"CASE WHEN (daa01json ->>'juros') NOTNULL \n" +
				"THEN (daa01json ->>'juros')::numeric ELSE 0.00 END as jurosq, \n" +
				"\n" +
				"CASE WHEN (daa01json ->>'multa') NOTNULL \n" +
				"THEN (daa01json ->>'multa')::numeric ELSE 0.00 END as multaq, \n" +
				"\n" +
				"CASE WHEN (daa01json ->>'encargos') NOTNULL \n" +
				"THEN (daa01json ->>'encargos')::numeric ELSE 0.00 END as encargosq, \n" +
				"\n" +
				"CASE WHEN (daa01json ->>'desconto') NOTNULL \n" +
				"THEN (daa01json ->>'desconto')::numeric ELSE 0.00 END as descontoq, \n" +
				" \n" +
				"daa01valor as vlr\n" +
				"\n" +
				"FROM Daa01\n" +
				"LEFT JOIN abb01 ON abb01.abb01id = Daa01.daa01central\n" +
				"LEFT JOIN abe01 ON abe01.abe01id = abb01.abb01ent\n" +
				JoinCta +
				"LEFT JOIN abb10 ON abb10.abb10id = abb01.abb01opercod\n" +
				"LEFT JOIN aah01 ON aah01.aah01id = abb01.abb01tipo\n" +
				"LEFT JOIN abf15 ON abf15.abf15id = Daa01.daa01port\n" +
				"LEFT JOIN abf16 ON abf16.abf16id = Daa01.daa01oper\n" +
				"LEFT JOIN aac10 ON aac10.aac10id = Daa01.daa01eg\n" +
				"LEFT JOIN dab10 ON dab10.dab10central = abb01id\n" +
				"\n WHERE ((daa01dtLcto <= '" + dataPer[1] + "' AND (daa01dtBaixa IS NULL OR daa01dtBaixa >= '" + dataPer[1] + "')) OR (daa01dtBaixa >= '"+dataPer[0]+"' AND daa01dtBaixa <= '"+dataPer[1]+"') OR (daa01dtLcto >= '"+dataPer[0]+"' AND daa01dtLcto <= '"+dataPer[1]+"')) \n" +
				WhereCta +
				whereReceberPagar +
				whereNumini +
				whereNumFim +
				whereTipo +
				wherePortador +
				whereTipoDoc +
				whereEntidade +
				whereEmpresa +
				orderBy

		return getAcessoAoBanco().buscarListaDeTableMap(sql, paramPort, paramDocumento, paramEmpresa)
	}

	/*COLOCANDO CAMPOS ADICIONAIS*/
	private List<TableMap> ajusteDeDados(List<TableMap> dados, LocalDate[] dataPer){

		List<TableMap> dadosAux = new ArrayList<>()
		BigDecimal juros = BigDecimal.ZERO
		BigDecimal multas = BigDecimal.ZERO
		BigDecimal encargos = BigDecimal.ZERO
		BigDecimal descontos = BigDecimal.ZERO
		BigDecimal jme = BigDecimal.ZERO


		for (dado in dados){
			LocalDate dt_baixa = dado.getDate("dt_baixa")
			TableMap tb = new TableMap()
			tb.putAll(dado)

			tb.put("aRealizar", BigDecimal.ZERO)
			tb.put("realizado", BigDecimal.ZERO)

			// COLOCANDO REALIZADO E A REALIZAR
			if (dt_baixa != null){
				if (dataPer != null && dataPer.size() > 0){
					if(dt_baixa <= dataPer[1] && dt_baixa >= dataPer[0]){
						tb.put("realizado", dado.getBigDecimal_Zero("vlr"))
					}
					else{
						tb.put("aRealizar", dado.getBigDecimal_Zero("vlr"))
						tb.put("dt_baixa", null)
					}
				}
			}
			else {
				tb.put("aRealizar", dado.getBigDecimal_Zero("vlr"))
			}

			// COLOCANDO JME
			juros = dado.getBigDecimal_Zero("jurosq")
			multas = dado.getBigDecimal_Zero("multaq")
			encargos = dado.getBigDecimal_Zero("encargosq")

			if (tb.get("realizado") > 0){
				tb.put("descontoq", BigDecimal.ZERO)
				jme = juros + multas + encargos
			}

			tb.put("jme", jme)
			jme = 0.0

			//SEPARANDO DOCUMENTOS DEPOIS DO PERIODO
			LocalDate dt_emis = dado.getDate("dt_emis")

			if (dt_emis <= dataPer[1]){
				dadosAux.add(tb)
			}
		}

		return dadosAux

	}

	private String buscarEmpresa(List<Long> empresas) {

		String whereEmpresas = empresas != null && empresas.size() > 0 ? "where aac10id in (:empresas)" : ""

		String sql = "select string_agg(aac10na, ' / ') AS aac10na_concatenado\n" +
				"from aac10\n" +
				whereEmpresas

		Parametro p1 = empresas != null && empresas.size() > 0 ? criarParametroSql("empresas", empresas) : null

		return getAcessoAoBanco().obterString(sql,p1)
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEV4dHJhdG8gQ29udGFiaWwiLCJ0aXBvIjoicmVsYXRvcmlvIn0=