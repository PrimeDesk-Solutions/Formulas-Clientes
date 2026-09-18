package multitec.relatorios.sgc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros
import sam.model.entities.ab.Aba01;
import sam.model.entities.eb.Eba10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SGC_Agrupamento extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SGC - Agrupamento de Contas";
	}

	@Override
	public DadosParaDownload executar() {
		Long idAgrupamento = getLong("agrupamento");
		LocalDate anoMes = DateUtils.parseDate("01/" + getString("dataInicial"));
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Agrupamento de Contas");
		params.put("MESANO", anoMes.format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		Integer detalhamento = getInteger("detalhamento");
		
		Aba01 aba01_EB_ESTRCODAGRUP = getParametro(Parametros.EB_ESTRCODAGRUP);
									   
		if(aba01_EB_ESTRCODAGRUP == null || aba01_EB_ESTRCODAGRUP.getString() == null || aba01_EB_ESTRCODAGRUP.getString().length() == 0) 
			throw new ValidacaoException("Necessário ter o parâmetro EB-ESTRCODAGRUP preenchido.");
		
		List<TableMap> contas = new ArrayList<>(); 
		List<Integer> grupos = new ArrayList<>();
		
		getEstrutura(aba01_EB_ESTRCODAGRUP.getString(), grupos, 0);
		
		if(grupos == null || grupos.size() == 0) throw new ValidacaoException("Não foi definida a estrutura de códigos de agrupamento.");
		
		if(idAgrupamento == null)  throw new ValidacaoException("Necessário informar o agrupamento.");
		
		String codAgrupamento = getSession().createQuery(" SELECT eba10codigo FROM Eba10 WHERE eba10id = :idAgrupamento ")
											.setParameter("idAgrupamento", idAgrupamento)
											.getUniqueResult(ColumnType.STRING);
			
		if(codAgrupamento == null)  throw new ValidacaoException("Não foi encontrado o agrupamento.");
		
		int tamAgrupamento = codAgrupamento.length();
		
		List<TableMap> listTMEba10s = getSession().createQuery(" SELECT eba10codigo, eba10nome FROM Eba10 ",
															   " WHERE SUBSTR(UPPER(eba10codigo), 1, :tamAgrupamento) = :codAgrupamento ",
															   getSamWhere().getWherePadrao(" AND ", Eba10.class),
															   " ORDER BY eba10codigo")
												  .setParameter("tamAgrupamento", tamAgrupamento)
												  .setParameter("codAgrupamento", codAgrupamento)
												  .getListTableMap();
		
		if(listTMEba10s == null || listTMEba10s.size() == 0)  throw new ValidacaoException("Não foram encontrados os agrupamentos.");
		
		List<TableMap> listTMSaldosCorrente = getSession().createQuery(" SELECT eba10codigo, abc10codigo, abc10nome, ",
																	   " SUM(ebb02deb) AS deb, SUM(ebb02cred) AS cred ",
																	   " FROM Ebb02 ",
																	   " INNER JOIN Abc10 ON ebb02cta = abc10id ",
																	   " INNER JOIN Eba1001 ON ebb02cta = eba1001cta ",
																	   " INNER JOIN Eba10 ON eba1001agrup = eba10id ",
																	   " WHERE SUBSTR(UPPER(eba10codigo), 1, :tamAgrupamento) = :codAgrupamento ",
																	   " AND ", Fields.numMeses("ebb02mes", "ebb02ano"), " <= :anoMes ",
																	    getSamWhere().getWherePadrao(" AND ", Eba10.class),  
																	   " GROUP BY eba10codigo, abc10codigo, abc10nome ",
																	   " ORDER BY eba10codigo, abc10codigo")
														  .setParameter("tamAgrupamento", tamAgrupamento)
														  .setParameter("codAgrupamento", codAgrupamento)
														  .setParameter("anoMes", Criterions.valNumMeses(anoMes.getMonthValue(), anoMes.getYear()))
														  .getListTableMap();
		
		List<TableMap> dados = new ArrayList<>();
		
		for(TableMap tmEba10 : listTMEba10s) {
			String codigo = tmEba10.getString("eba10codigo");
			int tamCodigo = codigo.length();
			
			BigDecimal saldo = BigDecimal.ZERO;
			if(listTMSaldosCorrente != null && listTMSaldosCorrente.size() > 0) {
				for(TableMap tmSaldo : listTMSaldosCorrente) {
					String eba10codigo = tmSaldo.getString("eba10codigo");
					
					if(codigo.equals(eba10codigo.substring(0, tamCodigo))){
						BigDecimal deb = tmSaldo.getBigDecimal("deb");
						BigDecimal cred = tmSaldo.getBigDecimal("cred");
						BigDecimal saldoCorrente = deb.subtract(cred);
						saldo = saldo.add(saldoCorrente);
					}
				}
			}
			
			String tab = "";
			if(codigo.length() != tamAgrupamento) {
				tab = StringUtils.ajustString("", codigo.length());
			}
			
			TableMap tmDado = new TableMap();
			tmDado.put("codigo", tab + codigo);
			tmDado.put("nome", tmEba10.getString("eba10nome"));
			tmDado.put("saldo", saldo);
			dados.add(tmDado);
			
			if(detalhamento == 1) {
				if(tamCodigo == grupos.get(grupos.size()-1)) {
					if(listTMSaldosCorrente != null && listTMSaldosCorrente.size() > 0) {
						for(TableMap tmSaldo : listTMSaldosCorrente) {
							String eba10codigo = tmSaldo.getString("eba10codigo");
							
							if(codigo.equals(eba10codigo)){
								BigDecimal deb = tmSaldo.getBigDecimal("deb");
								BigDecimal cred = tmSaldo.getBigDecimal("cred");
								BigDecimal saldoCorrente = deb.subtract(cred);

								tab = StringUtils.ajustString("", tamCodigo+3);
								
								TableMap tmDadoConta = new TableMap();
								tmDadoConta.put("codigo", tab + tmSaldo.getString("abc10codigo"));
								tmDadoConta.put("nome", tmSaldo.getString("abc10nome"));
								tmDadoConta.put("saldo", saldoCorrente);
								dados.add(tmDadoConta);
							}
						}
					}
				}
			}
		}
		
		return gerarPDF("SGC_Agrupamento_R1", dados);
	}

	private void getEstrutura(String estrutura, List<Integer> grupos, int tamanhoTotal) {
		tamanhoTotal = tamanhoTotal + StringUtils.substringBeforeFirst(estrutura, "|").length();
		grupos.add(new Integer(tamanhoTotal));
		estrutura = StringUtils.substringAfterFirst(estrutura, "|");
		if(!estrutura.contains("|")) {
			grupos.add(new Integer(tamanhoTotal + estrutura.length()));
			tamanhoTotal = tamanhoTotal + estrutura.length();
		}else {
			getEstrutura(estrutura, grupos, tamanhoTotal);
		}
	}
	
	private Aba01 getParametro(br.com.multitec.utils.dicdados.Parametro param) {
		return getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", param.getAplic()))
				.addWhere(getSamWhere().getCritPadrao(Aba01.class))
				.get();
	}
	
	private void configurarMsgAtualizarCtas(Map<String, Object> filtrosDefault) {
		Aba01 aba01_EB_ATUALIZARCTAS = getParametro(Parametros.EB_ATUALIZARCTAS);
		String atualizarCtas = null;
		if(aba01_EB_ATUALIZARCTAS != null && aba01_EB_ATUALIZARCTAS.getString() != null && aba01_EB_ATUALIZARCTAS.getInteger() == 1) {
			atualizarCtas = "É necessário atualizar os saldos das contas contábeis.";
		}
		filtrosDefault.put("atualizarCtas", atualizarCtas);
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("detalhamento", "0");
		filtrosDefault.put("dataInicial", MDate.date().format(DateTimeFormatter.ofPattern("MM/yyyy")));
		
		configurarMsgAtualizarCtas(filtrosDefault);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
}
//meta-sis-eyJkZXNjciI6IlNHQyAtIEFncnVwYW1lbnRvIGRlIENvbnRhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==