package Profer.relatorios.sce

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.Parametros
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcc02
import sam.model.entities.bc.Bcc0201
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro
import java.time.format.DateTimeFormatter;


public class SCE_ItensForaDosNiveisDeEstoqueII extends RelatorioBase {
	@Override
	public DadosParaDownload executar() {
		List<Integer> mps = getListInteger("mpm");
		List<Long> itens = getListLong("itens");
		Boolean itemMovEstoque = getBoolean("itemMovEst");
		Boolean itemNaoMovEst = getBoolean("itemNaoMovEst");
		String loteIni = getString("loteIni");
		String loteFin = getString("loteFin");
		String serieIni = getString("serieIni");
		String serieFin = getString("serieFin");
		LocalDate dataSaldo = getLocalDate("dataSaldo");
		Integer detalhamento = getInteger("optionsDetalhamento");
		Boolean pedidoVenda = getBoolean("pedidoVenda")
		Boolean pedidoCompra = getBoolean("pedidoCompra")
		Boolean naoImprimirSaldoZero = getBoolean("itemSaldoZero");
		LocalDate[] dtPedidos = getIntervaloDatas("dataPedido");
		LocalDate[] dtEntrega = getIntervaloDatas("dtEntrega");
		Boolean naoAtendido = getBoolean("naoAtend")
		Boolean parcialAtendido = getBoolean("parcialAtend")
		Boolean totalAtendido = getBoolean("totalAtend")
		Integer impressao = getInteger("impressao");
		List<Long> idsStatus = getListLong("status");
		List<Long> idsLocal = getListLong("local");
		List<TableMap> dados = new ArrayList();

		List<Integer> atendimentos = new ArrayList();

		if(naoAtendido) atendimentos.add(0);
		if(parcialAtendido) atendimentos.add(1);
		if(totalAtendido) atendimentos.add(2);

		buscarDadosRelatorio(mps, itens, loteIni, loteFin,
				serieIni, serieFin, dataSaldo, detalhamento, pedidoVenda, pedidoCompra, naoImprimirSaldoZero,dados, idsStatus, idsLocal, itemMovEstoque, itemNaoMovEst, dtPedidos, dtEntrega,atendimentos  );

		params.put("titulo", "SCE - Itens Fora dos Níveis de Estoque");
		params.put("periodo", "Data Saldo: " + dataSaldo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na() );

		if(impressao == 1) return gerarXLSX("SCE_ItensForaDosNiveisDeEstoque_Excel",dados)
		return gerarPDF("SCE_ItensForaDosNiveisDeEstoque_PDF",dados)

	}
	@Override
	public String getNomeTarefa() {
		return "SCE - Itens Fora dos Niveis de Estoque";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap()
		LocalDate data = MDate.date()
		filtrosDefault.put("dataSaldo", data)
		filtrosDefault.put("itensInventariaveis", true)
		filtrosDefault.put("itensNaoInventariaveis", true)
		filtrosDefault.put("impressao", "0")
		filtrosDefault.put("loteIni", "")
		filtrosDefault.put("loteFin", "")
		filtrosDefault.put("serieIni", "")
		filtrosDefault.put("serieFin", "")
		filtrosDefault.put("optionsDetalhamento","0")
		filtrosDefault.put("itemSaldoZero",true)
		filtrosDefault.put("pedidoVenda",true)
		filtrosDefault.put("pedidoCompra",true)

		return Utils.map("filtros", filtrosDefault);
	}

	private void buscarDadosRelatorio(List<Integer> mps,List<Long> itens,String loteIni,String loteFin,
												String serieIni,String serieFin,LocalDate dataSaldo,Integer detalhamento,Boolean pedidoVenda,Boolean pedidoCompra,Boolean naoImprimirSaldoZero, List<TableMap> dados, List<Long>idsStatus, List<Long> idsLocal, Boolean itemMovEstoque, Boolean itemNaoMovEst, LocalDate[] dtPedidos,LocalDate[] dtEntrega, List<Integer> atendimentos  ){
		List<TableMap> listItens = buscarItens(mps,itens, itemMovEstoque, itemNaoMovEst, dtPedidos, dtEntrega);
		for(abm01 in listItens){
			BigDecimal saldoItemEntrada = buscarSaldoPorLancamentoItem(abm01.getLong("abm01id"), dataSaldo,idsStatus,idsLocal,0,loteIni, loteFin, serieIni, serieFin);
			BigDecimal saldoItemSaida = buscarSaldoPorLancamentoItem(abm01.getLong("abm01id"), dataSaldo,idsStatus,idsLocal,1,loteIni, loteFin, serieIni, serieFin);
			BigDecimal saldoAtual = saldoItemEntrada - saldoItemSaida

			if(saldoAtual <= 0 && naoImprimirSaldoZero ) continue;

			TableMap itemDetalhado = buscarItemDetalhado(abm01.getLong("abm01id"),saldoAtual,detalhamento);


			if(itemDetalhado != null){
				BigDecimal totCompra = pedidoCompra ? obterValorTotalCompraVenda(abm01.getLong("abm01id"), dataSaldo,dtPedidos, 0) : new BigDecimal(0);
				BigDecimal totVenda = pedidoVenda ? obterValorTotalCompraVenda(abm01.getLong("abm01id"), dataSaldo,dtPedidos, 1) : new BigDecimal(0);

				itemDetalhado.putAll(itemDetalhado);

				itemDetalhado.put("saldo",saldoAtual);
				itemDetalhado.put("totCompra",totCompra);
				itemDetalhado.put("totVenda",totVenda);
				itemDetalhado.put("excesso",(itemDetalhado.getBigDecimal_Zero("abm0101estMax") - (saldoAtual + totCompra - totVenda)) * -1   );

				dados.add(itemDetalhado)
			}
		}

	}

	private List<TableMap>buscarItens(List<Integer>tipos,List<Long>  itens, Boolean itemMovEstoque, Boolean itemNaoMovEst, LocalDate[] dtPedidos, LocalDate[] dtEntrega){

		def movEst = !itemNaoMovEst && !itemMovEstoque ? [-1] : new ArrayList();

		if(itemNaoMovEst) movEst.add(0);
		if(itemMovEstoque) movEst.add(1);

		String whereGrupo = "where abm01grupo = 0 ";
		String whereTipo = tipos != null && !tipos.contains(-1) ? "and abm01tipo in (:tipos) " : null;
		String whereDatas = dtPedidos != null && dtEntrega != null ? "and (abb01data between :dtPedidosIni and :dtPedidosFin or eaa0103dtEntrega between :dtEntregaIni and :dtEntregaFin) " :
							dtPedidos != null && dtEntrega == null ? "and abb01data between :dtPedidosIni and :dtPedidosFin " :
							dtPedidos == null && dtEntrega != null ? "eaa0103dtEntrega between :dtEntregaIni and :dtEntregaFin " : "";
		String whereEmpresa = "and eaa01gc = :empresa ";
		String whereItens = itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "";
		String whereInativo = "and abm01di is null ";
		String whereMovEst = !movEst.contains(-1) ? "and abm11movEst in (:movEst) " : "";


		Parametro parametroTipo = tipos != null && !tipos.contains(-1) ? Parametro.criar("tipos", tipos) : null;
		Parametro parametroDataPedidoIni = dtPedidos != null ? Parametro.criar("dtPedidosIni", dtPedidos[0]) : null;
		Parametro parametroDataPedidoFin = dtPedidos != null ? Parametro.criar("dtPedidosFin", dtPedidos[1]) : null;
		Parametro parametroDataEntregaIni = dtEntrega != null ? Parametro.criar("dtEntregaIni", dtEntrega[0]) : null;
		Parametro parametroDataEntregaFin = dtEntrega != null ? Parametro.criar("dtEntregaFin", dtEntrega[1]) : null;
		Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
		Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
		Parametro parametroMOvEst = !movEst.contains(-1) ? Parametro.criar("movEst", movEst) : null;

		String sql = "select distinct abm01codigo, abm01na, abm01id " +
						"from abm01 " +
						"inner join eaa0103 on eaa0103item = abm01id " +
						"inner join eaa01 on eaa01id = eaa0103doc " +
						"inner join abb01 on abb01id = eaa01central " +
						"left join abm0101 on abm0101item = abm01id "+
						"left join abm11 on abm11id = abm0101estoque "+
						whereGrupo+
						whereTipo+
						whereDatas+
						whereEmpresa+
						whereItens+
						whereInativo+
						whereMovEst+
						"order by abm01codigo"

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroTipo, parametroDataPedidoIni, parametroDataPedidoFin,parametroDataEntregaIni,parametroDataEntregaFin, parametroEmpresa, parametroItens, parametroMOvEst )

	}



	private BigDecimal buscarSaldoPorLancamentoItem(Long abm01id, LocalDate data,List<Long> idsStatus,List<Long> idsLocal, Integer movimentacao, String loteIni, String loteFin, String serieIni, String serieFin){
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "and bcc01status in (:idsStatus) " : "";
		String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "and bcc01ctrl0 in (:idsLocal) " : "";

		String whereLoteIni = loteIni != null && !loteIni.isEmpty() ? " and bcc01lote >= :loteIni " : "";
		String whereLoteFin = loteFin != null && !loteFin.isEmpty() ? " and bcc01lote <= :loteFin " : "";
		String whereSerieIni = serieIni != null && !serieIni.isEmpty() ? " and bcc01serie >= :serieIni " : "";
		String whereSerieFin = serieFin != null && !serieFin.isEmpty() ? " and bcc01serie <= :loteFin " : "";


		String sql = "select coalesce(sum(bcc01qt),0) as qtd "+
				"from bcc01 "+
				"inner join abm01 on abm01id = bcc01item "+
				"inner join abm0101 on abm0101item = abm01id "+
				"inner join aam06 on aam06id = abm01umu "+
				"where abm01id = :abm01id "+
				"and bcc01data <= :data " +
				"and  bcc01mov = :movimentacao " +
				whereStatus +
				whereLocal +
				whereLoteIni +
				whereLoteFin +
				whereSerieIni +
				whereSerieFin

		Parametro p1 = Parametro.criar("abm01id",abm01id);
		Parametro p2 = Parametro.criar("data",data);
		Parametro p3 = Parametro.criar("movimentacao",movimentacao);
		Parametro p4 = Parametro.criar("loteIni",loteIni);
		Parametro p5 = Parametro.criar("loteFin",loteFin);
		Parametro p6 = Parametro.criar("serieIni",serieIni);
		Parametro p7 = Parametro.criar("serieFin",serieFin);
		Parametro parametroStatus = idsStatus != null && idsStatus.size() > 0 ? Parametro.criar("idsStatus", idsStatus) : null;
		Parametro parametroLocal = idsLocal != null && idsLocal.size() > 0 ? Parametro.criar("idsLocal", idsLocal) : null;

		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5,p6,p7,parametroStatus, parametroLocal);
	}

	private BigDecimal obterValorTotalCompraVenda(Long abm01id,LocalDate data,LocalDate[] dtPedidos, Integer compraVenda){

		// Data Pedidos Inicial - Final
		LocalDate dtInicial = null;
		LocalDate dtFinal = null;
		if(dtPedidos != null){
			dtInicial = dtPedidos[0];
			dtFinal = dtPedidos[1];
		}

		String whereItem = "where eaa0103item = :abm01id ";
		String whereDatas = dtPedidos != null ? "and abb01data between :dtInicial and :dtFinal " : "";
		String whereEsMov = "and eaa01esMov = :compraVenda ";
		String whereClasDoc = "and eaa01clasdoc = 0 ";



		String sql = "select SUM(EAA0103QTCOML - COALESCE(EAA01032QTCOML,0)) from eaa01 "+
					"inner join eaa0103 on eaa0103doc = eaa01id "+
					"left join eaa01032 on eaa01032itemscv = eaa0103id "+
					"inner join abb01 on abb01id = eaa01central "+
					whereItem+
					whereDatas+
					whereEsMov+
					whereClasDoc;

		Parametro p1 = Parametro.criar("abm01id",abm01id);
		Parametro p2 = Parametro.criar("data",data);
		Parametro p3 = Parametro.criar("compraVenda",compraVenda);
		Parametro p4 = dtPedidos != null ? Parametro.criar("dtInicial",dtInicial) : null;
		Parametro p5 = dtPedidos != null ? Parametro.criar("dtFinal",dtFinal) : null;

		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4,p5);
	}

	private buscarItemDetalhado(Long idItem,BigDecimal saldoAtual,Integer detalhamento){

		String whereDetalhado = detalhamento == 0 ? "and abm0101estMax < :saldoAtual " : detalhamento == 1 ? "and abm0101estMin > :saldoAtual " : detalhamento == 2 ?  "and abm0101estMin >= :saldoAtual and  abm0101ptoPed <= :saldoAtual  " : "";

		String sql = "select abm01codigo as codItem, case when abm01tipo = 0 then 'M' else 'P' end as mps, abm01na as naItem, abm0101estMax, "+
				"abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo "+
				"from abm01 "+
				"inner join abm0101 on abm0101item = abm01id "+
				"inner join aam06 on aam06id = abm01umu "+
				"where abm01id = :idItem "+
				whereDetalhado

		Parametro p1 = Parametro.criar("idItem",idItem);
		Parametro p2 = Parametro.criar("saldoAtual",saldoAtual);

		return getAcessoAoBanco().buscarUnicoTableMap(sql,p1,p2)
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9