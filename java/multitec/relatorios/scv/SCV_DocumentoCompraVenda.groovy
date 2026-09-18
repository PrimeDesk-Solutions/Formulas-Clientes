package multitec.relatorios.scv;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SCV_DocumentoCompraVenda extends RelatorioBase {
		
	@Override
	public String getNomeTarefa() {
		return "SCV - Documento de Compra/Venda";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("pedEntSai", "0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> tipos = getListLong("tipos");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidades = getListLong("entidades");
		LocalDate[] emissao = getIntervaloDatas("emissao");
		Integer pedEntSai = getInteger("pedEntSai");
		Long id = getLong("eaa01id");
		
		LocalDate dtEmisIni = null;
		LocalDate dtEmisFin = null;
		if(emissao != null) {
			dtEmisIni = emissao[0];
			dtEmisFin = emissao[1];
		}
		
		List<TableMap> listTMDados = null;
		if(id == null) {
			listTMDados = buscarPedidos(tipos, numeroInicial, numeroFinal, dtEmisIni, dtEmisFin, entidades, pedEntSai);
		}else {
			listTMDados = buscarPedidoPeloId(id);
		}
		
		if(listTMDados == null || listTMDados.size() == 0) throw new ValidacaoException("Não foram encontrados dados com base no filtro informado.");
		
		TableMap tmEmpresa = buscarDadosEmpresa();
		
		List<TableMap> listTMItens = new ArrayList();
		
		for(TableMap tmPed : listTMDados) {
			Long eaa01id = tmPed.getLong("eaa01id");
			tmPed.put("key", eaa01id);
			
			if(tmEmpresa != null) tmPed.putAll(tmEmpresa);
			
			TableMap eaa01json = tmPed.getTableMap("eaa01json");
			if(eaa01json != null) tmPed.putAll(eaa01json);
			
			TableMap tmEnderecoPrincipal = buscarEnderecoPrincipalEntidadeDocumento(eaa01id);
			if(tmEnderecoPrincipal != null) tmPed.putAll(tmEnderecoPrincipal);
			
			TableMap tmEnderecoEntrega = buscarEnderecoEntregaEntidadeDocumento(eaa01id);
			if(tmEnderecoEntrega != null) tmPed.putAll(tmEnderecoEntrega);
			
			List<TableMap> listTMItensDoc = buscarItensDocumento(eaa01id);
			if(listTMItensDoc != null && listTMItensDoc.size() > 0) {
				for(TableMap tmItem : listTMItensDoc) {
					TableMap eaa0103json = tmItem.getTableMap("eaa0103json");
					if(eaa0103json != null) tmItem.putAll(eaa0103json);
					
					tmItem.put("key", eaa01id);					
					
					listTMItens.add(tmItem);
				}
			}
		}	
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		dsPrincipal.addSubDataSource("dsItens", listTMItens, "key", "key");
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCV_DocumentoCompraVenda_S1"))
		
		return gerarPDF("SCV_DocumentoCompraVenda", dsPrincipal);
	}
	
	private List<TableMap> buscarPedidos(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, 
		LocalDate dtEmisIni, LocalDate dtEmisFin, List<Long> entidades, Integer pedEntSai) {
				
		Query query = getSession().createQuery(" SELECT eaa01id, eaa01totItens, eaa01totDoc, eaa01totFinanc, eaa01json, ",
										" eaa0102codigo, eaa0102nome, eaa0102ddd, eaa0102fone, eaa0102eMail, eaa0102ni, ",
										" abb01id, abb01num, abb01data, aah01id, aah01codigo, aah01nome, abe01id, abe01codigo, abe01nome, abe01ni, abb10codigo, abb10descr ",
										" FROM Eaa01 ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" LEFT JOIN Abb10 ON abb01operCod = abb10id ",
										" LEFT JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										(dtEmisIni != null && dtEmisFin != null ? " AND abb01data BETWEEN :dtEmisIni AND :dtEmisFin " : ""),
										" AND abb01num BETWEEN :numeroInicial AND :numeroFinal ",
										(tipos != null && tipos.size() > 0 ? " AND abb01tipo IN (:tipos) " : ""),
										(entidades != null && entidades.size() > 0 ? " AND abb01ent IN (:entidades) " : ""),
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" ORDER BY aah01codigo, abb01num");
									
		if(tipos != null && tipos.size() > 0) {
			query.setParameter("tipos", tipos);
		}
		
		if(entidades != null && entidades.size() > 0) {
			query.setParameter("entidades", entidades);
		}
		
		if(dtEmisIni != null && dtEmisFin != null) {
			query.setParameter("dtEmisIni", dtEmisIni);
			query.setParameter("dtEmisFin", dtEmisFin);
		}
		
		query.setParameters("esMov", pedEntSai,
							"clasDoc", Eaa01.CLASDOC_SCV,
							"numeroInicial", numeroInicial,
							"numeroFinal", numeroFinal);
									  
		return query.getListTableMap();
	}
	
	private List<TableMap> buscarPedidoPeloId(Long id) {
		Query query = getSession().createQuery(" SELECT eaa01id, eaa01totItens, eaa01totDoc, eaa01totFinanc, eaa01json, ",
										" eaa0102codigo, eaa0102nome, eaa0102ddd, eaa0102fone, eaa0102eMail, eaa0102ni, ",
										" abb01id, abb01num, abb01data, aah01id, aah01codigo, aah01nome, abe01id, abe01codigo, abe01nome, abe01ni, abb10codigo, abb10descr ",
										" FROM Eaa01 ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" LEFT JOIN Abb10 ON abb01operCod = abb10id ",
										" LEFT JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" WHERE eaa01id = :id ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" ORDER BY aah01codigo, abb01num");
									
		query.setParameter("id", id);
									  
		return query.getListTableMap();
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
	
	private TableMap buscarEnderecoPrincipalEntidadeDocumento(Long eaa01id) {
		return getSession().createQuery(
				" SELECT eaa0101local as eaa0101local_princ, eaa0101endereco as eaa0101endereco_princ, eaa0101numero as eaa0101numero_princ, eaa0101bairro as eaa0101bairro_princ, ",
				" eaa0101complem as eaa0101complem_princ, eaa0101cep as eaa0101cep_princ, eaa0101ddd as eaa0101ddd_princ, eaa0101fone as eaa0101fone_princ, eaa0101eMail as eaa0101eMail_princ, ",
				" aag0201nome as aag0201nome_princ, aag02uf as aag02uf_princ, aap15descr as aap15descr_princ, aag03nome as aag03nome_princ ",
				" FROM Eaa0101 ",
				" LEFT JOIN Aap15 ON aap15id = eaa0101tpLog ",
				" LEFT JOIN Aag0201 ON aag0201id = eaa0101municipio ",
				" LEFT JOIN Aag02 ON aag02id = aag0201uf ",
				" LEFT JOIN Aag03 ON aag03id = eaa0101regiao ",
				" WHERE eaa0101doc = :eaa01id AND eaa0101principal = :principal ")
			.setParameters("eaa01id", eaa01id,
						   "principal", 1)
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private TableMap buscarEnderecoEntregaEntidadeDocumento(Long eaa01id) {
		return getSession().createQuery(
				" SELECT eaa0101local as eaa0101local_entrega, eaa0101endereco as eaa0101endereco_entrega, eaa0101numero as eaa0101numero_entrega, eaa0101bairro as eaa0101bairro_entrega, ",
				" eaa0101complem as eaa0101complem_entrega, eaa0101cep as eaa0101cep_entrega, eaa0101ddd as eaa0101ddd_entrega, eaa0101fone as eaa0101fone_entrega, eaa0101eMail as eaa0101eMail_entrega, ",
				" aag0201nome as aag0201nome_entrega, aag02uf as aag02uf_entrega, aap15descr as aap15descr_entrega, aag03nome as aag03nome_entrega ",
				" FROM Eaa0101 ",
				" LEFT JOIN Aap15 ON aap15id = eaa0101tpLog ",
				" LEFT JOIN Aag0201 ON aag0201id = eaa0101municipio ",
				" LEFT JOIN Aag02 ON aag02id = aag0201uf ",
				" LEFT JOIN Aag03 ON aag03id = eaa0101regiao ",
				" WHERE eaa0101doc = :eaa01id AND eaa0101entrega = :entrega ")
			.setParameters("eaa01id", eaa01id,
						   "entrega", 1)
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private List<TableMap> buscarItensDocumento(Long eaa01id) {
		return getSession().createQuery(
				" SELECT eaa0103id, eaa0103doc, eaa0103seq, eaa0103codigo, eaa0103descr, eaa0103qtComl, eaa0103unit, eaa0103total, eaa0103totDoc, eaa0103totFinanc, eaa0103json, ",
				" aam06umComl.aam06codigo as aam06codigo_umComl ",
				" FROM Eaa0103 ",
				" LEFT JOIN Aam06 aam06umComl ON eaa0103umComl = aam06umComl.aam06id ",
				" WHERE eaa0103doc = :eaa01id ",
				" ORDER BY eaa0103seq")
			.setParameters("eaa01id", eaa01id)
			.getListTableMap();
	}
	
}
//meta-sis-eyJkZXNjciI6IlNDViAtIERvY3VtZW50byBkZSBDb21wcmEvVmVuZGEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=