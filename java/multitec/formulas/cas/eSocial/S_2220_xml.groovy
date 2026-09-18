package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fa.Fab10;
import sam.model.entities.fa.Fab1001;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2220_xml extends FormulaBase {
	private Aaa15 aaa15;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		this.aaa15 = get("aaa15");
		Fab10 fab10 = getAcessoAoBanco().buscarRegistroUnicoById("Fab10", aaa15.aaa15registro);
		Abh80 abh80 = getAcessoAoBanco().buscarRegistroUnicoById("Abh80", fab10.fab10trab.abh80id);
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		def tpAmb = 1;
		def indRetif = aaa15.aaa15tipo == Aaa15.TIPO_RETIFICACAO ? 2 : 1;
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtMonit/v_S_01_01_00");
		ElementXml evtMonit = eSocial.addNode("evtMonit");
		evtMonit.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtMonit.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtMonit.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml ideVinculo = evtMonit.addNode("ideVinculo");
		ideVinculo.addNode("cpfTrab", StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true), true);
		ideVinculo.addNode("matricula", abh80.abh80codigo, false);
		if(abh80.abh80categ != null) ideVinculo.addNode("codCateg", abh80.abh80categ.aap14eSocial, false);
		
		ElementXml exMedOcup = evtMonit.addNode("exMedOcup");
		exMedOcup.addNode("tpExameOcup", fab10.fab10tpExOcup, true);
		
		ElementXml aso = exMedOcup.addNode("aso");
		aso.addNode("dtAso", ESocialUtils.formatarData(fab10.fab10data, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		aso.addNode("resAso", fab10.fab10result, true);
		
		if(fab10.fab1001s != null && fab10.fab1001s.size() > 0) {
			for(Fab1001 fab1001 : fab10.fab1001s) {
				ElementXml exame = aso.addNode("exame");
				exame.addNode("dtExm", ESocialUtils.formatarData(fab1001.fab1001data, ESocialUtils.PATTERN_YYYY_MM_DD), true);
				exame.addNode("procRealizado", fab1001.fab1001proc, false);
				exame.addNode("obsProc", fab1001.fab1001obs, false);
				exame.addNode("ordExame", fab1001.fab1001ordem, true);
				exame.addNode("indResult", fab1001.fab1001result, false);
			}
		}
		
		ElementXml medico = aso.addNode("medico");
		medico.addNode("nmMed", fab10.fab10medNome, true);
		medico.addNode("nrCRM", fab10.fab10medNi, true);
		medico.addNode("ufCRM", fab10.fab10medUf, true);
		
		ElementXml respMonit = exMedOcup.addNode("respMonit");
		respMonit.addNode("cpfResp", fab10.fab10pcmsoCpf, false);
		respMonit.addNode("nmResp", fab10.fab10pcmsoNome, true);
		respMonit.addNode("nrCRM", fab10.fab10pcmsoNi, true);
		respMonit.addNode("ufCRM", fab10.fab10pcmsoUf, true);
				
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