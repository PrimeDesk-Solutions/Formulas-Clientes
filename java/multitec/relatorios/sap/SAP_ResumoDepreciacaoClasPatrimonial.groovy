package multitec.relatorios.sap

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ec.Eca01
import sam.model.entities.ec.Ecb01
import sam.model.entities.ec.Ecb0101
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SAP_ResumoDepreciacaoClasPatrimonial extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "SAP - Resumo da Depreciação por Clas. Patrimonial";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("dtReferencia", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		return Utils.map("filtros", filtrosDefault);
	}
	
	@Override
	public DadosParaDownload executar() {
		LocalDate dtReferencia = DateUtils.parseDate("01/" + getString("dtReferencia"));
		params.put("EMPRESA", getVariaveis().getAac10().getAac10rs());
		params.put("TITULO_RELATORIO", "Resumo da Depreciação por Classificação Patrimonial");
		params.put("MESANO", dtReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy")));
	
		Integer dataRef = Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear());
		
		Map<Long, List<Long>> mapUltReclasMesAnterior = compoeMapImobByReclass(dataRef, 0);
		
		Map<Long, List<Long>> mapUltReclasMesRef = compoeMapImobByReclass(dataRef, 1);
		
		List<TableMap> dados = buscaClassificacoesCodigoNome();
		
		List<Long> idsEspec;
		
		if(dados != null && dados.size() > 0) {
			for(int i = 0; i < dados.size(); i++) {
				TableMap tm = dados.get(i);
				
				String codigo = tm.getString("codigo");
				if(codigo.length() > 5) {
					Long id = tm.getLong("id");
					
					idsEspec = mapUltReclasMesAnterior.get(id);
					BigDecimal saldoAnterior = buscaSaldo(dataRef-1, idsEspec);
					tm.put("saldoAnterior", saldoAnterior);
					
					idsEspec = mapUltReclasMesRef.get(id);
					
					BigDecimal deprec = buscaDepreciacao(dataRef, idsEspec);
					tm.put("deprec", deprec);
					
					BigDecimal baixas = buscaBaixas(dataRef, idsEspec);
					tm.put("baixas", baixas);
					
					BigDecimal saldoAtual = buscaSaldo(dataRef, idsEspec);
					tm.put("saldoAtual", saldoAtual);
					
					BigDecimal transf = saldoAtual - saldoAnterior - deprec + baixas;
					tm.put("transf", transf);
				}
			}
		}
		
		somaGrausSuperiores(dados);
		
		return gerarPDF("SAP_ResumoDepreciacaoClasPatrimonial", dados);
	}
	
	private Map<Long, List<Long>> compoeMapImobByReclass(Integer dataRef, int tipoData) {
		Map<Long, List<Long>> mapUltReclas = new HashMap<>();
		
		List<Ecb0101> ecb0101s = buscaUltimasReclassificacoesByDataReferencia(dataRef, tipoData);
		
		List<Long> idsEcb01;
		for(Ecb0101 ecb0101 : ecb0101s) {
			idsEcb01 = new ArrayList<Long>();
			
			Eca01 eca01 = ecb0101.getEcb0101clas();
			Long key = eca01.getEca01id();
			
			if(mapUltReclas.get(key) != null) {
				idsEcb01 = mapUltReclas.get(key);
			}
			idsEcb01.add(ecb0101.getEcb0101imob().getEcb01id());

			mapUltReclas.put(key, idsEcb01);
		}
		
		return mapUltReclas;
	}
	
	private List<Ecb0101> buscaUltimasReclassificacoesByDataReferencia(Integer dataRef, int tipoData) {
		String sinal = tipoData == 0 ? "<" : "<=";
		Query query = getSession().createQuery(" SELECT * FROM Ecb0101 ",
											   " INNER JOIN Ecb01 ON ecb01id = ecb0101imob ",
											   " WHERE ",
											   " (ecb0101imob, ", Fields.numMeses("ecb0101mes", "ecb0101ano"), ") IN ",
											   " (SELECT ecb0101imob, MAX(", Fields.numMeses("ecb0101mes", "ecb0101ano"), ") ",
											   " FROM ecb0101 INNER JOIN Ecb01 ON ecb01id = ecb0101imob ",
											   " WHERE ", Fields.numMeses("ecb0101mes", "ecb0101ano"), " ", sinal, " :dataRef ",
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class),
											   " GROUP BY ecb0101imob)");
		query.setParameter("dataRef", dataRef);
		return query.getList(ColumnType.ENTITY);
	}
	
	private List<TableMap> buscaClassificacoesCodigoNome() {
		Query query = getSession().createQuery(" SELECT eca01id as id, eca01codigo as codigo, eca01nome as nome ",
											   " FROM Eca01 ",
											   getSamWhere().getWherePadrao("WHERE", Eca01.class),
											   " ORDER BY eca01codigo");
		return query.getListTableMap();
	}
	
	private BigDecimal buscaSaldo(Integer dataRef, List<Long> idsEspec) {
		Query query = getSession().createQuery(" SELECT SUM(ecb0102deprec) FROM Ecb0102 ",
											   " INNER JOIN Ecb01 ON ecb0102imob = ecb01id ",
											   " INNER JOIN Abb20 ON abb20id = ecb01bem ",
											   " WHERE ", 
											   Fields.numMeses("ecb0102mes", "ecb0102ano").toString() + " <= :dataRef ",
											   " AND (" + Fields.numMeses(Fields.month("abb20baixa").toString(), Fields.year("abb20baixa").toString()) + " > :dataRef OR abb20baixa IS NULL) ",
											   " AND ecb01id IN (:idsEspec) ",
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class));
		query.setParameter("dataRef", dataRef);
		query.setParameter("idsEspec", idsEspec);
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result != null ? result : BigDecimal.ZERO;
	}
	
	private BigDecimal buscaDepreciacao(Integer dataRef, List<Long> idsEspec) {
		Query query = getSession().createQuery(" SELECT SUM(ecb0102deprec) FROM Ecb0102 ",
											   " INNER JOIN Ecb01 ON ecb0102imob = ecb01id ",
											   " INNER JOIN Abb20 ON abb20id = ecb01bem ",
											   " WHERE ",
											   Fields.numMeses("ecb0102mes", "ecb0102ano").toString() + " = :dataRef ",
											   " AND ecb01id IN (:idsEspec) ",
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class));
		query.setParameter("dataRef", dataRef);
		query.setParameter("idsEspec", idsEspec);
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result != null ? result : BigDecimal.ZERO;
	}
	
	private BigDecimal buscaBaixas(Integer dataRef, List<Long> idsEspec) {
		Query query = getSession().createQuery(" SELECT SUM(ecb0102deprec) FROM Ecb0102 ",
											   " INNER JOIN Ecb01 ON ecb0102imob = ecb01id ",
											   " INNER JOIN Abb20 ON abb20id = ecb01bem ",
											   " WHERE ",
											   Fields.numMeses("ecb0102mes", "ecb0102ano").toString() + " = :dataRef ",
											   " AND " + Fields.numMeses(Fields.month("abb20baixa").toString(), Fields.year("abb20baixa").toString()) + " <= :dataRef ",
											   " AND ecb01id IN (:idsEspec) ",
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class));
		query.setParameter("dataRef", dataRef);
		query.setParameter("idsEspec", idsEspec);
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result != null ? result : BigDecimal.ZERO;
	}
	
	private void somaGrausSuperiores(List<TableMap> dados) {
		for(TableMap tmDado : dados) {
			BigDecimal saldoAnterior = 0.0;
			BigDecimal deprec = 0.0;
			BigDecimal baixas = 0.0;
			BigDecimal saldoAtual = 0.0;
			BigDecimal transf = 0.0;
			
			String codigo = tmDado.getString("codigo");
			int tam = codigo.length();
			
			if(tam <= 5) {
				for(TableMap tmSup : dados) {
					String cod = tmSup.getString("codigo");
					
					if(cod.length() >= tam) {
						if(codigo.equals(cod.substring(0, tam))){
							saldoAnterior = saldoAnterior + tmSup.getBigDecimal_Zero("saldoAnterior");
							deprec = deprec + tmSup.getBigDecimal_Zero("deprec");
							baixas = baixas + tmSup.getBigDecimal_Zero("baixas");
							saldoAtual = saldoAtual + tmSup.getBigDecimal_Zero("saldoAtual");
							transf = transf + tmSup.getBigDecimal_Zero("transf");
						}
					}
				}
				
				tmDado.put("saldoAnterior", saldoAnterior);
				tmDado.put("deprec", deprec);
				tmDado.put("baixas", baixas);
				tmDado.put("saldoAtual", saldoAtual);
				tmDado.put("transf", transf);
			}
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNBUCAtIFJlc3VtbyBkYSBEZXByZWNpYcOnw6NvIHBvciBDbGFzLiBQYXRyaW1vbmlhbCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==