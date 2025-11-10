package multitec.relatorios.sac;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.ca.Caa10
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

/**
 * Classe para Relatório - SAC Lista de Atendimentos
 * @author Samuel André
 * @version 1.0
 * @copyright Multitec Sistemas
 *
 */
public class SAC_ListaDeAtendimentos extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SAC - Lista de Atendimentos";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais(){
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("numIni", "000000000");
		filtrosDefault.put("numFin", "999999999");
		filtrosDefault.put("data", datas);
		filtrosDefault.put("encerradas", true);
		filtrosDefault.put("pendentes", true);
		
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Long numIni = getLong("numIni");
		Long numFin = getLong("numFin");
		LocalDate[] datas = getIntervaloDatas("data");
		List<Long> idsUsuarios = getListLong("atendente");
		List<Integer> tiposItem = getListInteger("mpms");
		List<Long> idsItens = getListLong("itens");
		List<Long> idsReclamacoes = getListLong("reclamacoes");
		List<Long> idsProvidencias = getListLong("providencias");
		String ddd = getString("ddd");
		String fone = getString("fone");
		Boolean isEncerradas = get("encerradas");
		Boolean isPendentes = get("pendentes");
		String nome = getString("nome");
		
		Aac10 aac10 = getVariaveis().getAac10();
		String nomeRelatorio = "SAC_ListaDeAtendimentos";
				
		// Seta os parametros principais
		params.put("TITULO_RELATORIO", "Lista de Atendimentos");
		params.put("EMPRESA", aac10.getAac10na());
		String data = datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + datas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
		params.put("PERIODO", data);
		
		List<TableMap> listaAtendimentos = buscarDadosRelatorio(numIni, numFin, datas, idsUsuarios, tiposItem, idsItens, idsReclamacoes, idsProvidencias, ddd, fone, isEncerradas, isPendentes, nome);
		
		return gerarPDF(nomeRelatorio, listaAtendimentos);
	}
	
	/**
	 * Método para buscar os dados para compoção do relatorio
	 * @param numIni
	 * @param numFin
	 * @param datas
	 * @param idsUsuarios
	 * @param tiposItem
	 * @param idsItens
	 * @param idsReclamacoes
	 * @param idsProvidencias
	 * @param ddd
	 * @param fone
	 * @param isEncerradas
	 * @param isPendentes
	 * @param nome
	 * @return Lista de TableMap com os dados do relatorio
	 */
	public List<TableMap> buscarDadosRelatorio(Long numIni, Long numFin, LocalDate[] datas, List<Long> idsUsuarios, List<Integer> tiposItem, List<Long> idsItens, List<Long> idsReclamacoes, List<Long> idsProvidencias, String ddd, String fone, Boolean isEncerradas, Boolean isPendentes, String nome) {
		List<TableMap> listAtendimentos = buscarAtendimentos(numIni, numFin, datas, idsUsuarios, tiposItem, idsItens, idsReclamacoes, idsProvidencias, ddd, fone, isEncerradas, isPendentes, nome);
		return listAtendimentos;
	}
	
	/**
	 * Método para buscar os dados no banco passando os filtros do Front-end
	 * @param numIni
	 * @param numFin
	 * @param datas
	 * @param idsUsuarios
	 * @param tiposItem
	 * @param idsItens
	 * @param idsReclamacoes
	 * @param idsProvidencias
	 * @param ddd
	 * @param fone
	 * @param isEncerradas
	 * @param isPendentes
	 * @param nome
	 * @return Lista de TableMap com os dados obtidos no banco
	 */
	public List<TableMap> buscarAtendimentos(Long numIni, Long numFin, LocalDate[] datas, List<Long> idsUsuarios, List<Integer> tiposItem, List<Long> idsItens, List<Long> idsReclamacoes, List<Long> idsProvidencias, String ddd, String fone, Boolean isEncerradas, Boolean isPendentes, String nome){
		String whereDatas    = datas != null ? getWhereDataInterval("WHERE", datas, "caa10data ") : "";
		String whereUsuarios = idsUsuarios != null && idsUsuarios.size() > 0 ? "AND caa10user IN (:idsUsuarios) " : "";
		String whereTipoItem = tiposItem != null && tiposItem.size() > 0 ? "AND abm01tipo IN (:tiposItem) " : "";
		String whereItens    = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
		String whereReclamac = idsReclamacoes != null && idsReclamacoes.size() > 0 ? "AND r.caa01id IN (:idsReclamacoes) " : "";
		String whereProviden = idsProvidencias != null && idsProvidencias.size() > 0 ? "AND p.caa01id IN (:idsProvidencias) " : "";
		String whereDdd      = ddd != null ? "AND caa10ddd1 = :ddd OR caa10ddd2 = :ddd " : "";
		String whereFone     = fone != null ? "AND (caa10fone1 = :fone OR caa10fone2 = :fone) " : "";
		String whereNome     = nome != null ? "AND caa10nome = :nome " : "";
		String whereNumero   = numIni != null && numFin != null ? "AND caa10num BETWEEN :numIni AND :numFin " : "";
		String whereEncerr   = (isEncerradas && isPendentes) ? "" : isEncerradas ? "AND caa10dtEnc IS NOT NULL " : "AND caa10dtEnc IS NULL ";
		
		String sql = " SELECT DISTINCT caa10id, caa10num, caa10data, caa10dtEnc, caa10ddd1, caa10fone1, caa10nome, " +
					 " abm01codigo, abm01na, bcc01fabric, bcc01validade, r.caa01codigo as recCodigo, r.caa01descr as recDescr " +
					 " FROM Caa10 " +
					 " LEFT JOIN Caa1001 ON caa1001atend = caa10id " +
					 " LEFT JOIN Caa1002 ON caa1002atend = caa10id " + 
 					 " LEFT JOIN Caa01 r ON r.caa01id = caa1001rec " + 
					 " LEFT JOIN Caa01 p ON p.caa01id = caa1002prov " +
					 " INNER JOIN Aab10 ON aab10id = caa10user " +
					 " INNER JOIN Abm01 ON abm01id = caa10item " +
					 " LEFT JOIN Bcc01 ON bcc01id = caa10loteSerie " +
					   whereDatas + whereUsuarios + whereTipoItem + whereItens +
					   whereReclamac + whereProviden + whereDdd + whereFone + whereEncerr +
					   whereNome + whereNumero + getSamWhere().getWherePadrao("AND", Caa10.class) +
					 " ORDER BY caa10id";
					 
		Query query = getSession().createQuery(sql);
		if(idsUsuarios != null && idsUsuarios.size() > 0)query.setParameter("idsUsuarios", idsUsuarios);
		if(tiposItem != null && tiposItem.size() > 0)query.setParameter("tiposItem", tiposItem);
		if(idsItens != null && idsItens.size() > 0)query.setParameter("idsItens", idsItens);
		if(idsReclamacoes != null && idsReclamacoes.size() > 0)query.setParameter("idsReclamacoes", idsReclamacoes);
		if(idsProvidencias != null && idsProvidencias.size() > 0)query.setParameter("idsProvidencias", idsProvidencias);
		if(ddd != null)query.setParameter("ddd", ddd);
		if(fone != null)query.setParameter("fone", fone);
		if(nome != null)query.setParameter("nome", nome);
		if(numIni != null)query.setParameter("numIni", numIni);
		if(numFin != null)query.setParameter("numFin", numFin);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNBQyAtIExpc3RhIGRlIEF0ZW5kaW1lbnRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==