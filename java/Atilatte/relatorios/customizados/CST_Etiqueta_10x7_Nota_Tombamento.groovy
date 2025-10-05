package Atilatte.relatorios.customizados;

import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.ValidacaoException
import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

public class CST_Etiqueta_10x7_Nota_Tombamento extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CST - Etiqueta 10x7 Nota Fiscal (Tombamento)";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("numeroInicial",1);
		filtrosDefault.put("numeroFinal",99999999);
		return Utils.map("filtros", filtrosDefault);

	}
	@Override
	public DadosParaDownload executar() {
		Integer numInicio = getInteger("numeroInicial");
		Integer numFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		List<Long> idEntidade = getListLong("entidades");
		List<Long> idDespacho = getListLong("despacho");
		List<Long> idRedesp = getListLong("redespachos");
		List<Long> itens = getListLong("itens");
		List<Integer> mps = getListLong("mps");
		List<Long> categoria = getListLong("categoria");
		Boolean imprimirDescrItem = getBoolean("imprimirDescrItem");


		params.put("CNPJ", getVariaveis().getAac10().getAac10ni());
		params.put("enderecoEmpresa", getVariaveis().getAac10().getAac10endereco());
		params.put("bairroEmpresa","Bairro " +getVariaveis().getAac10().getAac10bairro()+" - Itatiba/SP");
		params.put("nomeEmpresa", getVariaveis().getAac10().getAac10na());
		params.put("imprimirDescrItem", imprimirDescrItem);


		List<TableMap> dados = buscarDocumentos(numInicio, numFinal,dataEmissao,idEntidade,idDespacho,idRedesp, itens, mps, categoria);

		//return gerarPDF("CST_Etiqueta10x7_Sam4(Nota)", dados);
		return gerarPDF("CST_Etiqueta10x7_Sam4(Faturamento)", dsPrincipal);
	}
	private List<TableMap> buscarDocumentos(Integer numInicio, Integer numFinal,LocalDate[] dataEmissao, List<Long> idEntidade,List<Long> idDespacho, List<Long> idRedesp, List<Long>itens, List<Integer> mps, List<Long> categoria){

		String whereData = dataEmissao != null  ? "and abb01data between :dataInicial and :dataFinal  " : ""
		String whereNumDoc = numInicio != null && numFinal != null ? "where abb01num BETWEEN :numInicio AND :numFinal " : numInicio != null && numFinal == null ? "WHERE abb01num >= :numInicio " : numInicio == null && numFinal != null ? "WHERE abb01num <= :numFinal " : ""
		String whereRedespacho = idRedesp != null && idRedesp.size() > 0 != null ? "and (redesp.abe01id in (:idRedesp) or eaa0102redespacho is null) " : ""
		String whereEntidade = idEntidade != null && idEntidade.size() > 0 ? "and cliente.abe01id in (:idEntidade) " : "";
		String whereDespacho = idDespacho != null && idDespacho.size() > 0 ? "and desp.abe01id in (:idDespacho) " : "";
		String whereItens = itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "";
		String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
		String whereCategoria = categoria != null && categoria.size() > 0 ? "and abm0102criterio in (:categoria) " : ""
		
		Parametro parametroDataIni = dataEmissao != null ? Parametro.criar("dataInicial",dataEmissao[0]) : null;
		Parametro parametroDataFin = dataEmissao != null ? Parametro.criar("dataFinal",dataEmissao[1]) : null;
		Parametro parametroNumDocIni = numInicio != null ? Parametro.criar("numInicio",numInicio) : null;
		Parametro parametroDocFin = numFinal != null ? Parametro.criar("numFinal",numFinal) : null;
		Parametro parametroRedesp = idRedesp != null && idRedesp.size() > 0 ? Parametro.criar("idRedesp",idRedesp) : null;
		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade",idEntidade) : null;
		Parametro parametroDespacho = idDespacho != null && idDespacho.size() > 0 ? Parametro.criar("idTransp",idDespacho) : null;
		Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens",itens) : null;
		Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps",mps) : null;
		Parametro parametroCategoria = categoria != null && categoria.size() > 0 ? Parametro.criar("categoria",categoria) : null;

		String sql = "select distinct eaa0103id, abb01num,abm01na,abm01codigo,eaa0103qtComl,cast(eaa0103json ->> 'qt_convertida' as numeric(18,6)) as qtConvertida,cast(eaa0103json ->> 'frasco' as numeric(18,6)) as frasco,cast(eaa0103json ->> 'caixa' as numeric(18,6)) as caixa, "+
					"cast(abm0101json ->> 'volume_caixa' as numeric(18,6)) as volumeCaixa,cast(abm0101json ->> 'cvdnf' as numeric(18,6)) as cvdnf, eaa01operdescr, cliente.abe01nome as entidade, eaa0101endereco as endereco, "+
					"eaa0101numero as numero,eaa0101bairro as bairro, aag0201nome as municipio, aag02uf as uf, eaa0101cep as cep, desp.abe01codigo as codigoDespacho, desp.abe01na despacho, "+
					"redesp.abe01codigo as codigoRedespacho, redesp.abe01na as redespacho, cast(eaa0103json ->>'umv' as character varying(2)) as UMV,CAST(abm0101json ->>'ordem_separacao' AS numeric(18,2)) AS ordemSeparacao "+
					"from eaa01  "+
					"inner join abb01 on abb01id = eaa01central  "+
					"inner join eaa0101 on eaa0101doc = eaa01id and eaa0101principal = 1 "+
					"inner join aag0201 on aag0201id = eaa0101municipio  "+
					"inner join aag02 on aag02id = aag0201uf  "+
					"inner join eaa0102 on eaa0102doc = eaa01id  "+
					"inner join abe01 as desp on desp.abe01id = eaa0102despacho  "+
					"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho  "+
					"inner join eaa0103 on eaa0103doc = eaa01id  "+
					"inner join abe01 as cliente on cliente.abe01id = abb01ent  "+
					"inner join abe02 on abe02ent = cliente.abe01id  "+
					"inner join abm01 on abm01id = eaa0103item  "+
					"inner join abm0101 on abm0101item = abm01id  "+
					"inner join aam06 on aam06id = abm01umu  "+
					"left join abm0102 on abm0102item = abm01id "+
					whereNumDoc +
					whereData+
					whereRedespacho +
					whereEntidade +
					whereDespacho +
					whereItens+
					whereMps+
					whereCategoria+
					"and eaa01clasdoc = 1 "+
					"and eaa01esmov = 1 "+
					"and eaa01cancdata is null "+
					"order by ordemSeparacao desc, abb01num asc ";

		List<TableMap> registros = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDataIni, parametroDataFin, parametroNumDocIni, parametroDocFin, parametroRedesp, parametroEntidade, parametroDespacho, parametroItens, parametroMps, parametroCategoria);

		List<TableMap> dadosRelatorio = buscarCaixasItens(registros);

		return dadosRelatorio;
	}
	private List<TableMap> buscarCaixasItens(List<TableMap> registros){
		List<TableMap> itensEtiqueta = new ArrayList();

		for(int i = 0; i < registros.size();i++){

			String umv = registros.get(i).getString("UMV");
			Integer qtdCaixa = umv == 'UN' || umv == 'FR' ? registros.get(i).getInteger("eaa0103qtComl") / registros.get(i).getInteger("volumeCaixa") : registros.get(i).getInteger("caixa");
			Integer capacidade = registros.get(i).getInteger("cvdnf")

			for(int cx = 0; cx < qtdCaixa; cx++){
				TableMap itens = new TableMap()
				String codbarsam = registros.get(i).getInteger("abb01num");

				itens.put("item",(registros.get(i).getString("abm01na") + "                         ").substring(0,40) +capacidade);
				itens.put("nf",registros.get(i).getInteger("abb01num"));
				itens.put("tipo","CAIXA");
				itens.put("operacao", registros.get(i).getString("eaa01operdescr"));
				itens.put("entidade", registros.get(i).getString("entidade"));
				itens.put("endereco", registros.get(i).getString("endereco"));
				itens.put("numero", registros.get(i).getString("numero"));
				itens.put("bairro", registros.get(i).getString("bairro"));
				itens.put("municipio", registros.get(i).getString("municipio"));
				itens.put("uf",registros.get(i).getString("uf"));
				itens.put("cep",registros.get(i).getString("cep"));
				itens.put("despacho",registros.get(i).getString("despacho"));
				itens.put("codigodespacho",registros.get(i).getString("codigoDespacho"));
				itens.put("ordemSeparacao",registros.get(i).getString("ordemSeparacao"));
				itens.put("redespacho",registros.get(i).getString("redespacho"));
				itens.put("qtd", registros.get(i).getInteger("frasco"));
				itens.put("codbarsam", codbarsam);

				itensEtiqueta.add(itens)

			}
		}
		return itensEtiqueta;
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgRmF0dXJhbWVudG8gKFRvbWJhbWVudG8pIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgRmF0dXJhbWVudG8gKFRvbWJhbWVudG8pIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgTm90YSBGaXNjYWwgKFRvbWJhbWVudG8pIiwidGlwbyI6InJlbGF0b3JpbyJ9