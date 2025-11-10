package multitec.baseDemo

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.TextFileLeitura
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase

class SCF_Conciliacao extends FormulaBase {
	private MultipartFile arquivo;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_SERVICO_DE_PROTECAO_AO_CREDITO;
	}

	@Override
	public void executar() {
		arquivo = get("arquivo");
		
		TextFile txtNovo = new TextFile();
		
		File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
		arquivo.transferTo(file);
		
		List<String> registros = FileUtils.readLines(file, "UTF-8");
		TextFileLeitura txt = new TextFileLeitura(registros);
		
		while(txt.nextLine()) {
			LocalDate data = MDate.date();
			if(txt.getLinha().substring(36, 44).trim().equalsIgnoreCase("CONCILIA")) {
				DateUtils.parseDate(txt.getLinha().substring(44, 52).trim(), "yyyyMMdd");
			}
			
			if(txt.getLinha().substring(0, 2).equals("01") && txt.getLinha().substring(16, 18).equals("05")){
				//Procura o ID do documento na posição 66 a 99, caso não seja encontrado procura na posição 19 a 28
				String id = txt.getLinha().substring(65, 99).trim();
				int pos = id.indexOf(";", -1);
				if(pos < 0) {
					id = txt.getLinha().substring(18, 28).trim();
					pos = id.indexOf(";", -1);
				}
				
				if(pos >= 0) { //Se pos == -1 significa que a linha não é correspondentes aos dados do documento
					
					txtNovo.print(txt.getLinha().substring(0, 57)); //Primeira parte da linha, ou seja, até o início da data de pagamento
					
					Long daa01id = Long.parseLong(id.substring(pos+1, id.length())); //Id do documento - Daa01
					Daa01 daa01 = getAcessoAoBanco().buscarRegistroUnicoById("Daa01", daa01id);
					String dtPgto = null;

					if(daa01 != null) {
						if(daa01.daa01dtPgto != null && daa01.daa01dtPgto.compareTo(data) <= 0) {
							dtPgto = daa01.daa01dtPgto.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
						}
					}else {
						dtPgto = data.format(DateTimeFormatter.ofPattern("yyyyMMdd")); //Se o documento não for encontrado no sistema seta a data do arquivo pois significa que o documento foi excluído do sistema
					}
					
					txtNovo.print(dtPgto != null ? dtPgto : StringUtils.space(8));
					txtNovo.print(txt.getLinha().substring(65)); //Segunda parte da linha, ou seja, após a data de pagamento
					
				}else {
					txtNovo.print(txt.getLinha());
				}
			}else {
				txtNovo.print(txt.getLinha());
			}
			
			txtNovo.newLine();
		}
		
		put("txt", txtNovo);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDcifQ==