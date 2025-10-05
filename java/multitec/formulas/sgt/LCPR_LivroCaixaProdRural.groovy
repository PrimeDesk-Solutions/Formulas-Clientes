package multitec.formulas.sgt

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac01
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abc10
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abf01
import sam.model.entities.eb.Ebb05
import sam.server.samdev.formula.FormulaBase

class LCPR_LivroCaixaProdRural extends FormulaBase {
	DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("ddMMyyyy")
	ClientCriterion whereData
	TextFile txt
	Aac10 aac10
	
	LocalDate dataInicial
	LocalDate dataFinal
	
	Integer qtdRegistros = 0
	
	Set<Long> idsImoveis
	Set<Long> abc10Ids
	Map<Integer, Long> mapContas

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_EXPORTAR_DOCUMENTOS_SRF
	}
	
	@Override
	public void executar() {
		aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		
		whereData = get("whereData")

		dataInicial = LocalDate.parse(whereData.getValor1().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		dataFinal = LocalDate.parse(whereData.getValor2().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		
		txt = new TextFile("|")
		
		gerarBloco0()
		
		gerarBlocoQ()
		
		gerarBloco9()
		
		put("dadosArquivo", txt)
		
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * *  BLOCO 0: ABERTURA DO ARQUIVO E IDENTIFICAÇÃO DA PF * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	void gerarBloco0() {
		/**
		 * REGISTRO 0000: Abertura do arquivo digital e identificação da pessoa física
		 */
		txt.print("0000")
		txt.print("LCDPR")
		txt.print("1.2", 4)
		txt.print(StringUtils.extractNumbers(aac10.aac10rCpf), 11)
		txt.print(aac10.aac10rNome)
		txt.print("0", 1)
		txt.print("0", 1)
		txt.print(null)
		txt.print(dataInicial.format(ddMMyyyy))
		txt.print(dataFinal.format(ddMMyyyy))
		txt.newLine()
		qtdRegistros++

		/**
		 * REGISTRO 0010: Parâmetros de Tributação
		 */
		txt.print("0010")
		txt.print(1)
		txt.newLine()
		qtdRegistros++

		/**
		 * REGISTRO 0030: Dados Cadastrais
		 */
		txt.print("0030")
		txt.print(aac10.aac10rEndereco)
		txt.print(aac10.aac10rNumero, 6)
		txt.print(aac10.aac10rComplem)
		txt.print(aac10.aac10rBairro)
		txt.print(aac10.aac10rMunicipio == null ? null : aac10.aac10rMunicipio.aag0201uf.aag02uf)
		txt.print(aac10.aac10rMunicipio == null ? null : aac10.aac10municipio.aag0201ibge)
		txt.print(aac10.aac10rCep)
		txt.print(aac10.aac10rFone == null ? null : aac10.aac10rDddFone == null ? aac10.aac10rFone : aac10.aac10rDddFone + aac10.aac10rFone)
		txt.print(aac10.aac10rEmail)
		txt.newLine()
		qtdRegistros++

		/**
		 * REGISTRO 0040: Cadastro dos Imóveis Rurais
		 */
		def imoveis = buscarEmpresasRuralDoResponsavelLegal(aac10.aac10rCpf)
		idsImoveis = new HashSet<Long>()
		
		for(imovel in imoveis) {
			imovel = getAcessoAoBanco().obterEmpresa(imovel.aac10id)
			
			if(imovel.aac10json == null) imovel.aac10json = new TableMap();
			
			txt.print("0040")
			txt.print(imovel.aac10codigo)
			txt.print("BR")
			txt.print("BRL")
			txt.print(StringUtils.extractNumbers(imovel.aac10json.getString("cafir")))
			txt.print(StringUtils.extractNumbers(imovel.aac10json.getString("cae")))
			
			def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(imovel.aac10id, aac10.aac10municipio.aag0201uf.aag02id)
			txt.print(StringUtils.extractNumbers(ie))
			txt.print(imovel.aac10rs)
			txt.print(imovel.aac10endereco)
			txt.print(imovel.aac10numero)
			txt.print(imovel.aac10complem)
			txt.print(imovel.aac10bairro)
			txt.print(imovel.aac10municipio == null ? null : imovel.aac10municipio.aag0201uf.aag02uf)
			txt.print(imovel.aac10municipio == null ? null : imovel.aac10municipio.aag0201ibge)
			txt.print(imovel.aac10cep)
			txt.print(imovel.aac10json.getString("exploir"))
			txt.print("10000")
			txt.newLine()
			qtdRegistros++
			
			idsImoveis.add(imovel.aac10id)
		}
		

		/**
		 * REGISTRO 0050: Cadastro das Contas Bancárias do Produtor Rural
		 */
		def gcs = buscarGruposCentralizadoresPorEmpresaTabela(idsImoveis, "Abc10")
		
		def abc10s = buscarCtaContabilLCDPR(gcs)
		mapContas = new HashMap<Integer, Long>()
		abc10Ids = new HashSet<>()
		
		def codigo = 1
		for(abc10 in abc10s) {
			txt.print("0050")
			txt.print(codigo)
			txt.print("BR")
			
			def idBanco = abc10.getCamposCustomizados() != null ? abc10.getCamposCustomizados().getLong("idbanco") : null
			def abf01 = idBanco != null ? getSession().get(Abf01.class, idBanco) : null
			
			if(abf01 != null) {
				txt.print(abf01.abf01numero)
				txt.print(abf01.abf01nome)
				txt.print(StringUtils.extractNumbers(abf01.abf01agencia))
				txt.print(StringUtils.extractNumbers(abf01.abf01conta))
			}else {
				txt.print(null)
				txt.print(null)
				txt.print(null)
				txt.print(null)
			}
			
			txt.newLine()
			qtdRegistros++
			
			abc10Ids.add(abc10.abc10id)
			mapContas.put(codigo, abc10.abc10id)
			codigo++
		}
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * *  BLOCO Q: DEMONSTRATIVO DO RESULTADO DA ATIVIDADE RURAL * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	void gerarBlocoQ() {
		
		def gcs = buscarGruposCentralizadoresPorEmpresaTabela(idsImoveis, "EB")
		
		/**
		 * REGISTRO Q100: Demonstrativo do Resultado da Atividade Rural
		 */
		def sdoAnterior = BigDecimal.ZERO
		for(codConta in mapContas.keySet()) {
			def idConta = mapContas.get(codConta)
			def saldo = buscarSaldoAnteriorCtaSemEmpresa(idConta, dataInicial.getMonthValue(), dataInicial.getYear())
			sdoAnterior = sdoAnterior.add(saldo)
			
			def ebb05s = buscarLancamentosContabeisPorConta(dataInicial, dataFinal, idConta)
			for(ebb05 in ebb05s) {
				def aac01 = getSession().get(Aac01.class, ebb05.ebb05gc.aac01id)
				def abb01 = getSession().get(Abb01.class, ebb05.ebb05central.abb01id)
				def abe01 = abb01.abb01ent != null ? getSession().get(Abe01.class, abb01.abb01ent.abe01id) : null
				def ctDeb = getSession().get(Abc10.class, ebb05.ebb05deb.abc10id)
				
				txt.print("Q100")
				txt.print(ebb05.ebb05data.format(ddMMyyyy))
				txt.print(aac01.aac01codigo)
				txt.print(codConta)
				txt.print(abb01.abb01num)
				txt.print("6")
				txt.print(ebb05.ebb05historico)
				txt.print(abe01 == null ? null : StringUtils.extractNumbers(abe01.abe01ni))
				
				boolean isEntrada = ctDeb.abc10id == idConta
				txt.print(isEntrada ? "1" : "2")
			
				if(isEntrada) {
					txt.print(formatarValor(ebb05.ebb05valor))
					txt.print(null)
					saldo = saldo.add(ebb05.ebb05valor)
					
				}else {
					txt.print(null)
					txt.print(formatarValor(ebb05.ebb05valor))
					saldo = saldo.subtract(ebb05.ebb05valor)
				}
				
				txt.print(formatarValor(saldo))
				txt.print(saldo != BigDecimal.ZERO ? "P" : "N")
				txt.newLine()
				qtdRegistros++
			}
		}
		
		
		/**
		 * REGISTRO Q200: Resumo Mensal do Demonstrativo do Resultado da Atividade Rural
		 */
		def listSaldos = buscarSaldosPorAnoMesSemEmpresa(dataInicial.getMonthValue(), dataInicial.getYear(), dataFinal.getMonthValue(), dataFinal.getYear(), abc10Ids)
		if(listSaldos != null && listSaldos.size() > 0) {
			for(saldo in listSaldos) {
				txt.print("Q200")
				txt.print(saldo.getInteger("ebb02mes") + "" + saldo.getInteger("ebb02ano"))
				txt.print(formatarValor(saldo.getBigDecimal("deb")))
				txt.print(formatarValor(saldo.getBigDecimal("cred")))
				
				def saldoFinal = sdoAnterior.add(saldo.getBigDecimal("deb"))
				saldoFinal = saldoFinal.subtract(saldo.getBigDecimal("cred"))
				txt.print(formatarValor(saldoFinal))
			
				txt.print(saldoFinal != BigDecimal.ZERO ? "P" : "N")
				txt.newLine()
				qtdRegistros++
			}
		}
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * BLOCO 9: ENCERRAMENTO DO ARQUIVO DIGITAL  * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	void gerarBloco9() {
		
		/**
		 * REGISTRO 9999: Identificação do Signatário e Encerramento do Arquivo
		 */
		qtdRegistros++
		
		txt.print("9999")
		txt.print(aac10.aac10cNome)
		txt.print(aac10.aac10cCnpj == null ? StringUtils.extractNumbers(aac10.aac10cCpf) : StringUtils.extractNumbers(aac10.aac10cCnpj))
		txt.print(aac10.aac10cCrc)
		txt.print(aac10.aac10cEmail)
		txt.print(aac10.aac10cDddFone == null ? aac10.aac10cFone : aac10.aac10cDddFone + aac10.aac10cFone)
		txt.print(qtdRegistros)
		txt.newLine()
	}
	
	String formatarValor(BigDecimal valor) {
		if(valor == null) return null
		
		valor = valor.multiply(100)
		
		return valor.longValue() + ""
	}
	
	List<Aac10> buscarEmpresasRuralDoResponsavelLegal(String cpfRL) {
		def sql = " SELECT * FROM Aac10 " +
				  " INNER JOIN Aac13 ON aac13empresa = aac10id " +
				  " INNER JOIN Aaj01 ON aaj01id = aac13classTrib " +
				  " WHERE aaj01codigo = :classProdRural " +
				  " AND aac10rCpf = :cpfRL ORDER BY aac10codigo"
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, criarParametroSql("classProdRural", "006"), criarParametroSql("cpfRL", cpfRL))
	}

	List<Long> buscarGruposCentralizadoresPorEmpresaTabela(Set<Long> aac10s, String tabela) {
		def sql = " SELECT aac1001gc FROM Aac1001 WHERE aac1001empresa IN (:aac10s) AND UPPER(aac1001tabela) = UPPER(:tabela) "
		
		def p1 = criarParametroSql("aac10s", aac10s)
		def p2 = criarParametroSql("tabela", tabela)
		
		return getAcessoAoBanco().obterListaDeLong(sql, p1, p2)
	}
	
	List<Abc10> buscarCtaContabilLCDPR(List<Long> gcs){
		def sql = " SELECT * FROM Abc10 WHERE abc10iLivCx = 1 and abc10gc IN (:gcs) ORDER BY abc10codigo"
		return getAcessoAoBanco().buscarListaDeRegistros(sql, criarParametroSql("gcs", gcs))
	}
	
	BigDecimal buscarSaldoAnteriorCtaSemEmpresa(Long abc10id, Integer mes, Integer ano) {
		def sql = " SELECT ebb02saldo FROM Ebb02 " + 
				  " WHERE ebb02cta = :abc10id " +
				  " AND " + Fields.numMeses("ebb02mes", "ebb02ano") + " < :numMeses "

		def p1 = criarParametroSql("numMeses", DateUtils.numMeses(mes, ano))
		def p2 = criarParametroSql("abc10id", abc10id)
		
		return getAcessoAoBanco().obterBigDecimal(sql, p1, p2)
	}
	
	List<Ebb05> buscarLancamentosContabeisPorConta(LocalDate dtInicial, LocalDate dtFinal, Long abc10id) {
		def sql = " SELECT * FROM Ebb05 " +
				  " WHERE ebb05data BETWEEN :dataInicial AND :dataFinal " +
				  " AND (ebb05deb = :abc10id OR ebb05cred = :abc10id) "
		  
		def p1 = criarParametroSql("dataInicial", dataInicial)
		def p2 = criarParametroSql("dataFinal", dataFinal)
		def p3 = criarParametroSql("abc10id", abc10id)
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, p1, p2, p3)
	}
	
	List<TableMap> buscarSaldosPorAnoMesSemEmpresa(Integer mesInicial, Integer anoInicial, Integer mesFinal, Integer anoFinal, Set<Long> abc10s) {
		def sql = " SELECT ebb02mes, ebb02ano, SUM(ebb02deb) as deb, SUM(ebb02cred) as cred  FROM Ebb02 " +
				  " WHERE " + Fields.numMeses("ebb02mes", "ebb02ano") + " BETWEEN :numMesesI AND :numMesesF " +
				  " AND ebb02cta IN (:abc10s) " +
				  " GROUP BY ebb02mes, ebb02ano " +
				  " ORDER BY ebb02ano, ebb02mes"
				  
		def p1 = criarParametroSql("numMesesI", DateUtils.numMeses(mesInicial, anoInicial))
		def p2 = criarParametroSql("numMesesF", DateUtils.numMeses(mesFinal, anoFinal))
		def p3 = criarParametroSql("abc10s", abc10s)
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, p1, p2, p3)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDUifQ==