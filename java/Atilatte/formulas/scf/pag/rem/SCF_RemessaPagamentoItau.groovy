package Atilatte.formulas.scf.pag.rem

import com.ctc.wstx.util.StringUtil
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe02;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import java.time.LocalDate
import java.time.temporal.ChronoUnit

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe03
import sam.model.entities.ab.Abf01
import sam.model.entities.da.Daa01
import sam.model.entities.da.Daa0102
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro
import sam.server.scf.service.SCFService

public class SCF_RemessaPagamentoItau extends FormulaBase {

    public final static String PATTERN_DDMMYYYY = "ddMMyyyy";
    public final static String PATTERN_HHMMSS = "HHmmss";

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_REMESSA_DE_PAGAMENTO;
    }

    @Override
    public void executar() {
        TextFile txt = new TextFile();
        Integer numRemessa = get("numRemessa");
        LocalDate dataRemessa = get("dataRemessa");
        Integer movimento = get("movimento");
        Aac10 aac10 = get("aac10");
        Abf01 abf01 = get("abf01");
        List<Daa01> daa01s = get("daa01s");
        TableMap tmBanco = abf01.abf01json != null ? abf01.abf01json : new TableMap();
        SCFService scfService = instanciarService(SCFService.class);

        // Municipio Empresa
        Aag0201 municEmpresa = getSession().get(Aag0201.class, aac10.aac10municipio.aag0201id);

        // UF Empresa
        Aag02 ufEmpresa = getSession().get(Aag02.class, municEmpresa.aag0201uf.aag02id);

        def contador = 0;
        def numLote = 0;
        def qtdDetalheLote = 0;
        def totalDocsLote = 0.00;

        /*
            HEADER do Arquivo
         */
        txt.print("341");                                                                                       //001-003
        txt.print(0, 4);                                                                          //004-007
        txt.print("0");                                                                                         //008-008
        txt.print(StringUtils.space(6));                                                                //009-014
        txt.print("080")                                                                                        //015-017
        txt.print(aac10.aac10ti == 0 ? "2" : "1");                                                              //018-018
        txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);                                       //019-032
        txt.print(StringUtils.space(20))                                                                //033-052
        txt.print(abf01.abf01agencia, 5, '0', true);                          //053-057
        txt.print(StringUtils.space(1))                                                                 //058-058
        txt.print(abf01.abf01conta, 12, '0', true);                           //059-070
        txt.print(StringUtils.space(1));                                                                //071-071
        txt.print(abf01.abf01digConta, 1, '0', true);                         //072-072
        txt.print(aac10.aac10rs, 30, true, true);                                   //073-102
        txt.print("BANCO ITAU", 30);                                                              //103-132
        txt.print(StringUtils.space(10));                                                               //133-142
        txt.print("1");                                                                                         //143-143
        txt.print(MDate.date().format(PATTERN_DDMMYYYY));                                                       //144-151
        txt.print(MDate.time().format(PATTERN_HHMMSS));                                                         //152-157
        txt.print(0, 9);                                                                          //158-166
        txt.print(0, 5);                                                                          //167-161
        txt.print(StringUtils.space(69));                                                               //172-240
        txt.newLine();
        contador++;


        /*
            LOTE
         */

        def fpAnterior = "";

        for (daa01 in daa01s) {

            // Entidade Documento
            Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);

            // Entidade (Cliente)
            Abe02 abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

            // Campos Livre Entidade
            TableMap tmAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();

            Daa0102 daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movimento);

            if (!fpAnterior.equals(tmAbe01.getString("forma_pagamento"))) { // Novo Lote

                if (qtdDetalheLote > 0) { //Se a quantidade de detalhes do lote for maior que zero significa que o lote anterior precisa ser fechado
                    trailerLote(txt, numLote, qtdDetalheLote, totalDocsLote);
                    contador++;
                    qtdDetalheLote = 0;
                }

                fpAnterior = tmAbe01.getString("forma_pagamento");
                totalDocsLote = 0;

                /*
                    HEADER DE LOTE
                 */
                if (tmAbe01.getString("forma_pagamento") == "30" || tmAbe01.getString("forma_pagamento") == "31") {// header segmento J
                    txt.print("341");                                                                                                                //001-003
                    txt.print(++numLote, 4);                                                                                                 //004-007
                    txt.print("1");                                                                                                                  //008-008
                    txt.print("C");                                                                                                                  //009-009
                    txt.print("20");                                                                                                                 //010-011
                    txt.print(tmAbe01.getString("forma_pagamento"), 2);                                                                 //012-013
                    txt.print("040");                                                                                                                //014-016
                    txt.print(StringUtils.space(1));                                                                                         //017-017
                    txt.print(aac10.aac10ti == 0 ? "2" : "1");                                                                                       //018-018
                    txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);                                                                //019-032
                    txt.print(StringUtils.space(20));                                                                                        //033-052
                    txt.print(abf01.abf01agencia, 5, '0', true);                                                   //053-057
                    txt.print(StringUtils.space(1));                                                                                        //058-058
                    txt.print(abf01.abf01conta, 12, '0', true);                                                    //059-070
                    txt.print(StringUtils.space(1));                                                                                        //071-071
                    txt.print(abf01.abf01digConta, 1, '0', true);                                                  //072-072
                    txt.print(aac10.aac10rs, 30, true, true);                                                           //073-102
                    txt.print(StringUtils.space(30));                                                                                       //103-132
                    txt.print(StringUtils.space(10));                                                                                       //133-142
                    txt.print(aac10.aac10endereco, 30, true, true);                                                     //143-172
                    txt.print(aac10.aac10numero, 5, '0', true);                                                                                        //173-177
                    txt.print(aac10.aac10complem, 15, true, true);                                                      //178-192
                    txt.print(aac10.aac10municipio == null ? null : municEmpresa.aag0201nome, 20);                                          //193-212
                    txt.print(aac10.aac10cep, 8);                                                                                           //213-220
                    txt.print(aac10.aac10municipio == null ? null : ufEmpresa.aag02uf, 2);                                                  //221-222
                    txt.print(StringUtils.space(8));                                                                                        //223-230
                    txt.print(StringUtils.space(10));                                                                                       //231-240
                    txt.newLine();
                    contador++;
                } else {
                    txt.print("341");                                                                                                               //001-003
                    txt.print(++numLote, 4);                                                                                                //004-007
                    txt.print("1");                                                                                                                 //008-008
                    txt.print("C");                                                                                                                 //009-009
                    txt.print("20");                                                                                                                //010-011
                    txt.print(tmAbe01.getString("forma_pagamento"), 2);                                                                //012-013
                    txt.print("040");                                                                                                               //014-016
                    txt.print(StringUtils.space(1));                                                                                        //017-017
                    txt.print(aac10.aac10ti == 0 ? "2" : "1");                                                                                      //018-018
                    txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);                                                               //019-032
                    txt.print(StringUtils.space(4));                                                                                        //033-036
                    txt.print(StringUtils.space(16));                                                                                       //037-052
                    txt.print(abf01.abf01agencia, 5, '0', true);                                                   //053-057
                    txt.print(StringUtils.space(1));                                                                                         //058-058
                    txt.print(abf01.abf01conta, 12, '0', true);                                                    //059-070
                    txt.print(StringUtils.space(1));                                                                                         //071-071
                    txt.print(abf01.abf01digConta, 1, '0', true);                                                  //072-072
                    txt.print(aac10.aac10rs, 30, true, true);                                                            //073-102
                    txt.print(StringUtils.space(30));                                                                                        //103-132
                    txt.print(StringUtils.space(10));                                                                                        //133-142
                    txt.print(aac10.aac10endereco, 30, true, true);                                                      //143-172
                    txt.print(aac10.aac10numero, 5, '0', true);                                                                                         //173-177
                    txt.print(aac10.aac10complem, 15, true, true);                                                       //178-192
                    txt.print(aac10.aac10municipio == null ? null : municEmpresa.aag0201nome, 20);                                   //193-212
                    txt.print(aac10.aac10cep, 8);                                                                                            //213-220
                    txt.print(aac10.aac10municipio == null ? null : ufEmpresa.aag02uf, 2);                              //221-222
                    txt.print(StringUtils.space(8));                                                                                         //223-230
                    txt.print(StringUtils.space(10));                                                                                        //231-240
                    txt.newLine();
                    contador++;
                }
            }

            if (tmAbe01.getString("forma_pagamento") == "30" || tmAbe01.getString("forma_pagamento") == "31") {
                /*
                    DETALHE - SEGMENTO J
                */
                txt.print("341");                                                                                                                                                                //001-003
                txt.print(numLote, 4);                                                                                                                                                 //004-007
                txt.print("3");                                                                                                                                                                  //008-008
                txt.print(++qtdDetalheLote, 5);                                                                                                                                          //009-013
                txt.print("J");                                                                                                                                                                  //014-014
                txt.print("000");                                                                                                                                                               //015-017
                String codBarras = verificarCodigoDeBarras(daa01.daa01codBarras)
                txt.print(codBarras, 44);                                                                                                                                    //018-061
                txt.print(abe01.abe01nome, 30, true, true);                                                                                                         //062-091
                txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYYYY));                                                                                                                         //092-099
                txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);                                                                                           //100-114

                BigDecimal desconto = new BigDecimal(0);
                if (daa01.daa01json != null && daa01.daa01json.getDate("dt_limite_desc") && daa0102.daa0102dtPgto.compareTo(daa01.daa01json.getDate("dt_limite_desc")) <= 0) {
                    desconto = daa01.daa01json.getBigDecimal("desconto").abs() * -1;
                }
                txt.print(desconto.multiply(new BigDecimal(100)).intValue(), 15);                                                                                                   //115-129

                Long dias = DateUtils.dateDiff(daa01.daa01dtVctoN, daa0102.daa0102dtPgto, ChronoUnit.DAYS);
                BigDecimal jme = daa01.daa01json != null && daa01.daa01json.getBigDecimal("encargos") != null ? daa01.daa01json.getBigDecimal("encargos") : BigDecimal.ZERO;
                if (dias > 0) {
                    BigDecimal multa = daa01.daa01json != null && daa01.daa01json.getBigDecimal("multa") != null ? daa01.daa01json.getBigDecimal("multa") : BigDecimal.ZERO;
                    jme = jme.add(multa);
                    BigDecimal juros = daa01.daa01json != null && daa01.daa01json.getBigDecimal("juros") != null ? daa01.daa01json.getBigDecimal("juros") : BigDecimal.ZERO;
                    jme = jme.add(juros.multiply(new BigDecimal(dias)));
                }
                txt.print(jme.multiply(new BigDecimal(100)).intValue(), 15);                                                                                                        //130-144

                txt.print(daa0102.daa0102dtPgto.format(PATTERN_DDMMYYYY));                                                                                                                          //145-152
                txt.print(daa01.daa01valor.add(jme).add(desconto).multiply(new BigDecimal(100)).intValue(), 15);                                                                   //153-167
                txt.print(0, 15);                                                                                                                                                 //168-182
                txt.print(daa01.daa01id + "E" + movimento, 20);                                                                                                                         //183-202
                txt.print(StringUtils.space(13));                                                                                                                                       //203-215
                txt.print(StringUtils.space(15));                                                                                                                                       //216-230
                txt.print(StringUtils.space(10));                                                                                                                                       //231-240

                totalDocsLote = totalDocsLote + daa01.daa01valor + jme + desconto;

                txt.newLine();
                contador++;
                if(tmAbe01.getString("forma_pagamento") != "47"){
                    /**
                     * DETALHE - SEGMENTO J-52
                     */
                    txt.print("341");                                                                                                                                                             //001-003
                    txt.print(numLote, 4);                                                                                                                                                //004-007
                    txt.print("3");                                                                                                                                                               //008-008
                    txt.print(++qtdDetalheLote, 5);                                                                                                                                       //009-013
                    txt.print("J");                                                                                                                                                               //014-014
                    txt.print("000");                                                                                                                                                             //015-017
                    txt.print("52");                                                                                                                                                              //018-019
                    txt.print(abe01.abe01ti == 0 ? "2" : "1");                                                                                                                                    //020-020
                    txt.print(StringUtils.extractNumbers(abe01.abe01ni), 15, "0", true);                                                                        //021-035
                    txt.print(abe01.abe01nome, 40, true, true);                                                                                                         //036-075
                    txt.print(aac10.aac10ti == 0 ? "2" : "1" , 1);                                                                                                                                 //076-076
                    txt.print(StringUtils.extractNumbers(aac10.aac10ni), 15);                                                                         //077-091
                    txt.print(aac10.aac10rs, 40)                                                                                                                                         //092-131
                    txt.print(StringUtils.space(1));                                                                                                                                      //132-132
                    txt.print(StringUtils.space(15));                                                                                                                                     //133-147
                    txt.print(StringUtils.space(40));                                                                                                                                     //148-187
                    txt.print(StringUtils.space(53));                                                                                                                                     //188-240
                    txt.newLine();
                    contador++;
                }else{
                    /**
                     * DETALHE - SEGMENTO J-52 PIX
                     */
                    if(tmAbe01.getString("chave_pix") == null) interromper("Necessário informar a chave PIX na entidade " + abe01.abe01codigo + " - " + abe01.abe01na);
                    txt.print("341");                                                                                                                                                             //001-003
                    txt.print(numLote, 4);                                                                                                                                                //004-007
                    txt.print("3");                                                                                                                                                               //008-008
                    txt.print(++qtdDetalheLote, 5);                                                                                                                                       //009-013
                    txt.print("J");                                                                                                                                                               //014-014
                    txt.print("000");                                                                                                                                                             //015-017
                    txt.print("52");                                                                                                                                                              //018-019
                    txt.print(abe01.abe01ti == 0 ? "2" : "1");                                                                                                                                    //020-020
                    txt.print(StringUtils.extractNumbers(abe01.abe01ni), 15, "0", true);                                                                        //021-035
                    txt.print(abe01.abe01nome, 40, true, true);                                                                                                         //036-075
                    txt.print(aac10.aac10ti, 1);                                                                                                                                          //076-076
                    txt.print(StringUtils.extractNumbers(aac10.aac10ni), 15);                                                                         //077-091
                    txt.print(aac10.aac10rs, 40);                                                                                                                                         //092-131
                    txt.print(tmAbe01.getString("chave_pix"), 77, "0", true);                                                                               //132-208
                    txt.print(StringUtils.space(32));                                                                                                                                     //188-240
                    txt.newLine();
                    contador++;
                }
            }else if(tmAbe01.getString("forma_pagamento") == "45"){ // PIX Transferência
                /**
                 * DETALHE - SEGMENTO A
                 */
                Abb01 abb01 = getSession().get(Abb01.class, daa01.daa01central.abb01id);
                txt.print("341");                                                                                                                                                                 //001-003
                txt.print(numLote, 4);                                                                                                                                                   //004-007
                txt.print("3");                                                                                                                                                                  //008-008
                txt.print(++qtdDetalheLote, 5);                                                                                                                                         //009-013
                txt.print("A");                                                                                                                                                                 //014-014
                txt.print("000");                                                                                                                                                               //015-017
                txt.print("009");                                                                                                                                                               //018-020
                /*
                    Banco Favorecido
                 */
                txt.print(tmAbe01.getString("cod_banco"), 3);                                                                                                                      //021-023
                if(tmAbe01.getString("cod_banco") == "341" || tmAbe01.getString("cod_banco") == "409"){ // Itaú/Unibanco
                    txt.print(0, 1) // 024-024
                    txt.print(tmAbe01.getString("agencia_banco"), 4) // 025-028
                    txt.print(StringUtils.space(1)) // 029-029
                    txt.print(0, 6) // 030-035
                    txt.print(tmAbe01.getString("conta_corrente"), 6, '0', true) // 036-041
                    txt.print(StringUtils.space(1)) // 042-042
                    txt.print(tmAbe01.getString("digito_conta"),1) // 043-043
                }else{
                    txt.print(tmAbe01.getString("agencia_banco"), 5) // 024-028
                    txt.print(StringUtils.space(1)) // 029-029
                    txt.print(tmAbe01.getString("conta_corrente"), 6, '0', true) // 030-041
                    txt.print(StringUtils.space(1)) // 042-042
                    txt.print(tmAbe01.getString("digito_conta"),1) // 043-043
                }
                txt.print(abe01.abe01nome, 30, true, true)                                                                                                        //044-073
                txt.print(daa01.daa01id + "E" + movimento, 20);                                                                                                                       //074-0093
                txt.print(daa0102.daa0102dtPgto.format(PATTERN_DDMMYYYY));                                                                                                                    //094-101
                txt.print("REA");                                                                                                                                                             //102-104
                txt.print(0, 8);                                                                                                                                                //105-112
                txt.print(tmAbe01.getString("identificacao_pix"), 2);                                                                                                            //113-114
                txt.print(0, 5);                                                                                                                                                //115-119
                txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);                                                                                        //120-134
                txt.print(StringUtils.space(15));                                                                                                                                     //135-149
                txt.print(StringUtils.space(5));                                                                                                                                      //150-154
                txt.print(daa0102.daa0102dtPgto.format(PATTERN_DDMMYYYY), 8);                                                                                                         //155-162
                txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);                                                                                         //163-177
                txt.print(StringUtils.space(20));                                                                                                                                     //178-197
                txt.print("000000");                                                                                                                                                          //198-203
                txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, '0', true);                                                                         //204-217
                txt.print(tmAbe01.getString("finalidade_doc"), 2, '0', true);                                                                           //218-219
                txt.print(StringUtils.space(5));                                                                                                                                       //220-224
                txt.print(StringUtils.space(5));                                                                                                                                       //225-229
                txt.print(0, 1);                                                                                                                                                 //230-230
                txt.print(StringUtils.space(10));                                                                                                                                      //231-240
                totalDocsLote = totalDocsLote + daa01.daa01valor;
                txt.newLine();
                contador++;
            }else {
                /**
                 * DETALHE - SEGMENTO A
                 */
                Abb01 abb01 = getSession().get(Abb01.class, daa01.daa01central.abb01id);
                txt.print("341");                                                                                                                                                                 //001-003
                txt.print(numLote, 4);                                                                                                                                                 //004-007
                txt.print("3");                                                                                                                                                                  //008-008
                txt.print(++qtdDetalheLote, 5);                                                                                                                                         //009-013
                txt.print("A");                                                                                                                                                                 //014-014
                txt.print("000");                                                                                                                                                               //015-017
                txt.print("000");                                                                                                                                                              //018-020 // PIX - 009

                /*
                   Banco Favorecido
                */
                txt.print(tmAbe01.getString("cod_banco"), 3);                                                                                                                      //021-023
                if(tmAbe01.getString("cod_banco") == "341" || tmAbe01.getString("cod_banco") == "409"){ // Itaú/Unibanco
                    txt.print(0, 1) // 024-024
                    txt.print(tmAbe01.getString("agencia_banco"), 4) // 025-028
                    txt.print(StringUtils.space(1)) // 029-029
                    txt.print(0, 6) // 030-035
                    txt.print(tmAbe01.getString("conta_corrente"), 6, '0', true) // 036-041
                    txt.print(StringUtils.space(1)) // 042-042
                    txt.print(tmAbe01.getString("digito_conta"),1) // 043-043
                }else{
                    txt.print(tmAbe01.getString("agencia_banco"), 5) // 024-028
                    txt.print(StringUtils.space(1)) // 029-029
                    txt.print(tmAbe01.getString("conta_corrente"), 6, '0', true) // 030-041
                    txt.print(StringUtils.space(1)) // 042-042
                    txt.print(tmAbe01.getString("digito_conta"),1) // 043-043
                }
                txt.print(abe01.abe01nome, 30, true, true)                                                                                                        //044-073
                txt.print(daa01.daa01id + "E" + movimento, 20);                                                                                                                       //074-0093
                txt.print(daa0102.daa0102dtPgto.format(PATTERN_DDMMYYYY));                                                                                                                    //094-101
                txt.print("REA");                                                                                                                                                             //102-104
                txt.print(0, 8);                                                                                                                                                //105-112
                txt.print(0, 2);                                                                                                                                                //113-114
                txt.print(0, 5);                                                                                                                                                //115-119
                txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);                                                                                        //120-134
                txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);                                                                                         //135-149
                txt.print(StringUtils.space(5));                                                                                                                                      //150-154
                txt.print(daa0102.daa0102dtPgto.format(PATTERN_DDMMYYYY), 8);                                                                                                         //155-162
                txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);                                                                                         //163-177
                txt.print(StringUtils.space(20));                                                                                                                                     //178-197
                txt.print("000000");                                                                                                     //198-203
                txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, '0', true);                                                                                                     //204-217
                txt.print(tmAbe01.getString("finalidade_doc"), 2, '0', true);                                                                           //218-219
                txt.print(StringUtils.space(5));                                                                           //220-224
                txt.print(StringUtils.space(5));                                                                                                                                       //225-229
                txt.print(0, 1);                                                                                                                                                 //230-230
                txt.print(StringUtils.space(10));                                                                                                                                      //231-240
                totalDocsLote = totalDocsLote + daa01.daa01valor;

                txt.newLine();
                contador++;

                /**
                 * DETALHE - SEGMENTO J-52
                 */
                txt.print("341");                                                                                                                                                             //001-003
                txt.print(numLote, 4);                                                                                                                                                //004-007
                txt.print("3");                                                                                                                                                               //008-008
                txt.print(++qtdDetalheLote, 5);                                                                                                                                       //009-013
                txt.print("J");                                                                                                                                                               //014-014
                txt.print("000");                                                                                                                                                             //015-017
                txt.print("52");                                                                                                                                                              //018-019
                txt.print(aac10.aac10ti == 0 ? "2" : "1");                                                                                                                                    //020-020
                txt.print(StringUtils.extractNumbers(aac10.aac10ni), 15, "0", true);                                                                        //021-035
                txt.print(aac10.aac10rs, 40, true, true);                                                                                                         //036-075
                txt.print(abe01.abe01ti, 1);                                                                                                                                          //076-076
                txt.print(StringUtils.extractNumbers(abe01.abe01ni), 15);                                                                         //077-091
                txt.print(abe01.abe01nome, 40)                                                                                                                                         //092-131
                txt.print(StringUtils.space(1));                                                                                                                                      //132-132
                txt.print(StringUtils.space(15));                                                                                                                                     //133-147
                txt.print(StringUtils.space(40));                                                                                                                                     //148-187
                txt.print(StringUtils.space(53));                                                                                                                                     //188-240
                txt.newLine();
                contador++;
            }
        }

        trailerLote(txt, numLote, qtdDetalheLote, totalDocsLote);
        contador++;

        /**
         * TRAILLER DO ARQUIVO
         */
        txt.print("341");                                                                                                       //001-003
        txt.print("9999");                                                                                                      //004-007
        txt.print("9");                                                                                                         //008-008
        txt.print(StringUtils.space(9));                                                                                //009-017
        txt.print(numLote, 6);                                                                                          //018-023
        txt.print(contador + 1, 6);                                                                                     //024-029
        txt.print(StringUtils.space(211));                                                                              //030-240
        txt.newLine();

        put("txt", txt);

    }

    private void trailerLote(TextFile txt, Integer numLote, Integer qtDetalheLote, BigDecimal totalDocsLote) {

        /**
         * TRAILER DO LOTE
         */
        txt.print("341");                                                                                                           //001-003
        txt.print(numLote, 4);                                                                                              //004-007
        txt.print("5");                                                                                                             //008-008
        txt.print(StringUtils.space(9));                                                                                    //009-017
        txt.print(2 + qtDetalheLote, 6);                                                                                    //018-023
        txt.print(totalDocsLote.multiply(new BigDecimal(100)).intValue(), 18);                                          //024-041
        txt.print(0, 18);                                                                                              //042-059
        txt.print(StringUtils.space(171));                                                                                   //060-230
        txt.print(StringUtils.space(10));                                                                                    //231-240
        txt.newLine();

    }
    private String verificarCodigoDeBarras(String codBarras){
        String codAlterado;
        if(codBarras == null) interromper("Existem documentos sem o código de barras informado.")
        if(codBarras.length() == 44) return codBarras;

        String codBanco = codBarras.substring(0, 3);
        String codMoeda = codBarras.substring(3,4);
        String codLivre = codBarras.substring(4, 9) + codBarras.substring(10,20) + codBarras.substring(21,31);
        String DAC = codBarras.substring(32,33);
        String fatorVencimento = codBarras.substring(33, 37);
        String valorTitulo = codBarras.substring(37, 47);

        return codAlterado = codBanco + codMoeda + DAC + fatorVencimento + valorTitulo + codLivre;

    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDQifQ==