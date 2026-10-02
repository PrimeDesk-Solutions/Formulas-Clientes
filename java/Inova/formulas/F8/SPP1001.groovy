package Inova.formulas.F8;

import br.com.multitec.utils.collections.TableMap;
import sam.core.politica.OperacaoDeSeguranca;
import sam.dicdados.FormulaTipo;
import sam.dto.cadastro.f8formula.ColunaF8;
import sam.dto.cadastro.f8formula.RespostaDoF8;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.FiltroDoF8;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.RequisicaoDoF8;
import java.util.stream.Collectors;



class SPP1001 extends FormulaBase {
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.F8;
    }

    @Override
    public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao");

        List<ColunaF8> colunas = new ArrayList<ColunaF8>();
        colunas.add(new ColunaF8("aah01codigo",  "Doc"));
        colunas.add(new ColunaF8("abb01num",  "Número"));
        colunas.add(ColunaF8.criarAPartirDaColunaDoSAM("abb01data"));
        colunas.add(new ColunaF8("bab01opp", "OP Principal"));
        colunas.add(new ColunaF8("bab01status", "Status OP"));
        colunas.add(new ColunaF8("abp10codigo",  "Processo"));
        colunas.add(new ColunaF8("abp10descr",  "Descrição"));
        colunas.add(new ColunaF8("abm01codigo",  "Produto"));
        colunas.add(new ColunaF8("abm01na",  "Nome Abreviado"));
        colunas.add(new ColunaF8("abp20bomCodigo",  "BOM"));

        String whereFiltros = "";
        List<Parametro> parametros = new ArrayList<Parametro>();
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

        Parametro[] parametrosArray = parametros.toArray();

        String baseDaSql = " FROM Bab01 " +
                            " INNER JOIN abb01 ON abb01id = bab01central " +
                            " INNER JOIN aah01 ON aah01id = abb01tipo " +
                            " INNER  JOIN abp20 ON abp20id = bab01comp " +
                            " INNER JOIN abm01 ON abm01id = abp20item " +
                            " INNER JOIN abp10 ON abp10id = bab01proc " +
                            "WHERE TRUE " +
                            obterWherePadrao("Bab01") + " " +
                            whereFiltros + whereBusca;

        String sqlCount = " SELECT count(*) as qtdTotal " + baseDaSql;

        String sqlDados =
                        "SELECT bab01id AS id, aah01codigo AS Doc, abb01num, abb01data, bab01opp, bab01status, abp10codigo, abp10descr, abm01codigo, abm01na, abp20bomcodigo " +
                        baseDaSql;

        Long qtdTotalDeRegistros = getAcessoAoBanco().obterLong(sqlCount, parametrosArray);
        List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap(sqlDados, true, requisicao.getPagina(), requisicao.getTamanhoDaPagina(), parametrosArray);

        put("resposta", new RespostaDoF8(qtdTotalDeRegistros, colunas, dados));
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==