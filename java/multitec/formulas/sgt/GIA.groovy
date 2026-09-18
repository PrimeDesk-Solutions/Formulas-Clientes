package multitec.formulas.sgt

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac13
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aaj01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class GIA extends FormulaBase {
	DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd")
	DateTimeFormatter yyyyMM = DateTimeFormatter.ofPattern("yyyyMM")
	DateTimeFormatter HHmmss = DateTimeFormatter.ofPattern("HHmmss")
	
	private Aac10 aac10;
	private Aac13 aac13;
	private Aaj01 aaj01;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_GIA;
	}
	
	@Override
	public void executar() {
		LocalDate referencia = get("referencia");
		Integer tipo = get("tipo");
		
		aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
		
		aac13 = aac10.aac13fiscal;
		if(aac13 == null) throw new ValidacaoException("Necessário informar os parâmetros fiscais para a empresa.");
		
		aaj01 = aac13.aac13classTrib;
		if(aaj01 == null) throw new ValidacaoException("Necessário informar a classificação tributária nos parâmetros fiscais da empresa.");
		
		def mes = referencia.getMonthValue(); 
		def ano = referencia.getYear();
		
		selecionarAlinhamento("0061");
		
		def txt = new TextFile("");

		//CR 10
		def rsTipo10 = buscarDocumentosPorCFOP(mes, ano, getCampo("CR10", "bcIcms"), getCampo("CR10", "icms"), getCampo("CR10", "isentas"), getCampo("CR10", "outras"), getCampo("CR10", "icmsST"), getCampo("CR10", "vlrGIA"))
		
		//CR 20
		def rsTipo20 = buscarOcorrenciasPorMesAno(mes, ano);
		
		//CR 31
		def	lstTipo31 = buscarNumRegExportacoes(mes, ano);
		
		/**
		 * CR 01
		 */
		txt.print("01");
		txt.print("01");
		txt.print(MDate.date().format(yyyyMMdd));
		txt.print(MDate.time().format(HHmmss));
		txt.print(0, 4);
		txt.print("0210");
		txt.print("0001");
		txt.newLine();
		
		
		/**
		 * CR 05
		 */
		def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)
		txt.print("05");
		txt.print(StringUtils.extractNumbers(ie), 12);
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);
		txt.print(0, 7);
		txt.print(aaj01.aaj01gia, 2);
		txt.print(referencia.format(yyyyMM));
		txt.print(aaj01.aaj01gia == 2 ? referencia.format(yyyyMMdd) : 0, 6);
		txt.print(tipo, 2);
		
		if((rsTipo10 != null && rsTipo10.size() > 0) || (rsTipo20 != null && rsTipo20.size() > 0) || (lstTipo31 != null && lstTipo31.size() > 0)) {
			txt.print("1");
		}else {
			txt.print("0");
		}
		
		txt.print("0");
		
		def valor = somaSdoCredorAnterior(getCampo("CR05", "sdoCredor"), "001", ano, mes, null);
		txt.print(round((valor * 100), 0), 15);
		
		valor = somaSdoCredorAnterior(getCampo("CR05", "sdoCredor"), "002", ano, mes, aac10.aac10municipio.aag0201uf.aag02id);
		txt.print(round((valor * 100), 0), 15);
		
		txt.print("67919092000189");
		txt.print("0");
		txt.print(round(aac13.aac13icmsEst_Zero, 0), 15);
		txt.print(0, 32);
		txt.print(0, 4);
		txt.print(rsTipo10 != null && rsTipo10.size() > 0 ? rsTipo10.size() : 0, 4);
		txt.print(rsTipo20 != null && rsTipo20.size() > 0 ? rsTipo20.size() : 0, 4);
		txt.print(0, 4);
		txt.print(lstTipo31 != null && lstTipo31.size() > 0 ? lstTipo31.size() : 0, 4);
		txt.newLine();
		
		/**
		 * CR 10 - CFOP's
		 */
		if(rsTipo10 != null && rsTipo10.size() > 0) {
			for(tipo10 in rsTipo10) { 
				def CFOP = tipo10.getString("aaj15codigo");
				
				//CR 14
				def rsTipo14 = buscarDocumentosPorCFOPeUFs(mes, ano, CFOP, getCampo("CR10", "bcIcms"), getCampo("CR10", "icms"), getCampo("CR10", "isentas"), getCampo("CR10", "outras"), getCampo("CR10", "icmsST"), getCampo("CR10", "vlrGIA"))
				def mapR14 = comporRegistroTipo14(aac10, rsTipo14, CFOP, true);
				def mapValores = (TableMap)mapR14.get(CFOP);

				def impRetSubstitutoST = 0;
				def impRetSubstituido = 0;
				
				txt.print("10");
				txt.print(CFOP, 6, '0', false);
				
				if(CFOP.substring(0, 1).equals("2")) {
					txt.print(round((mapValores.getBigDecimal_Zero("vlrContabil1") * 100), 0), 15);
					txt.print(round(mapValores.getBigDecimal_Zero("bcIcms1") * 100, 0), 15);
					txt.print(round(mapValores.getBigDecimal_Zero("imposto") * 100, 0), 15);
					txt.print(round(tipo10.getBigDecimal_Zero("isentas") * 100, 0), 15);
					txt.print(round(mapValores.getBigDecimal_Zero("outras") * 100, 0), 15);
					txt.print(0, 15);
					
					if(CFOP.equals("2410") || CFOP.equals("2411")){
						impRetSubstitutoST = tipo10.getBigDecimal_Zero("icmsST");
						impRetSubstituido = 0;
					}else{
						impRetSubstitutoST = 0;
						impRetSubstituido = mapValores.getBigDecimal_Zero("petroleoEnergia") +  mapValores.getBigDecimal_Zero("outrosProdutos");
					}
					
				}else if(CFOP.substring(0, 1).equals("6")) {
					def totais = (mapValores.getBigDecimal_Zero("vlrContabil1") + mapValores.getBigDecimal_Zero("vlrContabil2"));
					txt.print(round(totais * 100, 0), 15);
					
					totais = (mapValores.getBigDecimal_Zero("bcIcms1") + mapValores.getBigDecimal_Zero("bcIcms2"))
					txt.print(round(totais * 100, 0), 15);
					
					txt.print(round(mapValores.getBigDecimal_Zero("imposto") * 100, 0), 15);
					txt.print(round(tipo10.getBigDecimal_Zero("isentas") * 100, 0), 15);
					txt.print(round(mapValores.getBigDecimal_Zero("outras") * 100, 0), 15);
					txt.print(0, 15);
					impRetSubstitutoST = mapValores.getBigDecimal_Zero("icmsCobradoSt");
					impRetSubstituido = 0;
					
				}else {
					txt.print(round(tipo10.getBigDecimal_Zero("vlrContabil") * 100, 0), 15);
					txt.print(round(tipo10.getBigDecimal_Zero("bcIcms") * 100, 0), 15);
					txt.print(round(tipo10.getBigDecimal_Zero("icms") * 100, 0), 15);
					txt.print(round(tipo10.getBigDecimal_Zero("isentas") * 100, 0), 15);
					txt.print(round(tipo10.getBigDecimal_Zero("outras") * 100, 0), 15);
					txt.print(0, 15);
					
					if(CFOP.substring(0, 1).equals("1") || CFOP.substring(0, 1).equals("3")) {
						if(CFOP.equals("1410") || CFOP.equals("1411")){
							impRetSubstitutoST = tipo10.getBigDecimal_Zero("icmsST");
							impRetSubstituido = 0;
						}else{
							impRetSubstitutoST = 0;
							impRetSubstituido = tipo10.getBigDecimal_Zero("icmsST");
						}
					}else {
						impRetSubstitutoST = tipo10.getBigDecimal_Zero("icmsST");
						impRetSubstituido = 0;
					}
				}
				
				if(verificaCFOP(CFOP)) {
					impRetSubstitutoST = 0;
					impRetSubstituido = 0;
				}
				
				txt.print(round(impRetSubstitutoST * 100, 0), 15);
				txt.print(round(impRetSubstituido * 100, 0), 15);
				txt.print(round(tipo10.getBigDecimal_Zero("vlrGIA") * 100, 0), 15);
				
				mapR14 = comporRegistroTipo14(aac10, rsTipo14, CFOP, false);
				txt.print(mapR14 != null && mapR14.size() > 0 ? mapR14.size() : 0, 4);
				txt.newLine();
				
				
				/**
				 * CR 14
				 */
				if(mapR14 != null && mapR14.size() > 0) {
					TreeSet<Object> setOrdenado = new TreeSet<Object>();
					setOrdenado.addAll(mapR14.keySet());
						
					for(Object key : setOrdenado) {
						txt.print("14");
						txt.print(key, 2);
						
						def tmR14 = new TableMap();
						if(mapR14.get(key) != null) tmR14 = (TableMap)mapR14.get(key);
						
						txt.print(round(tmR14.getBigDecimal_Zero("vlrContabil1") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("bcIcms1") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("vlrContabil2") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("bcIcms2") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("imposto") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("outras") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("icmsCobradoSt") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("petroleoEnergia") * 100, 0), 15);
						txt.print(round(tmR14.getBigDecimal_Zero("outrosProdutos") * 100, 0), 15);
							
						//CR 18
						def rsTipo18 = new ArrayList<TableMap>();
						if(CFOP.substring(0, 1).equals("6")) {
							rsTipo18 = buscarDocSaidasInterestadualALC(tmR14.getLong("aag02id"), ano, mes, CFOP, getCampo("CR18", "codMunicipio"));
							                                          
							txt.print(rsTipo18 != null && rsTipo18.size() > 0 ? "1" : "0");
						}else {
							txt.print("0");
						}
							
						txt.print(rsTipo18 != null && rsTipo18.size() > 0 ? rsTipo18.size() : 0, 4);
						txt.newLine();
							
						/**
						 * CR 18
						 */
						if(rsTipo18 != null && rsTipo18.size() > 0) {
							for(tipo18 in rsTipo18) {
								txt.print("18");
								txt.print(tipo18.getInteger("abb01num"), 9);
								txt.print(tipo18.getDate("abb01data").format(yyyyMMdd));
								txt.print(round(tipo18.getBigDecimal_Zero("total") * 100, 0), 15);
								txt.print(StringUtils.extractNumbers(tipo18.getString("eaa0102ni")), 14);
								txt.print(tipo18.getString("codMunicipio"), 5);
								txt.newLine();
							}
						}
					}
				}
			}
		}
		
		/**
		 * CR 20
		 */
		if(rsTipo20 != null && rsTipo20.size() > 0) {
			for(tipo20 in rsTipo20) {
				def subItem = StringUtils.ajustString(tipo20.getString("edb0101giaSI"), 5, '0', true);
				
				txt.print("20");
				txt.print(subItem, 5);
				txt.print(round(tipo20.getBigDecimal_Zero("edb0101valor") * 100, 0), 15);
				txt.print(tipo20.getString("aaj28codigo"));
				txt.print(subItem.substring(3).equals("99") ? tipo20.getString("edb0101giaFunLegal") : null, 100);
				txt.print(subItem.substring(3).equals("99") ? tipo20.getString("edb0101giaOcor") : null, 300);
				
				def rsTipo25 = new ArrayList<TableMap>();
				def rsTipo26 = new ArrayList<TableMap>();
				def rsTipo27 = new ArrayList<TableMap>();
				
				if(aaj01.aaj01gia != 4) {
					//CR 25
					if(tipo20.getInteger("aaj17gia25") == 1) {
						rsTipo25 = buscarOcorrenciasPorIE(mes, ano, subItem, 2, false);
					}
					//CR 26
					if(tipo20.getInteger("aaj17gia26") == 1) {
						rsTipo26 = buscarOcorrenciasPorIE(mes, ano, subItem, 0, true);
					}
				}
				
				//CR 27
				if(tipo20.getInteger("aaj17gia27") == 1) {
					rsTipo27 = buscarOcorrenciasPorIE(mes, ano, subItem, 1, true);
				}

				txt.print(rsTipo25 != null && rsTipo25.size() > 0 ? rsTipo25.size() : 0, 4);
				txt.print(rsTipo26 != null && rsTipo26.size() > 0 ? rsTipo26.size() : 0, 4);
				txt.print(rsTipo27 != null && rsTipo27.size() > 0 ? rsTipo27.size() : 0, 4);
				txt.print(0, 4);
				txt.newLine();
				
				/**
				 * CR 25
				 */
				if(rsTipo25 != null && rsTipo25.size() > 0) {
					for(tipo25 in rsTipo25) {
						txt.print("25");
						txt.print(StringUtils.extractNumbers(tipo25.getString("eaa0102ie")), 12);
						txt.print(round(tipo25.getBigDecimal_Zero("valor") * 100, 0), 15);
						txt.newLine();
					}
				}
				
				/**
				 * CR 26
				 */
				if(rsTipo26 != null && rsTipo26.size() > 0) {
					for(tipo26 in rsTipo26) {
						txt.print("26");
						txt.print(StringUtils.extractNumbers(tipo26.getString("eaa0102ie")), 12);
						txt.print(subItem.equals("00211") ? 0 : tipo26.getInteger("abb01num"), 9);
						txt.print(referencia.format(yyyyMM));
						txt.print(referencia.format(yyyyMM));
						txt.print(round(tipo26.getBigDecimal_Zero("valor") * 100, 0), 15);
						txt.newLine();
					}
				}
				
				/**
				 * CR 27
				 */
				if(rsTipo27 != null && rsTipo27.size() > 0) {
					for(tipo27 in rsTipo27) {
						txt.print("27");
						txt.print(StringUtils.extractNumbers(tipo27.getString("eaa0102ie")), 12);
						txt.print(subItem.equals("00702") ? 0 : tipo27.getInteger("abb01num"), 9);
						txt.print(round(tipo27.getBigDecimal_Zero("valor") * 100, 0), 15);
						txt.newLine();
					}
				}
			}
		}
		
		/**
		 * CR 31
		 */
		if(lstTipo31 != null && lstTipo31.size() > 0) {
			for(numReg in lstTipo31) {
				txt.print("31");
				txt.print(StringUtils.extractNumbers(numReg), 15, '0', true);
				txt.newLine();
			}
		}
		
		put("dadosArquivo", txt);
	}
	
	private TableMap comporRegistroTipo14(Aac10 aac10, List<TableMap> rsTipo14, String CFOP, boolean agruparByCFOP) {
		def mapValores = new TableMap(); //Key, TableMap com os valores agrupados
		
		if(!aaj01.aaj01gia.equals("4") && (CFOP.substring(0, 1).equals("2") || CFOP.substring(0, 1).equals("6"))) {
			for(tipo14 in rsTipo14) {
				def tm = new TableMap();
				def key = null;
				
				if(agruparByCFOP) { //Por CFOP
					key = CFOP;
					
				}else { //Por UF (Sinief)
					if(tipo14.getString("aag02sinief") == null){
						def aag02 = getSession().get(Aag02.class, tipo14.getLong("aag02id"));
						if(aag02.aag02uf == "EX") continue;
						interromper("O Código SINIEF não foi informado no estado: " + aag02.aag02uf);
					}
					
					key = tipo14.getString("aag02sinief");
				}
				
				if(mapValores.get(key) != null) tm = (TableMap)mapValores.get(key);
				
				tm.put("aag02id", tipo14.getString("aag02id"));
				
				if((tipo14.getInteger("eaa01esMov") == 0 && tipo14.getInteger("eaa01emissao") == 0) || tipo14.getInteger("eaa0102contribIcms") == 1) { 
					def vlrContabil = tm.getBigDecimal_Zero("vlrContabil1") + tipo14.getBigDecimal_Zero("vlrContabil");
					tm.put("vlrContabil1", vlrContabil);
					
					def bcIcms = tm.getBigDecimal_Zero("bcIcms1") + tipo14.getBigDecimal_Zero("bcIcms");
					tm.put("bcIcms1", bcIcms);
					
				}else { 
					if(CFOP.substring(0, 1).equals("6")) {
						def vlrContabil = tm.getBigDecimal_Zero("vlrContabil2") + tipo14.getBigDecimal_Zero("vlrContabil");
						tm.put("vlrContabil2", vlrContabil);
						
						def bcIcms = tm.getBigDecimal_Zero("bcIcms2") + tipo14.getBigDecimal_Zero("bcIcms")
						tm.put("bcIcms2", bcIcms);
					}
				}
				
				def imposto = tm.getBigDecimal_Zero("imposto") + tipo14.getBigDecimal_Zero("icms")
				tm.put("imposto", imposto);
				
				def outras = tm.getBigDecimal_Zero("outras") + tipo14.getBigDecimal_Zero("outras");
				tm.put("outras", outras);
				
				if(CFOP.substring(0, 1).equals("6") && tipo14.getInteger("aaj15st") > 0) {
					def valor = tipo14.getBigDecimal_Zero("icmsST");
					if(CFOP.equals("6109")) valor = 0
					
					tm.put("icmsCobradoSt", tm.getBigDecimal_Zero("icmsCobradoSt") + valor);
				}
				
				if(CFOP.substring(0, 1).equals("2")) {
					if(tipo14.getInteger("aaj15st") == 2) {
						def petroleoEnergia = tm.getBigDecimal_Zero("petroleoEnergia") + tipo14.getBigDecimal_Zero("icmsST")
						tm.put("petroleoEnergia", petroleoEnergia);
					}
					if(tipo14.getInteger("aaj15st") == 1) {
						def outrosProdutos = tm.getBigDecimal_Zero("outrosProdutos") + tipo14.getBigDecimal_Zero("icmsST");
						tm.put("outrosProdutos", outrosProdutos);
					}
				}
				
				mapValores.put(key, tm);
			}
		}
		return mapValores;
	}
	
	private boolean verificaCFOP(String CFOP) {
		if(CFOP.equals("1360") || (CFOP.compareTo("1401") >= 0 && CFOP.compareTo("1449") <= 0) ||
		  (CFOP.compareTo("1651") >= 0 && CFOP.compareTo("1699") <= 0) ||
		  (CFOP.substring(0, 2).equals("19")) ||
		  (CFOP.compareTo("2401") >= 0 && CFOP.compareTo("2449") <= 0) ||
		  (CFOP.compareTo("2651") >= 0 && CFOP.compareTo("2699") <= 0) ||
		  (CFOP.substring(0, 2).equals("29")) ||
		  (CFOP.equals("5360")) ||
		  (CFOP.compareTo("5401") >= 0 && CFOP.compareTo("5449") <= 0) ||
		  (CFOP.compareTo("5651") >= 0 && CFOP.compareTo("5699") <= 0) ||
		  (CFOP.substring(0, 2).equals("59")) ||
		  (CFOP.equals("6360")) ||
		  (CFOP.compareTo("6401") >= 0 && CFOP.compareTo("6449") <= 0) ||
		  (CFOP.compareTo("6651") >= 0 && CFOP.compareTo("6699") <= 0) ||
		  (CFOP.substring(0, 2).equals("69"))) {
			return false;
		}
		
		return true;
	}
	
	private List<TableMap> buscarDocumentosPorCFOP(Integer mes, Integer ano, String bcIcms, String icms, String isentas, String outras, String icmsST, String vlrGIA) {
		def str = new StringBuilder(", ");
		str.append("SUM(eaa0103totDoc) as vlrContabil");
		str.append(", SUM(jGet(eaa0103json." + bcIcms + ")::numeric) AS bcIcms");
		str.append(", SUM(jGet(eaa0103json." + icms + ")::numeric) AS icms");
		str.append(", SUM(jGet(eaa0103json." + isentas + ")::numeric) AS isentas");
		str.append(", SUM(jGet(eaa0103json." + outras + ")::numeric) AS outras");
		str.append(", SUM(jGet(eaa0103json." + icmsST + ")::numeric) AS icmsST");
		str.append(", SUM(jGet(eaa0103json." + vlrGIA + ")::numeric) AS vlrGIA");
		
		def numMesEmis = Fields.numMeses(Fields.month("abb01data").toString(false), Fields.year("abb01data").toString(false));
		def numMesES = Fields.numMeses(Fields.month("eaa01esData").toString(false), Fields.year("eaa01esData").toString(false));
		
		def sql = " SELECT aaj15codigo" + str + " FROM Eaa0103 " +
		          " INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE aaj15codigo NOT IN (:cfop) " +
				  " AND ((eaa01esMov = 0 AND " + numMesES + " = :numMeses) OR (eaa01esMov = 1 AND " + numMesEmis + " = :numMeses))" +
				  " AND (eaa0103totDoc > 0 " +
				  " OR jGet(eaa0103json." + bcIcms + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + icms + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + isentas + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + outras + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + icmsST + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + vlrGIA + ")::numeric > 0) " +
				  " AND eaa01cancData IS NULL " + obterWherePadrao("Eaa01") +
				  " GROUP BY aaj15codigo ORDER BY aaj15codigo";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)), Parametro.criar("cfop", Utils.list("1605", "5605")));
	}
	
	private List<TableMap> buscarOcorrenciasPorMesAno(Integer mes, Integer ano) {
		def numMes = Fields.numMeses("edb01mes", "edb01ano").toString(false);
		
		def sql = " SELECT edb0101giaSI, SUM(edb0101valor) as edb0101valor, aaj28codigo, edb0101giaFunLegal, edb0101giaOcor, aaj17gia25, aaj17gia26, aaj17gia27, aaj17gia28 " +
				  " FROM Edb01 " +
				  " INNER JOIN Aaj28 ON aaj28id = edb01tipo " +
				  " INNER JOIN Edb0101 ON edb0101apur = edb01id " +
				  " INNER JOIN Aaj17 ON aaj17id = edb0101ajuste " +
				  " WHERE " + numMes + " = :numMeses " +
				  " AND aaj28codigo = :aaj28codigo " +
				    obterWherePadrao("Edb01") +
				  " GROUP BY edb0101giaSI, aaj28codigo, edb0101giaFunLegal, edb0101giaOcor, aaj17gia25, aaj17gia26, aaj17gia27, aaj17gia28 " +
				  " ORDER BY edb0101giaSI";
			  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)), Parametro.criar("aaj28codigo", "002"));
	}
	
	private List<String> buscarNumRegExportacoes(Integer mes, Integer ano) {
		def docMes = Fields.month("eaa0104dtAverb").toString(false);
		def docAno = Fields.year("eaa0104dtAverb").toString(false);
		def numMes = Fields.numMeses(docMes, docAno).toString(false);
		
		def sql = " SELECT eaa01041num FROM Eaa01041 " +
				  " INNER JOIN Eaa0104 ON eaa0104id = eaa01041de " +
				  " INNER JOIN Eaa01 ON eaa01id = eaa0104doc " +
				  " WHERE " + numMes + " = :numMeses " +
				  " AND eaa01cancData IS NULL " +
				  " AND eaa01041num IS NOT NULL " +
				    obterWherePadrao("Eaa01") +
				  " GROUP BY eaa01041num ORDER BY eaa01041num";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)));
	}
	
	private BigDecimal somaSdoCredorAnterior(String campo, String tipoApur, Integer ano, Integer mes, Long aag02id) {
		String whereUF = aag02id == null ? "" : " AND edb01uf = :aag02id ";
		
		def sql = " SELECT COALESCE(SUM(jGet(edb01json." + campo + ")::numeric), 0) FROM Edb01 " +
				  " INNER JOIN Aaj28 ON aaj28id = edb01tipo " +
				  " WHERE aaj28codigo = :tipo " +
				  " AND edb01ano = :ano " +
				  " AND edb01mes = :mes "
				    whereUF + obterWherePadrao("Edb01");
					
		def query = getSession().createQuery(sql)
								.setParameter("tipo", tipoApur)
								.setParameter("ano", ano)
								.setParameter("mes", mes);
		
		if(aag02id != null) query.setParameter("aag02id", aag02id);
			
		return query.getUniqueResult(ColumnType.BIG_DECIMAL);
	}
	
	private List<TableMap> buscarDocumentosPorCFOPeUFs(Integer mes, Integer ano, String cfop, String bcIcms, String icms, String isentas, String outras, String icmsST, String vlrGIA) {
		def str = new StringBuilder(", ");
		str.append("SUM(eaa0103totDoc) as vlrContabil");
		str.append(", SUM(jGet(eaa0103json." + bcIcms + ")::numeric) AS bcIcms");
		str.append(", SUM(jGet(eaa0103json." + icms + ")::numeric) AS icms");
		str.append(", SUM(jGet(eaa0103json." + isentas + ")::numeric) AS isentas");
		str.append(", SUM(jGet(eaa0103json." + outras + ")::numeric) AS outras");
		str.append(", SUM(jGet(eaa0103json." + icmsST + ")::numeric) AS icmsST");
		str.append(", SUM(jGet(eaa0103json." + vlrGIA + ")::numeric) AS vlrGIA");
		
		def numMesEmis = Fields.numMeses(Fields.month("abb01data").toString(false), Fields.year("abb01data").toString(false));
		def numMesES = Fields.numMeses(Fields.month("eaa01esData").toString(false), Fields.year("eaa01esData").toString(false));
		
		def sql = " SELECT aag02id, aag02uf, aag02sinief, eaa01esMov, eaa01emissao, eaa0102contribIcms, aaj15st" + str + " FROM Eaa0103 " +
		          " INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
		 		  " INNER JOIN Abb01 ON abb01id = eaa01central " +
		 		  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0101principal = 1 " +
				  " AND ((eaa01esMov = 0 AND " + numMesES + " = :numMeses) OR (eaa01esMov = 1 AND " + numMesEmis + " = :numMeses)) " +
				  " AND eaa01cancData IS NULL " +
				  " AND (eaa0103totDoc > 0 " +
				  " OR jGet(eaa0103json." + bcIcms + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + icms + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + isentas + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + outras + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + icmsST + ")::numeric > 0 " +
				  " OR jGet(eaa0103json." + vlrGIA + ")::numeric > 0) " +
				  " AND aaj15codigo = :cfop " + obterWherePadrao("Eaa01") +
				  " GROUP BY aag02id, aag02uf, aag02sinief, eaa01esMov, eaa01emissao, eaa0102contribIcms, aaj15st " +
				  " ORDER BY aag02sinief, eaa0102contribIcms, aaj15st";
				  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)), Parametro.criar("cfop", cfop));
	}
	
	private List<TableMap> buscarDocSaidasInterestadualALC(Long aag02id, Integer ano, Integer mes, String cfop, String cpoCodMunicipio) {
		def docMes = Fields.month("abb01data").toString(false);
		def docAno = Fields.year("abb01data").toString(false);
		def numMes = Fields.numMeses(docMes, docAno).toString(false);
		
		def sql = " SELECT abb01num, abb01data, SUM(eaa0103totDoc) AS total, eaa0102ni, jGet(aag0201json." + cpoCodMunicipio + ") as codMunicipio " +
  				  " FROM Eaa0103 " +
				  " INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE " + numMes + " = :numMeses " +
				  " AND eaa01cancData IS NULL " +
				  " AND eaa0102alc > 0 AND aaj15codigo = :cfop " +
				  " AND aag02id = :aag02id AND eaa0101principal = 1 " +
				    obterWherePadrao("Eaa01") + 
				  " GROUP BY abb01num, abb01data, eaa0102ni, jGet(aag0201json." + cpoCodMunicipio + ")" + 
				  " ORDER BY abb01data, abb01num";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)), Parametro.criar("cfop", cfop), Parametro.criar("aag02id", aag02id));
	}
	
	private List<TableMap> buscarOcorrenciasPorIE(Integer mes, Integer ano, String subItem, int tipoIcms, boolean considNumDoc) { 
		def where = tipoIcms == 0 ? " AND aaj28codigo = '002' " : tipoIcms == 1 ? " AND aaj28codigo = '001' " : "";
		def campo = considNumDoc ? ", abb01num " : "";
		
		def numMesEmis = Fields.numMeses(Fields.month("abb01data").toString(false), Fields.year("abb01data").toString(false));
		def numMesES = Fields.numMeses(Fields.month("eaa01esData").toString(false), Fields.year("eaa01esData").toString(false));
		
		def sql = " SELECT eaa0102ie" + campo + ", SUM(eaa01035valor) as valor " +
				  " FROM Eaa01 "+
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Ea0102 ON eaa0102doc = eaa01id " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01035 ON eaa01035item = eaa0103id " +
				  " INNER JOIN Aaj17 ON aaj17id = eaa01035ajuste " +
				  " INNER JOIN Aaj28 ON aaj28id = aaj17tipo " +
				  " WHERE ((eaa01esMov = 0 AND " + numMesES + " = :numMeses) OR (eaa01esMov = 1 AND " + numMesEmis + " = :numMeses)) " +
				  " AND eaa01cancData IS NULL " +
				  " AND aaj17giaSI = :subItem " + 
				    where + obterWherePadrao("Eaa01") +
				  " GROUP BY eaa0102ie " + campo +
				  " ORDER BY eaa0102ie";
				  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)), Parametro.criar("subItem", subItem));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDgifQ==