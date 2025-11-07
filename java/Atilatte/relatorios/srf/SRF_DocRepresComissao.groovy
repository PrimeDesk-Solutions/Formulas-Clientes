package Atilatte.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SRF_DocRepresComissao  extends RelatorioBase{
	
	@Override
	public String getNomeTarefa() {
		return "SRF - Doc. por Representante e Comissão - LCR";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("resumoOperacao", "1");
		filtrosDefault.put("nomeEntidade", "0");
		filtrosDefault.put("impressao", "0");
		filtrosDefault.put("devolucao", true)
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idEntidade = getListLong("entidade");
		List<Long> idTipoDocumento = getListLong("tipo");
		List<Long> idCfop = getListLong("cfop")
		List<Long> idPcd = getListLong("pcd")
		Integer numeroInicial = get("numeroInicial") == null || get("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial")
		Integer numeroFinal = get("numeroFinal") == null || get("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal")
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntSai = getIntervaloDatas("dataEntSai");
		Integer resumoOperacao = getInteger("resumoOperacao");
		Integer nomeEntidade = getInteger("nomeEntidade");
		Integer imprimir = getInteger("impressao")
		List<Long> representante = getListLong("representante")
		
		String periodo = ""
		if(dataEmissao != null) {
			periodo = "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}else if(dataEntSai) {
			periodo = "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}
		
		params.put("empressa", obterEmpresaAtiva().getAac10rs())
		params.put("periodo",periodo)
	
		List<TableMap> representantes = buscarRepresentante(representante)
		List<TableMap> dadosRelatorios = new ArrayList()
		
		for(rep in representantes) {
			List<TableMap> dados = new ArrayList()
			
			comporDocumentos(dados, rep, 0, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
			comporDocumentos(dados, rep, 1, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
			comporDocumentos(dados, rep, 2, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
			comporDocumentos(dados, rep, 3, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
			comporDocumentos(dados, rep, 4, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
			
			ordenar(dados,1)
			ordenar(dados,2)
			
			
			for(dado in dados) {
				comporCfops(dado)
				BigDecimal taxa = dado.getBigDecimal("eaa01txcomis") == null ? BigDecimal.ZERO : dado.getBigDecimal("eaa01txcomis") / 100
				BigDecimal total = dado.getBigDecimal("eaa01totitens") == null ? BigDecimal.ZERO : dado.getBigDecimal("eaa01totitens")
				dado.put("comissao", (taxa * total) )
				dadosRelatorios.add(dado)
			}
		}
		if(imprimir == 1) return gerarXLSX(dadosRelatorios)
		return gerarPDF(dadosRelatorios)
		
	}
	
	
	private List<TableMap> buscarRepresentante(List<Long> representante) {
		
		String whereRepresentantes = representante != null && representante.size() > 0 ? " and abe01id in (:representante) " : ""
		
		String sql = " select abe01id as abe01id_rep, abe01na as abe01na_rep , abe01codigo as abe01codigo_rep " +
					 " from abe01 " +
					 " where abe01rep = 1 " +
					 obterWherePadrao("abe01", "and") +
					 whereRepresentantes
		
		Parametro p1 = representante != null && representante.size() > 0 ? criarParametroSql("representante", representante) : null
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
	}
	
	private void comporCfops(TableMap dado) {
		
		String whereDoc = " where eaa0101doc = :docId "
		
		Parametro parametroDoc = Parametro.criar("docId", dado.getLong("eaa01id"))
		
		String sql = " select aaj15codigo from eaa01 " + 
					 " inner join eaa0101 on eaa0101doc = eaa01id " +
					 " inner join Abd01 on eaa01pcd = Abd01id " +
					 " inner join Abd02 on abd01ceFiscais = Abd02id " +
					 " inner join Aaj15 on abd02cfop = Aaj15id " + 
					 whereDoc
		
		List<String> resultados = getAcessoAoBanco().obterListaDeString(sql,parametroDoc)
		
		List<String> cfops = new ArrayList()
		String cfop = ""
		for(resultado in resultados) {
			if(!cfops.contains(resultado)) {
				cfops.add(resultado)
				cfop += cfop == "" ?  resultado : "/"+resultado
			}
		}
		
		dado.put("cfops", cfop)
	}
	
	private comporDocumentos(List<TableMap> dados, TableMap dadosRepresentante, Integer representante, Integer nomeEntidade, Long numInicial, Long numFinal, LocalDate[] emissao, LocalDate[] entSai, Integer oper, List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> idCfop, List<Long> idPcd){
		
		String WhereOper = oper == 0 ? " and eaa01esMov = 0 " : " and eaa01esMov = 1 "
		String nomeEnt = "abe01nome" // nomeEntidade == 0 ? "abe01na" : abe01nome
		String eaa01rep = representante == 0 ? " eaa01rep0 " : representante == 1 ? " eaa01rep1 " : representante == 2 ? " eaa01rep2 " : representante == 3 ? " eaa01rep3 " : " eaa01rep4"
		String eaa01txComis = representante == 0 ? " eaa01txcomis0 " : representante == 1 ? " eaa01txcomis1 " : representante == 2 ? " eaa01txcomis2 " : representante == 3 ? " eaa01txcomis3 " : " eaa01txcomis4"
		String whereRepresentante = " and "+eaa01rep+" = " + dadosRepresentante.getString("abe01id_rep")
		String whereNumInicial = numInicial != null ?  " and abb01num >= :numInicial " : ""
		String whereNumfinal = numFinal != null ? " and abb01num <= :numFinal " : ""
		String whereEmissao = emissao != null ? " and abb01data between '"+emissao[0]+"' and '"+emissao[1]+"'":""
		String whereEntSai = entSai != null ? " and eaa01esdata between '"+entSai[0]+"' and '"+entSai[1]+"'":""
		String whereidEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01id in (:idEntidade) " : ""
		String whereidTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01id in (:idTipoDocumento) " : ""
		String whereidCfop = idCfop != null && idCfop.size() > 0 ? " and aaj15id in (:idCfop) " : ""
		String whereidPcd = idPcd != null && idPcd.size() > 0 ? " and eaa01pcd in (:idPcd) " : ""
		
		String sql = " select eaa01id,  "+eaa01txComis+" as eaa01txcomis, eaa01totdoc, eaa01totitens, aah01codigo, abb01num, abb01data, eaa01esdata, abe01codigo,"+nomeEnt+" as nomeEntidade,abe30nome  "+
					 " from eaa01 "+ 
					 " inner join Abd01 on eaa01pcd = Abd01id " +
					 " inner join Abd02 on abd01ceFiscais = Abd02id " + //Mudança de soma de Total de Documentos(eaa01totdoc)
					 " inner join Aaj15 on abd02cfop = Aaj15id " +		//para Total de Itens(eaa01totitens) no IReport
					 " inner join abb01 on abb01id = eaa01central "+
					 " left join abe30 on abe30id = eaa01cp "+
					 " left join aah01 on abb01tipo = aah01id "+
					 " left join abe01 on abe01id = abb01ent "+
					 obterWherePadrao("eaa01", "where")  +whereRepresentante +
					 whereNumInicial + whereNumfinal  +
					 whereEmissao  + whereEntSai + WhereOper +
					 whereidEntidade + whereidTipoDocumento + whereidCfop + whereidPcd +
					 " and eaa01clasdoc = 1 "+
					 " and eaa01cancData is null "+
					 " and eaa01nfestat <> 5 "+
					 " and eaa01nfestat <> 7 "
					 
		Parametro p1 = numInicial != null ? Parametro.criar("numInicial",numInicial) : null
		Parametro p2 = numFinal != null ? Parametro.criar("numFinal",numFinal) : null
		Parametro p3 = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null
		Parametro p4 = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null
		Parametro p5 = idCfop != null && idCfop.size() > 0 ? Parametro.criar("idCfop", idCfop) : null
		Parametro p6 = idPcd != null && idPcd.size() > 0 ? Parametro.criar("idPcd", idPcd) : null
		
		List<TableMap> resultados = getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2,p3,p4,p5,p6)
		for(resultado in resultados) {
			resultado.putAll(dadosRepresentante)
			
		}
		dados.addAll(resultados)
		
	}
	
	private ordenar(List<TableMap> dados, int ordenacao) {
		Collections.sort(dados, new Comparator<TableMap>() {
			@Override
			public int compare(TableMap  dado1, TableMap  dado2) {
				
				if(ordenacao == 1 && dado1.getInteger("abb01num") != null && dado2.getInteger("abb01num") != null) {
					return dado1.getInteger("abb01num").compareTo(dado2.getInteger("abb01num"))
				}else if(ordenacao == 2 && dado1.getLong("abe01id") != null && dado2.getLong("abe01id") != null){
					return dado1.getLong("abe01id").compareTo(dado2.getLong("abe01id"))
				}
				return 0
			}
		});
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvYy4gcG9yIFJlcHJlc2VudGFudGUgZSBDb21pc3PDo28gLSBMQ1IiLCJ0aXBvIjoicmVsYXRvcmlvIn0=