package multitec.relatorios.sca

import sam.model.entities.ab.Abh21;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fc.Fca20;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Banco de Horas Analítico
 * @author Lucas Eliel
 * @since 08/05/2019
 * @version 1.0
 */

public class SCA_BancoDeHorasAnalitico extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Banco de Horas Analítico";
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
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("datas", datas);
		filtrosDefault.put("imprimirDemitidos", false);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsMapHorario = getListLong("mapHorario");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		
		boolean isDemitidos = get("imprimirDemitidos");
		
		LocalDate[] datas = getIntervaloDatas("datas");
		
		Integer numMesesInicial = DateUtils.numMeses(datas[0].getMonthValue(), datas[0].getYear());
		Integer numMesesFinal = DateUtils.numMeses(datas[1].getMonthValue(), datas[1].getYear());
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		List<TableMap> bancoHoras = findBancoHorasByRelatorioAnalitico(idsTrabalhadores, idsCargos, idsDeptos, idsMapHorario, tiposTrab, isDemitidos, numMesesInicial, numMesesFinal);
		if(bancoHoras != null && !bancoHoras.isEmpty()) {
			String trabalhador = null;
			int saldo = 0;
			
			for(TableMap fca2001 : bancoHoras) {
				if(trabalhador != null && !trabalhador.equals(fca2001.getString("abh80codigo"))) saldo = 0;
				
				if(fca2001.getInteger("fca2001quita").equals(0)) {
					if(fca2001.getInteger("abh21tipo").equals(Abh21.TIPO_RENDIMENTO)){
						saldo += fca2001.getInteger("fca2001horas");
					}else{
						saldo -= fca2001.getInteger("fca2001horas");
						fca2001.put("fca2001horas", fca2001.getInteger("fca2001horas") * -1)
					}

					fca2001.put("horasRealizadas", fca2001.getInteger("fca2001horas"));
				} else {
					if(fca2001.getInteger("abh21tipo").equals(Abh21.TIPO_RENDIMENTO)){
						saldo += fca2001.getInteger("fca2001horas");
					}else{
						saldo -= fca2001.getInteger("fca2001horas");
						fca2001.put("fca2001horas", fca2001.getInteger("fca2001horas") * -1)
					}
					fca2001.put("horasQuitadas", fca2001.getInteger("fca2001horas"));
				}
				fca2001.put("saldo", saldo);
				
				trabalhador = fca2001.getString("abh80codigo");
			}
		}
		return gerarPDF("SCA_BancoDeHorasAnalitico", bancoHoras);
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
	 * @return 	List TableMap (Query da busca - Banco de Horas Analítico)
	 */
	private List<TableMap> findBancoHorasByRelatorioAnalitico(List<Long> idsTrabalhadores, List<Long> idsCargos, List<Long> idsDeptos, List<Long> idsMapHorario, Set<Integer> tiposAbh80, boolean isDemitidos, Integer numMesesInicial, Integer numMesesFinal){
		String whereDemitidos = isDemitidos ? "" : " AND abh80sit <> 1";
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereMapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? " AND abh13id IN (:idsMapHorario) " : "";
		
		String sql = "SELECT abh80codigo, abh21tipo, abh80nome, abh80hs, abb11codigo, abb11nome, abh05codigo, abh05nome, fca2001data, fca2001horas, fca2001quita " +
			     	 "FROM Fca20 " +
			     	 "INNER JOIN Fca2001 ON fca20id = fca2001bh "+
					 "INNER JOIN Abh21 ON abh21id = fca2001eve " +
			     	 "INNER JOIN Abh80 ON abh80id = fca20trab " +
			     	 "INNER JOIN Abb11 ON abb11id = abh80depto " +
			     	 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
			     	 "LEFT JOIN Abh13 ON abh13id = abh80mapHor " +
			     	 " WHERE "+ Fields.numMeses("fca20mes", "fca20ano") + " BETWEEN :numMesesInicial AND :numMesesFinal " +
			     	 "AND abh80tipo IN (:tiposAbh80) " + whereDemitidos +
			     	 whereTrabalhadores + whereDeptos + whereCargos + whereMapHorario + 
			     	 getSamWhere().getWherePadrao("AND", Fca20.class) +
			     	 " ORDER BY abh80codigo, fca2001data ";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameters("numMesesInicial", numMesesInicial);
		query.setParameter("numMesesFinal", numMesesFinal);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIEJhbmNvIGRlIEhvcmFzIEFuYWzDrXRpY28iLCJ0aXBvIjoicmVsYXRvcmlvIn0=