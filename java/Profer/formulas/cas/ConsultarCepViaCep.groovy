package profer.formulas.cas

import java.nio.charset.Charset

import javax.net.ssl.SSLHandshakeException

import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.dicdados.FormulaTipo
import sam.dto.cadastro.CepDto
import sam.server.samdev.formula.FormulaBase

class ConsultarCepViaCep extends FormulaBase {

	@Override
	public void executar() {
		def cep = get("cep");
		
		HttpURLConnection connection = null;
		try {
			java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
			URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			
			// Executar
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
			String inputLine;
			StringBuffer xml = new StringBuffer();
		   
			while ((inputLine = bufferedReader.readLine()) != null) {
				xml.append(inputLine);
			}
			bufferedReader.close();
			
			TableMap tmCep = JSonMapperCreator.create().read(xml.toString(), TableMap.class);
			
			CepDto cepDto = new CepDto();
			cepDto.logradouro = tmCep.getString("logradouro")
			cepDto.bairro = tmCep.getString("bairro")
			cepDto.uf = tmCep.getString("uf")
			cepDto.cidade = tmCep.getString("localidade")
			
			put("cepDto", cepDto)
			
		} catch (SSLHandshakeException err) {
			throw new RuntimeException("Não foi possível se conectar com o servidor do ViaCEP.\nVerifique se sua conexão está funcionado e se o acesso ao servidor ViaCEP está liberado em sua rede.", err);
		} catch(IOException err) {
			throw new RuntimeException("Erro ao buscar CEP no ViaCEP.", err);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CAS_CONSULTAR_CEP;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjgifQ==