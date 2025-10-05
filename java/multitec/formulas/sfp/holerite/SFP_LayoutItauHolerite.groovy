package multitec.formulas.sfp.holerite

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abf01
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fbc0101
import sam.server.samdev.formula.FormulaBase

class SFP_LayoutItauHolerite extends FormulaBase {
	private SFP8001DtoExportar sfp8001DtoExportar;
	private List<TableMap> abh80s;
	private Abf01 abf01;
	private Aac10 aac10;
	private ClientCriterion critAbb11;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVO_POR_TRABALHADOR;
	}

	@Override
	public void executar() {
		this.sfp8001DtoExportar = get("sfp8001DtoExportar");
		this.abf01 = getAcessoAoBanco().buscarRegistroUnicoById("Abf01", sfp8001DtoExportar.abf01id);
		this.abh80s = sfp8001DtoExportar.abh80s;
		this.aac10 = getAcessoAoBanco().obterEmpresa(getVariaveis().aac10.aac10id);
		this.critAbb11 = sfp8001DtoExportar.getCritAbb11();
		
		SimpleDateFormat sdfData = new SimpleDateFormat("ddMMyyyy");
		def sequencial = 1;

		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		def nomeAa65 = aac10.aac10rs.length() > 30 ? aac10.aac10rs.substring(0, 30) : aac10.aac10rs;
		def enderecoAa65 = aac10.aac10endereco.length() > 30 ? aac10.aac10endereco.substring(0, 30) : aac10.aac10endereco;
		def complemAa65 = aac10.aac10complem != null && aac10.aac10complem.length() > 15 ? aac10.aac10complem.substring(0, 15) : aac10.aac10complem;
		def municipioAa65 = aac10.aac10municipio.aag0201nome.length() > 20 ? aac10.aac10municipio.aag0201nome.substring(0, 20) : aac10.aac10municipio.aag0201nome;
		def cepAa65 = aac10.aac10cep != null ? aac10.aac10cep : "";
		def ufAa65 = aac10.aac10municipio.aag0201uf.aag02uf;
		def numLote = 1;

		TextFile txt = new TextFile();

		/**
		 * TIPO 0 - HEADER DO ARQUIVO
		 */
		txt.print(StringUtils.ajustString("341", 3));																											//001 a 003
		txt.print(StringUtils.ajustString("0000", 4));																											//004 a 007
		txt.print(StringUtils.ajustString("0", 1));																												//008 a 008
		txt.print(StringUtils.space(6));																								//009 a 014
		txt.print(StringUtils.ajustString("080", 3));																											//015 a 017
		txt.print(StringUtils.ajustString(aac10.aac10ti == 0 ? 2 : 1, 1));																					//018 a 018
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true));															//019 a 032
		txt.print(StringUtils.space(20));																								//033 a 052
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true));														//053 a 057
		txt.print(StringUtils.space(1));																								//058 a 058
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true));														//059 a 070
		txt.print(StringUtils.space(1));																								//071 a 071
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abf01.abf01digConta), 1, '0', true));													//072 a 072
		txt.print(StringUtils.ajustString(StringUtils.unaccented(nomeAa65), 30));																			//073 a 102
		txt.print(StringUtils.ajustString(StringUtils.unaccented(abf01.abf01nome), 30));																	//103 a 132
		txt.print(StringUtils.space(10));																								//133 a 142
		txt.print(StringUtils.ajustString("1", 1));																												//143 a 143
		txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8));																						//144 a 151
		txt.print(StringUtils.ajustString(MDate.dateTime().format(DateTimeFormatter.ofPattern("HHmmss")), 6));																						//152 a 157
		txt.print(StringUtils.ajustString("0", 9, '0', true));																									//158 a 166
		txt.print(StringUtils.ajustString("0", 5, '0', true));																									//167 a 171
		txt.print(StringUtils.space(69));																								//172 a 240
		txt.newLine();

		def totalRegistros = 0;

		/**
		 * TIPO 1 - HEADER DO LOTE
		 */
		txt.print(StringUtils.ajustString("341", 3));																											//001 a 003
		txt.print(StringUtils.ajustString(numLote, 4, '0', true));																								//004 a 007
		txt.print(StringUtils.ajustString("1", 1));																												//008 a 008
		txt.print(StringUtils.ajustString("C", 1));																												//009 a 009
		txt.print(StringUtils.ajustString("30", 2));																												//010 a 011
		txt.print(StringUtils.ajustString("01", 2));																												//012 a 013
		txt.print(StringUtils.ajustString("040", 3));																											//014 a 016
		txt.print(StringUtils.space(1));																								//017 a 017
		txt.print(StringUtils.ajustString(aac10.aac10ti == 0 ? 2 : 1, 1));																					//018 a 018
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true));															//019 a 032
		txt.print(StringUtils.ajustString("1707", 4));																											//033 a 036
		txt.print(StringUtils.space(16));																								//037 a 052
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true));														//053 a 057
		txt.print(StringUtils.space(1));																								//058 a 058
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true));														//059 a 070
		txt.print(StringUtils.space(1));																								//071 a 071
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abf01.abf01digConta), 1, '0', true));													//072 a 072
		txt.print(StringUtils.ajustString(StringUtils.unaccented(nomeAa65), 30));																			//073 a 102
		def loteItau = 1;
		txt.print(StringUtils.ajustString(loteItau, 2, '0', true));																								//103 a 104
		txt.print(StringUtils.space(28));																								//105 a 132
		txt.print(StringUtils.space(10));																								//133 a 142
		txt.print(StringUtils.ajustString(StringUtils.unaccented(enderecoAa65), 30));																		//143 a 172
		txt.print(StringUtils.ajustString(aac10.aac10numero, 5, '0', true));																					//173 a 177
		txt.print(StringUtils.ajustString(StringUtils.unaccented(complemAa65), 15));																			//178 a 192
		txt.print(StringUtils.ajustString(StringUtils.unaccented(municipioAa65), 20));																		//193 a 212
		txt.print(StringUtils.ajustString(StringUtils.unaccented(cepAa65), 8));																				//213 a 220
		txt.print(StringUtils.ajustString(StringUtils.unaccented(ufAa65), 2));																				//221 a 222
		txt.print(StringUtils.space(8));																								//223 a 230
		txt.print(StringUtils.space(10));																								//231 a 240
		txt.newLine();
		totalRegistros++;

		def totalPagtos = 0;
		for(int i = 0; i < abh80s.size(); i++) {
			Abh80 abh80 = getHoleriteUtils().findAbh80ByUniqueKey(abh80s.get(i).getString("abh80codigo"));
			def nomeAbh80 = abh80.abh80nome != null && abh80.abh80nome.length() > 30 ? abh80.abh80nome.substring(0, 30) : abh80.abh80nome;
			def nomeAbb11 = abh80.abh80depto.abb11nome.length() > 15 ? abh80.abh80depto.abb11nome.substring(0, 15) : abh80.abh80depto.abb11nome;
			def nomeAbh05 = abh80.abh80cargo.abh05nome.length() > 30 ? abh80.abh80cargo.abh05nome.substring(0, 30) : abh80.abh80cargo.abh05nome;

			//Rendimentos
			def totalRend = 0;
			List<TableMap> rsFba01011sRend = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(abh80.abh80id, critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, 
				sfp8001DtoExportar.comporTiposDeCalculos, 0, eveFerNaoImpr);
			if(rsFba01011sRend != null && rsFba01011sRend.size() > 0) {
				for(int j = 0; j < rsFba01011sRend.size(); j++) {
					totalRend = totalRend + rsFba01011sRend.get(j).getBigDecimal("valor");
				}
			}

			//Descontos
			def totalDesc = 0;
			List<TableMap> rsFba01011sDesc = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(abh80.abh80id, critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, 
				sfp8001DtoExportar.comporTiposDeCalculos, 1, eveFerNaoImpr);
			if(rsFba01011sDesc != null && rsFba01011sDesc.size() > 0) {
				for(int j = 0; j < rsFba01011sDesc.size(); j++) {
					totalDesc = totalDesc + rsFba01011sDesc.get(j).getBigDecimal("valor");
				}
			}

			//Férias e rescisão pagas.
			List<TableMap> mapFerResPagas = getHoleriteUtils().calcularFeriasRescisaoPagas(critAbb11, abh80.abh80id, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 
				eveFeriasLiquida, eveFeriasPagas, eveAbonoLiquido, eveAbonoPago, eveAd13Liquido, eveAd13Pago, eveResLiquida, eveResPaga);
			if(mapFerResPagas != null && mapFerResPagas.size() > 0) {
				for(int k = 0; k < mapFerResPagas.size(); k++) {
					totalDesc = totalDesc + mapFerResPagas.get(k).getBigDecimal("valor");
				}
			}

			//Líquido
			def totalLiquido = totalRend - totalDesc;

			/**
			 * SEGMENTO A - REGISTRO DETALHE
			 */
			txt.print(StringUtils.ajustString("341", 3));																										//001 a 003
			txt.print(StringUtils.ajustString(numLote, 4, '0', true));																							//004 a 007
			txt.print(StringUtils.ajustString("3", 1));																											//008 a 008
			txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																						//009 a 013
			txt.print(StringUtils.ajustString("A", 1));																											//014 a 014
			txt.print(StringUtils.ajustString("000", 3));																										//015 a 017
			txt.print(StringUtils.ajustString("000", 3));																										//018 a 020
			txt.print(StringUtils.ajustString("341", 3));																										//021 a 023
			txt.print(StringUtils.ajustString("0", 1));																											//024 a 024
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 4, '0', true));									//025 a 028
			txt.print(StringUtils.space(1));																							//029 a 029
			txt.print(StringUtils.ajustString("0", 6, '0', true));																								//030 a 035
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 6, '0', true));										//036 a 041
			txt.print(StringUtils.space(1));																							//042 a 042
			txt.print(StringUtils.ajustString(abh80s.get(i).getString("abh80bcoDigCta"), 1, '0', true));																//043 a 043
			txt.print(StringUtils.ajustString(nomeAbh80, 30));																									//044 a 073
			txt.print(StringUtils.space(20));																							//074 a 093
			txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8));																					//094 a 101
			txt.print(StringUtils.ajustString("REA", 3));																										//102 a 104
			txt.print(StringUtils.space(8));																							//105 a 112
			txt.print(StringUtils.ajustString("0", 7, '0', true));																								//113 a 119
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(totalLiquido)), 15, '0', true));										//120 a 134
			txt.print(StringUtils.space(15));																							//135 a 149
			txt.print(StringUtils.space(5));																							//150 a 154
			txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8));																					//155 a 162
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(totalLiquido)), 15, '0', true));										//163 a 177
			txt.print(StringUtils.space(20));																							//178 a 197
			txt.print(StringUtils.ajustString("0", 6, '0', true));																								//198 a 203
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(abh80s.get(i).getString("abh80cpf")), 14, '0', true));                              		//204 a 217
			txt.print(StringUtils.ajustString("21", 2));																											//218 a 219
			txt.print(StringUtils.space(5));																							//220 a 224
			txt.print(StringUtils.space(5));																							//225 a 229
			txt.print(StringUtils.ajustString("0", 1));																											//230 a 230
			txt.print(StringUtils.space(10));																							//231 a 240
			txt.newLine();
			totalRegistros++;

			def dataPGInicial = null;
			def dataPGFinal = null;
			Fbc0101 fbc0101 = getHoleriteUtils().findFbc0101UltimoCalculoByIdTrabalhadorAndTipoFerias(abh80.abh80id, 1);
			if(fbc0101 != null) {
				dataPGInicial = fbc0101.fbc0101pgi;
				dataPGFinal = fbc0101.fbc0101pgf;
			}

			def qtdeDepIR = getHoleriteUtils().findAbh8002QtdeDependentesIR(abh80.abh80id);
			def qtdeDepSF = getHoleriteUtils().findAbh8002QtdeDependentesSF(abh80.abh80id);

			def valorBaseINSS = getHoleriteUtils().calcularCAE(abh80.abh80id, critAbb11, caeBaseINSS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			def valorFGTS = getHoleriteUtils().calcularCAE(abh80.abh80id, critAbb11, caeFGTS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			def valorBaseIRRFSal = getHoleriteUtils().calcularCAE(abh80.abh80id, critAbb11, caeBaseIRRFSal, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			def valorBaseFGTS = getHoleriteUtils().calcularCAE(abh80.abh80id, critAbb11, caeBaseFGTS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);

			/**
			 * SEGMENTO D - REGISTRO DETALHE
			 */
			txt.print(StringUtils.ajustString("341", 3));																										//001 a 003
			txt.print(StringUtils.ajustString(numLote, 4, '0', true));																							//004 a 007
			txt.print(StringUtils.ajustString("3", 1));																											//008 a 008
			txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																						//009 a 013
			txt.print(StringUtils.ajustString("D", 1));																											//014 a 014
			txt.print(StringUtils.space(3));																							//015 a 017
			txt.print(StringUtils.ajustString(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("MMyyyy")), 6));																					//018 a 023
			txt.print(StringUtils.ajustString(StringUtils.unaccented(nomeAbb11), 15));																		//024 a 038
			txt.print(StringUtils.ajustString(abh80s.get(i).getString("abh80codigo"), 15));																			//039 a 053
			txt.print(StringUtils.ajustString(StringUtils.unaccented(nomeAbh05), 30));																		//054 a 083
			txt.print(StringUtils.ajustString(dataPGInicial != null ? sdfData.format(dataPGInicial) : 0, 8, '0', true));											//084 a 091
			txt.print(StringUtils.ajustString(dataPGFinal != null ? sdfData.format(dataPGFinal) : 0, 8, '0', true));												//092 a 099
			txt.print(StringUtils.ajustString(qtdeDepIR, 2, '0', true));																							//100 a 101
			txt.print(StringUtils.ajustString(qtdeDepSF, 2, '0', true));																							//102 a 103
			txt.print(StringUtils.ajustString(abh80s.get(i).getInteger("abh80hs"), 2, '0', true));																	//104 a 105
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseINSS.round(2))), 15, '0', true));									//106 a 120
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorFGTS.round(2))), 15, '0', true));										//121 a 135
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(totalRend)), 15, '0', true));										//136 a 150
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(totalDesc)), 15, '0', true));										//151 a 165
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(totalLiquido)), 15, '0', true));										//166 a 180
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(abh80s.get(i).getBigDecimal("abh80salario").round(2))), 15, '0', true));				//181 a 195
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseIRRFSal.round(2))), 15, '0', true));									//196 a 210
			txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valorBaseFGTS.round(2))), 15, '0', true));									//211 a 225
			txt.print(StringUtils.ajustString("00", 2));																											//226 a 227
			txt.print(StringUtils.space(3));																							//228 a 230
			txt.print(StringUtils.space(10));																							//231 a 240
			txt.newLine();
			totalRegistros++;

			if(rsFba01011sRend != null && rsFba01011sRend.size() > 0) {
				for(int j = 0; j < rsFba01011sRend.size(); j++) {
					/**
					 * SEGMENTO E - REGISTRO DETALHE
					 */
					gerarSegmentoEItau(txt, numLote, sequencial, 1, rsFba01011sRend.get(j).getString("abh21nome"), rsFba01011sRend.get(j).getBigDecimal("valor"));
					totalRegistros++;
				}
			}

			if(rsFba01011sDesc != null && rsFba01011sDesc.size() > 0) {
				for(int j = 0; j < rsFba01011sDesc.size(); j++) {
					/**
					 * SEGMENTO E - REGISTRO DETALHE
					 */
					gerarSegmentoEItau(txt, numLote, sequencial, 2, rsFba01011sDesc.get(j).getString("abh21nome"), rsFba01011sDesc.get(j).getBigDecimal("valor"));
					totalRegistros++;
				}
			}

			if(mapFerResPagas != null && mapFerResPagas.size() > 0) {
				for(int k = 0; k < mapFerResPagas.size(); k++) {
					gerarSegmentoEItau(txt, numLote, sequencial, 2, mapFerResPagas.get(k).getString("nome"), mapFerResPagas.get(k).getBigDecimal("valor"));
					totalRegistros++;
				}
			}

//			for(String mensagem : mensagens) {
//				if(mensagem == null) continue;
//
//				/**
//				 * SEGMENTO F - REGISTRO DETALHE
//				 */
//				txt.print("341", 3);																									//001 a 003
//				txt.print(numLote, 4, '0', true);																						//004 a 007
//				txt.print("3", 1);																										//008 a 008
//				txt.print(sequencial, 5, '0', true);																					//009 a 013
//				txt.print("F", 1);																										//014 a 014
//				txt.print(StringUtils.space(3));																						//015 a 017
//				txt.print(mensagem, 144);																								//018 a 161
//				txt.print(StringUtils.space(69));																						//162 a 230
//				txt.print(StringUtils.space(10));																						//231 a 240
//				txt.newLine();
//				totalRegistros++;
//			}

			sequencial++;
			totalPagtos = totalPagtos + totalLiquido;
		}

		/**
		 * TIPO 5 - TRAILER DO LOTE
		 */
		txt.print(StringUtils.ajustString("341", 3));																											//001 a 003
		txt.print(StringUtils.ajustString(numLote, 4, '0', true));																								//004 a 007
		txt.print(StringUtils.ajustString("5", 1));																												//008 a 008
		txt.print(StringUtils.space(9));																								//009 a 017
		txt.print(StringUtils.ajustString(totalRegistros+1, 6, '0', true));																						//018 a 023
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(totalPagtos)), 18, '0', true));											//024 a 041
		txt.print(StringUtils.ajustString("0", 18, '0', true));																									//042 a 059
		txt.print(StringUtils.space(171));																								//060 a 230
		txt.print(StringUtils.space(10));																								//231 a 240
		txt.newLine();

		/**
		 * TIPO 9 - TRAILER DO ARQUIVO
		 */
		txt.print(StringUtils.ajustString("341", 3));																											//001 a 003
		txt.print(StringUtils.ajustString("9999", 4, '0', true));																								//004 a 007
		txt.print(StringUtils.ajustString("9", 1));																												//008 a 008
		txt.print(StringUtils.space(9));																								//009 a 017
		txt.print(StringUtils.ajustString("1", 6, '0', true));																									//018 a 023
		txt.print(StringUtils.ajustString(totalRegistros+3, 6, '0', true));																						//024 a 029
		txt.print(StringUtils.space(211));																								//030 a 240
		txt.newLine();
		
		put("txt", txt);
	}
	
	private void gerarSegmentoEItau(TextFile txt, Integer numLote, int sequencial, int tipoMov, String descricao, BigDecimal valor) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		txt.print(StringUtils.ajustString("341", 3));																														//001 a 003
		txt.print(StringUtils.ajustString(numLote, 4, '0', true));																											//004 a 007
		txt.print(StringUtils.ajustString("3", 1));																															//008 a 008
		txt.print(StringUtils.ajustString(sequencial, 5, '0', true));																										//009 a 013
		txt.print(StringUtils.ajustString("E", 1));																															//014 a 014
		txt.print(StringUtils.space(3));																											//015 a 017
		txt.print(StringUtils.ajustString(tipoMov, 1));																														//018 a 018
		txt.print(StringUtils.ajustString(StringUtils.unaccented(descricao), 30));                          		 														//019 a 048
		txt.print(StringUtils.space(5));																											//049 a 053
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(nb.format(valor.round(2))), 15, '0', true));															//054 a 068
		txt.print(StringUtils.space(30));																											//069 a 098
		txt.print(StringUtils.space(5));																											//099 a 103
		txt.print(StringUtils.ajustString("0", 15, '0', true));																												//104 a 118
		txt.print(StringUtils.space(30));																											//119 a 148
		txt.print(StringUtils.space(5));																											//149 a 153
		txt.print(StringUtils.ajustString("0", 15, '0', true));																												//154 a 168
		txt.print(StringUtils.space(30));																											//169 a 198
		txt.print(StringUtils.space(5));																											//199 a 203
		txt.print(StringUtils.ajustString("0", 15, '0', true));																												//204 a 218
		txt.print(StringUtils.space(12));																											//219 a 230
		txt.print(StringUtils.space(10));																											//231 a 240
		txt.newLine();
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==