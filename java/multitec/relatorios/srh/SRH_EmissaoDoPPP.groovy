package multitec.relatorios.srh;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.fa.Fab01;
import sam.model.entities.fa.Fab02;
import sam.model.entities.fa.Fab03;
import sam.model.entities.fa.Fab10;
import sam.model.entities.fa.Fab11;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

public class SRH_EmissaoDoPPP extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SRH - Emissão do PPP";
	}
	
	@Override
	public DadosParaDownload executar() {
		List<TableMap> cmb = new ArrayList<>();
		TableMap combo = new TableMap();
		combo.put("0", "NA - Não Aplicável");
		combo.put("1", "BR - Beneficiário Reabilitado");
		combo.put("2", "PDH - Portador de Deficiência Habilitado");
		cmb.add(combo);
		
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsDeptos = getListLong("departamentos");
		
		LocalDate data = getLocalDate("data");
		Integer cmbBRPDH = getInteger("cmbBRPDH");
		
		//Dados do representante
		String rNome = getString("rNome");
		String rCpf = getString("rCpf");
		String rCnpj = getString("rCnpj");
		String rEndereco = getString("rEndereco");
		String rBairro = getString("rBairro");
		String rMunicipio = getString("rMunicipio");
		String rCep = getString("rCep");
		
		String observacoes = getString("observacoes");
		
		Set<Integer> tiposAbh80 = obterTipoTrabalhador();
		
		params.put("EMP_RS", getVariaveis().getAac10().getAac10rs());
		params.put("EMP_NI", getVariaveis().getAac10().getAac10ni());
		params.put("EMP_CNAE", getVariaveis().getAac10().getAac10cnae());
		
		params.put("DATA", DateUtils.formatDate(data));
		params.put("OBS", observacoes);
		params.put("BR/PDH", cmb.get(0).get(cmbBRPDH.toString()).toString().substring(0, 3).trim());
		params.put("RNOME", rNome);
		params.put("RCPF", rCpf);
		params.put("RCNPJ", rCnpj);
		params.put("RENDERECO", rEndereco);
		params.put("RBAIRRO", rBairro);
		params.put("RCIDADE", rMunicipio);
		params.put("RCEP", rCep);
		params.put("StreamSub1R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S1"));
		params.put("StreamSub2R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S2"));
		params.put("StreamSub3R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S3"));
		params.put("StreamSub4R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S4"));
		params.put("StreamSub5R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S5"));
		params.put("StreamSub6R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S6"));
		params.put("StreamSub7R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S7"));
		params.put("StreamSub8R1", carregarArquivoRelatorio("SRH_EmissaoDoPPP_S8"));
		
		
		List<TableMap> listfab02 = new ArrayList<>();
		List<TableMap> listFab01 = new ArrayList<>();
		List<TableMap> listAmbBio = new ArrayList<>();
		List<TableMap> listFab0302 = new ArrayList<>();
		List<TableMap> listFab10  = new ArrayList<>();
		List<TableMap> listFab1101 = new ArrayList<>();
		List<TableMap> sub8 = new ArrayList<>();
		
		
		List<TableMap> trabalhadores = obterTrabalhadores(idsTrabalhadores, idsCargos, idsDeptos, tiposAbh80);
		if(trabalhadores != null && trabalhadores.size() > 0) {
			for(TableMap abh80 : trabalhadores) {
				String sexo = abh80.getInteger("abh80sexo") == 0 ? "M" : "F";
				String ctps = StringUtils.concat(abh80.getString("abh80ctpsNum"), " ", abh80.getString("abh80ctpsSerie"), " ", abh80.getString("abh80ctpsEe"));
						
				abh80.put("sexo", sexo);
				abh80.put("ctps", ctps);
				
				String revez = abh80.getString("abh80jorDescr") != null ? abh80.getString("abh80jorDescr").substring(0, 14) : "";
				abh80.put("revez", revez);
				
				//Registros de CAT - SUB 1
				listfab02.addAll(obterDadosFab02Cat(abh80.getLong("abh80id")));
			
				//Movimentação funcional - SUB 2 e SUB 3
				listFab01.addAll(obterDadosFab01MovimentoFuncional(abh80.getLong("abh80id")));
				if(listFab01 != null && listFab01.size() > 0) {
					for(TableMap fab01 : listFab01) {
						String periodo = StringUtils.concat(DateUtils.formatDate(fab01.getDate("fab01dtI")), " - ", (fab01.getDate("fab01dtF") != null ? DateUtils.formatDate(fab01.getDate("fab01dtF")) : ""));
						fab01.put("periodo", periodo);
					}
				}
				
				//Exames médicos clínicos - SUB 6
				listFab10.addAll(obterExamesMedicos(abh80.getLong("abh80id")));
				if(listFab10 != null && listFab10.size() > 0) {
					for(TableMap fab10 : listFab10) {
						String tipo = null;
						switch (fab10.getInteger("fab10tpExOcup")) {
							case 0: tipo = "Admissional";	  
								break;
							case 1: tipo = "Periódico";	 	  
								break;
							case 2: tipo = "Base de CálculoRetorno"; 
								break;
							case 3: tipo = "Mudança de função"; 		  
								break;
							case 4: tipo = "Monitoração Pontual";		  
								break;
							case 9: tipo = "Demissional";
								break;
						}
						fab10.put("tipo", tipo);
						
						String exame = fab10.getInteger("fab1001result") != null && fab10.getInteger("fab1001result") == 1 ? "Normal" : "";
						fab10.put("exame", exame);
						
						String indResult = null;
						switch (fab10.getInteger("fab1001result")) {
							case 1: indResult = "";	  
								break;
							case 2: indResult = "Alterado";	 	  
								break;
							case 3: indResult = "Estável"; 
								break;
							case 4: indResult = "Agravamento"; 		  
								break;
						}
						fab10.put("resultado", indResult);
					}
				}
				
				//Responsáveis pelos registros biológios - SUB 7
				listFab1101.addAll(obterDadosRespRegistrosBiologicos(abh80.getLong("abh80id")));
				
				//SUB 8
				if(listAmbBio != null && listAmbBio.size() > 0) {
					String protecao = listAmbBio.get(0).getInteger("fab030112protCol") == 0 ? "N" : "S";
					String usoepi = listAmbBio.get(0).getInteger("fab030112funcUso") == 0 ? "N" : "S";
					String validade = listAmbBio.get(0).getInteger("fab030112valid") == 0 ? "N" : "S";
					String troca = listAmbBio.get(0).getInteger("fab030112troca") == 0 ? "N" : "S";
					String higiene = listAmbBio.get(0).getInteger("fab030112higiene") == 0 ? "N" : "S";
					
					TableMap sub = new TableMap();
					sub.put("protecao", protecao);
					sub.put("usoepi", usoepi);
					sub.put("validade", validade);
					sub.put("troca", troca);
					sub.put("higiene", higiene);
					
					sub8.add(sub);
				}
			}
		}
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(trabalhadores);
		dsPrincipal.addSubDataSource("DsSub1R1", listfab02, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DsSub2R1", listFab01, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DsSub3R1", listFab01, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DsSub4R1", listAmbBio, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DsSub5R1", listFab0302, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DsSub6R1", listFab10, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DsSub7R1", listFab1101, "abh80id", "abh80id");
		dsPrincipal.addSubDataSource("DsSub8R1", sub8, "abh80id", "abh80id");
		
		return gerarPDF("SRH_EmissaoDoPPP", dsPrincipal);
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		
		filtrosDefault.put("data", MDate.date());
		
		configurarDadosResponsavel(filtrosDefault);
		
		filtrosDefault.put("cmbBRPDH", 0);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	private void configurarDadosResponsavel(Map<String, Object> filtrosDefault) {
		Aac10 aac10 = getVariaveis().getAac10();
		filtrosDefault.put("rNome", aac10.getAac10rNome());
		filtrosDefault.put("rCpf", aac10.getAac10rCpf());
		filtrosDefault.put("rCnpj", aac10.getAac10rCnpj());
		filtrosDefault.put("rEndereco", aac10.getAac10rEndereco());
		filtrosDefault.put("rBairro", aac10.getAac10rBairro());
		
		Aag0201 aag0201 = aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getAag0201id()) : null;
		filtrosDefault.put("rMunicipio", aag0201 != null ? aag0201.getAag0201nome() : "");
		filtrosDefault.put("rCep", aac10.getAac10rCep());
	}
	
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
	
//	private String obterCNAE(Long aac10id) {
//		return getSession().createCriteria(Aac12.class)
//				.addFields("aac12cnae")
//				.addWhere(Criterions.eq("aac12empresa", aac10id))
//				.get(ColumnType.STRING);
//	}
	
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
	
	private List<TableMap> obterTrabalhadores(List<Long> idsTrabalhadores, List<Long> idsCargos, List<Long> idsDeptos, Set<Integer> tiposAbh80){
		
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abh80depto IN (:idsDeptos) " : "";
		
		String sql = "SELECT abh80id, abh80nome, abh80pis, abh80nascData, abh80sexo, abh80ctpsNum, abh80ctpsSerie, abh80ctpsEe, abh80dtAdmis, abh80jorDescr " +
				 	 "FROM Fab01 " +
				 	 "INNER JOIN Abh80 ON abh80id = fab01trab " +
				 	 "INNER JOIN Abb11 ON abb11id = abh80depto " +
				 	 "INNER JOIN Abh05 ON abh05id = fab01cargo " +
				 	 "WHERE abh80tipo IN (:tiposAbh80) " +
				 	 whereTrabalhadores + whereCargos +  whereDeptos + 
				 	 getSamWhere().getWherePadrao("AND", Fab01.class) +
				 	 "GROUP BY abh80id, abh80codigo, abh80nome, abh80pis, abh80nascData, abh80sexo, abh80ctpsNum, abh80ctpsSerie, abh80ctpsEe, abh80dtAdmis " +
				 	 "ORDER BY abh80codigo";
	
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterDadosFab02Cat(Long abh80id){
		Query query = getSession().createQuery("SELECT fab02trab AS abh80id, fab02data, fab02num ",
											   "FROM Fab02 ",
											   "WHERE fab02trab = :abh80id ", getSamWhere().getWherePadrao("AND", Fab02.class),
											   " ORDER BY fab02data")
				.setParameter("abh80id", abh80id);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterDadosFab01MovimentoFuncional(Long abh80id){
		String sql = "SELECT abh80id, abh02ni AS cnpj, fab01obs, abb11nome AS setor, abh05nome AS cargo, abh05funcao AS funcao, abh05tarefa AS tarefa, "+
					 "aap03cbo.aap03codigo AS cbo, aap04oco.aap04codigo AS gfip, fab01dtI, fab01dtF " +
				 	 "FROM Fab01 " +
				 	 "INNER JOIN Abh80 ON abh80id = fab01trab " +
				 	 "INNER JOIN Abb11 ON abb11id = abh80depto " +
				 	 "INNER JOIN Abh05 ON abh05id = fab01cargo " +
				 	 "INNER JOIN Abh02 ON abh02id = fab01lotacao " +
				 	 "LEFT JOIN Aap03 aap03cbo ON aap03cbo.aap03id = abh05cbo " +
				 	 "LEFT JOIN Aap04 aap04oco ON aap04oco.aap04id = fab01gfip " +
				 	 "WHERE fab01trab = :abh80id " +
				 	 getSamWhere().getWherePadrao("AND", Fab01.class) + 
				 	 " ORDER BY fab01dtI";
	
		Query query = getSession().createQuery(sql);
		query.setParameter("abh80id", abh80id);
		
		return query.getListTableMap();
	}
	
//	private List<TableMap> obterDadosRegistrosAmbientaisBiologicosEPC(Long abh80id){
//		String sql = "SELECT fab03trab AS abh80id, fab03011tipo, fab03011fr, aap02descr AS fator, fab03011intConc AS intensidade, fab03011tecMed AS tecnica, aap01caEpi AS caepi, " +
//					 "fab03dtI, fab03dtF " +
//			     	 "FROM Fab030111 " +
//			     	 "INNER JOIN Fab03011 ON fab03011id = fab030111fr "+
//			     	 "INNER JOIN Fab0301 ON fab0301id = fab03011at "+
//			     	 "INNER JOIN Fab03 ON fab03id = fab0301regAmb "+
//			     	 "INNER JOIN Aap01 ON aap01id = fab030111ep " +
//			     	 "INNER JOIN Aap02 ON aap02id = fab03011fr "+
//			     	 "WHERE fab03trab = :abh80id " +
//			     	 getSamWhere().getWherePadrao("AND", Fab03.class) +
//			     	 "ORDER BY fab03dtI";
//		
//		Query query = getSession().createQuery(sql);
//		query.setParameter("abh80id", abh80id);
//		
//		return query.getListTableMap();
//	}
	
	private List<TableMap> obterDadosRegistrosAmbientaisBiologicosEPI(Long abh80id){
		String sql = "SELECT fab03trab AS abh80id, fab03011tipo, fab03011fr, aap02descr AS fator, fab03011intConc AS intensidade, fab03011tecMed AS tecnica, aap01caEpi AS caepi, " +
					 "fab030112protCol, fab030112valid, fab030112troca, fab030112higiene, fab030112funcUso, fab03dtI, fab03dtF " +
					 "FROM Fab030112 " +
			     	 "INNER JOIN Fab03011 ON fab03011id = fab030112fr "+
			     	 "INNER JOIN Fab0301 ON fab0301id = fab03011at "+
			     	 "INNER JOIN Fab03 ON fab03id = fab0301regAmb "+
			     	 "INNER JOIN Aap01 ON aap01id = fab030112ep " +
			     	 "INNER JOIN Aap02 ON aap02id = fab03011fr "+
			     	 "WHERE fab03trab = :abh80id " +
			     	 getSamWhere().getWherePadrao("AND", Fab03.class) +
			     	 "ORDER BY fab03dtI";
		
		Query query = getSession().createQuery(sql);
		query.setParameter("abh80id", abh80id);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterDadosRespRegistrosBiologicos(Long abh80id){
		String sql = "SELECT fab11trab AS abh80id, fab1101pis AS nit, fab1101ni AS registro, fab1101nome AS nomeResp, " +
				 	 Fields.concat("fab1101dtI", "'-'", "fab1101dtF") + " AS periodo " +
			     	 "FROM Fab1101 " +
			     	 "INNER JOIN Fab11 ON fab11id = fab1101monit " +
			     	 "INNER JOIN Abh80 ON abh80id = fab11trab " +
			     	 "WHERE fab11trab = :abh80id  " +
			     	 getSamWhere().getWherePadrao("AND", Fab11.class) +
			     	 " ORDER BY fab1101dtI";
	
		Query query = getSession().createQuery(sql);
		query.setParameter("abh80id", abh80id);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterDadosRespRegistrosAmbientais(Long abh80id){
		String sql = "SELECT fab03trab AS abh80id, fab0302pis AS nit, fab0302ocNi AS registro, fab0302nome AS nomeResp, " +
					 "fab03dtI, fab03dtF " +
		     	 	 "FROM Fab0302 " +
		     	 	 "INNER JOIN Fab03 ON fab03id = fab0302regAmb " +
		     	 	 "INNER JOIN Abh80 ON abh80id = fab03trab " +
		     	 	 "WHERE fab03trab = :abh80id  " +
		     	 	 getSamWhere().getWherePadrao("AND", Fab03.class) +
		     	 	 " ORDER BY fab03dtI";

		Query query = getSession().createQuery(sql);
		query.setParameter("abh80id", abh80id);
		
		return query.getListTableMap();
	}
	
	private List<TableMap> obterExamesMedicos(Long abh80id){
		String sql = "SELECT fab10trab AS abh80id, fab1001data AS data, fab10tpExOcup, aap60descr AS natureza, fab1001ordem, fab1001result " +
			     	 "FROM Fab1001 " +
			     	 "INNER JOIN Fab10 ON fab10id = fab1001rst " +
			     	 "INNER JOIN Aap60 ON aap60id = fab1001proc " +
			     	 "WHERE fab10trab = :abh80id " +
			     	 getSamWhere().getWherePadrao("AND", Fab10.class) +
			     	 "ORDER BY fab1001data";
	
		Query query = getSession().createQuery(sql);
		query.setParameter("abh80id", abh80id);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNSSCAtIEVtaXNzw6NvIGRvIFBQUCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==