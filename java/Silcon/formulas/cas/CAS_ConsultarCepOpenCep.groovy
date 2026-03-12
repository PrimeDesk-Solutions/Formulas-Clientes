package Silcon.formulas.cas;

import java.nio.charset.Charset
import javax.net.ssl.SSLHandshakeException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.dicdados.FormulaTipo
import sam.dto.cadastro.CepDto
import sam.server.samdev.formula.FormulaBase

public class CAS_ConsultarCepOpenCep extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_CONSULTAR_CEP;
    }

    @Override
    public void executar() {
        interromper("teste")
        def cep = get("cep");

        HttpURLConnection connection = null;
        try {
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            URL url = new URL("https://opencep.com/v1/" + cep + ".json");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"))
            );

            String inputLine;
            StringBuffer json = new StringBuffer();

            while ((inputLine = bufferedReader.readLine()) != null) {
                json.append(inputLine);
            }
            bufferedReader.close();

            TableMap tmCep = JSonMapperCreator.create().read(json.toString(), TableMap.class);

            CepDto cepDto = new CepDto();
            cepDto.logradouro = tmCep.getString("logradouro");
            cepDto.bairro = tmCep.getString("bairro");
            cepDto.uf = tmCep.getString("uf");
            cepDto.cidade = tmCep.getString("localidade");
            cepDto.pais = "BRASIL";

            put("cepDto", cepDto);

        } catch (SSLHandshakeException err) {
            throw new RuntimeException(
                    "Não foi possível se conectar ao servidor OpenCEP.", err
            );
        } catch (IOException err) {
            throw new RuntimeException(
                    "Erro ao buscar CEP no OpenCEP.", err
            );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjgifQ==