package Atilatte.relatorios.site

import br.com.multiorm.criteria.join.Joins;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import br.com.multitec.utils.collections.TableMap;
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import br.com.multiorm.Query
import sam.model.entities.ab.Abe01
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ea.Eaa01

public class SITE_itensVendidosPorRepresentante extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SITE - Itens Por Representante"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		//Recupera o usuário logado
		def user = obterUsuarioLogado();

		//Recupera o ID do usuário logado
		Long idUser = user.aab10id;

		//Recupera a função do usuário que está logado
		String funcaoUser = user.aab10funcoes;

		//Busca o id do registro em entidades do usuário logado
		Abe01 entRep = buscarEntidadeDoRepresentante(idUser);

		Long idEnt = entRep.abe01id;

		//Busca as entidades ao qual o usuario logado é representante
		List<Long> entidades = getSession().createCriteria(Abe01.class)
				.addFields("abe01id")
				.addJoin(Joins.join("abe02","abe02ent = abe01id"))
				.addWhere(Criterions.where("(abe02rep0 = "+idEnt+" or abe02rep1 = "+idEnt+" or abe02rep2 = "+idEnt+" or abe02rep3 = "+idEnt+" or abe02rep4 = "+idEnt+" )"))
				.getList(ColumnType.LONG);

		//List<Long> listIds = buscarRepresentantes(idUser);
		

		Map<String, Object> filtrosDefault = new HashMap<>();
		
		
		if(entRep.abe01codigo == "0405100000" || entRep.abe01codigo == "0400000000" || entRep.abe01codigo == "0400000001"){
			List<Long> listIdsReps = buscarRepresentantes(idUser);
			filtrosDefault.put("idRepresentantes",listIdsReps);
		}else{
			filtrosDefault.put("idRepresentantes",idEnt);
		}
		
		if(entidades.size() > 0){
			filtrosDefault.put("idEntidades",entidades);
		}else{
			filtrosDefault.put("idEntidades",idEnt);
		}


		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		Long idUser = obterUsuarioLogado().aab10id;
		String repIni = getString("representanteIni");
		String repFin = getString("representanteFin");

		//Busca o id do registro em entidades do usuário logado
		Abe01 entRep = buscarEntidadeDoRepresentante(idUser);
		
		List<Long> ids = entRep.abe01codigo = "0405100000" || entRep.abe01codigo == "0400000000" ? buscarRepresentantes(idUser) : ids.add(idUser);


		List<TableMap> dados = buscarRepresentantesParaDocumentos(repIni,repFin,ids);


		return gerarPDF("SITE_itensVendidosPorRepresentante",dados);
	}

	private List<TableMap> buscarRepresentantesParaDocumentos(String repIni, String repFin,List<Long> ids){
		
		Query rep = getSession().createQuery("select abe01id,abe01na, abe01codigo from abe01 "+ 
										"where abe01rep = 1 "+
										(repIni != null && repFin!= null ? "and abe01codigo between :repIni and :repFin " : "and abe01id in (:idsReps)" ));

		if(repIni != null && repFin!= null){
			rep.setParameter("repIni",repIni);
			rep.setParameter("repFin",repFin);
		}else{
			rep.setParameter("ids",ids)
		}

		List<TableMap> tmRepresentantes = rep.getListTableMap();
		
		List<TableMap> tmDocumentos = new ArrayList();
		
		for(TableMap representante : tmRepresentantes){
			Long idRep = representante.getLong("abe01id");
			tmDocumentos.addAll(buscarDocumentosPorRepresentantes(idRep))
		}

		return tmDocumentos;

	}

	private List<TableMap> buscarDocumentosPorRepresentantes(Long idRep){

		String entIni = getString("entidadeIni");
		String entFin = getString("entidadeFin");
		LocalDate dataEmissaoIni = getLocalDate("dataEmissaoIni");
		LocalDate dataEmissaoFin = getLocalDate("dataEmissaoFin");
		String itemIni = getString("itensIni");
		String itemFin = getString("itensFin");
		Integer resumoOperacao = getInteger("resumoOperacao");
		Integer classeDoc = getInteger("clasDoc");
		List<Long> idsPcd = getListLong("pcd");


		String whereResumoOperacao = null;	
		if (resumoOperacao.equals(0)) {
			whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_ENTRADA;	
		} else {
			whereResumoOperacao = " and eaa01esMov = " + Eaa01.ESMOV_SAIDA;
		}

		String whereClasDoc = null;
		if(classeDoc.equals(0)){
			whereClasDoc = "and eaa01clasDoc = 0";
		}else{
			whereClasDoc = "and eaa01clasDoc = 1";
		}
		
		Query sql = getSession().createQuery("select aah01codigo as tipoDoc, abb01num as numDoc, case when abm01tipo = 0 then 'M' when abm01tipo = 1 then 'P' else 'S' end as MPS, abm01codigo as codItem,  " +
										"abm01na as naItem, cast(eaa0103json ->> 'umv' as character varying(3)) as umv, cast(eaa0103json ->> 'caixa' as numeric(18,6)) as caixa, "+ 
										"cast(eaa0103json ->> 'peso_bruto' as numeric(18,6)) as pesoBruto, eaa0103qtUso as SCE, eaa0103total as totItem, eaa0103totDoc as totDoc,abe30nome, "+
										"ent.abe01codigo as codEntidade, ent.abe01na as naEntidade, "+  
										"rep0.abe01codigo as codRep0, rep0.abe01na as naRep0,rep0.abe01id as idRep0, "+
										"rep1.abe01codigo as codRep1, rep1.abe01na as naRep1,rep1.abe01id as idRep1, "+
										"rep2.abe01codigo as codRep2, rep2.abe01na as naRep2,rep2.abe01id as idRep2, "+
										"rep3.abe01codigo as codRep3, rep3.abe01na as naRep3,rep3.abe01id as idRep3, "+
										"rep4.abe01codigo as codRep4, rep4.abe01na as naRep4,rep4.abe01id as idRep4,  "+
										"abb10descr " +
										"from eaa01 "+
										"inner join abb01 on abb01id = eaa01central "+ 
										"inner join abe01 ent on ent.abe01id = abb01ent "+
										"inner join aah01 on aah01id = abb01tipo "+
										"inner join eaa0103 on eaa0103doc = eaa01id "+
										"inner join abm01 on abm01id = eaa0103item "+
										"inner join abd01 on abd01id = eaa01pcd "+
										"inner join abb10 on abb10id = abb01opercod "+ 
										"left join abe01 rep0 on rep0.abe01id = eaa01rep0 "+
										"left join abe01 rep1 on rep1.abe01id = eaa01rep1 "+
										"left join abe01 rep2 on rep2.abe01id = eaa01rep2 "+
										"left join abe01 rep3 on rep3.abe01id = eaa01rep3 "+
										"left join abe01 rep4 on rep4.abe01id = eaa01rep4 "+
										"inner join abe30 on abe30id = eaa01cp "+
										"where true "+
										"and eaa01cancdata is null "+
										whereResumoOperacao +
										whereClasDoc +
										(entIni != null && entFin != null ? "and ent.abe01codigo between :entIni and :entFin " : "") +
										(itemIni != null && itemFin != null ? "and abm01codigo between :itemIni and :itemFin " : "")+
										(dataEmissaoIni != null && dataEmissaoFin != null ? "and abb01data between :dataEmissaoIni and :dataEmissaoFin " : "")+
										(idRep != null ? "and (eaa01rep0 = :idRep or eaa01rep1 = :idRep or eaa01rep2 = :idRep or eaa01rep3 = :idRep or eaa01rep4 = :idRep)" : "")+
										(idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : ""));

		if(idsPcd != null && idsPcd.size() > 0){
			sql.setParameter("idsPcd",idsPcd);
		}
		
		if(entIni != null && entFin != null ){
			sql.setParameter("entIni",entIni);
			sql.setParameter("entFin",entFin);
		}
		if(itemIni != null && itemFin != null){
			sql.setParameter("itemIni",itemIni);
			sql.setParameter("itemFin",itemFin);
		}

		if(dataEmissaoIni != null && dataEmissaoFin != null){
			sql.setParameter("dataEmissaoIni",dataEmissaoIni);
			sql.setParameter("dataEmissaoFin",dataEmissaoFin);
		}

		//if(idRep != null){
			sql.setParameter("idRep",idRep);
		//}

		if(tipoOperacao != null){
			sql.setParameter("tipoOperacao",tipoOperacao);
		}

		List<TableMap> documentos = sql.getListTableMap();
		List<TableMap> dadosRelatorio = new ArrayList();

		for(TableMap documento : documentos ){
	
			String codRepPrincipal = "";
			String naRepPrincipal = "";
			
			if(documento.getLong("idRep4") == idRep ){
				codRepPrincipal = documento.getString("codRep4");
				naRepPrincipal =  documento.getString("naRep4");
				
			}else if(documento.getLong("idRep3") == idRep ){
				codRepPrincipal = documento.getString("codRep3");
				naRepPrincipal =  documento.getString("naRep3");
				
			}else if(documento.getLong("idRep2") == idRep ){
				codRepPrincipal = documento.getString("codRep2");
				naRepPrincipal =  documento.getString("naRep2");
				
			}else if(documento.getLong("idRep1") == idRep ){
				codRepPrincipal = documento.getString("codRep1");
				naRepPrincipal =  documento.getString("naRep1");
				
			}else{
				codRepPrincipal = documento.getString("codRep0");
				naRepPrincipal =  documento.getString("naRep0");
			}

			documento.put("codRepPrincipal",codRepPrincipal);
			documento.put("naRepPrincipal",naRepPrincipal);

			dadosRelatorio.add(documento);
		}

		params.put("titulo","Pedidos Faturados");
		params.put("empresa", getVariaveis().getAac10().getAac10codigo() +"-"+ getVariaveis().getAac10().getAac10na());
		//params.put("periodo","Período: "+txtDataIni +" a " +txtDataFin);

		return dadosRelatorio;
		
	}

	private List<Long> buscarRepresentantes(Long idUser){
	
		Query sql = getSession().createQuery("SELECT abe01id FROM abe01 "+
										"inner join abe05 on abe05ent = abe01id "+
										"inner join aab10 on aab10id = abe05user "+
										"where abe01rep = 1 ");

		return sql.getList(ColumnType.LONG);
	}

	private	Abe01 buscarEntidadeDoRepresentante(Long idUser){
		
		return getSession().createCriteria(Abe01.class)
				//.addFields("abe01id")
				.addJoin(Joins.join("Abe05","abe05ent = abe01id"))
				.addJoin(Joins.join("Aab10","aab10id = abe05user"))
				.addWhere(Criterions.eq("aab10id",idUser)).setMaxResults(1)
				.get(ColumnType.ENTITY);

	}
}
//meta-sis-eyJkZXNjciI6IlNJVEUgLSBJdGVucyBQb3IgUmVwcmVzZW50YW50ZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==