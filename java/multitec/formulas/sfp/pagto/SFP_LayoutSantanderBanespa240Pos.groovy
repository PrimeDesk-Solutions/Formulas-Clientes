package multitec.formulas.sfp.pagto

import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.ab.Abf01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ESocialUtils

class SFP_LayoutSantanderBanespa240Pos extends FormulaBase {
	private SFP8001DtoExportar sfp8001DtoExportar;
	private List<TableMap> abh80s;
	private Abf01 abf01;
	private Aac10 aac10;
	private List<TableMap> mapParametros;

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
		this.mapParametros = get("mapParametros");

		def total = BigDecimal.ZERO;
		def sequencial = 1;
		
		DateTimeFormatter sdfData = DateTimeFormatter.ofPattern("ddMMyyyy");
		DateTimeFormatter sdfHora = DateTimeFormatter.ofPattern("hhmmss");

		def nomeEmpresa = aac10.aac10rs != null && aac10.aac10rs.length() > 30 ? aac10.aac10rs.substring(0, 30) : aac10.aac10rs;
		def enderecoEmpresa = aac10.aac10endereco != null && aac10.aac10endereco.length() > 30 ? aac10.aac10endereco.substring(0, 30) : aac10.aac10endereco;
		def complemEmpresa = aac10.aac10complem != null && aac10.aac10complem.length() > 15 ? aac10.aac10complem.substring(0, 15) : aac10.aac10complem;
		def cidadeEmpresa = aac10.aac10municipio != null && aac10.aac10municipio.aag0201nome.length() > 20 ? aac10.aac10municipio.aag0201nome().substring(0, 20) : aac10.aac10municipio.aag0201nome;
		def cepEmpresa = aac10.aac10cep != null ? aac10.aac10cep : "";
		Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", aac10.aac10municipio.aag0201uf.aag02id);
		def ufEmpresa = aag02 != null ? aag02.aag02uf : "";

		def codConvBanco = "";
		def formaLcto = "";
		def tipoMovim = "";
		def codInstrMov = "";
		def codCamaraComp = "";
		def finalidadeDoc = "";
		def reg = abf01.abf01json.getInteger("registro");
		def pi = abf01.abf01json.getInteger("pi");
		def conteudo = abf01.abf01json.getInteger("conteudo");

		if(reg == 0 && pi == 33) {
			codConvBanco = conteudo;
		}else if(reg == 1 && pi == 12) {
			formaLcto = conteudo;
		}else if(reg == 3 && pi == 15) {
			tipoMovim = conteudo;
		}else if(reg == 3 && pi == 16) {
			codInstrMov = conteudo;
		}else if(reg == 3 && pi == 18) {
			codCamaraComp = conteudo;
		}else if(reg == 3 && pi == 218) {
			finalidadeDoc = conteudo;
		}
		
		TextFile txt = new TextFile();

		/**
		 * HEADER DO ARQUIVO
		 */
		txt.print("033");																								//001 a 003
		txt.print("0000");																								//004 a 007
		txt.print(0);																									//008 a 008
		txt.print(StringUtils.space(9));																					//009 a 017
		txt.print(aac10.aac10ti == 0 ? 2 : 1, 1);																		//018 a 018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);												//019 a 032
		txt.print(codConvBanco, 20);																						//033 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);									//053 a 057
		txt.print(StringUtils.space(1));																					//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);										//059 a 070
		txt.print(abf01.abf01digConta, 1);																			//071 a 071
		txt.print(StringUtils.space(1));																					//072 a 072
		txt.print(StringUtils.unaccented(nomeEmpresa), 30);																//073 a 102
		txt.print("Banco Santander Banespa", 30);																			//103 a 132
		txt.print(StringUtils.space(10));																					//133 a 142
		txt.print(1);																									//143 a 143
		txt.print(MDate.dateTime().format(sdfData), 8);																			//144 a 151
		txt.print(MDate.dateTime().format(sdfHora), 6);																			//152 a 157
		txt.print("000011");																								//158 a 163
		txt.print("060");																								//164 a 166
		txt.print(0, 5, '0', true);																							//167 a 171
		txt.print(StringUtils.space(20));																					//172 a 191
		txt.print(StringUtils.space(20));																					//192 a 211
		txt.print(StringUtils.space(19));																					//212 a 230
		txt.print(StringUtils.space(10));																					//231 a 240
		txt.newLine();

		/**
		 * HEADER DO LOTE
		 */
		txt.print("033");																								//001 a 003
		txt.print("0001");																								//004 a 007
		txt.print(1);																									//008 a 008
		txt.print("C");																									//009 a 009
		txt.print(30);																									//010 a 011
		txt.print(formaLcto, 2, '0', true);																					//012 a 013
		txt.print("031");																								//014 a 016
		txt.print(StringUtils.space(1));																					//017 a 017
		txt.print(aac10.aac10ti == 0 ? 2 : 1, 1);																		//018 a 018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);												//019 a 032
		txt.print(codConvBanco, 20);																						//033 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);									//053 a 057
		txt.print(StringUtils.space(1));																					//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);										//059 a 070
		txt.print(abf01.abf01digConta, 1);																			//071 a 071
		txt.print(StringUtils.space(1));																					//072 a 072
		txt.print(StringUtils.extractNumbers(nomeEmpresa), 30);																//073 a 102
		txt.print(StringUtils.space(40));																					//103 a 142
		txt.print(StringUtils.unaccented(enderecoEmpresa), 30);															//143 a 172
		txt.print(aac10.aac10numero, 5, '0', true);																		//173 a 177
		txt.print(StringUtils.unaccented(complemEmpresa), 15);															//178 a 192
		txt.print(StringUtils.unaccented(cidadeEmpresa), 20);															//193 a 212
		txt.print(StringUtils.extractNumbers(cepEmpresa), 8, '0', true);													//213 a 220
		txt.print(ufEmpresa, 2);																							//221 a 222
		txt.print(StringUtils.space(8));																					//223 a 230
		txt.print(StringUtils.space(10));																					//231 a 240
		txt.newLine();

		/**
		 * DETALHE
		 */
		for(int i = 0; i < abh80s.size(); i++) {
			def nomeTrab = abh80s.get(i).getString("abh80nome") != null && abh80s.get(i).getString("abh80nome").length() > 30 ? abh80s.get(i).getString("abh80nome").substring(0, 30) : abh80s.get(i).getString("abh80nome");
			def numDocCli = abh80s.get(i).getString("abh80cpf") != null ? StringUtils.extractNumbers(abh80s.get(i).getString("abh80cpf")) + abh80s.get(i).getString("abh80codigo") : abh80s.get(i).getString("abh80codigo");
			def enderecoTrab = abh80s.get(i).getString("abh80endereco") != null && abh80s.get(i).getString("abh80endereco").length() > 30 ? abh80s.get(i).getString("abh80endereco").substring(0, 30) : abh80s.get(i).getString("abh80endereco");
			def complemTrab = abh80s.get(i).getString("abh80complem") != null && abh80s.get(i).getString("abh80complem").length() > 15 ? abh80s.get(i).getString("abh80complem").substring(0, 15) : abh80s.get(i).getString("abh80complem");
			def cidadeTrab = abh80s.get(i).getString("aag0201nome") != null && abh80s.get(i).getString("aag0201nome").length() > 20 ? abh80s.get(i).getString("aag0201nome").substring(0, 20) : abh80s.get(i).getString("aag0201nome");
			def cepTrab = abh80s.get(i).getString("abh80cep") != null && abh80s.get(i).getString("abh80cep").length() == 8 ? abh80s.get(i).getString("abh80cep") : "";
			def ufTrab = abh80s.get(i).getString("aag02uf") != null ? abh80s.get(i).getString("aag02uf") : "";

			//SEGMENTO A
			txt.print("033");																							//001 a 003
			txt.print("0001");																							//004 a 007
			txt.print(3);																								//008 a 008
			txt.print(sequencial, 5, '0', true);																			//009 a 013
			txt.print("A");																								//014 a 014
			txt.print(tipoMovim, 1, '0', true);																				//015 a 015
			txt.print(codInstrMov, 2, '0', true);																			//016 a 017
			txt.print(codCamaraComp, 3, '0', true);																			//018 a 020
			txt.print(abf01.abf01codigo, 3, '0', true);																//021 a 023
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 5, '0', true);						//024 a 028
			txt.print(StringUtils.space(1));																				//029 a 029
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 12, '0', true);						//030 a 041
			txt.print(abh80s.get(i).getString("abh80bcoDigCta"), 1);																//042 a 042
			txt.print(StringUtils.space(1));																				//043 a 043
			txt.print(StringUtils.unaccented(nomeTrab), 30);															//044 a 073
			txt.print(numDocCli, 20);																						//074 a 093
			txt.print(sfp8001DtoExportar.data.format(sdfData), 8);																	//094 a 101
			txt.print("BRL");																							//102 a 104
			txt.print(0, 15, '0', true);																					//105 a 119
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("valor")), 15, '0', true);		//120 a 134
			txt.print(StringUtils.space(20));																				//135 a 154
			txt.print(sfp8001DtoExportar.data.format(sdfData), 8);																	//155 a 162
			txt.print(0, 15, '0', true);																					//163 a 177
			txt.print(StringUtils.space(40));																				//178 a 217
			txt.print(finalidadeDoc, 2);																					//218 a 219
			txt.print(StringUtils.space(10));																				//220 a 229
			txt.print("0");																								//230 a 230
			txt.print(StringUtils.space(10));																				//231 a 240
			txt.newLine();
			sequencial++;
			total = total + abh80s.get(i).getBigDecimal("valor");

			//SEGMENTO B
			txt.print("033");																							//001 a 003
			txt.print("0001");																							//004 a 007
			txt.print(3);																								//008 a 008
			txt.print(sequencial, 5, '0', true);																			//009 a 013
			txt.print("B");																								//014 a 014
			txt.print(StringUtils.space(3));																				//015 A 017
			txt.print(1);																								//018 a 018
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80cpf")), 14, '0', true);							//019 a 032
			txt.print(StringUtils.unaccented(enderecoTrab), 30);														//033 a 062
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80numero")), 5, '0', true);													//063 a 067
			txt.print(StringUtils.unaccented(complemTrab), 15);															//068 a 082
			txt.print(StringUtils.unaccented(abh80s.get(i).getString("abh80bairro")), 15);									//083 a 097
			txt.print(StringUtils.unaccented(cidadeTrab), 20);															//098 a 117
			txt.print(StringUtils.extractNumbers(cepTrab), 8, '0', true);													//118 a 125
			txt.print(ufTrab, 2);																							//126 a 127
			txt.print(sfp8001DtoExportar.data.format(sdfData), 8);																	//128 a 135
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("valor")), 15, '0', true);		//136 a 150
			txt.print(0, 15, '0', true);																					//151 a 165
			txt.print(0, 15, '0', true);																					//166 a 180
			txt.print(0, 15, '0', true);																					//181 a 195
			txt.print(0, 15, '0', true);																					//196 a 210
			txt.print(0, 4, '0', true);																						//211 a 214
			txt.print(StringUtils.space(11));																				//215 a 225
			txt.print("2007", 5, '0', true);																				//226 a 230
			txt.print(StringUtils.space(10));																				//231 a 240
			txt.newLine();
			sequencial++;
		}

		/**
		 * TRAILLER DO LOTE
		 */
		txt.print("033");																								//001 a 003
		txt.print("0001");																								//004 a 007
		txt.print(5);																									//008 a 008
		txt.print(StringUtils.space(9));																					//009 a 017
		txt.print(sequencial + 1, 6, '0', true);																			//018 a 023
		txt.print(total, 18, '0', true);									//024 a 041
		txt.print(0, 18, '0', true);																						//042 a 059
		txt.print(0, 6, '0', true);																							//060 a 065
		txt.print(StringUtils.space(165));																					//066 a 230
		txt.print(StringUtils.space(10));																					//231 a 240
		txt.newLine();
		sequencial++;

		/**
		 * TRAILLER DO ARQUIVO
		 */
		txt.print("033");																								//001 a 003
		txt.print("9999");																								//004 a 007
		txt.print("9");																									//008 a 008
		txt.print(StringUtils.space(9));																					//009 a 017
		txt.print("000001");																								//018 a 023
		txt.print(sequencial + 2, 6, '0', true);																			//024 a 029
		txt.print(StringUtils.space(211));																					//030 a 240
		txt.newLine();
		
		put("txt", txt);
	}
	
	private BigDecimal formatarDecimal(BigDecimal valor, int casasDecimais, boolean valorZeroRetornaNull) {
		return ESocialUtils.formatarDecimal(valor, casasDecimais, valorZeroRetornaNull)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==