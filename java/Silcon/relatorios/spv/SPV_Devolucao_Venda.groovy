package Silcon.relatorios.spv

import groovy.swing.table.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap

import java.time.LocalDate
import java.util.Map;
import java.util.HashMap;

public class SPV_Devolucao_Venda extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPV - Devolucao de Venda";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        Long eaa01id = get("eaa01id");

        List<TableMap> documentos = buscarDocumentosDevolvidosPorID(eaa01id);
        List<TableMap> itens = new ArrayList<>();
        List<TableMap> docsRef = new ArrayList<>();

        for (documento in documentos) {
            Long idDocumento = documento.getLong("eaa01id");
            List<TableMap> listItens = buscarItensDoc(idDocumento);
            List<TableMap> listDocsReferenciados = buscarDocumentosReferenciados(idDocumento);

            for (item in listItens) {
                item.put("key", idDocumento);

                itens.add(item);
            }

            for (docs in listDocsReferenciados) {
                docs.put("key", idDocumento);
                docsRef.add(docs);
            }

            BigDecimal totalDocDevolvido = documento.getBigDecimal_Zero("vlrDoc");
            BigDecimal totalDocReferenciados = buscarTotalDocsReferenciados(idDocumento);
            BigDecimal totalDevolver = totalDocReferenciados.subtract(totalDocDevolvido);

            documento.put("totalDevolver", totalDevolver);

            documento.put("key", idDocumento);
        }

        adicionarParametro("EMPRESA", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        adicionarParametro("DATA_IMPRESSAO", LocalDate.now());

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(documentos);
        dsPrincipal.addSubDataSource("dsItens", itens, "key", "key");
        dsPrincipal.addSubDataSource("dsDocs", docsRef, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPV_Devolucao_Venda_s1"));
        adicionarParametro("StreamSub2", carregarArquivoRelatorio("SPV_Devolucao_Venda_s2"));

        return gerarPDF("SPV_Devolucao_Venda", dsPrincipal);

    }

    private List<TableMap> buscarDocumentosDevolvidosPorID(Long eaa01id) {
        String sql = "SELECT eaa01id, abe01nome AS nomeEntidade, abe0101endereco AS enderecoEntidade, abe0101complem AS complemento, abe0101bairro AS bairroEntidade, " +
                "aag0201nome AS cidadeEntidade, aag02uf AS ufEntidade, abe01ni AS numInscricao, abe0101cep AS cepEntidade, abe0101ddd1 AS ddd1, abe0101fone1 AS fone1, eaa01totDoc AS vlrDoc, abb01num AS numDoc, " +
                "aah01codigo AS codTipoDoc, aah01nome AS nomeTipoDoc, aab10nome AS user " +
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN abe01 ON abe01id = abb01ent " +
                "LEFT JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                "LEFT JOIN aag0201 ON aag0201id = abe0101municipio " +
                "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                "LEFT JOIN aab10 ON aab10id = abb01operUser " +
                "WHERE eaa01id = :eaa01id ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
    }

    private List<TableMap> buscarItensDoc(Long eaa01id) {
        String sql = "SELECT eaa0103seq AS sequencia, abm01codigo AS codItem, abm01descr AS descrItem, eaa0103qtComl AS qtd, aam06codigo AS umu, " +
                "eaa0103unit AS unitario, CAST(eaa0103json ->> 'desconto' AS NUMERIC(18,6)) AS desconto, eaa0103total AS totalItem " +
                "FROM eaa01 " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "LEFT JOIN aam06 ON aam06id = abm01umu " +
                "WHERE eaa01id = :eaa01id " +
                "ORDER BY eaa0103seq ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
    }

    private List<TableMap> buscarDocumentosReferenciados(Long eaa01id) {
        String sql = "SELECT DISTINCT abb01ref.abb01num AS numDoc, aah01ref.aah01codigo AS codTipoDoc, aah01ref.aah01nome AS nomeTipoDoc " +
                "FROM eaa01 AS eaa01dev " +
                "INNER JOIN abb01 AS abb01dev ON abb01dev.abb01id = eaa01dev.eaa01central " +
                "INNER JOIN aah01 AS aah01dev ON aah01dev.aah01id = abb01dev.abb01tipo " +
                "INNER JOIN eaa0103 AS eaa0103dev ON eaa0103dev.eaa0103doc = eaa01dev.eaa01id " +
                "INNER JOIN eaa01033 ON eaa01033item = eaa0103dev.eaa0103id " +
                "INNER JOIN eaa0103 AS eaa0103ref ON eaa0103ref.eaa0103id = eaa01033itemDoc " +
                "INNER JOIN eaa01 AS eaa01ref ON eaa01ref.eaa01id = eaa0103ref.eaa0103doc " +
                "INNER JOIN abb01 AS abb01ref ON abb01ref.abb01id = eaa01ref.eaa01central " +
                "INNER JOIN aah01 AS aah01ref ON aah01ref.aah01id = abb01ref.abb01tipo " +
                "WHERE eaa01dev.eaa01id = :eaa01id " +
                "ORDER BY abb01ref.abb01num"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
    }

    private BigDecimal buscarTotalDocsReferenciados(Long eaa01id) {
        String sql = "SELECT SUM(eaa0103ref.eaa0103totDoc) AS total " +
                " FROM eaa01 AS eaa01dev " +
                " INNER JOIN abb01 AS abb01dev ON abb01dev.abb01id = eaa01dev.eaa01central " +
                " INNER JOIN aah01 AS aah01dev ON aah01dev.aah01id = abb01dev.abb01tipo " +
                " INNER JOIN eaa0103 AS eaa0103dev ON eaa0103dev.eaa0103doc = eaa01dev.eaa01id " +
                " INNER JOIN eaa01033 ON eaa01033item = eaa0103dev.eaa0103id " +
                " INNER JOIN eaa0103 AS eaa0103ref ON eaa0103ref.eaa0103id = eaa01033itemDoc " +
                " INNER JOIN eaa01 AS eaa01ref ON eaa01ref.eaa01id = eaa0103ref.eaa0103doc " +
                " INNER JOIN abb01 AS abb01ref ON abb01ref.abb01id = eaa01ref.eaa01central " +
                " INNER JOIN aah01 AS aah01ref ON aah01ref.aah01id = abb01ref.abb01tipo " +
                " WHERE eaa01dev.eaa01id = :eaa01id ";

        BigDecimal totalDocReferenciados = getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("eaa01id", eaa01id));

        return totalDocReferenciados == null || totalDocReferenciados.compareTo(new BigDecimal(0)) == 0 ? new BigDecimal(0) : totalDocReferenciados;
    }
}