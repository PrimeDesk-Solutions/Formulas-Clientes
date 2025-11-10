package multitec.relatorios.sgt

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SGT_DARF extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SGT - DARF";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros(
			"vencimento", MDate.date(),
			"emissao", "0",
			"dtLctos", DateUtils.getStartAndEndMonth(MDate.date())
		);
	}

	@Override
	public DadosParaDownload executar() {
		def codsir = getListLong("codsir");
		def vencto = getLocalDate("vencimento");
		def emissao = getInteger("emissao");
		def dtLctos = getIntervaloDatas("dtLctos");
		def ni = getString("ni");
		
		def aac10 = obterEmpresaAtiva();
		adicionarParametro("AAC10RS", aac10.aac10rs);
		adicionarParametro("AAC10NI", aac10.aac10ni);
		adicionarParametro("AAC10FONE", aac10.aac10dddFone + " - " + aac10.aac10fone);
		adicionarParametro("DT_APURACAO", dtLctos[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		adicionarParametro("DT_VENCIMENTO", vencto.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		adicionarParametro("IMAGEM", "./imagens/receita.png");
		
		def dados = buscarDadosRelatorio(ni, codsir, dtLctos[0], dtLctos[1], emissao)
		
		return gerarPDF(dados);
	}
	
	public List<TableMap> buscarDadosRelatorio(String ni, List<Long> codsir, LocalDate dtInicial, LocalDate dtFinal, Integer emitirPor) {
		List<Long> gruposCentralizadores = new ArrayList<Long>();
		gruposCentralizadores.add(buscarGrupoCentralizadorPorEmpresaTabela(obterEmpresaAtiva().aac10id, "ED"));
		
		def idGC = buscarGrupoCentralizadorPorEmpresaTabela(obterEmpresaAtiva().aac10id, "ED");

		List<Aac10> filiais = buscarFiliaisEmpresaAtiva(obterEmpresaAtiva().aac10id);
		for(Aac10 emp : filiais){
			def gcEmpresa = buscarGrupoCentralizadorPorEmpresaTabela(emp.aac10id, "ED");
			if(!gruposCentralizadores.contains(gcEmpresa)) gruposCentralizadores.add(gcEmpresa);
		}
		
		def mapPrincipal = new TableMap();
		
		/**
		 * Entidades
		 */
		def edd40sEnt = buscarLctosParaDARF(ni, codsir, dtInicial, dtFinal, gruposCentralizadores);
		if(edd40sEnt != null && edd40sEnt.size() > 0) {
			for(edd40 in edd40sEnt) {
				def key = "";
				if(emitirPor == 0) { //0-Nome
					key = edd40.getString("abe01ni") + "/" + edd40.getString("aaj52codigo");
				}else if(emitirPor == 1) { //1-Documento
					key = edd40.getString("abe01ni") + "/" + edd40.getString("aah01codigo") + "/" + edd40.getInteger("abb01num") + "/" + edd40.getString("aaj52codigo");
				}else { 			//2-Retenção
					key = edd40.getString("aaj52codigo");
				}
				
				def tm = new TableMap();
				tm.put("nome", edd40.getString("abe01nome"));
				tm.put("numero", edd40.getInteger("abb01num"));
				tm.put("codCR", edd40.getString("aaj52codigo"));
				
				def vlr = mapPrincipal.getTableMap(key) == null ? BigDecimal.ZERO : mapPrincipal.getTableMap(key).getBigDecimal_Zero("edd4001valor");
				tm.put("valor", vlr.add(edd40.getBigDecimal_Zero("edd4001valor")));
				
				mapPrincipal.put(key, tm);
			}
		}
		
		/**
		 * Trabalhadores
		 */
		def edd40sTrab = buscarLctosParaDARF(ni, codsir, dtInicial, dtFinal, gruposCentralizadores);
		if(edd40sTrab != null && edd40sTrab.size() > 0) {
			for(edd40 in edd40sTrab) {
				
				break; // TODO REMOVER QUANDO TIVER A PARTE DE TRABALHADORES CRIADA
				
				String key = "";
				if(emitirPor == 0 || emitirPor == 1) { //0-Nome ou 1-Documento
					key = edd40.getString("abh80cpf") + "/" + edd40.getString("aaj52codigo");
				}else { 			//2-Retenção
					key = edd40.getString("aaj52codigo");
				}
				
				def tm = new TableMap();
				tm.put("nome", edd40.getString("abh80nome"));
				tm.put("numero", 0);
				tm.put("codCR", edd40.getString("aaj52codigo"));
				
				def vlr = mapPrincipal.getTableMap(key) == null ? BigDecimal.ZERO : mapPrincipal.getTableMap(key).getBigDecimal_Zero("edd4001valor"); 
				tm.put("valor", vlr.add(edd40.getBigDecimal_Zero("edd4001valor")));
				
				mapPrincipal.put(key, tm);
			}
		}
		
		//MapRel contém os dados ordenados para o relatório
		List<TableMap> mapRel = new ArrayList();
		
		TreeSet<String> setOrdenado = new TreeSet<String>();
		setOrdenado.addAll(mapPrincipal.keySet());
		
		for(key in setOrdenado){
			def tm = new TableMap();
			tm.put("nome", mapPrincipal.getTableMap(key).getString("nome"));
			tm.put("numero", mapPrincipal.getTableMap(key).getString("numero"));
			tm.put("codCR", mapPrincipal.getTableMap(key).getString("codCR"));
			tm.put("valor", mapPrincipal.getTableMap(key).getBigDecimal_Zero("valor"));
			tm.put("emissao", emitirPor);
			
			mapRel.add(tm);
		}
		
		return mapRel;
	}
	
	Long buscarGrupoCentralizadorPorEmpresaTabela(Long aac10id, String tabela) {
		def sql = " SELECT aac01id FROM Aac01 WHERE aac01id IN " +
				  "(SELECT aac1001gc FROM Aac1001 WHERE aac1001empresa = :aac10id AND UPPER(aac1001tabela) = :tabela) ";

		return getAcessoAoBanco().obterLong(sql, criarParametroSql("aac10id", aac10id), criarParametroSql("tabela", tabela));
	}
	
	private List<Aac10> buscarFiliaisEmpresaAtiva(Long aac10id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
			" SELECT * FROM Aac10 WHERE aac10matriz = :aac10id ", 
			criarParametroSql("aac10id", aac10id)
		);
	}
	
	public List<TableMap> buscarLctosParaDARF(String ni, List<Long> codsir, LocalDate dtInicial, LocalDate dtFinal, List<Long> gruposCentralizadores) {
		def whereNi = StringUtils.isNullOrEmpty(ni) ? "" : " AND abe01ni = :ni ";
		def whereCod = codsir != null && codsir.size() > 0 ? " AND aaj52id IN (:codsir) " : "";
		
		def sql = " SELECT abe01nome, abe01ni, aaj52codigo, aaj52descr, aaj53codigo, aaj53descr, abb01num, aah01codigo, edd4001valor, aaj53darf " +
				  " FROM Edd40 " +
				  " INNER JOIN Abb01 ON abb01id = edd40central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Abe01 ON abe01id = abb01ent " +
				  " INNER JOIN Aaj52 ON aaj52id = edd40cr " +
				  " INNER JOIN Edd4001 ON edd4001lct = edd40id " +
				  " INNER JOIN Aaj53 ON aaj53id = edd4001cv " +
				  " WHERE edd40gc IN (:gcs) " +
				  " AND edd40data BETWEEN :dtInicial AND :dtFinal " +
				  " AND aaj53darf = 1 " + whereNi + whereCod +
				  " ORDER BY abe01nome, abe01ni, aah01codigo, abb01num";
		
		def p1 = StringUtils.isNullOrEmpty(ni) ? null : criarParametroSql("ni", ni);
		def p2 = criarParametroSql("dtInicial", dtInicial);
		def p3 = criarParametroSql("dtFinal", dtFinal);
		def p4 = criarParametroSql("gcs", gruposCentralizadores);
		def p5 = codsir != null && codsir.size() > 0 ? criarParametroSql("codsir", codsir) : null;
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4, p5);
	}
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIERBUkYiLCJ0aXBvIjoicmVsYXRvcmlvIn0=