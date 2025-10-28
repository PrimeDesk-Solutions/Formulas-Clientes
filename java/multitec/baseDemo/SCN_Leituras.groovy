package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.scn.LeituraDto
import sam.model.entities.ab.Abe2701;
import sam.server.samdev.formula.FormulaBase

class SCN_Leituras extends FormulaBase {

	@Override
	public void executar() {
		def abe27id = get("abe27id");
		def abm01id = get("abm01id");
		def historico = get("historico");
		
		String sql = "SELECT abe2701id, abe2701un, abe2701json FROM Abe2701 WHERE abe2701leitura = :abe27id";
		List<Abe2701> abe2701s = getAcessoAoBanco().buscarListaDeRegistros(sql, criarParametroSql("abe27id", abe27id));
		
		List<LeituraDto> leituras = new ArrayList<>();
		for (abe2701 in abe2701s) {
			LeituraDto leitura = new LeituraDto();
			leitura.abe21id = abe2701.abe2701un.abe21id;
			if (historico == null || historico.length() <= 0) {
				leitura.historico = " Leitura Ok ";
			} else {
				leitura.historico = historico + " Leitura Ok ";
			}
			
			TableMap abe2701json = abe2701.abe2701json;
			def valor = abe2701json.get("valor");
			def valorAnterior = abe2701json.get("valor_ant");
			leitura.quantidade = valor - valorAnterior;
			leitura.unitario = 3.49;
			leitura.desconto = 0;
			
			leituras.add(leitura);
		}
		
		put("leituras", leituras);
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCN_LEITURAS;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjIifQ==