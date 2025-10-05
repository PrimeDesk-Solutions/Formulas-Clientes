package multitec.relatorios.srh;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fa.Fae01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

public class SRH_ListagemDeCurriculos extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SRH - Listagem de Curriculos";
	}
	
	@Override
	public DadosParaDownload executar() {
		Integer nroCurriculoIni = get("nroCurriculoIni") == null ? null : StringUtils.isNullOrEmpty(get("nroCurriculoIni").toString()) ? null : getInteger("nroCurriculoIni");
		Integer nroCurriculoFin = get("nroCurriculoFin") == null ? null : StringUtils.isNullOrEmpty(get("nroCurriculoFin").toString()) ? null : getInteger("nroCurriculoFin");
		LocalDate[] datas = getIntervaloDatas("datas");
		LocalDate[] dataValidade = getIntervaloDatas("dtValidade");
		List<Long> idsCargo = getListLong("cargos");
		List<Long> idsDepartamento = getListLong("departamentos");
		List<Long> idsQualificacoes = getListLong("qualificacoes");
		List<Long> idsMunicipio = getListLong("municipios");
		int sexo = getInteger("sexo");
		Set<Integer> situacoes = obterSituacoes();
		Integer tipoRel = getInteger("tipoRel");
		
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("StreamSub1R2", carregarArquivoRelatorio("SRH_ListagemDeCurriculos_R2_S1"));
		params.put("StreamSub2R3", carregarArquivoRelatorio("SRH_ListagemDeCurriculos_R3_S1"));
		params.put("StreamSub3R3", carregarArquivoRelatorio("SRH_ListagemDeCurriculos_R3_S2"));

		
		List<TableMap> curriculos = obterCurriculos(idsCargo, idsDepartamento, idsQualificacoes, idsMunicipio, nroCurriculoIni, nroCurriculoFin, datas, dataValidade, sexo, situacoes);
		
		List<TableMap> qualificacoes = new ArrayList<>();
		List<TableMap> recrutamentos = new ArrayList<>();
		
		for(TableMap fae01 : curriculos) {
			String descrSexo = fae01.getInteger("fae01sexo") == 0 ? "Masculino" : "Feminino";
			String telefone = fae01.getString("fae01ddd1") != null && fae01.getString("fae01fone1") != null ? fae01.getString("fae01ddd1") + "-" + fae01.getString("fae01fone1") : fae01.getString("fae01fone1");
			
			fae01.put("telefone", telefone);
			fae01.put("sexo", descrSexo);
			
			LocalDate verifData = fae01.getDate("fae01dtNasc")
			if(verifData != null) {
				int idade = (int) DateUtils.dateDiff(fae01.getDate("fae01dtNasc"), MDate.date(), ChronoUnit.MONTHS) / 12;
				fae01.put("idade", idade);
			}
			
			fae01.put("impQualificacoes", false);
			fae01.put("impRecrutamentos", false);
			
			
			if(tipoRel == 1 || tipoRel == 2) {
				qualificacoes.addAll(obterQualificacoesByIdCurriculo(fae01.getLong("fae01id")));
				
				if(qualificacoes != null && qualificacoes.size() > 0) fae01.put("impQualificacoes", true);
				
				if(tipoRel == 2) {
					recrutamentos = obterRecrutamentosByIdCurriculo(fae01.getLong("fae01id"));
					if(recrutamentos != null && recrutamentos.size() > 0) fae01.put("impRecrutamentos", true);
				}
			}
		}
		
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(curriculos);
		dsPrincipal.addSubDataSource("DSQualificacoes", qualificacoes, "fae01id", "fae01id");
		dsPrincipal.addSubDataSource("DSRecrutamentos", recrutamentos, "fae01id", "fae01id");
		
		return gerarPDF(tipoRel == 0 ? "SRH_ListagemDeCurriculos_R1" : tipoRel == 1 ? "SRH_ListagemDeCurriculos_R2" : "SRH_ListagemDeCurriculos_R3", dsPrincipal);
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("sexo", "2");
		
		filtrosDefault.put("sitDisponivel", true);
		filtrosDefault.put("sitRecrutamento", false);
		filtrosDefault.put("sitEfetivado", false);
		filtrosDefault.put("sitArquivado", false);
		
		filtrosDefault.put("tipoRel", "0");
		
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("datas", datas);
		
		LocalDate[] dtValidade = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("dtValidade", dtValidade);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	private Set<Integer> obterSituacoes(){
		Set<Integer> situacoes = new HashSet<>();
		
		if((boolean) get("sitDisponivel")) situacoes.add(0);
		if((boolean) get("sitRecrutamento")) situacoes.add(1);
		if((boolean) get("sitEfetivado")) situacoes.add(2);
		if((boolean) get("sitArquivado")) situacoes.add(3);
		
		if(situacoes.size() == 0) {
			situacoes.add(0);
			situacoes.add(1);
			situacoes.add(2);
			situacoes.add(3);
		}
		
		return situacoes;
	}

	private List<TableMap> obterCurriculos(List<Long> idsCargo, List<Long> idsDepartamento, List<Long> idsQualificacoes, List<Long> idsMunicipio, Integer nroCurriculoIni, Integer nroCurriculoFin, LocalDate[] datas, LocalDate[] dataValidade, int sexo, Set<Integer> situacoes){
		String whereAbh05Cargo1 = idsCargo != null && !idsCargo.isEmpty() ? " fae01cargo1 IN (:idsCargo) " : "";
		String whereAbh05Cargo2 = idsCargo != null && !idsCargo.isEmpty() ? " fae01cargo2 IN (:idsCargo) " : "";
		String whereAbh05Cargo3 = idsCargo != null && !idsCargo.isEmpty() ? " fae01cargo3 IN (:idsCargo) " : "";
		String whereAbh05Cargo4 = idsCargo != null && !idsCargo.isEmpty() ? " fae01cargo4 IN (:idsCargo) " : "";
		String whereAbh05Cargo5 = idsCargo != null && !idsCargo.isEmpty() ? " fae01cargo5 IN (:idsCargo) " : "";
		
		String addWhere = addWhereOrAnd("OR", whereAbh05Cargo1, whereAbh05Cargo2, whereAbh05Cargo3, whereAbh05Cargo4, whereAbh05Cargo5);
		String whereResultCargos = addWhere.isEmpty() ? "" : " AND (" + addWhere + ")";
		
		
		String whereAbb11Depto1 = idsDepartamento != null && !idsDepartamento.isEmpty() ? " fae01depto1 IN (:idsDepartamento) " : "";
		String whereAbb11Depto2 = idsDepartamento != null && !idsDepartamento.isEmpty() ? " fae01depto2 IN (:idsDepartamento) " : "";
		String whereAbb11Depto3 = idsDepartamento != null && !idsDepartamento.isEmpty() ? " fae01depto3 IN (:idsDepartamento) " : "";
		String whereAbb11Depto4 = idsDepartamento != null && !idsDepartamento.isEmpty() ? " fae01depto4 IN (:idsDepartamento) " : "";
		String whereAbb11Depto5 = idsDepartamento != null && !idsDepartamento.isEmpty() ? " fae01depto5 IN (:idsDepartamento) " : "";
		
		addWhere = addWhereOrAnd("OR", whereAbb11Depto1, whereAbb11Depto2, whereAbb11Depto3, whereAbb11Depto4, whereAbb11Depto5);
		String whereResultDeptos = addWhere.isEmpty() ? "" : " AND (" + addWhere + ")";
		
		
		String whereAap10Qualif = idsQualificacoes != null && !idsQualificacoes.isEmpty() ?  " AND fae0102qualif IN (:idsQualificacoes) " : "";
		
		String whereAag0201 = idsMunicipio != null && !idsMunicipio.isEmpty() ? " AND fae01municipio IN (:idsMunicipio) " : "";
		
		String whereSexo = sexo == 0 ? " AND fae01sexo = 0 " : sexo == 1 ? " AND fae01sexo = 1 " : "";
		
		String whereDtLcto = datas != null ? getWhereDataInterval("WHERE", datas, "fae01dtLcto") : "";
		String whereDtValidade = dataValidade != null ? getWhereDataInterval("AND", dataValidade, "fae01dtValidade") : "";
		
		String whereNum = nroCurriculoIni != null || nroCurriculoFin != null  ? getWhereIntegerInterval("AND", nroCurriculoIni, nroCurriculoFin, "fae01num") : "";
		
		String sql = "SELECT DISTINCT fae01id, fae01num, fae01nome, fae01endereco, fae01complem, fae01numero, fae01bairro, aag0201nome, aag02nome, aag01nome, fae01cep, fae01ddd1, fae01fone1, fae01email, fae01sexo, " +
					  "fae01ecivil, aap08descr AS eCivil, fae01dtNasc, " +
					  Fields.concat("cargo1.abh05codigo", "'-'", "cargo1.abh05nome") + " AS cargo1, " +
					  Fields.concat("cargo2.abh05codigo", "'-'", "cargo2.abh05nome") + " AS cargo2, " +
					  Fields.concat("cargo3.abh05codigo", "'-'", "cargo3.abh05nome") + " AS cargo3, " +
					  Fields.concat("cargo4.abh05codigo", "'-'", "cargo4.abh05nome") + " AS cargo4, " +
					  Fields.concat("cargo5.abh05codigo", "'-'", "cargo5.abh05nome") + " AS cargo5, " +
					  Fields.concat("dep1.abb11codigo", "'-'", "dep1.abb11nome") + " AS depto1, " +
					  Fields.concat("dep2.abb11codigo", "'-'", "dep2.abb11nome") + " AS depto2, " +
					  Fields.concat("dep3.abb11codigo", "'-'", "dep3.abb11nome") + " AS depto3, " +
					  Fields.concat("dep4.abb11codigo", "'-'", "dep4.abb11nome") + " AS depto4, " +
					  Fields.concat("dep5.abb11codigo", "'-'", "dep5.abb11nome") + " AS depto5, " +
					  "fae01salvlr1, fae01salvlr2, fae01salvlr3, fae01salvlr4, fae01salvlr5 " +
					 "FROM Fae0102 " +
					 "RIGHT JOIN Fae01 ON fae01id = fae0102cur " +
					 "LEFT JOIN Aap10 ON aap10id = fae0102qualif " +
					 "LEFT JOIN Abh05 cargo1 ON cargo1.abh05id = fae01cargo1 " +
					 "LEFT JOIN Abh05 cargo2 ON cargo2.abh05id = fae01cargo2 " +
					 "LEFT JOIN Abh05 cargo3 ON cargo3.abh05id = fae01cargo3 " +
					 "LEFT JOIN Abh05 cargo4 ON cargo4.abh05id = fae01cargo4 " +
					 "LEFT JOIN Abh05 cargo5 ON cargo5.abh05id = fae01cargo5 " +
					 "LEFT JOIN Abb11 dep1 ON dep1.abb11id = fae01depto1 " +
					 "LEFT JOIN Abb11 dep2 ON dep2.abb11id = fae01depto2 " +
					 "LEFT JOIN Abb11 dep3 ON dep3.abb11id = fae01depto3 " +
					 "LEFT JOIN Abb11 dep4 ON dep4.abb11id = fae01depto4 " +
					 "LEFT JOIN Abb11 dep5 ON dep5.abb11id = fae01depto5 " +
					 "LEFT JOIN Aag0201 ON Aag0201id = fae01municipio " +
					 "LEFT JOIN Aag02 ON aag02id = aag0201uf " +
					 "LEFT JOIN Aag01 ON aag01id = fae01pais " +
					 "LEFT JOIN Aap08 ON aap08id = fae01ecivil " +
					 whereDtLcto + whereDtValidade + whereNum + whereNum + " AND fae01sit IN (:sitsfae01) " + whereSexo +
					 whereAap10Qualif + whereAag0201 + getSamWhere().getWherePadrao("AND", Fae01.class) +
					 whereResultCargos + whereResultDeptos +
					 " ORDER BY fae01num";
		
		Query query = getSession().createQuery(sql);
		
		if(idsCargo != null && !idsCargo.isEmpty()) query.setParameter("idsCargo", idsCargo);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsQualificacoes != null && !idsQualificacoes.isEmpty()) query.setParameter("idsQualificacoes", idsQualificacoes);
		if(idsMunicipio != null && !idsMunicipio.isEmpty()) query.setParameter("idsMunicipio", idsMunicipio);
		if(nroCurriculoIni != null && nroCurriculoIni.toString().isEmpty()) query.setParameter("numInicial", nroCurriculoIni);
		if(nroCurriculoFin != null && nroCurriculoFin.toString().isEmpty()) query.setParameter("numFinal", nroCurriculoFin);
		query.setParameter("sitsfae01", situacoes);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterQualificacoesByIdCurriculo(Long fae01id){
		String sql = "SELECT fae0102cur AS fae01id, fae0102detalhe AS detalhamento, fae0102data1 AS data1, fae0102data2 AS data2, " +
					  Fields.concat("aap10codigo", "'-'", "aap10descr") + " AS qualificacao " +
					 "FROM Fae0102 " +
					 "INNER JOIN Fae01 ON fae01id = fae0102cur " +
					 "INNER JOIN Aap10 ON aap10id = fae0102qualif " +
					 "WHERE fae0102cur = :fae01id " + getSamWhere().getWherePadrao("AND", Fae01.class) +
					 " ORDER BY fae0102data1 DESC";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("fae01id", fae01id);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterRecrutamentosByIdCurriculo(Long fae01id){
		String sql = "SELECT fae0103cur AS fae01id, fae0103data AS data, fae0103obs AS obs, fae0103aval AS avaliacao, fae0103dtProxFase AS proxData, " +
					 Fields.concat("aap11fase.aap11codigo", "'-'", "aap11fase.aap11descr") + " AS fase, " +
					 Fields.concat("aap11proxfase.aap11codigo", "'-'", "aap11proxfase.aap11descr") + " AS proxFase " +
					 "FROM Fae0103 " +
					 "INNER JOIN Fae01 ON fae01id = fae0103cur " +
					 "INNER JOIN Aap11 aap11fase ON aap11fase.aap11id = fae0103fase " +
					 "LEFT JOIN Aap11 aap11proxfase ON aap11proxfase.aap11id = fae0103proxFase " +
					 "WHERE fae0103cur = :fae01id " + getSamWhere().getWherePadrao("AND", Fae01.class) +
					 " ORDER BY fae0103data DESC";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("fae01id", fae01id);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNSSCAtIExpc3RhZ2VtIGRlIEN1cnJpY3Vsb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=