package During.relatorios.srf

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SRF_ResumoPorItens extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SRF - Resumo Por Itens";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("total1", true)
		filtrosDefault.put("total2", true)
		filtrosDefault.put("total3", true)
		filtrosDefault.put("total4", true)
		filtrosDefault.put("total5", true)
		filtrosDefault.put("total6", true)
		filtrosDefault.put("devolucao", true)
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("resumoOperacao", "0");
		filtrosDefault.put("impressao", "0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idEntidade = getListLong("entidade");
		List<Long> idTipoDocumento = getListLong("tipo");
		List<Long> criterios = getListLong("criterios");
		List<Long> criteriosItem = getListLong("criteriosItem");
		List<Long> idsCfop = getListLong("cfop");
		List<Long> idsPcd = getListLong("pcd");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntSai = getIntervaloDatas("dataEntSai");
		Integer resumoOperacao = getInteger("resumoOperacao");
		List<Long> idsItens = getListLong("itens");
		Integer impressao = getInteger("impressao")
		String camposLivre1 = getString("campoLivre1")
		String camposFixo1 = getString("campoFixo1")
		String camposLivre2 = getString("campoLivre2")
		String camposFixo2 = getString("campoFixo2")
		String camposLivre3 = getString("campoLivre3")
		String camposFixo3 = getString("campoFixo3")
		String camposLivre4 = getString("campoLivre4")
		String camposFixo4 = getString("campoFixo4")
		String camposFixo5 = getString("campoFixo5")
		String camposLivre5 = getString("campoLivre5")
		String camposFixo6 = getString("campoFixo6")
		String camposLivre6 = getString("campoLivre6")
		Boolean totalizar1 = getBoolean("total1")
		Boolean totalizar2 = getBoolean("total2")
		Boolean totalizar3 = getBoolean("total3")
		Boolean totalizar4 = getBoolean("total4")
		Boolean totalizar5 = getBoolean("total5")
		Boolean totalizar6 = getBoolean("total6")
		Boolean devolucoes = getBoolean("devolucao")
		List<Integer> mps = getListInteger("mps")
		
		if(camposLivre1 != null && camposFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre2 != null && camposFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre3 != null && camposFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre4 != null && camposFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre5 != null && camposFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre6 != null && camposFixo6 != null) interromper("Selecione apenas 1 valor por campo!")
		
		Map<String,String> campos = new HashMap()
		campos.put("1", camposLivre1 != null ? camposLivre1 : camposFixo1 != null ? camposFixo1 : null   )
		campos.put("2", camposLivre2 != null ? camposLivre2 : camposFixo2 != null ? camposFixo2 : null   )
		campos.put("3", camposLivre3 != null ? camposLivre3 : camposFixo3 != null ? camposFixo3 : null   )
		campos.put("4", camposLivre4 != null ? camposLivre4 : camposFixo4 != null ? camposFixo4 : null   )
		campos.put("5", camposLivre5 != null ? camposLivre5 : camposFixo5 != null ? camposFixo5 : null   )
		campos.put("6", camposLivre6 != null ? camposLivre6 : camposFixo6 != null ? camposFixo6 : null   )
		
		String periodo = ""
		if(dataEmissao != null) {
			periodo = "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}else if(dataEntSai) {
			periodo = "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}
		
		params.put("aac10rs", obterEmpresaAtiva().getAac10rs())
		params.put("periodo",periodo)
		params.put("totaliza1", totalizar1)
		params.put("totaliza2", totalizar2)
		params.put("totaliza3", totalizar3)
		params.put("totaliza4", totalizar4)
		params.put("totaliza5", totalizar5)
		params.put("totaliza6", totalizar6)
		
		List<TableMap> dados = buscarItensDoc(idEntidade,idTipoDocumento,criterios,dataEmissao,dataEntSai,numeroInicial,numeroFinal,resumoOperacao,criteriosItem,idsItens,idsCfop,idsPcd,mps)
		
		for(dado in dados) {
			Long itemId = dado.getLong("abm01id")
			List<TableMap> tableMapJson= buscarCamposLivres(itemId,idEntidade,idTipoDocumento,criterios,dataEmissao,dataEntSai,numeroInicial,numeroFinal,resumoOperacao,criteriosItem,idsCfop,idsPcd,mps)
			TableMap jsonTotal = new TableMap()
			
			for(tm in tableMapJson) {
				TableMap json = tm.getTableMap("eaa0103json")
				Long itemNotaId = tm.getLong("eaa0103id")
				TableMap devolucao = obterDevolucao(itemNotaId)
				if(camposLivre1 != null) jsonTotal.put(camposLivre1, jsonTotal.getBigDecimal_Zero(camposLivre1) + json.getBigDecimal_Zero(camposLivre1))
				if(camposLivre2 != null) jsonTotal.put(camposLivre2, jsonTotal.getBigDecimal_Zero(camposLivre2) + json.getBigDecimal_Zero(camposLivre2))
				if(camposLivre3 != null) jsonTotal.put(camposLivre3, jsonTotal.getBigDecimal_Zero(camposLivre3) + json.getBigDecimal_Zero(camposLivre3))
				if(camposLivre4 != null) jsonTotal.put(camposLivre4, jsonTotal.getBigDecimal_Zero(camposLivre4) + json.getBigDecimal_Zero(camposLivre4))
				if(camposLivre5 != null) jsonTotal.put(camposLivre5, jsonTotal.getBigDecimal_Zero(camposLivre5) + json.getBigDecimal_Zero(camposLivre5))
				if(camposLivre6 != null) jsonTotal.put(camposLivre6, jsonTotal.getBigDecimal_Zero(camposLivre6) + json.getBigDecimal_Zero(camposLivre6))
				if(devolucao != null && devolucoes) {
					comporDevolucao(dado, devolucao, jsonTotal, [camposLivre1,camposLivre2,camposLivre3,camposLivre4,camposLivre5,camposLivre6])
				}
			}
			dado.putAll(jsonTotal)
			buscarCampo(dado, campos)
		}
		
		if(impressao == 1)return gerarXLSX("SRF_ResumoItem",dados)
		return gerarPDF("SRF_ResumoItem",dados);
	}
	
	private List<TableMap> buscarItensDoc(List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> criterios, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial, Integer numeroFinal, 
		Integer resumoOperacao, List<Long> criteriosItem, List<Long> idsItens, List<Long> idsCfops,List<Long> pcds, List<Integer> mps){
		
		String whereNumIni = numeroInicial != null ? " and abb01num >= :numIni " : ""
		String whereNumFim = numeroFinal != null ? " and abb01num <= :numFim " : ""
		String whereEsData = dataEntSai != null && dataEntSai.size() > 1 ? " and eaa01esdata between :esDataIni and :esDataFim " : ""
		String whereEmissaoData = dataEmissao != null && dataEmissao.size() > 1 ? " and abb01data between :emissaoDataIni and :emissaoDataFim " : ""
		String wherePcd = pcds != null && pcds.size() > 0 ? " and eaa01pcd in (:pcds) " : "" 
		String whereCfop = idsCfops != null && idsCfops.size() > 0 ? " and eaa0103cfop in (:cfops)" : ""
		String whereItens = idsItens != null && idsItens.size() > 0 ? " and abm01id in (:idItens)" : ""
		String whereCriteriosItem = criterios != null && criterios.size() > 0 ? " and abm0102criterio IN (:criterios) " : "";
		String whereEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01id in (:ent) " : ""
		String whereTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and abb01tipo in (:tipos) " : ""
		String whereClassDoc = " and eaa01clasDoc = 1 "
		String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0"
		String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
		
		
		Parametro parametroNumIni = numeroInicial != null ? Parametro.criar("numIni", numeroInicial) : null 
		Parametro parametroNumFim = numeroFinal != null ? Parametro.criar("numFim",numeroFinal) : null
		Parametro parametroEsDataIni =  dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataIni", dataEntSai[0]) : null
		Parametro parametroEsdataFim =  dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataFim",dataEntSai[1]) : null
		Parametro parametroEmissaoDataIni =  dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataIni", dataEmissao[0]) : null
		Parametro parametroEmissaodataFim =  dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataFim",dataEmissao[1]) : null
		Parametro parametroPcd = pcds != null && pcds.size() > 0 ? Parametro.criar("pcds",pcds) : null
		Parametro parametroCfop = idsCfops != null && idsCfops.size() > 0 ? Parametro.criar("cfops",idsCfops) : null
		Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idItens",idsItens) : null
		Parametro parametrocriteriosItem = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios",criterios) : null
		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("ent",idEntidade) : null
		Parametro parametroTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("tipos",idTipoDocumento) : null
		Parametro parametroMps =  mps != null && !mps.contains(-1) ?  Parametro.criar("mps",mps) : null;
		
		
		String sql = " select abm01id,abm01tipo, abm01codigo,abm01descr,aam06codigo, sum(eaa0103qtuso) as eaa0103qtuso, "+
					 " sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , sum(eaa0103totfinanc) as eaa0103totfinanc "+
					 " from eaa0103 "+
					 " inner join eaa01 on eaa01id = eaa0103doc "+
					 " inner join abb01 on abb01id = eaa01central "+
					 " inner join abm01 on abm01id = eaa0103item "+
					 " left join aam06 on aam06id = abm01umu "+
					 " left join abe01 on abe01id = abb01ent "+
					 //" left join abm0102 on abm0102item = abm01id "+
					 obterWherePadrao("eaa01","where") + whereNumIni + whereNumFim + whereEsData + 
					 whereEmissaoData + wherePcd + whereCfop + whereItens + whereCriteriosItem + whereEntidade + whereTipoDoc+whereClassDoc+whereES+ whereMps+
					 "and eaa01cancdata is null "+
					 " group by abm01id,abm01tipo,abm01codigo,abm01descr,aam06codigo "+
					 " order by abm01tipo,abm01codigo "
					 
		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni,parametroNumFim,parametroEsDataIni,parametroEsdataFim,parametroEmissaoDataIni,parametroEmissaodataFim,parametroPcd,parametroCfop,parametroItens,parametrocriteriosItem,parametroEntidade,parametroTipoDoc,parametroMps)
	}

	private List<TableMap> buscarCamposLivres(Long itemId, List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> criterios, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial, 
		Integer numeroFinal, Integer resumoOperacao, List<Long> criteriosItem,  List<Long> idsCfops,List<Long> pcds,List<Integer> mps){
		
		String whereItem = " and abm01id = :itemId"
		String whereNumIni = numeroInicial != null ? " and abb01num >= :numIni " : ""
		String whereNumFim = numeroFinal != null ? " and abb01num <= :numFim " : ""
		String whereEsData = dataEntSai != null && dataEntSai.size() > 1 ? " and eaa01esdata between :esDataIni and :esDataFim " : ""
		String whereEmissaoData = dataEmissao != null && dataEmissao.size() > 1 ? " and abb01data between :emissaoDataIni and :emissaoDataFim " : ""
		String wherePcd = pcds != null && pcds.size() > 0 ? " and eaa01pcd in (:pcds) " : ""
		String whereCfop = idsCfops != null && idsCfops.size() > 0 ? " and eaa0103cfop in (:cfops)" : ""
		String whereCriteriosItem = criterios != null && criterios.size() > 0 ? " and abm0102criterio IN (:criterios) " : "";
		String whereEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01id in (:ent) " : ""
		String whereTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and abb01tipo in (:tipos) " : ""
		String whereClassDoc = " and eaa01clasDoc = 1 "
		String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0 "
		 String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
		
		Parametro parametroNumIni = numeroInicial != null ? Parametro.criar("numIni", numeroInicial) : null
		Parametro parametroNumFim = numeroFinal != null ? Parametro.criar("numFim",numeroFinal) : null
		Parametro parametroEsDataIni =  dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataIni", dataEntSai[0]) : null
		Parametro parametroEsdataFim =  dataEntSai != null && dataEntSai.size() > 1 ? Parametro.criar("esDataFim",dataEntSai[1]) : null
		Parametro parametroEmissaoDataIni =  dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataIni", dataEmissao[0]) : null
		Parametro parametroEmissaodataFim =  dataEmissao != null && dataEmissao.size() > 1 ? Parametro.criar("emissaoDataFim",dataEmissao[1]) : null
		Parametro parametroPcd = pcds != null && pcds.size() > 0 ? Parametro.criar("pcds",pcds) : null
		Parametro parametroCfop = idsCfops != null && idsCfops.size() > 0 ? Parametro.criar("cfops",idsCfops) : null
		Parametro parametroItens =Parametro.criar("itemId",itemId) 
		Parametro parametrocriteriosItem = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios",criterios) : null
		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("ent",idEntidade) : null
		Parametro parametroTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("tipos",idTipoDocumento) : null
		Parametro parametroMps =  mps != null && !mps.contains(-1) ?  Parametro.criar("mps",mps) : null;
		
		String sql = " select eaa0103id, eaa0103json "+
						" from eaa0103 "+
						" inner join eaa01 on eaa01id = eaa0103doc "+
						" inner join abb01 on abb01id = eaa01central "+
						" inner join abm01 on abm01id = eaa0103item "+
						" left join aam06 on aam06id = abm01umu "+
						" left join abe01 on abe01id = abb01ent "+
						//" left join abm0102 on abm0102item = abm01id "+
						obterWherePadrao("eaa01","where") + whereNumIni + whereNumFim + whereEsData +
						whereEmissaoData + wherePcd + whereCfop + whereItem + whereCriteriosItem + whereEntidade + whereTipoDoc+whereClassDoc+whereES +whereMps
						"and eaa01cancdata is null "
						
		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni,parametroNumFim,parametroEsDataIni,parametroEsdataFim,parametroEmissaoDataIni,parametroEmissaodataFim,parametroPcd,
															parametroCfop,parametroItens,parametrocriteriosItem,parametroEntidade,parametroTipoDoc,parametroMps)
						
	}
	private TableMap obterDevolucao(Long idItem){
		def whereItem = " where eaa01033itemdoc = :item "
		
		def sql = " select eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json "+
					" from eaa01033 "+
					" inner join eaa0103 on eaa0103id = eaa01033item  "+
					"inner join eaa01 on eaa01id = eaa0103doc "+
					whereItem +
					"and eaa01cancdata is null " +
					//"and eaa01033qtComl > 0 "
					//+
					" and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "
		
		def p1 = criarParametroSql("item", idItem)
		return getAcessoAoBanco().buscarUnicoTableMap(sql,p1)
	}
	
	private void comporDevolucao(TableMap dado, TableMap devolucao, TableMap ItensJson, List<String> campos ) {
			for(campo in campos) {
				if(campo != null) {
					ItensJson.put(campo, ItensJson.getBigDecimal_Zero(campo) - devolucao.getTableMap("eaa0103json").getBigDecimal_Zero(campo))
				}
			}
			for(campo in ["eaa0103qtuso","eaa0103qtcoml","eaa0103unit","eaa0103total","eaa0103totdoc","eaa0103totfinanc"]) {
				dado.put(campo, dado.getBigDecimal_Zero(campo) - devolucao.getBigDecimal_Zero(campo))
			}
			
	}
	
	private String buscarCampo(TableMap dado, Map<String, String> nome) {
		for(campo in nome) {
			if(campo.value != null) {
				String nomeBanco = BuscarNomeBanco(campo.value)
				if(nomeBanco != null) {
					dado.put("nomeCampo"+campo.key, campo.value )
					dado.put("valor"+campo.key, dado.getBigDecimal_Zero(nomeBanco) )
				}else {
					nomeBanco = buscarNomeCampo(campo.value)
					dado.put("nomeCampo"+campo.key, nomeBanco)
					dado.put("valor"+campo.key, dado.getBigDecimal_Zero(campo.value))
					def t
				}
			}
		}
	}
	
	private String BuscarNomeBanco(String campo) {
		switch(campo) {
			case "Qtde. de Uso":
				return "eaa0103qtuso"
				break
			case "Qtde. Comercial":
				return "eaa0103qtcoml"
				break
			case "Preço Unitário":
				return "eaa0103unit"
				break
			case "Total do Item":
				return "eaa0103total"
				break
			case "Total Documento":
				return "eaa0103totdoc"
				break
			case "Total Financeiro":
				return "eaa0103totfinanc"
				break
			default:
				return null
				break
		}
	}
	
	private String buscarNomeCampo(String campo) {
		def sql = " select aah02descr from aah02 where aah02nome = :nome "
		return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))
		
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBQb3IgSXRlbnMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=