package Profer.relatorios.scv;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCV_pedidoscancelados extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCV - Pedidos Cancelados";
	}

	@Override
	public Map<String, Object> criarValoresIniciais(){
		def filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("pedido", "0");
		filtrosDefault.put("numInicial", "00000")
		filtrosDefault.put("numFinal", "99999")
		filtrosDefault.put("imprimirItens", true);

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {

		List<Long> idsTipo = getListLong("idsTipo");
		List<Long> idsEntidades = getListLong("idsEntidades");
		List<Long> idsReps = getListLong("representante")
		List<Long> cancelamento = getListLong("cancelamento");

		Integer pedido = getInteger("pedido");
		Integer numInicial = getInteger("numInicial");
		Integer numFinal = getInteger("numFinal");
		LocalDate[] dtEmissao = getIntervaloDatas("dtEmissao");
		LocalDate[] dtCanc = getIntervaloDatas("dtCanc")

		boolean imprimirItens = get("imprimirItens");

		params.put("TITULO_RELATORIO", "Documentos Cancelados");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("IMPRIMIR_ITENS", imprimirItens);
		
		if (dtCanc != null) {
			params.put("PERIODO", "Período: " + dtCanc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtCanc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		
		List<TableMap> dados = obterDadosRelatorio(pedido, idsTipo, numInicial, numFinal, dtEmissao, idsEntidades, dtCanc, idsReps, imprimirItens, cancelamento);

		return gerarPDF("SCV_DocumentosCancelados", dados);
	}

	private List<TableMap> obterDadosRelatorio(Integer pedido, List<Long> idsTipo, Integer numInicial, Integer numFinal, LocalDate[] dtEmissao, List<Long> idsEntidades, LocalDate[] dtCanc, List<Long> idsReps, boolean imprimirItens, List<Long> cancelamento){

		String wheretipo  = idsTipo != null && !idsTipo.isEmpty() ? " AND abb01tipo IN (:idsTipo) " : "";
		String whereEntidades = idsEntidades != null && !idsEntidades.isEmpty() ? " AND abe01id IN (:idsEntidades) " : "";
		String whereRepresentantes  = idsReps != null && !idsReps.isEmpty() ? " AND (eaa01rep0 IN (:idsReps) OR eaa01rep1 IN (:idsReps)  OR eaa01rep2 IN (:idsReps)  OR eaa01rep3 IN (:idsReps)  OR eaa01rep4 IN (:idsReps)) " : "";
		String whereCancelamento = cancelamento != null && !cancelamento.isEmpty() ? " AND eaa01cancMotivo IN (:cancelamento) "  : "";
		String whereDataEmissao = dtEmissao != null && dtEmissao.size() > 0 ? " and abb01data >= '" + dtEmissao[0] + "' and abb01data <= '" + dtEmissao[1] + "'": "";
		String whereDataCancelamento = dtCanc != null && dtCanc.size() > 0 ? " and eaa01cancData >= '" + dtCanc[0] + "' and eaa01cancData <= '" + dtCanc[1] + "'": " and eaa01.eaa01cancData is not null";
		String whereNumero = numInicial != null && numFinal != null ? " and abb01num >= '" + numInicial + "' and abb01num <= '" + numFinal + "'": "";
		
		Parametro p1 = idsTipo != null && idsTipo.size() > 0 ? Parametro.criar("idsTipo", idsTipo) : null;
		Parametro p2 = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
		Parametro p3 = idsReps != null && idsReps.size() > 0 ? Parametro.criar("idsReps", idsReps) : null;
		Parametro p4 = cancelamento != null && cancelamento.size() > 0 ? Parametro.criar("cancelamento", cancelamento) : null;

		String sql = "SELECT eaa01id, abb01num, abb01serie, abb01data, abd01codigo, abe01codigo, abe01na, abb01valor, aae11codigo, aae11descr, eaa01cancData, eaa01cancObs " +
				      (imprimirItens ? " , abm01tipo, abm01codigo,abm01na, eaa0103pcNum, eaa0103qtComl, eaa0103total ": "") +
				   "    FROM eaa0103 " +
				   "   INNER JOIN eaa01 ON eaa01id = eaa0103doc " +
				   "   INNER JOIN abb01 ON abb01id = eaa01central " +
				   "    LEFT JOIN abe01 ON Abe01id = abb01ent " +
				   "   INNER JOIN abd01 ON Abd01id = eaa01pcd " +
				   "    LEFT JOIN Aae11 ON Aae11id = eaa01cancMotivo " +
				   "   INNER JOIN Abm01 ON Abm01id = eaa0103item" +
				   "   WHERE eaa01.eaa01clasDoc = " + Eaa01.CLASDOC_SCV +
				   "     AND eaa01.eaa01esMov = " + pedido +
				   getSamWhere().getWherePadrao("AND", Eaa0103.class) + 
				   wheretipo + 
				   whereEntidades + 
				   whereRepresentantes + 
				   whereCancelamento + 
				   whereDataEmissao + 
				   whereDataCancelamento +
				   whereNumero;

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4);
		return receberDadosRelatorio;
	}
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MgQ2FuY2VsYWRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==