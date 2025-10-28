package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Ficha de Salário Família
 * @author Lucas Eliel
 * @since 01/03/2019
 * @version 1.0
 */

public class SFP_FichaDeSalarioFamilia extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Ficha de Salário Família";
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
		LocalDate[] admissao = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("admissao", admissao);
		filtrosDefault.put("tipo", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return String, byte[] (Dados para Download)
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		LocalDate[] admissao = getIntervaloDatas("admissao");	
		Integer tipo = getInteger("tipo");
		Boolean isFichaAtestadoVac = (tipo == 1 ? true : false);
	
		Aac10 aac10 = getVariaveis().getAac10();
		
		String razaoSocial = aac10.getAac10rs();
		String CNPJ = aac10.getAac10ni();

		String endereco = null;
		if(aac10.getAac10endereco() != null){
			if(aac10.getAac10numero() != null){
				endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
			}else{
				endereco = aac10.getAac10endereco();
			}
			
			if(aac10.getAac10complem() != null) {
				endereco += " - " + aac10.getAac10complem();
			}
		}
		
		String bairro = aac10.getAac10bairro();
		String cidade = aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null;
		String UF = aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null;

		params.put("TITULO_RELATORIO", "Ficha de Salário Família");
		params.put("EMP_RS", razaoSocial);
		params.put("EMP_NI", CNPJ);
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_BAIRRO", bairro);
		params.put("EMP_CIDADE", cidade);
		params.put("EMP_UF", UF);
		params.put("StreamSub1R1", carregarArquivoRelatorio("SFP_FichaDeSalarioFamilia_R1_S1"));
		
		List<TableMap> relatorioFichaSalarioFamilia = getDadosRelatorioFichaSalarioFamilia(idsTrabalhadores, idsDeptos, idsCargos, admissao, tiposTrab, isFichaAtestadoVac);
		
		List<TableMap> relatorioSub = getDadosAbh8002sByFichaSalarioFamilia(idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, admissao);
		TableMapDataSource dsPrincipal = new TableMapDataSource(relatorioFichaSalarioFamilia);
		dsPrincipal.addSubDataSource("DsSub1R1", relatorioSub, "abh80id", "abh80id");
		
		String relatorio = (isFichaAtestadoVac ? "SFP_FichaDeSalarioFamilia_R2" : "SFP_FichaDeSalarioFamilia_R1");
		
		return gerarPDF(relatorio, dsPrincipal);
	}

	/**Método Diverso
	 * @return Set Integer (Tipo de Trabalhador)
	 */
	private Set<Integer> obterTipoTrabalhador(){
		Set<Integer> tiposTrab = new HashSet<>();
		
		if((boolean) get("isTrabalhador")) tiposTrab.add(0);
		if((boolean) get("isAutonomo")) tiposTrab.add(1);
		if((boolean) get("isProlabore")) tiposTrab.add(2);
		if((boolean) get("isTerceiros")) tiposTrab.add(3);
		
		if(tiposTrab.size() == 0) {
			tiposTrab.add(0);
			tiposTrab.add(1);
			tiposTrab.add(2);
			tiposTrab.add(3);
		}
		return tiposTrab;
	}

	/**Método Diverso
	 * @return 	Aag0201 (uf, municipio)
	 */
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addJoin(Joins.fetch("aag0201uf"))
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Ficha de Salário Família)
	 */
	public List<TableMap> getDadosRelatorioFichaSalarioFamilia(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, LocalDate[] admissao, Set<Integer> tiposTrab, Boolean imprimirAtestadoVac) {
		List<TableMap> relatorioPrincipal = new ArrayList<>();

		if(imprimirAtestadoVac) {
			relatorioPrincipal = getDadosAbh8002sByFichaSalarioFamilia(idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, admissao);
		}else {
			relatorioPrincipal = getDadosAbh80sByFichaSalarioFamilia(idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, admissao);
		}
		return relatorioPrincipal;
	}
	
	/**Método Diverso
	 * @return List TableMap (Query da busca Ficha de Salário Família - Abh8002 - Dependentes)
	 */
	public List<TableMap> getDadosAbh8002sByFichaSalarioFamilia(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposTrab, LocalDate[] admissao) {
     	String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereAdmissao = admissao != null ? getWhereDataInterval("WHERE", admissao, "abh80dtAdmis") : "";
		
		String sql = "SELECT abh80id, abh8002trab, abh8002nome, abh8002nat, abh80nascData, abh8002dtNasc, abh80portNatu, abh8002sf, abh8002ir, abh8002cartorio, abh8002registro, " +
				     "abh8002livro, abh8002folha, abh8002dtCert, abh8002dtBaixa, aap09codigo, aap09descr " +
				     "FROM Abh8002 " +
				     "INNER JOIN Abh80 ON abh80id = abh8002trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap09 ON aap09id = abh8002parente " +
				     whereAdmissao + " AND abh80tipo IN (:tiposTrab) AND abh8002sf = 1 " +
	                 whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     "ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		query.setParameter("tiposTrab", tiposTrab);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return List TableMap (Query da busca Ficha de Salário Família - Abh80 - Dependentes)
	 */
	public List<TableMap> getDadosAbh80sByFichaSalarioFamilia(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposTrab, LocalDate[] admissao) {
     	String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereAdmissao = admissao != null ? getWhereDataInterval("WHERE", admissao, "abh80dtAdmis") : "";
		
		String sql = "SELECT abh80id, abh80codigo, abh80nome, abh80dtadmis, abh80ctpsnum " +
				     "FROM Abh80 " +
				     "INNER JOIN Abh8002 ON abh80id = abh8002trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     whereAdmissao +" AND abh80tipo IN (:tiposTrab)  AND abh8002sf = 1 " +
				     whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     "GROUP BY abh80id, abh80codigo, abh80nome, abh80dtadmis, abh80ctpsnum " +
				     "ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		query.setParameter("tiposTrab", tiposTrab);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIEZpY2hhIGRlIFNhbMOhcmlvIEZhbcOtbGlhIiwidGlwbyI6InJlbGF0b3JpbyJ9