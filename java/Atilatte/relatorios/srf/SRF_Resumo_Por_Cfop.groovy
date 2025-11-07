package Atilatte.relatorios.srf

import sam.model.entities.aa.Aac10;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SRF_Resumo_Por_Cfop extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SRF - Resumo por CFOP";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("resumoOperacao", "0");
		filtrosDefault.put("nomeEntidade", "0");
		filtrosDefault.put("impressao", "0");
		filtrosDefault.put("total1", true)
		filtrosDefault.put("total2", true)
		filtrosDefault.put("total3", true)
		filtrosDefault.put("total4", true)
		filtrosDefault.put("total5", true)
		filtrosDefault.put("total6", true)
		filtrosDefault.put("devolucao", true)
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		List<Long> idsTipoDoc = getListLong("tipo");
		List<Long> idsPcd = getListLong("pcd");
		List<Long> idsCfop = getListLong("cfop");
		Integer resumoOperacao = getInteger("resumoOperacao");
		LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		boolean totalizar1 = getBoolean("total1");
		boolean totalizar2 = getBoolean("total2");
		boolean totalizar3 = getBoolean("total3");
		boolean totalizar4 = getBoolean("total4");
		boolean totalizar5 = getBoolean("total5");
		boolean totalizar6 = getBoolean("total6");
		String campoFixo1 = getString("campoFixo1");
		String campoFixo2 = getString("campoFixo2");
		String campoFixo3 = getString("campoFixo3");
		String campoFixo4 = getString("campoFixo4");
		String campoFixo5 = getString("campoFixo5");
		String campoFixo6 = getString("campoFixo6");
		String campoLivre1 = getString("campoLivre1");
		String campoLivre2 = getString("campoLivre2");
		String campoLivre3 = getString("campoLivre3");
		String campoLivre4 = getString("campoLivre4");
		String campoLivre5 = getString("campoLivre5");
		String campoLivre6 = getString("campoLivre6");
		Integer impressao = getInteger("impressao");
		Aac10 empresa = obterEmpresaAtiva();
		Long idEmpresa = empresa.aac10id;
		
		if(campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!")

		String periodo = ""
		if(dtEmissao != null) {
			periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}else if(dtEntradaSaida) {
			periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}

		params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
		params.put("titulo","SRF - Resumo Por CFOP");
		params.put("periodo",periodo);
		params.put("totalizar1",totalizar1);
		params.put("totalizar2",totalizar2);
		params.put("totalizar3",totalizar3);
		params.put("totalizar4",totalizar4);
		params.put("totalizar5",totalizar5);
		params.put("totalizar6",totalizar6);

		Map<String,String> campos = new HashMap()
		campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null)
		campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null)
		campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null)
		campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null)
		campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null)
		campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null)

		List<TableMap> dados = buscarDocumentos(numDocIni,numDocFin,dtEmissao,dtEntradaSaida,resumoOperacao,idsCfop,idsPcd,idsTipoDoc, idEmpresa,campoLivre1,campoLivre2,campoLivre3,campoLivre4,campoLivre5,campoLivre6);

		for(dado in dados){
			comporValores(dado, campos);
		}

		if(impressao == 1) return gerarXLSX("SRF_Resumo_Por_CFOP_Excel",dados);

		return gerarPDF("SRF_Resumo_Por_CFOP_PDF",dados)
	}

	private List<TableMap> buscarDocumentos(Integer numDocIni,Integer numDocFin,LocalDate[] dtEmissao,LocalDate[] dtEntradaSaida,Integer resumoOperacao,List<Long>idsCfop,List<Long>idsPcd, List<Long> idsTipoDoc, Long idEmpresa,
											String campoLivre1,String campoLivre2,String campoLivre3,String campoLivre4,String campoLivre5,String campoLivre6){

		String whereNumIni = numDocIni != null ? "AND abb01num >= :numDocIni " : "";
		String whereNumFin = numDocFin != null ? "AND abb01num <= :numDocFin " : "";
		String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
		String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id IN (:idsPcd) " : "";
		String whereDtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? "AND abb01data between :dtEmissIni AND :dtEmissFin " : "";
		String whereDtEntradaSaida = dtEntradaSaida != null && dtEntradaSaida != null ? "AND eaa01esdata BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
		String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0";
		String whereCfops = idsCfop != null && idsCfop.size() > 0 ? "AND aaj15id IN (:idsCfop) " : "";
		String whereEmpresa = "AND eaa01gc = :idEmpresa ";

		String campo1 = campoLivre1 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre1+"'"+" as NUMERIC(18,2))) AS " + campoLivre1 + ",  " : "";
		String campo2 = campoLivre2 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre2+"'"+" as NUMERIC(18,2))) AS " + campoLivre2 + ", " : "";
		String campo3 = campoLivre3 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre3+"'"+" as NUMERIC(18,2))) AS " + campoLivre3 + ", "  : "";
		String campo4 = campoLivre4 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre4+"'"+" as NUMERIC(18,2))) AS " + campoLivre4 + ", "  : "";
		String campo5 = campoLivre5 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre5+"'"+" as NUMERIC(18,2))) AS " + campoLivre5 + ", "  : "";
		String campo6 = campoLivre6 != null ? "SUM(CAST(eaa0103json ->> '"+campoLivre6+"'"+" as NUMERIC(18,2))) AS " + campoLivre6 + ", "  : "";

		Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
		Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
		Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
		Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
		Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
		Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
		Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
		Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
		Parametro parametroCfops = idsCfop != null && idsCfop.size() > 0 ? Parametro.criar("idsCfop",idsCfop) : null;
		Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);

		String sql = "SELECT "+campo1+ campo2+ campo3 + campo4 + campo5 + campo6 +
				"aaj15codigo AS codCFOP, aaj15descr AS descrCFOP,aaj15id, " +
				"SUM(eaa0103qtuso) AS eaa0103qtuso,SUM(eaa0103qtcoml) AS eaa0103qtcoml,SUM(eaa0103unit) AS eaa0103unit, SUM(eaa0103total) AS eaa0103total, SUM(eaa0103totdoc) AS eaa0103totdoc , " +
				"SUM(eaa0103totfinanc) AS eaa0103totfinanc " +
				"FROM eaa01 " +
				"INNER JOIN abb01 ON abb01id = eaa01central  " +
				"INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
				"LEFT JOIN aaj15 ON aaj15id = eaa0103cfop " +
				"INNER JOIN aah01 ON aah01id = abb01tipo " +
				"INNER JOIN abd01 ON abd01id = eaa01pcd "+
				"WHERE eaa01clasDoc = 1 " +
				"AND eaa01cancData IS NULL " +
				"AND eaa01nfestat <> 5 " +
				whereNumIni +
				whereNumFin +
				whereTipoDoc +
				wherePcd +
				whereDtEmissao +
				whereDtEntradaSaida +
				whereES +
				whereCfops +
				whereEmpresa +
				"GROUP BY aaj15codigo,aaj15descr,aaj15id " +
				"ORDER BY aaj15codigo  ";

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin, parametroCfops, parametroEmpresa)

	}

	private void comporValores(TableMap dado, Map<String,String> campos){
		for(campo in campos){
			if(campo.value != null){
				String nomeCampo = buscarNomeBanco(campo.value);
				if(nomeCampo != null){
					dado.put("nomeCampo"+ campo.key, campo.value);
					dado.put("valorCampo"+campo.key,dado.getBigDecimal_Zero(nomeCampo));
				}else{
					nomeCampo = buscarNomeCampoLivre(campo.value);
					dado.put("nomeCampo" + campo.key, nomeCampo)
					dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value))
				}
			}
		}
	}

	private String buscarNomeBanco(String campo) {
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

	private String buscarNomeCampoLivre(String campo) {
		def sql = " select aah02descr from aah02 where aah02nome = :nome "
		return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgcG9yIENGT1AgLSBMQ1IiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgcG9yIENGT1AiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBwb3IgQ0ZPUCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBwb3IgQ0ZPUCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==