package Silcon.formulas.scf.cob.rem

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb0102
import sam.model.entities.ab.Abe0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101

import java.time.LocalDate;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abf01;
import sam.model.entities.da.Daa01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.scf.service.SCFService;

class SCF_Remessa_Banco_Itau_CNAB400 extends FormulaBase{
    public final static String PATTERN_DDMMYY = "ddMMyy";
    public final static String PATTERN_DDMMYYYY = "ddMMyyyy";

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_REMESSA_DE_COBRANCA;
    }

    @Override
    public void executar() {
        //**************************Fórmula gerada no dia 17/11/2020 a partir do Leiaute CNAB 400 padrão Itau - Janeiro/2017 ******************************
        //**************************Revisada em 02/03/2023, a partir do Leiaute Cobrança CNAB 400 Empresas Itaú - Julho/2022 ******************************
        TextFile txt = new TextFile();
        Integer numRemessa = get("numRemessa");
        LocalDate dataRemessa = get("dataRemessa");
        Integer movimento = get("movimento");
        Aac10 aac10 = get("aac10");
        Abf01 abf01 = get("abf01");
        List<Daa01> daa01s = get("daa01s");
        SCFService scfService = instanciarService(SCFService.class);
        def total = 0;
        for (Daa01 daa01 : daa01s){
            total = total + daa01.daa01valor;
        }

        /**
         * HEADER
         */
        txt.print("0");                                                      												//001-001
        txt.print("1");                                                               										//002-002
        txt.print("REMESSA");                                                         										//003-009
        txt.print("01");                                                              										//010-011
        txt.print("COBRANCA", 15);                                                    										//012-026
        txt.print(abf01.abf01agencia, 4, (char) '0', true);															//027-030
        txt.print("00");                                                              										//031-032
        txt.print(abf01.abf01conta, 5, (char) '0', true); 															//033-037
        txt.print(abf01.abf01digConta, 1);																			//038-038
        txt.print(StringUtils.space(8));		                                                  							//039-046
        txt.print(aac10.aac10rs, 30, true, true);								  									//047-076
        txt.print("341");                                                             										//077-079
        txt.print("BANCO ITAU SA", 15);                                                										//080-094
        txt.print(LocalDate.now().format(PATTERN_DDMMYY));                                            							//095-100
        txt.print(StringUtils.space(294));											  								//101-394
        txt.print("000001");           												  							//395-400
        txt.newLine();


        /**
         * DETALHE
         */
        int contador = 1;
        for(Daa01 daa01 : daa01s) {
            TableMap daa01json = daa01.daa01json == null ? new TableMap() : daa01.daa01json;
            txt.print("1");																					//001-001
            txt.print(aac10.aac10ti == 0 ? "02" : "01");													 			//002-003
            txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, (char) '0', true);					 				//004-017
            txt.print(abf01.abf01agencia, 4, (char) '0', true);													 	//018-021
            txt.print("00")																					//022-023
            txt.print(abf01.abf01conta, 5, (char) '0', true);											 				//024-028
            txt.print(abf01.abf01digConta, 1); 																	//029-029
            txt.print(StringUtils.space(4));																 		//030-033
            txt.print("0000")																					//034-037
            txt.print(daa01.daa01id + ";" + movimento, 25);										 					//038-062

            txt.print(daa01.daa01nossoNum, 8, (char) '0', true);											 			//063-069
            //txt.print(daa01.daa01nossoNumDV, 1, (char) '0', true);											 			//070-070

            txt.print(0,13);																		 			//071-083
            txt.print(abf01.abf01json.get("carteira"), 3, (char) '0', true);												//084-086
            txt.print(StringUtils.space(21));																 		//087-107
            txt.print(abf01.abf01json.get("cod_carteira"), 1, (char) '0', true);											//108-108
            txt.print("01");																					//109-110
            txt.print(StringUtils.ajustString(seuNumero(daa01.daa01central.abb01num, daa01.daa01central.abb01parcela), 10));		//111-120
            txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYY));													 	//121-126
            txt.print(daa01.daa01valor.multiply(100).intValue(), 13);										 			//127-139
            txt.print("341");																				 	//140-142
            txt.print("00000");																				 	//143-147
            txt.print(conteudoDinamicoParametro(daa01.daa01central.abb01tipo.aah01codigo), 2); 								//148-149
            txt.print("A")	 													 										//150-150
            txt.print(daa01.daa01central.abb01data.format(PATTERN_DDMMYY));												//151-156
            txt.print(abf01.abf01json.get("primeira_instrucao"), 2, true);												//157-158
            txt.print(abf01.abf01json.get("segunda_instrucao"), 2, true);											 	//159-160
            txt.print(daa01json.getBigDecimal("juros") == null ? "0000000000000" : daa01json.getBigDecimal("juros").multiply(100).intValue(), 13); //161-173
            txt.print(daa01json.getDate("dt_limite_desc") == null ? "000000" : daa01json.getDate("dt_lim_desc").format(PATTERN_DDMMYY)); //174-179
            txt.print(daa01json.getBigDecimal("desconto") == null ? "0000000000000" : daa01json.getBigDecimal("desconto").multiply(100).abs().intValue(), 13); //180-192
            txt.print(0, 13);																				 	//193-205
            txt.print(0, 13);																				 	//206-218

            Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);
            txt.print(abe01.abe01ti == 0 ? "02" : "01");                                                  						//219-220
            txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, (char) '0', true);            				 			//221-234
            txt.print(abe01.abe01nome, 30, true, true);                            						 				//235-264
            txt.print(StringUtils.space(10));																 		//265-274

            TableMap tm = buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(daa01.daa01central?.abb01id);

            if(tm != null && tm.get("endereco") != null) {
                txt.print(tm.get("numero") == null ? tm.get("endereco") : tm.get("endereco") + "," + tm.get("numero"), 40, true, true); //275-314
                txt.print(tm.get("bairro"), 12, true, true);															//315-326
                txt.print(tm.get("cep") == null ? null : tm.get("cep"), 8, (char) '0', true); 								//327-334
                txt.print(tm.get("municipio") == null ? null : tm.get("municipio"), 15, true, true); 						//335-349
                txt.print(tm.get("uf") == null ? null : tm.get("uf"), 2, true, true); 									//350-351
            }
            txt.print(StringUtils.space(30));																 		//352-381
            txt.print(StringUtils.space(4));																 		//382-385
            txt.print("000000");																				//386-391
            txt.print(abf01.abf01json.get("dias_protesto"), 2, (char) '0', true);									//392-393
            txt.print(StringUtils.space(1));																 		//394-394
            txt.print(++contador, 6);																		 	//395-400


            txt.newLine();

            /**
             * MENSAGENS ADICIONAIS
             */

            /*txt.print("2");																				//001-001
            txt.print("1");																				//002-002
            txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYYYY));											 	//003-010
            txt.print(daa01.daa01valor.multiply(10).intValue(), 13);												//011-023
            txt.print(StringUtils.space(371));																	//024-394
            txt.print(++contador, 6);																		//395-400
            txt.newLine();*/
        }

        /**
         * TRAILLER
         */
        txt.print("9");																						//001-001
        txt.print(StringUtils.space(393));																			//002-394
        txt.print(++contador, 6);																				//395-400
        txt.newLine();

        put("txt", txt);
    }

    private String seuNumero(Integer num, String parcela) {
        String seuNumero = null;
        if(parcela == null || parcela.equals("0")) {
            seuNumero = "" + num;
        }else {
            seuNumero = num + " " + parcela;
        }

        return seuNumero;
    }

    private String conteudoDinamicoParametro(String aah01codigo) {
        switch(aah01codigo) {
            case "08001":
                return "08";
            case "62001":
                return "05";
            default:
                return "01";
        }
    }

    /**
     * Buscar o endereço de cobrança a partir da central do documento financeiro
     * @param abb01id Long Id da central do documento financeiro
     * @return TableMap
     */
    public TableMap buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(Long abb01id) {
        TableMap tm = new TableMap();
        if (abb01id == null) return null;
        Abb01 abb01 = getSession().get(Abb01.class, "abb01id, abb01ent", abb01id);
        if (abb01.getAbb01ent() != null) {
            Abe0101 abe0101 = getSession().createCriteria(Abe0101.class)
                    .addFields("abe0101id, abe0101endereco, abe0101numero, abe0101cep, abe0101bairro, abe0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
                    .addJoin(Joins.fetch("abe0101municipio").left(true).partial(true).alias("aag0201"))
                    .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
                    .addWhere(Criterions.eq("abe0101ent", abb01.getAbb01ent().getAbe01id()))
                    .addWhere(Criterions.eq("abe0101cobranca", Abe0101.SIM))
                    .setOrder("abe0101id ASC").setMaxResults(1).get(ColumnType.ENTITY);
            if (abe0101 != null) {
                tm.put("endereco", abe0101.getAbe0101endereco());
                tm.put("numero", abe0101.getAbe0101numero());
                tm.put("cep", abe0101.getAbe0101cep());
                tm.put("bairro", abe0101.getAbe0101bairro());
                tm.put("municipio", abe0101.getAbe0101municipio() != null ? abe0101.getAbe0101municipio().getAag0201nome() : "");
                tm.put("uf", abe0101.getAbe0101municipio() != null  && abe0101.getAbe0101municipio().getAag0201uf() != null ? abe0101.getAbe0101municipio().getAag0201uf().getAag02uf() : "");
            } else {
                abe0101 = getSession().createCriteria(Abe0101.class)
                        .addFields("abe0101id, abe0101endereco, abe0101numero, abe0101cep, abe0101bairro, abe0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
                        .addJoin(Joins.fetch("abe0101municipio").left(true).partial(true).alias("aag0201"))
                        .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
                        .addWhere(Criterions.eq("abe0101ent", abb01.getAbb01ent().getAbe01id()))
                        .addWhere(Criterions.eq("abe0101principal", Abe0101.SIM))
                        .setOrder("abe0101id ASC").setMaxResults(1).get(ColumnType.ENTITY);
                if (abe0101 != null) {
                    tm.put("endereco", abe0101.getAbe0101endereco());
                    tm.put("numero", abe0101.getAbe0101numero());
                    tm.put("cep", abe0101.getAbe0101cep());
                    tm.put("bairro", abe0101.getAbe0101bairro());
                    tm.put("municipio", abe0101.getAbe0101municipio() != null ? abe0101.getAbe0101municipio().getAag0201nome() : "");
                    tm.put("uf", abe0101.getAbe0101municipio() != null  && abe0101.getAbe0101municipio().getAag0201uf() != null ? abe0101.getAbe0101municipio().getAag0201uf().getAag02uf() : "");
                }
            }
        }

        Long eaa01idCentral = getSession().createCriteria(Abb0102.class).addFields("abb0102central")
                .addWhere(Criterions.eq("abb0102doc", abb01id)).setMaxResults(1).get(ColumnType.LONG);
        if (eaa01idCentral == null) return tm;

        Long eaa01id = getSession().createCriteria(Eaa01.class).addFields("eaa01id")
                .addWhere(Criterions.eq("eaa01central", eaa01idCentral)).addWhere(getSamWhere().getCritPadrao(Eaa01.class))
                .get(ColumnType.LONG);
        if (eaa01id == null) return tm;

        Eaa0101 eaa0101 = getSession().createCriteria(Eaa0101.class)
                .addFields("eaa0101id, eaa0101endereco, eaa0101numero, eaa0101cep, eaa0101bairro, eaa0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
                .addJoin(Joins.fetch("eaa0101municipio").left(true).partial(true).alias("aag0201"))
                .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02")).addWhere(Criterions.eq("eaa0101doc", eaa01id))
                .addWhere(Criterions.eq("eaa0101cobranca", Eaa0101.SIM)).setOrder("eaa0101id ASC").setMaxResults(1)
                .get(ColumnType.ENTITY);
        if (eaa0101 != null) {
            tm.put("endereco", eaa0101.getEaa0101endereco());
            tm.put("numero", eaa0101.getEaa0101numero());
            tm.put("cep", eaa0101.getEaa0101cep());
            tm.put("bairro", eaa0101.getEaa0101bairro());
            tm.put("municipio", eaa0101.getEaa0101municipio() != null ? eaa0101.getEaa0101municipio().getAag0201nome() : "");
            tm.put("uf", eaa0101.getEaa0101municipio() != null  && eaa0101.getEaa0101municipio().getAag0201uf() != null ? eaa0101.getEaa0101municipio().getAag0201uf().getAag02uf() : "");
        } else {
            eaa0101 = getSession().createCriteria(Eaa0101.class)
                    .addFields("eaa0101id, eaa0101endereco, eaa0101numero, eaa0101cep, eaa0101bairro, eaa0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
                    .addJoin(Joins.fetch("eaa0101municipio").left(true).partial(true).alias("aag0201"))
                    .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
                    .addWhere(Criterions.eq("eaa0101doc", eaa01id))
                    .addWhere(Criterions.eq("eaa0101principal", Eaa0101.SIM)).setOrder("eaa0101id ASC").setMaxResults(1)
                    .get(ColumnType.ENTITY);
            if (eaa0101 == null) return tm;
            tm.put("endereco", eaa0101.getEaa0101endereco());
            tm.put("numero", eaa0101.getEaa0101numero());
            tm.put("cep", eaa0101.getEaa0101cep());
            tm.put("bairro", eaa0101.getEaa0101bairro());
            tm.put("municipio", eaa0101.getEaa0101municipio() != null ? eaa0101.getEaa0101municipio().getAag0201nome() : "");
            tm.put("uf", eaa0101.getEaa0101municipio() != null  && eaa0101.getEaa0101municipio().getAag0201uf() != null ? eaa0101.getEaa0101municipio().getAag0201uf().getAag02uf() : "");
        }
        return tm;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDIifQ==