package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh80;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Dependentes
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeDependentes extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Dependentes";
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
		filtrosDefault.put("ordenacao", "0");
		LocalDate[] periodoNascimento = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodoNascimento", periodoNascimento);
		
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
		Integer ordenacao = getInteger("ordenacao");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		LocalDate[] periodoNascimento = getIntervaloDatas("periodoNascimento");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(periodoNascimento[0]) + " à " + DateUtils.formatDate(periodoNascimento[1]));
		
		List<TableMap> dados = findDadosAbh8002sByRelacaoDependentes(idsTrabalhadores, idsDeptos, idsCargos, idsParentescos, periodoNascimento, tiposTrab, ordenacao);	
		if(dados != null && !dados.isEmpty()) {
			for(TableMap mapPrinc : dados) {
				String sf = mapPrinc.getInteger("abh8002sf") == 0 ? "0-Não" : (mapPrinc.getInteger("abh8002sf") == 1 ? "1-Sim" : "2-Inválido");
				String ir = mapPrinc.getInteger("abh8002ir") == 0 ? "0-Não" : "1-Sim";			

				mapPrinc.put("sf", sf);
				mapPrinc.put("ir", ir);
			}
		}
		
		return gerarPDF("SFP_RelacaoDeDependentes_R1", dados);
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
	 * @return 	List TableMap (Query da busca - Relação de Dependentes)
	 */
	private List<TableMap> findDadosAbh8002sByRelacaoDependentes(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsParentescos, LocalDate[] periodoNascimento, Set<Integer> tiposAbh80, int ordenacao) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereParentesco = idsParentescos != null && !idsParentescos.isEmpty() ? " AND aap09id IN (:idsParentescos) " : "";
		String ordem = ordenacao == 0 ? " ORDER BY abh80codigo " : (ordenacao == 1 ? " ORDER BY abh80nome " : " ORDER BY abb11codigo ");
		String whereData = periodoNascimento != null ? getWhereDataInterval("AND", periodoNascimento, "abh8002dtNasc") : "";
		
		String sql = "SELECT abh80codigo, abh80nome, abb11codigo, abh8002nome, abh8002dtNasc, aap09codigo, aap09descr, abh8002sf, abh8002ir, abh8002cpf " +
				     "FROM Abh8002 " +
				     "INNER JOIN Abh80 ON abh80id = abh8002trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap09 ON aap09id = abh8002parente " +
				     "WHERE abh80tipo IN (:tiposAbh80) AND abh80dtResTrans IS NULL " + whereData +
				     whereTrabalhadores + whereDeptos + whereCargos + whereParentesco + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsParentescos != null && !idsParentescos.isEmpty()) query.setParameter("idsParentescos", idsParentescos);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBEZXBlbmRlbnRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==