package multitec.formulas.sgc;

import java.time.format.DateTimeFormatter;

import sam.model.entities.aa.Aah01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abh21;
import sam.model.entities.eb.Ebb05;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.Parametro;

public class LancamentoContabil extends FormulaBase {
	private Ebb05 ebb05;
	private Aah01 aah01;
	private Abe01 abe01;
	private Abh21 abh21;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SGC;
	}
	
	public void executar(){
		ebb05 = (Ebb05)get("ebb05"); 
		aah01 = null;
		abe01 = null;
		abh21 = (Abh21)get("abh21");
  
		//Iniciar histórico
		def historico = ebb05.ebb05historico;
		if(historico == null) return;
  
		//Disponibilizar dados do documento
		if(ebb05.ebb05central != null){
			if(ebb05.ebb05central.abb01tipo != null){
				aah01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aah01 WHERE aah01id = :id", Parametro.criar("id", ebb05.ebb05central.abb01tipo.aah01id));
			}
    
			//Disponibilizar dados da entidade
			if(ebb05.ebb05central.abb01ent != null){
				abe01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe01 WHERE abe01id = :id " + getSamWhere().getWherePadrao("AND", Abe01.class), Parametro.criar("id", ebb05.ebb05central.abb01ent.abe01id));
			}
		}
    
		//Processamento dos coringas
		historico = historico.replace("\$1", aah01 == null ? "" : aah01.getAah01nome()); //Nome do tipo de documento
		historico = historico.replace("\$2", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01num == null ? "" : ""+ebb05.ebb05central.abb01num); //Número do documento na central
		historico = historico.replace("\$3", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01serie == null ? "" : ebb05.ebb05central.abb01serie); //Série do documento na central
		historico = historico.replace("\$4", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01data == null ? "" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(ebb05.ebb05central.abb01data)); //Data do documento na central
		historico = historico.replace("\$5", abe01 == null ? "" : abe01.abe01nome); //Nome da entidade do documento na central
		historico = historico.replace("\$6", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01parcela == null ? "" : ebb05.ebb05central.abb01parcela); //Parcela do documento na central
		historico = historico.replace("\$9", abh21 == null ? "" : abh21.abh21nome); //Nome do evento
 
		//Histórico final 
		ebb05.ebb05historico = historico;    	
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTUifQ==