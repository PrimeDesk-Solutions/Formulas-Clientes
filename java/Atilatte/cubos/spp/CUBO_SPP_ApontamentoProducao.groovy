package Atilatte.cubos.spp

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import sam.dicdados.Parametros
import sam.model.entities.ab.Aba01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

public class CUBO_SPP_ApontamentoProducao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CUBO - SPP - Apontamentos de Producao";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", "0000001");
        filtrosDefault.put("numeroFinal", "9999999");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {

        LocalDate[] dataDoc = getIntervaloDatas("dataDocProd");
        LocalDate[] dataApont = getIntervaloDatas("dataApont");
        LocalDate[] dataPlano = getIntervaloDatas("dataPlano");
        LocalDate[] dataInicioProd = getIntervaloDatas("dataInicioProd");
        Integer numI = getInteger("numeroInicial");
        Integer numF = getInteger("numeroFinal");
        List<Long> idTipos = getListLong("tipos");
        List<Long> idApontamentos = getListLong("apontamentos");
        List<Long> idRecurso = getListLong("recurso");
        String campoLivre1 = getString("campoLivre1");
        String campoLivre2 = getString("campoLivre2");
        String campoLivre3 = getString("campoLivre3");
        String campoLivre4 = getString("campoLivre4");
        String campoLivre5 = getString("campoLivre5");
        String campoLivre6 = getString("campoLivre6");

        Map<String, String> campos = new HashMap()
        campos.put("1", campoLivre1 != null ? campoLivre1 : null);
        campos.put("2", campoLivre2 != null ? campoLivre2 : null);
        campos.put("3", campoLivre3 != null ? campoLivre3 : null);
        campos.put("4", campoLivre4 != null ? campoLivre4 : null);
        campos.put("5", campoLivre5 != null ? campoLivre5 : null);
        campos.put("6", campoLivre6 != null ? campoLivre6 : null);

        List<TableMap> dadosRelatorio = buscarDadosRelatorio(dataDoc, dataPlano, numI, numF, idTipos, idApontamentos, idRecurso, dataInicioProd);
        String conteudoParam = buscarConteudoParametro(Parametros.BA_ESPECAPONTLIVRE.getAplic(), Parametros.BA_ESPECAPONTLIVRE.getParam());

        List<String> camposLivres = buscarCamposLivres(conteudoParam);

        for (dado in dadosRelatorio) {
            TableMap mapJson = dado.getTableMap("bab0102json") != null ? dado.getTableMap("bab0102json") : new TableMap();
            TableMap jsonTotal = new TableMap();
            def idOrdem = dado.getLong("bab01id");

            if (mapJson.get("qtd_kg") != null) {
                def pesoUnit = dado.getBigDecimal_Zero("pesoUnit");
                def totalPecas = mapJson.get("qtd_kg") / pesoUnit;
                jsonTotal.put("qtdPecas", totalPecas);
            }

            for (campo in camposLivres) {
                jsonTotal.put(campo, mapJson.get(campo));
            }

            def qtdProduzida = buscarQuantidadeProduzida(idOrdem);

            dado.put("qtdReali", qtdProduzida);
            dado.putAll(jsonTotal);

            buscarCampo(dado, campos);
        }

        gerarXLSX("relatoriodeapontamento", dadosRelatorio);

    }

    private List<TableMap> buscarDadosRelatorio(LocalDate[] dataDoc, LocalDate[] dataPlano, Integer numI, Integer numF, List<Long> idTipos, List<Long> idApontamentos, List<Long> idRecurso, LocalDate[] dataInicioProd) {
        // Datas Ordem Produção
        LocalDate dataIniOrdem = null;
        LocalDate dataFimOrdem = null;
        if (dataDoc != null && dataDoc.size() > 0) {
            dataIniOrdem = dataDoc[0];
            dataFimOrdem = dataDoc[1];

        }

        // Datas do Plano
        LocalDate dataIniPlano = null;
        LocalDate dataFimPlano = null;
        if (dataPlano != null && dataPlano.size() > 0) {
            dataIniPlano = dataPlano[0];
            dataFimPlano = dataPlano[1];
        }

        // Data Inicio Produção
        LocalDate dataIniProd = null;
        LocalDate dataFimProd = null;
        if (dataInicioProd != null && dataInicioProd.size() > 0) {
            dataIniProd = dataInicioProd[0];
            dataFimProd = dataInicioProd[1];
        }

        String whereNumIni = numI != null ? "WHERE abb01ordem.abb01num >=  :numI " : "";
        String whereNumFin = numF != null ? "and abb01ordem.abb01num <= :numF " : "";
        String whereData = dataIniOrdem != null && dataFimOrdem != null ? "and abb01ordem.abb01data BETWEEN :dataIniOrdem and :dataFimOrdem " : "";
        String whereDataInicioProd = dataIniProd != null && dataFimProd ? "and bab01ctDtI BETWEEN :dataIniProd and :dataFimProd " : "";
        String whereDataIniPlano = dataPlano != null ? "and abb01plano.abb01data >= :dataIniPlano " : "";
        String whereDataFimPlano = dataPlano != null ? "and abb01plano.abb01data <= :dataFimPlano " : "";
        String whereTipoDoc = idTipos != null ? "AND aah01id in (:idTipos) " : "";
        String whereApontamentos = idApontamentos != null ? "AND abp01id in (:idApontamentos) " : "";
        String whereRecurso = idRecurso != null ? "AND abb20id in (:idRecurso) " : "";

        Parametro parametroNumIni = numI != null ? Parametro.criar("numI", numI) : null;
        Parametro parametroNumFin = numF != null ? Parametro.criar("numF", numF) : null;
        Parametro parametroDataIniOrdem = dataIniOrdem != null ? Parametro.criar("dataIniOrdem", dataIniOrdem) : null;
        Parametro parametroDataFinOrdem = dataFimOrdem != null ? Parametro.criar("dataFimOrdem", dataFimOrdem) : null;

        Parametro parametroDataIniProd = dataIniProd != null ? Parametro.criar("dataIniProd", dataIniProd) : null;
        Parametro parametroDataFinProd = dataFimProd != null ? Parametro.criar("dataFimProd", dataFimProd) : null;

        Parametro parametroDataIniPlan = dataIniPlano != null ? Parametro.criar("dataIniPlano", dataIniPlano) : null;
        Parametro parametroDataFimPlan = dataFimPlano != null ? Parametro.criar("dataFimPlano", dataFimPlano) : null;

        Parametro parametroTipo = idTipos != null ? Parametro.criar("idTipos", idTipos) : null;
        Parametro parametroApontamento = idApontamentos != null ? Parametro.criar("idApontamentos", idApontamentos) : null;
        Parametro parametroRecurso = idRecurso != null ? Parametro.criar("idRecurso", idRecurso) : null;

        String sql = "Select distinct abb01ordem.abb01num,bab01id, bab0102dtI as dataIni, bab0102dtf as dataFim, " +
                "bab0102hrI as horaIni, bab0102hrf as horaFim, bab01ctDtI as dataIniProd, " +
                "bab01ctDtF as dataFimProd, bab01ctHrI as horaIniProd, bab01ctHrF as horaFimProd, " +
                "abm01codigo, abm01descr,abb20codigo as bem, bab01lote as lote, " +
                "abp01codigo, abp01descr as descrApont, bab0102det as obsApont, " +
                "bab01qt as qtProgr, bab0102json, " +
                "CASE WHEN abm01tipo = 0 THEN 'M' WHEN abm01tipo = 1 THEN 'P' ELSE '' END AS MPS, " +
                "CAST(abm0101json ->>'peso_unit_' AS numeric(14,6)) as pesoUnit, baa01descr as nomePlano  " +
                "from bab01  " +
                "inner join bab0102 on bab0102op = bab01id  " +
                "inner join bab0103 on bab0103op = bab01id " +
                "inner join abp20 on bab01comp = abp20id  " +
                "inner join abm01 on abp20item = abm01id  " +
                "left join abm0101 on abm0101item = abm01id  " +
                "inner join abp01 on bab0102ativ = abp01id  " +
                "left join abb20 on bab0102bem = abb20id  " +
                "inner join abb01 as abb01ordem on bab01central = abb01ordem.abb01id  " +
                "inner join aah01 on abb01ordem.abb01tipo = aah01id  " +
                "inner join baa0101 on baa0101id = bab0103itempp " +
                "inner join baa01 on baa01id = baa0101plano " +
                "inner join abb01 as abb01plano on abb01plano.abb01id = baa01central " +
                whereNumIni +
                whereNumFin +
                whereData +
                whereDataInicioProd +
                whereDataIniPlano +
                whereDataFimPlano +
                whereTipoDoc +
                whereApontamentos +
                whereRecurso;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumIni, parametroNumFin, parametroDataIniOrdem, parametroDataFinOrdem, parametroDataIniProd, parametroDataFinProd, parametroDataIniPlan, parametroDataFimPlan, parametroTipo, parametroApontamento, parametroRecurso);

    }

    private String buscarConteudoParametro(String aplic, String param) {
        return getSession().createCriteria(Aba01.class)
                .addFields("aba01conteudo")
                .addWhere(Criterions.eq("aba01aplic", aplic))
                .addWhere(Criterions.eq("aba01param", param))
                .addWhere(getSamWhere().getCritPadrao(Aba01.class))
                .get(ColumnType.STRING);
    }

    private List<String> buscarCamposLivres(String nomeEspec) {
        String sql = "select aah02nome from aam02 " +
                "inner join aam0201 on aam0201espec = aam02id " +
                "inner join aah02 on aah02id = aam0201campo " +
                "where aam02nome = :nomeEspec ";

        getAcessoAoBanco().obterListaDeString(sql, Parametro.criar("nomeEspec", nomeEspec));
    }

    private buscarCampo(TableMap dado, Map<String, String> campos) {
        for (campo in campos) {
            if (campo.value != null) {
                String nomeCampo = buscarNomeCampoLivre(campo.value);
                dado.put("nomeCampo" + campo.key, nomeCampo)
                dado.put("valorCampo" + campo.key, dado.get(campo.value));
            }
        }
    }

    public String buscarNomeCampoLivre(String campo) {
        def sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql, criarParametroSql("nome", campo));

    }

    private BigDecimal buscarQuantidadeProduzida(Long idOrdem) {
        String sql = "select COALESCE(sum(bab01041qt),0) as qtdProduzida " +
                "from bab01 " +
                "inner join bab0104 on bab0104op = bab01id " +
                "inner join bab01041 on bab01041pc = bab0104id " +
                "where bab01id = :idOrdem ";

        return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idOrdem", idOrdem));
    }


}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUFAgLSBBcG9udGFtZW50b3MgZGUgUHJvZHVjYW8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUFAgLSBBcG9udGFtZW50b3MgZGUgUHJvZHVjYW8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=