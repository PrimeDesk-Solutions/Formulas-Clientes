package multitec.formulas.sca;

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aap18;
import sam.model.entities.ab.Abh08;
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fc.Fca10;
import sam.server.samdev.formula.FormulaBase;

public class Acessos extends FormulaBase {
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
		
		//Horas Trabalhadas - 00
		Abh80 abh80 = buscarTrabalhadorPorId(fca10.fca10trab.abh80id);
		Aap18 aap18 = buscarUnidadeDePagamentoPorId(abh80.abh80unidPagto.aap18id);
		
		if(aap18.aap18codigo == "1") { //Horista
			fca10.fca10folha00 = fca10.getFca10jorBru_Zero();
			fca10.fca10eve00 = buscarEventoPorCodigo("1002");
		}

		//Adicional Noturno - 01
		def fator = 1.1428571;
		def adicionalNot = (fca10.fca10horNot + fca10.fca10heNot) * fator;

		if(adicionalNot > 0) {
			fca10.fca10folha01 = adicionalNot;
			fca10.fca10eve01 = buscarEventoPorCodigo("1059");
		}
		
		//DSR - 02
		fca10.fca10folha02 = fca10.getFca10complem0_Zero();
		fca10.fca10eve02 = buscarEventoPorCodigo("1003");
		
		//Desconto de DSR - 03
		fca10.fca10folha03 = fca10.getFca10complem1_Zero();
		fca10.fca10eve03 = buscarEventoPorCodigo("2005");
		
		Long idabh08 = fca10.getFca10tpDia().getAbh08id();
		String tipoDia = buscarTipoDeDiaPorId(idabh08).abh08codigo;
		
		//Horas extras diurna - 04 e 05
		if(tipoDia == "00" || tipoDia == "03") {                       // 00-Normal, 03-Sábado
			fca10.fca10eve04 = buscarEventoPorCodigo("1022");          // HE 60%
			fca10.fca10folha04 = fca10.getFca10heDiu_Zero();
		}else {                                                        // 01-Domingo, 02-Feriado
			fca10.fca10eve05 = buscarEventoPorCodigo("1024");          // HE 100%
			fca10.fca10folha05 = fca10.getFca10heDiu_Zero();
		}

		//Horas extras noturna - 06 e 07
		if(tipoDia == "00" || tipoDia == "03") {                       // 00-Normal, 03-Sábado
			fca10.fca10eve06 = buscarEventoPorCodigo("1022");          // HE 60%
			fca10.fca10folha06 = fca10.getFca10heNot_Zero();
		}else {                                                        // 01-Domingo, 02-Feriado
			fca10.fca10eve07 = buscarEventoPorCodigo("1024");          // HE 100%
			fca10.fca10folha07 = fca10.getFca10heNot_Zero();
		}
				
		//Considerar até 10 minutos de falta como atraso - 10
		if(fca10.fca10horFalt_Zero <= 10) {
			fca10.fca10folha10 = fca10.fca10horFalt_Zero;
			fca10.fca10eve10 = buscarEventoPorCodigo("1301");
		}
		
		//Considerar acima de 10 minutos como falta descontada - 11
		if(fca10.fca10horFalt_Zero > 10) {
			fca10.fca10folha11 = fca10.fca10horFalt_Zero;
			fca10.fca10eve11 = buscarEventoPorCodigo("2004");
		}
	}

	/**
	 * Métodos auxiliares
	 */
	private Abh80 buscarTrabalhadorPorId(Long abh80id) {
		return getSession().createCriteria(Abh80.class)
				.addWhere(Criterions.eq("abh80id", abh80id))
				.addWhere(getSamWhere().getCritPadrao(Abh80.class))
				.get(ColumnType.ENTITY);
	}
	
	private Aap18 buscarUnidadeDePagamentoPorId(Long aap18id) {
		return getSession().createCriteria(Aap18.class)
				.addWhere(Criterions.eq("aap18id", aap18id))
				.addWhere(getSamWhere().getCritPadrao(Aap18.class))
				.get(ColumnType.ENTITY);
	}
	
	private Abh21 buscarEventoPorCodigo(String abh21codigo) {
		return getSession().createCriteria(Abh21.class)
				.addFields("abh21id, abh21codigo, abh21nome")
				.addWhere(Criterions.eq("abh21codigo", abh21codigo))
				.addWhere(getSamWhere().getCritPadrao(Abh21.class))
				.get(ColumnType.ENTITY);
	}
	
	private Abh08 buscarTipoDeDiaPorId(Long abh08id) {
		return getSession().createCriteria(Abh08.class)
				.addFields("abh08id, abh08codigo")
				.addWhere(Criterions.eq("abh08id", abh08id))
				.addWhere(getSamWhere().getCritPadrao(Abh08.class))
				.get(ColumnType.ENTITY);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjAifQ==