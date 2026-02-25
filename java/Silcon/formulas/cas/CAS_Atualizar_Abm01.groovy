package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac01
import sam.model.entities.ab.Abe02
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abe01
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac1001
import sam.model.entities.aa.Aac01
import sam.model.entities.ab.Abm01
import sam.server.samdev.utils.Parametro
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins

public class CAS_Atualizar_Abm01 extends FormulaBase {
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
                Integer tipoItem = Integer.parseInt(row.getCell(0).getStringCellValue());
                String codItem = row.getCell(1).getStringCellValue();
                String naItem = row.getCell(2).getStringCellValue();
                String descrItem = row.getCell(3).getStringCellValue();

                Abm01 abm01 = getSession().createCriteria(Abm01.class).addWhere(Criterions.where("abm01codigo = '" + codItem +"' AND abm01tipo = " + tipoItem)).get(ColumnType.ENTITY);
                if(abm01 == null) interromper("Item " + tipoItem + " - " + codItem +" não encontrado no sistema.");

                abm01.setAbm01codigo(codItem);
                abm01.setAbm01tipo(tipoItem);
                abm01.setAbm01na(naItem);
                abm01.setAbm01descr(descrItem);

                getSession().persist(abm01);

            }
        }
    }
}