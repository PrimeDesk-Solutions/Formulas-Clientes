package multitec.formulas.sfp.pagto
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

class SFP_LayoutSantanderReal extends FormulaBase {
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

		def sequencial = 1;

		def nomeEmpresa = aac10.aac10rs != null && aac10.aac10rs.length() > 20 ? aac10.aac10rs.substring(0, 20) : aac10.aac10rs;

		def identLctoEmpresa = "";
		def identTipoServ = "";
		def digitoDAC = "";

		def reg = abf01.abf01json.getInteger("registro");
		def pi = abf01.abf01json.getInteger("pi");
		def conteudo = abf01.abf01json.getInteger("conteudo");

		if(reg == 1 && pi == 38) {
			identLctoEmpresa = conteudo;
		}else if(reg == 1 && pi == 142) {
			identTipoServ = conteudo;
		}else if(reg == 1 && pi == 162) {
			digitoDAC = conteudo;
		}
		
		TextFile txt = new TextFile();

		/**
		 * HEADER DO ARQUIVO
		 */
		txt.print(0, 1);																										//001 a 001
		txt.print(StringUtils.space(8));																						//002 a 009
		txt.print("03", 2);																										//010 a 011
		txt.print("créditos c/c", 15);																							//012 a 026
		txt.print(nomeEmpresa, 20);																								//027 a 046
		txt.print(StringUtils.space(30));																						//047 a 076
		txt.print("356", 3);																									//077 a 079
		txt.print("BANCO REAL", 15);																							//080 a 094
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyy")), 6);													//095 a 100
		txt.print("1600", 5);																									//101 a 105
		txt.print("BPI", 3);																									//106 a 108
		txt.print(StringUtils.space(86));																						//109 a 194
		txt.print(sequencial, 6, '0', true);																					//195 a 200
		txt.newLine();
		sequencial++;

		for(int i = 0; i < abh80s.size(); i++) {
			def agenciaTrab = "";
			if(abh80s.get(i).getString("abh80bcoAgencia") != null) {
				agenciaTrab += StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoAgencia"));
				if(abh80s.get(i).getString("abh80bcoDigAg") != null) agenciaTrab += abh80s.get(i).getString("abh80bcoDigAg");
			}
			def contaTrab = "";
			if(abh80s.get(i).getString("abh80bcoConta") != null) {
				contaTrab += StringUtils.extractNumbers(abh80s.get(i).getString("abh80bcoDigAg"));
				if(abh80s.get(i).getString("abh80bcoDigCta") != null) contaTrab += abh80s.get(i).getString("abh80bcoDigCta");
			}

			def nomeTrab = abh80s.get(i).getString("abh80nome") != null && abh80s.get(i).getString("abh80nome").length() > 40 ? abh80s.get(i).getString("abh80nome").substring(0, 40) : abh80s.get(i).getString("abh80nome");

			def agenciaEmp = "";
			if(abf01.abf01agencia != null) {
				agenciaEmp += StringUtils.extractNumbers(abf01.abf01agencia);
				if(abf01.abf01digAgencia != null) agenciaEmp += abf01.abf01digAgencia;
			}
			def contaEmp = "";
			if(abf01.abf01conta != null) {
				contaEmp += StringUtils.extractNumbers(abf01.abf01conta);
				if(abf01.abf01digConta != null) contaEmp += abf01.abf01digConta;
			}

			/**
			 * TRANSAÇÃO
			 */
			txt.print(1, 1);																									//001 a 001
			txt.print(aac10.aac10ti == 0 ? 2 : 1, 2, '0', true);																//002 a 003
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);												//004 a 017
			txt.print(nomeEmpresa, 20);																							//018 a 037
			txt.print(identLctoEmpresa, 25);																					//038 a 062
			txt.print(agenciaTrab, 4, '0', true);																				//063 a 066
			txt.print(contaTrab, 10);																							//067 a 076
			txt.print(StringUtils.space(6));																					//077 a 082
			txt.print(nomeTrab, 40);																							//083 a 122
			txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("ddMMyy")), 6);												//123 a 128
			txt.print(StringUtils.extractNumbers(formatarDecimal(abh80s.get(i).getBigDecimal("valor"), 2, false)), 13, '0', true);			//129 a 141
			txt.print(identTipoServ, 3);																						//142 a 144
			txt.print(StringUtils.space(6));																					//145 a 150
			txt.print(agenciaEmp, 4, '0', true);																				//151 a 154
			txt.print(contaEmp, 7, '0', true);																					//155 a 161
			txt.print(digitoDAC, 1);																							//162 a 162
			txt.print(StringUtils.space(32));																					//163 a 194
			txt.print(sequencial, 6, '0', true);																				//195 a 200
			txt.newLine();
			sequencial++;
		}

		/**
		 * TRAILLER DO ARQUIVO
		 */
		txt.print(9, 1);																										//001 a 001
		txt.print(StringUtils.space(193));																						//002 a 194
		txt.print(sequencial, 6, '0', true);																					//195 a 200
		txt.newLine();
		
		put("txt", txt);
	}
	
	private String formatarDecimal(BigDecimal valor, int casasDecimais, boolean valorZeroRetornaNull) {
		return ESocialUtils.formatarDecimal(valor, casasDecimais, valorZeroRetornaNull)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==