/*
Desenvolvido por : ROGER ROBERT
 */
package Atilatte.relatorios.scq


import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.model.entities.aa.Aac10;

public class SCQ_InformacoesFichaDeInspecao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCQ-Informações Ficha De Inspeção";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numeroFichaIni = getInteger("numeroInicial");
        Integer numeroFichaFin = getInteger("numeroFinal");

        LocalDate[] dataFicha = getIntervaloDatas("dataCentralDoc");
        LocalDate[] dataFichaFabricacao = getIntervaloDatas("dataFabricacao");
        LocalDate[] dataFichaValidade = getIntervaloDatas("dataValidade");

        List<Integer> statusFicha = getListInteger("statusFicha");
        List<Long> idsItem = getListLong("idsItens");
        List<Integer> mps = getListInteger("mps");
        List<Long> itensAvaliacao = getListLong("itensAvaliacao");
        List<TableMap> camposLivres = buscarCamposLivresItensAvaliacao(itensAvaliacao);

        List<TableMap> registros = buscarFichasInspecao(numeroFichaIni, numeroFichaFin, dataFicha, statusFicha, idsItem, mps, dataFichaFabricacao, dataFichaValidade);
        List<TableMap> dados = new ArrayList();

        for (registro in registros){
            LocalDate dtValidade = registro.getDate("dataValidade");
            LocalDate dtFabricacao = registro.getDate("dataFabricacao");
            Integer numFicha = registro.getInteger("numCentralDoc");

            if(dtValidade == null || dtFabricacao == null) interromper("Não foi preenchida a data de validade ou fabricação na ficha de inspeção " + numFicha);

            TableMap tmCamposLivreFicha = registro.getTableMap("bbb0101json") != null ? registro.getTableMap("bbb0101json") : new TableMap();
            preencherCamposLivres(registro, tmCamposLivreFicha, camposLivres);
            dados.add(registro);
        }

        return gerarXLSX("SCQ_InformacoesFichaDeInspecao", dados);
    }

    private List<TableMap> buscarFichasInspecao(Integer numeroFichaIni, Integer numeroFichaFin, LocalDate[] dataFicha, List<Integer> statusFicha,
                                                List<Long> idsItem, List<Integer> mps, LocalDate[] dataFichaFabricacao, LocalDate[] dataFichaValidade){

        //Data Inicial - Final
        LocalDate dataIni = null;
        LocalDate dataFin = null;

        if(dataFicha != null){
            dataIni = dataFicha[0];
            dataFin = dataFicha[1];
        }

        //Data Inicial - Final - Fabricação
        LocalDate dataFabricacaoIni = null;
        LocalDate dataFabricacaoFin = null;

        if(dataFichaFabricacao != null){
            dataFabricacaoIni = dataFichaFabricacao[0];
            dataFabricacaoFin = dataFichaFabricacao[1];
        }

        //Data Inicial - Final - Validade
        LocalDate dataValidadeIni = null;
        LocalDate dataValidadeFin = null;

        if(dataFichaValidade != null){
            dataValidadeIni = dataFichaValidade[0];
            dataValidadeFin = dataFichaValidade[1];
        }

        String whereCentralDoc = numeroFichaIni != null && numeroFichaFin != null ? "and abb01num between  :numeroFichaIni and :numeroFichaFin " : "";
        String whereDataFicha = dataIni != null && dataFin != null ? "and abb01data between :dataIni and :dataFin " : "";
        String whereDataFabricacao = dataFabricacaoIni != null && dataFabricacaoFin != null ? "and CAST(bbb01json ->> 'data_fabricacao' AS date)  between :dataFabricacaoIni and :dataFabricacaoFin " : "";
        String whereDataValidade = dataValidadeIni != null && dataValidadeFin != null ? "and CAST(bbb01json ->> 'data_de_validade' AS date)  between :dataValidadeIni and :dataValidadeFin " : "";
        String whereStatusFicha = statusFicha != null && statusFicha.size() > 0 ? "and bbb01status in (:statusFicha) " : "";
        String whereIdsItem = idsItem != null && idsItem.size() > 0 ? "and abm01id in (:idsItem) " : "";
        String whereMPS = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";

        Parametro parametroCentralDocIni = numeroFichaIni != null ? Parametro.criar("numeroFichaIni", numeroFichaIni) : null;
        Parametro parametroCentralDocFin = numeroFichaFin != null ? Parametro.criar("numeroFichaFin", numeroFichaFin) : null;

        Parametro parametroDataFichaIni = dataIni != null ? Parametro.criar("dataIni", dataIni) : null;
        Parametro parametroDataFichaFin = dataFin != null ? Parametro.criar("dataFin", dataFin) : null;

        Parametro parametroDataFabricacaoIni = dataFabricacaoIni != null ? Parametro.criar("dataFabricacaoIni", dataFabricacaoIni) : null;
        Parametro parametroDataFabricacaoFin = dataFabricacaoFin != null ? Parametro.criar("dataFabricacaoFin", dataFabricacaoFin) : null;

        Parametro parametroDataValidadeIni = dataValidadeIni != null ? Parametro.criar("dataValidadeIni", dataValidadeIni) : null;
        Parametro parametroDataValidadeFin = dataValidadeFin != null ? Parametro.criar("dataValidadeFin", dataValidadeFin) : null;

        Parametro parametroStatusFicha = statusFicha != null && statusFicha.size() > 0 ? Parametro.criar("statusFicha", statusFicha) : null;

        Parametro parametroIdsItem = idsItem != null && idsItem.size() > 0 ? Parametro.criar("idsItem", idsItem) : null;

        Parametro parametroMPS = !mps.contains(-1) ? Parametro.criar("mps", mps) : null;

        String sql = "select aah01codigo as codTipoDoc, aah01nome as nomeTipoDoc, abb01num as numCentralDoc, abb01serie as serieCentralDoc, abb01data as dataCentralDoc, case when bbb01status = 0 then '0-criada' when bbb01status = 1 then '1-Em Processo' " +
                "when bbb01status = 2 then '2-Analisada' when bbb01status = 3 then '3-Encerrada Sem Restriçoes' " +
                "when bbb01status = 4 then '4-Encerrada com Restriçoes' else '5-Encerrada Restriçoes' end as status, bbb01qtTotal, bbb01qtAmostra, bbb01lote, bbb01serie, case when abm01tipo = 0 then '0-Mat' " +
                "when abm01tipo = 1 then '1-Prod' when abm01tipo = 2 then '2-merc' else '3-serv' end as tipoItem, abm01codigo as codItem, abm01na as itemNomeAbrev, abm01descr as descrItem, aam06codigo as codUidadeMedida, " +
                "bbb0101seq as sequencia, bbb0101ficha, abm50codigo as codItensAvaliacao, abm50descr as descrItemAvali, bbb0101valida, bbb0101data as dataAvaliacao, bbb0101hora as horaAvaliacao, bbb0101user as userAvaliacao, bbb0101instrumento as instrumentoAvaliacao, abb20nome as nomePatrimonial, bbb0101clas as classificacaoAvaliacao, " +
                "bba01descr as descricaoClassAvaliacao, bbb0101just as justificativaAvali, abm50orientacoes as orientacaoItemAvali, abm50obs as observaçõesItemAvali, bbb0101json," +
                "CAST(BBB01JSON ->> 'data_de_validade' AS date) as dataValidade, " +
                "CAST(BBB01JSON ->> 'data_fabricacao' AS date) as dataFabricacao " +
                "from bbb01 " +
                "inner join abb01 on bbb01central = abb01id " +
                "left join aah01 on abb01tipo = aah01id " +
                "inner join abm01 on bbb01item = abm01id " +
                "left join aam06 on abm01umu = aam06id " +
                "left join bbb0101  on bbb0101ficha = bbb01id " +
                "left join abm50 on bbb0101ia = abm50id " +
                "left join aab10 on bbb0101user = aab10id " +
                "left join abb20 on bbb0101instrumento = abb20id " +
                "left join bba01 on bbb0101clas = bba01id " +
                "where true " +
                whereCentralDoc +
                whereDataFicha +
                whereDataFabricacao +
                whereDataValidade +
                whereStatusFicha +
                whereIdsItem +
                whereMPS +
                "order by abb01num, abm01codigo, abb01data, cast(bbb01json ->> 'data_fabricacao' as date), cast(bbb01json ->> 'data_de_validade' as date)";

        List<TableMap> tmFichas = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroCentralDocIni, parametroCentralDocFin,
                parametroDataFichaIni, parametroDataFichaFin, parametroDataFabricacaoIni, parametroDataFabricacaoFin, parametroDataValidadeIni, parametroDataValidadeFin,parametroStatusFicha,
                parametroIdsItem, parametroMPS);

        for(ficha in tmFichas){
            if(ficha.getInteger("bbb0101valida") != null){

                if(ficha.getInteger("bbb0101valida") == 0){
                    ficha.put("statusAvaliacao", "0-Verificar");

                }else if(ficha.getInteger("bbb0101valida") == 1){
                    ficha.put("statusAvaliacao", "1-Conforme");

                }else{
                    ficha.put("statusAvaliacao", "2-Não Conforme");
                }
            }else{
                ficha.put("statusAvaliacao", "Não Avaliado");
            }
        }

        return tmFichas;
    }

    private void preencherCamposLivres(TableMap registro, TableMap tmCamposLivreFicha, List<TableMap> camposLivres){
        def indexCampo = 0;
        for(campo in camposLivres){
            indexCampo++;
            String descrCampo = campo.getString("aah02descr");
            String nomeCampo = campo.getString("aah02nome");
            registro.put("nomeCampo" + indexCampo.toString(), descrCampo );
            registro.put("valorCampo" + indexCampo.toString(), tmCamposLivreFicha.get(nomeCampo));
        }
    }

    private List<TableMap>buscarCamposLivresItensAvaliacao(List<Long> idsItensAvaliacao){

        String whereItemAvaliacao = idsItensAvaliacao != null && idsItensAvaliacao.size() > 0 ? "where abm50id in (:idsItensAvaliacao)" : "";

        String sql = "select distinct aah02nome, aah02descr " +
                "from abm50 " +
                "inner join aam02 on aam02id = abm50especia " +
                "inner join aam0201 on aam0201espec = aam02id " +
                "inner join aah02 on aah02id = aam0201campo " +
                whereItemAvaliacao;


        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idsItensAvaliacao", idsItensAvaliacao));

    }
}
//meta-sis-eyJkZXNjciI6IlNDUS1JbmZvcm1hw6fDtWVzIEZpY2hhIERlIEluc3Blw6fDo28iLCJ0aXBvIjoicmVsYXRvcmlvIn0=