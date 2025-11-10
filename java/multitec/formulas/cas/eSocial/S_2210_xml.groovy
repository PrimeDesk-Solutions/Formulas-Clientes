package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fa.Fab02;
import sam.model.entities.fa.Fab0201;
import sam.model.entities.fa.Fab0202;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2210_xml extends FormulaBase {
	private Aaa15 aaa15;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		
		Fab02 fab02 = getAcessoAoBanco().buscarRegistroUnicoById("Fab02", aaa15.aaa15registro);
		Abh80 abh80 = getAcessoAoBanco().buscarRegistroUnicoById("Abh80", fab02.fab02trab.abh80id);
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		def tpAmb = 1;
		def indRetif = aaa15.aaa15tipo == Aaa15.TIPO_RETIFICACAO ? 2 : 1;

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtCAT/v_S_01_01_00");
		ElementXml evtCAT = eSocial.addNode("evtCAT");
		evtCAT.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtCAT.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtCAT.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml ideVinculo = evtCAT.addNode("ideVinculo");
		ideVinculo.addNode("cpfTrab", StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true), true);
		ideVinculo.addNode("matricula", abh80.abh80codigo, false);
		//if(abh80.abh80categ != null) ideVinculo.addNode("codCateg", abh80.abh80categ.aap14eSocial, false);
		
		ElementXml cat = evtCAT.addNode("cat");
		cat.addNode("dtAcid", ESocialUtils.formatarData(fab02.fab02data, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		cat.addNode("tpAcid", fab02.fab02codAcid?.aap58codigo?: null, true);
		cat.addNode("hrAcid", ESocialUtils.formatarHora(fab02.fab02hora, ESocialUtils.PATTERN_HHMM), false);
		cat.addNode("hrsTrabAntesAcid", ESocialUtils.formatarHora(fab02.fab02horasTrab, ESocialUtils.PATTERN_HHMM), true);
		cat.addNode("tpCat", fab02.fab02tipo, true);
		cat.addNode("indCatObito", fab02.fab02obito == 0 ? 'N' : 'S', true);
		cat.addNode("dtObito", ESocialUtils.formatarData(fab02.fab02dtObito, ESocialUtils.PATTERN_YYYY_MM_DD), false);
		cat.addNode("indComunPolicia", fab02.fab02policia == 0 ? 'N' : 'S', true);
		cat.addNode("codSitGeradora", fab02.fab02sitGer?.aap53codigo?: null, true);
		cat.addNode("iniciatCAT", fab02.fab02iniciativa, true);
		cat.addNode("obsCAT", fab02.fab02obs, false);
		cat.addNode("ultDiaTrab", ESocialUtils.formatarData(fab02.fab02data, ESocialUtils.PATTERN_YYYY_MM_DD),true)
		cat.addNode("houveAfast", fab02.fab02amAfast_Zero == 0 ? 'N' : 'S', true)
		
		ElementXml localAcidente = cat.addNode("localAcidente");
		localAcidente.addNode("tpLocal", fab02.fab02laTipo, true);
		localAcidente.addNode("dscLocal", fab02.fab02laEspecLocal, false);
		localAcidente.addNode("tpLograd", fab02.fab02laTpLog?.aap15codigo?: null, true);
		localAcidente.addNode("dscLograd", fab02.fab02laEndereco, true);
		localAcidente.addNode("nrLograd", fab02.fab02laNumero, true);
		localAcidente.addNode("complemento", fab02.fab02laComplem, false);
		localAcidente.addNode("bairro", fab02.fab02laBairro, false);
		localAcidente.addNode("cep", fab02.fab02laCep, false);
		localAcidente.addNode("codMunic", fab02.fab02laMunicipio?.aag0201ibge?: null, false);
		
		Aag02 aag02 = fab02.fab02laMunicipio != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aag02", fab02.fab02laMunicipio.aag0201uf.aag02id) : null;
		localAcidente.addNode("uf", aag02 != null ? aag02.aag02uf : null, false);
		//localAcidente.addNode("pais", fab02.fab02laPais?.aag01eSocial?: null, false);
		//localAcidente.addNode("codPostal", fab02.fab02laCep, false);
		
		ElementXml ideLocalAcid = localAcidente.addNode("ideLocalAcid");
		ideLocalAcid.addNode("tpInsc", fab02.fab02laTi, true);
		ideLocalAcid.addNode("nrInsc", StringUtils.extractNumbers(fab02.fab02laNi), true);
		
		String sqlFab0201s = "SELECT * FROM Fab0201 AS fab0201 INNER JOIN FETCH fab0201.fab0201parte AS aap56 WHERE fab0201.fab0201cat = :fab02id";
		List<Fab0201> fab0201s = getAcessoAoBanco().buscarListaDeRegistros(sqlFab0201s, Parametro.criar("fab02id",fab02.fab02id));
		if(fab0201s != null && fab0201s.size() > 0) {
			for(Fab0201 fab0201 : fab0201s) {
				ElementXml parteAtingida = cat.addNode("parteAtingida");
				parteAtingida.addNode("codParteAting", fab0201.fab0201parte.aap56codigo, true);
				parteAtingida.addNode("lateralidade", fab0201.fab0201lateralidade, true);
			}
		}
		
		String sqlFab0202s = "SELECT * FROM Fab0202 AS fab0202 INNER JOIN FETCH fab0202.fab0202agente AS aap55 WHERE fab0202.fab0202cat = :fab02id";
		List<Fab0202> fab0202s = getAcessoAoBanco().buscarListaDeRegistros(sqlFab0202s, Parametro.criar("fab02id", fab02.fab02id));
		if(fab0202s != null && fab0202s.size() > 0) {
			for(Fab0202 fab0202 : fab0202s) {
				ElementXml agenteCausador = cat.addNode("agenteCausador");
				agenteCausador.addNode("codAgntCausador", fab0202.fab0202agente.aap55codigo, true);
			}
		}
		
		if(fab02.fab02amData != null) {
			ElementXml atestado = cat.addNode("atestado");
			atestado.addNode("dtAtendimento", ESocialUtils.formatarData(fab02.fab02amData, ESocialUtils.PATTERN_YYYY_MM_DD), true);
			atestado.addNode("hrAtendimento", ESocialUtils.formatarHora(fab02.fab02amHora, ESocialUtils.PATTERN_HHMM), true);
			atestado.addNode("indInternacao", fab02.fab02amInternacao == 0 ? 'N' : 'S', true);
			atestado.addNode("durTrat", fab02.fab02amDurTrat, true);
			atestado.addNode("indAfast", fab02.fab02amAfast == 0 ? 'N' : 'S', true);
			atestado.addNode("dscLesao", fab02.fab02amLesao.aap54codigo, true);
			atestado.addNode("dscCompLesao", fab02.fab02amCompl, false);
			atestado.addNode("diagProvavel", fab02.fab02amDiag, false);
			atestado.addNode("codCID", fab02.fab02amCid.aap13codigo, true);
			atestado.addNode("observacao", fab02.fab02amObs, false);
			
			ElementXml emitente = atestado.addNode("emitente");
			emitente.addNode("nmEmit", fab02.fab02amMedico, true);
			emitente.addNode("ideOC", fab02.fab02amOc, true);
			emitente.addNode("nrOC", fab02.fab02amNiOc, true);
			emitente.addNode("ufOC", fab02.fab02amUfOc, false);
		}
		
		if(fab02.fab02catOrigem != null) {
			ElementXml catOrigem = cat.addNode("catOrigem");
			catOrigem.addNode("nrRecCatOrig", fab02.fab02catOrigem.fab02num, true);
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