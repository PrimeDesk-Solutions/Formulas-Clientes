package multitec.relatorios.scf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abb11
import sam.model.entities.da.Dab01
import sam.model.entities.da.Dab0101
import sam.model.entities.da.Dab10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro;

public class SCF_ResumoLancamentosDepartamentos extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCF - Resumo dos Lançamentos por Departamentos";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", datas);
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		boolean exibeTotalGeral = get("totalGeral");
		List<Long> idContaCorrente = getListLong("contaCorrente");
		List<Long> idDepartamento = getListLong("departamentos");
		LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
		List<TableMap> dados = new ArrayList<>();
		params.put("TITULO_RELATORIO", "Resumo dos Lançamentos por Departamentos");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

		Integer[] qtDigGrau = verificarGraus("ABB11", "ESTRCODDEPTO");

		BigDecimal saldo = BigDecimal.ZERO;
		BigDecimal saldoAnterior = BigDecimal.ZERO;
		String codigoConta = null;

		List<TableMap> dadosRel = obterDadosRelatorio(idDepartamento, idContaCorrente, dataPeriodo, exibeTotalGeral);
		List<String> graus = new ArrayList<>();
		
		for (TableMap mapDados : dadosRel) {
			for(int k = 0; k < qtDigGrau.size(); k++) {
				Integer indexOfTm = null;

				String grauDepartamento = mapDados.getString("abb11codigo").substring(0, qtDigGrau[k]);
				TableMap tm = new TableMap();
				if (!Utils.isEmpty(dados)) {
					TableMap find = null;
					if (exibeTotalGeral) {
						find = dados.stream().filter({f -> f.getString("abb11codigo").equalsIgnoreCase(mapDados.getString("abb11codigo"))}).findFirst().orElse(null);
					} else {
						find = dados.stream().filter({f -> f.getString("abb11codigo").equalsIgnoreCase(mapDados.getString("abb11codigo"))}).filter({f -> f.getString("dab01codigo").equalsIgnoreCase(mapDados.getString("dab01codigo"))}).findFirst().orElse(null);
					}
					if (find != null) {
						tm = find;
						indexOfTm = dados.indexOf(tm);
					}
				}

				if (tm.getBigDecimal("valorDepartamento") == null) tm.put("valorDepartamento", BigDecimal.ZERO);
				
				//Exibir somente total não precisa da conta corrente
				if (!exibeTotalGeral) {
					tm.put("codigoConta", mapDados.getString("dab01codigo"));
					if (!tm.getString("codigoConta").equalsIgnoreCase(codigoConta)) {
						saldo = BigDecimal.ZERO;
						saldoAnterior = BigDecimal.ZERO;
						graus.clear();
						saldoAnterior = buscarSaldoAnterior(mapDados.getLong("dab01id"), dataPeriodo[0]);
						if (saldoAnterior == BigDecimal.ZERO) {
							saldoAnterior = buscarSaldoInicial(mapDados.getLong("dab01id"));
						}
					}
				} else {
					//Sem filtro de conta corrente, busca o saldo todo
					String whereIdsContaCorrente = !Utils.isEmpty(idContaCorrente) ? " and dab01.dab01id IN (:idContaCorrente)": "";
					String sqlSaldoContas = " SELECT dab01id FROM Dab01 " + getSamWhere().getWherePadrao("WHERE", Dab01.class) + whereIdsContaCorrente;
					List<Long> dab01ids = getAcessoAoBanco().obterListaDeLong(sqlSaldoContas, !Utils.isEmpty(idContaCorrente) ? Parametro.criar("idContaCorrente", idContaCorrente) : null);
					saldoAnterior = BigDecimal.ZERO;
					for (dab01id in dab01ids) {
						BigDecimal saldoTotal = buscarSaldoAnterior(dab01id, dataPeriodo[0]);
						if (saldoTotal == BigDecimal.ZERO) {
							saldoTotal = buscarSaldoInicial(dab01id);
						}
						saldoAnterior = saldoAnterior.add(saldoTotal);
					}
				}
				
				if (mapDados.getString("abb11codigo") != grauDepartamento) {
					if (graus.contains(grauDepartamento)) continue;
					Abb11 departamento = buscarDepartamentoPeloCodigo(grauDepartamento);
					
					List<TableMap> totalDepartamento = null;
					if (!exibeTotalGeral) {
						totalDepartamento = buscarTotalDepartamento(mapDados.getLong("dab01id"), dataPeriodo, qtDigGrau[k], grauDepartamento)
					} else {
						totalDepartamento = buscarTotalDepartamento(dataPeriodo, qtDigGrau[k], grauDepartamento)
					}

					for (TableMap map : totalDepartamento) {

						tm.put("dab01codigo", mapDados.getString("dab01codigo"));
						tm.put("dab01nome", mapDados.getString("dab01nome"));
						tm.put("abb11codigo", departamento == null ? mapDados.getString("abb11codigo"): departamento.abb11codigo);
						tm.put("abb11nome", departamento == null ? mapDados.getString("abb11nome"): departamento.abb11nome);
						if (map.getInteger("dab10mov").equals(0)){
							tm.put("valorDepartamento", tm.getBigDecimal("valorDepartamento").add(map.getBigDecimal("valorDepartamento")));
						} else {
							tm.put("valorDepartamento", tm.getBigDecimal("valorDepartamento").subtract(map.getBigDecimal("valorDepartamento")));
						}
					}
				} else {
					tm.put("dab01codigo", mapDados.getString("dab01codigo"));
					tm.put("dab01nome", mapDados.getString("dab01nome"));
					tm.put("abb11codigo", mapDados.getString("abb11codigo"));
					tm.put("abb11nome", mapDados.getString("abb11nome"));
					if (mapDados.getInteger("dab10mov").equals(0)){
						BigDecimal valorDepartamento = tm.getBigDecimal("valorDepartamento") != null ? tm.getBigDecimal("valorDepartamento") : BigDecimal.ZERO;
						tm.put("valorDepartamento", valorDepartamento.add(mapDados.getBigDecimal("valorDepartamento")));
						saldo = saldo.add(mapDados.getBigDecimal("valorDepartamento"));
					} else {
						BigDecimal valorDepartamento = tm.getBigDecimal("valorDepartamento") != null ? tm.getBigDecimal("valorDepartamento") : BigDecimal.ZERO;
						tm.put("valorDepartamento", valorDepartamento.add(mapDados.getBigDecimal("valorDepartamento") * -1));
						saldo = saldo.subtract(mapDados.getBigDecimal("valorDepartamento"));
					}
				}
								
				tm.put("valorTotalPeri", saldo);
				tm.put("valorTotalDepartamento", saldoAnterior.add(saldo));
				tm.put("saldoAnterior", saldoAnterior);
				codigoConta = mapDados.getString("dab01codigo");
				if (indexOfTm == null) dados.add(tm);
				graus.add(grauDepartamento);
			}
		}

		if (exibeTotalGeral) {
			return gerarPDF("SCF_ResumoLancamentosDepartamentos_R2", dados)
		} else {
			return gerarPDF("SCF_ResumoLancamentosDepartamentos", dados)
		}
	}

	private List<TableMap> obterDadosRelatorio(List<Long> idDepartamento, List<Long> idContaCorrente, LocalDate[] dataPeriodo, boolean exibeTotalGeral) {

		String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
		String whereIdDepartamento = idDepartamento != null ? " and abb11.abb11id IN (:idDepartamento)": "";
		String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " and dab01.dab01id IN (:idContaCorrente)": "";

		Parametro paramCC = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
		Parametro paramDepartamento = idDepartamento != null && idDepartamento.size() > 0 ? Parametro.criar("idDepartamento", idDepartamento) : null;
		
		String select = null;
		String orderBy = null;
		String groupBy = null;
		if (exibeTotalGeral) {
			select = " select abb11.abb11codigo, abb11.abb11nome, dab10.dab10mov, SUM(dab1001.dab1001valor) as valorDepartamento ";
			orderBy = " order by abb11.abb11codigo, abb11.abb11nome ";
			groupBy = " group by abb11.abb11codigo, abb11.abb11nome, dab10.dab10mov ";
		} else {
			select = " select dab01.dab01id, dab01.dab01codigo, dab01.dab01nome, abb11.abb11codigo, abb11.abb11nome, dab10.dab10mov, SUM(dab1001.dab1001valor) as valorDepartamento";
			orderBy = " order by dab01.dab01codigo, abb11.abb11codigo ";
			groupBy = " group by dab01.dab01id, dab01.dab01codigo, abb11.abb11nome, abb11.abb11codigo, abb11.abb11nome, dab10.dab10mov ";
		}


		String sql = select +
				"  from dab1001 dab1001 " +
				" inner join Dab10 dab10 on dab10.dab10id = dab1001.dab1001lct" +
				"  left join Abf20 abf20 on Abf20.abf20id = dab10.dab10plf" +
				"  left join dab01 dab01 on dab01.dab01id = dab10.dab10cc" +
				"  left join Abb11 abb11 on abb11.abb11id = dab1001.dab1001depto" +
				wherePeriodoData +
				whereIdDepartamento +
				whereIdsContaCorrente +
				getSamWhere().getWherePadrao("AND", Dab10.class) +
				groupBy +
				orderBy;

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramCC, paramDepartamento);
		return receberDadosRelatorio;
	}

	private List<TableMap> buscarTotalDepartamento(Long idContaCorrente, LocalDate[] dataPeriodo, Integer grau, String codDepartamento) {

		String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
		String whereIdsContaCorrente = idContaCorrente != null  ? " and dab01.dab01id IN (:idContaCorrente)": "";
		String whereCodDepartamento = grau != null ? " and SUBSTRING(abb11codigo, 1, " + grau + ") = :codigo " : "";

		Parametro paramCC = idContaCorrente != null  ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
		Parametro paramCodDepartamento = grau != null ? Parametro.criar("codigo", codDepartamento) : null;

		String sql = " select SUM(dab1001.dab1001valor) as valorDepartamento, dab10.dab10mov " +
				"  from dab1001 dab1001 " +
				" inner join Dab10 dab10 on dab10.dab10id = dab1001.dab1001lct" +
				"  left join Abf20 abf20 on Abf20.abf20id = dab10.dab10plf" +
				"  left join dab01 dab01 on dab01.dab01id = dab10.dab10cc" +
				"  left join Abb11 abb11 on abb11.abb11id = dab1001.dab1001depto" +
				wherePeriodoData +
				whereIdsContaCorrente +
				whereCodDepartamento +
				getSamWhere().getWherePadrao("AND", Dab10.class) +
				" group by dab10.dab10mov";

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramCC, paramCodDepartamento);
		return receberDadosRelatorio;
	}

	private BigDecimal buscarSaldoInicial(Long dab01id) {
		BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
				.addFields("dab0101saldo")
				.addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
				.addWhere(Criterions.eq("dab0101mes", 0)).addWhere(Criterions.eq("dab0101ano", 0))
				.addWhere(Criterions.eq("dab01id", dab01id))
				.addWhere(samWhere.getCritPadrao(Dab01.class))
				.get(ColumnType.BIG_DECIMAL);
		return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
	}
	
	private BigDecimal buscarSaldoAnterior(Long dab01id, LocalDate[] data) {
		String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
		int numMeses = data[0] != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;
		BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
				.addFields(dab01id != null ? "dab0101saldo": "SUM(dab0101saldo) as dab0101saldo")
				.addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
				.addWhere(Criterions.lt(field, numMeses))
				.addWhere(Criterions.ne("dab0101mes", 0)).addWhere(Criterions.ne("dab0101ano", 0))
				.addWhere(dab01id != null ? Criterions.in("dab01id", dab01id): Criterions.isTrue()).setMaxResults(1)
				.addWhere(samWhere.getCritPadrao(Dab01.class))
				.setOrder(dab01id != null ? field + "desc": "").get(ColumnType.BIG_DECIMAL);
		return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
	}
	
	private BigDecimal buscarSaldoAnterior(LocalDate[] data) {
		return buscarSaldoAnterior(null, data);
	}
	
	private List<TableMap> buscarTotalDepartamento(LocalDate[] dataPeriodo, Integer grau, String codDepartamento) {
		return  buscarTotalDepartamento(null, dataPeriodo, grau, codDepartamento);
	}

	private Abb11 buscarDepartamentoPeloCodigo(String abb11codigo){
		return session.createQuery(" SELECT abb11id, abb11codigo, abb11nome",
				"   FROM abb11",
				"  WHERE LOWER(abb11codigo) = :abb11codigo",
				samWhere.getWherePadrao("AND", Abb11.class))
				.setParameter("abb11codigo", abb11codigo.toLowerCase())
				.getUniqueResult(ColumnType.ENTITY);
	}

	private Integer[] verificarGraus(String aplic, String param) {
		String parametros = getAcessoAoBanco().buscarParametro(param, aplic)

		int qtGrau;
		int[] qtDig;
		int tamanhoMaxGrau = 0;

		if (parametros != null) {
			StringTokenizer strToken = new StringTokenizer(parametros, "|");
			qtGrau = strToken.countTokens();

			if(qtGrau < 2 || qtGrau > 12)throw new ValidacaoException("A estrutura de código de contas deve ser no mínimo 2 e no máximo 12 graus.");

			qtDig = new int[qtGrau];

			int i = 0;
			while(strToken.hasMoreTokens()){
				int digitos = strToken.nextToken().length();
				if(i > 0){
					qtDig[i] = qtDig[i-1] + digitos;
				}else{
					qtDig[i] = digitos;
				}
				i++;
				tamanhoMaxGrau += digitos;
			}
		}

		return qtDig;
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIFJlc3VtbyBkb3MgTGFuw6dhbWVudG9zIHBvciBEZXBhcnRhbWVudG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9