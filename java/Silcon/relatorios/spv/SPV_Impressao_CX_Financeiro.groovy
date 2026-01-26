package Silcon.relatorios.spv

import groovy.swing.table.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap;


import java.util.Map;
import java.util.HashMap;

public class SPV_Impressao_CX_Financeiro extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPV - Impressão Caixa Financeiro";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> daa01ids = get("daa01ids");

        if(daa01ids == null && daa01ids.size()) return;

        List<TableMap> listFormasPagamento = new ArrayList<>();

        List<TableMap> dados = buscarDocumentosPorID(daa01ids);

        for (dado in dados) {
            Long daa01id = dado.getLong("daa01id");
            Long idEntidade = dado.getLong("abe01id");

            TableMap tmEnderecoCobranca = buscarEnderecoCobrancaEntidade(idEntidade);

            dado.putAll(tmEnderecoCobranca);

            List<TableMap> formasPagamento = buscarFormasDePagamento(daa01id);

            for(pagamento in formasPagamento){
                pagamento.put("key", daa01id);
                listFormasPagamento.add(pagamento);
            }

            dado.put("key", daa01id);
        }

        adicionarParametro("EMPRESA", obterEmpresaAtiva().getAac10na());

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsPagamento", listFormasPagamento, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPV_Impressao_CX_Financeiro_s1"));

        return gerarPDF("SPV_Impressao_CX_Financeiro", dsPrincipal);

    }

    private List<TableMap> buscarDocumentosPorID(List<Long> daa01ids) {
        String sql = "SELECT daa01id, abe01id, abe01codigo AS codEntidade, abe01nome AS nomeEntidade, " +
                "aah01codigo AS codTipoDoc, aah01nome AS nomeTipoDoc, abb01num AS numDoc, abb01data AS dtEmissao, " +
                "daa01dtVctoN AS dtVcto, daa01dtPgto AS dtPgto, daa01liquido AS vlrDoc, " +
                "CAST(daa01json ->> 'multa' AS NUMERIC(18,6)) AS multa, " +
                "CAST(daa01json ->> 'juros' AS NUMERIC(18,6)) AS juros, " +
                "CAST(daa01json ->> 'desconto' AS NUMERIC(18,6)) AS desconto " +
                "FROM daa01 " +
                "INNER JOIN abb01 ON abb01id = daa01central " +
                "INNER JOIN abe01 ON abe01id = abb01ent " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "WHERE daa01id IN (:daa01ids)"

        Parametro parametroIds = daa01ids != null && daa01ids.size() > 0 ? Parametro.criar("daa01ids", daa01ids) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroIds);
    }

    private TableMap buscarEnderecoCobrancaEntidade(Long idEntidade) {
        TableMap tmEnderecoCobranca = getSession().createQuery("SELECT abe0101endereco, aag0201nome, aag02uf " +
                "FROM abe0101 " +
                "LEFT JOIN aag0201 ON aag0201id = abe0101municipio "+
                "LEFT JOIN aag02 ON aag02id = aag0201uf "+
                "WHERE abe0101ent = :idEntidade " +
                "AND abe0101cobranca = 1 ")
                .setMaxResult(1)
                .setParameter("idEntidade", idEntidade).getUniqueTableMap();

        return tmEnderecoCobranca != null && tmEnderecoCobranca.size() > 0 ? tmEnderecoCobranca : new TableMap();
    }

    private List<TableMap> buscarFormasDePagamento(Long daa01id) {
        String sql = "SELECT abf40descr, dab1002valor " +
                "FROM dab10 " +
                "INNER JOIN dab1002 ON dab1002lct = dab10id " +
                "INNER JOIN abf40 ON abf40id = dab1002fp " +
                "INNER JOIN abb01 ON abb01id = dab10central " +
                "INNER JOIN daa01 ON daa01central = abb01id " +
                "WHERE daa01id = :idDoc " +
                "ORDER BY abf40codigo"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", daa01id));
    }
}