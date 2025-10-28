package multitec.formulas.sgt.apuracoes

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ed.Edb01
import sam.server.samdev.formula.FormulaBase

class Apuracao_LF_ManipulacaoCampos extends FormulaBase {

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
		
		selecionarAlinhamento("0040");
		
		//Sub total saídas: débito do imposto + outros débitos + estorno de crédito
		def subTotalSaidas = edb01json.getBigDecimal_Zero(getCampo("0", "debImp")) + edb01json.getBigDecimal_Zero(getCampo("0", "outrosDeb")) + edb01json.getBigDecimal_Zero(getCampo("0", "estCred"));
		edb01json.put(getCampo("0", "subTotSai"), subTotalSaidas);
		
		//Sub total entradas: crédito do imposto + outros créditos + estorno de débito
		def subTotalEntradas = edb01json.getBigDecimal_Zero(getCampo("0", "credImp")) + edb01json.getBigDecimal_Zero(getCampo("0", "outrosCred")) + edb01json.getBigDecimal_Zero(getCampo("0", "estDeb"));
		edb01json.put(getCampo("0", "subTotEnt"), subTotalEntradas);
		
		//Total: sub total entradas + saldo credor anterior
		def total = subTotalEntradas + edb01json.getBigDecimal_Zero(getCampo("0", "credAnt"));
		edb01json.put(getCampo("0", "total"), total);
		
		//Saldo devedor: sub total saídas - total entradas
		def saldoDevedor = 0;
		if(subTotalSaidas > subTotalEntradas) {
			saldoDevedor = subTotalSaidas - total;
		}
		edb01json.put(getCampo("0", "sdoDevedor"), saldoDevedor);
		
		//Imposto a recolher: Sdo devedor - deduções
		def impARecolher = saldoDevedor - edb01json.getBigDecimal_Zero(getCampo("0", "deducoes"));
		edb01json.put(getCampo("0", "impRecolher"), impARecolher);
		
		//Sdo credor: total entradas - sub-total saídas
		def saldoCredor = 0;
		if(subTotalSaidas <= total) {
			saldoCredor = total - subTotalSaidas;
		}
		edb01json.put(getCampo("0", "sdoCredor"), saldoCredor);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDIifQ==