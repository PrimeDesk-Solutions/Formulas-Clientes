package During.formulas.scf;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa1001;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;
import sam.server.scf.service.SCFService;

/**
 * Fórmula para manipular Movimento Financeiro  
 *
 */	
public class SCF_Movimento extends FormulaBase{

	private Daa1001 daa1001;
	
	@Override 
	public void executar() {
		SCFService scfService = instanciarService(SCFService.class);
		daa1001 = (Daa1001) get("daa1001");
		TableMap mapJson = daa1001.daa1001json == null ? new TableMap() : daa1001.daa1001json;
		
		Daa01 daa01 = null;
		if(daa1001.daa1001central != null && daa1001.daa1001central.abb01id != null) daa01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Daa01 WHERE daa01central = :abb01id ", Parametro.criar("abb01id", daa1001.daa1001central.abb01id));
		daa1001 = get("daa1001");
		def valor = daa1001.daa1001valor;
		
		//Juros = juros * qtd dias em atraso
		//Multa: considerar multa somente se estiver em atraso
		def diasAtraso = scfService.calculaDiasDeAtraso(daa1001);
//		if(diasAtraso <= 0){
//			mapJson.put("juros", 0.000000);
//			mapJson.put("multa", 0.000000);
//		}
		def juros = mapJson.getBigDecimal_Zero("juros");
		def multa = mapJson.getBigDecimal_Zero("multa");
		
		
		if (diasAtraso > 0 && mapJson.getInteger("jurosAjustado") != 1) {
			juros = mapJson.getBigDecimal_Zero("juros") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("juros") * diasAtraso, 2);
			multa = mapJson.getBigDecimal_Zero("multa") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("multa"), 2);
			mapJson.put("desconto", 0.000000);
			mapJson.put("jurosAjustado", 1);
		}
		
		//Encargos
		def encargos = mapJson.getBigDecimal_Zero("encargos") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("encargos"), 2);

		//Desconto: considerar desconto somente quando a data de pagamento for menor ou igual a data limite para desconto
		def desconto = 0;
		LocalDate dtLimDesc = mapJson.getDate("dt_limite_desc");
		
		if (dtLimDesc == null  || DateUtils.dateDiff(dtLimDesc, daa1001.daa1001dtPgto, ChronoUnit.DAYS) <= 0) {
			desconto = mapJson.getBigDecimal_Zero("desconto") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("desconto"), 2);
		}
		
		//Se documento está com valor parcial, ajusta os valores de JMED também parcialmente
		if(daa01 != null && !valor.equals(daa01.getDaa01valor()) && mapJson.getInteger("ajusteParcial") != 1 ) {
			def fatorParcial = round(valor / daa01.getDaa01valor(), 6);
			
			if(juros != 0) juros = round(juros * fatorParcial, 2);
			if(multa != 0) multa = round(multa * fatorParcial, 2);
			if(encargos != 0) encargos = round(encargos * fatorParcial, 2);
			if(desconto != 0) desconto = round(desconto * fatorParcial, 2);
			mapJson.put("ajusteParcial", 1);
		}
		
		//Setar JMED calculados, nos campos livres de quitação
		def jurosq = juros; //mapJson.getBigDecimal_Zero("juros") == 0 ? juros : round(mapJson.getBigDecimal_Zero("juros"), 2);
		
		mapJson.put("juros", jurosq);

		def multaq = multa;//mapJson.getBigDecimal_Zero("multa") == 0 ? multa : round(mapJson.getBigDecimal_Zero("multa"), 2);
		mapJson.put("multa", multaq);

		def encargosq = mapJson.getBigDecimal_Zero("encargos") == 0 ? encargos : round(mapJson.getBigDecimal_Zero("encargos"), 2);
		mapJson.put("encargos", encargosq);
		
		BigDecimal descontoq = mapJson.getBigDecimal_Zero("desconto") == 0 ? desconto : round(mapJson.getBigDecimal_Zero("desconto"), 2);
		if(descontoq != null) descontoq = descontoq.abs() * -1
		mapJson.put("desconto", descontoq);
				
		def valorLiquido = valor + jurosq + encargosq + multaq + descontoq;
		
//		if(jurosq != null) valorLiquido = valorLiquido + jurosq;
//		if(multaq != null) valorLiquido = valorLiquido + multaq;
//		if(encargosq != null) valorLiquido = valorLiquido + encargosq;
//		if(descontoq != null) valorLiquido = valorLiquido + descontoq;
		
		daa1001.daa1001liquido = round(valorLiquido, 2);
		
	}
	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCF_LCTOS_DE_MOVIMENTO; 
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDgifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDgifQ==