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


public class CUBOS_SCV_VendasPorCliente extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Vendas Por Cliente"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("operacao",0,"tipoOperacao",0)
	}
	@Override 
	public DadosParaDownload executar() {
		Integer operacao = getInteger("operacao");
		Integer tipoOperacao = getInteger("tipoOperacao");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Long idEntidade = getLong("entidade");

		List<TableMap> dadosRelatorio = buscarDadosRelatorio(operacao, tipoOperacao,dataEmissao,idEntidade);
		gerarXLSX("VendasPorCliente", dadosRelatorio)
	}

	private List<TableMap> buscarDadosRelatorio(Integer operacao, Integer tipoOperacao, LocalDate[] dataEmissao,List<Long> idEntidade){
		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;

		String operDocumento = "";
		String tipoOper = "";
		
		if(dataEmissao != null ){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1];
		}

		if(operacao == 0){
			operDocumento = "AND abd01aplic = 1 and abd01es = 0 "
		}else if(operacao == 1){
			operDocumento = "AND abd01aplic = 1 and abd01es = 1 "
		}else if(operacao == 2 ){
			operDocumento = "AND abd01aplic = 0 and abd01es = 0 "
		}else{
			operDocumento = "AND abd01aplic = 0 and abd01es = 1 "
		}


		
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
										(dataEmissaoIni != null && dataEmissaoFin != null ? "and abb01data between :dataEmissaoIni and :dataEmissaoFin " : "" )+
										"and abm01tipo = 1 "+
										(idEntidade != null ? "and abe01id between in (:idEntidadeIni) " : "" )+
										operDocumento +
										"and abb10tipocod = :tipoOperacao " +
										"and eaa01cancdata is null");
		if(dataEmissaoIni != null && dataEmissaoFin != null){
			sql.setParameter("dataEmissaoIni", dataEmissaoIni);
			sql.setParameter("dataEmissaoFin",dataEmissaoFin);
		}
		if(idEntidade != null ){
			sql.setParameter("idEntidade",idEntidadeIni);
			
		}

		if(tipoOper != null){
			sql.setParameter("tipoOperacao",tipoOperacao)
		}
		return sql.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBWZW5kYXMgUG9yIENsaWVudGUiLCJ0aXBvIjoicmVsYXRvcmlvIn0=