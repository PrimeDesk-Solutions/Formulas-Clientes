package Silcon.formulas.preGravar

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe02
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import java.time.format.DateTimeFormatter;


import java.time.LocalDate

public class Altera_Representante_Documento extends FormulaBase{

    private Eaa01 eaa01;
    private Abb01 abb01;
    private Abe01 abe01;
    private Abe0101 abe0101;
    private Abe02 abe02;
    private Integer gravar = 1; //0-Não 1-Sim

    TableMap jsonAbe02;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
    }

    @Override
    public void executar() {
        eaa01 = get("eaa01");
        alterarRepresentante(eaa01);
        validarQuantidadeItem(eaa01);
        verificarDataVctoLimiteCredito(eaa01);
        verificarInscricaoEntidades(eaa01);

        put("gravar", gravar);
    }
    private void alterarRepresentante(Eaa01 eaa01){
        if(eaa01.eaa01rep0 == null){
            Long idUser = obterUsuarioLogado().getAab10id();
            Abe01 abe01 = buscarIdEntidadeRepresentante(idUser);

            eaa01.eaa01rep0 = abe01;
        }
    }
    private Abe01 buscarIdEntidadeRepresentante(Long userLogado){
        return getSession().createCriteria(Abe01.class)
                .addFields("abe01id")
                .addJoin(Joins.join("Abe05", "abe05ent = abe01id"))
                .addJoin(Joins.join("Aab10", "aab10id = abe05user"))
                .addWhere(Criterions.eq("aab10id", userLogado)).setMaxResults(1)
                .get(ColumnType.ENTITY);
    }
    private validarQuantidadeItem(Eaa01 eaa01){
        if(eaa01.eaa0103s.size() == 0) throw new ValidacaoException("Não é permitido salvar documento sem itens informado. Insira pelo menos um item para continuar ");

        for(Eaa0103 eaa0103 : eaa01.eaa0103s){

            // Itens
            Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

            if(eaa0103.eaa0103unit == 0) throw new ValidacaoException("Unitário inválido para o item " + abm01.abm01codigo)

            if(eaa0103.eaa0103qtComl == 0 ) throw new ValidacaoException("Quantidade inválida para o item "+abm01.abm01codigo+" necessário um valor maior que zero")
        }
    }
    private void verificarDataVctoLimiteCredito(Eaa01 eaa01){

        Abb01 abb01 = eaa01.eaa01central;

        Abe01 abe01 = abb01.abb01ent;

        Abd01 abd01 = getSession().createCriteria(Abd01.class).addWhere(Criterions.eq("abd01id", eaa01.eaa01pcd.abd01id)).get(ColumnType.ENTITY);

        if(abe01.abe01codigo == "9999999100" || (abd01.abd01codigo != "60001" && abd01.abd01codigo != "70001")) return // CONSUMIDOR OU PCD VENDA

        TableMap jsonAbe01 = getSession().createQuery("SELECT abe01json FROM abe01 WHERE abe01id = :idEntidade")
                .setParameter("idEntidade", abb01.abb01ent.abe01id)
                .getUniqueResult(ColumnType.JSON);

        LocalDate dataAtual = LocalDate.now();
        LocalDate dtVencLimCredito = jsonAbe01.getDate("dt_vcto_lim_credito");

        if(dtVencLimCredito == null) interromper("Não foi informado data de vencimento de limite de crédito no cadastro da entidade.");

        if(dtVencLimCredito < dataAtual){ // Data de vencimento de crédito menor que data atual, significa que expirou
            interromper("Data de vencimento do limite de crédito do cliente venceu em " + dtVencLimCredito.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + ".")
        }
    }
    private void verificarInscricaoEntidades(Eaa01 eaa01){
        Abb01 abb01 = eaa01.eaa01central;

        Abe01 abe01 = getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01id", abb01.abb01ent.abe01id)).get(ColumnType.ENTITY);

        if(abe01.abe01codigo == "9999999100") return;
        if(abe01.abe01ti == 2 && !(abe01.abe01ni.length() == 11 || abe01.abe01ni.length() == 14)) interromper("Número da inscrição da entidade é inválido. Verifique o cadastro antes de prosseguir.");
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==