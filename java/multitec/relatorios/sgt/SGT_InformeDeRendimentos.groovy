package multitec.relatorios.sgt

import java.time.format.DateTimeFormatter

import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource

class SGT_InformeDeRendimentos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SGT - Informe de Rendimentos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		def aac10 = obterEmpresaAtiva();
		return criarFiltros(
			"ano", MDate.date().getYear(),
			"ni", aac10.aac10ni,
			"ti", "0",
			"dtinforme", MDate.date()
		);
	}

	@Override
	public DadosParaDownload executar() {
		def ano = getInteger("ano");
		def ni = getString("ni");
		def ti = getInteger("ti");
		def dtinforme = getLocalDate("dtinforme").format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		def info = getString("info");
		def resp = getString("resp");
		
		adicionarParametro("ANO", ano);
		adicionarParametro("AAC10RS", obterEmpresaAtiva().aac10rs);
		adicionarParametro("AAC10NI", ni ?: obterEmpresaAtiva().aac10ni);
		adicionarParametro("DT_INFORME", dtinforme);
		adicionarParametro("INFORMACOES", info);
		adicionarParametro("RESPONSAVEL", resp)
		adicionarParametro("IMAGEM", "./imagens/receita.png");
		
		List<Long> gruposCentralizadores = new ArrayList<Long>();
		gruposCentralizadores.add(buscarGrupoCentralizadorPorEmpresaTabela(obterEmpresaAtiva().aac10id, "ED"));
		
		def idGC = buscarGrupoCentralizadorPorEmpresaTabela(obterEmpresaAtiva().aac10id, "ED");

		List<Aac10> filiais = buscarFiliaisEmpresaAtiva(obterEmpresaAtiva().aac10id);
		for(Aac10 emp : filiais){
			def gcEmpresa = buscarGrupoCentralizadorPorEmpresaTabela(emp.aac10id, "ED");
			if(!gruposCentralizadores.contains(gcEmpresa)) gruposCentralizadores.add(gcEmpresa);
		}
		
		def dados = ti == 0 ? gerarDadosIRPFisica(ni, ano, gruposCentralizadores) : gerarDadosIRPJuridica(ni, ano, gruposCentralizadores)
		def nome = ti == 0 ? "SGT_InformeDeRendimentosPF" : "SGT_InformeDeRendimentosPJ";
		
		return gerarPDF(nome, dados);
	}
	
	public TableMapDataSource gerarDadosIRPFisica(String ni, Integer ano, List<Long> gruposCentralizadores) {
		def mapPrincipal = new TableMap();
		
		def codigosIRFisica = Utils.list("301", "302", "303", "304", "305", "401", "402", "403", "404", "405", "406", "407", "501", "502");
		def codigosOutrosIRFisica = Utils.list("407");
		
		List<TableMap> edd40sEnt = buscarLctosIRPF(ni, ano, gruposCentralizadores, codigosIRFisica);
		for(edd40 in edd40sEnt) {
			def key = edd40.getString("abe01id") + edd40.getString("abe01ni") + edd40.getString("aaj52codigo");
			
			def tm = new TableMap();
			tm.put("ni", edd40.getString("abe01ni"));
			tm.put("nome", edd40.getString("abe01nome"));
			tm.put("codCR", edd40.getString("aaj52codigo"));
			tm.put("descrCR", edd40.getString("aaj52descr"));
			
			def cv = edd40.getString("aaj53codigo");
			def vl = edd40.getBigDecimal_Zero("edd4001valor");
			def mp = mapPrincipal.getTableMap(key) ?: new TableMap();
			
			tm.put("vlr301", mp.getBigDecimal_Zero("vlr301").add(cv.equals("301") ? vl : BigDecimal.ZERO));
			tm.put("vlr302", mp.getBigDecimal_Zero("vlr302").add(cv.equals("302") ? vl : BigDecimal.ZERO));
			tm.put("vlr303", mp.getBigDecimal_Zero("vlr303").add(cv.equals("303") ? vl : BigDecimal.ZERO));
			tm.put("vlr304", mp.getBigDecimal_Zero("vlr304").add(cv.equals("304") ? vl : BigDecimal.ZERO));
			tm.put("vlr305", mp.getBigDecimal_Zero("vlr305").add(cv.equals("305") ? vl : BigDecimal.ZERO));
			tm.put("vlr401", mp.getBigDecimal_Zero("vlr401").add(cv.equals("401") ? vl : BigDecimal.ZERO));
			tm.put("vlr402", mp.getBigDecimal_Zero("vlr402").add(cv.equals("402") ? vl : BigDecimal.ZERO));
			tm.put("vlr403", mp.getBigDecimal_Zero("vlr403").add(cv.equals("403") ? vl : BigDecimal.ZERO));
			tm.put("vlr404", mp.getBigDecimal_Zero("vlr404").add(cv.equals("404") ? vl : BigDecimal.ZERO));
			tm.put("vlr405", mp.getBigDecimal_Zero("vlr405").add(cv.equals("405") ? vl : BigDecimal.ZERO));
			tm.put("vlr406", mp.getBigDecimal_Zero("vlr406").add(cv.equals("406") ? vl : BigDecimal.ZERO));
			tm.put("vlr407", mp.getBigDecimal_Zero("vlr407").add(cv.equals("407") ? vl : BigDecimal.ZERO));
			tm.put("vlr501", mp.getBigDecimal_Zero("vlr501").add(cv.equals("501") ? vl : BigDecimal.ZERO));
			tm.put("vlr502", mp.getBigDecimal_Zero("vlr502").add(cv.equals("502") ? vl : BigDecimal.ZERO));
			tm.put("vlr503", mp.getBigDecimal_Zero("vlr503").add(cv.equals("503") ? vl : BigDecimal.ZERO));
			
			mapPrincipal.put(key, tm);
		}
		
		/** Trabalhadores */
		List<TableMap> edd40sTrab = buscarLctosIRPF(ni, ano, gruposCentralizadores, codigosIRFisica);
		for(edd40 in edd40sTrab) {
			
			break; // TODO REMOVER QUANDO TIVER A PARTE DE TRABALHADORES CRIADA
			
			def key = edd40.getString("abh80nome") + edd40.getString("abh80cpf") + edd40.getString("aaj52codigo");
			
			def tm = new TableMap();
			tm.put("nome", edd40.getString("abh80nome"));
			tm.put("ni", edd40.getString("abh80cpf"));
			tm.put("codCR", edd40.getString("codCR"));
			tm.put("descrCR", edd40.getString("descrCR"));
			
			def cv = edd40.getString("aaj53codigo");
			def vl = edd40.getBigDecimal_Zero("edd4001valor");
			def mp = mapPrincipal.getTableMap(key) ?: new TableMap();
			
			tm.put("vlr301", mp.getBigDecimal_Zero("vlr301").add(cv.equals("301") ? vl : BigDecimal.ZERO));
			tm.put("vlr302", mp.getBigDecimal_Zero("vlr302").add(cv.equals("302") ? vl : BigDecimal.ZERO));
			tm.put("vlr303", mp.getBigDecimal_Zero("vlr303").add(cv.equals("303") ? vl : BigDecimal.ZERO));
			tm.put("vlr304", mp.getBigDecimal_Zero("vlr304").add(cv.equals("304") ? vl : BigDecimal.ZERO));
			tm.put("vlr305", mp.getBigDecimal_Zero("vlr305").add(cv.equals("305") ? vl : BigDecimal.ZERO));
			tm.put("vlr401", mp.getBigDecimal_Zero("vlr401").add(cv.equals("401") ? vl : BigDecimal.ZERO));
			tm.put("vlr402", mp.getBigDecimal_Zero("vlr402").add(cv.equals("402") ? vl : BigDecimal.ZERO));
			tm.put("vlr403", mp.getBigDecimal_Zero("vlr403").add(cv.equals("403") ? vl : BigDecimal.ZERO));
			tm.put("vlr404", mp.getBigDecimal_Zero("vlr404").add(cv.equals("404") ? vl : BigDecimal.ZERO));
			tm.put("vlr405", mp.getBigDecimal_Zero("vlr405").add(cv.equals("405") ? vl : BigDecimal.ZERO));
			tm.put("vlr406", mp.getBigDecimal_Zero("vlr406").add(cv.equals("406") ? vl : BigDecimal.ZERO));
			tm.put("vlr407", mp.getBigDecimal_Zero("vlr407").add(cv.equals("407") ? vl : BigDecimal.ZERO));
			tm.put("vlr501", mp.getBigDecimal_Zero("vlr501").add(cv.equals("501") ? vl : BigDecimal.ZERO));
			tm.put("vlr502", mp.getBigDecimal_Zero("vlr502").add(cv.equals("502") ? vl : BigDecimal.ZERO));
			tm.put("vlr503", mp.getBigDecimal_Zero("vlr503").add(cv.equals("503") ? vl : BigDecimal.ZERO));
			
			mapPrincipal.put(key, tm);
		}
		
		List<TableMap> mapRel = new ArrayList();
		
		TreeSet<String> setOrdenado = new TreeSet<Object>();
		setOrdenado.addAll(mapPrincipal.keySet());
		
		for(key in setOrdenado){
			def tm = new TableMap();
			tm.put("chave", key);
			tm.put("nome", mapPrincipal.getTableMap(key).getString("nome"));
			tm.put("ni", mapPrincipal.getTableMap(key).getString("ni"));
			tm.put("codCR", mapPrincipal.getTableMap(key).getString("codCR"));
			tm.put("descrCR", mapPrincipal.getTableMap(key).getString("descrCR"));
			tm.put("vlr301", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr301"));
			tm.put("vlr302", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr302"));
			tm.put("vlr303", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr303"));
			tm.put("vlr304", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr304"));
			tm.put("vlr305", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr305"));
			tm.put("vlr401", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr401"));
			tm.put("vlr402", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr402"));
			tm.put("vlr403", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr403"));
			tm.put("vlr404", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr404"));
			tm.put("vlr405", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr405"));
			tm.put("vlr406", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr406"));
			tm.put("vlr407", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr407"));
			tm.put("vlr501", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr501"));
			tm.put("vlr502", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr502"));
			tm.put("vlr503", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr503"));
			mapRel.add(tm);
		}
		
		def dsPrincipal = new TableMapDataSource(mapRel);
		
		def dadosSub = buscarLctosOutrosIRPF(ni, ano, codigosOutrosIRFisica);
		dsPrincipal.addSubDataSource("SubDataSource", dadosSub, "chave", "chave");
		adicionarParametro("StreamSub", carregarArquivoRelatorio("SGT_InformeDeRendimentosPF_Sub"))
		
		return dsPrincipal;
	}
	
	public TableMapDataSource gerarDadosIRPJuridica(String ni, Integer ano, List<Long> gruposCentralizadores) {
		def mapPrincipal = new TableMap();
		
		def codigosIRJuridica = Utils.list("901", "902");
		
		List<TableMap> edd40s = buscarLctosIRPJ(ni, ano, gruposCentralizadores, codigosIRJuridica);
		for(edd40 in edd40s) {
			def key = edd40.getString("abe01nome") + edd40.getString("abe01ni") + String.format("%02d", edd40.getInteger("mes")) + edd40.getString("codCR");
			
			def tm = new TableMap();
			tm.put("abe01ni", edd40.getString("abe01ni"));
			tm.put("abe01nome", edd40.getString("abe01nome"));
			tm.put("codCR", edd40.getString("aaj52codigo"));
			tm.put("descrCR", edd40.getString("aaj52descr"));
			tm.put("mes", edd40.getInteger("mes"));
			
			mapPrincipal.put(key, tm);
			
			switch(edd40.getString("aaj53codigo")) {
				case "901":  tm.put("vlr1", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr1").add(edd40.getBigDecimal_Zero("edd4001valor"))); break;
				case "902":  tm.put("vlr2", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr2").add(edd40.getBigDecimal_Zero("edd4001valor"))); break;
			}
			
			mapPrincipal.put(key, tm);
		}
		
		List<TableMap> mapRel = new ArrayList();
		
		TreeSet<String> setOrdenado = new TreeSet<String>();
		setOrdenado.addAll(mapPrincipal.keySet());
		
		for(key in setOrdenado){
			def tm = new TableMap();
			tm.put("chave", key);
			tm.put("abe01nome", mapPrincipal.getTableMap(key).getString("abe01nome"));
			tm.put("abe01ni", mapPrincipal.getTableMap(key).getString("abe01ni"));
			tm.put("aaj52codigo", mapPrincipal.getTableMap(key).getString("codCR"));
			tm.put("aaj52descr", mapPrincipal.getTableMap(key).getString("descrCR"));
			tm.put("mes", mapPrincipal.getTableMap(key).getInteger("mes"));
			tm.put("vlr1", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr1"));
			tm.put("vlr2", mapPrincipal.getTableMap(key).getBigDecimal_Zero("vlr2"));
			mapRel.add(tm);
		}
		
		return new TableMapDataSource(mapRel);
	}
	
	private List<Aac10> buscarFiliaisEmpresaAtiva(Long aac10id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
			" SELECT * FROM Aac10 WHERE aac10matriz = :aac10id ", 
			criarParametroSql("aac10id", aac10id)
		);
	}
	
	Long buscarGrupoCentralizadorPorEmpresaTabela(Long aac10id, String tabela) {
		def sql = " SELECT aac01id FROM Aac01 WHERE aac01id IN " +
				  "(SELECT aac1001gc FROM Aac1001 WHERE aac1001empresa = :aac10id AND UPPER(aac1001tabela) = :tabela) ";

		return getAcessoAoBanco().obterLong(sql, criarParametroSql("aac10id", aac10id), criarParametroSql("tabela", tabela));
	}
	
	public List<TableMap> buscarLctosIRPF(String ni, Integer ano, List<Long> gcs, List<String> codigosIREntidades) {
		def where = StringUtils.isNullOrEmpty(ni) ? "" : " AND abe01ni = :ni ";
		
		def sql = " SELECT abe01id, abe01nome, abe01ni, aaj52codigo, aaj52descr, aaj53codigo, aaj53descr, edd4001valor, aaj53irPf " +
				  " FROM Edd40 " +
				  " INNER JOIN Abb01 ON abb01id = edd40central " +
				  " INNER JOIN Abe01 ON abe01id = abb01ent " +
				  " INNER JOIN Aaj52 ON aaj52id = edd40cr " +
				  " INNER JOIN Edd4001 ON edd4001lct = edd40id " +
				  " INNER JOIN Aaj53 ON aaj53id = edd4001cv " +
				  " WHERE abe01ti = :ti " + where + 
				  " AND " + Fields.year("edd40data") + " = :ano " +
				  " AND edd40gc IN (:gcs) " +
				  " AND aaj53codigo IN (:codigos) " +
				  " ORDER BY abe01nome, aaj52codigo";
		
		def p1 = ni != null ? criarParametroSql("ni", ni) : null;
		def p2 = criarParametroSql("ano", ano);
		def p3 = criarParametroSql("ti", 1);
		def p4 = criarParametroSql("codigos", codigosIREntidades);
		def p5 = criarParametroSql("gcs", gcs);
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4, p5);
	}
	
	public List<TableMap> buscarLctosOutrosIRPF(String ni, Integer ano, List<String> codigosOutrosIRFisica) {
		def where = StringUtils.isNullOrEmpty(ni) ? "" : " AND abe01ni = :ni ";
		
		def sql = " SELECT CONCAT(abe01id, abe01ni, aaj52codigo) as chave, aaj53descr as descrCL, SUM(edd4001valor) as valor " +
				  " FROM Edd40 " +
				  " INNER JOIN Abb01 ON abb01id = edd40central " +
				  " INNER JOIN Abe01 ON abe01id = abb01ent " +
				  " INNER JOIN Aaj52 ON aaj52id = edd40cr " +
				  " INNER JOIN Edd4001 ON edd4001lct = edd40id " +
				  " INNER JOIN Aaj53 ON aaj53id = edd4001cv " +
				  " WHERE abe01ti = :ti " +
				  " AND " + Fields.year("edd40data") + " = :ano " +
				  " AND aaj53codigo IN (:outros) " +
				  	where + obterWherePadrao("Edd40") +
				  " GROUP BY CONCAT(abe01id, abe01ni, aaj52codigo), aaj53descr " +
				  " ORDER BY CONCAT(abe01id, abe01ni, aaj52codigo), aaj53descr";
		
		def p1 = ni != null ? criarParametroSql("ni", ni) : null;
		def p2 = criarParametroSql("ano", ano);
		def p3 = criarParametroSql("ti", 1);
		def p4 = criarParametroSql("outros", codigosOutrosIRFisica);
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4);
	}
	
	public List<TableMap> buscarLctosIRPJ(String ni, Integer ano, List<Long> gcs, List<String> codigosIRJuridica) {
		def where = StringUtils.isNullOrEmpty(ni) ? "" : " AND abe01ni = :ni ";
		
		def sql = " SELECT abe01codigo, abe01nome, abe01ni, aaj52codigo, aaj52descr, aaj53codigo, aaj53descr, " + Fields.month("edd40data") + " as mes, edd4001valor " +
				  " FROM Edd40 "+
				  " INNER JOIN Abb01 ON abb01id = edd40central " +
				  " INNER JOIN Abe01 ON abe01id = abb01ent " +
				  " INNER JOIN Aaj52 ON aaj52id = edd40cr " +
				  " INNER JOIN Edd4001 ON edd4001lct = edd40id " +
				  " INNER JOIN Aaj53 ON aaj53id = edd4001cv " +
				  " WHERE edd40gc IN (:gcs) " +
				  " AND abe01ti = :ti " +
				  " AND aaj53codigo IN (:codigos) " +
				  " AND " + Fields.year("edd40data") + " = :ano " +
				    where + obterWherePadrao("Edd40") +
				  " ORDER BY abe01nome, " + Fields.month("edd40data") +", aaj52codigo";
		
		def p1 = ni != null ? criarParametroSql("ni", ni) : null;
		def p2 = criarParametroSql("ano", ano);
		def p3 = criarParametroSql("ti", 0);
		def p4 = criarParametroSql("gcs", gcs);
		def p5 = criarParametroSql("codigos", codigosIRJuridica);
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4, p5);
	}
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIEluZm9ybWUgZGUgUmVuZGltZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=