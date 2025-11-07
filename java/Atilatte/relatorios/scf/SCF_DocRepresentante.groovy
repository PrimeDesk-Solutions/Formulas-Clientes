package Atilatte.relatorios.scf;

import java.time.LocalDate
import java.time.temporal.ChronoUnit


import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SCF_DocRepresentante extends RelatorioBase{

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("imprimir","1","numeroInicial","0000000000", "numeroFinal", "9999999999", "codigoInicial","0000000000","codigoFinal","9999999999","classe","0");
	}

	@Override
	public DadosParaDownload executar() {

		def numInicial = getLong("numeroInicial")
		def numFinal = getLong("numeroFinal")
		def codInicial = getString("representanteInicial")
		def codFinal = getString("representanteFinal")
		def impressao = getInteger("classe")
		def vencimento = getIntervaloDatas("vencimento")
		def emissao = getIntervaloDatas("emissao")
		def imprimir  = getInteger("imprimir")
		adicionarParametro("aac10rs", obterEmpresaAtiva().getAac10rs())
		def titulo = impressao == 0 ? "Documentos a Receber por Representantes" :"Documentos a Receber e Recebidos por Representantes"
		adicionarParametro("titulo",titulo)
		def periodo = vencimento == null && emissao == null ? "Data": vencimento == null ? "Emissão" : "Vencimento"
		def dataIni = vencimento == null && emissao == null ? "01/01/1990": vencimento == null ? emissao[0].format("dd/MM/yyyy") : vencimento[0].format("dd/MM/yyyy")
		def dataFim = vencimento == null && emissao == null ? "31/12/2050": vencimento == null ? emissao[1].format("dd/MM/yyyy") : vencimento[1].format("dd/MM/yyyy")
		adicionarParametro("periodo", periodo)
		adicionarParametro("dataInicial", dataIni)
		adicionarParametro("dataFinal", dataFim)


		List<TableMap> dados = new ArrayList()
		def representantes = buscarRepresentante(codInicial,codFinal)

		for(representante in representantes) {

			comporDocumentos(dados,representante, 0, numInicial, numFinal, impressao, vencimento, emissao)
			comporDocumentos(dados,representante, 1, numInicial, numFinal, impressao, vencimento, emissao)
			comporDocumentos(dados,representante, 2, numInicial, numFinal, impressao, vencimento, emissao)
			comporDocumentos(dados,representante, 3, numInicial, numFinal, impressao, vencimento, emissao)
			comporDocumentos(dados,representante, 4, numInicial, numFinal, impressao, vencimento, emissao)
		}
		ordenar(dados,1)
		ordenar(dados,2)
		ordenar(dados,3)
		ordenar(dados,4)
		ordenar(dados,5)
		if(imprimir == 0) {
			return gerarXLSX("SCF_DocRepresentanteXLS",dados);
		}else {
			return gerarPDF(dados);
		}
		
	}

	private ordenar(List<TableMap> dados, int ordenacao) {
		Collections.sort(dados, new Comparator<TableMap>() {
			@Override
			public int compare(TableMap  dado1, TableMap  dado2) {
				
				if(ordenacao == 1 && dado1.getString("abb01parcela") != null && dado2.getString("abb01parcela") != null) {
					return dado1.getString("abb01parcela").compareTo(dado2.getString("abb01parcela"))
				}else if(ordenacao == 2 && dado1.getString("abb01serie") != null && dado2.getString("abb01serie") != null) {
					return dado1.getString("abb01serie").compareTo(dado2.getString("abb01serie"))
				}else if(ordenacao == 3 && dado1.getInteger("abb01num") != null && dado2.getInteger("abb01num") != null) {
					return dado1.getInteger("abb01num").compareTo(dado2.getInteger("abb01num"))
				}else if(ordenacao == 4 && dado1.getString("aah01codigo") != null && dado2.getString("aah01codigo") != null) {
					return dado1.getString("aah01codigo").compareTo(dado2.getString("aah01codigo"))
				}else if(ordenacao == 5 && dado1.getLong("abe01id_rep") != null && dado2.getLong("abe01id_rep") != null){
					return dado1.getLong("abe01id_rep").compareTo(dado2.getLong("abe01id_rep"))
				}
				return 0
			}
		});
	}
	private List<TableMap> buscarRepresentante(String codInicial, String codFinal) {
		def whereCodInicial = codInicial != null ?  " and abe01codigo >= :codInicial " : ""
		def whereCodfinal = codFinal != null ? " and abe01codigo <= :codFinal " : ""
		def sql = "select abe01id as abe01id_rep, abe01na as abe01na_rep , abe01codigo as abe01codigo_rep from abe01  where abe01rep = 1 "+
				whereCodInicial+whereCodfinal
		
		def p1 = codInicial != null ? criarParametroSql("codInicial",codInicial.toString()) : null
		def p2 = codFinal != null ? criarParametroSql("codFinal",codFinal.toString()) : null
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2)
	}

	private comporDocumentos(List<TableMap> dados, TableMap dadosRepresentante, Integer representante, Long numInicial, Long numFinal, Integer impressao, LocalDate[] vencimentos, LocalDate[] emissao){

		def daa01rep = representante == 0 ? " daa01rep0 " : representante == 1 ? " daa01rep1 " : representante == 2 ? " daa01rep2 " : representante == 3 ? " daa01rep3 " : " daa01rep4"
		def daa01txComis = representante == 0 ? " daa01txComis0 " : representante == 1 ? " daa01txComis1 " : representante == 2 ? " daa01txComis2 " : representante == 3 ? " daa01txComis3 " : " daa01txComis4"
		def whereRepresentante = " and "+daa01rep+" = " + dadosRepresentante.getString("abe01id_rep")
		def whereNumInicial = numInicial != null ?  " and abb01num >= :numInicial " : ""
		def whereNumfinal = numFinal != null ? " and abb01num <= :numFinal " : ""
		def whereVencimento = vencimentos != null ? " and daa01dtVctoN between '"+vencimentos[0]+"' and '"+vencimentos[1]+"' ":""
		def whereEmissao = emissao != null ? " and abb01data between '"+emissao[0]+"' and '"+emissao[1]+"'":""
		def wherePagamento = impressao == 0 ? "and daa01dtPgto is null" : ""


		def sql = " select "+daa01txComis+" as daa01txComis, daa01valor, daa01dtPgto, daa01dtVctoN, abb01num, abb01serie, "+
				" abb01parcela,abb01quita, abb01data, abe01codigo,abe01na, aah01codigo,abf15codigo, abf15nome, abf16codigo, daa01json from daa01 "+
				" inner join abb01 on abb01id = daa01central "+
				" left join abe01 on abe01id = abb01ent "+
				" inner join aah01 on aah01id = abb01tipo "+
				" left join abf15 on abf15id = daa01port "+
				" left join abf16 on abf16id = daa01oper "+
				obterWherePadrao("Daa01", "where")  +whereRepresentante +
				whereNumInicial + whereNumfinal + whereVencimento +
				whereEmissao + wherePagamento +
				" and daa01rp = 0 "+
				" and daa01previsao = 0" 

		def p1 = numInicial != null ? criarParametroSql("numInicial",numInicial) : null
		def p2 = numFinal != null ? criarParametroSql("numFinal",numFinal) : null
		def resultados = getAcessoAoBanco().buscarListaDeTableMap(sql, p1,p2)
		for(resultado in resultados) {
			TableMap daa01json = resultado.getTableMap("daa01json")
			LocalDate dataAtual =  LocalDate.now()
			def dias = ChronoUnit.DAYS.between(dataAtual, resultado.getDate("daa01dtVctoN"))
			def desconto = BigDecimal.ZERO
			def JM = BigDecimal.ZERO
			if(daa01json != null) {
				def juros = daa01json.getBigDecimal("juros") == null ? BigDecimal.ZERO : daa01json.getBigDecimal("juros")
				def multa = daa01json.getBigDecimal("multa") == null ? BigDecimal.ZERO : daa01json.getBigDecimal("multa")
				desconto = daa01json.getDate("data_lim") == null ? daa01json.getBigDecimal("desconto") == null ? BigDecimal.ZERO : daa01json.getBigDecimal("desconto") : BigDecimal.ZERO
				JM = dias < 0 && resultado.getDate("daa01dtPgto") == null ? (juros * (dias *-1)) + multa : BigDecimal.ZERO
			}

			resultado.putAll(dadosRepresentante)
			resultado.put("dias", dias)
			resultado.put("JM",JM)
			resultado.put("desconto", desconto)
			dados.add(resultado)
		}
	}



	@Override
	public String getNomeTarefa() {
		return "SCF - Documento por Representante - El Tech";
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50byBwb3IgUmVwcmVzZW50YW50ZSAtIExDUiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==