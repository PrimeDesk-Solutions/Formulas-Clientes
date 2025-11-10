package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac12
import sam.model.entities.aa.Aac1201;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;

public class S_1005_xml extends FormulaBase {

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
		if(aac12 == null) throw new ValidacaoException("Necessário informar os Parâmetros SFP da empresa.");

		String aac10ni = StringUtils.extractNumbers(aac10.aac10ni);
		String aac12ni = StringUtils.extractNumbers(aac12.aac12ni);
		String aac10esDti = ESocialUtils.formatarData(aac10.aac10esDti, ESocialUtils.PATTERN_YYYY_MM)
		Integer aac10ti = aac10.aac10ti + 1 // 1 - CNPJ / 2 - CPF

		if(aac10ti == 1) aac10ni = StringUtils.ajustString(aac10ni, 14, '0', false).substring(0, 8);
		if(aac10ti == 2) aac10ni = StringUtils.ajustString(aac10ni, 11, '0', true);
		if(aac10ti == 1) aac12ni = StringUtils.ajustString(aac12ni, 14, '0', true);


		Boolean isAlteracao = aaa15.aaa15tipo == Aaa15.TIPO_ALTERACAO
		Boolean isInclusao = aaa15.aaa15tipo == Aaa15.TIPO_INCLUSAO
		Boolean isExclusao = !(isInclusao || isAlteracao)
		Boolean isCNPJ = aac10ti == 1
		
		String aac12fap = ESocialUtils.formatarDecimal(aac12.aac12fap, 4, false)
		BigDecimal aliqRatAjust = null
		if(aac12.aac12fap != null) aliqRatAjust = aac12.aac12rat * aac12.aac12fap;
		String valueTagAliqRatAjust = ESocialUtils.formatarDecimal(aliqRatAjust, 4, false)
		
		
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtTabEstab/v_S_01_01_00");
		ElementXml evtTabEstab = eSocial.addNode("evtTabEstab");
		evtTabEstab.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));


		ElementXml ideEvento = evtTabEstab.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb);
		ideEvento.addNode("procEmi", "1");
		ideEvento.addNode("verProc", Utils.getVersao());


		ElementXml ideEmpregador = evtTabEstab.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10ti);
		ideEmpregador.addNode("nrInsc", aac10ni);


		ElementXml infoEstab = evtTabEstab.addNode("infoEstab");
		if( isInclusao || isAlteracao ) {
			ElementXml elemento = infoEstab.addNode(isInclusao ? "inclusao" : "alteracao");

			ElementXml ideEstab = elemento.addNode("ideEstab");
			ideEstab.addNode("tpInsc", aac12.aac12ti, true);

			ideEstab.addNode("nrInsc", aac12ni, true);
			ideEstab.addNode("iniValid", aac10esDti, true);

			ElementXml dadosEstab = ideEstab.addNode("dadosEstab");
			dadosEstab.addNode("cnaePrep", aac12.aac12cnae, false);

			ElementXml aliqGilrat = dadosEstab.addNode("aliqGilrat");
			aliqGilrat.addNode("aliqRat", aac12.aac12rat, true);
			aliqGilrat.addNode("fap", aac12fap);
			aliqGilrat.addNode("aliqRatAjust", valueTagAliqRatAjust);

			if(aac12.aac12procRat != null) {
				ElementXml procAdmJudRat = aliqGilrat.addNode("procAdmJudRat");
				procAdmJudRat.addNode("tpProc", aac12.aac12procRat.abb40tipo, true);
				procAdmJudRat.addNode("nrProc", aac12.aac12procRat.abb40num, true);
				procAdmJudRat.addNode("codSusp", !aac12.aac12procRat.abb4001s.isEmpty() ? aac12.aac12procRat.abb4001s.stream().findFirst() : null, true);
			}

			if( aac12.aac12procFap != null) {
				ElementXml procAdmJudFap = aliqGilrat.addNode("procAdmJudFap");
				procAdmJudFap.addNode("tpProc", aac12.aac12procFap.abb40tipo, true);
				procAdmJudFap.addNode("nrProc", aac12.aac12procFap.abb40num, true);
				procAdmJudFap.addNode("codSusp", !aac12.aac12procFap.abb4001s.isEmpty() ? aac12.aac12procFap.abb4001s.stream().findFirst() : null, true);
			}

			if(aac12.aac12tpCaePF != null) {
				ElementXml infoCaepf = dadosEstab.addNode("infoCaepf");
				infoCaepf.addNode("tpCaepf", aac12.aac12tpCaePF, true);
			}

			if( aac12.aac12subPatr != null) {
				ElementXml infoObra = dadosEstab.addNode("infoObra");
				infoObra.addNode("indSubstPatrObra", aac12.aac12subPatr, true);
			}


			ElementXml infoTrab = dadosEstab.addNode("infoTrab");

			ElementXml infoApr = infoTrab.addNode("infoApr");
			infoApr.addNode("nrProcJud", aac10.aac12sfp?.aac12procApr?.abb40num?:null, false);

			if( aac10.aac12sfp?.aac1201s != null && aac10.aac12sfp?.aac1201s.size() > 0) {
				for(Aac1201 aac1201 : aac10.aac12sfp.aac1201s) {
					String aac1201cnpj_exactNumber = StringUtils.extractNumbers(aac1201.aac1201cnpj)
					
					ElementXml infoEntEduc = infoApr.addNode("infoEntEduc");
					infoEntEduc.addNode("nrInsc", aac1201cnpj_exactNumber, true);
				}
			}

			if(aac12.aac12pcd != null) {
				ElementXml infoPCD = infoTrab.addNode("infoPCD");
				infoPCD.addNode("nrProcJud", aac12.aac12procPcd?.abb40num?:null, false);
			}

			if( isAlteracao ) {
				ElementXml novaValidade = elemento.addNode("novaValidade");
				novaValidade.addNode("iniValid", aac10esDti, true);
			}
		}
		
		if( isExclusao ){
			ElementXml exclusao = infoEstab.addNode("exclusao");
			ElementXml ideEstab = exclusao.addNode("ideEstab");
			ideEstab.addNode("tpInsc", aac10ti);
			ideEstab.addNode("nrInsc", aac10ni);
			ideEstab.addNode("iniValid", aac10esDti);
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==