package multitec.relatorios.spv

import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.time.LocalDate
import java.util.stream.Collectors

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aah02
import sam.model.entities.ab.Abf40
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0113
import sam.model.entities.ea.Eaa01131
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource

class SPV_CancelamentoCupomFiscalSAT extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "SPV - Cancelamento de Cupom Fiscal SAT";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> tipos = getListLong("tipos");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidades = getListLong("entidades");
		LocalDate[] emissao = getIntervaloDatas("emissao");
		Long id = getLong("eaa01id");
		
		LocalDate dtEmisIni = null;
		LocalDate dtEmisFin = null;
		if(emissao != null) {
			dtEmisIni = emissao[0];
			dtEmisFin = emissao[1];
		}
		
		List<TableMap> listTMDados = null;
		if(id == null) {
			listTMDados = buscarCuponsFiscais(tipos, numeroInicial, numeroFinal, dtEmisIni, dtEmisFin, entidades);
		}else {
			listTMDados = buscarCupomFiscal(id);
		}
		
		if(listTMDados == null || listTMDados.size() == 0) throw new ValidacaoException("Não foram encontrados dados com base no filtro informado.");
				
		TableMap tmEmpresa = buscarDadosEmpresa();
		if(tmEmpresa == null) throw new ValidacaoException("Não foram encontrados dados da empresa.");
		String ie_emp = buscarInscricaoEstadualPorEstado(tmEmpresa.getLong("aac10id"), tmEmpresa.getLong("aag02id"));
		
		List<TableMap> listTMItens = new ArrayList();
		List<TableMap> listTMFormasPgto = new ArrayList();
		
		for(TableMap tmDoc : listTMDados) {
			Long eaa01id = tmDoc.getLong("eaa01id");
			tmDoc.put("key", eaa01id);
			
			if(tmEmpresa != null) tmDoc.putAll(tmEmpresa);
			tmDoc.put("ie_emp", ie_emp);
			
			TableMap eaa01json = tmDoc.getTableMap("eaa01json");
			if(eaa01json != null) tmDoc.putAll(eaa01json);
			
			Image imgQrCodeVenda = gerarQrCode(tmDoc.get("eaa0102pvQrCodeVenda"));
			tmDoc.put("imgQrCodeVenda", imgQrCodeVenda);
			
			Image imgQrCodeCanc = gerarQrCode(tmDoc.get("eaa0102pvQrCodeCanc"));
			tmDoc.put("imgQrCodeCanc", imgQrCodeCanc);
		}
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		
		return gerarPDF("SPV_CancelamentoCupomFiscalSAT", dsPrincipal);
	}
	
	private List<TableMap> buscarCuponsFiscais(List<Long> tipos, Integer numeroInicial, Integer numeroFinal,
		LocalDate dtEmisIni, LocalDate dtEmisFin, List<Long> entidades) {
				
		Query query = getSession().createQuery(" SELECT eaa01id, eaa01totItens, eaa01totDoc, eaa01totFinanc, eaa01json, ",
										" eaa01nfeChave, eaa01nfeStat, eaa01nfeData, eaa01nfeHora, eaa01obsFisco, eaa01obsContrib, ",
										" eaa0102codigo, eaa0102nome, eaa0102ddd, eaa0102fone, eaa0102eMail, eaa0102ni, ",
										" eaa0102pvNum, eaa0102pvComanda, eaa0102pvComprador, eaa0102pvTroco, eaa0102pvCPF, eaa0102pvQrCodeVenda, ",
										" abb01id, abb01num, abb01data, abb01operData, abb01operHora, aah01id, aah01codigo, aah01nome, abe01id, abe01codigo, abe01nome, abe01ni ",
										" abd10id, abd10codigo, abd10descr, abd10caixa, abd10serieFabr, abd10modelo, ",
										" eaa0102pvQrCodeCanc, eaa0102pvChaveCanc, eaa01cancData, eaa01cancHora, eaa01cancObs, ",
										" aae11codigo, aae11descr, aaj03codigo, aaj03descr ",
										" FROM Eaa01 ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" INNER JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" INNER JOIN Abd10 ON eaa01cfEF = abd10id ",
										" LEFT JOIN Aae11 ON eaa01cancMotivo = aae11id ",
										" LEFT JOIN Aaj03 ON aae11sitDoc = aaj03id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa0102pvNum IS NOT NULL AND eaa0102pvQrCodeVenda IS NOT NULL AND eaa01nfeChave IS NOT NULL ",
										" AND eaa01cancData IS NOT NULL AND eaa0102pvQrCodeCanc IS NOT NULL AND eaa0102pvChaveCanc IS NOT NULL ",
										(dtEmisIni != null && dtEmisFin != null ? " AND abb01data BETWEEN :dtEmisIni AND :dtEmisFin " : ""),
										" AND abb01num BETWEEN :numeroInicial AND :numeroFinal ",
										(tipos != null && tipos.size() > 0 ? " AND abb01tipo IN (:tipos) " : ""),
										(entidades != null && entidades.size() > 0 ? " AND abb01ent IN (:entidades) " : ""),
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" ORDER BY aah01codigo, abb01num");
									
		if(tipos != null && tipos.size() > 0) {
			query.setParameter("tipos", tipos);
		}
		
		if(entidades != null && entidades.size() > 0) {
			query.setParameter("entidades", entidades);
		}
		
		if(dtEmisIni != null && dtEmisFin != null) {
			query.setParameter("dtEmisIni", dtEmisIni);
			query.setParameter("dtEmisFin", dtEmisFin);
		}
		
		query.setParameters("esMov", Eaa01.ESMOV_SAIDA,
							"clasDoc", Eaa01.CLASDOC_SRF,
							"numeroInicial", numeroInicial,
							"numeroFinal", numeroFinal);
									  
		return query.getListTableMap();
	}
	
	private List<TableMap> buscarCupomFiscal(Long eaa01id) {
		Query query = getSession().createQuery(" SELECT eaa01id, eaa01totItens, eaa01totDoc, eaa01totFinanc, eaa01json, ",
										" eaa01nfeChave, eaa01nfeStat, eaa01nfeData, eaa01nfeHora, eaa01obsFisco, eaa01obsContrib, ",
										" eaa0102codigo, eaa0102nome, eaa0102ddd, eaa0102fone, eaa0102eMail, eaa0102ni, ",
										" eaa0102pvNum, eaa0102pvComanda, eaa0102pvComprador, eaa0102pvTroco, eaa0102pvCPF, eaa0102pvQrCodeVenda, ",
										" abb01id, abb01num, abb01data, abb01operData, abb01operHora, aah01id, aah01codigo, aah01nome, abe01id, abe01codigo, abe01nome, abe01ni ",
										" abd10id, abd10codigo, abd10descr, abd10caixa, abd10serieFabr, abd10modelo, ",
										" eaa0102pvQrCodeCanc, eaa0102pvChaveCanc, eaa01cancData, eaa01cancHora, eaa01cancObs, ",
										" aae11codigo, aae11descr, aaj03codigo, aaj03descr ",
										" FROM Eaa01 ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" INNER JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" INNER JOIN Abd10 ON eaa01cfEF = abd10id ",
										" LEFT JOIN Aae11 ON eaa01cancMotivo = aae11id ",
										" LEFT JOIN Aaj03 ON aae11sitDoc = aaj03id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa0102pvNum IS NOT NULL AND eaa0102pvQrCodeVenda IS NOT NULL AND eaa01nfeChave IS NOT NULL ",
										" AND eaa01cancData IS NOT NULL AND eaa0102pvQrCodeCanc IS NOT NULL AND eaa0102pvChaveCanc IS NOT NULL ",
										" AND eaa01id = :eaa01id ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" ORDER BY aah01codigo, abb01num");
									
		query.setParameters("esMov", Eaa01.ESMOV_SAIDA,
							"clasDoc", Eaa01.CLASDOC_SRF,
							"eaa01id", eaa01id);
									  
		return query.getListTableMap();
	}
	
	private TableMap buscarDadosEmpresa() {
		return getSession().createQuery(
				" SELECT aac10rs, aac10na, aac10endereco, aac10numero, aac10complem, ",
				" aac10bairro, aac10dddFone, aac10fone, aac10ni, aac10cep, aag0201nome as aag0201nome_emp, aag02id, aag02uf as aag02uf_emp ",
				" FROM Aac10 ",
				" LEFT JOIN Aag0201 ON aag0201id = aac10municipio ",
				" LEFT JOIN Aag02 ON aag02id = aag0201uf ",
				" WHERE aac10id = :idAac10 ")
			.setParameter("idAac10", getVariaveis().getAac10().getAac10id())
			.setMaxResult(1)
			.getUniqueTableMap();
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
	
	private String buscarInscricaoEstadualPorEstado(Long aac10id, Long aag02id) {		
		Aac1002 aac1002 = getSession().createCriteria(Aac1002.class)
						   .addJoin(Joins.join("Aac10", "aac10id = aac1002empresa"))
						   .addWhere(Criterions.eq("aac1002uf", aag02id))
						   .addWhere(Criterions.eq("aac1002empresa", aac10id))
						   .setMaxResults(1)
						   .get();
		return aac1002 == null ? aac1002 : aac1002.aac1002ie;
	}
	
}
//meta-sis-eyJkZXNjciI6IlNQViAtIENhbmNlbGFtZW50byBkZSBDdXBvbSBGaXNjYWwgU0FUIiwidGlwbyI6InJlbGF0b3JpbyJ9