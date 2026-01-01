package multitec.formulas.sgt.ecd;

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat;
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

import org.apache.commons.io.FileUtils

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aac11
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aag0201
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abc10
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe06
import sam.model.entities.eb.Eba30
import sam.model.entities.eb.Ebb02
import sam.model.entities.eb.Ebb03
import sam.model.entities.eb.Ebb05
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro

public class Leiaute9 extends FormulaBase {

	private static NumberFormat format;

	@Override
	public void executar() {
		LocalDate dtInicial = get("dtInicial");
		LocalDate dtFinal = get("dtFinal");
		Integer numLivro = getInteger("livroDiario");

		String versaoLeiaute = "9.00";
		
		Aac10 aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);

		if(aac10.aac10municipio == null) throw new ValidacaoException("Necessário infomar o município no cadastro da empresa ativa.");

		Aac11 aac11 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aac11 WHERE aac11empresa = :id", Parametro.criar("id", aac10.aac10id));
		if(aac11 == null) throw new ValidacaoException("Necessário infomar os dados contábeis da empresa empresa ativa.");
		
		Aag0201 aag0201 = buscarMunicipio(aac10.aac10municipio.aag0201id);
		Aag02 aag02 = buscarUnidadeDaFederacao(aag0201.aag0201uf.aag02id);
		Aac1002 aac1002 =  aag02 == null ? null : getSession().createCriteria(Aac1002.class).addWhere(Criterions.eq("aac1002empresa", aac10.aac10id)).addWhere(Criterions.eq("aac1002uf", aag02.getAag02id())).get();

		String ctaProvisoria = getAcessoAoBanco().buscarParametro("CTAPROVISORIA", "EB");
		String ctaARE = getAcessoAoBanco().buscarParametro("CTAARE", "EB");

		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator((char)',');
		format = new DecimalFormat("##0.00", dfs);

		DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("ddMMyyyy");
		DateTimeFormatter dtfAno = DateTimeFormatter.ofPattern("yyyy");

		//Início da geração do arquivo
		Integer totalRegistros = 0;

		Set<String> mesEncerramento = new HashSet<String>(); //Guarda o mês/ano que tem encerramento para poder gerar o J005 nos respectivos meses

		TextFile txt = new TextFile("|");
		
		/**
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * BLOCO 0 - Abertura, Identificação e Referências * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 */
		enviarStatusProcesso("Gerando Bloco 0...");
		
		Integer totalBloco0 = 0;
		Integer total0007 = 0;
		Integer total0150 = 0;
		Integer total0180 = 0;

		int finalidade = 0;

		/**
		 * 0000 – Abertura do arquivo digital e identificação da entidade 
		 */
		txt.print("0000");
		txt.print("LECD");
		txt.print(dtfData.format(dtInicial));
		txt.print(dtfData.format(dtFinal));
		txt.print(aac10.aac10rs);
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);
		txt.print(aag02 == null ? null : aag02.aag02uf, 2);
		txt.print(aac1002 != null ? StringUtils.extractNumbers(aac1002.aac1002ie) : null);
		txt.print(aac10.aac10municipio != null ? aag0201.aag0201ibge : null, 7);
		txt.print(aac10.aac10im == null ? null : StringUtils.extractNumbers(aac10.aac10im));
		txt.print(null);
		txt.print(0);
		txt.print(aac10.aac10nireNumero == null ? 0 : 1);
		txt.print(finalidade);
		txt.print(null);
		txt.print(0);
		txt.print(0);
		txt.print(null);
		txt.print("N");
		txt.print("N");
		txt.print("0");
		txt.print("0");
		txt.print(aac11.aac11ecdRespPc);
		txt.newLine();

		totalBloco0++;


		/**
		 * 0001 – Abertura do bloco 0 
		 */
		txt.print("0001");
		txt.print("0");
		txt.newLine();

		totalBloco0++;


		/**
		 * 0007 – Outras inscrições de empresas com acesso a contabilidade 
		 */
		List<TableMap> listAac1002s = getAcessoAoBanco().buscarListaDeTableMap("SELECT aac1002id, aag02uf, aac1002ie FROM Aac1002 INNER JOIN Aag02 ON aag02id = aac1002uf WHERE aac1002empresa = :idEmp", Parametro.criar("idEmp", aac10.aac10id));
		if(listAac1002s.size() > 0 && listAac1002s != null) {
			for(TableMap aac1002emp : listAac1002s){
				txt.print("0007");
				txt.print(aac1002emp.getString("aag02uf"), 2);
				txt.print(aac1002emp.getString("aac1002ie") == null ? null : StringUtils.extractNumbers(aac1002emp.getString("aac1002ie")));
				txt.newLine();

				total0007++;
			}
			listAac1002s = null;
		}

		totalBloco0 = totalBloco0 + total0007;

		/**
		 * 0150 - Participantes (entidades) 
		 */    
		enviarStatusProcesso("Gerando registro  0150");
		
		String sqlAbe01 = " SELECT abe01id, abe01codigo, abe01nome, aag01bacen, abe01ni, abe01ti, abe01pis, abe01ie, abe01im, abe01suframa, aag02uf, aag0201ibge " +
						  " FROM Ebb05 " +
						  " INNER JOIN Abb01 ON abb01id = ebb05central INNER JOIN Abe01 ON abe01id = abb01ent " +
						  " LEFT JOIN Abe0101 ON abe0101ent = abe01id LEFT JOIN Abe06 ON abe06ent = abe01id " +
						  " LEFT JOIN Aag01 ON aag01id = abe0101pais LEFT JOIN Aag0201 ON aag0201id = abe0101municipio LEFT JOIN Aag02 ON aag02id  = aag0201uf " +
						  " WHERE ebb05data BETWEEN :dtInicial AND :dtFinal AND abe06ecdCodRel <> :codrel " +
						  getSamWhere().getWherePadrao("AND", Ebb05.class);

		List<TableMap> listAbe01s = getAcessoAoBanco().buscarListaDeTableMap(sqlAbe01, Parametro.criar("dtInicial", dtInicial), Parametro.criar("dtFinal", dtFinal), Parametro.criar("codrel", 00));
		if(listAbe01s.size() > 0 && listAbe01s != null) {
			for(int i = 0; i < listAbe01s.size(); i++) {
				txt.print("0150");
				txt.print(listAbe01s.get(i).get("abe01codigo"));
				txt.print(listAbe01s.get(i).get("abe01nome"));
				txt.print(listAbe01s.get(i).get("aag01bacen"), 5, '0', true);

				String ni = listAbe01s.get(i).get("abe01ni") == null ? null : StringUtils.extractNumbers(listAbe01s.get(i).get("abe01ni"));
				txt.print(listAbe01s.get(i).get("abe01ti").equals(0) ? StringUtils.ajustString(ni, 14, '0', true) : null);
				txt.print(listAbe01s.get(i).get("abe01ti").equals(1) ? StringUtils.ajustString(ni, 11, '0', true) : null);

				txt.print(listAbe01s.get(i).get("abe01pis") == null ? null : StringUtils.ajustString(listAbe01s.get(i).get("abe01pis"), 11, '0', true));
				txt.print(listAbe01s.get(i).get("aag02uf"), 2);
				txt.print(listAbe01s.get(i).get("abe01ie") == null ? null : StringUtils.extractNumbers(listAbe01s.get(i).get("abe01ie")));
				txt.print(null);
				txt.print(listAbe01s.get(i).get("aag0201ibge"), 7, '0', true);
				txt.print(listAbe01s.get(i).get("abe01im") == null ? null : StringUtils.extractNumbers(listAbe01s.get(i).get("abe01im")));
				txt.print(listAbe01s.get(i).get("abe01suframa"), 9);
				txt.newLine();

				total0150++;

				/**
				 * 0180 - Identificação do relacionamento com o participante
				 */
				Abe06 abe06 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe06 WHERE abe06ent = :idEnt", Parametro.criar("idEnt", listAbe01s.get(i).get("abe01id")));
				if(abe06 != null) {
					if(abe06.abe06ecdCodRel_Zero > 0) {
						txt.print("0180");
						txt.print(abe06.abe06ecdCodRel);
						txt.print(abe06.abe06ecdDtIRel != null ? dtfData.format(abe06.abe06ecdDtIRel) : null);
						txt.print(abe06.abe06ecdDtFRel != null ? dtfData.format(abe06.abe06ecdDtFRel) : null);
						txt.newLine();

						total0180++;
					}
				}
			}
		}
		totalBloco0  = totalBloco0 + total0150 + total0180;

		/**
		 * 0990 – Encerramento do Bloco 0 
		 */
		txt.print("0990");
		txt.print(totalBloco0 + 1);
		txt.newLine();

		totalBloco0++;


		/**
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * BLOCO I - Lançamentos Contábeis * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 */
		enviarStatusProcesso("Gerando Bloco I");
		
		Integer totalBlocoI = 0;
		Integer totalI010 = 0;
		Integer totalI030 = 0;
		Integer totalI050 = 0;
		Integer totalI051 = 0;
		Integer totalI052 = 0;
		Integer totalI053 = 0;
		Integer totalI150 = 0;
		Integer totalI155 = 0;
		Integer totalI200 = 0;
		Integer totalI250 = 0;
		Integer totalI350 = 0;
		Integer totalI355 = 0;


		String sqlCountEbb05 = " SELECT COUNT(*) FROM Ebb05 " +
							   " WHERE ebb05data BETWEEN :dtInicial AND :dtFinal " +
							   getSamWhere().getWherePadrao("AND", Ebb05.class);
	    
        Integer qtdeLctosContab = getAcessoAoBanco().obterCount(sqlCountEbb05, Parametro.criar("dtInicial", dtInicial), Parametro.criar("dtFinal", dtFinal));

		/**
		 * I001 – Abertura do bloco I
		 */
		txt.print("I001");
		txt.print(qtdeLctosContab > 0 ? "0" : "1");
		txt.newLine();

		totalBlocoI++;


		if(qtdeLctosContab > 0) {
			/**
			 * I010 – Identificação da escrituração contábil 
			 */

			txt.print("I010");
			txt.print("G");
			txt.print(versaoLeiaute);
			txt.newLine();

			totalI010++;
			totalBlocoI++;


			/**
			 * I030 - Termo de Abertura 
			 */
			txt.print("I030");
			txt.print("TERMO DE ABERTURA");
			txt.print(numLivro);
			txt.print("LIVRO DIARIO CONTABIL");
			txt.print("{Qtde}");
			txt.print(aac10.aac10rs);
			txt.print(aac10.aac10nireNumero == null ? null : StringUtils.extractNumbers(aac10.aac10nireNumero), 11);
			txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);
			txt.print(aac11.aac11ecdDtAtos != null ? dtfData.format(aac11.aac11ecdDtAtos) : null);
			txt.print(aac11.aac11ecdDtConv != null ? dtfData.format(aac11.aac11ecdDtConv) : null);
			txt.print(aac10.aac10aMunicipio != null ? buscarMunicipio(aac10.aac10aMunicipio.aag0201id).aag0201nome : null);
			txt.print(dtfData.format(dtFinal));
			txt.newLine();


			totalI030++;
			totalBlocoI++;


			/**
			 * I050 - Plano de Contas
			 */
			enviarStatusProcesso("Gerando registro I050");
			
			String sqlAbc10 = " SELECT princ.abc10id AS abc10id, princ.abc10ecdNat AS abc10ecdNat, princ.abc10codigo AS abc10codigo, princ.abc10nome AS abc10nome, princ.abc10reduzido AS abc10reduzido, princ.abc10sup AS abc10sup, sup.abc10codigo AS supCodigo, " +
						  	  " princ.abc10ctaRef AS abc10ctaRef, aaj20.aaj20codigo as aaj20codigo " +
							  " FROM Abc10 as princ " +
							  " LEFT JOIN Abc10 AS sup ON sup.abc10id = princ.abc10sup " +
						      " LEFT JOIN Aaj20 AS aaj20 ON aaj20.aaj20id = princ.abc10ctaRef " +
							  getSamWhere().getWherePadrao("WHERE", Abc10.class, "princ") +
							  " ORDER BY princ.abc10codigo";
							  
			List<TableMap> listAbc10s = getAcessoAoBanco().buscarListaDeTableMap(sqlAbc10);
			String nome = null;
			if(listAbc10s.size() > 0 && listAbc10s != null) {
				for(TableMap abc10 : listAbc10s) {
					txt.print("I050");
					txt.print(dtfData.format(dtInicial));

					if(abc10.getInteger("abc10ecdNat") == null) {
						txt.print(null);
					}else {
						switch (abc10.getInteger("abc10ecdNat")) {
							case 1:  txt.print("01"); break;
							case 2:  txt.print("02"); break;
							case 3:  txt.print("03"); break;
							case 4:  txt.print("04"); break;
							case 5:  txt.print("04"); break;
							case 6:  txt.print("05"); break;
							case 9:  txt.print("09"); break;
							default: txt.print(null); break;
						}
					}

					txt.print(abc10.getInteger("abc10reduzido") == 0 ? "S" : "A");

					switch (abc10.getString("abc10codigo").length()) {
						case 1:  txt.print("1");  break;
						case 2:  txt.print("2");  break;
						case 3:  txt.print("3");  break;
						case 5:  txt.print("4");  break;
						case 7:  txt.print("5");  break;
						case 11: txt.print("6");  break;
						default: txt.print(null); break;
					}


					txt.print(abc10.getString("abc10codigo"));
					txt.print(abc10.getString("supCodigo") == null ? null : abc10.getString("supCodigo"));

					if(abc10.getString("abc10nome") != null) nome = abc10.getString("abc10nome");
					txt.print(nome);

					txt.newLine();

					totalI050++;
					totalBlocoI++;


					/**
					 * I051 - Plano de contas referencial 
					 */
					if(abc10.getInteger("abc10reduzido") > 0) {
						if(abc10.getLong("abc10ctaRef") != null) {
							txt.print("I051");
							txt.print(null);
							txt.print(abc10.getString("aaj20codigo"));
							txt.newLine();

							totalI051++;
							totalBlocoI++;
						}

						/**
						 * I052 - Códigos de Aglutinação
						 */
						if(abc10.getString("abc10sup") != null) {
							txt.print("I052");
							txt.print(null);
							txt.print(abc10.getString("supCodigo"));
							txt.newLine();

							totalI052++;
							totalBlocoI++;
						}

						/**
						 * I053 - Subcontas correlatas
						 */
						String sqlAbc10Sup = " SELECT abc10id, abc10reduzido, abc10codigo, abc10ecdNatSub, aaj21codigo FROM Abc10 "+
											 " LEFT JOIN Aaj21 ON aaj21id = abc10ecdNatSub " +
											 " WHERE abc10ecdCtaPai = :abc10id" +
											 getSamWhere().getWherePadrao("AND", Abc10.class) +
											 " ORDER BY abc10codigo";
											 
						List<Abc10> abc10sSub = getAcessoAoBanco().buscarListaDeRegistros(sqlAbc10Sup, Parametro.criar("abc10id", abc10.getLong("abc10id")));
						if(abc10sSub != null && abc10sSub.size() > 0) {
							for(Abc10 abc10sub : abc10sSub){
								txt.print("I053");
								txt.print(abc10sub.abc10reduzido);
								txt.print(abc10sub.abc10codigo);
								txt.print(abc10sub.abc10ecdNatSub != null ? abc10sub.abc10ecdNatSub.aaj21codigo : null);
								txt.newLine();

								totalI053++;
								totalBlocoI++;
							}
						}
					}
				}
				listAbc10s = null;
			}

			/**
			 * I150 - Saldos periódicos
			 */
			enviarStatusProcesso("Gerando registro I150");
			
			Long qtMeses = DateUtils.dateDiff(dtInicial, dtFinal, ChronoUnit.MONTHS) +1;

			LocalDate dtI = dtInicial;
			LocalDate dtF = dtFinal;

			String sqlEbb02 = " SELECT abc10id, abc10codigo FROM Ebb02 INNER JOIN Abc10 ON abc10id = ebb02cta " +
							  " WHERE abc10reduzido > 0 AND abc10codigo <> :ctaProvisoria AND " +
							  Fields.numMeses("ebb02mes", "ebb02ano") + " BETWEEN :numMesesIni AND :numMesesFin " +
							  getSamWhere().getWhereGc("AND", Abc10.class) +
							  " OR abc10id IN ( " +
							  " SELECT abc10id FROM Ebb02 INNER JOIN Abc10 ON abc10id = ebb02cta " +
							  " WHERE abc10reduzido > 0 AND abc10codigo <> :ctaProvisoria AND " +
							  Fields.numMeses("ebb02mes", "ebb02ano") + " <= :numMesesFin " +
							  getSamWhere().getWhereGc("AND", Abc10.class) + " GROUP BY abc10id " +
							  "HAVING SUM(ebb02deb - ebb02cred) <> 0 )" +
							  " GROUP BY abc10id, abc10codigo ORDER BY abc10codigo ";
			List<TableMap> listContas = getAcessoAoBanco().buscarListaDeTableMap(sqlEbb02, Parametro.criar("ctaProvisoria", ctaProvisoria),
										Parametro.criar("numMesesIni", Criterions.valNumMeses(dtI.getMonthValue(), dtI.getYear())),
										Parametro.criar("numMesesFin", Criterions.valNumMeses(dtF.getMonthValue(), dtF.getYear())));

			for(int i = 0; i < qtMeses; i++) {
				dtI = LocalDate.of(dtI.getYear(), dtI.getMonthValue(), 1); //Primeiro dia do mês

				/**
				 * I155 - Detalhe dos saldos periódicos  
				 */

				if(listContas != null && listContas.size() > 0) {//I150 - Saldos periódicos - Só gera o registro I150 se houver detalhamento dos saldos no período (I155)
					Map<String, TableMap> mapI155 = new HashMap<String, TableMap>();

					for(int j = 0; j < listContas.size(); j++) {
						TableMap mapPlan = new TableMap();

						String sqlEbb02id = "SELECT ebb02id, ebb02deb, ebb02cred FROM Ebb02 WHERE ebb02cta = :cta AND ebb02ano = :ano  AND  ebb02mes = :mes " +
											getSamWhere().getWhereGc("AND", Ebb02.class);
						Ebb02 ebb02 = getAcessoAoBanco().buscarRegistroUnico(sqlEbb02id, Parametro.criar("cta", listContas.get(j).get("abc10id")), Parametro.criar("ano", dtI.getYear()), Parametro.criar("mes", dtI.getMonthValue()));

						String sqlSaldoAnt = " SELECT SUM(ebb02deb - ebb02cred) AS saldo " +
											 " FROM Ebb02 WHERE ebb02cta = :cta AND " +
							 			 	 Fields.numMeses("ebb02mes", "ebb02ano") + " < :numMeses " +
											 getSamWhere().getWhereGc("AND", Ebb02.class);
						BigDecimal saldoAnterior = getAcessoAoBanco().obterBigDecimal(sqlSaldoAnt, Parametro.criar("cta", listContas.get(j).get("abc10id")), Parametro.criar("numMeses", Criterions.valNumMeses(dtI.getMonthValue(), dtI.getYear())));

						BigDecimal debito = ebb02 == null ? new BigDecimal(0) : ebb02.getEbb02deb();
						BigDecimal credito = ebb02 == null ? new BigDecimal(0) : ebb02.getEbb02cred();
						BigDecimal saldoFinal = saldoAnterior.add(debito).subtract(credito);

						if(saldoAnterior != 0 || debito != 0 || credito != 0) {
							mapPlan.put("sdoAnterior", saldoAnterior);
							mapPlan.put("debito", debito);
							mapPlan.put("credito", credito);
							mapPlan.put("sdoFinal", saldoFinal);

							mapI155.put(listContas.get(j).get("abc10codigo"), mapPlan);
						}
					}

					if(mapI155.size() > 0) {
						txt.print("I150");
						txt.print(dtfData.format(dtI));

						LocalDate dt = dtI;
						if(i == qtMeses-1) { //último mês recebe a data final
							dt = LocalDate.of(dtFinal.getYear(), dtFinal.getMonthValue(), dtFinal.getDayOfMonth());
						}else {
							dt = dt.with(TemporalAdjusters.lastDayOfMonth());
						}

						txt.print(dtfData.format(dt));
						txt.newLine();

						totalI150++;
						totalBlocoI++;

						TreeSet<String> setOrdenado = new TreeSet<String>();
						setOrdenado.addAll(mapI155.keySet());
						for(Object key : setOrdenado) {
							TableMap mapPlan = mapI155.get(key);

							txt.print("I155");
							txt.print(key);
							txt.print(null);

							BigDecimal saldoAnterior = mapPlan.getBigDecimal("sdoAnterior");
							txt.print(ajustarValor(saldoAnterior));
							txt.print(saldoAnterior >= 0 ? "D" : "C");

							txt.print(ajustarValor(mapPlan.getBigDecimal("debito")));
							txt.print(ajustarValor(mapPlan.getBigDecimal("credito")));

							BigDecimal saldoFinal = mapPlan.getBigDecimal("sdoFinal");
							txt.print(ajustarValor(saldoFinal));
							txt.print(saldoFinal >= 0 ? "D" : "C");
							txt.newLine();

							totalI155++;
							totalBlocoI++;
						}
					}
				}
				dtI = dtI.plusMonths(1);
			}
			listContas = null;


			/**
			 * I200 - Lançamento Contábil 
			 */
			dtI = dtInicial;
			for(int mes = 1; mes <= qtMeses; mes++) {
				if(mes > 1) dtI = LocalDate.of(dtI.getYear(), dtI.getMonthValue(), 1);
				dtF = mes == qtMeses ? dtFinal : LocalDate.of(dtI.getYear(), dtI.getMonthValue(), dtI.lengthOfMonth());
				enviarStatusProcesso("Gerando registro I200 - Mês: " + dtI.getMonthValue());
				
				int pagina = 0;
				
				String sqlLcto = " SELECT * FROM Ebb05 " +
								 " WHERE ebb05data BETWEEN :dtIni AND :dtFin " +
								 getSamWhere().getWherePadrao("AND", Ebb05.class) +
								 " ORDER BY ebb05data, ebb05num";

				List<Ebb05> listEbb05sLcto = getAcessoAoBanco().buscarListaDeRegistros(sqlLcto, true, pagina, Parametro.criar("dtIni", dtI), Parametro.criar("dtFin", dtF));
				while(listEbb05sLcto.size() > 0) {
					for(Ebb05 ebb05 : listEbb05sLcto) {
						txt.print("I200");
						txt.print(ebb05.getEbb05num());
						txt.print(dtfData.format(ebb05.getEbb05data()));
						txt.print(ajustarValor(ebb05.getEbb05valor()));
						txt.print(ebb05.getEbb05encerramento() == 0 ? "N" : "E");
						txt.print(null);
						txt.newLine();

						totalI200++;
						totalBlocoI++;

						/**
						 * I250 - Partidas do lançamento 
						 */
						for(int i = 0; i < 2; i++) { //0-Débito e 1-Crédito
							txt.print("I250");

							Long idCta = i == 0 ? ebb05.ebb05deb.abc10id : ebb05.ebb05cred.abc10id;
							Abc10 abc10 = getAcessoAoBanco().buscarRegistroUnicoById("Abc10", idCta);

							txt.print(abc10.getAbc10codigo());
							txt.print(null);
							txt.print(ajustarValor(ebb05.getEbb05valor()));
							txt.print(i == 0 ? "D" : "C");
							txt.print(null);
							txt.print(null);

							String historico = ebb05.getEbb05historico().replaceAll("\n", "").trim();
							txt.print(historico);

							Abe01 abe01 = null;
							if(ebb05.ebb05central != null){
								Abb01 abb01 = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", ebb05.ebb05central.abb01id);
								if(abb01 != null && abb01.abb01ent != null) {
									abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", abb01.abb01ent.abe01id);
									
									Abe06 abe06 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe06 WHERE abe06ent = :idEnt", Parametro.criar("idEnt", abb01.abb01ent.abe01id));
									if(abe06 == null || abe06.abe06ecdCodRel_Zero == 0)abe01 = null;
								}
							}
							
							txt.print(abe01 == null ? null : abe01.getAbe01codigo());
							txt.newLine();

							totalI250++;
							totalBlocoI++;
						}
					}

					pagina++;
					listEbb05sLcto = getAcessoAoBanco().buscarListaDeRegistros(sqlLcto, true, pagina, Parametro.criar("dtIni", dtI), Parametro.criar("dtFin", dtF));
				}
				listEbb05sLcto = null;

				dtI = dtI.plusMonths(1);
			}


			/**
			 * I350 - Saldos das contas antes do encerramento
			 */
			enviarStatusProcesso("Gerando registro  I350");
			
			String sqlEbb03 = " SELECT ebb03ano, ebb03mes, SUM(ebb03saldo) as ebb03saldo " +
							  " FROM Ebb03 INNER JOIN Abc10 ON abc10id = ebb03cta " +
							  " WHERE abc10class = 2 AND abc10reduzido > 0 AND " +
							  Fields.numMeses("ebb03mes", "ebb03ano") + " BETWEEN :numMesesIni AND :numMesesFin " +
							  getSamWhere().getWherePadrao("AND", Ebb03.class) +
							  " GROUP BY ebb03ano, ebb03mes " +
							  " ORDER BY ebb03ano, ebb03mes ";
							  
			List<TableMap> listEbb03s = getAcessoAoBanco().buscarListaDeTableMap(sqlEbb03, Parametro.criar("numMesesIni", Criterions.valNumMeses(dtInicial.getMonthValue(), dtInicial.getYear())),
								     	                                                   Parametro.criar("numMesesFin", Criterions.valNumMeses(dtFinal.getMonthValue(), dtFinal.getYear())));

			if(listEbb03s != null && listEbb03s.size() > 0) {
				for(TableMap ebb03 : listEbb03s) {
					dtI = LocalDate.of(ebb03.getInteger("ebb03ano"), ebb03.getInteger("ebb03mes"), 1);
					dtI = dtI.with(TemporalAdjusters.lastDayOfMonth());

					mesEncerramento.add(DateTimeFormatter.ofPattern("MM/yyyy").format(dtI));

					String sqlSaldos = " SELECT abc10codigo, ebb03saldo " +
									   " FROM Ebb03 INNER JOIN Abc10 ON abc10id = ebb03cta " +
									   " WHERE abc10class = 2 AND abc10reduzido > 0 AND " +
									   Fields.numMeses("ebb03mes", "ebb03ano") + " = :numMeses " +
									   " AND (ebb03saldo <> 0 OR abc10codigo = :ctaAre) " + getSamWhere().getWherePadrao("AND", Ebb03.class) +
									   " ORDER BY abc10codigo";
				
					List<TableMap> listSaldos = getAcessoAoBanco().buscarListaDeTableMap(sqlSaldos, Parametro.criar("numMeses", Criterions.valNumMeses(dtI.getMonthValue(), dtI.getYear())), Parametro.criar("ctaAre", ctaARE));
					if(listSaldos != null && listSaldos.size() > 0) {//I350 - Saldos - Só gera o registro I350 se houver detalhamento dos saldos no período (I355)
						txt.print("I350");
						txt.print(dtfData.format(dtI));
						txt.newLine();

						totalI350++;
						totalBlocoI++;

						/**
						 * I355 - Detalhe dos saldos das contas antes do encerramento
						 */
						for(TableMap saldo : listSaldos) {
							txt.print("I355");
							txt.print(saldo.getString("abc10codigo"));
							txt.print(null);
							txt.print(ajustarValor(saldo.getBigDecimal("ebb03saldo")));
							txt.print(saldo.getBigDecimal("ebb03saldo").compareTo(new BigDecimal(0)) < 0 ? "C" : "D");
							txt.newLine();

							totalI355++;
							totalBlocoI++;
						}
					}
					listSaldos = null;
				}
				listEbb03s = null;
			}
		}

		/**
		 * I990 - Encerramento do bloco I 
		 */
		txt.print("I990");
		txt.print(totalBlocoI + 1);
		txt.newLine();

		totalBlocoI++;


		/**
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * * * * BLOCO J - Demonstrações Contábeis * * * * * * * * * * * * * * * * * * * *  
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 */
		enviarStatusProcesso("Gerando Bloco J");

		Integer totalBlocoJ = 0;
		Integer totalJ005 = 0;
		Integer totalJ100 = 0;
		Integer totalJ150 = 0;
		Integer totalJ200 = 0;
		Integer totalJ210 = 0;
		Integer totalJ215 = 0;
		Integer totalJ800 = 0;
		Integer totalJ900 = 0;
		Integer totalJ930 = 0;
		Integer totalJ932 = 0;

		/**
		 * J001 - Abertura do Bloco J
		 */
		txt.print("J001");
		txt.print(qtdeLctosContab > 0 ? "0" : "1");
		txt.newLine();

		totalBlocoJ++;

		if(qtdeLctosContab > 0) {
			Long qtMeses = DateUtils.dateDiff(dtInicial, dtFinal, ChronoUnit.MONTHS) +1;

			LocalDate data = dtInicial;
			LocalDate dtI = dtInicial;
			LocalDate dtF = null;

			for(int mes = 0; mes < qtMeses; mes++) {
				dtF = mes == qtMeses-1 ? dtFinal.with(TemporalAdjusters.lastDayOfMonth()) : data.with(TemporalAdjusters.lastDayOfMonth());

				if(mesEncerramento.contains(DateTimeFormatter.ofPattern("MM/yyyy").format(data))) { //Somente o mês/ano que teve encerramento pode gerar o J005

					/**
					 * J005 - Demonstrações contábeis 
					 */
					txt.print("J005");
					txt.print(dtfData.format(dtI));
					txt.print(dtfData.format(dtF));
					txt.print("1");
					txt.print(null);
					txt.newLine();

					totalJ005++;
					totalBlocoJ++;

					/**
					 * J100 - Balanço Patrimonial
					 */
					enviarStatusProcesso("Gerando registro  J100");
					
					String sqlCtasPatr = " SELECT abc10cta.abc10id As abc10id, abc10cta.abc10codigo As abc10codigo, abc10cta.abc10nome As abc10nome, abc10cta.abc10class As abc10class, abc10sup.abc10codigo As abc10codigoSup " +
										 " FROM Ebb02 " +
										 " INNER JOIN Abc10 As abc10cta ON abc10cta.abc10id = ebb02cta " +
										 " LEFT JOIN Abc10 As abc10sup ON abc10sup.abc10id = abc10cta.abc10sup " +
										 " WHERE abc10cta.abc10reduzido = 0  AND abc10cta.abc10class < 2 " +
										 " AND abc10cta.abc10codigo <> :ctaProvisoria AND " +
										 Fields.numMeses("ebb02mes", "ebb02ano") + " <= :numMeses " +
										 getSamWhere().getWherePadrao("AND", Ebb02.class) +
										 " GROUP BY abc10cta.abc10id, abc10cta.abc10codigo, abc10cta.abc10nome, abc10cta.abc10class, abc10sup.abc10codigo " +
										 " ORDER BY abc10cta.abc10codigo, abc10cta.abc10class";
										 
					List<TableMap> listCtasPatr = getAcessoAoBanco().buscarListaDeTableMap(sqlCtasPatr, Parametro.criar("numMeses", Criterions.valNumMeses(data.getMonthValue(), data.getYear())),
							                                                                            Parametro.criar("ctaProvisoria", ctaProvisoria));
					String nome = null;

					if(listCtasPatr != null && listCtasPatr.size() > 0) {
						for(int i = 0; i < listCtasPatr.size(); i++) {
							txt.print("J100");

							String abc10codigo = listCtasPatr.get(i).getString("abc10codigo");
							txt.print(abc10codigo);

							txt.print(abc10codigo.length() < 7 ? "T" : "D");

							switch (abc10codigo.length()) {
								case 1:  txt.print("1");  break;
								case 2:  txt.print("2");  break;
								case 3:  txt.print("3");  break;
								case 5:  txt.print("4");  break;
								case 7:  txt.print("5");  break;
								case 11: txt.print("6");  break;
								default: txt.print(null); break;
							}

							txt.print(listCtasPatr.get(i).getString("abc10codigoSup"));

							txt.print(listCtasPatr.get(i).getInteger("abc10class") == 0 ? "A" : "P");

							if(listCtasPatr.get(i).getString("abc10nome") != null) nome = listCtasPatr.get(i).getString("abc10nome");
							txt.print(nome);

							String sqlSdoAnt = " SELECT ebb02saldo " +
											   " FROM Ebb02 WHERE ebb02cta = :idAbc10 AND " +
											   Fields.numMeses("ebb02mes", "ebb02ano") + " < :numMeses " +
											   getSamWhere().getWherePadrao("AND", Ebb02.class) +
											   " ORDER BY ebb02ano DESC, ebb02mes DESC";
											   
							BigDecimal saldoAnterior = getAcessoAoBanco().obterBigDecimal(sqlSdoAnt, Parametro.criar("numMeses", Criterions.valNumMeses(dtInicial.getMonthValue(), dtInicial.getYear())),
											                                                         Parametro.criar("idAbc10", listCtasPatr.get(i).getLong("abc10id")));
												   
							txt.print(ajustarValor(saldoAnterior));
							txt.print(saldoAnterior >= 0 ? "D" : "C");
							
							String sqlSaldo = " SELECT ebb02saldo " +
											  " FROM Ebb02 WHERE ebb02cta = :idAbc10 AND " +
											  Fields.numMeses("ebb02mes", "ebb02ano") + " <= :numMeses " +
											  getSamWhere().getWherePadrao("AND", Ebb02.class) +
											  " ORDER BY ebb02ano DESC, ebb02mes DESC";
							
                            BigDecimal saldo = getAcessoAoBanco().obterBigDecimal(sqlSaldo, Parametro.criar("numMeses", Criterions.valNumMeses(data.getMonthValue(), data.getYear())),
 																		                    Parametro.criar("idAbc10", listCtasPatr.get(i).getLong("abc10id")));
						    
							txt.print(ajustarValor(saldo));
							txt.print(saldo >= 0 ? "D" : "C");

							txt.print(null);
							txt.newLine();

							totalJ100++;
							totalBlocoJ++;
						}
						listCtasPatr = null;
					}


					/**
					 * J150 - DRE
					 */  
					enviarStatusProcesso("Gerando registro  J150");
					
					String sqlCtasResult = " SELECT abc10cta.abc10id As abc10id, abc10cta.abc10codigo As abc10codigo, abc10cta.abc10nome As abc10nome, abc10cta.abc10class As abc10class, " +
										   " abc10sup.abc10codigo As abc10codigoSup, SUM(ebb03saldo) AS ebb03saldo " +
										   " FROM Ebb03 " +
										   " INNER JOIN Abc10 As abc10cta ON abc10cta.abc10id = ebb03cta " +
										   " LEFT JOIN Abc10 As abc10sup ON abc10sup.abc10id = abc10cta.abc10sup " +
										   " WHERE abc10cta.abc10reduzido = 0 AND abc10cta.abc10class = 2 AND " +
										   Fields.numMeses("ebb03mes", "ebb03ano") + " = :numMeses " +
										   getSamWhere().getWherePadrao("AND", Ebb03.class) +
										   " GROUP BY abc10cta.abc10id, abc10cta.abc10codigo, abc10cta.abc10nome, abc10cta.abc10class, abc10sup.abc10codigo " +
										   " ORDER BY abc10cta.abc10codigo, abc10cta.abc10class";

					List<TableMap> listCtasResult = getAcessoAoBanco().buscarListaDeTableMap(sqlCtasResult, Parametro.criar("numMeses", Criterions.valNumMeses(data.getMonthValue(), data.getYear())));
					nome = null;
					if(listCtasResult != null && listCtasResult.size() > 0) {
						BigDecimal totalSaldo = new BigDecimal(0);
						BigDecimal totalSdoAnterior = new BigDecimal(0);
						
						Integer gruposDRE = getAcessoAoBanco().obterCount("SELECT COUNT(*) FROM ABC10 WHERE abc10class = 2");
						boolean existeMaisDRE = gruposDRE > 1;
						
						for(int i = 0; i < listCtasResult.size(); i++) {

							txt.print("J150");
							txt.print(i);

							String abc10codigo = listCtasResult.get(i).getString("abc10codigo");
							txt.print(abc10codigo);
							txt.print(abc10codigo.length() < 7 ? "T" : "D");

							Integer nivel = 0;
							switch (abc10codigo.length()) {
								case 1:  nivel = 1;  break;
								case 2:  nivel = 2;  break;
								case 3:  nivel = 3;  break;
								case 5:  nivel = 4;  break;
								case 7:  nivel = 5;  break;
								case 11: nivel = 6;  break;
							}
							
							txt.print(existeMaisDRE ? nivel+1 : nivel);
							
							String codigoSuperior = listCtasResult.get(i).getString("abc10codigoSup");
							if(existeMaisDRE && codigoSuperior == null) codigoSuperior = "DRE";
							txt.print(codigoSuperior);

							if(listCtasResult.get(i).getString("abc10nome") != null) nome = listCtasResult.get(i).getString("abc10nome");
							txt.print(nome);
							
							String sqlSdoAnt = "SELECT ebb03saldo FROM Ebb03 " +
											   "WHERE ebb03cta = :idAbc10 AND " + Fields.numMeses("ebb03mes", "ebb03ano") + " < :numMeses " + getSamWhere().getWherePadrao("AND", Ebb03.class) +
											   "ORDER BY ebb03ano DESC, ebb03mes DESC";
							
                            BigDecimal saldoAnterior = getAcessoAoBanco().obterBigDecimal(sqlSdoAnt, Parametro.criar("idAbc10", listCtasResult.get(i).getLong("abc10id")), Parametro.criar("numMeses", Criterions.valNumMeses(dtI.getMonthValue(), dtI.getYear())));
							txt.print(ajustarValor(saldoAnterior));
							txt.print(saldoAnterior.compareTo(0) <= 0 ? "C" : "D");

							if(nivel == 1)totalSdoAnterior = totalSdoAnterior.add(saldoAnterior);
							
							BigDecimal saldo = listCtasResult.get(i).getBigDecimal("ebb03saldo");
							txt.print(ajustarValor(saldo));
							txt.print(saldo <= 0 ? "C" : "D");
							txt.print(saldo <= 0 ? "R" : "D");

							if(nivel == 1) totalSaldo = totalSaldo.add(saldo);
							
							txt.print(null);
							txt.newLine();

							totalJ150++;
							totalBlocoJ++;
						}
						
						/**
						 * Totalização da DRE quando há mais que um grupo de resultado (Ex: 3, 4, 5)
						 */
						if(existeMaisDRE) {
							txt.print("J150");
							txt.print(listCtasResult.size());
							txt.print("DRE");
							txt.print("T");
							txt.print(1);
							txt.print(null);
							txt.print("RESULTADO LIQUIDO DO EXERCICIO");
							txt.print(ajustarValor(totalSdoAnterior));
							txt.print(totalSdoAnterior.compareTo(0) <= 0 ? "C" : "D");
							txt.print(ajustarValor(totalSaldo));
							txt.print(totalSaldo <= 0 ? "C" : "D");
							txt.print(totalSaldo <= 0 ? "R" : "D");
							txt.print(null);
							txt.newLine();

							totalJ150++;
							totalBlocoJ++;
						}
					}


					/**
					 * J200 - J210 - J215
					 */
					/******** SET para armazenar as contas que serão consideradas na geração do J200 ********/
					/******** INFORMAR AS MESMAS QUANDO NECESSÁRIO ********/
					Set<String> abc10sJ200 = new HashSet<String>();

					if(abc10sJ200 != null && abc10sJ200.size() > 0){
						/**
						 * J200 - Histórico de Fatos Contábeis que modificam Lucros ou Prejuízos ou Prejuízo Acumulado ou Patrimônio Líquido
						 */
						Map<Integer, TableMap> mapJ200 = new HashMap<Integer, TableMap>();

						int row = 2;
						for(String grupo5 : abc10sJ200){
							String sqlGrupoCta = " SELECT ebb05.ebb05historico, ebb05.ebb05valor, abc10d.abc10codigo AS abc10codigoDeb, abc10c.abc10codigo AS abc10codigoCred" +
												 " FROM Ebb05 AS ebb05" +
												 " INNER JOIN Abc10 AS abc10d ON abc10d.abc10id = ebb05.ebb05deb " +
												 " INNER JOIN Abc10 AS abc10c ON abc10c.abc10id = ebb05.ebb05cred " +
												 " WHERE ebb05.ebb05data BETWEEN :dtInicial AND :dtFinal AND " +
												 " (abc10d.abc10codigo LIKE :abc10codigo OR abc10c.abc10codigo LIKE :abc10codigo) " +
												 getSamWhere().getWherePadrao("AND", Ebb05.class, "ebb05") +
												 " ORDER BY ebb05.ebb05data, ebb05.ebb05num ";

							List<TableMap> ebb05s = getAcessoAoBanco().buscarListaDeTableMap(sqlGrupoCta, Parametro.criar("dtInicial", dtI),
									Parametro.criar("dtFinal", dtF), Parametro.criar("abc10codigo", grupo5 + "%"));
							for(TableMap ebb05 : ebb05s){
								if(ebb05.getString("abc10codigoDeb").substring(0, 7).equals(grupo5)){
									TableMap j200 = new TableMap();
									j200.put("codHist", row);
									j200.put("nome", ebb05.getString("ebb05historico"));
									j200.put("conta", grupo5);
									j200.put("valor", ebb05.getBigDecimal("ebb05valor"));
									j200.put("dc", "D");

									mapJ200.put(row, j200);
									row++;
								}

								if(ebb05.getString("abc10codigoCred").substring(0, 7).equals(grupo5)){
									TableMap j200 = new TableMap();
									j200.put("codHist", row);
									j200.put("nome", ebb05.getString("ebb05historico"));
									j200.put("conta", grupo5);
									j200.put("valor", ebb05.getBigDecimal("ebb05valor"));
									j200.put("dc", "C");

									mapJ200.put(row, j200);
									row++;
								}
							}
						}

						if(mapJ200.size() > 0){
							TableMap sdoIni = new TableMap();
							sdoIni.put("codHist", 1);
							sdoIni.put("nome", "Saldo inicial");

							mapJ200.put(1, sdoIni);

							TableMap sdoFin = new TableMap();
							sdoFin.put("codHist", 9999);
							sdoFin.put("nome", "Saldo final");

							mapJ200.put(9999, sdoFin);
						}

						TreeSet setJ200 = new TreeSet<>();
						setJ200.addAll(mapJ200.keySet());

						/**
						 * J210 - DMPL - Demonstração de Mutações do Patrimônio Líquido
						 */
						for(String grupo5 : abc10sJ200){
							String sqlAbc10 = " SELECT abc10id, abc10nome FROM Abc10 " +
											  " WHERE UPPER(abc10codigo) = UPPER(:abc10codigo) " +
											  getSamWhere().getWhereGc("AND", Abc10.class);
							Abc10 abc10 = getAcessoAoBanco().buscarRegistroUnico(sqlAbc10, Parametro.criar("abc10codigo", grupo5));

							String sqlSdoIni = " SELECT SUM(ebb02deb - ebb02cred) AS ebb02saldo " +
											   " FROM Ebb02 INNER JOIN Abc10 ON abc10id = ebb02cta" +
											   " WHERE ebb02cta = :idAbc10 AND " +
											   Fields.numMeses("ebb02mes", "ebb02ano") + " < :numMeses " +
											   getSamWhere().getWherePadrao("AND", Ebb02.class);
							BigDecimal saldoInicial = getAcessoAoBanco().obterBigDecimal(sqlSdoIni, Parametro.criar("numMeses", Criterions.valNumMeses(dtI.getMonthValue(), dtI.getYear())),
													  Parametro.criar("idAbc10", abc10.getAbc10id()));

							String sqlSdoFinal = " SELECT SUM(ebb02deb - ebb02cred) AS ebb02saldo " +
												 " FROM Ebb02 INNER JOIN Abc10 ON abc10id = ebb02cta" +
												 " WHERE ebb02cta = :idAbc10 AND " +
												 Fields.numMeses("ebb02mes", "ebb02ano") + " <= :dataRef " +
												 getSamWhere().getWherePadrao("AND", Ebb02.class);
							BigDecimal saldoFinal = getAcessoAoBanco().obterBigDecimal(sqlSdoFinal, Parametro.criar("dataRef", Criterions.valNumMeses(dtF.getMonthValue(), dtF.getYear())),
													Parametro.criar("idAbc10", abc10.getAbc10id()));

							txt.print("J210");
							txt.print("1");
							txt.print(grupo5);
							txt.print(abc10.getAbc10nome());

							txt.print(ajustarValor(saldoInicial));
							txt.print(saldoInicial <= 0 ? "C" : "D");

							txt.print(ajustarValor(saldoFinal));
							txt.print(saldoFinal <= 0 ? "C" : "D");

							txt.print(null);
							txt.newLine();

							totalJ210++;
							totalBlocoJ++;

							/**
							 * J215 - Fato contábil
							 */
							/** Saldo inicial */
							TableMap j215SdoIni = mapJ200.get(1);
							txt.print("J215");
							txt.print(j215SdoIni.getString("codHist"));
							txt.print(ajustarValor(saldoInicial));
							txt.print(saldoInicial <= 0 ? "P" : "N");
							txt.newLine();

							totalJ215++;
							totalBlocoJ++;


							/** Lançamentos */
							for(Object key : setJ200){
								TableMap j215lct = mapJ200.get(key);

								if(j215lct.getString("conta") == null || !j215lct.getString("conta").equals(grupo5))continue;

								txt.print("J215");
								txt.print(j215lct.getString("codHist"));
								txt.print(ajustarValor(j215lct.getBigDecimal("valor")));
								txt.print(j215lct.getString("dc"));
								txt.newLine();

								totalJ215++;
								totalBlocoJ++;
							}

							/** Saldo final */
							TableMap j215SdoFin = mapJ200.get(9999);
							txt.print("J215");
							txt.print(j215SdoFin.getInteger("codHist"));
							txt.print(ajustarValor(saldoFinal));
							txt.print(saldoFinal <= 0 ? "P" : "N");
							txt.newLine();

							totalJ215++;
							totalBlocoJ++;
						}
					}

					/**
					 * J800 - Outras Informações
					 */
					String sqlEba30 = " SELECT eba30id, eba30texto FROM Eba30 WHERE " +
									  Fields.numMeses("eba30mes", "eba30ano") + " = :numMeses " +
									  getSamWhere().getWhereGc("AND", Eba30.class);
					List<Eba30> eba30s = getAcessoAoBanco().buscarListaDeRegistros(sqlEba30, Parametro.criar("numMeses", Criterions.valNumMeses(data.getMonthValue(), data.getYear())));
					for(Eba30 eba30 : eba30s){
						txt.print("J800");
						txt.print("010");
						txt.print("Notas Explicativas");
						txt.print(null);
						txt.print(eba30.getEba30texto());
						txt.print("J800FIM");
						txt.newLine();

						totalJ800++;
						totalBlocoJ++;
					}

					dtI = dtF;
					dtI = dtI.plusDays(1);
				}
				data = data.plusMonths(1);
			}
		}

		/**
		 * J900 - Termo de Encerramento
		 */
		txt.print("J900");
		txt.print("TERMO DE ENCERRAMENTO");
		txt.print(numLivro);
		txt.print("LIVRO DIARIO CONTABIL");
		txt.print(aac10.aac10rs);
		txt.print("{Qtde}");
		txt.print(dtfData.format(dtInicial));
		txt.print(dtfData.format(dtFinal));
		txt.newLine();

		totalJ900++;
		totalBlocoJ++;

		/**
		 * J930 - Signatários da escrituração
		 */
		//Dados do Contador
		if(!aac10.aac10cQualifCod.equals(910) && !aac10.aac10cQualifCod.equals(920)) {
			txt.print("J930");
			txt.print(aac10.aac10cNome);
			txt.print(aac10.aac10cCpf == null ? null : StringUtils.extractNumbers(aac10.aac10cCpf));
			txt.print(aac10.aac10cQualifDescr);
			txt.print(aac10.aac10cQualifCod, 3);
			txt.print(aac10.aac10cCrc == null ? null : StringUtils.extractNumbers(aac10.aac10cCrc));
			txt.print(aac10.aac10cEmail);
			txt.print(aac10.aac10cDddFone == null ? aac10.aac10cFone : aac10.aac10cFone == null ? null : StringUtils.concat(aac10.aac10cDddFone, aac10.aac10cFone));
			
			Aag02 ufCrc = aac10.aac10cCrcUf == null ? null : buscarUnidadeDaFederacao(aac10.aac10cCrcUf.aag02id);
			txt.print(ufCrc == null ? null : ufCrc.aag02uf);
			txt.print(ufCrc == null ? null : ufCrc.aag02uf + "/" + dtfAno.format(dtInicial) + "/001");
			
			txt.print(aac10.aac10cCrcDtValid == null ? null : dtfData.format(aac10.aac10cCrcDtValid));
			txt.print("N");
			txt.newLine();

			totalJ930++;
			totalBlocoJ++;
		}

		//Dados do representante legal
		txt.print("J930");
		txt.print(aac10.aac10rNome);
		txt.print(aac10.aac10rCpf == null ? null : StringUtils.extractNumbers(aac10.aac10rCpf));
		txt.print(aac10.aac10rQualifDescr);
		txt.print(aac10.aac10rQualifCod, 3);
		txt.print(null);
		txt.print(aac10.aac10rEmail);
		txt.print(aac10.aac10rDddFone == null ? aac10.aac10rFone : aac10.aac10rFone == null ? null : StringUtils.concat(aac10.aac10rDddFone, aac10.aac10rFone));
		txt.print(null);
		txt.print(null);
		txt.print(null);
		txt.print("S");
		txt.newLine();

		totalJ930++;
		totalBlocoJ++;

		/**
		 * J932 - Signatários do Termo de Verificação para Fins de Substituição da ECD
		 */
		if(finalidade == 1 && aac10.aac10cNome != null) {
			txt.print("J932");
			txt.print(aac10.aac10cNome);
			txt.print(aac10.aac10cCpf == null ? null : StringUtils.extractNumbers(aac10.aac10cCpf));
			txt.print(aac10.aac10cQualifDescr);
			txt.print(aac10.aac10cQualifCod, 3);
			txt.print(aac10.aac10cCrc == null ? null : StringUtils.extractNumbers(aac10.aac10cCrc));
			txt.print(aac10.aac10cEmail);
			txt.print(aac10.aac10cDddFone == null ? aac10.aac10cFone : aac10.aac10cFone == null ? null : StringUtils.concat(aac10.aac10cDddFone, aac10.aac10cFone));
			
			Aag02 ufCrc = buscarUnidadeDaFederacao(aac10.aac10cCrcUf.aag02id);
			txt.print(ufCrc == null ? null : ufCrc.aag02uf);
			txt.print(ufCrc == null ? null : ufCrc.aag02uf + "/" + dtfAno.format(dtInicial) + "/001");
			
			txt.print(aac10.aac10cCrcDtValid == null ? null : dtfData.format(aac10.aac10cCrcDtValid));
			txt.newLine();

			totalJ932++;
			totalBlocoJ++;
		}


		/**
		 * J990 - Encerramento do bloco J 
		 */
		txt.print("J990");
		txt.print(totalBlocoJ + 1);
		txt.newLine();

		totalBlocoJ++;



		/**
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 * * * * * * * * * * * * * * * BLOCO 9 - Controle e Encerramento do Arquivo Digital * * * * * * * * * * * * *   
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
		 */
		enviarStatusProcesso("Gerando Bloco 9");
		
		Integer totalBloco9 = 0;

		/**
		 * 9001 - Abertura do bloco 9
		 */
		txt.print("9001");
		txt.print("0");
		txt.newLine();

		totalBloco9++;


		/**
		 * 9900 - Registros do arquivo
		 */
		Integer total9900 = 0;
		total9900 = compor9900(txt, "0000", 1, total9900);
		total9900 = compor9900(txt, "0001", 1, total9900);
		total9900 = compor9900(txt, "0007", total0007, total9900);
		total9900 = compor9900(txt, "0150", total0150, total9900);
		total9900 = compor9900(txt, "0180", total0180, total9900);
		total9900 = compor9900(txt, "0990", 1, total9900);
		total9900 = compor9900(txt, "I001", 1, total9900);
		total9900 = compor9900(txt, "I010", totalI010, total9900);
		total9900 = compor9900(txt, "I030", totalI030, total9900);
		total9900 = compor9900(txt, "I050", totalI050, total9900);
		total9900 = compor9900(txt, "I051", totalI051, total9900);
		total9900 = compor9900(txt, "I052", totalI052, total9900);
		total9900 = compor9900(txt, "I053", totalI053, total9900);
		total9900 = compor9900(txt, "I150", totalI150, total9900);
		total9900 = compor9900(txt, "I155", totalI155, total9900);
		total9900 = compor9900(txt, "I200", totalI200, total9900);
		total9900 = compor9900(txt, "I250", totalI250, total9900);
		total9900 = compor9900(txt, "I350", totalI350, total9900);
		total9900 = compor9900(txt, "I355", totalI355, total9900);
		total9900 = compor9900(txt, "I990", 1, total9900);
		total9900 = compor9900(txt, "J001", 1, total9900);
		total9900 = compor9900(txt, "J005", totalJ005, total9900);
		total9900 = compor9900(txt, "J100", totalJ100, total9900);
		total9900 = compor9900(txt, "J150", totalJ150, total9900);
		total9900 = compor9900(txt, "J200", totalJ200, total9900);
		total9900 = compor9900(txt, "J210", totalJ210, total9900);
		total9900 = compor9900(txt, "J215", totalJ215, total9900);
		total9900 = compor9900(txt, "J800", totalJ800, total9900);
		total9900 = compor9900(txt, "J900", totalJ900, total9900);
		total9900 = compor9900(txt, "J930", totalJ930, total9900);
		total9900 = compor9900(txt, "J932", totalJ932, total9900);
		total9900 = compor9900(txt, "J990", 1, total9900);
		total9900 = compor9900(txt, "9001", 1, total9900);
		total9900 = compor9900(txt, "9900", total9900 +3, total9900); //Soma 2 pois é dos próximos registros que serão impressos
		total9900 = compor9900(txt, "9990", 1, total9900);
		total9900 = compor9900(txt, "9999", 1, total9900);

		totalBloco9 = totalBloco9 + total9900;


		/**
		 * 9990 - Encerramento do bloco 9
		 */
		txt.print("9990");
		txt.print(totalBloco9 + 2);
		txt.newLine();

		totalBloco9++;


		/**
		 * 9999 - Encerramento do arquivo digital
		 */
		totalRegistros = totalBloco0 + totalBlocoI + totalBlocoJ + totalBloco9 + 1;

		txt.print("9999");
		txt.print(totalRegistros);
		txt.newLine();


		/**
		 * MUDA O VALOR DA VARIAVEL '{Qtde}' SETANDO O NÚMERO TOTAL DE LINHAS DO ARQUIVO
		 */

		TextFile dados = substituirVariavel(txt, totalRegistros);


		// Adiciona o arquivo TXT no campo dadosArquivo para que o service da tarefa grave o arquivo no local informado
		put("dadosArquivo", dados);

	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ECD;
	}

	private TextFile substituirVariavel(TextFile texto, Integer totalRegistros) {
		File file = texto.getPath().toFile();
		List<String> lines = FileUtils.readLines(file, "UTF-8");

		String novaLinha = new String();
		TextFile txtNovo = new TextFile();

		for(linha in lines) {
			int pos = linha.indexOf("|{Qtde}|", -1);
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

	private Integer compor9900(TextFile txt, String registro, Integer qtde, Integer total9900) {
		if (qtde > 0) {
			txt.print("9900");
			txt.print(registro);
			txt.print(qtde);
			txt.newLine();

			total9900++;
		}

		return total9900;
	}

	private String ajustarValor(BigDecimal valor) {
		if (valor == null || valor.compareTo(new BigDecimal(0)) == 0)
			return "0,00";
		String vlr = format.format(valor.abs());

		return vlr;
	}

	private Aag0201 buscarMunicipio(Long Aag0201id) {
		Query query = getSession().createQuery("SELECT * FROM Aag0201 WHERE aag0201id = :aag0201id");
		query.setParameter("aag0201id", Aag0201id);
		return query.setMaxResult(1).getUniqueResult(ColumnType.ENTITY);
	}

	private Aag02 buscarUnidadeDaFederacao(Long Aag02id) {
		Query query = getSession().createQuery("SELECT * FROM Aag02 WHERE aag02id = :aag02id");
		query.setParameter("aag02id", Aag02id);
		return query.setMaxResult(1).getUniqueResult(ColumnType.ENTITY);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTYifQ==