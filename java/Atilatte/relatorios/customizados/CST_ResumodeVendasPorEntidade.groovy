package Atilatte.relatorios.customizados;
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

public class ResumodeVendasPorEntidade extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Resumo de Vendas por Entidade"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("numeroInicial",000000001,
						"numeroFinal", 99999999,
//						"dataSaidaIni", LocalDate.now(),
//						"dataSaidaFin", LocalDate.now(),
//						"dataEmissaoIni", LocalDate.now(),
//						"dataEmissaoFin", LocalDate.now(),
						"operacao", 0,
						"tipoOperacao",0
						);
	}
	@Override 
	public DadosParaDownload executar() {
		Integer operacao = getInteger("operacao");
		Integer tipoOperacao = getInteger("tipoOperacao")
		Long idItemInicial = getLong("itemIni");
		Long idItemFinal = getLong("itemFin");
		Long idEntidadeIni = getLong("entIni");
		Long idEntidadeFin = getLong("entFin");
		Long idMunicipioIni = getLong("municipioIni");
		Long idMunicipioFin = getLong("municipioFin");
		LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");

		List<TableMap> dadosRelatorio = buscarDocumentos(operacao,tipoOperacao,dtEmissao,idItemInicial,idItemFinal,idEntidadeIni,idEntidadeFin,idMunicipioIni,idMunicipioFin)

		return gerarPDF("CST_resumodevendasporentidade", dadosRelatorio);
		
	}

	private buscarDocumentos(Integer operacao, Integer tipoOperacao, LocalDate[] dtEmissao, Long idItemInicial, Long idItemFinal,Long idEntidadeIni,Long idEntidadeFin,Long idMunicipioIni,Long idMunicipioFin ){
		String operDocumento = ""
		String tipoOper = ""

		LocalDate dtEmissIni = null;
		LocalDate dtEmissFin = null;
		if(dtEmissao != null){
			dtEmissIni = dtEmissao[0];
			dtEmissFin = dtEmissao[1];
		}
		
		
		if(operacao == 0){
			operDocumento = "AND abd01aplic = 1 and abd01es = 0 "
		}else if(operacao == 1){
			operDocumento = "AND abd01aplic = 1 and abd01es = 1 "
		}else if(operacao == 2 ){
			operDocumento = "AND abd01aplic = 0 and abd01es = 0 "
		}else{
			operDocumento = "AND abd01aplic = 0 and abd01es = 1 "
		}
		

		if(tipoOperacao == 99){
			tipoOper = "AND abb10tipocod in (0,1,2,3,4,5,6,7) "
		}else{
			tipoOper = "AND abb10tipocod = :tipoOperacao "
		}

		Query sql = getSession().createQuery("select distinct abe01na as aa80na, abe0101endereco as aa80endereco,abe0101bairro as aa80bairro, abe0101numero as aa80numero,abe0101fone1  as aa80fone, aag0201nome as aa601nome,abm01na as aa50descr "+
										"from eaa01 "+
										"inner join abd01 on abd01id = eaa01pcd "+
										"inner join abb10 on abb10id = abd01opercod "+
										"inner join abb01 on abb01id = eaa01central "+
										"inner join abe01 on abe01id = abb01ent "+
										"inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
										"inner join aag0201 on aag0201id = abe0101municipio "+
										"inner join eaa0103 on eaa0103doc = eaa01id "+
										"inner join abm01 on abm01id = eaa0103item "+
										"inner join eaa0102 on eaa0102doc = eaa01id "+
										(dtEmissIni != null && dtEmissFin != null ? "where abb01data between :dtEmissIni and :dtEmissFin " : "")+
										(idEntidadeIni != null && idEntidadeFin != null ? "and abe01id between :idEntidadeIni and :idEntidadeFin " : "")+										
										"and abm01tipo = '1' "+
										(idItemInicial != null && idItemFinal != null ? "and abm01id between :idItemInicial and :idItemFinal " : "")+
										(dtEmissIni != null && dtEmissFin != null ? "and aag0201id between :idMunicipioIni and :idMunicipioFin " : "")+
										operDocumento +
										tipoOper);
		if(dtEmissIni != null && dtEmissFin != null){
			sql.setParameter("dtEmissIni",dtEmissIni);
			sql.setParameter("dtEmissFin",dtEmissFin);
		}

		if(idEntidadeIni != null && idEntidadeFin != null ){
			sql.setParameter("idEntidadeIni",idEntidadeIni);
			sql.setParameter("idEntidadeFin",idEntidadeFin);
		}

		if(idItemInicial != null && idItemFinal != null ){
			sql.setParameter("idItemInicial",idItemInicial);
			sql.setParameter("idItemFinal",idItemFinal);
		}

		if(idMunicipioIni != null && idMunicipioFin != null ){
			sql.setParameter("idMunicipioIni",idMunicipioIni);
			sql.setParameter("idMunicipioFin",idMunicipioFin);
		}

		if(tipoOper != null){
			sql.setParameter("tipoOperacao",tipoOperacao);
		}

		return sql.getListTableMap();

		
		
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJlc3VtbyBkZSBWZW5kYXMgcG9yIEVudGlkYWRlIiwidGlwbyI6InJlbGF0b3JpbyJ9