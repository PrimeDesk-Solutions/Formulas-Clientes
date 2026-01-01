package multitec.baseDemo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.multiorm.criteria.criterion.Criterion;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.criteria.client.ClientCriterion;
import sam.core.criteria.ClientCriteriaConvert;
import sam.core.variaveis.MDate;
import sam.core.variaveis.Variaveis;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.fc.Fca10;
import sam.server.samdev.formula.FormulaBase;

public class SCA_GerarAFDTOld extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCA_GERAR_ARQUIVOS;
	}

	@Override
	public void executar() {
		TextFile txt = new TextFile();
		List<TableMap> fca1001s = get("fca1001s");
		LocalDate dtInicial = getLocalDate("dataInicial");
		LocalDate dtFinal = getLocalDate("dataFinal");
		
		if (Utils.isEmpty(fca1001s)) interromper("Nenhuma marcação de ponto foi encontrada no período selecionado.");

		for (TableMap tm : fca1001s) {
			if (!Utils.jsBoolean(tm.get("fca10consistente"))) interromper("Existem pontos inconsistentes no período selecionado.");
		}
		
		DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("ddMMyyyy");
		DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("HHmm");

		Aac10 aac10 = Variaveis.obter().getAac10();
		int sequencial = 1;

		//TIPO 1 - CABEÇALHO
		txt.print(sequencial, 9, '0', true);																												//001 a 009
		txt.print("1", 1);																																	//010 a 010
		txt.print(aac10.getAac10ti() == 0 ? "1" : "2", 1);																									//011 a 011
		txt.print(StringUtils.extractNumbers(aac10.getAac10ni()), 14, '0', true);																			//012 a 025
		txt.print(StringUtils.extractNumbers(/**aac10.getAac10cei()**/""), 12, '0', true);																	//026 a 037 TODO
		txt.print(aac10.getAac10rs(), 150);																													//038 a 187
		txt.print(dtInicial.format(dtfData), 8, '0', true);																									//188 a 195
		txt.print(dtFinal.format(dtfData), 8, '0', true);																									//196 a 203
		txt.print(MDate.date().format(dtfData), 8, '0', true);																							//204 a 211
		txt.print(MDate.time().format(dtfHora), 4, '0', true);																							//212 a 215
		txt.newLine();
		sequencial++;

		int seqJornada = 1;
		Long fca10id = null;
		for(TableMap tm : fca1001s) {
			String abh80pis = tm.get("abh80pis") != null ? StringUtils.extractNumbers(tm.get("abh80pis")) : "";
			String fca1001numFabRep = tm.get("fca1001numFabRep") != null ? StringUtils.extractNumbers(tm.get("fca1001numFabRep")) : "";
			String fca1001justificativa = tm.get("fca1001justificativa") != null ? tm.get("fca1001justificativa") : "";
			int fca1001classificacao = tm.get("fca1001classificacao");
			if(fca10id != null && !fca10id.equals(tm.get("fca10id"))) seqJornada = 1;
			
			//TIPO 2 - DETALHE
			txt.print(sequencial, 9, '0', true);																											//001 a 009
			txt.print("2", 1);																																//010 a 010
			txt.print(((LocalDate) tm.get("fca1001data")).format(dtfData), 8, '0', true);																	//011 a 018
			txt.print(((LocalTime) tm.get("fca1001hrBase")).format(dtfHora), 4, '0', true);																	//019 a 022
			txt.print(abh80pis.length() > 12 ? abh80pis.substring(0, 12) : abh80pis, 12, '0', true);														//023 a 034
			txt.print(fca1001numFabRep.length() > 17 ? fca1001numFabRep.substring(0, 17) : fca1001numFabRep, 17, '0', true);								//035 a 051
			txt.print(fca1001classificacao == 0 ? "E" : fca1001classificacao == 1 ? "S" : "D", 1);															//052 a 052
			txt.print(fca1001classificacao == 3 ? "00" : seqJornada, 2, '0', true);																			//053 a 054
			txt.print(Utils.jsBoolean(tm.get("fca1001pa")) ? "P" : tm.get("fca1001hrRep") != null ? "O" : "I", 1);											//055 a 055
			txt.print(fca1001justificativa, 100);																											//056 a 155
			txt.newLine();
			sequencial++;

			fca10id = tm.get("fca10id");
			if(fca1001classificacao == 1) seqJornada++;
		}

		//TIPO 9 - TRAILLER
		txt.print(sequencial, 9, '0', true);																												//001 a 009
		txt.print("9", 1);
		
		values.put("txt", txt);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTkifQ==