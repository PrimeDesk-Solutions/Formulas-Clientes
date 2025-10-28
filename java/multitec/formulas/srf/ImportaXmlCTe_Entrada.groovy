package multitec.formulas.srf

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aag0201
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase

class ImportaXmlCTe_Entrada extends FormulaBase {
	
	private Eaa01 eaa01;
	private Eaa0103 eaa0103;
	private ElementXml elementXmlCte;
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		elementXmlCte = get("elementXmlCte");
		
		selecionarAlinhamento("0013");
		
		eaa0103 = obterItemDoDocumento(eaa01);
		if(eaa0103 == null) return;
		
		TableMap eaa0103json = eaa0103.eaa0103json;
		
		if(!elementXmlCte.getNodeName().equals("cteProc")) return;
		
		ElementXml elementCTe = elementXmlCte.getChildNode("CTe");
		if(elementCTe == null) return;
		
		ElementXml elementinfCte = elementCTe.getChildNode("infCte");
		if(elementinfCte == null) return;
		
		setarMunicipiosNosEnderecosSaidaEntrega(elementinfCte);
		
		//Valores da Prestação de Serviço
		ElementXml elementvPrest = elementinfCte.getChildNode("vPrest");
		
		eaa0103.eaa0103qtComl = 1;
		eaa0103.eaa0103qtUso = 1;
		
		def vRec = obterValorXml(elementvPrest, "vRec", 2);
		eaa0103.eaa0103unit = vRec;
		eaa0103.eaa0103total = vRec;
		eaa0103.eaa0103totDoc = vRec;
		eaa0103.eaa0103totFinanc = vRec;
		
		setarValorJson(eaa0103json, getCampo("188", "vTPrest"), obterValorXml(elementvPrest, "vTPrest", 2));
		setarValorJson(eaa0103json, getCampo("189", "vRec"), vRec);
		
		//Impostos
		ElementXml elementimp = elementinfCte.getChildNode("imp");
		
		ElementXml elementICMS = elementimp.getChildNode("ICMS");
		
		ElementXml elementICMS00 = elementICMS.getChildNode("ICMS00");
		if(elementICMS00 != null) {
			setarValorJson(eaa0103json, getCampo("197", "vBC"), obterValorXml(elementICMS00, "vBC", 2));
			setarValorJson(eaa0103json, getCampo("198", "pICMS"), obterValorXml(elementICMS00, "pICMS", 2));
			setarValorJson(eaa0103json, getCampo("199", "vICMS"), obterValorXml(elementICMS00, "vICMS", 2));
		}
		
		ElementXml elementICMS20 = elementICMS.getChildNode("ICMS20");
		if(elementICMS20 != null) {
			setarValorJson(eaa0103json, getCampo("202", "pRedBC"), obterValorXml(elementICMS20, "pRedBC", 2));
			setarValorJson(eaa0103json, getCampo("203", "vBC"), obterValorXml(elementICMS20, "vBC", 2));
			setarValorJson(eaa0103json, getCampo("204", "pICMS"), obterValorXml(elementICMS20, "pICMS", 2));
			setarValorJson(eaa0103json, getCampo("205", "vICMS"), obterValorXml(elementICMS20, "vICMS", 2));
		}
		
		ElementXml elementICMS60 = elementICMS.getChildNode("ICMS60");
		if(elementICMS60 != null) {
			setarValorJson(eaa0103json, getCampo("210", "vBCSTRet"), obterValorXml(elementICMS60, "vBCSTRet", 2));
			setarValorJson(eaa0103json, getCampo("211", "vICMSSTRet"), obterValorXml(elementICMS60, "vICMSSTRet", 2));
			setarValorJson(eaa0103json, getCampo("212", "pICMSSTRet"), obterValorXml(elementICMS60, "pICMSSTRet", 2));
			setarValorJson(eaa0103json, getCampo("213", "vCred"), obterValorXml(elementICMS60, "vCred", 2));
		}
		
		ElementXml elementICMS90 = elementICMS.getChildNode("ICMS90");
		if(elementICMS90 != null) {
			setarValorJson(eaa0103json, getCampo("216", "pRedBC"), obterValorXml(elementICMS90, "pRedBC", 2));
			setarValorJson(eaa0103json, getCampo("217", "vBC"), obterValorXml(elementICMS90, "vBC", 2));
			setarValorJson(eaa0103json, getCampo("218", "pICMS"), obterValorXml(elementICMS90, "pICMS", 2));
			setarValorJson(eaa0103json, getCampo("219", "vICMS"), obterValorXml(elementICMS90, "vICMS", 2));
			setarValorJson(eaa0103json, getCampo("220", "vCred"), obterValorXml(elementICMS90, "vCred", 2));
		}
		
		ElementXml elementICMSOutraUF = elementICMS.getChildNode("ICMSOutraUF");
		if(elementICMSOutraUF != null) {
			setarValorJson(eaa0103json, getCampo("223", "pRedBCOutraUF"), obterValorXml(elementICMSOutraUF, "pRedBCOutraUF", 2));
			setarValorJson(eaa0103json, getCampo("224", "vBCOutraUF"), obterValorXml(elementICMSOutraUF, "vBCOutraUF", 2));
			setarValorJson(eaa0103json, getCampo("225", "pICMSOutraUF"), obterValorXml(elementICMSOutraUF, "pICMSOutraUF", 2));
			setarValorJson(eaa0103json, getCampo("226", "vICMSOutraUF"), obterValorXml(elementICMSOutraUF, "vICMSOutraUF", 2));
		}
		
		setarValorJson(eaa0103json, getCampo("230", "vTotTrib"), obterValorXml(elementvPrest, "vTotTrib", 2));
		
		String infAdFisco = elementvPrest.getChildValue("infAdFisco");
		if(infAdFisco != null) {
			String obsFisco = eaa01.getEaa01obsFisco();
			eaa01.setEaa01obsFisco(obsFisco == null ? infAdFisco : (obsFisco + "\n" + infAdFisco));
		}
		
		ElementXml elementinfCTeNorm = elementinfCte.getChildNode("infCTeNorm");
		
		if(elementinfCTeNorm != null) {
			ElementXml elementinfCarga = elementinfCTeNorm.getChildNode("infCarga");
			if(elementinfCarga != null) {
				setarValorJson(eaa0103json, getCampo("242", "vCarga"), obterValorXml(elementinfCarga, "vCarga", 2));
				setarValorJson(eaa0103json, getCampo("249", "vCargaAverb"), obterValorXml(elementinfCarga, "vCargaAverb", 2));
			}
		}
	}
	
	private BigDecimal obterValorXml(ElementXml element, String nomeTagXml, int casasDecimais) {
		String conteudo = element.getChildValue(nomeTagXml);
		BigDecimal valor = conteudo == null ? BigDecimal.ZERO : new BigDecimal(conteudo);
		return round(valor, casasDecimais);
	}
	
	private void setarValorJson(TableMap json, String nomeCampo, BigDecimal valor) {
		if(json == null) return;
		if(nomeCampo == null) return;
		if(!json.containsKey(nomeCampo)) return;
		json.put(nomeCampo, valor);
	}
	
	private Eaa0103 obterItemDoDocumento(Eaa01 eaa01) {
		if(Utils.isEmpty(eaa01.getEaa0103s())) return null;
		
		for(Eaa0103 eaa0103 : eaa01.getEaa0103s()) {
			return eaa0103;
		}
	}
	
	private void setarMunicipiosNosEnderecosSaidaEntrega(ElementXml elementinfCte) {
		ElementXml elementide = elementinfCte.getChildNode("ide");
		if(elementide == null) return null;
		
		//Removendo endereços de saída e entrega
		List<Eaa0101> eaa0101sRemover = new ArrayList<>();
		if(eaa01.eaa0101s != null && eaa01.eaa0101s.size() > 0) {
			for(Eaa0101 eaa0101 : eaa01.eaa0101s) {
				if(eaa0101.getEaa0101saida_Zero() == 1 || eaa0101.getEaa0101entrega_Zero() == 1) {
					eaa0101sRemover.add(eaa0101);
				}
			}
		}
		if(eaa0101sRemover != null && eaa0101sRemover.size() > 0) {
			eaa01.eaa0101s.removeAll(eaa0101sRemover);
		}
		
		//Município de saída
		String cMunIni = elementide.getChildValue("cMunIni");
		Aag0201 aag0201Saida = buscarMunicipioPeloCodigoIBGE(cMunIni);
		if(aag0201Saida != null) {
			Eaa0101 eaa0101Saida = new Eaa0101();
			eaa0101Saida.eaa0101municipio = aag0201Saida;
			eaa0101Saida.eaa0101ti = 0;
			eaa0101Saida.eaa0101saida = 1;
			eaa0101Saida.eaa0101principal = 0;
			eaa0101Saida.eaa0101entrega = 0;
			eaa0101Saida.eaa0101cobranca = 0;
			eaa0101Saida.eaa0101outros = 0;
			eaa01.addToEaa0101s(eaa0101Saida);
		}
		
		//Município de entrega
		String cMunFim = elementide.getChildValue("cMunFim");
		Aag0201 aag0201Entrega = buscarMunicipioPeloCodigoIBGE(cMunFim);
		if(aag0201Entrega != null) {
			Eaa0101 eaa0101Entrega = new Eaa0101();
			eaa0101Entrega.eaa0101municipio = aag0201Entrega;
			eaa0101Entrega.eaa0101ti = 0;
			eaa0101Entrega.eaa0101saida = 0;
			eaa0101Entrega.eaa0101principal = 0;
			eaa0101Entrega.eaa0101entrega = 1;
			eaa0101Entrega.eaa0101cobranca = 0;
			eaa0101Entrega.eaa0101outros = 0;
			eaa01.addToEaa0101s(eaa0101Entrega);
		}
	}
	
	private Aag0201 buscarMunicipioPeloCodigoIBGE(String codigoIBGE) {
		return getSession().createCriteria(Aag0201.class)
						   .addJoin(Joins.fetch("aag0201uf"))
						   .addWhere(Criterions.eq("aag0201ibge", codigoIBGE))
						   .get();
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.IMPORTAR_ARQUIVOS_XML_DE_CTE;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzYifQ==