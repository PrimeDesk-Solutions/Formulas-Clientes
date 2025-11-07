package Atilatte.relatorios.sce;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.bc.Bcc01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCE_Lancamentos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCE - Lançamentos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros(
				"movimentoEntSai", "2",
				"loteIni","",
				"loteFin","",
				"serieIni","",
				"serieFin","",
				"data", DateUtils.getStartAndEndMonth(MDate.date()),
				"impressao","0"
		);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idEntidade = getListLong("entidade");
		List<Long> idTipoDocumento = getListLong("tipo");
		List<Long> idPLE = getListLong("paramEstoque");
		LocalDate[] data = getIntervaloDatas("data");
		Integer movimentoEntSai = getInteger("movimentoEntSai");
		List<Integer> mpm = getListInteger("mpms");
		String itemIni = getString("itemIni");
		String itemFim = getString("itemFim");
		List<Long> criteriosItem = getListLong("criteriosItem");
		List<Long> idsStatus = getListLong("status");
		List<Long> idsLocal = getListLong("local");
		String loteIni = getString("loteIni");
		String loteFin = getString("loteFin");
		String serieIni = getString("serieIni");
		String serieFin = getString("serieFin");
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");

		Integer impressao = getInteger("impressao");

		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Lançamentos Por Item");
		if (data != null) {
			params.put("PERIODO", "Período: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		List<TableMap> dados = obterDadosRelatorio(idEntidade, idTipoDocumento, idPLE, data, movimentoEntSai, criteriosItem, mpm, itemIni, itemFim,idsStatus,idsLocal,loteIni,loteFin,serieIni,serieFin,numDocIni,numDocFin);

		if(impressao == 1) return gerarXLSX("SCE_Lancamentos_Excel", dados)
		return gerarPDF("SCE_Lancamentos_PDF", dados)
	}

	public List<TableMap> obterDadosRelatorio (List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> idPLE, LocalDate[] data, Integer movimentoEntSai, List<Long> criteriosItem, List<Integer> mpm, String itemIni, String itemFim,List<Long>idsStatus,List<Long>idsLocal,String loteIni,String loteFin,String serieIni,String serieFin, Integer numDocIni, Integer numDocFin) {

		String whereData = data != null && data[0] != null && data[1] != null ? " and bcc01.bcc01data >= '" + data[0] + "' and bcc01.bcc01data <= '" + data[1] + "'": "";
		String whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01.aah01id IN (:idTipoDocumento)": "";
		String whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " and ent3.abe01id IN (:idEntidade)": "";
		String whereidPLE = idPLE != null && idPLE.size() > 0 ? " and abm20.abm20id IN (:idPLE)": "";
		String whereCriterioItem = criteriosItem != null && criteriosItem.size() > 0 ? " and abm0102.abm0102item IN (:criteriosItem) " : "";
		String whereTipos = (!mpm.contains(-1)) ? " AND abm01tipo IN (:mpm) " : "";
		String whereItens = itemIni != null && itemFim != null ? " AND abm01codigo BETWEEN :itemIni AND :itemFim " : itemIni != null && itemFim == null ? " AND abm01codigo >= :itemIni " : itemIni == null && itemFim != null ? " AND abm01codigo <= :itemFim " : "";
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND aam04.aam04id in (:idsStatus) " : "";
		String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "AND abm15.abm15id in (:idsLocal) " : "";
		String whereLoteIni = !loteIni.isEmpty() ? "and bcc01lote >= :loteIni " : "";
		String whereLoteFin = !loteFin.isEmpty() ? "and bcc01lote <= :loteFin " : "";
		String whereSerieIni = !serieIni.isEmpty() ? "and bcc01serie >= :serieIni " : "";
		String whereSerieFin = !serieFin.isEmpty() ? "and bcc01serie <= :serieFin " : "";
		String whereNumDocIni = numDocIni != null ? "and doc.abb01num >= :numDocIni " : "";
		String whereNumDocFin = numDocFin != null ? "and doc.abb01num <= :numDocFin " : "";

		String whereMovimento = "";
		if (movimentoEntSai.equals(0)) {
			whereMovimento = " and bcc01.bcc01mov = " + Bcc01.MOV_ENTRADA;
		}

		if (movimentoEntSai.equals(1)) {
			whereMovimento = " and bcc01.bcc01mov = " + Bcc01.MOV_SAIDA;
		}

		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null;
		Parametro parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null;
		Parametro parametroidPLE = idPLE != null && idPLE.size() > 0 ? Parametro.criar("idPLE", idPLE) : null;
		Parametro parametroCriterioItem = criteriosItem != null && criteriosItem.size() > 0 ? Parametro.criar("criteriosItem", criteriosItem) : null;
		Parametro parametroTipos = (!mpm.contains(-1)) ? Parametro.criar("mpm", mpm) : null;
		Parametro parametroItemIni = itemIni != null ? criarParametroSql("itemIni", itemIni) : null;
		Parametro parametroItemFim = itemFim != null ? criarParametroSql("itemFim", itemFim) : null;
		Parametro parametroStatus = idsStatus != null && idsStatus.size() > 0 ? criarParametroSql("idsStatus", idsStatus) : null;
		Parametro parametroLocal = idsLocal != null && idsLocal.size() > 0 ? criarParametroSql("idsLocal", idsLocal) : null;
		Parametro parametroLoteIni = !loteIni.isEmpty() != null ? criarParametroSql("loteIni", loteIni) : null;
		Parametro parametroLoteFin = !loteFin.isEmpty() != null ? criarParametroSql("loteFin", loteFin) : null;
		Parametro parametroSerieIni = !serieIni.isEmpty() != null ? criarParametroSql("serieIni", serieIni) : null;
		Parametro parametroSerieFin = !serieFin.isEmpty() != null ? criarParametroSql("serieFin", serieFin) : null;
		Parametro parametroNumIni = numDocIni != null ? criarParametroSql("numDocIni", numDocIni) : null;
		Parametro parametroNumFin = numDocFin != null ? criarParametroSql("numDocFin", numDocFin) : null;

		String sql = " select aah01.aah01codigo,aah01.aah01na, aam04.aam04codigo, aam06.aam06codigo, abb11.abb11codigo, abm01.abm01codigo, " +
				"   abm01.abm01na, abm01.abm01tipo, abm20.abm20codigo, abm20.abm20descr," +
				"   bcc01.bcc01custo, bcc01.bcc01data, bcc01.bcc01pmu, bcc01.bcc01qt, bcc01.bcc01qtPS, bcc01.bcc01mov," +
				"   doc3.abb01num AS abb01numCE3, ent3.abe01codigo AS abe01codigoCE3, doc.abb01num AS abb01numCD,abm15.abm15nome, bcc01lote, bcc01serie, " +
				"   bcc01validade as validade, bcc01fabric as fabricacao, ent.abe01codigo as codEntidade, ent.abe01na as naEntidade "+
				"  from bcc01 bcc01" +
				"  left join aam04 aam04 on aam04.aam04id = bcc01.bcc01status " +
				"  left join abm01 abm01 on abm01.abm01id = bcc01.bcc01item " +
				"  left join abb11 abb11 on abb11.abb11id = bcc01.bcc01depto " +
				"  left join abm15 abm15 on abm15.abm15id = bcc01.bcc01ctrl0 " +
				"  left join aam06 aam06 on aam06id = abm01.abm01umu " +
				"  left join abm20 abm20 on abm20.abm20id = bcc01.bcc01ple " +
				"  left join abb01 as doc3 on doc3.abb01id = bcc01centralEst " +
				"  left join abe01 as ent3 on ent3.abe01id = doc3.abb01ent " +
				"  left join abb01 as doc on doc.abb01id = bcc01central " +
				"  left join abe01 as ent on ent.abe01id = doc.abb01ent " +
				"  left join aah01 aah01 on aah01id = doc.abb01tipo " +
				getSamWhere().getWherePadrao(" WHERE ", Bcc01.class) +
				whereData +
				whereTipoDocumento +
				whereIdEntidade +
				whereidPLE +
				whereCriterioItem +
				whereTipos +
				whereItens +
				whereMovimento +
				whereStatus +
				whereLocal +
				whereLoteIni +
				whereLoteFin +
				whereSerieIni +
				whereSerieFin +
				whereNumDocIni +
				whereNumDocFin +
				" order by abm01.abm01tipo, abm01.abm01codigo, bcc01.bcc01data, bcc01.bcc01id";


		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento, parametroidPLE, parametroCriterioItem, parametroTipos, parametroItemIni, parametroItemFim,parametroStatus,parametroLocal,parametroLoteIni,parametroLoteFin,parametroSerieIni,parametroSerieFin,parametroNumIni, parametroNumFin);
		return receberDadosRelatorio;
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIExhbmNhbWVudG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9