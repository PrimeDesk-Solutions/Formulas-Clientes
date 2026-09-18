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
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0114
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils

class SRF_CCe extends FormulaBase {
	
	private Eaa01 eaa01;
	private Eaa0114 eaa0114;
	
	private static String versaoXMLLayoutCCe = "1.00";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVOS_DE_NFE;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		eaa0114 = get("eaa0114");
		
		Abb01 central = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", eaa01.getEaa01central().getAbb01id());
		Aac10 empresa = obterEmpresaAtiva();
		empresa.setAac10municipio(getAcessoAoBanco().buscarRegistroUnicoById("Aag0201", empresa.getAac10municipio().getAag0201id()));
				
		/** Gerando XML */
		String chaveAcesso = eaa01.getEaa01nfeChave();
		if(chaveAcesso == null) throw new ValidacaoException("A NFe " + central.getAbb01num() + " não possui chave de acesso.");
		if(eaa01.getEaa01nfeStat() != 3) throw new ValidacaoException("A NFe " + central.getAbb01num() + " não foi autorizada, portanto não pode ser corrigida.");

		LocalDate dtProdNFe = getAcessoAoBanco().buscarParametro("NFeDataProducao", "EA");
		Boolean isProducao = DateUtils.dateDiff(dtProdNFe, central.getAbb01data(), ChronoUnit.DAYS) >= 0;
		
		/** ALTERA DADOS DA CARTA DE CORREÇÃO */
		eaa0114.setEaa0114status(1);
		
		/** GERAÇÃO CCe - Carta de Correção Eletrônica */
		ElementXml evento = NFeUtils.criarElementXmlNFe("http://www.portalfiscal.inf.br/nfe", "evento");
		evento.setAttribute("versao", versaoXMLLayoutCCe);
		
		/** infEvento - Informações do Registro do Evento */
		ElementXml infEvento = evento.addNode("infEvento");
		infEvento.setAttribute("Id", "ID110110" + chaveAcesso + StringUtils.ajustString(eaa0114.getEaa0114seq(), 2));
		infEvento.addNode("cOrgao", empresa.getAac10municipio().getAag0201ibge(), true);
		infEvento.addNode("tpAmb", isProducao ? 1 : 2, true);
		infEvento.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.getAac10ni()), 14), true);
		infEvento.addNode("chNFe", chaveAcesso, true);
		infEvento.addNode("dhEvento", NFeUtils.dataFormatoUTC(MDate.dateTime(), empresa.aac10fusoHorario), true);
		infEvento.addNode("tpEvento", "110110", true);
		infEvento.addNode("nSeqEvento", eaa0114.getEaa0114seq(), true);
		infEvento.addNode("verEvento", versaoXMLLayoutCCe, true);
		
		/** detEvento - Informações da Carta de Correção */
		ElementXml detEvento = infEvento.addNode("detEvento");
		detEvento.setAttribute("versao", versaoXMLLayoutCCe);
		detEvento.addNode("descEvento", "Carta de Correcao", true);
		detEvento.addNode("xCorrecao", eaa0114.getEaa0114correcao(), true, 1000);
		detEvento.addNode("xCondUso", "A Carta de Correcao e disciplinada pelo paragrafo 1o-A do art. 7o do Convenio S/N, de 15 de dezembro de 1970 e pode ser utilizada para regularizacao de erro ocorrido na emissao de documento fiscal, desde que o erro nao esteja relacionado com: I - as variaveis que determinam o valor do imposto tais como: base de calculo, aliquota, diferenca de preco, quantidade, valor da operacao ou da prestacao; II - a correcao de dados cadastrais que implique mudanca do remetente ou do destinatario; III - a data de emissao ou de saida.", true);
		
		/** Gera o XML */
		getSession().persist(eaa0114);
		String dados = NFeUtils.gerarXML(evento);
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==