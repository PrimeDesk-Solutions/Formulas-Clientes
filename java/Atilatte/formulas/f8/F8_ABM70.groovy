package Atilatte.formulas;

import br.com.multitec.utils.collections.TableMap;
import sam.core.politica.OperacaoDeSeguranca;
import sam.dicdados.FormulaTipo;
import sam.dto.cadastro.f8formula.ColunaF8;
import sam.dto.cadastro.f8formula.RespostaDoF8;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.FiltroDoF8;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.RequisicaoDoF8;


class F8_ABM70 extends FormulaBase {
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.F8;
	}

	@Override
	public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao");

        List<ColunaF8> colunas = new ArrayList<ColunaF8>();
		colunas.add(new ColunaF8("abm70num",  "Número"));
		colunas.add(new ColunaF8("abm70dv",  "Dígito Verificador"));
		colunas.add(new ColunaF8("abb01num",  "Número O.P"));
		colunas.add(new ColunaF8("abm70lote",  "Lote"));
		colunas.add(new ColunaF8("mps",  "Item-Tipo"));
		colunas.add(new ColunaF8("abm01codigo",  "Item-Código"));
		colunas.add(new ColunaF8("abm0101gtinTrib",  "Item-GTIN"));
		colunas.add(new ColunaF8("abm01livre",  "Item-Cód.Livre"));
		colunas.add(new ColunaF8("abm01reduzido",  "Item-Reduzido"));
		colunas.add(new ColunaF8("abm01descr",  "Item-Descrição"));
		colunas.add(new ColunaF8("abm70qt",  "Quantidade"));
		colunas.add(new ColunaF8("aac01codigo",  "EG-Código"));
		colunas.add(new ColunaF8("aac01nome",  "EG-Nome"));
		
		
		String whereFiltros = "";
		List<Parametro> parametros = new ArrayList<Parametro>();
		if (requisicao.getFiltros() != null && requisicao.getFiltros().size() > 0) {
			for(FiltroDoF8 filtro : requisicao.getFiltros()){
				whereFiltros += filtro.getWhere().contains("false") ? "" : filtro.getWhere() + " OR ";
				parametros.addAll(filtro.getParametros());
			}
			whereFiltros = whereFiltros.isEmpty() ? "" : "AND ("+ whereFiltros.substring(0, whereFiltros.length() - 3) +")"
		}
        Parametro[] parametrosArray = parametros.toArray();

        String baseDaSql = 
     	"from abm70 "+
		"inner join abb01 on abb01id = abm70central "+ 
		"inner join abm01 on abm01id = abm70item "+
		"inner join abm0101 on abm0101item = abm01id "+
		"inner join aac01 on aac01id = abm70gc "+
        	whereFiltros;

        String sqlCount = " SELECT count(*) as qtdTotal " + baseDaSql;

        String sqlDados = 
        "SELECT abm70id as id, abm70num,abm70lote,abm70dv, abb01num, "+
	   "CASE WHEN abm01tipo = 0 then '0-Mat' WHEN abm01tipo = 1 THEN '1-Prod' ELSE '3-Serv' END AS mps, "+
	   "abm01codigo, abm0101gtinTrib, abm01livre, abm01reduzido,abm01descr, abm70qt, aac01codigo, aac01nome "+
        baseDaSql +
        " ORDER BY abm70num "
        
        Long qtdTotalDeRegistros = getAcessoAoBanco().obterLong(sqlCount, parametrosArray);
        List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap(sqlDados, true, requisicao.getPagina(), requisicao.getTamanhoDaPagina(), parametrosArray);

        put("resposta", new RespostaDoF8(qtdTotalDeRegistros, colunas, dados));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==