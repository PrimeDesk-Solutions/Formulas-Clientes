package Atilatte.relatorios.slm

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import java.math.RoundingMode;
import br.com.multitec.utils.collections.TableMap;
import java.time.LocalDate;
import sam.server.samdev.utils.Parametro;
import java.time.format.DateTimeFormatter;
import br.com.multitec.utils.Utils;

public class SLM_PagamentosFreteAgregados extends RelatorioBase {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Valores de EXTRACT(DOW ...) do PostgreSQL: 0 = Domingo ... 6 = Sábado
    private static final Map<Integer, String> DIAS_SEMANA = [0: "Domingo", 1: "Segunda-Feira", 2: "Terça-Feira", 3: "Quarta-Feira",
                                                             4: "Quinta-Feira", 5: "Sexta-Feira", 6: "Sábado"];

    // Ordem de impressão dos dias no modo por motorista (domingo no fim da semana)
    private static final List<Integer> ORDEM_DIAS_SEMANA = [1, 2, 3, 4, 5, 6, 0];

    @Override
    public String getNomeTarefa() {
        return "SLM - Pagamento Frete Agregados";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("impressao", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idsMotoristas = getListLong("motorista");
        List<Long> idsVeiculos = getListLong("veiculo");
        LocalDate[] datas = resolverPeriodo(getIntervaloDatas("data"));
        Integer impressao = getInteger("impressao") ?: 0;

        if (!idsMotoristas) idsMotoristas = buscarMotoristas(idsVeiculos, datas);

        params.put("periodo", "Período: " + datas[0].format(FORMATO_DATA) + " à " + datas[1].format(FORMATO_DATA));

        if (impressao == 1) return gerarPDF("SLM_Pagamento_Frete_Agregado_Motorista", buscarRegistrosMotoristas(idsMotoristas, idsVeiculos, datas));

        return gerarPDF("SLM_Pagamento_Frete_Agregado_Geral", buscarRegistrosGeral(idsMotoristas, idsVeiculos, datas));
    }

    /**
     * Sem período informado, considera os últimos 7 dias.
     * Se apenas uma das datas for informada, a outra é completada a partir dela.
     */
    private LocalDate[] resolverPeriodo(LocalDate[] datas) {
        LocalDate dataFim = datas != null && datas[1] != null ? datas[1] : LocalDate.now();
        LocalDate dataInicio = datas != null && datas[0] != null ? datas[0] : dataFim.minusDays(7);

        return [dataInicio, dataFim] as LocalDate[];
    }

    private List<TableMap> buscarRegistrosGeral(List<Long> idsMotoristas, List<Long> idsVeiculos, LocalDate[] datas) {
        List<TableMap> documentos = new ArrayList();

        for (motorista in idsMotoristas) {
            List<TableMap> cargas = buscarCargas(motorista, idsVeiculos, datas);
            Integer numSemanaAux = null;
            String orientacao = "";

            for (carga in cargas) {
                Long idCarga = carga.getLong("idCarga");
                Integer numCarga = carga.getInteger("numCarga");
                BigDecimal totDoc = carga.getBigDecimal_Zero("totDoc");
                BigDecimal totalBruto = carga.getBigDecimal_Zero("totBruto");
                Integer capacidade = carga.getInteger("capacidade");

                if (totalBruto == 0) interromper("Total do Peso Bruto na carga " + numCarga + " é zero, verifique a carga e tente novamente.");

                BigDecimal vlrFrete = buscarValorOcorrencias(idCarga, "FRETE");
                BigDecimal vlrDiaria = buscarValorOcorrencias(idCarga, "DIARIA");
                BigDecimal vlrPedagio = buscarValorOcorrencias(idCarga, "PEDAGIO ENTREGA");
                BigDecimal valePedagio = buscarValorOcorrencias(idCarga, "VALE PEDAGIO");
                BigDecimal vlrDescarga = buscarValorOcorrencias(idCarga, "DESCARGA");
                BigDecimal vlrColeta = buscarValorOcorrencias(idCarga, "COLETA");
                BigDecimal vlrDesconto = buscarValorOcorrencias(idCarga, "DESCONTO").abs().negate();
                BigDecimal vlrFreteBruto = vlrFrete + vlrPedagio + vlrDescarga + vlrColeta + vlrDiaria + valePedagio;
                BigDecimal vlrFreteLiquido = vlrFrete + vlrPedagio + vlrDescarga + vlrColeta + vlrDiaria;
                BigDecimal percentNota = dividir(vlrFreteBruto, totDoc);
                BigDecimal vlrFretePorQuilo = dividir(vlrFreteBruto, totalBruto);
                BigDecimal percentCapacidade = dividir(totalBruto, capacidade);
                Integer numSemana = carga.getInteger("semana");
                Integer qtdCarga = buscarQuantidadesDeCargasSemana(motorista, idsVeiculos, datas, numSemana);

                if (numSemana != numSemanaAux) {
                    orientacao = buscarOrientacoesCarga(motorista, idsVeiculos, datas, numSemana);
                }
                numSemanaAux = numSemana;

                carga.put("percentNota", percentNota);
                carga.put("freteKG", vlrFretePorQuilo);
                carga.put("percentCapacidade", percentCapacidade);
                carga.put("semana", buscarNomeSemana(numSemana));
                carga.put("vlrFrete", vlrFrete + vlrDiaria);
                carga.put("vlrPedagio", vlrPedagio);
                carga.put("vlrDescarga", vlrDescarga);
                carga.put("vlrColeta", vlrColeta);
                carga.put("vlrDesconto", vlrDesconto);
                carga.put("vlrFreteBruto", vlrFreteBruto);
                carga.put("vlrFreteLiquido", vlrFreteLiquido);
                carga.put("orientacao", orientacao);
                carga.put("qtdCarga", qtdCarga);
                carga.put("valePedagio", valePedagio);
            }
            documentos.addAll(cargas);
        }

        return documentos;
    }

    private List<TableMap> buscarRegistrosMotoristas(List<Long> idsMotoristas, List<Long> idsVeiculos, LocalDate[] datas) {
        List<TableMap> documentos = new ArrayList();

        for (motorista in idsMotoristas) {
            // Retorna nulo quando o motorista não tem cargas no período/veículos filtrados
            TableMap tmInformacoesMotorista = buscarInformacoesMotorista(motorista, idsVeiculos, datas);
            if (tmInformacoesMotorista == null) continue;

            String nomeMotorista = tmInformacoesMotorista.getString("nomeMotorista");
            String veiculo = tmInformacoesMotorista.getString("veiculo");
            Integer capacidadeVeiculo = tmInformacoesMotorista.getInteger("capacidade");

            for (Integer diaSemana in ORDEM_DIAS_SEMANA) {
                TableMap tmDocumento = new TableMap();
                TableMap cargasMotorista = buscarCargasPorSemanaMotorista(motorista, idsVeiculos, datas, diaSemana);

                BigDecimal totDoc = cargasMotorista != null ? cargasMotorista.getBigDecimal_Zero("totDoc") : BigDecimal.ZERO;
                BigDecimal totQuilo = cargasMotorista != null ? cargasMotorista.getBigDecimal_Zero("totBruto") : BigDecimal.ZERO;

                BigDecimal vlrFrete = buscarValorOcorrenciasSemanaMotorista("FRETE", diaSemana, motorista, idsVeiculos, datas);
                BigDecimal vlrDiaria = buscarValorOcorrenciasSemanaMotorista("DIARIA", diaSemana, motorista, idsVeiculos, datas);
                BigDecimal vlrPedagio = buscarValorOcorrenciasSemanaMotorista("PEDAGIO ENTREGA", diaSemana, motorista, idsVeiculos, datas);
                BigDecimal vlrDescarga = buscarValorOcorrenciasSemanaMotorista("DESCARGA", diaSemana, motorista, idsVeiculos, datas);
                BigDecimal vlrColeta = buscarValorOcorrenciasSemanaMotorista("COLETA", diaSemana, motorista, idsVeiculos, datas);
                BigDecimal vlrFreteTotal = vlrFrete + vlrPedagio + vlrDescarga + vlrColeta + vlrDiaria;
                BigDecimal vlrFretePorQuilo = dividir(vlrFreteTotal, totQuilo);

                if (diaSemana == 0 && totDoc == 0 && vlrFreteTotal == 0) continue;

                tmDocumento.put("totDoc", totDoc);
                tmDocumento.put("pesoBruto", totQuilo);
                tmDocumento.put("nomeMotorista", nomeMotorista);
                tmDocumento.put("veiculo", veiculo);
                tmDocumento.put("vlrFrete", vlrFrete + vlrDiaria);
                tmDocumento.put("vlrDescarga", vlrDescarga);
                tmDocumento.put("vlrPedagio", vlrPedagio);
                tmDocumento.put("vlrColeta", vlrColeta);
                tmDocumento.put("capacidadeVeiculo", capacidadeVeiculo);
                tmDocumento.put("semana", buscarNomeSemana(diaSemana));
                tmDocumento.put("vlrFretePorQuilo", vlrFretePorQuilo);

                documentos.add(tmDocumento);
            }
        }

        return documentos;
    }
    private BigDecimal dividir(BigDecimal dividendo, Number divisor) {
        if (dividendo == null || divisor == null || divisor == 0) return BigDecimal.ZERO;

        return dividendo.divide(new BigDecimal(divisor.toString()), 10, RoundingMode.HALF_UP);
    }

    private List<TableMap> buscarCargas(Long idMotorista, List<Long> idsVeiculos, LocalDate[] data) {
        String whereMotorista = "WHERE bfc10motorista = :idMotorista ";
        String whereData = "AND bfc10retdatareal BETWEEN :dataInicio AND :dataFim ";
        String whereVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? "AND bfc10veiculo IN (:idsVeiculos) " : "";

        Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
        Parametro dataInicial = Parametro.criar("dataInicio", data[0]);
        Parametro dataFinal = Parametro.criar("dataFim", data[1]);
        Parametro parametroVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? Parametro.criar("idsVeiculos", idsVeiculos) : null;

        String sql = "SELECT carga.abb01num AS numCarga, bfc10id AS idCarga, " +
                "EXTRACT(DOW FROM bfc10retdatareal) AS semana, aah21nome AS nomeMotorista, " +
                "aah20nome AS veiculo, aah20capkg AS capacidade, bfc10retdatareal AS dtRealEntrega, SUM(eaa01totdoc) AS totDoc, " +
                "SUM(CAST(NULLIF(eaa01json ->> 'peso_bruto', '') AS NUMERIC(18,6))) AS totBruto, bfc10orienta AS orienta " +
                "FROM bfc10 " +
                "INNER JOIN abb01 AS carga ON carga.abb01id = bfc10central " +
                "INNER JOIN bfc1002 ON bfc1002carga = bfc10id " +
                "INNER JOIN abb01 AS doc ON doc.abb01id = bfc1002central " +
                "INNER JOIN eaa01 ON eaa01central = doc.abb01id " +
                "INNER JOIN aah21 ON aah21id = bfc10motorista " +
                "INNER JOIN aah20 ON aah20id = bfc10veiculo " +
                whereMotorista +
                whereData +
                whereVeiculo +
                "GROUP BY carga.abb01num, bfc10id, EXTRACT(DOW FROM bfc10retdatareal), aah21nome, aah20nome, aah20capkg " +
                "ORDER BY EXTRACT(ISODOW FROM bfc10retdatareal), bfc10retdatareal, carga.abb01num";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroMotorista, dataInicial, dataFinal, parametroVeiculo);
    }

    /**
     * Totaliza notas e peso de todas as cargas do motorista no dia da semana informado, dentro do período.
     * Sem GROUP BY a consulta sempre retorna uma única linha, mesmo com várias datas do mesmo dia da semana no período.
     */
    private TableMap buscarCargasPorSemanaMotorista(Long idMotorista, List<Long> idsVeiculos, LocalDate[] data, Integer diaSemana) {
        String whereMotorista = "WHERE bfc10motorista = :idMotorista ";
        String whereData = "AND bfc10retdatareal BETWEEN :dataInicio AND :dataFim ";
        String whereVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? "AND bfc10veiculo IN (:idsVeiculos) " : "";
        String whereSemana = "AND EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana ";

        Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
        Parametro dataInicial = Parametro.criar("dataInicio", data[0]);
        Parametro dataFinal = Parametro.criar("dataFim", data[1]);
        Parametro parametroVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? Parametro.criar("idsVeiculos", idsVeiculos) : null;
        Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);

        String sql = "SELECT COALESCE(SUM(eaa01totdoc), 0) AS totDoc, " +
                "COALESCE(SUM(CAST(NULLIF(eaa01json ->> 'peso_bruto', '') AS NUMERIC(18,6))), 0) AS totBruto " +
                "FROM bfc10 " +
                "INNER JOIN bfc1002 ON bfc1002carga = bfc10id " +
                "INNER JOIN abb01 AS doc ON doc.abb01id = bfc1002central " +
                "INNER JOIN eaa01 ON eaa01central = doc.abb01id " +
                whereMotorista +
                whereData +
                whereVeiculo +
                whereSemana;

        return getAcessoAoBanco().buscarUnicoTableMap(sql, parametroMotorista, dataInicial, dataFinal, parametroVeiculo, parametroSemana);
    }

    private BigDecimal buscarValorOcorrencias(Long idCarga, String descrOcorrencia) {
        String whereCarga = "WHERE bfc10id = :idCarga ";
        String whereOcorrencia = "AND UPPER(bfc01descr) LIKE '%" + descrOcorrencia + "%'";

        Parametro parametrocarga = Parametro.criar("idCarga", idCarga);

        String sql = "SELECT COALESCE(SUM(bfc1001valor), 0) AS valor " +
                "FROM bfc10 " +
                "INNER JOIN bfc1001 ON bfc1001carga = bfc10id " +
                "INNER JOIN bfc01 ON bfc01id = bfc1001oco " +
                whereCarga +
                whereOcorrencia;

        return getAcessoAoBanco().obterBigDecimal(sql, parametrocarga);
    }

    private BigDecimal buscarValorOcorrenciasSemanaMotorista(String descrOcorrencia, Integer diaSemana, Long idMotorista, List<Long> idsVeiculos, LocalDate[] data) {
        String whereOcorrencia = "WHERE UPPER(bfc01descr) LIKE '%" + descrOcorrencia + "%' ";
        String whereSemana = "AND EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana ";
        String whereMotorista = "AND bfc10motorista = :idMotorista ";
        String whereData = "AND bfc10retdatareal BETWEEN :dataInicio AND :dataFim ";
        String whereVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? "AND bfc10veiculo IN (:idsVeiculos) " : "";

        Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);
        Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
        Parametro dataInicial = Parametro.criar("dataInicio", data[0]);
        Parametro dataFinal = Parametro.criar("dataFim", data[1]);
        Parametro parametroVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? Parametro.criar("idsVeiculos", idsVeiculos) : null;

        String sql = "SELECT COALESCE(SUM(bfc1001valor), 0) AS valor " +
                "FROM bfc10 " +
                "INNER JOIN bfc1001 ON bfc1001carga = bfc10id " +
                "INNER JOIN bfc01 ON bfc01id = bfc1001oco " +
                whereOcorrencia +
                whereSemana +
                whereMotorista +
                whereData +
                whereVeiculo;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroSemana, parametroMotorista, dataInicial, dataFinal, parametroVeiculo);
    }

    private String buscarNomeSemana(Integer diaSemana) {
        return DIAS_SEMANA.get(diaSemana) ?: "";
    }

    /**
     * Motoristas com cargas no período (e nos veículos filtrados), ativos ou não.
     */
    private List<Long> buscarMotoristas(List<Long> idsVeiculos, LocalDate[] data) {
        String whereVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? "AND bfc10veiculo IN (:idsVeiculos) " : "";

        Parametro dataInicial = Parametro.criar("dataInicio", data[0]);
        Parametro dataFinal = Parametro.criar("dataFim", data[1]);
        Parametro parametroVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? Parametro.criar("idsVeiculos", idsVeiculos) : null;

        String sql = "SELECT aah21id FROM aah21 " +
                "WHERE EXISTS (SELECT 1 FROM bfc10 " +
                "WHERE bfc10motorista = aah21id " +
                "AND bfc10retdatareal BETWEEN :dataInicio AND :dataFim " +
                whereVeiculo +
                ") " +
                "ORDER BY aah21nome";

        return getAcessoAoBanco().obterListaDeLong(sql, dataInicial, dataFinal, parametroVeiculo);
    }
    private TableMap buscarInformacoesMotorista(Long idMotorista, List<Long> idsVeiculos, LocalDate[] data) {
        String whereMotorista = "WHERE bfc10motorista = :idMotorista ";
        String whereData = "AND bfc10retdatareal BETWEEN :dataInicio AND :dataFim ";
        String whereVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? "AND bfc10veiculo IN (:idsVeiculos) " : "";

        Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
        Parametro dataInicial = Parametro.criar("dataInicio", data[0]);
        Parametro dataFinal = Parametro.criar("dataFim", data[1]);
        Parametro parametroVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? Parametro.criar("idsVeiculos", idsVeiculos) : null;

        String sql = "SELECT aah21nome AS nomeMotorista, STRING_AGG(DISTINCT aah20nome, ', ') AS veiculo, MAX(aah20capkg) AS capacidade " +
                "FROM bfc10 " +
                "INNER JOIN aah21 ON aah21id = bfc10motorista " +
                "INNER JOIN aah20 ON aah20id = bfc10veiculo " +
                whereMotorista +
                whereData +
                whereVeiculo +
                "GROUP BY aah21nome";

        return getAcessoAoBanco().buscarUnicoTableMap(sql, parametroMotorista, dataInicial, dataFinal, parametroVeiculo);
    }

    private String buscarOrientacoesCarga(Long idMotorista, List<Long> idsVeiculos, LocalDate[] data, Integer diaSemana) {
        String whereMotorista = "WHERE bfc10motorista = :idMotorista ";
        String whereData = "AND bfc10retdatareal BETWEEN :dataInicio AND :dataFim ";
        String whereVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? "AND bfc10veiculo IN (:idsVeiculos) " : "";
        String whereSemana = "AND EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana ";

        Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
        Parametro dataInicial = Parametro.criar("dataInicio", data[0]);
        Parametro dataFinal = Parametro.criar("dataFim", data[1]);
        Parametro parametroVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? Parametro.criar("idsVeiculos", idsVeiculos) : null;
        Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);

        String sql = "SELECT STRING_AGG(bfc10orienta, ',') AS orientacoes " +
                "FROM bfc10 " +
                whereMotorista +
                whereData +
                whereVeiculo +
                whereSemana;

        return getAcessoAoBanco().obterString(sql, parametroMotorista, dataInicial, dataFinal, parametroVeiculo, parametroSemana);
    }

    private Integer buscarQuantidadesDeCargasSemana(Long idMotorista, List<Long> idsVeiculos, LocalDate[] data, Integer diaSemana) {
        String whereMotorista = "WHERE bfc10motorista = :idMotorista ";
        String whereData = "AND bfc10retdatareal BETWEEN :dataInicio AND :dataFim ";
        String whereVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? "AND bfc10veiculo IN (:idsVeiculos) " : "";
        String whereSemana = "AND EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana ";

        Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
        Parametro dataInicial = Parametro.criar("dataInicio", data[0]);
        Parametro dataFinal = Parametro.criar("dataFim", data[1]);
        Parametro parametroVeiculo = idsVeiculos != null && idsVeiculos.size() > 0 ? Parametro.criar("idsVeiculos", idsVeiculos) : null;
        Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);

        String sql = "SELECT COUNT(bfc10id) AS qtdCarga " +
                "FROM bfc10 " +
                whereMotorista +
                whereData +
                whereVeiculo +
                whereSemana;

        return getAcessoAoBanco().obterInteger(sql, parametroMotorista, dataInicial, dataFinal, parametroVeiculo, parametroSemana);
    }
}
//meta-sis-eyJkZXNjciI6IlBhZ2FtZW50byBGcmV0ZSBBZ3JlZ2Fkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
