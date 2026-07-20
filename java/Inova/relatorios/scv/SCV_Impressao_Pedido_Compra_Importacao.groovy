package Inova.relatorios.scv

import br.com.multitec.utils.ValidacaoException;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap
import java.nio.file.Path;
import java.nio.file.Paths;


import java.util.Map;
import java.util.HashMap;

public class SCV_Impressao_Pedido_Compra_Importacao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Impressão Pedido Compra Importação";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        Long eaa01id = getLong("eaa01id");

        List<TableMap> dados = buscarDocumentosPorID(eaa01id);
        List<TableMap> listItens = new ArrayList<>();

        for(dado in dados){
            Long idDoc = dado.getLong("eaa01id");

            List<TableMap> itens = buscarItensDoc(idDoc);
            for(item in itens){
                item.put("key", idDoc);

                listItens.add(item)
            }

            dado.put("key", idDoc);
        }

        preencherParametrosRelatorio();

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsItens", listItens, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCV_Pedido_Compra_Importacao_S1"));

        return gerarPDF("SCV_Pedido_Compra_Importacao", dsPrincipal);
    }
    private List<TableMap> buscarDocumentosPorID(Long eaa01id){
        String sql = " SELECT DISTINCT eaa01id, aah01nome AS nomeTipoDoc, abb01num AS numDoc, ent.abe01codigo AS codEntidade, ent.abe01nome AS nomeEntidade, eaa0101endereco AS enderecoEntidade, eaa0101numero AS numEnderecoEntidade, " +
                "aag0201nome AS municipioEntidade, aag02uf AS ufEntidade, ent.abe01ni AS cnpjEntidade, eaa0102pcObs AS obsContato, eaa0101cep AS cepEntidade, " +
                "eaa0101bairro AS bairroEntidade, eaa0101ddd AS ddEntidade, eaa0101fone AS foneEntidade, ent.abe01ie AS ieEntidade, abe30codigo AS codCondPgto, abe30nome AS condPgto, " +
                "eaa01totItens AS totItens, CAST(eaa01json ->> 'ipi' AS NUMERIC(18,6)) AS totIPI, CAST(eaa01json ->> 'icms' AS NUMERIC(18,6)) AS totICMS, " +
                "CAST(eaa01json ->> 'icms_st' AS NUMERIC(18,6)) AS totICMSST, CAST(eaa01json ->> 'frete_dest' AS NUMERIC(18,6)) AS totFrete, CAST(eaa01json ->> 'desconto' AS NUMERIC(18,6)) AS totDesconto, " +
                "eaa01totDoc AS totalDocumento, eaa01obsContrib, eaa01obsUsoInt, desp.abe01na AS nomeDespacho, aab10user AS usuario, abb01data, eaa0102frete, eaa01esMov, abb0103data AS dtAprovacao " +
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "LEFT JOIN abb0103 ON abb0103central = abb01id "+
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN abe01 AS ent ON ent.abe01id = abb01ent " +
                "INNER JOIN eaa0101 ON eaa0101doc = eaa01id AND eaa0101principal = 1 " +
                "LEFT JOIN abe30 ON abe30id = eaa01cp " +
                "LEFT JOIN aag0201 ON aag0201id = eaa0101municipio " +
                "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "LEFT JOIN abe01 AS desp ON desp.abe01id = eaa0102despacho "+
                "LEFT JOIN aab10 ON aab10id = abb01operUser "+
                "WHERE eaa01id = :eaa01id ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));

    }
    private List<TableMap> buscarItensDoc(Long eaa01id){
        String sql = "SELECT eaa0103pedAtend, eaa0103seq, abm01codigo, eaa0103complem, aam06codigo, eaa0103qtComl, eaa0103unit, eaa0103total, " +
                "CAST(eaa0103json ->> 'aliq_icms' AS NUMERIC(18,6)) AS aliqICMS, CAST(eaa0103json ->> 'aliq_ipi' AS NUMERIC(18,6)) AS aliqIPI,  " +
                "eaa0103dtEntrega, eaa0103pedAtend, COALESCE(CAST(eaa0103json ->> 'tx_financ' AS NUMERIC(18,6)), 0.00) AS txFinanc "+
                "FROM eaa0103 " +
                "INNER JOIN abm01 ON abm01id = eaa0103item "+
                "LEFT JOIN aam06 ON aam06id = abm01umu " +
                "WHERE eaa0103doc = :eaa01id " +
                "ORDER BY eaa0103seq "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
    }
    private void preencherParametrosRelatorio(){

        TableMap dadosEmpresa = buscarDadosEmpresa(obterEmpresaAtiva().getAac10id());

        adicionarParametro("EMPRESA", dadosEmpresa.getString("aac10rs"));
        adicionarParametro("ENDERECO_EMPRESA", dadosEmpresa.getString("aac10endereco") + "," + dadosEmpresa.getString("aac10numero") + "," + dadosEmpresa.getString("aac10bairro"));
        adicionarParametro("MUNICIPIO_EMPRESA", dadosEmpresa.getString("municipio") + " - " + dadosEmpresa.getString("estado"));
        adicionarParametro("CNPJ_EMPRESA", dadosEmpresa.getString("aac10ni"));
        adicionarParametro("FONE_EMPRESA", "(" + dadosEmpresa.getString("aac10dddFone") + ") " + " " + dadosEmpresa.getString("aac10fone"));
        adicionarParametro("CEP_EMPRESA", dadosEmpresa.getString("aac10cep"));
        adicionarParametro("INSCRICAO_ESTAD_EMPRESA", dadosEmpresa.getString("aac1002ie"));
        adicionarParametro("SITE_EMPRESA", "www.inovatron.com");
    }
    private TableMap buscarDadosEmpresa(Long idEmpresa){
        String sql = "SELECT aac10rs, aac10endereco, aac10numero, aac10bairro, " +
                "aag0201nome AS municipio, aag02uf AS estado, aac1002ie, " +
                "aac10ni, aac10dddFone, aac10fone, aac10cep " +
                "FROM aac10 " +
                "INNER JOIN aag0201 ON aag0201id = aac10municipio "+
                "INNER JOIN aag02 ON aag02id = aag0201uf " +
                "LEFT JOIN aac1002 ON aac1002empresa = aac10id "+
                "WHERE aac10id = :idEmpresa";

        return getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("idEmpresa", idEmpresa));
    }
}