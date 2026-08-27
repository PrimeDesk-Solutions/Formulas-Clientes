package Profer.relatorios.scf;

import java.time.LocalDate

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SCF_ExtratoContabil extends RelatorioBase {

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("classe","0","tipo","0","ordem","0","impressao","0","documentoInicial","0000000000","documentoFinal","9999999999","periodo", DateUtils.getStartAndEndMonth(LocalDate.now()));
	}

	@Override
	public DadosParaDownload executar() {

		def entidadeIni = getString("entidadeInicial")
		def entidadeFim = getString("entidadeFinal")
		def documentoInicial = getLong("documentoInicial")
		def documentoFinal = getLong("documentoFinal")
		def periodo = getIntervaloDatas("periodo")
		def classe = getInteger("classe")
		def rp = getInteger("tipo")
		def ordem = getInteger("ordem")
		def impressao = getInteger("impressao")
		if(classe == 0) adicionarParametro("titulo", "a receber/recebidos")
		if(classe == 1) adicionarParametro("titulo", "a pagar/pagos")
		adicionarParametro("periodoIni", periodo[0].format("dd/MM/yyyy"))
		adicionarParametro("periodoFim", periodo[1].format("dd/MM/yyyy"))
		adicionarParametro("empresa", obterEmpresaAtiva().aac10rs)
		def entidades = buscarEntidade(entidadeIni, entidadeFim, ordem)
		List<TableMap> dados = new ArrayList()
		for(entidade in entidades) {
			buscarContaContabil(entidade, classe)
			def documentos = buscarDocumentos(entidade, documentoInicial, documentoFinal, rp, classe)
			def indexs = new ArrayList()
			def count = 0
			for(documento in documentos) {
				documento.putAll(entidade)
				def dtLancamento = documento.getDate("abb01operData")
				def dtBaixa = documento.getDate("daa01dtBaixa")
				def dtInicial = periodo[0]
				def dtFinal = periodo[1]
				def realizar = 0.0, realizado = 0.0
				def entra = false
				if( dtLancamento <= dtFinal && (dtBaixa == null || ( dtBaixa >= dtInicial  && dtBaixa <= dtFinal )) ) entra = true
				if( dtLancamento >= dtInicial && dtLancamento <= dtFinal ) entra = true
				if( dtBaixa >= dtInicial  && dtBaixa <= dtFinal ) entra = true

				if( entra ) {
					
					if(dtBaixa == null) {
						
						if( dtLancamento <= dtFinal ) {
							realizar = documento.getBigDecimal("daa01valor")
							documento.put("daa01dtBaixa", null)
							documento.put("dab10historico", null)
							documento.put("jm",BigDecimal.ZERO)
							documento.put("desconto",BigDecimal.ZERO)
						}else {
							realizado = documento.getBigDecimal("daa01valor")
							TableMap json = documento.getTableMap("daa01json")
							if(json != null) {
								def juros = json.getBigDecimal("jurosq") == null ? BigDecimal.ZERO : json.getBigDecimal("jurosq")
								def multa = json.getBigDecimal("multaq") == null ? BigDecimal.ZERO  : json.getBigDecimal("multaq")
								def desconto = json.getBigDecimal("descontoq") == null ? BigDecimal.ZERO  : json.getBigDecimal("descontoq")
								def jm = juros + multa
								documento.put("jm", jm)
								documento.put("desconto",desconto < 0 ? desconto * -1 : desconto)
							}else {
								documento.put("jm", BigDecimal.ZERO )
								documento.put("desconto",BigDecimal.ZERO )
							}
						}
						
						
					}else {
						if(   (dtBaixa >= dtInicial && dtBaixa <= dtFinal ) || (dtBaixa <= dtFinal)   ) {
							realizado = documento.getBigDecimal("daa01valor")
							TableMap json = documento.getTableMap("daa01json")
							if(json != null) {
								def juros = json.getBigDecimal("jurosq") == null ? BigDecimal.ZERO : json.getBigDecimal("jurosq")
								def multa = json.getBigDecimal("multaq") == null ? BigDecimal.ZERO  : json.getBigDecimal("multaq")
								def desconto = json.getBigDecimal("descontoq") == null ? BigDecimal.ZERO  : json.getBigDecimal("descontoq")
								def jm = juros + multa
								documento.put("jm", jm)
								documento.put("desconto",desconto < 0 ? desconto * -1 : desconto)
							}else {
								documento.put("jm", BigDecimal.ZERO )
								documento.put("desconto",BigDecimal.ZERO )
							}
						}
	
						if( dtBaixa >= dtFinal  && dtLancamento <= dtFinal ) {
							realizar = documento.getBigDecimal("daa01valor")
							documento.put("daa01dtBaixa", null)
							documento.put("dab10historico", null)
							documento.put("jm",BigDecimal.ZERO)
							documento.put("desconto",BigDecimal.ZERO)
						}
					}
					
					documento.put("realizar", realizar)
					documento.put("realizado", realizado)
				}else {
					indexs.add(count)
				}
				count++
			}
			Collections.reverse(indexs)
			for(index in indexs) {
				documentos.remove(index)
			}


			dados.addAll(documentos)
		}

		if(impressao == 0) {
			return gerarPDF(dados)
		}else {
			return gerarXLSX(dados)
		}
		
	}

	private List<TableMap> buscarEntidade(String entidadeInicial, String entidadeFinal, Integer ordem){

		def whereInicial = entidadeInicial == null ? "" : " and abe01codigo >= :inicial "
		def whereFinal = entidadeFinal == null ? "" : " and abe01codigo <= :final "
		def order = ordem == 0 ? " abe01codigo " : " abe01nome "

		def sql = " select abe01id, abe01codigo, abe01nome from abe01 " + obterWherePadrao("abe01","where") + whereInicial + whereFinal +" order by "+ order
		def p1 = entidadeInicial == null ? null : criarParametroSql("inicial", entidadeInicial)
		def p2 = entidadeFinal == null ? null : criarParametroSql("final", entidadeFinal)

		return  getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2)
	}

	private buscarContaContabil(TableMap entidade, Integer classe) {
		if(classe == 0) {
			def sql = " select abc10codigo from abc10 " +
					" inner join abe0201 on abc10id = abe0201cta " +
					" inner join abe02 on abe0201cli = abe02id "+
					" inner join abe01 on abe01id = abe02ent "+
					" where abe01id = :abe01id and abe0201seq = 1"
			def p1 = criarParametroSql("abe01id", entidade.getLong("abe01id"))
			def resultado = getAcessoAoBanco().buscarUnicoTableMap(sql,p1)
			if(resultado != null ) {
				entidade.put("abc10codigo", resultado.getString("abc10codigo") )
			}else {
				entidade.put("abc10codigo", null )
			}
		}else {
			def sql = " select abc10codigo from abc10 "+
					" inner join abe0301 on abc10id = abe0301cta " +
					" inner join abe03 on abe0301for = abe03id "+
					" inner join abe01 on abe01id = abe03ent "+
					" where abe01id = :abe01id and abe0301seq = 1"
			def p1 = criarParametroSql("abe01id", entidade.getLong("abe01id"))
			def resultado = getAcessoAoBanco().buscarUnicoTableMap(sql,p1)
			if(resultado != null ) {
				entidade.put("abc10codigo", resultado.getString("abc10codigo") )
			}else {
				entidade.put("abc10codigo", null )
			}
		}
	}

	private List<TableMap> buscarDocumentos(TableMap entidade, Long documentoInicial, Long documentoFinal, Integer rp, Integer classe) {
		def whereNumeroIni = documentoInicial == null ? "" : " and abb01num >= :inicial "
		def whereNumeroFim = documentoFinal == null ? "" : " and abb01num <= :final "
		def wherePrevisao = rp == 0 ? " and daa01previsao = 0 " : rp == 1 ? " and daa01previsao = 1 " : ""
		def whereEntidade = " where abb01ent = :abe01id "
		def whereClasse = classe == 0 ? " and daa01rp = 0 " : " and daa01rp = 1 "

		def sql = "select abb01operData, abb01tipo, abb01num, abb01serie,  "+
				" abb01parcela, abb01quita, abf15nome, aah01codigo, "+
				" abb01data, daa01dtVctoN, daa01dtBaixa,dab10historico,daa01valor, daa01json, abb01ent "+
				" from daa01 "+
				" inner join abb01 on abb01id = daa01central "+
				" left join abf15 on abf15id = daa01port "+
				" left join aah01 on abb01tipo = aah01id "+
				" left join dab10 on dab10central = abb01id " +whereEntidade +whereNumeroIni+whereNumeroFim+wherePrevisao+whereClasse+
				" order by abb01operData, aah01codigo, abb01num, abb01serie, abb01parcela "
		def p1 = documentoInicial == null ? null : criarParametroSql("inicial", documentoInicial)
		def p2 = documentoFinal == null ? null : criarParametroSql("final", documentoFinal)
		def p3 = criarParametroSql("abe01id",entidade.getLong("abe01id"))

		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2,p3)
	}

	@Override
	public String getNomeTarefa() {
		return "SCF - Extrato Contábil - El Tech";
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEV4dHJhdG8gQ29udMOhYmlsIC0gRWwgVGVjaCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==