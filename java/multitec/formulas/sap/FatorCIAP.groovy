package multitec.formulas.sap;

import java.time.LocalDate

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.Scale;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ea.Eaa01
import sam.model.entities.ec.Ecc01
import sam.model.entities.ec.Ecc0101
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro

public class FatorCIAP extends FormulaBase {
	private Ecc0101 ecc0101;
	//Mapa de chave e valor para obtenção dos campos
	private HashMap mapeamentoDosCamposLivres = new HashMap()
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CIAP;
	}
	
	//Alinhamento dos campos livres do CIAP com os campos de cálculo utilizados nesta formula
	private void mapearCamposLivres() {
		mapeamentoDosCamposLivres.put("valor_contabil", "vlr_valorcontabil")
		mapeamentoDosCamposLivres.put("icm_base_calculo", "icm_bc")
		mapeamentoDosCamposLivres.put("icm_isento", "icm_isento")
		mapeamentoDosCamposLivres.put("icm_outras", "icm_outras")
		mapeamentoDosCamposLivres.put("valor_ipi", "ipi_ipi")
		mapeamentoDosCamposLivres.put("icm_variavel", "ciap_icm")
	}

	private String getNomeCampo(String chave) {
		return mapeamentoDosCamposLivres.get(chave)
	}
	
	public void executar(){
		mapearCamposLivres()
		
		
		ecc0101 = (Ecc0101)get("ecc0101");

		if(ecc0101 == null) throw new ValidacaoException("Necessário informar o registro de fator a ser calculado.");

		BigDecimal fator = BigDecimal.ZERO;

		TableMap ecc0101json = ecc0101.getEcc0101json() == null ? new TableMap() : ecc0101.getEcc0101json();

		List<String> cfopsASomar = ["5101", "5102", "5103"];
		List<String> cfopsADiminuir = ["1101", "1102", "1111"];

		int mes = ecc0101.ecc0101mes
		int ano = ecc0101.ecc0101ano

		TableMap valoresASomar = buscarSomaDosValoresDoPeriodoPeloCFOPs(mes,ano,cfopsASomar)
		TableMap valoresADiminuir = buscarSomaDosValoresDoPeriodoPeloCFOPs(mes,ano,cfopsADiminuir)

		ecc0101json.put(getNomeCampo("valor_contabil"), BigDecimal.ZERO)
		ecc0101json.put(getNomeCampo("icm_base_calculo"), BigDecimal.ZERO)
		ecc0101json.put(getNomeCampo("icm_isento"), BigDecimal.ZERO)
		ecc0101json.put(getNomeCampo("icm_outras"), BigDecimal.ZERO)
		ecc0101json.put(getNomeCampo("valor_ipi"), BigDecimal.ZERO)

		ecc0101json.put(getNomeCampo("valor_contabil"), valoresASomar.getBigDecimal_Zero(getNomeCampo("valor_contabil")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("valor_contabil")))
		ecc0101json.put(getNomeCampo("icm_base_calculo"), valoresASomar.getBigDecimal_Zero(getNomeCampo("icm_base_calculo")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("icm_base_calculo")))
		ecc0101json.put(getNomeCampo("icm_isento"),valoresASomar.getBigDecimal_Zero(getNomeCampo("icm_isento")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("icm_isento")))
		ecc0101json.put(getNomeCampo("icm_outras"), valoresASomar.getBigDecimal_Zero(getNomeCampo("icm_outras")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("icm_outras")))
		ecc0101json.put(getNomeCampo("valor_ipi"), valoresASomar.getBigDecimal_Zero(getNomeCampo("valor_ipi")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("valor_ipi")))

		BigDecimal vlrcontabil = ecc0101json.getBigDecimal(getNomeCampo("valor_contabil"));
		BigDecimal bcicms = ecc0101json.getBigDecimal(getNomeCampo("icm_base_calculo"));
		BigDecimal isentas = ecc0101json.getBigDecimal(getNomeCampo("icm_isento"));
		BigDecimal outras = ecc0101json.getBigDecimal(getNomeCampo("icm_outras"));
		BigDecimal ipi = ecc0101json.getBigDecimal(getNomeCampo("valor_ipi"));

		Ecc01 ecc01 = ecc0101.getEcc0101ficha();
		if(vlrcontabil != null && vlrcontabil.compareTo(new BigDecimal(0)) != 0) {
			if(ecc01.getEcc01modelo().equals(0)){ //Modelo B
				if(isentas == null) isentas = BigDecimal.ZERO;
				if(ipi == null) ipi = BigDecimal.ZERO;

				fator = isentas.add(ipi);
				ecc0101json.put(getNomeCampo("icm_variavel"), round( ( ecc01.getEcc01icms() / 60 ) , 2 ) )

			}else{ //Modelo D
				if(bcicms == null) bcicms = BigDecimal.ZERO;
				if(outras == null) outras = BigDecimal.ZERO;

				fator = bcicms.add(outras);
				
				Integer divisor = ecc01.getEcc01modelo().equals(1) ? 48 : 24 
				
				ecc0101json.put(getNomeCampo("icm_variavel"), round( ( ecc01.getEcc01icms() / divisor ) , 2 ) )
			}

			fator = fator/vlrcontabil;
			fator = round(fator, 6);
			
			if(fator > 9.999999) {
				fator = 9.999999
			}
		}

		ecc0101.setEcc0101fator(fator);
		//Cálculo do ICMS fixo do mês
		BigDecimal icmsFixo = DecimalUtils.create(ecc0101json.getBigDecimal_Zero(getNomeCampo("icm_variavel")).multiply(fator)).round(2).get();
		ecc0101.setEcc0101icms(icmsFixo);

		ecc0101.setEcc0101json(ecc0101json)
	}


	private TableMap buscarSomaDosValoresDoPeriodoPeloCFOPs(int mes, int ano, List<String> cfops) {
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(LocalDate.create(ano, mes, 1))

		String wherePeriodo = " and abb01data between :dataIni and :dataFim "
		String whereCfops   = " and aaj15codigo in (:cfops) "

		String sql = " select " +
					" sum(cast(eaa0103json->>'"+getNomeCampo("valor_contabil")+"' as float)) as "+getNomeCampo("valor_contabil")+", " +
					" sum(cast(eaa0103json->>'"+getNomeCampo("icm_base_calculo")+"' as float)) as "+getNomeCampo("icm_base_calculo")+", " +
					" sum(cast(eaa0103json->>'"+getNomeCampo("icm_isento")+"' as float)) as"+getNomeCampo("icm_isento")+", " +
					" sum(cast(eaa0103json->>'"+getNomeCampo("icm_outras")+"' as float)) as "+getNomeCampo("icm_outras")+", " +
					" sum(cast(eaa0103json->>'"+getNomeCampo("valor_ipi")+"' as float)) as "+getNomeCampo("valor_ipi")+" " +
				" from eaa0103 " +
					" inner join eaa01 on eaa01id = eaa0103doc " +
					" inner join abb01 on abb01id = eaa01central " +
					" inner join aaj15 on aaj15id = eaa0103cfop " +
				" where eaa01cancData is null and eaa01clasDoc = 1 " + wherePeriodo + whereCfops + getSamWhere().getWherePadrao("and", Eaa01.class)

		Parametro parametroPeriodoIni = Parametro.criar("dataIni",periodo[0])
		Parametro parametroPeriodoFim = Parametro.criar("dataFim",periodo[1])
		Parametro parametroCfops = Parametro.criar("cfops",cfops)

		return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroPeriodoIni,parametroPeriodoFim,parametroCfops)
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTkifQ==