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


class F8_SRF1001 extends FormulaBase {
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.F8;
	}

	@Override
	public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao");

        List<ColunaF8> colunas = new ArrayList<ColunaF8>();
        colunas.add(new ColunaF8("aah01codigo",  "Tipo"));
        colunas.add(new ColunaF8("aah01nome",  "Nome do Tipo"));
        colunas.add(new ColunaF8("abb01num",  "Número"));
        colunas.add(new ColunaF8("abb01serie",  "Série"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abb01data"));
        colunas.add(new ColunaF8("abe01codigo", "Entidade"));
        colunas.add(new ColunaF8("abe01na", "Nome Entidade"));
		colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abb01valor"));
        colunas.add(new ColunaF8("abb10codigo",  "Cód Oper"));
        colunas.add(new ColunaF8("abb10descr",  "Descr Oper"));

		String whereFiltros = "";
		List<Parametro> parametros = new ArrayList<Parametro>();
		if (requisicao.getFiltros() != null && requisicao.getFiltros().size() > 0) {
			for(FiltroDoF8 filtro : requisicao.getFiltros()){
				whereFiltros += filtro.getWhere().contains("false") ? "" : filtro.getWhere() + " OR ";
				parametros.addAll(filtro.getParametros());
			}
			whereFiltros = whereFiltros.isEmpty() ? "" : "AND ("+ whereFiltros.substring(0, whereFiltros.length() - 3) +")"
		}

	    //Instrução para considerar somente Documentos de SRF de Entrada
		whereFiltros += " AND eaa01clasDoc = 1 AND eaa01esMov = 0 ";
		
	    //Instrução considerando a política de segurança "Acessar" definida no tipo de documento
	    whereFiltros += " AND " + getVariaveis().getPermissoes().montarWherePelaColuna("aah01psAcessar", OperacaoDeSeguranca.TIPO_DE_DOCUMENTO_ACESSO);
				
        Parametro[] parametrosArray = parametros.toArray();

        String baseDaSql = 
        " FROM Eaa01 " +
        " INNER JOIN Abb01 ON eaa01central = abb01id " +
        " INNER JOIN Aah01 ON abb01tipo = aah01id " +
        " LEFT JOIN Abe01 ON abb01ent = abe01id " +
        " LEFT JOIN Abb10 ON abb01operCod = abb10id " +
        " WHERE true " + obterWherePadrao("Eaa01") +
        " " + whereFiltros;

        String sqlCount = " SELECT count(*) as qtdTotal " + baseDaSql;

        String sqlDados = 
        " SELECT eaa01id as id, aah01codigo, aah01nome, abb01num, abb01serie, abb01data, " +
        " abe01codigo, abe01na, abb01valor, abb10codigo, abb10descr " +
        baseDaSql +
        " ORDER BY abb01data DESC, abb01num DESC ";
        
        Long qtdTotalDeRegistros = getAcessoAoBanco().obterLong(sqlCount, parametrosArray);
        List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap(sqlDados, true, requisicao.getPagina(), requisicao.getTamanhoDaPagina(), parametrosArray);

        put("resposta", new RespostaDoF8(qtdTotalDeRegistros, colunas, dados));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==