package Atilatte.formulas.scf.cob.ret;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFileLeitura;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abf01;
import sam.model.entities.ab.Abf20
import sam.model.entities.ab.Abb01;
import sam.model.entities.aa.Aah01;
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa0102;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro
import sam.server.scf.service.SCFService;
import java.time.LocalDate;

class Layout_001_Bradesco_Retorno extends FormulaBase {
	public final static String PATTERN_DDMMYY = "ddMMyy";

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_RETORNO_DE_COBRANCA;
	}
	
	@Override
	public void executar() {
		//**************************Fórmula gerada no dia 01/04/2020 ******************************
		List<TableMap> tmList = new ArrayList();
		TextFileLeitura txt = new TextFileLeitura(get("registros"))
		SCFService scfService = instanciarService(SCFService.class);
		
		selecionarAlinhamento("0001");
		
		Abf01 abf01 = getSession().get(Abf01.class, get("abf01id"));
		
		boolean isCarteira6 = false;
		String carteira = abf01.abf01json.get(getCampo("0","carteira"));//Carteira
		if(carteira != null && StringUtils.ajustString(carteira, 3, '0', true).equals("006")) {
			//Só verifica o valor se a carteira for diferente de 006-sem registro, se for carteira 6 o valor não bate pois o retorno volta somente com valor pago
			isCarteira6 = true;
		}
		
		txt.nextLine();//Pula HEADER
		Integer countLinha = 1;
		
		while(txt.nextLine()){
			if(txt.getSubString(0, 1).equals("1")){
				TableMap tm = new TableMap();
				List<String> inconsistencias = new ArrayList();
				String id = txt.getSubString(37, 62).trim();
				int pos = id.indexOf(";", -1);
				
				Daa01 daa01 = null;
				if(pos == -1) {
					String inconsistencia = "Documento não encontrado por não haver o ID informado no retorno. Conteúdo encontrado: " + id;
					inconsistencias.add(inconsistencia);
				}else {
					daa01 = getAcessoAoBanco().buscarRegistroUnicoById("Daa01", Long.parseLong(id.substring(0, pos)));
				}
				
				/**
				 * Validando o documento - Daa01 e Daa0102
				 */
				boolean validouDocumento = true;
				if(daa01 == null){
					validouDocumento = false;
					inconsistencias.add("Documento de número "+txt.getSubString(116,122) + " não foi encontrado no sistema. Linha: "+countLinha.toString());
					tm.put("inconsistencias", inconsistencias);
//					continue
				
				}else{
					if(daa01.daa01central.abb01quita_Zero > 0){
						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " já foi recebido.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}
				
//					Integer movim = pos == -1 ? 0 : Integer.parseInt(StringUtils.extractNumbers(id.substring(pos+1, id.length())));  //número do movimento
//					Daa0102 daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movim);
//					if(daa0102 == null){
//						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi enviado ao banco, porém consta no retorno.";
//						inconsistencias.add(inconsistencia);
//						validouDocumento = false;
//					}
					
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
					
					// Data Crédito/Baixa
					String diaBaixa = txt.getSubString(295,297).trim();
					String mesBaixa = txt.getSubString(297,299).trim();
					String anoBaixa = "20" + txt.getSubString(299,301);

					// Data Pagamento
					String diaPgto = txt.getSubString(110,112).trim();
					String mesPgto = txt.getSubString(112,114).trim();
					String anoPgto = "20" + txt.getSubString(114,116);

					String txtValorLiq = txt.getSubString(253,266).replaceFirst("^0+","");
					String valorLiqFormatado = txtValorLiq != null && txtValorLiq.size() > 0 ? formatarValor(txtValorLiq) : "0";
					String txtJurosM = txt.getSubString(266,279) != "000000000000" ? txt.getSubString(266,279).replaceFirst("^0+","") : "0";
					String txtDesconto = txt.getSubString(240,253) != "0000000000000" ? txt.getSubString(240,253).replaceFirst("^0+","") : "0";
					String txtEncargo = txtDesconto;

					
					def jurosM = txtJurosM.size() > 0 ? new BigDecimal(txtJurosM) / 100 : new BigDecimal(0);
					def multa = jurosM * 0.8;
					def juros =  jurosM * 0.2;
					def desconto = (new BigDecimal(txtDesconto) / 100) * -1;
					def encargos = new BigDecimal(txtEncargo) / 100;
					
					// Define o Valor liquido do documento
					daa01.daa01liquido = new BigDecimal(valorLiqFormatado);
					
					// Juros e Multa 
					TableMap tmCampoLivreDoc = new TableMap()
					if(daa01 != null ) tmCampoLivreDoc.put("multa",multa);
					if(daa01 != null ) tmCampoLivreDoc.put("juros",juros);
					if(daa01 != null ) tmCampoLivreDoc.put("desconto",desconto);
					if(daa01 != null ) tmCampoLivreDoc.put("dt_limite_desc",daa01.daa01dtVctoN);
					if(daa01 != null ) tmCampoLivreDoc.put("jurosAjustado",1);
					//if(daa01 != null ) tmCampoLivreDoc.put("encargos",encargos);

					daa01.daa01json = tmCampoLivreDoc;

					if(daa01 != null ) daa01.daa01dtPgto = !diaPgto.isEmpty() && !mesPgto.isEmpty() ? LocalDate.of(anoPgto.toInteger(),mesPgto.toInteger(),diaPgto.toInteger()) : null;
					if(daa01 != null) daa01.daa01dtBaixa = !diaBaixa.isEmpty() && !mesBaixa.isEmpty() ? LocalDate.of(anoBaixa.toInteger(),mesBaixa.toInteger(),diaBaixa.toInteger()) : null;
					tm.put("daa01", daa01);
					tm.put("abf20id", buscarPLF(codigoPLF(txt.getSubString(108, 110))));
					tm.put("ocorrencia", buscarDescricaoOcorrencia(txt.getSubString(108, 110)));
				}
				
				tmList.add(tm);
			}
		}
		
		put("tmList", tmList);
	}

	private String formatarValor(String txtValorLiq){
		String valorInteiro = txtValorLiq.substring(0, txtValorLiq.length() - 2);
		String valorFormatado = txtValorLiq.substring(txtValorLiq.length() - 2);

		return valorInteiro + "." + valorFormatado
		
	}
	
	private String buscarDescricaoOcorrencia(String codigoOcorrencia) {
		switch(codigoOcorrencia) {
			case "02": return "Entrada confirmada";
			case "03": return "Entrada rejeitada";
			case "06": return "Liquidação normal";
			case "10": return "Baixado conforme instruções da Agência";
			default: return null;
		}
	}
	
	private String codigoPLF(String codigoOcorrencia) {
		switch(codigoOcorrencia) {
			case "06": return "099";
			default: return null;
		}
	}
	
	private Long buscarPLF(String codigoPLF) {
		Abf20 abf20 = getAcessoAoBanco().buscarRegistroUnico("SELECT abf20id FROM Abf20 WHERE abf20codigo = :P1 " + getSamWhere().getWherePadrao("AND", Abf20.class) , Parametro.criar("P1", codigoPLF));
		return abf20 == null ? null : abf20.abf20id;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==