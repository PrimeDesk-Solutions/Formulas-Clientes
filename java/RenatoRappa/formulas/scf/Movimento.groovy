package RenatoRappa.formulas.scf;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa1001;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;
import sam.server.scf.service.SCFService;

/**
 * Fórmula para manipular Movimento Financeiro  
 *
 */
public class Movimento extends FormulaBase{

    private Daa1001 daa1001;

    @Override
    public void executar() {
        SCFService scfService = instanciarService(SCFService.class);
        daa1001 = (Daa1001) get("daa1001");
        TableMap mapJson = daa1001.daa1001json == null ? new TableMap() : daa1001.daa1001json;

        Daa01 daa01 = null;
        if(daa1001.daa1001central != null && daa1001.daa1001central.abb01id != null) daa01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Daa01 WHERE daa01central = :abb01id ", Parametro.criar("abb01id", daa1001.daa1001central.abb01id));

        def valor = daa1001.daa1001valor;

        //Setar JMED calculados, nos campos livres de quitação
        def jurosq = mapJson.getBigDecimal("juros") == null ? new BigDecimal(0) : round(mapJson.getBigDecimal("juros"), 2);
        mapJson.put("juros", jurosq);

        def multaq = mapJson.getBigDecimal("multa") == null ? new BigDecimal(0) : round(mapJson.getBigDecimal("multa"), 2);
        mapJson.put("multa", multaq);

        def encargosq = mapJson.getBigDecimal("encargos") == null ? new BigDecimal(0) : round(mapJson.getBigDecimal("encargos"), 2);
        mapJson.put("encargos", encargosq);

        BigDecimal descontoq = mapJson.getBigDecimal("desconto") == null ? new BigDecimal(0) : round(mapJson.getBigDecimal("desconto"), 2);
        if(descontoq != null) descontoq = descontoq.abs() * -1
        mapJson.put("desconto", descontoq);

        //def valorLiquido = valor + jurosq + encargosq + multaq + descontoq;
        def valorLiquido = valor;
        if(jurosq != null) valorLiquido = valorLiquido + jurosq;
        if(multaq != null) valorLiquido = valorLiquido + multaq;
        if(encargosq != null) valorLiquido = valorLiquido + encargosq;
        if(descontoq != null) valorLiquido = valorLiquido + descontoq;

        daa1001.daa1001liquido = round(valorLiquido, 2);
    }
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_LCTOS_DE_MOVIMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDgifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDgifQ==