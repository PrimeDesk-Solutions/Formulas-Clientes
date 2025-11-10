package multitec.formulas.sgt.apuracoes

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ed.Edb01
import sam.server.samdev.formula.FormulaBase

class Apuracao_EFD_ICMS_ST_ManipulacaoCampos extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_CAMPOS_APURACAO;
	}
	
	@Override
	public void executar() {
		Edb01 edb01 = get("edb01");
		if(edb01 == null) return;
		
		TableMap edb01json = edb01.edb01json;
		if (edb01json == null) return;
		
		selecionarAlinhamento("0033");
		
		def saldoDeb = edb01json.getBigDecimal_Zero(getCampo("0", "debSaidas")) + edb01json.getBigDecimal_Zero(getCampo("0", "outrosDeb"));

		def saldoCred = edb01json.getBigDecimal_Zero(getCampo("0", "credAnt")) + edb01json.getBigDecimal_Zero(getCampo("0", "credEntradas")) + edb01json.getBigDecimal_Zero(getCampo("0", "outrosCred"));

        def saldo = saldoDeb - saldoCred;
		 
		edb01json.put(getCampo("0", "saldoCredor"), saldo < 0 ? saldo.abs() : 0);
		
		edb01json.put(getCampo("0", "saldoDevedor"), saldo >= 0 ? saldo : 0);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDIifQ==