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


public class SRF_Documentos_Por_Transportadoras extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Documentos Por Transportadoras";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
        filtrosDefault.put("devolucao", true);
        filtrosDefault.put("chkDevolucao", true);
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("tipoOperacao", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("resumo", "0");
        filtrosDefault.put("resumoOperacao", "0")
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial") == null || getInteger("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal") == null || getInteger("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal");
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer resumoOperacao = getInteger("resumoOperacao");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idDespacho = getListLong("despacho");
        List<Long> idRedespacho = getListLong("redespacho");
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

        String periodo = "";

        if (dtEmissao != null) {
            periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
        } else if (dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
        }

        params.put("totalizar1", totalizar1);
        params.put("totalizar2", totalizar2);
        params.put("totalizar3", totalizar3);
        params.put("totalizar4", totalizar4);
        params.put("totalizar5", totalizar5);
        params.put("totalizar6", totalizar6);
        params.put("title", "Documento por Transportadoras");
        params.put("empresa", empresa.aac10codigo + "-" + empresa.aac10na);
        params.put("periodo", periodo);

        if (campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
        if (campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!")

        Map<String, String> campos = new HashMap();

        campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null);
        campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null);
        campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null);
        campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null);
        campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null);
        campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null);


        List<TableMap> documentos = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, idEmpresa, idDespacho, idRedespacho, campoLivre1,
                campoLivre2, campoLivre3, campoLivre4, campoLivre5, campoLivre6);

        for (documento in documentos) {
            comporValores(documento, campos);
        }

        if (impressao == 0) return gerarPDF("SRF_Documentos_Por_Transportadoras_PDF", documentos);
        return gerarXLSX("SRF_Documentos_Por_Transportadoras_Excel", documentos);

    }

    private buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, Long idEmpresa, List<Long> idDespacho, List<Long> idRedespacho,
                             String campoLivre1, String campoLivre2, String campoLivre3, String campoLivre4,
                             String campoLivre5, String campoLivre6) {

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

        String whereNumIni = numDocIni != null ? "and doc.abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and doc.abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and doc.abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereDespacho = idDespacho != null && idDespacho.size() > 0 ? "and desp.abe01id in (:idDespacho) " : "";
        String whereRedespacho = idRedespacho != null && idRedespacho.size() > 0 ? "and redesp.abe01id in (:idRedespacho) " : "";
        String whereES = resumoOperacao == 1 ? "and eaa01esMov = 1 " : "and eaa01esMov = 0 ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroDespacho = idDespacho != null && idDespacho.size() > 0 ? Parametro.criar("idDespacho", idDespacho) : null;
        Parametro parametroRedespacho = idRedespacho != null && idRedespacho.size() > 0 ? Parametro.criar("idRedespacho", idRedespacho) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);

        String campo1 = campoLivre1 != null ? "SUM(CAST(eaa0103json ->> '" + campoLivre1 + "'" + " AS NUMERIC(18,2))) AS " + campoLivre1 + ",  " : "";
        String campo2 = campoLivre2 != null ? "SUM(CAST(eaa0103json ->> '" + campoLivre2 + "'" + " AS NUMERIC(18,2))) AS " + campoLivre2 + ", " : "";
        String campo3 = campoLivre3 != null ? "SUM(CAST(eaa0103json ->> '" + campoLivre3 + "'" + " AS NUMERIC(18,2))) AS " + campoLivre3 + ", " : "";
        String campo4 = campoLivre4 != null ? "SUM(CAST(eaa0103json ->> '" + campoLivre4 + "'" + " AS NUMERIC(18,2))) AS " + campoLivre4 + ", " : "";
        String campo5 = campoLivre5 != null ? "SUM(CAST(eaa0103json ->> '" + campoLivre5 + "'" + " AS NUMERIC(18,2))) AS " + campoLivre5 + ", " : "";
        String campo6 = campoLivre6 != null ? "SUM(CAST(eaa0103json ->> '" + campoLivre6 + "'" + " AS NUMERIC(18,2))) AS " + campoLivre6 + ", " : "";


        String sql = "SELECT " + campo1 + campo2 + campo3 + campo4 + campo5 + campo6 + "aag02uf AS estado, aag0201nome AS municipio, aah20placa AS placa, doc.abb01num, doc.abb01data, eaa01esdata, ent.abe01codigo AS codEnt, ent.abe01na AS naEnt, " +
                "aah01codigo,eaa01json, desp.abe01codigo AS codDespacho, desp.abe01na AS nomeDespacho, redesp.abe01codigo AS codRedespacho, redesp.abe01na AS nomeRedespacho, " +
                "carga.abb01num AS numCarga, " +
                "SUM(eaa0103qtuso) AS eaa0103qtuso, SUM(eaa0103qtComl) AS eaa0103qtcoml, SUM(eaa0103unit) AS eaa0103unit,  SUM(eaa0103unit) AS eaa0103unit, " +
                "SUM(eaa0103total) AS eaa0103total, SUM(eaa0103totdoc) AS eaa0103totdoc, SUM(eaa0103totfinanc) AS eaa0103totfinanc " +
                "FROM eaa01 " +
                "INNER JOIN abb01 AS doc ON abb01id = eaa01central " +
                "INNER JOIN abe01 AS ent ON ent.abe01id = abb01ent " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "INNER JOIN abe01 AS desp ON desp.abe01id = eaa0102despacho " +
                "LEFT JOIN abe01 AS redesp ON redesp.abe01id = eaa0102redespacho " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN abe0101 ON abe0101ent = ent.abe01id AND abe0101principal = 1 " +
                "INNER JOIN aag0201 ON aag0201id = abe0101municipio  " +
                "INNER JOIN aag02 ON aag02id = aag0201uf  " +
                "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                "LEFT JOIN aah20 ON aah20id = eaa0102veiculo " +
                "LEFT JOIN bfc1002 ON doc.abb01id = bfc1002central " +
                "LEFT JOIN bfc10 ON bfc1002carga = bfc10id " +
                "LEFT JOIN abb01 AS carga on bfc10central = carga.abb01id " +
                "WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                " AND eaa01cancData IS NULL " +
                " AND eaa01nfestat <> 5 " +
                whereEmpresa +
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereES +
                whereDespacho +
                whereRedespacho +
                " GROUP BY aag02uf,aag0201nome,aah20placa,eaa01id, doc.abb01num, doc.abb01data, eaa01esdata, ent.abe01codigo, " +
                "ent.abe01na, desp.abe01codigo, redesp.abe01codigo,desp.abe01na, redesp.abe01na, aah01codigo, carga.abb01num " +
                "ORDER BY desp.abe01codigo, doc.abb01num";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin, parametroEntidade,
                parametroDespacho, parametroRedespacho);
    }

    private void comporValores(TableMap documento, Map<String, String> campos) {
        for (campo in campos) {
            if (campo.value != null) {
                String nomeCampo = buscarNomeCampoFixo(campo.value);
                if (nomeCampo != null) {
                    documento.put("nomeCampo" + campo.key, campo.value);
                    documento.put("valorCampo" + campo.key, documento.get(nomeCampo));
                } else {
                    nomeCampo = buscarNomeCampoLivre(campo.value);
                    documento.put("nomeCampo" + campo.key, nomeCampo);
                    documento.put("valorCampo" + campo.key, documento.get(campo.value));
                }
            }
        }
    }

    private String buscarNomeCampoFixo(String campo) {
        switch (campo) {
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
        def sql = " select aah02descr from aah02 where aah02nome = :nome ";
        return getAcessoAoBanco().obterString(sql, criarParametroSql("nome", campo));

    }
}
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBUcmFuc3BvcnRhZG9yYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiBEb2N1bWVudG9zIFBvciBUcmFuc3BvcnRhZG9yYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgUG9yIFRyYW5zcG9ydGFkb3JhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==