package MM.relatorios.scf;

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource

class SCF_ExtratoFinanceiro extends RelatorioBase {

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("vencimento","0","classe","0","ordem","0","imprimir", "1");
	}

	@Override
	public DadosParaDownload executar() {

		def entidadeInicial = getString("entidadeInicial")
		def entidadeFinal = getString("entidadeFinal")
		def portadorInicial = getString("portadorInicial")
		def portadorFinal = getString("portadorFinal")
		def operacaoInicial = getString("operacaoInicial")
		def operacaoFinal = getString("operacaoFinal")
		def receber = getIntervaloDatas("receber")
		def recebidas = getIntervaloDatas("recebidas")
		def pagar = getIntervaloDatas("pagar")
		def pagas = getIntervaloDatas("pagas")
		def vencimento = getInteger("vencimento")
		def classe = getInteger("classe")
		def ordem = getInteger("ordem")
		def forma = get("forma")
		def imprimir = getInteger("imprimir")

		adicionarParametro("Empresa", obterEmpresaAtiva().aac10rs)
		adicionarParametro("tituloVcto", vencimento == 0 ? "VCTON" : "VCTOR")

		List<TableMap> dados = new ArrayList()
		List<TableMap> formasPagamento = new ArrayList()
		
		List<Long> abe01ids = new ArrayList()
			def entidades = buscarEntidade(entidadeInicial,entidadeFinal)

			for(entidade in entidades) {
				List<Long> abb01ids = new ArrayList()
				entidade.put("key", entidade.getLong("abe01id"))
				def representante = new TableMap()
				if(entidade.getLong("abe02rep0") != null ) {
					def sql = "select abe01na, abe01codigo as abe01codigo_rep from abe01 where abe01id = " + entidade.getLong("abe02rep0")
					representante = getAcessoAoBanco().buscarUnicoTableMap(sql)
				}else if(entidade.getLong("abe02rep1") != null  ) {
					def sql = "select abe01na, abe01codigo as abe01codigo_rep from abe01 where abe01id = " + entidade.getLong("abe02rep1")
					representante = getAcessoAoBanco().buscarUnicoTableMap(sql)
				}else if(entidade.getLong("abe02rep2") != null){
					def sql = "select abe01na, abe01codigo as abe01codigo_rep from abe01 where abe01id = " + entidade.getLong("abe02rep2")
					representante = getAcessoAoBanco().buscarUnicoTableMap(sql)
				}else if(entidade.getLong("abe02rep3") != null) {
					def sql = "select abe01na, abe01codigo as abe01codigo_rep from abe01 where abe01id = " + entidade.getLong("abe02rep3")
					representante = getAcessoAoBanco().buscarUnicoTableMap(sql)
				}else if(entidade.getLong("abe02rep4") != null){
					def sql = "select abe01na, abe01codigo as abe01codigo_rep from abe01 where abe01id = " + entidade.getLong("abe02rep4")
					representante = getAcessoAoBanco().buscarUnicoTableMap(sql)
				}

				entidade.put("representante", representante.getString("abe01na"))
				entidade.put("abe01codigo_rep", representante.getString("abe01codigo_rep"))
				
				TableMap abe01json = entidade.getTableMap("abe01json")
				if(abe01json != null) {
					
					def limiteCredito = abe01json.getBigDecimal("lim_cred")
					def fixacao = abe01json.getDate("dt_fix_cred")
					def dataVencimento = abe01json.getDate("dt_vcto_cred")
					def dataCompra = abe01json.getDate("dt_pri_cp")
					def dataVenda = abe01json.getDate("dt_pri_vd")
					def observacao = abe01json.getString("obs_cred")
					entidade.put("limite_credito",limiteCredito)
					entidade.put("fixacao", fixacao != null ?fixacao.format("dd/MM/yyyy") : fixacao)
					entidade.put("dataVencimento", dataVencimento)
					entidade.put("dataVenda", dataVenda)
					entidade.put("dataCompra", dataCompra)
					entidade.put("observacao", observacao)
				}

				def documentos = buscarDocumentos(entidade, portadorInicial, portadorFinal, operacaoInicial, operacaoFinal, receber, recebidas, pagar, pagas, vencimento, classe, ordem,abb01ids)
				if(documentos.size() > 0) {

					def totalReceber = 0.0, JMReceber = 0.0, descontoReceber = 0.0
					def totalRecebido = 0.0, JMRecebido = 0.0, descontoRecebido = 0.0
					def totalPagar = 0.0, JMPagar = 0.0, descontoPagar = 0.0
					def totalPago = 0.0, JMPago = 0.0, descontoPago = 0.0

					for(documento in documentos) {
						TableMap daa01json = documento.getTableMap("daa01json")
						LocalDate dataAtual =  documento.getDate("daa01dtPgto") == null ? LocalDate.now() : documento.getDate("daa01dtPgto")
						def dias = ChronoUnit.DAYS.between(dataAtual, documento.getDate("vencimento"))
						def desconto = 0.0
						def JM = 0.0
						if(daa01json != null) {
							def juros = documento.getDate("daa01dtPgto") == null ? daa01json.getBigDecimal("juros") == null ? 0.0 : daa01json.getBigDecimal("juros"): daa01json.getBigDecimal("jurosq") == null ? 0.0 : daa01json.getBigDecimal("jurosq")
							def multa = documento.getDate("daa01dtPgto") == null ? daa01json.getBigDecimal("multa") == null ? 0.0 : daa01json.getBigDecimal("multa"): daa01json.getBigDecimal("multaq") == null ? 0.0 : daa01json.getBigDecimal("multaq")
							if(documento.getDate("daa01dtPgto") == null) {
								desconto = daa01json.getDate("data_lim") == null ? daa01json.getBigDecimal("desconto") == null ? 0.0 : daa01json.getBigDecimal("desconto") : 0.0
								desconto = desconto < 0 ? desconto * -1 : desconto
							}else {
								desconto = daa01json.getBigDecimal("descontoq") == null ? 0.0 : daa01json.getBigDecimal("descontoq")
								desconto = desconto < 0 ? desconto * -1 : desconto
							}

							JM =   dias < 0 ? (juros * (dias *-1)) + multa  : 0.0
							JM = JM < 0 ? JM *-1 : JM
							if(documento.getDate("daa01dtPgto") != null ) {
								JM = juros + multa
								JM = JM < 0 ? JM *-1 : JM
							}

							if(documento.getInteger("daa01rp") == 0) {
								if(documento.getDate("daa01dtPgto") == null ) {
									descontoReceber += desconto
									JMReceber += JM
								}else {
									descontoRecebido += desconto
									JMRecebido += JM
								}
							}else {
								if(documento.getDate("daa01dtPgto") == null ) {
									descontoPagar+= desconto
									JMPagar += JM
								}else {
									descontoPago += desconto
									JMPago += JM
								}
							}
						}

						if(documento.getInteger("daa01rp") == 0) {
							if(documento.getDate("daa01dtPgto") == null ) {
								totalReceber += documento.getBigDecimal("daa01valor")
							}else {
								totalRecebido += documento.getBigDecimal("daa01valor")
							}
						}else {
							if(documento.getDate("daa01dtPgto") == null ) {
								totalPagar+= documento.getBigDecimal("daa01valor")
							}else {
								totalPago += documento.getBigDecimal("daa01valor")
							}
						}
						comporEnderecos(entidade)
						comporContato(entidade)
						documento.putAll(entidade)
						documento.put("dias", dias)
						documento.put("JM",JM)
						documento.put("desconto", desconto)
						abb01ids.add(documento.getLong("abb01id"))
					}					
					
					TableMap aux = new TableMap()
					aux.putAll(entidade)
					if(receber != null) {
						aux.put("total_receber",totalReceber)
						aux.put("jm_receber", JMReceber)
						aux.put("desconto_receber", descontoReceber)
					}
					if(recebidas != null) {
						aux.put("total_recebido",totalRecebido)
						aux.put("jm_recebido", JMRecebido)
						aux.put("desconto_recebido", descontoRecebido)
					}
					if(pagar != null) {
						aux.put("total_pagar",totalPagar)
						aux.put("jm_pagar", JMPagar)
						aux.put("desconto_pagar", descontoPagar)
					}
					if(pagas != null) {
						aux.put("total_pago",totalPago)
						aux.put("jm_pago", JMPago)
						aux.put("desconto_pago", descontoPago)
					}
					if(receber == null && recebidas == null && pagar == null && pagas == null ) {
						aux.put("total_receber",totalReceber)
						aux.put("jm_receber", JMReceber)
						aux.put("desconto_receber", descontoReceber)
						aux.put("total_recebido",totalRecebido)
						aux.put("jm_recebido", JMRecebido)
						aux.put("desconto_recebido", descontoRecebido)
						aux.put("total_pagar",totalPagar)
						aux.put("jm_pagar", JMPagar)
						aux.put("desconto_pagar", descontoPagar)
						aux.put("total_pago",totalPago)
						aux.put("jm_pago", JMPago)
						aux.put("desconto_pago", descontoPago)
					}
					documentos.add(aux)
					dados.addAll(documentos)

					if(forma && abb01ids.size() > 0) {
						List<TableMap> pagamentos = new ArrayList()
						comporFormaPagamento(pagamentos,abb01ids)
						def total_conciliado = BigDecimal.ZERO
						def total_nonao_cciliado = BigDecimal.ZERO
						for(pagamento in pagamentos) {
							pagamento.put("key", entidade.getLong("abe01id"))
							formasPagamento.add(pagamento)
						}

						somarFormas(formasPagamento, abb01ids,entidade.getLong("abe01id"))
					}
				}
			}
		
		if(forma && classe != 1) {
			def dsPrincipal = new TableMapDataSource(dados)
			dsPrincipal.addSubDataSource("DsSub1", formasPagamento, "key", "key")
			adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCF_ExtratoFinanceiro_S1"))
			dsPrincipal.addSubDataSource("DsSub2", dados, "key", "key")
			adicionarParametro("StreamSub2", carregarArquivoRelatorio("SCF_ExtratoFinanceiro_S2"))
			if(imprimir == 0) {
				return gerarXLSX("SCF_ExtratoFinanceiro", dsPrincipal);
			}else {
				return gerarPDF("SCF_ExtratoFinanceiro", dsPrincipal);
			}
		}else {
			def dsPrincipal = new TableMapDataSource(dados)
			dsPrincipal.addSubDataSource("DsSub2", dados, "key", "key")
			adicionarParametro("StreamSub2", carregarArquivoRelatorio("SCF_ExtratoFinanceiro_S2"))
			if(imprimir == 0) {
				return gerarXLSX("SCF_ExtratoFinanceiro", dsPrincipal);
			}else {
				return gerarPDF("SCF_ExtratoFinanceiro", dsPrincipal);
			}
		}
	}

	private List<TableMap> buscarEntidade(String codInicial, String codFinal) {

		def whereInicial = codInicial == null ? "" : " and abe01codigo >= :codInicial"
		def whereFinal = codFinal == null ? "" : " and abe01codigo <= :codFinal "

		def sql = " select abe01id, abe01nome, abe01codigo, abe01na, abe01json, abe01ni,abe01ie,abe01im,  abe02rep0, abe02rep1, abe02rep2, abe02rep3, abe02rep4, abe01json "+
				" from abe01 left join abe02 on abe02ent = abe01id "+

				obterWherePadrao("abe01","where")+whereInicial+whereFinal+" order by abe01codigo"

		def p1 = codInicial == null ? null : criarParametroSql("codInicial", codInicial)
		def p2 = codFinal == null ? null : criarParametroSql("codFinal", codFinal)

		def resultados = getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2)

		return resultados
	}
	private comporContato(TableMap entidade) {
		def sql = " select  abe0104email, abe0104ddd, abe0104fone  from abe0104 where abe0104ent = " + entidade.getLong("abe01id")
		def resultado = getAcessoAoBanco().buscarUnicoTableMap(sql)
		if(resultado != null) {
			entidade.putAll(resultado)
		}
	}
	private comporEnderecos(TableMap entidade) {
		//seleciona o endereco principal
		def sql = " select abe01id,abe0101ddd1, abe0101fone1, abe0101eMail, aag03nome, abe0101endereco as abe0101endereco_principal, abe0101numero as abe0101numero_principal, abe0101complem as abe0101complem_principal, "+
				" abe0101cep as abe0101cep_principal, abe0101bairro as abe0101bairro_principal, "+
				" aag0201nome as aag0201nome_principal, aag02uf as aag02uf_principal "+
				" from abe01 "+
				" left join abe0101 on abe01id = abe0101ent and abe0101principal = 1 "+
				" left join aag0201 on abe0101municipio = aag0201id "+
				" left join aag02 on aag0201uf = aag02id "+
				" left join aag03 on aag03id = abe0101regiao"+
				" where abe01id = :abe01id "

		def p1 = criarParametroSql("abe01id", entidade.getLong("abe01id"))
		def resultEndereco = getAcessoAoBanco().buscarUnicoTableMap(sql, p1)
		entidade.putAll(resultEndereco)

		//seleciona o endereco de cobranca
		sql = " select abe01id, abe0101endereco as abe0101endereco_cobranca, abe0101numero as abe0101numero_cobranca, abe0101complem as abe0101complem_cobranca,  "+
				" abe0101cep as abe0101cep_cobranca, abe0101bairro as abe0101bairro_cobranca, "+
				" aag0201nome as aag0201nome_cobranca, aag02uf as aag02uf_cobranca "+
				" from abe01 "+
				" left join abe0101  on abe01id = abe0101ent and abe0101cobranca = 1 "+
				" left join aag0201  on abe0101municipio = aag0201id "+
				" left join aag02 on aag0201uf = aag02id "+
				" where abe01id = :abe01id "

		p1 = criarParametroSql("abe01id", entidade.getLong("abe01id"))
		resultEndereco = getAcessoAoBanco().buscarUnicoTableMap(sql, p1)
		entidade.putAll(resultEndereco)

		//seleciona o endereco de entrega
		sql = " select abe01id, abe0101endereco as abe0101endereco_entrega, abe0101numero as abe0101numero_entrega, abe0101complem as abe0101complem_entrega, "+
				" abe0101cep as abe0101cep_entrega, abe0101bairro as abe0101bairro_entrega, "+
				" aag0201nome as aag0201nome_entrega, aag02uf as aag02uf_entrega "+
				" from abe01 "+
				" left join abe0101  on abe01id = abe0101ent and abe0101entrega = 1 "+
				" left join aag0201  on abe0101municipio = aag0201id "+
				" left join aag02  on aag0201uf = aag02id "+
				" where abe01id = :abe01id "

		p1 = criarParametroSql("abe01id", entidade.getLong("abe01id"))
		resultEndereco = getAcessoAoBanco().buscarUnicoTableMap(sql, p1)
		entidade.putAll(resultEndereco)
	}
	private List<TableMap> buscarDocumentos(TableMap entidade, String portadorInicial, String portadorFinal, String operacaoInicial, String operacaoFinal, LocalDate[] receber, LocalDate[] recebidas, LocalDate[] pagar, LocalDate[] pagas, Integer vencimento, Integer classe , Integer ordem, List<Long> abb01ids) {

		def wherePortadorIni = portadorInicial == null ? "" : "  and abf15codigo >= :portInicial "
		def wherePortadorFim = portadorFinal == null ? "" : " and abf15codigo <= :portFinal "

		def whereOperacaoIni = operacaoInicial == null ? "" : " and abf16codigo >= :opeInicial "
		def whereOperacaoFim = operacaoFinal == null ? "" : "  and abf16codigo <= :opeFinal "

		def whereClasse = classe == 0 ? " and daa01previsao = 0 " : classe == 1 ? " and daa01previsao = 1 " : ""
		def whereEntidade = " and abb01ent = " + entidade.getLong("abe01id")
		def columnVencimento = vencimento == 0 ? " daa01dtVctoN "  : " daa01dtVctoR "
		def orderBy = ordem == 0 ? columnVencimento +", " : ""

		def baseSql = " select daa01id, daa01rp, "+columnVencimento+" as vencimento, daa01dtPgto, daa01json,aah01codigo, abb01num, abb01id, abb01serie, abb01parcela, abb01quita, "+
				" abb01data, abf15codigo, abf15nome, abf16codigo, abf16nome, daa01valor, abf20codigo, abf20descr from daa01 "+
				" inner join abb01 on abb01id = daa01central "+
				" inner join aah01 on aah01id = abb01tipo "+
				" left join abf15 on abf15id = daa01port "+
				" left join abf16 on abf16id = daa01oper "+
				" left join dab10 on dab10central = abb01id "+
				" left join abf20 on abf20id = dab10plf "+
				obterWherePadrao("daa01", "where")+ wherePortadorIni + wherePortadorFim +
				whereOperacaoIni + whereOperacaoFim + whereClasse + whereEntidade

		def p1 = portadorInicial == null ? null : criarParametroSql("portInicial",portadorInicial )
		def p2 = portadorFinal == null ? null : criarParametroSql("portFinal", portadorFinal)
		def p3 = operacaoInicial == null ? null : criarParametroSql("opeInicial", operacaoInicial)
		def p4 = operacaoFinal == null ? null : criarParametroSql("opeFinal", operacaoFinal)

		List<TableMap> resultados = new ArrayList()
		if(receber != null) {
			// seleciona os documentos a receber
			def sql = baseSql +" and daa01rp = 0 and daa01dtPgto is null "+
					" and abb01data between :receberIni and :receberFim "+
					" order by " + orderBy + " aah01codigo, abb01num, abb01serie, abb01parcela "
			def p5 = criarParametroSql("receberIni", receber[0])
			def p6 = criarParametroSql("receberFim", receber[1])
			def resultadosReceber = getAcessoAoBanco().buscarListaDeTableMap(sql, p1,p2,p3,p4,p5,p6)

			if(resultadosReceber.size() > 0 ) {
				resultados.addAll(resultadosReceber)
			}
		}

		if(recebidas != null) {
			// seleciona os documentos recebidos
			def sql = baseSql +" and daa01rp = 0 and daa01dtPgto is not null "+
					" and daa01dtPgto between :recebidasIni and :recebidasFim "+
					" order by " + orderBy + " aah01codigo, abb01num, abb01serie, abb01parcela "
			def p5 = criarParametroSql("recebidasIni", recebidas[0])
			def p6 = criarParametroSql("recebidasFim", recebidas[1])
			def resultadosRecebidas = getAcessoAoBanco().buscarListaDeTableMap(sql, p1,p2,p3,p4,p5,p6)

			if(resultadosRecebidas.size() > 0 ) {
				resultados.addAll(resultadosRecebidas)
			}
		}

		if(pagar != null) {
			// seleciona os documentos a pagar
			def sql = baseSql +" and daa01rp = 1 and daa01dtPgto is null "+
					" and abb01data between :pagarIni and :pagarFim "+
					" order by " + orderBy + " aah01codigo, abb01num, abb01serie, abb01parcela "
			def p5 = criarParametroSql("pagarIni", pagar[0])
			def p6 = criarParametroSql("pagarFim", pagar[1])
			def resultadosPagar = getAcessoAoBanco().buscarListaDeTableMap(sql, p1,p2,p3,p4,p5,p6)

			if(resultadosPagar.size() > 0 ) {
				resultados.addAll(resultadosPagar)
			}
		}

		if(pagas != null) {
			// seleciona os documentos pagas
			def sql = baseSql +" and daa01rp = 1 and daa01dtPgto is not null "+
					" and daa01dtPgto between :pagasIni and :pagasFim "+
					" order by " + orderBy + " aah01codigo, abb01num, abb01serie, abb01parcela "
			def p5 = criarParametroSql("pagasIni", pagas[0])
			def p6 = criarParametroSql("pagasFim", pagas[1])
			def resultadosPagas = getAcessoAoBanco().buscarListaDeTableMap(sql, p1,p2,p3,p4,p5,p6)

			if(resultadosPagas.size() > 0 ) {
				resultados.addAll(resultadosPagas)
			}
		}
		
		if(receber == null && recebidas == null && pagar == null && pagas == null ) {
			def sql = baseSql 
			def resultadosReceber = getAcessoAoBanco().buscarListaDeTableMap(sql, p1,p2,p3,p4)
			if(resultadosReceber.size() > 0 ) {
				resultados.addAll(resultadosReceber)
			}
		}
		
		return resultados
	}
	private comporFormaPagamento(List<TableMap> pagamentos, List<Long> abb01ids ) {

		def sql = " select abf40codigo, abf40descr, daa01dtBaixa, sum(dab1002valor) as dab1002valor from dab10 "+
				" inner join abb01 on abb01id =  dab10central "+
				" inner join dab1002 on dab1002lct = dab10id "+
				" inner join  abf40 on dab1002fp = abf40id "+
				" inner join daa01 on daa01central = abb01id "+
				" where abb01id in (:abb01id) "+
				" group by daa01dtBaixa, abf40codigo, abf40descr, abf40descr "
		" order by daa01dtBaixa, abf40codigo "

		def p1 = criarParametroSql("abb01id", abb01ids)
		def resultados = getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
		pagamentos.addAll(resultados)

	}
	private somarFormas(List<TableMap>formasPagamento,List<Long> abb01ids, Long key) {
		
		TableMap aux = new TableMap()
		def sql = " select  sum(dab1002valor) as total_conciliado from dab10 "+
				" inner join abb01 on abb01id =  dab10central "+
				" inner join dab1002 on dab1002lct = dab10id "+
				" where abb01id in (:abb01id) "+
				" and dab1002dtconc is not null"
		def p1 = criarParametroSql("abb01id", abb01ids)
		def resultado = getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
		aux.putAll(resultado[0])
		sql = " select  sum(dab1002valor) as total_nao_conciliado from dab10 "+
				" inner join abb01 on abb01id =  dab10central "+
				" inner join dab1002 on dab1002lct = dab10id "+
				" where abb01id in (:abb01id) "+
				" and dab1002dtconc is null"
		p1 = criarParametroSql("abb01id", abb01ids)
		resultado = getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
		aux.putAll(resultado[0])
		aux.put("key", key)
		formasPagamento.add(aux)
		
	}
	
	
	@Override
	public String getNomeTarefa() {
		return "SCF - Extrato Financeiro - El Tech";
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEV4dHJhdG8gRmluYW5jZWlybyAtIEVsIFRlY2giLCJ0aXBvIjoicmVsYXRvcmlvIn0=