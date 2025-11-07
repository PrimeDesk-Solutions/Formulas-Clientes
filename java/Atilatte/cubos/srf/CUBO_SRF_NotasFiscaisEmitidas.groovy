package Atilatte.cubos.srf;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import sam.core.variaveis.MDate
import br.com.multitec.utils.DateUtils

import sam.model.entities.aa.Aac10;

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

public class CUBO_SRF_NotasFiscaisEmitidas extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SRF - Notas Fiscais Emitidas"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros(
			"movimentacao","0",
			"atendido",0,
			"cancelados", 1,
		);
	}
	@Override 
	public DadosParaDownload executar() {
		List<TableMap> dados = dadosRelatorio();
		Integer atendido = getInteger("atendido");
		Boolean resumo = getBoolean("resumo");
		
		if(resumo) return gerarXLSX("CUBO_SRF_NotasFiscaisEmitidas_SIF",dados);
		return gerarXLSX("CUBO_SRF_NotasFiscaisEmitidas",dados);
		
	}

	private List<TableMap>dadosRelatorio(){
		
		Integer movimentacao = getInteger("movimentacao");
		Integer cancelado = getInteger("cancelados");
		LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		LocalDate[] dtEntrega = getIntervaloDatas("dataEntrega");
		LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao")
		List<Long> entidade = getListLong("entidade");
		List<Long> pcd = getListLong("pcd");
		List<Long> tipoDoc = getListLong("tipoDoc");
		List<Long> tipoOper = getListLong("tipoOperacao");
		List<Long> item = getListLong("itens");
		Aac10 empresa = obterEmpresaAtiva();
		Long idEmpresa = empresa.aac10id;
		LocalDate dtEntradaSaidaIni = null;
		LocalDate dtEntradaSaidaFin = null;
		LocalDate dtEntregaIni = null;
		LocalDate dtEntregaFin = null;
		LocalDate dtEmissaoIni = null;
		LocalDate dtEmissaoFin = null;
		

		String pedAtend = "";
		String operDocumento = "";

          

	  	if(dtEntradaSaida != null){
			dtEntradaSaidaIni = dtEntradaSaida[0];
			dtEntradaSaidaFin = dtEntradaSaida[1];
	  	}
	  
	  	if(dtEntrega != null){
			dtEntregaIni = dtEntrega[0];
			dtEntregaFin = dtEntrega[1];
	  	}
	  
	 	if(dtEmissao != null){
			dtEmissaoIni = dtEmissao[0];
			dtEmissaoFin = dtEmissao[1];
	  	}

	  	String wereCancelado = cancelado == 0 ? "and eaa01nota.eaa01cancdata is null " : cancelado == 1 ? "and eaa01nota.eaa01cancdata is not null " : ""
		String whereMovimentacao = movimentacao == 0 ? "and eaa01nota.eaa01esMov = 0 " : movimentacao == 1 ? "and eaa01nota.eaa01esMov = 1 " : "and eaa01nota.eaa01esmov in (0,1) ";


		 Query sql = getSession().createQuery("select eaa01nota.eaa01esmov as movimentacao, case when eaa01nota.eaa01esmov = 0 then 'Entrada' else 'Saída' end as descrMov, abb10nota.abb10codigo as codOper, abb10nota.abb10descr as descrOper, cast(eaa0103nota.eaa0103json ->> 'qt_original' as numeric(16,2)) as qtdOriginal, eaa0103nota.eaa0103qtComl as sce, eaa0103nota.eaa0103unit as unit, cast(eaa0103nota.eaa0103json ->> 'icms' as numeric(16,2)) as icms, cast(eaa0103nota.eaa0103json ->> 'icms_st' as numeric(16,2)) as icms_st, cast(eaa0103nota.eaa0103json ->> 'frete_item' as numeric(16,2)) as frete, cast(eaa0103nota.eaa0103json ->> 'frete_dest' as numeric(16,2)) as freteDest, eaa0103nota.eaa0103total as totItem,  "+
									"eaa0103nota.eaa0103totdoc as totDoc, cast(eaa0103nota.eaa0103json ->> 'peso_liquido' as numeric(16,2)) as pesoliquido,  "+
									"cast(eaa0103nota.eaa0103json ->> 'peso_bruto' as numeric(16,2)) as pesoBruto, abd01nota.abd01codigo as PCD, abd01nota.abd01descr as descrPcd,eaa0103nota.eaa0103totDoc as totDoc, ent.abe01codigo as codCliente,  "+
									"ent.abe01na as naCliente, abm01codigo as codItem,abm01na as descrItem,abd01nota.abd01es as oper, abb10tipocod as tipoOper,  "+
									"abb01nota.abb01data as dataEmissao,TO_CHAR(abb01nota.abb01data,'YYYY/MM') as periodo, eaa0103nota.eaa0103dtentrega as dataEntrega,  "+
									"eaa01nota.eaa01esdata as dataentradaSaida, abb01nota.abb01num as numDoc, aah01nota.aah01codigo as tipoDocOrig,  "+
									"aah01nota.aah01nome as descrTipoDocOrig, desp.abe01codigo as codigoTranspOrig, desp.abe01na naTranspOrig,  "+
									"redesp.abe01codigo as codigoRedespOrig, redesp.abe01na as naRedespOrig, rep0.abe01codigo as codRep0Orig,  "+
									"rep0.abe01na as naRep0Orig, rep1.abe01codigo as codRep1Orig, rep1.abe01na as naRep1Orig,  "+
									"rep2.abe01codigo as codRep2Orig, rep2.abe01na as naRep2Orig, rep3.abe01codigo as codRep3Orig,  "+
									"rep3.abe01na as naRep3Orig, rep4.abe01codigo as codRep4Orig, rep4.abe01na as naRep4Orig,  "+
									"aba3001descr as rede,(abe02json ->> 'primeira_venda')::date as primeiraVenda,  "+
									"(abe02json ->> 'ultima_venda')::date as ultimaVenda, aag0201nome as cidade, aag02uf as estado, aae11descr as motivo,  "+
									"eaa01nota.eaa01cancdata as cancDataOrig,aam06codigo as umv,cast(eaa0103nota.eaa0103json ->> 'qt_convertida' as numeric(18,6)) as qtConvertida, eaa01esdata as esData, "+
									"cast(eaa0103nota.eaa0103json ->> 'vlr_icms_fcp_' as numeric(18,6)) as fcp, " +	
									"cast(eaa0103nota.eaa0103json ->> 'volumes' as numeric(18,6)) as volumes, cast(eaa0103nota.eaa0103json ->> 'm3' as numeric(18,6)) as m3 " +
									"from eaa01 as eaa01nota   "+
									"inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id   "+
									"inner join abb01 as abb01nota on abb01nota.abb01id = eaa01nota.eaa01central   "+
									"inner join abd01 as abd01nota on abd01nota.abd01id = eaa01nota.eaa01pcd   "+
									"inner join abe01 ent on ent.abe01id = abb01nota.abb01ent   "+
									"inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1   "+
									"inner join aag0201 on aag0201id = abe0101municipio   "+
									"inner join aag02 on aag02id = aag0201uf   "+
									"inner join abe02 on abe02ent = ent.abe01id   "+
									"inner join abm01 as abm01nota on abm01nota.abm01id = eaa0103nota.eaa0103item   "+
									"inner join aam06 on aam06id = abm01umu  "+
									"inner join abb10 as abb10nota on abb10nota.abb10id = abd01nota.abd01opercod  "+
									"inner join aah01 as aah01nota on aah01nota.aah01id = abd01nota.abd01tipo   "+
									"inner join eaa0102 as eaa0102nota on eaa0102nota.eaa0102doc = eaa01nota.eaa01id   "+
									"left join abe01 as desp on desp.abe01id = eaa0102nota.eaa0102despacho   "+
									"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho   "+
									"left join abe01 rep0 on rep0.abe01id = eaa01nota.eaa01rep0   "+
									"left join abe01 rep1 on rep1.abe01id = eaa01nota.eaa01rep1   "+
									"left join abe01 rep2 on rep2.abe01id = eaa01nota.eaa01rep2   "+
									"left join abe01 rep3 on rep3.abe01id = eaa01nota.eaa01rep3   "+
									"left join abe01 rep4 on rep4.abe01id = eaa01nota.eaa01rep4   "+
									"left join abe0103 on abe0103ent = ent.abe01id   "+
									"left join aba3001 on aba3001id = abe0103criterio and aba3001criterio = 18145240  "+
									"left join aae11 on aae11id = eaa01nota.eaa01cancmotivo  "+
									"where eaa01nota.eaa01clasDoc = 1 "+
									(dtEmissaoIni != null && dtEmissaoFin != null ? "AND abb01nota.abb01data between :dtEmissaoIni  and :dtEmissaoFin " : "")+
									(dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01nota.eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin ": "")+									
									(entidade != null ? "and ent.abe01id in (:entidade) ": "")+									
									(dtEntregaIni != null && dtEntregaFin != null ? "and eaa0103nota.eaa0103dtentrega between :dtEntregaIni and :dtEntregaFin " : "")+									
									(pcd != null ? "and abd01nota.abd01id in (:pcd) " : "")+									
									(item != null ? "and abm01nota.abm01id in (:item) " : "")+	
									(tipoOper != null && tipoOper.size() > 0 ? "and abb10nota.abb10id in (:tipoOper) " : "")+	
									(tipoDoc != null && tipoDoc.size() > 0 ? "and aah01nota.aah01id in (:tipoDoc) " : "")+	
									wereCancelado +
									whereMovimentacao +						
									"and eaa01nota.eaa01gc = :idEmpresa "+
									"order by abb01nota.abb01num, abm01nota.abm01codigo ");

		
		if(dtEmissaoIni != null && dtEmissaoFin!= null){
			sql.setParameter("dtEmissaoIni",dtEmissaoIni);
			sql.setParameter("dtEmissaoFin",dtEmissaoFin);
		}

		if(dtEntradaSaidaIni != null && dtEntradaSaidaFin!= null){
			sql.setParameter("dtEntradaSaidaIni",dtEntradaSaidaIni);
			sql.setParameter("dtEntradaSaidaFin",dtEntradaSaidaFin);
		}

		if(entidade != null && entidade.size() > 0){
			sql.setParameter("entidade",entidade);
		}

		if(dtEntregaIni != null && dtEntregaFin!= null){
			sql.setParameter("dtEntregaIni",dtEntregaIni);
			sql.setParameter("dtEntregaFin",dtEntregaFin);
		}

		if(pcd != null && pcd.size() > 0){
			sql.setParameter("pcd",pcd);
		}

		if(item != null && item.size() > 0 ){
			sql.setParameter("item",item);
		}

		if(movimentacao != null && movimentacao!= null){
			sql.setParameter("movimentacao",movimentacao);
		}

		if(tipoOper != null && tipoOper!= null){
			sql.setParameter("tipoOper",tipoOper);
		}

		if(tipoDoc != null && tipoDoc.size() > 0){
			sql.setParameter("tipoDoc",tipoDoc);							
		}
		sql.setParameter("idEmpresa",idEmpresa);
		
		return sql.getListTableMap();
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBOb3RhcyBGaXNjYWlzIEVtaXRpZGFzIiwidGlwbyI6InJlbGF0b3JpbyJ9