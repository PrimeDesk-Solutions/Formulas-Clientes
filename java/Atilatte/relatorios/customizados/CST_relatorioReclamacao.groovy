package Atilatte.relatorios.customizados;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate


public class CST_relatorioReclamacao extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Relatório Reclamações"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		Integer numIni = getInteger("numeroInicial");
		Integer numFin = getInteger("numeroFinal");

		List<TableMap> listReclamacao = new ArrayList();
		List<TableMap> dados = buscarDocumentosReclamacoes(numIni, numFin);

		for(TableMap registro : dados){
			Long idDoc = registro.getLong("caa10id");
			registro.put("key",idDoc);
			List<TableMap> reclamacoes =  buscarListaDeReclamacoes(idDoc);
			for(TableMap reclamacao : reclamacoes){
				reclamacao.put("key",idDoc);
				listReclamacao.add(reclamacao);
			}
		}

		TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
		dsPrincipal.addSubDataSource("dsRec", listReclamacao, "key", "key");
		adicionarParametro("SUBREPORT_DIR", carregarArquivoRelatorio("relatorioReclamacao_subreport1"))


		params.put("empresa", getVariaveis().getAac10().getAac10codigo() + "-" +getVariaveis().getAac10().getAac10na());;

		return gerarPDF("CST_relatorioReclamacao",dsPrincipal);
		//throw new ValidacaoException(dados.toString());
		
		
	}

	private buscarDocumentosReclamacoes(Integer numIni, Integer numFin){
		Query sql = getSession().createQuery("select caa10id,caa10num,caa10data,aab10user,caa10dtenc,abm01codigo, abm01na, cast(caa10json ->> 'tipo_atendimento' as character varying(30)) as tipoAtend,"+
										"cast(caa10json ->> 'responsavel' as character varying(50)) as responsavel, cast(caa10json ->> 'tel_contato' as character varying(20)) as telContato,"+
										"cast(caa10json ->> 'validade' as date) as validade, cast(caa10json ->> 'data' as date) as dataFabricao, cast(caa10json ->> 'lote' as character varying(50)) as lote, caa10loccpra,abe0101ddd1 as dd1, abe0101fone1 as fone1, abe0101ddd2 as ddd2, abe0101fone2 as fone2,"+
										"abe01na as nomeEntidade, abe0101endereco as endereco, abe0101bairro, aag0201nome as cidade, abe0101numero as numero, aag02uf, abe0101cep as cep,"+  
										"abe0101complem as complemento,abe0101cp as caixaPostal, abe0101email as email, caa10obsRec as observacao "+
										"from caa10 "+
										"inner join aab10 on aab10id = caa10user "+
										"inner join abm01 on abm01id = caa10item "+
										"left join abe01 on abe01id = caa10ent "+
										"left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
										"left join aag0201 on aag0201id = abe0101regiao "+
										"left join aag02 on aag02id = aag0201uf "+ 
										"where caa10num between :numIni and :numFin");
		if(numIni != null && numFin != null ){
			sql.setParameter("numIni",numIni);
			sql.setParameter("numFin",numFin);
			
		}
		return sql.getListTableMap();
	}

	private buscarListaDeReclamacoes(Long idDoc){
		Query rec = getSession().createQuery("select caa01codigo, caa01descr " +
									"from caa1001 "+
									"inner join caa01 on caa01id = caa1001rec "+
									"where caa1001atend = '292' ");
		return rec.getListTableMap();
	}
	
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJlbGF0w7NyaW8gUmVjbGFtYcOnw7VlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==