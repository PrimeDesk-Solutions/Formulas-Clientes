package Atilatte.relatorios.spp;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import br.com.multitec.utils.Utils
import br.com.multitec.utils.DateUtils

import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.TableMapDataSource

public class SPP_ComposicaoDetalhada extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SPP - Composição Detalhada"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		List<Long> idItens = getListLong("itens");
		List<TableMap> dados = buscarItensComposicao(idItens);

		params.put("empresa",obterEmpresaAtiva().aac10codigo+"-"+obterEmpresaAtiva().aac10na)

		return gerarPDF("SPP_ComposicaoDetalhada", dados);
	}

	private buscarItensComposicao(List<Long> idItens){
		String sql = "select abm01composicao.abm01codigo as codItemComposicao, abm01composicao.abm01na as naItemCompos, "+
					"aam06Composicao.aam06codigo as umuComposicao,abm01componente.abm01codigo as codItemComponente, abm01componente.abm01na as naItemComponente, "+
					"aam06componente.aam06codigo as umuComponente,case when abm01componente.abm01tipo = 1 then 'P' end as mps, abp20011seq as sequencia, "+
					"abp20011qt as quantidade, abp20011perda as perda "+
					"from abp20 "+
					"inner join abm01 as abm01composicao on abm01composicao.abm01id = abp20item "+
					"inner join aam06 as aam06Composicao on aam06Composicao.aam06id = abm01composicao.abm01umu "+
					"inner join abp2001 on abp2001comp = abp20id "+
					"inner join abp20011 on abp20011proc = abp2001id "+
					"inner join abm01 as abm01componente on abm01componente.abm01id = abp20011item "+
					"inner join aam06 as aam06componente on aam06componente.aam06id = abm01componente.abm01umu "+
					"where abm01componente.abm01tipo = 1 "+
					(idItens != null && idItens.size() > 0 ? "and abm01composicao.abm01id in (:idItens) " : "")
					
					
		Parametro p1 = idItens != null && idItens.size() > 0 ? Parametro.criar("idItens",idItens) : null;

		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1);
			
	}
}
//meta-sis-eyJkZXNjciI6IlNQUCAtIENvbXBvc2nDp8OjbyBEZXRhbGhhZGEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=