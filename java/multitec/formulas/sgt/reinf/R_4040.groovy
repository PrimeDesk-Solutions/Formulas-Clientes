package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.xml.ElementXml
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_4040 extends FormulaBase{
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_REINF
	}

	@Override
	public void executar() {
		ReinfService reinfService =  instanciarService(ReinfService.class)

		boolean isRetificacao = get("isRetificacao")
		LocalDate periodo = get("periodo")
		List<Aaa17> aaa17s = new ArrayList()

		Aac10 aac10 = Variaveis.obter().getAac10()

		Integer tpAmb = 1

		Set<Long> idsEmpresas = reinfService.selecionarEmpresasReinf()
		for(idEmpresa in idsEmpresas){
			
			ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtInfoProdRural/v2.1.1")
			
			ElementXml evtBenefNId = reinf.addNode("evtBenefNId")
			evtBenefNId.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni))
			
			String recibo = isRetificacao ? reinfService.buscarAaa17RetRecPeloLayoutPeriodo("R-4040", aac10.aac10id, periodo) : null;
			
			ElementXml ideEvento = reinf.addNode("ideEvento")
			ideEvento.addNode("indRetif", isRetificacao ? 2 : 1, true)
			if(isRetificacao) ideEvento.addNode("nrRecibo", recibo, false)
			ideEvento.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true)
			ideEvento.addNode("tpAmb", tpAmb, true)
			ideEvento.addNode("procEmi", 1, true)
			ideEvento.addNode("verProc", Utils.getVersao(), true)
			
			ElementXml ideContri = reinf.addNode("ideContri")
			ideContri.addNode("tpInsc", 1, true)
			ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8) , true)
			
			Aaa17 aaa17 = reinfService.comporAaa17("R-4040", periodo, isRetificacao);
			aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf))

			if(reinfService.confirmarGeracaoXML("R-4040", periodo, aaa17.getAaa17xmlEnvio(), "nrInsc")) {
				aaa17s.add(aaa17)
			}
		}
		
		put("aaa17s", aaa17s)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==