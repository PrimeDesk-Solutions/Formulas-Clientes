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
import br.com.multitec.utils.criteria.client.ClientField
import sam.core.criteria.ClientCriteriaConvert
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aaj15
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ed.Edb01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

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
		
		LocalDate dataInicial = LocalDate.parse(whereData.getValor1().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		LocalDate dataFinal = LocalDate.parse(whereData.getValor2().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		Aac10 matriz = aac10.aac10matriz != null ? getSession().get(Aac10.class, aac10.aac10matriz.aac10id) : aac10
		String cnpjSucedida = null //"12.123.123/0001-12"
		Integer apuracao = 0
		String codigoTipoApuracao = "003"
		
		TextFile txt = new TextFile("|")
		
		/** R11 - Apuração do IPI - Entradas */
		List<TableMap> cfops = buscarValoresCFOP(whereTipo, whereData, whereNum, whereEnt, 0)
		for(cfop in cfops) {
			txt.print("R11")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida == null ? null : cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(dataInicial.getYear(), 4)
			txt.print(dataInicial.getMonthValue(), 2)
			txt.print(apuracao, 1)
			txt.print(cfop.getString("aaj15codigo"), 4)
			txt.print(cfop.getBigDecimal("ipi_bc") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_bc").multiply(100).longValue(), 14)
			txt.print(cfop.getBigDecimal("ipi_ipi") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_ipi").multiply(100).longValue(), 14)
			txt.print(cfop.getBigDecimal("ipi_isento") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_isento").multiply(100).longValue(), 14)
			txt.print(cfop.getBigDecimal("ipi_outras") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_outras").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		/** R12 - Apuração do IPI - Saídas */
		cfops = buscarValoresCFOP(whereTipo, whereData, whereNum, whereEnt, 1)
		for(cfop in cfops) {
			txt.print("R12")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(dataInicial.getYear(), 4)
			txt.print(dataInicial.getMonthValue(), 2)
			txt.print(apuracao, 1)			
			txt.print(cfop.getString("aaj15codigo"), 4)
			txt.print(cfop.getBigDecimal("ipi_bc") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_bc").multiply(100).longValue(), 14)
			txt.print(cfop.getBigDecimal("ipi_ipi") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_ipi").multiply(100).longValue(), 14)
			txt.print(cfop.getBigDecimal("ipi_isento") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_isento").multiply(100).longValue(), 14)
			txt.print(cfop.getBigDecimal("ipi_outras") == null ? BigDecimal.ZERO : cfop.getBigDecimal("ipi_outras").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		/** R13 - Notas Fiscais de Entrada/Aquisição */
		List<TableMap> eaa0103s = buscarNotasCfop(whereTipo, whereData, whereNum, whereEnt)
		for(eaa0103 in eaa0103s) {			
			Eaa0102 eaa0102 = getSession().get(Eaa0102.class, "eaa0102id, eaa0102ni", Criterions.eq("eaa0102doc", eaa0103.getLong("eaa01id")));
			Abb01 abb01 = getSession().get(Abb01.class, "abb01id, abb01serie, abb01data, abb01num", eaa0103.getLong("abb01id"))
			
			txt.print("R13")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(StringUtils.extractNumbers(eaa0102.eaa0102ni), 14, '0', true)
			txt.print(abb01.abb01num, 9)
			txt.print(abb01.abb01serie == null ? "U" : abb01.abb01serie, 3)
			txt.print(abb01.abb01data.format(ddMMyyyy), 8, '0', true)
			txt.print(abb01.abb01data.format(ddMMyyyy), 8, '0', true)
			
			txt.print(eaa0103.getString("aaj15codigo"), 4)
			txt.print(eaa0103.getBigDecimal("eaa0103totdoc") == null ? BigDecimal.ZERO : eaa0103.getBigDecimal("eaa0103totdoc").multiply(100).longValue(), 14)
			txt.print(eaa0103.getBigDecimal("ipi_ipi") == null ? BigDecimal.ZERO : eaa0103.getBigDecimal("ipi_ipi").multiply(100).longValue(), 14)
			txt.print(eaa0103.getBigDecimal("ipi_ipi") == null ? BigDecimal.ZERO : eaa0103.getBigDecimal("ipi_ipi").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		/** R21 - Livro Apuração IPI */
		Edb01 edb01 = buscarApuracaoPorPeriodo(codigoTipoApuracao, dataInicial.getYear(), dataInicial.getMonthValue(), aac10.aac10municipio.aag0201uf.aag02id)
		if(edb01 != null) {
			if(edb01.edb01json == null) edb01.edb01json = new TableMap();
			
			BigDecimal credImp = buscarSomaDebOuCredIPI(dataInicial, dataFinal, true)
			BigDecimal credImpExterior = buscarSomaDebOuCredIPIExterior(dataInicial, dataFinal, true)
			BigDecimal credImpNacional = credImp.subtract(credImpExterior)
			BigDecimal debImp = buscarSomaDebOuCredIPI(dataInicial, dataFinal, false)
			BigDecimal debImpExterior = buscarSomaDebOuCredIPIExterior(dataInicial, dataFinal, false)
			BigDecimal debImpNacional = debImp.subtract(debImpExterior)
			
			txt.print("R21")
			txt.print(StringUtils.extractNumbers(matriz.aac10ni), 14, '0', true)
			txt.print(cnpjSucedida, 14)
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(dataInicial.getYear(), 4)
			txt.print(dataInicial.getMonthValue(), 2)
			txt.print(apuracao, 1)
			txt.print("2")
			txt.print(credImpNacional.multiply(100).longValue(), 14)
			txt.print(credImpExterior.multiply(100).longValue(), 14)
			txt.print(edb01.edb01json.getBigDecimal("estornodeb") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("estornodeb").multiply(100).longValue(), 14)
			txt.print(0, 14)
			txt.print(0, 14)
			txt.print(edb01.edb01json.getBigDecimal("credoutros") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("credoutros").multiply(100).longValue(), 14)
			txt.print(debImpNacional.multiply(100).longValue(), 14)
			txt.print(edb01.edb01json.getBigDecimal("estornocred") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("estornocred").multiply(100).longValue(), 14)
			txt.print(0, 14)
			txt.print(edb01.edb01json.getBigDecimal("deboutros") == null ? BigDecimal.ZERO : edb01.edb01json.getBigDecimal("deboutros").multiply(100).longValue(), 14)
			txt.newLine()
		}
		
		put("dadosArquivo", txt)
	}

	List<TableMap> buscarValoresCFOP(ClientCriterion whereTipo, ClientCriterion whereData, ClientCriterion whereNum, ClientCriterion whereEnt, Integer mov){
		Criterion critTipo = whereTipo != null ? ClientCriteriaConvert.convertCriterion(whereTipo) : Criterions.isTrue()
		Criterion critData = whereData != null ? ClientCriteriaConvert.convertCriterion(whereData) : Criterions.isTrue()
		Criterion critNum = whereNum != null ? ClientCriteriaConvert.convertCriterion(whereNum) : Criterions.isTrue()
		Criterion critEnt = whereEnt != null ? ClientCriteriaConvert.convertCriterion(whereEnt) : Criterions.isTrue() 
		
		return getSession().createCriteria(Eaa0103.class)
					.addFields("aaj15codigo, SUM(jGet(eaa0103json.ipi_bc)::numeric) AS ipi_bc, SUM(jGet(eaa0103json.ipi_ipi)::numeric) AS ipi_ipi")
					.addFields("SUM(jGet(eaa0103json.ipi_isento)::numeric) AS ipi_isento, SUM(jGet(eaa0103json.ipi_outras)::numeric) AS ipi_outras")
					.addJoin(Joins.join("eaa01","eaa01id = eaa0103doc").left(false))
					.addJoin(Joins.join("aaj15","aaj15id = eaa0103cfop").left(false))
					.addJoin(Joins.join("abb01","abb01id = eaa01central").left(false))
					.addJoin(Joins.join("abe01","abe01id = abb01ent").left(false))
					.addJoin(Joins.join("aah01","aah01id = abb01tipo").left(false))
					.addWhere(critTipo).addWhere(critData).addWhere(critNum).addWhere(critEnt)
					.addWhere(Criterions.eq("eaa01esmov", mov))
					.addWhere(Criterions.eq("eaa01clasdoc", Eaa01.CLASDOC_SRF))
					.setGroupBy("GROUP BY aaj15codigo")
					.getListTableMap()
	}
	
	List<TableMap> buscarNotasCfop(ClientCriterion whereTipo, ClientCriterion whereData, ClientCriterion whereNum, ClientCriterion whereEnt){
		Criterion critTipo = whereTipo != null ? ClientCriteriaConvert.convertCriterion(whereTipo) : Criterions.isTrue()
		Criterion critData = whereData != null ? ClientCriteriaConvert.convertCriterion(whereData) : Criterions.isTrue()
		Criterion critNum = whereNum != null ? ClientCriteriaConvert.convertCriterion(whereNum) : Criterions.isTrue()
		Criterion critEnt = whereEnt != null ? ClientCriteriaConvert.convertCriterion(whereEnt) : Criterions.isTrue()
		
		return getSession().createCriteria(Eaa0103.class)
					.addFields("aaj15codigo, abb01id, abb01num, eaa01id, SUM(eaa0103totdoc) AS eaa0103totdoc, SUM(jGet(eaa0103json.ipi_ipi)::numeric) AS ipi_ipi")
					.addJoin(Joins.join("eaa01","eaa01id = eaa0103doc").left(false))
					.addJoin(Joins.join("aaj15","aaj15id = eaa0103cfop").left(false))
					.addJoin(Joins.join("abb01","abb01id = eaa01central").left(false))
					.addJoin(Joins.join("abe01","abe01id = abb01ent").left(false))
					.addJoin(Joins.join("aah01","aah01id = abb01tipo").left(false))
					.addWhere(critTipo).addWhere(critData).addWhere(critNum).addWhere(critEnt)
					.addWhere(Criterions.eq("eaa01esmov", Eaa01.ESMOV_ENTRADA))
					.addWhere(Criterions.eq("eaa01clasdoc", Eaa01.CLASDOC_SRF))
					.setGroupBy("GROUP BY aaj15codigo, abb01num, eaa01id, abb01id")
					.setOrder("aaj15codigo, abb01num")
					.getListTableMap()
	}
	
	Edb01 buscarApuracaoPorPeriodo(String aaj28codigo, Integer edb01ano, Integer ee01mes, Long uf) {
		return getSession().createCriteria(Edb01.class)
					.addFields("edb01id, edb01json")
					.addJoin(Joins.join("aaj28", "aaj28id = edb01tipo").left(false))
					.addWhere(Criterions.eq("aaj28codigo", aaj28codigo))
					.addWhere(Criterions.eq("edb01ano", edb01ano))
					.addWhere(Criterions.eq("edb01mes", ee01mes))
					.addWhere(Criterions.eq("edb01uf", uf))
					.addWhere(getSamWhere().getCritPadrao(Edb01.class))
					.get(ColumnType.ENTITY)
	}
	
	BigDecimal buscarSomaDebOuCredIPI(LocalDate dataIni, LocalDate dataFin, Boolean isEntrada) {
		String where = isEntrada ? "(aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : "(aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') "
		
		String sql =  " SELECT SUM(jGet(eaa01json.ipi_ipi)::numeric) AS valor FROM Eaa01 " +
				   " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				   " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				   " INNER JOIN Abb01 ON abb01id = eaa01central " +
				   " WHERE abb01data BETWEEN :dataIni AND :dataFin " +
				   " AND eaa01esmov = :oper AND eaa01cancData IS NULL " +
				   " AND eaa01clasdoc = 1 "  +
				   " AND " + where + obterWherePadrao("Eaa01")
			   
		Parametro p1 = criarParametroSql("dataIni", dataIni)
		Parametro p2 = criarParametroSql("dataFin", dataFin)
		Parametro p3 = criarParametroSql("oper", isEntrada ? 0 : 1)
		
		BigDecimal result = getAcessoAoBanco().obterBigDecimal(sql, p1, p2, p3)
		return result ?: BigDecimal.ZERO
	}
	
	BigDecimal buscarSomaDebOuCredIPIExterior(LocalDate dataIni, LocalDate dataFin, Boolean isEntrada) {
		String where = isEntrada ? "(aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : "(aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') "
		
		String sql = " SELECT SUM(jGet(eaa01json.ipi_ipi)::numeric) AS valor FROM Eaa01 " +
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
				  " AND eaa01clasdoc = 1 " +
				  " AND " + where + obterWherePadrao("Eaa01")
			  
		Parametro p1 = criarParametroSql("dataIni", dataIni)
		Parametro p2 = criarParametroSql("dataFin", dataFin)
		Parametro p3 = criarParametroSql("oper", isEntrada ? 0 : 1)
		
		BigDecimal result = getAcessoAoBanco().obterBigDecimal(sql, p1, p2, p3)
		return result ?: BigDecimal.ZERO
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDUifQ==