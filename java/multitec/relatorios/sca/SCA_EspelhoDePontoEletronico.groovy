package multitec.relatorios.sca;

import java.time.LocalDate;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abh11;
import sam.model.entities.ab.Abh1301;
import sam.model.entities.fc.Fca10;
import sam.model.entities.fc.Fca1001;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SCA - Espelho de Ponto Eletrônico
 * @author Lucas Eliel
 * @since 08/05/2019
 * @version 1.0
 */

public class SCA_EspelhoDePontoEletronico extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Espelho de Ponto Eletrônico";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("sitTrabalhando", true);
		filtrosDefault.put("sitAfastado", false);
		filtrosDefault.put("sitFerias", false);
		filtrosDefault.put("ord", "0");
		filtrosDefault.put("marcacoes", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
				
		List<Long> idsTrabalhador = getListLong("trabalhador");
		List<Long> idsDepartamento = getListLong("departamento");
		List<Long> idsMapHorario = getListLong("mapHorario");
		LocalDate[] periodo = getIntervaloDatas("periodo");
		Set<Integer> situacoes = getSituacoes();
		int ordenacao = getInteger("ord");
		boolean isREP = getInteger("marcacoes") == 0 ? false : true;
		boolean isPA = getInteger("marcacoes") == 0 ? true : false;
		
		Aac10 aac10 = getVariaveis().getAac10();
		params.put("TITULO_RELATORIO", "Espelho de Ponto Eletrônico");
		params.put("PERIODO", DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		params.put("AAC10RS", aac10.getAac10rs());
		params.put("AAC10NI", aac10.getAac10ni());
		params.put("AAC10ENDERECO", aac10.getAac10endereco());
		params.put("AAC10COMPLEM", aac10.getAac10complem());
		params.put("AAC10NUMERO", aac10.getAac10numero());
		params.put("AAC10BAIRRO", aac10.getAac10bairro());
		params.put("AAC10MUNICIPIO", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
		params.put("AAC10UF", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
		params.put("AAC10CEP", aac10.getAac10cep() == null ? null : aac10.getAac10cep());
		params.put("sca_espelhor1s1Path", carregarArquivoRelatorio("SCA_EspelhoDePontoEletronico_S1"));
		params.put("sca_espelhor1s2Path", carregarArquivoRelatorio("SCA_EspelhoDePontoEletronico_S2"));
		
		List<TableMap> dadosPrincipal = new ArrayList<>();
		List<TableMap> dadosSubS1 = new ArrayList<>();
		List<TableMap> dadosSubS2 = new ArrayList<>();
		
		String campoFaltaDesc = getParametros(Parametros.FC_CODEVENTOFALTADESC);
		if(StringUtils.isNullOrEmpty(campoFaltaDesc)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FC_CODEVENTOFALTADESC.");
		
		String numComplemDSR = getParametros(Parametros.FC_NUMCOMPLEMDSR);
		if(StringUtils.isNullOrEmpty(numComplemDSR)) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FC_NUMCOMPLEMDSR.");
		
		Integer complemDSR = Integer.parseInt(numComplemDSR);
		
		if(existePontosInconsistentes(periodo, idsTrabalhador, idsDepartamento, situacoes)) throw new ValidacaoException("Existem pontos inconsistentes no filtro informado.");
	
		List<Fca10> fca10s = findFca10sByEspelhoPontoEletronico(idsTrabalhador, idsDepartamento, idsMapHorario, periodo, situacoes, ordenacao);
		List<TableMap> marcacoes = findMarcacoesByEspelhoPontoEletronico(idsTrabalhador, idsDepartamento, idsMapHorario, periodo, situacoes, ordenacao);
		getEspelhoPontoEletronico(dadosPrincipal, dadosSubS1, dadosSubS2, fca10s, marcacoes, ordenacao, isREP, isPA, periodo, campoFaltaDesc, complemDSR, idsDepartamento, idsMapHorario, idsTrabalhador);
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(dadosPrincipal);
		dsPrincipal.addSubDataSource("DsSub1R1", dadosSubS1, "abh80codigo", "abh80codigo");
		dsPrincipal.addSubDataSource("DsSub2R1", dadosSubS2, "chave", "chave");
		
		return gerarPDF("SCA_EspelhoDePontoEletronico", dsPrincipal);
	
	}
	
	/**Método Diverso
	 * @return Set Integer (Situação do Trabalhador)
	 */
	private Set<Integer> getSituacoes(){
		Set<Integer> situacoes = new HashSet<>();
		
		if((boolean) get("sitTrabalhando")) situacoes.add(0);
		if((boolean) get("sitAfastado")) situacoes.add(1);
		if((boolean) get("sitFerias")) situacoes.add(2);
		
		if(situacoes.size() == 0) {
			situacoes.add(0);
			situacoes.add(1);
			situacoes.add(2);
		}
		return situacoes;
	}
	
	/**Método Diverso
	 * @return Aag0201 (Município)
	 */
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addJoin(Joins.fetch("aag0201uf"))
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
	
	/**Método Diverso
	 * @return String (Space)
	 */
	private static String space(int tamanho){
		StringBuffer retorno = new StringBuffer("");
		for(int i = 0; i < tamanho; i++) {
			return retorno.append(" ").toString();
		}
	}
	
	/**Método Diverso
	 * @return 	List de Fca10 (Espelho de Ponto Eletrônico)
	 */
	private List<Fca10> findFca10sByEspelhoPontoEletronico(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, LocalDate[] periodo, Set<Integer> situacoes, int ordenacao){
		String whereTrabalhadores = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? " AND abh80.abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDepartamento != null && !idsDepartamento.isEmpty() ? " AND abb11.abb11id IN (:idsDeptos) " : "";
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
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDeptos", idsDepartamento);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
	
		return query.getList(ColumnType.ENTITY);
	}
	
	/**Método Diverso
	 * @return 	List de TableMap (Horários Contratuais)
	 */
	private List<TableMap> findAbh13ByHorariosContratuais(List<Long> idsTrabalhador){
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
	
		String sql = "SELECT DISTINCT abh80codigo, abh11codigo, abh11horaE, abh11jorLiq, abh11horaS, abh11intervE, abh11intervS "+
					 "FROM fca10 "+
					 "INNER JOIN Abh80 ON abh80id = fca10trab "+
					 "INNER JOIN Abh13 ON abh13id = fca10mapHor "+
					 "INNER JOIN Abh1301 ON abh1301mapHor = abh13id "+
					 "INNER JOIN Abh11 ON Abh11id = abh1301horario "+
					 whereAbh80Trabalhador + getSamWhere().getWherePadrao("AND", Fca10.class);
		
        Query query = getSession().createQuery(sql);
        
    	if(idsTrabalhador != null) query.setParameter("idsTrabalhador", idsTrabalhador);
    	
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List de TableMap (Espelho de Ponto Eletrônico)
	 */
	public List<TableMap> findMarcacoesByEspelhoPontoEletronico(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, LocalDate[] periodo, Set<Integer> situacoes, int ordenacao) {
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";
		String whereDt = periodo != null ? getWhereDataInterval("WHERE", periodo, "fca10.fca10data") : "";
		
		String sql = "SELECT abh80codigo, fca10data, fca1001hrBase, fca1001hrRep, fca1001classificacao, fca1001pa, fca1001justificativa, fca10data " +
                     "FROM Fca1001 " +
                     "INNER JOIN Fca10 ON fca10id = fca1001ponto " +
                     "INNER JOIN Abh80 ON abh80id = fca10trab " +
                     "INNER JOIN Abb11 ON abb11id = fca10depto " +
                     "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
                     whereDt +" AND fca10consistente = :cons AND fca10sit IN (:sit) " +
                     whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) + 
                     " ORDER BY abh80codigo, fca10data, fca1001hrBase";
			
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		query.setParameter("sit", situacoes);
		query.setParameter("cons", 1);
		
		return query.getListTableMap();

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
	 * @return 	Marcacoes (Abh1301)
	 */
	private Abh1301 findMarcacoesByUniqueKey(Long idAbh13, LocalDate data) {
		return getSession().createCriteria(Abh1301.class)
				.addJoin(Joins.fetch("abh1301horario"))
				.addWhere(Criterions.eq("abh1301data", data))
				.addWhere(Criterions.eq("abh1301mapHor", idAbh13))
				.get();
	}
	
	/**Método Diverso
	 * @return 	Boolean (Pontos Inconsistentes)
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
	 * @return 	Espelho do Ponto Eletrônico
	 */
	public void getEspelhoPontoEletronico(List<TableMap> dadosPrincipal, List<TableMap> dadosSubS1, List<TableMap> dadosSubS2 , List<Fca10> fca10s, List<TableMap> marcacoes, int ordenacao, boolean isREP, boolean isPA, LocalDate[] periodo, String campoFaltaDesc, Integer complemDSR, List<Long> idsDepartamento, List<Long> idsMapHorario, List<Long> idsTrabalhador) {
		if(fca10s != null && fca10s.size() > 0) {
			for(Fca10 fca10 : fca10s) {
				TableMap mapPrincipal = new TableMap();
				
				mapPrincipal.put("abh80id", fca10.getFca10trab().getAbh80id());
				mapPrincipal.put("abh80codigo", fca10.getFca10trab().getAbh80codigo());
				mapPrincipal.put("abh80nome", fca10.getFca10trab().getAbh80nome());
				mapPrincipal.put("abh80dtadmis", fca10.getFca10trab().getAbh80dtAdmis());
				mapPrincipal.put("abh80pis", fca10.getFca10trab().getAbh80pis());
				mapPrincipal.put("fca10data", fca10.getFca10data());

				String chave = fca10.getFca10trab().getAbh80codigo() + "/" + fca10.getFca10data();
				mapPrincipal.put("chave", chave);
				
				Abh1301 abh1301 = findMarcacoesByUniqueKey(fca10.getFca10mapHor().getAbh13id(), fca10.getFca10data());
				if(abh1301 != null) {
					Abh11 abh11 = abh1301.getAbh1301horario();
					mapPrincipal.put("abh11codigo", abh11 == null ? null : abh11.getAbh11codigo());
				}
											
				if(fca10.getFca1001s() == null || fca10.getFca1001s().size() == 0) {
					mapPrincipal.put("rep", "");
					mapPrincipal.put("base", "");
				}else {
					String marcRep = null;
					String marcBase = null;
		
					TreeSet<Fca1001> fc101Ordem = new TreeSet<Fca1001>(new Comparator<Fca1001>(){
						public int compare(Fca1001 o1, Fca1001 o2) {
							return o1.getFca1001hrBase().compareTo(o2.getFca1001hrBase());	
						}
					});
					fc101Ordem.addAll(fca10.getFca1001s());
		
					//Alinhamento das Strings de hora base e hora rep do relatório,
					for(Fca1001 fca1001 : fc101Ordem) {
						String horaRep = fca1001.getFca1001hrRep() == null ? "" : fca1001.getFca1001hrRep().toString();
						String horaBase = fca1001.getFca1001hrBase() == null ? "" : fca1001.getFca1001hrBase().toString();

						if(marcRep == null){
							marcRep = horaRep;
							marcBase = fca1001.getFca1001classificacao() == 3 ? "" : (fca1001.getFca1001classificacao() == 0 ? horaBase : space(15) + horaBase);
						}else {
							marcRep = marcRep.equals("") ? horaRep : (horaRep.equals("") ? marcRep : marcRep +space(6)+ horaRep);
							marcBase = fca1001.getFca1001classificacao() == 3 ? marcBase : (marcBase == "" ? horaBase : marcBase +space(6)+ horaBase);
						}
					}
					mapPrincipal.put("rep", marcRep);
					mapPrincipal.put("base", marcBase);
				}
				dadosPrincipal.add(mapPrincipal);
			}
			
			
			//Sub-relatório dos horários contratuais.
			List<TableMap> abh13s = findAbh13ByHorariosContratuais(idsTrabalhador);
			dadosSubS1.addAll(abh13s);
		
			//Sub-relatório dos tratamentos efetuados sobre os dados originais.
			if(marcacoes != null && marcacoes.size() > 0) {
				for(TableMap dsSubFc101s : marcacoes) {
					TableMap mapSubMarcTrab = new TableMap();
					
					int clas = dsSubFc101s.getInteger("fca1001classificacao");
					int pa = dsSubFc101s.getInteger("fca1001pa");
					String just = dsSubFc101s.getString("fca1001justificativa");
					
					if(clas == 3 || pa == 1 || just != null) {
						
						mapSubMarcTrab.put("abh80codigo", dsSubFc101s.getString("abh80codigo"));
						mapSubMarcTrab.put("hora", dsSubFc101s.getTime("fca1001hrBase"));
						mapSubMarcTrab.put("ocor", clas == 3 ? "D" : (pa == 1 ? "P" : "I"));
						mapSubMarcTrab.put("motivo", dsSubFc101s.getString("fca1001justificativa"));
						
						String chave = dsSubFc101s.getString("abh80codigo") + "/" + dsSubFc101s.getDate("fca10data");
						mapSubMarcTrab.put("chave", chave);
					}
					
					dadosSubS2.add(mapSubMarcTrab);
				}
			}
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIEVzcGVsaG8gZGUgUG9udG8gRWxldHLDtG5pY28iLCJ0aXBvIjoicmVsYXRvcmlvIn0=