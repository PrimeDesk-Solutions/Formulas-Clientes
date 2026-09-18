package multitec.relatorios.sap

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abb11
import sam.model.entities.ec.Ecb01
import sam.model.entities.ec.Ecb0101
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SAP_ResumoDepreciacaoDepartamentos extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "SAP - Resumo da Depreciação por Departamento";
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
		
		String estrutura = buscaEstruturaCodigoDep();
		
		List<Integer> grupos = new ArrayList<>();
		
		getEstrutura(estrutura, grupos, 0);
		if(grupos.size() <= 1) interromper("Há menos de dois grupos na estrutura de códigos de departamento.");
		
		int tamMaxGrupos = grupos.get(grupos.size()-2);
	
		Integer dataRef = Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear());
		
		Map<Long, List<Long>> mapUltReclasMesAnterior = compoeMapImobByReclass(dataRef, 0);
		
		Map<Long, List<Long>> mapUltReclasMesRef = compoeMapImobByReclass(dataRef, 1);
		
		List<TableMap> dados = buscaDepartamentosCodigoNome();
		
		List<Long> idsEspec;
		
		if(dados != null && dados.size() > 0) {
			for(int i = 0; i < dados.size(); i++) {
				TableMap tm = dados.get(i);
				
				String codigo = tm.getString("codigo");
				if(codigo.length() > tamMaxGrupos) {
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
		
		somaGrausSuperiores(dados, tamMaxGrupos);
		
		return gerarPDF("SAP_ResumoDepreciacaoClasPatrimonial", dados);
	}
	
	private String buscaEstruturaCodigoDep() {
		String estrutura = getSession().createCriteria(Aba01.class)
						   .addFields("aba01conteudo")
						   .addWhere(Criterions.eq("aba01aplic", "ABB11"))
						   .addWhere(Criterions.eq("aba01param", "ESTRCODDEPTO"))
						   .addWhere(getSamWhere().getCritPadrao(Aba01.class))
						   .get(ColumnType.STRING);
		if(estrutura == null || estrutura.length() == 0) interromper("Não foi encontrado o conteúdo do parâmetro ABB11-ESTRCODDEPTO.");
		return estrutura;
	}
	
	private void getEstrutura(String estrutura, List<Integer> grupos, int tamanhoTotal) {
		tamanhoTotal = tamanhoTotal + StringUtils.substringBeforeFirst(estrutura, "|").length();
		grupos.add(Integer.valueOf(tamanhoTotal));
		estrutura = StringUtils.substringAfterFirst(estrutura, "|");
		if(!estrutura.contains("|")) {
			grupos.add(Integer.valueOf(tamanhoTotal + estrutura.length()));
			tamanhoTotal = tamanhoTotal + estrutura.length();
		}else {
			getEstrutura(estrutura, grupos, tamanhoTotal);
		}
	}
	
	private Map<Long, List<Long>> compoeMapImobByReclass(Integer dataRef, int tipoData) {
		Map<Long, List<Long>> mapUltReclas = new HashMap<>();
		
		List<Ecb0101> ecb0101s = buscaUltimasReclassificacoesByDataReferencia(dataRef, tipoData);
		
		List<Long> idsEcb01;
		for(Ecb0101 ecb0101 : ecb0101s) {
			idsEcb01 = new ArrayList<Long>();
			
			Abb11 abb11 = ecb0101.getEcb0101depto();
			Long key = abb11.getAbb11id();
			
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
	
	private List<TableMap> buscaDepartamentosCodigoNome() {
		Query query = getSession().createQuery(" SELECT abb11id as id, abb11codigo as codigo, abb11nome as nome ",
											   " FROM Abb11 ",
											   getSamWhere().getWherePadrao("WHERE", Abb11.class),
											   " ORDER BY abb11codigo");
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
	
	private void somaGrausSuperiores(List<TableMap> dados, int tamMaxGrupos) {
		for(TableMap tmDado : dados) {
			BigDecimal saldoAnterior = 0.0;
			BigDecimal deprec = 0.0;
			BigDecimal baixas = 0.0;
			BigDecimal saldoAtual = 0.0;
			BigDecimal transf = 0.0;
			
			String codigo = tmDado.getString("codigo");
			int tam = codigo.length();
			
			if(tam <= tamMaxGrupos) {
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
//meta-sis-eyJkZXNjciI6IlNBUCAtIFJlc3VtbyBkYSBEZXByZWNpYcOnw6NvIHBvciBEZXBhcnRhbWVudG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=