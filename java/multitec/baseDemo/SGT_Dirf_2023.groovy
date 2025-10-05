package multitec.baseDemo

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ed.Edd40
import sam.model.entities.ed.Edd4001
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SGT_Dirf_2023 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_DIRF;
	}

	@Override
	public void executar() {
		Integer referencia = getInteger("referencia")
		Integer calendario = getInteger("calendario")
		def tipo = get("tipo")
		def recibo = get("recibo")

		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(getVariaveis().aac10.aac10id);
		TableMap aac10json = aac10.aac10json != null ? aac10.aac10json : new TableMap()
		
		//Códigos dos valores
		String codigoValorIR 		= "530"																					//código para buscar os valores de imposto de renda
		String codigoValorRend		= "501"																					//código para buscar os valores de rendimento
		
		//campos com conteudos fixos
		String planoSaude           = aac10json.getString("dirf_plano") != null ? aac10json.getString("dirf_plano") : "N"	//Empressa possui plano de saude coletivo
		String socioOstensivo 		= "N" 																					//indicador de socio ostensivo responsavel por sociedade em conta de participação
		String depositario 			= "N" 																					//indicade de depositario de crédito decorrente de decisão judicial
		String clubeInvestimento 	= "N" 																					//declarante de instituição administradora de fundos ou clube de investimento
		String domiciliadoExterior 	= "N" 																					//declarante residente no exterior
		String recebeRecursos 		= "N" 																					//Recebe recursos do tesouro nacional
		String funcaoPublica 		= "N" 																					//Empresa é fundação publica
		String situacaoEspecial 	= "N" 																					//Declaração de situação especial
		LocalDate dataEvento 		= null 																					//Data do evento, preencher se "situacaoEspecial" for igual a "S"
		Integer natDeclarante       = 0																						//Natureza do declarante
		
		
		List<Long> idsGruposCentralizadores = buscarIdsGruposCentralizadoresDaMatrizFiliais(aac10)
		List<String> codigosLancamentosIR = buscarCodigosLancamentosIR(idsGruposCentralizadores,calendario,codigoValorIR)
		
		TextFile txt = new TextFile("|", false, true);

		//DIRF - Declaração de imposto de renda retido na fonte
/*Ordem 01*/		txt.print("Dirf");
/*Ordem 02*/		txt.print(referencia, 4);
/*Ordem 03*/		txt.print(calendario, 4);
/*Ordem 04*/		txt.print(tipo);
/*Ordem 05*/		txt.print(recibo == null ? null : StringUtils.ajustString(recibo, 12, '0', true));
/*Ordem 06*/		txt.print("ARNZRXP");
		txt.newLine();

		//RESPO - Responsável pelo preenchimento
/*Ordem 01*/		txt.print("RESPO");
/*Ordem 02*/		txt.print(StringUtils.extractNumbers(aac10.aac10rCpf), 11);
/*Ordem 03*/		txt.print(aac10.aac10rNome);
/*Ordem 04*/		txt.print(aac10.aac10rDddFone, 2);
/*Ordem 05*/		txt.print(aac10.aac10rFone);
/*Ordem 06*/		txt.print(null);
/*Ordem 07*/		txt.print(null);
/*Ordem 08*/		txt.print(aac10.aac10rEmail);
		txt.newLine();

		//DECPJ - Declarante pessoa juridica
/*Ordem 01*/		txt.print("DECPJ")
/*Ordem 02*/		txt.print(StringUtils.extractNumbers(aac10.aac10ni),14)
/*Ordem 03*/		txt.print(aac10.aac10rs,150)
/*Ordem 04*/		txt.print(natDeclarante)
/*Ordem 05*/		txt.print(StringUtils.extractNumbers(aac10.aac10rCpf), 11)
/*Ordem 06*/		txt.print(socioOstensivo)
/*Ordem 07*/		txt.print(depositario)
/*Ordem 08*/		txt.print(clubeInvestimento)
/*Ordem 09*/		txt.print(domiciliadoExterior)
/*Ordem 10*/		txt.print(planoSaude)
/*Ordem 11*/		txt.print(recebeRecursos)
/*Ordem 12*/		txt.print(funcaoPublica)
/*Ordem 13*/		txt.print(situacaoEspecial)
/*Ordem 14*/		txt.print(dataEvento != null ? dataEvento.format("ddMMyyyy") : null)
		txt.newLine();

		for(codigoLancamentoIR in codigosLancamentosIR) {
			List<TableMap> informacoesEntidades = buscarInformacoesEntidadesPJPorCodigoLancamentoIR(idsGruposCentralizadores,calendario,codigoValorIR, codigoLancamentoIR)

			//IDREC - Identificação do código de receita
/*Ordem 01*/		txt.print("IDREC")
/*Ordem 02*/		txt.print(codigoLancamentoIR, 4, '0', true)
			txt.newLine()
			
			for(informacoesEntidade in informacoesEntidades) {
				HashMap<Integer,BigDecimal> valoresMesesRTRT = buscarValoresTotaisPorMes(idsGruposCentralizadores,calendario, codigoValorRend, informacoesEntidade.getLong("abe01id"), codigoLancamentoIR )
				HashMap<Integer,BigDecimal> valoresMesesRTIRF = buscarValoresTotaisPorMes(idsGruposCentralizadores,calendario, codigoValorIR, informacoesEntidade.getLong("abe01id"), codigoLancamentoIR )
				
				if(valoresMesesRTIRF != null && valoresMesesRTIRF.size() > 0) {
					
					//BPJDEC – Beneficiário pessoa jurídica do declarante
/*Ordem 01*/  			txt.print("BPJDEC")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(informacoesEntidade.getString("abe01ni")), 14)
/*Ordem 03*/  			txt.print(informacoesEntidade.getString("abe01nome"),150)
					txt.newLine()
				
					//RTRT – Rendimentos Tributáveis – Rendimento Tributável
					if(valoresMesesRTRT != null && valoresMesesRTRT.size() > 0) {
					
/*Ordem 01*/  				txt.print("RTRT")
/*Ordem 02*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(1).toString()), 13, '0', true)
/*Ordem 03*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(2).toString()), 13, '0', true)
/*Ordem 04*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(3).toString()), 13, '0', true)
/*Ordem 05*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(4).toString()), 13, '0', true)
/*Ordem 06*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(5).toString()), 13, '0', true)
/*Ordem 07*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(6).toString()), 13, '0', true)
/*Ordem 08*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(7).toString()), 13, '0', true)
/*Ordem 09*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(8).toString()), 13, '0', true)
/*Ordem 10*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(9).toString()), 13, '0', true)
/*Ordem 11*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(10).toString()), 13, '0', true)
/*Ordem 12*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(11).toString()), 13, '0', true)
/*Ordem 13*/  				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(12).toString()), 13, '0', true)
/*Ordem 14*/				txt.print(StringUtils.extractNumbers(valoresMesesRTRT.get(13).toString()), 13, '0', true)
						txt.newLine()
					}
				
					//RTIRF – Rendimentos Tributáveis – Imposto sobre a Renda Retido na Fonte					
/*Ordem 01*/  			txt.print("RTIRF")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(1).toString()), 13, '0', true)
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(2).toString()), 13, '0', true)
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(3).toString()), 13, '0', true)
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(4).toString()), 13, '0', true)
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(5).toString()), 13, '0', true)
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(6).toString()), 13, '0', true)
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(7).toString()), 13, '0', true)
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(8).toString()), 13, '0', true)
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(9).toString()), 13, '0', true)
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(10).toString()), 13, '0', true)
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(11).toString()), 13, '0', true)
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(12).toString()), 13, '0', true)
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresMesesRTIRF.get(13).toString()), 13, '0', true)
					txt.newLine()
				}
			}
		}
		
		txt.print("FIMDirf")
		txt.newLine()
		//Gravar o arquivo TXT ora composto
		put("txt", txt);
	}
	
	private HashMap<Integer,BigDecimal> buscarValoresTotaisPorMes(List<Long> idsGruposCentralizadores, Integer ano, String codigoValorRend, Long idEntidade, String codigoReceita) {
		
		String whereGrupoCentralizadores = " where edd40gc in (:idsGC) "
		String whereAno = " and EXTRACT(YEAR FROM edd40data ) = :ano "
		String whereValorIR = " and (aaj53codigo = :codValorRend and edd4001valor > 0) "
		String whereIdEntidade = " and abe01id = :idEntidade "
		String whereCodigoReceita = " and aaj52codigo = :codigoReceita "
		
		String sql = " select "+
							" sum(edd4001valor) as valor, EXTRACT(MONTH FROM edd40data ) as mes " +
					 " from edd40 " +
							 " inner join abb01 on abb01id = edd40central " +
							 " inner join abe01 on abe01id = abb01ent " +
							 " inner join edd4001 on edd4001lct = edd40id " +
							 " inner join aaj53 on aaj53id = edd4001cv "+
							 " inner join aaj52 on aaj52id = edd40cr " +
					whereGrupoCentralizadores +
					whereAno +
					whereValorIR +
					whereIdEntidade +
					whereCodigoReceita +
					" group by " +
							" EXTRACT(MONTH FROM edd40data ) "
							
		Parametro parametroGrupoCentralizadores = Parametro.criar("idsGC", idsGruposCentralizadores)
		Parametro parametroAno = Parametro.criar("ano", ano)
		Parametro parametroValorRend = Parametro.criar("codValorRend", codigoValorRend)
		Parametro parametroIdEntidade = Parametro.criar("idEntidade", idEntidade)
		Parametro parametroCodigoReceita = Parametro.criar("codigoReceita", codigoReceita)
		
		List<TableMap> valoresMeses =  getAcessoAoBanco().buscarListaDeTableMap(sql,parametroGrupoCentralizadores,parametroAno,parametroValorRend,parametroIdEntidade,parametroCodigoReceita)
		
		HashMap<Integer,BigDecimal> mapValoresMeses = new HashMap()
		
		if(valoresMeses != null && valoresMeses.size() > 0 ) {
			for(valorMes in valoresMeses) {
				mapValoresMeses.put(valorMes.getInteger("mes"), valorMes.getBigDecimal("valor"))
			}
		}
		
		for(int i = 1; i <= 13; i++) {
			if(mapValoresMeses.get(i) == null) {
				mapValoresMeses.put(i, BigDecimal.ZERO)
			}
		}
		
		return mapValoresMeses
	}
	
	private List<TableMap> buscarInformacoesEntidadesPJPorCodigoLancamentoIR(List<Long> idsGruposCentralizadores, Integer ano, String codigoValorIR, String codigoLctoIR){
		
		String whereGrupoCentralizadores = " and edd40gc in (:idsGC) "
		String whereAno = " and EXTRACT(YEAR FROM edd40data ) = :ano "
		String whereValorIR = " and (aaj53codigo = :codValorIR and edd4001valor > 0) "
		String whereCodigoLctoIR = " and aaj52codigo = :codLctoIR "
		
		String sql = " select " + 
					 		" distinct abe01id, abe01ni, abe01nome "+
					 " from edd40 " +
					 		" inner join abb01 on abb01id = edd40central " +
							" inner join abe01 on abe01id = abb01ent " +
							" inner join aaj52 on aaj52id = edd40cr " +
							" inner join edd4001 on edd4001lct = edd40id " +
							" inner join aaj53 on aaj53id = edd4001cv " +
					" where " +
							" abe01ti = 0 " +
							whereGrupoCentralizadores +
							whereAno +
							whereValorIR +
							whereCodigoLctoIR +
					" order by " +
							" abe01ni "
		
		Parametro parametroGrupoCentralizadores = Parametro.criar("idsGC", idsGruposCentralizadores)
		Parametro parametroAno = Parametro.criar("ano", ano)
		Parametro parametroValorIR = Parametro.criar("codValorIR", codigoValorIR)
		Parametro parametroCodigoLctoIR = Parametro.criar("codLctoIR", codigoLctoIR)
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroGrupoCentralizadores,parametroAno,parametroValorIR,parametroCodigoLctoIR)
	}
	
	private List<String> buscarCodigosLancamentosIR(List<Long> idsGruposCentralizadores, Integer ano, String codigoValorIR){
		
		String whereGrupoCentralizadores = " where edd40gc in (:idsGC) "
		String whereAno = " and EXTRACT(YEAR FROM edd40data ) = :ano "
		String whereValorIR = " and (aaj53codigo = :codValorIR and edd4001valor > 0) "
		
		String sql = " select " +
							" distinct aaj52codigo " +
					 " from edd40 " +
							" inner join aaj52 on aaj52id = edd40cr " +
							" inner join edd4001 on edd4001lct = edd40id " +
							" inner join aaj53 on aaj53id = edd4001cv " +
				     whereGrupoCentralizadores +
					 whereAno +
					 whereValorIR +
				     " order by " +
							" aaj52codigo "

		Parametro parametroGrupoCentralizadores = Parametro.criar("idsGC", idsGruposCentralizadores)
		Parametro parametroAno = Parametro.criar("ano", ano)
		Parametro parametroValorIR = Parametro.criar("codValorIR", codigoValorIR)
		
		return getAcessoAoBanco().obterListaDeString(sql,parametroGrupoCentralizadores,parametroAno,parametroValorIR)
	}

	private List<Long> buscarIdsGruposCentralizadoresDaMatrizFiliais(Aac10 empresaAtiva){
		List<Long> idsEmpresas = buscarIdsEmpresasDaMatrizFiliais(empresaAtiva)

		String whereEmpresas = " and aac1001empresa in (:idsEmpresas) "

		String sql = " select " +
							" aac1001gc " +
				     " from aac1001 " +
				     " where " +
							" Lower(aac1001tabela) = 'ed' " +
				     whereEmpresas

		Parametro parametroEmpresas = Parametro.criar("idsEmpresas", idsEmpresas)

		return getAcessoAoBanco().obterListaDeLong(sql, parametroEmpresas )
	}

	private List<Long> buscarIdsEmpresasDaMatrizFiliais(Aac10 empresaAtiva){
		Boolean isMatriz = empresaAtiva.aac10matriz == null ? true : false
		List<Long> empresasDoGrupo = new ArrayList()

		if(isMatriz) {
			List<Long> filiais = session.createCriteria(Aac10.class).addFields("aac10id").addWhere(Criterions.eq("aac10matriz", empresaAtiva.aac10id)).setOrder("aac10codigo").getList(ColumnType.LONG)

			if(filiais != null && filiais.size() > 0) {
				empresasDoGrupo.add(empresaAtiva.aac10id)
				empresasDoGrupo.addAll(filiais)
			}

			return empresasDoGrupo
		}

		Long idEmpresaMatriz = session.createCriteria(Aac10.class).addFields("aac10id").addWhere(Criterions.eq("aac10id", empresaAtiva.aac10matriz.aac10id)).get(ColumnType.LONG)

		List<Long> filiais = session.createCriteria(Aac10.class).addFields("aac10id").addWhere(Criterions.eq("aac10matriz", idEmpresaMatriz)).setOrder("aac10codigo").getList(ColumnType.LONG)

		if(filiais != null && filiais.size() > 0) {
			empresasDoGrupo.add(idEmpresaMatriz)
			empresasDoGrupo.addAll(filiais)
		}

		return empresasDoGrupo
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDkifQ==