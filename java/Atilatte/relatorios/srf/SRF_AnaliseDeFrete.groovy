package Atilatte.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;
import java.time.LocalDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.ea.Eaa01
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.relatorio.TableMapDataSource


public class SRF_AnaliseDeFrete extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF Análise de Frete";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
        filtrosDefault.put("optionTransp", '0');
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
        List<Long> idsTransps = getListLong("transportadora");
        Integer optionTransp = getInteger("optionTransp");
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
        params.put("title","Documento por Transportadoras");
        params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
        params.put("periodo",periodo);

        if(campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")


        List<TableMap> documentos = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,
                optionResumo,idsItens,idEmpresa,idsTransps,optionTransp);
        def totalGeral1 = 0;
        def totalGeral2 = 0;
        def totalGeral3 = 0;

        for(documento in documentos){
            def nomeCampo = ""
            def valorCampo = ""
            documento.put("key",idTransp);
            documento.put("totalizar1",totalizar1);
            documento.put("totalizar2",totalizar2);
            documento.put("totalizar3",totalizar3);

            if(documento.getTableMap("eaa01json") != null && documento.getTableMap("eaa01json").size() > 0 ) documento.putAll(documento.getTableMap("eaa01json"));
            if(campoLivre1 != null){
                nomeCampo = buscarNomeCampoLivre(campoLivre1);
                valorCampo = campoLivre1;
                documento.put("nomeCampo1",nomeCampo);
                def valor1 = documento.get(campoLivre1) == null ? new BigDecimal(0) : documento.get(campoLivre1);
                documento.put("valorCampo1",valor1);
                documento.put("campoControle1",campoLivre1);
            }
            if(campoFixo1 != null){
                nomeCampo = campoFixo1;
                valorCampo = buscarNomeCampoFixo(campoFixo1);
                documento.put("nomeCampo1",nomeCampo);
                def valor1 = documento.get(valorCampo) == null ? new BigDecimal(0) : documento.get(valorCampo);
                documento.put("valorCampo1",valor1);
            }
            if(campoLivre2 != null){
                nomeCampo = buscarNomeCampoLivre(campoLivre2);
                valorCampo = campoLivre2;
                documento.put("nomeCampo2",nomeCampo);
                def valor2 = documento.get(campoLivre2) == null ? new BigDecimal(0) : documento.get(campoLivre2);
                documento.put("valorCampo2",valor2);
                documento.put("campoControle2",campoLivre2);
            }
            if(campoFixo2 != null){
                nomeCampo = campoFixo2;
                valorCampo = buscarNomeCampoFixo(campoFixo2);
                documento.put("nomeCampo2",nomeCampo);
                def valor2 = documento.get(valorCampo) == null ? new BigDecimal(0) : documento.get(valorCampo);
                documento.put("valorCampo2",valor2);
            }
            if(campoLivre3 != null){
                nomeCampo = buscarNomeCampoLivre(campoLivre3);
                valorCampo = campoLivre3;
                documento.put("nomeCampo3",nomeCampo);
                def valor3 = documento.get(campoLivre3) == null ? new BigDecimal(0) : documento.get(campoLivre3);
                documento.put("valorCampo3",valor3);
                documento.put("campoControle3",campoLivre3);
            }
            if(campoFixo3 != null){
                nomeCampo = campoFixo3;
                valorCampo = buscarNomeCampoFixo(campoFixo3);
                documento.put("nomeCampo3",nomeCampo);
                def valor3 = documento.get(valorCampo) == null ? new BigDecimal(0) : documento.get(valorCampo);
                documento.put("valorCampo3",valor3);
            }

            if(chkDevolucao){
                Long idDoc = documento.getLong("eaa01id");
                def devolucao1 = 0, devolucao2 = 0,devolucao3 = 0;
                def nome1, nome2, nome3;
                List<Long> itens = obterItensDoc(idDoc);
                if(itens != null && itens.size() > 0){
                    for(item in itens){
                        TableMap tmDevolucao = buscarDevolucao(item);
                        if(tmDevolucao != null){
                            if(tmDevolucao.get("eaa01033qtComl") > 0 && tmDevolucao.get("eaa01033qtUso") > 0){
                                tmDevolucao.putAll(tmDevolucao.getTableMap("eaa0103json"));
                                if(documento.get("nomeCampo1") != null){
                                    nome1 = buscarNomeCampoFixo(documento.get("nomeCampo1"))
                                    if(nome1 == null) {
                                        nome1 = documento.get("campoControle1")
                                        if(nome1 != null)devolucao1  += tmDevolucao.getBigDecimal_Zero(nome1)
                                    }else {
                                        devolucao1  += tmDevolucao.getBigDecimal_Zero(nome1)
                                    }
                                }
                                if(documento.get("nomeCampo2") != null){
                                    nome2 = buscarNomeCampoFixo(documento.get("nomeCampo2"))
                                    if(nome2 == null) {
                                        nome2 = documento.get("campoControle2")
                                        if(nome2 != null)devolucao2  += tmDevolucao.getBigDecimal_Zero(nome2)
                                    }else {
                                        devolucao2  += tmDevolucao.getBigDecimal_Zero(nome2)
                                    }
                                }
                                if(documento.get("nomeCampo3") != null){
                                    nome3 = buscarNomeCampoFixo(documento.get("nomeCampo3"))
                                    if(nome3 == null) {
                                        nome3 = documento.get("campoControle1")
                                        if(nome3 != null)devolucao3  += tmDevolucao.getBigDecimal_Zero(nome3)
                                    }else {
                                        devolucao3  += tmDevolucao.getBigDecimal_Zero(nome3)
                                    }
                                }
                            }
                        }
                    }
                }

                if(devolucao1 > 0) documento.put("valorCampo1",documento.get(nome1) - devolucao1);
                if(devolucao2 > 0) documento.put("valorCampo2",documento.get(nome2) - devolucao2);
                if(devolucao3 > 0) documento.put("valorCampo3",documento.get(nome3) - devolucao3);


            }

            if(documento.get("valorCampo1") != null) totalGeral1 += documento.get("valorCampo1");
            if(documento.get("valorCampo2") != null) totalGeral2 += documento.get("valorCampo2");
            if(documento.get("valorCampo3") != null) totalGeral3 += documento.get("valorCampo3");
            documento.put("totalGeral1",totalGeral1);
            documento.put("totalGeral2",totalGeral2);
            documento.put("totalGeral3",totalGeral3);
        }

        return gerarXLSX("SRF_AnaliseDeFrete",documentos);
    }



    private buscarDocumentos(Integer numDocIni, Integer numDocFin,List<Long>idsTipoDoc,List<Long> idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao,
                             LocalDate[] dtEntradaSaida,List<Long> idsEntidades,Integer optionResumo,List<Long>idsItens,Long idEmpresa,List<Long> idTransp,Integer optionTransp){
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

        String whereNumIni = numDocIni != null ? "and abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereTransp = (idTransp != null && idTransp.size() > 0)? "and (desp.abe01id in (:idTransp) or redesp.abe01id in (:idTransp)) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTransp = idTransp != null && idTransp.size() > 0 ? Parametro.criar("idTransp", idTransp) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);

        String orderBy = "order by abb01num "

        String sql = "select aag02uf as estado, aag0201nome as municipio, aah20placa as placa,eaa01id,abb01num,abb01data, eaa01esdata, ent.abe01codigo as codEnt, ent.abe01na as naEnt, "+
                "eaa01rep0,aah01codigo,abe30nome,abe40nome,eaa01json, desp.abe01codigo as codDesp, desp.abe01na as naDesp, desp.abe01id as idDesp, "+
                "redesp.abe01codigo as codRedesp, redesp.abe01na as naRedesp, redesp.abe01id as idRedesp, aah20codigo as codVeiculo, aah20nome as nomeVeiculo, "+
                "sum(eaa0103qtuso) as eaa0103qtuso, sum(eaa0103qtComl) as eaa0103qtcoml, sum(eaa0103unit) as eaa0103unit,  sum(eaa0103unit) as eaa0103unit, "+
                "sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc, sum(eaa0103totfinanc) as eaa0103totfinanc, sum(bfc01carvalor) as freteDesp, cast(eaa01json ->> 'valor_frete_redesp' as numeric(18,6)) as freteRedesp "+
                "from eaa01 "+
                "inner join abb01 on abb01id = eaa01central "+
                "inner join abe01 as ent on ent.abe01id = abb01ent "+
                "inner join eaa0102 on eaa0102doc = eaa01id "+
                "inner join abe01 as desp on desp.abe01id = eaa0102despacho "+
                "left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                "inner join aaj15 on aaj15id = eaa0103cfop  "+
                "inner join aah01 on aah01id = abb01tipo "+
                "left join abe30 on abe30id = eaa01cp "+
                "left join abe40 on abe40id = eaa01tp  "+
                "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1  "+
                "inner join aag0201 on aag0201id = abe0101municipio  "+
                "inner join aag02 on aag02id = aag0201uf  "+
                "left join bfc1002 on bfc1002central = abb01id "+
                "left join bfc10 on bfc10id = bfc1002carga "+
                "left join bfc1001 on bfc1001carga = bfc10id "+
                "left join bfc01 on bfc01id = bfc1001oco "+
                "left join aah20 on aah20id = eaa0102veiculo  "+
                "where eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                " and eaa01cancData is null "+
                "and eaa01nfestat <> 5 "+
                whereEmpresa+
                whereNumIni+
                whereNumFin+
                whereTipoDoc+
                wherePcd+
                whereDtEmissao+
                whereDtEntradaSaida+
                whereEntidade+
                whereES+
                whereItens+
                whereTransp+
                "group by aag02uf,aag0201nome,aah20placa,eaa01id,abb01num,abb01data, eaa01esdata, ent.abe01codigo, ent.abe01na,aah20codigo,aah20nome, "+
                "eaa01rep0,aah01codigo,abe30nome,abe40nome,eaa01json, desp.abe01codigo, desp.abe01na, desp.abe01id, redesp.abe01codigo, redesp.abe01na, redesp.abe01id " + orderBy ;


        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEmpresa,parametroNumIni,parametroNumFin,parametroResumo,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,parametroDtNascIni,
                parametroDtNascFin,parametroEstados,parametroMunicipios,parametroItens,parametroTransp);

    }



    private List<Long> obterItensDoc(Long eaa01id) {
        def sql = " select eaa0103id from eaa0103 where eaa0103doc = :id "
        def p1 =criarParametroSql("id", eaa01id)
        return getAcessoAoBanco().obterListaDeLong(sql, p1)
    }

    private TableMap buscarDevolucao(Long idItem){

        def whereItem = " where eaa01033itemdoc = :item "

        def sql = " select eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103totfinanc, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json "+
                " from eaa01033 "+
                " inner join eaa0103 on eaa0103id = eaa01033item  "+
                whereItem

        def p1 = criarParametroSql("item", idItem)
        return getAcessoAoBanco().buscarUnicoTableMap(sql,p1)

    }


    private buscarNomeCampoFixo(String campo){
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


    public String buscarNomeCampoLivre(String campo) {
        def sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

    }
}
//meta-sis-eyJkZXNjciI6IlNSRiBBbsOhbGlzZSBkZSBGcmV0ZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==