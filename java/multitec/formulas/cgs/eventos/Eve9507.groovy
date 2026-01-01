package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9507 - FGTS sobre 13° Salário/Férias
 **/
public class Eve9507 extends FormulaBase {

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.EVENTOS;
    }

    @Override
    public void executar() {
        SFPUtils sfpUtils = get("sfpUtils");
        SFPVarDto sfpVarDto = sfpUtils.getSfpVarDto();
        List<Fba01011> fba01011s = sfpVarDto.getFba01011s();
        Fba01011 fba01011 = fba01011s.get(sfpVarDto.getIndex());
        Abh80 abh80 = sfpVarDto.getAbh80();

        def tx = abh80.abh80fgtsAliq;
        def bc = sfpUtils.buscarValor("9003"); //BC FGTS 13° Salário / Férias
        def fgts = bc * tx / 100;

        fba01011.fba01011refUnid = round(tx, 2);
        fba01011.fba01011valor = round(fgts, 2);
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==