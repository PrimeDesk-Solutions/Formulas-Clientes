package Atilatte.cubos.sac;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import sam.model.entities.ea.Eaa01;


import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

public class CUBO_SAC_ResumoPorProduto extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SAC Resumo por Produto"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("movimentacao", "0");
		filtrosDefault.put("clasDoc", "0");
		filtrosDefault.put("situacaoDoc", "0");

		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		
		List<TableMap> dados = buscarDocumentos();

		 gerarXLSX("sacResumoPorProduto", dados)
		
	}

	private List<TableMap> buscarDocumentos(){
		List<Long> idEntidade = getListLong("entidade");
		Integer sitDoc = getInteger("situacaoDoc");
		Integer movimentacao = getInteger("movimentacao");
		Integer clasDoc = getInteger("clasDoc");
		List<Long> tipoDoc = getListLong("tipoDoc");
		List<Long> pcd = getListLong("pcd");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] entradaSaida = getIntervaloDatas("dataEntradaSaida");
		LocalDate[] dataEntrega = getIntervaloDatas("dataEntrega");

	
		//Data Emissao - Inicial - Final
		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;
		
		if(dataEmissao != null){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1];
		}

		//Data Entrada Saida - Inicial - Final
		LocalDate entradaSaidaIni = null;
		LocalDate entradaSaidaFin = null;
		if(entradaSaida != null){
			entradaSaidaIni = entradaSaida[0];
			entradaSaidaFin = entradaSaida[1];
		}

		//Data Entrega - Inicial - Final
		LocalDate dataEntregaIni = null;
		LocalDate dataEntregaFin = null;
		
		if(dataEntrega != null){
			dataEntregaIni = dataEntrega[0];
			dataEntregaFin = dataEntrega[1];
		}

		String pedAtend = "";


		if(clasDoc == 0){
			if(sitDoc == 0){
				pedAtend = "and eaa01scvatend = '0' "
			}else if(sitDoc == 1){
				pedAtend = "and eaa01scvatend = '1' "
			}else{
				pedAtend = "and eaa01scvatend = '2' "
			}
		}

		Query sql = getSession().createQuery("select SUM(eaa0103qtUso) as qtd, SUM(cast(eaa0103json ->> 'peso_liquido' as numeric(18,2))) as pesoLiquido, "+
												"abm01codigo as codItem, abm01na as itemDescr, to_char(abb01data,'MM/YYYY') as periodo "+
												"from eaa01 "+
												"inner join eaa0103 on eaa0103doc = eaa01id "+
												"inner join abd01 on abd01id = eaa01pcd "+
												"inner join abb01 on abb01id = eaa01central "+
												"inner join abe01 on abe01id = abb01ent "+
												"inner join abm01 on abm01id = eaa0103item "+
												"inner join aah01 on aah01id = abb01tipo "+
												getSamWhere().getWherePadrao("WHERE", Eaa01.class) +
												(dataEmissaoIni != null && dataEmissaoFin != null ? "and abb01data between :dataEmissaoIni and :dataEmissaoFin " : "" ) +
												(entradaSaidaIni != null && entradaSaidaIni != null ? "and eaa01esdata between :entradaSaidaIni and :entradaSaidaFin " : "") +
												(idEntidade != null ? "and abe01id in (:idEntidade) " : "")+
												(dataEntregaIni != null && dataEntregaFin != null ? "and eaa0103dtentrega between :dataEntregaIni and :dataEntregaFin " : "")+
												(tipoDoc != null ? "and aah01id in (:tipoDoc) " : "" ) +
												(pcd != null ? "and abd01id in (:pcd) " : "" ) +
												"and eaa01esmov = :movimentacao "+
												"and eaa01clasdoc = :clasDoc "+
												pedAtend +
												"and eaa01cancdata is null "+
												"group by abm01codigo, abm01na, periodo  "+
												"order by abm01codigo");

		if(dataEmissaoIni != null && dataEmissaoFin != null ){
			sql.setParameter("dataEmissaoIni",dataEmissaoIni);
			sql.setParameter("dataEmissaoFin",dataEmissaoFin);
		}
		if(entradaSaidaIni != null && entradaSaidaFin != null ){
			sql.setParameter("entradaSaidaIni",entradaSaidaIni);
			sql.setParameter("entradaSaidaFin",entradaSaidaFin);
		}
		if(idEntidade != null){
			sql.setParameter("idEntidade",idEntidade);
		}
		if(dataEntregaIni != null && dataEntregaFin != null ){
			sql.setParameter("dataEntregaIni",dataEntregaIni);
			sql.setParameter("dataEntregaFin",dataEntregaFin);
		}
		if(tipoDoc != null){
			sql.setParameter("tipoDoc",tipoDoc);
		}
		if(pcd != null){
			sql.setParameter("pcd",pcd);
		}

		if(sitDoc != null){
			sql.setParameter("sitDoc",sitDoc);
		}

		sql.setParameter("movimentacao", movimentacao);
		sql.setParameter("clasDoc", clasDoc);

		List<TableMap> registros = sql.getListTableMap();

		return registros;
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQUMgUmVzdW1vIHBvciBQcm9kdXRvIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQUMgUmVzdW1vIHBvciBQcm9kdXRvIiwidGlwbyI6InJlbGF0b3JpbyJ9