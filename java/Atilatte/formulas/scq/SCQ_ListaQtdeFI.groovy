package Atilatte.formulas.scq

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

		// Cria apenas uma ficha
		listaQtdeFI.add(quantidade);
		
		put("listaQtdeFI", listaQtdeFI);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTIifQ==