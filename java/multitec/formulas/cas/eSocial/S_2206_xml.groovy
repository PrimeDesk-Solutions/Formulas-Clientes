package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Abh8004;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2206_xml extends FormulaBase {
	private Aaa15 aaa15;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		Abh80 abh80 = getAcessoAoBanco().buscarRegistroUnicoById("Abh80", aaa15.aaa15registro);
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		def tpAmb = 1;
		def indRetif = aaa15.aaa15tipo == Aaa15.TIPO_RETIFICACAO ? 2 : 1;

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtAltContratual/v_S_01_03_00");
		ElementXml evtAltContratual = eSocial.addNode("evtAltContratual");
		evtAltContratual.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtAltContratual.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtAltContratual.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti + 1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml ideVinculo = evtAltContratual.addNode("ideVinculo");
		ideVinculo.addNode("cpfTrab", abh80.abh80cpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true) : null, true);
		ideVinculo.addNode("matricula", abh80.abh80codigo, true);

		ElementXml altContratual = evtAltContratual.addNode("altContratual");
		altContratual.addNode("dtAlteracao", ESocialUtils.formatarData(MDate.date(), ESocialUtils.PATTERN_YYYY_MM_DD), true);
		altContratual.addNode("dtEf", ESocialUtils.formatarData(abh80.abh80dtAlt, ESocialUtils.PATTERN_YYYY_MM_DD), false);
		altContratual.addNode("dscAlt", abh80.abh80justAlt, false);

		ElementXml vinculo = altContratual.addNode("vinculo");
		vinculo.addNode("tpRegPrev", "1", true);

		ElementXml infoRegimeTrab = vinculo.addNode("infoRegimeTrab");
		ElementXml infoCeletista = infoRegimeTrab.addNode("infoCeletista");
		infoCeletista.addNode("tpRegJor", abh80.abh80regJor, true);
		infoCeletista.addNode("natAtividade", abh80.abh80natAtiv, true);
		infoCeletista.addNode("dtBase", abh80.abh80mesDtBase, false);
		infoCeletista.addNode("cnpjSindCategProf", abh80.abh80sindSindical != null ? StringUtils.extractNumbers(abh80.abh80sindSindical.abh03cnpj) : null, true);

		if (abh80.abh80ttJust != null && abh80.abh80ttJust.length() > 0) {
			ElementXml trabTemporario = infoCeletista.addNode("trabTemporario");
			trabTemporario.addNode("justProrr", abh80.abh80ttJust, true);
		}

		if(abh80.abh80aprendiz == 1) {
			String aprendizNiPrat = StringUtils.extractNumbers(abh80.abh80aprendizNi);
			ElementXml aprend = infoCeletista.addNode("aprend");
			aprend.addNode("indAprend", abh80.abh80aprendizMod, true)
			if(abh80.abh80aprendizMod == 1) aprend.addNode("cnpjEntQual", StringUtils.ajustString(aprendizNiPrat, 14, '0', false) ,true)
			aprend.addNode("tpInsc", abh80.abh80aprendizTi+1, true);
			String aprendizNi = StringUtils.extractNumbers(abh80.abh80aprendizNi);
			if(abh80.abh80aprendizTi == 0) {
				aprendizNi = StringUtils.ajustString(aprendizNi, 14, '0', false);
			}else {
				aprendizNi = StringUtils.ajustString(aprendizNi, 11, '0', true);
			}
			aprend.addNode("nrInsc", aprendizNi, true);
			if(abh80.abh80aprendizMod == 2) aprend.addNode("cnpjPrat", StringUtils.ajustString(aprendizNiPrat, 14, '0', false) ,true)
		}

		ElementXml infoContrato = vinculo.addNode("infoContrato");
		infoContrato.addNode("nmCargo", abh80.abh80cargo?.abh05nome?: null, false);
		String cbo = abh80.abh80cargo.abh05cbo != null ? getAcessoAoBanco().obterString("SELECT aap03codigo FROM Aap03 WHERE aap03id = :aap03id", criarParametroSql("aap03id", abh80.abh80cargo.abh05cbo.aap03id)) : null;
		infoContrato.addNode("CBOCargo", cbo, false);
		//infoContrato.addNode("nmFuncao", abh80.abh80cargo == null ? null : abh80.abh80cargo.abh05funcao, false);
		infoContrato.addNode("codCateg", abh80.abh80categ?.aap14eSocial?: null, true);
		
		ElementXml remuneracao = infoContrato.addNode("remuneracao");
		remuneracao.addNode("vrSalFx", ESocialUtils.formatarDecimal(abh80.abh80salario, 2, false), true);
		remuneracao.addNode("undSalFixo", abh80.abh80unidPagto != null ? abh80.abh80unidPagto.aap18eSocial : null, true);
		remuneracao.addNode("dscSalVar", abh80.abh80salVar, false);

		ElementXml duracao = infoContrato.addNode("duracao");
		String tpContr = String.valueOf(abh80.abh80prazoConTrab);
		if(tpContr != null && !tpContr.equals("1")) tpContr = "2";
		duracao.addNode("tpContr", tpContr, true);
		if(tpContr != null && tpContr.equals("2")) duracao.addNode("dtTerm", ESocialUtils.formatarData(abh80.abh80dataTerm, ESocialUtils.PATTERN_YYYY_MM_DD), false);

		ElementXml localTrabalho = infoContrato.addNode("localTrabalho");
		ElementXml localTrabGeral = localTrabalho.addNode("localTrabGeral");
		localTrabGeral.addNode("tpInsc", aac10.aac10ti + 1, true);
		localTrabGeral.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true), true);

		ElementXml horContratual = infoContrato.addNode("horContratual");
		horContratual.addNode("qtdHrsSem", abh80.abh80hs, true);
		horContratual.addNode("tpJornada", abh80.abh80jorTipo, true);
		horContratual.addNode("tmpParc", abh80.abh80regParc, true);
		horContratual.addNode("horNoturno", abh80.abh80jorNot == 0 ? "N" : "S", true);
		horContratual.addNode("dscJorn", abh80.abh80jorDescr, true);
		
		if(abh80.abh8004s != null && abh80.abh8004s.size() > 0) {
			for(Abh8004 abh8004 : abh80.abh8004s) {
				if(abh8004.abh8004ficha == 1) {
					ElementXml observacoes = infoContrato.addNode("observacoes");
					observacoes.addNode("observacao", abh8004.abh8004nota, true);
				}
			}
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}

	private String getRecibo() {
		String sqlAaa15Anterior = "SELECT * FROM Aaa15 WHERE aaa15evento = :aaa15evento AND aaa15cnpj = :aaa15cnpj AND " + 
		                          "aaa15tabela = :aaa15tabela AND aaa15registro = :aaa15registro AND aaa15status = :aaa15status " + 
								  "ORDER BY aaa15id DESC LIMIT 1";

		Aaa15 aaa15Anterior = getAcessoAoBanco().buscarRegistroUnico(sqlAaa15Anterior,
				Parametro.criar("aaa15evento", aaa15.aaa15evento.aap50id),
				Parametro.criar("aaa15cnpj", aaa15.aaa15cnpj),
				Parametro.criar("aaa15tabela", aaa15.aaa15tabela),
				Parametro.criar("aaa15registro", aaa15.aaa15registro),
				Parametro.criar("aaa15status", aaa15.STATUS_APROVADO));

		return aaa15Anterior != null ? aaa15Anterior.aaa15retRec : null;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==