package Atilatte.paineis.scf

import br.com.multitec.utils.collections.TableMap;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import sam.server.samdev.relatorio.UiDto
import br.com.multitec.utils.Utils
import org.springframework.http.MediaType

public class Servlet_SCF_PainelMovimentosEmAberto extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "SCF - Movimentos Em Aberto";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
    }

    @Override
    public ResponseEntity<Object> executar() {
        List<String> mensagens = new ArrayList();

        List<TableMap> movimentos = buscarMovimentosEmAberto();

        for(movimento in movimentos){
            Integer numMov = movimento.getInteger("daa0102movim");
            String codBanco = movimento.getString("abf01codigo");
            String nomeBanco = movimento.getString("abf01nome");

            String msg = "O movimento " + numMov.toString() + " do banco " + codBanco + " - " + nomeBanco + " encontra-se em aberto."

            mensagens.add(msg)
        }

        Map<String,Object> valores = Utils.map(
                "mensagens",mensagens
        )
        UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.scf.Recurso_SCF_PainelMovimentosEmAberto.html",valores)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
    }

    private List<TableMap> buscarMovimentosEmAberto(){
        String sql = "select distinct daa0102movim, abf01codigo, abf01nome " +
                        "from daa0102 " +
                        "inner join abf01 on abf01id = daa0102banco " +
                        "where daa0102remdata is null " +
                        "order by daa0102movim"

        return getAcessoAoBanco().buscarListaDeTableMap(sql)
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIE1vdmltZW50b3MgRW0gQWJlcnRvIiwidGlwbyI6InNlcnZsZXQiLCJ3IjoxMiwiaCI6MTIsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9