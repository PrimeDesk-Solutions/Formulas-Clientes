package multitec.baseDemo

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aae11
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils

class SRF_NFeInutilizacao extends FormulaBase {
	
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
		
		if(empresa.getAac10municipio() == null) {
			throw new ValidacaoException("Necessário informar o município da empresa ativa.");
		}else if(empresa.getAac10municipio().getAag0201ibge() == null) {
			throw new ValidacaoException("Necessário informar o código IBGE do estado referente ao município da empresa ativa.");
		}			
			
		/** ALTERA DADOS DO DOCUMENTO */
		eaa01.setEaa01nfeStat(1);
		LocalDate dtProdNFe = getAcessoAoBanco().buscarParametro("NFeDataProducao", "EA");
		Boolean isProducao = DateUtils.dateDiff(dtProdNFe, central.getAbb01data(), ChronoUnit.DAYS) >= 0;
		
		StringBuilder obs = new StringBuilder();
		obs.append(eaa01.getEaa01obsSAM() == null ? "" : eaa01.getEaa01obsSAM());
		obs.append(" NFe: XML de inutilização da NFe gerado por ");
		obs.append(obterUsuarioLogado().getAab10user());
		obs.append(" em ");
		obs.append(NFeUtils.formatarDataHora(MDate.dateTime(), NFeUtils.PATTERN_PT_BR) + ".");
		eaa01.setEaa01obsSAM(obs.toString());
		
		/** GERAÇÃO DO XML DE PEDIDO DE INUTILIZAÇÃO DA NFE */
		StringBuilder ID = new StringBuilder("");
		ID.append(StringUtils.ajustString(empresa.getAac10municipio().getAag0201ibge(), 2)); 		//Código da UF do solicitante
		ID.append(NFeUtils.formatarData(MDate.date(), "yy"));										//Ano de inutilização
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
		infInut.addNode("cUF", StringUtils.ajustString(empresa.getAac10municipio().getAag0201ibge(), 2), true);
		infInut.addNode("ano", NFeUtils.formatarData(MDate.date(), "yy"), true);
		infInut.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.getAac10ni()), 14), true);
		infInut.addNode("mod", "55", true);
		infInut.addNode("serie", central.getAbb01serie() == null ? 0 : central.getAbb01serie().length() <= 3 ? central.getAbb01serie() : central.getAbb01serie().substring(0, 3), true);
		infInut.addNode("nNFIni", central.getAbb01num(), true);
		infInut.addNode("nNFFin", central.getAbb01num(), true);
		Aae11 aae11 = eaa01.getEaa01cancMotivo() != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aae11", eaa01.getEaa01cancMotivo().getAae11id()) : null;
		infInut.addNode("xJust", aae11 != null ? aae11.getAae11descr() : null, true);
		
		/** Gera o XML */
		getSession().persist(eaa01);
		String dados = NFeUtils.gerarXML(inutNFe);
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==