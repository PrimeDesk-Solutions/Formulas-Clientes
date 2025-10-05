package Atilatte.relatorios.sce

import java.lang.reflect.Parameter
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


public class SCE_ItensForaDosNiveisDeEstoque extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCE - Itens Fora dos Niveis de Estoque";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap()
		LocalDate data = MDate.date()
		filtrosDefault.put("dataSaldo", data)
		filtrosDefault.put("itemMovEst", true)
		filtrosDefault.put("itemNaoMovEst", true)
		filtrosDefault.put("naoAtend", true)
		filtrosDefault.put("parcialAtend", true)
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
		Boolean naoImprimirSaldoZero = getBoolean("itemSaldoZero");
		LocalDate[] dtPedidos = getIntervaloDatas("dataPedido");
		LocalDate[] dtEntrega = getIntervaloDatas("dtEntrega");
		Boolean naoAtendido = getBoolean("naoAtend")
		Boolean parcialAtendido = getBoolean("parcialAtend")
		Integer impressao = getInteger("impressao");
		List<Long> idsStatus = getListLong("status");
		List<Long> idsLocal = getListLong("local");

		List<Integer> atendimentos = new ArrayList();

		if(naoAtendido) atendimentos.add(0);
		if(parcialAtendido) atendimentos.add(1);

		List<TableMap> dados = buscarDadosRelatorio(mps, itens, loteIni, loteFin, serieIni, serieFin, dataSaldo, detalhamento,naoImprimirSaldoZero, idsStatus, idsLocal, itemMovEstoque, itemNaoMovEst, dtPedidos, dtEntrega,atendimentos  );

		params.put("titulo", "SCE - Itens Fora dos Níveis de Estoque");
		params.put("periodo", "Data Saldo: " + dataSaldo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na() );

		if(impressao == 1) return gerarXLSX("SCE_ItensForaDosNiveisDeEstoque_Excel",dados);

		return gerarPDF("SCE_ItensForaDosNiveisDeEstoque_PDF",dados);

	}


	private List<TableMap> buscarDadosRelatorio(List<Integer> mps,List<Long> itens,String loteIni,String loteFin, String serieIni,String serieFin,LocalDate dataSaldo,
												Integer detalhamento,Boolean naoImprimirSaldoZero, List<Long>idsStatus, List<Long> idsLocal, Boolean itemMovEstoque, Boolean itemNaoMovEst, LocalDate[] dtPedidos,LocalDate[] dtEntrega, List<Integer> atendimentos  ){

		List<TableMap> listItensComMov = buscarItensComMovimentacao(mps,itens, itemMovEstoque, itemNaoMovEst, dtPedidos, dtEntrega, atendimentos);

		List<Long> idsItensMov = new ArrayList();
		List<TableMap> registros = new ArrayList();

		for(item in listItensComMov){
			Long idItem = item.getLong("abm01id");
			BigDecimal saldo = buscarSaldoItem(idItem, dataSaldo, idsStatus, idsLocal, loteIni, loteFin, serieIni, serieFin );
			def esMov = item.getInteger("mov");
			BigDecimal totalCompra = esMov == 0 ? item.getBigDecimal_Zero("qtd") : new BigDecimal(0);
			BigDecimal totalVenda = esMov == 1 ? item.getBigDecimal_Zero("qtd") : new BigDecimal(0);

			idsItensMov.add(idItem);

			item.put("saldo", saldo);
			item.put("totCompra", totalCompra);
			item.put("totVenda", totalVenda);
			item.put("excesso",((saldo + totalCompra - totalVenda) - item.getBigDecimal_Zero("abm0101estMax")));

			Integer itemValido  = verificarItem(item,saldo,detalhamento);

			if(itemValido == 0) continue;
			if(naoImprimirSaldoZero && saldo == 0) continue

			registros.add(item)
		}

		List<TableMap> itensSemMov = buscarItensSemMovimentacao(idsItensMov,mps,itens, itemMovEstoque, itemNaoMovEst);

		for(item in itensSemMov){
			Long idItem = item.getLong("abm01id");
			BigDecimal saldo = buscarSaldoItem(idItem, dataSaldo, idsStatus, idsLocal, loteIni, loteFin, serieIni, serieFin );

			item.put("saldo", saldo);
			item.put("totCompra", new BigDecimal(0));
			item.put("totVenda", new BigDecimal(0));
			item.put("excesso",saldo - item.getBigDecimal_Zero("abm0101estMax"));

			Integer itemValido  = verificarItem(item,saldo,detalhamento);

			if(itemValido == 0) continue;
			if(naoImprimirSaldoZero && saldo == 0) continue

			registros.add(item)

		}

		return registros;

	}

	private List<TableMap>buscarItensComMovimentacao(List<Integer>tipos,List<Long>  itens, Boolean itemMovEstoque, Boolean itemNaoMovEst, LocalDate[] dtPedidos,LocalDate[] dtEntrega, List<Integer> atendimentos){

		def movEst = !itemNaoMovEst && !itemMovEstoque ? [-1] : new ArrayList();

		if(itemNaoMovEst) movEst.add(0);
		if(itemMovEstoque) movEst.add(1);

		String whereGrupo = "where abm01grupo = 0 ";
		String whereClasDoc = "and eaa01clasdoc = 0 ";
		String whereTipo = tipos != null && !tipos.contains(-1) ? "and abm01tipo in (:tipos) " : null;
		String whereEmpresa = "and abm01gc = :empresa ";
		String whereItens = itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "";
		String whereInativo = "and abm01di is null ";
		String whereMovEst = !movEst.contains(-1) ? "and abm11movEst in (:movEst) " : "";
		String whereDatas = dtPedidos != null && dtEntrega != null ? "and (abb01data between :dtPedidosIni and :dtPedidosFin or eaa0103dtEntrega between :dtEntregaIni and :dtEntregaFin) " :
							dtPedidos != null && dtEntrega == null ? "and abb01data between :dtPedidosIni and :dtPedidosFin " :
							dtPedidos == null && dtEntrega != null ? "eaa0103dtEntrega between :dtEntregaIni and :dtEntregaFin " : "";
		String whereAtendimento = atendimentos != null && atendimentos.size() > 0 ? "and eaa01scvatend in (:atendimentos) " : "and eaa01scvatend in (0,1) "


		Parametro parametroTipo = tipos != null && !tipos.contains(-1) ? Parametro.criar("tipos", tipos) : null;
		Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
		Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
		Parametro parametroMOvEst = !movEst.contains(-1) ? Parametro.criar("movEst", movEst) : null;
		Parametro parametroDataPedidoIni = dtPedidos != null ? Parametro.criar("dtPedidosIni", dtPedidos[0]) : null;
		Parametro parametroDataPedidoFin = dtPedidos != null ? Parametro.criar("dtPedidosFin", dtPedidos[1]) : null;
		Parametro parametroDataEntregaIni = dtEntrega != null ? Parametro.criar("dtEntregaIni", dtEntrega[0]) : null;
		Parametro parametroDataEntregaFin = dtEntrega != null ? Parametro.criar("dtEntregaFin", dtEntrega[1]) : null;
		Parametro parametroAtendimento = atendimentos != null && atendimentos.size() > 0 ? Parametro.criar("atendimentos", atendimentos) : Parametro.criar("atendimentos", [0,1])

		String sql = "select abm01tipo,abm01id, abm01codigo as codItem, case when abm01tipo = 0 then 'M' else 'P' end as mps, abm01na as naItem, abm0101estMax, "+
						"abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo,eaa01esmov as mov, COALESCE(SUM(eaa0103qtUso),0) - COALESCE(SUM(eaa01032qtUso),0) as qtd "+
						"from abm01 "+
						"left join eaa0103 on eaa0103item = abm01id "+
						"left join eaa01032 on eaa01032itemscv = eaa0103id "+
						"left join eaa01 on eaa01id = eaa0103doc "+
						"left join abb01 on abb01id = eaa01central "+
						"left join abm0101 on abm0101item = abm01id "+
						"left join abm11 on abm11id = abm0101estoque "+
						"left join aam06 on aam06id = abm01umu "+
						whereGrupo+
						whereClasDoc +
						whereTipo+
						whereEmpresa+
						whereItens+
						whereInativo+
						whereMovEst+
						whereDatas +
						whereAtendimento +
						"group by abm01tipo, abm01id, abm01codigo,mps, abm01na, abm0101estMax," +
						"abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo, eaa01esmov "+
						"order by abm01tipo, abm01codigo"
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroTipo,parametroEmpresa, parametroItens, parametroMOvEst, parametroDataPedidoIni,parametroDataPedidoFin,parametroDataEntregaIni,parametroDataEntregaFin, parametroAtendimento )

	}

	private List<TableMap>buscarItensSemMovimentacao(List<Long> idItensAux, List<Integer>tipos,List<Long>  itens, Boolean itemMovEstoque, Boolean itemNaoMovEst){

		def movEst = !itemNaoMovEst && !itemMovEstoque ? [-1] : new ArrayList();

		if(itemNaoMovEst) movEst.add(0);
		if(itemMovEstoque) movEst.add(1);

		String whereGrupo = "where abm01grupo = 0 ";
		String whereTipo = tipos != null && !tipos.contains(-1) ? "and abm01tipo in (:tipos) " : null;
		String whereEmpresa = "and abm01gc = :empresa ";
		String whereItens = itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "";
		String whereItensAux = "and abm01id not in (:idItensAux) ";
		String whereInativo = "and abm01di is null ";
		String whereMovEst = !movEst.contains(-1) ? "and abm11movEst in (:movEst) " : "";


		Parametro parametroTipo = tipos != null && !tipos.contains(-1) ? Parametro.criar("tipos", tipos) : null;
		Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
		Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
		Parametro parametroItensAux = Parametro.criar("idItensAux", idItensAux);
		Parametro parametroMovEst = !movEst.contains(-1) ? Parametro.criar("movEst", movEst) : null;

		String sql = "select abm01tipo,abm01id, abm01codigo as codItem, case when abm01tipo = 0 then 'M' else 'P' end as mps, abm01na as naItem, abm0101estMax, "+
					"abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo "+
					"from abm01 "+
					"left join abm0101 on abm0101item = abm01id "+
					"left join abm11 on abm11id = abm0101estoque "+
					"left join aam06 on aam06id = abm01umu "+
					whereGrupo+
					whereTipo+
					whereEmpresa+
					whereItens+
					whereItensAux +
					whereInativo+
					whereMovEst+
					"group by abm01tipo, abm01id, abm01codigo,mps, abm01na, abm0101estMax," +
					"abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo "+
					"order by abm01tipo, abm01codigo"

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroTipo,parametroEmpresa, parametroItens,parametroItensAux, parametroMovEst )

	}

	private BigDecimal buscarSaldoItem(Long abm01id, LocalDate data,List<Long> idsStatus,List<Long> idsLocal, String loteIni, String loteFin, String serieIni, String serieFin){
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "and bcc01status in (:idsStatus) " : "";
		String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "and bcc01ctrl0 in (:idsLocal) " : "";
		String whereLoteIni = loteIni != null && !loteIni.isEmpty() ? " and bcc01lote >= :loteIni " : "";
		String whereLoteFin = loteFin != null && !loteFin.isEmpty() ? " and bcc01lote <= :loteFin " : "";
		String whereSerieIni = serieIni != null && !serieIni.isEmpty() ? " and bcc01serie >= :serieIni " : "";
		String whereSerieFin = serieFin != null && !serieFin.isEmpty() ? " and bcc01serie <= :loteFin " : "";

		Parametro paramItem = Parametro.criar("abm01id",abm01id);
		Parametro paramData = Parametro.criar("data",data);
		Parametro paramLoteIni = Parametro.criar("loteIni",loteIni);
		Parametro paramLoteFin = Parametro.criar("loteFin",loteFin);
		Parametro paramSerieIni = Parametro.criar("serieIni",serieIni);
		Parametro paramSerieFin = Parametro.criar("serieFin",serieFin);
		Parametro parametroStatus = idsStatus != null && idsStatus.size() > 0 ? Parametro.criar("idsStatus", idsStatus) : null;
		Parametro parametroLocal = idsLocal != null && idsLocal.size() > 0 ? Parametro.criar("idsLocal", idsLocal) : null;


		String sql = "select coalesce(sum(bcc01qtps),0) as qtd "+
					"from bcc01 "+
					"inner join abm01 on abm01id = bcc01item "+
					"inner join abm0101 on abm0101item = abm01id "+
					"inner join aam06 on aam06id = abm01umu "+
					"where abm01id = :abm01id "+
					"and bcc01data <= :data " +
					whereStatus +
					whereLocal +
					whereLoteIni +
					whereLoteFin +
					whereSerieIni +
					whereSerieFin;

		return getAcessoAoBanco().obterBigDecimal(sql,paramItem, paramData, paramLoteIni, paramLoteFin, paramSerieIni, paramSerieFin,parametroStatus, parametroLocal);
	}

	private Integer verificarItem(TableMap item, BigDecimal saldo, Integer detalhamento){
		/*
			Verifica se o item atende os requisitos do filtro de detalhamento, se sim retorna 1, caso contrário retorna 0

			Somente serão exibidos os itens caso o retorno dessa função for igual 1

		 */

		def estMax = item.getBigDecimal_Zero("abm0101estMax");
		def estMin = item.getBigDecimal_Zero("abm0101estMin");
		def pontoPedido = item.getBigDecimal_Zero("abm0101ptoPed");

		if ( saldo > estMax && detalhamento == 0 ) return 1;
		if (saldo < estMin && detalhamento == 1) return 1;
		if ((saldo >= pontoPedido && saldo <= pontoPedido) && detalhamento == 2) return 1;
		if (detalhamento == 3) return 1;

		return 0
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9