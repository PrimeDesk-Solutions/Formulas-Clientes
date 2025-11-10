package multitec.formulas.cas.eSocial;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aap02
import sam.model.entities.ab.Abh80;
import sam.model.entities.fa.Fab03;
import sam.model.entities.fa.Fab0301;
import sam.model.entities.fa.Fab0302;
import sam.model.entities.fa.Fab0303;
import sam.model.entities.fa.Fab03031;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2240_xml extends FormulaBase {
	private Aaa15 aaa15;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		Fab03 fab03 = getAcessoAoBanco().buscarRegistroUnicoById("Fab03", aaa15.aaa15registro);
		Abh80 abh80 = getAcessoAoBanco().buscarRegistroUnicoById("Abh80", fab03.fab03trab.abh80id);
		
		def tpAmb = 1;
		def indRetif = aaa15.aaa15tipo == Aaa15.TIPO_RETIFICACAO ? 2 : 1;
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtExpRisco/v_S_01_01_00");
		ElementXml evtExpRisco = eSocial.addNode("evtExpRisco");
		evtExpRisco.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtExpRisco.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtExpRisco.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml ideVinculo = evtExpRisco.addNode("ideVinculo");
		ideVinculo.addNode("cpfTrab", StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true), true);
		ideVinculo.addNode("matricula", abh80.abh80codigo, false);
		if(abh80.abh80categ != null && abh80.abh80tipo != 0) ideVinculo.addNode("codCateg", abh80.abh80categ.aap14eSocial, false);
		
		ElementXml infoExpRisco = evtExpRisco.addNode("infoExpRisco");
		infoExpRisco.addNode("dtIniCondicao",fab03.fab03dtI, true);
//		infoExpRisco.addNode("dtFimCondicao", ESocialUtils.formatarData(fab03.fab03dtF), false);
		
		if(fab03.fab0301s != null && fab03.fab0301s.size() > 0) {
			for(Fab0301 fab0301 : fab03.fab0301s) {
				fab0301 = getAcessoAoBanco().buscarRegistroUnicoById("Fab0301", fab0301.fab0301id);
				ElementXml infoAmb = infoExpRisco.addNode("infoAmb");
				infoAmb.addNode("localAmb", fab0301.fab0301amb.abh01tipo, true);
				infoAmb.addNode("dscSetor", fab0301.fab0301amb.abh01descr, true, 100);
				infoAmb.addNode("tpInsc", fab0301.fab0301amb.abh01ti, true);
				infoAmb.addNode("nrInsc", StringUtils.extractNumbers(fab0301.fab0301amb.abh01ni), true);
			}
		}
		
		ElementXml infoAtiv = infoExpRisco.addNode("infoAtiv");
		infoAtiv.addNode("dscAtivDes", fab03.fab03descr, true);
		
		if(fab03.fab0303s != null && fab03.fab0303s.size() > 0) {
			for(Fab0303 fab0303 : fab03.fab0303s) {
				ElementXml fatRisco = infoExpRisco.addNode("agNoc");
				Aap02 aap02 = getSession().createCriteria(Aap02.class).addWhere(Criterions.eq("aap02id",fab0303.fab0303fr.aap02id)).get(ColumnType.ENTITY)
				fatRisco.addNode("codAgNoc", aap02.aap02codigo, true);
				fatRisco.addNode("dscAgNoc", aap02.aap02descr, true);
				fatRisco.addNode("tpAval", fab0303.fab0303aval, true);
				fatRisco.addNode("intConc", fab0303.fab0303intConc, false);
				if( fab0303.fab0303aval == 1 && (aap02.aap02codigo == "01.18.001" || aap02.aap02codigo == "02.01.014" ) ) fatRisco.addNode("limTol", fab0303.fab0303limTol, false);
				if( fab0303.fab0303aval == 1 ) fatRisco.addNode("unMed",fab0303.fab0303um,true)
				if( fab0303.fab0303aval == 1 ) fatRisco.addNode("tecMedicao",fab0303.fab0303tecMed,true)
					
					
				ElementXml epcEpi = fatRisco.addNode("epcEpi");
				epcEpi.addNode("utilizEPC", fab0303.fab0303utilEpc, true);
				if(fab0303.fab0303utilEpc == 2) epcEpi.addNode("eficEpc", fab0303.fab0303epcEficaz == 1 ? "S" : "N", true);
				epcEpi.addNode("utilizEPI", fab0303.fab0303utilEpi, true);
				if(fab0303.fab0303utilEpi == 2) epcEpi.addNode("eficEpi", fab0303.fab0303epiEficaz == 1 ? "S" : "N", true);
				
				def fab03031s = getSession().createCriteria(Fab03031.class).addWhere(Criterions.eq("fab03031fr", fab0303.fab0303id)).getList(ColumnType.ENTITY)
				if(fab03031s != null && fab03031s.size() > 0) {
					for(Fab03031 fab03031 : fab03031s) {
						fab03031 = getAcessoAoBanco().buscarRegistroUnicoById("Fab03031", fab03031.fab03031id);
						ElementXml epi = epcEpi.addNode("epi");
						epi.addNode("docAval", fab03031.fab03031ep.aap01caEpi, false);
						
						ElementXml epiCompl = epcEpi.addNode("epiCompl");
						epiCompl.addNode("medProtecao", fab03031.fab03031protCol == 1 ? "S" : "N", true);
						epiCompl.addNode("condFuncto", fab03031.fab03031funcUso == 1 ? "S" : "N", true);
						epiCompl.addNode("usoInint", fab03031.fab03031usoInint == 1 ? "S" : "N", true);
						epiCompl.addNode("przValid", fab03031.fab03031valid == 1 ? "S" : "N", true);
						epiCompl.addNode("periodicTroca", fab03031.fab03031troca == 1 ? "S" : "N", true);
						epiCompl.addNode("higienizacao", fab03031.fab03031higiene == 1 ? "S" : "N", true);
					}
				}
			}
		}
		
		if(fab03.fab0302s != null && fab03.fab0302s.size() > 0) {
			for(Fab0302 fab0302 : fab03.fab0302s) {
				fab0302 = getAcessoAoBanco().buscarRegistroUnicoById("Fab0302", fab0302.fab0302id);
				ElementXml respReg = infoExpRisco.addNode("respReg");
				//respReg.addNode("cpfResp", StringUtils.extractNumbers(fab0302.fab0302cpf), true);
				respReg.addNode("cpfResp", StringUtils.extractNumbers(fab0302.fab0302resp.fab05cpf), true);
				respReg.addNode("ideOC", fab0302.fab0302resp.fab05ocTipo, true);
				if(fab0302.fab0302resp.fab05ocTipo == 9) respReg.addNode("dscOC", fab0302.fab0302resp.fab05ocDescr, false);
				respReg.addNode("nrOC", fab0302.fab0302resp.fab05ocNi, true);
				respReg.addNode("ufOC", fab0302.fab0302resp.fab05ocUf, true);
			}
		}
		
		if(fab03.fab03obs != null && fab03.fab03descrMet != null) {
			ElementXml obs = infoExpRisco.addNode("obs");
			obs.addNode("obsCompl", fab03.fab03obs, true);
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