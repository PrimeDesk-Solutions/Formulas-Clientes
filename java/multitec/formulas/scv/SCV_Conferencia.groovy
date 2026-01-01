package multitec.formulas.scv

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0107
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.ea.Eaa0106;
import sam.model.entities.ea.Eaa01061

import java.time.LocalDate
import java.time.LocalTime;

class SCV_Conferencia extends FormulaBase{
    @Override
    FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SEPARACAO_CONFERENCIA_DE_ITENS;
    }

    @Override
    void executar() {
        Eaa0106 eaa0106 = get("eaa0106")
        List<TableMap> itens = get("itens")
        Integer volumes = get("volumes")

        Boolean itemNaoExistePedido = false
        Boolean itensConferidos = true;
        for(TableMap item : itens){
            Long abm01id = item.getLong("abm01id")
            BigDecimal quantidade = item.getBigDecimal_Zero("quantidade").setScale(6)

            Eaa0103 eaa0103 = getSession().createCriteria(Eaa0103.class)
                    .addFields("eaa0103id, eaa0103codigo, eaa0103descr, eaa0103qtUso")
                    .addWhere(Criterions.eq("eaa0103doc", eaa0106.getEaa0106doc().getEaa01id()))
                    .addWhere(Criterions.eq("eaa0103item", abm01id))
                    .setMaxResults(1)
                    .get(ColumnType.ENTITY)
            if(eaa0103 == null){
                itemNaoExistePedido = true;
                break;
            }
            Eaa01061 eaa01061 = eaa0106.getEaa01061s().stream()
                    .filter(e -> e.getEaa01061item().getEaa0103id().equals(eaa0103.getEaa0103id()))
                    .findFirst()
                    .orElse(null);

            if(eaa01061 == null){
                itemNaoExistePedido = true;
                break;
            }

            if(quantidade.compareTo(eaa01061.getEaa01061qtde()) == 0 && eaa01061.getEaa01061qtde().compareTo(eaa0103.getEaa0103qtUso()) == 0){
                eaa01061.setEaa01061conf(Eaa01061.SIM)
            }else{
                itensConferidos = false
                eaa01061.setEaa01061conf(Eaa01061.NAO)
            }
        }

        if(itemNaoExistePedido){
            throw  new ValidacaoException("Itens não constam no pedido.")
        }

        if(itensConferidos){
            Eaa0107 eaa0107 = getSession().createCriteria(Eaa0107.class)
                    .addWhere(Criterions.eq("eaa0107doc", eaa0106.getEaa0106doc().getEaa01id()))
                    .addWhere(Criterions.eq("eaa0107ident","separação"))
                    .get(ColumnType.ENTITY);

            Eaa01 eaa01 = getSession().createCriteria(Eaa01.class)
                    .addFields("eaa01id, eaa01bloqueado")
                    .addWhere(Criterions.eq("eaa01id", eaa0106.getEaa0106doc().getEaa01id()))
                    .get(ColumnType.ENTITY);

            eaa0107.setEaa0107user(obterUsuarioLogado())
            eaa0107.setEaa0107data(LocalDate.now())
            eaa0107.setEaa0107hora(LocalTime.now())

            getSession().persist(eaa0107)
        }

    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTE3In0=