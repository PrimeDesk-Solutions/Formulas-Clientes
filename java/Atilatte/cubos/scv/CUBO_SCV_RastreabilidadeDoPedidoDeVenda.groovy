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

public class CUBO_SCV_RastreabilidadeDoPedidoDeVenda extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Rastreabilidade do Pedido de Venda"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("busca", 0,
						"numeroPedInicial",1,
						"numeroPedFinal",999999999,
						"numeroNotaInicial", 1,
						"numeroNotaFinal", 999999999,
						"numeroDocFinancInicial",1,
						"numeroDocFinancFinal",999999999,
						"opcoesRecebimento",0)
	}
	@Override 
	public DadosParaDownload executar() {
		List<TableMap> dados = buscarDadosRelatorio();

		gerarXLSX("RastreabilidadeDoPedidoDeVenda",dados);
	}

	private List<TableMap> buscarDadosRelatorio(){
		List<Long> entidades = getListLong("entidade");
		Integer atendimento = getInteger("busca");

		//Busca Informações Filtro Pedido
		List<Long> tipoDocPed = getListLong("tipoDocPed");
		Integer numeroPedInicial = getInteger("numeroPedInicial");
		Integer numeroPedFinal = getInteger("numeroPedFinal");
		LocalDate[] dataEmissaoPed = getIntervaloDatas("dataEmissaoPed");

		//Busca Informações Filtro Nota
		List<Long> tipoDocNota = getListLong("tipoDocNota");
		Integer numeroNotaInicial = getInteger("numeroNotaInicial");
		Integer numeroNotaFinal = getInteger("numeroNotaFinal");
		LocalDate[] dataEmissaoNota = getIntervaloDatas("dataEmissaoNota");

		//Busca Informções Filtro Financeiro
		List<Long> tipoDocFinanc = getListLong("tipoDocFinanc");
		Integer numeroDocFinancInicial = getInteger("numeroDocFinancInicial");
		Integer numeroDocFinancFinal = getInteger("numeroDocFinancFinal");
		LocalDate[] dataEmissaoFinanc = getIntervaloDatas("dataEmissaoFinanc");		
		LocalDate[] dataVencimento = getIntervaloDatas("dataVencimento");
		LocalDate[] dataQuitacao = getIntervaloDatas("dataQuitacao");
		String portador = getString("portador");
		String operacao = getString("operacao");
		Integer opcoesRecebimento = getInteger("opcoesRecebimento");

		String port = portador != null ? "and abf15codigo = :portador " : "";
		String oper = operacao != null ? "and abf16codigo = :operacao " : "";
		String dataRec = "";
		String dataarec = "";
		String pedido = "";
		String nota = "";
		String financeiro = "";
		Query sql = null;
		
		//Separa o que foi recebido ou não
		if(busca == 2 && opcoesRecebimento == 1 ){
			dataRec = "and daa01dtpgto between :dtQuitacaoIni and :dtQuitacaoFin "
		}else{
			dataRec = "";
		}
		
		if(busca == 2 && opcoesRecebimento == 2 ){
			dataarec = "and daa01dtpgto is null ";
		}else{
			dataarec = "";
		}

		//Condição para buscar por Pedido
		if(busca == 0){
			//Datas Emissão Pedido
			LocalDate dataEmissaoPedIni = null;
			LocalDate dataEmissaoPedFin = null;

			if(dataEmissaoPed != null ){
				dataEmissaoPedIni = dataEmissaoPed[0];
				dataEmissaoPedFin = dataEmissaoPed[1];
			}
			sql = getSession().createQuery("select abe01codigo as codCliente, abe01na as naCliente, tipoDocPed.aah01codigo as tipoDocPed, pcdPed.abd01codigo as codigoPcdPedido, pcdPed.abd01descr as descrPcdPedido, centralPed.abb01num as numPedido, centralPed.abb01data as dataEmissPedido, ped.eaa01totdoc as totalPedido, "+
											"tipoDocNota.aah01codigo as tipoDocNota, centralNota.abb01num as numNota,  tipoDocNota.aah01serie as serieNota, centralNota.abb01data as dataEmissNota, nota.eaa01cancdata as cancDataNota, nota.eaa01cancobs as obsCancNota, nota.eaa01totdoc as totalDocNota, pcdNota.abd01codigo as codPcdNota, "+
											"pcdNota.abd01descr as descrPcdNota, tipoDocFinanc.aah01codigo as tipoDocFinanc, centralFinanc.abb01num as numDocFinanc, daa01dtVctoR as dataVencReal, daa01dtPgto as dataPagamento, daa01valor as valorFinanceiro, centralFinanc.abb01data as dtEmissaoFinanc "+
											"from eaa01 ped "+
											"inner join abd01 pcdPed on pcdPed.abd01id = ped.eaa01pcd "+
											"inner join abb10 on abb10id = pcdPed.abd01operCod "+ 
											"left join eaa0103 itemPed on itemPed.eaa0103doc = ped.eaa01id "+
											"left join eaa01032 on eaa01032itemscv = itemPed.eaa0103id "+
											"left join abb01 centralPed on centralPed.abb01id = ped.eaa01central "+
											"inner join abe01 on abe01id = centralPed.abb01ent "+
											"inner join aah01 tipoDocPed on tipoDocPed.aah01id = centralPed.abb01tipo "+
											"left join eaa0103 itemNota on itemNota.eaa0103id = eaa01032itemsrf "+
											"left join eaa01 nota on nota.eaa01id = itemNota.eaa0103doc "+
											"left join abb01 centralNota on centralNota.abb01id = nota.eaa01central "+
											"left join abd01 pcdNota on pcdNota.abd01id = nota.eaa01pcd "+
											"left join aah01 tipoDocNota on tipoDocNota.aah01id = centralNota.abb01tipo "+
											"left join eaa0113 on eaa0113doc = case when nota is null then ped.eaa01id else nota.eaa01id end "+
											"left join abb0102 on abb0102central = centralNota.abb01id "+
											"left join daa01 on daa01central = abb0102doc "+
											"left join abb01 centralFinanc on centralFinanc.abb01id = daa01central "+
											"left join aah01 tipoDocFinanc on tipoDocFinanc.aah01id = centralFinanc.abb01tipo "+
											"where pcdPed.abd01es = 1 and pcdPed.abd01aplic = 0 "+
											"and abb10tipoCod = 2 "+
											(entidades != null ? "and abe01id in (:entidades) " : "") +
											(tipoDocPed != null ? "and tipoDocPed.aah01id in (:tipoDocPed) " : "") + 
											(numeroPedInicial != null && numeroPedFinal != null ? "and centralPed.abb01num between :numeroPedInicial and :numeroPedFinal " : "")+
											(dataEmissaoPedIni != null && dataEmissaoPedFin != null ? "and centralPed.abb01data between :dataEmissaoPedIni and :dataEmissaoPedFin " : "" ));

		
			if(entidades != null){
				sql.setParameter("entidades",entidades)
			}
			if(tipoDocPed != null){
				sql.setParameter("tipoDocPed",tipoDocPed)
			}
			if(numeroPedInicial != null &&  numeroPedFinal != null){
				sql.setParameter("numeroPedInicial",numeroPedInicial);
				sql.setParameter("numeroPedFinal",numeroPedFinal);
			}
			if(dataEmissaoPedIni != null &&  dataEmissaoPedFin != null){
				sql.setParameter("dataEmissaoPedIni",dataEmissaoPedIni);
				sql.setParameter("dataEmissaoPedFin",dataEmissaoPedFin);
			}

		
		}else{
			pedido = "";
		}

		//Condição para buscar por Notas
		if(busca == 1){
			
			//Datas Emissão Nota
			LocalDate dataEmissaoNotaIni = null;
			LocalDate dataEmissaoNotaFin = null;
			
			if(dataEmissaoNota != null){
				dataEmissaoNotaIni = dataEmissaoNota[0];
				dataEmissaoNotaFin = dataEmissaoNota[1];
			}
			sql = getSession().createQuery("select abe01na as codCliente, abe01na as naCliente, tipoDocPedido.aah01codigo as tipoDocPed, centralPedido.abb01num as numPedido, pcdPedido.abd01codigo as codigoPcdPedido, "+ 
										"pcdPedido.abd01descr as descrPcdPedido, centralPedido.abb01data as dataEmissPedido, pedido.eaa01totDoc as totalPedido, tipoDocNota.aah01codigo as tipoDocNota, centralNota.abb01num as numNota, centralNota.abb01serie as serieNota, "+ 
										"centralNota.abb01data as dataEmissNota, nota.eaa01totDoc as totalDocNota, nota.eaa01cancdata as cancDataNota, nota.eaa01cancobs as obsCancNota, pcdNota.abd01codigo as codPcdNota, "+ 
										"pcdNota.abd01descr as descrPcdNota, tipoDocFinanc.aah01codigo as tipoDocFinanc, centralFinanc.abb01num as numDocFinanc, daa01dtVctoR as dataVencReal, daa01dtPgto as dataPagamento, daa01valor as valorFinanceiro, centralFinanc.abb01data as dtEmissaoFinanc "+ 
										"from eaa01 nota "+ 
										"inner join abd01 pcdNota on pcdNota.abd01id = nota.eaa01pcd "+ 
										"inner join abb10 on abb10id = pcdNota.abd01operCod "+  
										"left join eaa0103 itemNota on itemNota.eaa0103doc = nota.eaa01id "+ 
										"left join eaa01032 on eaa01032itemsrf = itemNota.eaa0103id "+ 
										"left join abb01 centralNota on centralNota.abb01id = nota.eaa01central "+ 
										"inner join abe01 on abe01id = centralNota.abb01ent "+ 
										"inner join aah01 tipoDocNota on tipoDocNota.aah01id = centralNota.abb01tipo "+ 
										"left join eaa0103 itemPedido on itemPedido.eaa0103id = eaa01032itemscv "+ 
										"left join eaa01 pedido on pedido.eaa01id = itemPedido.eaa0103doc "+ 
										"left join abb01 centralPedido on centralPedido.abb01id = pedido.eaa01central "+ 
										"left join abd01 pcdPedido on pcdPedido.abd01id = pedido.eaa01pcd "+ 
										"left join aah01 tipoDocPedido on tipoDocPedido.aah01id = centralPedido.abb01tipo "+ 
										"left join eaa0113 on eaa0113doc = case when nota is null then pedido.eaa01id else nota.eaa01id end "+ 
										"left join abb0102 on abb0102central = centralNota.abb01id "+ 
										"left join daa01 on daa01central = abb0102doc "+ 
										"left join abb01 centralFinanc on centralFinanc.abb01id = daa01central "+ 
										"left join aah01 tipoDocFinanc on tipoDocFinanc.aah01id = centralFinanc.abb01tipo "+ 
										"where pcdNota.abd01es in (0,1) and pcdNota.abd01aplic = 1 "+ 
										"and abb10tipoCod in (1,3,4,5,6,7) "+
										(entidades != null ? "and abe01id in (:entidades) " : "") +
										(tipoDocNota != null ? "and tipoDocNota.aah01id in (:tipoDocNota) " : "") + 
										(numeroNotaInicial != null && numeroNotaFinal != null ? "and centralNota.abb01num between :numeroNotaInicial and :numeroNotaFinal " : "")+
										(dataEmissaoNotaIni != null && dataEmissaoNotaFin != null ? "and centralNota.abb01data between :dataEmissaoNotaIni and :dataEmissaoNotaFin " : "" ));

			if(entidades != null){
				sql.setParameter("entidades",entidades)
			}
			if(tipoDocNota != null){
				sql.setParameter("tipoDocNota",tipoDocNota)
			}
			if(numeroNotaInicial != null &&  numeroNotaFinal != null){
				sql.setParameter("numeroNotaInicial",numeroNotaInicial);
				sql.setParameter("numeroNotaFinal",numeroNotaFinal);
			}
			if(dataEmissaoNotaIni != null &&  dataEmissaoNotaFin != null){
				sql.setParameter("dataEmissaoNotaIni",dataEmissaoNotaIni);
				sql.setParameter("dataEmissaoNotaFin",dataEmissaoNotaFin);
			}
				
		}else{
			nota = "";
		}

		//Condição para buscar por Financeiro
		if(busca == 2){
			
			//Datas Emissão Inicial-Final 
			LocalDate dataEmissaoFinancIni = null;
			LocalDate dataEmissaoFinancFin = null;
			
			if(dataEmissaoFinanc != null){
				dataEmissaoFinancIni = dataEmissaoFinanc[0];
				dataEmissaoFinancFin = dataEmissaoFinanc[1];
			}
			

			//Data de Quitação Inicial-Final
			LocalDate dtQuitacaoIni = null;
			LocalDate dtQuitacaoFin = null;
			
			if(dataQuitacao != null){
				dtQuitacaoIni = dataQuitacao[0];
				dtQuitacaoFin = dataQuitacao[1];
			}

			//Verifica se foi preenchido campo de date de quitação, caso selecionado Documentos Financeiros "Recebidos"
			
			if(dataRec.length() > 0 && dtQuitacaoIni == null && dtQuitacaoFin == null ){
				 throw new ValidacaoException("Necessário Informar Data de Quitação.")
			}

			//Data de Vencimento Inicial-Final
			LocalDate dataVencimentoIni = null;
			LocalDate dataVencimentoFin = null;
			
			if(dataVencimento != null){
				dataVencimentoIni = dataVencimento[0];
				dataVencimentoFin = dataVencimento[1];
			}


			
			sql = getSession().createQuery("select abe01codigo as codCliente, abe01na as naCliente, tipoDocPed.aah01codigo as tipoDocPed, centralPed.abb01num as numPedido, pcdPedido.abd01codigo as codigoPcdPedido, "+  
										"pcdPedido.abd01descr as descrPcdPedido, centralPed.abb01data as dataEmissPedido, pedido.eaa01totDoc as totalPedido, tipoDocNota.aah01codigo as tipoDocNota, centralNota.abb01num as numNota, centralNota.abb01serie as serieNota, "+  
										"centralNota.abb01data as dataEmissNota, nota.eaa01totDoc as totalDocNota, nota.eaa01cancdata as cancDataNota, nota.eaa01cancobs as obsCancNota, pcdNota.abd01codigo as codPcdNota, "+  
										"pcdNota.abd01descr as descrPcdNota, tipoDocFinanc.aah01codigo as tipoDocFinanc, centralFinanc.abb01num as numDocFinanc, daa01dtVctoR as dataVencReal, daa01dtPgto as dataPagamento, daa01valor as valor, centralFinanc.abb01data as dtEmissaoFinanc, (daa01valor + cast(daa01json ->> 'juros' as numeric(16,2)) + cast(daa01json ->> 'encargos' as numeric(16,2)) + cast(daa01json ->> 'desconto' as numeric(18,2))) as valorFinaceiro, "+
										"centralFinanc.abb01data as dtEmissaoFinanc "+
										"from daa01 "+
										"left join abb01 centralFinanc on centralFinanc.abb01id = daa01central "+
										"left join aah01 tipoDocFinanc on tipoDocFinanc.aah01id = centralFinanc.abb01tipo "+
										"left join abb0102 on abb0102doc = centralFinanc.abb01id "+
										"left join abb01 centralNota on centralNota.abb01id = abb0102central "+
										"left join aah01 tipoDocNota on tipoDocNota.aah01id = centralNota.abb01tipo "+
										"left join eaa01 nota on eaa01central = centralNota.abb01id "+
										"left join abd01 pcdNota on pcdNota.abd01id = nota.eaa01pcd "+
										"inner join abb10 tipoOperNota on tipoOperNota.abb10id = pcdNota.abd01operCod "+
										"left join eaa0113 on eaa0113doc = nota.eaa01id "+ 
										"left join abe01 on abe01id = centralNota.abb01ent "+
										"left join eaa0103 itemNota on itemNota.eaa0103doc = nota.eaa01id "+
										"left join eaa01032 on eaa01032itemsrf = itemNota.eaa0103id "+
										"left join eaa0103 itemPedido on itemPedido.eaa0103id = eaa01032itemscv "+
										"left join eaa01 pedido on pedido.eaa01id = itemPedido.eaa0103doc "+
										"left join abd01 pcdPedido on pcdPedido.abd01id = pedido.eaa01pcd "+
										"left join abb01 centralPed on centralPed.abb01id = pedido.eaa01central "+
										"left join aah01 tipoDocPed on tipoDocPed.aah01id = centralPed.abb01tipo "+
										"left join abf15 on abf15id = daa01port "+
										"left join abf16 on abf16id = daa01oper " +
										"where pcdNota.abd01es = 1 and pcdNota.abd01aplic = 1 "+
										"and tipoOperNota.abb10tipocod = 1 "+
										(entidades != null ? "and abe01id in (:entidades) " : "") +
										(tipoDocFinanc != null ? "and tipoDocFinanc.aah01id in (:tipoDocFinanc) " : "") + 
										(numeroDocFinancInicial != null && numeroDocFinancFinal != null ? "and centralFinanc.abb01num between :numeroDocFinancInicial and :numeroDocFinancFinal " : "")+
										(dataEmissaoFinancIni != null && dataEmissaoFinancFin != null ? "and centralFinanc.abb01data between :dataEmissaoFinancIni and :dataEmissaoFinancFin " : "" )+
										(dataVencimentoIni != null && dataVencimentoFin != null ? "and daa01dtVctoR between :dataVencimentoIni and :dataVencimentoFin " : "" ) +
										dataRec + dataarec + port + oper);
			if(entidades != null){
				sql.setParameter("entidades",entidades)
			}
			if(tipoDocFinanc != null){
				sql.setParameter("tipoDocFinanc",tipoDocFinanc)
			}
			if(numeroDocFinancInicial != null &&  numeroDocFinancFinal != null){
				sql.setParameter("numeroDocFinancInicial",numeroDocFinancInicial);
				sql.setParameter("numeroDocFinancFinal",numeroDocFinancFinal);
			}
			if(dataEmissaoFinancIni != null &&  dataEmissaoFinancFin != null){
				sql.setParameter("dataEmissaoFinancIni",dataEmissaoFinancIni);
				sql.setParameter("dataEmissaoFinancFin",dataEmissaoFinancFin);
			}
			if(dataVencimentoIni != null &&  dataVencimentoFin != null){
				sql.setParameter("dataVencimentoIni",dataVencimentoIni);
				sql.setParameter("dataVencimentoFin",dataVencimentoFin);
			}

			if(dtQuitacaoIni != null &&  dtQuitacaoFin != null){
				sql.setParameter("dtQuitacaoIni",dtQuitacaoIni);
				sql.setParameter("dtQuitacaoFin",dtQuitacaoFin);
			}
										
			if(portador != null){
				sql.setParameter("portador",portador);
			}
			if(operacao != null){
				sql.setParameter("operacao",operacao);
			}		
		
		}else{
			financeiro = "";
		}

		return sql.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBSYXN0cmVhYmlsaWRhZGUgZG8gUGVkaWRvIGRlIFZlbmRhIiwidGlwbyI6InJlbGF0b3JpbyJ9