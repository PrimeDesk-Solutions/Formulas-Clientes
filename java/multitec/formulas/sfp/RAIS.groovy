package multitec.formulas.sfp;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
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
import br.com.multitec.utils.functions.Optional;
import sam.core.criteria.ClientCriteriaConvert;
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFP8503Dto;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac1001;
import sam.model.entities.aa.Aac13;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abh02;
import sam.model.entities.ab.Abh20;
import sam.model.entities.ab.Abh2101;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba02;
import sam.model.entities.fb.Fbb01;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;

public class RAIS extends FormulaBase {
	private SFP8503Dto sfp8503Dto;

	private int sequencial = 1;
	private int totalRegTipo1 = 0;
	private int totalRegTipo2 = 0;

	/******************************** CAMPOS FIXOS DA FÓRMULA ***********************************/
	def raisProp = new BigDecimal(4);
	def raisDataBase = new BigDecimal(11);
	def raisPorte = new BigDecimal(2);
	def raisPat = new BigDecimal(1);
	def raisServProp = BigDecimal.ZERO;
	def raisAdmCoz = BigDecimal.ZERO;
	def raisRefConv = BigDecimal.ZERO;
	def raisRefTransp = new BigDecimal(100);
	def raisCesta = new BigDecimal(100);
	def raisAlimConv = BigDecimal.ZERO;
	def raisCSCentral = BigDecimal.ZERO;
	def raisCNPJCS = "";
	def raisFiliadaSind = BigDecimal.ZERO;
	/******************************** CAMPOS FIXOS DA FÓRMULA ***********************************/

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.RAIS;
	}

	@Override
	public void executar() {
		this.sfp8503Dto = get("sfp8503Dto");

		TextFile txt = new TextFile();
		if(sfp8503Dto.raisNormal) {
			gerarRAISNormal(txt, sfp8503Dto);
		}else {
			gerarRAISGenerico(txt, sfp8503Dto);
		}
		
		put("txt", txt);
	}

	/**
	 * Gera o RAIS normal.
	 * @param sb StringBuilder
	 * @param sfp8503Dto SFP8503Dto DTO
	 */
	private void gerarRAISNormal(TextFile txt, SFP8503Dto sfp8503Dto) {
		gerarTipo0Normal(txt, sfp8503Dto);
		gerarTipo1Normal(txt, sfp8503Dto);
		gerarTipo9Normal(txt, sfp8503Dto.cnpjUltimaEmpresa);
	}

	/**
	 * Gera o RAIS genérico.
	 * @param sb StringBuilder
	 * @param sfp8503Dto SFP8503Dto DTO
	 */
	private void gerarRAISGenerico(TextFile txt, SFP8503Dto sfp8503Dto) {
		gerarTipo0Generico(txt, sfp8503Dto);
		gerarTipo1Generico(txt, sfp8503Dto);
		gerarTipo9Generico(txt, sfp8503Dto.cnpjUltimaEmpresa);
	}

	/**
	 * TIPO 0 NORMAL
	 */
	private void gerarTipo0Normal(TextFile txt, SFP8503Dto sfp8503Dto) {
		txt.print(sequencial, 6, '0', true);                                                                      																//001 a 006
		txt.print(StringUtils.extractNumbers(sfp8503Dto.cnpjPrimeiraEmpresa), 14, '0', true);																				//007 a 020
		txt.print("00", 2);																																						//021 a 022
		txt.print(0, 1);																																							//023 a 023
		txt.print(1, 1);																			 																				//024 a 024
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respNI), 14, '0', true);																								//025 a 038
		txt.print(sfp8503Dto.respTI, 1);																																		//039 a 039
		txt.print(StringUtils.unaccented(sfp8503Dto.respRS), 40);			  																								//040 a 079
		txt.print(StringUtils.unaccented(sfp8503Dto.respEndereco), 40);																										//080 a 119
		txt.print(sfp8503Dto.getRespNumero(), 6, '0', true);																														//120 a 125
		txt.print(StringUtils.unaccented(sfp8503Dto.respComplem), 21);																										//126 a 146
		txt.print(StringUtils.unaccented(sfp8503Dto.respBairro), 19);																										//147 a 165
		txt.print(sfp8503Dto.respCEP, 8);																																	//166 a 173
		txt.print(sfp8503Dto.respMunicipioCod, 7, '0', true);																												//174 a 180
		txt.print(StringUtils.unaccented(sfp8503Dto.respMunicipio), 30);																										//181 a 210
		txt.print(sfp8503Dto.respUF, 2);																																		//211 a 212
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respDDD), 2, '0', true);																								//213 a 214
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respFone), 9, '0', true);																							//215 a 223
		txt.print(sfp8503Dto.raisRetificada ? 1 : 2, 1);																														//224 a 224
		txt.print(sfp8503Dto.dtRetificacao == null ? 0 : sfp8503Dto.dtRetificacao.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);						//225 a 232
		txt.print(sfp8503Dto.dtGeracao.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																					//233 a 240
		txt.print(StringUtils.unaccented(sfp8503Dto.respEmail), 45);																											//241 a 285
		txt.print(StringUtils.unaccented(sfp8503Dto.respNome), 52);																											//286 a 337
		txt.print(StringUtils.space(24));																																						//338 a 361
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respCPF), 11, '0', true);																							//362 a 372
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respCREA), 12, '0', true);																							//373 a 384
		txt.print(sfp8503Dto.respNasc == null ? 0 : sfp8503Dto.respNasc.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);									//385 a 392
		txt.print(StringUtils.space(159));															    																						//393 a 551
		txt.newLine();
		sequencial++;
	}

	/**
	 * TIPO 1 NORMAL
	 */
	private void gerarTipo1Normal(TextFile txt, SFP8503Dto sfp8503Dto) {
		Optional.of(buscarDadosDasEmpresasParaGeracaoDoRAIS(sfp8503Dto.clientCriterionAac10)).ifPresent ({ tmEmps ->
			NumberFormat nb = NumberFormat.getNumberInstance();
			nb.setMinimumFractionDigits(2);
			nb.setGroupingUsed(false);

			def dtInicial = LocalDate.of(sfp8503Dto.anoBase, Month.JANUARY, 1);
			def dtFinal = LocalDate.of(sfp8503Dto.anoBase, Month.DECEMBER, 31);

			def indParticipaPAT = raisPat == null ? 0 : raisPat.intValue() == 0 ? 2 : 1;
			def indCentralPgtoCS = raisCSCentral == null ? 0 : raisCSCentral == 0 ? 2 : 1;
			def indEmpFiliadaSind = raisFiliadaSind == null ? 0 : raisFiliadaSind == 0 ? 2 : 1;

			List<TableMap> tmAac10s = comporEmpresasELotacoesParaRAIS(tmEmps);
			for(int i = 0; i < tmAac10s.size(); i++) {
				def aac10id = tmAac10s.get(i).getLong("aac10id");
				def aac01id = tmAac10s.get(i).getLong("aac01id");
				Aac13 aac13 = buscarParametroFiscalParaRAIS(aac10id);

				def trabs5Sal = 0;
				def trabsAcima5Sal = 0;

				List<TableMap> tmSalarios = buscarDadosDoUltimoCalculoDosTrabalhadoresParaRAIS(aac01id, dtInicial, dtFinal, sfp8503Dto.tiposAbh80);
				if(tmSalarios != null && tmSalarios.size() > 0) {
					def salarioMinimo = buscarSalarioMinimo();
					def abh80codigo = null;

					for(int j = 0; j < tmSalarios.size(); j++) {
						if(abh80codigo == null || !abh80codigo == tmSalarios.get(j).getString("abh80codigo")) {
							BigDecimal salario = tmSalarios.get(j).getString("aap18codigo") == "5" ? tmSalarios.get(j).getBigDecimal("abh80salario") : tmSalarios.get(j).getBigDecimal("abh80salario") * tmSalarios.get(j).getInteger("abh80hs") * 5;
							if(salarioMinimo != null && salario <= (salarioMinimo * 5)) {
								trabs5Sal++;
							}else {
								trabsAcima5Sal++;
							}
							abh80codigo = tmSalarios.get(j).getString("abh80codigo");
						}
					}
				}

				TableMap tmContrPatronal = sfp8503Dto.tmContribPatr.stream().filter({tm -> tm.get("aac10codigo").equals(tmAac10s.get(i).getString("aac10codigo"))}).findFirst().orElse(null);

				txt.print(sequencial, 6, '0', true);                                                 																				//001 a 006
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10ni")), 14, '0', true);																				//007 a 020
				txt.print(StringUtils.ajustString(tmAac10s.get(i).getInteger("prefixoDoEstabelecimento"), 2, '0', true), 2);																//021 a 022
				txt.print(1, 1);																																					//023 a 023
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10rs")), 52);																								//024 a 075
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10endereco")), 40);																						//076 a 115
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10numero")), 6, '0', true);																			//116 a 121
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10complem")), 21);																							//122 a 142
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10bairro")), 19);																							//143 a 161
				txt.print(tmAac10s.get(i).getString("aac10cep"), 8);																														//162 a 169
				txt.print(tmAac10s.get(i).getString("aag0201ibge"), 7, '0', true);																										//170 a 176
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aag0201nome")), 30);																							//177 a 206
				txt.print(tmAac10s.get(i).getString("aag02uf"), 2);																														//207 a 208
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10dddFone")), 2, '0', true);																			//209 a 210
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10fone")), 9, '0', true);																				//211 a 219
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10email")), 45);																							//220 a 264
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10cnae")), 7, '0', true);																				//265 a 271
				txt.print(aac13 != null && aac13.aac13natJurid != null ? StringUtils.extractNumbers(aac13.aac13natJurid.aaj02eSocial) : "", 4, '0', true);			//272 a 275
				txt.print(raisProp == null ? 0 : raisProp.intValue(), 4, '0', true);																								//276 a 279
				txt.print(raisDataBase == null ? 0 : raisDataBase.intValue(), 2, '0', true); 																						//280 a 281
				txt.print("1", 1);																																				//282 a 282
				txt.print(existemTrabalhadoresNaEmpresa(aac01id, sfp8503Dto.anoBase, sfp8503Dto.tiposAbh80) ? 0 : 1, 1);												//283 a 283
				txt.print("00", 2);																																				//284 a 285
				txt.print(tmAac10s.get(i).getString("abh02ni") != null ? StringUtils.extractNumbers(tmAac10s.get(i).getString("abh02ni")) : 0, 12, '0', true);    								//286 a 297
				txt.print(sfp8503Dto.anoBase, 4);																															//298 a 301
				txt.print(raisPorte == null ? 0 : raisPorte.intValue(), 1);																										//302 a 302
				txt.print(aac13 != null && aac13.aac13classTrib() != null ? StringUtils.extractNumbers(aac13.aac13classTrib.aaj01eSocial) : "", 1);					//303 a 303
				txt.print(indParticipaPAT, 1);																																	//304 a 304
				txt.print(trabs5Sal, 6, '0', true);																																//305 a 310
				txt.print(trabsAcima5Sal, 6, '0', true);																															//311 a 316
				txt.print(raisServProp != null ? StringUtils.extractNumbers(nb.format(raisServProp)) : 0, 3, '0', true);												//317 a 319
				txt.print(raisAdmCoz != null ? StringUtils.extractNumbers(nb.format(raisAdmCoz)) : 0, 3, '0', true);													//320 a 322
				txt.print(raisRefConv != null ? StringUtils.extractNumbers(nb.format(raisRefConv)) : 0, 3, '0', true);												//323 a 325
				txt.print(raisRefTransp != null ? StringUtils.extractNumbers(nb.format(raisRefTransp)) : 0, 3, '0', true);											//326 a 328
				txt.print(raisCesta != null ? StringUtils.extractNumbers(nb.format(raisCesta)) : 0, 3, '0', true);													//329 a 331
				txt.print(raisAlimConv != null ? StringUtils.extractNumbers(nb.format(raisAlimConv)) : 0, 3, '0', true);												//332 a 334
				txt.print(tmAac10s.get(i).getDate("aac10dtEnc") != null ? 1 : 2, 1);																										//335 a 335
				txt.print(tmAac10s.get(i).getDate("aac10dtEnc") == null ? 0 : tmAac10s.get(i).getDate("aac10dtEnc").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);				//336 a 343
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(tmContrPatronal.getString("assocCNPJ")) : "", 14, '0', true);										//344 a 357
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(nb.format(tmContrPatronal.getBigDecimal("assocValor").setScale(2))) : "", 9, '0', true);			//358 a 366
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(tmContrPatronal.getString("sindCNPJ")) : "", 14, '0', true);										//367 a 380
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(nb.format(tmContrPatronal.getBigDecimal("sindValor").setScale(2))) : "", 9, '0', true);			//381 a 389
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(tmContrPatronal.getString("assistCNPJ")) : "", 14, '0', true);										//390 a 403
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(nb.format(tmContrPatronal.getBigDecimal("assistValor").setScale(2))) : "", 9, '0', true);			//404 a 412
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(tmContrPatronal.getString("confCNPJ")) : "", 14, '0', true);										//413 a 426
				txt.print(tmContrPatronal != null ? StringUtils.extractNumbers(nb.format(tmContrPatronal.getBigDecimal("confValor").setScale(2))) : "", 9, '0', true);			//427 a 435
				txt.print(1, 1);																																					//436 a 436
				txt.print(indCentralPgtoCS, 1);																																	//437 a 437
				txt.print(raisCNPJCS == null ? 0 : StringUtils.extractNumbers(raisCNPJCS), 14, '0', true);																		//438 a 451
				txt.print(indEmpFiliadaSind, 1);																																	//452 a 452
				txt.print(sfp8503Dto.tpSisPonto, 2);																															//453 a 454
				txt.print(StringUtils.space(85));																																				//455 a 539
				txt.print(StringUtils.space(12));																																				//540 a 551
				txt.newLine();
				sequencial++;

				gerarTipo2Normal(txt, sfp8503Dto, aac01id, tmAac10s.get(i).getLong("abh02id"), tmAac10s.get(i).getString("aac10ni"), dtInicial, dtFinal, tmAac10s.get(i).getInteger("prefixoDoEstabelecimento"));
				totalRegTipo1++;
			}
		});
	}

	/**
	 * TIPO 2 NORMAL
	 */
	private void gerarTipo2Normal(TextFile txt, SFP8503Dto sfp8503Dto, Long aac01id, Long abh02id, String aac10ni, LocalDate dtInicial, LocalDate dtFinal, Integer prefixoDoEstabelecimento) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);

		List<TableMap> tmAbh80 = buscarDadosDosTrabalhadoresParaRAIS(aac01id, abh02id, sfp8503Dto.anoBase, sfp8503Dto.tiposAbh80);
		for(int i = 0; i < tmAbh80.size(); i++) {
			def abh80id = tmAbh80.get(i).getLong("abh80id");
			def codigoDaRescisao = "";
			def dataDaProjecao = "";
			def avisoPrevioIndenizado = BigDecimal.ZERO;
			def feriasIndenizadas = BigDecimal.ZERO;
			def multaRescisao = BigDecimal.ZERO;

			Fbd10 fbd10 = buscarRescisaoDoTrabalhadorParaRAIS(abh80id, sfp8503Dto.anoBase);
			if(tmAbh80.get(i).getInteger("abh80sit") != Abh80.SIT_ATIVO && fbd10 == null) return;

			if(fbd10 != null) {
				codigoDaRescisao = fbd10.fbd10causa.abh06rais;
				dataDaProjecao = fbd10.fbd10apDtProj().format(DateTimeFormatter.ofPattern("ddMM"));
				avisoPrevioIndenizado = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20AvPrevInd, 0, fbd10.fbd10dtRes.getYear, false);
				feriasIndenizadas = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20FeriasInd, 0, fbd10.fbd10dtRes.getYear, false);
				multaRescisao = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20MultaFGTS, 0, fbd10.fbd10dtRes.getYear, false);
			}

			//Obtêm as remunerações dos meses a partir do CAE da tela "Remuneração mensal" (se houver).
			def remJaneiro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 1, sfp8503Dto.anoBase, true);
			def remFevereiro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 2, sfp8503Dto.anoBase, true);
			def remMarco = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 3, sfp8503Dto.anoBase, true);
			def remAbril = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 4, sfp8503Dto.anoBase, true);
			def remMaio = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 5, sfp8503Dto.anoBase, true);
			def remJunho = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 6, sfp8503Dto.anoBase, true);
			def remJulho = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 7, sfp8503Dto.anoBase, true);
			def remAgosto = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 8, sfp8503Dto.anoBase, true);
			def remSetembro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 9, sfp8503Dto.anoBase, true);
			def remOutubro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 10, sfp8503Dto.anoBase, true);
			def remNovembro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 11, sfp8503Dto.anoBase, true);
			def remDezembro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 12, sfp8503Dto.anoBase, true);

			//13º salário.
			def mesPag13Parc1 = buscarMesDePagamentoDo13SalarioParaRAIS(aac01id, abh80id, sfp8503Dto.abh2013Sal1, sfp8503Dto.anoBase);
			def mesPag13Parc2 = buscarMesDePagamentoDo13SalarioParaRAIS(aac01id, abh80id, sfp8503Dto.abh2013Sal2, sfp8503Dto.anoBase);

			//Remuneração do 13º salário.
			def sal13Parc1 = calcularCAE(aac01id, abh80id, sfp8503Dto.abh2013Sal1, 0, sfp8503Dto.anoBase, false);
			def sal13Parc2 = calcularCAE(aac01id, abh80id, sfp8503Dto.abh2013Sal2, 0, sfp8503Dto.anoBase, false);

			//Dados de deficiência do trabalhador.
			List<Integer> deficiente = new ArrayList<Integer>();
			if(tmAbh80.get(i).getInteger("abh80defFisico") == 1) deficiente.add(1);
			if(tmAbh80.get(i).getInteger("abh80defVisual") == 1) deficiente.add(3);
			if(tmAbh80.get(i).getInteger("abh80defAuditivo") == 1) deficiente.add(2);
			if(tmAbh80.get(i).getInteger("abh80defMental") == 1) deficiente.add(4);
			if(tmAbh80.get(i).getInteger("abh80defIntelecto") == 1) deficiente.add(4);
			if(tmAbh80.get(i).getInteger("abh80defReabil") == 1) deficiente.add(6);
			if(deficiente.size() == 0) deficiente.add(0);

			//Afastamentos do trabalhador.
			Map<String, Object> mapAfast = calcularAfastamentos(aac01id, abh80id, sfp8503Dto.anoBase);
			def afast1 = mapAfast.get("afast1");
			def dtSai1 = mapAfast.get("dtSai1");
			def dtRet1 = mapAfast.get("dtRet1");
			def afast2 = mapAfast.get("afast2");
			def dtSai2 = mapAfast.get("dtSai2");
			def dtRet2 = mapAfast.get("dtRet2");
			def afast3 = mapAfast.get("afast3");
			def dtSai3 = mapAfast.get("dtSai3");
			def dtRet3 = mapAfast.get("dtRet3");
			def diasAfast = mapAfast.get("diasAfast");

			//Contribuições do trabalhador.
			def contribAss1 = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20ContribAssoc1, 0, sfp8503Dto.anoBase, false);
			def contribAss2 = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20ContribAssoc2, 0, sfp8503Dto.anoBase, false);
			def contribSind = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20ContribSind, 0, sfp8503Dto.anoBase, false);
			def contribAssist = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20ContribAssist, 0, sfp8503Dto.anoBase, false);
			def contribConfed = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20ContribConf, 0, sfp8503Dto.anoBase, false);

			//Horas extras do trabalhador.
			def horExtrasJaneiro = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 1, sfp8503Dto.anoBase);
			def horExtrasFevereiro = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 2, sfp8503Dto.anoBase);
			def horExtrasMarco = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 3, sfp8503Dto.anoBase);
			def horExtrasAbril = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 4, sfp8503Dto.anoBase);
			def horExtrasMaio = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 5, sfp8503Dto.anoBase);
			def horExtrasJunho = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 6, sfp8503Dto.anoBase);
			def horExtrasJulho = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 7, sfp8503Dto.anoBase);
			def horExtrasAgosto = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 8, sfp8503Dto.anoBase);
			def horExtrasSetembro = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 9, sfp8503Dto.anoBase);
			def horExtrasOutubro = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 10, sfp8503Dto.anoBase);
			def horExtrasNovembro = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 11, sfp8503Dto.anoBase);
			def horExtrasDezembro = buscarHorasExtrasMensaisParaRAIS(aac01id, abh80id, sfp8503Dto.getAbh20HorExtras(), 12, sfp8503Dto.anoBase);

			txt.print(sequencial, 6, '0', true); 																																	//001 a 006
			txt.print(StringUtils.extractNumbers(aac10ni), 14, '0', true);																										//007 a 020
			txt.print(StringUtils.ajustString(prefixoDoEstabelecimento, 2, '0', true), 2);																						//021 a 022
			txt.print(2, 1);																																						//023 a 023
			txt.print(StringUtils.extractNumbers(tmAbh80.get(i).getString("abh80pis")), 11, '0', true);																					//024 a 034
			txt.print(StringUtils.unaccented(tmAbh80.get(i).getString("abh80nome")), 52);																								//035 a 086
			txt.print(tmAbh80.get(i).getDate("abh80nascData") == null ? 0 : tmAbh80.get(i).getDate("abh80nascData").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);				//087 a 094
			txt.print(tmAbh80.get(i).getString("aap05codigo"), 2, '0', true);																											//095 a 096
			txt.print(tmAbh80.get(i).getDate("abh80dtChegBr") == null ? 0 : tmAbh80.get(i).getDate("abh80dtChegBr").format(DateTimeFormatter.ofPattern("yyyy")), 4, '0', true);					//097 a 100
			txt.print(tmAbh80.get(i).getString("aap06codigo"), 2, '0', true);																											//101 a 102
			txt.print(tmAbh80.get(i).getString("abh80cpf"), 11, '0', true);																												//103 a 113
			txt.print(tmAbh80.get(i).getString("abh80ctpsNum"), 8, '0', true);																											//114 a 121
			txt.print(tmAbh80.get(i).getString("abh80ctpsSerie"), 5, '0', true);																											//122 a 126
			txt.print(tmAbh80.get(i).getDate("abh80dtAdmis") == null ? 0 : tmAbh80.get(i).getDate("abh80dtAdmis").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);			  	//127 a 134
			txt.print(tmAbh80.get(i).getString("aap16rais"), 2, '0', true);																												//135 a 136
			txt.print(StringUtils.extractNumbers(nb.format(buscarSalarioContratual(abh80id, dtInicial, dtFinal))), 9, '0', true);													//137 a 145
			txt.print(tmAbh80.get(i).getString("aap18rais"), 1, '0', true);																												//146 a 146
			txt.print(tmAbh80.get(i).getInteger("abh80hs"), 2, '0', true);																												//147 a 148
			txt.print(tmAbh80.get(i).getString("aap03codigo"), 6, '0', true);																											//149 a 154
			txt.print(tmAbh80.get(i).getString("aap17codigo"), 2, '0', true);																											//155 a 156
			txt.print(codigoDaRescisao, 2, '0', true);			                       																							//157 a 158
			txt.print(dataDaProjecao, 4, '0', true);																																//159 a 162
			txt.print(StringUtils.extractNumbers(nb.format(remJaneiro.setScale(2))), 9, '0', true);																				//163 a 171
			txt.print(StringUtils.extractNumbers(nb.format(remFevereiro.setScale(2))), 9, '0', true);			   																	//172 a 180
			txt.print(StringUtils.extractNumbers(nb.format(remMarco.setScale(2))), 9, '0', true);																					//181 a 189
			txt.print(StringUtils.extractNumbers(nb.format(remAbril.setScale(2))), 9, '0', true);																					//190 a 198
			txt.print(StringUtils.extractNumbers(nb.format(remMaio.setScale(2))), 9, '0', true);																					//199 a 207
			txt.print(StringUtils.extractNumbers(nb.format(remJunho.setScale(2))), 9, '0', true);																					//208 a 216
			txt.print(StringUtils.extractNumbers(nb.format(remJulho.setScale(2))), 9, '0', true);																					//217 a 225
			txt.print(StringUtils.extractNumbers(nb.format(remAgosto.setScale(2))), 9, '0', true);																				//226 a 234
			txt.print(StringUtils.extractNumbers(nb.format(remSetembro.setScale(2))), 9, '0', true);																				//235 a 243
			txt.print(StringUtils.extractNumbers(nb.format(remOutubro.setScale(2))), 9, '0', true);																				//244 a 252
			txt.print(StringUtils.extractNumbers(nb.format(remNovembro.setScale(2))), 9, '0', true);			 																	//253 a 261
			txt.print(StringUtils.extractNumbers(nb.format(remDezembro.setScale(2))), 9, '0', true);																				//262 a 270
			txt.print(StringUtils.extractNumbers(nb.format(sal13Parc1.setScale(2))), 9, '0', true);					 															//271 a 279
			txt.print(mesPag13Parc1, 2, '0', true);																																//280 a 281
			txt.print(StringUtils.extractNumbers(nb.format(sal13Parc2.setScale(2))), 9, '0', true);																				//282 a 290
			txt.print(mesPag13Parc2, 2, '0', true);																																//291 a 292
			txt.print(tmAbh80.get(i).getString("aap07codigo"), 1, '0', true);																											//293 a 293
			txt.print(deficiente.get(0) == 0 ? 2 : 1 , 1);				    																									//294 a 294
			txt.print(deficiente.size() > 1 ? "5" : deficiente.get(0), 1, '0', true);																								//295 a 295
			txt.print(tmAbh80.get(i).getInteger("abh80alvara") == 1 ? 1 : 2, 1);																											//296 a 296
			txt.print(StringUtils.extractNumbers(nb.format(avisoPrevioIndenizado.setScale(2))), 9, '0', true);																	//297 a 305
			txt.print(tmAbh80.get(i).getInteger("abh80sexo") == 0 ? 1 : 2, 1);																											//306 a 306
			txt.print(afast1 != null ? afast1 : 0, 2, '0', true);																													//307 a 308
			txt.print(dtSai1 == null ? 0 : dtSai1.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//309 a 312
			txt.print(dtRet1 == null ? 0 : dtRet1.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//313 a 316
			txt.print(afast2 != null ? afast2 : 0, 2, '0', true);																													//317 a 318
			txt.print(dtSai2 == null ? 0 : dtSai2.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//319 a 322
			txt.print(dtRet2 == null ? 0 : dtRet2.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//323 a 326
			txt.print(afast3 != null ? afast3 : 0, 2, '0', true);																													//327 a 328
			txt.print(dtSai3 == null ? 0 : dtSai3.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//329 a 332
			txt.print(dtRet3 == null ? 0 : dtRet3.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//333 a 336
			txt.print(diasAfast, 3, '0', true);																																	//337 a 339
			txt.print(StringUtils.extractNumbers(nb.format(feriasIndenizadas.setScale(2))), 8, '0', true);																		//340 a 347
			txt.print(0, 8, '0', true);																																			//348 a 355
			txt.print(0, 2, '0', true);																																			//356 a 357
			txt.print(0, 8, '0', true);																																			//358 a 365
			txt.print(0, 2, '0', true);																																			//366 a 367
			txt.print(0, 8, '0', true);																																			//368 a 375
			txt.print(0, 2, '0', true);																																			//376 a 377
			txt.print(StringUtils.extractNumbers(nb.format(multaRescisao.setScale(2))), 8, '0', true);																			//378 a 385
			txt.print(StringUtils.extractNumbers(tmAbh80.get(i).getString("abh03cnpjAssis1")), 14, '0', true);																			//386 a 399
			txt.print(StringUtils.extractNumbers(nb.format(contribAss1.setScale(2))), 8, '0', true);																				//400 a 407
			txt.print(StringUtils.extractNumbers(tmAbh80.get(i).getString("abh03cnpjAssis2")), 14, '0', true);																			//408 a 421
			txt.print(StringUtils.extractNumbers(nb.format(contribAss2.setScale(2))), 8, '0', true);																				//422 a 429
			txt.print(StringUtils.extractNumbers(tmAbh80.get(i).getString("abh03cnpjSind")), 14, '0', true);																				//430 a 443
			txt.print(StringUtils.extractNumbers(nb.format(contribSind.setScale(2))), 8, '0', true);																				//444 a 451
			txt.print(StringUtils.extractNumbers(tmAbh80.get(i).getString("abh03cnpjAssist")), 14, '0', true);																			//452 a 465
			txt.print(StringUtils.extractNumbers(nb.format(contribAssist.setScale(2))), 8, '0', true);																			//466 a 473
			txt.print(StringUtils.extractNumbers(tmAbh80.get(i).getString("abh03cnpjConf")), 14, '0', true);																				//474 a 487
			txt.print(StringUtils.extractNumbers(nb.format(contribConfed.setScale(2))), 8, '0', true);																			//488 a 495
			txt.print(tmAbh80.get(i).getString("aag0201ibge") == null ? 0 : tmAbh80.get(i).getString("aag0201ibge"), 7, '0', true);																//496 a 502
			txt.print(horExtrasJaneiro.intValue(), 3, '0', true);																													//503 a 505
			txt.print(horExtrasFevereiro.intValue(), 3, '0', true);																												//506 a 508
			txt.print(horExtrasMarco.intValue(), 3, '0', true);																													//509 a 511
			txt.print(horExtrasAbril.intValue(), 3, '0', true);																													//512 a 514
			txt.print(horExtrasMaio.intValue(), 3, '0', true);																													//515 a 517
			txt.print(horExtrasJunho.intValue(), 3, '0', true);																													//518 a 520
			txt.print(horExtrasJulho.intValue(), 3, '0', true);																													//521 a 523
			txt.print(horExtrasAgosto.intValue(), 3, '0', true);																													//524 a 526
			txt.print(horExtrasSetembro.intValue(), 3, '0', true);																												//527 a 529
			txt.print(horExtrasOutubro.intValue(), 3, '0', true);																													//530 a 532
			txt.print(horExtrasNovembro.intValue(), 3, '0', true);																												//533 a 535
			txt.print(horExtrasDezembro.intValue(), 3, '0', true);																												//536 a 538
			txt.print(tmAbh80.get(i).getInteger("abh80sindTrab") == 1 ? 1 : 2, 1);																										//539 a 539
			txt.print("2", 1);																																					//540 a 540
			txt.print(tmAbh80.get(i).getInteger("abh80regParc") != null && tmAbh80.get(i).getInteger("abh80regParc") == 1 ? 1 : 2, 1);															//541 a 541
			txt.print(tmAbh80.get(i).getInteger("abh80teleTrab") == 1 ? 1 : 2, 1);																										//542 a 542
			txt.print(tmAbh80.get(i).getInteger("abh80intermitente") == 0 ? "2" : "1", 1);																								//543 a 543
			txt.print(StringUtils.space(8));																																					//544 a 551
			txt.newLine();
			sequencial++;
			totalRegTipo2++;
		}
	}

	/**
	 * TIPO 9 NORMAL
	 */
	private void gerarTipo9Normal(TextFile txt, String CNPJUltimaEmpresa) {
		txt.print(sequencial, 6, '0', true);																																		//001 a 006
		txt.print(StringUtils.extractNumbers(CNPJUltimaEmpresa), 14, '0', true);																									//007 a 020
		txt.print("00", 2);																																						//021 a 022
		txt.print(9, 1);																																							//023 a 023
		txt.print(totalRegTipo1, 6, '0', true);																																	//024 a 029
		txt.print(totalRegTipo2, 6, '0', true);																																	//030 a 035
		txt.print(StringUtils.space(516));																																						//036 a 551
		txt.newLine();
	}

	/**
	 * TIPO 0 GENÉRICO
	 */
	private void gerarTipo0Generico(TextFile txt, SFP8503Dto sfp8503Dto) {
		txt.print(sequencial, 6, '0', true);                                                                      																//001 a 006
		txt.print(StringUtils.extractNumbers(sfp8503Dto.cnpjPrimeiraEmpresa), 14, '0', true);																				//007 a 020
		txt.print("00", 2);																																						//021 a 022
		txt.print(0, 1);																																							//023 a 023
		txt.print(1, 1);																			 																				//024 a 024
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respNI), 14, '0', true);																								//025 a 038
		txt.print(sfp8503Dto.respTI, 1);																																		//039 a 039
		txt.print(StringUtils.unaccented(sfp8503Dto.respRS), 40);			  																								//040 a 079
		txt.print(StringUtils.unaccented(sfp8503Dto.respEndereco), 40);																										//080 a 119
		txt.print(sfp8503Dto.respNumero, 6, '0', true);																														//120 a 125
		txt.print(StringUtils.unaccented(sfp8503Dto.respComplem), 21);																										//126 a 146
		txt.print(StringUtils.unaccented(sfp8503Dto.respBairro), 19);																										//147 a 165
		txt.print(sfp8503Dto.respCEP, 8);																																	//166 a 173
		txt.print(sfp8503Dto.respMunicipioCod, 7, '0', true);																												//174 a 180
		txt.print(StringUtils.unaccented(sfp8503Dto.respMunicipio), 30);																										//181 a 210
		txt.print(sfp8503Dto.respUF, 2);																																		//211 a 212
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respDDD), 2, '0', true);																								//213 a 214
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respFone), 9, '0', true);																							//215 a 223
		txt.print(sfp8503Dto.raisRetificada ? 1 : 2, 1);																														//224 a 224
		txt.print(sfp8503Dto.dtRetificacao == null ? 0 : sfp8503Dto.dtRetificacao.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);						//225 a 232
		txt.print(sfp8503Dto.dtGeracao.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																					//233 a 240
		txt.print(StringUtils.unaccented(sfp8503Dto.respEmail), 45);																											//241 a 285
		txt.print(StringUtils.unaccented(sfp8503Dto.respNome), 52);																											//286 a 337
		txt.print(StringUtils.space(24));																																						//338 a 361
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respCPF), 11, '0', true);																							//362 a 372
		txt.print(StringUtils.extractNumbers(sfp8503Dto.respCREA), 12, '0', true);																							//373 a 384
		txt.print(sfp8503Dto.respNasc == null ? 0 : sfp8503Dto.respNasc.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);									//385 a 392
		txt.print(StringUtils.space(36));															    																						//393 a 428
		txt.newLine();
		sequencial++;
	}

	/**
	 * TIPO 1 GENÉRICO
	 */
	private void gerarTipo1Generico(TextFile txt, SFP8503Dto sfp8503Dto) {
		Optional.of(buscarDadosDasEmpresasParaGeracaoDoRAIS(sfp8503Dto.clientCriterionAac10)).ifPresent({tmEmps ->
			def dtInicial = LocalDate.of(sfp8503Dto.anoBase, Month.JANUARY, 1);
			def dtFinal = LocalDate.of(sfp8503Dto.anoBase, Month.DECEMBER, 31);

			List<TableMap> tmAac10s = comporEmpresasELotacoesParaRAIS(tmEmps);
			for(int i = 0; i < tmAac10s.size(); i++) {
				def aac10id = tmAac10s.get(i).getLong("aac10id");
				def aac01id = tmAac10s.get(i).getLong("aac01id");
				Aac13 aac13 = buscarParametroFiscalParaRAIS(aac10id);

				txt.print(sequencial, 6, '0', true);                                                 																				//001 a 006
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10ni")), 14, '0', true);																				//007 a 020
				txt.print(StringUtils.ajustString(tmAac10s.get(i).getInteger("prefixoDoEstabelecimento"), 2, '0', true), 2);																//021 a 022
				txt.print(1, 1);																																					//023 a 023
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10rs")), 52);																								//024 a 075
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10endereco")), 40);																						//076 a 115
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10numero")), 6, '0', true);																			//116 a 121
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10complem")), 21);																							//122 a 142
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aac10bairro")), 19);																							//143 a 161
				txt.print(tmAac10s.get(i).getString("aac10cep"), 8);																														//162 a 169
				txt.print(tmAac10s.get(i).getString("aag0201ibge"), 7);																													//170 a 176
				txt.print(StringUtils.unaccented(tmAac10s.get(i).getString("aag0201nome")), 30);																							//177 a 206
				txt.print(tmAac10s.get(i).getString("aag02uf"), 2);																														//207 a 208
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10dddFone")), 2, '0', true);																			//209 a 210
				txt.print(StringUtils.extractNumbers(tmAac10s.get(i).getString("aac10fone")), 9, '0', true);																				//211 a 219
				txt.print("1", 1);																																				//220 a 220
				txt.print(existemTrabalhadoresNaEmpresa(aac01id, sfp8503Dto.anoBase, sfp8503Dto.tiposAbh80) ? 0 : 1, 1);												//221 a 221
				txt.print("00", 2);																																				//222 a 223
				txt.print(tmAac10s.get(i).getString("abh02ni") != null ? StringUtils.extractNumbers(tmAac10s.get(i).getString("abh02ni")) : 0, 12, '0', true);									//224 a 235
				txt.print(sfp8503Dto.anoBase, 4);																															//236 a 239
				txt.print(tmAac10s.get(i).getDate("aac10dtEnc") != null ? 1 : 2, 1);																										//240 a 240
				txt.print(tmAac10s.get(i).getDate("aac10dtEnc") == null ? 0 : tmAac10s.get(i).getDate("aac10dtEnc").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);				//241 a 248
				txt.print(aac13 != null && aac13.aac13natJurid != null ? StringUtils.extractNumbers(aac13.aac13natJurid.aaj02eSocial) : "", 4, '0', true);			//249 a 252
				txt.print(StringUtils.space(171));																																				//253 a 423
				txt.print(StringUtils.space(5));																																				//424 a 428
				txt.newLine();
				sequencial++;

				gerarTipo2Generico(txt, sfp8503Dto, aac01id, tmAac10s.get(i).getLong("abh02id"), tmAac10s.get(i).getString("aac10ni"), dtInicial, dtFinal, tmAac10s.get(i).getInteger("prefixoDoEstabelecimento"));
				totalRegTipo1++;
			}
		});
	}

	/**
	 * TIPO 2 GENÉRICO
	 */
	private void gerarTipo2Generico(TextFile txt, SFP8503Dto sfp8503Dto, Long aac01id, Long abh02id, String aac10ni, LocalDate dtInicial, LocalDate dtFinal, Integer prefixoDoEstabelecimento) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);

		List<TableMap> tmAbh80s = buscarDadosDosTrabalhadoresParaRAIS(aac01id, abh02id, sfp8503Dto.anoBase, sfp8503Dto.tiposAbh80);
		for(int i = 0; i < tmAbh80s.size(); i++) {
			def abh80id = tmAbh80s.get(i).getLong("abh80id");
			def codigoDaRescisao = "";
			def dataDaProjecao = "";
			def avisoPrevioIndenizado = BigDecimal.ZERO;
			def feriasIndenizadas = BigDecimal.ZERO;
			def multaRescisao = BigDecimal.ZERO;

			Fbd10 fbd10 = buscarRescisaoDoTrabalhadorParaRAIS(abh80id, sfp8503Dto.anoBase);
			if(tmAbh80s.get(i).getInteger("abh80sit") != Abh80.SIT_ATIVO && fbd10 == null) return;

			if(fbd10 != null) {
				codigoDaRescisao = fbd10.fbd10causa.abh06rais;
				dataDaProjecao = fbd10.fbd10apDtProj.format(DateTimeFormatter.ofPattern("ddMM"));
				avisoPrevioIndenizado = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20AvPrevInd, 0, fbd10.fbd10dtRes.getYear, false);
				feriasIndenizadas = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20FeriasInd, 0, fbd10.fbd10dtRes.getYear, false);
				multaRescisao = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20MultaFGTS, 0, fbd10.fbd10dtRes.getYear, false);
			}

			//Obtêm as remunerações dos meses a partir do CAE da tela "Remuneração mensal" (se houver).
			def remJaneiro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 1, sfp8503Dto.anoBase, true);
			def remFevereiro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 2, sfp8503Dto.anoBase, true);
			def remMarco = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 3, sfp8503Dto.anoBase, true);
			def remAbril = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 4, sfp8503Dto.anoBase, true);
			def remMaio = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 5, sfp8503Dto.anoBase, true);
			def remJunho = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 6, sfp8503Dto.anoBase, true);
			def remJulho = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 7, sfp8503Dto.anoBase, true);
			def remAgosto = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 8, sfp8503Dto.anoBase, true);
			def remSetembro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 9, sfp8503Dto.anoBase, true);
			def remOutubro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 10, sfp8503Dto.anoBase, true);
			def remNovembro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 11, sfp8503Dto.anoBase, true);
			def remDezembro = calcularCAE(aac01id, abh80id, sfp8503Dto.abh20RemunMensal, 12, sfp8503Dto.anoBase, true);

			//13º salário
			def mesPag13Parc1 = buscarMesDePagamentoDo13SalarioParaRAIS(aac01id, abh80id, sfp8503Dto.abh2013Sal1, sfp8503Dto.anoBase);
			def mesPag13Parc2 = buscarMesDePagamentoDo13SalarioParaRAIS(aac01id, abh80id, sfp8503Dto.abh2013Sal2, sfp8503Dto.anoBase);

			//Remuneração do 13º salário.
			def sal13Parc1 = calcularCAE(aac01id, abh80id, sfp8503Dto.abh2013Sal1, 0, sfp8503Dto.anoBase, false);
			def sal13Parc2 = calcularCAE(aac01id, abh80id, sfp8503Dto.abh2013Sal2, 0, sfp8503Dto.anoBase, false);

			//Dados de deficiência do trabalhador.
			List<Integer> deficiente = new ArrayList<Integer>();
			if(tmAbh80s.get(i).getInteger("abh80defFisico") == 1) deficiente.add(1);
			if(tmAbh80s.get(i).getInteger("abh80defVisual") == 1) deficiente.add(3);
			if(tmAbh80s.get(i).getInteger("abh80defAuditivo") == 1) deficiente.add(2);
			if(tmAbh80s.get(i).getInteger("abh80defMental") == 1) deficiente.add(4);
			if(tmAbh80s.get(i).getInteger("abh80defIntelecto") == 1) deficiente.add(4);
			if(tmAbh80s.get(i).getInteger("abh80defReabil") == 1) deficiente.add(6);
			if(deficiente.size() == 0) deficiente.add(0);

			//Afastamentos do trabalhador.
			Map<String, Object> mapAfast = calcularAfastamentos(aac01id, abh80id, sfp8503Dto.anoBase);
			def afast1 = mapAfast.get("afast1");
			def dtSai1 = mapAfast.get("dtSai1");
			def dtRet1 = mapAfast.get("dtRet1");
			def afast2 = mapAfast.get("afast2");
			def dtSai2 = mapAfast.get("dtSai2");
			def dtRet2 = mapAfast.get("dtRet2");
			def afast3 = mapAfast.get("afast3");
			def dtSai3 = mapAfast.get("dtSai3");
			def dtRet3 = mapAfast.get("dtRet3");
			def diasAfast = mapAfast.get("diasAfast");

			txt.print(sequencial, 6, '0', true); 																																	//001 a 006
			txt.print(StringUtils.extractNumbers(aac10ni), 14, '0', true);																										//007 a 020
			txt.print(StringUtils.ajustString(prefixoDoEstabelecimento, 2, '0', true), 2);																						//021 a 022
			txt.print(2, 1);																																						//023 a 023
			txt.print(StringUtils.extractNumbers(tmAbh80s.get(i).getString("abh80pis")), 11, '0', true);																					//024 a 034
			txt.print(StringUtils.unaccented(tmAbh80s.get(i).getString("abh80nome")), 52);																								//035 a 086
			txt.print(tmAbh80s.get(i).getDate("abh80nascData") == null ? 0 : tmAbh80s.get(i).getDate("abh80nascData").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);				//087 a 094
			txt.print(tmAbh80s.get(i).getString("abh80cpf"), 11, '0', true);																												//095 a 105
			txt.print("0", 1);																																					//106 a 106
			txt.print(tmAbh80s.get(i).getString("abh80ctpsNum"), 8, '0', true);																											//107 a 114
			txt.print(tmAbh80s.get(i).getString("abh80ctpsSerie"), 5, '0', true);																											//115 a 119
			txt.print(tmAbh80s.get(i).getDate("abh80dtAdmis") == null ? 0 : tmAbh80s.get(i).getDate("abh80dtAdmis").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8, '0', true);			  	//120 a 127
			txt.print(tmAbh80s.get(i).getString("aap16rais"), 2, '0', true);																												//128 a 129
			txt.print(StringUtils.extractNumbers(nb.format(buscarSalarioContratual(abh80id, dtInicial, dtFinal))), 12, '0', true);												//130 a 141
			txt.print(tmAbh80s.get(i).getString("aap18rais"), 1, '0', true);																												//142 a 142
			txt.print(tmAbh80s.get(i).getInteger("abh80hs"), 2, '0', true);																												//143 a 144
			txt.print(tmAbh80s.get(i).getString("aap03codigo"), 6, '0', true);																											//145 a 150
			txt.print(tmAbh80s.get(i).getString("aap17codigo"), 2, '0', true);																											//151 a 152
			txt.print(codigoDaRescisao, 2, '0', true);			                        																						//153 a 154
			txt.print(dataDaProjecao, 4, '0', true);																																//155 a 158
			txt.print(StringUtils.extractNumbers(nb.format(remJaneiro.setScale(2))), 12, '0', true);						   	 													//159 a 170
			txt.print(StringUtils.extractNumbers(nb.format(remFevereiro.setScale(2))), 12, '0', true);			   																//171 a 182
			txt.print(StringUtils.extractNumbers(nb.format(remMarco.setScale(2))), 12, '0', true);																				//183 a 194
			txt.print(StringUtils.extractNumbers(nb.format(remAbril.setScale(2))), 12, '0', true);																				//195 a 206
			txt.print(StringUtils.extractNumbers(nb.format(remMaio.setScale(2))), 12, '0', true);																					//207 a 218
			txt.print(StringUtils.extractNumbers(nb.format(remJunho.setScale(2))), 12, '0', true);																				//219 a 230
			txt.print(StringUtils.extractNumbers(nb.format(remJulho.setScale(2))), 12, '0', true);																				//231 a 242
			txt.print(StringUtils.extractNumbers(nb.format(remAgosto.setScale(2))), 12, '0', true);																				//243 a 254
			txt.print(StringUtils.extractNumbers(nb.format(remSetembro.setScale(2))), 12, '0', true);																				//255 a 266
			txt.print(StringUtils.extractNumbers(nb.format(remOutubro.setScale(2))), 12, '0', true);																				//267 a 278
			txt.print(StringUtils.extractNumbers(nb.format(remNovembro.setScale(2))), 12, '0', true);			 																	//279 a 290
			txt.print(StringUtils.extractNumbers(nb.format(remDezembro.setScale(2))), 12, '0', true);																				//291 a 302
			txt.print(StringUtils.extractNumbers(nb.format(sal13Parc1.setScale(2))), 12, '0', true);																				//303 a 314
			txt.print(mesPag13Parc1, 2, '0', true);																																//315 a 316
			txt.print(StringUtils.extractNumbers(nb.format(sal13Parc2.setScale(2))), 12, '0', true);																				//317 a 328
			txt.print(mesPag13Parc2, 2, '0', true);																																//329 a 330
			txt.print(StringUtils.extractNumbers(nb.format(avisoPrevioIndenizado.setScale(2))), 12, '0', true);																	//331 a 342
			txt.print(deficiente.get(0) == 0 ? 2 : 1 , 1);				    																									//343 a 343
			txt.print(deficiente.size() > 1 ? "5" : deficiente.get(0), 1, '0', true);																								//344 a 344
			txt.print(afast1 != null ? afast1 : 0, 2, '0', true);																													//345 a 346
			txt.print(dtSai1 == null ? 0 : dtSai1.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//347 a 350
			txt.print(dtRet1 == null ? 0 : dtRet1.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//351 a 354
			txt.print(afast2 != null ? afast2 : 0, 2, '0', true);																													//355 a 356
			txt.print(dtSai2 == null ? 0 : dtSai2.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//357 a 360
			txt.print(dtRet2 == null ? 0 : dtRet2.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//361 a 364
			txt.print(afast3 != null ? afast3 : 0, 2, '0', true);																													//365 a 366
			txt.print(dtSai3 == null ? 0 : dtSai3.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//367 a 370
			txt.print(dtRet3 == null ? 0 : dtRet3.format(DateTimeFormatter.ofPattern("ddMM")), 4, '0', true);																		//371 a 374
			txt.print(diasAfast, 3, '0', true);																																	//375 a 377
			txt.print(StringUtils.extractNumbers(nb.format(feriasIndenizadas.setScale(2))), 8, '0', true);																		//378 a 385
			txt.print(0, 8, '0', true);																																			//386 a 393
			txt.print(0, 2, '0', true);																																			//394 a 395
			txt.print(0, 8, '0', true);																																			//396 a 403
			txt.print(0, 2, '0', true);																																			//404 a 405
			txt.print(0, 8, '0', true);																																			//406 a 413
			txt.print(0, 2, '0', true);																																			//414 a 415
			txt.print(StringUtils.extractNumbers(nb.format(multaRescisao.setScale(2))), 8, '0', true);																			//416 a 423
			txt.print("2", 1);																																					//424 a 424
			txt.print(StringUtils.space(4));																																					//425 a 428
			txt.newLine();
			sequencial++;
			totalRegTipo2++;
		}

	}

	/**
	 * TIPO 9 GENÉRICO
	 */
	private void gerarTipo9Generico(TextFile txt, String CNPJUltimaEmpresa) {
		txt.print(sequencial, 6, '0', true);																																		//001 a 006
		txt.print(StringUtils.extractNumbers(CNPJUltimaEmpresa), 14, '0', true);																									//007 a 020
		txt.print("00", 2);																																						//021 a 022
		txt.print(9, 1);																																							//023 a 023
		txt.print(totalRegTipo1, 6, '0', true);																																	//024 a 029
		txt.print(totalRegTipo2, 6, '0', true);																																	//030 a 035
		txt.print(StringUtils.space(393));																																						//036 a 428
		txt.newLine();
	}

	private List<TableMap> comporEmpresasELotacoesParaRAIS(List<TableMap> tmEmps) {
		List<TableMap> tmAac10s = new ArrayList<>();
		for(int i = 0; i < tmEmps.size(); i++) {
			def prefixoDoEstabelecimento = 0;
			def aac01id = buscarIdDoGrupoCentralizadorPorIdEmpresa(tmEmps.get(i).getLong("aac10id"));
			tmEmps.get(i).put("aac01id", aac01id);
			tmEmps.get(i).put("abh02id", null);
			tmEmps.get(i).put("abh02ni", null);
			tmEmps.get(i).put("prefixoDoEstabelecimento", prefixoDoEstabelecimento);
			tmAac10s.add(tmEmps.get(i));
			prefixoDoEstabelecimento++;

			List<TableMap> tmLots = buscarDadosDasLotacoesParaRAIS(aac01id);
			if(tmLots != null && tmLots.size() > 0) {
				for(TableMap tmLot : tmLots) {
					TableMap tm = new TableMap();
					tm.put("aac01id", aac01id);
					tm.put("abh02id", tmLot.getLong("abh02id"));
					tm.put("abh02ni", tmLot.getString("abh02ni"));
					tm.put("prefixoDoEstabelecimento", prefixoDoEstabelecimento);
					tm.put("aac10id", tmEmps.get(i).getLong("aac10id"));
					tm.put("aac10codigo", tmEmps.get(i).getString("aac10codigo"));
					tm.put("aac10ni", tmEmps.get(i).getString("aac10ni"));
					tm.put("aac10rs", tmEmps.get(i).getString("aac10rs"));
					tm.put("aac10endereco", tmEmps.get(i).getString("aac10endereco"));
					tm.put("aac10numero", tmEmps.get(i).getString("aac10numero"));
					tm.put("aac10complem", tmEmps.get(i).getString("aac10complem"));
					tm.put("aac10bairro", tmEmps.get(i).getString("aac10bairro"));
					tm.put("aac10cep", tmEmps.get(i).getString("aac10cep"));
					tm.put("aac10dddFone", tmEmps.get(i).getString("aac10dddFone"));
					tm.put("aac10fone", tmEmps.get(i).getString("aac10fone"));
					tm.put("aac10email", tmEmps.get(i).getString("aac10email"));
					tm.put("aac10cnae", tmEmps.get(i).getString("aac10cnae"));
					tm.put("aac10dtEnc", tmEmps.get(i).getDate("aac10dtEnc"));
					tm.put("aag0201nome", tmEmps.get(i).getString("aag0201nome"));
					tm.put("aag0201ibge", tmEmps.get(i).getString("aag0201ibge"));
					tm.put("aag02uf", tmEmps.get(i).getString("aag02uf"));
					tmAac10s.add(tm);
					prefixoDoEstabelecimento++;
				}
			}
		}

		return tmAac10s;
	}

	private List<TableMap> buscarDadosDasEmpresasParaGeracaoDoRAIS(ClientCriterion clientCriterionAac10) {
		return getSession().createCriteria(Aac10.class)
				.addFields("aac10id, aac10codigo, aac10ni, aac10rs, aac10endereco, aac10numero, aac10complem, aac10bairro, aac10cep, aac10dddFone, aac10fone, aac10email, aac10cnae, aac10dtEnc, aag0201nome, aag0201ibge, aag02uf")
				.addJoin(Joins.join("aac10municipio").left(true))
				.addJoin(Joins.join("aac10municipio.aag0201uf").left(true))
				.addWhere(ClientCriteriaConvert.convertCriterion(clientCriterionAac10))
				.addWhere(getSamWhere().getCritPadrao(Aac10.class))
				.setOrder("aac10codigo")
				.getListTableMap();
	}

	private Long buscarIdDoGrupoCentralizadorPorIdEmpresa(Long aac10id) {
		return getSession().createCriteria(Aac1001.class)
				.addFields("aac1001gc")
				.addWhere(Criterions.eq("aac1001empresa", aac10id))
				.addWhere(Criterions.eq("aac1001tabela", "FB"))
				.get(ColumnType.LONG);
	}

	private List<TableMap> buscarDadosDasLotacoesParaRAIS(Long aac01id) {
		return getSession().createCriteria(Abh80.class)
				.addFields("DISTINCT abh02id, abh02ni")
				.addJoin(Joins.join("abh80lotacao"))
				.addWhere(Criterions.eq("abh80gc", aac01id))
				.addWhere(Criterions.eq("abh02ti", Abh02.TI_CNO))
				.addWhere(Criterions.isNotNull("abh02ni"))
				.addWhere(getSamWhere().getCritPadrao(Abh80.class))
				.setOrder("abh02ni")
				.getListTableMap();
	}

	private boolean existemTrabalhadoresNaEmpresa(Long aac01id, Integer anoBase, Set<Integer> tiposAbh80) {
		Integer count = getSession().createQuery(
				"SELECT COUNT(*) FROM Fba0101 AS fba0101 ",
				"INNER JOIN Fba01 ON fba01id = fba0101calculo ",
				"INNER JOIN Abh80 ON abh80id = fba0101trab ",
				"WHERE abh80.abh80gc = :aac01id AND DATE_PART('YEAR', fba0101.fba0101dtCalc) = :anoBase AND abh80.abh80tipo IN (:tiposAbh80) AND abh80.abh80pis IS NOT NULL ",
				getSamWhere().getWherePadrao("AND", Fba01.class))
				.setParameters("aac01id", aac01id, "anoBase", anoBase, "tiposAbh80", tiposAbh80)
				.getUniqueResult(ColumnType.INTEGER);

		return count != null && count > 0;
	}

	private BigDecimal buscarSalarioMinimo() {
		String sql = " SELECT * FROM Aba01 WHERE UPPER(aba01aplic) = :aba01aplic AND UPPER(aba01param) = :aba01param " + getSamWhere().getWherePadrao("AND", Aba01.class);
		TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("aba01aplic", "FB"), Parametro.criar("aba01param", "FB_SALARIOMINIMO"));
		if(tm == null) return BigDecimal.ZERO;
		return tm.getBigDecimal("aba01conteudo");
	}

	private List<TableMap> buscarDadosDoUltimoCalculoDosTrabalhadoresParaRAIS(Long aac01id, LocalDate dtInicial, LocalDate dtFinal, Set<Integer> tiposAbh80) {
		return getSession().createCriteria(Fba0101.class)
				.addFields("abh80codigo, abh80hs, abh80salario, aap18codigo")
				.addJoin(Joins.join("fba0101calculo"))
				.addJoin(Joins.join("fba0101trab"))
				.addJoin(Joins.join("fba0101trab.abh80unidPagto"))
				.addWhere(Criterions.eq("abh80gc", aac01id))
				.addWhere(Criterions.between("fba0101dtCalc", dtInicial, dtFinal))
				.addWhere(Criterions.in("abh80tipo", tiposAbh80))
				.addWhere(Criterions.isNotNull("abh80pis"))
				.addWhere(Criterions.not(Criterions.in("fba0101tpVlr", Arrays.asList([5]))))
				.addWhere(getSamWhere().getCritPadrao(Fba01.class))
				.setOrder("abh80codigo, fba0101dtCalc DESC")
				.getListTableMap();
	}

	private Aac13 buscarParametroFiscalParaRAIS(Long aac10id) {
		return getSession().createCriteria(Aac13.class)
				.addFields("aac13id, aaj01id, aaj01eSocial, aaj02id, aaj02eSocial")
				.addJoin(Joins.part("aac13classTrib").partial(true).left(true))
				.addJoin(Joins.part("aac13natJurid").partial(true).left(true))
				.addWhere(Criterions.eq("aac13empresa", aac10id))
				.get(ColumnType.ENTITY);
	}

	private List<TableMap> buscarDadosDosTrabalhadoresParaRAIS(Long aac01id, Long abh02id, Integer anoBase, Set<Integer> tiposAbh80) {
		String whereAbh02 = "";
		List<Long> idsAbh02 = null;
		if(abh02id != null) {
			whereAbh02 = "AND abh02id = :abh02id ";
		}else {
			List<TableMap> tmAbh02s = buscarDadosDasLotacoesParaRAIS(aac01id);
			if(tmAbh02s != null && tmAbh02s.size() > 0) {
				idsAbh02 = tmAbh02s.stream().map({tm -> tm.getLong("abh02id")}).collect(Collectors.toList());
				whereAbh02 = "AND abh02id NOT IN (:idsAbh02) ";
			}
		}

		Query query = getSession().createQuery(
				"SELECT abh80id, abh80pis, abh80nome, abh80nascData, aap05codigo, abh80dtChegBr, aap06codigo, abh80cpf, abh80dtAdmis, aap16rais, aap18rais, abh80hs, aap03codigo, aap07codigo, ",
				"abh80defFisico, abh80defVisual, abh80defAuditivo, abh80defMental, abh80defIntelecto, abh80defReabil, abh80alvara, abh80sexo, abh03ass1.abh03cnpj AS abh03cnpjAssis1, ",
				"abh03ass2.abh03cnpj AS abh03cnpjAssis2, abh03sind.abh03cnpj AS abh03cnpjSind, abh03assist.abh03cnpj AS abh03cnpjAssist, abh03conf.abh03cnpj AS abh03cnpjConf, aag0201ibge, ",
				"abh80sindTrab, abh80regParc, abh80teleTrab, abh80intermitente, abh80sit ",
				"FROM Abh80 ",
				"INNER JOIN Abh05 ON abh05id = abh80cargo ",
				"INNER JOIN Abh02 ON abh02id = abh80lotacao ",
				"INNER JOIN Aap18 ON aap18id = abh80unidPagto ",
				"LEFT JOIN Aap03 ON aap03id = abh05cbo ",
				"LEFT JOIN Aap05 ON aap05id = abh80nacionalidade ",
				"LEFT JOIN Aap06 ON aap06id = abh80gi ",
				"LEFT JOIN Aap16 ON aap16id = abh80tpAdmis ",
				"LEFT JOIN Aap17 ON aap17id = abh80vincEmp ",
				"LEFT JOIN Aap07 ON aap07id = abh80rc ",
				"LEFT JOIN Abh03 AS abh03ass1 ON abh03ass1.abh03id = abh80sindAss1 ",
				"LEFT JOIN Abh03 AS abh03ass2 ON abh03ass2.abh03id = abh80sindAss2 ",
				"LEFT JOIN Abh03 AS abh03sind ON abh03sind.abh03id = abh80sindSindical ",
				"LEFT JOIN Abh03 AS abh03assist ON abh03assist.abh03id = abh80sindAssist ",
				"LEFT JOIN Abh03 AS abh03conf ON abh03conf.abh03id = abh80sindConfed ",
				"LEFT JOIN Aag0201 ON aag0201id = abh02municipio ",
				"WHERE abh80gc = :aac01id AND abh80tipo IN (:tiposAbh80) AND abh80pis IS NOT NULL AND DATE_PART('YEAR', abh80dtAdmis) <= :anoBase ",
				"AND abh80id IN (SELECT DISTINCT fba0101trab FROM Fba0101 INNER JOIN Fba01 ON fba01id = fba0101calculo WHERE fba01gc = :aac01id AND DATE_PART('YEAR', fba0101dtCalc) = :anoBase) ",
				whereAbh02, getSamWhere().getWherePadrao("AND", Abh80.class),
				"ORDER BY abh80codigo");

		query.setParameters("aac01id", aac01id, "dataBase", LocalDate.of(anoBase, Month.JANUARY, 1), "anoBase", anoBase, "tiposAbh80", tiposAbh80);

		if(abh02id != null) {
			query.setParameter("abh02id", abh02id);
		}else if(idsAbh02 != null && idsAbh02.size() > 0) {
			query.setParameter("idsAbh02", idsAbh02);
		}

		return query.getListTableMap();
	}

	private BigDecimal buscarSalarioContratual(Long abh80id, LocalDate dtInicial, LocalDate dtFinal) {
		TableMap tm = getSession().createCriteria(Fba02.class)
				.addFields("fba02salario, abh80hs, aap18codigo")
				.addJoin(Joins.join("fba02trab"))
				.addJoin(Joins.join("fba02trab.abh80unidPagto"))
				.addWhere(Criterions.eq("abh80id", abh80id))
				.addWhere(Criterions.between("fba02dtCalc", dtInicial, dtFinal))
				.addWhere(getSamWhere().getCritPadrao(Fba02.class))
				.setOrder("fba02id DESC")
				.setMaxResults(1)
				.getUniqueTableMap();

		return tm != null ? tm.getBigDecimal("fba02salario") : BigDecimal.ZERO;
	}

	private Fbd10 buscarRescisaoDoTrabalhadorParaRAIS(Long abh80id, int anoBase) {
		return getSession().createCriteria(Fbd10.class)
				.addJoin(Joins.fetch("fbd10causa"))
				.addWhere(Criterions.eq("fbd10trab", abh80id))
				.addWhere(Criterions.eq("DATE_PART('YEAR', fbd10apDtProj)", anoBase))
				.addWhere(getSamWhere().getCritPadrao(Fbd10.class))
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}

	private BigDecimal calcularCAE(Long aac01id, Long abh80id, Abh20 abh20, int mes, int ano, boolean consideraMes) {
		BigDecimal valor = BigDecimal.ZERO;
		if(abh20 == null) return valor;

		List<TableMap> tms = buscarValoresDosEventosParaCalculoDoCAE(aac01id, abh80id, abh20.getAbh20codigo(), mes, ano, consideraMes);
		if(tms != null && tms.size() > 0) {
			for(int i = 0; i < tms.size(); i++) {
				TableMap tm = tms.get(i);

				if(tm.getInteger("abh2101cvr") == Abh2101.CVR_SOMA_VLR) {
					valor = valor.add(tm.getBigDecimal("fba01011valor"));
				}else if(tm.getInteger("abh2101cvr") == Abh2101.CVR_DIMINUI_VLR) {
					valor = valor.subtract(tm.getBigDecimal("fba01011valor"));
				}
			}
		}
		return valor;
	}

	private List<TableMap> buscarValoresDosEventosParaCalculoDoCAE(Long aac01id, Long abh80id, String abh20codigo, int mes, int ano, boolean consideraMes) {
		String whereMes = consideraMes ? "AND DATE_PART('MONTH', fba0101dtCalc) = :mes " : "";

		Query query = getSession().createQuery(
				"SELECT abh2101cvr, fba01011valor ",
				"FROM Fba01011 ",
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
				"INNER JOIN Fba01 ON fba01id = fba0101calculo ",
				"INNER JOIN Abh21 ON abh21id = fba01011eve ",
				"INNER JOIN Abh2101 ON abh2101evento = abh21id ",
				"INNER JOIN Abh20 ON abh20id = abh2101cae ",
				"WHERE fba01gc = :aac01id ",
				"AND fba0101trab = :abh80id ",
				"AND DATE_PART('YEAR', fba0101dtCalc) = :ano ",
				"AND abh20codigo = :abh20codigo ",
				"AND fba01011valor > 0 ",
				whereMes);

		query.setParameter("aac01id", aac01id);
		if(abh80id != null) query.setParameter("abh80id", abh80id);
		query.setParameter("abh20codigo", abh20codigo);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		return query.getListTableMap();
	}

	private Integer buscarMesDePagamentoDo13SalarioParaRAIS(Long aac01id, Long abh80id, Abh20 abh20, int anoBase) {
		if(abh20 == null) return 0;

		Integer mes = getSession().createQuery(
				"SELECT DATE_PART('MONTH', fba0101dtCalc) ",
				"FROM Fba01011 AS fba01011 ",
				"INNER JOIN fba0101 ON fba0101id = fba01011vlr ",
				"INNER JOIN fba01 ON fba01id = fba0101calculo ",
				"INNER JOIN abh21 ON abh21id = fba01011eve ",
				"INNER JOIN Abh2101 ON abh2101evento = abh21id ",
				"INNER JOIN Abh20 ON abh20id = abh2101cae ",
				"WHERE fba01gc = :aac01id AND fba0101trab = :abh80id AND DATE_PART('YEAR', fba0101dtCalc) = :anoBase AND abh20codigo = :abh20codigo AND fba0101tpVlr <> 5 ")
				.setParameters("aac01id", aac01id, "abh80id", abh80id, "abh20codigo", abh20.getAbh20codigo(), "anoBase", anoBase)
				.setMaxResult(1)
				.getUniqueResult(ColumnType.INTEGER);

		return mes != null ? mes : 0;
	}

	private Map<String, Object> calcularAfastamentos(Long aac01id, Long abh80id, int anoBase) {
		Map<String, Object> mapAfasts = new HashMap<String, Object>();
		int diasAfast = 0;
		LocalDate dtInicial = LocalDate.of(sfp8503Dto.anoBase, Month.JANUARY, 1);
		LocalDate dtFinal = LocalDate.of(sfp8503Dto.anoBase, Month.DECEMBER, 31);

		//1º Afastamento
		Fbb01 fbb01Pri = buscarAfastamentoDoTrabalhadorParaRAIS(aac01id, abh80id, dtInicial, dtFinal, null);
		if(fbb01Pri != null) {
			LocalDate dtSai = DateUtils.dateDiff(fbb01Pri.getFbb01dtSai(), dtInicial, ChronoUnit.DAYS) > 0 ? dtInicial : fbb01Pri.getFbb01dtSai();
			LocalDate dtRet = fbb01Pri.getFbb01dtRet() == null || DateUtils.dateDiff(dtFinal, fbb01Pri.getFbb01dtRet(), ChronoUnit.DAYS) > 0 ? dtFinal : fbb01Pri.getFbb01dtRet();

			mapAfasts.put("afast1", fbb01Pri.getFbb01ma().getAbh07rais());
			mapAfasts.put("dtSai1", dtSai);
			mapAfasts.put("dtRet1", dtRet);
			diasAfast += (int) DateUtils.dateDiff(dtSai, dtRet, ChronoUnit.DAYS);

			//2º Afastamento
			Set<Long> idsAfast = new HashSet<Long>();
			idsAfast.add(fbb01Pri.getFbb01id());
			Fbb01 fbb01Seg = buscarAfastamentoDoTrabalhadorParaRAIS(aac01id, abh80id, dtInicial, dtFinal, idsAfast);
			if(fbb01Seg != null) {
				dtSai = DateUtils.dateDiff(fbb01Seg.getFbb01dtSai(), dtInicial, ChronoUnit.DAYS) > 0 ? dtInicial : fbb01Seg.getFbb01dtSai();
				dtRet = fbb01Seg.getFbb01dtRet() == null || DateUtils.dateDiff(dtFinal, fbb01Seg.getFbb01dtRet(), ChronoUnit.DAYS) > 0 ? dtFinal : fbb01Seg.getFbb01dtRet();

				mapAfasts.put("afast2", fbb01Seg.getFbb01ma().getAbh07rais());
				mapAfasts.put("dtSai2", dtSai);
				mapAfasts.put("dtRet2", dtRet);
				diasAfast += (int) DateUtils.dateDiff(dtSai, dtRet, ChronoUnit.DAYS);

				//3º Afastamento
				idsAfast.add(fbb01Seg.getFbb01id());
				Fbb01 fbb01Ter = buscarAfastamentoDoTrabalhadorParaRAIS(aac01id, abh80id, dtInicial, dtFinal, idsAfast);
				if(fbb01Ter != null) {
					dtSai = DateUtils.dateDiff(fbb01Ter.getFbb01dtSai(), dtInicial, ChronoUnit.DAYS) > 0 ? dtInicial : fbb01Ter.getFbb01dtSai();
					dtRet = fbb01Ter.getFbb01dtRet() == null || DateUtils.dateDiff(dtFinal, fbb01Ter.getFbb01dtRet(), ChronoUnit.DAYS) > 0 ? dtFinal : fbb01Ter.getFbb01dtRet();

					mapAfasts.put("afast3", fbb01Ter.getFbb01ma().getAbh07rais());
					mapAfasts.put("dtSai3", dtSai);
					mapAfasts.put("dtRet3", dtRet);
					diasAfast += (int) DateUtils.dateDiff(dtSai, dtRet, ChronoUnit.DAYS);
				}
			}
		}

		mapAfasts.put("diasAfast", diasAfast);
		return mapAfasts;
	}

	private Fbb01 buscarAfastamentoDoTrabalhadorParaRAIS(Long aac01id, Long abh80id, LocalDate dataInicial, LocalDate dataFinal, Set<Long> idsFbb01s) {
		String whereAfast = idsFbb01s != null && idsFbb01s.size() > 0 ? "AND fbb01.fbb01id NOT IN (:idsFbb01s) " : "";

		Query query = getSession().createQuery(
				"SELECT * FROM Fbb01 AS fbb01 ",
				"INNER JOIN FETCH fbb01.fbb01ma AS abh07 ",
				"WHERE fbb01.fbb01gc = :aac01id ",
				"AND fbb01.fbb01trab = :abh80id ",
				"AND abh07.abh07rais IS NOT NULL ",
				"AND ((fbb01.fbb01dtSai BETWEEN :dataInicial AND :dataFinal) ",
				"OR (fbb01.fbb01dtRet BETWEEN :dataInicial AND :dataFinal) ",
				"OR (:dataInicial BETWEEN fbb01.fbb01dtSai AND fbb01.fbb01dtRet) ",
				"OR (:dataFinal BETWEEN fbb01.fbb01dtSai AND fbb01.fbb01dtRet) ",
				"OR (fbb01.fbb01dtSai <= :dataFinal AND fbb01.fbb01dtRet IS NULL)) ",
				whereAfast,
				getSamWhere().getWherePadrao("AND", Fbb01.class),
				" ORDER BY fbb01.fbb01id ASC");

		query.setParameter("aac01id", aac01id);
		query.setParameter("abh80id", abh80id);
		query.setParameter("dataInicial", dataInicial);
		query.setParameter("dataFinal", dataFinal);
		if(idsFbb01s != null && idsFbb01s.size() > 0) query.setParameter("idsFbb01s", idsFbb01s);
		query.setMaxResult(1);
		return query.getUniqueResult(ColumnType.ENTITY);
	}

	private BigDecimal buscarHorasExtrasMensaisParaRAIS(Long aac01id, Long abh80id, Abh20 abh20, int mes, int ano) {
		BigDecimal horas = BigDecimal.ZERO;
		if(abh20 == null) return horas;

		List<TableMap> tms = getSession().createQuery(
				"SELECT abh2101cvr, fba01011refHoras ",
				"FROM Fba01011 ",
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
				"INNER JOIN Fba01 ON fba01id = fba0101calculo ",
				"INNER JOIN Abh21 ON abh21id = fba01011eve ",
				"INNER JOIN Abh2101 ON abh2101evento = abh21id ",
				"INNER JOIN Abh20 ON abh20id = abh2101cae ",
				"WHERE fba01gc = :aac01id AND fba0101trab = :abh80id AND DATE_PART('MONTH', fba0101dtCalc) = :mes AND DATE_PART('YEAR', fba0101dtCalc) = :ano AND abh20codigo = :abh20codigo AND fba01011refHoras > 0 AND fba0101tpVlr <> 5")
				.setParameters("aac01id", aac01id, "abh80id", abh80id, "abh20codigo", abh20.getAbh20codigo(), "mes", mes, "ano", ano)
				.getListTableMap();

		if(tms != null && tms.size() > 0) {
			for(int i = 0; i < tms.size(); i++) {
				if(tms.get(i).getInteger("abh2101cvr") == Abh2101.CVR_SOMA_REF) {
					horas = horas + tms.get(i).getBigDecimal("fba01011refHoras");
				}else if(tms.get(i).getInteger("abh2101cvr") == Abh2101.CVR_DIMINUI_REF) {
					horas = horas - tms.get(i).getBigDecimal("fba01011refHoras");
				}
			}
		}

		return horas;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTQifQ==