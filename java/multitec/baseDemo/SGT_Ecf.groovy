package multitec.baseDemo;

import br.com.multitec.utils.TextFile
import sam.dicdados.FormulaTipo;
import sam.server.samdev.formula.FormulaBase;

public class SGT_Ecf extends FormulaBase{
		  
	@Override
	public void executar() {
		TextFile txt = new TextFile("|");
		
		txt.print("0000");
		txt.print("MODELO");
		txt.print("DEMONSTRACAO");
		txt.newLine();
		
		put("dadosArquivo", txt);
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ECF;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTcifQ==