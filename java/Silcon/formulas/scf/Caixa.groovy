package Silcon.formulas.scf

import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ab.Abb01
import sam.model.entities.da.Daa0101;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.scf.service.SCFService;

/**
 *
 * Fórmula para manipular o documento financeiro no programa SCF0155 - Caixa Financeiro
 *
 */
public class Caixa extends FormulaBase{

    private Daa01 daa01;


    @Override
    public void executar() {
        SCFService scfService = instanciarService(SCFService.class);
        daa01 = (Daa01) get("daa01");
        TableMap mapJson = daa01.daa01json == null ? new TableMap() : daa01.daa01json;

        def valor = daa01.daa01valor;

        //Juros = juros * qtd dias em atraso
        //Multa: considerar multa somente se estiver em atraso
        def juros = null;
        def multa = null;
        def diasAtraso = scfService.calculaDiasDeAtraso(daa01);

        //Encargos
        def encargos = mapJson.getBigDecimal("encargosq") == null ? null : mapJson.getBigDecimal("encargos");

        //Desconto: considerar desconto somente quando a data de pagamento for menor ou igual a data limite para desconto
        def desconto = mapJson.getBigDecimal("descontoq") == null ? null : mapJson.getBigDecimal("desconto");

        //Se documento está com valor parcial, ajusta os valores de JMED também parcialmente
        if(daa01 != null && !valor.equals(daa01.getDaa01valor())) {
            def fatorParcial = round(valor / daa01.getDaa01valor(), 6);

            if(juros != null) juros = round(juros * fatorParcial, 2);
            if(multa != null) multa = round(multa * fatorParcial, 2);
            if(encargos != null) encargos = round(encargos * fatorParcial, 2);
            if(desconto != null) desconto = round(desconto * fatorParcial, 2);
        }

        //Setar JMED calculados, nos campos livres de quitação
        def jurosq = mapJson.getBigDecimal("jurosq") == null ? juros : mapJson.getBigDecimal("jurosq");
        mapJson.put("jurosq", jurosq);

        def multaq = mapJson.getBigDecimal("multaq") == null ? multa : mapJson.getBigDecimal("multaq");
        mapJson.put("multaq", multaq);

        def encargosq = mapJson.getBigDecimal("encargosq") == null ? encargos : mapJson.getBigDecimal("encargosq");
        mapJson.put("encargosq", encargosq);

        BigDecimal descontoq = mapJson.getBigDecimal("descontoq") == null ? desconto : mapJson.getBigDecimal("descontoq");
        if(descontoq != null) descontoq = descontoq.abs() * -1
        mapJson.put("descontoq", descontoq);

        // Vale Consumidor
        if(mapJson.getBigDecimal("vale_consumidor") != null) mapJson.put("vale_consumidor", mapJson.getBigDecimal("vale_consumidor").abs() * -1);

        mapJson.put("user_baixa", obterUsuarioLogado().getAab10id());

        //def valorLiquido = valor + jurosq + encargosq + multaq + descontoq;
        def valorLiquido = valor;
        if(jurosq != null) valorLiquido = valorLiquido + jurosq;
        if(multaq != null) valorLiquido = valorLiquido + multaq;
        if(encargosq != null) valorLiquido = valorLiquido + encargosq;
        if(descontoq != null) valorLiquido = valorLiquido + descontoq;
        if(mapJson.getBigDecimal("vale_consumidor") != null) valorLiquido = valorLiquido + mapJson.getBigDecimal("vale_consumidor")

        trocarDepartamentos(daa01);

        daa01.daa01liquido = valorLiquido;
    }
    private void trocarDepartamentos(Daa01 daa01){

        if(daa01.daa01central.abb01quita == 0) return;

        TableMap jsonDaa01 = daa01.daa01json != null ? daa01.daa01json : new TableMap();

        if(jsonDaa01.getInteger("quita_manual") > 0) return;

        if(daa01.daa0101s != null && daa01.daa0101s.size() > 1){
            List<Daa01011> daa01011s = new ArrayList<>();

            Daa0101 daa0101novo = null;

            for(Daa0101 daa0101 in daa01.daa0101s){
                if(daa0101novo == null && daa0101.daa0101depto.abb11id != 295878){
                    daa0101novo = daa0101;
                }

                for(Daa01011 daa01011 in daa0101.daa01011s){
                    daa01011.daa01011depto = null;
                    daa01011s.add(daa01011);
                }
            }

            if(daa0101novo != null){
                daa0101novo.daa0101id = null;
                daa01.daa0101s = null;
                daa0101novo.daa01011s = null;
                daa0101novo.daa0101valor = new BigDecimal(0);

                for(Daa01011 daa01011 in  daa01011s){
                    daa01011.daa01011id = null;
                    daa0101novo.addToDaa01011s(daa01011);

                    daa0101novo.daa0101valor += daa01011.daa01011valor
                }
                daa01.addToDaa0101s(daa0101novo);
            }

            jsonDaa01.put("quita_manual", 1);
        }
    }
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_DOCUMENTOS;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDAifQ==