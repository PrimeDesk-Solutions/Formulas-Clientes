package multitec.formulas.cas.eSocial;

import java.time.LocalDate;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac13
import sam.model.entities.aa.Aaj01
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;

public class S_1299_xml extends FormulaBase {
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		Aac13 aac13 = aac10.aac13fiscal;
		
		Integer indApuracao = get("indApuracao");
		LocalDate perApur = get("perApur");
		Integer tpAmb = get("tpAmb");
		
		boolean evtRemun = get("evtRemun");
		boolean evtComProd = get("evtComProd");
	    boolean evtContratAvNP = get("evtContratAvNP");
		boolean evtInfoComplPer = get("evtInfoComplPer");
		boolean evtPgto = get("evtPgtos")
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtFechaEvPer/v_S_01_03_00");
		ElementXml evtFechaEvPer = eSocial.addNode("evtFechaEvPer");
		evtFechaEvPer.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtFechaEvPer.addNode("ideEvento");
		ideEvento.addNode("indApuracao", indApuracao, true);
		ideEvento.addNode("perApur", indApuracao == 1 ? ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY_MM) : ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY), true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtFechaEvPer.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml infoFech = evtFechaEvPer.addNode("infoFech");
		infoFech.addNode("evtRemun", evtRemun ? 'S' : 'N', true);
		infoFech.addNode("evtPgtos", evtPgto ? 'S' : 'N', true);
		infoFech.addNode("evtComProd", evtComProd ? 'S' : 'N', true);
		infoFech.addNode("evtContratAvNP", evtContratAvNP ? 'S' : 'N', true);
		infoFech.addNode("evtInfoComplPer", evtInfoComplPer ? 'S' : 'N', true);
		
		if (indApuracao.intValue() != 2 && perApur.isBefore(LocalDate.of(2021, 7, 1))) infoFech.addNode("indExcApur1250", 'S', false);
		
		Aaj01 aaj01 = aac13.aac13classTrib;
		String classTrib = aaj01 == null ? null : aaj01.aaj01eSocial;
		if (classTrib != null && classTrib.equalsIgnoreCase("04")) infoFech.addNode("transDCTFWeb", 'S', false);
		
		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==