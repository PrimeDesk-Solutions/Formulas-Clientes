package Atilatte.cubos.cgs

import br.com.multitec.utils.Utils;
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
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("tipoEntidade","0")
        return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		String entIni = getString("entidadeIni");
		String entFin = getString("entidadeFin");
		Boolean chkPolitica = getBoolean("chkPolitica");
        Integer tipoEntidade = getInteger("tipoEntidade");

		Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;
		

		List<TableMap> dados = buscarDadosRelatorio(entIni, entFin,tipoEntidade,chkPolitica, idEmpresa);

		if(chkEntidade) return gerarXLSX("CUBO_CGS_Entidades_Politica",dados);

		return gerarXLSX("CUBO_CGS_Entidades",dados)
	}

	private List<TableMap> buscarDadosRelatorio(String entIni, String entFin, Integer tipoEntidade, Boolean chkPolitica, Long idEmpresa){

		String whereEntidade = entIni != null && entFin != null ? "AND abe01ent.abe01codigo >= :entIni AND abe01ent.abe01codigo <=  :entFin " :
                               entIni != null && entFin == null ? "AND abe01ent.abe01codigo >= :entIni " :
                               entIni == null && entFin != null ? "AND abe01ent.abe01codigo <= :entFin " : ""
		String politica = chkPolitica ? "aab03nome AS politica, " : ""
		String whereTipoEntidade = tipoEntidade == 0 ? "AND abe01ent.abe01codigo LIKE '02%' " : tipoEntidade == 1 ? "AND abe01ent.abe01codigo LIKE '01%' " : "";
		String whereEmpresa = "AND abe01ent.abe01gc = :empresa "

		Parametro parametroEntidadeIni = entIni != null ? Parametro.criar("entIni",entIni) : null ;
		Parametro parametroEntidadeFin = entFin != null ? Parametro.criar("entFin",entFin) : null ;
		Parametro parametroEmpresa = Parametro.criar("empresa", idEmpresa);
		
		String sql = "SELECT " +politica+ " abe01ent.abe01codigo AS codEntidade, abe01ent.abe01na AS naEntidade, abe01ent.abe01nome AS nomeEntidade, " +
                    "aag0201nome AS municipio, "+
					"aag02uf AS uf, CAST(abe02json ->> 'primeira_venda' AS date) AS primeiraVenda,CAST(abe02json ->> 'ultima_venda' AS date) AS ultimaVenda,CAST(abe02json ->> 'tx_fixa' AS numeric(18,2)) AS txFixa, "+
					"CAST(abe01ent.abe01camposcustom ->> 'ramo_atividade' AS text) AS ramo, "+
					"rep0.abe01codigo AS codRep0,rep0.abe01na AS naRep0, rep1.abe01codigo AS codRep1, rep1.abe01na AS naRep1,   "+
					"rep2.abe01codigo AS codRep2, rep2.abe01na AS naRep2, rep3.abe01codigo AS codRep3, "+
					"rep3.abe01na AS naRep3, rep4.abe01codigo AS codRep4, rep4.abe01na AS naRep4, "+
					"abe02txcomis0 AS txComis0,abe02txcomis1 AS txComis1,abe02txcomis2 AS txComis2,abe02txcomis3 AS txComis3,abe02txcomis4 AS txComis4, abe02vidautil, "+
					"abe01ent.abe01di AS dtInativacao, aba3001descr AS rede, desp.abe01codigo AS codDesp, desp.abe01na AS naDesp, redesp.abe01codigo AS codRedesp, redesp.abe01na AS naRedesp, "+
					"CAST(abe01ent.abe01json ->> '_reduc_bc_comissao' AS numeric(18,6)) AS percentReducao, cASe when CAST(abe01ent.abe01json ->> 'aplica_reducao' AS integer) = 0 then 'Leite (Garrafa)' else 'Todos' end AS aplicaReducao, "+
					"abe0101endereco, abe0101cep, abe0101numero, abe01ent.abe01ni AS abe01ni, abe01ent.abe01ie AS abe01ie, abe0101ddd1, abe0101fone1, aag03nome AS regiao, abe0101complem AS complemento, abe0101bairro AS bairro, abe40codigo AS tabelaPreco, abe40nome AS nomeTabPreco, " +
					"abf15codigo AS codPort, abf15nome AS descrPort, abf16codigo AS codOperacao, abf16nome AS descrOperacao "+
					"FROM abe01 AS abe01ent "+
					"INNER JOIN abe0101 ON abe0101ent = abe01id  AND abe0101principal = 1 "+
					"INNER JOIN aag0201 ON aag0201id = abe0101municipio "+
					"INNER JOIN aag02 ON aag02id= aag0201uf  "+
					"INNER JOIN abe02 ON abe02ent = abe01id "+
					"LEFT JOIN abf15 ON abf15id = abe02port "+
					"LEFT JOIN abf16 ON abf16id = abe02oper "+
					"LEFT JOIN abe40 ON abe40id = abe02tp " +
					"LEFT JOIN abe01 rep0 ON rep0.abe01id = abe02rep0 "+
					"LEFT JOIN abe01 rep1 ON rep1.abe01id = abe02rep1 "+
					"LEFT JOIN abe01 rep2 ON rep2.abe01id = abe02rep2  "+
					"LEFT JOIN abe01 rep3 ON rep3.abe01id = abe02rep3 "+
					"LEFT JOIN abe01 rep4 ON rep4.abe01id = abe02rep4 "+
					"LEFT JOIN abe01 AS desp ON desp.abe01id = abe02despacho "+
					"LEFT JOIN abe01 AS redesp ON redesp.abe01id = abe02redespacho "+
					"LEFT JOIN abe0103 ON abe0103ent = abe01ent.abe01id "+
					"LEFT JOIN aba3001 ON aba3001id = abe0103criterio AND aba3001criterio = 18145240 "+
					"LEFT JOIN aba30 ON aba30id = abe0103criterio  "+
					"LEFT JOIN aab03 ON CAST(aab03id AS text) = abe01ent.abe01psuso "+
					"LEFT JOIN aag03 ON aag03id = abe0101regiao "+
					"WHERE TRUE "+
					whereEntidade  +
					whereTipoEntidade +
					whereEmpresa + 
					"ORDER BY abe01ent.abe01codigo "

					return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEntidadeIni,parametroEntidadeFin,parametroEmpresa);
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk9TIC0gQ0dTIC0gRW50aWRhZGVzIiwidGlwbyI6InJlbGF0b3JpbyJ9