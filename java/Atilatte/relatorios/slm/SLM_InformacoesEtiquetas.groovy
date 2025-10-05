package Atilatte.relatorios.slm;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.utils.Parametro;

public class SLM_InformacoesEtiquetas extends RelatorioBase {
	
	@Override 
	public DadosParaDownload executar() {

		List<Integer> mps = getListInteger("mps");

		List<Long> itens = getListLong("itens");
		String despachoIni = getString("despachoIni");
		String despachoFin = getString("despachoFin");
		String redespachoIni = getString("redespachoIni");
		String redespachoFin = getString("redespachoFin");
		Integer numNotaIni = getInteger("numNotaIni");
		Integer numNotaFin = getInteger("numNotaFin");
		Integer numRomIni = getInteger("numRomIni");
		Integer numRomFin = getInteger("numRomFin");
		Integer numEtiquetaIni = getInteger("numEtiquetaIni");
		Integer numEtiquetaFin = getInteger("numEtiquetaFin");
		Integer numCargaIni = getInteger("numCargaIni");
		Integer numCargaFin = getInteger("numCargaFin");
		LocalDate[] dataColeta = getIntervaloDatas("dataColeta");

		LocalDate[] dataCriacao = getIntervaloDatas("dataCriacao");
		

		String loteIni = getString("loteIni");
		String loteFin = getString("loteFin");

		List<TableMap> dados = buscarInformacoesEtiquetas(mps,itens, despachoIni, despachoFin, redespachoIni,
												 redespachoFin, numNotaIni, numNotaFin, numRomIni, numRomFin,
												 numEtiquetaIni, numEtiquetaFin,dataColeta,dataCriacao, loteIni, loteFin, numCargaIni, numCargaFin );
		
		for(dado in dados){
			String horaFormatado
			String hora = dado.getString("horaColeta").substring(0,2)
			String minuto = dado.getString("horaColeta").substring(2,4)
			String segundo = dado.getString("horaColeta").substring(4,6)
			horaFormatado = hora + ":" + minuto + ":" + segundo
			dado.put("horaColeta", horaFormatado);	
		}
		
		return gerarXLSX("SLM_InformacoesEtiquetas", dados);
	}
	private List<TableMap> buscarInformacoesEtiquetas(List<Integer> mps, List<Long> itens, String despachoIni, String despachoFin, String redespachoIni,
												String redespachoFin, Integer numNotaIni, Integer numNotaFin, Integer numRomIni, Integer numRomFin,
												Integer numEtiquetaIni, Integer numEtiquetaFin, LocalDate[] dataColeta, LocalDate[] dataCriacao, String loteIni, String loteFin, Integer numCargaIni, Integer numCargaFin ){


		//data Coleta
		LocalDate dtColetaIni = null
		LocalDate dtColetaFin = null

		if(dataColeta != null){
			dtColetaIni = dataColeta[0]
			dtColetaFin = dataColeta[1]
		}


		//data Criação
		LocalDate dtCriacaoIni = null
		LocalDate dtCriacaoFin = null

		if(dataCriacao != null){
			dtCriacaoIni = dataCriacao[0]
			dtCriacaoFin = dataCriacao[1]
		}


		String whereRepositorio = "where aba2001rd = 40347419 ";
		String whereMps = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
		String whereItens = itens != null && itens.size() > 0 ?  "and abm01id in (:itens) " : "";
		String whereNumNota = numNotaIni != null && numNotaFin != null ? "and cast(aba2001json ->> 'num_nota' as integer) between :numNotaIni and :numNotaFin  " : "";
		String whereNumRomaneio = numRomIni != null && numRomFin != null ? "and cast(aba2001json ->> 'num_romaneio' as integer)  between :numRomIni and :numRomFin " : "";
		String whereNumEtiqueta = numEtiquetaIni != null && numEtiquetaFin != null ? "and cast(aba2001json ->> 'num_etiqueta' as integer) between :numEtiquetaIni and :numEtiquetaFin " : "";
		String whereNumCarga = numCargaIni != null && numCargaFin != null ? "and abb01num between :numCargaIni and :numCargaFin " : "";
		String whereDtCriacaoEtiqueta = dataCriacao != null ? "and cast(aba2001json ->> 'dt_criacao_etiqueta' as date) between :dataCriacaoIni and :dataCriacaoFin " : "";
		String whereLoteEtiqueta = loteIni != null && loteFin != null ? "and cast(aba2001json ->> 'lote_etiqueta' as text) between :loteIni and :loteFin " : "";
		String whereDtColeta = dataColeta != null ? "and cast(aba2001json ->> 'data_coleta' as date) between :dataColetaIni and :dataColetaFin " : "";
		String whereCodDespacho = despachoIni != null && despachoFin != null ? "and cast(aba2001json ->> 'cod_despacho' as text) between :despachoIni and :despachoFin " : "";
		String whereCodRedespacho = redespachoIni != null && redespachoFin != null ? "and cast(aba2001json ->> 'cod_redesp' as text) between :redespachoIni and :redespachoFin " : "";
		Parametro parametroMps = !mps.contains(-1) ? Parametro.criar("mps", mps) : null;

		Parametro parametroItens = itens != null && itens.size() > 0 ?  Parametro.criar("itens", itens) : null;

		Parametro parametroNumNotaIni = numNotaIni != null && numNotaFin != null ? Parametro.criar("numNotaIni", numNotaIni): null;
		Parametro parametroNumNotaFin = numNotaIni != null && numNotaFin != null ? Parametro.criar("numNotaFin", numNotaFin) : null;

		Parametro parametroNumRomaneioIni = numRomIni != null ? Parametro.criar("numRomIni", numRomIni) : null;
		Parametro parametroNumRomaneioFin = numRomFin != null ? Parametro.criar("numRomFin", numRomFin) : null;

		Parametro parametroNumEtiquetaIni = numEtiquetaIni != null ? Parametro.criar("numEtiquetaIni", numEtiquetaIni) : null;
		Parametro parametroNumEtiquetaFin = numEtiquetaFin != null ? Parametro.criar("numEtiquetaFin", numEtiquetaFin) : null;

		Parametro parametroCargaIni = numCargaIni != null ? Parametro.criar("numCargaIni", numCargaIni) : null;
		Parametro parametroCargaFin = numCargaFin != null ? Parametro.criar("numCargaFin", numCargaFin) : null;
			
		Parametro parametroDtCriacaoEtiquetaIni = dataCriacao != null ? Parametro.criar("dataCriacaoIni", dtCriacaoIni) : null;
		Parametro parametroDtCriacaoEtiquetaFin = dataCriacao != null ? Parametro.criar("dataCriacaoFin", dtCriacaoFin) : null;

		Parametro parametroLoteEtiquetaIni = loteIni != null ? Parametro.criar("loteIni", loteIni) : null;
		Parametro parametroLoteEtiquetaFin = loteFin != null ? Parametro.criar("loteFin", loteFin) : null;

		Parametro parametroDtColetaIni = dataColeta != null ? Parametro.criar("dataColetaIni", dtColetaIni) : null;
		Parametro parametroDtColetaFin = dataColeta != null ? Parametro.criar("dataColetaFin", dtColetaFin) : null;

		Parametro parametroCodDespachoIni = despachoIni != null ? Parametro.criar("despachoIni", despachoIni) : null;
		Parametro parametroCodDespachoFin = despachoFin != null ? Parametro.criar("despachoFin", despachoFin) : null;

		Parametro parametroCodRedespachoIni = redespachoIni != null ? Parametro.criar("redespachoIni", redespachoIni) : null;
		Parametro parametroCodRedespachoFin = redespachoFin != null ? Parametro.criar("redespachoFin", redespachoFin) : null;

		String sql = "select aba2001lcto, aba2001prop, aab10user, abm01codigo, abm01na, aam06codigo, " +
					"case when abm01tipo = 0 then '0-Mat' when abm01tipo = 1 then '1-Prod' when abm01tipo = 2 then '2-Merc' else '3-Serv' end as tipoItem, "+
					"cast(aba2001json ->> 'nome_lote' as character varying(20)) as nomeLote, "+
					"cast(aba2001json ->> 'num_nota' as integer) as numNota, "+
					"cast(aba2001json ->> 'num_romaneio'as integer) as numRomaneio, "+
					"cast(aba2001json ->> 'num_etiqueta' as integer) as numEtiqueta, "+
					"cast(aba2001json ->> 'dt_criacao_etiqueta' as date) as dtCriacaoEtiqueta, "+
					"cast(aba2001json ->> 'lote_etiqueta' as integer) as loteEiqueta, "+
					"cast(aba2001json ->> 'qtd_etiqueta' as numeric(20,6)) as qtdEtiqueta, "+
					"cast(aba2001json ->> 'validade_etiqueta' as date) as validadeEtiqueta, "+
					"cast(aba2001json ->> 'fabric_etiqueta' as date) as fabricEtiqueta, "+
					"cast(aba2001json ->> 'data_coleta' as date) as dataColeta, "+
					"cast(aba2001json ->> 'hora_coleta' as character varying(6)) as horaColeta, "+
					"cast(aba2001json ->> 'cod_despacho' as text) as codDespacho, "+
					"cast(aba2001json ->> 'na_despacho' as text) as naDespacho, "+
					"cast(aba2001json ->> 'cod_redesp' as text) as codRedespacho, "+
					"cast(aba2001json ->> 'na_redesp' as text) as naRedespacho, "+
					"abb01num as numCarga "+
					"from aba2001 "+
					"inner join aba20  on aba20id = aba2001rd "+
					"inner join aab10 on aab10id = aba2001user "+
					"inner join abm01 on abm01id = aba2001item "+
					"left join aam06 on aam06id = abm01umu "+
					"left join bfc1002 on bfc1002central = cast(aba2001json ->> 'id_nota' as integer) "+
					"left join bfc10 on bfc10id = bfc1002carga "+
					"left join abb01 on abb01id = bfc10central "+
					whereRepositorio +
					whereMps +
					whereItens + 
					whereNumNota +
					whereNumRomaneio +
					whereNumEtiqueta +
				    whereNumCarga +
					whereDtCriacaoEtiqueta +
					whereLoteEtiqueta + 
					whereDtColeta + 
					whereCodDespacho +
					whereCodRedespacho+
					"order by cast(aba2001json ->> 'data_coleta' as date), cast(aba2001json ->> 'num_nota' as integer), cast(aba2001json ->> 'hora_coleta' as integer) "

		return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroMps, parametroItens, parametroNumNotaIni, parametroNumNotaFin, 
												parametroNumRomaneioIni,  parametroNumRomaneioFin,  parametroNumEtiquetaIni, 
												parametroNumEtiquetaFin,  parametroDtCriacaoEtiquetaIni, parametroDtCriacaoEtiquetaFin,
												parametroLoteEtiquetaIni, parametroLoteEtiquetaFin, parametroDtColetaIni,
												parametroDtColetaFin, parametroCodDespachoIni, parametroCodDespachoFin,
												parametroCodRedespachoIni, parametroCodRedespachoFin, parametroCargaIni, parametroCargaFin);
										
	}

	@Override 
	public String getNomeTarefa() { 
		return "SLM - Informações Etiquetas"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
}
//meta-sis-eyJkZXNjciI6IlNMTSAtIEluZm9ybWHDp8O1ZXMgRXRpcXVldGFzIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNMTSAtIEluZm9ybWHDp8O1ZXMgRXRpcXVldGFzIiwidGlwbyI6InJlbGF0b3JpbyJ9