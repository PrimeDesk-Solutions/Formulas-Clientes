package multitec.baseDemo

import java.time.LocalDate
import java.time.ZoneId

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aap10
import sam.model.entities.aa.Aap11
import sam.model.entities.aa.Aap12
import sam.model.entities.aa.Aap13
import sam.server.samdev.formula.FormulaBase

class CAS_ImportarDadosDemo extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CAS_IMPORTAR_DADOS;
	}

	@Override
	public void executar() {
		TableMap json = get("json");
		MultipartFile arquivo = get("arquivo");
		
		/** Exemplo 1 **/
		/*XSSFWorkbook workbook = new XSSFWorkbook(arquivo.getInputStream());
		for (int index = 0; index < workbook.size(); index++) {
			XSSFSheet worksheet = workbook.getSheetAt(index);
			String nomeTabela = StringUtils.capitalize(worksheet.getSheetName(), true);
			
			for (Iterator<Row> iterator = worksheet.iterator(); iterator.hasNext();) {
				Row row = iterator.next();
				if (row.getRowNum() == 0) continue;
				
				XSSFRow xssfRow = worksheet.getRow(row.getRowNum());
				if(xssfRow.getCell(0).getRawValue() == null || xssfRow.getCell(0).getRawValue().length() == 0) continue;
				String codigo = String.valueOf(xssfRow.getCell(0).getRawValue());
				String descricao = xssfRow.getCell(1).getStringCellValue();
				Date date = xssfRow.getCell(2) != null ? xssfRow.getCell(2).getDateCellValue() : null;
				LocalDate dataInativacao = date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
				
				if (json != null) {
					String texto = json.getString("texto")
					descricao = descricao + " " + texto;
				}

				String nomeClass = "sam.model.entities." + nomeTabela.toLowerCase().substring(0, 2) + "." + nomeTabela;
				Class<MultiEntity> classEntity = Class.forName(nomeClass);
				def entityInstance = classEntity.newInstance()
				entityInstance.invokeMethod("set"+nomeTabela+"codigo", codigo)
				entityInstance.invokeMethod("set"+nomeTabela+"descr", descricao)
				if (dataInativacao != null) entityInstance.invokeMethod("set"+nomeTabela+"di", dataInativacao)
				getSamWhere().setDefaultValues(entityInstance)
				getSession().persist(entityInstance)
			}
		}*/
		
		/** Exemplo 2 **/
		XSSFWorkbook workbook = new XSSFWorkbook(arquivo.getInputStream());
		XSSFSheet worksheet = workbook.getSheet("Aap10")
		for (Iterator<Row> iterator = worksheet.iterator(); iterator.hasNext();) {
			Row row = iterator.next();
			if (row.getRowNum() == 0) continue;
			
			XSSFRow xssfRow = worksheet.getRow(row.getRowNum());
			if(xssfRow.getCell(0).getRawValue() == null || xssfRow.getCell(0).getRawValue().length() == 0) continue;
			String codigo = String.valueOf(xssfRow.getCell(0).getRawValue());
			String descricao = xssfRow.getCell(1).getStringCellValue();
			Date date = xssfRow.getCell(2) != null ? xssfRow.getCell(2).getDateCellValue() : null;
			LocalDate dataInativacao = date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
			
			if (json != null) {
				String texto = json.getString("texto")
				descricao = descricao + " " + texto;
			}

			Aap10 aap10 = new Aap10()
			aap10.aap10codigo = codigo;
			aap10.aap10descr = descricao;
			aap10.aap10di = dataInativacao;
			getSamWhere().setDefaultValues(aap10)
			getSession().persist(aap10)
		}
		
		worksheet = workbook.getSheet("Aap11")
		for (Iterator<Row> iterator = worksheet.iterator(); iterator.hasNext();) {
			Row row = iterator.next();
			if (row.getRowNum() == 0) continue;
			
			XSSFRow xssfRow = worksheet.getRow(row.getRowNum());
			if(xssfRow.getCell(0).getRawValue() == null || xssfRow.getCell(0).getRawValue().length() == 0) continue;
			String codigo = String.valueOf(xssfRow.getCell(0).getRawValue());
			String descricao = xssfRow.getCell(1).getStringCellValue();
			Date date = xssfRow.getCell(2) != null ? xssfRow.getCell(2).getDateCellValue() : null;
			LocalDate dataInativacao = date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
			
			if (json != null) {
				String texto = json.getString("texto")
				descricao = descricao + " " + texto;
			}

			Aap11 aap11 = new Aap11()
			aap11.aap11codigo = codigo;
			aap11.aap11descr = descricao;
			aap11.aap11di = dataInativacao;
			getSamWhere().setDefaultValues(aap11)
			getSession().persist(aap11)
		}
		
		worksheet = workbook.getSheet("Aap12")
		for (Iterator<Row> iterator = worksheet.iterator(); iterator.hasNext();) {
			Row row = iterator.next();
			if (row.getRowNum() == 0) continue;
			
			XSSFRow xssfRow = worksheet.getRow(row.getRowNum());
			if(xssfRow.getCell(0).getRawValue() == null || xssfRow.getCell(0).getRawValue().length() == 0) continue;
			String codigo = String.valueOf(xssfRow.getCell(0).getRawValue());
			String descricao = xssfRow.getCell(1).getStringCellValue();
			Date date = xssfRow.getCell(2) != null ? xssfRow.getCell(2).getDateCellValue() : null;
			LocalDate dataInativacao = date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
			
			if (json != null) {
				String texto = json.getString("texto")
				descricao = descricao + " " + texto;
			}

			Aap12 aap12 = new Aap12()
			aap12.aap12codigo = codigo;
			aap12.aap12descr = descricao;
			aap12.aap12di = dataInativacao;
			getSamWhere().setDefaultValues(aap12)
			getSession().persist(aap12)
		}
		
		worksheet = workbook.getSheet("Aap13")
		for (Iterator<Row> iterator = worksheet.iterator(); iterator.hasNext();) {
			Row row = iterator.next();
			if (row.getRowNum() == 0) continue;
			
			XSSFRow xssfRow = worksheet.getRow(row.getRowNum());
			if(xssfRow.getCell(0).getRawValue() == null || xssfRow.getCell(0).getRawValue().length() == 0) continue;
			String codigo = String.valueOf(xssfRow.getCell(0).getRawValue());
			String descricao = xssfRow.getCell(1).getStringCellValue();
			Date date = xssfRow.getCell(2) != null ? xssfRow.getCell(2).getDateCellValue() : null;
			LocalDate dataInativacao = date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
			
			if (json != null) {
				String texto = json.getString("texto")
				descricao = descricao + " " + texto;
			}

			Aap13 aap13 = new Aap13()
			aap13.aap13codigo = codigo;
			aap13.aap13descr = descricao;
			aap13.aap13di = dataInativacao;
			getSamWhere().setDefaultValues(aap13)
			getSession().persist(aap13)
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=