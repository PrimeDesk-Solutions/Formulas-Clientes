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

class SPV_CupomFiscalSAT extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "SPV - Cupom Fiscal SAT";
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
		
		selecionarAlinhamento("0012");
		
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
		
		Aah02 aah02CpoLivreDesc = buscarCampoLivreDesconto();
				
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
			if(eaa01json != null) {
				BigDecimal vCFeLei12741 = eaa01json.get(getCampo("W22","vCFeLei12741"));
				eaa01json.put("vCFeLei12741", vCFeLei12741);
				
				tmDoc.putAll(eaa01json);
			}
			
			TableMap tmEnderecoPrincipal = buscarEnderecoPrincipalEntidadeDocumento(eaa01id);
			if(tmEnderecoPrincipal != null) tmDoc.putAll(tmEnderecoPrincipal);
			
			TableMap tmEnderecoEntrega = buscarEnderecoEntregaEntidadeDocumento(eaa01id);
			if(tmEnderecoEntrega != null) tmDoc.putAll(tmEnderecoEntrega);
			
			List<TableMap> listTMItensDoc = buscarItensDocumento(eaa01id);
			if(listTMItensDoc != null && listTMItensDoc.size() > 0) {
				for(TableMap tmItem : listTMItensDoc) {
					TableMap eaa0103json = tmItem.getTableMap("eaa0103json");
					if(eaa0103json != null) tmItem.putAll(eaa0103json);
					
					tmItem.put("key", eaa01id);
					
					BigDecimal cpoLivreDesc = tmItem.getBigDecimal_Zero(aah02CpoLivreDesc.aah02nome);
					tmItem.put("cpoLvreDesc", cpoLivreDesc);
					
					listTMItens.add(tmItem);
				}
			}
			
			comporFormasPagto(eaa01id, listTMFormasPgto);
			
			Image imgqrcode = gerarQrCode(tmDoc.get("eaa0102pvQrCodeVenda"));
			tmDoc.put("imgqrcode", imgqrcode);
		}
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);
		
		dsPrincipal.addSubDataSource("dsItens", listTMItens, "key", "key");
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPV_CupomFiscalSAT_S1"))
		
		dsPrincipal.addSubDataSource("dsFormasPgto", listTMFormasPgto, "key", "key");
		adicionarParametro("StreamSub2", carregarArquivoRelatorio("SPV_CupomFiscalSAT_S2"))
		
		return gerarPDF("SPV_CupomFiscalSAT", dsPrincipal);
	}
	
	private List<TableMap> buscarCuponsFiscais(List<Long> tipos, Integer numeroInicial, Integer numeroFinal,
		LocalDate dtEmisIni, LocalDate dtEmisFin, List<Long> entidades) {
				
		Query query = getSession().createQuery(" SELECT eaa01id, eaa01totItens, eaa01totDoc, eaa01totFinanc, eaa01json, ",
										" eaa01nfeChave, eaa01nfeStat, eaa01nfeData, eaa01nfeHora, eaa01obsFisco, eaa01obsContrib, ",
										" eaa0102codigo, eaa0102nome, eaa0102ddd, eaa0102fone, eaa0102eMail, eaa0102ni, ",
										" eaa0102pvNum, eaa0102pvComanda, eaa0102pvComprador, eaa0102pvTroco, eaa0102pvCPF, eaa0102pvQrCodeVenda, ",
										" abb01id, abb01num, abb01data, abb01operData, abb01operHora, aah01id, aah01codigo, aah01nome, abe01id, abe01codigo, abe01nome, abe01ni ",
										" abd10id, abd10codigo, abd10descr, abd10caixa, abd10serieFabr, abd10modelo ",
										" FROM Eaa01 ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" INNER JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" INNER JOIN Abd10 ON eaa01cfEF = abd10id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa0102pvNum IS NOT NULL AND eaa0102pvQrCodeVenda IS NOT NULL AND eaa01nfeChave IS NOT NULL ",
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
										" eaa01nfeChave, eaa01nfeStat, eaa01nfeData, eaa01nfeHora, ",
										" eaa0102codigo, eaa0102nome, eaa0102ddd, eaa0102fone, eaa0102eMail, eaa0102ni, ",
										" eaa0102pvNum, eaa0102pvComanda, eaa0102pvComprador, eaa0102pvTroco, eaa0102pvCPF, eaa0102pvQrCodeVenda, ",
										" abb01id, abb01num, abb01data, abb01operData, abb01operHora, aah01id, aah01codigo, aah01nome, abe01id, abe01codigo, abe01nome, abe01ni ",
										" abd10id, abd10codigo, abd10descr, abd10caixa, abd10serieFabr, abd10modelo ",
										" FROM Eaa01 ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Abe01 ON abb01ent = abe01id ",
										" INNER JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" INNER JOIN Abd10 ON eaa01cfEF = abd10id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa0102pvNum IS NOT NULL AND eaa0102pvQrCodeVenda IS NOT NULL AND eaa01nfeChave IS NOT NULL ",
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
	
	private TableMap buscarEnderecoPrincipalEntidadeDocumento(Long eaa01id) {
		return getSession().createQuery(
				" SELECT eaa0101local as eaa0101local_princ, eaa0101endereco as eaa0101endereco_princ, eaa0101numero as eaa0101numero_princ, eaa0101bairro as eaa0101bairro_princ, ",
				" eaa0101complem as eaa0101complem_princ, eaa0101cep as eaa0101cep_princ, eaa0101ddd as eaa0101ddd_princ, eaa0101fone as eaa0101fone_princ, eaa0101eMail as eaa0101eMail_princ, ",
				" aag0201nome as aag0201nome_princ, aag02uf as aag02uf_princ, aap15descr as aap15descr_princ, aag03nome as aag03nome_princ ",
				" FROM Eaa0101 ",
				" LEFT JOIN Aap15 ON aap15id = eaa0101tpLog ",
				" LEFT JOIN Aag0201 ON aag0201id = eaa0101municipio ",
				" LEFT JOIN Aag02 ON aag02id = aag0201uf ",
				" LEFT JOIN Aag03 ON aag03id = eaa0101regiao ",
				" WHERE eaa0101doc = :eaa01id AND eaa0101principal = :principal ")
			.setParameters("eaa01id", eaa01id,
						   "principal", 1)
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private TableMap buscarEnderecoEntregaEntidadeDocumento(Long eaa01id) {
		return getSession().createQuery(
				" SELECT eaa0101local as eaa0101local_entrega, eaa0101endereco as eaa0101endereco_entrega, eaa0101numero as eaa0101numero_entrega, eaa0101bairro as eaa0101bairro_entrega, ",
				" eaa0101complem as eaa0101complem_entrega, eaa0101cep as eaa0101cep_entrega, eaa0101ddd as eaa0101ddd_entrega, eaa0101fone as eaa0101fone_entrega, eaa0101eMail as eaa0101eMail_entrega, ",
				" aag0201nome as aag0201nome_entrega, aag02uf as aag02uf_entrega, aap15descr as aap15descr_entrega, aag03nome as aag03nome_entrega ",
				" FROM Eaa0101 ",
				" LEFT JOIN Aap15 ON aap15id = eaa0101tpLog ",
				" LEFT JOIN Aag0201 ON aag0201id = eaa0101municipio ",
				" LEFT JOIN Aag02 ON aag02id = aag0201uf ",
				" LEFT JOIN Aag03 ON aag03id = eaa0101regiao ",
				" WHERE eaa0101doc = :eaa01id AND eaa0101entrega = :entrega ")
			.setParameters("eaa01id", eaa01id,
						   "entrega", 1)
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private List<TableMap> buscarItensDocumento(Long eaa01id) {
		return getSession().createQuery(
				" SELECT eaa0103id, eaa0103doc, eaa0103seq, eaa0103codigo, eaa0103descr, eaa0103qtComl, eaa0103unit, eaa0103total, eaa0103totDoc, eaa0103totFinanc, eaa0103json, ",
				" aam06umComl.aam06codigo as aam06codigo_umComl ",
				" FROM Eaa0103 ",
				" LEFT JOIN Aam06 aam06umComl ON eaa0103umComl = aam06umComl.aam06id ",
				" WHERE eaa0103doc = :eaa01id ",
				" ORDER BY eaa0103seq")
			.setParameters("eaa01id", eaa01id)
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
	
	private Aah02 buscarCampoLivreDesconto() {
		String cpoLivreDesc = getAcessoAoBanco().buscarParametro("CPOLIVREDESC", "CC");
		if(cpoLivreDesc == null || cpoLivreDesc.length() == 0) throw new ValidacaoException("Não foi encontrado o parâmetro geral CC-CPOLIVREDESC.");
		
		Aah02 aah02CpoLivreDesc = session.createCriteria(Aah02.class)
										 .addFields("aah02id, aah02nome")
										 .addWhere(Criterions.eq("aah02nome", cpoLivreDesc))
										 .setMaxResults(1)
										 .get();
		
		if(aah02CpoLivreDesc == null) throw new ValidacaoException("Não foi encontrado o campo " + cpoLivreDesc + " definido no parâmetro geral CC-CPOLIVREDESC.");
		
		return aah02CpoLivreDesc;
	}
	
	private List<Eaa0113> buscarParcelasFormasPgto(Long eaa01id) {
		return session.createCriteria(Eaa0113.class)
					  .addJoin(Joins.fetch("eaa01131s").alias("eaa01131").left(true))
					  .addJoin(Joins.fetch("eaa01131.eaa01131fp").alias("abf40").left(true))
					  .addWhere(Criterions.eq("eaa0113doc", eaa01id))
					  .getList();
	}
	
	private void comporFormasPagto(Long eaa01id, List<TableMap> listTMFormasPgto) {
		List<Eaa0113> eaa0113s = buscarParcelasFormasPgto(eaa01id);
		
		Map<String, BigDecimal> mapPgtos = new HashMap<>();
				
		if(eaa0113s == null || eaa0113s.size() == 0) {
			mapPgtos.put("99", BigDecimal.ZERO);
		}else {
			for(Eaa0113 eaa0113 : eaa0113s) {
				if(eaa0113.eaa01131s != null && eaa0113.eaa01131s.size() > 0) {
					for(Eaa01131 eaa01131 : eaa0113.eaa01131s) {
						if(eaa01131.eaa01131valor < 0) continue;
						
						String meioPgto = eaa01131.eaa01131fp.abf40meioPgto;
						BigDecimal valorPgto = BigDecimal.ZERO;
						if(mapPgtos.containsKey(meioPgto)) {
							valorPgto = mapPgtos.get(meioPgto);
						}
						valorPgto = valorPgto + eaa01131.eaa01131valor;
						
						mapPgtos.put(meioPgto, valorPgto);
					}
				}else {
					String obs = eaa0113.eaa0113obs;
					if(obs == null) obs = "";
					
					String meioPgto = null;
					if(obs.toLowerCase().contains("cashback")) {
						meioPgto = "05";
					}else {
						meioPgto = "99";
					}
					
					BigDecimal valorPgto = BigDecimal.ZERO;
					if(mapPgtos.containsKey(meioPgto)) {
						valorPgto = mapPgtos.get(meioPgto);
					}
					valorPgto = valorPgto + eaa0113.eaa0113valor;
					
					mapPgtos.put(meioPgto, valorPgto);
				}
			}
		}
		
		Map<String, String> mapFP = new HashMap<>();
		Abf40.ABF40_ABF40MEIOPGTO.getLegendas().stream().forEach({legenda -> mapFP.put(legenda.value, legenda.label)});
				
		Set<String> setPgtos = mapPgtos.keySet().stream().sorted().collect(Collectors.toList());
		for(String cMP : setPgtos) {
			BigDecimal vMP = mapPgtos.get(cMP);
			
			TableMap tm = new TableMap();
			tm.put("eaa01id", eaa01id);
			tm.put("cMP", cMP);
			tm.put("dMP", mapFP.get(cMP));
			tm.put("vMP", vMP);
			tm.put("key", eaa01id);
			
			listTMFormasPgto.add(tm);
		}
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
//meta-sis-eyJkZXNjciI6IlNQViAtIEN1cG9tIEZpc2NhbCBTQVQiLCJ0aXBvIjoicmVsYXRvcmlvIn0=