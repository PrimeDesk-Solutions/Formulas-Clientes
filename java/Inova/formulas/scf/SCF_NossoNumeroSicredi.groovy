/*
AUTOR: NAGYLA
ULTIMA ALTERACAO: 23/06/2026    11:33
 */

package Inova.formulas.scf;

import sam.model.entities.ab.Abf01
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

import java.time.LocalDate

public class SCF_NossoNumeroSicredi extends FormulaBase{

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCF_NOSSO_NUMERO; 
	}

	@Override 
	public void executar() {
        Daa01 daa01 = get("daa01");
        Abf01 abf01 = get("abf01");
        Long ultimoNossoNumero = getLong("ultimoNossoNumero");
        Long nossoNumero = 0;

        Long ultNossoNum = ultimoNossoNumero

        if(ultimoNossoNumero.toString().length() >= 5){
            ultNossoNum = ultimoNossoNumero.toString().substring(ultimoNossoNumero.toString().length() - 5).toLong()
        }

        nossoNumero = ++ultNossoNum;
        def dv = null;

        String ano = LocalDate.now().getYear().toString().substring(LocalDate.now().getYear().toString().length() - 2);
        int byteGeracao = 2;
        String numeroFormatado = String.format("%05d", nossoNumero);

        StringBuilder nossoNum = new StringBuilder();
        nossoNum.append(ano);
        nossoNum.append(byteGeracao);
        nossoNum.append(numeroFormatado);

        nossoNumero = nossoNum.toLong();

        String codCooperativa = abf01.abf01agencia;
        String codPostoBeneficiario =  abf01.getAbf01json().getString("posto_cooperativa");
//        String beneficiario = abf01.abf01conta
        String beneficiario = abf01.abf01json.getString("cod_identificador");

        StringBuilder valorCalcularDV = new StringBuilder();
        valorCalcularDV.append(codCooperativa);
        valorCalcularDV.append(codPostoBeneficiario);
        valorCalcularDV.append(beneficiario);
        valorCalcularDV.append(ano);
        valorCalcularDV.append(byteGeracao);
        valorCalcularDV.append(numeroFormatado)

        dv = calcularModulo11(valorCalcularDV.toString());

        put("nossoNumero", nossoNumero);
        put("dv", dv);
	}

   int calcularModulo11(String codigo) {
        int soma = 0;
        int peso = 2;

        for (int i = codigo.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(codigo.charAt(i));
            soma += digito * peso;

            peso++;
            if (peso > 9) {
                peso = 2;
            }
        }

        int resto = soma % 11;

        int dv = 11 - resto;

        if (dv == 10 || dv == 11) {
            dv = 0;
        }

        return dv;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDEifQ==