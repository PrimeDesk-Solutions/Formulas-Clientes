package Silcon.servlet

import br.com.multiorm.ColumnType;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.stream.Collectors
import com.fasterxml.jackson.core.type.TypeReference;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.jackson.JSonMapperCreator


public class Buscar_Titulos_Vencidos_Entidade extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "Busca Titulos Vencidos Entidade"
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Object> executar() {
        String req = httpServletRequest.getReader().lines().collect(Collectors.joining());
        TableMap body = JSonMapperCreator.create().read(req, new TypeReference<TableMap>() {});
        Long idEntidade = body.get("abe01id");

        Integer qtdTitulosVencidos = buscarTitulosVencidos(idEntidade);
        Boolean existemTitulosVencidos = qtdTitulosVencidos > 0

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(existemTitulosVencidos);
    }
    private Integer buscarTitulosVencidos(Long idEntidade){
        return getSession().createQuery("SELECT COUNT(daa01id) AS idDocVencidos " +
                                "FROM daa01 " +
                                "INNER JOIN abb01 ON abb01id = daa01central " +
                                "WHERE daa01rp = 0 " +
                                "AND abb01quita = 0 " +
                                "AND daa01dtVctoR < current_date " +
                                "AND abb01ent = :idEntidade")
                                .setMaxResult(1)
                                .setParameter("idEntidade", idEntidade)
                                .getUniqueResult(ColumnType.INTEGER);
    }
}
