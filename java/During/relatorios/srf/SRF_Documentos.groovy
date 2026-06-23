package During.relatorios.srf;

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
		def camposLivre1 = get("campoLivre1")
		def camposFixo1 = get("campoFixo1")
		def camposLivre2 = get("campoLivre2")
		def camposFixo2 = get("campoFixo2")
		def camposLivre3 = get("campoLivre3")
		def camposFixo3 = get("campoFixo3")
		def camposLivre4 = get("campoLivre4")
		def camposFixo4 = get("campoFixo4")
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
		
		
		def dados = obterDadosRelatorio(idEntidade, idTipoDocumento, criterios, dataEmissao, dataEntSai, numeroInicial, numeroFinal, resumoOperacao, nomeEntidade,criteriosCfop,criteriosPcd, estados, municipios);
		
		
		if( ( camposFixo1 != null || camposFixo2 != null || camposFixo3 != null || camposFixo4 != null ) || ( camposLivre1 != null || camposLivre2 != null || camposLivre3 != null || camposLivre4 != null )  ) {
			
			for( dado in dados) {
				if(dado.getTableMap("eaa01json") != null && dado.getTableMap("eaa01json").size() > 0 ) dado.putAll(dado.getTableMap("eaa01json"))
				
				TableMap dadoDevolucao = null
				if(devolucoes) {
					dadoDevolucao = obterDevolucao(dado.getLong("eaa0103id"))
					if( dadoDevolucao != null ) {
						if(dadoDevolucao.getTableMap("eaa0103json") != null && dadoDevolucao.getTableMap("eaa0103json").size()> 0) {
							dadoDevolucao.putAll(dadoDevolucao.getTableMap("eaa0103json"))
						}
					}
				}
				def nomeCampo = ""
				def valorCampo = ""
				
				if(camposLivre1 != null || camposFixo1 != null) {
					if(camposLivre1 != null) {
						nomeCampo = buscarNomeCampo(camposLivre1)
						valorCampo  = camposLivre1
						dado.put("nomeCampo1", nomeCampo)
						def valor1 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo1", valor1 )
						dado.put("campoControle1", camposLivre1)
					}
					if(camposFixo1 != null) {
						nomeCampo = camposFixo1
						valorCampo  = BuscarNomeBanco(camposFixo1)
						dado.put("nomeCampo1", nomeCampo)
						def valor1 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo1", valor1 )						
					}
				}
				if(camposLivre2 != null || camposFixo2 != null) {
					if(camposLivre2 != null) {
						nomeCampo = buscarNomeCampo(camposLivre2)
						valorCampo  = camposLivre2
						dado.put("nomeCampo2", nomeCampo)
						def valor2 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo2", valor2 )
						dado.put("campoControle2", camposLivre2)
					}
					if(camposFixo2 != null) {
						nomeCampo = camposFixo2
						valorCampo  = BuscarNomeBanco(camposFixo2)
						dado.put("nomeCampo2", nomeCampo)
						def valor2 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo2", valor2 )
					}
				}
				if(camposLivre3 != null || camposFixo3 != null) {
					if(camposLivre3 != null) {
						nomeCampo = buscarNomeCampo(camposLivre3)
						valorCampo  = camposLivre3
						dado.put("nomeCampo3", nomeCampo)
						def valor3 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo3", valor3 )
						dado.put("campoControle3", camposLivre3)
					}
					if(camposFixo3 != null) {
						nomeCampo = camposFixo3
						valorCampo  = BuscarNomeBanco(camposFixo3)
						dado.put("nomeCampo3", nomeCampo)
						def valor3 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo3", valor3 )
					}
				}
				if(camposLivre4 != null || camposFixo4 != null) {
					if(camposLivre4 != null) {
						nomeCampo = buscarNomeCampo(camposLivre4)
						valorCampo  = camposLivre4
						dado.put("nomeCampo4", nomeCampo)
						def valor4 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo4", valor4 )
						dado.put("campoControle4", camposLivre4)
					}
					if(camposFixo4 != null) {
						nomeCampo = camposFixo4
						valorCampo  = BuscarNomeBanco(camposFixo4)
						dado.put("nomeCampo4", nomeCampo)
						def valor4 = dado.get(valorCampo) == null ? new BigDecimal(0) : dado.get(valorCampo)
						dado.put("valorCampo4", valor4 )
					}
				}
				
				if(devolucoes) {
					def eaa01id = dado.get("eaa01id")
					List<Long> itens = obterItensDoc(eaa01id)
					if(itens != null && itens.size() > 0 ) {
						def devolucao1 = 0.0, devolucao2 = 0.0, devolucao3 =0.0, devolucao4 =0.0
						def nome1,nome2,nome3,nome4
						for(item in itens) {
							TableMap dadosDevolucao = obterDevolucao(item)
							if(dadosDevolucao != null) {
								if(dadosDevolucao.getBigDecimal("eaa01033qtComl") > 0 || dadosDevolucao.getBigDecimal("eaa01033qtUso") > 0 ){
									dadosDevolucao.putAll(dadosDevolucao.getTableMap("eaa0103json"))
									if(dado.get("nomeCampo1") != null ) {
										nome1 = BuscarNomeBanco(dado.get("nomeCampo1"))
										if(nome1 == null) {
											nome1 = dado.get("campoControle1")
											if(nome1 != null)devolucao1  += dadosDevolucao.getBigDecimal_Zero(nome1)
										}else {
											devolucao1  += dadosDevolucao.getBigDecimal_Zero(nome1)
										}
									}
									if(dado.get("nomeCampo2") != null ) {
										nome2 = BuscarNomeBanco(dado.get("nomeCampo2"))
										if(nome2 == null) {
											nome2 = dado.get("campoControle2")
											if(nome2 != null)devolucao2  += dadosDevolucao.getBigDecimal_Zero(nome2)
										}else {
											devolucao2  += dadosDevolucao.getBigDecimal_Zero(nome2)
										}								}
									if(dado.get("nomeCampo3") != null ) {
										nome3 = BuscarNomeBanco(dado.get("nomeCampo3"))
										if(nome3 == null) {
											nome3 = dado.get("campoControle3")
											if(nome3 != null)devolucao3  += dadosDevolucao.getBigDecimal_Zero(nome3)
										}else {
											devolucao3  += dadosDevolucao.getBigDecimal_Zero(nome3)
										}
									}
									if(dado.get("nomeCampo4") != null ) {
										nome4 = BuscarNomeBanco(dado.get("nomeCampo4"))
										if(nome4 == null) {
											nome4 = dado.get("campoControle4")
											if(nome4 != null)devolucao4  += dadosDevolucao.getBigDecimal_Zero(nome4)
										}else {
											devolucao4  += dadosDevolucao.getBigDecimal_Zero(nome4)
										}
									}
								}
							}
						}
						if(devolucao1 > 0) dado.put("valorCampo1",dado.getBigDecimal_Zero(nome1) - devolucao1)
						if(devolucao2 > 0) dado.put("valorCampo2",dado.getBigDecimal_Zero(nome2) - devolucao2)
						if(devolucao3 > 0) dado.put("valorCampo3",dado.getBigDecimal_Zero(nome3) - devolucao3)
						if(devolucao4 > 0) dado.put("valorCampo4",dado.getBigDecimal_Zero(nome4) - devolucao4)
					}
				}
				
			}
		}
		if(imprimir == 1 )return gerarXLSX(dados);
		return gerarPDF("SRF_Documentos", dados);
	}
	private List<Long> obterItensDoc(Long eaa01id) {
		def sql = " select eaa0103id from eaa0103 where eaa0103doc = :id "
		def p1 =criarParametroSql("id", eaa01id)
		return getAcessoAoBanco().obterListaDeLong(sql, p1)
	}
	private TableMap obterDevolucao(Long idItem){
		def whereItem = " where eaa01033itemdoc = :item "
		
		def sql = " select eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103totfinanc, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json "+
					" from eaa01033 "+
					" inner join eaa0103 on eaa0103id = eaa01033item  "+
					whereItem
		
		def p1 = criarParametroSql("item", idItem)
		return getAcessoAoBanco().buscarUnicoTableMap(sql,p1)
	}
	public List<TableMap> obterDadosRelatorio (List<Long> idEntidade, List<Long> idTipoDocumento, List<Long> criterios, LocalDate[] dataEmissao, LocalDate[] dataEntSai, Integer numeroInicial, 
													Integer numeroFinal, Integer resumoOperacao, Integer nomeEntidade, List<Long>cfop, List<Long>pcd, List<Long> estados, List<Long> municipios)  {
														
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
			entidade = " abe01.abe01na as nomeEntidade";
			groupBy = " group by abb01num, abb01data, eaa01esdata, aaj15codigo, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, abe01na, eaa01json,eaa01id,abd01codigo, abd01descr ";
		} else {
			entidade = " abe01nome as nomeEntidade";
			groupBy = " group by abb01num, abb01data, eaa01esdata, aaj15codigo, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, abe01nome, eaa01json,eaa01id,abd01codigo, abd01descr ";
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
		def whereCriterio = criterios != null && criterios.size() > 0 ? " and abm0102criterio IN (:criterios) " : "";
		def orderBy = data != null && data.size() > 0 ? " order by " + data + " , aah01codigo, abb01num,abd01codigo, abd01descr ": " order by abb01data, aah01codigo, abb01num, abd01codigo, abd01descr ";
		def whereCriterioCfop = cfop != null && cfop.size() > 0 ? " and eaa0103.eaa0103cfop IN (:cfops) " : ""
		def whereCriterioPcd = pcd != null && pcd.size() > 0 ? " and eaa01.eaa01pcd IN (:pcd) " : "";
		def whereEstados = estados != null && estados.size() > 0 ? "and aag02id in (:estados) " : "";
		def whereMunicipios = municipios != null && municipios.size() > 0 ? "and aag0201id in (:municipios) " : "";
		
		def parametroCriterioCfop = cfop != null && cfop.size() > 0 ? Parametro.criar("cfops",cfop) : null;
		def parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? criarParametroSql("idEntidade", idEntidade) : null;
		def parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? criarParametroSql("idTipoDocumento", idTipoDocumento) : null;
		def parametroCriterios = criterios != null && criterios.size() > 0 ? criarParametroSql("criterios", criterios) : null;
		def parametroCriteriosPcd = pcd != null && pcd.size() > 0 ? criarParametroSql("pcd", pcd) : null;
		def parametroEstados = estados != null && estados.size() > 0 ? Parametro.criar("estados",estados) : null;
		def parametroMunicipios = municipios != null && municipios.size() > 0 ? Parametro.criar("municipios",municipios) : null;
		
		def sql = " select abd01codigo, abd01descr,abb01num, abb01data,eaa01id, eaa01esdata, eaa01json, aaj15codigo, aah01codigo, abe01codigo, abe30nome, abe40nome, abe01ni, " +
				"             sum(eaa0103qtUso) as eaa0103qtUso, sum(eaa0103qtComl) as eaa0103qtComl, sum(eaa0103unit) as eaa0103unit,  sum(eaa0103unit) as eaa0103unit,"+
				" 			  sum(eaa0103total) as eaa0103total, sum(eaa0103totDoc) as eaa0103totDoc, sum(eaa0103totFinanc) as eaa0103totFinanc,  " + entidade +
				"        from eaa0103  " +
				"        left join eaa01 on eaa01id = eaa0103doc " +
				"	    left join abd01 on abd01id = eaa01pcd  							"+
				"        left join abb01 on abb01id = eaa01central " +
				"        left join aah01 on aah01id = abb01tipo " +
				"        left join abe01 on abe01id = abb01ent " +
				"        left join abe30 on abe30id = eaa01cp  " +
				"        left join abe40 on abe40id = eaa01tp  " +
				"        left join aaj15 on aaj15id = eaa0103cfop  " +
				"        left join abm01 on abm01id = eaa0103item " +
				"	    left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
				"        left join aag0201 on aag0201id = abe0101municipio "+
				"        left join aag02 on aag02id = aag0201uf " +
				//"        left join abm0102 on abm0102item = abm01id " +
				"        where eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
				getSamWhere().getWherePadrao(" AND ", Eaa01.class) +
				"        and eaa01cancData is null "+
				"        and eaa01nfestat <> 5 "+
				whereNumero +
				whereTipoDocumento +
				whereIdEntidade +
				whereCriterio +
				whereData +
				whereCriterioCfop +
				whereResumoOperacao +
				whereCriterioPcd+
				whereEstados + 
				whereMunicipios +
				groupBy +
				orderBy;

				

		def receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento, parametroCriterios,parametroCriterioCfop,parametroCriteriosPcd,parametroEstados,parametroMunicipios);
		return receberDadosRelatorio;
	}

	public String BuscarNomeBanco(String campo) {
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
	
	public String buscarNomeCampo(String campo) {
		def sql = " select aah02descr from aah02 where aah02nome = :nome "
		return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))
		
	}
	
	@Override
	public String getNomeTarefa() {
		return "SRF - Documentos ";
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=