package multitec.baseDemo

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abm01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SRF_DocumItem  extends FormulaBase {
	
	private Eaa0103 eaa0103;
	private String procInvoc;
	
	private Eaa01 eaa01;
	private Abe40 abe40;
	private Abe30 abe30;
	private Abe30 abe30Item;
	private Abm01 abm01;
		
	@Override
	public void executar() {
		eaa0103 = get("eaa0103");
		procInvoc = get("procInvoc");
		
		eaa01 = eaa0103.eaa0103doc;
		abe40 = eaa01.eaa01tp;
		abe30 = eaa01.eaa01cp;
		
		abm01 = eaa0103.eaa0103item;
		abe30Item = eaa0103.eaa0103cp;
		
		setarObterPrecoUnitarioTaxasComissaoItem();
		
		if(procInvoc == "CAS0240" || procInvoc == "CAS0242") return;
		
		//Calculando a quantidade de uso com base no fator de conversão de venda para uso
		def fatorVendaUso = 1;

		def sql = " SELECT abm13fcVU" +
			  " FROM Abm0101" +
			  " INNER JOIN Abm13 ON abm0101comercial = abm13id" +
			  " WHERE abm0101item = :abm01id " +
			  " AND abm0101empresa = :aac10id ";
		
		def tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abm01id", abm01.abm01id),
								 Parametro.criar("aac10id", getVariaveis().aac10.idValue));
		
		if(tm != null) {
			fatorVendaUso = tm.getBigDecimal_Zero("abm13fcVU");	
			if(fatorVendaUso == 0) fatorVendaUso = 1; 	
		}
		eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl * fatorVendaUso;

		//Unitário zerado, não calcula-se o Total da Mercadoria, provavél complemento de preço
		if(eaa0103.eaa0103unit != 0) {
			eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;
			eaa0103.eaa0103total = round(eaa0103.eaa0103total, 2);
		}
		//Igualando Total do Documento com o Total da Mercadoria				
		eaa0103.eaa0103totDoc = eaa0103.eaa0103total;
		eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);

		//Totalizar Financeiro sem considerar os itens de Retorno de Industrialização
		if(eaa0103.eaa0103retInd == 0) {
			eaa0103.eaa0103totFinanc = eaa0103.eaa0103total;
			eaa0103.eaa0103totFinanc = round(eaa0103.eaa0103totFinanc, 2);
		}
		
		//Calculando campos json de visualização: 1-Item e 2-Ambos
		TableMap mapJson = eaa0103.eaa0103json == null ? new TableMap() : eaa0103.eaa0103json;
		
		//ICMS
		def txIcms = 0.18;
		def icms = eaa0103.eaa0103total * txIcms
		icms = round(icms, 2);
		mapJson.put("icm_bc", eaa0103.eaa0103total);
		mapJson.put("icm_aliq", txIcms);
		mapJson.put("icm_icm", icms);
		
		//ICMS ST
		def txIcmsST = 0.12;
		def icmsST = eaa0103.eaa0103total * txIcmsST
		icmsST = round(icmsST, 2);
		mapJson.put("st_bc", eaa0103.eaa0103total);
		mapJson.put("st_aliq", txIcmsST);
		mapJson.put("st_icm", icmsST);
		
		//IPI
		def txIpi = 0.10;
		def ipi = eaa0103.eaa0103total * txIpi
		ipi = round(ipi, 2);
		mapJson.put("ipi_bc", eaa0103.eaa0103total);
		mapJson.put("ipi_aliq", txIpi);
		mapJson.put("ipi_ipi", ipi);

		//Custo de aquisição
		def custoAquisicao = eaa0103.eaa0103total - ipi
		mapJson.put("custo_aquisicao", custoAquisicao);

		//Retorno de Industrialização
		if(eaa0103.eaa0103retInd == 0) mapJson.put("ind_encomenda", eaa0103.eaa0103total);
		else if(eaa0103.eaa0103retInd == 1) mapJson.put("ind_retorno", eaa0103.eaa0103total);
		else if(eaa0103.eaa0103retInd == 2) mapJson.put("ind_devolucao", eaa0103.eaa0103total);
		else if(eaa0103.eaa0103retInd == 3) mapJson.put("ind_complpreco", eaa0103.eaa0103total);
				
		eaa0103.eaa0103json = mapJson;

		//Itens recebem a mesma taxa de comissão do documento
		eaa0103.eaa0103txComis0 = eaa01.eaa01txComis0;
		eaa0103.eaa0103txComis1 = eaa01.eaa01txComis1;
		eaa0103.eaa0103txComis2 = eaa01.eaa01txComis2;
		eaa0103.eaa0103txComis3 = eaa01.eaa01txComis3;
		eaa0103.eaa0103txComis4 = eaa01.eaa01txComis4;
		
		//*** FIM DO CÁLCULO DO ITEM ***
	}
	
	private void setarObterPrecoUnitarioTaxasComissaoItem() {
		if(abe40 == null) return;
		
		if(eaa0103.eaa0103unit != 0) return;
		
		//Verificando se a tabela de preço está vencida
		def sql = " SELECT abe40dtVcto" +
				   " FROM Abe40" +
				   " WHERE abe40id = :abe40id";
					  
		def abe40dtVcto = getAcessoAoBanco().obterDate(sql, Parametro.criar("abe40id", abe40.abe40id));
		
		if(abe40dtVcto != null) {
			if(MDate.date() > abe40dtVcto) {
				throw new ValidacaoException("Tabela de preços vencida.");
			}
		}
		
		//Buscando preço na tabela de preço por item, tabela, condição de pagamento (se informada no item ou no documento), qtde comercial, taxa de desconto (se informada nos campos livres json) 
		def abe30id = abe30Item != null ? abe30Item.abe30id : null;
		if(abe30id == null && abe30 != null) abe30id = abe30.abe30id;
			
		sql = " SELECT abe4001preco, abe4001txComis0, abe4001txComis1, abe4001txComis2, abe4001txComis3, abe4001txComis4" +
			  " FROM Abe4001 " +
			  " INNER JOIN Abe40 ON abe4001tab = abe40id" +
			  " WHERE abe4001item = :abm01id" +
			  " AND abe4001tab = :abe40id" +
			  (abe30id != null ? " AND abe4001cp = :abe30id" : "") +
			  " AND abe4001qtMax >= :qtde" +
			  " AND abe4001txDesc >= :txDesc" +
			  getSamWhere().getWherePadrao("AND", Abe40.class) +
			  " ORDER BY abe4001qtMax, abe4001txDesc";
		
		//Buscando taxa de desconto informada no item do documento em campo Json e verificando se este campo existe
		def txDesc = 0;
		TableMap mapJson = eaa0103.eaa0103json == null ? new TableMap() : eaa0103.eaa0103json;
		if(mapJson.containsKey("vlr_desc_tx")) {
			txDesc = mapJson.getBigDecimal("vlr_desc_tx");
			if(txDesc == null) txDesc = 0;
		}
		
		List<Parametro> parametros = new ArrayList<>();
		parametros.add(Parametro.criar("abm01id", abm01.abm01id));
		parametros.add(Parametro.criar("abe40id", abe40.abe40id));
		parametros.add(Parametro.criar("qtde", eaa0103.eaa0103qtComl));
		parametros.add(Parametro.criar("txDesc", txDesc));
		if(abe30id != null) parametros.add(Parametro.criar("abe30id", abe30id)); // <- Se não informada condição de pagamento no item ou no documento, a mesma não será considerada na busca
					 
		TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, parametros.toArray(new Parametro[parametros.size()]));
	
		def unit = 0;
		def txComis0 = 0;
		def txComis1 = 0;
		def txComis2 = 0;
		def txComis3 = 0;
		def txComis4 = 0;
		
		if(tm != null) {
			unit = tm.getBigDecimal("abe4001preco");
			txComis0 = tm.getBigDecimal("abe4001txComis0");
			txComis1 = tm.getBigDecimal("abe4001txComis1");
			txComis2 = tm.getBigDecimal("abe4001txComis2");
			txComis3 = tm.getBigDecimal("abe4001txComis3");
			txComis4 = tm.getBigDecimal("abe4001txComis4");
		}
		
		//Buscando taxas de comissão na configuração do item (Abm13)
		sql = " SELECT abm13txCom0, abm13txCom1, abm13txCom2, abm13txCom3, abm13txCom4" +
			  " FROM Abm0101" +
			  " INNER JOIN Abm13 ON abm0101comercial = abm13id" +
			  " WHERE abm0101item = :abm01id " +
			  " AND abm0101empresa = :aac10id ";
		
		tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abm01id", abm01.abm01id),
								 Parametro.criar("aac10id", getVariaveis().aac10.idValue));
		
		if(tm != null) {
			if(txComis0 == null || txComis0 == 0) txComis0 = tm.getBigDecimal("abm13txCom0");
			if(txComis1 == null || txComis1 == 0) txComis1 = tm.getBigDecimal("abm13txCom1");
			if(txComis2 == null || txComis2 == 0) txComis2 = tm.getBigDecimal("abm13txCom2");
			if(txComis3 == null || txComis3 == 0) txComis3 = tm.getBigDecimal("abm13txCom3");
			if(txComis4 == null || txComis4 == 0) txComis4 = tm.getBigDecimal("abm13txCom4");
		}
		
		if(unit == null) unit = 0;
		if(txComis0 == null) txComis0 = 0;
		if(txComis1 == null) txComis1 = 0;
		if(txComis2 == null) txComis2 = 0;
		if(txComis3 == null) txComis3 = 0;
		if(txComis4 == null) txComis4 = 0;
		
		eaa0103.eaa0103unit = unit;
		
		eaa0103.eaa0103txComis0 = txComis0;
		eaa0103.eaa0103txComis1 = txComis1;
		eaa0103.eaa0103txComis2 = txComis2;
		eaa0103.eaa0103txComis3 = txComis3;
		eaa0103.eaa0103txComis4 = txComis4;
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==