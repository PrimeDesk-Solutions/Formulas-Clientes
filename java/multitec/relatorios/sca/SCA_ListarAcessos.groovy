package multitec.relatorios.sca;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fc.Fcb10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Listar Acessos
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_ListarAcessos extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Listar Acessos";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("datas", datas);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsAcessante = getListLong("acessantes");
		List<Long> idsTipoAcesso = getListLong("tipoAcesso");
		LocalDate[] datas = getIntervaloDatas("datas");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		List<TableMap> dados = getAcessosByRelatorio(idsAcessante, idsTipoAcesso, datas);
		if(dados != null && !dados.isEmpty()) {
			for(TableMap fcb10 : dados) {
				String acessante  = StringUtils.concat(fcb10.get("fcb10codigo"), " - ", fcb10.get("fcb10nome"));
				fcb10.put("acessante", acessante);
			}
		}
		return gerarPDF("SCA_ListarAcessos", dados);
	}

	/**Método Diverso
	 * @return 	List de TableMap (Acessos)
	 */
	private List<TableMap> getAcessosByRelatorio(List<Long> idsAcessante, List<Long> idsTipoAcesso, LocalDate[] datas){
		String whereAcessante = idsAcessante != null && !idsAcessante.isEmpty() ? " AND fcb10id IN (:idsAcessante) " : "";
		String whereTipoAcesso = idsTipoAcesso != null && !idsTipoAcesso.isEmpty() ? " AND fcb01id IN (:idsTipoAcesso) " : "";
		String whereDataEnt = datas != null ? getWhereDataInterval("WHERE", datas, "fcb1001dtE") : "";
		
		String sql = "SELECT fcb10codigo, fcb10nome, fcb1001dtE, fcb1001hrE, fcb1001dtS, fcb1001hrS, fcb01codigo, fcb01descr, fcb1001mtAces, fcb1001empresa, " +
			     	 "fcb1001ativ, fcb1001cargo, fcb1001obs " +
			     	 "FROM Fcb1001 " +
			     	 "INNER JOIN Fcb10 ON fcb10id = fcb1001aces " +
			     	 "INNER JOIN Fcb01 ON fcb01id = fcb1001tpAces " +
			     	 whereDataEnt + 
			     	 whereAcessante + whereTipoAcesso + getSamWhere().getWherePadrao("AND", Fcb10.class) +
			     	 " ORDER BY fcb1001aces, fcb1001dtE, fcb1001hrE";
		
		Query query = getSession().createQuery(sql);
		if(idsAcessante != null && !idsAcessante.isEmpty()) query.setParameter("idsAcessante", idsAcessante);
		if(idsTipoAcesso != null && !idsTipoAcesso.isEmpty()) query.setParameter("idsTipoAcesso", idsTipoAcesso);
		
		return query.getListTableMap();
	}

}
//meta-sis-eyJkZXNjciI6IlNDQSAtIExpc3RhciBBY2Vzc29zIiwidGlwbyI6InJlbGF0b3JpbyJ9