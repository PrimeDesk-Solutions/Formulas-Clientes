package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.Variaveis
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abg02
import sam.model.entities.ed.Edb10
import sam.model.entities.ed.Edb1001
import sam.model.entities.ed.Edb10011
import sam.model.entities.ed.Edb100111
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_2060 extends FormulaBase {

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
		
		String sql = " SELECT * FROM Edb10 " + 
					 " INNER JOIN Edb1001 ON edb1001apur = edb10id " +
					 " INNER JOIN Aac10 ON aac10id = edb1001empresa " +
					 " INNER JOIN Edb10011 ON edb10011emp = edb1001id " + 
					 " INNER JOIN Abg02 ON abg02id = edb10011ativ" +
					 " LEFT JOIN Edb100111 ON edb100111ativ = edb10011id " +
					 " LEFT JOIN Abb40 ON abb40id = edb100111processo " +
					 " WHERE edb10ano = :ano AND edb10mes = :mes " + getSamWhere().getWherePadrao("AND", Edb10.class);
					 
		Edb10 edb10 = getAcessoAoBanco().buscarRegistroUnico(sql, criarParametroSql("ano", periodo.year), criarParametroSql("mes", periodo.monthValue));
		
		int index = 0;
		if (edb10 != null) {
				List<Edb1001> edb1001s = getSession().createCriteria(Edb1001.class)
														.addWhere(Criterions.eq("edb1001apur", edb10.edb10id))
														.getList(ColumnType.ENTITY)
			
			for (edb1001 in edb1001s) {				
				
				ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtInfoCPRB/v1_05_01");
				
				ElementXml evtCPRB = reinf.addNode("evtCPRB");
				evtCPRB.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
		
				ElementXml ideEvento = evtCPRB.addNode("ideEvento");
				String recibo = isRetificacao ? reinfService.buscarAaa17RetRecPeloLayoutPeriodo("R-2060", aac10.aac10id, periodo) : null;
				ideEvento.addNode("indRetif", isRetificacao && recibo != null ? 2 : 1, true);
				if(recibo != null) ideEvento.addNode("nrRecibo", recibo, false);
				
				ideEvento.addNode("perApur", ReinfUtils.formatarData(periodo, ReinfUtils.PATTERN_YYYY_MM), true);
				ideEvento.addNode("tpAmb", tpAmb, true);
				ideEvento.addNode("procEmi", "1", true);
				ideEvento.addNode("verProc", Utils.getVersao(), true);
				
				ElementXml ideContri = evtCPRB.addNode("ideContri");
				ideContri.addNode("tpInsc", 1, true);
				ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
				
				ElementXml infoCPRB = evtCPRB.addNode("infoCPRB");
				
				String aac10ni = getSession().createCriteria(Aac10.class).addFields("aac10ni").addWhere(Criterions.eq("aac10id", edb1001.edb1001empresa.aac10id)).get(ColumnType.STRING)
				ElementXml ideEstab = infoCPRB.addNode("ideEstab");
				ideEstab.addNode("tpInscEstab", "1", true);
				ideEstab.addNode("nrInscEstab", StringUtils.ajustString(StringUtils.extractNumbers(aac10ni), 14), true);
				ideEstab.addNode("vlrRecBrutaTotal", ReinfUtils.formatarDecimal(edb1001.edb1001rb_Zero, 2, false), true);
				
				def edb10011cprb = 0;
				List<Edb10011> edb10011s = getSession().createCriteria(Edb10011.class).addWhere(Criterions.eq("edb10011emp", edb1001.edb1001id)).getList(ColumnType.ENTITY)
				for (edb10011 in edb10011s) {
					edb10011cprb = edb10011cprb + edb10011.edb10011cprb_Zero;
				}
				ideEstab.addNode("vlrCPApurTotal", ReinfUtils.formatarDecimal(edb10011cprb, 2, false), true);
				
				def somaAjusteSuspensao = 0;
				for (edb10011 in edb10011s) {
					List<Edb100111> edb100111s = getSession().createCriteria(Edb100111.class).addWhere(Criterions.eq("edb100111ativ", edb10011.edb10011id)).getList(ColumnType.ENTITY)
					for (edb100111 in edb100111s) {
						somaAjusteSuspensao = somaAjusteSuspensao + edb100111.edb100111valor_Zero;
					}
				}
				ideEstab.addNode("vlrCPRBSuspTotal", ReinfUtils.formatarDecimal(somaAjusteSuspensao, 2, true), false);
								
				Collections.sort(edb10011s, new Comparator<Edb10011>() {
					@Override
					public int compare(Edb10011 o1, Edb10011 o2){
						String abg02codigo1 = getSession().createCriteria(Abg02.class).addFields("abg02codigo").addWhere(Criterions.eq("abg02id", o1.edb10011ativ.abg02id)).get(ColumnType.STRING)
						String abg02codigo2 = getSession().createCriteria(Abg02.class).addFields("abg02codigo").addWhere(Criterions.eq("abg02id", o2.edb10011ativ.abg02id)).get(ColumnType.STRING)
						return abg02codigo1.compareTo(abg02codigo2);
					}
				});
			
				for (edb10011 in edb10011s) {
					String abg02codigo = getSession().createCriteria(Abg02.class).addFields("abg02codigo").addWhere(Criterions.eq("abg02id", edb10011.edb10011ativ.abg02id)).get(ColumnType.STRING)
					ElementXml tipoCod = ideEstab.addNode("tipoCod");
					tipoCod.addNode("codAtivEcon", abg02codigo, true);
					tipoCod.addNode("vlrRecBrutaAtiv", ReinfUtils.formatarDecimal(edb10011.edb10011rb_Zero, 2, false), true);
					tipoCod.addNode("vlrExcRecBruta", ReinfUtils.formatarDecimal(edb10011.edb10011exc_Zero, 2, false), true);
					tipoCod.addNode("vlrAdicRecBruta", ReinfUtils.formatarDecimal(edb10011.edb10011adic_Zero, 2, false), true);
					tipoCod.addNode("vlrBcCPRB", ReinfUtils.formatarDecimal(edb10011.edb10011rb_Zero + edb10011.edb10011adic_Zero - edb10011.edb10011adic_Zero, 2, false), true);
					tipoCod.addNode("vlrCPRBapur", ReinfUtils.formatarDecimal(edb10011.edb10011cprb_Zero, 2, false), true);
					
					List<Edb100111> edb100111s = getSession().createCriteria(Edb100111.class).addWhere(Criterions.eq("edb100111ativ", edb10011.edb10011id)).getList(ColumnType.ENTITY)
					for (edb100111 in edb100111s) {
						ElementXml tipoAjuste = tipoCod.addNode("tipoAjuste");
						tipoAjuste.addNode("tpAjuste", edb100111.edb100111tipo, true);
						tipoAjuste.addNode("codAjuste", edb100111.edb100111reCodAj_Zero, true);
						tipoAjuste.addNode("vlrAjuste", ReinfUtils.formatarDecimal(edb100111.edb100111valor_Zero, 2, false), true);
						tipoAjuste.addNode("descAjuste", edb100111.edb100111descr, false, 20);
						tipoAjuste.addNode("dtAjuste", edb100111.edb100111data == null ? null : ReinfUtils.formatarData(edb100111.edb100111data, ReinfUtils.PATTERN_YYYY_MM), false);
					}
				}
				
				Aaa17 aaa17 = reinfService.comporAaa17("R-2060", periodo, isRetificacao);
				aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
				
				if(reinfService.confirmarGeracaoXML("R-2060", periodo, aaa17.getAaa17xmlEnvio(), "nrInsc", "nrInscEstab")) {
					aaa17s.add(aaa17);
				}
			}
		}
		
		put("aaa17s", aaa17s);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==