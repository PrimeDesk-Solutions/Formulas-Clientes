package multitec.formulas.sgt.reinf

import java.time.LocalDate

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

class R_9000 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_REINF;
	}

	@Override
	public void executar() {
		ReinfService reinfService = instanciarService(ReinfService.class);
		
		boolean isRetificacao = get("isRetificacao");
		LocalDate periodo = get("periodo");
		String evento = get("evento");
		String recibo = get("recibo");
		
		List<Aaa17> aaa17s = new ArrayList();
		Aac10 aac10 = Variaveis.obter().getAac10();
		Integer tpAmb = 1;
		
		
		ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtExclusao/v1_05_01");
		
		ElementXml evtExclusao = reinf.addNode("evtExclusao");
		evtExclusao.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
	
		ElementXml ideEvento = evtExclusao.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideContri = evtExclusao.addNode("ideContri");
		ideContri.addNode("tpInsc", aac10.aac10ti.equals(0) ? "1" : "2", true);
		ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
		
		ElementXml infoExclusao = evtExclusao.addNode("infoExclusao");
		infoExclusao.addNode("tpEvento", evento, true);
		infoExclusao.addNode("nrRecEvt", recibo, true);
		infoExclusao.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true);
		
		Aaa17 aaa17 = reinfService.comporAaa17("R-9000", periodo, isRetificacao);
		aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
		
		if(reinfService.confirmarGeracaoXML("R-9000", periodo, aaa17.getAaa17xmlEnvio(), "tpInsc", "tpEvento", "nrRecEvt")) {
			aaa17s.add(aaa17);
		}
		
		put("aaa17s", aaa17s);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==