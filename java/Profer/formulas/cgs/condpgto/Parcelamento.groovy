package multitec.formulas.cgs.condpgto

import java.time.DayOfWeek
import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import sam.dicdados.FormulaTipo
import sam.dto.cgs.ParcelaDto
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe3001
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.ab.Abe3002
import br.com.multitec.utils.collections.TableMap;


public class Parcelamento extends FormulaBase {
	
	private LocalDate dtBase;
	private Long abe30id;
	private BigDecimal valor;
	private Long abe01id;
	private Eaa01 eaa01;
	
	@Override
	public void executar() {
		dtBase = (LocalDate)get("dtBase");
		abe30id = (Long)get("abe30id");
		valor = (BigDecimal)get("valor");
		abe01id = (BigDecimal)get("abe01id");
		eaa01 = (Eaa01)get("eaa01");
		
		if(dtBase == null) return;
		
		if(abe30id == null) return;
		
		if(valor == null) return;
		
		List<ParcelaDto> listaParcelas = new ArrayList<>();
		
		Abe30 abe30 = buscarCondicaoPagamentoPorId(abe30id);
		if(abe30 == null) return;
		
		//Verificando dia da semana para a data base
		def diasAdicionaisDtBase = obterDiasAdicionaisAData(dtBase, abe30, 0);
		//Adicionando dias a data base
		dtBase = dtBase.plusDays(diasAdicionaisDtBase);
		
		def vlrSaldo = valor;
		
		List<Abe3001> abe3001s = buscarParcelasPeloIdCondicaoPagamento(abe30id);
		if(abe3001s != null && abe3001s.size() > 0) {
			int i = 1;
			for(Abe3001 abe3001 : abe3001s) {
				
				//Valor da parcela
				def vlrParcela = 0.0;
				if(i == abe3001s.size()) { //Última parcela
					vlrParcela = vlrSaldo;
				}else{					
					vlrParcela = round((valor * abe3001.getAbe3001perc_Zero()) / 100, 2);
					vlrSaldo = vlrSaldo - vlrParcela;
				}
				
				//Data de vencimento nominal
				def vctoN = LocalDate.of(dtBase.getYear(), dtBase.getMonth(), dtBase.getDayOfMonth());
				
				//Verificando vencimento fixo
				vctoN = vctoN.plusDays(abe3001.getAbe3001dias());

				//Verificando Ajustes Complementares
				def diaVctoN = vctoN.dayOfMonth;
				List<Abe3002> abe3002s = buscarDiaComplementarPeloIdCondicaoPagamento(abe30id);
				for(Abe3002 abe3002 : abe3002s) {
					TableMap tmAbe3002 = abe3002.abe3002json;
					int diai = tmAbe3002.getInteger("diai");
					int diaf = tmAbe3002.getInteger("diaf");
					int dia = vctoN.getMonthValue() == 2 ? 28 : tmAbe3002.getInteger("diavcto");
					int mesref = tmAbe3002.getInteger("refmes") == 0 ? 0 : 1;
					if(diaVctoN >= diai && diaVctoN <= diaf) {
						Integer mesAtual = vctoN.monthValue;
						vctoN = LocalDate.of(vctoN.getYear(), vctoN.getMonth() + mesref, dia);
						if (vctoN.monthValue < mesAtual) vctoN = LocalDate.of(vctoN.getYear() + 1, vctoN.getMonth(), dia);
					}
				}
				
				//Verificando dia da semana para a data de vencimento nominal
				def diasAdicionaisVctoN = obterDiasAdicionaisAData(vctoN, abe30, 1);
				
				//Adicionando dias a data de vencimento nominal
				vctoN = vctoN.plusDays(diasAdicionaisVctoN);
				
				ParcelaDto parcelaDto = new ParcelaDto();
				parcelaDto.vctoN = vctoN;
				parcelaDto.valor = vlrParcela;
				parcelaDto.criaDoc = abe3001.getAbe3001docFinan();
				listaParcelas.add(parcelaDto);
				
				i++;
			}
		}
		
		put("listaParcelas", listaParcelas);
	}
	
	/**
	 * Verifica o dia da semana e obtém os dias a serem adicionados a data
	 * @param data LocalDate
	 * @param abe30 Abe30
	 * @param qualData 0-Data Base 1-Vencimento Nominal
	 * @return int
	 */
	private int obterDiasAdicionaisAData(LocalDate data, Abe30 abe30, int qualData) {
		int diasAdicionaisData = 0;
		
		DayOfWeek diaSemana = data.getDayOfWeek();
		
		switch (diaSemana) {
			case DayOfWeek.SUNDAY:
				diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase1() : abe30.getAbe30diasVctoN1(); break;
			case DayOfWeek.MONDAY:
				diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase2() : abe30.getAbe30diasVctoN2(); break;
			case DayOfWeek.TUESDAY:
				diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase3() : abe30.getAbe30diasVctoN3(); break;
			case DayOfWeek.WEDNESDAY:
				diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase4() : abe30.getAbe30diasVctoN4(); break;
			case DayOfWeek.THURSDAY:
				diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase5() : abe30.getAbe30diasVctoN5(); break;
			case DayOfWeek.FRIDAY:
				diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase6() : abe30.getAbe30diasVctoN6(); break;
			case DayOfWeek.SATURDAY:
				diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase7() : abe30.getAbe30diasVctoN7(); break;
			default: break;
		}
	}
	
	private Abe30 buscarCondicaoPagamentoPorId(Long abe30id) {
		return getSession().createCriteria(Abe30.class)
				.addWhere(Criterions.eq("abe30id", abe30id))
				.get(ColumnType.ENTITY);
	}
	
	private List<Abe3001> buscarParcelasPeloIdCondicaoPagamento(Long abe30id) {
		return getSession().createCriteria(Abe3001.class)
				.addWhere(Criterions.eq("abe3001cp", abe30id))
				.setOrder("abe3001dias")
				.getList(ColumnType.ENTITY);
	}

	private List<Abe3002> buscarDiaComplementarPeloIdCondicaoPagamento(Long abe30id) {
		return getSession().createCriteria(Abe3002.class)
				.addWhere(Criterions.eq("abe3002cp", abe30id))
				.setOrder("abe3002id")
				.getList(ColumnType.ENTITY);
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CONDICAO_PAGAMENTO;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzEifQ==