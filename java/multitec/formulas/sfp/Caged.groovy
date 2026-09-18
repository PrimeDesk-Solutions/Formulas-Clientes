package multitec.formulas.sfp;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.criteria.client.ClientCriterion;
import sam.core.criteria.ClientCriteriaConvert;
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFP8502Dto;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac1001;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;

public class Caged extends FormulaBase {
	private SFP8502Dto sfp8502Dto;
	private int sequencial = 1;
	private int totalMovim = 0;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CAGED;
	}

	@Override
	public void executar() {
		this.sfp8502Dto = get("sfp8502Dto");

		TextFile txt = new TextFile();

		gerarTipoA(txt, sfp8502Dto);
		txt = substituirVariavel(txt, String.format("%05d", totalMovim));

		put("txt", txt);
	}

	/**
	 * TIPO A
	 */
	private void gerarTipoA(TextFile txt, SFP8502Dto sfp8502Dto) {
		List<TableMap> tmAac10s = buscarDadosDasEmpresasParaGeracaoDoCAGED(sfp8502Dto.getClientCriterionAac10());
		if(tmAac10s != null && tmAac10s.size() > 0) {
			int totalEstab = tmAac10s.size();

			txt.print(ajustarCampo("A", 1));																																					//001 a 001
			txt.print(ajustarCampo("L2009", 5));																																				//002 a 006
			txt.print(StringUtils.space(3));																																					//007 a 009
			txt.print(ajustarCampo(sfp8502Dto.dataRef.format(DateTimeFormatter.ofPattern("MMyyyy")), 6));																					//010 a 015
			txt.print(ajustarCampo(sfp8502Dto.respCodAlteracao, 1));																														//016 a 016
			txt.print(ajustarCampo(sequencial, 5, '0', true));																																	//017 a 021
			txt.print(ajustarCampo(sfp8502Dto.respTI, 1));																																	//022 a 022
			txt.print(ajustarCampo(StringUtils.extractNumbers(sfp8502Dto.respNI), 14));																									//023 a 036
			txt.print(ajustarCampo(sfp8502Dto.respNome, 35));																																//037 a 071
			txt.print(ajustarCampo(sfp8502Dto.respEndereco, 40));																															//072 a 111
			txt.print(ajustarCampo(sfp8502Dto.respCep, 8));																																//112 a 119
			txt.print(ajustarCampo(sfp8502Dto.respUF, 2));																																	//120 a 121
			txt.print(ajustarCampo(sfp8502Dto.respDDD, 4));																																//122 a 125
			txt.print(ajustarCampo(sfp8502Dto.respFone, 8));																																//126 a 133
			txt.print(ajustarCampo(sfp8502Dto.respRamal, 5));																																//134 a 138
			txt.print(ajustarCampo(totalEstab, 5, '0', true));																																	//139 a 143
			txt.print("qtdmv");																																									//144 a 148
			txt.print(StringUtils.space(92));																																					//149 a 240
			txt.newLine();
			sequencial++;

			for(int i = 0; i < tmAac10s.size(); i++) {
				Long aac01id = buscarIdDoGrupoCentralizadorPorIdEmpresa(tmAac10s.get(i).getLong("aac10id"));
				tmAac10s.get(i).put("aac01id", aac01id);

				gerarTipoB(txt, sfp8502Dto, tmAac10s.get(i));
			}
		}
	}

	/**
	 * TIPO B
	 */
	private void gerarTipoB(TextFile txt, SFP8502Dto sfp8502Dto, TableMap tmAac10) {
		def dataRef = sfp8502Dto.getDataRef().withDayOfMonth(1);

		txt.print(ajustarCampo("B", 1));																																						//001 a 001
		txt.print(ajustarCampo("1", 1));																																						//002 a 002
		txt.print(ajustarCampo(StringUtils.extractNumbers(tmAac10.getString("aac10ni")), 14, '0', true));																						//003 a 016
		txt.print(ajustarCampo(sequencial, 5, '0', true));																																		//017 a 021
		txt.print(ajustarCampo(sfp8502Dto.empPrimDeclaracao ? 1 : 2, 1));																													//022 a 022
		txt.print(ajustarCampo(sfp8502Dto.empAlteracao, 1));																																//023 a 023
		txt.print(ajustarCampo(tmAac10.getString("aac10cep"), 8));																																//024 a 031
		txt.print(StringUtils.space(5));																																						//032 a 036
		txt.print(ajustarCampo(tmAac10.getString("aac10rs"), 40));																																//037 a 076
		txt.print(ajustarCampo(tmAac10.getString("aac10endereco"), 40));																														//077 a 116
		txt.print(ajustarCampo(tmAac10.getString("aac10bairro"), 20));																															//117 a 136
		txt.print(ajustarCampo(tmAac10.getString("aag02uf"), 2));																																//137 a 138
		txt.print(ajustarCampo(buscarQtdeDeTrabalhadoresParaCAGED(tmAac10.getLong("aac01id"), sfp8502Dto.empConsTrabTerceiros, dataRef.minusMonths(1)), 5, '0', true));						//139 a 143
		txt.print(ajustarCampo(sfp8502Dto.getEmpPorte(), 1));																																	//144 a 144
		txt.print(ajustarCampo(tmAac10.getString("aac10cnae"), 7));																																//145 a 151
		txt.print(ajustarCampo(tmAac10.getString("aac10dddFone"), 4));																															//152 a 155
		txt.print(ajustarCampo(tmAac10.getString("aac10fone"), 8));																																//156 a 163
		txt.print(ajustarCampo(tmAac10.getString("aac10email"), 50));																															//164 a 213
		txt.print(StringUtils.space(27));																																						//214 a 240
		txt.newLine();
		sequencial++;

		List<TableMap> tmAbh80s = buscarDadosDosTrabalhadoresParaCAGED(tmAac10.getLong("aac01id"), sfp8502Dto.empConsTrabTerceiros, dataRef.getMonthValue(), dataRef.getYear(), sfp8502Dto.tpArquivo == 0);
		if(tmAbh80s != null  && tmAbh80s.size() > 0) {
			for(int i = 0; i < tmAbh80s.size(); i++) {
				def aap16caged = tmAbh80s.get(i).getString("aap16caged");
				def abh80dtAdmis = tmAbh80s.get(i).getDate("abh80dtAdmis");
				def abh80dtResTrans = tmAbh80s.get(i).getDate("abh80dtResTrans");

				//Trabalhadores admitidos.
				if(abh80dtAdmis.getMonthValue() == dataRef.getMonthValue() && abh80dtAdmis.getYear() == dataRef.getYear()) {
					gerarTipoC(txt, sfp8502Dto, tmAac10.getString("aac10ni"), tmAbh80s.get(i), abh80dtAdmis, aap16caged, dataRef);
					sequencial++;
					totalMovim++;
				}

				//Trabalhadores transferidos.
				if(abh80dtResTrans != null && aap16caged != null && aap16caged == "70") {
					if((abh80dtResTrans.getMonthValue() != abh80dtAdmis.getMonthValue() || abh80dtResTrans.getYear() != abh80dtAdmis.getYear()) && abh80dtResTrans.getMonthValue() == dataRef.getMonthValue() && abh80dtResTrans.getYear() == dataRef.getYear()) {
						gerarTipoC(txt, sfp8502Dto, tmAac10.getString("aac10ni"), tmAbh80s.get(i), abh80dtResTrans, aap16caged, dataRef);
						sequencial++;
						totalMovim++;
					}
				}

				//Trabalhadores demitidos.
				if(tmAbh80s.get(i).getInteger("abh80sit") == Abh80.SIT_DEMITIDO) {
					Fbd10 fbd10 = buscarRescisaoDoTrabalhadorParaCAGED(tmAbh80s.get(i).getLong("abh80id"));
					if(fbd10 != null && fbd10.fbd10dtRes.getMonthValue() == dataRef.getMonthValue() && fbd10.fbd10dtRes.getYear() == dataRef.getYear()) {
						gerarTipoC(txt, sfp8502Dto, tmAac10.getString("aac10ni"), tmAbh80s.get(i), abh80dtAdmis, fbd10.fbd10causa.abh06caged, dataRef);
						sequencial++;
						totalMovim++;
					}
				}

				if(sfp8502Dto.tpArquivo == 1) {
					Abh80 abh80 = getSession().createCriteria(Abh80.class).addWhere(Criterions.eq("abh80id", tmAbh80s.get(i).getLong("abh80id"))).get(ColumnType.ENTITY);
					abh80.abh80segDesemp = Abh80.SEGDESEMP_ENVIADO;
					getSession().persist(abh80);
				}
			}
		}
	}

	/**
	 * TIPO C
	 */
	private void gerarTipoC(TextFile txt, SFP8502Dto sfp8502Dto, String aac10ni, TableMap tmAbh80, LocalDate dataMovim, String codMovim, LocalDate dataRef) {
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);

		//Salário mensal.
		def salario = null;
		if(tmAbh80.getString("aap18codigo") == "5") { //Salário mensal
			salario = tmAbh80.getBigDecimal("abh80salario");
		}else {
			salario = tmAbh80.getBigDecimal("abh80salario") * tmAbh80.getInteger("abh80hs") * 5;
		}

		//Dia do desligamento (se o tipo do movimento for desligamento).
		int diaDesliga = 0;
		if(tmAbh80.getDate("abh80dtResTrans") != null) {
			if(tmAbh80.getDate("abh80dtResTrans").getMonthValue() == dataRef.getMonthValue()) diaDesliga = tmAbh80.getDate("abh80dtResTrans").getDayOfMonth();
		}

		//Dados de deficiência do trabalhador.
		List<Integer> deficiente = new ArrayList<Integer>();
		if(tmAbh80.getInteger("abh80defFisico") == 1) deficiente.add(1);
		if(tmAbh80.getInteger("abh80defVisual") == 1) deficiente.add(3);
		if(tmAbh80.getInteger("abh80defAuditivo") == 1) deficiente.add(2);
		if(tmAbh80.getInteger("abh80defMental") == 1) deficiente.add(4);
		if(tmAbh80.getInteger("abh80defIntelecto") == 1) deficiente.add(4);
		if(tmAbh80.getInteger("abh80defReabil") == 1) deficiente.add(6);
		if(deficiente.size() == 0) deficiente.add(0);

		txt.print(ajustarCampo("C", 1));																																						//001 a 001
		txt.print(ajustarCampo("1", 1));																																						//002 a 002
		txt.print(ajustarCampo(StringUtils.extractNumbers(aac10ni), 14, '0', true));																											//003 a 016
		txt.print(ajustarCampo(sequencial, 5, '0', true));																																		//017 a 021
		txt.print(ajustarCampo(tmAbh80.getString("abh80pis"), 11, '0', true));																													//022 a 032
		txt.print(ajustarCampo(tmAbh80.getInteger("abh80sexo") == 0 ? 1 : 2, 1));								 																				//033 a 033
		txt.print(ajustarCampo(tmAbh80.getDate("abh80nascData") != null ? tmAbh80.getDate("abh80nascData").format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "", 8));							//034 a 041
		txt.print(ajustarCampo(tmAbh80.getString("aap06caged"), 2));																															//042 a 043
		txt.print(StringUtils.space(4));																																						//044 a 047
		txt.print(ajustarCampo(StringUtils.extractNumbers(nb.format(salario)), 8, '0', true));																						//048 a 055
		txt.print(ajustarCampo(tmAbh80.getInteger("abh80hs"), 2));																																//056 a 057
		txt.print(ajustarCampo(dataMovim.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8));																									//058 a 065
		txt.print(ajustarCampo(codMovim, 2));																																					//066 a 067
		txt.print(ajustarCampo(diaDesliga == 0 ? "" : diaDesliga, 2));																															//068 a 069
		txt.print(ajustarCampo(tmAbh80.getString("abh80nome"), 40));																															//070 a 109
		txt.print(ajustarCampo(tmAbh80.getString("abh80ctpsNum"), 8));																															//110 a 117
		txt.print(ajustarCampo(tmAbh80.getString("abh80ctpsSerie"), 4));																														//118 a 121
		txt.print(StringUtils.space(7));																																						//122 a 128
		txt.print(ajustarCampo(tmAbh80.getString("aap07caged"), 1));																															//129 a 129
		txt.print(ajustarCampo(deficiente.get(0) == 0 ? 2 : 1 , 1));				    																										//130 a 130
		txt.print(ajustarCampo(tmAbh80.getString("aap03codigo"), 6));																															//131 a 136
		txt.print(ajustarCampo(tmAbh80.getInteger("abh80aprendiz") == 1 ? 1 : 2, 1));																											//137 a 137
		txt.print(ajustarCampo(tmAbh80.getString("abh80ctpsEe"), 2));																															//138 a 139
		txt.print(ajustarCampo(deficiente.size() > 1 ? "5" : deficiente.get(0), 1));																											//140 a 140
		txt.print(ajustarCampo(tmAbh80.getString("abh80cpf"), 11));																																//141 a 151
		txt.print(ajustarCampo(tmAbh80.getString("abh80cep"), 8));																																//152 a 159
		txt.print(ajustarCampo(tmAbh80.getString("abh80motExame"), 17));																														//160 a 176
		txt.print(ajustarCampo(tmAbh80.getDate("abh80motDtExame") != null ? tmAbh80.getDate("abh80motDtExame").format(DateTimeFormatter.ofPattern("ddMMyyyy")) : null, 8));						//177 a 184
		txt.print(ajustarCampo(tmAbh80.getString("abh80motLab") != null ? StringUtils.extractNumbers(tmAbh80.getString("abh80motLab")) : null, 14, '0', true));									//185 a 198
		txt.print(ajustarCampo(tmAbh80.getString("abh80motUF"), 2));																															//199 a 200
		txt.print(ajustarCampo(tmAbh80.getString("abh80motCRM") != null ? StringUtils.extractNumbers(tmAbh80.getString("abh80motCRM")) : null, 10));											//201 a 210
		txt.print(ajustarCampo("2", 1));																																						//211 a 211
		txt.print(ajustarCampo("2", 1));																																						//212 a 212
		txt.print(ajustarCampo(tmAbh80.getInteger("abh80intermitente") == 0 ? "2" : "1", 1));																									//213 a 213
		txt.print(StringUtils.space(27));																																						//214 a 240
		txt.newLine();
	}

	private String ajustarCampo(Object string, int tamanho) {
		return StringUtils.ajustString(string, tamanho);
	}

	private String ajustarCampo(Object string, int tamanho, String character, boolean concatAEsquerda) {
		return StringUtils.ajustString(string, tamanho, character, concatAEsquerda);
	}

	private TextFile substituirVariavel(TextFile texto, String totalRegistros) {
		File file = texto.getPath().toFile();
		List<String> lines = FileUtils.readLines(file, "UTF-8");

		String novaLinha = new String();
		TextFile txtNovo = new TextFile();

		for(linha in lines) {
			int pos = linha.indexOf("qtdmv", -1);
			if(pos >= 0) { //Se pos == -1 significa que a linha não é correspondentes aos dados do documento
				novaLinha = StringUtils.concat(linha.substring(0, pos), "|", totalRegistros, "|", linha.substring(pos+8));
				txtNovo.print(novaLinha);
				txtNovo.newLine();
			}else {
				txtNovo.print(linha);
				txtNovo.newLine();
			}
		}


		return txtNovo;
	}


	private List<TableMap> buscarDadosDasEmpresasParaGeracaoDoCAGED(ClientCriterion clientCriterionAac10) {
		return getSession().createCriteria(Aac10.class)
				.addFields("aac10id, aac10ni, aac10rs, aac10endereco, aac10bairro, aac10cep, aag02uf, aac10cnae, aac10dddFone, aac10fone, aac10email")
				.addJoin(Joins.join("aac10municipio").left(true))
				.addJoin(Joins.join("aac10municipio.aag0201uf").left(true))
				.addWhere(ClientCriteriaConvert.convertCriterion(clientCriterionAac10))
				.addWhere(getSamWhere().getCritPadrao(Aac10.class))
				.setOrder("aac10codigo")
				.getListTableMap();
	}

	private Long buscarIdDoGrupoCentralizadorPorIdEmpresa(Long aac10id) {
		return getSession().createCriteria(Aac1001.class)
				.addFields("aac1001gc")
				.addWhere(Criterions.eq("aac1001empresa", aac10id))
				.addWhere(Criterions.eq("aac1001tabela", "FB"))
				.get(ColumnType.LONG);
	}

	private Integer buscarQtdeDeTrabalhadoresParaCAGED(Long aac01id, boolean consideraTerceiros, LocalDate dataRef) {
		String whereTerceiros = consideraTerceiros ? "AND abh80tipo IN (0,3) " : "AND abh80tipo = 0 ";

		Integer qtde = getSession().createQuery(
				"SELECT COUNT(*) FROM Abh80 ",
				"WHERE abh80gc = :aac01id AND abh80dtAdmis <= :dataRef AND (abh80dtResTrans IS NULL OR (abh80dtResTrans IS NOT NULL AND abh80dtResTrans > :dataRef))",
				whereTerceiros)
				.setParameter("aac01id", aac01id)
				.setParameter("dataRef", dataRef)
				.getUniqueResult(ColumnType.INTEGER);

		return qtde != null ? qtde : 0;
	}

	private List<TableMap> buscarDadosDosTrabalhadoresParaCAGED(Long aac01id, boolean consideraTerceiros, int mes, int ano, boolean isCagedMensal) {
		String whereTerceiros = consideraTerceiros ? "AND abh80tipo IN (0, 3) " : "AND abh80tipo = 0 ";
		String whereCaged = isCagedMensal ? "AND ((DATE_PART('MONTH', abh80dtAdmis) = :mes AND DATE_PART('YEAR', abh80dtAdmis) = :ano AND abh80segDesemp = 0) OR (abh80dtResTrans IS NOT NULL AND DATE_PART('MONTH', abh80dtResTrans) = :mes AND DATE_PART('YEAR', abh80dtResTrans) = :ano AND abh80segDesemp IN (0, 2))) " :
				"AND (DATE_PART('MONTH', abh80dtAdmis) = :mes AND DATE_PART('YEAR', abh80dtAdmis) = :ano AND abh80segDesemp = 1) ";

		return getSession().createQuery(
				"SELECT abh80id, aap16caged, abh80dtResTrans, abh80sit, abh80pis, abh80sexo, abh80nascData, aap06caged, aap18codigo, abh80salario, abh80hs, abh80nome, aap07caged, abh80defFisico, abh80defVisual, abh80defAuditivo, ",
				"abh80defMental, abh80defIntelecto, abh80defReabil, aap03codigo, abh80cpf, abh80cep, abh80motExame, abh80dtAdmis, abh80aprendiz, abh80ctpsNum, abh80ctpsSerie, abh80ctpsEe, abh80motExame, abh80motDtExame, abh80motLab, ",
				"abh80motUF, abh80motCRM, abh80intermitente ",
				"FROM Abh80 ",
				"INNER JOIN Abh05 ON abh05id = abh80cargo ",
				"LEFT JOIN Aap03 ON aap03id = abh05cbo ",
				"LEFT JOIN Aap06 ON aap06id = abh80gi ",
				"LEFT JOIN Aap07 ON aap07id = abh80rc ",
				"LEFT JOIN Aap16 ON aap16id = abh80tpAdmis ",
				"LEFT JOIN Aap18 ON aap18id = abh80unidPagto ",
				"WHERE abh80gc = :aac01id ", whereTerceiros, whereCaged,
				" ORDER BY abh80codigo")
				.setParameters("aac01id", aac01id, "consideraTerceiros", consideraTerceiros, "mes", mes, "ano", ano)
				.getListTableMap();
	}

	private Fbd10 buscarRescisaoDoTrabalhadorParaCAGED(Long abh80id) {
		return getSession().createCriteria(Fbd10.class)
				.addJoin(Joins.fetch("fbd10causa"))
				.addWhere(Criterions.eq("fbd10trab", abh80id))
				.addWhere(getSamWhere().getCritPadrao(Fbd10.class))
				.setOrder("fbd10id DESC")
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTMifQ==