package multitec.formulas.sfp;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.criteria.client.ClientCriterion;
import sam.core.criteria.ClientCriteriaConvert;
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFP8501Dto;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac1001;
import sam.model.entities.ab.Abb40;
import sam.model.entities.ab.Abh02;
import sam.model.entities.ab.Abh2101;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fa.Fab01;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fba02;
import sam.model.entities.fb.Fbd10;
import sam.model.entities.fb.Fbg01;
import sam.model.entities.fb.Fbg02;
import sam.model.entities.fb.Fbg03;
import sam.server.samdev.formula.FormulaBase;

public class Sefip84 extends FormulaBase {
	private SFP8501Dto sfp8501Dto;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SEFIP;
	}

	@Override
	public void executar() {
		this.sfp8501Dto = get("sfp8501Dto");

		TextFile txt = new TextFile();
				
		/******************************** CAMPOS FIXOS DA FÓRMULA ***********************************/
		def codFPAS = "507";
		def simples = new BigDecimal(1);
		def taxaRAT = new BigDecimal(3);
		def recFGTS = BigDecimal.ZERO;
		def codINSSTerc = "0079";
		def codPgtoGPS = "2100";
		def taxaIsentaFilan = BigDecimal.ZERO;
		def sefipCAESF = "6201";
		def sefipCAESM = "6202";
		def sefipCAEDed13 = "6217";
		def sefipCAERem = "6210";
		def sefipCAERem13 = "6211";
		def sefipCAEDesc = "6212";
		def sefipCAESCINSS = "6213";
		def sefipCAESC13COM = "6215";
		def sefipCAESC13GPS = "6214";
		def sefipCAESC13 = "6216";
		
		Set<String> codsAap52 = new HashSet<>();
		codsAap52.add("03");
		codsAap52.add("04");
		codsAap52.add("05");
		codsAap52.add("06");
		codsAap52.add("07");
		codsAap52.add("08");
		/******************************** CAMPOS FIXOS DA FÓRMULA ***********************************/
		
		gerarTipo00(txt, sfp8501Dto);
		
		def mesRef = sfp8501Dto.mesAnoRef.getMonthValue();
		def anoRef = sfp8501Dto.mesAnoRef.getYear();
		def dataRefAdmis = sfp8501Dto.mesAnoRef.withDayOfMonth(sfp8501Dto.mesAnoRef.lengthOfMonth());
		List<Aac10> aac10s = buscarEmpresasParaSefip(sfp8501Dto.clientCriterionAac10);
		
		if(aac10s != null && aac10s.size() > 0) {
			for(Aac10 aac10 : aac10s) {
				Long aac01id = buscarIdDoGrupoCentralizadorPorIdEmpresa(aac10.aac10id);
				
				if(sfp8501Dto.codigoRec == 608) {
					List<String> cnpjsAac10sOrigem = buscarCNPJsDasEmpresasOrigensParaTipo608(aac01id);
					if(cnpjsAac10sOrigem != null && cnpjsAac10sOrigem.size() > 0) {
						for(String abh80eoCNPJ : cnpjsAac10sOrigem) {
							List<TableMap> tmsTrabalhadores = buscarDadosDeTrabalhadores(aac01id, sfp8501Dto.tiposAbh80, sfp8501Dto.tiposFba0101, dataRefAdmis, mesRef, anoRef, null, abh80eoCNPJ);
							if(tmsTrabalhadores != null && tmsTrabalhadores.size() > 0) {
								Aac10 aac10Origem = buscarEmpresaPorNI(abh80eoCNPJ);
								if(aac10Origem != null) {
									gerarTipo10(txt, sfp8501Dto, aac10Origem, aac01id, codFPAS, simples, taxaRAT, recFGTS, codINSSTerc, codPgtoGPS, taxaIsentaFilan, sefipCAESF, sefipCAESM);
									gerarTipo12(txt, sfp8501Dto, aac10Origem, aac01id, sefipCAEDed13);
									gerarTipo13(txt, sfp8501Dto, aac10Origem, aac01id);
									gerarTipo14(txt, sfp8501Dto, aac10Origem, aac01id);
									gerarTipo20(txt, sfp8501Dto, aac10Origem, aac10, null, codPgtoGPS, sefipCAESF, mesRef, anoRef, true);
									gerarTipo30(txt, sfp8501Dto, aac10Origem, aac10, aac01id, null, tmsTrabalhadores, sefipCAERem, sefipCAERem13, sefipCAEDesc, sefipCAESCINSS, sefipCAESC13COM, sefipCAESC13GPS, sefipCAESC13, true);
									
								}
							}
						}
					}
				}else {
					gerarTipo10(txt, sfp8501Dto, aac10, aac01id, codFPAS, simples, taxaRAT, recFGTS, codINSSTerc, codPgtoGPS, taxaIsentaFilan, sefipCAESF, sefipCAESM);
					gerarTipo12(txt, sfp8501Dto, aac10, aac01id, sefipCAEDed13);
					gerarTipo13(txt, sfp8501Dto, aac10, aac01id);
					gerarTipo14(txt, sfp8501Dto, aac10, aac01id);
					
					List<TableMap> tmsTomadoras = new ArrayList<>();
					List<TableMap> tms = buscarDadosDeLotacoesTomadoras(aac01id, Abh02.TI_CNPJ, sfp8501Dto.tiposAbh80, sfp8501Dto.tiposFba0101, dataRefAdmis, mesRef, anoRef, codsAap52);
					if(tms != null && tms.size() > 0) tmsTomadoras.addAll(tms);
					tms = buscarDadosDeLotacoesTomadoras(aac01id, Abh02.TI_CNO, sfp8501Dto.tiposAbh80, sfp8501Dto.tiposFba0101, dataRefAdmis, mesRef, anoRef, codsAap52);
					if(tms != null && tms.size() > 0) tmsTomadoras.addAll(tms);
					
					for(TableMap tmTomador : tmsTomadoras) {
						List<TableMap> tmsTrabalhadores = buscarDadosDeTrabalhadoresTomadores(aac01id, sfp8501Dto.tiposAbh80, sfp8501Dto.tiposFba0101, dataRefAdmis, mesRef, anoRef, tmTomador.getLong("abh02id"));
						if(tmsTrabalhadores != null && tmsTrabalhadores.size() > 0) {
							gerarTipo20(txt, sfp8501Dto, aac10, null, tmTomador, codPgtoGPS, sefipCAESF, mesRef, anoRef, false);
							gerarTipo30(txt, sfp8501Dto, aac10, null, aac01id, tmTomador, tmsTrabalhadores, sefipCAERem, sefipCAERem13, sefipCAEDesc, sefipCAESCINSS, sefipCAESC13COM, sefipCAESC13GPS, sefipCAESC13, false);
						}
					}
					
					List<Long> idsAbh02 = tmsTomadoras.stream().map({tm -> tm.getLong("abh02id")}).collect(Collectors.toList());
					List<TableMap> tmsTrabalhadores = buscarDadosDeTrabalhadores(aac01id, sfp8501Dto.tiposAbh80, sfp8501Dto.tiposFba0101, dataRefAdmis, mesRef, anoRef, idsAbh02, null);
					if(tmsTrabalhadores != null && tmsTrabalhadores.size() > 0) {
						gerarTipo30(txt, sfp8501Dto, aac10, null, aac01id, null, tmsTrabalhadores, sefipCAERem, sefipCAERem13, sefipCAEDesc, sefipCAESCINSS, sefipCAESC13COM, sefipCAESC13GPS, sefipCAESC13, false);
					}
				}
			}
		}
		
		gerarTipo90(txt);
		
		put("txt", txt);
	}
	
	/**
	 * TIPO 00
	 * Informações do responsável
	 */
	private void gerarTipo00(TextFile txt, SFP8501Dto sfp8501Dto) {
		def respFone = sfp8501Dto.respDDD != null && sfp8501Dto.respFone != null ? sfp8501Dto.respDDD + sfp8501Dto.respFone : sfp8501Dto.respFone;
		def competencia = sfp8501Dto.competencia13Sal ? sfp8501Dto.mesAnoRef.format(DateTimeFormatter.ofPattern("yyyy")) + "13" : sfp8501Dto.mesAnoRef.format(DateTimeFormatter.ofPattern("yyyyMM"));
		
		txt.print(0, 2, '0', true);																																				//001 a 002
		txt.print(StringUtils.space(51));																				    																	//003 a 053
		txt.print(1, 1);																																							//054 a 054
		txt.print(sfp8501Dto.respTI, 1);																																		//055 a 055
		txt.print(StringUtils.extractNumbers(sfp8501Dto.respNI), 14, '0', true);																								//056 a 069
		txt.print(StringUtils.unaccented(sfp8501Dto.respNome), 30);																											//070 a 099
		txt.print(StringUtils.unaccented(sfp8501Dto.respContato), 20);																										//100 a 119
		txt.print(StringUtils.unaccented(sfp8501Dto.respEndereco), 50);																										//120 a 169
		txt.print(StringUtils.unaccented(sfp8501Dto.respBairro), 20);																										//170 a 189
		txt.print(StringUtils.extractNumbers(sfp8501Dto.respCEP), 8, '0', true);																								//190 a 197
		txt.print(StringUtils.unaccented(sfp8501Dto.respMunicipio), 20);																										//198 a 217
		txt.print(sfp8501Dto.respUF, 2);																																		//218 a 219
		txt.print(StringUtils.extractNumbers(respFone), 12, '0', true);																											//220 a 231
		txt.print(StringUtils.unaccented(sfp8501Dto.respEndInternet), 60);																									//232 a 291
		txt.print(competencia, 6);																																				//292 a 297
		txt.print(sfp8501Dto.codigoRec, 3);																																	//298 a 300
		txt.print(sfp8501Dto.competencia13Sal || sfp8501Dto.codigoRecFGTS == 0 ? " " : sfp8501Dto.codigoRecFGTS, 1);												//301 a 301
		txt.print(sfp8501Dto.competencia13Sal ? 1 : sfp8501Dto.modalidadeArquivo == 0 ? " " : sfp8501Dto.modalidadeArquivo, 1);										//302 a 302
		txt.print(sfp8501Dto.codigoRecFGTSData == null ? StringUtils.space(8) : sfp8501Dto.codigoRecFGTSData.format(DateTimeFormatter.ofPattern("ddMMyyyy")));						//303 a 310
		txt.print(sfp8501Dto.codigoRecGPS, 1);																																//311 a 311
		txt.print(sfp8501Dto.codigoRecGPSData == null ? StringUtils.space(8) : sfp8501Dto.codigoRecGPSData.format(DateTimeFormatter.ofPattern("ddMMyyyy")));							//312 a 319
		txt.print(StringUtils.space(7));																																						//320 a 326
		txt.print(1, 1);																																							//327 a 327
		txt.print("67919092000189", 14);																																			//328 a 341
		txt.print(StringUtils.space(18));																																						//342 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	/**
	 * TIPO 10
	 * Informações da empresa
	 */
	private void gerarTipo10(TextFile txt, SFP8501Dto sfp8501Dto, Aac10 aac10, Long aac01id, String codFPAS, BigDecimal simples, BigDecimal taxaRAT, BigDecimal recFGTS, String codINSSTerc, String codPgtoGPS, BigDecimal taxaIsentaFilan, String sefipCAESF, String sefipCAESM) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		//Endereço formatado.
		def endereco = null;
		if(aac10.aac10endereco != null) {
			if(aac10.aac10numero != null) {
				endereco = aac10.aac10endereco + " " + aac10.aac10numero;
			}else {
				endereco = aac10.aac10endereco;
			}
			
			if(aac10.aac10complem != null) {
				endereco += " " + aac10.aac10complem;
			}
		}
		
		//Telefone formatado.
		def foneAac10 = (aac10.aac10dddFone != null && aac10.aac10fone != null) ? aac10.aac10dddFone + aac10.aac10fone : "";
		
		//Alíquota RAT.
		def aliquotaRAT = null;
		boolean isCodRec = isCodigoValido(["145", "307", "317", "327", "337", "345", "640", "660"], String.valueOf(sfp8501Dto.codigoRec));
		boolean isCodFPAS = isCodigoValido(["604", "647", "825", "833", "868"], codFPAS);

		Set<Integer> codigosSimples = new HashSet<Integer>();
		codigosSimples.add(2);
		codigosSimples.add(3);
		codigosSimples.add(6);

		if(isCodFPAS || (simples != null && codigosSimples.contains(simples.intValue()))) {
			aliquotaRAT = "00";
		}else if(taxaRAT != null && !isCodRec) {
			aliquotaRAT = StringUtils.extractNumbers(nb.format(taxaRAT.setScale(2)));
			if(aliquotaRAT.length() == 1) aliquotaRAT = aliquotaRAT + "0";
		}else {
			aliquotaRAT = "";
		}
		
		//Código de centralização.
		isCodRec = isCodigoValido(["130", "135", "150", "155", "211", "317", "337", "608"], String.valueOf(sfp8501Dto.codigoRec));
		def codCentral = null;
		if(isCodRec || (codFPAS != null && codFPAS.equals("868"))) {
			codCentral = "0";
		}else if(recFGTS != null) {
			codCentral = nb.format(recFGTS.setScale(2));
		}
		
		//Código de outras entidades.
		def codOutrasEnt = null;
		if(isCodRec || (simples != null && codigosSimples.contains(simples.intValue()))) {
			codOutrasEnt = "";
		}else if(codFPAS != null && (codFPAS == "582" || codFPAS == "868")) {
			codOutrasEnt = "0000";
		}else {
			codOutrasEnt = codINSSTerc != null ? codINSSTerc : "";
		}
		
		//Código de pagamento da GPS.
		isCodRec = isCodigoValido(["115", "150", "211", "650"], String.valueOf(sfp8501Dto.codigoRec));
		def codGPS = (isCodRec && codPgtoGPS != null) ? codPgtoGPS : "";
		
		//Percentual de isenção de filantropia.
		def percFilan = codFPAS != null && codFPAS == "639" && taxaIsentaFilan != null ? StringUtils.extractNumbers(nb.format(taxaIsentaFilan.setScale(2))) : StringUtils.space(5);
		
		//Salário-família.
		def salFamilia = !sfp8501Dto.competencia13Sal && sfp8501Dto.codigoRec == "115" ? calcularCAE(aac01id, null, null, null, sefipCAESF, sfp8501Dto.mesAnoRef.getMonthValue(), sfp8501Dto.mesAnoRef.getYear(), sfp8501Dto.tiposFba0101) : BigDecimal.ZERO;

		//Salário-maternidade.
		isCodRec = isCodigoValido(["115", "150", "155", "608"], String.valueOf(sfp8501Dto.codigoRec));
		def salMaternidade = (!sfp8501Dto.competencia13Sal && isCodRec) ? calcularCAE(aac01id, null, null, null, sefipCAESM, sfp8501Dto.mesAnoRef.getMonthValue(), sfp8501Dto.mesAnoRef.getYear(), sfp8501Dto.tiposFba0101) : BigDecimal.ZERO;
		
		txt.print(10, 2);																																							//001 a 002
		txt.print("1", 1);																																						//003 a 003
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																									//004 a 017
		txt.print(0, 36, '0', true);																					   															//018 a 053
		txt.print(StringUtils.unaccented(aac10.aac10rs), 40);																												//054 a 093
		txt.print(StringUtils.unaccented(endereco), 50);																															//094 a 143
		txt.print(StringUtils.unaccented(aac10.aac10bairro), 20);																											//144 a 163
		txt.print(aac10.aac10cep, 8);																																		//164 a 171
		txt.print(aac10.aac10municipio != null ? StringUtils.unaccented(aac10.aac10municipio.aag0201nome) : null, 20);												//172 a 191
		txt.print(aac10.aac10municipio != null ? aac10.aac10municipio.aag0201uf.aag02uf : null, 2);															//192 a 193
		txt.print(StringUtils.extractNumbers(foneAac10), 12, '0', true);																											//194 a 205
		txt.print(sfp8501Dto.competencia13Sal ? "N" : sfp8501Dto.enderecoEmpAlterado ? "S" : "N", 1);																		//206 a 206
		txt.print(StringUtils.extractNumbers(aac10.aac10cnae), 7);																											//207 a 213
		txt.print(sfp8501Dto.competencia13Sal ? "N" : sfp8501Dto.empresaCNAE, 1);																						//214 a 214
		txt.print(aliquotaRAT, 2);																																				//215 a 216
		txt.print(codCentral, 1);																																					//217 a 217
		txt.print(simples, 1);																																					//218 a 218
		txt.print(codFPAS, 3);																																					//219 a 221
		txt.print(codOutrasEnt, 4);																																				//222 a 225
		txt.print(codGPS, 4);																																						//226 a 229
		txt.print(percFilan, 5, '0', true);																																		//230 a 234
		txt.print(StringUtils.extractNumbers(nb.format(salFamilia.setScale(2))), 15, '0', true);																					//235 a 249
		txt.print(StringUtils.extractNumbers(nb.format(salMaternidade.setScale(2))), 15, '0', true);																				//250 a 264
		txt.print(0, 15, '0', true);																																				//265 a 279
		txt.print(0, 1);																																							//280 a 280
		txt.print(0, 14, '0', true);																																				//281 a 294
		txt.print(StringUtils.space(16));																																						//295 a 310
		txt.print(0, 45, '0', true);																																				//311 a 355
		txt.print(StringUtils.space(4));																																						//356 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	/**
	 * TIPO 12
	 * Informações adicionais do recolhimento da empresa
	 */
	private void gerarTipo12(TextFile txt, SFP8501Dto sfp8501Dto, Aac10 aac10, Long aac01id, String sefipCAEDed13) {
		Fbg01 fbg01 = buscarInformacoesAdicionaisDaEmpresa(sfp8501Dto.mesAnoRef.getYear(), sfp8501Dto.mesAnoRef.getMonthValue());
		if(fbg01 != null) {
			NumberFormat nb = NumberFormat.getNumberInstance();
			nb.setMinimumFractionDigits(2);
			nb.setGroupingUsed(false);
			
			//Dedução 13º salário para licença maternidade.
			boolean isCodRec = isCodigoValido(["130", "135", "145", "211", "307", "317", "327", "337", "345", "640", "650", "660"], String.valueOf(sfp8501Dto.codigoRec));
			def ded13SalMatern = null;
			if(isCodRec) {
				ded13SalMatern = BigDecimal.ZERO;
			}else {
				ded13SalMatern = fbg01.fbg01dedLicMat > 0 ? fbg01.fbg01dedLicMat : calcularCAE(aac01id, null, null, null, sefipCAEDed13, sfp8501Dto.mesAnoRef.getMonthValue(), sfp8501Dto.mesAnoRef.getYear(), sfp8501Dto.tiposFba0101);
			}
			
			//Indicativo origem da receita.
			def fbg01origemRec = fbg01.fbg01origemRec == 0 ? "E" : fbg01.fbg01origemRec == 1 ? "P" : fbg01.fbg01origemRec == 2 ? "A" : "";
			
			//Processo.
			Abb40 abb40 = fbg01.fbg01processo;
			def numProc = abb40 == null || abb40.abb40num == "0" ? "" : String.format("%011d", abb40.abb40num);
			def anoProc = abb40 == null || abb40.abb40dtI == null ? "" : String.format("%04d", abb40.abb40dtI.getYear());
			def varaProc = abb40 == null || abb40.abb40vara == null || abb40.abb40vara == "0" ? "" : String.format("%05d", abb40.abb40vara);
			
			//Período do processo.
			def perProcInicial = abb40.abb40dtI != null ? abb40.abb40dtI.format(DateTimeFormatter.ofPattern("yyyyMM")) : StringUtils.space(6);
			def perProcFinal = abb40.abb40dtF != null ? abb40.abb40dtF.format(DateTimeFormatter.ofPattern("yyyyMM")) : StringUtils.space(6);
			
			//Período de compensação.
			def perCompInicial = fbg01.fbg01compDti != null ? fbg01.fbg01compDti.format(DateTimeFormatter.ofPattern("yyyyMM")) : StringUtils.space(6);
			def perCompFinal = fbg01.fbg01compDtf != null ? fbg01.fbg01compDtf.format(DateTimeFormatter.ofPattern("yyyyMM")) : StringUtils.space(6);
			
			txt.print(12, 2);																																						//001 a 002
			txt.print("1", 1);																																					//003 a 003
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																								//004 a 017
			txt.print(0, 36, '0', true);																					   		 												//018 a 053
			txt.print(StringUtils.extractNumbers(nb.format(ded13SalMatern)), 15, '0', true);																			//054 a 068
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01recEveDesp)), 15, '0', true);																			//069 a 083
			txt.print(!sfp8501Dto.competencia13Sal ? fbg01origemRec : "", 1);																									//084 a 084
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01prodPF)), 15, '0', true);																				//085 a 099
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01prodPJ)), 15, '0', true);																				//100 a 114
			txt.print(numProc, 11);																																				//115 a 125
			txt.print(anoProc, 4);																																				//126 a 129
			txt.print(varaProc, 5);																																				//130 a 134
			txt.print(perProcInicial, 6);																																			//135 a 140
			txt.print(perProcFinal, 6);																																			//141 a 146
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01compVlr)), 15, '0', true);																				//147 a 161
			txt.print(perCompInicial, 6);																																			//162 a 167
			txt.print(perCompFinal, 6);																																			//168 a 173
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01caInss)), 15, '0', true);																				//174 a 188
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01caEnt)), 15, '0', true);																				//189 a 203
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01caCom)), 15, '0', true);																				//204 a 218
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01caComEnt)), 15, '0', true);																			//219 a 233
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01caEveDesp)), 15, '0', true);																			//234 a 248
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01parcFGTS1)), 15, '0', true);																			//249 a 263
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01parcFGTS2)), 15, '0', true);																			//264 a 278
			txt.print(StringUtils.extractNumbers(nb.format(fbg01.fbg01parcFGTS3)), 15, '0', true);																			//279 a 293
			txt.print(0, 15, '0', true);																																			//294 a 308
			txt.print(0, 45, '0', true);																																			//309 a 353
			txt.print(StringUtils.space(6));																																					//354 a 359
			txt.print("*", 1);																																					//360 a 360
			txt.newLine();
		}
	}
	
	/**
	 * TIPO 13
	 * Alteração cadastral do trabalhador
	 */
	private void gerarTipo13(TextFile txt, SFP8501Dto sfp8501Dto, Aac10 aac10, Long aac01id) {
		boolean isCodRec = isCodigoValido(["130", "135", "150", "155", "317", "337", "608"], String.valueOf(sfp8501Dto.codigoRec));
		if(!sfp8501Dto.competencia13Sal && !isCodRec) {
			List<Fba02> fba02s = buscarDadosDoHistoricoDoCalculo(aac01id, sfp8501Dto.mesAnoRef.getMonthValue(), sfp8501Dto.mesAnoRef.getYear(), sfp8501Dto.tiposAbh80);
			if(fba02s != null && fba02s.size() > 0) {
				Set<Long> idsAbh80 = new HashSet<Long>();
				
				for(Fba02 fba02 : fba02s) {
					Abh80 abh80 = fba02.fba02trab;
					if(idsAbh80.contains(abh80.abh80id)) continue;
					
					List<String> codsAlteracao = new ArrayList<String>();
					List<String> camposAlterados = new ArrayList<String>();

					def CTPSNum = fba02.fba02ctpsNum != null ? fba02.fba02ctpsNum : "";
					def CTPSSerie = fba02.fba02ctpsSerie != null ? fba02.fba02ctpsSerie : "";
					def nome = fba02.fba02nome != null ? fba02.fba02nome : "";
					def nit = fba02.fba02pis != null ? fba02.fba02pis : "";
					def matr = fba02.fba02codigo;
					def dtAdmis = fba02.fba02dtAdmis;
					def CBO = fba02.fba02cbo != null ? fba02.fba02cbo : "";
					def dtNasc = fba02.fba02dtNasc;
					
					def CTPSNumAtual = abh80.abh80ctpsNum != null ? abh80.abh80ctpsNum : "";
					def CTPSSerieAtual = abh80.abh80ctpsSerie != null ? abh80.abh80ctpsSerie : "";
					def nomeAtual = abh80.abh80nome != null ? abh80.abh80nome : "";
					def nitAtual = abh80.abh80pis != null ? abh80.abh80pis : "";
					def matrAtual = abh80.abh80codigo;
					def dtAdmisAtual = abh80.abh80dtAdmis;
					def CBOAtual = abh80.abh80cargo != null && abh80.abh80cargo.abh05cbo != null ? abh80.abh80cargo.abh05cbo.aap03codigo : "";
					def dtNascAtual = abh80.abh80nascData;
					def categ = abh80.abh80categ != null ? abh80.abh80categ.aap14sefip : "";

					if(!(CTPSNumAtual + CTPSSerieAtual).equals(CTPSNum + CTPSSerie)) {
						codsAlteracao.add("403");
						camposAlterados.add(StringUtils.extractNumbers(CTPSNumAtual + CTPSSerieAtual));
					}
					if(!nomeAtual.equals(nome)) {
						codsAlteracao.add("404");
						camposAlterados.add(nomeAtual);
					}
					if(!nitAtual.equals(nit)) {
						codsAlteracao.add("405");
						camposAlterados.add(StringUtils.extractNumbers(nitAtual));
					}
					if(!matrAtual.equals(matr)) {
						codsAlteracao.add("406");
						camposAlterados.add(matrAtual);
					}
					if(DateUtils.dateDiff(dtAdmisAtual, dtAdmis, ChronoUnit.DAYS) != 0) {
						codsAlteracao.add("408");
						camposAlterados.add(dtAdmisAtual.format(DateTimeFormatter.ofPattern("ddMMyyyy")));
					}
					if(!CBOAtual.equals(CBO)) {
						codsAlteracao.add("427");

						if(CBOAtual != null && CBOAtual.length() >= 5) {
							CBOAtual = CBOAtual.substring(0, 4);
							CBOAtual = "0" + CBOAtual;
						}
						camposAlterados.add(CBOAtual);
					}
					if(DateUtils.dateDiff(dtNascAtual, dtNasc, ChronoUnit.DAYS) != 0) {
						codsAlteracao.add("428");
						camposAlterados.add(dtNascAtual.format(DateTimeFormatter.ofPattern("ddMMyyyy")));
					}

					if(camposAlterados != null && camposAlterados.size() > 0) {
						int tam = camposAlterados.size() >= 2 ? 2 : 1;
						for(int j = 0; j < tam; j++) {
							/**
							 * TIPO 13
							 */
							txt.print(13, 2);																																		//001 a 002
							txt.print("1", 1);																																	//003 a 003
							txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																				//004 a 017
							txt.print(0, 36, '0', true);																															//018 a 053
							txt.print(StringUtils.extractNumbers(nit), 11);																										//054 a 064
							txt.print(dtAdmis.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																				//065 a 072
							txt.print(StringUtils.extractNumbers(categ), 2);																										//073 a 074
							txt.print(matr, 11, '0', true);																														//075 a 085
							txt.print(StringUtils.extractNumbers(CTPSNum), 7, CTPSNum == "" ? ' '  : '0', true);																//086 a 092
							txt.print(StringUtils.extractNumbers(CTPSSerie), 5, CTPSSerie == "" ? ' ' : '0', true);															//093 a 097
							txt.print(StringUtils.unaccented(nome), 70);																											//098 a 167
							txt.print(StringUtils.space(14));																																	//168 a 181
							txt.print(StringUtils.space(11));																																	//182 a 192
							txt.print(codsAlteracao.get(j), 3);																													//193 a 195
							txt.print(camposAlterados.get(j), 70);																												//196 a 265
							txt.print(StringUtils.space(94));																																	//266 a 359
							txt.print("*", 1);																																	//360 a 360
							txt.newLine();
						}
					}
					idsAbh80.add(abh80.abh80id);
				}
			}
		}
	}
	
	/**
	 * TIPO 14
	 * Inclusão/Alteração do endereço do trabalhador
	 */
	private void gerarTipo14(TextFile txt, SFP8501Dto sfp8501Dto, Aac10 aac10, Long aac01id) {
		boolean isCodRec = isCodigoValido(["130", "135", "150", "155", "317", "337", "608"], String.valueOf(sfp8501Dto.codigoRec));
		if(!sfp8501Dto.competencia13Sal && !isCodRec) {
			List<Fba02> fba02s = buscarDadosDoHistoricoDoCalculo(aac01id, sfp8501Dto.mesAnoRef.getMonthValue(), sfp8501Dto.mesAnoRef.getYear(), sfp8501Dto.tiposAbh80);
			if(fba02s != null && fba02s.size() > 0) {
				Set<Long> idsAbh80 = new HashSet<Long>();
				
				for(Fba02 fba02 : fba02s) {
					Abh80 abh80 = fba02.fba02trab;
					if(idsAbh80.contains(abh80.abh80id)) continue;
					def sefip = abh80.abh80categ != null ? abh80.abh80categ.aap14sefip : null;
					if(abh80.abh80categ != null && !isCodigoValido(["01", "02", "03", "04", "05", "06", "07"], sefip)) continue;
					
					boolean alterouCad = false;
					def fba01end = fba02.fba02endereco;
					def abh80end = abh80.abh80endereco;
					def fba01bai = fba02.fba02bairro;
					def abh80bai = abh80.abh80bairro;
					def fba01cep = fba02.fba02cep;
					def abh80cep = abh80.abh80cep;
					def fba01mun = fba02.fba02municipio != null ? fba02.fba02municipio.aag0201nome : null;
					def abh80mun = abh80.abh80municipio != null ? abh80.abh80municipio.aag0201nome : null;
					def fba01uf = fba02.fba02municipio != null ? fba02.fba02municipio.aag0201uf.aag02uf : null;
					def abh80uf = abh80.abh80municipio != null ? abh80.abh80municipio.aag0201uf.aag02uf : null;

					if((fba01end == null && abh80end != null) || (fba01end != null && abh80end == null) || (fba01end != null && abh80end != null && !fba01end == abh80end)) alterouCad = true;
					if((fba01bai == null && abh80bai != null) || (fba01bai != null && abh80bai == null) || (fba01bai != null && abh80bai != null && !fba01bai == abh80bai)) alterouCad = true;
					if((fba01cep == null && abh80cep != null) || (fba01cep != null && abh80cep == null) || (fba01cep != null && abh80cep != null && !fba01cep == abh80cep)) alterouCad = true;
					if((fba01mun == null && abh80mun != null) || (fba01mun != null && abh80mun == null) || (fba01mun != null && abh80mun != null && !fba01mun == abh80mun)) alterouCad = true;
					if((fba01uf == null && abh80uf != null) || (fba01uf != null && abh80uf == null) || (fba01uf != null && abh80uf != null && !fba01uf == abh80uf)) alterouCad = true;

					if(((sfp8501Dto.getMesAnoRef().getMonthValue()-1) == abh80.getAbh80dtAdmis().getMonthValue() && sfp8501Dto.getMesAnoRef().getYear() == abh80.getAbh80dtAdmis().getYear()) || alterouCad) {
						def logradouro = StringUtils.unaccented(abh80.abh80endereco);
						if(abh80.abh80numero != null) logradouro = logradouro + " " + abh80.abh80numero;
						
						def CTPSNum = abh80.abh80ctpsNum != null ? abh80.abh80ctpsNum : "";
						def CTPSSerie = abh80.abh80ctpsSerie != null ? abh80.abh80ctpsSerie : "";

						/**
						 * TIPO 14
						 */
						txt.print(14, 2);																																			//001 a 002
						txt.print("1", 1);																																		//003 a 003
						txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																					//004 a 017
						txt.print(0, 36, '0', true);																																//018 a 053
						txt.print(StringUtils.extractNumbers(abh80.abh80pis), 11);																							//054 a 064
						txt.print(abh80.abh80dtAdmis.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																	//065 a 072
						txt.print(abh80.abh80categ != null ? StringUtils.extractNumbers(abh80.abh80categ.aap14sefip) : "", 2);										//073 a 074
						txt.print(StringUtils.unaccented(abh80.abh80nome), 70);																								//075 a 144
						txt.print(StringUtils.extractNumbers(CTPSNum), 7, CTPSNum == "" ? ' '  : '0', true);																	//145 a 151
						txt.print(StringUtils.extractNumbers(CTPSSerie), 5, CTPSSerie == "" ? ' '  : '0', true);																//152 a 156
						txt.print(retirarEspacoDuplo(logradouro), 50);																											//157 a 206
						txt.print(StringUtils.unaccented(abh80.abh80bairro), 20);																							//207 a 226
						txt.print(StringUtils.extractNumbers(abh80.abh80cep), 8);																							//227 a 234
						txt.print(abh80.abh80municipio != null ? StringUtils.unaccented(abh80.abh80municipio.aag0201nome) : "", 20);								//235 a 254
						txt.print(abh80.abh80municipio != null ? abh80.abh80municipio.aag0201uf.aag02uf : "", 2);												//255 a 256
						txt.print(StringUtils.space(103));																																		//257 a 359
						txt.print("*", 1);																																		//360 a 360
						txt.newLine();
					}
					idsAbh80.add(abh80.abh80id);
				}
			}
		}
	}
	
	/**
	 * TIPO 20
	 * Registro do tomador de serviço
	 */
	private void gerarTipo20(TextFile txt, SFP8501Dto sfp8501Dto, Aac10 aac10, Aac10 aac10Tomador, TableMap tmTomador, String codPgtoGPS, String sefipCAESF, int mesRef, int anoRef, boolean is608) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		//Dados do tomador.
		def tipoInscrTom = 0;
		def numInscrTom = "0";
		def nome = null;
		def endereco = null;
		def bairro = null;
		def cep = null;
		def cidade = null;
		def uf = null;
		
		if(is608) {
			tipoInscrTom = 1;
			numInscrTom = StringUtils.extractNumbers(aac10Tomador.aac10ni);
			nome = aac10Tomador.aac10rs != null && aac10Tomador.aac10rs.length() > 40 ? StringUtils.unaccented(aac10Tomador.aac10rs.substring(0, 40)) : StringUtils.unaccented(aac10Tomador.aac10rs);
			endereco = StringUtils.unaccented(aac10Tomador.aac10endereco) + aac10Tomador.aac10numero;
			bairro = StringUtils.unaccented(aac10Tomador.aac10bairro);
			cep = aac10Tomador.aac10cep;
			cidade = aac10Tomador.aac10municipio != null ? aac10Tomador.aac10municipio.aag0201nome : "";
			uf = aac10Tomador.aac10municipio != null ? aac10Tomador.aac10municipio.aag0201uf.aag02uf : "";
		}else {
			tipoInscrTom = tmTomador.getInteger("abh02ti");
			numInscrTom = StringUtils.extractNumbers(tmTomador.getString("abh02ni"));
			nome = StringUtils.unaccented(tmTomador.getString("abh02rs"));
			endereco = StringUtils.unaccented(tmTomador.getString("abh02endereco")) + tmTomador.getString("abh02numero");
			bairro = StringUtils.unaccented(tmTomador.getString("abh02bairro"));
			cep = StringUtils.extractNumbers(tmTomador.getString("abh02cep"));
			cidade = StringUtils.unaccented(tmTomador.getString("aag0201nome"));
			uf = StringUtils.unaccented(tmTomador.getString("aag02uf"));
		}
		
		//Código de pagamento GPS.
		boolean isCodRec = isCodigoValido(["130", "135", "155", "608"], String.valueOf(sfp8501Dto.codigoRec));
		def codGPS = (isCodRec && codPgtoGPS != null) ? codPgtoGPS : "";
		
		//Salário-família.
		isCodRec = isCodigoValido(["150", "155", "608"], String.valueOf(sfp8501Dto.codigoRec));
		def aac01id = buscarIdDoGrupoCentralizadorPorIdEmpresa(is608 ? aac10Tomador.aac10id : aac10.aac10id);
		
		Set<Long> idsAbh02 = new HashSet<>();
		if(tmTomador != null) idsAbh02.add(tmTomador.getLong("abh02id"));
		
		def salFamilia = (!sfp8501Dto.competencia13Sal && isCodRec) ? calcularCAE(aac01id, null, null, idsAbh02, sefipCAESF, mesRef, anoRef, sfp8501Dto.tiposFba0101) : BigDecimal.ZERO;
		
		//Valor da retenção e total de faturas.
		def valorRetenc = BigDecimal.ZERO;
		def valorFatura = BigDecimal.ZERO;
		
		if(!is608) {
			Fbg02 fbg02 = buscarInformacoesDoTomadorPorChaveUnica(anoRef, mesRef, tmTomador.getLong("abh02id"));
			if(fbg02 != null) {
				if(sfp8501Dto.codigoRec == "150" || sfp8501Dto.codigoRec == "155") valorRetenc = fbg02.fbg02ret;
				if(sfp8501Dto.codigoRec == "211") valorFatura = fbg02.fbg02fat;
			}
		}
		
		txt.print(20, 2);																																							//001 a 002
		txt.print("1", 1);																																						//003 a 003
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																									//004 a 017
		txt.print(tipoInscrTom, 1);																																				//018 a 018
		txt.print(numInscrTom, 14, '0', true);																																	//019 a 032
		txt.print(0, 21, '0', true);																																				//033 a 053
		txt.print(nome, 40);																																						//054 a 093
		txt.print(endereco, 50);																																					//094 a 143
		txt.print(bairro, 20);																																					//144 a 163
		txt.print(cep, 8);																																						//164 a 171
		txt.print(cidade, 20);																																					//172 a 191
		txt.print(uf, 2);																																							//192 a 193
		txt.print(codGPS, 4);																																						//194 a 197
		txt.print(StringUtils.extractNumbers(nb.format(salFamilia.setScale(2))), 15, '0', true);																					//198 a 212
		txt.print(0, 15, '0', true);																																				//213 a 227
		txt.print(0, 1, '0', true);																																				//228 a 228
		txt.print(0, 14, '0', true);																																				//229 a 242
		txt.print(StringUtils.extractNumbers(nb.format(valorRetenc.setScale(2))), 15, '0', true);																					//243 a 257
		txt.print(StringUtils.extractNumbers(nb.format(valorFatura.setScale(2))), 15, '0', true);																					//258 a 272
		txt.print(0, 45, '0', true);																																				//273 a 317
		txt.print(StringUtils.space(42));																																						//318 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
		
		gerarTipo21(txt, sfp8501Dto, tmTomador, mesRef, anoRef, 1, aac10.aac10ni, tipoInscrTom, numInscrTom, is608);
	}
	
	/**
	 * TIPO 21
	 * Registro de informações adicionais do tomador de serviço
	 */
	private void gerarTipo21(TextFile txt, SFP8501Dto sfp8501Dto, TableMap tmTomador, int mesRef, int anoRef, int tipoInscrEmp, String numInscrEmp, int tipoInscrTom, String numInscrTom, boolean is608) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		def compVlrCorrigido = BigDecimal.ZERO;
		def perCompInicial = null;
		def perCompFinal = null;
		def compVlrINSS = BigDecimal.ZERO;
		def compVlrEnt = BigDecimal.ZERO;
		def parcFGTS1 = BigDecimal.ZERO;
		def parcFGTS2 = BigDecimal.ZERO;
		def parcFGTS3 = BigDecimal.ZERO;
		
		if(!is608) {
			Fbg03 fbg03 = buscarInformacoesAdicionaisDoTomadorPorChaveUnica(anoRef, mesRef, tmTomador.getLong("abh02id"));
			if(fbg03 != null) {
				//Compensação - valor corrigido.
				boolean isCodRec = isCodigoValido(["317", "337"], String.valueOf(sfp8501Dto.codigoRec));
				if(!isCodRec || !sfp8501Dto.competencia13Sal) compVlrCorrigido = fbg03.fbg03compVlr;

				//Período de compensação.
				perCompInicial = fbg03.fbg03compDti != null ? fbg03.fbg03compDti.format(DateTimeFormatter.ofPattern("yyyyMM")) : StringUtils.space(6);
				perCompFinal = fbg03.fbg03compDtf != null ? fbg03.fbg03compDtf.format(DateTimeFormatter.ofPattern("yyyyMM")) : StringUtils.space(6);

				//Recolhimento de competências anteriores.
				isCodRec = isCodigoValido(["211", "317", "337"], String.valueOf(sfp8501Dto.codigoRec));
				if(!isCodRec) {
					compVlrINSS = fbg03.fbg03caInss;
					compVlrEnt = fbg03.fbg03caEnt;
				}

				//Parcelamento do FGTS.
				parcFGTS1 = fbg03.fbg03parcFGTS1;
				parcFGTS2 = fbg03.fbg03parcFGTS2;
				parcFGTS3 = fbg03.fbg03parcFGTS3;
			}
		}
		
		txt.print(21, 2);																																							//001 a 002
		txt.print(tipoInscrEmp, 1);																																				//003 a 003
		txt.print(StringUtils.extractNumbers(numInscrEmp), 14, '0', true);																										//004 a 017
		txt.print(tipoInscrTom, 1);																																				//018 a 018
		txt.print(StringUtils.extractNumbers(numInscrTom), 14, '0', true);																										//019 a 032
		txt.print(0, 21, '0', true);																																				//033 a 053
		txt.print(StringUtils.extractNumbers(nb.format(compVlrCorrigido.setScale(2))), 15, '0', true);																			//054 a 068
		txt.print(perCompInicial != null ? perCompInicial : "", 6);																												//069 a 074
		txt.print(perCompFinal != null ? perCompFinal : "", 6);																													//075 a 080
		txt.print(StringUtils.extractNumbers(nb.format(compVlrINSS.setScale(2))), 15, '0', true);																					//081 a 095
		txt.print(StringUtils.extractNumbers(nb.format(compVlrEnt.setScale(2))), 15, '0', true);																					//096 a 110
		txt.print(StringUtils.extractNumbers(nb.format(parcFGTS1)), 15, '0', true);																								//111 a 125
		txt.print(StringUtils.extractNumbers(nb.format(parcFGTS2)), 15, '0', true);																								//126 a 140
		txt.print(StringUtils.extractNumbers(nb.format(parcFGTS3)), 15, '0', true);																								//141 a 155
		txt.print(StringUtils.space(204));																																						//156 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	/**
	 * TIPO 30
	 * Registro do trabalhador
	 */
	private void gerarTipo30(TextFile txt, SFP8501Dto sfp8501Dto, Aac10 aac10, Aac10 aac10Tomador, Long aac01id, TableMap tmTomador, List<TableMap> tmsTrabalhadores, String sefipCAERem, String sefipCAERem13, String sefipCAEDesc, String sefipCAESCINSS, String sefipCAESC13COM, String sefipCAESC13GPS, String sefipCAESC13, boolean is608) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		for(TableMap tmTrab : tmsTrabalhadores) {
			//Dados do tomador.
			def tipoInscrTom = 0;
			def numInscrTom = "0";
			if(is608) {
				tipoInscrTom = 1;
				numInscrTom = StringUtils.extractNumbers(aac10Tomador.aac10ni);
			}else if(tmTomador != null && tmTomador.size() > 0) {
				tipoInscrTom = tmTomador.getInteger("abh02ti");
				numInscrTom = StringUtils.extractNumbers(tmTomador.getString("abh02ni"));
			}
			
			//PIS.
			Fba0101 fba0101 = buscarTrabalhadorPorIdParaTipo30(tmTrab.getLong("fba0101id"));
			Abh80 abh80 = fba0101.fba0101trab;

			def nit = abh80.abh80pis;

			//Data de admissão.
			def mesRef = sfp8501Dto.mesAnoRef.getMonthValue();
			def anoRef = sfp8501Dto.mesAnoRef.getYear();
			Fba02 fba02 = buscarHistoricoDoTrabalhadorParaTipo30(abh80.abh80id, aac01id, mesRef, anoRef);
			
			def categ = is608 ? sfp8501Dto.categoriaRec608 : fba02 != null && fba02.fba02categ != null ? fba02.fba02categ.aap14sefip : "";
			boolean isCateg = isCodigoValido(["01", "03", "04", "05", "06", "07", "11", "12", "19", "20", "21", "26"], categ);
			def dtAdmis = isCateg ? abh80.abh80dtAdmis.format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "";

			//CTPS.
			isCateg = isCodigoValido(["01", "03", "04", "06", "07", "26"], categ);
			def CTPSNum = isCateg && abh80.abh80ctpsNum != null ? abh80.abh80ctpsNum : "";
			def CTPSSerie = isCateg && abh80.abh80ctpsSerie != null ? abh80.abh80ctpsSerie : "";

			//Data de opção FGTS.
			isCateg = isCodigoValido(["01", "03", "04", "05", "06", "07"], categ);
			def dataOpFGTS = isCateg ? abh80.abh80dtAdmis.format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "";

			//Data de nascimento.
			isCateg = isCodigoValido(["01", "02", "03", "04", "05", "06", "07", "12", "19", "20", "21", "26"], categ);
			def dataNasc = isCateg ? abh80.abh80nascData.format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "";

			//CBO.
			def cbo = fba02 != null && fba02.fba02cbo != null ? fba02.fba02cbo : null;
			if(cbo != null && cbo.length() >= 5) {
				cbo = cbo.substring(0, 4);
				cbo = "0" + cbo;
			}
			
			//Remuneração sem 13º salário.
			def abh80id = abh80.abh80id;
			aac01id = buscarIdDoGrupoCentralizadorPorIdEmpresa(is608 ? aac10Tomador.aac10id : aac10.aac10id);

			List<Long> idsAbh02 = tmTomador != null ? Arrays.asList(tmTomador.getLong("abh02id")) : buscarIdsDaLotacaoPorValorDoCalculo(tmTrab.getLong("fba0101id"));

			def remSal = !sfp8501Dto.competencia13Sal ? calcularCAE(aac01id, abh80id, null, idsAbh02, sefipCAERem, mesRef, anoRef, sfp8501Dto.tiposFba0101) : BigDecimal.ZERO;

			//Remuneração 13º salário.
			def remSal13 = !sfp8501Dto.competencia13Sal ? calcularCAE(aac01id, abh80id, null, idsAbh02, sefipCAERem13, mesRef, anoRef, sfp8501Dto.tiposFba0101) : BigDecimal.ZERO;
			
			//Ocorrência.
			def oco = null;
			if(is608) {
				oco = sfp8501Dto.ocorrenciaRec608;
			}else {
				oco = buscarCodigoDaOcorrenciaGFIPParaTipo30(abh80id, idsAbh02);
			}
			if(oco == null || oco == "00") oco = "";

			//Valor descontado do segurado.
			def valorDesc = BigDecimal.ZERO;
			if(isCodigoValido(["05", "06", "07", "08"], oco) || isCodigoValido(["130", "135", "650"], String.valueOf(sfp8501Dto.codigoRec))) {
				valorDesc = calcularCAE(aac01id, abh80id, null, idsAbh02, sefipCAEDesc, mesRef, anoRef, sfp8501Dto.tiposFba0101);
			}
			
			//Base de cálculo do INSS.
			BigDecimal bCalcINSS = BigDecimal.ZERO;
			boolean isCodRec = isCodigoValido(["145", "307", "317", "327", "337", "345", "640", "660"], String.valueOf(sfp8501Dto.codigoRec));
			if(!sfp8501Dto.competencia13Sal && !isCodRec) {
				boolean isMovValido = false;
				
				List<TableMap> tmAfastamentos = buscarDadosAfastamentosParaTipo30(aac01id, abh80id, mesRef, anoRef, null);
				if(tmAfastamentos != null && tmAfastamentos.size() > 0) {
					for(TableMap tmAfast : tmAfastamentos) {
						if(isCodigoValido(["O1", "O2", "R", "Z2", "Z3", "Z4"], tmAfast.getString("fgtsma"))) isMovValido = true;
					}
				}
				if(isMovValido) bCalcINSS = calcularCAE(aac01id, abh80id, null, idsAbh02, sefipCAESCINSS, mesRef, anoRef, sfp8501Dto.tiposFba0101);
			}
			
			//Base de cálculo do INSS 13º salário.
			isCateg = isCodigoValido(["01", "02", "04", "06", "07", "12", "19", "20", "21", "26"], categ);
			def bCalcINSS13 = BigDecimal.ZERO;
			def bCalcINSS13GPS = BigDecimal.ZERO;
			
			if(!sfp8501Dto.competencia13Sal) {
				def vlrComp = calcularCAE(aac01id, abh80id, null, idsAbh02, sefipCAESC13COM, mesRef, anoRef, sfp8501Dto.tiposFba0101);
				if(isCateg && vlrComp > 0) {
					bCalcINSS13 = vlrComp;
					bCalcINSS13GPS = calcularCAE(aac01id, abh80id, null, idsAbh02, sefipCAESC13GPS, mesRef, anoRef, sfp8501Dto.tiposFba0101);
				}
			}else {
				bCalcINSS13 = isCateg ? calcularCAE(aac01id, abh80id, null, idsAbh02, sefipCAESC13, mesRef, anoRef, sfp8501Dto.tiposFba0101) : BigDecimal.ZERO;
				bCalcINSS13GPS = BigDecimal.ZERO;
			}
			
			txt.print(30, 2);																																						//001 a 002
			txt.print("1", 1);																																					//003 a 003
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);																								//004 a 017
			txt.print(tipoInscrTom != 0 ? tipoInscrTom : "", 1);																													//018 a 018
			txt.print(tipoInscrTom != 0 ? StringUtils.extractNumbers(numInscrTom) : "", 14);																						//019 a 032
			txt.print(StringUtils.extractNumbers(nit), 11);																														//033 a 043
			txt.print(dtAdmis, 8);																																				//044 a 051
			txt.print(categ, 2);																																					//052 a 053
			txt.print(StringUtils.unaccented(abh80.abh80nome), 70);			   																								//054 a 123
			txt.print(StringUtils.space(11));																																					//124 a 134
			txt.print(StringUtils.extractNumbers(CTPSNum), 7, CTPSNum == "" ? ' '  : '0', true);																				//135 a 141
			txt.print(StringUtils.extractNumbers(CTPSSerie), 5, CTPSSerie == "" ? ' '  : '0', true);																			//142 a 146
			txt.print(dataOpFGTS, 8);																																				//147 a 154
			txt.print(dataNasc, 8);																																				//155 a 162
			txt.print(cbo, 5);																																					//163 a 167
			txt.print(StringUtils.extractNumbers(nb.format(remSal.setScale(2))), 15, '0', true);																					//168 a 182
			txt.print(StringUtils.extractNumbers(nb.format(remSal13.setScale(2))), 15, '0', true);																				//183 a 197
			txt.print(StringUtils.space(2));																																					//198 a 199
			txt.print(oco, 2);																																					//200 a 201
			txt.print(StringUtils.extractNumbers(nb.format(valorDesc.setScale(2))), 15, '0', true);																				//202 a 216
			txt.print(StringUtils.extractNumbers(nb.format(bCalcINSS.setScale(2))), 15, '0', true);																				//217 a 231
			txt.print(sfp8501Dto.codigoRec == "327" ? BigDecimal.ZERO : StringUtils.extractNumbers(nb.format(bCalcINSS13.setScale(2))), 15, '0', true);		//232 a 246
			txt.print(StringUtils.extractNumbers(nb.format(bCalcINSS13GPS.setScale(2))), 15, '0', true);																			//247 a 261
			txt.print(StringUtils.space(98));																																					//262 a 359
			txt.print("*", 1);																																					//360 a 360
			txt.newLine();
			
			//Gera arquivos do tipo 32(se houver) para os trabalhadores.
			isCateg = isCodigoValido(["01","02", "03", "04", "05", "06", "07", "11", "12", "19", "20", "21", "26"], categ);
			if(!sfp8501Dto.competencia13Sal && isCateg) {
				def dataRef = LocalDate.of(anoRef, mesRef, 1);

				//Movimentação do tipo afastamentos.
				List<TableMap> tmAfastamentos = buscarDadosAfastamentosParaTipo30(aac01id, abh80id, mesRef, anoRef, dataRef.withDayOfMonth(dataRef.lengthOfMonth()));
				if(tmAfastamentos != null && tmAfastamentos.size() > 0) {
					for(TableMap tmAfast : tmAfastamentos) {
						def dataAfast = tmAfast.getDate("fbb01dtSai");
						if(mesRef == dataAfast.getMonthValue()) dataAfast = dataAfast.minusDays(1);
						
						def codMovim = null;
						if(isCodigoValido(["145", "307", "317", "327", "337", "345"], String.valueOf(sfp8501Dto.codigoRec))) {
							codMovim = "V3";
						}else {
							codMovim = tmAfast.getString("fgtsma");
						}
						def teste = codMovim != "V3"
						if(codMovim != "V3") {
							gerarTipo32(txt, "1", aac10.aac10ni, tipoInscrTom, numInscrTom, nit, dtAdmis, categ, abh80.abh80nome, codMovim, dataAfast.format(DateTimeFormatter.ofPattern("ddMMyyyy")), "");

							if(tmAfast.getDate("fbb01dtRet") != null) {
								def dataRet = tmAfast.getDate("fbb01dtRet");

								if(mesRef == dataRet.getMonthValue()) {
									//Se o afastamento for temporário (códigos: Z1, Z2, Z3, Z4, Z5 ou Z6) incluir novamente o registro de afastamento.
									if(dataAfast.getMonthValue() != dataRet.getMonthValue() && tmAfast.getString("fgtsma") != null && isCodigoValido(["Z1", "Z2", "Z3", "Z4", "Z5", "Z6"], tmAfast.getString("fgtsma"))) {
										gerarTipo32(txt, "1", aac10.aac10ni, tipoInscrTom, numInscrTom, nit, dtAdmis, categ, abh80.abh80nome, codMovim, dataAfast.format(DateTimeFormatter.ofPattern("ddMMyyyy")), "");
									}

									if(codMovim != "V3") codMovim = tmAfast.getString("fgtsmr");
									gerarTipo32(txt, "1", aac10.aac10ni, tipoInscrTom, numInscrTom, nit, dtAdmis, categ, abh80.abh80nome, codMovim, dataRet.format(DateTimeFormatter.ofPattern("ddMMyyyy")), "");
								}
							}
						}
					}
				}

				//Movimentação do tipo rescisão.
				Fbd10 fbd10 = buscarRescisaoParaTipo32(abh80.abh80id, mesRef, anoRef);
				if(fbd10 != null) {
					def codMovim = null;
					if(isCodigoValido(["145", "307", "317", "327", "337", "345"], String.valueOf(sfp8501Dto.codigoRec))) {
						codMovim = "V3";
					}else {
						codMovim = fbd10.fbd10causa.abh06sefip;
					}
					
					if(codMovim != null && codMovim != "V3") {
						def indRec = "";
						if(isCodigoValido(["L", "I1", "I2", "I3", "I4", "I5", "I6"], codMovim)) {
							indRec = fbd10.fbd10causa.abh06recFgts == 1 ? "S" : "N";
						}

						gerarTipo32(txt, "1", aac10.aac10ni, tipoInscrTom, numInscrTom, nit, dtAdmis, categ, abh80.abh80nome, codMovim, fbd10.fbd10dtRes.format(DateTimeFormatter.ofPattern("ddMMyyyy")), indRec);
					}
				}
			}
		}
	}
	
	/**
	 * TIPO 32
	 * Movimentação do trabalhador
	 */
	private void gerarTipo32(TextFile txt, String tipoInscrEmp, String numInscrEmp, int tipoInscrTom, String numInscrTom, String niTrab, String dtAdmis, String categ, String nomeTrab, String codMovim, String dtResAfast, String indRec) {
		/**
		 * TIPO 32
		 */
		txt.print(32, 2);																																							//001 a 002
		txt.print(tipoInscrEmp, 1);																																				//003 a 003
		txt.print(StringUtils.extractNumbers(numInscrEmp), 14, '0', true);																										//004 a 017
		txt.print(tipoInscrTom != 0 ? tipoInscrTom : "", 1);																														//018 a 018
		txt.print(tipoInscrTom != 0 ? StringUtils.extractNumbers(numInscrTom) : "", 14);																							//019 a 032
		txt.print(StringUtils.extractNumbers(niTrab), 11);																														//033 a 043
		txt.print(dtAdmis, 8);																																					//044 a 051
		txt.print(categ, 2);																																						//052 a 053
		txt.print(StringUtils.unaccented(nomeTrab), 70);																															//054 a 123
		txt.print(codMovim, 2);																																					//124 a 125
		txt.print(dtResAfast, 8);																																					//126 a 133
		txt.print(indRec, 1);																																						//134 a 134
		txt.print(StringUtils.space(225));																																						//135 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	/**
	 * TIPO 90
	 * Totalizador do arquivo
	 */
	private void gerarTipo90(TextFile txt) {
		txt.print(90, 2);										          																											//001 a 002
		txt.print("999999999999999999999999999999999999999999999999999", 51);																										//003 a 053
		txt.print(StringUtils.space(306));																																						//054 a 359
		txt.print("*", 1);																																						//360 a 360
		txt.newLine();
	}
	
	private boolean isCodigoValido(List<String> codigos, String codigo) {
		for(int i = 0; i < codigos.size(); i++) {
			if(codigos[i].equals(codigo)) return true;
		}
		return false;
	}
	
	private BigDecimal calcularCAE(Long aac01id, Long abh80id, Set<Long> idsAbb11, List<Long> idsAbh02, String abh20codigo, int mes, int ano, Set<Integer> tiposFba0101) {
		BigDecimal valor = BigDecimal.ZERO;

		List<TableMap> tms = buscarValoresDosEventosParaCalculoDoCAE(aac01id, abh80id, idsAbb11, idsAbh02, abh20codigo, mes, ano, tiposFba0101);
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
	
	private List<Aac10> buscarEmpresasParaSefip(ClientCriterion clientCriterionAac10) {
		return getSession().createCriteria(Aac10.class)
				.addFields("aac10id, aac10ni, aac10endereco, aac10numero, aac10bairro, aac10complem, aac10cep, aac10dddFone, aac10fone, aac10rs, aag0201id, aag0201nome, aag02id, aag02uf, aac10cnae")
				.addJoin(Joins.part("aac10municipio").partial(true).left(true))
				.addJoin(Joins.part("aac10municipio.aag0201uf").partial(true).left(true))
				.addWhere(ClientCriteriaConvert.convertCriterion(clientCriterionAac10))
				.setOrder("aac10ni")
				.getList(ColumnType.ENTITY);
	}
	
	private Long buscarIdDoGrupoCentralizadorPorIdEmpresa(Long aac10id) {
		return getSession().createCriteria(Aac1001.class)
				.addFields("aac1001gc")
				.addWhere(Criterions.eq("aac1001empresa", aac10id))
				.addWhere(Criterions.eq("aac1001tabela", "FB"))
				.get(ColumnType.LONG);
	}
	
	private List<TableMap> buscarValoresDosEventosParaCalculoDoCAE(Long aac01id, Long abh80id, Set<Long> idsAbb11, List<Long> idsAbh02, String abh20codigo, int mes, int ano, Set<Integer> tiposFba0101) {
		String whereAbh80 = abh80id != null ? "AND fba0101trab = :abh80id " : "";
		String whereAbb11 = idsAbb11 != null && idsAbb11.size() > 0 ? "AND fba01011depto IN (:idsAbb11) " : "";
		String whereAbh02 = idsAbh02 != null && idsAbh02.size() > 0 ? "AND fba01011lotacao IN (:idsAbh02) " : "";
		
		Query query = getSession().createQuery("SELECT abh2101cvr, fba01011valor ",
											   "FROM Fba01011 ",
											   "INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
											   "INNER JOIN Fba01 ON fba01id = fba0101calculo ",
											   "INNER JOIN Abh21 ON abh21id = fba01011eve ",
											   "INNER JOIN Abh2101 ON abh2101evento = abh21id ",
											   "INNER JOIN Abh20 ON abh20id = abh2101cae ",
											   "WHERE fba01gc = :aac01id ",
											   "AND DATE_PART('MONTH', fba0101dtCalc) = :mes ",
											   "AND DATE_PART('YEAR', fba0101dtCalc) = :ano ",
											   "AND fba0101tpVlr IN (:tiposFba0101) ",
											   "AND abh20codigo = :abh20codigo ",
											   whereAbh80, whereAbb11, whereAbh02);
		
		query.setParameter("aac01id", aac01id);
		if(abh80id != null) query.setParameter("abh80id", abh80id);
		if(idsAbb11 != null && idsAbb11.size() > 0) query.setParameter("idsAbb11", idsAbb11);
		if(idsAbh02 != null && idsAbh02.size() > 0) query.setParameter("idsAbh02", idsAbh02);
		query.setParameter("abh20codigo", abh20codigo);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		query.setParameter("tiposFba0101", tiposFba0101);
		return query.getListTableMap();
	}
	
	private Fbg01 buscarInformacoesAdicionaisDaEmpresa(int fbg01ano, int fbg01mes) {
		return getSession().createCriteria(Fbg01.class)
				.addWhere(Criterions.eq("fbg01ano", fbg01ano))
				.addWhere(Criterions.eq("fbg01mes", fbg01mes))
				.addWhere(getSamWhere().getCritPadrao(Fbg01.class))
				.get(ColumnType.ENTITY);
	}
	
	private List<TableMap> buscarDadosDeLotacoesTomadoras(Long aac01id, Integer abh02ti, Set<Integer> tiposAbh80, Set<Integer> tiposFba0101, LocalDate dataRefAdmis, int mes, int ano, Set<String> codsAap52) {
		Query query = getSession().createQuery("SELECT DISTINCT abh02id, abh02ti, abh02ni, abh02rs, abh02endereco, abh02numero, abh02bairro, abh02cep, aag0201nome, aag02uf ",
											   "FROM Fba01011 ",
											   "INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
											   "INNER JOIN Fba01 ON fba01id = fba0101calculo ",
											   "INNER JOIN Abh80 ON abh80id = fba0101trab ",
											   "INNER JOIN Abh02 ON abh02id = fba01011lotacao ",
											   "INNER JOIN Aap52 ON aap52id = abh02tipo ",
											   "LEFT JOIN Aag0201 ON aag0201id = abh02municipio ",
											   "LEFT JOIN Aag02 ON aag02id = aag0201uf ",
											   "WHERE fba01gc = :aac01id ",
											   "AND abh80tipo IN (:tiposAbh80) ",
											   "AND fba0101tpVlr IN (:tiposFba0101) ",
											   "AND abh80dtAdmis <= :dataRefAdmis ",
											   "AND DATE_PART('MONTH', fba0101dtCalc) = :mes ",
											   "AND DATE_PART('YEAR', fba0101dtCalc) = :ano ",
											   "AND aap52codigo IN (:codsAap52) ",
											   "AND abh02ti = :abh02ti ",
											   "ORDER BY abh02ti");
		
		query.setParameter("aac01id", aac01id);
		query.setParameter("abh02ti", abh02ti);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameter("tiposFba0101", tiposFba0101);
		query.setParameter("dataRefAdmis", dataRefAdmis);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		query.setParameter("codsAap52", codsAap52);
		return query.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDeTrabalhadoresTomadores(Long aac01id, Set<Integer> tiposAbh80, Set<Integer> tiposFba0101, LocalDate dataRefAdmis, int mes, int ano, Long abh02id) {
		Query query = getSession().createQuery("SELECT abh80.abh80id, abh80.abh80pis, MAX(fba0101.fba0101id) as fba0101id ",
											   "FROM Fba01011 fba01011 ",
											   "INNER JOIN Fba0101 fba0101 ON fba0101.fba0101id = fba01011.fba01011vlr ",
											   "INNER JOIN Fba01 fba01 ON fba01.fba01id = fba0101.fba0101calculo ",
											   "INNER JOIN Abh80 abh80 ON abh80.abh80id = fba0101.fba0101trab ",
											   "INNER JOIN Abh02 abh02 ON abh02.abh02id = abh80.abh80lotacao ",
											   "INNER JOIN Aap52 aap52 ON aap52.aap52id = abh02.abh02tipo ",
											   "WHERE fba01.fba01gc = :aac01id ",
											   "AND abh80.abh80tipo IN (:tiposAbh80) ",
											   "AND fba0101.fba0101tpVlr IN (:tiposFba0101) ",
											   "AND abh80.abh80dtAdmis <= :dataRefAdmis ",
											   "AND DATE_PART('MONTH', fba0101.fba0101dtCalc) = :mes ",
											   "AND DATE_PART('YEAR', fba0101.fba0101dtCalc) = :ano ",
											   "AND abh80.abh80pis IS NOT NULL ",
											   "AND abh02id = :abh02id ",
											   "GROUP BY abh80.abh80id, abh80.abh80pis ",
											   "ORDER BY abh80.abh80pis");
		
		query.setParameter("aac01id", aac01id);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameter("tiposFba0101", tiposFba0101);
		query.setParameter("dataRefAdmis", dataRefAdmis);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		query.setParameter("abh02id", abh02id);
		return query.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDeTrabalhadores(Long aac01id, Set<Integer> tiposAbh80, Set<Integer> tiposFba0101, LocalDate dataRefAdmis, int mes, int ano, List<Long> idsAbh02, String abh80eoCNPJ) {
		String whereAbh02 = idsAbh02 != null && idsAbh02.size() > 0 ? "AND abh02id NOT IN (:idsAbh02) " : "";
		String whereCNPJOrigem = abh80eoCNPJ != null ? "AND abh80eoCNPJ = :abh80eoCNPJ " : "";
		
		Query query = getSession().createQuery("SELECT abh80.abh80id, abh80.abh80pis, MAX(fba0101.fba0101id) as fba0101id ",
											   "FROM Fba01011 fba01011 ",
											   "INNER JOIN Fba0101 fba0101 ON fba0101.fba0101id = fba01011.fba01011vlr ",
											   "INNER JOIN Fba01 fba01 ON fba01.fba01id = fba0101.fba0101calculo ",
											   "INNER JOIN Abh80 abh80 ON abh80.abh80id = fba0101.fba0101trab ",
											   "INNER JOIN Abh02 abh02 ON abh02.abh02id = abh80.abh80lotacao ",
											   "INNER JOIN Aap52 aap52 ON aap52.aap52id = abh02.abh02tipo ",
											   "WHERE fba01.fba01gc = :aac01id ",
											   "AND abh80.abh80tipo IN (:tiposAbh80) ",
											   "AND fba0101.fba0101tpVlr IN (:tiposFba0101) ",
											   "AND abh80.abh80dtAdmis <= :dataRefAdmis ",
											   "AND DATE_PART('MONTH', fba0101.fba0101dtCalc) = :mes ",
											   "AND DATE_PART('YEAR', fba0101.fba0101dtCalc) = :ano ",
											   "AND abh80.abh80pis IS NOT NULL ",
											   whereAbh02, whereCNPJOrigem,
											   "GROUP BY abh80.abh80id, abh80.abh80pis ",
											   "ORDER BY abh80.abh80pis");
		
		query.setParameter("aac01id", aac01id);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameter("tiposFba0101", tiposFba0101);
		query.setParameter("dataRefAdmis", dataRefAdmis);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		if(idsAbh02 != null && idsAbh02.size() > 0) query.setParameter("idsAbh02", idsAbh02);
		if(abh80eoCNPJ != null) query.setParameter("abh80eoCNPJ", abh80eoCNPJ);
		return query.getListTableMap();
	}
	
	private Fbg02 buscarInformacoesDoTomadorPorChaveUnica(Integer fbg02ano, Integer fbg02mes, Long fbg02lotacao) {
		return getSession().createCriteria(Fbg02.class)
				.addWhere(Criterions.eq("fbg02ano", fbg02ano))
				.addWhere(Criterions.eq("fbg02mes", fbg02mes))
				.addWhere(Criterions.eq("fbg02lotacao", fbg02lotacao))
				.addWhere(getSamWhere().getCritPadrao(Fbg02.class))
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}
	
	private Fbg03 buscarInformacoesAdicionaisDoTomadorPorChaveUnica(Integer fbg03ano, Integer fbg03mes, Long fbg03lotacao) {
		return getSession().createCriteria(Fbg03.class)
				.addWhere(Criterions.eq("fbg03ano", fbg03ano))
				.addWhere(Criterions.eq("fbg03mes", fbg03mes))
				.addWhere(Criterions.eq("fbg03lotacao", fbg03lotacao))
				.addWhere(getSamWhere().getCritPadrao(Fbg03.class))
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}
	
	private List<Fba02> buscarDadosDoHistoricoDoCalculo(Long aac01id, int mes, int ano, Set<Integer> tiposAbh80) {
		return getSession().createCriteria(Fba02.class)
				.addJoin(Joins.fetch("fba02trab").alias("abh80"))
				.addJoin(Joins.fetch("abh80.abh80cargo").alias("abh05"))
				.addJoin(Joins.fetch("abh05.abh05cbo").left(true).alias("aap03"))
				.addJoin(Joins.fetch("fba02municipio").left(true).alias("aag0201"))
				.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).alias("aag02"))
				.addJoin(Joins.fetch("abh80.abh80municipio").left(true).alias("aag0201Trab"))
				.addJoin(Joins.fetch("aag0201Trab.aag0201uf").left(true).alias("aag02Trab"))
				.addJoin(Joins.fetch("abh80.abh80categ").left(true))
				.addWhere(Criterions.eq("abh80gc", aac01id))
				.addWhere(Criterions.eq("DATE_PART('YEAR', fba02dtCalc)", ano))
				.addWhere(Criterions.eq("DATE_PART('MONTH', fba02dtCalc)", mes))
				.addWhere(Criterions.in("abh80tipo", tiposAbh80))
				.getList(ColumnType.ENTITY);
	}
	
	private Fba0101 buscarTrabalhadorPorIdParaTipo30(Long fba0101id) {
		return getSession().createCriteria(Fba0101.class)
				.addFields("fba0101id, abh80id, abh80codigo, abh80nome, abh80pis, abh80dtAdmis, abh80ctpsNum, abh80ctpsSerie, abh80nascData")
				.addJoin(Joins.part("fba0101trab").partial(true))
				.addWhere(Criterions.eq("fba0101id", fba0101id))
				.get(ColumnType.ENTITY);
	}
	
	private Fba02 buscarHistoricoDoTrabalhadorParaTipo30(Long abh80id, Long aac01id, int mes, int ano) {
		return getSession().createCriteria(Fba02.class)
				.addFields("fba02id, fba02cbo, aap14id, aap14sefip")
				.addJoin(Joins.part("fba02categ").partial(true).left(true))
				.addWhere(Criterions.eq("fba02trab", abh80id))
				.addWhere(Criterions.eq("DATE_PART('YEAR', fba02dtCalc)", ano))
				.addWhere(Criterions.eq("DATE_PART('MONTH', fba02dtCalc)", mes))
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}
	
	private List<TableMap> buscarDadosAfastamentosParaTipo30(Long aac01id, Long abh80id, int mes, int ano, LocalDate dataRef) {
		String whereData = dataRef != null ? "OR (:dataRef BETWEEN fbb01dtSai AND fbb01dtRet) OR (fbb01dtRet IS NULL AND fbb01dtSai <= :dataRef) " : "";
		
		Query query = getSession().createQuery("SELECT fbb01dtSai, fbb01dtRet, abh07ma.abh07fgts as fgtsma, abh07mr.abh07fgts as fgtsmr ",
												"FROM Fbb01 ",
												"INNER JOIN Abh07 abh07ma ON abh07ma.abh07id = fbb01ma ",
												"LEFT JOIN Abh07 abh07mr ON abh07mr.abh07id = fbb01mr ",
												"WHERE fbb01gc = :aac01id ",
												"AND fbb01trab = :abh80id ",
												"AND abh07ma.abh07fgts IS NOT NULL ",
												"AND ((DATE_PART('MONTH', fbb01dtSai) = :mes AND DATE_PART('YEAR', fbb01dtSai) = :ano) OR (DATE_PART('MONTH', fbb01dtRet) = :mes AND DATE_PART('YEAR', fbb01dtRet) = :ano) ",
												whereData + ") ");
		
		query.setParameter("aac01id", aac01id);
		query.setParameter("abh80id", abh80id);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		if(dataRef != null) query.setParameter("dataRef", dataRef);
		return query.getListTableMap();
	}
	
	private String buscarCodigoDaOcorrenciaGFIPParaTipo30(Long abh80id, List<Long> abh02ids) {
		return getSession().createCriteria(Fab01.class)
				.addFields("aap04codigo")
				.addJoin(Joins.join("fab01gfip"))
				.addWhere(Criterions.eq("fab01trab", abh80id))
				.addWhere(Criterions.in("fab01lotacao", abh02ids))
				.addWhere(Criterions.isNotNull("aap04codigo"))
				.setMaxResults(1)
				.setOrder("fab01dtI DESC, fab01id DESC")
				.get(ColumnType.STRING);
	}
	
	private Fbd10 buscarRescisaoParaTipo32(Long abh80id, int mes, int ano) {
		return getSession().createQuery("SELECT * FROM Fbd10 AS fbd10 ",
										"INNER JOIN FETCH fbd10.fbd10causa AS abh06 ",
										"WHERE fbd10.fbd10trab = :abh80id ",
										"AND DATE_PART('MONTH', fbd10.fbd10dtRes) = :mes ",
										"AND DATE_PART('YEAR', fbd10.fbd10dtRes) = :ano ",
										getSamWhere().getWherePadrao("AND", Fbd10.class),
										" ORDER BY fbd10.fbd10dtRes")
						   .setParameter("abh80id", abh80id)
						   .setParameter("mes", mes)
						   .setParameter("ano", ano)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private List<Long> buscarIdsDaLotacaoPorValorDoCalculo(Long fba0101id) {
		return getSession().createCriteria(Fba01011.class)
				.addFields("DISTINCT fba01011lotacao")
				.addWhere(Criterions.eq("fba01011vlr", fba0101id))
				.getList(ColumnType.LONG);
	}
	
	private List<String> buscarCNPJsDasEmpresasOrigensParaTipo608(Long aac01id) {
		return getSession().createCriteria(Abh80.class)
				.addFields("DISTINCT abh80eoCNPJ")
				.addWhere(Criterions.eq("abh80gc", aac01id))
				.addWhere(Criterions.isNotNull("abh80eoCNPJ"))
				.setOrder("abh80eoCNPJ")
				.getList(ColumnType.STRING);
	}
	
	private Aac10 buscarEmpresaPorNI(String aac10ni) {
		return getSession().createCriteria(Aac10.class)
				.addJoin(Joins.fetch("aac10municipio"))
				.addJoin(Joins.fetch("aac10municipio.aag0201uf"))
				.addWhere(Criterions.eq("aac10ni", aac10ni))
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}
	
	private String retirarEspacoDuplo(String palavra) {
		if(palavra == null) return "";
		palavra = palavra.replaceAll("  ", " ");
		return palavra;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTIifQ==