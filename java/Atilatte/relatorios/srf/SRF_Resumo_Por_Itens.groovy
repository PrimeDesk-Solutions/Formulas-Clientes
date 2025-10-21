package Atilatte.relatorios.srf

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SRF_Resumo_Por_Itens extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Resumo Por Itens";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true)
        filtrosDefault.put("total2", true)
        filtrosDefault.put("total3", true)
        filtrosDefault.put("total4", true)
        filtrosDefault.put("total5", true)
        filtrosDefault.put("total6", true)
        filtrosDefault.put("devolucao", true)
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("resumoOperacao", "0");
        filtrosDefault.put("impressao", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idEntidade = getListLong("entidade");
        List<Long> idTipoDocumento = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer numeroInicial = getInteger("numeroInicial") == null || getInteger("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal") == null || getInteger("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal");
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dataEntSai = getIntervaloDatas("dataEntSai");
        Integer resumoOperacao = getInteger("resumoOperacao");
        List<Long> idsItens = getListLong("itens");
        Integer impressao = getInteger("impressao")
        String campoLivre1 = getString("campoLivre1")
        String camposFixo1 = getString("campoFixo1")
        String campoLivre2 = getString("campoLivre2")
        String camposFixo2 = getString("campoFixo2")
        String campoLivre3 = getString("campoLivre3")
        String camposFixo3 = getString("campoFixo3")
        String campoLivre4 = getString("campoLivre4")
        String camposFixo4 = getString("campoFixo4")
        String camposFixo5 = getString("campoFixo5")
        String campoLivre5 = getString("campoLivre5")
        String camposFixo6 = getString("campoFixo6")
        String campoLivre6 = getString("campoLivre6")
        Boolean totalizar1 = getBoolean("total1")
        Boolean totalizar2 = getBoolean("total2")
        Boolean totalizar3 = getBoolean("total3")
        Boolean totalizar4 = getBoolean("total4")
        Boolean totalizar5 = getBoolean("total5")
        Boolean totalizar6 = getBoolean("total6")
        Boolean devolucoes = getBoolean("devolucao");
        List<Integer> mps = getListInteger("mps")

        if (campoLivre1 != null && camposFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre2 != null && camposFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre3 != null && camposFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre4 != null && camposFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre5 != null && camposFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre6 != null && camposFixo6 != null) interromper("Selecione apenas 1 valor por campo!")

        Map<String, String> campos = new HashMap()
        campos.put("1", campoLivre1 != null ? campoLivre1 : camposFixo1 != null ? camposFixo1 : null)
        campos.put("2", campoLivre2 != null ? campoLivre2 : camposFixo2 != null ? camposFixo2 : null)
        campos.put("3", campoLivre3 != null ? campoLivre3 : camposFixo3 != null ? camposFixo3 : null)
        campos.put("4", campoLivre4 != null ? campoLivre4 : camposFixo4 != null ? camposFixo4 : null)
        campos.put("5", campoLivre5 != null ? campoLivre5 : camposFixo5 != null ? camposFixo5 : null)
        campos.put("6", campoLivre6 != null ? campoLivre6 : camposFixo6 != null ? camposFixo6 : null)


        String periodo = ""
        if (dataEmissao != null) {
            periodo = "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        } else if (dataEntSai) {
            periodo = "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());
        params.put("periodo", periodo);
        params.put("titulo", "SRF - Resumo Por Itens");
        params.put("totalizar1", totalizar1);
        params.put("totalizar2", totalizar2);
        params.put("totalizar3", totalizar3);
        params.put("totalizar4", totalizar4);
        params.put("totalizar5", totalizar5);
        params.put("totalizar6", totalizar6);


        List<TableMap> dados = buscarItensDoc(idEntidade, idTipoDocumento, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, idsItens, idsPcd, mps);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<Long> idsItensDoc = obterIdsItensDoc(idEntidade, idTipoDocumento, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, idsItens, idsPcd, mps);

        List<TableMap> dadosRelatorio = new ArrayList();
        List<TableMap> listDevolucoesGeral = new ArrayList();
        List<TableMap> listDevolucoesAjustado = new ArrayList<>()
        def idControle = null;
        def idControleDevolucao = null;
        TableMap dadosTmp = new TableMap();
        TableMap dadosTmpDev = new TableMap()
        TableMap valoresTotais = new TableMap();
        TableMap valoresTotaisDevolucao = new TableMap()

//		 Agrupa as devoluções
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

        for (dado in dados) {
            Long idItem = dado.getLong("eaa0103id");
            dado.putAll(dado.getTableMap("eaa0103json"));
            dado.remove("eaa0103json");
            for (devolucao in listDevolucoesAjustado) {
                Long idItemDev = devolucao.getLong("eaa01033itemdoc")
                if (idItem == idItemDev) {
                    comporDevolucoes(dado, devolucao, campos);
                }
            }

            if (idControle == null) {
                dadosTmp.putAll(dado);
                idControle = dado.getLong("abm01id");
                somarValores(dado, valoresTotais, campos);
            } else if (idControle == dado.getLong("abm01id")) {
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
                idControle = dado.getLong("abm01id");
                somarValores(dado, valoresTotais, campos);
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);
        comporValores(tmp, campos);

        dadosRelatorio.add(tmp)



        if (impressao == 1) return gerarXLSX("SRF_Resumo_Por_Itens_Excel", dadosRelatorio)
        return gerarPDF("SRF_Resumo_Por_Itens_PDF", dadosRelatorio);
    }

    private List<TableMap> buscarItensDoc(List<Long> idEntidade, List<Long> idTipoDocumento, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial, Integer numeroFinal,
                                          Integer resumoOperacao, List<Long> idsItens, List<Long> pcds, List<Integer> mps) {

        String whereNumIni = numeroInicial != null ? " AND abb01num >= :numeroInicial " : ""
        String whereNumFim = numeroFinal != null ? " AND abb01num <= :numeroFinal " : ""
        String whereEsData = dataEntSai != null && dataEntSai.size() > 1 ? " AND eaa01esdata BETWEEN :esDataIni AND :esDataFim " : ""
        String whereEmissaoData = dataEmissao != null && dataEmissao.size() > 1 ? " AND abb01data BETWEEN :emissaoDataIni AND :emissaoDataFim " : ""
        String wherePcd = pcds != null && pcds.size() > 0 ? " AND eaa01pcd IN (:pcds) " : ""
        String whereItens = idsItens != null && idsItens.size() > 0 ? " AND abm01id IN (:idItens)" : ""
        String whereEntidade = idEntidade != null && idEntidade.size() > 0 ? " AND abe01id IN (:idEntidade) " : ""
        String whereTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " AND abb01tipo in (:idTipoDocumento) " : ""
        String whereClassDoc = " AND eaa01clasDoc = 1 "
        String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0"
        String whereMps = mps != null && !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";

        Parametro parametroNumIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null
        Parametro parametroNumFim = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null
        Parametro parametroEsDataIni = dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataIni", dataEntSai[0]) : null
        Parametro parametroEsdataFim = dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataFim", dataEntSai[1]) : null
        Parametro parametroEmissaoDataIni = dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataIni", dataEmissao[0]) : null
        Parametro parametroEmissaodataFim = dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataFim", dataEmissao[1]) : null
        Parametro parametroPcd = pcds != null && pcds.size() > 0 ? Parametro.criar("pcds", pcds) : null
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null
        Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null
        Parametro parametroTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null
        Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;


        String sql = " SELECT abm01id,abm01tipo, abm01codigo,abm01descr,aam06codigo, eaa0103qtuso, " +
                        " eaa0103qtcoml,eaa0103unit,eaa0103total,eaa0103totdoc ,eaa0103totfinanc, eaa0103id, eaa0103json, eaa01id " +
                        " FROM eaa0103 " +
                        " INNER JOIN eaa01 on eaa01id = eaa0103doc " +
                        " INNER JOIN abb01 on abb01id = eaa01central " +
                        " INNER JOIN abm01 on abm01id = eaa0103item " +
                        " LEFT JOIN aam06 on aam06id = abm01umu " +
                        " LEFT JOIN abe01 on abe01id = abb01ent " +
                        " WHERE eaa01clasDoc = 1 " +
                        " AND eaa01cancData IS NULL " +
                        " AND eaa01nfestat <> 5 " +
                        whereNumIni +
                        whereNumFim +
                        whereEsData +
                        whereEmissaoData +
                        wherePcd +
                        whereItens +
                        whereEntidade +
                        whereTipoDoc +
                        whereClassDoc +
                        whereES +
                        whereMps +
                        "ORDER BY abm01codigo, abm01tipo"


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumIni, parametroNumFim, parametroEsDataIni, parametroEsdataFim, parametroEmissaoDataIni, parametroEmissaodataFim, parametroPcd, parametroItens, parametroEntidade, parametroTipoDoc, parametroMps)
    }

    private List<Long> obterIdsItensDoc(List<Long> idEntidade, List<Long> idTipoDocumento, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial, Integer numeroFinal,
                                          Integer resumoOperacao, List<Long> idsItens, List<Long> pcds, List<Integer> mps) {

        String whereNumIni = numeroInicial != null ? " AND abb01num >= :numeroInicial " : ""
        String whereNumFim = numeroFinal != null ? " AND abb01num <= :numeroFinal " : ""
        String whereEsData = dataEntSai != null && dataEntSai.size() > 1 ? " AND eaa01esdata BETWEEN :esDataIni AND :esDataFim " : ""
        String whereEmissaoData = dataEmissao != null && dataEmissao.size() > 1 ? " AND abb01data BETWEEN :emissaoDataIni AND :emissaoDataFim " : ""
        String wherePcd = pcds != null && pcds.size() > 0 ? " AND eaa01pcd IN (:pcds) " : ""
        String whereItens = idsItens != null && idsItens.size() > 0 ? " AND abm01id IN (:idItens)" : ""
        String whereEntidade = idEntidade != null && idEntidade.size() > 0 ? " AND abe01id IN (:idEntidade) " : ""
        String whereTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " AND abb01tipo in (:idTipoDocumento) " : ""
        String whereClassDoc = " AND eaa01clasDoc = 1 "
        String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0"
        String whereMps = mps != null && !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";

        Parametro parametroNumIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null
        Parametro parametroNumFim = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null
        Parametro parametroEsDataIni = dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataIni", dataEntSai[0]) : null
        Parametro parametroEsdataFim = dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataFim", dataEntSai[1]) : null
        Parametro parametroEmissaoDataIni = dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataIni", dataEmissao[0]) : null
        Parametro parametroEmissaodataFim = dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataFim", dataEmissao[1]) : null
        Parametro parametroPcd = pcds != null && pcds.size() > 0 ? Parametro.criar("pcds", pcds) : null
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null
        Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null
        Parametro parametroTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null
        Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;


        String sql = " SELECT eaa0103id " +
                " FROM eaa0103 " +
                " INNER JOIN eaa01 on eaa01id = eaa0103doc " +
                " INNER JOIN abb01 on abb01id = eaa01central " +
                " INNER JOIN abm01 on abm01id = eaa0103item " +
                " LEFT JOIN aam06 on aam06id = abm01umu " +
                " LEFT JOIN abe01 on abe01id = abb01ent " +
                " WHERE eaa01clasDoc = 1 " +
                " AND eaa01cancData IS NULL " +
                " AND eaa01nfestat <> 5 " +
                whereNumIni +
                whereNumFim +
                whereEsData +
                whereEmissaoData +
                wherePcd +
                whereItens +
                whereEntidade +
                whereTipoDoc +
                whereClassDoc +
                whereES +
                whereMps +
                "ORDER BY abm01codigo, abm01tipo"


        return getAcessoAoBanco().obterListaDeLong(sql, parametroNumIni, parametroNumFim, parametroEsDataIni, parametroEsdataFim, parametroEmissaoDataIni, parametroEmissaodataFim, parametroPcd, parametroItens, parametroEntidade, parametroTipoDoc, parametroMps)
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

    private List<TableMap> obterDevolucao(List<Long> idsItem) {
        def whereItem = " WHERE eaa01033itemdoc IN (:item) "

        def sql = " SELECT eaa01033itemdoc, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103totfinanc, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json " +
                " FROM eaa01033 " +
                " INNER JOIN eaa0103 on eaa0103id = eaa01033item  " +
                whereItem +
                " AND (eaa01033qtComl > 0 OR eaa01033qtUso > 0) " +
                " ORDER BY eaa01033itemdoc ";

        def p1 = criarParametroSql("item", idsItem)
        return getAcessoAoBanco().buscarListaDeTableMap(sql, p1)
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

    public String buscarNomeCampoFixo(String campo) {
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
        }
    }

    public String buscarNomeCampoLivre(String campo) {
        def sql = " SELECT aah02descr FROM aah02 WHERE aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql, criarParametroSql("nome", campo))

    }
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBQb3IgSXRlbnMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBQb3IgSXRlbnMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=