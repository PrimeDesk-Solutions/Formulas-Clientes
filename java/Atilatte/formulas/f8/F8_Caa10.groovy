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


class F8_Caa10 extends FormulaBase {
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.F8;
    }

    @Override
    public void executar() {
        RequisicaoDoF8 requisicao = get("requisicao");

        List<ColunaF8> colunas = new ArrayList<ColunaF8>();
        colunas.add(new ColunaF8("caa10num",  "Número"));
        colunas.add(new ColunaF8("caa10data",  "Data"));
        colunas.add(new ColunaF8("caa10nome",  "Nome Cliente"));
        colunas.add(new ColunaF8("tipo",  "Tipo Item"));
        colunas.add(new ColunaF8("codigoItem",  "Código Item"));
        colunas.add(new ColunaF8("abm01na",  "Descrição Item"));
        colunas.add(new ColunaF8("caa10dtenc",  "Data Encerramento"));
        colunas.add(new ColunaF8("aab10user",  "Atendente"));

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
                "from caa10 "+
                        "inner join aab10 on aab10id = caa10user  "+
                        "inner join abm01 on abm01id = caa10item "+
                        whereFiltros;

        String sqlCount = " SELECT count(*) as qtdTotal " + baseDaSql;

        String sqlDados =
                "select caa10id as id, caa10num,caa10nome,to_char(caa10data,'dd/mm/yyyy') as caa10data,aab10user,to_char(caa10dtenc,'dd/mm/yyyy') as caa10dtenc , "+
                        "case when abm01tipo = 0 then '0-Mat' when abm01tipo = 1 then '1-Prod' else '2-Merc' end as tipo, "+
                        "abm01codigo as codigoItem,abm01na "+
                        baseDaSql +
                        " order by caa10num desc "

        Long qtdTotalDeRegistros = getAcessoAoBanco().obterLong(sqlCount, parametrosArray);
        List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap(sqlDados, true, requisicao.getPagina(), requisicao.getTamanhoDaPagina(), parametrosArray);

        put("resposta", new RespostaDoF8(qtdTotalDeRegistros, colunas, dados));
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjUifQ==