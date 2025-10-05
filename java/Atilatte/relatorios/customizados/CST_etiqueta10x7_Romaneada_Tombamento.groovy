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

public class CST_etiqueta10x7_Romaneada_Tombamento extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CST - Etiqueta 10x7 Com Romaneio (Tombamento)";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();

		String sql = "select distinct bfb01id from bfa01 "+
				"inner join bfa0101 on bfa0101rom = bfa01id  "+
				"inner join bfa01011 on bfa01011item = bfa0101id  "+
				"inner join abb01 on abb01id = bfa01docscv "+
				"inner join bfb0101 on bfb0101central = abb01id "+
				"inner join bfb01 on bfb01id = bfb0101lote "

		List<Long> listIds =  getAcessoAoBanco().obterListaDeLong(sql);

		filtrosDefault.put("listIds",listIds);

		return filtrosDefault;

	}
	@Override
	public DadosParaDownload executar() {


		Integer numInicio = getInteger("numeroInicial");
		Integer numFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Long idEntIni = getLong("entIni");
		Long idEntFin = getLong("entFin");
		List<Long> idTransp = getListLong("transportadora");
		List<Long> idRedesp = getListLong("redespacho");
		Long capaLoteIni = getLong("capaLoteIni");
		Long capaLoteFin = getLong("capaLoteFin");
		List<Long> itens = getListLong("itens");
		List<Integer> mps = getListLong("mps");
		List<Long> categoria = getListLong("categoria");
		Boolean imprimirDescrItem = getBoolean("imprimirDescrItem");

		LocalDate dataInicial = null;
		LocalDate dataFinal = null;
		if(dataEmissao != null){
			dataInicial = dataEmissao[0];
			dataFinal = dataEmissao[1];
		}

		params.put("CNPJ", getVariaveis().getAac10().getAac10ni());
		params.put("enderecoEmpresa", getVariaveis().getAac10().getAac10endereco());
		params.put("bairroEmpresa","Bairro " +getVariaveis().getAac10().getAac10bairro()+" - Itatiba/SP");
		params.put("nomeEmpresa", getVariaveis().getAac10().getAac10na());
		params.put("imprimirDescrItem", imprimirDescrItem);

		List<TableMap> dados = buscarDocumentos(numInicio, numFinal,dataEmissao,idEntIni,idEntFin,idTransp,idRedesp,capaLoteIni,capaLoteFin,itens, mps, categoria);

		return gerarPDF("CST_Etiqueta10x7_Sam4", dados);
	}
	private List<TableMap> buscarDocumentos(Integer numInicio, Integer numFinal, LocalDate[] dataEmissao, Long idEntIni, Long idEntFin,List<Long> idTransp, List<Long> idRedesp, Long capaLoteIni, Long capaLoteFin, List<Long>itens, List<Integer> mps, List<Long> categoria){

		String whereData = dataEmissao != null ? "and abb01data between :dataInicial  and :dataFinal  " : ""
		String whereNumDoc = numInicio != null && numFinal != null ? "and abb01num BETWEEN :numInicio AND :numFinal " : numInicio != null && numFinal == null ? "and abb01num >= :numInicio " : numInicio == null && numFinal != null ? "and abb01num <= :numFinal " : ""
		String whereRedespacho = idRedesp != null && idRedesp.size() > 0 != null ? "and (redesp.abe01id in (:idRedesp) or eaa0102redespacho is null) " : ""
		String whereEntidade = idEntIni != null && idEntFin != null ? "and cliente.abe01id between :idEntIni and :idEntFin " : idEntIni != null && idEntFin == null ? "cliente.abe01id >= :idEntIni " : idEntIni == null && idEntFin != null ? "cliente.abe01id <= :idEntFin ": "";
		String whereDespacho = idTransp != null && idTransp.size() > 0 ? "and transp.abe01id in (:idTransp) " : ""
		String whereCapaLote = capaLoteIni != null && capaLoteFin != null ? "and bfb01id between :capaLoteIni and :capaLoteFin " : "";
		String whereItens = itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "";
		String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
		String whereCategoria = categoria != null && categoria.size() > 0 ? "and abm0102criterio in (:categoria) " : ""

		Parametro parametroDataIni = dataEmissao != null ? Parametro.criar("dataInicial",dataEmissao[0]) : null;
		Parametro parametroDataFin = dataEmissao != null ? Parametro.criar("dataFinal",dataEmissao[1]) : null;
		Parametro parametroNumDocIni = numInicio != null ? Parametro.criar("numInicio",numInicio) : null;
		Parametro parametroDocFin = numFinal != null ? Parametro.criar("numFinal",numFinal) : null;
		Parametro parametroRedesp = idRedesp != null && idRedesp.size() > 0 ? Parametro.criar("idRedesp",idRedesp) : null;
		Parametro parametroEntidadeIni = idEntIni != null ? Parametro.criar("idEntIni",idEntIni) : null;
		Parametro parametroEntidadeFin = idEntFin != null ? Parametro.criar("idEntFin",idEntFin) : null;
		Parametro parametroDespacho = idTransp != null && idTransp.size() > 0 ? Parametro.criar("idTransp",idTransp) : null;
		Parametro parametroCapaLoteIni = capaLoteIni != null ? Parametro.criar("capaLoteIni",capaLoteIni) : null;
		Parametro parametroCapaLoteFin = capaLoteFin != null ? Parametro.criar("capaLoteFin",capaLoteFin) : null;
		Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens",itens) : null;
		Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps",mps) : null;
		Parametro parametroCategoria = categoria != null && categoria.size() > 0 ? Parametro.criar("categoria",categoria) : null;

		String sql = "select distinct eaa0103id, abb01num,abm01na,abm01codigo,eaa0103qtComl,cast(eaa0103json ->> 'qt_convertida' as numeric(18,6)) as qtConvertida,cast(eaa0103json ->> 'frasco' as numeric(18,6)) as frasco,cast(eaa0103json ->> 'caixa' as numeric(18,6)) as caixa, "+
						"cast(abm0101json ->> 'volume_caixa' as numeric(18,6)) as volumeCaixa, eaa01operdescr, cliente.abe01nome as entidade, eaa0101endereco as endereco, "+
						"eaa0101numero as numero,eaa0101bairro as bairro, aag0201nome as municipio, aag02uf as uf, eaa0101cep as cep, desp.abe01codigo as codigoDespacho, desp.abe01na despacho, "+
						"redesp.abe01codigo as codigoRedespacho, redesp.abe01na as redespacho, cast(eaa0103json ->>'umv' as character varying(2)) as UMV,CAST(abm0101json ->>'ordem_separacao' AS numeric(18,2)) AS ordemSeparacao,cast(abm0101json ->> 'cvdnf' as numeric(18,6)) as cvdnf "+
						"from bfa01  "+
						"inner join bfa0101 on bfa0101rom = bfa01id "+
						"inner join bfa01011 on bfa01011item = bfa0101id "+
						"inner join abb01 on abb01id = bfa01docscv  "+
						"inner join bfb0101 on bfb0101central = abb01id "+
						"inner join bfb01 on bfb01id = bfb0101lote "+
						"inner join eaa01 on eaa01central = abb01id  "+
						"inner join eaa0101 on eaa0101doc = eaa01id and eaa0101principal = 1 " +
						"inner join eaa0102 on eaa0102doc = eaa01id  "+
						"inner join aag0201 on aag0201id = eaa0101municipio  "+
						"inner join aag02 on aag02id = aag0201uf  "+
						"inner join abe01 as desp on desp.abe01id = eaa0102despacho "+
						"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
						"inner join eaa0103 on eaa0103id = bfa0101item "+
						"inner join abe01 as cliente on cliente.abe01id = abb01ent "+
						"inner join abe02 on abe02ent = cliente.abe01id  "+
						"inner join abm01 on abm01id = eaa0103item "+
						"inner join abm0101 on abm0101item = abm01id "+
						"left join abm0102 on abm0102item = abm01id "+
						"where true "+
						whereNumDoc +
						whereData+
						whereRedespacho +
						whereEntidade +
						whereCapaLote +
						whereDespacho +
						whereItens+
						whereMps+
						whereCategoria+
						"and abm0101empresa = '1322578'"+
						"and eaa01cancdata is null "+
						"order by ordemSeparacao desc, abb01num  ";

		List<TableMap> registros = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDataIni, parametroDataFin, parametroNumDocIni, parametroDocFin, parametroRedesp, parametroEntidadeIni, parametroEntidadeFin, parametroDespacho, parametroCapaLoteIni, parametroCapaLoteFin, parametroItens, parametroMps, parametroCategoria);

		List<TableMap> dadosRelatorio = processarCaixas(registros);

		return dadosRelatorio;



	}
	private List<TableMap>processarCaixas(List<TableMap> registros){

		List<TableMap> itensEtiqueta = new ArrayList();
		String umv = "";
		Integer cxs = 0;
		Integer uns = 0;

		for(int i = 0; i < registros.size();i++){

			umv = registros.get(i).getString("UMV");
			cxs = umv == 'CX' || umv == 'KG'  ?  registros.get(i).getInteger("caixa") : registros.get(i).getInteger("eaa0103qtComl") / registros.get(i).getInteger("cvdnf");

			if(umv == 'CX' || umv == 'KG'){
				for(int cx = 0; cx < cxs; cx++){
					TableMap itens = new TableMap()
					String codbarsam = registros.get(i).getInteger("abb01num");
					itens.put("numFolha", cx+1);
					itens.put("totalFolha", cxs);
					itens.put("item",(registros.get(i).getString("abm01na") + "                         ").substring(0,40) +registros.get(i).getInteger("cvdnf"));
					itens.put("nf",registros.get(i).getInteger("abb01num"));
					itens.put("tipo","CAIXA");
					//itens.put("etiqueta", dados - 1);
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

			if(umv != 'CX'){

				TableMap itensMisto = new TableMap()

				if(umv == 'FR' || umv == 'UN'){
					for(int cx = 0; cx < cxs; cx++){
						String codbarsam = registros.get(i).getInteger("abb01num");
						itensMisto.put("item",registros.get(i).getString("abm01na") + registros.get(i).getInteger("cvdnf"));
						itensMisto.put("nf",registros.get(i).getInteger("abb01num"));
						itensMisto.put("tipo","CAIXA");
						//itensMisto.put("etiqueta",dados - 1);
						itensMisto.put("operacao", registros.get(i).getString("eaa01operdescr"));
						itensMisto.put("entidade",registros.get(i).getString("entidade"));
						itensMisto.put("endereco",registros.get(i).getString("endereco"));
						itensMisto.put("numero",registros.get(i).getString("numero"));
						itensMisto.put("bairro", registros.get(i).getString("bairro"));
						itensMisto.put("municipio",registros.get(i).getString("municipio"));
						itensMisto.put("uf",registros.get(i).getString("uf"));
						itensMisto.put("cep",registros.get(i).getString("cep"));
						itensMisto.put("despacho",registros.get(i).getString("despacho"));
						itensMisto.put("codigodespacho",registros.get(i).getString("codigoDespacho"));
						itensMisto.put("qtd",registros.get(i).getString("ordemSeparacao"));
						itensMisto.put("redespacho",registros.get(i).getString("redespacho"));
						itensMisto.put("codbarsam",codbarsam);
						itensEtiqueta.add(itensMisto)
					}

				}

			}
		}
		return itensEtiqueta;

	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgQ29tIFJvbWFuZWlvIChUb21iYW1lbnRvKSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgQ29tIFJvbWFuZWlvIChUb21iYW1lbnRvKSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==