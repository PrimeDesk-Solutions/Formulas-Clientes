package Profer.formulas.scf;

import java.time.format.DateTimeFormatter;

import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aah01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.da.Dab10;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;

/**
 * 
 * Fórmula para manipular o Lançamento Financeiro
 *
 */
public class Lancamento extends FormulaBase{
	
	private Aah01 aah01;
	private Abe01 abe01;
	private Dab10 dab10;
	
	@Override
	public void executar() {
		dab10 = get("dab10");
		abe01 = null;
		aah01 = null;
		
		if(dab10.dab10historico != null) {
			def historico = dab10.dab10historico;
			
			//Disponibilizar dados do documento
			if(dab10.dab10central != null){
				if(dab10.dab10central.abb01tipo != null){
					aah01 = getAcessoAoBanco().buscarRegistroUnico("SELECT aah01id, aah01nome FROM Aah01 WHERE aah01id = :id", Parametro.criar("id", dab10.dab10central.abb01tipo.aah01id));
				}
	
				//Disponibilizar dados da entidade
				if(dab10.dab10central.abb01ent != null){
					abe01 = getAcessoAoBanco().buscarRegistroUnico("SELECT abe01id, abe01nome FROM Abe01 WHERE abe01id = :id " + getSamWhere().getWherePadrao("AND", Abe01.class), Parametro.criar("id", dab10.dab10central.abb01ent.abe01id));
				}
			}
	
			//Processamento dos coringas
			historico = historico.replace("\$1", aah01 == null ? "" : aah01.getAah01nome()); //Nome do tipo de documento
			historico = historico.replace("\$2", dab10.dab10central == null ? "" : dab10.dab10central.abb01num == null ? "" : ""+dab10.dab10central.abb01num); //Número do documento na central
			historico = historico.replace("\$3", dab10.dab10central == null ? "" : dab10.dab10central.abb01serie == null ? "" : dab10.dab10central.abb01serie); //Série do documento na central
			historico = historico.replace("\$4", DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dab10.dab10data)); //Data do lançamento
			historico = historico.replace("\$5", abe01 == null ? "" : abe01.abe01nome == null ? "" : abe01.abe01nome); //Nome da entidade do documento na central
			historico = historico.replace("\$6", dab10.dab10central == null ? "" : dab10.dab10central.abb01parcela == null ? "" : dab10.dab10central.abb01parcela); //Parcela do documento na central

			dab10.dab10historico = historico;
		}

	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SCF;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDkifQ==