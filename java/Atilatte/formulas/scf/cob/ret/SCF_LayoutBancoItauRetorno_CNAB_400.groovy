package Atilatte.formulas.scf

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

class SCF_LayoutBancoItauRetorno_CNAB_400 extends FormulaBase {

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


                    jsons.put("multa", vlrMulta)
                    if(vlrMulta == 0) jsons.put("juros", new BigDecimal(0));
                    jsons.put("desconto", vlrDesconto/100)
                    jsons.put("dt_limite_desc", dtLimiteDesc)

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
            case "02": return "Entrada confirmada";
            case "03": return "Entrada rejeitada";
            case "06": return "Liquidação normal";
            case "10": return "Baixado conforme instruções da Agência";
            default: return null;
        }
    }

    private String codigoPLF(String codigoOcorrencia) {
        switch(codigoOcorrencia) {
            case "06": return "099";
            default: return null;
        }
    }

    private Long buscarPLF(String codigoPLF) {
        Abf20 abf20 = getAcessoAoBanco().buscarRegistroUnico("SELECT abf20id FROM Abf20 WHERE abf20codigo = :P1 " + getSamWhere().getWherePadrao("AND", Abf20.class) , Parametro.criar("P1", codigoPLF));
        return abf20 == null ? null : abf20.abf20id;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==