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

public class CUBO_SCV_SuporteAnalisedeMargem extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Suporte Analise de Margem"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("operacao",0,"tipoOperacao",0)
	}
	@Override 
	public DadosParaDownload executar() {
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		List<Long> tipoDoc = getListLong("tipoDoc");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		List<Long> entidade = getListLong("entidade");
		Integer oper = getInteger("operacao");
		Integer tipoOper = getInteger("tipoOperacao");

		List<TableMap> dadosRelatorio = buscarDocumentos(numDocIni, numDocFin, tipoDoc,dataEmissao,dataEntradaSaida, entidade, oper, tipoOper);
		
		gerarXLSX("RelatorioDeSuporteAnaliseDeMargem", dadosRelatorio);
	}

	private List<TableMap> buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> tipoDoc,LocalDate[] dataEmissao, LocalDate[] dataEntradaSaida, List<Long> entidade, Integer oper, Integer tipoOper){
		
		String operacao = "";
		String tipoOperacao = "";
		Long idNota;
		def valorTotal;
		def valorTotalNota;
		def valorTotalDaDevolucao;

		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;
		LocalDate dataEntradaSaidaIni = null;
		LocalDate dataEntradaSaidaFin = null;

		if(dataEmissao != null){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1];
		}

		if(dataEntradaSaida != null){
			dataEntradaSaidaIni = dataEntradaSaida[0];
			dataEntradaSaidaFin = dataEntradaSaida[1];
		}

		if(oper == 0){
			operacao = "AND abd01es = 0";
		}else if(oper == 1){
			operacao = "AND abd01es = 1 ";
		}else{
			operacao = "AND abd01es in (0,1)";
		}

		if(tipoOper = 99){
			tipoOperacao = "AND abb10tipocod in (0,1,2,3,4,5,6,7) ";
		}else{
			tipoOperacao = "AND abb10tipocod = :tipoOper ";
		}

		
		
		Query query = getSession().createQuery(" select abb01id as idNota, eaa01id, abb01num as numero, eaa01totDoc as totalDocumento, eaa01totitens as totalItens,cast(eaa01.eaa01json ->> 'qt_original' as numeric(16,2)) as qtOriginal, "+
										"abd01descr as descrPCD, abe01codigo as codEntidade, abe01na as nomeEntidade, abb01data as dataEmissao, eaa01esdata as entradaSaida, aah01codigo as tipoDoc "+
										"from eaa01 "+
										"inner join aac10 on aac10id = eaa01eg "+
										"inner join abb01 on abb01id = eaa01central "+
										"inner join abe01 on abe01id = abb01ent "+
										"inner join abd01 on abd01id = eaa01pcd "+
										"inner join abb10 on abd01opercod = abb10id "+
										"inner join aah01 on aah01id = abd01tipo "+
										getSamWhere().getWherePadrao("WHERE", Eaa01.class) + 
										(numDocIni != null && numDocFin != null ? "and abb01num between :numDocIni and :numDocFin " : "") +
										(tipoDoc != null ? "and aah01id in (:tipoDoc) " : "" )+
										(dataEmissaoIni != null && dataEmissaoFin != null ?"and abb01data between :dataEmissaoIni and :dataEmissaoFin " : "") +
										(dataEntradaSaidaIni != null && dataEntradaSaidaFin != null ?"and eaa01esdata between :dataEntradaSaidaIni and :dataEntradaSaidaFin " : "") +
										operacao+
										tipoOperacao+
										(entidade != null ? "and abe01id in (:entidade) " : "")+
										"and eaa01cancdata is null");
										
	
	  List<TableMap> documentos = new ArrayList()

	  if(numDocIni != null && numDocFin != null){
  	
	  	query.setParameter("numDocIni", numDocIni);
	  	query.setParameter("numDocFin", numDocFin);
	  	
	  }

	  if(tipoDoc != null ){
	  	query.setParameter("tipoDoc", tipoDoc);	  	
	  }

	  if(dataEmissaoIni != null && dataEmissaoFin != null){
	  	query.setParameter("dataEmissaoIni", dataEmissaoIni);
	  	query.setParameter("dataEmissaoFin", dataEmissaoFin);
	  	
	  }

	  if(dataEntradaSaidaIni != null && dataEntradaSaidaFin != null){
	  	query.setParameter("dataEntradaSaidaIni", dataEntradaSaidaIni);
	  	query.setParameter("dataEntradaSaidaFin", dataEntradaSaidaFin);
	  	
	  }

	  if(tipoOper != null){
	  	query.setParameter("tipoOper", tipoOper);

	  }

	
	  if(entidade != null ){
	  	query.setParameter("entidade", entidade);	  	
	  }

	    List<TableMap>registrosSQL = query.getListTableMap();
	    

	   
	
	  for(TableMap registro : registrosSQL){
	  	idNota = registro.getLong("idNota");
		TableMap totalDevolvido = obterTotalDevolvidoPorDocumento(idNota);
		
		if(totalDevolvido.getBigDecimal("totalDevolvido") != null){
			valorTotalNota = registrosSQL.size() > 0 ? registro.getBigDecimal("totalDocumento") : 0;
		 	valorTotalDaDevolucao = totalDevolvido.size() > 0 ? totalDevolvido.getBigDecimal("totalDevolvido") : 0;
		 	valorTotal = valorTotalNota - valorTotalDaDevolucao;
		 	registro.put("TotalDoc",valorTotal);
			
		 	documentos.add(registro);
		}
	  }
	  return documentos;
	 
	}

	private TableMap obterTotalDevolvidoPorDocumento(Long idNota){
		Query totalDocDevolvido = getSession().createQuery("select sum(eaa0103devol.eaa0103totDoc) as totalDevolvido from eaa01033 "+
													"inner join eaa0103 eaa0103princ on eaa0103princ.eaa0103id = eaa01033itemdoc "+
													"inner join eaa01 eaa01princ on eaa01princ.eaa01id = eaa0103princ.eaa0103doc "+
													"inner join abb01 abb01princ on abb01princ.abb01id = eaa01princ.eaa01central "+
													"inner join eaa0103 eaa0103devol on eaa0103devol.eaa0103id = eaa01033item "+
													"inner join eaa01 eaa01devol on eaa01devol.eaa01id = eaa0103devol.eaa0103doc "+
													"inner join abb01 abb01devol on abb01devol.abb01id = eaa01devol.eaa01central "+
													"where abb01princ.abb01id = :idNota ");
		
															
		if(idNota != null ){
			totalDocDevolvido.setParameter("idNota",idNota);											
		}
		return totalDocDevolvido.getUniqueTableMap();	
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBTdXBvcnRlIEFuYWxpc2UgZGUgTWFyZ2VtIiwidGlwbyI6InJlbGF0b3JpbyJ9