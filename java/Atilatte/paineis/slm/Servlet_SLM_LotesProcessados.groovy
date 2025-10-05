package Atilatte.paineis.slm;

import br.com.multitec.utils.collections.TableMap;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import sam.server.samdev.relatorio.UiDto
import br.com.multitec.utils.Utils
import org.springframework.http.MediaType
import sam.server.samdev.utils.Parametro;

public class Servlet_SLM_LotesProcessados extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "SLM - Lotes Processados";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
    }

    @Override
    public ResponseEntity<Object> executar() {
        List<String> mensagens = new ArrayList();
        List<TableMap> lotes = buscarLotes();

        for(lote in lotes){
            Long idLote = lote.getInteger("idLote");
            String nomeLote = lote.getString("lote")
            String statusLote = verificarStatusLote(idLote, nomeLote);
            String statusColeta;

            if(statusLote.contains("totalmente") || statusLote.contains("parcialmente")){
                statusColeta = verificaStatusColeta(idLote)
            }else{
                statusColeta = " Coleta não iniciada."
            }

            statusLote = statusLote + statusColeta;

            mensagens.add(statusLote)
        }

        Map<String,Object> valores = Utils.map(
                "mensagens",mensagens
        )

        UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.slm.Recurso_SLM_LotesProcessados.html",valores)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
    }
    private List<TableMap> buscarLotes(){
        String sql = "select bfb01lote as lote, bfb01id as idLote from bfb01 order by bfb01lote "

        return getAcessoAoBanco().buscarListaDeTableMap(sql)
    }
    private String verificarStatusLote(Long idLote, String nomeLote){
        Integer qtdPedidosNaoProcessados = 0;
        Integer qtdPedidosProcessados = 0;

        String sql = "select bfb0101romproc from bfb0101 where bfb0101lote = :idLote";

        List<Integer> listRomProc = getAcessoAoBanco().obterListaDeInteger(sql, Parametro.criar("idLote", idLote));

        for(status in listRomProc){
            if (status == 0){
                qtdPedidosNaoProcessados++
            }else{
                qtdPedidosProcessados++
            }
        }

        String statusLote = ""
        if(qtdPedidosNaoProcessados == listRomProc.size()){
            statusLote = "Lote " + nomeLote + " não foi processado."
        }else if(qtdPedidosProcessados == listRomProc.size()){
            statusLote = "Lote " + nomeLote + " totalmente processado."
        }else if(qtdPedidosNaoProcessados >= 1){
            statusLote = "Lote " + nomeLote + " processado parcialmente."
        }

        return statusLote;
    }
    private String verificaStatusColeta(Long idLote){
       List<TableMap> itensRomaneio = buscarItensRomaneio(idLote);
        String statusColeta;

        Integer countColetados = 0;
        Integer countItensRomaneio = 0;

        for (item in itensRomaneio){
            Integer ajustado = item.getInteger("ajustado");
            Integer realizaColeta = item.getInteger("realizaColeta");
            countItensRomaneio++;

            if (ajustado == 1 || realizaColeta == 0 ) countColetados++

        }

        if(countColetados == itensRomaneio.size()){
            statusColeta = " Coleta finalizada."
        }else if(countColetados >= 1){
            statusColeta = " Coleta iniciada."
        }else{
            statusColeta = " Coleta não iniciada."
        }

        return statusColeta;
    }
    private List<TableMap> buscarItensRomaneio(Long idLote){
        String whereLote =  "where bfb01id = :idLote ";
        Parametro parametroLote = Parametro.criar("idLote", idLote);

        String sql = "select bfa01011ajustado as ajustado, cast(abm0101json ->> 'realiza_coleta' as integer) as realizaColeta "+
                "from bfa01 " +
                "inner join abb01 as romaneio on romaneio.abb01id = bfa01central " +
                "inner join bfa0101 on bfa0101rom = bfa01id " +
                "inner join bfa01011 on bfa01011item = bfa0101id " +
                "inner join bfb0101 on bfb0101central = bfa01docscv " +
                "inner join bfb01 on bfb01id = bfb0101lote " +
                "inner join eaa0103 on eaa0103id = bfa0101item " +
                "inner join eaa01 on eaa01id = eaa0103doc " +
                "inner join abb01 as pedido on pedido.abb01id = eaa01central " +
                "inner join abm01 on abm01id = eaa0103item " +
                "inner join abm0101 on abm0101item = abm01id " +
                whereLote+
                "order by pedido.abb01num ";



        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroLote);
    }
}
//meta-sis-eyJkZXNjciI6IlNMTSAtIExvdGVzIFByb2Nlc3NhZG9zIiwidGlwbyI6InNlcnZsZXQiLCJ3IjoxMiwiaCI6MTIsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9