package Atilatte.cubos.scv;
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
import sam.model.entities.ea.Eaa01;

import java.time.LocalDate

public class CUBO_SCV_VendasPorProduto extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Vendas Por Produto"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		
		List<Long> tipoDoc = getListLong("tipoDoc");
		List<Long> representantes = getListLong("rep");
		List<Long> idEnt = getListLong("entidade");
		List<Long> idItem = getListLong("itens");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		Integer numDocIni  = getInteger("numeroInicial");
		Integer numDocfin  = getInteger("numeroFinal");

		List<TableMap>dadosRelatorio = buscarDadosRelatorio(dataEmissao,dataEntradaSaida,tipoDoc,representantes,idEnt, idItem,numDocIni,numDocfin)
		gerarXLSX("VendasPorProduto",dadosRelatorio);
	}

	private List<TableMap> buscarDadosRelatorio(LocalDate[] dataEmissao, LocalDate[] dataEntradaSaida,  List<Long> tipoDoc, List<Long> representantes, List<Long> idEnt, List<Long> idItem,Integer numDocIni,Integer numDocfin){

		
		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;
		LocalDate esDataIni = null;
		LocalDate esDataFin = null;

		//Define as datas inicial e final de emissão
		if(dataEmissao != null){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1];
		}

	
		//Define as datas Inicial e Final de Entrada e Saída
		if(dataEntradaSaida != null){
			esDataIni = dataEmissao[0];
			esDataFin = dataEmissao[1];
		}
		
		//Buscando entidades caracterizadas como representante 
		Query rep = getSession().createQuery("select abe01id, abe01codigo,abe01na from abe01 "+
										"where abe01rep = 1 "+
										(representantes != null ? "and abe01id in (:representantes) " : ""));
										
		//Seta os parametros para inicial e final com ID dos representantes								
		if(representantes != null){
			rep.setParameter("representantes",representantes);
		}

		List<TableMap> listaRepresentante = rep.getListTableMap()
		List<TableMap> documentos = new ArrayList()

		for(int i = 0; i < listaRepresentante.size(); i++ ){
			def idRep = listaRepresentante.get(i).getLong("abe01id")

			Query sql = getSession().createQuery("select abb01num as numDoc, eaa0103total as totalItem, eaa0103totDoc as totalDocumento, eaa0103qtUso as QtdSCE, abe01codigo as codigoEntidade, abe01na as nomeEntidade, TO_CHAR(abb01data,'yyyy/mm') as periodo, abb01data as dataEmissao, "+
											"eaa01esdata as dataEntradaSaida, abd01descr as descrPCD, abd01codigo as codigoPCD, abm01descr as descrItem "+
											"from eaa01 "+
											"inner join abb01 on abb01id = eaa01central "+
											"inner join eaa0103 on eaa0103doc = eaa01id "+
											"inner join abe01 on abe01id = abb01ent "+
											"inner join abd01 on abd01id = eaa01pcd "+
											"inner join abm01 on abm01id = eaa0103item "+
											"inner join abb10 on abb10id = abd01opercod "+
											"inner join aah01 on aah01id = abd01tipo "+
											getSamWhere().getWherePadrao("WHERE", Eaa01.class) + 
											(numDocIni != null && numDocfin != null ?"and abb01num between :numDocIni and :numDocfin ": "")+
											"and abb10tipocod = 1 "+
											(tipoDocIni != null ? "and aah01id in (:tipoDoc) " : "") +
											(idEnt != null ? "and abe01id in (:idEnt) " : "") +
											(idRep != null ? "and (eaa01rep0 = :idRep or eaa01rep1 = :idRep or eaa01rep2 = :idRep or eaa01rep3 = :idRep or eaa01rep4 = :idRep) " : "" ) +
											(dataEmissaoIni != null && dataEmissaoFin != null ? "and abb01data between :dataEmissaoIni and :dataEmissaoFin " : "") +
											(esDataIni != null && esDataFin != null ? "and eaa01esdata between :esDataIni and :esDataFin " : "") +
											"and eaa01cancdata is null "+
											(idItem != null ? "and abm01id in (:idItem) " : "" ));

		
			if(numDocIni != null && numDocfin != null){
				sql.setParameter("numDocIni",numDocIni);
				sql.setParameter("numDocfin",numDocfin);
			}
			
			if(tipoDocIni != null){
				sql.setParameter("tipoDoc",tipoDoc);
			}

			if(idEnt != null){
				sql.setParameter("idEnt",idEnt);
			}

			if(idRep != null){
				sql.setParameter("idRep",idRep)
			}
			
			if(dataEmissaoIni != null && dataEmissaoFin != null){
				sql.setParameter("dataEmissaoIni",dataEmissaoIni);
				sql.setParameter("dataEmissaoFin",dataEmissaoFin);
			}

			if(esDataIni != null && esDataFin != null){
				sql.setParameter("esDataIni",esDataIni);
				sql.setParameter("esDataFin",esDataFin);
			}
			
			if(idItem != null ){
				sql.setParameter("idItem",idItem);
				
			}

			List<TableMap>dados = sql.getListTableMap()
			for(TableMap registro : dados){
				documentos.add(registro);
			}
		}
		
		return documentos;
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBWZW5kYXMgUG9yIFByb2R1dG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=