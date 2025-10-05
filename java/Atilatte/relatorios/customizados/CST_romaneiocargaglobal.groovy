package Atilatte.relatorios.customizados;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat

public class CST_romaneiocargaglobal extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Romaneio Carga Global"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap()
		
		filtrosDefault.put("totalCaixas", true);
		filtrosDefault.put("totalFrascos", true)
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataSaida = getIntervaloDatas("dataEntrega"); 
		Boolean totalCaixas = get("totalCaixas");
		Boolean totalFrasco = get("totalFrascos");
		List<Long> despacho = getListLong("despacho");
		String hora = getString("hora");

		//Data de Entrega
		LocalDate dataSaidaIni = null;
		LocalDate dataSaidaFin = null;
		if(dataSaida != null){
			dataSaidaIni = dataSaida[0];
			dataSaidaFin = dataSaida[1];
		}

		//Data Emissao Inicial - Final
		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;
		if(dataEmissao != null){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1];
		}

		List<TableMap> dados = new ArrayList();

		List<TableMap> documentos = buscarDocumentos(dataSaidaIni,dataSaidaFin,dataEmissaoIni,dataEmissaoFin,despacho,hora);
		for(int i = 0; i < documentos.size(); i++){
			
			dados += processarCaixas(1,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(3,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(4,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(5,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(6,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(10,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(12,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(12.1,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(16,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(20,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
			dados += processarCaixas(30,documentos.get(i).getLong("eaa01id"),documentos.size(),totalCaixas,totalFrasco, dataSaidaIni);
		}
//		throw new ValidacaoException(dados.toString())
		gerarPDF("RomaneioCargaGlobal",dados);

	}

	private List<TableMap> buscarDocumentos(LocalDate dataSaidaIni, LocalDate dataSaidaFin, LocalDate dataEmissaoIni, LocalDate dataEmissaoFin, List<Long> despacho,String hora){
		
		//SQL para retornar o ID de todos os pedidos filtrados 
		Query sql = getSession().createQuery("select distinct abb01num,eaa01id "+
										"from eaa01 "+
										"inner join eaa0103 on eaa0103doc = eaa01id "+
										"inner join abb01 on abb01id = eaa01central "+
										"inner join abd01 on abd01id = eaa01pcd "+
										"inner join eaa0102 on eaa0102doc = eaa01id "+
										"inner join abe01 desp on desp.abe01id = eaa0102despacho "+ 
										"where true "+
										(dataEmissaoIni != null && dataEmissaoFin ? "and abb01data between :dataEmissaoIni and :dataEmissaoFin " : "") +
										(dataSaidaIni != null && dataSaidaFin != null ? "and eaa0103dtentrega between :dataSaidaIni and :dataSaidaFin " : "") +
										(despacho != null ? "and eaa0102despacho in (:despacho) " : "") +
										(hora != null ? "and substring(abb01operhora::text,0,6) <= :hora " : "")+
										"and abd01es = 1 and abd01aplic = 0 "+
										"and eaa01cancdata is null "+
										"order by abb01num,eaa01id");
										
		if(dataEmissaoIni != null && dataEmissaoFin != null ){
			sql.setParameter("dataEmissaoIni",dataEmissaoIni);
			sql.setParameter("dataEmissaoFin",dataEmissaoFin);
		}

		if(dataSaidaIni != null && dataSaidaFin != null){
			sql.setParameter("dataSaidaIni", dataSaidaIni);
			sql.setParameter("dataSaidaFin", dataSaidaFin);
		}

		if(despacho != null){
			sql.setParameter("despacho",despacho);
		}

		if(hora != null){
			sql.setParameter("hora",hora);
		}

		List<TableMap> registrosSQL = sql.getListTableMap()

		return registrosSQL;
	}

	private List<TableMap> processarCaixas(Double tamanhoCaixa, Long doc, Integer tamDados, Boolean totalCaixas, Boolean totalFrasco, LocalDate dataSaidaIni){
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String dataEntrega = ""
		if(dataSaidaIni != null){
			dataEntrega = dataSaidaIni.format(formatter);
        		
		}
        	
		Integer chk1 = 0;
		Integer chk2 = 0;
		if(totalCaixas){
			chk1 = 1;
		}else{
			chk1 = 0;
		}


		if(totalFrasco){
			chk2 = 1;
		}else{
			chk2 = 0;
		}
		
		String umv = "";
		Query sqlItens = getSession().createQuery("select cast(abm0101json ->> 'volume_caixa' as numeric(16,0)) as volumeCaixa,cast(abm0101json ->> 'ordem_separacao' as numeric(16,0)) as ordemSeparacao, abb01num as numDoc, eaa01dtentrega as dataEntrega, abm01descr as descrItem, abm01codigo as codItem, eaa0103qtComl, eaa0103totDoc, "+ 
											"desp.abe01na as naDespacho, redesp.abe01na as naRedesp, cast(eaa0103json ->> 'caixa' as numeric(16,0)) as caixa, cast(eaa0103json ->>'frasco' as numeric(16,0)) as frasco, cast(eaa0103json ->> 'umv' as character varying(3)) as umv,cast(eaa0103json ->> 'qt_convertida' as numeric(16,0)) as qtConvertida "+
											"from eaa0103 "+
											"inner join eaa01 on eaa01id = eaa0103doc "+ 
											"inner join abb01 on abb01id = eaa01central "+
											"inner join eaa0102 on eaa0102doc = eaa01id  "+
											"inner join abm01 on abm01id = eaa0103item "+
											"inner join abm0101 on abm0101item = abm01id "+
											"inner join abe01 desp on desp.abe01id = eaa0102despacho "+
											"left join abe01 redesp on redesp.abe01id = eaa0102redespacho "+ 
											"where eaa01id = :doc "+ 
											"and cast(abm0101json ->> 'volume_caixa' as numeric(16,2)) = :tamanhoCaixa "+
											"order by abb01num, eaa0103seq");
		if(doc != null && tamanhoCaixa != null){
			sqlItens.setParameter("doc",doc);
			sqlItens.setParameter("tamanhoCaixa",tamanhoCaixa);
		}

		//Lista de Itens do pedido
		List<TableMap> itensPedidos = sqlItens.getListTableMap();
		
		//Lista de Itens do Pedido Para Cáculo do Total
		List<TableMap> Total = itensPedidos;

		Map<String, Integer> qtdAnteriores = new HashMap<String, Integer>(); 

		List<TableMap> dadosRelatorio = new ArrayList();
		
        	Integer qtdCaixa = tamanhoCaixa;

        	for(int i = 0; i < itensPedidos.size(); i++){
			TableMap tmItensCX = new TableMap();
			Integer cxs = itensPedidos.get(i).getInteger("volumeCaixa") / qtdCaixa;
			Integer uns = itensPedidos.get(i).getInteger("qtConvertida") % qtdCaixa;
			umv = itensPedidos.get(i).getString("umv");
			//Busca Itens Com Unidade de Medida Caixa 
			if((uns < cxs || cxs == 1) || umv == 'CX'){
				tmItensCX.put("nf",itensPedidos.get(i).getInteger("numDoc"));
				tmItensCX.put("codigo",itensPedidos.get(i).getString("codItem"));
				tmItensCX.put("item",itensPedidos.get(i).getString("descrItem"));
				if(itensPedidos.get(i).getString("codItem") == "0101001" || itensPedidos.get(i).getString("codItem") == "0101002" || itensPedidos.get(i).getString("codItem") == "0101003"){
					tmItensCX.put("tipo","LITRO ");
					tmItensCX.put("caixa",itensPedidos.get(i).getInteger("qtConvertida"));
					tmItensCX.put("ordem",1);
				}else{
					if(itensPedidos.get(i).getString("codItem").contains('0204')){
						tmItensCX.put("tipo","BALDE");
						tmItensCX.put("caixa",itensPedidos.get(i).getInteger("qtConvertida") == 0 ? itensPedidos.get(i).getBigDecimal("frasco"): itensPedidos.get(i).getBigDecimal("caixa"));
						tmItensCX.put("ordem",2);
					}else if(itensPedidos.get(i).getString("codItem").contains('0404')){
						tmItensCX.put("tipo","BAG");
						tmItensCX.put("caixa",itensPedidos.get(i).getInteger("qtConvertida") == 0 ? itensPedidos.get(i).getBigDecimal("frasco"): itensPedidos.get(i).getBigDecimal("caixa"));
						tmItensCX.put("ordem",2);
					}else{
						tmItensCX.put("tipo","CAIXA ");
						tmItensCX.put("caixa",itensPedidos.get(i).getInteger("qtConvertida") == 0 ? itensPedidos.get(i).getBigDecimal("frasco"): itensPedidos.get(i).getBigDecimal("caixa"));
						tmItensCX.put("ordem",2);
					}
				}
				//tmItensCX.put("etiqueta",dados.size()-1);
				tmItensCX.put("transp",itensPedidos.get(i).getString("naDespacho"));
				tmItensCX.put("redespacho",itensPedidos.get(i).getString("naRedesp"));
				tmItensCX.put("umv",umv);
				tmItensCX.put("ab50vlr19",itensPedidos.get(i).getInteger("ordemSeparacao"));
				dadosRelatorio.add(tmItensCX)
			}

			

			//Busca Itens Com Unidade de Medida De Venda Diferente de Caixa 
			if(umv != 'CX'){
			TableMap tmItensNaoCX = new TableMap();
                int totalUndsAnteriores = getTotalUnidAnteriores(qtdAnteriores);
                if(totalUndsAnteriores + uns >= tamanhoCaixa) {
                    Integer sobraMisto = (totalUndsAnteriores + uns) - qtdCaixa;
                    uns -= sobraMisto;
                    String descrItens = "";
                    for(String item : qtdAnteriores.keySet()) {
                        descrItens += item + " Qtd.: " + qtdAnteriores.get(item) + ";";
                    }
                    descrItens += itensPedidos.get(i).getString("descrItem") + " Qtd.: " + uns;
                
                    tmItensNaoCX.put("ab50cvdnf", qtdCaixa);
                    tmItensNaoCX.put("item",descrItens);
                    tmItensNaoCX.put("nf",itensPedidos.get(i).getInteger("numDoc"));
                    tmItensNaoCX.put("tipo","CAIXA MISTA");
                    //adicionarValor("etiqueta",dados.size()-1);
                    tmItensNaoCX.put("transp",itensPedidos.get(i).getString("naDespacho")); // parei aqui
                    tmItensNaoCX.put("redespacho",itensPedidos.get(i).getString("naRedesp"));
                    tmItensNaoCX.put("caixa",1);
                    tmItensNaoCX.put("umv",umv);
                    tmItensNaoCX.put("ordem",3);
                    qtdAnteriores.clear();
                                    
                    if(sobraMisto > 0) {
                        qtdAnteriores.put(itensPedidos.get(i).getString("descrItem") , sobraMisto);
                        //adicionarValor("ab50vlr19",rs.getInteger(i, "ab50vlr19"));
                    }
                }else if(uns > 0){
                    qtdAnteriores.put(itensPedidos.get(i).getString("descrItem") , uns);
                    //adicionarValor("ab50vlr19",rs.getInteger(i, "ab50vlr19"));
                }
                if(tmItensNaoCX.size() > 0){
                	dadosRelatorio.add(tmItensNaoCX);
                }
            }
        	}	
        	//Avulsos
   	     for(String key : qtdAnteriores.keySet() ){
           	TableMap tmAvulsos = new TableMap();
		     tmAvulsos.put("Item",key + "-(AVULSO)");
               tmAvulsos.put("nf",itensPedidos.get(0).getInteger("numDoc"));
               tmAvulsos.put("tipo","AVULSO");
               //tmAvulsos.put("etiqueta",dados.size()-1);
               tmAvulsos.put("transp",itensPedidos.get(0).getString("naDespacho"));
               tmAvulsos.put("redespacho",itensPedidos.get(0).getString("naRedesp"));
               tmAvulsos.put("caixa", qtdAnteriores.get(key));
               tmAvulsos.put("umv",itensPedidos.get(0).getString("umv"));
               tmAvulsos.put("ordem",4);
               dadosRelatorio.add(tmAvulsos);
		}

		//Busca Total
		 for(int t = 0; t < Total.size(); t++){  
		 	TableMap tmTotal = new TableMap();
	          tmTotal.put("nf",Total.get(t).getInteger("numDoc"));
	          tmTotal.put("codigo",Total.get(t).getString("codItem"));
	          tmTotal.put("item",Total.get(t).getString("descrItem"));
	          tmTotal.put("tipo","Total");
	          if(itensPedidos.get(t).getString("codItem").contains('0204')){
	           	tmTotal.put("tipo","TotalBALDE");
	          }else if(itensPedidos.get(t).getString("codItem").contains('0404')){
		          tmTotal.put("tipo","TotalBAG");
	          }
	          tmTotal.put("qtd",Total.get(t).getString("umv") != 'CX' ? Total.get(t).getInteger("qtConvertida") : Total.get(t).getInteger("qtConvertida") * Total.get(t).getInteger("volumeCaixa"));
	          if(itensPedidos.get(t).getString("codItem") == "0101001" ||itensPedidos.get(t).getString("codItem") == "0101002" || itensPedidos.get(t).getString("codItem") == "0101003"){
	            tmTotal.put("cxs",0);
	            tmTotal.put("frs",0);
	          }else{
	            tmTotal.put("cxs", Total.get(t).getBigDecimal("caixa"));
	            tmTotal.put("frs", Total.get(t).getBigDecimal("frasco"));
	          }
	          tmTotal.put("caixa",-1);
	          tmTotal.put("ordem",0);
	          tmTotal.put("ab50cvdnf", qtdCaixa);
	          dadosRelatorio.add(tmTotal);
	      }
	       adicionarParametro("Periodo",dataEntrega);
		  adicionarParametro("TotItem",chk1);
		  adicionarParametro("TotFrsc",chk2);

		  return dadosRelatorio;
	}
	private int getTotalUnidAnteriores(Map<String, Integer> qtdAnteriores) {
	    int retorno = 0;
	    for(String key : qtdAnteriores.keySet()) {
	        retorno += qtdAnteriores.get(key);
	    }
	    return retorno;
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJvbWFuZWlvIENhcmdhIEdsb2JhbCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==