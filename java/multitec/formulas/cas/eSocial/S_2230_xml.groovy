package multitec.formulas.cas.eSocial;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.fb.Fbb01;
import sam.model.entities.fb.Fbc01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;

public class S_2230_xml extends FormulaBase {
	
	private Aaa15 aaa15;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}
	
	@Override
	public void executar() {
		aaa15 = get("aaa15");
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);
		
		def tpAmb = 1;
		Fbb01 fbb01 = getAcessoAoBanco().buscarRegistroUnicoById("Fbb01", aaa15.aaa15registro);
		if(fbb01 == null) return;
		
        def indRetif = aaa15.aaa15tipo == Aaa15.TIPO_RETIFICACAO ? 2 : 1;
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtAfastTemp/v_S_01_03_00");
		
		ElementXml evtAfastTemp = eSocial.addNode("evtAfastTemp");
		evtAfastTemp.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));
		
		ElementXml ideEvento = evtAfastTemp.addNode("ideEvento");
		ideEvento.addNode("indRetif", indRetif, true);
		if(indRetif == 2) ideEvento.addNode("nrRecibo", getRecibo(), false);
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);
		
		ElementXml ideEmpregador = evtAfastTemp.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10.aac10ti+1, true);
		String ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10.aac10ti == 0) {
			ni = StringUtils.ajustString(ni, 14, '0', false).substring(0, 8);
		}else {
			ni = StringUtils.ajustString(ni, 11, '0', true);
		}
		ideEmpregador.addNode("nrInsc", ni, true);
		
		ElementXml ideVinculo = evtAfastTemp.addNode("ideVinculo");
		ideVinculo.addNode("cpfTrab", StringUtils.ajustString(StringUtils.extractNumbers(fbb01.fbb01trab.abh80cpf), 11, '0', true), true);
		ideVinculo.addNode("matricula", fbb01.fbb01trab.abh80codigo, true);
		
		if(fbb01.fbb01trab.abh80tipo != 0) {
			ideVinculo.addNode("codCateg", fbb01.fbb01trab?.abh80categ?.aap14eSocial?: null, false);
		}
		
		ElementXml infoAfastamento = evtAfastTemp.addNode("infoAfastamento");

		ElementXml iniAfastamento = infoAfastamento.addNode("iniAfastamento");
		iniAfastamento.addNode("dtIniAfast", ESocialUtils.formatarData(fbb01.fbb01dtSai, ESocialUtils.PATTERN_YYYY_MM_DD), true);
		iniAfastamento.addNode("codMotAfast", fbb01.fbb01ma.abh07eSocial, true);
		iniAfastamento.addNode("infoMesmoMtv", fbb01.fbb01mma == 1 ? "S" : "N", false);
		
		if(fbb01.fbb01ma.abh07eSocial != null && (fbb01.fbb01ma.abh07eSocial.equals("01") || fbb01.fbb01ma.abh07eSocial.equals("03"))) {
			if(fbb01.fbb01at != 0) iniAfastamento.addNode("tpAcidTransito", fbb01.fbb01at, false);
		}
		
		iniAfastamento.addNode("observacao", fbb01.fbb01motAfast, false);
		
		if (fbb01.fbb01ma.abh07eSocial.equalsIgnoreCase("15") && fbb01.fbb01fer != null) {
			Long fbc01id = getAcessoAoBanco().obterLong("SELECT fbc0101pa FROM Fbc0101 WHERE fbc0101id = :fbc0101id", criarParametroSql("fbc0101id", fbb01.fbb01fer.fbc0101id));
					
			def fbc01 = getSession().createCriteria(Fbc01.class)
					.addWhere(Criterions.eq("fbc01id",fbc01id))
					.get(ColumnType.ENTITY)
					
			ElementXml perAquis = iniAfastamento.addNode("perAquis");
			perAquis.addNode("dtInicio", ESocialUtils.formatarData(fbc01.fbc01pai, ESocialUtils.PATTERN_YYYY_MM_DD), true);
			perAquis.addNode("dtFim", ESocialUtils.formatarData(fbc01.fbc01paf, ESocialUtils.PATTERN_YYYY_MM_DD), false);
		}
		
		if (fbb01.fbb01sindCnpj != null && fbb01.fbb01sindCnpj.length() > 0) {
			ElementXml infoMandSind = iniAfastamento.addNode("infoMandSind");
			infoMandSind.addNode("cnpjSind", fbb01.fbb01sindCnpj, true);
			infoMandSind.addNode("infOnusRemun", fbb01.fbb01sindOnus, true);
		}

		if (fbb01.fbb01raProcesso != null) {
			ElementXml infoRetif = infoAfastamento.addNode("infoRetif");
			infoRetif.addNode("origRetif", fbb01.fbb01raOrigem, true);
			infoRetif.addNode("tpProc", fbb01.fbb01raProcesso.abb40tipo, true);
			infoRetif.addNode("nrProc", fbb01.fbb01raProcesso.abb40num, false);
		}
		
		if(fbb01.fbb01dtRet != null) {
			ElementXml fimAfastamento = infoAfastamento.addNode("fimAfastamento");
			fimAfastamento.addNode("dtTermAfast", ESocialUtils.formatarData(fbb01.fbb01dtRet.minusDays(1), ESocialUtils.PATTERN_YYYY_MM_DD), true);
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}

	
	private String getRecibo() {
		String sqlAaa15Anterior = " SELECT * FROM aaa15 " +
								  " WHERE aaa15id = (SELECT MAX(aaa15id) FROM aaa15 WHERE aaa15evento = :aaa15evento " +
								  " AND aaa15cnpj = :aaa15cnpj AND aaa15tabela = :aaa15tabela AND aaa15registro = :aaa15registro)";
								  
		Aaa15 aaa15Anterior = getAcessoAoBanco().buscarRegistroUnico(sqlAaa15Anterior, Parametro.criar("aaa15evento", aaa15.aaa15evento.aap50id),
			                                                                           Parametro.criar("aaa15cnpj", aaa15.aaa15cnpj), 
																					   Parametro.criar("aaa15tabela", aaa15.aaa15tabela),
																					   Parametro.criar("aaa15registro",aaa15.aaa15registro));
						
		return aaa15Anterior != null ? aaa15Anterior.aaa15retRec : null;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==