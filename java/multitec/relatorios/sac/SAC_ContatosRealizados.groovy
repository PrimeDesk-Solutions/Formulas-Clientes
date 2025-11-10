package multitec.relatorios.sac;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abe01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
/**
 * Classe para gerar relatorio de contato realizados
 * @author Samuel André
 * @version 1.0
 * @copyright Multitec Sistemas
 *
 */
public class SAC_ContatosRealizados extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SAC - Contatos Realizados";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("situacao", '0');
		filtrosDefault.put("data", datas);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	@Override
	public DadosParaDownload executar() {
		List<Long> idsAssuntos = getListLong("assuntos");
		List<Long> idsEntidade = getListLong("entidades");
		List<Long> idsRepresen = getListLong("representantes");
		List<Long> idsAtendent = getListLong("atendente");
		LocalDate[] proxContat = getIntervaloDatas("proxContato");
		LocalDate[] datas      = getIntervaloDatas("data");
		Integer situacao       = getInteger("situacao");
		
		Aac10 aac10 = getVariaveis().getAac10();
		String nomeRelatorio = "SAC_ContatosRealizados";
				
		// Seta os parametros principais
		params.put("TITULO_RELATORIO", "Contatos Realizados");
		params.put("EMPRESA", aac10.getAac10na());
		
		List<TableMap> contatosRealizados = buscarDadosRelatorio(idsAssuntos, datas, idsEntidade, idsRepresen, idsAtendent, proxContat, situacao);
		
		return gerarPDF(nomeRelatorio, contatosRealizados);
	}
	
	/**
	 * Método para criar o relatorio com os dados
	 * @param idsAssuntos
	 * @param datas
	 * @param idsEntidade
	 * @param idsRepresen
	 * @param idsAtendent
	 * @param proxContat
	 * @param situacao
	 * @return Lista de TableMap com os dados do relatorio
	 */
	public List<TableMap> buscarDadosRelatorio(List<Long> idsAssuntos, LocalDate[] datas, List<Long> idsEntidade, List<Long> idsRepresen, List<Long> idsAtendent, LocalDate[] proxContat, Integer situacao){
		List<TableMap> listContatos = buscarContatosPorPeriodo(idsAssuntos, datas, idsEntidade, idsRepresen, idsAtendent, proxContat, situacao);
		
		return listContatos;
	}
	
	/**
	 * Método para buscar os dados do relatorio no banco
	 * @param idsAssuntos
	 * @param datas
	 * @param idsEntidade
	 * @param idsRepresen
	 * @param idsAtendent
	 * @param proxContat
	 * @param situacao
	 * @return Lista de TableMap com os dados obtidos no banco
	 */
	public List<TableMap> buscarContatosPorPeriodo(List<Long> idsAssuntos, LocalDate[] datas, List<Long> idsEntidade, List<Long> idsRepresen, List<Long> idsAtendent, LocalDate[] proxContat, Integer situacao){
		String whereData     = datas != null ? getWhereDataInterval("WHERE", datas, "cab10011data") : "";
		String whereProxCont = proxContat != null ? getWhereDataInterval("AND", proxContat, "cab10011dtProxCont") : "";
		String whereAssuntos = idsAssuntos != null ? "AND cab1001id IN (:idsAssuntos) " : "";
		String whereEntidade = idsEntidade != null ? "AND abe01id IN (:idsEntidade) " : "";
		String whereRepresen = idsRepresen != null ? "AND (abe01id IN (:idsRepresen) AND abe01rep = 1)" : "";
		String whereAtendent = idsAtendent != null ? "AND aab10id IN (:idsAtendent) " : "";
		
		String sql = " SELECT abe01id, cab10011id, abe01codigo, abe01na, abe0101eMail, abe0101ddd1, abe0101fone1, cab01codigo, " + 
					 " cab01descr, cab10011data, cab10011hora, aab10user, cab02codigo, cab02descr, cab10011dtProxCont, cab10011detalhe, cab10011contato " +
					 " FROM Cab10011 " +
					 " INNER JOIN Cab1001 ON cab1001id = cab10011ass " +
					 " INNER JOIN Cab10 ON cab10id = cab1001acomp " +
					 " INNER JOIN Abe01 ON abe01id = cab10ent " +
					 " LEFT JOIN Abe0101 ON abe0101ent = abe01id " +
					 " INNER JOIN Cab01 ON cab01id = cab10011assunto " +
					 " INNER JOIN Aab10 ON aab10id = cab10011user " +
					 " INNER JOIN Cab02 ON cab02id = cab10011resposta " +
					   whereData + whereProxCont + whereAssuntos + whereEntidade +
					   whereRepresen + whereAtendent + getSamWhere().getWherePadrao("AND", Abe01.class) +
					 " ORDER BY abe01id, abe01codigo, cab10011data";
					 
		Query query = session.createQuery(sql);
		query.setParameter("idsAssuntos", idsAssuntos);
		query.setParameter("idsEntidade", idsEntidade);
		query.setParameter("idsRepresen", idsRepresen);
		query.setParameter("idsAtendent", idsAtendent);
		
		return query.getListTableMap();
	}
	
}
//meta-sis-eyJkZXNjciI6IlNBQyAtIENvbnRhdG9zIFJlYWxpemFkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=