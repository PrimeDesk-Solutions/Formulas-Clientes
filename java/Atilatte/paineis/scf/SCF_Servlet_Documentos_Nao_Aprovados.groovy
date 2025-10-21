package Atilatte.paineis.scf;

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


public class SCF_Servlet_Documentos_Nao_Aprovados extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "SCF - Documentos não Aprovados";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
    }

    @Override
    public ResponseEntity<Object> executar() {
        List<TableMap> mensagens = new ArrayList();
        List<TableMap> documentos = buscarDocumentosNaoAprovados();

        for(documento in documentos){
            Integer numDoc = documento.getInteger("numDoc");
            String codEntidade = documento.getString("codEntidade");
            String nomeEntidade = documento.getString("naEntidade");
            String dtVcto = documento.getDate("dtVcto").format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
            BigDecimal valor = documento.getBigDecimal_Zero("valor");

            String mensagem = "Documento " + numDoc.toString() + " da entidade " + codEntidade + " " + nomeEntidade + " com valor RS "+ valor + " e vencimento " + dtVcto + " está aguardando aprovação.";

            mensagens.add(mensagem);
        }
        Map<String, Object> valores = Utils.map("mensagens", mensagens)

        UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.scf.SCF_Recurso_Documentos_Nao_Aprovados.html", valores)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
    }

    private List<TableMap> buscarDocumentosNaoAprovados() {
        String whereEmpresa = "AND daa01gc = :idEmpresa ";
        String whereDtAprov = "AND abb0103data IS NULL ";

        String sql = "SELECT abb01num AS numDoc, abe01codigo AS codEntidade, abe01na AS naEntidade, daa01dtvctor AS dtVcto,  " +
                "daa01valor AS valor, abb0103data AS dtAprovacao, abb0103hora AS horaAprov, aab10user AS user " +
                "FROM daa01 " +
                "INNER JOIN abb01 ON abb01id = daa01central " +
                "INNER JOIN abe01 ON abe01id = abb01ent " +
                "INNER JOIN abb0103 ON abb0103central = abb01id " +
                "LEFT JOIN aab10 ON aab10id = abb0103user " +
                "WHERE TRUE " +
                whereEmpresa +
                whereDtAprov+
                "ORDER BY abb01num, daa01dtvctor, daa01valor "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id()));
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgTsOjbyBBcHJvdmFkb3MiLCJ0aXBvIjoic2VydmxldCIsInciOjEyLCJoIjoxMiwicmVzaXplIjoidHJ1ZSIsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=