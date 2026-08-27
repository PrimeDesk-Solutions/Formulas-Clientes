// OFICIAL
package MM.relatorios.scf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multitec.utils.Utils
import br.com.multitec.utils.StringUtils
import br.com.multiorm.criteria.fields.Fields
import sam.model.entities.ab.Abf10
import sam.model.entities.da.Dab01
import sam.model.entities.da.Dab0101
import sam.model.entities.da.Dab10
import br.com.multiorm.criteria.join.Joins
import br.com.multiorm.ColumnType

class SCF_NaturezasAgrupadas extends RelatorioBase {

	@Override
	public Map<String, Object> criarValoresIniciais() {
		def datas = DateUtils.getStartAndEndMonth(LocalDate.now())
		return criarFiltros("classe","0", "periodo", datas, "ano",LocalDate.now().year,"imprimir", "1")
	}

	@Override
	public DadosParaDownload executar() {
		def periodo = getIntervaloDatas("periodo")
		def empresa = obterEmpresaAtiva()
		def classe = getInteger("classe")
		def contas = getListLong("contas")
		def agrupamento = getListLong("agrupamento")
		def ano = classe == 2 || classe == 3 ? getInteger("ano") : LocalDate.now().year
		BigDecimal saldoAnterior = 0;
		def imprimir = getInteger("imprimir")
		List<TableMap> agrupamentos = new ArrayList<TableMap>()
		def dados = buscarAgrupamentos(contas, agrupamento)
		def titulo = classe == 0 ? "Resumo de Natureza Agrupada" : classe == 1 ? "Natureza Agrupada" : "Resumo Anual de Natureza Agrupada"

		// Busca as contas correntes que tiveram lançamentos, caso não informado no filtro
		String where = getSamWhere().getWherePadrao("WHERE", Dab01.class);

		String sqlCC = "select distinct dab01id "+
				"from dab10 "+
				" LEFT JOIN dab1002 ON dab1002lct = dab10id "+
				" LEFT JOIN dab01 ON dab01id = dab1002cc " +
				where +
				" and dab10data between :dtIni and :dtFin "+
				"order by dab01id ";

		def dtIni = criarParametroSql("dtIni", periodo[0]);
		def dtFin = criarParametroSql("dtFin", periodo[1]);

		contas = contas != null && contas.size() > 0 ? contas : getAcessoAoBanco().obterListaDeLong(sqlCC,dtIni, dtFin);

		// Verifica se foi inserido saldo inicial no filtro
		if(get("saldoAnterior") == null){
			def saldo = 0;
			// Busca os saldos iniciais/anteriores de cada conta
			for(idConta in contas){
				saldo = buscarSaldoAnterior(idConta, periodo);

				if(saldo == 0) saldo = buscarSaldoInicial(idConta);

				saldoAnterior += saldo
			}
		}else{
			saldoAnterior = get("saldoAnterior");
		}


		adicionarParametro("TITULO", titulo)
		adicionarParametro("EMPRESA", empresa.aac10na)
		adicionarParametro("DATAINI", periodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
		adicionarParametro("DATAFIM", periodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
		adicionarParametro("SALDO_ANTERIOR", saldoAnterior)
		adicionarParametro("ANO", ano)

		def totais = 0.0, totaisJan = 0.0, totaisFev = 0.0
		def totaisMar = 0.0, totaisAbr = 0.0, totaisMai = 0.0
		def totaisJun = 0.0, totaisJul = 0.0, totaisAgo = 0.0
		def totaisSet = 0.0, totaisOut = 0.0, totaisNov = 0.0
		def totaisDez = 0.0, temGrupo = false, idGrupo = ""

		Integer grupoP = 0

		for(dado in dados) {
			def mapa = new TableMap()

			if(dado.getInteger("abf11grupo") == 1) {
				temGrupo = true

				idGrupo = dado.getString("abf11codigo").length() <= 2 ? dado.getString("abf11codigo") : idGrupo

				def totalGrupo = totalizarGrupoAgrupamento(dado.getString("abf11codigo"), contas, periodo,classe,ano)
				if( dado.getString("abf11codigo") == idGrupo) {
					totais += totalGrupo
				}

				if (grupoP != dado.getInteger("abf11grupo")){

					grupoP = dado.getInteger("abf11grupo")
					mapa.put("abf11grupo", dado.getInteger("abf11grupo")) // grupo de natureza

				}

				mapa.put("abf11codigo", dado.getString("abf11codigo"))

				mapa.put("abf11nome", dado.getString("abf11nome"))
				mapa.put("total", totalGrupo ?: BigDecimal.ZERO)

				if(classe == 2 || classe == 3) criarLinha(mapa,ano, dado, contas)
				if((classe == 2 || classe == 3) && dado.getString("abf11codigo") == idGrupo) {

					totaisJan += mapa.getBigDecimal("total_jan")
					totaisFev += mapa.getBigDecimal("total_fev")
					totaisMar += mapa.getBigDecimal("total_mar")
					totaisAbr += mapa.getBigDecimal("total_abr")
					totaisMai += mapa.getBigDecimal("total_mai")
					totaisJun += mapa.getBigDecimal("total_jun")
					totaisJul += mapa.getBigDecimal("total_jul")
					totaisAgo += mapa.getBigDecimal("total_ago")
					totaisSet += mapa.getBigDecimal("total_set")
					totaisOut += mapa.getBigDecimal("total_out")
					totaisNov += mapa.getBigDecimal("total_nov")
					totaisDez += mapa.getBigDecimal("total_dez")
				}
				def total = totaisJan +totaisFev+totaisMar+totaisAbr+totaisMai+totaisJun+totaisJul+totaisAgo+totaisSet+totaisOut+totaisNov+totaisDez
				agrupamentos.add(mapa)

			} else {
				def totalGrupo = totalizarGrupoAgrupamento(dado.getString("abf11codigo"), contas, periodo, classe,ano)

				mapa.put("abf11codigo", dado.getString("abf11codigo"))
				//mapa.put("abf11grupo", dado.getInteger("abf11grupo"))
				mapa.put("abf11nome", dado.getString("abf11nome"))
				mapa.put("total", totalGrupo ?: BigDecimal.ZERO)
				grupoP = dado.getInteger("abf11grupo")

				if(classe == 2 || classe == 3) criarLinha(mapa,ano, dado, contas)

				agrupamentos.add(mapa)

				if(classe == 1) {
					def naturezas = buscarNaturezas(dado.getLong("abf11id"))

					for(natureza in naturezas) {
						def soma = totalizarNaturezas(natureza.getLong("abf10id"),dado.getLong("abf11id"),contas,periodo)
						def tm = new TableMap()
						tm.put("abf11codigo", dado.getString("abf11codigo"))
						tm.put("abf11nome", dado.getString("abf11nome"))
						tm.put("abf10codigo", natureza.getString("abf10codigo"))
						tm.put("abf10nome", natureza.getString("abf10nome"))
						tm.put("total_nat", soma)
						agrupamentos.add(tm)
					}
				}
			}
		}
		def somaTotal = new TableMap()
		somaTotal.put("totais_jan", totaisJan)
		somaTotal.put("totais_fev", totaisFev)
		somaTotal.put("totais_mar", totaisMar)
		somaTotal.put("totais_abr", totaisAbr)
		somaTotal.put("totais_mai", totaisMai)
		somaTotal.put("totais_jun", totaisJun)
		somaTotal.put("totais_jul", totaisJul)
		somaTotal.put("totais_ago", totaisAgo)
		somaTotal.put("totais_set", totaisSet)
		somaTotal.put("totais_out", totaisOut)
		somaTotal.put("totais_nov", totaisNov)
		somaTotal.put("totais_dez", totaisDez)

		if(!temGrupo) {
			totais = 0.0
			for(dado in dados) {
				def totalGrupo = totalizarGrupoAgrupamento(dado.getString("abf11codigo"), contas, periodo,classe,ano)
				totais += totalGrupo
			}
		}

		somaTotal.put("Totais_Total", totais)
		agrupamentos.add(somaTotal)
		def aux = 0
		List removeIndex = new ArrayList()
		for(grupo in agrupamentos) {
			if(grupo.getBigDecimal("total") != null && grupo.getBigDecimal("total") == 0) {
				removeIndex.push(aux)
			}
			if(grupo.getBigDecimal("total_nat") != null && grupo.getBigDecimal("total_nat") == 0 ) {
				removeIndex.push(aux)
			}
			aux++
		}

		for(index in removeIndex) {
			agrupamentos.remove(index)
		}
		if(imprimir == 0) {
			def nomeRelatorio = classe == 2 ? "SCF_NaturezasAgrupadasMesXLS" : "SCF_NaturezasAgrupadas"
			return gerarXLSX(nomeRelatorio, agrupamentos)
		}else {
			def nomeRelatorio = ""
			if(classe == 0 || classe == 1) {
				nomeRelatorio = "SCF_NaturezasAgrupadas"
			}else if(classe == 2){
				nomeRelatorio = "SCF_NaturezasAgrupadasMes"
			}else {
				nomeRelatorio = "SCF_NaturezasAgrupadasMesGrafico"
			}
			return gerarPDF(nomeRelatorio, agrupamentos)
		}

	}

	private void criarLinha (TableMap mapa, int ano, TableMap dado, List<Long> contas) {
		if(ano == null) ano = LocalDate.now().year
		mapa.put( "total_jan", somarSaldoMes(dado.getString("abf11codigo"), contas, 1, ano) )
		mapa.put( "total_fev", somarSaldoMes(dado.getString("abf11codigo"), contas, 2, ano) )
		mapa.put( "total_mar", somarSaldoMes(dado.getString("abf11codigo"), contas, 3, ano) )
		mapa.put( "total_abr", somarSaldoMes(dado.getString("abf11codigo"), contas, 4, ano) )
		mapa.put( "total_mai", somarSaldoMes(dado.getString("abf11codigo"), contas, 5, ano) )
		mapa.put( "total_jun", somarSaldoMes(dado.getString("abf11codigo"), contas, 6, ano) )
		mapa.put( "total_jul", somarSaldoMes(dado.getString("abf11codigo"), contas, 7, ano) )
		mapa.put( "total_ago", somarSaldoMes(dado.getString("abf11codigo"), contas, 8, ano) )
		mapa.put( "total_set", somarSaldoMes(dado.getString("abf11codigo"), contas, 9, ano) )
		mapa.put( "total_out", somarSaldoMes(dado.getString("abf11codigo"), contas, 10, ano) )
		mapa.put( "total_nov", somarSaldoMes(dado.getString("abf11codigo"), contas, 11, ano) )
		mapa.put( "total_dez", somarSaldoMes(dado.getString("abf11codigo"), contas, 12, ano) )
	}

	private List<TableMap> buscarAgrupamentos(List<Long> contas, List<Long> agrupamentos) {
		def whereAgrupamento = agrupamentos != null && agrupamentos.size() > 0 ? " AND abf11id IN (:agrupamentos) " : ""
		def sql = " SELECT abf11id, abf11codigo, abf11nome, abf11grupo FROM abf11 " +
				obterWherePadrao("abf11", "WHERE") + whereAgrupamento +
				" ORDER BY abf11codigo"


		def p1 = agrupamentos != null && agrupamentos.size() > 0 ? criarParametroSql("agrupamentos", agrupamentos) : null
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
	}

	private List<TableMap> buscarNaturezas(Long abf11id){

		def sql = " select abf1101id, abf10id, abf10codigo, abf10nome from abf1101 "+
				" inner join abf10 on abf10id = abf1101nat "+
				" where abf1101agrup = :id"+
				" order by abf10codigo"
		def p1 =  criarParametroSql("id", abf11id)
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)

	}

	private BigDecimal totalizarNaturezas(Long abf1101id, Long abf11id, List<Long> contas, LocalDate[] periodo) {
		def whereCcs = contas != null && contas.size() > 0 ? " AND dab1002cc IN (:contas) " : ""
		def wherePeriodo = periodo != null && periodo.length > 0 ? " AND dab10data BETWEEN :dtIni AND :dtFin " : ""

		def sql = " SELECT SUM(CASE WHEN dab10mov = 0 THEN dab10011valor ELSE (dab10011valor * -1) END ) AS total FROM Dab10011 " +
				" LEFT JOIN Abf1101 ON dab10011nat = abf1101nat " +
				" LEFT JOIN Dab1001 ON dab1001id = dab10011depto" +
				" LEFT JOIN Dab10 ON  dab10id = dab1001lct" +
				" LEFT JOIN Abf10 ON abf10id = dab10011nat " +
				" LEFT JOIN dab1002 ON dab1002lct = dab10id "+
				" WHERE abf1101nat = :abf1101id " +
				" AND abf1101agrup = :abf11id " +
				whereCcs + wherePeriodo +
				" GROUP BY abf10id, abf10codigo, abf10nome" +
				" ORDER BY abf10codigo"

		def p1 = contas != null && contas.size() > 0 ? criarParametroSql("contas", contas) : null
		def p2 = periodo != null && periodo.length > 0 ? criarParametroSql("dtIni", periodo[0]) : null
		def p3 = periodo != null && periodo.length > 0 ? criarParametroSql("dtFin", periodo[1]) : null

		return getAcessoAoBanco().obterBigDecimal(sql, criarParametroSql("abf1101id", abf1101id), criarParametroSql("abf11id", abf11id), p1, p2, p3)
	}

	private BigDecimal totalizarGrupoAgrupamento(String abf11codigo, List<Long> contas, LocalDate[] periodo, int impressao, int ano) {
		def whereCcs = contas != null && contas.size() > 0 ? " AND dab1002cc IN (:contas) " : ""
		def wherePeriodo = periodo != null && periodo.length > 0 ? " AND dab10data BETWEEN :dtIni AND :dtFin " : ""
		def dataAnoIni = LocalDate.of(ano, 01, 01)
		def dataAnoFim = LocalDate.of(ano, 12, 31)

		def sql = " SELECT SUM(CASE WHEN dab10mov = 0 THEN dab10011valor ELSE (dab10011valor * -1) END ) FROM Dab10011 " +
				" LEFT JOIN Dab1001 ON dab1001id = dab10011depto" +
				" LEFT JOIN Dab10 ON dab10id = dab1001lct" +
				" LEFT JOIN dab1002 ON dab1002lct = dab10id "+
				" LEFT JOIN Abf1101 ON dab10011nat = abf1101nat " +
				" LEFT JOIN Abf11 ON abf11id = abf1101agrup " +
				" WHERE abf11codigo LIKE :abf11codigo  " +
				whereCcs + wherePeriodo + obterWherePadrao("Abf11")

		def p1 = contas != null && contas.size() > 0 ? criarParametroSql("contas", contas) : null
		def p2 = periodo != null && periodo.length > 0 ? criarParametroSql("dtIni", impressao == 2 || impressao == 3 ? dataAnoIni : periodo[0]) : null
		def p3 = periodo != null && periodo.length > 0 ? criarParametroSql("dtFin", impressao == 2 || impressao == 3 ? dataAnoFim : periodo[1]) : null

		return getAcessoAoBanco().obterBigDecimal(sql, criarParametroSql("abf11codigo", abf11codigo+'%'), p1, p2, p3)
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

	private BigDecimal somarSaldoMes(String abf11codigo, List<Long> contas, int mes, int ano){
		def data = DateUtils.getStartAndEndMonth(LocalDate.create(ano, mes, 10) )
		def whereCcs = contas != null && contas.size() > 0 ? " AND dab1002cc IN (:contas) " : ""

		def sql = " SELECT SUM(CASE WHEN dab10mov = 0 THEN dab10011valor ELSE (dab10011valor * -1) END ) FROM Dab10011 " +
				" INNER JOIN Dab1001 ON dab1001id = dab10011depto " +
				" INNER JOIN Dab10 ON  dab10id = dab1001lct " +
				" LEFT JOIN dab1002 ON dab1002lct = dab10id "+
				" INNER JOIN Abf1101 ON dab10011nat = abf1101nat "+
				" INNER JOIN Abf11 ON abf11id = abf1101agrup "+
				" WHERE abf11codigo LIKE :abf11codigo " +
				" AND dab10data BETWEEN :dataini AND :datafin " +
				obterWherePadrao("Abf11") + whereCcs

		def p1 = contas != null && contas.size() > 0 ? criarParametroSql("contas", contas) : null
		def p2 = criarParametroSql("abf11codigo", abf11codigo+'%')
		def p3 = criarParametroSql("dataini", data[0])
		def p4 = criarParametroSql("datafin", data[1])

		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2,p3,p4)
	}

	@Override
	public String getNomeTarefa() {
		return "SCF - Naturezas Agrupadas"
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIE5hdHVyZXphcyBBZ3J1cGFkYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNDRiAtIE5hdHVyZXphcyBBZ3J1cGFkYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=