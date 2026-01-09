package Silcon.relatorios.srf;

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SRF_Impressao_Documento_Interno extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "SRF - Impressão Documento Interno";
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
        def tipos = getListLong("tipo");
        def numeroInicial = getInteger("numeroInicial");
        def numeroFinal = getInteger("numeroFinal");
        def entidades = getListLong("entidade");
        def dtEmissao = getIntervaloDatas("dataEmissao");
        def dtEntradaSaida = getIntervaloDatas("dtEntSai");
        def id = getLong("eaa01id");

        List<TableMap> dados = new ArrayList<>();

        if (id == null) {
            dados = buscarDocumentos(tipos, numeroInicial, numeroFinal, entidades, dtEmissao, dtEntradaSaida);
        } else {
            dados = buscarDocumentoById(id);
        }

        List<TableMap> listItens = new ArrayList<>();

        for (dado in dados) {
            Long idDoc = dado.getLong("eaa01id");
            List<TableMap> itensDoc = buscarItensDoc(idDoc);
            List<TableMap> parcelamento = buscarParcelamentosDocumentos(idDoc);
            String obsInterno = dado.getString("obsInterno");
            Integer countItens = 0;
            Integer countParcela = 0;

            if(obsInterno != null ){
                if(obsInterno.toUpperCase().contains("COM NOTA")) dado.put("comNota", 1);
            }

            for (item in itensDoc) {
                countItens++;

                if(item.getInteger("eaa0103entrega") == 1){
                    item.put("entrega", "RETIRAR")
                }else if(item.getInteger("eaa0103entrega") == 0){
                    item.put("entrega", "ENTREGAR")
                }

                item.put("seq", countItens.toString() + "º ")
                item.put("key", idDoc);
                listItens.add(item);
            }
            for (parcela in parcelamento) {
                countParcela++;
                dado.put("parcela" + countParcela.toString(), countParcela);
                dado.put("valorParcela" + countParcela, parcela.getBigDecimal("eaa0113valor"));
                dado.put("data" + countParcela, parcela.getDate("eaa0113dtVctoN"));
                dado.putAll(parcela);
            }

            dado.put("key", idDoc);
        }

        adicionarParametro("empresa", "SILCON " + obterEmpresaAtiva().getAac10ni());
        adicionarParametro("telefoneEmpresa", "Telefone: " + "(" + obterEmpresaAtiva().getAac10dddFone() + ")" + " " + obterEmpresaAtiva().getAac10fone());
        adicionarParametro("dataIni", dtEmissao == null ? null : dtEmissao[0].format("dd/MM/yyyy"));
        adicionarParametro("dataFim", dtEmissao == null ? null : dtEmissao[1].format("dd/MM/yyyy"));

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsItens", listItens, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SRF_Impressao_Documento_Interno_S1"));

        return gerarPDF("SRF_Impressao_Documento_Interno", dsPrincipal);
    }

    private List<TableMap> buscarDocumentos(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, List<Long> entidades, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida) {

        def whereTipos = tipos != null && tipos.size() > 0 ? " and abb01tipo in (:tipos) " : ""
        def whereEntidades = entidades != null && entidades.size() > 0 ? " and abb01ent in (:entidades) " : ""
        def wheredtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? " and abb01data between :dataIni and :dataFim " : ""
        def wheredtEntradaSaida = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? " and eaa01esData between :dataEntSaidaIni and :dataEntSaidaFin " : ""
        def whereNumIni = " and abb01num >= :numeroInicial "
        def whereNumFim = " and abb01num <= :numeroFinal "
        def whereEmpresa = "AND eaa01gc = :idEmpresa ";

        def sql = "SELECT DISTINCT eaa01id, aah01codigo AS codTipoDoc, aah01nome AS nomeTipoDoc, abb01num AS numDoc, " +
                "abe01codigo AS codEntidade, abe01na AS nomeEntidade, abe0101Principal.abe0101endereco AS enderecoEntidade, abe0101Principal.abe0101numero AS numEndEntidade, " +
                "abe0101Principal.abe0101complem AS complemEntidade, abe0101Principal.abe0101bairro AS bairroEntidade, aag0201Principal.aag0201nome AS cidadeEntidade, aag02Principal.aag02uf AS ufEntidade, " +
                "abe0101Principal.abe0101cep AS cepEntidade, abe0101Principal.abe0101ddd1 AS dddEntidade, abe0101Principal.abe0101fone1 AS foneEntidade, aab10nome AS usuario, abb01data AS dtVenda, " +
                "abe30codigo AS codCondPgto, abe30nome AS descrCondPgto, eaa01totItens AS totalItem, CAST(eaa01json ->> 'desconto' AS numeric(18,6)) AS desconto, eaa01totDoc AS totDoc, " +
                "abe0101Entrega.abe0101endereco AS enderecoEntregaEnt, abe0101Entrega.abe0101bairro AS bairroEntregaEnt, abe0101Entrega.abe0101numero AS numeroEntregaEnt, aag0201Entrega.aag0201nome AS cidadeEntregaEnt, " +
                "aag02Principal.aag02uf AS ufEntregaEntidade, abe0101Entrega.abe0101cep AS cepEntregaEnt, abe0101Entrega.abe0101complem AS complemEntregaEnt, eaa01obsUsoInt AS obsInterno " +
                "FROM eaa01 \n" +
                "INNER JOIN eaa0101 ON eaa0101doc = eaa01id " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN aab10 ON aab10id = abb01operUser " +
                "INNER JOIN abe01 ON abe01id = abb01ent  " +
                "LEFT JOIN abe0101 AS abe0101Principal ON abe0101Principal.abe0101ent = abe01id AND abe0101Principal.abe0101principal = 1 " +
                "LEFT JOIN abe0101 AS abe0101Entrega ON abe0101Entrega.abe0101ent = abe01id AND abe0101Entrega.abe0101entrega = 1 " +
                "INNER JOIN aah01 ON abb01tipo = aah01id " +
                "INNER JOIN abe30 ON eaa01cp = abe30id " +
                "LEFT JOIN aag0201 AS aag0201Principal ON aag0201Principal.aag0201id = abe0101Principal.abe0101municipio " +
                "LEFT JOIN aag02 AS aag02Principal ON aag0201Principal.aag0201uf = aag02Principal.aag02id " +
                "LEFT JOIN aag0201 AS aag0201Entrega ON aag0201Entrega.aag0201id = abe0101Entrega.abe0101municipio " +
                "LEFT JOIN aag02 AS aag02Entrega ON aag0201Entrega.aag0201uf = aag02Entrega.aag02id " +
                "WHERE eaa01cancData IS NULL " +
                whereTipos +
                whereEntidades +
                wheredtEmissao +
                whereNumIni +
                whereNumFim+
                wheredtEntradaSaida +
                whereEmpresa +
                "ORDER BY abb01num"


        def parametroTipo = tipos != null && tipos.size() > 0 ? criarParametroSql("tipos", tipos) : null
        def parametroEntidade = entidades != null && entidades.size() > 0 ? criarParametroSql("entidades", entidades) : null
        def parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? criarParametroSql("dataIni", dtEmissao[0]) : null
        def parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? criarParametroSql("dataFim", dtEmissao[1]) : null
        def parametroDtEntSaiIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? criarParametroSql("dataEntSaidaIni", dtEntradaSaida[0]) : null
        def parametroDtEntSaiFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? criarParametroSql("dataEntSaidaFin", dtEntradaSaida[1]) : null
        def parametroNumIni = criarParametroSql("numeroInicial", numeroInicial);
        def parametroNumFin = criarParametroSql("numeroFinal", numeroFinal);
        def parametroEmpresa = criarParametroSql("idEmpresa", obterEmpresaAtiva().getAac10id());


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipo, parametroEntidade, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroNumIni, parametroNumFin, parametroDtEntSaiIni, parametroDtEntSaiFin, parametroEmpresa);

    }

    private List<TableMap> buscarDocumentoById(Long idDoc) {
        def sql = "SELECT DISTINCT eaa01id, aah01codigo AS codTipoDoc, aah01nome AS nomeTipoDoc, abb01num AS numDoc, " +
                "abe01codigo AS codEntidade, abe01na AS nomeEntidade, abe0101Principal.abe0101endereco AS enderecoEntidade, abe0101Principal.abe0101numero AS numEndEntidade, " +
                "abe0101Principal.abe0101complem AS complemEntidade, abe0101Principal.abe0101bairro AS bairroEntidade, aag0201Principal.aag0201nome AS cidadeEntidade, aag02Principal.aag02uf AS ufEntidade, " +
                "abe0101Principal.abe0101cep AS cepEntidade, abe0101Principal.abe0101ddd1 AS dddEntidade, abe0101Principal.abe0101fone1 AS foneEntidade, aab10nome AS usuario, abb01data AS dtVenda, " +
                "abe30codigo AS codCondPgto, abe30nome AS descrCondPgto, eaa01totItens AS totalItem, CAST(eaa01json ->> 'desconto' AS numeric(18,6)) AS desconto, eaa01totDoc AS totDoc, " +
                "abe0101Entrega.abe0101endereco AS enderecoEntregaEnt, abe0101Entrega.abe0101bairro AS bairroEntregaEnt, abe0101Entrega.abe0101numero AS numeroEntregaEnt, aag0201Entrega.aag0201nome AS cidadeEntregaEnt, " +
                "aag02Principal.aag02uf AS ufEntregaEntidade, abe0101Entrega.abe0101cep AS cepEntregaEnt, abe0101Entrega.abe0101complem AS complemEntregaEnt, eaa01obsUsoInt AS obsInterno " +
                "FROM eaa01 \n" +
                "INNER JOIN eaa0101 ON eaa0101doc = eaa01id " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN aab10 ON aab10id = abb01operUser " +
                "INNER JOIN abe01 ON abe01id = abb01ent  " +
                "LEFT JOIN abe0101 AS abe0101Principal ON abe0101Principal.abe0101ent = abe01id AND abe0101Principal.abe0101principal = 1 " +
                "LEFT JOIN abe0101 AS abe0101Entrega ON abe0101Entrega.abe0101ent = abe01id AND abe0101Entrega.abe0101entrega = 1 " +
                "INNER JOIN aah01 ON abb01tipo = aah01id " +
                "INNER JOIN abe30 ON eaa01cp = abe30id " +
                "LEFT JOIN aag0201 AS aag0201Principal ON aag0201Principal.aag0201id = abe0101Principal.abe0101municipio " +
                "LEFT JOIN aag02 AS aag02Principal ON aag0201Principal.aag0201uf = aag02Principal.aag02id " +
                "LEFT JOIN aag0201 AS aag0201Entrega ON aag0201Entrega.aag0201id = abe0101Entrega.abe0101municipio " +
                "LEFT JOIN aag02 AS aag02Entrega ON aag0201Entrega.aag0201uf = aag02Entrega.aag02id " +
                "WHERE eaa01id = :idDoc " +
                "ORDER BY abb01num"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", idDoc))
    }

    private List<TableMap> buscarItensDoc(Long id) {
        return getSession().createQuery(" SELECT abm01codigo AS codItem, abm01descr AS descrItem, aam06codigo AS umu, eaa0103qtComl AS qtdItem, eaa0103unit AS unitItem, " +
                "eaa0103total AS totItem,eaa0103totDoc AS totDocItem, CAST(eaa0103json ->>'desconto' AS numeric(18,6)) AS descontoItem, abg01codigo AS codNcm, eaa0103entrega " +
                " FROM eaa0103 " +
                " INNER JOIN abm01 ON abm01id = eaa0103item " +
                " LEFT JOIN aam06 on aam06id = eaa0103umComl " +
                " LEFT JOIN abg01 ON abg01id = eaa0103ncm "+
                " WHERE eaa0103doc = :id " +
                " ORDER BY eaa0103seq").setParameters("id", id)
                .getListTableMap();
    }

    private List<TableMap> buscarParcelamentosDocumentos(Long idDoc) {
        String sql = "SELECT eaa0113dtVctoN, eaa0113valor " +
                "FROM eaa0113 " +
                "WHERE eaa0113doc = :idDoc " +
                "ORDER BY eaa0113dtVctoN"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", idDoc));
    }
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIEltcHJlc3PDo28gRG9jdW1lbnRvIEludGVybm8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIEltcHJlc3PDo28gRG9jdW1lbnRvIEludGVybm8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=