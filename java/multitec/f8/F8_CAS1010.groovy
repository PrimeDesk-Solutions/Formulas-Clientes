package multitec.f8

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.cadastro.f8formula.ColunaF8
import sam.dto.cadastro.f8formula.RespostaDoF8
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.FiltroDoF8
import sam.server.samdev.utils.Parametro
import sam.server.samdev.utils.RequisicaoDoF8


//Exibe apenas os dados do usuário logado
class F8_CAS1010 extends FormulaBase {
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.F8;
	}

	@Override
	public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao");

        List<ColunaF8> colunas = new ArrayList<ColunaF8>();
        colunas.add(new ColunaF8("aab10user","Usuário"));
        colunas.add(new ColunaF8("aab10nome", "Nome do usuário"));

        TableMap linha = new TableMap();
        linha.put("aab10user", getVariaveis().getAab10().getAab10user());
        linha.put("aab10nome", getVariaveis().getAab10().getAab10nome());
        linha.put("id", getVariaveis().getAab10().getAab10id());

        List<TableMap> dados = new ArrayList<TableMap>();
        dados.add(linha);

        put("resposta", new RespostaDoF8(1, colunas, dados));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==