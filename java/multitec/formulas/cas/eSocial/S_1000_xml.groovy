package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac12
import sam.model.entities.aa.Aac13;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;

public class S_1000_xml extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		Integer tpAmb = 1;
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);

		Aac12 aac12 = aac10.aac12sfp;
		Aac13 aac13 = aac10.aac13fiscal;

		String ni = StringUtils.extractNumbers(aac10.aac10ni);

		if(aac10.aac10ti == 0) ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		if(aac10.aac10ti == 1) ni = StringUtils.ajustString(ni, 11, '0', true);
		if(aac12 == null) throw new ValidacaoException("Necessário informar os Parâmetros SFP da empresa.");
		if(aac13 == null) throw new ValidacaoException("Necessário informar os Parâmetros Fiscais da empresa.");

		String aac10esDti = ESocialUtils.formatarData(aac10.aac10esDti, ESocialUtils.PATTERN_YYYY_MM)
		String aaj01eSocial = StringUtils.ajustString(aac13.aac13classTrib?.aaj01eSocial, 2, '0', true)

		Integer aac10ti = aac10.aac10ti + 1 // 1 - CNPJ / 2 - CPF

		Boolean geraIndPorteEmInfoCadastro = aac13.aac13regTrib == 6 && !(aac13.aac13classTrib?.aaj01codigo?.equalsIgnoreCase("21") || aac13.aac13classTrib?.aaj01codigo?.equalsIgnoreCase("22"))
		Boolean isAlteracao = aaa15.aaa15tipo == Aaa15.TIPO_ALTERACAO
		Boolean isInclusao = aaa15.aaa15tipo == Aaa15.TIPO_INCLUSAO
		Boolean isExclusao = !(isInclusao || isAlteracao)
		Boolean isCNPJ = aac10ti == 1
		


		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtInfoEmpregador/v_S_01_01_00");

		ElementXml evtInfoEmpregador = eSocial.addNode("evtInfoEmpregador");
		evtInfoEmpregador.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtInfoEmpregador.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtInfoEmpregador.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10ti, true);
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml infoEmpregador = evtInfoEmpregador.addNode("infoEmpregador");
		if( isInclusao || isAlteracao ) {
			ElementXml elemento = infoEmpregador.addNode(isInclusao? "inclusao" : "alteracao");

			ElementXml idePeriodo = elemento.addNode("idePeriodo");
			idePeriodo.addNode("iniValid", aac10esDti , true);

			ElementXml infoCadastro = elemento.addNode("infoCadastro");
			infoCadastro.addNode("classTrib", aaj01eSocial, true);
			if( isCNPJ ) infoCadastro.addNode("indCoop", aac12.aac12coop, false);
			if( isCNPJ ) infoCadastro.addNode("indConstr", aac12.aac12constr, false);
			infoCadastro.addNode("indDesFolha", aac12.aac12desFolha, true);
			infoCadastro.addNode("indOpcCP", aac12.aac12tribPR, false);
			if( geraIndPorteEmInfoCadastro ) infoCadastro.addNode("indPorte", 'S', false);
			infoCadastro.addNode("indOptRegEletron", aac12.aac12regEletron, true);

			if( aac12.aac12eiMinLei != null ) {
				ElementXml dadosIsencao = infoCadastro.addNode("dadosIsencao");
				dadosIsencao.addNode("ideMinLei", aac12.aac12eiMinLei, true);
				dadosIsencao.addNode("nrCertif", aac12.aac12eiCertif, true);
				dadosIsencao.addNode("dtEmisCertif", aac12.aac12eiDtEmis, true);
				dadosIsencao.addNode("dtVencCertif", aac12.aac12eiDtVcto, true);
				dadosIsencao.addNode("nrProtRenov", aac12.aac12eiProtRenov, false);
				dadosIsencao.addNode("dtProtRenov", aac12.aac12eiDtProt, false);
				dadosIsencao.addNode("dtDou", aac12.aac12eiDtDou, false);
				dadosIsencao.addNode("pagDou", aac12.aac12eiPagDou, false);
			}

			if( isAlteracao ) {
				ElementXml novaValidade = elemento.addNode("novaValidade");
				novaValidade.addNode("iniValid", aac10esDti, true);
			}
		}
		
		if(isExclusao){
			ElementXml exclusao = infoEmpregador.addNode("exclusao");
			ElementXml idePeriodo = exclusao.addNode("idePeriodo");
			idePeriodo.addNode("iniValid", aac10esDti, true);
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==