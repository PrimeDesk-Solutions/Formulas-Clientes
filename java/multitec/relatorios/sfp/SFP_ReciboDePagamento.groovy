package multitec.relatorios.sfp;

import java.time.LocalDate;

import javax.mail.util.ByteArrayDataSource

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Email
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aab1008
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.aa.Aap03;
import sam.model.entities.aa.Aap18
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abb11;
import sam.model.entities.ab.Abh05;
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba02
import sam.server.cas.service.CAS1010Service
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;
/**
 * Classe para gerar relatório de Recibo de Pagamento
 * @author Samuel
 * @since 12/07/2019
 * @version 1.1
 *
 */
public class SFP_ReciboDePagamento extends RelatorioBase {
	/**
	 * Métoro principal
	 * @return String - Nome do relatório
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Recibo de Pagamento";
	}
	
	
	/**
	 * Método Criar valores iniciais
	 * @return Map - Filtros do Front-end
	 */
	@Override
	public Map<String, Object> criarValoresIniciais(){
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		filtrosDefault.put("calcFolha", true);
		filtrosDefault.put("calcFerias", true);
		filtrosDefault.put("calcRescisao", true);
		filtrosDefault.put("calcAdiantamento", false);
		filtrosDefault.put("calc13sal", false);
		filtrosDefault.put("calcPlr", false);
		filtrosDefault.put("calcOutros", false);
		filtrosDefault.put("ordenacao", "0");
		filtrosDefault.put("periodo", datas);
		List<Long> aab1008ids = getAcessoAoBanco().obterListaDeLong("SELECT aab1008id FROM Aab1008 WHERE aab1008user = :aab10id", criarParametroSql("aab10id", obterUsuarioLogado().aab10id));
		if (Utils.isEmpty(aab1008ids)) aab1008ids.add(0);
		filtrosDefault.put("aab1008ids", aab1008ids);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**
	 * Método para gerar o PDF com os dados
	 * @return gerarPDF (Nome, Dados)
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhador = getListLong("trabalhadores");
		List<Long> idsDepartamento = getListLong("departamento");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsSindicato = getListLong("sindicato");
		Integer ordenacao = getInteger("ordenacao");
		LocalDate[] periodo = getIntervaloDatas("periodo");
		Set<Integer> tipoTrabalhador = obterTipoTrabalhador();
		Set<Integer> tipoCalculo = obterTiposCalculo();
		String nomeRelatorio = "SFP_ReciboDePagamento";
		String obs = getString("observacoes");
		String eventosNaoImprimir = getString("eventos") == null ? "": getString("eventos");
		Long aab1008id = getLong("email");
		boolean enviaEmail = getBoolean("enviaEmail");
		String emailAssunto = getString("emailAssunto")
		
		String[] listaEventos = null;
		if (!eventosNaoImprimir.isEmpty()) {
			listaEventos = eventosNaoImprimir.split(",");
		}
		
		if (listaEventos != null) {
			for(int i = 0; i < listaEventos.length; i++) {
				String evento = listaEventos[i];
				evento = evento.trim();
				listaEventos[i] = evento;
			}
		}
		
		// Eventos
		String eveFeriasLiquida = getParametros(Parametros.FB_EVELIQFERIAS);
		String eveFeriasPagas = getParametros(Parametros.FB_EVEPAGTOFERIASDESC);
		String eveAbonoLiquido = getParametros(Parametros.FB_EVELIQABONO);
		String eveAbonoPago = getParametros(Parametros.FB_EVEPAGTOABONODESC);
		String eveAd13Liquido = getParametros(Parametros.FB_EVELIQADIANT13);
		String eveAd13Pago = getParametros(Parametros.FB_EVEPAGTO13SALDESC); //TODO Verificar parametro
		String eveResLiquida = getParametros(Parametros.FB_EVERESLIQUIDA);
		String eveResPaga = getParametros(Parametros.FB_EVERESLIQUIDA); //TODO Verificar parametro
		String eveFerNaoImpr = getParametros(Parametros.FB_EVELIQFERIAS); //TODO Verificar parametro
		String eveBcFgtsSal = "9001";
		String eveBcFgts13 = "9003";
		String eveBcFgtsFer = "9002";
		String eveFgtsSal = "9501";
		String eveFgts13 = "9503";
		String eveFgtsFer = "9502";
		String eveScSal = "9010";
		String eveSc13 = "9012";
		String eveScFer = "9011";
		String eveRbSal = "9030";
		String eveRb13 = "9032";
		String eveRbFer = "9031";
		String eveSalContrato = "9999";

		
		Aac10 aac10 = getVariaveis().getAac10();
		
		String endereco = null;
		if(aac10.getAac10endereco() != null) {
			if(aac10.getAac10numero() != null) {
				endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
			}else {
				endereco = aac10.getAac10endereco();
			}
			if(aac10.getAac10bairro() != null) {
				endereco += " - " + aac10.getAac10bairro();
			}
			if(aac10.getAac10complem() != null) {
				endereco += " - " + aac10.getAac10complem();
			}
		}
		
		// Define os campos principais do relatório
		params.put("TITULO_RELATORIO_1", "Recibo de Pagamento");
		params.put("TITULO_RELATORIO_2", "Demostrativo de Pagamento");
		params.put("EMP_NI", aac10.getAac10ni());
		params.put("EMP_ENDERECO", endereco + " - " + obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() + "/" + obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf());
		params.put("EMP_CEP", aac10.getAac10cep());
		params.put("OBS", obs == null ? "" : obs);
		params.put("MES_REF", DateUtils.formatDate(periodo[0], "MM/YYYY"))
		params.put("DATA_INICIAL", DateUtils.formatDate(periodo[0]));
		params.put("DATA_FINAL", DateUtils.formatDate(periodo[1]));
		params.put("EMP_RS", aac10.getAac10rs());
		params.put("EMP_SFPTI1", aac10.getAac10ti());
		params.put("EMP_CEI", aac10.getAac10rCpf());

		List<String> eventosBase = new ArrayList<String>();
		eventosBase.add(eveBcFgtsSal);
		eventosBase.add(eveBcFgts13);
		eventosBase.add(eveBcFgtsFer);
		eventosBase.add(eveFgtsSal);
		eventosBase.add(eveFgts13);
		eventosBase.add(eveFgtsFer);
		eventosBase.add(eveScSal);
		eventosBase.add(eveSc13);
		eventosBase.add(eveScFer);
		eventosBase.add(eveRbSal);
		eventosBase.add(eveRb13);
		eventosBase.add(eveRbFer);
		eventosBase.add(eveSalContrato);
		
		TableMapDataSource reciboDePagamento = buscarDadosRelatorio(idsTrabalhador, idsDepartamento, idsCargos, idsSindicato, periodo, tipoTrabalhador, tipoCalculo, ordenacao, eveFeriasLiquida, eveFeriasPagas, eveAbonoLiquido, eveAbonoPago, eveAd13Liquido, eveAd13Pago, eveResLiquida, eveResPaga, eveFerNaoImpr, eventosBase, listaEventos);
		
		if (enviaEmail && aab1008id != null) {
			for (int i =0; i < reciboDePagamento.recordCount; i++) {
				TableMap tm = reciboDePagamento.records.get(i);
				Long abh80id = tm.getLong("abh80id");
				Abh80 abh80 = buscarTrabalhador(abh80id);
				if (abh80.abh80eMail == null) continue;
				
				JasperReport report = carregarArquivoRelatorio(nomeRelatorio);
				TableMapDataSource reciboParaEmail = buscarDadosRelatorio(Arrays.asList(abh80id), idsDepartamento, idsCargos, idsSindicato, periodo, tipoTrabalhador, tipoCalculo, ordenacao, eveFeriasLiquida, eveFeriasPagas, eveAbonoLiquido, eveAbonoPago, eveAd13Liquido, eveAd13Pago, eveResLiquida, eveResPaga, eveFerNaoImpr, eventosBase, listaEventos);
				JasperPrint print = processarRelatorio(report, reciboParaEmail);
				
				Aab1008 aab1008 = getAcessoAoBanco().buscarRegistroUnicoById("Aab1008", aab1008id);
				Email email = new Email(aab1008);
				email.assunto = emailAssunto;
				
				StringBuilder strCorpo = new StringBuilder("");
				strCorpo.append("Esta mensagem refere-se ao recibo de pagamento:");
		
				byte[] bytes = convertPrintToPDF(print);
				ByteArrayDataSource arquivoPdf = new ByteArrayDataSource(bytes, "application/pdf");
				String nomeArquivoPdf = "Recibo de Pagamento - " + abh80.abh80codigo + " - " + abh80.abh80nome + ".pdf";
				
				email.anexar(arquivoPdf, nomeArquivoPdf, nomeArquivoPdf);
				strCorpo.append("<p><p>");
				
				email.setCorpoMsg(strCorpo.toString());
				email.setEmailDestinoPara(abh80.abh80eMail);
				
				if (aab1008.aab1008assinatura != null) {
					CAS1010Service cas1010Service = instanciarService(CAS1010Service.class);
					email.adicionarAssinatura(cas1010Service.converterAssinaturaDoEmailEmInputStream(aab1008.aab1008assinatura));
				}
				
				email.enviar();
			}
		}
		
		return gerarPDF(nomeRelatorio, reciboDePagamento);
	}
	
	/**
	 * Método para buscar os dados no banco
	 * @param idsTrabalhador - Trabalhodores
	 * @param idsDepartamento - Departamentos
	 * @param idsCargos - Cargos
	 * @param idsSindicato - Sindicatos
	 * @param periodo - Intervalo de Datas
	 * @param tipoTrabalhador - Tipo trabalhador 0-Trabalhador 1-Autônomo 2-Pró-labore 3-Terceiros
	 * @param tipoCalculo - Tipo calculo 0-Folha 1-Férias 2-Rescisão 3-Adiantamento 4-13º salário 5-Outros
	 * @param ordenacao - Ordem
	 * @param eveFeriasLiquida - Valor férias liquida
	 * @param eveFeriasPagas - Valor férias pagar
	 * @param eveAbonoLiquido - Valor abono liquido
	 * @param eveAbonoPago - Valor abono pago
	 * @param eveAd13Liquido - Valor 13º salario liquido
	 * @param eveAd13Pago - Valor 13º salario pago
	 * @param eveResLiquida - Valor rescisão liquida
	 * @param eveResPaga - Valor rescisão pago
	 * @param eveFerNaoImpr - Eventos para não imprimir
	 * @param eventosBase - Eventos base
	 * @param eventosNaoImprimir - Eventos para não imprimir
	 * @return TableMapDataSource
	 */
	public TableMapDataSource buscarDadosRelatorio(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsCargos, List<Long> idsSindicato, LocalDate[] periodo, Set<Integer> tipoTrabalhador, Set<Integer> tipoCalculo, Integer ordenacao, String eveFeriasLiquida, String eveFeriasPagas, String eveAbonoLiquido, String eveAbonoPago, String eveAd13Liquido, String eveAd13Pago, String eveResLiquida, String eveResPaga, String eveFerNaoImpr, List<String> eventosBase, String[] eventosNaoImprimir){
		
		List<TableMap>  listValores = buscarDadosFba0101sPeloReciboPagamento(idsTrabalhador, idsDepartamento, idsCargos, idsSindicato, periodo, tipoTrabalhador, tipoCalculo, ordenacao);
		List<TableMap> mapValores = new ArrayList<TableMap>();
		
		//Prepara o mapa com os valores para recibo de pagamento.
		int linha = 0;
		if(listValores != null && listValores.size() > 0) {
			for(int i = 0; i < listValores.size(); i++) {
				
				Long idAbh80 = listValores.get(i).getLong("abh80id");
				String key = linha + "/" + idAbh80;
				
				List<TableMap> listaValoresEveBase = buscarDadosFba0101sValoresEventosBasePorReciboPagamento(idsDepartamento, idsCargos, idsSindicato, idAbh80, tipoCalculo, periodo, eventosBase, eveFerNaoImpr, eventosNaoImprimir);

				List<BigDecimal> valoresEveBase = new ArrayList<BigDecimal>();
				for(int index = 0; index < 13; index++) {
					valoresEveBase.add(index, new BigDecimal(0));
				}

				for(int j = 0; j < listaValoresEveBase.size(); j++) {

					String codigo = listaValoresEveBase.get(j).getString("abh21codigo");
					BigDecimal valor = listaValoresEveBase.get(j).getBigDecimal("totalValor");
					
					if(codigo.equals(eventosBase.get(0))) {
						valoresEveBase.set(0, valor);
					}else if(codigo.equals(eventosBase.get(1))) {
						valoresEveBase.set(1, valor);
					}else if(codigo.equals(eventosBase.get(2))) {
						valoresEveBase.set(2, valor);
					}else if(codigo.equals(eventosBase.get(3))) {
						valoresEveBase.set(3, valor);
					}else if(codigo.equals(eventosBase.get(4))) {
						valoresEveBase.set(4, valor);
					}else if(codigo.equals(eventosBase.get(5))) {
						valoresEveBase.set(5, valor);
					}else if(codigo.equals(eventosBase.get(6))) {
						valoresEveBase.set(6, valor);
					}else if(codigo.equals(eventosBase.get(7))) {
						valoresEveBase.set(7, valor);
					}else if(codigo.equals(eventosBase.get(8))) {
						valoresEveBase.set(8, valor);
					}else if(codigo.equals(eventosBase.get(9))) {
						valoresEveBase.set(9, valor);
					}else if(codigo.equals(eventosBase.get(10))) {
						valoresEveBase.set(10, valor);
					}else if(codigo.equals(eventosBase.get(11))) {
						valoresEveBase.set(11, valor);
					}else if(codigo.equals(eventosBase.get(12))) {
						valoresEveBase.set(12, valor);
					}
				}
				
				//Calcula o salário do CADASTRO do trabalhador.
				BigDecimal salarioMes = null;
				BigDecimal salarioHora = null;
				Integer horasSem = listValores.get(i).getInteger("abh80hs");
				Long unidPagto = listValores.get(i).getLong("abh80unidPagto");
				
				Fba02 fba02 = getAcessoAoBanco().buscarRegistroUnico(" SELECT fba02id, fba02salario, fba02unidPagto FROM Fba02 " +
																	 " WHERE fba02trab = :idAbh80 " + getWhereDataInterval("AND", periodo, "fba02dtCalc") +
																		" ORDER BY fba02dtCalc DESC, fba02id DESC",
																	 criarParametroSql("idAbh80", listValores.get(i).getLong("abh80id")));
																 
				if (fba02 != null && fba02.fba02unidPagto != null) {
					Aap18 aap18 = getAcessoAoBanco().buscarRegistroUnicoById("Aap18", fba02.fba02unidPagto.aap18id);
					def salario = fba02.fba02salario;
					salarioMes = (salario * aap18.aap18fSM) * horasSem;
					salarioHora = (salario * aap18.aap18fSH) * horasSem;
				} else {
					Aap18 aap18 = getAcessoAoBanco().buscarRegistroUnicoById("Aap18", unidPagto);
					def salario = listValores.get(i).getBigDecimal("abh80salario");
					salarioMes = (salario * aap18.aap18fSM) * horasSem;
					salarioHora = (salario * aap18.aap18fSH) * horasSem;
				}
				
				//Obtêm o centro de custo, cargo e cbo do ARQUIVO DE VALORES.
				String codAbh05 = null;
				String nomeAbh05 = null;
				String cbo = null;
				
				Fba0101 fba0101 = buscarFba0101PorIdEPeriodo(idAbh80, periodo);
				
				Abh80 abh80 = buscarTrabalhador(fba0101.getFba0101trab().getAbh80id());
				Abh05 abh05 = buscarCargoESalario(abh80.getAbh80cargo().getAbh05id());
				Abb11 abb11 = buscarDepartamento(abh80.getAbh80depto().getAbb11id());
				
				if(abh05 != null) {
					codAbh05 = abh05.getAbh05codigo();
					nomeAbh05 = abh05.getAbh05nome();
					cbo = buscarCBO(abh05.getAbh05cbo().getAap03id()) != null ? buscarCBO(abh05.getAbh05cbo().getAap03id()).getAap03codigo() : null;
				}
				
				comporLinhaMapa(mapValores, key, idAbh80, listValores.get(i).getString("abh80codigo"), listValores.get(i).getString("abh80nome"), listValores.get(i).getString("abh80bcoConta"), listValores.get(i).getString("abh80bcoDigCta"), listValores.get(i).getString("abh80bcoAgencia"), listValores.get(i).getString("abh80bcoDigAg"), salarioMes, salarioHora, listValores.get(i).getString("abh80cpf"), listValores.get(i).getString("abh80rgNum"), listValores.get(i).getDate("abh80rgDtExped"), listValores.get(i).getString("abh80rgEe"), listValores.get(i).getString("abh80rgOe"), listValores.get(i).getString("abh80pis"), listValores.get(i).getDate("abh80dtPis"), listValores.get(i).getDate("abh80dtAdmis"), abb11.getAbb11codigo(), abb11.getAbb11nome(), codAbh05, nomeAbh05, cbo, "Recibo de Pagamento", valoresEveBase);
				linha++;
			}
		}

		//Busca os eventos de cada cálculo e adiciona no mapa de eventos.
		List<TableMap> mapEventos = new ArrayList<TableMap>();
		int countItem = 0;
		if(mapValores != null && mapValores.size() > 0) {
			for(int i = 0; i < mapValores.size(); i++) {
				countItem = 0;

				BigDecimal totalRend = new BigDecimal(0);
				BigDecimal totalDesc = new BigDecimal(0);
				Long idAbh80 = mapValores.get(i).getLong("abh80id");
				String key = i + "/" + idAbh80;
																							
				List<TableMap> listEventos = buscarDadosFba01011sEventosPorReciboPagamento(idsDepartamento, idsCargos, idsSindicato, idAbh80, periodo, tipoCalculo, eveFerNaoImpr, eventosNaoImprimir);

				if(listEventos != null && listEventos.size() > 0) {
					for(int j = 0; j < listEventos.size(); j++) {
						
						TableMap mapEvento = new TableMap();
						mapEvento.put("count", countItem);
						mapEvento.put("key", key);
						mapEvento.put("evento", listEventos.get(j).getString("abh21codigo") + " - " + listEventos.get(j).getString("abh21nome"));
						mapEvento.put("ref", listEventos.get(j).getBigDecimal("totalRef"));
						
						if(listEventos.get(j).getInteger("abh21tipo") == 0) {
							mapEvento.put("rendimento", listEventos.get(j).getBigDecimal("totalValor"));
							totalRend = totalRend.add(listEventos.get(j).getBigDecimal("totalValor"));
						}else {
							mapEvento.put("desconto", listEventos.get(j).getBigDecimal("totalValor"));
							totalDesc = totalDesc.add(listEventos.get(j).getBigDecimal("totalValor"));
						}
						countItem++;
						mapEventos.add(mapEvento);
					}
				}

				if(tipoCalculo.contains(3)) {
					//Verifica se existe evento de férias líquida para zerar com férias pagas.
					
					Abh21 abh21FeriasPagas = buscarPorChaveUnica(eveFeriasPagas);
					BigDecimal valorFerias = buscarFba01011ValorEventoParaZerarFeriasERescisao(idsTrabalhador, idsDepartamento, idsCargos, idsSindicato, idAbh80, tipoTrabalhador, "", periodo, 3, eveFeriasLiquida, eventosNaoImprimir);
					
					if(abh21FeriasPagas != null && valorFerias != null && valorFerias.compareTo(new BigDecimal(0)) > 0) {
						comporEventoPago(mapEventos, key, abh21FeriasPagas.getAbh21codigo(), abh21FeriasPagas.getAbh21nome(), valorFerias, countItem);
						totalDesc = totalDesc.add(valorFerias);
						countItem++;
					}

					//Verifica se existe evento de abono líquido para zerar com abono pago.
					Abh21 abh21AbonoPago = buscarPorChaveUnica(eveAbonoPago);
					BigDecimal valorAbono = buscarFba01011ValorEventoParaZerarFeriasERescisao(idsTrabalhador, idsDepartamento, idsCargos, idsSindicato, idAbh80, tipoTrabalhador, "", periodo, 3, eveAbonoLiquido, eventosNaoImprimir);
					if(abh21AbonoPago != null && valorAbono != null && valorAbono.compareTo(new BigDecimal(0)) > 0) {
						comporEventoPago(mapEventos, key, abh21AbonoPago.getAbh21codigo(), abh21AbonoPago.getAbh21nome(), valorAbono, countItem);
						totalDesc = totalDesc.add(valorAbono);
						countItem++;
					}

					//Verifica se existe evento de adiantamento de 13º salário líquido para zerar com adiant. 13º pago.
					Abh21 abh21Ad13Pago = buscarPorChaveUnica(eveAd13Pago);
					BigDecimal valorAd13 = buscarFba01011ValorEventoParaZerarFeriasERescisao(idsTrabalhador, idsDepartamento, idsCargos, idsSindicato, idAbh80, tipoTrabalhador, "", periodo, 3, eveAd13Pago, eventosNaoImprimir);
					if(abh21Ad13Pago != null && valorAd13 != null && valorAd13.compareTo(new BigDecimal(0)) > 0) {
						comporEventoPago(mapEventos, key, abh21Ad13Pago.getAbh21codigo(), abh21Ad13Pago.getAbh21nome(), valorAd13, countItem);
						totalDesc = totalDesc.add(valorAd13);
						countItem++;
					}
				}

				if(tipoCalculo.contains(4)) {
					//Verifica se existe evento de rescisão líquido para zerar com rescisão paga.
					Abh21 abh21ResPaga = buscarPorChaveUnica(eveResPaga);
					BigDecimal valorRes = buscarFba01011ValorEventoParaZerarFeriasERescisao(idsTrabalhador, idsDepartamento, idsCargos, idsSindicato, idAbh80, tipoTrabalhador, "", periodo, 4, eveResLiquida, eventosNaoImprimir);
					if(abh21ResPaga != null && valorRes != null && valorRes.compareTo(new BigDecimal(0)) > 0) {
						comporEventoPago(mapEventos, key, abh21ResPaga.getAbh21codigo(), abh21ResPaga.getAbh21nome(), valorRes, countItem);
						totalDesc = totalDesc.add(valorRes);
						countItem++;
					}
				}
				
				mapValores.get(i).put("totalRend", totalRend);
				mapValores.get(i).put("totalDesc", totalDesc);
				mapValores.get(i).put("totalLiquido", totalRend.subtract(totalDesc));
			}
		}

		TableMapDataSource dsPrincipal = new TableMapDataSource(mapValores);
		dsPrincipal.addSubDataSource("DsSub1", mapEventos, "key", "key");
		dsPrincipal.addSubDataSource("DsSub2", mapEventos, "key", "key");
		params.put("StreamSub1", carregarArquivoRelatorio("SFP_ReciboDePagamento_S1"));
		params.put("StreamSub2", carregarArquivoRelatorio("SFP_ReciboDePagamento_S1"));
		
		return dsPrincipal;
	}

	//Compõe as linhas do mapa
	private void comporLinhaMapa(List<TableMap> mapa, String key, Long idAbh80, String codAbh80, String nomeAbh80, String contaAbh80, String digContaAbh80, String agenciaAbh80, String digAgenciaAbh80, BigDecimal salarioMes, BigDecimal salarioHora, String cpfAbh80, String rgAbh80, LocalDate rgExpedAbh80, String rgEEAbh80, String rgOEAbh80, String pisAbh80, LocalDate dtPisAbh80, LocalDate dtAdmisAbh80, String codAbb11, String nomeAbb11, String codAbh05, String nomeAbh05, String cbo, String tituloRel, List<BigDecimal> valoresEveBase) {
		TableMap map = new TableMap();
		
		map.put("key", key);
		map.put("abh80id", idAbh80);
		map.put("abh80codigo", codAbh80);
		map.put("abh80nome", nomeAbh80);
		map.put("abh80bcoConta", contaAbh80 == null ? "" : contaAbh80);
		map.put("abh80bcoDigCta", digContaAbh80 == null ? "" : digContaAbh80);
		map.put("abh80bcoAgencia", agenciaAbh80 == null ? "" : agenciaAbh80);
		map.put("abh80bcoDigAg", digAgenciaAbh80 == null ? "" : digAgenciaAbh80);
		map.put("salarioMes", salarioMes);
		map.put("salarioHora", salarioHora);
		map.put("abh80cpf", cpfAbh80);
		map.put("abh80rg", rgAbh80);
		map.put("abh80rgexped", rgExpedAbh80);
		map.put("abh80rgee", rgEEAbh80);
		map.put("abh80rgoe", rgOEAbh80);
		map.put("abh80pis", pisAbh80);
		map.put("abh80dtpis", dtPisAbh80);
		map.put("abh80dtAdmis", dtAdmisAbh80);
		map.put("abb11codigo", codAbb11);
		map.put("abb11nome", nomeAbb11);
		map.put("abh05codigo", codAbh05);
		map.put("abh05nome", nomeAbh05);
		map.put("aap03codigo", cbo);
		map.put("tituloRel", tituloRel);
		map.put("valorBcFgtsSal", valoresEveBase.get(0));
		map.put("valorBcFgts13", valoresEveBase.get(1));
		map.put("valorBcFgtsFer", valoresEveBase.get(2));
		map.put("valorFgtsSal", valoresEveBase.get(3));
		map.put("valorFgts13", valoresEveBase.get(4));
		map.put("valorFgtsFer", valoresEveBase.get(5));
		map.put("valorScSal", valoresEveBase.get(6));
		map.put("valorSc13", valoresEveBase.get(7));
		map.put("valorScFer", valoresEveBase.get(8));
		map.put("valorRbSal", valoresEveBase.get(9));
		map.put("valorRb13", valoresEveBase.get(10));
		map.put("valorRbFer", valoresEveBase.get(11));
		map.put("valorSalContrato", valoresEveBase.get(12));
		
		mapa.add(map);
	}

	//Compõe uma linha no mapa de eventos para férias pagas, abono pago e adiantamento de 13º salário pago (se houver)
	private void comporEventoPago(List<TableMap> mapa, String key, String codAbh21, String nomeAbh21, BigDecimal valor, Integer count) {
		TableMap map = new TableMap();
		
		map.put("key", key);
		map.put("evento", codAbh21 + " - " + nomeAbh21);
		map.put("ref", new BigDecimal(0));
		map.put("desconto", valor);
		map.put("count", count);
		
		mapa.add(map);
	}
		
	/**Método Diverso
	 * @return Set Integer (Tipo de Cálculo)
	 */
	private Set<Integer> obterTiposCalculo(){
		Set<Integer> calc = new HashSet<Integer>();
		
		if((Boolean) get("calcFolha")) calc.add(0);
		if((Boolean) get("calcAdiantamento")) calc.add(1);
		if((Boolean) get("calc13sal")) calc.add(2);
		if((Boolean) get("calcFerias")) calc.add(3);
		if((Boolean) get("calcRescisao")) calc.add(4);
		if((boolean) get("calcPlr")) calc.add(6);
		if((Boolean) get("calcOutros")) calc.add(9);
		
		if(calc.size() == 0) {
			calc.add(0);
			calc.add(1);
			calc.add(2);
			calc.add(3);
			calc.add(4);
			calc.add(6);
			calc.add(9);
		}
		return calc;
	}
	
	/**Método Diverso
	 * @return Set Integer (Tipo de Trabalhador)
	 */
	private Set<Integer> obterTipoTrabalhador(){
		Set<Integer> tiposTrab = new HashSet<Integer>();
		
		if((Boolean) get("isTrabalhador")) tiposTrab.add(0);
		if((Boolean) get("isAutonomo")) tiposTrab.add(1);
		if((Boolean) get("isProlabore")) tiposTrab.add(2);
		if((Boolean) get("isTerceiros")) tiposTrab.add(3);
		
		if(tiposTrab.size() == 0) {
			tiposTrab.add(0);
			tiposTrab.add(1);
			tiposTrab.add(2);
			tiposTrab.add(3);
		}
		return tiposTrab;
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
	
	/**Método Diverso
	 * @return 	Aag0201 (uf, municipio)
	 */
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addJoin(Joins.fetch("aag0201uf"))
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
	
	/**
	 * Método buscar dados Recibo de Pagamento
	 * @return List<TableMap> Dados do Banco
	 */
	public List<TableMap> buscarDadosFba0101sPeloReciboPagamento(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsCargos, List<Long> idsSindicato, LocalDate[] periodo, Set<Integer> tipoTrabalhador, Set<Integer> tipoCalculo, Integer ordenacao) {
		String ordem = null;
		if(ordenacao == 0) {
			ordem = " ORDER BY abb11codigo, abh80codigo ";
		}else if(ordenacao == 1) {
			ordem = " ORDER BY abb11codigo, abh80nome ";
		}else if(ordenacao == 2) {
			ordem = " ORDER BY abh80nome ";
		}else {
			ordem = " ORDER BY abh80codigo ";
		}

		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Departamento = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereFad0101Cargos = idsCargos != null && !idsCargos.isEmpty() ? "AND fad0101id IN (:idsCargos) " : "";
		String whereAbh03Sindicato = idsSindicato != null && !idsSindicato.isEmpty() ? "AND abh03id IN (:idsSindicato) " : "";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "fba0101dtCalc") : "";
		
		String sql = "SELECT abh80id, abh80codigo, abh80nome, abh80bcoConta, abh80bcoDigCta, abh80bcoAgencia, abh80bcoDigAg, abh80unidPagto, abh80salario, " +
					 "abh80hs, abh80cpf, abh80rgNum, abh80rgDtExped, abh80rgEe, abh80rgOe, abh80pis, abh80dtPis, abh80dtAdmis, MAX(abb11codigo) as abb11codigo " +
					 "FROM Fba0101 " +
					 "INNER JOIN Fba01011 ON fba01011vlr = fba0101id " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
					 whereData + whereAbh80Trabalhador + whereAbb11Departamento + whereFad0101Cargos + whereAbh03Sindicato +
					 "AND fba0101tpVlr IN (:fba0101tpVlr) " +
					 "AND abh80tipo IN (:abh80tipo) " +
					 getSamWhere().getWherePadrao("AND", Abh80.class) +
					 " GROUP BY abh80id, abh80codigo, abh80nome, abh80bcoConta, abh80bcoDigCta, abh80bcoAgencia, abh80bcoDigAg, abh80salTipo, abh80salario, " +
					 "abh80hs, abh80cpf, abh80rgNum, abh80rgDtExped, abh80rgEe, abh80rgOe, abh80pis, abh80dtPis, abh80dtAdmis " +
					 ordem;
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsSindicato != null && !idsSindicato.isEmpty()) query.setParameter("idsSindicato", idsSindicato);
		query.setParameter("fba0101tpVlr", tipoCalculo);
		query.setParameter("abh80tipo", tipoTrabalhador);
		
		
		return query.getListTableMap();
		
	}
	
	/**
	 * Método buscar valores Evento Base por Recibo de Pagamento
	 * @return List<TableMap> Dados do Banco
	 */
	public List<TableMap> buscarDadosFba0101sValoresEventosBasePorReciboPagamento(List<Long> idsDepartamento, List<Long> idsCargos, List<Long> idsSindicato, Long idAbh80, Set<Integer> tipoCalculo, LocalDate[] periodo, List<String> eventosBase, String eveFerNaoImpr, String[] eventosNaoImprimir) {
		String whereEve = eveFerNaoImpr != null ? "AND abh21codigo <> :eveFerNaoImpr " : "";
		String whereAbb11Departamento = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereFad0101Cargos = idsCargos != null && !idsCargos.isEmpty() ? "AND fad0101id IN (:idsCargos) " : "";
		String whereAbh03Sindicato = idsSindicato != null && !idsSindicato.isEmpty() ? "AND abh03id IN (:idsSindicato) " : "";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "fba0101dtCalc") : "";
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";
		
		String sql = "SELECT abh21codigo, SUM(fba01011valor) as totalValor " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Fba01 ON Fba01id = fba0101calculo "+
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
					 whereData + whereAbb11Departamento + whereFad0101Cargos + whereAbh03Sindicato + whereEve + whereEventosNaoImprimir +
					 "AND fba0101tpVlr IN (:fba0101tpVlr) " +
					 "AND abh80id = :abh80id " +
					 "AND abh21codigo IN (:abh21codigo) " +
					 getSamWhere().getWherePadrao("AND", Fba01.class) +
					 " GROUP BY abh21codigo";
			
		Query query = getSession().createQuery(sql);
		
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsSindicato != null && !idsSindicato.isEmpty()) query.setParameter("idsSindicato", idsSindicato);
		if(eveFerNaoImpr != null && !eveFerNaoImpr.isEmpty()) query.setParameter("eveFerNaoImpr", eveFerNaoImpr);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		query.setParameter("fba0101tpVlr", tipoCalculo);
		query.setParameter("abh80id", idAbh80);
		query.setParameter("abh21codigo", eventosBase);
		
		return query.getListTableMap();
		
	}
	
	/**
	 * Método buscar valores por periodo
	 * @return Fba0101 Dados do Banco
	 */
	public Fba0101 buscarFba0101PorIdEPeriodo(Long idAbh80, LocalDate[] periodo) {
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "fba0101dtCalc") : "";
		String sql = "SELECT * FROM Fba0101 " +
					 "INNER JOIN Fba01 ON fba01id = fba0101calculo "+
					 whereData +
					 "AND fba0101trab = :idAbh80 " +
					 getSamWhere().getWherePadrao("AND", Fba01.class) +
					 " ORDER BY fba0101dtcalc DESC, fba0101id DESC";
		
		Query query = getSession().createQuery(sql);
		
		query.setParameter("idAbh80", idAbh80);
		query.setMaxResult(1);
		return (Fba0101) query.getUniqueResult(ColumnType.ENTITY);
		
	}

	/**
	 * Método buscar eventos por Recibo de Pagamento
	 * @return List<TableMap> Dados do Banco
	 */
	public List<TableMap> buscarDadosFba01011sEventosPorReciboPagamento(List<Long> idsDepartamento, List<Long> idsCargos, List<Long> idsSindicato, Long idAbh80, LocalDate[] periodo, Set<Integer> tiposFba0101, String eveFerNaoImpr, String[] eventosNaoImprimir) {
		String whereEve = eveFerNaoImpr != null ? "AND abh21codigo <> :eveFerNaoImpr " : "";
		String whereAbb11Departamento = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereFad0101Cargos = idsCargos != null && !idsCargos.isEmpty() ? "AND abh05id IN (:idsCargos) " : "";
		String whereAbh03Sindicato = idsSindicato != null && !idsSindicato.isEmpty() ? "AND abh03id IN (:idsSindicato) " : "";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "fba0101dtCalc") : "";
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";
		
		String sql = "SELECT abh21codigo, abh21nome, abh21tipo, abh80id, SUM(CASE WHEN fba01011refHoras > 0 THEN fba01011refHoras WHEN fba01011refDias > 0 THEN fba01011refDias ELSE fba01011refUnid END) as totalRef, SUM(fba01011valor) as totalValor " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Fba01 ON Fba01id = fba0101calculo "+
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
					 whereData + whereEve +
					 "AND fba0101trab = :idAbh80 " +
					 "AND fba0101tpVlr IN (:fba0101tpVlr)  " +
					 "AND abh21tipo IN (0, 1) " +
					 whereAbb11Departamento + whereFad0101Cargos + whereAbh03Sindicato + whereEventosNaoImprimir + getSamWhere().getWherePadrao("AND", Fba01.class) +
					 " GROUP BY abh21codigo, abh21nome, abh21tipo, abh80id " +
					 "HAVING SUM(fba01011vlr) > 0 " +
					 "ORDER BY abh21tipo, abh21codigo";
		
		Query query = getSession().createQuery(sql);
		
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsSindicato != null && !idsSindicato.isEmpty()) query.setParameter("idsSindicato", idsSindicato);
		if(eveFerNaoImpr != null && !eveFerNaoImpr.isEmpty()) query.setParameter("eveFerNaoImpr", eveFerNaoImpr);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		query.setParameter("idAbh80", idAbh80);
		query.setParameter("fba0101tpVlr", tiposFba0101);
		
		return query.getListTableMap();
				
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
	
	/**
	 * Método buscar valor evento ferias rescisão
	 * @return Abh21 Dados do Banco
	 */
	public BigDecimal buscarFba01011ValorEventoParaZerarFeriasERescisao(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsCargos, List<Long> idsSindicato, Long idAbh80, Set<Integer> tipoTrabalhador, String codAbb11, LocalDate[] periodo, Integer tipoFba0101, String codAbh21, String[] eventosNaoImprimir) {
		String where = idAbh80 != null ? " AND fba0101trab = :idAbh80 " : (codAbh21.equals("*") ? "" : " AND abb11codigo LIKE :abb11codigo ");
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? " AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Departamento = idsDepartamento != null && !idsDepartamento.isEmpty() ? " AND abb11id IN (:idsDepartamento) " : "";
		String whereFad0101Cargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereAbh03Sindicato = idsSindicato != null && !idsSindicato.isEmpty() ? " AND abh03id IN (:idsSindicato) " : "";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "fba0101dtCalc") : "";
		String whereEventosNaoImprimir = eventosNaoImprimir != null && eventosNaoImprimir.length > 0 ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";
		
		String sql = "SELECT SUM(fba01011valor) FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "INNER JOIN Abh03 ON abh03id = abh80sindSindical " +
					 whereData + where + whereAbb11Departamento + whereFad0101Cargos + whereAbh03Sindicato + whereAbh80Trabalhador + whereEventosNaoImprimir + getSamWhere().getWherePadrao("AND", Abh21.class) +
					 " AND abh80tipo IN (:abh80tipo) " +
					 " AND fba0101tpVlr IN (:fba0101tpVlr) " +
					 " AND abh21codigo = :abh21codigo " +
					 " AND fba01011valor > 0";
			
		Query query = getSession().createQuery(sql);
		
		query.setParameter("abh80tipo", tipoTrabalhador);
		query.setParameter("fba0101tpVlr", tipoFba0101);
		query.setParameter("abh21codigo", codAbh21);
		query.setParameter("idsTrabalhador", idsTrabalhador);
		if (eventosNaoImprimir != null && eventosNaoImprimir.length > 0) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
		
		if(idAbh80 != null) {
			query.setParameter("idAbh80", idAbh80);
		}else if(!codAbh21.equals("*")) {
			query.setParameter("abb11codigo", codAbh21.toUpperCase()+"%");
		}
				
		//return query.getListTableMap();
		return (BigDecimal) query.getUniqueResult(ColumnType.BIG_DECIMAL); // == null ? new BigDecimal(0) : (BigDecimal) query.getUniqueResult(ColumnType.BIG_DECIMAL);
	}
	
	// Buscar cargo e salario
	public Abh05 buscarCargoESalario(Long abh05id) {
		return getSession().createCriteria(Abh05.class).addWhere(Criterions.eq("abh05id", abh05id)).get();
	}
	
	// Buscar trabalhador
	public Abh80 buscarTrabalhador(Long abh80) {
		return getSession().createCriteria(Abh80.class).addWhere(Criterions.eq("abh80id", abh80)).get();
	}
	
	// Buscar CBO
	public Aap03 buscarCBO(Long aap03id) {
		return getSession().createCriteria(Aap03.class).addWhere(Criterions.eq("aap03id", aap03id)).get();
	}
	// Buscar departamento
	public Abb11 buscarDepartamento(Long abh80depto) {
		return getSession().createCriteria(Abb11.class).addWhere(Criterions.eq("abb11id", abh80depto)).get();
	}

}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlY2libyBkZSBQYWdhbWVudG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=