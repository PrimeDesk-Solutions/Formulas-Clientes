package Silcon.relatorios.spv

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SPV_Impressao_Pre_Venda extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPV - Impressão Pré Venda";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("impressao", "0");

        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> tipos = getListLong("tipo");
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> entidades = getListLong("entidade");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dtEntSai");
        Long id = getLong("ccb01id");

        List<TableMap> dados = new ArrayList<>();
        List<TableMap> listItens = new ArrayList<>();

        if(id == null){
            dados = buscarDocumentosPreVenda(numeroInicial,numeroFinal,entidades,dtEmissao)
        }else{
            dados = buscarDocumentosPreVendaById(id);

        }

        for(dado in dados){
            Long idPreVenda = dado.getLong("ccb01id");
            String nomeEntidade = dado.getString("nomeEntidade").toUpperCase();
            String dddConsumidor = dado.getString("dddConsumidor");
            String foneConsumidor = dado.getString("foneConsumidor");
            List<TableMap> itensPreVenda = buscarItensPreVenda(idPreVenda);

            if(dado.getInteger("ccb01status") == 2){
                dado.put("tipoDoc", "ORÇAMENTO");
            }else{
                dado.put("tipoDoc", "VENDA")
            }

            for(item in itensPreVenda){
                Integer entrega = item.getInteger("entrega");

                if(entrega == 0){
                    item.put("entrega", "RETIRAR");
                }else{
                    item.put("entrega", "ENTREGAR");
                }
                item.put("key", idPreVenda);

                listItens.add(item);
            }

            TableMap somaTotalPreVenda = buscarSomaItensPreVenda(idPreVenda);

            dado.putAll(somaTotalPreVenda);
            if(nomeEntidade == "CONSUMIDOR"){
                dado.put("dddEntidade", dddConsumidor);
                dado.put("foneEntidade", foneConsumidor);
            }
            dado.put("key", idPreVenda);
        }

        adicionarParametro("empresa", obterEmpresaAtiva().getAac10rs());
        adicionarParametro("telefoneEmpresa", "Telefone: " + "(" + obterEmpresaAtiva().getAac10dddFone() + ")" + " " + obterEmpresaAtiva().getAac10fone());
        adicionarParametro("dataIni", dtEmissao == null ? null : dtEmissao[0].format("dd/MM/yyyy"));
        adicionarParametro("dataFim", dtEmissao == null ? null : dtEmissao[1].format("dd/MM/yyyy"));

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsItens", listItens, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPV_Impressao_Pre_Venda_S1"));

        return gerarPDF("SPV_Impressao_Pre_Venda", dsPrincipal);

    }

    private List<TableMap> buscarDocumentosPreVenda(Integer numeroInicial, Integer numeroFinal, List<Long> entidades,LocalDate[] dtEmissao){

        String whereEmpresa = "WHERE ccb01gc = :idEmpresa ";
        String whereNumDoc = numeroInicial != null && numeroFinal != null ? "AND ccb01num BETWEEN :numeroInicial AND :numeroFinal " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "AND abe01id IN (:entidades) " : "";
        String whereDtEmissao = dtEmissao != null ? "AND ccb01data BETWEEN :dtInicial AND :dtFinal " : "";

        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroNumDocIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null;
        Parametro parametroNumDocFin = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null;
        Parametro parametroEntidade = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroDtInicial = dtEmissao != null ? Parametro.criar("dtInicial", dtEmissao[0]) : null;
        Parametro parametroDtFinal = dtEmissao != null ? Parametro.criar("dtFinal", dtEmissao[1]) : null;

        String sql = "SELECT ccb01id, ccb01num AS numDoc, abe01nome AS nomeEntidade, abe0101endereco AS enderecoEntidade, " +
                "abe0101numero AS numEndEntidade, abe0101complem AS complemEntidade, abe0101bairro AS bairroEntidade, " +
                "abe0101cep AS cepEntidade, abe0101ddd1 AS dddEntidade, abe0101fone1 AS foneEntidade, aab10user AS usuario, " +
                "ccb01obs AS observacao, ccb01eeDdd1 AS dddConsumidor, ccb01eeFone1 AS foneConsumidor, ccb01comprador AS comprador, ccb01status " +
                "FROM ccb01 " +
                "INNER JOIN abe01 ON abe01id = ccb01ent " +
                "LEFT JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                "LEFT JOIN aag0201 ON aag0201id = abe0101municipio  " +
                "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                "INNER JOIN aab10 ON aab10id = ccb01user " +
                whereEmpresa +
                whereNumDoc +
                whereEntidades +
                whereDtEmissao +
                "ORDER BY ccb01num";


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroNumDocIni, parametroNumDocFin,parametroEntidade, parametroDtInicial, parametroDtFinal);
    }
    private List<TableMap> buscarDocumentosPreVendaById(Long id){
        String whereId = "WHERE ccb01id = :id";
        Parametro parametroID = Parametro.criar("id", id);

        String sql = "SELECT ccb01id, ccb01num AS numDoc, abe01nome AS nomeEntidade, abe0101endereco AS enderecoEntidade, " +
                    "abe0101numero AS numEndEntidade, abe0101complem AS complemEntidade, abe0101bairro AS bairroEntidade, " +
                    "abe0101cep AS cepEntidade, abe0101ddd1 AS dddEntidade, abe0101fone1 AS foneEntidade, aab10user AS usuario, " +
                    "ccb01obs AS observacao, ccb01eeDdd1 AS dddConsumidor, ccb01eeFone1 AS foneConsumidor, ccb01comprador AS comprador, ccb01status " +
                    "FROM ccb01 " +
                    "INNER JOIN abe01 ON abe01id = ccb01ent " +
                    "LEFT JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                    "LEFT JOIN aag0201 ON aag0201id = abe0101municipio  " +
                    "LEFT JOIN aag02 ON aag02id = aag0201uf " +
                    "INNER JOIN aab10 ON aab10id = ccb01user " +
                    whereId;


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroID);
    }

    private List<TableMap> buscarItensPreVenda(Long id){
        String whereId = "WHERE ccb0101pv = :id ";
        Parametro parametroId = Parametro.criar("id", id);

        String sql = "SELECT ccb0101seq, aam06codigo AS umu, abm01codigo AS codItem, abm01descr AS naItem, ccb0101unit AS totItem, " +
                    "ccb0101desc AS desconto, ccb0101totDoc AS totDoc, ccb0101qtComl AS qtd, ccb0101entregar AS entrega " +
                    "FROM ccb0101 " +
                    "INNER JOIN abm01 ON abm01id = ccb0101item " +
                    "LEFT JOIN aam06 ON aam06id = abm01umu "+
                    whereId +
                    "ORDER BY ccb0101seq";


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroId);

    }

    private TableMap buscarSomaItensPreVenda(Long id){
        String whereId = "WHERE ccb0101pv = :id ";
        Parametro parametroId = Parametro.criar("id", id);

        String sql = "SELECT SUM(ccb0101unit) AS totalItem, " +
                    "SUM(ccb0101desc) AS desconto, SUM(ccb0101totDoc) AS totDoc " +
                    "FROM ccb0101 " +
                    whereId;

        return getAcessoAoBanco().buscarUnicoTableMap(sql, parametroId);

    }
}
//meta-sis-eyJkZXNjciI6IlNQViAtIEltcHJlc3PDo28gUHLDqSBWZW5kYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNQViAtIEltcHJlc3PDo28gUHLDqSBWZW5kYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==