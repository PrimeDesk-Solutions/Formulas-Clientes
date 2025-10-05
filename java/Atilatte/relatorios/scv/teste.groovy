package Atilatte.relatorios.scv;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;
import java.time.LocalDate
import br.com.multitec.utils.Utils;


import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;

import sam.model.entities.aa.Aac10;

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class teste extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "Teste"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("impressao", "0");
		filtrosDefault.put("atendimento", true);
		filtrosDefault.put("atendimento2", true);
		filtrosDefault.put("liquido", true)
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		List<Long> tipos = getListLong("tipos");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidades = getListLong("entidades");
		LocalDate[] emissao = getIntervaloDatas("emissao");
		LocalDate[] entrega = getIntervaloDatas("entrega")
		def atendimento = [get("atendimento") ? 0 : null, get("atendimento2") ? 1 : null, get("atendimento3") ? 2 : null]
		Boolean liquido = getBoolean("liquido")

		List<TableMap> dados =  buscarPedidos(tipos,numeroInicial,numeroFinal,entidades,emissao,entrega);

		return gerarPDF("report2",dados)
	}

	private List<TableMap> buscarPedidos(List<Long>tipos,Integer numeroInicial, Integer numeroFinal,List<Long> entidades,LocalDate[] emissao,LocalDate[] entrega){
		
		LocalDate dtEmissIni = null;
		LocalDate dtEmissFin = null;
		
		if(emissao != null){
			dtEmissIni = emissao[0];
			dtEmissFin = emissao[1];
			
		}

		LocalDate dtEntregaIni = null;
		LocalDate dtEntregaFin = null;
		
		if(entrega != null){
			dtEntregaIni = entrega[0];
			dtEntregaFin = entrega[1];
			
		}

		def whereTipos = tipos != null && tipos.size() > 0 ? " and abb01tipo in (:tipos) " : ""
		def whereEntidades = entidades != null && entidades.size() > 0 ? " and abb01ent in (:entidades) " : ""
		//def whereCompVenda = compraVenda == 0 ? " and eaa01esmov = 0 " : " and eaa01esmov = 1 "
		def whereEmissao = emissao != null && emissao.size() > 0 ? " and abb01data between :dataIni and :dataFim " : "" 
		def whereNumIni = " and abb01num >= :numIni "
		def whereNumFim = " and abb01num <= :numFim "
		def whereEntrega = entrega != null && entrega.size() > 0 ? " and eaa0103dtEntrega between :dtIni and :dtFim " : ""
		//ef whereAtendimento  = atendimento != null && atendimento.size() > 0 ? " and eaa01scvAtend in (:atendimento) " : ""

		Parametro parametroNumIni = Parametro.criar("numIni",numeroInicial);
		Parametro parametroNumFim = Parametro.criar("numFim",numeroFinal);
		Parametro parametroTipo =  tipos != null && tipos.size() > 0 ? Parametro.criar("tipos",tipos) : null;
		Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades",entidades) : null;
		Parametro parametroEmissaoIni = emissao != null ? Parametro.criar("dtEmissIni",dtEmissIni) : null;
		Parametro parametroEmissaoFin = emissao != null ? Parametro.criar("dtEmissFin",dtEmissFin) : null;
		Parametro parametroEntregaIni = entrega != null ? Parametro.criar("dtEntregaIni",dtEntregaIni) : null;
		Parametro parametroEntregaFin = emissao != null ? Parametro.criar("dtEntregaFin",dtEntregaFin) : null;
		
		String sql = " select distinct eaa01id, aah01codigo, abb01num, abb01data, abe01codigo, abe01na, abm01codigo, abm01na, abm01descr,eaa0102frete, abe30nome, aac10dddfone, aac10fone,eaa0103codigo, "  +
					" eaa0103dtentrega, eaa0103qtcoml, eaa01032qtComl, eaa0103total, eaa0103totdoc, eaa01scvatend, eaa0103unit, comercial.aam06descr as aam06descr_coml,eaa01totitens,eaa01totdoc, abg01codigo, cast(eaa0103.eaa0103json ->> 'bc_icms' as numeric(18,6)) as bcIcms,cast(eaa0103.eaa0103json ->> 'icms' as numeric(18,6)) as icms,cast(eaa0103.eaa0103json ->> 'bc_ipi' as numeric(18,6)) as bcIpi,cast(eaa0103.eaa0103json ->> 'ipi' as numeric(18,6)) as ipi,cast(eaa0103.eaa0103json ->> '_icms' as numeric(18,6)) as aliqIcms,cast(eaa0103.eaa0103json ->> '_ipi' as numeric(18,6)) as aliqIpi, "+
					" uso.aam06descr as aam06descr_uso, abe0104ddd as DDD, abe0104fone as fone, abe0104nome as nomeContato, abe0104ramal as Ramal, eaa0102ddd, eaa0102email, eaa01dtentrega "+
					 "from eaa01 "+
					 "inner join abb01 on abb01id = eaa01central "+
					 "inner join eaa0103 on eaa0103doc = eaa01id "+
					 "left join eaa01032 on eaa01032itemscv = eaa0103id "+
					 "left join abm01 on abm01id = eaa0103item "+
					 "left join aam06 as comercial on comercial.aam06id = eaa0103umcoml "+
					 "left join aam06 as uso on uso.aam06id = eaa0103umu "+
					 "left join abe01 on abe01id = abb01ent "+
					 "inner join aah01 on abb01tipo = aah01id "+
					 "inner join eaa0101 on eaa0101doc = eaa01id "+
					 "inner join abe0104 on abe0104ent = abe01id "+
					 "inner join eaa0102 on eaa0102doc = eaa01id "+
					 "inner join abe30 on eaa01cp = abe30id "+
					 "inner join aac10 on eaa01eg = aac10id "+
					 "left join abg01 on eaa0103ncm = abg01id "+
					 "where eaa01cancData is null and eaa01clasdoc = 0 "+
					 "and aah01codigo = '30' "+
					//obterWherePadrao("eaa01","and")+
					whereTipos + whereEntidades + whereEmissao + whereNumIni + whereNumFim + whereEntrega  //+ whereAtendimento +
//					" order by abb01num, abb01serie,  eaa0103seq "
		

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroTipo,parametroEntidades,parametroEmissaoIni,parametroNumIni,parametroNumFim,parametroEmissaoFin,parametroEntregaIni,parametroEntregaFin);
	}
}
//meta-sis-eyJkZXNjciI6IlRlc3RlIiwidGlwbyI6InJlbGF0b3JpbyJ9