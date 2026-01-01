package multitec.relatorios.cgs

import com.google.zxing.datamatrix.DataMatrixWriter;

import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.time.LocalDate

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abm70
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class CGS_Etiquetas extends RelatorioBase {
		
	@Override
	public String getNomeTarefa() {
		return "CGS - Etiquetas";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroEtiquetaInicial", "000000001");
		filtrosDefault.put("numeroEtiquetaFinal", "999999999");
		filtrosDefault.put("data", DateUtils.getStartAndEndMonth(MDate.date()));
		filtrosDefault.put("imprimir", "0");
		filtrosDefault.put("numeroDocumentoInicial", "000000001");
		filtrosDefault.put("numeroDocumentoFinal", "999999999");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer numeroEtiquetaInicial = getInteger("numeroEtiquetaInicial");
		Integer numeroEtiquetaFinal = getInteger("numeroEtiquetaFinal");
		LocalDate[] data = getIntervaloDatas("data");
		Integer imprimir = getInteger("imprimir");
		Long idTipoDocumento = getLong("tipoDocumento");
		Integer numeroDocumentoInicial = getInteger("numeroDocumentoInicial");
		Integer numeroDocumentoFinal = getInteger("numeroDocumentoFinal");
		List<Integer> mpm = getListInteger("mpm");
		String itemIni = getString("itemIni");
		String itemFim = getString("itemFim");
		
		List<Long> ids = getListLong("abm70ids");	
		
		List<TableMap> listTMDados = null;
		if(ids == null || ids.size() == 0) {
			listTMDados = buscarEtiquetas(numeroEtiquetaInicial, numeroEtiquetaFinal, data, 
										imprimir, idTipoDocumento, numeroDocumentoInicial, numeroDocumentoFinal,
										mpm, itemIni, itemFim);
									
			if(listTMDados == null || listTMDados.size() == 0) throw new ValidacaoException("Não foram encontrados dados com base no filtro informado.");
		}else {
			listTMDados = buscarEtiquetas(ids);
			
			if(listTMDados == null || listTMDados.size() == 0) throw new ValidacaoException("Não foram encontradas as etiquetas para impressão.");
		}
		
		List<Long> abm70ids = new ArrayList<Long>();
		for(TableMap tm : listTMDados) {
			Long abm70id = tm.getLong("abm70id");
			abm70ids.add(abm70id);
			
			String codbarsam = tm.getLong("abm70num") + "-" + tm.getInteger("abm70dv");
			tm.put("codbarsam", codbarsam)
			
			Image imgqrcode = gerarQrCode(codbarsam);
			tm.put("imgqrcode", imgqrcode);
		}
				
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		
		gravarStatusImpressaoEtiquetas(abm70ids);
		
		return gerarPDF("CGS_Etiquetas", dsPrincipal);
	}
	
	private List<TableMap> buscarEtiquetas(Integer numeroEtiquetaInicial, Integer numeroEtiquetaFinal, LocalDate[] data, 
		Integer imprimir, Long idTipoDocumento, Integer numeroDocumentoInicial, Integer numeroDocumentoFinal,
		List<Integer> mpm, String itemIni, String itemFim) {
		
		LocalDate dtIni = null;
		LocalDate dtFin = null;
		if(data != null) {
			dtIni = data[0];
			dtFin = data[1];
		}
		
		if(mpm == null || mpm.size() == 0) mpm = Arrays.asList(-1);
		String whereItens = itemIni != null && itemFim != null ? " AND abm01codigo BETWEEN :itemIni AND :itemFim " : itemIni != null && itemFim == null ? " AND abm01codigo >= :itemIni " : itemIni == null && itemFim != null ? " AND abm01codigo <= :itemFim " : "";
				
		Query query = getSession().createQuery(" SELECT abm70id, abm70num, abm70dv, abm70item, abm70qt, abm70lote, abm70serie, ",
										" abm70validade, abm70fabric, abm70central, abm70status, abm70data, abm70modelo, ",
										" abm01id, abm01tipo, abm01codigo, abm01na, abm01descr, ", 
										" aam06id, aam06codigo, ",
										" aah01id, aah01codigo, aah01na, abb01num, abb01data", 
										" abe01id, abe01codigo, abe01na, ",
										" aam05id, aam05codigo, aam05nome ",
										" FROM Abm70 ",
										" INNER JOIN Abm01 ON abm70item = abm01id ",
										" LEFT JOIN Aam06 ON abm01umu = aam06id ",
										" LEFT JOIN Abb01 ON abm70central = abb01id ",
										" LEFT JOIN Aah01 ON abb01tipo = aah01id ",
										" LEFT JOIN Abe01 ON abb01ent = abe01id ",
										" LEFT JOIN Aam05 ON abm70modelo = aam05id ",
										" WHERE abm70num BETWEEN :numeroEtiquetaInicial AND :numeroEtiquetaFinal ",
										" AND abm70data BETWEEN :dtIni AND :dtFin ",
										" AND abm70status = :status ",
										(idTipoDocumento != null ? " AND aah01id = :idTipoDocumento " : ""),
										(idTipoDocumento != null ? " AND abb01num BETWEEN :numeroDocumentoInicial AND :numeroDocumentoFinal " : ""),
										" AND abm01tipo IN (:mpm) ",
										whereItens,
										getSamWhere().getWherePadrao("AND", Abm70.class),
										" ORDER BY abm70num");
		
		if(idTipoDocumento != null) {
			query.setParameter("idTipoDocumento", idTipoDocumento);
			query.setParameter("numeroDocumentoInicial", numeroDocumentoInicial);
			query.setParameter("numeroDocumentoFinal", numeroDocumentoFinal);
		}
		
		if(dtIni != null && dtFin != null) {
			query.setParameter("dtIni", dtIni);
			query.setParameter("dtFin", dtFin);
		}
		
		if(whereItens.length() > 0) {
			if(itemIni != null) query.setParameter("itemIni", itemIni);
			if(itemFim != null) query.setParameter("itemFim", itemFim);
		}
		
		query.setParameters("numeroEtiquetaInicial", numeroEtiquetaInicial,
							"numeroEtiquetaFinal", numeroEtiquetaFinal,
							"dtIni", dtIni,
							"dtFin", dtFin,
							"status", imprimir,
							"mpm", mpm);
									  
		return query.getListTableMap();
	}
	
	private List<TableMap> buscarEtiquetas(List<Long> ids) {
		Query query = getSession().createQuery(" SELECT abm70id, abm70num, abm70dv, abm70item, abm70qt, abm70lote, abm70serie, ",
										" abm70validade, abm70fabric, abm70central, abm70status, abm70data, abm70modelo, abm70gc, abm70eg, ",
										" abm01id, abm01tipo, abm01codigo, abm01na, abm01descr, ",
										" aam06id, aam06codigo, ",
										" aah01id, aah01codigo, aah01na, ",
										" abe01id, abe01codigo, abe01na, ",
										" aam05id, aam05codigo, aam05nome ",
										" FROM Abm70 ",
										" INNER JOIN Abm01 ON abm70item = abm01id ",
										" LEFT JOIN Aam06 ON abm01umu = aam06id ",
										" LEFT JOIN Abb01 ON abm70central = abb01id ",
										" LEFT JOIN Aah01 ON abb01tipo = aah01id ",
										" LEFT JOIN Abe01 ON abb01ent = abe01id ",
										" LEFT JOIN Aam05 ON abm70modelo = aam05id ",
										" WHERE abm70id IN (:ids) ",
										getSamWhere().getWherePadrao("AND", Abm70.class),
										" ORDER BY abm70num");
		
		query.setParameters("ids", ids);
									  
		return query.getListTableMap();
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

	private Image gerarDataMatrix(String dataMatrixCode) {
		Image imgDataMatrix = null;
		if (dataMatrixCode != null) {
			DataMatrixWriter dataMatrixWriter = new DataMatrixWriter();

			int size = 400;
			BitMatrix bitMatrix = dataMatrixWriter.encode(dataMatrixCode, BarcodeFormat.DATA_MATRIX, size, size);

			BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
			imgDataMatrix = Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
		}
		return imgDataMatrix;
	}
	
	private void gravarStatusImpressaoEtiquetas(List<Long> abm70ids) {
		try {
			if(abm70ids == null || abm70ids.size() == 0) return;
			
			for(Long abm70id : abm70ids) {
				Abm70 abm70 = getSession().get(Abm70.class, "abm70id, abm70status", abm70id);
				abm70.setAbm70status(1);
				getSession().persist(abm70);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
//meta-sis-eyJkZXNjciI6IkNHUyAtIEV0aXF1ZXRhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==