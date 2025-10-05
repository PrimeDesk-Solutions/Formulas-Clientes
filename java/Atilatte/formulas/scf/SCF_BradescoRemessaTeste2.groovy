package Atilatte.formulas.scf;

import java.time.LocalDate;
import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Join;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abf01;
import sam.model.entities.da.Daa01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.scf.service.SCFService;

class SCF_BradescoRemessa extends FormulaBase{
	public final static String PATTERN_DDMMYY = "ddMMyy";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_REMESSA_DE_COBRANCA;
	}
	
	@Override
	public void executar() {
		//**************************Fórmula gerada no dia 25/03/2020 a partir do Manual de Procedimentos Nº 4008.524.0121 Versão 14 Bradesco ******************************
		TextFile txt = new TextFile();
		Integer numRemessa = get("numRemessa");
		LocalDate dataRemessa = get("dataRemessa");
		Integer movimento = get("movimento");
		Aac10 aac10 = get("aac10");
		Abf01 abf01 = get("abf01");
		List<Daa01> daa01s = get("daa01s");
		SCFService scfService = instanciarService(SCFService.class);
		
		selecionarAlinhamento("0001");
		
		/**
		 * HEADER
		 */
		txt.print("0");                                                      												    //001-001
		txt.print("1");                                                               											//002-002
		txt.print("REMESSA");                                                         											//003-009
		txt.print("01");                                                              											//010-011
		txt.print("COBRANCA", 15);                                                    											//012-026
//		txt.print(abf01.abf01json.get(getCampo("0","cod_emp")), 20, (char) '0', true);                       	  				//027-046
		txt.print(abf01.abf01json.getBigDecimal_Zero("cod_emp"), 20, (char) '0', true);                       	  				//027-046
		txt.print(aac10.aac10rs, 30, true, true);								  											    //047-076
		txt.print("237");                                                             											//077-079
		txt.print("BRADESCO", 15);                                                    											//080-094
		txt.print(MDate.date().format(PATTERN_DDMMYY));                                            							//095-100
		txt.print(StringUtils.space(8));                                              											//101-108
		txt.print("MX");            												  											//109-110
		txt.print(numRemessa, 7);													  											//111-117
		txt.print(StringUtils.space(277));											  											//118-394
		txt.print("000001");           												  											//395-400
		txt.newLine();
		
		/**
		 * DETALHE
		 */
		int contador = 1;
		for(Daa01 daa01 : daa01s) {
			TableMap jsonDaa01 = daa01.daa01json;
			if(jsonDaa01 == null) jsonDaa01 = new TableMap();
			
			txt.print("1");																					 					//001-001
			txt.print("00000");																				 					//002-006
			txt.print(StringUtils.space(1));																 					//007-007
			txt.print("00000");																				 					//008-012
			txt.print("0000000");																		     					//013-019
			txt.print(StringUtils.space(1));																 					//020-020
			txt.print("0"); 																				 					//021-021
			txt.print(abf01.abf01json.getBigDecimal_Zero("carteira"), 3, (char) '0', true);                                      //022-024
			txt.print(abf01.abf01json.get(getCampo("1","agencia")) == null ? abf01.abf01agencia : abf01.abf01json.get(getCampo("1","agencia")), 5, (char) '0', true); //025-029
			txt.print(abf01.abf01json.get(getCampo("1","conta_corrente")) == null ? abf01.abf01conta : abf01.abf01json.get(getCampo("1","conta_corrente")), 7, (char) '0', true); //030-036
			txt.print(abf01.abf01json.get(getCampo("1","dig_conta_corrente")) == null ? abf01.abf01digConta : abf01.abf01json.get(getCampo("1","dig_conta_corrente")), 1); //037-037
			txt.print(daa01.daa01id + ";" + movimento, 25);												 						//038-062
			txt.print("000");																				 					//063-065
			
			def multa = jsonDaa01.getBigDecimal(getCampo("1","multa"));
			def semMulta = abf01.abf01json.get(getCampo("1","multa")) == null || multa.equals(0);
			txt.print(semMulta ? "0" : "2");																					//066-066
			txt.print(semMulta ? "0000" : multa.multiply(100).intValue(), 4, (char) '0', true); 								//067-070
			
			txt.print(daa01.daa01nossoNum, 11, (char) '0', true);																//071-081
			txt.print(daa01.daa01nossoNumDV, 1, (char) '0', true);	                                                                   //082-082
			
			txt.print(abf01.abf01json.getBigDecimal_Zero("valor_desconto_bonif"), 10, (char) '0', true);							//083-092
			txt.print(abf01.abf01json.getBigDecimal_Zero("emiss_papeleta"), 1);													//093-093
			txt.print(abf01.abf01json.getBigDecimal_Zero("emite_papeleta"), 1);													//094-094
			txt.print(StringUtils.space(10));																 					//095-104
			txt.print(StringUtils.space(1));																 					//105-105
			txt.print("2");																					 					//106-106
			txt.print(StringUtils.space(2));																 					//107-108
			txt.print(abf01.abf01json.getBigDecimal_Zero("ident_ocorrencia"), 2);												//109-110
			txt.print(StringUtils.ajustString(seuNumero(daa01.daa01central.abb01num, daa01.daa01central.abb01parcela), 10));	//111-120
			txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYY));													 			//121-126
			txt.print(daa01.daa01valor.multiply(100).intValue(), 13);										 					//127-139
			txt.print("000");																				 					//140-142
			txt.print("00000");																				 					//143-147
			txt.print(conteudoDinamicoParametro(daa01.daa01central.abb01tipo.aah01codigo), 2); 									//148-149
			txt.print(abf01.abf01json.getString("aceito"), 1);	 													 	//150-150
			txt.print(daa01.daa01central.abb01data.format(PATTERN_DDMMYY));													 	//151-156
			txt.print(abf01.abf01json.getBigDecimal_Zero("primeira_instrucao"), 2, (char) '0', true);						    //157-158
			txt.print(abf01.abf01json.getBigDecimal_Zero("segunda_instrucao"), 2, (char) '0', true);								//159-160
			txt.print(jsonDaa01.getBigDecimal(getCampo("1","juros")) == null ? "0000000000000" : jsonDaa01.getBigDecimal(getCampo("1","juros")).multiply(100).intValue(), 13); //161-173
			txt.print(jsonDaa01.getDate(getCampo("1","dt_lim_desc")) == null ? "000000" : jsonDaa01.getDate(getCampo("1","dt_lim_desc")).format(PATTERN_DDMMYY)); //174-179
			txt.print(jsonDaa01.getBigDecimal(getCampo("1","desconto")) == null ? "0000000000000" : jsonDaa01.getBigDecimal(getCampo("1","desconto")).multiply(100).abs().intValue(), 13); //180-192
			txt.print(0, 13);																				 					//193-205
			txt.print(0, 13);																				 					//206-218
			
			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);
			txt.print(abe01.abe01ti == 0 ? "02" : "01");                                                  						//219-220
			txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, (char) '0', true);            				 				//221-234
			txt.print(abe01.abe01nome, 40, true, true);                            						 						//235-274
			
			TableMap tm = getAcessoAoBanco().buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(daa01.daa01central.abb01id);
			if(tm != null && tm.get("endereco") != null) {
				txt.print(tm.get("numero") == null ? tm.get("endereco") : tm.get("endereco") + "," + tm.get("numero"), 40, true, true); //275-314
				txt.print(abf01.abf01json.get(getCampo("1","primeira_mensagem")), 12, true, true);								//315-326
				txt.print(tm.get("cep") == null ? null : tm.get("cep"), 8, (char) '0', true); 									//327-334
			}
			
			txt.print(abf01.abf01json.get(getCampo("1","segunda_mensagem")), 60, true, true);  									//335-394
			txt.print(++contador, 6);																		 					//395-400
			txt.newLine();

			/**
			 * MENSAGENS ADICIONAIS
			 */
			 if((abf01.abf01json.get(getCampo("2","msg_opc1")) != null && abf01.abf01json.getString(getCampo("2","msg_opc1")).length() > 0) ||
				(abf01.abf01json.get(getCampo("2","msg_opc2")) != null && abf01.abf01json.getString(getCampo("2","msg_opc2")).length() > 0) ||
				(abf01.abf01json.get(getCampo("2","msg_opc3")) != null && abf01.abf01json.getString(getCampo("2","msg_opc3")).length() > 0) ||
				(abf01.abf01json.get(getCampo("2","msg_opc4")) != null && abf01.abf01json.getString(getCampo("2","msg_opc4")).length() > 0)){
				 
				txt.print("2");																					 				//001-001
				txt.print(abf01.abf01json.get(getCampo("2","msg_opc1")), 80, true, true);										//002-081
				txt.print(abf01.abf01json.get(getCampo("2","msg_opc2")), 80, true, true);										//082-161
				txt.print(abf01.abf01json.get(getCampo("2","msg_opc3")), 80, true, true);										//162-241
				txt.print(abf01.abf01json.get(getCampo("2","msg_opc4")), 80, true, true);										//242-321
				txt.print(StringUtils.space(45));																 				//322-366
				txt.print(abf01.abf01json.get(getCampo("2","carteira")), 3, (char) '0', true);  								//367-369
				txt.print(abf01.abf01agencia, 5, (char) '0', true);												 				//370-374
				txt.print(abf01.abf01conta, 7, (char) '0', true);												 				//375-381
				txt.print(abf01.abf01digConta, 1);																 				//382-382
				txt.print(daa01.daa01nossoNum, 12, (char) '0', true);											 				//383-394
				txt.print(++contador, 6);																		 				//395-400
				txt.newLine();
			 }
		}

		/**
		 * TRAILLER
		 */
		txt.print("9");																							 				//001-001
		txt.print(StringUtils.space(393));																		 				//002-394
		txt.print(++contador, 6);																				 				//395-400
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
			case "2002":
				return "01";
			case "2003":
				return "02";
			case "0001":
				return "05";
			default:
				return "99";
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDIifQ==