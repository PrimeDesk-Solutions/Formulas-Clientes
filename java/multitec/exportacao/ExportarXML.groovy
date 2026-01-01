package multitec.exportacao;

import br.com.multitec.utils.collections.TableMap
import java.util.Map;

import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.ExportacaoBase;
import sam.server.samdev.utils.Parametro

import br.com.multitec.utils.xml.*

public class ExportarXML extends ExportacaoBase {

	@Override
	public String getNomeTarefa() {
		return "CGS - Naturezas";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return null;
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idNaturezas = getListLong("naturezas");
		Boolean idImprimir = get("imprimircontas");
		List<TableMap> dados = dadosNaturezas(idNaturezas);
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Naturezas");
		params.put("IMPRIMIR", idImprimir)

		def root = XMLConverter.createElement("naturezas");
		for(TableMap natureza : dados) {
			def elNat = root.addNode("natureza");
			elNat.addNode("abf10codigo", natureza.getString("abf10codigo"));
			elNat.addNode("abf10nome", natureza.getString("abf10nome"));
			elNat.addNode("abf1001seq", natureza.getInteger("abf1001seq"));
			elNat.addNode("abc10reduzido", natureza.getString("abc10reduzido"));
			elNat.addNode("abc10codigo", natureza.getString("abc10codigo"));
			elNat.addNode("abc10nome", natureza.getString("abc10nome"));
		}
		
		return gerarXml("CGS_Naturezas", root);
	}
	
	public List<TableMap> dadosNaturezas(List<Long> idNaturezas){
		String whereIdsNat = idNaturezas != null && idNaturezas.size() > 0 ? "AND abf10id IN (:idNaturezas)" : "";
		Parametro parametro = idNaturezas != null && idNaturezas.size() > 0 ? Parametro.criar("idNaturezas", idNaturezas) : null;
		
		String sql = " SELECT abf10codigo, abf10nome, abf1001seq," +
                     " abc10reduzido, abc10codigo, abc10nome" +
                     " FROM Abf10" +
                     " LEFT JOIN Abf1001 ON abf1001nat = abf10id" +
                     " LEFT JOIN Abc10 ON abc10id = abf1001cta " + obterWherePadrao("Abf10", "WHERE") + whereIdsNat + 
					 " ORDER BY abf10codigo";
					 
		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
		return receberDadosRelatorio;
	}

}
//meta-sis-eyJkZXNjciI6IkNHUyAtIE5hdHVyZXphcyIsInRpcG8iOiJleHBvcnRhY2FvIn0=