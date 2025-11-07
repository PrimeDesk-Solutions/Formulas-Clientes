package Atilatte.relatorios.cgs

import br.com.multitec.utils.collections.TableMap
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class CGS_ImpressaoEtiquetaPalletAgregadora extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CGS - Etiqueta Pallet Agregadora ";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> ids = getListLong("abm70ids");

        List<TableMap> etiquetas = buscarEtiquetasPaletes(ids);

        Integer countEtiquetas = 0
        for(etiqueta in etiquetas){
            countEtiquetas++
            Long idEtiqueta = etiqueta.getLong("idEtiqueta")
            Integer capacidadeCaixa = etiqueta.getInteger("capacidadeCaixa");
            TableMap etiquetasFilhas = buscarEtiquetasFilhas(idEtiqueta);
            BigDecimal qtdFrasco = etiquetasFilhas.getBigDecimal_Zero("qtdFrasco");
            BigDecimal qtdAvulso = qtdFrasco.intValue() % capacidadeCaixa;
            Integer qtdCaixa = etiquetasFilhas.getInteger("qtdEtiqueta");
            String lote = etiquetasFilhas.getString("lote");
            String codQrCode = etiqueta.getString("numEtiqueta");
            Image imgQrCode = gerarQrCode(codQrCode);

            LocalDate dtValidade = etiquetasFilhas.getDate("valid");
            LocalDate dtFabric = etiquetasFilhas.getDate("fabric");
            BigDecimal qtdPallet = etiquetas.size()
            String sequenciaPallet = countEtiquetas.toString() + " / " + qtdPallet.toString();

            etiqueta.put("sequenciaPallet", sequenciaPallet)
            etiqueta.put("frascos", qtdFrasco);
            etiqueta.put("avulsos", qtdAvulso);
            etiqueta.put("caixas", qtdCaixa);
            etiqueta.put("lote", lote);
            etiqueta.put("fabric", dtFabric);
            etiqueta.put("valid", dtValidade);
            etiqueta.put("imgQrCode", imgQrCode);
        }

        return gerarPDF("CGS_EtiquetaPalletAgregadora", etiquetas)

    }

    private List<TableMap> buscarEtiquetasPaletes(List<Long>ids){

        String sql = "SELECT abm01codigo AS codItem, abm01na AS naItem, abm70id AS idEtiqueta, " +
                        "abm70num AS numEtiqueta, CAST(abm0101json ->> 'cvdnf' AS INTEGER) AS capacidadeCaixa, abm70num as numEtiqueta " +
                        "FROM abm70 " +
                        "INNER JOIN abm01 ON abm01id = abm70item " +
                        "INNER JOIN abm0101 ON abm0101item = abm01id " +
                        "WHERE abm70id IN (:ids)"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("ids", ids));
    }
    private TableMap buscarEtiquetasFilhas(Long idEtiqueta){
        String sql = "SELECT SUM(abm70qt) as qtdFrasco, count(abm70id) as qtdEtiqueta, abm70lote as lote, abm70fabric as fabric, abm70validade as valid " +
                    "FROM abm70 " +
                    "WHERE abm70paletizadora = :idEtiqueta " +
                    "GROUP BY abm70lote, abm70fabric, abm70validade";

        return getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("idEtiqueta", idEtiqueta))
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
//meta-sis-eyJkZXNjciI6IkNHUyAtIEltcHJlc3PDo28gRXRpcXVldGEgUGFsbGV0IiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNHUyAtIEV0aXF1ZXRhIFBhbGxldCBBZ3JlZ2Fkb3JhICIsInRpcG8iOiJyZWxhdG9yaW8ifQ==