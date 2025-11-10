package multitec.relatorios.smr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.be.Bef01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**
 * SMR Historico dos Serviços
 * Mostra os Serviços e os bens que foram submetidos
 * @author Samuel Silva
 *
 */
public class SMR_HistoricoDosServicos extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SMR - Histórico dos Serviços";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idsBemPatrimonial = getListLong("bemPatrimonial");
		List<Long> idsServicos = getListLong("servicos");
		LocalDate[] datas = getIntervaloDatas("periodo");

		params.put("TITULO_RELATORIO", "Histórico dos Serviços");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		List<TableMap> historicoBemPatrimonial = buscarDadosRelatorio(idsBemPatrimonial, idsServicos, datas);
		
		String nomeRelatorio = "SMR_HistoricoDosServicos_R1";
		
		return gerarPDF(nomeRelatorio, historicoBemPatrimonial);
	}
	
	public List<TableMap> buscarDadosRelatorio(List<Long> idsBemPatrimonial, List<Long> idsServicos, LocalDate[] datas){
		List<TableMap> dados = buscarHistoricoExecutores(idsBemPatrimonial, idsServicos, datas);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		for(TableMap map : dados) {
			String iniFim = "";
			
			if(map.getDate("bef01realDataI") != null && map.getDate("bef01realDataF") == null) {
				iniFim = map.getDate("bef01realDataI").format(formatter).toString();
			}else if(map.getDate("bef01realDataI") == null && map.getDate("bef01realDataF") != null) {
				iniFim = map.getDate("bef01realDataF").format(formatter).toString();;
			}else if(map.getDate("bef01realDataI") != null && map.getDate("bef01realDataF") != null){
				iniFim = map.getDate("bef01realDataI").format(formatter).toString() + " à " + map.getDate("bef01realDataF").format(formatter).toString();
			}
			
			map.put("iniFim", iniFim);
		}
		
		return dados;
	}
	
	public List<TableMap> buscarHistoricoExecutores(List<Long> idsBemPatrimonial, List<Long> idsServicos, LocalDate[] datas) {
		String whereAbb20BemPatrimonial = idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty() ? " AND abb20id IN (:idsBemPatrimonial) " : "";
		String whereBea01Servicos = idsServicos != null && !idsServicos.isEmpty() ? " AND bea01id IN (:idsServicos) " : "";
		String whereData = datas != null ? getWhereDataInterval("AND", datas, "abb01data") : "";
		
		String sql = " SELECT bea01codigo, bea01descr, abb01num, abb01data, abb20codigo, abb20nome, bef01realDataI, bef01realDataF, (bef01mo + bef01insumo + bef01equip + bef01despesas) AS custo" + 
					 " FROM Bef0101 " + 
					 " INNER JOIN Bea01 ON bea01id = bef0101serv " + 
					 " INNER JOIN Bef01 ON bef01id = bef0101os " + 
					 " INNER JOIN Abb01 ON abb01id = bef01central " + 
					 " INNER JOIN Abb20 ON abb20id = bef01bem " + 
					 getSamWhere().getWherePadrao("WHERE", Bef01.class) +
					  whereData +  whereAbb20BemPatrimonial + whereBea01Servicos +
					 " ORDER BY bea01codigo, abb01data, abb01num";
		
		Query query = getSession().createQuery(sql);
		
		if(idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty()) query.setParameter("idsBemPatrimonial", idsBemPatrimonial);
		if(idsServicos != null && !idsServicos.isEmpty()) query.setParameter("idsServicos", idsServicos);
		
		return query.getListTableMap();
	}

}
//meta-sis-eyJkZXNjciI6IlNNUiAtIEhpc3TDs3JpY28gZG9zIFNlcnZpw6dvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==