package Atilatte.relatorios.srf

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
import sam.model.entities.ea.Eaa01



public class SRF_Resumo_Por_Representantes extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "SRF - Resumo por Representantes";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
        filtrosDefault.put("total4", true);
        filtrosDefault.put("total5", true);
        filtrosDefault.put("total6", true);
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
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idsReps = getListLong("representantes");
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
        params.put("title", "SRF - Documento por Entidades");
        params.put("empresa", empresa.aac10codigo + "-" + empresa.aac10na);
        params.put("periodo", periodo);

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
        List<TableMap> documentos = new ArrayList<>()
        List<TableMap> dadosRelatorio = new ArrayList();
        List<TableMap> idsItensDoc = obterIdsItensDoc(idsReps, numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, idEmpresa);

        for (representante in representantes) {
            Long idRep = representante.getLong("abe01id");
            String codRep = representante.getString("abe01codigo");
            String nomeRep = representante.getString("abe01na");
            def txComissao = representante.getBigDecimal_Zero("abe05taxa");
            List<TableMap> listDevolucoesGeral = new ArrayList();
            List<TableMap> listDevolucoesAjustado = new ArrayList<>()
            def idControle = null;
            def idControleDevolucao = null;
            TableMap dadosTmp = new TableMap();
            TableMap dadosTmpDev = new TableMap()
            TableMap valoresTotais = new TableMap();
            TableMap valoresTotaisDevolucao = new TableMap();

            List<TableMap> dados = buscarDadosDocumentos(idRep, numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, idEmpresa);

            if(dados == null || dados.size() == 0) continue;

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

            for (dado in dados) {
                Long idItem = dado.getLong("eaa0103id");
                dado.putAll(dado.getTableMap("eaa0103json"));
                dado.put("codRep", codRep);
                dado.put("naRep", nomeRep);
                dado.put("txComis", txComissao );
                dado.remove("eaa0103json");

                for (devolucao in listDevolucoesAjustado) {
                    Long idItemDev = devolucao.getLong("eaa01033itemdoc")
                    if (idItem == idItemDev) {
                        comporDevolucoes(dado, devolucao, campos);
                    }
                }

                if (idControle == null) {
                    dadosTmp.putAll(dado);
                    idControle = idRep;
                    somarValores(dado, valoresTotais, campos);
                } else if (idControle == idRep) {
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
                    idControle = idRep;
                    somarValores(dado, valoresTotais, campos);
                }
            }

            TableMap tmp = new TableMap();
            tmp.putAll(dadosTmp);
            tmp.putAll(valoresTotais);
            comporValores(tmp, campos);

            dadosRelatorio.add(tmp)
        }

        if (impressao == 0) return gerarPDF("SRF_ResumoPorRepresentantes(PDF)", dadosRelatorio);

        return gerarXLSX("SRF_ResumoPorRepresentantes(Excel)", dadosRelatorio);
    }

    private List<TableMap> buscarDadosDocumentos(Long idRepresentante, Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida,
                                                 List<Long> idsEntidades, Long idEmpresa) {
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

        String whereNumDoc = "AND abb01num BETWEEN :numDocIni AND :numDocFin ";
        String whereMov = resumoOperacao == 0 ? "AND eaa01esmov = 0 " : "AND eaa01esmov = 1 ";
        String whereClasDoc = "AND eaa01clasdoc = 1 "
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePCD = idsPcd != null && idsPcd.size() > 0 ? "AND eaa01pcd IN (:idsPcd) " : "";
        String whereDataEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "AND eaa01esdata BETWEEN :esDataIni AND :esDataFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "AND abe01id IN (:idsEntidades) " : "";
        String whereRepresentantes = "AND (eaa01rep0 = :idRepresentante OR eaa01rep1 = :idRepresentante OR eaa01rep2 = :idRepresentante OR eaa01rep3 = :idRepresentante OR eaa01rep4 = :idRepresentante) ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        Parametro parametroNumDocIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumDocFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPCD = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDataEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro parametroDataEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro parametroDataEntradaSaidaIni = dtEntradaSaida != null ? Parametro.criar("esDataIni", esDataIni) : null;
        Parametro parametroDataEntradaSaidaFin = dtEntradaSaida != null ? Parametro.criar("esDataFin", esDataFin) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroRepresentantes = Parametro.criar("idRepresentante", idRepresentante);
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);

        String sql = "SELECT eaa01id, eaa0103id, eaa0103qtuso,eaa0103qtcoml, eaa0103unit, eaa0103unit,eaa0103total,eaa0103totdoc, eaa0103totfinanc, eaa0103json " +
                "FROM eaa01 " +
                "INNER JOIN abb01 ON eaa01central = abb01id " +
                "INNER JOIN aah01 ON abb01tipo = aah01id " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                " WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                " AND eaa01cancData IS NULL " +
                " AND eaa01nfestat <> 5 " +
                whereNumDoc +
                whereMov +
                whereTipoDoc +
                wherePCD +
                whereDataEmissao +
                whereDataEntradaSaida +
                whereEntidade +
                whereRepresentantes +
                whereClasDoc +
                whereEmpresa +
                "ORDER BY eaa01id";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroTipoDoc, parametroPCD, parametroDataEmissaoIni, parametroDataEmissaoFin,
                parametroDataEntradaSaidaIni, parametroDataEntradaSaidaFin, parametroEntidade, parametroRepresentantes, parametroEmpresa);
    }
    private List<Long> obterIdsItensDoc(List<Long> idsReps, Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida,
                                                 List<Long> idsEntidades, Long idEmpresa) {
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

        String whereNumDoc = "AND abb01num BETWEEN :numDocIni AND :numDocFin ";
        String whereMov = resumoOperacao == 0 ? "AND eaa01esmov = 0 " : "AND eaa01esmov = 1 ";
        String whereClasDoc = "AND eaa01clasdoc = 1 "
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePCD = idsPcd != null && idsPcd.size() > 0 ? "AND eaa01pcd IN (:idsPcd) " : "";
        String whereDataEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "AND eaa01esdata BETWEEN :esDataIni AND :esDataFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "AND abe01id IN (:idsEntidades) " : "";
        String whereRepresentantes = idsReps != null && idsReps.size() > 0 ? "AND (eaa01rep0 IN (:idsReps) OR eaa01rep1 IN (:idsReps) OR eaa01rep2 IN (:idsReps) OR eaa01rep3 IN (:idsReps) OR eaa01rep4 IN (:idsReps)) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        Parametro parametroNumDocIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumDocFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPCD = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDataEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro parametroDataEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro parametroDataEntradaSaidaIni = dtEntradaSaida != null ? Parametro.criar("esDataIni", esDataIni) : null;
        Parametro parametroDataEntradaSaidaFin = dtEntradaSaida != null ? Parametro.criar("esDataFin", esDataFin) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroRepresentantes = idsReps != null && idsReps.size() > 0 ? Parametro.criar("idsReps", idsReps) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);

        String sql = "SELECT DISTINCT eaa0103id " +
                    "FROM eaa01 " +
                    "INNER JOIN abb01 ON eaa01central = abb01id " +
                    "INNER JOIN aah01 ON abb01tipo = aah01id " +
                    "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                    " WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                    " AND eaa01cancData IS NULL " +
                    " AND eaa01nfestat <> 5 " +
                    whereNumDoc +
                    whereMov +
                    whereTipoDoc +
                    wherePCD +
                    whereDataEmissao +
                    whereDataEntradaSaida +
                    whereEntidade +
                    whereRepresentantes +
                    whereClasDoc +
                    whereEmpresa;

        return getAcessoAoBanco().obterListaDeLong(sql, parametroNumDocIni, parametroNumDocFin, parametroTipoDoc, parametroPCD, parametroDataEmissaoIni, parametroDataEmissaoFin,
                parametroDataEntradaSaidaIni, parametroDataEntradaSaidaFin, parametroEntidade, parametroRepresentantes, parametroEmpresa);
    }

    private List<TableMap> buscarRepresentantes(List<Long> ids) {
        String whereReps = ids != null && ids.size() > 0 ? "and abe01id in (:ids) " : "";

        String sql = " select abe01id,abe01na, abe01codigo, abe05taxa " +
                " from abe01 " +
                " left join abe05 on abe05ent = abe01id " +
                " where abe01rep = 1  " +
                whereReps +
                " order by abe01codigo";

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
//meta-sis-eyJkZXNjciI6IlNSRiBSZXN1bW8gUG9yIFJlcHJlc2VudGFudGVzIiwidGlwbyI6InJlbGF0b3JpbyJ9