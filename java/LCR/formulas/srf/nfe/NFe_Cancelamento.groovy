package LCR.formulas.srf.nfe

import java.time.LocalDate
import java.time.LocalDateTime
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

class NFe_Cancelamento extends FormulaBase {
	
	private Eaa01 eaa01;
	private static String versaoXMLLayoutCancNFe = "1.00";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVOS_DE_NFE;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		
		Abb01 central = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", eaa01.getEaa01central().getAbb01id());
		
		Aac10 empresa = obterEmpresaAtiva();
		empresa.setAac10municipio(getAcessoAoBanco().buscarRegistroUnicoById("Aag0201", empresa.aac10municipio.aag0201id));
					
		/** GERAÇÃO DO XML DE PEDIDO DE CANCELAMENTO DA NFE */
		LocalDate dtProdNFe = getAcessoAoBanco().buscarParametro("NFeDataProducao", "EA");
		boolean isProducao = dtProdNFe == null ? false : DateUtils.dateDiff(dtProdNFe, central.getAbb01data(), ChronoUnit.DAYS) >= 0;
		
		/** evento - Evento do Lote */
		ElementXml evento = NFeUtils.criarElementXmlNFe("http://www.portalfiscal.inf.br/nfe", "evento")
		evento.setAttribute("versao", versaoXMLLayoutCancNFe);

		/** infEvento - Informações do Registro do Evento */
		ElementXml infEvento = evento.addNode("infEvento");
		infEvento.setAttribute("Id", "ID110111" + eaa01.getEaa01nfeChave() + StringUtils.ajustString(1, 2));
		infEvento.addNode("cOrgao", empresa.aac10municipio.aag0201uf.aag02ibge, true);
		infEvento.addNode("tpAmb", isProducao ? 1 : 2, true);
		infEvento.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.getAac10ni()), 14), true);
		infEvento.addNode("chNFe", eaa01.getEaa01nfeChave(), true);
		infEvento.addNode("dhEvento", NFeUtils.dataFormatoUTC(MDate.dateTime(), empresa.aac10fusoHorario), true);
		infEvento.addNode("tpEvento", "110111", true);
		infEvento.addNode("nSeqEvento", 1, true);
		infEvento.addNode("verEvento", versaoXMLLayoutCancNFe, true);
		
		/** detEvento - Informações do Pedido de Cancelamento */
		ElementXml detEvento = infEvento.addNode("detEvento");
		detEvento.setAttribute("versao", versaoXMLLayoutCancNFe);
		detEvento.addNode("descEvento", "Cancelamento", true);
		detEvento.addNode("nProt", eaa01.getEaa01nfeProt(), true);
		Aae11 aae11 = getAcessoAoBanco().buscarRegistroUnicoById("Aae11", eaa01.getEaa01cancMotivo().getAae11id());
		detEvento.addNode("xJust", aae11.getAae11descr(), true);
		
		/** Gera o XML */
		String dados = NFeUtils.gerarXML(evento);
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==