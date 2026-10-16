package Silcon.paineis.scf;

import br.com.multitec.utils.collections.TableMap;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.aa.Aac10
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import sam.server.samdev.relatorio.UiDto
import br.com.multitec.utils.Utils
import org.springframework.http.MediaType
import sam.server.samdev.utils.Parametro

import java.time.format.DateTimeFormatter;


public class SCF_Documentos_Sem_Naturezas extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "SCF - Documentos sem Naturezas";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
    }

    @Override
    public ResponseEntity<Object> executar() {
        List<TableMap> mensagens = new ArrayList();
        List<TableMap> documentos = buscarDocumentosSemNaturezas();

        for(documento in documentos){
            Integer numDoc = documento.getInteger("abb01num");
            BigDecimal valor = documento.getBigDecimal_Zero("dab10valor");

            String mensagem = "Documento " + numDoc.toString() + " no valor de RS " + valor + " encontra-se sem natureza" ;

            mensagens.add(mensagem);
        }
        Map<String, Object> valores = Utils.map("mensagens", mensagens)

        UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.scf.SCF_Recurso_Documentos_Nao_Aprovados.html", valores)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
    }

    private List<TableMap> buscarDocumentosSemNaturezas() {

        String sql = "SELECT abb01num, dab10valor " +
                    "FROM dab10 " +
                    "INNER JOIN abb01 ON abb01id = dab10central " +
                    "LEFT JOIN dab1001 ON dab1001lct = dab10id " +
                    "LEFT JOIN dab10011 ON dab10011depto = dab1001id " +
                    "WHERE dab10011id IS NULL " +
                    "ORDER BY abb01num, dab10valor"

        return getAcessoAoBanco().buscarListaDeTableMap(sql);
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgTsOjbyBBcHJvdmFkb3MiLCJ0aXBvIjoic2VydmxldCIsInciOjEyLCJoIjoxMiwicmVzaXplIjoidHJ1ZSIsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=