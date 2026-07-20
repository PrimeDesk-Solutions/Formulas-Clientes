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

public class CGS_Compor_Rupturas_Repositorio extends ServletBase {

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

        List<TableMap> listRupturas = buscarDadosRuptura();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(listRupturas);
    }

    private List<TableMap> buscarDadosRuptura(){
        HttpURLConnection connection = null;

        try{
            String sql = "SELECT * FROM rupturas";

            List<TableMap> listRupturas = getAcessoAoBanco().buscarListaDeTableMap(sql);

            if(listRupturas == null || listRupturas.size() == 0) throw new ValidacaoException("Não foi encontrado registros de ruptura.")

            return listRupturas;
        } catch (Exception err){
            interromper(err.getMessage())
        }

    }
}