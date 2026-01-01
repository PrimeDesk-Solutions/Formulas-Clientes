package multitec.formulas.cas.eSocial;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aap09
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Abh8002;
import sam.model.entities.ab.Abh8004;
import sam.model.entities.fb.Fbb01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2200_xml extends FormulaBase {
	private Aaa15 aaa15;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = (Aaa15) get("aaa15");
		Abh80 abh80 = getAcessoAoBanco().buscarRegistroUnicoById("Abh80", aaa15.aaa15registro);
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);

		def tpAmb = 1;

		def indRetif = aaa15.aaa15tipo == 3 ? 2 : 1;

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtAdmissao/v_S_01_03_00");
		ElementXml evtAdmissao = eSocial.addNode("evtAdmissao");
		evtAdmissao.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtAdmissao.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtAdmissao.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);

		ElementXml trabalhador = evtAdmissao.addNode("trabalhador");
		trabalhador.addNode("cpfTrab", abh80.abh80cpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true) : null, true);
		trabalhador.addNode("nmTrab", abh80.abh80nome, true);
		trabalhador.addNode("sexo", abh80.abh80sexo == 0 ? 'M' : 'F', true);
		trabalhador.addNode("racaCor", abh80.abh80rc?.aap07eSocial?: null, true);
		trabalhador.addNode("estCiv", abh80.abh80estCivil?.aap08eSocial?: null, false);
		trabalhador.addNode("grauInstr", abh80.abh80gi?.aap06eSocial?: null, true);
		trabalhador.addNode("nmSoc", abh80.abh80nomeSocial, false);

		ElementXml nascimento = trabalhador.addNode("nascimento");
		nascimento.addNode("dtNascto", ESocialUtils.formatarData(abh80.abh80nascData, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		nascimento.addNode("paisNascto", abh80.abh80nascPais?.aag01eSocial?: null, true);
		nascimento.addNode("paisNac", abh80.abh80nascPais?.aag01eSocial?: null, true);

		ElementXml endereco = trabalhador.addNode("endereco");
		ElementXml brasil = endereco.addNode("brasil");
		brasil.addNode("tpLograd", abh80.abh80tpLog?.aap15eSocial?: null, true);
		brasil.addNode("dscLograd", abh80.abh80endereco, true);
		brasil.addNode("nrLograd", abh80.abh80numero, true);
		brasil.addNode("complemento", abh80.abh80complem, false);
		brasil.addNode("bairro", abh80.abh80bairro, false);
		brasil.addNode("cep", abh80.abh80cep, true);
		brasil.addNode("codMunic",  abh80.abh80municipio != null ? abh80.abh80municipio.aag0201ibge : null, true);

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
		infoDeficiencia.addNode("infoCota", abh80.abh80defCota == 0 ? 'N' : 'S', true);
		infoDeficiencia.addNode("observacao", abh80.abh80defObs, false);
		def teste = abh80.abh8002s
		if(abh80.abh8002s != null && abh80.abh8002s.size() > 0) {
			for(Abh8002 abh8002 : abh80.abh8002s) {
				Aap09 aap09 = abh8002.abh8002parente != null ? getSession().createCriteria(Aap09.class).addWhere(Criterions.eq("aap09id", abh8002.abh8002parente.aap09id)).get(ColumnType.ENTITY) : null
				ElementXml dependente = trabalhador.addNode("dependente");
				dependente.addNode("tpDep", abh8002.abh8002parente != null ? aap09.aap09codigo : null, true);
				dependente.addNode("nmDep", abh8002.abh8002nome, true);
				dependente.addNode("dtNascto", ESocialUtils.formatarData(abh8002.abh8002dtNasc, ESocialUtils.PATTERN_YYYY_MM_DD), true);
				dependente.addNode("cpfDep", abh8002.abh8002cpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true) : null, false);
				//dependente.addNode("sexoDep", abh8002.abh8002sexo == 0 ? 'M' : 'F', false);
				dependente.addNode("depIRRF", abh8002.abh8002ir == 0 ? 'N' : 'S', true);
				dependente.addNode("depSF", abh8002.abh8002sf == 0 ? 'N' : 'S', true);
				dependente.addNode("incTrab", abh8002.abh8002incapaz == 2 ? 'S' : 'N', true);
				if(aap09.aap09codigo.equals("99")) dependente.addNode("descrDep",abh8002.abh8002descrDep, false);
			}
		}

		ElementXml contato = trabalhador.addNode("contato");
		String fonePrinc = (abh80.abh80ddd1 != null && abh80.abh80fone1 != null) ? StringUtils.extractNumbers(abh80.abh80ddd1 + abh80.abh80fone1) : null;
		contato.addNode("fonePrinc", fonePrinc, false);
		contato.addNode("emailPrinc", abh80.abh80eMail, false);

		ElementXml vinculo = evtAdmissao.addNode("vinculo");
		vinculo.addNode("matricula", abh80.abh80codigo, true);
		vinculo.addNode("tpRegTrab", "1", true);
		vinculo.addNode("tpRegPrev", abh80.abh80regPrev, true);

		LocalDate date = LocalDate.of(2018, 5, 1);
		LocalDate dataBase = abh80.abh80dtResTrans != null && aac10.aac10esDti != null && DateUtils.dateDiff(aac10.aac10esDti, abh80.abh80dtResTrans, ChronoUnit.DAYS) > 0 ? abh80.abh80dtResTrans : abh80.abh80dtAdmis;
		String cadIni = DateUtils.dateDiff(date, dataBase, ChronoUnit.DAYS) >= 0 ? "N" : "S";
		vinculo.addNode("cadIni", cadIni, true);

		ElementXml infoRegimeTrab = vinculo.addNode("infoRegimeTrab");
		ElementXml infoCeletista = infoRegimeTrab.addNode("infoCeletista");
		infoCeletista.addNode("dtAdm", ESocialUtils.formatarData(abh80.abh80dtAdmis, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		infoCeletista.addNode("tpAdmissao", abh80.abh80tpAdmis?.aap16eSocial?: null, true);
		infoCeletista.addNode("indAdmissao", abh80.abh80indAdmis, true);
		infoCeletista.addNode("tpRegJor", abh80.abh80regJor, true);
		infoCeletista.addNode("natAtividade", abh80.abh80natAtiv, true);
		infoCeletista.addNode("dtBase",abh80.abh80mesDtBase, false);
		infoCeletista.addNode("cnpjSindCategProf", abh80.abh80sindSindical != null ? StringUtils.extractNumbers(abh80.abh80sindSindical.abh03cnpj) : null, true);

		/*if(abh80.abh80fgtsData != null) {
			ElementXml FGTS = infoCeletista.addNode("FGTS");
			FGTS.addNode("dtOpcFGTS", ESocialUtils.formatarData(abh80.abh80fgtsData, ESocialUtils.PATTERN_YYYY_MM_DD), false);
		}*/
		if(abh80.abh80ttContr != null && abh80.abh80ttContr != 0) {
			ElementXml trabTemporario = infoCeletista.addNode("trabTemporario");
			trabTemporario.addNode("hipLeg", abh80.abh80ttContr, true);
			trabTemporario.addNode("justContr", abh80.abh80ttJust, true);

			ElementXml ideEstabVinc = trabTemporario.addNode("ideEstabVinc");
			ideEstabVinc.addNode("tpInsc", aac10.aac10ti+1, true);

			String niTS = StringUtils.extractNumbers(aac10.aac10ni);
			if(aac10.aac10ti == 0) {
				niTS = StringUtils.ajustString(niTS, 14, '0', false).substring(0, 8);
			}else {
				niTS = StringUtils.ajustString(niTS, 11, '0', true);
			}
			ideEstabVinc.addNode("nrInsc", niTS, true);

			ElementXml ideTrabSubstituido = trabTemporario.addNode("ideTrabSubstituido");
			ideTrabSubstituido.addNode("cpfTrabSubst", StringUtils.extractNumbers(abh80.abh80ttTrabSub), true);
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
		def aap03 = getAcessoAoBanco().buscarUnicoTableMap("select * from aap03 where aap03id = :id ", criarParametroSql("id", abh80.abh80cargo.abh05cbo.aap03id))
		infoContrato.addNode("CBOCargo", abh80.abh80cargo == null ? null : abh80.abh80cargo.abh05cbo == null ? null : aap03.getString("aap03codigo"), false);
		//infoContrato.addNode("nmFuncao", abh80.abh80cargo == null ? null : abh80.abh80cargo.abh05funcao, false);
		infoContrato.addNode("codCateg", abh80.abh80categ?.aap14eSocial?: null, true);

		ElementXml remuneracao = infoContrato.addNode("remuneracao");
		remuneracao.addNode("vrSalFx", ESocialUtils.formatarDecimal(abh80.abh80salario, 2, false), true);
		remuneracao.addNode("undSalFixo", abh80.abh80unidPagto?.aap18eSocial?: null, true);
		remuneracao.addNode("dscSalVar", abh80.abh80salVar, false);

		ElementXml duracao = infoContrato.addNode("duracao");
		String tpContr = String.valueOf(abh80.abh80prazoConTrab);

		if(tpContr != null && !tpContr.equals("1")) tpContr = "2";
		duracao.addNode("tpContr", tpContr, true);

		if(tpContr != null && tpContr.equals("2")) {
			duracao.addNode("dtTerm", ESocialUtils.formatarData(abh80.abh80dataTerm, ESocialUtils.PATTERN_YYYY_MM_DD), false);
			duracao.addNode("clauAssec", abh80.abh80clauAsseg == 0 ? "N" : "S", false);
		}

		if(tpContr != null && tpContr.equals("3")) {
			duracao.addNode("objDet", abh80.abh80justTpContr, true);
		}

		ElementXml localTrabalho = infoContrato.addNode("localTrabalho");
		ElementXml localTrabGeral = localTrabalho.addNode("localTrabGeral")
		localTrabGeral.addNode("tpInsc", aac10.aac10ti == 0 ? 1 : 2)
		localTrabGeral.addNode("nrInsc", StringUtils.extractNumbers(aac10.aac10ni))

		ElementXml horContratual = infoContrato.addNode("horContratual");
		horContratual.addNode("qtdHrsSem", abh80.abh80hs, true);
		horContratual.addNode("tpJornada", abh80.abh80jorTipo, true);
		horContratual.addNode("tmpParc", abh80.abh80regParc, true);
		horContratual.addNode("horNoturno", abh80.abh80jorNot == 0 ? "N" : "S", true);
		horContratual.addNode("dscJorn", abh80.abh80jorDescr, true);

		if(abh80.abh8004s != null && abh80.abh8004s.size() > 0) {
			for (Abh8004 abh8004 : abh80.abh8004s) {
				if (abh8004.abh8004ficha == 1) {
					ElementXml observacoes = infoContrato.addNode("observacoes");
					observacoes.addNode("observacao", abh8004.abh8004nota, true);
				}
			}
		}

		if(abh80.abh80eaNi != null) {
			Integer abh80eaTi = abh80.abh80eaTi != null ? abh80.abh80eaTi+1 + 1 : null
			ElementXml sucessaoVinc = vinculo.addNode("sucessaoVinc");
			sucessaoVinc.addNode("tpInsc",abh80eaTi, true);
			String cnpjEmpregAnt = StringUtils.extractNumbers(abh80.abh80eaNi);
			sucessaoVinc.addNode("nrInsc", StringUtils.ajustString(cnpjEmpregAnt, 14, '0', true), true);
			sucessaoVinc.addNode("matricAnt", abh80.abh80eaMatric, false);
			sucessaoVinc.addNode("dtTransf", ESocialUtils.formatarData(abh80.abh80eaTransf, ESocialUtils.PATTERN_YYYY_MM_DD), true);
			sucessaoVinc.addNode("observacao", abh80.abh80eaObs, false);
		}


		String sql = "SELECT * FROM Fbb01 AS fbb01 LEFT JOIN FETCH fbb01.fbb01ma AS abh07 " +
				"WHERE fbb01.fbb01trab = :abh80id AND fbb01.fbb01dtRet IS NULL " +
				getSamWhere().getWherePadrao(" AND ", Fbb01.class) +
				" ORDER BY fbb01dtsai DESC";

		Fbb01 fbb01 = getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("abh80id", abh80.abh80id));
		if(fbb01 != null) {
			ElementXml afastamento = vinculo.addNode("afastamento");
			afastamento.addNode("dtIniAfast", ESocialUtils.formatarData(fbb01.fbb01dtSai, ESocialUtils.PATTERN_YYYY_MM_DD), true);
			afastamento.addNode("codMotAfast", fbb01.fbb01ma?.abh07eSocial?: null, true);
		}

		if(abh80.abh80dtResTrans != null) {
			ElementXml desligamento = vinculo.addNode("desligamento");
			desligamento.addNode("dtDeslig", abh80.abh80dtResTrans, true);
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