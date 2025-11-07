package Atilatte.relatorios.srf;


import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SRF_Documentos extends RelatorioBase {

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("resumoOperacao", "0");
        filtrosDefault.put("nomeEntidade", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("total1", true)
        filtrosDefault.put("total2", true)
        filtrosDefault.put("total3", true)
        filtrosDefault.put("total4", true)
        filtrosDefault.put("devolucao", true)
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        def idEntidade = getListLong("entidade");
        def idTipoDocumento = getListLong("tipo");
        def numeroInicial = getInteger("numeroInicial") == null || getInteger("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial");
        def numeroFinal = get("numeroFinal") == null || get("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal");
        def dataEmissao = getIntervaloDatas("dataEmissao");
        def dataEntSai = getIntervaloDatas("dataEntSai");
        def resumoOperacao = getInteger("resumoOperacao");
        def criteriosPcd = getListLong("pcd");
        def estados = getListLong("estados");
        def municipios = getListLong("municipios");
        def nomeEntidade = getInteger("nomeEntidade");
        def imprimir = getInteger("impressao")
        def camposLivre1 = getString("campoLivre1")
        def camposFixo1 = getString("campoFixo1")
        def camposLivre2 = getString("campoLivre2")
        def camposFixo2 = getString("campoFixo2")
        def camposLivre3 = getString("campoLivre3")
        def camposFixo3 = getString("campoFixo3")
        def camposLivre4 = getString("campoLivre4")
        def camposFixo4 = getString("campoFixo4")
        def totalizar1 = getBoolean("total1")
        def totalizar2 = getBoolean("total2")
        def totalizar3 = getBoolean("total3")
        def totalizar4 = getBoolean("total4")
        def devolucoes = getBoolean("devolucao")

        if (camposLivre1 != null && camposFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if (camposLivre2 != null && camposFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if (camposLivre3 != null && camposFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if (camposLivre4 != null && camposFixo4 != null) interromper("Selecione apenas 1 valor por campo!")

        adicionarParametro("totalizar1", totalizar1)
        adicionarParametro("totalizar2", totalizar2)
        adicionarParametro("totalizar3", totalizar3)
        adicionarParametro("totalizar4", totalizar4)

        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        if (resumoOperacao.equals(1)) {
            params.put("TITULO_RELATORIO", "Documentos - Faturamento");
        } else {
            params.put("TITULO_RELATORIO", "Documentos - Recebimento");
        }

        if (dataEmissao != null) {
            params.put("PERIODO", "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        if (dataEntSai != null) {
            params.put("PERIODO", "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        HashMap<String, String> campos = new HashMap();

        campos.put("1", camposLivre1 != null ? camposLivre1 : camposFixo1 != null ? camposFixo1 : null);
        campos.put("2", camposLivre2 != null ? camposLivre2 : camposFixo2 != null ? camposFixo2 : null);
        campos.put("3", camposLivre3 != null ? camposLivre3 : camposFixo3 != null ? camposFixo3 : null);
        campos.put("4", camposLivre4 != null ? camposLivre4 : camposFixo4 != null ? camposFixo4 : null);


        def dados = obterDadosRelatorio(idEntidade, idTipoDocumento, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, nomeEntidade, criteriosPcd, estados, municipios, camposLivre1, camposLivre2, camposLivre3, camposLivre4);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<Long> idsItensDoc = obterIdsItensDoc(idEntidade, idTipoDocumento, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, nomeEntidade, criteriosPcd, estados, municipios, camposLivre1, camposLivre2, camposLivre3, camposLivre4);
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
            for (devolucao in listDevolucoesAjustado) {
                Long idItemDev = devolucao.getLong("eaa01033itemdoc")
                if (idItem == idItemDev) {
                    comporDevolucoes(dado, devolucao, campos);
                }
            }

            if (idControle == null) {
                dadosTmp.putAll(dado);
                idControle = dado.getLong("eaa01id");
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

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);
        comporValores(tmp, campos);

        dadosRelatorio.add(tmp)

        if (imprimir == 1) return gerarXLSX(dadosRelatorio);
        return gerarPDF("SRF_Documentos", dadosRelatorio);
    }

    private List<TableMap> obterDadosRelatorio(List<Long> idEntidade, List<Long> idTipoDocumento, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial,
                                               Integer numeroFinal, Integer resumoOperacao, Integer nomeEntidade, List<Long> pcd, List<Long> estados, List<Long> municipios, String camposLivre1, String camposLivre2, String camposLivre3, String camposLivre4) {

        def whereData = ""
        def data = "";
        if (dataEmissao != null) {
            data = " abb01data";
            whereData = dataEmissao[0] != null && dataEmissao[1] != null ? " AND abb01data >= '" + dataEmissao[0] + "' AND abb01data <= '" + dataEmissao[1] + "'" : "";
        } else {
            if (dataEntSai != null) {
                data = " eaa01esData";
                whereData = dataEntSai[0] != null && dataEntSai[1] != null ? " AND eaa01esData >= '" + dataEntSai[0] + "' AND eaa01esData <= '" + dataEntSai[1] + "'" : "";
            }
        }

        def entidade = "";
        def groupBy = "";
        if (nomeEntidade.equals(0)) {
            entidade = " abe01.abe01na AS nomeEntidade ";
        } else {
            entidade = " abe01nome AS nomeEntidade ";
        }

        def whereResumoOperacao = null;
        if (resumoOperacao.equals(0)) {
            whereResumoOperacao = " AND eaa01esMov = " + Eaa01.ESMOV_ENTRADA;
        } else {
            whereResumoOperacao = " AND eaa01esMov = " + Eaa01.ESMOV_SAIDA;
        }

        def whereNumero = numeroInicial != null && numeroFinal != null ? " AND abb01num >= '" + numeroInicial + "' AND abb01num <= '" + numeroFinal + "' " : "";
        def whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " AND aah01id IN (:idTipoDocumento) " : "";
        def whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " AND abe01id IN (:idEntidade) " : "";
        def orderBy = " ORDER BY eaa01id, abb01num DESC "
        def whereCriterioPcd = pcd != null && pcd.size() > 0 ? " AND eaa01.eaa01pcd IN (:pcd) " : "";
        def whereEstados = estados != null && estados.size() > 0 ? "AND aag02id IN (:estados) " : "";
        def whereMunicipios = municipios != null && municipios.size() > 0 ? "AND aag0201id IN (:municipios) " : "";
        def whereEmpresa = "AND eaa01gc = :idEmpresa ";

        def parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? criarParametroSql("idEntidade", idEntidade) : null;
        def parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? criarParametroSql("idTipoDocumento", idTipoDocumento) : null;
        def parametroCriteriosPcd = pcd != null && pcd.size() > 0 ? criarParametroSql("pcd", pcd) : null;
        def parametroEstados = estados != null && estados.size() > 0 ? Parametro.criar("estados", estados) : null;
        def parametroMunicipios = municipios != null && municipios.size() > 0 ? Parametro.criar("municipios", municipios) : null;
        def parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());


        def sql = " SELECT eaa01id, eaa0103id, abd01codigo, abd01descr,abb01num, abb01data,eaa01id, eaa01esdata, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, " +
                    "eaa0103qtUso as eaa0103qtuso, eaa0103qtComl as eaa0103qtcoml,  eaa0103unit as eaa0103unit, " +
                    "eaa0103total as eaa0103total, eaa0103totDoc as eaa0103totdoc, eaa0103totFinanc as eaa0103totfinanc, eaa0103json,  " + entidade +
                    "FROM eaa0103 " +
                    "INNER JOIN eaa01 ON eaa01id = eaa0103doc " +
                    "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                    "INNER JOIN abb01 ON abb01id = eaa01central " +
                    "INNER JOIN aah01 ON aah01id = abb01tipo " +
                    "INNER JOIN abe01 ON abe01id = abb01ent " +
                    "LEFT JOIN abe30 ON abe30id = eaa01cp  " +
                    "LEFT JOIN abe40 ON abe40id = eaa01tp  " +
                    "INNER JOIN abm01 ON abm01id = eaa0103item " +
                    "LEFT JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                    "LEFT JOIN aag0201 ON aag0201id = abe0101municipio " +
                    "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                    "WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF + " " +
                    "AND eaa01cancData IS NULL " +
                    "AND eaa01nfestat <> 5 " +
                    whereEmpresa +
                    whereNumero +
                    whereTipoDocumento +
                    whereIdEntidade +
                    whereData +
                    whereResumoOperacao +
                    whereCriterioPcd +
                    whereEstados +
                    whereMunicipios +
                    orderBy;

        def dadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento, parametroCriteriosPcd, parametroEstados, parametroMunicipios, parametroEmpresa);

        for (dados in dadosRelatorio) {
            dados.putAll(dados.getTableMap("eaa0103json"))
            dados.remove("eaa0103json")
        }

        return dadosRelatorio;
    }

    private List<Long> obterIdsItensDoc(List<Long> idEntidade, List<Long> idTipoDocumento, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial,
                                        Integer numeroFinal, Integer resumoOperacao, Integer nomeEntidade, List<Long> pcd, List<Long> estados, List<Long> municipios, String camposLivre1, String camposLivre2, String camposLivre3, String camposLivre4) {

        def whereData = ""
        if (dataEmissao != null) {
            whereData = dataEmissao[0] != null && dataEmissao[1] != null ? " AND abb01data >= '" + dataEmissao[0] + "' AND abb01data <= '" + dataEmissao[1] + "'" : "";
        } else {
            if (dataEntSai != null) {
                whereData = dataEntSai[0] != null && dataEntSai[1] != null ? " AND eaa01esData >= '" + dataEntSai[0] + "' AND eaa01esData <= '" + dataEntSai[1] + "'" : "";
            }
        }


        def whereResumoOperacao = null;
        if (resumoOperacao.equals(0)) {
            whereResumoOperacao = " AND eaa01esMov = " + Eaa01.ESMOV_ENTRADA;
        } else {
            whereResumoOperacao = " AND eaa01esMov = " + Eaa01.ESMOV_SAIDA;
        }

        def whereNumero = numeroInicial != null && numeroFinal != null ? " AND abb01num >= '" + numeroInicial + "' AND abb01num <= '" + numeroFinal + "'" : "";
        def whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " AND aah01id IN (:idTipoDocumento) " : "";
        def whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " AND abe01id IN (:idEntidade) " : "";
        def whereCriterioPcd = pcd != null && pcd.size() > 0 ? " AND eaa01.eaa01pcd IN (:pcd) " : "";
        def whereEstados = estados != null && estados.size() > 0 ? "AND aag02id IN (:estados) " : "";
        def whereMunicipios = municipios != null && municipios.size() > 0 ? "AND aag0201id IN (:municipios) " : "";
        def whereEmpresa = "AND eaa01gc = :idEmpresa ";

        def parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? criarParametroSql("idEntidade", idEntidade) : null;
        def parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? criarParametroSql("idTipoDocumento", idTipoDocumento) : null;
        def parametroCriteriosPcd = pcd != null && pcd.size() > 0 ? criarParametroSql("pcd", pcd) : null;
        def parametroEstados = estados != null && estados.size() > 0 ? Parametro.criar("estados", estados) : null;
        def parametroMunicipios = municipios != null && municipios.size() > 0 ? Parametro.criar("municipios", municipios) : null;
        def parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());


        def sql = "SELECT DISTINCT eaa0103id " +
                    "FROM eaa0103  " +
                    "INNER JOIN eaa01 ON eaa01id = eaa0103doc " +
                    "INNER JOIN abd01 ON abd01id = eaa01pcd  " +
                    "INNER JOIN abb01 ON abb01id = eaa01central " +
                    "INNER JOIN aah01 ON aah01id = abb01tipo " +
                    "INNER JOIN abe01 ON abe01id = abb01ent " +
                    "LEFT JOIN abe30 ON abe30id = eaa01cp  " +
                    "LEFT JOIN abe40 ON abe40id = eaa01tp  " +
                    "INNER JOIN abm01 ON abm01id = eaa0103item " +
                    "LEFT JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                    "LEFT JOIN aag0201 ON aag0201id = abe0101municipio " +
                    "LEFT JOIN aag02 on aag02id = aag0201uf " +
                    "WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF + " "+
                    "AND eaa01cancData IS NULL " +
                    "AND eaa01nfestat <> 5 " +
                    whereEmpresa +
                    whereNumero +
                    whereTipoDocumento +
                    whereIdEntidade +
                    whereData +
                    whereResumoOperacao +
                    whereCriterioPcd +
                    whereEstados +
                    whereMunicipios

        return getAcessoAoBanco().obterListaDeLong(sql, parametroEntidade, parametroTipoDocumento, parametroCriteriosPcd, parametroEstados, parametroMunicipios, parametroEmpresa);
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

        def sql = " SELECT eaa01033itemdoc, eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103totfinanc, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json " +
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

    @Override
    public String getNomeTarefa() {
        return "SRF - Documentos";
    }
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=