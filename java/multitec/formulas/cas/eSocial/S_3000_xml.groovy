package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;

public class S_3000_xml extends FormulaBase {
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		def indApuracao = get("indApuracao");
		def perApur = get("perApur");
		def tpAmb = get("tpAmb");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		
		if(indApuracao == null) indApuracao = aaa15.getAaa15dados().get("indApuracao")
		if(perApur == null) perApur = aaa15.getAaa15dados().get("perApur")
		if(tpAmb == null) tpAmb = 1
		
		def tpEvento = aaa15.aaa15dados.getString("tpEvento");
		def nrRecEvt = aaa15.aaa15dados.getString("nrRecEvt");
		def cpfTrab = aaa15.aaa15dados.getString("cpfTrab");
		def nisTrab = aaa15.aaa15dados.getString("nisTrab");
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtExclusao/v_S_01_01_00");

		ElementXml evtExclusao = eSocial.addNode("evtExclusao");
		evtExclusao.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtExclusao.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtExclusao.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti + 1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if (aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		} else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml infoExclusao = evtExclusao.addNode("infoExclusao");
		infoExclusao.addNode("tpEvento", tpEvento, true);
		infoExclusao.addNode("nrRecEvt", nrRecEvt, true);

		if (cpfTrab != null) {
			ElementXml ideTrabalhador = infoExclusao.addNode("ideTrabalhador");
			ideTrabalhador.addNode("cpfTrab", StringUtils.extractNumbers(cpfTrab), true);
		}

		if(tpEvento.equals("S-1200") || tpEvento.equals("S-1202") || tpEvento.equals("S-1207")  || tpEvento.equals("S-1210") || tpEvento.equals("S-1280") || tpEvento.equals("S-1300")) {
			ElementXml ideFolhaPagto = infoExclusao.addNode("ideFolhaPagto");
			if(!tpEvento.equals("S-1210")) ideFolhaPagto.addNode("indApuracao", indApuracao, true);
			ideFolhaPagto.addNode("perApur", indApuracao == 1 ? ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY_MM) : ESocialUtils.formatarData(perApur, ESocialUtils.PATTERN_YYYY), true);
		}
		
		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==