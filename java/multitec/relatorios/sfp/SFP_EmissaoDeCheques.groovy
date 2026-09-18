package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Extenso
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abf01;
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Emissão de Cheques
 * @author Lucas Eliel
 * @since 30/04/2019
 * @version 1.0
 */

public class SFP_EmissaoDeCheques extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Emissão de Cheques";
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
		LocalDate[] pagamentos = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("pagamentos", pagamentos);
		filtrosDefault.put("dataBase", MDate.date());
		filtrosDefault.put("calcFolha", true);
		filtrosDefault.put("calcFerias", true);
		filtrosDefault.put("calcRescisao", true);
		filtrosDefault.put("calcAdiantamento", false);
		filtrosDefault.put("calc13sal", false);
		filtrosDefault.put("calcPlr", false);
		filtrosDefault.put("calcOutros", false);
		filtrosDefault.put("ordenacao", "0");

		return Utils.map("filtros", filtrosDefault);
	}

	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		int ordenar = getInteger("ordenacao"); // 0 - codigo trab; 1 - nome trab; 2 - depto; 3 - agência

		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		Long idBanco = getLong("banco");

		Set<Integer> tiposTrab = obterTipoTrabalhador();
		LocalDate[] dtPagamentos = getIntervaloDatas("pagamentos");
		LocalDate dataBase = getLocalDate("dataBase");

		Set<Integer> tiposCalc = obterTiposCalculo();
		if(tiposCalc == null || tiposCalc.size() == 0) {
			throw new ValidacaoException("Necessário informar um tipo de cálculo.");
		}

		//Obtêm os Parâmetros
		String eveLiqSalario = getParametros(Parametros.FB_EVELIQSALARIO);
		if(eveLiqSalario == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQSALARIO.");

		String eveLiqAdSalario = getParametros(Parametros.FB_EVELIQADSALARIO);
		if(eveLiqAdSalario == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQADSALARIO.");

		String eveLiqFerias = getParametros(Parametros.FB_EVELIQFERIAS);
		if(eveLiqFerias == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQFERIAS.");

		String eveLiqAbono = getParametros(Parametros.FB_EVELIQABONO);
		if(eveLiqAbono == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQABONO.");

		String eveResLiquida = getParametros(Parametros.FB_EVERESLIQUIDA);
		if(eveResLiquida == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVERESLIQUIDA.");

		String eveLiqAdiant13 = getParametros(Parametros.FB_EVELIQADIANT13);
		if(eveLiqAdiant13 == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQADIANT13.");

		String eveLiq13Salario = getParametros(Parametros.FB_EVELIQ13SALARIO);
		if(eveLiq13Salario == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQ13SALARIO.");

		Set<String> codigosEve = new HashSet<String>();
		if(eveLiqSalario != null && (tiposCalc.contains(0) || tiposCalc.contains(6))) codigosEve.add(eveLiqSalario);
		if(eveLiqAdSalario != null && tiposCalc.contains(3)) codigosEve.add(eveLiqAdSalario);
		if(eveLiqFerias != null && tiposCalc.contains(1)) codigosEve.add(eveLiqFerias);
		if(eveLiqAbono != null && tiposCalc.contains(1)) codigosEve.add(eveLiqAbono);
		if(eveLiqAdiant13 != null && tiposCalc.contains(1)) codigosEve.add(eveLiqAdiant13);
		if(eveResLiquida != null && tiposCalc.contains(2)) codigosEve.add(eveResLiquida);
		if(eveLiq13Salario != null && tiposCalc.contains(4)) codigosEve.add(eveLiq13Salario);

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

		params.put("EMP_RS", aac10.getAac10rs());
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_BAIRRO", aac10.getAac10bairro());
		params.put("EMP_CIDADE", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
		params.put("EMP_UF", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
		params.put("DATA_EMISSAO", DateUtils.formatDate(dataBase));

		Abf01 abf01 = getSession().createCriteria(Abf01.class).addWhere(Criterions.eq("abf01id", idBanco)).get();
		params.put("NOME_BANCO", abf01 != null ? abf01.getAbf01nome() : "");
		params.put("COD_BANCO", abf01 != null ? abf01.getAbf01numero() : "");
		params.put("AGENCIA", (abf01 != null && abf01.getAbf01digAgencia() == null) ? abf01.getAbf01agencia() : (abf01 != null && abf01.getAbf01digAgencia() != null) ? abf01.getAbf01agencia() + " - " + abf01.getAbf01digAgencia() : "");


		List<TableMap> dados = getDadosFba01011sByEmissaoCheques(idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, tiposCalc, codigosEve, dtPagamentos, ordenar, idBanco);
		if(dados != null && !dados.isEmpty()) {
			for(TableMap mapPrinc : dados) {
				String agencia = mapPrinc.getString("abh80bcoDigAg") == null ? StringUtils.concat(mapPrinc.getString("abh80bcoAgencia")) : StringUtils.concat(mapPrinc.getString("abh80bcoAgencia"), " - ", mapPrinc.getString("abh80bcoDigAg"));
				mapPrinc.put("agencia", agencia);

				String conta = mapPrinc.getString("abh80bcoDigCta") == null ? mapPrinc.getString("abh80bcoConta") == null ? "" : StringUtils.concat(mapPrinc.getString("abh80bcoConta"), " - ", mapPrinc.getString("abh80bcoDigCta")) : "";
				mapPrinc.put("conta", conta);

				Extenso extenso = new Extenso(mapPrinc.getBigDecimal("totalValor").abs());
				mapPrinc.put("vlrExtenso", extenso.toString());
			}
		}

		return gerarPDF("SFP_EmissaoDeCheques_R1", dados);
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
	 * @return 	Parametros (Aba01)
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
	 * @return 	Aag0201 (uf, municipio)
	 */
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addJoin(Joins.fetch("aag0201uf"))
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Emissão de Cheques)
	 */
	private List<TableMap> getDadosFba01011sByEmissaoCheques(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposTrab, Set<Integer> tiposCalc, Set<String> codigosEve, LocalDate[] dtPagamentos, int ordenar, Long idBanco){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";

		String ordem = ordenar == 0 ? "ORDER BY abh80codigo" : ordenar == 1 ? "ORDER BY abh80nome" : ordenar == 2 ? "ORDER BY abb11codigo" : "ORDER BY abh80bcoAgencia, abh80codigo";
		String whereBanco = idBanco != null ? " AND abh80bcoCod = :idAbf01 " : "";

		String whereData = dtPagamentos != null ? getWhereDataInterval("WHERE", dtPagamentos, "fba0101dtPgto") : "";

		String sql = "SELECT abh80codigo, abh80nome, abh80bcoConta, abh80bcoDigCta, abh80bcoAgencia, abh80bcoDigAg, abh80cpf, SUM(fba01011valor) as totalValor " +
				"FROM Fba01011 " +
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				"INNER JOIN Fba01 ON fba01id = fba0101calculo " +
				"INNER JOIN Abh80 ON abh80id = fba0101trab " +
				"INNER JOIN Abb11 ON abb11id = abh80depto " +
				"INNER JOIN Abh05 ON abh05id = abh80cargo " +
				"INNER JOIN Abh21 ON abh21id = fba01011eve " +
				whereData + " AND fba0101tpVlr IN (:tiposFba0101) AND abh80tipo IN (:tiposAbh80) AND abh21codigo IN (:codigosAbh21) " + whereBanco +
				whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Fba01.class) +
				" GROUP BY abh80codigo, abh80nome, abh80bcoConta, abh80bcoDigCta, abh80bcoAgencia, abh80bcoDigAg, abh80cpf, abb11codigo " +
				ordem;

		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposTrab);
		query.setParameter("tiposFba0101", tiposCalc);
		query.setParameter("codigosAbh21", codigosEve);

		if(idBanco != null) query.setParameter("idAbf01", idBanco);

		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);

		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIEVtaXNzw6NvIGRlIENoZXF1ZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=