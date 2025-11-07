package Atilatte.relatorios.sce;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.utils.Parametro
import sam.model.entities.bc.Bcc01
import sam.model.entities.ab.Abm01
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit;
import sam.core.variaveis.MDate
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils

public class SCE_SaldosVencidos extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SCE - Saldos Vencidos"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap()
		LocalDate data = MDate.date()
		filtrosDefault.put("data", data)
		filtrosDefault.put("dataValidade", DateUtils.getStartAndEndMonth(MDate.date()))
		filtrosDefault.put("impressao","0")
		filtrosDefault.put("optionsSaldos", "0")
		filtrosDefault.put("itensInventariaveis", true)
		filtrosDefault.put("itensNaoInventariaveis", true)
		
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		List<Integer> mps = getListInteger("mpm");
		Long idItemIni = getLong("itemIni");
		Long idItemFin = getLong("itemFin");
		Boolean itemInventariavel = getBoolean("itensInventariaveis");
		Boolean itemNoaInventariavel = getBoolean("itensNaoInventariaveis");
		String loteIni = getString("loteIni");
		String loteFin = getString("loteFin");
		String serieIni = getString("serieIni");
		String serieFin = getString("serieFin");
		List<Long> idsStatus = getListLong("status");
		List<Long> idsLocal = getListLong("local");
		LocalDate[] dataValidade = getIntervaloDatas("dataValidade");
		Integer saldos = getInteger("optionsSaldos");
		LocalDate data = getLocalDate("data");
		Integer impressao = getInteger("impressao");

		List<TableMap> dadosRelatorio = buscarDadosRelatorio( mps, idItemIni, idItemFin, itemInventariavel, itemNoaInventariavel, loteIni,
	 			loteFin, serieIni, serieFin, dataValidade, saldos, data,idsStatus,idsLocal);
	 	String titulo = "";
	 	
		if(saldos == 0){
			titulo = "Saldos Vencidos"
		}else if(saldos == 1){
			titulo = "Saldos a Vencer"
		}else{
			titulo = "Saldos Vencidos e Saldos a Vencer"
		}
	 	params.put("titulo",titulo);
		params.put("empresa",obterEmpresaAtiva().aac10codigo +"-"+ obterEmpresaAtiva().aac10na);
		params.put("periodo","Data Considerada: " + data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

		if(impressao == 0){
			return gerarPDF("SCE_SaldosVencidos_PDF",dadosRelatorio )
		}else{
			return gerarXLSX("SCE_SaldosVencidos_Excel",dadosRelatorio )
		}
		
	}

	private List<TableMap> buscarDadosRelatorio(List<Integer> mps,Long idItemIni,Long idItemFin,Boolean itemInventariavel,Boolean itemNoaInventariavel,String loteIni,
	String loteFin,String serieIni,String serieFin,LocalDate[] dataValidade,Integer saldos,LocalDate data, List<Long>idsStatus, List<Long> idsLocal ){

		// Data Validade
		LocalDate dataValidIni = null;
		LocalDate dataValidFin = null;
		if(dataValidade != null){
			dataValidIni = dataValidade[0];
			dataValidFin = dataValidade[1];
		}

		String whereMPS = !mps.contains(-1) ? "and abm01tipo in (:mps)" : "";
		String whereItemIni = idItemIni != null ? "and abm01id >= :idItemIni  " : "";
		String whereItemFin = idItemFin != null ? "and abm01id <= :idItemFin " : "";
		String whereLoteIni = loteIni != null ? "and bcc0201lote >= :loteIni " : "";
		String whereLoteFin = loteFin != null ? "and bcc0201lote <= :loteFin " : "";
		String whereSerieIni = serieIni != null ? "and bcc0201serie >= :serieIni " : "";
		String whereSerieFin = serieFin != null ? "and bcc0201serie <= :serieFin " : "";
		String whereValidade = dataValidade != null ? "and bcc0201validade between :dataValidIni and :dataValidFin " : "";
		String whereData = saldos == 0 ? "and bcc0201validade <= :data " : saldos == 1 ? "and bcc0201validade >= :data " : ""; 
		String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND aam04.aam04id in (:idsStatus) " : "";
		String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "AND abm15.abm15id in (:idsLocal) " : "";

		Parametro parametroMPS = !mps.contains(-1) ?  Parametro.criar("mps",mps): null;
		Parametro parametroItemIni = idItemIni != null ? Parametro.criar("idItemIni",idItemIni): null;
		Parametro parametroItemFin = idItemFin != null ? Parametro.criar("idItemFin",idItemFin): null;
		Parametro parametroLoteIni = loteIni != null ?  Parametro.criar("loteIni",loteIni): null;
		Parametro parametroLoteFin = loteFin != null ?  Parametro.criar("loteFin",loteIni): null;
		Parametro parametroSerieIni = serieIni != null ? Parametro.criar("serieIni",serieIni): null;
		Parametro parametroSerieFin = serieFin != null ? Parametro.criar("serieFin",serieFin): null;
		Parametro parametroValidadeIni = dataValidade != null ? Parametro.criar("dataValidIni",dataValidIni): null;
		Parametro parametroValidadeFin = dataValidade != null ? Parametro.criar("dataValidFin",dataValidFin): null;
		Parametro parametroData = Parametro.criar("data",data);
		Parametro parametroStatus = idsStatus != null && idsStatus.size() > 0 ? criarParametroSql("idsStatus", idsStatus) : null;
		Parametro parametroLocal = idsLocal != null && idsLocal.size() > 0 ? criarParametroSql("idsLocal", idsLocal) : null;
				
		
		
		
		String sql =   "select case when abm01tipo = 0 then 'M' else 'P' end as MPS, abm01codigo as codItem, abm01na as naItem, aam06codigo, aam04codigo as status, abm15nome as local,  "+
					"bcc0201lote as lote, bcc0201serie as serie, bcc0201fabric as fabricacao, bcc0201validade as validade, bcc0201qt "+
					"from bcc02 "+
					"inner join bcc0201 on bcc0201saldo = bcc02id "+
					"inner join abm01 on abm01id = bcc02item  "+
					"inner join aam06 on aam06id = abm01umu  "+
					"inner join aam04 on aam04id = bcc02status "+
					"inner join abm15 on abm15id = bcc02ctrl0 "+
					"where true " + 
					whereMPS +
					whereItemIni +
					whereItemFin +
					whereLoteIni +
					whereLoteFin +
					whereSerieIni +
					whereSerieFin + 
					whereValidade +
					whereData + 
					whereStatus +
					whereLocal +
					"order by bcc0201validade ";
		
		List<TableMap> registros = getAcessoAoBanco().buscarListaDeTableMap(sql,parametroMPS,parametroItemIni,parametroItemFin,parametroLoteIni,parametroLoteFin,parametroSerieIni,parametroSerieFin,parametroValidadeIni,parametroValidadeFin,parametroData,parametroStatus,parametroLocal)
		List<TableMap> lancamentos = new ArrayList();
		
		for(registro in registros){
			def validade = registro.getDate("validade");
			if(validade != null ) registro.put("dias",ChronoUnit.DAYS.between(LocalDate.now(), validade));
			lancamentos.add(registro)
		}
		return lancamentos;
					
					
					
					
		
		
	}

	
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFNhbGRvcyBWZW5jaWRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==