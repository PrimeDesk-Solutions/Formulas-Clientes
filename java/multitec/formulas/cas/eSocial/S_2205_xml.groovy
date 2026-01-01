package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aap09;
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Abh8002;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2205_xml extends FormulaBase {
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

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtAltCadastral/v_S_01_03_00");
		ElementXml evtAltCadastral = eSocial.addNode("evtAltCadastral");
		evtAltCadastral.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtAltCadastral.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtAltCadastral.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml ideTrabalhador = evtAltCadastral.addNode("ideTrabalhador");
		ideTrabalhador.addNode("cpfTrab", abh80.abh80cpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true) : null, true);

		ElementXml alteracao = evtAltCadastral.addNode("alteracao");
		alteracao.addNode("dtAlteracao", ESocialUtils.formatarData(MDate.date(), ESocialUtils.PATTERN_YYYY_MM_DD), true);

		ElementXml dadosTrabalhador = alteracao.addNode("dadosTrabalhador");
		dadosTrabalhador.addNode("nmTrab", abh80.abh80nome, true);
		dadosTrabalhador.addNode("sexo", abh80.abh80sexo == 0 ? 'M' : 'F', true);
		dadosTrabalhador.addNode("racaCor", abh80.abh80rc?.aap07eSocial?: null, true);
		dadosTrabalhador.addNode("estCiv", abh80.abh80estCivil?.aap08eSocial?: null, false);
		dadosTrabalhador.addNode("grauInstr", abh80.abh80gi?.aap06eSocial?: null, true);
		dadosTrabalhador.addNode("nmSoc", abh80.abh80nomeSocial?: abh80.abh80nomeSocial, false);
		dadosTrabalhador.addNode("paisNac", abh80.abh80nascPais?.aag01eSocial?: null, false);

		ElementXml endereco = dadosTrabalhador.addNode("endereco");
		ElementXml brasil = endereco.addNode("brasil");
		brasil.addNode("tpLograd", abh80.abh80tpLog?.aap15eSocial?: null, true);
		brasil.addNode("dscLograd", abh80.abh80endereco, true);
		brasil.addNode("nrLograd", abh80.abh80numero, true);
		brasil.addNode("complemento", abh80.abh80complem, false);
		brasil.addNode("bairro", abh80.abh80bairro, false);
		brasil.addNode("cep", abh80.abh80cep, true);
		brasil.addNode("codMunic", abh80.abh80municipio?.aag0201ibge?: null, true);

		Aag02 aag02 = abh80.abh80municipio != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aag02", abh80.abh80municipio.aag0201uf.aag02id) : null;
		brasil.addNode("uf", aag02?.aag02uf?: null, true);

		if (abh80.abh80extEndereco != null) {
			ElementXml exterior = endereco.addNode("exterior");
			exterior.addNode("paisResid", abh80.abh80extPais?.aag01eSocial?: null, false);
			exterior.addNode("dscLograd", abh80.abh80extEndereco, true);
			exterior.addNode("nrLograd", abh80.abh80extNumero, true);
			exterior.addNode("complemento", abh80.abh80extComplem, false);
			exterior.addNode("bairro", abh80.abh80extBairro, false);
			exterior.addNode("nmCid", abh80.abh80extCidade, true);
			exterior.addNode("codPostal", abh80.abh80extCodPostal, false);
		}

		if(abh80.abh80dtChegBr != null) {
			ElementXml trabImig = dadosTrabalhador.addNode("trabImig");
			trabImig.addNode("tmpResid", abh80.abh80tmpResid, false);
			trabImig.addNode("condIng", abh80.abh80condIng, true);
		}

		ElementXml infoDeficiencia = dadosTrabalhador.addNode("infoDeficiencia");
		infoDeficiencia.addNode("defFisica", abh80.abh80defFisico == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defVisual", abh80.abh80defVisual == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defAuditiva", abh80.abh80defAuditivo == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defMental", abh80.abh80defMental == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defIntelectual", abh80.abh80defIntelecto == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("reabReadap", abh80.abh80defReabil == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("infoCota", abh80.abh80defCota == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("observacao", abh80.abh80defObs, false);

		if(abh80.abh8002s != null && abh80.abh8002s.size() > 0) {
			for(Abh8002 abh8002 : abh80.abh8002s) {
				ElementXml dependente = dadosTrabalhador.addNode("dependente");

				Aap09 aap09 = abh8002.abh8002parente != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aap09", abh8002.abh8002parente.aap09id) : null;
				dependente.addNode("tpDep", aap09 != null ? aap09.aap09eSocial : null, true);
				dependente.addNode("nmDep", abh8002.abh8002nome, true);
				dependente.addNode("dtNascto", ESocialUtils.formatarData(abh8002.abh8002dtNasc, ESocialUtils.PATTERN_YYYY_MM_DD), true);
				dependente.addNode("cpfDep", abh8002.abh8002cpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true) : null, false);
				if(abh80.abh80regPrev == 2 )dependente.addNode("sexoDep", abh8002.abh8002sexo == 0 ? 'M' : 'F', false);
				dependente.addNode("depIRRF", abh8002.abh8002ir == 0 ? 'N' : 'S', true);
				dependente.addNode("depSF", abh8002.abh8002sf == 0 ? 'N' : 'S', true);
				dependente.addNode("incTrab", abh8002.abh8002incapaz == 2 ? 'S' : 'N', true);
			}
		}

		ElementXml contato = dadosTrabalhador.addNode("contato");
		String fonePrinc = (abh80.abh80ddd1 != null && abh80.abh80fone1 != null) ? StringUtils.extractNumbers(abh80.abh80ddd1 + abh80.abh80fone1) : null;
		contato.addNode("fonePrinc", fonePrinc, false);
		contato.addNode("emailPrinc", abh80.abh80eMail, false);

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
		aaa15.adicionarTagsEsocial("ideTrabalhador", "dadosTrabalhador");
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