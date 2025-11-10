package multitec.relatorios.sce;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.bc.Bcb01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCE_InventarioOperacional extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "SCE - Inventario Operacional";
	}
	
	public Map<String, Object> criarValoresIniciais(){
		return criarFiltros("data", DateUtils.getStartAndEndMonth(MDate.date()));
	}

	@Override
	public DadosParaDownload executar() {
		String itemIni = getString("itemIni");
		String itemFin = getString("itemFin");
		
		LocalDate[] data = getIntervaloDatas("data");
		List<Integer> tipoItem = getListInteger("mpms")
		
		params.put("TITULO_RELATORIO", "Inventário Operacional");
		params.put("NOME_EMPRESA", getVariaveis().getAac10().getAac10na());
		if (data != null) {
			params.put("PERIODO", "Período: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		
		List<TableMap> dados = obterDadosRelatorio(itemIni, itemFin , data, tipoItem);
		
		return gerarPDF("SCE_InventarioOperacional", dados);
	}

	private List<TableMap> obterDadosRelatorio(String itemIni, String itemFin, LocalDate[] data, List<Integer> tipoItem) {
		String wherePeriodoData = data != null && data.size() > 0 ? " and bcb01.bcb01data >= '" + data[0] + "' and bcb01.bcb01data <= '" + data[1] + "'": "";
		String whereItem = itemIni != null && itemFin != null ? " AND abm01codigo BETWEEN :itemIni AND :itemFin " :
						   itemIni != null && itemFin == null ? " AND abm01codigo >= :itemIni " :
						   itemIni == null && itemFin != null ? " AND abm01codigo <= :itemFin " : ""
						   
		String whereTipo = tipoItem != null && tipoItem.size() > 0 ? " AND abm01tipo IN (:tipoItem)" : "";
		
		Parametro paramItemIni = itemIni != null ? Parametro.criar("itemIni", itemIni) : null;
		Parametro paramItemFin = itemFin != null ? Parametro.criar("itemFin", itemFin) : null;
		Parametro paramTipo = tipoItem != null && tipoItem.size() > 0 ? Parametro.criar("tipoItem", tipoItem) : null;
		
		String sql = "SELECT bcb01id, bcb01data, abm01codigo, abm01descr AS nomeItem, bcb01saldo, bcb01inventario, bcb01invCorrigido, (bcb01saldo - bcb01inventario) AS qtdLcto " +
					 "  FROM bcb01 " +
					 " INNER JOIN Abm01 ON Abm01id = bcb01item" +
					 getSamWhere().getWherePadrao("WHERE", Bcb01.class) +
					 wherePeriodoData + 
					 whereItem + 
					 whereTipo + 
					 " ORDER BY bcb01data, abm01tipo, abm01codigo";
					 
		 List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramItemIni, paramItemFin, paramTipo);
		 return receberDadosRelatorio;
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIEludmVudGFyaW8gT3BlcmFjaW9uYWwiLCJ0aXBvIjoicmVsYXRvcmlvIn0=