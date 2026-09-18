package During.relatorios.sce;

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
	@Override 
	public DadosParaDownload executar() {
		List<Integer> mps = getListInteger("mpm");
		String itemIni = getString("itemIni");
		String itemFin = getString("itemFim");
		Boolean itemInventariavel = getBoolean("itensInventariaveis");
		Boolean itemNaoInventariavel = getBoolean("itensNaoInventariaveis");
		String loteIni = getString("loteIni");
		String loteFin = getString("loteFin");
		String serieIni = getString("serieIni");
		String serieFin = getString("serieFin");
		LocalDate dataSaldo = getLocalDate("dataSaldo");
		Integer detalhamento = getInteger("optionsDetalhamento");
		Boolean pedidoVenda = getBoolean("pedidoVenda")
		Boolean pedidoCompra = getBoolean("pedidoCompra")
		Boolean naoImprimirSaldoZero = getBoolean("itemSaldoZero")
		Integer impressao = getInteger("impressao");

		List<TableMap> dados = new ArrayList();
		
		buscarDadosRelatorio(mps, itemIni, itemFin, itemInventariavel, itemNaoInventariavel, loteIni, loteFin,
	 	serieIni, serieFin, dataSaldo, detalhamento, pedidoVenda, pedidoCompra, naoImprimirSaldoZero,dados)
		if(impressao == 1) return gerarXLSX("SCE_ItensForaDosNiveisDeEstoque_Excel",dados)
	 	return gerarPDF("SCE_ItensForaDosNiveisDeEstoque_PDF",dados)

	}

	private List<TableMap> buscarDadosRelatorio(List<Integer> mps,String itemIni,String itemFin,Boolean itemInventariavel,Boolean itemNaoInventariavel,String loteIni,String loteFin,
	String serieIni,String serieFin,LocalDate dataSaldo,Integer detalhamento,Boolean pedidoVenda,Boolean pedidoCompra,Boolean naoImprimirSaldoZero, List<TableMap> dados){
		List<Abm01> abm01s = buscarItens(mps,itemIni,itemFin,itemInventariavel,itemNaoInventariavel);

		for(abm01 in abm01s){
			BigDecimal saldoItemEntrada = buscarSaldoPorLancamentoItem(abm01.abm01id, dataSaldo,0);
			BigDecimal saldoItemSaida = buscarSaldoPorLancamentoItem(abm01.abm01id, dataSaldo,1);
			BigDecimal saldoAtual = saldoItemEntrada - saldoItemSaida

			if(saldoAtual <= 0 && naoImprimirSaldoZero ) continue;

			TableMap itemDetalhado = buscarItemDetalhado(abm01.abm01id,saldoAtual,detalhamento);
			
			if(itemDetalhado.size() > 0){
				BigDecimal totCompra = pedidoCompra ? obterValorTotalCompraVenda(abm01.abm01id, dataSaldo, 0) : new BigDecimal(0);
				BigDecimal totVenda = pedidoVenda ? obterValorTotalCompraVenda(abm01.abm01id, dataSaldo, 1) : new BigDecimal(0);
	
				itemDetalhado.putAll(itemDetalhado);
				
				itemDetalhado.put("saldo",saldoAtual);
				itemDetalhado.put("totCompra",totCompra);
				itemDetalhado.put("totVenda",totVenda);
				itemDetalhado.put("excesso",saldoAtual - totVenda + totCompra  );

				dados.add(itemDetalhado)
			}	
		}
		
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

	

	private BigDecimal buscarSaldoPorLancamentoItem(Long abm01id, LocalDate data, Integer movimentacao){
		
		String sql = "select coalesce(sum(bcc01qt),0) as qtd "+
					"from bcc01 "+
					"inner join abm01 on abm01id = bcc01item "+
					"inner join abm0101 on abm0101item = abm01id "+
					"inner join aam06 on aam06id = abm01umu "+
					"where abm01id = :abm01id "+
					"and bcc01data <= :data " +
					"and  bcc01mov = :movimentacao "

					
		Parametro p1 = Parametro.criar("abm01id",abm01id);
		Parametro p2 = Parametro.criar("data",data);
		Parametro p3 = Parametro.criar("movimentacao",movimentacao);
		
		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3);
	}

	private BigDecimal obterValorTotalCompraVenda(Long abm01id,LocalDate data, Integer compraVenda){
		String sql = "select coalesce(sum(eaa01totDoc),0) from eaa01 "+
					"inner join eaa0103 on eaa0103doc = eaa01id "+
					"inner join abb01 on abb01id = eaa01central "+
					"where eaa0103item = :abm01id "+
					"and abb01data <= :data "+
					"and eaa01esMov = :compraVenda "
					
		Parametro p1 = Parametro.criar("abm01id",abm01id);
		Parametro p2 = Parametro.criar("data",data);
		Parametro p3 = Parametro.criar("compraVenda",compraVenda);
		
		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3);
	}

	private buscarItemDetalhado(Long idItem,BigDecimal saldoAtual,Integer detalhamento){
		
		String whereDetalhado = detalhamento == 0 ? "and abm0101estMax < :saldoAtual " : detalhamento == 1 ? "and abm0101estMin > :saldoAtual " : "and abm0101ptoPed >= :saldoAtual and abm0101estMin <= :saldoAtual  "
		
		String sql = "select abm01codigo as codItem, case when abm01tipo = 0 then 'M' else 'P' end as mps, abm01na as naItem, abm0101estMax, "+
				    "abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo "+
				    "from abm01 "+
				   "inner join abm0101 on abm0101item = abm01id "+ 
				   "inner join aam06 on aam06id = abm01umu "+
				   "where abm01id = :idItem "+
				   whereDetalhado 
				   
		Parametro p1 = Parametro.criar("idItem",idItem);
		Parametro p2 = Parametro.criar("saldoAtual",saldoAtual);
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2)
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9