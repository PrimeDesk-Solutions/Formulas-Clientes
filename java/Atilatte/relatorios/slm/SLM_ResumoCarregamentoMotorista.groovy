package Atilatte.relatorios.slm;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;

public class SLM_ResumoCarregamentoMotorista extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SLM - Resumo Carregamento Motoristas"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		String capaLoteIni = getString("capaLoteIni");
		String capaLoteFin = getString("capaLoteFin");
		Long idEntIni = getLong("entIni");
		Long idEntFin = getLong("entFin");

		List<TableMap> dados = buscarDadosRelatorio(capaLoteIni,capaLoteFin,idEntIni,idEntFin);

		 params.put("titulo","Resumo Carregamento (Motoristas)");

		return gerarPDF("SLM_ResumoCarregamentoMotoristas",dados)
		
	}

	private List<TableMap> buscarDadosRelatorio(String capaLoteIni,String capaLoteFin,Long idEntIni,Long idEntFin){
		String whereCapaLote = capaLoteIni != null && capaLoteFin != null ? "and bfb01lote between :capaLoteIni and :capaLoteFin ": "";
		String whereEntidade = idEntIni != null && idEntFin != null ? "and cliente.abe01id between :idEntIni and :idEntFin " : idEntIni != null && idEntFin == null ? "and cliente.abe01id >= :idEntIni " : idEntIni == null && idEntFin != null ? "and cliente.abe01id <= :idEntFin " : "";

		Parametro parametroCapaLoteIni = capaLoteIni != null ? Parametro.criar("capaLoteIni",capaLoteIni) : null;
		Parametro parametroCapaLoteFin = capaLoteFin != null ? Parametro.criar("capaLoteFin",capaLoteFin) : null;
		Parametro parametroEntidadeIni = idEntIni != null ? Parametro.criar("idEntIni",idEntIni) : null;
		Parametro parametroEntidadeFin = idEntFin != null ? Parametro.criar("idEntFin",idEntFin) : null;
		
		String sql = "select distinct upper(bfb01lote) as lote, abb01num, cliente.abe01codigo as codCliente, cliente.abe01na as naCliente,abe0101endereco as endereco, abe0101bairro as bairro,aag0201nome as municipio "+
					"from bfb01 "+
					"inner join bfb0101 on bfb0101lote = bfb01id "+
					"inner join bfb01011 on bfb01011doc = bfb0101id "+
					"inner join abb01 on abb01id = bfb0101central  "+
					"inner join eaa01 on eaa01central = abb01id  "+
					"inner join eaa0103 on eaa0103id = bfb01011item "+
					"inner join abe01 as cliente on cliente.abe01id = abb01ent "+
					"inner join abe0101 on abe0101ent = abe01id and abe0101entrega = 1 "+
					"inner join aag0201 on aag0201id = abe0101municipio  "+
					"inner join aag02 on aag02id = aag0201uf  "+
					whereCapaLote +
					whereEntidade ;

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroCapaLoteIni,parametroCapaLoteFin,parametroEntidadeIni,parametroEntidadeFin);
	}
}
//meta-sis-eyJkZXNjciI6IlNMTSAtIFJlc3VtbyBDYXJyZWdhbWVudG8gTW90b3Jpc3RhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==