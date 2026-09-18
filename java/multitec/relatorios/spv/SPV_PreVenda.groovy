package multitec.relatorios.spv;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.cc.Ccb01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SPV_PreVenda extends RelatorioBase {
		
	@Override
	public String getNomeTarefa() {
		return "SPV - Pré-Venda";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("statusConcluida", true);
		filtrosDefault.put("statusBalcao", true);
		filtrosDefault.put("statusOrcamento", true);
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidades = getListLong("entidades");
		LocalDate[] emissao = getIntervaloDatas("emissao");
		boolean statusConcluida = get("statusConcluida");
		boolean statusBalcao = get("statusBalcao");
		boolean statusOrcamento = get("statusOrcamento");
		Long id = getLong("ccb01id");
				
		LocalDate dtIni = null;
		LocalDate dtFin = null;
		if(emissao != null) {
			dtIni = emissao[0];
			dtFin = emissao[1];
		}
		
		List<TableMap> listTMDados = null;
		if(id == null) {
			if(!statusConcluida && !statusBalcao && !statusOrcamento) throw new ValidacaoException("Necessário escolher pelo menos um status.");
			
			List<Integer> status = new ArrayList<>();
			if(statusConcluida) status.add(0);
			if(statusBalcao) status.add(1);
			if(statusOrcamento) status.add(2);
			
			listTMDados = buscarPreVendas(numeroInicial, numeroFinal, dtIni, dtFin, entidades, status);
		}else {
			TableMap tmPreVenda = buscarPreVenda(id);
			if(tmPreVenda == null) throw new ValidacaoException("Não foi encontrada a pré-venda para impressão.");
			listTMDados = new ArrayList();
			listTMDados.add(tmPreVenda);
		}
		
		if(listTMDados == null || listTMDados.size() == 0) throw new ValidacaoException("Não foram encontrados dados com base no filtro informado.");
		
		TableMap tmEmpresa = buscarDadosEmpresa();
		
		List<TableMap> listTMItens = new ArrayList();
		
		for(TableMap tmPreVenda : listTMDados) {
			Long ccb01id = tmPreVenda.getLong("ccb01id");
			tmPreVenda.put("key", ccb01id);
			
			if(tmEmpresa != null) tmPreVenda.putAll(tmEmpresa);
			
			def totalItens = 0.0;
			def totalDesconto = 0.0;
			def totalDoc = 0.0;
			List<TableMap> listTMItensPreVenda = buscarItensPreVenda(ccb01id);
			if(listTMItensPreVenda != null && listTMItensPreVenda.size() > 0) {
				for(TableMap tmItem : listTMItensPreVenda) {					
					tmItem.put("key", ccb01id);
					
					totalItens = totalItens + tmItem.getBigDecimal_Zero("ccb0101total");
					totalDesconto = totalDesconto + tmItem.getBigDecimal_Zero("ccb0101desc");
					totalDoc = totalDoc + tmItem.getBigDecimal_Zero("ccb0101totDoc");
					
					listTMItens.add(tmItem);
				}
			}
			
			tmPreVenda.put("totalItens", totalItens);
			tmPreVenda.put("totalDesconto", totalDesconto);
			tmPreVenda.put("totalDoc", totalDoc);
		}	
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		dsPrincipal.addSubDataSource("dsItens", listTMItens, "key", "key");
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPV_PreVenda_S1"))
		
		return gerarPDF("SPV_PreVenda", dsPrincipal);
	}
	
	private List<TableMap> buscarPreVendas(Integer numeroInicial, Integer numeroFinal, 
		LocalDate dtIni, LocalDate dtFin, List<Long> entidades, List<Integer> status) {
				
		Query query = getSession().createQuery(" SELECT ccb01id, ccb01num, ccb01comanda, ccb01data, ccb01comprador, ",
										" ccb01eeLocal, ccb01eeEndereco, ccb01eeNumero, ccb01eeBairro, ccb01eeComplem, ccb01eeCep, ccb01obs, ccb01status, ",
										" ccb01eeDdd1, ccb01eeFone1, ccb01eeDdd2, ccb01eeFone2, ",
										" abe01id, abe01codigo, abe01na, abe01nome, abe01ni, ",
										" cca01id, cca01codigo, cca01descr, ",
										" abe40id, abe40codigo, abe40nome, ",
										" abe30id, abe30codigo, abe30nome, ",
										" aab10id, aab10user, aab10nome, ",
										" aap15id, aap15codigo, aap15descr, ",
										" aag0201nome as aag0201nome_ent, aag02uf as aag02uf_ent, aag01nome as aag01nome_ent, aag03nome as aag03nome_ent ",
										" FROM Ccb01 ",
										" INNER JOIN Abe01 ON ccb01ent = abe01id ",
										" INNER JOIN Cca01 ON ccb01ppv = cca01id ",
										" LEFT JOIN Abe40 ON ccb01tp = abe40id ",
										" LEFT JOIN Abe30 ON ccb01cp = abe30id ",
										" LEFT JOIN Aap15 ON ccb01eeTpLog = aap15id ",
										" LEFT JOIN Aag0201 ON ccb01eeMunicipio = aag0201id ",
										" LEFT JOIN Aag02 ON aag0201uf = aag02id ",
										" LEFT JOIN Aag01 ON ccb01eePais = aag01id ",
										" LEFT JOIN Aag03 ON ccb01eeRegiao = aag03id ",
										" LEFT JOIN Aab10 ON ccb01user = aab10id ",
										" WHERE ccb01num BETWEEN :numeroInicial AND :numeroFinal ",
										" AND ccb01status IN (:status) ",
										(dtIni != null && dtFin != null ? " AND ccb01data BETWEEN :dtIni AND :dtFin " : ""),
										(entidades != null && entidades.size() > 0 ? " AND ccb01ent IN (:entidades) " : ""),
										getSamWhere().getWherePadrao("AND", Ccb01.class),
										" ORDER BY ccb01num");
		
		if(entidades != null && entidades.size() > 0) {
			query.setParameter("entidades", entidades);
		}
		
		if(dtIni != null && dtFin != null) {
			query.setParameter("dtIni", dtIni);
			query.setParameter("dtFin", dtFin);
		}
		
		query.setParameters("numeroInicial", numeroInicial,
							"numeroFinal", numeroFinal,
							"status", status);
									  
		return query.getListTableMap();
	}
	
	private TableMap buscarPreVenda(Long ccb01id) {
		Query query = getSession().createQuery(" SELECT ccb01id, ccb01num, ccb01comanda, ccb01data, ccb01comprador, ",
										" ccb01eeLocal, ccb01eeEndereco, ccb01eeNumero, ccb01eeBairro, ccb01eeComplem, ccb01eeCep, ccb01obs, ccb01status, ",
										" ccb01eeDdd1, ccb01eeFone1, ccb01eeDdd2, ccb01eeFone2, ",
										" abe01id, abe01codigo, abe01na, abe01nome, abe01ni, ",
										" cca01id, cca01codigo, cca01descr, ",
										" abe40id, abe40codigo, abe40nome, ",
										" abe30id, abe30codigo, abe30nome, ",
										" aab10id, aab10user, aab10nome, ",
										" aap15id, aap15codigo, aap15descr, ",
										" aag0201nome as aag0201nome_ent, aag02uf as aag02uf_ent, aag01nome as aag01nome_ent, aag03nome as aag03nome_ent ",
										" FROM Ccb01 ",
										" INNER JOIN Abe01 ON ccb01ent = abe01id ",
										" INNER JOIN Cca01 ON ccb01ppv = cca01id ",
										" LEFT JOIN Abe40 ON ccb01tp = abe40id ",
										" LEFT JOIN Abe30 ON ccb01cp = abe30id ",
										" LEFT JOIN Aap15 ON ccb01eeTpLog = aap15id ",
										" LEFT JOIN Aag0201 ON ccb01eeMunicipio = aag0201id ",
										" LEFT JOIN Aag02 ON aag0201uf = aag02id ",
										" LEFT JOIN Aag01 ON ccb01eePais = aag01id ",
										" LEFT JOIN Aag03 ON ccb01eeRegiao = aag03id ",
										" LEFT JOIN Aab10 ON ccb01user = aab10id ",
										" WHERE ccb01id = :ccb01id ",
										getSamWhere().getWherePadrao("AND", Ccb01.class));
		
		query.setParameter("ccb01id", ccb01id);
									  
		return query.getUniqueTableMap();
	}
	
	private TableMap buscarDadosEmpresa() {
		return getSession().createQuery(
				" SELECT aac10rs, aac10na, aac10endereco, aac10numero, aac10complem, ",
				" aac10bairro, aac10dddFone, aac10fone, aac10ni, aac10cep, aag0201nome as aag0201nome_emp, aag02uf as aag02uf_emp ",
				" FROM Aac10 ",
				" LEFT JOIN Aag0201 ON aag0201id = aac10municipio ",
				" LEFT JOIN Aag02 ON aag02id = aag0201uf ",
				" WHERE aac10id = :idAac10 ")
			.setParameter("idAac10", getVariaveis().getAac10().getAac10id())
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private List<TableMap> buscarItensPreVenda(Long ccb01id) {
		return getSession().createQuery(
				" SELECT ccb0101id, ccb0101seq, abm01id, abm01tipo, abm01codigo, abm01descr, abm01na, ccb0101entregar, ",
				" aam06umv.aam06codigo as aam06codigo_umv, ",
				" ccb0101qtComl, ccb0101unit, ccb0101total, ccb0101percDesc, ccb0101desc, ccb0101totDoc ",
				" FROM Ccb0101 ",
				" LEFT JOIN Abm01 ON ccb0101item = abm01id ",
				" LEFT JOIN Aam06 aam06umv ON ccb0101umv = aam06umv.aam06id ",
				" WHERE ccb0101pv = :ccb01id ",
				" ORDER BY ccb0101seq")
			.setParameters("ccb01id", ccb01id)
			.getListTableMap();
	}
	
}
//meta-sis-eyJkZXNjciI6IlNQViAtIFByw6ktVmVuZGEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=