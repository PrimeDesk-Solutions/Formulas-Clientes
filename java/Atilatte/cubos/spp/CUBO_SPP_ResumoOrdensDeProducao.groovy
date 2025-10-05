package Atilatte.cubos.spp;
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

public class CUBO_SPP_ResumoOrdensDeProducao extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CUBO - SPP - Resumo das Ordens de Produção";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("numeroInicial",1,
				"numeroFinal", 99999999, "impressao", "0")
	}
	@Override
	public DadosParaDownload executar() {
		Integer numIni = getInteger("numeroInicial");
		Integer numFin = getInteger("numeroFinal");
		List<Long> idTipodoc = getListLong("tipoDoc");
		LocalDate[] dtEmissao = getIntervaloDatas("data");
		List<Long> prodAcabado = getListLong("prodAcabado");
		List<Long> idsComponentes = getListLong("componentes");
		Boolean exibirComponente = getBoolean("exibirComponente");
		Integer impressao = getInteger("impressao");


		List<TableMap> dados = buscarDocumentosProducao(numIni,numFin,idTipodoc, dtEmissao,prodAcabado,idsComponentes,exibirComponente, impressao);

		if(exibirComponente) return gerarXLSX("ProducaoResumoOrdensDeProducaoComponentes", dados);

		return gerarXLSX("ProducaoResumoOrdensDeProducao", dados);

	}

	private List<TableMap>buscarDocumentosProducao(Integer numIni,Integer numFin,List<Long>idTipodoc,LocalDate[] dtEmissao, List<Long>prodAcabado, List<Long> idsComponentes, Boolean exibirComponente, Integer impressao ){


		//Datas de Emissão Inicial e Final
		LocalDate dataEmissIni = null;
		LocalDate dataEmissFin = null;

		if(dtEmissao != null){
			dataEmissIni = dtEmissao[0];
			dataEmissFin = dtEmissao[1];
		}

		String whereTipoDoc = idTipodoc != null && idTipodoc.size() > 0 ? "and aah01id in (:idTipodoc) " : "";
		String whereDtEmiss = dataEmissIni != null && dataEmissFin != null ? "and abb01ordem.abb01data between :dataEmissIni and :dataEmissFin " : "";
		String whereProdAcab = prodAcabado != null && prodAcabado.size() > 0 ? "and itemPlano.abm01id in (:prodAcabado) " : ""
		String whereComponentes = idsComponentes != null && idsComponentes.size() > 0 ? "and itemComp.abm01id in (:idsComponentes) " : ""
		String groupBy = exibirComponente ? "order by abb01ordem.abb01num,itemPlano.abm01codigo, itemComp.abm01codigo " : "order by abb01ordem.abb01num "
		String whereDescrPlano = impressao == 0 ? "AND UPPER(BAA01DESCR) like '%ENV%'" : impressao == 1 ? "AND UPPER(BAA01DESCR) like '%FAB%'" : "";

		Parametro parametroNumDocIni = Parametro.criar("numIni",numIni);
		Parametro parametroNumDocFin = Parametro.criar("numFin",numFin);
		Parametro ParametroTipoDoc = idTipodoc != null && idTipodoc.size() > 0 ? Parametro.criar("idTipodoc",idTipodoc) : null;
		Parametro ParametroDtEmissIni = dataEmissIni != null && dataEmissFin != null ? Parametro.criar("dataEmissIni",dataEmissIni) : null;
		Parametro ParametroDtEmissFin = dataEmissIni != null && dataEmissFin != null ? Parametro.criar("dataEmissFin",dataEmissFin) : null;
		Parametro ParametroProdAcab = prodAcabado != null && prodAcabado.size() > 0 ? Parametro.criar("prodAcabado",prodAcabado) : null;
		Parametro ParametroComponentes = idsComponentes != null && idsComponentes.size() > 0 ? Parametro.criar("idsComponentes",idsComponentes) : null;

		String sql = "select  abb01ordem.abb01num as numOrdem, itemPlano.abm01codigo as codigoProdFinal, " +
				"itemPlano.abm01na as naProdFinal, abb01plano.abb01num as numPlano,baa01descr as descrPlano, " +
				"abb01plano.abb01data as dataPlano,bab01dte as dataEntrega, " +
				"bab01ctdtf as dataFim,bab01ctdti as dataInicio, bab01lote as lote,bab01serie as serie, " +
				"bab01status as status,baa0101ap as qtdProduzirAcab,bab01qtp as qtdProduzidaAcab " +
				"from bab01 " +
				"inner join abb01 as abb01ordem on abb01ordem.abb01id = bab01central " +
				"inner join abp20 on abp20id = bab01comp " +
				"inner join abm01 as itemPlano on itemPlano.abm01id = abp20item " +
				"inner join bab0103 on bab0103op = bab01id " +
				"inner join baa0101 on baa0101id = bab0103itempp " +
				"inner join baa01 on baa01id = baa0101plano " +
				"inner join abb01 as abb01plano on abb01plano.abb01id = baa01central " +
				"inner join aah01 on aah01id = abb01ordem.abb01tipo " +
				"where true " +
				"and abb01ordem.abb01num between :numIni and :numFin "+
				whereTipoDoc+
				whereDtEmiss +
				whereProdAcab +
				whereDescrPlano+
				groupBy;


		if(exibirComponente) sql = "select distinct abb01ordem.abb01num as numOrdem, itemPlano.abm01codigo as codigoProdFinal,itemPlano.abm01na as naProdFinal,  "+
				"abb01plano.abb01num as numPlano,baa01descr as descrPlano, abb01plano.abb01data as dataPlano,bab01dte as dataEntrega,bab01ctdtf as dataFim,bab01ctdti as dataInicio,  "+
				"bab01lote as lote,bab01serie as serie, bab01status as status,baa0101ap as qtdProduzirAcab,bab01qtp as qtdProduzidaAcab,bab01ctcc as custoProdutoFinal, "+
				"itemComp.abm01codigo as codComp,itemComp.abm01na as naComp,bab01011validade as validadeComp,bab01011lote as loteComp,bab01011serie as serieComp, bab01011fabric as fabricComp, "+
				"bab0101qtp as qtdPlanejada, bab0101qta as qtdAplic, bab0101custo as custoComp "+
				"from bab01  "+
				"inner join abb01 as abb01ordem on abb01ordem.abb01id = bab01central  "+
				"inner join abp20 on abp20id = bab01comp  "+
				"inner join abm01 as itemPlano on itemPlano.abm01id = abp20item  "+
				"inner join bab0103 on bab0103op = bab01id  "+
				"inner join baa0101 on baa0101id = bab0103itempp  "+
				"inner join baa01 on baa01id = baa0101plano  "+
				"inner join abb01 as abb01plano on abb01plano.abb01id = baa01central  "+
				"inner join aah01 on aah01id = abb01ordem.abb01tipo  "+
				"inner join bab0101 on bab0101op = bab01id "+
				"inner join abm01 as itemComp on itemComp.abm01id = bab0101item  "+
				"inner join bab01011 on bab01011comp = bab0101id "+
				"where true "+
				"and abb01ordem.abb01num between :numIni and :numFin "+
				whereTipoDoc+
				whereDtEmiss +
				whereProdAcab +
				whereComponentes+
				whereDescrPlano +
				groupBy;

		return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, ParametroTipoDoc, ParametroDtEmissIni, ParametroDtEmissFin, ParametroProdAcab, ParametroComponentes)
	}


}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUFAgLSBSZXN1bW8gZGFzIE9yZGVucyBkZSBQcm9kdcOnw6NvIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUFAgLSBSZXN1bW8gZGFzIE9yZGVucyBkZSBQcm9kdcOnw6NvIiwidGlwbyI6InJlbGF0b3JpbyJ9