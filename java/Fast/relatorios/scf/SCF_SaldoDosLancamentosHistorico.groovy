package Fast.relatorios.scf;

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

public class SCF_SaldoDosLancamentosHistorico extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCF - Saldo dos Lançamentos (histórico)";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("periodo", DateUtils.getStartAndEndMonth(MDate.date()));
		filtrosDefault.put("impressao", "0");
		filtrosDefault.put("conciliacao", "0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idContaCorrente = getListLong("contaCorrente");
		LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
		boolean isSaltarPagina = get("isSaltarPagina");
		Integer impressao = getInteger("impressao")

		List<TableMap> dados = new ArrayList<>();

		List<TableMap> dab10s = obterDadosRelatorio(idContaCorrente, dataPeriodo);
		Integer codigoConta = 0;
		BigDecimal saldo = BigDecimal.ZERO;

		for (TableMap dab10 : dab10s) {
			TableMap tm = new TableMap();

			if(dab10.getInteger("dab01codigo") != codigoConta){
				saldo = BigDecimal.ZERO;
				Long contaCorrente = dab10.getLong("dab01id");

				BigDecimal saldoCC = buscarSaldoConta(contaCorrente, dataPeriodo[0]) ;

				saldoCC = saldoCC == BigDecimal.ZERO ? buscarSaldoInicial(contaCorrente,dataPeriodo[0]) : saldoCC;

				BigDecimal saldoInicial = buscarSaldoInicial(contaCorrente,dataPeriodo[0]);

				BigDecimal saldoAnterior = buscarSaldoAnterior(contaCorrente, dataPeriodo,saldoInicial);

				tm.put("valorInicial", saldoAnterior);
			}

			tm.put("codConta", dab10.getString("dab01codigo"));
			tm.put("nomeConta", dab10.getString("dab01nome"));
			tm.put("data", dab10.getDate("dab10data"));
			tm.put("valorLancamento", dab10.getBigDecimal("dab10valor"));
			tm.put("codEntidade", dab10.getString("abe01codigo"));
			tm.put("naEntidade", dab10.getString("abe01na"));
			tm.put("parcela", dab10.getString("abb01parcela"));
			tm.put("quita", dab10.getString("abb01quita"));
			tm.put("numDoc", dab10.getString("abb01num"));
			tm.put("historico", dab10.getString("dab10historico"));

			if(saldo == BigDecimal.ZERO){
				saldo = tm.getBigDecimal("valorInicial");
			}

			if(dab10.getInteger("dab10mov").equals(0)){
				tm.put("saldo", saldo.add(dab10.getBigDecimal("dab10valor")));
				tm.put("movimentacao", "E")
			}else{
				tm.put("saldo", saldo.subtract(dab10.getBigDecimal("dab10valor")));
				tm.put("movimentacao", "S")
			}

			saldo = tm.getBigDecimal("saldo");

			codigoConta = dab10.getInteger("dab01codigo");

			dados.add(tm);
		}

		params.put("periodo", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		params.put("empresa", obterEmpresaAtiva().getAac10codigo() +"-"+obterEmpresaAtiva().getAac10na());
		params.put("titulo", "SCF - Saldo dos Lançamentos (Histórico)" );

		if(impressao == 1 ) return gerarXLSX("SCF_SaldoDosLancamentosHistorico_Excel", dados)
		return gerarPDF("SCF_SaldoDosLancamentosHistorico_PDF", dados, "codigoConta", isSaltarPagina)
	}


	public List<TableMap> obterDadosRelatorio (List<Long> idContaCorrente, LocalDate[] dataPeriodo)  {

		String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
		String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " and dab01.dab01id IN (:idContaCorrente)": "";
		Parametro parametro = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;

		String sql = " select abb01num,  Dab01.dab01id, Dab01.dab01codigo, Dab01.dab01nome, Dab10.dab10id, Dab10.dab10data, Dab10.dab10cc, Dab10.dab10mov, Dab10.dab10historico, Dab10.dab10valor, dab1002dtconc, " +
				"abe01codigo, abe01na, abb01parcela, abb01quita "+
				" from Dab10 Dab10" +
				" left join Dab01 Dab01 on Dab01.dab01id = Dab10.dab10cc " +
				" left join abb01 on abb01id = Dab10.dab10central "+
				"LEFT join abe01 on abe01id = abb01ent " +
				" left join dab1002 on dab1002lct = dab10id "+
				wherePeriodoData +
				whereIdsContaCorrente +
				getSamWhere().getWherePadrao(" AND ", Dab10.class) +
				" order by Dab01.dab01codigo,Dab10.dab10data, dab10id "


		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
		return receberDadosRelatorio;
	}

	private  BigDecimal buscarSaldoAnterior (Long idContaCorrente, LocalDate[] dataPeriodo,BigDecimal saldoInicial)  {

		String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data < '" + dataPeriodo[0] + "'": "";
		String whereIdsContaCorrente = " and dab01.dab01id = :idContaCorrente "

		Parametro parametroCC = Parametro.criar("idContaCorrente", idContaCorrente);

		String sql = " select abb01num,  Dab01.dab01id, Dab01.dab01codigo, Dab01.dab01nome, Dab10.dab10id, Dab10.dab10data, Dab10.dab10cc, Dab10.dab10mov, Dab10.dab10historico, Dab10.dab10valor, dab1002dtconc, " +
				"abe01codigo, abe01na, abb01parcela, abb01quita "+
				" from Dab10 Dab10" +
				" left join Dab01 Dab01 on Dab01.dab01id = Dab10.dab10cc " +
				" left join abb01 on abb01id = Dab10.dab10central "+
				" LEFT join abe01 on abe01id = abb01ent " +
				" left join dab1002 on dab1002lct = dab10id "+
				wherePeriodoData +
				whereIdsContaCorrente +
				getSamWhere().getWherePadrao(" AND ", Dab10.class) +
				" order by Dab01.dab01codigo,Dab10.dab10data, dab10id "


		List<TableMap> lancamentos = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroCC);

		BigDecimal saldo = saldoInicial;

		for(lancamento in lancamentos){
			if(lancamento.getInteger("dab10mov").equals(0)){
				saldo += lancamento.getBigDecimal("dab10valor")
			}else{
				saldo -= lancamento.getBigDecimal("dab10valor")
			}

		}


		return saldo;
	}

	private BigDecimal buscarSaldoInicial(Long idConta, LocalDate[] data) {
		String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
		int numMeses = data != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;

		BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
				.addFields("dab0101saldo")
				.addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
				.addWhere(Criterions.le(field, numMeses))
				.addWhere(Criterions.eq("dab0101mes", 0)).addWhere(Criterions.eq("dab0101ano", 0))
				.addWhere(Criterions.in("dab01id", idConta)).setMaxResults(1)
				.addWhere(samWhere.getCritPadrao(Dab01.class))
				.setOrder("dab01.dab01codigo").get(ColumnType.BIG_DECIMAL);
		return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
	}

	private BigDecimal buscarSaldoConta(Long idConta, LocalDate[] data) {
		String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
		int numMeses = data[0] != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;

		BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
				.addFields("dab0101saldo")
				.addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
				.addWhere(Criterions.lt(field, numMeses))
				.addWhere(Criterions.ne("dab0101mes", 0)).addWhere(Criterions.ne("dab0101ano", 0))
				.addWhere(Criterions.in("dab01id", idConta)).setMaxResults(1)
				.addWhere(samWhere.getCritPadrao(Dab01.class))
				.setOrder(field + "desc").get(ColumnType.BIG_DECIMAL);
		return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
	}

}
//meta-sis-eyJkZXNjciI6IlNDRiAtIFNhbGRvIGRvcyBMYW7Dp2FtZW50b3MgKERldGFsaGFkb3MpIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRiAtIFNhbGRvIGRvcyBMYW7Dp2FtZW50b3MgKGhpc3TDs3JpY28pIiwidGlwbyI6InJlbGF0b3JpbyJ9