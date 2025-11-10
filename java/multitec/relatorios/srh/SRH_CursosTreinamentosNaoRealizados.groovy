package multitec.relatorios.srh;

import java.time.LocalDate;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh80;
import sam.model.entities.fa.Fad01;
import sam.model.entities.fa.Fad02;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SRH_CursosTreinamentosNaoRealizados extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SRH - Cursos e Treinamentos não Realizados";
	}
	
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsCursos = getListLong("cursos");
		LocalDate[] datas = getIntervaloDatas("datas");
		
		
		params.put("TITULO_RELATORIO", "Cursos e Treinamentos não Realizados");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		
		//Dados que serão exibidos no relatório
		List<TableMap> dados = new ArrayList<>();
		
		//Seleciona os cursos a partir do filtro.
		List<TableMap> fad01s = obterCursoETreinamentos(idsCursos);
		if(fad01s != null && fad01s.size() > 0) {
			for(TableMap tbFad01 : fad01s) {
				//Seleciona os cargos que devem fazer o curso.
				Long idFad01 = tbFad01.getLong("fad01id");
				List<Long> idsAbh05 = obterCargosPeloIdCurso(idFad01);
				
				//Seleciona os trabalhadores que fizeram o curso dentro do período selecionado na tela.
				List<Long> idsAbh80 = obterTrabalhadoresPeloIdCursoAndPeriodo(idFad01, datas[0], datas[1]);
				
				//Seleciona os trabalhadores que não fizeram o curso.
				List<TableMap> abh80s = obterDadosDoRelatorio(idsTrabalhadores, idsCargos, tiposTrab, idsAbh05, idsAbh80);
				if(abh80s != null && abh80s.size() > 0) {
					for(TableMap abh80 : abh80s) {
						
						Fad01 fad01 = getSession().createCriteria(Fad01.class, "fad01id, fad01codigo, fad01descr, fad01ch", Criterions.eq("fad01id", idFad01)).get(ColumnType.ENTITY);
						
						String curso = StringUtils.concat(fad01.getFad01codigo(), "-", fad01.getFad01descr());
						abh80.put("curso", curso);
						
						abh80.put("cargaHor", fad01.getFad01ch().toString());
						
						dados.add(abh80);
					}
				}
			}
		}
		return gerarPDF("SRH_CursosTreinamentosNaoRealizados_R1", dados);
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("datas", datas);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	private List<Long> obterTrabalhadoresPeloIdCursoAndPeriodo(Long idFad01, LocalDate dataIni, LocalDate dataFin){
		return getSession().createQuery("SELECT fad0201trab FROM Fad0201 ",
										"INNER JOIN Fad02 ON fad02id = fad0201lcto ",
										"INNER JOIN Fad01 ON fad01id = fad02curso ",
										"WHERE fad01id = :idFad01 AND fad02data BETWEEN :dataIni AND :dataFin ",
										getSamWhere().getWhereGc("AND", Fad02.class),
										" ORDER BY fad0201trab")
				
				.setParameter("idFad01", idFad01)
				.setParameter("dataIni", dataIni)
				.setParameter("dataFin", dataFin)
				.getList(ColumnType.LONG);
	}
	
	private List<Long> obterCargosPeloIdCurso(Long idFad01){
		return getSession().createQuery("SELECT fad0101cargo FROM Fad0101 ",
										"INNER JOIN Fad01 ON fad01id = fad0101curso ",
										"WHERE fad01id = :idFad01 ",
										getSamWhere().getWhereGc("AND", Fad01.class),
										" ORDER BY fad0101cargo ")
				.setParameter("idFad01", idFad01)
				.getList(ColumnType.LONG);
	}
	
	private List<TableMap> obterCursoETreinamentos(List<Long> idsCursos){
		String strCursos = idsCursos != null && !idsCursos.isEmpty() ? " AND fad01id IN (:idsCursos) " : "";
		
		Query query = getSession().createQuery(" SELECT DISTINCT fad01id, fad01codigo",
										  	   " FROM Fad0101",
										  	   " INNER JOIN Fad01 ON fad01id = fad0101curso ",
										  	   getSamWhere().getWhereGc("WHERE", Fad01.class),
										  	   strCursos);
		
		if(idsCursos != null && !idsCursos.isEmpty()) query.setParameter("idsCursos", idsCursos);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterDadosDoRelatorio(List<Long> idsTrabalhadores, List<Long> idsCargos, Set<Integer> tiposAbh80, List<Long> idsAbh05, List<Long> idsAbh80){
		String whereTrab = (idsAbh80 != null && idsAbh80.size() > 0) ? "AND abh80id NOT IN (:idsAbh80) " : "";
 		String whereCargo = (idsAbh05 != null && idsAbh05.size() > 0) ? "AND abh05id IN (:idsAbh05) " : "";
 		
 		String strTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : ""; 
 		String strCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
 		
		Query query = getSession().createQuery("SELECT ",
												Fields.concat("abh80codigo", "'-'", "abh80nome").toString() + " AS trabalhador, ",
												Fields.concat("abh05codigo", "'-'", "abh05nome").toString() + " AS cargo, ",
												Fields.concat("abb11codigo", "'-'", "abb11nome").toString() + " AS cc ",
											   "FROM Abh80 ",
											   "INNER JOIN Abh05 ON abh05id = abh80cargo ",
											   "INNER JOIN Abb11 ON abb11id = abh80depto ",
											   "WHERE abh80tipo IN (:tiposAbh80) AND abh80sit <> 1 ",
											   whereTrab, whereCargo, strTrabalhadores, strCargos,
											   getSamWhere().getWhereGc("AND", Abh80.class),
											   " ORDER BY abh80codigo ");
				
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsAbh80 != null && !idsAbh80.isEmpty()) query.setParameter("idsAbh80", idsAbh80);
		if(idsAbh05 != null && !idsAbh05.isEmpty()) query.setParameter("idsAbh05", idsAbh05);
		query.setParameter("tiposAbh80", tiposAbh80);
				
		return query.getListTableMap();
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
}
//meta-sis-eyJkZXNjciI6IlNSSCAtIEN1cnNvcyBlIFRyZWluYW1lbnRvcyBuw6NvIFJlYWxpemFkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=