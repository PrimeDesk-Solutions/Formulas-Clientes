package Silcon.relatorios.scv;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.cb.Cbd01
import sam.model.entities.cc.Ccb01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SCV_OrdemDeServico extends RelatorioBase {
		
	@Override
	public String getNomeTarefa() {
		return "SCV - Ordem de Serviço";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("statusCriada", true);
		filtrosDefault.put("statusAprovada", true);
		filtrosDefault.put("statusAutorizada", true);
		filtrosDefault.put("statusEmProcesso", true);
		filtrosDefault.put("statusExecutada", true);
		filtrosDefault.put("statusConcluida", true);
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidades = getListLong("entidades");
		LocalDate[] emissao = getIntervaloDatas("emissao");
		boolean statusCriada = get("statusCriada");
		boolean statusAprovada = get("statusAprovada");
		boolean statusAutorizada = get("statusAutorizada");
		boolean statusEmProcesso = get("statusEmProcesso");
		boolean statusExecutada = get("statusExecutada");
		boolean statusConcluida = get("statusConcluida");
		Long id = getLong("cbd01id");
				
		LocalDate dtIni = null;
		LocalDate dtFin = null;
		if(emissao != null) {
			dtIni = emissao[0];
			dtFin = emissao[1];
		}
		
		List<TableMap> listTMDados = null;
		if(id == null) {
			if(!statusCriada && !statusAprovada && !statusAutorizada 
				&& !statusEmProcesso && !statusExecutada && !statusConcluida) throw new ValidacaoException("Necessário escolher pelo menos um status.");
			
			List<Integer> status = new ArrayList<>();
			if(statusCriada) status.add(0);
			if(statusAprovada) status.add(1);
			if(statusAutorizada) status.add(2);
			if(statusEmProcesso) status.add(3);
			if(statusExecutada) status.add(4);
			if(statusConcluida) status.add(5);
			
			listTMDados = buscarOrdensDeServico(numeroInicial, numeroFinal, dtIni, dtFin, entidades, status);
		}else {
			TableMap tmOrdemServico = buscarOrdemDeServico(id);
			if(tmOrdemServico == null) throw new ValidacaoException("Não foi encontrada a Ordem de Serviço para impressão.");
			listTMDados = new ArrayList();
			listTMDados.add(tmOrdemServico);
		}
		
		if(listTMDados == null || listTMDados.size() == 0) throw new ValidacaoException("Não foram encontrados dados com base no filtro informado.");
		
		TableMap tmEmpresa = buscarDadosEmpresa();
		
		List<TableMap> listTMItens = new ArrayList();
		
		for(TableMap tmOrdemServico : listTMDados) {
			Long cbd01id = tmOrdemServico.getLong("cbd01id");
			tmOrdemServico.put("key", cbd01id);
			
			if(tmEmpresa != null) tmOrdemServico.putAll(tmEmpresa);
		}	
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		
		return gerarPDF("SCV_OrdemDeServico", dsPrincipal);
	}
	
	private List<TableMap> buscarOrdensDeServico(Integer numeroInicial, Integer numeroFinal, 
		LocalDate dtIni, LocalDate dtFin, List<Long> entidades, List<Integer> status) {
				
		Query query = getSession().createQuery(" SELECT cbd01id, abb01num, abb01data, cbd01status, ",
										" cbd01eeLocal, cbd01eeEndereco, cbd01eeNumero, cbd01eeBairro, cbd01eeComplem, cbd01eeCep, ",
										" cbd01eeDdd1, cbd01eeFone1, cbd01eeDdd2, cbd01eeFone2, cbd01eeeMail, ",
										" abe01id, abe01codigo, abe01na, abe01nome, abe01ni, ",
										" abm01Obj.abm01codigo AS abm01codigoObj, abm01Obj.abm01descr AS abm01descrObj, ",
										" abm01Serv.abm01codigo AS abm01codigoServ, abm01Serv.abm01descr AS abm01descrServ, ",
										" cbd01descrObj, cbd01servObj, cbd01espec, cbd01obs, cbd01garant, cbd01oco, cbd01implic, ",
										" cbd01vlrServ, cbd01vlrPecas, cbd01vlrDesp, cbd01vlrGastos, ",
										" cbd01prevDti, cbd01prevHri, cbd01prevDtf, cbd01prevHrf, ",
										" cbd01realDti, cbd01realHri, cbd01realDtf, cbd01realHrf, ",
										" cba20id, cba20codigo, cba20descr, ",
										" abe40id, abe40codigo, abe40nome, ",
										" abe30id, abe30codigo, abe30nome, ",
										" aap15id, aap15codigo, aap15descr, ",
										" aag0201nome as aag0201nome_ent, aag02uf as aag02uf_ent, aag01nome as aag01nome_ent, aag03nome as aag03nome_ent ",
										" FROM Cbd01 ",
										" INNER JOIN Abb01 ON cbd01central = abb01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" LEFT JOIN Abm01 AS abm01Obj ON cbd01itemObj = abm01Obj.abm01id ",
										" LEFT JOIN Abm01 AS abm01Serv ON cbd01itemServ = abm01Serv.abm01id ",
										" LEFT JOIN Cba20 ON cbd01roteiro = cba20id ",
										" LEFT JOIN Abe40 ON cbd01tp = abe40id ",
										" LEFT JOIN Abe30 ON cbd01cp = abe30id ",
										" LEFT JOIN Aap15 ON cbd01eeTpLog = aap15id ",
										" LEFT JOIN Aag0201 ON cbd01eeMunicipio = aag0201id ",
										" LEFT JOIN Aag02 ON aag0201uf = aag02id ",
										" LEFT JOIN Aag01 ON cbd01eePais = aag01id ",
										" LEFT JOIN Aag03 ON cbd01eeRegiao = aag03id ",
										" WHERE abb01num BETWEEN :numeroInicial AND :numeroFinal ",
										" AND cbd01status IN (:status) ",
										(dtIni != null && dtFin != null ? " AND abb01data BETWEEN :dtIni AND :dtFin " : ""),
										(entidades != null && entidades.size() > 0 ? " AND abb01ent IN (:entidades) " : ""),
										getSamWhere().getWherePadrao("AND", Cbd01.class),
										" ORDER BY abb01num");
		
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
	
	private TableMap buscarOrdemDeServico(Long cbd01id) {
		Query query = getSession().createQuery(" SELECT cbd01id, abb01num, abb01data, cbd01status, ",
										" cbd01eeLocal, cbd01eeEndereco, cbd01eeNumero, cbd01eeBairro, cbd01eeComplem, cbd01eeCep, ",
										" cbd01eeDdd1, cbd01eeFone1, cbd01eeDdd2, cbd01eeFone2, cbd01eeeMail, ",
										" abe01id, abe01codigo, abe01na, abe01nome, abe01ni, ",
										" abm01Obj.abm01codigo AS abm01codigoObj, abm01Obj.abm01descr AS abm01descrObj, ",
										" abm01Serv.abm01codigo AS abm01codigoServ, abm01Serv.abm01descr AS abm01descrServ, ",
										" cbd01descrObj, cbd01servObj, cbd01espec, cbd01obs, cbd01garant, cbd01oco, cbd01implic, ",
										" cbd01vlrServ, cbd01vlrPecas, cbd01vlrDesp, cbd01vlrGastos, ",
										" cbd01prevDti, cbd01prevHri, cbd01prevDtf, cbd01prevHrf, ",
										" cbd01realDti, cbd01realHri, cbd01realDtf, cbd01realHrf, ",
										" cba20id, cba20codigo, cba20descr, ",
										" abe40id, abe40codigo, abe40nome, ",
										" abe30id, abe30codigo, abe30nome, ",
										" aap15id, aap15codigo, aap15descr, ",
										" aag0201nome as aag0201nome_ent, aag02uf as aag02uf_ent, aag01nome as aag01nome_ent, aag03nome as aag03nome_ent ",
										" FROM Cbd01 ",
										" INNER JOIN Abb01 ON cbd01central = abb01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" LEFT JOIN Abm01 AS abm01Obj ON cbd01itemObj = abm01Obj.abm01id ",
										" LEFT JOIN Abm01 AS abm01Serv ON cbd01itemServ = abm01Serv.abm01id ",
										" LEFT JOIN Cba20 ON cbd01roteiro = cba20id ",
										" LEFT JOIN Abe40 ON cbd01tp = abe40id ",
										" LEFT JOIN Abe30 ON cbd01cp = abe30id ",
										" LEFT JOIN Aap15 ON cbd01eeTpLog = aap15id ",
										" LEFT JOIN Aag0201 ON cbd01eeMunicipio = aag0201id ",
										" LEFT JOIN Aag02 ON aag0201uf = aag02id ",
										" LEFT JOIN Aag01 ON cbd01eePais = aag01id ",
										" LEFT JOIN Aag03 ON cbd01eeRegiao = aag03id ",
										" WHERE cbd01id = :cbd01id ",
										getSamWhere().getWherePadrao("AND", Cbd01.class));
		
		query.setParameter("cbd01id", cbd01id);
									  
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
	
}
//meta-sis-eyJkZXNjciI6IlNDViAtIE9yZGVtIGRlIFNlcnZpw6dvIiwidGlwbyI6InJlbGF0b3JpbyJ9