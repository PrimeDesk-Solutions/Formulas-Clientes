package multitec.relatorios.sce;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abm01
import sam.model.entities.bc.Bcc01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;
import sam.server.samdev.utils.Parametro

public class SCE_MovimentacaoFisica extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCE - Movimentação Física";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", datas);
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Integer> mpms = getListInteger("mpms");
		String itemIni = getString("itemIni");
		String itemFim = getString("itemFim");
		LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
		List<Long> idsStatus = getListLong("status");
		List<Long> criterios = getListLong("criterios");
		Aac10 aac10 = getVariaveis().getAac10();
		boolean naoImprimirItensSemMovSaldoZerado = getBoolean("naoImprimirItensSemMovSaldoZerado");

		params.put("TITULO_RELATORIO", "Movimentação Física");
		params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		params.put("EMPRESA", aac10.getAac10codigo() + "-" + aac10.getAac10na());

		List<TableMap> dados = buscarDadosRelatorioAnalitico(mpms, itemIni, itemFim, dataPeriodo, idsStatus, criterios, naoImprimirItensSemMovSaldoZerado);
		return gerarPDF("SCE_MovimentacaoFisica", dados);
	}

	private List<TableMap> buscarDadosRelatorioAnalitico(List<Integer> mpms, String itemIni, String itemFim, LocalDate[] dataPeriodo, List<Long> idsStatus, List<Long> criterios, boolean naoImprimirItensSemMovSaldoZerado) {
		List<TableMap> mapDados = new ArrayList();
		
		List<TableMap> listItens = buscarItens(mpms, itemIni, itemFim, criterios);
		if(listItens.size() > 0) {
			for(int i = 0; i < listItens.size(); i++) {
				Long abm01id = listItens.get(i).getLong("abm01id");
				BigDecimal saldoAtual = new BigDecimal(0);
				BigDecimal saldoAnterior = buscarSaldoAnteriorPorCodigoData(abm01id, dataPeriodo[0], idsStatus);
				saldoAtual = saldoAnterior;
								
				List<TableMap> listLancamentos = buscarLancamentos(abm01id, dataPeriodo, idsStatus);
				if(listLancamentos.size() == 0) {
					if(naoImprimirItensSemMovSaldoZerado) {
						if(saldoAnterior == 0) continue;
					}
					
					TableMap tmLcto = new TableMap();
					BigDecimal entradaSaida = new BigDecimal(0);
					
					tmLcto.put("mpm", listItens.get(i).getInteger("abm01tipo") == 0 ? "M" : listItens.get(i).getInteger("abm01tipo") == 1 ? "P" : "Me");
					tmLcto.put("codigo", listItens.get(i).getString("abm01codigo"));
					tmLcto.put("descricao", listItens.get(i).getString("abm01descr"));
					tmLcto.put("unid", listItens.get(i).getString("aam06codigo"));
					tmLcto.put("abc", listItens.get(i).getString("abm0101abc"));
					
					String aam01codigo = listItens.get(i).getString("aam01codigo") ?: "";
					String aam01descr = listItens.get(i).getString("aam01descr") ?: "";
					tmLcto.put("classe", aam01codigo + "-" + aam01descr);
					
					tmLcto.put("entradaSaida", entradaSaida);
					tmLcto.put("saldoAtual", saldoAtual);
					tmLcto.put("saldoAnterior", saldoAnterior);
					mapDados.add(tmLcto);
				}else {
					for(int j = 0; j < listLancamentos.size(); j++) {
						TableMap tmLcto = new TableMap();
						
						BigDecimal entradaSaida = listLancamentos.get(j).getBigDecimal("bcc01qtPS");
						saldoAtual = saldoAtual + entradaSaida;
						
						tmLcto.put("mpm", listItens.get(i).getInteger("abm01tipo") == 0 ? "M" : listItens.get(i).getInteger("abm01tipo") == 1 ? "P" : "Me");
						tmLcto.put("codigo", listItens.get(i).getString("abm01codigo"));
						tmLcto.put("descricao", listItens.get(i).getString("abm01descr"));
						tmLcto.put("unid", listItens.get(i).getString("aam06codigo"));
						tmLcto.put("abc", listItens.get(i).getString("abm0101abc"));
						
						String aam01codigo = listItens.get(i).getString("aam01codigo") ?: "";
						String aam01descr = listItens.get(i).getString("aam01descr") ?: "";
						tmLcto.put("classe", aam01codigo + "-" + aam01descr);
						
						tmLcto.put("data", listLancamentos.get(j).getDate("bcc01data"));
						
						String abm20codigo = listLancamentos.get(j).getString("abm20codigo") ?: "";
						String abm20descr = listLancamentos.get(j).getString("abm20descr") ?: "";
						tmLcto.put("movimentacao", abm20codigo +"-"+ abm20descr);
						
						tmLcto.put("status", listLancamentos.get(j).getString("aam04codigo"));
						tmLcto.put("ctrl0", listLancamentos.get(j).getString("ctrl0"));
						tmLcto.put("ctrl1", listLancamentos.get(j).getString("ctrl1"));
						tmLcto.put("ctrl2", listLancamentos.get(j).getString("ctrl2"));
						tmLcto.put("lote", listLancamentos.get(j).getString("bcc01lote"));
						tmLcto.put("serie", listLancamentos.get(j).getString("bcc01serie"));
						tmLcto.put("numDoc3", listLancamentos.get(j).getInteger("numDoc3"));
						
						String aah01na = listLancamentos.get(j).getString("aah01na") ?: "";
						String aah01cod = listLancamentos.get(j).getString("aah01codigo") ?: "";
						tmLcto.put("documento", aah01cod +" "+ aah01na);
						
						tmLcto.put("numero", listLancamentos.get(j).getInteger("numDoc"));
						tmLcto.put("entidade", listLancamentos.get(j).getString("codEnt"));
						tmLcto.put("entradaSaida", entradaSaida);
						tmLcto.put("saldoAtual", saldoAtual);
						tmLcto.put("saldoAnterior", saldoAnterior);
						mapDados.add(tmLcto);
					}
				}
			}
		}

		return mapDados;
	}
	
	private List<TableMap> buscarItens(List<Integer> mpms, String itemIni, String itemFim, List<Long> criterios){
		String whereTipos = (!mpms.contains(-1)) ? "AND abm01tipo IN (:mpm) " : "";
		String whereItens = itemIni != null && itemFim != null ? "AND abm01codigo BETWEEN :itemIni AND :itemFim " : itemIni != null && itemFim == null ? " AND abm01codigo >= :itemIni " : itemIni == null && itemFim != null ? " AND abm01codigo <= :itemFim " : "";
		String whereCriterio = criterios != null && criterios.size() > 0 ? "AND abm0102criterio IN (:criterios) " : "";

		String sql = " SELECT abm01id, abm01tipo, abm01na, abm01codigo, abm01descr, aam06codigo, abm0101abc, aam01codigo, aam01descr " +
					 " FROM Abm01 " +
					 " LEFT JOIN Aam06 ON aam06id = abm01umu" +
					 " LEFT JOIN Abm0101 ON abm0101id = abm01id" +
					 " LEFT JOIN Aam01 ON aam01id = abm01classe" +
					 " LEFT JOIN Abm0102 ON abm0102item = abm01id" +
					   getSamWhere().getWherePadrao("WHERE", Abm01.class) +
					 " AND abm01grupo = " + Abm01.NAO +
					   whereTipos + whereItens + whereCriterio + 
					 " ORDER BY abm01tipo, abm01codigo";
					 
		Parametro paramMpm = mpms != null && mpms.size() > 0 ? Parametro.criar("mpm", mpms) : null;
		Parametro paramItemIni = itemIni != null ? Parametro.criar("itemIni", itemIni) : null;
		Parametro paramItemFim = itemFim != null ? Parametro.criar("itemFim", itemFim) : null;
		Parametro paramCriterio = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios", criterios) : null;

		return getAcessoAoBanco().buscarListaDeTableMap(sql, paramMpm, paramItemIni, paramItemFim, paramCriterio);
	}

	private List<TableMap> buscarLancamentos(Long abm01id, LocalDate[] periodo, List<Long> idsStatus){
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND bcc01status IN (:idsStatus) " : "";
		String whereData = periodo != null ? getWhereDataInterval("AND", periodo, "bcc01data") : "";

		String sql = " SELECT bcc01data, " +
					 " abm20codigo, abm20descr, aam04codigo, ctrl0.abm15nome AS ctrl0, ctrl1.abm15nome AS ctrl1, ctrl2.abm15nome AS ctrl2, bcc01lote, " +
					 " bcc01serie, doc3.abb01num AS numDoc3, ent3.abe01codigo AS ent3, doc.abb01num AS numDoc, aah01.aah01codigo, aah01.aah01na, " +
					 " aah01.aah01nome, ent.abe01codigo AS codEnt, ent.abe01codigo AS codEnt, bcc01qt, bcc01qtPS " +
					 " FROM Bcc01 " +
					 " INNER JOIN Abm20 ON abm20id = bcc01ple" + 
					 " INNER JOIN Aam04 ON aam04id = bcc01status" +
					 " LEFT JOIN Abm15 AS ctrl0 ON ctrl0.abm15id = bcc01ctrl0" + 
					 " LEFT JOIN Abm15 AS ctrl1 ON ctrl1.abm15id = bcc01ctrl1" +
					 " LEFT JOIN Abm15 AS ctrl2 ON ctrl2.abm15id = bcc01ctrl2 " +
					 " LEFT JOIN Abb01 AS doc3 ON doc3.abb01id = bcc01centralEst " +
					 " LEFT JOIN Abe01 AS ent3 ON ent3.abe01id = doc3.abb01ent" +
					 " LEFT JOIN Abb01 AS doc ON doc.abb01id = bcc01central" + 
					 " LEFT JOIN Abe01 AS ent ON ent.abe01id = doc.abb01ent" +
					 " LEFT JOIN aah01 AS aah01 ON aah01id = doc.abb01tipo" +
					   getSamWhere().getWherePadrao("WHERE", Bcc01.class) +
					 " AND bcc01item = " + abm01id + 
					   whereStatus + whereData +
					 " ORDER BY bcc01data, bcc01id";
					 
		Parametro paramStatus = idsStatus != null && idsStatus.size() > 0 ? Parametro.criar("idsStatus", idsStatus) : null;

		return getAcessoAoBanco().buscarListaDeTableMap(sql, paramStatus);
	}

	private BigDecimal buscarSaldoAnteriorPorCodigoData(Long abm01id, LocalDate dataInicial, List<Long> idsStatus) {
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? " AND bcc01status IN (:idsStatus) " : "";
		
		String sql = " SELECT COALESCE(SUM(bcc01qtps), 0) " +
	                 " FROM Bcc01 " +
				     " INNER JOIN Abb01 ON Abb01id = bcc01central " +
				     " INNER JOIN Abm01 ON abm01id = bcc01item " +
				     " WHERE abm01id = :abm01id " +
					 " AND bcc01data >= :dataInicial " +
				       getSamWhere().getWherePadrao("AND", Bcc01.class);

		Query query = getSession().createQuery(sql);
		query.setParameter("abm01id", abm01id);
		query.setParameter("dataInicial", dataInicial);
		if(idsStatus != null && idsStatus.size() > 0) query.setParameter("idsStatus", idsStatus);

		BigDecimal saldoPeriodo = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		BigDecimal saldoAtual = buscarSaldoAtualItem(abm01id, idsStatus);
		
		return saldoAtual.subtract(saldoPeriodo);
	}

	private BigDecimal buscarSaldoAtualItem(Long abm01id, List<Long> idsStatus) {
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? " AND bcc02status IN (:idsStatus) " : "";

		String sql = " SELECT COALESCE(SUM(bcc02qt), 0) FROM Bcc02 " +
				  " WHERE bcc02item = :abm01id " +
				    whereStatus + obterWherePadrao("Bcc02");

		Parametro p1 = criarParametroSql("abm01id", abm01id);
		Parametro p2 = idsStatus != null && idsStatus.size() > 0 ? criarParametroSql("idsStatus", idsStatus) : null;

		return getAcessoAoBanco().obterBigDecimal(sql, p1, p2);
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIE1vdmltZW50YcOnw6NvIEbDrXNpY2EiLCJ0aXBvIjoicmVsYXRvcmlvIn0=