package Atilatte.relatorios.srf

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SRF_DocRepresentante extends RelatorioBase{

    @Override
    public String getNomeTarefa() {
        return "SRF - Documento por Representante - El Tech";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        //filtrosDefault.put("numeroInicial", "000000001");
        //filtrosDefault.put("numeroFinal", "999999999");
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
        List<Long> idEntidade = getListLong("entidade");
        List<Long> idTipoDocumento = getListLong("tipo");
        List<Long> idCfop = getListLong("cfop")
        List<Long> idPcd = getListLong("pcd")
        //Integer numeroInicial = getInteger("numeroInicial");
        //Integer numeroFinal = getInteger("numeroFinal");
        Integer numeroInicial = get("numeroInicial") == null || get("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial")
        Integer numeroFinal = get("numeroFinal") == null || get("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal")
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dataEntSai = getIntervaloDatas("dataEntSai");
        Integer resumoOperacao = getInteger("resumoOperacao");
        Integer nomeEntidade = getInteger("nomeEntidade");
        Integer imprimir = getInteger("impressao")
        List<Long> representante = getListLong("representante")
        String camposLivre1 = get("campoLivre1")
        String camposLivre2 = get("campoLivre2")
        String camposLivre3 = get("campoLivre3")
        String camposLivre4 = get("campoLivre4")
        String camposFixo1 = get("campoFixo1")
        String camposFixo2 = get("campoFixo2")
        String camposFixo3 = get("campoFixo3")
        String camposFixo4 = get("campoFixo4")
        Boolean totalizar1 = getBoolean("total1")
        Boolean totalizar2 = getBoolean("total2")
        Boolean totalizar3 = getBoolean("total3")
        Boolean totalizar4 = getBoolean("total4")
        Boolean devolucoes = getBoolean("devolucao")


        if(camposLivre1 != null && camposFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if(camposLivre2 != null && camposFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if(camposLivre3 != null && camposFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if(camposLivre4 != null && camposFixo4 != null) interromper("Selecione apenas 1 valor por campo!")


        Map<String,String> campos = new HashMap()
        campos.put("1", camposLivre1 != null ? camposLivre1 : camposFixo1 != null ? camposFixo1 : null   )
        campos.put("2", camposLivre2 != null ? camposLivre2 : camposFixo2 != null ? camposFixo2 : null   )
        campos.put("3", camposLivre3 != null ? camposLivre3 : camposFixo3 != null ? camposFixo3 : null   )
        campos.put("4", camposLivre4 != null ? camposLivre4 : camposFixo4 != null ? camposFixo4 : null   )

        String periodo = ""
        if(dataEmissao != null) {
            periodo = "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }else if(dataEntSai) {
            periodo = "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("empressa", obterEmpresaAtiva().getAac10rs())
        params.put("periodo",periodo)
        params.put("totalizar1", totalizar1)
        params.put("totalizar2", totalizar2)
        params.put("totalizar3", totalizar3)
        params.put("totalizar4", totalizar4)

        List<TableMap> representantes = buscarRepresentante(representante)
        List<TableMap> dadosRelatorios = new ArrayList()

        for(rep in representantes) {
            List<TableMap> dados = new ArrayList()

            comporDocumentos(dados, rep, 0, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
            comporDocumentos(dados, rep, 1, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
            comporDocumentos(dados, rep, 2, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
            comporDocumentos(dados, rep, 3, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)
            comporDocumentos(dados, rep, 4, nomeEntidade, numeroInicial, numeroFinal,dataEmissao,dataEntSai,resumoOperacao, idEntidade, idTipoDocumento, idCfop, idPcd)

            ordenar(dados,1)
            ordenar(dados,2)

            TableMap valoresTotais = new TableMap()
            TableMap dadosTmp = new TableMap()
            Long idControle = null
            List<Long> lista = new ArrayList()
            List<BigDecimal> units = new ArrayList()
            for(dado in dados) {
                units.add(dado.getBigDecimal("eaa0103unit"))
                if(devolucoes){
                    TableMap valoresDevolucaoTotais = new TableMap()
                    List<TableMap> valorDevolucoes = obterDevolucao(dado.getLong("eaa0103id"))
                    for(devolucao in valorDevolucoes) {
                        devolucao.putAll(devolucao.getTableMap("eaa0103json"))
                        devolucao.remove("eaa0103json")
                        somarValores(devolucao,valoresDevolucaoTotais,campos)
                    }
                    comporDevolucao(dado,valoresDevolucaoTotais,campos)
                }

                comporCfops(dado)
                lista.add(dado.getLong("eaa01id"))
                if(idControle == null) {
                    dadosTmp.putAll(dado)
                    idControle = dado.getLong("eaa01id")
                    somarValores(dado,valoresTotais,campos)
                }else if(idControle == dado.getLong("eaa01id")) {
                    somarValores(dado,valoresTotais,campos)
                }else {
                    TableMap tmp = new TableMap()
                    tmp.putAll(dadosTmp)
                    tmp.putAll(valoresTotais)
                    comporCampo(tmp,campos)
                    dadosRelatorios.add(tmp)

                    dadosTmp = new TableMap()
                    dadosTmp.putAll(dado)
                    valoresTotais = new TableMap()
                    idControle = dado.getLong("eaa01id")
                    somarValores(dado,valoresTotais,campos)
                }
            }
            TableMap tmp = new TableMap()
            tmp.putAll(dadosTmp)
            tmp.putAll(valoresTotais)
            comporCampo(tmp,campos)
            if(!tmp.isEmpty()) {
                dadosRelatorios.add(tmp)
            }
        }
        if(imprimir == 1) return gerarXLSX(dadosRelatorios)
        return gerarPDF(dadosRelatorios)

    }


    private List<TableMap> buscarRepresentante(List<Long> representante) {

        String whereRepresentantes = representante != null && representante.size() > 0 ? " and abe01id in (:representante) " : ""

        String sql = " select abe01id as abe01id_rep, abe01na as abe01na_rep , abe01codigo as abe01codigo_rep " +
                " from abe01 " +
                " where abe01rep = 1 " +
                obterWherePadrao("abe01", "and") +
                whereRepresentantes

        Parametro p1 = representante != null && representante.size() > 0 ? criarParametroSql("representante", representante) : null
        return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
    }

    private void comporCfops(TableMap dado) {

        String whereDoc = " where eaa0103doc = :docId "

        Parametro parametroDoc = Parametro.criar("docId", dado.getLong("eaa01id"))

        String sql = " select aaj15codigo from eaa0103 inner join aaj15 on aaj15id = eaa0103cfop " + whereDoc

        List<String> resultados = getAcessoAoBanco().obterListaDeString(sql,parametroDoc)

        List<String> cfops = new ArrayList()
        String cfop = ""
        for(resultado in resultados) {
            if(!cfops.contains(resultado)) {
                cfops.add(resultado)
                cfop += cfop == "" ?  resultado : "/"+resultado
            }
        }

        dado.put("cfops", cfop)
    }

    private comporDocumentos(List<TableMap> dados, TableMap dadosRepresentante, Integer representante, Integer nomeEntidade, Long numInicial, Long numFinal, LocalDate[] emissao, LocalDate[] entSai, Integer oper, List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> idCfop, List<Long> idPcd){

        String WhereOper = oper == 0 ? " and eaa01esMov = 0 " : " and eaa01esMov = 1 "
        String nomeEnt = nomeEntidade == 0 ? "abe01na" : "abe01nome"
        String eaa01rep = representante == 0 ? " eaa01rep0 " : representante == 1 ? " eaa01rep1 " : representante == 2 ? " eaa01rep2 " : representante == 3 ? " eaa01rep3 " : " eaa01rep4"
        String eaa01txComis = representante == 0 ? " eaa01txcomis0 " : representante == 1 ? " eaa01txcomis1 " : representante == 2 ? " eaa01txcomis2 " : representante == 3 ? " eaa01txcomis3 " : " eaa01txcomis4"
        String whereRepresentante = " and "+eaa01rep+" = " + dadosRepresentante.getString("abe01id_rep")
        String whereNumInicial = numInicial != null ?  " and abb01num >= :numInicial " : ""
        String whereNumfinal = numFinal != null ? " and abb01num <= :numFinal " : ""
        String whereEmissao = emissao != null ? " and abb01data between '"+emissao[0]+"' and '"+emissao[1]+"'":""
        String whereEntSai = entSai != null ? " and eaa01esdata between '"+entSai[0]+"' and '"+entSai[1]+"'":""
        String whereidEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01id in (:idEntidade) " : ""
        String whereidTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01id in (:idTipoDocumento) " : ""
        String whereidCfop = idCfop != null && idCfop.size() > 0 ? " and eaa0103cfop in (:idCfop) " : ""
        String whereidPcd = idPcd != null && idPcd.size() > 0 ? " and eaa01pcd in (:idPcd) " : ""

        String sql = " select eaa01id, eaa0103id,  "+eaa01txComis+" as eaa01txcomis, eaa01totdoc, aah01codigo, abb01num, abb01data, eaa01esdata, abe01codigo,"+nomeEnt+" as nomeEntidade, eaa0103json,abe30nome,  "+
                " eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103total, eaa0103totdoc, eaa0103totfinanc " +
                " from eaa0103 "+
                " inner join eaa01 on eaa01id = eaa0103doc "+
                " inner join abb01 on abb01id = eaa01central "+
                " left join abe30 on abe30id = eaa01cp "+
                " left join aah01 on abb01tipo = aah01id "+
                " left join abe01 on abe01id = abb01ent "+
                obterWherePadrao("eaa01", "where")  +whereRepresentante +
                whereNumInicial + whereNumfinal  +
                whereEmissao  + whereEntSai + WhereOper +
                whereidEntidade + whereidTipoDocumento + whereidCfop + whereidPcd +
                " and eaa01clasdoc = 1 "+
                " and eaa01cancData is null "+
                " and eaa01nfestat <> 5 "+
                " and eaa01nfestat <> 7 "

        Parametro p1 = numInicial != null ? Parametro.criar("numInicial",numInicial) : null
        Parametro p2 = numFinal != null ? Parametro.criar("numFinal",numFinal) : null
        Parametro p3 = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null
        Parametro p4 = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null
        Parametro p5 = idCfop != null && idCfop.size() > 0 ? Parametro.criar("idCfop", idCfop) : null
        Parametro p6 = idPcd != null && idPcd.size() > 0 ? Parametro.criar("idPcd", idPcd) : null

        List<TableMap> resultados = getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2,p3,p4,p5,p6)
        for(resultado in resultados) {
            resultado.putAll(resultado.getTableMap("eaa0103json"))
            resultado.putAll(dadosRepresentante)

        }
        dados.addAll(resultados)

    }

    private ordenar(List<TableMap> dados, int ordenacao) {
        Collections.sort(dados, new Comparator<TableMap>() {
            @Override
            public int compare(TableMap  dado1, TableMap  dado2) {

                if(ordenacao == 1 && dado1.getInteger("abb01num") != null && dado2.getInteger("abb01num") != null) {
                    return dado1.getInteger("abb01num").compareTo(dado2.getInteger("abb01num"))
                }else if(ordenacao == 2 && dado1.getLong("abe01id") != null && dado2.getLong("abe01id") != null){
                    return dado1.getLong("abe01id").compareTo(dado2.getLong("abe01id"))
                }
                return 0
            }
        });
    }

    private void somarValores(TableMap valoresDocumento, TableMap valoresTotal, Map<String,String> campos) {
        for(campo in campos){
            if(campo != null){
                String nomeCampo = BuscarNomeBanco(campo.value.toString()) != null ? BuscarNomeBanco(campo.value.toString()) : campo.value.toString()
                if(valoresDocumento.getBigDecimal(nomeCampo) != null){
                    if(valoresTotal.getBigDecimal(nomeCampo) == null){
                        valoresTotal.put(nomeCampo, valoresDocumento.getBigDecimal_Zero(nomeCampo))
                    }else{
                        BigDecimal valorTotal = valoresTotal.getBigDecimal(nomeCampo) == null ? 0.00 :valoresTotal.getBigDecimal(nomeCampo)
                        BigDecimal valorItem = valoresDocumento.getBigDecimal(nomeCampo) == null ? 0.00 : valoresDocumento.getBigDecimal(nomeCampo)
                        BigDecimal soma = valorTotal + valorItem
                        valoresTotal.put(nomeCampo, soma)
                    }
                }
            }
        }
    }


    private List<TableMap> obterDevolucao(Long idItem){
        def whereItem = " where eaa01033itemdoc = :item "

        def sql = " select eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103totfinanc, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json "+
                " from eaa01033 "+
                " inner join eaa0103 on eaa0103id = eaa01033item  "+
                whereItem +
                " and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "

        def p1 = criarParametroSql("item", idItem)
        return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
    }

    private void comporDevolucao(TableMap valores, TableMap devolucao, Map<String,String> campos  ) {
        for (campo in campos) {
            if(campo != null){
                String nomeCampo = BuscarNomeBanco(campo.value.toString()) != null ? BuscarNomeBanco(campo.value.toString()) : campo.value.toString()
                if(valores.getBigDecimal(nomeCampo) != null){
                    BigDecimal valor = valores.getBigDecimal(nomeCampo) == null ? 0.00 : valores.getBigDecimal(nomeCampo)
                    BigDecimal valorDevolucao = devolucao.getBigDecimal(nomeCampo) == null ? 0.00 : devolucao.getBigDecimal(nomeCampo)
                    valores.put(nomeCampo, valor - valorDevolucao )
                }
            }
        }
    }

    private void comporCampo(TableMap dado, Map<String, String> nome) {
        for(campo in nome) {
            if(campo.value != null) {
                String nomeBanco = BuscarNomeBanco(campo.value)
                if(nomeBanco != null) {
                    dado.put("nome"+campo.key, campo.value )
                    dado.put("valor"+campo.key, dado.getBigDecimal_Zero(nomeBanco) )
                }else {
                    nomeBanco = buscarNomeCampo(campo.value)
                    dado.put("nome"+campo.key, nomeBanco)
                    dado.put("valor"+campo.key, dado.getBigDecimal_Zero(campo.value))
                }
            }
        }
    }

    private String BuscarNomeBanco(String campo) {
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
            default:
                return null
                break
        }
    }

    private String buscarNomeCampo(String campo) {
        String sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql,Parametro.criar("nome", campo))

    }


}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50byBwb3IgUmVwcmVzZW50YW50ZSAtIEVsIFRlY2giLCJ0aXBvIjoicmVsYXRvcmlvIn0=