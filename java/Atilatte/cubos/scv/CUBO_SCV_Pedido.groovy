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
import sam.model.entities.ea.Eaa01;

public class CUBO_SCV_Pedido extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Pedido"; 
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
		List<Long> codDesp = getListLong("despacho");
		LocalDate[] dataEmiss = getIntervaloDatas("dataEmissao");

		//Datas Emissão 
		LocalDate dataEmissIni = null;
		LocalDate dataEmissFin = null;
		if(dataEmiss != null){
			 dataEmissIni = dataEmiss[0];
			 dataEmissFin = dataEmiss[1];
			 
		}
		
		
		
		List<TableMap> dados = obterDadosRelatorio(numDocIni, numDocFin, codDesp, dataEmissIni, dataEmissFin);
		gerarXLSX("Pedido",dados);
		
		
	}

	private List<TableMap>obterDadosRelatorio(Integer numDocIni, Integer numDocFin, List<Long> codDesp, LocalDate dataEmissIni, LocalDate dataEmissFin){
		
		Query sql =  getSession().createQuery("select abb01num as numDoc, eaa01totdoc as totalDoc, cast(eaa01json ->> 'qt_convertida' as numeric(16,2)) as qtFaturamento, "+
										"cast(ent.abe01camposcustom ->> 'ramo_atividade' as character varying(20)) as ramo, abm01tipo as MPS, ent.abe01codigo as codEntidade, ent.abe01na as naEntidade, abd01es as oper, abb10tipocod as tipoOper, abb01data as dataEmiss, "+
										"desp.abe01codigo as codDespacho, desp.abe01na as naDesp, abm01codigo as codItem, abm01na as naitem "+
										"from eaa01 "+
										"inner join abb01 on eaa01central = abb01id "+
										"inner join abe01 ent on abb01ent = ent.abe01id "+
										"inner join eaa0103 on eaa01id = eaa0103doc "+
										"inner join abm01 on eaa0103item = abm01id "+ 
										"inner join abd01 on eaa01pcd = abd01id "+
										"inner join abb10 on abd01opercod = abb10id "+
										"inner join eaa0102 on eaa01id = eaa0102doc "+
										"inner join abe01 desp on eaa0102despacho = desp.abe01id "+
										 getSamWhere().getWherePadrao("WHERE", Eaa01.class) +
										(numDocIni != null && numDocFin ? "and abb01num between :numDocIni and :numDocFin " : "") +
										(dataEmissIni != null && dataEmissFin != null ? "and abb01data between :dataEmissIni and :dataEmissFin " : "") +
										(codDespIni != null ? "and desp.abe01codigo in (:codDesp) " : "") +
										"and abd01es = 1 "+ 
										"and abd01aplic = 0 "+ 
										"and abm01tipo = 1 ");
								
		if(numDocIni != null && numDocFin != null ){
			sql.setParameter("numDocIni", numDocIni);
			sql.setParameter("numDocFin", numDocFin);
		}
		
		if(dataEmissIni != null && dataEmissFin != null ){
			sql.setParameter("dataEmissIni", dataEmissIni);
			sql.setParameter("dataEmissFin", dataEmissFin);
		}
		
		if(codDespIni != null ){
			sql.setParameter("codDesp", codDesp);
		}


		List<TableMap> dadosSQL = sql.getListTableMap();
		
		return dadosSQL;
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBQZWRpZG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=