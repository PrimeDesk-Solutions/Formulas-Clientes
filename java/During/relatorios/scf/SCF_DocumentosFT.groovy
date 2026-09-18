package During.relatorios.scf

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

public class SCF_DocumentosFT extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Documentos Financeiros por FT - LCR";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("ordem", "0");
		filtrosDefault.put("classe","0");
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("tipoData","0");
		filtrosDefault.put("tipo","0");
		filtrosDefault.put("impressao","0");
		filtrosDefault.put("vencimento", "0")
		def result = getAcessoAoBanco().obterListaDeLong("select abf20id from abf20")
		filtrosDefault.put("PLF",result)
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer ordem = getInteger("ordem");
		Integer classe = getInteger("classe");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> documento = getListLong("documento");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Integer tipoData = getInteger("tipoData");
		LocalDate[] data = getIntervaloDatas("data");
		Integer tipo = getInteger("tipo")
		Integer impressao = getInteger("impressao")
		Integer vencimento = getInteger("vencimento")
		Boolean isTotalDia = getBoolean("isTotalDia")

		String tituloOrdem = ordem == 0 ? " por Número" : ordem == 1 ? " por Vencimento " :  ordem == 2 ? " por Entidade" : "por Pagamento"

		switch (classe) {
			case 0: adicionarParametro("TITULO_RELATORIO", "Documentos à Receber"+tituloOrdem);
				break;
			case 1: adicionarParametro("TITULO_RELATORIO", "Documentos Recebidos"+tituloOrdem);
				break;
			case 2: adicionarParametro("TITULO_RELATORIO", "Documentos à Pagar"+tituloOrdem);
				break;
			case 3: adicionarParametro("TITULO_RELATORIO", "Documentos Pagos"+tituloOrdem);
				break;
			case 4: adicionarParametro("TITULO_RELATORIO", "Documentos à Recerber/Recebidos"+tituloOrdem)
				break
			case 5: adicionarParametro("TITULO_RELATORIO", "Documentos à Pagar/Pagos"+tituloOrdem)
				break
		}

		adicionarParametro("EMPRESA", getVariaveis().getAac10().getAac10rs());

		if(isTotalDia != false) {
			adicionarParametro("ORDEM", ordem)
		}
		if (dataEmissao != null) {
			String titulo = "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
			adicionarParametro("PERIODO", titulo);
		}
		if (dataVenc != null) {
			String titulo = "Período Vencimento: " + dataVenc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataVenc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
			adicionarParametro("PERIODO", titulo);
		}
		if (data != null) {
			if (tipoData == 0) {
				String titulo = "Período Pagamento: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
				adicionarParametro("PERIODO", titulo );
			} else {
				String titulo = "Período Recebimento: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
				adicionarParametro("PERIODO", titulo);
			}
		}

		List<TableMap> documentos = buscaDocumentos(ordem, classe, numeroInicial, numeroFinal, documento, dataVenc, dataEmissao, tipoData, data, tipo, vencimento)

		List<TableMap> docs = new ArrayList()
		List<TableMap> remove = new ArrayList()
		Integer count = 0
		for (doc in documentos) {
			if(docs.contains(doc.getLong("daa01id"))) {
				remove.add(count)
			}else {
				if (doc.getString("ft") == null) {
					doc.put("ft", buscarFt(doc.getLong("daa01id")))
				}
				String previsao = "R"
				if (doc.getInteger("daa01previsao") == 1) previsao = "P";
				doc.put("previsao", previsao);
				LocalDate dataAtual =  doc.getDate("daa01dtPgto") == null ? LocalDate.now() : doc.getDate("daa01dtPgto")
				Long dias = ChronoUnit.DAYS.between(dataAtual, doc.getDate("vencimento"))
				doc.put("dias", dias);

				TableMap json = doc.getTableMap("daa01json")
				BigDecimal desconto = 0.0
				BigDecimal JM = 0.0
				if(json != null ) {
					BigDecimal juros = doc.getDate("daa01dtPgto") == null ? json.getBigDecimal("juros") == null ? 0.0 : json.getBigDecimal("juros"): json.getBigDecimal("jurosq") == null ? 0.0 : json.getBigDecimal("jurosq")
					BigDecimal multa = doc.getDate("daa01dtPgto") == null ? json.getBigDecimal("multa") == null ? 0.0 : json.getBigDecimal("multa"): json.getBigDecimal("multaq") == null ? 0.0 : json.getBigDecimal("multaq")
					if(doc.getDate("daa01dtPgto") == null) {
						desconto = json.getDate("data_lim") == null ? json.getBigDecimal("desconto") == null ? 0.0 : json.getBigDecimal("desconto") : 0.0
						desconto = desconto < 0 ? desconto * -1 : desconto
					}else {
						desconto = json.getBigDecimal("descontoq") == null ? 0.0 : json.getBigDecimal("descontoq")
						desconto = desconto < 0 ? desconto * -1 : desconto
					}

					JM =   dias < 0 ? (juros * (dias *-1)) + multa  : 0.0
					JM = JM < 0 ? JM *-1 : JM
					if(doc.getDate("daa01dtPgto") != null ) {
						JM = juros + multa
						JM = JM < 0 ? JM *-1 : JM
					}
				}
				doc.put("jm", JM <= 0 ? JM*-1 : JM)
				doc.put("desconto", desconto < 0 ? desconto*-1 : desconto )
				docs.add(doc.getLong("daa01id"))
			}
			count++
		}

		Collections.reverse(remove)
			for(index in remove) {
				documentos.remove(index)
			}


		String relatorio = ordem == 2 ? "SCF_DocumentosEntidade"  : "SCF_DocumentosFT"

		if(impressao == 0) {
			return gerarPDF(relatorio,documentos);
		}else {
			return gerarXLSX(relatorio,documentos);
		}

	}

	private List<TableMap> buscaDocumentos(Integer ordem, Integer classe, Integer numeroInicial, Integer numeroFinal, List<Long> documento, LocalDate[] dataVenc, LocalDate[] dataEmissao, Integer tipoData, LocalDate[] data, Integer tipo, Integer vencimento) {

		String whereTipo = tipo == 0 ? " AND daa01previsao = 0": tipo == 1  ? " AND daa01previsao = 1 " : ""
		String whereRp = classe == 0 || classe == 1 || classe == 4 ? " WHERE daa01rp = 0 " : " WHERE daa01rp = 1 ";
		String whereDtBaixa = classe.equals(0) || classe.equals(2) ? " AND daa01dtBaixa IS NULL " : classe.equals(1) || classe.equals(3) ? " AND daa01dtBaixa IS NOT NULL " : "";
		String whereTipoDoc = documento != null && documento.size() != 0 ? " AND aah01id IN (:idDocumentos) " : "";
		String campoVcto = vencimento == 0 ? " daa01dtVctoR " : " daa01dtVctoN "
		String whereVenc = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? getWhereDataInterval("AND", dataVenc, campoVcto) : "";
		String whereEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? getWhereDataInterval("AND", dataEmissao, "daa01dtlcto") : "";
		String whereData = data != null && data[0] != null && data[1] != null ?
				tipoData == 0 ? " AND daa01dtPgto >= '" + data[0] + "' and daa01dtPgto <= '" + data[1] + "' "
				: " AND daa01dtBaixa >= '" + data[0] + "' and daa01dtBaixa <= '" + data[1] + "' " : "";
		String orderBy= ordem == 0 ? " ORDER BY abb01num, abb01serie, abb01parcela " :
							ordem == 1 ? " ORDER BY " + (vencimento == 0 ? " daa01dtVctoR " : " daa01dtVctoN ") + ", aah01codigo, abb01num, abb01serie, abb01parcela " :
							ordem == 2 ? " ORDER BY abe01codigo, aah01codigo, abb01num, abb01serie, abb01parcela " :
							" ORDER BY daa01dtPgto, aah01codigo, abb01num, abb01serie, abb01parcela "

		String campoGrupo = ordem == 1 ? ", " + campoVcto + " as grupo " : ordem == 3 ? ", daa01dtPgto as grupo " : ""

		String sql = " SELECT daa01id,abe01id, abe01codigo, abe01na, aah01codigo, aah01nome, abb01num, abb01serie, " +
				" abb01parcela, abb01quita, abb01data, "+campoVcto+" as vencimento, daa01dtPgto, " +
				" daa01dtBaixa, daa01valor AS valor, daa01json, cast(daa01json ->> 'ft' as varchar) as ft, daa01previsao " + campoGrupo +
				" FROM daa01 " +
				" LEFT JOIN abb01  ON abb01id = daa01central " +
				" LEFT JOIN abe01  ON abe01id = abb01ent " +
				" LEFT JOIN aah01  ON aah01id = abb01tipo " +
				whereRp + whereDtBaixa + " AND abb01num >= :numeroInicial AND abb01num <= :numeroFinal " +
				whereTipoDoc + whereVenc + whereEmissao + whereData + whereTipo +
				orderBy

		Parametro p1 = criarParametroSql("numeroInicial", numeroInicial)
		Parametro p2 = criarParametroSql("numeroFinal", numeroFinal)
		Parametro p3 = documento != null && documento.size() > 0 ? criarParametroSql("idDocumentos", documento) : null

		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2,p3)
	}

	private String buscarFt(Long id) {

		String sql = "select distinct cast(eaa0103json ->> 'ft' as varchar) as ft\n" +
					 "from daa01\n" +
					 "inner join abb01 as central on central.abb01id = daa01central \n" +
					 "inner join abb0102 on central.abb01id = abb0102doc\n" +
					 "inner join abb01 as centraldesdobr on centraldesdobr.abb01id = abb0102central\n" +
					 "inner join eaa01 on centraldesdobr.abb01id = eaa01central\n" +
					 "inner join eaa0103 on eaa01id = eaa0103doc\n" +
				 	 "where daa01id = " + id

		return getAcessoAoBanco().obterString(sql)
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgRmluYW5jZWlyb3MgcG9yIEZUIC0gTENSIiwidGlwbyI6InJlbGF0b3JpbyJ9