package multitec.baseDemo;

import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.Scale;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ec.Ecc01
import sam.model.entities.ec.Ecc0101
import sam.server.samdev.formula.FormulaBase;

public class SAP_FatorCIAP extends FormulaBase {
	private Ecc0101 ecc0101;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CIAP;
	}
  
	public void executar(){
		ecc0101 = (Ecc0101)get("ecc0101"); 
    
		if(ecc0101 == null) throw new ValidacaoException("Necessário informar o registro de fator a ser calculado.");
    
		Ecc01 ecc01 = ecc0101.getEcc0101ficha();
    
		BigDecimal fator = BigDecimal.ZERO;
    
		TableMap mapJson = ecc0101.getEcc0101json() == null ? new TableMap() : ecc0101.getEcc0101json();
    
		BigDecimal vlrcontabil = mapJson.getBigDecimal("vlr_valorcontabil");
		BigDecimal isentas = mapJson.getBigDecimal("icm_isento");
		BigDecimal outras = mapJson.getBigDecimal("icm_outras");
		BigDecimal ipi = mapJson.getBigDecimal("ipi_ipi");
		BigDecimal bcicms = mapJson.getBigDecimal("icm_bc");
    
		if(vlrcontabil != null && vlrcontabil.compareTo(new BigDecimal(0)) != 0) {
      
			if(ecc01.getEcc01modelo().equals(0)){ //Modelo B
				if(isentas == null) isentas = BigDecimal.ZERO;
				if(ipi == null) ipi = BigDecimal.ZERO;
        
				fator = isentas.add(ipi);
                
			}else{ //Modelo D
				if(bcicms == null) bcicms = BigDecimal.ZERO;
				if(outras == null) outras = BigDecimal.ZERO;
        
				fator = bcicms.add(outras);
			}
      
			fator = fator.divide(vlrcontabil, Scale.ROUND_6);
		}
    
		ecc0101.setEcc0101fator(fator);
		BigDecimal icmsmes = DecimalUtils.create(ecc0101.getEcc0101icms().multiply(fator)).round(2).get();
		ecc0101.setEcc0101icms(icmsmes);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTkifQ==