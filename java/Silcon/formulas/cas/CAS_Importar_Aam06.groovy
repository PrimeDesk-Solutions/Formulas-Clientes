package Silcon.formulas.cas;

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.aa.Aam06

public class CAS_Importar_Aam06 extends FormulaBase{
    private XSSFSheet sheet
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        MultipartFile arquivo = get("arquivo");
        XSSFWorkbook workbook = new XSSFWorkbook(arquivo.getInputStream());
        sheet = workbook.getSheetAt(0);

        leituraePersistBanco()
    }
    private void leituraePersistBanco() {
        for (Row row : sheet) {
            if (row.getRowNum() >= 1) {
                Aam06 aam06 = new Aam06();

                aam06.setAam06codigo(row.getCell(0).getStringCellValue());
                aam06.setAam06descr(row.getCell(1).getStringCellValue());

                getSession().persist(aam06);
            }
        }
    }
}