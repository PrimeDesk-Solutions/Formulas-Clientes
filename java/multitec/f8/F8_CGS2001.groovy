package sam.server.samdev.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sam.dicdados.FormulaTipo;
import sam.dto.cadastro.f8formula.ColunaF8;
import sam.dto.cadastro.f8formula.RespostaDoF8;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.RequisicaoDoF8;

public class F8_CGS2001 extends FormulaBase {
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.F8;
    }

    @Override
    public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao"); // Obtem a requisição do F8

        //Define a lista de colunas que serão exibidas na Spread do cadastro
        //Atenção, o identificadorUnicoDaColuna deve ser o mesmo dos campos que serão retornados na SQL
        List<ColunaF8> colunas = new ArrayList<ColunaF8>(6);
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abe01codigo"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abe01na"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abe01nome"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abe01ni"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abe01ie"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abe01contribicms"));
        
        //Lista de parâmetros para os wheres dos Filtros e o da Busca
        List<Parametro> parametros = new ArrayList<Parametro>();
        
        //Monta o WHERE contendo os filtros que o usuário adicionou na tela
        String whereFiltros = "";
        if (requisicao.getFiltros() != null && requisicao.getFiltros().size() > 0) {
        	if(requisicao.getFiltros().size() > 0) {
        		whereFiltros = " AND " + requisicao.getFiltros().stream()
	        		.peek(filtro -> parametros.addAll(filtro.getParametros()))
	        		.map(filtro -> filtro.getWhere())
	        		.collect(Collectors.joining(" AND "));
        	}

    	}
        
        //Monta o WHERE a partir dos filtros montados pelo campo de busca
        String whereBusca = "";
        if(requisicao.getBuscas().size() > 0) {
        	whereBusca = " AND (" + requisicao.getBuscas()
        	.stream()
        	.peek(filtro -> parametros.addAll(filtro.getParametros()))
        	.map(filtro -> filtro.getWhere())
        	.collect(Collectors.joining(" OR ")) + ") ";
        }

        //Monta a SQL para obter os dados da página atual (NÃO ESQUECER DA COLUNA ID)
        String sql =
                " SELECT abe01id as id, abe01codigo, abe01na, abe01nome, abe01ni, abe01ie, abe01contribicms" +
        		" FROM Abe01 AS abe01" +
                " WHERE true " + 
        		obterWherePadrao("Abe01") +
                whereFiltros + 
                whereBusca +
                " ORDER BY abe01codigo ";
        System.out.println("SQL{" + sql + "}");
        List dados = getAcessoAoBanco().buscarListaDeTableMap(sql, true, requisicao.getPagina(), requisicao.getTamanhoDaPagina(), parametros.toArray(new Parametro[0]));

        //Monta a resposta do F8
        put("resposta", new RespostaDoF8(0L, colunas, dados));
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==