package Silcon.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.model.entities.aa.Aac10;

class SRF_Itens_Por_Entidades extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Itens Por Entidades";
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
        filtrosDefault.put("chkDevolucao", true)
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("resumoOperacao", "0");
        filtrosDefault.put("impressao", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial") == null || getInteger("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal") == null || getInteger("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal");
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer resumoOperacao = getInteger("resumoOperacao");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idsTransps = getListLong("transportadora");
        List<Long> idsItens = getListLong("itens")
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

        if (campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!")

        Map<String, String> campos = new HashMap()
        campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null)
        campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null)
        campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null)
        campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null)
        campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null)
        campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null)

        String periodo = ""
        if (dtEmissao != null) {
            periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        } else if (dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na())
        params.put("title", "SRF - Itens Por Entidades")
        params.put("periodo", periodo)
        params.put("totalizar1", totalizar1)
        params.put("totalizar2", totalizar2)
        params.put("totalizar3", totalizar3)
        params.put("totalizar4", totalizar4)
        params.put("totalizar5", totalizar5)
        params.put("totalizar6", totalizar6)

        List<TableMap> dados = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, idsItens, idEmpresa, idsTransps);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<Long> idsItensDoc = obterIdsItensDoc(numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, idsItens, idEmpresa, idsTransps);

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


        if (impressao == 0) return gerarPDF("SRF_Itens_Por_Entidades_PDF", dadosRelatorio);
        return gerarXLSX("SRF_Itens_Por_Entidades_Excel", dadosRelatorio);

    }

    private List<TableMap> buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, List<Long> idsItens, Long idEmpresa, List<Long> idsTransps) {

        String whereNumIni = numDocIni != null ? "AND abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "AND abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id IN (:idsPcd) " : "";
        String whereDtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? "AND abb01data between :dtEmissIni AND :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaida != null && dtEntradaSaida != null ? "AND eaa01esdata BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "AND ent.abe01id IN (:idsEntidades) " : "";
        String whereTransp = idsTransps != null && idsTransps.size() > 0 ? "AND redep.abe01id IN (:idsTransps) " : "";
        String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id in (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";


        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTransp = idsTransps != null && idsTransps.size() > 0 ? Parametro.criar("idsTransps", idsTransps) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);



        String sql = "SELECT ent.abe01codigo AS codEnt, ent.abe01na AS naEnt,ent.abe01id AS idEnt,aam06codigo,abm01codigo,abm01id, abm01na, " +
                "CASE WHEN abm01tipo = 0 THEN 'M' when abm01tipo = 1 THEN 'P' when abm01tipo = 2 then 'S' ELSE 'MER' END AS mps, " +
                "eaa0103qtuso,eaa0103qtcoml,eaa0103unit,eaa0103total,eaa0103totdoc,eaa0103totfinanc, eaa0103id, eaa01id, eaa0103json " +
                "FROM eaa01 " +
                "INNER JOIN abb01 on abb01id = eaa01central " +
                "INNER JOIN eaa0103 on eaa0103doc = eaa01id " +
                "INNER JOIN abm01 on abm01id = eaa0103item " +
                "INNER JOIN aam06 on aam06id = abm01umu " +
                "INNER JOIN abe01 as ent on ent.abe01id = abb01ent " +
                "INNER JOIN aah01 on aah01id = abb01tipo " +
                "INNER JOIN abd01 on abd01id = eaa01pcd  " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "LEFT JOIN abe01 AS redesp ON redesp.abe01id = eaa0102redespacho " +
                "WHERE eaa01clasDoc = 1 " +
                "AND eaa01cancData IS NULL " +
                "AND eaa01nfestat <> 5 " +
                whereEmpresa +
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereES +
                whereItens +
                whereTransp +
                "ORDER BY ent.abe01codigo, abm01codigo "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin,
                parametroEntidade, parametroItens, parametroTransp);
    }

    private List<Long> obterIdsItensDoc(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, List<Long> idsItens, Long idEmpresa, List<Long> idsTransps) {

        String whereNumIni = numDocIni != null ? "AND abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "AND abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id IN (:idsPcd) " : "";
        String whereDtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? "AND abb01data between :dtEmissIni AND :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaida != null && dtEntradaSaida != null ? "AND eaa01esdata BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "AND ent.abe01id IN (:idsEntidades) " : "";
        String whereTransp = idsTransps != null && idsTransps.size() > 0 ? "AND desp.abe01id IN (:idsTransps) " : "";
        String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id in (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";


        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTransp = idsTransps != null && idsTransps.size() > 0 ? Parametro.criar("idsTransps", idsTransps) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);



        String sql = "SELECT eaa0103id " +
                "FROM eaa01 " +
                "INNER JOIN abb01 on abb01id = eaa01central " +
                "INNER JOIN eaa0103 on eaa0103doc = eaa01id " +
                "INNER JOIN abm01 on abm01id = eaa0103item " +
                "INNER JOIN aam06 on aam06id = abm01umu " +
                "INNER JOIN abe01 as ent on ent.abe01id = abb01ent " +
                "INNER JOIN aah01 on aah01id = abb01tipo " +
                "INNER JOIN abd01 on abd01id = eaa01pcd  " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "LEFT JOIN abe01 AS redesp ON redesp.abe01id = eaa0102redespacho " +
                "WHERE eaa01clasDoc = 1 " +
                "AND eaa01cancData IS NULL " +
                "AND eaa01nfestat <> 5 " +
                whereEmpresa +
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereES +
                whereItens +
                whereTransp +
                "ORDER BY ent.abe01codigo, abm01codigo "

        return getAcessoAoBanco().obterListaDeLong(sql, parametroEmpresa, parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin,
                parametroEntidade, parametroItens, parametroTransp);
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
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBQb3IgSXRlbSAtIExDUiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiBJdGVucyBQb3IgRW50aWRhZGVzIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNSRiAtIEl0ZW5zIFBvciBFbnRpZGFkZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=