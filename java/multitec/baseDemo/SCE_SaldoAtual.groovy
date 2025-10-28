package multitec.baseDemo

import br.com.multiorm.DBColumn
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SCE_SaldoAtual extends RelatorioBase{
    @Override
    String getNomeTarefa() {
        return "Saldo Atual"
    }

    @Override
    Map<String, Object> criarValoresIniciais() {
        return null
    }

    @Override
    DadosParaDownload executar() {
        String codigoItemInicial = get("itemIni")
        String codigoItemFinal = get("itemFin")

        params.put("aac10rs", getVariaveis().getAac10().getAac10rs());

        String nomeTabelaProvisoriaItemFinal = getSession().criaTabelaProvisoria(
                "saldo_itens",
                new DBColumn("codigoItem", DBColumn.TipoCampo.STRING),
                new DBColumn("nomeItem", DBColumn.TipoCampo.STRING),
                new DBColumn("saldoTotal", DBColumn.TipoCampo.DECIMAL, new BigDecimal("20.6")),
        )
        String estruturaCodigoItem = buscarEstruturaCodigoItem()
        List<String> gruposItens = estruturaCodigoItem.split("\\|")

        comporDadosTabelaProvisoriaItensFinais(nomeTabelaProvisoriaItemFinal, codigoItemInicial, codigoItemFinal)

        List<TableMap> saldosPorGrupo = comporSaldos(gruposItens.get(0), nomeTabelaProvisoriaItemFinal)

        return gerarPDF(saldosPorGrupo)
    }

    private String buscarEstruturaCodigoItem(){
        return getAcessoAoBanco().buscarParametro("ESTRCODMAT","ABM01");
    }

    private List<TableMap> comporSaldos(String estruturaGrupo, String nomeTabela){
        int tamanhoGrupo = estruturaGrupo.length()
        List<TableMap> saldos = new ArrayList<>()

        List<TableMap> grupos = getSession().createQuery(
                " SELECT SUBSTRING(codigoItem, 1, "+tamanhoGrupo+") AS codigoGrupo, sum(saldoTotal) as saldoGrupo FROM " + nomeTabela +
                " GROUP BY SUBSTRING(codigoItem, 1, "+tamanhoGrupo+")"
        ).getListTableMap()

        for (TableMap grupo in grupos){
            List<TableMap> itensGrupo = getSession().createQuery(
                    " SELECT * FROM " + nomeTabela +
                    " WHERE codigoItem LIKE '"+grupo.getString("codigoGrupo")+"%' "
            ).getListTableMap()

            for(TableMap item in itensGrupo){
                item.putAll(grupo)
                saldos.add(item)
            }
        }

        return saldos
    }

    private void comporDadosTabelaProvisoriaItensFinais(String nomeTabela, String codigoItemInicial, String codigoItemFinal){
        String estruturaCodigoItem = buscarEstruturaCodigoItem()
        if(estruturaCodigoItem == null) interromper("Estrutura do código do item não encontrada!")
        int tamanhoCodigoCompleto = estruturaCodigoItem.replace("|", "").length();
        String whereItem =
                codigoItemInicial != null && codigoItemFinal != null ? " AND abm01codigo BETWEEN '"+codigoItemInicial+"' AND '"+codigoItemFinal+"' " :
                codigoItemInicial != null ? " AND abm01codigo >= '"+codigoItemInicial+"' " :
                codigoItemFinal != null ? " AND abm01codigo <= '"+codigoItemFinal+"' " :
                "";

        getSession().createQuery(
                " INSERT INTO " + nomeTabela + " (codigoItem, nomeItem, saldoTotal) " +
                " SELECT abm01codigo, abm01na, SUM(bcc02qt) FROM Bcc02 " +
                " INNER JOIN Abm01 ON abm01id = bcc02item " +
                " WHERE LENGTH(abm01codigo) = " + tamanhoCodigoCompleto +
                whereItem +
                " GROUP BY abm01codigo, abm01na "
        ).executeUpdate()
    }
}
//meta-sis-eyJkZXNjciI6IlNhbGRvIEF0dWFsIiwidGlwbyI6InJlbGF0b3JpbyJ9