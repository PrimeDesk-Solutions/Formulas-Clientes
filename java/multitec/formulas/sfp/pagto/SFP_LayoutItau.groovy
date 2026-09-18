package multitec.formulas.sfp.pagto

import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abf01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ESocialUtils

class SFP_LayoutItau extends FormulaBase {
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
		def tipoPagto = "";
		def formaPagto = "";
		def tipoInscricao = "";
		def numInscricao = "";
		def finalidadePagto = "";
		def complemHist = "";
		def endereco = "";
		def numero = "";
		def complemento = "";
		def cidade = "";
		def cep = "";
		def estado = "";

		def pi = abf01.abf01json.getInteger("pi");
		def conteudo = abf01.abf01json.getInteger("conteudo");

		if(abf01.abf01json.getInteger("registro") == 1) {
			if(pi == 10) {
				tipoPagto = conteudo;
			}else if(pi == 12) {
				formaPagto = conteudo;
			}else if(pi == 18) {
				tipoInscricao = conteudo;
			}else if(pi == 19) {
				numInscricao = conteudo;
			}else if(pi == 103) {
				finalidadePagto = conteudo;
			}else if(pi == 133) {
				complemHist = conteudo;
			}else if(pi == 143) {
				endereco = conteudo;
			}else if(pi == 173) {
				numero = conteudo;
			}else if(pi == 178) {
				complemento = conteudo;
			}else if(pi == 193) {
				cidade = conteudo;
			}else if(pi == 213) {
				cep = conteudo;
			}else if(pi == 221) {
				estado = conteudo;
			}
		}
		
		TextFile txt = new TextFile();

		/**
		 * HEADER DO ARQUIVO
		 */
		txt.print(341, 3);																									//001 a 003
		txt.print("0000", 4);																								//004 a 007
		txt.print(0, 1);																									//008 a 008
		txt.print(StringUtils.space(6));																					//009 a 014
		txt.print("040", 3);																								//015 a 017
		txt.print(aac10.aac10ti == 0 ? 2 : 1, 1);																		//018 a 018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);														//019 a 032
		txt.print(StringUtils.space(20));																					//033 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);									//053 a 057
		txt.print(StringUtils.space(1));																					//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);										//059 a 070
		txt.print(StringUtils.space(1));																					//071 a 071
		txt.print(abf01.abf01digConta, 1, '0', true);																//072 a 072
		txt.print(aac10.aac10rs, 30);																					//073 a 102
		txt.print("BANCO ITAU S/A - FP", 30);																				//103 a 132
		txt.print(StringUtils.space(10));																					//133 a 142
		txt.print(1, 1);																									//143 a 143
		txt.print(MDate.date().format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																			//144 a 151
		txt.print(MDate.dateTime().format(DateTimeFormatter.ofPattern("HHmmss")), 6);													//152 a 157
		txt.print(0, 9, '0', true);																							//158 a 166
		txt.print(0, 5, '0', true);																							//167 a 171
		txt.print(StringUtils.space(69));																					//172 a 240
		txt.newLine();

		/**
		 * HEADER DE LOTE
		 */
		txt.print(341, 3);																									//001 a 003
		txt.print("0001", 4);																								//004 a 007
		txt.print(1, 1);																									//008 a 008
		txt.print("C", 1);																									//009 a 009
		txt.print(tipoPagto, 2);																							//010 a 011
		txt.print(formaPagto, 2);																							//012 a 013
		txt.print("030", 3);																								//014 a 016
		txt.print(StringUtils.space(1));																					//017 a 017
		txt.print(tipoInscricao, 1);																						//018 a 018
		txt.print(numInscricao, 14);																						//019 a 032
		txt.print(StringUtils.space(20));																					//033 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);									//053 a 057
		txt.print(StringUtils.space(1));																					//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);										//059 a 070
		txt.print(StringUtils.space(1));																					//071 a 071
		txt.print(abf01.abf01digConta, 1, '0', true);																//072 a 072
		txt.print(aac10.aac10rs, 30);																					//073 a 102
		txt.print(finalidadePagto, 30);																						//103 a 132
		txt.print(complemHist, 10);																							//133 a 142
		txt.print(endereco, 30);																							//143 a 172
		txt.print(numero, 5);																								//173 a 177
		txt.print(complemento, 15);																							//178 a 192
		txt.print(cidade, 20);																								//193 a 212
		txt.print(cep, 8);																									//213 a 220
		txt.print(estado, 2);																								//221 a 222
		txt.print(StringUtils.space(8));																					//223 a 230
		txt.print(StringUtils.space(10));																					//231 a 240
		txt.newLine();

		/**
		 * DETALHE
		 */
		for(int i = 0; i < abh80s.size(); i++) {
			String nomeTrab = abh80s.get(i).getString("abh80nome") != null && abh80s.get(i).getString("abh80nome").length() > 30 ? abh80s.get(i).getString("abh80nome").substring(0, 30) : abh80s.get(i).getString("abh80nome");

			txt.print(341, 3);																								//001 a 003
			txt.print("0001", 4);																							//004 a 007
			txt.print(3, 1);																								//008 a 008
			txt.print(sequencial, 5, '0', true);																			//009 a 013
			txt.print("A", 1);																								//014 a 014
			txt.print(0, 3, '0', true);																						//015 a 017
			txt.print(0, 3, '0', true);																						//018 a 020
			txt.print(341, 3);																								//021 a 023
			txt.print(0, 1);																								//024 a 024
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 4, '0', true);						//025 a 028
			txt.print(StringUtils.space(1));																				//029 a 029
			txt.print(0, 7, '0', true);																						//030 a 036
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 5, '0', true);							//037 a 041
			txt.print(StringUtils.space(1));																				//042 a 042
			txt.print(abh80s.get(i).getString("abh80bcoDigCta"), 1, '0', true);													//043 a 043
			txt.print(nomeTrab, 30);																						//044 a 073
			txt.print(StringUtils.space(20));																				//074 a 093
			txt.print(MDate.date().format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																	//094 a 101
			txt.print("REA", 3);																							//102 a 104
			txt.print(0, 15);																								//105 a 119
			txt.print(StringUtils.extractNumbers(formatarDecimal(abh80s.get(i).getBigDecimal("valor"), 2, false)), 15, '0', true);		//120 a 134
			txt.print(StringUtils.space(15));																				//135 a 149
			txt.print(StringUtils.space(5));																				//150 a 154
			txt.print(0, 8, '0', true);																						//155 a 162
			txt.print(0, 15, '0', true);																					//163 a 177
			txt.print(StringUtils.space(18));																				//178 a 195
			txt.print(StringUtils.space(2));																				//196 a 197
			txt.print(0, 6, '0', true);																	 					//198 a 203
			txt.print(abh80s.get(i).getString("abh80cpf"), 14, '0', true);														//204 a 217
			txt.print(StringUtils.space(12));																				//218 a 229
			txt.print(0, 1);																								//230 a 230
			txt.print(StringUtils.space(10));																				//231 a 240
			txt.newLine();
			sequencial++;
			total = total + abh80s.get(i).getBigDecimal("valor");
		}

		/**
		 * TRAILER DO LOTE
		 */
		sequencial = sequencial + 1;

		txt.print(341, 3);																									//001 a 003
		txt.print("0001", 4);																								//004 a 007
		txt.print("5");																										//008 a 008
		txt.print(StringUtils.space(9));																					//009 a 017
		txt.print(sequencial, 6);																							//018 a 023
		txt.print(StringUtils.extractNumbers(formatarDecimal(total, 2, false)), 18, '0', true);									//024 a 041
		txt.print(0, 18, '0', true);																						//042 a 059
		txt.print(StringUtils.space(171));																					//060 a 230
		txt.print(StringUtils.space(10));																					//231 a 240
		txt.newLine();
		sequencial++;

		/**
		 * TRAILER DO ARQUIVO
		 */
		txt.print(341, 3);																									//001 a 003
		txt.print("9999", 4);																								//004 a 007
		txt.print(9, 1);																									//008 a 008
		txt.print(StringUtils.space(9));																					//009 a 017
		txt.print(1, 6);																									//018 a 023
		txt.print(sequencial + 1, 6);																						//024 a 029
		txt.print(StringUtils.space(211));																					//030 a 240
		txt.newLine();
		
		put("txt", txt);
	}
	
	private BigDecimal formatarDecimal(BigDecimal valor, int casasDecimais, boolean valorZeroRetornaNull) {
		return ESocialUtils.formatarDecimal(valor, casasDecimais, valorZeroRetornaNull)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==