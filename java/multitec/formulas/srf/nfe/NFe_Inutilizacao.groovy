package multitec.formulas.srf.nfe

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aae11
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils

class NFe_Inutilizacao extends FormulaBase {
	
	private Eaa01 eaa01;
	
	private static String versaoXMLLayoutInutNFe = "4.00";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVOS_DE_NFE;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		
		Abb01 central = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", eaa01.getEaa01central().getAbb01id());
		
		Aac10 empresa = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());
			
		LocalDate dtProdNFe = getAcessoAoBanco().buscarParametro("NFeDataProducao", "EA");
		boolean isProducao = dtProdNFe == null ? false : DateUtils.dateDiff(dtProdNFe, central.getAbb01data(), ChronoUnit.DAYS) >= 0;
		
		/** GERAÇÃO DO XML DE PEDIDO DE INUTILIZAÇÃO DA NFE */
		StringBuilder ID = new StringBuilder("");
		ID.append(StringUtils.ajustString(empresa.aac10municipio.aag0201uf.aag02ibge, 2)); 		//Código da UF do solicitante
		ID.append(NFeUtils.formatarData(MDate.date(), "yy"));									//Ano de inutilização
		ID.append(StringUtils.ajustString(StringUtils.extractNumbers(empresa.getAac10ni()), 14)); 	//CNPJ do emitente
		ID.append("55"); 																			//Modelo da NFe
		ID.append(NFeUtils.tratarSerie(central.getAbb01serie())); 									//Série da NFe
		ID.append(StringUtils.ajustString(central.getAbb01num(), 9));								//Número inicial da NFe
		ID.append(StringUtils.ajustString(central.getAbb01num(), 9));								//Número final da NFe
			
		/** inutNFe - Nota Fiscal Eletrônica */
		ElementXml inutNFe = NFeUtils.criarElementXmlNFe("http://www.portalfiscal.inf.br/nfe", "inutNFe"); 
		inutNFe.setAttribute("versao", versaoXMLLayoutInutNFe);
		
		/** infCanc - Dados do pedido */
		ElementXml infInut = inutNFe.addNode("infInut");
		infInut.setAttribute("Id", "ID" + ID.toString());
		infInut.addNode("tpAmb", isProducao ? 1 : 2, true);
		infInut.addNode("xServ", "INUTILIZAR", true);
		infInut.addNode("cUF", StringUtils.ajustString(empresa.aac10municipio.aag0201uf.aag02ibge, 2), true);
		infInut.addNode("ano", NFeUtils.formatarData(MDate.date(), "yy"), true);
		infInut.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.getAac10ni()), 14), true);
		infInut.addNode("mod", "55", true);
		infInut.addNode("serie", central.getAbb01serie() == null ? 0 : central.getAbb01serie().length() <= 3 ? central.getAbb01serie() : central.getAbb01serie().substring(0, 3), true);
		infInut.addNode("nNFIni", central.getAbb01num(), true);
		infInut.addNode("nNFFin", central.getAbb01num(), true);
		Aae11 aae11 = eaa01.getEaa01cancMotivo() != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aae11", eaa01.getEaa01cancMotivo().getAae11id()) : null;
		infInut.addNode("xJust", aae11 != null ? aae11.getAae11descr() : null, true);
		
		/** Gera o XML */
		String dados = NFeUtils.gerarXML(inutNFe);
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==