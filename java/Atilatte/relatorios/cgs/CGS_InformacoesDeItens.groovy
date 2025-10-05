//Desevolvido por ROGER.
// Alterado por: Leonardo - Acrescentado filtro de campos livres na tela para buscar os campos livres do cadastro dos itens de forma dinamica.

package Atilatte.relatorios.cgs

import br.com.multitec.utils.collections.TableMap
import net.sf.jasperreports.components.items.ItemData;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

public class CGS_InformacoesDeidItens extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CGS - Informações De Itens";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idItens = getListLong("idItens");
        List<Integer> mps = getListInteger("mpms");
        List<TableMap> itens = buscandoRegistrosidItens(idItens, mps);
        String campoLivre1 = getString("campoLivre1");
        String campoLivre2 = getString("campoLivre2");
        String campoLivre3 = getString("campoLivre3");
        String campoLivre4 = getString("campoLivre4");
        String campoLivre5 = getString("campoLivre5");
        String campoLivre6 = getString("campoLivre6");


        Map<String, String> campos = new HashMap();

        campos.put("1", campoLivre1 != null ? campoLivre1 : null );
        campos.put("2", campoLivre2 != null ? campoLivre2 : null );
        campos.put("3", campoLivre3 != null ? campoLivre3 : null );
        campos.put("4", campoLivre4 != null ? campoLivre4 : null );
        campos.put("5", campoLivre5 != null ? campoLivre5 : null );
        campos.put("6", campoLivre6 != null ? campoLivre6 : null );

        for(item in itens){
            TableMap campoLivreItem = item.getTableMap("abm0101json") != null ? item.getTableMap("abm0101json") : new TableMap();
            Integer grupo = item.getInteger("grupo");
            String grupoFiscal = buscarGrupoFiscalItem(grupo);
            Integer tipo = item.getInteger("tipo");
            String tipoItem = buscarTipoItem(tipo)

            item.put("grupoFiscal", grupoFiscal);
            item.put("tipoItem", tipoItem);

            buscarCamposItem(item, campoLivreItem, campos);
        }

        return gerarXLSX("CGS_InformacoesDeItens", itens);

    }

    private List<TableMap> buscandoRegistrosidItens(List<Long> idItens, List<Integer> mps) {
        String whereEmpresa = "where abm01gc = :idEmpresa ";
        String whereItem = idItens != null && idItens.size() > 0 ? "and abm01id in (:idItens) " : "";
        String whereMpms = !mps.contains(-1) ? "and abm01tipo in (:mpms) " : "";

        Parametro parametrosEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());
        Parametro parametrosItem = idItens != null && idItens.size() > 0 ? Parametro.criar("idItens", idItens) : null;
        Parametro parametrosMps = !mps.contains(-1) ? Parametro.criar("mpms", mps) : null;

        String sql = "select abm0101json, abm01codigo as codItem, abm01tipo as tipo, abm01descr as descricaoItem,  " +
                        "abm11codigo as codEstoque, abm11descr as descricaoEstoque, abm13codigo as codComercial, abm13descr as descricaoComercial, " +
                        "abg01codigo as codNCM, abg01descr as descrNCM, abm0101cest as cest, aam06codigo as umu, aaj10codigo as codCST, aaj10descr as descrCST, abm12tipo as grupo " +
                        "from abm01  " +
                        "left join abm0101 on abm0101item = abm01id  " +
                        "left join abm11 on abm0101estoque = abm11id  " +
                        "left join abm12 on abm12id = abm0101fiscal " +
                        "left join aaj10 on aaj10id = abm12csticms " +
                        "left join abm13 on abm0101comercial = abm13id  " +
                        "left join abg01 on abg01id = abm0101ncm " +
                        "left join aam06 on aam06id = abm01umu "+
                        whereEmpresa +
                        whereItem +
                        whereMpms +
                        "order by abm01codigo";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametrosEmpresa, parametrosItem, parametrosMps);
    }
    private void buscarCamposItem(TableMap item, TableMap campoLivreItem, Map<String, String> campos){
        if (campos != null){
            for(campo in campos){
                if(campo.value != null){
                    String nomeCampo = buscarNomeCampoLivre(campo.value);
                    item.put("nomeCampo" + campo.key, nomeCampo);
                    item.put("valorCampo" + campo.key, campoLivreItem.get(campo.value));
                }
            }
        }
    }

    public String buscarNomeCampoLivre(String campo) {
        def sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql, criarParametroSql("nome", campo));

    }
    private String buscarGrupoFiscalItem(Integer grupo){
        String grupoFiscal = ""
        switch (grupo){
            case 0:
                grupoFiscal = "0-Mercadoria para Revenda";
                break;
            case 1:
                grupoFiscal = "1-Matéria-Prima";
                break;
            case 2:
                grupoFiscal = "2-Embalagem";
                break;
            case 3:
                grupoFiscal = "3-Produto em Processo";
                break;
            case 4:
                grupoFiscal = "4-Produto Acabado";
                break;
            case 5:
                grupoFiscal = "5-Subproduto";
                break;
            case 6:
                grupoFiscal = "6-Produto Intermediário";
                break;
            case 7:
                grupoFiscal = "7-Material de Uso e Consumo";
                break;
            case 8:
                grupoFiscal = "8-Ativo Imobilizado";
                break;
            case 9:
                grupoFiscal = "9-Serviços";
                break;
            case 10:
                grupoFiscal = "10-Outros insumos";
                break;
            case 99:
                grupoFiscal = "99-Outras";
                break;
           default:
                grupoFiscal = "SEM GRUPO FISCAL";
        }

        return grupoFiscal
    }
    private buscarTipoItem(Integer tipo){
        String tipoItem;
        switch (tipo){
            case 0:
                tipoItem = "0-Mat";
                break;
            case 1:
                tipoItem = "1-Prod";
                break;
            case 2:
                tipoItem = "2-Merc";
                break;
            default:
                tipoItem = "3-Serv"
        }

        return tipoItem
    }
}
//meta-sis-eyJkZXNjciI6IkNHUyAtIEluZm9ybWHDp8O1ZXMgRGUgSXRlbnMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNHUyAtIEluZm9ybWHDp8O1ZXMgRGUgSXRlbnMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=