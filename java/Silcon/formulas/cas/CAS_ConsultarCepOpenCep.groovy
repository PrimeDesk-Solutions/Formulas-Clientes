package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import sam.model.entities.ab.Aba20
import sam.model.entities.ab.Aba2001;

import java.nio.charset.Charset
import javax.net.ssl.SSLHandshakeException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.dicdados.FormulaTipo
import sam.dto.cadastro.CepDto
import sam.server.samdev.formula.FormulaBase

public class CAS_ConsultarCepOpenCep extends FormulaBase{
    String logradouro = "";
    String bairro = "";
    String uf = "";
    String cidade = "";
    String pais = "BRASIL";

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_CONSULTAR_CEP;
    }

    @Override
    public void executar() {
        def cep = get("cep");

        HttpURLConnection connection = null;
        try {
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            URL url = new URL("https://opencep.com/v1/" + cep + ".json");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            String responseCode = connection.getResponseCode().toString();
            CepDto cepDto = new CepDto();

            if(responseCode != "404"){
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

                if(tmCep == null || tmCep.size() == 0){
                    buscarCepRepositorio(cep)
                }else{
                    logradouro = tmCep.getString("logradouro");
                    bairro = tmCep.getString("bairro");
                    uf = tmCep.getString("uf");
                    cidade = tmCep.getString("localidade");
                }

            }else{
                buscarCepRepositorio(cep)
            }

            cepDto.logradouro = logradouro;
            cepDto.bairro = bairro;
            cepDto.uf = uf;
            cepDto.cidade = cidade;
            cepDto.pais = pais;

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
    private void buscarCepRepositorio(String cep){
        try{
            // Repositorio de dados de frete
            Aba20 aba20 = getSession().createCriteria(Aba20.class).addWhere(Criterions.eq("aba20codigo", "001")).get(ColumnType.ENTITY);

            if(aba20 == null) interromper("Não foi encontrado repositório de dados de CEP com o código 001");

            // Linha do repositorio com o frete
            Aba2001 aba2001 = getSession().createCriteria(Aba2001.class).addWhere(Criterions.where("aba2001rd = " + aba20.aba20id + " AND CAST(aba2001json ->> 'cep' AS text) = '" + cep + "'")).get(ColumnType.ENTITY);

            if(aba2001 == null) interromper("CEP " + cep + " não cadastrado no repositório de dados.");

            TableMap jsonAba2001 = aba2001.aba2001json != null ? aba2001.aba2001json : new TableMap();

            logradouro = jsonAba2001.getString("logradouro");
            bairro = jsonAba2001.getString("bairro");
            uf = jsonAba2001.getString("uf");
            cidade = jsonAba2001.getString("cidade");
        }catch(Exception e){
            throw new ValidacaoException("Erro ao buscar CEP no repositório de dados.")
        }
    }

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjgifQ==