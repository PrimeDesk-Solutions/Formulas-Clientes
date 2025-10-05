package multitec.baseDemo

import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.DecimalUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.dto.scf.SCF0221LctoExtratoDto
import sam.server.samdev.formula.FormulaBase

class SCF_LeituraExtrato extends FormulaBase {
	private MultipartFile arquivo;
	
		@Override
		public FormulaTipo obterTipoFormula() {
			return FormulaTipo.SCF_EXTRATO_DE_CONTA_CORRENTE;
		}
	
		@Override
		public void executar() {
			arquivo = get("arquivo");
			
			List<SCF0221LctoExtratoDto> listExtratoDto = new ArrayList();
			
			File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
			arquivo.transferTo(file);
			
			List<String> registros = FileUtils.readLines(file, "UTF-8");
			TextFileLeitura txt = new TextFileLeitura(registros);
			
			while(txt.nextLine()) {
				if(txt.getLinha().length() > 14 && txt.getLinha().substring(13, 14).trim().equalsIgnoreCase("E")) {
					SCF0221LctoExtratoDto extratoDto = new SCF0221LctoExtratoDto();
					extratoDto.data = DateUtils.parseDate(txt.getLinha().substring(142, 150).trim(), "ddMMyyyy");
					String valor = txt.getLinha().substring(150, 166).trim() + "." + txt.getLinha().substring(166, 168).trim();
					extratoDto.valor = DecimalUtils.create(valor).get();
					extratoDto.dc = txt.getLinha().substring(168, 169).trim();
					extratoDto.historico = txt.getLinha().substring(176, 201).trim();
					extratoDto.ni = StringUtils.extractNumbers(txt.getLinha().substring(214, 228).trim());
					extratoDto.dados1 = null;
					extratoDto.dados2 = null;
					
					if(extratoDto.valor > 0) listExtratoDto.add(extratoDto);
				}
			}
			
			put("lista", listExtratoDto);
		}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTEifQ==