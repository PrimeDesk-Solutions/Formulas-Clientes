package multitec.baseDemo

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SamPalm_SaldoFisicoFinanceiro extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "SCE - Saldo Fisico Financeiro";
	}


	@Override
	public Map<String, Object> criarValoresIniciais() {
		return null;
	}

	@Override
	public DadosParaDownload executar() {
		String itemIni = getString("itemIni")
		String itemFim = getString("itemFim")
		
		adicionarParametro("aac10rs", obterEmpresaAtiva().aac10na)
		adicionarParametro("data", "Data: "+LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
		
		List<TableMap> dados = new ArrayList<>()
		List<Abm01> itens = buscarItensParaComporSaldo(itemIni, itemFim)
		buscarSaldosAtuaisEstoquePorItens(itens,dados)
		
		return gerarPDF(dados);
	}
	
	private void buscarSaldosAtuaisEstoquePorItens(List<Abm01> abm01s, List<TableMap> dados) {
		for (abm01 in abm01s) {
			TableMap tmSaldoAtual = buscarSaldoAtualItem(abm01.abm01id)
			if(tmSaldoAtual.isEmpty()) continue

			def pmu = getSession().createCriteria(Abm0101.class)
						.addFields("abm0101pmu")
						.addWhere(Criterions.eq("abm0101item", abm01.abm01id))
						.addWhere(Criterions.eq("abm0101empresa", obterEmpresaAtiva().aac10id))
						.get(ColumnType.BIG_DECIMAL)

			pmu = pmu ?: BigDecimal.ZERO

			def valor = pmu * tmSaldoAtual.getBigDecimal_Zero("quantidade")
			tmSaldoAtual.put("valor", valor)

			dados.add(tmSaldoAtual)
		}

	}
	
	private List<Abm01> buscarItensParaComporSaldo(String itemIni, String itemFim) {
		Criterion critItem = itemIni != null && itemFim != null ? Criterions.between("abm01codigo", itemIni, itemFim) : 
							 itemIni != null && itemFim == null ? Criterions.ge("abm01codigo", itemIni) : 
							 itemIni == null && itemFim != null ? Criterions.le("abm01codigo", itemFim) : 
							 Criterions.isTrue()
		
		return getSession().createCriteria(Abm01.class)
				.addFields("DISTINCT abm01id, abm01tipo, abm01codigo")
				.addJoin(Joins.join("Abm0102", "abm0102item = abm01id").left(true))
				.addWhere(critItem)
				.addWhere(Criterions.eq("abm01grupo", Abm01.NAO))
				.setOrder(" abm01tipo, abm01codigo ")
				.getList(ColumnType.ENTITY)
	}
	
	private TableMap buscarSaldoAtualItem(Long abm01id) {

		String sql = " SELECT abm01tipo, abm01codigo, abm01na, aam06codigo, SUM(bcc02qt) AS quantidade " +			
				" FROM bcc02 " +
				" INNER JOIN Abm01 ON abm01id = bcc02item " +
				" LEFT JOIN Aam06 ON aam06id = abm01umu " +
				" WHERE abm01id = :abm01id " +
				obterWherePadrao("Bcc02") +
				" GROUP BY abm01tipo, abm01codigo, abm01na,  aam06codigo" +
				" LIMIT 1 "

		Parametro parametro = Parametro.criar("abm01id", abm01id)

		return getAcessoAoBanco().buscarListaDeTableMap(sql, parametro)
	}

}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFNhbGRvIEZpc2ljbyBGaW5hbmNlaXJvIiwidGlwbyI6InJlbGF0b3JpbyJ9