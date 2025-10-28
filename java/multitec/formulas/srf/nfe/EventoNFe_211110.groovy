package multitec.formulas.srf.nfe

import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

class EventoNFe_211110 extends FormulaBase {
	
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
		infEvento.setAttribute("Id", "ID110110" + chaveAcesso + StringUtils.ajustString(eaa0114.getEaa0114seq(), 2));
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
		detEvento.addNode("tpAutor", 1, true);
		detEvento.addNode("verAplic", "SAM4_" + Utils.getVersao(), true)
		
		/** gCredPres - Informações de crédito presumido por item  */
		if(eaa0114.eaa01141s != null && eaa0114.eaa01141s.size() > 0) {
			for(Eaa01141 eaa01141 : eaa0114.eaa01141s) {
				ElementXml gCredPres = detEvento.addNode("gCredPres");
				gCredPres.addNode("nItem", eaa01141.eaa01141seq_Zero);
				gCredPres.addNode("vBC", eaa01141.eaa01141json.getBigDecimal_Zero("credpres_vbc"));
				
				if(eaa01141.eaa01141json.getString("credpres_ibs_cod") != null) {
					ElementXml gIBS = gCredPres.addNode("gIBS");
					gIBS.addNode("cCredPres", eaa01141.eaa01141json.getString("credpres_ibs_cod"));
					gIBS.addNode("pCredPres", eaa01141.eaa01141json.getBigDecimal_Zero("credpres_ibs_perc"));
					gIBS.addNode("vCredPres", eaa01141.eaa01141json.getBigDecimal_Zero("credpres_ibs_vlr"));
				}
				
				if(eaa01141.eaa01141json.getString("credpres_cbs_cod") != null) {
					ElementXml gCBS = gCredPres.addNode("gCBS");
					gCBS.addNode("cCredPres", eaa01141.eaa01141json.getString("credpres_cbs_cod"));
					gCBS.addNode("pCredPres", eaa01141.eaa01141json.getBigDecimal_Zero("credpres_cbs_perc"));
					gCBS.addNode("vCredPres", eaa01141.eaa01141json.getBigDecimal_Zero("credpres_cbs_vlr"));
				}
			}
		}
		
		/** Gera o XML */
		String dados = NFeUtils.gerarXML(evento);
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==