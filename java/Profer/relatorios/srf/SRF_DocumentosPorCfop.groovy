package Profer.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SRF_DocumentosPorCfop extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SRF - Documentos por CFOP";
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
		List<Long> idTipoDocumento = getListLong("tipos");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dataEntSai = getIntervaloDatas("dataEntSai");
		Integer resumoOperacao = getInteger("resumoOperacao");
		List<Long> cfops = getListLong("cfop");
		List<Long> pcd = getListLong("pcd");
		Integer imprimir = getInteger("impressao")
		String camposLivre1 = getString("campoLivre1")
		String camposFixo1 = getString("campoFixo1")
		String camposLivre2 = getString("campoLivre2")
		String camposFixo2 = getString("campoFixo2")
		String camposLivre3 = getString("campoLivre3")
		String camposFixo3 = getString("campoFixo3")
		String camposLivre4 = getString("campoLivre4")
		String camposFixo4 = getString("campoFixo4")
		String camposLivre5 = getString("campoLivre5")
		String camposFixo5 = getString("campoFixo5")
		String camposLivre6 = getString("campoLivre6")
		String camposFixo6 = getString("campoFixo6")
		Boolean totalizar1 = getBoolean("total1")
		Boolean totalizar2 = getBoolean("total2")
		Boolean totalizar3 = getBoolean("total3")
		Boolean totalizar4 = getBoolean("total4")
		Boolean totalizar5 = getBoolean("total5")
		Boolean totalizar6 = getBoolean("total6")
		Boolean considerarDevolucoes = getBoolean("devolucao");

		if(camposLivre1 != null && camposFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre2 != null && camposFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre3 != null && camposFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre4 != null && camposFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre5 != null && camposFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre6 != null && camposFixo6 != null) interromper("Selecione apenas 1 valor por campo!")
		
		String periodo = ""
		if(dataEmissao != null) {
			periodo = "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}else if(dataEntSai) {
			periodo = "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}
		
		params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
		params.put("titulo","SRF - Documentos Por CFOP");
		params.put("periodo",periodo);
		params.put("totalizar1",totalizar1);
		params.put("totalizar2",totalizar2);
		params.put("totalizar3",totalizar3);
		params.put("totalizar4",totalizar4);
		params.put("totalizar5",totalizar5);
		params.put("totalizar6",totalizar6);

		Map<String,String> campos = new HashMap()
		campos.put("1", camposLivre1 != null ? camposLivre1 : camposFixo1 != null ? camposFixo1 : null   )
		campos.put("2", camposLivre2 != null ? camposLivre2 : camposFixo2 != null ? camposFixo2 : null   )
		campos.put("3", camposLivre3 != null ? camposLivre3 : camposFixo3 != null ? camposFixo3 : null   )
		campos.put("4", camposLivre4 != null ? camposLivre4 : camposFixo4 != null ? camposFixo4 : null   )
		campos.put("5", camposLivre5 != null ? camposLivre5 : camposFixo5 != null ? camposFixo5 : null   )
		campos.put("6", camposLivre6 != null ? camposLivre6 : camposFixo6 != null ? camposFixo6 : null   )

		List<TableMap> dados = buscarDocumentos(idTipoDocumento,numeroInicial,numeroFinal,dataEmissao,dataEntSai,resumoOperacao,cfops,pcd,camposLivre1,camposLivre2,camposLivre3,camposLivre4,camposLivre5,camposLivre6);

		for(dado in dados){
			Long idDocumento = dado.getLong("eaa01id");
			Long idCfop = dado.getLong("aaj15id");

//			if(considerarDevolucoes){
//				TableMap devolucoes = buscarDevolucoes(idDocumento,idCfop,camposLivre1,camposLivre2,camposLivre3,camposLivre4,camposLivre5,camposLivre6)
//				comporDevolucoes(dado,devolucoes,[camposLivre1,camposLivre2,camposLivre3,camposLivre4,camposLivre5,camposLivre6]);
//			}

			comporValores(dado, campos);

		}
		
		// Colocado para imprimir no mesmo arquivo por solicitação da colaboradora Deyse, devido processos do Alta;
		if(imprimir == 1) return gerarXLSX("SRF_DocumentosPorCFOP(PDF)",dados);
		return gerarPDF("SRF_DocumentosPorCFOP(PDF)",dados)
	}

	private List<TableMap> buscarDocumentos(List<Long>idTipoDocumento,Integer numeroInicial,Integer numeroFinal,LocalDate[] dataEmissao,LocalDate[] dataEntSai,Integer resumoOperacao,List<Long>cfops,List<Long>pcd,
											String camposLivre1,String camposLivre2,String camposLivre3,String camposLivre4,String camposLivre5,String camposLivre6){

		// Data Emissão
		LocalDate dtEmissaoIni = null;
		LocalDate dtEmissaoFin = null;

		if(dataEmissao != null){
			dtEmissaoIni = dataEmissao[0];
			dtEmissaoFin = dataEmissao[1];
		}

		// Data Entrada/Saída
		LocalDate dtEntSaidaIni = null;
		LocalDate dtEntSaidaFin = null;

		if(dataEntSai != null){
			dtEntSaidaIni = dataEntSai[0];
			dtEntSaidaFin = dataEntSai[1];
		}


		String whereCancData = "and eaa01cancdata is null ";
		String whereClassDoc = " and eaa01clasDoc = 1 "
		String whereTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? "and aah01id in (:idTipoDocumento) " : "";
		String whereNumIni = numeroInicial != null ? "and abb01num >= :numeroInicial " : "";
		String whereNumFin = numeroFinal != null ? "and abb01num <= :numeroFinal " : "";
		String whereDataEmissaoIni = dtEmissaoIni != null ? "and abb01data >= :dtEmissaoIni " : "";
		String whereDataEmissaoFin = dtEmissaoFin != null ? "and abb01data <= :dtEmissaoFin " : "";
		String whereDataEntSaidaIni = dtEntSaidaIni != null ? "and eaa01esdata >= :dtEntSaidaIni " : "";
		String whereDataEntSaidaFin = dtEntSaidaFin != null ? "and eaa01esdata <= :dtEntSaidaFin " : "";
		String whereResumoOper = resumoOperacao == 0 ? "and eaa01esmov = 0 " : "and eaa01esmov = 1 ";
		String whereCfops = cfops != null && cfops.size() > 0 ? "and aaj15id in (:cfops) " : "";
		String wherePcd = pcd != null && pcd.size() > 0 ? "and abd01id in (:pcd) " : "";

		String campo1 = camposLivre1 != null ? "SUM(CAST(eaa0103json ->> '"+camposLivre1+"'"+" as NUMERIC(18,2))) AS " + camposLivre1 + ",  " : "";
		String campo2 = camposLivre2 != null ? "SUM(CAST(eaa0103json ->> '"+camposLivre2+"'"+" as NUMERIC(18,2))) AS " + camposLivre2 + ", " : "";
		String campo3 = camposLivre3 != null ? "SUM(CAST(eaa0103json ->> '"+camposLivre3+"'"+" as NUMERIC(18,2))) AS " + camposLivre3 + ", "  : "";
		String campo4 = camposLivre4 != null ? "SUM(CAST(eaa0103json ->> '"+camposLivre4+"'"+" as NUMERIC(18,2))) AS " + camposLivre4 + ", "  : "";
		String campo5 = camposLivre5 != null ? "SUM(CAST(eaa0103json ->> '"+camposLivre5+"'"+" as NUMERIC(18,2))) AS " + camposLivre5 + ", "  : "";
		String campo6 = camposLivre6 != null ? "SUM(CAST(eaa0103json ->> '"+camposLivre6+"'"+" as NUMERIC(18,2))) AS " + camposLivre6 + ", "  : "";


		Parametro parametroTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento",idTipoDocumento) : null;
		Parametro parametroNumIni = numeroInicial != null ? Parametro.criar("numeroInicial",numeroInicial) : null;
		Parametro parametroNumFin = numeroFinal != null ? Parametro.criar("numeroFinal",numeroFinal) : null;
		Parametro parametroDataEmissaoIni = dtEmissaoIni != null ? Parametro.criar("dtEmissaoIni",dtEmissaoIni) : null;
		Parametro parametroDataEmissaoFin = dtEmissaoFin != null ? Parametro.criar("dtEmissaoFin",dtEmissaoFin) : null;
		Parametro parametroDataEntSaidaIni = dtEntSaidaIni != null ? Parametro.criar("dtEntSaidaIni",dtEntSaidaIni) : null;
		Parametro parametroDataEntSaidaFin = dtEntSaidaFin != null ? Parametro.criar("dtEntSaidaFin",dtEntSaidaFin) : null;
		Parametro parametroCfops = cfops != null && cfops.size() > 0 ? Parametro.criar("cfops",cfops) : null;
		Parametro parametroPcd = pcd != null && pcd.size() > 0 ? Parametro.criar("pcd",pcd) : null;

		String sql = "select "+campo1+ campo2+ campo3 + campo4 + campo5 + campo6 + "abb01num as numDoc, abb01data as dtEmissao, aah01codigo as codTipoDoc, eaa01esdata as esData,eaa01id, " +
				"abe01codigo as codEnt, abe01id,abe01na as naEnt, abe01ie as cnpj,aaj15codigo as codCFOP, aaj15descr as descrCFOP,aaj15id, " +
				"sum(eaa0103qtuso) as eaa0103qtuso,sum(eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit, sum(eaa0103total) as eaa0103total, sum(eaa0103totdoc) as eaa0103totdoc , " +
				"sum(eaa0103totfinanc) as eaa0103totfinanc " +
				"from eaa01 " +
				"inner join abb01 on abb01id = eaa01central  " +
				"inner join aah01 on aah01id = abb01tipo " +
				"inner join abe01 on abe01id = abb01ent " +
				"inner join eaa0103 on eaa0103doc = eaa01id " +
				"inner join aaj15 on aaj15id = eaa0103cfop " +
				"inner join abd01 on abd01id = eaa01pcd " +
				obterWherePadrao("eaa01","where")+
				whereCancData+
				whereClassDoc+
				whereTipoDoc+
				whereNumIni+
				whereNumFin+
				whereDataEmissaoIni+
				whereDataEmissaoFin+
				whereDataEntSaidaIni+
				whereDataEntSaidaFin+
				whereResumoOper+
				whereCfops+
				wherePcd+
				"group by abb01num, abb01data, aah01codigo, eaa01esdata,abe01id,eaa01id, " +
				"abe01codigo, abe01na, abe01ie,aaj15codigo,aaj15descr,aaj15id " +
				"order by aaj15codigo,abb01num,abb01data,abe01codigo  ";

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroTipoDoc,parametroNumIni,parametroNumFin,parametroDataEmissaoIni,parametroDataEmissaoFin,parametroDataEntSaidaIni,parametroDataEntSaidaFin,
				parametroCfops,parametroPcd)

	}

	private void comporDevolucoes(TableMap dados, TableMap devolucoes, List<String> campos){
		for(campo in campos){
			if(campo != null){
				dados.put(campo, dados.getBigDecimal_Zero(campo) - devolucoes.getBigDecimal_Zero(campo))
			}
		}

		for(campo in ["eaa0103qtuso","eaa0103qtcoml","eaa0103unit","eaa0103total","eaa0103totdoc","eaa0103totfinanc"]){
			dados.put(campo, dados.getBigDecimal_Zero(campo) - devolucoes.getBigDecimal_Zero(campo))
		}
	}


	private TableMap buscarDevolucoes(Long idDocumento, Long idCfop, String camposLivre1,String camposLivre2,String camposLivre3,String camposLivre4,String camposLivre5,String camposLivre6){

		String whereIdDoc = "where eaa01origem.eaa01id = :idDocumento ";
		String whereIdCfop = "and eaa0103origem.eaa0103cfop = :idCfop ";

		String campo1 = camposLivre1 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '"+camposLivre1+"'"+" as NUMERIC(18,2))) AS " + camposLivre1 + ",  " : "";
		String campo2 = camposLivre2 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '"+camposLivre2+"'"+" as NUMERIC(18,2))) AS " + camposLivre2 + ", " : "";
		String campo3 = camposLivre3 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '"+camposLivre3+"'"+" as NUMERIC(18,2))) AS " + camposLivre3 + ", "  : "";
		String campo4 = camposLivre4 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '"+camposLivre4+"'"+" as NUMERIC(18,2))) AS " + camposLivre4 + ", "  : "";
		String campo5 = camposLivre5 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '"+camposLivre5+"'"+" as NUMERIC(18,2))) AS " + camposLivre5 + ", "  : "";
		String campo6 = camposLivre6 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '"+camposLivre6+"'"+" as NUMERIC(18,2))) AS " + camposLivre6 + " "  : "";

		String sql = "SELECT " + campo1 + campo2 + campo3 + campo4 + campo5 + campo6 +
						"SUM(eaa01033qtComl) as eaa0103qtcoml, SUM(eaa01033qtUso) as eaa01033qtuso,  " +
						"sum(eaa0103dev.eaa0103qtuso) as eaa0103qtuso,sum(eaa0103dev.eaa0103qtcoml) as eaa0103qtcoml,sum(eaa0103dev.eaa0103unit) as eaa0103unit, sum(eaa0103dev.eaa0103total) as eaa0103total, sum(eaa0103dev.eaa0103totdoc) as eaa0103totdoc , " +
						"sum(eaa0103dev.eaa0103totfinanc) as eaa0103totfinanc  " +
						"from eaa01033 " +
						"inner join eaa0103 as eaa0103dev on eaa0103dev.eaa0103id = eaa01033item " +
						"inner join eaa01 as eaa01dev on eaa01dev.eaa01id = eaa0103dev.eaa0103doc " +
						"inner join eaa0103 as eaa0103origem on eaa0103origem.eaa0103id = eaa01033itemdoc " +
						"inner join eaa01 as eaa01origem on eaa01origem.eaa01id = eaa0103origem.eaa0103doc " +
						whereIdDoc +
				        whereIdCfop;
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDocumento",idDocumento),Parametro.criar("idCfop",idCfop))
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
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgcG9yIENGT1AiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgcG9yIENGT1AiLCJ0aXBvIjoicmVsYXRvcmlvIn0=