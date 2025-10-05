package multitec.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SRF_Documentos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SRF - Documentos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("resumoOperacao", "0");
		filtrosDefault.put("nomeEntidade", "0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		
		List<Long> idEntidade = getListLong("entidade");
		List<Long> idTipoDocumento = getListLong("tipo");
		List<Long> criterios = getListLong("criterios");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntSai = getIntervaloDatas("dataEntSai");
		Integer resumoOperacao = getInteger("resumoOperacao");
		Integer nomeEntidade = getInteger("nomeEntidade");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		if (resumoOperacao.equals(1)) {
			params.put("TITULO_RELATORIO", "Documentos - Faturamento");
		} else {
			params.put("TITULO_RELATORIO", "Documentos - Recebimento");
		}

		if (dataEmissao != null) {
			params.put("PERIODO", "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		
		if (dataEntSai != null) {
			params.put("PERIODO", "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		List<TableMap> dados = obterDadosRelatorio(idEntidade, idTipoDocumento, criterios, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, nomeEntidade);
			
		return gerarXLSX("SRF_Documentos_R1", dados);
		
	}
	
	public List<TableMap> obterDadosRelatorio (List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> criterios, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial, Integer numeroFinal, 
			Integer resumoOperacao, Integer nomeEntidade)  {
			
		String whereData = "";
		String data = "";
		if (dataEmissao != null) {
			data = " abb01.abb01data";
			whereData = dataEmissao[0] != null && dataEmissao[1] != null ? " and abb01.abb01data >= '" + dataEmissao[0] + "' and abb01.abb01data <= '" + dataEmissao[1] + "'": "";
		} else {
			if (dataEntSai != null) {
				data = " eaa01.eaa01esData"; 
				whereData = dataEntSai[0] != null && dataEntSai[1] != null ? " and eaa01.eaa01esData >= '" + dataEntSai[0] + "' and eaa01.eaa01esData <= '" + dataEntSai[1] + "'": "";
			}
		}
		
		String entidade = "";
		String groupBy = "";
		if (nomeEntidade.equals(0)) {
			entidade = " abe01.abe01na as nomeEntidade";	
			groupBy = " group by abb01.abb01num, abb01.abb01data, eaa01.eaa01esdata, aaj15.aaj15codigo, aah01.aah01codigo, abe01.abe01codigo, abe30.abe30nome, abe40.abe40nome, abe01.abe01ni, abe01.abe01na";
		} else {
			entidade = " abe01.abe01nome as nomeEntidade";
			groupBy = " group by abb01.abb01num, abb01.abb01data, eaa01.eaa01esdata, aaj15.aaj15codigo, aah01.aah01codigo, abe01.abe01codigo, abe30.abe30nome, abe40.abe40nome, abe01.abe01ni, abe01.abe01nome";
		}
		
		String whereResumoOperacao = null;	
		if (resumoOperacao.equals(0)) {
			whereResumoOperacao = " and eaa01.eaa01esMov = " + Eaa01.ESMOV_ENTRADA;	
		} else {
			whereResumoOperacao = " and eaa01.eaa01esMov = " + Eaa01.ESMOV_SAIDA;
		}
		
		String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= '" + numeroInicial + "' and abb01.abb01num <= '" + numeroFinal + "'": "";
		String whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01.aah01id IN (:idTipoDocumento)": "";
		String whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01.abe01id IN (:idEntidade)": "";
		String whereCriterio = criterios != null && criterios.size() > 0 ? " and abm0102.abm0102criterio IN (:criterios) " : "";
		String orderBy = data != null && data.size() > 0 ? " order by " + data + " , abb01.abb01num": " order by abb01data, abb01.abb01num";
		
		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null;
		Parametro parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null;
		Parametro parametroCriterios = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios", criterios) : null;
		
		String sql = " select abb01.abb01num, abb01.abb01data, eaa01.eaa01esdata, aaj15.aaj15codigo, aah01.aah01codigo, abe01.abe01codigo, abe30.abe30nome, abe40.abe40nome, abe01.abe01ni, " +
		        "             sum(eaa0103.eaa0103qtUso) as eaa0103qtUso, sum(eaa0103.eaa0103unit) as eaa0103unit, sum(eaa0103.eaa0103totDoc) as eaa0103totDoc,  " + entidade +
				"        from eaa0103 eaa0103 " +
 				"        left join eaa01 eaa01 on eaa01.eaa01id = eaa0103.eaa0103doc " + 
				"        left join abb01 abb01 on abb01.abb01id = eaa01.eaa01central " +
 				"        left join aah01 aah01 on aah01id = abb01.abb01tipo " +
				"        left join abe01 abe01 on abe01id = abb01.abb01ent " +
 				"        left join abe30 abe30 on abe30id = eaa01.eaa01cp  " +
 				"        left join abe40 abe40 on abe40id = eaa01.eaa01tp  " +
 				"        left join aaj15 aaj15 on aaj15.aaj15id = eaa0103.eaa0103cfop  " +
				"        left join abm01 abm01 on abm01.abm01id = eaa0103.eaa0103item " +   
				//"        left join abm0102 abm0102 on abm0102.abm0102item = abm01.abm01id " +
				"  where eaa01.eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
				getSamWhere().getWherePadrao(" AND ", Eaa01.class) +
				whereNumero +
				whereTipoDocumento +
				whereIdEntidade +
				whereCriterio +
				whereData +
				whereResumoOperacao +
				groupBy +
				orderBy;

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento, parametroCriterios); 
		return receberDadosRelatorio;
	}
	
	
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=