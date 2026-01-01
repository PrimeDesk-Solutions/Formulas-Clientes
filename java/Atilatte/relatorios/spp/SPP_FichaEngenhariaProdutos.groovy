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

public class SPP_FichaEngenhariaProdutos extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SPP - Ficha De Engenharia de Produtos";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("quantidade","1");
		filtrosDefault.put("precoItem","0");
		filtrosDefault.put("impressao","0");
		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		List<Long> idsItens = getListLong("itens");
		Integer quantidade = getBigDecimal("quantidade");
		Integer precoItem = getInteger("precoItem");
		boolean chkPrecoItem = getBoolean("chkPrecoItem");
		Integer impressao = getInteger("impressao");
		List<TableMap> dados = new ArrayList();
		List<TableMap> tmComponentes = new ArrayList();
		List<TableMap> tmAtividades = new ArrayList();
		List<TableMap> tmRecurso = new ArrayList();
		String wherePrecoItem = ""

		List<TableMap> tmComposicoes = buscarComposicoes(idsItens);

		if(chkPrecoItem) wherePrecoItem = buscarWherePrecoItem(precoItem);

		params.put("empresa",obterEmpresaAtiva().aac10codigo+"-"+obterEmpresaAtiva().aac10na)

		for(tmComposicao in tmComposicoes ){
			Long idComposicao = tmComposicao.getLong("abp20id");
			tmComposicao.put("key",idComposicao);
			tmComposicao.put("quantidade",quantidade)

			List<TableMap> componentes = buscarComponentesComposicao(idComposicao,wherePrecoItem);
			List<TableMap> atividades = buscarAtividades(idComposicao);
			List<TableMap> recursos = buscarRecursos(idComposicao);



			if(componentes != null && componentes.size() > 0){
				for(componente in componentes){
					String codProcesso = componente.getString("abp10codigo");
					def total = obterTotalGeralPorProcesso(idComposicao,codProcesso);
					componente.put("key",idComposicao);
					componente.put("qtdFinal",componente.getBigDecimal_Zero("qtd") * quantidade );
					componente.put("totalItem",componente.getBigDecimal_Zero("valorItem") * componente.getBigDecimal_Zero("qtdFinal") )
					componente.put("totalProcesso",total)
					componente.put("percentual",(componente.getBigDecimal_Zero("qtdFinal") /  quantidade) * 100  )
					tmComponentes.add(componente);
				}
			}

			if(atividades != null && atividades.size() > 0){
				for(atividade in atividades){
					atividade.put("key",idComposicao);
					tmAtividades.add(atividade)
				}
			}

			if(recursos != null && recursos.size() > 0){
				for(recurso in recursos){
					recurso.put("key",idComposicao);
					tmRecurso.add(recurso)
				}
			}

		}

		def sub1 = chkPrecoItem ? carregarArquivoRelatorio("SPP_FichaEngenhariaProdutos_s1_Unitario") : carregarArquivoRelatorio("SPP_FichaEngenhariaProdutos_s1")

		TableMapDataSource dsPrincipal = new TableMapDataSource(tmComposicoes);
		dsPrincipal.addSubDataSource("dsComponentes", tmComponentes, "key", "key");
		dsPrincipal.addSubDataSource("dsAtividades", tmAtividades, "key", "key");
		dsPrincipal.addSubDataSource("dsRecursos", tmRecurso, "key", "key");
		adicionarParametro("SUBREPORT_DIR", sub1)
		adicionarParametro("SUBREPORT_DIR2", carregarArquivoRelatorio("SPP_FichaEngenhariaProdutos_s2"))
		adicionarParametro("SUBREPORT_DIR3", carregarArquivoRelatorio("SPP_FichaEngenhariaProdutos_s3"))

		if(impressao == 1) {
			List<TableMap> tmRegistros = buscarDadosGerais(idsItens, wherePrecoItem, quantidade)
			return gerarXLSX("SPP_FichaEngenhariaProdutos_Excel",tmRegistros )
		}
		return gerarPDF("SPP_FichaEngenhariaProdutos",dsPrincipal)
	}

	private List<TableMap> buscarComposicoes(List<Long>idsItens){
		String whereItens = idsItens != null && idsItens.size() > 0 ? "where abp20item in (:idsItens) " : "";
		Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;

		String sql = "select abp20id,abm01id,abm01codigo as codProd, abm01na as descrProd,umu.aam06codigo as umu, umv.aam06codigo as umv, abp20id,abp20bomcodigo as formula,abp20bomdtcria as dtFormula, "+
				"abp20bomnumrev as numRevisao, abp20bomdtrev as dtRevisao "+
				"from abp20 "+
				"inner join abm01 on abm01id = abp20item "+
				"inner join aam06 as umu on umu.aam06id = abm01umu " +
				"inner join abm0101 on abm0101item = abm01id "+
				"inner join abm13 on abm13id = abm0101comercial "+
				"inner join aam06 as umv on umv.aam06id = abm13umv "+
				whereItens +
				"order by abm01codigo, abp20bomcodigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens)
	}

	private buscarComponentesComposicao(Long idComposicao,String wherePrecoItem){
		String sql = "select abm01codigo, abm01na,abp10codigo, abp10descr,aam06codigo,abp20011perda as perda,abp20011qt as qtd "+
				wherePrecoItem+
				"from abp2001  "+
				"inner join abp20011 on abp20011proc = abp2001id  "+
				"inner join abm01 on abm01id = abp20011item  "+
				"inner join abm0101 as confComponente on confComponente.abm0101item = abm01id "+
				"inner join abp10 on abp10id = abp2001proc  "+
				"inner join aam06 on aam06id = abm01umu "+
				"where abp2001comp = :idComposicao ";
		Parametro p1 = Parametro.criar("idComposicao",idComposicao);

		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1);
	}

	private buscarAtividades(Long idComposicao ){
		String sql = "select abp1001seq,abp01codigo,abp01descr,abp10codigo,abp10descr,abp1001minativ as tempo,abp1001minsetup as tempoSetup "+
					"from abp2001 "+
					"inner join abp10 on abp10id = abp2001proc "+
					"inner join abp1001 on abp1001proc = abp10id "+
					"inner join abp01 on abp01id = abp1001ativ "+
					"where abp2001comp = :idComposicao "+
					"order by abp10codigo,abp1001seq "

		Parametro p1 = Parametro.criar("idComposicao",idComposicao);
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1);
	}

	private buscarRecursos(Long idComposicao){
		String sql = "select distinct abp2001seq,abp10codigo,abp10descr,abb20codigo, abb20nome "+
					"from abp2001 "+
					"inner join abp10 on abp10id = abp2001proc "+
					"inner join abp1001 on abp1001proc = abp10id "+
					"inner join abp01 on abp01id = abp1001ativ "+
					"inner join abb20 on abb20id = abp01bem "+
					"where abp2001comp = :idComposicao ";

		Parametro p1 = Parametro.criar("idComposicao",idComposicao);

		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1);
	}

	private List<TableMap> buscarDadosGerais(List<TableMap> idsItens, String wherePrecoItem, Integer quantidade){
		String whereItens = idsItens != null && idsItens.size() > 0 ? "where abp20item in (:idsItens) " : "";
		Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;

		String sql = "select abp20id,itemComp.abm01id,itemComp.abm01codigo as codProdComp, itemComp.abm01na as descrProdComp, "+
					"umuComp.aam06codigo as umuComp, umv.aam06codigo as umvComp, abp20id,abp20bomcodigo as formula, "+
					"abp20bomdtcria as dtFormula, abp20bomnumrev as numRevisao, abp20bomdtrev as dtRevisao, "+
					"itemComponente.abm01codigo as codItemComponente, itemComponente.abm01na as naItemComponente,abp10codigo as codProcesso, abp10descr as descrProcesso,umuComponente.aam06codigo as umuComponente,abp20011perda as perda,abp20011qt as qtd, "+
					"case when itemComp.abm01tipo = 0 then 'M' when itemComp.abm01tipo = 1 then 'P' else 'S' end as mpsComp, "+
					"case when itemComponente.abm01tipo = 0 then 'M' when itemComponente.abm01tipo = 1 then 'P' else 'S' end as mpsComponente, abp20011seq as seq, "+
					"abp1001seq,abp01codigo as codAtiv,abp01descr as descrAtiv,abp10codigo as procProdutivo,abp10descr as descrProcProdutivo,abp1001minativ as tempo,abp1001minsetup as tempoSetup "+ wherePrecoItem +
					"from abp20 "+
					"inner join abm01 as itemComp on itemComp.abm01id = abp20item "+
					"inner join aam06 as umuComp on umuComp.aam06id = itemComp.abm01umu "+
					"inner join abm0101 as confCompo on abm0101item = itemComp.abm01id "+
					"inner join abm13 on abm13id = abm0101comercial "+
					"inner join aam06 as umv on umv.aam06id = abm13umv "+
					"inner join abp2001 on abp2001comp = abp20id "+
					"inner join abp20011 on abp20011proc = abp2001id "+
					"inner join abm01 as itemComponente on itemComponente.abm01id = abp20011item "+
					"inner join abm0101 as confComponente on confComponente.abm0101item = itemComponente.abm01id "+
					"inner join abp10 on abp10id = abp2001proc "+
					"inner join aam06 as umuComponente on umuComponente.aam06id = itemComponente.abm01umu "+
					"inner join abp1001 on abp1001proc = abp10id "+
					"left join abp01 on abp01id = abp1001ativ "+
					"left join abb20 on abb20id = abp01bem "+
					whereItens +
					"order by  itemComp.abm01codigo,abp20bomcodigo, abp20011seq,abp10codigo,abp1001seq ";

		List<TableMap> dadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens)
		List<TableMap> registros = new ArrayList();
		for(dados in dadosRelatorio) {
			String codProcesso = dados.getString("abp10codigo");
			Long idComposicao = dados.getLong("abp10id")
			def total = obterTotalGeralPorProcesso(idComposicao,codProcesso);
			dados.put("quantidade", quantidade)
			dados.put("qtdFinal",dados.getBigDecimal_Zero("qtd") * quantidade );
			dados.put("totalItem",dados.getBigDecimal_Zero("valorItem") * dados.getBigDecimal_Zero("qtdFinal") )
			dados.put("totalProcesso",total)
			dados.put("percentual",(dados.getBigDecimal_Zero("qtdFinal") /  quantidade) * 100  )
			registros.add(dados);
		}

		return registros;
	}
	private obterTotalGeralPorProcesso(Long idComposicao, String codProcesso){
		String sql = "select sum(abp20011qt) as qtdTotal "+
					"from abp2001   "+
					"inner join abp20011 on abp20011proc = abp2001id   "+
					"inner join abm01 on abm01id = abp20011item   "+
					"inner join abp10 on abp10id = abp2001proc   "+
					"inner join aam06 on aam06id = abm01umu  "+
					"where abp2001comp = :idComposicao "+
					"and abp10codigo = :codProcesso "+
					"group by abp10codigo, abp10descr ";

		Parametro p1 = Parametro.criar("idComposicao",idComposicao);
		Parametro p2 = Parametro.criar("codProcesso",codProcesso);

		return getAcessoAoBanco().obterBigDecimal(sql, p1,p2)

	}
	private String buscarWherePrecoItem(precoItem){

		switch(precoItem){
			case 0:
				return ", cast(confComponente.abm0101json ->> 'custo' as numeric(18,2)) as valorItem ";
				break;
			case 1:
				return ", confComponente.abm0101pmu as valorItem ";
				break;
			case 2:
				return ", cast(confComponente.abm0101json ->> 'maior_preco_unit' as numeric(18,2)) as valorItem ";
				break;
			case 3:
				return ", cast(confComponente.abm0101json ->> 'menor_preco_unit' as numeric(18,2)) as valorItem ";
				break;
			case 4:
				return ", cast(confComponente.abm0101json ->> 'ultimo_preco' as numeric(18,2)) as valorItem ";
				break;
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNQUCAtIEZpY2hhIERlIEVuZ2VuaGFyaWEgZGUgUHJvZHV0b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNQUCAtIEZpY2hhIERlIEVuZ2VuaGFyaWEgZGUgUHJvZHV0b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=