package multitec.formulas.scf.cob.rem

import java.time.LocalDate;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb0102
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abf01;
import sam.model.entities.da.Daa01;
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.server.samdev.formula.FormulaBase;
import sam.server.scf.service.SCFService;

class Layout_002_Bradesco extends FormulaBase{
	public final static String PATTERN_DDMMYY = "ddMMyy";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_REMESSA_DE_COBRANCA;
	}
	
	@Override
	public void executar() {
		//**************************Fórmula gerada no dia 26/03/2020******************************
		TextFile txt = new TextFile();
		Integer numRemessa = get("numRemessa");
		LocalDate dataRemessa = get("dataRemessa");
		Integer movimento = get("movimento");
		Aac10 aac10 = get("aac10");
		Abf01 abf01 = get("abf01");
		List<Daa01> daa01s = get("daa01s");
		SCFService scfService = instanciarService(SCFService.class);
		
		selecionarAlinhamento("0001");
		
		/**
    	 * HEADER
    	 */
    	txt.print("0");																						 //001-001
    	txt.print("1");																						 //002-002
    	txt.print("REMESSA");																				 //003-009
    	txt.print("01");																					 //010-011
    	txt.print("COBRANCA", 15);																			 //012-026
    	txt.print(abf01.abf01json.get(getCampo("0","cod_emp")), 20, '0', true);								 //027-046
    	txt.print(aac10.aac10rs, 30, true, true);														 	 //047-076
    	txt.print("237");																					 //077-079
    	txt.print("BRADESCO", 15);																			 //080-094
    	txt.print(MDate.date().format(PATTERN_DDMMYY));													 //095-100
    	txt.print(StringUtils.space(8));																	 //101-108
    	txt.print("MX");																					 //109-110
    	txt.print(numRemessa++, 7);																			 //111-117
    	txt.print(StringUtils.space(249));																	 //118-366
    	txt.print("DESC");																					 //367-370
    	txt.print(abf01.abf01json.get(getCampo("0","num_autorizacao")));									 //371-374
    	txt.print(abf01.abf01agencia, 5, '0', true);														 //375-379
    	txt.print(abf01.abf01conta, 7, '0', true);														 	 //380-386
    	txt.print(abf01.abf01digConta, 1);																 	 //387-387
    	txt.print(StringUtils.space(7));																	 //388-394
    	txt.print("000001");																				 //395-400
    	txt.newLine();
    	
    	/**
    	 * DETALHE
    	 */
    	int contador = 1;
		for(Daa01 daa01 : daa01s) {
    		txt.print("1");																					 //001-001
    		txt.print(aac10.aac10ti == 0 ? "02" : "01");													 //002-003
    		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);							 //004-017
    		txt.print("0000"); 																				 //018-021
    		txt.print("070");																				 //022-024
    		txt.print(abf01.abf01agencia, 5, '0', true);													 //025-029
    		txt.print(abf01.abf01conta, 7, '0', true);													 	 //030-036
    		txt.print(abf01.abf01digConta, 1);															 	 //037-037
    		txt.print(daa01.daa01id + ";" + movimento, 25);												 	 //038-062
    		txt.print(0, 8);																				 //063-070
    		
			txt.print(daa01.daa01nossoNum, 12); 															 //071-082 
			
    		txt.print(0, 10);																				 //083-092
    		txt.print(StringUtils.space(16));																 //093-108
    		txt.print(abf01.abf01json.get(getCampo("1","ident_ocorrencia")), 2, '0', true);					 //109-110
    		txt.print(seuNumero(daa01.daa01central.abb01num, daa01.daa01central.abb01parcela), 10);			 //111-120
    		txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYY));											 //121-126
    		txt.print(daa01.daa01valor.multiply(100).intValue(), 13);									 	 //127-139
    		txt.print(StringUtils.space(8));																 //140-147
    		txt.print(conteudoDinamicoParametro(daa01.daa01central.abb01tipo.aah01codigo), 2, '0', true);	 //148-149
    		txt.print(StringUtils.space(1));																 //150-150
    		txt.print(0, 68); 																				 //151-218
    		
			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);
    		txt.print(abe01.abe01ti == 0 ? "02" : "01");													 //219-220
    		txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, '0', true);							 //221-234
    		txt.print(abe01.abe01nome, 40, true, true);													 	 //235-274
    		
    		TableMap tm = buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(daa01.daa01central.abb01id);
			if(tm != null && tm.get("endereco") != null) {
				txt.print(tm.get("numero") == null ? tm.get("endereco") : tm.get("endereco") + "," + tm.get("numero"), 40, true, true); //275-314
				txt.print(StringUtils.space(12));                                                            //315-326
				txt.print(tm.get("cep") == null ? null : tm.get("cep"), 8, '0', true);						 //327-334
			}

			txt.print(StringUtils.space(60));																 //335-394
			txt.print(++contador, 6);																		 //395-400

			String texto = txt.getLastLine();
			txt.newLine();	
    	}
        
    	/**
         * TRAILLER
         */
        txt.print("9"); 																					 //001-001
        txt.print(StringUtils.space(393)); 																	 //002-394
        txt.print(++contador, 6); 																			 //395-400
        txt.newLine();
		
		put("txt", txt);
	}
	
	private String seuNumero(Integer num, String parcela) {
		String seuNumero = null;
		if(parcela == null || parcela.equals("0")) {
			 seuNumero = "" + num;
		}else {
			seuNumero = num + " " + parcela;
		}
		
		return seuNumero;
	}
	
	private String conteudoDinamicoParametro(String aah01codigo) {
		switch(aah01codigo) {
			case "2002":
				return "01";
			case "2003":
				return "02";
			case "0001":
				return "05";
			default:
				return "99";
		}
	}
	
	/**
	 * Buscar o endereço de cobrança a partir da central do documento financeiro
	 * @param abb01id Long Id da central do documento financeiro
	 * @return TableMap
	 */
	public TableMap buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(Long abb01id) {
		TableMap tm = new TableMap();
		if (abb01id == null) return null;
		Abb01 abb01 = getSession().get(Abb01.class, "abb01id, abb01ent", abb01id);
		if (abb01.getAbb01ent() != null) {
			Abe0101 abe0101 = getSession().createCriteria(Abe0101.class)
					.addFields("abe0101id, abe0101endereco, abe0101numero, abe0101cep, abe0101bairro, abe0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
					.addJoin(Joins.fetch("abe0101municipio").left(true).partial(true).alias("aag0201"))
					.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
					.addWhere(Criterions.eq("abe0101ent", abb01.getAbb01ent().getAbe01id()))
					.addWhere(Criterions.eq("abe0101cobranca", Abe0101.SIM))
					.setOrder("abe0101id ASC").setMaxResults(1).get(ColumnType.ENTITY);
			if (abe0101 != null) {
				tm.put("endereco", abe0101.getAbe0101endereco());
				tm.put("numero", abe0101.getAbe0101numero());
				tm.put("cep", abe0101.getAbe0101cep());
				tm.put("bairro", abe0101.getAbe0101bairro());
				tm.put("municipio", abe0101.getAbe0101municipio() != null ? abe0101.getAbe0101municipio().getAag0201nome() : "");
				tm.put("uf", abe0101.getAbe0101municipio() != null  && abe0101.getAbe0101municipio().getAag0201uf() != null ? abe0101.getAbe0101municipio().getAag0201uf().getAag02uf() : "");
			} else {
				abe0101 = getSession().createCriteria(Abe0101.class)
						.addFields("abe0101id, abe0101endereco, abe0101numero, abe0101cep, abe0101bairro, abe0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
						.addJoin(Joins.fetch("abe0101municipio").left(true).partial(true).alias("aag0201"))
						.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
						.addWhere(Criterions.eq("abe0101ent", abb01.getAbb01ent().getAbe01id()))
						.addWhere(Criterions.eq("abe0101principal", Abe0101.SIM))
						.setOrder("abe0101id ASC").setMaxResults(1).get(ColumnType.ENTITY);
				if (abe0101 != null) {
					tm.put("endereco", abe0101.getAbe0101endereco());
					tm.put("numero", abe0101.getAbe0101numero());
					tm.put("cep", abe0101.getAbe0101cep());
					tm.put("bairro", abe0101.getAbe0101bairro());
					tm.put("municipio", abe0101.getAbe0101municipio() != null ? abe0101.getAbe0101municipio().getAag0201nome() : "");
					tm.put("uf", abe0101.getAbe0101municipio() != null  && abe0101.getAbe0101municipio().getAag0201uf() != null ? abe0101.getAbe0101municipio().getAag0201uf().getAag02uf() : "");
				}
			}
		}
		
		Long eaa01idCentral = getSession().createCriteria(Abb0102.class).addFields("abb0102central")
				.addWhere(Criterions.eq("abb0102doc", abb01id)).setMaxResults(1).get(ColumnType.LONG);
		if (eaa01idCentral == null) return tm;

		Long eaa01id = getSession().createCriteria(Eaa01.class).addFields("eaa01id")
				.addWhere(Criterions.eq("eaa01central", eaa01idCentral)).addWhere(getSamWhere().getCritPadrao(Eaa01.class))
				.get(ColumnType.LONG);
		if (eaa01id == null) return tm;

		Eaa0101 eaa0101 = getSession().createCriteria(Eaa0101.class)
				.addFields("eaa0101id, eaa0101endereco, eaa0101numero, eaa0101cep, eaa0101bairro, eaa0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
				.addJoin(Joins.fetch("eaa0101municipio").left(true).partial(true).alias("aag0201"))
				.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02")).addWhere(Criterions.eq("eaa0101doc", eaa01id))
				.addWhere(Criterions.eq("eaa0101cobranca", Eaa0101.SIM)).setOrder("eaa0101id ASC").setMaxResults(1)
				.get(ColumnType.ENTITY);
		if (eaa0101 != null) {
			tm.put("endereco", eaa0101.getEaa0101endereco());
			tm.put("numero", eaa0101.getEaa0101numero());
			tm.put("cep", eaa0101.getEaa0101cep());
			tm.put("bairro", eaa0101.getEaa0101bairro());
			tm.put("municipio", eaa0101.getEaa0101municipio() != null ? eaa0101.getEaa0101municipio().getAag0201nome() : "");
			tm.put("uf", eaa0101.getEaa0101municipio() != null  && eaa0101.getEaa0101municipio().getAag0201uf() != null ? eaa0101.getEaa0101municipio().getAag0201uf().getAag02uf() : "");
		} else {
			eaa0101 = getSession().createCriteria(Eaa0101.class)
					.addFields("eaa0101id, eaa0101endereco, eaa0101numero, eaa0101cep, eaa0101bairro, eaa0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
					.addJoin(Joins.fetch("eaa0101municipio").left(true).partial(true).alias("aag0201"))
					.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
					.addWhere(Criterions.eq("eaa0101doc", eaa01id))
					.addWhere(Criterions.eq("eaa0101principal", Eaa0101.SIM)).setOrder("eaa0101id ASC").setMaxResults(1)
					.get(ColumnType.ENTITY);
			if (eaa0101 == null) return tm;
			tm.put("endereco", eaa0101.getEaa0101endereco());
			tm.put("numero", eaa0101.getEaa0101numero());
			tm.put("cep", eaa0101.getEaa0101cep());
			tm.put("bairro", eaa0101.getEaa0101bairro());
			tm.put("municipio", eaa0101.getEaa0101municipio() != null ? eaa0101.getEaa0101municipio().getAag0201nome() : "");
			tm.put("uf", eaa0101.getEaa0101municipio() != null  && eaa0101.getEaa0101municipio().getAag0201uf() != null ? eaa0101.getEaa0101municipio().getAag0201uf().getAag02uf() : "");
		}
		return tm;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDIifQ==