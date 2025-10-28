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

public class S_2298_xml extends FormulaBase {
	Aaa15 aaa15;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		def indRetif = get("indRetif");
		def nrRecibo = get("nrRecibo");
		def cpfTrab = get("cpfTrab");
		def nisTrab = get("nisTrab");
		def matricula = get("matricula");
		def tpReint = get("tpReint");
		def nrProcJud = get("nrProcJud");
		def nrLeiAnistia = get("nrLeiAnistia");
		def dtEfetRetorno = get("dtEfetRetorno");
		def dtEfeito = get("dtEfeito");
		def indPagtoJuizo = get("indPagtoJuizo");
		def tpAmb = get("tpAmb");
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtReintegr/v_S_01_03_00");
		ElementXml evtReintegr = eSocial.addNode("evtReintegr");
		evtReintegr.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtReintegr.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(nrRecibo != null) ideEvento.addNode("nrRecibo", nrRecibo, false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtReintegr.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml ideVinculo = evtReintegr.addNode("ideVinculo");
		ideVinculo.addNode("cpfTrab", StringUtils.ajustString(StringUtils.extractNumbers(cpfTrab), 11, '0', true), true);
		ideVinculo.addNode("nisTrab", StringUtils.extractNumbers(nisTrab), true);
		ideVinculo.addNode("matricula", matricula, true);
		
		ElementXml infoReintegr = evtReintegr.addNode("infoReintegr");
		infoReintegr.addNode("tpReint", tpReint, true);
		infoReintegr.addNode("nrProcJud", nrProcJud, false);
		infoReintegr.addNode("nrLeiAnistia", nrLeiAnistia, false);
		infoReintegr.addNode("dtEfetRetorno", ESocialUtils.formatarData(dtEfetRetorno, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		infoReintegr.addNode("dtEfeito", ESocialUtils.formatarData(dtEfeito, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		infoReintegr.addNode("indPagtoJuizo", indPagtoJuizo == 1 ? "S" : "N", true);
		
		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==