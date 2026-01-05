package Fast.relatorios.scv;

import sam.model.entities.ab.Abe01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDate
import br.com.multitec.utils.ValidacaoException
import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;

public class SCV_DiasSemVendas extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SCV - Dias Sem Vendas";
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("impressao","0");
		filtrosDefault.put("dias","1");
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		List<Long> entidade = getListLong("ent");
		List<Long> rep = getListLong("rep");
		Integer numDias = getInteger("dias");
		Integer diasCorridos = getInteger("diasCorridos");
		Integer impressao = getInteger("impressao");

		List<TableMap> dadosRelatorio = buscarDocumentos(entidade,rep,numDias,diasCorridos);

		if(impressao == 1) return gerarXLSX("UltimasVendas_Excel", dadosRelatorio);

		return gerarPDF("UltimasVendas_PDF", dadosRelatorio);
	}

	private buscarDocumentos(List<Long> entidade,List<Long> rep, Integer numDias, Integer diasCorridos){
		
		Query reps = getSession().createQuery("select distinct abe01id, abe01na "+
												"from abe01 "+
												getSamWhere().getWherePadrao(" WHERE ", Abe01.class) +
												"and abe01rep = 1 " +
												(rep != null && rep.size() > 0 ? "and abe01id in (:rep) " : ""));
		if(rep != null && rep.size() > 0){
			reps.setParameter("rep",rep)
		}
		
		List<TableMap> listRep = reps.getListTableMap();
		List<TableMap> dados = new ArrayList();
		
		for(TableMap representante : listRep){
			Long idRep = representante.getLong("abe01id");
			String nomeRep = representante.getString("abe01na");

			String whereDias = diasCorridos != 0 && numDias != 0 ? "and current_date - CAST(abe02json ->>'ultima_venda' AS date) >= :numDias and current_date - CAST(abe02json ->>'primeira_venda' AS date) >= :diasCorridos " : numDias == 0 ? "and current_date - CAST(abe02json ->>'primeira_venda' AS date) >= :diasCorridos " : "and current_date - CAST(abe02json ->>'ultima_venda' AS date) >= :numDias ";
			
			Query sql = getSession().createQuery("select distinct ent.abe01nome,abe0101bairro,abe0101ddd1,abe0101fone1,aag0201nome,ent.abe01codigo, "+
													"ent.abe01na,rep0.abe01na as rep0, rep1.abe01na as rep1 ,rep2.abe01na as rep2, rep3.abe01na as rep3,rep4.abe01na as rep4,CAST(ent.abe01camposcustom ->>'status' AS character varying(20)) as status,"+
													"(CAST(abe02json ->>'ultima_venda' AS date)) as ultimaVenda, (current_date - (CAST(abe02json ->>'ultima_venda' AS date))) as diasSemVenda, "+
													"rep0.abe01id as idRep0, rep1.abe01id as idRep1, rep2.abe01id as idRep2, rep4.abe01id as idRep3, rep4.abe01id as idRep4, "+
													"rep0.abe01codigo as codRep0, rep1.abe01codigo as codRep1, rep2.abe01codigo as codRep2, rep4.abe01codigo as codRep3, rep4.abe01codigo as codRep4, CAST(abe02json ->>'primeira_venda' AS date) as primeiraVenda, (current_date - CAST(abe02json ->>'primeira_venda' AS date)) as diasPrimeiraVenda "+
													"from abe01 ent "+
													"inner join abe02  on abe02ent = ent.abe01id "+
													"left join abe01 rep0 on rep0.abe01id = abe02rep0 "+
													"left join abe01 rep1 on rep1.abe01id = abe02rep1 "+
													"left join abe01 rep2 on rep2.abe01id = abe02rep2 "+
													"left join abe01 rep3 on rep3.abe01id = abe02rep3 "+
													"left join abe01 rep4 on rep4.abe01id = abe02rep4 "+
													"left join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 "+
													"left join aag0201 on abe0101municipio = aag0201id "+
													"where (abe02rep0 = :idRep or abe02rep1 =  :idRep or abe02rep2 =  :idRep or abe02rep3 =  :idRep or abe02rep4 = :idRep ) "+
													(entidade != null && entidade.size() > 0 ? "and ent.abe01id in (:entidade) " : "" )+
													whereDias +
													"order by current_date - CAST(abe02json ->>'ultima_venda' as date)");
													

			if(entidade != null && entidade.size() > 0){
				sql.setParameter("entidade",entidade)								
			}

			sql.setParameter("idRep",idRep);
			sql.setParameter("numDias",numDias);
			sql.setParameter("diasCorridos",diasCorridos)

			
			List<TableMap>registroSql = sql.getListTableMap();

			for(TableMap registro : registroSql){
				buscarRepresentantesPrincipal(registro,idRep);
				dados.add(registro);
			}
			
		}
		return dados;
		
		
	}

	private void buscarRepresentantesPrincipal(TableMap registro, Long idRep){
		Long rep0 = registro.getLong("idRep0");
		Long rep1 = registro.getLong("idRep1");
		Long rep2 = registro.getLong("idRep2");
		Long rep3 = registro.getLong("idRep3");
		Long rep4 = registro.getLong("idRep4");

		if(rep4 == idRep){
			registro.put("rep",rep4 )
			registro.put("codRep",registro.getString("codRep4"))
			registro.put("naRep",registro.getString("rep4"))
		}else if(rep3 == idRep){
			registro.put("rep",rep3 );
			registro.put("codRep",registro.getString("codRep3"))
			registro.put("naRep",registro.getString("rep3"))
		}else if(rep2 == idRep){
			registro.put("rep",rep2 );
			registro.put("codRep",registro.getString("codRep2"))
			registro.put("naRep",registro.getString("rep2"))
		}else if(rep1 == idRep){
			registro.put("rep",rep1 );
			registro.put("codRep",registro.getString("codRep1"))
			registro.put("naRep",registro.getString("rep1"))
		}else{
			registro.put("rep",rep0 );
			registro.put("codRep",registro.getString("codRep0"))
			registro.put("naRep",registro.getString("rep0"))
		}
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBVbHRpbWFzIFZlbmRhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNDViAtIERpYXMgU2VtIFZlbmRhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==