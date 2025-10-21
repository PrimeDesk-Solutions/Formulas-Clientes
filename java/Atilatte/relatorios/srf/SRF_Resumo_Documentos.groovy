package Atilatte.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;

import sam.model.entities.aa.Aac10;

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class SRF_Resumo_Documentos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Resumo Documentos ";
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
        Integer numDocIni = getInteger("numeroInicial") == null || getInteger("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal") == null || getInteger("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal");
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer resumoOperacao = getInteger("resumoOperacao");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsEntidades = getListLong("entidade");
        LocalDate[] dtNascimento = getIntervaloDatas("dataNascimento");
        List<Long> idsEstados = getListLong("estados");
        List<Long> idsMunicipios = getListLong("municipios");
        List<Long> idsTransportadora = getListLong("transportadoras");
        List<Long> idsItens = getListLong("itens")
        Integer optionResumo = getInteger("resumo");
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
        List<Integer> mps = getListInteger("mps")
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
        params.put("empresa", empresa.aac10codigo + "-" + empresa.aac10na);
        params.put("periodo", periodo);

        if (campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!")

        Map<String, String> campos = new HashMap<>();

        campos.put("1", campoFixo1 != null ? campoFixo1 : campoLivre1 != null ? campoLivre1 : null);
        campos.put("2", campoFixo2 != null ? campoFixo2 : campoLivre2 != null ? campoLivre2 : null);
        campos.put("3", campoFixo3 != null ? campoFixo3 : campoLivre3 != null ? campoLivre3 : null);
        campos.put("4", campoFixo4 != null ? campoFixo4 : campoLivre4 != null ? campoLivre4 : null);
        campos.put("5", campoFixo5 != null ? campoFixo5 : campoLivre5 != null ? campoLivre5 : null);
        campos.put("6", campoFixo6 != null ? campoFixo6 : campoLivre6 != null ? campoLivre6 : null);

        List<TableMap> dados = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, dtNascimento, idsEstados, idsMunicipios, idsTransportadora, optionResumo, idsItens, idEmpresa, mps);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<Long> idsItensDoc = obterIdsItensDoc(numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, dtNascimento, idsEstados, idsMunicipios, idsTransportadora, idsItens, idEmpresa, mps);
        List<TableMap> dadosRelatorio = new ArrayList();
        List<TableMap> listDevolucoesGeral = new ArrayList();
        List<TableMap> listDevolucoesAjustado = new ArrayList<>()
        def idControle = null;
        def idControleDevolucao = null;
        TableMap dadosTmp = new TableMap();
        TableMap dadosTmpDev = new TableMap()
        TableMap valoresTotais = new TableMap();
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

        String campoControle = ""
        if (optionResumo == 0) {
            campoControle = "idEnt";
        } else if (optionResumo == 1) {
            campoControle = "idTransp";
        } else if (optionResumo == 2) {
            campoControle = "idMunic";
        } else if (optionResumo == 3) {
            campoControle = "idEstado";
        } else {
            campoControle = "abm01id";
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
                idControle = dado.getLong(campoControle);
                somarValores(dado, valoresTotais, campos);
            } else if (idControle == dado.getLong(campoControle)) {
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
                idControle = dado.getLong(campoControle);
                somarValores(dado, valoresTotais, campos);
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);
        comporValores(tmp, campos);

        dadosRelatorio.add(tmp)

        if (optionResumo == 0) {
            params.put("title", "SRF - Resumo por Entidades");
            if (impressao == 1) return gerarXLSX("SRF_Resumo_Por_Entidade_Excel", dadosRelatorio);
            return gerarPDF("SRF_Resumo_Por_Entidade_PDF", dadosRelatorio);
        } else if (optionResumo == 1) {
            params.put("title", "SRF - Resumo por Transportadora");
            if (impressao == 1) return gerarXLSX("SRF_Resumo_Por_Transportadora_Excel", dadosRelatorio);
            return gerarPDF("SRF_Resumo_Por_Transportadora_PDF", dadosRelatorio)
        } else if (optionResumo == 2) {
            params.put("title", "SRF - Resumo por Municípios");
            if (impressao == 1) return gerarXLSX("SRF_Resumo_Por_Municipios_Excel", dadosRelatorio);
            return gerarPDF("SRF_Resumo_Por_Municipios_PDF", dadosRelatorio)
        } else if (optionResumo == 3) {
            params.put("title", "SRF - Resumo por Estados");
            if (impressao == 1) return gerarXLSX("SRF_Resumo_Por_Estados_Excel", dadosRelatorio);
            return gerarPDF("SRF_Resumo_Por_Estados_PDF", dadosRelatorio);
        } else {
            params.put("titulo", "SRF - Resumo por Itens");
            if (impressao == 1) return gerarXLSX("SRF_Resumo_Por_Itens_Excel", dadosRelatorio);
            return gerarPDF("SRF_Resumo_Por_Itens_PDF", dadosRelatorio)
        }

    }

    private List<TableMap> buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, LocalDate[] dtNascimento, List<Long> idsEstados,
                             List<Long> idsMunicipios, List<Long> idsTransportadora, Integer optionResumo, List<Long> idsItens, Long idEmpresa, List<Integer> mps) {

        //Data Emissao
        LocalDate dtEmissIni = null;
        LocalDate dtEmissFin = null;
        if (dtEmissao != null) {
            dtEmissIni = dtEmissao[0];
            dtEmissFin = dtEmissao[1];
        }

        //Data Entrada/Saida
        LocalDate dtEntradaSaidaIni = null;
        LocalDate dtEntradaSaidaFin = null;
        if (dtEntradaSaida != null) {
            dtEntradaSaidaIni = dtEntradaSaida[0];
            dtEntradaSaidaFin = dtEntradaSaida[1];
        }

        //Data Nascimento
        LocalDate dtNascIni = null;
        LocalDate dtNascFin = null;
        if (dtNascimento != null) {
            dtNascIni = dtNascimento[0];
            dtNascFin = dtNascimento[1];
        }

        String whereNumIni = numDocIni != null ? "AND abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "AND abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id IN (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "AND abb01data BETWEEN :dtEmissIni AND :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "AND eaa01esdata BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "AND ent.abe01id IN (:idsEntidades) " : "";
        String whereDtNasc = dtNascIni != null && dtNascFin != null ? "AND ent.abe01dtnasc BETWEEN :dtNascIni AND :dtNascFin " : "";
        String whereEstados = idsEstados != null && idsEstados.size() > 0 ? "AND aag02id IN (:idsEstados) " : "";
        String whereMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? "AND aag0201id IN (:idsMunicipios) " : "";
        String whereTransportadora = idsTransportadora != null && idsTransportadora.size() > 0 ? "AND eaa0102redespacho IN (:idsTransportadora) " : "";
        String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereMps = mps != null && !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";



        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroDtNascIni = dtNascimento != null && dtNascimento.size() > 0 ? Parametro.criar("dtNascIni", dtNascimento[0]) : null;
        Parametro parametroDtNascFin = dtNascimento != null && dtNascimento.size() > 0 ? Parametro.criar("dtNascFin", dtNascimento[1]) : null;
        Parametro parametroEstados = idsEstados != null && idsEstados.size() > 0 ? Parametro.criar("idsEstados", idsEstados) : null;
        Parametro parametroMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? Parametro.criar("idsMunicipios", idsMunicipios) : null;
        Parametro parametroTransportadora = idsTransportadora != null && idsTransportadora.size() > 0 ? Parametro.criar("idsTransportadora", idsTransportadora) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);
        Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;

        String orderBy = "";
        if (optionResumo == 0) {
            orderBy = "ORDER BY ent.abe01id";
        } else if (optionResumo == 1) {
            orderBy = "ORDER BY transp.abe01id";
        } else if (optionResumo == 2) {
            orderBy = "ORDER BY aag02uf, aag0201nome";
        } else if (optionResumo == 3) {
            orderBy = "ORDER BY aag02uf";
        } else {
            orderBy = "ORDER BY abm01codigo";
        }

        String resumo = buscarResumo(optionResumo);

        String sql = "SELECT " + resumo + 
                "eaa0103qtuso,eaa0103qtcoml,eaa0103unit,eaa0103total,eaa0103totdoc ,eaa0103totfinanc, eaa0103id, eaa01id, eaa0103json " +
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN abe01 AS ent ON ent.abe01id = abb01ent " +
                "INNER JOIN abe0101 ON abe0101ent = ent.abe01id and abe0101principal = 1 " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "LEFT JOIN abb10 ON abb10id = abd01opercod " +
                "LEFT JOIN aag0201 ON aag0201id = abe0101municipio " +
                "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "LEFT JOIN aam06 ON aam06id = abm01umu " +
                "LEFT join abe01 AS transp ON transp.abe01id = eaa0102despacho " +
                "WHERE eaa01clasDoc = 1 " +
                "AND eaa01cancData IS NULL " +
                "AND eaa01nfestat <> 5 " +
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereDtNasc +
                whereEstados +
                whereMunicipios +
                whereTransportadora +
                whereES +
                whereItens +
                whereEmpresa +
                whereMps +
                orderBy;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin, parametroEntidade, parametroDtNascIni,
                parametroDtNascFin, parametroEstados, parametroMunicipios, parametroTransportadora, parametroItens, parametroEmpresa, parametroMps)
    }

    private List<Long> obterIdsItensDoc(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, LocalDate[] dtNascimento, List<Long> idsEstados,
                                            List<Long> idsMunicipios, List<Long> idsTransportadora, List<Long> idsItens, Long idEmpresa, List<Integer> mps) {

        //Data Emissao
        LocalDate dtEmissIni = null;
        LocalDate dtEmissFin = null;
        if (dtEmissao != null) {
            dtEmissIni = dtEmissao[0];
            dtEmissFin = dtEmissao[1];
        }

        //Data Entrada/Saida
        LocalDate dtEntradaSaidaIni = null;
        LocalDate dtEntradaSaidaFin = null;
        if (dtEntradaSaida != null) {
            dtEntradaSaidaIni = dtEntradaSaida[0];
            dtEntradaSaidaFin = dtEntradaSaida[1];
        }

        //Data Nascimento
        LocalDate dtNascIni = null;
        LocalDate dtNascFin = null;
        if (dtNascimento != null) {
            dtNascIni = dtNascimento[0];
            dtNascFin = dtNascimento[1];
        }

        String whereNumIni = numDocIni != null ? "AND abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "AND abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id IN (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "AND abb01data BETWEEN :dtEmissIni AND :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "AND eaa01esdata BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "AND ent.abe01id IN (:idsEntidades) " : "";
        String whereDtNasc = dtNascIni != null && dtNascFin != null ? "AND ent.abe01dtnasc BETWEEN :dtNascIni AND :dtNascFin " : "";
        String whereEstados = idsEstados != null && idsEstados.size() > 0 ? "AND aag02id IN (:idsEstados) " : "";
        String whereMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? "AND aag0201id IN (:idsMunicipios) " : "";
        String whereTransportadora = idsTransportadora != null && idsTransportadora.size() > 0 ? "AND eaa0102redespacho IN (:idsTransportadora) " : "";
        String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereMps = mps != null && !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";



        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroDtNascIni = dtNascimento != null && dtNascimento.size() > 0 ? Parametro.criar("dtNascIni", dtNascimento[0]) : null;
        Parametro parametroDtNascFin = dtNascimento != null && dtNascimento.size() > 0 ? Parametro.criar("dtNascFin", dtNascimento[1]) : null;
        Parametro parametroEstados = idsEstados != null && idsEstados.size() > 0 ? Parametro.criar("idsEstados", idsEstados) : null;
        Parametro parametroMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? Parametro.criar("idsMunicipios", idsMunicipios) : null;
        Parametro parametroTransportadora = idsTransportadora != null && idsTransportadora.size() > 0 ? Parametro.criar("idsTransportadora", idsTransportadora) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);
        Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;

        String sql = "SELECT eaa0103id "+
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN abe01 AS ent ON ent.abe01id = abb01ent " +
                "INNER JOIN abe0101 ON abe0101ent = ent.abe01id and abe0101principal = 1 " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "LEFT JOIN abb10 ON abb10id = abd01opercod " +
                "LEFT JOIN aag0201 ON aag0201id = abe0101municipio " +
                "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "LEFT JOIN aam06 ON aam06id = abm01umu " +
                "LEFT join abe01 AS transp ON transp.abe01id = eaa0102despacho " +
                "WHERE eaa01clasDoc = 1 " +
                "AND eaa01cancData IS NULL " +
                "AND eaa01nfestat <> 5 " +
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereDtNasc +
                whereEstados +
                whereMunicipios +
                whereTransportadora +
                whereES +
                whereItens +
                whereEmpresa +
                whereMps;
        return getAcessoAoBanco().obterListaDeLong(sql, parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin, parametroEntidade, parametroDtNascIni,
                parametroDtNascFin, parametroEstados, parametroMunicipios, parametroTransportadora, parametroItens, parametroEmpresa, parametroMps)
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

    private String buscarResumo(Integer optionResumo) {

        switch (optionResumo) {
            case 0:
                return "ent.abe01codigo AS codEnt,ent.abe01na AS naEnt,ent.abe01id AS idEnt, "
                break;
            case 1:
                return "transp.abe01codigo AS codTransp, transp.abe01na AS naTransp, transp.abe01id AS idTransp, "
                break;
            case 2:
                return "aag02uf AS nomeEstado, aag0201nome AS nomeMunic, aag0201id AS idMunic, ";
                break;
            case 3:
                return "aag02uf AS uf,aag02nome AS nomeEstado,aag02id AS idEstado, ";
                break;
            default:
                return "abm01id,abm01codigo,abm01descr,abm01tipo,aam06codigo, "
        }
    }





}
//meta-sis-eyJkZXNjciI6IlNSRiBSZXN1bW8gRG9jdW1lbnRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBEb2N1bWVudG9zICIsInRpcG8iOiJyZWxhdG9yaW8ifQ==