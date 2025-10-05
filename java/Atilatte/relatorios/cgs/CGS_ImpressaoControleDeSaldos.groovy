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

public class CGS_ImpressaoControleDeSaldos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CGS - Impressão de Controle De Saldos";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        String localIni = getString("localIni");
        String localFin = getString("localFin");

        List<TableMap> dados = buscarNomesControleSaldos(localIni, localFin);

        for (local in dados) {
            String nomeLocal = local.getString("abm15nome");

            Image imgQrCode = gerarQrCode(nomeLocal);
            local.put("imgQrCode", imgQrCode);
        }

        return gerarPDF("CGS_ImpressaoControleDeSaldos", dados);
    }

    private List<TableMap> buscarNomesControleSaldos(String localIni, String localFin) {

        String whereLocal = localIni != null && localFin != null ? "WHERE abm15nome BETWEEN :localIni AND :localFin " :
                localIni != null && localFin == null ? "WHERE abm15nome >= :localIni " :
                        localIni == null && localFin != null ? "WHERE abm15nome <= :localFin " : "";

        Parametro parametroLocalIni = localIni != null ? Parametro.criar("localIni", localIni) : null;
        Parametro parametroLocalFin = localFin != null ? Parametro.criar("localFin", localFin) : null;

        String sql = " SELECT abm15nome " +
                " FROM abm15 " +
                whereLocal +
                "ORDER BY abm15nome ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroLocalIni, parametroLocalFin);
    }

    private Image gerarQrCode(String qrCode) {
        Image imgqrcode = null;

        if (qrCode != null) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            int size = 400;
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCode, BarcodeFormat.QR_CODE, size, size);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            imgqrcode = Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
        }

        return imgqrcode;
    }
}
//meta-sis-eyJkZXNjciI6IkNHUyAtIEltcHJlc3PDo28gZGUgQ29udHJvbGUgRGUgU2FsZG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9