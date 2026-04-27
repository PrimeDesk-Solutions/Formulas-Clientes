package Atilatte.servlets;


import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import br.com.multiorm.ColumnType
import br.com.multitec.utils.ValidacaoException;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import sam.server.samdev.utils.Parametro

import javax.net.ssl.SSLHandshakeException
import java.nio.charset.Charset
import java.time.LocalDate
import java.util.stream.Collectors
import com.fasterxml.jackson.core.type.TypeReference;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.jackson.JSonMapperCreator
import java.time.format.DateTimeFormatter;

public class CGS_Compor_Feriados_Repositorio extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "CGS - Compor Feriados Repositorio";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Object> executar() {
        String req = httpServletRequest.getReader().lines().collect(Collectors.joining());
        TableMap body = JSonMapperCreator.create().read(req, new TypeReference<TableMap>() {});

        List<TableMap> listFeriados = buscarFeriadosAPI();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(listFeriados);
    }

    private List<TableMap> buscarFeriadosAPI(){
        HttpURLConnection connection = null;

        try{
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            LocalDate dtAtual = LocalDate.now();
            Integer anoAtual = dtAtual.year
            URL url = new URL("https://brasilapi.com.br/api/feriados/v1/" + anoAtual.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            String responseCode = connection.getResponseCode().toString();

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"))
            );

            String inputLine;
            StringBuffer json = new StringBuffer();

            while ((inputLine = bufferedReader.readLine()) != null) {
                json.append(inputLine);
            }
            bufferedReader.close();

            List<TableMap> tmFeriados = JSonMapperCreator.create().read(json.toString(), new TypeReference<List<TableMap>>() {});

            return tmFeriados;


        }catch (SSLHandshakeException err) {
            throw new RuntimeException(
                    "Não foi possível se conectar ao servidor BrasilAPI.", err
            );
        }catch (IOException err) {
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