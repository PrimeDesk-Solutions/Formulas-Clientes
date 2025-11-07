package Atilatte.cubos.srf;
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

public class CUBO_SRF_DocumentosDevolvidosPorRepresentante extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SRF - DevolucaoDadosParaFaturamento"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		
		List<Long> tipoDoc = getLong("tipoDoc");
		List<Long> Rep = getLong("rep");
		List<Long> idEnt = getLong("ent");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		Integer numDocIni  = getInteger("numeroInicial");
		Integer numDocfin  = getInteger("numeroFinal");

		List<TableMap>dadosRelatorio = buscarDadosRelatorio(dataEmissao,dataEntradaSaida,tipoDoc,Rep,idEnt,numDocIni,numDocfin)
		gerarXLSX("CUBO_SRF_DevolucaoDadosParaFaturamento",dadosRelatorio);
	}

	private List<TableMap> buscarDadosRelatorio(LocalDate[] dataEmissao, LocalDate[] dataEntradaSaida,  List<Long> tipoDoc,List<Long> Rep,List<Long> idEnt,Integer numDocIni,Integer numDocfin){

		
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
			esDataIni = dataEntradaSaida[0];
			esDataFin = dataEntradaSaida[1];
		}
		
		//Buscando entidades caracterizadas como representante 
		Query rep = getSession().createQuery("select abe01id, abe01codigo,abe01na from abe01 "+
										"where abe01rep = 1 "+
										(Rep != null ? "and abe01id in (:Rep) " : ""));
										
		//Seta os parametros para inicial e final com ID dos representantes								
		if(Rep != null){
			rep.setParameter("Rep",Rep);
		}

		List<TableMap> listaRepresentante = rep.getListTableMap()
		List<TableMap> documentos = new ArrayList()

		for(int i = 0; i < listaRepresentante.size(); i++ ){
			def idRep = listaRepresentante.get(i).getLong("abe01id")

			Query sql = getSession().createQuery("SELECT eaa0103dev.eaa0103id as eaa0103id, abb01dev.abb01num as numDocDev,eaa01dev.eaa01totitens as totItemDev ,eaa01dev.eaa01totdoc as totDocDev, cast(eaa01dev.eaa01json ->> 'qt_original' as numeric(16,2)) as qtOriginalDev, abe01dev.abe01codigo as codigoEntidadeDev, abe01dev.abe01na as naEntidadeDev,abb01dev.abb01data as dataEmissDev, eaa01dev.eaa01esdata as dataEntradaSaidaDev, "+
											"abd01dev.abd01descr as pcdDevolvido,aah01dev.aah01codigo as tipoDocDevolvido, aae10descr as descrDevolucao,abb01orig.abb01num as numDocOriginal, "+
											"eaa01033qtComl as qtDevolvido, eaa0103dev.eaa0103unit as unitDevolvido, (eaa01033qtComl * eaa0103dev.eaa0103unit) as valorDevolvido "+
											"FROM Eaa01 eaa01dev "+
											"INNER JOIN Eaa0103 eaa0103dev ON eaa01dev.eaa01id = eaa0103dev.eaa0103doc "+
											"INNER JOIN Abb01 abb01dev ON abb01dev.abb01id = eaa01dev.eaa01central "+
											"INNER JOIN Aah01 aah01dev ON aah01dev.aah01id = abb01dev.abb01tipo "+
											"INNER JOIN Abe01 abe01dev ON abe01dev.abe01id = abb01dev.abb01ent "+
											"inner join abd01 abd01dev on abd01dev.abd01id = eaa01dev.eaa01pcd "+
											"inner join abb10 abb10dev on abb10dev.abb10id = abd01dev.abd01opercod "+
											"INNER JOIN Eaa01033 eaa01033 ON eaa01033.eaa01033item = eaa0103dev.eaa0103id "+
											"INNER JOIN Eaa0103 eaa0103orig ON eaa0103orig.eaa0103id = eaa01033.eaa01033itemDoc "+
											"INNER JOIN Eaa01 eaa01orig ON eaa01orig.eaa01id = eaa0103orig.eaa0103doc "+
											"inner join abd01 abd01orig on abd01orig.abd01id = eaa01orig.eaa01pcd "+
											"INNER JOIN Abb01 abb01orig ON abb01orig.abb01id = eaa01orig.eaa01central "+
											"INNER JOIN Aah01 aah01orig ON aah01orig.aah01id = abb01orig.abb01tipo "+
											"LEFT JOIN Aae10 ON aae10id = eaa01dev.eaa01motivoDev "+
											(numDocIni != null && numDocfin != null ? "where abb01dev.abb01num between :numDocIni and :numDocfin " : "" )+
											"and abb10dev.abb10tipocod = '4' "+
											"AND eaa01dev.eaa01clasDoc = 1 " +
											"AND eaa01dev.eaa01esMov = 0 " +
											"and eaa01033qtComl > 0 "+
											(tipoDoc != null? "and aah01dev.aah01id in (:tipoDoc) " : "" )+
											(idEnt != null ? "and abe01dev.abe01id in (:idEnt) " : "" )+
											(idRep != null ? "and (eaa01dev.eaa01rep0 = :idRep or eaa01dev.eaa01rep1 = :idRep or eaa01dev.eaa01rep2 = :idRep or eaa01dev.eaa01rep3 = :idRep or eaa01dev.eaa01rep4 = :idRep) " : "" )+
											(dataEmissaoIni != null && dataEmissaoFin != null ? "and abb01dev.abb01data between :dataEmissaoIni and :dataEmissaoFin " : "" )+
											(esDataIni != null && esDataFin != null ? "and eaa01dev.eaa01esdata between :esDataIni and :esDataFin ": "" )+
											"and eaa01dev.eaa01cancdata is null "+
											"order by abb01dev.abb01num ");

		
			if(numDocIni != null && numDocfin != null){
				sql.setParameter("numDocIni",numDocIni);
				sql.setParameter("numDocfin",numDocfin);
			}
			
			if(tipoDoc != null){
				sql.setParameter("tipoDoc",tipoDoc);
			}

			if(idEnt != null ){
				sql.setParameter("idEnt",idEnt);
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


			List<TableMap>registroSql = sql.getListTableMap();
			
			if(registroSql != null && registroSql.size() > 0){
				Long eaa0103idAnterior = null
				for(TableMap registro : registroSql){
					Long eaa0103id = registro.getLong("eaa0103id");
					if(eaa0103idAnterior != null && eaa0103idAnterior.equals(eaa0103id)){
						registro.put("qtDevolvido", BigDecimal.ZERO);
						registro.put("unitDevolvido", BigDecimal.ZERO);
						registro.put("valorDevolvido", BigDecimal.ZERO);
					}else{
						eaa0103idAnterior = eaa0103id;
					}
					registro.put("naRep", naRep);
					documentos.add(registro);
				}
			}
		}
		
		return documentos;
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBEZXZvbHVjYW9EYWRvc1BhcmFGYXR1cmFtZW50byIsInRpcG8iOiJyZWxhdG9yaW8ifQ==