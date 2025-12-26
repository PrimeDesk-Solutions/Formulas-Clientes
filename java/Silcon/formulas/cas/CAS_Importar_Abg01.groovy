
package Silcon.formulas.cas;
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.collections.TableMap
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac01
import sam.model.entities.aa.Aac10
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.ab.Abg01
import sam.model.entities.aa.Aam06

public class CAS_Importar_Abg01 extends FormulaBase{
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
                Aac01 aac01 = buscarGCPeloCodigo(row.getCell(8).getStringCellValue())
                Abg01 abg01 = new Abg01();
                abg01.setAbg01codigo(row.getCell(0).getStringCellValue());
                abg01.setAbg01descr(row.getCell(1).getStringCellValue());
                abg01.setAbg01vatFedNac(new BigDecimal(row.getCell(2).getStringCellValue()));
                abg01.setAbg01vatFedImp(new BigDecimal(row.getCell(3).getStringCellValue()));
                abg01.setAbg01vatEst(new BigDecimal(row.getCell(4).getStringCellValue()));
                abg01.setAbg01vatMun(new BigDecimal(row.getCell(5).getStringCellValue()));
                abg01.setAbg01redCbsIbs(new BigDecimal(row.getCell(6).getStringCellValue()));
                abg01.setAbg01txIS(new BigDecimal(row.getCell(7).getStringCellValue()));
                abg01.setAbg01gc(aac01);

                getSession().persist(abg01);
            }
        }
    }
    private Aac01 buscarGCPeloCodigo(String codGC){
        return getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01codigo", codGC)).get();
    }
}