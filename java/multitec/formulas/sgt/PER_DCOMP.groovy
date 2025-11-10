package multitec.formulas.sgt

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.core.criteria.ClientCriteriaConvert
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aaj15
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ed.Edb01
import sam.server.samdev.formula.FormulaBase

class PER_DCOMP extends FormulaBase{
	DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("ddMMyyyy")
	
	@Override
	FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_EXPORTAR_DOCUMENTOS_SRF
	}

	@Override
	void executar() {
		ClientCriterion whereTipo = get("whereTipo")
        ClientCriterion whereData = get("whereData")
        ClientCriterion whereEnt = get("whereEnt")
        ClientCriterion whereNum = get("whereNum")
		
		def dataInicial = LocalDate.parse(whereData.getValor1().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		def dataFinal = LocalDate.parse(whereData.getValor2().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		
		def aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		
		def cnpjSucedida = "12.123.123/0001-12"
		
		def apuracao = 0
		
		def matriz = aac10.aac10matriz != null ? getSession().get(Aac10.class, aac10.aac10matriz.aac10id) : aac10
		
		def txt = new TextFile("|")
		
		/** R11 - Apuração do IPI - Entradas */
		def eaa01s = buscarDocumentos(whereTipo, whereData, whereNum, whereEnt, 0)
		for(eaa01 in eaa01s) {
			if(eaa01.eaa01json == null) eaa01.eaa01json = new TableMap();
			
			txt.print("R11")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida == null ? null : cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(dataInicial.getYear(), 4)
			txt.print(dataInicial.getMonthValue(), 2)
			txt.print(apuracao, 1)
			
			def cfop = buscarCfopItemDocumento(eaa01.eaa01id)
			txt.print(cfop, 4)
			txt.print(eaa01.eaa01json.getBigDecimal("bcipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("bcipi").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("ipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("ipi").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("isentasipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("isentasipi").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("outrasipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("outrasipi").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		/** R12 - Apuração do IPI - Saídas */
		eaa01s = buscarDocumentos(whereTipo, whereData, whereNum, whereEnt, 1)
		for(eaa01 in eaa01s) {
			if(eaa01.eaa01json == null) eaa01.eaa01json = new TableMap();
			
			txt.print("R12")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(dataInicial.getYear(), 4)
			txt.print(dataInicial.getMonthValue(), 2)
			txt.print(apuracao, 1)
			
			def cfop = buscarCfopItemDocumento(eaa01.eaa01id)
			txt.print(cfop, 4)
			txt.print(eaa01.eaa01json.getBigDecimal("bcipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("bcipi").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("ipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("ipi").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("isentasipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("isentasipi").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("outrasipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("outrasipi").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		/** R13 - Notas Fiscais de Entrada/Aquisição */
		eaa01s = buscarDocumentos(whereTipo, whereData, whereNum, whereEnt, 3)
		for(eaa01 in eaa01s) {
			if(eaa01.eaa01json == null) eaa01.eaa01json = new TableMap();
			
			Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
			Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id)
			
			txt.print("R13")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(StringUtils.extractNumbers(eaa0102.eaa0102ni), 14, '0', true)
			txt.print(abb01.abb01num, 9)
			txt.print(abb01.abb01serie == null ? "U" : abb01.abb01serie, 3)
			txt.print(abb01.abb01data.format(ddMMyyyy), 8, '0', true)
			txt.print(abb01.abb01data.format(ddMMyyyy), 8, '0', true)
			
			def cfop = buscarCfopItemDocumento(eaa01.eaa01id)
			txt.print(cfop, 4)
			txt.print(eaa01.eaa01json.getBigDecimal("vlrcontabil") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("vlrcontabil").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("ipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("ipi").multiply(100).longValue(), 14)
			txt.print(eaa01.eaa01json.getBigDecimal("ipi") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("ipi").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		/** R21 - Livro Apuração IPI */
		def gcEE = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EE")
		def edb01 = buscarApuracaoPorPeriodo(1, dataInicial.getYear(), dataInicial.getMonthValue(), aac10.aac10municipio.aag0201uf.aag02id) // TODO TIPO
		
		if(edb01 != null) {
			if(edb01.edb01json == null) edb01.edb01json = new TableMap();
			
			txt.print("R21")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(dataInicial.getYear(), 4)
			txt.print(dataInicial.getMonthValue(), 2)
			txt.print(apuracao, 1)
			txt.print("2")
			
			def credImp = buscarSomaDebOuCredIPI(dataInicial, dataFinal, true)
			def credImpExterior = buscarSomaDebOuCredIPIExterior(dataInicial, dataFinal, true)
			def credImpNacional = credImp.subtract(credImpExterior)
			
			txt.print(credImpNacional.multiply(100).longValue(), 14)
			txt.print(credImpExterior.multiply(100).longValue(), 14)
			txt.print(edb01.edb01json.getBigDecimal("estdeb") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("estdeb").multiply(100).longValue(), 14)
			txt.print(0, 14)
			txt.print(0, 14)
			txt.print(edb01.edb01json.getBigDecimal("outroscred") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("outroscred").multiply(100).longValue(), 14)
			
			def debImp = buscarSomaDebOuCredIPI(dataInicial, dataFinal, false)
			def debImpExterior = buscarSomaDebOuCredIPIExterior(dataInicial, dataFinal, false)
			def debImpNacional = debImp.subtract(debImpExterior)
			
			txt.print(debImpNacional.multiply(100).longValue(), 14)
			txt.print(edb01.edb01json.getBigDecimal("estcred") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("estcred").multiply(100).longValue(), 14)
			txt.print(0, 14)
			txt.print(edb01.edb01json.getBigDecimal("outrosdeb") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("outrosdeb").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		put("dadosArquivo", txt)
	}

    List<Eaa01> buscarDocumentos(ClientCriterion whereTipo, ClientCriterion whereData, ClientCriterion whereNum, ClientCriterion whereEnt, Integer mov) {
        Criterion critTipo = whereTipo != null ? ClientCriteriaConvert.convertCriterion(whereTipo) : Criterions.isTrue()
        Criterion critData = whereData != null ? ClientCriteriaConvert.convertCriterion(whereData) : Criterions.isTrue()
        Criterion critNum = whereNum != null ? ClientCriteriaConvert.convertCriterion(whereNum) : Criterions.isTrue()
        Criterion critEnt = whereEnt != null ? ClientCriteriaConvert.convertCriterion(whereEnt) : Criterions.isTrue()

        return getSession().createCriteria(Eaa01.class).alias("eaa01")
                .addJoin(Joins.fetch("eaa01.eaa01central").alias("abb01"))
                .addJoin(Joins.fetch("abb01.abb01tipo").alias("aah01"))
                .addJoin(Joins.fetch("abb01.abb01ent").alias("abe01"))
                .addJoin(Joins.fetch("eaa01.eaa01cancMotivo").left(true).alias("aae11"))
                .addJoin(Joins.fetch("abb01.abb01operCod").left(true).alias("abb10"))
                .addWhere(critTipo).addWhere(critData).addWhere(critNum).addWhere(critEnt)
				.addWhere(mov != 3 ? Criterions.eq("eaa01esMov", mov) : Criterions.isTrue())
                .getList(ColumnType.ENTITY)
    }
	
	String buscarCfopItemDocumento(Long eaa01id) {
		return getSession().createCriteria(Aaj15.class)
						   .addFields("aaj15codigo")
						   .addJoin(Joins.join("Eaa0103", "eaa0103cfop = aaj15id"))
						   .addWhere(Criterions.eq("eaa0103doc", eaa01id))
						   .setMaxResults(1)
						   .get(ColumnType.STRING)
	}
	
	Long buscarGrupoCentralizadorPorEmpresaTabela(Long aac10id, String tabela) {
		def sql = " SELECT aac1001gc FROM Aac1001 WHERE aac1001empresa = :aac10id AND UPPER(aac1001tabela) = UPPER(:tabela) "
		
		def p1 = criarParametroSql("aac10id", aac10id)
		def p2 = criarParametroSql("tabela", tabela)
		
		return getAcessoAoBanco().obterLong(sql, p1, p2)
	}
	
	Edb01 buscarApuracaoPorPeriodo(Integer edb01tipo, Integer edb01ano, Integer ee01mes, Long uf) {
		def sql = " SELECT * FROM Edb01 " +
				  " WHERE edb01tipo = :edb01tipo " +
				  " AND edb01ano = :edb01ano " +
				  " AND edb01mes = :edb01mes " +
				  " AND edb01uf = :edb01uf " + 
				  	obterWherePadrao("Edb01")
		
		def p1 = criarParametroSql("edb01tipo", edb01tipo)
		def p2 = criarParametroSql("edb01ano", edb01ano)
		def p3 = criarParametroSql("edb01mes", ee01mes)
		def p4 = criarParametroSql("edb01uf", uf)
		
		return getAcessoAoBanco().buscarRegistroUnico(sql, p1, p2, p3, p4)
	}
	
	BigDecimal buscarSomaDebOuCredIPI(LocalDate dataIni, LocalDate dataFin, Boolean isEntrada) {
		def where = isEntrada ? "(aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : "(aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') "
		
		def sql =  " SELECT SUM(jGet(eaa01json.ipi)::numeric) AS valor FROM Eaa01 " +
				   " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				   " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				   " INNER JOIN Abb01 ON abb01id = eaa01central " +
				   " WHERE abb01data BETWEEN :dataIni AND :dataFin " +
				   " AND eaa01esmov = :oper AND eaa01cancData IS NULL " +
				   " AND " + where + obterWherePadrao("Eaa01")
			   
		def p1 = criarParametroSql("dataIni", dataIni)
		def p2 = criarParametroSql("dataFin", dataFin)
		def p3 = criarParametroSql("oper", isEntrada ? 0 : 1)
		
		def result = getAcessoAoBanco().obterBigDecimal(sql, p1, p2, p3)
		return result ?: BigDecimal.ZERO
	}
	
	BigDecimal buscarSomaDebOuCredIPIExterior(LocalDate dataIni, LocalDate dataFin, Boolean isEntrada) {
		def where = isEntrada ? "(aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : "(aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') "
		
		def sql = " SELECT SUM(jGet(eaa01json.ipi)::numeric) AS valor FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf"
				  " WHERE abb01data BETWEEN :dataIni AND :dataFin " +
				  " AND eaa01esMov = :oper " +
				  " AND eaa01cancData IS NULL " +
				  " AND eaa0101principal = 1 " +
				  " AND UPPER(aag02uf) = 'EX' " + 
				  " AND " + where + obterWherePadrao("Eaa01")
			  
		def p1 = criarParametroSql("dataIni", dataIni)
		def p2 = criarParametroSql("dataFin", dataFin)
		def p3 = criarParametroSql("oper", isEntrada ? 0 : 1)
		
		def result = getAcessoAoBanco().obterBigDecimal(sql, p1, p2, p3)
		return result ?: BigDecimal.ZERO
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDUifQ==