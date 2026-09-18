package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abd02
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe02
import sam.model.entities.ab.Abe05
import sam.model.entities.ab.Abe40
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SRF_Documento  extends FormulaBase {
	
	private Eaa01 eaa01;
	private String procInvoc;
	
	private Abb01 abb01;
	private Abe01 abe01;
	private Abe40 abe40;
	
	private Abe01 abe01rep0;
	private Abe01 abe01rep1;
	private Abe01 abe01rep2;
	private Abe01 abe01rep3;
	private Abe01 abe01rep4;
	private Abd01 abd01;
	private Abd02 abd02;
			
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		procInvoc = get("procInvoc");
		
		abb01 = eaa01.eaa01central;
		if(abb01 == null) return;
		
		//PCD - Parâmetro de Cálculo de Documentos
		abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);
		
		//PCD - Fiscal
		if (abd01 != null && abd01.abd01ceFiscais != null) {
			abd02 = getSession().get(Abd02.class, abd01.abd01ceFiscais.abd02id);
		}
		
		if (abd01 != null) {
			eaa01.eaa01obsGerais = abd01.abd01obsGerais;
			eaa01.eaa01obsUsoInt = abd01.abd01obsUsoInt;
		}
		
		if (abd02 != null) {
			eaa01.eaa01obsFisco = abd02.abd02obsFisco;
			eaa01.eaa01obsContrib = abd02.abd02obsContrib;
		}
		
		
		abe01 = abb01.abb01ent;
				
		abe40 = eaa01.eaa01tp;
		
		abe01rep0 = eaa01.eaa01rep0;
		abe01rep1 = eaa01.eaa01rep1;
		abe01rep2 = eaa01.eaa01rep2;
		abe01rep3 = eaa01.eaa01rep3;
		abe01rep4 = eaa01.eaa01rep4;
		
		setarObterTaxasComissaoDocumento();
		
		// Calculando campos json de visualização 0-Documento
		TableMap mapJson = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;
		
		// Cashback: Plano de fidelidade creditado na emissão do pedido
		if(eaa01.eaa01pcd != null && eaa01.eaa01pcd.abd01codigo == "10001") {
			def fidelidade = 0;
			fidelidade = eaa01.eaa01totDoc * 0.01;
			fidelidade = round(fidelidade, 2);
			mapJson.put("vlr_fidelidade", fidelidade);
		}
		
		// Cashback: Crédito lançado pela devolução de venda na emissão da NFe de Entrada
		if(eaa01.eaa01pcd != null && eaa01.eaa01pcd.abd01codigo == "11100") {
			def credito = mapJson.getBigDecimal("vlr_credito");
			credito = credito == null ? null : eaa01.eaa01totDoc;
			mapJson.put("vlr_credito", credito);
		}
		
		eaa01.eaa01json = mapJson;
	}
	
	private void setarObterTaxasComissaoDocumento() {
		if(eaa01.eaa01esMov == 0) return;
		
		if(abe01 == null) return;
		
		def txComis0 = eaa01.eaa01txComis0;
		def txComis1 = eaa01.eaa01txComis1;
		def txComis2 = eaa01.eaa01txComis2;
		def txComis3 = eaa01.eaa01txComis3;
		def txComis4 = eaa01.eaa01txComis4;
		
		//Obtendo taxas fixadas na entidade (cliente)
		def sql = " SELECT abe02txComis0, abe02txComis1, abe02txComis2, abe02txComis3, abe02txComis4" +
				  " FROM Abe01" +
				  " INNER JOIN Abe02 ON abe02ent = abe01id" +
				  " WHERE abe01cli = 1" +
				  " AND abe01id = :abe01id" +
				  getSamWhere().getWherePadrao("AND", Abe02.class);

		TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abe01id", abe01.abe01id));
		
		if(tm != null) {
			if(txComis0 == null || txComis0 == 0) txComis0 = tm.getBigDecimal("abe02txComis0");
			if(txComis1 == null || txComis1 == 0) txComis1 = tm.getBigDecimal("abe02txComis1");
			if(txComis2 == null || txComis2 == 0) txComis2 = tm.getBigDecimal("abe02txComis2");
			if(txComis3 == null || txComis3 == 0) txComis3 = tm.getBigDecimal("abe02txComis3");
			if(txComis4 == null || txComis4 == 0) txComis4 = tm.getBigDecimal("abe02txComis4");
		}

		//Obtendo taxas fixadas na tabela de preço
		if(abe40 != null) {
			sql = " SELECT abe40txComis0, abe40txComis1, abe40txComis2, abe40txComis3, abe40txComis4" +
				  " FROM Abe40" +
				  " WHERE abe40id = :abe40id";
	
			tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abe40id", abe40.abe40id));
			
			if(tm != null) {
				if(txComis0 == null || txComis0 == 0) txComis0 = tm.getBigDecimal("abe40txComis0");
				if(txComis1 == null || txComis1 == 0) txComis1 = tm.getBigDecimal("abe40txComis1");
				if(txComis2 == null || txComis2 == 0) txComis2 = tm.getBigDecimal("abe40txComis2");
				if(txComis3 == null || txComis3 == 0) txComis3 = tm.getBigDecimal("abe40txComis3");
				if(txComis4 == null || txComis4 == 0) txComis4 = tm.getBigDecimal("abe40txComis4");
			}
		}

		//Obtendo taxas fixadas em cada representante do documento
		if(txComis0 == null || txComis0 == 0) txComis0 = obterTaxaDoRepresentante(abe01rep0);
		if(txComis1 == null || txComis1 == 0) txComis1 = obterTaxaDoRepresentante(abe01rep1);
		if(txComis2 == null || txComis2 == 0) txComis2 = obterTaxaDoRepresentante(abe01rep2);
		if(txComis3 == null || txComis3 == 0) txComis3 = obterTaxaDoRepresentante(abe01rep3);
		if(txComis4 == null || txComis4 == 0) txComis4 = obterTaxaDoRepresentante(abe01rep4);
		
		if(txComis0 == null) txComis0 = 0;
		if(txComis1 == null) txComis1 = 0;
		if(txComis2 == null) txComis2 = 0;
		if(txComis3 == null) txComis3 = 0;
		if(txComis4 == null) txComis4 = 0;
		
		eaa01.eaa01txComis0 = txComis0;
		eaa01.eaa01txComis1 = txComis1;
		eaa01.eaa01txComis2 = txComis2;
		eaa01.eaa01txComis3 = txComis3;
		eaa01.eaa01txComis4 = txComis4;
	}
	
	private def obterTaxaDoRepresentante(Abe01 abe01rep) {
		if(abe01rep == null) return 0;
		
		def sql = " SELECT abe05taxa" +
				  " FROM Abe01" +
				  " INNER JOIN Abe05 ON abe05ent = abe01id" +
				  " WHERE abe01rep = 1" +
				  " AND abe01id = :abe01id" +
				  getSamWhere().getWherePadrao("AND", Abe05.class);
				  
		def txComis = getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("abe01id", abe01rep.abe01id));
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_DOCUMENTOS;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjAifQ==