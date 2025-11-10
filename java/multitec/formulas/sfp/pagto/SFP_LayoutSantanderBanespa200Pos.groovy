package multitec.formulas.sfp.pagto;

import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abf01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ESocialUtils

class SFP_LayoutSantanderBanespa200Pos extends FormulaBase {
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

		def codConvenio = "";
		def reg = abf01.abf01json.getInteger("registro");
		def pi = abf01.abf01json.getInteger("pi");
		def conteudo = abf01.abf01json.getInteger("conteudo");

		if(reg == 0 && pi == 26) {
			codConvenio = conteudo;
		}
		
		TextFile txt = new TextFile();

		/**
		 * HEADER DO ARQUIVO
		 */
		txt.print(0, 1);																										//001 a 001
		txt.print(1, 1);																										//002 a 002
		txt.print("REMESSA", 7);																								//003 a 009
		txt.print("03", 2);																										//010 a 011
		txt.print("CRÉDITO EM C/C", 14);																						//012 a 025
		txt.print(codConvenio, 4);																								//026 a 029
		txt.print(StringUtils.space(17));																						//030 a 046
		txt.print(nomeEmpresa, 30);																								//047 a 076
		txt.print("033", 3);																									//077 a 079
		txt.print("BANESPA", 15);																								//080 a 094
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyy")), 6);													//095 a 100
		txt.print("01600", 5);																									//101 a 105
		txt.print("BPI", 3);																									//106 a 108
		txt.print("01", 2);																										//109 a 110
		txt.print(StringUtils.space(84));																						//111 a 194
		txt.print(sequencial, 6, '0', true);																					//195 a 200
		txt.newLine();
		sequencial++;

		for(int i = 0; i < abh80s.size(); i++) {
			String cc = "";
			if(abh80s.get(i).getString("abh80bcoAgencia") != null) {
				cc += StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia"));
				if(abh80s.get(i).getString("abh80bcoDigAg") != null) cc += abh80s.get(i).getString("abh80bcoDigAg");

				if(abh80s.get(i).getString("abh80bcoConta") != null) {
					cc += StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta"));
					if(abh80s.get(i).getString("abh80bcoDigCta") != null) cc += abh80s.get(i).getString("abh80bcoDigCta");
				}
			}

			String nomeTrab = abh80s.get(i).getString("abh80nome") != null && abh80s.get(i).getString("abh80nome").length() > 40 ? abh80s.get(i).getString("abh80nome").substring(0, 40) : abh80s.get(i).getString("abh80nome");

			/**
			 * TRANSAÇÃO
			 */
			txt.print(1, 1);																									//001 a 001
			txt.print(aac10.aac10ti == 0 ? "02" : "01", 2, '0', true);														//002 a 003
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);												//004 a 017
			txt.print(codConvenio, 4);																							//018 a 021
			txt.print(StringUtils.space(16));																					//022 a 037
			txt.print(StringUtils.space(25));																					//038 a 062
			txt.print(cc, 12);																									//063 a 074
			txt.print(StringUtils.space(8));																					//075 a 082
			txt.print(nomeTrab, 40);																							//083 a 122
			txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyy")), 6);												//123 a 128
			txt.print(StringUtils.extractNumbers(formatarDecimal(abh80s.get(i).getBigDecimal("valor"), 2, false)), 13, '0', true);			//129 a 141
			txt.print("001", 3);																								//142 a 144
			txt.print("021", 3);																								//145 a 147
			txt.print(StringUtils.space(3));																					//148 a 150
			txt.print("C", 1);																									//151 a 151
			txt.print(StringUtils.space(3));																					//152 a 154
			txt.print(abh80s.get(i).getString("abh80codigo"), 14, '0', true);														//155 a 168
			txt.print(StringUtils.space(26));																					//169 a 194
			txt.print(sequencial, 6, '0', true);																				//195 a 200
			txt.newLine();
			sequencial++;
			total = total + abh80s.get(i).getBigDecimal("valor");
		}

		/**
		 * TRAILLER DO ARQUIVO
		 */
		txt.print(9, 1);																										//001 a 001
		txt.print(StringUtils.space(149));																						//002 a 150
		txt.print(0, 6, '0', true);																								//151 a 156
		txt.print(0, 15, '0', true);																							//157 a 171
		txt.print(sequencial - 2, 6, '0', true);																				//172 a 177
		txt.print(StringUtils.extractNumbers(formatarDecimal(total, 2, false)), 15, '0', true);										//178 a 192
		txt.print(StringUtils.space(2));																						//193 a 194
		txt.print(sequencial, 6, '0', true);																					//195 a 200
		txt.newLine();
		
		put("txt", txt);
	}
	
	private BigDecimal formatarDecimal(BigDecimal valor, int casasDecimais, boolean valorZeroRetornaNull) {
		return ESocialUtils.formatarDecimal(valor, casasDecimais, valorZeroRetornaNull)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==