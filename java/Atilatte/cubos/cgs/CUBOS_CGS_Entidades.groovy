package Atilatte.cubos.cgs;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.aa.Aac10;
import java.util.Map;
import java.util.HashMap;

// Teste hoje
public class CUBOS_CGS_Entidades extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBOS - CGS - Entidades"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		String entIni = getString("entidadeIni");
		String entFin = getString("entidadeFin");
		Boolean chkEntidade = getBoolean("chkPolitica");
		Boolean chkClientes = getBoolean("chkCliente");

		Aac10 empresa = obterEmpresaAtiva();
          Long idEmpresa = empresa.aac10id;
		

		List<TableMap> dados = buscarDadosRelatorio(entIni, entFin,chkEntidade, chkClientes,idEmpresa);

		if(chkEntidade) return gerarXLSX("CUBO_CGS_Entidades_Politica",dados);

		return gerarXLSX("CUBO_CGS_Entidades",dados)
	}

	private List<TableMap> buscarDadosRelatorio(String entIni, String entFin, Boolean chkEntidade, Boolean chkClientes, Long idEmpresa){

		String whereEntidade = entIni != null && entFin != null ? "and abe01ent.abe01codigo >= :entIni and abe01ent.abe01codigo <=  :entFin " : entIni != null && entFin == null ? "and abe01ent.abe01codigo >= :entIni " : entIni == null && entFin != null ? "and abe01ent.abe01codigo <= :entFin " : "" 
		String politica = chkEntidade ? "aab03nome as politica, " : ""
		String whereClientes = chkClientes ? "and abe01ent.abe01codigo like '02%' " : "";
		String whereEmpresa = "and abe01ent.abe01gc = :empresa "

		Parametro parametroEntidadeIni = entIni != null ? Parametro.criar("entIni",entIni) : null ;
		Parametro parametroEntidadeFin = entFin != null ? Parametro.criar("entFin",entFin) : null ;
		Parametro parametroEmpresa = Parametro.criar("empresa", idEmpresa);
		
		String sql = "select " +politica+ " abe01ent.abe01codigo as codEntidade, abe01ent.abe01na as naEntidade, abe01ent.abe01nome as nomeEntidade, aag0201nome as municipio, "+
					"aag02uf as uf, cast(abe02json ->> 'primeira_venda' as date) as primeiraVenda,cast(abe02json ->> 'ultima_venda' as date) as ultimaVenda,cast(abe02json ->> 'tx_fixa' as numeric(18,2)) as txFixa, "+
					"cast(abe01ent.abe01camposcustom ->> 'ramo_atividade' as text) as ramo, "+
					"rep0.abe01codigo as codRep0,rep0.abe01na as naRep0, rep1.abe01codigo as codRep1, rep1.abe01na as naRep1,   "+
					"rep2.abe01codigo as codRep2, rep2.abe01na as naRep2, rep3.abe01codigo as codRep3, "+
					"rep3.abe01na as naRep3, rep4.abe01codigo as codRep4, rep4.abe01na as naRep4, "+
					"abe02txcomis0 as txComis0,abe02txcomis1 as txComis1,abe02txcomis2 as txComis2,abe02txcomis3 as txComis3,abe02txcomis4 as txComis4, abe02vidautil, "+
					"abe01ent.abe01di as dtInativacao, aba3001descr as rede, desp.abe01codigo as codDesp, desp.abe01na as naDesp, redesp.abe01codigo as codRedesp, redesp.abe01na as naRedesp, "+
					"cast(abe01ent.abe01json ->> '_reduc_bc_comissao' as numeric(18,6)) as percentReducao, case when cast(abe01ent.abe01json ->> 'aplica_reducao' as integer) = 0 then 'Leite (Garrafa)' else 'Todos' end as aplicaReducao, "+
					"abe0101endereco, abe0101cep, abe0101numero, abe01ent.abe01ni as abe01ni, abe01ent.abe01ie as abe01ie, abe0101ddd1, abe0101fone1, aag03nome as regiao, abe0101complem as complemento, abe0101bairro as bairro, abe40codigo as tabelaPreco, abe40nome as nomeTabPreco " +
					",abf15codigo as codPort, abf15nome as descrPort, abf16codigo as codOperacao, abf16nome as descrOperacao "+
					"from abe01 as abe01ent "+
					"inner join abe0101 on abe0101ent = abe01id  and abe0101principal = 1 "+
					"inner join aag0201 on aag0201id = abe0101municipio "+
					"inner join aag02 on aag02id= aag0201uf  "+
					"inner join abe02 on abe02ent = abe01id "+
					"left join abf15 on abf15id = abe02port "+
					"left join abf16 on abf16id = abe02oper "+
					"left join abe40 on abe40id = abe02tp " +
					"left join abe01 rep0 on rep0.abe01id = abe02rep0 "+
					"left join abe01 rep1 on rep1.abe01id = abe02rep1 "+
					"left join abe01 rep2 on rep2.abe01id = abe02rep2  "+
					"left join abe01 rep3 on rep3.abe01id = abe02rep3 "+
					"left join abe01 rep4 on rep4.abe01id = abe02rep4 "+
					"left join abe01 as desp on desp.abe01id = abe02despacho "+
					"left join abe01 as redesp on redesp.abe01id = abe02redespacho "+
					"left join abe0103 on abe0103ent = abe01ent.abe01id "+
					"left join aba3001 on aba3001id = abe0103criterio and aba3001criterio = 18145240 "+
					"left join aba30 on aba30id = abe0103criterio  "+
					"left join aab03 on cast(aab03id as text) = abe01ent.abe01psuso "+
					"left join aag03 on aag03id = abe0101regiao "+
					"where true "+
					whereEntidade  +
					whereClientes +
					whereEmpresa + 
					"order by abe01ent.abe01codigo "

					return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEntidadeIni,parametroEntidadeFin,parametroEmpresa);
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk9TIC0gQ0dTIC0gRW50aWRhZGVzIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNVQk9TIC0gQ0dTIC0gRW50aWRhZGVzIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNVQk9TIC0gQ0dTIC0gRW50aWRhZGVzIiwidGlwbyI6InJlbGF0b3JpbyJ9