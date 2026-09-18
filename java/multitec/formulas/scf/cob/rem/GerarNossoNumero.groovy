package multitec.formulas.scf.cob.rem

import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abf01
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase

class GerarNossoNumero extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_NOSSO_NUMERO;
	}

	@Override
	public void executar() {
		Daa01 daa01 = get("daa01");
		Abf01 abf01 = get("abf01");
		Long ultimoNossoNumero = get("ultimoNossoNumero");
		Long nossoNumero = 0;
		
		/**
		 * NOSSO NÚMERO
		 */
		
		// Sequencial
		nossoNumero = ++ultimoNossoNumero;

		/*
		if(abf01.abf01codigo.equals("000001") || abf01.abf01codigo.equals("000003")){
			String nosNum = StringUtils.ajustString(ultimoNossoNumero, 8, '0', true);
			GregorianCalendar dt = new GregorianCalendar();
			String ano = new SimpleDateFormat("yy").format(dt.getTime());
			if(!nosNum.substring(0, 2).equals(ano)){
				Integer indGeraNosNum = abf01.abf01json.get("ind_gera_nos_num");
				if(indGeraNosNum == null)throw new ValidacaoException("Indicador de geração do nosso número não encontrado nos parâmetros do banco.");

				StringBuffer novo = new StringBuffer();
				novo.append(ano);
				novo.append(StringUtils.ajustString(indGeraNosNum == null ? 0 : indGeraNosNum, 1, '0', true));
				novo.append(StringUtils.ajustString(1, 5, '0', true));

				nossoNumero = Long.parseLong(novo.toString());
			}
		}
			
		// Numero + Parcela
		String parcela = StringUtils.ajustString(StringUtils.extractNumbers(daa01.daa01central.abb01parcela), 3, '0', true);

		if(abf01.abf01codigo.equals("000003") && abf01.abf01json.get("gerar_dv").equals(1) && daa01.daa01central.abb01num >= 99999999) {
			parcela = StringUtils.ajustString(StringUtils.extractNumbers(daa01.daa01central.abb01parcela), 2, '0', true);
		}
		if(abf01.abf01codigo.equals("000001")){
			parcela = daa01.daa01central.abb01parcela.equals("0") ? "01" : daa01.daa01central.abb01parcela;
			parcela = parcela.split("/")[0];
			parcela = StringUtils.ajustString(StringUtils.extractNumbers(parcela), 2, '0', true);
		}

		String numParc = daa01.daa01central.abb01num + parcela;
		nossoNumero = Long.parseLong(numParc);*/
		
		def dv = null;
		
		put("nossoNumero", nossoNumero);
		put("dv", dv);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDEifQ==