package Atilatte.cubos.srf;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate;
import sam.server.samdev.relatorio.TableMapDataSource;
import sam.server.samdev.utils.Parametro;
import java.time.LocalDate;

import sam.model.entities.ea.Eaa01;

public class CUBO_SRF_FaturamentoPorTransportadora extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SRF - Faturamento Por Transportadora"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		String codDespIni = getString("codDespachoIni");
		String codDespFin = getString("codDespachoFin");
		LocalDate dataEmissIni = getLocalDate("dataEmissaoIni");
		LocalDate dataEmissFin = getLocalDate("dataEmissaoFin");
		
		List<TableMap> dados = obterDadosRelatorio(numDocIni, numDocFin, codDespIni, codDespFin, dataEmissIni, dataEmissFin);
		gerarXLSX("FaturamentoPorTransportadora",dados);
		
		
	}

	private List<TableMap>obterDadosRelatorio(Integer numDocIni, Integer numDocFin, String codDespIni, String codDespFin, LocalDate dataEmissIni, LocalDate dataEmissFin){
		Query sql =  getSession().createQuery("select abb01num as numDoc, eaa0103totdoc as totDocItem, cast(eaa0103json ->> 'qt_convertida' as numeric(16,2)) as qtFaturamento, eaa0103total as totItem, "+
								"cast(ent.abe01camposcustom ->> 'ramo_atividade' as character varying(20)) as ramo, abm01tipo as tipo, ent.abe01codigo as codEntidade, ent.abe01na as naEntidade, "+
								"abd01es as oper, abb10tipocod as tipoOper, abb01data as dataEmiss, desp.abe01codigo as codDespacho, desp.abe01na as naDesp, abm01codigo as codItem, abm01na as naItem,abd01codigo as PCD "+
								"from eaa01 "+
								"inner join abb01 on abb01id = eaa01central "+
								"inner join eaa0103 on eaa0103doc = eaa01id "+
								"inner join abe01 as ent on ent.abe01id = abb01ent "+ 
								"inner join abd01 on abd01id = eaa01pcd "+
								"inner join abb10 on abb10id = abd01opercod "+
								"inner join aah01 on aah01id = abb01tipo "+
								"inner join eaa0102 on eaa0102doc = eaa01id "+
								"left join abe01 as desp on desp.abe01id = eaa0102despacho "+
								"inner join abm01 on abm01id = eaa0103item "+
								 getSamWhere().getWherePadrao("WHERE", Eaa01.class) +
								(numDocIni != null && numDocFin != null ? "and abb01num between :numDocIni and :numDocFin " : "" )+
								(dataEmissIni != null && dataEmissFin != null ? "and abb01data between :dataEmissIni and :dataEmissFin " : "" )+
								//"and abd01es = 1 and abd01aplic = 1 " + 
								(codDespIni != null && codDespFin != null ? "and desp.abe01codigo between :codDespIni and :codDespFin " : "" )+
								"and abm01tipo = 1 " );
								
		if(numDocIni != null && numDocFin != null ){
			sql.setParameter("numDocIni", numDocIni);
			sql.setParameter("numDocFin", numDocFin);
		}
		if(dataEmissIni != null && dataEmissFin != null ){
			sql.setParameter("dataEmissIni", dataEmissIni);
			sql.setParameter("dataEmissFin", dataEmissFin);
		}
		if(codDespIni != null && codDespFin != null ){
			sql.setParameter("codDespIni", codDespIni);
			sql.setParameter("codDespFin", codDespFin);
		}
		
		List<TableMap> dadosSQL = sql.getListTableMap();
		return dadosSQL;
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBGYXR1cmFtZW50byBQb3IgVHJhbnNwb3J0YWRvcmEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=