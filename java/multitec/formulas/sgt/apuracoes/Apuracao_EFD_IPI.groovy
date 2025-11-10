package multitec.formulas.sgt.apuracoes;

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ea.Eaa01
import sam.model.entities.ed.Edb01;
import sam.model.entities.ed.Edb0105
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;

class Apuracao_EFD_IPI extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_APURACAO;
	}

	@Override
	public void executar() {
		Edb01 edb01 = get("edb01");
		
		LocalDate dtInicial = LocalDate.of(edb01.edb01ano, edb01.edb01mes, 1);
		LocalDate dtFinal = LocalDate.of(edb01.edb01ano, edb01.edb01mes, dtInicial.lengthOfMonth());
		
		TableMap edb01json = edb01.edb01json != null ? edb01.edb01json : new TableMap();
		
		selecionarAlinhamento("0032");
		
		String cpoIpi = getCampo("0", "ipi_efd");
		
		/**
		* 01 - Saldo credor anterior
		*/
		def credAnt = 0;
		Edb01 edb01Anterior = buscarApuracaoAnterior(edb01.edb01mes, edb01.edb01ano, edb01.edb01tipo.aaj28id);
		if(edb01Anterior != null) credAnt = edb01Anterior.edb01json.get("saldoCredor");
		
		edb01json.put(getCampo("0", "credAnt"), credAnt);
		
		
		/**
		 * 02 - Saídas com débito do imposto
		 */
		
		def debSaidas = buscarDebitosIPI(cpoIpi, dtInicial, dtFinal);
		edb01json.put(getCampo("0", "debSaidas"), debSaidas);
		
		/**
		 * 03 - Entradas com crédito do imposto
		 */
		def credEntradas = buscarCreditosIPI(cpoIpi, dtInicial, dtFinal);
		edb01json.put(getCampo("0", "credEntradas"), credEntradas);
		
		/**
		 * 04 - Outros débitos e estorno de créditos
		 */
		def outrosDeb = 0;
		if(edb01 != null) {
			for(Edb0105 edb0105 : edb01.edb0105s) {
				outrosDeb = outrosDeb + edb0105.edb0105valor;
			}
		}
		edb01json.put(getCampo("0", "outrosDeb"), outrosDeb);
		
		/**
		 * 05 - Outros créditos e estorno de débitos
		 */
		def outrosCred = 0;
		if(edb01 != null) {
			for(Edb0105 edb0105 : edb01.edb0105s) {
				outrosCred = outrosCred + edb0105.edb0105valor;
			}
		}
		edb01json.put(getCampo("0", "outrosCred"), outrosCred);
		
		/**
		 * 06 - Total de IPI a recuperar
		 */
		def saldoCredor = 0;
		
		def saldo1 = edb01.edb01json.getBigDecimal_Zero("debSaidas") + outrosDeb;
		def saldo2 = edb01.edb01json.getBigDecimal_Zero("credAnt") + outrosCred;
		def saldo = saldo1 - saldo2;
		
		if(saldo < 0) saldoCredor = saldo * (-1);
		edb01json.put(getCampo("0", "saldoCredor"), saldoCredor);
		
		/**
		 * 07 - Total de IPI a recolher
		 */
		def saldoDevedor = 0;
		
		if(saldo >= 0) saldoDevedor = saldo;
		edb01json.put(getCampo("0", "saldoDevedor"), saldoDevedor);
		
		edb01.edb01json = edb01json;
		put("edb01", edb01);
	}
	
	private Edb01 buscarApuracaoAnterior(Integer mes, Integer ano, Long aaj28id) {
		String sql = " SELECT * FROM Edb01 " +
					 " WHERE edb01tipo = :tipo AND " + Fields.numMeses("edb01mes", "edb01ano") + " < :mesAno " +
					 getSamWhere().getWhereGc("AND", Edb01.class) + " ORDER BY edb01ano desc, edb01mes desc";
		
		def numMeses = DateUtils.numMeses(mes, ano);
		Edb01 edb01Anterior = getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("tipo", aaj28id), Parametro.criar("mesAno", numMeses));
		
		return edb01Anterior;
	}
	
	private BigDecimal buscarDebitosIPI(String nomeCampo, LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
			  		 " FROM Eaa01 " +
					   " INNER JOIN Abb01 ON abb01id = eaa01central " +
					   " INNER JOIN AAh01 ON aah01id = abb01tipo " +
					   " INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					   " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
					   " LEFT JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					   " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					   " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					   " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
					   " AND SUBSTR(aaj15codigo, 1, 1) IN (:cfop) " +
					   " AND aah01modelo IN (:modelos) " +
					   getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getSession().createQuery(sql)
					.setParameters("cfop", Utils.list("5", "6"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1, "modelos", Utils.list("01", "1B", "04", "55"))
					.getUniqueResult(ColumnType.BIG_DECIMAL);
					
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarCreditosIPI(String nomeCampo, LocalDate dtInicial, LocalDate dtFinal) {
		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
			  		 " FROM Eaa01 " +
					   " INNER JOIN Abb01 ON abb01id = eaa01central " +
					   " INNER JOIN AAh01 ON aah01id = abb01tipo " +
					   " INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
					   " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
					   " LEFT JOIN Aaj03 ON aaj03id = eaa01sitDoc " +
					   " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
					   " AND aaj03efd NOT IN ('02', '03', '04', '05')" +
					   " AND eaa01iEfdIcms = :eaa01iEfdIcms" +
					   " AND SUBSTR(aaj15codigo, 1, 1) IN (:cfop) " +
					   " AND aah01modelo IN (:modelos) " +
					   getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getSession().createQuery(sql)
					.setParameters("cfop", Utils.list("1", "2", "3"), "dtInicial", dtInicial, "dtFinal", dtFinal, "eaa01iEfdIcms", 1, "modelos", Utils.list("01", "1B", "04", "55"))
					.getUniqueResult(ColumnType.BIG_DECIMAL);
					
		return result == null ? 0 : result;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDEifQ==