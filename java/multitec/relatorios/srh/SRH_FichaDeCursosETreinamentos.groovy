package multitec.relatorios.srh;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh80;
import sam.model.entities.fa.Fad01;
import sam.model.entities.fa.Fad02;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

public class SRH_FichaDeCursosETreinamentos extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SRH - Ficha de Cursos e Treinamentos";
	}
	
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsCursos = getListLong("cursos");
		List<Long> idsDeptos = getListLong("departamentos");
		LocalDate[] datas = getIntervaloDatas("datas");
		boolean isDemitidos = get("considTrabDemitidos");
		
		params.put("EMP_RS", getVariaveis().getAac10().getAac10rs());
		params.put("EMP_NI", getVariaveis().getAac10().getAac10ni());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		params.put("StreamSub1", carregarArquivoRelatorio("SRH_FichaDeCursosETreinamentos_S1"));
		params.put("StreamSub2", carregarArquivoRelatorio("SRH_FichaDeCursosETreinamentos_S2"));
		params.put("StreamSub3", carregarArquivoRelatorio("SRH_FichaDeCursosETreinamentos_S3"));
		
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		
		//Subrelatório S1
		List<TableMap> subRelCursosRealizados = new ArrayList<>();
				
		//Subrelatório S2
		List<TableMap> subRelCursosNaoRealizados = new ArrayList<>();
		
		//Subrelatório S3
		List<TableMap> subRelQualificacoes = new ArrayList<>();
				
		//Dados que serão exibidos no relatório
		List<TableMap> abh80s = obterDadosDoRelatorio(idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, isDemitidos);
		if(abh80s != null && abh80s.size() > 0) {
			for(TableMap tbAbh80 : abh80s) {
				
				//Cursos realizados
				Long idAbh80 = tbAbh80.getLong("abh80id");
				Set<Long> idsFad01Cursados = new HashSet<>();
				List<TableMap> cursosRealizados = obterCursosRealizados(idsCursos, idAbh80, datas[0], datas[1]);
				if(cursosRealizados != null && cursosRealizados.size() > 0) {
					for(TableMap cursos : cursosRealizados) {
						String eficacia = cursos.getInteger("eficacia") == 0 ? "N" : "S";
						String certificado = cursos.getInteger("certificado") == 0 ? "N" : "S";

						cursos.put("eficacia", eficacia);
						cursos.put("certificado", certificado);
						cursos.put("abh80id", idAbh80);
						
						subRelCursosRealizados.add(cursos);
						idsFad01Cursados.add(cursos.getLong("fad01id"));
					}
				}
				
				//Cursos não realizados.
				Long idAbh05 = tbAbh80.getLong("abh05id");
				List<TableMap> cursosNaoRealizados = obterCursosNaoRealizados(idsCursos, idsFad01Cursados, idAbh05);
				if(cursosNaoRealizados != null && cursosNaoRealizados.size() > 0) {
					for(TableMap cursos : cursosNaoRealizados) {
						cursos.put("abh80id", idAbh80);
						
						subRelCursosNaoRealizados.add(cursos);
					}
				}
				
				//Qualificações curriculares.
				List<TableMap> qualificações = obterQualificacoes(idAbh80);
				if(qualificações != null && qualificações.size() > 0) {
					for(TableMap qualific : qualificações) {
						qualific.put("abh80id", idAbh80);

						subRelQualificacoes.add(qualific);
					}
				}
			}
		}
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(abh80s);
		dsPrincipal.addSubDataSource("DSSub1", subRelCursosRealizados, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DSSub2", subRelCursosNaoRealizados, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DSSub3", subRelQualificacoes, "abh80id", "abh80id");
		
		return gerarPDF("SRH_FichaDeCursosETreinamentos", dsPrincipal);
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		filtrosDefault.put("considTrabDemitidos", false);
		
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("datas", datas);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
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
	
	private List<TableMap> obterDadosDoRelatorio(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, boolean isDemitidos){
		String whereDemitidos = isDemitidos ? "" : " AND abh80sit <> 1";
		String strTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String strDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String strCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		
		Query query = getSession().createQuery("SELECT abh80id, abh80codigo, abh80nome, abh80dtAdmis, abh80crHabProf, abh80crOe, abh80crSigla, abh80crNum , abh80crRegiao, abb11codigo, abb11nome, abh05id, abh05codigo, abh05nome, aap06codigo, aap06descr ",
				 							   "FROM Abh80 ",
				 							   "INNER JOIN Abb11 ON abb11id = abh80depto ",
				 							   "INNER JOIN Abh05 ON abh05id = abh80cargo ",
				 							   "LEFT JOIN Aap06 ON aap06id = abh80gi ",
				 							   "WHERE abh80tipo IN (:tiposAbh80) ", whereDemitidos,
				 							   strTrabalhadores, strDeptos, strCargos, getSamWhere().getWherePadrao("AND", Abh80.class),
				 							   "ORDER BY abh80codigo");
		
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterCursosRealizados(List<Long> idsCursos, Long idAbh80, LocalDate dataInicial, LocalDate dataFinal){
		String strCursos = idsCursos != null && !idsCursos.isEmpty() ? " AND fad01id IN (:idsCursos) " : "";
		
		Query query = getSession().createQuery("SELECT fad01id, fad01codigo, fad01descr, fad02data, fad0201eficacia AS eficacia, fad0201certificado AS certificado, fad0201aprov ",
										       "FROM Fad0201 ",
											   "INNER JOIN Fad02 ON fad02id = fad0201lcto ",
											   "INNER JOIN Fad01 ON fad01id = fad02curso ",
											   "WHERE fad0201trab = :idAbh80  AND fad02data BETWEEN :dataInicial AND :dataFinal ", strCursos, getSamWhere().getWherePadrao("AND", Fad02.class),
											   "ORDER BY fad01codigo");
		
		if(idsCursos != null && !idsCursos.isEmpty()) query.setParameter("idsCursos", idsCursos);
		query.setParameter("idAbh80", idAbh80);
		query.setParameter("dataInicial", dataInicial);
		query.setParameter("dataFinal", dataFinal);
		
		return  query.getListTableMap();
	}
	
	private List<TableMap> obterCursosNaoRealizados(List<Long> idsCursos, Set<Long> idsFad01Cursados, Long idAbh05){
		String strCursos = idsCursos != null && idsCursos.isEmpty() ? "AND fad01id IN (:idsCursos) " : "";
		String whereCursados = idsFad01Cursados != null && idsFad01Cursados.size() > 0 ? "AND fad01id NOT IN (:idsFad01Cursados) " : "";

		
		Query query = getSession().createQuery("SELECT fad01codigo, fad01descr, fad01ch ",
											   "FROM Fad0101 ",
											   "INNER JOIN Fad01 ON fad01id = fad0101curso ",
											   "WHERE fad0101cargo = :idAbh05 " + whereCursados,
											   strCursos, getSamWhere().getWherePadrao("AND", Fad01.class) +
											   "ORDER BY fad01codigo");
		
		if(idsCursos != null && !idsCursos.isEmpty()) query.setParameter("idsCursos", idsCursos);
		if(idsFad01Cursados != null && idsFad01Cursados.size() > 0) query.setParameter("idsFad01Cursados", idsFad01Cursados);
		query.setParameter("idAbh05", idAbh05);
		
		return  query.getListTableMap();
	}
	
	private List<TableMap> obterQualificacoes(Long idAbh80){
		Query query = getSession().createQuery("SELECT abh8003detalhe, abh8003data1, abh8003data2, aap10codigo, aap10descr  ",
			     							   "FROM Abh8003 ",
			     							   "INNER JOIN Aap10 ON aap10id = abh8003qualif ",
			     							   "INNER JOIN Abh80 ON abh80id = abh8003trab ",
			     							   "WHERE abh8003trab = :idAbh80 ", getSamWhere().getWherePadrao("AND", Abh80.class));
		
		query.setParameter("idAbh80", idAbh80);
		
		return  query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNSSCAtIEZpY2hhIGRlIEN1cnNvcyBlIFRyZWluYW1lbnRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==