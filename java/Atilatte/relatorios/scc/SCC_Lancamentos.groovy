package Atilatte.relatorios.scc;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.utils.Parametro
import java.time.LocalDate
import br.com.multitec.utils.Utils
import sam.core.variaveis.MDate

public class SCC_Lancamentos extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SCC - Lançamentos"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap()
		LocalDate data = MDate.date()
		filtrosDefault.put("impressao", "0")
		
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		List<Long> representantes = getListLong("representantes");
		LocalDate[] dtCalculo = getIntervaloDatas("dtCalculo");
		Integer impressao = getInteger("impressao");

		List<TableMap> dados = buscarDadosRelatorio(representantes,dtCalculo);

		if(impressao == 1) return gerarXLSX("SCC_Lancamentos_Excel", dados);
		
		return gerarPDF("SCC_Lancamentos_PDF", dados);
		
	}

	private buscarDadosRelatorio(List<Long> representantes,LocalDate[] dtCalculo){

		// Data Calculo
		LocalDate dtCalculoIni = null;
		LocalDate dtCalculoFin = null;

		if(dtCalculo != null){
			dtCalculoIni = dtCalculo[0];
			dtCalculoFin = dtCalculo[1];
		}
		
		String whereRepresentantes = representantes != null && representantes.size() > 0 ? "AND abe01id in (:representantes) " : "";
		String whereDataCalculo = dtCalculo != null ? "AND  abb01data between :dtCalculoIni AND :dtCalculoFin " : "";
		Parametro parametroRepresentante = representantes != null && representantes.size() > 0 ? Parametro.criar("representantes",representantes) : null;
		Parametro parametroDtCalculoIni = dtCalculo != null ? Parametro.criar("dtCalculoIni", dtCalculoIni) : null;
		Parametro parametroDtCalculoFin = dtCalculo != null ? Parametro.criar("dtCalculoIni", dtCalculoIni) : null;
		
		String sql = "SELECT abb01num as lcto, abb01data as data, dcc01historico as historico, dcc01valor as valor "+
					"FROM dcc01 "+
					"INNER JOIN abb01 ON abb01id = dcc01central "+
					"WHERE TRUE "+
					whereDataCalculo +
					whereRepresentantes;

		return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroRepresentante,parametroDtCalculoIni );
					
	}
}
//meta-sis-eyJkZXNjciI6IlNDQyAtIExhbsOnYW1lbnRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==