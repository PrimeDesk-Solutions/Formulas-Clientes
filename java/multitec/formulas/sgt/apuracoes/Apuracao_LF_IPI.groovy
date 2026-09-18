package multitec.formulas.sgt.apuracoes;

import java.time.LocalDate;

import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ed.Edb01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;

class Apuracao_LF_IPI extends FormulaBase {

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
		
		selecionarAlinhamento("0040");
		
		edb01json.put(getCampo("0", "debImp"), 0);
		edb01json.put(getCampo("0", "outrosDeb"), 0);
		edb01json.put(getCampo("0", "estCred"), 0);
		edb01json.put(getCampo("0", "credImp"), 0);
		edb01json.put(getCampo("0", "outrosCred"), 0);
		edb01json.put(getCampo("0", "estDeb"), 0);
		edb01json.put(getCampo("0", "credAnt"), 0);
		edb01json.put(getCampo("0", "deducoes"), 0);
		edb01json.put(getCampo("0", "subTotSai"), 0);
		edb01json.put(getCampo("0", "subTotEnt"), 0);
		edb01json.put(getCampo("0", "total"), 0);
		edb01json.put(getCampo("0", "sdoDevedor"), 0);
		edb01json.put(getCampo("0", "sdoCredor"), 0);
		edb01json.put(getCampo("0", "impRecolher"), 0);
		
		LocalDate dataInicial = LocalDate.of(edb01.edb01ano, edb01.edb01mes, 1);
		LocalDate dataFinal = LocalDate.of(edb01.edb01ano, edb01.edb01mes, dataInicial.lengthOfMonth());
		
		/**
		 * Valor com débito do imposto
		 */
		def debImp = buscarSaidas_DebitoImposto(getCampo("0","ipi"), dataInicial, dataFinal);
		edb01json.put(getCampo("0", "debImp"), debImp);
		
		/**
		 * Valor com crédito do imposto
		 */
		def credImp = buscarEntradas_CreditoImposto(getCampo("0","ipi"), dataInicial, dataFinal);
		edb01json.put(getCampo("0", "credImp"), credImp);
		
		/**
		 * Saldo credor anterior
		 */
		Edb01 edb01Anterior = buscarApuracaoAnterior(edb01.edb01mes, edb01.edb01ano, edb01.edb01tipo.aaj28id); 
		if(edb01Anterior != null) {
			def saldoCredorAnterior = edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "credImp")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "outrosCred")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "estDeb")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "credAnt"));
			def saldoDebitos = edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "debImp")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "outrosDeb")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "estCred"));
			saldoCredorAnterior = saldoCredorAnterior - saldoDebitos;
			if(saldoCredorAnterior > 0) edb01json.put(getCampo("0", "credAnt"), saldoCredorAnterior);
		}
	}
	
	private BigDecimal buscarSaidas_DebitoImposto(String nomeCampo, LocalDate dataInicial, LocalDate dataFinal) {
		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
					 " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " + 
					 " WHERE eaa01esData BETWEEN :dataIni AND :dataFin AND eaa01esMov = 1 AND eaa01cancData IS NULL AND " +
					 " (aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') " + getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("dataIni", dataInicial), Parametro.criar("dataFin", dataFinal));
		return result == null ? 0 : result;
	}
	
	private BigDecimal buscarEntradas_CreditoImposto(String nomeCampo, LocalDate dataInicial, LocalDate dataFinal) {
		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
 					 " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
					 " WHERE eaa01esData BETWEEN :dataIni AND :dataFin AND eaa01esMov = 0 AND eaa01cancData IS NULL AND " +
					 " (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " + getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		def result = getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("dataIni", dataInicial), Parametro.criar("dataFin", dataFinal));
		return result == null ? 0 : result;
	}
	
	private Edb01 buscarApuracaoAnterior(Integer mes, Integer ano, Long aaj28id) {
		String sql = " SELECT * FROM Edb01 " +
					 " WHERE edb01tipo = :tipo AND " + Fields.numMeses("edb01mes", "edb01ano") + " < :mesAno " +
					 getSamWhere().getWhereGc("AND", Edb01.class) + " ORDER BY edb01ano desc, edb01mes desc";
		
		def numMeses = DateUtils.numMeses(mes, ano);
		Edb01 edb01Anterior = getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("tipo", aaj28id), Parametro.criar("mesAno", numMeses));
		
		return edb01Anterior;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDEifQ==