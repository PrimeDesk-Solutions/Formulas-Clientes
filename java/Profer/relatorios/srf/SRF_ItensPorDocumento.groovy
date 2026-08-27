package Profer.relatorios.srf

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


public class SRF_ItensPorDocumento extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Itens Por Documento";
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
        List<Long> idsTransps = getListLong("transportadoras");
        List<Long> idsNcm = getListLong("ncm");
        List<Long> idsDepartamentos = getListLong("departamentos");
        List<Long> idsItens = getListLong("itens");
        List<Integer> mps = getListInteger("mps");
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
        params.put("title","Itens Por Documentos");
        params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
        params.put("periodo",periodo);

        if(campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!");
        if(campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!");
        if(campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!");
        if(campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!");
        if(campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!");
        if(campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!");

        Map<String, String> campos = new HashMap();

        campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null);
        campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null);
        campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null);
        campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null);
        campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null);
        campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null);

        List<TableMap> documentos = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,
                                                        idsItens,idEmpresa,idsTransps,idsNcm, idsDepartamentos, mps, campoLivre1, campoLivre2, campoLivre3, campoLivre4, campoLivre5, campoLivre6 );

        List<TableMap> dados = new ArrayList()

        for(documento in documentos){
            if(chkDevolucao){
                comporDevolucao(documento, [campoLivre1, campoLivre2, campoLivre3,campoLivre4, campoLivre5, campoLivre6]);
            }

            buscarCampos(documento, campos);

            dados.add(documento);

        }

        if(impressao == 0) return gerarPDF("SRF_ItensPorDocumentos(PDF)",dados);
        return gerarXLSX("SRF_ItensPorDocumentos(Excel)",documentos);
    }   
    
		

    private buscarDocumentos(Integer numDocIni, Integer numDocFin,List<Long>idsTipoDoc,List<Long> idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao, 
                                    LocalDate[] dtEntradaSaida,List<Long> idsEntidades,List<Long>idsItens,Long idEmpresa,List<Long> idTransp,List<Long>idsNcm, List<Long>idsDepartamentos,List<Integer> mps, String campoLivre1, String campoLivre2, String campoLivre3, String campoLivre4, String campoLivre5, String campoLivre6){
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


        String whereNumIni = numDocIni != null ? "and abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereTransp = idTransp != null && idTransp.size() > 0 ? "and desp.abe01id in (:idTransp) " :"";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0 ";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereNcm = idsNcm != null && idsNcm.size() > 0 ? "and abg01id in (:idsNcm) " : "";
        String whereDepartamento = idsDepartamentos != null && idsDepartamentos.size() > 0 ? "and abb11id in (:departamentos) " : "";
        String whereMPS = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";

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
        Parametro parametroNcm = idsNcm != null && idsNcm.size() > 0 ? Parametro.criar("idsNcm",idsNcm) : null;                
        Parametro parametroDepto = idsDepartamentos != null && idsDepartamentos.size() > 0 ? Parametro.criar("departamentos",idsDepartamentos) : null;
        Parametro parametroMPS =  mps.size() > 0 && !mps.contains(-1) ? Parametro.criar("mps",mps) : null;

        String campo1 = campoLivre1 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre1+"'" + " as NUMERIC(18,2))) AS " + campoLivre1 + ",  " : "";
        String campo2 = campoLivre2 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre2+"'" + " as NUMERIC(18,2))) AS " + campoLivre2 + ", " : "";
        String campo3 = campoLivre3 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre3+"'" + " as NUMERIC(18,2))) AS " + campoLivre3 + ", "  : "";
        String campo4 = campoLivre4 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre4+"'" + " as NUMERIC(18,2))) AS " + campoLivre4 + ", "  : "";
        String campo5 = campoLivre5 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre5+"'" + " as NUMERIC(18,2))) AS " + campoLivre5 + ", "  : "";
        String campo6 = campoLivre6 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + campoLivre6+"'" + " as NUMERIC(18,2))) AS " + campoLivre6 + ", "  : "";

        String campoDev1 = campoLivre1 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre1+"'" + " as NUMERIC(18,2))) AS " + campoLivre1 + "dev"+ ",  " : "";
        String campoDev2 = campoLivre2 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre2+"'" + " as NUMERIC(18,2))) AS " + campoLivre2 + "dev"+ ", " : "";
        String campoDev3 = campoLivre3 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre3+"'" + " as NUMERIC(18,2))) AS " + campoLivre3 + "dev"+ ", "  : "";
        String campoDev4 = campoLivre4 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre4+"'" + " as NUMERIC(18,2))) AS " + campoLivre4 + "dev"+ ", "  : "";
        String campoDev5 = campoLivre5 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre5+"'" + " as NUMERIC(18,2))) AS " + campoLivre5 + "dev"+ ", "  : "";
        String campoDev6 = campoLivre6 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + campoLivre6+"'" + " as NUMERIC(18,2))) AS " + campoLivre6 + "dev"+ ", "  : "";


        String sql =    "select "+campo1 + campo2 + campo3 + campo4 + campoDev1 + campoDev2+ campoDev3+ campoDev4 +"aah01codigo, abb01num, abb01data, eaa01esdata, ent.abe01codigo as codEnt, " +
                        "ent.abe01na as naEnt, " +
                        "case when abm01tipo = 0 then 'M'  " +
                        "when abm01tipo = 1 then 'P'  " +
                        "when abm01tipo = 2 then 'S'  " +
                        "else 'MER' end as mps, abm01codigo, abm01na, aam06codigo,abe30codigo, abe30nome,abe40codigo, " +
                        "abe40nome,abg01codigo, SUM(eaa0103doc.eaa0103qtuso) as eaa0103qtuso,SUM(eaa0103doc.eaa0103qtComl) as eaa0103qtcoml, " +
                        "SUM(eaa0103doc.eaa0103unit) as eaa0103unit, SUM(eaa0103doc.eaa0103total) as eaa0103total, SUM(eaa0103doc.eaa0103totdoc) as eaa0103totdoc, " +
                        "SUM(eaa0103doc.eaa0103totfinanc) as eaa0103totfinanc, " +
                        "COALESCE(SUM(EAA01033qtComl),0) as eaa0103qtcomldev, " +
                        "COALESCE(SUM(eaa0103dev.eaa0103qtuso),0) as eaa0103qtusodev, " +
                        "COALESCE(SUM(eaa0103dev.eaa0103unit),0) as eaa0103unitdev, " +
                        "COALESCE(SUM(eaa0103dev.eaa0103total),0) as eaa0103totaldev, " +
                        "COALESCE(SUM(eaa0103dev.eaa0103totdoc),0) as eaa0103totdocdev, " +
                        "COALESCE(SUM(eaa0103dev.eaa0103totfinanc),0) as eaa0103totfinancdev " +
                        "from eaa01  " +
                        "inner join abd01 on abd01id = eaa01pcd  " +
                        "left join abb11 on abb11id = abd01depto  " +
                        "inner join abb01 on abb01id = eaa01central  " +
                        "inner join aah01 on aah01id = abb01tipo  " +
                        "inner join abe01 as ent on ent.abe01id = abb01ent  " +
                        "inner join eaa0103 as eaa0103doc on eaa0103doc.eaa0103doc = eaa01id  " +
                        "inner join abm01 on abm01id = eaa0103doc.eaa0103item  " +
                        "left join aam06 on aam06id = abm01umu  " +
                        "left join aaj15 on aaj15id = eaa0103doc.eaa0103cfop  " +
                        "left join abe30 on abe30id = eaa01cp  " +
                        "left join abe40 on abe40id = eaa01tp  " +
                        "left join abg01 on abg01id = eaa0103doc.eaa0103ncm  " +
                        "left join eaa01033 on eaa01033itemdoc = eaa0103doc.eaa0103id " +
                        "left join eaa0103 as eaa0103dev on eaa0103dev.eaa0103id = eaa01033item and eaa0103dev.eaa0103tipo <> 3 " +
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
                        whereNcm+
                        whereDepartamento+
                        whereMPS +
                        "group by aah01codigo, abb01num, abb01data, eaa01esdata, ent.abe01codigo, " +
                        "ent.abe01na,mps, " +
                        "abm01codigo, abm01na, aam06codigo,abe30codigo, abe30nome,abe40codigo, " +
                        "abe40nome,abg01codigo "+
                        "order by abb01num,mps, abm01codigo "

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEmpresa,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,parametroEntidade,
                parametroItens,parametroTransp,parametroNcm,parametroDepto,parametroMPS);
        
    }


    private comporDevolucao(TableMap dados, List<String> camposLivres){
        for(campo in camposLivres ){
            if(campo != null){
                dados.put(campo, dados.getBigDecimal_Zero(campo) - dados.getBigDecimal_Zero(campo+"dev"));
            }
        }

        for(campo in ["eaa0103qtuso","eaa0103qtcoml","eaa0103unit","eaa0103total","eaa0103totdoc","eaa0103totfinanc"]){
            dados.put(campo, dados.getBigDecimal_Zero(campo) - dados.getBigDecimal_Zero(campo+"dev"));
        }
    }

    private buscarCampos(TableMap dados, Map<String, String> campos){
        for(campo in campos){
            if(campo.value != null){
                String nomeBanco = buscarNomeCampoFixo(campo.value);
                if(nomeBanco != null){
                    dados.put("nomeCampo"+campo.key, campo.value);
                    dados.put("valorCampo"+campo.key, dados.getBigDecimal_Zero(nomeBanco))
                }else{
                    nomeBanco = buscarNomeCampoLivre(campo.value)
                    dados.put("nomeCampo"+campo.key, nomeBanco);
                    dados.put("valorCampo"+campo.key, dados.getBigDecimal_Zero(campo.value))
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


    private String buscarNomeCampoLivre(String campo) {
		def sql = " select aah02descr from aah02 where aah02nome = :nome "
		return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))
		
	}
}
// Formula corrigida 24/10/2024
//meta-sis-eyJkZXNjciI6IlNSRiAtIEl0ZW5zIFBvciBEb2N1bWVudG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=