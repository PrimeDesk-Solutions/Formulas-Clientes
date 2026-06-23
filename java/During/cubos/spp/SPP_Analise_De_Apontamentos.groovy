package During.cubos.spp;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.relatorio.TableMapDataSource

public class SPP_Analise_De_Apontamentos extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SPP - Análise de Apontamentos"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("numeroInicial", "00000001")
		filtrosDefault.put("numeroFinal", "99999999")
		
		filtrosDefault.put("impressao", "0");
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		String numFtIni = getString("numeroInicial");
		String numFtFin = getString("numeroFinal");
		LocalDate[] data = getIntervaloDatas("data");
		List<Long> idUsers = getListLong("colaboradores");
		List<Long> idAtividades = getListLong("atividades");
		
		List<TableMap> dados = buscarDadosRelatorio(numFtIni,numFtFin, data, idUsers, idAtividades);

		gerarXLSX("SPP_Analise_De_Apontamentos", dados)
		
		
	}

	private List<TableMap> buscarDadosRelatorio(String numFtIni, String numFtFin, LocalDate[] data, List<Long> idUsers, List<Long> idAtividades){

		// Data Inicial Final
		LocalDate dataInicial = null;
		LocalDate dataFinal = null;

		if(data != null){
			dataInicial = data[0];
			dataFinal = data[1];
		}
		
		String whereNumFtIni = numFtIni != null && !numFtIni.isEmpty() ? "and cast(bab0102json ->> 'ft' as text) >= :numFtIni " : "";
		String whereNumFtFin = numFtFin != null && !numFtFin.isEmpty() ? "and cast(bab0102json ->> 'ft' as text) <= :numFtFin " : "";
		String whereData = data != null ? "and bab0102dti between :dataInicial and :dataFinal " : "";
		String whereUsers = idUsers != null && idUsers.size() > 0 ? "and aab10id in (:idUsers) " : "";
		String whereAtividades = idAtividades != null && idAtividades.size() > 0 ? "and abp01id in (:idAtividades) " : "";

		Parametro parametroNumFtIni = numFtIni != null && !numFtIni.isEmpty() ? Parametro.criar("numFtIni", numFtIni) : null;
		Parametro parametroNumFtFin = numFtFin != null && !numFtFin.isEmpty() ? Parametro.criar("numFtFin", numFtFin) : null;
		Parametro parametroDataIni = dataInicial != null ?  Parametro.criar("dataInicial", dataInicial) : null;
		Parametro parametroDataFin = dataFinal != null ?  Parametro.criar("dataFinal", dataFinal) : null;
		Parametro parametroUsers = idUsers != null && idUsers.size() > 0 ? Parametro.criar("idUsers", idUsers) : null;
		Parametro parametroAtividades = idAtividades != null && idAtividades.size() > 0 ? Parametro.criar("idAtividades", idAtividades) : null;

		String sql =   "select cast(bab0102json ->> 'ft' as text) as ft, aab10nome as colaborador, abp01codigo as codAtiv, abp01descr as descrAtiv, "+
					"abb01num as numOp, bab0102hri as HoraInic, bab0102hrf as HoraFin, abp01custo as custoPadrao,  "+
					"bab0102dti as data, bab0102dtf as dtFim, bab0102hri as inicio, "+
					"bab0102hrf as fim, "+
					"(bab0102hrf -  bab0102hri)::text as intervalo, "+
					"(EXTRACT(EPOCH FROM bab0102hrf - bab0102hri)/60)::text as minutos, "+
					"(((EXTRACT(EPOCH FROM bab0102hrf - bab0102hri ) / 60) / 60))::text horas, "+
					"((EXTRACT(EPOCH FROM bab0102hrf - bab0102hri ) / 60) / 60) * abp01custo as custoHora "+
					"from bab01 "+
					"left join bab0102 on bab0102op = bab01id "+
					"left join abp01 on abp01id = bab0102ativ "+
					"inner join aab10 on aab10id = bab0102userdig "+
					"inner join abb01 on abb01id = bab01central "+
					"where bab0102dti is not null "+
					"and bab0102dtf is not null "+
					"and bab0102hri is not null "+
					"and bab0102hrf is not null " +
					whereNumFtIni +
					whereNumFtFin +
					whereData + 
					whereUsers + 
					whereAtividades+
					"order by cast(bab0102json ->> 'ft' as text), aab10nome "

					

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumFtIni, parametroNumFtFin, parametroDataIni, parametroDataFin, parametroUsers,parametroAtividades )
					
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUFAgLSBBbsOhbGlzZSBkZSBBcG9udGFtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=