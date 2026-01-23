package multitec.formulas.scf;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.da.Daa01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.scf.service.SCFService;

/**
 * 
 * Fórmula para manipular o documento financeiro no programa SCF0155 - Caixa Financeiro
 * 
 */
public class Caixa extends FormulaBase{

	private Daa01 daa01;
	
	
	@Override 
	public void executar() {
		SCFService scfService = instanciarService(SCFService.class);
		daa01 = (Daa01) get("daa01");
		TableMap mapJson = daa01.daa01json == null ? new TableMap() : daa01.daa01json;

		def valor = daa01.daa01valor;
        BigDecimal juros = mapJson.getBigDecimal_Zero("juros");
        BigDecimal multa = mapJson.getBigDecimal_Zero("multa");
        BigDecimal encargos = mapJson.getBigDecimal_Zero("encargos");
        BigDecimal desconto = mapJson.getBigDecimal_Zero("desconto");


        //Juros = juros * qtd dias em atraso
		//Multa: considerar multa somente se estiver em atraso
		def diasAtraso = scfService.calculaDiasDeAtraso(daa01);
		if (diasAtraso > 0 && mapJson.getInteger("calculouJuros") != 1 ) {
			juros = round(mapJson.getBigDecimal_Zero("juros") * diasAtraso,2);
			multa = round(mapJson.getBigDecimal_Zero("multa"),2);

            mapJson.put("calculouJuros", 1)
		}

		//Desconto: considerar desconto somente quando a data de pagamento for menor ou igual a data limite para desconto
        LocalDate dtLimDesc = mapJson.getDate("dt_limite_desc");
        if (dtLimDesc == null  || DateUtils.dateDiff(dtLimDesc, daa01.daa01dtPgto, ChronoUnit.DAYS) <= 0) { // Negativo -> Está dentro da data de desconto. Positivo -> Passou da data
			desconto = mapJson.getBigDecimal_Zero("desconto");
            juros = new BigDecimal(0);
            multa = new BigDecimal(0);
		}

		//Se documento está com valor parcial, ajusta os valores de JMED também parcialmente
		if(daa01 != null && !valor.equals(daa01.getDaa01valor())) {
			def fatorParcial = round(valor / daa01.getDaa01valor(), 6);
			
			juros = round(juros * fatorParcial, 2);
			multa = round(multa * fatorParcial, 2);
			encargos = round(encargos * fatorParcial, 2);
			desconto = round(desconto * fatorParcial, 2);
		}

        desconto = desconto.abs() * -1;
		
		//Setar JMED calculados, nos campos livres de quitação
		mapJson.put("juros", round(juros,2));
        mapJson.put("multa", round(multa,2));
        mapJson.put("encargos", round(encargos,2));
        mapJson.put("desconto", round(desconto,2));
				
		//def valorLiquido = valor + jurosq + encargosq + multaq + descontoq;
		BigDecimal valorLiquido = valor + juros + multa + encargos + desconto;

		daa01.daa01liquido = round(valorLiquido, 2);
    }
	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCF_DOCUMENTOS; 
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDAifQ==