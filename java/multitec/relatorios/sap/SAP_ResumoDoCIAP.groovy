package multitec.relatorios.sap;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.ec.Ecc01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SAP_ResumoDoCIAP extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SAP - Resumo do CIAP";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("dataInicial", datas[0].format(DateTimeFormatter.ofPattern("MM/yyyy")));
		filtrosDefault.put("dataFinal", datas[1].format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		filtrosDefault.put("ordenacao", "0");
		
		return Utils.map("filtros", filtrosDefault);
		
	}

	@Override
	public DadosParaDownload executar() {
		LocalDate[] periodo = new LocalDate[2];
		periodo[0] = DateUtils.parseDate("01/" + getString("dataInicial"));
		periodo[1] = DateUtils.parseDate("01/" + getString("dataFinal"));
		
		Integer ordenacao = getInteger("ordenacao");
		Boolean baixas = get("baixadas");
		Aac10 aac10 = getVariaveis().getAac10();
		String nomeRelatorio = "SAP_ResumoDoCIAP";
		
		params.put("EMPRESA", aac10.getAac10na());
		params.put("TITULO_RELATORIO", "RESUMO DO CIAP");
		params.put("PERIODO", periodo[0].format(DateTimeFormatter.ofPattern("MM/yyyy")) + " à " + periodo[1].format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		TableMapDataSource resumoDoCiap = buscarDadosRelatorio(periodo, ordenacao, baixas);
		
		return gerarPDF(nomeRelatorio, resumoDoCiap);
	}
	
	public TableMapDataSource buscarDadosRelatorio(LocalDate[] periodo, Integer ordenacao, Boolean baixas) {
		Integer mesInicial = periodo[0].getMonthValue().toInteger()
		Integer mesFinal = periodo[1].getMonthValue().toInteger();
		Integer anoInicial = periodo[0].getYear().toInteger();
		Integer anoFinal = periodo[1].getYear().toInteger();
		
		if(!baixas) {
			List<TableMap> listCIAPs = buscarCIAPPorPeriodoEOrdenacao(mesInicial, mesFinal, anoInicial, anoFinal, ordenacao);
			
			if(listCIAPs != null && listCIAPs.size() > 0) {
				Integer i = 0;
				for(TableMap mapCIAP : listCIAPs) {
					
					Long ecc01id = mapCIAP.getLong("ecc01id");
					
					BigDecimal vlrPeriodo = buscarVlrDoPeriodoCIAP(ecc01id, mesInicial, anoInicial, mesFinal, anoFinal);
					listCIAPs.get(i).put("vlrPeriodo", vlrPeriodo);
					
					BigDecimal vlrRecuperado = buscarVlrRecuperadoCIAP(ecc01id, mesInicial, anoInicial);
					listCIAPs.get(i).put("vlrRecuperado", vlrRecuperado);
					
					BigDecimal vlrRecupFixo = buscarVlrRecuperadoFixoCIAP(ecc01id, mesInicial, anoInicial);
					listCIAPs.get(i).put("vlrRecupFixo", vlrRecupFixo);
					
					BigDecimal vlrARecuperar = buscarVlrARecuperarCIAP(ecc01id, mesFinal, anoFinal);
					listCIAPs.get(i).put("vlrARecuperar", vlrARecuperar);
					
					BigDecimal diferenca = buscarSomaICMSByAtePeriodo(ecc01id, mesFinal, anoFinal);
					diferenca = mapCIAP.getBigDecimal("ecc01icms") - diferenca;
					listCIAPs.get(i).put("diferenca", diferenca);
					
					BigDecimal fixo = buscarSomaIcmsMesICMSByNoPeriodo(ecc01id, mesInicial, anoInicial, mesFinal, anoFinal);
					listCIAPs.get(i).put("fixo", fixo);
					
					BigDecimal fixoMenosVlrPeriodo = fixo - vlrPeriodo;
					listCIAPs.get(i).put("fixoMenosVlrPeriodo", fixoMenosVlrPeriodo);
					
					i++;

				}
				
				TableMapDataSource dsPrincipal = new TableMapDataSource(listCIAPs);
				return dsPrincipal;
			}
			
		}else {
			List<TableMap> listCIAPs = buscarCIAPBaixadosPorPeriodo(mesInicial, anoInicial, mesFinal, anoFinal, ordenacao);
			
			if(listCIAPs != null && listCIAPs.size() > 0) {
				for(TableMap mapCIAP : listCIAPs) {
					
					Long ecc01id = mapCIAP.getLong("ecc01id");
					
					BigDecimal valor = buscarVlrRecuperadoCIAP(ecc01id, mesInicial, anoInicial);
					mapCIAP.put("vlrRecuperado", valor);
					
					valor = mapCIAP.getBigDecimal_Zero("ecc01icms") - valor;
					mapCIAP.put("vlrNaoRecuperado", valor);
					
					TableMapDataSource dsPrincipal = new TableMapDataSource(mapCIAP);
					return dsPrincipal;
				}
			}
		}
	}
	
	public List<TableMap> buscarCIAPPorPeriodoEOrdenacao(Integer mesInicial, Integer anoInicial, Integer mesFinal, Integer anoFinal, Integer ordenacao) {
		
		String order = ordenacao == 0 ? "" : ordenacao == 1 ? "abb20codigo, " : "abb20nome, ";
		
		String sql = "SELECT ecc01id, ecc01num, ecc01modelo, ecc01icms, ecc01data, abb20codigo, abb20nome " +
				     "FROM Ecc01 " +
					 "INNER JOIN Ecc0101 ON ecc01id = ecc0101ficha " +
					 "INNER JOIN Abb20 ON abb20id = ecc01bem " +
				     "WHERE ((ecc0101mes * 12) + ecc0101ano) BETWEEN :numMesesIni AND :numMesesFin " + 
					 getSamWhere().getWherePadrao("AND", Ecc01.class) +
				     " GROUP BY ecc01id, ecc01num, ecc01modelo, ecc01icms, ecc01data, abb20codigo, abb20nome " +
				     "ORDER BY " + order + "ecc01data, ecc01modelo, ecc01num";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("numMesesIni", buscarVlrNumMeses(mesInicial, anoInicial));
		query.setParameter("numMesesFin", buscarVlrNumMeses(mesFinal, anoFinal));

		return query.getListTableMap();
	}	
	
	public List<TableMap> buscarCIAPBaixadosPorPeriodo(Integer mesInicial, Integer anoInicial, Integer mesFinal, Integer anoFinal, Integer ordenacao) {
		
		String order = ordenacao == 0 ? "" : ordenacao == 1 ? "abb20codigo, " : "abb20nome, ";
		
		String sql = "SELECT ecc01id, ecc01num, ecc01modelo, ecc01icms, ecc01data, abb20codigo, abb20nome, abb20baixa " +
					 "FROM Ecc01 " + 
					 "INNER JOIN Abb20 ON abb20id = ecc01bem " +
					 "WHERE ((EXTRACT(MONTH FROM abb20baixa) * 12) + EXTRACT(YEAR FROM abb20baixa)) BETWEEN :numMesesIni AND :numMesesFin " + 
					 getSamWhere().getWherePadrao("AND", Ecc01.class) +
					 " ORDER BY " + order + "ecc01data, ecc01modelo, ecc01num";
					 
		Query query = getSession().createQuery(sql);
		query.setParameter("numMesesIni", buscarVlrNumMeses(mesInicial, anoInicial));
		query.setParameter("numMesesFin", buscarVlrNumMeses(mesFinal, anoFinal));
		
		return query.getListTableMap();
	}
	
	public BigDecimal buscarVlrDoPeriodoCIAP(Long ecc01id, Integer mesInicial, Integer anoInicial, Integer mesFinal, Integer anoFinal) {
		String sql = "SELECT SUM(ecc0101icms) as ecc0101icms " +
					 "FROM Ecc01 " + 
					 "INNER JOIN Ecc0101 ON ecc01id = ecc0101ficha " +
					 "INNER JOIN Abb20 ON abb20id = ecc01bem " +
					 "WHERE ecc01id = :ecc01id " +
					 "AND ((ecc0101mes * 12) + ecc0101ano) BETWEEN :numMesesIni AND :numMesesFin " + 
					 getSamWhere().getWherePadrao("AND", Ecc01.class);
					 
		Query query = getSession().createQuery(sql);
		query.setParameter("ecc01id", ecc01id);
		query.setParameter("numMesesIni", buscarVlrNumMeses(mesInicial, anoInicial));
		query.setParameter("numMesesFin", buscarVlrNumMeses(mesFinal, anoFinal));
		
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result == null ? new BigDecimal(0) : result;
	}
	
	public BigDecimal buscarVlrRecuperadoCIAP(Long ecc01id, Integer mesInicial, Integer anoInicial) {
		String sql = "SELECT SUM(ecc0101icms) as ecc0101icms " +
					 "FROM Ecc01 " + 
					 "INNER JOIN Ecc0101 ON ecc0101id = ecc0101ficha " + 
					 "INNER JOIN Abb20 ON abb20id = ecc01bem " +
					 "WHERE ecc01id = :ecc01id " + 
					 "AND ((ecc0101mes * 12) + ecc0101ano) < :numMesesIni " + 
					 getSamWhere().getWherePadrao("AND", Ecc01.class);
					 
		Query query = getSession().createQuery(sql);
		query.setParameter("ecc01id", ecc01id);
		query.setParameter("numMesesIni", buscarVlrNumMeses(mesInicial, anoInicial));
		
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result == null ? new BigDecimal(0) : result;
	}
	
	public BigDecimal buscarVlrRecuperadoFixoCIAP(Long ecc01id, Integer mesInicial, Integer anoInicial) {
		String sql = "SELECT ecc0101json " +
					 "FROM Ecc01 " +
					 "INNER JOIN Ecc0101 ON ecc01id = ecc0101ficha " +
					 "INNER JOIN Abb20 ON abb20id = ecc01bem " +
					 "WHERE ecc01id = :ecc01id " +
					 "AND ((ecc0101mes * 12) + ecc0101ano) < :numMesesIni " +
					 getSamWhere().getWherePadrao("AND", Ecc01.class);
					 
		Query query = getSession().createQuery(sql);
		query.setParameter("ecc01id", ecc01id);
		query.setParameter("numMesesIni", buscarVlrNumMeses(mesInicial, anoInicial));
		
		List<TableMap> result = query.getListTableMap();
		
		BigDecimal valorCIAP = 0;
		
		for(TableMap map : result) {
			TableMap mapJson = map.get("ecc0101json");
			if(mapJson == null) continue;
			valorCIAP += mapJson.getBigDecimal_Zero("Valor_fixo_ICMS_mes");
		}
		
		return valorCIAP;
	}
	
	public BigDecimal buscarVlrARecuperarCIAP(Long ecc01id, Integer mesFinal, Integer anoFinal) {
		
		String sql = "SELECT SUM(ecc0101icms) as ecc0101icms " +
					 "FROM Ecc01 " + 
					 "INNER JOIN Ecc0101 ON ecc01id = ecc0101ficha " + 
					 "INNER JOIN Abb20 ON abb20id = ecc01bem " +
					 "WHERE ecc01id = :ecc01id " + 
					 "AND ((ecc0101mes * 12) + ecc0101ano) > :numMesesFin " + 
					 getSamWhere().getWherePadrao("AND", Ecc01.class);
					 
		Query query = getSession().createQuery(sql);
		query.setParameter("ecc01id", ecc01id);
		query.setParameter("numMesesFin", buscarVlrNumMeses(mesFinal, anoFinal));
		
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	public BigDecimal buscarSomaICMSByAtePeriodo(Long ecc01id, Integer mesFinal, Integer anoFinal) {

		Query query = getSession().createQuery("SELECT SUM(ecc0101icms) FROM Ecc0101 " +
											   "WHERE ecc0101ficha = :ecc01id " +
											   "AND ((ecc0101mes * 12) + ecc0101ano) <= :numMeses");
								
		query.setParameter("ecc01id", ecc01id);
		query.setParameter("numMeses", buscarVlrNumMeses(mesFinal, anoFinal));
		
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result == null ? 0 : result;
	}
	
	public BigDecimal buscarSomaIcmsMesICMSByNoPeriodo(Long ecc01id, Integer mesInicial, Integer anoInicial, Integer mesFinal, Integer anoFinal) {
	
		Query query = getSession().createQuery("SELECT SUM(ecc0101icms) FROM Ecc0101 " +
									"WHERE ecc0101ficha = :ecc01id AND " +
									"((ecc0101mes * 12) + ecc0101ano) BETWEEN :numMesesI AND :numMesesF");
								
		query.setParameter("ecc01id", ecc01id);
		query.setParameter("numMesesI", buscarVlrNumMeses(mesInicial, anoInicial));
		query.setParameter("numMesesF", buscarVlrNumMeses(mesFinal, anoFinal));
		
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result == null ? 0 : result;
	}
	
	public static Integer buscarVlrNumMeses(Integer mes, Integer ano){
		return (ano * 12) + mes;
	}

}
//meta-sis-eyJkZXNjciI6IlNBUCAtIFJlc3VtbyBkbyBDSUFQIiwidGlwbyI6InJlbGF0b3JpbyJ9