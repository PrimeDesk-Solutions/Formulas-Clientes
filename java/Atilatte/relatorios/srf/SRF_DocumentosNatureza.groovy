package Atilatte.relatorios.srf


import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SRF_DocumentosNatureza extends RelatorioBase {

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
        def criterios = getListLong("criterios");
        def numeroInicial = getInteger("numeroInicial");
        def numeroFinal = getInteger("numeroFinal");
        def dataEmissao = getIntervaloDatas("dataEmissao");
        def dataEntSai = getIntervaloDatas("dataEntSai");
        def resumoOperacao = getInteger("resumoOperacao");
        def criteriosCfop = getListLong("cfop");
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
        def devolucoes = getBoolean("devolucao");
        List<Long> idNaturezas = getListLong("naturezas");

        if(camposLivre1 != null && camposFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if(camposLivre2 != null && camposFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if(camposLivre3 != null && camposFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if(camposLivre4 != null && camposFixo4 != null) interromper("Selecione apenas 1 valor por campo!")

        adicionarParametro("totalizar1", totalizar1)
        adicionarParametro("totalizar2", totalizar2)
        adicionarParametro("totalizar3", totalizar3)
        adicionarParametro("totalizar4", totalizar4)

        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        if (resumoOperacao.equals(1)) {
            params.put("TITULO_RELATORIO", "Documentos Natureza - Faturamento");
        } else {
            params.put("TITULO_RELATORIO", "Documentos Natureza - Recebimento");
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


        def dados = obterDadosRelatorio(idEntidade, idTipoDocumento, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, nomeEntidade,criteriosPcd, estados, municipios, idNaturezas);

        if(dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<Long> idsItensDoc = obterIdsItensDoc(idEntidade, idTipoDocumento, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, nomeEntidade,criteriosPcd, estados, municipios, idNaturezas);
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
        if(devolucoes){
            listDevolucoesGeral = obterDevolucao(idsItensDoc);
            if(listDevolucoesGeral != null && listDevolucoesGeral.size() > 0 ){
                for(devolucao in listDevolucoesGeral){
                    devolucao.putAll(devolucao.getTableMap("eaa0103json"));
                    devolucao.remove("eaa0103json");
                    if(idControleDevolucao == null){
                        dadosTmpDev.putAll(devolucao);
                        idControleDevolucao = devolucao.getLong("eaa01033itemdoc");
                        somarValores(devolucao, valoresTotaisDevolucao, campos);
                    }else if (idControleDevolucao == devolucao.getLong("eaa01033itemdoc")){
                        somarValores(devolucao, valoresTotaisDevolucao, campos);
                    }else{
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
                if(!tmpDev.isEmpty()) listDevolucoesAjustado.add(tmpDev);
            }
        }

        for(dado in dados){
            Long idItem = dado.getLong("eaa0103id");
            for(devolucao in listDevolucoesAjustado){
                Long idItemDev = devolucao.getLong("eaa01033itemdoc")
                if (idItem == idItemDev){
                    comporDevolucoes(dado, devolucao, campos);
                }
            }

            if (idControle == null){
                dadosTmp.putAll(dado);
                idControle = dado.getLong("eaa01id");
                somarValores(dado, valoresTotais, campos );
            }else if (idControle == dado.getLong("eaa01id")){
                somarValores(dado, valoresTotais, campos );
            }else{
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
        comporValores(tmp,campos);

        dadosRelatorio.add(tmp)

        if(imprimir == 1 )return gerarXLSX(dadosRelatorio);
        return gerarPDF("SRF_DocumentosNatureza", dadosRelatorio);
    }

    private List<TableMap> obterDadosRelatorio (List<Long> idEntidade, List<Long> idTipoDocumento, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial,
                                                Integer numeroFinal, Integer resumoOperacao, Integer nomeEntidade, List<Long>pcd, List<Long> estados, List<Long> municipios, List<Long> idNaturezas )  {

        def whereData = ""
        def data = "";
        if (dataEmissao != null) {
            data = " abb01data";
            whereData = dataEmissao[0] != null && dataEmissao[1] != null ? " and abb01data >= '" + dataEmissao[0] + "' and abb01data <= '" + dataEmissao[1] + "'": "";
        } else {
            if (dataEntSai != null) {
                data = " eaa01esData";
                whereData = dataEntSai[0] != null && dataEntSai[1] != null ? " and eaa01esData >= '" + dataEntSai[0] + "' and eaa01esData <= '" + dataEntSai[1] + "'": "";
            }
        }

        def entidade = "";
        def groupBy = "";
        if (nomeEntidade.equals(0)) {
            entidade = " abe01.abe01na as nomeEntidade ";
        } else {
            entidade = " abe01nome as nomeEntidade ";
        }

        def whereResumoOperacao = null;
        if (resumoOperacao.equals(0)) {
            whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_ENTRADA;
        } else {
            whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_SAIDA;
        }

        def whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01num >= '" + numeroInicial + "' and abb01num <= '" + numeroFinal + "'": "";
        def whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01id IN (:idTipoDocumento) ": "";
        def whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01id IN (:idEntidade) ": "";
        def orderBy = " order by eaa01id, abb01num desc "
        def whereCriterioPcd = pcd != null && pcd.size() > 0 ? " and eaa01.eaa01pcd IN (:pcd) " : "";
        def whereEstados = estados != null && estados.size() > 0 ? " and aag02id in (:estados) " : "";
        def whereMunicipios = municipios != null && municipios.size() > 0 ? " and aag0201id in (:municipios) " : "";
        def whereNaturezas = idNaturezas != null && idNaturezas.size() > 0 ? " AND abf10id IN (:idNaturezas) " : "";
        def whereEmpresa = "AND eaa01gc = :idEmpresa ";

        def parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? criarParametroSql("idEntidade", idEntidade) : null;
        def parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? criarParametroSql("idTipoDocumento", idTipoDocumento) : null;
        def parametroCriteriosPcd = pcd != null && pcd.size() > 0 ? criarParametroSql("pcd", pcd) : null;
        def parametroEstados = estados != null && estados.size() > 0 ? Parametro.criar("estados",estados) : null;
        def parametroMunicipios = municipios != null && municipios.size() > 0 ? Parametro.criar("municipios",municipios) : null;
        def parametroNatureza = idNaturezas != null && idNaturezas.size() > 0 ? Parametro.criar("idNaturezas", idNaturezas) : null;
        def parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());


        def sql = " select abf10codigo, abf10nome,eaa01id, eaa0103id, abd01codigo, abd01descr,abb01num, abb01data,eaa01id, eaa01esdata, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, " +
                "        eaa0103qtUso as eaa0103qtuso, eaa0103qtComl as eaa0103qtcoml,  eaa0103unit as eaa0103unit,"+
                " 	     eaa0103total as eaa0103total, eaa0103totDoc as eaa0103totdoc, eaa0103totFinanc as eaa0103totfinanc, eaa0103json,  " + entidade +
                "        from eaa0103 " +
                "        left join eaa01 on eaa01id = eaa0103doc " +
                "	     left join abd01 on abd01id = eaa01pcd  							"+
                "        left join abb01 on abb01id = eaa01central " +
                "        left join aah01 on aah01id = abb01tipo " +
                "        left join abe01 on abe01id = abb01ent " +
                "        left join abe30 on abe30id = eaa01cp  " +
                "        left join abe40 on abe40id = eaa01tp  " +
                "        left join abm01 on abm01id = eaa0103item " +
                "	     left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
                "        left join aag0201 on aag0201id = abe0101municipio "+
                "        left join aag02 on aag02id = aag0201uf " +
                "        LEFT JOIN eaa01039 ON eaa01039item = eaa0103id  " +
                "        LEFT JOIN eaa010391 ON eaa010391depto = eaa01039id  " +
                "        LEFT JOIN abb11 ON abb11id = eaa01039depto  " +
                "        LEFT JOIN abf10 ON abf10id = eaa010391nat  " +
                "        where eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                "        and eaa01cancData is null "+
                "        and eaa01nfestat <> 5 "+
                whereEmpresa +
                whereNumero +
                whereTipoDocumento +
                whereIdEntidade +
                whereData +
                whereResumoOperacao +
                whereCriterioPcd+
                whereEstados +
                whereMunicipios +
                whereNaturezas +
                orderBy;

        def dadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento, parametroCriteriosPcd,parametroEstados,parametroMunicipios, parametroEmpresa, parametroNatureza);

        for(dados in dadosRelatorio){
            dados.putAll(dados.getTableMap("eaa0103json"))
            dados.remove("eaa0103json")
        }

        return dadosRelatorio;
    }

    private List<Long> obterIdsItensDoc (List<Long> idEntidade, List<Long> idTipoDocumento, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial,
                                         Integer numeroFinal, Integer resumoOperacao, Integer nomeEntidade, List<Long>pcd, List<Long> estados, List<Long> municipios,List<Long> idNaturezas )  {

        def whereData = ""
        if (dataEmissao != null) {
            whereData = dataEmissao[0] != null && dataEmissao[1] != null ? " and abb01data >= '" + dataEmissao[0] + "' and abb01data <= '" + dataEmissao[1] + "'": "";
        } else {
            if (dataEntSai != null) {
                whereData = dataEntSai[0] != null && dataEntSai[1] != null ? " and eaa01esData >= '" + dataEntSai[0] + "' and eaa01esData <= '" + dataEntSai[1] + "'": "";
            }
        }


        def whereResumoOperacao = null;
        if (resumoOperacao.equals(0)) {
            whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_ENTRADA;
        } else {
            whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_SAIDA;
        }

        def whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01num >= '" + numeroInicial + "' and abb01num <= '" + numeroFinal + "'": "";
        def whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01id IN (:idTipoDocumento) ": "";
        def whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01id IN (:idEntidade) ": "";
        def whereCriterioPcd = pcd != null && pcd.size() > 0 ? " and eaa01.eaa01pcd IN (:pcd) " : "";
        def whereEstados = estados != null && estados.size() > 0 ? "and aag02id in (:estados) " : "";
        def whereMunicipios = municipios != null && municipios.size() > 0 ? " and aag0201id in (:municipios) " : "";
        def whereNaturezas = idNaturezas != null && idNaturezas.size() > 0 ? " AND abf10id IN (:idNaturezas) " : "";
        def whereEmpresa = "AND eaa01gc = :idEmpresa ";

        def parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? criarParametroSql("idEntidade", idEntidade) : null;
        def parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? criarParametroSql("idTipoDocumento", idTipoDocumento) : null;
        def parametroCriteriosPcd = pcd != null && pcd.size() > 0 ? criarParametroSql("pcd", pcd) : null;
        def parametroEstados = estados != null && estados.size() > 0 ? Parametro.criar("estados",estados) : null;
        def parametroMunicipios = municipios != null && municipios.size() > 0 ? Parametro.criar("municipios",municipios) : null;
        def parametroNatureza = idNaturezas != null && idNaturezas.size() > 0 ? Parametro.criar("idNaturezas", idNaturezas) : null;
        def parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());


        def sql = "      select distinct eaa0103id "+
                "        from eaa0103  " +
                "        left join eaa01 on eaa01id = eaa0103doc " +
                "	     left join abd01 on abd01id = eaa01pcd  							"+
                "        left join abb01 on abb01id = eaa01central " +
                "        left join aah01 on aah01id = abb01tipo " +
                "        left join abe01 on abe01id = abb01ent " +
                "        left join abe30 on abe30id = eaa01cp  " +
                "        left join abe40 on abe40id = eaa01tp  " +
                "        left join abm01 on abm01id = eaa0103item " +
                "	     left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
                "        left join aag0201 on aag0201id = abe0101municipio "+
                "        left join aag02 on aag02id = aag0201uf " +
                "        LEFT JOIN eaa01039 ON eaa01039item = eaa0103id  " +
                "        LEFT JOIN eaa010391 ON eaa010391depto = eaa01039id  " +
                "        LEFT JOIN abb11 ON abb11id = eaa01039depto  " +
                "        LEFT JOIN abf10 ON abf10id = eaa010391nat  " +
                "        where eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                "        and eaa01cancData is null "+
                "        and eaa01nfestat <> 5 "+
                whereEmpresa +
                whereNumero +
                whereTipoDocumento +
                whereIdEntidade +
                whereData +
                whereResumoOperacao +
                whereCriterioPcd+
                whereEstados +
                whereNaturezas +
                whereMunicipios

        return getAcessoAoBanco().obterListaDeLong(sql, parametroEntidade, parametroTipoDocumento, parametroCriteriosPcd,parametroEstados,parametroMunicipios, parametroEmpresa, parametroNatureza);
    }

    private void somarValores(TableMap valoresDocumento, TableMap valoresTotais, Map<String, String> campos){
        for (campo in campos){
            if(campo != null){
                String nomeCampo = buscarNomeCampoFixo(campo.value.toString()) != null ? buscarNomeCampoFixo(campo.value.toString()) : campo.value.toString();
                if(valoresDocumento.getBigDecimal(nomeCampo) != null){
                    if(valoresTotais.getBigDecimal(nomeCampo) == null){
                        valoresTotais.put(nomeCampo, valoresDocumento.getBigDecimal_Zero(nomeCampo));
                    }else{
                        BigDecimal valorTotal = valoresTotais.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresTotais.getBigDecimal(nomeCampo);
                        BigDecimal valorItem = valoresDocumento.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresDocumento.getBigDecimal(nomeCampo);
                        BigDecimal soma = valorTotal + valorItem;
                        valoresTotais.put(nomeCampo, soma)
                    }
                }
            }
        }

    }

    private List<TableMap> obterDevolucao(List<Long> idsItem){
        def whereItem = " where eaa01033itemdoc in (:item) "

        def sql = " select eaa01033itemdoc, eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103totfinanc, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json "+
                " from eaa01033 "+
                " inner join eaa0103 on eaa0103id = eaa01033item  "+
                whereItem +
                " and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "+
                " ORDER BY eaa01033itemdoc ";

        def p1 = criarParametroSql("item", idsItem)
        return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
    }

    private void comporDevolucoes(TableMap valores, TableMap devolucao, Map<String, String> campos){
        for (campo in campos){
            if(campo != null){
                String nomeCampo = buscarNomeCampoFixo(campo.value.toString()) != null ? buscarNomeCampoFixo(campo.value.toString()) : campo.value.toString();
                if(valores.getBigDecimal(nomeCampo) != null){
                    BigDecimal valor = valores.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valores.getBigDecimal(nomeCampo);
                    BigDecimal valorDevolucao = devolucao.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : devolucao.getBigDecimal(nomeCampo);
                    valores.put(nomeCampo, valor - valorDevolucao);
                }
            }
        }
    }

    private void comporValores(TableMap dado, HashMap<String, String> campos){
        for(campo in campos){
            if(campo.value != null){
                String nomeCampo = buscarNomeCampoFixo(campo.value);
                if(nomeCampo != null){
                    dado.put("nomeCampo"+campo.key, campo.value);
                    dado.put("valorCampo"+campo.key, dado.getBigDecimal_Zero(nomeCampo))
                }else{
                    nomeCampo = buscarNomeCampoLivre(campo.value);
                    dado.put("nomeCampo"+campo.key, nomeCampo);
                    dado.put("valorCampo"+campo.key, dado.getBigDecimal_Zero(campo.value) );
                }
            }
        }
    }

    public String buscarNomeCampoFixo(String campo) {
        switch(campo) {
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
        def sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

    }

    @Override
    public String getNomeTarefa() {
        return "SRF - Documentos";
    }
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgZSBOYXR1cmV6YXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=