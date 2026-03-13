package Silcon.formulas.scf

import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abf01
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.ValidacaoException

class SCF_Nosso_Numero_Itau extends FormulaBase {

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_NOSSO_NUMERO;
    }

    @Override
    public void executar() {
        Abf01 abf01 = get("abf01");
        Long ultimoNossoNumero = get("ultimoNossoNumero");


        Daa01 daa01 = get("daa01");

        Abb01 abb01 = getSession().createCriteria(Abb01.class).addWhere(Criterions.eq("abb01id", daa01.daa01central.abb01id)).get();
        Integer numDoc = abb01.abb01num;
        String parcela = abb01.abb01parcela.trim();
        String complemento = "00";

        if(!"0".equals(parcela) && parcela != null ){
            if(parcela != null && parcela.contains("/")){
                Integer index = parcela.indexOf("/");
                String parcelas = parcela.substring(0, index);
                if(parcelas.length() == 1){
                    complemento = "0" + parcelas;
                }else{
                    complemento = parcelas
                }
            }else{
                if(parcela != null && parcela.matches("\\d+")){
                    if(parcela.length() == 1){
                        complemento = "0" + parcela;
                    }else{
                        complemento = parcela;
                    }
                }
            }
        }

        String numParc = numDoc + complemento;
        Long nossoNumero = Long.parseLong(numParc.length() > 8 ? numParc.substring(1) : numParc);

        put("nossoNumero", nossoNumero);

    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDEifQ==