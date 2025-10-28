package multitec.relatorios.sca

import br.com.multiorm.criteria.criterion.Criterion;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.aa.Aap18
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abb11;
import sam.model.entities.ab.Abh05
import sam.model.entities.ab.Abh13
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fc.Fca10;
import sam.model.entities.fc.Fca1001;
import sam.model.entities.fc.Fca20;
import sam.model.entities.fc.Fca2001;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SCA - Cartão de Ponto
 * @author Lucas Eliel
 * @since 08/05/2019
 * @version 1.0
 */

public class SCA_CartaoDePonto extends RelatorioBase{

	private Integer nroCampoFolha;
	private Integer numEvento;
	private Integer order;

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Cartão de Ponto";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("sitTrabalhando", true);
		filtrosDefault.put("sitAfastado", true);
		filtrosDefault.put("sitFerias", true);
		filtrosDefault.put("ord", "0");
		filtrosDefault.put("marcacoes", "0");
		filtrosDefault.put("pa", false);

		return Utils.map("filtros", filtrosDefault);
	}

	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhador");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsMapHorario = getListLong("mapHorario");
		List<Long> idsEventos = getListLong("eventos"); // TODO

		LocalDate[] periodo = getIntervaloDatas("periodo");

		Set<Integer> situacoes = getSituacoes();

		int ordenacao = getInteger("ord");
		order = ordenacao
		boolean isREP = getInteger("marcacoes") == 0 ? false : true;
		boolean isPA = getInteger("marcacoes") == 0 ? true : false;
		boolean pa = get("pa");

		String campoFaltaDesc = getParametros(Parametros.FC_CODEVENTOFALTADESC);
		if(StringUtils.isNullOrEmpty(campoFaltaDesc)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FC_CODEVENTOFALTADESC.");

		String numCampoFaltaDesc = getParametros(Parametros.FC_NUMCAMPOFALTADESC);
		if(StringUtils.isNullOrEmpty(numCampoFaltaDesc)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FC_NUMCAMPOFALTADESC.");

		String numComplemDSR = getParametros(Parametros.FC_NUMCOMPLEMDSR)
		if(StringUtils.isNullOrEmpty(numComplemDSR)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FC_NUMCOMPLEMDSR.");

		Integer complemDSR = Integer.parseInt(numComplemDSR);

		if(existePontosInconsistentes(periodo, idsTrabalhadores, idsDeptos, situacoes)) throw new ValidacaoException("Existem pontos inconsistentes no filtro informado.");

		TableMap aac10 = getAac10();
		params.put("PERIODO", DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		params.put("EMP_RS", aac10.get("emp_rs"));
		params.put("EMP_NI", aac10.get("emp_ni"));
		params.put("EMP_ENDERECO", aac10.get("emp_endereco"));
		params.put("EMP_CEP", aac10.get("emp_cep"));
		params.put("EMP_BAIRRO", aac10.get("emp_bairro"));
		params.put("EMP_CIDADE", aac10.get("emp_cidade"));
		params.put("EMP_UF", aac10.get("emp_uf"));
		params.put("StreamSub1R1", carregarArquivoRelatorio("SCA_CartaoDePonto_S1"));
		params.put("StreamSub2R1", carregarArquivoRelatorio("SCA_CartaoDePonto_S2"));

		List<TableMap> dadosPrincipal = new ArrayList<TableMap>();
		List<TableMap> dadosSubS1 = new ArrayList<TableMap>();
		List<TableMap> dadosSubS2 = new ArrayList<TableMap>();

		List<Fca10> fca10s = findFca10sByEspelhoPontoEletronico(idsTrabalhadores, idsDeptos, idsMapHorario, periodo, situacoes, ordenacao);
		getDadosCartaoDePonto(dadosPrincipal, dadosSubS1, dadosSubS2, fca10s, ordenacao, isREP, isPA, pa, periodo, campoFaltaDesc, complemDSR, idsDeptos, idsMapHorario, numCampoFaltaDesc);
		
		if(ordenacao == 1 ) {
			ordenaDadosAlfabetico(dadosPrincipal)
			ordenaDadosAlfabetico(dadosSubS1)
			ordenaDadosAlfabetico(dadosSubS2)
		}
		
		if(ordenacao == 2) {
			ordenaDadosDepartamento(dadosPrincipal)
			ordenaDadosDepartamento(dadosSubS1)
			ordenaDadosDepartamento(dadosSubS2)
		}
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(dadosPrincipal);
		dsPrincipal.addSubDataSource("DsSub1R1", dadosSubS1, "key", "key");
		dsPrincipal.addSubDataSource("DsSub2R1", dadosSubS2, "key", "key");
		
		return gerarPDF("SCA_CartaoDePonto", dsPrincipal);
	}

	private void ordenaDadosAlfabetico(List<TableMap> dadosPrincipal) {
		dadosPrincipal.sort(new Comparator<TableMap>() {
					public int compare(TableMap tm1, TableMap tm2) {
						return  tm1.getString("key").compareTo(tm2.getString("key"));
					}
				});
		dadosPrincipal.sort(new Comparator<TableMap>() {
					public int compare(TableMap tm1, TableMap tm2) {
						return  tm1.getString("trabalhador").split("-")[1].compareTo(tm2.getString("trabalhador").split("-")[1]);
					}
				});
	}

	private void ordenaDadosDepartamento(List<TableMap> dadosPrincipal) {
		dadosPrincipal.sort(new Comparator<TableMap>() {
					public int compare(TableMap tm1, TableMap tm2) {
						return  tm1.getString("key").compareTo(tm2.getString("key"));
					}
				});
		/*dadosPrincipal.sort(new Comparator<TableMap>() {
					public int compare(TableMap tm1, TableMap tm2) {
						return  tm1.getString("key").split("-")[0].compareTo(tm2.getString("key").split("-")[0]);
					}
				});*/
	}
	/**Método Diverso
	 * @return Set Integer (Situação do Trabalhador)
	 */
	private Set<Integer> getSituacoes(){
		Set<Integer> situacoes = new HashSet<Integer>();

		if((Boolean) get("sitTrabalhando")) situacoes.add(0);
		if((Boolean) get("sitAfastado")) situacoes.add(1);
		if((Boolean) get("sitFerias")) situacoes.add(2);

		if(situacoes.size() == 0) {
			situacoes.add(0);
			situacoes.add(1);
			situacoes.add(2);
		}
		return situacoes;
	}

	/**Método Diverso
	 * @return 	Parametros (Aba01)
	 */
	private String getParametros(Parametro param) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", "FC"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.get();

		String conteudo = null;
		if(aba01 != null) {
			conteudo = aba01.getAba01conteudo();
		}
		return conteudo;
	}

	/**Método Diverso
	 * @return 	Boolean (Analisa a existência de pontos inconsistentes)
	 */
	private boolean existePontosInconsistentes(LocalDate[] periodo, List<Long> idsTrabalhadores, List<Long> idsDeptos, Set<Integer> situacoes) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereData = periodo != null ? getWhereDataInterval("AND", periodo, "fca10data") : "";

		String sql = "SELECT COUNT(*) FROM Fca10 " +
				"INNER JOIN Abh80 ON abh80id = fca10trab " +
				"INNER JOIN Abb11 ON abb11id = fca10depto " +
				"WHERE fca10consistente = :cons " + whereData + " AND fca10sit IN (:situacoes) " +
				whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fca10.class) ;

		Query query = getSession().createQuery(sql);
		query.setParameter("cons", 0);
		query.setParameter("situacoes", situacoes);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);

		query.setMaxResult(1);

		Integer result = (Integer)query.getUniqueResult(ColumnType.INTEGER);
		return result > 0;
	}

	/**Método Diverso
	 * @return 	TableMap (Retorna um TableMap com os campos da Aac10)
	 */
	private TableMap getAac10() {
		Aac10 aac10 = getVariaveis().getAac10();

		String CNPJ = aac10.getAac10ni();

		String endereco = null;
		if(aac10.getAac10endereco() != null){
			if(aac10.getAac10numero() != null){
				endereco = StringUtils.concat(aac10.getAac10endereco(), ", ", aac10.getAac10numero());
			}else{
				endereco = aac10.getAac10endereco();
			}

			if(aac10.getAac10complem() != null) {
				endereco += " - " + aac10.getAac10complem();
			}
		}

		String CEP = aac10.getAac10cep() ;
		String bairro = aac10.getAac10bairro();
		String cidade = aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null;
		String UF = aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null;
		String razaoSocial = aac10.getAac10rs();

		TableMap dadosEmp = new TableMap();
		dadosEmp.put("emp_rs", razaoSocial);
		dadosEmp.put("emp_ni", CNPJ);
		dadosEmp.put("emp_endereco", endereco);
		dadosEmp.put("emp_cep", CEP);
		dadosEmp.put("emp_bairro", bairro);
		dadosEmp.put("emp_cidade", cidade);
		dadosEmp.put("emp_uf", UF);

		return dadosEmp;
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
	 * @return 	List TableMap da Fca10 (Query da busca - Espelho do Ponto Eletrônico)
	 */
	private List<Fca10> findFca10sByEspelhoPontoEletronico(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsMapHorario, LocalDate[] periodo, Set<Integer> situacoes, int ordenacao){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80.abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11.abb11id IN (:idsDeptos) " : "";
		String whereMapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? " AND abh13.abh13id IN (:idsMapHorario) " : "";
		String whereData = periodo != null ? getWhereDataInterval("AND", periodo, "fca10data") : "";
		String ordem = ordenacao == 0 ? " ORDER BY abh80.abh80codigo, fca10.fca10data" : ordenacao == 1 ? " ORDER BY abh80.abh80nome, fca10.fca10data" : " ORDER BY abb11.abb11codigo, abh80.abh80codigo, fca10.fca10data";

		String sql = "SELECT * FROM Fca10 AS fca10 " +
				"INNER JOIN FETCH fca10.fca10trab AS abh80 " +
				"INNER JOIN FETCH fca10.fca10depto AS abb11 " +
				"INNER JOIN FETCH fca10.fca10mapHor AS abh13 " +
				"LEFT JOIN FETCH fca10.fca1001s AS fca1001 " +
				"INNER JOIN FETCH fca10.fca10tpDia As abh08 "+
				"WHERE fca10.fca10consistente = :cons AND fca10.fca10sit IN (:situacoes) " +
				whereData + whereTrabalhadores + whereDeptos + whereMapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) + ordem;

		Query query = getSession().createQuery(sql);
		query.setParameter("cons", 1);
		query.setParameter("situacoes", situacoes);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);

		return query.getList(ColumnType.ENTITY);
	}

	/**Método Diverso
	 * @return 	List TableMap da Fca2001 (Query da busca - Banco de Horas do Calculo Salários)
	 */
	private List<Fca2001> findBancoHorasByCalculoSalarios(Long idAbh80, Integer numMesesInicial, Integer numMesesFinal, boolean isAnterior) {
		String numMeses = isAnterior ? " < :numMesesInicial " : " BETWEEN :numMesesInicial AND :numMesesFinal ";
		Criterion mes = isAnterior ? Criterions.lt(Fields.numMeses("fca20mes", "fca20ano"),numMesesInicial ) : Criterions.between(Fields.numMeses("fca20mes", "fca20ano"),numMesesInicial,numMesesFinal )
		return getSession().createCriteria(Fca2001.class)
			.addFields("fca20id, fca2001id, fca2001horas, fca2001quita, abh21id, abh21tipo")
			.addJoin(Joins.part("fca2001bh"))
			.addJoin(Joins.part("fca2001eve"))
			.addWhere(getSamWhere().getCritPadrao(Fca2001.class))
			.addWhere(Criterions.eq("fca20trab", idAbh80))
			.addWhere(Criterions.eq("fca20quitado", Fca2001.NAO))
			.addWhere(mes)
			.getList(ColumnType.ENTITY)
	}

	/**Método Diverso
	 * @return 	Abb11 (Busca Departamento pelo id)
	 */
	private Abb11 getDepartamentoById(Long idAbb11) {
		return getSession().createCriteria(Abb11.class, "abb11id, abb11codigo, abb11nome", Criterions.eq("abb11id", idAbb11)).get(ColumnType.ENTITY);
	}

	/**Método Diverso
	 * @return 	Abh05 (Busca Cargo pelo id)
	 */
	private Abh05 getCargoById(Long idAbh05) {
		return getSession().createCriteria(Abh05.class, "abh05id, abh05codigo, abh05nome", Criterions.eq("abh05id", idAbh05)).get(ColumnType.ENTITY);
	}

	/**Método Diverso
	 * @return 	Método para organizar as marcações
	 */
	private void organizarMarcacoes(LocalTime[] marcacoes, LocalTime marcacao, int clas) {
		if(clas == 0 || clas == 1) {
			for(int i = clas; i < marcacoes.length; i+=2) {
				if(i + 1 != marcacoes.length && marcacoes[i] == null && marcacoes[i + 1] != null) continue;

				if(marcacoes[i] == null) {
					marcacoes[i] = marcacao;
					return;
				}
			}
		}
	}

	/**Método Diverso
	 * @return 	Abh21 (Busca Evento pelo código)
	 */
	private Abh21 getAbh21ByCodigo(String abh21codigo) {
		return getSession().createCriteria(Abh21.class)
				.addWhere(Criterions.eq("abh21codigo", abh21codigo))
				.addWhere(getSamWhere().getCritPadrao(Abh21.class))
				.get();
	}

	/**Método Diverso
	 * @return 	Abh21 (Busca Evento pelo código e pelo ponto)
	 */
	private Abh21 getEventoByFca10eventos(String abh21codigo, Fca10 fca10) {
		nroCampoFolha = 0;

		Abh21 abh21 = getAbh21ByCodigo(abh21codigo);

		for(int campo = 0; campo < 20; campo++) {
			if(fca10.getEvento(campo) != null && fca10.getEvento(campo).getAbh21id().equals(abh21.getAbh21id())) {
				nroCampoFolha = campo;
				return abh21;
			}
		}
		return null;
	}

	/**Método Diverso
	 * @return 	Integer (Busca Horas da Folha pelo ponto)
	 */
	private Integer getHorasFolhaByFca10folhas(Fca10 fca10) {
		return fca10.getHorasFolha(nroCampoFolha) != null ? fca10.getHorasFolha(nroCampoFolha) : 0;
	}

	/**Método Diverso
	 * @return 	String (Busca o dia da semana)
	 */
	private String getDiaSemana(LocalDate data) {
		String diaDaSemana = null;

		if(data.getDayOfWeek() == DayOfWeek.SUNDAY) {
			diaDaSemana = "Dom";
		}else if(data.getDayOfWeek() == DayOfWeek.MONDAY){
			diaDaSemana = "Seg";
		}else if(data.getDayOfWeek() == DayOfWeek.TUESDAY){
			diaDaSemana = "Ter";
		}else if(data.getDayOfWeek() == DayOfWeek.WEDNESDAY){
			diaDaSemana = "Qua";
		}else if(data.getDayOfWeek() == DayOfWeek.THURSDAY){
			diaDaSemana = "Qui";
		}else if(data.getDayOfWeek() == DayOfWeek.FRIDAY){
			diaDaSemana = "Sex";
		}else {
			diaDaSemana = "Sab";
		}
		return diaDaSemana;
	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Total de Horas pelo Cartão de Ponto)
	 */
	private List<TableMap> findTotalHorasByCartaoDePonto(String codAbh80, List<Long> idsDeptos, List<Long> idsMapHorario, LocalDate[] periodo, String numCampoFaltaDesc, int complemDSR) {
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11.abb11id IN (:idsDeptos) " : "";
		String whereMapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? " AND abh13.abh13id IN (:idsMapHorario) " : "";
		String whereData = periodo != null ? getWhereDataInterval("AND", periodo, "fca10data") : "";
		Integer intCampoFalta = Integer.parseInt(numCampoFaltaDesc);
		String campoFaltaDesc = String.format("%02d", intCampoFalta);

		String sql = "SELECT abh80codigo, SUM(fca10horDiu) as totalDiu, SUM(fca10horNot) as totalNot, SUM(fca10heDiu) as totalEDiu, SUM(fca10heNot) as totalENot, " +
				"SUM(fca10folha" + campoFaltaDesc + ") as totalFaltas, SUM(fca10complem" + complemDSR + ") as totalDSR " +
				"FROM Fca10 " +
				"INNER JOIN Abh80 ON fca10trab = abh80id " +
				"INNER JOIN Abb11 ON fca10depto = abb11id " +
				"INNER JOIN Abh13 ON fca10mapHor = abh13id " +
				"WHERE abh80codigo = :codAbh80 " + whereData +
				whereDeptos +whereMapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) +
				"GROUP BY abh80codigo ";

		Query query = getSession().createQuery(sql);

		query.setParameter("codAbh80", codAbh80);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);

		return query.getListTableMap();
	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Total de Eventos pelo Cartão de Ponto)
	 */
	private List<TableMap> findTotalEventosByCartaoDePonto(int evento, String codAbh80, List<Long> idsDeptos, List<Long> idsMapHorario, LocalDate[] periodo) {
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11.abb11id IN (:idsDeptos) " : "";
		String whereMapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? " AND abh13.abh13id IN (:idsMapHorario) " : "";
		String whereData = periodo != null ? getWhereDataInterval("AND", periodo, "fca10data") : "";
		String eve = String.format("%02d", evento);

		Query query = getSession().createQuery("SELECT abh21codigo, abh21nome, fca10folha" + eve + " as horas " +
				"FROM Fca10 " +
				"INNER JOIN Abh80 ON fca10trab = abh80id " +
				"INNER JOIN Abb11 ON fca10depto = abb11id " +
				"INNER JOIN Abh21 ON abh21id = fca10eve" + eve + " " +
				"INNER JOIN Abh13 ON fca10mapHor = abh13id " +
				"WHERE abh80codigo = :codAbh80 AND fca10folha" + eve + " > 0 " + whereData +
				whereDeptos + whereMapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) +
				"ORDER BY abh80codigo");
			
		String codigo = order == 2 ? codAbh80.split("/")[1] : codAbh80
		query.setParameter("codAbh80", codigo);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);

		return query.getListTableMap();
	}

	private Aap18 getAap18ById(Long aap18id) {
		return getSession().createCriteria(Aap18.class)
				.addWhere(Criterions.eq("aap18id", aap18id))
				.get();
	}

	private Abh13 getAbh13ById(Long abh13id) {
		return getSession().createCriteria(Abh13.class)
				.addWhere(Criterions.eq("abh13id", abh13id))
				.get();
	}

	private Abh21 getAbh21ById(Long abh21id) {
		return getSession().createCriteria(Abh21.class)
				.addWhere(Criterions.eq("abh21id", abh21id))
				.get();
	}

	private Abh21 getEventoFolha(Fca10 fca10) {
		String parametro = getAcessoAoBanco().obterString("select aba01param from aba01 where aba01param like 'NOMECAMPOFOLHA%' and UPPER (aba01conteudo) = UPPER ('faltas') " + obterWherePadrao("aba01","and"))
		
		Integer index = parametro.replace("NOMECAMPOFOLHA", "").toInteger()
		if(fca10.getEvento(index) == null ) return null
		return getSession().createCriteria(Abh21.class).addWhere(Criterions.eq("abh21id", fca10.getEvento(index).abh21id)).get(ColumnType.ENTITY)
		
	}

	/**Método Diverso
	 * @return 	Busca e monta os Dados do Cartão de Ponto
	 */
	private void getDadosCartaoDePonto(List<TableMap> dadosPrincipal, List<TableMap> dadosSubS1, List<TableMap> dadosSubS2 , List<Fca10> fca10s, int ordenacao, boolean isREP, boolean isPA, boolean pa, LocalDate[] periodo, String campoFaltaDesc, Integer complemDSR, List<Long> idsDeptos, List<Long> idsMapHorario, String numCampoFaltaDesc) {
		Integer numMesesInicial = DateUtils.numMeses(periodo[0].getMonthValue(), periodo[0].getYear());
		Integer numMesesFinal = DateUtils.numMeses(periodo[1].getMonthValue(), periodo[1].getYear());

		if(fca10s != null && fca10s.size() > 0) {
			String codControle = null;

			for(Fca10 fca10 : fca10s) {
				Abh80 abh80 = fca10.getFca10trab();

				String key = ordenacao == 2 ? fca10.getFca10depto().getAbb11codigo() + "/" + abh80.getAbh80codigo() : abh80.getAbh80codigo();

				if(codControle == null || !codControle.equals(key)) {
					//Pega o saldo de horas no banco do trabalhador no período selecionado (se houver).
					int saldoPeriodo = 0;
					List<Fca2001> fca2001SaldoPeriodo = findBancoHorasByCalculoSalarios(abh80.getAbh80id(), numMesesInicial, numMesesFinal, false);
					if(fca2001SaldoPeriodo != null && fca2001SaldoPeriodo.size() > 0) {
						for(int i = 0; i < fca2001SaldoPeriodo.size(); i++) {
							if(fca2001SaldoPeriodo.get(i).getFca2001quita().equals(0)) {
								if(fca2001SaldoPeriodo.get(i).getFca2001eve().getAbh21tipo().equals(Abh21.TIPO_RENDIMENTO)){
									saldoPeriodo += fca2001SaldoPeriodo.get(i).getFca2001horas();
								}else{
									saldoPeriodo -= fca2001SaldoPeriodo.get(i).getFca2001horas();
								}
							} else {
								if(fca2001SaldoPeriodo.get(i).getFca2001eve().getAbh21tipo().equals(Abh21.TIPO_RENDIMENTO)){
									saldoPeriodo += fca2001SaldoPeriodo.get(i).getFca2001horas();
								}else{
									saldoPeriodo -= fca2001SaldoPeriodo.get(i).getFca2001horas();
								}
							}
						}
					}

					//Pega o saldo de horas antes do período selecionado (se houver).
					int saldoAnterior = 0;
					List<Fca2001> fca2001SaldoAnt = findBancoHorasByCalculoSalarios(abh80.getAbh80id(), numMesesInicial, numMesesFinal, true);
					if(fca2001SaldoAnt != null && fca2001SaldoAnt.size() > 0) {
						for(int i= 0; i < fca2001SaldoAnt.size(); i++) {
							if(fca2001SaldoAnt.get(i).getFca2001quita().equals(0)) {
								if(fca2001SaldoAnt.get(i).getFca2001eve().getAbh21tipo().equals(Abh21.TIPO_RENDIMENTO)) {
									saldoAnterior += fca2001SaldoAnt.get(i).getFca2001horas();
								} else {
									saldoAnterior -= fca2001SaldoAnt.get(i).getFca2001horas();
								}
							} else {
								if(fca2001SaldoAnt.get(i).getFca2001eve().getAbh21tipo().equals(Abh21.TIPO_RENDIMENTO)) {
									saldoAnterior += fca2001SaldoAnt.get(i).getFca2001horas();
								} else {
									saldoAnterior -= fca2001SaldoAnt.get(i).getFca2001horas();
								}
							}
						}
					}

					//Saldo total de horas do banco.
					int saldoTotal = saldoPeriodo + saldoAnterior;

					TableMap dsTrab = new TableMap();
					dsTrab.put("key", key);
					dsTrab.put("trabalhador", StringUtils.concat(abh80.getAbh80codigo(), " - ", abh80.getAbh80nome()));
					dsTrab.put("abh80cpf", abh80.getAbh80cpf())
					dsTrab.put("trabJornada", getAbh13ById(abh80.abh80mapHor.abh13id).abh13nome);
					dsTrab.put("dtAdmis", abh80.getAbh80dtAdmis());
					dsTrab.put("ctps", abh80.getAbh80ctpsNum() == null ? "" : StringUtils.concat(abh80.getAbh80ctpsNum(), "/", (abh80.getAbh80ctpsSerie() == null ? "" : abh80.getAbh80ctpsSerie())));
					dsTrab.put("tipoSal", abh80.getAbh80unidPagto() == null ? "" : getAap18ById(abh80.getAbh80unidPagto().getAap18id()).aap18descr )
					dsTrab.put("tipoTrab", abh80.getAbh80tipo() == 0 ? "Trabalhador" : (abh80.getAbh80tipo() == 1 ? "Autônomo" : (abh80.getAbh80tipo() == 2 ? "Pró-labore" : "Terceiros")));
					dsTrab.put("depto", fca10.getFca10depto() == null ? "" : StringUtils.concat(fca10.getFca10depto().getAbb11codigo(), " - ", (fca10.getFca10depto().getAbb11nome() == null ? "" : fca10.getFca10depto().getAbb11nome())));

					Abh05 abh05 = getCargoById(abh80.getAbh80cargo().getIdValue());
					dsTrab.put("cargo", abh05 == null ? "" : StringUtils.concat(abh05.getAbh05codigo(), " - ", (abh05.getAbh05nome() == null ? "" : abh05.getAbh05nome())));

					dsTrab.put("saldoBHPer", saldoPeriodo);
					dsTrab.put("saldoBHAnt", saldoAnterior);
					dsTrab.put("saldoBHTotal", saldoTotal);

					dadosPrincipal.add(dsTrab);

					codControle = ordenacao == 2 ? StringUtils.concat(fca10.getFca10depto().getAbb11codigo(), "/", abh80.getAbh80codigo()) : abh80.getAbh80codigo();
				}


				/****
				 **** SUB-RELATÓRIO 01
				 ************************/

				//Prepara as marcações (se houver).
				LocalTime[] marcacoes = new LocalTime[8];
				if(fca10.getFca1001s() != null) {
					List<Object> fca1001s = fca10.getFca1001s().stream().sorted(new Comparator<Fca1001>() {
								public int compare(Fca1001 o1, Fca1001 o2) {
									int ano1 = o1.getFca1001data().getYear();
									String mes1 = (String) (o1.getFca1001data().getMonth().getValue() < 10 ? "0"+o1.getFca1001data().getMonth().getValue() : o1.getFca1001data().getMonth().getValue());
									String dia1 = (String) (o1.getFca1001data().getDayOfMonth() < 10 ? "0"+o1.getFca1001data().getDayOfMonth() : o1.getFca1001data().getDayOfMonth());
									String data1 = ano1 +""+ mes1 +""+ dia1;

									int ano2 = o2.getFca1001data().getYear();
									String mes2 = (String) (o2.getFca1001data().getMonth().getValue() < 10 ? "0"+o2.getFca1001data().getMonth().getValue() : o2.getFca1001data().getMonth().getValue());
									String dia2 = (String) (o2.getFca1001data().getDayOfMonth() < 10 ? "0"+o2.getFca1001data().getDayOfMonth() : o2.getFca1001data().getDayOfMonth());
									String data2 = ano2 +""+ mes2 +""+ dia2;

									String dataHora1 = data1 +"-"+ o1.getFca1001hrBase();
									String dataHora2 = data2 +"-"+ o2.getFca1001hrBase();
									return dataHora1.compareTo(dataHora2);
								}
							}).collect(Collectors.toList());

					for(Object fca1001 : fca1001s) {
						if(isREP) {
							LocalTime marcacao = ((Fca1001) fca1001).getFca1001hrRep();
							if(marcacao == null && isPA && ((Fca1001) fca1001).getFca1001pa().equals(1)){
								marcacao = ((Fca1001) fca1001).getFca1001hrBase();
							}
							if(marcacao != null)organizarMarcacoes(marcacoes, marcacao, ((Fca1001) fca1001).getFca1001classificacao());
						}else if(!isREP){
							organizarMarcacoes(marcacoes, ((Fca1001) fca1001).getFca1001hrBase(), ((Fca1001) fca1001).getFca1001classificacao());
						}
					}
				}

				String hDiu = fca10.getFca10horDiu() == 0 ? "" : String.format("%02d", (fca10.getFca10horDiu()/60).intValue()) + ":" + String.format("%02d", (fca10.getFca10horDiu()%60).intValue());
				String hNot = fca10.getFca10horNot() == 0 ? "" : String.format("%02d", (fca10.getFca10horNot()/60).intValue()) + ":" + String.format("%02d", (fca10.getFca10horNot()%60).intValue());
				String hEDiu = fca10.getFca10heDiu() == 0 ? "" : String.format("%02d", (fca10.getFca10heDiu()/60).intValue()) + ":" + String.format("%02d", (fca10.getFca10heDiu()%60).intValue());
				String hENot = fca10.getFca10heNot() == 0 ? "" : String.format("%02d", (fca10.getFca10heNot()/60).intValue()) + ":" + String.format("%02d", (fca10.getFca10heNot()%60).intValue());

				int soma = fca10.getFca10horDiu() + fca10.getFca10horNot() + fca10.getFca10heDiu() + fca10.getFca10heNot();
				String total = soma == 0 ? "" : String.format("%02d", (soma/60).intValue()) + ":" + String.format("%02d", (soma%60).intValue());

				Integer minFalta = fca10.getFca10horFalt_Zero()
				String hEveFalta = minFalta == 0 ? "" : String.format("%02d", (minFalta/60).intValue()) + ":" + String.format("%02d", (minFalta%60).intValue());

				
				Abh21 abh21 = minFalta > 0 ? getEventoFolha(fca10) : null
				String eveFalta = abh21 == null ? "" : abh21.getAbh21codigo();

				int valor = 0;
				if(complemDSR.intValue() == 0) {
					valor = fca10.getFca10complem0();
				}else if(complemDSR.intValue() == 1) {
					valor = fca10.getFca10complem1();
				}else if(complemDSR.intValue() == 2) {
					valor = fca10.getFca10complem2();
				}else if(complemDSR.intValue() == 3) {
					valor = fca10.getFca10complem3();
				}else if(complemDSR.intValue() == 4) {
					valor = fca10.getFca10complem4();
				}
				String dsr = valor == 0 ? "" : String.format("%02d", (valor/60).intValue()) + ":" + String.format("%02d", (valor%60).intValue());


				//Seta os dados das marcações.
				TableMap dsSub1 = new TableMap();
				dsSub1.put("key", key);
				dsSub1.put("data", fca10.getFca10data());
				dsSub1.put("sem", getDiaSemana(fca10.getFca10data()));
				dsSub1.put("tipo", fca10.getFca10tpDia().getAbh08descr());
				dsSub1.put("ent1", marcacoes[0]);
				dsSub1.put("sai1", marcacoes[1]);
				dsSub1.put("ent2", marcacoes[2]);
				dsSub1.put("sai2", marcacoes[3]);
				dsSub1.put("ent3", marcacoes[4]);
				dsSub1.put("sai3", marcacoes[5]);
				dsSub1.put("ent4", marcacoes[6]);
				dsSub1.put("sai4", marcacoes[7]);
				dsSub1.put("hDiu", hDiu);
				dsSub1.put("hNot", hNot);
				dsSub1.put("hEDiu", hEDiu);
				dsSub1.put("hENot", hENot);
				dsSub1.put("total", total);
				dsSub1.put("eveFalta", eveFalta);
				dsSub1.put("hEveFalta", hEveFalta);
				dsSub1.put("dsr", dsr);
				dsSub1.put("horas01", fca10.getFca10folha00());
				dsSub1.put("horas02", fca10.getFca10folha01());
				dsSub1.put("horas03", fca10.getFca10folha02());
				dsSub1.put("horas04", fca10.getFca10folha03());
				dsSub1.put("horas05", fca10.getFca10folha04());
				dsSub1.put("horas06", fca10.getFca10folha05());
				dsSub1.put("horas07", fca10.getFca10folha06());
				dsSub1.put("horas08", fca10.getFca10folha07());
				dsSub1.put("horas09", fca10.getFca10folha08());
				dsSub1.put("horas10", fca10.getFca10folha09());
				dsSub1.put("horas11", fca10.getFca10folha10());
				dsSub1.put("horas12", fca10.getFca10folha11());
				dsSub1.put("horas13", fca10.getFca10folha12());
				dsSub1.put("horas14", fca10.getFca10folha13());
				dsSub1.put("horas15", fca10.getFca10folha14());
				dsSub1.put("horas16", fca10.getFca10folha15());
				dsSub1.put("horas17", fca10.getFca10folha16());
				dsSub1.put("horas18", fca10.getFca10folha17());
				dsSub1.put("horas19", fca10.getFca10folha18());
				dsSub1.put("horas20", fca10.getFca10folha19());
				dsSub1.put("trabalhador", StringUtils.concat(abh80.getAbh80codigo(), " - ", abh80.getAbh80nome()));
				dadosSubS1.add(dsSub1);
			}
		}

		/****
		 **** SUB-RELATÓRIO 02
		 ************************/

		for(int i = 0; i < dadosPrincipal.size(); i++) {
			String codAbh80 = ordenacao == 2 ? dadosPrincipal.get(i).getString("key").split("/")[1] : dadosPrincipal.get(i).getString("key");

			List<TableMap> rsTotais = findTotalHorasByCartaoDePonto(codAbh80, idsDeptos, idsMapHorario, periodo, numCampoFaltaDesc, complemDSR);
			if(rsTotais != null && rsTotais.size() > 0) {

				String totalDiu = String.format("%02d", (rsTotais.get(0).getInteger("totalDiu")/60).intValue()) + ":" + String.format("%02d", (rsTotais.get(0).getInteger("totalDiu")%60).intValue());
				String totalNot = String.format("%02d", (rsTotais.get(0).getInteger("totalNot")/60).intValue()) + ":" + String.format("%02d", (rsTotais.get(0).getInteger("totalNot")%60).intValue());
				String totalEDiu = String.format("%02d", (rsTotais.get(0).getInteger("totalEDiu")/60).intValue()) + ":" + String.format("%02d", (rsTotais.get(0).getInteger("totalEDiu")%60).intValue());
				String totalENot = String.format("%02d", (rsTotais.get(0).getInteger("totalENot")/60).intValue()) + ":" + String.format("%02d", (rsTotais.get(0).getInteger("totalENot")%60).intValue());
				String totalFaltas = String.format("%02d", (rsTotais.get(0).getInteger("totalFaltas")/60).intValue()) + ":" + String.format("%02d", (rsTotais.get(0).getInteger("totalFaltas")%60).intValue());
				String totalDSR = String.format("%02d", (rsTotais.get(0).getInteger("totalDSR")/60).intValue()) + ":" + String.format("%02d", (rsTotais.get(0).getInteger("totalDSR")%60).intValue());

				dadosPrincipal.get(i).put("totalDiu", totalDiu);
				dadosPrincipal.get(i).put("totalNot", totalNot);
				dadosPrincipal.get(i).put("totalEDiu", totalEDiu);
				dadosPrincipal.get(i).put("totalENot", totalENot);
				dadosPrincipal.get(i).put("totalFaltas", totalFaltas);
				dadosPrincipal.get(i).put("totalDSR", totalDSR);
				
			}
		}

		List<TableMap> mapDadosPrincipaisEventos = new ArrayList<TableMap>();
		int i = 0;

		//Ordena os dadosPricipais para buscar os eventos
		Collections.sort(dadosPrincipal, new Comparator<TableMap>() {
					public int compare(TableMap tm1, TableMap tm2)
					{
						return  tm1.getString("key").compareTo(tm2.getString("key"));
					}
				});

		for(TableMap mapDadosPrincipais : dadosPrincipal) {

			if(mapDadosPrincipaisEventos.size() <= 0) {
				mapDadosPrincipaisEventos.add(mapDadosPrincipais);
			}else{
				if(! mapDadosPrincipais.get("key").equals(mapDadosPrincipaisEventos.get(i).get("key"))) {
					mapDadosPrincipaisEventos.add(mapDadosPrincipais);
					i++;
				}
			}
		}

		//Calcula os totais das horas e dos eventos.
		List<TableMap> subRel2 = new ArrayList<TableMap>();

		for(TableMap mapDadosPrincipaisEvento : mapDadosPrincipaisEventos) {
			for(int j = 0; j < 20; j++) {

				List<TableMap> rsEventos = findTotalEventosByCartaoDePonto(j, mapDadosPrincipaisEvento.getString("key"), idsDeptos, idsMapHorario, periodo);
				if(rsEventos != null && rsEventos.size() > 0) {

					for(TableMap rsEvento : rsEventos) {

						TableMap sub2 = new TableMap();

						String key = mapDadosPrincipaisEvento.getString("key") + rsEvento.getString("abh21codigo");
						String eve = rsEvento.getString("abh21codigo") + " - " + rsEvento.getString("abh21nome");

						sub2.put("key", key);
						sub2.put("trab", mapDadosPrincipaisEvento.getString("key"));
						sub2.put("eve", eve);
						sub2.put("horas", (rsEvento.getInteger("horas")));
						sub2.put("trabalhador", mapDadosPrincipaisEvento.getString("trabalhador"));
						subRel2.add(sub2);
					}
				}
			}
		}

		//Ordena os Eventos para buscar os eventos
		Collections.sort(subRel2, new Comparator<TableMap>() {
					public int compare(TableMap tm1, TableMap tm2)
					{
						return  tm1.getString("key").compareTo(tm2.getString("key"));
					}
				});

		int j = 0;
		List<TableMap> sub2List = new ArrayList<TableMap>();

		for(TableMap tm : subRel2) {

			TableMap sub2 = new TableMap();

			if(sub2List.size() <= 0) {
				Integer horas = tm.getInteger("horas");
				sub2.put("key", tm.get("trab"));
				sub2.put("eve", tm.get("eve"));
				sub2.put("horas", horas);
				sub2.put("chave", tm.get("key"));
				sub2.put("trabalhador", tm.getString("trabalhador"));
				sub2List.add(sub2);
			}else {
				if(! tm.get("key").equals(sub2List.get(j).get("chave"))) {
					Integer horas = tm.getInteger("horas");
					sub2.put("key", tm.get("trab"));
					sub2.put("eve", tm.get("eve"));
					sub2.put("horas", horas);
					sub2.put("chave", tm.get("key"));
					sub2.put("trabalhador", tm.getString("trabalhador"));
					sub2List.add(sub2);
					j++;
				}else {
					Integer horas = sub2List.get(j).getInteger("horas") + tm.getInteger("horas");
					sub2List.get(j).put("horas", horas);
				}
			}
		}

		for(TableMap tm : sub2List) {

			TableMap sub2 = new TableMap();

			String horas = String.format("%02d", (tm.getInteger("horas")/60).intValue()) + ":" + String.format("%02d", (tm.getInteger("horas")%60).intValue());

			sub2.put("key", tm.get("key"));
			sub2.put("eve", tm.get("eve"));
			sub2.put("horas", horas);
			sub2.put("chave", tm.get("chave"));
			sub2.put("trabalhador", tm.getString("trabalhador"));

			dadosSubS2.add(sub2);
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIENhcnTDo28gZGUgUG9udG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=