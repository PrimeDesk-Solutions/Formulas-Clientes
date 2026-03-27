package Silcon.formulas.preGravar

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import sam.model.entities.ab.Abe01
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa01033;
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.server.samdev.utils.Parametro

public class Pre_Gravar_Altera_Rep_Devolucao extends FormulaBase{

    private Eaa01 eaa01;
    private Integer gravar = 1; //0-Não 1-Sim


    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
    }

    @Override
    public void executar() {
        eaa01 = get("eaa01");
        alterarRepresentanteDevolucao(eaa01)
        put("gravar", gravar);
    }
    private void alterarRepresentanteDevolucao(Eaa01 eaa01){
        // DOC ORIGEM == DOCUMENTO DA VENDA

        Eaa0103 eaa0103dev = eaa01.eaa0103s[0];

        Eaa0103 eaa0103origem = eaa0103dev.eaa01033s[0].eaa01033itemDoc;

        Eaa01 eaa01origem = eaa0103origem.eaa0103doc;

        if(eaa01origem != null){
            Abe01 rep0 = buscarIdRepresentanteDocOrigem(eaa01origem.eaa01id)

            eaa01.setEaa01rep0(rep0);
        }
    }
    private Abe01 buscarIdRepresentanteDocOrigem(Long idDoc){
        return getSession().createCriteria(Abe01.class)
                .addFields("abe01id")
                .addJoin(Joins.join("eaa01", "eaa01rep0 = abe01id").left(true))
                .addWhere(Criterions.eq("eaa01id", idDoc))
                .get(ColumnType.ENTITY);
    }
}