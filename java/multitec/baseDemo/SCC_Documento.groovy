package multitec.baseDemo;

import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abb01
import sam.model.entities.dc.Dcb01
import sam.server.samdev.formula.FormulaBase

public class SCC_Documento extends FormulaBase{

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCC_DOCUMENTOS; 
	}

	@Override 
	public void executar() {
		Dcb01 dcb01 = get("dcb01");
		
		def dcb01json = dcb01.dcb01json;
		def desconto = dcb01json.getBigDecimal("descontoq");
		desconto = desconto == null ? 0 : desconto;
		
		Abb01 abb01 = getSession().get(Abb01.class, dcb01.dcb01central.abb01id)
		
		dcb01.dcb01bc = abb01.abb01valor_Zero;
		dcb01.dcb01bc = dcb01.dcb01bc - desconto;

		dcb01.dcb01valor = (dcb01.dcb01bc * dcb01.dcb01taxa) / 100;
		dcb01.dcb01valor = round(dcb01.dcb01valor, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzgifQ==