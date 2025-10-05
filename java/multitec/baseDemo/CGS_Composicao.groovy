package multitec.baseDemo;

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abp2001
import sam.model.entities.ab.Abp20011
import sam.server.samdev.formula.FormulaBase

class CGS_Composicao extends FormulaBase {
	
	private Abp2001 abp2001;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.COMPOSICAO_DE_PRODUTOS;
	}
	
	@Override
	public void executar() {
		abp2001 = (Abp2001)get("abp2001");
		
		//Obtendo a soma total das quantidades dos itens da composição
		def totalQtdes = 0.0;
		for(Abp20011 abp20011 : abp2001.abp20011s) {
			totalQtdes = totalQtdes + abp20011.abp20011qt_Zero;
		}
		
		//Percorrendo cada item da composição
		int i = 1;
		def somaPercentual = 0.0;
		for(Abp20011 abp20011 : abp2001.abp20011s) {
			
			//Calculando o percentual do item em relação a soma total
			def percentual = 0.0;
			if(abp20011.abp20011qt_Zero > 0) {
				if(i < abp2001.abp20011s.size()) {
					percentual = (abp20011.abp20011qt_Zero * 100) / totalQtdes;
					percentual = round(percentual, 2);
				}else {
					//Último item faz por diferença
					percentual = 100 - somaPercentual;
				}
			}
			somaPercentual = somaPercentual + percentual;
			
			//Caso o campo json esteja null (vazio), criá-lo
			TableMap json = abp20011.abp20011json;
			if(json == null) json = new TableMap();
			
			//Atribuindo o percentual calculado ao campo percentual do json
			json.put("percentual", percentual);
			
			//Cálculo simples de valor
			def valor = abp20011.abp20011qt_Zero * percentual;
			valor = round(valor, 2);
			json.put("valor", valor);
			
			//Atualizando campos json
			abp20011.abp20011json = json;
			
			i++;
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzMifQ==