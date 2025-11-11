package Silcon.relatorios.srf;

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.time.LocalDate
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SRF_NFCe extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "SRF - NFCe";
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
        List<TableMap> listPagamentos = new ArrayList<>();

        for (dado in dados) {
            Long idDoc = dado.getLong("eaa01id");
            List<TableMap> itensDoc = buscarItensDoc(idDoc);
            Integer countItens = 0;
            Image imgQrdCode = gerarQrCode(dado.getString("qrCode"));
            Integer tipoInscricao = dado.getInteger("abe01ti");
            String numInscricao = dado.getString("numInscricao");
            String inscricaoFormatada = formatarInscricao(numInscricao,tipoInscricao);
            List<TableMap> listFormaPagamento = buscarFormaDePagamento(idDoc);

            for (item in itensDoc) {
                countItens++;

                item.put("seq", countItens.toString())
                item.put("key", idDoc);
                listItens.add(item);
            }
            if(listFormaPagamento != null && listFormaPagamento.size() > 0){
                for(pagamento in listFormaPagamento){
                    pagamento.put("key", idDoc);
                    pagamento.put("descrFormaPgto", pagamento.getString("descrFormaPgto"));
                    pagamento.put("valorPgto", pagamento.getString("valorPgto"));
                    listPagamentos.add(pagamento);
                }
            }

            dado.put("key", idDoc);
            dado.put("imgQrCode", imgQrdCode);
            dado.put("numInscricao", inscricaoFormatada);

            if(itensDoc != null) dado.put("qtdTotalItens", itensDoc.size());
        }
        Aag0201 aag0201Empresa = getSession().get(Aag0201.class, "aag0201id, aag0201nome, aag0201uf", obterEmpresaAtiva().getAac10municipio().getAag0201id())
        Aag02 aag02Empresa = getSession().get(Aag02.class, "aag02id, aag02uf", aag0201Empresa.aag0201uf.aag02id);
        adicionarParametro("nomeEmpresa", obterEmpresaAtiva().getAac10rs());
        adicionarParametro("enderecoEmpresa", obterEmpresaAtiva().getAac10endereco() + ", " + obterEmpresaAtiva().getAac10numero() + ", " + obterEmpresaAtiva().getAac10bairro());
        adicionarParametro("ufEmpresa", aag0201Empresa.aag0201nome + ", " + aag02Empresa.aag02uf);
        adicionarParametro("inscricaoEmpresa", formatarInscricao(obterEmpresaAtiva().getAac10ni(),obterEmpresaAtiva().getAac10ti()));

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsItens", listItens, "key", "key");
        dsPrincipal.addSubDataSource("dsPagamentos", listPagamentos, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SRF_NFCe_S1"));
        adicionarParametro("StreamSub2", carregarArquivoRelatorio("SRF_NFCe_S2"));

        return gerarPDF("SRF_NFCe", dsPrincipal);
    }

    private List<TableMap> buscarDocumentos(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, List<Long> entidades, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida) {

        String whereTipos = tipos != null && tipos.size() > 0 ? " and abb01tipo in (:tipos) " : ""
        String whereEntidades = entidades != null && entidades.size() > 0 ? " and abb01ent in (:entidades) " : ""
        String wheredtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? " and abb01data between :dataIni and :dataFim " : ""
        String wheredtEntradaSaida = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? " and eaa01esData between :dataEntSaidaIni and :dataEntSaidaFin " : ""
        String whereNumIni = " and abb01num >= :numeroInicial "
        String whereNumFim = " and abb01num <= :numeroFinal "
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        String sql = "SELECT DISTINCT eaa01id, aah01codigo AS codTipoDoc, aah01nome AS nomeTipoDoc, abb01num AS numDoc, eaa01obsContrib, CAST(eaa01json -> 'vlr_carga_trib' AS NUMERIC(18,6)) AS cargaTrib,    " +
                "abe01codigo AS codEntidade, abe01na AS nomeEntidade, abe0101Principal.abe0101endereco AS enderecoEntidade, abe0101Principal.abe0101numero AS numEndEntidade, eaa01obsFisco,     " +
                "abe0101Principal.abe0101complem AS complemEntidade, abe0101Principal.abe0101bairro AS bairroEntidade, aag0201Principal.aag0201nome AS cidadeEntidade, aag02Principal.aag02uf AS ufEntidade,    " +
                "abe0101Principal.abe0101cep AS cepEntidade, abe0101Principal.abe0101ddd1 AS dddEntidade, abe0101Principal.abe0101fone1 AS foneEntidade, aab10nome AS usuario, abb01data AS dataEmissao, abb01operHora AS horaEmissao,    " +
                "abe30codigo AS codCondPgto, abe30nome AS descrCondPgto, eaa01totItens AS totalItem, CAST(eaa01json ->> 'desconto' AS numeric(18,6)) AS desconto, eaa01totDoc AS totDoc,    " +
                "aag02Principal.aag02uf AS ufEntidade, eaa01nfeChave AS chaveDoc, eaa01nfeProt AS protocoloDoc, eaa01nfeData AS dataAutorizacao,eaa01nfeHora AS horaAutorizacao, abe01ni AS numInscricao, abe01ti, abb01serie AS serie, eaa0102pvQrCodeVenda AS qrCode " +
                "FROM eaa01    " +
                "INNER JOIN eaa0101 ON eaa0101doc = eaa01id    " +
                "INNER JOIN abb01 ON abb01id = eaa01central    " +
                "INNER JOIN aab10 ON aab10id = abb01operUser    " +
                "INNER JOIN abe01 ON abe01id = abb01ent     " +
                "LEFT JOIN abe0101 AS abe0101Principal ON abe0101Principal.abe0101ent = abe01id AND abe0101Principal.abe0101principal = 1    " +
                "INNER JOIN aah01 ON abb01tipo = aah01id    " +
                "INNER JOIN abe30 ON eaa01cp = abe30id    " +
                "LEFT JOIN aag0201 AS aag0201Principal ON aag0201Principal.aag0201id = abe0101Principal.abe0101municipio    " +
                "LEFT JOIN aag02 AS aag02Principal ON aag0201Principal.aag0201uf = aag02Principal.aag02id   " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id  " +
                " WHERE eaa01cancData IS NULL    "+
                whereTipos +
                whereEntidades +
                wheredtEmissao +
                whereNumIni +
                whereNumFim+
                wheredtEntradaSaida +
                whereEmpresa +
                "ORDER BY abb01num"


        Parametro parametroTipo = tipos != null && tipos.size() > 0 ? criarParametroSql("tipos", tipos) : null
        Parametro parametroEntidade = entidades != null && entidades.size() > 0 ? criarParametroSql("entidades", entidades) : null
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? criarParametroSql("dataIni", dtEmissao[0]) : null
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? criarParametroSql("dataFim", dtEmissao[1]) : null
        Parametro parametroDtEntSaiIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? criarParametroSql("dataEntSaidaIni", dtEntradaSaida[0]) : null
        Parametro parametroDtEntSaiFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? criarParametroSql("dataEntSaidaFin", dtEntradaSaida[1]) : null
        Parametro parametroNumIni = criarParametroSql("numeroInicial", numeroInicial);
        Parametro parametroNumFin = criarParametroSql("numeroFinal", numeroFinal);
        Parametro parametroEmpresa = criarParametroSql("idEmpresa", obterEmpresaAtiva().getAac10id());


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipo, parametroEntidade, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroNumIni, parametroNumFin, parametroDtEntSaiIni, parametroDtEntSaiFin, parametroEmpresa);

    }
    private List<TableMap> buscarDocumentoById(Long idDoc) {
        String sql = " SELECT DISTINCT eaa01id, aah01codigo AS codTipoDoc, aah01nome AS nomeTipoDoc, abb01num AS numDoc, eaa01obsContrib, CAST(eaa01json -> 'vlr_carga_trib' AS NUMERIC(18,6)) AS cargaTrib, "+
                " abe01codigo AS codEntidade, abe01na AS nomeEntidade, abe0101Principal.abe0101endereco AS enderecoEntidade, abe0101Principal.abe0101numero AS numEndEntidade, eaa01obsFisco, "+
                " abe0101Principal.abe0101complem AS complemEntidade, abe0101Principal.abe0101bairro AS bairroEntidade, aag0201Principal.aag0201nome AS cidadeEntidade, aag02Principal.aag02uf AS ufEntidade, "+
                " abe0101Principal.abe0101cep AS cepEntidade, abe0101Principal.abe0101ddd1 AS dddEntidade, abe0101Principal.abe0101fone1 AS foneEntidade, aab10nome AS usuario, abb01data AS dataEmissao, abb01operHora AS horaEmissao, "+
                " abe30codigo AS codCondPgto, abe30nome AS descrCondPgto, eaa01totItens AS totalItem, CAST(eaa01json ->> 'desconto' AS numeric(18,6)) AS desconto, eaa01totDoc AS totDoc, "+
                " aag02Principal.aag02uf AS ufEntidade, eaa01nfeChave AS chaveDoc, eaa01nfeProt AS protocoloDoc, eaa01nfeData AS dataAutorizacao, eaa01nfeHora AS horaAutorizacao, abe01ni AS numInscricao,abe01ti, abb01serie AS serie " +
                " FROM eaa01 "+
                " INNER JOIN eaa0101 ON eaa0101doc = eaa01id "+
                " INNER JOIN abb01 ON abb01id = eaa01central "+
                " INNER JOIN aab10 ON aab10id = abb01operUser "+
                " INNER JOIN abe01 ON abe01id = abb01ent  "+
                " LEFT JOIN abe0101 AS abe0101Principal ON abe0101Principal.abe0101ent = abe01id AND abe0101Principal.abe0101principal = 1 "+
                " INNER JOIN aah01 ON abb01tipo = aah01id "+
                " INNER JOIN abe30 ON eaa01cp = abe30id "+
                " LEFT JOIN aag0201 AS aag0201Principal ON aag0201Principal.aag0201id = abe0101Principal.abe0101municipio "+
                " LEFT JOIN aag02 AS aag02Principal ON aag0201Principal.aag0201uf = aag02Principal.aag02id  "+
                " INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                " WHERE eaa01id = :idDoc " +
                " ORDER BY abb01num"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", idDoc))
    }
    private List<TableMap> buscarItensDoc(Long id) {
        return getSession().createQuery(" SELECT abm01codigo AS codItem, abm01na AS naItem, aam06codigo AS umu, eaa0103qtComl AS qtdItem, eaa0103unit AS unitItem, " +
                "eaa0103total AS totItem,eaa0103totDoc AS totDocItem, CAST(eaa0103json ->>'desconto' AS numeric(18,6)) AS descontoItem " +
                " FROM eaa0103 " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                " LEFT JOIN aam06 on aam06id = eaa0103umComl " +
                " WHERE eaa0103doc = :id " +
                " ORDER BY eaa0103seq").setParameters("id", id)
                .getListTableMap();

    }
    private List<TableMap> buscarFormaDePagamento(Long eaa01id){
        String sql = " SELECT abf40descr AS descrFormaPgto, eaa01131valor AS valorPgto " +
                     " FROM eaa01131 " +
                     " INNER JOIN eaa0113 ON eaa0113id = eaa01131fin "+
                     " INNER JOIN abf40 ON abf40id = eaa01131fp "+
                     " WHERE eaa0113doc = :eaa01id "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));

    }

    private String formatarInscricao(String ie, Integer tipoInscricao) {
        // Retira os caracteres que não são números
        ie = ie.replaceAll("\\D", "");


        if (tipoInscricao == 0 && ie.length() != 14){
            interromper("CNPJ do cliente é Inválido")
        }else if(tipoInscricao == 1 && ie.length() != 11){
            interromper( "CPF do cliente é Inválido");
        }

        if (tipoInscricao == 0){
            // Formata CNPJ: 00.000.000/0000-00
            ie = ie.substring(0, 2) + "." +
                    ie.substring(2, 5) + "." +
                    ie.substring(5, 8) + "/" +
                    ie.substring(8, 12) + "-"+
                    ie.substring(12)
        }else{
            // Formata CPF: 000.000.000-00
            ie = ie.substring(0, 3) + "." +
                    ie.substring(3, 6) + "." +
                    ie.substring(6, 9) + "-" +
                    ie.substring(9);
        }

        return ie
    }
    private Image gerarQrCode(String qrCode) {
        Image imgqrcode = null;
        if(qrCode != null){
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            int size = 400;
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCode, BarcodeFormat.QR_CODE, size, size);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            imgqrcode = Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
        }
        return imgqrcode;
    }
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIEltcHJlc3PDo28gRG9jdW1lbnRvIEludGVybm8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIE5GQ2UiLCJ0aXBvIjoicmVsYXRvcmlvIn0=