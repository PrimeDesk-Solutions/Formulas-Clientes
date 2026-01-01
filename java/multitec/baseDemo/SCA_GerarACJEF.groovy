package multitec.baseDemo;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.multiorm.ColumnType;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate;
import sam.core.variaveis.Variaveis;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh13;
import sam.model.entities.ab.Abh1301;
import sam.model.entities.ab.Abh21;
import sam.model.entities.fc.Fca10;
import sam.server.samdev.formula.FormulaBase;

public class SCA_GerarACJEFOld extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCA_GERAR_ARQUIVOS;
	}

	@Override
	public void executar() {
		TextFile txt = new TextFile();
		List<TableMap> fca10s = get("fca10s");
		LocalDate dtInicial = get("dataInicial");
		LocalDate dtFinal = get("dataFinal");

		DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("ddMMyyyy");
		DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("HHmm");
		Abh21 abh21Eve0 = buscarAbh21PeloCodigo("9999");
		Abh21 abh21Eve1 = null;
		Abh21 abh21Eve2 = null;
		Abh21 abh21Eve3 = null;
		boolean isEve0Diu = true;
		boolean isEve1Diu = true;
		boolean isEve2Diu = true;
		boolean isEve3Diu = true;

		Aac10 aac10 = Variaveis.obter().getAac10();
		int sequencial = 1;
		
		//TIPO 1 - CABEÇALHO
		txt.print(sequencial, 9, '0', true);																												//001 a 009
		txt.print("1", 1);																																	//010 a 010
		txt.print(aac10.getAac10ti() == 0 ? "1" : "2", 1);																									//011 a 011
		txt.print(StringUtils.extractNumbers(aac10.getAac10ni()), 14, '0', true);																			//012 a 025
		txt.print(StringUtils.extractNumbers(/**aac10.getAac10cei()**/""), 12, '0', true);																	//026 a 037 TODO
		txt.print(aac10.getAac10rs(), 150);																													//038 a 187
		txt.print(dtInicial.format(dtfData), 8, '0', true);																									//188 a 195
		txt.print(dtFinal.format(dtfData), 8, '0', true);																									//196 a 203
		txt.print(MDate.date().format(dtfData), 8, '0', true);																							//204 a 211
		txt.print(MDate.time().format(dtfHora), 4, '0', true);																							//212 a 215
		txt.newLine();
		sequencial++;
		
		Set<Long> abh80ids = fca10s.stream().map({tm -> (Long)tm.get("abh80id")}).distinct().collect(Collectors.toSet());
		List<TableMap> mapAbh11s = buscarHorariosDistintosPorIdsDosTrabalhadores(abh80ids);
		if(mapAbh11s != null && mapAbh11s.size() > 0) {
			for(TableMap tm : mapAbh11s) {
				//TIPO 2 - DETALHE
				txt.print(sequencial, 9, '0', true);																										//001 a 009
				txt.print("2", 1);																															//010 a 010
				txt.print(tm.get("abh11codigo"), 4, '0', true);																								//011 a 014
				txt.print(tm.get("abh11horaE") != null ? ((LocalTime) tm.get("abh11horaE")).format(dtfHora) : "0000", 4, '0', true);						//015 a 018
				txt.print(tm.get("abh11intervE") != null ? ((LocalTime) tm.get("abh11intervE")).format(dtfHora) : "0000", 4, '0', true);					//019 a 022
				txt.print(tm.get("abh11intervS") != null ? ((LocalTime) tm.get("abh11intervS")).format(dtfHora) : "0000", 4, '0', true);					//023 a 026
				txt.print(tm.get("abh11horaS") != null ? ((LocalTime) tm.get("abh11horaS")).format(dtfHora) : "0000", 4, '0', true);						//027 a 030
				txt.newLine();
				sequencial++;
			}
		}
		
		for(TableMap tm : fca10s) {
			Abh1301 abh1301 = buscarMarcacaoPorChaveUnica(tm.get("abh13id"), tm.get("fca10data"));
			if(abh1301 == null) throw new ValidacaoException("O dia " + DateUtils.formatDate(tm.get("fca10data")) + " não foi encontrado no mapeamento de horário " + tm.get("abh13codigo") + ".");
			
			Long fca10id = tm.get("fca10id");
			String abh80pis = tm.get("abh80pis") != null ? StringUtils.extractNumbers(tm.get("abh80pis")) : "";
			LocalTime fca1001hrBase = buscarPrimeiraMarcacaoDeEntradaPorIdDoPonto(fca10id);
			int fca10hpComp = (Integer)tm.get("fca10hpComp");
			int fca10hnComp = (Integer)tm.get("fca10hnComp");
			String saldoDeHoras = fca10hpComp > 0 ? formatarQtdeMin(fca10hpComp) : fca10hnComp > 0 ? formatarQtdeMin(fca10hnComp) : "0000";
			
			//TIPO 3 - DETALHE
			txt.print(sequencial, 9, '0', true);																											//001 a 009
			txt.print("3", 1);																																//010 a 010
			txt.print(abh80pis.length() > 12 ? abh80pis.substring(0, 12) : abh80pis, 12, '0', true);														//011 a 022
			txt.print(((LocalDate) tm.get("fca10data")).format(dtfData), 8, '0', true);																		//023 a 030
			txt.print(fca1001hrBase != null ? fca1001hrBase.format(dtfHora) : '0', 4, '0', true);																							//031 a 034
			txt.print(abh1301.getAbh1301horario().getAbh11codigo(), 4, '0', true);																			//035 a 038
			txt.print(formatarQtdeMin(tm.get("fca10horDiu")), 4, '0', true);																				//039 a 042
			txt.print(formatarQtdeMin(tm.get("fca10horNot")), 4, '0', true);																				//043 a 046
			txt.print(somarHorasExtras(fca10id, abh21Eve0), 4, '0', true);																					//047 a 050
			txt.print(StringUtils.extractNumbers(calcularPercentualDoEvento(abh21Eve0)), 4, '0', true);														//051 a 054
			txt.print(abh21Eve0 == null ? " " : isEve0Diu ? "D" : "N");																						//055 a 055
			txt.print(somarHorasExtras(fca10id, abh21Eve1), 4, '0', true);																					//056 a 059
			txt.print(StringUtils.extractNumbers(calcularPercentualDoEvento(abh21Eve1)), 4, '0', true);														//060 a 063
			txt.print(abh21Eve1 == null ? " " : isEve1Diu ? "D" : "N");																						//064 a 064
			txt.print(somarHorasExtras(fca10id, abh21Eve2), 4, '0', true);																					//065 a 068
			txt.print(StringUtils.extractNumbers(calcularPercentualDoEvento(abh21Eve2)), 4, '0', true);														//069 a 072
			txt.print(abh21Eve2 == null ? " " : isEve2Diu ? "D" : "N");																						//073 a 073
			txt.print(somarHorasExtras(fca10id, abh21Eve3), 4, '0', true);																					//074 a 077
			txt.print(StringUtils.extractNumbers(calcularPercentualDoEvento(abh21Eve3)), 4, '0', true);														//078 a 081
			txt.print(abh21Eve3 == null ? " " : isEve3Diu ? "D" : "N");																						//082 a 082
			txt.print(formatarQtdeMin(tm.get("fca10horFalt")), 4, '0', true);																			   	//083 a 086
			txt.print(fca10hpComp > 0 ? 1 : fca10hnComp > 0 ? 2 : 0, 1);																					//087 a 087
			txt.print(saldoDeHoras, 4, '0', true);																											//088 a 091
			txt.newLine();
			sequencial++;
		}

		//TIPO 9 - TRAILLER
		txt.print(sequencial, 9, '0', true);																												//001 a 009
		txt.print("9", 1);
		
		values.put("txt", txt);
	}

	private Abh21 buscarAbh21PeloCodigo(String abh21codigo) {
		return getAcessoAoBanco().buscarRegistroUnico("SELECT abh21id, abh21codigo, abh21fator FROM Abh21 WHERE abh21codigo = :abh21codigo " + getSamWhere().getWherePadrao("AND", Abh21.class), criarParametroSql("abh21codigo", abh21codigo));
	}
	
	/**
	 * Soma as horas extras dos eventos do ponto.
	 * @param fca10id Long Id do ponto
	 * @param abh21 Abh21 Evento base
	 * @return String
	 */
	private String somarHorasExtras(Long fca10id, Abh21 abh21) {
		if(abh21 == null) return "0000";
		Fca10 fca10 = buscarEventosDoPontoPorId(fca10id);
		int horasExtras = 0;

		horasExtras += fca10.getFca10eve00() == null ? 0 : fca10.getFca10eve00().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha00() : 0;
		horasExtras += fca10.getFca10eve01() == null ? 0 : fca10.getFca10eve01().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha01() : 0;
		horasExtras += fca10.getFca10eve02() == null ? 0 : fca10.getFca10eve02().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha02() : 0;
		horasExtras += fca10.getFca10eve03() == null ? 0 : fca10.getFca10eve03().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha03() : 0;
		horasExtras += fca10.getFca10eve04() == null ? 0 : fca10.getFca10eve04().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha04() : 0;
		horasExtras += fca10.getFca10eve05() == null ? 0 : fca10.getFca10eve05().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha05() : 0;
		horasExtras += fca10.getFca10eve06() == null ? 0 : fca10.getFca10eve06().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha06() : 0;
		horasExtras += fca10.getFca10eve07() == null ? 0 : fca10.getFca10eve07().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha07() : 0;
		horasExtras += fca10.getFca10eve08() == null ? 0 : fca10.getFca10eve08().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha08() : 0;
		horasExtras += fca10.getFca10eve09() == null ? 0 : fca10.getFca10eve09().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha09() : 0;
		horasExtras += fca10.getFca10eve10() == null ? 0 : fca10.getFca10eve10().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha10() : 0;
		horasExtras += fca10.getFca10eve11() == null ? 0 : fca10.getFca10eve11().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha11() : 0;
		horasExtras += fca10.getFca10eve12() == null ? 0 : fca10.getFca10eve12().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha12() : 0;
		horasExtras += fca10.getFca10eve13() == null ? 0 : fca10.getFca10eve13().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha13() : 0;
		horasExtras += fca10.getFca10eve14() == null ? 0 : fca10.getFca10eve14().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha14() : 0;
		horasExtras += fca10.getFca10eve15() == null ? 0 : fca10.getFca10eve15().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha15() : 0;
		horasExtras += fca10.getFca10eve16() == null ? 0 : fca10.getFca10eve16().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha16() : 0;
		horasExtras += fca10.getFca10eve17() == null ? 0 : fca10.getFca10eve17().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha17() : 0;
		horasExtras += fca10.getFca10eve18() == null ? 0 : fca10.getFca10eve18().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha18() : 0;
		horasExtras += fca10.getFca10eve19() == null ? 0 : fca10.getFca10eve19().getAbh21codigo().compareTo(abh21.getAbh21codigo()) == 0 ? fca10.getFca10folha19() : 0;
		return formatarQtdeMin(horasExtras);
	}
	
	/**
	 * Formata a quantidade de minutos para imnprimir no txt.
	 * @param qtdeMin int Quantidade de minutos
	 * @return String
	 */
	private String formatarQtdeMin(int qtdeMin) {
		DecimalFormat df = new DecimalFormat("00");
		return df.format(qtdeMin/60) + df.format(qtdeMin%60);
	}
	
	/**
	 * Calcula o percentual do evento.
	 * @param abh21 Abh21 Evento
	 * @return String
	 */
	private String calcularPercentualDoEvento(Abh21 abh21) {
		if(abh21 == null) return "0000";
		
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setMaximumFractionDigits(2);
		nb.setGroupingUsed(true);
		return nb.format((abh21.getAbh21fator().subtract(new BigDecimal(1))).multiply(new BigDecimal(100)));
	}
	
	/**
	 * Retorna uma lista de horários distintos a partir dos ids dos trabalhadores.
	 * @param abh80ids Set&lt;Long&gt; Ids dos trabalhadores
	 * @return List&lt;TableMap&gt;
	 */
	public List<TableMap> buscarHorariosDistintosPorIdsDosTrabalhadores(Set<Long> abh80ids) {
		return getSession().createQuery("SELECT DISTINCT abh11id, abh11codigo, abh11nome, abh11horaE, abh11horaS, abh11intervE, abh11intervS, abh11jorNot, abh11jorLiq ",
								   "FROM Abh1301 ",
								   "INNER JOIN Abh13 ON abh13id = abh1301maphor ",
								   "INNER JOIN Abh11 ON abh11id = abh1301horario ",
								   "WHERE abh13id IN (SELECT abh80mapHor FROM Abh80 WHERE abh80id IN (:abh80ids)) ",
								   getSamWhere().getWherePadrao("AND", Abh13.class) +
								   " ORDER BY abh11codigo")
					  .setParameter("abh80ids", abh80ids)
					  .getListTableMap();
	}
	
	/**
	 * Retorna uma marcação do mapeamento de horário a partir da chave única.
	 * @param abh13id Long Id do mapeamento de horário
	 * @param abh1301data LocalDate Data
	 * @return Abh1301
	 */
	public Abh1301 buscarMarcacaoPorChaveUnica(Long abh13id, LocalDate abh1301data) {
		return getSession().createQuery("SELECT abh1301.abh1301id, abh1301.abh1301data, abh1301.abh1301horario, abh1301.abh1301tpDia, abh13.abh13id, abh11.abh11id, abh11.abh11horaE, abh11.abh11horaS, ",
								   "abh11.abh11intervE, abh11.abh11intervS, abh11.abh11jorLiq, abh11.abh11pa, abh11.abh11varPos, abh11.abh11varNeg, abh11.abh11complem0, abh11.abh11complem1, ",
								   "abh11.abh11complem2, abh11.abh11complem3, abh11.abh11complem4, abh11.abh11jorNot, abh11.abh11codigo, abh12.abh12id ",
								   "FROM Abh1301 AS abh1301 ",
						   		   "INNER JOIN PART abh1301.abh1301maphor AS abh13 ",
						   		   "INNER JOIN PART abh1301.abh1301horario AS abh11 ",
						   		   "LEFT JOIN PART abh11.abh11divhe AS abh12 ",
						   		   "WHERE abh13.abh13id = :abh13id AND abh1301.abh1301data = :abh1301data ",
						   		   getSamWhere().getWherePadrao("AND", Abh13.class))
					  .setParameter("abh13id", abh13id)
					  .setParameter("abh1301data", abh1301data)
					  .setMaxResult(1)
					  .getUniqueResult(ColumnType.ENTITY);
	}
	
	/**
	 * Retorna a primeira marcação de entrada a partir do id do ponto.
	 * @param fca10id Long Id do ponto
	 * @return LocalTime
	 */
	public LocalTime buscarPrimeiraMarcacaoDeEntradaPorIdDoPonto(Long fca10id) {
		return getSession().createQuery("SELECT fca1001hrBase FROM Fca1001 ",
								   "INNER JOIN Fca10 ON fca10id = fca1001ponto ",
								   "WHERE fca10id = :fca10id AND fca1001classificacao = 0 ",
								   "ORDER BY fca1001data, fca1001hrBase")
					  .setParameter("fca10id", fca10id)
					  .setMaxResult(1)
					  .getUniqueResult(ColumnType.TIME);
	}
	
	/**
	 * Retorna um ponto carregado apenas com os dados de transferência dos eventos a partir do id.
	 * @param fca10id Long Id do ponto
	 * @return Fca10
	 */
	public Fca10 buscarEventosDoPontoPorId(Long fca10id) {
		return getAcessoAoBanco().buscarRegistroUnicoById("Fca10", fca10id);
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTkifQ==