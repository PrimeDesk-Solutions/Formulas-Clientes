package Atilatte.relatorios.sif

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import java.time.LocalDate
import java.time.format.DateTimeFormatter;


public class SRF_Devolucoes_Por_Categoria_Estado_SIF extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Devoluções por Categoria/Estado (SIF)"
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
        filtrosDefault.put("impressao", "0");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        List<Long> idEstados = getListLong("estados");
        List<Long> idItens = getListLong("itens");
        List<Long> idCategoria = getListLong("categoria");
        List<Long> tipoDoc = getListLong("tipoDoc");
        Integer impressao = getInteger("impressao");
        Boolean impressaoQuilo = getBoolean("imprimeQuilo");
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

        String periodo = ""
        if (dataEmissao != null) {
            periodo = "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("TOTALIZAR1", totalizar1);
        params.put("TOTALIZAR2", totalizar2);
        params.put("TOTALIZAR3", totalizar3);
        params.put("TOTALIZAR4", totalizar4);
        params.put("TOTALIZAR5", totalizar5);
        params.put("TOTALIZAR6", totalizar6);
        params.put("TITULO", "SRF - Devoluções por Categoria/Estado (SIF)");
        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        params.put("PERIODO", periodo);

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

        List<TableMap> dados = buscarDadosRelatorio(dataEmissao, idEstados, idItens, idCategoria, tipoDoc);
        List<TableMap> dadosRelatorio = new ArrayList<>();

        TableMap valoresTotais = new TableMap();
        TableMap dadosTmp = new TableMap();
        String idcontrole = null;

        for(dado in dados){
            String categoria = dado.getString("categoria");
            BigDecimal fatorQuilo = dado.getBigDecimal_Zero("fatorQuilo");
            String codItem = dado.getString("codItem");
            String naItem = dado.getString("naItem");
            String uf = dado.getString("uf");

            if(categoria == null) interromper("O item " + codItem + " - " + naItem + " encontra-se sem o critério de seleção SIF");
            if(fatorQuilo == 0) interromper("O item " + codItem + " - " + naItem + " encontra-se sem um fator de conversão para quilo");

            dado.putAll(dado.getTableMap("eaa0103json"));
            dado.remove("eaa0103json");

            if(idcontrole == null){
                dadosTmp.putAll(dado);
                idcontrole = dado.getString("categoria") + dado.getString("uf");
                somarValores(dado, valoresTotais, campos);
            }else if(dado.getString("categoria") + dado.getString("uf") == idcontrole){
                somarValores(dado, valoresTotais, campos);
            }else{
                TableMap tmp = new TableMap();
                tmp.putAll(dadosTmp);
                tmp.putAll(valoresTotais);
                comporValores(tmp, campos, fatorQuilo, impressaoQuilo);
                dadosRelatorio.add(tmp);

                dadosTmp = new TableMap();
                dadosTmp.putAll(dado);
                valoresTotais = new TableMap();
                idcontrole = dado.getString("categoria") + dado.getString("uf");
                somarValores(dado, valoresTotais, campos);
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);
        comporValores(tmp, campos, tmp.getBigDecimal_Zero("fatorQuilo"), impressaoQuilo);
        dadosRelatorio.add(tmp)

        if(impressao == 1) return gerarXLSX("SRF_Devolucoes_Por_Categoria_Estado_SIF_Excel", dadosRelatorio);

        return gerarPDF("SRF_Devolucoes_Por_Categoria_Estado_SIF_PDF", dadosRelatorio);
    }

    private List<TableMap> buscarDadosRelatorio(LocalDate[] dataEmissao, List<Long> idEstados, List<Long> idItens, List<Long> idCategoria, List<Long> tipoDoc){
        String whereCriterio = "WHERE aba30nome = 'SIF' ";
        String whereClasDoc = "AND eaa01clasDoc = 1 ";
        String whereEsMov = "AND eaa01esMov = 0 ";
        String whereDtEmissao = dataEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereItens = idItens != null && idItens.size() > 0 ? "AND abm01id IN (:idItens) " : "";
        String whereEstados = idEstados != null && idEstados.size() > 0 ? "AND aag02id IN (:idEstados) " : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? "AND aah01id IN (:tipoDoc) " : "";
        String whereCategoria = idCategoria != null && idCategoria.size() > 0 ? "AND aba3001id IN (:idCategoria) " : "";
        String whereCancData = "AND eaa01cancData IS NULL ";
        String whereMovEst = "AND eaa01iSce = 0 AND abd01isce = 0 ";
        String orderBy = "ORDER BY aba3001descr, aag02uf ";

        Parametro parametroDtEmissaoIni = dataEmissao != null ? Parametro.criar("dtEmissaoIni", dataEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dataEmissao != null ? Parametro.criar("dtEmissaoFin", dataEmissao[1]) : null;
        Parametro parametroItens = idItens != null && idItens.size() > 0 ? Parametro.criar("idItens", idItens) : null;
        Parametro parametroEstados = idEstados != null && idEstados.size() > 0 ? Parametro.criar("idEstados", idEstados) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;
        Parametro parametroCategoria = idCategoria != null && idCategoria.size() > 0 ? Parametro.criar("idCategoria", idCategoria) : null;

        String sql = "SELECT abm01codigo AS codItem, abm01na AS naItem, aba3001descr AS categoria, aag02uf AS uf, " +
                    "CAST(abm0101json ->> 'fator_litro' AS numeric(18,6)) as fatorQuilo, " +
                    "eaa0103qtcoml, eaa0103total, eaa0103unit, eaa0103qtuso, eaa0103json, eaa0103totdoc, eaa0103totfinanc " +
                    "FROM eaa01 " +
                    "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                    "INNER JOIN abm01 ON abm01id = eaa0103item " +
                    "INNER JOIN abm0101 ON abm0101item = abm01id " +
                    "INNER JOIN abb01 ON abb01id = eaa01central " +
                    "INNER JOIN abe01 ON abe01id = abb01ent " +
                    "LEFT  JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                    "LEFT JOIN aag0201 ON aag0201id = abe0101municipio " +
                    "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                    "LEFT JOIN abm0102 ON abm0102item = abm01id " +
                    "LEFT JOIN aba3001 ON aba3001id = abm0102criterio " +
                    "LEFT JOIN aba30 ON aba3001criterio = aba30id " +
                    "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                    "INNER JOIN aah01 ON aah01id = abb01tipo " +
                    whereCriterio +
                    whereClasDoc +
                    whereEsMov +
                    whereDtEmissao +
                    whereItens +
                    whereEstados +
                    whereTipoDoc +
                    whereCategoria +
                    whereCancData +
                    whereMovEst +
                    orderBy

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroItens, parametroEstados, parametroTipoDoc, parametroCategoria);
    }
    private void somarValores(TableMap valoresDocumento, TableMap valoresTotais, Map<String, String> campos){
        for(campo in campos){
            if(campo != null){
                String nomeCampo = buscarNomeCampoFixo(campo.value) != null ? buscarNomeCampoFixo(campo.value) : campo.value;
                if(valoresDocumento.getBigDecimal(nomeCampo) != null){
                    if(valoresTotais.getBigDecimal(nomeCampo) == null){
                        valoresTotais.put(nomeCampo, valoresDocumento.getBigDecimal(nomeCampo));
                    }else{
                        BigDecimal valorTotal = valoresTotais.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresTotais.getBigDecimal(nomeCampo);
                        BigDecimal valorItem = valoresDocumento.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresDocumento.getBigDecimal(nomeCampo);
                        BigDecimal soma = valorTotal + valorItem;
                        valoresTotais.put(nomeCampo, soma);
                    }
                }
            }
        }
    }
    private void comporValores(TableMap dado, HashMap<String, String> campos, BigDecimal fatorQuilo, Boolean impressaoQuilo){
        for(campo in campos){
            if(campo.value != null){
                String nomeCampo = buscarNomeCampoFixo(campo.value);
                if(nomeCampo != null){
                    dado.put("nomeCampo" + campo.key, campo.value);
                    if(impressaoQuilo){
                        dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(nomeCampo) * fatorQuilo);
                    }else{
                        dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(nomeCampo))
                    }
                }else{
                    nomeCampo = buscarNomeCampoLivre(campo.value);
                    dado.put("nomeCampo" + campo.key, nomeCampo);

                    if(impressaoQuilo){
                        dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value) * fatorQuilo)
                    }else{
                        dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value))
                    }
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
//meta-sis-eyJkZXNjciI6IlNSRiAtIERldm9sdcOnw7VlcyBwb3IgQ2F0ZWdvcmlhL0VzdGFkbyAoU0lGKSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==