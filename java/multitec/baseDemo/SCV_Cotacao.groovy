package multitec.baseDemo;

import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.cb.Cbb11
import sam.model.entities.cb.Cbb1101;
import sam.server.samdev.formula.FormulaBase;

public class SCV_Cotacao extends FormulaBase {

	private Cbb11 cbb11;
	private Cbb1101 cbb1101;

	@Override
	public void executar() {
		cbb1101 = (Cbb1101)get("cbb1101");

		if(cbb1101 == null) throw new ValidacaoException("Necessário informar o item da cotação.");
		
		cbb11 = cbb1101.cbb1101cot;

		//Eleger o preço unitário
		def unit = 0;

		//Obter o preço unitário na tabela de preço indicada no cadastro do fornecedor
		def preco = buscarPrecoDoItemNaTabela();

		if(preco != 0) unit = preco;

		//Obter o preço unitário no cadastro do item
		if(unit == 0) {
			def valor = buscarUnitarioItem();
			if(valor != 0) unit = valor;
		}

		//Calculo do preço total
		def total = cbb1101.cbb1101qtC * unit;
		total = round(total, 2);
		cbb1101.cbb1101unit = unit;
		cbb1101.cbb1101total = total;

		//Preço (json) = Total
		TableMap mapJson = cbb1101.cbb1101json == null ? new TableMap() : cbb1101.cbb1101json;
		mapJson.put("preco", total);
		cbb1101.cbb1101json = mapJson;
	}

	BigDecimal buscarPrecoDoItemNaTabela() {
		def sql = " SELECT abe4001preco FROM Abe4001 " +
				" INNER JOIN Abe40 ON abe40id = abe4001tab " +
				" INNER JOIN Abe03 ON abe03tp = abe40id " +
				" INNER JOIN Abe01 ON abe01id = abe03ent " +
				" WHERE abe01id = :ent " +
				" AND abe4001item = :item ";

		def param1 = criarParametroSql("ent", cbb11.cbb11ent.abe01id);
		def param2 = criarParametroSql("item", cbb1101.cbb1101item.abm01id);

		return getAcessoAoBanco().obterBigDecimal(sql, param1, param2);
	}

	BigDecimal buscarUnitarioItem() {
		def sql = " SELECT jGet(abm0101json.preco) FROM Abm0101 WHERE abm0101item = :item ";
		return getAcessoAoBanco().obterBigDecimal(sql, criarParametroSql("item", cbb1101.cbb1101item.abm01id));
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_COTACOES;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzcifQ==