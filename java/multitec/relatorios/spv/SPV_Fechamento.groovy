package multitec.relatorios.spv;

import java.time.LocalDate
import java.time.LocalTime

import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SPV_Fechamento extends RelatorioBase {
		
	@Override
	public String getNomeTarefa() {
		return "SPV - Fechamento";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		LocalDate data = getLocalDate("data");
		LocalTime hora = getLocalTime("hora");
		Long dab01id = getLong("dab01id");
		Long aab10id = getLong("aab10id");
		List<TableMap> listFormasPgto = get("listFormasPgto");
		BigDecimal totalEntradas = getBigDecimal("totalEntradas");
		BigDecimal totalSaidas = getBigDecimal("totalSaidas");
		BigDecimal totalSaldo = getBigDecimal("totalSaldo");
		
		if(dab01id == null) throw new ValidacaoException("Não foi informado o caixa.");
		if(aab10id == null) throw new ValidacaoException("Não foi informado o usuário.");
		if(listFormasPgto == null) throw new ValidacaoException("Não foram informadas as formas de pagamento.");
		if(totalEntradas == null) throw new ValidacaoException("Não foi informado o total das entradas.");
		if(totalSaidas == null) throw new ValidacaoException("Não foi informado o total das saídas.");
		if(totalSaldo == null) throw new ValidacaoException("Não foi informado o total do saldo.");
		
		List<TableMap> listTMDados = new ArrayList<>();
		
		TableMap tmFechamento = new TableMap();
		tmFechamento.put("data", data);
		tmFechamento.put("hora", hora);
		
		TableMap tmEmpresa = buscarDadosEmpresa();
		tmFechamento.putAll(tmEmpresa);
		
		TableMap tmContaCorrente = buscarDadosContaCorrente(dab01id);
		tmFechamento.putAll(tmContaCorrente);
		
		TableMap tmUsuario = buscarDadosUsuario(aab10id);
		tmFechamento.putAll(tmUsuario);
		
		tmFechamento.put("key", dab01id);
		for(TableMap tmFormaPgto : listFormasPgto) {
			tmFormaPgto.put("key", dab01id);
		}
		
		tmFechamento.put("totalEntradas", totalEntradas);
		tmFechamento.put("totalSaidas", totalSaidas);
		tmFechamento.put("totalSaldo", totalSaldo);
		
		listTMDados.add(tmFechamento);
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		dsPrincipal.addSubDataSource("dsFormasPgto", listFormasPgto, "key", "key");
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPV_Fechamento_S1"))
		
		return gerarPDF("SPV_Fechamento", dsPrincipal);
	}
	
	private TableMap buscarDadosEmpresa() {
		return getSession().createQuery(
				" SELECT aac10rs, aac10na, aac10endereco, aac10numero, aac10complem, ",
				" aac10bairro, aac10dddFone, aac10fone, aac10ni, aac10cep, aag0201nome as aag0201nome_emp, aag02uf as aag02uf_emp ",
				" FROM Aac10 ",
				" LEFT JOIN Aag0201 ON aag0201id = aac10municipio ",
				" LEFT JOIN Aag02 ON aag02id = aag0201uf ",
				" WHERE aac10id = :idAac10 ")
			.setParameter("idAac10", getVariaveis().getAac10().getAac10id())
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private TableMap buscarDadosContaCorrente(Long dab01id) {
		return getSession().createQuery(
				" SELECT dab01id, dab01codigo, dab01nome ",
				" FROM Dab01 ",
				" WHERE dab01id = :dab01id ")
			.setParameter("dab01id", dab01id)
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private TableMap buscarDadosUsuario(Long aab10id) {
		return getSession().createQuery(
				" SELECT aab10id, aab10user, aab10nome ",
				" FROM Aab10 ",
				" WHERE aab10id = :aab10id ")
			.setParameter("aab10id", aab10id)
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
}
//meta-sis-eyJkZXNjciI6IlNQViAtIEZlY2hhbWVudG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=