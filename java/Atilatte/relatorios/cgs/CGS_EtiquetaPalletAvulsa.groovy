package Atilatte.relatorios.cgs

import br.com.multitec.utils.collections.TableMap
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.HashMap;

public class CGS_EtiquetaPalletAvulsa extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CGS - Etiqueta Pallet Avulsa";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> ids = getListLong("abm70ids");

        List<TableMap> etiquetas = buscarEtiquetas(ids);

        Integer countEtiquetas = 0;

        for(etiqueta in etiquetas){
            countEtiquetas++;
            Integer qtdPallet = etiquetas.size();
            String codItem = etiqueta.getString("codItem");
            String descrItem = etiqueta.getString("naItem");
            BigDecimal qtdFrasco = etiqueta.getBigDecimal_Zero("frascos");
            Integer capacidadeCaixa = etiqueta.getInteger("capacidadeCaixa");
            if(capacidadeCaixa == 0) interromper("Quantidade de itens por caixa no cadastro do item " + codItem + " - " + descrItem + " é inválida.");
            BigDecimal qtdCaixa = qtdFrasco / capacidadeCaixa;
            BigDecimal qtdAvulso = qtdFrasco.intValue() % capacidadeCaixa;
            String codQrCode = etiqueta.getString("numEtiqueta");
            Image imgQrCode = gerarQrCode(codQrCode);


            String sequenciaPallet = countEtiquetas.toString() + " / " + qtdPallet.toString();

            etiqueta.put("sequenciaPallet", sequenciaPallet)
            etiqueta.put("frascos", qtdFrasco);
            etiqueta.put("avulsos", qtdAvulso);
            etiqueta.put("caixas", qtdCaixa);

            etiqueta.put("imgQrCode", imgQrCode);


        }
        return gerarPDF("CGS_EtiquetaPalletAvulsa", etiquetas);
    }
    private List<TableMap> buscarEtiquetas(List<Long> ids){
        String sql = "SELECT abm01codigo AS codItem, abm01na AS naItem, abm70id AS idEtiqueta, " +
                "abm70num AS numEtiqueta, CAST(abm0101json ->> 'cvdnf' AS INTEGER) AS capacidadeCaixa, abm70num as numEtiqueta, " +
                "abm70qt as frascos, abm70lote as lote, abm70fabric as fabric, abm70validade as valid " +
                "FROM abm70 " +
                "INNER JOIN abm01 ON abm01id = abm70item " +
                "INNER JOIN abm0101 ON abm0101item = abm01id " +
                "WHERE abm70id IN (:ids)"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("ids", ids));

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
//meta-sis-eyJkZXNjciI6IkNHUyAtIEV0aXF1ZXRhIFBhbGxldCBBdnVsc2EiLCJ0aXBvIjoicmVsYXRvcmlvIn0=