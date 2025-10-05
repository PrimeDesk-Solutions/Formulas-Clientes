package multitec.baseDemo

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase

class SCF_ComporHistoricoMovimentacaoPLF extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_MOVIM_PLF;
	}

	@Override
	public void executar() {
		Daa01 daa01 = get("daa01");
		if (daa01.isNew()) return;
		Daa01 daa01Antigo = getAcessoAoBanco().buscarRegistroUnico("SELECT daa01id, daa01dtVctoN FROM Daa01 WHERE daa01id = :daa01id", criarParametroSql("daa01id", daa01.daa01id));
		
		TableMap daa01json = daa01.daa01json;
		if (daa01json != null) {
			def prorrogacoes = daa01json.get("prorrogacoes");
			if (prorrogacoes == null) prorrogacoes = 0;
			prorrogacoes = prorrogacoes + 1;
			daa01json.put("prorrogacoes", prorrogacoes);
		}
		
		String historico = StringUtils.concat("A data de Vencimento Nominal foi alterada de ", DateUtils.formatDate(daa01Antigo.daa01dtVctoN), " para ", DateUtils.formatDate(daa01.daa01dtVctoN), ".");
		put("historico", historico);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTMifQ==