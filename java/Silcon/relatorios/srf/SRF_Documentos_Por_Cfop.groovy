package Silcon.relatorios.srf

import sam.model.entities.ea.Eaa01;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SRF_Documentos_Por_Cfop extends RelatorioBase {
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
		Integer numeroInicial = getInteger("numeroInicial") == null || getInteger("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal") == null || getInteger("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal");
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
			comporValores(dado, campos);
		}
		
		// Colocado para imprimir no mesmo arquivo por solicitação da colaboradora Deyse, devido processos do Alta;
		if(imprimir == 1) return gerarXLSX("SRF_Documentos_Por_CFOP_PDF",dados);
		return gerarPDF("SRF_Documentos_Por_CFOP_PDF",dados)
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


		String whereTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? "AND aah01id IN (:idTipoDocumento) " : "";
		String whereNumIni = numeroInicial != null ? "AND abb01num >= :numeroInicial " : "";
		String whereNumFin = numeroFinal != null ? "AND abb01num <= :numeroFinal " : "";
		String whereDataEmissaoIni = dtEmissaoIni != null ? "AND abb01data >= :dtEmissaoIni " : "";
		String whereDataEmissaoFin = dtEmissaoFin != null ? "AND abb01data <= :dtEmissaoFin " : "";
		String whereDataEntSaidaIni = dtEntSaidaIni != null ? "AND eaa01esdata >= :dtEntSaidaIni " : "";
		String whereDataEntSaidaFin = dtEntSaidaFin != null ? "AND eaa01esdata <= :dtEntSaidaFin " : "";
		String whereResumoOper = resumoOperacao == 0 ? "AND eaa01esmov = 0 " : "AND eaa01esmov = 1 ";
		String whereCfops = cfops != null && cfops.size() > 0 ? "AND aaj15id IN (:cfops) " : "";
		String wherePcd = pcd != null && pcd.size() > 0 ? "AND abd01id IN (:pcd) " : "";
		String whereEmpresa = "AND eaa01gc = :idEmpresa ";


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
		Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

		String sql = "SELECT "+campo1+ campo2+ campo3 + campo4 + campo5 + campo6 + "abb01num AS numDoc, abb01data AS dtEmissao, aah01codigo AS codTipoDoc, eaa01esdata AS esData,eaa01id, " +
				"abe01codigo AS codEnt, abe01id,abe01na AS naEnt, abe01ie AS cnpj,aaj15codigo AS codCFOP, aaj15descr AS descrCFOP,aaj15id, " +
				"sum(eaa0103qtuso) AS eaa0103qtuso,sum(eaa0103qtcoml) AS eaa0103qtcoml,sum(eaa0103unit) AS eaa0103unit, sum(eaa0103total) AS eaa0103total, sum(eaa0103totdoc) AS eaa0103totdoc , " +
				"sum(eaa0103totfinanc) AS eaa0103totfinanc " +
				"FROM eaa01 " +
				"INNER JOIN abb01 on abb01id = eaa01central  " +
				"INNER JOIN aah01 on aah01id = abb01tipo " +
				"INNER JOIN abe01 on abe01id = abb01ent " +
				"INNER JOIN eaa0103 on eaa0103doc = eaa01id " +
				"INNER JOIN aaj15 on aaj15id = eaa0103cfop " +
				"INNER JOIN abd01 on abd01id = eaa01pcd " +
				" WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
				" AND eaa01cancData IS NULL " +
				" AND eaa01nfestat <> 5 " +
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
				whereEmpresa +
				"GROUP BY abb01num, abb01data, aah01codigo, eaa01esdata,abe01id,eaa01id, " +
				"abe01codigo, abe01na, abe01ie,aaj15codigo,aaj15descr,aaj15id " +
				"ORDER by aaj15codigo,abb01num,abb01data,abe01codigo  ";

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroTipoDoc,parametroNumIni,parametroNumFin,parametroDataEmissaoIni,parametroDataEmissaoFin,parametroDataEntSaidaIni,parametroDataEntSaidaFin,
				parametroCfops,parametroPcd, parametroEmpresa)

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
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgcG9yIENGT1AiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgcG9yIENGT1AiLCJ0aXBvIjoicmVsYXRvcmlvIn0=