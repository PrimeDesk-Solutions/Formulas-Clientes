package multitec.formulas.sap;

import java.time.LocalDate;

import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ec.Eca02;
import sam.model.entities.ec.Ecb01;
import sam.model.entities.ec.Ecb0102;
import sam.server.samdev.formula.FormulaBase;

public class Valores extends FormulaBase {
	private Ecb01 ecb01;
	private LocalDate dataInicio;
  
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SAP;
	}

	public void executar() {
		ecb01 = (Ecb01)get("ecb01");
		dataInicio = get("dataInicio");
		
		if(ecb01 == null) throw new ValidacaoException("Necessário informar o bem imobilizado.");
		
		if(ecb01.ecb01dtImob == null) throw new ValidacaoException("Necessário infomar a data de imobilização.");
		
		if(ecb01.ecb01bem == null) throw new ValidacaoException("Necessário carregar o bem na imobilização.");
		
		if(ecb01.ecb01vlrAtual <= 0) throw new ValidacaoException("O valor a depreciar deve ser maior que zero.");
		
		//Obtendo o cálculo (número de meses) da data da última contabilização (ano * 12 + mes)
		def sql = " SELECT MAX(" + Fields.numMeses("eca02mes", "eca02ano") + ") FROM Eca02 " + getSamWhere().getWhereGc(" WHERE ", Eca02.class);
		def ultimaContabilizacao = getAcessoAoBanco().obterInteger(sql);
		
		def mesImob = null;
		def anoImob = null;
		
		def somaDeprContabilizadas = 0;
		def qtdDeprANaoConsiderar = 0;
		
		if(ecb01.ecb01bem.abb20baixa == null) {
			//Cálculo a partir da data de imobilização / início
			if(dataInicio == null) throw new ValidacaoException("Necessário infomar a data de início para o cálculo.");
			
			mesImob = dataInicio.getMonthValue();
			anoImob = dataInicio.getYear();
		}else {
			//Cálculo a partir da data da baixa
			LocalDate dataBaixa = ecb01.ecb01bem.abb20baixa;
			dataBaixa = dataBaixa.withDayOfMonth(1);
			dataBaixa = dataBaixa.plusMonths(1);
			
			mesImob = dataBaixa.getMonthValue();
			anoImob = dataBaixa.getYear();
		}
		
		def qtdMesesImobilizacao = DateUtils.numMeses(mesImob, anoImob);
		
		if(qtdMesesImobilizacao <= ultimaContabilizacao) throw new ValidacaoException("Data da imobilização/início/baixa deve ser maior que a data da última contabilização.");
		
		//Monta o map de depreciações zerando o valor da depreciação pois sem esse procedimento poderão ficar registros com valor (lixo),
		//visto que podem ser alteradas as depreciações
		Map<String, Ecb0102> mapDepreciacoes = new HashMap<String, Ecb0102>();
		
		if(ecb01.ecb0102s != null && ecb01.ecb0102s.size() > 0) {
			for(Ecb0102 ecb0102 : ecb01.ecb0102s) {
				def qtdMesesDepMes = DateUtils.numMeses(ecb0102.ecb0102mes, ecb0102.ecb0102ano);
				if(qtdMesesDepMes < qtdMesesImobilizacao) {
					somaDeprContabilizadas = somaDeprContabilizadas + ecb0102.ecb0102deprec;
					qtdDeprANaoConsiderar++;
				}else {
					ecb0102.ecb0102deprec = 0;
				}
				
				mapDepreciacoes.put(ecb0102.ecb0102mes + "/" + ecb0102.ecb0102ano, ecb0102);
			}
		}
		
		def taxa = 0;
		def valor = 0;
		def valorDepreciarMes = 0;
		def valorUltimaDepreciacao = 0;
		def qtdMesesDepreciar = 0;
		
		if(ecb01.ecb01txDepr > 0) {
			taxa = ecb01.ecb01txDepr;
			
			valor = ecb01.ecb01vlrAtual;
			valor = valor - somaDeprContabilizadas;
			
			//Gerar valores para depreciação
			qtdMesesDepreciar = (100 / taxa) * 12;
			qtdMesesDepreciar = round(qtdMesesDepreciar, 0);
			
			qtdMesesDepreciar = qtdMesesDepreciar - qtdDeprANaoConsiderar;
			
			valorDepreciarMes = (valor / qtdMesesDepreciar);
			valorDepreciarMes = round(valorDepreciarMes, 2);
			
			//Valor última depreciação
			valorUltimaDepreciacao = valorDepreciarMes + (valor - (valorDepreciarMes * qtdMesesDepreciar));
			valorUltimaDepreciacao = round(valorUltimaDepreciacao, 2);
		}
		
		LocalDate dataDepreciacao = LocalDate.of(anoImob, mesImob, 1);
		
		Set<Ecb0102> setEcb0102 = new HashSet<>();
		
		//Inclui as novas depreciações no map
		for(int i = 0; i < qtdMesesDepreciar; i++) {
			def mes = dataDepreciacao.getMonthValue();
			def ano = dataDepreciacao.getYear();
			def key = mes + "/" + ano;
			  
			Ecb0102 ecb0102 = mapDepreciacoes.get(key);
			if(ecb0102 == null) {
				ecb0102 = new Ecb0102();
				ecb0102.ecb0102mes = mes;
				ecb0102.ecb0102ano = ano;
				
				//Utilizando campo json
				TableMap mapJson = ecb0102.ecb0102json == null ? new TableMap() : ecb0102.ecb0102json;
				mapJson.put("sap_deprec_fiscal", BigDecimal.ZERO);
				ecb0102.setEcb0102json(mapJson);
				
			}
			  
			//Última depreciação
			if((i + 1) >= qtdMesesDepreciar) {
				valorDepreciarMes = valorUltimaDepreciacao;
			}
			
			//Utilizando campo json
			TableMap mapJson = ecb0102.ecb0102json == null ? new TableMap() : ecb0102.ecb0102json;
			mapJson.put("sap_deprec_fiscal", valorDepreciarMes);
			ecb0102.ecb0102json = mapJson;
			
			ecb0102.ecb0102deprec = valorDepreciarMes;
			
			ecb01.addToEcb0102s(ecb0102);
			
			setEcb0102.add(ecb0102);
		
			dataDepreciacao = dataDepreciacao.plusMonths(1);
		}
		
		//Limpando/removendo valores de depreciação zeradas
		for(def key : mapDepreciacoes.keySet()) {
			Ecb0102 ecb0102 = mapDepreciacoes.get(key);
			if(ecb0102.ecb0102deprec <= 0) {
				ecb01.ecb0102s.remove(ecb0102);
			}
		}

	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTgifQ==