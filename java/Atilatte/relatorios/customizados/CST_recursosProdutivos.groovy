package Atilatte.relatorios.customizados;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.ValidacaoException;
import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.TableMapDataSource;
import br.com.multiorm.Query;

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage

public class CST_recursosProdutivos extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Recursos Produtivos"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		Long idRecursoIni = getLong("recursoIni");
		
		Long idRecursoFin = getLong("recursoFin");
		
		List<TableMap> recursos = buscarRecursos(idRecursoIni,idRecursoFin);

		for(TableMap recurso : recursos){
			
			String codRecurso = recurso.getString("codigo");
			
			Image qrCode = gerarQrCode(codRecurso);
			recurso.put("qrCode",qrCode);
		}

		
		return gerarPDF("CST_recursosprodutivos", recursos);
		
	}

	private  List<TableMap> buscarRecursos(Long recursoIni, Long recursoFin){
		Query sql = getSession().createQuery("SELECT abb20id, abb20codigo as codigo, abb20nome as descricao FROM abb20 "+ (recursoIni != null && recursoFin != null ? "WHERE abb20id BETWEEN :recursoIni AND :recursoFin" : ""));

		if(recursoIni != null ){
			sql.setParameter("recursoIni",recursoIni);
		}

		if(recursoFin != null){
			sql.setParameter("recursoFin",recursoFin)
		}

		return sql.getListTableMap();
		
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
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJlY3Vyc29zIFByb2R1dGl2b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=