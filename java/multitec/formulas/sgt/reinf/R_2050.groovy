package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.Variaveis
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_2050 extends FormulaBase {

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
		
		Aac10 aac10 = Variaveis.obter().getAac10();
		
		Integer tpAmb = 1;
		
		selecionarAlinhamento("0052");
		
		Set<Long> empresas = reinfService.selecionarEmpresasReinf();
		for (idEG in empresas) {
			String sql = " SELECT * FROM Eaa01 " +
						 " INNER JOIN Abb01 ON abb01id = eaa01central " +
						 " INNER JOIN Aac10 ON aac10id = eaa01eg " +
						 " INNER JOIN Aac13 ON aac13empresa = aac10id " +
						 " WHERE eaa01clasDoc = 1 AND eaa01esMov = 1 " +
						 " AND eaa01iReinf = 1 AND " +
						 " eaa01cancData IS NULL AND " +
						 " eaa01eg = :aac01id AND " +
						 " aac13reCom > 0 AND " +
						 Fields.numMeses(Fields.month("eaa01esData").toString(), Fields.year("eaa01esData").toString()) + " = :numMeses " +
						 " ORDER BY abb01tipo, abb01num, abb01data";
			
			def numMeses = periodo.year * 12 + periodo.monthValue;
			
			List<Eaa01> eaa01s = getAcessoAoBanco().buscarListaDeTableMap(sql, criarParametroSql("aac01id", idEG), criarParametroSql("numMeses", numMeses));
			
			Aac10 aac10EG = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aac10 WHERE aac10id = :id", criarParametroSql("id", idEG));
			
			ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtInfoProdRural/v1_05_01");
			
			ElementXml evtComProd = reinf.addNode("evtComProd");
			evtComProd.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
		
			ElementXml ideEvento = evtComProd.addNode("ideEvento");
			
			def tags = Arrays.asList("nrInsc");
			def nrInsc = StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8);
			def valores = Arrays.asList(nrInsc);
			String recibo = isRetificacao ? reinfService.buscarAaa17RetRecPeloLayoutPeriodo("R-2050", aac10.aac10id, periodo, tags, valores) : null;
			ideEvento.addNode("indRetif", isRetificacao && recibo != null ? 2 : 1, true);
			if(recibo != null) ideEvento.addNode("nrRecibo", recibo, false);
			
			ideEvento.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true);
			ideEvento.addNode("tpAmb", tpAmb, true);
			ideEvento.addNode("procEmi", "1", true);
			ideEvento.addNode("verProc", Utils.getVersao(), true);
			
			ElementXml ideContri = evtComProd.addNode("ideContri");
			ideContri.addNode("tpInsc", 1, true);
			ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
			
			ElementXml infoComProd = evtComProd.addNode("infoComProd");
			ElementXml ideEstab = infoComProd.addNode("ideEstab");
			ideEstab.addNode("tpInscEstab", "1", true);
			ideEstab.addNode("nrInscEstab", StringUtils.extractNumbers(aac10EG.aac10ni), true);
			
			def vlrRecBrutaTotal = 0;
			def vlrCPApur = 0;
			def vlrRatApur = 0;
			def vlrSenarApur = 0;
			def vlrCPSuspTotal = 0;
			def vlrRatSuspTotal = 0;
			def vlrSenarSuspTotal = 0;
			
			def totalTipoCom_1 = 0;
			def totalTipoCom_7 = 0;
			def totalTipoCom_8 = 0;
			def totalTipoCom_9 = 0;
			
			for(Eaa01 eaa01 in eaa01s) {
				TableMap eaa01json = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;
				
				def recBrutaTotal = eaa01json.getBigDecimal_Zero(getCampo("R-2050", "vlrRecBrutaTotal"));
				vlrRecBrutaTotal = vlrRecBrutaTotal + recBrutaTotal;
				vlrCPApur = vlrCPApur + eaa01json.getBigDecimal_Zero(getCampo("R-2050", "vlrCPApur"));
				vlrRatApur = vlrRatApur + eaa01json.getBigDecimal_Zero(getCampo("R-2050", "vlrRatApur"));
				vlrSenarApur = vlrSenarApur + eaa01json.getBigDecimal_Zero(getCampo("R-2050", "vlrSenarApur"));
				vlrCPSuspTotal = vlrCPSuspTotal + eaa01json.getBigDecimal_Zero(getCampo("R-2050", "vlrCPSuspTotal"));
				vlrRatSuspTotal = vlrRatSuspTotal + eaa01json.getBigDecimal_Zero(getCampo("R-2050", "vlrRatSuspTotal"));
				vlrSenarSuspTotal = vlrSenarSuspTotal + eaa01json.getBigDecimal_Zero(getCampo("R-2050", "vlrSenarSuspTotal"));
				
				def aac13reCom = getAcessoAoBanco().obterInteger("SELECT aac13reCom FROM Aac13 WHERE aac13empresa = :aac10id", criarParametroSql("aac10id", eaa01.eaa01eg.aac10id));
				if(aac13reCom == 1) totalTipoCom_1 = totalTipoCom_1 + recBrutaTotal;
				if(aac13reCom == 7) totalTipoCom_7 = totalTipoCom_7 + recBrutaTotal;
				if(aac13reCom == 8) totalTipoCom_8 = totalTipoCom_8 + recBrutaTotal;
				if(aac13reCom == 9) totalTipoCom_9 = totalTipoCom_9 + recBrutaTotal;
			}
			
			ideEstab.addNode("vlrRecBrutaTotal", ReinfUtils.formatarDecimal(vlrRecBrutaTotal, 2, false), true);
			ideEstab.addNode("vlrCPApur", ReinfUtils.formatarDecimal(vlrCPApur, 2, false), true);
			ideEstab.addNode("vlrRatApur", ReinfUtils.formatarDecimal(vlrRatApur, 2, false), true);
			ideEstab.addNode("vlrSenarApur", ReinfUtils.formatarDecimal(vlrSenarApur, 2, false), true);
			ideEstab.addNode("vlrCPSuspTotal", ReinfUtils.formatarDecimal(vlrCPSuspTotal, 2, true), false);
			ideEstab.addNode("vlrRatSuspTotal", ReinfUtils.formatarDecimal(vlrRatSuspTotal, 2, true), false);
			ideEstab.addNode("vlrSenarSuspTotal", ReinfUtils.formatarDecimal(vlrSenarSuspTotal, 2, true), false);
			
			if(!totalTipoCom_1.equals(0)) {
				ElementXml tipoCom = ideEstab.addNode("tipoCom");
				tipoCom.addNode("indCom", "1", true);
				tipoCom.addNode("vlrRecBruta", ReinfUtils.formatarDecimal(totalTipoCom_1, 2, false), true);
			}
			
			if(!totalTipoCom_7.equals(0)) {
				ElementXml tipoCom = ideEstab.addNode("tipoCom");
				tipoCom.addNode("indCom", "7", true);
				tipoCom.addNode("vlrRecBruta", ReinfUtils.formatarDecimal(totalTipoCom_7, 2, false), true);
			}
			
			if(!totalTipoCom_8.equals(0)) {
				ElementXml tipoCom = ideEstab.addNode("tipoCom");
				tipoCom.addNode("indCom", "8", true);
				tipoCom.addNode("vlrRecBruta", ReinfUtils.formatarDecimal(totalTipoCom_8, 2, false), true);
			}
			
			if(!totalTipoCom_9.equals(0)) {
				ElementXml tipoCom = ideEstab.addNode("tipoCom");
				tipoCom.addNode("indCom", "9", true);
				tipoCom.addNode("vlrRecBruta", ReinfUtils.formatarDecimal(totalTipoCom_9, 2, false), true);
			}
			
			Aaa17 aaa17 = reinfService.comporAaa17("R-2050", periodo, isRetificacao);
			aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
			
			if(reinfService.confirmarGeracaoXML("R-2050", periodo, aaa17.getAaa17xmlEnvio(), "nrInsc")) {
				aaa17s.add(aaa17);
			}
		}
		
		put("aaa17s", aaa17s);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==