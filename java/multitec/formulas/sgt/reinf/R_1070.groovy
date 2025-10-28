package multitec.formulas.sgt.reinf

import java.time.LocalDate

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.xml.ElementXml
import sam.core.variaveis.MDate
import sam.core.variaveis.Variaveis
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa17
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.ab.Abb40
import sam.model.entities.ab.Abb4001
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ReinfUtils
import sam.server.sgt.service.ReinfService

class R_1070 extends FormulaBase {
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
		
		String sql = " SELECT * FROM Abb40 " +
					 getSamWhere().getWherePadrao("WHERE", Abb40.class) +
					 " ORDER BY abb40num ";
		
		List<Abb40> abb40s = getAcessoAoBanco().buscarListaDeRegistros(sql);
		
		if (!Utils.isEmpty(abb40s)) {
			for (abb40 in abb40s) {
				ElementXml reinf = ReinfUtils.configurarXML("http://www.reinf.esocial.gov.br/schemas/evtTabProcesso/v1_05_01");
				
				ElementXml evtTabProcesso = reinf.addNode("evtTabProcesso");
				evtTabProcesso.setAttribute("id", ReinfUtils.gerarID(aac10.aac10ti, aac10.aac10ni));
		
				ElementXml ideEvento = evtTabProcesso.addNode("ideEvento");
				ideEvento.addNode("tpAmb", tpAmb, true);
				ideEvento.addNode("procEmi", "1", true);
				ideEvento.addNode("verProc", Utils.getVersao(), true);
		
				ElementXml ideContri = evtTabProcesso.addNode("ideContri");
				ideContri.addNode("tpInsc", aac10.aac10ti.equals(0) ? "1" : "2", true);
				ideContri.addNode("nrInsc", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 8), true);
		
				ElementXml infoProcesso = evtTabProcesso.addNode("infoProcesso");
				int tipo = ReinfUtils.isInclusao(abb40.abb40esDti, periodo) ? 0 : 1;
				ElementXml operacao = infoProcesso.addNode(tipo == 0 ? "inclusao" : tipo == 1 ? "alteracao" : "exclusao");
		
				ElementXml ideProcesso = operacao.addNode("ideProcesso");
				ideProcesso.addNode("tpProc", abb40.abb40tipo == 0 ? "1" : "2", true);
				ideProcesso.addNode("nrProc", abb40.abb40num, true);
				ideProcesso.addNode("iniValid", tipo == 0 ? ReinfUtils.formatarData(MDate.date(), ReinfUtils.PATTERN_YYYY_MM) : ReinfUtils.formatarData(abb40.abb40esDti, ReinfUtils.PATTERN_YYYY_MM), true);
				ideProcesso.addNode("fimValid", null, false);
				ideProcesso.addNode("indAutoria", abb40.abb40autor, true);
				
				if(abb40.abb40tipo == 0){
					List<Abb4001> abb4001s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Abb4001 WHERE abb4001processo = :abb40id", criarParametroSql("abb40id", abb40.abb40id));
					for(Abb4001 abb4001 : abb4001s){
						ElementXml infoSusp = ideProcesso.addNode("infoSusp");
						infoSusp.addNode("codSusp", abb4001.abb4001codSusp, false);
						infoSusp.addNode("indSusp", StringUtils.ajustString(abb4001.abb4001tipoSusp, 2), true);
						infoSusp.addNode("dtDecisao", ReinfUtils.formatarData(abb4001.abb4001data, ReinfUtils.PATTERN_YYYY_MM_DD), true);
						infoSusp.addNode("indDeposito", abb4001.abb4001deposito_Zero == 0 ? "N" : "S", true);
					}
				}
		
				if(abb40.abb40tipo != 0){
					ElementXml dadosProcJud = ideProcesso.addNode("dadosProcJud");
					String sqlUf = " SELECT * FROM Aag02 WHERE aag02id IN (SELECT aag0201uf FROM Aag0201 WHERE aag0201id = :aag0201id)";
					Aag02 aag02 = abb40.abb40municipio != null ? getAcessoAoBanco().buscarRegistroUnico(sqlUf, criarParametroSql("aag0201id", abb40.abb40municipio.aag0201id)) : null;
					dadosProcJud.addNode("ufVara", aag02 != null ? aag02.aag02uf : null, true);
					String codMunic = abb40.abb40municipio != null ? getAcessoAoBanco().obterString("SELECT aag0201ibge FROM Aag0201 WHERE aag0201id = :aag0201id", criarParametroSql("aag0201id", abb40.abb40municipio.aag0201id)) : null;
					dadosProcJud.addNode("codMunic", codMunic, false);
					dadosProcJud.addNode("idVara", abb40.abb40vara, true);
				}
			
				if(tipo == 1) { // ALTERAÇÃO
					ElementXml novaValidade = operacao.addNode("novaValidade");
					novaValidade.addNode("iniValid", tipo == 0 ? ReinfUtils.formatarData(MDate.date(), ReinfUtils.PATTERN_YYYY_MM) : ReinfUtils.formatarData(abb40.abb40esDti, ReinfUtils.PATTERN_YYYY_MM), true);
					novaValidade.addNode("fimValid", null, false);
				}
				
				Aaa17 aaa17 = reinfService.comporAaa17("R-1070", periodo, isRetificacao);
				aaa17.setAaa17xmlEnvio(ReinfUtils.gerarXML(reinf));
				
				if(reinfService.confirmarGeracaoXML("R-1070", periodo, aaa17.getAaa17xmlEnvio(), "tpInsc", "tpProc", "nrProc")) {
					aaa17s.add(aaa17);
				}
			}
		}
		
		put("aaa17s", aaa17s);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODkifQ==