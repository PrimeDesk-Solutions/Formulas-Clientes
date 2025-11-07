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

public class CUBO_SRF_DevolucaoDadosParaFaturamento extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SRF - Docs Devolvidos Por Representante"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("numeroInicial", 1,
						"numeroFinal", 9999999999)
	}
	@Override 
	public DadosParaDownload executar() {
		List<TableMap> dados = dadosRelatorio();
		gerarXLSX("CUBO_SRF_DocumentosDevolvidosPorRepresentante",dados);
	}

	private List<TableMap>dadosRelatorio(){
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		List<Long> tipoDoc = getListLong("tipoDoc");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;
		LocalDate dataEntradaSaidaIni = null;
		LocalDate dataEntradaSaidaFin = null;
		List<Long> filtroRep = getListLong("representante");
		List<Long> filtroEnt = getListLong("entidade");
 
		Query rep = getSession().createQuery("select abe01id, abe01codigo, abe01na from abe01 "+
										"where abe01rep = 1 "+ 
										(filtroRep != null ? "and abe01codigo in (:filtroRep) ":""));
		if(dataEmissao != null){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1];							
		}

		if(dataEmissao != null){
			dataEntradaSaidaIni = dataEmissao[0];
			dataEntradaSaidaFin = dataEmissao[1];								
		}
		
		if(filtroRep != null){
			rep.setParameter("filtroRep",filtroRep);
		}
		List<TableMap>tmRepresentantes = rep.getListTableMap();
		List<TableMap> documentos = new ArrayList()

		for(TableMap representante : tmRepresentantes){
			Long idRep = representante.getLong("abe01id");
			String naRep = representante.getString("abe01na");
			
			Query sql = getSession().createQuery("select  eaa0103dev.eaa0103id as eaa0103id, abb01dev.abb01num as numDocDevolvido, eaa01dev.eaa01totitens as totalItensDevolvido, eaa01dev.eaa01totdoc as totalDocDev, eaa0103dev.eaa0103qtComl as qtdOriginal, "+
											"abe01dev.abe01codigo as codigoEntidadeDev, abe01dev.abe01na as naEntidadeDev, abb01dev.abb01data as dataEmissaoDev, eaa01dev.eaa01esdata as dataEntradaSaidaDev, abd01dev.abd01codigo as PcdDev, "+
											"aah01dev.aah01codigo as tipoDocDev, aae10descr as motivoDevolucao, abe01TranspDev.abe01codigo as codigoDespDev, abe01TranspDev.abe01na as naDespDev, abb01orig.abb01num as numDocOrig, "+
											"eaa01033qtComl as qtDevolvido, eaa0103dev.eaa0103unit as unitDevolvido, (eaa01033qtComl * eaa0103dev.eaa0103unit) as valorDevolvido "+
											"from eaa01 eaa01dev "+ 
											"inner join eaa0103 eaa0103dev on eaa0103dev.eaa0103doc = eaa01dev.eaa01id "+
											"inner join eaa01033 on eaa01033item = eaa0103dev.eaa0103id "+
											"inner join abb01 abb01dev on abb01dev.abb01id = eaa01dev.eaa01central "+
											"inner join abe01 abe01dev on abe01dev.abe01id = abb01dev.abb01ent "+
											"inner join abd01 abd01dev on abd01dev.abd01id = eaa01dev.eaa01pcd "+
											"inner join abb10 abb10dev on abb10dev.abb10id = abd01dev.abd01opercod "+
											"inner join aah01 aah01dev on aah01dev.aah01id = abd01dev.abd01tipo "+
											"left join aae10 on aae10id = eaa01dev.eaa01motivodev "+
											"inner join eaa0102 eaa0102dev on eaa0102dev.eaa0102doc = eaa01dev.eaa01id "+
											"left join abe01 abe01TranspDev on abe01TranspDev.abe01id =  eaa0102dev.eaa0102despacho "+
											"inner join eaa0103 eaa0103orig on eaa0103orig.eaa0103id = eaa01033itemdoc "+
											"inner join eaa01 eaa01orig on eaa01orig.eaa01id = eaa0103orig.eaa0103doc "+
											"inner join abb01 abb01orig on abb01orig.abb01id = eaa01orig.eaa01central "+
											(numDocIni != null && numDocFin  != null ? "where abb01dev.abb01num between :numDocIni and :numDocFin ":"" )+
											"and abb10dev.abb10tipocod = 4 "+
											"and eaa01033qtComl > 0 "+
											 " AND eaa01dev.eaa01clasDoc = 1 " +
											 " AND eaa01dev.eaa01esMov = 0 " +
											(tipoDoc != null ? "and aah01dev.aah01id in (:tipoDoc) ": "" )+
											(filtroEnt != null ? "and abe01dev.abe01id in (:filtroEnt) ": "" )+
											(idRep != null ? "and (eaa01dev.eaa01rep0 = :idRep or eaa01dev.eaa01rep1 = :idRep or eaa01dev.eaa01rep1 = :idRep or eaa01dev.eaa01rep1 = :idRep or eaa01dev.eaa01rep1 = :idRep) ": "" )+
											(dataEmissaoIni != null && dataEmissaoFin != null ? "and abb01dev.abb01data between :dataEmissaoIni and :dataEmissaoFin ": "" )+
											(dataEntradaSaidaIni != null && dataEntradaSaidaFin != null ? "and eaa01dev.eaa01esdata between :dataEntradaSaidaIni and :dataEntradaSaidaFin ": "" )+
											"and eaa01dev.eaa01cancdata is null "+
											"order by abb01dev.abb01num ");
											
			if(numDocIni != null && numDocFin != null){
				sql.setParameter("numDocIni",numDocIni);
				sql.setParameter("numDocFin",numDocFin);
			}
			
			if(tipoDoc != null){
				sql.setParameter("tipoDoc",tipoDoc);
			}

			if(filtroEnt != null){
				sql.setParameter("filtroEnt",filtroEnt);
			}

			if(idRep != null){
				sql.setParameter("idRep",idRep)
			}
			if(dataEmissaoIni != null && dataEmissaoFin != null){
				sql.setParameter("dataEmissaoIni",dataEmissaoIni);
				sql.setParameter("dataEmissaoFin",dataEmissaoFin);
			}

			if(dataEntradaSaidaIni != null && dataEmissaoFin != null){
				sql.setParameter("dataEntradaSaidaIni",dataEntradaSaidaIni);
				sql.setParameter("dataEntradaSaidaFin",dataEntradaSaidaFin);
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
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBEb2NzIERldm9sdmlkb3MgUG9yIFJlcHJlc2VudGFudGUiLCJ0aXBvIjoicmVsYXRvcmlvIn0=