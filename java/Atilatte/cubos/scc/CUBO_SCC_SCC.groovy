package Atilatte.cubos.scc;
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

public class SCC_SCC extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SCC (Cubo)"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");

		List<TableMap> dadosRelatorio = buscarDocumentos(dataEmissao);

		gerarXLSX("SCC",dadosRelatorio);
		
	}

	private List<TableMap> buscarDocumentos(LocalDate[] dataEmissao){
		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;
		//Data Emissao - Inicial - Final
		if(dataEmissao != null){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1]; 
		}
		Query sql = getSession().createQuery("select abb01nota.abb01num as numNota, abb01nota.abb01data as dataEmissao, abd01nota.abd01codigo as pcdNota, eaa01nota.eaa01txcomis0 as txComiss0, abb01pedido.abb01num as numPedido,abe01nota.abe01na as entidadeNota, "+  
										"abd01pedido.abd01codigo as pcdPedido, aah01pedido.aah01codigo as tipoDocPedido "+
										"from eaa01 eaa01nota "+
										"inner join abb01 abb01nota on abb01nota.abb01id = eaa01nota.eaa01central "+ 
										"inner join abd01 abd01nota on abd01nota.abd01id = eaa01nota.eaa01pcd "+
										"inner join abb10 abb10nota on abb10nota.abb10id = abd01nota.abd01opercod "+
										"inner join eaa0103 eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id "+
										"inner join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id "+ 
										"inner join eaa0103 eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv "+
										"inner join eaa01 eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc "+
										"inner join abb01 abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central "+
										"inner join abd01 abd01pedido on abd01pedido.abd01id = eaa01pedido.eaa01pcd "+
										"inner join aah01 aah01pedido on aah01pedido.aah01id = abb01pedido.abb01tipo "+
										"inner join abe01 abe01nota on abe01nota.abe01id = abb01nota.abb01ent "+
										"where eaa01nota.eaa01txcomis0 = '0.00' "+ 
										"and abd01nota.abd01aplic = 1 and abd01nota.abd01es = 1 "+
										"and abb10tipocod = '1' "+
										"and eaa01nota.eaa01gc = '1322578' "+
										"and eaa01nota.eaa01rep0 not in ('306824','306825') "+
										"and eaa01nota.eaa01iscc = '1' "+
										(dataEmissaoIni != null && dataEmissaoFin != null ? "and abb01nota.abb01data between :dataEmissaoIni and :dataEmissaoFin " : ""));
		if(dataEmissaoIni != null && dataEmissaoFin != null ){
			sql.setParameter("dataEmissaoIni",dataEmissaoIni);
			sql.setParameter("dataEmissaoFin",dataEmissaoFin);
		}

		List<TableMap> documentos = sql.getListTableMap();
		return documentos;
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ0MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=