package multitec.baseDemo

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.collections.TableMap;
import org.springframework.web.multipart.MultipartFile;
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abe01;
import sam.model.entities.da.Daa01
import sam.model.entities.dc.Dcb01
import sam.model.entities.eb.Ebb05;
import sam.server.samdev.formula.FormulaBase;

import java.time.LocalDate;

public class CAS_FromDaa01ToDcb01 extends FormulaBase {
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        Ebb05 ebb05 = new Ebb05()
        TableMap json = get("json");
        MultipartFile arquivo = get("arquivo");

        LocalDate dataInicial = json.getDate("data_inicial");
        LocalDate dataFinal = json.getDate("data_final");
        Integer seqRep = json.getInteger("seq_rep");
        String codRep = json.getString("cod_representante");

        if(dataInicial == null || dataFinal == null) interromper("Informe as datas inicial e final!");
        if(seqRep == null) interromper("Informe a sequencia do Representante!");
        if(codRep == null || codRep.equals("")) interromper("Informe o código do representante!");

        Abe01 abe01 = buscarRepresentante(codRep);
        if(abe01 == null) interromper("Representante não encontrado para o codigo: " + codRep);

        List<Daa01> daa01s = buscarDaa01s(dataInicial,dataFinal,seqRep,abe01.abe01id);
        if(daa01s == null || daa01s.size() < 1) interromper("Nenhum documento Daa01 encontrado!");

        for (Daa01 daa01 in daa01s){
            Dcb01 dcb01 = new Dcb01();

            switch (seqRep){
                case 0:
                    dcb01.setDcb01rep(daa01.daa01rep0);
                    dcb01.setDcb01taxa(daa01.daa01txComis0_Zero);
                    break;
                case 1:
                    dcb01.setDcb01rep(daa01.daa01rep1);
                    dcb01.setDcb01taxa(daa01.daa01txComis1_Zero);
                    break;
                case 2:
                    dcb01.setDcb01rep(daa01.daa01rep2);
                    dcb01.setDcb01taxa(daa01.daa01txComis2_Zero);
                    break;
                case 3:
                    dcb01.setDcb01rep(daa01.daa01rep3);
                    dcb01.setDcb01taxa(daa01.daa01txComis3_Zero);
                    break;
                case 4:
                    dcb01.setDcb01rep(daa01.daa01rep4);
                    dcb01.setDcb01taxa(daa01.daa01txComis4_Zero);
                    break;
                default:
                    interromper("A sequencia selecionada não é valida!");
                    break;
            }

            dcb01.setDcb01aceite(Dcb01.SIM);
            dcb01.setDcb01dtCredito(null);
            dcb01.setDcb01json(null);
            dcb01.setDcb01calculo(null);

            dcb01.setDcb01gc(daa01.daa01gc);
            dcb01.setDcb01eg(daa01.daa01eg);
            dcb01.setDcb01bc(daa01.daa01liquido_Zero);
            dcb01.setDcb01central(daa01.daa01central);

            BigDecimal percentual = dcb01.dcb01taxa_Zero != BigDecimal.ZERO ? dcb01.dcb01taxa_Zero.divide(new BigDecimal(100)) : BigDecimal.ZERO;
            BigDecimal valor = dcb01.dcb01bc_Zero.multiply(percentual)

            dcb01.setDcb01valor(round(valor,2));

            getSession().persist(dcb01);
        }
    }

    private Abe01 buscarRepresentante(String codRep){
        return getSession().createCriteria(Abe01.class)
                .addFields("abe01id, abe01codigo, abe01na")
                .addWhere(Criterions.eq("abe01codigo", codRep))
                .addWhere(Criterions.eq("abe01rep", Abe01.SIM))
                .addWhere(getSamWhere().getCritPadrao(Abe01.class))
                .setMaxResults(1)
                .get(ColumnType.ENTITY)
    }

    private List<Daa01> buscarDaa01s(LocalDate dataInicial, LocalDate dataFinal, Integer seqRep, Long repId){
        String campoRep = "daa01rep"+seqRep;
        return getSession().createCriteria(Daa01.class)
                .addWhere(Criterions.between("daa01dtPgto",dataInicial, dataFinal))
                .addWhere(Criterions.eq(campoRep, repId))
                .addWhere(getSamWhere().getCritPadrao(Daa01.class))
                .getList();
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=