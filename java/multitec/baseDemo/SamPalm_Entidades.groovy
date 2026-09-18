package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abe01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SamPalm_Entidades extends RelatorioBase {

	@Override
	public Map<String, Object> criarValoresIniciais() {
		String where = getSamWhere().getWherePadrao("WHERE", Abe01.class);
		List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap("select abe01codigo, abe01na from abe01 " + where + " order by abe01codigo limit 100")
		return criarFiltros("dados", dados);
	}

	@Override
	public DadosParaDownload executar() {
		return null;
	}

	@Override
	public String getNomeTarefa() {
		return "Entidades";
	}
}
//meta-sis-eyJkZXNjciI6IkVudGlkYWRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==