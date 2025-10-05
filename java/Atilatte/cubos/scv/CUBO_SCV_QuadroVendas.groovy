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

public class CUBO_SCV_QuadroVendas extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - QuadroVendas"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		LocalDate dataIni = getLocalDate("dataInicial");
          LocalDate dataFin = getLocalDate("dataFinal");
		Long idNumeroIni = getLong("numeroInicial");
		Long idNumeroFin = getLong("numeroFinal");
//throw new ValidacaoException(dataIni.toString());
		List<TableMap> dadosRelatorio = buscarDadosRelatorio(dataIni,dataFin,idNumeroIni,idNumeroFin);
		gerarXLSX("QuadroVendas", dadosRelatorio)
	}

	private List<TableMap> buscarDadosRelatorio(LocalDate dataIni,LocalDate dataFin,Long idNumeroIni, Long idNumeroFin){
		

//		String operDocumento = "";
//		String tipoOper = "";
		
		

		   Query sql = getSession().createQuery("select abb01num as numDoc, eaa0103qtUso as qtUso, eaa0103unit as unitario, cast(eaa0103json ->> 'icms' as numeric(16,2)) as ICMS, "+
										"cast(eaa0103json ->> 'ipi' as numeric(16,2)) as IPI, cast(eaa0103json ->> 'icms_st' as numeric(16,2)) as icmsST, eaa0103total as Total, cast(eaa0103json ->> 'desconto' as numeric(16,2)) as Desconto, "+
										"eaa0103totDoc as totalDoc, eaa0103qtComl as qtComl, abd01codigo as PCD,cast(abe01camposcustom ->> 'ramo_atividade' as character varying(40)) as  ramoAtividade, abm01tipo as MPS, "+
										"ent.abe01codigo as codigoEnt, ent.abe01na naEntidade, abd01es as operacao, abb10tipocod as tipoOperacao, To_CHAR(abb01data, 'dd/mm/yyyy') as dataEmissao, TO_CHAR(eaa01esdata, 'dd/mm/yyyy') as dataEntradaSaida, "+
										"abm01codigo as codigoItem, abm01na as item "+
										"from eaa01 "+
										"inner join abb01 on abb01id = eaa01central "+
										"inner join abe01 as ent on ent.abe01id = abb01ent "+
										"inner join eaa0103 on eaa0103doc = eaa01id "+
										"inner join abd01 on abd01id = eaa01pcd "+
										"inner join abm01 on abm01id = eaa0103item "+ 
										"inner join abb10 on abb10id = abd01opercod "+
										getSamWhere().getWherePadrao("WHERE", Eaa01.class) +
										(dataInicial != null && dataFinal != null ? "and abb01data between :dataInicial and :dataFinal " : "" ) +
										(idNumeroIni != null && idNumeroFin != null ? "and abb01num between :idNumeroIni and :idNumeroFin " : "") +
										"and abm01tipo = 1 "+
										"and eaa01cancdata is null");
										
		if(dataIni != null && dataFin != null  ){
			sql.setParameter("dataInicial", dataIni);
			sql.setParameter("dataFinal",dataFin);
		}
		
		if(idNumeroIni != null && idNumeroIni != null){
			sql.setParameter("idNumeroIni",idNumeroIni);
			sql.setParameter("idNumeroFin", idNumeroFin);
			
		}
		
		List<TableMap> lancamento = sql.getListTableMap();

		return lancamento;
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBRdWFkcm9WZW5kYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=