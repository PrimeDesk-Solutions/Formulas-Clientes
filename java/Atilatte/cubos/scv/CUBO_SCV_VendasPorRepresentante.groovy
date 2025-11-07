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

import java.time.LocalDate

public class CUBO_SCV_VendasPorRepresentante extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Vendas Por Representante"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		
		Long    tipoDocIni = getLong("tipoDocIni");
		Long    tipoDocFin = getLong("tipoDocFin");
		Long    idRepIni   = getLong("repIni");
		Long    idRepFin   = getLong("repFin");
		Long    idEntIni   = getLong("entIni");
		Long    idEntFin   = getLong("entFin");
		Long    idItemIni  = getLong("itemIni");
		Long    idItemFin  = getLong("itemFin");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		Integer numDocIni  = getInteger("numeroInicial");
		Integer numDocfin  = getInteger("numeroFinal");

		List<TableMap>dadosRelatorio = buscarDadosRelatorio(dataEmissao,dataEntradaSaida,tipoDocIni,tipoDocFin,idRepIni,idRepFin,idEntIni,idEntFin, idItemIni, idItemFin,numDocIni,numDocfin)
		gerarXLSX("VendasPorRepresentante",dadosRelatorio);
	}

	private List<TableMap> buscarDadosRelatorio(LocalDate[] dataEmissao, LocalDate[] dataEntradaSaida,  Long tipoDocIni, Long tipoDocFin, Long idRepIni, Long idRepFin, Long idEntIni, Long idEntFin, Long idItemIni, Long idItemFin,Integer numDocIni,Integer numDocfin){

		
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
										"and abe01id between :idRepIni and :idRepFin ");
		//Seta os parametros para inicial e final com ID dos representantes								
		if(idRepIni != null && idRepFin != null){
			rep.setParameter("idRepIni",idRepIni);
			rep.setParameter("idRepFin",idRepFin);
		}

		List<TableMap> listaRepresentante = rep.getListTableMap();
		List<TableMap> documentos = new ArrayList()

		for(TableMap representante : listaRepresentante){
			Long idRep = representante.getLong("abe01id");
			String naRep = representante.getString("abe01na");

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
											"where abb01num between :numDocIni and :numDocfin "+
											"and abb10tipocod = 1 "+
											"and aah01id between :tipoDocIni and :tipoDocFin "+
											"and abe01id between :idEntIni and :idEntFin "+
											"and (eaa01rep0 = :idRep or eaa01rep1 = :idRep or eaa01rep2 = :idRep or eaa01rep3 = :idRep or eaa01rep4 = :idRep) "+
											"and abb01data between :dataEmissaoIni and :dataEmissaoFin "+
											"and eaa01esdata between :esDataIni and :esDataFin "+
											"and eaa01cancdata is null "+
											"and abm01id between :idItemIni and :idItemFin");

		
			if(numDocIni != null && numDocfin != null){
				sql.setParameter("numDocIni",numDocIni);
				sql.setParameter("numDocfin",numDocfin);
			}
			
			if(tipoDocIni != null && tipoDocFin != null){
				sql.setParameter("tipoDocIni",tipoDocFin);
				sql.setParameter("tipoDocFin",tipoDocFin);
			}

			if(idEntIni != null && idEntFin != null){
				sql.setParameter("idEntIni",idEntIni);
				sql.setParameter("idEntFin",idEntFin);
			}

			if(idRep != null){
				sql.setParameter("idRep",idRep)
			}

			if(esDataIni != null && esDataFin != null){
				sql.setParameter("esDataIni",esDataIni);
				sql.setParameter("esDataFin",esDataFin);
			}

			if(dataEmissaoIni != null && dataEmissaoFin != null){
				sql.setParameter("dataEmissaoIni",dataEmissaoIni);
				sql.setParameter("dataEmissaoFin",dataEmissaoFin);
			}

			if(idItemIni != null && idItemFin != null){
				sql.setParameter("idItemIni",idItemIni);
				sql.setParameter("idItemFin",idItemFin);
			}

			List<TableMap>registroSql = sql.getListTableMap()
			for(TableMap registro : registroSql){
				registro.put("naRep",naRep);
				documentos.add(registro);
			}
		}
		
		return documentos;
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBWZW5kYXMgUG9yIFJlcHJlc2VudGFudGUiLCJ0aXBvIjoicmVsYXRvcmlvIn0=