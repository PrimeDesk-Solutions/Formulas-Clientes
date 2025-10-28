package multitec.formulas.cgs.condpgto

import java.time.LocalDate

import br.com.multitec.utils.Utils
import sam.dicdados.FormulaTipo
import sam.dto.cgs.ComposicaoBaseParcelaDto
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase

public class ComposicaoBaseParcelaPorDataEntrega extends FormulaBase {
		
	private Eaa01 eaa01;
	
	@Override
	public void executar() {
		eaa01 = (Eaa01)get("eaa01"); // eaa01 - objeto passivo de ser manipulado para se obter data, condição de pagemento e valor 
		
		if(eaa01 == null) return;
		
		if(eaa01.getEaa01central() == null) return;
		
		def dtBase = eaa01.getEaa01central().getAbb01data(); //Data base considerada sendo a data do documento da central
		if(dtBase == null) return;
		
		def abe30id = eaa01.getEaa01cp() != null ? eaa01.getEaa01cp().getIdValue() : null;
		if(abe30id == null) return; //Condição de pagamento considerada será a do documento Eaa01
		
		List<ComposicaoBaseParcelaDto> listaComposicaoBaseParcela = new ArrayList<>();
		
		Map<LocalDate, BigDecimal> agrupamentoDataEntregaValor = agruparValorFinanceiroItensPorDataEntrega(eaa01, dtBase);
		
		if(agrupamentoDataEntregaValor != null && agrupamentoDataEntregaValor.size() > 0) {
			for(def dataEntrega : agrupamentoDataEntregaValor.keySet()) {
				ComposicaoBaseParcelaDto dto = new ComposicaoBaseParcelaDto(); // Objeto ComposicaoBaseParcelaDto contém os atributos dtBase, abe30id, valor
				dto.setDtBase(dataEntrega);
				dto.setAbe30id(abe30id);
				dto.setValor(agrupamentoDataEntregaValor.get(dataEntrega));
				
				listaComposicaoBaseParcela.add(dto);
			}
		}
		
		put("listaComposicaoBaseParcela", listaComposicaoBaseParcela); // listaComposicaoBaseParcela - retornando a lista de composição para a base das parcelas (deve ser retornada exatamente com esse nome/key)
	}
	
	/**
	 * Agrupa o valor financeiro (eaa0103totFinanc) dos itens por data de entrega
	 * @param eaa01 Eaa01
	 * @return Map&lt;LocalDate,BigDecimal&gt
	 */
	private Map<LocalDate, BigDecimal> agruparValorFinanceiroItensPorDataEntrega(Eaa01 eaa01, LocalDate dataDefault) {
		if(eaa01 == null) return;
		if(dataDefault == null) return;
		
		Map<LocalDate, BigDecimal> map = new HashMap<>();
		
		if(!Utils.isEmpty(eaa01.getEaa0103s())) {
			for(Eaa0103 eaa0103 : eaa01.getEaa0103s()) {
				def eaa0103totFinanc = eaa0103.getEaa0103totFinanc();
				if(eaa0103totFinanc == null) eaa0103totFinanc = 0.0;
				
				def dataEntregaItem = eaa0103.getEaa0103dtEntrega();
				if(dataEntregaItem == null) dataEntregaItem = dataDefault;
				
				def valor = 0.0;
				if(map.containsKey(dataEntregaItem)) {
					valor = map.get(dataEntregaItem);
					valor = valor + eaa0103totFinanc;
				}else {
					valor = eaa0103totFinanc;
				}
				
				map.put(dataEntregaItem, valor);
			}
		}
		
		return map;
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.COMPOSICAO_BASE_PARCELA;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzIifQ==