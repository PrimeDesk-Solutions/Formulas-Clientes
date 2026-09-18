package multitec.baseDemo;

import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import br.com.multitec.utils.xml.ElementXml
import br.com.multitec.utils.xml.XMLConverter
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.NFeUtils

public class SCV_ExportarDocumXml extends FormulaBase {
	
	private List<Long> eaa01ids;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.EXPORTAR_DOCUMENTOS;
	}
	
	@Override
	public void executar() {
		this.eaa01ids = get("eaa01ids");

		ElementXml eaa01sEX = XMLConverter.createElement("eaa01s");
		
		eaa01sEX.addCaracterSubstituir('\n'.toCharacter(), "");
		eaa01sEX.addCaracterSubstituir('<'.toCharacter(), "&lt;");
		eaa01sEX.addCaracterSubstituir('>'.toCharacter(), "&gt;");
		eaa01sEX.addCaracterSubstituir('&'.toCharacter(), "&amp;");
		eaa01sEX.addCaracterSubstituir('"'.toCharacter(), "&quot;");
		eaa01sEX.addCaracterSubstituir('\''.toCharacter(), "&#39;");
		
		for(Long eaa01id : this.eaa01ids) {
			//Documento			
			Eaa01 eaa01 = getAcessoAoBanco().buscarRegistroUnicoById("Eaa01", eaa01id);
			
			Abd01 abd01 = eaa01.eaa01pcd;
			Abe40 abe40 = eaa01.eaa01tp;
			Abe30 abe30 = eaa01.eaa01cp;
						
			Abb01 abb01 = eaa01.getEaa01central();
			Aah01 aah01 = getAcessoAoBanco().buscarRegistroUnicoById("Aah01", abb01.abb01tipo.aah01id);
			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", abb01.abb01ent.abe01id);
			
			ElementXml eaa01EX = eaa01sEX.addNode("eaa01");
			
			eaa01EX.addNode("aah01codigo", aah01.aah01codigo, true);
			eaa01EX.addNode("abb01num", abb01.abb01num, true);
			eaa01EX.addNode("abb01data", DateTimeFormatter.ofPattern("dd/MM/yyyy").format(abb01.abb01data), true);
			eaa01EX.addNode("abe01codigo", abe01.abe01codigo, true);
			eaa01EX.addNode("abd01codigo", abd01.abd01codigo, true);
			eaa01EX.addNode("abe40codigo", abe40 != null ? abe40.abe40codigo : null, false);
			eaa01EX.addNode("abe30codigo", abe30 != null ? abe30.abe30codigo : null, false);
			
			//Itens
			if(eaa01.eaa0103s != null && eaa01.eaa0103s.size() > 0) {
				List<Eaa0103> eaa0103s = eaa01.eaa0103s.stream().sorted({o1, o2 -> o1.eaa0103seq_Zero.compareTo(o2.eaa0103seq_Zero)}).collect(Collectors.toList());
				
				ElementXml eaa0103sEX = eaa01EX.addNode("eaa0103s");
				
				for(Eaa0103 eaa0103 : eaa0103s) {
					ElementXml eaa0103EX = eaa0103sEX.addNode("eaa0103");
					
					eaa0103EX.addNode("eaa0103seq", eaa0103.eaa0103seq, true);
					eaa0103EX.addNode("eaa0103tipo", eaa0103.eaa0103tipo, true);
					eaa0103EX.addNode("eaa0103codigo", eaa0103.eaa0103codigo, true);
					eaa0103EX.addNode("eaa0103qtComl", formatarDecimal(eaa0103.eaa0103qtComl_Zero, 0, false), true);
					eaa0103EX.addNode("eaa0103unit", formatarDecimal(eaa0103.eaa0103unit_Zero, 0, false), true);
				}
			}
		}
		
		String dados = NFeUtils.gerarXML(eaa01sEX);
		
		put("dados", dados.getBytes());
	}
	
	private static String formatarDecimal(BigDecimal value, int casasDecimais, boolean vlrZeroRetornaNull) {
		if(value == null || value.compareTo(BigDecimal.ZERO) == 0) {
			if(vlrZeroRetornaNull) {
				return null;
			}
			value = BigDecimal.ZERO;
		}

		BigDecimal bigDecimal = value.setScale(casasDecimais, RoundingMode.HALF_EVEN);
		return bigDecimal.toString();
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
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjUifQ==