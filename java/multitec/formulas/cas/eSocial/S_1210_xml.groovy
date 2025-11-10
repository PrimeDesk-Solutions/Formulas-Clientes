package multitec.formulas.cas.eSocial;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_1210_xml extends FormulaBase{
	private Aaa15 aaa15;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		def tpAmb = get("tpAmb");;
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		Abh80 abh80 = getAcessoAoBanco().buscarRegistroUnicoById("abh80", get("abh80id"));
		LocalDate mesAnoRef = get("perApur");
		def indRetif = aaa15.aaa15tipo == aaa15.TIPO_RETIFICACAO ? 2 : 1;
		List<Fba0101> fba0101sOriginal = get("fba0101s");

		HashMap<String, Fba0101> fba0101s = new HashMap<String, Fba0101>()

		for(fba0101 in fba0101sOriginal) {
			String data = ESocialUtils.formatarData(fba0101.fba0101dtPgto, ESocialUtils.PATTERN_YYYY_MM_DD) + "/" + StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto)
			def ideDmDev = StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto)
			if(fba0101.fba0101tpVlr == 4 ){
				ideDmDev = fba0101.fba0101tpVlr + fba0101.fba0101dtCalc.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
			}
			
			data += "/" + ideDmDev
			if(!fba0101s.containsKey(data)) {
				fba0101s.put(data, fba0101)
			}else{
				fba0101s.get(data).fba0101liqPago += fba0101.fba0101liqPago
			}
		}

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtPgtos/v_S_01_01_00");

		ElementXml evtPgtos = eSocial.addNode("evtPgtos");
		evtPgtos.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtPgtos.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("perApur", ESocialUtils.formatarData(mesAnoRef, ESocialUtils.PATTERN_YYYY_MM), true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtPgtos.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml ideBenef = evtPgtos.addNode("ideBenef");
		ideBenef.addNode("cpfBenef", StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true), true);

		if(fba0101s != null && fba0101s.size() > 0) {
			for (fba0101 in fba0101s) {
				fba0101 = fba0101.getValue()
				def ideDmDev = StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto)
				if(fba0101.fba0101tpVlr == 4 ){
					ideDmDev = fba0101.fba0101tpVlr + fba0101.fba0101dtCalc.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
				}
				def dataPerRef = ideDmDev == "31" ? fba0101.fba0101dtPgto : fba0101.fba0101dtCalc
				ElementXml infoPgto = ideBenef.addNode("infoPgto");
				infoPgto.addNode("dtPgto", ESocialUtils.formatarData(fba0101.fba0101dtPgto, ESocialUtils.PATTERN_YYYY_MM_DD), true);
				infoPgto.addNode("tpPgto", fba0101.fba0101tpPgto, true);
				infoPgto.addNode("perRef", ESocialUtils.formatarData(dataPerRef, ESocialUtils.PATTERN_YYYY_MM), false);
				infoPgto.addNode("ideDmDev", ideDmDev, true);
				infoPgto.addNode("vrLiq", ESocialUtils.formatarDecimal(fba0101.fba0101liqPago, 2, false), true);
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