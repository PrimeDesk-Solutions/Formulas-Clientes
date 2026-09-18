package During.formulas.sca;

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import sam.model.entities.aa.Aap18;
import sam.model.entities.ab.Abh08;
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fc.Fca10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;

public class Dia_Abonado extends FormulaBase {
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.PONTOS;
	}
	
	@Override
	public void executar() {
		Fca10 fca10 = get("fca10");
		
		//Inicializando os campos
		fca10.fca10folha00 = 0;		fca10.fca10eve00 = null;
		fca10.fca10folha01 = 0;		fca10.fca10eve01 = null;
		fca10.fca10folha02 = 0;		fca10.fca10eve02 = null;
		fca10.fca10folha03 = 0;		fca10.fca10eve03 = null;
		fca10.fca10folha04 = 0;		fca10.fca10eve04 = null;
		fca10.fca10folha05 = 0;		fca10.fca10eve05 = null;
		fca10.fca10folha06 = 0;		fca10.fca10eve06 = null;
		fca10.fca10folha07 = 0;		fca10.fca10eve07 = null;
		fca10.fca10folha08 = 0;		fca10.fca10eve08 = null;
		fca10.fca10folha09 = 0;		fca10.fca10eve09 = null;
		fca10.fca10folha10 = 0;		fca10.fca10eve10 = null;
		fca10.fca10folha11 = 0;		fca10.fca10eve11 = null;
		fca10.fca10folha12 = 0;		fca10.fca10eve12 = null;
		fca10.fca10folha13 = 0;		fca10.fca10eve13 = null;
		fca10.fca10folha14 = 0;		fca10.fca10eve14 = null;
		fca10.fca10folha15 = 0;		fca10.fca10eve15 = null;
		fca10.fca10folha16 = 0;		fca10.fca10eve16 = null;
		fca10.fca10folha17 = 0;		fca10.fca10eve17 = null;
		fca10.fca10folha18 = 0;		fca10.fca10eve18 = null;
		fca10.fca10folha19 = 0;		fca10.fca10eve19 = null;
		
		//Horas Trabalhadas
		Abh80 abh80 = buscarTrabalhadorPorId(fca10.fca10trab.abh80id);
		Aap18 aap18 = buscarUnidadeDePagamentoPorId(abh80.abh80unidPagto.aap18id);
		
		if(aap18.aap18codigo == "1") { //Horista
			fca10.fca10folha01 = fca10.fca10horDiu;  
			if(fca10.fca10horDiu != 0) fca10.fca10eve01 = buscarEventoPorCodigo("1002");
		}else if(aap18.aap18codigo == "5" || aap18.aap18codigo == "6" || aap18.aap18codigo == "7" || aap18.aap18codigo == "8") { //mensalista
				fca10.fca10folha01 = fca10.fca10horDiu;
				if(fca10.fca10horDiu != 0) fca10.fca10eve01 = buscarEventoPorCodigo("1309");			    
		}		

		//Adicional Noturno
		def fator = 1.142858;
		def adicionalNot = (fca10.fca10horNot + fca10.fca10heNot) * fator; 

		if(adicionalNot > 0) {
			fca10.fca10folha03 = adicionalNot;
			if(adicionalNot != 0) fca10.fca10eve03 = buscarEventoPorCodigo("1012");
		}
		
		//DSR
		if(aap18.aap18codigo == "1") { //Horista
			fca10.fca10folha02 = fca10.fca10complem0;
			if(fca10.fca10complem0 != 0) fca10.fca10eve02 = buscarEventoPorCodigo("1003");
		}else if(aap18.aap18codigo == "5" || aap18.aap18codigo == "6" || aap18.aap18codigo == "7" || aap18.aap18codigo == "8") { //mensalista
				fca10.fca10folha02 = fca10.fca10complem0;
				if(fca10.fca10complem0 != 0) fca10.fca10eve02 = buscarEventoPorCodigo("1310");
		}	
						
		Long idabh08 = fca10.getFca10tpDia().getAbh08id();
		String tipoDia = buscarTipoDeDiaPorId(idabh08).abh08codigo;

		//limite máxima de 10 minutos diários diurno
		if(fca10.fca10heDiu > 0 && fca10.fca10heDiu <= 10){
		   fca10.fca10folha06 = fca10.fca10heDiu;
	 	   if(fca10.fca10heDiu != 0) fca10.fca10eve06 = buscarEventoPorCodigo("1307");
		}
		//limite máxima de 10 minutos diários noturno
		//if(fca10.fca10heNot > 0 && fca10.fca10heNot <= 10){
		//   fca10.fca10folha07 = fca10.fca10heNot;
	 	//   if(fca10.fca10heNot != 0) fca10.fca10eve07 = buscarEventoPorCodigo("1308");
		//}	
				
		//Horas extras diurna
		if(fca10.fca10heDiu > 10 && tipoDia == "00") { //Dias normais
			fca10.fca10folha04 = fca10.fca10heDiu;
			if(fca10.fca10heDiu != 0) fca10.fca10eve04 = buscarEventoPorCodigo("1301");
		}

		//Horas extras Noturnas
		if(fca10.fca10heNot > 10 && tipoDia == "00") { //Dias normais Noturno
			fca10.fca10folha05 = fca10.fca10heNot;
			if(fca10.fca10heNot != 0) fca10.fca10eve05 = buscarEventoPorCodigo("1302");
		}

		//Somar horas extras diurnas com horas extras noturnas
		def horasdn = fca10.fca10heDiu + fca10.fca10heNot;
		
		//Horas Extras divisão diaria, as 08 primeiras fator 100% restantes 150%
		if(horasdn > 480 && (tipoDia == "01" || tipoDia == "02" || tipoDia == "03")){ 
			horasdn = horasdn - 480;

			fca10.fca10folha10 = 480;
			fca10.fca10eve10 = buscarEventoPorCodigo("1323");

			fca10.fca10folha11 = horasdn;
			fca10.fca10eve11 = buscarEventoPorCodigo("1353");
		}else if(horasdn > 10 && (tipoDia == "01" || tipoDia == "02" || tipoDia == "03")){
				fca10.fca10folha10 = fca10.fca10heDiu + fca10.fca10heNot;
				fca10.fca10eve10 = buscarEventoPorCodigo("1323");				
		}	
		
		//Justificar faltas como Ferias e não parar na consistência do ponto
		if(fca10.fca10horFalt > 0) {
			fca10.fca10folha00 = fca10.fca10horFalt;
			fca10.fca10eve00 = buscarEventoPorCodigo("1004");
			fca10.fca10consistente = 1;
		}				
	}

	/**
	 * Métodos auxiliares
	 */
	
	private Abh80 buscarTrabalhadorPorId(Long abh80id) {
		return getSession().createCriteria(Abh80.class)
				.addWhere(Criterions.eq("abh80id", abh80id))
				.get(ColumnType.ENTITY);
	}
	
	private Aap18 buscarUnidadeDePagamentoPorId(Long aap18id) {
		return getSession().createCriteria(Aap18.class)
				.addWhere(Criterions.eq("aap18id", aap18id))
				.get(ColumnType.ENTITY);
	}
	
	private Abh21 buscarEventoPorCodigo(String abh21codigo) {
		return getSession().createCriteria(Abh21.class)
				.addFields("abh21id, abh21codigo, abh21nome")
				.addWhere(Criterions.eq("abh21codigo", abh21codigo))
				.get(ColumnType.ENTITY);
	}
	
	private Abh08 buscarTipoDeDiaPorId(Long abh08id) {
		return getSession().createCriteria(Abh08.class)
				.addFields("abh08id, abh08codigo")
				.addWhere(Criterions.eq("abh08id", abh08id))
				.get(ColumnType.ENTITY);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjAifQ==