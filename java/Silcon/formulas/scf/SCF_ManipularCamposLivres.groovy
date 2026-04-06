package Silcon.formulas.scf;

import sam.dicdados.FormulaTipo
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase

public class SCF_ManipularCamposLivres extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_QUITA_PARCIAL_CASHBACK;
    }

    @Override
    public void executar() {
        Daa01 daa01quitado = get("daa01quitado")
        Daa01 daa01origem = get("daa01origem")

        def jurosq = daa01quitado.daa01json.get('jurosq')
        def descontoq = daa01quitado.daa01json.get('descontoq')
        def encargosq = daa01quitado.daa01json.get('encargosq')
        def multaq = daa01quitado.daa01json.get('multaq')

        daa01origem.daa01json.put('jurosq', jurosq)
        daa01origem.daa01json.put('descontoq', descontoq)
        daa01origem.daa01json.put('encargosq', encargosq)
        daa01origem.daa01json.put('multaq', multaq)

        daa01quitado.daa01json.put('jurosq', BigDecimal.ZERO)
        daa01quitado.daa01json.put('descontoq', BigDecimal.ZERO)
        daa01quitado.daa01json.put('encargosq', BigDecimal.ZERO)
        daa01quitado.daa01json.put('multaq', BigDecimal.ZERO)
    }
}