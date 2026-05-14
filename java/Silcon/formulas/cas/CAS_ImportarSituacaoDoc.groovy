package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.collections.TableMap
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aah02
import sam.model.entities.aa.Aaj03
import sam.model.entities.ab.Aba60
import sam.model.entities.ab.Aba6001
import sam.server.samdev.formula.FormulaBase

class CAS_ImportarSituacaoDoc extends FormulaBase {

    private Aba60 aba60
    private TableMap json
    private XSSFSheet sheet

    @Override
    FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS
    }

    @Override
    void executar() {
        json = get("json");
        MultipartFile arquivo = get("arquivo");
        XSSFWorkbook workbook = new XSSFWorkbook(arquivo.getInputStream());
        sheet = workbook.getSheetAt(0)

        leituraExcelePersistNoBanco()
    }

    private void leituraExcelePersistNoBanco() {
        for (Row row : sheet) {
            if (row.getRowNum() >= 1) {
                // Se não informado o código da situação do doc, ignora a linha do excel
                if (row.getCell(0) != null && !row.getCell(0).toString().isEmpty()) {

                    // Busca o a situação de doc no banco de acordo com o código informado no excel
                    Aaj03 aaj03 = getSession().createCriteria(Aaj03.class)
                            .addWhere(Criterions.eq("aaj03codigo", row.getCell(0).getStringCellValue()))
                            .get(ColumnType.ENTITY)

                    // Se o registro não existir no banco, faz o persist
                    if (!aaj03) {
                        aaj03 = new Aaj03()
                        aaj03.setAaj03codigo(row.getCell(0).getStringCellValue())
                        aaj03.setAaj03descr(row.getCell(1).getStringCellValue())
                        aaj03.setAaj03nfe(row.getCell(2).getStringCellValue())
                        aaj03.setAaj03tipoNfDeb(row.getCell(3).getStringCellValue())
                        aaj03.setAaj03tipoNfCred(row.getCell(4).getStringCellValue())
                        aaj03.setAaj03sintegra(row.getCell(5).getStringCellValue())
                        aaj03.setAaj03efd(row.getCell(6).getStringCellValue())
                        getSession().persist(aaj03)
                    }
                }
            }
        }
    }
}