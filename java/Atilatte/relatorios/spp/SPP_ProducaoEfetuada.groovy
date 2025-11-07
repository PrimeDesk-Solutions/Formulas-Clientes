package Atilatte.relatorios.spp;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.aa.Aac10;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate
import br.com.multitec.utils.Utils

public class SPP_ProducaoEfetuada extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SPP - Produção Efetuada"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		 Map<String,Object> filtrosDefault = new HashMap()
	        filtrosDefault.put("impressao","0");
	        return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		List<Long> idTipodoc = getListLong("tipoDoc");
		LocalDate[] dtEmissao = getIntervaloDatas("data");
		List<Long> prodAcabado = getListLong("prodAcabado");
		Integer impressao = getInteger("impressao");

		List<TableMap> dados = buscarDadosRelatorio(idTipodoc,dtEmissao,prodAcabado);

		params.put("titulo", "SPP - Produção Efetuada");
		params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na())
		
		if(impressao == 1) return gerarXLSX("SPP_ProducaoEfetuadaExcel",dados);

		return gerarPDF("SPP_ProducaoEfetuada", dados)
	}

	private List<TableMap> buscarDadosRelatorio(List<Long>idTipodoc,LocalDate[] dtEmissao,List<Long> prodAcabado){

		//Datas de Emissão Inicial e Final
		LocalDate dataEmissIni = null;
		LocalDate dataEmissFin = null;
		
		if(dtEmissao != null){
			dataEmissIni = dtEmissao[0];
			dataEmissFin = dtEmissao[1];
		}
		
		String whereData = dataEmissIni != null && dataEmissFin != null ? "and abb01data between :dataEmissIni and :dataEmissFin " : "";
		String whereProd = prodAcabado != null && prodAcabado.size() > 0 ? "and abm01id in (:prodAcabado) " : "";

		
		Parametro parametroDataIni = dataEmissIni != null && dataEmissFin ? Parametro.criar("dataEmissIni",dataEmissIni) : null;
		Parametro parametroDataFin = dataEmissIni != null && dataEmissFin ? Parametro.criar("dataEmissFin",dataEmissFin) : null;
		Parametro parametroProd = prodAcabado != null && prodAcabado.size() > 0 ? Parametro.criar("prodAcabado",prodAcabado) : null;

		String sql = "select itemPlano.abm01tipo as tipoItem,itemPlano.abm01codigo as codigoItem, itemPlano.abm01na as naItem,aam06codigo as unidade, sum(bab01qtp) as qtd, abm0101pmu as unitMedio,  "+
					"abm0101pmu * sum(bab01qtp) as total " +
					"from bab01  "+
					"inner join abb01 as abb01ordem on abb01ordem.abb01id = bab01central "+
					"inner join abp20 on abp20id = bab01comp  "+
					"inner join abm01 as itemPlano on itemPlano.abm01id = abp20item  "+
					"inner join abm0101 on abm0101item = abm01id "+
					"inner join aam06 on aam06id = abm01umu "+
					"where true "+
					whereData +
					whereProd +
					"group by itemPlano.abm01tipo,itemPlano.abm01codigo,itemPlano.abm01na,aam06codigo,abm0101pmu "+
					"order by itemPlano.abm01codigo "
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroTipo,parametroDataIni,parametroDataFin,parametroProd)
		
	}
}
//meta-sis-eyJkZXNjciI6IlNQUCAtIFByb2R1w6fDo28gRWZldHVhZGEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=