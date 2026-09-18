package multitec.relatorios.sap


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ec.Eca01;
import sam.model.entities.ec.Ecb01
import sam.model.entities.ec.Ecb0101;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SAP_Imobilizacoes extends RelatorioBase {

	@Override
	public DadosParaDownload executar() {
		LocalDate dtReferencia = DateUtils.parseDate("01/" + getString("dtReferencia"));
		List<Long> idsClas = getListLong("clas");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Imobilizações");
		params.put("MES_REFERENCIA", "Imobilização em : " + dtReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		List<Long> idsEcb0101s = new ArrayList<Long>();
		List<Ecb0101> ecb0101s = findUltimasReclassificacoesByDataReferencia(dtReferencia);
		if(ecb0101s != null && ecb0101s.size() > 0) {
			for (Ecb0101 ecb0101 : ecb0101s) {
				idsEcb0101s.add(ecb0101.getEcb0101id());
			}
		}else {
			idsEcb0101s.add(-1);
		}
		
		List<String> eca01s = new ArrayList<String>();
		List<TableMap> rsEca01s = findClassificacaoAndBensByDataImobOrAquis(idsClas, idsEcb0101s, dtReferencia);
		for (int i = 0; i < rsEca01s.size(); i++) {
			if(rsEca01s.get(i).getLong("ecb01id") != null) {
				eca01s.add(rsEca01s.get(i).getString("codigo"));
			}
		}
		
		List<TableMap> dados = new ArrayList<>();
		List<TableMap> rsVlrAtual = findVlrAtualComDataImobilizacaoIgualDtRef(dtReferencia, idsEcb0101s);
		
		for (int i = 0; i < rsEca01s.size(); i++) {
			String codigo = rsEca01s.get(i).getString("codigo");
			int tamCodigo = codigo.length();
			
			for (String eca01 : eca01s) {
				if(eca01.substring(0, tamCodigo).equals(rsEca01s.get(i).getString("codigo"))) {
					TableMap tm = new TableMap();
					tm.put("codigo", rsEca01s.get(i).getString("codigo"));
					tm.put("nome", rsEca01s.get(i).getString("nome"));
					tm.put("ecb01id", rsEca01s.get(i).getLong("ecb01id"));
					tm.put("abb20codigo", rsEca01s.get(i).getString("abb20codigo"));
					tm.put("abb20nome", rsEca01s.get(i).getString("abb20nome"));
					tm.put("abb20chapa", rsEca01s.get(i).getInteger("abb20chapa"));
					tm.put("abb20aquis", rsEca01s.get(i).getDate("abb20aquis"));
					tm.put("ecb01dtImob", rsEca01s.get(i).getDate("ecb01dtImob"));
					String documento = rsEca01s.get(i).getString("aah01codigo") != null ? rsEca01s.get(i).getString("aah01codigo") + " - " + rsEca01s.get(i).getString("aah01nome") : "";
					tm.put("documento", documento);
					tm.put("abb01num", rsEca01s.get(i).getInteger("abb01num"));
					tm.put("ecb01txDepr", rsEca01s.get(i).getBigDecimal_Zero("ecb01txDepr"));
					tm.put("abb20descr", rsEca01s.get(i).getString("abb20descr"));
					tm.put("entidade", rsEca01s.get(i).getString("abe01na"));
					String departamento = rsEca01s.get(i).getString("abb11codigo") != null ? rsEca01s.get(i).getString("abb11codigo") + " - " + rsEca01s.get(i).getString("abb11nome") : "";
					tm.put("departamento", departamento);
					
					//Valor Atual e Valor da Baixa
					BigDecimal vlrAtual = BigDecimal.ZERO;
					BigDecimal somaVlrAtual = BigDecimal.ZERO;
					if(rsVlrAtual != null && rsVlrAtual.size() > 0){
						for(int j = 0; j < rsVlrAtual.size(); j++){//Sub Total
							if(codigo.equals(rsVlrAtual.get(j).getString("eca01codigo").substring(0, tamCodigo))){
								somaVlrAtual = somaVlrAtual.add(rsVlrAtual.get(j).getBigDecimal_Zero("ecb01vlrAtual"));
							}
						}
						tm.put("somaVlrAtual", somaVlrAtual);
						
						if(rsEca01s.get(i).getString("abb20codigo") != null){ //Bens Patrimoniais
							for(int j = 0; j < rsVlrAtual.size(); j++){
								if(rsEca01s.get(i).getLong("ecb01id").equals(rsVlrAtual.get(j).getLong("ecb01id"))){
									vlrAtual = rsVlrAtual.get(j).getBigDecimal_Zero("ecb01vlratual");
									tm.put("vlrAtual", vlrAtual);
								}
							}
						}
					}
					dados.add(tm);
					break;
				}
			}
		}
		
		if(dados == null || dados.size() == 0) throw new ValidacaoException("Não foram encontrados registros para exibição.");
		
		return gerarPDF("SAP_Imobilizacoes", dados);
	}

	private List<TableMap> findVlrAtualComDataImobilizacaoIgualDtRef(LocalDate dtReferencia, List<Long> idsEcb0101s) {
		Query query = getSession().createQuery(" SELECT eca01codigo, ecb01id, ecb01vlrAtual " +
											   " FROM Ecb01 " +
											   " INNER JOIN Abb20 ON abb20id = ecb01bem " +
											   " INNER JOIN Ecb0101 ON ecb01id = ecb0101imob " +
											   " INNER JOIN Eca01 ON ecb0101clas = eca01id " +
											   " WHERE (DATE_PART('YEAR', ecb01dtImob) * 12 + DATE_PART('MONTH', ecb01dtImob)) = :qtdDtRef " +
											   " AND ecb0101id IN (:idsEcb0101s) ");
										   
		query.setParameter("qtdDtRef", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		query.setParameter("idsEcb0101s", idsEcb0101s);
		return query.getListTableMap();
	}

	private List<TableMap> findClassificacaoAndBensByDataImobOrAquis(List<Long> idsClas, List<Long> idsEcb0101s, LocalDate dtReferencia) {
		String where = idsClas != null && idsClas.size() > 0 ? " (eca01id IN (:idsClas)) AND " : "";
		Query query = getSession().createQuery(" SELECT eca01codigo as codigo, eca01nome as nome, ecb01id, abb20codigo, abb20nome, abb20chapa, abb20aquis, ecb01dtImob, " +
											   " aah01codigo, aah01nome, abb01num, ecb01txDepr, abb20descr, abb11codigo, abb11nome, abe01na " +
											   " FROM Eca01 LEFT JOIN Ecb0101 ON ecb0101clas = eca01id " +
											   " LEFT JOIN Ecb01 ON ecb01id = ecb0101imob LEFT JOIN Abb20 ON abb20id = ecb01bem " +
											   " LEFT JOIN Abb01 ON abb20central = abb01id LEFT JOIN Abb11 ON abb20depto = abb11id " +
											   " LEFT JOIN Abe01 ON abe01id = abb01ent LEFT JOIN Aah01 ON aah01id = abb01tipo " +
											   " WHERE " + where + "( (DATE_PART('YEAR', ecb01dtImob) * 12 + DATE_PART('MONTH', ecb01dtImob)) = :qtdDtRef OR ecb01bem IS NULL )" +
											   " AND (ecb0101id IN (:ecb0101s) OR ecb0101id IS NULL) " +
											   getSamWhere().getWherePadrao(" AND ", Eca01.class)+
											   " ORDER BY eca01codigo, abb20codigo");
		
		query.setParameter("qtdDtRef", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		query.setParameter("ecb0101s", idsEcb0101s);
		if(idsClas != null && idsClas.size() > 0) query.setParameter("idsClas", idsClas);
		return query.getListTableMap();
	}

	private List<Ecb0101> findUltimasReclassificacoesByDataReferencia(LocalDate dtReferencia) {
		Query query = getSession().createQuery(" SELECT * FROM ecb0101 " +
											   " INNER JOIN ecb01 ON ecb01id = ecb0101imob " +
											   " WHERE (ecb0101imob, ( ecb0101ano * 12 + ecb0101mes)) IN " +
											   " (SELECT ecb0101imob, MAX(( ecb0101ano * 12 + ecb0101mes)) " +
											   " FROM ecb0101 INNER JOIN ecb01 ON ecb01id = ecb0101imob " +
											   " WHERE ( ecb0101ano * 12 + ecb0101mes) <= :qtdDtRef " +
											   getSamWhere().getWherePadrao(" AND ", Ecb01.class) +
											   " GROUP BY ecb0101imob)");
										   
		query.setParameter("qtdDtRef", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		return query.getList(ColumnType.ENTITY);
	}

	@Override
	public String getNomeTarefa() {
		return "SAP - Imobilizações";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("dtReferencia", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		return Utils.map("filtros", filtrosDefault);
	}

}
//meta-sis-eyJkZXNjciI6IlNBUCAtIEltb2JpbGl6YcOnw7VlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==