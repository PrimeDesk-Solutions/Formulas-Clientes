package Atilatte.formulas.scf

import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe02;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa1001;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;
import sam.server.scf.service.SCFService;

/**
 * Fórmula para manipular Movimento Financeiro
 *
 */
public class SCF_Movimento extends FormulaBase{

	private Daa1001 daa1001;

	@Override
	public void executar() {
		SCFService scfService = instanciarService(SCFService.class);
		daa1001 = (Daa1001) get("daa1001");



		Daa01 daa01 = null;

		// Central do Documento Financeiro
		Abb01 abb01 = daa1001.daa1001central;

		// Entidade
		Abe01 abe01 = abb01 != null ? abb01.abb01ent : null;

		// Entidade - Cliente
		Abe02 abe02 = abe01 != null ? getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id)) : null;

		// Campos Livres
		TableMap mapJson = daa1001.daa1001json == null ? new TableMap() : daa1001.daa1001json;
		TableMap jsonAbe02 = abe02 != null && abe02.abe02json != null ? abe02.abe02json : new TableMap();

		if(daa1001.daa1001central != null && daa1001.daa1001central.abb01id != null) daa01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Daa01 WHERE daa01central = :abb01id ", Parametro.criar("abb01id", daa1001.daa1001central.abb01id));
		def valor = daa1001.daa1001valor;

		//Juros = juros * qtd dias em atraso
		//Multa: considerar multa somente se estiver em atraso
		def diasAtraso = scfService.calculaDiasDeAtraso(daa1001);
		if(diasAtraso <= 0){
			mapJson.put("juros", 0.000000);
			mapJson.put("multa", 0.000000);
		}
		def juros = mapJson.getBigDecimal_Zero("juros");
		def multa = mapJson.getBigDecimal_Zero("multa");


		if (diasAtraso > 0 && mapJson.getInteger("jurosAjustado") != 1) {
			juros = mapJson.getBigDecimal_Zero("juros") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("juros") * diasAtraso, 2);
			multa = mapJson.getBigDecimal_Zero("multa") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("multa"), 2);
			mapJson.put("desconto", 0.000000);
			mapJson.put("jurosAjustado", 1);
		}

		//Encargos
		def encargos = mapJson.getBigDecimal_Zero("encargos") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("encargos"), 2);

		//Desconto: considerar desconto somente quando a data de pagamento for menor ou igual a data limite para desconto
		def desconto = 0;
		LocalDate dtLimDesc = mapJson.getDate("dt_limite_desc");

		if (dtLimDesc == null  || DateUtils.dateDiff(dtLimDesc, daa1001.daa1001dtPgto, ChronoUnit.DAYS) <= 0) {
			desconto = mapJson.getBigDecimal_Zero("desconto") == 0 ? 0 : round(mapJson.getBigDecimal_Zero("desconto"), 2);
		}

		//Se documento está com valor parcial, ajusta os valores de JMED também parcialmente
		if(daa01 != null && !valor.equals(daa01.getDaa01valor()) && mapJson.getInteger("ajusteParcial") != 1 ) {
			def fatorParcial = round(valor / daa01.getDaa01valor(), 6);

			if(juros != 0) juros = round(juros * fatorParcial, 2);
			if(multa != 0) multa = round(multa * fatorParcial, 2);
			if(encargos != 0) encargos = round(encargos * fatorParcial, 2);
			if(desconto != 0) desconto = round(desconto * fatorParcial, 2);
			mapJson.put("ajusteParcial", 1);
		}

		// Aplica  a taxa de desconto do cartão no valor do documento
		def txCartao = jsonAbe02.getBigDecimal_Zero("tx_cartao");
		def descontoCartao = (valor * (txCartao / 100)).abs() * -1

		//Setar JMED calculados, nos campos livres de quitação
		def jurosq = juros;
		def multaq = multa;
		def encargosq = mapJson.getBigDecimal_Zero("encargos") == 0 ? encargos : round(mapJson.getBigDecimal_Zero("encargos"), 2);
		BigDecimal descontoq = mapJson.getBigDecimal_Zero("desconto") == 0 ? desconto : round(mapJson.getBigDecimal_Zero("desconto"), 2);
		if(descontoq != null) descontoq = descontoq.abs() * -1

		if(txCartao > 0) descontoq = descontoCartao;

		// Desconto manual inserido na linha do documento, refaz o calculo do desconto do documento, zerando multa e juros
		if(mapJson.getBigDecimal_Zero("valor_desc_incond") > 0){
			jurosq = 0;
			multaq = 0;
			encargosq = 0;

			descontoq = (valor * (mapJson.getBigDecimal_Zero("valor_desc_incond") / 100)).abs()

			descontoq = descontoq * -1
		}
		
		mapJson.put("juros", jurosq);
		mapJson.put("multa", multaq);
		mapJson.put("encargos", encargosq);
		mapJson.put("desconto", descontoq);

		def valorLiquido = valor + jurosq + encargosq + multaq + descontoq;

		daa1001.daa1001liquido = round(valorLiquido, 2);
		daa1001.daa1001json = mapJson

	}
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_LCTOS_DE_MOVIMENTO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDgifQ==