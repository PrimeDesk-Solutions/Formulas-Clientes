package multitec.formulas.scf.cob.rem;

import java.time.LocalDate;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb0102
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abf01;
import sam.model.entities.da.Daa01;
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.server.samdev.formula.FormulaBase;
import sam.server.scf.service.SCFService;

class Layout_012_Bradesco_240 extends FormulaBase{
	public final static String PATTERN_DDMMYY = "ddMMyy";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_REMESSA_DE_COBRANCA;
	}
	
	@Override
	public void executar() {
		//**************************Fórmula gerada no dia 27/03/2020******************************
		TextFile txt = new TextFile();
		Integer numRemessa = get("numRemessa");
		LocalDate dataRemessa = get("dataRemessa");
		Integer movimento = get("movimento");
		Aac10 aac10 = get("aac10");
		Abf01 abf01 = get("abf01");
		List<Daa01> daa01s = get("daa01s");
		SCFService scfService = instanciarService(SCFService.class);
		
		int totalLinhas = 0;
		
		selecionarAlinhamento("0001");
		
		/**
		 * HEADER do arquivo
		 */
		txt.print("001"); 																										//001-003
		txt.print("0000");  																									//004-007
		txt.print("0"); 																										//008-008
		txt.print(StringUtils.space(9)); 																						//009-017
		txt.print(aac10.aac10ti == 0 ? "2" : "1");  																			//018-018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);												 	//019-032
		txt.print(abf01.abf01json.get(getCampo("0","num_conv_cobr")), 9, '0', true);											//033-041
		txt.print(abf01.abf01json.get(getCampo("0","cobr_cedente")), 4, '0', true);												//042-045
		txt.print(abf01.abf01json.get(getCampo("0","num_carteira")), 2, '0', true); 											//046-047
		txt.print(abf01.abf01json.get(getCampo("0","variacao_carteira")), 3, '0', true); 										//048-050
		txt.print(StringUtils.space(2));		 																				//051-052
		txt.print(abf01.abf01agencia, 5, '0', true); 																			//053-057
		txt.print(abf01.abf01digAgencia, 1); 																					//058-058
		txt.print(abf01.abf01conta, 12, '0', true); 																			//059-070
		txt.print(abf01.abf01digConta, 1); 																						//071-071
		txt.print(StringUtils.space(1)); 																						//072-072
		txt.print(aac10.aac10rs, 30, true, true);									 											//073-102
		txt.print("BRADESCO", 30); 																								//103-132
		txt.print(StringUtils.space(10)); 																						//133-142
		txt.print("1"); 																										//143-143
		txt.print(MDate.date().format(PATTERN_DDMMYY)); 																		//144-151
		txt.print(0, 6);				 																						//152-157
		txt.print("000001"); 																									//158-163
		txt.print("083"); 																										//164-166
		txt.print(0, 5);				 																						//167-171
		txt.print(StringUtils.space(69)); 																						//172-240
		txt.newLine();
		totalLinhas++;
		
		
		/**
		 * HEADER do lote
		 */
		txt.print("001"); 																										//001-003
		txt.print(1, 4); 																										//004-007
		txt.print("1"); 																										//008-008
		txt.print("R"); 																										//009-009
		txt.print("01");  																										//010-011
		txt.print("00"); 																										//012-013
		txt.print("042"); 																										//014-016
		txt.print(StringUtils.space(1)); 																						//017-017
		txt.print(aac10.aac10ti == 0 ? "2" : "1"); 																				//018-018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 15, '0', true);													//019-033
		txt.print(abf01.abf01json.get(getCampo("1","num_conv_cobr")), 9, '0', true);											//034-042
		txt.print(abf01.abf01json.get(getCampo("1","cobr_cedente")), 4, '0', true);												//043-046
		txt.print(abf01.abf01json.get(getCampo("1","num_carteira")), 2, '0', true); 											//047-048
		txt.print(abf01.abf01json.get(getCampo("1","variacao_carteira")), 3, '0', true); 										//049-051
		txt.print(StringUtils.space(2));																						//052-053
		txt.print(abf01.abf01agencia, 5, '0', true); 																			//054-058
		txt.print(abf01.abf01digAgencia, 1); 																					//059-059
		txt.print(abf01.abf01conta, 12, '0', true); 																			//060-071
		txt.print(abf01.abf01digConta, 1);			 																			//072-072
		txt.print(StringUtils.space(1));  																						//073-073
		txt.print(aac10.aac10rs, 30, true, true);									 											//074-103
		txt.print(StringUtils.space(40)); 																						//104-143
		txt.print(StringUtils.space(40));  																						//144-183
		txt.print(numRemessa, 8); 																								//184-191
		txt.print(MDate.date().format(PATTERN_DDMMYY)); 																		//192-199
		txt.print(0, 8); 																										//200-207
		txt.print(StringUtils.space(33)); 																						//208-240
		txt.newLine();
		totalLinhas++;
		
		/**
		 * DETALHE - Segmento P - 3
		 */
		int totalReg = 0;
		StringBuilder textoDetalhe = new StringBuilder();
		for(Daa01 daa01 : daa01s) {
			txt.print("001"); 																									//001-003
			txt.print(1, 4); 																									//004-007
			txt.print("3"); 																									//008-008
			txt.print(++totalReg, 5); 																							//009-013
			txt.print("P"); 																									//014-014
			txt.print(StringUtils.space(1)); 																					//015-015
			txt.print(abf01.abf01json.get(getCampo("3","cod_mov_remessa")), 2, '0', true); 										//016-017
			txt.print(abf01.abf01agencia, 5, '0', true); 																		//018-022
			txt.print(abf01.abf01digAgencia, 1); 																				//023-023
			txt.print(abf01.abf01conta, 12, '0', true);  																		//024-035
			txt.print(abf01.abf01digConta, 1); 																					//036-036
			txt.print(StringUtils.space(1)); 																					//037-037
			txt.print(daa01.daa01nossoNum, 20);																					//038-057
			txt.print(abf01.abf01json.get(getCampo("3","cod_carteira")), 1, '0', true);  										//058-058
			txt.print(abf01.abf01json.get(getCampo("3","forma_cadastro_bb")), 1, '0', true);  									//059-059
			txt.print(abf01.abf01json.get(getCampo("3","tipo_documento_bb")), 1); 												//060-060
			txt.print(abf01.abf01json.get(getCampo("3","ident_emissao_bloqueto")), 1, '0', true);								//061-061
			txt.print(abf01.abf01json.get(getCampo("3","ident_distrib")), 1); 													//062-062
			txt.print(seuNumero(daa01.daa01central.abb01num, daa01.daa01central.abb01parcela), 15); 							//063-077
			txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYY)); 																//078-085
			txt.print(daa01.daa01valor.multiply(100).intValue(), 15); 															//086-100 
			txt.print(0, 5); 																									//101-105
			txt.print(StringUtils.space(1)); 																					//106-106
			txt.print(conteudoDinamicoParametro(daa01.daa01central.abb01tipo.aah01codigo), 2, '0', true); 						//107-108
			txt.print(abf01.abf01json.get(getCampo("3","aceito")), 1);  														//109-109
			txt.print(daa01.daa01central.abb01data.format(PATTERN_DDMMYY)); 													//110-117
			txt.print(daa01.daa01json.getBigDecimal(getCampo("3","juros")).equals(0) ? "3" : "1"); 								//118-118
			txt.print(daa01.daa01json.getBigDecimal(getCampo("3","juros")).equals(0) ? "00000000" : daa01.daa01dtVctoN.format(PATTERN_DDMMYY)); //119-126
			txt.print(daa01.daa01json.getBigDecimal(getCampo("3","juros")).multiply(100).intValue(), 15); 						//127-141
			txt.print(daa01.daa01json.getBigDecimal(getCampo("3","desconto")).equals(0) ? "0" : "1"); 							//142-142
			txt.print(daa01.daa01json.getDate(getCampo("3","dt_lim_desc")) == null ? "00000000" : daa01.daa01json.getDate(getCampo("3","dt_lim_desc")).format(PATTERN_DDMMYY)); //143-150
			txt.print(daa01.daa01json.getBigDecimal(getCampo("3","desconto")).multiply(100).abs().intValue(), 15); 				//151-165
			txt.print(0, 15); 																									//166-180
			txt.print(0, 15); 																									//181-195
			txt.print(daa01.daa01id + ";" + movimento, 25);																		//196-220
			txt.print(abf01.abf01json.get(getCampo("3","cod_protesto")), 1, '0', true); 										//221-221
			txt.print(abf01.abf01json.get(getCampo("3","num_dias_protesto")), 2, '0', true);									//222-223			
			txt.print(abf01.abf01json.get(getCampo("3","cod_baixa")), 1, '0', true);											//224-224
			txt.print(0, 3); 																									//225-227
			txt.print(abf01.abf01json.get(getCampo("3","cod_moeda")), 2, '0', true); 											//228-229
			txt.print(0, 10);				 																					//230-239
			txt.print(StringUtils.space(1)); 																					//240-240
			
			textoDetalhe.append(txt.getLastLine());
			txt.newLine();
			totalLinhas++;
			
			/**
			 * DETALHE - SEGMENTO Q - 4
			 */
			txt.print("001"); 																									//001-003
			txt.print(1, 4);  																									//004-007
			txt.print("3");  																									//008-008
			txt.print(++totalReg, 5);																							//009-013
			txt.print("Q"); 																									//014-014
			txt.print(StringUtils.space(1)); 																					//015-015
			txt.print(abf01.abf01json.get(getCampo("4","cod_mov_remessa")), 2, '0', true);  									//016-017
			
			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);
			
	        txt.print(abe01.abe01ti == 0 ? "2" : "1"); 																			//018-018
	        txt.print(StringUtils.extractNumbers(abe01.abe01ni), 15, '0', true); 												//019-033
	        txt.print(abe01.abe01nome, 40, true, true); 																		//034-073
	        
	       TableMap tm = buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(daa01.daa01central.abb01id);
			if(tm != null && tm.get("endereco") != null) {
				txt.print(tm.get("numero") == null ? tm.get("endereco") : tm.get("endereco") + "," + tm.get("numero"), 40, true, true);	//074-113
				txt.print(tm.get("bairro"), 15, true, true); 																	//114-128
				txt.print(tm.get("cep") == null ? null : tm.get("cep"), 8, '0', true);     										//129-136
				txt.print(tm.get("municipio") == null ? null : tm.get("municipio"), 15, true, true); 							//137-151
				txt.print(tm.get("uf") == null ? null : tm.get("uf"), 2, true, true); 											//152-153
			}
			
	        txt.print(abf01.abf01json.get(getCampo("4","tipo_incri")), 1, '0', true); 											//154-154
	        txt.print(abf01.abf01json.get(getCampo("4","num_inscri")), 15, '0', true);											//155-169
	        txt.print(abf01.abf01json.get(getCampo("4","nome_sacador")), 40); 													//170-209
	        txt.print(0, 3); 																									//210-212
	        txt.print(StringUtils.space(20));																					//213-232
	        txt.print(StringUtils.space(8)); 																					//233-240
	        
	        textoDetalhe.append(txt.getLastLine());
	        txt.newLine();
	        totalLinhas++;

//			/**
//			 * DETALHE - SEGMENTO J-52
//			 */
//			txt.print("001"); 																									//001-003
//			txt.print(1, 4);																									//004-007
//			txt.print("3");																										//008-008
//			txt.print(++totalReg, 5);																							//009-013
//			txt.print("J");																										//014-014
//			txt.print(StringUtils.space(1));																					//015-015
//			txt.print(abf01.abf01json.get("316"), 2, '0', true);																//016-017
//			txt.print("52");																									//018-019
//			//Pagador
//			txt.print(aa80.getAa80ti() == 0 ? "2" : "1");																		//020-020
//			txt.print(StringUtils.filtrarNumeros(aa80.getAa80ni()), 15, '0', true);												//021-035
//			txt.print(aa80.getAa80nome(), 40, true, true);																		//036-075
//			//Beneficiário
//			txt.print(aa65.getAa65ti() == 0 ? "2" : "1");																		//076-076
//			txt.print(StringUtils.filtrarNumeros(aa65.getAa65ni()), 15, '0', true);												//077-091
//			txt.print(aa65.getAa65rs(), 40, true, true);																		//092-131
//			//Pagador responsável	
//			txt.print(aa80.getAa80ti() == 0 ? "2" : "1");																		//132-132
//			txt.print(StringUtils.filtrarNumeros(aa80.getAa80ni()), 15, '0', true);												//133-147
//			txt.print(aa80.getAa80nome(), 40, true, true);																		//148-187
//			txt.print(StringUtils.space(53));																					//188-240
//			txt.newLine();	
		}
		
		/**
		 * TRAILLER DE LOTE
		 */
		txt.print("001");																										//001-003
		txt.print(1, 4);																										//004-007
		txt.print("5");																											//008-008
		txt.print(StringUtils.space(9));																						//009-017
		txt.print(totalLinhas, 6);																								//018-023
		txt.print(0, 92);																										//024-115
		txt.print(StringUtils.space(125));																						//116-240
		txt.newLine();
		totalLinhas++;
        
		/**
		 * TRAILLER DE ARQUIVO
		 */
		txt.print("001"); 																										//001-003
		txt.print("9999"); 																										//004-007
		txt.print("9"); 																										//008-008
		txt.print(StringUtils.space(9)); 																						//009-017
		txt.print(1, 6); 																										//018-023
		txt.print(totalLinhas+1, 6);																							//024-029
		txt.print(StringUtils.space(6)); 																						//030-035
		txt.print(StringUtils.space(205)); 																						//036-240
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