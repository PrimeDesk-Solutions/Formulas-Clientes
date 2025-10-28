package multitec.formulas.cas.eSocial;

import java.time.LocalDate;
import java.time.Month
import java.time.format.DateTimeFormatter
import com.lowagie.text.pdf.ColumnText

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aaj50
import sam.model.entities.aa.Aap09
import sam.model.entities.ab.Abb40
import sam.model.entities.ab.Abb4001
import sam.model.entities.ab.Abh21
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Abh8002
import sam.model.entities.ab.Abh80021
import sam.model.entities.dc.Dcb01
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.SFPUtils
import sam.server.sfp.service.SFPService

public class S_1210_xml extends FormulaBase{
	private Aaa15 aaa15;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		Integer tpAmb = get("tpAmb");
		Long abh80id = get("abh80id");
		LocalDate mesAnoRef = get("perApur");
		List<Fba0101> fba0101sOriginal = get("fba0101s");
		
		Integer indRetif = aaa15.aaa15tipo == aaa15.TIPO_RETIFICACAO ? 2 : 1;
		Aac10 aac10 = getSession().get(Aac10.class, "aac10id, aac10ti, aac10ni", aaa15.getAaa15eg().getAac10id());
		Abh80 abh80 = getSession().get(Abh80.class, "abh80id, abh80tipo, abh80cpf, abh80dtMolestia, abh80json", abh80id);
		
		List<Fba0101> fba0101s = somaFba0101liqPagoPorDataPgto(fba0101sOriginal);
		
		String versaoESocial = "http://www.esocial.gov.br/schema/evt/evtPgtos/v_S_01_03_00";
		String perApur = ESocialUtils.formatarData(mesAnoRef, ESocialUtils.PATTERN_YYYY_MM);
		
		Integer aac10ti = aac10.aac10ti_Zero + 1;
		String aac10ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti_Zero == 0) aac10ni = StringUtils.ajustString(aac10ni, 14, '0', false).substring(0, 8);
		if(aac10.aac10ti_Zero == 1) aac10ni = StringUtils.ajustString(aac10ni, 11, '0', true);
		
		String abh80cpf = StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true);
		String abh80dtMolestia = ESocialUtils.formatarData(abh80.abh80dtMolestia, ESocialUtils.PATTERN_YYYY_MM_DD);
		
		List<Abh8002> abh8002s = buscarDependentesDoTrabalhador(abh80id)
		
		List<String> tiposCR = new ArrayList();
		tiposCR.add(abh80.abh80tipo == 0 || abh80.abh80tipo == 2 ? "056107" :  "058806");
		String codigoCRParaPLR = "356201";
		Boolean temPLR = false;
		

		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial(versaoESocial);
		
		ElementXml evtPgtos = eSocial.addNode("evtPgtos");
		evtPgtos.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtPgtos.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("perApur", perApur, true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtPgtos.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10ti, true);
		ideEmpregador.addNode("nrInsc", aac10ni, true);
		
		ElementXml ideBenef = evtPgtos.addNode("ideBenef");
		ideBenef.addNode("cpfBenef", abh80cpf, true);
		
		Map<String, TableMap> map = new HashMap<String, TableMap>()
		
		for(fba0101 in fba0101s) {
			if(fba0101.fba0101tpVlr_Zero == 6) temPLR = true;
				
			String ideDmDev = StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto);
			if(fba0101.fba0101tpVlr == 4 ) ideDmDev = fba0101.fba0101tpVlr + fba0101.fba0101dtCalc.format(DateTimeFormatter.ofPattern("ddMMyyyy"));
			
			LocalDate dataPerRef = ideDmDev == "31" ? fba0101.fba0101dtPgto : fba0101.fba0101dtCalc;
			String fba0101dtPgto = ESocialUtils.formatarData(fba0101.fba0101dtPgto, ESocialUtils.PATTERN_YYYY_MM_DD);
			String fba0101liqPago = ESocialUtils.formatarDecimal(fba0101.fba0101liqPago, 2, false);
			String perRef = ideDmDev.equals("21") && dataPerRef.getMonth() == Month.DECEMBER ?
				ESocialUtils.formatarData(dataPerRef, ESocialUtils.PATTERN_YYYY) :
				ESocialUtils.formatarData(dataPerRef, ESocialUtils.PATTERN_YYYY_MM);
			
			if(!map.containsKey(ideDmDev)) {
				TableMap tm = new TableMap()
				tm.put("ideDmDev", ideDmDev)
				tm.put("dtPgto", fba0101dtPgto)
				tm.put("tpPgto", fba0101.fba0101tpPgto)
				tm.put("perRef", perRef)
				tm.put("ideDmDev", ideDmDev)
				tm.put("vrLiq", fba0101.fba0101liqPago)
				map.put(ideDmDev, tm)
			}else {
				TableMap tm = map.get(ideDmDev)
				BigDecimal vlrLiqSomado  = tm.getBigDecimal_Zero("vrLiq") + fba0101.fba0101liqPago
				tm.put("vrLiq", vlrLiqSomado)
				tm.put("dtPgto", fba0101dtPgto)
				map.put(ideDmDev, tm)
			}
				
			
		}
		
		Set<String> keys = map.keySet();
		List<TableMap> tms = new ArrayList<TableMap>(map.values())
		Collections.sort(tms, new Comparator<TableMap>() {
			@Override
			public int compare(TableMap o1, TableMap o2) {
				return o1.getString("dtPgto").compareTo(o2.getString("dtPgto"));
			}
		});
		
		for(tm in tms) {
			String fba0101liqPago = ESocialUtils.formatarDecimal(tm.getBigDecimal_Zero("vrLiq"), 2, false);

			ElementXml infoPgto = ideBenef.addNode("infoPgto");
			infoPgto.addNode("dtPgto", tm.getString("dtPgto"), true);
			infoPgto.addNode("tpPgto", tm.getString("tpPgto"), true);
			infoPgto.addNode("perRef", tm.getString("perRef"), false);
			infoPgto.addNode("ideDmDev", tm.getString("ideDmDev"), true);
			infoPgto.addNode("vrLiq", fba0101liqPago, true);
		}

		ElementXml infoIRComplem = ideBenef.addNode("infoIRComplem")
		infoIRComplem.addNode("dtLaudo", abh80dtMolestia, false)
		
		if(!temS2300OuS2200Aprovado(abh80id)) {
			for(abh8002 in abh8002s) {
				Aap09 aap09 = getSession().get(Aap09.class, "aap09id, aap09codigo, aap09descr", abh8002.getAbh8002parente().getAap09id());
				String abh8002cpf = StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true);
				String abh8002dtNasc = ESocialUtils.formatarData(abh8002.abh8002dtNasc, ESocialUtils.PATTERN_YYYY_MM_DD);
				
				
					ElementXml infoDep = infoIRComplem.addNode("infoDep");
					infoDep.addNode("cpfDep", abh8002cpf, true);
					infoDep.addNode("dtNascto", abh8002dtNasc, false);
					infoDep.addNode("nome", abh8002.abh8002nome, false);
					if(abh8002.abh8002ir_Zero == 1) {
						infoDep.addNode("depIRRF", "S", true);
						infoDep.addNode("tpDep",aap09.aap09codigo, true);
					}
					if(aap09.aap09codigo.equals("99")) infoDep.addNode("descrDep",aap09.aap09descr,true);
			}
		}
		if(temPLR) tiposCR.add(codigoCRParaPLR);
		for(tpCR in tiposCR) {
			ElementXml infoIRCR = infoIRComplem.addNode("infoIRCR")
			infoIRCR.addNode("tpCR", tpCR, true)
			
			if(!tpCR.equals(codigoCRParaPLR)) {
				HashMap<String, String> dedDepens = new HashMap();
				for(fba0101 in fba0101s) {
					Integer tpRend = fba0101.fba0101tpVlr == 3 ? 13 : fba0101.fba0101tpVlr == 2 ? 12 : 11;
					BigDecimal valorDedDep = buscarValorDeducaoDependente("001");
					
					for(abh8002 in abh8002s) {
						String abh8002cpf = StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true);
						if(abh8002.abh8002ir_Zero == 1) {
							String chave = tpRend.toString() + "|" + abh8002cpf
							if(!dedDepens.containsKey(chave)) {
								dedDepens.put(chave, chave)
								ElementXml dedDepen = infoIRCR.addNode("dedDepen");
								dedDepen.addNode("tpRend",tpRend, true);
								dedDepen.addNode("cpfDep", abh8002cpf, true);
								dedDepen.addNode("vlrDedDep", valorDedDep, true);
							}
						}
					}
				}
			}
			
			
			for(fba0101 in fba0101s) {
				Integer tpRend = fba0101.fba0101tpVlr == 3 ? 13 : fba0101.fba0101tpVlr == 2 ? 12 : fba0101.fba0101tpVlr == 6 ? 14 : 11;
				Integer tipoCalculo = fba0101.fba0101tpVlr == 0 ? 1 : fba0101.fba0101tpVlr == 3 ? 2 : fba0101.fba0101tpVlr == 2 ? 3 : fba0101.fba0101tpVlr == 6 ? 4 : null
				
				for(abh8002 in abh8002s) {
					if(abh8002.abh8002pa == 1) {
						List<Abh80021> abh80021s = buscarEventosDependente(abh8002.getAbh8002id());
						List<Fba01011> eventosPa = new ArrayList<Abh80021>();
						
						String abh8002cpf = StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true);
						Fba01011 fba01011Pa = null;
						List<Fba01011> fba01011s = buscarEventosValores(fba0101.getFba0101id());
						
						for(abh80021 in abh80021s) {
							if(abh80021.abh80021tipoCalc != 0 ) {
								if(tipoCalculo != null && abh80021.abh80021tipoCalc == tipoCalculo) {
									Abh21 abh21Dep = getSession().get(Abh21.class, "abh21id, abh21codigo", abh80021.abh80021eve.abh21id)
									String codigoEvento = abh21Dep.abh21codigo
									for(fba01011 in fba01011s) {
										Abh21 abh21 = getSession().get(Abh21.class, "abh21id, abh21codigo", fba01011.fba01011eve.abh21id)
										if(abh21.abh21codigo.equals(codigoEvento)) {
											eventosPa.add(fba01011);
										}
									}
								}
							}
						}
						for(evento in eventosPa) {
							Abh21 abh21 = session.get(Abh21.class, "abh21id, abh21esir", evento.getFba01011eve().abh21id);
							if(List.of(51,52,53,54).contains(abh21.getAbh21esIr_Zero()) && evento.fba01011valor > 0){
								ElementXml penAlim  = infoIRCR.addNode("penAlim");
								penAlim.addNode("tpRend", tpRend, true);
								penAlim.addNode("cpfDep", abh8002cpf, true);
								penAlim.addNode("vlrDedPenAlim", evento.fba01011valor, true);
							}
						}
					}
				}
			}
		}
		
		HashMap<String, TableMap> hashPlanSaude = new HashMap()
		List<Abh21> abh21sDep = buscarEventosDependenteTrabalhador(abh80id);
		for(fba0101 in fba0101s) {
			List<Fba01011> fba01011s = buscarEventosValores(fba0101.getFba0101id());

			for(fba01011 in fba01011s) {
				Abh21 abh21 = getSession().get(Abh21.class, "abh21id, abh21codigo, abh21esOpsCnpj, abh21esOpsReg, abh21esReemb", fba01011.getFba01011eve().getAbh21id());

				if(abh21.abh21esOpsCnpj != null && abh21.abh21esReemb == 0) {
					String abh21esOpsCnpj = StringUtils.ajustString(StringUtils.extractNumbers(abh21.abh21esOpsCnpj), 14, '0', true);
					TableMap tm = new TableMap()
					tm.put("abh21", abh21)
					tm.put("valor", fba01011.fba01011valor)
					if(!hashPlanSaude.containsKey(abh21esOpsCnpj)) {
						hashPlanSaude.put(abh21esOpsCnpj, tm)
					}
				}
			}
		}

		for(plan in hashPlanSaude) {
			TableMap tm = plan.getValue()
			Abh21 abh21 = tm.get("abh21")
			BigDecimal valor = tm.get("valor")
			String abh21esOpsCnpj = StringUtils.ajustString(StringUtils.extractNumbers(abh21.abh21esOpsCnpj), 14, '0', true);

			Boolean eventoEstaNosDependentes = abh21sDep.contains(abh21.abh21codigo)


			valor = eventoEstaNosDependentes ? 0 : valor
			Boolean temAbh21Dep = false
			for(abh8002 in abh8002s) {
				if(abh8002.abh8002ps_Zero == 1 ) {
					List<Abh80021> abh80021s = buscarEventosDependente(abh8002.getAbh8002id());
					for(abh80021 in abh80021s) {
						Abh21 abh21Dependente = getSession().get(Abh21.class, "abh21id, abh21esOpsCnpj", abh80021.abh80021eve.abh21id);
						if(abh21Dependente.abh21esOpsCnpj.equals(abh21.abh21esOpsCnpj)) {
							temAbh21Dep = true
						}
					}
				}
			}

			if(temAbh21Dep || valor != 0) {
				ElementXml planSaude = infoIRComplem.addNode("planSaude");
				planSaude.addNode("cnpjOper", abh21esOpsCnpj, true);
				planSaude.addNode("regANS",abh21.abh21esOpsReg,true);
				planSaude.addNode("vlrSaudeTit", valor, true);

				for(abh8002 in abh8002s) {
					if(abh8002.abh8002ps_Zero == 1 ) {
						List<Abh80021> abh80021s = buscarEventosDependente(abh8002.getAbh8002id());
						String abh8002cpf = StringUtils.ajustString(StringUtils.extractNumbers(abh8002.abh8002cpf), 11, '0', true);

						for(abh80021 in abh80021s) {
							Abh21 abh21Dependente = getSession().get(Abh21.class, "abh21id, abh21esOpsCnpj", abh80021.abh80021eve.abh21id);
							if(abh21Dependente.abh21esOpsCnpj.equals(abh21.abh21esOpsCnpj)) {
								ElementXml infoDepSau = planSaude.addNode("infoDepSau");
								infoDepSau.addNode("cpfDep", abh8002cpf,true);
								infoDepSau.addNode("vlrSaudeDep",abh80021.abh80021vlr,true);
							}
						}
					}
				}
			}
		}
		
		
		
		
		List<TableMap> fba01011sReembolsos = buscarEventosReembolso(fba0101s);
		
		for(fba01011Reembolso in fba01011sReembolsos) {
			Fba01011 fba01011 = fba01011Reembolso.get("fba0101")
			List<TableMap> eventos = fba01011Reembolso.get("eventos")
			ElementXml infoReembMed  = infoIRComplem.addNode("infoReembMed");
			infoReembMed.addNode("indOrgReemb", fba01011.fba01011eve.abh21esReemb, true)
			if(fba01011.fba01011eve.abh21esReemb == 1) infoReembMed.addNode("cnpjOper ", StringUtils.ajustString(StringUtils.extractNumbers(fba01011.fba01011eve.abh21esOpsCnpj), 14, '0', false))
			infoReembMed.addNode("regANS", fba01011.fba01011eve.abh21esOpsReg)
			Abh21 abh21 = getSession().get(Abh21.class, "abh21id, abh21codigo, abh21esOpsCnpj", fba01011.getFba01011eve().getAbh21id());
			
			if(abh21 != null) {
				String abh21esOpsCnpj = StringUtils.extractNumbers(abh21.abh21esOpsCnpj);
				Integer tipoInscricao = abh21esOpsCnpj.length() == 14 ? 1 : 2;
				if(tipoInscricao == 1) abh21esOpsCnpj = StringUtils.ajustString(StringUtils.extractNumbers(abh21esOpsCnpj), 14, '0', true);
				if(tipoInscricao == 2) abh21esOpsCnpj = StringUtils.ajustString(StringUtils.extractNumbers(abh21esOpsCnpj), 11, '0', true);
				List<String> abh21codigos = buscarEventosDependenteTrabalhador(abh80id);
				BigDecimal fba01011valor = 0;
				boolean eventoEstaNosDependentes = true
				for(evento in eventos) {
					Abh21 event = evento.get("abh21")
					if(!abh21codigos.contains(event.abh21codigo)) {
						eventoEstaNosDependentes = false
						fba01011valor = evento.getBigDecimal_Zero("fba01011valor")
					}
				}
				
				
				ElementXml detReembTit = infoReembMed.addNode("detReembTit");
				detReembTit.addNode("tpInsc", tipoInscricao);
				detReembTit.addNode("nrInsc", abh21esOpsCnpj);
				detReembTit.addNode("vlrReemb", eventoEstaNosDependentes ? 0 : fba01011valor);
			}
			
			List<Abh21> abh21s = new ArrayList();
			for(evento in eventos) {
				abh21s.add(evento.get("abh21"))
			}
			
			List<Abh8002> abh8002sDoEvento = buscarDependentesPorEvento(abh21s)
			for(abh8002Evento in abh8002sDoEvento) {
				String abh8002cpf = StringUtils.ajustString(StringUtils.extractNumbers(abh8002Evento.abh8002cpf), 11, '0', false)
				ElementXml infoReembDep = infoReembMed.addNode("infoReembDep")
				infoReembDep.addNode("cpfBenef", abh8002cpf)
				
				Abh21 abh21Reembolso = getSession().get(Abh21.class, fba01011.fba01011eve.abh21id);
				
				String abh21esOpsCnpjReembolso = StringUtils.extractNumbers(abh21Reembolso.abh21esOpsCnpj);
				Integer tipoInscricao = abh21esOpsCnpjReembolso.length() == 14 ? 1 : 2;
				if(tipoInscricao == 1) abh21esOpsCnpjReembolso = StringUtils.ajustString(StringUtils.extractNumbers(abh21esOpsCnpjReembolso), 14, '0', true);
				if(tipoInscricao == 2) abh21esOpsCnpjReembolso = StringUtils.ajustString(StringUtils.extractNumbers(abh21esOpsCnpjReembolso), 11, '0', true);
				
				ElementXml detReembDep = infoReembDep .addNode("detReembDep");
				detReembDep.addNode("tpInsc", tipoInscricao);
				detReembDep.addNode("nrInsc", abh21esOpsCnpjReembolso);
				detReembDep.addNode("vlrReemb", fba01011.fba01011valor);
			}
			
			
		}
		
		
		
		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
	
	private List<String> buscarEventosDependenteTrabalhador(Long abh80id){
		List<String> abh21codigos = getSession().createCriteria(Abh21.class)
			.addFields("abh21codigo")
			.addJoin(Joins.join("abh80021", "abh80021eve = abh21id"))
			.addJoin(Joins.join("abh8002", "abh8002id = abh80021dep"))
			.addWhere(Criterions.eq("abh8002trab", abh80id))
			.getList(ColumnType.STRING);
		
		if(abh21codigos == null) return new ArrayList<String>();
		return abh21codigos;
	}
	
	private List<Abh8002> buscarDependentesPorEvento(List<Abh21> abh21s){
		List<Long> abh21ids = new ArrayList();
		for(abh21 in abh21s) {
			abh21ids.add(abh21.abh21id)
		}
		List<Abh8002> abh8002s = getSession().createCriteria(Abh8002.class)
		.addJoin(Joins.join("abh80021", "abh80021dep = abh8002id"))
		.addWhere(Criterions.in("abh80021eve", abh21ids))
		.addWhere(Criterions.eq("abh8002ps", Abh8002.SIM))
		.addWhere(getSamWhere().getCritPadrao(Abh8002.class))
		.getList(ColumnType.ENTITY)
		
		if(abh8002s == null) return new ArrayList();
		return abh8002s;
	}
	
	
	private Abh21 buscarEventoPlanoSaudeTitular(List<Fba0101> fba0101s) {
		for(fba0101 in fba0101s){
			List<Fba01011> fba01011s = buscarEventosValores(fba0101.getFba0101id());
			for(fba01011 in fba01011s) {
				Abh21 abh21 = getSession().get(Abh21.class, "abh21id, abh21esOpsCnpj, abh21esOpsReg, abh21esReemb", fba01011.getFba01011eve().getAbh21id());
				if(abh21.abh21esOpsCnpj != null && abh21.abh21esReemb == 0) {
					return abh21;
				}
			}
		}
		
		return null;
	}
	
	private List<TableMap> buscarEventosReembolso(List<Fba0101> fba0101s){
		List<Long> fba0101ids = new ArrayList();
		for(fba0101 in fba0101s) {
			fba0101ids.add(fba0101.fba0101id);
		}
		List<Fba01011> fba01011s = getSession().createCriteria(Fba01011.class)
		.addJoin(Joins.join("fba0101", "fba01011vlr = fba0101id").left(false))
		.addJoin(Joins.fetch("fba01011eve").alias("fba01011eve").left(false))
		.addWhere(Criterions.in("fba0101id", fba0101ids))
		.addWhere(Criterions.not(Criterions.eq("abh21esReemb", Abh21.ESREEMB_NAO_E_REEMBOLSO)))
		.getList(ColumnType.ENTITY);
		
		List<TableMap> fba0101reembolsos = new ArrayList();
		HashMap<String, TableMap> hashFba01011s = new HashMap<String, Fba01011>()
		
		for(fba01011 in fba01011s) {
			if(!hashFba01011s.containsKey(fba01011.fba01011eve.abh21esOpsCnpj)) {
				TableMap tm = new TableMap();
				TableMap tmEventos = new TableMap();
				tmEventos.put("fba01011valor", fba01011.fba01011valor)
				tmEventos.put("abh21", fba01011.fba01011eve)
				tm.put("fba0101", fba01011);
				tm.put("eventos",[tmEventos] );
				hashFba01011s.put(fba01011.fba01011eve.abh21esOpsCnpj, tm);
			}else {
				TableMap tm = hashFba01011s.get(fba01011.fba01011eve.abh21esOpsCnpj)
				TableMap tmEventos = new TableMap();
				tmEventos.put("fba01011valor", fba01011.fba01011valor)
				tmEventos.put("abh21", fba01011.fba01011eve)
				
				List<Abh21> eventos = tm.get("eventos")
				eventos.add(tmEventos)
				tm.put("eventos",eventos);
				
				hashFba01011s.put(fba01011.fba01011eve.abh21esOpsCnpj, tm);
			}
		}
		
		for(hash in hashFba01011s) {
			fba0101reembolsos.add(hash.getValue())
		}
		
		return fba0101reembolsos;
	}
	
	private List<Abh80021> buscarEventosDependente(Long abh8002id){
		List<Abh80021> abh80021s = getSession().createCriteria(Abh80021.class)
		.addFields("abh80021id, abh80021eve, abh80021vlr, abh80021tipoCalc")
		.addWhere(Criterions.eq("abh80021dep", abh8002id))
		.getList(ColumnType.ENTITY);
		
		if(abh80021s == null) return new ArrayList<Abh80021>();
		return abh80021s;
	}

	private List<Fba01011> buscarEventosValores(Long fba0101id) {
		List<Fba01011> fba01011s = getSession().createCriteria(Fba01011.class)
				.addFields("fba01011id, fba01011eve, fba01011vlr, fba01011valor")
				.addWhere(Criterions.eq("fba01011vlr", fba0101id))
				.getList(ColumnType.ENTITY)

		if(fba01011s == null) return new ArrayList<Fba01011>();
		return fba01011s;
	}
	
	private List<Abh8002> buscarDependentesDoTrabalhador(Long abh80id) {
		List<Abh8002> abh8002s = getSession().createCriteria(Abh8002.class)
			.addFields("abh8002id, abh8002cpf, abh8002dtNasc, abh8002nome, abh8002parente, abh8002ir, abh8002ps, abh8002pa")
			.addWhere(Criterions.eq("abh8002trab", abh80id))
			.getList(ColumnType.ENTITY);
		
		if(abh8002s == null) return new ArrayList<Abh8002>();
		return abh8002s;
	}
	
	private List<Fba0101> somaFba0101liqPagoPorDataPgto(List<Fba0101> fba0101sOriginal) {
		if(fba0101sOriginal == null || fba0101sOriginal.size() <= 0) return new ArrayList<Fba0101>();
		
		HashMap<String, Fba0101> hashFba0101s = new HashMap<String, Fba0101>()
		for(fba0101 in fba0101sOriginal) {
			String data = ESocialUtils.formatarData(fba0101.fba0101dtPgto, ESocialUtils.PATTERN_YYYY_MM_DD) + "/" + StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto)
			String ideDmDev = StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto)
			if(fba0101.fba0101tpVlr == 4 ){
				ideDmDev = fba0101.fba0101tpVlr + fba0101.fba0101dtCalc.format(DateTimeFormatter.ofPattern("ddMMyyyy"))
			}
			
			data += "/" + ideDmDev
			if(!hashFba0101s.containsKey(data)) {
				hashFba0101s.put(data, fba0101)
			}else{
				hashFba0101s.get(data).fba0101liqPago += fba0101.fba0101liqPago
			}
		}
		List<Fba0101> fba0101s = new ArrayList();
		for(fba0101 in hashFba0101s) {
			fba0101s.add(fba0101.getValue());
		}
		
		return fba0101s;
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
	
	private Boolean temS2300OuS2200Aprovado(Long abh80id) {
		String sql = " SELECT * FROM Aaa15 INNER JOIN aap50 on aap50id = aaa15evento WHERE (aap50evento = :aap50evento or aap50evento = :aap50evento2) " +
				" AND aaa15status = :aaa15status " +
				" AND aaa15registro = :aaa15registro ";
		
		Aaa15 aaa15 = getAcessoAoBanco().buscarRegistroUnico(sql,
				Parametro.criar("aap50evento", "S-2300"),
				Parametro.criar("aap50evento2", "S-2200"),
				Parametro.criar("aaa15registro", abh80id),
				Parametro.criar("aaa15status", Aaa15.STATUS_APROVADO));
		
		if(aaa15 == null) return false;
		
		return true;
	}
	
	public BigDecimal buscarValorDeducaoDependente(String aaj50codigo) {
		return session.createQuery("SELECT aaj50vlrDep FROM Aaj50 WHERE aaj50codigo = :aaj50codigo ", samWhere.getWherePadrao("AND", Aaj50.class))
				.setParameter("aaj50codigo", aaj50codigo)
				.getUniqueResult(ColumnType.BIG_DECIMAL);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==