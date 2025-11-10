package multitec.formulas.sfp.holerite;

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
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
import sam.server.samdev.formula.FormulaBase

class SFP_LayoutSantanderHolerite extends FormulaBase {
	private SFP8001DtoExportar sfp8001DtoExportar;
	private List<TableMap> abh80s;
	private Abf01 abf01;
	private Aac10 aac10;
	private ClientCriterion critAbb11;
	private def seqTextoSantander = 1;
	private def seqRegNoLoteSantander = 1;

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
		this.critAbb11 = sfp8001DtoExportar.critAbb11;
		
		def sequencial = 1;

		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);

		def nomeAac10 = aac10.aac10rs.length() > 30 ? aac10.aac10rs.substring(0, 20) : aac10.aac10rs;
		def naAac10 = aac10.aac10na.length() > 30 ? aac10.aac10na.substring(0, 8) : aac10.aac10na;
		def numLote = 1;
		
		NumberFormat nbHolerite = NumberFormat.getNumberInstance();
		nbHolerite.setMinimumFractionDigits(2);
		nbHolerite.setGroupingUsed(false);
		
		TextFile txt = new TextFile();
		
		/**
		 * TIPO 0 - HEADER DE ARQUIVO
		 */
		txt.print("0", 1);																												//001 a 001
		txt.print(StringUtils.space(8));																								//002 a 009
		txt.print("01", 2);																												//010 a 011
		txt.print("CREDITOS C/C", 15);																									//012 a 026
		txt.print(StringUtils.unaccented(nomeAac10.toUpperCase()), 20);																//027 a 046
		txt.print("CONTRACHEQUE", 12);																									//047 a 058
		txt.print(StringUtils.unaccented(naAac10.toUpperCase()), 8);																	//059 a 066
		txt.print(StringUtils.space(10));																								//067 a 076
		txt.print("033", 3);																											//077 a 079
		txt.print("BANCO SANTANDER", 15);																								//080 a 094
		txt.print(MDate.date().format(DateTimeFormatter.ofPattern("ddMMyy")), 6);																//095 a 100
		txt.print("01600", 5);																											//101 a 105
		txt.print("BPI", 3);																											//106 a 108
		txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("yyyyMMdd")), 8);														//109 a 116
		txt.print(StringUtils.space(4));																								//117 a 120
		txt.print(StringUtils.space(37));																								//121 a 157
		txt.print(StringUtils.space(12));																								//158 a 169
		txt.print(StringUtils.space(20));																								//170 a 189
		txt.print(numLote, 5, '0', true);																								//190 a 194
		txt.print(seqRegNoLoteSantander, 6, '0', true);																					//195 a 200
		txt.newLine();
		seqRegNoLoteSantander++;
		
		for(int i = 0; i < abh80s.size(); i++) {
			def qtdeDependentes = getHoleriteUtils().findAbh8002QtdeDependentes(abh80s.get(i).getLong("abh80id"));
			Calendar calRef = new GregorianCalendar();
			calRef.setTime(sfp8001DtoExportar.dataInicial);
			
			def idAbh80 = abh80s.get(i).getLong("abh80id");
			def agencia = abh80s.get(i).getString("abh80bcoAgencia");
			def numDAC = abh80s.get(i).getString(i, "abh80bcoDigCta");
			def matricula = abh80s.get(i).getString(i, "abh80codigo");
			def conta = abh80s.get(i).getString(i, "abh80bcoConta");
			
			/**
			 * BLOCO 1 - CABEÇALHO
			 */
			def dataCredSantander = sfp8001DtoExportar.data;
			def numConvenioSantander = 1;
			gerarTipo3Santander(txt, "DEMONSTRATIVO DE PAGAMENTO", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "EMPRESA    : " + nomeAac10, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "CNPJ       : " + aac10.aac10ni, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "NOME       : " + abh80s.get(i).getString("abh80nome"), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "MATRÍCULA  : " + abh80s.get(i).getString("abh80codigo"), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "CPF        : " + abh80s.get(i).getString("abh80cpf"), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "FUNÇÃO     : " + abh80s.get(i).getString("abh05nome"), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "C.R.       : " + abh80s.get(i).getString("abb11nome"), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "ADMISSÃO   : " + new SimpleDateFormat("dd/MM/yyyy").format(abh80s.get(i).getDate("abh80dtadmis")), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "DEP        : " + qtdeDependentes, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "FILH       : " + qtdeDependentes, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "SALÁRIO    : " + nbHolerite.format(abh80s.get(i).getBigDecimal("abh80salario").round(2)), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "BCO        : SANTANDER", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "AG         : " + agencia, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "CONTA      : " + abh80s.get(i).getString(i, "abh80bcoConta"), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "DT PAGTO   : " + new SimpleDateFormat("dd/MM/yyyy").format(sfp8001DtoExportar.data), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "MÊS REF    : " + StringUtils.ajustString(calRef.get(Calendar.MONTH)+1, 2, '0', true), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			
			/**
			 * BLOCO 2 - VENCIMENTOS
			 */
			def totalVencimentos = 0;
			List<TableMap> rsFba01011sRend = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(idAbh80, critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 0, eveFerNaoImpr);
			if(rsFba01011sRend != null && rsFba01011sRend.size() > 0) {
				gerarTipo3Santander(txt, "VENCIMENTOS", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				
				for(int j = 0; j < rsFba01011sRend.size(); j++) {
					String lancamento = gerarLancamentoSantander(rsFba01011sRend.get(j).getString("abh21codigo"), rsFba01011sRend.get(j).getString("abh21nome"), rsFba01011sRend.get(j).getBigDecimal("ref"), rsFba01011sRend.get(j).getBigDecimal("valor"));
					gerarTipo3Santander(txt, lancamento, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
					totalVencimentos = totalVencimentos + rsFba01011sRend.get(j).getBigDecimal("valor");
				}
				
				gerarTipo3Santander(txt, StringUtils.ajustString("TOTAL VENCIMENTOS", 30) + StringUtils.ajustString(nbHolerite.format(totalVencimentos), 10, ' ', true), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			}
			
			/**
			 * BLOCO 3 - DESCONTOS
			 */
			def totalDescontos = 0;
			List<TableMap> rsFba01011sDesc = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(idAbh80, critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 1, eveFerNaoImpr);
			if(rsFba01011sDesc != null && rsFba01011sDesc.size() > 0) {
				gerarTipo3Santander(txt, "DESCONTOS", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				
				for(int j = 0; j < rsFba01011sDesc.size(); j++) {
					String lancamento = gerarLancamentoSantander(rsFba01011sDesc.get(j).getString("abh21codigo"), rsFba01011sDesc.get(j).getString("abh21nome"), rsFba01011sDesc.get(j).getBigDecimal("ref"), rsFba01011sDesc.get(j).getBigDecimal("valor"));
					gerarTipo3Santander(txt, lancamento, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
					totalDescontos = totalDescontos + rsFba01011sDesc.get(j).getBigDecimal("valor");
				}
				
				gerarTipo3Santander(txt, StringUtils.ajustString("TOTAL DESCONTOS", 30) + StringUtils.ajustString(nbHolerite.format(totalDescontos), 10, ' ', true), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			}
			
			/**
			 * BLOCO 4 - VALOR LÍQUIDO
			 */
			def totalLiquido = totalVencimentos - totalDescontos;
			gerarTipo3Santander(txt, StringUtils.ajustString("VALOR LÍQUIDO", 30) + StringUtils.ajustString(nbHolerite.format(totalLiquido), 10, ' ', true), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			
			/**
			 * BLOCO 5 - BASE/OUTROS
			 */
			List<TableMap> rsFba01011sBC = getHoleriteUtils().findDadosFba01011sByRemessaHolerite(idAbh80, critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 2, eveFerNaoImpr);
			if(rsFba01011sBC != null && rsFba01011sBC.size() > 0) {
				gerarTipo3Santander(txt, "BASE/OUTROS", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				
				for(int j = 0; j < rsFba01011sBC.size(); j++) {
					String lancamento = gerarLancamentoSantander(rsFba01011sBC.get(j).getString("abh21codigo"), rsFba01011sBC.get(j).getString("abh21nome"), rsFba01011sBC.get(j).getBigDecimal("ref"), rsFba01011sBC.get(j).getBigDecimal("valor"));
					gerarTipo3Santander(txt, lancamento, agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
				}
				
				gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			}
			
			/**
			 * BLOCO 6 - OUTRAS INFORMAÇÕES
			 */
			gerarTipo3Santander(txt, "OUTRAS INFORMAÇÕES", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			gerarTipo3Santander(txt, "----------------------------------------", agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
			
//			for(String mensagem : mensagens) {
//				if(mensagem == null) continue;
//			
//				gerarTipo3Santander(txt, StringUtils.ajustString(mensagem, 40), agencia, numDAC, nomeAac10, dataCredSantander, calRef.getTime(), matricula, conta, numConvenioSantander);
//			}
			
		}

		/**
		 * TIPO 9 - TRAILLER DE ARQUIVO
		 */
		txt.print("9", 1);																												//001 a 001
		txt.print(StringUtils.space(156));																								//002 a 157
		txt.print(StringUtils.space(12));																								//158 a 169
		txt.print(StringUtils.space(25));																								//170 a 194
		txt.print(seqRegNoLoteSantander, 6, '0', true);
		
		put("txt", txt);
	}
	
	private void gerarTipo3Santander(TextFile txt, String texto, String agencia, String dac, String empresa, LocalDate dataCredito, LocalDate dataRef, String matricula, String conta, String codConvenio) {
		txt.print(StringUtils.ajustString("3", 1));																											//001 a 001
		txt.print(StringUtils.ajustString(texto, 40));																										//002 a 041
		txt.print(StringUtils.ajustString("001", 3));																										//042 a 044
		txt.print(StringUtils.space(5));																							//045 a 049
		txt.print(StringUtils.ajustString(seqTextoSantander, 5, '0', true));																					//050 a 054
		txt.print(StringUtils.space(8));																							//055 a 062
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(agencia), 4, '0', true));																//063 a 066
		txt.print(StringUtils.space(7));																							//067 a 073
		txt.print(StringUtils.ajustString(dac, 1));																											//074 a 074
		txt.print(StringUtils.space(8));																							//075 a 082
		txt.print(StringUtils.space(12));																							//083 a 094
		txt.print(StringUtils.ajustString(StringUtils.unaccented(empresa.toUpperCase()), 20));															//095 a 114
		txt.print(StringUtils.ajustString(dataCredito.format(DateTimeFormatter.ofPattern("ddMMyy")), 6));															//115 a 120
		txt.print(StringUtils.ajustString(dataRef.format(DateTimeFormatter.ofPattern("MMyy")), 4));																	//121 a 124
		txt.print(StringUtils.ajustString(matricula, 15));																									//125 a 139
		txt.print(StringUtils.space(6));																							//140 a 145
		txt.print(StringUtils.ajustString(StringUtils.extractNumbers(conta), 12, '0', true));																//146 a 157
		txt.print(StringUtils.space(12));																							//158 a 169
		txt.print(StringUtils.ajustString(codConvenio, 20));																									//170 a 189
		txt.print(StringUtils.space(5));																							//190 a 194
		txt.print(StringUtils.ajustString(seqRegNoLoteSantander, 6, '0', true));																				//195 a 200
		txt.newLine();
		seqTextoSantander++;
		seqRegNoLoteSantander++;
	}
	
	private String gerarLancamentoSantander(String codigo, String nome, BigDecimal qtde, BigDecimal valor) {
		NumberFormat nbHolerite = NumberFormat.getNumberInstance();
		nbHolerite.setMinimumFractionDigits(2);
		nbHolerite.setGroupingUsed(false);
		
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.ajustString(codigo, 4));																				//002 a 005
		sb.append(StringUtils.space(1));																							//006 a 006
		sb.append(StringUtils.ajustString(nome != null && nome.length() > 23 ? nome.substring(0, 23) : nome, 17));					//007 a 023
		sb.append(StringUtils.space(1));																							//024 a 024
		sb.append(StringUtils.ajustString(nbHolerite.format(qtde.round(2)), 6, ' ', true));										//025 a 030
		sb.append(StringUtils.space(1));																							//031 a 031
		sb.append(StringUtils.ajustString(nbHolerite.format(valor.round(2)), 10, ' ', true));										//032 a 041
		return sb.toString();
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==