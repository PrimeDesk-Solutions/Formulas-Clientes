package Atilatte.relatorios.cas

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

public class CAS_ImpressaoStatus extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CAS - Impressão de Status";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        String statusSaldosIni = getString("statusIni");
        String statusSaldosFin = getString("statusFin");

        List<TableMap> dados = buscarStatusSaldos(statusSaldosIni, statusSaldosFin);

        for (status in dados) {
            String statusSaldos = status.getString("aam04codigo");

            Image imgQrCodee = gerarQrCode(statusSaldos);
            status.put("imgQrCodee", imgQrCodee);
        }

        return gerarPDF("CAS_ImpressaoStatus", dados);
    }

    private List<TableMap> buscarStatusSaldos(String statusSaldosIni, String statusSaldosFin) {

        String whereStatus = statusSaldosIni != null && statusSaldosFin != null ? " WHERE aam04codigo BETWEEN :statusSaldosIni and :statusSaldosFin " :
                statusSaldosIni != null && statusSaldosFin == null ? "WHERE aam04codigo >= :statusSaldosIni " :
                        statusSaldosIni == null && statusSaldosFin != null ? "WHERE aam04codigo <= :statusSaldosFin " : "";

        Parametro parametrosStatusIni = statusSaldosIni != null ? Parametro.criar("statusSaldosIni", statusSaldosIni) : null;
        Parametro parametrosStatusFin = statusSaldosFin != null ? Parametro.criar("statusSaldosFin", statusSaldosFin) : null;

        String sql = " SELECT aam04codigo " +
                " FROM aam04 " +
                whereStatus +
                "ORDER BY aam04codigo";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametrosStatusIni, parametrosStatusFin);
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
//meta-sis-eyJkZXNjciI6IkNBUyAtIEltcHJlc3PDo28gZGUgU3RhdHVzIiwidGlwbyI6InJlbGF0b3JpbyJ9