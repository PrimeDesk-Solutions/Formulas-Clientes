package multitec.formulas.sfp;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.criteria.client.ClientCriterion;
import sam.core.criteria.ClientCriteriaConvert;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFP8507Dto;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh2101;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.formula.FormulaBase;

public class SeguroDesemprego extends FormulaBase {
	private SFP8507Dto sfp8507Dto;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SEGURO_DESEMPREGO;
	}

	@Override
	public void executar() {
		this.sfp8507Dto = get("sfp8507Dto");

		TextFile txt = new TextFile();
		
		DateTimeFormatter sdfData = DateTimeFormatter.ofPattern("ddMMyyyy");

		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);

		Aac10 aac10 = getVariaveis().getAac10();

		/**
		 * TIPO 00 - REGISTRO HEADER
		 */
		txt.print("00", 2);																																							//001 a 002
		txt.print(1, 1);																																							//003 a 003
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);																												//004 a 017
		txt.print("001", 3);																																						//018 a 020
		txt.print(StringUtils.space(280));																																							//021 a 300
		txt.newLine();

		def totalReq = 0;
		List<Abh80> abh80s = buscarAbh80PorSeguroDesemprego(sfp8507Dto.tiposAbh80, sfp8507Dto.critAbh80);
		if(abh80s != null && abh80s.size() > 0) {
			for(Abh80 abh80 : abh80s) {
				def mesAno = MDate.date();
				if(abh80.abh80dtResTrans != null) mesAno = abh80.abh80dtResTrans;

				mesAno = mesAno.minusMonths(1);
				def ultimoSal = calcularCAE(aac10.aac10id, abh80.abh80id, sfp8507Dto.abh20.abh20id, mesAno.getMonthValue(), mesAno.getYear());

				mesAno = mesAno.minusMonths(1);
				def penultimoSal = calcularCAE(aac10.aac10id, abh80.abh80id, sfp8507Dto.abh20.abh20id, mesAno.getMonthValue(), mesAno.getYear());

				mesAno = mesAno.minusMonths(1);
				def antepenultimoSal = calcularCAE(aac10.aac10id, abh80.abh80id, sfp8507Dto.abh20.abh20id, mesAno.getMonthValue(), mesAno.getYear());

				def avisoPrevInd = buscarAvisoPrevioIndenizado(abh80.abh80id);

				/**
				 * TIPO 01 - REGISTRO DE REQUERIMENTO
				 */
				txt.print("01", 2);																																					//001 a 002
				txt.print(StringUtils.extractNumbers(abh80.abh80cpf != null ? abh80.abh80cpf : ""), 11, '0', true);														//003 a 013
				txt.print(removerAcentos(abh80.abh80nome != null ? abh80.abh80nome : ""), 40);																			//014 a 053
				txt.print(removerAcentos(abh80.abh80endereco != null ? abh80.abh80endereco : ""), 40);																	//054 a 093
				txt.print(removerAcentos(abh80.abh80numero != null ? abh80.abh80numero : "" + " " + abh80.abh80complem != null ? abh80.abh80complem : ""), 16); //094 a 109
				txt.print(abh80.abh80cep != null ? abh80.abh80cep : "", 8, '0', true);																					//110 a 117
				def uf = "";
				if(abh80.abh80municipio != null) uf = buscarUF(abh80.abh80municipio.aag0201id); 
				txt.print(uf, 2);																																					//118 a 119
				txt.print(StringUtils.extractNumbers(abh80.abh80ddd1 != null ? abh80.abh80ddd1 : ""), 2, '0', true);														//120 a 121
				txt.print(StringUtils.extractNumbers(abh80.abh80fone1 != null ? abh80.abh80fone1 : ""), 8, '0', true);													//122 a 129
				txt.print(removerAcentos(abh80.abh80mae != null? abh80.abh80mae : ""), 40);																				//130 a 169
				txt.print(StringUtils.extractNumbers(abh80.abh80pis != null ? abh80.abh80pis : ""), 11, '0', true);														//170 a 180
				txt.print(StringUtils.extractNumbers(abh80.abh80ctpsNum != null ? abh80.abh80ctpsNum : ""), 8, '0', true);												//181 a 188
				txt.print(abh80.abh80ctpsSerie != null ? abh80.abh80ctpsSerie : "", 5);											     									//189 a 193
				txt.print(removerAcentos(abh80.abh80ctpsEe != null ? abh80.abh80ctpsEe : ""), 2);																			//194 a 195
				txt.print(StringUtils.extractNumbers(buscarCBO(abh80.abh80cargo.abh05id)), 6, '0', true);																	//196 a 201
				txt.print(sdfData.format(abh80.abh80dtAdmis), 8, '0', true);																									//202 a 209
				txt.print(abh80.abh80dtResTrans != null ? sdfData.format(abh80.abh80dtResTrans) : "", 8, '0', true);														//210 a 217
				txt.print(abh80.abh80sexo == 0 ? 1 : 2, 1);																													//218 a 218
				def grauInstrucao = "";
				if(abh80.abh80gi != null) grauInstrucao = buscarGI(abh80.abh80gi.aap06id);
				txt.print(grauInstrucao, 2, '0', true);																																//219 a 220
				txt.print(sdfData.format(abh80.abh80nascData), 8, '0', true);																									//221 a 228
				txt.print(abh80.abh80hs, 2);																																	//229 a 230
				txt.print(StringUtils.extractNumbers(nb.format(antepenultimoSal.setScale(2))), 10, '0', true);																		//231 a 240
				txt.print(StringUtils.extractNumbers(nb.format(penultimoSal.setScale(2))), 10, '0', true);																			//241 a 250
				txt.print(StringUtils.extractNumbers(nb.format(ultimoSal.setScale(2))), 10, '0', true);																				//251 a 260
				txt.print("00", 2);																																					//261 a 262
				txt.print("0", 1);																																					//263 a 263
				txt.print(avisoPrevInd, 1);																																			//264 a 264
				txt.print("000", 3);																																				//265 a 267
				txt.print("0000", 4);																																				//268 a 271
				txt.print("0", 1);																																					//272 a 272
				txt.print(StringUtils.space(28));																																					//273 a 300
				txt.newLine();
				totalReq++;
			}
		}

		/**
		 * TIPO 99 - REGISTRO TRAILLER
		 */
		txt.print("99", 2);																																							//001 a 002
		txt.print(totalReq, 5, '0', true);																																			//003 a 007
		txt.print(StringUtils.space(293));
		txt.newLine();
		put("txt", txt);
	}

	private String buscarGI(Long aap06id) {
		Query query = getSession().createQuery(" SELECT aap06codigo FROM aap06 WHERE aap06id = :aap06id ");

		query.setParameter("aap06id", aap06id);
		return query.getUniqueResult(ColumnType.STRING);
	}

	private String buscarCBO(Long abh05id) {
		Query query = getSession().createQuery(" SELECT aap03codigo FROM aap03 INNER JOIN Abh05 ON abh05cbo = aap03id ",
				" WHERE abh05id = :abh05id ");

		query.setParameter("abh05id", abh05id);
		String uniqueResult = query.getUniqueResult(ColumnType.STRING);
		return uniqueResult != null ? uniqueResult : "";
	}

	private String buscarUF(Long aag0201id) {
		Query query = getSession().createQuery(" SELECT aag02uf FROM aag02 INNER JOIN Aag0201 ON aag0201uf = aag02id ",
				" WHERE aag0201id = :aag0201id ");

		query.setParameter("aag0201id", aag0201id);
		return query.getUniqueResult(ColumnType.STRING);
	}

	private int buscarAvisoPrevioIndenizado(Long abh80id) {
		Query query = getSession().createQuery(" SELECT abh06avPrevGrrf FROM Abh06", 
				" INNER JOIN Fbd10 ON fbd10causa = abh06id ",
				" INNER JOIN Abh80 ON abh80id = fbd10trab ",
				" WHERE abh80id = :abh80id");

		query.setParameter("abh80id", abh80id);
		Integer uniqueResult = query.getUniqueResult(ColumnType.INTEGER);
		return uniqueResult != null && uniqueResult == 2 ? 2 : 1;
	}

	private BigDecimal calcularCAE(Long aac01id, Long abh80id, Long abh20id, int mes, int ano) {
		BigDecimal valor = BigDecimal.ZERO;

		List<TableMap> tms = buscarValoresDosEventosParaCalculoDoCAE(aac01id, abh80id, abh20id, mes, ano);
		if(tms != null && tms.size() > 0) {
			for(int i = 0; i < tms.size(); i++) {
				TableMap tm = tms.get(i);

				if(tm.getInteger("abh2101cvr") == Abh2101.CVR_SOMA_VLR) {
					valor = valor + tm.getBigDecimal("fba01011valor");
				}else if(tm.getInteger("abh2101cvr") == Abh2101.CVR_DIMINUI_VLR) {
					valor = valor - tm.getBigDecimal("fba01011valor");
				}
			}
		}
		return valor;
	}

	private List<TableMap> buscarValoresDosEventosParaCalculoDoCAE(Long aac01id, Long abh80id, Long abh20id, int mes, int ano) {
		String whereAbh80 = abh80id != null ? "AND fba0101trab = :abh80id " : "";

		Query query = getSession().createQuery("SELECT abh2101cvr, fba01011valor ",
				"FROM Fba01011 ",
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
				"INNER JOIN Fba01 ON fba01id = fba0101calculo ",
				"INNER JOIN Abh21 ON abh21id = fba01011eve ",
				"INNER JOIN Abh2101 ON abh2101evento = abh21id ",
				"INNER JOIN Abh20 ON abh20id = abh2101cae ",
				"WHERE fba01gc = :aac01id ",
				"AND DATE_PART('MONTH', fba0101dtCalc) = :mes ",
				"AND DATE_PART('YEAR', fba0101dtCalc) = :ano ",
				"AND abh20id = :abh20id ", 
				whereAbh80);

		query.setParameter("aac01id", aac01id);
		if(abh80id != null) query.setParameter("abh80id", abh80id);
		query.setParameter("abh20id", abh20id);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		return query.getListTableMap();		
	}

	private List<Abh80> buscarAbh80PorSeguroDesemprego(Set<Integer> tiposAbh80, ClientCriterion clientCriterion) {
		return getSession().createCriteria(Abh80.class)
				.addWhere(Criterions.in("abh80tipo", tiposAbh80))
				.addWhere(ClientCriteriaConvert.convertCriterion(clientCriterion))
				.addWhere(getSamWhere()
						.getCritPadrao(Abh80.class)).getList(ColumnType.ENTITY);

	}

	private String removerAcentos(String string) {
		return StringUtils.unaccented(string);
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTgifQ==