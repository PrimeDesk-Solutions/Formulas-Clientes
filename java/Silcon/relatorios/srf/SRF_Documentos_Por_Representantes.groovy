package Silcon.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;
import java.time.LocalDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.ea.Eaa01
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.relatorio.TableMapDataSource


public class SRF_Documentos_Por_Representantes extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "SRF - Documentos Por Representantes";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
        filtrosDefault.put("devolucao", true);
        filtrosDefault.put("chkDevolucao", true);
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("tipoOperacao", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("resumo", "0");
        filtrosDefault.put("resumoOperacao", "0")
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer resumoOperacao = getInteger("resumoOperacao");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsReps = getListLong("reps");
        boolean totalizar1 = getBoolean("total1");
        boolean totalizar2 = getBoolean("total2");
        boolean totalizar3 = getBoolean("total3");
        boolean totalizar4 = getBoolean("total4");
        boolean totalizar5 = getBoolean("total5");
        boolean totalizar6 = getBoolean("total6");
        String campoFixo1 = getString("campoFixo1");
        String campoFixo2 = getString("campoFixo2");
        String campoFixo3 = getString("campoFixo3");
        String campoFixo4 = getString("campoFixo4");
        String campoFixo5 = getString("campoFixo5");
        String campoFixo6 = getString("campoFixo6");
        String campoLivre1 = getString("campoLivre1");
        String campoLivre2 = getString("campoLivre2");
        String campoLivre3 = getString("campoLivre3");
        String campoLivre4 = getString("campoLivre4");
        String campoLivre5 = getString("campoLivre5");
        String campoLivre6 = getString("campoLivre6");
        Integer impressao = getInteger("impressao");
        Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;
        boolean devolucoes = getBoolean("chkDevolucao");

        String periodo = ""
        if (dtEmissao != null) {
            periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        } else if (dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("totalizar1", totalizar1);
        params.put("totalizar2", totalizar2);
        params.put("totalizar3", totalizar3);
        params.put("totalizar4", totalizar4);
        params.put("totalizar5", totalizar5);
        params.put("totalizar6", totalizar6);
        params.put("title", "SRF - Documento por Representantes");
        params.put("empresa", empresa.aac10codigo + "-" + empresa.aac10na);
        params.put("periodo", periodo);
        params.put("LOGO_EMPRESA", "C:\\SAM-Servidor\\samdev\\resources\\Silcon\\relatorios\\srf\\Logo Silcon.png");
        params.put("LOGO_REVENDA", "C:\\SAM-Servidor\\samdev\\resources\\Silcon\\relatorios\\srf\\logoPrimeDesk.png");



        if (campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!");
        if (campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!");
        if (campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!");
        if (campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!");
        if (campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!");
        if (campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!");

        Map<String, String> campos = new HashMap();

        campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null);
        campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null);
        campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null);
        campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null);
        campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null);
        campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null);

        List<TableMap> representantes = buscarRepresentantes(idsReps);
        List<TableMap> documentos = new ArrayList<>();
        List<TableMap> dadosRelatorio = new ArrayList();
        List<TableMap> dados = buscarDadosDocumentos(idsReps, numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida);
        if (dados == null || dados.size() == 0) interromper("Não foram encontrados registros com os filtros informados.");

        List<Long> idsItensDoc = obterIdsItensDoc(idsReps, numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida);

        List<TableMap> listDevolucoesGeral = new ArrayList();
        def idControleDevolucao = null;
        List<TableMap> listDevolucoesAjustado = new ArrayList<>()
        TableMap dadosTmpDev = new TableMap()
        TableMap valoresTotaisDevolucao = new TableMap();

        //Agrupa as devoluções
        if (devolucoes) {
            listDevolucoesGeral = obterDevolucao(idsItensDoc);
            if (listDevolucoesGeral != null && listDevolucoesGeral.size() > 0) {
                for (devolucao in listDevolucoesGeral) {
                    devolucao.putAll(devolucao.getTableMap("eaa0103json"));
                    devolucao.remove("eaa0103json");
                    if (idControleDevolucao == null) {
                        dadosTmpDev.putAll(devolucao);
                        idControleDevolucao = devolucao.getLong("eaa01033itemdoc");
                        somarValores(devolucao, valoresTotaisDevolucao, campos);
                    } else if (idControleDevolucao == devolucao.getLong("eaa01033itemdoc")) {
                        somarValores(devolucao, valoresTotaisDevolucao, campos);
                    } else {
                        TableMap tmpDev = new TableMap();
                        tmpDev.putAll(dadosTmpDev);
                        tmpDev.putAll(valoresTotaisDevolucao);
                        listDevolucoesAjustado.add(tmpDev);

                        dadosTmpDev = new TableMap()
                        dadosTmpDev.putAll(devolucao);
                        valoresTotaisDevolucao = new TableMap();
                        idControleDevolucao = devolucao.getLong("eaa01033itemdoc");

                        somarValores(devolucao, valoresTotaisDevolucao, campos)
                    }
                }

                TableMap tmpDev = new TableMap();
                tmpDev.putAll(dadosTmpDev);
                tmpDev.putAll(valoresTotaisDevolucao);
                if (!tmpDev.isEmpty()) listDevolucoesAjustado.add(tmpDev);
            }
        }

        for (representante in representantes) {
            Long idRep = representante.getLong("abe01id");
            String codRep = representante.getString("abe01codigo");
            String nomeRep = representante.getString("abe01na");
            def txComissao = representante.getBigDecimal_Zero("abe05taxa");
            def idControle = null;
            TableMap dadosTmp = new TableMap();
            TableMap valoresTotais = new TableMap();

            for (dado in dados) {
                Long idRep0 = dado.getLong("eaa01rep0");
                Long idRep1 = dado.getLong("eaa01rep1");
                Long idRep2 = dado.getLong("eaa01rep2");
                Long idRep3 = dado.getLong("eaa01rep3");
                Long idRep4 = dado.getLong("eaa01rep4");
                if(idRep != idRep0 && idRep != idRep1 && idRep != idRep2 && idRep != idRep3 && idRep != idRep4) continue;
                Long idItem = dado.getLong("eaa0103id");
                dado.putAll(dado.getTableMap("eaa0103json"));
                dado.put("codRep", codRep);
                dado.put("naRep", nomeRep);
                dado.put("txComis", txComissao);
                //dado.remove("eaa0103json");

                for (devolucao in listDevolucoesAjustado) {
                    Long idItemDev = devolucao.getLong("eaa01033itemdoc")
                    if (idItem == idItemDev) {
                        comporDevolucoes(dado, devolucao, campos);
                    }
                }

                if (idControle == null) {
                    dadosTmp.putAll(dado);
                    idControle = dado.getLong("eaa01id")
                    somarValores(dado, valoresTotais, campos);
                } else if (idControle == dado.getLong("eaa01id")) {
                    somarValores(dado, valoresTotais, campos);
                } else {
                    TableMap tmp = new TableMap();
                    tmp.putAll(dadosTmp);
                    tmp.putAll(valoresTotais);
                    comporValores(tmp, campos);
                    dadosRelatorio.add(tmp);

                    dadosTmp = new TableMap();
                    dadosTmp.putAll(dado);
                    valoresTotais = new TableMap()
                    idControle = dado.getLong("eaa01id");
                    somarValores(dado, valoresTotais, campos);
                }
            }

            if(dadosTmp.size() > 0){
                TableMap tmp = new TableMap();
                tmp.putAll(dadosTmp);
                tmp.putAll(valoresTotais);
                comporValores(tmp, campos);

                dadosRelatorio.add(tmp)
            }
        }

        if (impressao == 0) return gerarPDF("SRF_Documentos_Por_Representantes_PDF", dadosRelatorio);
        return gerarXLSX("SRF_Documentos_Por_Representantes_Excel", dadosRelatorio);
    }

    private List<TableMap> buscarDadosDocumentos(List<Long> idsReps, Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida) {
        // Data Emissao
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null
        if (dtEmissao != null) {
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        // Data Entrada Saída
        LocalDate esDataIni = null;
        LocalDate esDataFin = null
        if (dtEntradaSaida != null) {
            esDataIni = dtEntradaSaida[0];
            esDataFin = dtEntradaSaida[1];
        }

        String whereNumDoc = "AND abb01nota.abb01num BETWEEN :numDocIni and :numDocFin ";
        String whereMov = resumoOperacao == 0 ? "AND eaa01nota.eaa01esmov = 0 " : "AND eaa01nota.eaa01esmov = 1 ";
        String whereClasDoc = "AND eaa01nota.eaa01clasdoc = 1 "
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01nota.aah01id IN (:idsTipoDoc) " : "";
        String wherePCD = idsPcd != null && idsPcd.size() > 0 ? "AND eaa01nota.eaa01pcd IN (:idsPcd) " : "";
        String whereDataEmissao = dtEmissao != null ? "AND abb01nota.abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "AND eaa01nota.eaa01esdata BETWEEN :esDataIni AND :esDataFin " : "";
        String whereRepresentantes = idsReps != null && idsReps.size() > 0 ? "AND (eaa01nota.eaa01rep0 IN (:idsReps) OR eaa01nota.eaa01rep1 IN (:idsReps) OR eaa01nota.eaa01rep2 IN (:idsReps) OR eaa01nota.eaa01rep3 IN (:idsReps) OR eaa01nota.eaa01rep4 IN (:idsReps)) " : "";

        Parametro parametroNumDocIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumDocFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPCD = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDataEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro parametroDataEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro parametroDataEntradaSaidaIni = dtEntradaSaida != null ? Parametro.criar("esDataIni", esDataIni) : null;
        Parametro parametroDataEntradaSaidaFin = dtEntradaSaida != null ? Parametro.criar("esDataFin", esDataFin) : null;
        Parametro parametroRepresentantes = idsReps != null && idsReps.size() > 0 ? Parametro.criar("idsReps", idsReps) : null;

        String sql = " SELECT eaa01nota.eaa01rep0, eaa01nota.eaa01rep1, eaa01nota.eaa01rep2, eaa01nota.eaa01rep3, eaa01nota.eaa01rep4, eaa01nota.eaa01id AS eaa01id, eaa0103nota.eaa0103id AS eaa0103id, aah01nota.aah01codigo AS tipoDocNota, abb01nota.abb01num AS numNota, abb01nota.abb01data AS dtEmissaoNota, eaa01nota.eaa01esdata AS esDataNota, " +
                " aah01pedido.aah01codigo AS tipoDocPedido,abb01pedido.abb01num AS numPedido, abe01codigo AS codEnt,abe01na AS naEnt, abe30codigo AS codCondPgto, abe30nome AS nomeCondPgto, " +
                " abe40nome AS tabelaPreco, eaa0103nota.eaa0103qtuso AS eaa0103qtuso,eaa0103nota.eaa0103qtcoml AS eaa0103qtcoml, eaa0103nota.eaa0103unit AS eaa0103unit,eaa0103nota.eaa0103total AS eaa0103total,eaa0103nota.eaa0103totdoc AS eaa0103totdoc, eaa0103nota.eaa0103totfinanc AS eaa0103totfinanc, eaa0103nota.eaa0103json AS eaa0103json " +
                " FROM eaa01 AS eaa01nota " +
                " INNER JOIN abb01 AS abb01nota ON eaa01central = abb01nota.abb01id " +
                " INNER JOIN aah01 AS aah01nota ON abb01nota.abb01tipo = aah01nota.aah01id " +
                " INNER JOIN eaa0103 AS eaa0103nota ON eaa0103nota.eaa0103doc = eaa01id " +
                " LEFT JOIN eaa01032 ON eaa01032itemsrf = eaa0103id " +
                " LEFT JOIN eaa0103 AS eaa0103pedido on eaa01032itemscv = eaa0103pedido.eaa0103id " +
                " LEFT JOIN eaa01 AS eaa01pedido ON eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc " +
                " LEFT JOIN abb01 AS abb01pedido ON abb01pedido.abb01id = eaa01pedido.eaa01central " +
                " LEFT JOIN aah01 AS aah01pedido ON aah01pedido.aah01id = abb01pedido.abb01tipo " +
                " INNER JOIN abe01 ON abb01nota.abb01ent = abe01id " +
                " LEFT JOIN abe30 ON eaa01nota.eaa01cp = abe30id " +
                " LEFT JOIN abe40 ON eaa01nota.eaa01tp = abe40id " +
                " WHERE eaa01nota.eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                " AND eaa01nota.eaa01cancData IS NULL " +
                " AND eaa01nota.eaa01nfestat <> 5 " +
                whereNumDoc +
                whereMov +
                whereTipoDoc +
                wherePCD +
                whereDataEmissao +
                whereDataEntradaSaida +
                whereRepresentantes +
                whereClasDoc +
                "ORDER BY abb01nota.abb01num ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroTipoDoc, parametroPCD, parametroDataEmissaoIni, parametroDataEmissaoFin,
                parametroDataEntradaSaidaIni, parametroDataEntradaSaidaFin, parametroRepresentantes);
    }

    private List<TableMap> obterIdsItensDoc(List<Long> idsReps, Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida) {
        // Data Emissao
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null
        if (dtEmissao != null) {
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        // Data Entrada Saída
        LocalDate esDataIni = null;
        LocalDate esDataFin = null
        if (dtEntradaSaida != null) {
            esDataIni = dtEntradaSaida[0];
            esDataFin = dtEntradaSaida[1];
        }

        String whereNumDoc = "AND abb01num BETWEEN :numDocIni and :numDocFin ";
        String whereMov = resumoOperacao == 0 ? "AND eaa01esmov = 0 " : "AND eaa01esmov = 1 ";
        String whereClasDoc = "AND eaa01clasdoc = 1 "
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePCD = idsPcd != null && idsPcd.size() > 0 ? "AND eaa01pcd IN (:idsPcd) " : "";
        String whereDataEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "AND eaa01esdata BETWEEN :esDataIni AND :esDataFin " : "";
        String whereRepresentantes = idsReps != null && idsReps.size() > 0 ? "AND (eaa01rep0 IN (:idsReps) OR eaa01rep1 IN (:idsReps) OR eaa01rep2 IN (:idsReps) OR eaa01rep3 IN (:idsReps) OR eaa01rep4 IN (:idsReps)) " : "";

        Parametro parametroNumDocIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumDocFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPCD = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDataEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro parametroDataEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro parametroDataEntradaSaidaIni = dtEntradaSaida != null ? Parametro.criar("esDataIni", esDataIni) : null;
        Parametro parametroDataEntradaSaidaFin = dtEntradaSaida != null ? Parametro.criar("esDataFin", esDataFin) : null;
        Parametro parametroRepresentantes = idsReps != null && idsReps.size() > 0 ? Parametro.criar("idsReps", idsReps) : null;

        String sql = "SELECT DISTINCT eaa0103id " +
                " FROM eaa01 " +
                " INNER JOIN abb01 ON eaa01central = abb01id " +
                " INNER JOIN aah01 ON abb01tipo = aah01id " +
                " INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                " LEFT JOIN abe30 ON eaa01cp = abe30id " +
                " LEFT JOIN abe40 ON eaa01tp = abe40id " +
                " WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                " AND eaa01cancData IS NULL " +
                " AND eaa01nfestat <> 5 " +
                whereNumDoc +
                whereMov +
                whereTipoDoc +
                wherePCD +
                whereDataEmissao +
                whereDataEntradaSaida +
                whereRepresentantes +
                whereClasDoc;

        return getAcessoAoBanco().obterListaDeLong(sql, parametroNumDocIni, parametroNumDocFin, parametroTipoDoc, parametroPCD, parametroDataEmissaoIni, parametroDataEmissaoFin,
                parametroDataEntradaSaidaIni, parametroDataEntradaSaidaFin, parametroRepresentantes);
    }

    private List<TableMap> buscarRepresentantes(List<Long> ids) {
        String whereReps = ids != null && ids.size() > 0 ? "and abe01id in (:ids) " : "";

        String sql = " SELECT abe01id,abe01na, abe01codigo, abe05taxa " +
                " FROM abe01 " +
                " LEFT JOIN abe05 ON abe05ent = abe01id " +
                " WHERE abe01rep = 1  " +
                whereReps +
                " ORDER BY abe01codigo";

        Parametro p1 = Parametro.criar("ids", ids);

        return getAcessoAoBanco().buscarListaDeTableMap(sql, p1)
    }

    private List<TableMap> obterDevolucao(List<Long> idsItem) {
        def whereItem = " WHERE eaa01033itemdoc IN (:item) "

        def sql = " SELECT eaa01033itemdoc, eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103totfinanc, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json " +
                " FROM eaa01033 " +
                " INNER JOIN eaa0103 on eaa0103id = eaa01033item  " +
                whereItem +
                " AND (eaa01033qtComl > 0 OR eaa01033qtUso > 0) " +
                " ORDER BY eaa01033itemdoc ";

        def p1 = criarParametroSql("item", idsItem)
        return getAcessoAoBanco().buscarListaDeTableMap(sql, p1)
    }

    private void somarValores(TableMap valoresDocumento, TableMap valoresTotais, Map<String, String> campos) {
        for (campo in campos) {
            if (campo != null) {
                String nomeCampo = buscarNomeCampoFixo(campo.value.toString()) != null ? buscarNomeCampoFixo(campo.value.toString()) : campo.value.toString();
                if (valoresDocumento.getBigDecimal(nomeCampo) != null) {
                    if (valoresTotais.getBigDecimal(nomeCampo) == null) {
                        valoresTotais.put(nomeCampo, valoresDocumento.getBigDecimal_Zero(nomeCampo));
                    } else {
                        BigDecimal valorTotal = valoresTotais.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresTotais.getBigDecimal(nomeCampo);
                        BigDecimal valorItem = valoresDocumento.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresDocumento.getBigDecimal(nomeCampo);
                        BigDecimal soma = valorTotal + valorItem;
                        valoresTotais.put(nomeCampo, soma)
                    }
                }
            }
        }

    }

    private void comporValores(TableMap dado, HashMap<String, String> campos) {
        for (campo in campos) {
            if (campo.value != null) {
                String nomeCampo = buscarNomeCampoFixo(campo.value);
                if (nomeCampo != null) {
                    dado.put("nomeCampo" + campo.key, campo.value);
                    dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(nomeCampo))
                } else {
                    nomeCampo = buscarNomeCampoLivre(campo.value);
                    dado.put("nomeCampo" + campo.key, nomeCampo);
                    dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value));
                }
            }
        }
    }

    private void comporDevolucoes(TableMap valores, TableMap devolucao, Map<String, String> campos) {
        for (campo in campos) {
            if (campo != null) {
                String nomeCampo = buscarNomeCampoFixo(campo.value.toString()) != null ? buscarNomeCampoFixo(campo.value.toString()) : campo.value.toString();
                if (valores.getBigDecimal(nomeCampo) != null) {
                    BigDecimal valor = valores.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valores.getBigDecimal(nomeCampo);
                    BigDecimal valorDevolucao = devolucao.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : devolucao.getBigDecimal(nomeCampo);
                    valores.put(nomeCampo, valor - valorDevolucao);
                }
            }
        }
    }

    private buscarNomeCampoFixo(String campo) {
        switch (campo) {
            case "Qtde. de Uso":
                return "eaa0103qtuso"
                break
            case "Qtde. Comercial":
                return "eaa0103qtcoml"
                break
            case "Preço Unitário":
                return "eaa0103unit"
                break
            case "Total do Item":
                return "eaa0103total"
                break
            case "Total Documento":
                return "eaa0103totdoc"
                break
            case "Total Financeiro":
                return "eaa0103totfinanc"
                break
            default:
                return null
                break
        }
    }

    public String buscarNomeCampoLivre(String campo) {
        def sql = " SELECT aah02descr FROM aah02 WHERE aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql, criarParametroSql("nome", campo))

    }
}
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBSZXByZXNlbnRhbnRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBSZXByZXNlbnRhbnRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==