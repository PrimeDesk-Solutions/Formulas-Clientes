package multitec.relatorios.sfp;

import java.time.LocalDate;
import java.time.format.TextStyle;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Declaração de Encargo de Familia Para IR
 * @author Lucas Eliel
 * @since 30/04/2019
 * @version 1.0
 */

public class SFP_DeclaracaoDeEncargoDeFamiliaParaIR extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Declaração de Encargo de Família para IR";
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
		filtrosDefault.put("situacao", "0");
		filtrosDefault.put("data", MDate.date());
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsParentescos = getListLong("parentescos");
		
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		LocalDate data = getLocalDate("data");
		Integer situacao = getInteger("situacao");
		
		params.put("DATA", data.getDayOfMonth() +" de "+ data.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "br")) +" de "+ data.getYear());
		params.put("EMP_RS", getVariaveis().getAac10().getAac10rs());
		params.put("EMP_NI", getVariaveis().getAac10().getAac10ni());
		
		Aac10 aac10 = getVariaveis().getAac10();
		String endereco = null;
		if(aac10.getAac10endereco() != null) {
			if(aac10.getAac10numero() != null) {
				endereco = StringUtils.concat(aac10.getAac10endereco(), ", ", aac10.getAac10numero());
			}else {
				endereco = aac10.getAac10endereco();
			}
			
			if(aac10.getAac10complem() != null) {
				endereco += StringUtils.concat(" - ", aac10.getAac10complem());
			}
		}
		
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_CEP", aac10.getAac10cep());
		params.put("EMP_CIDADE", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
		params.put("EMP_UF",  aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
		params.put("StreamSub1R1", carregarArquivoRelatorio("SFP_DeclaracaoDeEncargoDeFamiliaParaIR_R1_S1"));
		
		List<TableMap> dados = getDadosAbh80sByTermoResponsabilidadeSF(idsTrabalhadores, idsDeptos, idsCargos, idsParentescos, tiposTrab, situacao, false, true);	
		
		List<TableMap> dadosSub = getDadosAbh8002sByTermoResponsabilidadeSF(idsTrabalhadores, idsDeptos, idsCargos, idsParentescos, tiposTrab, situacao, false, true);
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
		dsPrincipal.addSubDataSource("DsSub1R1", dadosSub, "abh80id", "abh80id");
		
		return gerarPDF("SFP_DeclaracaoDeEncargoDeFamiliaParaIR_R1", dsPrincipal);
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
	 * @return 	List TableMap (Query da busca - Termo de Responsabilidade do Trabalhador)
	 */
	private List<TableMap> getDadosAbh80sByTermoResponsabilidadeSF(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsParentescos, Set<Integer> tiposAbh80, int sitTrab, boolean consideraSalFam, boolean consideraIR) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereParentesco = idsParentescos != null && !idsParentescos.isEmpty() ? " AND aap09id IN (:idsParentescos) " : "";
		String whereSit = sitTrab == 0 ? "AND abh80dtResTrans IS NULL " : sitTrab == 1 ? "AND abh80dtResTrans IS NOT NULL " : "";
		String whereSalFam = consideraSalFam ? "AND abh8002sf = 1 " : "";
		String whereIR = consideraIR ? "AND abh8002ir = 1 " : "";
		
		String sql = "SELECT abh80id, abh80codigo, abh80nome, abh80rgNum, abh80cpf, abh80ctpsNum, abh80ctpsSerie " +
				     "FROM Abh80 " +
				     "INNER JOIN Abh8002 ON abh80id = abh8002trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap09 ON aap09id = abh8002parente " +
	                 "WHERE abh80tipo IN (:tiposAbh80) " + whereSit + whereSalFam + whereIR +
				     whereTrabalhadores + whereDeptos + whereCargos + whereParentesco + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     "GROUP BY abh80id, abh80codigo, abh80nome, abh80rgNum, abh80cpf, abh80ctpsNum, abh80ctpsSerie " +
				     "ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsParentescos != null && !idsParentescos.isEmpty()) query.setParameter("idsParentescos", idsParentescos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Termo de Responsabilidade do Dependente)
	 */
	public List<TableMap> getDadosAbh8002sByTermoResponsabilidadeSF(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsParentescos, Set<Integer> tiposAbh80, int sitTrab, boolean consideraSalFam, boolean consideraIR) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereParentesco = idsParentescos != null && !idsParentescos.isEmpty() ? " AND aap09id IN (:idsParentescos) " : "";
		String whereSit = sitTrab == 0 ? "AND abh80dtResTrans IS NULL " : sitTrab == 1 ? "AND abh80dtResTrans IS NOT NULL " : "";
		String whereSalFam = consideraSalFam ? "AND abh8002sf = 1 " : "";
		String whereIR = consideraIR ? "AND abh8002ir = 1 " : "";
		
		String sql = "SELECT abh80id, abh8002trab, abh8002nome, aap09codigo, aap09descr, abh8002dtNasc, abh8002cpf " +
				     "FROM Abh8002 " +
				     "INNER JOIN Abh80 ON abh80id = abh8002trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap09 ON aap09id = abh8002parente " +
	                 "WHERE abh80tipo IN (:tiposAbh80) " + whereSit + whereSalFam + whereIR +
				     whereTrabalhadores + whereDeptos + whereCargos + whereParentesco + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     "ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsParentescos != null && !idsParentescos.isEmpty()) query.setParameter("idsParentescos", idsParentescos);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIERlY2xhcmHDp8OjbyBkZSBFbmNhcmdvIGRlIEZhbcOtbGlhIHBhcmEgSVIiLCJ0aXBvIjoicmVsYXRvcmlvIn0=