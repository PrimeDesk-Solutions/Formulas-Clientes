// Saldo Fisico e Financeiro corrigido

package Profer.relatorios.sce;

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.Parametros
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcc02
import sam.model.entities.bc.Bcc0201
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCE_SaldoFisicoFinanceiro extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCE - Saldo Físico e Financeiro";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap()
		LocalDate data = MDate.date()
		filtrosDefault.put("data", data)
		filtrosDefault.put("totalGrau", true)
		filtrosDefault.put("inativos", true)
		filtrosDefault.put("entidade3", "0")
		filtrosDefault.put("loteIni", "")
		filtrosDefault.put("loteFin", "")
		filtrosDefault.put("serieIni", "")
		filtrosDefault.put("serieFin", "")
		filtrosDefault.put("impressao", "0")
		filtrosDefault.put("detalhamento", "0")

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Integer> tpsItem = getListInteger("mpm")
		List<Long> classes = getListLong("classe")
		List<Long> idStatu = getListLong("status")
		List<Long> idLocal = getListLong("local")
		List<Long> idsCrit = getListLong("criterios")
		String itemIni = getString("itemIni")
		String itemFim = getString("itemFim")
		LocalDate dataSdo = getLocalDate("data")
		Boolean totGrau = getBoolean("totalGrau")
		Boolean inativo = getBoolean("inativos")
		Integer cpoEnt3 = getInteger("entidade3")
		Integer impressao = getInteger("impressao")
		Boolean movimentaEstoque = getBoolean("itensMovimentaEstoque");
		Boolean naoMovimentaEstoque = getBoolean("itensNaoMovimentaEstoque");
		List<Integer> listMovEstoque = []
		String loteIni = getString("loteIni");
		String loteFin = getString("loteFin");
		String serieIni = getString("serieIni");
		String serieFin = getString("serieFin");
		Boolean naoImprimirSaldoZero = getBoolean("itemSaldoZero");
		Integer detalhamento = getInteger("detalhamento");

		if(movimentaEstoque) listMovEstoque.add(1);
		if(naoMovimentaEstoque) listMovEstoque.add(0);


		adicionarParametro("TITULO_RELATORIO", "Saldo Físico e Financeiro")
		adicionarParametro("PERIODO", "Data: " + DateUtils.formatDate(dataSdo, "dd/MM/yyyy"))
		Aac10 aac10 = obterEmpresaAtiva()
		adicionarParametro("EMPRESA", aac10.aac10codigo + "-" + aac10.aac10na)
		String nomeCtrl0 = buscarConteudoParametro(Parametros.ABM15_NOMECTRL0.getAplic(), Parametros.ABM15_NOMECTRL0.getParam())
		String nomeCtrl1 = buscarConteudoParametro(Parametros.ABM15_NOMECTRL1.getAplic(), Parametros.ABM15_NOMECTRL1.getParam())
		String nomeCtrl2 = buscarConteudoParametro(Parametros.ABM15_NOMECTRL2.getAplic(), Parametros.ABM15_NOMECTRL2.getParam())
		adicionarParametro("CTRL0", nomeCtrl0)
		adicionarParametro("CTRL1", nomeCtrl1)
		adicionarParametro("CTRL2", nomeCtrl2)

		def paramDataAtual = buscarConteudoParametro(Parametros.BC_DATAATUAL.getAplic(), Parametros.BC_DATAATUAL.getParam())
		if(StringUtils.isNullOrEmpty(paramDataAtual)) interromper("Não foi localizado o conteúdo do parametro BC-DataAtual")
		def dataAtualEstoque = DateUtils.parseDate(paramDataAtual)

		def grupos = new HashMap<Integer, List<Integer>>()
		criarEstruturaItens(grupos)

		def itens = buscarItensParaComporSaldo(itemIni, itemFim, classes, inativo, idsCrit,tpsItem,listMovEstoque)

		def dados = new ArrayList<TableMap>()

		if (dataSdo.compareTo(dataAtualEstoque) >= 0) {
			buscarSaldosAtuaisEstoquePorItens(itens, dados, grupos, itemIni, itemFim, totGrau, cpoEnt3, idStatu,idLocal, loteIni, loteFin, serieIni, serieFin,naoImprimirSaldoZero,detalhamento)
		} else {
			List<Abm01> items = buscarItens(itemIni,itemFim,tpsItem,classes,inativo,idsCrit,listMovEstoque)
			buscarSaldosReatroativosPorItens(items, dados, grupos, dataSdo, totGrau, cpoEnt3, idStatu,idLocal, loteIni, loteFin, serieIni, serieFin,naoImprimirSaldoZero,detalhamento)
		}

		if(detalhamento == 0 && impressao == 0 ){
			return gerarPDF("SCE_SaldoFisicoFinanceiro_Sintetico_PDF", dados);
		}else if(detalhamento == 0 && impressao == 1){
			return gerarXLSX("SCE_SaldoFisicoFinanceiro_Sintetico_Excel", dados);
		}else if(detalhamento == 1 && impressao == 0){
			return gerarPDF("SCE_SaldoFisicoFinanceiro_Analitico_PDF", dados);
		}else{
			return gerarXLSX("SCE_SaldoFisicoFinanceiro_Analitico_Excel", dados);
		}

	}

	private void criarEstruturaItens(Map<Integer, List<Integer>> grupos) {
		List<Integer> grupoMat = new ArrayList()
		String estrMat = buscarConteudoParametro(Parametros.ABM01_ESTRCODMAT.getAplic(), Parametros.ABM01_ESTRCODMAT.getParam())
		if(estrMat != null) {
			getEstrutura(estrMat, grupoMat, 0)
			grupos.put(0, grupoMat)
		}

		List<Integer> grupoProd = new ArrayList()
		String estrProd = buscarConteudoParametro(Parametros.ABM01_ESTRCODPROD.getAplic(), Parametros.ABM01_ESTRCODPROD.getParam())
		if(estrProd != null) {
			getEstrutura(estrProd, grupoProd, 0)
			grupos.put(1, grupoProd)
		}

		List<Integer> grupoMerc = new ArrayList()
		String estrMerc = buscarConteudoParametro(Parametros.ABM01_ESTRCODMERC.getAplic(), Parametros.ABM01_ESTRCODMERC.getParam())
		if(estrMerc != null) {
			getEstrutura(estrMerc, grupoMerc, 0)
			grupos.put(2, grupoMerc)
		}
	}

	private void getEstrutura(String estrutura, List<Integer> grupos, int tamanhoTotal) {
		tamanhoTotal = tamanhoTotal + StringUtils.substringBeforeFirst(estrutura, "|").length()
		grupos.add(Integer.valueOf(tamanhoTotal))
		estrutura = StringUtils.substringAfterFirst(estrutura, "|")
		if(!estrutura.contains("|")) {
			grupos.add(Integer.valueOf(tamanhoTotal + estrutura.length()))
			tamanhoTotal = tamanhoTotal + estrutura.length()
		}else {
			getEstrutura(estrutura, grupos, tamanhoTotal)
		}
	}

	private List<Abm01> buscarItensParaComporSaldo(String itemIni, String itemFim, List<Long> idsClasse, boolean inativos, List<Long> criterios, List<Integer> tipos, List<Integer> listMovEstoque) {
		Criterion critItem = itemIni != null && itemFim != null ? Criterions.between("abm01codigo", itemIni, itemFim) : itemIni != null && itemFim == null ? Criterions.ge("abm01codigo", itemIni): itemIni == null && itemFim != null ? Criterions.le("abm01codigo", itemFim) : Criterions.isTrue()
		Criterion critClas = idsClasse != null && idsClasse.size() > 0 ? Criterions.in("abm01classe", idsClasse) : Criterions.isTrue()
		Criterion critInat = !inativos ? Criterions.isNull("abm01di") : Criterions.isTrue()
		Criterion critCrit = criterios != null && criterios.size() > 0 ? Criterions.in("abm0102criterio", criterios) : Criterions.isTrue()
		Criterion critTipos = tipos != null && tipos.size() > 0 ? Criterions.in("abm01tipo",tipos) : Criterions.isTrue()
		Criterion crtitMovEst = listMovEstoque != null && listMovEstoque.size() > 0 ? Criterions.in("abm11movest",listMovEstoque ) : Criterions.isTrue();

		return getSession().createCriteria(Abm01.class)
				.addFields("DISTINCT abm01id, abm01tipo, abm01codigo")
				.addJoin(Joins.join("Abm0102", "abm0102item = abm01id").left(true))
				.addJoin(Joins.join("Abm0101", "abm0101item = abm01id").left(true))
				.addJoin(Joins.join("Abm11","abm11id = abm0101estoque").left(true))
				.addWhere(critItem).addWhere(critClas)
				.addWhere(critInat).addWhere(critCrit)
				.addWhere(crtitMovEst)
				.addWhere(Criterions.eq("abm01grupo", Abm01.NAO))
				.addWhere(critTipos).setOrder(" abm01tipo, abm01codigo ")
				.addWhere(Criterions.eq("abm0101empresa", obterEmpresaAtiva().aac10id))
				.getList(ColumnType.ENTITY)
	}

	private void buscarSaldosAtuaisEstoquePorItens(List<Abm01> abm01s, List<TableMap> dados, Map<Integer, List<Integer>> grupos, String itemIni, String itemFim, boolean totGrau, Integer cpoEnt3, List<Long> idStatu,List<Long> idLocal,String loteIni,String loteFin, String serieIni, String serieFin,Boolean naoImprimirSaldoZero,Integer detalhamento) {
		for (abm01 in abm01s) {
			def saldos = detalhamento == 0 ? buscarSaldoAtualItemSintetico(abm01.abm01id, idStatu,idLocal, cpoEnt3) : buscarSaldoAtualItemAnalitico(abm01.abm01id, idStatu,idLocal, cpoEnt3,loteIni,loteFin, serieIni, serieFin)
			if(saldos.size() == 0 && naoImprimirSaldoZero) continue

			def pmu = getSession().createCriteria(Abm0101.class)
					.addFields("abm0101pmu")
					.addWhere(Criterions.eq("abm0101item", abm01.abm01id))
					.addWhere(Criterions.eq("abm0101empresa", obterEmpresaAtiva().aac10id))
					.get(ColumnType.BIG_DECIMAL)

			pmu = pmu ?: BigDecimal.ZERO

			for(saldo in saldos) {
				def valor = pmu * saldo.getBigDecimal_Zero("quantidade")
				saldo.put("custoMedio", pmu)
				saldo.put("valor", valor)
			}

			if(saldos.size() == 0 ){
				TableMap itemSemSaldo = buscarItensComSaldoZero(abm01.abm01id);
				itemSemSaldo = itemSemSaldo == null ? new TableMap() : itemSemSaldo;
				itemSemSaldo.put("quantidade", 0)
				itemSemSaldo.put("valor", 0)
				dados.add(itemSemSaldo)
			}

			dados.addAll(saldos)
		}

		if(totGrau )totalizarAgrupamentosDeItem(dados, grupos, totGrau, idStatu, true, null)

	}

	private List<TableMap> buscarSaldoAtualItemSintetico(Long abm01id, List<Long> status,List<Long> idLocal, Integer cpoEnt3) {
		def whereStatus = status != null && status.size() > 0 ? " AND bcc02status IN (:status) " : ""
		String whereLocal = idLocal != null && idLocal.size() > 0 ? "and (bcc02ctrl0 in (:idLocal) or bcc02ctrl1 in (:idLocal) or bcc02ctrl1 in (:idLocal) )" : ""
		def cpoEntidade = cpoEnt3 == 0 ? " abe01codigo " : " abe01na "

		def sql = " SELECT abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, " +
				" abb01num, " + cpoEntidade + " AS entidade, COALESCE(SUM(bcc02qt), 0) AS quantidade " +
				" FROM bcc02 " +
				" INNER JOIN Abm01 ON abm01id = bcc02item " +
				" LEFT JOIN Aam06 ON aam06id = abm01umu " +
				" LEFT JOIN Abb01 ON abb01id = bcc02centralEst " +
				" LEFT JOIN Abe01 ON abe01id = abb01ent " +
				" WHERE abm01id = :abm01id " +
				whereStatus + whereLocal+ obterWherePadrao("Bcc02") +
				" GROUP BY abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, " +
				"abb01num, " + cpoEntidade + " ORDER BY abm01tipo, abm01codigo"

		def p1 = status != null && status.size() > 0 ? criarParametroSql("status", status) : null
		def p2 = criarParametroSql("abm01id", abm01id);
		def p3 = idLocal != null && idLocal.size() > 0 ? criarParametroSql("idLocal", idLocal) : null

		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2,p3)
	}


	private List<TableMap> buscarSaldoAtualItemAnalitico(Long abm01id, List<Long> status, List<Long> idLocal, Integer cpoEnt3,String loteIni,String loteFin, String serieIni, String serieFin) {
		def whereStatus = status != null && status.size() > 0 ? " AND bcc02status IN (:status) " : ""
		String whereLocal = idLocal != null && idLocal.size() > 0 ? "and (bcc02ctrl0 in (:idLocal) or bcc02ctrl1 in (:idLocal) or bcc02ctrl1 in (:idLocal) ) " : ""
		def cpoEntidade = cpoEnt3 == 0 ? " abe01codigo " : " abe01na "
		String whereLoteIni = !loteIni.isEmpty() ? " and bcc0201lote >= :loteIni " : "";
		String whereLoteFin = !loteFin.isEmpty() ? "and bcc0201lote <= :loteFin " : "";
		String whereSerieIni = !serieIni.isEmpty() ? "and bcc0201serie >= :serieIni " : "";
		String whereSerieFin = !serieFin.isEmpty() ? "and bcc0201serie <= :serieFin " : "";


		def sql = " SELECT abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, aam04id, aam04codigo,bcc0201lote as lote, bcc0201serie as serie, " +
				" ctr0.abm15id AS abm15id0, ctr0.abm15nome AS abm15nome0, " +
				" ctr1.abm15id AS abm15id1, ctr1.abm15nome AS abm15nome1, " +
				" ctr2.abm15id AS abm15id2, ctr2.abm15nome AS abm15nome2, " +
				" abb01num, " + cpoEntidade + " AS entidade, COALESCE(SUM(bcc0201qt), 0) AS quantidade, bcc0201validade as validade, bcc0201fabric as fabricacao " +
				" FROM bcc02 " +
				" INNER JOIN Abm01 ON abm01id = bcc02item " +
				" LEFT JOIN Aam06 ON aam06id = abm01umu " +
				" INNER JOIN Aam04 ON aam04id = bcc02status " +
				" LEFT JOIN Abm15 ctr0 ON ctr0.abm15id = bcc02ctrl0 " +
				" LEFT JOIN Abm15 ctr1 ON ctr1.abm15id = bcc02ctrl1 " +
				" LEFT JOIN Abm15 ctr2 ON ctr2.abm15id = bcc02ctrl2 " +
				" LEFT JOIN Abb01 ON abb01id = bcc02centralEst " +
				" LEFT JOIN Abe01 ON abe01id = abb01ent " +
				" INNER JOIN bcc0201 on bcc0201saldo = bcc02id " +
				" WHERE abm01id = :abm01id " +
				whereStatus +
				whereLocal +
				obterWherePadrao("Bcc02") +
				whereLoteIni+
				whereLoteFin+
				whereSerieIni+
				whereSerieFin+
				" GROUP BY abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, aam04id, aam04codigo,bcc0201lote, bcc0201serie,bcc0201validade,bcc0201fabric, " +
				" ctr0.abm15id, ctr0.abm15nome, ctr1.abm15id, ctr1.abm15nome, ctr2.abm15id, " +
				" ctr2.abm15nome, abb01num, " + cpoEntidade + " ORDER BY abm01tipo, abm01codigo"

		def p1 = status != null && status.size() > 0 ? criarParametroSql("status", status) : null
		def p2 = criarParametroSql("abm01id", abm01id);
		def p3 = idLocal != null && idLocal.size() > 0 ? criarParametroSql("idLocal", idLocal) : null

		Parametro parametroLoteIni = !loteIni.isEmpty() ? criarParametroSql("loteIni", loteIni) : null;
		Parametro parametroLoteFin = !loteFin.isEmpty() ? criarParametroSql("loteFin", loteFin) : null;
		Parametro parametroSerieIni = !serieIni.isEmpty() ? criarParametroSql("serieIni", serieIni) : null;
		Parametro parametroSerieFin = !serieFin.isEmpty() ? criarParametroSql("serieFin", serieFin) : null;


		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1,p2,p3,parametroLoteIni,parametroLoteFin,parametroSerieIni,parametroSerieFin)
	}

	private List<Abm01> buscarItens(String itemIni, String itemFim,List<Integer> tipoItem,List<Long> idsClasse, boolean inativos, List<Long> criterios,List<Integer>listMovEstoque) {
		Criterion critItemIni = itemIni != null ? Criterions.ge("abm01codigo", itemIni) : null
		Criterion critItemFim = itemFim != null ? Criterions.le("abm01codigo", itemFim) : null
		Criterion critTipo = tipoItem != null && !tipoItem.contains(-1) ? Criterions.in("abm01tipo", tipoItem) : Criterions.isTrue();
		Criterion critClas = idsClasse != null && idsClasse.size() > 0 ? Criterions.in("abm01classe", idsClasse) : Criterions.isTrue();
		Criterion critInat = !inativos ? Criterions.isNull("abm01di") : Criterions.isTrue();
		Criterion critCrit = criterios != null && criterios.size() > 0 ? Criterions.in("abm0102criterio", criterios) : Criterions.isTrue();
		Criterion crtitMovEst = listMovEstoque != null && listMovEstoque.size() > 0 ? Criterions.in("abm11movest",listMovEstoque ) : Criterions.isTrue();


		return getSession().createCriteria(Abm01.class)
		//.addJoin(Joins.join("Abm0102", "abm0102item = abm01id").left(true))
				.addJoin(Joins.join("Abm0101", "abm0101item = abm01id").left(true))
				.addJoin(Joins.join("Abm11","abm11id = abm0101estoque").left(true))
				.addWhere(getSamWhere().getCritPadrao(Abm01.class))
				.addWhere(critItemIni).addWhere(critItemFim)
				.addWhere(critTipo).addWhere(critClas)
				.addWhere(crtitMovEst)
				.addWhere(Criterions.eq("abm0101empresa", obterEmpresaAtiva().aac10id))
				.addWhere(critInat)//.addWhere(critCrit)
				.setOrder("abm01tipo, abm01codigo")
				.getList(ColumnType.ENTITY)
	}

	private List<TableMap> buscarLancamentosItemSintetico(Long abm01id, LocalDate dataSaldo, List<Long> idStatu,List<Long> idLocal) {
		def whereStatus = idStatu != null && idStatu.size() > 0  ? " AND bcc01status in (:idStatu) " : ""
		def whereCtrl0 = idLocal != null && idLocal.size() > 0 ?  "AND bcc01ctrl0 in (:idLocal) " : ""
		def whereCtrl1 = idLocal != null && idLocal.size() > 0 ?  "AND bcc01ctrl1 in (:idLocal) " : ""
		def whereCtrl2 = idLocal != null && idLocal.size() > 0 ?  "AND bcc01ctrl2 in (:idLocal) " : ""

		def paramStatus = idStatu != null && idStatu.size() > 0 ? criarParametroSql("idStatu", idStatu) : null
		def paramLocal = idLocal != null && idLocal.size() > 0 ? criarParametroSql("idLocal", idLocal) : null;


		String sql =  "SELECT abm01tipo, abm01codigo,abm01na, abm01grupo, aam06codigo, "+
				"SUM(bcc01qtPS) AS quantidade FROM bcc01 "+
				"INNER JOIN Abm01 ON abm01id = bcc01item "+
				"LEFT JOIN Aam06 ON aam06id = abm01umu "+
				"WHERE abm01id = :abm01id  "+
				"AND bcc01data <= :dataSaldo "+
				whereStatus + whereCtrl0 + whereCtrl1 + whereCtrl2 +
				"GROUP BY abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo "+
				"ORDER BY abm01tipo, abm01codigo "

		Parametro p1 = Parametro.criar("abm01id",abm01id)
		Parametro p2 = Parametro.criar("dataSaldo",dataSaldo)

		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2, paramStatus, paramLocal)
	}

	private List<TableMap> buscarLancamentosItemAnalitico(Long abm01id, Integer cpoEnt3,dataSaldo,List<Long> idStatu,List<Long> idLocal,String loteIni,String loteFin, String serieIni, String serieFin) {

		def whereStatus = idStatu != null && idStatu.size() > 0  ? " AND bcc01status in (:idStatu) " : ""
		def whereCtrl0 = idLocal != null && idLocal.size() > 0 ?  "AND bcc01ctrl0 in (:idLocal) " : ""
		def whereCtrl1 = idLocal != null && idLocal.size() > 0 ?  "AND bcc01ctrl1 in (:idLocal) " : ""
		def whereCtrl2 = idLocal != null && idLocal.size() > 0 ?  "AND bcc01ctrl2 in (:idLocal) " : ""

		String whereLote = loteIni.isEmpty() && loteFin.isEmpty() ? "" : "AND bcc01lote = :lote "
		String whereSerie = serieIni.isEmpty() && serieFin.isEmpty() ? "" : "AND bcc01serie = :serie "

		def campoEnt = cpoEnt3 == 0 ? " abe01codigo " : " abe01na "

		def sql = " SELECT abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, aam04id, aam04codigo,bcc01validade as validade, bcc01fabric as fabricacao, " +
				" ctr0.abm15id AS abm15id0, ctr0.abm15nome AS abm15nome0, bcc01centralEst, " +
				" ctr1.abm15id AS abm15id1, ctr1.abm15nome AS abm15nome1, " +
				" ctr2.abm15id AS abm15id2, ctr2.abm15nome AS abm15nome2, " +
				" abb01num, " + campoEnt + " AS entidade, " +
				" SUM(bcc01qtPS) AS quantidade, bcc01lote as lote, bcc01serie as serie FROM bcc01 " +
				" INNER JOIN Abm01 ON abm01id = bcc01item " +
				" LEFT JOIN Aam06 ON aam06id = abm01umu " +
				" INNER JOIN Aam04 ON aam04id = bcc01status " +
				" LEFT JOIN Abm15 ctr0 ON ctr0.abm15id = bcc01ctrl0 " +
				" LEFT JOIN Abm15 ctr1 ON ctr1.abm15id = bcc01ctrl1 " +
				" LEFT JOIN Abm15 ctr2 ON ctr2.abm15id = bcc01ctrl2 " +
				" LEFT JOIN Abb01 ON abb01id = bcc01centralEst " +
				" LEFT JOIN Abe01 ON abe01id = abb01ent " +
				" WHERE abm01id = :abm01id " +
				" AND bcc01data <= :data " +
				obterWherePadrao("Bcc01") +
				whereStatus + whereCtrl0 + whereCtrl1 + whereCtrl2 +
				whereLote + whereSerie +
				" GROUP BY abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, aam04id, aam04codigo, bcc01centralEst, bcc01validade, bcc01fabric, " +
				" ctr0.abm15id, ctr0.abm15nome, ctr1.abm15id, ctr1.abm15nome, ctr2.abm15id, ctr2.abm15nome, abb01num,bcc01lote,bcc01serie, " + campoEnt +
				" ORDER BY aam04codigo,abm01tipo, abm01codigo"

		def p1 = idStatu != null && idStatu.size() > 0 ? criarParametroSql("idStatu", idStatu) : null
		def p2 = criarParametroSql("abm01id", abm01id)
		def p3 = criarParametroSql("data", dataSaldo)
		def p4 = idLocal != null && idLocal.size() > 0 ? criarParametroSql("idLocal", idLocal) : null;
		def p5 = loteIni.isEmpty() ? null : Parametro.criar("loteIni", loteIni);
		def p6 = loteFin.isEmpty() ? null : Parametro.criar("loteFin", loteFin);

		def p7 = serieIni.isEmpty() ? null : Parametro.criar("serieIni",serieIni);
		def p8 = serieFin.isEmpty() ? null : Parametro.criar("serieFin",serieFin);

		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3,p4,p5,p6,p7,p8)
	}

	private void buscarSaldosReatroativosPorItens(List<Abm01> abm01s, List<TableMap> dados, Map<Integer, List<Integer>> grupos, LocalDate dataSaldo, Boolean totGrau, Integer cpoEnt3, List<Long> idStatu,List<Long> idLocal, String loteIni, String loteFin, String serieIni, String serieFin,Boolean naoImprimirSaldoZero, Integer detalhamento) {
		for (abm01 in abm01s) {
			List<TableMap> lancamentos = detalhamento == 0 ? buscarLancamentosItemSintetico(abm01.abm01id,dataSaldo, idStatu, idLocal) : buscarLancamentosItemAnalitico(abm01.abm01id, cpoEnt3,dataSaldo,idStatu, idLocal,loteIni,loteFin, serieIni, serieFin) ;
			List<TableMap> tmRegistros = new ArrayList<>()
			BigDecimal pmu = buscarPrecoMedioItemNaData(abm01.abm01id, dataSaldo)

			def valor;
			for(lancamento in lancamentos){
				BigDecimal quantidade = lancamento.getBigDecimal_Zero("quantidade")
				valor = lancamento.getBigDecimal_Zero("quantidade") * pmu;
				lancamento.put("valor", valor);
				lancamento.put("custoMedio", pmu)

				if(quantidade == 0 && naoImprimirSaldoZero) continue
				dados.add(lancamento)

//				if(detalhamento == 0){
//					valor = lancamento.getBigDecimal_Zero("quantidade") * pmu;
//					lancamento.put("valor", valor);
//					lancamento.put("custoMedio", pmu)
//					dados.add(lancamento)
//				}else{
//					String lote = lancamento.getString("lote");
//					Long idItem = abm01.abm01id;
//					BigDecimal quantidade = lancamento.getBigDecimal_Zero("quantidade")
//
//					BigDecimal qtdSaldo = buscarSaldoLote(idItem, lote);
//
//					if((qtdSaldo == 0 || quantidade == 0) && naoImprimirSaldoZero) continue
//
//					valor = lancamento.getBigDecimal_Zero("quantidade") * pmu;
//					lancamento.put("valor", valor);
//					lancamento.put("custoMedio", pmu)
//
//					if(quantidade > 0 ) dados.add(lancamento)
//
//				}

			}

		}


		if(totGrau)totalizarAgrupamentosDeItem(dados, grupos, totGrau, idStatu, false, dataSaldo)
	}

	private List<TableMap> buscarLancamentosItemAposData(Long abm01id, List<Long> idStatus, Integer cpoEnt3, LocalDate dataSaldo) {
		def whereStatus = idStatus != null && idStatus.size() > 0 ? " AND bcc01status IN (:status) " : ""

		def campoEnt = cpoEnt3 == 0 ? " abe01codigo " : " abe01na "

		def sql = " SELECT abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, aam04id, aam04codigo, " +
				" ctr0.abm15id AS abm15id0, ctr0.abm15nome AS abm15nome0, bcc01centralEst, " +
				" ctr1.abm15id AS abm15id1, ctr1.abm15nome AS abm15nome1, " +
				" ctr2.abm15id AS abm15id2, ctr2.abm15nome AS abm15nome2, " +
				" abb01num, " + campoEnt + " AS entidade, " +
				" SUM(bcc01qtPS) AS quantidade FROM bcc01 " +
				" INNER JOIN Abm01 ON abm01id = bcc01item " +
				" LEFT JOIN Aam06 ON aam06id = abm01umu " +
				" INNER JOIN Aam04 ON aam04id = bcc01status " +
				" LEFT JOIN Abm15 ctr0 ON ctr0.abm15id = bcc01ctrl0 " +
				" LEFT JOIN Abm15 ctr1 ON ctr1.abm15id = bcc01ctrl1 " +
				" LEFT JOIN Abm15 ctr2 ON ctr2.abm15id = bcc01ctrl2 " +
				" LEFT JOIN Abb01 ON abb01id = bcc01centralEst " +
				" LEFT JOIN Abe01 ON abe01id = abb01ent " +
				" WHERE abm01id = :abm01id " +
				" AND bcc01data > :data " +
				whereStatus + obterWherePadrao("Bcc01") +
				" GROUP BY abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo, aam04id, aam04codigo, bcc01centralEst," +
				" ctr0.abm15id, ctr0.abm15nome, ctr1.abm15id, ctr1.abm15nome, ctr2.abm15id, ctr2.abm15nome, abb01num, " + campoEnt +
				" ORDER BY abm01tipo, abm01codigo"

		def p1 = idStatus != null && idStatus.size() > 0 ? criarParametroSql("status", idStatus) : null
		def p2 = criarParametroSql("abm01id", abm01id)
		def p3 = criarParametroSql("data", dataSaldo)

		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3)
	}

	private void totalizarAgrupamentosDeItem(List<TableMap> dados, Map<Integer, List<Integer>> grupos, Boolean totGrau, List<Long> idStatu, boolean atual, LocalDate dataSdo) {
		if(!totGrau) return
		for(int index = 0; index < dados.size(); index++) {

			List<Integer> grupo = grupos.get(dados.get(index).getInteger("abm01tipo"))

			if(index > dados.size()-1) continue
			String abm01codigo = dados.get(index).getString("abm01codigo")
			Integer abm01tipo = dados.get(index).getInteger("abm01tipo")
			int indexOfAbm01 = grupo.indexOf(abm01codigo.length())

			String param = abm01tipo == 0 ? "ABM01_ESTRCODMAT" : abm01tipo == 1 ? "ABM01_ESTRCODPROD" : "ABM01_ESTRCODMERC"
			if(indexOfAbm01 < 0) interromper("O código do item " + abm01codigo + " não corresponde a estrutura informada no parametro " + param)

			String abm01codigoPai = null
			if(abm01codigo.length() > grupo.get(0)) {
				int indexWhileGrupo = indexOfAbm01
				int indexWhileList = index
				while (abm01codigoPai == null || abm01codigoPai.length() > grupo.get(0)) {
					indexWhileGrupo = indexWhileGrupo - 1
					abm01codigoPai = StringUtils.ajustString(abm01codigo, grupo.get(indexWhileGrupo))
					TableMap abm01Prox = null
					List<Integer> proxGrupo = null
					if(index < dados.size()-1) abm01Prox = dados.get(index + 1)

					if(abm01Prox != null && (abm01Prox.getString("abm01codigo").length() == grupo.get(0) || (abm01Prox.getString("abm01codigo").substring(0, grupo.get(indexWhileGrupo)).equalsIgnoreCase(abm01codigoPai) && abm01Prox.getInteger("abm01tipo") == abm01tipo))) continue

					Abm01 abm01Grupo = getSession().createQuery(" SELECT abm01id, abm01tipo, abm01codigo, abm01na, abm01grupo FROM Abm01 WHERE abm01codigo = :abm01codigoPai AND abm01tipo = :abm01tipo " + getSamWhere().getWherePadrao("AND", Abm01.class))
							.setParameter("abm01codigoPai", abm01codigoPai)
							.setParameter("abm01tipo", dados.get(index).getInteger("abm01tipo"))
							.getUniqueResult(ColumnType.ENTITY)

					if(abm01Grupo == null) continue
					TableMap totalizacao = new TableMap()
					totalizacao.put("abm01tipo", abm01Grupo.abm01tipo)
					totalizacao.put("abm01codigo", abm01Grupo.abm01codigo)
					totalizacao.put("abm01na", abm01Grupo.abm01na)
					totalizacao.put("abm01grupo", abm01Grupo.getInteger("abm01grupo"))

					totalizarGrupo(dados, abm01Grupo, totalizacao)

					dados.add(index+1, totalizacao)
					index++
				}
			}
		}
	}

	private TableMap totalizarGrupo(List<TableMap> dados, Abm01 grupo, TableMap totalizacao) {
		def quantidade = BigDecimal.ZERO
		def valor = BigDecimal.ZERO
		for (dado in dados) {
			def codigo = grupo.abm01codigo
			if (dado.getString("abm01codigo").startsWith(codigo) && dado.getInteger("abm01tipo").equals(grupo.abm01tipo) && dado.getInteger("abm01grupo") == 0) {
				quantidade = quantidade.add(dado.getBigDecimal("quantidade"))
				valor = valor.add(dado.getBigDecimal("valor"))
			}
		}

		totalizacao.put("quantidade", quantidade)
		totalizacao.put("custoMedio", 0)
		totalizacao.put("valor", valor)

		return totalizacao
	}

	private TableMap buscarItensComSaldoZero(Long idItem){
		String sql = "SELECT abm01tipo,abm01grupo,abm01codigo,abm01na,aam06codigo,abm0101pmu as custoMedio "+
				"FROM abm01 "+
				"LEFT JOIN abm0101 ON abm0101item = abm01id "+
				"LEFT JOIN aam06 ON aam06id = abm01umu "+
				"WHERE abm01id = :idItem " +
				"and abm0101empresa = :empresa "

		return getAcessoAoBanco().buscarUnicoTableMap(sql,Parametro.criar("idItem",idItem),Parametro.criar("empresa",obterEmpresaAtiva().aac10id))
	}

	private BigDecimal buscarPrecoMedioItemNaData(Long abm01id, LocalDate dataSdo) {
//		def whereCtrl0 = ctrl0 == null ? " AND bcc01ctrl0 IS NULL" : " AND bcc01ctrl0 = :ctrl0 "
//		def whereCtrl1 = ctrl1 == null ? " AND bcc01ctrl1 IS NULL" : " AND bcc01ctrl1 = :ctrl1 "
//		def whereCtrl2 = ctrl2 == null ? " AND bcc01ctrl2 IS NULL" : " AND bcc01ctrl2 = :ctrl2 "
//		def whereLote = lote.isEmpty() ? "" : " AND bcc01lote = :lote "
//		def whereSerie = serie.isEmpty() ? "" : " AND bcc01serie = :serie "
//
//		def whereStatus = status != null ? " AND bcc01status = :status " : "";



		def sql = " SELECT bcc01pmu FROM bcc01 " +
				" WHERE bcc01item = :item " +
				" AND bcc01data <= :data " +
				//whereCtrl0 + whereCtrl1 + whereCtrl2 +
				//whereLote+ whereSerie + whereStatus +
				//obterWherePadrao("bcc01") +
				" ORDER BY bcc01id DESC, bcc01data DESC"

		/*
		def p1 = criarParametroSql("item", abm01id)
		def p2 = criarParametroSql("status", status)
		def p3 = criarParametroSql("data", dataSdo)
		def p4 = ctrl0 == null ? null : criarParametroSql("ctrl0", ctrl0)
		def p5 = ctrl1 == null ? null : criarParametroSql("ctrl1", ctrl1)
		def p6 = ctrl2 == null ? null : criarParametroSql("ctrl2", ctrl2)
		def p7 = lote.isEmpty() ? null : criarParametroSql("lote", lote)
		def p8 = serie.isEmpty() ? null : criarParametroSql("serie",serie)
		*/

		def p1 = criarParametroSql("item", abm01id)
		def p2 = criarParametroSql("data", dataSdo)

		//return getAcessoAoBanco().obterBigDecimal(sql, p1, p2, p3, p4, p5, p6, p7, p8)
		return getAcessoAoBanco().obterBigDecimal(sql, p1, p2)
	}

	private BigDecimal buscarSaldoAtualItem(Long abm01id, Long aam04id, Long abm15id0, Long abm15id1, Long abm15id2, Long centralEst) {
		def whereCtrl0 = abm15id0 == null ? " AND bcc02ctrl0 IS NULL " : " AND bcc02ctrl0 = :abm15id0 "
		def whereCtrl1 = abm15id1 == null ? " AND bcc02ctrl1 IS NULL " : " AND bcc02ctrl1 = :abm15id1 "
		def whereCtrl2 = abm15id2 == null ? " AND bcc02ctrl2 IS NULL " : " AND bcc02ctrl2 = :abm15id2 "
		def whereCentralEst = centralEst == null ? " AND bcc02centralEst IS NULL " : " AND bcc02centralEst = :centralEst "

		def sql = " SELECT bcc02qt FROM Bcc02 " +
				" WHERE bcc02item = :abm01id " +
				" AND bcc02status = :aam04id " +
				whereCtrl0 + whereCtrl1 +
				whereCtrl2 + whereCentralEst +
				obterWherePadrao("Bcc02")

		def p1 = criarParametroSql("abm01id", abm01id)
		def p2 = criarParametroSql("aam04id", aam04id)
		def p3 = abm15id0 == null ? null : criarParametroSql("abm15id0", abm15id0)
		def p4 = abm15id1 == null ? null : criarParametroSql("abm15id1", abm15id1)
		def p5 = abm15id2 == null ? null : criarParametroSql("abm15id2", abm15id2)
		def p6 = centralEst == null ? null : criarParametroSql("centralEst", centralEst)

		return getAcessoAoBanco().obterBigDecimal(sql, p1, p2, p3, p4, p5, p6)
	}

	private String buscarConteudoParametro(String aplic, String param) {
		return getSession().createCriteria(Aba01.class)
				.addFields("aba01conteudo")
				.addWhere(Criterions.eq("aba01aplic", aplic))
				.addWhere(Criterions.eq("aba01param", param))
				.addWhere(getSamWhere().getCritPadrao(Aba01.class))
				.get(ColumnType.STRING)
	}

	private BigDecimal buscarSaldoLote(Long idItem,String  lote){

		String sql = "SELECT SUM(bcc01qtps) as quantidade " +
				"FROM " +
				"bcc01 " +
				"where bcc01lote in (:lote) " +
				"and bcc01item = :idItem " +
				"AND bcc01gc = 1322578"
		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("lote", lote), Parametro.criar("idItem", idItem))
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFNhbGRvIEbDrXNpY28gZSBGaW5hbmNlaXJvIiwidGlwbyI6InJlbGF0b3JpbyJ9