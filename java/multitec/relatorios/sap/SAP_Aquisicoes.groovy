package multitec.relatorios.sap;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abb20
import sam.model.entities.ec.Ecb01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SAP_Aquisicoes extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SAP - Aquisições";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("dataInicial", datas[0].format(DateTimeFormatter.ofPattern("MM/yyyy")));
		filtrosDefault.put("dataFinal", datas[1].format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		LocalDate dataInicial = DateUtils.parseDate("01/" + getString("dataInicial"));
		LocalDate dataFinal = DateUtils.parseDate("01/" + getString("dataFinal"));
		dataFinal = dataFinal.withDayOfMonth(dataFinal.lengthOfMonth());
		
		Aac10 aac10 = getVariaveis().getAac10();
		String nomeRelatorio = "SAP_Aquisicoes";
		
		params.put("EMPRESA", aac10.getAac10na());
		params.put("TITULO_RELATORIO", "AQUISIÇÕES");
		params.put("PERIODO", dataInicial.format(DateTimeFormatter.ofPattern("MM/yyyy")) + " à " + dataFinal.format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		TableMapDataSource tmDsAquisicoes = buscarDadosRelatorio(dataInicial, dataFinal);
		
		return gerarPDF(nomeRelatorio, tmDsAquisicoes);
	}
	
	public TableMapDataSource buscarDadosRelatorio(LocalDate dataInicial, LocalDate dataFinal) {
		Set<String> setBens = new HashSet<String>();
		
		List<TableMap> listTMImobilizacoes = buscarImobilizacaoPorAquisicao(dataInicial, dataFinal);
		
		if(listTMImobilizacoes == null || listTMImobilizacoes.size() == 0) throw new ValidacaoException("Não foram encontrados registros para exibição.");
		
		List<TableMap> mapRelatorio = new ArrayList<>();
		
		for(TableMap tm : listTMImobilizacoes) {
			String codGrupo = tm.getString("abb20codigo").substring(0, 2);
			
			if(setBens == null || !setBens.contains(codGrupo)) {
				Abb20 abb20 = getSession().get(Abb20.class, "abb20id, abb20codigo, abb20nome, abb20chapa", Criterions.and(Criterions.eq("abb20codigo", codGrupo), getSamWhere().getCritPadrao(Abb20.class)));
				if(abb20 != null) comporDadosMap(mapRelatorio, abb20.getAbb20codigo(), abb20.getAbb20nome(), abb20.getAbb20chapa(), null, null, BigDecimal.ZERO, null, null, 0, BigDecimal.ZERO, null, null, null, null);
				setBens.add(codGrupo);
			}
			
			comporDadosMap(mapRelatorio, tm.getString("abb20codigo"), tm.getString("abb20nome"), tm.getInteger("abb20chapa"), tm.getDate("abb20aquis"), 
				tm.getDate("ecb01dtImob"), tm.getBigDecimal_Zero("ecb01vlrAtual"), tm.getString("aah01codigo"), tm.getString("aah01nome"), tm.getInteger("abb01num"), 
				tm.getBigDecimal_Zero("ecb01txDepr"), tm.getString("abb20descr"), tm.getString("abe01na"), tm.getString("abb11codigo"), tm.getString("abb11nome"));
		}
		
		somarGrausSuperioresBens(mapRelatorio);
		
		TableMapDataSource tmDsAquisicoes = new TableMapDataSource(mapRelatorio);
		return tmDsAquisicoes;
	}
	
	public List<TableMap> buscarImobilizacaoPorAquisicao(LocalDate dtAquisIni, LocalDate dtAquisFin) {					 
		Query query = getSession().createQuery(" SELECT abb20codigo, abb20nome, abb20chapa, abb20aquis, ecb01dtImob, ecb01vlrAtual, ",
											   " aah01codigo, aah01nome, abb01num, ecb01txDepr, abb20descr, abe01na, abb11codigo, abb11nome ",
											   " FROM Ecb01 INNER JOIN Abb20 ON abb20id = ecb01bem ",
											   " LEFT JOIN Abb11 ON abb20depto = abb11id LEFT JOIN Abb01 ON abb20central = abb01id ",
											   " LEFT JOIN Abe01 ON abe01id = abb01ent LEFT JOIN Aah01 ON abb01tipo = aah01id ",
											   " WHERE abb20aquis BETWEEN :dtAquisIni AND :dtAquisFin ",
											   getSamWhere().getWherePadrao("AND", Ecb01.class),
											   " ORDER BY abb20codigo");
		query.setParameter("dtAquisIni", dtAquisIni);
		query.setParameter("dtAquisFin", dtAquisFin);
		return query.getListTableMap();
	}
	
	private void comporDadosMap(List<TableMap> mapRelatorio, String abb20codigo, String abb20nome, Integer abb20chapa, 
		LocalDate abb20aquis, LocalDate ecb01dtImob, BigDecimal ecb01vlrAtual, String aah01codigo, String aah01nome, Integer abb01num, 
		BigDecimal ecb01txDepr, String abb20descr, String abe01na, String abb11codigo, String abb11nome) {
		
		TableMap tm = new TableMap();
		
		tm.put("abb20codigo", abb20codigo);
		tm.put("abb20nome", abb20nome);
		tm.put("abb20chapa", abb20chapa);
		tm.put("abb20aquis", abb20aquis);
		tm.put("ecb01dtImob", ecb01dtImob);
		tm.put("ecb01vlrAtual", ecb01vlrAtual);
		tm.put("documento", aah01codigo == null ? null : aah01codigo + " - " + aah01nome);
		tm.put("abb01num", abb01num);
		tm.put("ecb01txDepr", ecb01txDepr);
		tm.put("abb20descr", abb20descr);
		tm.put("abe01na", abe01na);
		tm.put("centrocusto", abb11codigo == null ? null : abb11codigo + " - " + abb11nome);
		tm.put("total", BigDecimal.ZERO);
		
		mapRelatorio.add(tm);
	}
	
	private void somarGrausSuperioresBens(List<TableMap> mapRelatorio) {
		if(mapRelatorio == null || mapRelatorio.size() == 0)return;
		
		BigDecimal total = new BigDecimal(0);
		
		for(int i = mapRelatorio.size()-1; i >= 0; i--) {
			TableMap tm = mapRelatorio.get(i);
			String codigo = tm.getString("abb20codigo");
			
			if(codigo.length() < 10) {
				tm.put("total", total);
				total = BigDecimal.ZERO;
			}else {
				total = total + tm.getBigDecimal_Zero("ecb01vlrAtual");
			}
		}
	}

}
//meta-sis-eyJkZXNjciI6IlNBUCAtIEFxdWlzacOnw7VlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==