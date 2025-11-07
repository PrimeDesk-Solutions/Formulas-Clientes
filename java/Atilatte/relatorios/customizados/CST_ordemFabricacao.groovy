// Alterado SubRelatório 3
package Atilatte.relatorios.customizados;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;


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
import java.time.LocalDate



import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


public class CST_ordemFabricacao extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CST - Ordem de Fabricação ";
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

		params.put("empcodigo", getVariaveis().getAac10().getAac10codigo())
		params.put("empnome", getVariaveis().getAac10().getAac10na());;

		listTMDados = buscarDadosRelatorio(idDocIni, idDocFin, datas, datasEntrega, numOpIni, numOpFin);
		List<TableMap> listItens = new ArrayList();
		List<TableMap> listProcessos = new ArrayList();
		List<Integer> numDocs = new ArrayList<>();
		List<TableMap> requisicaoEstoque = new ArrayList<>()



		for(TableMap registro : listTMDados){
			Long idDocumento = registro.getLong("abb01id");
			Long idPlano = registro.getLong("baa01id")
			Integer numDocumento = registro.getInteger("abb01num");
			String codProcesso = registro.getString("abp10codigo");

			registro.put("key",idDocumento);
			//registro.put("key2",idPlano);

			List<TableMap> itensComponentes = buscarItensComponentes(numDocumento);
			List<TableMap> processos = buscarProcessosAtividades(codProcesso);


			for(TableMap processo : processos){
				processo.put("key",idDocumento);
				listProcessos.add(processo);
			}

			for(TableMap itens : itensComponentes){

				itens.put("key", idDocumento);
				listItens.add(itens)
			}

			 requisicaoEstoque = buscarRequisicaoestoque(numOpIni, numOpFin, idPlano );

			for(requisicao in requisicaoEstoque){
				requisicao.put("key", idDocumento);
			}

			String codbarsam = registro.getInteger("ba10num");
			registro.put("codbarsam",codbarsam );

			Image imgqrcode = gerarQrCode(codbarsam);
			registro.put("imgqrcode",imgqrcode)


		}



		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		dsPrincipal.addSubDataSource("DsSub1", listItens, "key", "key");
		dsPrincipal.addSubDataSource("DsSub2", listProcessos, "key", "key");
		dsPrincipal.addSubDataSource("DsSub3", requisicaoEstoque, "key", "key");


		adicionarParametro("StreamSub1", carregarArquivoRelatorio("ordemdefabricacao_s1"))
		adicionarParametro("StreamSub2", carregarArquivoRelatorio("ordemdefabricacao_s2"))
		adicionarParametro("StreamSub3", carregarArquivoRelatorio("ordemdefabricacao_s3"))



		return gerarPDF("CST_ordemdefabricacao", dsPrincipal);


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

		Query query = getSession().createQuery ("select abb01num,abb01id, abp10codigo, baa01id,abm01id,abm01codigo as ab50codigo, abm01na as aa50na,bab01qt as ba10qta, bab01qtp as qtdproduzida, baa01descr as ba01descr, abb01num as ba10num, abb01data as ba10data,bab01dte as ba10entrega,bab01lote as lotePrincipal,bab01ctdti as ba10realdti, bab01cthri as ba10realhi, bab01cthrf as ba10realhf, aam06codigo as unidade "+
				"from bab01 "+
				"inner join abp20 on abp20id = bab01comp "+
				"inner join abm01 on abm01id = abp20item "+
				"inner join bab0103 on bab0103op = bab01id "+
				"inner join baa0101 on baa0101id = bab0103itempp "+
				"inner join baa01 on baa01id = baa0101plano "+
				"inner join abb01 on abb01id = bab01central "+
				"inner join abp10 on abp10id = bab01proc "+
				"inner join aam06 on aam06id = abm01umu "+
				wherePlano +
				whereDoc +
				(dataIni != null && dataFin != null ? "and abb01data between :dataIni and :dataFin " : "" ) +
				(dtEntregaIni != null && dtEntregaFin != null ? "and bab01dte between :dtEntregaIni and :dtEntregaFin " : "" ) );

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
	private List<TableMap> buscarItensComponentes(Integer numDoc){

		return getSession().createQuery(
				"select abm01codigo as codigoItemComp,abm01na as descrItemComp,aam06codigo,bab01011lote, bab01011validade, bab01011qt as bc02qt from bab01 "+
						"inner join bab0101 on bab0101op = bab01id "+
						"full join bab01011 on bab01011comp = bab0101id "+
						"inner join abb01 on abb01id = bab01central "+
						"inner join abm01 on abm01id = bab0101item "+
						"inner join aam06 on aam06id = abm01umu "+
						"where abb01num = :numDoc "+
						"order by bab0101seq")
				.setParameter("numDoc", numDoc)
				.getListTableMap();
	}
	private List<TableMap> buscarProcessosAtividades(String codProcesso){
		return getSession().createQuery(
				"select abp1001seq,abb20codigo,abb20nome,abp10descr, abp01descr, CAST(abp01camposcustom ->>'temp' AS numeric(18,6)) as temper, CAST(abp01camposcustom ->>'agitacao' AS character varying(18)) as agitacao "+
						"from abp1001 "+
						"inner join abp10 on abp10id = abp1001proc "+
						"inner join  abp01 on abp01id = abp1001ativ "+
						"left join abb20 on abb20id = abp01bem "+
						"where abp10codigo = :codProcesso " +
						"order by abp1001seq ").setParameter("codProcesso",codProcesso).getListTableMap();
	}

	private List<TableMap> buscarRequisicaoestoque(Integer numIni, Integer numFim, Long idPlano){

		return getSession().createQuery(
				"select abm01codigo as codigoItemComp,abm01na as descrItemComp,aam06codigo,bab01011lote, bab01011validade, sum(bab01011qt) as qtd " +
						"from bab01 " +
						"inner join bab0101 on bab0101op = bab01id " +
						"left join bab01011 on bab01011comp = bab0101id " +
						"inner join abb01 on abb01id = bab01central " +
						"inner join abm01 on abm01id = bab0101item " +
						"inner join aam06 on aam06id = abm01umu " +
						"inner join bab0103 on bab0103op = bab01id "+
						"inner join baa0101 on baa0101id = bab0103itempp "+
						"inner join baa01 on baa01id = baa0101plano "+
						"where abb01num between :numIni and :numFim " +
						"and baa01id = :idPlano "+
						"group by abm01codigo,abm01na,aam06codigo,bab01011lote, bab01011validade " +
						"order by abm01codigo ")

				.setParameter("numIni", numIni)
				.setParameter("numFim", numFim)
				.setParameter("idPlano", idPlano)
				.getListTableMap();
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
//meta-sis-eyJkZXNjciI6IkNTVCAtIE9yZGVtIGRlIEZhYnJpY2HDp8OjbyAiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNTVCAtIE9yZGVtIGRlIEZhYnJpY2HDp8OjbyAiLCJ0aXBvIjoicmVsYXRvcmlvIn0=