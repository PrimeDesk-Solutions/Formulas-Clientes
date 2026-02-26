package Silcon.servlet;


import br.com.multiorm.ColumnType
import br.com.multitec.utils.ValidacaoException;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.util.stream.Collectors
import com.fasterxml.jackson.core.type.TypeReference;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.jackson.JSonMapperCreator
import java.time.format.DateTimeFormatter;

public class Verificar_Limite_Credito_Entidade extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "Verifica Limite Crédito";
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
        TableMap jsonAbe01 = buscarInformacoesLimiteCreditoEntidade(idEntidade);

        if(jsonAbe01 == null || jsonAbe01.size() == 0) return;

        Boolean limiteCreditoExedido = verificarLimiteDeCredito(jsonAbe01, idEntidade);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(limiteCreditoExedido);
    }

    private TableMap buscarInformacoesLimiteCreditoEntidade(Long idEntidade) {
        return getSession().createQuery("SELECT abe01json FROM abe01 WHERE abe01id = :idEntidade")
                                            .setParameter("idEntidade", idEntidade)
                                            .getUniqueResult(ColumnType.JSON);
    }

    private Boolean verificarLimiteDeCredito(TableMap jsonAbe01, Long idEntidade){
        BigDecimal vlrLimiteCredito = jsonAbe01.getBigDecimal_Zero("vlr_lim_credito");
        LocalDate dataAtual = LocalDate.now();
        LocalDate dtVencLimCredito = jsonAbe01.getDate("dt_vcto_lim_credito");

        if(dtVencLimCredito == null) interromper("Não foi informado data de vencimento de limite de crédito no cadastro da entidade.");

        if(vlrLimiteCredito >= 0){
            if(dtVencLimCredito < dataAtual){ // Data de vencimento de crédito menor que data atual, significa expirou
                interromper("Data de vencimento do limite de crédito do cliente venceu em " + dtVencLimCredito.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + ".")
            }

            BigDecimal vlrDocumentosReceber = somarDocsAReceber(idEntidade);

            // Soma todos os documentos emitidos para a entidade com SCF = 2-Batch
            BigDecimal valorDocumentosEmitidos = buscarSomaDocumentosEmitidos(idEntidade);

            // Calcula o valor total devedor
            BigDecimal valorTotalDevedor = vlrDocumentosReceber + valorDocumentosEmitidos;

            // Se o total devedor do cliente for maior que o limite de crédito, significa que houve inconsistências e precisa ser analisada
            return valorTotalDevedor > vlrLimiteCredito
        }
    }
    private BigDecimal somarDocsAReceber(Long idEntidade){

        String sql = " SELECT COALESCE(SUM(daa01valor), 0.00) AS valor" +
                        " FROM daa01 " +
                        " INNER JOIN abb01 ON abb01id = daa01central " +
                        " WHERE abb01quita = 0 " +
                        " AND daa01rp = 0 " +
                        " AND abb01ent = :idEntidade"

        return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idEntidade", idEntidade));
    }

    private BigDecimal buscarSomaDocumentosEmitidos(Long idEntidade){

        String sql = " SELECT COALESCE(SUM(eaa01totDoc),0.00) AS totalGeral " +
                " FROM eaa01 " +
                " INNER JOIN abb01 ON abb01id = eaa01central " +
                " INNER JOIN abd01 ON abd01id = eaa01pcd " +
                " LEFT JOIN abb10 ON abb10id = abd01opercod " +
                " WHERE abb10tipoCod = 1 " +
                " AND eaa01esMov = 1 " +
                " AND eaa01clasDoc = 1 " +
                " AND eaa01cancData IS NULL " +
                " AND eaa01iSCF = 2 " +
                " AND abb01ent = :idEntidade"

        getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idEntidade", idEntidade))

    }
}