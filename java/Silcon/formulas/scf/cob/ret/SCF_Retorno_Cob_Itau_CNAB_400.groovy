package Silcon.formulas.scf.cob.ret

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abf20
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro
import sam.server.scf.service.SCFService

class SCF_Retorno_Cob_Itau_CNAB_400 extends FormulaBase {

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_RETORNO_DE_COBRANCA;
    }

    @Override
    public void executar() {
        List<TableMap> tmList = new ArrayList();
        TextFileLeitura txt = new TextFileLeitura(get("registros"))
        SCFService scfService = instanciarService(SCFService.class);


        txt.nextLine() //Pula o Header
        def linha = 1
        while(txt.nextLine()) {
            linha++
            if(txt.getSubString(0,1).equals("1")){
                TableMap tm = new TableMap();
                List<String> inconsistencias = new ArrayList();

                //pega o id do documento, remove zeros a esquerda e converte em Integer
                String id = txt.getSubString(37,45)
                Integer docId = null
                if(id == null || id.length() <= 0 || id == '        ') {
                    String inconsistencia = "Documento não encontrado por não haver o ID informado no retorno. Conteúdo encontrado: " + id + ". Linha: " + linha;
                    inconsistencias.add(inconsistencia);
                }else {
                    docId = Integer.parseInt(id)
                }

                Daa01 daa01 = null

                if(docId != null) {
                    daa01 = getAcessoAoBanco().buscarRegistroUnicoById("Daa01", Long.parseLong(docId.toString()));
                }

                boolean validouDocumento = true;
                if(daa01 == null) {
                    validouDocumento = false;
                }else {

                    if(daa01.daa01central.abb01quita_Zero > 0){
                        String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " já foi recebido.";
                        inconsistencias.add(inconsistencia);
                        validouDocumento = false;
                    }

                    //verifica se o valor do documento não é 0,01
                    if(daa01.daa01valor.compareTo(new BigDecimal(0.01)) != 0){
                        //verifica se o valor do documento é igual ao valor do arquivo de retorno
                        if(daa01.daa01valor.compareTo(new BigDecimal(txt.getSubString(152, 165)).divide(100)) != 0){
                            String inconsistencia = "O valor do documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " é diferente do valor do documento do retorno.";
                            inconsistencias.add(inconsistencia);
                            validouDocumento = false;
                        }
                    }

                    String descricaoOcor = buscarDescricaoOcorrencia(txt.getSubString(108, 110));
                    if(descricaoOcor == null){
                        String inconsistencia = "A ocorrência " + txt.getSubString(108, 110) + " informada no retorno para o documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrada nos parâmetros de retorno do banco.";
                        inconsistencias.add(inconsistencia);
                        validouDocumento = false;
                    }

                    if(txt.getSubString(295, 301) != '      ') {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
                        LocalDate dtBaixa = LocalDate.parse( txt.getSubString(295, 301), formatter);
                        LocalDate dtPgto = LocalDate.parse( txt.getSubString(110, 116), formatter);
                        daa01.daa01dtPgto = dtPgto
                        daa01.daa01dtBaixa = dtBaixa
                    }else {
//                        String inconsistencia = "No Documento número: " + daa01.daa01central.abb01num + ", série: " + (daa01.daa01central.abb01serie == null ? "" : daa01.daa01central.abb01serie) + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrada data de pagamento.";
//                        inconsistencias.add(inconsistencia);
                        LocalDate dtAtual = LocalDate.now()
                        daa01.daa01dtPgto = dtAtual;
                    }


                    BigDecimal vlrLiq = new BigDecimal(txt.getSubString(253, 266))
                    daa01.daa01liquido =   vlrLiq / 100

                    TableMap jsons = daa01.daa01json == null ? new TableMap() : daa01.daa01json

                    BigDecimal vlrDesconto = new BigDecimal(txt.getSubString(240, 253));
                    BigDecimal vlrMulta = new BigDecimal(txt.getSubString(266, 279));
                    vlrMulta = vlrMulta / 100;
                    LocalDate dtLimiteDesc = daa01.daa01dtVctoN
                    LocalDate dtPgto = daa01.daa01dtPgto;

                    // Retira o Juros que vem somado a multa
                    if(vlrMulta > 0){
                        Integer difDias = dtPgto.compareTo(dtLimiteDesc);
                        BigDecimal juros = jsons.getBigDecimal_Zero("juros")
                        BigDecimal jurosPorDia = difDias * juros
                        vlrMulta = vlrMulta - jurosPorDia
                    }
                    
                    // Taxa Bancária
                    //BigDecimal txBanco = new BigDecimal(txt.getSubString(175, 188));


                    jsons.put("multa", vlrMulta)
                    if(vlrMulta == 0) jsons.put("juros", new BigDecimal(0));
                    jsons.put("desconto", vlrDesconto/100)
                    jsons.put("dt_limite_desc", dtLimiteDesc)
                    //jsons.put("tx_bancaria", txBanco/100);

                    daa01.daa01json = jsons

                }

                tm.put("inconsistencias", inconsistencias);
                /**
                 * Exibindo documentos
                 */
                if(validouDocumento){
                    tm.put("daa01", daa01);
                    tm.put("abf20id", buscarPLF(codigoPLF(txt.getSubString(108, 110))));
                    tm.put("ocorrencia", buscarDescricaoOcorrencia(txt.getSubString(108, 110)));
                }

                tmList.add(tm);

            }
        }
        put("tmList", tmList);
    }

    private String buscarDescricaoOcorrencia(String codigoOcorrencia) {
        switch(codigoOcorrencia) {
            case "02": return "ENTRADA CONFIRMADA COM POSSIBILIDADE DE MENSAGEM (NOTA 20 - TABELA 10)";
            case "03": return "ENTRADA REJEITADA (NOTA 20 - TABELA 1)";
            case "04": return "ALTERAÇÃO DE DADOS - NOVA ENTRADA OU ALTERAÇÃO/EXCLUSÃO DE DADOS ACATADA";
            case "05": return "ALTERAÇÃO DE DADOS - BAIXA";
            case "06": return "LIQUIDAÇÃO NORMAL";
            case "07": return "LIQUIDAÇÃO PARCIAL - COBRANÇA INTELIGENTE (B2B)";
            case "08": return "LIQUIDAÇÃO EM CARTÓRIO";
            case "09": return "BAIXA SIMPLES";
            case "10": return "BAIXA POR TER SIDO LIQUIDADO";
            case "11": return "EM SER (SÓ NO RETORNO MENSAL)";
            case "12": return "ABATIMENTO CONCEDIDO";
            case "13": return "ABATIMENTO CANCELADO";
            case "14": return "VENCIMENTO ALTERADO";
            case "15": return "BAIXAS REJEITADAS (NOTA 20 - TABELA 4)";
            case "16": return "INSTRUÇÕES REJEITADAS (NOTA 20 - TABELA 3)";
            case "17": return "ALTERAÇÃO/EXCLUSÃO DE DADOS REJEITADOS (NOTA 20 - TABELA 2)";
            case "18": return "COBRANÇA CONTRATUAL - INSTRUÇÕES/ALTERAÇÕES REJEITADAS/PENDENTES (NOTA 20 - TABELA 5)";
            case "19": return "CONFIRMA RECEBIMENTO DE INSTRUÇÃO DE PROTESTO";
            case "20": return "CONFIRMA RECEBIMENTO DE INSTRUÇÃO DE SUSTAÇÃO DE PROTESTO /TARIFA";
            case "21": return "CONFIRMA RECEBIMENTO DE INSTRUÇÃO DE NÃO PROTESTAR";
            case "23": return "TÍTULO ENVIADO A CARTÓRIO/TARIFA";
            case "24": return "INSTRUÇÃO DE PROTESTO REJEITADA / SUSTADA / PENDENTE (NOTA 20 - TABELA 7)";
            case "25": return "ALEGAÇÕES DO PAGADOR (NOTA 20 - TABELA 6)";
            case "26": return "TARIFA DE AVISO DE COBRANÇA";
            case "27": return "TARIFA DE EXTRATO POSIÇÃO (B40X)";
            case "28": return "TARIFA DE RELAÇÃO DAS LIQUIDAÇÕES";
            case "29": return "TARIFA DE MANUTENÇÃO DE TÍTULOS VENCIDOS";
            case "30": return "DÉBITO MENSAL DE TARIFAS (PARA ENTRADAS E BAIXAS)";
            case "32": return "BAIXA POR TER SIDO PROTESTADO";
            case "33": return "CUSTAS DE PROTESTO";
            case "34": return "CUSTAS DE SUSTAÇÃO";
            case "35": return "CUSTAS DE CARTÓRIO DISTRIBUIDOR";
            case "36": return "CUSTAS DE EDITAL";
            case "37": return "TARIFA DE EMISSÃO DE BOLETO/TARIFA DE ENVIO DE DUPLICATA";
            case "38": return "TARIFA DE INSTRUÇÃO";
            case "39": return "TARIFA DE OCORRÊNCIAS";
            case "40": return "TARIFA MENSAL DE EMISSÃO DE BOLETO/TARIFA MENSAL DE ENVIO DE DUPLICATA";
            case "41": return "DÉBITO MENSAL DE TARIFAS - EXTRATO DE POSIÇÃO (B4EP/B4OX)";
            case "42": return "DÉBITO MENSAL DE TARIFAS - OUTRAS INSTRUÇÕES";
            case "43": return "DÉBITO MENSAL DE TARIFAS - MANUTENÇÃO DE TÍTULOS VENCIDOS";
            case "44": return "DÉBITO MENSAL DE TARIFAS - OUTRAS OCORRÊNCIAS";
            case "45": return "DÉBITO MENSAL DE TARIFAS - PROTESTO";
            case "46": return "DÉBITO MENSAL DE TARIFAS - SUSTAÇÃO DE PROTESTO";
            case "47": return "BAIXA COM TRANSFERÊNCIA PARA DESCONTO";
            case "48": return "CUSTAS DE SUSTAÇÃO JUDICIAL";
            case "51": return "TARIFA MENSAL REF A ENTRADAS BANCOS CORRESPONDENTES NA CARTEIRA";
            case "52": return "TARIFA MENSAL BAIXAS NA CARTEIRA";
            case "53": return "TARIFA MENSAL BAIXAS EM BANCOS CORRESPONDENTES NA CARTEIRA";
            case "54": return "TARIFA MENSAL DE LIQUIDAÇÕES NA CARTEIRA";
            case "55": return "TARIFA MENSAL DE LIQUIDAÇÕES EM BANCOS CORRESPONDENTES NA CARTEIRA";
            case "56": return "CUSTAS DE IRREGULARIDADE";
            case "57": return "INSTRUÇÃO CANCELADA (NOTA 20 - TABELA 8)";
            case "59": return "BAIXA POR CRÉDITO EM C/C ATRAVÉS DO SISPAG";
            case "60": return "ENTRADA REJEITADA CARNÊ (NOTA 20 - TABELA 1)";
            case "61": return "TARIFA EMISSÃO AVISO DE MOVIMENTAÇÃO DE TÍTULOS (2154)";
            case "62": return "DÉBITO MENSAL DE TARIFA - AVISO DE MOVIMENTAÇÃO DE TÍTULOS (2154)";
            case "63": return "TÍTULO SUSTADO JUDICIALMENTE";
            case "64": return "ENTRADA CONFIRMADA COM RATEIO DE CRÉDITO";
            case "65": return "PAGAMENTO COM CHEQUE - AGUARDANDO COMPENSAÇÃO";
            case "69": return "CHEQUE DEVOLVIDO (NOTA 20 - TABELA 9)";
            case "71": return "ENTRADA REGISTRADA, AGUARDANDO AVALIAÇÃO";
            case "72": return "BAIXA POR CRÉDITO EM C/C ATRAVÉS DO SISPAG SEM TÍTULO CORRESPONDENTE";
            case "73": return "CONFIRMAÇÃO DE ENTRADA NA COBRANÇA SIMPLES - ENTRADA NÃO ACEITA NA COBRANÇA CONTRATUAL";
            case "74": return "INSTRUÇÃO DE NEGATIVAÇÃO EXPRESSA REJEITADA (NOTA 20 - TABELA 11)";
            case "75": return "CONFIRMAÇÃO DE RECEBIMENTO DE INSTRUÇÃO DE ENTRADA EM NEGATIVAÇÃO EXPRESSA";
            case "76": return "CHEQUE COMPENSADO";
            case "77": return "CONFIRMAÇÃO DE RECEBIMENTO DE INSTRUÇÃO DE EXCLUSÃO DE ENTRADA EM NEGATIVAÇÃO EXPRESSA";
            case "78": return "CONFIRMAÇÃO DE RECEBIMENTO DE INSTRUÇÃO DE CANCELAMENTO DE NEGATIVAÇÃO EXPRESSA";
            case "79": return "NEGATIVAÇÃO EXPRESSA INFORMACIONAL (NOTA 20 - TABELA 12)";
            case "80": return "CONFIRMAÇÃO DE ENTRADA EM NEGATIVAÇÃO EXPRESSA - TARIFA";
            case "82": return "CONFIRMAÇÃO DO CANCELAMENTO DE NEGATIVAÇÃO EXPRESSA - TARIFA";
            case "83": return "CONFIRMAÇÃO DE EXCLUSÃO DE ENTRADA EM NEGATIVAÇÃO EXPRESSA POR LIQUIDAÇÃO - TARIFA";
            case "85": return "TARIFA POR BOLETO (ATÉ 03 ENVIOS) COBRANÇA ATIVA ELETRÔNICA";
            case "86": return "TARIFA EMAIL COBRANÇA ATIVA ELETRÔNICA";
            case "87": return "TARIFA SMS COBRANÇA ATIVA ELETRÔNICA";
            case "88": return "TARIFA MENSAL POR BOLETO (ATÉ 03 ENVIOS) COBRANÇA ATIVA ELETRÔNICA";
            case "89": return "TARIFA MENSAL EMAIL COBRANÇA ATIVA ELETRÔNICA";
            case "90": return "TARIFA MENSAL SMS COBRANÇA ATIVA ELETRÔNICA";
            case "91": return "TARIFA MENSAL DE EXCLUSÃO DE ENTRADA DE NEGATIVAÇÃO EXPRESSA";
            case "92": return "TARIFA MENSAL DE CANCELAMENTO DE NEGATIVAÇÃO EXPRESSA";
            case "93": return "TARIFA MENSAL DE EXCLUSÃO DE ENTRADA DE NEGATIVAÇÃO EXPRESSA POR LIQUIDAÇÃO";
            default: return null;
        }
    }

    private String codigoPLF(String codigoOcorrencia) {
        switch(codigoOcorrencia) {
            case "06": return "100";
            default: return null;
        }
    }

    private Long buscarPLF(String codigoPLF) {
        Abf20 abf20 = getAcessoAoBanco().buscarRegistroUnico("SELECT abf20id FROM Abf20 WHERE abf20codigo = :P1 " + getSamWhere().getWherePadrao("AND", Abf20.class) , Parametro.criar("P1", codigoPLF));
        return abf20 == null ? null : abf20.abf20id;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==