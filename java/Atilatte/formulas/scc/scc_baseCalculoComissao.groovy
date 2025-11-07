package Atilatte.formulas.scc;

import sam.model.entities.aa.Aaj03;
import sam.model.entities.ab.Abb01;
import sam.model.entities.dc.Dcb01
import sam.model.entities.ea.Eaa01;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.ValidacaoException
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.collections.TableMap;

public class Documento extends FormulaBase{

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCC_DOCUMENTOS; 
	}

	@Override 
	public void executar() {
		//Tabela de Comissões
		Dcb01 dcb01 = get("dcb01");
		
		//Central de Documentos
		Abb01 abb01 = dcb01.dcb01central
		
		//Documentos 
		Eaa01 eaa01 = abb01.abb01id != null ? getSession().get(Eaa01.class, Criterions.where("eaa01central = " + abb01.abb01id )) : null;

		//Motivos Devoluções 
		Aaj03 aaj03 = eaa01.eaa01id != null ? getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id) : null;

		//Campos Livres 
		TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap()

		def dcb01json = dcb01.dcb01json;
		def desconto = dcb01json.getBigDecimal("descontoq");
		desconto = desconto == null ? 0 : desconto;

		//Base de Calculo de Comissão do Documento
		def bcComissao = jsonEaa01.getBigDecimal_Zero("bc_comissao");

		String sitDoc = aaj03.aaj03codigo;

		if(sitDoc == "10" || sitDoc == "11"){
			bcComissao *= -1;
			dcb01.dcb01bc = bcComissao;
		}else{
			dcb01.dcb01bc = bcComissao;
		}
		
		//dcb01.dcb01bc = dcb01.dcb01central.abb01valor;
		dcb01.dcb01valor = (dcb01.dcb01bc * dcb01.dcb01taxa) / 100;
		dcb01.dcb01valor = round(dcb01.dcb01valor, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzgifQ==