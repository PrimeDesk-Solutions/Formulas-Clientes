package multitec.relatorios.sap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abb20;
import sam.model.entities.ec.Eca01;
import sam.model.entities.ec.Ecb01;
import sam.model.entities.ec.Ecb0101;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SAP_Baixas extends RelatorioBase {
	@Override
	public DadosParaDownload executar() {
		
		LocalDate dtReferencia = DateUtils.parseDate("01/" + getString("dtReferencia"));
		
		List<Long> idsBens = getListLong("bens");
		List<Long> idsClas = getListLong("clas");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Baixas");
		params.put("MES_REFERENCIA", "Bens Patrimoniais Baixados em : " + dtReferencia.format(DateTimeFormatter.ofPattern("MM/yyyy")));	

		List<Long> abb20s = findIdsBensBaixados(dtReferencia, idsBens);
		if(abb20s == null || abb20s.size() == 0){
			abb20s = null;
		}

		List<TableMap> dados = new ArrayList<>();
		List<Ecb0101> ecb0101s = findUltimasReclassificacoesByDataReferencia(dtReferencia);
		List<Long> idsEcb0101s = new ArrayList<>();
		for(Ecb0101 ecb0101 : ecb0101s){
			idsEcb0101s.add(ecb0101.getEcb0101id());
		}

		//Buscando valores da coluna: "Valor Atual e valor da baixa"
		List<TableMap> rsVlrAtual = findVlrAtualComDataBaixaIgualDtRef(dtReferencia, idsEcb0101s, idsBens);

		//Buscando valores da coluna: "Valor Depreciação"
		List<TableMap> rsVlrDepreciacoes = findDepreciacoesComDataBaixaIgualDtRef(dtReferencia, idsEcb0101s, idsBens);

		//Adicionando valores às colunas de valores 
		List<TableMap> rsEc01s = findClassificacaoAndBensByDataBaixa(idsClas, abb20s, idsEcb0101s, dtReferencia);
		String codigo;
		int tamCodigo;
		for(int i = 0; i < rsEc01s.size(); i++) {

			codigo = rsEc01s.get(i).getString("eca01codigo");
			tamCodigo = codigo.length();

			String key = rsEc01s.get(i).getString("eca01codigo") + "/" + rsEc01s.get(i).getString("abb20codigo");
			TableMap tm = new TableMap();
			tm.put("key", key);
			tm.put("eca01codigo", rsEc01s.get(i).getString("eca01codigo"));
			tm.put("eca01nome", rsEc01s.get(i).getString("eca01nome"));
			tm.put("abb20codigo", rsEc01s.get(i).getString("abb20codigo"));
			tm.put("abb20nome", rsEc01s.get(i).getString("abb20nome"));
			tm.put("abb20chapa", rsEc01s.get(i).getInteger("abb20chapa"));
			tm.put("abb20aquis", rsEc01s.get(i).getDate("abb20aquis"));
			tm.put("abb20baixa", rsEc01s.get(i).getDate("abb20baixa"));
			tm.put("abb20motivo", rsEc01s.get(i).getString("abb20motivo"));
			tm.put("ecb01dtimob", rsEc01s.get(i).getDate("ecb01dtimob"));

			//Valor Atual e Valor da Baixa
			BigDecimal vlrAtual = BigDecimal.ZERO;
			BigDecimal somaVlrAtual = BigDecimal.ZERO;
			BigDecimal vlrBaixa = BigDecimal.ZERO;
			BigDecimal somaVlrBaixa = BigDecimal.ZERO;
			if(rsVlrAtual != null && rsVlrAtual.size() > 0){
				for(int j = 0; j < rsVlrAtual.size(); j++){//Sub Total - Class ou CCusto
					if(codigo.equals(rsVlrAtual.get(j).getString("eca01codigo").substring(0, tamCodigo))){
						somaVlrAtual = somaVlrAtual.add(rsVlrAtual.get(j).getBigDecimal_Zero("ecb01vlrAtual"));
						somaVlrBaixa = somaVlrBaixa.add(rsVlrAtual.get(j).getBigDecimal_Zero("ecb01vlrBx"));
					}
				}

				if(rsEc01s.get(i).getString("abb20codigo") != null){ //Bens Patrimoniais
					for(int j = 0; j < rsVlrAtual.size(); j++){
						if(rsEc01s.get(i).getLong("ecb01id").equals(rsVlrAtual.get(j).getLong("ecb01id"))){
							vlrAtual = rsVlrAtual.get(j).getBigDecimal_Zero("ecb01vlrAtual");
							vlrBaixa = rsVlrAtual.get(j).getBigDecimal_Zero("ecb01vlrBx");
							break;
						}
					}					
				}
			}
			tm.put("somaVlrAtual", somaVlrAtual);
			tm.put("somaVlrBaixa", somaVlrBaixa);
			
			tm.put("vlrAtual", vlrAtual);
			tm.put("vlrBaixa", vlrBaixa);

			//Depreciação
			BigDecimal depPeriodo =  BigDecimal.ZERO;
			BigDecimal somaDepPeriodo = BigDecimal.ZERO;
			if(rsVlrDepreciacoes != null && rsVlrDepreciacoes.size() > 0){
				for(int j = 0; j < rsVlrDepreciacoes.size(); j++){//Sub Total - Class ou CCusto
					if(codigo.equals(rsVlrDepreciacoes.get(j).getString("eca01codigo").substring(0, tamCodigo))){
						somaDepPeriodo = somaDepPeriodo.add(rsVlrDepreciacoes.get(j).getBigDecimal_Zero("valorDepreciacao"));
					}
				}

				if(rsEc01s.get(i).getString("abb20codigo") != null){ //Bens Patrimoniais
					for(int j = 0; j < rsVlrDepreciacoes.size(); j++){
						if(rsEc01s.get(i).getLong("ecb01id").equals(rsVlrDepreciacoes.get(j).getLong("ecb01id"))){
							depPeriodo = rsVlrDepreciacoes.get(j).getBigDecimal_Zero("valorDepreciacao");
							break;
						}
					}
				}
			}
			tm.put("somaVlrDepreciacao", somaDepPeriodo);
			tm.put("vlrDepreciacao", depPeriodo);
			
			if(tm.getBigDecimal_Zero("somaVlrAtual") == 0 && tm.getBigDecimal_Zero("somaVlrBaixa") == 0 && tm.getBigDecimal_Zero("somaVlrDepreciacao") == 0) continue;
			
			dados.add(tm);
		}
		
		if(dados == null || dados.size() == 0) throw new ValidacaoException("Não foram encontrados registros para exibição.");

		Collections.sort(dados, new Comparator<TableMap>() {
	        @Override
	        public int compare(TableMap tm1, TableMap tm2)
	        {
	            return  tm1.getString("key").compareTo(tm2.getString("key"));
	        }
	    });

		return gerarPDF("SAP_Baixas", dados);
	}

	private List<TableMap> findClassificacaoAndBensByDataBaixa(List<Long> idsClas, List<Long> abb20s, List<Long> idsEcb0101s, LocalDate dtReferencia) {
		String whereClas = idsClas != null ? "AND eca01id IN (:idsClas) " : "";
		String whereAbb20s = abb20s != null && !abb20s.isEmpty() ? "AND (ecb01bem IN (:abb20s) OR ecb01bem IS NULL) " : "";
		String whereEcb0101s = idsEcb0101s != null && !idsEcb0101s.isEmpty() ? "AND (ecb0101id IN (:idsEcb0101s) OR ecb0101id IS NULL) " : "";
		
		String sql = "SELECT eca01codigo, eca01nome, ecb01id, abb20codigo, abb20nome,  abb20chapa, abb20aquis, ecb01dtimob, abb20baixa, abb20motivo " +
					 "FROM Eca01 " +
					 "LEFT JOIN Ecb0101 ON ecb0101clas = eca01id " +
					 "LEFT JOIN Ecb01 ON ecb01id = ecb0101imob " +
					 "LEFT JOIN Ecb0102 ON ecb0102imob = ecb01id " +
					 "LEFT JOIN Abb20 ON ecb01bem = abb20id " +
					 whereClas + whereAbb20s + whereEcb0101s + getSamWhere().getWherePadrao("AND", Eca01.class) + 
					 "GROUP BY eca01codigo, eca01nome, ecb01id, abb20codigo, abb20nome, abb20chapa, abb20aquis, " + 
					 "ecb01dtimob, abb20baixa, abb20motivo " +
					 "ORDER BY eca01codigo, abb20codigo";
		
		Query query = getSession().createQuery(sql);
			
		query.setParameter("baixa", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		if(abb20s != null && !abb20s.isEmpty()) query.setParameter("abb20s", abb20s);
		if(idsEcb0101s != null && !idsEcb0101s.isEmpty()) query.setParameter("idsEcb0101s", idsEcb0101s);

		return query.getListTableMap();
	}

	private List<TableMap> findDepreciacoesComDataBaixaIgualDtRef(LocalDate dtReferencia, List<Long> idsEcb0101s, List<Long> idsBens) {
		String whereBens = idsBens != null ? " AND abb20id IN (:idsBens) " : "";
		String whereEcb0101s = !idsEcb0101s.isEmpty() ? "AND ecb0101id IN (:idsEcb0101s) " : "";
		Query query = getSession().createQuery(" SELECT eca01codigo, ecb01id, SUM(ecb0102deprec) AS valorDepreciacao " + 
											   " FROM Ecb0102 " + 
											   " INNER JOIN Ecb01 ON ecb0102imob = ecb01id " +
											   " INNER JOIN Ecb0101 ON ecb01id = ecb0101imob " + 
											   " INNER JOIN Eca01 ON ecb0101clas = eca01id " +
											   " INNER JOIN Abb20 ON ecb01bem = abb20id " +
											   " WHERE (DATE_PART('YEAR', abb20baixa) * 12 +  DATE_PART('MONTH', abb20baixa)) = :dtReferencia " + 
											   whereEcb0101s + whereBens + getSamWhere().getWherePadrao("AND", Ecb01.class) + 
											   " GROUP BY eca01codigo, ecb01id");

		query.setParameter("dtReferencia", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		query.setParameter("idsEcb0101s", idsEcb0101s);
		if(idsBens != null) query.setParameter("idsBens", idsBens);
		return query.getListTableMap();
	}

	private List<TableMap> findVlrAtualComDataBaixaIgualDtRef(LocalDate dtReferencia, List<Long> idsEcb0101s, List<Long> idsBens) {
		String whereBens = idsBens != null ? " AND abb20id IN (:idsBens) " : "";
		String whereEcb0101s = !idsEcb0101s.isEmpty() ? "AND ecb0101id IN (:idsEcb0101s) " : "";
		Query query = getSession().createQuery(" SELECT eca01codigo, ecb01id, ecb01vlrAtual, ecb01vlrBx " + 
											   " FROM Ecb01 INNER JOIN Ecb0101 ON ecb01id = ecb0101imob " + 
											   " INNER JOIN Eca01 ON ecb0101clas = eca01id " +
											   " INNER JOIN Abb20 ON ecb01bem = abb20id " +
											   " WHERE " + Fields.numMeses(Fields.month("abb20baixa").toString(), Fields.year("abb20baixa").toString()) + 
											   " =:dtReferencia " + whereEcb0101s + whereBens + getSamWhere().getWherePadrao("AND", Ecb01.class));

		query.setParameter("dtReferencia", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		if(idsEcb0101s != null) query.setParameter("idsEcb0101s", idsEcb0101s);
		if(idsBens != null) query.setParameter("idsBens", idsBens);
		return query.getListTableMap();
	}

	private List<Ecb0101> findUltimasReclassificacoesByDataReferencia(LocalDate dtReferencia) {
		Query query = getSession().createQuery(" SELECT * FROM Ecb0101 " +
											   " INNER JOIN Ecb01 ON ecb01id = ecb0101imob " +
											   " WHERE (ecb0101imob, ( ecb0101ano * 12 + ecb0101mes)) IN " + 
											   " (SELECT ecb0101imob, MAX(ecb0101ano * 12 + ecb0101mes) " +
											   " FROM Ecb0101 INNER JOIN Ecb01 ON ecb01id = ecb0101imob " +
											   " WHERE (ecb0101ano * 12 + ecb0101mes) <= :qtdData " + 
											   getSamWhere().getWherePadrao("AND", Ecb01.class) + 
											   " GROUP BY ecb0101imob)");

		query.setParameter("qtdData", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		return query.getList(ColumnType.ENTITY);
	}

	private List<Long> findIdsBensBaixados(LocalDate dtReferencia, List<Long> idsBens) {
		String whereBens = idsBens != null ? " AND abb20id IN (:idsBens) " : "";
		Query query = getSession().createQuery(" SELECT abb20id FROM Abb20 " +
								 " WHERE " + Fields.numMeses(Fields.month("abb20baixa").toString(), 
										 Fields.year("abb20baixa").toString()) + " = :dtReferencia " +
								 " AND LENGTH (abb20codigo) > 2 " + whereBens + getSamWhere().getWherePadrao("AND", Abb20.class) +
								 " ORDER BY abb20codigo");
		
		query.setParameter("dtReferencia", Criterions.valNumMeses(dtReferencia.getMonthValue(), dtReferencia.getYear()));
		if(idsBens != null) query.setParameter("idsBens", idsBens);
		return query.getList(ColumnType.LONG);
	}

	@Override
	public String getNomeTarefa() {
		return "SAP - Baixas";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("dtReferencia", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		return Utils.map("filtros", filtrosDefault);
	}
}
//meta-sis-eyJkZXNjciI6IlNBUCAtIEJhaXhhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==