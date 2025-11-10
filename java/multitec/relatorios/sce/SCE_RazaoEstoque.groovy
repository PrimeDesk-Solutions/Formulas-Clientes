package multitec.relatorios.sce;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abm01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCE_RazaoEstoque extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCE - Razão Estoque";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("periodo", DateUtils.getStartAndEndMonth(MDate.date()));
		filtrosDefault.put("descrItem", "0");
		filtrosDefault.put("isInvent", true);
		filtrosDefault.put("isNoInvent", true);
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		LocalDate[] periodo = getIntervaloDatas("periodo");
		Integer descrItem = getInteger("descrItem");
		boolean isSemMovSaldoZero = get("isSemMovSaldoZero");
		List<Long> idsLocal = getListLong("local");
		List<Long> idsStatus = getListLong("status");
		List<Integer> tipoItem = getListInteger("tipoItem");
		String itemIni = getString("itemIni");
		String itemFim = getString("itemFim");
		List<Long> critSelec = getListLong("critSelec");
		boolean isInvent = get("isInvent");
		boolean isNoInvent = get("isNoInvent");

		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Razão do Estoque");
		params.put("PERIODO", "Período: " + periodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + periodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

		List<TableMap> dados = obterDadosRelatorio(periodo, descrItem, isSemMovSaldoZero, idsLocal, idsStatus, tipoItem, itemIni, itemFim, critSelec, isInvent, isNoInvent);
		
		return gerarPDF("SCE_RazaoEstoque", dados);
	}

	public List<TableMap> obterDadosRelatorio(LocalDate[] periodo, Integer descrItem, boolean isSemMovSaldoZero, List<Long> idsLocal, List<Long> idsStatus, List<Integer> tipoItem, String itemIni, String itemFim, List<Long> critSelec, boolean isInvent, boolean isNoInvent) {
		List<TableMap> dados = new ArrayList();
		List<Abm01> abm01s = buscarItensParaRazaoEstoque(tipoItem, itemIni, itemFim, critSelec, isInvent, isNoInvent); // TODO
		for(abm01 in abm01s) {
			List<TableMap> lctos = buscarLancamentosItemPorPeriodo(abm01.abm01id, periodo, descrItem, idsLocal, idsStatus);
			if(lctos.size() == 0) {
				TableMap item = new TableMap();
				item.put("abm01tipo", abm01.abm01tipo);
				item.put("abm01codigo", abm01.abm01codigo);
				item.put("nomeItem", descrItem == 0 ? abm01.abm01na : abm01.abm01descr);
				item.put("aam06codigo", abm01.abm01umu != null ? abm01.abm01umu.aam06codigo : null);
				
				BigDecimal saldoAnteriorEnt = buscarQuantidadeSandoAnteriorPeriodoItem(periodo, abm01.abm01id, 0);
				BigDecimal saldoAnteriorSai = buscarQuantidadeSandoAnteriorPeriodoItem(periodo, abm01.abm01id, 1);
				BigDecimal saldoAnterior = saldoAnteriorEnt - saldoAnteriorSai;
				
				if(isSemMovSaldoZero && saldoAnterior <= 0) continue;
				
				item.put("qtdSaldoAnt", saldoAnterior);
				
				BigDecimal valorAnteriorEnt = buscarValorSaldoAnteriorPeriodoItem(periodo, abm01.abm01id, 0);
				BigDecimal valorAnteriorSai = buscarValorSaldoAnteriorPeriodoItem(periodo, abm01.abm01id, 1);
				BigDecimal valorAnterior = valorAnteriorEnt + valorAnteriorSai;
				item.put("vlrSaldoAnt", valorAnterior);
				
				BigDecimal medioSaldoAnterior = buscarPrecoMedioUnitarioAnterior(periodo, abm01.abm01id);
				item.put("medSaldoAnt", medioSaldoAnterior);
				
				dados.add(item);
				
			}else if(lctos.size() > 0) {
				BigDecimal saldoAnteriorEnt = buscarQuantidadeSandoAnteriorPeriodoItem(periodo, abm01.abm01id, 0);
				BigDecimal saldoAnteriorSai = buscarQuantidadeSandoAnteriorPeriodoItem(periodo, abm01.abm01id, 1);
				BigDecimal saldoAnterior = saldoAnteriorEnt - saldoAnteriorSai;
				
				BigDecimal valorAnteriorEnt = buscarValorSaldoAnteriorPeriodoItem(periodo, abm01.abm01id, 0);
				BigDecimal valorAnteriorSai = buscarValorSaldoAnteriorPeriodoItem(periodo, abm01.abm01id, 1);
				BigDecimal valorAnterior = valorAnteriorEnt + valorAnteriorSai;
				
				BigDecimal medioSaldoAnterior = buscarPrecoMedioUnitarioAnterior(periodo, abm01.abm01id);
				
				for(lcto in lctos) {
					BigDecimal saldo = 0;
					
					lcto.put("qtdSaldoAnt", saldoAnterior);
					lcto.put("vlrSaldoAnt", valorAnterior);
					lcto.put("medSaldoAnt", medioSaldoAnterior);
					
					BigDecimal qtdSaldo = 0;
					BigDecimal valSaldo = 0;
					qtdSaldo = saldoAnterior + lcto.getBigDecimal_Zero("bcc01qtPS");
					if(lcto.getInteger("abm20mov") == 0) {
						valSaldo = valorAnterior + lcto.getBigDecimal_Zero("bcc01custo");
					} else {
						valSaldo = valorAnterior - lcto.getBigDecimal_Zero("bcc01custo");
					}
					saldoAnterior = qtdSaldo;
					valorAnterior = valSaldo;
					lcto.put("qtdSaldo", qtdSaldo);
					lcto.put("valSaldo", valSaldo);
					dados.add(lcto);
				}
			}
		}
		
		return dados;
	}
	
	private List<Abm01> buscarItensParaRazaoEstoque(List<Integer> tipoItem, String itemIni, String itemFim, List<Long> critSele, boolean isInvent, boolean isNoInvent) {
		Criterion critTipo = tipoItem != null && !tipoItem.contains(-1) ? Criterions.in("abm01tipo", tipoItem) : Criterions.isTrue();
		Criterion critItem = itemIni != null && itemFim != null ? Criterions.between("abm01codigo", itemIni, itemFim) : itemIni != null && itemFim == null ? Criterions.ge("abm01codigo", itemIni) : itemIni == null && itemFim != null ? Criterions.le("abm01codigo", itemIni) : Criterions.isTrue();
		Criterion critCrit = critSele != null && critSele.size() > 0 ? Criterions.in("abm0102criterio", critSele) : Criterions.isTrue();
		
		Criterion invent = Criterions.or(Criterions.isNotNull("abm11giProp"), Criterions.isNotNull("abm11giComTerc"), Criterions.isNotNull("abm11giDeTerc"));
		Criterion noInve = Criterions.and(Criterions.isNull("abm11giProp"), Criterions.isNull("abm11giComTerc"), Criterions.isNull("abm11giDeTerc"));
		Criterion critInve = isInvent && !isNoInvent ? invent : !isInvent && isNoInvent ? noInve : Criterions.isTrue();
		
		return getSession().createCriteria(Abm01.class)
						   .addJoin(Joins.fetch("abm01umu").left(true))
						   .addJoin(Joins.join("Abm0101", "abm0101item = abm01id"))
						   .addJoin(Joins.join("Abm11", "abm11id = abm0101estoque").left(true))
						   .addJoin(Joins.join("Abm0102", "abm0102item = abm01id").left(true))
						   .addWhere(Criterions.eq("abm01grupo", Abm01.NAO))
						   .addWhere(critTipo).addWhere(critItem).addWhere(critCrit)
						   .addWhere(critInve).setOrder("abm01tipo, abm01codigo").getList(ColumnType.ENTITY);
	}
	
	private List<TableMap> buscarLancamentosItemPorPeriodo(Long abm01id, LocalDate[] periodo, Integer descrItem, List<Long> idsLocal, List<Long> idsStatus) {
		String wherePeriodo = periodo != null && periodo.length > 0 ? " AND bcc01data BETWEEN :dtInicial AND :dtFinal " : ""
		String whereLocal = idsLocal != null && idsLocal.size() > 0 ? " AND (bcc01ctrl0 IN (:idsLocal) OR bcc01ctrl1 IN (:idsLocal) OR bcc01ctrl2 IN (:idsLocal))" : "";
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? " AND bcc01status IN (:idsStatus) " : "";

		String sql = " SELECT abm01tipo, abm01codigo, " + (descrItem == 0 ? "abm01na" : "abm01descr") + " AS nomeItem, aam06codigo, bcc01pmu, bcc01data, " + 
					 " abm20id, abm20mov, abm20codigo, abm20descr, aah01codigo, aah01na, abb01num, abb01data, bcc01qt, bcc01qtPS, bcc01custo " + 
					 " FROM Bcc01 " +
					 " INNER JOIN Abm01 ON abm01id = bcc01item " + 
					 " LEFT JOIN Aam06 ON aam06id = abm01umu " +
					 " INNER JOIN Abm20 ON abm20id = bcc01ple " +
					 " LEFT JOIN Abb01 ON abb01id = bcc01central " +
					 " LEFT JOIN Aah01 ON aah01id = abb01tipo " +
					 " WHERE abm01id = :abm01id " + 
					   wherePeriodo + whereLocal + 
					   whereStatus + obterWherePadrao("Bcc01") +
					 " ORDER BY bcc01data, bcc01mov";	
					
		Parametro paramPerIni = criarParametroSql("dtInicial", periodo[0]);
		Parametro paramPerFin = criarParametroSql("dtFinal", periodo[1]);
		Parametro paramItem = criarParametroSql("abm01id", abm01id);
		Parametro paramLocal = idsLocal != null && idsLocal.size() > 0 ? criarParametroSql("idsLocal", idsLocal) : null;
		Parametro paramStatus = idsStatus != null && idsStatus.size() > 0 ? criarParametroSql("idsStatus", idsStatus) : null 
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, paramPerIni, paramPerFin, paramItem, paramLocal, paramStatus);
	}
	
	private BigDecimal buscarQuantidadeSandoAnteriorPeriodoItem(LocalDate[] periodo, Long abm01id, Integer abm20mov) {
		String sql = " SELECT COALESCE(SUM(bcc01qt), 0) FROM Bcc01 " + 
					 " INNER JOIN Abm20 ON abm20id = bcc01ple" +
					 " WHERE bcc01item = :abm01id " +
					 " AND bcc01data < :dtInicial " +
					 " AND abm20mov = :abm20mov " +
					   obterWherePadrao("Bcc01");
					   
		Parametro paramItem = criarParametroSql("abm01id", abm01id);
		Parametro paramData = criarParametroSql("dtInicial", periodo[0]);
		Parametro paramMovi = criarParametroSql("abm20mov", abm20mov);
		
		return getAcessoAoBanco().obterBigDecimal(sql, paramItem, paramData, paramMovi);
	}
	
	private BigDecimal buscarValorSaldoAnteriorPeriodoItem(LocalDate[] periodo, Long abm01id, Integer abm20mov) {
		String sql = " SELECT COALESCE(SUM(bcc01custo), 0) FROM Bcc01 " +
					 " INNER JOIN Abm20 ON abm20id = bcc01ple" +
					 " WHERE bcc01item = :abm01id " +
					 " AND bcc01data < :dtInicial " +
					 " AND abm20mov = :abm20mov " +
					   obterWherePadrao("Bcc01");
					   
	   Parametro paramItem = criarParametroSql("abm01id", abm01id);
	   Parametro paramData = criarParametroSql("dtInicial", periodo[0]);
	   Parametro paramMovi = criarParametroSql("abm20mov", abm20mov);
	   
	   return getAcessoAoBanco().obterBigDecimal(sql, paramItem, paramData, paramMovi);
	}
	
	private BigDecimal buscarPrecoMedioUnitarioAnterior(LocalDate[] periodo, Long abm01id) {
		String sql = " SELECT bcc01pmu FROM Bcc01 " +
					 " INNER JOIN Abm20 ON abm20id = bcc01ple " +
					 " WHERE bcc01item = :abm01id " +
					 " AND bcc01data < :dtInicial "
					   obterWherePadrao("bcc01");
					   
	   Parametro paramData = criarParametroSql("dtInicial", periodo[0]);
	   Parametro paramItem = criarParametroSql("abm01id", abm01id);
	   
	   List<BigDecimal> pmus = getAcessoAoBanco().obterListaDeBigDecimal(sql, paramData, paramItem);
	   BigDecimal medioUni = 0;
	   if(pmus.size() > 0) {
		   for(pmu in pmus) medioUni += pmu;
		   medioUni = medioUni / pmus.size();
	   }
	   return medioUni;
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJhesOjbyBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9