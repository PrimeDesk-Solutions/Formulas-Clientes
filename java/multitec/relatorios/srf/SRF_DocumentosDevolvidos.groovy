package multitec.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SRF_DocumentosDevolvidos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SRF - Documentos Devolvidos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("esMov", "0");
		filtrosDefault.put("devolucaoOrigem", "0");
		filtrosDefault.put("datas", DateUtils.getStartAndEndMonth(MDate.date()));
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer esMov = getInteger("esMov");
		List<Long> idsEntidades = getListLong("entidades");
		List<Long> idsMotivosDevolucao = getListLong("motivosDevolucao");
		Integer devolucaoOrigem = getInteger("devolucaoOrigem");
		LocalDate[] datas = getIntervaloDatas("datas");
		List<Long> criterios = getListLong("criterios");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		if (esMov.equals(1)) {
			params.put("TITULO_RELATORIO", "Documentos Devolvidos - Faturamento");
		} else {
			params.put("TITULO_RELATORIO", "Documentos Devolvidos - Recebimento");
		}

		if (devolucaoOrigem.equals(0)) {
			params.put("PERIODO", "Período Devolução: " + datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + datas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		} else {
			params.put("PERIODO", "Período Origem: " + datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + datas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		List<TableMap> dados = obterDadosRelatorio(esMov, idsEntidades, idsMotivosDevolucao, devolucaoOrigem, datas, criterios);

		return gerarPDF("SRF_DocumentosDevolvidos", dados);
		
	}
	
	public List<TableMap> obterDadosRelatorio(Integer esMov, List<Long> idsEntidades, List<Long> idsMotivosDevolucao, Integer devolucaoOrigem, LocalDate[] datas, List<Long> criterios)  {
		String whereData = "";
		if (devolucaoOrigem.equals(0)) {
			whereData = datas[0] != null && datas[1] != null ? " AND abb01dev.abb01data >= '" + datas[0] + "' AND abb01dev.abb01data <= '" + datas[1] + "'": "";
		} else {
			whereData = datas[0] != null && datas[1] != null ? " AND abb01orig.abb01data >= '" + datas[0] + "' AND abb01orig.abb01data <= '" + datas[1] + "'": "";
		}
		
		String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? " AND abe01.abe01id IN (:idsEntidades)": "";
		String whereCriterios = criterios != null && criterios.size() > 0 ? " AND abm0103.abm0103criterio IN (:criterios) " : "";
		String whereMotivosDevolucao = idsMotivosDevolucao != null && idsMotivosDevolucao.size() > 0 ? " AND aae10.aae10id IN (:idsMotivosDevolucao)": "";
		
		Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
		Parametro parametroCriterios = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios", criterios) : null;
		Parametro parametroMotivosDevolucao = idsMotivosDevolucao != null && idsMotivosDevolucao.size() > 0 ? Parametro.criar("idsMotivosDevolucao", idsMotivosDevolucao) : null;
		
		String sql = " SELECT eaa01dev.eaa01id AS eaa01idDev, aah01dev.aah01codigo AS aah01codigoDev, abb01dev.abb01num AS abb01numDev, abb01dev.abb01data AS abb01dataDev, " +
					 " eaa01orig.eaa01id AS eaa01idOrig, aah01orig.aah01codigo AS aah01codigoOrig, abb01orig.abb01num AS abb01numOrig, abb01orig.abb01data AS abb01dataOrig, " +
					 " abe01dev.abe01codigo AS abe01codigoDev, abe01dev.abe01nome AS abe01nomeDev, aae10codigo, aae10descr, " +
					 " eaa0103dev.eaa0103id As eaa0103id, eaa0103dev.eaa0103tipo AS eaa0103tipo, eaa0103dev.eaa0103codigo AS eaa0103codigo, eaa0103dev.eaa0103descr AS eaa0103descr, " +
					 " eaa01033.eaa01033qtComl AS eaa01033qtComl, eaa0103dev.eaa0103unit AS eaa0103unit, (eaa0103dev.eaa0103unit * eaa01033.eaa01033qtComl) as valorDevolucao " + 
					 " FROM Eaa01 eaa01dev " +
					 " INNER JOIN Eaa0103 eaa0103dev ON eaa01dev.eaa01id = eaa0103dev.eaa0103doc " +
					 " INNER JOIN Abb01 abb01dev ON abb01dev.abb01id = eaa01dev.eaa01central " +
					 " INNER JOIN Aah01 aah01dev ON aah01dev.aah01id = abb01dev.abb01tipo " +
					 " INNER JOIN Abe01 abe01dev ON abe01dev.abe01id = abb01dev.abb01ent " +
					 " INNER JOIN Eaa01033 eaa01033 ON eaa01033.eaa01033item = eaa0103dev.eaa0103id " +
					 " INNER JOIN Eaa0103 eaa0103orig ON eaa0103orig.eaa0103id = eaa01033.eaa01033itemDoc " +
					 " INNER JOIN Eaa01 eaa01orig ON eaa01orig.eaa01id = eaa0103orig.eaa0103doc " +
					 " INNER JOIN Abb01 abb01orig ON abb01orig.abb01id = eaa01orig.eaa01central " +
					 " INNER JOIN Aah01 aah01orig ON aah01orig.aah01id = abb01orig.abb01tipo " +
					 " LEFT JOIN Aae10 ON aae10id = eaa01dev.eaa01motivoDev " +
					 " LEFT JOIN Abe0103 ON abe01dev.abe01id = abe0103ent " +
					 getSamWhere().getWherePadrao("WHERE", Eaa01.class, "eaa01dev") +
					 " AND eaa01033qtComl > 0 " +  
					 " AND eaa01Dev.eaa01clasDoc = 1 " +
					 " AND eaa01dev.eaa01esMov = " + esMov +
					 whereEntidades +
					 whereCriterios +
					 whereData +
					 whereMotivosDevolucao +
					 " ORDER BY aah01dev.aah01codigo, abb01dev.abb01num, aah01orig.aah01codigo, abb01orig.abb01num, eaa0103dev.eaa0103seq";

		List<TableMap> listTM = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroMotivosDevolucao, parametroCriterios); 
		
		if(listTM != null && listTM.size() > 0) {
			Long eaa0103idAnterior = null;
			for(TableMap tm : listTM) {
				Long eaa0103id = tm.getLong("eaa0103id");
				
				if(eaa0103idAnterior != null && eaa0103idAnterior.equals(eaa0103id)) {
					tm.put("eaa01033qtComl", BigDecimal.ZERO);
					tm.put("eaa0103unit", BigDecimal.ZERO);
					tm.put("valorDevolucao", BigDecimal.ZERO);
				}else {
					eaa0103idAnterior = eaa0103id;
				}
			}
		}
		
		return listTM;
	}
	
	
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgRGV2b2x2aWRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==