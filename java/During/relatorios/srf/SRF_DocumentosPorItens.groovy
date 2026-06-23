package During.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap
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


public class SRF_DocumentosPorItens extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF Documentos Por Itens";
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
        List<Long> idsItens = getListLong("itens");
        Integer resumoOperacao = getInteger("resumoOperacao");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idsTransps = getListLong("transportadora");
        List<Integer> mps = getListInteger("mps");
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
        params.put("title","Documento por Itens");
        params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
        params.put("periodo",periodo);

        if(campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if(campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")

        Map<String,String> campos = new HashMap();

        campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null   )
        campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null   )
        campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null   )
        campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null   )
        campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null   )
        campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null   )
  

        List<TableMap> documentos = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,
                                                        optionResumo,idsItens,idEmpresa,idsTransps,mps);

        for(documento in documentos){
            Long idItem = documento.getLong("eaa0103id");

            if(chkDevolucao){
                TableMap devolucoes = buscarDevolucoes(idItem,campoLivre1,campoLivre2,campoLivre3,campoLivre4,campoLivre5,campoLivre6);
                comporDevolucoes(documento,devolucoes,[campoLivre1,campoLivre2,campoLivre3,campoLivre4,campoLivre5,campoLivre6])
            }
            comporValores(documento, campos)
        }
      

        if(impressao == 0) return gerarPDF("SRF_DocumentosPorItens",documentos);      
        return gerarXLSX("SRF_DocumentosPorItens(Excel)",documentos); 
    }
    
		

    private buscarDocumentos(Integer numDocIni, Integer numDocFin,List<Long>idsTipoDoc,List<Long> idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao,
                                    LocalDate[] dtEntradaSaida,List<Long> idsEntidades,Integer optionResumo,List<Long>idsItens,Long idEmpresa,List<Long> idTransp, List<Integer> mps){
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
        String whereTransp = (idTransp != null && idTransp.size() > 0) && optionTransp == 0 ? "and desp.abe01id in (:idTransp) " : (idTransp != null && idTransp.size() > 0) && optionTransp == 1 ?"and redesp.abe01id in (:idTransp) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01nota.eaa01esMov = 1 " : " and eaa01nota.eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "AND eaa01nota.eaa01gc = :idEmpresa ";
        String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";

        String campo1 = campoLivre1 != null ? "SUM(CAST(eaa0103nota.eaa0103json ->> '"+campoLivre1+"'"+" as NUMERIC(18,2))) AS " + campoLivre1 + ",  " : "";
        String campo2 = campoLivre2 != null ? "SUM(CAST(eaa0103nota.eaa0103json ->> '"+campoLivre2+"'"+" as NUMERIC(18,2))) AS " + campoLivre2 + ", " : "";
        String campo3 = campoLivre3 != null ? "SUM(CAST(eaa0103nota.eaa0103json ->> '"+campoLivre3+"'"+" as NUMERIC(18,2))) AS " + campoLivre3 + ", "  : "";
        String campo4 = campoLivre4 != null ? "SUM(CAST(eaa0103nota.eaa0103json ->> '"+campoLivre4+"'"+" as NUMERIC(18,2))) AS " + campoLivre4 + ", "  : "";
        String campo5 = campoLivre5 != null ? "SUM(CAST(eaa0103nota.eaa0103json ->> '"+campoLivre5+"'"+" as NUMERIC(18,2))) AS " + campoLivre5 + ", "  : "";
        String campo6 = campoLivre6 != null ? "SUM(CAST(eaa0103nota.eaa0103json ->> '"+campoLivre6+"'"+" as NUMERIC(18,2))) AS " + campoLivre6 + ", "  : "";
        

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
        Parametro parametroMps =  mps != null && !mps.contains(-1) ?  Parametro.criar("mps",mps) : null;

        String sql = "select " + campo1 + campo2 + campo3 + campo4 + campo5 + campo6 + "abm01id, abm01codigo, abm01descr,abm01tipo,aah01codigo,abb01nota.abb01num as numNota,abb01nota.abb01data as dataNota,eaa01nota.eaa01esdata as esDataNota, eaa0103nota.eaa0103id as eaa0103id,\n" +
                "aaj15codigo, ent.abe01codigo as codEnt, ent.abe01na as naEnt,abb01pedido.abb01num as numPed, " +
                "sum(eaa0103nota.eaa0103qtuso) as eaa0103qtuso, sum(eaa0103nota.eaa0103qtComl) as eaa0103qtcoml, sum(eaa0103nota.eaa0103unit) as eaa0103unit,\n" +
                "sum(eaa0103nota.eaa0103unit) as eaa0103unit, sum(eaa0103nota.eaa0103total) as eaa0103total, sum(eaa0103nota.eaa0103totdoc) as eaa0103totdoc,\n" +
                "sum(eaa0103nota.eaa0103totfinanc) as eaa0103totfinanc  \n" +
                "from eaa01 as eaa01nota  \n" +
                "inner join abb01 as abb01nota on abb01id = eaa01nota.eaa01central  \n" +
                "inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent  \n" +
                "inner join eaa0102 on eaa0102doc = eaa01nota.eaa01id  \n" +
                "left join abe01 as desp on desp.abe01id = eaa0102despacho  \n" +
                "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id  \n" +
                "inner join abm01 on abm01id = eaa0103nota.eaa0103item \n" +
                "left join aam06 on aam06id = abm01umu \n" +
                "left join aaj15 on aaj15id = eaa0103nota.eaa0103cfop  \n" +
                "left join aah01 on aah01id = abb01nota.abb01tipo  \n" +
                "left join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1  \n" +
                "left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id \n" +
                "left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv \n" +
                "left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc \n" +
                "left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central "+
                "where eaa01nota.eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                " and eaa01nota.eaa01cancData is null "+
                "and eaa01nota.eaa01nfestat <> 5 "+
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
                whereMps +
                "group by abm01id,eaa01nota.eaa01id, abm01codigo, abm01descr,abm01tipo,aam06codigo,aah01codigo,abb01nota.abb01num,abb01nota.abb01data,eaa01nota.eaa01esdata,aaj15codigo, eaa0103nota.eaa0103id, "+
                "ent.abe01codigo, ent.abe01na,abb01pedido.abb01num ";


        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEmpresa,parametroNumIni,parametroNumFin,parametroResumo,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,parametroDtNascIni,
                parametroDtNascFin,parametroEstados,parametroMunicipios,parametroItens,parametroTransp,parametroMps);
        
    }

    private TableMap buscarDevolucoes(Long idItem, String campoLivre1,String campoLivre2,String campoLivre3,String campoLivre4,String campoLivre5,String campoLivre6){

        String whereIdItem = "where eaa01033itemDoc = :idItem ";

        String campo1 = campoLivre1 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre1+"'"+" as NUMERIC(18,2))) AS " + campoLivre1 + ",  " : "";
        String campo2 = campoLivre2 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre2+"'"+" as NUMERIC(18,2))) AS " + campoLivre2 + ", " : "";
        String campo3 = campoLivre3 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre3+"'"+" as NUMERIC(18,2))) AS " + campoLivre3 + ", "  : "";
        String campo4 = campoLivre4 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre4+"'"+" as NUMERIC(18,2))) AS " + campoLivre4 + ", "  : "";
        String campo5 = campoLivre5 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre5+"'"+" as NUMERIC(18,2))) AS " + campoLivre5 + ", "  : "";
        String campo6 = campoLivre6 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre6+"'"+" as NUMERIC(18,2))) AS " + campoLivre6 + ", "  : "";

        String sql = "SELECT " + campo1 + campo2 + campo3 + campo4 + campo5 + campo6 +
                "SUM(eaa01033qtComl) as eaa0103qtcoml, SUM(eaa01033qtUso) as eaa01033qtuso,  " +
                "sum(eaa0103qtuso) as eaa0103qtuso,sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , " +
                "sum(eaa0103totfinanc) as eaa0103totfinanc  " +
                "from eaa01033 " +
                "inner join eaa0103 on eaa0103id = eaa01033item "+
                whereIdItem;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,Parametro.criar("idItem",idItem))
    }

    private void comporDevolucoes(TableMap dado, TableMap devolucoes, List<String> camposLivres){
        for(campo in camposLivres){
            if(campo != null){
                dado.put(campo, dado.getBigDecimal_Zero(campo) - devolucoes.getBigDecimal_Zero(campo));
            }
        }

        for(campo in ["eaa0103qtuso","eaa0103qtcoml","eaa0103unit","eaa0103total","eaa0103totdoc","eaa0103totfinanc"]){
            dado.put(campo, dado.getBigDecimal_Zero(campo) - devolucoes.getBigDecimal_Zero(campo))
        }
    }

    private void comporValores(TableMap dado,Map<String,String>campos){
        for(campo in campos){
            if(campo.value != null){
                String nomeCampo = buscarNomeCampoFixo(campo.value);
                if(nomeCampo != null){
                    dado.put("nomeCampo" + campo.key, campo.value);
                    dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(nomeCampo));
                }else{
                    nomeCampo = buscarNomeCampoLivre(campo.value)
                    dado.put("nomeCampo" + campo.key, nomeCampo);
                    dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value));
                }
            }
        }
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
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBJdGVucyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBJdGVucyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==