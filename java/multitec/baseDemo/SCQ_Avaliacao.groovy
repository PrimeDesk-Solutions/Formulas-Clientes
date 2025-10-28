package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abm50
import sam.model.entities.bb.Bbb01
import sam.model.entities.bb.Bbb0101
import sam.server.samdev.formula.FormulaBase

public class SCQ_Avaliacao extends FormulaBase {
	
	private Bbb01 bbb01;
    private String procInvoc;
    private Bbb0101 bbb0101;
    private Abm50 abm50;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCQ_AVALIACAO;
    }

    @Override
    public void executar() {
        bbb0101 = get("bbb0101");
		procInvoc = get("procInvoc");
		
        bbb01 = bbb0101.bbb0101ficha;
        abm50 = bbb0101.bbb0101ia;

        TableMap tmJsonBbb0101 = bbb0101.bbb0101json == null ? new TableMap() : bbb0101.bbb0101json;

        //Avaliação de itens: 10-Sólidos
        if(abm50.abm50codigo == "10") {
            def odor = tmJsonBbb0101.get("scq_odor");             // 0-Fraco,        1-Médio, 2-Forte
            def tamanho = tmJsonBbb0101.get("scq_tamanho");       // 0-Pequeno,      1-Médio  2-Grande
            def peso = tmJsonBbb0101.get("scq_peso");             // 0-até 1 quilo   1-acima de 1 quilo

            if(odor == null || tamanho == null || peso == null) {
                bbb0101.bbb0101valida = 0; //0-Validar
            }else if(odor.equals(1) && tamanho.equals(1) && peso.equals(1)) {
                bbb0101.bbb0101valida = 1; //1-Válida
            }else {
                bbb0101.bbb0101valida = 2; //2-Inválida
            }
        }

        //Avaliação de itens: 20-Líquidos
        if(abm50.abm50codigo == "20") {
            def embalagem = tmJsonBbb0101.get("scq_embalagem");   // 0-Excelente, 1-Boa, 2-Aceitável, 3-Ruim
            def tipo = tmJsonBbb0101.get("scq_tipo");             // A, B, C
            def sabor = tmJsonBbb0101.get("scq_sabor");           // 0-Normal, 1-Anormal

            if(embalagem == null || tipo == null || sabor == null) {
                bbb0101.bbb0101valida = 0; //0-Validar
            }else if(embalagem < 2 && tipo.equals("A") && sabor.equals(0)) {
                bbb0101.bbb0101valida = 1; //1-Válida
            }else {
                bbb0101.bbb0101valida = 2; //2-Inválida
            }
        }

        //Avaliação da itens: 30-Frutas
        if(abm50.abm50codigo == "30") {
            def embalagem = tmJsonBbb0101.get("scq_embalagem");   // 0-Excelente, 1-Boa,   2-Aceitável, 3-Ruim
            def tamanho = tmJsonBbb0101.get("scq_tamanho");       // 0-Pequeno,   1-Médio  2-Grande
            def sabor = tmJsonBbb0101.get("scq_sabor");           // 0-Normal,    1-Anormal

            if(embalagem == null || tamanho == null || sabor == null) {
                bbb0101.bbb0101valida = 0; //0-Validar
            }else if(embalagem < 2 && tamanho.equals(1) && sabor.equals(0)) {
                bbb0101.bbb0101valida = 1; //1-Válida
            }else {
                bbb0101.bbb0101valida = 2; //2-Inválida
            }
        }
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzkifQ==