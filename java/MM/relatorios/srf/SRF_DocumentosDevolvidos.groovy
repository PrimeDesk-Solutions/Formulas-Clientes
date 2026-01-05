package MM.relatorios.srf;

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
		List<Long> pcd = getListLong("pcd");
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

		List<TableMap> dados = obterDadosRelatorio(esMov, idsEntidades, idsMotivosDevolucao, devolucaoOrigem, datas, criterios,pcd);

		return gerarXLSX("SRF_DocumentosDevolvidos", dados);
		
	}
	
	public List<TableMap> obterDadosRelatorio(Integer esMov, List<Long> idsEntidades, List<Long> idsMotivosDevolucao, Integer devolucaoOrigem, LocalDate[] datas, List<Long> criterios, List<Long> pcd)  {
		String whereData = "";
		if (devolucaoOrigem.equals(0)) {
			whereData = datas[0] != null && datas[1] != null ? " AND abb01dev.abb01data >= '" + datas[0] + "' AND abb01dev.abb01data <= '" + datas[1] + "'": "";
		} else {
			whereData = datas[0] != null && datas[1] != null ? " AND abb01orig.abb01data >= '" + datas[0] + "' AND abb01orig.abb01data <= '" + datas[1] + "'": "";
		}
		
		String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? " AND abe01dev.abe01id IN (:idsEntidades)": "";
		String wherePcd = pcd != null && pcd.size() > 0 ? " AND abd01dev.abd01id IN (:pcd)": "";
		String whereCriterios = criterios != null && criterios.size() > 0 ? " AND abm0103.abm0103criterio IN (:criterios) " : "";
		String whereMotivosDevolucao = idsMotivosDevolucao != null && idsMotivosDevolucao.size() > 0 ? " AND aae10.aae10id IN (:idsMotivosDevolucao)": "";
		
		Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
		Parametro parametroPcd = pcd != null && pcd.size() > 0 ? Parametro.criar("pcd", pcd) : null;
		Parametro parametroCriterios = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios", criterios) : null;
		Parametro parametroMotivosDevolucao = idsMotivosDevolucao != null && idsMotivosDevolucao.size() > 0 ? Parametro.criar("idsMotivosDevolucao", idsMotivosDevolucao) : null;
		
		String sql = " SELECT eaa0103dev.eaa0103total as totDocDev, eaa0103dev.eaa0103qtComl as qtdDocDev, abd01dev.abd01codigo as pcdDev, abd01dev.abd01descr as descrPcdDev, eaa01dev.eaa01id AS eaa01idDev, aah01dev.aah01codigo AS aah01codigoDev, abb01dev.abb01num AS abb01numDev, abb01dev.abb01data AS abb01dataDev, " +
					 " eaa01orig.eaa01id AS eaa01idOrig, aah01orig.aah01codigo AS aah01codigoOrig, abb01orig.abb01num AS abb01numOrig, abb01orig.abb01data AS abb01dataOrig, " +
					 " abe01dev.abe01codigo AS abe01codigoDev, abe01dev.abe01nome AS abe01nomeDev, aae10codigo, aae10descr, " +
					 " eaa0103dev.eaa0103id As eaa0103id, eaa0103dev.eaa0103tipo AS eaa0103tipo, eaa0103dev.eaa0103codigo AS eaa0103codigo, eaa0103dev.eaa0103descr AS eaa0103descr, " +
					 " eaa01033.eaa01033qtComl AS eaa01033qtComl, eaa0103dev.eaa0103unit AS eaa0103unit, (eaa0103dev.eaa0103unit * eaa01033.eaa01033qtComl) as valorDevolucao,abe01orig.abe01codigo as codEntOrig, abe01orig.abe01na as naEntOrigem, eaa0103orig.eaa0103id as idOrig " + 
					 " FROM Eaa01 eaa01dev " +
					 "INNER JOIN abd01 abd01dev ON abd01dev.abd01id = eaa01dev.eaa01pcd " +
					 " INNER JOIN Eaa0103 eaa0103dev ON eaa01dev.eaa01id = eaa0103dev.eaa0103doc " +
					 " INNER JOIN Abb01 abb01dev ON abb01dev.abb01id = eaa01dev.eaa01central " +
					 " INNER JOIN Aah01 aah01dev ON aah01dev.aah01id = abb01dev.abb01tipo " +
					 " INNER JOIN Abe01 abe01dev ON abe01dev.abe01id = abb01dev.abb01ent " +
					 " LEFT JOIN Eaa01033 eaa01033 ON eaa01033.eaa01033item = eaa0103dev.eaa0103id " +
					 " LEFT JOIN Eaa0103 eaa0103orig ON eaa0103orig.eaa0103id = eaa01033.eaa01033itemDoc " +
					 " LEFT JOIN Eaa01 eaa01orig ON eaa01orig.eaa01id = eaa0103orig.eaa0103doc " +
					 " LEFT JOIN Abb01 abb01orig ON abb01orig.abb01id = eaa01orig.eaa01central " +
					 "LEFT JOIN abe01 as abe01orig on abe01orig.abe01id = abb01orig.abb01ent "+
					 " LEFT JOIN Aah01 aah01orig ON aah01orig.aah01id = abb01orig.abb01tipo " +
					 " LEFT JOIN Aae10 ON aae10id = eaa01dev.eaa01motivoDev " +
					 getSamWhere().getWherePadrao("WHERE", Eaa01.class, "eaa01dev") +
					 " AND eaa01Dev.eaa01clasDoc = 1 " +
					 " AND eaa01dev.eaa01esMov = " + esMov +
					 whereEntidades +
					 whereCriterios +
					 wherePcd +
					 whereData +
					 whereMotivosDevolucao +
					 "and eaa01dev.eaa01cancdata is null "+
					 " ORDER BY aah01dev.aah01codigo, abb01dev.abb01num, aah01orig.aah01codigo, abb01orig.abb01num, eaa0103dev.eaa0103seq"

					 

		
		List<TableMap> listTM = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroMotivosDevolucao, parametroCriterios,parametroPcd); 
		
		if(listTM != null && listTM.size() > 0) {
			Long eaa0103idAnterior = null;
			for(TableMap tm : listTM) {
				if(tm.getBigDecimal("abb01numOrig") == null){
					tm.put("eaa01033qtComl",BigDecimal.ZERO);
					tm.put("valorDevolucao",BigDecimal.ZERO);
				}else{
					tm.put("totDocDev",BigDecimal.ZERO);
				}
				
			}
		}
		
		return listTM;
	}
	
	
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgRGV2b2x2aWRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgRGV2b2x2aWRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==