/*
    Script utilizado para replicar os dados de uma empresa principal para as filiais. Excluir a Abm0101 das demais empresas e deixar somente da principal.
 */
package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abm0101
import sam.model.entities.aa.Aac10


import javax.swing.text.html.parser.Entity


public class CAS_Criar_Abm0101_Entre_Empresas extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        List<Abm0101> abm0101s = buscarListaAbm0101();
        replicarAbm0101(abm0101s);
    }

    private List<Abm0101> buscarListaAbm0101(){

        return getSession().createCriteria(Abm0101.class).getList(ColumnType.ENTITY);
    }
    private void replicarAbm0101(List<Abm0101> abm0101s){

        List<Aac10> aac10s = buscarListaAac10();
        for(abm0101 in abm0101s){
            for(aac10 in aac10s){
                Abm0101 abm0101new = new Abm0101();

                abm0101new.setAbm0101item(abm0101.abm0101item);
                abm0101new.setAbm0101empresa(aac10);
                abm0101new.setAbm0101valores(abm0101.abm0101valores);
                abm0101new.setAbm0101estoque(abm0101.abm0101estoque);
                abm0101new.setAbm0101fiscal(abm0101.abm0101fiscal);
                abm0101new.setAbm0101comercial(abm0101.abm0101comercial);
                abm0101new.setAbm0101unidDiv(abm0101.abm0101unidDiv);
                abm0101new.setAbm0101montagem(abm0101.abm0101montagem);
                abm0101new.setAbm0101cest(abm0101.abm0101cest);
                abm0101new.setAbm0101umt(abm0101.abm0101umt);
                abm0101new.setAbm0101gtinTrib(abm0101.abm0101gtinTrib);
                abm0101new.setAbm0101ncm(abm0101.abm0101ncm);
                abm0101new.setAbm0101pmu(abm0101.abm0101pmu);
                abm0101new.setAbm0101calcNiveis(abm0101.abm0101calcNiveis);
                abm0101new.setAbm0101estMax(abm0101.abm0101estMax);
                abm0101new.setAbm0101estMin(abm0101.abm0101estMin);
                abm0101new.setAbm0101estSeg(abm0101.abm0101estSeg);
                abm0101new.setAbm0101ptoPed(abm0101.abm0101ptoPed);
                abm0101new.setAbm0101json(abm0101.abm0101json);
                abm0101new.setAbm0101itemTerc(abm0101.abm0101itemTerc);
                abm0101new.setAbm0101eDepto(abm0101.abm0101eDepto);
                abm0101new.setAbm0101eNat(abm0101.abm0101eNat);
                abm0101new.setAbm0101sDepto(abm0101.abm0101sDepto);
                abm0101new.setAbm0101sNat(abm0101.abm0101sNat);
                abm0101new.setAbm0101wmsMassa(abm0101.abm0101wmsMassa);
                abm0101new.setAbm0101sgpPrev(abm0101.abm0101sgpPrev);
                abm0101new.setAbm0101sgpCpra(abm0101.abm0101sgpCpra);
                abm0101new.setAbm0101empresa(aac10);

                getSession().persist(abm0101new);
            }
        }
    }
    private List<Aac10> buscarListaAac10(){
        return getSession().createCriteria(Aac10.class).addWhere(Criterions.in("aac10codigo", List.of("000", "002"))).getList(ColumnType.ENTITY)
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=