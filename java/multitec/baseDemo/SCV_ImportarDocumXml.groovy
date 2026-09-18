package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap;

import java.time.LocalDate

import org.springframework.web.multipart.MultipartFile

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.xml.ElementXml
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

public class SCV_ImportarDocumXml extends FormulaBase {
	
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
		
		InputStream is = arquivo.getInputStream();
		ElementXml elementXml = new ElementXml(is);
		
		List<ElementXml> elementsEaa01 = elementXml.getChildNodes("eaa01");
		for(ElementXml elementEaa01 : elementsEaa01) {
			//"cabeça" do documento 
			String aah01codigo = elementEaa01.getChildValue("aah01codigo");
			Integer abb01num = Integer.parseInt(elementEaa01.getChildValue("abb01num"));
			LocalDate abb01data = DateUtils.parseDate(elementEaa01.getChildValue("abb01data"), "dd/MM/yyyy");
			String abe01codigo = elementEaa01.getChildValue("abe01codigo");
			String abd01codigo = elementEaa01.getChildValue("abd01codigo");
			String abe40codigo = elementEaa01.getChildValue("abe40codigo");
			String abe30codigo = elementEaa01.getChildValue("abe30codigo");
			
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
			
			//itens do documento
			ElementXml elementEaa0103s = elementEaa01.getChildNode("eaa0103s");
			
			List<ElementXml> elementsEaa0103 = elementEaa0103s.getChildNodes("eaa0103");
			for(ElementXml elementEaa0103 : elementsEaa0103) {
				Integer eaa0103seq = Integer.parseInt(elementEaa0103.getChildValue("eaa0103seq"));
				Integer eaa0103tipo = Integer.parseInt(elementEaa0103.getChildValue("eaa0103tipo"));
				String eaa0103codigo = elementEaa0103.getChildValue("eaa0103codigo");
				BigDecimal eaa0103qtComl = new BigDecimal(elementEaa0103.getChildValue("eaa0103qtComl"));
				BigDecimal eaa0103unit = new BigDecimal(elementEaa0103.getChildValue("eaa0103unit"));
				
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
			
			srfService.executarFormulaSRFCalculoDocumento(new FormulaSRFCalculoDocumentoDto(eaa01.eaa01pcd.abd01frmItem, eaa01.eaa01pcd.abd01frmDoc, eaa01, null, "SCV3002", true));
			srfService.comporFinanceiroContabilidadeSeVazios(eaa01);
		}
		
		put("eaa01s", eaa01s);
		put("msgs", msgs);
	}

	/**
	 * XML de exemplo
	 
	<?xml version="1.0" encoding="UTF-8"?>
	<eaa01s>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>1</abb01num>
			<abb01data>11/12/2020</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10001</abd01codigo>
			<abe40codigo>003</abe40codigo>
			<abe30codigo>103</abe30codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101001</eaa0103codigo>
					<eaa0103qtComl>1</eaa0103qtComl>
					<eaa0103unit>5</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>14</abb01num>
			<abb01data>14/05/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10050</abd01codigo>
			<abe40codigo>003</abe40codigo>
			<abe30codigo>103</abe30codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101001</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>10</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>2</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101002</eaa0103codigo>
					<eaa0103qtComl>3</eaa0103qtComl>
					<eaa0103unit>3</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>15</abb01num>
			<abb01data>14/05/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10050</abd01codigo>
			<abe40codigo>003</abe40codigo>
			<abe30codigo>103</abe30codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101001</eaa0103codigo>
					<eaa0103qtComl>6</eaa0103qtComl>
					<eaa0103unit>2</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>2</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101002</eaa0103codigo>
					<eaa0103qtComl>1</eaa0103qtComl>
					<eaa0103unit>1</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>21</abb01num>
			<abb01data>21/10/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10001</abd01codigo>
			<abe40codigo>001</abe40codigo>
			<abe30codigo>001</abe30codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>1</eaa0103tipo>
					<eaa0103codigo>30001</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>13</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>22</abb01num>
			<abb01data>21/10/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10001</abd01codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>1</eaa0103tipo>
					<eaa0103codigo>30001</eaa0103codigo>
					<eaa0103qtComl>3</eaa0103qtComl>
					<eaa0103unit>10</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>23</abb01num>
			<abb01data>25/10/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10050</abd01codigo>
			<abe40codigo>003</abe40codigo>
			<abe30codigo>103</abe30codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102001</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>5</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>2</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102002</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>2</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>3</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102003</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>4</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>4</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102004</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>10</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>5</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102005</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>8</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>6</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102006</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>3</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>7</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102007</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>3</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>8</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0102008</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>4</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>9</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101001</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>2</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>10</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101002</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>10</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>11</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101003</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>3</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>12</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101004</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>2</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>13</eaa0103seq>
					<eaa0103tipo>0</eaa0103tipo>
					<eaa0103codigo>0101005</eaa0103codigo>
					<eaa0103qtComl>5</eaa0103qtComl>
					<eaa0103unit>3</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>24</abb01num>
			<abb01data>25/10/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10050</abd01codigo>
			<abe40codigo>003</abe40codigo>
			<abe30codigo>103</abe30codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>2</eaa0103tipo>
					<eaa0103codigo>0102</eaa0103codigo>
					<eaa0103qtComl>4</eaa0103qtComl>
					<eaa0103unit>19</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>2</eaa0103seq>
					<eaa0103tipo>2</eaa0103tipo>
					<eaa0103codigo>0103</eaa0103codigo>
					<eaa0103qtComl>4</eaa0103qtComl>
					<eaa0103unit>4</eaa0103unit>
				</eaa0103>
				<eaa0103>
					<eaa0103seq>3</eaa0103seq>
					<eaa0103tipo>2</eaa0103tipo>
					<eaa0103codigo>0101</eaa0103codigo>
					<eaa0103qtComl>4</eaa0103qtComl>
					<eaa0103unit>7</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>148</abb01num>
			<abb01data>04/11/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10003</abd01codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>1</eaa0103tipo>
					<eaa0103codigo>30001</eaa0103codigo>
					<eaa0103qtComl>1</eaa0103qtComl>
					<eaa0103unit>30</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>149</abb01num>
			<abb01data>04/11/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10003</abd01codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>1</eaa0103tipo>
					<eaa0103codigo>30001</eaa0103codigo>
					<eaa0103qtComl>1</eaa0103qtComl>
					<eaa0103unit>35</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
		<eaa01>
			<aah01codigo>011</aah01codigo>
			<abb01num>152</abb01num>
			<abb01data>29/11/2021</abb01data>
			<abe01codigo>0100001000</abe01codigo>
			<abd01codigo>10001</abd01codigo>
			<eaa0103s>
				<eaa0103>
					<eaa0103seq>1</eaa0103seq>
					<eaa0103tipo>1</eaa0103tipo>
					<eaa0103codigo>01001</eaa0103codigo>
					<eaa0103qtComl>10</eaa0103qtComl>
					<eaa0103unit>5</eaa0103unit>
				</eaa0103>
			</eaa0103s>
		</eaa01>
	 </eaa01s>
	 
	 *
	 */
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjYifQ==