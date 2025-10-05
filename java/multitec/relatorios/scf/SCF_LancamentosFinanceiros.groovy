package multitec.relatorios.scf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.da.Dab01
import sam.model.entities.da.Dab0101
import sam.model.entities.da.Dab10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_LancamentosFinanceiros extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCF - Lançamentos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("periodo", DateUtils.getStartAndEndMonth(MDate.date()));
		filtrosDefault.put("impressao", "0")
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idContaCorrente = getListLong("contaCorrente");
		LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
		boolean isSaltarPagina = get("isSaltarPagina");	
		Integer impressao = getInteger("impressao")
		
		List<TableMap> dados = new ArrayList<>();
		params.put("TITULO_RELATORIO", "Lançamentos Financeiros");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

		List<TableMap> dab10s = obterDadosRelatorio(idContaCorrente, dataPeriodo);
		Integer i = 0;
		Integer codigoConta = 0;
		BigDecimal saldo = BigDecimal.ZERO;
		for (TableMap dab10 : dab10s) {
			TableMap tm = new TableMap();
			if (tm.getBigDecimal("SALDOINICIAL") == null) tm.put("SALDOINICIAL", BigDecimal.ZERO);
			
			tm.put("dab10id", dab10.getLong("dab10id"));
			tm.put("codigoConta", dab10.getInteger("dab01codigo"));
			if (tm.getInteger("codigoConta") != codigoConta) {
				saldo = BigDecimal.ZERO;
				BigDecimal valorSaldo = buscarSaldoAnterior(dab10.getLong("dab01id"), dataPeriodo[0]);
				if (valorSaldo == BigDecimal.ZERO) {
					valorSaldo = buscarSaldoInicial(dab10.getLong("dab01id"), dataPeriodo[0]);
				}
				i++;
				tm.put("SALDOINICIAL", valorSaldo);
			}
			tm.put("dab01codigo", dab10.getString("dab01codigo"));
			tm.put("dab01nome", dab10.getString("dab01nome"));
			
			tm.put("dab10data", dab10.getDate("dab10data"));
			tm.put("dab10historico", dab10.getString("dab10historico"));
			tm.put("dab10mov", dab10.getInteger("dab10mov"));
			tm.put("dab10valor", dab10.getBigDecimal("dab10valor"));
			
			if (saldo == BigDecimal.ZERO) {
				tm.put("SALDO", tm.getBigDecimal("SALDOINICIAL"));
				saldo = tm.getBigDecimal("SALDO");
			}

			if (tm.getInteger("dab10mov").equals(0)) {
				tm.put("SALDO", saldo.add(tm.getBigDecimal("dab10valor")));
			} else {
				tm.put("SALDO", saldo.subtract(tm.getBigDecimal("dab10valor")));
			}
			saldo = tm.getBigDecimal("SALDO");

			
			codigoConta = dab10.getInteger("dab01codigo")
			
			dados.add(tm)
		}	
		
		if(impressao == 1 ) return gerarXLSX("SCF_LancamentosFinanceiros", dados)
		return gerarPDF("SCF_LancamentosFinanceiros", dados, "codigoConta", isSaltarPagina)
	}
	
	private BigDecimal buscarSaldoInicial(Long dab01id, LocalDate[] data) {
		String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
		int numMeses = data != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;
		BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
				.addFields("dab0101saldo")
				.addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
				.addWhere(Criterions.le(field, numMeses))
				.addWhere(Criterions.eq("dab0101mes", 0)).addWhere(Criterions.eq("dab0101ano", 0))
				.addWhere(Criterions.in("dab01id", dab01id)).setMaxResults(1)
				.addWhere(samWhere.getCritPadrao(Dab01.class))
				.setOrder("dab01.dab01codigo").get(ColumnType.BIG_DECIMAL);
		return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
	}
	
	private BigDecimal buscarSaldoAnterior(Long dab01id, LocalDate[] data) {
		String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
		int numMeses = data[0] != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;
		BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
				.addFields("dab0101saldo")
				.addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
				.addWhere(Criterions.lt(field, numMeses))
				.addWhere(Criterions.ne("dab0101mes", 0)).addWhere(Criterions.ne("dab0101ano", 0))
				.addWhere(Criterions.in("dab01id", dab01id)).setMaxResults(1)
				.addWhere(samWhere.getCritPadrao(Dab01.class))
				.setOrder(field + "desc").get(ColumnType.BIG_DECIMAL);
			return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
	}
	
	public List<TableMap> obterDadosRelatorio (List<Long> idContaCorrente, LocalDate[] dataPeriodo)  {
		
		String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
		String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " and dab01.dab01id IN (:idContaCorrente)": "";
		Parametro parametro = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
		String sql = " select abb01num,  Dab01.dab01id, Dab01.dab01codigo, Dab01.dab01nome, Dab10.dab10id, Dab10.dab10data, Dab10.dab10cc, Dab10.dab10mov, Dab10.dab10historico, Dab10.dab10valor " + 
		              
		               " from Dab10 Dab10" + 
					   " left join Dab01 Dab01" +
					   "   on Dab01.dab01id = Dab10.dab10cc " + 
					   "left join abb01 on abb01id = Dab10.dab10central "+
					   wherePeriodoData +
					   whereIdsContaCorrente + 
					   getSamWhere().getWherePadrao(" AND ", Dab10.class) +
					   " order by Dab10.dab10data, dab10id " 
					

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro); 
		return receberDadosRelatorio;
	}
	
	
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==