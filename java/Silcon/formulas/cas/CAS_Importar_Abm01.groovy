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
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.ab.Abm01
import sam.model.entities.aa.Aam06

public class groovy extends FormulaBase{

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
    private void leituraePersistBanco(){
        for (Row row : sheet) {
            if (row.getRowNum() >= 1) {
                Abm01 abm01 = new Abm01();
                abm01.setAbm01tipo(Integer.parseInt(row.getCell(0).getStringCellValue()));
                abm01.setAbm01codigo(row.getCell(1).getStringCellValue());
                abm01.setAbm01gtin(row.getCell(2).getStringCellValue());
                abm01.setAbm01livre(row.getCell(3).getStringCellValue());
                abm01.setAbm01reduzido(Integer.parseInt(row.getCell(4).getStringCellValue()));
                abm01.setAbm01grupo(Integer.parseInt(row.getCell(5).getStringCellValue()));
                abm01.setAbm01na(row.getCell(6).getStringCellValue());
                abm01.setAbm01descr(row.getCell(7).getStringCellValue());
                if(!row.getCell(8).getStringCellValue().isEmpty()){
                    Aam06 aam06 = buscarUMUPeloCodigo(row.getCell(8).getStringCellValue())

                    if(aam06 == null) interromper("Unidade de medida " + row.getCell(8).getStringCellValue() + " não encontrada no sistema.");

                    abm01.setAbm01umu(aam06);
                }
                abm01.setAbm01arredUmu(Integer.parseInt(row.getCell(9).getStringCellValue()));
                abm01.setAbm01pesoBruto(new BigDecimal(row.getCell(10).getStringCellValue()));
                abm01.setAbm01pesoLiq(new BigDecimal(row.getCell(11).getStringCellValue()));
                Aac01 aac01 = buscarEmpresa()
                abm01.setAbm01gc(aac01);

                getSession().persist(abm01);
            }
        }
    }
    private Aam06 buscarUMUPeloCodigo(String umu){
        return getSession().createCriteria(Aam06.class).addWhere(Criterions.eq("aam06codigo", umu)).get(ColumnType.ENTITY);
    }
    private Aac01 buscarEmpresa(){
        return getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01id", 1075797 )).get()
    }
}