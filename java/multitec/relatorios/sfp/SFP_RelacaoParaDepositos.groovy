package multitec.relatorios.sfp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01;
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação para Depósitos
 * @author Lucas Eliel
 * @since 27/01/2019
 * @version 1.0
 */

public class SFP_RelacaoParaDepositos extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação para Depósitos";
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
		filtrosDefault.put("calcFolha", true);
		filtrosDefault.put("calcFerias", true);
		filtrosDefault.put("calcRescisao", true);
		filtrosDefault.put("calcAdiantamento", false);
		filtrosDefault.put("calc13sal", false);
		filtrosDefault.put("calcPlr", false);
		filtrosDefault.put("calcOutros", false);
		LocalDate data = MDate.date();
		filtrosDefault.put("data", data);
		LocalDate[] pagamentos = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("pagamentos", pagamentos);
		filtrosDefault.put("detalhamento", "0");
		filtrosDefault.put("ordenamento", "0");

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
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsSindicatos = getListLong("sindicatos");
		List<Long> idsBancos = getListLong("bancos");
		Set<Integer> tiposCalc = obterTiposCalculo();
		LocalDate data = getLocalDate("data");
		LocalDate[] pagamentos = getIntervaloDatas("pagamentos");
		Integer rdoDetail = getInteger("detalhamento");
		Integer ordenacao = getInteger("ordenamento");
		Boolean chkConsideraFolha = get("calcFolha");
		Boolean chkConsideraFerias = get("calcFerias");
		Boolean chkConsideraRescisao = get("calcRescisao");
		Boolean chkConsideraAdiantamento = get("calcAdiantamento");
		Boolean chkConsidera13Sal = get("calc13sal");
		Boolean chkCalcPlr = get("calcPlr");
		Boolean chkConsideraOutros = get("calcOutros");
		Boolean isAgencia = getInteger("ordenamento") == 3 ? true : false;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		Aac10 aac10 = getVariaveis().getAac10();

		String endereco = null;
		if(aac10.getAac10endereco() != null) {
			if(aac10.getAac10numero() != null) {
				endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
			}else {
				endereco = aac10.getAac10endereco();
			}
			if(aac10.getAac10complem() != null) {
				endereco += " - " + aac10.getAac10complem();
			}
		}

		params.put("TITULO_RELATORIO", "Relação para Depósitos");
		params.put("EMP_RS", aac10.getAac10rs());
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_BAIRRO", aac10.getAac10bairro());
		params.put("EMP_CIDADE", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
		params.put("EMP_UF", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
		params.put("DATA_DEPOSITO", data.format(formatter));
		params.put("IS_POR_AGENCIA", isAgencia);

		String eveFolhaLiquida = getParametros(Parametros.FB_EVELIQSALARIO);
		if(StringUtils.isNullOrEmpty(eveFolhaLiquida)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQSALARIO.");

		String eveAdLiquido = getParametros(Parametros.FB_EVELIQADSALARIO);
		if(StringUtils.isNullOrEmpty(eveAdLiquido)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQADSALARIO.");

		String eveFeriasLiquida = getParametros(Parametros.FB_EVELIQFERIAS);
		if(StringUtils.isNullOrEmpty(eveFeriasLiquida)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQFERIAS.");

		String eveAbonoLiquido = getParametros(Parametros.FB_EVELIQABONO);
		if(StringUtils.isNullOrEmpty(eveAbonoLiquido)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQABONO.");

		String eveAd13Liquido = getParametros(Parametros.FB_EVELIQADIANT13);
		if(StringUtils.isNullOrEmpty(eveAd13Liquido)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQADIANT13.");

		String eveResLiquida = getParametros(Parametros.FB_EVERESLIQUIDA);
		if(StringUtils.isNullOrEmpty(eveResLiquida)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVERESLIQUIDA.");

		String eve13SalLiquida = getParametros(Parametros.FB_EVELIQ13SALARIO);
		if(StringUtils.isNullOrEmpty(eve13SalLiquida)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQ13SALARIO.");

		Set<String> codigosEve = new HashSet<String>();

		if(eveFolhaLiquida != null && (chkConsideraFolha || chkConsideraOutros || chkCalcPlr)) codigosEve.add(eveFolhaLiquida);
		if(eveAdLiquido != null && chkConsideraAdiantamento) codigosEve.add(eveAdLiquido);
		if(eveFeriasLiquida != null && chkConsideraFerias) codigosEve.add(eveFeriasLiquida);
		if(eveAbonoLiquido != null && chkConsideraFerias) codigosEve.add(eveAbonoLiquido);
		if(eveAd13Liquido != null && chkConsideraFerias) codigosEve.add(eveAd13Liquido);
		if(eveResLiquida != null && chkConsideraRescisao) codigosEve.add(eveResLiquida);
		if(eve13SalLiquida != null && chkConsidera13Sal) codigosEve.add(eve13SalLiquida);

		List<TableMap> relacaoParaDepositos = getDadosRelacaoParaDepositos(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, idsBancos, tiposTrab, tiposCalc, codigosEve, pagamentos, ordenacao, rdoDetail == 1 ? true : false);

		String relatorio = null;
		if(rdoDetail == 1 && ordenacao == 2) {
			relatorio = "SFP_RelacaoParaDepositos_R3";
		}else {
			relatorio = ordenacao == 2 ? "SFP_RelacaoParaDepositos_R2" : "SFP_RelacaoParaDepositos_R1";
		}

		return gerarPDF(relatorio, relacaoParaDepositos);
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
	 * @return Set Integer (Tipo de Cálculo)
	 */
	private Set<Integer> obterTiposCalculo(){
		Set<Integer> calc = new HashSet<>();

		if((boolean) get("calcFolha")) calc.add(0);
		if((boolean) get("calcAdiantamento")) calc.add(1);
		if((boolean) get("calc13sal")) calc.add(2);
		if((boolean) get("calcFerias")) calc.add(3);
		if((boolean) get("calcRescisao")) calc.add(4);
		if((boolean) get("calcPlr")) calc.add(6);
		if((boolean) get("calcOutros")) calc.add(9);

		if(calc.size() == 0) {
			calc.add(0);
			calc.add(1);
			calc.add(2);
			calc.add(3);
			calc.add(4);
			calc.add(6);
			calc.add(9);
		}
		return calc;
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
	 * @return 	String (Parâmetro do SAM)
	 */
	private String getParametros(Parametro param) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", "FB"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.get();

		String conteudo = null;
		if(aba01 != null) {
			conteudo = aba01.getAba01conteudo();
		}
		return conteudo;
	}

	/**Método Diverso
	 * @return List TableMap (Query da Relacao Para Depositos para Eventos)
	 */
	public List<TableMap> getDadosFba01011sByRelacaoParaDepositos(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, List<Long> idsBancos, LocalDate[] pagamentos, Set<Integer> tiposTrab, Set<Integer> tiposCalc, Set<String> codigosEve, Integer ordenacao) {
		String campos             = ordenacao == 2 ? " abb11codigo, abb11nome, " : "";
		String grupo              = ordenacao == 2 ? " abb11codigo, abb11nome, " : "";
		String ordem              = ordenacao == 0 ? "ORDER BY abf01codigo, abh80codigo" : ordenacao == 1 ? "ORDER BY abf01codigo, abh80nome" : ordenacao == 2 ? "ORDER BY abf01codigo, abb11codigo, abh80codigo" : "ORDER BY abf01codigo, abh80bcoAgencia, abh80codigo";
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos        = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos        = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereSindicatos    = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
		String whereBancos        = idsBancos != null && !idsBancos.isEmpty() ? " AND abf01id IN (:idsBancos) " : "";
		String wherePagamentos    = pagamentos != null ? getWhereDataInterval("WHERE", pagamentos, "fba0101dtPgto") : "";

		String sql = "SELECT " + campos + " abh80codigo, abh80nome, abh80bcoCod, abh80bcoConta, abh80bcoDigCta, abh80bcoAgencia, abh80bcoDigAg, abh80cpf, abf01codigo, abf01nome, abf01numero, abf01agencia, SUM(fba01011valor) as totalValor " +
				"FROM Fba01011 " +
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				"INNER JOIN Fba01 ON fba01id = fba0101calculo " +
				"INNER JOIN Abh80 ON abh80id = fba0101trab " +
				"INNER JOIN Abb11 ON abb11id = abh80depto " +
				"INNER JOIN Abh05 ON abh05id = abh80cargo " +
				"LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
				"INNER JOIN Abh21 ON abh21id = fba01011eve " +
				"LEFT JOIN Abf01 ON abf01id = abh80bcoCod " +
				wherePagamentos + " AND fba0101tpVlr IN (:tiposCalc) AND abh80tipo IN (:tiposTrab) AND abh21codigo IN (:codigosEve) " +
				whereTrabalhadores + whereDeptos + whereCargos + whereSindicatos + whereBancos + getSamWhere().getWherePadrao("AND", Fba01.class) +
				" GROUP BY " + grupo + " abh80codigo, abh80nome, abh80bcoCod, abh80bcoConta, abh80bcoDigCta, abh80bcoAgencia, abh80bcoDigAg, abh80cpf, abf01codigo, abf01nome, abf01numero, abf01agencia, abb11codigo " +
				ordem;

		Query query = getSession().createQuery(sql);

		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		query.setParameter("tiposTrab", tiposTrab);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
		if(idsBancos != null && !idsBancos.isEmpty()) query.setParameter("idsBancos", idsBancos);
		query.setParameter("tiposCalc", tiposCalc);
		query.setParameter("codigosEve", codigosEve);

		return query.getListTableMap();
	}

	/**Método Diverso
	 * @return 	List TableMap (Relação para Depósitos)
	 */
	public List<TableMap> getDadosRelacaoParaDepositos(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, List<Long> idsBancos, Set<Integer> tiposTrab, Set<Integer> tiposCalc, Set<String> codigosEve, LocalDate[] pagamentos, Integer ordenacao, boolean isSintetico) {
		List<TableMap> mapPrincipal = getDadosFba01011sByRelacaoParaDepositos(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, idsBancos, pagamentos, tiposTrab, tiposCalc, codigosEve, ordenacao);

		for(int i = 0; i < mapPrincipal.size(); i++) {
			String agencia = mapPrincipal.get(i).getString("abh80bcoDigAg") == null ? mapPrincipal.get(i).getString("abh80bcoAgencia") : mapPrincipal.get(i).getString("abh80bcoAgencia") + " - " + mapPrincipal.get(i).getString("abh80bcoDigAg");
			mapPrincipal.get(i).put("agencia", agencia);

			String conta = mapPrincipal.get(i).getString("abh80bcoDigCta") == null ? mapPrincipal.get(i).getString("abh80bcoConta") : mapPrincipal.get(i).getString("abh80bcoConta") + " - " + mapPrincipal.get(i).getString("abh80bcoDigCta");
			mapPrincipal.get(i).put("conta", conta);
		}
		return mapPrincipal;
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBwYXJhIERlcMOzc2l0b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=