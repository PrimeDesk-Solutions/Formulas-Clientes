package multitec.formulas.cas.eSocial;

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh02;
import sam.model.entities.ab.Abh0201;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro;
import sam.server.samdev.utils.ValidacaoESocial

public class S_1020_xml extends FormulaBase{

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		Integer tpAmb = 1;
		Abh02 abh02 = getAcessoAoBanco().buscarRegistroUnicoById("Abh02", aaa15.aaa15registro);
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);


		Boolean isAlteracao = aaa15.aaa15tipo == Aaa15.TIPO_ALTERACAO
		Boolean isInclusao = aaa15.aaa15tipo == Aaa15.TIPO_INCLUSAO
		Boolean isExclusao = !(isInclusao || isAlteracao)
		
		Integer aac10ti = aac10.aac10ti + 1 // 1 - CNPJ / 2 - CPF
		
		String niEmpregador = StringUtils.extractNumbers(aac10.aac10ni);
		String abh02esDti = ESocialUtils.formatarData(abh02.abh02esDti, ESocialUtils.PATTERN_YYYY_MM)
		String abh02ni_extractNumbers = StringUtils.extractNumbers(abh02.abh02ni);
		String abh02niContr = StringUtils.extractNumbers(abh02.abh02niContr)
		String abh02niProp = StringUtils.extractNumbers(abh02.abh02niProp);

		if( aac10ti == 1 ) niEmpregador = StringUtils.ajustString(niEmpregador, 14, '0', false).substring(0, 8);
		if( aac10ti == 2 ) niEmpregador = StringUtils.ajustString(niEmpregador, 11, '0', true);

		if( abh02.abh02ti == 0 ) abh02ni_extractNumbers = StringUtils.ajustString(abh02ni_extractNumbers, 14, '0', false);
		if( abh02.abh02ti != 0 ) abh02ni_extractNumbers = StringUtils.ajustString(abh02ni_extractNumbers, 11, '0', true);

		if( abh02.abh02tiContr == 0 ) abh02niContr = StringUtils.ajustString(abh02niContr, 14, '0', false);
		if( abh02.abh02tiContr != 0 ) abh02niContr = StringUtils.ajustString(abh02niContr, 11, '0', true);
		
		if( abh02.abh02tiProp == 0 ) abh02niProp = StringUtils.ajustString(abh02niProp, 14, '0', false);
		if( abh02.abh02tiProp != 0 ) abh02niProp = StringUtils.ajustString(abh02niProp, 11, '0', true);
		
			
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtTabLotacao/v_S_01_03_00");
		ElementXml evtTabLotacao = eSocial.addNode("evtTabLotacao");
		evtTabLotacao.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtTabLotacao.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtTabLotacao.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10ti, true);
		ideEmpregador.addNode("nrInsc", niEmpregador, true);

		ElementXml infoLotacao = evtTabLotacao.addNode("infoLotacao");
		if( isInclusao|| isAlteracao ) {
			ElementXml elemento = infoLotacao.addNode(aaa15.aaa15tipo == Aaa15.TIPO_INCLUSAO ? "inclusao" : "alteracao");

			ElementXml ideLotacao = elemento.addNode("ideLotacao");
			ideLotacao.addNode("codLotacao", abh02.abh02codigo, true);
			ideLotacao.addNode("iniValid", abh02esDti, true);

            ElementXml dadosLotacao = elemento.addNode("dadosLotacao");
            dadosLotacao.addNode("tpLotacao", StringUtils.ajustString(abh02.abh02tipo?.aap52codigo?: null, 2, '0', true), true);
            if(!ValidacaoESocial.isCodigoValido(["01", "10", "21", "24", "90", "91" ] as String[], abh02.abh02tipo.aap52codigo)) {
				dadosLotacao.addNode("tpInsc", abh02.abh02ti + 1, true);
                dadosLotacao.addNode("nrInsc", abh02ni_extractNumbers, true);
            }

			ElementXml fpasLotacao = dadosLotacao.addNode("fpasLotacao");
			fpasLotacao.addNode("fpas", abh02.abh02fpas?.aap51codigo?: null, true);
			fpasLotacao.addNode("codTercs", StringUtils.ajustString(abh02.abh02fpas?.aap51codTerc?: null, 4, '0', true), true);
			if(abh02.abh02fpas?.aap51codTercSusp != null) fpasLotacao.addNode("codTercsSusp", StringUtils.ajustString(abh02.abh02fpas?.aap51codTercSusp?: null, 4, '0', true), false);

			ElementXml infoProcJudTerceiros = fpasLotacao.addNode("infoProcJudTerceiros");

			String sql = "SELECT * FROM Abh0201 AS abh0201 INNER JOIN FETCH abh0201.abh0201processo AS abb40 " +
					"LEFT JOIN FETCH abb40.abb4001s AS abb4001s " +
					"WHERE abh0201lotacao = :abh02id " + getSamWhere().getWherePadrao(" AND ", Abh0201.class);

			List<Abh0201> abh0201s = getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("abh02id", abh02.abh02id));

			for (Abh0201 abh0201 : abh0201s) {
				ElementXml procJudTerceiro = infoProcJudTerceiros.addNode("procJudTerceiro");
				procJudTerceiro.addNode("codTerc", abh0201.abh0201codTerc, true);
				procJudTerceiro.addNode("nrProcJud", abh0201.abh0201processo?.abb40num?: null, true);
				procJudTerceiro.addNode("codSusp", abh0201.abh0201processo != null ? abh0201.abh0201processo.abb4001s.stream().findFirst() : null, false);
			}

            if(abh02.abh02niProp != null || abh02.abh02niContr != null){
                ElementXml infoEmprParcial = dadosLotacao.addNode("infoEmprParcial");
                infoEmprParcial.addNode("tpInscContrat", abh02.abh02tiContr + 1, true);
                infoEmprParcial.addNode("nrInscContrat", abh02niContr, true);
                infoEmprParcial.addNode("tpInscProp", abh02.abh02tiProp + 1, true);
                infoEmprParcial.addNode("nrInscProp", abh02niProp, true);
            }

			if(isAlteracao) {
				ElementXml novaValidade = elemento.addNode("novaValidade");
				novaValidade.addNode("iniValid", abh02esDti, true);
			}
		}
		if( isExclusao ){
			ElementXml exclusao = infoLotacao.addNode("exclusao");

			ElementXml ideLotacao = exclusao.addNode("ideLotacao");
			ideLotacao.addNode("codLotacao", abh02.abh02codigo, true);
			ideLotacao.addNode("iniValid", abh02esDti, true);
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==