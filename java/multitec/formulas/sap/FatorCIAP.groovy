package multitec.formulas.sap;

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaj15
import sam.model.entities.ea.Eaa01
import sam.model.entities.ec.Ecc01
import sam.model.entities.ec.Ecc0101
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

import java.time.LocalDate

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
		mapeamentoDosCamposLivres.put("vlr_valorcontabil", "vlr_valorcontabil");
		mapeamentoDosCamposLivres.put("icm_base_calculo", "icm_bc");
		mapeamentoDosCamposLivres.put("icm_isento", "icm_isento");
		mapeamentoDosCamposLivres.put("icm_outras", "icm_outras");
		mapeamentoDosCamposLivres.put("valor_ipi", "ipi_ipi");
		mapeamentoDosCamposLivres.put("icm_variavel", "ciap_icm");
	}

	private String getNomeCampo(String chave) {
		return mapeamentoDosCamposLivres.get(chave);
	}

	public void executar(){
		mapearCamposLivres();

		ecc0101 = (Ecc0101)get("ecc0101");

		if(ecc0101 == null) throw new ValidacaoException("Necessário informar o registro de fator a ser calculado.");

		def fator = 0.0;

		TableMap ecc0101json = ecc0101.getEcc0101json() == null ? new TableMap() : ecc0101.getEcc0101json();

		List<String> cfopsASomar = obterCfops(0);
	   if(cfopsASomar == null || cfopsASomar.size() == 0) throw new ValidacaoException("Não foram encontrados CFOPs a somar.");
		
		List<String> cfopsADiminuir = obterCfops(1);
		if(cfopsADiminuir == null || cfopsADiminuir.size() == 0) throw new ValidacaoException("Não foram encontrados CFOPs a diminuir.");

		int mes = ecc0101.ecc0101mes;
		int ano = ecc0101.ecc0101ano;

		TableMap valoresASomar = buscarSomaDosValoresDoPeriodoPeloCFOPs(mes,ano,cfopsASomar);
		TableMap valoresADiminuir = buscarSomaDosValoresDoPeriodoPeloCFOPs(mes,ano,cfopsADiminuir);

		ecc0101json.put("vlr_valorcontabil", 0.0);
		ecc0101json.put(getNomeCampo("icm_base_calculo"), 0.0);
		ecc0101json.put(getNomeCampo("icm_isento"), 0.0);
		ecc0101json.put(getNomeCampo("icm_outras"), 0.0);
		ecc0101json.put(getNomeCampo("valor_ipi"), 0.0);

		ecc0101json.put("vlr_valorcontabil", valoresASomar.getBigDecimal_Zero("vlr_valorcontabil") - valoresADiminuir.getBigDecimal_Zero("valor_contabil"));
		ecc0101json.put(getNomeCampo("icm_base_calculo"), valoresASomar.getBigDecimal_Zero(getNomeCampo("icm_base_calculo")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("icm_base_calculo")));
		ecc0101json.put(getNomeCampo("icm_isento"),valoresASomar.getBigDecimal_Zero(getNomeCampo("icm_isento")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("icm_isento")));
		ecc0101json.put(getNomeCampo("icm_outras"), valoresASomar.getBigDecimal_Zero(getNomeCampo("icm_outras")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("icm_outras")));
		ecc0101json.put(getNomeCampo("valor_ipi"), valoresASomar.getBigDecimal_Zero(getNomeCampo("valor_ipi")) - valoresADiminuir.getBigDecimal_Zero(getNomeCampo("valor_ipi")));

		def vlrcontabil = ecc0101json.getBigDecimal_Zero("vlr_valorcontabil");
		def bcicms = ecc0101json.getBigDecimal_Zero(getNomeCampo("icm_base_calculo"));
		def isentas = ecc0101json.getBigDecimal_Zero(getNomeCampo("icm_isento"));
		def outras = ecc0101json.getBigDecimal_Zero(getNomeCampo("icm_outras"));
		def ipi = ecc0101json.getBigDecimal_Zero(getNomeCampo("valor_ipi"));

		Ecc01 ecc01 = ecc0101.getEcc0101ficha();
		if(vlrcontabil != null && vlrcontabil != 0.0) {
			if(ecc01.getEcc01modelo().equals(0)){ //Modelo B
				if(isentas == null) isentas = 0.0;
				if(ipi == null) ipi = 0.0;

				fator = isentas + ipi;
				
				ecc0101json.put(getNomeCampo("icm_variavel"), round( ( ecc01.getEcc01icms() / 60 ) , 2 ) );
				
			}else{ //Modelo D
				if(bcicms == null) bcicms = 0.0;
				if(outras == null) outras = 0.0;

				fator = bcicms + outras;

				def divisor = ecc01.getEcc01modelo().equals(1) ? 48 : 24;

				ecc0101json.put(getNomeCampo("icm_variavel"), round( ( ecc01.getEcc01icms() / divisor ) , 2 ) );
				
			}

			fator = fator / vlrcontabil;
			fator = round(fator, 6);

			if(fator > 0.999999) {
				fator = 1;
			}
		}

		ecc0101.setEcc0101fator(fator);
		
		//Cálculo do ICMS fixo do mês
	   def icmsFixo = ecc0101json.getBigDecimal_Zero(getNomeCampo("icm_variavel")) * fator;
	   icmsFixo = round(icmsFixo, 2);
		ecc0101.setEcc0101icms(icmsFixo);

		ecc0101.setEcc0101json(ecc0101json);
	}

	private TableMap buscarSomaDosValoresDoPeriodoPeloCFOPs(int mes, int ano, List<String> cfops) {
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(LocalDate.of(ano, mes, 1))

		String wherePeriodo = " and abb01data between :dataIni and :dataFim "
		String whereCfops   = " and aaj15codigo in (:cfops) "

		String sql = " select " +
				" sum(eaa0103totDoc) as vlr_valorcontabil, " +
				" sum(cast(eaa0103json->>'"+getNomeCampo("icm_base_calculo")+"' as float)) as "+getNomeCampo("icm_base_calculo")+", " +
				" sum(cast(eaa0103json->>'"+getNomeCampo("icm_isento")+"' as float)) as "+getNomeCampo("icm_isento")+", " +
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

	private List<String> obterCfops(Integer opc){
		String sql = " Select aaj15codigo from aaj15 where (aaj15camposcustom ->> 'ciap')::numeric = "+opc+" " + getSamWhere().getWherePadrao("and", Aaj15.class) + " order by aaj15codigo";
		return getAcessoAoBanco().obterListaDeString(sql);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTkifQ==