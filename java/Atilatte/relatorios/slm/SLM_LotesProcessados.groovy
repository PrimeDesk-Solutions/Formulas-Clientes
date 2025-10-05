package Atilatte.relatorios.slm

import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.TableMapDataSource;
import sam.server.samdev.utils.Parametro;

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SLM_LotesProcessados extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SLM - Lotes Processados";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("impressao","0")
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsLotes = getListLong("capaLote");
        LocalDate[] datas = getIntervaloDatas("data");
        Integer impressao = getInteger("impressao");

        // Data Atual Sistema
        LocalDateTime dataAtual = LocalDateTime.now();

        // Formato de data e hora
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Formata a data e hora de acordo com o formato especificado
        String emissaoRelatorio = dataAtual.format(formatter);

        List<TableMap> lotes = buscarLotes(idsLotes, datas );
        List<TableMap> listPedidos = new ArrayList();
        List<TableMap> registrosexcel = new ArrayList();

        for(lote in lotes){
            Long idLote = lote.getLong("idLote");
            lote.put("key", idLote);
            String statusLote = "";
            Integer countItensColetados = 0;
            Integer countItensLote = 0;
            TableMap informacoesColeta = new TableMap();
            TableMap tmRomaneioLote = verificarRomaneioLote(idLote);
            List<TableMap> pedidos = tmRomaneioLote.getInteger("bfb0101romproc") == 0 ? buscarPedidosLoteSemRomaneio(idLote) : buscarPedidosLote(idLote);

            for(pedido in pedidos){
                pedido.put("key", idLote);
                countItensLote++;

                if(pedido.getInteger("ajustado") == 1 || pedido.getInteger("realizaColeta") == 0 ) countItensColetados++

                listPedidos.add(pedido);
            }

            if(tmRomaneioLote.getInteger("bfb0101romproc") == 1){
                if(countItensColetados == countItensLote){
                    statusLote = "Coleta Finalizada";
                }else if(countItensColetados > 0){
                    statusLote = "Coleta Iniciada (Ou Contém Leite de Saquinho)";
                }else{
                    statusLote = "Coleta não iniciada";
                }

                if(statusLote == "Coleta Finalizada" || statusLote == "Coleta Iniciada (Ou Contém Leite de Saquinho)" ) informacoesColeta = buscarInformacoesColeta(idLote, statusLote);

                if(informacoesColeta != null) lote.putAll(informacoesColeta);
            }else{
                statusLote = "Lote Não Processado";
            }

            lote.put("statusLote", statusLote);

        }



        params.put("titulo", "SLM - Lotes Processados");
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        params.put("emissaoRelatorio", emissaoRelatorio);

        TableMapDataSource dsPrincipal = new TableMapDataSource(lotes);
        dsPrincipal.addSubDataSource("DsSub1", listPedidos, "key", "key");

        adicionarParametro("SUBREPORT_DIR1", carregarArquivoRelatorio("SLM_LotesProcessados(PDF)_s1"))

        if(impressao == 1) return gerarXLSX("SLM_LotesProcessados(Excel)", lotes);

        return gerarPDF("SLM_LotesProcessados(PDF)", dsPrincipal);
    }

    private List<TableMap> buscarLotes(List<Long> idsLotes, LocalDate[] datas){
        String whereLote = idsLotes != null && idsLotes.size() > 0 ? "where bfb01id in (:idsLotes) " : "";
        String whereDatas = datas != null ? "and bfb01data between :dtInicio and :dtFinal " : "";

        Parametro parametroLote = idsLotes != null && idsLotes.size() > 0 ? Parametro.criar("idsLotes", idsLotes) : null;
        Parametro parametroDataIni = datas != null ? Parametro.criar("dtInicio", datas[0]) : null;
        Parametro parametroDataFin = datas != null ? Parametro.criar("dtFinal", datas[1]) : null;

        String sql =   "select bfb01id as idLote, bfb01lote as nomeLote, cast(bfb01json ->> 'caixa' as numeric(18,6)) as qtdCaixaLote, "+
					"cast(bfb01json ->> 'peso_bruto' as numeric(18,6)) as pesoBrutoLote,cast(bfb01json ->> 'm3' as numeric(18,6)) as m3Lote, "+
					"aah20capkg as capacidadeKgVeiculo, aah20capm3 as capacidadeM3Veiculo "+
					"from bfb01 "+
					"inner join aah20 on aah20id = bfb01veiculo "+
		                whereLote+
		                whereDatas+
		                "order by bfb01lote";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroLote, parametroDataIni, parametroDataFin);

    }

    private List<TableMap> buscarPedidosLote(Long idLote){

        String whereLote =  "where bfb01id = :idLote ";
        Parametro parametroLote = Parametro.criar("idLote", idLote);

        String sql = "select abm01codigo, abm01na, bfa01011ajustado as ajustado, bfa01011ajustado, cast(abm0101json ->> 'realiza_coleta' as integer) as realizaColeta, abm01id,\n" +
                    "pedido.abb01num as numPedido, case when bfb0101romproc = 0 then '0-Não Processado' else '1-Processado' end  as statusPedido, CAST(eaa01json ->> 'caixa' as numeric(18,6)) as caixaPedido, romaneio.abb01num as numRomaneio, " +
                    "CAST(eaa01json ->> 'peso_bruto' as numeric(18,6)) as pesoBrutoPedido, CAST(eaa01json ->> 'm3' as numeric(18,6)) as m3Pedido " +
                    "from bfa01\n" +
                    "inner join abb01 as romaneio on romaneio.abb01id = bfa01central\n" +
                    "inner join bfa0101 on bfa0101rom = bfa01id\n" +
                    "inner join bfa01011 on bfa01011item = bfa0101id\n" +
                    "inner join bfb0101 on bfb0101central = bfa01docscv\n" +
                    "inner join bfb01 on bfb01id = bfb0101lote\n" +
                    "inner join eaa0103 on eaa0103id = bfa0101item\n" +
                    "inner join eaa01 on eaa01id = eaa0103doc\n" +
                    "inner join abb01 as pedido on pedido.abb01id = eaa01central\n" +
                    "inner join abm01 on abm01id = eaa0103item\n" +
                    "inner join abm0101 on abm0101item = abm01id\n" +
                    whereLote+
                    "order by pedido.abb01num ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroLote);
    }
    private List<TableMap> buscarPedidosLoteSemRomaneio(Long idLote){
        String whereLote =  "where bfb0101lote = :idLote ";
        Parametro parametroLote = Parametro.criar("idLote", idLote);

        String sql = "select abb01num as numPedido, case when bfb0101romproc = 0 then '0-Não Processado' else '1-Processado' end  as statusPedido, " +
        			"CAST(eaa01json ->> 'caixa' as numeric(18,6)) as caixaPedido, "+
        			"CAST(eaa01json ->> 'peso_bruto' as numeric(18,6)) as pesoBrutoPedido, CAST(eaa01json ->> 'm3' as numeric(18,6)) as m3Pedido " +
                    "from bfb0101 " +
                    "inner join abb01 on abb01id = bfb0101central " +
                    "inner join eaa01 on eaa01central = abb01id"
                    whereLote +
                    "order by abb01id ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroLote )
    }

    private TableMap buscarInformacoesColeta(Long idLote, String statusLote){
        String whereLote = "WHERE bfb01id = :idLote ";

        String fieldColeta = statusLote == "Coleta Finalizada" ? "MIN(abm70uldata) AS dataInicioColeta, MAX(abm70uldata) AS dataFimColeta, MIN(abm70ulhora) AS horaInicioColeta, MAX(abm70ulhora) AS horaFimColeta, aab10user AS userColeta " : "MIN(abm70uldata) AS dataInicioColeta, MIN(abm70ulhora) AS horaInicioColeta, aab10user AS userColeta ";

        String sql = "SELECT " + fieldColeta +
                " FROM bfa01  " +
                " INNER JOIN bfb0101 ON bfb0101central = bfa01docscv " +
                " INNER JOIN bfb01 ON bfb01id = bfb0101lote " +
                " INNER JOIN abb01 as romaneio ON romaneio.abb01id = bfa01central  " +
                " INNER JOIN bfa0101 ON bfa0101rom = bfa01id  " +
                " INNER JOIN bfa01011 ON bfa01011item = bfa0101id  " +
                " INNER JOIN eaa0103 ON eaa0103id = bfa0101item  " +
                " INNER JOIN abm01 ON abm01id = eaa0103item  " +
                " INNER JOIN abm70 ON abm70idunidrom = bfa01011id  " +
                " INNER JOIN  abb01 as pedido ON pedido.abb01id= bfa01docscv  " +
                " INNER JOIN aab10 ON aab10id = abm70uluser " +
                whereLote +
                "GROUP BY aab10user ";

        return getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("idLote", idLote));

    }

    private TableMap verificarRomaneioLote(Long idLote){

        String sql = "select distinct bfb0101romproc from bfb0101 where bfb0101lote = :idLote ";

        return getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("idLote", idLote));
    }


}
//meta-sis-eyJkZXNjciI6IlNMTSAtIExvdGVzIFByb2Nlc3NhZG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9