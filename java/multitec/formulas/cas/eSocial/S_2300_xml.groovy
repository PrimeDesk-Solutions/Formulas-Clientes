package multitec.formulas.cas.eSocial;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aap09
import sam.model.entities.aa.Aap03
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Abh8002;
import sam.model.entities.fb.Fbb01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2300_xml extends FormulaBase {
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

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtTSVInicio/v_S_01_03_00");
		ElementXml evtTSVInicio = eSocial.addNode("evtTSVInicio");
		evtTSVInicio.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtTSVInicio.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtTSVInicio.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti + 1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml trabalhador = evtTSVInicio.addNode("trabalhador");
		trabalhador.addNode("cpfTrab", StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true), true);
		trabalhador.addNode("nmTrab", abh80.abh80nome, true);
		trabalhador.addNode("sexo", abh80.abh80sexo == 0 ? 'M' : 'F', true);
		trabalhador.addNode("racaCor", abh80.abh80rc?.aap07eSocial?: null, true);
		trabalhador.addNode("estCiv", abh80.abh80estCivil?.aap08eSocial?: null, false);
		trabalhador.addNode("grauInstr", abh80.abh80gi?.aap06eSocial?: null, true);
		trabalhador.addNode("nmSoc", abh80.abh80nomeSocial, false);

		ElementXml nascimento = trabalhador.addNode("nascimento");
		nascimento.addNode("dtNascto", ESocialUtils.formatarData(abh80.abh80nascData, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		nascimento.addNode("paisNascto", abh80.abh80nascPais?.aag01eSocial?: null, true);
		nascimento.addNode("paisNac", abh80.abh80paisOrigem?.aag01eSocial?: null, true);

		ElementXml endereco = trabalhador.addNode("endereco");
		ElementXml brasil = endereco.addNode("brasil");
		brasil.addNode("tpLograd", abh80.abh80tpLog?.aap15eSocial?: null, true);
		brasil.addNode("dscLograd", abh80.abh80endereco, true);
		brasil.addNode("nrLograd", abh80.abh80numero, true);
		brasil.addNode("complemento", abh80.abh80complem, false);
		brasil.addNode("bairro", abh80.abh80bairro, false);
		brasil.addNode("cep", abh80.abh80cep, true);
		brasil.addNode("codMunic", abh80.abh80municipio?.aag0201ibge?: null, true);

		Aag02 aag02 = abh80.abh80municipio != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aag02", abh80.abh80municipio.aag0201uf.aag02id) : null;
		brasil.addNode("uf", aag02 != null ? aag02.aag02uf : null, true);

		if(abh80.abh80extPais != null) {
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
			ElementXml trabImig = trabalhador.addNode("trabImig");
			trabImig.addNode("tmpResid", abh80.abh80tmpResid, false);
			trabImig.addNode("condIng", abh80.abh80condIng, true);
		}

		ElementXml infoDeficiencia = trabalhador.addNode("infoDeficiencia");
		infoDeficiencia.addNode("defFisica", abh80.abh80defFisico == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defVisual", abh80.abh80defVisual == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defAuditiva", abh80.abh80defAuditivo == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defMental", abh80.abh80defMental == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("defIntelectual", abh80.abh80defIntelecto == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("reabReadap", abh80.abh80defReabil == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("observacao", abh80.abh80defObs, false);

		if(abh80.abh8002s != null && abh80.abh8002s.size() > 0) {
			for(Abh8002 abh8002 : abh80.abh8002s) {
				Aap09 aap09 = abh8002.abh8002parente == null ? null : getSession().get(Aap09.class, abh8002.abh8002parente.aap09id)
				ElementXml dependente = trabalhador.addNode("dependente");
				dependente.addNode("tpDep", aap09?.aap09codigo?: null, true);
				dependente.addNode("nmDep", abh8002.abh8002nome, true);
				dependente.addNode("dtNascto", ESocialUtils.formatarData(abh8002.abh8002dtNasc, ESocialUtils.PATTERN_YYYY_MM_DD), true);
				dependente.addNode("cpfDep", abh8002.abh8002cpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true) : null, false);
				dependente.addNode("depIRRF", abh8002.abh8002ir == 0 ? 'N' : 'S', true);
				dependente.addNode("depSF", abh8002.abh8002sf == 0 ? 'N' : 'S', true);
				dependente.addNode("incTrab", abh8002.abh8002incapaz == 2 ? 'S' : 'N', true);
				if(aap09?.aap09codigo?.equals("99")) dependente.addNode("descrDep",abh8002.abh8002descrDep, false);
			}
		}

		ElementXml contato = trabalhador.addNode("contato");
		String fonePrinc = (abh80.abh80ddd1 != null && abh80.abh80fone1 != null) ? StringUtils.extractNumbers(abh80.abh80ddd1 + abh80.abh80fone1) : null;
		contato.addNode("fonePrinc", fonePrinc, false);
		contato.addNode("emailPrinc", abh80.abh80eMail, false);

		ElementXml infoTSVInicio = evtTSVInicio.addNode("infoTSVInicio");
		String codCateg = abh80.abh80categ != null ? abh80.abh80categ.aap14eSocial : null;

		LocalDate date = LocalDate.of(2018, 5, 1);
		LocalDate dataBase = abh80.abh80dtResTrans != null && aac10.aac10esDti != null && DateUtils.dateDiff(aac10.aac10esDti, abh80.abh80dtResTrans, ChronoUnit.DAYS) > 0 ? abh80.abh80dtResTrans : abh80.abh80dtAdmis;
		String cadIni = DateUtils.dateDiff(date, dataBase, ChronoUnit.DAYS) >= 0 ? "N" : "S";

		infoTSVInicio.addNode("cadIni", cadIni, true);
		infoTSVInicio.addNode("matricula", abh80.abh80codigo, true);
		infoTSVInicio.addNode("codCateg", codCateg, true);
		infoTSVInicio.addNode("dtInicio", ESocialUtils.formatarData(abh80.abh80dtAdmis, ESocialUtils.PATTERN_YYYY_MM_DD), true);

		if(codCateg != null && !codCateg.equals("721") && !codCateg.equals("722") && !codCateg.equals("901") && !codCateg.equals("305")) {
			infoTSVInicio.addNode("natAtividade", abh80.abh80natAtiv, false);
		}

		ElementXml infoComplementares = infoTSVInicio.addNode("infoComplementares");

		Aap03 aap03 = abh80.abh80cargo.abh05cbo == null ? null : getSession().get(Aap03.class, abh80.abh80cargo.abh05cbo.aap03id)
		ElementXml cargoFuncao = infoComplementares.addNode("cargoFuncao");
		cargoFuncao.addNode("nmCargo", abh80.abh80cargo?.abh05nome?: null, false);
		cargoFuncao.addNode("CBOCargo", abh80.abh80cargo == null ? null : aap03?.aap03codigo, false);
		cargoFuncao.addNode("nmFuncao", abh80.abh80cargo == null ? null : abh80.abh80cargo.abh05funcao, false);

		ElementXml remuneracao = infoComplementares.addNode("remuneracao");
		remuneracao.addNode("vrSalFx", ESocialUtils.formatarDecimal(abh80.abh80salario, 2, false), true);
		remuneracao.addNode("undSalFixo", abh80.abh80unidPagto?.aap18eSocial?: null, true);
		remuneracao.addNode("dscSalVar", abh80.abh80salVar, false);

		if(abh80.abh80fgtsData != null) {
			ElementXml FGTS = infoComplementares.addNode("FGTS");
			FGTS.addNode("dtOpcFGTS", ESocialUtils.formatarData(abh80.abh80fgtsData, ESocialUtils.PATTERN_YYYY_MM_DD), false);

		}


		if(abh80.abh80eoCateg != null) {
			ElementXml infoDirigenteSindical = infoComplementares.addNode("infoDirigenteSindical");
			infoDirigenteSindical.addNode("categOrig", abh80.abh80eoCateg, false);
			infoDirigenteSindical.addNode("tpInsc", "1", false);
			infoDirigenteSindical.addNode("nrInsc", StringUtils.extractNumbers(abh80.abh80eoCNPJ), false);
			infoDirigenteSindical.addNode("dtAdmOrig", ESocialUtils.formatarData(abh80.abh80eoAdmis, ESocialUtils.PATTERN_YYYY_MM_DD), false);
			infoDirigenteSindical.addNode("matricOrig", abh80.abh80eoMatric, false);
			infoDirigenteSindical.addNode("tpRegTrab", abh80.abh80eoRegTrab, false);
			infoDirigenteSindical.addNode("tpRegPrev", abh80.abh80eoRegPrev, false);

		}

		if(abh80.abh80tcCateg != null) {
			ElementXml infoTrabCedido = infoComplementares.addNode("infoTrabCedido");
			infoTrabCedido.addNode("categOrig", abh80.abh80tcCateg, false);
			infoTrabCedido.addNode("cnpjCednt", StringUtils.extractNumbers(abh80.abh80tcCNPJ), false);
			infoTrabCedido.addNode("matricCed", abh80.abh80tcMatric, false);
			infoTrabCedido.addNode("dtAdmCed", abh80.abh80tcAdmis, false);
			infoTrabCedido.addNode("tpRegTrab", abh80.abh80tcRegTrab, false);
			infoTrabCedido.addNode("tpRegPrev", abh80.abh80tcRegPrev, false);

		}

		if(abh80.abh80estagiario == 1) {
			ElementXml infoEstagiario = infoComplementares.addNode("infoEstagiario");
			infoEstagiario.addNode("natEstagio", abh80.abh80estNat == 0 ? "N" : "O", true);
			infoEstagiario.addNode("nivEstagio", abh80.abh80estNivel, true);
			infoEstagiario.addNode("areaAtuacao", abh80.abh80estAreaAtu, false);
			infoEstagiario.addNode("nrApol", abh80.abh80estApolSeg, false);
			infoEstagiario.addNode("dtPrevTerm", abh80.abh80estDtTerm != null ? ESocialUtils.formatarData(abh80.abh80estDtTerm, ESocialUtils.PATTERN_YYYY_MM_DD) : null, true);

			ElementXml instEnsino = infoEstagiario.addNode("instEnsino");
			instEnsino.addNode("cnpjInstEnsino", StringUtils.extractNumbers(abh80.abh80ieeCnpj), false);
			instEnsino.addNode("nmRazao", abh80.abh80ieeNome, true);
			instEnsino.addNode("dscLograd", abh80.abh80ieeEndereco, false);
			instEnsino.addNode("nrLograd", abh80.abh80ieeNumero, false);
			instEnsino.addNode("bairro", abh80.abh80ieeBairro, false);
			instEnsino.addNode("cep", abh80.abh80ieeCep, false);
			instEnsino.addNode("codMunic", abh80.abh80ieeMunicipio?.aag0201ibge?: null, false);
			instEnsino.addNode("uf", abh80.abh80ieeMunicipio?.aag0201uf?.aag02ibge?: null, false);

			if(abh80.abh80aieCnpj != null) {
				ElementXml ageIntegracao = infoEstagiario.addNode("ageIntegracao");
				ageIntegracao.addNode("cnpjAgntInteg", StringUtils.extractNumbers(abh80.abh80aieCnpj), true);
			}

			if(abh80.abh80estCpfSup != null) {
				ElementXml supervisorEstagio = infoEstagiario.addNode("supervisorEstagio");
				supervisorEstagio.addNode("cpfSupervisor", StringUtils.extractNumbers(abh80.abh80estCpfSup), true);
			}
		}

		ElementXml localTrabGeral = infoComplementares.addNode("localTrabGeral")
		localTrabGeral.addNode("tpInsc", aac10.aac10ti == 0 ? 1 : 2)
		localTrabGeral.addNode("nrInsc", StringUtils.extractNumbers(aac10.aac10ni))

		if(abh80.abh80cpfAnt != null) {
			ElementXml mudancaCPF = infoTSVInicio.addNode("mudancaCPF");
			mudancaCPF.addNode("cpfAnt", StringUtils.extractNumbers(abh80.abh80cpfAnt), false);
			mudancaCPF.addNode("matricAnt", abh80.abh80codigo, false);
			mudancaCPF.addNode("dtAltCPF", ESocialUtils.formatarData(abh80.abh80cpfDtAlt), false);
			mudancaCPF.addNode("observacao", abh80.abh80cpfObs, false);

		}

		String sql = "SELECT * FROM Fbb01 AS fbb01 LEFT JOIN FETCH fbb01.fbb01ma AS abh07 " +
				"WHERE fbb01.fbb01trab = :abh80id AND fbb01.fbb01dtRet IS NULL " +
				getSamWhere().getWherePadrao(" AND ", Fbb01.class) +
				" ORDER BY fbb01dtsai DESC";

		Fbb01 fbb01 = getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("abh80id", abh80.abh80id));
		if(fbb01 != null) {
			ElementXml afastamento = infoTSVInicio.addNode("afastamento");
			afastamento.addNode("dtIniAfast", ESocialUtils.formatarData(fbb01.fbb01dtSai, ESocialUtils.PATTERN_YYYY_MM_DD), true);
			afastamento.addNode("codMotAfast", fbb01.fbb01ma?.abh07eSocial?: null, true);
		}

		if(abh80.abh80dtResTrans != null) {
			ElementXml termino = infoTSVInicio.addNode("termino");
			termino.addNode("dtTerm", abh80.abh80dtResTrans, true);
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