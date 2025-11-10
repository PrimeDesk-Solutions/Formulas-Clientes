package multitec.formulas.scf.pag.ret

import br.com.multiorm.exception.NonUniqueObject
import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abf01
import sam.model.entities.ab.Abf20
import sam.model.entities.da.Daa01
import sam.model.entities.da.Daa0102
import sam.server.samdev.formula.FormulaBase
import sam.server.scf.service.SCFService

class Layout_006_Bradesco_240 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_RETORNO_DE_PAGAMENTO;
	}

	@Override
	public void executar() {
		SCFService scfService = instanciarService(SCFService.class);
		
		//**************************Fórmula gerada no dia 09/09/2020 ******************************
		List<TableMap> tmList = new ArrayList();
		TextFileLeitura txt = new TextFileLeitura(get("registros"))
		Aac10 aac10 = get("aac10");
		Abf01 abf01 = get("abf01");
		Long idDaa01 = null;
		int linha = 0;
		TableMap tm = new TableMap();
		List<String> inconsistencias = new ArrayList();

		while(txt.nextLine()){
			if(txt.getSubString(7, 8).equals("3") && txt.getSubString(13, 14).equals("J")){
				String posId = txt.getSubString(182, 202).trim();
				int pos = posId.indexOf(";", -1);

				Abf20 abf20 = null;
				Daa0102 daa0102 = null;
				Daa01 daa01 = null;
				Integer movim = pos == -1 ? 0 : Integer.parseInt(posId.substring(pos+1, posId.length()));  //número do movimento

				if(posId == -1) {
					String serie = txt.getSubString(197, 201).trim();
					serie = serie.length() > 0 ? serie : null;
					def numero = txt.getSubString(191, 197).trim().length() == 0 ? null : Integer.parseInt(txt.getSubString(191, 197).trim());
					try {
						daa01 = scfService.buscaDocFinPagarPorNumSerParcQuita(numero, serie, serie, 0);

						if(daa01 != null){
							daa0102 = scfService.buscarIntegracaoPorNossoNumero(abf01.abf01id, true, daa01.daa01nossoNum);
							if(daa0102 != null) {
								daa01 = daa0102.daa0102doc;
								movim = daa0102.daa0102movim;
							}
						}

					}catch (NonUniqueObject e) {
						String inconsistencia = "Foi encontrado mais que um documento de número: " + daa01.daa01central.abb01num + " nesta empresa.";
						inconsistencias.add(inconsistencia);
					}
				}else {
					idDaa01 = Long.parseLong(posId.substring(0, posId));
					daa01 = getAcessoAoBanco().buscarRegistroUnicoById("Daa01", idDaa01);
				}

				if(daa01 == null){
					if(posId == -1) {
						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrado nesta empresa.";
						inconsistencias.add(inconsistencia);
					}else {
						String inconsistencia = "Documento de ID: " + idDaa01 + " não foi encontrado nesta empresa.";
						inconsistencias.add(inconsistencia);
					}
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

					daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movim);
					if(daa0102 == null){
						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi enviado ao banco, porém consta no retorno.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}

					if(daa01.daa01valor.compareTo(new BigDecimal(0.01)) != 0){ //Se o valor não for (0,01)
						if(daa01.daa01valor.compareTo(new BigDecimal(txt.getSubString(99, 114)).divide(100)) != 0){
							String inconsistencia = "O valor do documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " é diferente do valor recebido.";
							inconsistencias.add(inconsistencia);
							validouDocumento = false;
						}
					}

					String descricaoOcor = buscarDescricaoOcorrencia(txt.getSubString(108, 110));
					if(descricaoOcor == null){
						String inconsistencia = "A ocorrência " + txt.getSubString(108, 110) + " informada no retorno para o documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrada nos parâmetros de retorno do banco.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}
				}

			}else if(txt.getSubString(7, 8).equals("3") && txt.getSubString(13, 14).equals("A")){
				String id = txt.getSubString(73, 93).trim();
				int pos = id.indexOf(";", -1);

				Abf20 abf20 = null;
				Daa0102 daa0102 = null;
				Daa01 daa01 = null;
				Integer movim = pos == -1 ? 0 : Integer.parseInt(id.substring(pos+1, id.length()));  //número do movimento


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

					daa0102 = scfService.buscarUltimaIntegracao(abf01.abf01id, daa01.daa01id, movim);
					if(daa0102 == null){
						String inconsistencia = "Documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi enviado ao banco, porém consta no retorno.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}

					if(daa01.daa01valor.compareTo(new BigDecimal(0.01)) != 0){ //Se o valor não for (0,01)
						if(daa01.daa01valor.compareTo(new BigDecimal(txt.getSubString(99, 114)).divide(100)) != 0){
							String inconsistencia = "O valor do documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " é diferente do valor recebido.";
							inconsistencias.add(inconsistencia);
							validouDocumento = false;
						}
					}

					String descricaoOcor = buscarDescricaoOcorrencia(txt.getSubString(108, 110));
					if(descricaoOcor == null){
						String inconsistencia = "A ocorrência " + txt.getSubString(108, 110) + " informada no retorno para o documento número: " + daa01.daa01central.abb01num + ", série: " + daa01.daa01central.abb01serie + ", parcela: " + daa01.daa01central.abb01parcela + " não foi encontrada nos parâmetros de retorno do banco.";
						inconsistencias.add(inconsistencia);
						validouDocumento = false;
					}
				}
			}
		}
	}

	private String buscarDescricaoOcorrencia(String codigoOcorrencia) {
		switch(codigoOcorrencia) {
			case "10":
				return "Liquidação ok";
			default:
				return null;
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDUifQ==