package multitec.relatorios.smr;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abb20
import sam.model.entities.be.Bec0101
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

public class SMR_AssistenciaTecnica extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SMR - Assistência Técnica";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] dtEnvio = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("dtEnvio", dtEnvio);
		filtrosDefault.put("retorno", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**
	 * Método para gerar os PDF
	 * @return gerarPDF
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsBemPatrimonial = getListLong("bemPatrimonial");
		List<Long> idsEntidades = getListLong("entidades");
		LocalDate[] datas = getIntervaloDatas("dtEnvio");
		Integer retorno = getInteger("retorno");
		
		params.put("TITULO_RELATORIO", "Assistência Técnica");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		List<TableMap> assistenciaTecnica = buscarDadosRelatorio(idsBemPatrimonial, idsEntidades, datas, retorno);		
	     
	    String nomeRelatorio = "SMR_AssistenciaTecnica_R1";
				
		return gerarPDF(nomeRelatorio, assistenciaTecnica);
	}
	
	/**
	 * Método para realizar a busca no Banco
	 * @return
	 */
	public List<TableMap> buscarDadosRelatorio(List<Long> idsBemPatrimonial, List<Long> idsEntidades, LocalDate[] datas, Integer retorno) {
		return buscarAssistenciaTecnica(idsBemPatrimonial, idsEntidades, datas, retorno);
	}
	
	/**
	 * Método para executar a SQL
	 * @param idsBemPatrimonial - Bem Patrimonial
	 * @param idsEntidades - Entidade
	 * @param datas - Intervalo de datas
	 * @param retorno 0-Retornados 1-Não retornados 2-Ambos 
	 * @return Dados do Banco
	 */
	public List<TableMap> buscarAssistenciaTecnica(List<Long> idsBemPatrimonial, List<Long> idsEntidades, LocalDate[] datas, Integer retorno) {

		String whereRet = retorno == 2 ? "" : retorno == 0 ? "AND bec0101dtret IS NOT NULL " : "AND bec0101dtret IS NULL ";
		String whereAbb20BemPatrimonial = idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty() ? "AND abb20id IN (:idsBemPatrimonial) " : "";
		String whereAbe01Entidade = idsEntidades != null && !idsEntidades.isEmpty() ? "AND abe01 IN (:idsEntidades) " : "";
		String whereData = datas != null ? getWhereDataInterval("WHERE", datas, "bec0101dtenv") : "";
		
		String sql = "SELECT abb20codigo, abb20nome, abe01codigo, abe01na, bec0101dtenv, bec0101dtret, bec0101defeito, bec0101valor " +
					 "FROM Bec0101 " + 
					 "INNER JOIN bec01 ON bec01id = bec0101at " +
					 "INNER JOIN Abb20 ON abb20id = bec01bem " +
					 "INNER JOIN Abe01 ON abe01id = bec0101ent " +
					 whereData + whereAbb20BemPatrimonial + whereAbe01Entidade + whereRet + getSamWhere().getWherePadrao("AND", Abb20.class) +
					 " ORDER BY bec0101dtenv, abb20codigo, abe01codigo";
		
		 Query query = getSession().createQuery(sql);
		 
		 if(idsBemPatrimonial != null && !idsBemPatrimonial.isEmpty()) query.setParameter("idsBemPatrimonial", idsBemPatrimonial);
		 if(idsEntidades != null && !idsEntidades.isEmpty()) query.setParameter("idsEntidades", idsEntidades);
		 
		 return  query.getListTableMap();
			
	}
	
}
//meta-sis-eyJkZXNjciI6IlNNUiAtIEFzc2lzdMOqbmNpYSBUw6ljbmljYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==