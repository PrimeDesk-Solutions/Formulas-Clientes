package PrimeDesk.relatorios.sce;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.collections.TableMap;

import java.time.LocalDate
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.Parametros
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcc02
import sam.model.entities.bc.Bcc0201
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro
import java.time.format.DateTimeFormatter
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class SCE_AnaliseDaMovimentacao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE Análise da Movimentação ";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        LocalDate data = MDate.date()
        LocalDate dataIni = LocalDate.of(data.year,data.getMonthValue() - 1,1)
        filtrosDefault.put("dataSaldo", data)
        filtrosDefault.put("itensInventariaveis", true)
        filtrosDefault.put("itensNaoInventariaveis", true)
        filtrosDefault.put("impressao", "0")
        filtrosDefault.put("loteIni", "")
        filtrosDefault.put("loteFin", "")
        filtrosDefault.put("serieIni", "")
        filtrosDefault.put("serieFin", "")
        filtrosDefault.put("dtIni", dataIni.format(DateTimeFormatter.ofPattern("MM/yyyy")))
        filtrosDefault.put("dtFin", data.format(DateTimeFormatter.ofPattern("MM/yyyy")))


        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Integer> mps = getListInteger("mpm");
        String itemIni = getString("itemIni");
        String itemFin = getString("itemFim");
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
        String dtMovimentacaoIni = getString("dtIni");
        String dtMovimentacaoFin = getString("dtFin");
        LocalDate dataSaldo = getLocalDate("dataSaldo");
        List<Long> critSelec = getListLong("critSelec");
        boolean classA = getBoolean("a");
        boolean classB = getBoolean("b");
        boolean classC = getBoolean("c");
        List<String> classificacoes = new ArrayList<>();
        if(classA) classificacoes.add('A');
        if(classB) classificacoes.add('B');
        if(classC) classificacoes.add('C');



        List<TableMap> dados = buscarDadosRelatorio(mps,itemIni,itemFin,itemInventariavel,itemNaoInventariavel,tipoMovimentacao,PLE,idsStatus,idsLocal,loteIni,loteFin,serieIni,serieFin,dtMovimentacaoIni,dtMovimentacaoFin,dataSaldo, classificacoes, critSelec);

        params.put("titulo","SCE - Analise de Movimentação");
        params.put("empresa",obterEmpresaAtiva().aac10codigo +"-"+ obterEmpresaAtiva().aac10na);
        params.put("periodo","Movimentação: " +dtMovimentacaoIni +" à "+dtMovimentacaoFin);
        if(impressao == 1) return gerarXLSX("SCE_AnaliseDaMovimentacao_Excel",dados);
        return gerarPDF("SCE_AnaliseDaMovimentacao_PDF",dados);
    }

    private List<TableMap>  buscarDadosRelatorio(List<Integer>mps,String itemIni,String itemFin,Boolean itemInventariavel, Boolean itemNaoInventariavel,List<Integer>tipoMovimentacao,List<Long>PLE,List<Long>idsStatus,List<Long>idsLocal,String loteIni,String loteFin,String serieIni,String serieFin,String dtMovimentacaoIni,String dtMovimentacaoFin,LocalDate dataSaldo, List<String> classificacao, List<Long> criteSele){
        LocalDate dtAtual = LocalDate.now();
        List<Abm01> abm01s = buscarItens(mps,itemIni,itemFin,itemInventariavel,itemNaoInventariavel, classificacao, criteSele);
        List<Long> idsItens = new ArrayList<>();

        for(abm01 in abm01s){
            Long idItem = abm01.abm01id;
            idsItens.add(idItem);
        }

        if(abm01s != null && abm01s.size() > 0){
            List<String> datas = buscarListaDeDatas(dtMovimentacaoIni,dtMovimentacaoFin);
            List<TableMap> saldosEstoque = dataSaldo.compareTo(dtAtual) >= 0 ? buscarSaldoAtualItem(idsItens, idsStatus,idsLocal, loteIni, loteFin, serieIni, serieFin) : buscarSaldoRetroativosItens(idsItens, dataSaldo, idsStatus,idsLocal, loteIni, loteFin, serieIni, serieFin);

            List<TableMap> registros = new ArrayList();

            for(abm01 in abm01s){
                Abm0101 abm0101 =  obterCamposLivresItem(abm01.abm01id);
                TableMap tmSaldo = saldosEstoque.stream().filter({tm -> tm.getLong("id").equals(abm01.abm01id)}).findFirst().orElse(new TableMap());
                BigDecimal saldoEstoque = tmSaldo.getBigDecimal_Zero("saldo");
                if(abm0101.abm0101json != null){
                    String tipoItem = abm01.abm01tipo == 0 ? 'M' : 'P';
                    def total = 0;
                    def media = 0;
                    if(datas.size() > 0){
                        for(periodo in datas){
                            TableMap lancamento = new TableMap();

                            BigDecimal totalEntrada = buscarLancamentoItem(abm01.abm01id,tipoMovimentacao,PLE,idsStatus,idsLocal, loteIni, loteFin, serieIni, serieFin,periodo,0)
                            BigDecimal totalSaida = buscarLancamentoItem(abm01.abm01id,tipoMovimentacao,PLE,idsStatus,idsLocal, loteIni, loteFin, serieIni, serieFin,periodo,1)

                            BigDecimal totalGeralItem = totalEntrada - totalSaida;

                            total += totalGeralItem;
                            media = total / datas.size();

                            lancamento.put("valor",totalGeralItem);
                            lancamento.put("item",tipoItem +" "+abm01.abm01codigo +" "+abm01.abm01na);
                            lancamento.put("codItem",abm01.abm01codigo);
                            lancamento.put("naItem",abm01.abm01na);
                            lancamento.put("tipoItem",tipoItem);
                            lancamento.put("cabecalho",periodo);
                            lancamento.put("estMax",abm0101.abm0101estMax);
                            lancamento.put("estMin",abm0101.abm0101estMin);
                            lancamento.put("estSeg",abm0101.abm0101estSeg);
                            lancamento.put("leadTime",abm0101.abm0101json.getBigDecimal_Zero("lead_time"));
                            lancamento.put("pontoPedido",abm0101.abm0101ptoPed);
                            lancamento.put("custo",abm0101.abm0101json.getBigDecimal_Zero("custo"));
                            lancamento.put("maiorPreco",abm0101.abm0101json.getBigDecimal_Zero("maior_preco_unit"));
                            lancamento.put("menorPreco",abm0101.abm0101json.getBigDecimal_Zero("menor_preco_unit"));
                            lancamento.put("ultimoPreco",abm0101.abm0101json.getBigDecimal_Zero("ultimo_preco"));
                            lancamento.put("total",total);
                            lancamento.put("media",media);
                            lancamento.put("saldoEstoque",saldoEstoque != null ? saldoEstoque : new BigDecimal(0));

                            registros.add(lancamento);
                        }
                    }
                }
            }


            return registros
        }
    }

    private List<Abm01>buscarItens(List<Integer>tipos,String itemIni,String itemFin,Boolean itemInventariavel,Boolean itemNaoInventariavel, List<String> classificacao, List<Long> critSele){
        Criterion critTipo = tipos != null && !tipos.contains(-1) ? Criterions.in("abm01tipo", tipos) : Criterions.isTrue();
        Criterion critItem = itemIni != null && itemFin != null ? Criterions.between("abm01codigo", itemIni, itemFin) : itemIni != null && itemFin == null ? Criterions.ge("abm01codigo", itemIni) : itemIni == null && itemFin != null ? Criterions.le("abm01codigo", itemIni) : Criterions.isTrue();
        Criterion critCrit = critSele != null && critSele.size() > 0 ? Criterions.in("abm0102criterio", critSele) : Criterions.isTrue();
        Criterion invent = Criterions.or(Criterions.isNotNull("abm11giProp"), Criterions.isNotNull("abm11giComTerc"), Criterions.isNotNull("abm11giDeTerc"));
        Criterion noInve = Criterions.and(Criterions.isNull("abm11giProp"), Criterions.isNull("abm11giComTerc"), Criterions.isNull("abm11giDeTerc"));
        Criterion critInve = itemInventariavel && !itemNaoInventariavel ? invent : !itemInventariavel && itemNaoInventariavel ? noInve : Criterions.isTrue();
        Criterion critClassificacao = classificacao != null && classificacao.size() > 0 ? Criterions.and(Criterions.in("abm0101abc", classificacao)) : Criterions.isTrue();

        return getSession().createCriteria(Abm01.class).addFields("DISTINCT abm01id, abm01tipo, abm01codigo, abm01na, abm01descr")
                .addJoin(Joins.join("abm01umu").left(true))
                .addJoin(Joins.join("Abm0101", "abm0101item = abm01id"))
                .addJoin(Joins.join("Abm11", "abm11id = abm0101estoque").left(true))
                .addJoin(Joins.join("Abm0102", "abm0102item = abm01id").left(true))
                .addWhere(Criterions.eq("abm01grupo", Abm01.NAO))
                .addWhere(critTipo).addWhere(critItem)
                .addWhere(Criterions.eq("abm0101empresa", obterEmpresaAtiva().aac10id))
                .addWhere(critClassificacao).addWhere(critCrit)
                .addWhere(critInve).setOrder("abm01tipo, abm01codigo").getList(ColumnType.ENTITY);

    }

    private BigDecimal buscarLancamentoItem(Long abm01id,List<Integer>tipoMovimentacao,List<Long>PLE,List<Long>idsStatus,List<Long>idsLocal,String loteIni,String loteFin,String serieIni,String serieFin,String periodo, Integer movimentacao){
        List<Long> listPLE = new ArrayList();

        String whereItem = "AND bcc01item = :idItem ";
        String wherePLE = listPLE != null && listPLE.size() > 0 ? "AND abm20id IN (:listPLE) " : "";
        String whereTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? " AND abm20rastrear IN (:tipoMovimentacao) " : "";
        String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND aam04id IN (:idsStatus) " : "";
        String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "AND abm15id IN (:idsLocal) " : "";
        String whereLoteIni = !loteIni.isEmpty() ? "AND bcc01lote >= :loteIni " : "";
        String whereLoteFin = !loteFin.isEmpty() ? "AND bcc01lote <= :loteFin " : "";
        String whereSerieIni = !serieIni.isEmpty() ? "AND bcc01serie >= :serieIni " : "";
        String whereSerieFin = !serieFin.isEmpty() ? "AND bcc01serie <= :serieFin " : "";
        String wherePeriodo = "AND TO_CHAR(bcc01data, 'MM/YYYY') = :periodo "
        String whereMovimentacao = "AND bcc01mov = :movimentacao "


        Parametro parametroItem = Parametro.criar("idItem",abm01id);
        Parametro parametroPLE = listPLE != null && listPLE.size() > 0 ? Parametro.criar("listPLE",listPLE) : null;
        Parametro parametroTipoMov = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? Parametro.criar("tipoMovimentacao",tipoMovimentacao) : null;
        Parametro parametroStatus = idsStatus != null && idsStatus.size() >= 0 ? Parametro.criar("idsStatus",idsStatus) : null;
        Parametro parametroLocal = idsLocal != null && idsLocal.size() >= 0 ? Parametro.criar("idsLocal",idsLocal) : null;
        Parametro parametroLoteIni = !loteIni.isEmpty() ? criarParametroSql("loteIni", loteIni) : null;
        Parametro parametroLoteFin = !loteFin.isEmpty() ? criarParametroSql("loteFin", loteFin) : null;
        Parametro parametroSerieIni = !serieIni.isEmpty() ? criarParametroSql("serieIni", serieIni) : null;
        Parametro parametroSerieFin = !serieFin.isEmpty() ? criarParametroSql("serieFin", serieFin) : null;
        Parametro parametroPeriodo = Parametro.criar("periodo",periodo);
        Parametro parametroMovimentacao = Parametro.criar("movimentacao",movimentacao);


        String sql = "SELECT COALESCE(SUM(bcc01qt),0) AS valor "+
                "FROM bcc01 "+
                "INNER JOIN abm01 ON abm01id = bcc01item "+
                "INNER JOIN abm0101 ON abm0101item = abm01id "+
                "INNER JOIN abm20 ON abm20id = bcc01ple "+
                "INNER JOIN aam04 ON aam04id = bcc01status "+
                "INNER JOIN abm15 ON abm15id = bcc01ctrl0 "+
                wherePLE+
                whereTipoMov+
                whereStatus+
                whereLocal+
                whereLoteIni+
                whereLoteFin+
                whereSerieIni+
                whereSerieFin+
                whereItem+
                wherePeriodo +
                whereMovimentacao

        return getAcessoAoBanco().obterBigDecimal(sql,parametroPLE,parametroTipoMov,parametroStatus,parametroLocal,parametroLoteIni,
                parametroLoteFin,parametroSerieIni,parametroSerieFin,parametroItem,parametroPeriodo,parametroMovimentacao)

    }

    private List<String> buscarListaDeDatas(String dtMovimentacaoIni, String dtMovimentacaoFin){

        String dataInicialString = dtMovimentacaoIni;
        String dataFinalString = dtMovimentacaoFin;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        YearMonth mesInicial = YearMonth.parse(dataInicialString, formatter);
        YearMonth mesFinal = YearMonth.parse(dataFinalString, formatter);

        LocalDate dataInicial = mesInicial.atDay(1);
        LocalDate dataFinal = mesFinal.atEndOfMonth();

        String sql = "SELECT DISTINCT TO_CHAR(bcc01data, 'MM/YYYY') AS datas  "+
                "FROM bcc01  "+
                "WHERE bcc01data BETWEEN :dtMovimentacaoIni AND :dtMovimentacaoFin " +
                "AND bcc01mov IN (0,1) " +
                "ORDER BY TO_CHAR(bcc01data, 'MM/YYYY')"

        Parametro p1 = Parametro.criar("dtMovimentacaoIni",dataInicial);
        Parametro p2 = Parametro.criar("dtMovimentacaoFin",dataFinal);

        return getAcessoAoBanco().obterListaDeString(sql,p1,p2);
    }

    private List<Long> buscarPlePorMovimentacao(List<Integer>tipoMovimentacao){

        String whereRastrear = tipoMovimentacao != null && tipoMovimentacao.size() > 0 ? "WHERE abm20rastrear IN (:tipoMovimentacao) " : "";

        String sql = "SELECT abm20id FROM abm20 " + whereRastrear;

        Parametro p1 = Parametro.criar("tipoMovimentacao",tipoMovimentacao);

        return getAcessoAoBanco().obterListaDeLong(sql,p1)

    }

    private Abm0101 obterCamposLivresItem(Long abm01id){
        return getSession().createCriteria(Abm0101.class)
                .addWhere(Criterions.eq("abm0101item", abm01id))
                .get(ColumnType.ENTITY);
    }

    private List<TableMap> buscarSaldoAtualItem(List<Long> idsItens,  List<Long> idsStatus, List<Long> idsLocal, String loteIni, String loteFin, String serieIni, String serieFin){
        String whereItens = "WHERE bcc02item IN (:idsItens)";
        String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "AND (bcc02crtl0 IN (:idsLocal) OR bcc02crtl1 IN (:idsLocal) OR bcc02crtl2 IN (:idsLocal)) " : "";
        String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND bcc02status IN (:idsStatus) " : "";
        String whereLote = loteIni != null && loteFin != null && !loteIni.isEmpty() && !loteFin.isEmpty() ? "AND bcc0201lote BETWEEN :loteIni AND :loteFin " :
                loteIni != null && loteFin == null && !loteIni.isEmpty() && loteFin.isEmpty() ? "AND bcc0201lote >= :loteIni " :
                        loteIni == null && loteFin != null && loteIni .isEmpty() && !loteFin.isEmpty() ? "AND bcc0201lote <= :loteFin " : "";

        String whereSerie = serieIni != null && serieFin != null && !serieIni.isEmpty() && !serieFin.isEmpty() ? "AND bcc0201serie BETWEEN :serieIni AND :serieFin " :
                serieIni != null && serieFin == null && !serieIni.isEmpty() && serieFin.isEmpty() ? "AND bcc0201serie >= :serieIni " :
                        serieIni == null && serieFin != null && serieIni.isEmpty() && !serieFin.isEmpty() ? "AND bcc0201serie <= :serieFin " : "";


        String sql = "SELECT bcc02item AS id, SUM(bcc0201qt) AS saldo " +
                "FROM bcc02 " +
                "INNER JOIN bcc0201 ON bcc0201saldo = bcc02id " +
                whereItens +
                whereLocal +
                whereStatus +
                whereLote +
                whereSerie +
                "GROUP BY bcc02item "+
                "ORDER BY bcc02item ";

        Parametro parametroItens = Parametro.criar("idsItens", idsItens);
        Parametro parametroLocal = idsLocal != null && idsLocal.size() ? Parametro.criar("idsLocal", idsLocal) : null;
        Parametro parametroStatus = idsStatus != null && idsStatus.size() ? Parametro.criar("idsStatus", idsStatus) : null;
        Parametro parametroLoteIni = loteIni != null ? Parametro.criar("loteIni", loteIni) : null;
        Parametro parametroLoteFin = loteFin != null ? Parametro.criar("loteFin", loteFin) : null;
        Parametro parametroSerieIni = serieIni != null ? Parametro.criar("serieIni", serieIni) : null;
        Parametro parametroSerieFin = serieFin != null ? Parametro.criar("serieFin", serieFin) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens, parametroLocal, parametroStatus, parametroLoteIni, parametroLoteFin, parametroSerieIni, parametroSerieFin)
    }
    private List<TableMap> buscarSaldoRetroativosItens(List<Long> idsItens, LocalDate dataSaldo, List<Long> idsStatus, List<Long> idsLocal, String loteIni, String loteFin, String serieIni, String serieFin){
        String whereItens = "WHERE bcc01item IN (:idsItens)";
        String whereDataSaldo = "AND bcc01data <= :dataSaldo "
        String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "AND (bcc02crtl0 IN (:idsLocal) OR bcc02crtl1 IN (:idsLocal) OR bcc02crtl2 IN (:idsLocal)) " : "";
        String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND bcc02status IN (:idsStatus) " : "";
        String whereLote = loteIni != null && loteFin != null ? "AND bcc0201lote BETWEEN :loteIni AND :loteFin " :
                loteIni != null && loteFin == null ? "AND bcc0201lote >= :loteIni " :
                        loteIni == null && loteFin != null ? "AND bcc0201lote <= :loteFin " : "";

        String whereSerie = serieIni != null && serieFin != null ? "AND bcc0201serie BETWEEN :serieIni AND :serieFin " :
                serieIni != null && serieFin == null ? "AND bcc0201serie >= :serieIni " :
                        serieIni == null && serieFin != null ? "AND bcc0201serie <= :serieFin " : "";

        String sql = "SELECT bcc01item AS id, SUM(bcc01qtPS) AS saldo "+
                "FROM bcc01 "+
                whereItens +
                whereDataSaldo +
                whereLocal +
                whereStatus +
                whereLote +
                whereSerie +
                "GROUP BY bcc01item " +
                "ORDER BY bcc01item";

        Parametro parametroItens = Parametro.criar("idsItens", idsItens);
        Parametro parametroDataSaldo = Parametro.criar("dataSaldo", dataSaldo);
        Parametro parametroLocal = idsLocal != null && idsLocal.size() ? Parametro.criar("idsLocal", idsLocal) : null;
        Parametro parametroStatus = idsStatus != null && idsStatus.size() ? Parametro.criar("idsStatus", idsStatus) : null;
        Parametro parametroLoteIni = loteIni != null ? Parametro.criar("loteIni", loteIni) : null;
        Parametro parametroLoteFin = loteFin != null ? Parametro.criar("loteFin", loteFin) : null;
        Parametro parametroSerieIni = serieIni != null ? Parametro.criar("serieIni", serieIni) : null;
        Parametro parametroSerieFin = serieFin != null ? Parametro.criar("serieFin", serieFin) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens, parametroDataSaldo, parametroLocal, parametroStatus, parametroLoteIni, parametroLoteFin, parametroSerieIni, parametroSerieFin)

    }
}
//meta-sis-eyJkZXNjciI6IlNDRSBBbsOhbGlzZSBkYSBNb3ZpbWVudGHDp8OjbyAiLCJ0aXBvIjoicmVsYXRvcmlvIn0=