package multitec.formulas.scf.cob.ret;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFileLeitura;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abf01;
import sam.model.entities.ab.Abf20
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa0102;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro
import sam.server.scf.service.SCFService;

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
				
				}else{
					if(daa01.daa01central.abb01quita_Zero > 0){
						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " já foi recebido.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}
				
					Integer movim = pos == -1 ? 0 : Integer.parseInt(StringUtils.extractNumbers(id.substring(pos+1, id.length())));  //número do movimento
					Daa0102 daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movim);
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
					tm.put("daa01", daa01);
					tm.put("abf20id", buscarPLF(codigoPLF(txt.getSubString(108, 110))));
					tm.put("ocorrencia", buscarDescricaoOcorrencia(txt.getSubString(108, 110)));
				}
				
				tmList.add(tm);
			}
		}
		
		put("tmList", tmList);
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
			case "06": return "100";
			default: return null;
		}
	}
	
	private Long buscarPLF(String codigoPLF) {
		Abf20 abf20 = getAcessoAoBanco().buscarRegistroUnico("SELECT abf20id FROM Abf20 WHERE abf20codigo = :P1 " + getSamWhere().getWherePadrao("AND", Abf20.class) , Parametro.criar("P1", codigoPLF));
		return abf20 == null ? null : abf20.abf20id;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==