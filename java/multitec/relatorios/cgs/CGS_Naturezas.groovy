package multitec.relatorios.cgs;

import br.com.multitec.utils.collections.TableMap
import java.util.Map;

import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class CGS_Naturezas extends RelatorioBase {

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
		
		return gerarPDF("CGS_Naturezas", dados);
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
//meta-sis-eyJkZXNjciI6IkNHUyAtIE5hdHVyZXphcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==