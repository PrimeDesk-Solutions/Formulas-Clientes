package multitec.relatorios.smr;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.be.Bef01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**
 * Classe para relatório SMR - Ordem de Serviços
 * @author Samuel Silva
 * @since 04/07/2019
 * @version 1.0
 */
public class SMR_OrdemDeServicos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SMR - Ordem de Serviços";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("tipo", "0");
		filtrosDefault.put("st", "0");
		filtrosDefault.put("numInicial", "0000000");
		filtrosDefault.put("numFinal", "9999999")
		
		return Utils.map("filtros", filtrosDefault);
	}

	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsBemPatrimonial = getListLong("bemPatrimonial");
		LocalDate[] datas = getIntervaloDatas("periodo");
		Integer tipo = getInteger("tipo");
		Integer st = getInteger("st");
		Integer numInicial = getInteger("numInicial");
		Integer numFinal = getInteger("numFinal");

		params.put("TITULO_RELATORIO", "Ordem de Serviços");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		
		List<TableMap> ordemDeServicos = buscarDadosRelatorio(idsBemPatrimonial, numInicial, numFinal, datas, tipo, st);
		
		String nomeRelatorio = "SMR_OrdemDeServicos_R1";
		
		return gerarPDF(nomeRelatorio, ordemDeServicos);
	}
	
	/**
	 * Método para buscar os dados do relatório
	 * @param idsBemPatrimonial - Bem Patrimonial
	 * @param numInicial - Numero inicial
	 * @param numFinal -  Numero Final
	 * @param datas - Intervalo de Datas
	 * @param tipo - Tipo de Ordem de Serviço
	 * @param st - Status da Ordem de Serviço
	 * @return TableMap - Dados Obtidos no Banco
	 */
	public List<TableMap> buscarDadosRelatorio(List<Long> idsBemPatrimonial, Integer numInicial, Integer numFinal, LocalDate[] datas, Integer tipo, Integer st) {
		return buscarOrdensDeServicos(idsBemPatrimonial, numInicial, numFinal, datas, tipo, st);
	}
	
	/**
	 * Método para realizar a busca dos dados no Banco
	 * @return Dados do Banco
	 */
	public List<TableMap> buscarOrdensDeServicos(List<Long> idsBemPatrimonial, Integer numInicial, Integer numFinal, LocalDate[] datas, Integer tipo, Integer st) {
		String whereAbb20BemPatrimonial = idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty() ? "AND abb20id IN (:idsBemPatrimonial) " : "";
		String whereStatus = st == 2 ? "" : st == 0 ? " AND bef01encData IS NULL " : " AND bef01encData IS NOT NULL ";
		String whereData = datas != null ? getWhereDataInterval("WHERE", datas, "abb01data") : "";
		
		String sql = "SELECT abb01num, abb01data, bef01prevHoraI, abb20codigo, abb20nome, bef01descr " +
		     		 "FROM bef01 " + 
		 			 "INNER JOIN Abb20 ON abb20id = bef01bem " +
					 "INNER JOIN Abb01 ON abb01id = bef01central " +
					 whereData + "AND abb01num BETWEEN :numInicial AND :numFinal AND bef01tipo = :tipo " +
					 whereAbb20BemPatrimonial + whereStatus +
					 getSamWhere().getWherePadrao("AND", Bef01.class) +
					 " ORDER BY abb01num, abb01data, abb20codigo";
		
		Query query = getSession().createQuery(sql);
					 
		if(idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty()) query.setParameter("idsBemPatrimonial", idsBemPatrimonial);
		query.setParameter("numInicial", numInicial);
		query.setParameter("numFinal", numFinal);
		query.setParameter("tipo", tipo);
					 
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNNUiAtIE9yZGVtIGRlIFNlcnZpw6dvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==