package Atilatte.cubos.sac;
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

import sam.model.entities.ca.Caa10;

import java.time.LocalDate

public class CUBO_SAC_SAC extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CUBO - SAC";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("numeroInicial", "1");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("situacaoDoc", "0");

		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		Integer numIni = getInteger("numeroInicial");
		Integer numFin = getInteger("numeroFinal");
		List<Long> itens = getListLong("itens");
		LocalDate[] data = getIntervaloDatas("datas");
		List<Long> entidades = getListLong("entidade");
		List<Long> reclamacao = getListLong("reclamacao");

		List<TableMap> tmReclamacoes = buscarReclamacoes(numIni, numFin, itens,data, entidades, reclamacao);
		gerarXLSX("SAC",tmReclamacoes)

	}

	private List<TableMap> buscarReclamacoes(Integer numIni, Integer numFin, List<Long> itens,LocalDate[] data, List<Long> entidades, List<Long> reclamacao){

		// Data Inicial - Final
		LocalDate dataIni = null;
		LocalDate dataFin = null;
		if(data != null){
			dataIni = data[0];
			dataFin = data[1];
		}

		Query sql = getSession().createQuery("select caa10num as numAtendimento, caa10data as dataAtendimento, caa10dtenc as dataEncerramento,abm01codigo as codItem, abm01na as naItem, abe01codigo as codEntidade, abe01na as naEntidade, caa01codigo as codReclamacao, caa01descr as descrReclamacao, "+
				"cast(caa10json ->> 'tipo_atend' as character varying(30)) as tipoAtend, cast(caa10json ->> 'responsavel' as character varying(30)) as responsavel,cast(caa10json ->> 'tel_contato' as character varying(30)) as contato, "+
				"cast(caa10json ->> 'lote' as character varying(50)) as lote, cast(caa10json ->> 'validade' as date ) as validade, cast(caa10json ->> 'dtfabricacao' as date) as dataFabricacao "+
				"from caa10 "+
				"inner join abm01 on abm01id = caa10item "+
				"left join abe01 on abe01id =caa10ent "+
				"left join caa1001 on caa1001atend = caa10id "+
				"left join caa01 on caa01id = caa1001rec "+
				getSamWhere().getWherePadrao("WHERE", Caa10.class) +
				(dataIni != null && dataFin!= null ? "and caa10data between :dataIni  and :dataFin " : "" )+
				(numIni != null && numFin!= null ? "and caa10num between :numIni and :numFin " : "" )+
				(entidades != null  ? "and abe01id in (:entidades) " : "") +
				(reclamacao != null ? "and caa01id in (:reclamacao) " : "") +
				(itens != null ? "and abm01id in (:itens) " : "" ));

		if(dataIni != null && dataFin != null){
			sql.setParameter("dataIni",dataIni);
			sql.setParameter("dataFin",dataFin);
		}

		if(numIni != null && numFin != null){
			sql.setParameter("numIni",numIni);
			sql.setParameter("numFin",numFin);
		}

		if(entidades != null){
			sql.setParameter("entidades",entidades);
		}
		if(reclamacao != null ){
			sql.setParameter("reclamacao",reclamacao);
		}
		if(itens != null){
			sql.setParameter("itens",itens);
		}

		return sql.getListTableMap();
	}


}
//meta-sis-eyJkZXNjciI6IlNBQyAoQ3VibykiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQUMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=