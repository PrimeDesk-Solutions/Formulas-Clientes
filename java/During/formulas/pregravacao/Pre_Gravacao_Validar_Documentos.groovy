package During.formulas.pregravacao

import br.com.multiorm.criteria.criterion.Criterion
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table
import org.apache.commons.collections4.functors.ComparatorPredicate
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa01039;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multiorm.criteria.criterion.Criterions

public class Pre_Gravacao_Validar_Documentos extends FormulaBase{

    private Eaa01 eaa01;
    private Integer gravar = 0; // 0-Não 1-Sim

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
    }

    @Override
    public void executar() {
        eaa01 = get("eaa01");

        verificaQuantidadeUnitario(eaa01);
        put("gravar", gravar);
    }

    private verificaQuantidadeUnitario(Eaa01 eaa01){
        for(Eaa0103 eaa0103 in  eaa01.eaa0103s){
            // Item do Documento
            Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

            // Itens Valores
            Abm0101 abm0101 = getSession().get(Abm0101.class, Criterions.eq("abm0101item", abm01.abm01id));

            // Campos Livres
            TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();

            if(eaa0103.eaa0103qtComl_Zero == 0) throw new ValidacaoException("Não é permitido salvar item do documento com quantidade Zero. Item: " + abm01.abm01codigo + " Sequência: " + eaa0103.eaa0103seq.toString());
            if(eaa0103.eaa0103unit_Zero == 0) throw new ValidacaoException("Não é permitido salvar item do documento com unitário Zero. Item: " + abm01.abm01codigo + " Sequência: " + eaa0103.eaa0103seq.toString());

            if(jsonEaa0103.getString("ft") == null || jsonEaa0103.getString("ft").isEmpty()) throw new ValidacaoException("Necessário preencher o campo FT no item: " + abm01.abm01codigo + " Sequência: " + eaa0103.eaa0103seq.toString())
        }
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==