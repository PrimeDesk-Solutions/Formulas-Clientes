package multitec.formulas.sfp.holerite;

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abf01
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fbc0101
import sam.server.samdev.formula.FormulaBase

class SFP_LayoutBradescoHolerite extends FormulaBase {
	private SFP8001DtoExportar sfp8001DtoExportar;
	private List<TableMap> abh80s;
	private Abf01 abf01;
	private Aac10 aac10;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVO_POR_TRABALHADOR;
	}

	@Override
	public void executar() {
		this.sfp8001DtoExportar = get("sfp8001DtoExportar");
		this.sb = get("sb");
		this.abf01 = getAcessoAoBanco().buscarRegistroUnicoById("Abf01", sfp8001DtoExportar.getAbf01().getAbf01id());
		this.abh80s = sfp8001DtoExportar.getAbh80s();
		this.aac10 = getAcessoAoBanco().obterEmpresa(getVariaveis().getAac10().getAac10id());

		SimpleDateFormat sdfData = new SimpleDateFormat("ddMMyyyy");
		def sequencial = 1;

		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);

		def cnpjAa65 = StringUtils.extractNumbers(aac10.getAac10ni());
		def numLote = 1;
		def codEmpresaBradesco = 1;
		def operacaoBradesco = 0;
		def tipoCompBradesco = 0;
		
		TextFile txt = new TextFile();

		/**
		 * TIPO 0 - HEADER DA EMPRESA
		 */
		txt.print(0);																												//001 a 001
		txt.print(StringUtils.ajustString("REMESSA HPAG EMPRESA", 20));																							//002 a 021
		txt.print(StringUtils.ajustString(codEmpresaBradesco, 9, '0', true));																					//022 a 030
		txt.print(StringUtils.ajustString(numLote, 9, '0', true));																								//031 a 039
		txt.print(StringUtils.ajustString(cnpjAa65.substring(0, cnpjAa65.length() -6), 9, '0', true));															//040 a 048
		txt.print(StringUtils.ajustString(cnpjAa65.substring(cnpjAa65.length() -6, cnpjAa65.length() -2), 4, '0', true));										//049 a 052
		txt.print(StringUtils.ajustString(cnpjAa65.substring(cnpjAa65.length() -2, cnpjAa65.length()), 2, '0', true));											//053 a 054
		txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8));																						//055 a 062
		txt.print(operacaoBradesco == 0 ? "I" : "S");																				//063 a 063
		txt.print(StringUtils.ajustString("00777", 5));																											//064 a 068
		txt.print(StringUtils.space(155)); 																								//069 a 223
		txt.print(StringUtils.space(1));																								//224 a 224
		txt.print(StringUtils.space(9));																								//225 a 233
		txt.print(StringUtils.space(12));																								//234 a 245
		txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																							//246 a 250
		txt.newLine();
		sequencial++;

		for(int i = 0; i < abh80s.size(); i++) {
			Abh80 abh80 = getHoleriteUtils().findAbh80ByUniqueKey(abh80s.get(i).getString("abh80codigo"));
			def cpfAb83 = StringUtils.extractNumbers(abh80s.get(i).getString("abh80cpf"));
			def nomeAb83 = abh80.abh80nome != null && abh80.abh80nome.length() > 30 ? abh80.abh80nome.substring(0, 30) : abh80.abh80nome;
			def nomeAb98 = abh80.abh80cargo.abh05nome.length() > 40 ? abh80.abh80cargo.abh05nome.substring(0, 40) : abh80.abh80cargo.abh05nome;
			def totalRegistros = 0;

			/**
			 * TIPO 1 - HEADER DO COMPROVANTE
			 */
			txt.print(1);																											//001 a 001
			txt.print("I");																											//002 a 002
			txt.print(StringUtils.ajustString(tipoCompBradesco, 3, '0', true));																					//003 a 005
			txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("MMyyyy")), 6));																				//006 a 011
			txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8));																					//012 a 019
			txt.print(StringUtils.ajustString("0237", 4));																										//020 a 023
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 5, '0', true));									//024 a 028
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 13, '0', true));									//029 a 041
			txt.print(StringUtils.ajustString(abh80s.get(i).getString("abh80bcoDigCta"), 2, '0', true));																//042 a 043
			txt.print(StringUtils.ajustString(cpfAb83.substring(0, cpfAb83.length() -2), 9, '0', true));															//044 a 052
			txt.print(StringUtils.ajustString(cpfAb83.substring(cpfAb83.length() -2, cpfAb83.length()), 2, '0', true));											//053 a 054
			txt.print(StringUtils.ajustString(abh80s.get(i).getString("abh80pis"), 14));																				//055 a 068
			txt.print(StringUtils.ajustString(abh80s.get(i).getString("abh80rgNum"), 13));																				//069 a 081
			txt.print(StringUtils.ajustString(abh80s.get(i).getString("abh80ctpsNum"), 9));																			//082 a 090
			txt.print(StringUtils.ajustString(nomeAb83, 30));																									//091 a 120
			txt.print(StringUtils.space(12));																							//121 a 132
			txt.print(StringUtils.ajustString(nomeAb98, 40));																									//133 a 172
			txt.print(abh80s.get(i).getDate("abh80dtAdmis").format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);															//173 a 180
			txt.print(StringUtils.space(53));																							//181 a 233
			txt.print(StringUtils.space(12));																							//234 a 245
			txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																						//246 a 250
			txt.newLine();
			sequencial++;

			/**
			 * TIPO 2 - DETALHES DO COMPROVANTE
			 */
			def idAbh80 = abh80s.get(i).getLong("abh80id");

			//Rendimentos
			def totalRend = 0;
			List<TableMap> rsFb011sRend = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(idAbh80, sfp8001DtoExportar.critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 0, eveFerNaoImpr);
			if(rsFb011sRend != null && rsFb011sRend.size() > 0) {
				for(int j = 0; j < rsFb011sRend.size(); j++) {
					gerarTipo2Bradesco(sb, rsFb011sRend.get(j).getString("abh21codigo"), rsFb011sRend.get(j).getString("abh21nome"), rsFb011sRend.get(j).getBigDecimal("valor"), 1, sequencial);
					sequencial++;
					totalRegistros++;
					totalRend = totalRend + rsFb011sRend.get(j).getBigDecimal("valor");
				}
			}

			//Total dos rendimentos
			gerarTipo2Bradesco(sb, "TR", "Total dos Rendimentos", totalRend, 2, sequencial);
			sequencial++;
			totalRegistros++;

			//Descontos
			def totalDesc = 0;
			List<TableMap> rsFb011sDesc = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(idAbh80, sfp8001DtoExportar.critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 1, eveFerNaoImpr);
			if(rsFb011sDesc != null && rsFb011sDesc.size() > 0) {
				for(int j = 0; j < rsFb011sDesc.size(); j++) {
					gerarTipo2Bradesco(sb, rsFb011sDesc.get(j).getString("abh21codigo"), rsFb011sDesc.get(j).getString("abh21nome"), rsFb011sDesc.get(j).getBigDecimal("valor"), 3, sequencial);
					sequencial++;
					totalRegistros++;
					totalDesc = totalDesc + rsFb011sDesc.get(j).getBigDecimal("valor");
				}
			}else {
				gerarTipo2Bradesco(sb, "D", "Descontos", 0, 3, sequencial);
				sequencial++;
				totalRegistros++;
			}

			//Férias e rescisão pagas.
			List<TableMap> mapFerResPagas = getHoleriteUtils().calcularFeriasRescisaoPagas(sfp8001DtoExportar.critAbb11, idAbh80, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, eveFeriasLiquida, eveFeriasPagas, eveAbonoLiquido, eveAbonoPago, eveAd13Liquido, eveAd13Pago, eveResLiquida, eveResPaga);
			if(mapFerResPagas != null && mapFerResPagas.size() > 0) {
				for(int k = 0; k < mapFerResPagas.size(); k++) {
					gerarTipo2Bradesco(sb, mapFerResPagas.get(k).getString("codigo"), mapFerResPagas.get(k).getString("nome"), mapFerResPagas.get(k).getBigDecimal("valor"), 3, sequencial);
					sequencial++;
					totalRegistros++;
					totalDesc = totalDesc + mapFerResPagas.get(k).getBigDecimal("valor");
				}
			}

			//Total dos descontos
			gerarTipo2Bradesco(sb, "TD", "Total dos Descontos", totalDesc, 4, sequencial);
			sequencial++;
			totalRegistros++;

			//Total Líquido
			gerarTipo2Bradesco(sb, "TL", "Total Líquido", totalRend - totalDesc, 5, sequencial);
			sequencial++;
			totalRegistros++;

//			for(String mensagem : mensagens) {
//				if(mensagem == null) continue;
//				/**
//				 * TIPO 3 - MENSAGENS DO COMPROVANTE
//				 */
//				txt.print(3, 1);																										//001 a 001
//				txt.print(mensagem, 40);																								//002 a 041
//				txt.print(StringUtils.space(195));																						//042 a 236
//				txt.print(StringUtils.space(9));																						//237 a 245
//				txt.print(sequencial, 5, '0', true);																					//246 a 250
//				txt.newLine();
//				sequencial++;
//				totalRegistros++;
//			}

			def isTipo4Bradesco = false;
			if(isTipo4Bradesco) {
				def qtdeDepIR = getHoleriteUtils().findAbh8002QtdeDependentesIR(idAbh80);
				def qtdeDepSF = getHoleriteUtils().findAbh8002QtdeDependentesSF(idAbh80);

				def dataPAInicial = null;
				def dataPAFinal = null;
				def dataPGInicial = null;
				def dataPGFinal = null;
				Fbc0101 fbc0101 = getHoleriteUtils().findFbc0101UltimoCalculoByIdTrabalhadorAndTipoFerias(idAbh80, 1);
				if(fbc0101 != null) {
					dataPAInicial = fbc0101.fbc0101pa.fbc01pai;
					dataPAFinal = fbc0101.fbc0101pa.fbc01paf;
					dataPGInicial = fbc0101.fbc0101pgi;
					dataPGFinal = fbc0101.fbc0101pgf;
				}else {
					Calendar cal = new GregorianCalendar();
					cal.setTime(abh80s.get(i).getDate("abh80dtadmis"));
					dataPAInicial = cal.getTime();
					cal.add(Calendar.YEAR, 1);
					cal.add(Calendar.DAY_OF_MONTH, -1);
					dataPAFinal = cal.getTime();
				}

				def diasFaltas = 0;
				def eveFaltaDesc = null;
				if(eveFaltaDesc != null) {
					def numeroHorasDesc = getHoleriteUtils().findFba01011RefByTrabAndEventoAndData(idAbh80, eveFaltaDesc, dataPAInicial, dataPAFinal);
					if(numeroHorasDesc != null) {
						diasFaltas = abh80s.get(i).getInteger("abh80hs") > 0 ? numeroHorasDesc / abh80s.get(i).getInteger("abh80hs") : 0;
					}
				}

				def valorBaseINSS = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeBaseINSS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
				def valorBaseINSS13 = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeBaseINSS13, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
				def valorBaseIRRFSal = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeBaseIRRFSal, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
				def valorBaseIRRF13 = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeBaseIRRF13, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
				def valorBaseIRRFFer = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeBaseIRRFFer, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
				def valorBaseIRRFPPR = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeBaseIRRFPPR, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
				def valorBaseFGTS = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeBaseFGTS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
				def valorFGTS = getHoleriteUtils().calcularCAE(idAbh80, sfp8001DtoExportar.critAbb11, caeFGTS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);

				/**
				 * TIPO 4 - INFORMAÇÔES GERAIS
				 */
				txt.print(4);																										//001 a 001
				txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8));																				//002 a 009
				txt.print(StringUtils.ajustString(qtdeDepIR, 2, '0', true));																						//010 a 011
				txt.print(StringUtils.ajustString(qtdeDepSF, 2, '0', true));																						//012 a 013
				txt.print(StringUtils.ajustString(abh80s.get(i).getInteger("abh80hs"), 2, '0', true));																//014 a 015
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(abh80s.get(i).getBigDecimal("abh80salario").round(2))), 12, '0', true));			//016 a 027
				txt.print(StringUtils.ajustString(diasFaltas, 2, '0', true));																			//028 a 029
				txt.print("S");																										//030 a 030
				txt.print(StringUtils.ajustString(sdfData.format(dataPAInicial), 8, '0', true));																	//031 a 038
				txt.print(StringUtils.ajustString(sdfData.format(dataPAFinal), 8, '0', true));																	//039 a 046
				txt.print(StringUtils.ajustString(dataPGInicial != null ? sdfData.format(dataPGInicial) : 0, 8, '0', true));										//047 a 054
				txt.print(StringUtils.ajustString(dataPGFinal != null ? sdfData.format(dataPGFinal) : 0, 8, '0', true));											//055 a 062
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseINSS)), 12, '0', true));								//063 a 074
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseINSS13)), 12, '0', true));								//075 a 086
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseIRRFSal)), 12, '0', true));								//087 a 098
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseIRRF13)), 12, '0', true));								//099 a 110
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseIRRFFer)), 12, '0', true));								//111 a 122
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseIRRFPPR)), 12, '0', true));								//123 a 134
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseFGTS)), 12, '0', true));								//135 a 146
				txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorFGTS)), 12, '0', true));									//147 a 158
				txt.print(StringUtils.space(78));																						//159 a 236
				txt.print(StringUtils.space(9));																						//237 a 245
				txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																					//246 a 250
				txt.newLine();
				sequencial++;
				totalRegistros++;
			}

			/**
			 * TIPO 5 - TRAILLER DO COMPROVANTE
			 */
			txt.print(5);																											//001 a 001
			txt.print(StringUtils.ajustString(totalRegistros, 5, '0', true));																					//002 a 006
			txt.print(StringUtils.space(230));																							//007 a 236
			txt.print(StringUtils.space(9));																							//237 a 245
			txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																						//246 a 250
			txt.newLine();
			sequencial++;
		}

		/**
		 * TIPO 9 - TRAILLER DA EMPRESA
		 */
		txt.print(9);																												//001 a 001
		txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																							//002 a 006
		txt.print(StringUtils.space(230));																								//007 a 236
		txt.print(StringUtils.space(9));																								//237 a 245
		txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																							//246 a 250
		txt.newLine();
		
		put("txt", txt);
	}
	
	private void gerarTipo2Bradesco(TextFile txt, String abh21codigo, String abh21nome, BigDecimal valor, int identificador, int sequencial) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		txt.print(2);																															//001 a 001
		txt.print(StringUtils.ajustString(abh21codigo, 4));																														//002 a 005
		txt.print(StringUtils.ajustString(abh21nome != null && abh21nome.length() > 20 ? abh21nome.substring(0, 20) : abh21nome, 20));											//006 a 025
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valor.round(2))), 12, '0', true));															//026 a 037
		txt.print(StringUtils.ajustString(identificador, 1));																												//038 a 038
		txt.print(StringUtils.space(198));																											//039 a 236
		txt.print(StringUtils.space(9));																											//237 a 245
		txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																										//246 a 250
		txt.newLine();
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==