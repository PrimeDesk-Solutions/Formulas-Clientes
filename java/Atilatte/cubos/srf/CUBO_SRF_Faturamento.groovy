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

public class CUBO_SRF_Faturamento extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SRF -Faturamento"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("numeroInicial",1,
						"numeroFinal", 999999999);
	}
	@Override 
	public DadosParaDownload executar() {
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		List<Long> codDesp = getListLong("codDespacho");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		
		List<TableMap> dados = obterDadosRelatorio(numDocIni, numDocFin, codDesp, dataEmissao);
		gerarXLSX("Faturamento",dados);
		
		
	}

	private List<TableMap>obterDadosRelatorio(Integer numDocIni, Integer numDocFin, List<Long> codDesp, LocalDate[] dataEmissao){
		LocalDate dataEmissIni = null;
		LocalDate dataEmissFin = null;

		if(dataEmissao != null){
			dataEmissIni = dataEmissao[0];
			dataEmissFin = dataEmissao[1];
		}
		
		Query sql =  getSession().createQuery("select abb01num as numDoc, eaa0103totdoc as totDocItem, cast(eaa0103json ->> 'qt_convertida' as numeric(16,2)) as qtFaturamento, eaa0103total as totalItem, "+
										"cast(ent.abe01camposcustom ->> 'ramo_atividade' as character varying(20)) as ramo, abm01tipo as tipo, ent.abe01codigo as codEntidade, ent.abe01na as naEntidade, "+
										"abd01es as oper, abb10tipocod as tipoOper, abb01data as dataEmiss, desp.abe01codigo as codDespacho, desp.abe01na as naDesp, abm01codigo as codItem, abm01na as naItem "+
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
										(numDocIni != null && numDocFin != null ? "where abb01num between :numDocIni and :numDocFin " : "" ) +
										(dataEmissIni != null && dataEmissFin != null ?"and abb01data between :dataEmissIni and :dataEmissFin " : "" )+
										(codDesp != null ? "and desp.abe01codigo in (:codDesp) " : "")+
										"and abd01es = 1 and abd01aplic = 1 " +
										"and abm01tipo = 1 " );
								
		if(numDocIni != null && numDocFin != null ){
			sql.setParameter("numDocIni", numDocIni);
			sql.setParameter("numDocFin", numDocFin);
		}
		if(dataEmissIni != null && dataEmissFin != null ){
			sql.setParameter("dataEmissIni", dataEmissIni);
			sql.setParameter("dataEmissFin", dataEmissFin);
		}
		if(codDesp != null){
			sql.setParameter("codDesp", codDesp);
		}

		List<TableMap> dadosSQL = sql.getListTableMap();

		throw new ValidacaoException(dadosSQL.toString())
		return dadosSQL;
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLUZhdHVyYW1lbnRvIiwidGlwbyI6InJlbGF0b3JpbyJ9