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

		//Juros = juros * qtd dias em atraso
		//Multa: considerar multa somente se estiver em atraso
		def juros = null;
		def multa = null;
		def diasAtraso = scfService.calculaDiasDeAtraso(daa01);
		if (diasAtraso > 0) {
			juros = mapJson.getBigDecimal("juros") == null ? null : mapJson.getBigDecimal("juros") * diasAtraso;
			multa = mapJson.getBigDecimal("multa") == null ? null : mapJson.getBigDecimal("multa");
		}

		//Encargos
		def encargos = mapJson.getBigDecimal("encargos") == null ? null : mapJson.getBigDecimal("encargos");

		//Desconto: considerar desconto somente quando a data de pagamento for menor ou igual a data limite para desconto
		def desconto = null;
		LocalDate dtLimDesc = mapJson.getDate("dtlimdesc");
		if (dtLimDesc == null  || DateUtils.dateDiff(dtLimDesc, daa01.daa01dtPgto, ChronoUnit.DAYS) <= 0) {
			desconto = mapJson.getBigDecimal("desconto") == null ? null : mapJson.getBigDecimal("desconto");
		}

		//Se documento está com valor parcial, ajusta os valores de JMED também parcialmente
		if(daa01 != null && !valor.equals(daa01.getDaa01valor())) {
			def fatorParcial = round(valor / daa01.getDaa01valor(), 6);
			
			if(juros != null) juros = round(juros * fatorParcial, 2);
			if(multa != null) multa = round(multa * fatorParcial, 2);
			if(encargos != null) encargos = round(encargos * fatorParcial, 2);
			if(desconto != null) desconto = round(desconto * fatorParcial, 2);
		}
		
		//Setar JMED calculados, nos campos livres de quitação
		def jurosq = mapJson.getBigDecimal("jurosq") == null ? juros : mapJson.getBigDecimal("jurosq");
		mapJson.put("jurosq", jurosq);

		def multaq = mapJson.getBigDecimal("multaq") == null ? multa : mapJson.getBigDecimal("multaq");
		mapJson.put("multaq", multaq);

		def encargosq = mapJson.getBigDecimal("encargosq") == null ? encargos : mapJson.getBigDecimal("encargosq");
		mapJson.put("encargosq", encargosq);
		
		BigDecimal descontoq = mapJson.getBigDecimal("descontoq") == null ? desconto : mapJson.getBigDecimal("descontoq");
		if(descontoq != null) descontoq = descontoq.abs() * -1
		mapJson.put("descontoq", descontoq);
				
		//def valorLiquido = valor + jurosq + encargosq + multaq + descontoq;
		def valorLiquido = valor;
		if(jurosq != null) valorLiquido = valorLiquido + jurosq;
		if(multaq != null) valorLiquido = valorLiquido + multaq;
		if(encargosq != null) valorLiquido = valorLiquido + encargosq;
		if(descontoq != null) valorLiquido = valorLiquido + descontoq;
		
		daa01.daa01liquido = valorLiquido;
	}
	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCF_DOCUMENTOS; 
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDAifQ==