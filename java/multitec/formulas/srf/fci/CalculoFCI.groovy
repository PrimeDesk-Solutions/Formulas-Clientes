package multitec.formulas.srf.fci

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.core.criteria.ClientCriteriaConvert
import sam.dicdados.FormulaTipo
import sam.dicdados.Parametros
import sam.dto.cgs.CGSListarComponentesComposicaoDto
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aah02
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm12
import sam.model.entities.ab.Abp20
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eab01
import sam.model.entities.ea.Eab0101
import sam.model.entities.ea.Eab01011
import sam.server.cas.service.CASService
import sam.server.cas.service.EspecificacaoService
import sam.server.cas.service.ParametroService
import sam.server.cgs.service.CGSService
import sam.server.samdev.formula.FormulaBase

class CalculoFCI extends FormulaBase {
	
	private LocalDate dataCalculo;
	private String bom;
	private ClientCriterion whereItem;
	private LocalDate dtIniEnt;
	private LocalDate dtFinEnt;
	private LocalDate dtIniSai;
	private LocalDate dtFinSai;
	
	private String fciCampoVlrImp;
	private String fciCampoQtdImp;
	private String fciCampoVlrSai;
	private String fciCampoQtdSai;
	
	private int tipoSaida = 0; //Tipo de saída: 0-Internas e Interestaduais 1-Somente interestaduais e 2-Interestaduais, e na inexistência as internas
	private boolean considEntRetro = false; //Caso não haja compra no período, considerar entradas retroativas
	private boolean consEntRetroSomenteUltMes = false; //true = Entradas com data menor que a data inicial - false = Entradas somente do último mês que ocorreu compra
	private boolean considSaiRetro = false; // Caso não haja vendas no período, considerar saídas retroativas
	private boolean consSaiRetroSomenteUltMes = false; //true = Saídas com data menor que a data inicial - false = Saídas somente do último mês que ocorreu compra
	private boolean considMediaUnitAPartirDaUltimaFCI = true; //Fazer média dos unitários a partir da última FCI do item
	private boolean gerarFCIcomCIZero = true; //Gerar FCI quando CI for igual a zero
	private boolean atualizarOrigemMercadoria = true; //Atualizar campo 'Origem da mercadoria - ICMS e CSOSN (A)' no cadastro do item de acordo com o cálculo CI %
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_CALCULO_DA_FCI;
	}

	@Override
	public void executar() {
		dataCalculo = get("dataCalculo");
		bom = get("bom");
		whereItem = get("whereItem")
		dtIniEnt = get("dtIniEnt");
		dtFinEnt = get("dtFinEnt");
		dtIniSai = get("dtIniSai");
		dtFinSai = get("dtFinSai");
		
		selecionarAlinhamento("0015");
		validarAlinhamento("0015");
		
		Criterion critItem = ClientCriteriaConvert.convertCriterion(whereItem);
		CGSService cgsService = instanciarService(CGSService);
		
		//Busca especificação da FCI
		ParametroService parametroService = instanciarService(ParametroService);
		String especCpoLivreFCI = parametroService.buscarConteudoDoParametro(Parametros.EA_ESPECCPOLIVREFCI, true, true);
		
		//Busca campos da especificação da FCI
		EspecificacaoService especificacaoService = instanciarService(EspecificacaoService);
		List<Aah02> aah02s = especificacaoService.buscarCamposEspecificacao(especCpoLivreFCI);
		
		CASService casService = instanciarService(CASService);
		
		boolean incluiuCalculo = false;
		
		String uf = obterUFEmpresa();
		
		List<TableMap> listProdutos = buscarProdutosComBOM(critItem, bom);
		if(Utils.isEmpty(listProdutos)) throw new ValidacaoException("Nenhum produto/composição foi selecionado a partir do BOM informado para cálculo das FCI's.");
		
		for(TableMap tmProduto : listProdutos) {
			Long abm01idPrincipal = tmProduto.getLong("abm01id");
						
			Map<Long, BigDecimal> mapComposicao = cgsService.obterIdsComponentesComposicao(new CGSListarComponentesComposicaoDto(abm01idPrincipal, bom, null, 1.0, true, false));
			if(mapComposicao == null || mapComposicao.size() == 0) continue;
			
			List<Long> abm01idsImportados = buscarIdsItensImportados(mapComposicao.keySet());
			if(Utils.isEmpty(abm01idsImportados)) continue;
			
			//Seleciona o último cálculo FCI
			def ciUltCalc = 0.0;
			def unitSaidaUltCalc = 0.0;
			Eab0101 eab0101ultCalc = buscarUltimoCalculo(abm01idPrincipal);
			if(eab0101ultCalc != null) {
				ciUltCalc = eab0101ultCalc.getEab0101ci();
				unitSaidaUltCalc = considMediaUnitAPartirDaUltimaFCI ? eab0101ultCalc.getEab0101unitSai() : 0.0;
			}
			
			/**
			 * Importações (Entradas) dos componentes importados
			 */
			//Primeiro:
			//Inclui os itens importados com o respectivo eab01011vmu constante no último cálculo
			Map<Long, TableMap> mapItensImp = new HashMap<>();
			for(Long abm01idComponente : abm01idsImportados) {
				TableMap tm = new TableMap();
				tm.put("qtde", 0.0);
				tm.put("valor", 0.0);
				tm.put("qtdeComp", mapComposicao.get(abm01idComponente));
				
				def ultimoVMU = 0.0;
				if(considMediaUnitAPartirDaUltimaFCI) ultimoVMU = buscarUltimoVMU(abm01idPrincipal, abm01idComponente, dataCalculo);
				
				mapItensImp.put(abm01idComponente, tm);
			}
			
			//Segundo:
			//Verifica se teve entradas no período
			for(Long abm01idComponente : abm01idsImportados) {
				TableMap tmEntradas = selecionarEntradas(considEntRetro, abm01idPrincipal, abm01idComponente, dtIniEnt, dtFinEnt, consEntRetroSomenteUltMes);
				if(tmEntradas != null) {
					def qtde = tmEntradas.getBigDecimal_Zero("qtde");
					def valor = tmEntradas.getBigDecimal_Zero("valor");
					def unit = qtde == 0.0 ? 0.0 : valor / qtde;
					unit = round(unit, 6);
					
					TableMap tm = mapItensImp.get(abm01idComponente);
					def qtdeComp = tm.getBigDecimal_Zero("qtde");
					def ultimoVMU = tm.getBigDecimal_Zero("vmu");
					def divisor = ultimoVMU != 0.0 && unit != 0.0 ? 2.0 : 1.0;
					
					def vmu = unit * qtdeComp;
					vmu = vmu + ultimoVMU;
					vmu = round(divisor, 6);
					
					tm.put("qtde", qtde);
					tm.put("valor", valor);
					tm.put("vmu", vmu);
				}
			}
			
			def totalVMUImp = 0.0;
			for(Long abm01id : mapItensImp.keySet()) {
				TableMap tm = mapItensImp.get(abm01id);
				totalVMUImp = totalVMUImp + tm.getBigDecimal_Zero("vmu");
			}
			totalVMUImp = round(totalVMUImp, 2);
			
			/**
			 * Vendas (Saídas) do item principal
			 */
			def totalVlrSaidas = 0.0;
			def totalQtdSaidas = 0.0;
			
			TableMap tmSaidas = selecionarSaidas(considSaiRetro, abm01idPrincipal, dtIniSai, dtFinSai, uf, tipoSaida, consSaiRetroSomenteUltMes);
			if(tmSaidas != null) {
				totalVlrSaidas = tmSaidas.getBigDecimal_Zero("valor");
				totalQtdSaidas = tmSaidas.getBigDecimal_Zero("qtde");
			}
			totalQtdSaidas = round(totalQtdSaidas, 6);
			totalVlrSaidas = round(totalVlrSaidas, 2);
			
			//Soma o valor unitário das saídas (ea601unitsaidas) do último cálculo ao unitário encontrado para fazer a média
			def unitSai = totalQtdSaidas == 0.0 ? 0.0 : totalVlrSaidas / totalQtdSaidas;
			def divisor = unitSai != 0.0 && unitSaidaUltCalc != 0.0 ? 2.0 : 1.0;
			unitSai = unitSai + unitSaidaUltCalc;
			unitSai = unitSai / divisor;
			unitSai = round(unitSai, 2);
			
			/**
			 * FCI - Ficha de Conteúdo de Importação
			 *
			 * A FCI deverá ser gerada somente se a diferença do CI atual para o CI do último cálculo for de 5% para mais ou para menos
			 */
			if(totalVMUImp != 0.0 && unitSai != 0.0) {
				if(totalVMUImp > unitSai) {
					throw new ValidacaoException("O unitário dos itens importados é superior ao unitário médio das saídas do item " + tmProduto.getString("abm01codigo") + ".\nVerifique a fórmula e/ou os valores contidos nos documentos que estão sendo considerados no cálculo.");
				}
			}
			
			def ciAtual = unitSai == 0.0 ? 0.0 : ((totalVMUImp / unitSai) * 100);
			ciAtual = round(ciAtual, 2);
			
			if(!gerarFCIcomCIZero && ciAtual == 0.0) continue;
			
			def fatorCI = (ciUltCalc * 5) / 100;
			fatorCI = round(fatorCI, 6);
			
			def diferencaCI = ciAtual - ciUltCalc;
			
			if(diferencaCI.abs() >= fatorCI) {
				Eab01 eab01 = session.get(Eab01.class, Criterions.and(Criterions.eq("eab01item", abm01idPrincipal), getSamWhere().getCritPadrao(Eab01.class)));
				if(eab01 == null) {
					eab01 = new Eab01();
					eab01.eab01item = Abm01.createById(abm01idPrincipal);
					samWhere.setDefaultValues(eab01);	
				}
				
				Eab0101 eab0101 = session.get(Eab0101.class, Criterions.and(Criterions.eq("eab0101fci", eab01.eab01id), Criterions.eq("eab0101data", dataCalculo)));
				if(eab0101 != null) continue; //Se o cálculo já existir, desconsiderá-lo
				
				eab0101 = new Eab0101();
				eab0101.eab0101fci = eab01;
				eab0101.eab0101data = dataCalculo;
				
				eab0101.eab0101codigo = tmProduto.getString("abm01codigo");
				eab0101.eab0101descr = tmProduto.getString("abm01descr");
				eab0101.eab0101ncm = buscarNCMDoItem(abm01idPrincipal);
				eab0101.eab0101ean = tmProduto.getString("abm01gtin");
				eab0101.eab0101umv = buscarUMVDoItem(abm01idPrincipal);
				eab0101.eab0101status = Eab0101.STATUS_A_ENVIAR;
				
				eab0101.eab0101unitImp = totalVMUImp;
				eab0101.eab0101unitSai = unitSai;
				eab0101.eab0101ci = ciAtual;
				eab0101.eab0101qtdSai = totalQtdSaidas;
				eab0101.eab0101vlrSai = totalVlrSaidas;
				
				eab0101.eab0101json = casService.comporJson(aah02s);
				
				if(mapItensImp != null && mapItensImp.size() > 0) {
					for(Long abm01id : mapItensImp.keySet()) {
						TableMap tm = mapItensImp.get(abm01id);
						
						Eab01011 eab01011 = new Eab01011();
						eab01011.eab01011calc = eab0101;
						eab01011.eab01011item = Abm01.createById(abm01id);
						eab01011.eab01011qtde = round(tm.getBigDecimal_Zero("qtde"), 6);
						eab01011.eab01011valor = round(tm.getBigDecimal_Zero("valor"), 2);
						eab01011.eab01011vmu = round(tm.getBigDecimal_Zero("vmu"), 6);
						eab01011.eab01011qtdComp = round(tm.getBigDecimal_Zero("qtdeComp"), 6);
						eab0101.addToEab01011s(eab01011);
					}
				}
				
				//Se for SP, mantém a última FCI enviada conforme faixa de CI, até 40%, de 40,01 a 70% e maior que 70%
				if(uf.equalsIgnoreCase("SP")) {
					Eab0101 eab0101Ult = buscarUltimaFCIPorFaixaCI(abm01idPrincipal, ciAtual);
					if(eab0101Ult != null) {
						eab0101.eab0101status = eab0101Ult.eab0101status;
						eab0101.eab0101numFCI = eab0101Ult.eab0101numFCI;
						eab0101.eab0101protocolo = eab0101Ult.eab0101protocolo;
					}
				}
				
				eab01.addToEab0101s(eab0101);
				session.persist(eab01);
				
				setarOrigemMercadoriaNoItemByCI(atualizarOrigemMercadoria, abm01idPrincipal, ciAtual);
				
				incluiuCalculo = true;
			}else {
				//Se não houver variação de 5%, gerar um novo cálculo com os dados da FCI anterior, exceto se o cálculo já existir
				if(eab0101ultCalc == null) continue;
				
				Eab01 eab01 = eab0101ultCalc.eab0101fci;
				Eab0101 eab0101 = session.get(Eab0101.class, Criterions.and(Criterions.eq("eab0101fci", eab01.eab01id), Criterions.eq("eab0101data", dataCalculo)));
				
				if(eab0101 != null) continue;
				
				eab0101 = new Eab0101();
				eab0101.eab0101fci = eab01;
				eab0101.eab0101data = dataCalculo;
				
				eab0101.eab0101codigo = eab0101ultCalc.eab0101codigo;
				eab0101.eab0101descr = eab0101ultCalc.eab0101descr;
				eab0101.eab0101ncm = eab0101ultCalc.eab0101ncm;
				eab0101.eab0101ean = eab0101ultCalc.eab0101ean;
				eab0101.eab0101umv = eab0101ultCalc.eab0101umv;
				eab0101.eab0101status = eab0101ultCalc.eab0101status;
				
				eab0101.eab0101unitImp = eab0101ultCalc.eab0101unitImp;
				eab0101.eab0101unitSai = eab0101ultCalc.eab0101unitSai;
				eab0101.eab0101ci = eab0101ultCalc.eab0101ci;
				eab0101.eab0101qtdSai = eab0101ultCalc.eab0101qtdSai;
				eab0101.eab0101vlrSai = eab0101ultCalc.eab0101vlrSai;
				eab0101.eab0101json = eab0101ultCalc.eab0101json;
				
				List<Eab01011> eab01011sUltCalc = buscarItensDoCalculo(eab0101ultCalc.eab0101id);
				if(!Utils.isEmpty(eab01011sUltCalc)) {
					for(Eab01011 eab01011Anterior : eab01011sUltCalc) {
						Eab01011 eab01011 = new Eab01011();
						eab01011.eab01011calc = eab0101;
						eab01011.eab01011item = eab01011Anterior.eab01011item;
						eab01011.eab01011qtde = eab01011Anterior.eab01011qtde;
						eab01011.eab01011valor = eab01011Anterior.eab01011valor;
						eab01011.eab01011vmu = eab01011Anterior.eab01011vmu;
						eab01011.eab01011qtdComp = eab01011Anterior.eab01011qtdComp;
						eab0101.addToEab01011s(eab01011);
					}
				}
				
				eab01.addToEab0101s(eab0101);
				session.persist(eab01);
				
				setarOrigemMercadoriaNoItemByCI(atualizarOrigemMercadoria, abm01idPrincipal, eab0101ultCalc.eab0101ci);
				
				incluiuCalculo = true;
			}
		}
		
		if(!incluiuCalculo) throw new ValidacaoException("Não houve inclusão de novos cálculos de FCI.");
	}
	
	private void validarAlinhamento(String codigoAlinhamento) {
		fciCampoVlrImp = getCampo("0", "FCICAMPOVLRIMP");
		if(fciCampoVlrImp == null) throw new ValidacaoException("Não foi encontrado o campo FCICAMPOVLRIMP no alinhamento " + codigoAlinhamento + ".");
		
		fciCampoQtdImp = getCampo("0", "FCICAMPOQTDIMP");
		if(fciCampoQtdImp == null) throw new ValidacaoException("Não foi encontrado o campo FCICAMPOVLRSAI no alinhamento " + codigoAlinhamento + ".");
		
		fciCampoVlrSai = getCampo("0", "FCICAMPOVLRSAI");
		if(fciCampoVlrSai == null) throw new ValidacaoException("Não foi encontrado o campo FCICAMPOQTDIMP no alinhamento " + codigoAlinhamento + ".");
		
		fciCampoQtdSai = getCampo("0", "FCICAMPOQTDSAI");
		if(fciCampoQtdSai == null) throw new ValidacaoException("Não foi encontrado o campo FCICAMPOQTDSAI no alinhamento " + codigoAlinhamento + ".");
	}
	
	private String obterUFEmpresa() {
		Aag0201 aag0201 = variaveis.aac10.getAac10municipio();
		if(aag0201 == null) throw new ValidacaoException("Necessário informar o munícipio da empresa ativa no cadastro de empresa.");
		
		aag0201 = getSession().createCriteria(Aag0201.class)
			.addFields("aag0201id, aag0201uf, aag02id, aag02uf")
			.addJoin(Joins.part("aag0201uf"))
			.addWhere(Criterions.eq("aag0201id", aag0201.getIdValue()))
			.get();
			
		return aag0201.getAag0201uf().getAag02uf();
	}
	
	private List<TableMap> buscarProdutosComBOM(Criterion critItem, String bom) {
		return getSession().createQuery(" SELECT abm01id, abm01codigo, abm01descr, abm01gtin ",
										" FROM Abp20 ",
										" INNER JOIN Abm01 ON abp20item = abm01id ",
										" WHERE abp20bomCodigo = :bom ",
										critItem.getSQL("AND", session),
										getSamWhere().getWherePadrao("AND", Abp20.class),
										" ORDER BY abm01codigo")
							.setParameter("bom", bom)
							.setCriterions(critItem)
							.getListTableMap();
	}
	
	private List<Long> buscarIdsItensImportados(Set<Long> abm01ids) {		
		return getSession().createQuery(" SELECT abm01id ",
										" FROM Abm01 ",
										" INNER JOIN Abm0101 ON abm0101item = abm01id ",
										" INNER JOIN Abm12 ON abm0101fiscal = abm12id ",
										" WHERE abm01id IN (:abm01ids) ",
										" AND abm12cstA IN (:cst) ",
										" AND abm0101empresa = :aac10id ",
										getSamWhere().getWherePadrao("AND", Abm01.class))
						   .setParameters("abm01ids", abm01ids,
							   			  "aac10id", obterEmpresaAtiva().aac10id,
										  "cst", [1, 2, 6, 7, 8])
						   .getList(ColumnType.LONG);
	}
	
	private Eab0101 buscarUltimoCalculo(Long abm01id) {
		return getSession().createQuery(" SELECT * ",
										" FROM Eab0101 ",
										" INNER JOIN Eab01 ON eab0101fci = eab01id ",
										" WHERE eab01item = :abm01id ",
										getSamWhere().getWherePadrao("AND", Eab01.class),
										" ORDER BY eab0101data DESC ")
						   .setParameter("abm01id", abm01id)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private BigDecimal buscarUltimoVMU(Long abm01idFci, Long abm01idImp, LocalDate dataCalc) {
		Query query = getSession().createQuery(" SELECT eab01011vmu ",
											   " FROM Eab01011 ",
											   " INNER JOIN Eab0101 ON eab01011calc = eab0101id ",
											   " INNER JOIN Eab01 ON eab0101fci = eab01id ",
											   " WHERE eab01item = :abm01idFci ",
											   " AND eab01011item = :abm01idImp ",
											   " AND eab0101data < :dataCalc ",
											   getSamWhere().getWherePadrao("AND", Eab01.class),
											   " ORDER BY eab0101data DESC ")
								  .setParameters("abm01idFci", abm01idFci,
									  			 "abm01idImp", abm01idImp,
												 "dataCalc", dataCalc)
								  .setMaxResult(1);
		
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result == null ? 0.0 : result;
	}
	
	private TableMap selecionarEntradas(boolean considEntRetro, Long idProduto, Long idComponente, LocalDate dtIniEnt, LocalDate dtFinEnt, boolean consEntRetroSomenteUltMes) {
		TableMap tmEntradas = buscarSomaEntradasFCIPorItemQtde(idComponente, dtIniEnt, dtFinEnt, fciCampoVlrImp, fciCampoQtdImp);
		
		if(considEntRetro) {
			if(tmEntradas == null || tmEntradas.getBigDecimal_Zero("qtde") == 0.0) {
				//Caso não haja entrada no período da tela, verifica o último mês que ocorreu entrada
				if(consEntRetroSomenteUltMes) {
					LocalDate data = buscarUltimaEntradaFCIPorItemData(idComponente, dtIniEnt);
					if(data != null) {
						LocalDate[] datas = DateUtils.getStartAndEndMonth(data);
						LocalDate dtIni = datas[0];
						LocalDate dtFin = datas[1];
						
						tmEntradas = buscarSomaEntradasFCIPorItemQtde(idComponente, dtIni, dtFin, fciCampoVlrImp, fciCampoQtdImp);
					}
				}else {
					//Caso não haja entrada no período da tela, verifica se teve entradas dos cálculos efetuados da FCI até a data inicial da tela
					List<LocalDate> datasFCI = buscarDatasDosCalculosDecrescente(idProduto);
					
					if(!Utils.isEmpty(datasFCI)) {
						for(LocalDate dtCalcFCI : datasFCI) {
							tmEntradas = buscarSomaEntradasFCIPorItemQtde(idComponente, dtCalcFCI, dtIniEnt, fciCampoVlrImp, fciCampoQtdImp);
							if(tmEntradas != null && tmEntradas.getBigDecimal_Zero("qtde") > 0.0) return tmEntradas;
						}
					}
					
					//Caso ainda não haja entrada, verifica se teve entradas desde sempre
					if(tmEntradas == null || tmEntradas.getBigDecimal_Zero("qtde") == 0.0) {
						if(consEntRetroSomenteUltMes) {
							LocalDate data = buscarUltimaEntradaFCIPorItemData(idComponente, dtIniEnt);
							if(data != null) {
								LocalDate[] datas = DateUtils.getStartAndEndMonth(data);
								LocalDate dtIni = datas[0];
								LocalDate dtFin = datas[1];
								
								tmEntradas = buscarSomaEntradasFCIPorItemQtde(idComponente, dtIni, dtFin, fciCampoVlrImp, fciCampoQtdImp);
							}
						}else {
							tmEntradas = buscarSomaEntradasFCIPorItemQtde(idComponente, null, dtIniEnt, fciCampoVlrImp, fciCampoQtdImp);
						}
					}
				}
			}
		}
		
		return tmEntradas;
	}
	
	private TableMap buscarSomaEntradasFCIPorItemQtde(Long abm01id, LocalDate dataIni, LocalDate dataFin, String cpoValor, String cpoQtde) {
		StringBuilder select = new StringBuilder("");
		select.append(" SUM(jGet(eaa0103json." + cpoQtde + ")::numeric) As qtde ");
		select.append(", SUM(jGet(eaa0103json." + cpoValor + ")::numeric) As valor ");
		
		String whereData = dataIni == null ? " AND eaa01esData <= :dataFin " : " AND eaa01esData BETWEEN :dataIni AND :dataFin ";
		
		Query query = getSession().createQuery(" SELECT ", 
											   select.toString(),
											   " FROM Eaa0103 ",
											   " INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
											   " INNER JOIN Abb01 ON eaa01central = abb01id ",
											   " WHERE eaa0103item = :abm01id ",
											   " AND eaa01clasDoc = 1 ",
											   " AND eaa01esMov = 0 ",
											   " AND eaa01iFCI = 1 ",
											   " AND eaa01cancData IS NULL AND abb01aprovado = 1 AND eaa01bloqueado = 0 AND abb01status = 0 ",
											   " AND eaa0103retInd = 0 ",
											   whereData,
											   getSamWhere().getWherePadrao("AND", Eaa01.class))
								  .setParameters("abm01id", abm01id,
												 "dataFin", dataFin);
											 
		if(dataIni != null) query.setParameter("dataIni", dataIni);
		
		return query.getUniqueTableMap();
	}
	
	private LocalDate buscarUltimaEntradaFCIPorItemData(Long abm01id, LocalDate data) {
		return getSession().createQuery(" SELECT MAX(eaa01esData) ",
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" WHERE eaa0103item = :abm01id ",
										" AND eaa01clasDoc = 1 ",
										" AND eaa01esMov = 0 ",
										" AND eaa01iFCI = 1 ",
										" AND eaa01esData < :data ",
										" AND eaa01cancData IS NULL AND abb01aprovado = 1 AND eaa01bloqueado = 0 AND abb01status = 0 ",
										" AND eaa0103retInd = 0 ",
										getSamWhere().getWherePadrao("AND", Eaa01.class))
							.setParameters("abm01id", abm01id,
										  "data", data)
							.getUniqueResult(ColumnType.DATE);
	}
	
	private List<LocalDate> buscarDatasDosCalculosDecrescente(Long abm01id) {
		return getSession().createQuery(" SELECT eab0101data ",
										" FROM Eab0101 ",
										" INNER JOIN Eab01 ON eab0101fci = eab01id ",
										" WHERE eab01item = :abm01id ",
										getSamWhere().getWherePadrao("AND", Eab01.class),
										" ORDER BY eab0101data DESC")
						   .setParameter("abm01id", abm01id)
						   .getList(ColumnType.DATE);
	}
	
	private TableMap selecionarSaidas(boolean considSaiRetro, Long idProduto, LocalDate dtIniSai, LocalDate dtFinSai, String uf, int tipoSaida, boolean consSaiRetroSomenteUltMes) {
		TableMap tmSaidas = selecionarDocumentosDeSaidas(idProduto, dtIniSai, dtFinSai, uf, tipoSaida);
		
		if(considSaiRetro) {
			if(tmSaidas == null || tmSaidas.getBigDecimal_Zero("valor") == 0.0) {
				if(consSaiRetroSomenteUltMes) {
					LocalDate data = buscarUltimaSaidaFCIPorItemData(idProduto, dtIniSai, uf, tipoSaida == 1);
					if(data != null) {
						LocalDate[] datas = DateUtils.getStartAndEndMonth(data);
						LocalDate dtIni = datas[0];
						LocalDate dtFin = datas[1];
						
						tmSaidas = selecionarDocumentosDeSaidas(idProduto, dtIni, dtFin, uf, tipoSaida);
					}
				}else {
					//Caso não haja saída no período da tela, verifica se teve saídas dos cálculos efetuados da FCI até a data inicial da tela
					List<LocalDate> datasFCI = buscarDatasDosCalculosDecrescente(idProduto);
					
					if(!Utils.isEmpty(datasFCI)) {
						for(LocalDate dtCalcFCI : datasFCI) {
							tmSaidas = selecionarDocumentosDeSaidas(idProduto, dtCalcFCI, dtIniSai, uf, tipoSaida);
							if(tmSaidas != null && tmSaidas.getBigDecimal_Zero("valor") > 0.0) return tmSaidas;
						}
					}
					
					//Caso ainda não haja saida, verifica se teve saídas desde sempre
					if(tmSaidas == null || tmSaidas.getBigDecimal_Zero("valor") == 0.0) {
						tmSaidas = selecionarDocumentosDeSaidas(idProduto, null, dtIniSai, uf, tipoSaida);
					}
				}
			}
		}
		
		return tmSaidas;
	}
	
	private TableMap selecionarDocumentosDeSaidas(Long abm01id,  LocalDate dataIni, LocalDate dataFin, String uf, int tipoSaida) {
		TableMap tmSaidas = buscarSomaSaidasFCIPorItemQtde(abm01id, dataIni, dataFin, fciCampoVlrSai, fciCampoQtdSai, uf, tipoSaida > 0);
		if(tipoSaida == 2 && (tmSaidas == null || tmSaidas.getBigDecimal_Zero("valor") == 0.0)) {
			tmSaidas = buscarSomaSaidasFCIPorItemQtde(abm01id, dataIni, dataFin, fciCampoVlrSai, fciCampoQtdSai, uf, false);
		}
		return tmSaidas;
	}
	
	private TableMap buscarSomaSaidasFCIPorItemQtde(Long abm01id, LocalDate dataIni, LocalDate dataFin, String cpoValor, String cpoQtde, String uf, boolean somenteInterestaduais) {
		StringBuilder select = new StringBuilder("");
		select.append(" SUM(jGet(eaa0103json." + cpoQtde + ")::numeric) As qtde ");
		select.append(", SUM(jGet(eaa0103json." + cpoValor + ")::numeric) As valor ");
		
		String whereData = dataIni == null ? " AND abb01data <= :dataFin " : " AND abb01data BETWEEN :dataIni AND :dataFin ";
		
		Query query = null;
		if(!somenteInterestaduais) { //Internas e Interestaduais
			query = getSession().createQuery(" SELECT ",
											 select.toString(),
											 " FROM Eaa0103 ",
											 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
											 " INNER JOIN Abb01 ON eaa01central = abb01id ",
											 " WHERE eaa0103item = :abm01id ",
											 " AND eaa01clasDoc = 1 ",
											 " AND eaa01esMov = 1 ",
											 " AND eaa01iFCI = 1 ",
											 " AND eaa01cancData IS NULL AND abb01aprovado = 1 AND eaa01bloqueado = 0 AND abb01status = 0 ",
											 " AND eaa0103retInd = 0 ",
											 whereData,
											 getSamWhere().getWherePadrao("AND", Eaa01.class));
		}else { //Interestaduais
			query = getSession().createQuery(" SELECT ",
											 select.toString(),
											 " FROM Eaa0103 ",
											 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
											 " INNER JOIN Abb01 ON eaa01central = abb01id ",
											 " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id AND eaa0101principal = 1 ",
											 " INNER JOIN Aag0201 ON eaa0101municipio = aag0201id ",
											 " INNER JOIN Aag02 ON aag0201uf = aag02id ",
											 " WHERE eaa0103item = :abm01id ",
											 " AND eaa01clasDoc = 1 ",
											 " AND eaa01esMov = 1 ",
											 " AND eaa01iFCI = 1 ",
											 " AND aag02uf <> :uf ",
											 " AND eaa01cancData IS NULL AND abb01aprovado = 1 AND eaa01bloqueado = 0 AND abb01status = 0 ",
											 " AND eaa0103retInd = 0 ",
											 whereData,
											 getSamWhere().getWherePadrao("AND", Eaa01.class));
			query.setParameter("uf", uf);
		}
		
		query.setParameters("abm01id", abm01id,
							"dataFin", dataFin);
						
		if(dataIni != null) query.setParameter("dataIni", dataIni);
		
		return query.getUniqueTableMap();
	}
	
	private LocalDate buscarUltimaSaidaFCIPorItemData(Long abm01id, LocalDate data, String uf, boolean somenteInterestaduais) {
		Query query = null;
		if(!somenteInterestaduais) { //Internas e Interestaduais
			query = getSession().createQuery(" SELECT MAX(abb01data) ",
											 " FROM Eaa0103 ",
											 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
											 " INNER JOIN Abb01 ON eaa01central = abb01id ",
											 " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id AND eaa0101principal = 1 ",
											 " INNER JOIN Aag0201 ON eaa0101municipio = aag0201id ",
											 " INNER JOIN Aag02 ON aag0201uf = aag02id ",
											 " WHERE eaa0103item = :abm01id ",
											 " AND eaa01clasDoc = 1 ",
											 " AND eaa01esMov = 1 ",
											 " AND eaa01iFCI = 1 ",
											 " AND eaa01esData < :data ",
											 " AND eaa01cancData IS NULL AND abb01aprovado = 1 AND eaa01bloqueado = 0 AND abb01status = 0 ",
											 " AND eaa0103retInd = 0 ",
											 getSamWhere().getWherePadrao("AND", Eaa01.class));
		}else { //Interestaduais
			query = getSession().createQuery(" SELECT MAX(abb01data) ",
											 " FROM Eaa0103 ",
											 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
											 " INNER JOIN Abb01 ON eaa01central = abb01id ",
											 " WHERE eaa0103item = :abm01id ",
											 " AND eaa01clasDoc = 1 ",
											 " AND eaa01esMov = 1 ",
											 " AND eaa01iFCI = 1 ",
											 " AND eaa01esData < :data ",
											 " AND aag02uf <> :uf ",
											 " AND eaa01cancData IS NULL AND abb01aprovado = 1 AND eaa01bloqueado = 0 AND abb01status = 0 ",
											 " AND eaa0103retInd = 0 ",
											 getSamWhere().getWherePadrao("AND", Eaa01.class));
			query.setParameter("uf", uf);
		}
		
		query.setParameters("abm01id", abm01id,
							"data", data);
						
		return query.getUniqueResult(ColumnType.DATE);
	}
	
	private Eab0101 buscarUltimaFCIPorFaixaCI(Long abm01id, BigDecimal ci) {
		String where = null;
		if(ci <= 40.0) {
			where = " AND eab0101ci <= :ci ";
		}else if(ci > 40.0 && ci <= 70.0) {
			where = " AND (eab0101ci > :ci1 AND eab0101ci <= :ci2) ";
		}else {
			where = " AND eab0101ci > :ci ";
		}
		
		Query query = getSession().createQuery(" SELECT * ",
											   " FROM Eab0101 ",
											   " INNER JOIN Eab01 ON eab0101fci = eab01id ",
											   " WHERE eab0101numFCI IS NOT NULL AND eab01item = :abm01id ",
											   where,
											   getSamWhere().getWherePadrao("AND", Eab01.class),
											   " ORDER BY eab0101data")
								  .setParameter("abm01id", abm01id)
								  .setMaxResult(1);
								  
	  if(ci <= 40.0) {
		  query.setParameter("ci", 40.0);
	  }else if(ci > 40.0 && ci <= 70.0) {
		  query.setParameter("ci1", 40.0);
		  query.setParameter("ci2", 70.0);
	  }else {
		  query.setParameter("ci", 70.0);
	  }
	  
	  return query.getUniqueResult(ColumnType.ENTITY);
	}
	
	private void setarOrigemMercadoriaNoItemByCI(boolean atualizarOrigemMercadoria, Long abm01id, BigDecimal ci) {
		if(!atualizarOrigemMercadoria) return;
		
		Long abm12id = buscarAbm12idConfiguracaoFiscalDoItem(abm01id);
		if(abm12id == null) return;
		
		Abm12 abm12 = session.get(Abm12.class, "abm12id, abm12cstA", abm12id);
		abm12.abm12cstA = obterOrigemMercadoriaByCI(ci);
		session.persist(abm12);
	}
	
	private int obterOrigemMercadoriaByCI(BigDecimal ci) {
		int origemMercadoria = -1;
		if(ci <= 40.0) {
			origemMercadoria = 5;
		}else if(ci > 40.0 && ci <= 70.0) {
			origemMercadoria = 3;
		}else {
			origemMercadoria = 8;
		}
		return origemMercadoria;
	}
	
	private Long buscarAbm12idConfiguracaoFiscalDoItem(Long abm01id) {
		return getSession().createQuery(" SELECT abm0101fiscal ",
										" FROM Abm01 ",
										" INNER JOIN Abm0101 ON abm0101item = abm01id ",
										" WHERE abm01id = :abm01id ",
										" AND abm0101empresa = :aac10id ",
										getSamWhere().getWherePadrao("AND", Abm01.class))
						   .setParameters("abm01id", abm01id,
										  "aac10id", obterEmpresaAtiva().aac10id)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.LONG);
	}
	
	private List<Eab01011> buscarItensDoCalculo(Long eab0101id) {
		return getSession().createQuery(" SELECT * ",
										" FROM Eab01011 ",
										" WHERE eab01011calc = :eab0101id ")
						   .setParameter("eab0101id", eab0101id)
						   .getList(ColumnType.ENTITY);
	}
	
	private String buscarNCMDoItem(Long abm01id) {
		return getSession().createQuery(" SELECT abg01codigo ",
										" FROM Abm01 ",
										" INNER JOIN Abm0101 ON abm0101item = abm01id ",
										" INNER JOIN Abg01 ON abm0101ncm = abg01id ",
										" WHERE abm01id = :abm01id ",
										" AND abm0101empresa = :aac10id ",
										getSamWhere().getWherePadrao("AND", Abm01.class))
						   .setParameters("abm01id", abm01id,
										  "aac10id", obterEmpresaAtiva().aac10id)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.STRING);
	}
	
	private String buscarUMVDoItem(Long abm01id) {
		return getSession().createQuery(" SELECT aam06codigo ",
										" FROM Abm01 ",
										" INNER JOIN Abm0101 ON abm0101item = abm01id ",
										" INNER JOIN Abm13 ON abm0101comercial = abm13id ",
										" INNER JOIN Aam06 ON abm13umv = aam06id ",
										" WHERE abm01id = :abm01id ",
										" AND abm0101empresa = :aac10id ",
										getSamWhere().getWherePadrao("AND", Abm01.class))
						   .setParameters("abm01id", abm01id,
										  "aac10id", obterEmpresaAtiva().aac10id)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.STRING);
	}
	
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODUifQ==