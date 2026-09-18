package multitec.formulas.sgt.apuracoes;

import java.time.LocalDate;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aaj17;
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa01035;
import sam.model.entities.ed.Edb01;
import sam.model.entities.ed.Edb0101;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;

class Apuracao_LF_ICMS_ST extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_APURACAO;
	}

	@Override
	public void executar() {
		Edb01 edb01Retorno = null;
		Edb01 edb01 = get("edb01");
		
		List<Aag02> aag02s = new ArrayList<>();
		if (edb01 != null && edb01.edb01uf != null) {
			aag02s.add(edb01.edb01uf);
			
			excluirApuracoes(edb01.edb01mes, edb01.edb01ano, edb01.edb01tipo.aaj28id, edb01.edb01uf.aag02id);
		} else {
			aag02s.addAll(buscarEstados());
			
			excluirApuracoes(edb01.edb01mes, edb01.edb01ano, edb01.edb01tipo.aaj28id, null);
		}
		
		selecionarAlinhamento("0040");
		
		for (aag02 in aag02s) {
			Edb01 edb01Icms = new Edb01();
			edb01Icms.edb01ano = edb01.edb01ano;
			edb01Icms.edb01mes = edb01.edb01mes;
			edb01Icms.edb01tipo = edb01.edb01tipo;
			edb01Icms.edb01uf = aag02;
			
			TableMap edb01json = edb01Icms.edb01json != null ? edb01Icms.edb01json : new TableMap();
			
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
			 * Ocorrências
			 */
			Map<String, BigDecimal> mapValores = new HashMap<>();
			if(Utils.isEmpty(edb01Icms.edb0101s)) {
				List<Eaa01035> eaa01035s = buscarAjustesDaApurICMS(dataInicial, dataFinal, aag02.aag02id);
	
				if (!Utils.isEmpty(eaa01035s)) {
					Map<Aaj17, BigDecimal> mapOcorrencias = new HashMap<>();
					for (eaa01035 in eaa01035s) {
						def valor = mapOcorrencias.get(eaa01035.eaa01035ajuste) == null ? 0 : mapOcorrencias.get(eaa01035.eaa01035ajuste);
						mapOcorrencias.put(eaa01035.eaa01035ajuste, valor + eaa01035.eaa01035valor);
					}
					
					TreeSet<Aaj17> aaj17sOrdem = new TreeSet<Aaj17>(new Comparator<Aaj17>(){
						public int compare(Aaj17 o1, Aaj17 o2) {
							return o1.aaj17codigo.compareTo(o2.aaj17codigo);
						}
					});
					aaj17sOrdem.addAll(mapOcorrencias.keySet());
					
					if (!Utils.isEmpty(aaj17sOrdem)) {
						for (aaj17 in aaj17sOrdem) {
							Edb0101 edb0101 = new Edb0101();
							edb0101.edb0101ajuste = aaj17;
							edb0101.edb0101valor = mapOcorrencias.get(aaj17)
							edb0101.edb0101giaSI = aaj17?.aaj17giaSI;
							edb0101.edb0101giaFunLegal = aaj17?.aaj17giaFunLegal;
							edb0101.edb0101giaOcor = aaj17?.aaj17giaOcor;
							edb0101.edb0101obs = aaj17?.aaj17giaOcor?.length() > 100 ? aaj17?.aaj17giaOcor?.substring(0, 100) : aaj17?.aaj17giaOcor;
							edb0101.edb0101apur = edb01Icms;
							edb01Icms.addToEdb0101s(edb0101);
						}
					}
				}
			} else {
				for (edb0101 in edb01Icms.edb0101s) {
					Aaj17 aaj17 = edb0101.edb0101ajuste;
	
					String subItem = aaj17.aaj17codigo;
					subItem = subItem.length() > 3 ? subItem.substring(0, 3) : subItem;
					def vlrMap = mapValores.get(subItem) == null ? 0 : mapValores.get(subItem);
					mapValores.put(subItem, edb0101.edb0101valor + vlrMap);
				}
			}
			
			/**
			 * Valor com débito do imposto
			 */
			def debImp = buscarSaidas_DebitoImposto(getCampo("0","icmsst"), dataInicial, dataFinal, aag02.aag02id);
			edb01json.put(getCampo("0", "debImp"), debImp);
			
			/**
			 * Valor com crédito do imposto
			 */
			def credImp = buscarEntradas_CreditoImposto(getCampo("0","icmsst"), dataInicial, dataFinal, aag02.aag02id);
			edb01json.put(getCampo("0", "credImp"), credImp);
			
			/**
			 * Saldo credor anterior
			 */
			Edb01 edb01Anterior = buscarApuracaoAnterior(edb01.edb01mes, edb01.edb01ano, edb01.edb01tipo.aaj28id);
			def saldoCredorAnterior = 0;
			if(edb01Anterior != null) {
				saldoCredorAnterior = edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "credImp")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "outrosCred")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "estDeb")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "credAnt"));
				def saldoDebitos = edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "debImp")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "outrosDeb")) + edb01Anterior.edb01json.getBigDecimal_Zero(getCampo("0", "estCred"));
				saldoCredorAnterior = saldoCredorAnterior - saldoDebitos;
			}
			edb01json.put(getCampo("0", "credAnt"), saldoCredorAnterior);
			
			if (debImp != 0 || credImp != 0 || saldoCredorAnterior != 0 || (edb01Icms.edb0101s != null && edb01Icms.edb0101s.size() > 0)) {
				edb01Icms.setEdb01json(edb01json)
				getSamWhere().setDefaultValues(edb01Icms);
				getSession().persist(edb01Icms);
				edb01Retorno = edb01Icms;
			}
		}
		
		put("edb01", edb01Retorno)
	}
	
	private List<Aag02> buscarEstados(){
		return getSession().createQuery(" SELECT * FROM Aag02 ORDER BY aag02uf ").getList(ColumnType.ENTITY);
	}
	
	List<Eaa01035> buscarAjustesDaApurICMS(LocalDate dataInicial, LocalDate dataFinal, Long aag02id){
		String sql = " SELECT eaa01035id, eaa01035valor, aaj17id, aaj17codigo, aaj17descr, aaj17giaSI, aaj17giaFunLegal, aaj17giaOcor, eaa0103id, eaa01id, eaa0101id, aag0201id, aag02id " + 
					 " FROM Eaa01035 AS eaa01035 " +
					 " INNER JOIN PART eaa01035.eaa01035item AS eaa0103 " +
					 " INNER JOIN PART eaa0103.eaa0103doc AS eaa01 " +
					 " INNER JOIN PART eaa01035.eaa01035ajuste AS aaj17 " +
					 " INNER JOIN PART eaa01.eaa0101s AS eaa0101 " +
					 " INNER JOIN PART eaa0101.eaa0101municipio AS aag0201 " +
					 " INNER JOIN PART aag0201.aag0201uf AS aag02 " +
					 " WHERE eaa01esData BETWEEN :dataIni AND :dataFin " +
					 " AND eaa0101principal = 1 AND aag02id = :uf " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class);
		
		List<Eaa01035> eaa01035s = getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dataIni", dataInicial), Parametro.criar("dataFin", dataFinal), Parametro.criar("uf", aag02id));
		return eaa01035s;
	}
	
	BigDecimal buscarSaidas_DebitoImposto(String nomeCampo, LocalDate dataInicial, LocalDate dataFinal, Long aag02id) {
		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
					 " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
					 " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
					 " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
					 " INNER JOIN Aag02 ON aag02id = aag0201uf " +
					 " WHERE eaa01esData BETWEEN :dataIni AND :dataFin AND eaa01esMov = 1 " +
					 " AND eaa01cancData IS NULL AND eaa0101principal = 1 AND aag02id = :uf AND " +
					 " (aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') " + getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("dataIni", dataInicial), Parametro.criar("dataFin", dataFinal), Parametro.criar("uf", aag02id));
		return result == null ? 0 : result;
	}
	
	BigDecimal buscarEntradas_CreditoImposto(String nomeCampo, LocalDate dataInicial, LocalDate dataFinal, Long aag02id) {
		String sql = " SELECT SUM(jGet(eaa0103json." + nomeCampo + ")::numeric) AS valor " +
				 	 " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
					 " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
					 " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
					 " INNER JOIN Aag02 ON aag02id = aag0201uf " +
					 " WHERE eaa01esData BETWEEN :dataIni AND :dataFin AND eaa01esMov = 0 " +
					 " AND eaa01cancData IS NULL AND eaa0101principal = 1 AND aag02id = :uf AND " +
					 " (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " + getSamWhere().getWherePadrao("AND", Eaa01.class);

		def result = getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("dataIni", dataInicial), Parametro.criar("dataFin", dataFinal), Parametro.criar("uf", aag02id));
		return result == null ? 0 : result;
	}
	
	Edb01 buscarApuracaoAnterior(Integer mes, Integer ano, Long aaj28id) {
		String sql = " SELECT * FROM Edb01 " +
					 " WHERE edb01tipo = :tipo AND " + Fields.numMeses("edb01mes", "edb01ano") + " < :mesAno " +
					 getSamWhere().getWhereGc("AND", Edb01.class) + " ORDER BY edb01ano desc, edb01mes desc";
		
		def numMeses = DateUtils.numMeses(mes, ano);
		Edb01 edb01Anterior = getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("tipo", aaj28id), Parametro.criar("mesAno", numMeses));
		return edb01Anterior;
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