package multitec.relatorios.spv;

import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SPV_Vale extends RelatorioBase {
		
	@Override
	public String getNomeTarefa() {
		return "SPV - Vale";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("valor", BigDecimal.ZERO);
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {		
		BigDecimal valor = getBigDecimal("valor");
				
		List<TableMap> listTMDados = new ArrayList<>();
		
		TableMap tmDado = buscarDadosEmpresa();
		tmDado.put("valor", valor);
		
		tmDado.put("aab10user", getVariaveis().aab10.aab10user);
		tmDado.put("aab10nome", getVariaveis().aab10.aab10nome);
		
		listTMDados.add(tmDado);
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
				
		return gerarPDF("SPV_Vale", dsPrincipal);
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
	
}
//meta-sis-eyJkZXNjciI6IlNQViAtIFZhbGUiLCJ0aXBvIjoicmVsYXRvcmlvIn0=