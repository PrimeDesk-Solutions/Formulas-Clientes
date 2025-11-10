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
 * Classe para relatório SMR - Histórico dos Bens Patrimoniais
 * @author Samuel Siva
 * @since 04/07/2019
 * @version 1.0
*/
public class SMR_HistoricoDosBensPatrimoniais extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SMR - Histórico dos Bens Patrimoniais";
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
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**
	 * Método para gerar o PDF
	 * @return Gera o PDF
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsBemPatrimonial = getListLong("bemPatrimonial");
		LocalDate[] datas = getIntervaloDatas("periodo");
		Integer tipo = getInteger("tipo");

		params.put("TITULO_RELATORIO", "Histórico dos Bens Patrimoniais");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		List<TableMap> historicoBemPatrimonial = buscarDadosRelatorio(idsBemPatrimonial, datas, tipo);
		
		String nomeRelatorio = "SMR_HistoricoDosBensPatrimoniais_R1";
		
		return gerarPDF(nomeRelatorio, historicoBemPatrimonial);
	}
	
	/**
	 * Método Buscar dados do Relatório
	 * @param idsBemPatrimonial - Bem Patrimonial
	 * @param datas - Intervalo da Datas
	 * @param tipo - Tipo da Ordem de Serviço
	 * @return Dados do relatório vindos do Banco
	 */
	public List<TableMap> buscarDadosRelatorio(List<Long> idsBemPatrimonial, LocalDate[] datas, Integer tipo) {
		List<TableMap> dadosBuscarHistoricoBens = buscarHistoricoBens(idsBemPatrimonial, datas, tipo);
		
		for(TableMap map : dadosBuscarHistoricoBens) {
			String iniFim = "";
			
			if(map.getDate("bef01realDataI") != null && map.getDate("bef01realDataF") == null) {
				iniFim = map.getDate("bef01realDataI");
			}else if(map.getDate("bef01realDataI") == null && map.getDate("bef01realDataF") != null) {
				iniFim = map.getDate("bef01realDataF");
			}else if(map.getDate("bef01realDataI") != null && map.getDate("bef01realDataF") != null){
				iniFim = map.getDate("bef01realDataI") + " à " + map.getDate("bef01realDataF");;
			}
			
			map.put("iniFim", iniFim);
		}
		return dadosBuscarHistoricoBens;
	}
	
	/**
	 * Método para realizar a busca dos dados no Banco
	 * @param idsBemPatrimonial - Bem Patrimonial
	 * @param datas - Intervalo da Datas
	 * @param tipo - Tipo da Ordem de Serviço
	 * @return Dados do Banco
	 */
	public List<TableMap> buscarHistoricoBens(List<Long> idsBemPatrimonial, LocalDate[] datas, Integer tipo) {
		String whereAbb20BemPatrimonial = idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty() ? "AND abb20id IN (:idsBemPatrimonial) " : "";
		String whereData = datas != null ? getWhereDataInterval("WHERE", datas, "abb01data") : "";
		
		String sql = "SELECT abb01num, abb01data, abb20codigo, abb20nome, bef01descr, bef01tipo, bef01realDataI, bef01realDataF, (bef01mo + bef01insumo + bef01equip + bef01despesas) as custo " +
					"FROM bef01 " +
					"INNER JOIN Abb20 ON abb20id = bef01bem " +
					"INNER JOIN Abb01 ON abb01id = bef01central " +
					 whereData + " AND bef01tipo = :tipo " +
					 whereAbb20BemPatrimonial + getSamWhere().getWherePadrao("AND", Bef01.class) +
					" ORDER BY abb20codigo, abb01data, abb01num";
		
		Query query = getSession().createQuery(sql);
		
		if(idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty()) query.setParameter("idsBemPatrimonial", idsBemPatrimonial);
		query.setParameter("tipo", tipo);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNNUiAtIEhpc3TDs3JpY28gZG9zIEJlbnMgUGF0cmltb25pYWlzIiwidGlwbyI6InJlbGF0b3JpbyJ9