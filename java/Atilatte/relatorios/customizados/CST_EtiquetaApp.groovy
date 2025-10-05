package Atilatte.relatorios.customizados

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

public class CST_EtiquetaApp extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "CST - EtiquetaApp";
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
			def pesoBruto;
			def pesoLiquido
			def taraTotal;
			def taraEmb;
			def taraCaixa;
			def capacidadeItem;
			String umu = tm.getString("aam06codigo");
			String codQrCode = tm.getString("abm70num");
			Image imgqrcode = gerarQrCode(codQrCode);
			String codBarDun14 = tm.getString("dun14")

			if(umu.toUpperCase() == 'KG'){
				taraEmb = tm.getBigDecimal_Zero("abm0101taraemb") * tm.getBigDecimal_Zero("abm0101cvdnf");
				taraCaixa = tm.getBigDecimal_Zero("abm0101taracaixa");
				taraTotal = taraEmb + taraCaixa;
				pesoBruto = tm.getBigDecimal_Zero("abm70peso");
				pesoLiquido = tm.getBigDecimal_Zero("abm70qt");
			}else{
				taraEmb = tm.getBigDecimal_Zero("abm0101taraemb") * tm.getBigDecimal_Zero("abm70qt");
				taraCaixa = tm.getBigDecimal_Zero("abm0101taracaixa");
				taraTotal = taraEmb + taraCaixa;
				//pesoBruto = tm.getBigDecimal_Zero("abm01pesobruto") * tm.getBigDecimal_Zero("abm70qt");
				//pesoLiquido = pesoBruto - taraTotal;
				pesoLiquido = tm.getBigDecimal_Zero("abm70qt") * tm.getBigDecimal_Zero("abm01pesoliq");
				pesoBruto = taraTotal + pesoLiquido
			}

			tm.put("imgqrcode", imgqrcode);
			tm.put("pesoBruto", pesoBruto.round(4));
			tm.put("pesoLiquido", pesoLiquido.round(4));
			tm.put("taraTotal", taraTotal);
			tm.put("taraEmb", taraEmb);
			tm.put("taraCaixa", taraCaixa);

			String codigoDataMatrix = gerarStringDataMatrix(tm);
			Image imgDataMatrix = gerarDataMatrix(codigoDataMatrix);

			tm.put("imgDataMatrix", imgDataMatrix);
			tm.put("codigoDataMatrix", codigoDataMatrix);
			tm.put("codbarsam", codigoDataMatrix);
			tm.put("codBarDun14", codBarDun14);

			verificarDiaSemanaFabricacao(tm.getDate("dtFabricacao").getDayOfWeek().toString());

			abm70ids.add(abm70id);
		}

		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);

		gravarStatusImpressaoEtiquetas(abm70ids);

		return gerarPDF("CST_etiquetaAppSam4", dsPrincipal);
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
				" abm70central, abm70status, abm70data, abm70modelo, abm70gc, abm70eg, ",
				" abm01id, abm01tipo, abm01codigo, abm01na, abm01descr, abm01pesobruto, abm01pesoliq, ",
				" aam06id, aam06codigo, ",
				" aah01id, aah01codigo, aah01na, ",
				" abe01id, abe01codigo, abe01na,cast(abm0101json ->> 'descricao_livre' as text) as dun14, ",
				" aam05id, aam05codigo, aam05nome, ",
				" abm14validdias, abm01gtin, ",
				" CAST(abm0101json ->>'cvdnf' AS integer) as abm0101cvdnf, CAST(abm0101json ->>'tara_emb_' AS numeric(18,6)) as abm0101taraemb,CAST(abm0101json ->>'tara_caixa' AS numeric(18,6)) as abm0101taracaixa, ",
				" CAST(abm70json ->>'peso_bruto' AS numeric(18,4)) AS abm70peso, CAST(abm0101json ->> 'frase_de_armazenagem' as text) as fraseArmazenagem, ",
				" case when bab01dte is not null then bab01dte else abm70fabric end as dtFabricacao, " +
						" case when bab01dte is not null then to_char(bab01dte, 'yymmdd') else to_char(abm70fabric, 'yymmdd') end as dtFabricAux, " +
						" case when bab01dte is not null then bab01dte + abm14validdias else abm70fabric + abm14validdias end as dtValidadeAjustado, " +
						" case when bab01dte is not null then to_char(bab01dte + abm14validdias,'yymmdd') else to_char(abm70fabric + abm14validdias,'yymmdd') end as dtValidadeAjustadoAux ",
				" FROM Abm70 ",
				" INNER JOIN Abm01 ON abm70item = abm01id ",
				"INNER JOIN abm0101 on abm0101item = abm01id and abm0101empresa = '1322578' ",
				"left JOIN abm14 on abm14id = abm0101producao ",
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
				" abm70central, abm70status, abm70data, abm70modelo, abm70gc, abm70eg, ",
				" abm01id, abm01tipo, abm01codigo, abm01na, abm01descr, abm01pesobruto, abm01pesoliq, ",
				" aam06id, aam06codigo, ",
				" aah01id, aah01codigo, aah01na, ",
				" abe01id, abe01codigo, abe01na,cast(abm0101json ->> 'descricao_livre' as text) as dun14, ",
				" aam05id, aam05codigo, aam05nome, ",
				" abm14validdias, abm01gtin, ",
				" CAST(abm0101json ->>'cvdnf' AS integer) as abm0101cvdnf, CAST(abm0101json ->>'tara_emb_' AS numeric(18,6)) as abm0101taraemb,CAST(abm0101json ->>'tara_caixa' AS numeric(18,6)) as abm0101taracaixa, ",
				" CAST(abm70json ->>'peso_bruto' AS numeric(18,4)) AS abm70peso, CAST(abm0101json ->> 'frase_de_armazenagem' as text) as fraseArmazenagem, ",
				" case when bab01dte is not null then bab01dte else abm70fabric end as dtFabricacao, " +
						" case when bab01dte is not null then to_char(bab01dte, 'yymmdd') else to_char(abm70fabric, 'yymmdd') end as dtFabricAux, " +
						" case when bab01dte is not null then bab01dte + abm14validdias else abm70fabric + abm14validdias end as dtValidadeAjustado, " +
						" case when bab01dte is not null then to_char(bab01dte + abm14validdias,'yymmdd') else to_char(abm70fabric + abm14validdias,'yymmdd') end as dtValidadeAjustadoAux ",
				" FROM Abm70 ",
				" INNER JOIN Abm01 ON abm70item = abm01id ",
				"INNER JOIN abm0101 on abm0101item = abm01id and abm0101empresa = '1322578' ",
				"left JOIN abm14 on abm14id = abm0101producao ",
				" LEFT JOIN Aam06 ON abm01umu = aam06id ",
				" LEFT JOIN Abb01 ON abm70central = abb01id ",
				" LEFT JOIN Aah01 ON abb01tipo = aah01id ",
				" LEFT JOIN Abe01 ON abb01ent = abe01id ",
				" LEFT JOIN Aam05 ON abm70modelo = aam05id ",
				" LEFT JOIN bab01 ON bab01central = abm70central",
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

	private String gerarStringDataMatrix(TableMap tmEtiqueta){

		String codigoDataMatrix = "";

		String pesoLiquido = tmEtiqueta.getBigDecimal_Zero("pesoLiquido").toString();
		String pesoBruto = tmEtiqueta.getBigDecimal_Zero("pesoBruto").toString();

		String gtin;
		String validade;
		String producao;
		String qtdItem;
		String pesoLiquidoFormatado;
		String pesoBrutoFormatado;
		String lote;

		if(tmEtiqueta.getString("aam06codigo").toUpperCase() == 'KG'){
			gtin = tmEtiqueta.getString("abm01gtin");
			validade = tmEtiqueta.getString("dtValidadeAjustadoAux");
			producao = tmEtiqueta.getString("dtFabricAux");
			qtdItem = tmEtiqueta.getString("abm0101cvdnf");
			pesoLiquidoFormatado = formatarPeso(pesoLiquido);
			pesoBrutoFormatado = formatarPeso(pesoBruto);
			lote = tmEtiqueta.getString("abm70lote");
			char gs = 29;

			codigoDataMatrix =  "010" + gtin + "17" + validade + "11" + producao + "30" + qtdItem + gs + "3104" + pesoLiquidoFormatado + "3304" + pesoBrutoFormatado + "10" + lote + gs + "00078995392999999990" ;

		}else{
			gtin = tmEtiqueta.getString("abm01gtin");
			validade = tmEtiqueta.getString("dtValidadeAjustadoAux");
			producao = tmEtiqueta.getString("dtFabricAux");
			lote = tmEtiqueta.getString("abm70lote");

			codigoDataMatrix =  "01" + gtin + "17" + validade + "11" + producao + "10" + lote + "000000000" ;

		}

		return codigoDataMatrix;


	}

	private String formatarPeso(String peso){
		def sizeString = peso.replace(".","").length();
		def complemento = 6 - sizeString
		def pesoFormatado = peso.replace(".","");

		if (complemento == 0) return peso.replace(".","")

		for(int i = 0; i < complemento; i++){
			pesoFormatado = "0" + pesoFormatado
		}

		return pesoFormatado;

	}

	private verificarDiaSemanaFabricacao(String dtFabricacao){
		switch (dtFabricacao){
			case "SUNDAY":
				params.put("paramDomingo",true);
				params.put("paramSegunda",true);
				params.put("paramTerca",true);
				params.put("paramQuarta",true);
				params.put("paramQuinta",true);
				params.put("paramSexta",true);
				params.put("paramSabado",true);
				break;
			case "MONDAY":
				params.put("paramDomingo",false);
				params.put("paramSegunda",true);
				params.put("paramTerca",false);
				params.put("paramQuarta",false);
				params.put("paramQuinta",false);
				params.put("paramSexta",false);
				params.put("paramSabado",false);
				break;
			case "TUESDAY":
				params.put("paramDomingo",false);
				params.put("paramSegunda",true);
				params.put("paramTerca",true);
				params.put("paramQuarta",false);
				params.put("paramQuinta",false);
				params.put("paramSexta",false);
				params.put("paramSabado",false);
				break;
			case "WEDNESDAY":
				params.put("paramDomingo",false);
				params.put("paramSegunda",true);
				params.put("paramTerca",true);
				params.put("paramQuarta",true);
				params.put("paramQuinta",false);
				params.put("paramSexta",false);
				params.put("paramSabado",false);
				break;
			case "THURSDAY":
				params.put("paramDomingo",false);
				params.put("paramSegunda",true);
				params.put("paramTerca",true);
				params.put("paramQuarta",true);
				params.put("paramQuinta",true);
				params.put("paramSexta",false);
				params.put("paramSabado",false);
				break;
			case "FRIDAY":
				params.put("paramDomingo",false);
				params.put("paramSegunda",true);
				params.put("paramTerca",true);
				params.put("paramQuarta",true);
				params.put("paramQuinta",true);
				params.put("paramSexta",true);
				params.put("paramSabado",false);
				break;
			default:
				params.put("paramDomingo",false);
				params.put("paramSegunda",true);
				params.put("paramTerca",true);
				params.put("paramQuarta",true);
				params.put("paramQuinta",true);
				params.put("paramSexta",true);
				params.put("paramSabado",true);
				break;
		}
	}
//	private String montarCodigoDeBarras(String umu, String codigoDataMatrix){
//		String codigoBarras = ""
//		String doisPrimeiroDigitos = codigoDataMatrix.substring(0,2);
//		String restoCodigo = codigoDataMatrix.substring(2);
//
//		if(umu == 'KG'){
//			codigoBarras = doisPrimeiroDigitos + '0' + restoCodigo;
//		}else{
//			codigoBarras = codigoDataMatrix
//		}
//
//		return codigoBarras;
//	}

}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhQXBwIiwidGlwbyI6InJlbGF0b3JpbyJ9