/*
AUTOR: NAGYLA
ULTIMA ALTERACAO: 04/08/2026	13:30
*/

package Inova.formulas.scf.cob.ret;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFileLeitura;
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abf01;
import sam.model.entities.ab.Abf20
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa0102;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro
import sam.server.scf.service.SCFService

import java.time.LocalDate

public class SCF_LayoutBancoSicrediRetorno_CNAB_400 extends FormulaBase{
	public final static String PATTERN_DDMMYY = "ddMMyy";

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_RETORNO_DE_COBRANCA;
	}

	@Override
	public void executar() {
		//************************** Fórmula gerada a partir do Manual de Soluções em Recebimentos CNAB400/CBR641 Fevereiro/2026 – Versão 3.0 ******************************
		List<TableMap> tmList = new ArrayList();
		TextFileLeitura txt = new TextFileLeitura(get("registros"))
		SCFService scfService = instanciarService(SCFService.class);

		selecionarAlinhamento("0001");

		Abf01 abf01 = getSession().get(Abf01.class, get("abf01id"));

		boolean isCarteira6 = false;
		String carteira = abf01.abf01json.get("cod_carteira");//Carteira
		if(carteira != null && StringUtils.ajustString(carteira, 3, '0', true).equals("006")) {
			//Só verifica o valor se a carteira for diferente de 006-sem registro, se for carteira 6 o valor não bate pois o retorno volta somente com valor pago
			isCarteira6 = true;
		}

		txt.nextLine();//Pula HEADER
		while(txt.nextLine()){
			if(txt.getSubString(0, 1).equals("1")){
				TableMap tm = new TableMap();
				List<String> inconsistencias = new ArrayList();
				String id = txt.getSubString(116, 126).trim();
				int pos = id.indexOf(";", -1);

				Daa01 daa01 = null;
				daa01 = buscarDocumento(id);

				/**
				 * Validando o documento - Daa01 e Daa0102
				 */
				boolean validouDocumento = true;
				if(daa01 == null){
					validouDocumento = false;
					String inconsistencia = "Documento não encontrado pelo ID informado no retorno. Conteúdo encontrado: " + id;
					inconsistencias.add(inconsistencia);
					tm.put("inconsistencias", inconsistencias);
				}else{
					if(daa01.daa01central.abb01quita_Zero > 0){
						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " já foi recebido.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}

					Integer movim = pos == -1 ? 0 : Integer.parseInt(StringUtils.extractNumbers(id.substring(pos+1, id.length())));  //número do movimento
					Daa0102 daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, null);
					if(daa0102 == null){
						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi enviado ao banco, porém consta no retorno.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}

					if(daa01.daa01valor.compareTo(new BigDecimal(0.01)) != 0){ //Se o valor não for (0,01)
						if(!isCarteira6) {
							if(daa01.daa01valor.compareTo(new BigDecimal(txt.getSubString(152, 165)).divide(100)) != 0){
								String inconsistencia = "O valor do documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " é diferente do valor do documento do retorno.";
								inconsistencias.add(inconsistencia);
								validouDocumento = false;
							}
						}
					}

					String descricaoOcor = buscarDescricaoOcorrencia(txt.getSubString(108, 110));
					if(descricaoOcor == null){
						String inconsistencia = "A ocorrência " + txt.getSubString(108, 110) + " informada no retorno para o documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrada nos parâmetros de retorno do banco.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}
					tm.put("inconsistencias", inconsistencias);
				}

				/**
				 * Exibindo documentos
				 */
				if(validouDocumento){
					if (buscarDescricaoOcorrencia(txt.getSubString(108, 110))  != "Tarifa"){

						TableMap daa01Json = daa01.daa01json != null ? daa01.daa01json : new TableMap()

						BigDecimal vlrTXT = new BigDecimal(txt.getSubString(253, 266)) / 100
						BigDecimal jurosTXT = new BigDecimal(txt.getSubString(266, 279)) / 100
						BigDecimal multaTXT = new BigDecimal(txt.getSubString(279, 292)) / 100
						BigDecimal descontoTXT = new BigDecimal(txt.getSubString(240, 253)) / 100
						BigDecimal abatimentoTXT = new BigDecimal(txt.getSubString(227, 240)) / 100
						String data = txt.getSubString(110, 116).trim()
						LocalDate dtPagto = null

						if(data != "" && data != "000000"){
							Integer dia = Integer.parseInt(data.substring(0, 2));
							Integer mes = Integer.parseInt(data.substring(2, 4));
							Integer ano = Integer.parseInt("20" + data.substring(4, 6));
							dtPagto = LocalDate.of(ano, mes, dia);
						}

						String datac = txt.getSubString(328, 337).trim()
						LocalDate dtBaixa = null

						if(datac != "" && datac != "000000"){
							Integer ano = Integer.parseInt(datac.substring(0, 4));
							Integer mes = Integer.parseInt(datac.substring(4, 6));
							Integer dia = Integer.parseInt(datac.substring(6, 8));
							dtBaixa = LocalDate.of(ano, mes, dia);
						}

						daa01.daa01liquido = vlrTXT

						if (txt.getSubString(108, 110) == "06" || txt.getSubString(108, 110) == "15") {
							daa01.daa01dtPgto = dtPagto
							daa01.daa01dtBaixa = dtBaixa
						}

						daa01Json.put("jurosq", jurosTXT)
						daa01Json.put("multa", multaTXT)
						daa01Json.put("descontoq", descontoTXT + abatimentoTXT)

						daa01.setDaa01json(daa01Json)

						tm.put("daa01", daa01);
						tm.put("abf20id", buscarPLF(codigoPLF(txt.getSubString(108, 110))));
						tm.put("ocorrencia", buscarDescricaoOcorrencia(txt.getSubString(108, 110)));
					}
				}
				tmList.add(tm);
			}
		}
		put("tmList", tmList);
	}

	private Daa01 buscarDocumento(String id3){
		Daa01 daa01 = getSession().createCriteria(Daa01.class)
				.addJoin(Joins.fetch("daa01central"))
				.addWhere(Criterions.eq("daa01id", Long.parseLong(id3)))
				.get(ColumnType.ENTITY)
		if(daa01 != null) return daa01

		try{
			daa01 = getSession().createCriteria(Daa01.class)
					.addJoin(Joins.fetch("daa01central"))
					.addWhere(Criterions.eq("daa01camposcustom->>'id_sam3'", id3))
					.get(ColumnType.ENTITY)
		}catch(Exception err){

		}
		return daa01;
	}

	private String buscarDescricaoOcorrencia(String codigoOcorrencia) {
		switch(codigoOcorrencia) {
			case "02": return "Entrada confirmada";
			case "03": return "Entrada rejeitada";
			case "06": return "Liquidação normal";
			case "07": return "Intenção de pagamento";
			case "09": return "Baixa de Título";
			case "10": return "Baixada conforme solicitação cooperativa";
			case "12": return "Abatimento concedido";
			case "13": return "Abatimento cancelado";
			case "14": return "Alteração de data de vencimento";
			case "15": return "Liquidação em cartório";
			case "17": return "Liquidação após baixa";
			case "19": return "Confirmação de recebimento de instrução de protesto";
			case "20": return "Confirmação de recebimento de instrução de sustação de protesto";
			case "23": return "Entrada de titulo em cartório";
			case "24": return "Entrada rejeitada por CEP irregular";
			case "27": return "Baixa rejeitada";
			case "28": return "Tarifa";
			case "29": return "Rejeição do Pagador";
			case "30": return "Alteração rejeitada";
			case "32": return "Instrução rejeitada";
			case "33": return "Confirmação de pedido e alteração de outros dados";
			case "34": return "Retirado de cartório e manutenção em carteira";
			case "35": return "Aceite do pagador";
			case "78": return "Confirmação de pedido de negativação";
			case "79": return "Confirmação de pedido de exclusão de negativação";
			case "80": return "Confirmação de entrada de negativação";
			case "81": return "Entrada de negativação rejeitada";
			case "82": return "Confirmação de exclusão de negativação";
			case "83": return "Exclusão de negativação rejeitada";
			case "84": return "Exclusão de negativação por outros motivos";
			case "85": return "Ocorrência informacional por outros motivos";
			default: return null;
		}
	}

	private String codigoPLF(String codigoOcorrencia) {
		switch(codigoOcorrencia) {
			case "06": return "201";
			case "15": return "201";
			default: return null;
		}
	}
	/*
	private String buscarDataPagamento(String dataPagamento) {
	    if (dataPagamento != null && !dataPagamento.isEmpty()) {
	        return dataPagamento;
	    } else {
			return null
	    }
	}

	private String buscarDataBaixa(String dataBaixa) {
	    if (dataBaixa != null && !dataBaixa.isEmpty()) {
	        return dataBaixa;
	    } else {
			return null;
	    }
	}*/

	private Long buscarPLF(String codigoPLF) {
		Abf20 abf20 = getAcessoAoBanco().buscarRegistroUnico("SELECT abf20id FROM Abf20 WHERE abf20codigo = :P1 " + getSamWhere().getWherePadrao("AND", Abf20.class) , Parametro.criar("P1", codigoPLF));
		return abf20 == null ? null : abf20.abf20id;
	}


}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==