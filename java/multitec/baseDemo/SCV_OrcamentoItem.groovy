package multitec.baseDemo

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abm01
import sam.model.entities.cb.Cbe10
import sam.model.entities.cb.Cbe1001
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SCV_OrcamentoItem extends FormulaBase {
	
	private Cbe1001 cbe1001;
	private String procInvoc;
	
	private Cbe10 cbe10;
	private Abe40 abe40;
	private Abe30 abe30;
	private Abm01 abm01;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_ITEM_ORCAMENTO;
	}
	
	@Override
	public void executar() {
		cbe1001 = get("cbe1001");
		procInvoc = get("procInvoc");
		
		cbe10 = cbe1001.cbe1001orcam;
		abe40 = cbe10.cbe10tp;
		abe30 = cbe10.cbe10cp;
		
		abm01 = cbe1001.cbe1001item;
		
		setarObterPrecoUnitario();
		
		if(procInvoc == "CAS0240" || procInvoc == "CAS0242") return;
		
		cbe1001.cbe1001total = cbe1001.cbe1001qtComl * cbe1001.cbe1001unit;
		cbe1001.cbe1001total = round(cbe1001.cbe1001total, 2);
		
		cbe1001.cbe1001totDoc = cbe1001.cbe1001total;
		cbe1001.cbe1001totDoc = round(cbe1001.cbe1001totDoc, 2);
		
		cbe1001.cbe1001totFinanc = cbe1001.cbe1001total;
		cbe1001.cbe1001totFinanc = round(cbe1001.cbe1001totFinanc, 2);
	}
	
	private void setarObterPrecoUnitario() {
		if(abe40 == null) return;
		
		if(cbe1001.cbe1001unit != 0) return;
		
		//Verificando se a tabela de preço está vencida
		def sql = " SELECT abe40dtVcto" +
				   " FROM Abe40" +
				   " WHERE abe40id = :abe40id";
					  
		def abe40dtVcto = getAcessoAoBanco().obterDate(sql, Parametro.criar("abe40id", abe40.abe40id));
		
		if(abe40dtVcto != null) {
			if(MDate.date() > abe40dtVcto) {
				throw new ValidacaoException("Tabela de preços vencida.");
			}
		}
		
		//Buscando preço na tabela de preço por item, tabela, condição de pagamento (se informada no item ou no documento), qtde comercial, taxa de desconto (se informada nos campos livres json)
		def abe30id = abe30 != null ? abe30.abe30id : null;
		
		sql = " SELECT abe4001preco" +
			  " FROM Abe4001 " +
			  " INNER JOIN Abe40 ON abe4001tab = abe40id" +
			  " WHERE abe4001item = :abm01id" +
			  " AND abe4001tab = :abe40id" +
			  (abe30id != null ? " AND abe4001cp = :abe30id" : "") +
			  " AND abe4001qtMax >= :qtde" +
			  " AND abe4001txDesc >= :txDesc" +
			  getSamWhere().getWherePadrao("AND", Abe40.class) +
			  " ORDER BY abe4001qtMax, abe4001txDesc";
			  
		//Buscando taxa de desconto informada no item do documento em campo Json e verificando se este campo existe
		def txDesc = 0;
		TableMap mapJson = cbe1001.cbe1001json == null ? new TableMap() : cbe1001.cbe1001json;
		if(mapJson.containsKey("vlr_desc_tx")) {
			txDesc = mapJson.getBigDecimal("vlr_desc_tx");
			if(txDesc == null) txDesc = 0;
		}
	  
		List<Parametro> parametros = new ArrayList<>();
		parametros.add(Parametro.criar("abm01id", abm01.abm01id));
		parametros.add(Parametro.criar("abe40id", abe40.abe40id));
		parametros.add(Parametro.criar("qtde", cbe1001.cbe1001qtComl));
		parametros.add(Parametro.criar("txDesc", txDesc));
		if(abe30id != null) parametros.add(Parametro.criar("abe30id", abe30id)); // <- Se não informada condição de pagamento no item ou no documento, a mesma não será considerada na busca
				   
		TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, parametros.toArray(new Parametro[parametros.size()]));
		
		def unit = 0;
		
		if(tm != null) {
			unit = tm.getBigDecimal("abe4001preco");
		}
		
		cbe1001.cbe1001unit = unit;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTE2In0=