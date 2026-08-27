/*
ULTIMA ALTERACAO: 31/07/2026	09:03
AUTOR: NAGYLA
 */

package Inova.formulas.scf.cob.rem

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
import br.com.multitec.utils.DateUtils
//import org.apache.commons.lang3.StringUtils

class SCF_LayoutBancoSicredi_CNAB_400 extends FormulaBase{
	public final static String PATTERN_DDMMYY = "ddMMyy";
	public final static String PATTERN_YYYYMMDD = "yyyyMMdd";
    private TableMap jsonAbf01

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_REMESSA_DE_COBRANCA;
	}

	@Override
	public void executar() {
		//**************************Fórmula gerada a partir do Manual de Soluções em Recebimentos CNAB400/CBR641 Fevereiro/2026 – Versão 3.0 ******************************
		TextFile txt = new TextFile();
		Integer numRemessa = get("numRemessa");
		LocalDate dataRemessa = get("dataRemessa");
		Integer movimento = get("movimento");
		Aac10 aac10 = get("aac10");
		Abf01 abf01 = get("abf01");
        jsonAbf01 = abf01 != null && abf01.abf01json != null ?  abf01.abf01json : new TableMap()
        List<Daa01> daa01s = get("daa01s");
		SCFService scfService = instanciarService(SCFService.class);

		selecionarAlinhamento("0004");

		/**
		 * HEADER
		 */

		txt.print("0");																									//001-001
		txt.print("1");																									//002-002
		txt.print("REMESSA");																							//003-009
		txt.print("01");																								//010-011
		txt.print("COBRANCA", 15);																		//012-026
		txt.print(jsonAbf01.getString("cod_identificador"), 5, '0', true);		//027-031
		txt.print(StringUtils.extractNumbers(aac10.getAac10ni()), 14, '0', true);		//032-045
		txt.print(StringUtils.space(31));																		//046-076
		txt.print("748");																								//077-079
		txt.print("SICREDI", 15);																			//080-094
		txt.print(MDate.date().format(PATTERN_YYYYMMDD));																//095-102
		txt.print(StringUtils.space(8));																		//103-110
		txt.print(numRemessa, 7);																				//111-117
		txt.print(StringUtils.space(273));																		//118-390
		txt.print("2.00");																								//391-394
		txt.print("000001");																							//395-400
		txt.newLine();


		//("Imprimindo Detalhe");
		int contador = 1;
		for (Daa01 daa01 : daa01s) {

			TableMap jsonDaa01 = daa01.daa01json;
			if(jsonDaa01 == null) jsonDaa01 = new TableMap();

			txt.print("1");																								//001-001
			txt.print("A");																								//002-002
			txt.print("A");																								//003-003
			txt.print("A");																								//004-004
			txt.print(StringUtils.space(12));																	//005-016
			txt.print("A");																								//017-017
			txt.print(jsonAbf01.getString("tp_desconto"));															//018-018
			txt.print(jsonAbf01.getString("tp_juros"));															//019-019
			txt.print(jsonAbf01.getString("tp_multa"));															//020-020
			txt.print(StringUtils.space(8));																	//021-028
			txt.print(StringUtils.space(8));																	//029-036
			txt.print(StringUtils.space(11));																	//037-047
			txt.print(daa01.daa01nossoNum.toString() + daa01.daa01nossoNumDV, 9);								//048-056
			txt.print(StringUtils.space(6));																	//057-062
			txt.print(MDate.date().format(PATTERN_YYYYMMDD));                                 			 				//063-070
			txt.print(StringUtils.space(1));																	//071-071
			txt.print("N");																								//072-072
			txt.print(StringUtils.space(1));																	//073-073
			txt.print("B");																								//074-074
			txt.print("0000");																							//075-078
			txt.print(StringUtils.space(4));																	//079-082
			txt.print("0000000000");												    								//083-092
			txt.print(StringUtils.ajustString(jsonAbf01.getString("aliq_multa").replaceAll("\\D", ""),4,"0",false));//093-096
			if(jsonDaa01.getString("multa") != "" && jsonDaa01.getString("multa") != null){//097-108
				txt.print(StringUtils.ajustString(jsonDaa01.getString("multa").replaceAll("\\D", ""),12,"0",true));
			}else{
				txt.print(StringUtils.ajustString("",12,"0",true));
			}
			txt.print("01");																							//109-110
			txt.print(StringUtils.ajustString(daa01.daa01id.toString(), 10));									//111-120
			txt.print(daa01.daa01dtVctoN.format(PATTERN_DDMMYY));														//121-126
			txt.print((daa01.daa01valor.multiply(100).intValue()), 13);											//127-139
			txt.print(StringUtils.space(2))																		//140-141
			txt.print(StringUtils.space(7));																	//142-148
			txt.print("A");																								//149-149
			txt.print("N");																								//150-150
			txt.print(DateUtils.formatDate(daa01.daa01central.abb01data, "ddMMyy"));//151-156
			txt.print(StringUtils.ajustString(jsonAbf01.getString("instr_protesto"),2,"0",true));	//157-158
			txt.print(StringUtils.ajustString(jsonAbf01.getString("dias_protesto"),2,"0",true));														//159-160
			if(jsonDaa01.getString("juros") != null ){//161-173
				txt.print(StringUtils.ajustString(jsonDaa01.getString("juros").replaceAll("\\D", ""),13,"0",true));
			}else{
				txt.print("0000000000000")
			}
			txt.print(DateUtils.formatDate(jsonDaa01.getDate("dt_lim_desc"), "ddMMyy"))	//174-179
			if(jsonDaa01.getString("desconto") != null){//180-192
				txt.print(StringUtils.ajustString(jsonDaa01.getString("desconto").replaceAll("\\D", ""),13,"0",true));
			}else{
				txt.print("0000000000000")
			}
			txt.print("0000000000000");																					//193-205
			txt.print("0000000000000");																					//206-218

			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", daa01.daa01central.abb01ent.abe01id);
			txt.print((abe01.abe01ti == 0) ? "2" : "1");																//219-219
			txt.print("0");																			     				//220-222
			txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14, '0', true);		//221-234
			txt.print(abe01.abe01nome, 40, true, true);										//235-274

			TableMap tm = buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(daa01.daa01central.abb01id);
			if(tm != null && tm.get("endereco") != null) {
				def numero = tm.get("numero").replaceAll(/[^0-9]/, '')
				txt.print(tm.get("numero") == null ? tm.get("endereco") : tm.get("endereco") + "," + tm.get("numero").replaceAll(/[^0-9]/, ''), 40, true, true); //275-314
			}

			txt.print("00000");																			     			//315-319
			txt.print("000000");																						//320-325
			txt.print(StringUtils.space(1));															    	//326-326
			txt.print(tm.get("cep") == null ? null : tm.get("cep"), 8, (char) '0', true);		//327-334
			txt.print("00000");																			   				//335-339
			txt.print(StringUtils.space(14));																   	//340-353
			txt.print(StringUtils.space(41));																   	//354-394
			txt.print(++contador, 6);															    		   	//395-400
			txt.newLine();
		}

		/**
		 * TRAILLER
		 */
		txt.print("9");																									//001-001
		txt.print("1");																									//002-002
		txt.print("748");																	 							//003-005
		txt.print(abf01.abf01json.getString("cod_identificador"), 5, '0', true);	//006-010
		txt.print(StringUtils.space(384));																		//011-394
		txt.print(++contador, 6);									  											//395-400
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