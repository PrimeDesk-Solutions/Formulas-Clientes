package Atilatte.formulas.f8;


import br.com.multitec.utils.collections.TableMap;
import sam.core.politica.OperacaoDeSeguranca;
import sam.dicdados.FormulaTipo;
import sam.dto.cadastro.f8formula.ColunaF8;
import sam.dto.cadastro.f8formula.RespostaDoF8;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.FiltroDoF8;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.RequisicaoDoF8

import java.util.stream.Collectors;


public class SCV_Eaa01 extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.F8;
    }

    @Override
    public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao");

        List<ColunaF8> colunas = new ArrayList<ColunaF8>();
        colunas.add(new ColunaF8("aah01codigo", "Tipo"));
        colunas.add(new ColunaF8("aah01nome", "Nome do Tipo"));
        colunas.add(new ColunaF8("abb01num", "Número"));
        colunas.add(new ColunaF8("abb01serie", "Série"));
        colunas.add(new ColunaF8("abb01data", "Data"));
        colunas.add(new ColunaF8("abe01codigo", "Entidade"));
        colunas.add(new ColunaF8("abe01nome", "Nome Entidade"));
        colunas.add(new ColunaF8("abb01valor", "Valor"));
        colunas.add(new ColunaF8("abb10codigo", "Cód Oper"));
        colunas.add(new ColunaF8("abb10descr", "Descr Oper"));

        String whereFiltros = "";

        List<Parametro> parametros = new ArrayList<Parametro>();

        // Mantém os filtros de tela definidos no F8
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

        // Considera somente os pedidos de venda
        whereFiltros += "AND eaa01clasDoc = 0 AND eaa01esMov = 1";

        // Filtra a politica de segurança do usuário e empresa ativa
        whereFiltros += obterWherePadrao("Eaa01", "AND");

        // Filtra somente os pedidos com o tipo 31 e 39
        whereFiltros = "AND aah01codigo IN ('31', '39')"

        Parametro[] parametrosArray = parametros.toArray();

        String baseSql =
        " FROM eaa01 " +
        " INNER JOIN abb01 ON abb01id = eaa01central " +
        " INNER JOIN aah01 ON aah01id = abb01tipo " +
        " INNER JOIN abe01 ON abe01id = abb01ent " +
        " INNER JOIN abd01 ON abd01id = eaa01pcd " +
        " INNER JOIN abb10 ON abb10id = abd01opercod "+
        " WHERE TRUE " +
        whereBusca +
        obterWherePadrao("Eaa01") + " " +
        whereFiltros;

        String sqlCount = "SELECT COUNT(*) as qtdTotal " + baseSql;

        String sqlFields =
        "SELECT eaa01id as id, aah01codigo, aah01nome, abb01num, abb01serie, abb01data, " +
        "abe01codigo, abe01nome, abb01valor, abb10codigo, abb10descr "+
        baseSql +
        "ORDER BY abb01data desc, abb01num desc ";

        Long qtdTotalRegistros = getAcessoAoBanco().obterLong(sqlCount, parametrosArray);

        List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap(sqlFields, true, requisicao.getPagina(), requisicao.getTamanhoDaPagina(), parametrosArray);

        put("resposta", new RespostaDoF8(qtdTotalRegistros, colunas, dados));
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==