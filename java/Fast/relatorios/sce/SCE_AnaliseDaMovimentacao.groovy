package Fast.relatorios.sce;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.collections.TableMap;

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
import java.time.format.DateTimeFormatter

public class SCE_AnaliseDaMovimentacao extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SCE Análise da Movimentação "; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap()
		LocalDate data = MDate.date()
		LocalDate dataIni = LocalDate.of(data.year,data.getMonthValue() - 1,1)
		filtrosDefault.put("dataSaldo", data)
		filtrosDefault.put("itensInventariaveis", true)
		filtrosDefault.put("itensNaoInventariaveis", true)
		filtrosDefault.put("impressao", "0")
		filtrosDefault.put("loteIni", "")
		filtrosDefault.put("loteFin", "")
		filtrosDefault.put("serieIni", "")
		filtrosDefault.put("serieFin", "")
		filtrosDefault.put("dtIni", dataIni.format(DateTimeFormatter.ofPattern("MM/yyyy")))
		filtrosDefault.put("dtFin", data.format(DateTimeFormatter.ofPattern("MM/yyyy")))
		
		
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		List<Integer> mps = getListInteger("mpm");
		String itemIni = getString("itemIni");
		String itemFin = getString("itemFin");
		Boolean itemInventariavel = getBoolean("itensInventariaveis");
		Boolean itemNaoInventariavel = getBoolean("itensNaoInventariaveis");
		List<Integer> tipoMovimentacao = getListInteger("tipoMov");
		List<Long> PLE = getListInteger("PLE");
		List<Long> idsStatus = getListLong("status");
		List<Long> idsLocal = getListLong("local");
		String loteIni = getString("loteIni");
		String loteFin = getString("loteFin");
		String serieIni = getString("serieIni");
		String serieFin = getString("serieFin");
		Integer impressao = getInteger("impressao");
		String dtMovimentacaoIni = getString("dtIni");
		String dtMovimentacaoFin = getString("dtFin");
		LocalDate dataSaldo = getLocalDate("dataSaldo");

		
		List<TableMap> dados = buscarDadosRelatorio(mps,itemIni,itemFin,itemInventariavel,itemNaoInventariavel,tipoMovimentacao,PLE,idsStatus,idsLocal,loteIni,loteFin,serieIni,serieFin,dtMovimentacaoIni,dtMovimentacaoFin,dataSaldo);
		
		params.put("titulo","Analise de Movimentação");
		params.put("empresa",obterEmpresaAtiva().aac10codigo +"-"+ obterEmpresaAtiva().aac10na);
		params.put("periodo","Movimentação: " +dtMovimentacaoIni +" à "+dtMovimentacaoFin);
		if(impressao == 1) return gerarXLSX("SCE_AnaliseDaMovimentacao_Excel",dados);
		return gerarPDF("SCE_AnaliseDaMovimentacao_PDF",dados);
	}

	private List<TableMap>  buscarDadosRelatorio(List<Integer>mps,String itemIni,String itemFin,Boolean itemInventariavel, Boolean itemNaoInventariavel,List<Integer>tipoMovimentacao,List<Long>PLE,List<Long>idsStatus,List<Long>idsLocal,String loteIni,String loteFin,String serieIni,String serieFin,String dtMovimentacaoIni,String dtMovimentacaoFin,LocalDate dataSaldo){
		List<Abm01> abm01s = buscarItens(mps,itemIni,itemFin,itemInventariavel,itemNaoInventariavel);
		List<String> datas = buscarListaDeDatas(dtMovimentacaoIni,dtMovimentacaoFin);
		
		List<TableMap> registros = new ArrayList();
		
		for(abm01 in abm01s){
			Abm0101 abm0101 =  obterCamposLivresItem(abm01.abm01id);
			if(abm0101.abm0101json != null){
				String tipoItem = abm01.abm01tipo == 0 ? 'M' : 'P';
				def total = 0;
				def media = 0;
				def saldoEstoque = buscarSaldoAtualItem(abm01.abm01id)
				if(datas.size() > 0){
					for(periodo in datas){
						TableMap lancamento = new TableMap();
	
						BigDecimal totalEntrada = buscarLancamentoItem(abm01.abm01id,tipoMovimentacao,PLE,idsStatus,idsLocal, loteIni, loteFin, serieIni, serieFin,periodo,0)
						BigDecimal totalSaida = buscarLancamentoItem(abm01.abm01id,tipoMovimentacao,PLE,idsStatus,idsLocal, loteIni, loteFin, serieIni, serieFin,periodo,1)
						
						BigDecimal totalGeralItem = totalEntrada - totalSaida;

						total += totalGeralItem;
						media = total / datas.size();
						
						lancamento.put("valor",totalGeralItem);
						lancamento.put("item",tipoItem +" "+abm01.abm01codigo +" "+abm01.abm01na);
						lancamento.put("codItem",abm01.abm01codigo);
						lancamento.put("naItem",abm01.abm01na);
						lancamento.put("tipoItem",tipoItem);
						lancamento.put("cabecalho",periodo);
						lancamento.put("estMax",abm0101.abm0101estMax);
						lancamento.put("estMin",abm0101.abm0101estMin);
						lancamento.put("estSeg",abm0101.abm0101estSeg);
						lancamento.put("pontoPedido",abm0101.abm0101ptoPed);
						lancamento.put("custo",abm0101.abm0101json.getBigDecimal_Zero("custo"));
						lancamento.put("maiorPreco",abm0101.abm0101json.getBigDecimal_Zero("maior_preco_unit"));
						lancamento.put("menorPreco",abm0101.abm0101json.getBigDecimal_Zero("menor_preco_unit"));
						lancamento.put("ultimoPreco",abm0101.abm0101json.getBigDecimal_Zero("ultimo_preco"));
						lancamento.put("total",total);
						lancamento.put("media",media);
						lancamento.put("saldoEstoque",saldoEstoque != null ? saldoEstoque : new BigDecimal(0));
	
						registros.add(lancamento);	 
					}
				}
			}
		}
		
					
		return registros
		
	}

	private List<Abm01>buscarItens(List<Integer>tipos,String itemIni,String itemFin,Boolean itemInventariavel,Boolean itemNaoInventariavel){
		Criterion critTipo = tipos != null && !tipos.contains(-1) ? Criterions.in("abm01tipo", tipos) : Criterions.isTrue();
		Criterion critItem = itemIni != null && itemFim != null ? Criterions.between("abm01codigo", itemIni, itemFim) : itemIni != null && itemFim == null ? Criterions.ge("abm01codigo", itemIni) : itemIni == null && itemFim != null ? Criterions.le("abm01codigo", itemIni) : Criterions.isTrue();
		
		Criterion invent = Criterions.or(Criterions.isNotNull("abm11giProp"), Criterions.isNotNull("abm11giComTerc"), Criterions.isNotNull("abm11giDeTerc"));
		Criterion noInve = Criterions.and(Criterions.isNull("abm11giProp"), Criterions.isNull("abm11giComTerc"), Criterions.isNull("abm11giDeTerc"));
		Criterion critInve = itemInventariavel && !itemNaoInventariavel ? invent : !itemInventariavel && itemNaoInventariavel ? noInve : Criterions.isTrue();

		return getSession().createCriteria(Abm01.class)
						   .addJoin(Joins.fetch("abm01umu").left(true))
						   .addJoin(Joins.join("Abm0101", "abm0101item = abm01id"))
						   .addJoin(Joins.join("Abm11", "abm11id = abm0101estoque").left(true))
						   .addWhere(Criterions.eq("abm01grupo", Abm01.NAO))
						   .addWhere(critTipo).addWhere(critItem).addWhere(critCrit)
						   .addWhere(Criterions.eq("abm0101empresa", obterEmpresaAtiva().aac10id))
						   .addWhere(critInve).setOrder("abm01tipo, abm01codigo").getList(ColumnType.ENTITY);
				   
	}

	private BigDecimal buscarLancamentoItem(Long abm01id,List<Integer>tipoMovimentacao,List<Long>PLE,List<Long>idsStatus,List<Long>idsLocal,String loteIni,String loteFin,String serieIni,String serieFin,String periodo, Integer movimentacao){
		List<Long> listPLE = new ArrayList();
		
		if(PLE == null && PLE == []){
			listPLE = buscarPlePorMovimentacao(tipoMovimentacao);
		}

		String whereItem = "AND bcc01item = :idItem ";
		String wherePLE = listPLE != null && listPLE.size() > 0 ? "and abm20id in (:listPLE) " : "";
		String whereTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? " and abm20rastrear in (:tipoMovimentacao) " : "";
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "and aam04id in (:idsStatus) " : "";
		String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "and abm15id in (:idsLocal) " : "";
		String whereLoteIni = !loteIni.isEmpty() ? "and bcc01lote >= :loteIni " : "";
		String whereLoteFin = !loteFin.isEmpty() ? "and bcc01lote <= :loteFin " : "";
		String whereSerieIni = !serieIni.isEmpty() ? "and bcc01serie >= :serieIni " : "";
		String whereSerieFin = !serieFin.isEmpty() ? "and bcc01serie <= :serieFin " : "";
		String wherePeriodo = "and TO_CHAR(bcc01data, 'MM/YYYY') = :periodo "
		String whereMovimentacao = "and bcc01mov = :movimentacao "
		

		Parametro parametroItem = Parametro.criar("idItem",abm01id);
		Parametro parametroPLE = listPLE != null && listPLE.size() > 0 ? Parametro.criar("listPLE",listPLE) : null;
		Parametro parametroTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? Parametro.criar("tipoMovimentacao",tipoMovimentacao) : null;
		Parametro parametroStatus = idsStatus != null && idsStatus.size() >= 0 ? Parametro.criar("idsStatus",idsStatus) : null;
		Parametro parametroLocal = idsLocal != null && idsLocal.size() >= 0 ? Parametro.criar("idsLocal",idsLocal) : null;
		Parametro parametroLoteIni = !loteIni.isEmpty() ? criarParametroSql("loteIni", loteIni) : null;
		Parametro parametroLoteFin = !loteFin.isEmpty() ? criarParametroSql("loteFin", loteFin) : null;
		Parametro parametroSerieIni = !serieIni.isEmpty() ? criarParametroSql("serieIni", serieIni) : null;
		Parametro parametroSerieFin = !serieFin.isEmpty() ? criarParametroSql("serieFin", serieFin) : null;
		Parametro parametroPeriodo = Parametro.criar("periodo",periodo);
		Parametro parametroMovimentacao = Parametro.criar("movimentacao",movimentacao);
		
		
		String sql = "SELECT coalesce(sum(bcc01qt),0) as valor "+
					"from bcc01 "+
					"inner join abm01 on abm01id = bcc01item "+
					"inner join abm0101 on abm0101item = abm01id "+
					"inner join abm20 on abm20id = bcc01ple "+
					"inner join aam04 on aam04id = bcc01status "+
					"inner join abm15 on abm15id = bcc01ctrl0 "+
					wherePLE+
					whereTipoMov+
					whereStatus+
					whereLocal+
					whereLoteIni+
					whereLoteFin+
					whereSerieIni+
					whereSerieFin+
					whereItem+
					wherePeriodo +
					whereMovimentacao

					

					
		return getAcessoAoBanco().obterBigDecimal(sql,parametroPLE,parametroTipoMov,parametroStatus,parametroLocal,parametroLoteIni,
										parametroLoteFin,parametroSerieIni,parametroSerieFin,parametroItem,parametroPeriodo,parametroMovimentacao)

	}

	private List<String> buscarListaDeDatas(String dtMovimentacaoIni, String dtMovimentacaoFin){
		String sql = "select distinct TO_CHAR(bcc01data, 'MM/YYYY') as datas  "+
					"from bcc01  "+
					"where TO_CHAR(bcc01data, 'MM/YYYY') between :dtMovimentacaoIni and :dtMovimentacaoFin " +
					"and bcc01mov in (0,1) "
					
		Parametro p1 = Parametro.criar("dtMovimentacaoIni",dtMovimentacaoIni);
		Parametro p2 = Parametro.criar("dtMovimentacaoFin",dtMovimentacaoFin);

		return getAcessoAoBanco().obterListaDeString(sql,p1,p2);
	}

	private List<Long> buscarPlePorMovimentacao(List<Integer>tipoMovimentacao){
		
		String whereRastrear = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? "where abm20rastrear in (:tipoMovimentacao) " : "";
		
		String sql = "select abm20id from abm20 " + whereRastrear;

		Parametro p1 = Parametro.criar("tipoMovimentacao",tipoMovimentacao);

		return getAcessoAoBanco().obterListLong(sql,p1)
					
	}

	private Abm0101 obterCamposLivresItem(Long abm01id){
		return getSession().createCriteria(Abm0101.class)
		.addWhere(Criterions.eq("abm0101item", abm01id))
		.get(ColumnType.ENTITY);
	}

	private BigDecimal buscarSaldoAtualItem(Long abm01id){
		String sql = "select bcc02qt from bcc02 where bcc02item = :abm01id and bcc02status = 112003 ";

		return getAcessoAoBanco().obterBigDecimal(sql,Parametro.criar("abm01id",abm01id))
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSBBbsOhbGlzZSBkYSBNb3ZpbWVudGHDp8OjbyAiLCJ0aXBvIjoicmVsYXRvcmlvIn0=