package multitec.relatorios.smr;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.be.Bea20
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

/**
 * Classe para relatório SMR - Solicitações de Serviços
 * @author Samuel Silva
 * @since 10/07/2019
 * @version 1.0
 */
public class SMR_SolicitacoesDeServicos extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SMR - Solicitações de Serviços";
	}
	
	/**
	 * Método Principal
	 * @return Map (Dados do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("tipo", "0");
		filtrosDefault.put("numInicial", "0000000");
		filtrosDefault.put("numFinal", "9999999")
		
		return Utils.map("filtros", filtrosDefault);
	}

	/**
	 * Método para gerar PDF
	 * @return gerarPDF (Nome Relatorio, Dados Relatorio)
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsBemPatrimonial = getListLong("bemPatrimonial");
		LocalDate[] datas = getIntervaloDatas("periodo");
		Integer tipo = getInteger("tipo");
		Integer numInicial = getInteger("numInicial");
		Integer numFinal = getInteger("numFinal");

		params.put("TITULO_RELATORIO", "Solicitações de Serviços");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		
		List<TableMap> solicitacoesDeServicos = buscarDadosRelatorio(idsBemPatrimonial, numInicial, numFinal, datas, tipo);
		
		String nomeRelatorio = "SMR_SolicitacoesDeServicos_R1";
		
		return gerarPDF(nomeRelatorio, solicitacoesDeServicos);
	}
	
	/**
	 * Método para buscar os dados do relatorio
	 * @param idsBemPatrimonial - Bem Patrimonial
	 * @param numInicial - Numero de Solicitação Inicial
	 * @param numFinal - Numero de Solicitação Final
	 * @param datas - Intervalo de Datas
	 * @param tipo - Tipo 0-Corretiva 1-Preventiva 2-Preditiva
	 * @return List (Dados do Relatorio)
	 */
	public List<TableMap> buscarDadosRelatorio(List<Long> idsBemPatrimonial, Integer numInicial, Integer numFinal, LocalDate[] datas, Integer tipo) {
		return buscarSolicitacoesDeServicos(idsBemPatrimonial, numInicial, numFinal, datas, tipo);
	}
	
	/**
	 * Método para realizar a busca dos dados no Banco
	 * @return List (Dados do Banco)
	 */
	public List<TableMap> buscarSolicitacoesDeServicos(List<Long> idsBemPatrimonial, Integer numInicial, Integer numFinal, LocalDate[] datas, Integer tipo) {
		String whereAbb20BemPatrimonial = idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty() ? "AND abb20id IN (:idsBemPatrimonial) " : "";
		String whereData = datas != null ? getWhereDataInterval("WHERE", datas, "centss.abb01data") : "";
		
		String sql = "SELECT centss.abb01num AS numss, centss.abb01data AS datass, bea20hora, abb20codigo, abb20nome, bea20motivo, centos.abb01num AS numos " +
					 "FROM Bea20 " +
					 "INNER JOIN Abb20 ON abb20id = bea20bem " +
					 "INNER JOIN Abb01 AS centss ON centss.abb01id = bea20central " +
					 "LEFT JOIN Abb0102 ON abb0102central = bea20central " +
					 "LEFT JOIN Abb01 AS centos ON centos.abb01id = abb0102doc " +
					 whereData + whereAbb20BemPatrimonial +
					 "AND centss.abb01num BETWEEN :numInicial AND :numFinal " +
					 "AND bea20tipo = :tipo " +
					 getSamWhere().getWherePadrao("AND", Bea20.class) +
					 " ORDER BY centss.abb01num, centss.abb01data, abb20codigo";
		
		Query query = getSession().createQuery(sql);
					 
		if(idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty()) query.setParameter("idsBemPatrimonial", idsBemPatrimonial);
		query.setParameter("numInicial", numInicial);
		query.setParameter("numFinal", numFinal);
		query.setParameter("tipo", tipo);
					 
		return query.getListTableMap();
	}

}
//meta-sis-eyJkZXNjciI6IlNNUiAtIFNvbGljaXRhw6fDtWVzIGRlIFNlcnZpw6dvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==