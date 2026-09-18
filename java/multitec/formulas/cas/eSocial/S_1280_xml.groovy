package multitec.formulas.cas.eSocial;

import java.time.LocalDate;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;

public class S_1280_xml extends FormulaBase {
	private Aaa15 aaa15;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		def indApuracao = get("indApuracao");
		LocalDate perApur = get("perApur");
		def indRetif = get("indRetif");
		def nrRecibo = get("nrRecibo");
		def indSubstPatr = get("indSubstPatr");
		BigDecimal percRedContrib = get("percRedContrib");
		BigDecimal fator13 = get("fator13");
		BigDecimal fatorMes = get("fatorMes");
		def tpAmb = get("tpAmb");
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtInfoComplPer/v_S_01_01_00");
		ElementXml evtInfoComplPer = eSocial.addNode("evtInfoComplPer");
		evtInfoComplPer.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtInfoComplPer.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(nrRecibo != null) ideEvento.addNode("nrRecibo", nrRecibo, false);
		ideEvento.addNode("indApuracao", indApuracao, true);
		ideEvento.addNode("perApur", indApuracao == 1 ? ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY_MM) : ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY), true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtInfoComplPer.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml infoSubstPatr = evtInfoComplPer.addNode("infoSubstPatr");
		infoSubstPatr.addNode("indSubstPatr", indSubstPatr, true);
		infoSubstPatr.addNode("percRedContrib", ESocialUtils.formatarDecimal(percRedContrib, 2, false), true);
		
		if(fatorMes.compareTo(BigDecimal.ZERO) > 0 || fator13.compareTo(BigDecimal.ZERO) > 0) {
			ElementXml infoAtivConcom = evtInfoComplPer.addNode("infoAtivConcom");
			infoAtivConcom.addNode("fatorMes", ESocialUtils.formatarDecimal(fatorMes, 2, false), true);
			infoAtivConcom.addNode("fator13", ESocialUtils.formatarDecimal(fator13, 2, false), true);
		}
		
		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==