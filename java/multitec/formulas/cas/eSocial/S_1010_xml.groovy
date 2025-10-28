package multitec.formulas.cas.eSocial

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.aa.Aac12;

import java.time.LocalDate

import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.xml.ElementXml;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aaa15;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Abh21;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.ESocialUtils;
import sam.server.samdev.utils.Parametro

public class S_1010_xml extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		Integer tpAmb = 1;
		Abh21 abh21 = getAcessoAoBanco().buscarRegistroUnicoById("Abh21", aaa15.aaa15registro);
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(aaa15.aaa15eg.aac10id);

		Boolean isAlteracao = aaa15.aaa15tipo == Aaa15.TIPO_ALTERACAO
		Boolean isInclusao = aaa15.aaa15tipo == Aaa15.TIPO_INCLUSAO
		Boolean isExclusao = !(isInclusao || isAlteracao)
		Integer aac10ti = aac10.aac10ti + 1 // 1 - CNPJ / 2 - CPF
		
		Aaa15 ultimoAprovado = isAlteracao ? buscarUltimoAprovado(abh21.abh21id) : null
		LocalDate dataUltimoAprovado = ultimoAprovado != null ? ultimoAprovado.aaa15data : null
		
		String abh21esDti = ESocialUtils.formatarData(dataUltimoAprovado ? dataUltimoAprovado : abh21.abh21esDti, ESocialUtils.PATTERN_YYYY_MM) 
		String aac10ni = StringUtils.extractNumbers(aac10.aac10ni);
		if(aac10ti == 1) aac10ni = StringUtils.ajustString(aac10ni, 14, '0', false).substring(0, 8);
		if(aac10ti == 2) aac10ni = StringUtils.ajustString(aac10ni, 11, '0', true);
			
		
		ElementXml eSocial = ESocialUtils.criarElementXmlESocial("http://www.esocial.gov.br/schema/evt/evtTabRubrica/v_S_01_03_00");
		ElementXml evtTabRubrica = eSocial.addNode("evtTabRubrica");
		evtTabRubrica.setAttribute("Id", ESocialUtils.comporIdDoEvento(aac10.aac10ti, aac10.aac10ni));

		ElementXml ideEvento = evtTabRubrica.addNode("ideEvento");
		ideEvento.addNode("tpAmb", tpAmb, true);
		ideEvento.addNode("procEmi", "1", true);
		ideEvento.addNode("verProc", Utils.getVersao(), true);

		ElementXml ideEmpregador = evtTabRubrica.addNode("ideEmpregador");
		ideEmpregador.addNode("tpInsc", aac10ti, true);
		ideEmpregador.addNode("nrInsc", aac10ni, true);

		ElementXml infoRubrica = evtTabRubrica.addNode("infoRubrica");
		if(isInclusao || isAlteracao){
			ElementXml elemento = infoRubrica.addNode(isInclusao ? "inclusao" : "alteracao");
			ElementXml ideRubrica = elemento.addNode("ideRubrica");
			ideRubrica.addNode("codRubr", abh21.abh21codigo, true);
			ideRubrica.addNode("ideTabRubr", abh21.abh21codigo, true);
			ideRubrica.addNode("iniValid",abh21esDti , true);

			ElementXml dadosRubrica = elemento.addNode("dadosRubrica");
			dadosRubrica.addNode("dscRubr", abh21.abh21nome, true);
			dadosRubrica.addNode("natRubr", abh21.abh21esNatRub?.aap57codigo?: null, true);
			dadosRubrica.addNode("tpRubr", abh21.abh21esTipoRub, true);
			dadosRubrica.addNode("codIncCP", StringUtils.ajustString(abh21.abh21esPrev, 2, '0', true), true);
			dadosRubrica.addNode("codIncIRRF", StringUtils.ajustString(abh21.abh21esIr, 2, '0', true), true);
			dadosRubrica.addNode("codIncFGTS", StringUtils.ajustString(abh21.abh21esFgts, 2, '0', true), true);
			dadosRubrica.addNode("observacao", abh21.abh21obs, false);

			if(EmpresaTemPisPasep()){
				dadosRubrica.addNode("codIncPisPasep", abh21.abh21esPisPasep, true);
			}

			if(abh21.abh21esProcPS != null) {
				ElementXml ideProcessoCP = dadosRubrica.addNode("ideProcessoCP");
				ideProcessoCP.addNode("tpProc", abh21.abh21esProcPS.abb40tipo, true);
				ideProcessoCP.addNode("nrProc", abh21.abh21esProcPS.abb40num, true);
				ideProcessoCP.addNode("extDecisao", abh21.abh21esProcPS.abb40extDecisao, true);
				ideProcessoCP.addNode("codSusp", !abh21.abh21esProcPS.abb4001s.isEmpty() ? abh21.abh21esProcPS.abb4001s.stream().findFirst().get().abb4001codSusp : null, true);
			}
			if(abh21.abh21esProcIR != null) {
				ElementXml ideProcessoIRRF = dadosRubrica.addNode("ideProcessoIRRF");
				ideProcessoIRRF.addNode("nrProc", abh21.abh21esProcIR.abb40num, true);
				ideProcessoIRRF.addNode("codSusp", !abh21.abh21esProcIR.abb4001s.isEmpty() ? abh21.abh21esProcPS.abb4001s.stream().findFirst().get().abb4001codSusp : null, true);
			}
			if(abh21.abh21esProcFgts != null) {
				ElementXml ideProcessoFGTS = dadosRubrica.addNode("ideProcessoFGTS");
				ideProcessoFGTS.addNode("nrProc", abh21.abh21esProcFgts.abb40num, true);
			}

			if(abh21.abh21esProcPisPasep != null){
				ElementXml ideProcessoFGTS = dadosRubrica.addNode("ideProcessoPisPasep");
				ideProcessoFGTS.addNode("nrProc", abh21.abh21esProcPisPasep.abb40num, true);
				ideProcessoIRRF.addNode("codSusp", !abh21.abh21esProcPisPasep.abb4001s.isEmpty() ? abh21.abh21esProcPisPasep.abb4001s.stream().findFirst().get().abb4001codSusp : null, true);
			}

			if(isAlteracao) {
				ElementXml novaValidade = elemento.addNode("novaValidade");
				novaValidade.addNode("iniValid", LocalDate.now().format("yyyy-MM"), true);
			}
		}
		
		if(isExclusao) {
			ElementXml exclusao = infoRubrica.addNode("exclusao");
			ElementXml ideRubrica = exclusao.addNode("ideRubrica");
			ideRubrica.addNode("codRubr", abh21.abh21codigo, true);
			ideRubrica.addNode("ideTabRubr", abh21.abh21codigo, true);
			ideRubrica.addNode("iniValid", abh21esDti, true);
		}

		aaa15.setAaa15xmlEnvio(ESocialUtils.gerarXML(eSocial));
	}

	private boolean EmpresaTemPisPasep(){
		Integer aac12IndTribFolha = getSession().createCriteria(Aac12.class)
				.addFields("aac12indTribFolha")
				.addWhere(Criterions.eq("aac12empresa", obterEmpresaAtiva().getAac10id()))
				.get(ColumnType.INTEGER)
		return aac12IndTribFolha.equals(1);
	}

	private Aaa15 buscarUltimoAprovado(Long abh21id) {
		String sql = " SELECT * FROM Aaa15 INNER JOIN aap50 on aap50id = aaa15evento WHERE aap50evento = :aap50evento  " +
				" AND aaa15status = :aaa15status " +
				" AND aaa15registro = :aaa15registro " +
				" ORDER BY aaa15id  DESC";
		
		Aaa15 aaa15 = getAcessoAoBanco().buscarRegistroUnico(sql,
				Parametro.criar("aap50evento", "S-1010"),
				Parametro.criar("aaa15registro", abh21id),
				Parametro.criar("aaa15status", Aaa15.STATUS_APROVADO));
		return aaa15;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==