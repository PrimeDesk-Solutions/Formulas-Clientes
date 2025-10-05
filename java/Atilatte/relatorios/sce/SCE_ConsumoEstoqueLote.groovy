package Atilatte.relatorios.sce

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import com.ctc.wstx.util.DataUtil
import com.fasterxml.jackson.databind.BeanProperty
import com.lowagie.text.Table
import org.apache.poi.ss.usermodel.DateUtil;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCE_ConsumoEstoqueLote extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Consumo Estoque por Lote (Pedidos)";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("impressao","0")
        filtrosDefault.put("detalhamento","0")
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> lotesFaturamento = getListLong("lotesFaturamento");
        List<Long> itens = getListLong("itens");
        List<Long> entidades = getListLong("entidades");
        Integer impressao = getInteger("impressao");
        Integer detalhamento = getInteger("detalhamento");


        verificaSePossuiLotesProcessados(lotesFaturamento);

        if(lotesFaturamento == null || lotesFaturamento.size() == 0){
            lotesFaturamento = buscarLotesFaturar();
        }
        List<TableMap> dados = detalhamento == 0 ?  buscarPedidosVendaSintetico(lotesFaturamento, itens, entidades) : buscarPedidosVendaAnalitico(lotesFaturamento, itens, entidades);
        List<TableMap> registros = new ArrayList();
        List<TableMap> listItens = new ArrayList<>();
        HashMap<Long, BigDecimal> lotesUtilizados = new HashMap<>();



        for(dado in dados){
            Long idItem = dado.getLong("idItem");
            String codItem = dado.getString("codItem");
            String naItem = dado.getString("naItem");
            List<TableMap> saldosItem = buscarSaldoAtualItem(idItem);
            Long idDoc = dado.getLong("eaa01id");
            TableMap itemSemSaldo = new TableMap();
            Boolean contemSaldoDisponivel = true;
            def codigoItem = dado.getString("codItem")
            def countLotesIndisponivel = 0;
            def countLotesDisponiveis= 0;
            def qtdPedido = dado.getBigDecimal_Zero("qtdPedido");
            def saldoPedido = 0;
            def saldoFinalLote;

            if(saldosItem != null && saldosItem.size() > 0 ){
                for (saldo in saldosItem){

                    String lote = saldo.getString("lote");
                    LocalDate dataAtual = LocalDate.now();
                    BigDecimal percentEntidade = dado.getBigDecimal_Zero("vidaUtilEntidade") / 100;
                    LocalDate fabricacao = saldo.getDate("fabricacao");
                    LocalDate validade = saldo.getDate("validade");
                    if(fabricacao == null || validade == null) continue;
                    def idadeItem = ChronoUnit.DAYS.between(dataAtual,fabricacao );
                    def diasValidadeItem = ChronoUnit.DAYS.between(validade,fabricacao );
                    if(diasValidadeItem == 0) interromper("A data de validade e fabricação do item " + codItem + " - " + naItem + " no lote "+lote+" estão iguais. Verifique os lançamentos do item.")
                    def percentVidaUtilItem = idadeItem / diasValidadeItem
                    def percentValidLote = 1 - percentVidaUtilItem;
                    def qtdLote = saldo.getBigDecimal_Zero("qtd");
                    def idSaldo = saldo.getLong("bcc0201id");

                    if(percentValidLote >= percentEntidade ){
                        countLotesDisponiveis++

                        //Verifica se o pedido já foi atendido totalmente (Saldo Pedido = 0), se sim, não inlcui mais linhas abatendo os saldos do pedido
                        if(!listItens.stream().anyMatch {tmSaldos -> tmSaldos.getBigDecimal("saldoEntregar") == 0 &&
                                tmSaldos.getLong("key") == idDoc &&
                                tmSaldos.getString("codItem") == codigoItem }){

                            qtdLote = verificarSaldoUtilizado(lotesUtilizados, idSaldo, saldo.getBigDecimal_Zero("qtd"));

                            if(qtdLote > 0){
                                if(countLotesDisponiveis == 1){
                                    saldoPedido = qtdLote - qtdPedido;
                                    if(saldoPedido < 0){
                                        saldoFinalLote = 0
                                    }else{
                                        saldoFinalLote = qtdLote - qtdPedido
                                    }
                                }else{
                                    if(saldoPedido < 0){
                                        saldoPedido = qtdLote + saldoPedido;
                                        saldoFinalLote = qtdLote - (qtdLote - saldoPedido)
                                    }else{
                                        saldoPedido = qtdLote - saldoPedido;
                                        saldoFinalLote = qtdLote - qtdPedido;
                                    }
                                }

                                if (saldoPedido > 0) saldoPedido = 0;

                                saldo.put("key", idDoc);
                                saldo.put("loteItem", saldo.getString("lote") );
                                saldo.put("codItem", dado.getString("codItem"));
                                saldo.put("naItem", dado.getString("naItem"));
                                saldo.put("qtdPedido", dado.getBigDecimal_Zero("qtdPedido"));
                                saldo.put("qtdLote", qtdLote);
                                saldo.put("saldoEntregar", saldoPedido);
                                saldo.put("saldoFinalLote", saldoFinalLote);
                                saldo.put("loteFaturamento", dado.getString("loteFaturamento"));
                                saldo.put("naEntidade", dado.getString("naEntidade"));
                                saldo.put("codEntidade", dado.getString("codEntidade"));
                                saldo.put("vidaUtilEntidade", dado.getString("vidaUtilEntidade"));
                                saldo.put("numPed", dado.getString("numPed"));

                                lotesUtilizados.put(idSaldo, saldoFinalLote);
                            }else{
                                saldo.put("key", idDoc);
                                saldo.put("loteItem", saldo.getString("lote") + " QUANTIDADE INSUFICIENTE");
                                saldo.put("codItem", dado.getString("codItem"));
                                saldo.put("naItem", dado.getString("naItem"));
                                saldo.put("qtdPedido", dado.getBigDecimal_Zero("qtdPedido"));
                                saldo.put("qtdLote", 0);
                                saldo.put("saldoEntregar", dado.getBigDecimal_Zero("qtdPedido"));
                                saldo.put("saldoFinalLote", 0);
                                saldo.put("loteFaturamento", dado.getString("loteFaturamento"));
                                saldo.put("naEntidade", dado.getString("naEntidade"));
                                saldo.put("codEntidade", dado.getString("codEntidade"));
                                saldo.put("vidaUtilEntidade", dado.getString("vidaUtilEntidade"));
                                saldo.put("numPed", dado.getString("numPed"));
                            }

                            def qtdConsumidaLote = saldo.getBigDecimal_Zero("qtdLote") - saldo.getBigDecimal_Zero("saldoFinalLote")

                            saldo.put("qtdConsumidaLote", qtdConsumidaLote)
                            listItens.add(saldo);

                            saldoPedido = saldo.getBigDecimal("saldoEntregar");

                        }
                    }else{
                        countLotesIndisponivel++;

                        // Verifica se todos os lotes do item não atende a entidade
                        if(countLotesIndisponivel == saldosItem.size()) contemSaldoDisponivel = false;
                    }
                }
            }else{ // Itens sem saldos em estoque
                itemSemSaldo.put("key", idDoc);
                itemSemSaldo.put("qtdPedido", dado.getBigDecimal_Zero("qtdPedido"));
                itemSemSaldo.put("qtdLote", new BigDecimal(0));
                itemSemSaldo.put("codItem", dado.getString("codItem"));
                itemSemSaldo.put("naItem", dado.getString("naItem"));
                itemSemSaldo.put("loteItem", "SEM SALDO EM ESTOQUE");
                itemSemSaldo.put("qtdLote", new BigDecimal(0));
                itemSemSaldo.put("saldoEntregar", new BigDecimal(0));
                itemSemSaldo.put("saldoFinalLote", new BigDecimal(0));
                itemSemSaldo.put("loteFaturamento", dado.getString("loteFaturamento"));
                itemSemSaldo.put("naEntidade", dado.getString("naEntidade"));
                itemSemSaldo.put("codEntidade", dado.getString("codEntidade"));
                itemSemSaldo.put("vidaUtilEntidade", dado.getString("vidaUtilEntidade"));
                itemSemSaldo.put("numPed", dado.getString("numPed"));

                listItens.add(itemSemSaldo)
            }

            // Itens ao qual nenhum lote atenda a entidade (% vida útil menor do que a entidade)
            if(!contemSaldoDisponivel){
                itemSemSaldo.put("key", idDoc);
                itemSemSaldo.put("qtdPedido", dado.getBigDecimal_Zero("qtdPedido"));
                itemSemSaldo.put("qtdLote", new BigDecimal(0));
                itemSemSaldo.put("codItem", dado.getString("codItem"));
                itemSemSaldo.put("naItem", dado.getString("naItem"));
                itemSemSaldo.put("loteItem", "NENHUM LOTE DISPONIVEL");
                itemSemSaldo.put("qtdLote", new BigDecimal(0));
                itemSemSaldo.put("saldoEntregar", new BigDecimal(0));
                itemSemSaldo.put("saldoFinalLote", new BigDecimal(0));
                itemSemSaldo.put("loteFaturamento", dado.getString("loteFaturamento"));
                itemSemSaldo.put("naEntidade", dado.getString("naEntidade"));
                itemSemSaldo.put("codEntidade", dado.getString("codEntidade"));
                itemSemSaldo.put("vidaUtilEntidade", dado.getString("vidaUtilEntidade"));
                itemSemSaldo.put("numPed", dado.getString("numPed"));

                listItens.add(itemSemSaldo)
            }

            dado.put("key", idDoc)

        }

        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("DsSub1", listItens, "key", "key");

        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCE_ConsumoEstoqueLote_s1"))

        params.put("titulo", "SCE - Consumo Estoque por Lote");
        params.put("empresa",obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());
        params.put("empresa",obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());

        if(impressao == 0 && detalhamento == 0){
            return gerarPDF("SCE_ConsumoEstoqueLoteSinteticoPDF", listItens);
        }else if(impressao == 1 && detalhamento == 0){
            return gerarXLSX("SCE_ConsumoEstoqueLoteSinteticoExcel", listItens);
        }else if(impressao == 0 && detalhamento == 1){
            return gerarPDF("SCE_ConsumoEstoqueLoteAnaliticoPDF", dsPrincipal);
        }else{
            return gerarXLSX("SCE_ConsumoEstoqueLoteAnaliticoExcel", listItens);
        }

    }

    private List<TableMap> buscarPedidosVendaAnalitico(List<Long> lotesFaturamento, List<Long> itens, List<Long> entidades ){
        String whereLotesFaturamento = lotesFaturamento != null && lotesFaturamento.size() > 0 ? "AND bfb01id IN (:lotesFaturamento) " : "";
        String whereItens = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "AND abe01id IN (:entidades) " : "";

        Parametro parametroLoteFaturamento =  lotesFaturamento != null && lotesFaturamento.size() > 0 ? Parametro.criar("lotesFaturamento", lotesFaturamento) : null;
        Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;

        String sql = "SELECT eaa01id, abm01id as idItem, abb01num AS numPed, abe01codigo AS codEntidade, abe01na AS naEntidade, abm01codigo AS codItem, " +
                "abm01na AS naItem, abe02vidautil as vidaUtilEntidade, bfb01lote as loteFaturamento, eaa0103qtcoml as qtdPedido, abm01livre as ordem " +
                "FROM bfb01 " +
                "INNER JOIN bfb0101 ON bfb0101lote = bfb01id " +
                "INNER JOIN abb01 ON abb01id = bfb0101central " +
                "INNER JOIN abe01 on abe01id = abb01ent " +
                "INNER JOIN eaa01 ON eaa01central = abb01id " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "INNER JOIN abm0101 ON abm0101item = abm01id "+
                "LEFT JOIN abe02 ON abe02ent = abe01id " +
                "WHERE TRUE "+
                whereLotesFaturamento +
                whereItens +
                whereEntidades +
                "order by abb01num,abm01livre, abm01codigo ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroLoteFaturamento, parametroItens, parametroEntidades)
    }
    private List<TableMap> buscarPedidosVendaSintetico(List<Long> lotesFaturamento, List<Long> itens, List<Long> entidades ){
        String whereLotesFaturamento = lotesFaturamento != null && lotesFaturamento.size() > 0 ? "AND bfb01id IN (:lotesFaturamento) " : "";
        String whereItens = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "AND abe01id IN (:entidades) " : "";

        Parametro parametroLoteFaturamento =  lotesFaturamento != null && lotesFaturamento.size() > 0 ? Parametro.criar("lotesFaturamento", lotesFaturamento) : null;
        Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;

        String sql = "SELECT eaa01id, abm01id as idItem, abb01num AS numPed, abe01codigo AS codEntidade, abe01na AS naEntidade, abm01codigo AS codItem, " +
                "abm01na AS naItem, abe02vidautil as vidaUtilEntidade, bfb01lote as loteFaturamento, eaa0103qtcoml as qtdPedido " +
                "FROM bfb01 " +
                "INNER JOIN bfb0101 ON bfb0101lote = bfb01id " +
                "INNER JOIN abb01 ON abb01id = bfb0101central " +
                "INNER JOIN abe01 on abe01id = abb01ent " +
                "INNER JOIN eaa01 ON eaa01central = abb01id " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "LEFT JOIN abe02 ON abe02ent = abe01id " +
                "WHERE TRUE "+
                whereLotesFaturamento +
                whereItens +
                whereEntidades +
                "order by abm01codigo, abb01num";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroLoteFaturamento, parametroItens, parametroEntidades)
    }

    private List<TableMap> buscarSaldoAtualItem(Long idItem){
        String sql = "SELECT bcc0201id,bcc0201qt as qtd, bcc0201lote as lote, bcc0201validade as validade, bcc0201fabric as fabricacao " +
                "FROM bcc02 " +
                "INNER JOIN bcc0201 ON bcc0201saldo = bcc02id " +
                "WHERE bcc02item = :idItem " +
                "and bcc02status = 112003 " +
                "and bcc02ctrl0 = 112114 "+
                "ORDER BY bcc0201fabric, bcc0201validade, bcc0201qt "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idItem", idItem));
    }

    private BigDecimal verificarSaldoUtilizado(HashMap<Long, BigDecimal> lotesUtilizados, Long idLote, BigDecimal qtdOriginal){
        BigDecimal saldo = qtdOriginal

        for (lote in lotesUtilizados){
            if (lote.key == idLote){
                saldo = lote.value;
            }
        }

        return saldo;
    }

    private List<Long> buscarLotesFaturar(){
        String sql = "SELECT bfb01id " +
                "FROM bfb01 " +
                "INNER JOIN bfb0101 on bfb0101lote = bfb01id " +
                "WHERE bfb0101romproc = 0"

        return getAcessoAoBanco().obterListaDeLong(sql);
    }

    private void verificaSePossuiLotesProcessados(List<Long> idsLotes){
        String sql = "SELECT bfb01id, bfb0101romproc, bfb01lote " +
                "FROM bfb01  " +
                "INNER JOIN bfb0101 on bfb0101lote = bfb01id " +
                "WHERE bfb01id in (:idsLotes) ";

        List<TableMap> lotes = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idsLotes", idsLotes))

        for(lote in lotes){
            String nomeLote = lote.getString("bfb01lote");

            if(lote.getInteger("bfb0101romproc") == 1) interromper("O lote " +nomeLote+ " já foi processado. Retire o lote do filtro para prosseguir com o relatório.")
        }
    }
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIENvbnN1bW8gRXN0b3F1ZSBwb3IgTG90ZSAoUGVkaWRvcykiLCJ0aXBvIjoicmVsYXRvcmlvIn0=