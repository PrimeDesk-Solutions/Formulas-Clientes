package MM.relatorios.scf;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.TableMapDataSource
import java.util.Map;
import java.util.HashMap;




public class SCF_DemonstrativosAdiantamento extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SCF - Demonstrativos de Adiantamentos"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		List<Long> adiantamentos = getListLong("adiantamentos");
		List<Long> entidades = getListLong("entidades");
		Integer tipo = getInteger("tipo");

		Aac10 empresa = obterEmpresaAtiva();
          Long idEmpresa = empresa.aac10id;

		List<TableMap> dados = new ArrayList();
		List<TableMap> listAdiantamento = buscarAdiantamentos(adiantamentos,entidades,idEmpresa);

		List<TableMap> listLancamentosAdiant = new ArrayList();
		List<TableMap> listAmortizacoes = new ArrayList();
		List<TableMap> listItens = new ArrayList();

		def aux = 0
		for(tmAdiantamento in listAdiantamento ){
			aux++
			Long idDocumento = tmAdiantamento.getLong("dad01id");

			tmAdiantamento.put("key",idDocumento)

			List<TableMap>tmLancamentos = buscarLancamentosAdiantamento(idDocumento);
			List<TableMap>tmAmortizacoes = buscarAmortizacoes(idDocumento);
			List<TableMap>tmItens = buscarItens(idDocumento);

			BigDecimal totAdiantamento = buscarTotalAdiantamento(idDocumento);
			BigDecimal totAmortizacao = buscarTotalAmortizacoes(idDocumento)

			for(lancamento in tmLancamentos){
				lancamento.put("key",idDocumento);
				listLancamentosAdiant.add(lancamento);
			}

			for(amortizacao in tmAmortizacoes){
				amortizacao.put("key",idDocumento);
				amortizacao.put("valor", amortizacao.getBigDecimal("valor") * -1)
				listAmortizacoes.add(amortizacao)
			}

			for(item in tmItens){
				item.put("key",idDocumento);
				listItens.add(item)
			}

			tmAdiantamento.put("totAdiantamento",totAdiantamento);
			tmAdiantamento.put("totAmortizacao",totAmortizacao);
			tmAdiantamento.put("aux",aux);
			
			dados.add(tmAdiantamento)
		}
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
		dsPrincipal.addSubDataSource("DsSub1", listLancamentosAdiant, "key", "key");
		dsPrincipal.addSubDataSource("DsSub2", listAmortizacoes, "key", "key");
		dsPrincipal.addSubDataSource("DsSub3", listItens, "key", "key");
		
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCF_DemonstrativoAdiantamentos_s1"));
		adicionarParametro("StreamSub2", carregarArquivoRelatorio("SCF_DemonstrativoAdiantamentos_s2"));
		adicionarParametro("StreamSub3", carregarArquivoRelatorio("SCF_DemonstrativoAdiantamentos_s3"));

		params.put("empresa",empresa.getAac10codigo() +"-"+empresa.getAac10na())

		return gerarPDF("SCF_DemonstrativoAdiantamentos",dsPrincipal)
	}

	private List<TableMap> buscarAdiantamentos(List<Long>adiantamentos,List<Long>entidades, Long empresa){
		
		String whereAdiantamentos = adiantamentos != null && adiantamentos.size() > 0 ? "and dad01id in (:adiantamentos) " : "";
		String whereEntidades = entidades != null && entidades.size() > 0 ? "and abe01id in (:entidades) " : "";
		String whereEmpresa = "and dad01gc = :empresa "

		Parametro parametroAdiantamento = adiantamentos != null && adiantamentos.size() > 0 ? Parametro.criar("adiantamentos",adiantamentos) : null;
		Parametro parametroentidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades",entidades) : null;
		Parametro parametroEmpresa = Parametro.criar("empresa", empresa);

		String sql = "select dad01id, dad01nome, abe01codigo, abe01na, to_char(dad01dti,'dd/mm/yyyy') as dtIni, to_char(dad01dtf,'dd/mm/yyyy') as dtfim, abe0101endereco, abe0101numero,abe0101bairro, "+
					"aag0201nome as cidade, aag02uf as uf, abe0101ddd1 as dd, abe0101fone1 as fone, abe0101email as email, abe01ni as ni, dad01saldo as saldo "+
					"from dad01  "+
					"inner join abe01 on abe01id = dad01ent "+
					"inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
					"inner join aag0201 on aag0201id = abe0101municipio "+
					"inner join aag02 on aag02id = aag0201uf "+
					"where true "+
					whereAdiantamentos + 
					whereEntidades + 
					whereEmpresa

					return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroAdiantamento,parametroentidades,parametroEmpresa)
					
					
	}

	private List<TableMap> buscarLancamentosAdiantamento(Long id){
		//String whereTipo = tipo == 0 ? "and dab10mov = 0 " : tipo == 1 ? "and dab10mov = 1 " : "and dab10mov in (0,1) "
		 
		String sql = "select dab10data as dtLcto, dab10valor as valorLcto, dab01codigo as codigoConta, dab01nome as nomeConta, dab10historico "+
					"from dab10 "+
					"INNER JOIN dab01 ON dab01id = dab10cc "+
					"where dab10cashback = :id "//+
					//whereTipo;
					
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("id",id));
	}

	private List<TableMap> buscarAmortizacoes(Long id){
		//String whereTipo = tipo == 0 ? "and dad0101es = 0 " : tipo == 1 ? "and dad01010es = 1 " : "and dad0101es in (0,1) "
		
		String sql = "select dad0101data as dtAmortizacao, aah01codigo as codTipoDoc, aah01nome as descrTipoDoc, abb01num as numDoc, abb01data as dtEmissao, dad0101valor as valor "+
					"from dad01 "+
					"inner join dad0101 on dad0101cb = dad01id "+
					"inner join abb01 on abb01id = dad0101central "+
					"inner join aah01 on aah01id = abb01tipo "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where dad01id = :id " 
					//whereTipo;
					
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("id",id));
	}

	private List<TableMap> buscarItens(Long id){
		String sql = "select aah01codigo as codTipoDoc, aah01nome as nomeTipoDoc,abb01nota.abb01num as numDoc, abb01pedido.abb01num as numPedido, "+
					"abb01nota.abb01data as dtEmissNota,eaa0103nota.eaa0103dtentrega as dtEntrega, abm01codigo, aam06codigo as umu, eaa0103nota.eaa0103qtComl as qtd,  "+
					"eaa0103nota.eaa0103total as total "+
					"from dad01 "+
					"inner join dad0101 on dad0101cb = dad01id "+
					"inner join abb01 as abb01nota on abb01nota.abb01id = dad0101central "+
					"inner join aah01 on aah01id = abb01tipo "+
					"inner join eaa01 as eaa01nota on eaa01nota.eaa01central = abb01nota.abb01id "+
					"inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id "+
					"inner join abm01 on abm01id = eaa0103nota.eaa0103item "+
					"inner join aam06 on aam06id = abm01umu "+
					"left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id "+
					"left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv "+
					"left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc "+
					"left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central "+
					"where dad01id = :id ";
					
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("id",id));
	}

	private BigDecimal buscarTotalAdiantamento(Long id){
		String whereTipo = tipo == 0 ? "and dab10mov = 0 " : tipo == 1 ? "and dab10mov = 1 " : "and dab10mov in (0,1) "
		 
		String sql = "select SUM(dab10valor) as totAdiantamento "+
					"from dab10 "+
					"INNER JOIN dab01 ON dab01id = dab10cc "+
					"where dab10cashback = :id "+
					whereTipo;
					
		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("id",id));
	}

	private BigDecimal buscarTotalAmortizacoes(Long id){
		String whereTipo = tipo == 0 ? "and dad0101es = 0 " : tipo == 1 ? "and dad01010es = 1 " : "and dad0101es in (0,1) "
		
		String sql = "select SUM(dad0101valor) as totAmortizacao "+
					"from dad01 "+
					"inner join dad0101 on dad0101cb = dad01id "+
					"inner join abb01 on abb01id = dad0101central "+
					"inner join aah01 on aah01id = abb01tipo "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where dad01id = :id " +
					whereTipo;
					
		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("id",id));
	}

	
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERlbW9uc3RyYXRpdm9zIGRlIEFkaWFudGFtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=