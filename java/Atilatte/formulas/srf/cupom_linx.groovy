package Atilatte.formulas.srf

import br.com.multiorm.criteria.criterion.Criterion
import br.com.multitec.utils.collections.TableMap
import sam.dto.cgs.ParcelaDto
import sam.model.entities.ab.Abf4001;
import sam.model.entities.ea.Eaa01;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0113
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ab.Abf40
import sam.model.entities.ea.Eaa01131;
import sam.model.entities.ab.Abb01






public class cupom_linx extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_DOCUMENTOS;
    }

    @Override
    public void executar() {
        Eaa01 eaa01 = (Eaa01)get("eaa01");
        Abf40 abf40
        Abf4001 abf4001
        def txFormaPagamento = 0

        // Central Documento
        Abb01 abb01 = eaa01.eaa01central;

	   // Campos Livres
	   TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();

	   // Desconto Documento
	   def descontoDoc = jsonEaa01.getBigDecimal_Zero("desconto");

	   // Total Bruto Documento
	   def totBruto = eaa01.eaa01totDoc + descontoDoc;
	   
        // Altera as parcelas do financeiro para gerar quitados
        for(Eaa0113 eaa0113 in eaa01.eaa0113s){
            eaa0113.eaa0113docFin = 2
            
            TableMap tmFinanc = eaa0113.eaa0113json != null ? eaa0113.eaa0113json : new TableMap();

            tmFinanc.put("desconto", descontoDoc * -1 );
            tmFinanc.put("total_doc_sem_imposto", totBruto)
            
		  eaa0113.eaa0113json = tmFinanc;
        }

    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjAifQ==