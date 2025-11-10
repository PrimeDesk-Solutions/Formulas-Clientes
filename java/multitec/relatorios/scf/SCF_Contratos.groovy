package multitec.relatorios.scf;

import java.time.LocalDate

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.da.Dac10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_Contratos extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCF - Contratos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();

		filtrosDefault.put("operacao", "R");
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");	
	
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		params.put("TITULO_RELATORIO", "Contratos");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());

		List<Long> idTipo = getListLong("tipo");
		LocalDate[] dataVigencia = getIntervaloDatas("dataVigencia");
		boolean isContratosRescindidos = get("contratosRescindidos");
		String operacao = get("operacao");
		Integer numeroInicial = getString("numeroInicial").length() > 0 ? getInteger("numeroInicial"): 0;
		Integer numeroFinal =  getString("numeroFinal").length() > 0 ? getInteger("numeroFinal"): 999999999;

		List<TableMap> dados = obterDadosRelatorio(idTipo, dataVigencia, operacao, numeroInicial, numeroFinal, isContratosRescindidos);

		return gerarPDF("SCF_Contratos", dados)
	}

	public List<TableMap> obterDadosRelatorio (List<Long> idTipo, LocalDate[] dataVigencia, String operacao, Integer numeroInicial, Integer numeroFinal, boolean isContratosRescindidos)  {
		
		String whereDataVigencia = dataVigencia != null && dataVigencia[0] != null && dataVigencia[1] != null ? " and dac10.dac10dtI >= '" + dataVigencia[0] + "' and dac10.dac10dtF <= '" + dataVigencia[1] + "'": "";
		String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= '" + numeroInicial + "' and abb01.abb01num <= '" + numeroFinal + "'": "";
		
		String whereOperacao = null;
		if (operacao == "R") {
			whereOperacao = " and dac10.dac10rp = " + Dac10.RP_RECEBER;
		} else {
			whereOperacao = " and dac10.dac10rp = " + Dac10.RP_PAGAR;
		}
		
		String listaRecisao = "";
		String whereRecisao = "";
		if (isContratosRescindidos) {
			listaRecisao = " , dac10.dac10resData";
			whereRecisao = " and (dac10.dac10resData is null or dac10.dac10resData is not null)"
		} else {
			whereRecisao = " and dac10.dac10resData is null";
		}
		
		String whereIdTipo = idTipo != null && idTipo.size() > 0 ? " and aah01.aah01id IN (:idTipo)": "";
		Parametro parametro = idTipo != null && idTipo.size() > 0 ? Parametro.criar("idTipo", idTipo) : null;

		String sql = " select dac10.dac10resenha, dac10.dac10id, dac10.dac10vctoDias, dac10.dac10vctoMeses, dac10.dac10vctoSemana, dac1001.dac1001id, dac1001.dac1001data, dac1001.dac1001valor, " + 
		        " abb11.abb11id, abb11.abb11codigo, abb11.abb11nome, dac1003.dac1003valor, abf10.abf10id, abf10.abf10codigo, abf10.abf10nome, dac10031.dac10031valor, " +
				" dac10.dac10id, abb01.abb01num, dac10.dac10dtI, dac10.dac10dtF, aah01.aah01codigo, aah01.aah01nome, abe01.abe01nome, abe01.abe01codigo, dac10.dac10valor, dac10.dac10rp, dac10.dac10resenha " + listaRecisao +
				" from Dac10 dac10 " +
				" left join Dac1001 dac1001 " +
				"   on dac1001.dac1001contrato = dac10.dac10id " +
				" left join Abb01 abb01 " +
				"   on abb01.abb01id = dac10.dac10central " +
				" left join Abe01 abe01 " +
				"   on abb01.abb01ent = abe01.abe01id " +
				" left join Aah01 aah01 " +
				"   on aah01.aah01id = abb01.abb01tipo " +
				" left join Dac1003 dac1003 " +
				"   on dac10.dac10id = dac1003.dac1003contrato " +
				" left join Abb11 abb11 " +
				"   on abb11.abb11id = dac1003.dac1003depto " +
				" left join Dac10031 dac10031 " +
				"   on dac1003.dac1003id = dac10031.dac10031depto " +
				" left join Abf10 abf10 " +
				"   on abf10.abf10id = dac10031.dac10031nat " +
				getSamWhere().getWherePadrao(" WHERE ", Dac10.class) +
				whereDataVigencia +
				whereIdTipo +
				whereNumero +
				whereRecisao +
				whereOperacao +
				" order by abb01.abb01num";

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro); 
		return receberDadosRelatorio;
	}
	
	
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIENvbnRyYXRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==