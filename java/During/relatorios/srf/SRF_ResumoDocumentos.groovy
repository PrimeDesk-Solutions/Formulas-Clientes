package During.relatorios.srf

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

public class SRF_ResumoDocumentos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF Resumo Documentos ";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
        filtrosDefault.put("total4", true);
        filtrosDefault.put("total5", true);
        filtrosDefault.put("total6", true);
        filtrosDefault.put("devolucao", true);
        filtrosDefault.put("chkDevolucao",true);
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("tipoOperacao", "0");
        filtrosDefault.put("impressao","0");
        filtrosDefault.put("resumo","0");
        filtrosDefault.put("resumoOperacao","0")
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

        if(campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!")

        Map<String, String> campos = new HashMap<>();

        campos.put("1", campoFixo1 != null ? campoFixo1 : campoLivre1 != null ? campoLivre1 : null);
        campos.put("2", campoFixo2 != null ? campoFixo2 : campoLivre2 != null ? campoLivre2 : null);
        campos.put("3", campoFixo3 != null ? campoFixo3 : campoLivre3 != null ? campoLivre3 : null);
        campos.put("4", campoFixo4 != null ? campoFixo4 : campoLivre4 != null ? campoLivre4 : null);
        campos.put("5", campoFixo5 != null ? campoFixo5 : campoLivre5 != null ? campoLivre5 : null);
        campos.put("6", campoFixo6 != null ? campoFixo6 : campoLivre6 != null ? campoLivre6 : null);

        List<TableMap> dados = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, dtNascimento, idsEstados,
                                                idsMunicipios, idsTransportadora,optionResumo,idsItens,idEmpresa,mps);
                  
   

        for(dado in dados){
            Long id = null;
            String filtroIds = "";
            String filtrosCamposLivres = "";
            String codigoEntidade = dado.getString("abe01codigo");

            if(optionResumo == 0 ){
                filtroIds = "abe01id";
                filtrosCamposLivres = "ent.abe01id"
            }else if(optionResumo == 1){
                filtroIds = "abe01id";
                filtrosCamposLivres = "desp.abe01id"
            }else if(optionResumo == 2){
                filtroIds = "aag0201id";
                filtrosCamposLivres = "aag0201id"
            }else if(optionResumo == 3){
                filtroIds = "aag02id";
                filtrosCamposLivres = "aag02id";
            }else{
                filtroIds = "abm01id";
                filtrosCamposLivres = "abm01id";
            }
            
            id = dado.getLong(filtroIds);

            List<TableMap> tableMapJson = buscarCamposLivres( id, filtrosCamposLivres, numDocIni, numDocFin, idsTipoDoc,idsPcd,resumoOperacao,dtEmissao,dtEntradaSaida, idsEntidades,dtNascimento,idsEstados,
                                                                 idsMunicipios,idsTransportadora,idsItens,idEmpresa, mps)
            TableMap jsonTotal = new TableMap();

            for(tm in tableMapJson) {
                TableMap json = tm.getTableMap("eaa0103json")
                Long itemNotaId = tm.getLong("eaa0103id")
                TableMap devolucao = obterDevolucao(itemNotaId);
                //interromper(devolucao.toString())
                if(campoLivre1 != null) jsonTotal.put(campoLivre1, jsonTotal.getBigDecimal_Zero(campoLivre1) + json.getBigDecimal_Zero(campoLivre1))
                if(campoLivre2 != null) jsonTotal.put(campoLivre2, jsonTotal.getBigDecimal_Zero(campoLivre2) + json.getBigDecimal_Zero(campoLivre2))
                if(campoLivre3 != null) jsonTotal.put(campoLivre3, jsonTotal.getBigDecimal_Zero(campoLivre3) + json.getBigDecimal_Zero(campoLivre3))
                if(campoLivre4 != null) jsonTotal.put(campoLivre4, jsonTotal.getBigDecimal_Zero(campoLivre4) + json.getBigDecimal_Zero(campoLivre4))
                if(campoLivre5 != null) jsonTotal.put(campoLivre5, jsonTotal.getBigDecimal_Zero(campoLivre5) + json.getBigDecimal_Zero(campoLivre5))
                if(campoLivre6 != null) jsonTotal.put(campoLivre6, jsonTotal.getBigDecimal_Zero(campoLivre6) + json.getBigDecimal_Zero(campoLivre6))
                if(devolucao != null && chkDevolucao) {
                    comporDevolucao(dado, devolucao, jsonTotal, [campoLivre1,campoLivre2,campoLivre3,campoLivre4,campoLivre5,campoLivre6])
                }
            }


            dado.putAll(jsonTotal);
            buscarCampo(dado,campos)

        }

        
        if(optionResumo == 0){
            params.put("title","Resumo por Entidades");
            if (impressao == 1) return gerarXLSX("SRF_ResumoPorEntidade(Excel)",dados);
            return gerarPDF("SRF_ResumoPorEntidade(PDF)",dados);
        }else if(optionResumo == 1){
            params.put("title","Resumo por Transportadora");
            if (impressao == 1) return gerarXLSX("SRF_ResumoPorTransportadora(Excel)",dados);
            return gerarPDF("SRF_ResumoPorTransportadora(PDF)",dados)
        }else if(optionResumo == 2){
            params.put("title","Resumo por Municípios");
            if (impressao == 1) return gerarXLSX("SRF_ResumoPorMunicipios(Excel)",dados);
            return gerarPDF("SRF_ResumoPorMunicipios(PDF)",dados)
        }else if(optionResumo == 3){
            params.put("title","Resumo por Estados");
            if (impressao == 1) return gerarXLSX("SRF_ResumoPorEstados(Excel)",dados);
            return gerarPDF("SRF_ResumoPorEstados(PDF)",dados);
        }else{
            params.put("title","Resumo por Itens");
            if (impressao == 1) return gerarXLSX("SRF_ResumoPorItens(Excel)",dados);
            return gerarPDF("SRF_ResumoPorItens(PDF)",dados)
        }

    }

    private buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd,Integer resumoOperacao,LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, LocalDate[] dtNascimento, List<Long> idsEstados,
                             List<Long> idsMunicipios, List<Long> idsTransportadora, Integer optionResumo,List<Long>idsItens, Long idEmpresa, List<Integer> mps) {

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

        String whereNumIni = numDocIni != null ? "where abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereDtNasc = dtNascIni != null && dtNascFin != null ? "and ent.abe01dtnasc between :dtNascIni and :dtNascFin " : "";
        String whereEstados = idsEstados != null && idsEstados.size() > 0 ? "and aag02id in (:idsEstados) " : "";
        String whereMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? "and aag0201id in (:idsMunicipios) " : "";
        String whereTransportadora = idsTransportadora != null && idsTransportadora.size() > 0 ? "and eaa0102despacho in (:idsTransportadora) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "and eaa01gc = :idEmpresa ";
        String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";



        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
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
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
        Parametro parametroMps =  mps != null && !mps.contains(-1) ?  Parametro.criar("mps",mps) : null;

        String resumo = buscarResumo(optionResumo);

        String sql = "select sum(eaa0103qtuso) as eaa0103qtuso, " +
                     "sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , sum(eaa0103totfinanc) as eaa0103totfinanc, "+
                     resumo +
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
                     "inner join abm01 on abm01id = eaa0103item "+
                     "inner join aam06 on aam06id = abm01umu "+
                     "left join abe01 as desp on desp.abe01id = eaa0102despacho "+
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
                     whereES+
                     whereItens +
                     whereEmpresa +
                     whereMps +
                      "and eaa01cancdata is null "+
                    "and eaa01clasdoc = 1 "+
                     "group by " + resumo;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,parametroDtNascIni,
                parametroDtNascFin,parametroEstados,parametroMunicipios,parametroTransportadora,parametroItens,parametroEmpresa,parametroMps)
    }

    private List<TableMap> buscarCamposLivres(Long id,String filtrosCamposLivres,Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao,LocalDate[] dtEntradaSaida,List<Long> idsEntidades, LocalDate[] dtNascimento, List<Long>idsEstados,
                                              List<Long> idsMunicipios,List<Long> idsTransportadora,List<Long> idsItens,Long idEmpresa,List<Integer> mps){
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


        String whereNumIni = numDocIni != null ? "where abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereResumo = id != null ? "and "+filtrosCamposLivres +" = :id " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereDtNasc = dtNascIni != null && dtNascFin != null ? "and ent.abe01dtnasc between :dtNascIni and :dtNascFin " : "";
        String whereEstados = idsEstados != null && idsEstados.size() > 0 ? "and aag02id in (:idsEstados) " : "";
        String whereMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? "and aag0201id in (:idsMunicipios) " : "";
        String whereTransportadora = idsTransportadora != null && idsTransportadora.size() > 0 ? "and eaa0102despacho in (:idsTransportadora) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "and eaa01gc = :idEmpresa ";
        String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";



        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroResumo = id != null ? Parametro.criar("id",id) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
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
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
        Parametro parametroMps =  mps != null && !mps.contains(-1) ?  Parametro.criar("mps",mps) : null;


        String sql = "select eaa0103id,eaa0103json " +
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
                    "inner join abm01 on abm01id = eaa0103item "+ 
                    "inner join aam06 on aam06id = abm01umu "+
                    "left join abe01 as desp on desp.abe01id = eaa0102despacho "+
                    whereNumIni +
                    whereNumFin +
                    whereResumo +
                    whereTipoDoc +
                    wherePcd +
                    whereDtEmissao +
                    whereDtEntradaSaida +
                    whereEntidade +
                    whereDtNasc +
                    whereEstados +
                    whereMunicipios +
                    whereTransportadora +
                    whereES+
                    whereItens+
                    whereEmpresa+
                    whereMps+
                    "and eaa01cancdata is null "+
                    "and eaa01clasdoc = 1 ";

                    

                 
        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni,parametroNumFin,parametroResumo,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,parametroDtNascIni,
                parametroDtNascFin,parametroEstados,parametroMunicipios,parametroTransportadora,parametroItens,parametroEmpresa,parametroMps);

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


    private String buscarResumo(Integer optionResumo) {

        switch (optionResumo) {
            case 0:
                return "ent.abe01codigo,ent.abe01na,ent.abe01id "
                break;
            case 1:
                return "desp.abe01id, desp.abe01codigo, desp.abe01na "
                break;
            case 2:
                return "aag02nome,aag0201nome, aag0201id ";
                break;
            case 3:
                return "aag02uf,aag02nome,aag02id ";
                break;
            default:   
                return "abm01id,abm01codigo,abm01descr,abm01tipo,aam06codigo "
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
//meta-sis-eyJkZXNjciI6IlNSRiBSZXN1bW8gRG9jdW1lbnRvcyAiLCJ0aXBvIjoicmVsYXRvcmlvIn0=