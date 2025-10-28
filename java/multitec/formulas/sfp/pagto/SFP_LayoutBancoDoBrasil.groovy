package multitec.formulas.sfp.pagto

import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.ab.Abf01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ESocialUtils

class SFP_LayoutBancoDoBrasil extends FormulaBase{
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
		
		
		def sequencial = 1;
		def numConvenio = "";
		def tipoRetorno = "";
		def reg = abf01.abf01json.getInteger("registro")
		def pi = abf01.abf01json.getInteger("pi");
		def conteudo = abf01.abf01json.getInteger("conteudo");

		if(reg == 0 && pi == 80) {
			numConvenio = conteudo;
		}else if(reg == 0 && pi == 86) {
			tipoRetorno = conteudo;
		}
		
		TextFile txt = new TextFile();

		/**
		 * A/ HEADER
		 */
		txt.print(0);																								//001 a 001
		txt.print(1);																								//002 a 002
		txt.print(StringUtils.space(7));																			//003 a 009
		txt.print("03");																							//010 a 011
		txt.print(StringUtils.space(1));																			//012 a 012
		txt.print(0, 5, '0', true);																					//013 a 017
		txt.print(StringUtils.space(9));																			//018 a 026
		txt.print(StringUtils.extractNumbers(abf01.abf01agencia), 4, '0', true);									//027 a 030
		txt.print(abf01.abf01digAgencia, 1, '0', true);																//031 a 031
		txt.print(StringUtils.extractNumbers(abf01.abf01conta), 9, '0', true);										//032 a 040
		txt.print(abf01.abf01digConta, 1, '0', true);																//041 a 041
		txt.print(StringUtils.space(5));																			//042 a 046
		txt.print(getVariaveis().aac10.aac10rs, 30);																//047 a 076
		txt.print("001", 3);																						//077 a 079
		txt.print(numConvenio, 6);																					//080 a 085
		txt.print(tipoRetorno, 3);																					//086 a 088
		txt.print(StringUtils.space(10));																			//089 a 098
		txt.print(StringUtils.space(2));																			//099 a 100
		txt.print(StringUtils.space(3));																			//101 a 103
		txt.print(StringUtils.space(63));																			//104 a 166
		txt.print("NOVO", 4);																						//167 a 170
		txt.print(StringUtils.space(15));																			//171 a 185
		txt.print(StringUtils.space(9));																			//186 a 194
		txt.print(sequencial, 6, '0', true);																		//195 a 200
		txt.newLine();;
		sequencial++;

		/**
		 * B/ DETALHE
		 */
		for(int i = 0; i < abh80s.size(); i++) {
			def nomeTrab = abh80s.get(i).getString("abh80nome") != null && abh80s.get(i).getString("abh80nome").length() > 40 ? abh80s.get(i).getString("abh80nome").substring(0, 40) : abh80s.get(i).getString("abh80nome");

			txt.print(1);																							//001 a 001
			txt.print(StringUtils.space(1));																		//002 a 002
			txt.print(2);																							//003 a 003
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80cpf")), 14, '0', true);				//004 a 017
			txt.print(0, 4, '0', true);														   						//018 a 021
			txt.print(0);																   							//022 a 022
			txt.print(0, 9, '0', true);														   						//023 a 031
			txt.print(0);																   							//032 a 032
			txt.print(StringUtils.space(10));																		//033 a 042
			txt.print(StringUtils.space(8));																		//043 a 050
			txt.print(StringUtils.space(6));																		//051 a 056
			txt.print(0, 3, '0', true);																				//057 a 059
			txt.print(StringUtils.space(3));																		//060 a 062
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia")), 4, '0', true);		//063 a 066
			txt.print(abh80s.get(i).getString("abh80bcoDigAg"), 1, '0', true);										//067 a 067
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoConta")), 12, '0', true);			//068 a 079
			txt.print(abh80s.get(i).getString("abh80bcoDigCta"), 1, '0', true);										//080 a 080
			txt.print(StringUtils.space(2));																		//081 a 082
			txt.print(nomeTrab, 40);																				//083 a 122
			txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyy")));						//123 a 128
			txt.print(StringUtils.extractNumbers(formatarDecimal(abh80s.get(i).getBigDecimal("valor"), 2, false)), 13, '0', true);	//129 a 141
			txt.print("001");																						//142 a 144
			txt.print(StringUtils.space(40));																		//145 a 184
			txt.print(StringUtils.space(10));																		//185 a 194
			txt.print(sequencial, 6, '0', true);																	//195 a 200
			txt.newLine();;
			sequencial++;
		}

		/**
		 * C/ TRAILER
		 */
		txt.print(9);																								//001 a 001
		txt.print(StringUtils.space(193));																			//002 a 194
		txt.print(sequencial, 6, '0', true);																		//195 a 200
		txt.newLine();
		
		put("txt", txt);
	}
	
	private BigDecimal formatarDecimal(BigDecimal valor, int casasDecimais, boolean valorZeroRetornaNull) {
		return ESocialUtils.formatarDecimal(valor, casasDecimais, valorZeroRetornaNull)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==