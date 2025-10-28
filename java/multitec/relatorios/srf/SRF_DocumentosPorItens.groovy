package multitec.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SRF_DocumentosPorItens extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SRF - Documentos Por Itens";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("resumoOperacao", "0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		
		List<Long> idEntidade = getListLong("entidade");
		List<Long> idTipoDocumento = getListLong("tipo");
		List<Long> criterios = getListLong("criterios");
		List<Long> criteriosItem = getListLong("criteriosItem");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntSai = getIntervaloDatas("dataEntSai");
		Integer resumoOperacao = getInteger("resumoOperacao");
		List<Integer> mpm = getListInteger("mpm");
		List<Long> idsItens = getListLong("itens");

		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		if (resumoOperacao.equals(1)) {
			params.put("TITULO_RELATORIO", "Documentos Por Itens - Faturamento");
		} else {
			params.put("TITULO_RELATORIO", "Documentos Por Itens - Recebimento");
		}

		if (dataEmissao != null) {
			params.put("PERIODO", "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		} else {
			if (dataEntSai != null) {
				params.put("PERIODO", "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
			}
		}

		List<TableMap> dados = obterDadosRelatorio(idEntidade, idTipoDocumento, criterios, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, criteriosItem, mpm, idsItens);

		return gerarPDF("SRF_DocumentosPorItens", dados)
	}
	
	public List<TableMap> obterDadosRelatorio (List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> criterios, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial, Integer numeroFinal, Integer resumoOperacao, 
		List<Long> criteriosItem, List<Integer> mpm, List<Long> idsItens)  {
			
		String whereDataEmissao = "";
		if (dataEmissao != null) {
			whereDataEmissao = dataEmissao[0] != null && dataEmissao[1] != null ? " and abb01.abb01data >= '" + dataEmissao[0] + "' and abb01.abb01data <= '" + dataEmissao[1] + "'": "";
		}
		
		String whereDataEntSai = "";
		if (dataEntSai != null) {
			whereDataEntSai = dataEntSai[0] != null && dataEntSai[1] != null ? " and eaa01.eaa01esData >= '" + dataEntSai[0] + "' and eaa01.eaa01esData <= '" + dataEntSai[1] + "'": "";
		}
		
		if(whereDataEmissao.length() == 0 && whereDataEntSai.length() == 0) {
			throw new ValidacaoException("Não foram informadas datas de emissão e/ou entrada/saída.");
		}
		
		String whereClassDoc = " and eaa01clasDoc = " + Eaa01.CLASDOC_SRF
		
		String whereResumoOperacao = null;	
		if (resumoOperacao.equals(0)) {
			whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_ENTRADA;	
		} else {
			whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_SAIDA;
		}
		
		String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= " + numeroInicial + " and abb01.abb01num <= " + numeroFinal + "": "";
		String whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01.aah01id IN (:idTipoDocumento)": "";
		String whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01.abe01id IN (:idEntidade)": "";
		String whereCriterio = criterios != null && criterios.size() > 0 ? " and abm0102.abm0102criterio IN (:criterios) " : "";
		
		String whereCriterioItem = criteriosItem != null && criteriosItem.size() > 0 ? " and abm0102.abm0102item IN (:criteriosItem) " : "";
		String whereTipos = (!mpm.contains(-1)) ? " AND abm01tipo IN (:mpm) " : "";
		String whereItens = idsItens != null && idsItens.size() > 0 ? " AND abm01id IN (:idsItens) " : "";
		
		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null;
		Parametro parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null;
		Parametro parametroCriterios = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios", criterios) : null;
		
		Parametro parametroCriterioItem = criteriosItem != null && criteriosItem.size() > 0 ? Parametro.criar("criteriosItem", criteriosItem) : null;
		Parametro parametroTipos = (!mpm.contains(-1)) ? Parametro.criar("mpm", mpm) : null;
		Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
		

		String sql = " SELECT eaa0103.eaa0103codigo, eaa0103.eaa0103descr, aam06.aam06codigo, eaa0103.eaa0103tipo, eaa0103.eaa0103doc, " +
				" abb01.abb01num, abb01.abb01data, eaa01.eaa01esdata, aaj15.aaj15codigo, eaa0103.eaa0103pcSeq, eaa0103.eaa0103pcNum, " +
				" abe01doc.abe01codigo as abe01codigoDoc, abe01doc.abe01na as abe01naDoc, " +
				" abe01rep.abe01codigo as abe01codigoRep, " +
				" abe30.abe30nome, abe40.abe40nome, " +
				" eaa0103.eaa0103qtUso, eaa0103.eaa0103unit, eaa0103.eaa0103totDoc " +
				" FROM Eaa0103 eaa0103 " +
				" INNER JOIN Eaa01 eaa01 ON eaa01.eaa01id = eaa0103.eaa0103doc " +
				" INNER JOIN Abb01 abb01 ON abb01.abb01id = eaa01.eaa01central " +
				" INNER JOIN Abm01 abm01 ON abm01.abm01id = eaa0103.eaa0103item " +
				" LEFT JOIN Abm0102 abm0102 ON abm0102.abm0102item = abm01.abm01id " +
				" LEFT JOIN Aah01 aah01 ON aah01.aah01id = abb01.abb01tipo " +
				" LEFT JOIN Abe01 abe01doc ON abe01doc.abe01id = abb01.abb01ent " +
				" LEFT JOIN Abe30 abe30 ON abe30id = eaa01.eaa01cp " +
				" LEFT JOIN Abe40 abe40 ON abe40id = eaa01.eaa01tp " +
				" LEFT JOIN Aam06 aam06 ON aam06.aam06id = eaa0103.eaa0103umu " +
				" LEFT JOIN Aaj15 aaj15 ON aaj15.aaj15id = eaa0103.eaa0103cfop " +
				" LEFT JOIN Abe01 abe01rep ON abe01rep.abe01id = eaa01.eaa01rep0 " +
				getSamWhere().getWherePadrao(" WHERE ", Eaa01.class) +
				whereNumero +
				whereTipoDocumento +
				whereIdEntidade +
				whereCriterio +
				whereCriterioItem +
				whereDataEmissao +
				whereDataEntSai +
				whereResumoOperacao +
				whereClassDoc +
				whereTipos +
				whereItens + 
				" ORDER BY eaa0103.eaa0103tipo, eaa0103.eaa0103codigo, abb01.abb01num";

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento, parametroCriterios, parametroCriterioItem, parametroTipos, parametroItens);
		if(receberDadosRelatorio == null || receberDadosRelatorio.size() == 0) {
			throw new ValidacaoException("Não foram encontrados registros a partir do filtro informado.");
		}
		 
		return receberDadosRelatorio;
	}
	
	
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgUG9yIEl0ZW5zIiwidGlwbyI6InJlbGF0b3JpbyJ9