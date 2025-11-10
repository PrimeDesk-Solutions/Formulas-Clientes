package multitec.baseDemo

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ba.Bab01
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.server.sce.service.SCEService

class CAS_SelecionarItens extends FormulaBase {
	
	Long abm01id;
	BigDecimal estoque;
	BigDecimal quantidade;
	TableMap json;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CAS_SELECIONAR_ITENS;
	}
	
	@Override
	public void executar() {
		abm01id = get("abm01id");
		estoque = get("estoque");
		quantidade = get("quantidade");
		json = get("json");
		
		if(json == null) json = new TableMap();
		
		SCEService sceService = instanciarService(SCEService.class);
		
		def estoquedisponivel = sceService.saldoEstoqueItemResumido(abm01id, 1, null);
		def estoqueindisponivel = sceService.saldoEstoqueItemResumido(abm01id, 0, null);
		def estoquetotal = estoquedisponivel + estoqueindisponivel;
		
		def vendidobruto = somaQuantidadesDocumentos(abm01id, Eaa01.ESMOV_SAIDA);
		def vendidoatendido = somaQuantidadesDocumentosAtendimentos(abm01id, Eaa01.ESMOV_SAIDA);
		def vendido = vendidobruto.subtract(vendidoatendido);
		
		def compradobruto = somaQuantidadesDocumentos(abm01id, Eaa01.ESMOV_ENTRADA);
		def compradoatendido = somaQuantidadesDocumentosAtendimentos(abm01id, Eaa01.ESMOV_ENTRADA);
		def comprado = compradobruto - compradoatendido;
		
		def aproduzir = somaQuantidadeAProduzir(abm01id);
		def produzida = somaQuantidadeProduzida(abm01id);
		def emProducao = aproduzir - produzida;
		
		def liquido = estoquetotal - vendido + comprado + emProducao;
		
		json.put("estoquedisponivel", estoquedisponivel);
		json.put("estoqueindisponivel", estoqueindisponivel);
		json.put("estoquetotal", estoquetotal);
		
		json.put("vendido", vendido);
		
		json.put("comprado", comprado);
		
		json.put("emproducao", emProducao);
		
		json.put("liquido", liquido);
		
		put("estoque", estoque);
		put("quantidade", quantidade);
		put("json", json);
	}
	
	private BigDecimal somaQuantidadesDocumentos(Long abm01id, Integer esMov) {
		BigDecimal somaQuantidade = getSession().createQuery(" SELECT SUM(eaa0103qtUso) ",
															 " FROM Eaa0103 ",
															 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
															 " WHERE eaa01clasDoc = 0 ",
															 " AND eaa01esMov = :esMov ",
															 " AND eaa01cancData IS NULL ",
															 " AND eaa01scvAtend <> 2 ",
															 " AND eaa01gravParc = 0 ",
															 " AND eaa01iSCE = 0 ",
															 " AND eaa0103item = :abm01id ",
															 " AND eaa0103pedAtend <> 2 ",
															 getSamWhere().getWhereGc("AND", Eaa01.class))
												.setParameter("esMov", esMov)
												.setParameter("abm01id", abm01id)
												.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return somaQuantidade != null ? somaQuantidade : BigDecimal.ZERO;
	}
	
	private BigDecimal somaQuantidadesDocumentosAtendimentos(Long abm01id, Integer esMov) {
		BigDecimal somaQuantidade = getSession().createQuery(" SELECT SUM(eaa01032qtUso) ",
															 " FROM Eaa01032 ",
															 " INNER JOIN Eaa0103 ON eaa01032itemScv = eaa0103id ",
															 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
															 " WHERE eaa01clasDoc = 0 ",
															 " AND eaa01esMov = :esMov ",
															 " AND eaa01cancData IS NULL ",
															 " AND eaa01scvAtend <> 2 ",
															 " AND eaa01gravParc = 0 ",
															 " AND eaa01iSCE = 0 ",
															 " AND eaa0103item = :abm01id ",
															 " AND eaa0103pedAtend <> 2 ",
															 getSamWhere().getWhereGc("AND", Eaa01.class))
												.setParameter("esMov", esMov)
												.setParameter("abm01id", abm01id)
												.getUniqueResult(ColumnType.BIG_DECIMAL);
				
		return somaQuantidade != null ? somaQuantidade : BigDecimal.ZERO;
	}
	
	private BigDecimal somaQuantidadeAProduzir(Long abm01id) {
		BigDecimal somaQuantidadeAProduzir = getSession().createQuery(" SELECT SUM(bab01qt) ",
																	  " FROM Bab01 ",
																	  " INNER JOIN Abp20 ON bab01comp = abp20id ",
																	  " WHERE bab01status < 2 ",
																	  " AND abp20item = :abm01id ",
																	  getSamWhere().getWhereGc("AND", Bab01.class))
														 .setParameter("abm01id", abm01id)
														 .getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return somaQuantidadeAProduzir != null ? somaQuantidadeAProduzir : BigDecimal.ZERO;
	}
	
	private BigDecimal somaQuantidadeProduzida(Long abm01id) {
		BigDecimal somaQuantidadeProduzida = getSession().createQuery(" SELECT SUM(bab01qtP) ",
																	  " FROM Bab01 ",
																	  " INNER JOIN Abp20 ON bab01comp = abp20id ",
																	  " WHERE bab01status < 2 ",
																	  " AND abp20item = :abm01id ",
																	  getSamWhere().getWhereGc("AND", Bab01.class))
														 .setParameter("abm01id", abm01id)
														 .getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return somaQuantidadeProduzida != null ? somaQuantidadeProduzida : BigDecimal.ZERO;
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjYifQ==