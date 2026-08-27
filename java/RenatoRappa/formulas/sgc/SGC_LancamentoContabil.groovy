package RenatoRappa.formulas.sgc;

import java.time.format.DateTimeFormatter;
import sam.model.entities.ab.Abb01
import sam.model.entities.da.Daa1001
import sam.model.entities.da.Daa10012
import sam.model.entities.da.Dab20;
import sam.model.entities.aa.Aah01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abh21;
import sam.model.entities.eb.Ebb05;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.Parametro;
import br.com.multiorm.criteria.criterion.Criterions


public class SGC_LancamentoContabil extends FormulaBase {
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

  		Integer numCheque = 0;
  		
		//Disponibilizar dados do documento
		if(ebb05.ebb05central != null){

			// Central Documentos
			Abb01 abb01 = getSession().get(Abb01.class, ebb05.ebb05central.abb01id )

			// Lançamento Financeiro
			Daa1001 daa1001 = abb01 != null ? getSession().get(Daa1001.class, Criterions.eq("daa1001central", abb01.abb01id) ) : null;

			// Pagamentos
			Daa10012 daa10012 = daa1001 != null ? getSession().get(Daa10012.class, Criterions.eq("daa10012lct", daa1001.daa1001id)) : null;

			// Cheque
			Dab20 dab20 = daa10012 != null && daa10012.daa10012cheque != null ? getSession().get(Dab20.class,"dab20id, dab20num", Criterions.eq("dab20id", daa10012.daa10012cheque.dab20id)) : null;

			if(dab20 != null) numCheque = dab20.dab20num;
			
			if(ebb05.ebb05central.abb01tipo != null){
				aah01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aah01 WHERE aah01id = :id", Parametro.criar("id", ebb05.ebb05central.abb01tipo.aah01id));
			}
    
			//Disponibilizar dados da entidade
			if(ebb05.ebb05central.abb01ent != null){
				abe01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe01 WHERE abe01id = :id " + getSamWhere().getWherePadrao("AND", Abe01.class), Parametro.criar("id", ebb05.ebb05central.abb01ent.abe01id));
			}
		}

    		// Processamento dos Históricos
		historico = historico.replace("\$1", aah01 == null ? "" : aah01.getAah01nome()); //Nome do tipo de documento
		historico = historico.replace("\$2", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01num == null ? "" : ""+ebb05.ebb05central.abb01num); //Número do documento na central
		historico = historico.replace("\$3", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01serie == null ? "" : ebb05.ebb05central.abb01serie); //Série do documento na central
		historico = historico.replace("\$4", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01data == null ? "" : DateTimeFormatter.ofPattern("dd/MM/yyyy").format(ebb05.ebb05central.abb01data)); //Data do documento na central
		historico = historico.replace("\$5", abe01 == null ? "" : abe01.abe01nome); //Nome da entidade do documento na central
		historico = historico.replace("\$6", ebb05.ebb05central == null ? "" : ebb05.ebb05central.abb01parcela == null ? "" : ebb05.ebb05central.abb01parcela )
		historico = historico.replace("\$7", numCheque == 0 ? "" : numCheque.toString());
		historico = historico.replace("\$9", abh21 == null ? "" : abh21.abh21nome); //Nome do evento
		
		//Histórico final 
		ebb05.ebb05historico = historico;    	
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTUifQ==