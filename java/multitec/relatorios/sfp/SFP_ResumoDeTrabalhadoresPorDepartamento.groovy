package multitec.relatorios.sfp;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Resumo de Trabalhadores por Departamento
 * @author Lucas Eliel
 * @since 27/01/2019
 * @version 1.0
 */

public class SFP_ResumoDeTrabalhadoresPorDepartamento extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Resumo de Trabalhadores por Departamento";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		filtrosDefault.put("listagem", "0");
		filtrosDefault.put("considerar", "0");
		filtrosDefault.put("isImpSalario", true);
		filtrosDefault.put("isCalcSalario", true);
		filtrosDefault.put("isImpDemitidos", false);
		filtrosDefault.put("posicao", MDate.date());
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		List<Long> idsDeptos = getListLong("departamentos");
		LocalDate dtPosicao = getLocalDate("posicao");
		Integer rdoListagem = getInteger("listagem");
		Integer rdoConsiderar = getInteger("considerar");
		Set<Integer> detalhes = obterDetalhes();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
		String dtPos = dtPosicao.format(formatter);
		
		LocalDate dtAtual = MDate.date();
		String dtNow = dtAtual.format(formatter);
		
		Aac10 aac10 = getVariaveis().getAac10();
		
		params.put("TITULO_RELATORIO", "Resumo de Trabalhadores por Departamento");
		params.put("DATA_POS", rdoConsiderar == 1 ? dtPos : dtNow);
		params.put("EMPRESA", aac10.getAac10na());
		
		List<TableMap> resumoDeTrabalhadoresPorDepartamento = getDadosRelatorioTrabalhadoresPorDepartamento(idsTrabalhadores, idsDeptos, tiposTrab, rdoListagem, rdoConsiderar, dtPosicao, detalhes);
		
		String relatorio;
		
		if(rdoListagem == 1){
			relatorio = "SFP_ResumoDeTrabalhadoresPorDepartamento_R2";
		}else {
			relatorio = "SFP_ResumoDeTrabalhadoresPorDepartamento_R1";
		}
		return gerarPDF(relatorio, resumoDeTrabalhadoresPorDepartamento);
	}

	/**Método Diverso
	 * @return Set Integer (Tipo de Trabalhador)
	 */
	private Set<Integer> obterTipoTrabalhador(){
		Set<Integer> detalhes = new HashSet<>();
		
		if((boolean) get("isTrabalhador")) detalhes.add(0);
		if((boolean) get("isAutonomo")) detalhes.add(1);
		if((boolean) get("isProlabore")) detalhes.add(2);
		if((boolean) get("isTerceiros")) detalhes.add(3);
		
		if(detalhes.size() == 0) {
			detalhes.add(0);
			detalhes.add(1);
			detalhes.add(2);
			detalhes.add(3);
		}
		return detalhes;
	}
	
	/**Método Diverso
	 * @return Set Integer (Tipos de Detalhes do Relatório)
	 */
	private Set<Integer> obterDetalhes(){
		Set<Integer> tiposTrab = new HashSet<>();
		
		if((boolean) get("isImpSalario")) tiposTrab.add(0);
		if((boolean) get("isCalcSalario")) tiposTrab.add(1);
		if((boolean) get("isImpDemitidos")) tiposTrab.add(2);
		
		if(tiposTrab.size() == 0) {
			tiposTrab.add(0);
			tiposTrab.add(1);
			tiposTrab.add(2);
			tiposTrab.add(3);
		}
		return tiposTrab;
	}
	
	/**Método Diverso
	 * @return Integer (Situação do Trabalhador)
	 */
	private int obterSituacaoDoTrabalhador(String codTrab) {
		Abh80 abh80 = getSession().createCriteria(Abh80.class)
				.addWhere(Criterions.eq("abh80codigo", codTrab))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Abh80.class)))
				.get();
		
		Integer sit = null;
		if(abh80 != null) {
			sit = abh80.getAbh80tipo();
		}
		return sit; //0-Ativo, 1-Demitido, 2-Transferido 
	}
	
	/**Método Diverso
	 * @return List TableMap (Query da busca do Trabalhador pelo Relatorio Por Departamento)
	 */
	public List<TableMap> findDadosAbh80sByRelatorioPorDepartamento(List<Long> idsTrabalhadores,List<Long> idsDeptos, Set<Integer> tiposTrab, Set<Integer> detalhes){
		Boolean impSalario = false;
		Boolean calcSalario = false;
		Boolean impDemitidos = false;
		
		for(Integer detalhe : detalhes) {
			if(detalhe.intValue() == 0) {
				impSalario = true;
			}else if(detalhe.intValue() == 1) {
				calcSalario = true;
			}else if(detalhe.intValue() == 2) {
				impDemitidos = true;
			}
		}
		
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abh80depto IN (:idsDeptos) " : "";
		String whereDemitidos = impDemitidos ? "" : "AND abh80dtResTrans IS NULL ";

		String sql = "SELECT abh80codigo as codTrab, abh80nome as nomeTrab, abb11codigo as codDepto, abb11nome as nomeDepto, abh05codigo as codCargo, abh05nome as nomeCargo, aap03codigo as cbo, " +
				     "abh80dtadmis as admisTrab, abh80nascData as nascTrab, abh80salario as salTrab, abh80unidPagto as salTipoTrab, abh80hs as hsTrab, abh80sexo as sexoTrab " +
				     "FROM Abh80 " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap03 ON aap03id = abh05cbo " +
					 "WHERE abh80tipo IN (:tiposAbh80) " + whereDemitidos +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Abh80.class) +
					 " ORDER BY abb11codigo, abh80codigo";

		Query query = getSession().createQuery(sql);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		query.setParameter("tiposAbh80", tiposTrab);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return List TableMap (Query da busca dos Eventos pelo Relatorio Por Departamento)
	 */
	public List<TableMap> findDadosFba01011sByRelatorioPorDepartamento(List<Long> idsTrabalhadores,List<Long> idsDeptos, Set<Integer> tiposTrab, int mes, int ano, Set<Integer> detalhes){
		Boolean impSalario = false;
		Boolean calcSalario = false;
		Boolean impDemitidos = false;
		
		for(Integer detalhe : detalhes) {
			if(detalhe.intValue() == 0) {
				impSalario = true;
			}else if(detalhe.intValue() == 1) {
				calcSalario = true;
			}else if(detalhe.intValue() == 2) {
				impDemitidos = true;
			}
		}
		
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND Abb11id IN (:idsDeptos) " : "";
		String whereDemitidos = impDemitidos ? "" : " AND (DATE_PART('MONTH', abh80dtResTrans) <> "+mes+" OR abh80dtResTrans is null)";

		String sql = "SELECT DISTINCT abh80codigo as codTrab, abh80nome as nomeTrab, abb11codigo as codDepto, abb11nome as nomeDepto, abh05codigo as codCargo, abh05nome as nomeCargo, aap03codigo as cbo, " +
				     "abh80dtadmis as admisTrab, abh80nascData as nascTrab, abh80salario as salTrab, abh80unidPagto as salTipoTrab, abh80hs as hsTrab, abh80sexo as sexoTrab, abh80tipo as sitTrab " +
				     "FROM Fba01011 " +
				     "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				     "INNER JOIN Abh80 ON abh80id = fba0101trab " +
				     "INNER JOIN Abb11 ON abb11id = fba01011depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap03 ON aap03id = abh05cbo " +
					 "WHERE abh80tipo IN (:tiposAbh80) AND DATE_PART('MONTH', fba0101dtCalc) = "+mes+" AND DATE_PART('YEAR', fba0101dtCalc) = "+ ano + whereDemitidos +
					 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Abh80.class) +
					 " ORDER BY abb11codigo, abh80codigo DESC";

		Query query = getSession().createQuery(sql);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		query.setParameter("tiposAbh80", tiposTrab);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return List TableMap (Query da busca dos Afastamentos e Retornos pelo Resumo de Trabalhadores por Departamento)
	 */
	public List<TableMap> findDadosFbb01sByResumoTrabalhadoresPorDepartamento(List<Long> idsTrabalhadores,List<Long> idsDeptos, Set<Integer> tiposTrab, LocalDate data){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abh80depto IN (:idsDeptos) " : "";

		String sql = "SELECT abh80codigo as codTrab, abh80nome as nomeTrab, abb11codigo as codDepto, abb11nome as nomeDepto, abh05codigo as codCargo, abh05nome as nomeCargo, aap03codigo as cbo, " +
			     	 "abh80dtadmis as admisTrab, abh80nascData as nascTrab, abh80salario as salTrab, abh80unidPagto as salTipoTrab, abh80hs as hsTrab, abh80sexo as sexoTrab " +
			     	 "FROM Fbb01 " +
			     	 "INNER JOIN Abh80 ON abh80id = fbb01trab " +
			     	 "INNER JOIN Abb11 ON abb11id = abh80depto " +
			     	 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
			     	 "LEFT JOIN Aap03 ON aap03id = abh05cbo " +
			     	 "WHERE abh80tipo IN (:tiposAbh80) AND ((:data BETWEEN fbb01dtSai AND fbb01dtRet) OR (:data >= fbb01dtSai AND fbb01dtRet IS NULL)) " +
			     	 whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Abh80.class);
		
		Query query = getSession().createQuery(sql);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		query.setParameter("tiposAbh80", tiposTrab);
		query.setParameter("data", data);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return List TableMap (Query dos dados do Relatorio -Trabalhadores Por Departamento)
	 */
	public List<TableMap> getDadosRelatorioTrabalhadoresPorDepartamento(List<Long> idsTrabalhadores, List<Long> idsDeptos, Set<Integer> tiposTrab, int rdoListagem, int rdoConsiderar, LocalDate dtPosicao, Set<Integer> detalhes) {
		Boolean impSalario = false;
		Boolean calcSalario = false;
		Boolean impDemitidos = false;
		
		for(Integer detalhe : detalhes) {
			if(detalhe.intValue() == 0) {
				impSalario = true;
			}else if(detalhe.intValue() == 1) {
				calcSalario = true;
			}else if(detalhe.intValue() == 2) {
				impDemitidos = true;
			}
		}
		
		List<TableMap> dados = null;
		
		if(rdoConsiderar == 0) {
			dados = findDadosAbh80sByRelatorioPorDepartamento(idsTrabalhadores, idsDeptos, tiposTrab, detalhes);
		}else {
			
			dados = findDadosFba01011sByRelatorioPorDepartamento(idsTrabalhadores, idsDeptos, tiposTrab, dtPosicao.getDayOfMonth(), dtPosicao.getYear(), detalhes);
		}
		
		List<TableMap> mapDadosList = new ArrayList<>();
		
		for(TableMap map : dados) {
			
		    TableMap mapDados = new TableMap();
			String key = map.getString("codDepto") + "/" + map.getString("codTrab");

			//Situação
			int sitTrab = rdoConsiderar == 0 ? obterSituacaoDoTrabalhador(map.getString("codTrab")) : map.getInteger("sitTrab");
			String situacao = sitTrab == 0 ? "Trab" : sitTrab == 1 ? "Demit" :  "Transf";

 			//Salário
			BigDecimal salario = new BigDecimal(0);
 			if(impSalario) {
 				salario = map.getBigDecimal("salTrab");
 	 			if(calcSalario && map.getInteger("salTipoTrab") == 22739) {
 	 				salario = salario.multiply(new BigDecimal(map.getInteger("hsTrab"))).multiply(new BigDecimal(5)).setScale(2, RoundingMode.HALF_EVEN);
 	 			} 
 			}

 			int idade = MDate.date().getYear() - (map.getDate("nascTrab") == null ? 0 : map.getDate("nascTrab").getYear());

 			mapDados.put("codTrab", map.getString("codTrab"));
 			mapDados.put("nomeTrab", map.getString("nomeTrab"));
 			mapDados.put("codDepto", map.getString("codDepto"));
 			mapDados.put("nomeDepto", map.getString("nomeDepto"));
 			mapDados.put("codCargo", map.getString("codCargo"));
 			mapDados.put("nomeCargo", map.getString("nomeCargo"));
 			mapDados.put("cbo", map.getString("cbo"));
 			mapDados.put("admisTrab", map.getDate("admisTrab"));
 			mapDados.put("nascTrab", map.getDate("nascTrab"));
 			mapDados.put("sitTrab", sitTrab);
 			mapDados.put("situacao", situacao);
 			mapDados.put("sexoTrab", map.getInteger("sexoTrab"));
 			mapDados.put("salario", salario);
 			mapDados.put("idade", idade);
 			mapDados.put("key", key);

 			mapDadosList.add(mapDados);
		}
		
		//Trabalhadores afastados que não possuem cálculo de folha.
		if(rdoConsiderar != 0) {
			
			List<TableMap> Fbb01s = findDadosFbb01sByResumoTrabalhadoresPorDepartamento(idsTrabalhadores, idsDeptos, tiposTrab, dtPosicao);
			
			if(Fbb01s != null && Fbb01s.size() > 0) {
				for(TableMap map : Fbb01s) {
					
					TableMap mapDados = new TableMap();
					String key = map.getString("codDepto") + "/" + map.getString("codDepto");

		 			//Salário
					BigDecimal salario = new BigDecimal(0);
		 			if(impSalario) {
		 				salario = map.getBigDecimal("salTrab");
		 	 			if(calcSalario && map.getInteger("salTipoTrab") == 22739) salario = salario.multiply(new BigDecimal(map.getInteger("hsTrab"))).multiply(new BigDecimal(5)).setScale(2, RoundingMode.HALF_EVEN);
		 			}

		 			//Idade
		 			int idade = MDate.date().getYear() - map.getDate("nascTrab").getYear();
		 			 			
		 			mapDados.put("codTrab", map.getString("codTrab"));
		 			mapDados.put("nomeTrab", map.getString("nomeTrab"));
		 			mapDados.put("codDepto", map.getString("codDepto"));
		 			mapDados.put("nomeDepto", map.getString("nomeDepto"));
		 			mapDados.put("codCargo", map.getString("codCargo"));
		 			mapDados.put("nomeCargo", map.getString("nomeCargo"));
		 			mapDados.put("cbo", map.getString("cbo"));
		 			mapDados.put("admisTrab", map.getDate("admisTrab"));
		 			mapDados.put("nascTrab", map.getDate("nascTrab"));
		 			mapDados.put("sitTrab", 2);
		 			mapDados.put("situacao", "Transf");
		 			mapDados.put("sexoTrab", map.getInteger("sexoTrab"));
		 			mapDados.put("salario", salario);
		 			mapDados.put("idade", idade);
		 			mapDados.put("key", key);

		 			mapDadosList.add(mapDados);
				}
			}
		}

		List<TableMap> mapPrincipalList = new ArrayList<>();
		
		Collections.sort(mapDadosList, new Comparator<TableMap>() {
	        @Override
	        public int compare(TableMap tm1, TableMap tm2)
	        {
	            return  tm1.getString("key").compareTo(tm2.getString("key"));
	        }
	    });

		for(TableMap mapDados : mapDadosList) {
			
			TableMap mapPrincipal = new TableMap();

			mapPrincipal.put("codTrab", mapDados.getString("codTrab"));
 			mapPrincipal.put("nomeTrab", mapDados.getString("nomeTrab"));
 			mapPrincipal.put("codDepto", mapDados.getString("codDepto"));
 			mapPrincipal.put("nomeDepto", mapDados.getString("nomeDepto"));
 			mapPrincipal.put("codCargo", mapDados.getString("codCargo"));
 			mapPrincipal.put("nomeCargo", mapDados.getString("nomeCargo"));
 			mapPrincipal.put("cbo", mapDados.getString("cbo"));
 			mapPrincipal.put("admisTrab", mapDados.getDate("admisTrab"));
 			mapPrincipal.put("nascTrab", mapDados.getDate("nascTrab"));
 			mapPrincipal.put("sitTrab", mapDados.getInteger("sitTrab"));
 			mapPrincipal.put("situacao", mapDados.getString("situacao"));
 			mapPrincipal.put("sexoTrab", mapDados.getInteger("sexoTrab"));
 			mapPrincipal.put("salario", mapDados.getBigDecimal("salario"));
 			mapPrincipal.put("idade", mapDados.getInteger("idade"));
 			
 			mapPrincipalList.add(mapPrincipal);
		}
		return mapDadosList;
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlc3VtbyBkZSBUcmFiYWxoYWRvcmVzIHBvciBEZXBhcnRhbWVudG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=