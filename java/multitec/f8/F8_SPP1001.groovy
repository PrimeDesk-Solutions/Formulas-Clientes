package multitec.f8;

import br.com.multitec.utils.collections.TableMap;
import sam.core.politica.OperacaoDeSeguranca;
import sam.dicdados.FormulaTipo;
import sam.dto.cadastro.f8formula.ColunaF8;
import sam.dto.cadastro.f8formula.RespostaDoF8;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.FiltroDoF8;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.RequisicaoDoF8;


class F8_SPP1001 extends FormulaBase {
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.F8;
	}

	@Override
	public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao");

        List<ColunaF8> colunas = new ArrayList<ColunaF8>();
		colunas.add(new ColunaF8("numplano",  "Núm Plano"));
		colunas.add(new ColunaF8("descrplano",  "Descr Plano"));
        colunas.add(new ColunaF8("abb01num",  "Número"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abb01data"));
		colunas.add(new ColunaF8("bab01status", "Status"));
        colunas.add(new ColunaF8("abp20bomcodigo", "BOM"));
        colunas.add(new ColunaF8("abm01codigo", "Produto"));
        colunas.add(new ColunaF8("abm01na",  "NA Produto"));
        colunas.add(new ColunaF8("abp10codigo",  "Processo"));
		colunas.add(new ColunaF8("abp10descr",  "Descr Processo"));

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
        " FROM Bab01 " +
        " INNER JOIN Abb01 ON bab01central = abb01id " +
        " INNER JOIN Abp20 ON bab01comp = abp20id " +
        " LEFT JOIN Abm01 ON abp20item = abm01id " +
        " LEFT JOIN Abp10 ON bab01proc = abp10id " +
        " WHERE true " + obterWherePadrao("Bab01") +
        " " + whereFiltros;

        String sqlCount = " SELECT count(*) as qtdTotal " + baseDaSql;

        String sqlDados = 
        " SELECT bab01id as id, " + 
		" (select abb01num from baa01 inner join abb01 on baa01central = abb01id where baa01id in (select baa0101plano from baa0101 inner join bab0103 on baa0101id = bab0103itemPP and bab0103op = bab01id) limit 1) as numplano, " +
		" (select baa01descr from baa01 where baa01id in (select baa0101plano from baa0101 inner join bab0103 on baa0101id = bab0103itemPP and bab0103op = bab01id) limit 1) as descrplano, " +
		" abb01num, abb01data, bab01status, abp20bomcodigo, " +
        " abm01codigo, abm01na, abp10codigo, abp10descr " +
        baseDaSql +
        " ORDER BY abb01num DESC ";
        
        Long qtdTotalDeRegistros = getAcessoAoBanco().obterLong(sqlCount, parametrosArray);
        List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap(sqlDados, true, requisicao.getPagina(), requisicao.getTamanhoDaPagina(), parametrosArray);

        put("resposta", new RespostaDoF8(qtdTotalDeRegistros, colunas, dados));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==