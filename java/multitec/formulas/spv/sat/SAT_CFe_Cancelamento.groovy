package multitec.formulas.spv.sat

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.xml.ElementXml
import br.com.multitec.utils.xml.XMLConverter
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abd10
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils

class SAT_CFe_Cancelamento extends FormulaBase {
	
	private Eaa01 eaa01;
	private Abd10 abd10;
	
	private Aac10 empresa;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPV_CFE_SAT;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		abd10 = get("abd10");
		
		empresa = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
		
		/**
		 * CFeCanc - Cupom Fiscal Eletrônico SAT cancelamento
		 */
		ElementXml cfeCanc = XMLConverter.createElement("CFeCanc");
		
		cfeCanc.addCaracterSubstituir('\n'.toCharacter(), "");
		cfeCanc.addCaracterSubstituir('<'.toCharacter(), "&lt;");
		cfeCanc.addCaracterSubstituir('>'.toCharacter(), "&gt;");
		cfeCanc.addCaracterSubstituir('&'.toCharacter(), "&amp;");
		cfeCanc.addCaracterSubstituir('"'.toCharacter(), "&quot;");
		cfeCanc.addCaracterSubstituir('\''.toCharacter(), "&#39;");
		
		/**
		 * infCFe - Dados do CFe (A01)
		 */
		ElementXml infCfe = cfeCanc.addNode("infCFe");
		infCfe.setAttribute("chCanc", "CFe" + eaa01.eaa01nfeChave);
					
		/**
		 * ide - Grupo das informações de identificação do CF-e (B01)
		 */
		ElementXml ide = infCfe.addNode("ide");
		
		ide.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.aac10aCnpj), 14), true);
		ide.addNode("signAC", abd10.abd10signAC, true);
		ide.addNode("numeroCaixa", StringUtils.ajustString(abd10.abd10caixa_Zero, 3), true);
		
		/**
		 * emit - Grupo de identificação do emitente do CF-e (C01)
		 */
		infCfe.addNode("emit");
		
		/**
		 * dest - Grupo de identificação do Destinatário do CF-e (E01)
		 */
		infCfe.addNode("dest");
					
		/**
		 * total - Grupo de Valores Totais do CF-e (W01)
		 */
		infCfe.addNode("total");
		
		/**
		 * infAdic - Grupo de Informações Adicionais (Z01)
		 */
		infCfe.addNode("infAdic");
		
		String dados = NFeUtils.gerarXML(cfeCanc);
		
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzUifQ==