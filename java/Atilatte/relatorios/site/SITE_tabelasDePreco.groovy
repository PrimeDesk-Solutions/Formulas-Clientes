package Atilatte.relatorios.site

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ab.Abe01;
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
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins;
import sam.model.entities.ab.Abe05;
import sam.model.entities.aa.Aab10;


public class SITE_tabelasDePreco extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SITE - Tabelas de Preço"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();

		Long idEnt  = buscarEntidadeDoRepresentante(obterUsuarioLogado().aab10id);
		
		List<Long> entidades = getSession()
				.createCriteria(Abe01.class)
				.addFields("abe01id")
				.addJoin(Joins.join("abe02","abe02ent = abe01id"))
				.addWhere(Criterions.where("(abe02rep0 = "+idEnt+" or abe02rep1 = "+idEnt+" or abe02rep2 = "+idEnt+" or abe02rep3 = "+idEnt+" or abe02rep4 = "+idEnt+" )"))
				.getList(ColumnType.LONG);
		if(entidades.size() > 0){
			filtrosDefault.put("idEntidades",entidades);
		}else{
			filtrosDefault.put("idEntidades",idEnt);
		}
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		

		String entidadeIni = getString("entidadeIni");
		String entidadeFin = getString("entidadeFin");
		String prodIni = getString("itemIni");
		String prodFin = getString("itemFin");
		String tabIni = getString("tabelaIni");
		String tabFin = getString("tabelaFin");
		def user = obterUsuarioLogado();
		Long idUser = user.aab10id;

		Long idEntidade = buscarEntidadeDoRepresentante(idUser);
		
	
		List<TableMap> tmItensTabela = new ArrayList();
		List<TableMap> tmEntidades = new ArrayList();
		List<TableMap> listTabelasPrecos = buscarTabelasPreco(tabIni, tabFin);
		List<TableMap> dados = new ArrayList();
		
		for(TableMap tabela : listTabelasPrecos){
			Long idTabela = tabela.getLong("abe40id");
			List<TableMap> listEntidades = buscarEntidades(idTabela,entidadeIni,entidadeFin, idEntidade);
			
			if(listEntidades.size() != 0){
				tabela.put("key",idTabela)
				List<TableMap> listItens = buscarItensTabelaPreco(idTabela,prodIni,prodFin);
				

				for(TableMap itens : listItens){
					itens.put("key",idTabela);
					tmItensTabela.add(itens);
				}
				for(TableMap entidade : listEntidades){
					entidade.put("key",idTabela);
					tmEntidades.add(entidade);
				}
				dados.add(tabela);
			}	
		}

		TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
		dsPrincipal.addSubDataSource("dsItens", tmItensTabela, "key", "key");
		dsPrincipal.addSubDataSource("dsEntidades", tmEntidades, "key", "key");
		adicionarParametro("SUBREPORT_DIR", carregarArquivoRelatorio("SITE_tabelasDePreco_S1"));
		adicionarParametro("SUBREPORT_DIR2", carregarArquivoRelatorio("SITE_tabelasDePreco_S2"));

		params.put("empresa", getVariaveis().getAac10().getAac10codigo() +"-"+getVariaveis().getAac10().getAac10na())
		params.put("titulo","Tabela de Preços");

		return gerarPDF("SITE_tabelasDePreco", dsPrincipal);
		
	}

	private List<TableMap>buscarTabelasPreco(String tabIni, String tabFin){
		Query sqlTabelasPreco = getSession().createQuery("select abe40id,abe40codigo,abe40nome,abe40dtcriacao,abe40dtvcto "+ 
												"from abe40 "+ 
												(tabIni != null && tabFin != null ? "where abe40codigo between :tabIni and :tabFin " :""));
		if(tabIni != null && tabFin != null){
			sqlTabelasPreco.setParameter("tabIni",tabIni);
			sqlTabelasPreco.setParameter("tabFin",tabFin);
		}

		return sqlTabelasPreco.getListTableMap();
	}
	private List<TableMap> buscarItensTabelaPreco(Long idTabela, String prodIni, String prodFin){
		Query sqlItens = getSession().createQuery("select abm01codigo,abm01na, abe4001preco, abe30codigo, abe4001txdesc,abe4001qtmax " +
										  "FROM abe4001 " +
										  "INNER JOIN abm01 ON abm01id = abe4001item "+
										  "INNER JOIN abe30 on abe30id = abe4001cp "+
										  "AND abe4001tab = :idTabela "+
										  (prodIni != null && prodFin != null ? "AND abm01codigo BETWEEN :prodIni and :prodFin " : "")+
										  "order by abm01codigo");
		if(idTabela != null){
			sqlItens.setParameter("idTabela",idTabela);
		}

		if(prodIni != null && prodFin != null ){
			sqlItens.setParameter("prodIni",prodIni);
			sqlItens.setParameter("prodFin",prodFin);
		}

		List<TableMap> itens = sqlItens.getListTableMap()

		return itens;
		
	}
	private List<TableMap> buscarEntidades(Long idTabela, String entidadeIni, String entidadeFin, Long idEntidade){
		Query sqlEntidade = getSession().createQuery("select abe01codigo, abe01na from abe01 "+
											"inner join abe02 on abe02ent = abe01id "+
											"where (abe02rep0 = :idEntidade or abe02rep1 = :idEntidade or abe02rep2 = :idEntidade or abe02rep3 = :idEntidade or abe02rep4 = :idEntidade) "+
											(entidadeIni != null && entidadeFin != null ? "and abe01codigo between :entidadeIni and :entidadeFin " : "")+
											"and abe02tp = :idTabela");
	 	if(entidadeIni != null && entidadeFin != null ){
	 		sqlEntidade.setParameter("entidadeIni",entidadeIni);
	 		sqlEntidade.setParameter("entidadeFin",entidadeFin);
	 	}
	 	if(idEntidade != null){
	 		sqlEntidade.setParameter("idEntidade",idEntidade);
	 	}
	 	if(idTabela != null){
	 		sqlEntidade.setParameter("idTabela",idTabela);
	 	}

	 	return sqlEntidade.getListTableMap();

	}

	private	Long buscarEntidadeDoRepresentante(Long idUser){
		Long idEntidade = getSession().createCriteria(Abe01.class)
		                  .addFields("abe01id")
		                  .addJoin(Joins.join("Abe05","abe05ent = abe01id"))
				          .addJoin(Joins.join("Aab10","aab10id = abe05user"))
				          .addWhere(Criterions.eq("aab10id",idUser))
				          .get(ColumnType.LONG);
		return idEntidade;


	}
}
//meta-sis-eyJkZXNjciI6IlRhYmVsYXMgZGUgUHJlw6dvIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNJVEUgLSBUYWJlbGFzIGRlIFByZcOnbyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==