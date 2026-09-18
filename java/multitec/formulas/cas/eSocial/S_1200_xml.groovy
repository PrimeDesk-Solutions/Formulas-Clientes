package multitec.formulas.cas.eSocial;

import java.time.LocalDate;
import java.util.stream.Collectors
import java.util.*
import java.util.Comparator;
import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aap03;
import sam.model.entities.aa.Aap50
import sam.model.entities.ab.Aba2001
import sam.model.entities.ab.Abb40;
import sam.model.entities.ab.Abh02;
import sam.model.entities.ab.Abh05;
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Abh8006;
import sam.model.entities.fb.Fba01
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fba010111;
import sam.model.entities.fb.Fba0103;
import sam.model.entities.fb.Fba01031;
import sam.model.entities.fb.Fba30;
import sam.model.entities.fb.Fba31;
import sam.model.entities.fb.Fba3101;
import sam.model.entities.fb.Fba31011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.ValidacaoESocial;

public class S_1200_xml extends FormulaBase {
	private Aaa15 aaa15;
	private LocalDate mesAnoRef;
	private List<Fba0101> fba0101s;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		aaa15 = get("aaa15");
		def teste = aaa15.getAaa15dados()
		def tpAmb = get("tpAmb");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		Abh80 abh80 = getAcessoAoBanco().buscarRegistroUnicoById("abh80", get("abh80id"));
		int indApuracao = get("indApuracao");
		mesAnoRef = get("perApur");
		int indRetif = aaa15.aaa15tipo == Aaa15.TIPO_RETIFICACAO ? 2 : 1;
		fba0101s = get("fba0101s");

		HashMap<String, Fba0101> fba0101sMap = new HashMap<String, Fba0101>()

		for(fba0101 in fba0101s) {
			String chave = StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto)
			if(!fba0101sMap.containsKey(chave)) {
				fba0101sMap.put(chave, fba0101)
			}else{
				fba0101sMap.get(chave).fba01011s.addAll(fba0101.fba01011s)
			}
		}

		List<Fba0101> newListFba0101s = new ArrayList()

		for( fba0101 in fba0101sMap){
			newListFba0101s.add(fba0101.getValue())
		}
		fba0101s = newListFba0101s

		Collections.sort(fba0101s, new Comparator<Fba0101>() {
		@Override
		public int compare(Fba0101 fba0101_1, Fba0101  fba0101_2)
		{
			def dmDev1 = StringUtils.concat(fba0101_1.fba0101tpVlr, fba0101_1.fba0101tpPgto)
			def dmDev2 = StringUtils.concat(fba0101_2.fba0101tpVlr, fba0101_2.fba0101tpPgto)
			return  dmDev1.compareTo(dmDev2);
		}
	});
		
		// Carregando Paramêtros do eSocial no Repositório de Dados
		Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("SFP-PARAM-ESOCIAL", "jGet(aba2001json.evento_esocial) = 'S-1200'");
		def eventoInss = aba2001?.getAba2001json()?.get("evento_inss") ?: null;
		def eventoInssFerias = aba2001?.getAba2001json()?.get("evento_inss_ferias") ?: null;

		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtRemun/v_S_01_01_00");
		
		ElementXml evtRemun = eSocial.addNode("evtRemun");
		evtRemun.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtRemun.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("indApuracao", indApuracao, true);

		String perApur = indApuracao == 1 ? ESocialUtils.formatarData(mesAnoRef, ESocialUtils.PATTERN_YYYY_MM) : ESocialUtils.formatarData(mesAnoRef, ESocialUtils.PATTERN_YYYY);
		ideEvento.addNode("perApur", perApur, true);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtRemun.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti + 1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if (aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		} else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml ideTrabalhador = evtRemun.addNode("ideTrabalhador");
		ideTrabalhador.addNode("cpfTrab", StringUtils.ajustString(StringUtils.extractNumbers(abh80.abh80cpf), 11, '0', true), true);
		
		List<Fba0103> fba0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Fba0103 AS fba0103 LEFT JOIN FETCH fba0103.fba01031s AS fba01031s " +
																		   " LEFT JOIN FETCH fba01031s.fba01031categ AS fba01031categ " +
																		   "WHERE fba0103trab = :idTrab AND fba0103calculo = :idCalculo",
																		   Parametro.criar("idTrab", abh80.abh80id),
																		   Parametro.criar("idCalculo", fba0101s.get(0).fba0101calculo.fba01id));
		
		if (fba0103s != null  && fba0103s.size() > 0) {
			for (Fba0103 fba0103 : fba0103s) {
				ElementXml infoMV = ideTrabalhador.addNode("infoMV");
				infoMV.addNode("indMV", fba0103.fba0103contrib, true);
				
				Collection<Fba01031> fba01031s = fba0103.fba01031s;
				if (fba01031s != null && fba01031s.size() > 0) {
					for(Fba01031 fba01031 : fba01031s) {
						ElementXml remunOutrEmpr = infoMV.addNode("remunOutrEmpr");
						remunOutrEmpr.addNode("tpInsc", fba01031.fba01031ti + 1, true);
						
						String nrInsc = StringUtils.extractNumbers(fba01031.fba01031ni);
						if (fba01031.fba01031ti == 0) {
							nrInsc = StringUtils.ajustString(nrInsc, 14, '0', false);
						} else {
							nrInsc = StringUtils.ajustString(nrInsc, 11, '0', true);
						}
						remunOutrEmpr.addNode("nrInsc", nrInsc, true);
						remunOutrEmpr.addNode("codCateg", fba01031.fba01031categ.aap14eSocial, true);
						remunOutrEmpr.addNode("vlrRemunOE", ESocialUtils.formatarDecimal(fba01031.fba01031vlrRem, 2, false), true);
					}
				}
			}
			
		}
		
		if(abh80.abh80categ != null && abh80.abh80categ.aap14eSocial != null) {
			if(ValidacaoESocial.isCodigoValido(["701", "711", "712", "741"] as String[], abh80.abh80categ.aap14eSocial)) {
				ElementXml infoComplem = ideTrabalhador.addNode("infoComplem");
				infoComplem.addNode("nmTrab", abh80.abh80nome, true);
				infoComplem.addNode("dtNascto", abh80.abh80nascData, true);
			}
		}

		if(abh80.abh8006s != null && abh80.abh8006s.size() > 0) {
			Collection<Abh8006> abh8006s = abh80.abh8006s;
			for (Abh8006 abh8006 : abh8006s) {
				Abb40 abb40 = abh8006.abh8006processo;
				ElementXml procJudTrab = ideTrabalhador.addNode("procJudTrab");
				procJudTrab.addNode("tpTrib", abb40.abb40tipo, true);
				procJudTrab.addNode("nrProcJud", abb40.abb40num, true);
				procJudTrab.addNode("codSusp", abb40.abb4001s.stream().findFirst().get().abb4001codSusp, true);
			}
		}
		
		def valorInssNegativo = 0;
		for (Fba0101 fba0101 : fba0101s) {
			if(fba0101.fba0101diasInterm != null) {
				String[] dias = fba0101.fba0101diasInterm.split(";");
				for (String dia : dias) {
					if (dia.length() == 0) continue;
					ElementXml infoInterm = ideTrabalhador.addNode("infoInterm");
					infoInterm.addNode("dia", dia, true);
				}
			}
			
			ElementXml dmDev = evtRemun.addNode("dmDev");
			dmDev.addNode("ideDmDev", StringUtils.concat(fba0101.fba0101tpVlr, fba0101.fba0101tpPgto), true);
			if(abh80.abh80categ != null) dmDev.addNode("codCateg", abh80.abh80categ.aap14eSocial, true);
			
			if(fba0101.fba01011s != null && fba0101.fba01011s.size() > 0) {
				ElementXml infoPerApur = dmDev.addNode("infoPerApur");
				Map<Long, List<Fba01011>> mapLotacaoFba01011s = fba0101.fba01011s.stream().collect(Collectors.groupingBy({fba01011 -> fba01011.fba01011lotacao.abh02id}));
				for (Long abh02id : mapLotacaoFba01011s.keySet()) {
					ElementXml ideEstabLot = infoPerApur.addNode("ideEstabLot");
					Abh02 abh02 = getAcessoAoBanco().buscarRegistroUnicoById("Abh02", abh02id);
					ideEstabLot.addNode("tpInsc", abh02.abh02ti != null ? abh02.abh02ti + 1 : null, true);
					
					String nrInsc = StringUtils.extractNumbers(abh02.abh02ni);
					ideEstabLot.addNode("nrInsc", StringUtils.ajustString(nrInsc, 14, '0', true), true);
					ideEstabLot.addNode("codLotacao", abh02.abh02codigo, true);
					
					ElementXml remunPerApur = ideEstabLot.addNode("remunPerApur");
					if (abh80.abh80tipo == 0 || foiEnviadoMatricula()) remunPerApur.addNode("matricula", abh80.abh80codigo, false);
					remunPerApur.addNode("indSimples", abh02.abh02contribSubst, false);
					List<Fba01011> fba01011s = mapLotacaoFba01011s.get(abh02id).stream().sorted({o1, o2 -> o1.fba01011eve.abh21codigo.compareTo(o2.fba01011eve.abh21codigo)}).collect(Collectors.toList());
					for (Fba01011 fba01011 : fba01011s) {
						Abh21 abh21 = getAcessoAoBanco().buscarRegistroUnicoById("Abh21", fba01011.fba01011eve.abh21id);
						def vrRubr = fba01011.fba01011valor;
						if (eventoInss != null && eventoInssFerias != null && abh21.abh21codigo == eventoInss) {
							def valorInssFerias = buscarValorDoEvento(abh80.abh80id, mesAnoRef.monthValue, mesAnoRef.year, eventoInssFerias);
							vrRubr = vrRubr - valorInssFerias;
							if (vrRubr < 0) {
								valorInssNegativo = vrRubr * (-1);
							}
						}
						if (eventoInssFerias != null && abh21.abh21codigo == eventoInssFerias && valorInssNegativo > 0) {
							vrRubr = vrRubr - valorInssNegativo;
						}
						if (vrRubr <= 0) continue;
						
						ElementXml itensRemun = remunPerApur.addNode("itensRemun");
						itensRemun.addNode("codRubr", abh21.abh21codigo, true);
						itensRemun.addNode("ideTabRubr", abh21.abh21codigo, true);
						BigDecimal qtdRubr = BigDecimal.ONE;
						if (fba01011.fba01011refDias > 0) qtdRubr = fba01011.fba01011refDias;
						if (fba01011.fba01011refHoras > 0) qtdRubr = fba01011.fba01011refHoras;
						if (fba01011.fba01011refUnid > 0) qtdRubr = fba01011.fba01011refUnid;
						if (qtdRubr.compareTo(BigDecimal.ZERO) > 0) itensRemun.addNode("qtdRubr", qtdRubr,false);
						if (abh21.abh21fator.compareTo(BigDecimal.ZERO) > 0) itensRemun.addNode("fatorRubr", ESocialUtils.formatarDecimal(abh21.abh21fator, 2, false), false);
						if (vrRubr.compareTo(BigDecimal.ZERO) > 0) itensRemun.addNode("vrRubr", ESocialUtils.formatarDecimal(vrRubr, 2, false), true);
						if (indApuracao == 1 && mesAnoRef.compareTo(LocalDate.of(2021, 07, 01)) >= 0 || indApuracao == 2 && mesAnoRef.getYear() >= 2021) itensRemun.addNode("indApurIR", abh21.abh21esApurIr, false);
						
					}
					ElementXml infoAgNocivo = remunPerApur.addNode("infoAgNocivo");
					infoAgNocivo.addNode("grauExp", abh02.abh02aposEspec, true);
				}
			}

			if(abh80.abh80tipo == 1){
				ElementXml infoComplCont = dmDev.addNode("infoComplCont")
				def cargo = getSession().createCriteria(Abh05.class)
								.addWhere(Criterions.eq("abh05id",abh80.abh80cargo.abh05id))
								.get(ColumnType.ENTITY)
				if(cargo != null){
					def cbo = getSession().createCriteria(Aap03.class)
								.addWhere(Criterions.eq("aap03id",cargo.abh05cbo.aap03id))
								.get(ColumnType.ENTITY)
				 if(cbo != null)infoComplCont.addNode("codCBO", cbo.aap03codigo ,true)
				}
			}
			
			List<Fba31> fba31s = buscarFba31011s(fba0101);
			if(fba31s != null && fba31s.size() > 0) {
				ElementXml infoPerAnt = dmDev.addNode("infoPerAnt");
				for (Fba31 fba31 : fba31s) {
					Fba30 fba30 = fba31.fba31instrum;
					ElementXml ideADC = infoPerAnt.addNode("ideADC");
					ideADC.addNode("dtAcConv", fba30.fba30data, false);
					ideADC.addNode("tpAcConv", fba30.fba30tipo, true);
					ideADC.addNode("dsc", fba30.fba30descr, true);
					ideADC.addNode("remunSuc", fba30.fba30vs, true);
					
					ElementXml idePeriodo = ideADC.addNode("idePeriodo");
					idePeriodo.addNode("perRef", StringUtils.concat(fba31.fba31ano, "-", fba31.fba31mes), true);
					
					Collection<Fba3101> fba3101s = fba31.fba3101s;
					for (Fba3101 fba3101 : fba3101s) {
						ElementXml ideEstabLot = idePeriodo.addNode("ideEstabLot");
						ideEstabLot.addNode("tpInsc", fba3101.fba3101ti, true);
						ideEstabLot.addNode("nrInsc", StringUtils.extractNumbers(fba3101.fba3101ni), true);
						ideEstabLot.addNode("codLotacao", fba3101.fba3101lotacao.abh02codigo, true);
						
						if(fba3101.fba31011s != null && fba3101.fba31011s.size() > 0) {
							ElementXml remunPerAnt = ideEstabLot.addNode("remunPerAnt");
							if (abh80.abh80tipo == 0) remunPerAnt.addNode("matricula", abh80.abh80codigo, true);
							remunPerAnt.addNode("indSimples", fba3101.fba3101lotacao.abh02contribSubst, true);
							
							List<Fba31011> fba31011s = fba3101.fba31011s.stream().sorted({o1, o2 -> o1.fba31011eve.fba01011eve.abh21codigo.compareTo(o2.fba31011eve.fba01011eve.abh21codigo)}).collect(Collectors.toList());
							for (Fba31011 fba31011 : fba31011s) {
								Abh21 abh21 = getAcessoAoBanco().buscarRegistroUnicoById("Abh21", fba31011.fba31011eve.fba01011eve.abh21id);
								String aap14eSocial = abh80.abh80categ?.aap14eSocial?: null;
								if(ValidacaoESocial.isCodigoValido(["23", "24", "61"] as String[], StringUtils.ajustString(abh21.abh21esPrev, 2, '0', true)) ||
								ValidacaoESocial.isCodigoValido(["31", "32", "33", "34", "35", "51", "52", "53", "54", "55", "81", "82", "83"] as String[], StringUtils.ajustString(abh21.abh21esIr, 2, '0', true)) ||
								(aap14eSocial != null && (aap14eSocial.startsWith("7") || aap14eSocial.startsWith("9")) &&
								ValidacaoESocial.isCodigoValido(["25", "26", "51"] as String[], StringUtils.ajustString(abh21.abh21esPrev, 2, '0', true)))) continue;
					
								ElementXml itensRemun = remunPerAnt.addNode("itensRemun");
								itensRemun.addNode("codRubr", fba31011.fba31011eve.fba01011eve.abh21codigo, true);
								itensRemun.addNode("ideTabRubr", fba31011.fba31011eve.fba01011eve.abh21codigo, true);
								BigDecimal qtdRubr = BigDecimal.ONE;
								if(fba31011.fba31011eve.fba01011refDias > 0) qtdRubr = fba31011.fba31011eve.fba01011refDias;
								if(fba31011.fba31011eve.fba01011refHoras > 0) qtdRubr = fba31011.fba31011eve.fba01011refHoras;
								if(fba31011.fba31011eve.fba01011refUnid > 0) qtdRubr = fba31011.fba31011eve.fba01011refUnid;
								if (qtdRubr.compareTo(BigDecimal.ZERO) > 0) itensRemun.addNode("qtdRubr", ESocialUtils.formatarDecimal(qtdRubr, 2, false), false);
								if (fba31011.fba31011eve.fba01011eve.abh21fator.compareTo(BigDecimal.ZERO) > 0) itensRemun.addNode("fatorRubr", ESocialUtils.formatarDecimal(fba31011.fba31011eve.fba01011eve.abh21fator, 2, false), false);
								if (fba31011.fba31011eve.fba01011valor.compareTo(BigDecimal.ZERO) > 0) itensRemun.addNode("vrRubr", ESocialUtils.formatarDecimal(fba31011.fba31011eve.fba01011valor, 2, false), true);
								if (indApuracao == 1 && mesAnoRef.compareTo(LocalDate.of(2021, 07, 01)) >= 0 || indApuracao == 2 && mesAnoRef.getYear() >= 2021) itensRemun.addNode("indApurIR", fba31011.fba31011eve.fba01011eve.abh21esApurIr, false);
							}
							
							Abh02 abh02 = getAcessoAoBanco().buscarRegistroUnicoById("Abh02", fba3101.fba3101lotacao.abh02id);
							ElementXml infoAgNocivo = remunPerAnt.addNode("infoAgNocivo");
							infoAgNocivo.addNode("grauExp", abh02.abh02aposEspec, true);
						}
					}
				}
			}
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
	
	private boolean verificaSeTemEventoS2299ES2399AprovadoParaOTrabalhador(Long abh80id, String cnpj, LocalDate[] periodo) {
		List<Long> aap50ids = session.createCriteria(Aap50.class)
				.addFields("aap50id")
				.addWhere(Criterions.in("aap50evento", ["S-2299", "S-2399"]))
				.getList(ColumnType.LONG);

		long count = session.createCriteria(Aaa15.class)
				.addFields("COUNT(aaa15id)")
				.addWhere(Criterions.in("aaa15evento", aap50ids))
				.addWhere(Criterions.between("aaa15data", periodo[0], periodo[1]))
				.addWhere(Criterions.eq("aaa15cnpj", cnpj))
				.addWhere(Criterions.eq("aaa15tabela", "Abh80").ignoreCase(true))
				.addWhere(Criterions.eq("aaa15registro", abh80id))
				.addWhere(Criterions.eq("aaa15status", Aaa15.STATUS_APROVADO))
				.get(ColumnType.LONG);

		return count > 0;
	}
	
	private BigDecimal buscarValorDoEvento(Long abh80id, int mes, int ano, String abh21codigo) {
		String sql = " SELECT fba01011valor FROM Fba01011 AS fba01011 " +
					 " INNER JOIN Fba0101 AS fba0101 ON fba0101id = fba01011vlr " +
					 " INNER JOIN Abh21 AS abh21 ON abh21id = fba01011eve " +
					 " WHERE fba0101.fba0101trab = :abh80id AND (DATE_PART('MONTH', fba0101dtCalc)) = :mes AND (DATE_PART('YEAR', fba0101dtCalc)) = :ano " +
					 " AND fba0101.fba0101tpVlr IN (3) " +
					 " AND abh21.abh21codigo = :abh21codigo"
					 samWhere.getWherePadrao("AND", Fba01.class);
					 
		def fba01011valor = getAcessoAoBanco().obterBigDecimal(sql, criarParametroSql("abh80id", abh80id), criarParametroSql("mes", mes), criarParametroSql("ano", ano), criarParametroSql("abh21codigo", abh21codigo));
		return fba01011valor;
	}
	
	private List<Fba010111> buscarDependentes(long fba01011id) {
		return getSession().createCriteria(Fba010111.class).addJoin(Joins.fetch("fba010111dep").left(false)).addWhere(Criterions.eq("fba010111evento", fba01011id)).getList(ColumnType.ENTITY);
	}

	private Abh21 buscarAbh21(long abh21id) {
		return getSession().createCriteria(Abh21.class).addWhere(Criterions.eq("abh21id", abh21id)).get(ColumnType.ENTITY);
	}

	private List<Fba31> buscarFba31011s(Fba0101 fba0101) {
		return getSession().createCriteria(Fba31.class)
						   .alias("fba31")
						   .addJoin(Joins.fetch("fba31.fba31instrum").left(false).alias("fba30"))
						   .addJoin(Joins.fetch("fba3101s").left(false).alias("fba3101s"))
						   .addJoin(Joins.fetch("fba3101s.fba3101lotacao").left(false).alias("abh02"))
						   .addJoin(Joins.fetch("fba3101s.fba31011s").left(false).alias("fba31011s"))
						   .addJoin(Joins.fetch("fba31011s.fba31011eve").left(false).alias("fba01011"))
						   .addJoin(Joins.fetch("fba01011.fba01011eve").left(false).alias("abh21"))
						   .addJoin(Joins.fetch("fba01011.fba01011vlr").left(false).alias("fba0101"))
						   .addWhere(Criterions.eq("fba01011.fba01011vlr", fba0101.fba0101id))
						   .addWhere(Criterions.eq("fba31mes", mesAnoRef.getMonthValue()))
						   .addWhere(Criterions.eq("fba31ano", mesAnoRef.getYear()))
						   .addWhere(Criterions.eq("fba0101.fba0101trab", fba0101.fba0101trab.abh80id))
						   .getList(ColumnType.ENTITY);
	}
	
	public BigDecimal buscarRefDias(String ... eventos) {
		BigDecimal total = new BigDecimal(0);
		for (String abh21codigo : eventos) {
			for (Fba0101 fba0101 : fba0101s) {
				for (Fba01011 fba01011 : fba0101.fba01011s) {
					if(fba01011.fba01011eve.abh21codigo.equalsIgnoreCase(abh21codigo)) {
						total = total.add(fba01011.fba01011refDias);
					}
				}
			}
		}
		return total;
	}
	
	private boolean foiEnviadoMatricula() {
		Aap50 aap50 = getAcessoAoBanco().buscarRegistroUnico("SELECT aap50id FROM Aap50 WHERE aap50evento = :aap50evento", criarParametroSql("aap50evento", "S-2300"));
		
		String sql = " SELECT * FROM Aaa15" +
					 " WHERE aaa15status = :aaa15status AND UPPER(aaa15tabela) = :aaa15tabela " +
					 " AND aaa15evento = :aaa15evento AND aaa15registro = :aaa15registro " +
					 " ORDER BY aaa15id DESC";
					 
		Aaa15 aaa15Enviado = getAcessoAoBanco().buscarRegistroUnico(sql, criarParametroSql("aaa15status", Aaa15.STATUS_APROVADO),
																		 criarParametroSql("aaa15tabela", "ABH80"),
																		 criarParametroSql("aaa15evento", aap50.aap50id),
																		 criarParametroSql("aaa15registro", get("abh80id")));
		if (aaa15Enviado != null) {
			String aaa15xmlEnvio = aaa15Enviado.aaa15xmlEnvio;
			ElementXml el1 = new ElementXml(new StringReader(aaa15xmlEnvio));
			ElementXml subEl1 = el1.findChildNode("matricula");
			return subEl1.getAttValue("matricula") != null;
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
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==