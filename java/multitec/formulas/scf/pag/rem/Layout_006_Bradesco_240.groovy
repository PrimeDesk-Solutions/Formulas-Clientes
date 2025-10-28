package multitec.formulas.scf.pag.rem

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

class Layout_006_Bradesco_240 extends FormulaBase{
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
		TableMap cpoLivBco = get("cpoLivBco");
		SCFService scfService = instanciarService(SCFService.class);

		def contador = 0;
		def numLote = 0;
		def qtDetalheLote = 0;
		def totalDocsLote = 0;
		
		selecionarAlinhamento("0002");

		/**
		 * HEADER do arquivo
		 */
		txt.print("237");																											//001-003
		txt.print(0, 4);																											//004-007
		txt.print("0"); 																											//008-008
		txt.print(StringUtils.space(9));																							//009-017
		txt.print(aac10.aac10ti == 0 ? "2" : "1");	  																				//018-018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);																	//019-032
		txt.print(abf01.abf01json.getString(getCampo("0","cod_convenio")), 20);														//033-052
		txt.print(abf01.abf01agencia, 5, '0', true);																				//053-057
		txt.print(abf01.abf01digAgencia, 1, '0', true);																				//058-058
		txt.print(abf01.abf01conta, 12, '0', true);																					//059-070
		txt.print(abf01.abf01digConta, 1, '0', true);																				//071-071
		txt.print(abf01.abf01digConta, 1, '0', true);																				//072-072
		txt.print(aac10.aac10rs, 30, true, true);																					//073-102
		txt.print("BANCO BRADESCO", 30);																							//103-132
		txt.print(StringUtils.space(10));																							//133-142
		txt.print("1");																												//143-143
		txt.print(MDate.date().format(PATTERN_DDMMYYYY));																			//144-151
		txt.print(MDate.time().format(PATTERN_HHMMSS));																				//152-157
		txt.print(0, 6);																											//158-163
		txt.print("089");																											//164-166
		txt.print(0, 5);																											//167-171
		txt.print(StringUtils.space(20));																							//172-191
		txt.print(StringUtils.space(20));																							//192-211
		txt.print(StringUtils.space(29));																							//212-240
		txt.newLine();
		contador++;
		
		/**
		 * LOTE
		 */
		def fpAnterior = ""; //Lotes por forma de pagamento

		for(Daa01 daa01 : daa01s){
			Daa0102 daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movimento);
			if(!fpAnterior.equals(cpoLivBco.get("formapgto"))) {	//Novo lote
				if(qtDetalheLote > 0) { //Se a quantidade de detalhes do lote for maior que zero significa que o lote anterior precisa ser fechado
					trailerLoteBradesco(txt, numLote, qtDetalheLote, totalDocsLote);
					contador++;
					qtDetalheLote = 0;
				}
				fpAnterior = cpoLivBco.get("formapgto");
				totalDocsLote = 0;

				/**
				 * HEADER DE LOTE
				 */
				if(cpoLivBco.get("formapgto") == "30" || cpoLivBco.get("formapgto") == "31"){
					txt.print("237");																								//001-003
					txt.print(++numLote, 4);																						//004-007
					txt.print("1");																									//008-008
					txt.print("C");																									//009-009
					txt.print("20");																								//010-011
					txt.print(cpoLivBco.get("formapgto"), 2);																		//012-013
					txt.print("040");																								//014-016
					txt.print(StringUtils.space(1));																				//017-017
					txt.print(aac10.aac10ti == 0 ? "2" : "1");																		//018-018
					txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);														//019-032
					txt.print(abf01.abf01json.getString(getCampo("1","cod_convenio")), 20);											//033-052
					txt.print(abf01.abf01agencia, 5, '0', true);																	//053-057
					txt.print(abf01.abf01digAgencia, 1, '0', true);																	//058-058
					txt.print(abf01.abf01conta, 12, '0', true);																		//059-070
					txt.print(abf01.abf01digConta, 1, '0', true);																	//071-071
					txt.print(abf01.abf01digConta, 1, '0', true);																	//072-072
					txt.print(aac10.aac10rs, 30, true, true);																		//073-102
					txt.print(StringUtils.space(40));																				//103-142
					txt.print(aac10.aac10endereco, 30, true, true);																	//143-172
					txt.print(aac10.aac10numero, 5);																				//173-177
					txt.print(aac10.aac10complem, 15, true, true);																	//178-192
					txt.print(aac10.aac10municipio == null ? null : aac10.aac10municipio.aag0201nome, 20);							//193-212
					txt.print(aac10.aac10cep, 8);																					//213-220
					txt.print(aac10.aac10municipio == null ? null : aac10.aac10municipio.aag0201uf.aag02uf, 2);						//221-222
					txt.print(StringUtils.space(8));																				//223-230
					txt.print(StringUtils.space(10));																				//231-240
					txt.newLine();
					contador++;
				}else{
					txt.print("237");																								//001-003
					txt.print(++numLote, 4);																						//004-007
					txt.print("1");																									//008-008
					txt.print("C");																									//009-009
					txt.print("20");																								//010-011
					txt.print(cpoLivBco.get("formapgto"), 2);																		//012-013
					txt.print("045");																								//014-016
					txt.print(StringUtils.space(1));																				//017-017
					txt.print(aac10.aac10ti == 0 ? "2" : "1");																		//018-018
					txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);														//019-032
					txt.print(abf01.abf01json.getString(getCampo("1","cod_convenio")), 20);											//033-052
					txt.print(abf01.abf01agencia, 5, '0', true);																	//053-057
					txt.print(abf01.abf01digAgencia, 1, '0', true);																	//058-058
					txt.print(abf01.abf01conta, 12, '0', true);																		//059-070
					txt.print(abf01.abf01digConta, 1, '0', true);																	//071-071
					txt.print(abf01.abf01digConta, 1, '0', true);																	//072-072
					txt.print(aac10.aac10rs, 30, true, true);																		//073-102
					txt.print(StringUtils.space(40));																				//103-142
					txt.print(aac10.aac10endereco, 30, true, true);																	//143-172
					txt.print(aac10.aac10numero, 5);																				//173-177
					txt.print(aac10.aac10complem, 15, true, true);																	//178-192
					def municipio = null;
					def uf = null;
					if (aac10.aac10municipio != null) {
						TableMap tm = getAcessoAoBanco().buscarUnicoTableMap("SELECT aag0201nome, aag02uf FROM Aag0201 INNER JOIN Aag02 ON aag02id = aag0201uf WHERE aag0201id = :id", Parametro.criar("id", aac10.aac10municipio.aag0201id));
						municipio = tm.getString("aag0201nome");
						uf = tm.getString("aag02uf");
					}
					txt.print(municipio == null ? null : municipio, 20);															//193-212
					txt.print(aac10.aac10cep, 8);																					//213-220
					txt.print(uf == null ? null : uf, 2);																			//221-222
					txt.print("01");																								//223-224
					txt.print(StringUtils.space(6));																				//225-230
					txt.print(StringUtils.space(10));																				//231-240
					txt.newLine();
					contador++;
				}
			}

			if(cpoLivBco.get("formapgto") == "30" || cpoLivBco.get("formapgto") == "31"){
				/**
				 * DETALHE - SEGMENTO J
				 */
				txt.print("237"); 																									//001-003
				txt.print(numLote, 4);																								//004-007
				txt.print("3");																										//008-008
				txt.print(++qtDetalheLote, 5);																						//009-013
				txt.print("J");																										//014-014
				txt.print(abf01.abf01json.getString(getCampo("3","tipo_movimento")), 1, '0', true);									//015-015
				txt.print(abf01.abf01json.getString(getCampo("3","instru_movimento")), 2, '0', true);								//016-017
				txt.print(scfService.extrairDadosCodBarras(daa01.daa01leitor.equals(1), daa01.daa01codBarras), 44); 				//018-061

				Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);

				txt.print(abe01.abe01nome, 30, true, true);																			//062-091
				txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYYYY));																//092-099
				txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);											//100-114

				BigDecimal desconto = BigDecimal.ZERO;
				if(daa01.daa01json != null && daa01.daa01json.getDate(getCampo("3","dtlimdesc")) && daa01.daa01dtPgto.compareTo(daa01.daa01json.getDate(getCampo("3","dtlimdesc"))) <= 0) {
					desconto = daa01.daa01json.getBigDecimal(getCampo("3","desconto")).abs() * -1;
				}
				txt.print(desconto.multiply(new BigDecimal(100)).intValue(), 15);													//115-129

				Long dias = DateUtils.dateDiff(daa01.daa01dtVctoN, daa01.daa01dtPgto, ChronoUnit.DAYS);
				BigDecimal jme = daa01.daa01json != null && daa01.daa01json.getBigDecimal(getCampo("3","encargos")) != null ? daa01.daa01json.getBigDecimal(getCampo("3","encargos")) : BigDecimal.ZERO;
				if(dias > 0){
					BigDecimal multa = daa01.daa01json != null && daa01.daa01json.getBigDecimal(getCampo("3","multa")) != null ? daa01.daa01json.getBigDecimal(getCampo("3","multa")) : BigDecimal.ZERO;
					jme = jme.add(multa);
					BigDecimal juros = daa01.daa01json != null && daa01.daa01json.getBigDecimal(getCampo("3","juros")) != null ? daa01.daa01json.getBigDecimal(getCampo("3","juros")) : BigDecimal.ZERO;
					jme = jme.add(juros.multiply(new BigDecimal(dias)));
				}
				txt.print(jme.multiply(new BigDecimal(100)).intValue(), 15);														//130-144

				txt.print(daa01.daa01dtPgto.format(PATTERN_DDMMYYYY));																//145-152
				txt.print(daa01.daa01valor.add(jme).add(desconto).multiply(new BigDecimal(100)).intValue(), 15);					//153-167
				txt.print(0, 15);																									//168-182
				txt.print(daa01.daa01id + ";" + movimento, 20);																		//183-202
				txt.print(StringUtils.space(20));																					//203-222
				txt.print("09");																									//223-224
				txt.print(StringUtils.space(6));																					//225-230
				txt.print(StringUtils.space(10));																					//231-240

				totalDocsLote = totalDocsLote + daa01.daa01valor + jme + desconto;

				txt.newLine();
				contador++;

			}else{
				/**
				 * DETALHE - SEGMENTO A
				 */
				txt.print("237"); 																									//001-003
				txt.print(numLote, 4);																								//004-007
				txt.print("3");																										//008-008
				txt.print(++qtDetalheLote, 5);																						//009-013
				txt.print("A");																										//014-014
				txt.print(abf01.abf01json.getString(getCampo("3","tipo_movimento")), 1, '0', true);									//015-015
				txt.print(abf01.abf01json.getString(getCampo("3","instru_movimento")), 2, '0', true);								//016-017
				txt.print(abf01.abf01json.getString(getCampo("3","cod_camara_centr")), 3, '0', true);								//018-020

				Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);
				Abe03 abe03 = getSession().get(Abe03.class, Criterions.where("abe03ent = " + abe01.abe01id));
				
				txt.print(abe03.abe03bcoCod, 3, '0', true);																			//021-023
				txt.print(abe03.abe03bcoAg, 5, '0', true);																			//024-028
				txt.print(abe03.abe03bcoAgDig, 1, '0', true);																		//029-029
				txt.print(abe03.abe03bcoCta, 12, '0', true);																		//030-041
				txt.print(abe03.abe03bcoCtaDig, 1, '0', true);																		//042-042
				txt.print("0");																										//043-043
				
				txt.print(abe01.abe01nome, 30);																						//044-073
				txt.print(daa01.daa01id + ";" + movimento, 20);																		//074-093
				txt.print(daa0102.daa0102dtPgto.format(PATTERN_DDMMYYYY));															//094-101
				txt.print("BRL");																									//102-104
				txt.print(0, 15);																									//105-119
				txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);											//120-134
				txt.print(0, 20);																									//135-154
				txt.print(0, 8);																									//155-162
				txt.print(0, 15);																									//163-177
				txt.print(StringUtils.space(40));																					//178-217
				txt.print(abf01.abf01json.getString(getCampo("3","cod_fin_doc")), 2);												//218-219
				
				if("000".equals(abf01.abf01json.getString(getCampo("3","cod_camara_centr"))) && "237".equals(abe01.abe01json.getString(getCampo("3","banco")))){
					txt.print(StringUtils.space(5));																				//220-224
				}else{
					txt.print(abf01.abf01json.getString(getCampo("3","cod_fin_ted")), 5);											//220-224
				}
				
				txt.print(abf01.abf01json.getString(getCampo("3","cod_fin_compl")), 2);												//225-226
				txt.print(StringUtils.space(3));																					//227-229
				txt.print(0, 1);																									//230-230
				txt.print(StringUtils.space(10));																					//231-240
				
				totalDocsLote = totalDocsLote + daa01.daa01valor;

				txt.newLine();
				contador++;
				
				/**
				 * DETALHE - SEGMENTO B
				 */
				txt.print("237"); 																									//001-003
				txt.print(numLote, 4);																								//004-007
				txt.print("3");																										//008-008
				txt.print(++qtDetalheLote, 5);																						//009-013
				txt.print("B");																										//014-014
				txt.print(StringUtils.space(3));																					//015-017
				txt.print(abe01.abe01ti == 1 ? 1 : abe01.abe01ti == 0 ? 2 : 0);														//018-018
				txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, '0', true);												//019-032
				
				String sql = " SELECT abe0101endereco, abe0101numero, abe0101bairro, abe0101complem, abe0101cep, aag0201nome, aag02uf " +
							 " FROM abe0101 " +
							 " LEFT JOIN Aag0201 ON aag0201id = abe0101municipio " +
							 " LEFT JOIN Aag02 ON aag02id = aag0201uf " +
							 " WHERE abe0101ent = :abe01id " +
							 " AND abe0101principal = 1 ";
				
				TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abe01id", abe01.abe01id));
				
				txt.print(tm != null && tm.getString("abe0101endereco") != null ? tm.getString("abe0101endereco") : null, 30, true, true);//033-062
				txt.print(tm != null && tm.getString("abe0101numero") != null ? tm.getString("abe0101numero") : null, 5, '0', true);//063-067
				txt.print(tm != null && tm.getString("abe0101complem") != null ? tm.getString("abe0101complem") : null, 15); 		//068-082
				txt.print(tm != null && tm.getString("abe0101bairro") != null ? tm.getString("abe0101bairro") : null, 15);			//083-097
				txt.print(tm != null && tm.getString("aag0201nome") != null ? tm.getString("aag0201nome") : null, 20);				//098-117
				txt.print(tm != null && tm.getString("abe0101cep") != null ? tm.getString("abe0101cep") : null, 8, '0', true);		//118-125
				txt.print(tm != null && tm.getString("aag02uf") != null ? tm.getString("aag02uf") : null, 2);						//126-127
				
				txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYYYY));																//128-135
				txt.print(daa01.daa01valor.multiply(new BigDecimal(100)).intValue(), 15);											//136-150
				txt.print(0, 15);																									//151-165
				
				BigDecimal desconto = BigDecimal.ZERO;
				if(daa01.daa01json != null && daa01.daa01json.getDate(getCampo("3","dtlimdesc")) && daa01.daa01dtPgto.compareTo(daa01.daa01json.getDate(getCampo("3","dtlimdesc"))) <= 0) {
					desconto = daa01.daa01json.getBigDecimal(getCampo("3","desconto")).abs() * -1;
				}
				txt.print(desconto.multiply(new BigDecimal(100)).intValue(), 15);													//166-180

				BigDecimal juros = daa01.daa01json != null ? daa01.daa01json.getBigDecimal(getCampo("3","juros")) : BigDecimal.ZERO;
				txt.print(juros.multiply(new BigDecimal(100)).intValue(), 15);														//181-195
				
				BigDecimal multa = daa01.daa01json != null ? daa01.daa01json.getBigDecimal(getCampo("3","multa")) : BigDecimal.ZERO;
				txt.print(multa.multiply(new BigDecimal(100)).intValue(), 15);														//196-210
				txt.print(daa01.daa01id, 15);																						//211-225
				txt.print(0, 1);																									//226-226
				txt.print(0, 6);																									//227-232
				txt.print(0, 8);																									//233-240
				txt.newLine();
				contador++;
			}
		}
		trailerLoteBradesco(txt, numLote, qtDetalheLote, totalDocsLote);
		contador++;

		/**
		 * TRAILLER DO ARQUIVO
		 */
		txt.print("237");																											//001-003
		txt.print("9999");																											//004-007
		txt.print("9");																												//008-008
		txt.print(StringUtils.space(9));																							//009-017
		txt.print(numLote, 6);																										//018-023
		txt.print(contador+1, 6);																									//024-029
		txt.print(0, 6);																											//030-035
		txt.print(StringUtils.space(205));																							//036-240
		txt.newLine();



		put("txt", txt);
	}
	
	private void trailerLoteBradesco(TextFile txt, Integer numLote, Integer qtDetalheLote, BigDecimal totalDocsLote) {
		/**
		 * TRAILER DO LOTE
		 */
		txt.print("237");																											//001-003
		txt.print(numLote, 4);																										//004-007
		txt.print("5");																												//008-008
		txt.print(StringUtils.space(9));																							//009-017
		txt.print(2+qtDetalheLote, 6);																								//018-023
		txt.print(totalDocsLote.multiply(new BigDecimal(100)).intValue(), 18);														//024-041
		txt.print(0, 18);																											//042-059
		txt.print(0, 6);																											//060-065	
		txt.print(StringUtils.space(165));																							//225-230
		txt.print(StringUtils.space(10));																							//231-240
		txt.newLine();
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDQifQ==