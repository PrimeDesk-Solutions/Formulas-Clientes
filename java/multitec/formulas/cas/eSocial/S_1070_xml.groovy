package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abb40;
import sam.model.entities.ab.Abb4001;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;

public class S_1070_xml extends FormulaBase{

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		Integer tpAmb = 1;
		Abb40 abb40 = getAcessoAoBanco().buscarRegistroUnicoById("Abb40", aaa15.aaa15registro);
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);

		Boolean isAlteracao = aaa15.aaa15tipo == Aaa15.TIPO_ALTERACAO
		Boolean isInclusao = aaa15.aaa15tipo == Aaa15.TIPO_INCLUSAO
		Boolean isExclusao = !(isInclusao || isAlteracao)

		Integer aac10ti = aac10.aac10ti + 1  // 1 - CNPJ / 2 - CPF

		String aac10ni = StringUtils.extractNumbers(aac10.aac10ni);
		String abb40esDti = ESocialUtils.formatarData(abb40.abb40esDti, ESocialUtils.PATTERN_YYYY_MM)

		if(aac10ti == 1) aac10ni = StringUtils.ajustString(aac10ni, 14, '0', false).substring(0, 8);
		if(aac10ti != 1) aac10ni = StringUtils.ajustString(aac10ni, 11, '0', true);

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtTabProcesso/v_S_01_01_00");

		ElementXml evtTabProcesso = eSocial.addNode("evtTabProcesso");
		evtTabProcesso.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtTabProcesso.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtTabProcesso.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10ti, true);
		ideEmpregador.addNode("nrInsc", aac10ni, true);

		ElementXml infoProcesso = evtTabProcesso.addNode("infoProcesso");
		if(isInclusao || isAlteracao){
			ElementXml elemento = infoProcesso.addNode(isInclusao ? "inclusao" : "alteracao");
			ElementXml ideProcesso = elemento.addNode("ideProcesso");
			ideProcesso.addNode("tpProc", abb40.abb40tipo, true);
			ideProcesso.addNode("nrProc", StringUtils.extractNumbers(abb40.abb40num), true);

			ideProcesso.addNode("iniValid", abb40esDti, true);

			ElementXml dadosProc = elemento.addNode("dadosProc");
			dadosProc.addNode("indAutoria", abb40.abb40autor, true);
			dadosProc.addNode("indMatProc", abb40.abb40materia, true);
			dadosProc.addNode("observacao", abb40.abb40resumo, false);

			ElementXml dadosProcJud = dadosProc.addNode("dadosProcJud");
			dadosProcJud.addNode("ufVara", abb40.abb40municipio?.aag0201uf?.aag02ibge?: null, true);
			dadosProcJud.addNode("codMunic", abb40.abb40municipio?.aag0201ibge?: null, true);
			dadosProcJud.addNode("idVara", abb40.abb40vara, true);
			Collection<Abb4001> abb4001s = abb40.abb4001s;
			for (Abb4001 abb4001 : abb4001s) {
				String abb4001data = ESocialUtils.formatarData(abb4001.abb4001data, ESocialUtils.PATTERN_MMYYYY)
				
				ElementXml infoSusp = dadosProc.addNode("infoSusp");
				infoSusp.addNode("codSusp", abb4001.abb4001codSusp, true);
				infoSusp.addNode("indSusp", abb4001.abb4001tipoSusp, true);
				infoSusp.addNode("dtDecisao", abb4001data, true);
				infoSusp.addNode("indDeposito", converteCampoIntegerParaSouN(abb4001.abb4001deposito) , true);
			}
			if(isAlteracao) {
				ElementXml novaValidade = elemento.addNode("novaValidade");
			}
		}
		if(isExclusao) {
			ElementXml exclusao = infoProcesso.addNode("exclusao");
			ElementXml ideProcesso = exclusao.addNode("ideProcesso");
			ideProcesso.addNode("tpProc", abb40.abb40tipo, true);
			ideProcesso.addNode("nrProc", StringUtils.extractNumbers(abb40.abb40num), true);

			ideProcesso.addNode("iniValid", abb40esDti, true);
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
	
	private String converteCampoIntegerParaSouN(Integer value) {
		return value == 0 ? "N" : "S"
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==