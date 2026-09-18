package multitec.formulas.scf;

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
public class Movimento extends FormulaBase{

	private Daa1001 daa1001;
	
	@Override 
	public void executar() {
		SCFService scfService = instanciarService(SCFService.class);
		daa1001 = (Daa1001) get("daa1001");
		TableMap mapJson = daa1001.daa1001json == null ? new TableMap() : daa1001.daa1001json;
		
		Daa01 daa01 = null;
		if(daa1001.daa1001central != null && daa1001.daa1001central.abb01id != null)daa01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Daa01 WHERE daa01central = :abb01id ", Parametro.criar("abb01id", daa1001.daa1001central.abb01id));
		
		def valor = daa1001.daa1001valor;

		//Juros = juros * qtd dias em atraso
		//Multa: considerar multa somente se estiver em atraso
		def juros = null;
		def multa = null;
		def diasAtraso = scfService.calculaDiasDeAtraso(daa1001);
		if (diasAtraso > 0) {
			juros = mapJson.getBigDecimal("juros") == null ? null : round(mapJson.getBigDecimal("juros") * diasAtraso, 2);
			multa = mapJson.getBigDecimal("multa") == null ? null : round(mapJson.getBigDecimal("multa"), 2);
		}

		//Encargos
		def encargos = mapJson.getBigDecimal("encargos") == null ? null : round(mapJson.getBigDecimal("encargos"), 2);

		//Desconto: considerar desconto somente quando a data de pagamento for menor ou igual a data limite para desconto
		def desconto = null;
		LocalDate dtLimDesc = mapJson.getDate("dtlimdesc");
		if (dtLimDesc == null  || DateUtils.dateDiff(dtLimDesc, daa1001.daa1001dtPgto, ChronoUnit.DAYS) <= 0) {
			desconto = mapJson.getBigDecimal("desconto") == null ? null : round(mapJson.getBigDecimal("desconto"), 2);
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
		def jurosq = mapJson.getBigDecimal("jurosq") == null ? juros : round(mapJson.getBigDecimal("jurosq"), 2);
		mapJson.put("jurosq", jurosq);

		def multaq = mapJson.getBigDecimal("multaq") == null ? multa : round(mapJson.getBigDecimal("multaq"), 2);
		mapJson.put("multaq", multaq);

		def encargosq = mapJson.getBigDecimal("encargosq") == null ? encargos : round(mapJson.getBigDecimal("encargosq"), 2);
		mapJson.put("encargosq", encargosq);
		
		BigDecimal descontoq = mapJson.getBigDecimal("descontoq") == null ? desconto : round(mapJson.getBigDecimal("descontoq"), 2);
		if(descontoq != null) descontoq = descontoq.abs() * -1
		mapJson.put("descontoq", descontoq);
				
		//def valorLiquido = valor + jurosq + encargosq + multaq + descontoq;
		def valorLiquido = valor;
		if(jurosq != null) valorLiquido = valorLiquido + jurosq;
		if(multaq != null) valorLiquido = valorLiquido + multaq;
		if(encargosq != null) valorLiquido = valorLiquido + encargosq;
		if(descontoq != null) valorLiquido = valorLiquido + descontoq;
		
		daa1001.daa1001liquido = round(valorLiquido, 2);
	}
	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCF_LCTOS_DE_MOVIMENTO; 
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDgifQ==