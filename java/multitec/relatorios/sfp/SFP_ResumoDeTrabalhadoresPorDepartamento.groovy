package multitec.relatorios.sfp;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01
import sam.model.entities.fb.Fba0101
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.sfp.service.SFPService


public class SFP_ResumoDeTrabalhadoresPorDepartamento extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SFP - Resumo de Trabalhadores por Departamento";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		filtrosDefault.put("listagem", "0");
		filtrosDefault.put("considerar", "0");
		filtrosDefault.put("isImpSalario", true);
		filtrosDefault.put("isCalcSalario", true);
		filtrosDefault.put("isImpDemitidos", false);
		filtrosDefault.put("posicao", MDate.date());

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDepartamentos = getListLong("departamentos");
		Boolean isTrabalhadores = getBoolean("isTrabalhador");
		Boolean isAutomoto = getBoolean("isAutonomo");
		Boolean isProLabore = getBoolean("isProlabore");
		Boolean isTerceiro = getBoolean("isTerceiros");
		Boolean imprimirSalario = getBoolean("isImpSalario");
		Boolean imprimirSalarioHorista = getBoolean("isCalcSalario");
		Boolean imprimirDemitidos = getBoolean("isImpDemitidos")
		Integer listagem = getInteger("listagem");
		Integer considerar = getInteger("considerar");
		LocalDate dataPosicao = getLocalDate("posicao")

		List<Integer> tiposTrabalhadores = converterTipoTrabalhador(isTrabalhadores, isAutomoto, isProLabore, isTerceiro);

		if(considerar.equals(0)) dataPosicao = LocalDate.now();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
		String dtPos = dataPosicao.format(formatter);
		Aac10 aac10 = getVariaveis().getAac10();

		params.put("TITULO_RELATORIO", "Resumo de Trabalhadores por Departamento");
		params.put("DATA_POS",dtPos );
		params.put("EMPRESA", aac10.getAac10na());

		List<TableMap> departamentos = new ArrayList<TableMap>();
		List<TableMap> trabalhadores = new ArrayList<TableMap>();

		if(considerar.equals(0)) trabalhadores = buscarTrabalhadores(idsTrabalhadores, idsDepartamentos, imprimirDemitidos, tiposTrabalhadores);
		if(considerar.equals(1)) trabalhadores = buscarTrabalhadoresPelaData(idsTrabalhadores, idsDepartamentos, imprimirDemitidos, tiposTrabalhadores, dataPosicao);

		if(trabalhadores == null || trabalhadores.size() <= 0) interromper("Nenhum trabalhador encontrado!");

		if(listagem.equals(0)) departamentos = montarDadosDepartamentosSintetico(trabalhadores, dataPosicao);
		if(listagem.equals(1)) departamentos = montarDadosDepartamentosAnalitico(trabalhadores, dataPosicao, imprimirSalario);


		String relatorio = listagem.equals(1) ? "SFP_ResumoDeTrabalhadoresPorDepartamento_R2" : "SFP_ResumoDeTrabalhadoresPorDepartamento_R1";
		return gerarPDF(relatorio, departamentos);
	}

	public List<TableMap> buscarTrabalhadores(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Boolean imprimirDemitidos, List<Integer> tiposTrabalhadores){
		Criterion trabalhadores = idsTrabalhadores != null && idsTrabalhadores.size() > 0 ? Criterions.in("abh80id", idsTrabalhadores) : Criterions.isTrue();
		Criterion departamentos = idsDepartamentos != null && idsDepartamentos.size() > 0 ? Criterions.in("abb11id", idsDepartamentos) : Criterions.isTrue();
		Criterion tipos = tiposTrabalhadores != null && tiposTrabalhadores.size() > 0 ? Criterions.in("abh80tipo", tiposTrabalhadores) : Criterions.isTrue();
		Criterion demitidos = imprimirDemitidos ? Criterions.isTrue() : Criterions.not(Criterions.eq("abh80sit", Abh80.SIT_DEMITIDO));

		return getSession().createCriteria(Abh80.class)
				.addFields("abh80id, abh80sexo, abh80nascData, abb11id, abb11nome, abb11codigo")
				.addFields("abh80codigo, abh80nome, abh05codigo, abh05nome, aap03codigo, abh80dtAdmis, abh80salario")
				.addJoin(Joins.join("abb11", "abb11id = abh80depto"))
				.addJoin(Joins.join("abh05", "abh05id = abh80cargo"))
				.addJoin(Joins.join("aap03", "aap03id = abh05cbo"))
				.addWhere(trabalhadores)
				.addWhere(departamentos)
				.addWhere(tipos)
				.addWhere(demitidos)
				.addWhere(getSamWhere().getCritPadrao(Abh80.class))
				.getListTableMap();
	}

	public List<TableMap> buscarTrabalhadoresPelaData(List<Long> idsTrabalhadores, List<Long> idsDepartamentos, Boolean imprimirDemitidos, List<Integer> tiposTrabalhadores, LocalDate dataPosicao){
		Criterion trabalhadores = idsTrabalhadores != null && idsTrabalhadores.size() > 0 ? Criterions.in("abh80id", idsTrabalhadores) : Criterions.isTrue();
		Criterion departamentos = idsDepartamentos != null && idsDepartamentos.size() > 0 ? Criterions.in("abb11id", idsDepartamentos) : Criterions.isTrue();
		Criterion tipos = tiposTrabalhadores != null && tiposTrabalhadores.size() > 0 ? Criterions.in("abh80tipo", tiposTrabalhadores) : Criterions.isTrue();
		Criterion demitidos = imprimirDemitidos ? Criterions.isTrue() : Criterions.not(Criterions.eq("abh80sit", Abh80.SIT_DEMITIDO));

		Integer ano = dataPosicao.getYear();
		Integer mes = dataPosicao.getMonthValue()

		return getSession().createCriteria(Fba01.class)
				.addFields("abh80id, abh80sexo, abh80nascData, abb11nome, abb11codigo")
				.addFields("abh80codigo, abh80nome, abh05codigo, abh05nome, aap03codigo, abh80dtAdmis, abh80salario")
				.addJoin(Joins.join("fba0101", "fba0101calculo = fba01id"))
				.addJoin(Joins.join("abh80", "abh80id = fba0101trab"))
				.addJoin(Joins.join("abb11", "abb11id = abh80depto"))
				.addJoin(Joins.join("abh05", "abh05id = abh80cargo"))
				.addJoin(Joins.join("aap03", "aap03id = abh05cbo"))
				.addWhere(trabalhadores)
				.addWhere(departamentos)
				.addWhere(tipos)
				.addWhere(demitidos)
				.addWhere(Criterions.eq("fba01ano", ano))
				.addWhere(Criterions.eq("fba01mes", mes))
				.addWhere(Criterions.eq("fba0101tpVlr", Fba0101.TPVLR_FOLHA))
				.addWhere(getSamWhere().getCritPadrao(Fba01.class))
				.getListTableMap();
	}

	public List<TableMap> montarDadosDepartamentosAnalitico(List<TableMap> trabalhadores, LocalDate dataPosicao, boolean imprimirSalario){
		List<TableMap> departamentosSinteticos = montarDadosDepartamentosSintetico(trabalhadores, dataPosicao);
		SFPService sfpService = instanciarService(SFPService.class);
		List<String> abb11codigos = new ArrayList<String>();
		Map<String,Integer> totais = new HashMap<String,Integer>();
		for (TableMap trabalhador in trabalhadores) {
			String abb11codigo = trabalhador.getString("abb11codigo");

			TableMap departamento = departamentosSinteticos.stream().filter({depto -> depto.getString("abb11codigo").equals(abb11codigo)}).collect(Collectors.toList()).get(0);

			Integer sit = sfpService.buscarSituacaoDoTrabalhador(trabalhador.getLong("abh80id"),dataPosicao);
			String situacao = sit.equals(0) ? "Trabalhando" : sit.equals(1) ? "Demitido" : sit.equals(2) ? "Em férias" : "Afastado";

			trabalhador.put("situacao",situacao);
			trabalhador.putAll(departamento);
			if(!imprimirSalario) trabalhador.put("abh80salario", null);

			if(!abb11codigos.contains(abb11codigo)) {
				abb11codigos.add(abb11codigo);

				totais.put("totalHomens", totais.get("totalHomens") == null ? departamento.getInteger("homens") : totais.get("totalHomens") + departamento.getInteger("homens"));
				totais.put("totalMulheres", totais.get("totalMulheres") == null ? departamento.getInteger("mulheres") : totais.get("totalMulheres") + departamento.getInteger("mulheres"));
				totais.put("totalMaiores", totais.get("totalMaiores") == null ? departamento.getInteger("maiores") : totais.get("totalMaiores") + departamento.getInteger("maiores"));
				totais.put("totalMenores", totais.get("totalMenores") == null ? departamento.getInteger("menores") : totais.get("totalMenores") + departamento.getInteger("menores"));
				totais.put("totalFerias", totais.get("totalFerias") == null ? departamento.getInteger("ferias") : totais.get("totalFerias") + departamento.getInteger("ferias"));
				totais.put("totalAfastamentos", totais.get("totalAfastamentos") == null ? departamento.getInteger("afastamentos") : totais.get("totalAfastamentos") + departamento.getInteger("afastamentos"));
				totais.put("totalDemissoes", totais.get("totalDemissoes") == null ? departamento.getInteger("demissoes") : totais.get("totalDemissoes") + departamento.getInteger("demissoes"));
				totais.put("totalTrabalhando", totais.get("totalTrabalhando") == null ? departamento.getInteger("trabalhando") : totais.get("totalTrabalhando") + departamento.getInteger("trabalhando"));
			}
		}

		Collections.sort(trabalhadores, new Comparator<TableMap>() {
			@Override
			public int compare(TableMap depto1, TableMap depto2) {
				return  depto1.getString("abh80codigo").compareTo(depto2.getString("abh80codigo"));
			}
		});
		Collections.sort(trabalhadores, new Comparator<TableMap>() {
			@Override
			public int compare(TableMap depto1, TableMap depto2) {
				return  depto1.getString("abb11codigo").compareTo(depto2.getString("abb11codigo"));
			}
		});

		totais.put("totalTrabalhadores", trabalhadores.size());
		params.putAll(totais);
		return trabalhadores;
	}

	public List<TableMap> montarDadosDepartamentosSintetico(List<TableMap> trabalhadores, LocalDate dataPosicao) {
		HashMap<String, TableMap> dadosDepartamentos = new HashMap<String, TableMap>();
		SFPService sfpService = instanciarService(SFPService.class);
		for (TableMap trabalhador in trabalhadores) {
			String abb11codigo = trabalhador.getString("abb11codigo");
			TableMap dadosDepartamento = dadosDepartamentos.get(abb11codigo) != null ? dadosDepartamentos.get(abb11codigo) : new TableMap();

			LocalDate nascimento = trabalhador.getDate("abh80nascData");
			Integer idade = calcularIdade(nascimento);
			Integer situacaoTrabalhador = sfpService.buscarSituacaoDoTrabalhador(trabalhador.getLong("abh80id"), dataPosicao);

			Boolean isHomem = trabalhador.getInteger("abh80sexo").equals(Abh80.SEXO_MASCULINO);
			Boolean isMaiorIdade = idade >= 18;
			Boolean isTrabalhando = situacaoTrabalhador.equals(0);
			Boolean isDemitido = situacaoTrabalhador.equals(1);
			Boolean isFerias = situacaoTrabalhador.equals(2);
			Boolean isAfastado = situacaoTrabalhador.equals(3);

			Integer homens = dadosDepartamento.getInteger("homens") == null ? 0 : dadosDepartamento.getInteger("homens");
			Integer mulheres = dadosDepartamento.getInteger("mulheres") == null ? 0 : dadosDepartamento.getInteger("mulheres");
			Integer maiores = dadosDepartamento.getInteger("maiores") == null ? 0 : dadosDepartamento.getInteger("maiores");
			Integer menores = dadosDepartamento.getInteger("menores") == null ? 0 : dadosDepartamento.getInteger("menores");
			Integer ferias = dadosDepartamento.getInteger("ferias") == null ? 0 : dadosDepartamento.getInteger("ferias");
			Integer afastamentos = dadosDepartamento.getInteger("afastamentos") == null ? 0 : dadosDepartamento.getInteger("afastamentos");
			Integer demissoes = dadosDepartamento.getInteger("demissoes") == null ? 0 : dadosDepartamento.getInteger("demissoes");
			Integer trabalhando = dadosDepartamento.getInteger("trabalhando") == null ? 0 : dadosDepartamento.getInteger("trabalhando");
			Integer trabalhadoresTotais = dadosDepartamento.getInteger("trabalhadorestotais") == null ? 0 : dadosDepartamento.getInteger("trabalhadorestotais");

			dadosDepartamento.put("abb11codigo", abb11codigo);
			dadosDepartamento.put("abb11nome", trabalhador.getString("abb11nome"));
			dadosDepartamento.put("homens", isHomem ? homens + 1 : homens);
			dadosDepartamento.put("mulheres", isHomem ? mulheres : mulheres + 1);
			dadosDepartamento.put("maiores", isMaiorIdade ? maiores + 1 : maiores);
			dadosDepartamento.put("menores", isMaiorIdade ? menores : menores + 1);
			dadosDepartamento.put("ferias", isFerias ? ferias +1 : ferias);
			dadosDepartamento.put("afastamentos", isAfastado ? afastamentos + 1 : afastamentos);
			dadosDepartamento.put("demissoes", isDemitido ? demissoes + 1 : demissoes);
			dadosDepartamento.put("trabalhando", isTrabalhando ? trabalhando + 1 : trabalhando);
			dadosDepartamento.put("trabalhadorestotais", trabalhadoresTotais + 1);

			dadosDepartamentos.put(abb11codigo, dadosDepartamento);
		}


		List<TableMap> departamentos = dadosDepartamentos.values().asList();

		Collections.sort(departamentos, new Comparator<TableMap>() {
			@Override
			public int compare(TableMap depto1, TableMap depto2) {
				return  depto1.getString("abb11codigo").compareTo(depto2.getString("abb11codigo"));
			}
		});

		return departamentos
	}

	public List<Integer> converterTipoTrabalhador(Boolean isTrabalhadores, Boolean isAutomoto, Boolean isProLabore, Boolean isTerceiro){
		List<Integer> tipos = new ArrayList<Integer>();

		if(isTrabalhadores) tipos.add(Abh80.TIPO_EMPREGADO);
		if(isAutomoto) tipos.add(Abh80.TIPO_AUTONOMO);
		if(isProLabore) tipos.add(Abh80.TIPO_PRO_LABORE);
		if(isTerceiro) tipos.add(Abh80.TIPO_TERCEIROS);

		return tipos;
	}

	public int calcularIdade(LocalDate nascimento) {
		if(nascimento != null) {
			return Period.between(nascimento, LocalDate.now()).getYears();
		}else {
			return 0;
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlc3VtbyBkZSBUcmFiYWxoYWRvcmVzIHBvciBEZXBhcnRhbWVudG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=