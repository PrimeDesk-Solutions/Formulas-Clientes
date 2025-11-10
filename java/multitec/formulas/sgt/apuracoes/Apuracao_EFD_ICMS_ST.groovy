package multitec.formulas.sgt.apuracoes

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.ed.Edb01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class Apuracao_EFD_ICMS_ST extends FormulaBase {
	private String campo01;
	private String campo02;
	private String campo03;
	private String campo04;
	private String campo05;
	private String campo06;
	private String campo07;
	private String campo08;
	private String campo09;
	private String campo10;
	private String campo11;
	private String campo12;
	private String campo13;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_APURACAO
	}

	@Override
	public void executar() {
		Edb01 edb01Retorno = null;
		
		Edb01 edb01 = get("edb01")

		selecionarAlinhamento("0033")
		
		def dtInicial = LocalDate.of(edb01.edb01ano, edb01.edb01mes, 1)
		def dtFinal = LocalDate.of(edb01.edb01ano, edb01.edb01mes, dtInicial.lengthOfMonth())

		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		def ufEmpresa = aac10.aac10municipio.aag0201uf.aag02uf
		
		String cpoIcmsSt = getCampo("0", "icmsST_efd");

		List<Aag02> aag02s = new ArrayList<>()
		if (edb01 != null && edb01.edb01uf != null) {
			aag02s.add(edb01.edb01uf);
			
			excluirApuracoes(edb01.edb01mes, edb01.edb01ano, edb01.edb01tipo.aaj28id, edb01.edb01uf.aag02id);
		} else {
			aag02s.addAll(buscarEstados());
			
			excluirApuracoes(edb01.edb01mes, edb01.edb01ano, edb01.edb01tipo.aaj28id, null);
		}

		for(aag02 in aag02s) {
			if(aag02.aag02uf.equalsIgnoreCase("EX")) continue
			Edb01 edb01IcmsSt = new Edb01();
			edb01IcmsSt.edb01ano = edb01.edb01ano;
			edb01IcmsSt.edb01mes = edb01.edb01mes;
			edb01IcmsSt.edb01tipo = edb01.edb01tipo;
			edb01IcmsSt.edb01uf = aag02;

			def isUFDaEmpresa = aag02.aag02uf.equals(ufEmpresa);

			/**
			 * 01 - Saldo credor anterior
			 */
			def credAnt = 0
			Edb01 edb01Anterior = buscarApuracaoAnterior(aag02.aag02id, edb01IcmsSt)
			if(edb01Anterior != null) {
				credAnt = edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "credAnt"));
			}

			/**
			 * 02 - ICMS ST de devolução
			 */
			def devolucao = buscarDevolucoesRessarcimentosIcmsST(true, aag02.aag02id, cpoIcmsSt, dtInicial, dtFinal)

			/**
			 * 03 - ICMS ST de ressarcimentos
			 */
			def ressarcimento = buscarDevolucoesRessarcimentosIcmsST(false, aag02.aag02id, cpoIcmsSt, dtInicial, dtFinal)

			/**
			 * 04 - Outros créditos e estornos de débitos
			 */
			def outrosCredEaa01035 = buscarOutrosCredEaa01035IcmsSt(Utils.list("01", "1B", "04", "06", "21", "22", "28", "29", "55", "65", "66"), aag02.aag02id, dtInicial, dtFinal);
			def outrosCredIcmsST = buscarOutrosCredIcmsST(Utils.list("01", "1B", "04", "55", "65"), aag02.aag02id, cpoIcmsSt, dtInicial, dtFinal);
			def outrosCred = outrosCredEaa01035 + outrosCredIcmsST;
			
			/**
			 * 05 - edb01ajustescred: Ajustes a crédito
			 */
			def ajustesCred = buscarAjustesCredEaa01031IcmsSt(Utils.list("01", "1B", "04", "55", "65"), aag02.aag02id, dtInicial, dtFinal);

			/**
			 * 06 - edb01retencao: ICMS retido por ST
			 */
			def retencao = 0;
			for(int reg = 0; reg < 4; reg++) {
				def ab09reg;
				def modelos;
				switch (reg) {
					case 0: 
						ab09reg = "C190"
						modelos = Utils.list("01", "1B", "04", "55", "65")
						break
					case 1: 
						ab09reg = "C590"
						modelos = Utils.list("06", "66", "29", "28")
						break
					case 2: 
						ab09reg = "D190"
						modelos = Utils.list("07", "08", "8B", "09", "10", "11", "26", "27", "57", "63", "67")
						break
					case 3: 
						ab09reg = "D590"
						modelos = Utils.list("21", "22")
						break
				}

				def valor = buscarRetencaoIcmsSt(modelos, aag02.aag02id,cpoIcmsSt, dtInicial, dtFinal)
				retencao = retencao + valor;
			}


			/**
			 * 07 - edb01outrosDeb: Outros débitos e estornos de créditos  (EFD -> campo 09)
			 */
			def outrosDeb = buscarOutrosDebEaa01035IcmsSt(Utils.list("01", "1B", "04", "06", "28", "29", "55", "65", "66"), aag02.aag02id, dtInicial, dtFinal);

			/**
			 * 08 - edb01ajustesDeb: Ajustes a débito lançados na tabela Ea0123  (EFD -> campo 10)
			 */
			def ajustesDeb = buscarAjustesDebEaa01031IcmsSt(Utils.list("01", "1B", "04", "55", "65"), aag02.aag02id, dtInicial, dtFinal);

			/**
			 * 09 - edb01saldo: Saldo devedor antes das deduções  (EFD -> campo 11)
			 */
			def saldo = 0;

			def valor1 = retencao + outrosDeb + ajustesDeb;
			def valor2 = credAnt + devolucao + ressarcimento + outrosCred + ajustesCred;
			def total = valor1 - valor2;

			if(total >= 0) saldo = total;


			/**
			 * 10 - edb01deducoes: Deduções  (EFD -> campo 12)
			 */
			def deducoesEaa01031 = buscarDeducoesEaa01031IcmsSt(Utils.list("01", "1B", "04", "55", "65"), aag02.aag02id, dtInicial, dtFinal)
			def deducoesEaa01035 = buscarDeducoesEaa01035IcmsSt(Utils.list("01", "1B", "04", "06", "28", "29", "55", "65", "66"), aag02.aag02id, dtInicial, dtFinal)
			def deducoes = deducoesEaa01031 + deducoesEaa01035;


			/**
			 * 11 - edb01saldoDevedor: Total do ICMS ST a recolher  (EFD -> campo 13)
			 */
			def saldoDevedor = saldo - deducoes;


			/**
			 * 12 - edb01saldocredor: Saldo credor a transportar  (EFD -> campo 14)
			 */
			def saldoCredor = 0;
			if(total < 0) saldoCredor = total * (-1);

			/**
			 * 13 - edb01extra: Valores extra-apuração  (EFD -> campo 15)
			 */
			def extra = 0
			for(int reg = 0; reg < 3; reg++) {
				def ab09reg;
				def modelos;
				switch (reg) {
					case 0: 
						ab09reg = "C100"
						modelos = Utils.list("01", "1B", "04", "55", "65")
						break
					case 1: 
						ab09reg = "C500"
						modelos = Utils.list("06", "66", "29", "28")
						break
					case 2: 
						ab09reg = "D500"
						modelos = Utils.list("21", "22")
						break
				}

				def valor = buscarExtraApurC100IcmsSt(modelos, aag02.aag02id, cpoIcmsSt, dtInicial, dtFinal)
				extra = extra + valor;
			}

			def extraEaa01031 = buscarExtraApurEaa01031IcmsSt(Utils.list("01", "1B", "04", "55", "65"), aag02.aag02id, dtInicial, dtFinal);
			extra = extra + extraEaa01031;

			def extraEaa01035 = buscarExtraApurEaa01035IcmsSt(Utils.list("01", "1B", "04", "06", "28", "29", "55", "65", "66"), aag02.aag02id, dtInicial, dtFinal);
			extra = extra + extraEaa01035;

			if (isUFDaEmpresa || (credAnt != 0 || devolucao != 0 || ressarcimento != 0 || outrosCred != 0 || ajustesCred != 0 || retencao != 0 || 
				outrosDeb != 0 || ajustesDeb != 0 || saldo != 0 || deducoes != 0 || saldoDevedor != 0 || saldoCredor != 0 || extra != 0)) {

				edb01IcmsSt.edb01json = edb01IcmsSt.edb01json ?: new TableMap();
			
				edb01IcmsSt.edb01json.put(getCampo("0", "credAnt"), credAnt);
				edb01IcmsSt.edb01json.put(getCampo("0", "devolucao"), devolucao);
				edb01IcmsSt.edb01json.put(getCampo("0", "ressarcimento"), ressarcimento);
				edb01IcmsSt.edb01json.put(getCampo("0", "outrosCred"), outrosCred);
				edb01IcmsSt.edb01json.put(getCampo("0", "ajustesCred"), ajustesCred);
				edb01IcmsSt.edb01json.put(getCampo("0", "retencao"), retencao);
				edb01IcmsSt.edb01json.put(getCampo("0", "outrosDeb"), outrosDeb);
				edb01IcmsSt.edb01json.put(getCampo("0", "ajustesDeb"), ajustesDeb);
				edb01IcmsSt.edb01json.put(getCampo("0", "saldo"), saldo);
				edb01IcmsSt.edb01json.put(getCampo("0", "deducoes"), deducoes);
				edb01IcmsSt.edb01json.put(getCampo("0", "saldoDevedor"), saldoDevedor);
				edb01IcmsSt.edb01json.put(getCampo("0", "saldoCredor"), saldoCredor);
				edb01IcmsSt.edb01json.put(getCampo("0", "extra"), extra);
				edb01IcmsSt.edb01obs = null;
				
				getSamWhere().setDefaultValues(edb01IcmsSt);
				getSession().persist(edb01IcmsSt);
				edb01Retorno = edb01IcmsSt;
			}
		}
		
		put("edb01", edb01Retorno);
	}
	
	private List<Aag02> buscarEstados(){
		return getSession().createQuery(" SELECT * FROM Aag02 ORDER BY aag02uf ").getList(ColumnType.ENTITY);
	}
	
	Edb01 buscarApuracaoAnterior(Long aag02id, Edb01 edb01){
		def sql = " SELECT * FROM Edb01 " +
				  " WHERE " + Fields.numMeses("edb01mes", "edb01ano") + " < :numMeses " +
				  " AND edb01uf = :aag02id " + obterWherePadrao("Edb01") +
				  " ORDER BY edb01ano DESC, edb01mes DESC"

		def numMeses = DateUtils.numMeses(edb01.edb01mes, edb01.edb01ano)
		return getAcessoAoBanco().buscarRegistroUnico(sql, criarParametroSql("aag02id", aag02id), 
			                                               criarParametroSql("numMeses", numMeses))
	}

	BigDecimal buscarDevolucoesRessarcimentosIcmsST(boolean isDevolucao, Long aag02id, String cpoVlr1, LocalDate dtInicial, LocalDate dtFinal) {
		def cfops = isDevolucao ? Utils.list("1410", "1411", "1414", "1415", "1660", "1661", "1662", "2410", "2411", "2414", "2415", "2660", "2661", "2662") : Utils.list("1603", "2603")
		def modelos = isDevolucao ? Utils.list("01", "1B", "04", "55", "65") : Utils.list("01", "1B", "04", "55", "65")
		
		def sql = " SELECT SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) AS " + cpoVlr1 +
				" FROM Eaa01 " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				" INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				" INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				" INNER JOIN Aag02 ON aag02id = aag0201uf " +
				" INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				" INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				" WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				" AND eaa0101principal = 1 " +
				" AND aaj15codigo IN (:cfop) " +
				" AND aag02id = :aag02id " + 
				" AND aah01modelo IN (:modelos) " +
				" AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				" AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				" AND eaa0101principal = 1 " + obterWherePadrao("eaa01")

		def result = getSession().createQuery(sql)
					             .setParameter("cfop", cfops)
								 .setParameter("modelos", modelos)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("eaa01iEfdIcms", 1)
								 .setParameter("aag02id", aag02id)
								 .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarOutrosCredEaa01035IcmsSt(List<String> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01035valor) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " INNER JOIN Eaa01035 ON eaa01035item = eaa0103id " +
				  " INNER JOIN Aaj17 ON aaj17id = eaa01035ajuste " +
				  " LEFT JOIN Aaj28 ON aaj28id = aaj17tipo " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND aaj28codigo = :aaj28codigo " +
				  " AND SUBSTR(aaj17codigo, 3, 1) = :ter " +
				  " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("eaa01")
		
		def result = getSession().createQuery(sql)
								 .setParameter("ter", "1")
								 .setParameter("qua", Utils.list("2", "3"))
								 .setParameter("aag02id", aag02id)
								 .setParameter("modelos", modelos)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("aaj28codigo", "001")
								 .setParameter("eaa01iEfdIcms", 1)
		                         .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result;
	}
	
	BigDecimal buscarOutrosCredIcmsST(List<String> modelos, Long aag02id, String cpoVlr1, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(jGet(eaa01json." + cpoVlr1 + ")::numeric) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND (SUBSTR(aaj15codigo, 1, 1) IN (:posCfop) " +
				  " AND aaj15codigo NOT IN (:cfop)) " +
				  " AND aag02id = :aag02id " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				   obterWherePadrao("Eaa01")
		
		def result = getSession().createQuery(sql)
								 .setParameter("posCfop", Utils.list("1", "2"))
								 .setParameter("cfop", Utils.list("1410", "1411", "1414", "1415", "1660", "1661", "1662", "2410", "2411", "2414", "2415", "2660", "2661", "2662"))
								 .setParameter("aag02id", aag02id)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("eaa01iEfdIcms", 1)
								 .setParameter("modelos", modelos)
		                         .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarAjustesCredEaa01031IcmsSt(List<String> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01031icms) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01031 ON eaa01031item = eaa0103id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " LEFT JOIN Aaj16 ON aaj16id = eaa01031codAjuste " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND SUBSTR(aaj16codigo, 3, 1) IN (:ter) " +
				  " AND SUBSTR(aaj16codigo, 4, 1) = :qua " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('01', '02', '03', '04', '05', '07')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("Eaa01")
					 
		def result = getSession().createQuery(sql)
		                         .setParameter("ter", Utils.list("0", "1", "2"))
								 .setParameter("qua", "1")
								 .setParameter("aag02id", aag02id)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("eaa01iEfdIcms", 1)
								 .setParameter("modelos", modelos)
		                         .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarRetencaoIcmsSt(List<String> modelos, Long aag02id, String cpoVlr1, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) AS valor" +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND SUBSTR(aaj15codigo, 1, 1) IN (:cfop) " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
	   			    obterWherePadrao("Eaa01")
		
		def result = getSession().createQuery(sql)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("aag02id", aag02id)
								 .setParameter("cfop", Utils.list("5", "6"))
								 .setParameter("modelos", modelos)
								 .setParameter("eaa01iEfdIcms" , 1)
								 .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarOutrosDebEaa01035IcmsSt(List<String> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01035valor) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01035 ON eaa01035item = eaa0103id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " LEFT JOIN Aaj17 ON aaj17id = eaa01035ajuste " +
				  " LEFT JOIN Aaj28 ON aaj28id = aaj17tipo " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND aaj28codigo = '1' " +
				  " AND SUBSTR(aaj17codigo, 3, 1) = :ter " +
				  " AND SUBSTR(aaj17codigo, 4, 1) IN (:qua) " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("Eaa01")
		
		def result = getSession().createQuery(sql)
		                         .setParameter("ter", "1")
								 .setParameter("qua", Utils.list("0", "1"))
								 .setParameter("aag02id", aag02id)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("eaa01iEfdIcms", 1)
								 .setParameter("modelos", modelos)
								 .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarAjustesDebEaa01031IcmsSt(List<String> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01031icms) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01031 ON eaa01031item = eaa0103id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " LEFT JOIN Aaj16 ON aaj16id = eaa01031codAjuste " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND SUBSTR(aaj16codigo, 3, 1) IN (:ter) " +
				  " AND SUBSTR(aaj16codigo, 4, 1) = :qua " + 
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('01', '02', '03', '04', '05', '07')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("Eaa01")
					 
		def result = getSession().createQuery(sql)
		                         .setParameter("ter", Utils.list("3", "4", "5"))
								 .setParameter("qua", "1")
								 .setParameter("aag02id", aag02id)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("eaa01iEfdIcms", 1)
								 .setParameter("modelos", modelos)
								 .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarDeducoesEaa01031IcmsSt(List<String> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01031icms) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01031 ON eaa01031item = eaa0103id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " LEFT JOIN Aaj16 ON aaj16id = eaa01031codAjuste " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND SUBSTR(aaj16codigo, 3, 1) = :ter " +
				  " AND SUBSTR(aaj16codigo, 4, 1) = :qua " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("Eaa01")
				  
		def result = getSession().createQuery(sql)
								 .setParameter("ter", "6")
								 .setParameter("qua", "1")
								 .setParameter("aag02id", aag02id)
								 .setParameter("dtInicial", dtInicial)
								 .setParameter("dtFinal", dtFinal)
								 .setParameter("eaa01iEfdIcms", 1)
								 .setParameter("modelos", modelos)
								 .getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarDeducoesEaa01035IcmsSt(List<TableMap> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01035valor) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01035 ON eaa01035item = eaa0103id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " LEFT JOIN Aaj17 ON aaj17id = eaa01035ajuste " +
				  " LEFT JOIN Aaj28 ON aaj28id = aaj17tipo " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aaj28codigo = '1' " +
				  " AND aag02id = :aag02id " +
				  " AND SUBSTR(aaj17codigo, 3, 1) = :ter " +
				  " AND SUBSTR(aaj17codigo, 4, 1) = :qua " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms " +
				  	obterWherePadrao("Eaa01")
		
		def result = getSession().createQuery(sql)
		.setParameters("ter", "1", 
			           "qua", "4", 
					   "aag02id", aag02id, 
					   "dtInicial", dtInicial, 
					   "dtFinal", dtFinal, 
					   "eaa01iEfdIcms", 1, 
					   "modelos", modelos)
		.getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarExtraApurC100IcmsSt(List<String> modelos, Long aag02id, String cpoVlr1, LocalDate dtInicial, LocalDate dtFinal) {
		if(cpoVlr1 == null) return 0;
		
		def sql = " SELECT SUM(jGet(eaa0103json." + cpoVlr1 + "):numeric) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aaag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('01', '07')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("Eaa01")
		
		def result = getSession().createQuery(sql)
		.setParameters("aag02id", aag02id, 
			           "dtInicial", dtInicial, 
					   "dtFinal", dtFinal, 
					   "eaa01iEfdIcms", 1, 
					   "modelos", modelos)
		.getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarExtraApurEaa01031IcmsSt(List<String> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01031icms) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01031 ON eaa01031item = eaa0103id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " LEFT JOIN Aaj16 ON aaj16id = eaa01031codAjuste " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND SUBSTR(aaj16codigo, 3, 1) = :ter " +
				  " AND SUBSTR(aaj16codigo, 4, 1) IN (:qua) " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("Eaa01")
		
		def result = getSession().createQuery(sql)
		.setParameters("ter", "7", 
			           "qua", "1", 
					   "aag02id", aag02id, 
					   "dtInicial", dtInicial, 
					   "dtFinal", dtFinal, 
					   "eaa01iEfdIcms", 1, 
					   "modelos", modelos)
		.getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	BigDecimal buscarExtraApurEaa01035IcmsSt(List<String> modelos, Long aag02id, LocalDate dtInicial, LocalDate dtFinal) {
		def sql = " SELECT SUM(eaa01035valor) AS valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Eaa01035 ON eaa01035item = eaa0103id " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " INNER JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
				  " LEFT JOIN Aaj17 ON aaj17id = eaa01035ajuste " +
				  " LEFT JOIN Aaj28 ON aaj28id = aaj17tipo " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND eaa0101principal = 1 " +
				  " AND aag02id = :aag02id " +
				  " AND aaj28codigo = '1' " +
				  " AND SUBSTR(aaj17codigo, 3, 1) = :ter " +
				  " AND SUBSTR(aaj17codigo, 4, 1) = :qua " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
				  " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
				    obterWherePadrao("Eaa01")
		
		def result = getSession().createQuery(sql)
		.setParameters("ter", "1", "qua", "5", "aag02id", aag02id, "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1, "modelos", modelos)
		.getUniqueResult(ColumnType.BIG_DECIMAL);
   
		return result == null ? 0 : result
	}
	
	private void excluirApuracoes(Integer mes, Integer ano, Long aaj28id, Long aag02id){
		String sql = " DELETE FROM Edb01 WHERE edb01tipo = :tipo  AND " + 
			         Fields.numMeses("edb01mes", "edb01ano") + " = :mesAno " + (aag02id == null ? "" : " AND edb01uf = :uf") + 
					 getSamWhere().getWhereGc("AND", Edb01.class);

        def numMeses = DateUtils.numMeses(mes, ano);
		getAcessoAoBanco().deletarRegistrosBySQL(sql, Parametro.criar("tipo", aaj28id), Parametro.criar("mesAno", numMeses), aag02id == null ? null : Parametro.criar("uf", aag02id));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDEifQ==