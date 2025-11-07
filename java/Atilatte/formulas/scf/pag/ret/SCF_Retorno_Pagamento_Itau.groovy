package Atilatte.formulas.scf.pag.ret

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.exception.NonUniqueObject
import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abf01
import sam.model.entities.ab.Abf20
import sam.model.entities.da.Daa01
import sam.model.entities.da.Daa0102
import sam.server.samdev.formula.FormulaBase
import sam.server.scf.service.SCFService
import sam.server.samdev.utils.Parametro


import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SCF_Retorno_Pagamento_Itau extends FormulaBase {

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_RETORNO_DE_PAGAMENTO;
    }

    @Override
    public void executar() {
        SCFService scfService = instanciarService(SCFService.class);

        //**************************Fórmula gerada no dia 16/10/2025 ******************************
        List<TableMap> tmList = new ArrayList();
        TextFileLeitura txt = new TextFileLeitura(get("registros"))
        Aac10 aac10 = get("aac10");
        Abf01 abf01 = getSession().get(Abf01.class, Criterions.eq("abf01id", get("abf01id")))
        Long idDaa01 = null;
        int linha = 0;
        TableMap tm = new TableMap();
        List<String> inconsistencias = new ArrayList();

        while(txt.nextLine()){
            if(txt.getSubString(7, 8).equals("3") && txt.getSubString(13, 14).equals("J")){
                String posId = txt.getSubString(182, 202).trim();
                int pos = posId.indexOf(";", -1);

                Abf20 abf20 = null;
                Daa0102 daa0102 = null;
                Daa01 daa01 = null;
                Integer movim = pos == -1 ? 0 : Integer.parseInt(posId.substring(pos+1, posId.length()));  //número do movimento

                if(posId == -1) {
                    String serie = txt.getSubString(197, 201).trim();
                    serie = serie.length() > 0 ? serie : null;
                    def numero = txt.getSubString(191, 197).trim().length() == 0 ? null : Integer.parseInt(txt.getSubString(191, 197).trim());
                    try {
                        daa01 = scfService.buscaDocFinPagarPorNumSerParcQuita(numero, serie, serie, 0);

                        if(daa01 != null){
                            daa0102 = scfService.buscarIntegracaoPorNossoNumero(abf01.abf01id, 1, daa01.daa01nossoNum);
                            if(daa0102 != null) {
                                daa01 = daa0102.daa0102doc;
                                movim = daa0102.daa0102movim;
                            }
                        }

                    }catch (NonUniqueObject e) {
                        String inconsistencia = "Foi encontrado mais que um documento de número: " + daa01.daa01central.abb01num + " nesta empresa.";
                        inconsistencias.add(inconsistencia);
                    }
                }else {
                    idDaa01 = Long.parseLong(posId.substring(0, pos));
                    daa01 = getAcessoAoBanco().buscarRegistroUnicoById("Daa01", idDaa01);
                }

                if(daa01 == null){
                    if(posId == -1) {
                        String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrado nesta empresa.";
                        inconsistencias.add(inconsistencia);
                    }else {
                        String inconsistencia = "Documento de ID: " + idDaa01 + " não foi encontrado nesta empresa.";
                        inconsistencias.add(inconsistencia);
                    }
                }

                /**
                 * Validando o documento - Daa01 e Daa0102
                 */
                boolean validouDocumento = true;
                if(daa01 == null){
                    validouDocumento = false;

                }else{
                    if(daa01.daa01central.abb01quita_Zero > 0){
                        String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " já foi recebido.";
                        inconsistencias.add(inconsistencia);
                        validouDocumento = false;
                    }

                    daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movim);
                    if(daa0102 == null){
                        String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi enviado ao banco, porém consta no retorno.";
                        inconsistencias.add(inconsistencia);
                        validouDocumento = false;
                    }

                    if(daa01.daa01valor.compareTo(new BigDecimal(0.01)) != 0){ //Se o valor não for (0,01)
                        if(daa01.daa01valor.compareTo(new BigDecimal(txt.getSubString(99, 114)).divide(100)) != 0){
                            String inconsistencia = "O valor do documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " é diferente do valor recebido.";
                            inconsistencias.add(inconsistencia);
                            validouDocumento = false;
                        }
                    }

                    String codOcorrencia = txt.getSubString(230, 240);
                    String descricaoOcor = tratarDescricaoOcorrencia(codOcorrencia.trim());

                    if(descricaoOcor == null){
                        String inconsistencia = "A ocorrência " + txt.getSubString(230, 240) + " informada no retorno para o documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrada nos parâmetros de retorno do banco.";
                        inconsistencias.add(inconsistencia);
                        validouDocumento = false;
                    }
                }
                if(validouDocumento){
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
                    daa01.daa01dtPgto = LocalDate.parse( txt.getSubString(144, 152), formatter);

                    BigDecimal vlrLiq = new BigDecimal(txt.getSubString(99, 114));
                    daa01.daa01liquido =   vlrLiq / 100;

                    TableMap jsonDaa01 = daa01.daa01json != null ? daa01.daa01json : new TableMap();

                    BigDecimal vlrDesconto = new BigDecimal(txt.getSubString(114, 129));
                    BigDecimal vlrMulta = new BigDecimal(txt.getSubString(129, 144));
                    vlrMulta = vlrMulta / 100;
                    LocalDate dtLimiteDesc = daa01.daa01dtVctoN
                    LocalDate dtPgto = daa01.daa01dtPgto;

                    jsonDaa01.put("multa", vlrMulta)
                    if(vlrMulta == 0) jsonDaa01.put("juros", new BigDecimal(0));
                    jsonDaa01.put("desconto", vlrDesconto/100)
                    jsonDaa01.put("dt_limite_desc", dtLimiteDesc)

                    daa01.daa01json = jsonDaa01
                }
                tm.put("inconsistencias", inconsistencias);
                /**
                 * Exibindo documentos
                 */
                if(validouDocumento){
                    tm.put("daa01", daa01);
                    tm.put("abf20id", buscarPLF("400"));
                    tm.put("ocorrencia",tratarDescricaoOcorrencia(txt.getSubString(230, 240).trim()));
                }

                tmList.add(tm);

            }else if(txt.getSubString(7, 8).equals("3") && txt.getSubString(13, 14).equals("A")){
                String id = txt.getSubString(73, 93).trim();
                int pos = id.indexOf(";", -1);

                Abf20 abf20 = null;
                Daa0102 daa0102 = null;
                Daa01 daa01 = null;
                Integer movim = pos == -1 ? 0 : Integer.parseInt(id.substring(pos+1, id.length()));  //número do movimento


                /**
                 * Validando o documento - Daa01 e Daa0102
                 */
                boolean validouDocumento = true;
                if(daa01 == null){
                    validouDocumento = false;

                }else{
                    if(daa01.daa01central.abb01quita_Zero > 0){
                        String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " já foi recebido.";
                        inconsistencias.add(inconsistencia);
                        validouDocumento = false;
                    }

                    daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movim);
                    if(daa0102 == null){
                        String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi enviado ao banco, porém consta no retorno.";
                        inconsistencias.add(inconsistencia);
                        validouDocumento = false;
                    }

                    if(daa01.daa01valor.compareTo(new BigDecimal(0.01)) != 0){ //Se o valor não for (0,01)
                        if(daa01.daa01valor.compareTo(new BigDecimal(txt.getSubString(99, 114)).divide(100)) != 0){
                            String inconsistencia = "O valor do documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " é diferente do valor recebido.";
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
                }
            }

        }
        put("tmList", tmList);

    }


    private String buscarDescricaoOcorrencia(String codigo) {
        if (codigo == null) return "";

        switch (codigo) {
            case "00": return "PAGAMENTO EFETUADO";
            case "AE": return "DATA DE PAGAMENTO ALTERADA";
            case "AG": return "NÚMERO DO LOTE INVÁLIDO";
            case "AH": return "NÚMERO SEQUENCIAL DO REGISTRO NO LOTE INVÁLIDO";
            case "AI": return "PRODUTO DEMONSTRATIVO DE PAGAMENTO NÃO CONTRATADO";
            case "AJ": return "TIPO DE MOVIMENTO INVÁLIDO";
            case "AL": return "CÓDIGO DO BANCO FAVORECIDO INVÁLIDO";
            case "AM": return "AGÊNCIA DO FAVORECIDO INVÁLIDA";
            case "AN": return "CONTA CORRENTE DO FAVORECIDO INVÁLIDA";
            case "AO": return "NOME DO FAVORECIDO INVÁLIDO";
            case "AP": return "DATA DE PAGAMENTO / DATA DE VALIDADE / HORA DE LANÇAMENTO / ARRECADAÇÃO / APURAÇÃO INVÁLIDA";
            case "AQ": return "QUANTIDADE DE REGISTROS MAIOR QUE 999999";
            case "AR": return "VALOR ARRECADADO / LANÇAMENTO INVÁLIDO";
            case "BC": return "NOSSO NÚMERO INVÁLIDO";
            case "BD": return "PAGAMENTO AGENDADO";
            case "BE": return "PAGAMENTO AGENDADO COM FORMA ALTERADA PARA OP";
            case "BI": return "CNPJ / CPF DO FAVORECIDO NO SEGMENTO J-52 ou B INVÁLIDO";
            case "BL": return "VALOR DA PARCELA INVÁLIDO";
            case "CD": return "CNPJ / CPF INFORMADO DIVERGENTE DO CADASTRADO";
            case "CE": return "PAGAMENTO CANCELADO";
            case "CF": return "VALOR DO DOCUMENTO INVÁLIDO";
            case "CG": return "VALOR DO ABATIMENTO INVÁLIDO";
            case "CH": return "VALOR DO DESCONTO INVÁLIDO";
            case "CI": return "CNPJ / CPF / IDENTIFICADOR / INSCRIÇÃO ESTADUAL / INSCRIÇÃO NO CAD / ICMS INVÁLIDO";
            case "CJ": return "VALOR DA MULTA INVÁLIDO";
            case "CK": return "TIPO DE INSCRIÇÃO INVÁLIDA";
            case "CL": return "VALOR DO INSS INVÁLIDO";
            case "CM": return "VALOR DO COFINS INVÁLIDO";
            case "CN": return "CONTA NÃO CADASTRADA";
            case "CO": return "VALOR DE OUTRAS ENTIDADES INVÁLIDO";
            case "CP": return "CONFIRMAÇÃO DE OP CUMPRIDA";
            case "CQ": return "SOMA DAS FATURAS DIFERE DO PAGAMENTO";
            case "CR": return "VALOR DO CSLL INVÁLIDO";
            case "CS": return "DATA DE VENCIMENTO DA FATURA INVÁLIDA";
            case "DA": return "NÚMERO DE DEPEND. SALÁRIO FAMILIA INVALIDO";
            case "DB": return "NÚMERO DE HORAS SEMANAIS INVÁLIDO";
            case "DC": return "SALÁRIO DE CONTRIBUIÇÃO INSS INVÁLIDO";
            case "DD": return "SALÁRIO DE CONTRIBUIÇÃO FGTS INVÁLIDO";
            case "DE": return "VALOR TOTAL DOS PROVENTOS INVÁLIDO";
            case "DF": return "VALOR TOTAL DOS DESCONTOS INVÁLIDO";
            case "DG": return "VALOR LÍQUIDO NÃO NUMÉRICO";
            case "DH": return "VALOR LIQ. INFORMADO DIFERE DO CALCULADO";
            case "DI": return "VALOR DO SALÁRIO-BASE INVÁLIDO";
            case "DJ": return "BASE DE CÁLCULO IRRF INVÁLIDA";
            case "DK": return "BASE DE CÁLCULO FGTS INVÁLIDA";
            case "DL": return "FORMA DE PAGAMENTO INCOMPATÍVEL COM HOLERITE";
            case "DM": return "E-MAIL DO FAVORECIDO INVÁLIDO";
            case "DV": return "DOC / TED DEVOLVIDO PELO BANCO FAVORECIDO";
            case "D0": return "FINALIDADE DO HOLERITE INVÁLIDA";
            case "D1": return "MÊS DE COMPETENCIA DO HOLERITE INVÁLIDA";
            case "D2": return "DIA DA COMPETENCIA DO HOLERITE INVÁLIDA";
            case "D3": return "CENTRO DE CUSTO INVÁLIDO";
            case "D4": return "CAMPO NUMÉRICO DA FUNCIONAL INVÁLIDO";
            case "D5": return "DATA INÍCIO DE FÉRIAS NÃO NUMÉRICA";
            case "D6": return "DATA INÍCIO DE FÉRIAS INCONSISTENTE";
            case "D7": return "DATA FIM DE FÉRIAS NÃO NUMÉRICO";
            case "D8": return "DATA FIM DE FÉRIAS INCONSISTENTE";
            case "D9": return "NÚMERO DE DEPENDENTES IR INVÁLIDO";
            case "EM": return "CONFIRMAÇÃO DE OP EMITIDA";
            case "EX": return "DEVOLUÇÃO DE OP NÃO SACADA PELO FAVORECIDO";
            case "E0": return "TIPO DE MOVIMENTO HOLERITE INVÁLIDO";
            case "E1": return "VALOR 01 DO HOLERITE / INFORME INVÁLIDO";
            case "E2": return "VALOR 02 DO HOLERITE / INFORME INVÁLIDO";
            case "E3": return "VALOR 03 DO HOLERITE / INFORME INVÁLIDO";
            case "E4": return "VALOR 04 DO HOLERITE / INFORME INVÁLIDO";
            case "FC": return "PAGAMENTO EFETUADO ATRAVÉS DE FINANCIAMENTO COMPROR";
            case "FD": return "PAGAMENTO EFETUADO ATRAVÉS DE FINANCIAMENTO DESCOMPROR";
            case "HA": return "ERRO NO LOTE";
            case "HM": return "ERRO NO REGISTRO HEADER DE ARQUIVO";
            case "IB": return "VALOR DO DOCUMENTO INVÁLIDO";
            case "IC": return "VALOR DO ABATIMENTO INVÁLIDO";
            case "ID": return "VALOR DO DESCONTO INVÁLIDO";
            case "IE": return "VALOR DA MORA INVÁLIDO";
            case "IF": return "VALOR DA MULTA INVÁLIDO";
            case "IG": return "VALOR DA DEDUÇÃO INVÁLIDO";
            case "IH": return "VALOR DO ACRÉSCIMO INVÁLIDO";
            case "II": return "DATA DE VENCIMENTO INVÁLIDA";
            case "IJ": return "COMPETÊNCIA / PERÍODO REFERÊNCIA / PARCELA INVÁLIDA";
            case "IK": return "TRIBUTO NÃO LIQUIDÁVEL VIA SISPAG OU NÃO CONVENIADO COM ITAÚ";
            case "IL": return "CÓDIGO DE PAGAMENTO / EMPRESA / RECEITA INVÁLIDO";
            case "IM": return "TIPO X FORMA NÃO COMPATÍVEL";
            case "IN": return "BANCO / AGÊNCIA NÃO CADASTRADOS";
            case "IO": return "DAC / VALOR / COMPETÊNCIA / IDENTIFICADOR DO LACRE INVÁLIDO";
            case "IP": return "DAC DO CÓDIGO DE BARRAS INVÁLIDO";
            case "IQ": return "DÍVIDA ATIVA OU NÚMERO DE ETIQUETA INVÁLIDO";
            case "IR": return "PAGAMENTO ALTERADO";
            case "IS": return "CONCESSIONÁRIA NÃO CONVENIADA COM ITAÚ";
            case "IT": return "VALOR DO TRIBUTO INVÁLIDO";
            case "IU": return "VALOR DA RECEITA BRUTA ACUMULADA INVÁLIDO";
            case "IV": return "NÚMERO DO DOCUMENTO ORIGEM / REFERÊNCIA INVÁLIDO";
            case "IX": return "CÓDIGO DO PRODUTO INVÁLIDO";
            case "LA": return "DATA DE PAGAMENTO DE UM LOTE ALTERADA";
            case "LC": return "LOTE DE PAGAMENTOS CANCELADO";
            case "NA": return "PAGAMENTO CANCELADO POR FALTA DE AUTORIZAÇÃO";
            case "NB": return "IDENTIFICAÇÃO DO TRIBUTO INVÁLIDA";
            case "NC": return "EXERCÍCIO (ANO BASE) INVÁLIDO";
            case "ND": return "CÓDIGO RENAVAM NÃO ENCONTRADO/INVÁLIDO";
            case "NE": return "UF INVÁLIDA";
            case "NF": return "CÓDIGO DO MUNICÍPIO INVÁLIDO";
            case "NG": return "PLACA INVÁLIDA";
            case "NH": return "OPÇÃO/PARCELA DE PAGAMENTO INVÁLIDA";
            case "NI": return "TRIBUTO JÁ FOI PAGO OU ESTÁ VENCIDO";
            case "NR": return "OPERAÇÃO NÃO REALIZADA";
            case "PD": return "AQUISIÇÃO CONFIRMADA (EQUIVALE A OCORRÊNCIA 02 NO LAYOUT DE RISCO SACADO)";
            case "RJ": return "REGISTRO REJEITADO";
            case "RS": return "PAGAMENTO DISPONÍVEL PARA ANTECIPAÇÃO NO RISCO SACADO – MODALIDADE RISCO SACADO PÓS AUTORIZADO";
            case "SS": return "PAGAMENTO CANCELADO POR INSUFICIÊNCIA DE SALDO / LIMITE DIÁRIO DE PAGTO";
            case "TA": return "LOTE NÃO ACEITO - TOTAIS DO LOTE COM DIFERENÇA";
            case "TI": return "TITULARIDADE INVÁLIDA";
            case "X1": return "FORMA INCOMPATÍVEL COM LAYOUT 010";
            case "X2": return "NÚMERO DA NOTA FISCAL INVÁLIDO";
            case "X3": return "IDENTIFICADOR DE NF/CNPJ INVÁLIDO";
            case "X4": return "FORMA 32 INVÁLIDA";
            default: return "CÓDIGO DESCONHECIDO";
        }
    }

    private String tratarDescricaoOcorrencia(String codOcorrencia){
        String ocorrencias = "";
        for(int i = 0; i < codOcorrencia.length(); i += 2){
            String codigo = codOcorrencia.substring(i, Math.min(i + 2, codOcorrencia.length()));
            ocorrencias += buscarDescricaoOcorrencia(codigo) + ";";
        }

        return ocorrencias;
    }

    private String codigoPLF(String codigoOcorrencia) {
        switch(codigoOcorrencia) {
            case "400": return "400";
            default: return null;
        }
    }

    private Long buscarPLF(String codigoPLF) {
        Abf20 abf20 = getAcessoAoBanco().buscarRegistroUnico("SELECT abf20id FROM Abf20 WHERE abf20codigo = :P1 " + getSamWhere().getWherePadrao("AND", Abf20.class) , Parametro.criar("P1", codigoPLF));
        return abf20 == null ? null : abf20.abf20id;
    }

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDUifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDUifQ==