package Atilatte.cubos.scf;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate;
import sam.server.samdev.relatorio.TableMapDataSource;
import sam.server.samdev.utils.Parametro;
import java.time.LocalDate;

public class CUBO_SCF_lancamentosPorNatureza extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCF - Lançamentos por Natureza"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		
		List<TableMap> dados = buscarDocumentos();

		return gerarXLSX("SCF - Lançamentos por Natureza",dados)
		
	}

	private List<TableMap> buscarDocumentos(){
		
		List<Long> naturezas = getListLong("natureza");
		List<Long> contas = getListLong("contaCorrente");
		List<Long> lancamento = getListLong("lancamento");
		LocalDate[] data = getIntervaloDatas("datas");
		
		LocalDate dtIni = null;
		LocalDate dtFin = null;
		
		if(data != null){
			dtIni = data[0];
			dtFin = data[1];
		}

		Query sql = getSession().createQuery(	"select sum(dab10011valor) as valorRec,abf10codigo,abf10nome,dab01codigo "+
										"from dab10011 "+
										"inner join abf10 on abf10id = dab10011nat "+
										"inner join dab1001 on dab1001id = dab10011depto "+
										"inner join dab10 on dab10id = dab1001lct "+
										"left join abf20 on abf20id = dab10plf "+
										"left join dab01 on dab01id = dab10cc "+
										"where dab10eg = 1322578 "+
										(dtIni != null && dtFin != null ? "and dab10data between :dtIni and :dtFin " : "")+
										(naturezas != null ? "and abf10id in (:naturezas) " : "")+
										(contas != null ? "and dab01id in (:contas) " : "")+
										(lancamento != null && lancamento.size() > 0 ? "and abf20id in (:lancamento) " : "")+
										"group by dab01codigo,abf10codigo,abf10nome "+
										"order by dab01codigo,abf10codigo ");
										
		if(dtIni != null && dtFin != null ){
			sql.setParameter("dtIni",dtIni);
			sql.setParameter("dtFin",dtFin);
		}
		
		if(naturezas != null){
			sql.setParameter("naturezas",naturezas);
		}

		if(contas != null){
			sql.setParameter("contas",contas);
		}

		if(lancamento != null){
			sql.setParameter("lancamento",lancamento);
		}		

		List<TableMap> tmDocumentos = sql.getListTableMap();
		List<TableMap> tmDados = new ArrayList();

		for(TableMap documento : tmDocumentos){
			BigDecimal vlrRec = documento.getBigDecimal("valorRec");
			String ctaCorrente = documento.getString("dab01codigo");
			String natureza = documento.getString("abf10codigo");

			documento.put("valor",vlrRec);

			List<TableMap> docPagos = buscarDocumentosPagos(vlrRec,dtIni,dtFin,ctaCorrente,natureza);
			for(TableMap docPago : docPagos ){
				documento.put("valor",docPago.getBigDecimal("valor"))
			}
			tmDados.add(documento);
		}

		return tmDados;
		
	}

	private List<TableMap> buscarDocumentosPagos(BigDecimal vlrRec, LocalDate dtIni, LocalDate dtFin, String ctaCorrente, String natureza){
		Query sqlPagos = getSession().createQuery(	"select sum(dab10011valor) as valorPg,abf10codigo,dab01codigo "+
											"from dab10011 "+
											"inner join abf10 on abf10id = dab10011nat "+
											"inner join dab1001 on dab1001id = dab10011depto "+
											"inner join dab10 on dab10id = dab1001lct "+
											"left join abf20 on abf20id = dab10plf "+
											"left join dab01 on dab01id = dab10cc "+
											"where dab10eg = 1322578 "+
											(dtIni != null && dtFin != null ? "and dab10data between :dtIni and :dtFin " : "")+
											(naturezas != null ? "and abf10codigo = :natureza " : "")+
											(contas != null ? "and dab0codigo = :ctaCorrente " : "")+
											(lancamento != null && lancamento.size() > 0 ? "and abf20id in (:lancamento) " : "")+
											"and dab10mov = 1 "+
											"group by dab01codigo,abf10codigo,abf10nome "+
											"order by dab01codigo,abf10codigo ");
			if(dtIni != null && dtFin != null ){
				sqlPagos.setParameter("dtIni",dtIni);
				sqlPagos.setParameter("dtFin",dtFin);
			}
			
			if(natureza != null){
				sqlPagos.setParameter("natureza",natureza);
			}
	
			if(ctaCorrente != null){
				sqlPagos.setParameter("ctaCorrente",ctaCorrente);
			}
	
			if(lancamento != null){
				sqlPagos.setParameter("lancamento",lancamento);
			}

			List<TableMap> tmDocsPagos = sqlPagos.getListTableMap();
			List<TableMap> tmPagos = new ArrayList();
			

			for(TableMap tmDocPag : tmDocsPagos ){
				BigDecimal vlrPag = tmDocPag.getBigDecimal("valorPg");
				def valor;

				if(vlrRec < 0){
					vlrRec = 0;
					valor = vlrRec - vlrPag;
					tmDocPag.put("valor",valor);
					
				}else{
					valor = vlrRec - (vlrPag * 2)
					tmDocPag.put("valor",valor);
				}
				tmPagos.add(tmDocPag);
			}
			return tmPagos;

			
		}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ0YgLSBMYW7Dp2FtZW50b3MgcG9yIE5hdHVyZXphIiwidGlwbyI6InJlbGF0b3JpbyJ9