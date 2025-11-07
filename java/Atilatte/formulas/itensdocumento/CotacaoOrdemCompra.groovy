package Atilatte.formulas.itensdocumento;

import sam.model.entities.ab.Abm13;

import sam.model.entities.ab.Abm0101;

import sam.model.entities.aa.Aac10;

import sam.model.entities.ab.Abm01;

import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multiorm.criteria.criterion.Criterions
import sam.dicdados.FormulaTipo;
import sam.model.entities.cb.Cbb11
import sam.model.entities.cb.Cbb1101;
import sam.model.entities.ab.Abm1301;

import sam.server.samdev.formula.FormulaBase;

public class CotacaoOrdemCompra extends FormulaBase {

	private Cbb11 cbb11;
	private Cbb1101 cbb1101;
	private Abm1301 abm1301;
	private Abm01 	 abm01;
	private Aac10 aac10;
	private Abm0101 abm0101;
	private Abm13 abm13;

	@Override
	public void executar() {
		cbb1101 = (Cbb1101)get("cbb1101");
		if(cbb1101 == null) throw new ValidacaoException("Necessário informar o item da cotação.");
		abm01 = cbb1101.cbb1101item;
		cbb11 = cbb1101.cbb1101cot;
		
		//Empresa
		aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);
		

		//Configurações do item, por empresa
		abm0101 = abm01 != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;
		//throw new ValidacaoException(abm0101.abm0101id) 

		//Dados Comerciais do item
		abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

		//Fatores de Conv. da Unid de Compra para Estoque
		abm1301 = abm13 == null ? null : cbb1101.cbb1101umc == null ? null : getSession().get(Abm1301.class, Criterions.where("abm1301cc = " + abm13.abm13id + " AND abm1301umc = " + cbb1101.cbb1101umc.aam06id));
		
		cbb1101.cbb1101qtU = (cbb1101.cbb1101qtC * abm1301.abm1301fcCU)
		cbb1101.cbb1101total =  cbb1101.cbb1101qtU * cbb1101.cbb1101unit;
		cbb1101.cbb1101total = round(cbb1101.cbb1101total,2)
		

		//Preço (json) = Total
		TableMap mapJson = cbb1101.cbb1101json == null ? new TableMap() : cbb1101.cbb1101json;
		mapJson.put("total", total);
		cbb1101.cbb1101json = mapJson;
		
	}
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_COTACOES;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzcifQ==