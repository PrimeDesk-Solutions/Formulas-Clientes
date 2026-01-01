package multitec.formulas.scv

import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103;
import sam.model.entities.ea.Eaa0106;
import sam.model.entities.ea.Eaa01061;
import sam.model.entities.ea.Eaa0107;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

public class SCV_PreGravacaoSeparacao extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
    }

    @Override
    public void executar() {
        Eaa01 eaa01 = get("eaa01");
        Integer gravar = Eaa01.SIM;

        if(eaa01.getEaa01clasDoc() == Eaa01.CLASDOC_SCV && eaa01.getEaa01esMov() == Eaa01.ESMOV_SAIDA){
            List<Eaa0106> eaa0106s = new ArrayList<>();
            if(eaa01.getEaa0103s() != null && eaa01.getEaa0103s().size() > 0){
                Integer seq = 1
                for(Eaa0103 eaa0103 : eaa01.getEaa0103s()){
                    Eaa0106 eaa0106 = new Eaa0106();
                    eaa0106.setEaa0106doc(eaa01);
                    eaa0106.setEaa0106descr("Separar Item " + eaa0103.getEaa0103codigo());
                    eaa0106.setEaa0106status(0);
                    eaa0106.setEaa0106seq(seq);

                    Eaa01061 eaa01061 = new Eaa01061();
                    eaa01061.setEaa01061etapa(eaa0106);
                    eaa01061.setEaa01061item(eaa0103);
                    eaa01061.setEaa01061qtde(BigDecimal.ZERO);
                    eaa01061.setEaa01061conf(Eaa0106.NAO);

                    eaa0106.addToEaa01061s(eaa01061);
                    eaa0106s.add(eaa0106);
                    seq++
                }
            }

            if(eaa0106s.size() > 0){
                Eaa0107 eaa0107 = new Eaa0107();
                eaa0107.setEaa0107doc(eaa01);
                eaa0107.setEaa0107msg("Bloqueio da Separação");
                eaa0107.setEaa0107justificativa("Pedido a Separar");
                eaa0107.setEaa0107ident("separação");

                eaa01.addToEaa0107s(eaa0107);
                eaa01.setEaa01bloqueado(Eaa01.SIM);
                eaa01.setEaa0106s(eaa0106s);
                eaa01.setEaa01sepStatus(1);
                eaa01.setEaa01sepSeq(Eaa01.NAO);
            }
        }

        put("gravar", gravar);
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==