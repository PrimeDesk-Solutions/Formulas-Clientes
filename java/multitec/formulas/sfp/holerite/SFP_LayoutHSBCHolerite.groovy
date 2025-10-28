package multitec.formulas.sfp.holerite;

import java.text.NumberFormat
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abf01
import sam.model.entities.ab.Abh80
import sam.server.samdev.formula.FormulaBase

class SFP_LayoutHSBCHolerite extends FormulaBase {
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
		
		def sequencial = 1;

		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);

		def nomeAac10 = aac10.aac10rs.length() > 30 ? aac10.aac10rs.substring(0, 30) : aac10.aac10rs;
		def enderecoAac10 = aac10.aac10endereco.length() > 30 ? aac10.aac10endereco.substring(0, 30) : aac10.aac10endereco;
		def complemAac10 = aac10.aac10complem != null && aac10.aac10complem.length() > 15 ? aac10.aac10complem.substring(0, 15) : aac10.aac10complem;
		def municipioAac10 = aac10.aac10municipio.aag0201nome.length() > 20 ? aac10.aac10municipio.aag0201nome.substring(0, 20) : aac10.aac10municipio.aag0201nome;
		def cepAac10 = aac10.aac10cep != null ? aac10.aac10cep : "";
		def ufAac10 = aac10.aac10municipio.aag0201uf.aag02uf;
		def numLote = 1;

		TextFile txt = new TextFile();

		/**
		 * TIPO 0 - HEADER DO ARQUIVO
		 */
		txt.print("399", 3);																											//001 a 003
		txt.print("0000", 4);																											//004 a 007
		txt.print("0", 1);																												//008 a 008
		txt.print(StringUtils.space(9));																								//009 a 017
		txt.print("2", 1);																												//018 a 018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);															//019 a 032
		def numConvenioHSBC = 0;
		txt.print(numConvenioHSBC, 6, '0', true);																						//033 a 038
		txt.print(StringUtils.space(14));																								//039 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);														//053 a 057
		txt.print(StringUtils.space(1));																								//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);														//059 a 070
		txt.print(StringUtils.extractNumbers(abf01.abf01digConta), 1, '0', true);													//071 a 071
		txt.print(StringUtils.space(1));																								//072 a 072
		txt.print(StringUtils.unaccented(nomeAac10), 30);																			//073 a 102
		txt.print(StringUtils.unaccented(abf01.abf01nome), 30);																	//103 a 132
		txt.print(StringUtils.space(10));																								//133 a 142
		txt.print("1");																												//143 a 143
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																						//144 a 151
		txt.print(MDate.dateTime().format(DateTimeFormatter.ofPattern("hhmmss")), 6);																						//152 a 157
		txt.print(sequencial, 6, '0', true);																							//158 a 163
		txt.print("010", 3);																											//164 a 166
		txt.print("01600", 5);																											//167 a 171
		txt.print("PAY", 3);																											//172 a 174
		txt.print("Y2K", 3);																											//175 a 177
		txt.print(StringUtils.space(3));																								//178 a 180
		txt.print(StringUtils.space(11));																								//181 a 191
		txt.print(StringUtils.space(49));																								//192 a 240
		txt.newLine();

		int totalRegistros = 0;

		/**
		 * TIPO 1 - HEADER DE LOTE
		 */
		txt.print("399", 3);																											//001 a 003
		txt.print("0001", 4);																											//004 a 007
		txt.print("1", 1);																												//008 a 008
		txt.print("E", 1);																												//009 a 009
		def tipoServHSBC = 0;
		txt.print(tipoServHSBC, 2, '0', true);																							//010 a 011
		txt.print(StringUtils.space(2));																								//012 a 013
		txt.print("010", 3);																											//014 a 016
		txt.print("H", 1);																												//017 a 017
		txt.print("2", 1);																												//018 a 018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);															//019 a 032
		txt.print(numConvenioHSBC, 6, '0', true);																						//033 a 038
		txt.print(StringUtils.space(14));																								//039 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);														//053 a 057
		txt.print(StringUtils.space(1));																								//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);														//059 a 070
		txt.print(StringUtils.extractNumbers(abf01.abf01digConta), 1, '0', true);													//071 a 071
		txt.print(StringUtils.space(1));																								//072 a 072
		txt.print(StringUtils.unaccented(nomeAac10), 30);																			//073 a 102
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																						//103 a 110
		txt.print(StringUtils.space(32));																								//111 a 142
		txt.print(StringUtils.unaccented(enderecoAac10), 30);																		//143 a 172
		txt.print(aac10.aac10numero, 5, '0', true);																					//173 a 177
		txt.print(StringUtils.unaccented(complemAac10), 15);																			//178 a 192
		txt.print(StringUtils.unaccented(municipioAac10), 20);																		//193 a 212
		txt.print(StringUtils.unaccented(cepAac10), 8);																				//213 a 220
		txt.print(StringUtils.unaccented(ufAac10), 2);																				//221 a 222
		txt.print(StringUtils.space(1));																								//223 a 223
		txt.print(StringUtils.space(17));																								//224 a 240
		txt.newLine();
		totalRegistros++;

		for(int i = 0; i < abh80s.size(); i++) {
			Abh80 abh80 = getHoleriteUtils().findAbh80ByUniqueKey(abh80s.get(i).getString("abh80codigo"));
			def nomeAbh80 = abh80.abh80nome != null && abh80.abh80nome.length() > 30 ? abh80.abh80nome.substring(0, 30) : abh80.abh80nome;
			def nomeAbb11 = abh80.abh80depto.abb11nome.length() > 25 ? abh80.abh80depto.abb11nome.substring(0, 25) : abh80.abh80depto.abb11nome;
			def nomeAbh05 = abh80.abh80cargo.abh05nome.length() > 20 ? abh80.abh80cargo.abh05nome.substring(0, 20) : abh80.abh80cargo.abh05nome;

			def seqSegmentos = 1;

			/**
			 * SEGMENTO A/H - DETALHE
			 */
			txt.print("399", 3);																										//001 a 003
			txt.print("0003", 4);																										//004 a 007
			txt.print("3", 1);																											//008 a 008
			txt.print(seqSegmentos, 5, '0', true);																						//009 a 013
			txt.print("A", 1);																											//014 a 014
			def operacaoHSBC = 0;
			txt.print(operacaoHSBC, 1);																									//015 a 015
			txt.print(operacaoHSBC == 5 ? "55" : "00", 2);																				//016 a 017
			txt.print("H", 1);																											//018 a 018
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 5, '0', true);									//019 a 023
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 12, '0', true);									//024 a 035
			txt.print(abh80s.get(i).getString("abh80bcoDigCta"), 1, '0', true);																//036 a 036
			txt.print(StringUtils.space(1));																							//037 a 037
			txt.print(nomeAbh80, 30);																									//038 a 067
			txt.print(0, 16, '0', true);																								//068 a 083
			txt.print(abh80.abh80codigo, 10);																						//084 a 093
			txt.print(nomeAbh05, 20);																									//094 a 113
			txt.print(1, 1);																											//114 a 114
			txt.print(abh80.abh80cpf, 11, '0', true);																				//115 a 125
			txt.print(sfp8001DtoExportar.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																					//126 a 133
			txt.print(sfp8001DtoExportar.dataFinal.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																					//134 a 141
			txt.print(StringUtils.space(2));																							//142 a 143
			txt.print(nomeAbb11, 25);																									//144 a 168
			txt.print(StringUtils.space(36));																							//169 a 204
			txt.print(StringUtils.space(36));																							//205 a 240
			txt.newLine();
			seqSegmentos++;
			totalRegistros++;

			def idAbh80 = abh80.abh80id;
			List<TableMap> mapEventos = new ArrayList();

			/**
			 * SEGMENTO C/H - REGISTRO DETALHE
			 */
			List<TableMap> rsFba01011sRend = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(idAbh80, critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 0, eveFerNaoImpr);
			comporMapaEventosHSBC(rsFba01011sRend, mapEventos);
			gerarSegmentoCDHSBC(txt, "0003", seqSegmentos, "C", operacaoHSBC, mapEventos);
			seqSegmentos = seqSegmentos + mapEventos.size();
			totalRegistros = totalRegistros + mapEventos.size();

			/**
			 * SEGMENTO D/H - REGISTRO DETALHE
			 */
			List<TableMap> rsFba01011sDesc = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(idAbh80, critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 1, eveFerNaoImpr);
			comporMapaEventosHSBC(rsFba01011sDesc, mapEventos);

			//Férias e rescisão pagas.
			List<TableMap> mapFerResPagas = getHoleriteUtils().calcularFeriasRescisaoPagas(critAbb11, idAbh80, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 
				eveFeriasLiquida, eveFeriasPagas, eveAbonoLiquido, eveAbonoPago, eveAd13Liquido, eveAd13Pago, eveResLiquida, eveResPaga);
			if(mapFerResPagas != null && mapFerResPagas.size() > 0) {
				int linha = mapEventos == null || mapEventos.size() == 0 ? 0 : mapEventos.size() -1;

				for(int k = 0; k < mapFerResPagas.size(); k++) {
					if(mapEventos == null || mapEventos.size() == 0 || mapEventos.get(linha).get("nome5") != null) {
						if(mapEventos.get(linha).get("nome5") != null) linha++;
					}

					setDadosMapHSBC(mapEventos, linha, mapFerResPagas.get(k).getString("nome"), mapFerResPagas.get(k).getBigDecimal("valor"));
				}
			}

			gerarSegmentoCDHSBC(txt, "0003", seqSegmentos, "D", operacaoHSBC, mapEventos);
			seqSegmentos = seqSegmentos + mapEventos.size();
			totalRegistros = totalRegistros + mapEventos.size();

			/**
			 * SEGMENTO O/H
			 */
			List<String> textos = new ArrayList<String>();
			def valorBaseINSS = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeBaseINSS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorBaseINSS > 0) textos.add(StringUtils.ajustString("Valor Base INSS", 30), "R\$", nb.format(valorBaseINSS.round(2)));
			
			def valorBaseINSS13 = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeBaseINSS13, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorBaseINSS13 > 0) textos.add(StringUtils.ajustString("Valor Base INSS 13º Salário", 30), "R\$", nb.format(valorBaseINSS13.round(2)));
			
			def valorBaseIRRFSal = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeBaseIRRFSal, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorBaseIRRFSal > 0) textos.add(StringUtils.ajustString("Valor Base IRRF Salário", 30), "R\$", nb.format(valorBaseIRRFSal.round(2)));
			
			def valorBaseIRRF13 = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeBaseIRRF13, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorBaseIRRF13 > 0) textos.add(StringUtils.ajustString("Valor Base IRRF 13º Salário", 30), "R\$", nb.format(valorBaseIRRF13.round(2)));
			
			def valorBaseIRRFFer = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeBaseIRRFFer, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorBaseIRRFFer > 0) textos.add(StringUtils.ajustString("Valor Base IRRF Férias", 30), "R\$", nb.format(valorBaseIRRFFer.round(2)));
			
			def valorBaseIRRFPPR = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeBaseIRRFPPR, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorBaseIRRFPPR > 0) textos.add(StringUtils.ajustString("Valor Base IRRF PPR", 30), "R\$", nb.format(valorBaseIRRFPPR.round(2)));
			
			def valorBaseFGTS = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeBaseFGTS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorBaseFGTS > 0) textos.add(StringUtils.ajustString("Valor Base FGTS", 30), "R\$", nb.format(valorBaseFGTS.round(2)));
			
			def valorFGTS = getHoleriteUtils().calcularCAE(idAbh80, critAbb11, caeFGTS, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(valorFGTS > 0) textos.add(StringUtils.ajustString("Valor FGTS", 30), "R\$", nb.format(valorFGTS.round(2)));
			
			def totalRend = 0;
			if(rsFba01011sRend != null && rsFba01011sRend.size() > 0) {
				for(int k = 0; k < rsFba01011sRend.size(); k++) {
					totalRend = totalRend + rsFba01011sRend.get(k).getBigDecimal("valor");
				}
			}
			textos.add(StringUtils.ajustString("Total de Rendimentos", 30) + "R\$" + nb.format(totalRend));
			
			def totalDesc = 0;
			if(rsFba01011sDesc != null && rsFba01011sDesc.size() > 0) {
				for(int k = 0; k < rsFba01011sDesc.size(); k++) {
					totalDesc = totalDesc + rsFba01011sDesc.get(k).getBigDecimal("valor");
				}
			}
			textos.add(StringUtils.ajustString("Total de Descontos", 30) + "R\$" + nb.format(totalDesc));
			
			def totalLiquido = totalRend - totalDesc;
			textos.add(StringUtils.ajustString("Total Líquido", 30) + "R\$" + nb.format(totalLiquido));
			
			List<TableMap> mapTextos = new ArrayList();
			int linha = 0;
			for(String texto : textos) {
				if(mapTextos.get(linha).get("texto5") != null) {
					linha++;
				}

				if(mapTextos.get(linha).get("texto1") == null) {
					TableMap tm = new TableMap();
					tm.put("texto1", texto);
					mapTextos.add(linha, tm);
				} else if(mapTextos.get(linha).get("texto2") == null) {
					TableMap tm = new TableMap();
					tm.put("texto2", texto);
					mapTextos.add(linha, tm);
				} else if(mapTextos.get(linha).get("texto3") == null) {
					TableMap tm = new TableMap();
					tm.put("texto3", texto);
					mapTextos.add(linha, tm);
				} else if(mapTextos.get(linha).get("texto4") == null) {
					TableMap tm = new TableMap();
					tm.put("texto4", texto);
					mapTextos.add(linha, tm);
				} else if(mapTextos.get(linha).get("texto5") == null) {
					TableMap tm = new TableMap();
					tm.put("texto5", texto);
					mapTextos.add(linha, tm);
				}
			}
			
			for(int k = 0; k < mapTextos.size(); k++) {
				txt.print("399", 3);																									//001 a 003
				txt.print("0003", 4);																									//004 a 007
				txt.print("3", 1);																										//008 a 008
				txt.print(seqSegmentos, 5, '0', true);																					//009 a 013
				txt.print("O", 1);																										//014 a 014
				txt.print(operacaoHSBC, 1);																								//015 a 015
				txt.print(operacaoHSBC == 5 ? "55" : "00", 2);																			//016 a 017
				txt.print("H", 1);																										//018 a 018
				txt.print(mapTextos.get(k).get("texto1"), 40);																		//019 a 058
				txt.print(StringUtils.space(1));																						//059 a 059
				txt.print(mapTextos.get(k).get("texto2"), 40);																		//060 a 099
				txt.print(StringUtils.space(1));																						//100 a 100
				txt.print(mapTextos.get(k).get("texto3"), 40);																		//101 a 140
				txt.print(StringUtils.space(1));																						//141 a 141
				txt.print(mapTextos.get(k).get("texto4"), 40);																		//142 a 181
				txt.print(StringUtils.space(1));																						//182 a 182
				txt.print(mapTextos.get(k).get("texto5"), 40);																		//183 a 222
				txt.print(StringUtils.space(1));																						//223 a 223
				txt.print(StringUtils.space(17));																						//224 a 240
				txt.newLine();
				seqSegmentos++;
				totalRegistros++;
			}

//			for(String mensagem : mensagens) {
//				if(mensagem == null) continue;
//				
//				/**
//				 * SEGMENTO T/H - REGISTRO DETALHE
//				 */
//				txt.print("399", 3);																									//001 a 003
//				txt.print("0003", 4);																									//004 a 007
//				txt.print("3", 1);																										//008 a 008
//				txt.print(seqSegmentos, 5, '0', true);																					//009 a 013
//				txt.print("T", 1);																										//014 a 014
//				txt.print(operacaoHSBC, 1);																								//015 a 015
//				txt.print(operacaoHSBC == 5 ? "55" : "00", 2);																			//016 a 017
//				txt.print("H", 1);																										//018 a 018
//				txt.print(mensagem, 40);																								//019 a 058
//				txt.print(StringUtils.space(1));																						//059 a 059
//				txt.print(StringUtils.space(40));																						//060 a 099
//				txt.print(StringUtils.space(1));																						//100 a 100
//				txt.print(StringUtils.space(40));																						//101 a 140
//				txt.print(StringUtils.space(1));																						//141 a 141
//				txt.print(StringUtils.space(40));																						//142 a 181
//				txt.print(StringUtils.space(1));																						//182 a 182
//				txt.print(StringUtils.space(40));																						//183 a 222
//				txt.print(StringUtils.space(1));																						//223 a 223
//				txt.print(StringUtils.space(17));																						//224 a 240
//				txt.newLine();
//				seqSegmentos++;
//				totalRegistros++;
//			}
		}
		
		/**
		 * TIPO 5 - TRAILLER DE LOTE
		 */
		txt.print("399", 3);																											//001 a 003
		txt.print("0005", 4);																											//004 a 007
		txt.print("5", 1);																												//008 a 008
		txt.print(StringUtils.space(9));																								//009 a 017
		txt.print(totalRegistros+1, 6, '0', true);																						//018 a 023
		txt.print(StringUtils.space(3));																								//024 a 026
		txt.print(StringUtils.space(15));																								//027 a 041
		txt.print(StringUtils.space(199));																								//042 a 240
		txt.newLine();
		
		/**
		 * TIPO 9 - TRAILLER DE ARQUIVO
		 */
		txt.print("399", 3);																											//001 a 003
		txt.print("9999", 4);																											//004 a 007
		txt.print("9", 1);																												//008 a 008
		txt.print(StringUtils.space(9));																								//009 a 017
		txt.print(1, 6, '0', true);																										//018 a 023
		txt.print(totalRegistros+3, 6, '0', true);																						//024 a 029
		txt.print(StringUtils.space(211));																								//030 a 240
		txt.newLine();
		
		put("txt", txt);
	}
	
	private void comporMapaEventosHSBC(List<TableMap> rs, List<TableMap> map) {
		map.clear();
		int linha = 0;

		if(rs != null && rs.size() > 0) {
			NumberFormat nb = NumberFormat.getNumberInstance();
			nb.setMinimumFractionDigits(1);
			nb.setGroupingUsed(false);
			
			for(int i = 0; i < rs.size(); i++) {
				if(i == 0 || map.get(linha).get("nome5") != null) {
					if(map.get(linha).get("nome5") != null) linha++;
				}

				StringBuilder evento = new StringBuilder();
				evento.append(rs.get(i).getString("abh21nome"));
				
				if(rs.get(i).getBigDecimal("ref") > 0) {
					if(evento.length() > 23) evento.setLength(23);
					String ref = nb.format(rs.get(i).getBigDecimal("ref").round(1));
					evento.append(StringUtils.space(29 - evento.length() - ref.length()));
					evento.append(ref);
				}
				setDadosMapHSBC(map, linha, evento.toString(), rs.get(i).getBigDecimal("valor"));
			}
		}
	}
	
	private void gerarSegmentoCDHSBC(TextFile txt, String numLote, int seqSegmentos, String segmento, int operacaoHSBC, List<TableMap> mapEventos) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		if(mapEventos != null && mapEventos.size() > 0) {
			for(int i = 0; i < mapEventos.size(); i++) {
				def valor1 = mapEventos.get(i).getBigDecimal("valor1");
				def valor2 = mapEventos.get(i).getBigDecimal("valor2");
				def valor3 = mapEventos.get(i).getBigDecimal("valor3");
				def valor4 = mapEventos.get(i).getBigDecimal("valor4");
				def valor5 = mapEventos.get(i).getBigDecimal("valor5");
				
				txt.print("399", 3);																												//001 a 003
				txt.print(numLote, 4);																												//004 a 007
				txt.print("3", 1);																													//008 a 008
				txt.print(seqSegmentos, 5, '0', true);																								//009 a 013
				txt.print(segmento, 1);																												//014 a 014
				txt.print(operacaoHSBC, 1);																											//015 a 015
				txt.print(operacaoHSBC == 5 ? "55" : "00", 2);																						//016 a 017
				txt.print("H", 1);																													//018 a 018
				txt.print(mapEventos.get(i).getString("nome1") != null ? mapEventos.get(i).getString("nome1") : "", 29);							//019 a 047
				txt.print("R\$", 2);																													//048 a 049
				txt.print(valor1 != null ? StringUtils.extractNumbers(nb.format(valor1)) : "", 8, '0', true);								//050 a 057
				txt.print(mapEventos.get(i).getString("nome2") != null ? mapEventos.get(i).getString("nome2") : "", 29);							//058 a 086
				txt.print("R\$", 2);																													//087 a 088
				txt.print(valor2 != null ? StringUtils.extractNumbers(nb.format(valor2)) : "", 8, '0', true);								//089 a 096
				txt.print(mapEventos.get(i).getString("nome3") != null ? mapEventos.get(i).getString("nome3") : "", 29);							//097 a 125
				txt.print("R\$", 2);																													//126 a 127
				txt.print(valor3 != null ? StringUtils.extractNumbers(nb.format(valor3)) : "", 8, '0', true);								//128 a 135
				txt.print(mapEventos.get(i).getString("nome4") != null ? mapEventos.get(i).getString("nome4") : "", 29);							//136 a 164
				txt.print("R\$", 2);																													//165 a 166
				txt.print(valor4 != null ? StringUtils.extractNumbers(nb.format(valor4)) : "", 8, '0', true);								//167 a 174
				txt.print(mapEventos.get(i).getString("nome5") != null ? mapEventos.get(i).getString("nome5") : "", 29);							//175 a 203
				txt.print("R\$", 2);																													//204 a 205
				txt.print(valor5 != null ? StringUtils.extractNumbers(nb.format(valor5)) : "", 8, '0', true);								//206 a 213
				txt.print(StringUtils.space(27));																									//214 a 240
				txt.newLine();
				seqSegmentos++;
			}
		}
	}
	
	private void setDadosMapHSBC(List<TableMap> map, int linha, String nome, def valor) {
		if(map.get(linha).get("nome1") == null) {
			TableMap tm = new TableMap();
			tm.put("nome1", nome);
			tm.put("valor1", valor);
			map.add(linha, tm);
		}else if(map.get(linha).get("nome2") == null) {
			TableMap tm = new TableMap();
			tm.put("nome2", nome);
			tm.put("valor2", valor);
			map.add(linha, tm);
		}else if(map.get(linha).get("nome3") == null) {
			TableMap tm = new TableMap();
			tm.put("nome3", nome);
			tm.put("valor3", valor);
			map.add(linha, tm);
		}else if(map.get(linha).get("nome4") == null) {
			TableMap tm = new TableMap();
			tm.put("nome4", nome);
			tm.put("valor4", valor);
			map.add(linha, tm);
		}else if(map.get(linha).get("nome5") == null) {
			TableMap tm = new TableMap();
			tm.put("nome5", nome);
			tm.put("valor5", valor);
			map.add(linha, tm);
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==