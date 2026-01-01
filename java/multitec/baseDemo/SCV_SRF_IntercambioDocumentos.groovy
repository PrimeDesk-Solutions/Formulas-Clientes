package multitec.baseDemo

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.srf.FormulaSRFCalculoDocumentoDto
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abm01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase
import sam.server.srf.service.SRFService

public class SCV_SRF_IntercambioDocumentos extends FormulaBase {
	
	private Long eaa01idOrigem;
	private Long abd01idDestino;
	private TableMap json;
		
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_INTERCAMBIO_DE_DOCUMENTOS;
	}
	
	@Override
	public void executar() {
		eaa01idOrigem = get("eaa01idOrigem");
		abd01idDestino = get("abd01idDestino");
		json = get("json");
		
		SRFService srfService = instanciarService(SRFService.class);
		
		Eaa01 eaa01Origem = buscarDocumento(eaa01idOrigem);
		if(eaa01Origem == null) interromper("Documento origem não encontrado.");
		
		Abe01 abe01Origem = buscarEntidadePeloNISemPontuacao(eaa01Origem.eaa01eg.aac10ni);
		if(abe01Origem == null) interromper("Entidade origem não encontrada a partir do número de inscrição da empresa geradora do documento origem.")
		
		Eaa01 eaa01 = srfService.comporDocumentoPadrao(abe01Origem.abe01id, abd01idDestino, null);
		
		Abb01 abb01 = eaa01.eaa01central;
		Aah01 aah01 = abb01.abb01tipo;
		if(aah01.aah01numeracao != 0) {
			abb01.abb01num = eaa01Origem.eaa01central.abb01num;
			abb01.abb01data = eaa01Origem.eaa01central.abb01data;
		}
		abb01.abb01operAutor = "SRF1007";
		
		List<Eaa0103> eaa0103sOrigem = buscarItensDocumento(eaa01idOrigem);
		if(eaa0103sOrigem != null && eaa0103sOrigem.size() > 0) {
			for(Eaa0103 eaa0103Origem : eaa0103sOrigem) {
				
				Abm01 abm01 = buscarItem(eaa0103Origem.eaa0103tipo, eaa0103Origem.eaa0103codigo);
				if(abm01 == null) interromper("Item " + eaa0103Origem.eaa0103codigo + " não encontrado.");
				
				Eaa0103 eaa0103 = srfService.comporItemDoDocumentoPadrao(eaa01, abm01.abm01id);
				
				eaa0103.eaa0103seq = eaa0103Origem.eaa0103seq;
				eaa0103.eaa0103qtComl = eaa0103Origem.eaa0103qtComl;
				eaa0103.eaa0103unit = eaa0103Origem.eaa0103unit;
				
				eaa01.addToEaa0103s(eaa0103);
			}
		}
		
		srfService.executarFormulaSRFCalculoDocumento(new FormulaSRFCalculoDocumentoDto(eaa01.eaa01pcd.abd01frmItem, eaa01.eaa01pcd.abd01frmDoc, eaa01, null, "SRF1007", true));
		srfService.comporFinanceiroContabilidadeSeVazios(eaa01);
		
		put("eaa01", eaa01);
	}
	
	private Eaa01 buscarDocumento(Long eaa01id) {
		return getSession().createCriteria(Eaa01.class)
						   .addJoin(Joins.part("eaa01eg"))
						   .addJoin(Joins.part("eaa01central"))
						   .addFields("eaa01id")
						   .addFields("eaa01eg, aac10id, aac10ni")
						   .addFields("eaa01central, abb01id, abb01num, abb01data")
						   .addWhere(Criterions.eq("eaa01id", eaa01id))
						   .get();
	}
	
	private Abe01 buscarEntidadePeloNISemPontuacao(String ni) {
		return getSession().createCriteria(Abe01.class)
						   .addFields("abe01id")
						   .addWhere(Criterions.eq(Fields.regexReplace("abe01ni", "'[^0-9]'", "''"), ni))
						   .addWhere(Criterions.where(obterWherePadrao("Abe01", "")))
						   .setMaxResults(1)
						   .get();
	}
	
	private List<Eaa0103> buscarItensDocumento(Long eaa01id) {
		return getSession().createCriteria(Eaa0103.class)
						   .addFields("eaa0103id, eaa0103seq, eaa0103tipo, eaa0103codigo, eaa0103qtComl, eaa0103unit")
						   .addWhere(Criterions.eq("eaa0103doc", eaa01id))
						   .setOrder("eaa0103seq")
						   .getList();
	}
	
	private Abm01 buscarItem(Integer tipo, String codigo) {
		return getSession().createCriteria(Abm01.class)
						   .addFields("abm01id")
						   .addWhere(Criterions.eq("abm01tipo", tipo))
						   .addWhere(Criterions.eq("abm01codigo", codigo))
						   .addWhere(getSamWhere().getCritPadrao(Abm01.class))
						   .get();
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEwIn0=