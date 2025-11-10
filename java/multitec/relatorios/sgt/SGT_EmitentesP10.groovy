package multitec.relatorios.sgt

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ed.Edd10
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

/**
 * SGT - Emitentes P10
 * @author Samuel Silva
 *
 */
class SGT_EmitentesP10 extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SGT - Emitentes P10"
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		def edd10 = buscarUltimoLivro();
		def ultimaPag = 0
		if(edd10 != null) {
			ultimaPag = edd10.edd10ultPag
		}
		
		def filtrosDefault = new HashMap<String, Object>()
		filtrosDefault.put("isEntrada", "0")
		filtrosDefault.put("ultPagEntrada", ultimaPag)
		filtrosDefault.put("ultPagSaida", ultimaPag)
		filtrosDefault.put("emissao", MDate.date())
		filtrosDefault.put("isRascunho", true)
		return Utils.map("filtros", filtrosDefault)
	}

	@Override
	public DadosParaDownload executar() {
		def idsEntidades = getListLong("entidades")
		def isEntrada = Utils.jsBoolean(getInteger("isEntrada"))
		def ultPagEntrada = getInteger("ultPagEntrada")
		def ultPagSaida = getInteger("ultPagSaida")
		def emissao = getLocalDate("emissao")
		def isRascunho = (boolean) get("isRascunho")
		
		def aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)
		
		adicionarParametro("AAC1002IE", ie)
		adicionarParametro("AAC10RS", aac10.aac10rs)
		adicionarParametro("AAC10NI", aac10.aac10ni)
		adicionarParametro("PERIODO", emissao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
		adicionarParametro("TITULO_RELATORIO", "Códigos de Emitentes")
		
		def numPaginas = 0
		if(!isRascunho) numPaginas = isEntrada ? ultPagEntrada : ultPagSaida
		
		adicionarParametro("NUMERO_PAGINA", numPaginas)
		
		def dadosRelatorio = buscarEntidades(idsEntidades)
		
		return gerarRelatorio(dadosRelatorio, isRascunho, numPaginas)
	}
	
	DadosParaDownload gerarRelatorio(List<TableMap> dadosRelatorio, boolean isRascunho, Integer numPaginas) {
		def report = carregarArquivoRelatorio("SGT_EmitentesP10_R1")
		def print = processarRelatorio(report, dadosRelatorio);
		
		def numPags = print.getPages() != null ? print.getPages().size() : 0;
		
		gravarNumeroPaginas(numPags + numPaginas, isRascunho);
		
		byte[] bytes = convertPrintToPDF(print);
		return criarDadosParaDownload(bytes);
	}
	
	void gravarNumeroPaginas(Integer numPags, boolean isRascunho) {
		if(isRascunho) return;
		
		def edd10 = buscarUltimoLivro();
		if(edd10 != null) {
			edd10.setEdd10ultPag(numPags)
			getSession().persist(edd10)
		}
	}
	
	List<TableMap> buscarEntidades(List<Long> idsEntidades) {
		def where = idsEntidades != null && idsEntidades.size() > 0 ? " AND abe01id IN (:idsEntidades) " : ""
		
		def sql = " SELECT abe01codigo, abe01nome, aag02uf, abe01ni, abe01ie FROM Abe01" +
		 		  " INNER JOIN Abe0101 ON abe0101ent = abe01id " +
				  " INNER JOIN Aag0201 ON aag0201id = abe0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " WHERE abe0101principal = 1 " +
				    where + obterWherePadrao("Abe01")

		def param = idsEntidades != null && idsEntidades.size() > 0 ? criarParametroSql("i", idsEntidades) : null
		return getAcessoAoBanco().buscarListaDeTableMap(sql, param)
	}
	
	Edd10 buscarUltimoLivro() {
		return getSession().createCriteria(Edd10.class)
						.addWhere(getSamWhere().getCritPadrao(Edd10.class))
						.setOrder("edd10num DESC")
						.setMaxResults(1)
						.get(ColumnType.ENTITY)
	}
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIEVtaXRlbnRlcyBQMTAiLCJ0aXBvIjoicmVsYXRvcmlvIn0=