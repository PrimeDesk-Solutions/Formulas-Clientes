package Atilatte.relatorios.customizados;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import java.lang.Math;


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
import sam.server.samdev.utils.Parametro
import java.time.LocalDate



import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


public class CST_ordemEnvase extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CST - Ordem de Envase";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("numeroInicial","1");
		filtrosDefault.put("numeroFinal","9999999")
		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		Long idDocIni = getLong("numOrdemIni");
		Long idDocFin = getLong("numOrdemFin");
		LocalDate[] datas = getIntervaloDatas("datas");
		LocalDate[] datasEntrega = getIntervaloDatas("dtEntrega");
		Integer numOpIni = getInteger("numeroInicial");
		Integer numOpFin = getInteger("numeroFinal");
		List<TableMap> listTMDados = null;



		params.put("empresa", getVariaveis().getAac10().getAac10codigo() +"-"+getVariaveis().getAac10().getAac10na())

		listTMDados = buscarDadosRelatorio(idDocIni, idDocFin, datas, datasEntrega,numOpIni, numOpFin);
		List<TableMap> listItens = new ArrayList();
		List<TableMap> listProcessos = new ArrayList();

		List<Long> numDoc = new ArrayList();



		for(TableMap registro : listTMDados){
			def  qtdEtiquetaTotal = 0;
			def qtdEtiquetaTotalArredondado;
			Long idDocumento = registro.getLong("abb01id");
			Long idPlano = registro.getLong("baa01id")
			Integer numDocumento = registro.getInteger("numOrdem");
			String codProcesso = registro.getString("abp10codigo");
			String codItem = registro.getString("codigoItemEnvase")

			BigDecimal qtdCaixaItem = quantidadeCaixas(codItem);

			Integer qtdDocProd = registro.getInteger("qtProduzir") != null ? registro.getInteger("qtProduzir") : 0;

			if(qtdCaixaItem == 0 || qtdCaixaItem == null){
				qtdEtiquetaTotalArredondado = 0;
			}else{
				qtdEtiquetaTotal = qtdDocProd / qtdCaixaItem;
				qtdEtiquetaTotalArredondado = Math.ceil(qtdEtiquetaTotal)
			}

			registro.put("qtcaixas", qtdEtiquetaTotalArredondado)

			List<TableMap> processos = buscarProcessosAtividades(codProcesso);

			TableMap itensSemi = buscarItensSemi(numDocumento)

			if(itensSemi != null) registro.putAll(itensSemi);

			numDoc.add(numDocumento)

			String codbarsam = registro.getInteger("numOrdem");
			registro.put("codbarsam",codbarsam );

			Image imgqrcode = gerarQrCode(codbarsam);
			registro.put("imgqrcode",imgqrcode)

		}

		List<TableMap> itensComponentes = buscarItensComponentes(numDoc);


		listTMDados.addAll(itensComponentes)

		return gerarPDF("CST_ordemdeenvase", listTMDados);


	}

	private List<TableMap>buscarDadosRelatorio(Long idDocIni, Long idDocFin, LocalDate[] datas, LocalDate[] datasEntrega, Integer numOpIni, Integer numOpFin){

		//Data Documento
		LocalDate dataIni = null;
		LocalDate dataFin = null;
		if(datas != null){
			dataIni = datas[0];
			dataFin = datas[1];
		}

		//Data Entrega
		LocalDate dtEntregaIni = null;
		LocalDate dtEntregaFin = null;
		if(datasEntrega != null) {
			dtEntregaIni = datasEntrega[0];
			dtEntregaFin = datasEntrega[1];
		}

		String wherePlano = idDocIni != null && idDocFin != null ? "WHERE baa01id BETWEEN :idDocIni AND :idDocFin " : idDocIni != null && idDocFin == null ? "WHERE baa01id >= :idDocIni " : idDocIni == null && idDocFin != null ? "WHERE baa01id <= :idDocFin ": "";
		String whereDoc = numOpIni != null && numOpFin != null ? "AND abb01num BETWEEN :numOpIni AND :numOpFin " : numOpIni != null && numOpFin == null ? "AND abb01num >= :numOpIni " : numOpIni == null && numOpFin != null ? "AND abb01num <= :numOpFin ": "";

		Query query = getSession().createQuery ("select umuEnvase.aam06codigo as umuPrincipal, abb01num as numOrdem,abb01id, abp10codigo, " +
				"baa01id,itemEnvase.abm01id,itemEnvase.abm01codigo as codigoItemEnvase, itemEnvase.abm01na as naItemEnvase, bab01qt as qtProduzir, " +
				"baa01descr as descrPlano, abb01data as dataPlano,bab01dte as dataPrevPlano,bab01lote as lotePrincipal,bab01ctdti as ba10realdti, " +
				"bab01cthri as ba10realhi, bab01cthrf as ba10realhf, umuEnvase.aam06codigo as unidade, "+
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_1' AS numeric(10,2)) as desvio1, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_2' AS numeric(10,2)) as desvio2, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_3' AS numeric(10,2)) as desvio3, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_4' AS numeric(10,2)) as desvio4, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_5' AS numeric(10,2)) as desvio5, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_6' AS numeric(10,2)) as desvio6, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_7' AS numeric(10,2)) as desvio7, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_8' AS numeric(10,2)) as desvio8, " +
				"CAST(itemEnvase.abm01camposcustom ->>'desvio_9' AS numeric(10,2)) as desvio9, "+
				"itemComp.abm01tipo as tipoComponentePesagem,itemComp.abm01codigo as codItemComponentePesagem, " +
				"itemComp.abm01na as naItemComponentePesagem,umuComp.aam06codigo as umuPesagem, CAST(abm0101json ->> 'registro_mapa' AS text) AS registroMapa, " +
				"bab01011lote as lotePesagem, bab01011validade as validadePesagem, bab0101qtp as qtPlanPesagem, bab01lote as loteEnvase " +
				"from bab01  "+
				"inner join bab0101 on bab0101op = bab01id  "+
				"full join bab01011 on bab01011comp = bab0101id  "+
				"inner join abm01 as itemComp on itemComp.abm01id = bab0101item "+
				"inner join aam06 as umuComp on umuComp.aam06id = abm01umu  "+
				"inner join abp20 on abp20id = bab01comp  "+
				"inner join abm01 as itemEnvase on itemEnvase.abm01id = abp20item "+
				"INNER JOIN abm0101 ON abm0101item = itemEnvase.abm01id "+
				"inner join bab0103 on bab0103op = bab01id "+
				"inner join baa0101 on baa0101id = bab0103itempp "+
				"inner join baa01 on baa01id = baa0101plano "+
				"inner join abb01 on abb01id = bab01central "+
				"inner join abp10 on abp10id = bab01proc "+
				"inner join aam06 as umuEnvase on umuEnvase.aam06id = itemEnvase.abm01umu "+
				wherePlano +
				whereDoc +
				(dataIni != null && dataFin != null ? "and abb01data between :dataIni and :dataFin " : "" ) +
				(dtEntregaIni != null && dtEntregaFin != null ? "and bab01dte between :dtEntregaIni and :dtEntregaFin " : "" )+
				"order by abb01num ");

		if(wherePlano.length() > 0){
			if(idDocIni != null ) query.setParameter("idDocIni", idDocIni);
			if(idDocFin != null ) query.setParameter("idDocFin", idDocFin);
		}

		if(whereDoc.length() > 0){
			if(numOpIni != null ) query.setParameter("numOpIni", numOpIni);
			if(numOpFin != null ) query.setParameter("numOpFin", numOpFin);
		}

		if(dataIni != null && dataFin != null){
			query.setParameter("dataIni", dataIni);
			query.setParameter("dataFin", dataFin);
		}

		if(dtEntregaIni != null && dtEntregaFin != null){
			query.setParameter("dtEntregaIni", dtEntregaIni);
			query.setParameter("dtEntregaFin", dtEntregaFin);
		}

		return query.getListTableMap();

	}
	private List<TableMap> buscarItensComponentes(List<Integer> numDoc){

		return getSession().createQuery(
				"select abm01tipo as tipoComponenteGeral,abm01codigo as codItemComponenteGeral,abm01na as naItemComponenteGeral,aam06codigo as umuPesagemGeral,bab01011lote as lotePesagemGeral, bab01011validade as validadePesagemGeral, bab0101qtp as qtPlanPesagemGeral from bab01 "+
						"inner join bab0101 on bab0101op = bab01id "+
						"full join bab01011 on bab01011comp = bab0101id "+
						"inner join abb01 on abb01id = bab01central "+
						"inner join abm01 on abm01id = bab0101item "+
						"inner join aam06 on aam06id = abm01umu "+
						"where abb01num in (:numDoc) "+
						"and abm01tipo = 0 "+
						"order by abm01codigo")
				.setParameter("numDoc", numDoc)
				.getListTableMap();
	}
	private List<TableMap> buscarProcessosAtividades(String codProcesso){
		return getSession().createQuery(
				"select abb20codigo,abb20nome,abp10descr, abp01descr, CAST(abp01camposcustom ->>'temp' AS numeric(18,6)) as temper, CAST(abp01camposcustom ->>'agitacao' AS character varying(18)) as agitacao "+
						"from abp1001 "+
						"inner join abp10 on abp10id = abp1001proc "+
						"inner join  abp01 on abp01id = abp1001ativ "+
						"left join abb20 on abb20id = abp01bem "+
						"where abp10codigo = :codProcesso ").setParameter("codProcesso",codProcesso).getListTableMap();
	}
	private TableMap buscarItensSemi(Integer numDoc) {
		return getSession().createQuery(
				"select bab0101qtp as qtPlanSemi, semi.abm01na as naItemSemi, semi.abm01codigo as codItemSemi, aam06codigo as umuSemi from bab01 "+
						"inner join abb01 on abb01id = bab01central "+
						"inner join abp20 on abp20id = bab01comp "+
						"inner join abm01 as princ on abm01id = abp20item and abm01tipo = 1 "+
						"inner join bab0101 on bab0101op=bab01id "+
						"inner join abp20 as compSemi on compSemi.abp20item = bab0101item "+
						"inner join abm01 as semi on semi.abm01id = bab0101item "+
						"inner join aam06 on aam06id = semi.abm01umu "+
						"where abb01num = :numDoc "+
						"group by bab0101qtp, semi.abm01na, semi.abm01codigo, aam06codigo")
				.setParameter("numDoc", numDoc)
				.setMaxResult(1)
				.getUniqueTableMap();
	}
	private BigDecimal quantidadeCaixas(String codigoItem){
		String sql = "select CAST(abm0101json ->>'volume_caixa' AS numeric(14,6)) as volumeCaixa "+
				"from abm01 "+
				"inner join abm0101 on abm01id = abm0101item "+
				"where abm01codigo = :codigoItem and abm0101empresa = '1322578' and abm01tipo = '1' "

		Parametro parametroCodigo = criarParametroSql("codigoItem",codigoItem );
		return getAcessoAoBanco().obterBigDecimal(sql,parametroCodigo);
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
//meta-sis-eyJkZXNjciI6IkNTVCAtIE9yZGVtIGRlIEVudmFzZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==