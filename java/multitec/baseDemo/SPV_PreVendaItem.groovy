package multitec.baseDemo

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abm01
import sam.model.entities.cc.Ccb01
import sam.model.entities.cc.Ccb0101
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SPV_PreVendaItem extends FormulaBase {
	
	private Ccb0101 ccb0101;
	private String procInvoc;
	private Integer campoDigitado; // 0: Nenhum, 1: Quantidade, 2: Unitário, 3: % Desconto, 4: Valor Desconto
	
	private Ccb01 ccb01;
	private Abe01 abe01;
	private Abe40 abe40;
	private Abe30 abe30;
	private Abm01 abm01;
		
	@Override
	public void executar() {
		ccb0101 = get("ccb0101");
		procInvoc = get("procInvoc");
		campoDigitado = get("campoDigitado");
		
		ccb01 = ccb0101.ccb0101pv;
		abe01 = ccb01.ccb01ent;
		abe40 = ccb01.ccb01tp;
		abe30 = ccb01.ccb01cp;
		
		abm01 = ccb0101.ccb0101item;
		
		//Calcula somente o % Desconto e TotalDoc caso tenha sido digitado o Valor Desconto
		if(campoDigitado == 4) {			
			def percDesc = 0;
			if(ccb0101.ccb0101total_Zero > 0 && ccb0101.ccb0101desc_Zero > 0) {
				if(ccb0101.ccb0101desc_Zero > ccb0101.ccb0101total_Zero) {
					throw new ValidacaoException("Valor do desconto não pode ser maior que o total do item.");
				}
				
				percDesc = (ccb0101.ccb0101desc_Zero * 100) / ccb0101.ccb0101total_Zero;
			}
			ccb0101.ccb0101percDesc = percDesc;
			ccb0101.ccb0101percDesc = round(ccb0101.ccb0101percDesc_Zero, 2);
			
			ccb0101.ccb0101totDoc = ccb0101.ccb0101total_Zero - ccb0101.ccb0101desc_Zero;
			
			return;
		}
		
		setarObterPrecoUnitario();
		
		if(procInvoc == "CAS0240") return;
		
		//Calculando total do item
		ccb0101.ccb0101total = ccb0101.ccb0101qtComl_Zero * ccb0101.ccb0101unit_Zero;
		ccb0101.ccb0101total = round(ccb0101.ccb0101total_Zero, 2);
		
		//Calculando o valor do desconto com base no % Desconto
		def valorDesc = 0;
		if(ccb0101.ccb0101total_Zero > 0 && ccb0101.ccb0101percDesc_Zero > 0) {
			if(ccb0101.ccb0101percDesc_Zero > 100) {
				throw new ValidacaoException("% do desconto não pode ser maior que 100,00.");
			}
			
			valorDesc = (ccb0101.ccb0101total_Zero * ccb0101.ccb0101percDesc_Zero) / 100;
		}
		ccb0101.ccb0101desc = valorDesc;
		ccb0101.ccb0101desc = round(ccb0101.ccb0101desc_Zero, 2);
		
		//Calculando o TotalDoc
		ccb0101.ccb0101totDoc = ccb0101.ccb0101total_Zero - ccb0101.ccb0101desc_Zero;
	}
	
	private void setarObterPrecoUnitario() {
		if(abe40 == null) return;
		
		if(ccb0101.ccb0101unit != 0) return;
		
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
		
		//Buscando preço na tabela de preço por item, tabela, condição de pagamento, qtde comercial, taxa de desconto			
		sql = " SELECT abe4001preco" +
			  " FROM Abe4001 " +
			  " INNER JOIN Abe40 ON abe4001tab = abe40id" +
			  " WHERE abe4001item = :abm01id" +
			  " AND abe4001tab = :abe40id" +
			  (abe30 != null ? " AND abe4001cp = :abe30id" : "") +
			  " AND abe4001qtMax >= :qtde" +
			  " AND abe4001txDesc >= :txDesc" +
			  getSamWhere().getWherePadrao("AND", Abe40.class) +
			  " ORDER BY abe4001qtMax, abe4001txDesc";
		
		List<Parametro> parametros = new ArrayList<>();
		parametros.add(Parametro.criar("abm01id", abm01.abm01id));
		parametros.add(Parametro.criar("abe40id", abe40.abe40id));
		parametros.add(Parametro.criar("qtde", ccb0101.ccb0101qtComl_Zero));
		parametros.add(Parametro.criar("txDesc", ccb0101.ccb0101percDesc_Zero));
		if(abe30 != null) parametros.add(Parametro.criar("abe30id", abe30.abe30id)); // <- Se não informada condição de pagamento, a mesma não será considerada na busca
					 
		TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, parametros.toArray(new Parametro[parametros.size()]));
	
		def unit = 0;
		if(tm != null) {
			unit = tm.getBigDecimal("abe4001preco");
		}
		if(unit == null) unit = 0;
		ccb0101.ccb0101unit = unit;
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPV_PREVENDA;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODgifQ==