package multitec.relatorios.sap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abb11
import sam.model.entities.ec.Eca01
import sam.model.entities.ec.Ecb01;
import sam.model.entities.ec.Ecb0101;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SAP_BensPorDepartamentos extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "SAP - Bens por Departamento";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("dtReferencia", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		filtrosDefault.put("naoExibirBensTotalmenteDepreciados", false);
		return Utils.map("filtros", filtrosDefault);
	}
	
	@Override
	public DadosParaDownload executar() {
		LocalDate dtReferencia = DateUtils.parseDate("01/" + getString("dtReferencia"));
		List<Long> idsBens = getListLong("bens");
		List<Long> idsDep = getListLong("dep");
		boolean naoExibirBensTotalmenteDepreciados = getBoolean("naoExibirBensTotalmenteDepreciados");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Bens por Departamento");
		params.put("MES_REFERENCIA", "Mês/Ano : " + dtReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		String estrutura = buscaEstruturaCodigoDep();
		
		List<Integer> grupos = new ArrayList<>();
		
		getEstrutura(estrutura, grupos, 0);
		if(grupos.size() <= 1) interromper("Há menos de dois grupos na estrutura de códigos de departamento.");
		
		int tamMaxGrupos = grupos.get(grupos.size()-2);
		
		Integer dataRef = Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear());
		
		Map<Long, List<Long>> mapUltReclasMesRef = compoeMapImobByReclass(dataRef, idsBens);
		
		List<Long> idsEspec = new ArrayList<Long>();
		
		List<TableMap> dados = new ArrayList<>();
		
		List<TableMap> listTMPrincipal = buscaDepartamentosCodigoNome(idsDep);
		if(listTMPrincipal != null && listTMPrincipal.size() > 0) {
			for(int i = 0; i < listTMPrincipal.size(); i++) {
				TableMap tmPrincipal = listTMPrincipal.get(i);
				
				String key = tmPrincipal.getString("codigo");
				
				if(key.length() <= tamMaxGrupos) {
					TableMap tmDado = new TableMap();
					tmDado.put("codigo", tmPrincipal.getString("codigo"));
					tmDado.put("nome", tmPrincipal.getString("nome"));
					dados.add(tmDado);
				}else {
					idsEspec = mapUltReclasMesRef.get(tmPrincipal.getLong("id"));
					if(idsEspec != null && idsEspec.size() > 0) {
						for(Long idEspec : idsEspec) {
							List<TableMap> listTMImobilizacoes = buscaEspecializacoesBens(idsBens, idEspec);
							if(listTMImobilizacoes != null && listTMImobilizacoes.size() > 0) {
								for(int row = 0; row < listTMImobilizacoes.size(); row++) {
									TableMap tmImobilizacao = listTMImobilizacoes.get(row);
									
									BigDecimal depPeriodo = buscaValorDepreciacaoPeriodoOrAcumulada(dataRef, idEspec, true);
									
									BigDecimal depAcum = buscaValorDepreciacaoPeriodoOrAcumulada(dataRef, idEspec, false);
									
									BigDecimal depAnterior = depAcum.subtract(depPeriodo);
									
									BigDecimal vlrAtual = buscaVlrAtualComDataImobMenorIgualDtRef(dataRef, idEspec);
									
									BigDecimal custoLiq = vlrAtual.subtract(depAcum);
									
									BigDecimal percDep = 0.0;
									if(vlrAtual.compareTo(0) > 0) {
										percDep = (depAcum * 100) / vlrAtual;
										percDep = round(percDep, 2);
									}
									
									if(naoExibirBensTotalmenteDepreciados && depPeriodo == 0.0 && percDep == 100.00) continue;
									
									TableMap tmDado = new TableMap();
									
									tmDado.put("codigo", tmPrincipal.getString("codigo"));
									tmDado.put("nome", tmPrincipal.getString("nome"));
									
									tmDado.put("abb20codigo", tmImobilizacao.getString("abb20codigo"));
									tmDado.put("abb20nome", tmImobilizacao.getString("abb20nome"));
									tmDado.put("abb20chapa", tmImobilizacao.getInteger("abb20chapa"));
									tmDado.put("abb20aquis", tmImobilizacao.getDate("abb20aquis"));
									tmDado.put("ecb01txDepr", tmImobilizacao.getBigDecimal_Zero("ecb01txDepr"));
									
									tmDado.put("depPeriodo", depPeriodo);
									tmDado.put("depAcum", depAcum);
									tmDado.put("depAnterior", depAnterior);
									tmDado.put("vlrAtual", vlrAtual);
									tmDado.put("custoLiq", custoLiq);
									tmDado.put("percDep", percDep);
									
									dados.add(tmDado);
								}
							}
						}
					}
				}
			}
		}
		
		somaGrausSuperiores(dados);
		
		return gerarPDF("SAP_BensPorDepartamentos", dados);
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
	
	private Map<Long, List<Long>> compoeMapImobByReclass(Integer dataRef, List<Long> idsBens) {
		Map<Long, List<Long>> mapUltReclas = new HashMap<>();
		
		List<Ecb0101> ecb0101s = buscaUltimasReclassificacoesByDataReferencia(dataRef, idsBens);
		
		List<Long> idsEcb01;
		for(Ecb0101 ecb0101 : ecb0101s){
			idsEcb01 = new ArrayList<Long>();
			
			Abb11 abb11 = ecb0101.getEcb0101depto();
			Long key = abb11.getAbb11id();
			
			if(mapUltReclas.get(key) != null){
				idsEcb01 = mapUltReclas.get(key);
			}
			idsEcb01.add(ecb0101.getEcb0101imob().getEcb01id());

			mapUltReclas.put(key, idsEcb01);
		}
		
		return mapUltReclas;
	}
	
	private List<Ecb0101> buscaUltimasReclassificacoesByDataReferencia(Integer dataRef, List<Long> idsBens) {
		String whereBens = idsBens != null && idsBens.size() > 0 ? " ecb01bem IN (:idsBens) AND " : "";
		Query query = getSession().createQuery(" SELECT * FROM Ecb0101 ",
											   " INNER JOIN Ecb01 ON ecb01id = ecb0101imob ",
											   " WHERE ", whereBens, 
											   " (ecb0101imob, ", Fields.numMeses("ecb0101mes", "ecb0101ano"), ") IN ",
											   " (SELECT ecb0101imob, MAX(", Fields.numMeses("ecb0101mes", "ecb0101ano"), ") ",
											   " FROM ecb0101 INNER JOIN Ecb01 ON ecb01id = ecb0101imob ",
											   " WHERE ", Fields.numMeses("ecb0101mes", "ecb0101ano"), " <= :dataRef ",
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class),
											   " GROUP BY ecb0101imob)");
		if(idsBens != null && idsBens.size() > 0) query.setParameter("idsBens", idsBens);
		query.setParameter("dataRef", dataRef);
		return query.getList(ColumnType.ENTITY);
	}
	
	private List<TableMap> buscaDepartamentosCodigoNome(List<Long> idsDep) {
		String whereDep = idsDep != null && idsDep.size() > 0 ? " WHERE (abb11id IN (:idsDep)) AND " : " WHERE ";
		Query query = getSession().createQuery(" SELECT abb11id as id, abb11codigo as codigo, abb11nome as nome ",
											   " FROM Abb11 ",
											   whereDep,
											   getSamWhere().getWherePadrao("", Abb11.class),
											   " ORDER BY abb11codigo");
		if(idsDep != null && idsDep.size() > 0) query.setParameter("idsDep", idsDep);
		return query.getListTableMap();
	}
	
	private List<TableMap> buscaEspecializacoesBens(List<Long> idsBens, Long idEspec) {
		String whereBens = idsBens != null && idsBens.size() > 0 ? " AND ecb01bem IN (:idsBens) " : "";
		Query query = getSession().createQuery(" SELECT abb20codigo, abb20nome, abb20chapa, abb20aquis, ecb01txDepr ",
											   " FROM Ecb01 ",
											   " LEFT JOIN Abb20 ON abb20id = ecb01bem ",
											   " WHERE ecb01id = :idEspec ", whereBens,
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class),
											   " ORDER BY abb20codigo");
		if(idsBens != null && idsBens.size() > 0) query.setParameter("idsBens", idsBens);
		query.setParameter("idEspec", idEspec);
		return query.getListTableMap();
	}
	
	private BigDecimal buscaValorDepreciacaoPeriodoOrAcumulada(Integer dataRef, Long idEspec, boolean depPeriodo){
		String whereNumMeses = depPeriodo ? Fields.numMeses("ecb0102mes", "ecb0102ano").toString() + " = :dataRef " : Fields.numMeses("ecb0102mes", "ecb0102ano").toString() + " <= :dataRef ";
		String whereBaixa = depPeriodo ? "" : " AND (" + Fields.numMeses(Fields.month("abb20baixa").toString(), Fields.year("abb20baixa").toString()) + " > :dataRef OR abb20baixa IS NULL) ";
		Query query = getSession().createQuery(" SELECT SUM(ecb0102deprec) FROM Ecb0102 ",
											   " INNER JOIN Ecb01 ON ecb0102imob = ecb01id ",
											   " INNER JOIN Abb20 ON abb20id = ecb01bem ",
											   " WHERE ", whereNumMeses, " AND ecb01id = :idEspec ", whereBaixa,
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class));
		query.setParameter("dataRef", dataRef);
		query.setParameter("idEspec", idEspec);
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result != null ? result : BigDecimal.ZERO;
	}
	
	private BigDecimal buscaVlrAtualComDataImobMenorIgualDtRef(Integer dataRef, Long idEspec){
		Query query = getSession().createQuery(" SELECT ecb01vlrAtual FROM Ecb01 " +
											   " INNER JOIN Abb20 ON ecb01bem = abb20id " +
											   " WHERE (DATE_PART('YEAR', ecb01dtImob) * 12 + DATE_PART('MONTH', ecb01dtImob)) <= :dataRef AND " +
											   " (DATE_PART('YEAR', abb20baixa) * 12 + DATE_PART('MONTH', abb20baixa)) > :dataRef OR abb20baixa IS NULL AND " +
											   " ecb01id IN (:idEspec) ",
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class));
		query.setParameter("dataRef", dataRef);
		query.setParameter("idEspec", idEspec);
		query.setMaxResult(1);
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result != null ? result : BigDecimal.ZERO;
	}
	
	private void somaGrausSuperiores(List<TableMap> dados) {
		for(TableMap tmDado : dados) {
			BigDecimal depAnterior = 0.0;
			BigDecimal depPeriodo = 0.0;
			BigDecimal depAcum = 0.0;
			BigDecimal vlrAtual = 0.0;
			BigDecimal custoLiq = 0.0;
			
			String codigo = tmDado.getString("codigo");
			int tam = codigo.length();
			
			for(TableMap tmSup : dados) {
				String cod = tmSup.getString("codigo");
				
				if(cod.length() >= tam) {
					if(codigo.equals(cod.substring(0, tam))){
						depAnterior = depAnterior + tmSup.getBigDecimal_Zero("depAnterior");
						depPeriodo = depPeriodo + tmSup.getBigDecimal_Zero("depPeriodo");
						depAcum = depAcum + tmSup.getBigDecimal_Zero("depAcum");
						vlrAtual = vlrAtual + tmSup.getBigDecimal_Zero("vlrAtual");
						custoLiq = custoLiq + tmSup.getBigDecimal_Zero("custoLiq");
					}
				}
			}
			
			tmDado.put("somaDepAnterior", depAnterior);
			tmDado.put("somaDepPeriodo", depPeriodo);
			tmDado.put("somaDepAcum", depAcum);
			tmDado.put("somaVlrAtual", vlrAtual);
			tmDado.put("somaCustoLiq", custoLiq);
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNBUCAtIEJlbnMgcG9yIERlcGFydGFtZW50byIsInRpcG8iOiJyZWxhdG9yaW8ifQ==