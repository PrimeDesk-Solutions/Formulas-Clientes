package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap;

import java.time.LocalDate

import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo;
import sam.dto.srf.FormulaSRFCalculoDocumentoDto
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abm01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase;
import sam.server.srf.service.SRFService

public class SCV_ImportarDocum extends FormulaBase {
	
	private Long abd01id;
	private LocalDate data;
	private TableMap json;
	private MultipartFile arquivo;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.IMPORTAR_DOCUMENTOS;
	}

	@Override
	public void executar() {
		abd01id = get("abd01id");
		data = get("data");
		json = get("json");
		arquivo = get("arquivo");
		
		List<Eaa01> eaa01s = new ArrayList<>();
		List<String> msgs = new ArrayList<>();
		
		SRFService srfService = instanciarService(SRFService.class);
		
		File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
		arquivo.transferTo(file);
		
		List<String> registros = FileUtils.readLines(file, "UTF-8");
		
		TextFileLeitura txt = new TextFileLeitura(registros, "|");
		
		int indexDoc = -1;
		while(txt.nextLine()) {
			String registro = txt.getCampo(1);
			
			if(registro == "01") { // 01 - "cabeça" do documento
				
				if(indexDoc >= 0) {
					Eaa01 eaa01 = eaa01s.get(indexDoc);
					srfService.executarFormulaSRFCalculoDocumento(new FormulaSRFCalculoDocumentoDto(eaa01.eaa01pcd.abd01frmItem, eaa01.eaa01pcd.abd01frmDoc, eaa01, null, "SCV3002", true));
					srfService.comporFinanceiroContabilidadeSeVazios(eaa01);
				}
				
				indexDoc++;
				
				String aah01codigo = txt.getCampo(2);
				Integer abb01num = Integer.parseInt(txt.getCampo(3));
				LocalDate abb01data = DateUtils.parseDate(txt.getCampo(4), "dd/MM/yyyy");
				String abe01codigo = txt.getCampo(5);
				String abd01codigo = txt.getCampo(6);
				String abe40codigo = txt.getCampo(7);
				String abe30codigo = txt.getCampo(8);
				
				Aah01 aah01 = getSession().createCriteria(Aah01.class)
										  .addWhere(Criterions.eq("aah01codigo", aah01codigo))
										  .get();
										  
				if(aah01 == null) interromper("Tipo de documento " + aah01codigo + " não encontrado.");
				
				Abe01 abe01 = getSession().createCriteria(Abe01.class)
										  .addWhere(Criterions.eq("abe01codigo", abe01codigo))
										  .addWhere(getSamWhere().getCritPadrao(Abe01.class))
										  .get();
										  
				if(abe01 == null) interromper("Entidade " + abe01codigo + " não encontrada.");
				
				Abd01 abd01 = getSession().createCriteria(Abd01.class)
										  .addWhere(Criterions.eq("abd01codigo", abd01codigo))
										  .addWhere(getSamWhere().getCritPadrao(Abd01.class))
										  .get();
										  
				if(abd01 == null) interromper("PCD " + abd01codigo + " não encontrado.");
				
				if(abd01.abd01frmItem == null) interromper("Não foi encontraa a fórmula de cálculo de itens no PCD " + abd01codigo + ".");
				
				if(abd01.abd01frmDoc == null) interromper("Não foi encontraa a fórmula de cálculo de documento no PCD " + abd01codigo + ".");
				
				Abe40 abe40 = null;
				if(abe40codigo != null && abe40codigo.length() > 0) {
					abe40 = getSession().createCriteria(Abe40.class)
										.addWhere(Criterions.eq("abe40codigo", abe40codigo))
										.addWhere(getSamWhere().getCritPadrao(Abe40.class))
										.get();
										  
					if(abe40 == null) interromper("Tabela de preço " + abe40codigo + " não encontrada.");
				}
				
				Abe30 abe30 = null;
				if(abe30codigo != null && abe30codigo.length() > 0) {
					abe30 = getSession().createCriteria(Abe30.class)
										.addWhere(Criterions.eq("abe30codigo", abe30codigo))
										.addWhere(getSamWhere().getCritPadrao(Abe30.class))
										.get();
										  
					if(abe30 == null) interromper("Condição de pagamento " + abe30codigo + " não encontrada.");
				}
				
				Eaa01 eaa01 = srfService.comporDocumentoPadrao(abe01.abe01id, abd01.abd01id, null);
				
				Abb01 abb01 = eaa01.eaa01central;
				abb01.abb01tipo = aah01;
				//abb01.abb01num = abb01num;
				abb01.abb01data = abb01data;
				abb01.abb01operAutor = "SCV3002";
				
				eaa01.eaa01tp = abe40;
				eaa01.eaa01cp = abe30;
				
				eaa01s.add(eaa01);
			}else { // 02 - item do documento
				Eaa01 eaa01 = eaa01s.get(indexDoc);
				
				Integer eaa0103seq = Integer.parseInt(txt.getCampo(2));
				Integer eaa0103tipo = Integer.parseInt(txt.getCampo(3));
				String eaa0103codigo = txt.getCampo(4);
				BigDecimal eaa0103qtComl = new BigDecimal(txt.getCampo(5));
				BigDecimal eaa0103unit = new BigDecimal(txt.getCampo(6));
				
				Abm01 abm01 = getSession().createCriteria(Abm01.class)
										  .addWhere(Criterions.eq("abm01tipo", eaa0103tipo))
										  .addWhere(Criterions.eq("abm01codigo", eaa0103codigo))
										  .addWhere(getSamWhere().getCritPadrao(Abm01.class))
										  .get();
										  
				if(abm01 == null) interromper("Item " + eaa0103codigo + " não encontrado.");
				
				Eaa0103 eaa0103 = srfService.comporItemDoDocumentoPadrao(eaa01, abm01.abm01id);
				
				eaa0103.eaa0103seq = eaa0103seq;
				eaa0103.eaa0103qtComl = eaa0103qtComl;
				eaa0103.eaa0103unit = eaa0103unit;
				
				eaa01.addToEaa0103s(eaa0103);
			}
		}
		if(indexDoc >= 0) {
			Eaa01 eaa01 = eaa01s.get(indexDoc);
			srfService.executarFormulaSRFCalculoDocumento(new FormulaSRFCalculoDocumentoDto(eaa01.eaa01pcd.abd01frmItem, eaa01.eaa01pcd.abd01frmDoc, eaa01, null, "SCV3002", true));
			srfService.comporFinanceiroContabilidadeSeVazios(eaa01);
		}
		
		put("eaa01s", eaa01s);
		put("msgs", msgs);
	}

	/*
	 *
	    TXT de Exemplo
	    Campos separados por pipe "|"
   
	    Registro 01 - "cabeça" do documento
	    |01|aah01codigo|abb01num|abb01data|abe01codigo|abd01codigo|abe40codigo|abe30codigo|
   
	    Registro 02 - itens
	    |02|eaa0103seq|eaa0103tipo|eaa0103codigo|eaa0103qtComl|eaa0103unit|
		  
		|01|011|1|11/12/2020|0100001000|10001|003|103|
		|02|1|0|0101001|1.000000|5.000000|
		|01|011|14|14/05/2021|0100001000|10050|003|103|
		|02|1|0|0101001|5.000000|10.000000|
		|02|2|0|0101002|3.000000|3.000000|
		|01|011|15|14/05/2021|0100001000|10050|003|103|
		|02|1|0|0101001|6.000000|2.000000|
		|02|2|0|0101002|1.000000|1.000000|
		|01|011|21|21/10/2021|0100001000|10001|001|001|
		|02|1|1|30001|5.000000|13.000000|
		|01|011|22|21/10/2021|0100001000|10001|||
		|02|1|1|30001|3.000000|10.000000|
		|01|011|23|25/10/2021|0100001000|10050|003|103|
		|02|1|0|0102001|5.000000|5.200000|
		|02|2|0|0102002|5.000000|2.300000|
		|02|3|0|0102003|5.000000|3.500000|
		|02|4|0|0102004|5.000000|10.000000|
		|02|5|0|0102005|5.000000|8.000000|
		|02|6|0|0102006|5.000000|2.850000|
		|02|7|0|0102007|5.000000|3.000000|
		|02|8|0|0102008|5.000000|4.200000|
		|02|9|0|0101001|5.000000|2.500000|
		|02|10|0|0101002|5.000000|10.000000|
		|02|11|0|0101003|5.000000|3.200000|
		|02|12|0|0101004|5.000000|1.580000|
		|02|13|0|0101005|5.000000|2.950000|
		|01|011|24|25/10/2021|0100001000|10050|003|103|
		|02|1|2|0102|4.000000|18.650000|
		|02|2|2|0103|4.000000|3.500000|
		|02|3|2|0101|4.000000|6.700000|
		|01|011|148|04/11/2021|0100001000|10003|||
		|02|1|1|30001|1.000000|30.000000|
		|01|011|149|04/11/2021|0100001000|10003|||
		|02|1|1|30001|1.000000|35.000000|
		|01|011|152|29/11/2021|0100001000|10001|||
		|02|1|1|01001|10.000000|5.000000|
	 *
	 */
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjYifQ==