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

public class CUBO_SCV_PedidoDeVendas extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Pedido De Vendas"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais(){
		return criarFiltros("operacao",0,"tipoOperacao",0,"situacaoDoc",0)
	}
	@Override 
	public DadosParaDownload executar() {
		List<Long> idEnt = getListLong("entidade");
		Integer operacao = getInteger("operacao");
		Integer tipoOperacao = getInteger("tipoOperacao");
		Integer situacaoDoc = getInteger("situacaoDoc");
		List<Long> idPcd= getListLong("pcd");
		List<Long> tipoDoc = getLong("tipoDoc");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		LocalDate[] dataEntrega = getIntervaloDatas("dataEntrega");
		
		 
		
		List<TableMap> dadosRelatorio = buscarDocumentos(idEnt,operacao,tipoOperacao,situacaoDoc,idPcd,tipoDoc,dataEmissao,dataEntradaSaida,dataEntrega);
		
		gerarXLSX("pedidodevendas", dadosRelatorio)
		
	}
	private List<TableMap>buscarDocumentos(List<Long> idEnt, Integer operacao, Integer tipoOperacao, Integer situacaoDoc, List<Long> idPcd, List<Long> tipoDoc, LocalDate[] dataEmissao, LocalDate[] dataEntradaSaida, LocalDate[] dataEntrega ){
		
		String operDocumento = "";
		String tipoOper = "";
		String pedAtend = "";
		
		LocalDate dataEmissIni = null;
		LocalDate dataEmissFin = null;

		LocalDate dataEntradaSaidaIni = null;
		LocalDate dataEntradaSaidaFin = null;


		LocalDate dataEntregaIni = null;
		LocalDate dataEntregaFin = null;
		
		if(dataEmissao != null){
			dataEmissIni = dataEmissao[0];
			dataEmissFin = dataEmissao[1];
		}

		if(dataEntradaSaida != null){
			dataEntradaSaidaIni = dataEntradaSaida[0];
			dataEntradaSaidaFin = dataEntradaSaida[1];
		}

		if(dataEntrega != null){
			dataEntregaIni = dataEntrega[0];
			dataEntregaFin = dataEntrega[1];
			
		}

		if(operacao == 0){
			operDocumento = "AND abd01aplic = 1 and abd01es = 0 ";
		}else if(operacao == 1){
			operDocumento = "AND abd01aplic = 1 and abd01es = 1 ";
		}else if(operacao == 2 ){
			operDocumento = "AND abd01aplic = 0 and abd01es = 0 ";
		}else{
			operDocumento = "AND abd01aplic = 0 and abd01es = 1 ";
		}

		if(tipoOperacao == 99){
			tipoOper = "AND abb10tipocod in (0,1,2,3,4,5,6,7) "
		}else{
			tipoOper = "AND abb10tipocod = :tipoOperacao "
		}

		if(tipoOperacao == 2 ){
			if(situacaoDoc == 0){
				pedAtend = "and eaa01scvatend = '0' "
			}else if(situacaoDoc == 1){
				pedAtend = "and eaa01scvatend = '1' "
			}else{
				pedAtend = "and eaa01scvatend = '2' "
			}
		}

		
		
		
		Query sql = getSession().createQuery("select eaa0103qtUso as qtdUso,eaa0103unit as unit,cast(eaa0103json ->> 'qt_original' as numeric(18,6)) as qtOriginal, "+
											"eaa0103total as totItem, eaa0103totDoc, cast(eaa0103json ->> 'peso_liquido' as numeric(18,6)) as pesoLiq, "+
											"abd01codigo as PCD,ent.abe01codigo as codCliente,ent.abe01na as nomeEntidade, "+
											"abm01codigo as codItem, abm01descr as descrItem, abd01es as oper, abb10tipoCod as tipoOper, "+
											"abb01data as dataEmissao, TO_CHAR(abb01data,'YYYY/MM') as periodo, "+
											"eaa0103dtentrega as dataEntrega,eaa01esdata as dtEntradaSaida,abb01num as numero, "+
											"aah01codigo as tipoDoc, transp.abe01codigo as codTransp, transp.abe01na as nomeTransp, "+
											"redesp.abe01codigo as codRedespacho, redesp.abe01na as naRedesp "+
											"from eaa01 "+
											"inner join eaa0103 on eaa0103doc = eaa01id "+
											"inner join abd01 on abd01id = eaa01pcd "+
											"inner join abb01 on abb01id = eaa01central "+
											"inner join abe01 as ent on ent.abe01id = abb01ent "+
											"inner join abm01 on abm01id = eaa0103item "+
											"inner join abb10 on abb10id = abd01operCod "+
											"inner join aah01 on aah01id = abd01tipo "+
											"inner join eaa0102 on eaa0102doc = eaa01id "+
											"left  join abe01 as transp on transp.abe01id = eaa0102despacho "+
											"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
											getSamWhere().getWherePadrao("WHERE", Eaa01.class) +
											(dataEmissIni != null && dataEmissFin != null ? "and abb01data between :dataEmissIni and :dataEmissFin " : "" ) +
											(dataEntradaSaidaIni != null && dataEntradaSaidaFin != null ? "and eaa01esdata between :dataEntradaSaidaIni and :dataEntradaSaidaFin " : "" ) +
											(idEnt != null ? "and ent.abe01id in (:idEnt) " : "" ) +
											(dataEntregaIni != null && dataEntregaFin != null ? "and eaa0103dtentrega between :dataEntregaIni and :dataEntregaFin " : "" ) +
											(tipoDoc != null ? "and aah01id in (:tipoDoc) " : "" ) +
											(idPcd != null ? "and abd01id in (:idPcd) " : "") +
											"and eaa01scvatend = :situacaoDoc "+
											operDocumento +
											tipoOper+
											pedAtend+
											"and eaa01cancdata is null");
		if(dataEmissIni != null && dataEmissFin != null ){
			sql.setParameter("dataEmissIni", dataEmissIni);
			sql.setParameter("dataEmissFin", dataEmissFin);
		}

		if(dataEntradaSaidaIni != null && dataEntradaSaidaFin != null ){
			sql.setParameter("dataEntradaSaidaIni",dataEntradaSaidaIni);
			sql.setParameter("dataEntradaSaidaFin", dataEntradaSaidaFin);
		}

		if(idEnt != null){
			sql.setParameter("idEnt",idEnt);
		}
		
		if(dataEntregaIni != null && dataEntregaFin != null ){
			sql.setParameter("dataEntregaIni",dataEntregaIni);
			sql.setParameter("dataEntregaFin", dataEntregaFin);
		}

		if(tipoDoc != null ){
			sql.setParameter("tipoDoc",tipoDoc);
		}

		if(idPcd != null){
			sql.setParameter("idPcd",idPcd);
		}

		if(situacaoDoc != null ){
			sql.setParameter("situacaoDoc",situacaoDoc);
		}

		if(tipoOperacao != null){
			sql.setParameter("tipoOperacao",tipoOperacao)
		}

		List<TableMap> tmPedidos = sql.getListTableMap();
		List<TableMap> registros = new ArrayList();

		for(TableMap pedido : tmPedidos){
			if(situacaoDoc == 0){
				pedido.put("situacao","Não Atendido");
			}else{
				pedido.put("situacao","Atendido");
			}
			registros.add(pedido);
		}

		return registros;
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBQZWRpZG8gRGUgVmVuZGFzIiwidGlwbyI6InJlbGF0b3JpbyJ9