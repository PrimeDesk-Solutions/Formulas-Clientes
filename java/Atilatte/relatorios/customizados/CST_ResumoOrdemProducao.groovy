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
import sam.server.samdev.relatorio.TableMapDataSource

public class CST_ResumoOrdemProducao extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Resumo Ordem de Produção"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	
	@Override 
	public DadosParaDownload executar() {
		Long idTipoDoc = getListLong("tipo");
		Integer numPlanoIni = getInteger("inicial");
		Integer numPlanoFinal = getInteger("final");

		List<TableMap>ordemProducao = buscarOrdensProducao(idTipoDoc,numPlanoIni, numPlanoFinal);
		return gerarPDF("CST_resumoordemdeprodução", ordemProducao);
	}

	private List<TableMap> buscarOrdensProducao(Long idTipoDoc, Integer numPlanoIni, Integer numPlanoFinal ){
		 List<TableMap> teste = getSession().createQuery("select abb01num as numeroos,bab01dte as dataos, princ.abm01codigo as codItemPrinc,princ.abm01na as descrItemPrinc, umuPrinc.aam06codigo as umuProdPrinc,baa01descr as descplano,bab01qt as qtdPlanejadaPrinc,bab01qtp as qtdRealPrinc,bab01ctcc,bab0101qtp as qtdPlanejadaComp ,bab0101qta as qtdRealComp,princ.abm01tipo,comp.abm01codigo as codItemComp,comp.abm01na as descrItemComp,umuComp.aam06codigo as umuComp,bab01qt, bab01qtp "+  
											"from bab01 "+
											"inner join abb01 on abb01id = bab01central "+
											"inner join aah01 on aah01id = abb01tipo "+
											"inner join abp20 on abp20id = bab01comp "+
											"inner join abm01 as princ on princ.abm01id = abp20item "+
											"inner join aam06 as umuPrinc on umuPrinc.aam06id = princ.abm01umu "+
											"inner join bab0103 on bab0103op = bab01id "+
											"inner join baa0101 on baa0101id = bab0103itempp "+
											"inner join baa01 on baa01id = baa0101plano "+
											"inner join bab0101 on bab0101op = bab01id "+
											"inner join abm01 as comp on comp.abm01id = bab0101item "+
											"inner join aam06 as umuComp on umuComp.aam06id = comp.abm01umu "+
											(numPlanoIni != null && numPlanoFinal != null ? "where abb01num between :numPlanoIni and :numPlanoFinal " : "")+
											(idTipoDoc != null ? "and aah01id in (:idTipoDoc) " : "" ))  
											.setParameters("numPlanoIni",numPlanoIni,"numPlanoFinal",numPlanoFinal,"idTipoDoc",idTipoDoc)
											.getListTableMap();
		//throw new ValidacaoException(idTipoDoc.toString())
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJlc3VtbyBPcmRlbSBkZSBQcm9kdcOnw6NvIiwidGlwbyI6InJlbGF0b3JpbyJ9