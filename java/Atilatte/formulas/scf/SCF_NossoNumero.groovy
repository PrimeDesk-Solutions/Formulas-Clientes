package Atilatte.formulas.scf;

import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abf01
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.ValidacaoException

class SCF_NossoNumero extends FormulaBase {

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

	
		
		//Nosso número sequencial
		nossoNumero = ++ultimoNossoNumero;
		//Número da carteira
		String carteira = abf01.abf01json.getString("carteira");

		
		String num = nossoNumero.toString();
		//Preencher o nosso último número com 11 casas decimais
		while(num.length() < 11){
			 num = "0" + num;
			 
		}

		//Concatena o nosso número com o número da carteira
		String numCompleto = carteira + num;

		
		//Digito verificador
		Integer dv = 0;
		
		if(abf01.abf01numero == "237") {
			dv = modulo11(numCompleto.toString())
		}else if(abf01.abf01numero == "341") {
			throw new ValidacaoException("341")	
			dv = modulo10(numCompleto.toString())
		}

		if(dv == 1){
			put("dv","P");
		}else if(dv == 0){
			put("dv","0");
		}else{
			dv = 11 - dv;
			put("dv",dv);
		}

		put("nossoNumero", nossoNumero);
		
	}

	private Integer modulo11(String numero){
		
		//Sequencia de indices para cálculo do digito verificador
		String indices = "2765432765432";
		Integer soma = 0;
		Integer digito = 0;

		//Calculo do Dígito verificador
		for(int i = 0; i < numero.length(); i++){
			soma += Integer.parseInt(String.valueOf(numero.charAt(i))) * Integer.parseInt(String.valueOf(indices.charAt(i)));
			//soma = Integer.parseInt(numero.charAt(i)) * Integer.parseInt(indices.charAt(i)); Opção 2
			//soma = Integer.valueOf(numero.charAt(i)) * Integer.parseInt(indices.charAt(i)); Opção 3
		}

		
		digito = soma % 11;
		return digito;

		
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDEifQ==