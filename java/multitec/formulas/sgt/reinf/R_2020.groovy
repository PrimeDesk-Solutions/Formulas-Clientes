package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.Variaveis
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aaj05
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe03
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm12
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_2020 extends FormulaBase {
	private Aac10 aac10;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_REINF;
	}

	@Override
	public void executar() {
		ReinfService reinfService = instanciarService(ReinfService.class);
		
		boolean isRetificacao = get("isRetificacao");
		LocalDate periodo = get("periodo");
		List<Aaa17> aaa17s = new ArrayList();
		
		aac10 = Variaveis.obter().getAac10();
		
		Integer tpAmb = 1;
		
		selecionarAlinhamento("0052");
		
		Set<Long> empresas = reinfService.selecionarEmpresasReinf();
		String sql = " SELECT eaa01eg, abb01ent FROM Eaa01 " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " INNER JOIN Aac10 ON aac10id = eaa01eg " +
					 " INNER JOIN Aac13 ON aac13empresa = aac10id " + 
					 " WHERE eaa01clasDoc = 1 AND eaa01esMov = 1 AND eaa01iReinf = 1 AND " +
					 " eaa01cancData IS NULL AND eaa01eg IN (:aac01ids) AND " +
					 " aac13reCom = 0 AND " +
					 Fields.numMeses(Fields.month("abb01data").toString(), Fields.year("abb01data").toString()) + " = :numMeses " +
					 " GROUP BY eaa01eg, abb01ent";
					 
		def numMeses = periodo.year * 12 + periodo.monthValue;
		
		List<TableMap> entidadesPorEmpresa = getAcessoAoBanco().buscarListaDeTableMap(sql, criarParametroSql("aac01ids", empresas), criarParametroSql("numMeses", numMeses));
		for(int i = 0; i < entidadesPorEmpresa.size(); i++) {
			TableMap tm = entidadesPorEmpresa.get(i);
			
			String sqlNotas = " SELECT * FROM Eaa01 " +
							  " INNER JOIN Abb01 ON abb01id = eaa01central " +
							  " INNER JOIN Abe01 ON abe01id = abb01ent " +
							  " INNER JOIN Abe03 ON abe03ent = abe01id " +
							  " INNER JOIN Aac10 ON aac10id = eaa01eg " +
							  " INNER JOIN Aac13 ON aac13empresa = aac10id " +
							  " WHERE eaa01clasDoc = 1 AND eaa01esMov = 1 AND eaa01iReinf = 1 AND " +
							  " abb01ent = :abe01id AND aac13reCom = 0 AND " +
							  " eaa01cancData IS NULL AND eaa01eg IN (:eg) AND " +
							  Fields.numMeses(Fields.month("abb01data").toString(), Fields.year("abb01data").toString()) + " = :numMeses " +
							  " ORDER BY eaa01eg, abb01ent, abb01tipo, abb01num, abb01data";
		    
			List<Eaa01> notas = getAcessoAoBanco().buscarListaDeRegistros(sqlNotas, criarParametroSql("abe01id", tm.getLong("abb01ent")), criarParametroSql("eg", tm.getLong("eaa01eg")), criarParametroSql("numMeses", numMeses));
							  
			Aac10 aac10EG = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aac10 WHERE aac10id = :id", criarParametroSql("id", tm.getLong("eaa01eg")));
							  
			ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtPrestadorServicos/v1_05_01");
			
			ElementXml evtServPrest = reinf.addNode("evtServPrest");
			evtServPrest.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
			
			ElementXml ideEvento = evtServPrest.addNode("ideEvento");
			String recibo = isRetificacao ? reinfService.buscarAaa17RetRecPeloLayoutPeriodo("R-2020", aac10.aac10id, periodo) : null;
			ideEvento.addNode("indRetif", isRetificacao && recibo != null ? 2 : 1, true);
			if(recibo != null) ideEvento.addNode("nrRecibo", recibo, false);
			
			ideEvento.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true);
			ideEvento.addNode("tpAmb", tpAmb, true);
			ideEvento.addNode("procEmi", "1", true);
			ideEvento.addNode("verProc", Utils.getVersao(), true);
			
			ElementXml ideContri = evtServPrest.addNode("ideContri");
			ideContri.addNode("tpInsc", aac10.aac10ti.equals(0) ? "1" : "2", true);
			ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
			
			ElementXml infoServPrest = evtServPrest.addNode("infoServPrest");
			ElementXml ideEstabPrest = infoServPrest.addNode("ideEstabPrest");
			ideEstabPrest.addNode("tpInscEstabPrest", 1, true);
			ideEstabPrest.addNode("nrInscEstabPrest", StringUtils.extractNumbers(aac10EG.aac10ni), true);
			
			Abe03 abe03 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe03 WHERE abe03ent = :abe01id " + getSamWhere().getWherePadrao("AND", Abe03.class), criarParametroSql("abe01id", tm.getLong("abb01ent")));
			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe01 WHERE abe01id = :abe01id ", criarParametroSql("abe01id", tm.getLong("abb01ent")));
			
			ElementXml ideTomador = ideEstabPrest.addNode("ideTomador");
			ideTomador.addNode("tpInscTomador", abe03.abe03ccEmpreita_Zero == 0 ? 1 : 4, true);
			ideTomador.addNode("nrInscTomador", abe03.abe03ccEmpreita_Zero == 0 ? StringUtils.extractNumbers(abe01.abe01ni) : abe03.abe03ccCno, true);
			ideTomador.addNode("indObra", abe03.abe03ccEmpreita_Zero, true);
			
			def vlrTotalBruto = 0;
			def vlrTotalBaseRet = 0;
			def vlrTotalRetPrinc = 0;
			def vlrTotalRetAdic = 0;
			def vlrTotalNRetPrinc = 0;
			def vlrTotalNRetAdic = 0;
			
			for(Eaa01 eaa01 : notas) {
				TableMap eaa01json = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;
				vlrTotalBruto = vlrTotalBruto + eaa01json.getBigDecimal_Zero(getCampo("R-2020", "vlrTotalBruto"));
				vlrTotalBaseRet = vlrTotalBaseRet + eaa01json.getBigDecimal_Zero(getCampo("R-2020", "vlrTotalBaseRet"));
				vlrTotalRetPrinc = vlrTotalRetPrinc + eaa01json.getBigDecimal_Zero(getCampo("R-2020", "vlrTotalRetPrinc"));
				vlrTotalRetAdic = vlrTotalRetAdic + eaa01json.getBigDecimal_Zero(getCampo("R-2020", "vlrTotalRetAdic"));
				vlrTotalNRetPrinc = vlrTotalNRetPrinc + eaa01json.getBigDecimal_Zero(getCampo("R-2020", "vlrTotalNRetPrinc"));
				vlrTotalNRetAdic = vlrTotalNRetAdic + eaa01json.getBigDecimal_Zero(getCampo("R-2020", "vlrTotalNRetAdic"));
			}
			
			ideTomador.addNode("vlrTotalBruto", ReinfUtils.formatarDecimal(vlrTotalBruto, 2, false), true);
			ideTomador.addNode("vlrTotalBaseRet", ReinfUtils.formatarDecimal(vlrTotalBaseRet, 2, false), true);
			ideTomador.addNode("vlrTotalRetPrinc", ReinfUtils.formatarDecimal(vlrTotalRetPrinc, 2, false), true);
			ideTomador.addNode("vlrTotalRetAdic", ReinfUtils.formatarDecimal(vlrTotalRetAdic, 2, true), false);
			ideTomador.addNode("vlrTotalNRetPrinc", ReinfUtils.formatarDecimal(vlrTotalNRetPrinc, 2, true), false);
			ideTomador.addNode("vlrTotalNRetAdic", ReinfUtils.formatarDecimal(vlrTotalNRetAdic, 2, true), false);
			
			for(Eaa01 eaa01 : notas) {
				ElementXml nfs = ideTomador.addNode("nfs");
				nfs.addNode("serie", eaa01.eaa01central.abb01serie == null ? 0 : eaa01.eaa01central.abb01serie, true);
				nfs.addNode("numDocto", eaa01.eaa01central.abb01num_Zero, true);
				nfs.addNode("dtEmissaoNF", ReinfUtils.formatarData(eaa01.eaa01central.abb01data, ReinfUtils.PATTERN_YYYY_MM_DD), true);
				TableMap eaa01json = eaa01.eaa01json;
				nfs.addNode("vlrBruto", ReinfUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("R-2020", "vlrBruto")), 2, false), true);
				nfs.addNode("obs", null, false);
				
				Map<String, TableMap> mapItens = agruparItens(eaa01);
				for(String tpServico : mapItens.keySet()) {
					TableMap tmServ = mapItens.get(tpServico);
					ElementXml infoTpServ = nfs.addNode("infoTpServ");
					infoTpServ.addNode("tpServico", tpServico, true);
					infoTpServ.addNode("vlrBaseRet", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrBaseRet"), 2, false), true);
					infoTpServ.addNode("vlrRetencao", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrRetencao"), 2, false), true);
					infoTpServ.addNode("vlrRetSub", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrRetSub"), 2, true), false);
					infoTpServ.addNode("vlrNRetPrinc", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrNRetPrinc"), 2, true), false);
					infoTpServ.addNode("vlrServicos15", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrServicos15"), 2, true), false);
					infoTpServ.addNode("vlrServicos20", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrServicos20"), 2, true), false);
					infoTpServ.addNode("vlrServicos25", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrServicos25"), 2, true), false);
					infoTpServ.addNode("vlrAdicional", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrAdicional"), 2, true), false);
					infoTpServ.addNode("vlrNRetAdic", ReinfUtils.formatarDecimal(tmServ.getBigDecimal_Zero("vlrNRetAdic"), 2, true), false);
				}
			}
			
			
			Aaa17 aaa17 = reinfService.comporAaa17("R-2020", periodo, isRetificacao);
			aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
			
			if(reinfService.confirmarGeracaoXML("R-2020", periodo, aaa17.getAaa17xmlEnvio(), "nrInsc", "nrInscEstabPrest")) {
				aaa17s.add(aaa17);
			}
		}
		
		put("aaa17s", aaa17s);
	}
	
	private Map<String, TableMap> agruparItens(Eaa01 eaa01) {
		Map<String, TableMap> mapItens = new HashMap();
		for(Eaa0103 eaa0103 : eaa01.eaa0103s) {
			Abm0101 abm0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abm0101", Criterions.eq("abm0101item", eaa0103.eaa0103item.abm01id), Criterions.eq("abm0101empresa", aac10.aac10id));
			Abm12 abm12 = abm0101.abm0101fiscal == null ? null : getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id);
			Aaj05 aaj05 = abm12 == null ? null : abm12.abm12codServ == null ? null : getSession().get(Aaj05.class, abm12.abm12codServ.aaj05id);
			String servico = aaj05 == null ? null : aaj05.aaj05codigo;
			
			if(servico == null) {
				String tipo = eaa0103.eaa0103item.abm01tipo == 0 ? "Mat-" : eaa0103.eaa0103item.abm01tipo == 1 ? "Prod-" : eaa0103.eaa0103item.abm01tipo == 2 ? "Merc-" : eaa0103.eaa0103item.abm01tipo == 3 ? "Serv-" : "";
				String tipoDoc = eaa01.eaa01central.abb01tipo.aah01codigo + " - " + eaa01.eaa01central.abb01tipo.aah01nome;
				interromper("Necessário informar o tipo de Serviço (Reinf) no cadastro do item " + tipo + eaa0103.eaa0103item.abm01codigo + ". Empresa: " + aac10.aac10codigo + " Documento: " + tipoDoc + " Número: " + eaa01.eaa01central.abb01num);
			}
			
			TableMap tm = mapItens.containsKey(servico) ? mapItens.get(servico) : new TableMap();
			TableMap eaa0103json = eaa0103.eaa0103json;
			
			tm.put("vlrBaseRet", tm.getBigDecimal_Zero("vlrBaseRet") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrBaseRet")));
			tm.put("vlrRetencao", tm.getBigDecimal_Zero("vlrRetencao") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrRetencao")));
			tm.put("vlrRetSub", tm.getBigDecimal_Zero("vlrRetSub") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrRetSub")));
			tm.put("vlrNRetPrinc", tm.getBigDecimal_Zero("vlrNRetPrinc") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrNRetPrinc")));
			tm.put("vlrServicos15", tm.getBigDecimal_Zero("vlrServicos15") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrServicos15")));
			tm.put("vlrServicos20", tm.getBigDecimal_Zero("vlrServicos20") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrServicos20")));
			tm.put("vlrServicos25", tm.getBigDecimal_Zero("vlrServicos25") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrServicos25")));
			tm.put("vlrAdicional", tm.getBigDecimal_Zero("vlrAdicional") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrAdicional")));
			tm.put("vlrNRetAdic", tm.getBigDecimal_Zero("vlrNRetAdic") + eaa0103json.getBigDecimal_Zero(getCampo("R-2020", "vlrNRetAdic")));
			
			mapItens.put(servico, tm);
		}
		
		return mapItens;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==