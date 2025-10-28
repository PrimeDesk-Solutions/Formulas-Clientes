package multitec.formulas.srf.mdfe

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa10;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.MDFeUtils

public class MDFe_Cancelamento extends FormulaBase {
	private Eaa10 eaa10;
	private Integer tipo;
	private Aac10 aac10;

	private String versaoXMLLayoutMDFe = "3.00";

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_MDFE;
	}

	@Override
	public void executar() {
		eaa10 = get("eaa10");
		tipo = get("tipo");

		aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);

		def abb01 = getSession().get(Abb01.class, eaa10.eaa10central.abb01id);

		/*** GERAÇÃO DO XML ***/
		def dtProdNFe = getAcessoAoBanco().buscarParametro("MDFeDataProducao", "EA");
		def isProducao = dtProdNFe == null ? false : DateUtils.dateDiff(dtProdNFe, abb01.abb01data, ChronoUnit.DAYS) >= 0;

		def eventoMDFe = MDFeUtils.criarElementXmlNFe("http://www.portalfiscal.inf.br/mdfe", "eventoMDFe");
		eventoMDFe.setAttribute("versao", versaoXMLLayoutMDFe);

		def codigoIbge = buscarCodigoIbge(aac10);

		def infEvento = eventoMDFe.addNode("infEvento");
		infEvento.setAttribute("Id", "ID110111" + eaa10.eaa10chave + StringUtils.ajustString(1, 2));
		infEvento.addNode("cOrgao", codigoIbge, true);
		infEvento.addNode("tpAmb", isProducao ? 1 : 2, true);
		infEvento.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 14), true);
		infEvento.addNode("chMDFe", eaa10.eaa10chave, true);
		infEvento.addNode("dhEvento", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")), true); // sam3 -> yyyy-MM-dd'T'HH:mm:ssXXX
		infEvento.addNode("tpEvento", "110111", true);
		infEvento.addNode("nSeqEvento", 1, true);

		/*** detEvento - Informações do Evento ***/
		def detEvento = infEvento.addNode("detEvento");
		detEvento.setAttribute("versaoEvento", versaoXMLLayoutMDFe);

		/*** evCancMDFe - Evento de cancelamento ***/
		def evCancMDFe = detEvento.addNode("evCancMDFe");
		evCancMDFe.addNode("descEvento", "Cancelamento", true);
		evCancMDFe.addNode("nProt", eaa10.eaa10retProt, true);
		evCancMDFe.addNode("xJust", eaa10.eaa10cancMotivo, true);

		/** Gera o XML */
		def dados = MDFeUtils.gerarXML(eventoMDFe);
		put("dados", dados);
	}

	def buscarCodigoIbge(Aac10 aac10) {
		def sql = " SELECT aag02ibge FROM Aac10 " +
				" INNER JOIN Aag0201 ON aag0201id = aac10municipio " +
				" INNER JOIN Aag02 ON aag02id = aag0201uf " +
				" WHERE aac10id = :municipio " + obterWherePadrao("Aac10");

		return getAcessoAoBanco().obterString(sql, criarParametroSql("municipio", aac10.aac10id));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzQifQ==