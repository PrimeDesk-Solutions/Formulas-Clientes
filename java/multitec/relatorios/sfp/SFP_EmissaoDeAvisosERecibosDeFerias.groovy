package multitec.relatorios.sfp;

import java.time.LocalDate;

import org.springframework.http.MediaType;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Extenso
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abh21;
import sam.model.entities.fb.Fbc01;
import sam.model.entities.fb.Fbc0101;
import sam.model.entities.fb.Fbc01011;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Emissão de Avisos e Recibos de Férias
 * @author Lucas Eliel
 * @since 12/03/2019
 * @version 1.0
 */

public class SFP_EmissaoDeAvisosERecibosDeFerias extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Emissão de Avisos e Recibos de Férias";
	}

	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		LocalDate[] dataPgto = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("dataPgto", dataPgto);
		filtrosDefault.put("impSolicitAbonoPecuniario", true);
		filtrosDefault.put("impNotifSaidaFerias", true);
		filtrosDefault.put("impSolicitAdiant13Salario", true);
		filtrosDefault.put("impReciboFerias", true);
		filtrosDefault.put("impReciboAbono", true);
		filtrosDefault.put("impReciboAdiant13Salario", true);
		filtrosDefault.put("diasAdicPerGozo", 0000);

		return Utils.map("filtros", filtrosDefault);
	}

	/**Método Principal
	 * @return String, byte[] (Dados para Download)
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhador = getListLong("trabalhador");
		List<Long> idsDepartamento = getListLong("departamento");
		LocalDate[] dataPgto = getIntervaloDatas("dataPgto");
		Set<Integer> impressao = getImpressao();
		Integer diasAdicPerGozo = getInteger("diasAdicPerGozo");

		Aac10 aac10 = getVariaveis().getAac10();

		String endereco = null;
		if(aac10.getAac10endereco() != null) {
			if(aac10.getAac10numero() != null) {
				endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
			}else {
				endereco = aac10.getAac10endereco();
			}
		}
		String CEP = aac10.getAac10cep() != null ? aac10.getAac10cep() : null;

		params.put("TITULO_RELATORIO", "Emissão de Avisos e Recibos de Férias");
		params.put("EMP_RS", aac10.getAac10rs());
		params.put("EMP_SFPTI", aac10.getAac10ti());
		params.put("EMP_NI", aac10.getAac10ni());
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_COMPLEM", aac10.getAac10complem());
		params.put("EMP_BAIRRO", aac10.getAac10bairro());
		params.put("EMP_CEP", CEP);
		params.put("EMP_CIDADE", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
		params.put("EMP_UF", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
		params.put("StreamSub1R4", carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R4_S1"));
		params.put("StreamSub2R4", carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R4_S2"));
		params.put("StreamSub1R5", carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R5_S1"));
		params.put("StreamSub2R5", carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R5_S2"));
		params.put("StreamSub1R6", carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R6_S1"));

		String feriasLiquida = getParametros(Parametros.FB_EVELIQFERIAS);
		if(StringUtils.isNullOrEmpty(feriasLiquida)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQFERIAS.");

		String abonoLiquido = getParametros(Parametros.FB_EVELIQABONO);
		if(StringUtils.isNullOrEmpty(abonoLiquido)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQABONO.");

		String adiant13Liquido = getParametros(Parametros.FB_EVELIQADIANT13);
		if(StringUtils.isNullOrEmpty(adiant13Liquido)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQADIANT13.");

		String complFerRend = getParametros(Parametros.FB_EVECOMPLEMFERIASREND);
		if(StringUtils.isNullOrEmpty(complFerRend)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVECOMPLEMFERIASREND.");

		String complFerDesc = getParametros(Parametros.FB_EVECOMPLEMFERIASDESC);
		if(StringUtils.isNullOrEmpty(complFerDesc)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVECOMPLEMFERIASDESC.");

		String complAbonoRend = getParametros(Parametros.FB_EVECOMPLEMABONOREND);
		if(StringUtils.isNullOrEmpty(complAbonoRend)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVECOMPLEMABONOREND.");

		String complAbonoDesc = getParametros(Parametros.FB_EVECOMPLEMABONODESC);
		if(StringUtils.isNullOrEmpty(complAbonoDesc)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVECOMPLEMABONODESC.");

		Abh21 abh21Ferias = findByUniqueKey(feriasLiquida);
		if(abh21Ferias == null) throw new ValidacaoException("Evento não encontrado com o código fornecido no parâmetro FB_EVELIQFERIAS.");

		Abh21 abh21Abono = findByUniqueKey(abonoLiquido);
		if(abh21Abono == null) throw new ValidacaoException("Evento não encontrado com o código fornecido no parâmetro FB_EVELIQABONO.");

		Abh21 abh21Adiant13 = findByUniqueKeyAdiant(adiant13Liquido);
		if(abh21Adiant13 == null) throw new ValidacaoException("Evento não encontrado com o código fornecido no parâmetro FB_EVELIQADIANT13.");

		JasperPrint emissaoDeAvisosERecibosDeFerias = getDadosRelatorioAvisosRecibosFerias(idsTrabalhador, idsDepartamento, dataPgto, complFerRend, complFerDesc, complAbonoRend, complAbonoDesc, impressao, abh21Ferias.getAbh21id(), abh21Abono.getAbh21id(), abh21Adiant13.getAbh21id(), new Integer(diasAdicPerGozo));

		byte[] bytes;
		try {
			bytes = JasperExportManager.exportReportToPdf(emissaoDeAvisosERecibosDeFerias);
		} catch (JRException e) {
			throw new RuntimeException("Erro ao gerar o relatório da classe "+ this.getClass().getName(), e);
		}

		return new DadosParaDownload(bytes, this.getClass().getSimpleName() + ".pdf", MediaType.APPLICATION_PDF);
	}

	/**Método Diverso
	 * @return Set Integer (Impressão)
	 */
	private Set<Integer> getImpressao(){
		Set<Integer> impressao = new HashSet<>();

		if((boolean) get("impSolicitAbonoPecuniario")) impressao.add(0);
		if((boolean) get("impNotifSaidaFerias")) impressao.add(1);
		if((boolean) get("impSolicitAdiant13Salario")) impressao.add(2);
		if((boolean) get("impReciboFerias")) impressao.add(3);
		if((boolean) get("impReciboAbono")) impressao.add(4);
		if((boolean) get("impReciboAdiant13Salario")) impressao.add(5);

		if(impressao.size() == 0) {
			impressao.add(0);
			impressao.add(1);
			impressao.add(2);
			impressao.add(3);
			impressao.add(4);
			impressao.add(5);
		}
		return impressao;
	}

	/**Método Diverso
	 * @return 	Aag0201 (uf, municipio)
	 */
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addJoin(Joins.fetch("aag0201uf"))
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.setMaxResults(1).get();
	}

	/**Método Diverso
	 * @return 	Abh21 (abh21)
	 */
	private Abh21 findByUniqueKey(String abh21codigo) {
		return getSession().createCriteria(Abh21.class)
				.addWhere(Criterions.eq("abh21codigo", abh21codigo))
				.setMaxResults(1).get();
	}

	/**Método Único Adiantamento
	 * @return 	Abh21 (abh21)
	 */
	private Abh21 findByUniqueKeyAdiant(String abh21codigo) {
		return getSession().createCriteria(Abh21.class)
				.addWhere(Criterions.eq("abh21codigo", abh21codigo))
				.addWhere(getSamWhere().getCritPadrao(Abh21.class))
				.setMaxResults(1).get();
	}



	/**Método Diverso
	 * @return 	Parametros (Aba01)
	 */
	private String getParametros(Parametro param) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", "FB"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.setMaxResults(1).get();

		String conteudo = null;
		if(aba01 != null) {
			conteudo = aba01.getAba01conteudo();
		}
		return conteudo;
	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Trabalhador e Departamento)
	 */
	public List<TableMap> getFbc0101sByTrabalhadorAndCCAndBetweenData(List<Long> idsTrabalhador, List<Long> idsDepartamento,LocalDate[] dataPgto) {
		String whereTrabalhadores = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? " AND abh80.abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamento != null && !idsDepartamento.isEmpty() ? " AND abb11.abb11id IN (:idsDeptos) " : "";
		String whereData = dataPgto != null ? getWhereDataInterval("WHERE", dataPgto, "fbc0101dtPagto") : "";

		String sql = "SELECT * FROM Fbc0101 "+
				"LEFT JOIN Fbc01 ON fbc01id = fbc0101pa "+
				"INNER JOIN Abh80 ON abh80id = fbc01trab " +
				"INNER JOIN Abb11 ON abb11id = abh80depto " +
				"INNER JOIN Abh05 ON abh05id = abh80cargo " +
				"LEFT JOIN Aap03 ON aap03id = abh05cbo " +
				whereData + whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbc01.class) +
				" ORDER BY fbc0101id, abh80nome, fbc0101dtPagto";

		Query query = getSession().createQuery(sql);
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDeptos", idsDepartamento);

		return query.getListTableMap();
	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Trabalhador e Departamento)
	 */
	public List<TableMap> getFbc01011sByTrabalhadorAndCCAndBetweenData(List<Long> idsTrabalhador, List<Long> idsDepartamento,LocalDate[] dataPgto) {
		String whereTrabalhadores = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamento != null && !idsDepartamento.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereData = dataPgto != null ? getWhereDataInterval("WHERE", dataPgto, "fbc0101dtPagto") : "";

		String sql = "SELECT * FROM Fbc01011 " +
				"INNER JOIN Fbc0101 ON fbc0101id = fbc01011ferias " +
				"INNER JOIN Fbc01 ON fbc01id = fbc0101pa "+
				"INNER JOIN Abh80 ON abh80id = fbc01trab " +
				"INNER JOIN Abb11 ON abb11id = abh80depto " +
				"INNER JOIN Abh05 ON abh05id = abh80cargo " +
				"INNER JOIN Abh21 ON abh21id = fbc01011eve " +
				whereData + " AND fbc01011valor > 0 " +
				whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbc01.class) +
				" ORDER BY fbc01011ferias, Abh21codigo";

		Query query = getSession().createQuery(sql);
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDeptos", idsDepartamento);

		return query.getListTableMap();
	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Eventos e Avisos de Recibos de ferias Abono)
	 */
	public List<TableMap> getDadosFba01011sEventosByReciboFeriasAbono(List<Long> idsTrabalhador, List<Long> idsDepartamento,LocalDate[] dataPgto, int tipoValor, String complRend, String complDesc){
		String whereTrabalhadores = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamento != null && !idsDepartamento.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereData = dataPgto != null ? getWhereDataInterval("WHERE", dataPgto, "fbc0101dtPagto") : "";
		String whereTipo = " AND fba01011tipo = "+tipoValor+"";
		String whereCodigo = " AND abh21codigo NOT IN ('"+complRend+"', '"+complDesc+"')";

		String sql = "SELECT abh21codigo, abh21nome, abh21tipo, fba0101fer, fba01011eve, SUM(fba01011valor) as fba01011valor " +
				"FROM Fba01011 " +
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				"INNER JOIN Abh21 ON abh21id = fba01011eve " +
				"LEFT JOIN Fbc0101 ON fbc0101id = fba0101fer "+
				"INNER JOIN Fbc01 ON fbc01id = fbc0101pa "+
				"INNER JOIN Abh80 ON abh80id = fbc01trab " +
				"INNER JOIN abb11 ON abb11id = abh80depto " +
				whereData + " AND abh21tipo IN (0, 1, 5) "+ whereTipo + whereCodigo +
				whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbc01.class) +
				" GROUP BY abh21codigo, abh21nome, abh21tipo, fba0101fer, fba01011eve " +
				"HAVING SUM(fba01011valor) > 0 " +
				"ORDER BY fba0101fer, abh21tipo, abh21codigo";

		Query query = getSession().createQuery(sql);

		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDeptos", idsDepartamento);

		return query.getListTableMap();

	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Eventos e Avisos de Recibos Adiant 13 Salario)
	 */
	public List<TableMap> getDadosFba01011sEventosByReciboAdiantamento13Sal(List<Long> idsTrabalhador, List<Long>  idsDepartamento, LocalDate[] dataPgto, int tipoValor, Long idEVE) {
		String whereTrabalhadores = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? " AND abh80.abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamento != null && !idsDepartamento.isEmpty() ? " AND abb11.abb11id IN (:idsDeptos) " : "";
		String whereData = dataPgto != null ? getWhereDataInterval("WHERE", dataPgto, "fbc0101dtPagto") : "";

		String sql = "SELECT abh21codigo, abh21nome, fba0101fer, fbc01011ferias, fba01011eve, fba01011valor " +
				"FROM Fba01011 " +
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				"INNER JOIN Abh21 ON abh21id = fba01011eve " +
				"LEFT JOIN Fbc0101 ON fbc0101id = fba0101fer " +
				"INNER JOIN Fbc01011 ON fbc01011ferias = fbc0101id "+
				"INNER JOIN Abh80 ON abh80id = fba0101trab " +
				"INNER JOIN Abb11 ON abb11id = abh80depto " +
				whereData + " AND abh21tipo IN (0, 1, 5) AND fba01011tipo = "+tipoValor+" AND fba01011eve = "+idEVE+" " +
				whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbc0101.class) +
				" GROUP BY abh21codigo, abh21nome, fba0101fer, fba01011eve, fbc01011ferias, fba01011valor";

		Query query = getSession().createQuery(sql);

		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDeptos", idsDepartamento);

		return query.getListTableMap();
	}

	/**Método Diverso
	 * (Adiciona dias ao período de gozo)
	 */
	private void adicionarDiasAoPeriodoGozo(List<TableMap> dados, int dias) {
		for(TableMap map : dados) {
			LocalDate calDias = map.get("fbc0101pgf");
			calDias = calDias.plusDays(dias);
			map.put("fbc0101pgf", calDias);
		}
	}

	/**Método Diverso
	 * @return 	JasperPrint (Eventos de Avisos e Recibos de ferias)
	 */
	private JasperPrint getDadosRelatorioAvisosRecibosFerias(List<Long> idsTrabalhador, List<Long> idsDepartamento, LocalDate[] dataPgto, String complFerRend, String complFerDesc, String complAbonoRend, String complAbonoDesc, Set<Integer> impressao,Long abh21Ferias,Long abh21Abono,Long abh21Adiant13, Integer diasAdicPerGozo){

		List<TableMap> dados = getFbc0101sByTrabalhadorAndCCAndBetweenData(idsTrabalhador, idsDepartamento, dataPgto);
		JasperPrint print = null;
		JasperReport report = carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R1");

		Boolean geraSolicAbono = false;
		Boolean geraSolicFerias = false;
		Boolean geraSolic13 = false;
		Boolean geraReciboFerias = false;
		Boolean geraReciboAbono = false;
		Boolean geraRecibo13 = false;

		for(Integer imp : impressao) {
			if(imp.intValue() == 0) {
				geraSolicAbono = true;
			}else if(imp.intValue() == 1) {
				geraSolicFerias = true;
			}else if(imp.intValue() == 2) {
				geraSolic13 = true;
			}else if(imp.intValue() == 3) {
				geraReciboFerias = true;
			}else if(imp.intValue() == 4) {
				geraReciboAbono = true;
			}else if(imp.intValue() == 5){
				geraRecibo13 = true;
			}
		}

		//Adiciona dias ao período de gozo final, se diasAdicionaisPerGozo for maior que 0.
		if(diasAdicPerGozo > 0) {
			adicionarDiasAoPeriodoGozo(dados, diasAdicPerGozo);
		}

		//Relatório de solicitação de abono pecuniário.
		if(geraSolicAbono) {
			TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
			print = processarRelatorio(report, dsPrincipal);
		}

		//Relatório de solicitação de férias.
		if(geraSolicFerias) {
			report = carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R2");

			TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de solicitação de 13º salário.
		if(geraSolic13) {
			report = carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R3");

			TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de recibo de férias.
		if(geraReciboFerias) {
			report = carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R4");
			List<TableMap> dadosFerias = getFbc0101sByTrabalhadorAndCCAndBetweenData(idsTrabalhador, idsDepartamento, dataPgto);

			//Adiciona dias ao período de gozo final, se diasAdicionaisPerGozo for maior que 0.
			if(diasAdicPerGozo > 0) {
				adicionarDiasAoPeriodoGozo(dadosFerias, diasAdicPerGozo);
			}

			TableMapDataSource dsPrincipal = new TableMapDataSource(dadosFerias);

			//Sub-relatório	maior remuneração.
			List<TableMap> dsSubFbc01011 = getFbc01011sByTrabalhadorAndCCAndBetweenData(idsTrabalhador, idsDepartamento, dataPgto);
			dsPrincipal.addSubDataSource("DsSub1R4", dsSubFbc01011, "fbc0101id", "fbc01011ferias");

			//Sub-relatório eventos férias.
			List<TableMap> mapFerLiquida = getDadosFba01011sEventosByReciboFeriasAbono(idsTrabalhador, idsDepartamento, dataPgto, 0, complFerRend, complFerDesc);
			dsPrincipal.addSubDataSource("DsSub2R4", mapFerLiquida, "fbc0101id", "fba0101fer");

			//Transformando valores decimais em extenso segundo o id das férias liquidas dos parâmetros gerais.
			Map<Long, Integer> mapIndexFerias = new HashMap<Long, Integer>();

			for(int i = 0; i < dadosFerias.size(); i++) {
				mapIndexFerias.put(dadosFerias.get(i).getLong("fbc0101id"), i);

				LocalDate cal = dadosFerias.get(i).getDate("fbc0101pgf");
				LocalDate dtAtual = cal.plusDays(1);
				dadosFerias.get(i).put("dtRetTrab", dtAtual);
			}
			StringUtils.concat("", "");

			for(int i = 0; i < mapFerLiquida.size(); i++) {
				Extenso extenso = new Extenso();
				extenso.setNumber(mapFerLiquida.get(i).get("fba01011valor"));

				Long fba0101fer = mapFerLiquida.get(i).get("fba0101fer");
				int indexFerias = mapIndexFerias.get(fba0101fer);
				dadosFerias.get(indexFerias).put("extenso", extenso.toString());
			}
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}

		//Relatório de recibo de abono.
		if(geraReciboAbono) {
			report = carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R5");

			List<TableMap> dadosAbono = getFbc0101sByTrabalhadorAndCCAndBetweenData(idsTrabalhador, idsDepartamento, dataPgto);

			//Adiciona dias ao período de gozo final, se diasAdicionaisPerGozo for maior que 0.
			if(diasAdicPerGozo > 0) {
				adicionarDiasAoPeriodoGozo(dadosAbono, diasAdicPerGozo);
			}

			TableMapDataSource dsPrincipal = new TableMapDataSource(dadosAbono);

			//Sub-relatório	maior remuneração.
			List<TableMap> dsSubFba101 = getFbc01011sByTrabalhadorAndCCAndBetweenData(idsTrabalhador, idsDepartamento, dataPgto);
			dsPrincipal.addSubDataSource("DsSub1R5", dsSubFba101, "fbc0101id", "fbc01011ferias");

			//Sub-relatório eventos abono
			List<TableMap> mapAbono = getDadosFba01011sEventosByReciboFeriasAbono(idsTrabalhador, idsDepartamento, dataPgto, 1, complFerRend, complFerDesc);
			dsPrincipal.addSubDataSource("DsSub2R5", mapAbono, "fbc0101id", "fba0101fer");

			//Transformando valores decimais em extenso segundo o id do abono liquido dos parâmetros gerais.
			Map<Long, Integer> mapIndexAbonos = new HashMap<Long, Integer>();

			for(int i = 0; i < dadosAbono.size(); i++) {
				mapIndexAbonos.put(dadosAbono.get(i).getLong("fbc0101id"), i);
			}

			for(int i = 0; i < mapAbono.size(); i++) {
				Extenso extenso = new Extenso();
				extenso.setNumber(mapAbono.get(i).get("fba01011valor"));

				Long fba0101fer = mapAbono.get(i).get("fba0101fer");
				int indexAbono = mapIndexAbonos.get(fba0101fer);
				dadosAbono.get(indexAbono).put("extenso", extenso.toString());
			}

			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}


		//Relatório de recibo de adiantamento de 13º salário.
		if(geraRecibo13) {
			report = carregarArquivoRelatorio("SFP_EmissaoDeAvisosERecibosDeFerias_R6");

			List<TableMap> dadosAdiant13 = getFbc0101sByTrabalhadorAndCCAndBetweenData(idsTrabalhador, idsDepartamento, dataPgto);

			TableMapDataSource dsPrincipal = new TableMapDataSource(dadosAdiant13);

			//Sub-relatório evento adiantamento de 13º salário.
			List<TableMap> querySubFb011 = getDadosFba01011sEventosByReciboAdiantamento13Sal(idsTrabalhador, idsDepartamento, dataPgto, 2, abh21Adiant13);
			List<TableMap> mapAdiant13 = new ArrayList<>();
			mapAdiant13.addAll(querySubFb011);
			dsPrincipal.addSubDataSource("DsSub1R6", mapAdiant13, "fbc0101id", "fba0101fer");

			//Transformando valores decimais em extenso segundo o id do adiantamento liquido dos parâmetros gerais.
			Map<Long, Integer> mapIndexAdiant13 = new HashMap<Long, Integer>();

			for(int i = 0; i < dadosAdiant13.size(); i++) {
				mapIndexAdiant13.put(dadosAdiant13.get(i).getLong("fbc0101id"), i);
			}

			for(int i = 0; i < mapAdiant13.size(); i++) {
				Extenso extenso = new Extenso();
				extenso.setNumber(mapAdiant13.get(i).get("fba01011valor"));

				Long fba0101fer = mapAdiant13.get(i).get("fba0101fer");
				int indexAdiant13 = mapIndexAdiant13.get(fba0101fer);
				dadosAdiant13.get(indexAdiant13).put("extenso", extenso.toString());
			}

			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		return print;
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIEVtaXNzw6NvIGRlIEF2aXNvcyBlIFJlY2lib3MgZGUgRsOpcmlhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==