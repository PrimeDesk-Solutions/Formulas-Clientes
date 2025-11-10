package multitec.relatorios.smr;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.be.Beb01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SMR - Manutenções Agendadas
 * @author Samuel Silva
 * @since 02/07/2019
 * @version 1.0
 */
public class SMR_ManutencoesAgendadas extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SMR - Manutenções Agendadas";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("agrupamento", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		
		List<Long> idsBemPatrimonial = getListLong("bemPatrimonial");
		List<Long> idsServicos = getListLong("servicos");
		LocalDate[] datas = getIntervaloDatas("periodo");
		Integer agrupamento = getInteger("agrupamento");
		
		params.put("TITULO_RELATORIO", "Manutenções Agendadas");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		List<TableMap> manutencoesAgendadas = buscarDadosRelatorio(idsBemPatrimonial, idsServicos, datas, agrupamento);		
	     
	    String nomeRelatorio = agrupamento == 0 ? "SMR_ManutencoesAgendadas_R1" : "SMR_ManutencoesAgendadas_R2";
				
		return gerarPDF(nomeRelatorio, manutencoesAgendadas);
	}
	
	/**
	 * Método para buscar os dados do relatório
	 * @param idsBemPatrimonial 
	 * @param idsServicos
	 * @param datas
	 * @param agrupamento
	 * @return TableMap - Dados Obtidos no Banco
	 */
	
	public List<TableMap> buscarDadosRelatorio(List<Long> idsBemPatrimonial, List<Long> idsServicos, LocalDate[] datas, Integer agrupamento) {
		return buscarAgendamentosByBemServicoDatas(idsBemPatrimonial, idsServicos, datas, agrupamento);
	}

	/**
	 * Método para realizar a busca dos dados no Banco
	 * @param idsBemPatrimonial
	 * @param idsServicos
	 * @param datas
	 * @param agrupamento
	 * @return TableMap - Resultado da Query
	 */
	
	public List<TableMap> buscarAgendamentosByBemServicoDatas(List<Long> idsBemPatrimonial, List<Long> idsServicos, LocalDate[] datas, Integer agrupamento) {
		
		String whereAbb20BemPatrimonial = idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty() ? "AND abb20id IN (:idsBemPatrimonial) " : "";
		String whereBeb0101Servicos = idsServicos != null && !idsServicos.isEmpty() ? "AND beb0101id IN (:idsServicos) " : "";
		String whereData = datas != null ? getWhereDataInterval("WHERE", datas, "beb0101data") : "";

		String orderBy = agrupamento == 0 ? "beb0101data, abb20codigo, bea01codigo" : "abb20codigo, beb0101data, bea01codigo";
		
		String sql = "SELECT beb0101data, beb0101detalhe, bea01codigo, bea01descr, abb20codigo, abb20nome " + 
					 "FROM Beb01 " + 
					 "INNER JOIN beb0101 ON beb0101agenda = Beb01id " + 
					 "INNER JOIN Bea01 ON beb0101serv = bea01id  " + 
					 "INNER JOIN abb20 ON beb01bem = abb20id " + 
					 whereData + whereAbb20BemPatrimonial + whereBeb0101Servicos + getSamWhere().getWherePadrao("AND", Beb01.class) +
                     " ORDER BY " + orderBy;
		
		Query query = getSession().createQuery(sql);
		
		if(idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty()) query.setParameter("idsBemPatrimonial", idsBemPatrimonial);
		if(idsServicos != null && !idsServicos.isEmpty()) query.setParameter("idsServicos", idsServicos);
		
		return  query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNNUiAtIE1hbnV0ZW7Dp8O1ZXMgQWdlbmRhZGFzIiwidGlwbyI6InJlbGF0b3JpbyJ9