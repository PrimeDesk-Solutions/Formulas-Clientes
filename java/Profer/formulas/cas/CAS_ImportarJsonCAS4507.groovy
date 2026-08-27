package Atilatte.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.web.multipart.MultipartFile
import sam.model.entities.aa.Aaj07
import sam.model.entities.aa.Aap10
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

import java.time.LocalDate
import java.time.format.DateTimeFormatter

public class CAS_ImportarJsonCAS4507 extends FormulaBase {

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        MultipartFile arquivo = get("arquivo");

        // Verifica se o arquivo foi enviado
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RuntimeException("Nenhum arquivo JSON foi enviado");
        }

        String jsonContent = new String(arquivo.getBytes());

        List<TableMap> dadosJson = JSonMapperCreator.create().read(jsonContent, new TypeReference<List<TableMap>>() {})

        // Processa cada registro do JSON
        for (dadoJson in dadosJson) {
            Aaj07 aaj07 = getSession().createCriteria(Aaj07.class)
                    .addWhere(Criterions.where("aaj07codigo = '" + dadoJson.get("código da classificação tributária") + "'"))
                    .get(ColumnType.ENTITY)

//					if(dadoJson.get("código da classificação tributária") == "000002"){
            if(aaj07){
                TableMap jsonAaj07 = aaj07 != null && aaj07.aaj07json != null ? aaj07.aaj07json : new TableMap();

                jsonAaj07.put("cst_cbsibs", dadoJson.get("Código da Situação Tributária"))
                jsonAaj07.put("desc_cstcbsibs", dadoJson.get("Descrição da Situação Tributária"))
                jsonAaj07.put("tributacao", converterValor(dadoJson.get("Tributação Regular")))
                jsonAaj07.put("red_bc_cst", converterValor(dadoJson.get("Redução BC CST")))
                jsonAaj07.put("red_bc_aliq", converterValor(dadoJson.get("Redução de Alíquota")))
                jsonAaj07.put("transf_cred", converterValor(dadoJson.get("Transferência de Crédito")))
                jsonAaj07.put("dif_cbsibs", converterValor(dadoJson.get("Diferimento")))
                jsonAaj07.put("monofasica_cbsibs", converterValor(dadoJson.get("Monofásica")))
                jsonAaj07.put("cred_pres_ibs_zfm", converterValor(dadoJson.get("Crédito Presumido IBS Zona Franca de Manaus")))
                jsonAaj07.put("ajuste_comp", converterValor(dadoJson.get("Ajuste de Competência")))
                jsonAaj07.put("perc_red_ibs", dadoJson.get("Percentual Redução IBS"))
                jsonAaj07.put("perc_red_cbs", dadoJson.get("Percentual Redução CBS"))
                jsonAaj07.put("red_bc", converterValor(dadoJson.get("Redução BC")))
                jsonAaj07.put("cred_presumido", converterValor(dadoJson.get("Crédito Presumido")))
                jsonAaj07.put("estorno_cred", converterValor(dadoJson.get("Estorno de Crédito")))
                jsonAaj07.put("tipo_aliq", buscarIndexTpAliq(dadoJson.get("Tipo de Alíquota")))//CRIAR FUNÇAO PARA BUSCAR O INDEX CORRETO
                jsonAaj07.put("dfe_relacionado", listarDfeRelacionado(dadoJson))


                aaj07.setAaj07json(jsonAaj07)

                //verificando se a transação está aberta
                if (!getSession().transactionOpened) {
                    getSession().beginTransaction();
                }

                //efetivamente gravando os valores
                getSession().persist(aaj07);
                getSession().commit();
            }
        }
    }
    private String converterValor(Object valor) {
        return "sim".equalsIgnoreCase(String.valueOf(valor)) ? "1" : "0";
    }

    private String buscarIndexTpAliq(Object valor) {
        Map<String, String> mapeamentoCampos = Map.of(
                "1 - Fixa", "0",
                "2 - Padrão", "1",
                "3 - Sem aliquota", "2",
                "4 - Uniforme Nacional", "3",
                "5 - Uniforme Setorial", "4"
        );

        return mapeamentoCampos.get(String.valueOf(valor));
    }

    private String listarDfeRelacionado( TableMap dadoJson){
        String dados = ""

        converterValor(dadoJson.get("NFe")) == "1"? dados = dados + " NFe" : null
        converterValor(dadoJson.get("CTe")) == "1"? dados = dados + " CTe" : null
        converterValor(dadoJson.get("CTe OS")) == "1"? dados = dados + " CTe OS" : null
        converterValor(dadoJson.get("BPe")) == "1"? dados = dados + " BPe" : null
        converterValor(dadoJson.get("NF3e")) == "1"? dados = dados + " NF3e" : null
        converterValor(dadoJson.get("NFCom")) == "1"? dados = dados + " NFCom" : null
        converterValor(dadoJson.get("NFSE")) == "1"? dados = dados + " NFSE" : null
        converterValor(dadoJson.get("BPe TM")) == "1"? dados = dados + " BPe TM" : null
        converterValor(dadoJson.get("BPe TA")) == "1"? dados = dados + " BPe TA" : null
        converterValor(dadoJson.get("NFAg")) == "1"? dados = dados + " NFAg" : null
        converterValor(dadoJson.get("NFSVIA")) == "1"? dados = dados + " NFSVIA" : null
        converterValor(dadoJson.get("NFABI")) == "1"? dados = dados + " NFABI" : null
        converterValor(dadoJson.get("NFGas")) == "1"? dados = dados + " NFGas" : null
        converterValor(dadoJson.get("DERE")) == "1"? dados = dados + " DERE" : null

        return dados
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=