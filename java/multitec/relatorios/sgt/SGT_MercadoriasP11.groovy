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
 * SGT - Mercadorias P11
 * @author Samuel Silva
 *
 */
class SGT_MercadoriasP11 extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SGT - Mercadorias P11";
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
		def idsItens = getListLong("itens")
		def mpms = getListInteger("mpms")
		def isEntrada = Utils.jsBoolean(getInteger("isEntrada"))
		def ultPagEntrada = getInteger("ultPagEntrada")
		def ultPagSaida = getInteger("ultPagSaida")
		def emissao = getLocalDate("emissao")
		def isRascunho = (boolean) get("isRascunho")
		
		def aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)
		
		def numPaginas = 0
		if(!isRascunho) numPaginas = isEntrada ? ultPagEntrada : ultPagSaida
		
		adicionarParametro("TITULO_RELATORIO", "Tabela de Código de Mercadorias")
		adicionarParametro("PERIODO", emissao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
		adicionarParametro("NUMERO_PAGINA", numPaginas)
		adicionarParametro("AAC10RS", aac10.aac10rs)
		adicionarParametro("AAC1002IE", ie)
		adicionarParametro("AAC10NI", aac10.aac10ni)
		
		def dadosRelatorio = buscarDadosItens(idsItens, mpms)
		
		return gerarRelatorio(dadosRelatorio, isRascunho, numPaginas)
	}
	
	DadosParaDownload gerarRelatorio(List<TableMap> dadosRelatorio, boolean isRascunho, Integer numPaginas) {
		def report = carregarArquivoRelatorio("SGT_MercadoriasP11_R1")
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
	
	List<TableMap> buscarDadosItens(List<Integer> mpms, List<Long> idsItens) {
		def whereTipo  = mpms != null && mpms.size() > 0 && !mpms.contains(-1) ? " AND abm01tipo IN (:mpms) " : ""
		def whereItens = idsItens != null && idsItens.size() > 0 ? " AND abm01id IN (idsItens) " : ""
		
		def sql = " SELECT abm01tipo, abm01codigo, abm01descr, abg01codigo FROM Abm01 " +
				  " INNER JOIN Abm0101 ON abm0101item = abm01id " +
				  " LEFT JOIN Abg01 ON abg01id = abm0101ncm " +
				  " WHERE abm01grupo = 0 " + 
				  " ORDER BY abm01tipo, abm01codigo"
		
		def param1 = mpms != null && mpms.size() > 0 && !mpms.contains(-1) ? criarParametroSql("mpms", mpms) : null
		def param2 = idsItens != null && idsItens.size() > 0 ? criarParametroSql("idsItens", idsItens) : null
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, param1, param2);
	}
	
	Edd10 buscarUltimoLivro() {
		return getSession().createCriteria(Edd10.class)
						.addWhere(getSamWhere().getCritPadrao(Edd10.class))
						.setOrder("edd10num DESC")
						.setMaxResults(1)
						.get(ColumnType.ENTITY)
	}
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIE1lcmNhZG9yaWFzIFAxMSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==