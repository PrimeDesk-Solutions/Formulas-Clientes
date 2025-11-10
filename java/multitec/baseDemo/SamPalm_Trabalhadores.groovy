package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abh80
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SamPalm_Trabalhadores extends RelatorioBase {

	@Override
	public Map<String, Object> criarValoresIniciais() {
		String where = getSamWhere().getWherePadrao("WHERE", Abh80.class);
		List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap("select abh80codigo, abh80nome, to_char(abh80salario,'L9G999G990D99') as abh80salario from abh80 " + where + " order by abh80codigo limit 100")
		return criarFiltros("dados", dados);
	}

	@Override
	public DadosParaDownload executar() {
		return null;
	}

	@Override
	public String getNomeTarefa() {
		return "Trabalhadores";
	}
}
//meta-sis-eyJkZXNjciI6IlRyYWJhbGhhZG9yZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=