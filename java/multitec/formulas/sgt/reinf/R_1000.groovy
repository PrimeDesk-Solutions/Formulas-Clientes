package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.MDate
import sam.core.variaveis.Variaveis
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac11
import sam.model.entities.aa.Aac12
import sam.model.entities.aa.Aac13
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_1000 extends FormulaBase {

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
		Integer tpAmb = 1;
		
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(Variaveis.obter().getAac10().aac10id);
		Aac13 aac13 = aac10.getAac13fiscal();
		Aac11 aac11 = aac10.getAac11sgc();
		Aac12 aac12 = aac10.getAac12sfp();
		
		ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtInfoContribuinte/v1_05_01");
		
		ElementXml evtInfoContri = reinf.addNode("evtInfoContri");
		evtInfoContri.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtInfoContri.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideContri = evtInfoContri.addNode("ideContri");
		ideContri.addNode("tpInsc", aac10.aac10ti.equals(0) ? "1" : "2", true);
		ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);

		ElementXml infoContri = evtInfoContri.addNode("infoContri");
		int tipo = ReinfUtils.isInclusao(aac13.aac13reDti, periodo) ? 0 : 1;
		ElementXml operacao = infoContri.addNode(tipo == 0 ? "inclusao" : tipo == 1 ? "alteracao" : "exclusao");

		if(tipo == 2) {
			ElementXml idePeriodo = operacao.addNode("idePeriodo");
			idePeriodo.addNode("iniValid", ReinfUtils.formatarData(aac13.aac13reDti, ReinfUtils.PATTERN_YYYY_MM), true);
			idePeriodo.addNode("fimValid", ReinfUtils.formatarData(aac13.aac13reDti, ReinfUtils.PATTERN_YYYY_MM), false);
			
		}else {
			ElementXml idePeriodo = operacao.addNode("idePeriodo");
			idePeriodo.addNode("iniValid", tipo == 0 ? ReinfUtils.formatarData(aac13.aac13reDti, ReinfUtils.PATTERN_YYYY_MM) : ReinfUtils.formatarData(aac13.aac13reDti, ReinfUtils.PATTERN_YYYY_MM), true);
			idePeriodo.addNode("fimValid", null, false);
			
			ElementXml infoCadastro = operacao.addNode("infoCadastro");
			infoCadastro.addNode("classTrib", StringUtils.ajustString(aac13.getAac13classTrib()?.getAaj01eSocial()?: null, 2, '0', true), true);
			infoCadastro.addNode("indEscrituracao", aac11 == null ? "1" : aac11.aac11ecd, true);
			infoCadastro.addNode("indDesoneracao", aac12 == null ? "0" : aac12.aac12desFolha, true);
			infoCadastro.addNode("indAcordoIsenMulta", 0, true);
			infoCadastro.addNode("indSitPJ", aac12 == null ? "0" : aac12.aac12sitPj, true);
			
			ElementXml contato = infoCadastro.addNode("contato");
			contato.addNode("nmCtt", aac10.aac10cNome != null ? aac10.aac10cNome : "", true);
			contato.addNode("cpfCtt", aac10.aac10cCpf != null ? StringUtils.extractNumbers(aac10.aac10cCpf) : "", true);
			
			String foneFixo = StringUtils.extractNumbers(aac10.aac10cDddFone == null ? aac10.aac10cFone : aac10.aac10cDddFone + aac10.aac10cFone);
			contato.addNode("foneFixo", foneFixo.length() == 0 ? null : foneFixo, true);
			contato.addNode("foneCel", null, false);
			contato.addNode("email", aac10.aac10cEmail, false);

			ElementXml softHouse = infoCadastro.addNode("softHouse");
			softHouse.addNode("cnpjSoftHouse", StringUtils.extractNumbers(aac10.aac10aCnpj), true);
			softHouse.addNode("nmRazao", aac10.aac10aNome, true);
			softHouse.addNode("nmCont", aac10.aac10aNome, true, 70);
			String telefone = aac10.aac10aDddFone == null ? aac10.aac10aFone : aac10.aac10aDddFone + aac10.aac10aFone;
			softHouse.addNode("telefone", telefone.length() == 0 ? null : telefone, false);
			softHouse.addNode("email", aac10.aac10aEmail, false, 60);
		}
		
		Aaa17 aaa17 = reinfService.comporAaa17("R-1000", periodo, isRetificacao);
		aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
		
		if(reinfService.confirmarGeracaoXML("R-1000", periodo, aaa17.getAaa17xmlEnvio(), "tpInsc")) {
			aaa17s.add(aaa17);
		}
		
		put("aaa17s", aaa17s);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==