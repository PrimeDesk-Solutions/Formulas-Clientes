package Atilatte.relatorios.site

import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ab.Abe01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat
import br.com.multiorm.Query
import br.com.multiorm.criteria.join.Joins;
import br.com.multiorm.ColumnType




public class SITE_ClientesXdiasSemVendas extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return 'SITE - Clientes "X" Dias Sem Vendas' ;
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {

//        //Recupera o usuário logado
        def user = obterUsuarioLogado();
//
//        //Recupera o ID do usuário logado
        Long idUser = user.aab10id;
//
        //Busca o id do registro em entidades do usuário logado
        Long idEnt = buscarEntidadeDoRepresentante(idUser);
//
//        //Busca as entidades ao qual o usuario logado é representante
//        List<Long> entidades = getSession().createCriteria(Abe01.class)
//                                        .addFields("abe01id")
//                                        .addJoin(Joins.join("abe02","abe02ent = abe01id"))
//                                        .addWhere(Criterions.where("(abe02rep0 = "+idEnt+" or abe02rep1 = "+idEnt+" or abe02rep2 = "+idEnt+" or abe02rep3 = "+idEnt+" or abe02rep4 = "+idEnt+" )"))
//                                        .getList(ColumnType.LONG);
//
//        List<Long> listIds = buscarRepresentantes(idUser);
//
        Map<String, Object> filtrosDefault = new HashMap<>();
//
//        filtrosDefault.put("idRepresentantes",listIds);
//        
//        if(entidades.size() > 0){
//			filtrosDefault.put("idEntidades",entidades);
//	   }else{
//			filtrosDefault.put("idEntidades",idEnt);
//	   }
	   
	    filtrosDefault.put("idRepresentantes",idEnt);
        return filtrosDefault;

    }
	@Override
	public DadosParaDownload executar() {
		List<TableMap> dados = buscarDocumentos();

        return gerarPDF("SITE_ClientesXdiasSemVendas", dados)
	}

	private List<TableMap> buscarDocumentos(){
		Long idUser = obterUsuarioLogado().aab10id;
		String repIni = getString("representanteIni");
		String repFin = getString("representanteFin");
		String entIni = getString("entidadeIni");
		String entFin = getString("entidadeFin");
		Integer diasSemVenda = getInteger("dias");

		Long idEnt = buscarEntidadeDoRepresentante(idUser);
		
		//List<Long> idsReps = buscarRepresentantes(idUser);
		
		Query sqlReps = getSession().createQuery("select abe01id,abe01na, abe01codigo from abe01 "+ 
										"where abe01rep = 1 "+
										(repIni != null && repFin!= null ? "and abe01codigo between :repIni and :repFin " : "and abe01id in (:idsReps)" ));

		if(repIni != null && repFin!= null){
			sqlReps.setParameter("repIni",repIni);
			sqlReps.setParameter("repFin",repFin);
		}else{
//			sqlReps.setParameter("idsReps",idsReps)
			sqlReps.setParameter("idsReps",idEnt)
		}

	   List<TableMap> tmReps =  sqlReps.getListTableMap();
        List<TableMap> registros = new ArrayList<>();
		for(rep in tmReps){
            Long idRep = rep.getLong("abe01id");
            List<TableMap> tmEntidades = buscarDadosEntidade(idRep,diasSemVenda,entIni,entFin);

            for(TableMap entidade : tmEntidades){
               String idRep0 = entidade.getString("idRep0");
			String idRep1 = entidade.getString("idRep1");
			String idRep2 = entidade.getString("idRep2");
			String idRep3 = entidade.getString("idRep3");
			String idRep4 = entidade.getString("idRep4");

                String codRepPrinc = "";
                String naRepPrinc = "";


                if(idRep4 == idRep){
                    codRepPrinc = entidade.getString("codRep4");
                    naRepPrinc = entidade.getString("naRep4");
                }else if (codRep3 == idRep){
                     codRepPrinc = entidade.getString("codRep3");
                    naRepPrinc = entidade.getString("naRep3");
                }else if (codRep2 == idRep){
                    codRepPrinc = entidade.getString("codRep2");
                    naRepPrinc = entidade.getString("naRep2");
                }else if (codRep1 == idRep){
                   codRepPrinc = entidade.getString("codRep1");
                    naRepPrinc = entidade.getString("naRep1");
                }else{
                    codRepPrinc = entidade.getString("codRep0");
                    naRepPrinc = entidade.getString("naRep0");
                }
                entidade.put("codRepPrinc",codRepPrinc);
                entidade.put("naRepPrinc",naRepPrinc);

                registros.add(entidade);
            }
		}

        return registros;


	}

	private List<TableMap> buscarDadosEntidade(Long idRep, Integer dias, String entIni, String entFin){
		Query sqlentidade = getSession().createQuery("select ent.abe01na as naEntidade,ent.abe01nome as razaoSocial,abe0101bairro as bairro, aag0201nome as cidade, abe0101fone1,cast(abe02json ->> 'ultima_venda' as date) as ultimaVenda, " +
														"UPPER(cast(ent.abe01camposcustom ->> 'status' as character varying(40))) as status, " +
														"rep0.abe01codigo as codRep0, rep0.abe01na as naRep0, rep0.abe01id as idRep0, " +
														"rep1.abe01codigo as codRep1, rep1.abe01na as naRep1, rep1.abe01id as idRep1, " +
														"rep2.abe01codigo as codRep2, rep2.abe01na as naRep2, rep2.abe01id as idRep2, " +
														"rep3.abe01codigo as codRep3, rep3.abe01na as naRep3, rep3.abe01id as idRep3, " +
														"rep4.abe01codigo as codRep4, rep4.abe01na as naRep4, rep4.abe01id as idRep4, " +
														"(current_date - cast(abe02json ->> 'ultima_venda' as date)) as dias " +
														"from abe01 as ent " +
														"inner join abe02 on abe02ent = ent.abe01id " +
														"inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 " +
														"inner join aag0201 on abe0101municipio = aag0201id " +
														"left join abe01 as rep0 on rep0.abe01id = abe02rep0 " +
														"left join abe01 as rep1 on rep1.abe01id = abe02rep1 " +
														"left join abe01 as rep2 on rep2.abe01id = abe02rep2 " +
														"left join abe01 as rep3 on rep3.abe01id = abe02rep3 " +
														"left join abe01 as rep4 on rep4.abe01id = abe02rep4 " +
														"WHERE ent.abe01di is null " + 
			                                                        (idRep != null ? "and (abe02rep0 = :idRep or abe02rep1 = :idRep or abe02rep2 = :idRep or abe02rep3 = :idRep or abe02rep4 = :idRep) " : "") +
			                                                        (dias != null ? "and (current_date - cast(abe02json ->> 'ultima_venda' as date)) >= :dias " : "") +
			                                                        (entIni != null ? "and ent.abe01codigo between :entIni and :entFin " : ""));
        if(idRep != null){
            sqlentidade.setParameter("idRep",idRep);
        }

        if(dias != null){
            sqlentidade.setParameter("dias",dias)
        }

        if(entIni != null && entFin != null){
            sqlentidade.setParameter("entIni",entIni);
            sqlentidade.setParameter("entFin",entFin);
        }

        return sqlentidade.getListTableMap()
	}

	private List<Long> buscarRepresentantes(Long idUser){
		//Recupera a função do usuário que está logado
		String funcaoUser = obterUsuarioLogado().aab10funcoes;

		String whereEntidade = "";

		//Verifica a função do usuário e seleciona os representantes de acordo com a função
		if(funcaoUser != null){
			if(funcaoUser.contains("GERENTE")){
				whereEntidade = "WHERE abe01rep = 1";
			}else if(funcaoUser.contains("03 - SUPERVISOR")){
				whereEntidade = "where aab10funcoes like '%03%'";
			}else if(funcaoUser.contains("04 - SUPERVISOR")){
				whereEntidade = "where aab10funcoes like '%04%'";
			}else if(funcaoUser.contains("05 - GERAL")){
				whereEntidade = "where aab10funcoes like '%05%'";
			}else{
				whereEntidade = "where abe05user = :user"
			}
		}

		
		Query sql = getSession().createQuery("SELECT abe01id FROM abe01 "+
				"inner join abe05 on abe05ent = abe01id "+
				"inner join aab10 on aab10id = abe05user "+
				whereEntidade);

		sql.setParameter("user",idUser);

		return sql.getList(ColumnType.LONG);
	}
    private Long buscarEntidadeDoRepresentante(Long idUser){
    	
        Long idEntidade = getSession().createCriteria(Abe01.class)
                .addFields("abe01id")
                .addJoin(Joins.join("Abe05","abe05ent = abe01id"))
                .addJoin(Joins.join("Aab10","aab10id = abe05user"))
                .addWhere(Criterions.eq("aab10id",idUser))
                .get(ColumnType.LONG);
        
        return idEntidade;
    }
}
//meta-sis-eyJkZXNjciI6IlNJVEUgLSBDbGllbnRlcyBcIlhcIiBEaWFzIFNlbSBWZW5kYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=