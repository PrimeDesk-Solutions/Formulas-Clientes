package multitec.formulas.sfp;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.criteria.client.ClientCriterion;
import sam.core.criteria.ClientCriteriaConvert;
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFP8505Dto;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh2101;
import sam.model.entities.fb.Fba01;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;

public class GRRF extends FormulaBase {
	private SFP8505Dto sfp8505Dto;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.GRRF;
	}

	@Override
	public void executar() {
		this.sfp8505Dto = get("sfp8505Dto");

				
		/******************************** CAMPOS FIXOS DA FÓRMULA ***********************************/
		def codFPAS = "507";
		def simples = new BigDecimal(1);
		def GRRFDissidio = "";
		def GRRFRemMesAnt = "";
		def GRRFRemMesAtual = "6102";
		def GRRFAvisoPrevio = "6101";
		def GRRFPensao = "";
		def GRRFSaldo = "6103";
		/******************************** CAMPOS FIXOS DA FÓRMULA ***********************************/
		
		TextFile txt = new TextFile();
		
		gerarTipo00(txt, sfp8505Dto);
		gerarTipo10(txt, sfp8505Dto, simples, codFPAS);
		gerarTipo40(txt, sfp8505Dto, GRRFDissidio, GRRFRemMesAnt, GRRFRemMesAtual, GRRFAvisoPrevio, GRRFPensao, GRRFSaldo);
		gerarTipo90(txt);
		
		put("txt", txt);
	}
	
	/**
	 * TIPO 00
	 */
	private void gerarTipo00(TextFile txt, SFP8505Dto sfp8505Dto) {
		def fone = ( sfp8505Dto.respDDD == null ? "" : sfp8505Dto.respDDD ) + (sfp8505Dto.respFone == null ? "" : sfp8505Dto.respFone )
		txt.print(0, 2, "0", true);																																				//001 a 002
		txt.print(StringUtils.space(51));																				    																	//003 a 053
		txt.print(2, 1);																																							//054 a 054
		txt.print(sfp8505Dto.respTI, 1);																																		//055 a 055
		txt.print(StringUtils.extractNumbers(sfp8505Dto.respNI), 14, '0', true);																								//056 a 069
		txt.print(StringUtils.unaccented(sfp8505Dto.respNome), 30);																											//070 a 099
		txt.print(StringUtils.unaccented(sfp8505Dto.respContato), 20);																										//100 a 119
		txt.print(StringUtils.unaccented(sfp8505Dto.respEndereco), 50);																										//120 a 169
		txt.print(StringUtils.unaccented(sfp8505Dto.respBairro), 20);																										//170 a 189
		txt.print(StringUtils.extractNumbers(sfp8505Dto.respCEP), 8, '0', true);																								//190 a 197
		txt.print(StringUtils.unaccented(sfp8505Dto.respMunicipio), 20);																										//198 a 217
		txt.print(sfp8505Dto.respUF, 2);																																		//218 a 219
		txt.print(StringUtils.extractNumbers(fone), 12, '0', true);																							//220 a 231
		txt.print(StringUtils.unaccented(sfp8505Dto.respEndInternet), 60);																									//232 a 291
		txt.print(sfp8505Dto.dataRec == null ? 0 : sfp8505Dto.dataRec.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);									//292 a 299
		txt.print(StringUtils.space(60));																																						//300 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	/**
	 * TIPO 10
	 */
	private void gerarTipo10(TextFile txt, SFP8505Dto sfp8505Dto, BigDecimal simples, String codFPAS) {
		Aac10 aac10 = getSession().createCriteria(Aac10.class)
				.addFields("aac10id, aac10ti, aac10ni, aac10endereco, aac10numero, aac10complem, aac10bairro, aac10dddFone, aac10fone, aac10rs, aac10cep, aac10cnae, aag0201id, aag0201nome, aag02id, aag02uf")
				.addJoin(Joins.part("aac10municipio").partial(true).left(true))
				.addJoin(Joins.part("aac10municipio.aag0201uf").partial(true).left(true))
				.addWhere(Criterions.eq("aac10id", getVariaveis().getAac10().getAac10id()))
				.get(ColumnType.ENTITY);
		
		def enderecoEmp = null;
		if(aac10.aac10endereco != null) {
			if(aac10.aac10numero != null) {
				enderecoEmp = aac10.aac10endereco + " " + aac10.aac10numero;
			}else {
				enderecoEmp = aac10.aac10endereco;
			}
			
			if(aac10.aac10complem != null) {
				enderecoEmp += " " + aac10.aac10complem;
			}
		}
		
		def foneAac10 = aac10.aac10dddFone != null && aac10.aac10fone != null ? aac10.aac10dddFone + aac10.aac10fone : aac10.aac10fone;
		
		txt.print(10, 2);																																							//001 a 002
		txt.print(aac10.aac10ti + 1, 1);																																			//003 a 003
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																									//004 a 017
		txt.print(0, 36, '0', true);																					    														//018 a 053
		txt.print(StringUtils.unaccented(aac10.aac10rs), 40);																												//054 a 093
		txt.print(StringUtils.unaccented(enderecoEmp), 50);																														//094 a 143
		txt.print(StringUtils.unaccented(aac10.aac10bairro), 20);																											//144 a 163
		txt.print(aac10.aac10cep, 8);																																		//164 a 171
		txt.print(aac10.aac10municipio != null ? StringUtils.unaccented(aac10.aac10municipio.aag0201nome) : "", 20);												//172 a 191
		txt.print(aac10.aac10municipio != null ? aac10.aac10municipio.aag0201uf.aag02uf : "", 2);																//192 a 193
		txt.print(StringUtils.extractNumbers(foneAac10), 12, '0', true);																											//194 a 205
		txt.print(StringUtils.extractNumbers(aac10.aac10cnae), 7);																											//206 a 212
		txt.print(simples == null ? 1 : simples.intValue(), 1);																													//213 a 213
		txt.print(codFPAS, 3);																																					//214 a 216
		txt.print(StringUtils.space(143));																																						//217 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	/**
	 * TIPO 40
	 */
	private void gerarTipo40(TextFile txt, SFP8505Dto sfp8505Dto, String GRRFDissidio, String GRRFRemMesAnt, String GRRFRemMesAtual, String GRRFAvisoPrevio, String GRRFPensao, String GRRFSaldo) {
		Aac10 aac10 = getVariaveis().getAac10();
		
		List<TableMap> tmFbd10s = buscarDadosDaRescisaoParaGRRF(sfp8505Dto.getClientCriterionAbh80(), sfp8505Dto.getDataInicial(), sfp8505Dto.getDataFinal());
		if(tmFbd10s != null && tmFbd10s.size() > 0) {
			for(TableMap tm : tmFbd10s) {
				def abh80id = tm.getLong("abh80id");
				def abh80pis = tm.getString("abh80pis");
				
				//CBO.
				def cbo = tm.getString("aap03codigo");
				if(cbo != null) cbo = cbo.substring(0, 4);
				
				//Data de ínicio do aviso prévio.
				def dataIniAviso = tm.getDate("fbd10apData") == null || tm.getInteger("abh06avPrevGrrf") == 2 ? "00000000" : tm.getDate("fbd10apData").format(DateTimeFormatter.ofPattern("ddMMyyyy"));
				
				//Data da homologação.
				def dataHomol = tm.getDate("fbd10homData");
				
				//Valor Dissídio.
				def vlrDissidio = dataHomol != null ? calcularCAE(abh80id, GRRFDissidio, sfp8505Dto.getDataInicial(), sfp8505Dto.getDataFinal()) : BigDecimal.ZERO;
				
				//Remuneração do mês anterior.
				def dataInicialRMA = sfp8505Dto.getDataRec().minusMonths(1).withDayOfMonth(1);
				def dataFinalRMA = dataInicialRMA.withDayOfMonth(dataInicialRMA.lengthOfMonth());
				
				def vlrMesAnt = dataHomol == null ? calcularCAE(abh80id, GRRFRemMesAnt, dataInicialRMA, dataFinalRMA) : BigDecimal.ZERO;
				
				//Remuneração no mês da rescisão.
				def vlrMesRes = /*dataHomol == null ?*/ calcularCAE(abh80id, GRRFRemMesAtual, sfp8505Dto.getDataInicial(), sfp8505Dto.getDataFinal())/*: BigDecimal.ZERO;*/
				
				//Aviso prévio indenizado.
				def vlrAvPrevInd = tm.getInteger("fbd10apDias") == 0 ? BigDecimal.ZERO : calcularCAE(abh80id, GRRFAvisoPrevio, sfp8505Dto.getDataInicial(), sfp8505Dto.getDataFinal());
				
				//Pensão alimentícia.
				def vlrPensao = calcularCAE(abh80id, GRRFPensao, sfp8505Dto.getDataInicial(), sfp8505Dto.getDataFinal());
				def indPensao = vlrPensao == 0 ? "N" : (tm.getBigDecimal("fbd10paPercFgts") > 0 ? "P" : "V");
				
				//Saldo para fins rescisórios.
				def vlrSaldo = vlrDissidio > 0 ? BigDecimal.ZERO : calcularCAE(abh80id, GRRFSaldo, sfp8505Dto.getDataInicial(), sfp8505Dto.getDataFinal());
				
				NumberFormat nb = NumberFormat.getNumberInstance();
				nb.setMinimumFractionDigits(2);
				nb.setGroupingUsed(false);
				
				txt.print(40, 2);																																					//001 a 002
				txt.print(aac10.aac10ti + 1, 1);																																	//003 a 003
				txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																							//004 a 017
				txt.print("0", 1);																																				//018 a 018
				txt.print(StringUtils.extractNumbers("0"), 14, '0', true);																										//019 a 032
				txt.print(abh80pis == null ? "" : StringUtils.extractNumbers(abh80pis), 11);																						//033 a 043
				txt.print(tm.getDate("abh80dtAdmis").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																			//044 a 051
				txt.print(tm.getString("aap14grrf"), 2);																															//052 a 053
				txt.print(StringUtils.unaccented(tm.getString("abh80nome")), 70);																									//054 a 123
				txt.print(tm.getString("abh80ctpsNum"), 7, '0', true);																											//124 a 130
				txt.print(tm.getString("abh80ctpsSerie"), 5, '0', true);																											//131 a 135
				txt.print(tm.getInteger("abh80sexo") == 0 ? 1 : 2, 1);																											//136 a 136
				txt.print(tm.getString("aap06grrf"), 2);																															//137 a 138
				txt.print(tm.getDate("abh80nascData").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																		//139 a 146
				txt.print(tm.getInteger("abh80hs"), 2);																															//147 a 148
				txt.print(cbo, 6, '0', true);																																		//149 a 154
				txt.print(tm.getDate("abh80dtAdmis").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																			//155 a 162
				txt.print(tm.getString("abh06sefip"), 2);																															//163 a 164
				txt.print(tm.getDate("fbd10dtRes").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																			//165 a 172
				txt.print(tm.getString("abh06saqueFgts"), 3);																														//173 a 175
				txt.print(tm.getInteger("abh06avPrevGrrf"), 1);																													//176 a 176
				txt.print(dataIniAviso, 8);																																		//177 a 184
				txt.print("S", 1);																																				//185 a 185
				txt.print(dataHomol == null ? 0 : dataHomol.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);				 										//186 a 193
				txt.print(StringUtils.extractNumbers(nb.format(vlrDissidio.setScale(2))), 15, '0', true);																			//194 a 208
				txt.print(StringUtils.extractNumbers(nb.format(vlrMesAnt.setScale(2))), 15, '0', true);																			//209 a 223
				txt.print(StringUtils.extractNumbers(nb.format(vlrMesRes.setScale(2))), 15, '0', true);																			//224 a 238
				txt.print(StringUtils.extractNumbers(nb.format(vlrAvPrevInd.setScale(2))), 15, '0', true);																		//239 a 253
				txt.print(indPensao, 1);																																			//254 a 254
				txt.print(indPensao.equals("P") ? tm.getBigDecimal("fbd10paPercFgts") : 0, 5, '0', true);																			//255 a 259
				txt.print(StringUtils.extractNumbers(nb.format(vlrPensao.setScale(2))), 15, '0', true);																			//260 a 274
				txt.print(tm.getString("abh80cpf"), 11);																															//275 a 285
				txt.print(0, 3, '0', true);																																		//286 a 288
				txt.print(0, 4, '0', true);																																		//289 a 292
				txt.print(0, 13, '0', true);																																		//293 a 305
				txt.print(StringUtils.extractNumbers(nb.format(vlrSaldo.setScale(2))), 15, '0', true);																			//306 a 320
				txt.print(StringUtils.space(39));																																				//321 a 359
				txt.print("*", 1);																																				//360 a 360
				txt.newLine();
			}
		}
	}
	
	/**
	 * TIPO 90
	 */
	private void gerarTipo90(TextFile txt) {
		txt.print(90, 2);																																							//001 a 002
		txt.print("999999999999999999999999999999999999999999999999999", 51);		    																							//003 a 053
		txt.print(StringUtils.space(306));																																						//054 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	private String ajustarCampo(Object string, int tamanho) {
		return StringUtils.ajustString(string, tamanho);
	}

	private String ajustarCampo(Object string, int tamanho, String character, boolean concatAEsquerda) {
		return StringUtils.ajustString(string, tamanho, character, concatAEsquerda);
	}
	
	private List<TableMap> buscarDadosDaRescisaoParaGRRF(ClientCriterion clientCriterionAbh80, LocalDate dataInicial, LocalDate dataFinal) {
		return getSession().createCriteria(Fbd10.class)
				.addFields("abh80id, abh80nome, abh80cpf, abh80pis, abh80dtAdmis, abh80ctpsNum, abh80ctpsSerie, abh80sexo, abh80nascData, abh80hs, aap14grrf, aap06grrf, aap03codigo, abh06sefip, abh06saqueFgts, abh06avPrevGrrf, fbd10dtRes, fbd10apDias, fbd10apData, fbd10homData, fbd10paPercFgts")
				.addJoin(Joins.join("fbd10trab").alias("fbd10trab"))
				.addJoin(Joins.join("fbd10trab.abh80cargo").alias("abh80cargo"))
				.addJoin(Joins.join("fbd10causa").alias("fbd10causa"))
				.addJoin(Joins.join("fbd10trab.abh80categ").alias("abh80categ").left(true))
				.addJoin(Joins.join("fbd10trab.abh80gi").alias("abh80gi").left(true))
				.addJoin(Joins.join("abh80cargo.abh05cbo").alias("abh05cbo").left(true))
				.addWhere(Criterions.between("fbd10dtRes", dataInicial, dataFinal))
				.addWhere(Criterions.in("aap14grrf", ["01", "02", "03", "04", "05", "06", "07"]))
				.addWhere(Criterions.in("abh06sefip", ["I1", "I2", "I3", "I4", "I5", "H", "J", "L", "M"]))
				.addWhere(ClientCriteriaConvert.convertCriterion(clientCriterionAbh80))
				.getListTableMap();
	}
	
	private BigDecimal calcularCAE(Long abh80id, String abh20codigo, LocalDate dataInicial, LocalDate dataFinal) {
		BigDecimal valor = BigDecimal.ZERO;

		List<TableMap> tms = buscarValoresDosEventosParaCalculoDoCAE(abh80id, abh20codigo, dataInicial, dataFinal);
		if(tms != null && tms.size() > 0) {
			for(int i = 0; i < tms.size(); i++) {
				TableMap tm = tms.get(i);

				if(tm.getInteger("abh2101cvr") == Abh2101.CVR_SOMA_VLR) {
					valor = valor + tm.getBigDecimal("fba01011valor");
				}else if(tm.getInteger("abh2101cvr") == Abh2101.CVR_DIMINUI_VLR) {
					valor = valor - tm.getBigDecimal("fba01011valor");
				}
			}
		}
		return valor;
	}
	
	private List<TableMap> buscarValoresDosEventosParaCalculoDoCAE(Long abh80id, String abh20codigo, LocalDate dataInicial, LocalDate dataFinal) {
		Query query = getSession().createQuery("SELECT abh2101cvr, fba01011valor ",
									           "FROM Fba01011 ",
									           "INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
									           "INNER JOIN Fba01 ON fba01id = fba0101calculo ",
									           "INNER JOIN Abh21 ON abh21id = fba01011eve ",
									           "INNER JOIN Abh2101 ON abh2101evento = abh21id ",
									           "INNER JOIN Abh20 ON abh20id = abh2101cae ",
									           "AND fba0101trab = :abh80id ",
									           "AND abh20codigo = :abh20codigo ", 
									           "AND fba0101dtCalc BETWEEN :dataInicial AND :dataFinal ",
									           getSamWhere().getWherePadrao("AND", Fba01.class));
		
		query.setParameter("abh80id", abh80id);
		query.setParameter("abh20codigo", abh20codigo);
		query.setParameter("dataInicial", dataInicial);
		query.setParameter("dataFinal", dataFinal);
		return query.getListTableMap();		
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTYifQ==