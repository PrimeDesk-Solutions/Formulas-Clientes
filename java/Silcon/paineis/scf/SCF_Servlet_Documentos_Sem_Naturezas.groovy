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


public class SCF_Servlet_Documentos_Sem_Naturezas extends ServletBase {

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
        String mensagem = "";
        for(documento in documentos){
            Integer numDoc = documento.getInteger("abb01num");
            BigDecimal valor = documento.getBigDecimal_Zero("dab10valor");
            Long idDepartamento = documento.getLong("dab1001id");
            Long idNatureza = documento.getLong("dab10011id");
            String dataLcto = documento.getString("data");
            String codEntidade = documento.getString("abe01codigo")
            String nomeEntidade = documento.getString("abe01nome");

            if(idDepartamento == null && idNatureza == null){
                mensagem =  "Documento Nº " + numDoc.toString() + " no valor de RS " + valor + "do cliente " + codEntidade + " - " + nomeEntidade + " lançado em " + dataLcto + " encontra-se sem departamento e natureza";
            } else {
                mensagem =  "Documento Nº " + numDoc.toString() + " no valor de RS " + valor + "do cliente " + codEntidade + " - " + nomeEntidade + " lançado em " + dataLcto + " encontra-se natureza";
            }

            mensagens.add(mensagem);
        }
        Map<String, Object> valores = Utils.map("mensagens", mensagens)

        UiDto dto = buscarComponenteCustomizado("Silcon.paineis.scf.SCF_Recurso_Documentos_Sem_Naturezas.html", valores)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
    }

    private List<TableMap> buscarDocumentosSemNaturezas() {

        String sql = "SELECT abb01num, dab10valor, dab1001id, dab10011id, abf20codigo, TO_CHAR(dab10data, 'dd/mm/yyyy') AS data, " +
                    "abe01codigo, abe01nome " +
                    "FROM dab10 " +
                    "INNER JOIN abb01 ON abb01id = dab10central " +
                    "LEFT JOIN dab1001 ON dab1001lct = dab10id " +
                    "LEFT JOIN dab10011 ON dab10011depto = dab1001id " +
                    "INNER JOIN abf20 ON abf20id = dab10plf " +
                    "INNER JOIN abe01 ON abe01id = abb01ent " +
                    "WHERE dab10011id IS NULL " +
                    "AND abf20id NOT IN (35527340,35527342, 35576745, 35576747, 35527569, 35576746) " +
                    "ORDER BY dab10data, abb01num, dab10valor "

        return getAcessoAoBanco().buscarListaDeTableMap(sql);
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgTsOjbyBBcHJvdmFkb3MiLCJ0aXBvIjoic2VydmxldCIsInciOjEyLCJoIjoxMiwicmVzaXplIjoidHJ1ZSIsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=