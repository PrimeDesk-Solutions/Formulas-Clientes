package Atilatte.relatorios.scf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abf10
import sam.model.entities.da.Dab10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_Historico_Lancamento_Nat extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCF - Histórico Lançamento Natureza";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", datas);
		filtrosDefault.put("impressao", "0");

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idContaCorrente = getListLong("contaCorrente");
		String idNaturezaIni = getString("naturezaIni");
		String idNaturezaFin = getString("naturezaFin");
		LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
		Integer impressao = getInteger("impressao");

		params.put("TITULO_RELATORIO", "SCF - Histórico Lançamentos Por Natureza");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

		String estrutura = getSession().createCriteria(Aba01.class)
				.addFields("aba01conteudo")
				.addWhere(Criterions.eq("aba01aplic", "ABF10"))
				.addWhere(Criterions.eq("aba01param", "ESTRNATUREZA"))
				.addWhere(getSamWhere().getCritPadrao(Aba01.class))
				.get(ColumnType.STRING);

		List<Integer> grupos = new ArrayList<>();
		getEstrutura(estrutura, grupos, 0);

		int tamanhoTotal = grupos.get(grupos.size() - 1);
		params.put("TAMANHO_NAT", tamanhoTotal);

		List<TableMap> lancamentos = buscarLancamentosPorNatureza(dataPeriodo, idContaCorrente, idNaturezaIni,idNaturezaFin);

		for (int i = lancamentos.size(); i > 0 ; i--) {
			int index = i - 1;
			if(index < 0) continue;
			String abf10codigo = lancamentos.get(index).getString("abf10codigo");
			int indexOfAbf10 = grupos.indexOf(abf10codigo.length());
			String abf10codigoPai = null;
			if(abf10codigo.length() > grupos.get(0)) {
				int indexWhileGrupo = indexOfAbf10;
				int indexWhileList = index;
				while (abf10codigoPai == null || abf10codigoPai.length() > grupos.get(0)) {
					indexWhileGrupo = indexWhileGrupo - 1;
					abf10codigoPai = StringUtils.ajustString(abf10codigo, grupos.get(indexWhileGrupo));
					TableMap tm = null;
					if(index > 0) tm = lancamentos.get(index - 1);
					if(tm != null && (indexWhileGrupo == 0 || tm.getString("abf10codigo").length() == grupos.get(0) || tm.getString("abf10codigo").substring(0, grupos.get(indexWhileGrupo)).equalsIgnoreCase(abf10codigoPai))) continue;
					TableMap abf10Pai = getSession().createQuery(" SELECT abf10codigo, abf10nome FROM Abf10 WHERE abf10codigo LIKE :abf10codigoPai " + getSamWhere().getWherePadrao("AND", Abf10.class)).setParameter("abf10codigoPai", abf10codigoPai).getUniqueTableMap();
					if (abf10Pai == null) interromper("A Natureza " + abf10codigo + " não corresponde a estrutura informada no parametro TAMANHO_NAT");
					int indexOf = lancamentos.indexOf(lancamentos.get(indexWhileList));
					lancamentos.add(indexOf, abf10Pai);
				}
				if(index == 0) i++;
			}
		}



		List<TableMap> dados = new ArrayList<>();
		for (lcto in lancamentos) {
			TableMap tm = new TableMap();
			tm.put("abf10codigo", lcto.getString("abf10codigo"));
			tm.put("abf10nome", lcto.getString("abf10nome"));
			tm.put("dab10data", lcto.getDate("dab10data"));
			tm.put("dab01codigo", lcto.getString("dab01codigo"));
			tm.put("dab01nome", lcto.getString("dab01nome"));
			tm.put("dab10historico", lcto.getString("dab10historico"));
			if (lcto.getString("abf10codigo").length() ==  tamanhoTotal) tm.put("es", lcto.getInteger("dab10mov").equals(0) ? "E" : "S");

			BigDecimal valor = lcto.getBigDecimal("dab10011valor");
			if(lcto.getInteger("dab10mov") == 1) {
				valor = valor.multiply(new BigDecimal(-1));
			}
			tm.put("valor", valor);

			if(lcto.getString("abf10codigo").length() > grupos.get(0)) {
				int indexOf = grupos.indexOf(lcto.getString("abf10codigo").length()) - 1;
				tm.put("pai", lcto.getString("abf10codigo").substring(0, grupos.get(indexOf)));
			}

			dados.add(tm);
		}

		for(TableMap dado : dados) {
			if(dado.getString("pai") != null && dado.getString("abf10codigo").length() == tamanhoTotal) {
				TableMap pai = dados.stream().filter({ d -> d.getString("abf10codigo").equalsIgnoreCase(dado.getString("pai"))}).findFirst().get();
				BigDecimal valor = pai.getBigDecimal("valor") == null ? BigDecimal.ZERO : pai.getBigDecimal("valor");
				BigDecimal valorFilho = dado.getBigDecimal("valor");
				pai.put("valor", valor.add(valorFilho));
				if(pai.getString("pai") != null && pai.getString("pai").length() > 0) somandoNosPais(dados, dado, pai.getString("pai"));
			}
		}

		if(impressao == 1) return gerarXLSX("SCF_Historico_Lancamento_Nat", dados)

		return gerarPDF("SCF_Historico_Lancamento_Nat", dados)
	}

	private void somandoNosPais(List<TableMap> dados, TableMap tm, String codigo) {
		for(TableMap pai : dados) {
			if(pai.getString("abf10codigo").equals(codigo)) {
				BigDecimal valor = pai.getBigDecimal("valor") == null ? BigDecimal.ZERO : pai.getBigDecimal("valor");
				BigDecimal valorFilho = tm.getBigDecimal("valor");
				pai.put("valor", valor.add(valorFilho));
				if(pai.getString("pai") != null && pai.getString("pai").length() > 0) somandoNosPais(dados, tm, pai.getString("pai"));
			}
		}
	}

	private List<TableMap> buscarLancamentosPorNatureza(LocalDate[] dataPeriodo, List<Long> idContaCorrente, String idNaturezaIni, String idNaturezaFin) {
		String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
		String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " and dab01.dab01id IN (:idContaCorrente)": "";
		String whereIdNatureza = idNaturezaIni != null && idNaturezaFin != null ? " and abf10.abf10codigo between :idNaturezaIni and :idNaturezaFin ": "";

		Parametro paramCC = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
		Parametro paramNaturezaIni = idNaturezaIni != null ? Parametro.criar("idNaturezaIni", idNaturezaIni) : null;
		Parametro paramNaturezaFin = idNaturezaFin != null ? Parametro.criar("idNaturezaFin", idNaturezaFin) : null;

		String sql = " SELECT dab10id, dab10data, abf10codigo, abf10nome, dab01codigo, dab01nome, dab10historico, dab10mov, dab10011valor " +
				" FROM Dab10011 " +
				" INNER JOIN abf10 ON abf10id = dab10011nat " +
				" INNER JOIN dab1001 ON dab1001id  = dab10011depto " +
				" INNER JOIN dab10 ON dab10id = dab1001lct " +
				" LEFT JOIN dab1002 ON dab1002lct = dab10id "+
				" LEFT JOIN dab01 ON dab01id = dab1002cc " +
				wherePeriodoData +
				whereIdsContaCorrente +
				whereIdNatureza +
				getSamWhere().getWherePadrao("AND", Dab10.class) +
				" ORDER BY abf10codigo, dab10data, dab01codigo ";

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramCC, paramNaturezaIni,paramNaturezaFin)
		return receberDadosRelatorio
	}

	private void getEstrutura(String estrutura, List<Integer> grupos, int tamanhoTotal) {
		tamanhoTotal = tamanhoTotal + StringUtils.substringBeforeFirst(estrutura, "|").length();
		grupos.add(Integer.valueOf(tamanhoTotal));
		estrutura = StringUtils.substringAfterFirst(estrutura, "|");
		if(!estrutura.contains("|")) {
			grupos.add(Integer.valueOf(tamanhoTotal + estrutura.length()));
			tamanhoTotal = tamanhoTotal + estrutura.length();
		}else {
			getEstrutura(estrutura, grupos, tamanhoTotal);
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEhpc3TDs3JpY28gTGFuw6dhbWVudG8gTmF0dXJlemEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNDRiAtIEhpc3TDs3JpY28gTGFuw6dhbWVudG8gTmF0dXJlemEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=