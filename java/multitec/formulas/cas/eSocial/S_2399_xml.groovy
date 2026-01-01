package multitec.formulas.cas.eSocial;

import java.time.format.DateTimeFormatter;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aap14;
import sam.model.entities.fb.Fba01;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba0103;
import sam.model.entities.fb.Fba01031;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2399_xml extends FormulaBase {
	private Aaa15 aaa15;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		Fbd10 fbd10 = get("fbd10");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		Aap14 aap14 = fbd10.fbd10trab.abh80categ != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aap14", fbd10.fbd10trab.abh80categ.aap14id) : null;
		
		def indRetif = get("indRetif");
		def tpAmb = get("tpAmb");
		def gerarVerbasResc = get("gerarVerbasResc");
		
		Map<Long, TableMap> mapDetOper = new HashMap<>();
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtTSVTermino/v_S_01_03_00");
		
		ElementXml evtTSVTermino = eSocial.addNode("evtTSVTermino");
		evtTSVTermino.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtTSVTermino.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtTSVTermino.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml ideTrabSemVinculo = evtTSVTermino.addNode("ideTrabSemVinculo");
		ideTrabSemVinculo.addNode("cpfTrab", fbd10.fbd10trab.abh80cpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(fbd10.fbd10trab.abh80cpf), 11, '0', true) : null, true);
		if(fbd10.fbd10trab.abh80pis != null) ideTrabSemVinculo.addNode("nisTrab", StringUtils.extractNumbers(fbd10.fbd10trab.abh80pis), false);
		ideTrabSemVinculo.addNode("codCateg", aap14 != null ? aap14.aap14eSocial : null, true);
		
		ElementXml infoTSVTermino = evtTSVTermino.addNode("infoTSVTermino");
		infoTSVTermino.addNode("dtTerm", ESocialUtils.formatarData(fbd10.fbd10dtRes, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		
		if(aap14 != null && aap14.aap14eSocial != null) {
			if(aap14.aap14eSocial.equals("721")) {
				infoTSVTermino.addNode("mtvDesligTSV", fbd10.fbd10causa.abh06eSocial, false);
			}
			
			if(aap14.aap14eSocial.equals("201") || aap14.aap14eSocial.equals("202") || aap14.aap14eSocial.equals("721")) {
				infoTSVTermino.addNode("pensAlim", fbd10.fbd10paTipo, false);
				if(fbd10.fbd10paTipo == 1 || fbd10.fbd10paTipo == 3) infoTSVTermino.addNode("percAliment", ESocialUtils.formatarDecimal(fbd10.fbd10paPercFgts, 2, false), false);
				if(fbd10.fbd10paTipo == 2 || fbd10.fbd10paTipo == 3) infoTSVTermino.addNode("vrAlim", ESocialUtils.formatarDecimal(fbd10.fbd10paVlr, 2, false), false);
			}
		}
		
		if(fbd10.fbd10trab.abh80cpfAnt != null) {
			ElementXml mudancaCPF = infoTSVTermino.addNode("mudancaCPF");
			mudancaCPF.addNode("novoCPF", StringUtils.extractNumbers(fbd10.fbd10trab.abh80cpf), true);
		}
		
		if(gerarVerbasResc) {
			List<Fba0101> fba0101s = buscarValoresParaGeracaoDasVerbasRescisorias(fbd10.fbd10trab.abh80id, fbd10.fbd10dtRes.getMonthValue(), fbd10.fbd10dtRes.getYear());
			if(fba0101s != null && fba0101s.size() > 0) {
				ElementXml verbasResc = infoTSVTermino.addNode("verbasResc");
				
				for(Fba0101 fba0101 : fba0101s) {
					ElementXml dmDev = verbasResc.addNode("dmDev");
					dmDev.addNode("ideDmDev", fba0101.fba0101tpVlr + fba0101.fba0101dtCalc.format(DateTimeFormatter.ofPattern("ddMMyyyy")), true);
					
					List<TableMap> tmLotacoes = buscarLotacoesParaGeracaoDasVerbasRescisorias(fba0101.fba0101id);
					if(tmLotacoes != null && tmLotacoes.size() > 0) {
						for(TableMap tm : tmLotacoes) {
							ElementXml ideEstabLot = dmDev.addNode("ideEstabLot");
							ideEstabLot.addNode("tpInsc", tm.getInteger("abh02ti"), true);
							
							String nrInsc = StringUtils.extractNumbers(obterEmpresaAtiva().aac10ni);
							ideEstabLot.addNode("nrInsc", StringUtils.ajustString(nrInsc, 14, '0', true), true);
							ideEstabLot.addNode("codLotacao", tm.getString("abh02codigo"), true);
							
							mapDetOper.clear();
							
							List<TableMap> tmEventos = buscarEventosDeValoresParaGeracaoDasVerbasRescisorias(fba0101.fba0101id, tm.getLong("abh02id"));
							if(tmEventos != null && tmEventos.size() > 0) {
								for(TableMap tmEve : tmEventos) {
									if(tmEve.getBigDecimal("fba01011valor").compareTo(BigDecimal.ZERO) <= 0) continue;
									
									if((tmEve.getInteger("abh21esPrev") != null && isCodigoValido(["23", "24", "61"], StringUtils.ajustString(tmEve.getInteger("abh21esPrev"), 2, '0', true))) ||
									   (tmEve.getInteger("abh21esIr") != null && isCodigoValido(["31", "32", "33", "34", "35", "51", "52", "53", "54", "55", "81", "82", "83"], StringUtils.ajustString(tmEve.getInteger("abh21esIr"), 2, '0', true))) ||
									   (aap14.aap14eSocial != null && (aap14.aap14eSocial.startsWith("7") || aap14.aap14eSocial.startsWith("9")) && isCodigoValido(["25", "26", "51"], StringUtils.ajustString(tmEve.getInteger("abh21esPrev"), 2, '0', true)))) continue;
									
									ElementXml detVerbas = ideEstabLot.addNode("detVerbas");
									detVerbas.addNode("codRubr", tmEve.getString("abh21codigo"), true);
									detVerbas.addNode("ideTabRubr", tmEve.getString("abh21codigo"), true);
									
									BigDecimal qtdRubr = tmEve.getBigDecimal("fba01011refHoras") > 0 ? tmEve.getBigDecimal("fba01011refHoras") :
														 tmEve.getBigDecimal("fba01011refDias") > 0 ? tmEve.getBigDecimal("fba01011refDias") :
														 tmEve.getBigDecimal("fba01011refUnid");
										
									detVerbas.addNode("qtdRubr", ESocialUtils.formatarDecimal(qtdRubr, 2, false), false);
									detVerbas.addNode("fatorRubr", ESocialUtils.formatarDecimal(tmEve.getBigDecimal("abh21fator"), 2, false), false);
									
									BigDecimal vrUnit = qtdRubr > 0 ? tmEve.getBigDecimal("fba01011valor") / qtdRubr : 0;
									detVerbas.addNode("vrUnit", ESocialUtils.formatarDecimal(vrUnit, 2, false), false);
									
									BigDecimal vrRubr = tmEve.getBigDecimal("fba01011valor");
									detVerbas.addNode("vrRubr", ESocialUtils.formatarDecimal(vrRubr, 2, false), true);
									
									detVerbas.addNode("indApurIR", tmEve.getInteger("abh21esApurIr"), true);
									
									if(tmEve.getString("aap57codigo") != null && tmEve.getString("aap57codigo").equals("9219") && tmEve.getString("abh21esOpsCnpj") != null) {
										TableMap tmDetOper = mapDetOper.get(tmEve.getLong("abh21id"));
										if(tmDetOper == null) tmDetOper = new TableMap();
										tmDetOper.put("abh21id", tmEve.getLong("abh21id"));
										tmDetOper.put("cnpjOper", tmEve.getString("abh21esOpsCnpj"));
										tmDetOper.put("regANS", tmEve.getString("abh21esOpsReg"));
										tmDetOper.put("vrPgTit", tmEve.getBigDecimal("vrPgTit") != null ? tmEve.getBigDecimal("vrPgTit") + tmEve.getBigDecimal("fba01011valor") : tmEve.getBigDecimal("fba01011valor"));
										mapDetOper.put(tmEve.getLong("abh21id"), tmDetOper);
									}
								}
							}
							
							if(mapDetOper != null && mapDetOper.size() > 0) {
								ElementXml infoSaudeColet = ideEstabLot.addNode("infoSaudeColet");
								for(Object key : mapDetOper.keySet()) {
									ElementXml detOper = infoSaudeColet.addNode("detOper");
									TableMap tmDetOper = mapDetOper.get(key);
									
									String cnpjOper = tmDetOper.getString("cnpjOper");
									detOper.addNode("cnpjOper", StringUtils.ajustString(StringUtils.extractNumbers(cnpjOper), 14, '0', false), true);
									
									String regANS = tmDetOper.getString("regANS");
									detOper.addNode("regANS", StringUtils.ajustString(StringUtils.extractNumbers(regANS), 6, '0', true), true);
									
									BigDecimal vrPgTit = tmDetOper.getBigDecimal("vrPgTit");
									detOper.addNode("vrPgTit", ESocialUtils.formatarDecimal(vrPgTit, 2, false), true);
									
									List<TableMap> tmsEveDep = buscarEventosDosDependentesParaGeracaoDasVerbasRescisorias(fbd10.fbd10trab.abh80id, tmDetOper.getLong("abh21id"));
									if(tmsEveDep != null && tmsEveDep.size() > 0) {
										for(TableMap tmEve : tmsEveDep) {
											ElementXml detPlano = detOper.addNode("detPlano");
											detPlano.addNode("tpDep", tmEve.getString("aap09codigo") != null ? tmEve.getString("aap09codigo") : "99", true);
											if(tmEve.getString("abh8002cpf") != null) detPlano.addNode("cpfDep", StringUtils.ajustString(StringUtils.extractNumbers(tmEve.getString("abh8002cpf")), 11, '0', true), false);
											detPlano.addNode("nmDep", tmEve.getString("abh8002nome"), true);
											detPlano.addNode("dtNascto", ESocialUtils.formatarData(tmEve.getDate("abh8002dtNasc"), ESocialUtils.PATTERN_YYYY_MM), true);
											detPlano.addNode("vlrPgDep", ESocialUtils.formatarDecimal(tmEve.getBigDecimal("abh80021vlr"), 2, false), true);
										}
									}
								}
							}
							
							if(tm.getInteger("abh02aposEspec") != null) {
								ElementXml infoAgNocivo = ideEstabLot.addNode("infoAgNocivo");
								infoAgNocivo.addNode("grauExp", tm.getInteger("abh02aposEspec"), true);
							}
							
							if(tm.getInteger("abh02contribSubst") != null) {
								ElementXml infoSimples = ideEstabLot.addNode("infoSimples");
								infoSimples.addNode("indSimples", tm.getInteger("abh02contribSubst"), true);
							}
						}
					}
				}
				
				List<TableMap> tmProcJud = buscarProcessosJudiciaisParaGeracaoDasVerbasRescisorias(fbd10.fbd10trab.abh80id);
				if(tmProcJud != null && tmProcJud.size() > 0) {
					for(TableMap tm : tmProcJud) {
						ElementXml procJudTrab = verbasResc.addNode("procJudTrab");
						procJudTrab.addNode("tpTrib", tm.getInteger("abb40tipo"), true);
						procJudTrab.addNode("nrProcJud", tm.getString("abb40num"), true);
						procJudTrab.addNode("codSusp", tm.getString("abb4001codSusp"), true);
					}
				}
				
				Fba01 fba01 = buscarCalculoParaGeracaoDasVerbasRescisorias(fbd10.fbd10dtRes.getMonthValue(), fbd10.fbd10dtRes.getYear());
				List<Fba0103> fba0103s = buscarOutrosVinculosParaGeracaoDasVerbasRescisorias(fba01.fba01id, fbd10.fbd10trab.abh80id);
				if(fba0103s != null && fba0103s.size() > 0) {
					for(Fba0103 fba0103 : fba0103s) {
						ElementXml infoMV = verbasResc.addNode("infoMV");
						infoMV.addNode("indMV", fba0103.fba0103contrib, true);
						
						for(Fba01031 fba01031 : fba0103.fba01031s) {
							ElementXml remunOutrEmpr = infoMV.addNode("remunOutrEmpr");
							remunOutrEmpr.addNode("tpInsc", fba01031.fba01031ti, true);
							
							String nrInsc = StringUtils.extractNumbers(fba01031.fba01031ni);
							remunOutrEmpr.addNode("nrInsc", StringUtils.ajustString(nrInsc, 14, '0', true), true);
							remunOutrEmpr.addNode("codCateg", fba01031.fba01031categ.aap14eSocial, true);
							remunOutrEmpr.addNode("vlrRemunOE", ESocialUtils.formatarDecimal(fba01031.fba01031vlrRem, 2, false), true);
						}
					}
				}
			}
		}
		
		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
	
	public static boolean isCodigoValido(String[] codigosValidos, String codigoUsado) {
		for(int i = 0; i < codigosValidos.length; i++) {
			if(codigosValidos[i].equals(codigoUsado)) return true;
		}
		return false;
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
	
	private List<Fba0101> buscarValoresParaGeracaoDasVerbasRescisorias(Long abh80id, int mes, int ano) {
		String sql = "SELECT * FROM Fba0101 as fba0101 " +
					 "WHERE fba0101.fba0101trab = :abh80id AND " + 
					 "DATE_PART('MONTH', fba0101dtCalc) = :mes AND DATE_PART('YEAR', fba0101dtCalc) = :ano AND " + 
                     "fba0101.fba0101tpVlr IN (1, 4) " +
					 "ORDER BY fba0101.fba0101tpVlr";
		
		List<Fba0101> fba0101s = getAcessoAoBanco().buscarListaDeRegistros(sql, 
				Parametro.criar("abh80id", abh80id), 
				Parametro.criar("mes", mes), 
				Parametro.criar("ano", ano));
		
		return fba0101s;
	}
	
	private List<TableMap> buscarLotacoesParaGeracaoDasVerbasRescisorias(Long fba0101id) {
		String sql = "SELECT DISTINCT abh02id, abh02ti, abh02ni, abh02codigo, abh02aposEspec, abh02contribSubst "+
					 "FROM Fba01011 " +
					 "INNER JOIN Abh02 ON abh02id = fba01011lotacao " +
					 "WHERE fba01011vlr = :fba0101id " +
					 "ORDER BY abh02codigo";
		
		List<TableMap> tmLotacoes = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("fba0101id", fba0101id));
		return tmLotacoes;
	}
	
	private List<TableMap> buscarEventosDeValoresParaGeracaoDasVerbasRescisorias(Long fba0101id, Long abh02id) {
		String sql = "SELECT abh21id, abh21codigo, abh21esIr, abh21esApurIr, abh21esPrev, abh21fator, abh21esOpsCnpj, abh21esOpsReg, fba01011refHoras, fba01011refDias, fba01011refUnid, fba01011valor, aap57codigo " +
					 "FROM Fba01011 " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "LEFT JOIN Aap57 ON aap57id = abh21esNatRub " +
					 "WHERE fba01011vlr = :fba0101id AND fba01011lotacao = :abh02id " +
					 "ORDER BY abh21codigo";
		
		List<TableMap> tmEventos = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("fba0101id", fba0101id), Parametro.criar("abh02id", abh02id));
		return tmEventos;
	}
	
	private List<TableMap> buscarEventosDosDependentesParaGeracaoDasVerbasRescisorias(Long abh80id, Long abh21id) {
		String sql = "SELECT abh21id, abh8002cpf, abh8002nome, abh8002dtNasc, abh80021vlr, aap09codigo " +
					 "FROM Abh80021 " +
					 "INNER JOIN Abh8002 ON abh8002id = abh80021dep " +
					 "INNER JOIN Abh21 ON abh21id = abh80021eve " +
					 "LEFT JOIN Aap09 ON aap09id = abh8002parente " +
					 "WHERE abh8002trab = :abh80id AND abh21id = :abh21id AND abh21esOpsCnpj IS NOT NULL";
		
		List<TableMap> tmEventos = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("abh80id", abh80id), Parametro.criar("abh21id", abh21id));
		return tmEventos;
	}
	
	private List<TableMap> buscarProcessosJudiciaisParaGeracaoDasVerbasRescisorias(Long abh80id) {
		String sql = "SELECT abb40tipo, abb40num, abb4001codSusp " +
					 "FROM Abh8006 " +
					 "INNER JOIN Abb40 ON abb40id = abh8006processo " +
					 "INNER JOIN Abb4001 ON abb4001processo = abb40id " +
					 "WHERE abh8006trab = :abh80id " +
					 "ORDER BY abb40num";
		
		List<TableMap> tmProcJud = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("abh80id", abh80id));
		return tmProcJud;
	}
	
	private Fba01 buscarCalculoParaGeracaoDasVerbasRescisorias(Integer fba01mes, Integer fba01ano) {
		String sql = "SELECT * FROM Fba01 " +
					 "WHERE fba01mes = :fba01mes AND fba01ano = :fba01ano " +
					 getSamWhere().getWherePadrao("AND", Fba01.class) +
					 " LIMIT 1";
		
		Fba01 fba01 = getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("fba01mes", fba01mes), Parametro.criar("fba01ano", fba01ano));
		return fba01;
	}
	
	private List<Fba0103> buscarOutrosVinculosParaGeracaoDasVerbasRescisorias(Long fba01id, Long abh80id) {
		String sql = "SELECT * FROM Fba0103 as fba0103 " +
					 "INNER JOIN FETCH fba0103.fba01031s as fba01031s " +
					 "INNER JOIN FETCH fba01031s.fba01031categ as aap14 " +
					 "WHERE fba0103calculo = :fba01id AND fba0103trab = :abh80id " +
					 "ORDER BY fba0103contrib, fba01031nome";
		
		List<Fba0103> fba0103s = getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("fba01id", fba01id), Parametro.criar("abh80id", abh80id));
		return fba0103s;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==