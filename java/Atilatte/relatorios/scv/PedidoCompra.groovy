package Atilatte.relatorios.scv;

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import java.time.LocalDate
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SCV_Pedidos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCV - Pedido de Compra";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("impressao", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		def tipos = getListLong("tipos");
		def numeroInicial = getInteger("numeroInicial");
		def numeroFinal = getInteger("numeroFinal");
		def entidades = getListLong("entidades");
		def emissao = getIntervaloDatas("emissao");
		
					
		def titulo = "Pedidos de Compra"
		adicionarParametro("aac10rs", obterEmpresaAtiva().getAac10rs())
		adicionarParametro("titulo", titulo)
		adicionarParametro("dataIni", emissao == null ? null :  emissao[0].format("dd/MM/yyyy") )
		adicionarParametro("dataFim", emissao == null ? null : emissao[1].format("dd/MM/yyyy"))
		
			
		def docs = buscarDocumentos(tipos, numeroInicial, numeroFinal, entidades, emissao)
		List<TableMap> newDocs = new ArrayList<>();
		Map<String, TableMap> codigoMap = new HashMap<>(); // Mapa para armazenar os documentos por código
		
		return gerarPDF("pedidocompra",docs);
	}
	
	private List<TableMap> buscarDocumentos(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, List<Long> entidades, LocalDate[] emissao) {
		
		def whereTipos = tipos != null && tipos.size() > 0 ? " and abb01tipo in (:tipos) " : ""
		def whereEntidades = entidades != null && entidades.size() > 0 ? " and abb01ent in (:entidades) " : ""
		def whereEmissao = emissao != null && emissao.size() > 0 ? " and abb01data between :dataIni and :dataFim " : "" 
		def whereNumIni = " and abb01num >= :numeroInicial "
		def whereNumFim = " and abb01num <= :numeroFinal "


		
		def sql = " select distinct eaa01id, aah01codigo, abb01num, abb01data, abe01codigo, eaa0103seq, abe01na, abm01codigo, abm01na, abm01descr,eaa01totitens,eaa01totdoc,eaa0102frete, abe30nome, aac10dddfone, aac10fone,eaa0103codigo, "+
				"eaa0103dtentrega, eaa0103qtcoml, eaa0103total, eaa0103totdoc, eaa01scvatend, eaa0103unit, comercial.aam06codigo as aam06descr_coml, abg01codigo, cast(eaa0103.eaa0103json ->> 'bc_icms' as numeric(18,6)) as bcIcms,cast(eaa0103.eaa0103json ->> 'icms' as numeric(18,6)) as icms,cast(eaa0103.eaa0103json ->> 'bc_ipi' as numeric(18,6)) as bcIpi,cast(eaa0103.eaa0103json ->> 'ipi' as numeric(18,6)) as ipi,cast(eaa0103.eaa0103json ->> '_icms' as numeric(18,6)) as aliqIcms,cast(eaa0103.eaa0103json ->> '_ipi' as numeric(18,6)) as aliqIpi, "+
				"uso.aam06CODIGO as aam06descr_uso, eaa0102ddd as DDD, eaa01dtentrega, aac10rs, aac10endereco, aac10numero, aac10cep, aag0201nome, aag02uf, aac10ni, aac1002ie, aac10email, abe0101ddd1, abe0101fone1 "+
				"	 from eaa01 "+
				"	 inner join abb01 on abb01id = eaa01central "+
				"	 inner join eaa0103 on eaa0103doc = eaa01id "+
				"	 inner join abm01 on abm01id = eaa0103item "+
				"	 left join aam06 as comercial on comercial.aam06id = eaa0103umcoml "+
				"	 left join aam06 as uso on uso.aam06id = eaa0103umu "+
				"	 left join abe01 on abe01id = abb01ent "+
				"	 inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
				"	 inner join aah01 on abb01tipo = aah01id "+
				"	 inner join eaa0101 on eaa0101doc = eaa01id "+
				"	 inner join eaa0102 on eaa0102doc = eaa01id "+
				"	 left join abe30 on eaa01cp = abe30id "+
				"	 inner join aac10 on eaa01eg = aac10id "+
				"	 left join abg01 on eaa0103ncm = abg01id "+
				"	 inner join aag0201 on aac10municipio = aag0201id "+
				"	 inner join aag02 on aag0201uf = aag02id "+
				"	 inner join aac1002 on aac1002empresa = aac10id "+
				"	 where eaa01cancData is null "+
				"	 and eaa01clasDoc = 0	"+
				"     and eaa01esmov = 0 "+
				//"	 and aah01codigo = '30' "+
					obterWherePadrao("eaa01","and") + whereTipos + whereEntidades + whereEmissao + whereNumIni + whereNumFim +
					" order by abb01num, eaa0103seq"

//					interromper(sql)
		
		def p1 = tipos != null && tipos.size() > 0 ? criarParametroSql("tipos", tipos) : null
		def p2 = entidades != null && entidades.size() > 0 ? criarParametroSql("entidades", entidades) : null
		def p3 = emissao != null && emissao.size() > 0 ? criarParametroSql("dataIni", emissao[0]) : null
		def p4 = emissao != null && emissao.size() > 0 ? criarParametroSql("dataFim", emissao[1]) : null 
		def p5 = criarParametroSql("numeroInicial",numeroInicial)
		def p6 = criarParametroSql("numeroFinal",numeroFinal)

		
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2,p3,p4,p5,p6)
		
	}
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkbyBkZSBDb21wcmEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=