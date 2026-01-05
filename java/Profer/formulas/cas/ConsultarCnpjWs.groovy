package Profer.formulas.cas;

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

import br.com.multitec.utils.ValidacaoException
import sam.dicdados.FormulaTipo
import sam.dto.cadastro.CNPJ
import sam.server.samdev.formula.FormulaBase

class ConsultarCnpjWs extends FormulaBase {

	String ni; //N.I. Número de Inscrição (CNPJ) para efetuar a consulta
	CNPJ cnpj; //Objeto com dados de retorno da consulta de CNPJ

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CAS_CONSULTAR_CNPJ;
	}

	@Override
	public void executar() {
		ni = get("ni");

		String json = obterJSONconsultaCNPJ(ni);

		cnpj = obterDadosDoJSON(json);

		put("cnpj", cnpj);
	}

	private String obterJSONconsultaCNPJ(String cnpj) {
		HttpURLConnection connection = null;
		try {
			java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			String urlBuscaCNPJ = "https://publica.cnpj.ws/cnpj/";
			URL url = new URL(urlBuscaCNPJ+cnpj);
			connection = (HttpURLConnection) url.openConnection();

			InputStream is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));

			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = reader.readLine()) != null) {
				response.append(responseLine.trim());
			}

			return response.toString();
		}catch(Exception err) {
			String strErro = null;
			try {
				if(connection != null) {
					if(connection.getResponseCode() == 400) {
						strErro = "Requisição inválida ao Web-Service do CNPJ.ws.";
					}
					if(connection.getResponseCode() == 401) {
						strErro = "Acesso não autorizado ao Web-Service do CNPJ.ws.";
					}
					if(connection.getResponseCode() == 403) {
						strErro = "Acesso negado ao Web-Service do CNPJ.ws.";
					}
					if(connection.getResponseCode() == 404) {
						strErro = "Recurso não localizado no Web-Service do CNPJ.ws.";
					}
					if(connection.getResponseCode() == 429) {
						strErro = "Limite de acessos ao Web-Service do CNPJ.ws excedido por minuto, tente novamente um pouco mais tarde.";
					}
					if(connection.getResponseCode() == 500) {
						strErro = "Erro interno no Web-Service do CNPJ.ws.";
					}
				}

			}catch(Exception err2) {

			}

			if(strErro != null) {
				throw new ValidacaoException(strErro);
			}else {
				throw new RuntimeException("Erro ao tentar conexão com o Web-Service do CNPJ.ws.", err);
			}
		}finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private CNPJ obterDadosDoJSON(String json) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(json);

		JsonNode razaoSocialNode = rootNode.path("razao_social");
		String razaoSocial = razaoSocialNode.asText();
		if(razaoSocial != null && razaoSocial.equals("null")) razaoSocial = null;

		JsonNode estabelecimentoNode = rootNode.path("estabelecimento");

		JsonNode cnpjNode = estabelecimentoNode.path("cnpj");
		String cnpjConsultado = cnpjNode.asText();
		if(cnpjConsultado != null && cnpjConsultado.equals("null")) cnpjConsultado = null;

		JsonNode nomeFantasiaNode = estabelecimentoNode.path("nome_fantasia");
		String nomeFantasia = nomeFantasiaNode.asText();
		if(nomeFantasia != null && nomeFantasia.equals("null")) nomeFantasia = null;

		JsonNode logradouroNode = estabelecimentoNode.path("logradouro");
		String logradouro = logradouroNode.asText();
		if(logradouro != null && logradouro.equals("null")) logradouro = null;

		JsonNode numeroNode = estabelecimentoNode.path("numero");
		String numero = numeroNode.asText();
		if(numero != null && numero.equals("null")) numero = null;

		JsonNode complementoNode = estabelecimentoNode.path("complemento");
		String complemento = complementoNode.asText();
		if(complemento != null && complemento.equals("null")) complemento = null;

		JsonNode cepNode = estabelecimentoNode.path("cep");
		String cep = cepNode.asText();
		if(cep != null && cep.equals("null")) cep = null;

		JsonNode bairroNode = estabelecimentoNode.path("bairro");
		String bairro = bairroNode.asText();
		if(bairro != null && bairro.equals("null")) bairro = null;

		JsonNode emailNode = estabelecimentoNode.path("email");
		String email = emailNode.asText();
		if(email != null && email.equals("null")) email = null;

		JsonNode ddd1Node = estabelecimentoNode.path("ddd1");
		String ddd1 = ddd1Node.asText();
		if(ddd1 != null && ddd1.equals("null")) ddd1 = null;

		JsonNode telefone1Node = estabelecimentoNode.path("telefone1");
		String telefone1 = telefone1Node.asText();
		if(telefone1 != null && telefone1.equals("null")) telefone1 = null;

		JsonNode cidadeNode = estabelecimentoNode.path("cidade");
		JsonNode nomeCidadeNode = cidadeNode.path("nome");
		String nomeCidade = nomeCidadeNode.asText();
		if(nomeCidade != null && nomeCidade.equals("null")) nomeCidade = null;

		JsonNode estadoNode = estabelecimentoNode.path("estado");
		JsonNode ufNode = estadoNode.get("sigla");
		String uf = ufNode.textValue();
		if(uf != null && uf.equals("null")) uf = null;

		//Inscrição Estadual
		String inscricaoEstadual = null;
		JsonNode inscricoesEstaduaisNode = estabelecimentoNode.path("inscricoes_estaduais");
		Iterator<JsonNode> inscricoesEstaduaisElements = inscricoesEstaduaisNode.elements();
		while(inscricoesEstaduaisElements.hasNext()) {
			JsonNode inscricaoEstadualNode = inscricoesEstaduaisElements.next();

			JsonNode ativoInscricaoEstadualNode = inscricaoEstadualNode.path("ativo");
			String ativoInscricaoEstadual = ativoInscricaoEstadualNode.asText();

			JsonNode estadoInscricaoEstadualNode = inscricaoEstadualNode.path("estado");
			JsonNode siglaNode = estadoInscricaoEstadualNode.get("sigla");
			String ufIE = siglaNode.textValue();
			if(uf.equals(ufIE) && ativoInscricaoEstadual.equals("true")) {
				JsonNode ieNode = inscricaoEstadualNode.path("inscricao_estadual");
				inscricaoEstadual = ieNode.asText();
				if(inscricaoEstadual != null && inscricaoEstadual.equals("null")) inscricaoEstadual = null;
				break;
			}
		}

		//Compondo objeto CNPJ para retorno dos dados
		CNPJ cnpj = new CNPJ();
		cnpj.cnpj = cnpjConsultado;
		cnpj.nome = razaoSocial;
		cnpj.fantasia = nomeFantasia;
		cnpj.logradouro = logradouro;
		cnpj.numero = numero;
		cnpj.complemento = complemento;
		cnpj.cep = cep;
		cnpj.bairro = bairro;
		cnpj.email = email;
		cnpj.ddd = ddd1;
		cnpj.telefone = telefone1;
		cnpj.uf = uf;
		cnpj.cidade = nomeCidade;
		cnpj.inscricaoEstadual = inscricaoEstadual;
		//cnpj.suframa = null;
		//cnpj.regimeTributario = null; //(abe01regTrib)
		//cnpj.porte = null 			//(abe01porte)

		return cnpj;
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjcifQ==