package multitec.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eac0101
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SRF_ProjecaoItens extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SRF - Projeção de Itens";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();

		filtrosDefault.put("dataInicial", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		filtrosDefault.put("dataFinal", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {

		List<Integer> mpm = getListInteger("mpm");
		List<Long> idsItens = getListLong("itens");
		
		LocalDate anoMesInicial = DateUtils.parseDate("01/" + getString("dataInicial"));
		LocalDate anoMesFinal = DateUtils.parseDate("01/" + getString("dataFinal"));
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Projeção de Itens");
		params.put("PERIODO", "Período: " + anoMesInicial.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + anoMesFinal.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

		List<TableMap> dados = obterDadosRelatorio(mpm, idsItens, anoMesInicial, anoMesFinal);

		return gerarPDF("SRF_ProjecaoItens", dados)
	}
	
	public List<TableMap> obterDadosRelatorio (List<Integer> mpm, List<Long> idsItens, LocalDate dataInicial, LocalDate dataFinal)  {
			
		String whereTipos = (!mpm.contains(-1)) ? " AND abm01tipo IN (:mpm) " : "";
		String whereItens = idsItens != null && idsItens.size() > 0 ? " AND abm01id IN (:idsItens) " : "";
		
		Parametro parametroTipos = (!mpm.contains(-1)) ? Parametro.criar("mpm", mpm) : null;
		Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
		
		String field = Fields.numMeses("eac01mes", "eac01ano").toString();
		Integer numMesesInicial = dataInicial != null ? (dataInicial.getYear() * 12) + dataInicial.getMonthValue() : 1;
		Integer numMesesFinal = dataFinal != null ? (dataFinal.getYear() * 12) + dataFinal.getMonthValue() : 1;
		String whereDataInicial = " and " + field + " >= " + numMesesInicial;
		String whereDataFinal = " and " + field + " <= " + numMesesFinal;

		String sql = " select eac01.eac01mes, eac01.eac01ano, abm01.abm01tipo, abm01.abm01codigo, abm01.abm01descr, aam06.aam06codigo, sum(eac0101.eac0101qt) as eac0101qt, " + 
				"         sum(eac0101.eac0101valor) as eac0101valor," +
				"         (select sum(eaa0103total) " +
				"            from eaa0103" +
				"           inner join Eaa01 eaa01 on eaa01.eaa01id = eaa0103.eaa0103doc" +
				"           where eaa0103item = abm01.abm01id " +
				"             and eaa01.eaa01clasDoc = " + Eaa01.CLASDOC_SRF + ") as eaa0103total," +
				"         (select sum(eaa0103qtComl) " +
				"            from eaa0103" +
				"           inner join Eaa01 eaa01 on eaa01.eaa01id = eaa0103.eaa0103doc" +
				"           where eaa0103item = abm01.abm01id " +
				"             and eaa01.eaa01clasDoc = " + Eaa01.CLASDOC_SRF + ") as eaa0103qtComl	" +
				"   from Eac0101 eac0101" +
				"  inner join Eac01 eac01 on eac01.eac01id = eac0101.eac0101pv " +
				"  inner join Abm01 abm01 on abm01.abm01id = eac0101.eac0101item" +
				"  inner join Aam06 aam06 on aam06.aam06id = abm01.abm01umu" +
				getSamWhere().getWherePadrao(" WHERE ", Eac0101.class) +
				whereTipos +
				whereItens +
				whereDataInicial + 
				whereDataFinal +
				"  group by abm01.abm01id, abm01.abm01tipo, abm01.abm01codigo, abm01.abm01descr, aam06.aam06codigo, eac01.eac01mes, eac01.eac01ano" +
				"  order by 4, 1";

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipos, parametroItens); 
		return receberDadosRelatorio;
	}
	
	
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFByb2plw6fDo28gZGUgSXRlbnMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=