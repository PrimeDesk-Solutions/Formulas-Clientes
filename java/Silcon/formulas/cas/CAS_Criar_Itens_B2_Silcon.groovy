package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ab.Abe10
import sam.model.entities.ab.Abe1001
import sam.model.entities.ab.Abm01;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

public class CAS_Criar_Itens_B2_Silcon extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        try{
            List<Abm01> abm01s = buscarItensEmpresa();
            Abe10 abe10 = getSession().createCriteria(Abe10.class).addWhere(Criterions.eq("abe10ent", 411628)).get(ColumnType.ENTITY);

            for(abm01 in abm01s){
                Abe1001 abe1001 = new Abe1001();
                abe1001.setAbe1001conf(abe10);
                abe1001.setAbe1001item(abm01);
                abe1001.setAbe1001codigo(abm01.abm01codigo);
                abe1001.setAbe1001descr(abm01.abm01descr);

                getSession().persist(abe1001)
            }

        }catch(Exception e){
            interromper("Falha ao gravar item no repositório " + e.getMessage())
        }
    }

    private List<Abm01> buscarItensEmpresa(){
        return getSession().createQuery("SELECT abm01id, abm01tipo, abm01codigo, abm01descr FROM abm01 WHERE abm01tipo = 1 AND abm01gc = 1075797 AND abm01grupo <> 1 ORDER BY abm01tipo, abm01codigo").getList(ColumnType.ENTITY)
    }
}