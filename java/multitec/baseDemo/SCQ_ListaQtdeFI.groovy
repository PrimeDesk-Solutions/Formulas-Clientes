package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

public class SCQ_ListaQtdeFI extends FormulaBase {
	
	private Long abm01id;					//requerido
	private BigDecimal quantidade;			//requerido
	private Long abp10id;					//não requerido
	private Long abd01id;					//não requerido
	private Long abe01id;					//não requerido
	private String procInvoc;
	
	private List<BigDecimal> listaQtdeFI; 	//lista de quantidades de FI que deve ser preenchida e retornada
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCQ_QUANTIDADE_DE_FI;
	}
	
	@Override
	public void executar() {
		abm01id = get("abm01id");
		quantidade = get("quantidade");
		abp10id = get("abp10id");
		abd01id = get("abd01id");
		abe01id = get("abe01id");
		procInvoc = get("procInvoc");
		
		listaQtdeFI = get("listaQtdeFI");
		
		if(quantidade > 10.0) {
			//Se a quantidade for maior que 10, criará uma FI com quantidade 10 e outra com o restante 
			listaQtdeFI.add(10.0);
			listaQtdeFI.add(quantidade - 10.0);
		}else {
			//Se a quantidade for menor ou igual a 10, criará somente uma FI com a quantidade original
			listaQtdeFI.add(quantidade);
		}
		
		put("listaQtdeFI", listaQtdeFI);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTIifQ==