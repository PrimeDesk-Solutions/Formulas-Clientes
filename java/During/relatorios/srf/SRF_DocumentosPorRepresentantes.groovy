package During.relatorios.srf

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


public class SRF_DocumentosPorRepresentantes extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF Documentos Por Representantes";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
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
        List<Long> idsReps = getListLong("reps");
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
        params.put("title","Documento por Entidades");
        params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
        params.put("periodo",periodo);

        if(campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")

        List<Long> representantes = buscarRepresentantes(idsReps);
        
        List<TableMap> documentos = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,
                                                        optionResumo,idsItens,idEmpresa,idsReps);
        List<TableMap> teste = new ArrayList();
        

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
        
        
       

      
        if(impressao == 0) return gerarPDF("SRF_DocumentosPorRepresentantes",documentos);      
        return gerarXLSX("SRF_DocumentosPorRepresentantes(Excel)",documentos); 
    }

    private List<Long> buscarRepresentantes(List<Long> ids){
        String whereReps = ids != null && ids.size() > 0 ? "and abe01id in (:ids) " : "";
        
        String sql =   "select abe01id,abe01na from abe01 where abe01rep = 1  "+ whereReps;
        
        Parametro p1 = Parametro.criar("ids",ids);

        return getAcessoAoBanco().obterListaDeLong(sql,p1)
    }
    
		

    private buscarDocumentos(Integer numDocIni, Integer numDocFin,List<Long>idsTipoDoc,List<Long> idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao, 
                                    LocalDate[] dtEntradaSaida,List<Long> idsEntidades,Integer optionResumo,List<Long>idsItens,Long idEmpresa,List<Long>idsReps){
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

        String whereNumIni = numDocIni != null ? "and abb01nota.abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01nota.abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01nota.abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01nota.eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereReps = idsReps != null && idsReps.size() > 0 ? "abe01id in (:idsReps) " : "";
        String whereTransp = (idTransp != null && idTransp.size() > 0) && optionTransp == 0 ? "and desp.abe01id in (:idTransp) " : (idTransp != null && idTransp.size() > 0) && optionTransp == 1 ?"and redesp.abe01id in (:idTransp) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01nota.eaa01esMov = 1 " : " and eaa01nota.eaa01esMov = 0";
        String whereEmpresa = "AND eaa01nota.eaa01gc = :idEmpresa ";

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroReps = idsReps != null && idsReps.size() > 0 ? Parametro.criar("idsReps", idsReps) : null;
        Parametro parametroTransp = idTransp != null && idTransp.size() > 0 ? Parametro.criar("idTransp", idTransp) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
        

        String sql = "select eaa01nota.eaa01id as eaa01id, eaa01nota.eaa01txcomis0 as txComis,aah01codigo,abb01nota.abb01num as numNota,abb01nota.abb01data as dataNota,eaa01nota.eaa01esdata as esDataNota,aaj15codigo,  "+
                        "ent.abe01codigo as codEnt, ent.abe01na as naEnt,abe30nome,abe40nome,eaa01nota.eaa01rep0 as repNota,abb01pedido.abb01num as numPed,eaa01nota.eaa01json as eaa01json,  "+
                        "rep0.abe01na as naRep,rep0.abe01codigo as codRep,rep0.abe01id as idRep, "+
                        "sum(eaa0103nota.eaa0103qtuso) as eaa0103qtuso, sum(eaa0103nota.eaa0103qtComl) as eaa0103qtcoml, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103total) as eaa0103total, sum(eaa0103nota.eaa0103totdoc) as eaa0103totdoc, sum(eaa0103nota.eaa0103totfinanc) as eaa0103totfinanc   "+
                        "from eaa01 as eaa01nota   "+
                        "inner join abb01 as abb01nota on abb01id = eaa01nota.eaa01central   "+
                        "inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent   "+
                        "inner join eaa0102 on eaa0102doc = eaa01nota.eaa01id   "+
                        "inner join abe01 as desp on desp.abe01id = eaa0102despacho   "+
                        "left join abe01 as redesp on redesp.abe01id = eaa0102redespacho   "+
                        "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id   "+
                        "inner join abm01 on abm01id = eaa0103nota.eaa0103item  "+
                        "inner join aam06 on aam06id = abm01umu  "+
                        "inner join aaj15 on aaj15id = eaa0103nota.eaa0103cfop   "+
                        "inner join aah01 on aah01id = abb01nota.abb01tipo   "+
                        "inner join abe30 on abe30id = eaa01nota.eaa01cp   "+
                        "left join abe40 on abe40id = eaa01nota.eaa01tp   "+
                        "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1   "+
                        "inner join aag0201 on aag0201id = abe0101municipio   "+
                        "inner join aag02 on aag02id = aag0201uf   "+
                        "left join aah20 on aah20id = eaa0102veiculo   "+
                        "inner join abe01 as rep0 on rep0.abe01id = eaa01rep0 "+
                        "left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id  "+
                        "left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv  "+
                        "left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc  "+
                        "left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central  "+
                        "where eaa01nota.eaa01clasDoc =  "+Eaa01.CLASDOC_SRF +
                        "and eaa01nota.eaa01cancData is null  "+
                        "and eaa01nota.eaa01nfestat <> 5  "+
                       (idsReps != null && idsReps.size() > 0 ? "and rep0."+whereReps : "") +
                        whereEmpresa+ 
                        whereNumIni+ 
                        whereNumFin+ 
                        whereTipoDoc+ 
                        wherePcd+ 
                        whereDtEmissao+ 
                        whereDtEntradaSaida+ 
                        whereEntidade+ 
                        whereES+ 
                        "group by rep0.abe01na,rep0.abe01codigo,rep0.abe01id,  "+
                        "eaa01nota.eaa01id,abb01nota.abb01num,abb01nota.abb01data, eaa01nota.eaa01esdata, ent.abe01codigo,  "+
                        "ent.abe01na,eaa01nota.eaa01txcomis0,aah01codigo,abe30nome,abe40nome,aaj15codigo,abb01pedido.abb01num,eaa01nota.eaa01json "+
                        "union all "+
                        "select eaa01nota.eaa01id as eaa01id, eaa01nota.eaa01txcomis1 as txComis,aah01codigo,abb01nota.abb01num as numNota,abb01nota.abb01data as dataNota,eaa01nota.eaa01esdata as esDataNota,aaj15codigo,  "+
                        "ent.abe01codigo as codEnt, ent.abe01na as naEnt,abe30nome,abe40nome,eaa01nota.eaa01rep0 as repNota,abb01pedido.abb01num as numPed,eaa01nota.eaa01json as eaa01json,  "+
                        "rep1.abe01na as naRep,rep1.abe01codigo as codRep,rep1.abe01id as idRep, "+
                        "sum(eaa0103nota.eaa0103qtuso) as eaa0103qtuso, sum(eaa0103nota.eaa0103qtComl) as eaa0103qtcoml, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103total) as eaa0103total, sum(eaa0103nota.eaa0103totdoc) as eaa0103totdoc, sum(eaa0103nota.eaa0103totfinanc) as eaa0103totfinanc   "+
                        "from eaa01 as eaa01nota   "+
                        "inner join abb01 as abb01nota on abb01id = eaa01nota.eaa01central   "+
                        "inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent   "+
                        "inner join eaa0102 on eaa0102doc = eaa01nota.eaa01id   "+
                        "inner join abe01 as desp on desp.abe01id = eaa0102despacho   "+
                        "left join abe01 as redesp on redesp.abe01id = eaa0102redespacho   "+
                        "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id   "+
                        "inner join abm01 on abm01id = eaa0103nota.eaa0103item  "+
                        "inner join aam06 on aam06id = abm01umu  "+
                        "inner join aaj15 on aaj15id = eaa0103nota.eaa0103cfop   "+
                        "inner join aah01 on aah01id = abb01nota.abb01tipo   "+
                        "inner join abe30 on abe30id = eaa01nota.eaa01cp   "+
                        "left join abe40 on abe40id = eaa01nota.eaa01tp   "+
                        "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1   "+
                        "inner join aag0201 on aag0201id = abe0101municipio   "+
                        "inner join aag02 on aag02id = aag0201uf   "+
                        "left join aah20 on aah20id = eaa0102veiculo   "+
                        "inner join abe01 as rep1 on rep1.abe01id = eaa01rep1 "+
                        "left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id  "+
                        "left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv  "+
                        "left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc  "+
                        "left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central  "+
                        "where eaa01nota.eaa01clasDoc =  "+Eaa01.CLASDOC_SRF +
                        "and eaa01nota.eaa01cancData is null  "+
                        "and eaa01nota.eaa01nfestat <> 5  "+
                        (idsReps != null && idsReps.size() > 0 ? "and rep1."+whereReps : "") +
                        whereEmpresa+ 
                        whereNumIni+ 
                        whereNumFin+ 
                        whereTipoDoc+ 
                        wherePcd+ 
                        whereDtEmissao+ 
                        whereDtEntradaSaida+ 
                        whereEntidade+ 
                        whereES+ 
                        "group by rep1.abe01na,rep1.abe01codigo,rep1.abe01id,  "+
                        "eaa01nota.eaa01id,abb01nota.abb01num,abb01nota.abb01data, eaa01nota.eaa01esdata, ent.abe01codigo,  "+
                        "ent.abe01na,eaa01nota.eaa01txcomis1,aah01codigo,abe30nome,abe40nome,aaj15codigo,abb01pedido.abb01num,eaa01nota.eaa01json "+
                        "union all "+
                        "select eaa01nota.eaa01id as eaa01id, eaa01nota.eaa01txcomis2 as txComis,aah01codigo,abb01nota.abb01num as numNota,abb01nota.abb01data as dataNota,eaa01nota.eaa01esdata as esDataNota,aaj15codigo,  "+
                        "ent.abe01codigo as codEnt, ent.abe01na as naEnt,abe30nome,abe40nome,eaa01nota.eaa01rep0 as repNota,abb01pedido.abb01num as numPed,eaa01nota.eaa01json as eaa01json,  "+
                        "rep2.abe01na as naRep,rep2.abe01codigo as codRep,rep2.abe01id as idRep, "+
                        "sum(eaa0103nota.eaa0103qtuso) as eaa0103qtuso, sum(eaa0103nota.eaa0103qtComl) as eaa0103qtcoml, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103total) as eaa0103total, sum(eaa0103nota.eaa0103totdoc) as eaa0103totdoc, sum(eaa0103nota.eaa0103totfinanc) as eaa0103totfinanc   "+
                        "from eaa01 as eaa01nota   "+
                        "inner join abb01 as abb01nota on abb01id = eaa01nota.eaa01central   "+
                        "inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent   "+
                        "inner join eaa0102 on eaa0102doc = eaa01nota.eaa01id   "+
                        "inner join abe01 as desp on desp.abe01id = eaa0102despacho   "+
                        "left join abe01 as redesp on redesp.abe01id = eaa0102redespacho   "+
                        "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id   "+
                        "inner join abm01 on abm01id = eaa0103nota.eaa0103item  "+
                        "inner join aam06 on aam06id = abm01umu  "+
                        "inner join aaj15 on aaj15id = eaa0103nota.eaa0103cfop   "+
                        "inner join aah01 on aah01id = abb01nota.abb01tipo   "+
                        "inner join abe30 on abe30id = eaa01nota.eaa01cp   "+
                        "left join abe40 on abe40id = eaa01nota.eaa01tp   "+
                        "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1   "+
                        "inner join aag0201 on aag0201id = abe0101municipio   "+
                        "inner join aag02 on aag02id = aag0201uf   "+
                        "left join aah20 on aah20id = eaa0102veiculo   "+
                        "inner join abe01 as rep2 on rep2.abe01id = eaa01rep2 "+
                        "left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id  "+
                        "left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv  "+
                        "left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc  "+
                        "left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central  "+
                        "where eaa01nota.eaa01clasDoc =  "+Eaa01.CLASDOC_SRF +
                        "and eaa01nota.eaa01cancData is null  "+
                        "and eaa01nota.eaa01nfestat <> 5  "+
                        (idsReps != null && idsReps.size() > 0 ? "and rep2."+whereReps : "") +
                        whereEmpresa+ 
                        whereNumIni+ 
                        whereNumFin+ 
                        whereTipoDoc+ 
                        wherePcd+ 
                        whereDtEmissao+ 
                        whereDtEntradaSaida+ 
                        whereEntidade+ 
                        whereES+ 
                        "group by rep2.abe01na,rep2.abe01codigo,rep2.abe01id,  "+
                        "eaa01nota.eaa01id,abb01nota.abb01num,abb01nota.abb01data, eaa01nota.eaa01esdata, ent.abe01codigo,  "+
                        "ent.abe01na,eaa01nota.eaa01txcomis2,aah01codigo,abe30nome,abe40nome,aaj15codigo,abb01pedido.abb01num,eaa01nota.eaa01json "+
                        "union all "+
                        "select eaa01nota.eaa01id as eaa01id, eaa01nota.eaa01txcomis3 as txComis,aah01codigo,abb01nota.abb01num as numNota,abb01nota.abb01data as dataNota,eaa01nota.eaa01esdata as esDataNota,aaj15codigo,  "+
                        "ent.abe01codigo as codEnt, ent.abe01na as naEnt,abe30nome,abe40nome,eaa01nota.eaa01rep0 as repNota,abb01pedido.abb01num as numPed,eaa01nota.eaa01json as eaa01json,  "+
                        "rep3.abe01na as naRep,rep3.abe01codigo as codRep,rep3.abe01id as idRep, "+
                        "sum(eaa0103nota.eaa0103qtuso) as eaa0103qtuso, sum(eaa0103nota.eaa0103qtComl) as eaa0103qtcoml, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103total) as eaa0103total, sum(eaa0103nota.eaa0103totdoc) as eaa0103totdoc, sum(eaa0103nota.eaa0103totfinanc) as eaa0103totfinanc   "+
                        "from eaa01 as eaa01nota   "+
                        "inner join abb01 as abb01nota on abb01id = eaa01nota.eaa01central   "+
                        "inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent   "+
                        "inner join eaa0102 on eaa0102doc = eaa01nota.eaa01id   "+
                        "inner join abe01 as desp on desp.abe01id = eaa0102despacho   "+
                        "left join abe01 as redesp on redesp.abe01id = eaa0102redespacho   "+
                        "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id   "+
                        "inner join abm01 on abm01id = eaa0103nota.eaa0103item  "+
                        "inner join aam06 on aam06id = abm01umu  "+
                        "inner join aaj15 on aaj15id = eaa0103nota.eaa0103cfop   "+
                        "inner join aah01 on aah01id = abb01nota.abb01tipo   "+
                        "inner join abe30 on abe30id = eaa01nota.eaa01cp   "+
                        "left join abe40 on abe40id = eaa01nota.eaa01tp   "+
                        "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1   "+
                        "inner join aag0201 on aag0201id = abe0101municipio   "+
                        "inner join aag02 on aag02id = aag0201uf   "+
                        "left join aah20 on aah20id = eaa0102veiculo   "+
                        "inner join abe01 as rep3 on rep3.abe01id = eaa01rep3 "+
                        "left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id  "+
                        "left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv  "+
                        "left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc  "+
                        "left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central  "+
                        "where eaa01nota.eaa01clasDoc =  "+Eaa01.CLASDOC_SRF +
                        "and eaa01nota.eaa01cancData is null  "+
                        "and eaa01nota.eaa01nfestat <> 5  "+
                       (idsReps != null && idsReps.size() > 0 ? "and rep3."+whereReps : "") +
                        whereEmpresa+ 
                        whereNumIni+ 
                        whereNumFin+ 
                        whereTipoDoc+ 
                        wherePcd+ 
                        whereDtEmissao+ 
                        whereDtEntradaSaida+ 
                        whereEntidade+ 
                        whereES+ 
                        "group by rep3.abe01na,rep3.abe01codigo,rep3.abe01id,  "+
                        "eaa01nota.eaa01id,abb01nota.abb01num,abb01nota.abb01data, eaa01nota.eaa01esdata, ent.abe01codigo,  "+
                        "ent.abe01na,eaa01nota.eaa01txcomis3,aah01codigo,abe30nome,abe40nome,aaj15codigo,abb01pedido.abb01num,eaa01nota.eaa01json "+
                        "union all "+
                        "select eaa01nota.eaa01id as eaa01id, eaa01nota.eaa01txcomis3 as txComis4,aah01codigo,abb01nota.abb01num as numNota,abb01nota.abb01data as dataNota,eaa01nota.eaa01esdata as esDataNota,aaj15codigo,  "+
                        "ent.abe01codigo as codEnt, ent.abe01na as naEnt,abe30nome,abe40nome,eaa01nota.eaa01rep0 as repNota,abb01pedido.abb01num as numPed,eaa01nota.eaa01json as eaa01json,  "+
                        "rep4.abe01na as naRep,rep4.abe01codigo as codRep,rep4.abe01id as idRep, "+
                        "sum(eaa0103nota.eaa0103qtuso) as eaa0103qtuso, sum(eaa0103nota.eaa0103qtComl) as eaa0103qtcoml, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103total) as eaa0103total, sum(eaa0103nota.eaa0103totdoc) as eaa0103totdoc, sum(eaa0103nota.eaa0103totfinanc) as eaa0103totfinanc   "+
                        "from eaa01 as eaa01nota   "+
                        "inner join abb01 as abb01nota on abb01id = eaa01nota.eaa01central   "+
                        "inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent   "+
                        "inner join eaa0102 on eaa0102doc = eaa01nota.eaa01id   "+
                        "inner join abe01 as desp on desp.abe01id = eaa0102despacho   "+
                        "left join abe01 as redesp on redesp.abe01id = eaa0102redespacho   "+
                        "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id   "+
                        "inner join abm01 on abm01id = eaa0103nota.eaa0103item  "+
                        "inner join aam06 on aam06id = abm01umu  "+
                        "inner join aaj15 on aaj15id = eaa0103nota.eaa0103cfop   "+
                        "inner join aah01 on aah01id = abb01nota.abb01tipo   "+
                        "inner join abe30 on abe30id = eaa01nota.eaa01cp   "+
                        "left join abe40 on abe40id = eaa01nota.eaa01tp   "+
                        "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1   "+
                        "inner join aag0201 on aag0201id = abe0101municipio   "+
                        "inner join aag02 on aag02id = aag0201uf   "+
                        "left join aah20 on aah20id = eaa0102veiculo   "+
                        "inner join abe01 as rep4 on rep4.abe01id = eaa01rep4 "+
                        "left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id  "+
                        "left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv  "+
                        "left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc  "+
                        "left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central  "+
                        "where eaa01nota.eaa01clasDoc =  " + Eaa01.CLASDOC_SRF +
                        "and eaa01nota.eaa01cancData is null  "+
                        "and eaa01nota.eaa01nfestat <> 5  "+
                        (idsReps != null && idsReps.size() > 0 ? "and rep4."+whereReps : "") +
                        whereEmpresa+ 
                        whereNumIni+ 
                        whereNumFin+ 
                        whereTipoDoc+ 
                        wherePcd+ 
                        whereDtEmissao+ 
                        whereDtEntradaSaida+ 
                        whereEntidade+ 
                        whereES+ 
                        "group by rep4.abe01na,rep4.abe01codigo,rep4.abe01id,  "+
                        "eaa01nota.eaa01id,abb01nota.abb01num,abb01nota.abb01data, eaa01nota.eaa01esdata, ent.abe01codigo,  "+
                        "ent.abe01na,eaa01nota.eaa01txcomis4,aah01codigo,abe30nome,abe40nome,aaj15codigo,abb01pedido.abb01num,eaa01nota.eaa01json "
                
        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEmpresa,parametroNumIni,parametroNumFin,parametroResumo,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,
                parametroReps);
        
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
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBSZXByZXNlbnRhbnRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==