package FormulasPadrao.relatorios.sce;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.utils.Parametro
import sam.model.entities.bc.Bcc01
import sam.model.entities.ab.Abm01
import java.time.format.DateTimeFormatter
import br.com.multitec.utils.Utils
import java.time.LocalDate
import br.com.multitec.utils.Utils
import sam.core.variaveis.MDate


public class SCE_EstoqueNaoMovimentadoDias extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Estoque Não Movimentado Em Dias";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        LocalDate data = MDate.date()
        filtrosDefault.put("dataSaldo", data)
        filtrosDefault.put("itensInventariaveis", true)
        filtrosDefault.put("itensNaoInventariaveis", true)
        filtrosDefault.put("impressao", "0")
        filtrosDefault.put("loteIni", "")
        filtrosDefault.put("loteFin", "")
        filtrosDefault.put("serieIni", "")
        filtrosDefault.put("serieFin", "")
        filtrosDefault.put("diasMin","0000")
        filtrosDefault.put("detalhamento","0")
        filtrosDefault.put("itemSaldoZero",true)


        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        String itemIni = getString("itemIni");
        String itemFin = getString("itemFim");
        List<Integer> mps = getListInteger("mpms");
        Boolean itemInventariavel = getBoolean("itensInventariaveis");
        Boolean itemNaoInventariavel = getBoolean("itensNaoInventariaveis");
        List<Integer> tipoMovimentacao = getListInteger("tipoMov");
        List<Long> PLE = getListInteger("PLE");
        List<Long> idsStatus = getListLong("status");
        List<Long> idsLocal = getListLong("local");
        String loteIni = getString("loteIni");
        String loteFin = getString("loteFin");
        String serieIni = getString("serieIni");
        String serieFin = getString("serieFin");
        Integer impressao = getInteger("impressao");
        Integer detalhamento = getInteger("detalhamento");
        String valorItem = getString("valorItem");
        LocalDate dataSaldo = getLocalDate("dataSaldo");
        Integer diasMin = getInteger("diasMin");
        Boolean naoImprimirSaldoZero = getBoolean("itemSaldoZero");

        List<TableMap> dadosRelatorio = buscarDadosRelatorio(itemIni,itemFin,mps,itemInventariavel,itemNaoInventariavel,tipoMovimentacao,PLE,idsStatus,idsLocal,
                loteIni,loteFin,serieIni,serieFin,valorItem,dataSaldo,diasMin,naoImprimirSaldoZero,detalhamento);

        String titulo = "";
        String nomeRelatorio = "";
        if(impressao == 0 && detalhamento == 0){
            titulo = "Estoque Não Movimentado Em dias (Sintético) ";
            nomeRelatorio = "SCE_EstoqueNaoMovimentadoDias_Sintetico_PDF"
        }else if(impressao == 0 && detalhamento == 1){
            titulo = "Estoque Não Movimentado Em dias (Analítico) ";
            nomeRelatorio = "SCE_EstoqueNaoMovimentadoDias_Analitico_PDF"
        }else if(impressao == 1 && detalhamento == 0){
            nomeRelatorio = "SCE_EstoqueNaoMovimentadoDias_Sintetico_Excel"
        }else{
            nomeRelatorio = "SCE_EstoqueNaoMovimentadoDias_Analitico_Excel"
        }

        params.put("titulo",titulo);
        params.put("empresa",obterEmpresaAtiva().aac10codigo +"-"+ obterEmpresaAtiva().aac10na);
        params.put("periodo","Período: " + dataSaldo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

        if(impressao == 1) return gerarXLSX(nomeRelatorio,dadosRelatorio);

        return gerarPDF(nomeRelatorio,dadosRelatorio)

    }

    private buscarDadosRelatorio(String itemIni,String itemFin,List<Integer> mps, Boolean itemInventariavel,Boolean itemNaoInventariavel,List<Integer>tipoMovimentacao,List<Long>PLE,List<Long>idsStatus,List<Long>idsLocal,
                                 String loteIni,String loteFin,String serieIni,String serieFin,String valorItem,LocalDate dataSaldo,Integer diasMin,Boolean naoImprimirSaldoZero,Integer detalhamento){
        List<TableMap> dadosRelatorio = new ArrayList();
        List<TableMap> tmItens = buscarItens(itemIni,itemFin,mps,itemInventariavel,itemNaoInventariavel);

        for(itens in tmItens){
            Long idItem = itens.getLong("abm01id");

            List<TableMap> lancamentosItem = new ArrayList();

            if(detalhamento == 0){
                lancamentosItem = buscarLancamentosSintetico(idItem,tipoMovimentacao,PLE,idsStatus,idsLocal,loteIni,loteFin,serieIni,serieFin,valorItem,dataSaldo,diasMin,naoImprimirSaldoZero);
            }else{
                lancamentosItem = buscarLancamentosAnalitico(idItem,tipoMovimentacao,PLE,idsStatus,idsLocal,loteIni,loteFin,serieIni,serieFin,valorItem,dataSaldo,diasMin,naoImprimirSaldoZero);
            }

            def vlrCampoLivre = null;
            String nomeCampoItem = buscarNomeCampo(valorItem);

            if(itens.getTableMap("abm0101json") != null) vlrCampoLivre = valorItem == 'descricao_livre' ? itens.getTableMap("abm0101json").getString("descricao_livre") : itens.getTableMap("abm0101json").getBigDecimal_Zero(valorItem)

            for(lancamento in lancamentosItem ){
                if(lancamento.getBigDecimal("quantidade") != null){

                    Long item = lancamento.getLong("bcc01item");
                    Long idStatus = lancamento.getLong("bcc01status");
                    Long idLocal = lancamento.getLong("bcc01ctrl0");
                    String lote = lancamento.getString("lote");
                    String serie = lancamento.getString("serie");



                    if(detalhamento == 1){
                        LocalDate maiorDataLcto = buscarMaiorDataLcto(item, idStatus, idLocal, lote, serie);
                        Integer difDias = ChronoUnit.DAYS.between(maiorDataLcto, LocalDate.now());

                        lancamento.put("bcc01data", maiorDataLcto);
                        lancamento.put("difDias", difDias);
                    }

                    lancamento.put("codItem",itens.getString("abm01codigo"));
                    lancamento.put("mps",itens.getString("MPS"));
                    lancamento.put("naItem",itens.getString("abm01na"));
                    lancamento.put("unidadeItem",itens.getString("aam06codigo"));
                    lancamento.put("campoLivreItem",nomeCampoItem);
                    lancamento.put("valorCampoLivreItem",vlrCampoLivre);

                    dadosRelatorio.add(lancamento)
                }

            }
        }

        return dadosRelatorio;
    }


    private buscarLancamentosSintetico(Long idItem,List<Integer>tipoMovimentacao,List<Long>PLE,List<Long>idsStatus,List<Long>idsLocal,
                                       String loteIni,String loteFin,String serieIni,String serieFin,String valorItem,LocalDate dataSaldo,Integer diasMin,Boolean naoImprimirSaldoZero){
        List<Long> listPLE = new ArrayList();

        if(PLE == null && PLE == []){
            listPLE = buscarPlePorMovimentacao(tipoMovimentacao);
        }

        String whereItem = "AND bcc01item = :idItem ";
        String wherePLE = listPLE != null && listPLE.size() > 0 ? "AND abm20id IN (:listPLE) " : "";
        String whereTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? " AND abm20rastrear IN (:tipoMovimentacao) " : "";
        String whereStatus = idsStatus != null && idsStatus.size() >= 0 ? "AND aam04id IN (:idsStatus) " : "";
        String whereLocal = idsLocal != null && idsLocal.size() >= 0 ? "AND abm15id IN (:idsLocal) " : "";
        String whereLoteIni = !loteIni.isEmpty() ? "AND bcc01lote >= :loteIni " : "";
        String whereLoteFin = !loteFin.isEmpty() ? "AND bcc01lote <= :loteFin " : "";
        String whereSerieIni = !serieIni.isEmpty() ? "AND bcc01serie >= :serieIni " : "";
        String whereSerieFin = !serieFin.isEmpty() ? "AND bcc01serie <= :serieFin " : "";
        String whereDataLcto =  "AND bcc01data <= :dataSaldo ";
        String whereDiasMin = "AND CURRENT_DATE - bcc01data >= :diasMin ";
        String whereSaldoZero = naoImprimirSaldoZero ? "HAVING SUM(bcc01qtPS) > 0 " : "";


        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroPLE = listPLE != null && listPLE.size() > 0 ? Parametro.criar("listPLE",listPLE) : null;
        Parametro parametroTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? Parametro.criar("tipoMovimentacao",tipoMovimentacao) : null;
        Parametro parametroStatus = idsStatus != null && idsStatus.size() >= 0 ? Parametro.criar("idsStatus",idsStatus) : null;
        Parametro parametroLocal = idsLocal != null && idsLocal.size() >= 0 ? Parametro.criar("idsLocal",idsLocal) : null;
        Parametro parametroLoteIni = !loteIni.isEmpty() ? criarParametroSql("loteIni", loteIni) : null;
        Parametro parametroLoteFin = !loteFin.isEmpty() ? criarParametroSql("loteFin", loteFin) : null;
        Parametro parametroSerieIni = !serieIni.isEmpty() ? criarParametroSql("serieIni", serieIni) : null;
        Parametro parametroSerieFin = !serieFin.isEmpty() ? criarParametroSql("serieFin", serieFin) : null;
        Parametro parametroDataLcto = Parametro.criar("dataSaldo",dataSaldo)
        Parametro parametroDiasMin = Parametro.criar("diasMin",diasMin);


        String sql = "SELECT SUM(bcc01qtPS) AS quantidade,SUM(bcc01custo) AS custo,MAX(bcc01data) AS bcc01data, CURRENT_DATE - MAX(bcc01data) AS difDias "+
                "FROM bcc01 "+
                "INNER JOIN abm20 on abm20id = bcc01ple "+
                "INNER JOIN aam04 on aam04id = bcc01status "+
                "INNER JOIN abm15 on abm15id = bcc01ctrl0 "+
                getSamWhere().getWherePadrao("WHERE ", Bcc01.class) +
                wherePLE+
                whereTipoMov+
                whereStatus+
                whereLocal+
                whereLoteIni+
                whereLoteFin+
                whereSerieIni+
                whereSerieFin+
                whereDataLcto+
                whereDiasMin+
                whereItem +
                whereSaldoZero;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroPLE,parametroTipoMov,parametroStatus,parametroLocal,parametroLoteIni,
                parametroLoteFin,parametroSerieIni,parametroSerieFin,parametroDataLcto,parametroDiasMin,parametroItem)



    }

    private buscarLancamentosAnalitico(Long idItem,List<Integer>tipoMovimentacao,List<Long>PLE,List<Long>idsStatus,List<Long>idsLocal,
                                       String loteIni,String loteFin,String serieIni,String serieFin,String valorItem,LocalDate dataSaldo,Integer diasMin,Boolean naoImprimirSaldoZero){

        List<Long> listPLE = new ArrayList();

        if(PLE == null && PLE == []){
            listPLE = buscarPlePorMovimentacao(tipoMovimentacao);
        }

        String whereItem = "AND bcc01item = :idItem ";
        String wherePLE = listPLE != null && listPLE.size() > 0 ? "AND abm20id IN (:listPLE) " : "";
        String whereTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? " AND abm20rastrear IN (:tipoMovimentacao) " : "";
        String whereStatus = idsStatus != null && idsStatus.size() >= 0 ? "AND aam04id IN (:idsStatus) " : "";
        String whereLocal = idsLocal != null && idsLocal.size() >= 0 ? "AND aam04id IN (:idsLocal) " : "";
        String whereLoteIni = !loteIni.isEmpty() ? "AND bcc01lote >= :loteIni " : "";
        String whereLoteFin = !loteFin.isEmpty() ? "AND bcc01lote <= :loteFin " : "";
        String whereSerieIni = !serieIni.isEmpty() ? "AND bcc01serie >= :serieIni " : "";
        String whereSerieFin = !serieFin.isEmpty() ? "AND bcc01serie <= :serieFin " : "";
        String whereDataLcto =  "AND bcc01data <= :dataSaldo ";
        String whereDiasMin = "AND CURRENT_DATE - bcc01data >= :diasMin ";
        String whereSaldoZero = naoImprimirSaldoZero ? "HAVING SUM(bcc01qtPS) > 0 " : "";



        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroPLE = listPLE != null && listPLE.size() > 0 ? Parametro.criar("listPLE",listPLE) : null;
        Parametro parametroTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? Parametro.criar("tipoMovimentacao",tipoMovimentacao) : null;
        Parametro parametroStatus = idsStatus != null && idsStatus.size() >= 0 ? Parametro.criar("idsStatus",idsStatus) : null;
        Parametro parametroLocal = idsLocal != null && idsLocal.size() >= 0 ? Parametro.criar("idsLocal",idsLocal) : null;
        Parametro parametroLoteIni = !loteIni.isEmpty() ? criarParametroSql("loteIni", loteIni) : null;
        Parametro parametroLoteFin = !loteFin.isEmpty() ? criarParametroSql("loteFin", loteFin) : null;
        Parametro parametroSerieIni = !serieIni.isEmpty() ? criarParametroSql("serieIni", serieIni) : null;
        Parametro parametroSerieFin = !serieFin.isEmpty() ? criarParametroSql("serieFin", serieFin) : null;
        Parametro parametroDataLcto = Parametro.criar("dataSaldo",dataSaldo)
        Parametro parametroDiasMin = Parametro.criar("diasMin",diasMin);


        String sql = "SELECT SUM(bcc01qtPS) AS quantidade,SUM(bcc01custo) AS custo, " +
                "bcc01status, bcc01ctrl0, aam04codigo AS status,abm15nome AS local,bcc01lote AS lote, bcc01serie AS serie, " +
                "doc3.abb01num AS numDoc3, ent3.abe01codigo AS codEnt3, ent3.abe01na AS naEnt3, bcc01item " +
                "FROM bcc01 " +
                "INNER JOIN abm20 ON abm20id = bcc01ple " +
                "INNER JOIN aam04 ON aam04id = bcc01status " +
                "INNER JOIN abm15 ON abm15id = bcc01ctrl0 " +
                "LEFT JOIN abb01 AS doc3 ON doc3.abb01id = bcc01centralEst " +
                "LEFT JOIN abe01 as ent3 ON ent3.abe01id = doc3.abb01ent " +
                getSamWhere().getWherePadrao("WHERE ", Bcc01.class) +
                wherePLE+
                whereTipoMov+
                whereStatus+
                whereLocal+
                whereLoteIni+
                whereLoteFin+
                whereSerieIni+
                whereSerieFin+
                whereDataLcto+
                whereDiasMin+
                whereItem +
                "GROUP BY aam04codigo,abm15nome,bcc01lote, bcc01serie,doc3.abb01num, ent3.abe01codigo, ent3.abe01na,bcc01status, bcc01ctrl0, bcc01item "+
                whereSaldoZero;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroPLE,parametroTipoMov,parametroStatus,parametroLocal,parametroLoteIni,
                parametroLoteFin,parametroSerieIni,parametroSerieFin,parametroDataLcto,parametroDiasMin,parametroItem);
    }

    private List<TableMap> buscarItens(String itemIni,String itemFin,List<Integer> mps,Boolean itemInventariavel,Boolean itemNaoInventariavel){

        String whereInventario = ""

        if(!itemInventariavel && itemNaoInventariavel ){
            whereInventario = "AND abm11giprop IS NULL "
        }else if(itemInventariavel && !itemNaoInventariavel){
            whereInventario = "AND abm11giprop IS NOT NULL "
        }else{
            whereInventario = ""
        }

        String whereItem = itemIni != null && itemFin != null ? " AND abm01codigo BETWEEN :itemIni AND :itemFin  " : itemIni != null && itemFin == null ? " AND abm01codigo >= :itemIni " : itemIni == null && itemFin != null ? " AND abm01codigo <= :itemFin " : "";
        String whereMPS = !mps.contains(-1) ? " AND abm01tipo IN (:mps) " : "";
        String whereEmpresa = " AND abm0101empresa = :empresa ";

        Parametro parametroItemIni = itemIni != null ? Parametro.criar("itemIni",itemIni) : null
        Parametro parametroItemFin = itemFin != null ? Parametro.criar("itemFin",itemFin) : null
        Parametro parametroMPS = !mps.contains(-1) ? Parametro.criar("mps",mps) : null;
        Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().aac10id)

        String sql = "SELECT CASE WHEN abm01tipo = 0 THEN 'M' ELSE 'P' END AS MPS, abm01codigo, abm01na,abm01id,aam06codigo,abm0101json "+
                "FROM abm01 "+
                "INNER JOIN aam06 ON aam06id = abm01umu "+
                "INNER JOIN abm0101 ON abm0101item = abm01id "+
                "INNER JOIN abm11 ON abm11id = abm0101estoque " +
                getSamWhere().getWherePadrao("WHERE", Abm01.class) +
                " AND abm01grupo = " + Abm01.NAO +
                whereItem +
                whereMPS+
                whereInventario+
                whereEmpresa +
                " ORDER BY abm01tipo, abm01codigo";

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItemIni,parametroItemFin,parametroMPS,parametroEmpresa)


    }

    private BigDecimal buscarSaldoAtual(Long idItem,List<Long> idsStatus){
        String whereStatus = idsStatus != null && idsStatus.size() > 0 ? " AND bcc02status IN (:idsStatus) " : "";

        String sql = " SELECT COALESCE(SUM(bcc02qt), 0) FROM Bcc02 " +
                " WHERE bcc02item = :idItem " +
                whereStatus + obterWherePadrao("Bcc02");

        Parametro p1 = Parametro.criar("idItem",idItem);
        Parametro p2 = idsStatus != null && idsStatus.size() > 0 ? Parametro.criar("idsStatus",idsStatus) : null;

        return getAcessoAoBanco().obterBigDecimal(sql,p1,p2);
    }

    private List<Long> buscarPlePorMovimentacao(List<Integer>tipoMovimentacao){

        String whereRastrear = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? "where abm20rastrear in (:tipoMovimentacao) " : "";

        String sql = "select abm20id from abm20 " + whereRastrear;

        Parametro p1 = Parametro.criar("tipoMovimentacao",tipoMovimentacao);

        return getAcessoAoBanco().obterListLong(sql,p1)

    }
    private LocalDate buscarMaiorDataLcto(Long idItem, Long status, Long local, String lote, String serie){

        String whereItem = " WHERE bcc01item = :idItem ";
        String whereStatus = " AND bcc01status = :status ";
        String whereLocal =  " AND bcc01ctrl0 = :local ";
        String whereLote = lote != null ? " AND bcc01lote = :lote " : "";
        String whereSerie = serie != null ? " AND bcc01serie = :serie " : "";

        Parametro parametroItem = Parametro.criar("idItem", idItem);
        Parametro parametroStatus = Parametro.criar("status", status);
        Parametro parametroLocal = Parametro.criar("local", local);
        Parametro parametroLote = lote != null ? Parametro.criar("lote", lote) : null;
        Parametro parametroSerie = serie != null ? Parametro.criar("serie", serie) : null;

        String sql = "SELECT MAX(bcc01data) AS bcc01data  " +
                " FROM bcc01 " +
                whereItem +
                whereStatus +
                whereLocal +
                whereLote +
                whereSerie;

        return getAcessoAoBanco().obterDate(sql, parametroItem, parametroStatus, parametroLocal, parametroLote, parametroSerie);

    }

    private String buscarNomeCampo(String valorItem){
        String nomeCampo;

        switch(valorItem){
            case 'abm0101pmu':
                nomeCampo = 'Preço Médio Unitário';
                break;
            case 'custo':
                nomeCampo = 'Custo Unitário';
                break;
            case 'maior_preco_unit':
                nomeCampo = 'Maior Preço Unitário';
                break;
            case 'menor_preco_unit':
                nomeCampo = 'Menor Preço Unitário';
                break;
            case 'ultimo_preco':
                nomeCampo = 'Menor Preço Unitário';
                break;
            case 'descricao_livre':
                nomeCampo = 'Código Livre';
                break;
        }

        return nomeCampo;
    }
}