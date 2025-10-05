package multitec.formulas.sfp.pagto;

import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.ab.Abf01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ESocialUtils

class SFP_LayoutHSBC extends FormulaBase {
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

		def nomeEmpresa = aac10.aac10rs != null && aac10.aac10rs.length() > 30 ? aac10.aac10rs.substring(0, 30) : aac10.aac10rs;
		def enderecoEmpresa = aac10.aac10endereco != null && aac10.aac10endereco.length() > 30 ? aac10.aac10endereco.substring(0, 30) : aac10.aac10endereco;
		def complemEmpresa = aac10.aac10complem != null && aac10.aac10complem.length() > 15 ? aac10.aac10complem.substring(0, 15) : aac10.aac10complem;
		def cidadeEmpresa = aac10.aac10municipio != null && aac10.aac10municipio.aag0201nome.length() > 20 ? aac10.aac10municipio.aag0201nome.substring(0, 20) : aac10.aac10municipio.aag0201nome;
		def cepEmpresa = aac10.aac10cep != null && aac10.aac10cep.length() == 8 ? aac10.aac10cep.substring(0, 5) : "";
		def cepEmpresaComp = aac10.aac10cep != null && aac10.aac10cep.length() == 8 ? aac10.aac10cep.substring(5, 8) : "";
		Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", aac10.aac10municipio.aag0201uf.aag02id);
		def ufEmpresa = aag02 != null ? aag02.aag02uf : "";

		def numContrato = "";

		def reg = abf01.abf01json.getInteger("registro");
		def pi = abf01.abf01json.getInteger("pi");
		def conteudo = abf01.abf01json.getInteger("conteudo");

		if(reg == 0 && pi == 33) {
			numContrato = conteudo;
		}

		TextFile txt = new TextFile();

		/**
		 * HEADER DO ARQUIVO
		 */
		txt.print("399", 3);																									//001 a 003
		txt.print("0000", 4);																									//004 a 007
		txt.print("0");																										//008 a 008
		txt.print(StringUtils.space(9));																						//009 a 017
		txt.print("1");																										//018 a 018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);													//019 a 032
		txt.print(numContrato, 6, '0', true);																					//033 a 038
		txt.print(StringUtils.space(14));																						//039 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);										//053 a 057
		txt.print(StringUtils.space(1));																						//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);											//059 a 070
		txt.print(abf01.abf01digConta, 1);																				//071 a 071
		txt.print(StringUtils.space(1));																						//072 a 072
		txt.print(nomeEmpresa, 30);																								//073 a 102
		txt.print("HSBC", 30);																									//103 a 132
		txt.print(StringUtils.space(10));																						//133 a 142
		txt.print("1");																										//143 a 143
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																			//144 a 151
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("hhmmss")), 6);																			//152 a 157
		txt.print(sequencial, 6, '0', true);																					//158 a 163
		txt.print("020", 3);																									//164 a 166
		txt.print("01600", 5);																									//167 a 171
		txt.print("CPG", 3);																									//172 a 174
		txt.print("Y2K", 3);																									//175 a 177
		txt.print(StringUtils.space(3));																						//178 a 180
		txt.print(StringUtils.space(11));																						//181 a 191
		txt.print(StringUtils.space(49));																						//192 a 240
		txt.newLine();

		/**
		 * HEADER DO LOTE
		 */
		txt.print("399", 3);																									//001 a 003
		txt.print("0000", 4);																									//004 a 007
		txt.print("1", 1);																										//008 a 008
		txt.print("C", 1);																										//009 a 009
		txt.print("30", 2);																										//010 a 011
		txt.print("01", 2);																										//012 a 013
		txt.print("020", 3);																									//014 a 016
		txt.print(StringUtils.space(1));																						//017 a 017
		txt.print("1", 1);																										//018 a 018
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);													//019 a 032
		txt.print(numContrato, 6, '0', true);																					//033 a 038
		txt.print(StringUtils.space(14));																						//039 a 052
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);										//053 a 057
		txt.print(StringUtils.space(1));																						//058 a 058
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 12, '0', true);											//059 a 070
		txt.print(abf01.abf01digConta, 1);																				//071 a 071
		txt.print(StringUtils.space(1));																						//072 a 072
		txt.print(nomeEmpresa, 30);																								//073 a 102
		txt.print(StringUtils.space(40));																						//103 a 142
		txt.print(enderecoEmpresa, 30);																							//143 a 172
		txt.print(aac10.aac10numero, 5, '0', true);																			//173 a 177
		txt.print(complemEmpresa, 15);																							//178 a 192
		txt.print(cidadeEmpresa, 20);																							//193 a 212
		txt.print(cepEmpresa, 5, '0', true);																					//213 a 217
		txt.print(cepEmpresaComp, 3, '0', true);																				//218 a 220
		txt.print(ufEmpresa, 2);																								//221 a 222
		txt.print("N", 1);																										//223 a 223
		txt.print(StringUtils.space(17));																						//224 a 240
		txt.newLine();

		for(int i = 0; i < abh80s.size(); i++) {
			String nomeTrab = abh80s.get(i).getString("abh80nome") != null && abh80s.get(i).getString("abh80nome").length() > 30 ? abh80s.get(i).getString("abh80nome").substring(0, 30) : abh80s.get(i).getString("abh80nome");

			/**
			 * DETALHE
			 */
			txt.print("399", 3);																												//001 a 003
			txt.print("0001", 4);																												//004 a 007
			txt.print("3", 1);																													//008 a 008
			txt.print(sequencial, 5, '0', true);																								//009 a 013
			txt.print("A", 1);																													//014 a 014
			txt.print("0", 1);																													//015 a 015
			txt.print("00", 2);																													//016 a 017
			txt.print("000", 3);																												//018 a 020
			txt.print(abf01.abf01numero != null ? Integer.parseInt(StringUtils.extractNumbers(abf01.abf01numero)) : "", 3);		//021 a 023
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 5, '0', true);											//024 a 028
			txt.print(StringUtils.space(1));																									//029 a 029
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 12, '0', true);											//030 a 041
			txt.print(abh80s.get(i).getString("abh80bcoDigCta"), 1);																					//042 a 042
			txt.print(StringUtils.space(1));																									//043 a 043
			txt.print(nomeTrab, 30);																											//044 a 073
			txt.print(abh80s.get(i).getString("abh80codigo"), 16, '0', true);																		//074 a 089
			txt.print(StringUtils.space(4));																									//090 a 093
			txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);																						//094 a 101
			txt.print("R\$", 3);																													//102 a 104
			txt.print(StringUtils.space(17));																									//105 a 121
			txt.print(StringUtils.extractNumbers(formatarDecimal(abh80s.get(i).getBigDecimal("valor"), 2, false)), 13, '0', true);							//122 a 134
			txt.print("N", 1);																													//135 a 135
			txt.print(StringUtils.space(30));																									//136 a 165
			txt.print(StringUtils.space(11));																									//166 a 176
			txt.print(StringUtils.space(40));																									//177 a 216
			txt.print(StringUtils.space(12));																									//217 a 228
			txt.print("N", 1);																													//229 a 229
			txt.print(StringUtils.space(11));																									//230 a 240
			txt.newLine();
			sequencial++;
			total = total + abh80s.get(i).getBigDecimal("valor");
		}
		
		/**
		 * TRAILLER DO LOTE
		 */
		txt.print("399", 3);																													//001 a 003
		txt.print("0001", 4);																													//004 a 007
		txt.print("5", 1);																														//008 a 008
		txt.print(StringUtils.space(9));																										//009 a 017
		txt.print(sequencial +1, 6, '0', true);																									//018 a 023
		txt.print(StringUtils.space(3));																										//024 a 026
		txt.print(StringUtils.extractNumbers(abh80s.get(i).getBigDecimal("valor")), 15, '0', true);														//027 a 041
		txt.print(StringUtils.space(199));																										//042 a 240
		txt.newLine();
		
		/**
		 * TRAILLER DO ARQUIVO
		 */
		txt.print("399", 3);																													//001 a 003
		txt.print("9999", 4);																													//004 a 007
		txt.print("9", 1);																														//008 a 008
		txt.print(StringUtils.space(9));																										//009 a 017
		txt.print("000001", 6);																													//018 a 023
		txt.print(sequencial +3, 6, '0', true);																									//024 a 029
		txt.print(StringUtils.space(211));																										//030 a 240
		txt.newLine();
		
		put("txt", txt);
	}
	
	private BigDecimal formatarDecimal(BigDecimal valor, int casasDecimais, boolean valorZeroRetornaNull) {
		return ESocialUtils.formatarDecimal(valor, casasDecimais, valorZeroRetornaNull)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==