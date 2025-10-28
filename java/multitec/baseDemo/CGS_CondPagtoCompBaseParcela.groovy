package multitec.baseDemo

import java.time.LocalDate

import br.com.multitec.utils.Utils
import sam.dicdados.FormulaTipo
import sam.dto.cgs.ComposicaoBaseParcelaDto
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase

public class CGS_CondPagtoCompBaseParcela extends FormulaBase {
		
	private Eaa01 eaa01;
	private Integer vlrAuxiliar;
	
	@Override
	public void executar() {
		eaa01 = (Eaa01)get("eaa01"); // eaa01 - objeto passivo de ser manipulado para se obter data, condição de pagemento e valor 
		vlrAuxiliar = get("vlrAuxiliar");
		if(vlrAuxiliar == null) vlrAuxiliar = 0;
		
		if(eaa01 == null) return;
		
		if(eaa01.getEaa01central() == null) return;
		
		def dtBase = eaa01.eaa01central.getAbb01data(); //Data base considerada sendo a data do documento da central
		if(dtBase == null) return;
		
		dtBase = dtBase.plusDays(vlrAuxiliar);
		
		List<ComposicaoBaseParcelaDto> listaComposicaoBaseParcela = new ArrayList<>();
		
		Map<Long, BigDecimal> agrupamentoCondPgtoValor = agruparValorFinanceiroItensPorCondPgto(eaa01);
		
		if(agrupamentoCondPgtoValor != null && agrupamentoCondPgtoValor.size() > 0) {
			for(def abe30id : agrupamentoCondPgtoValor.keySet()) {
				ComposicaoBaseParcelaDto dto = new ComposicaoBaseParcelaDto(); // Objeto ComposicaoBaseParcelaDto contém os atributos dtBase, abe30id, valor
				dto.setDtBase(dtBase);
				dto.setAbe30id(abe30id);
				dto.setValor(agrupamentoCondPgtoValor.get(abe30id));
				
				listaComposicaoBaseParcela.add(dto);
			}
		}
		
		put("listaComposicaoBaseParcela", listaComposicaoBaseParcela); // listaComposicaoBaseParcela - retornando a lista de composição para a base das parcelas (deve ser retornada exatamente com esse nome/key)
	}
	
	/**
	 * Agrupa o valor financeiro (eaa0103totFinanc) dos itens por condição de pagamento
	 * @param eaa01 Eaa01
	 * @return Map&lt;Long,BigDecimal&gt
	 */
	private Map<Long, BigDecimal> agruparValorFinanceiroItensPorCondPgto(Eaa01 eaa01) {
		if(eaa01 == null) return;
		
		Map<Long, BigDecimal> map = new HashMap<>();
		
		def abe30id = eaa01.getEaa01cp() != null ? eaa01.getEaa01cp().getIdValue() : null;
		if(abe30id != null) map.put(abe30id, 0.0);
		
		def contemCondPagtoNosItens = false;
		if(!Utils.isEmpty(eaa01.getEaa0103s())) {
			for(Eaa0103 eaa0103 : eaa01.getEaa0103s()) {
				if(eaa0103.getEaa0103cp() != null) {
					contemCondPagtoNosItens = true;
					break;
				}
			}
		}
		
		if(contemCondPagtoNosItens) {
			if(!Utils.isEmpty(eaa01.getEaa0103s())) {
				for(Eaa0103 eaa0103 : eaa01.getEaa0103s()) {
					def eaa0103totFinanc = eaa0103.getEaa0103totFinanc_Zero();
					if(eaa0103totFinanc == null) eaa0103totFinanc = 0.0;
					
					def abe30idItem = eaa0103.getEaa0103cp() != null ? eaa0103.getEaa0103cp().getIdValue() : null;
					if(abe30idItem == null) abe30idItem = abe30id;
					
					def valor = 0.0;
					if(map.containsKey(abe30idItem)) {
						valor = map.get(abe30idItem);
						valor = valor + eaa0103totFinanc;
					}else {
						valor = eaa0103totFinanc;
					}
					
					map.put(abe30idItem, valor);
				}
			}
		}else {
			if(abe30id != null) {
				map.put(abe30id, eaa01.getEaa01totFinanc_Zero());
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