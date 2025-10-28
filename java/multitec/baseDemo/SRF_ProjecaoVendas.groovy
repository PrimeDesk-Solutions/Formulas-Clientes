package multitec.baseDemo;

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.ea.Eac0101;
import sam.server.samdev.formula.FormulaBase;

public class SRF_ProjecaoVendas extends FormulaBase {
	Eac0101 eac0101;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_PV;
	}
	
	@Override
	public void executar() {
		eac0101 = get("eac0101");
		
		def ipi = eac0101.eac0101valor * 0.10;
		def icm = eac0101.eac0101valor * 0.18;
		
		def json = new TableMap();
		json.put("ipi_ipi", ipi);
		json.put("icm_icm", icm);
		
		eac0101.eac0101json = json;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzMifQ==