package Atilatte.relatorios.site

import br.com.multiorm.criteria.join.Joins;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import br.com.multitec.utils.collections.TableMap;
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import br.com.multiorm.Query
import sam.model.entities.ab.Abe01
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import sam.core.variaveis.MDate


import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abm01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro
import sam.model.entities.ea.Eaa01

public class SITE_pedidosNotasFiscais extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SITE - Pedidos e Notas Fiscais";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        //Recupera o usuário logado
        def user = obterUsuarioLogado();
        Map<String, Object> filtrosDefault = new HashMap<>();

        //Recupera o ID do usuário logado
        Long idUser = user.aab10id;

        //Busca a entidade do usuário logado
        Abe01 entRep = buscarEntidadeDoRepresentante(idUser);

        Long idEnt = entRep.abe01id;

        //Busca as entidades ao qual o usuario logado é representante
        List<Long> entidades = getSession().createCriteria(Abe01.class)
                .addFields("abe01id")
                .addJoin(Joins.join("abe02", "abe02ent = abe01id"))
                .addWhere(Criterions.where("(abe02rep0 = " + idEnt + " or abe02rep1 = " + idEnt + " or abe02rep2 = " + idEnt + " or abe02rep3 = " + idEnt + " or abe02rep4 = " + idEnt + " )"))
                .getList(ColumnType.LONG);


        if (entRep.abe01codigo == "0405100000" || entRep.abe01codigo == "0400000000" || entRep.abe01codigo == "0400000001") {
            List<Long> listIdsReps = buscarIdsRepresentantes();
            filtrosDefault.put("idRepresentantes", listIdsReps);
        } else {
            filtrosDefault.put("idRepresentantes", idEnt);
        }

        if (entidades.size() > 0) {
            filtrosDefault.put("idEntidades", entidades);
        } else {
            filtrosDefault.put("idEntidades", idEnt);
        }

        filtrosDefault.put("clasDoc", "0");

        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        Long idUser = obterUsuarioLogado().aab10id;
        String repIni = getString("representanteIni");
        String repFin = getString("representanteFin");
        String entIni = getString("entidadeIni");
        String entFin = getString("entidadeFin");
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        Integer resumoOperacao = getInteger("resumoOperacao")
        Integer classeDoc = getInteger("clasDoc");
        List<Long> idsPcd = getListLong("pcd");

        //Busca a entidade do usuário logado
        Abe01 entRep = buscarEntidadeDoRepresentante(idUser);

        if(entRep == null) interromper("Não foi encontrado uma entidade cadastrada para o usuário logado.");

        List<Long> idsReps = entRep.abe01codigo == "0405100000" || entRep.abe01codigo == "0400000000" || entRep.abe01codigo == "0400000001" ? buscarIdsRepresentantes() : [entRep.abe01id];

        List<TableMap> representantes = buscarDadosRepresentantes(repIni, repFin, idsReps);

        List<TableMap> dados = new ArrayList<>();

        for (representante in representantes) {
            Long idRep = representante.getLong("abe01id");
            String nomeRep = representante.getString("abe01na");
            String codRep = representante.getString("abe01codigo");


            List<TableMap> listDocs = buscarDocumentos(idRep, nomeRep, codRep, entIni, entFin, dataEmissao, resumoOperacao, classeDoc, idsPcd);

            dados.addAll(listDocs);

        }

        params.put("titulo", "Pedidos Faturados");
        params.put("empresa", getVariaveis().getAac10().getAac10codigo() + "-" + getVariaveis().getAac10().getAac10na());
        if (dataEmissao != null) {
            params.put("periodoIni", dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
            params.put("periodoFin", dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }


        return gerarPDF("SITE_pedidosNotasFiscais", dados);

    }

    private List<TableMap> buscarDadosRepresentantes(String repIni, String repFin, List<Long> ids ) {

        Query rep = getSession().createQuery("SELECT abe01id,abe01na, abe01codigo " +
                " FROM abe01 " +
                "where abe01rep = 1 " +
                (repIni != null && repFin!= null ? "and abe01codigo between :repIni and :repFin " : "and abe01id in (:ids)" ));

        if(repIni != null && repFin!= null){
            rep.setParameter("repIni",repIni);
            rep.setParameter("repFin",repFin);
        }else{
            rep.setParameter("ids",ids)
        }

        return rep.getListTableMap();
    }

    private List<TableMap> buscarDocumentos(Long idRep, String nomeRep, String codRep, String entIni, String entFin, LocalDate[] dataEmissao, Integer resumoOperacao, Integer classeDoc, List<Long> idsPcd) {

        //Data Emissão Inicial e Final
        LocalDate dataEmissaoIni = null;
        LocalDate dataEmissaoFin = null;
        if (dataEmissao != null) {
            dataEmissaoIni = dataEmissao[0];
            dataEmissaoFin = dataEmissao[1];
        }

        String whereResumoOperacao = resumoOperacao.equals(0) ? " and eaa01esMov = " + Eaa01.ESMOV_ENTRADA : " and eaa01esMov = " + Eaa01.ESMOV_SAIDA;
        String whereClasDoc = classeDoc.equals(0) ? " and eaa01clasDoc = 0 " : " and eaa01clasDoc = 1 ";
        String whereEntidade = entIni != null && entFin != null ? "AND ent.abe01codigo BETWEEN :entIni AND :entFin " : "";
        String whereRep = "AND (eaa01rep0 = :idRep OR eaa01rep1 = :idRep OR eaa01rep2 = :idRep OR eaa01rep3 = :idRep OR eaa01rep4 = :idRep) ";
        String whereDtEmissao = dataEmissaoIni != null && dataEmissaoFin != null ? "AND abb01data BETWEEN :dataEmissaoIni  AND :dataEmissaoFin " : "";
        String wherePCD = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id in (:idsPcd) " : "";
        String whereCancData = "AND eaa01cancdata IS NULL ";

        Parametro parametroEntidadeIni = entIni != null && entFin != null ? Parametro.criar("entIni", entIni) : null;
        Parametro parametroEntidadeFin = entIni != null && entFin != null ? Parametro.criar("entFin", entFin) : null;
        Parametro parametroRep = Parametro.criar("idRep", idRep);
        Parametro parametroDtEmissaoIni = dataEmissaoIni != null && dataEmissaoFin != null ? Parametro.criar("dataEmissaoIni", dataEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dataEmissaoIni != null && dataEmissaoFin != null ? Parametro.criar("dataEmissaoFin", dataEmissao[1]) : null;
        Parametro parametroPCD = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;


        String sql = "SELECT aah01codigo, aah01na, abb01num,abb01data AS dtEmissao, eaa01esData AS dtEntradaSaida, " +
                "eaa01totDoc AS totDoc, abe30nome,ent.abe01codigo AS codEntidade, ent.abe01nome AS nomeEntidade, eaa01rep0,eaa01rep1,eaa01rep2,eaa01rep3,eaa01rep4, " +
                "rep0.abe01codigo AS codRep0, rep0.abe01na AS naRep0,rep0.abe01id AS idRep0, " +
                "rep1.abe01codigo AS codRep1, rep1.abe01na AS naRep1,rep1.abe01id AS idRep1, " +
                "rep2.abe01codigo AS codRep2, rep2.abe01na AS naRep2,rep2.abe01id AS idRep2, " +
                "rep3.abe01codigo AS codRep3, rep3.abe01na AS naRep3,rep3.abe01id AS idRep3, " +
                "rep4.abe01codigo AS codRep4, rep4.abe01na AS naRep4,rep4.abe01id AS idRep4,  " +
                "abb10descr " +
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "LEFT JOIN abb10 ON abb10id = abb01opercod " +
                "INNER JOIN abe01 ent on ent.abe01id = abb01ent " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "LEFT JOIN abe30 ON abe30id = eaa01cp " +
                "LEFT JOIN abe01 AS rep0 ON rep0.abe01id = eaa01rep0 " +
                "LEFT JOIN abe01 AS rep1 ON rep1.abe01id = eaa01rep1 " +
                "LEFT JOIN abe01 AS rep2 ON rep2.abe01id = eaa01rep2 " +
                "LEFT JOIN abe01 AS rep3 ON rep3.abe01id = eaa01rep3 " +
                "LEFT JOIN abe01 AS rep4 ON rep4.abe01id = eaa01rep4 " +
                "WHERE TRUE " +
                whereResumoOperacao +
                whereClasDoc +
                whereEntidade +
                whereRep +
                whereDtEmissao +
                wherePCD +
                whereCancData +
                "ORDER BY abb01num"

        List<TableMap> documentos = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidadeIni, parametroEntidadeFin, parametroRep, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroPCD);

        for (documento in documentos) {
            documento.put("codRepPrincipal", codRep);
            documento.put("naRepPrincipal", nomeRep);
        }

        return documentos;
    }

    private List<Long> buscarIdsRepresentantes() {

        Query sql = getSession().createQuery("SELECT abe01id FROM abe01 " +
                "inner join abe05 on abe05ent = abe01id " +
                "inner join aab10 on aab10id = abe05user " +
                "where abe01rep = 1 ");

        return sql.getList(ColumnType.LONG);
    }

    private Abe01 buscarEntidadeDoRepresentante(Long idUser) {

        return getSession().createCriteria(Abe01.class)
        //.addFields("abe01id")
                .addJoin(Joins.join("Abe05", "abe05ent = abe01id"))
                .addJoin(Joins.join("Aab10", "aab10id = abe05user"))
                .addWhere(Criterions.eq("aab10id", idUser)).setMaxResults(1)
                .get(ColumnType.ENTITY);

    }
}
//meta-sis-eyJkZXNjciI6IlNJVEUgLSBQZWRpZG9zIGUgTm90YXMgRmlzY2FpcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNJVEUgLSBQZWRpZG9zIGUgTm90YXMgRmlzY2FpcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==