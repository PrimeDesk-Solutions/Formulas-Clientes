package multitec.relatorios.sfp;

import java.time.LocalDate;

import org.springframework.http.MediaType;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
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
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01;
import sam.model.entities.fb.Fbb01;
import sam.model.entities.fb.Fbb20;
import sam.model.entities.fb.Fbc01;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Ficha de Registro de Empregados
 * @author Lucas Eliel
 * @since 30/04/2019
 * @version 1.0
 */

public class SFP_FichaDeRegistroDeEmpregados extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Ficha de Registro de Empregados";
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
		LocalDate[] referencia = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("referencia", referencia);
		filtrosDefault.put("considDemitidos", false);
		filtrosDefault.put("regTrabalhador", true);
		filtrosDefault.put("cargosSalarios", true);
		filtrosDefault.put("afastamentos", true);
		filtrosDefault.put("anotacoesFerias", true);
		filtrosDefault.put("dependentes", true);
		filtrosDefault.put("anotacoesGerais", true);
		filtrosDefault.put("contribSindical", true);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return String, byte[] (Dados para Download)
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		LocalDate[] admissao = getIntervaloDatas("admissao");
		LocalDate[] referencia = getIntervaloDatas("referencia");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		boolean considDemitidos = get("considDemitidos");
		boolean isRegTrabalhador = get("regTrabalhador");
		boolean isCargosSalarios = get("cargosSalarios");
		boolean isAfastamentos = get("afastamentos");
		boolean isAnotacoesFerias = get("anotacoesFerias");
		boolean isDependentes = get("dependentes");
		boolean isAnotacoesGerais = get("anotacoesGerais");
		boolean isContribSindical = get("contribSindical");
		
		String eveSindical = getParametros(Parametros.FB_EVESINDICAL);
		if(eveSindical == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVESINDICAL.");
		
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
		
		params.put("TITULO_RELATORIO", "Ficha de Registro de Empregados");
		params.put("EMP_RS", aac10.getAac10rs());
		params.put("EMP_NA", aac10.getAac10na());
		params.put("EMP_NI", aac10.getAac10ni());
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_BAIRRO", aac10.getAac10bairro());
		params.put("EMP_CIDADE", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
		params.put("EMP_UF", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
		params.put("EMP_CNAE", aac10.getAac10cnae());
		params.put("StreamSub1R2", carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R2_S1"));
		params.put("StreamSub1R3", carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R3_S1"));
		params.put("StreamSub1R4", carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R4_S1"));
		params.put("StreamSub1R5", carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R5_S1"));
		params.put("StreamSub1R6", carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R6_S1"));
		params.put("StreamSub1R7", carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R7_S1"));
		
		JasperPrint print = carregarDadosRelatorio(idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, admissao, referencia, considDemitidos, isRegTrabalhador, isCargosSalarios, isAfastamentos, isAnotacoesFerias, isDependentes, isAnotacoesGerais, isContribSindical, eveSindical);
		
		byte[] bytes;
		try {
			bytes = JasperExportManager.exportReportToPdf(print);
		} catch (JRException e) {
			throw new RuntimeException("Erro ao gerar o relatório da classe "+ this.getClass().getName(), e);
		}
		return new DadosParaDownload(bytes, this.getClass().getSimpleName() + ".pdf", MediaType.APPLICATION_PDF);
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
	 * @return 	List - TableMap (Query de Ficha de Registro Empregados do Trabalhador)
	 */
	private List<TableMap> getDadosAbh80sByFichaRegistroEmpregados(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtAdmis, boolean considDemitidos) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDemitidos = considDemitidos ? "" : " AND abh80dtResTrans IS NULL ";
		String whereData = dtAdmis != null ? getWhereDataInterval("AND", dtAdmis, "abh80dtAdmis") : "";
		
		String sql = "SELECT abh80id, abh80codigo, abh80nome, abh80endereco, abh80bairro, abh80numero, aag0201nome, abh80cep, aag02uf, aap05descr, abh80nascData, abh80sexo, aap08descr, " +
				     "aap06descr, abh80pai, abh80mae, abh80salario, abh80salTipo, abh05nome, aap03codigo, abh80dtAdmis, abh80dtResTrans, abb11nome, abh80cpf, abh80rgNum, abh80rgDtExped, abh80rgOe, " +
				     "abh80teSecao, abh80teZona, abh80teNum, abh80ctpsNum, abh80ctpsSerie, abh80ctpsEe, abh80ctpsDtEmis, abh80pis, abh80dtPis, abh80crOe, abh80crSigla, abh80crNum, " +
				     "abh80crRegiao, abh80crHabProf, abh80obsTrab, abh80foto, aap18codigo, abh80tipoNac " +
				     "FROM Abh80 " +
				     "LEFT JOIN Aap08 on aap08id = abh80estCivil " +
				     "LEFT JOIN Aag0201 ON aag0201id = abh80municipio " +
				     "INNER JOIN Aag02 ON aag02id = aag0201uf " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap03 ON aap03id = abh05cbo " +
				     "LEFT JOIN Aap05 ON aap05id = abh80nacionalidade " +
				     "LEFT JOIN Aap06 ON aap06id = abh80gi " +
				     "INNER JOIN Aap18 ON aap18id = abh80unidPagto " +
				     "WHERE abh80tipo IN (:tiposAbh80) " + whereData + whereDemitidos +
				     whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     "ORDER BY abh80codigo";
			
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);

		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query da Rescisão do Trabalhador)
	 */
	private Fbd10 getDadosDaRescisaoByTrabalhador(Long abh80id) {
		return getSession().createCriteria(Fbd10.class)
				.addJoin(Joins.fetch("fbd10causa"))
				.addWhere(Criterions.eq("fbd10trab", abh80id))
				.get();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de Ficha de Registro Empregados do Reajuste Salarial)
	 */
	private List<TableMap> getDadosFbb20sByFichaRegistroEmpregados(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtReferencia, boolean considDemitidos) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDemitidos = considDemitidos ? "" : " AND abh80dtResTrans IS NULL ";
		String whereData = dtReferencia != null ? getWhereDataInterval("AND", dtReferencia, "fbb20data") : "";
		
		String sql = "SELECT fbb20trab, fbb20data, fbb20salAtu, fbb20motivo, Aap18codigo, fbb20hs, abh05codigo, abh05nome, abh80codigo, abh80nome " +
				     "FROM Fbb20 " +
				     "INNER JOIN Abh80 ON abh80id = fbb20trab " +
				     "INNER JOIN Abb11 ON abb11id = fbb20depto " +
				     "LEFT JOIN Abh05 ON abh05id = fbb20cargo " +
				     "LEFT JOIN Aap18 ON aap18id = abh80unidPagto " +
				     "WHERE abh80tipo IN (:tiposAbh80) " + whereData + whereDemitidos +
				     whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Fbb20.class) +
				     "ORDER BY abh80codigo, fbb20data";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);

		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de Ficha de Registro Empregados do Afastamento e Retorno)
	 */
	private List<TableMap> getDadosFbb01sByFichaRegistroEmpregados(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtReferencia, boolean considDemitidos) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDemitidos = considDemitidos ? "" : "AND abh80dtResTrans IS NULL ";
		String whereDataSaida = dtReferencia != null ? getWhereDataInterval("", dtReferencia, "fbb01dtSai") : "";
		String whereDataRetorno = dtReferencia != null ? getWhereDataInterval("", dtReferencia, "fbb01dtRet") : "";
		
		String sql = "SELECT fbb01trab, fbb01dtSai, fbb01dtRet, abh07codigo, abh07nome, abh80codigo, abh80nome " +
				     "FROM Fbb01 " +
			         "INNER JOIN Abh80 ON abh80id = fbb01trab " +
			         "INNER JOIN Abh07 ON abh07id = fbb01ma " +
			         "INNER JOIN Abb11 ON abb11id = abh80depto " +
			         "INNER JOIN Abh05 ON abh05id = abh80cargo " +
			         "WHERE ((" + whereDataSaida + ") OR (" + whereDataRetorno + ")) AND abh80tipo IN (:tiposAbh80) " + whereDemitidos +
			          whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Fbb01.class) +
			         "ORDER BY abh80codigo, fbb01dtSai";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de Ficha de Registro Empregados do Período Aquisitivo)
	 */
	private List<TableMap> getDadosFbc01sByFichaRegistroEmpregados(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtReferencia, boolean considDemitidos){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDemitidos = considDemitidos ? "" : "AND abh80dtResTrans IS NULL ";
		String whereData = dtReferencia != null ? getWhereDataInterval("AND", dtReferencia, "fbc0101dtPagto") : "";
		
		String sql = "SELECT fbc01trab, fbc01pai, fbc01paf, fbc0101pgi, fbc0101pgf, fbc0101pbi, fbc0101pbf, fbc0101obs, abh80codigo, abh80nome " +
				     "FROM Fbc01 " +
				     "INNER JOIN Fbc0101 ON fbc0101pa = fbc01id " +
			         "INNER JOIN Abh80 ON abh80id = fbc01trab " +
			         "INNER JOIN Abb11 ON abb11id = abh80depto " +
			         "INNER JOIN Abh05 ON abh05id = abh80cargo " +
			         "WHERE abh80tipo IN (:tiposAbh80) " + whereData + whereDemitidos +
			         whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Fbc01.class) +
			         "ORDER BY abh80codigo, fbc0101dtPagto";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de Ficha de Registro Empregados dos Dependentes)
	 */
	private List<TableMap> getDadosAbh8002sByFichaRegistroEmpregados(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtReferencia, boolean considDemitidos) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDemitidos = considDemitidos ? "" : "AND abh80dtResTrans IS NULL ";
		String whereData = dtReferencia != null ? getWhereDataInterval("AND", dtReferencia, "abh80dtAdmis") : "";
		
		String sql = "SELECT abh8002trab, abh8002nome, abh8002dtNasc, aap09descr " +
				     "FROM Abh8002 " +
				     "INNER JOIN Abh80 ON abh80id = abh8002trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap09 ON aap09id = abh8002parente " +
	                 "WHERE abh80tipo IN (:tiposAbh80) " + whereData + whereDemitidos +
				     whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     "ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos",	 idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
			
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query de Ficha de Registro Empregados das Anotações)
	 */
	private List<TableMap> getDadosAbh8004sByFichaRegistroEmpregados(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtReferencia, boolean considDemitidos) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDemitidos = considDemitidos ? "" : "AND abh80dtResTrans IS NULL ";
		String whereData = dtReferencia != null ? getWhereDataInterval("AND", dtReferencia, "abh80dtAdmis") : "";
		
		String sql = "SELECT abh8004trab, abh8004data, abh8004ficha, abh8004nota, aap12codigo, aap12descr " +
				     "FROM Abh8004 " +
				     "INNER JOIN Abh80 ON abh80id = abh8004trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "INNER JOIN Aap12 ON aap12id = abh8004anotacao " +
	                 "WHERE abh8004ficha = 1 AND abh80tipo IN (:tiposAbh80) " + whereData + whereDemitidos +
				     whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) + 
				     "ORDER BY abh80codigo";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List - TableMap (Query da Contribuição Sindical por Ficha de Registro de Empregados - Eventos)
	 */
	private List<TableMap> getDadosFba01011sContribuicaoSindicalByFichaRegistroEmpregados(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtReferencia, String codAbh21, boolean considDemitidos) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDemitidos = considDemitidos ? "" : "AND abh80dtResTrans IS NULL ";
		String whereData = dtReferencia != null ? getWhereDataInterval("AND", dtReferencia, "fba0101dtCalc") : "";
		
		String sql = "SELECT abh21codigo, abh21nome, fba01011valor, fba0101dtCalc, fba0101trab " +
			         "FROM Fba01011 " +
				     "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
			         "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
				     "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "WHERE abh80tipo IN (:tiposAbh80) AND abh21codigo = :codAbh21 " + whereData + whereDemitidos +
				     whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND",Fba01.class) + 
				     " ORDER BY abh80codigo, fba0101dtCalc";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameter("codAbh21", codAbh21);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	JasperPrint (Ficha de Registro de Empregados)
	 */
	private JasperPrint carregarDadosRelatorio(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, LocalDate[] dtAdmis, LocalDate[] dtReferencia, boolean considDemitidos, boolean isRegTrabalhador, boolean isCargosSalarios, boolean isAfastamentos, boolean isAnotacoesFerias, boolean isDependentes, boolean isAnotacoesGerais, boolean isContribSindical, String eveSindical) {
		List<TableMap> listAbh80s = getDadosAbh80sByFichaRegistroEmpregados(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, dtAdmis, considDemitidos);
		if(listAbh80s != null && listAbh80s.size() > 0) {
			for(int i = 0; i < listAbh80s.size(); i++) {
				String endereco = listAbh80s.get(i).getString("abh80endereco") + ", " + listAbh80s.get(i).getString("abh80numero");
				listAbh80s.get(i).put("endereco", endereco);

				String sexo = null;
				if(listAbh80s.get(i).getInteger("abh80sexo") == 0) {
					sexo = "M";
				}else {
					sexo = "F";
				}
				listAbh80s.get(i).put("sexo", sexo);

				String tipoSal = null;
				if(listAbh80s.get(i).getInteger("aap18codigo") != null) {
					if(listAbh80s.get(i).getInteger("aap18codigo") == 1) {
						tipoSal = "Horas";
					}else if(listAbh80s.get(i).getInteger("aap18codigo") == 5) {
						tipoSal = "Mês";
					}
				}
				listAbh80s.get(i).put("tipoSal", tipoSal);
				
				String naturalidade = null;
				listAbh80s.get(i).put("naturalidade", naturalidade);
				
				//Se demitido - localizar dados da rescisão.
				Fbd10 fbd10 = getDadosDaRescisaoByTrabalhador(listAbh80s.get(i).getLong("abh80id"));
				if(fbd10 != null)listAbh80s.get(i).put("causaRescisao", fbd10.getFbd10causa().getAbh06causa());
			}
		}
		
		JasperPrint print = null;
		
		//Relatório de registro de empregado
		JasperReport report = carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R1");
		if(isRegTrabalhador) {
			List<TableMap> dsPrincipal = new ArrayList<>();
			dsPrincipal.addAll(listAbh80s);
			print = processarRelatorio(report, dsPrincipal);
		}
		
		
		//Relatório de alterações de cargos e salários
		if(isCargosSalarios) {
			report = carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R2");
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listAbh80s);
			
			List<TableMap> listFbb20s = getDadosFbb20sByFichaRegistroEmpregados(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, dtReferencia, considDemitidos);
			for(TableMap fbb20 : listFbb20s) {
				String tipoSal = null;
				if(fbb20.getInteger("Aap18codigo") == 1) {
					tipoSal = "Horista";
				}else {
					tipoSal = "Mensalista";
				}
				fbb20.put("tipoSal", tipoSal);
			}
			
			dsPrincipal.addSubDataSource("DsSub1R2", listFbb20s, "abh80id", "fbb20trab");
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal); 
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		
		
		//Relatório de afastamentos
		if(isAfastamentos) {
			report = carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R3");
			
			List<TableMap> listFbb01s = getDadosFbb01sByFichaRegistroEmpregados(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, dtReferencia, considDemitidos);
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listAbh80s);
			dsPrincipal.addSubDataSource("DsSub1R3", listFbb01s, "abh80id", "fbb01trab");
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		
		
		//Relatório de férias
		if(isAnotacoesFerias) {
			report = carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R4");

			List<TableMap> listFbc01s = getDadosFbc01sByFichaRegistroEmpregados(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, dtReferencia, considDemitidos);
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listAbh80s);
			dsPrincipal.addSubDataSource("DsSub1R4", listFbc01s, "abh80id", "fbc01trab");
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		
		//Relatório de dependentes
		if(isDependentes) {
			report = carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R5");
			
			List<TableMap> listAbh8002s = getDadosAbh8002sByFichaRegistroEmpregados(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, dtReferencia, considDemitidos);
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listAbh80s);
			dsPrincipal.addSubDataSource("DsSub1R5", listAbh8002s, "abh80id", "abh8002trab");
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		
		//Anotações Gerais
		if(isAnotacoesGerais) {
			report = carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R6");
			
			List<TableMap> listAbh8004s = getDadosAbh8004sByFichaRegistroEmpregados(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, dtReferencia, considDemitidos);
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listAbh80s);
			dsPrincipal.addSubDataSource("DsSub1R6", listAbh8004s, "abh80id", "abh8004trab");
			
			JasperPrint printTemp = processarRelatorio(report, dsPrincipal);
			if(print == null) {
				print = printTemp;
			}else {
				for(Object page : printTemp.getPages()) {
					print.addPage((JRPrintPage)page);
				}
			}
		}
		
		//Contribuições Sindicais
		if(isContribSindical && eveSindical != null) {
			report = carregarArquivoRelatorio("SFP_FichaDeRegistroDeEmpregados_R7");
			
			List<TableMap> listFba01011s = getDadosFba01011sContribuicaoSindicalByFichaRegistroEmpregados(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, dtReferencia, eveSindical, considDemitidos);
			
			TableMapDataSource dsPrincipal = new TableMapDataSource(listAbh80s);
			dsPrincipal.addSubDataSource("DsSub1R7", listFba01011s, "abh80id", "fba0101trab");
					
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
//meta-sis-eyJkZXNjciI6IlNGUCAtIEZpY2hhIGRlIFJlZ2lzdHJvIGRlIEVtcHJlZ2Fkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=