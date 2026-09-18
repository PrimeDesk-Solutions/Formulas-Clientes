package multitec.formulas.sgt

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aah20
import sam.model.entities.aa.Aaj03
import sam.model.entities.aa.Aaj04
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm10
import sam.model.entities.ab.Abm12
import sam.model.entities.bc.Bcb11
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ed.Edd01
import sam.server.samdev.formula.FormulaBase

class Sintegra extends FormulaBase {
	DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("ddMMyyyy")
	DateTimeFormatter MMyyyy = DateTimeFormatter.ofPattern("MMyyyy")
	
	List<Aag02> aag02s
	LocalDate dataInicial
	LocalDate dataFinal
	Integer finalidade, natOper
	
	Aac10 aac10
	
	TextFile txt;
	
	Set<Abm01> setAbm01s;
	List<Long> estados;
	
	Integer totalTipo50 = 0;
	Integer totalTipo51 = 0;
	Integer totalTipo53 = 0;
	Integer totalTipo54 = 0;
	Integer totalTipo55 = 0;
	Integer totalTipo61 = 0;
	Integer totalTipo70 = 0;
	Integer totalTipo71 = 0;
	Integer totalTipo74 = 0;
	Integer totalTipo75 = 0;
	Integer totalTipo85 = 0;
	Integer totalTipo88 = 0;
	Integer operacao;
	
	@Override
	FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_SINTEGRA
	}

	@Override
	void executar() {
		dataInicial = get("dataInicial")
		dataFinal 	= get("dataFinal")
		finalidade 	= get("finalidade")
		natOper		= get("natOper")
		aag02s	 	= get("aag02s")
		
		operacao = 2
		
		selecionarAlinhamento("0060")
		
		setAbm01s = new HashSet<Abm01>()
		estados = new ArrayList()
		for(aag02 in aag02s) estados.add(aag02.aag02id)
		
		aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id)
		
		txt = new TextFile("|")
		
		gerarTipos10()
		gerarTipos50()
		gerarTipos70()
		gerarTipos80()
		gerarTipos90()
		
		put("dadosArquivo", txt)
	}
	
	void gerarTipos10() {
		/**
		* TIPO 10
		*/
	   txt.print("10")
	   txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
	   txt.print(inscrEstadual(getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)), 14)
	   txt.print(aac10.aac10rs, 35)
	   txt.print(aac10.aac10municipio == null ? null : aac10.aac10municipio.aag0201nome, 30)
	   txt.print(aac10.aac10municipio == null ? null : aac10.aac10municipio.aag0201uf.aag02id, 2)
	   txt.print(null, 10)
	   txt.print(dataInicial.format(ddMMyyyy))
	   txt.print(dataFinal.format(ddMMyyyy))
	   txt.print("3")
	   txt.print(natOper)
	   txt.print(finalidade)
	   txt.newLine()

	   /**
		* TIPO 11
		*/
	   txt.print("11")
	   txt.print(aac10.aac10endereco, 34)
	   txt.print(aac10.aac10numero, 5)
	   txt.print(aac10.aac10complem, 22)
	   txt.print(aac10.aac10bairro, 15)
	   txt.print(aac10.aac10cep, 8, '0', true)
	   txt.print(aac10.aac10cNome, 28)
	   txt.print(aac10.aac10fone == null ? null : aac10.aac10dddFone == null ? aac10.aac10fone : aac10.aac10dddFone + aac10.aac10fone, 12, '0', true)
	   txt.newLine()
	}
	
	void gerarTipos50() {
		/**
		 * TIPO 50
		 */
		def eaa01s = buscarDocComEscritFiscalPorOperDataUfModelo(Utils.list("01", "1A", "06", "21", "22", "55"), operacao, estados)
		for(tm in eaa01s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 50: Documento: " + tm.getInteger("abb01num"))
			
			def ni = numInscricao(tm.getString("aag02uf"), tm.getString("eaa0102ni"))
			
			txt.print("50")
			txt.print(ni, 14, '0', true)
			txt.print(inscrEstadual(tm.getString("eaa0102ni")), 14)
			txt.print(tm.getDate("eaa01esData") == null ? null : tm.getDate("eaa01esData").format(ddMMyyyy))
			txt.print(tm.getString("aag02uf"), 2)
			txt.print(tm.getString("aah01modelo"))
			txt.print(tm.getString("abb01serie"), 3)
			txt.print(numero(tm.getInteger("abb01num")), 6)
			txt.print(tm.getString("aaj15codigo"), 4, '0', true)
			txt.print(tm.getInteger("eaa01emissao") == 0 ? "P" : "T")
			
			def eaa01json = tm.getTableMap("eaa01json")
			if(eaa01json == null) {
				eaa01json = new TableMap()
				eaa01json.put(getCampo("50", "vlrcontabil"), 0)
				eaa01json.put(getCampo("50", "bcicms"), 0)
				eaa01json.put(getCampo("50", "icms"), 0)
				eaa01json.put(getCampo("50", "isentasicms"), 0)
				eaa01json.put(getCampo("50", "outrasicms"), 0)
				eaa01json.put(getCampo("50", "txicms"), 0)
			}
			txt.print(eaa01json.getBigDecimal(getCampo("50", "vlrcontabil")) == null ? 0 : eaa01json.getBigDecimal(getCampo("50", "vlrcontabil")).multiply(100).intValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("50", "bcicms")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("50", "icms")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("50", "isentasicms")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("50", "outrasicms")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("50", "txicms")).multiply(100).longValue(), 4)
			
			Aaj03 aaj03 = getSession().get(Aaj03.class, tm.getLong("eaa01sitDoc"))
			txt.print(aaj03 == null ? null : aaj03.aaj03sintegra, 1)
			txt.newLine()
			
			totalTipo50++
		}
		
		/**
		 * TIPO 51
		 */
		eaa01s = buscarDocComEscritFiscalPorOperDataUfModelo(Utils.list("01", "1A", "06", "21", "22", "55"), operacao, estados)
		for(tm in eaa01s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 51: Documento: " + tm.getInteger("abb01num"))
			
			def ni = numInscricao(tm.getString("aag02uf"), tm.getString("eaa0102ni"))
			
			txt.print("51")
			txt.print(ni, 14, '0', true)
			txt.print(inscrEstadual(tm.getString("eaa0102ie")), 14)
			txt.print(tm.getDate("abb01data").format(ddMMyyyy))
			txt.print(tm.getString("aag02uf"), 2)
			txt.print(tm.getString("abb01serie"), 3)
			txt.print(numero(tm.getInteger("abb01num")), 6)
			txt.print(tm.getString("aaj15codigo"), 4, '0', true)
			
			def eaa01json = tm.getTableMap("eaa01json")
			if(eaa01json == null) {
				eaa01json = new TableMap()
				eaa01json.put(getCampo("51", "vlrcontabil"), 0)
				eaa01json.put(getCampo("51", "ipi"), 0)
				eaa01json.put(getCampo("51", "icms"), 0)
				eaa01json.put(getCampo("51", "isentasipi"), 0)
				eaa01json.put(getCampo("51", "outrasipi"), 0)
			}
			txt.print(eaa01json.getBigDecimal(getCampo("51", "vlrcontabil")) == null ? 0 : eaa01json.getBigDecimal(getCampo("51", "vlrcontabil")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("51", "ipi")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("51", "isentasipi")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("51", "outrasipi")).multiply(100).longValue(), 13)
			txt.print(StringUtils.space(20))
			
			Aaj03 aaj03 = getSession().get(Aaj03.class, tm.getLong("eaa01sitDoc"))
			txt.print(aaj03 == null ? null : aaj03.aaj03sintegra, 1)
			txt.newLine()
			
			totalTipo51++
		}
		
		
		/**
		 * TIPO 53
		 */
		eaa01s = buscarDocComEscritFiscalPorOperDataUfModelo(Utils.list("01", "1A", "06", "21", "22", "55"), operacao, estados)
		for(tm in eaa01s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 53: Documento: " + tm.getInteger("abb01num"))
			
			if(tm.getInteger("eaa0103st") != 1) continue
			
			def ni = numInscricao(tm.getString("aag02uf"), tm.getString("eaa0102ni"))
			
			txt.print("53")
			txt.print(ni, 14, '0', true)
			txt.print(inscrEstadual(tm.getString("eaa0102ie")), 14)
			txt.print(tm.getDate("abb01data").format(ddMMyyyy))
			txt.print(tm.getString("aag02uf"), 2)
			txt.print(tm.getString("aah01modelo"))
			txt.print(tm.getString("abb01serie"), 3)
			txt.print(numero(tm.getInteger("abb01num")), 6)
			txt.print(tm.getString("aaj15codigo"), 4, '0', true)
			txt.print(tm.getInteger("eaa01emissao") == 0 ? "P" : "T")
			
			def eaa01json = tm.getTableMap("eaa01json")
			if(eaa01json == null) {
				eaa01json = new TableMap()
				eaa01json.put(getCampo("53", "bcicmsst"), 0)
				eaa01json.put(getCampo("53", "icmsst"), 0)
				eaa01json.put(getCampo("53", "item991"), 0)
				eaa01json.put(getCampo("53", "item992"), 0)
				eaa01json.put(getCampo("53", "item999"), 0)
			}
			txt.print(eaa01json.getBigDecimal(getCampo("53", "bcicmsst")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("53", "icmsst")).multiply(100).longValue(), 13)
			txt.print(eaa01json.getBigDecimal(getCampo("53", "item991"))
							   .add(eaa01json.getBigDecimal(getCampo("53", "item992")))
							   .add(eaa01json.getBigDecimal(getCampo("53", "item999")))
							   .multiply(100).longValue(), 13)
			
			Aaj03 aaj03 = getSession().get(Aaj03.class, tm.getLong("eaa01sitDoc"))
			txt.print(aaj03 == null ? null : aaj03.aaj03sintegra, 1)
			txt.print(eaa01json.getInteger("ea04codantst") == 0 ? " " : eaa01json.getInteger("ea04codantst"))
			txt.print(StringUtils.space(29))
			txt.newLine()
			
			totalTipo53++
		}
		
		
		/**
		 * TIPO 54
		 */
		def mapRegistro54 = new TableMap()
		comporRegistro54(mapRegistro54)
		
		for(key in mapRegistro54.keySet()) {
			txt.print("54")
			txt.print(mapRegistro54.getTableMap(key).getString("eaa0102ni"), 14, '0', true)
			txt.print(mapRegistro54.getTableMap(key).getString("aah01modelo"))
			txt.print(mapRegistro54.getTableMap(key).getString("abb01serie"), 3)
			txt.print(mapRegistro54.getTableMap(key).getInteger("abb01num"), 6)
			txt.print(mapRegistro54.getTableMap(key).getString("aaj15codigo"), 4, '0', true)
			txt.print(mapRegistro54.getTableMap(key).getString("cst"), 3)
			txt.print(mapRegistro54.getTableMap(key).getInteger("eaa0103seq"), 3)
			txt.print(mapRegistro54.getTableMap(key).getString("abm01codigo"), 14)
			txt.print(mapRegistro54.getTableMap(key).getBigDecimal("qt").round(3).multiply(1000).longValue(), 11)
			txt.print(mapRegistro54.getTableMap(key).getBigDecimal("valor").multiply(100).longValue(), 12)
			txt.print(mapRegistro54.getTableMap(key).getBigDecimal(getCampo("54", "desconto")).multiply(100).longValue(), 12)
			txt.print(mapRegistro54.getTableMap(key).getBigDecimal(getCampo("54", "bcicms")).multiply(100).longValue(), 12)
			txt.print(mapRegistro54.getTableMap(key).getBigDecimal(getCampo("54", "bcicmsst")).multiply(100).longValue(), 12)
			txt.print(mapRegistro54.getTableMap(key).getBigDecimal(getCampo("54", "ipi")).multiply(100).longValue(), 12)
			txt.print(mapRegistro54.getTableMap(key).getBigDecimal(getCampo("54", "txicms")).multiply(100).longValue(), 4)
			txt.newLine()
			
			totalTipo54++
		}
		
		/**
		 * TIPO 55
		 */
		def edd01s = buscarGNREPorData()
		if(edd01s != null && edd01s.size() > 0) {
			for(edd01 in edd01s) {
				verificarProcessoCancelado()
				enviarStatusProcesso("Compondo registro Tipo 55")
				
				txt.print("55")
				txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
				
				def aac1002 = edd01.edd01ufDest == null ? null : getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, edd01.edd01ufDest.aag02id)
				txt.print(aac1002 == null ? null : StringUtils.extractNumbers(aac1002), 14)
				
				txt.print(edd01.edd01data.format(ddMMyyyy))
				txt.print(edd01.edd01ufST == null ? null : edd01.edd01ufST.aag02uf, 2)
				txt.print(edd01.edd01ufDest == null ? null : edd01.edd01ufDest.aag02uf, 2)
				txt.print(edd01.edd01banco, 3, '0', true)
				txt.print(edd01.edd01agencia, 4, '0', true)
				txt.print(edd01.edd01aut, 20)
				txt.print(edd01.edd01valor.multiply(100).longValue(), 13, '0', true)
				txt.print(edd01.edd01vcto == null ? 0 : edd01.edd01vcto.format(ddMMyyyy), 8)
				txt.print(edd01.edd01mes + edd01.edd01ano, 6)
				txt.print(edd01.edd01conv, 30)
				txt.newLine()
				
				totalTipo55++
			}
		}
	}
	
	void gerarTipos70() {
		/**
		 * TIPO 70
		 */
		def eaa01s = buscarDocComEscritFiscalPorOperDataUfModelo(Utils.list("07", "08", "09", "10", "11", "26", "27"), operacao, estados);
		
		for(tm in eaa01s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 70: Documento: " + tm.getInteger("abb01num"))
			
			def ni = numInscricao(tm.getString("aag02uf"), tm.getString("eaa0102ni"))
			
			txt.print("70")
			txt.print(ni, 14, '0', true)
			txt.print(inscrEstadual(tm.getString("eaa0102ie")), 14)
			txt.print(tm.getDate("abb01data").format(ddMMyyyy))
			txt.print(tm.getString("aag02uf"), 2)
			txt.print(tm.getString("aah01modelo"), 2)
			txt.print(tm.getString("abb01serie"), 1)
			txt.print(StringUtils.space(2))
			txt.print(numero(tm.getInteger("abb01num")), 6)
			txt.print(tm.getString("aaj15codigo"), 4)
			
			def eaa01json = tm.getTableMap("eaa01json")
			if(eaa01json == null) {
				eaa01json = new TableMap()
				eaa01json.put(getCampo("70", "vlrcontabil"), 0)
				eaa01json.put(getCampo("70", "bcicms"), 0)
				eaa01json.put(getCampo("70", "icms"), 0)
				eaa01json.put(getCampo("70", "isentasicms"), 0)
				eaa01json.put(getCampo("70", "outrasicms"), 0)
			}
			txt.print(tm.getBigDecimal(getCampo("70", "vlrcontabil")).multiply(100).longValue(), 13)
			txt.print(tm.getBigDecimal(getCampo("70", "bcicms")).multiply(100).longValue(), 14)
			txt.print(tm.getBigDecimal(getCampo("70", "icms")).multiply(100).longValue(), 14)
			txt.print(tm.getBigDecimal(getCampo("70", "isentasicms")).multiply(100).longValue(), 14)
			txt.print(tm.getBigDecimal(getCampo("70", "outrasicms")).multiply(100).longValue(), 14)
			
			txt.print(tm.getInteger("eaa0102frete"), 1)
			
			Aaj03 aaj03 = getSession().get(Aaj03.class, tm.getLong("eaa01sitDoc"))
			txt.print(aaj03 == null ? null : aaj03.aaj03sintegra, 1)
			txt.newLine()
			
			totalTipo70++
		}
		
		/**
		 * TIPO 74
		 */
		def bcb11s = buscarItensInventarioPorAnoMes();
		for(bcb11 in bcb11s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 74")
			
			def abm01 = bcb11.bcb11item
			if(abm01 != null) {
				def abe01 = bcb11.bcb11ent
				
				if(bcb11.bcb11qtde == 0) continue;
				setAbm01s.add(abm01);
				
				txt.print("74")
				txt.print(dataFinal.format(ddMMyyyy))
				txt.print(abm01.abm01codigo, 14)
				txt.print(bcb11.bcb11qtde.multiply(1000).longValue(), 13)
				txt.print(bcb11.bcb11total.round(2).multiply(100).longValue(), 13)
				txt.print(bcb11.bcb11grupo.abm40posse == 0 ? "1" : bcb11.bcb11grupo.abm40posse == 1 ? "2" : "3")
				if(bcb11.bcb11grupo.abm40posse == 0) {
					txt.print(0, 14)
					txt.print(StringUtils.space(14))
					txt.print(StringUtils.space(2))
				}else {
					txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, '0', true)
					txt.print(inscrEstadual(abe01.abe01ie), 14)
					
					def aag02 = buscarUfEntidade(abe01.abe01id);
					txt.print(aag02 == null ? null : aag02.aag02uf, 2)
				}
				
				txt.print(StringUtils.space(45))
				txt.newLine()
				
				totalTipo74++
			}
		}
		
		/**
		 * TIPO 75
		 */
		for(abm01 in setAbm01s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 75")
			
			txt.print("75")
			txt.print(dataInicial.format(ddMMyyyy))
			txt.print(dataFinal.format(ddMMyyyy))
			txt.print(abm01.abm01codigo, 14)
			
			def ncm = buscarNcmItem(abm01.abm01id)
			txt.print(ncm == null ? null : ncm.abg01codigo.indexOf("/") == -1 ? ncm.abg01codigo : ncm.abg01codigo.substring(0, ncm.abg01codigo.indexOf("/")), 8)
			txt.print(abm01.abm01descr, 53)
			
			def aam06 = abm01.abm01umu != null ? getSession().get(Aam06.class, abm01.abm01umu.aam06id) : null
			txt.print(aam06 != null ? aam06.aam06codigo : null, 6)
			
			def abm10 = buscarValoresItem(abm01.abm01id)
			def abm10json = abm10.abm10json
			if(abm10json == null) {
				abm10json = new TableMap()
				abm10json.put(getCampo("75", "txIpi"), 0)
				abm10json.put(getCampo("75", "txIcms"), 0)
				abm10json.put(getCampo("75", "redIcms"), 0)
				abm10json.put(getCampo("75", "bcIcmsSt"), 0)
			}
			txt.print(abm10json.getBigDecimal(getCampo("75", "txIpi")).round(2).multiply(100).longValue(), 5)
			txt.print(abm10json.getBigDecimal(getCampo("75", "txIcms")).round(2).multiply(100).longValue(), 4)
			txt.print(abm10json.getBigDecimal(getCampo("75", "redIcms")).round(2).multiply(100).longValue(), 5)
			txt.print(abm10json.getBigDecimal(getCampo("75", "bcIcmsSt")).round(2).multiply(100).longValue(), 13)
			txt.newLine()
			
			totalTipo75++
		}
	}
	
	void gerarTipos80() {
		/**
		 * TIPO 85
		 */
		def eaa0104s = buscarDocComEscritFiscalPorDataUfModeloComExportacao(Utils.list("01", "1A", "06", "21", "22", "55"), estados)
		for(tm in eaa0104s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 85: Documento: " + tm.getInteger("abb01num"))
			
			txt.print("85")
			txt.print(StringUtils.extractNumbers(tm.getString("eaa0104num")), 11, '0', true)
			txt.print(tm.getDate("eaa0104data") == null ? 0 : tm.getDate("eaa0104data").format(ddMMyyyy), 8)
			txt.print(tm.getInteger("eaa0104nat"), 1)
			txt.print(tm.getString("eaa01041num"), 12)
			txt.print(tm.getDate("eaa01041data") == null ? 0 : tm.getDate("eaa01041data").format(ddMMyyyy), 8)
			
			if(tm.getString("eaa0104ceNum") != null) {
				txt.print(tm.getString("eaa0104ceNum"), 16)
				txt.print(tm.getDate("eaa0104ceData") == null ? 0 : tm.getDate("eaa0104ceData").format(ddMMyyyy), 8)
				txt.print(tm.getInteger("eaa0104ceTipo"), 2)
			}else {
				txt.print("PROPRIO", 16)
				txt.print(0, 8)
				txt.print("99")
			}
			
			txt.print(tm.getString("aag01ibge"), 4)
			txt.print(0, 8)
			txt.print(tm.getDate("eaa0104dtAverb") == null ? 0 : tm.getDate("eaa0104dtAverb").format(ddMMyyyy), 8)
			txt.print(numero(tm.getInteger("abb01num")), 6)
			txt.print(tm.getDate("abb01data").format(ddMMyyyy))
			txt.print(tm.getString("aah01modelo"), 2)
			txt.print(tm.getString("abb01serie"), 3)
			txt.print(StringUtils.space(19))
			txt.newLine();
			
			totalTipo85++;
		}
		
		/**
		 * TIPO 88C -> Complemento do tipo 54
		 */
		def eaa01s = buscarDocComEscritFiscalPorOperDataUfModelo(Utils.list(""), operacao, estados);
		for(tm in eaa01s) {
			verificarProcessoCancelado()
			enviarStatusProcesso("Compondo registro Tipo 88: Documento: " + tm.getInteger("abb01num"))
			
			def eaa0103s = buscarItensPorDocumento(tm.getLong("eaa01id"))
			for(eaa0103 in eaa0103s) {
				verificarProcessoCancelado()
					
				def ni = numInscricao(tm.getString("aag02uf"), tm.getString("eaa0102ni"));
				
				txt.print("88")
				txt.print("C")
				txt.print(ni, 14, '0', true)
				txt.print(tm.getString("aah01modelo"))
				txt.print(tm.getString("abb01serie"), 3)
				txt.print(numero(tm.getInteger("abb01num")), 6)
				txt.print(tm.getString("aaj15codigo"), 4, '0', true)
				txt.print(eaa0103.eaa0103seq, 3)
				txt.print(eaa0103.eaa0103item.abm01codigo, 14)
				txt.print(eaa0103.eaa0103qtComl.round(3).multiply(1000).longValue(), 11)
				
				def eaa0103json = eaa0103.eaa0103json
				if(eaa0103json == null) {
					eaa0103json = new TableMap()
					eaa0103json.put(getCampo("88", "bcicmsst"), 0)
					eaa0103json.put(getCampo("88", "icmsst"), 0)
					eaa0103json.put(getCampo("88", "icmsstcomp"), 0)
					eaa0103json.put(getCampo("88", "bcicmsstrem"), 0)
					eaa0103json.put(getCampo("88", "icmsstrem"), 0)
				}
				txt.print(eaa0103json.getBigDecimal(getCampo("88", "bcicmsst")).multiply(100).longValue(), 12)
				txt.print(eaa0103json.getBigDecimal(getCampo("88", "icmsst")).multiply(100).longValue(), 12)
				txt.print(eaa0103json.getBigDecimal(getCampo("88", "icmsstcomp")).multiply(100).longValue(), 12)
				txt.print(eaa0103json.getBigDecimal(getCampo("88", "bcicmsstrem")).multiply(100).longValue(), 12)
				txt.print(eaa0103json.getBigDecimal(getCampo("88", "icmsstrem")).multiply(100).longValue(), 12)
				txt.print(StringUtils.space(6))
				txt.newLine();
				
				totalTipo88++;
			}
		}
		
		/**
		 * TIPO 88D -> Complemento do tipo 50
		 */
		eaa01s = buscarDocComEscritFiscalPorOperDataUfModelo(Utils.list(""), operacao, estados)
		for(tm in eaa01s) {
			verificarProcessoCancelado()
			
			Eaa0102 eaa0102 = getSession().get(Eaa0102.class, tm.getLong("eaa01id"))
			
			def endPrin = buscarEnderecoDocumento(tm.getLong("eaa01id"), 0)
			def endSaid = buscarEnderecoDocumento(tm.getLong("eaa01id"), 1)
			def endEntr = buscarEnderecoDocumento(tm.getLong("eaa01id"), 2)
			
			def ni = numInscricao(tm.getString(i, "aag02uf"), tm.getString("eaa0102ni"))
			
			txt.print("88")
			txt.print("D")
			txt.print(ni, 14, '0', true)
			txt.print(inscrEstadual(eaa0102.eaa0102ie), 14)
			txt.print(endPrin.eaa0101municipio == null ? null : endPrin.eaa0101municipio.aag0201uf.aag02uf, 2)
			txt.print(tm.getString("aah01modelo"))
			txt.print(tm.getString("abb01serie"), 3)
			txt.print(numero(tm.getInteger("abb01num")), 6)
			txt.print(tm.getString("aaj15codigo"), 4, '0', true)
			txt.print(tm.getDate("abb01data").format(ddMMyyyy))
			txt.print(tm.getDate("eaa01esData").format(ddMMyyyy))
			txt.print(StringUtils.extractNumbers(endSaid.eaa0101ni), 14)
			txt.print(endSaid.eaa0101municipio == null ? null : endSaid.eaa0101municipio.aag0201uf.aag02uf, 2)
			txt.print(StringUtils.extractNumbers(endSaid.eaa0101ie), 14)
			txt.print(StringUtils.extractNumbers(endEntr.eaa0101ni), 14)
			txt.print(endEntr.eaa0101municipio == null ? null : endEntr.eaa0101municipio.aag0201uf.aag02uf, 2)
			txt.print(StringUtils.extractNumbers(endEntr.eaa0101ie), 14)
			txt.print(StringUtils.space(2))
			txt.newLine();
			
			totalTipo88++;
		}
		
		/**
		 * TIPO 88E -> Complemento do 75
		 */
		for(abm01 in setAbm01s) {
			def abm0101 = getSession().get(Abm0101.class, Criterions.eq("abm0101item", abm01.abm01id))
			def abm12 = abm0101 != null && abm0101.abm0101fiscal != null ? getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id) : null 
			if(abm12 == null) continue;
			
			def aaj04 = abm12.abm12codANP != null ? getSession().get(Aaj04.class, abm12.abm12codANP.aaj04id) : null
			
			verificarProcessoCancelado()
			
			txt.print("88")
			txt.print("E")
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			
			def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)
			txt.print(inscrEstadual(ie), 14)
			txt.print(abm01.abm01codigo, 14)
			txt.print(aaj04 == null ? null : aaj04.aaj04codigo, 14)
			txt.print(StringUtils.space(67))
			txt.newLine()
			
			totalTipo88++;
		}
		
		/**
		 * TIPO 88M - Período sem movimento
		 */
		if(totalTipo88 == 0) {
			txt.print("88")
			txt.print("M")
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print("SEM MOVIMENTO DE ENTRADAS / SAÍDAS", 34)
			txt.print(StringUtils.space(75))
			txt.newLine()
		}
		
		/**
		 * TIPO 88T -> Complemento do tipo 50
		 */
		for(tm in eaa01s) {
			verificarProcessoCancelado()
			
			Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", tm.getLong("eaa01id")))
			
			def ni = numInscricao(tm.getString("aag02uf"), tm.getString("ea012ni"))
			
			txt.print("88")
			txt.print("T")
			txt.print(ni, 14, '0', true)
			txt.print(tm.getDate("abb01data").format(ddMMyyyy))
			txt.print(tm.getString("aag02uf"), 2)
			txt.print(tm.getString("aah01modelo"))
			txt.print(tm.getString("abb01serie"), 3)
			txt.print(numero(tm.getInteger("abb01num")), 6)
			txt.print(eaa0102.eaa0102frete == 1 ? "1" : "2")
			
			def transp = eaa0102.eaa0102despacho != null ? getSession().get(Abe01.class, eaa0102.eaa0102despacho.abe01id) : null
			if(transp != null) {
				def uf = buscarUfEntidade(transp.abe01id);
				txt.print(StringUtils.extractNumbers(transp.abe01ni), 14, '0', true)
				txt.print(uf == null ? null : uf.aag02uf, 2)
				txt.print(inscrEstadual(transp.abe01ie), 14)
			}else {
				txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
				txt.print(aac10.aac10municipio == null ? null : aac10.aac10municipio.aag0201uf.aag02uf, 2)
				
				def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)
				txt.print(inscrEstadual(ie), 14)
			}

			txt.print(eaa0102.eaa0102modFrete)
				
			def aah20 = eaa0102.eaa0102veiculo != null ? getSession().get(Aah20.class, eaa0102.eaa0102veiculo.aah20id) : null
			txt.print(aah20 == null ? null : aah20.aah20placa, 7)
			txt.print(aah20 == null ? null : aah20.aah20ufPlaca, 2)
			
			aah20 = eaa0102.eaa0102reboque1 != null ? getSession().get(Aah20.class, eaa0102.eaa0102reboque1.aah20id) : null
			txt.print(aah20 == null ? null : aah20.aah20placa, 7)
			txt.print(aah20 == null ? null : aah20.aah20ufPlaca, 2)
			
			aah20 = eaa0102.eaa0102reboque2 != null ? getSession().get(Aah20.class, eaa0102.eaa0102reboque2.aah20id) : null
			txt.print(aah20 == null ? null : aah20.aah20placa, 7)
			txt.print(aah20 == null ? null : aah20.aah20ufPlaca, 2)
			
			aah20 = eaa0102.eaa0102reboque3 != null ? getSession().get(Aah20.class, eaa0102.eaa0102reboque3.aah20id) : null
			txt.print(aah20 == null ? null : aah20.aah20placa, 7)
			txt.print(aah20 == null ? null : aah20.aah20ufPlaca, 2)

			txt.print(StringUtils.space(29))
			txt.newLine();
			
			totalTipo88++;
		}
	}
	
	void gerarTipos90() {
		/**
		 * TIPO 90
		 */
		verificarProcessoCancelado()
		enviarStatusProcesso("Compondo registro Tipo 90")

		def totalizacao = new StringBuilder("")
		
		if(totalTipo50 > 0) totalizacao.append("50" + StringUtils.ajustString(totalTipo50, 8, '0', true));
		if(totalTipo51 > 0) totalizacao.append("51" + StringUtils.ajustString(totalTipo51, 8, '0', true));
		if(totalTipo53 > 0) totalizacao.append("53" + StringUtils.ajustString(totalTipo53, 8, '0', true));
		if(totalTipo54 > 0) totalizacao.append("54" + StringUtils.ajustString(totalTipo54, 8, '0', true));
		if(totalTipo55 > 0) totalizacao.append("55" + StringUtils.ajustString(totalTipo55, 8, '0', true));
		if(totalTipo61 > 0) totalizacao.append("61" + StringUtils.ajustString(totalTipo61, 8, '0', true));
		if(totalTipo70 > 0) totalizacao.append("70" + StringUtils.ajustString(totalTipo70, 8, '0', true));
		if(totalTipo71 > 0) totalizacao.append("71" + StringUtils.ajustString(totalTipo71, 8, '0', true));
		if(totalTipo74 > 0) totalizacao.append("74" + StringUtils.ajustString(totalTipo74, 8, '0', true));
		if(totalTipo75 > 0) totalizacao.append("75" + StringUtils.ajustString(totalTipo75, 8, '0', true));
		if(totalTipo85 > 0) totalizacao.append("85" + StringUtils.ajustString(totalTipo85, 8, '0', true));
		if(totalTipo88 > 0) totalizacao.append("88" + StringUtils.ajustString(totalTipo88, 8, '0', true));
		
		//O número 2 é referente aos registros 10 e 11
		Integer totalGeral = 2 + totalTipo50 + totalTipo51 + totalTipo53 + totalTipo54 + totalTipo55 + 
							 totalTipo61 + totalTipo70 + totalTipo71 + totalTipo74 + totalTipo75 + totalTipo85 + totalTipo88;
		
		if(totalizacao.toString().length() > 0) comporRegistro90(totalizacao.toString(), totalGeral);
	}
	
	private void comporRegistro90(String totalizacao, Integer totalGeral) {
		if(totalizacao.length() > 90) {
			txt.print("90")
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true)
			txt.print(inscrEstadual(getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)), 14)
			totalizacao = totalizacao.substring(0, 90)
			txt.print(totalizacao, 95)
			txt.print("2")
			txt.newLine()
			totalGeral = totalGeral + 2
			
			txt.print("90");
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);
			txt.print(inscrEstadual(getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)), 14);
			totalizacao = totalizacao.substring(90);
			totalizacao = totalizacao + "99" + StringUtils.ajustString(totalGeral, 8);
			txt.print(totalizacao, 95);
			txt.print("2");
			txt.newLine();
			
		}else{
			totalGeral++;
			
			txt.print("90");
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);
			txt.print(inscrEstadual(getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id)), 14);
			txt.print(totalizacao + "99" + StringUtils.ajustString(totalGeral, 8), 95);
			txt.print("1");
			txt.newLine();
		}
	}
	
	String inscrEstadual(String ie) {
		if(ie == null) return "ISENTO"
		if(ie.equalsIgnoreCase("ISENTO") || ie.equalsIgnoreCase("ISENTA")) return "ISENTO"
		return StringUtils.extractNumbers(ie)
	}
	
	String numInscricao(String aag02uf, String ni) {
		return aag02uf == null ? StringUtils.extractNumbers(ni) : aag02uf.equals("EX") ? null : StringUtils.extractNumbers(ni)
	}
	
	Integer numero(Integer num) {
		if(num > 999999) {
			int tam = num.toString().length()
			num = Integer.parseInt(num.toString().substring(tam-6, tam))
			return num
		}else {
			return num
		}
	}
	
	void comporRegistro54(TableMap mapRegistro54) {
		NumberFormat format = NumberFormat.getIntegerInstance()
		format.setMinimumIntegerDigits(3)
		
		enviarStatusProcesso("Selecionando registros para o Tipo 54")
		def eaa01s = buscarDocComEscritFiscalPorOperDataUfModelo(Utils.list("01", "1A", "06", "21", "22", "55"), operacao, estados)
		
		if(eaa01s != null && eaa01s.size() > 0) {
			def mapDespesas = new TableMap()
			
			for(tm in eaa01s) {
				verificarProcessoCancelado()
				enviarStatusProcesso("Compondo registro Tipo 54: Documento: " + tm.getInteger("abb01num"))
	
				if(tm.getDate("eaa01cancData") != null) continue
				
				def ni = numInscricao(tm.getString("aag02uf"), tm.getString("eaa0102ni"))
				
				def eaa0103s = buscarItensPorDocumento(tm.getLong("eaa01id"))
				for(eaa0103 in eaa0103s) {
					verificarProcessoCancelado()

					def key = tm.getLong("eaa01id") + "/" + tm.getString("aaj15codigo") + "/" + format.format(eaa0103.eaa0103seq);
					
					def tmi = new TableMap();
					tmi.put("eaa0102ni", ni);
					tmi.put("aah01modelo", tm.getString("aah01modelo"));
					tmi.put("abb01serie", tm.getString("abb01serie"));
					tmi.put("abb01num", numero(tm.getInteger("abb01num")));
					tmi.put("aaj15codigo", tm.getString("aaj15codigo"));
					tmi.put("eaa0103seq", eaa0103.eaa0103seq);
					tmi.put(getCampo("54", "desconto"), eaa0103.eaa0103json.getBigDecimal(getCampo("54", "desconto")));
					tmi.put(getCampo("54", "bcicms"), eaa0103.eaa0103json.getBigDecimal(getCampo("54", "bcicms")));
					tmi.put(getCampo("54", "bcicmsst"), eaa0103.eaa0103json.getBigDecimal(getCampo("54", "bcicmsst")));
					tmi.put(getCampo("54", "ipi"), eaa0103.eaa0103json.getBigDecimal(getCampo("54", "ipi")));
					tmi.put(getCampo("54", "txicms"), eaa0103.eaa0103json.getBigDecimal(getCampo("54", "txicms")));
					
					def abm01 = eaa0103.eaa0103item
					tmi.put("cst", eaa0103.eaa0103cstIcms == null ? null : eaa0103.eaa0103cstIcms.aaj10codigo)
					tmi.put("ab50codigo", abm01.abm01codigo);
					tmi.put("qt", eaa0103.eaa0103qtComl);
					tmi.put("valor", eaa0103.eaa0103json.getBigDecimal(getCampo("54", "valor")))
						
					setAbm01s.add(abm01)
						
					/**
					 * DESPESAS
					 */
					if(eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item991")) != 0) {
						comporDespesaAcessoria(mapDespesas, tm.getLong("eaa01id"), ni, tm.getString("aah01modelo"), tm.getString("abb01serie"), tm.getInteger("abb01num"), tm.getString("aaj15codigo"), "991", eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item991")))
					}
					if(eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item992")) != 0) {
						comporDespesaAcessoria(mapDespesas, tm.getLong("eaa01id"), ni, tm.getString("aah01modelo"), tm.getString("abb01serie"), tm.getInteger("abb01num"), tm.getString("aaj15codigo"), "992", eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item992")))
					}
					if(eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item993")) != 0) {
						comporDespesaAcessoria(mapDespesas, tm.getLong("eaa01id"), ni, tm.getString("aah01modelo"), tm.getString("abb01serie"), tm.getInteger("abb01num"), tm.getString("aaj15codigo"), "993", eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item993")))
					}
					if(eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item997")) != 0) {
						comporDespesaAcessoria(mapDespesas, tm.getLong("eaa01id"), ni, tm.getString("aah01modelo"), tm.getString("abb01serie"), tm.getInteger("abb01num"), tm.getString("aaj15codigo"), "997", eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item997")))
					}
					if(eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item998")) != 0) {
						comporDespesaAcessoria(mapDespesas, tm.getLong("eaa01id"), ni, tm.getString("aah01modelo"), tm.getString("abb01serie"), tm.getInteger("abb01num"), tm.getString("aaj15codigo"), "998", eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item998")))
					}
					if(eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item999")) != 0) {
						comporDespesaAcessoria(mapDespesas, tm.getLong("eaa01id"), ni, tm.getString("aah01modelo"), tm.getString("abb01serie"), tm.getInteger("abb01num"), tm.getString("aaj15codigo"), "999", eaa0103.eaa0103json.getBigDecimal(getCampo("54", "item999")))
					}
					
					mapRegistro54.put(key, tmi)
				}
			}
			
			comporRegistro54_DespesaAcessoria(mapDespesas, mapRegistro54);
		}
	}
	
	private void comporDespesaAcessoria(TableMap mapDespesas, Long eaa01id, String ni, String modelo, String serie, Integer num, String cfop, String codDespesa, BigDecimal valor) {
		String key = eaa01id + "/" + cfop + "/" + codDespesa;
		
		def tm = new TableMap();
		tm.put("eaa01id", eaa01id);
		tm.put("eaa0102ni", ni);
		tm.put("aah01modelo", modelo);
		tm.put("abb01serie", serie);
		tm.put("abb01num", numero(num));
		tm.put("aaj15codigo", cfop);
		tm.put("cst", null);
		tm.put("eaa0103seq", codDespesa);
		tm.put("abm01codigo", null);
		tm.put("qt", BigDecimal.ZERO);
		tm.put("valor", BigDecimal.ZERO);
		
		def desconto = mapDespesas.getTableMap(key) != null ? mapDespesas.getTableMap(key).getBigDecimal("desconto") != null ? mapDespesas.getTableMap(key).getBigDecimal("desconto").add(valor) : BigDecimal.ZERO : BigDecimal.ZERO
		tm.put(getCampo("54", "desconto"), desconto);
		tm.put(getCampo("54", "bcicms"), BigDecimal.ZERO);
		tm.put(getCampo("54", "bcicmsst"), BigDecimal.ZERO);
		tm.put(getCampo("54", "ipi"), BigDecimal.ZERO);
		tm.put(getCampo("54", "txicms"), BigDecimal.ZERO);
		
		mapDespesas.put(key, tm);
	}
	
	void comporRegistro54_DespesaAcessoria(TableMap mapDespesas, TableMap mapRegistro54) {
		NumberFormat format = NumberFormat.getIntegerInstance();
		format.setMinimumIntegerDigits(3);
		
		for(key in mapDespesas.keySet()) { //Chave do mapDespesas: ni + "/" + modelo + "/" + serie + "/" + num + "/" + codDespesa
			def chave = mapDespesas.getTableMap(key).getLong("eaa01id") + "/" + mapDespesas.getTableMap(key).getLong("aaj15codigo") + "/" + format.format(mapDespesas.getTableMap(key).getInteger("eaa0103seq"));
			
			def tm = new TableMap();
			tm.put("eaa0102ni", mapDespesas.getTableMap(key).getString("eaa0103ni"));
			tm.put("aah01modelo", mapDespesas.getTableMap(key).getString("aah01modelo"));
			tm.put("abb01serie", mapDespesas.getTableMap(key).getString("abb01serie"));
			tm.put("abb01num", mapDespesas.getTableMap(key).getInteger("abb01num"));
			tm.put("aaj15codigo", mapDespesas.getTableMap(key).getString("aaj15codigo"));
			tm.put("cst", mapDespesas.getTableMap(key).getString("cst"));
			tm.put("eaa0103seq", mapDespesas.getTableMap(key).getInteger("eaa0103seq"));
			tm.put("abm01codigo", mapDespesas.getTableMap(key).getString("abm01codigo"));
			tm.put("qt", mapDespesas.getTableMap(key).getBigDecimal("qt"));
			tm.put("valor", mapDespesas.getTableMap(key).getBigDecimal("valor"));
			tm.put(getCampo("54", "desconto"), mapDespesas.getTableMap(key).getBigDecimal(getCampo("54", "desconto")));
			tm.put(getCampo("54", "bcicms"), mapDespesas.getTableMap(key).getBigDecimal(getCampo("54", "bcicms")));
			tm.put(getCampo("54", "bcicmsst"), mapDespesas.getTableMap(key).getBigDecimal(getCampo("54", "bcicmsst")));
			tm.put(getCampo("54", "ipi"), mapDespesas.getTableMap(key).getBigDecimal(getCampo("54", "ipi")));
			tm.put(getCampo("54", "txicms"), mapDespesas.getTableMap(key).getBigDecimal(getCampo("54", "txicms")));
			
			mapRegistro54.put(chave, tm)
		}
	}
	
	List<TableMap> buscarDocComEscritFiscalPorOperDataUfModelo(List<String> modelos, Integer mov, List<Long> ufs) {
		def whereOper = mov < 2 ? "AND ea01oper = :oper" : ""
		def whereData = mov == 1 ? " AND eaa01esData BETWEEN :dataInicial AND :dataFinal " : " AND abb01data BETWEEN :dataInicial AND :dataFinal "
		
		def sql = " SELECT DISTINCT eaa01id, abe01id, aah01id, aah01modelo, abb01serie, abb01num, eaa01esMov, eaa01cancData, aaj15codigo, " +
				  " abb01data, eaa01emissao, eaa01sitDoc, eaa0102ie, eaa0102ni, eaa0102frete, aag02uf, eaa0102despacho, eaa01json, eaa0103st " +
				  " FROM Eaa01 INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " LEFT JOIN Abe01 ON abe01id = abb01ent " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " WHERE aag02id IN (:ufs) " +
				  " AND eaa0101principal = 1 " +
				  " AND aah01modelo IN (:modelos) " +
				  	whereOper + whereData + obterWherePadrao("Eaa01") + 
				  " ORDER BY abb01num, eaa01id";
		
		def p1 = criarParametroSql("ufs", ufs);
		def p2 = criarParametroSql("dataInicial", dataInicial);
		def p3 = criarParametroSql("dataFinal", dataFinal);
		def p4 = criarParametroSql("modelos", modelos);
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4);
	}
	
	List<TableMap> buscarDocComEscritFiscalPorDataUfModeloComExportacao(List<String> modelos, List<Long> ufs) {
		def sql = " SELECT eaa01id, eaa0104num, eaa0104data, eaa0104nat, eaa01041num, eaa01041data, eaa0104ceNum, eaa0104ceData, " + 
				  " eaa0104ceTipo, eaa0104pais, eaa0104dtAverb, aah01modelo, abb01serie, abb01num, abb01data, aag01ibge  " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " LEFT JOIN Abe01 ON abe01id = abb01ent " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				  " INNER JOIN Eaa0104 ON eaa0104doc = eaa01id " +
				  " INNER JOIN Eaa01041 ON eaa01041de = eaa0104id " +
				  " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " LEFT JOIN Aag01 ON aag01id = eaa0104pais " +
				  " WHERE aag02id IN (:ufs) AND aah01modelo IN (:modelos) " +
				  " AND eaa0104dtAverb BETWEEN :dataInicial AND :dataFinal " + 
				    obterWherePadrao("Eaa01") + " ORDER BY eaa0104dtAverb, abb01num";
		
		def p1 = criarParametroSql("ufs", ufs);
		def p2 = criarParametroSql("dataInicial", dataInicial);
		def p3 = criarParametroSql("dataFinal", dataFinal);
		def p4 = criarParametroSql("modelos", modelos);
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4)
	}
	
	List<Eaa0103> buscarItensPorDocumento(Long eaa01id) {
		return getSession().createCriteria(Eaa0103.class)
					.addJoin(Joins.fetch("eaa0103item"))
					.addJoin(Joins.fetch("eaa0103cstIcms"))
					.addWhere(Criterions.eq("eaa0103doc", eaa01id))
					.addWhere(Criterions.in("eaa0103retInd", [0, 1]))
					.getList(ColumnType.ENTITY)
	}
	
	List<Edd01> buscarGNREPorData() {
		return getSession().createCriteria(Edd01.class)
					.addJoin(Joins.fetch("edd01ufST"))
					.addJoin(Joins.fetch("edd01ufDest"))
					.addWhere(Criterions.between("edd01data", dataInicial, dataFinal))
					.addWhere(Criterions.where(obterWherePadrao("Edd01", "")))
					.getList(ColumnType.ENTITY)
	}
	
	List<Bcb11> buscarItensInventarioPorAnoMes() {
		return getSession().createCriteria(Bcb11.class)
						.addJoin(Joins.fetch("bcb11inv"))
						.addJoin(Joins.fetch("bcb11grupo"))
						.addJoin(Joins.fetch("bcb11item"))
						.addJoin(Joins.fetch("bcb11ent"))
						.addWhere(Criterions.eq(Fields.month("bcb10data"), dataFinal.getMonthValue()))
						.addWhere(Criterions.eq(Fields.year("bcb10data"), dataFinal.getYear()))
						.addWhere(Criterions.where(obterWherePadrao("Bcb10", "")))
						.getList(ColumnType.ENTITY)
	}
	
	Aag02 buscarUfEntidade(Long abe01id) {
		return getSession().createCriteria(Aag02)
						.addJoin(Joins.join("Aag0201", "aag0201uf = aag02id"))
						.addJoin(Joins.join("Abe0101", "abe0101municipio = aag0201id"))
						.addWhere(Criterions.eq("abe0101ent", abe01id))
						.addWhere(Criterions.eq("abe0101principal", Abe01.SIM))
						.get(ColumnType.ENTITY)
	}
	
	Abg01 buscarNcmItem(Long abm01id) {
		return getSession().createCriteria(Abg01.class)
						.addJoin(Joins.join("Abm0101", "abm0101ncm = abg01id"))
						.addWhere(Criterions.eq("abm0101item", abm01id))
						.get(ColumnType.ENTITY)
	}
	
	Abm10 buscarValoresItem(Long abm01id) {
		return getSession().createCriteria(Abm10.class)
						.addJoin(Joins.join("Abm0101", "abm0101valores = abm10id"))
						.addWhere(Criterions.eq("abm0101item", abm01id))
						.get(ColumnType.ENTITY)
	}

    Eaa0101 buscarEnderecoDocumento(Long eaa01id, Integer tipo) {
        def critEnd = tipo == 1 ? Criterions.eq("eaa0101.eaa0101saida", Eaa0101.SIM) : 
				  	  tipo == 2 ? Criterions.eq("eaa0101.eaa0101entrega", Eaa0101.SIM) : 
								  Criterions.eq("eaa0101.eaa0101principal", Eaa0101.SIM)
								  
		return getSession().createCriteria(Eaa0101.class)
                .addJoin(Joins.fetch("eaa0101.eaa0101municipio").left(true).alias("aag0201"))
                .addJoin(Joins.fetch("aag0201.aag0201uf").left(true).alias("aag02"))
                .addJoin(Joins.fetch("eaa0101.eaa0101pais").left(true).alias("aag01"))
                .addWhere(Criterions.eq("eaa0101.eaa0101doc", eaa01id))
                .addWhere(critEnd)
                .get(ColumnType.ENTITY)
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDcifQ==