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
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.server.samdev.formula.FormulaBase

class ISS extends FormulaBase {
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
		
		def isSaida = false
		
		def dataInicial = LocalDate.parse(whereData.getValor1().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		def dataFinal = LocalDate.parse(whereData.getValor2().replace("\"", ""), DateTimeFormatter.ofPattern("yyyyMMdd"))
		
		def aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		
		def txt = new TextFile("|")
		
		txt.print("0")
		txt.print(isSaida ? "P" : "T")
		txt.print(aac10.aac10ti.equals(0) ? "1" : "2")
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
		txt.print(dataInicial.getMonthValue(), 2, '0', true)
		txt.print(dataInicial.getYear(), 4, '0', true)
		txt.print(0, 8)
		txt.print("N")
		txt.print("02")
		txt.print(StringUtils.space(66))
		txt.newLine()
		
		def tipo1 = 0
		def servicos = BigDecimal.ZERO
		def impostos = BigDecimal.ZERO
		
		def eaa01s = buscarDocumentos(whereTipo, whereData, whereNum, whereEnt, 0)
		for(eaa01 in eaa01s) {
			txt.print("1")
			
			if(eaa01.eaa01json == null) eaa01.eaa01json = new TableMap()
			
			Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
			Eaa0101 eaa0101 = buscarEnderecoDocumento(eaa01.eaa01id, 0)
			
			Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id)
			
			if(eaa01.eaa01cancData == null) {
				txt.print(eaa0102.eaa0102ti.equals(0) ? "1" : "2")
				txt.print(StringUtils.extractNumbers(eaa0102.eaa0102ni), 14, '0', true)
				txt.print(eaa0102.eaa0102nome, 100)
				txt.print(eaa0101.eaa0101municipio.aag0201nome, 60)
				txt.print(eaa0101.eaa0101municipio.aag0201uf.aag02uf, 2)
				txt.print(numero(abb01.abb01num), 8)
				txt.print(abb01.abb01data.format(ddMMyyyy))
				txt.print(eaa01.eaa01json.getBigDecimal("total_doc") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("total_doc").multiply(100).longValue(), 14)
				txt.print(0, 14)
				txt.print(eaa01.eaa01json.getBigDecimal("txiss") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("txiss").multiply(100).longValue(), 5)
				txt.print(eaa01.eaa01json.getBigDecimal("iss") == null ? BigDecimal.ZERO : eaa01.eaa01json.getBigDecimal("iss").multiply(100).longValue(), 14)
				//txt.print(rsEa05s.getInteger(i, "ea05tipoiss").equals(0) ? "N" : rsEa05s.getInteger(i, "ea05tipoiss").equals(1) ? "S" : "I") // TODO
				txt.print("1")
				//txt.print(StringUtils.extractNumbers(rsEa05s.getString(i, "tipoServ")), 6, '0', true) // TODO
				txt.print(StringUtils.extractNumbers(eaa01.eaa01json.getString("cfps")), 3, '0', true)
				
				servicos = servicos.add(eaa01.eaa01json.getBigDecimal("serv") ?: BigDecimal.ZERO)
				impostos = impostos.add(eaa01.eaa01json.getBigDecimal("iss") ?: BigDecimal.ZERO)
				
			}else {
				txt.print(0, 15)
				txt.print(StringUtils.space(162))
				txt.print(numero(abb01.abb01num), 8)
				txt.print(abb01.abb01data.format(ddMMyyyy))
				txt.print(0, 47)
				txt.print(StringUtils.space(1))
				txt.print("2")
				txt.print(0, 9)
			}
			
			txt.print(abb01.abb01serie, 2, '0', true)
			txt.print(StringUtils.space(96))
			txt.newLine()
			
			tipo1++
		}
		
		txt.print("9")
		txt.print(tipo1, 4)
		txt.print(servicos.multiply(100).longValue(), 14)
		txt.print(0, 14)
		txt.print(impostos.multiply(100).longValue(), 14)
		txt.print(StringUtils.space(53))
		txt.newLine()
		
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
				.addWhere(Criterions.eq("eaa01iLivroServ", Eaa01.SIM))
                .getList(ColumnType.ENTITY)
    }

    Eaa0101 buscarEnderecoDocumento(Long eaa01id, Integer tipo) {
        def critTipo = tipo == 0 ? Criterions.eq("eaa0101.eaa0101principal", Eaa0101.SIM)
                : Criterions.eq("eaa0101.eaa0101entrega", Eaa0101.SIM)

        return getSession().createCriteria(Eaa0101.class)
                .addJoin(Joins.fetch("eaa0101.eaa0101municipio").left(true).alias("aag0201"))
                .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).alias("aag02"))
                .addJoin(Joins.fetch("eaa0101.eaa0101pais").left(true).alias("aag01"))
                .addWhere(Criterions.eq("eaa0101.eaa0101doc", eaa01id))
                .addWhere(critTipo)
                .get(ColumnType.ENTITY)
    }
	
	Integer numero(Integer num) {
		if(num > 99999999) {
			int tam = num.toString().length();
			num = Integer.parseInt(num.toString().substring(tam-8, tam));
			return num;
		}else {
			return num;
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDUifQ==