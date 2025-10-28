package multitec.formulas.sfp.holerite

import java.text.NumberFormat
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.criteria.client.ClientCriterion
import br.com.multitec.utils.dicdados.Parametro
import sam.dicdados.FormulaTipo
import sam.dicdados.Parametros
import sam.dto.sfp.SFP8001DtoExportar
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abh21
import sam.server.samdev.formula.FormulaBase

class SFP_RemessaHolerite extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVO_POR_TRABALHADOR;
	}

	@Override
	public void executar() {
		SFP8001DtoExportar sfp8001DtoExportar = get("sfp8001DtoExportar");
		List<TableMap> abh80s = sfp8001DtoExportar.abh80s;
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(getVariaveis().aac10.aac10id);
		ClientCriterion critAbb11 = sfp8001DtoExportar.critAbb11;
		
		String eveFeriasLiquida = getParametros(Parametros.FB_EVELIQFERIAS);
		String eveFeriasPagas = getParametros(Parametros.FB_EVEPAGTOFERIASDESC);
		String eveAbonoLiquido = getParametros(Parametros.FB_EVELIQABONO);
		String eveAbonoPago = getParametros(Parametros.FB_EVEPAGTOABONODESC);
		String eveAd13Liquido = getParametros(Parametros.FB_EVELIQADIANT13);
		String eveAd13Pago = getParametros(Parametros.FB_EVEPAGTO13SALDESC);
		String eveResLiquida = getParametros(Parametros.FB_EVERESLIQUIDA);
		String eveResPaga = getParametros(Parametros.FB_EVERESLIQUIDA);
		String eveFerNaoImpr = getParametros(Parametros.FB_EVELIQFERIAS);
		
		NumberFormat nb = NumberFormat.getNumberInstance();
		nb.setMinimumFractionDigits(2);
		nb.setGroupingUsed(false);
		
		TextFile txt = new TextFile();

		for(int i = 0; i < abh80s.size(); i++) {
			/**
			 * TIPO 1
			 */
			txt.print("01", 3);																											//001 a 003
			txt.print(abh80s.get(i).getString("abh80codigo"), 9);																			//004 a 012
			txt.print(abh80s.get(i).getString("abh80nome"), 45);																			//013 a 057
			txt.print(abh80s.get(i).getString("abh05nome"), 45);																			//058 a 102
			txt.print(abh80s.get(i).getDate("abh80dtadmis").format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 11);																//103 a 113
			txt.print(StringUtils.space(50));																							//114 a 163
			txt.print(StringUtils.space(2));																							//164 a 165
			txt.print(abh80s.get(i).getString("abb11codigo"), 6);																			//166 a 171
			txt.print(abh80s.get(i).getString("abh80pis"), 12);																				//172 a 183
			txt.print(abh80s.get(i).getString("abb11nome"), 50);																			//184 a 233
			txt.print(abh80s.get(i).getString("abh80cpf"), 12);																				//234 a 245
			txt.print(abh80s.get(i).getString("abh80rgNum"), 15);																				//246 a 260
			txt.print(StringUtils.extractNumbers(nb.format(abh80s.get(i).getBigDecimal("abh80salario").round(2))), 12, ' ', true);				//261 a 272
			txt.print(abh80s.get(i).getString("abh80endereco"), 55);																		//273 a 327
			txt.print(StringUtils.extractNumbers(abh80s.get(i).getString("abh80numero")), 6, '0', true);																			//328 a 333
			txt.print(abh80s.get(i).getString("abh80bairro"), 40);																			//334 a 373
			txt.print(abh80s.get(i).getString("aag0201nome"), 30);																			//374 a 403
			txt.print(abh80s.get(i).getString("aag02uf"), 3);																				//404 a 406
			txt.print(abh80s.get(i).getString("abh80cep"), 9);																				//407 a 415
			txt.print(sfp8001DtoExportar.data.format(DateTimeFormatter.ofPattern("MM/yyyy")), 8);																					//416 a 423
			txt.print(abh80s.get(i).getString("abf01nome"), 25);																			//424 a 448
			txt.print(abh80s.get(i).getString("abh80bcoAgencia"), 5);																			//449 a 453
			txt.print(abh80s.get(i).getString("abh80bcoConta"), 12);																			//454 a 465
			txt.print(StringUtils.space(10));																							//466 a 475
			txt.print(aac10.aac10rs, 60);																							//476 a 535
			txt.newLine();

			/**
			 * TIPO 2
			 */
			def totalRend = 0;
			List<TableMap> rsFb011sRend = getHoleriteUtils().findDadosFba01011sByExportarHoleriteParaTxt(abh80s.get(i).getLong("abh80id"), critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 0, eveFerNaoImpr);
			if(rsFb011sRend != null && rsFb011sRend.size() > 0) {
				for(int j = 0; j < rsFb011sRend.size(); j++) {
					txt.print("02", 3);																									//001 a 003
					txt.print(rsFb011sRend.get(j).getString("abh21codigo"), 5);																//004 a 008
					txt.print(rsFb011sRend.get(j).getString("abh21nome"), 40);																//009 a 048
					txt.print(nb.format(rsFb011sRend.get(j).getBigDecimal("ref").round(2)), 10, ' ', true);									//049 a 058
					txt.print(nb.format(rsFb011sRend.get(j).getBigDecimal("valor").round(2)), 15, ' ', true);									//059 a 073
					txt.print(StringUtils.space(15));																					//074 a 088
					txt.newLine();
					totalRend = totalRend + rsFb011sRend.get(j).getBigDecimal("valor");
				}
			}

			def totalDesc = 0;
			List<TableMap> rsFb011sDesc = getHoleriteUtils().findDadosFba01011sByExportarHoleriteParaTxt(abh80s.get(i).getLong("abh80id"), critAbb11, sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos, 1, eveFerNaoImpr);
			if(rsFb011sDesc != null && rsFb011sDesc.size() > 0) {
				for(int j = 0; j < rsFb011sDesc.size(); j++) {
					txt.print("02", 3);																									//001 a 003
					txt.print(rsFb011sDesc.get(j).getString("abh21codigo"), 5);																//004 a 008
					txt.print(rsFb011sDesc.get(j).getString("abh21nome"), 40);																//009 a 048
					txt.print(nb.format(rsFb011sDesc.get(j).getBigDecimal("ref").round(2)), 10, ' ', true);									//049 a 058
					txt.print(StringUtils.space(15));																					//059 a 073
					txt.print(nb.format(rsFb011sDesc.get(j).getBigDecimal("valor")), 15, ' ', true);									//074 a 088
					txt.newLine();
					totalDesc = totalDesc + rsFb011sDesc.get(j).getBigDecimal("valor");
				}
			}

			if(sfp8001DtoExportar.comporTiposDeCalculos.contains(3)) {
				//Verifica se existe evento de férias líquida para zerar com férias pagas.
				
				Abh21 abh21FeriasPagas = buscarPorChaveUnica(eveFeriasPagas);
				TableMap valorFerias = getHoleriteUtils().findFba01011ValorEventoParaZerarFeriasAndRescisaoHolerite(critAbb11, abh80s.get(i).getLong("abh80id"), sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, 3, eveFeriasLiquida, eveFerNaoImpr);
				if(abh21FeriasPagas != null && valorFerias != null && valorFerias.getBigDecimal_Zero("valor").compareTo(new BigDecimal(0)) > 0) {
					txt.print("02", 3);																									//001 a 003
					txt.print(abh21FeriasPagas.abh21codigo, 5);																//004 a 008
					txt.print(abh21FeriasPagas.abh21nome, 40);																//009 a 048
					txt.print(nb.format(valorFerias.getBigDecimal_Zero("ref").round(2)), 10, ' ', true);									//049 a 058
					txt.print(StringUtils.space(15));																					//074 a 088
					txt.print(nb.format(valorFerias.getBigDecimal_Zero("valor").round(2)), 15, ' ', true);									//059 a 073
					txt.newLine();
					totalDesc = totalDesc + valorFerias.getBigDecimal_Zero("valor");
				}
	
				//Verifica se existe evento de abono líquido para zerar com abono pago.
				Abh21 abh21AbonoPago = buscarPorChaveUnica(eveAbonoPago);
				TableMap valorAbono = getHoleriteUtils().findFba01011ValorEventoParaZerarFeriasAndRescisaoHolerite(critAbb11, abh80s.get(i).getLong("abh80id"), sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, 3, eveAbonoLiquido, null);
				if(abh21AbonoPago != null && valorAbono != null && valorAbono.getBigDecimal_Zero("valor").compareTo(new BigDecimal(0)) > 0) {
					txt.print("02", 3);																									//001 a 003
					txt.print(abh21AbonoPago.abh21codigo, 5);																//004 a 008
					txt.print(abh21AbonoPago.abh21nome, 40);																//009 a 048
					txt.print(nb.format(valorAbono.getBigDecimal_Zero("ref").round(2)), 10, ' ', true);									//049 a 058
					txt.print(StringUtils.space(15));																					//074 a 088
					txt.print(nb.format(valorAbono.getBigDecimal_Zero("valor").round(2)), 15, ' ', true);									//059 a 073
					txt.newLine();
					totalDesc = totalDesc + valorAbono.getBigDecimal_Zero("valor");
				}
	
				//Verifica se existe evento de adiantamento de 13º salário líquido para zerar com adiant. 13º pago.
				Abh21 abh21Ad13Pago = buscarPorChaveUnica(eveAd13Pago);
				TableMap valorAd13 = getHoleriteUtils().findFba01011ValorEventoParaZerarFeriasAndRescisaoHolerite(critAbb11, abh80s.get(i).getLong("abh80id"), sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, 3, eveAd13Liquido, null);
				if(abh21Ad13Pago != null && valorAd13 != null && valorAd13.getBigDecimal_Zero("valor").compareTo(new BigDecimal(0)) > 0) {
					txt.print("02", 3);																									//001 a 003
					txt.print(abh21Ad13Pago.abh21codigo, 5);																//004 a 008
					txt.print(abh21Ad13Pago.abh21nome, 40);																//009 a 048
					txt.print(nb.format(valorAd13.getBigDecimal_Zero("ref").round(2)), 10, ' ', true);									//049 a 058
					txt.print(StringUtils.space(15));																					//074 a 088
					txt.print(nb.format(valorAd13.getBigDecimal_Zero("valor").round(2)), 15, ' ', true);									//059 a 073
					txt.newLine();
					totalDesc = totalDesc + valorAd13.getBigDecimal_Zero("valor");
				}
			}

			def valorBaseINSS = 0;
			List<TableMap> rsValores = getHoleriteUtils().findDadosFba01011sCAEByRemessaHoleriteTxt(abh80s.get(i).getLong("abh80id"), critAbb11, "6505", sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(rsValores != null && rsValores.size() > 0) {
				for(int j = 0; j < rsValores.size(); j++) {

					if(rsValores.get(j).getBigDecimal("abh2101cvr") == 0) {
						valorBaseINSS = valorBaseINSS + rsValores.get(j).getBigDecimal("fba01011valor");
					}else {
						valorBaseINSS = valorBaseINSS - rsValores.get(j).getBigDecimal("fba01011valor");
					}
				}
			}
			
			rsValores.clear();
			def valorBaseFGTS = 0;
			rsValores = getHoleriteUtils().findDadosFba01011sCAEByRemessaHoleriteTxt(abh80s.get(i).getLong("abh80id"), critAbb11, "6501", sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(rsValores != null && rsValores.size() > 0) {
				for(int j = 0; j < rsValores.size(); j++) {

					if(rsValores.get(j).getBigDecimal("abh2101cvr") == 0) {
						valorBaseFGTS = valorBaseFGTS + rsValores.get(j).getBigDecimal("fba01011valor");
					}else {
						valorBaseFGTS = valorBaseFGTS - rsValores.get(j).getBigDecimal("fba01011valor");
					}
				}
			}
			
			rsValores.clear();
			def valorFGTS = 0;
			rsValores = getHoleriteUtils().findDadosFba01011sCAEByRemessaHoleriteTxt(abh80s.get(i).getLong("abh80id"), critAbb11, "6502", sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(rsValores != null && rsValores.size() > 0) {
				for(int j = 0; j < rsValores.size(); j++) {

					if(rsValores.get(j).getBigDecimal("abh2101cvr") == 0) {
						valorFGTS = valorFGTS + rsValores.get(j).getBigDecimal("fba01011valor");
					}else {
						valorFGTS = valorFGTS - rsValores.get(j).getBigDecimal("fba01011valor");
					}
				}
			}
			
			rsValores.clear();
			def valorBaseIRRF = 0;
			rsValores = getHoleriteUtils().findDadosFba01011sCAEByRemessaHoleriteTxt(abh80s.get(i).getLong("abh80id"), critAbb11, "6504", sfp8001DtoExportar.dataInicial, sfp8001DtoExportar.dataFinal, sfp8001DtoExportar.comporTiposDeCalculos);
			if(rsValores != null && rsValores.size() > 0) {
				for(int j = 0; j < rsValores.size(); j++) {

					if(rsValores.get(j).getBigDecimal("abh2101cvr") == 0) {
						valorBaseIRRF = valorBaseIRRF + rsValores.get(j).getBigDecimal("fba01011valor");
					}else {
						valorBaseIRRF = valorBaseIRRF - rsValores.get(j).getBigDecimal("fba01011valor");
					}
				}
			}

			/**
			 * TIPO 3
			 */
			txt.print("03", 3);																											//001 a 003
			txt.print(nb.format(totalRend), 16, ' ', true);																	//004 a 019
			txt.print(nb.format(totalDesc), 16, ' ', true);																	//020 a 035
			def valor = totalRend - totalDesc;
			txt.print(nb.format(valor), 16, ' ', true);												//036 a 051
			txt.print(nb.format(valorBaseINSS), 16, ' ', true);																//052 a 067
			txt.print(nb.format(valorBaseFGTS), 16, ' ', true);																//068 a 083
			txt.print(nb.format(valorFGTS), 16, ' ', true);																	//084 a 099
			txt.print(nb.format(valorBaseIRRF), 16, ' ', true);																//100 a 115
			txt.newLine();

			/**
			 * TIPO 4
			 */
//			for(String mensagem : mensagens) {
//				if(mensagem == null) continue;
//
//				txt.print("04", 3);																										//001 a 003
//				txt.print(mensagem, 80);																								//004 a 083
//				txt.newLine();
//			}
		}
		
		put("txt", txt);
	}
	
	/**Método Diverso
	 * @return 	String (Parâmetro do SAM)
	 */
	private String getParametros(Parametro param) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", "FB"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.get();
		
		String conteudo = null;
		if(aba01 != null) {
			conteudo = aba01.getAba01conteudo();
		}
		return conteudo;
	}
	
	/**
	 * Método buscar eventos por chave
	 * @return Abh21 Dados do Banco
	 */
	public Abh21 buscarPorChaveUnica(String Abh21codigo){
		if(Abh21codigo == null) return null;
		
		String sql = "SELECT * FROM Abh21 AS abh21 WHERE UPPER(abh21.abh21codigo) = UPPER(:P0) " + getSamWhere().getWherePadrao("AND", Abh21.class);
		Query query = getSession().createQuery(sql);
		query.setParameter("P0", Abh21codigo);
		
		return (Abh21) query.getUniqueResult(ColumnType.ENTITY);
	
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTkifQ==