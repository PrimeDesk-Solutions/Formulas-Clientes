package Atilatte.relatorios.site

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import groovy.swing.table.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import br.com.multitec.utils.collections.TableMap;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ab.Abe01;
import sam.core.variaveis.MDate
import sam.server.samdev.utils.Parametro
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat
import br.com.multiorm.Query
import br.com.multiorm.criteria.join.Joins;
import br.com.multiorm.ColumnType


public class SITE_DemonstrativoDeComissoes extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SITE - Demonstrativo de Comissões";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        //Recupera o usuário logado
        def user = obterUsuarioLogado();

        //Recupera o ID do usuário logado
        Long idUser = user.aab10id;

        //if(idUser = 5684083 || idUser = 3505098 )

        //Recupera a função do usuário que está logado
       // String funcaoUser = user.aab10funcoes;
		
        //Busca o id do registro em entidades do usuário logado
        Long idEnt = buscarEntidadeDoRepresentante(idUser);

        //List<Long> listIds = buscarRepresentantes(idUser);

        Map<String, Object> filtrosDefault = new HashMap<>();
       //filtrosDefault.put("idRepresentantes",listIds);

	  filtrosDefault.put("idRepresentantes",idEnt);
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<TableMap> dados = buscarDocumentosComissao();
        gerarPDF("SITE_DemonstrativoDasComissoes",dados);
    }
    private List<TableMap> buscarDocumentosComissao(){
        String codRepIni = getString("representanteIni");
        String codRepFin = getString("representanteFin");
        LocalDate dtCalculo = getLocalDate("dataCalculo");


        Query sql = getSession().createQuery("select aah01codigo as codTipoDoc, aah01nome as descrTipoDoc, nota.abb01num as numDoc, financ.abb01parcela as parcela, financ.abb01quita as quita,nota.abb01data as dtEmissao, ent.abe01codigo as codEntidade, " +
                                                "ent.abe01na as naEntidade,daa01dtvctor as vencimento,calculo.abb01data as dtCalculo,dcb01dtcredito as credito,dcb01bc as bcComissao, dcb01taxa as taxa, dcb01valor as comissao, rep.abe01codigo as codRep, rep.abe01na as naRep, " +
                                                "to_char(dcd01dtcredi,'dd/mm/yyyy') as dtInicio, to_char(dcd01dtcredf,'dd/mm/yyyy') as dtFim\n" +
                                                "from dcb01 " +
                                                "inner join abb01 as nota on dcb01central = nota.abb01id " +
                                                "inner join aah01 on aah01id = abb01tipo " +
                                                "inner join abb0102 on abb0102central = nota.abb01id " +
                                                "inner join abb01 as financ on financ.abb01id = abb0102doc " +
                                                "inner join abe01 as ent on abe01id = nota.abb01ent " +
                                                "inner join daa01 on daa01central = financ.abb01id " +
                                                "inner join dcd01 on dcd01id = dcb01calculo " +
                                                "inner join abb01 as calculo on calculo.abb01id = dcd01central " +
                                                "inner join abe01 as rep on rep.abe01id = dcb01rep "+
                                                (codRepIni != null && codRepFin != null ? "where rep.abe01codigo between :codRepIni and :codRepFin " : "") +
                                                (dtCalculo != null ? "and calculo.abb01data = :dtCalculo " : ""));
        if(codRepIni != null && codRepFin != null){
            sql.setParameter("codRepIni",codRepIni);
            sql.setParameter("codRepFin",codRepFin);
        }
        if(dtCalculo != null ){
            sql.setParameter("dtCalculo",dtCalculo);
        }

        List<TableMap> documentos =  sql.getListTableMap();
        return documentos;
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
//meta-sis-eyJkZXNjciI6IlNJVEUgLSBEZW1vbnN0cmF0aXZvIGRlIENvbWlzc8O1ZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=