package Atilatte.cubos.sce;
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

public class CUBO_SCE_LancamentoEstoque extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCE - Lancamento Estoque"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
          LocalDate dataIni = getLocalDate("dataInicial");
          LocalDate dataFin = getLocalDate("dataFinal");
		Integer numI = getInteger("numeroInicial");
		Integer numF = getInteger("numeroFinal");
		Long idEntidadeIni = getLong("entIni");
		Long idEntidadeFin = getLong("entFin");
		String idTipoIni = getString("tipoIni");
		String idTipoFim = getString("tipoFim");
		String idStatIni = getString("statIni");
		String idStatFim = getString("statFin");		
		String idLocalIni = getString("LocalIni");
		String idLocalFim = getString("LocalFin");

//		throw new ValidacaoException(idLocalIni.toString())

		List<TableMap> dadosRelatorio = buscarDadosRelatorio(dataIni,dataFin,numI,numF,idEntidadeIni,idEntidadeFin,idTipoIni,idTipoFim,idStatIni,idStatFim,idLocalIni,idLocalFim);
		
		gerarXLSX("LancamentoEstoque", dadosRelatorio);

//		private List<TableMap> buscarDadosRelatorio(Integer operacao, Integer tipoOperacao, LocalDate[] dataEmissao,Long idEntidadeIni, Long idEntidadeFin){
//		LocalDate dataEmissaoIni = null;
//		LocalDate dataEmissaoFin = null;
	}
	 private List<TableMap> buscarDadosRelatorio(LocalDate dataIni,LocalDate dataFin,Integer numI, Integer numF,Long idEntidadeIni, Long idEntidadeFin, String idTipoIni, String idTipoFim, String idStatIni, String idStatFim, String idLocalIni, String idLocalFim){
	   		
//			throw new ValidacaoException(idLocalFim.toString())

			
		Query sql = getSession().createQuery(" Select abe01codigo as CodEnt, abe01na as Entidade,bcc01data as Data, abm01codigo as CodigoItem, abm01descr as Item, aam06codigo as bc02umd, abm20codigo as CodMov,abm20descr as DescrMov, aah01codigo as TipoDoc, aah01nome as TipoDescr, abb01num as numero, aam04codigo as status, abm15nome as Local, bcc01lote as Validade, bcc01serie as lote, bcc01qt as Qtde, cast(bcc01.bcc01json ->> 'unitario_estoque' as numeric(16,2)) as Unit, bcc01custo as Total,   "+                                                                                                                                                                                                                                                                                                                                   
			     "CASE WHEN abm01tipo = 0  THEN 'M' "+
				"WHEN abm01tipo = 1 THEN 'P' "+
				"ELSE '' END AS MPS "+                                                                                                                                                                                                                                                                                                                                                       
                    "from bcc01 " +    
                    "inner join abm01 on bcc01item = abm01id "+
                    "inner join abm20 on bcc01ple = abm20id "+
				"inner join abb01 on bcc01central = abb01id "+
				"inner join aah01 on abb01tipo = aah01id "+
				"inner join abe01 on abb01ent = abe01id "+
				"inner join aam06 on abm01umu = aam06id "+
				"inner join aam04 on bcc01status = aam04id "+
				"inner join abm15 on bcc01ctrl0 = abm15id "+
				"WHERE abb01num BETWEEN  :numI  AND  :numF " +
                    "AND bcc01data BETWEEN :dataEmissaoIni AND :dataEmissaoFin " +
                    "AND aah01codigo between :idTipoIni AND :idTipoFim "+
                    "AND aam04codigo between :statIni and :statFin "+
                    "and abm15nome between :localIni and :localFin ");//+Status+" AND "+Local+" ");
//                    "AND abm20codigo IN("+lstVinc+")
                    sql.setParameter("numI",numI);
                    sql.setParameter("numF",numF);
                    sql.setParameter("dataEmissaoIni",dataIni);
                    sql.setParameter("dataEmissaoFin",dataFin);
                    sql.setParameter("idTipoIni",idTipoIni);
                    sql.setParameter("idTipoFim",idTipoFim);
                    sql.setParameter("statIni",idStatIni);
                    sql.setParameter("statFin",idStatFim); 
                    sql.setParameter("localIni",idLocalIni);
                    sql.setParameter("localFin",idLocalFim);
                    
                      
				List<TableMap> lancamento = sql.getListTableMap();

				return lancamento;
			
	   }   
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ0UgLSBMYW5jYW1lbnRvIEVzdG9xdWUiLCJ0aXBvIjoicmVsYXRvcmlvIn0=