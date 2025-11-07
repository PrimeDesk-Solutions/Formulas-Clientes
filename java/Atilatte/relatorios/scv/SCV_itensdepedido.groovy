package Atilatte.relatorios.scv;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDate

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import br.com.multiorm.Query;
import java.time.format.DateTimeFormatter

public class SCV_itensdepedido extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCV - Itens De Pedido";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		criarFiltros("numeroInicial","000001",
				"numeroFinal","9999999",
				"isCompra","0"
		)
	}
	@Override
	public DadosParaDownload executar() {
		Integer impressao = getInteger("impressao");
		List<TableMap> dados = buscarItensPedido();

		if(impressao == 1) return gerarXLSX("SCV_ItensDePedidos_Excel", dados);
		return gerarPDF("SCV_ItensDePedido_PDF",dados);
	}

	private List<TableMap> buscarItensPedido(){
		Integer isCompra = getInteger("isCompra");
		List<Long> tipoDoc = getListLong("tipo");
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("data");
		List<Integer> mps = getListInteger("mpms");
		List<Long> idsItens = getListLong("item");
		List<Long> idsEntidades = getListLong("entidade");
		List<Long> idsPcd = getListLong("pcd");

		Boolean totalizar1 = getBoolean("total1");
		Boolean totalizar2 = getBoolean("total2");
		Boolean totalizar3 = getBoolean("total3");
		Boolean totalizar4 = getBoolean("total4");

		String campoLivre1 = getString("campoLivre1");
		String campoLivre2 = getString("campoLivre2");
		String campoLivre3 = getString("campoLivre3");
		String campoLivre4 = getString("campoLivre4");

		Params.put("empresa",obterEmpresaAtiva().aac10codigo +"-"+obterEmpresaAtiva().aac10na );
		if(dataEmissao != null){
			params.put("periodo", "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}




		def atendimento = [get("naoAtend") ? 0 : null,get("parcialAtend") ? 1 : null, get("totalAtend") ? 2 : null]

		if(campoLivre1 != null && (campoLivre1 == campoLivre2 || campoLivre1 == campoLivre3 || campoLivre1 == campoLivre4)){
			interromper("Não é permitido inserir o mesmo Campo Livre em mais de um campo")
		}
		if(campoLivre2 != null && (campoLivre2 == campoLivre1 || campoLivre2 == campoLivre3 || campoLivre2 == campoLivre4)){
			interromper("Não é permitido inserir o mesmo Campo Livre em mais de um campo")
		}
		if(campoLivre3 != null && (campoLivre3 == campoLivre1 || campoLivre3 == campoLivre2 || campoLivre3 == campoLivre4)){
			interromper("Não é permitido inserir o mesmo Campo Livre em mais de um campo")
		}
		if(campoLivre4 != null && (campoLivre4 == campoLivre1 || campoLivre4 == campoLivre2 || campoLivre4 == campoLivre3)){
			interromper("Não é permitido inserir o mesmo Campo Livre em mais de um campo")
		}

		//Data Emissão Inicial - Final
		LocalDate dtEmissaoIni = null;
		LocalDate dtEmissaoFin = null;

		if(dataEmissao != null){
			dtEmissaoIni = dataEmissao[0];
			dtEmissaoFin = dataEmissao[1];
		}

		Query sql = getSession().createQuery("select case when abm01tipo = 0 then 'M' " +
											"when abm01tipo = 1 then 'P' "+
											"when abm01tipo = 2 then 'MER' "+
											"else 'S' end as MPS, abm01codigo as codItem, abm01na as naItem, cast(eaa0103json ->> 'umv' as character varying(3)) as UMV, "+
											"aah01codigo as tipoDoc, abb01num as numDoc, abb01data as dataEmissao, "+
											"abe01codigo as codEntidade,abe01na as naEnt, aag02uf as uf, eaa0103dtentrega as dtEntrega, "+
											"eaa0103pcnum as pedCliente, "+
											"eaa0103qtcoml, eaa0103totdoc, eaa0103qtuso as sce,cast(eaa0103json ->> 'caixa' as numeric(18,6)) as caixa "+
											"from eaa01 "+
											"inner join abb01 on abb01id = eaa01central "+
											"inner join abd01 on abd01id = eaa01pcd "+
											"inner join eaa0103 on eaa0103doc = eaa01id "+
											"inner join abm01 on abm01id = eaa0103item "+
											"inner join aah01 on aah01id = abb01tipo "+
											"inner join abe01 on abe01id = abb01ent "+
											"left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
											"left join aag0201 on aag0201id = abe0101municipio "+
											"left join aag02 on aag02id = aag0201uf "+
											"where abd01aplic = 0 "+
											(isCompra != null ? "and abd01es = :isCompra  " : "") +
											(tipoDoc != null ? "and aah01id in (:tipoDoc) " : "")+
											(numDocIni != null && numDocFin != null ? "and abb01num between :numDocIni and :numDocFin " : "")+
											(dtEmissaoIni != null && dtEmissaoFin ? "and abb01data between :dtEmissaoIni and :dtEmissaoFin " : "") +
											(idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "")+
											(idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "")+
											(idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "")+
											(atendimento != null ? "and eaa0103pedatend in (:atendimento) " : "")+
											(!mps.contains(-1) ? "and abm01tipo in (:mps) " : "")+
											"order by abb01num ");


		if(isCompra != null){
			sql.setParameter("isCompra",isCompra);
		}

		if(tipoDoc != null){
			sql.setParameter("tipoDoc",tipoDoc);
		}
		if(numDocIni != null && numDocFin != null){
			sql.setParameter("numDocIni",numDocIni);
			sql.setParameter("numDocFin",numDocFin);
		}

		if(dtEmissaoIni != null && dtEmissaoFin){
			sql.setParameter("dtEmissaoIni",dtEmissaoIni);
			sql.setParameter("dtEmissaoFin",dtEmissaoFin);
		}

		if(idsItens != null && idsItens.size() > 0){
			sql.setParameter("idsItens",idsItens);
		}

		if(idsEntidades != null && idsEntidades.size() > 0){
			sql.setParameter("idsEntidades",idsEntidades);
		}

		if(idsPcd != null && idsPcd.size() > 0){
			sql.setParameter("idsPcd",idsPcd);
		}

		if(atendimento != null){
			sql.setParameter("atendimento",atendimento);
		}

		if(!mps.contains(-1)){
			sql.setParameter("mps", mps);
		}

		List<TableMap> itensPedido = sql.getListTableMap();

		for(TableMap item : itensPedido){
			item.put("campoLivre1",campoLivre1);
			item.put("campoLivre2",campoLivre2);
			item.put("campoLivre3",campoLivre3);
			item.put("campoLivre4",campoLivre4);

			if(campoLivre1 != null){
				if(totalizar1){
					item.put("controle1",1);
				}
			}
			if(campoLivre2 != null){
				if(totalizar2){
					item.put("controle2",1);
				}
			}
			if(campoLivre3 != null){
				if(totalizar3){
					item.put("controle3",1);
				}
			}
			if(campoLivre4 != null){
				if(totalizar4){
					item.put("controle4",1);
				}
			}
		}

		return itensPedido;
	}
}
//meta-sis-eyJkZXNjciI6IlNDViAtIEl0ZW5zIERlIFBlZGlkbyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNDViAtIEl0ZW5zIERlIFBlZGlkbyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==