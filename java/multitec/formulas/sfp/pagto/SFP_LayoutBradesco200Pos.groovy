package multitec.formulas.sfp.pagto

import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.ab.Abf01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ESocialUtils

class SFP_LayoutBradesco200Pos extends FormulaBase{
	private SFP8001DtoExportar sfp8001DtoExportar;
	private List<TableMap> abh80s;
	private Abf01 abf01;
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
		this.mapParametros = get("mapParametros");
		
		def total = BigDecimal.ZERO;
		def sequencial = 1;
		def razaoContaEmp = "";
		def codEmpresa = "";
		def razaoContaTrab = "";
		def reg = abf01.abf01json.getInteger("registro");
		def pi = abf01.abf01json.getInteger("pi");
		def conteudo = abf01.abf01json.getInteger("conteudo");

		if(reg == 0 && pi == 32) {
			razaoContaEmp = conteudo;
		}else if(reg == 0 && pi == 47) {
			codEmpresa = conteudo;
		}else if(reg == 1 && pi == 68) {
			razaoContaTrab = conteudo;
		}
		
		TextFile txt = new TextFile();

		/**
		 * HEADER
		 */
		txt.print(0);																									//001 a 001
		txt.print(1);																									//002 a 002
		txt.print("REMESSA");																							//003 a 009
		txt.print("03");																								//010 a 011
		txt.print("CREDITO C/C", 15);																					//012 a 026
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 5, '0', true);										//027 a 031
		txt.print(razaoContaEmp, 5);																					//032 a 036
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 7, '0', true);											//037 a 043
		txt.print(abf01.abf01digConta, 1, '0', true);																	//044 a 044
		txt.print(StringUtils.space(1));																				//045 a 045
		txt.print(StringUtils.space(1));																				//046 a 046
		txt.print(codEmpresa, 5);																						//047 a 051
		txt.print(getVariaveis().aac10.aac10rs, 25);																	//052 a 076
		txt.print("237");																								//077 a 079
		txt.print("BRADESCO", 15);																						//080 a 094
		txt.print(MDate.dateTime().format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);									//095 a 102
		txt.print("01600", 5);																							//103 a 107
		txt.print("BPI");																								//108 a 110
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);							//111 a 118
		txt.print(StringUtils.space(1));																				//119 a 119
		txt.print("N");																									//120 a 120
		txt.print(StringUtils.space(74));																				//121 a 194
		txt.print(sequencial, 6);																						//195 a 200
		txt.newLine();;
		sequencial++;

		/**
		 * TRANSAÇÂO
		 */
		for(int i = 0; i < abh80s.size(); i++) {
			def nomeTrab = abh80s.get(i).getString("abh80nome") != null && abh80s.get(i).getString("abh80nome").length() > 38 ? abh80s.get(i).getString("abh80nome").substring(0, 38) : abh80s.get(i).getString("abh80nome");

			txt.print(1);																								//001 a 001
			txt.print(StringUtils.space(61));  																			//002 a 062
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 5, '0', true);			//063 a 067
			txt.print(razaoContaTrab, 5);																				//068 a 072
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 7, '0', true);				//073 a 079
			txt.print(abh80s.get(i).getString("abh80bcoDigCta"), 1, '0', true);											//080 a 080
			txt.print(StringUtils.space(2));																			//081 a 082
			txt.print(nomeTrab, 38);																					//083 a 120
			txt.print(abh80s.get(i).getString("abh80codigo"), 6, '0', true);											//121 a 126
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("valor")), 13, '0', true);						//127 a 139
			txt.print("298");																							//140 a 142
			txt.print(StringUtils.space(8));																			//143 a 150
			txt.print(StringUtils.space(44));																			//151 a 194
			txt.print(sequencial, 6);																					//195 a 200
			txt.newLine();;
			sequencial++;
			total = total + abh80s.get(i).getBigDecimal("valor");
		}

		/**
		 * TRAILLER
		 */
		txt.print(9);																									//001 a 001
		txt.print(StringUtils.extractNumbers(total.toString()), 13, '0', true);											//002 a 014
		txt.print(StringUtils.space(180));																	    		//015 a 194
		txt.print(sequencial, 6);																						//195 a 200
		txt.newLine();
		
		put("txt", txt);
	}
	
	private BigDecimal formatarDecimal(BigDecimal valor, int casasDecimais, boolean valorZeroRetornaNull) {
		return ESocialUtils.formatarDecimal(valor, casasDecimais, valorZeroRetornaNull);
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==