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
import sam.model.entities.aa.Aah01
import sam.model.entities.aa.Aaj05
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe03
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm12
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_2010 extends FormulaBase {
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
					 " INNER JOIN Abe01 ON abe01id = abb01ent " +
					 " INNER JOIN Abe03 ON abe03ent = abe01id " +
					 " WHERE eaa01clasDoc = 1 AND eaa01esMov = 0 AND " +
					 " eaa01iReinf = 1 AND abe03prRural = 0 AND " +
					 " eaa01cancData IS NULL AND " +
					 " eaa01eg IN (:aac01ids) AND " +
					 Fields.numMeses(Fields.month("eaa01esData").toString(), Fields.year("eaa01esData").toString()) + " = :numMeses " +
					 " GROUP BY eaa01eg, abb01ent";
					 
		def numMeses = periodo.year * 12 + periodo.monthValue; 
		
		List<TableMap> entidadesPorEmpresa = getAcessoAoBanco().buscarListaDeTableMap(sql, criarParametroSql("aac01ids", empresas), criarParametroSql("numMeses", numMeses));
		for(int i = 0; i < entidadesPorEmpresa.size(); i++) {
			TableMap tm = entidadesPorEmpresa.get(i);
			
			String sqlNotas = " SELECT * FROM Eaa01 " +
							  " INNER JOIN Abb01 ON abb01id = eaa01central " +
							  " INNER JOIN Abe01 ON abe01id = abb01ent " +
							  " INNER JOIN Abe03 ON abe03ent = abe01id " +
							  " WHERE eaa01clasDoc = 1 AND eaa01esMov = 0 AND " +
							  " eaa01iReinf = 1 AND abe03prRural = 0 AND " +
							  " abb01ent = :abe01id AND " +
							  " eaa01cancData IS NULL AND " +
							  " eaa01eg IN (:eg) " +
							  obterWherePadrao("abe03","AND") + 
							  " AND " + Fields.numMeses(Fields.month("eaa01esData").toString(), Fields.year("eaa01esData").toString()) + " = :numMeses " +
							  " ORDER BY abb01ent, abb01tipo, abb01num, eaa01esData";
			List<Eaa01> eaa01s = getAcessoAoBanco().buscarListaDeRegistros(sqlNotas, criarParametroSql("abe01id", tm.getLong("abb01ent")), criarParametroSql("eg", tm.getLong("eaa01eg")), criarParametroSql("numMeses", numMeses));
			
			Abe01 abe01Prestador = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe01 WHERE abe01id = :abe01id", criarParametroSql("abe01id", tm.getLong("abb01ent")));
			Abe03 abe03 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe03 WHERE abe03ent = :abe01id", criarParametroSql("abe01id", tm.getLong("abb01ent")));
			
			ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtTomadorServicos/v1_05_01");
			
			ElementXml evtServTom = reinf.addNode("evtServTom");
			evtServTom.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
		
			ElementXml ideEvento = evtServTom.addNode("ideEvento");
			def tags = Arrays.asList("nrInsc", "nrInscEstab", "cnpjPrestador");
			def nrInsc = StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8);
			def nrInscEstab = abe03.abe03ccEmpreita_Zero == 0 ? StringUtils.extractNumbers(aac10.aac10ni) : abe03.abe03ccCno;
			def cnpjPrestador = StringUtils.extractNumbers(abe01Prestador.abe01ni);
			def valores = Arrays.asList(nrInsc, nrInscEstab, cnpjPrestador);
			String recibo = isRetificacao ? reinfService.buscarAaa17RetRecPeloLayoutPeriodo("R-2010", aac10.aac10id, periodo, tags, valores) : null;
			ideEvento.addNode("indRetif", isRetificacao && recibo != null ? 2 : 1, true);
			if(recibo != null) ideEvento.addNode("nrRecibo", recibo, false);
			
			ideEvento.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true);
			ideEvento.addNode("tpAmb", tpAmb, true);
			ideEvento.addNode("procEmi", "1", true);
			ideEvento.addNode("verProc", Utils.getVersao(), true);
			
			ElementXml ideContri = evtServTom.addNode("ideContri");
			ideContri.addNode("tpInsc", aac10.aac10ti.equals(0) ? "1" : "2", true);
			ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
			
			def vlrTotalBruto = 0;
			def vlrTotalBaseRet = 0;
			def vlrTotalRetPrinc = 0;
			def vlrTotalRetAdic = 0;
			def vlrTotalNRetPrinc = 0;
			def vlrTotalNRetAdic = 0;
			
			ElementXml infoServTom = evtServTom.addNode("infoServTom");
			ElementXml ideEstabObra = infoServTom.addNode("ideEstabObra");
			ideEstabObra.addNode("tpInscEstab", abe03.abe03ccEmpreita_Zero == 0 ? 1 : 4, true);
			ideEstabObra.addNode("nrInscEstab", abe03.abe03ccEmpreita_Zero == 0 ? StringUtils.extractNumbers(aac10.aac10ni) : abe03.abe03ccCno, true);
			ideEstabObra.addNode("indObra", abe03.abe03ccEmpreita_Zero, true);
			
			for(Eaa01 eaa01 in eaa01s) {
				TableMap eaa01json = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;
				vlrTotalBruto = vlrTotalBruto + eaa01json.getBigDecimal_Zero(getCampo("R-2010", "vlrTotalBruto"));
				vlrTotalBaseRet = vlrTotalBaseRet + eaa01json.getBigDecimal_Zero(getCampo("R-2010", "vlrTotalBaseRet"));
				vlrTotalRetPrinc = vlrTotalRetPrinc + eaa01json.getBigDecimal_Zero(getCampo("R-2010", "vlrTotalRetPrinc"));
				vlrTotalRetAdic = vlrTotalRetAdic + eaa01json.getBigDecimal_Zero(getCampo("R-2010", "vlrTotalRetAdic"));
				vlrTotalNRetPrinc = vlrTotalNRetPrinc + eaa01json.getBigDecimal_Zero(getCampo("R-2010", "vlrTotalNRetPrinc"));
				vlrTotalNRetAdic = vlrTotalNRetAdic + eaa01json.getBigDecimal_Zero(getCampo("R-2010", "vlrTotalNRetAdic"));
			}
			
			ElementXml idePrestServ = ideEstabObra.addNode("idePrestServ");
			idePrestServ.addNode("cnpjPrestador", StringUtils.extractNumbers(abe01Prestador.abe01ni), true);
			
			idePrestServ.addNode("vlrTotalBruto", ReinfUtils.formatarDecimal(vlrTotalBruto, 2, false), true);
			idePrestServ.addNode("vlrTotalBaseRet", ReinfUtils.formatarDecimal(vlrTotalBaseRet, 2, false), true);
			idePrestServ.addNode("vlrTotalRetPrinc", ReinfUtils.formatarDecimal(vlrTotalRetPrinc, 2, false), true);
			idePrestServ.addNode("vlrTotalRetAdic", ReinfUtils.formatarDecimal(vlrTotalRetAdic, 2, true), false);
			idePrestServ.addNode("vlrTotalNRetPrinc", ReinfUtils.formatarDecimal(vlrTotalNRetPrinc, 2, true), false);
			idePrestServ.addNode("vlrTotalNRetAdic", ReinfUtils.formatarDecimal(vlrTotalNRetAdic, 2, true), false);
			idePrestServ.addNode("indCPRB", abe03.abe03contCprb_Zero == 0 ? 0 : 1, true);
			
			for(Eaa01 eaa01 in eaa01s) {
				Abb01 abb01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abb01 WHERE abb01id = :abb01id", criarParametroSql("abb01id", eaa01.eaa01central.abb01id));
				TableMap eaa01json = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;
				
				ElementXml nfs = idePrestServ.addNode("nfs");
				nfs.addNode("serie", abb01.abb01serie == null ? 0 : abb01.abb01serie, true);
				nfs.addNode("numDocto", abb01.abb01num_Zero, true);
				nfs.addNode("dtEmissaoNF", ReinfUtils.formatarData(abb01.abb01data, ReinfUtils.PATTERN_YYYY_MM_DD), true);
				nfs.addNode("vlrBruto", ReinfUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("R-2010", "vlrBruto")), 2, false), true);
				nfs.addNode("obs", null, false);
				
				Map<String, TableMap> mapItens = agruparItens(eaa01, abb01);
				for(Object tpServico : mapItens.keySet()) {
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
			
			
			Aaa17 aaa17 = reinfService.comporAaa17("R-2010", periodo, isRetificacao);
			aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
			
			if(reinfService.confirmarGeracaoXML("R-2010", periodo, aaa17.getAaa17xmlEnvio(), "nrInsc", "cnpjPrestador")) {
				aaa17s.add(aaa17);
			}
		}
	
		put("aaa17s", aaa17s);
	}
	
	private Map<String, TableMap> agruparItens(Eaa01 eaa01, Abb01 abb01) {
		Map<String, TableMap> mapItens = new HashMap();
		
		def eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa01id ORDER BY eaa0103seq", criarParametroSql("eaa01id", eaa01.eaa01id));
		for(Eaa0103 eaa0103 : eaa0103s) {
			Abm0101 abm0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abm0101", Criterions.eq("abm0101item", eaa0103.eaa0103item.abm01id), Criterions.eq("abm0101empresa", aac10.aac10id));
			Abm12 abm12 = abm0101.abm0101fiscal == null ? null : getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id);
			Aaj05 aaj05 = abm12 == null ? null : abm12.abm12codServ == null ? null : getSession().get(Aaj05.class, abm12.abm12codServ.aaj05id);
			Aah01 aah01 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Aah01", Criterions.eq("aah01id", abb01.abb01tipo.aah01id));
			
			String servico = aaj05 == null ? null : aaj05.aaj05reinf;
			if(servico == null) {
				String tipo = eaa0103.eaa0103tipo == 0 ? "Mat-" : eaa0103.eaa0103tipo == 1 ? "Prod-" : eaa0103.eaa0103tipo == 2 ? "Merc-" : eaa0103.eaa0103tipo == 3 ? "Serv-" : "";
				String tipoDoc = aah01.aah01codigo + " - " + aah01.aah01nome;
				interromper("Necessário informar o tipo de Serviço (Reinf) no cadastro do item " + tipo + eaa0103.eaa0103codigo + ". Empresa: " + aac10.aac10codigo + " Documento: " + tipoDoc + " Número: " + abb01.abb01num);
			}
			
			TableMap tm = mapItens.containsKey(servico) ? mapItens.get(servico) : new TableMap();
			TableMap eaa0103json = eaa0103.eaa0103json == null ? new TableMap() : eaa0103.eaa0103json;
			
			tm.put("vlrBaseRet", tm.getBigDecimal_Zero("vlrBaseRet") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrBaseRet")));
			tm.put("vlrRetencao", tm.getBigDecimal_Zero("vlrRetencao") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrRetencao")));
			tm.put("vlrRetSub", tm.getBigDecimal_Zero("vlrRetSub") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrRetSub")));
			tm.put("vlrNRetPrinc", tm.getBigDecimal_Zero("vlrNRetPrinc") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrNRetPrinc")));
			tm.put("vlrServicos15", tm.getBigDecimal_Zero("vlrServicos15") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrServicos15")));
			tm.put("vlrServicos20", tm.getBigDecimal_Zero("vlrServicos20") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrServicos20")));
			tm.put("vlrServicos25", tm.getBigDecimal_Zero("vlrServicos25") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrServicos25")));
			tm.put("vlrAdicional", tm.getBigDecimal_Zero("vlrAdicional") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrAdicional")));
			tm.put("vlrNRetAdic", tm.getBigDecimal_Zero("vlrNRetAdic") + eaa0103json.getBigDecimal_Zero(getCampo("R-2010", "vlrNRetAdic")));
			
			mapItens.put(servico, tm);
		}
		
		return mapItens;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==