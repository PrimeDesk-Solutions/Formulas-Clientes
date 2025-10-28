package multitec.formulas.sgt.apuracoes

import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abc10
import sam.model.entities.ab.Abg02
import sam.model.entities.ed.Edb10
import sam.model.entities.ed.Edb1001
import sam.model.entities.ed.Edb10011
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class Apuracao_Receita_PisCofins extends FormulaBase {

	private static final String alinApur = "0044";
	
	private List<Aac10> empresas = null;
	
	@Override
	FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_APURACAO
	}

	@Override
	void executar() {
		Edb10 edb10 = get("edb10");

		selecionarAlinhamento(alinApur);
		
		empresas = buscarMatrizEFiliais();
		
		String cpoP100_Vl_Rec_Tot_Est = getCampo("0", "vlRecTotEst");
		String cpoP100_Vl_Rec_Ativ_Estab = getCampo("0", "vlRecAtivEstab");
		String cpoP100_Vl_Exc = getCampo("0", "vlExc");
		String cpoP100_Aliq_Cont = getCampo("0", "aliqCont");
		
		/**
		 * Apuração da receita 
		 */
		def totalNaotributado = 0;
		def totalTributado = 0;
		def totalExportacao = 0;
		def totalCumulativo = 0;

		for(empresa in empresas) {
			Long gcEA = selecionarGCPorEmpresa(empresa.aac10id, "EA");
			
			def rsA100 = buscarReceita(edb10.edb10mes, edb10.edb10ano, gcEA);
			for(a100 in rsA100) {
				if(a100.getInteger("eaa0103clasReceita") == 1) {
					totalTributado += a100.getBigDecimal_Zero("eaa0103totDoc")
					
				}else if(a100.getInteger("eaa0103clasReceita") == 2) {
					totalNaotributado += a100.getBigDecimal_Zero("eaa0103totDoc")
					
				}else if(a100.getInteger("eaa0103clasReceita") == 3) {
					totalExportacao += a100.getBigDecimal_Zero("eaa0103totDoc")
					
				}else if(a100.getInteger("eaa0103clasReceita") == 4) {
					totalCumulativo += a100.getBigDecimal_Zero("eaa0103totDoc")
				}
			}
		}
		
		edb10.edb10rbTrib = round(totalTributado, 2);
		edb10.edb10rbNaoTrib = round(totalNaotributado, 2);
		edb10.edb10rbExp = round(totalExportacao, 2);
		edb10.edb10rbCumul = round(totalCumulativo, 2);

		/** 
		 * Contribuição Previdenciária sobre a Receita Bruta (P100)
		 */
		def totalReceita = 0;
		def totalRecSemAtividade = 0;
		def totalRecComAtividade = 0;

		for(empresa in empresas) {
			Long gcEA = selecionarGCPorEmpresa(empresa.aac10id, "EA");
			
			def mapP100 = new HashMap<Long, TableMap>();
			
			def rbEstab = 0;
			
			/**
			 * Receitas
			 */
			def rsP100 = buscarReceitaP100(edb10.edb10mes, edb10.edb10ano, cpoP100_Vl_Rec_Tot_Est, cpoP100_Vl_Rec_Ativ_Estab, cpoP100_Vl_Exc, cpoP100_Aliq_Cont, gcEA);
			for(p100 in rsP100) {
				if(p100.getLong("abg02id") == null) continue

				def key = p100.getLong("abg02id")

				totalReceita += p100.getBigDecimal("vlRecTotEst")
				rbEstab += p100.getBigDecimal("vlRecTotEst")

				def tm = new TableMap()
				if(mapP100.get(key) == null) {
					tm.put("vlRecTotEst", 0)
					tm.put("vlRecAtivEstab", 0)
					tm.put("vlExc", 0)

					mapP100.put(key, tm)
				}

				if(p100.getLong("abg02id") == null) {
					totalRecSemAtividade += p100.getBigDecimal("vlRecAtivEstab")
				}else {
					tm.put("vlRecTotEst", mapP100.get(key).getBigDecimal("vlRecTotEst") + p100.getBigDecimal("vlRecTotEst"))
					tm.put("vlRecAtivEstab", mapP100.get(key).getBigDecimal("vlRecAtivEstab") + p100.getBigDecimal("vlRecAtivEstab"))
					tm.put("vlExc", mapP100.get(key).getBigDecimal("vlExc") + p100.getBigDecimal("vlExc"))
					tm.put("aliqCont", p100.getBigDecimal("aliqCont"))
					tm.put("conta", p100.getLong("abc10id"))

					mapP100.put(key, tm)
					totalRecComAtividade += p100.getBigDecimal("vlRecAtivEstab")
				}
			}

			/**
			 * Devoluções
			 */
			rsP100 = buscarDevolucoesP100(edb10.edb10mes, edb10.edb10ano, cpoP100_Vl_Rec_Tot_Est, cpoP100_Vl_Rec_Ativ_Estab, cpoP100_Vl_Exc, cpoP100_Aliq_Cont, gcEA)
			for(p100 in rsP100) {
				if(p100.getLong("abg02id") == null) continue

				def key = p100.getLong("abg02id")

				def vlExc = mapP100.get(key).getBigDecimal("vlExc")
				def devolucao = (p100.getBigDecimal("vlRecAtivEstab") ?: 0) - (p100.getBigDecimal("vlExc") ?: 0)

				def tm = new TableMap()
				tm.put("vlExc", vlExc + devolucao)
				mapP100.put(key, tm)
			}

			/**
			 * Empresas que auferiram receita
			 */
			if(mapP100.size() > 0 || !rbEstab.equals(0)) {
				Edb1001 edb1001 = new Edb1001();
				edb1001.edb1001empresa = empresa;
				edb1001.edb1001rb = rbEstab;
				
				for(key in mapP100.keySet()) {
					Edb10011 edb10011 = new Edb10011();
					edb10011.edb10011adic = 0;
					edb10011.edb10011ativ = getSession().get(Abg02.class, key);
					edb10011.edb10011rb = mapP100.get(key).getBigDecimal("vlRecTotEst") ?: 0;
					edb10011.edb10011exc = mapP100.get(key).getBigDecimal("vlExc") ?: 0;
					edb10011.edb10011aliq = mapP100.get(key).getBigDecimal("aliqCont") ?: 0;

					def bc = edb10011.edb10011rb - edb10011.edb10011exc;
					bc = bc * edb10011.edb10011aliq;
					bc = bc / 100;
					edb10011.edb10011cprb = round(bc, 2);

					edb10011.edb10011cta = mapP100.get(key).getLong("conta") == null ? null : getSession().get(Abc10.class, mapP100.get(key).getLong("conta"));

					edb1001.addToEdb10011s(edb10011);
				}
				
				edb10.addToEdb1001s(edb1001);
			}
		}

		edb10.edb10rbAtivSemCP = totalRecSemAtividade
		edb10.edb10rbAtivCP = totalRecComAtividade
		edb10.edb10rbTotal = totalReceita
		
		put("edb10", edb10);
	}

	Long selecionarGCPorEmpresa(Long aac10id, String tabela) {
		def sql = " SELECT aac01id FROM Aac01 WHERE aac01id IN " +
				  " (SELECT aac1001gc FROM Aac1001 WHERE aac1001empresa = :aac10id AND UPPER(aac1001tabela) = :tabela) ";

		return getAcessoAoBanco().obterLong(sql, Parametro.criar("aac10id", aac10id), Parametro.criar("tabela", tabela));
	}

	List<Aac10> buscarMatrizEFiliais() {
		Aac10 empresaAtiva = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());
		Aac10 matriz = empresaAtiva.aac10matriz == null ? empresaAtiva : getAcessoAoBanco().obterEmpresa(empresaAtiva.aac10matriz.aac10id);

		List<Aac10> empresas = getAcessoAoBanco().buscarListaDeTableMap("SELECT * FROM Aac10 WHERE aac10matriz = :aac10id", Parametro.criar("aac10id", matriz.aac10id));
		empresas.add(matriz);
		
		return empresas;
	}

	List<TableMap> buscarReceita(Integer mes, Integer ano, Long gcEA) {
		def sql = " SELECT eaa01gc, eaa0103clasReceita, SUM(eaa0103totDoc) as eaa0103totDoc " +
				  " FROM Eaa01 INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " WHERE eaa01esMov = :mov " +
				  " AND " + Fields.month("abb01data") + " = :mes " +
				  " AND " + Fields.year("abb01data") + " = :ano " +
				  " AND eaa0103clasReceita > 0 " +
				  " AND eaa01cancData IS NULL " +
				  " AND eaa01iEfdContrib = 1 " +
				  " AND eaa01gc = :gcEA " +
				  " GROUP BY eaa01gc, eaa0103clasReceita"

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("mov", 1), Parametro.criar("mes", mes), Parametro.criar("ano", ano), Parametro.criar("gcEA", gcEA));
	}

	List<TableMap> buscarReceitaP100(Integer mes, Integer ano, String cpoVlRecTotEst, String cpoVlRecAtivEstab, String cpoVlExc, String cpoAliqCont, Long gc) {
		def select = new StringBuilder(", ")
		select.append("jGet(eaa0103json." + cpoAliqCont + ")::numeric AS aliqCont, ")
		select.append("SUM(jGet(eaa0103json." + cpoVlRecTotEst + ")::numeric) AS vlRecTotEst, ")
		select.append("SUM(jGet(eaa0103json." + cpoVlRecAtivEstab + ")::numeric) AS vlRecAtivEstab, ")
		select.append("SUM(jGet(eaa0103json." + cpoVlExc + ")::numeric) AS vlExc ")

		def sql = " SELECT abg02id, abg02codigo, abc10id " + select.toString() +
			 	  " FROM Eaa01 INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " LEFT JOIN Abg02 ON abg02id = eaa0103ativCP " +
				  " LEFT JOIN Abc10 ON abc10id = abg02cta " +
				  " WHERE eaa01esMov = :mov " +
				  " AND " + Fields.month("abb01data") + " = :mes " +
				  " AND " + Fields.year("abb01data") + " = :ano " +
				  " AND eaa01cancData IS NULL " +
				  " AND eaa01gc = :gc " +
				  " GROUP BY abg02id, abg02codigo, abc10id, jGet(eaa0103json." + cpoAliqCont + ") " +
				  " ORDER BY abg02codigo, abg02id"

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("mov", 1), Parametro.criar("mes", mes), Parametro.criar("ano", ano), Parametro.criar("gc", gc));
	}

	List<TableMap> buscarDevolucoesP100(Integer mes, Integer ano, String cpoVlRecTotEst, String cpoVlRecAtivEstab, String cpoVlExc, String cpoAliqCont, Long gc) {
		StringBuilder select = new StringBuilder(", ")
		select.append("jGet(eaa0103json." + cpoAliqCont + ")::numeric AS aliqCont, ")
		select.append("SUM(jGet(eaa0103json." + cpoVlRecTotEst + ")::numeric) AS vlRecTotEst, ")
		select.append("SUM(jGet(eaa0103json." + cpoVlRecAtivEstab + ")::numeric) AS vlRecAtivEstab, ")
		select.append("SUM(jGet(eaa0103json." + cpoVlExc + ")::numeric) AS vlExc ")

		def sql = " SELECT abg02id, abg02codigo, abc10id" + select.toString() +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " LEFT JOIN Abg02 ON abg02id = eaa0103ativCP " +
				  " LEFT JOIN Abc10 ON abc10id = abg02cta " +
				  " WHERE eaa01esMov = :mov " +
				  " AND " + Fields.month("eaa01esData") + " = :mes " +
				  " AND " + Fields.year("eaa01esData") + " = :ano " +
				  " AND eaa01cancData IS NULL " +
				  " AND eaa01gc = :gc " +
				  " GROUP BY abg02id, abg02codigo, abc10id, jGet(eaa0103json." + cpoAliqCont + ") " +
				  " ORDER BY abg02codigo, abg02id"

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("mov", 0), Parametro.criar("mes", mes), Parametro.criar("ano", ano), Parametro.criar("gc", gc))
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDEifQ==