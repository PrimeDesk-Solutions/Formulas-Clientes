package multitec.formulas.srf.nfe

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aaj26
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0114
import sam.model.entities.ea.Eaa01141
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class EventoNFe_211124 extends FormulaBase {
	
	private Eaa01 eaa01;
	private Eaa0114 eaa0114;
	private Aaj26 aaj26;
	
	private static String versaoXMLLayout = "1.00";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVOS_DE_NFE;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		eaa0114 = get("eaa0114");
		
		if(eaa0114.eaa0114eveNFe == null) throw new ValidacaoException("Evento não informado ou não encontrado.");
		
		aaj26 = getAcessoAoBanco().buscarRegistroUnicoById("Aaj26", eaa0114.eaa0114eveNFe.aaj26id);
		
		Abb01 central = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", eaa01.getEaa01central().getAbb01id());
		Aac10 empresa = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
				
		/** Gerando XML */
		String chaveAcesso = eaa01.getEaa01nfeChave();
		if(chaveAcesso == null) throw new ValidacaoException("A NFe " + central.getAbb01num() + " não possui chave de acesso.");
		if(eaa01.getEaa01nfeStat() != 3) throw new ValidacaoException("A NFe " + central.getAbb01num() + " não foi autorizada, portanto não pode ser corrigida.");

		LocalDate dtProdNFe = getAcessoAoBanco().buscarParametro("NFeDataProducao", "EA");
		Boolean isProducao = dtProdNFe == null ? false : DateUtils.dateDiff(dtProdNFe, central.getAbb01data(), ChronoUnit.DAYS) >= 0;
		
		/** GERAÇÃO Evento NFe */
		ElementXml evento = NFeUtils.criarElementXmlNFe("http://www.portalfiscal.inf.br/nfe", "evento");
		evento.setAttribute("versao", versaoXMLLayout);
		
		/** infEvento - Informações do Registro do Evento */
		ElementXml infEvento = evento.addNode("infEvento");
		infEvento.setAttribute("Id", "ID211124" + chaveAcesso + StringUtils.ajustString(eaa0114.getEaa0114seq(), 2));
		infEvento.addNode("cOrgao", empresa.aac10municipio.aag0201uf.aag02ibge, true);
		infEvento.addNode("tpAmb", isProducao ? 1 : 2, true);
		infEvento.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.getAac10ni()), 14), true);
		infEvento.addNode("chNFe", chaveAcesso, true);
		infEvento.addNode("dhEvento", NFeUtils.dataFormatoUTC(MDate.dateTime(), empresa.aac10fusoHorario), true);
		infEvento.addNode("tpEvento", aaj26.aaj26codigo, true);
		infEvento.addNode("nSeqEvento", eaa0114.getEaa0114seq(), true);
		infEvento.addNode("verEvento", versaoXMLLayout, true);
		
		/** detEvento - Informações do Evento */
		ElementXml detEvento = infEvento.addNode("detEvento");
		detEvento.setAttribute("versao", versaoXMLLayout);
		detEvento.addNode("descEvento", aaj26.aaj26descr, true);
		detEvento.addNode("cOrgaoAutor", empresa.aac10municipio.aag0201uf.aag02ibge, true);
		detEvento.addNode("tpAutor", 2, true);
		detEvento.addNode("verAplic", "SAM4_" + Utils.getVersao(), true)
		
		/** gConsumo - Informações por item da NF-e de Aquisição  */
		if(eaa0114.eaa01141s != null && eaa0114.eaa01141s.size() > 0) {
			for(Eaa01141 eaa01141 : eaa0114.eaa01141s) {
				ElementXml gPerecimento = detEvento.addNode("gPerecimento");
                gPerecimento.setAttribute("nItem", eaa01141.eaa01141seq_Zero.toString());
                gPerecimento.addNode("vIBS", eaa01141.eaa01141json.getBigDecimal_Zero("gperecimento_vibs"));
                gPerecimento.addNode("vCBS", eaa01141.eaa01141json.getBigDecimal_Zero("gperecimento_vcbs"));

                ElementXml gControleEstoque = gPerecimento.addNode("gControleEstoque");
                gControleEstoque.addNode("qPerecimento", eaa01141.eaa01141json.getBigDecimal_Zero("gcontroleestoque_qperecimento"));
                gControleEstoque.addNode("uPerecimento", eaa01141.eaa01141json.getString("gcontroleestoque_uperecimento"));
			}
		}
		
		/** Gera o XML */
		String dados = NFeUtils.gerarXML(evento);
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==