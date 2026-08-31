package multitec.formulas.srf.nfse;

import java.time.LocalDateTime

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aaj05
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.NFeUtils

public class Nfse_Itatiba extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.NFSE;
	}
	
	@Override
	public void executar() {
		List<Eaa01> eaa01s = get("eaa01s");
		
		//Seleciona Alinhamento (CAMPOS LIVRES)
		selecionarAlinhamento("0011");

		Aac10 aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);
		
		/** GERAÇÃO XML	*/
		ElementXml enviarLoteRpsEnvio = NFeUtils.criarElementXmlNFe("http://iss.itatiba.sp.gov.br/Arquivos/nfseV202.xsd", "EnviarLoteRpsEnvio");
		
		ElementXml LoteRps = enviarLoteRpsEnvio.addNode("LoteRps");
		LoteRps.setAttribute("Id", "lote");
		LoteRps.setAttribute("Versao", "2.02");
		
		long numLote = System.currentTimeMillis();
		LoteRps.addNode("NumeroLote", numLote, true);
		
		ElementXml Cnpj = LoteRps.addNode("CpfCnpj");
		Cnpj.addNode("Cnpj", StringUtils.extractNumbers(aac10.aac10ni), true);
		
		LoteRps.addNode("InscricaoMunicipal", StringUtils.extractNumbers(aac10.aac10im), true);
		LoteRps.addNode("QuantidadeRps", eaa01s.size(), true);
		
		ElementXml listaRps = LoteRps.addNode("ListaRps");
		for(Eaa01 eaa01 : eaa01s) {
			TableMap eaa01json = eaa01.eaa01json;
			
			Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
			LocalDateTime data = new LocalDateTime(abb01.abb01data, abb01.getAbb01operHora());

			Eaa0102 eaa0102 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eaa0102 WHERE eaa0102doc = :eaa01id", criarParametroSql("eaa01id", eaa01.eaa01id));
			Eaa0101 eaa0101Princ = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101principal", 1), Criterions.eq("eaa0101doc", eaa01.eaa01id));
			Aag0201 municipioPrinc = eaa0101Princ.eaa0101municipio == null ? null : getSession().get(Aag0201.class, eaa0101Princ.eaa0101municipio.aag0201id);
			Aag02 ufPrinc = eaa0101Princ.eaa0101municipio == null ? null : getSession().get(Aag02.class, municipioPrinc.aag0201uf.aag02id);
			
			/** NOTA */
			ElementXml Rps1 = listaRps.addNode("Rps");
			
			ElementXml InfDeclaracaoPrestacaoServico = Rps1.addNode("InfDeclaracaoPrestacaoServico");
			InfDeclaracaoPrestacaoServico.setAttribute("Id", "Id"+ (numLote++));
			
			ElementXml Rps2 = InfDeclaracaoPrestacaoServico.addNode("Rps");
			
			ElementXml IdentificacaoRps = Rps2.addNode("IdentificacaoRps");
			IdentificacaoRps.addNode("Numero", abb01.abb01num, true);
			IdentificacaoRps.addNode("Serie", abb01.abb01serie == null ? "Única" : abb01.abb01serie, true, 5);
			IdentificacaoRps.addNode("Tipo", "1", true);
			
			Rps2.addNode("DataEmissao", NFeUtils.dataFormatoUTC(data, aac10.aac10fusoHorario), true);
			Rps2.addNode("Status", 1, true);
			

			ElementXml ListaServicos = InfDeclaracaoPrestacaoServico.addNode("ListaServicos");
			
			TreeSet<Eaa0103> eaa0103sOrdem = new TreeSet<Eaa0103>(new Comparator<Eaa0103>(){
				public int compare(Eaa0103 o1, Eaa0103 o2) {
					return o1.eaa0103seq.compareTo(o2.eaa0103seq);
				}
			});
		
			List<Eaa0103> eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM eaa0103 WHERE eaa0103doc = " + eaa01.eaa01id);
			eaa0103sOrdem.addAll(eaa0103s);

			for(Eaa0103 eaa0103 : eaa0103sOrdem) {
				ElementXml servico = ListaServicos.addNode("Servico");
				TableMap eaa0103json = eaa0103.eaa0103json;
			
				ElementXml valores = servico.addNode("Valores");
				valores.addNode("ValorServicos", NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0","ValorServicos")), 2, false), true);
				valores.addNode("ValorDeducoes", NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0","ValorDeducoes")), 2, false), true);
				valores.addNode("ValorIss", NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0","ValorIss")), 2, false), true);
				valores.addNode("Aliquota", NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0","Aliquota")), 2, false), true);
				valores.addNode("BaseCalculo", NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0","BaseCalculo")), 2, false), true);
				
				servico.addNode("IssRetido", eaa0102.eaa0102issRet == 0 ? 2 : 1, true);
				
				Aaj05 aaj05 = eaa0103.eaa0103codServ == null ? null : getSession().get(Aaj05.class, eaa0103.eaa0103codServ.aaj05id);
				servico.addNode("ItemListaServico", aaj05 == null ? null : aaj05.aaj05codigo, true);
				
				servico.addNode("CodigoCnae", aaj05.aaj05cnae == null ? aac10.aac10cnae : aaj05.aaj05cnae, false);
				servico.addNode("Discriminacao", eaa0103.eaa0103descr, true);
				servico.addNode("CodigoMunicipio", eaa0101Princ == null ? null : municipioPrinc.aag0201ibge, true);
				servico.addNode("ExigibilidadeISS", eaa0103.eaa0103issExig == 0 ? 1 : eaa0103.eaa0103issExig, false);
				
				Aag0201 municIncidencia = null;
				if(eaa0102.eaa0102issRet == 0) {
					municIncidencia = aac10.aac10municipio == null ? null : getSession().get(Aag0201.class, aac10.aac10municipio.aag0201id);
				}else {
					municIncidencia = municipioPrinc == null ? null : getSession().get(Aag0201.class, municipioPrinc.aag0201id);
				}
				servico.addNode("MunicipioIncidencia", municIncidencia != null ? municIncidencia.aag0201ibge : null, true);
			}
			
			InfDeclaracaoPrestacaoServico.addNode("Competencia", NFeUtils.dataFormatoUTC(data, aac10.aac10fusoHorario), true);
			
			/** Prestador */
			ElementXml prestador = InfDeclaracaoPrestacaoServico.addNode("Prestador");
			ElementXml CpfCnpjPrestador = prestador.addNode("CpfCnpj");
			CpfCnpjPrestador.addNode("Cnpj", StringUtils.extractNumbers(aac10.aac10ni), true);
			prestador.addNode("InscricaoMunicipal", StringUtils.extractNumbers(aac10.aac10im), false);
			
			/** Tomador */
			ElementXml tomadorServico = InfDeclaracaoPrestacaoServico.addNode("TomadorServico");
			ElementXml identificacaoTomador = tomadorServico.addNode("IdentificacaoTomador");
			
			ElementXml cpfCnpjTomador = identificacaoTomador.addNode("CpfCnpj");
			cpfCnpjTomador.addNode(eaa0102.eaa0102ti == 0 ? "Cnpj" : "Cpf", StringUtils.extractNumbers(eaa0102.eaa0102ni), true);

			tomadorServico.addNode("RazaoSocial", eaa0102.eaa0102nome, true);
			
			ElementXml endereco = tomadorServico.addNode("Endereco");
			endereco.addNode("Endereco", eaa0101Princ.eaa0101endereco, false);
			endereco.addNode("Numero", eaa0101Princ.eaa0101numero, false);
			endereco.addNode("Complemento", eaa0101Princ.eaa0101complem, false);
			endereco.addNode("Bairro", eaa0101Princ.eaa0101bairro, false);
			endereco.addNode("CodigoMunicipio", municipioPrinc == null ? null : municipioPrinc.aag0201ibge, false);
			endereco.addNode("Uf", ufPrinc == null ? null : ufPrinc.aag02uf, false);
			endereco.addNode("CodigoPais", eaa0101Princ.eaa0101pais == null ? null : eaa0101Princ.eaa0101pais.aag01ibge, false);
			endereco.addNode("Cep", eaa0101Princ.eaa0101cep, false);
			
			ElementXml contato = tomadorServico.addNode("Contato");
			contato.addNode("Telefone", NFeUtils.ajustarFone(eaa0101Princ.eaa0101ddd, eaa0101Princ.eaa0101fone));
			contato.addNode("Email", eaa0101Princ.eaa0101eMail, false, 80);

			def simplesNacional = optanteSimplesNacional(aac10.aac10id);
			InfDeclaracaoPrestacaoServico.addNode("OptanteSimplesNacional", simplesNacional ? 1 : 2, true);
			InfDeclaracaoPrestacaoServico.addNode("OutrasInformacoes", eaa01.eaa01obsFisco == null ? null : eaa01.eaa01obsFisco.length() > 255 ? eaa01.eaa01obsFisco.substring(0, 254) : eaa01.eaa01obsFisco, false);
			
			/** Valores */
			ElementXml valoresServico = InfDeclaracaoPrestacaoServico.addNode("ValoresServico");
			valoresServico.addNode("ValorPis", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorPis")), 2, false), true);
			valoresServico.addNode("ValorCofins", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorCofins")), 2, false), true);
			valoresServico.addNode("ValorInss", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorInss")), 2, false), true);
			valoresServico.addNode("ValorIr", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorIr")), 2, false), true);
			valoresServico.addNode("ValorCsll", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorCsll")), 2, false), true);
			valoresServico.addNode("ValorIss", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorIss")), 2, false), true);
			valoresServico.addNode("ValorLiquidoNfse", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorLiquidoNfse")), 2, false), true);
			valoresServico.addNode("ValorServicos", NFeUtils.formatarDecimal(eaa01json.getBigDecimal(getCampo("0","ValorServicos")), 2, false), true);
		}

		String dados = NFeUtils.gerarXML(enviarLoteRpsEnvio);
		put("dados", dados);
		put("tagsAssinar", "InfDeclaracaoPrestacaoServico;LoteRps"); 
	}
	
	private boolean optanteSimplesNacional(Long aac10id) {
		String sql = " SELECT COUNT(*) FROM Aaj01 " +
					 " INNER JOIN Aac13 ON aac13classTrib = aaj01id " +
					 " INNER JOIN Aac10 ON aac13empresa = aac10id " +
					 " WHERE aac10id = :aac10id AND aaj01codigo IN (:codigos) ";
					 
		def result = getSession().createQuery(sql)
					 .setParameters("aac10id", aac10id, "codigos", Utils.list("001", "002"))
					 .getUniqueResult(ColumnType.INTEGER);
					 
		return result > 0;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzIifQ==