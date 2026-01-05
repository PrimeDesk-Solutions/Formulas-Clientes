package Fast.relatorios.srf;

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

        HashMap<String, String> campos = new HashMap<>()

        campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null);
        campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null);
        campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null);
        campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null);
        campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null);
        campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null);

        List<TableMap> representantes = buscarRepresentantes(idsReps);
        List<TableMap> documentos = new ArrayList<>()
        for(representante in representantes){
            Long idRep = representante.getLong("abe01id");
            String codRep = representante.getString("abe01codigo");
            String nomeRep = representante.getString("abe01na");
            def txComissao = representante.getBigDecimal_Zero("abe05taxa")

            List<TableMap> dados = buscarDadosDocumentos(idRep, numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida,
                                                            idsEntidades, campoLivre1, campoLivre2, campoLivre3, campoLivre4, campoLivre5, campoLivre6);

            for(dado in dados){
                if(chkDevolucao){
                    comporDevolucoes(dado, [campoLivre1, campoLivre2, campoLivre3, campoLivre4, campoLivre5, campoLivre6]);
                }
                dado.put("codRep", codRep);
                dado.put("naRep", nomeRep);
                dado.put("txComis", txComissao );

                comporValores(dado, campos);
                documentos.add(dado)
            }
        }


      
        if(impressao == 0) return gerarPDF("SRF_DocumentosPorRepresentantes(PDF)",documentos);
        return gerarXLSX("SRF_DocumentosPorRepresentantes(Excel)",documentos); 
    }

    private List<TableMap> buscarDadosDocumentos(Long idRepresentante, Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida,
                                                 List<Long> idsEntidades, String campoLivre1, String campoLivre2, String campoLivre3, String campoLivre4, String campoLivre5, campoLivre6){
        // Data Emissao
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null
        if(dtEmissao != null){
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        // Data Entrada Saída
        LocalDate esDataIni = null;
        LocalDate esDataFin = null
        if(dtEntradaSaida != null){
            esDataIni = dtEntradaSaida[0];
            esDataFin = dtEntradaSaida[1];
        }

        String whereNumDoc = "where abb01nota.abb01num between :numDocIni and :numDocFin ";
        String whereMov = resumoOperacao == 0 ? "and eaa01nota.eaa01esmov = 0 " : "and eaa01nota.eaa01esmov = 1 ";
        String whereClasDoc = "and eaa01nota.eaa01clasdoc = 1 "
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01nota.aah01id in (:idsTipoDoc) " : "";
        String wherePCD = idsPcd != null && idsPcd.size() > 0 ? "and eaa01nota.eaa01pcd in () " : "";
        String whereDataEmissao = dtEmissao != null ? "and abb01nota.abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "and eaa01nota.eaa01esdata between :esDataIni and :esDataFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
        String whereRepresentantes = "and (eaa01nota.eaa01rep0 = :idRepresentante or eaa01nota.eaa01rep1 = :idRepresentante or eaa01nota.eaa01rep2 = :idRepresentante or eaa01nota.eaa01rep3 = :idRepresentante or eaa01nota.eaa01rep4 = :idRepresentante) ";

        Parametro  parametroNumDocIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro  parametroNumDocFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro  parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro  parametroPCD = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro  parametroDataEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro  parametroDataEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro  parametroDataEntradaSaidaIni = dtEntradaSaida != null ? Parametro.criar("esDataIni", esDataIni) : null;
        Parametro  parametroDataEntradaSaidaFin = dtEntradaSaida != null ? Parametro.criar("esDataFin", esDataFin) : null;
        Parametro  parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro  parametroRepresentantes = Parametro.criar("idRepresentante", idRepresentante);

        String campo1 = campoLivre1 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre1+"'" + " as NUMERIC(18,2))) AS " + campoLivre1 + ",  " : "";
        String campo2 = campoLivre2 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre2+"'" + " as NUMERIC(18,2))) AS " + campoLivre2 + ", " : "";
        String campo3 = campoLivre3 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre3+"'" + " as NUMERIC(18,2))) AS " + campoLivre3 + ", "  : "";
        String campo4 = campoLivre4 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre4+"'" + " as NUMERIC(18,2))) AS " + campoLivre4 + ", "  : "";

        String campoDev1 = campoLivre1 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre1+"'" + " as NUMERIC(18,2))) AS " + campoLivre1 + "dev"+ ",  " : "";
        String campoDev2 = campoLivre2 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre2+"'" + " as NUMERIC(18,2))) AS " + campoLivre2 + "dev"+ ", " : "";
        String campoDev3 = campoLivre3 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre3+"'" + " as NUMERIC(18,2))) AS " + campoLivre3 + "dev"+ ", "  : "";
        String campoDev4 = campoLivre4 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre4+"'" + " as NUMERIC(18,2))) AS " + campoLivre4 + "dev"+ ", "  : "";


        String sql =    "select "+campo1 + campo2 + campo3 + campo4 + campoDev1 + campoDev2+ campoDev3+ campoDev4 + " aah01nota.aah01codigo as tipoDocNota, abb01nota.abb01num as numNota, abb01nota.abb01data as dtEmissaoNota, eaa01nota.eaa01esdata as esDataNota,\n" +
                        "aah01pedido.aah01codigo as tipoDocPedido,abb01pedido.abb01num as numPedido, abe01codigo as codEnt,abe01na as naEnt, abe30codigo as codCondPgto, abe30nome as nomeCondPgto,\n" +
                        "abe40nome as tabelaPreco, SUM(eaa0103nota.eaa0103qtComl) as eaa0103qtcoml, SUM(eaa0103nota.eaa0103unit) as eaa0103unit, SUM(eaa0103nota.eaa0103total) as eaa0103total,\n" +
                        "SUM(eaa0103nota.eaa0103totDoc) as eaa0103totdoc, SUM(eaa0103nota.eaa0103totFinanc) as eaa0103totfinanc, SUM(eaa01033qtComl) as eaa0103qtcomldev,\n" +
                        "COALESCE(SUM(EAA01033qtComl),0) as eaa0103qtcomldev,\n" +
                        "COALESCE(SUM(eaa0103dev.eaa0103qtuso),0) as eaa0103qtusodev,\n" +
                        "COALESCE(SUM(eaa0103dev.eaa0103unit),0) as eaa0103unitdev,\n" +
                        "COALESCE(SUM(eaa0103dev.eaa0103total),0) as eaa0103totaldev,\n" +
                        "COALESCE(SUM(eaa0103dev.eaa0103totdoc),0) as eaa0103totdocdev,\n" +
                        "COALESCE(SUM(eaa0103dev.eaa0103totfinanc),0) as eaa0103totfinancdev\n" +
                        "from eaa01 as eaa01nota\n" +
                        "inner join abb01 as abb01nota on eaa01central = abb01nota.abb01id\n" +
                        "inner join aah01 as aah01nota on abb01nota.abb01tipo = aah01nota.aah01id\n" +
                        "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01id\n" +
                        "inner join eaa01032 on eaa01032itemsrf = eaa0103id\n" +
                        "inner join eaa0103 as eaa0103pedido on eaa01032itemscv = eaa0103pedido.eaa0103id\n" +
                        "inner join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc\n" +
                        "inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central\n" +
                        "inner join aah01 as aah01pedido on aah01pedido.aah01id = abb01pedido.abb01tipo\n" +
                        "inner join abe01 on abb01nota.abb01ent = abe01id\n" +
                        "inner join abe30 on eaa01nota.eaa01cp = abe30id\n" +
                        "inner join abe40 on eaa01nota.eaa01tp = abe40id\n" +
                        "left join eaa01033 on eaa01033itemdoc = eaa0103nota.eaa0103id\n" +
                        "left join eaa0103 as eaa0103dev on eaa0103dev.eaa0103id = eaa01033item  and eaa0103dev.eaa0103tipo <> 3\n" +
                        whereNumDoc +
                        whereMov +
                        whereTipoDoc +
                        wherePCD +
                        whereDataEmissao +
                        whereDataEntradaSaida +
                        whereEntidade +
                        whereRepresentantes+
                        whereClasDoc +
                        "group by aah01nota.aah01codigo, abb01nota.abb01num, abb01nota.abb01data, eaa01nota.eaa01esdata,\n" +
                        "aah01pedido.aah01codigo,abb01pedido.abb01num, abe01codigo,abe01na, abe30codigo, abe30nome,\n" +
                        "abe40nome "+
                        "order by abb01nota.abb01num ";


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroTipoDoc, parametroPCD, parametroDataEmissaoIni, parametroDataEmissaoFin,
                parametroDataEntradaSaidaIni, parametroDataEntradaSaidaFin, parametroEntidade, parametroRepresentantes  );


    }

    private void comporDevolucoes(TableMap dado, List<String> camposLivres){
        for(campo in camposLivres){
            if(campo != null){
                dado.put(campo, dado.getBigDecimal_Zero(campo) - dado.getBigDecimal_Zero(campo + "dev"));
            }
        }

        for(campo in ["eaa0103qtuso", "eaa0103qtcoml", "eaa0103unit", "eaa0103total", "eaa0103totdoc", "eaa0103totfinanc" ]){
            dado.put(campo, dado.getBigDecimal_Zero(campo) - dado.getBigDecimal_Zero(campo + "dev"));
        }
    }

    private List<TableMap> buscarRepresentantes(List<Long> ids){
        String whereReps = ids != null && ids.size() > 0 ? "and abe01id in (:ids) " : "";
        
        String sql =   " select abe01id,abe01na, abe01codigo, abe05taxa " +
                        " from abe01 "+
                        " left join abe05 on abe05ent = abe01id "+
                        " where abe01rep = 1  "+
                        whereReps +
                        " order by abe01codigo";
        
        Parametro p1 = Parametro.criar("ids",ids);

        return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
    }

    private void comporValores(TableMap dado, HashMap<String, String> campos){
        for(campo in campos){
            if(campo.value != null){
                String nomeCampo = buscarNomeCampoFixo(campo.value)
                if(nomeCampo != null){
                    dado.put("nomeCampo"+campo.key, campo.value);
                    dado.put("valorCampo"+ campo.key, dado.getBigDecimal_Zero(nomeCampo))
                }else{
                    nomeCampo = buscarNomeCampoLivre(campo.value);
                    dado.put("nomeCampo" + campo.key, nomeCampo);
                    dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value))
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
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBSZXByZXNlbnRhbnRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBSZXByZXNlbnRhbnRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBSZXByZXNlbnRhbnRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==