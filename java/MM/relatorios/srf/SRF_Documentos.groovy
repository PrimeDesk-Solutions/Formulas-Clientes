package MM.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SRF_Documentos extends RelatorioBase {

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
		filtrosDefault.put("devolucao", true)
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		def idEntidade = getListLong("entidade");
		def idTipoDocumento = getListLong("tipo");
		def criterios = getListLong("criterios");
		def numeroInicial = getInteger("numeroInicial");
		def numeroFinal = getInteger("numeroFinal");
		def dataEmissao = getIntervaloDatas("dataEmissao");
		def dataEntSai = getIntervaloDatas("dataEntSai");
		def resumoOperacao = getInteger("resumoOperacao");
		def criteriosCfop = getListLong("cfop");
		def criteriosPcd = getListLong("pcd");
		def estados = getListLong("estados");
		def municipios = getListLong("municipios");
		def nomeEntidade = getInteger("nomeEntidade");
		def imprimir = getInteger("impressao")
		def camposLivre1 = getString("campoLivre1")
		def camposFixo1 = getString("campoFixo1")
		def camposLivre2 = getString("campoLivre2")
		def camposFixo2 = getString("campoFixo2")
		def camposLivre3 = getString("campoLivre3")
		def camposFixo3 = getString("campoFixo3")
		def camposLivre4 = getString("campoLivre4")
		def camposFixo4 = getString("campoFixo4")
		def totalizar1 = getBoolean("total1")
		def totalizar2 = getBoolean("total2")
		def totalizar3 = getBoolean("total3")
		def totalizar4 = getBoolean("total4")
		def devolucoes = getBoolean("devolucao")

		if(camposLivre1 != null && camposFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre2 != null && camposFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre3 != null && camposFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
		if(camposLivre4 != null && camposFixo4 != null) interromper("Selecione apenas 1 valor por campo!")

		adicionarParametro("totalizar1", totalizar1)
		adicionarParametro("totalizar2", totalizar2)
		adicionarParametro("totalizar3", totalizar3)
		adicionarParametro("totalizar4", totalizar4)

		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		if (resumoOperacao.equals(1)) {
			params.put("TITULO_RELATORIO", "Documentos - Faturamento");
		} else {
			params.put("TITULO_RELATORIO", "Documentos - Recebimento");
		}

		if (dataEmissao != null) {
			params.put("PERIODO", "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		if (dataEntSai != null) {
			params.put("PERIODO", "Período Entrada/Saída: " + dataEntSai[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEntSai[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		HashMap<String, String> campos = new HashMap();

		campos.put("1", camposLivre1 != null ? camposLivre1 : camposFixo1 != null ? camposFixo1 : null);
		campos.put("2", camposLivre2 != null ? camposLivre2 : camposFixo2 != null ? camposFixo2 : null);
		campos.put("3", camposLivre3 != null ? camposLivre3 : camposFixo3 != null ? camposFixo3 : null);
		campos.put("4", camposLivre4 != null ? camposLivre4 : camposFixo4 != null ? camposFixo4 : null);


		def dados = obterDadosRelatorio(idEntidade, idTipoDocumento, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, nomeEntidade,criteriosPcd, estados, municipios, camposLivre1, camposLivre2, camposLivre3, camposLivre4);
		for(dado in dados){
			if(devolucoes){
				comporDevolucoes(dado, [camposLivre1, camposLivre2, camposLivre3, camposLivre4] )
			}
			comporValores(dado,campos)

		}


		if(imprimir == 1 )return gerarXLSX(dados);
		return gerarPDF("SRF_Documentos", dados);
	}

	public List<TableMap> obterDadosRelatorio (List<Long> idEntidade, List<Long> idTipoDocumento, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial,
											   Integer numeroFinal, Integer resumoOperacao, Integer nomeEntidade, List<Long>pcd, List<Long> estados, List<Long> municipios, String camposLivre1, String camposLivre2, String camposLivre3, String camposLivre4 )  {

		def whereData = ""
		def data = "";
		if (dataEmissao != null) {
			data = " abb01data";
			whereData = dataEmissao[0] != null && dataEmissao[1] != null ? " and abb01data >= '" + dataEmissao[0] + "' and abb01data <= '" + dataEmissao[1] + "'": "";
		} else {
			if (dataEntSai != null) {
				data = " eaa01esData";
				whereData = dataEntSai[0] != null && dataEntSai[1] != null ? " and eaa01esData >= '" + dataEntSai[0] + "' and eaa01esData <= '" + dataEntSai[1] + "'": "";
			}
		}

		def entidade = "";
		def groupBy = "";
		if (nomeEntidade.equals(0)) {
			entidade = " abe01.abe01na as nomeEntidade,";
			groupBy = " group by abb01num, abb01data, eaa01esdata, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, abe01na, eaa01json,eaa01id,abd01codigo, abd01descr ";
		} else {
			entidade = " abe01nome as nomeEntidade,";
			groupBy = " group by abb01num, abb01data, eaa01esdata, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, abe01nome, eaa01json,eaa01id,abd01codigo, abd01descr ";
		}

		def whereResumoOperacao = null;
		if (resumoOperacao.equals(0)) {
			whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_ENTRADA;
		} else {
			whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_SAIDA;
		}

		def whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01num >= '" + numeroInicial + "' and abb01num <= '" + numeroFinal + "'": "";
		def whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01id IN (:idTipoDocumento)": "";
		def whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01id IN (:idEntidade)": "";
		def orderBy = data != null && data.size() > 0 ? " order by " + data + " , aah01codigo, abb01num,abd01codigo, abd01descr ": " order by abb01data, aah01codigo, abb01num, abd01codigo, abd01descr ";
		def whereCriterioPcd = pcd != null && pcd.size() > 0 ? " and eaa01.eaa01pcd IN (:pcd) " : "";
		def whereEstados = estados != null && estados.size() > 0 ? "and aag02id in (:estados) " : "";
		def whereMunicipios = municipios != null && municipios.size() > 0 ? "and aag0201id in (:municipios) " : "";

		def parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? criarParametroSql("idEntidade", idEntidade) : null;
		def parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? criarParametroSql("idTipoDocumento", idTipoDocumento) : null;
		def parametroCriteriosPcd = pcd != null && pcd.size() > 0 ? criarParametroSql("pcd", pcd) : null;
		def parametroEstados = estados != null && estados.size() > 0 ? Parametro.criar("estados",estados) : null;
		def parametroMunicipios = municipios != null && municipios.size() > 0 ? Parametro.criar("municipios",municipios) : null;


		String campo1 = camposLivre1 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + camposLivre1+"'" + " as NUMERIC(18,2))) AS " + camposLivre1 + ",  " : "";
		String campo2 = camposLivre2 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + camposLivre2+"'" + " as NUMERIC(18,2))) AS " + camposLivre2 + ", " : "";
		String campo3 = camposLivre3 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + camposLivre3+"'" + " as NUMERIC(18,2))) AS " + camposLivre3 + ", "  : "";
		String campo4 = camposLivre4 != null ? "SUM(CAST(eaa0103doc.eaa0103json ->> '" + camposLivre4+"'" + " as NUMERIC(18,2))) AS " + camposLivre4 + ", "  : "";

		String campoDev1 = camposLivre1 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + camposLivre1+"'" + " as NUMERIC(18,2))) AS " + camposLivre1 + "dev"+ ",  " : "";
		String campoDev2 = camposLivre2 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + camposLivre2+"'" + " as NUMERIC(18,2))) AS " + camposLivre2 + "dev"+ ", " : "";
		String campoDev3 = camposLivre3 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + camposLivre3+"'" + " as NUMERIC(18,2))) AS " + camposLivre3 + "dev"+ ", "  : "";
		String campoDev4 = camposLivre4 != null ? "SUM(CAST(eaa0103dev.eaa0103json ->> '" + camposLivre4+"'" + " as NUMERIC(18,2))) AS " + camposLivre4 + "dev"+ ", "  : "";

		def sql = " select " + campo1 + campo2 + campo3 + campo4 + campoDev1 + campoDev2+ campoDev3+ campoDev4 +"abd01codigo, abd01descr,abb01num, abb01data,eaa01id, eaa01esdata, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, " +
				"        sum(eaa0103doc.eaa0103qtUso) as eaa0103qtuso, sum(eaa0103doc.eaa0103qtComl) as eaa0103qtcoml,  sum(eaa0103doc.eaa0103unit) as eaa0103unit,"+
				" 	     sum(eaa0103doc.eaa0103total) as eaa0103total, sum(eaa0103doc.eaa0103totDoc) as eaa0103totdoc, sum(eaa0103doc.eaa0103totFinanc) as eaa0103totfinanc,  " + entidade +
				"		 COALESCE(SUM(EAA01033qtComl),0) as eaa0103qtcomldev, "+
				"		 COALESCE(SUM(eaa0103dev.eaa0103qtuso),0) as eaa0103qtusodev, "+
				"		 COALESCE(SUM(eaa0103dev.eaa0103unit),0) as eaa0103unitdev, "+
				"		 COALESCE(SUM(eaa0103dev.eaa0103total),0) as eaa0103totaldev, "+
				"		 COALESCE(SUM(eaa0103dev.eaa0103totdoc),0) as eaa0103totdocdev, "+
				"		 COALESCE(SUM(eaa0103dev.eaa0103totfinanc),0) as eaa0103totfinancdev "+
				"        from eaa0103 as eaa0103doc  " +
				"        left join eaa01 on eaa01id = eaa0103doc.eaa0103doc " +
				"	     left join abd01 on abd01id = eaa01pcd  							"+
				"        left join abb01 on abb01id = eaa01central " +
				"        left join aah01 on aah01id = abb01tipo " +
				"        left join abe01 on abe01id = abb01ent " +
				"        left join abe30 on abe30id = eaa01cp  " +
				"        left join abe40 on abe40id = eaa01tp  " +
				"        left join abm01 on abm01id = eaa0103doc.eaa0103item " +
				"	     left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
				"        left join aag0201 on aag0201id = abe0101municipio "+
				"        left join aag02 on aag02id = aag0201uf " +
				"        left join EAA01033 on EAA01033itemdoc = EAA0103DOC.EAA0103id "+
				"		 left join eaa0103 as eaa0103dev on eaa0103dev.eaa0103id = eaa01033item and eaa0103dev.eaa0103tipo <> 3"+
				"        where eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
				getSamWhere().getWherePadrao(" AND ", Eaa01.class) +
				"        and eaa01cancData is null "+
				"        and eaa01nfestat <> 5 "+
				whereNumero +
				whereTipoDocumento +
				whereIdEntidade +
				whereData +
				whereResumoOperacao +
				whereCriterioPcd+
				whereEstados +
				whereMunicipios +
				groupBy +
				orderBy;

		def receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento, parametroCriteriosPcd,parametroEstados,parametroMunicipios);
		return receberDadosRelatorio;
	}

	private void comporDevolucoes(TableMap dado, List<String> camposLivres){
		for(campo in camposLivres){
			if(campo != null){
				dado.put(campo, dado.getBigDecimal_Zero(campo) - dado.getBigDecimal_Zero(campo + "dev"))
			}
		}

		for(campo in ["eaa0103qtuso", "eaa0103qtcoml", "eaa0103unit", "eaa0103total", "eaa0103totdoc", "eaa0103totfinanc" ]){
			dado.put(campo, dado.getBigDecimal_Zero(campo) - dado.getBigDecimal_Zero(campo + "dev"))
		}
	}


	private List<Long> buscarItensDocumento(Long idDocumento){
		String sql = "SELECT eaa0103id FROM eaa0103 WHERE eaa0103doc = :idDocumento ";

		return getAcessoAoBanco().obterListaDeLong(sql, Parametro.criar("idDocumento", idDocumento))
	}


	private void comporValores(TableMap dado, HashMap<String, String> campos){
		for(campo in campos){
			if(campo.value != null){
				String nomeCampo = buscarNomeCampoFixo(campo.value);
				if(nomeCampo != null){
					dado.put("nomeCampo"+campo.key, campo.value);
					dado.put("valorCampo"+campo.key, dado.getBigDecimal_Zero(nomeCampo))
				}else{
					nomeCampo = buscarNomeCampoLivre(campo.value);
					dado.put("nomeCampo"+campo.key, nomeCampo);
					dado.put("valorCampo"+campo.key, dado.getBigDecimal_Zero(campo.value) );
				}
			}
		}
	}

	public String buscarNomeCampoFixo(String campo) {
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
		}
	}

	public String buscarNomeCampoLivre(String campo) {
		def sql = " select aah02descr from aah02 where aah02nome = :nome "
		return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

	}

	@Override
	public String getNomeTarefa() {
		return "SRF - Documentos";
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=