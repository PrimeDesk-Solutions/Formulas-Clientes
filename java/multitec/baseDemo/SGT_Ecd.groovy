package multitec.baseDemo;

import br.com.multitec.utils.TextFile
import sam.dicdados.FormulaTipo;
import sam.server.samdev.formula.FormulaBase;

public class SGT_Ecd extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ECD;
	}
	
	@Override
	public void executar() {
		
		TextFile txt = new TextFile("|");
		    
	    txt.print("0000");
		txt.print("MODELO");
		txt.print("DEMONSTRACAO");
	    txt.newLine();
	    
		put("dadosArquivo", txt);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTYifQ==