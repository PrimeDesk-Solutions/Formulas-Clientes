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

public class CST_etiqueta10x7_Faturamento extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Etiqueta 10x7 (Faturamento)"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
//		filtrosDefault.put("numeroInicial",1);
//		filtrosDefault.put("numeroFinal",99999999);
		return filtrosDefault;
				
	}
	@Override 
	public DadosParaDownload executar() {
		
		
		Integer numInicio = getInteger("numeroInicial");
		Integer numFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		List<Long> idEntidade = getListLong("entidades");
		List<Long> idTransp = getListLong("transportadoras");
		List<Long> idRedesp = getListLong("redespachos");
		Boolean imprimirDescrItem = getBoolean("imprimirDescrItem");
		
		Boolean printMist = get("printMist");

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

		List<TableMap> dados = buscarDocumentos(numInicio, numFinal,dataInicial, dataFinal,idEntidade,idTransp,idRedesp);
		
		List<TableMap> caixas = new ArrayList()
		for(int i = 0; i < dados.size(); i++){
			   caixas += processarCaixas(6, dados.get(i).getLong("eaa01id"),dados.size());
//		        caixas += processarCaixas(12, dados.get(i).getLong("eaa01id"),dados.size());
//		        caixas += processarCaixas(12.1, dados.get(i).getLong("eaa01id"),dados.size());
//		        caixas += processarCaixas(16, dados.get(i).getLong("eaa01id"),dados.size());
//		        caixas += processarCaixas(20, dados.get(i).getLong("eaa01id"),dados.size());
//		        caixas += processarCaixas(30, dados.get(i).getLong("eaa01id"),dados.size());
		}


		TableMapDataSource dsPrincipal = new TableMapDataSource(caixas);
		return gerarPDF("CST_Etiqueta10x7_Sam4(Faturamento)", dsPrincipal);
	}
	private List<TableMap> buscarDocumentos(Integer numInicio, Integer numFinal, LocalDate dataInicial, LocalDate dataFinal, List<Long> idEntidade,List<Long> idTransp, List<Long> idRedesp){
		
		String data = dataInicial != null && dataFinal != null  ? "and abb01data between cast(:dataInicial as date) and cast(:dataFinal as date)  " : dataInicial != null && dataFinal == null ? "and abb01data >= :dataInicial " : dataInicial == null && dataFinal != null ? "and abb01data <= :dataFinal " : ""
		String whereNumDoc = numInicio != null && numFinal != null ? "and abb01num BETWEEN :numInicio AND :numFinal " : numInicio != null && numFinal == null ? "WHERE abb01num >= :numInicio " : numInicio == null && numFinal != null ? "WHERE abb01num <= :numFinal " : ""
		String redespacho = idRedesp != null && idRedesp.size() > 0  ? "and redesp.abe01id in (:idRedesp) " : ""
		
		String entidade = idEntidade != null && idEntidade.size() > 0 ? "and cliente.abe01id in (:idEntidade) " : "";
		String despacho = idTransp != null && idTransp.size() > 0 ? "and desp.abe01id in (:idTransp) " : ""
		
		Query query = getSession().createQuery(" select distinct abb01num,eaa01id "+ 
										"from eaa01 "+
										"inner join abb01 on abb01id = eaa01central  "+
										"inner join eaa0102 on eaa0102doc = eaa01id  "+
										"inner join abe01 as desp on desp.abe01id = eaa0102despacho "+
										"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
										"inner join eaa0103 on eaa0103doc = eaa01id "+
										"inner join abe01 as cliente on cliente.abe01id = abb01ent "+
										"inner join abe02 on abe02ent = cliente.abe01id  "+
										"inner join abm01 on abm01id = eaa0103item "+
										"where true "+
										whereNumDoc +
										data+
										entidade +
										despacho +
										redespacho +
										"and eaa01clasdoc = 1 "+
										"and eaa01esmov = 1 "+
										"and eaa01cancdata is null "+
										"order by abb01num");
										

		if(whereNumDoc.length() > 0){
			if(numInicio != null) query.setParameter("numInicio", numInicio);
			if(numFinal != null ) query.setParameter("numFinal", numFinal);
		}


		if(dataInicial != null && dataFinal != null){
			query.setParameter("dataInicial", dataInicial);
			query.setParameter("dataFinal", dataFinal);
		}
		
		if(idEntIni != null && idEntFin != null){
			query.setParameter("idEntIni", idEntIni);
			query.setParameter("idEntFin", idEntFin);
		}
		if(idTransp != null && idTransp.size() > 0){
			query.setParameter("idTransp", idTransp);
		}
		if(idRedesp != null && idRedesp.size() > 0){
			query.setParameter("idRedesp", idRedesp);
		}

		return query.getListTableMap();
	}
	private List<TableMap>processarCaixas(Double tamanhoCaixa, Long idDoc,Integer tamDados ){
		String umv = "";
		Integer cxs = 0;
		Integer uns = 0;
		//Integer qtdCaixa = tamanhoCaixa;
		Integer dados = tamDados;

		List<TableMap> registros = new ArrayList()
		
		Query sql = getSession().createQuery(
			"select distinct eaa0103id, abb01num,abm01na,abm01codigo,eaa0103qtComl,cast(eaa0103json ->> 'qt_convertida' as numeric(18,6)) as qtConvertida,cast(eaa0103json ->> 'frasco' as numeric(18,6)) as frasco,cast(eaa0103json ->> 'caixa' as numeric(18,6)) as caixa, "+
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
			"where eaa01id = :idDoc "+//and CAST(abm0101json ->>'volume_caixa' AS numeric(18,6)) = :tamanhoCaixa "+
			"and abm0101empresa = '1322578'"+
			"order by ordemSeparacao desc ").setParameters("idDoc",idDoc,"tamanhoCaixa",tamanhoCaixa);
			//,abb01num, volumeCaixa
			registros = sql.getListTableMap()
			List<TableMap> itensEtiqueta = new ArrayList()
			Map<String, Integer> qtdAnteriores = new HashMap<String, Integer>();
			Integer qtdCaixa;
			def capacidade;
				
				for(int i = 0; i < registros.size();i++){
					
					umv = registros.get(i).getString("UMV");

					if(umv == 'CX' ){
						qtdCaixa = registros.get(i).getInteger("caixa");
						capacidade = registros.get(i).getInteger("cvdnf")
						for(int cx = 0; cx < qtdCaixa; cx++){
							TableMap itens = new TableMap()
							String codbarsam = registros.get(i).getInteger("abb01num");
							
							itens.put("item",(registros.get(i).getString("abm01na") + "                         ").substring(0,40) +capacidade);
							itens.put("nf",registros.get(i).getInteger("abb01num"));
							itens.put("tipo","CAIXA");
							itens.put("etiqueta", dados - 1);
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
					}else{
						TableMap itensMisto = new TableMap();
						if(umv == 'FR' || umv == 'UN'){
							qtdCaixa = registros.get(i).getInteger("eaa0103qtComl") / registros.get(i).getInteger("volumeCaixa");
							capacidade = registros.get(i).getInteger("cvdnf")
							for(int cx = 0; cx < qtdCaixa; cx++){
								String codbarsam = registros.get(i).getInteger("abb01num");
				                    itensMisto.put("item",(registros.get(i).getString("abm01na") + "                         ").substring(0,40) +capacidade);
				                    itensMisto.put("nf",registros.get(i).getInteger("abb01num"));
				                    itensMisto.put("tipo","CAIXA");
				                    itensMisto.put("etiqueta",dados - 1);
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
						}else{
							qtdCaixa = registros.get(i).getInteger("caixa");
							capacidade = registros.get(i).getInteger("cvdnf");
							for(int cx = 0; cx < qtdCaixa; cx++){
								String codbarsam = registros.get(i).getInteger("abb01num");
				                    itensMisto.put("item",(registros.get(i).getString("abm01na") + "                         ").substring(0,40) +capacidade);
				                    itensMisto.put("nf",registros.get(i).getInteger("abb01num"));
				                    itensMisto.put("tipo","CAIXA");
				                    itensMisto.put("etiqueta",dados - 1);
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

	private Integer getTotalUnidAnteriores(Map<String,Integer>qtdAnteriores){
		int retorno = 0;
		for(String key : qtdAnteriores.keySet()){
			retorno += qtdAnteriores.get(key)
		}
		return retorno;
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgKEZhdHVyYW1lbnRvKSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==