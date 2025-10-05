package Atilatte.relatorios.slm

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import org.apache.poi.ss.formula.atp.Switch
import sam.core.variaveis.MDate;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SLM_InformacoesCarga extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SLM - Informações Carga";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        LocalDate data = MDate.date()
        filtrosDefault.put("impressao", "0")
        filtrosDefault.put("numCargaIni", "00000001")
        filtrosDefault.put("numCargaFin", "999999999")

        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        Integer numCargaInicial = getInteger("numCargaIni");
        Integer numCargaFinal = getInteger("numCargaFin");
        List<Long> motoristas = getListLong("motorista");
        List<Long> veiculos = getListLong("veiculo");
        LocalDate[] dtCko = getIntervaloDatas("dataCheckout");
        LocalDate[] dtCki = getIntervaloDatas("dataCheckin");
        List<Integer> status = getListInteger("statusCarga");
        Integer impressao = getInteger("impressao");

        List<TableMap> documentos = buscarDocumentos(numCargaInicial, numCargaFinal, motoristas, veiculos, dtCko, dtCki, status);

        for(documento in documentos){
            Integer statusEntrega = documento.getInteger("statusEntrega");
            String txtStatusEntrega = buscarStatusEntrega(statusEntrega);

            documento.put("status", txtStatusEntrega)
        }

        params.put("titulo", "SLM - Informações Carga")
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na())

        if(impressao == 1) return gerarXLSX("SLM_InformacoesCarga(EXCEL)", documentos);

        return gerarPDF("SLM_InformacoesCarga(PDF)", documentos);

    }

    private List<TableMap> buscarDocumentos(Integer numNotaInicial, Integer numNotaFinal, List<Long> motoristas, List<Long> veiculos, LocalDate[] dtCko, LocalDate[] dtCki, List<Integer> status) {

        String whereNumCarga = numNotaInicial != null && numNotaFinal != null ? "AND abb01carga.abb01num BETWEEN :numNotaInicial AND :numNotaFinal " : "";
        String whereVeiculo = veiculos != null && veiculos.size() > 0 ? "AND aah20id IN (:veiculos) " : "";
        String whereMotorista = motoristas != null && motoristas.size() > 0 ? "AND aah21id IN (:motoristas) " : "";
        String whereChko = dtCko != null ? "AND bfc10ckoData BETWEEN :dtCkoIni AND :dtCkoFin " : "";
        String whereChki = dtCki != null ? "AND bfc10ckiData BETWEEN :dtCkiIni AND :dtCkiFin " : "";
        String whereStatus = status != null && status.size() > 0 ? "AND bfc1002entrega IN (:status)" : "";

        Parametro parametroNUmCargaIni = numNotaInicial != null ? Parametro.criar("numNotaInicial", numNotaInicial) : null;
        Parametro parametroNUmCargaFin = numNotaFinal != null ? Parametro.criar("numNotaFinal", numNotaFinal) : null;
        Parametro parametroVeiculo = veiculos != null && veiculos.size() > 0 ? Parametro.criar("veiculos", veiculos) : null;
        Parametro parametroMotorista = motoristas != null && motoristas.size() > 0 ? Parametro.criar("motoristas", motoristas) : null;
        Parametro parametroChkoIni = dtCko != null ? Parametro.criar("dtCkoIni", dtCko[0]) : null;
        Parametro parametroChkoFin = dtCko != null ? Parametro.criar("dtCkoFin", dtCko[1]) : null;
        Parametro parametroChkiIni = dtCki != null ? Parametro.criar("dtCkiIni", dtCki[0]) : null;
        Parametro parametroChkiFin = dtCki != null ? Parametro.criar("dtCkiFin", dtCki[1]) : null;
        Parametro parametroStatus = status != null && status.size() > 0 ? Parametro.criar("status", status) : null;

        String sql = "SELECT abb01carga.abb01num AS numCarga, aah20codigo AS codVeiculo, aah20nome AS nomeVeiculo, " +
                "aah21codigo AS codMotorista, aah21nome AS nomeMotorista, bfc10ckoData AS dataCko, bfc10ckohora AS horaCko, " +
                "aab10cko.aab10user AS userCko, bfc10ckiData AS dataCki, bfc10ckihora AS horaCki, aab10cki.aab10user, abb01doc.abb01num AS numDoc, " +
                "abb01doc.abb01data AS dtEmissaoNota, abe01codigo AS codEntidade, abe01na AS naEntidade, bfc1002entrega AS statusEntrega " +
                "FROM bfc10 " +
                "INNER JOIN abb01 AS abb01carga ON abb01carga.abb01id = bfc10central " +
                "INNER JOIN aah20 ON bfc10veiculo = aah20id " +
                "INNER JOIN aah21 ON bfc10motorista = aah21id " +
                "LEFT JOIN aab10 AS aab10cko ON aab10cko.aab10id = bfc10ckouser " +
                "LEFT JOIN aab10 AS aab10cki ON aab10cki.aab10id = bfc10ckiuser " +
                "INNER JOIN bfc1002 ON bfc1002carga = bfc10id " +
                "INNER JOIN abb01 AS abb01doc ON abb01doc.abb01id = bfc1002central " +
                "INNER JOIN abe01 ON abe01id = abb01doc.abb01ent " +
                "WHERE TRUE " +
                whereNumCarga +
                whereVeiculo +
                whereMotorista +
                whereChko +
                whereChki +
                whereStatus +
                "ORDER BY abb01carga.abb01num, abb01doc.abb01num";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNUmCargaIni, parametroNUmCargaFin, parametroVeiculo, parametroMotorista, parametroChkoIni, parametroChkoFin, parametroChkiIni, parametroChkiFin, parametroStatus);
    }
    private String buscarStatusEntrega(Integer statusEntrega){
        String status = "";

        switch (statusEntrega){
            case 0:
                status = "0 - Não Efetuada";
                break;
            case 1:
                status = "1 - Efetuada";
                break;
            case 2:
                status = "2 - Reentrega";
                break;
            defaut:
                status = "3 - Devolvido";
                break;
        }

        return status;
    }
}
//meta-sis-eyJkZXNjciI6IlNMTSAtIEluZm9ybWHDp8O1ZXMgQ2FyZ2EiLCJ0aXBvIjoicmVsYXRvcmlvIn0=