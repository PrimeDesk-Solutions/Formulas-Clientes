package multitec.formulas.cas.eSocial;

import java.time.LocalDate;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.ESocialUtils;

public class S_1298_xml extends FormulaBase {
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
		def perApur = get("perApur");
		def tpAmb = get("tpAmb");
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtReabreEvPer/v_S_01_03_00");
		ElementXml evtReabreEvPer = eSocial.addNode("evtReabreEvPer");
		evtReabreEvPer.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtReabreEvPer.addNode("ideEvento");
		ideEvento.addNode("indApuracao", indApuracao, true);
		ideEvento.addNode("perApur", indApuracao == 1 ? ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY_MM) : ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY), true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtReabreEvPer.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==