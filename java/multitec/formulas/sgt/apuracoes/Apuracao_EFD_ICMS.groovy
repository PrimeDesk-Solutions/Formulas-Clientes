package multitec.formulas.sgt.apuracoes;

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaj17
import sam.model.entities.ea.Eaa01
import sam.model.entities.ed.Edb01
import sam.model.entities.ed.Edb0101
import sam.server.samdev.formula.FormulaBase

class Apuracao_EFD_ICMS extends FormulaBase {
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_APURACAO;
	}

	@Override
	public void executar() {
		Edb01 edb01 = get("edb01");
		
		TableMap edb01json;
		if (edb01.edb01json != null) {
			edb01json = edb01.edb01json;
		} else {
			edb01json = new TableMap();
		}
		
		selecionarAlinhamento("0030");
		
		String cpoICMS = getCampo("0", "icms_efd");
		
		LocalDate dtInicial = LocalDate.of(edb01.edb01ano, edb01.edb01mes, 1);
		LocalDate dtFinal = LocalDate.of(edb01.edb01ano, edb01.edb01mes, dtInicial.lengthOfMonth());
		
		/**
		 * 01 - Saídas com débito do imposto
		 */
		def debSaidas = 0;
		for(int reg = 0; reg < 8; reg++) { //8 corresponde ao número de grupos que entram na composição do valor
			String registro; //0-C190 1-C320 2-C390 3-C490 4-C590 5-D190 6-D590 7-C850/C890
			switch (reg) {
				case 0: registro = "C190"; break;
				case 1: registro = "C320"; break;
				case 2: registro = "C390"; break;
				case 3: registro = "C490"; break;
				case 4: registro = "C590"; break;
				case 5: registro = "D190"; break;
				case 6: registro = "D590"; break;
				case 7: registro = "C850"; break;
				default: registro = null; break;
			}
			
			/** Entradas */
			def entradas = buscarDebitosICMS(cpoICMS, dtInicial, dtFinal, true, reg);
			debSaidas = debSaidas + entradas;
			
			/** Saídas */
			def saidas = buscarDebitosICMS(cpoICMS, dtInicial, dtFinal, false, reg);
			debSaidas = debSaidas + saidas;
		}
		edb01json.put(getCampo("0", "debSaidas"), debSaidas);
		
		/**
		 * 02 - Ajustes de débito1
		 */
		def debAjustes = buscarAjustesDebDoctsICMS(dtInicial, dtFinal);
		edb01json.put(getCampo("0", "debAjustes"), debAjustes);
		
		/**
		 * 03 - Ajustes de débito2
		 */
		def debAjustesApur = buscarAjustesDebApurICMS(dtInicial, dtFinal);
		if(edb01 != null) {
			if (edb01.edb0101s != null && edb01.edb0101s.size() > 0) {
				for(Edb0101 edb0101 : edb01.edb0101s) {
					Aaj17 aaj17 = edb0101.edb0101ajuste;
					if(aaj17 != null && aaj17.aaj17codigo.length() > 4 && aaj17.aaj17codigo.substring(2, 4).equals("00")) {
						debAjustesApur = debAjustesApur + edb0101.edb0101valor;
					}
				}
			}
		}
		edb01json.put(getCampo("0", "debAjustesApur"), debAjustesApur);
		
		/**
		 * 04 - Estorno de créditos
		 */
		def estornoCred = buscarEstornoCredICMS(dtInicial, dtFinal);
		if(edb01 != null) {
			if (edb01.edb0101s != null && edb01.edb0101s.size() > 0) {
				for(Edb0101 edb0101 : edb01.edb0101s) {
					Aaj17 aaj17 = edb0101.edb0101ajuste;
					if(aaj17 != null && aaj17.aaj17codigo.length() > 4 && aaj17.aaj17codigo.substring(2, 4).equals("01")) {
						debAjustesApur = debAjustesApur + edb0101.edb0101valor;
					}
				}
			}
		}
		edb01json.put(getCampo("0", "estornoCred"), estornoCred);
		
		/**
		 * 05 - Entradas com crédito do imposto
		 */
		def credEntradas = 0;
		for(int reg = 0; reg < 4; reg++) { //4 corresponde ao número de grupos que entram na composição do valor
			String registro; //0-C190 1-C590 5-D190 6-D590
			switch (reg) {
				case 0: registro = "C190"; break;
				case 1: registro = "C590"; break;
				case 2: registro = "D190"; break;
				case 3: registro = "D590"; break;
				default: registro = null; break;
			}
			
			/** Entradas */
			def entradas = buscarCreditosICMS(cpoICMS, dtInicial, dtFinal, true, reg);
			credEntradas = credEntradas + entradas;
			
			/** Saídas */
			def saidas = buscarCreditosICMS(cpoICMS, dtInicial, dtFinal, false, reg);
			credEntradas = credEntradas + saidas;
		}
		edb01json.put(getCampo("0", "credEntradas"), credEntradas);
		
		/**
		 * 06 - Ajustes à crédito1
		 */
		def credAjustes = buscarAjustesCredDoctsICMS(dtInicial, dtFinal);
		edb01json.put(getCampo("0", "credAjustes"), credAjustes);
		
		/**
		 * 07 - Ajustes à crédito2
		 */
		def credAjustesApur = buscarAjustesCredApurICMS(dtInicial, dtFinal);
		if(edb01 != null) {
			if (edb01.edb0101s != null && edb01.edb0101s.size() > 0) {
				for(Edb0101 edb0101 : edb01.edb0101s) {
					Aaj17 aaj17 = edb0101.edb0101ajuste;
					if(aaj17 != null && aaj17.aaj17codigo.length() > 4 && aaj17.aaj17codigo.substring(2, 4).equals("02")) {
						credAjustesApur = credAjustesApur + edb0101.edb0101valor;
					}
				}
			}
		}
		edb01json.put(getCampo("0", "credAjustesApur"), credAjustesApur);
		
		/**
		 * 08 - Estorno de débitos
		 */
		def estornoDeb = buscarEstornoDebICMS(dtInicial, dtFinal);
		if(edb01 != null) {
			if (edb01.edb0101s != null && edb01.edb0101s.size() > 0) {
				for(Edb0101 edb0101 : edb01.edb0101s) {
					Aaj17 aaj17 = edb0101.edb0101ajuste;
					if(aaj17 != null && aaj17.aaj17codigo.length() > 4 && aaj17.aaj17codigo.substring(2, 4).equals("03")) {
						estornoDeb = estornoDeb + edb0101.edb0101valor;
					}
				}
			}
		}
		edb01json.put(getCampo("0", "estornoDeb"), estornoDeb);
		
		/**
		 * 09 - Saldo credor anterior
		 */
		def saldoCredorAnt = buscarSaldoCredorAnterior(getCampo("0", "saldoCredorAnt"), edb01.edb01ano, edb01.edb01mes, edb01.edb01tipo.aaj28id);
		edb01json.put(getCampo("0", "saldoCredorAnt"), saldoCredorAnt);
		
		/**
		 * 10 - Saldo devedor antes das deduções
		 */
		def saldo1 = debSaidas + debAjustes + debAjustesApur + estornoDeb;
		def saldo2 = credEntradas + credAjustes + credAjustesApur + estornoCred + saldoCredorAnt;
		def saldo = saldo1 - saldo2;
		
		edb01json.put(getCampo("0", "saldo"), saldo);

		/**
		 * 11 - Deduções
		 */
		def deducoes = 0;
		def deducoesAjustesFiscais = buscarDeducoesAjustesFiscais(dtInicial, dtFinal);
		deducoes = deducoes + deducoesAjustesFiscais;
		
		def deducoesAjustesApuracao = buscarDeducoesAjustesApuracao(dtInicial, dtFinal);
		deducoes = deducoes + deducoesAjustesApuracao;
		if(edb01 != null) {
			if (edb01.edb0101s != null && edb01.edb0101s.size() > 0) {
				for(Edb0101 edb0101 : edb01.edb0101s) {
					Aaj17 aaj17 = edb0101.edb0101ajuste;
					if(aaj17 != null && aaj17.aaj17codigo.length() > 4 && aaj17.aaj17codigo.substring(2, 4).equals("04")) {
						deducoes = deducoes + edb0101.edb0101valor;
					}
				}
			}
		}
		edb01json.put(getCampo("0", "deducoes"), deducoes);
		
		/**
		 * 12 - Saldo devedor depois das deduções
		 */
		def saldoDevedor = 0;
		saldo = saldo - deducoes;
		if (saldo >= 0) saldoDevedor = saldo;
		edb01json.put(getCampo("0", "saldoDevedor"), saldoDevedor);
		
		/**
		 * 13 - Saldo credor a transportar
		 */
		def saldoCredor = 0;
		if (saldo < 0) saldoCredor = saldo.abs();
		edb01json.put(getCampo("0", "saldoCredor"), saldoCredor);
		
		/**
		 * 14 - Valores extra-apuração
		 */
		def valoresExtra = 0;
		for(int reg = 0; reg < 7; reg++) { //7 corresponde ao número de grupos que entram na composição do valor
			String registro; //0-C190 1-C320 2-C390 3-C490 4-C590 5-D190 6-D590
			switch (reg) {
				case 0: registro = "C100"; break;
				case 1: registro = "C300"; break;
				case 2: registro = "C400"; break;
				case 3: registro = "C500"; break;
				case 4: registro = "D100"; break;
				case 5: registro = "D500"; break;
				case 6: registro = "D590"; break;
				default: registro = null; break;
			}
			
			def valor = buscarValorExtraApur(cpoICMS, dtInicial, dtFinal, reg);
			valoresExtra = valoresExtra + valor;
		}
		
		def valorExtra = buscarValorExtraApurICMS(dtInicial, dtFinal);
		valoresExtra = valoresExtra + valorExtra;
		
		def valorExtraExtorno = buscarValorExtraApurICMSEstorno(dtInicial, dtFinal);
		valoresExtra = valoresExtra + valorExtraExtorno;
		
		if(edb01 != null) {
			if (edb01.edb0101s != null && edb01.edb0101s.size() > 0) {
				for(Edb0101 edb0101 : edb01.edb0101s) {
					Aaj17 aaj17 = edb0101.edb0101ajuste;
					if(aaj17 != null && aaj17.aaj17codigo.length() > 4 && aaj17.aaj17codigo.substring(2, 4).equals("05")) {
						valoresExtra = valoresExtra + edb0101.edb0101valor;
					}
				}
			}
		}
		edb01json.put(getCampo("0", "valoresExtra"), valoresExtra);
		
		edb01.edb01json = edb01json;
		put("edb01", edb01);
	}
	
	private BigDecimal buscarDebitosICMS(String nomeCampo, LocalDate dtInicial, LocalDate dtFinal, boolean isEntrada, int reg) {
		String whereES = isEntrada ? "(eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal AND aaj03codigo = :cfop) " :
				"(eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal AND aaj03codigo <> :cfop) ";

		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE " + whereES + " AND jGet(eaa0103json." + nomeCampo + ")::numeric > 0 " +
					 " AND aaj03efd NOT IN ('01', '02', '03', '04', '05', '07')" +
					 " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN (:modelos) " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getSession().createQuery(sql)
					 .setParameters("cfop", isEntrada ? "1605" : "5605", "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1, "modelos", buscarModelosDeb(reg))
					 .getUniqueResult(ColumnType.BIG_DECIMAL);
				
        return result == null ? 0 : result;
	}
	
	private BigDecimal buscarCreditosICMS(String nomeCampo, LocalDate dtInicial, LocalDate dtFinal, boolean isEntrada, int reg) {
		String whereES = isEntrada ? "(eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal AND aaj03codigo <> :cfop) " :
				"(eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal AND aaj03codigo = :cfop) ";

		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE " + whereES + " AND jGet(eaa0103json." + nomeCampo + ")::numeric > 0 " +
					 " AND aaj03efd NOT IN ('01', '02', '03', '04', '05', '07')" +
					 " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN (:modelos) " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getSession().createQuery(sql)
					.setParameters("cfop", isEntrada ? "1605" : "5605", "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1, "modelos", buscarModelosCred(reg))
					.getUniqueResult(ColumnType.BIG_DECIMAL);
				
        return result == null ? 0 : result;
	}
	
	private BigDecimal buscarAjustesDebDoctsICMS(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('01', '02', '03', '04', '05', '07')" +
					 " AND eaa01iEfdIcms = :eaa01iEfdIcms " +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("3", "4", "5"), "qua", Utils.list("0", "3", "4", "5", "6", "7", "8"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarAjustesCredDoctsICMS(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('01', '02', '03', '04', '05', '07')" +
					 " AND eaa01iEfdIcms = :eaa01iEfdIcms " +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("0", "1", "2"), "qua", Utils.list("0", "3", "4", "5"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarAjustesDebApurICMS(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63', " + 
					 " '02', '2D', '60', '06', '66', '29', '28', '21', '22') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("0"), "qua", Utils.list("0"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarAjustesCredApurICMS(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '06', '66', '29', '28', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63', " + 
					 " '21', '22') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("0"), "qua", Utils.list("2"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarEstornoCredICMS(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63', " + 
					 " '02', '2D', '60', '06', '66', '29', '28', '21', '22') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("0"), "qua", Utils.list("1"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
					
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarEstornoDebICMS(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '06', '66', '29', '28', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63', " + 
					 " '21', '22') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("0"), "qua", Utils.list("3"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarSaldoCredorAnterior(String nomeCampo, Integer ano, Integer mes, Long aaj28id) {
		String sql = " SELECT jGet(edb01json." + nomeCampo + ") AS valor " +
		             " FROM Edb01 " +
					 " WHERE edb01tipo = :tipo AND " + Fields.numMeses("edb01mes", "edb01ano") + " < :numMeses " +
					 getSamWhere().getWherePadrao("AND", Edb01.class) + 
					 " ORDER BY edb01ano DESC, edb01mes DESC";

        int numMeses = (ano * 12) + mes;
		def valor = getSession().createQuery(sql).setParameters("numMeses", numMeses, "tipo", aaj28id).setMaxResult(1).getUniqueResult(ColumnType.BIG_DECIMAL);
		return valor == null ? 0 : valor;
	}
	
	private BigDecimal buscarDeducoesAjustesFiscais(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('01', '02', '03', '04', '05', '07')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '02', '2D', '60', '06', '66', '29', '28', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63', '21', '22') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("6"), "qua", Utils.list("0"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarDeducoesAjustesApuracao(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("0"), "qua", Utils.list("4"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarValorExtraApur(String nomeCampo, LocalDate dtInicial, LocalDate dtFinal, int reg) {
		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) AND jGet(eaa0103json." + nomeCampo + ")::numeric > 0 " +
					 " AND aaj03efd IN ('01', '07')" +
					 " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN (:modelos) " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getSession().createQuery(sql)
					.setParameters("dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1, "modelos", buscarModelosExtraApur(reg))
					.getUniqueResult(ColumnType.BIG_DECIMAL);
				
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarValorExtraApurICMS(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					.setParameters("ter", Utils.list("7"), "qua", Utils.list("0", "2"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);
					
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarValorExtraApurICMSEstorno(LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(eaa01035valor) AS valor" +
					 " FROM Eaa01 INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aah01 ON aah01id = abb01tipo " +
					 " INNER JOIN Eaa01035 ON eaa01035item = eaa0103item " +
					 " LEFT JOIN Aaj17 ON eaa01035ajuste = aaj17id " +
					 " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					 " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					 " AND SUBSTR(aaj17codigo, 3, 1) IN (:ter) " +
					 " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
					 " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					 " AND  eaa01iEfdIcms = :eaa01iEfdIcms" +
					 " AND aah01modelo IN ('01', '1B', '04', '55', '65', '07', '08', '8B', '09', '10', '11', '26', '27', '57', '67', '63', '02', '2D', '60', '06', '66', '29', '28', '21', '22') " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getSession().createQuery(sql)
					 .setParameters("ter", Utils.list("0"), "qua", Utils.list("5"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1)
					 .getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return result == null ? 0 : result;
	}
	
	private List<String> buscarModelosDeb(int reg) {
		if (reg == 0) return Utils.list("01", "1B", "04", "55", "65");
		if (reg == 1) return Utils.list("02");
		if (reg == 2) return Utils.list("02");
		if (reg == 3) return Utils.list("02", "2D", "60");
		if (reg == 4) return Utils.list("06", "66", "29", "28");
		if (reg == 5) return Utils.list("07", "08", "8B", "09", "10", "11", "26", "27", "57", "63", "67");
		if (reg == 6) return Utils.list("21", "22");
		if (reg == 7) return Utils.list("59");
	}
	
	private List<String> buscarModelosCred(int reg) {
		switch (reg) { //0-C190 1-C590 2-D190 3-D590
			case 0: return Utils.list("01", "1B", "04", "55", "65");
			case 1: return Utils.list("06", "66", "29", "28");
			case 2: return Utils.list("07", "08", "8B", "09", "10", "11", "26", "27", "57", "63", "67");
			case 3: return Utils.list("21", "22");
			default: return null;
		}
	}
	
	private List<String> buscarModelosExtraApur(int reg) {
		switch (reg) { //0-C190 1-C320 2-C390 3-C490 4-C590 5-D190 6-D590
			case 0: return Utils.list("01", "1B", "04", "55", "65");
			case 1: return Utils.list("02");
			case 2: return Utils.list("02");
			case 3: return Utils.list("02", "2D", "60");
			case 4: return Utils.list("06", "66", "29", "28");
			case 5: return Utils.list("07", "08", "8B", "09", "10", "11", "26", "27", "57", "63", "67");
			case 6: return Utils.list("21", "22");
			default: return null;
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDEifQ==