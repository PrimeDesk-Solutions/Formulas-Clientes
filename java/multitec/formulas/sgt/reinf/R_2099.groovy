package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.Variaveis
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_2099 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_REINF;
	}

	@Override
	public void executar() {
		ReinfService reinfService = instanciarService(ReinfService.class);
		
		boolean isRetificacao = get("isRetificacao");
		LocalDate periodo = get("periodo");
		List<Aaa17> aaa17s = new ArrayList();
		
		Aac10 aac10 = Variaveis.obter().getAac10();
		
		Integer tpAmb = 1;
		
		
		ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtFechamento/v1_05_01");
		
		ElementXml evtFechaEvPer = reinf.addNode("evtFechaEvPer")
		evtFechaEvPer.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtFechaEvPer.addNode("ideEvento");
		ideEvento.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideContri = evtFechaEvPer.addNode("ideContri");
		ideContri.addNode("tpInsc", aac10.aac10ti.equals(0) ? "1" : "2", true);
		ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
		
		ElementXml ideRespInf = evtFechaEvPer.addNode("ideRespInf");
		ideRespInf.addNode("nmResp", aac10.aac10rNome, true);
		ideRespInf.addNode("cpfResp", StringUtils.extractNumbers(aac10.aac10rCpf), true);
		
		String foneFixo = aac10.aac10rDddFone != null ? aac10.aac10rDddFone + aac10.aac10rFone : aac10.aac10rFone;
		ideRespInf.addNode("telefone", foneFixo, false);
		ideRespInf.addNode("email", aac10.aac10rEmail, false);
		
		ElementXml infoFech = evtFechaEvPer.addNode("infoFech");
		boolean temR2010 = reinfService.existeLayoutPeloPeriodoNoReinf("R-2010", aac10.aac10id, periodo);
		boolean temR2020 = reinfService.existeLayoutPeloPeriodoNoReinf("R-2020", aac10.aac10id, periodo);
		boolean temR2050 = reinfService.existeLayoutPeloPeriodoNoReinf("R-2050", aac10.aac10id, periodo);
		boolean temR2055 = reinfService.existeLayoutPeloPeriodoNoReinf("R-2055", aac10.aac10id, periodo);
		boolean temR2060 = reinfService.existeLayoutPeloPeriodoNoReinf("R-2060", aac10.aac10id, periodo);
		
		infoFech.addNode("evtServTm", temR2010 ? "S" : "N", true);
		infoFech.addNode("evtServPr", temR2020 ? "S" : "N", true);
		infoFech.addNode("evtAssDespRec", "N", true);
		infoFech.addNode("evtAssDespRep", "N", true);
		infoFech.addNode("evtComProd", temR2050 ? "S" : "N", true);
		infoFech.addNode("evtCPRB", temR2060 ? "S" : "N", true);
		infoFech.addNode("evtAquis", temR2055 ? "S" : "N", true);
		infoFech.addNode("evtPgtos", null, false);
					
		Aaa17 aaa17 = reinfService.comporAaa17("R-2099", periodo, isRetificacao);
		aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
		
		if(reinfService.confirmarGeracaoXML("R-2099", periodo, aaa17.getAaa17xmlEnvio(), "nrInsc")) {
			aaa17s.add(aaa17);
		}
		
		put("aaa17s", aaa17s);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==