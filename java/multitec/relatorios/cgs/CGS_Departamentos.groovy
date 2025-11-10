package multitec.relatorios.cgs;

import java.util.List;
import java.util.Map;

import javax.management.Query

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class CGS_Departamentos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "CGS - Departamentos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return null;
	}

	@Override
	public DadosParaDownload executar() {
		
		params.put("TITULO_RELATORIO", "Departamentos");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		List<Long> idDepartamentos = getListLong("departamentos");
		List<TableMap> dados = obterDadosRelatorio(idDepartamentos);

		return gerarPDF("CGS_Departamentos", dados);
	}
	
	public List<TableMap> obterDadosRelatorio (List<Long> idDepartamentos)  {
		String whereIdsDep = idDepartamentos != null && idDepartamentos.size() > 0 ? "and abb11id IN (:idDepartamentos)": "";
		Parametro parametro = idDepartamentos != null && idDepartamentos.size() > 0 ? Parametro.criar("idDepartamentos", idDepartamentos) : null;
		String sql = "SELECT abb11codigo, abb11nome FROM Abb11 " + obterWherePadrao("Abb11", "Where") + whereIdsDep + " order by abb11codigo";
		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro); 
		return receberDadosRelatorio;
	}
}
//meta-sis-eyJkZXNjciI6IkNHUyAtIERlcGFydGFtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=