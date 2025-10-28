package multitec.relatorios.sfp;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.aa.Aap18;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abh04;
import sam.model.entities.ab.Abh0401;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01;
import sam.server.cas.service.ParametroService;
import sam.server.cgs.service.CentralService;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.SFPUtils;
import sam.server.sfp.service.SFPService;

/**Classe para relatório SFP - Relação de Salários
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeSalarios extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Salários";
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
		filtrosDefault.put("salario", "0");
		filtrosDefault.put("totalizacao", "0");
		filtrosDefault.put("ordenacao", "0");
		filtrosDefault.put("indiceSal", BigDecimal.ZERO);
		filtrosDefault.put("adicionalTempoServ", true);
		filtrosDefault.put("mesAno", DateUtils.formatDate(MDate.date(), "MM/yyyy"));
		
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
		Integer situacao = getInteger("situacao");
		boolean ordenarPorCodigo = getInteger("ordenacao") == 0 ? true : false;
		boolean isTotalizacaoGeral = getInteger("totalizacao") == 0 ? true : false;
		boolean isSalarioAtual = getInteger("salario") == 0 ? true : false;
		boolean adicionalTempoServ = get("adicionalTempoServ");
		BigDecimal indiceSobreSal = DecimalUtils.create(getBigDecimal("indiceSal").toString()).get();
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		CentralService centralService = instanciarService(CentralService.class)
		ParametroService parametroService = instanciarService(ParametroService.class)
		SFPService sfpService = new SFPService(session, samWhere, centralService, parametroService, variaveis, null);
		SFPVarDto sfpVarDto = new SFPVarDto();
		SFPUtils sfpUtils = new SFPUtils(session, samWhere, sfpService, variaveis, sfpVarDto);
		
		String strMesAno = get("mesAno");
		int ano = Integer.parseInt(strMesAno.substring(strMesAno.indexOf("/")+1, strMesAno.length()));
		int mes = Integer.parseInt(strMesAno.substring(0, strMesAno.indexOf("/")));
		LocalDate mesAno = LocalDate.of(ano, Month.of(mes), 1);
		
		String salMin = getParametros(Parametros.FB_SALARIOMINIMO);
		if(StringUtils.isNullOrEmpty(salMin)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_SALARIOMINIMO.");
		BigDecimal salarioMinimo = DecimalUtils.create(salMin).get();
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());

		List<TableMap> dados = new ArrayList<>();	
		comporRelatorio(sfpUtils, dados, isSalarioAtual, idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, situacao, isTotalizacaoGeral, ordenarPorCodigo, mesAno, indiceSobreSal, adicionalTempoServ, salarioMinimo);
		
		return gerarPDF(isTotalizacaoGeral ? "SFP_RelacaoDeSalarios_R1" : "SFP_RelacaoDeSalarios_R2", dados);
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
	 * @return 	List TableMap (Query da busca da Relação de Salários - Trabalhadores)
	 */
	public List<TableMap> findDadosAbh80sByRelacaoSalarios(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, int sitTrab, boolean isTotalizacaoGeral, boolean ordenarPorCodigo) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereSit = sitTrab == 0 ? "AND abh80dtResTrans IS NULL " : sitTrab == 1 ? "AND abh80dtResTrans IS NOT NULL " : "";
		String ordem = null;
		if(isTotalizacaoGeral) {
			ordem = ordenarPorCodigo ? " ORDER BY abh80codigo" : " ORDER BY abh80nome";
		}else {
			ordem = ordenarPorCodigo ? " ORDER BY abb11codigo, abh80codigo" : " ORDER BY abb11codigo, abh80nome";
		}
		
		String sql = "SELECT abh80id, abh80dtAdmis AS admissao, abh80nascData AS nascimento, abh80hs as hs, abh80salario as salvlr, abh80salTipo as saltipo, " +
					 "abh80unidPagto, " + 
					 Fields.concat("abh80codigo", "'-'", "abh80nome") + " AS trabalhador, " +
				     "abh80tmpData AS dttmpserv, abh04codigo, abb11codigo AS codDepto, abb11nome AS nomeDepto, abh05nome AS cargo " +
				     "FROM Abh80 " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "LEFT JOIN Abh04 ON abh04id = abh80tmpServ " +
					 "WHERE abh80tipo IN (:tiposAbh80) " + whereSit +
					 whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) +
					 ordem;
			
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Query da busca da Relação de Salários - Arquivo de Valores)
	 */
	public List<TableMap> findDadosFba0101sByRelacaoSalarios(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, int sitTrab, int mes, int ano, boolean isTotalizacaoGeral, boolean ordenarPorCodigo) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereSit = sitTrab == 0 ? " AND abh80dtResTrans IS NULL " : sitTrab == 1 ? " AND abh80dtResTrans IS NOT NULL " : "";
		String ordem = null;
		if(isTotalizacaoGeral) {
			ordem = ordenarPorCodigo ? " ORDER BY abh80codigo" : " ORDER BY abh80nome";
		}else {
			ordem = ordenarPorCodigo ? " ORDER BY abb11codigo, abh80codigo" : " ORDER BY abb11codigo, abh80nome";
		}
		
		String sql = "SELECT abh80id, abh80dtAdmis AS admissao, abh80nascData AS nascimento, abh80hs as hs, MAX(abh80salario) as salvlr, abh80salTipo as saltipo, " +
					 "abh80unidPagto, " + 
					 Fields.concat("abh80codigo", "'-'", "abh80nome") + " AS trabalhador, " +
				     "abh80tmpData as dttmpserv, abh04codigo, abb11codigo AS codDepto, abb11nome AS nomeDepto, abh05nome AS cargo " +
				     "FROM Fba0101 " +
				     "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
				     "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "LEFT JOIN Abh04 ON abh04id = abh80tmpServ " +
					 "WHERE abh80tipo IN (:tiposAbh80) AND " + 
					 Fields.month("fba0101dtCalc") + " = :mes AND " +  Fields.year("fba0101dtCalc") + " = :ano " + whereSit +
					 whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Fba01.class) +
					 " GROUP BY abh80id, abh80codigo, abh80nome, abh80dtAdmis, abh80nascData, abh80hs, abh80salTipo, abh80tmpData, abh04codigo, abb11codigo, abb11nome, abh05nome, abh80unidPagto " +
					 ordem;
			
		Query query = getSession().createQuery(sql);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * Compõe os dados do relatório
	 */			
	public void comporRelatorio(SFPUtils sfpUtils, List<TableMap> dados, boolean isSalarioAtual, List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, int sitTrab, boolean isTotalizacaoGeral, boolean ordenarPorCodigo, LocalDate mesAno, BigDecimal indiceSobreSal, boolean adicionalTempoServ, BigDecimal salarioMinimo) {
		List<TableMap> salarios = null;
		if(isSalarioAtual) {
			salarios = findDadosAbh80sByRelacaoSalarios(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, sitTrab, isTotalizacaoGeral, ordenarPorCodigo);
		}else {
			salarios = findDadosFba0101sByRelacaoSalarios(idsTrabalhadores, idsDeptos, idsCargos, tiposAbh80, sitTrab, mesAno.getMonthValue(), mesAno.getYear(), isTotalizacaoGeral, ordenarPorCodigo);
		}
		
		if(salarios != null && !salarios.isEmpty()) {
			for(TableMap sal : salarios) {
				int hs = sal.getInteger("hs");
				sal.put("hs", hs);
				
				Aap18 aap18 = getSession().createCriteria(Aap18.class)
						.addFields("aap18id, aap18codigo")
						.addWhere(Criterions.eq("aap18id", sal.getLong("abh80unidPagto")))
						.get();
				
				BigDecimal salario = sfpUtils.buscarValorDoSalarioMes(sal.getLong("abh80id"));

				BigDecimal adicIndice = new BigDecimal(0);
				if(indiceSobreSal.compareTo(adicIndice) > 0) {
					adicIndice = salario.multiply(indiceSobreSal);
				}
				
				BigDecimal adicTempServ = new BigDecimal(0);
				if(adicionalTempoServ && sal.getString("abh04codigo") != null && sal.getDate("dttmpserv") != null) {
					LocalDate dtBaseSalario = isSalarioAtual ? MDate.date() : mesAno;

					LocalDate dtBaseTmpServ = sal.getDate("dttmpserv");

					int dias = (int) DateUtils.dateDiff(dtBaseTmpServ, dtBaseSalario, ChronoUnit.DAYS);
					if(dias > 0) {
						Abh04 abh04 = getSession().createCriteria(Abh04.class)
								.addJoin(Joins.fetch("abh0401ts"))
								.addWhere(Criterions.eq("abh04codigo", sal.getString("abh04codigo")))
								.get();
						

						List<Abh0401> abh0401s = abh04.getAbh0401s().stream().sorted(new Comparator<Abh0401>() {
							@Override
							public int compare(Abh0401 o1, Abh0401 o2) {
								return o1.getAbh0401dias().compareTo(o2.getAbh0401dias());
							}
						 }).collect(Collectors.toList());
						
						for(Abh0401 abh0401 : abh0401s) {
							if(dias <= abh0401.getAbh0401dias()) {
								BigDecimal vlrAdicional = abh0401.getAbh0401adicional();

								if(abh04.getAbh04tipo() == 1) { //Verifica se o adicional é valor ou índice.
									adicTempServ = vlrAdicional;
									break;
								}else {
									if(abh04.getAbh04bc() == 1) { //Verifica se a base de cálculo do índice é o salário mínimo ou o salário base.
										if(salarioMinimo != null) adicTempServ = salarioMinimo.multiply(vlrAdicional);
										break;
									}else{
										adicTempServ = salario.add(adicIndice);
										adicTempServ = adicTempServ.multiply(vlrAdicional);
										break;
									}
								}
							}
						}
					}						
				}
				BigDecimal total = salario.add(adicIndice).add(adicTempServ);
				
				sal.put("salario", salario);
				sal.put("adicIndice", adicIndice);
				sal.put("adicTempServ", adicTempServ);
				sal.put("total", total);
				
				dados.add(sal);
			}
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBTYWzDoXJpb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=