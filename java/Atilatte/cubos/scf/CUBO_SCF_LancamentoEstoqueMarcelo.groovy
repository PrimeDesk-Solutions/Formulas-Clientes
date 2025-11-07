package Atilatte.cubos.scf;
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

public class CUBO_SCF_LancamentoEstoqueMarcelo extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCF - Lançamentos de Estoque (MARCELO)"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "0000001");
		filtrosDefault.put("numeroFinal", "9999999");
		return Utils.map("filtros", filtrosDefault);
	}
	@Override 
	public DadosParaDownload executar() {
		
		List<TableMap> dados = dadosRelatorio();	
		gerarXLSX("LancamentoEstoqueMarcelo",dados)	
	}
	private List<TableMap> dadosRelatorio(){
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		List<Long> tipoDoc = getListLong("tipoDoc");
		List<Long> itens = getListLong("itens");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		List<Long> parametrosLanc = getListLong("tipo");
		String statusIni= getString("statusInicial");
		String statusFin = getString("statusFinal");
		String localIni = getString("localInicial");
		String localFin = getString("localFinal");

		LocalDate dataEmissaoIni = null;
		LocalDate dataEmissaoFin = null;
		if(dataEmissao != null){
			dataEmissaoIni = dataEmissao[0];
			dataEmissaoFin = dataEmissao[1];
		}
		

		Query sql = getSession().createQuery("select bcc01data as data, abm01codigo as codItem, abm01na as item, aam06codigo as UMU, abm20codigo as codMov, abm20descr as descrMov, "+
										"aah01codigo as tipoDoc, aah01nome as descrTipoDoc, abb01num as numDoc, aam04codigo as status, abm15nome as local, bcc01lote as lote, "+
										"bcc01qt as qtde, cast(bcc01json ->> 'unitario_estoque' as numeric(16,2)) as unitEstoq, bcc01custo as custo, case when abm01tipo = 0 then 'M' "+ 
										"when abm01tipo = 1	then 'P' else '' end as MPS "+
										"from bcc01 "+
										"inner join abm01 on abm01id = bcc01item "+
										"inner join aam06 on aam06id = abm01umu "+
										"inner join abm20 on abm20id = bcc01ple "+
										"inner join abb01 on abb01id = bcc01central "+
										"inner join aah01 on aah01id = abb01tipo "+
										"inner join aam04 on aam04id = bcc01status "+
										"inner join abm15 on abm15id = bcc01ctrl0 "+
										"where abb01num between :numDocIni and :numDocFin "+
										(dataEmissaoIni != null && dataEmissaoFin ? "and bcc01data between :dataEmissaoIni and :dataEmissaoFin " : "")+
										(tipoDoc != null && tipoDoc.size() > 0 ? "and aah01id in (:tipoDoc) " : "")+
										(itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "")+
										(statusIni != null && statusFin != null ? "and aam04codigo between :statusIni and :statusFin " : "")+
										(localIni != null && localFin != null ? "and abm15nome between :localIni and :localFin " : "")+
										(parametrosLanc != null && parametrosLanc.size() > 0 ? "and abm20id in (:parametrosLanc) " : ""));
										
		if(numDocIni != null && numDocFin != null ){
			sql.setParameter("numDocIni",numDocIni);
			sql.setParameter("numDocFin",numDocFin);
		}
		 
		if(dataEmissaoIni != null && dataEmissaoFin!= null ){
			sql.setParameter("dataEmissaoIni", dataEmissaoIni);
			sql.setParameter("dataEmissaoFin", dataEmissaoFin);
		}

		if(tipoDoc != null && tipoDoc.size() > 0 ){
			sql.setParameter("tipoDoc", tipoDoc);
		}

		if(itens != null && itens.size() > 0){
			sql.setParameter("itens", itens);
		}

		if(statusIni != null && statusFin!= null ){
			sql.setParameter("statusIni", statusIni);
			sql.setParameter("statusFin", statusFin);
		}
		
		if(localIni != null && localFin!= null ){
			sql.setParameter("localIni", localIni);
			sql.setParameter("localFin", localFin);
		}
		if(parametrosLanc != null && parametrosLanc.size() > 0){
			sql.setParameter("parametrosLanc", parametrosLanc);
		}

		List<TableMap> registroSQl = sql.getListTableMap();
		return registroSQl;
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ0YgLSBMYW7Dp2FtZW50b3MgZGUgRXN0b3F1ZSAoTUFSQ0VMTykiLCJ0aXBvIjoicmVsYXRvcmlvIn0=