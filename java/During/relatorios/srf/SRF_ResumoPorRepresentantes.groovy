package During.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils
import sam.model.entities.aa.Aac10;
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class SRF_ResumoPorRepresentantes extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF Resumo Por Representante ";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true)
        filtrosDefault.put("total2", true)
        filtrosDefault.put("total3", true)
        filtrosDefault.put("total4", true)
        filtrosDefault.put("total5", true)
        filtrosDefault.put("total6", true)
        filtrosDefault.put("devolucao", true)
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("tipoOperacao", "0");
        filtrosDefault.put("impressao","0")
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
        List<Long> representantes = getListLong("representantes")
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
        boolean chkDevolucao = getBoolean("chkDevolucao");

        String periodo = ""
        if(dtEmissao != null) {
            periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }else if(dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("totalizar1",totalizar1);
        params.put("totalizar2",totalizar2);
        params.put("totalizar3",totalizar3);
        params.put("totalizar4",totalizar4);
        params.put("totalizar5",totalizar5);
        params.put("totalizar6",totalizar6);
        params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
        params.put("periodo",periodo);


        List<TableMap> dados = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,representantes,idEmpresa);

        if (campoFixo1 != null && campoLivre1 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoFixo2 != null && campoLivre2 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoFixo3 != null && campoLivre3 != null) interromper("Selecione apenas 1 valor por campo!")
         if (campoFixo4 != null && campoLivre4 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoFixo5 != null && campoLivre5 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoFixo6 != null && campoLivre6 != null) interromper("Selecione apenas 1 valor por campo!")


        Map<String, String> campos = new HashMap<>();

        campos.put("1", campoFixo1 != null ? campoFixo1 : campoLivre1 != null ? campoLivre1 : null);
        campos.put("2", campoFixo2 != null ? campoFixo2 : campoLivre2 != null ? campoLivre2 : null);
        campos.put("3", campoFixo3 != null ? campoFixo3 : campoLivre3 != null ? campoLivre3 : null);
        campos.put("4", campoFixo4 != null ? campoFixo4 : campoLivre4 != null ? campoLivre4 : null);
        campos.put("5", campoFixo5 != null ? campoFixo5 : campoLivre5 != null ? campoLivre5 : null);
        campos.put("6", campoFixo6 != null ? campoFixo6 : campoLivre6 != null ? campoLivre6 : null);


        for(dado in dados){
            Long id = dado.getLong("abe01id");
            List<TableMap> tableMapJson = buscarCamposLivres(id,numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,idEmpresa);

            TableMap jsonTotal = new TableMap();
            for(tm in tableMapJson){
                TableMap json = tm.getTableMap("eaa0103json");
                Long itemNotaId = tm.getLong("eaa0103id")
                TableMap devolucao = obterDevolucao(itemNotaId);
                if(campoLivre1 != null) jsonTotal.put(campoLivre1,json.getBigDecimal_Zero(campoLivre1));
                if(campoLivre2 != null) jsonTotal.put(campoLivre2,json.getBigDecimal_Zero(campoLivre2));
                if(campoLivre3 != null) jsonTotal.put(campoLivre3,json.getBigDecimal_Zero(campoLivre3));
                if(campoLivre4 != null) jsonTotal.put(campoLivre4,json.getBigDecimal_Zero(campoLivre4));
                if(campoLivre5 != null) jsonTotal.put(campoLivre5,json.getBigDecimal_Zero(campoLivre5));
                if(campoLivre6 != null) jsonTotal.put(campoLivre6,json.getBigDecimal_Zero(campoLivre6));
                if(devolucao != null && chkDevolucao) {
                    comporDevolucao(dado, devolucao, jsonTotal, [campoLivre1,campoLivre2,campoLivre3,campoLivre4,campoLivre5,campoLivre6])
                }
            }
            dado.putAll(jsonTotal);
            buscarCampo(dado,campos)

        }

        params.put("title","Resumo por Representantes");
        if (impressao == 1) return gerarXLSX("SRF_ResumoPorRepresentantes(Excel)",dados);
        return gerarPDF("SRF_ResumoPorRepresentantes(PDF)",dados);


    }

    private buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd,Integer resumoOperacao,LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, List<Long> representantes, Long idEmpresa) {

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

        String operDocumento = "";
        String tipoOper = "";

        if(operacao == 0){
            operDocumento = "and abd01aplic = 1 and abd01es = 0 ";
        }else if(operacao == 1){
            operDocumento = "and abd01aplic = 1 and abd01es = 1 ";
        }else if(operacao == 2 ){
            operDocumento = "and abd01aplic = 0 and abd01es = 0 ";
        }else{
            operDocumento = "and abd01aplic = 0 and abd01es = 1 ";
        }


        if(tipoOperacao == 99){
            tipoOper = "and abb10tipocod in (0,1,3,4,5,6,7) ";
        }else{
            tipoOper = "and abb10tipocod = :tipoOperacao ";
        }


        String whereNumIni = numDocIni != null ? "where abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereReps = representantes != null && representantes.size() > 0 ? "abe01id in (:representantes) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereEmpresa = "and eaa01gc = :idEmpresa ";




        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTipoOper = tipoOperacao != 99 ? Parametro.criar("tipoOperacao",tipoOperacao) : null;
        Parametro parametroReps = representantes != null && representantes.size() > 0 ? Parametro.criar("representantes", representantes) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);




        String sql = "SELECT rep0.abe01codigo,rep0.abe01id,rep0.abe01na, "+
                "sum(eaa0103qtuso) as eaa0103qtuso, sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , sum(eaa0103totfinanc) as eaa0103totfinanc "+
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "inner join aah01 on aah01id = abb01tipo " +
                "INNER JOIN abe01 as rep0 ON rep0.abe01id = eaa01rep0  " +
                "inner join abe01 as ent on ent.abe01id = abb01ent " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "inner join abb10 on abb10id = abd01opercod " +
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                "inner join abm01 on abm01id = eaa0103item "+
                "inner join aam06 on aam06id = abm01umu "+
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereEmpresa +
                (representantes != null && representantes.size() > 0 ? "and rep0."+whereReps : "") +
                whereES+
                "GROUP BY rep0.abe01codigo,rep0.abe01id,rep0.abe01na " +
                "UNION ALL " +
                "SELECT rep1.abe01codigo,rep1.abe01id,rep1.abe01na, " +
                "sum(eaa0103qtuso) as eaa0103qtuso, sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , sum(eaa0103totfinanc) as eaa0103totfinanc "+
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "inner join aah01 on aah01id = abb01tipo " +
                "INNER JOIN abe01 as rep1 ON rep1.abe01id = eaa01rep1  " +
                "inner join abe01 as ent on ent.abe01id = abb01ent " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "inner join abb10 on abb10id = abd01opercod " +
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                "inner join abm01 on abm01id = eaa0103item "+
                "inner join aam06 on aam06id = abm01umu "+
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereEmpresa +
                (representantes != null && representantes.size() > 0 ? "and rep1."+whereReps : "") +
                whereES+
                "GROUP BY rep1.abe01codigo,rep1.abe01id,rep1.abe01na " +
                "UNION ALL " +
                "SELECT rep2.abe01codigo,rep2.abe01id,rep2.abe01na, " +
                "sum(eaa0103qtuso) as eaa0103qtuso, sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , sum(eaa0103totfinanc) as eaa0103totfinanc "+
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "inner join aah01 on aah01id = abb01tipo " +
                "INNER JOIN abe01 as rep2 ON rep2.abe01id = eaa01rep2  " +
                "inner join abe01 as ent on ent.abe01id = abb01ent " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "inner join abb10 on abb10id = abd01opercod " +
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                "inner join abm01 on abm01id = eaa0103item "+
                "inner join aam06 on aam06id = abm01umu "+
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereEmpresa +
                (representantes != null && representantes.size() > 0 ? "and rep2."+whereReps : "") +
                whereES+
                "GROUP BY rep2.abe01codigo,rep2.abe01id,rep2.abe01na " +
                "UNION ALL " +
                "SELECT rep3.abe01codigo,rep3.abe01id,rep3.abe01na, " +
                "sum(eaa0103qtuso) as eaa0103qtuso, sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , sum(eaa0103totfinanc) as eaa0103totfinanc "+
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "inner join aah01 on aah01id = abb01tipo " +
                "INNER JOIN abe01 as rep3 ON rep3.abe01id = eaa01rep3  " +
                "inner join abe01 as ent on ent.abe01id = abb01ent " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "inner join abb10 on abb10id = abd01opercod " +
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                "inner join abm01 on abm01id = eaa0103item "+
                "inner join aam06 on aam06id = abm01umu "+
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereEmpresa +
                (representantes != null && representantes.size() > 0 ? "and rep3."+whereReps : "") +
                whereES+
                "GROUP BY rep3.abe01codigo,rep3.abe01id,rep3.abe01na " +
                "UNION ALL " +
                "SELECT rep4.abe01codigo,rep4.abe01id,rep4.abe01na, " +
                "sum(eaa0103qtuso) as eaa0103qtuso, sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , sum(eaa0103totfinanc) as eaa0103totfinanc "+
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "inner join aah01 on aah01id = abb01tipo " +
                "INNER JOIN abe01 as rep4 ON rep4.abe01id = eaa01rep4  " +
                "inner join abe01 as ent on ent.abe01id = abb01ent " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "inner join abb10 on abb10id = abd01opercod " +
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                "inner join abm01 on abm01id = eaa0103item "+
                "inner join aam06 on aam06id = abm01umu "+
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereEmpresa +
                (representantes != null && representantes.size() > 0 ? "and rep4."+whereReps : "")+
                whereES +
                "GROUP BY rep4.abe01codigo,rep4.abe01id,rep4.abe01na "

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,parametroEmpresa,parametroReps);
    }

    private List<TableMap> buscarCamposLivres(Long id,Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao,LocalDate[] dtEntradaSaida,List<Long> idsEntidades, Long idEmpresa){
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


        String whereNumIni = numDocIni != null ? "where abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereId = "and (eaa01rep0 = :id or eaa01rep1 = :id or eaa01rep2 = :id or eaa01rep3 = :id or eaa01rep4 = :id) ";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereEmpresa = "and eaa01gc = :idEmpresa ";


        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroId = Parametro.criar("id",id);
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);


        String sql = "select eaa0103id,eaa0103json "+
                "from eaa01 " +
                "inner join abb01 on abb01id = eaa01central " +
                "inner join abe01 as ent on ent.abe01id = abb01ent " +
                "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 " +
                "inner join aah01 on aah01id = abb01tipo " +
                "inner join abd01 on abd01id = eaa01pcd " +
                "inner join abb10 on abb10id = abd01opercod "+
                "inner join aag0201 on aag0201id = abe0101municipio " +
                "inner join aag02 on aag02id = aag0201uf " +
                "inner join eaa0102 on eaa0102doc = eaa01id " +
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                whereNumIni +
                whereNumFin +
                whereId +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereEmpresa +
                whereES;
                

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni,parametroNumFin,parametroId,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,parametroEmpresa)

    }

    private TableMap obterDevolucao(Long idItem){
        def whereItem = " where eaa01033itemdoc = :item "

        def sql = " select eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json "+
                " from eaa01033 "+
                " inner join eaa0103 on eaa0103id = eaa01033item  "+
                whereItem +
                " and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "

        def p1 = criarParametroSql("item", idItem)
        return getAcessoAoBanco().buscarUnicoTableMap(sql,p1)
    }

    private void comporDevolucao(TableMap dado, TableMap devolucao, TableMap ItensJson, List<String> campos ) {
        for(campo in campos) {
            if(campo != null) {
                ItensJson.put(campo, ItensJson.getBigDecimal_Zero(campo) - devolucao.getTableMap("eaa0103json").getBigDecimal_Zero(campo))
            }
        }
        for(campo in ["eaa0103qtuso","eaa0103qtcoml","eaa0103unit","eaa0103total","eaa0103totdoc","eaa0103totfinanc"]) {
            dado.put(campo, dado.getBigDecimal_Zero(campo) - devolucao.getBigDecimal_Zero(campo))
        }

    }


     private String buscarCampo(TableMap dados, Map<String,String>campos){
        for(campo in campos ){
            if(campo.value != null){
                String nomeBanco = buscarNomeBanco(campo.value);
                if(nomeBanco != null){
                    dados.put("campo"+campo.key,campo.value);
                    dados.put("valor"+campo.key,dados.getBigDecimal_Zero(nomeBanco))
                }else{
                    nomeBanco = buscarNomeCampo(campo.value);
                    dados.put("campo"+campo.key,nomeBanco);
                    dados.put("valor"+campo.key,dados.getBigDecimal_Zero(campo.value));
                }
            }
        }

    }
    private buscarNomeBanco(String campo){
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
        def sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

    }
}
//meta-sis-eyJkZXNjciI6IlNSRiBSZXN1bW8gUG9yIFJlcHJlc2VudGFudGUgIiwidGlwbyI6InJlbGF0b3JpbyJ9