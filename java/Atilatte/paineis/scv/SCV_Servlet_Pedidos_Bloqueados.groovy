package Atilatte.paineis.scv;


import br.com.multitec.utils.collections.TableMap;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.aa.Aac10
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import sam.server.samdev.relatorio.UiDto
import br.com.multitec.utils.Utils
import org.springframework.http.MediaType
import sam.server.samdev.utils.Parametro;

public class SCV_Servlet_Pedidos_Bloqueados extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "SCV - Pedidos Bloqueados"
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
    }

    @Override
    public ResponseEntity<Object> executar() {
        List<String> mensagens = new ArrayList();

        List<TableMap> pedidos = buscarPedidosBloqueados();

        for(pedido in pedidos ){
            Integer numDoc = pedido.getInteger("numDoc");
            String codEntidade = pedido.getString("codigoEntidade");
            String naEntidade = pedido.getString("naEntidade");

            String mensagem = "O pedido " + numDoc.toString() + " do cliente "+codEntidade + " - " + naEntidade + " encontra-se bloqueado.";

            mensagens.add(mensagem);

        }
        Map<String,Object> valores = Utils.map(
                "mensagens",mensagens
        )

        UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.scv.SCV_Recurso_Pedidos_Bloqueados.html",valores)

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
    }

    private List<TableMap> buscarPedidosBloqueados(){

        String whereBloqueado = "where eaa01bloqueado = 1 ";
        String whereEmpresa =  "and eaa01gc = :idEmpresa ";
        Aac10 empresaAtiva = obterEmpresaAtiva();

        String sql = "select abb01num as numDoc, abe01codigo as codigoEntidade, abe01na as naEntidade " +
                "from eaa01 " +
                "inner join abb01 on abb01id = eaa01central " +
                "inner join abe01 on abe01id = abb01ent " +
                whereBloqueado +
                whereEmpresa +
                "order by abe01codigo, abb01num";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idEmpresa", empresaAtiva.getAac10id()))
    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MgQmxvcXVlYWRvcyIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==