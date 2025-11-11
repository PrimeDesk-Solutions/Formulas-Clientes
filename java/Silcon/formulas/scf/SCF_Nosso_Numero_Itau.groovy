package Silcon.formulas.scf

import sam.dicdados.FormulaTipo
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
        Daa01 daa01 = get("daa01");
        Abf01 abf01 = get("abf01");
        Long ultimoNossoNumero = get("ultimoNossoNumero");
        Long nossoNumero = 0;
        //Nosso número sequencialsb
        nossoNumero = ++ultimoNossoNumero;

        put("nossoNumero", nossoNumero);

    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDEifQ==