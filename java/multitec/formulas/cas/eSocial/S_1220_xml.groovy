package multitec.formulas.cas.eSocial;

import java.time.LocalDate;
import java.util.stream.Collectors

import br.com.multitec.utils.DecimalUtils
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aap09
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Abh8002
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011
import sam.model.entities.fb.Fba03
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_1220_xml extends FormulaBase{
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
		def indApuracao = get("indApuracao");
		LocalDate mesAnoRef = get("perApur");
		def indRetif = aaa15.aaa15tipo == aaa15.TIPO_RETIFICACAO ? 2 : 1;
		List<Fba0101> fba0101s = get("fba0101s");

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtInfoIR/v_S_01_01_00");
		
		ElementXml evtPgtos = eSocial.addNode("evtInfoIR");
		evtPgtos.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = eSocial.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("indApuracao", indApuracao, true);
		String perApur = indApuracao == 1 ? ESocialUtils.formatarData(mesAnoRef, ESocialUtils.PATTERN_YYYY_MM) : ESocialUtils.formatarData(mesAnoRef, ESocialUtils.PATTERN_YYYY);
		ideEvento.addNode("perApur", perApur, true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = eSocial.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml ideBenef = eSocial.addNode("ideBenef");
		ideBenef.addNode("cpfBenef", StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true), true);
		
		if(!Utils.isEmpty(abh80.abh8002s)) {
			for (abh8002 in abh80.abh8002s) {
				ElementXml ideDep = ideBenef.addNode("ideDep");
				ideDep.addNode("cpfDep", StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true), true);
				ideDep.addNode("dtNascto", abh8002.abh8002dtNasc, false);
				ideDep.addNode("nome", abh8002.abh8002nome, false);
				if (abh8002.abh8002parente != null) {
					Aap09 aap09 = getAcessoAoBanco().buscarRegistroUnicoById("Aap09", abh8002.abh8002parente.aap09id);
					ideDep.addNode("redDep", aap09.aap09eSocial, true);
					if (aap09.aap09eSocial == '99') ideDep.addNode("descrDep", aap09.aap09descr, false);
				}
			}
		}
		
		ElementXml infoIR = ideBenef.addNode("infoIR");
		infoIR.addNode("vlrDedPC", somaEventos(fba0101s, 2), false);
		infoIR.addNode("vlrDedPensao", somaEventos(fba0101s, 0), false);
		infoIR.addNode("vlrDedDepen", somaEventos(fba0101s), false);
		
		for (fba0101 in fba0101s) {
			if (!Utils.isEmpty(fba0101.fba01011s)) {
				
				//Preenchendo valor referentes a dependentes
				if (contemFinalidade(fba0101.fba01011s, 3)) {
					ElementXml dedDepen = infoIR.addNode("dedDepen");
					for (fba01011 in fba0101.fba01011s) {
						if (fba01011.fba01011eve.abh21esFinalidade == 3) {
							for (fba010111 in fba01011.fba010111s) {
								Abh8002 abh8002 = getAcessoAoBanco().buscarRegistroUnicoById("Abh8002", fba010111.fba010111dep.abh8002id);
								dedDepen.addNode("cpfDep", StringUtils.ajustString(abh8002.abh8002cpf, 11, '0', false), true);
								dedDepen.addNode("vlrDeducao", fba010111.fba010111valor, true);
							}
						}
					}
				}
				
				//Preenchendo valor referentes a pensão
				if (contemFinalidade(fba0101.fba01011s, 0)) {
					ElementXml infoComplemDed = infoIR.addNode("infoComplemDed");
					infoComplemDed.addNode("indTpDeducao", 5, true);
					for (fba01011 in fba0101.fba01011s) {
						if (fba01011.fba01011eve.abh21esFinalidade == 0) {
							for (fba010111 in fba01011.fba010111s) {
								ElementXml penAlim = infoComplemDed.addNode("penAlim");
								Abh8002 abh8002 = getAcessoAoBanco().buscarRegistroUnicoById("Abh8002", fba010111.fba010111dep.abh8002id);
								penAlim.addNode("cpfDep", StringUtils.ajustString(abh8002.abh8002cpf, 11, '0', false), true);
								penAlim.addNode("vlrPensao", fba010111.fba010111valor, true);
							}
						}
					}
				}
				
				//Preenchendo valor referentes a previdência complementar
				if (contemFinalidade(fba0101.fba01011s, 2)) {
					ElementXml infoComplemDed = infoIR.addNode("infoComplemDed");
					infoComplemDed.addNode("indTpDeducao", 2, true);
					for (fba01011 in fba0101.fba01011s) {
						if (fba01011.fba01011eve.abh21esFinalidade == 2) {
							for (fba010111 in fba01011.fba010111s) {
								ElementXml previdCompl = infoComplemDed.addNode("previdCompl");
								previdCompl.addNode("cnpjEntidPC", StringUtils.ajustString(fba01011.fba01011eve.abh21esPcCnpj, 14, '0', false), true);
								previdCompl.addNode("vlrDedPC", fba010111.fba010111valor, true);
							}
						}
					}
				}
				
				//Preenchendo valor referentes a plano de saúde
				if (contemFinalidade(fba0101.fba01011s, 1)) {
					Map<String, List<Fba01011>> fba01011sPorPlano = fba0101.fba01011s.stream().collect(Collectors.groupingBy({fba01011 -> fba01011.fba01011eve.abh21esOpsCnpj}));
					for(String cnpj in fba01011sPorPlano.keySet()) {
						for (fba01011 in fba01011sPorPlano.get(cnpj)) {
							ElementXml planSaude = infoIR.addNode("planSaude");
							planSaude.addNode("cnpjOper", StringUtils.ajustString(cnpj, 14, '0', false), true);
							planSaude.addNode("regANS", StringUtils.ajustString(fba01011.fba01011eve.abh21esOpsReg, 6, '0', false), false);
							planSaude.addNode("vlrSaude", fba01011.fba01011valor, true);
							for (fba010111 in fba01011.fba010111s) {
								Abh8002 abh8002 = getAcessoAoBanco().buscarRegistroUnicoById("Abh8002", fba010111.fba010111dep.abh8002id);
								ElementXml infoDep = planSaude.addNode("infoDep");
								infoDep.addNode("cpdDep", StringUtils.ajustString(abh8002.abh8002cpf, 11, '0', false), true);
								infoDep.addNode("vlrSaide", fba010111.fba010111valor, true);
							}
						}
					}
				}
				
				String sql = "SELECT * FROM Fba03 WHERE fba03trab = :fba03trab AND fba03dependente IS NULL " + getSamWhere().getWherePadrao("AND", Fba03.class);
				List<Fba03> fba03sTitular = getAcessoAoBanco().buscarListaDeRegistros(sql, criarParametroSql("fba03trab", abh80.abh80id));
				boolean criouInfoReembMed = false;
				ElementXml infoReembMed = null;
				if (!Utils.isEmpty(fba03sTitular)) {
					criouInfoReembMed = true;
					infoReembMed = infoIR.addNode("infoReembMed");
					Map<Integer, List<Fba03>> fba03sPelaOrigem = fba03sTitular.stream().collect(Collectors.groupingBy({fba03 -> fba03.fba03origem}));
					for (fba03origem in fba03sPelaOrigem.keySet()) {
						Map<String, List<Fba03>> fba03sPelaOrigemEPlano = fba03sPelaOrigem.get(fba03origem).stream().collect(Collectors.groupingBy({fba03 -> fba03.fba03psNi}));
						for (fba03psNi in fba03sPelaOrigemEPlano.keySet()) {
							def fba03opsReg = fba03sPelaOrigemEPlano.get(fba03psNi).get(0).fba03opsReg;
							infoReembMed.addNode("indOrgReemb", fba03origem, true);
							infoReembMed.addNode("cnpjOper", StringUtils.ajustString(fba03psNi, 14, '0', false), true);
							infoReembMed.addNode("regANS", StringUtils.ajustString(fba03opsReg, 6, '0', false), false);
							
							List<Fba03> fba03s = fba03sPelaOrigemEPlano.get(fba03psNi);
							ElementXml detReembTit = infoReembMed.addNode("detReembTit");
							detReembTit.addNode("tpInsc", fba03s.get(0).fba03psTi + 1, true);
							detReembTit.addNode("nrInsc", StringUtils.ajustString(fba03psNi, 14, '0', false), true);
							
							List<BigDecimal> valores = fba03s.stream().filter({fba03 -> fba03.fba03ano == mesAnoRef.year && fba03.fba03mes == mesAnoRef.monthValue}).map({fba03 -> fba03.fba03valor}).collect(Collectors.toList());
							def vlrReemb = DecimalUtils.create(0).sum(valores.toArray()).get();
							detReembTit.addNode("vlrReemb", ESocialUtils.formatarDecimal(vlrReemb, 2, false), false);
							
							List<BigDecimal> valoresAnt = fba03s.stream().filter({fba03 -> fba03.fba03ano != mesAnoRef.year && fba03.fba03mes != mesAnoRef.monthValue}).map({fba03 -> fba03.fba03valor}).collect(Collectors.toList());
							def vlrReembAnt = DecimalUtils.create(0).sum(valoresAnt.toArray()).get();
							detReembTit.addNode("vlrReembAnt", ESocialUtils.formatarDecimal(vlrReemb, 2, false), false);
						}
					}
				}
				
				sql = "SELECT * FROM Fba03 WHERE fba03trab = :fba03trab AND fba03dependente IS NOT NULL " + getSamWhere().getWherePadrao("AND", Fba03.class);
				List<Fba03> fba03sDep = getAcessoAoBanco().buscarListaDeRegistros(sql, criarParametroSql("fba03trab", abh80.abh80id));
				if (!Utils.isEmpty(fba03sDep)) {
					if (!criouInfoReembMed) infoReembMed = infoIR.addNode("infoReembMed");
					Map<Integer, List<Fba03>> fba03sPelaOrigem = fba03sDep.stream().collect(Collectors.groupingBy({fba03 -> fba03.fba03origem}));
					for (fba03origem in fba03sPelaOrigem.keySet()) {
						Map<String, List<Fba03>> fba03sPelaOrigemEPlano = fba03sPelaOrigem.get(fba03origem).stream().collect(Collectors.groupingBy({fba03 -> fba03.fba03psNi}));
						for (fba03psNi in fba03sPelaOrigemEPlano.keySet()) {
							def fba03opsReg = fba03sPelaOrigemEPlano.get(fba03psNi).get(0).fba03opsReg;
							infoReembMed.addNode("indOrgReemb", fba03origem, true);
							infoReembMed.addNode("cnpjOper", StringUtils.ajustString(fba03psNi, 14, '0', false), true);
							infoReembMed.addNode("regANS", StringUtils.ajustString(fba03opsReg, 6, '0', false), false);
							
							List<Fba03> fba03s = fba03sPelaOrigemEPlano.get(fba03psNi);
							ElementXml detReembTit = infoReembMed.addNode("detReembTit");
							detReembTit.addNode("tpInsc", fba03s.get(0).fba03psTi + 1, true);
							detReembTit.addNode("nrInsc", StringUtils.ajustString(fba03psNi, 14, '0', false), true);
							
							List<BigDecimal> valores = fba03s.stream().filter({fba03 -> fba03.fba03ano == mesAnoRef.year && fba03.fba03mes == mesAnoRef.monthValue}).map({fba03 -> fba03.fba03valor}).collect(Collectors.toList());
							def vlrReemb = DecimalUtils.create(0).sum(valores.toArray()).get();
							detReembTit.addNode("vlrReemb", ESocialUtils.formatarDecimal(vlrReemb, 2, false), false);
							
							List<BigDecimal> valoresAnt = fba03s.stream().filter({fba03 -> fba03.fba03ano != mesAnoRef.year && fba03.fba03mes != mesAnoRef.monthValue}).map({fba03 -> fba03.fba03valor}).collect(Collectors.toList());
							def vlrReembAnt = DecimalUtils.create(0).sum(valoresAnt.toArray()).get();
							detReembTit.addNode("vlrReembAnt", ESocialUtils.formatarDecimal(vlrReemb, 2, false), false);
						}
					}
				}
			}
		}
		
		
		/*ElementXml infoProcRet = infoIR.addNode("infoProcRet");
		infoProcRet.addNode("tpProcRet", "tpProcRet", true);
		infoProcRet.addNode("nrProcRet", "nrProcRet", true);
		infoProcRet.addNode("codSusp", "codSusp", false);
		
		ElementXml penAlim = infoProcRet.addNode("penAlim");
		penAlim.addNode("cpfDep", "cpfDep", true);
		penAlim.addNode("vlrPensaoSusp", "vlrPensaoSusp", false);
		
		ElementXml previdCompl = infoProcRet.addNode("previdCompl");
		previdCompl.addNode("cnpjEntidPC", "cnpjEntidPC", true);
		previdCompl.addNode("vlrDedPCSusp", "vlrDedPCSusp", true);
		
		ElementXml dedDepen = infoProcRet.addNode("dedDepen");
		dedDepen.addNode("cpfDep", "cpfDep", true);
		dedDepen.addNode("vlrDedSusp", "vlrDedSusp", true);*/

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
	
	private def somaEventos(List<Fba0101> fba0101s) {
		if (Utils.isEmpty(fba0101s)) return null;
		
		def soma = 0;
		for (fba0101 in fba0101s) {
			if (!Utils.isEmpty(fba0101.fba01011s)) {
				for (fba01011 in fba0101.fba01011s) {
					soma = soma + fba01011.fba01011valor;
				}
			}
		}
		
		return soma == 0 ? null : soma;
	}
	
	private def somaEventos(List<Fba0101> fba0101s, int abh21esFinalidade) {
		if (Utils.isEmpty(fba0101s)) return null;
		
		def soma = 0;
		for (fba0101 in fba0101s) {
			if (!Utils.isEmpty(fba0101.fba01011s)) {
				for (fba01011 in fba0101.fba01011s) {
					if (abh21esFinalidade == null || fba01011.fba01011eve.abh21esFinalidade == abh21esFinalidade) {
						soma = soma + fba01011.fba01011valor;
					}
				}
			}
		}
		
		return soma == 0 ? null : soma;
	}
	
	private boolean contemFinalidade(List<Fba01011> fba01011s, int abh21esFinalidade) {
		if (Utils.isEmpty(fba01011s)) return false;
		
		for (fba01011 in fba01011s) {
			if (fba01011.fba01011eve.abh21esFinalidade == abh21esFinalidade) {
				return true;
			}
		}
		
		return false;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==