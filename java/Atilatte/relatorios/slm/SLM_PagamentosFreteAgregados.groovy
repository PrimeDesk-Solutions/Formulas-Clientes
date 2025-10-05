package Atilatte.relatorios.slm

import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.collections.TableMap;
import java.time.LocalDate;
import sam.server.samdev.utils.Parametro;
import java.time.format.DateTimeFormatter;
import br.com.multitec.utils.Utils;

public class SLM_PagamentosFreteAgregados extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "Pagamento Frete Agregados";
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
		LocalDate[] datas = getIntervaloDatas("data");
		List<Long> motoristas = buscarMotoristas();
		Integer impressao = getInteger("impressao");

		if (idsMotoristas == null || idsMotoristas == []) {
			idsMotoristas = motoristas;
		}

		String periodo = "";
		if (datas != null) {
			periodo = "Período Emissão: " + datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + datas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
		}
		params.put("periodo", periodo);

		List<TableMap> documentos = new ArrayList();

		if (impressao == 0) {
			documentos = buscarRegistrosGeral(idsMotoristas, datas);
		} else {
			documentos = buscarRegistrosMotoristas(idsMotoristas, datas);
		}

		if (impressao == 1) return gerarPDF("freteAgregadoMotorista", documentos);

		return gerarPDF("freteAgregado", documentos);

	}

	private List<TableMap> buscarRegistrosGeral(List<Long> idsMotoristas, LocalDate[] datas) {
		List<TableMap> documentos = new ArrayList();

		for (motorista in idsMotoristas) {
			List<TableMap> ocorrencias = new ArrayList();
			List<TableMap> docsMotoristas = new ArrayList();
			List<TableMap> cargas = buscarCargas(motorista, datas);
			TableMap freteDespacho = new TableMap();
			Integer numSemanaAux = 0;
			String orientacao;

			for (carga in cargas) {
				Long idCarga = carga.getLong("idCarga");

				BigDecimal totDoc = carga.getBigDecimal_Zero("totDoc");
				BigDecimal vlrQuilo = carga.getBigDecimal_Zero("totBruto");
				Integer capacidade = carga.getInteger("capacidade");
				BigDecimal vlrFrete = buscarValorOcorrencias(idCarga, "FRETE");
				BigDecimal vlrDiaria = buscarValorOcorrencias(idCarga, "DIARIA");
				BigDecimal vlrPedagio = buscarValorOcorrencias(idCarga, "PEDAGIO");
				BigDecimal vlrDescarga = buscarValorOcorrencias(idCarga, "DESCARGA");
				BigDecimal vlrColeta = buscarValorOcorrencias(idCarga, "COLETA");
				BigDecimal vlrDesconto = buscarValorOcorrencias(idCarga, "DESCONTO");
				vlrDesconto = vlrDesconto.abs() * -1;
				BigDecimal vlrFreteTotal = vlrFrete + vlrPedagio + vlrDescarga + vlrColeta + vlrDiaria;
				BigDecimal percentNota = vlrFreteTotal / totDoc;
				BigDecimal vlrFretePorQuilo = vlrFreteTotal / vlrQuilo;
				BigDecimal percentCapacidade = vlrQuilo / capacidade;
				Integer numSemana = carga.getInteger("semana");
				Integer qtdCarga = buscarQuantidadesDeCargasSemana(motorista, datas,numSemana);



				def totalGeral = vlrFrete + vlrPedagio + vlrDescarga;
				String nomeSemana = buscarNomeSemana(numSemana);

				if (numSemana != numSemanaAux) {
					orientacao = "";
					orientacao = buscarOrientacoesCarga(motorista, datas, numSemana);
				}
				numSemanaAux = carga.getInteger("semana");

				carga.put("percentNota", percentNota);
				carga.put("freteKG", vlrFretePorQuilo);
				carga.put("percentCapacidade", percentCapacidade);
				carga.put("semana", nomeSemana);
				carga.put("vlrFrete", vlrFrete + vlrDiaria);
				carga.put("vlrPedagio", vlrPedagio);
				carga.put("vlrDescarga", vlrDescarga);
				carga.put("vlrColeta", vlrColeta);
				carga.put("vlrDesconto", vlrDesconto);
				carga.put("vlrFreteTotal", vlrFreteTotal);
				carga.put("orientacao", orientacao);
				carga.put("qtdCarga", qtdCarga);

			}

			documentos.addAll(cargas);
		}

		return documentos;
	}

	private List<TableMap> buscarRegistrosMotoristas(List<Long> idsMotoristas, LocalDate[] datas) {
		List<TableMap> documentos = new ArrayList();

		for (motorista in idsMotoristas) {

			TableMap freteDespacho = new TableMap();
			TableMap cargasMotorista = new TableMap();
			TableMap tmInformacoesMotorista = buscarNomeMotoristaById(motorista);
			if (tmInformacoesMotorista == null) continue;

			for (int i = 1; i <= 6; i++) {
				TableMap tmDocumento = new TableMap();
				cargasMotorista = buscarCargasPorSemanaMotorista(motorista, datas, i);
				String nomeMotorista = tmInformacoesMotorista.getString("nomeMotorista");
				String veiculo = tmInformacoesMotorista.getString("veiculo");
				Integer capacidadeVeiculo = tmInformacoesMotorista.getInteger("capacidade");

				BigDecimal totDoc = cargasMotorista != null ? cargasMotorista.getBigDecimal_Zero("totDoc") : null;
				BigDecimal totQuilo = cargasMotorista != null ? cargasMotorista.getBigDecimal_Zero("totBruto") : null;
				Integer capacidade = cargasMotorista != null ? cargasMotorista.getInteger("capacidade") : null;

				BigDecimal vlrFrete = buscarValorOcorrenciasSemanaMotorista("FRETE", i, motorista, datas);
				BigDecimal vlrDiaria = buscarValorOcorrenciasSemanaMotorista("DIARIA", i, motorista, datas);
				BigDecimal vlrPedagio = buscarValorOcorrenciasSemanaMotorista("PEDAGIO", i, motorista, datas);
				BigDecimal vlrDescarga = buscarValorOcorrenciasSemanaMotorista("DESCARGA", i, motorista, datas);
				BigDecimal vlrColeta = buscarValorOcorrenciasSemanaMotorista("COLETA", i, motorista, datas);
				BigDecimal vlrDesconto = buscarValorOcorrenciasSemanaMotorista("DESCONTO", i, motorista, datas);
				vlrDesconto = vlrDesconto.abs() * -1;
				BigDecimal vlrFreteTotal = vlrFrete + vlrPedagio + vlrDescarga + vlrColeta + vlrDiaria;
				BigDecimal percentNota = totDoc == null ? 0 : vlrFreteTotal / totDoc;
				BigDecimal vlrFretePorQuilo = totQuilo == null ? 0 : vlrFreteTotal / totQuilo;
				BigDecimal percentCapacidade = capacidade == null ? 0 : totQuilo / capacidade;
				String nomeSemana = buscarNomeSemana(i);

				tmDocumento.put("totDoc", totDoc);
				tmDocumento.put("pesoBruto", totQuilo);
				tmDocumento.put("nomeMotorista", nomeMotorista);
				tmDocumento.put("veiculo", veiculo);
				tmDocumento.put("vlrFrete", vlrFrete + vlrDiaria);
				tmDocumento.put("vlrdescarga", vlrDescarga);
				tmDocumento.put("vlrpedagio", vlrPedagio);
				tmDocumento.put("vlrcoleta", vlrColeta);
				tmDocumento.put("capacidadeVeiculo", capacidadeVeiculo);
				tmDocumento.put("semana", nomeSemana);
				tmDocumento.put("vlrFretePorQuilo", vlrFretePorQuilo);

				documentos.add(tmDocumento);
			}
		}

		return documentos;

	}

	private List<TableMap> buscarCargas(Long idMotorista, LocalDate[] data) {
		LocalDate dataInicio = null;
		LocalDate dataFim = null;

		LocalDate dataAtual = LocalDate.now();

		if (data != null) {
			dataInicio = data[0];
			dataFim = data[1];
		} else {
			dataInicio = dataAtual.minusDays(7);
			dataFim = dataAtual;
		}

		String whereMotorista = "where bfc10motorista = :idMotorista ";
		String whereDataInicial = dataInicio != null ? "and bfc10retdatareal >= :dataInicio " : "";
		String whereDataFinal = dataFim != null ? "and bfc10retdatareal <= :dataFim " : "";

		Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
		Parametro dataInicial = Parametro.criar("dataInicio", dataInicio);
		Parametro dataFinal = Parametro.criar("dataFim", dataFim);

		String sql = "SELECT carga.abb01num AS numCarga, bfc10id AS idCarga, " +
				"EXTRACT(DOW FROM BFC10RETDATAREAL) AS semana, aah21nome as nomeMotorista, " +
				"aah20nome as veiculo,aah20capkg as capacidade,bfc10retdatareal as dtRealEntrega, SUM(eaa01totdoc) AS totDoc, SUM(CAST(eaa01json ->> 'peso_bruto' AS numeric(18,6))) as totBruto, bfc10orienta as orienta  " +
				"FROM BFC10 " +
				"INNER JOIN ABB01 as carga ON carga.abb01id = bfc10central " +
				"INNER JOIN bfc1002 ON bfc1002carga = bfc10id " +
				"INNER JOIN abb01 AS doc ON doc.abb01id = bfc1002central " +
				"INNER JOIN eaa01 ON eaa01central = doc.abb01id " +
				"INNER JOIN aah21 ON aah21id = bfc10motorista " +
				"INNER JOIN aah20 ON aah20id = bfc10veiculo " +
				whereMotorista +
				whereDataInicial +
				whereDataFinal +
				"AND BFC10RETDATAREAL IS NOT NULL " +
				"GROUP BY carga.abb01num, bfc10id, EXTRACT(DOW FROM BFC10RETDATAREAL), aah21nome, aah20nome, aah20capkg " +
				"ORDER BY  EXTRACT(DOW FROM BFC10RETDATAREAL)";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroMotorista, dataInicial, dataFinal);

	}

	private TableMap buscarCargasPorSemanaMotorista(Long idMotorista, LocalDate[] data, Integer diaSemana) {
		LocalDate dataInicio = null;
		LocalDate dataFim = null;

		LocalDate dataAtual = LocalDate.now();

		if (data != null) {
			dataInicio = data[0];
			dataFim = data[1];
		} else {
			dataInicio = dataAtual.minusDays(7);
			dataFim = dataAtual;
		}

		String whereMotorista = "where bfc10motorista = :idMotorista ";
		String whereDataInicial = dataInicio != null ? "and bfc10retdatareal >= :dataInicio " : "";
		String whereDataFinal = dataFim != null ? "and bfc10retdatareal <= :dataFim " : "";

		Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
		Parametro dataInicial = Parametro.criar("dataInicio", dataInicio);
		Parametro dataFinal = Parametro.criar("dataFim", dataFim);
		Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);

		String sql = "SELECT EXTRACT(DOW FROM BFC10RETDATAREAL) AS semana, aah21nome as nomeMotorista, bfc10retdatareal as dtRealEntrega, " +
				"aah20nome as veiculo,aah20capkg as capacidade,COALESCE(SUM(eaa01totdoc),0) AS totDoc, COALESCE(SUM(CAST(eaa01json ->> 'peso_bruto' AS numeric(18,6))),0) as totBruto " +
				"FROM BFC10 " +
				"INNER JOIN bfc1002 ON bfc1002carga = bfc10id " +
				"INNER JOIN abb01 AS doc ON doc.abb01id = bfc1002central " +
				"INNER JOIN eaa01 ON eaa01central = doc.abb01id " +
				"INNER JOIN aah21 ON aah21id = bfc10motorista " +
				"INNER JOIN aah20 ON aah20id = bfc10veiculo " +
				whereMotorista +
				whereDataInicial +
				whereDataFinal +
				"AND BFC10RETDATAREAL IS NOT NULL " +
				"and EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana " +
				"GROUP BY EXTRACT(DOW FROM BFC10RETDATAREAL), aah21nome, aah20nome, aah20capkg, bfc10retdatareal " +
				"ORDER BY  EXTRACT(DOW FROM BFC10RETDATAREAL)";

		return getAcessoAoBanco().buscarUnicoTableMap(sql, parametroMotorista, dataInicial, dataFinal, parametroSemana);

	}

	private BigDecimal buscarValorOcorrencias(Long idCarga, String descrOcorrencia) {

		String whereCarga = "where bfc10id = :idCarga ";
		String whereOcorrencia = "and UPPER(bfc01descr) like '%" + descrOcorrencia + "%'";

		Parametro parametrocarga = Parametro.criar("idCarga", idCarga);

		String sql = "select COALESCE(SUM(bfc1001valor), 0) as valor " +
				"from bfc10 " +
				"inner join bfc1001 on bfc1001carga = bfc10id " +
				"inner join bfc01 on bfc01id = bfc1001oco " +
				whereCarga +
				whereOcorrencia;

		return getAcessoAoBanco().obterBigDecimal(sql, parametrocarga);

	}

	private BigDecimal buscarValorOcorrenciasSemanaMotorista(String descrOcorrencia, Integer diaSemana, Long idMotorista, LocalDate[] data) {

		LocalDate dataInicio = null;
		LocalDate dataFim = null;

		LocalDate dataAtual = LocalDate.now();

		if (data != null) {
			dataInicio = data[0];
			dataFim = data[1];
		} else {
			dataInicio = dataAtual.minusDays(7);
			dataFim = dataAtual;
		}

		String whereOcorrencia = "where UPPER(bfc01descr) like '%" + descrOcorrencia + "%'";
		String whereSemana = "and EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana ";
		String whereMotorista = "and bfc10motorista = :idMotorista ";
		String whereDataInicial = dataInicio != null ? "and bfc10retdatareal >= :dataInicio " : "";
		String whereDataFinal = dataFim != null ? "and bfc10retdatareal <= :dataFim " : "";
		String whereRetData = "AND BFC10RETDATAREAL IS NOT NULL ";

		Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);
		Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
		Parametro paramDataInicial = Parametro.criar("dataInicio", dataInicio);
		Parametro paramDataFinal = Parametro.criar("dataFim", dataFim);

		String sql = "select COALESCE(SUM(bfc1001valor), 0) as valor " +
				"from bfc10 " +
				"inner join bfc1001 on bfc1001carga = bfc10id " +
				"inner join bfc01 on bfc01id = bfc1001oco " +
				whereOcorrencia +
				whereSemana +
				whereMotorista +
				whereDataInicial +
				whereDataFinal +
				whereRetData;

		return getAcessoAoBanco().obterBigDecimal(sql, parametroSemana, parametroMotorista, paramDataInicial, paramDataFinal);

	}

	private String buscarNomeSemana(Integer numSemana) {
		switch (numSemana) {
			case 1:
				return "Segunda-Feira";
				break;
			case 2:
				return "Terça-Feira"
				break;
			case 3:
				return "Quarta-Feira";
				break;
			case 4:
				return "Quinta-Feira";
				break;
			case 5:
				return "Sexta-Feira";
				break;
			default:
				return "Sábado";
				break;
		}

	}

	private List<Long> buscarMotoristas() {
		String sql = "select distinct aah21id from aah21 " +
				"inner join bfc10 on aah21id = bfc10motorista ";

		return getAcessoAoBanco().obterListaDeLong(sql);
	}

	private TableMap buscarNomeMotoristaById(Long idMotorista) {
		String sql = "select aah21nome as nomeMotorista, aah20nome as veiculo, aah20capKg as capacidade " +
				"from aah20 " +
				"left join aah21 on aah21id = aah20motorista " +
				"where aah21id = :idMotorista";


		return getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("idMotorista", idMotorista));
	}

	private String buscarOrientacoesCarga(Long idMotorista, LocalDate[] data, Integer diaSemana) {

		LocalDate dataInicio = null;
		LocalDate dataFim = null;

		LocalDate dataAtual = LocalDate.now();

		if (data != null) {
			dataInicio = data[0];
			dataFim = data[1];
		} else {
			dataInicio = dataAtual.minusDays(7);
			dataFim = dataAtual;
		}

		String whereMotorista = "where bfc10motorista = :idMotorista ";
		String whereDataInicial = dataInicio != null ? "and bfc10retdatareal >= :dataInicio " : "";
		String whereDataFinal = dataFim != null ? "and bfc10retdatareal <= :dataFim " : "";

		Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
		Parametro dataInicial = Parametro.criar("dataInicio", dataInicio);
		Parametro dataFinal = Parametro.criar("dataFim", dataFim);
		Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);

		String sql = "SELECT STRING_AGG(carga.bfc10orienta, ',') AS orientacoes " +
				"FROM BFC10 AS carga " +
				whereMotorista +
				whereDataInicial +
				whereDataFinal +
				"AND BFC10RETDATAREAL IS NOT NULL " +
				"and EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana ";

		return getAcessoAoBanco().obterString(sql, parametroMotorista, dataInicial, dataFinal, parametroSemana);
	}

	private Integer buscarQuantidadesDeCargasSemana(Long idMotorista, LocalDate[] data, Integer diaSemana) {
		LocalDate dataInicio = null;
		LocalDate dataFim = null;

		LocalDate dataAtual = LocalDate.now();

		if (data != null) {
			dataInicio = data[0];
			dataFim = data[1];
		} else {
			dataInicio = dataAtual.minusDays(7);
			dataFim = dataAtual;
		}

		String whereMotorista = "where bfc10motorista = :idMotorista ";
		String whereDataInicial = dataInicio != null ? "and bfc10retdatareal >= :dataInicio " : "";
		String whereDataFinal = dataFim != null ? "and bfc10retdatareal <= :dataFim " : "";

		Parametro parametroMotorista = Parametro.criar("idMotorista", idMotorista);
		Parametro dataInicial = Parametro.criar("dataInicio", dataInicio);
		Parametro dataFinal = Parametro.criar("dataFim", dataFim);
		Parametro parametroSemana = Parametro.criar("diaSemana", diaSemana);

		String sql = "SELECT COALESCE(COUNT(bfc10id),0) as qtdCarga  " +
				"FROM BFC10 " +
				whereMotorista +
				whereDataInicial +
				whereDataFinal +
				"AND BFC10RETDATAREAL IS NOT NULL " +
				"and EXTRACT(DOW FROM bfc10retdatareal) = :diaSemana";

		return getAcessoAoBanco().obterInteger(sql, parametroMotorista, dataInicial, dataFinal, parametroSemana);

	}
}
//meta-sis-eyJkZXNjciI6IlBhZ2FtZW50byBGcmV0ZSBBZ3JlZ2Fkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlBhZ2FtZW50byBGcmV0ZSBBZ3JlZ2Fkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlBhZ2FtZW50byBGcmV0ZSBBZ3JlZ2Fkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=