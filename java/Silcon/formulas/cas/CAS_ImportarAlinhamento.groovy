package Atilatte.formulas.cas

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
import sam.model.entities.ab.Aba60
import sam.model.entities.ab.Aba6001
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abe4001
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcb10
import sam.model.entities.bc.Bcb11
import sam.server.samdev.formula.FormulaBase

class CAS_ImportarAlinhamento extends FormulaBase {

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

        validacoes()
        leituraExcelePersistNoBanco()
    }

    private void validacoes() {
        if (json == null) {
            interromper("Necessário informar uma especificação!")
        } else if (json.size() == 0) {
            interromper("Necessário informar um código de alinhamento!")
        } else {
            aba60 = getSession().createCriteria(Aba60.class)
                    .addWhere(getSamWhere().getCritPadrao(Aba60.class))
                    .addWhere(Criterions.eq("aba60codigo", json.getString("cod_alinhamento")))
                    .get(ColumnType.ENTITY)

            if (aba60 == null) {
                interromper("Código do alinhamento " + json.getString("cod_alinhamento") + ", não foi encontrado na banco de dados!")
            }
        }
    }

    private void leituraExcelePersistNoBanco() {
        for (Row row : sheet) {
            if (row.getRowNum() >= 1) {
                if (row.getCell(0) != null && !row.getCell(0).toString().isEmpty()) {

                    // Busca o registro do campo livre
                    Aah02 aah02 = getSession().createCriteria(Aah02.class)
                            .addWhere(Criterions.eq("aah02nome", row.getCell(3).getStringCellValue()))
                            .get(ColumnType.ENTITY)

                    // Busca o registro de campo do alinhamento
                    Aba6001 aba6001existe = getSession().createCriteria(Aba6001.class)
                            .addJoin(Joins.join("aba60", "aba60id = aba6001alin"))
                            .addWhere(Criterions.eq("aba6001alin", aba60.getAba60id()))
                            .addWhere(Criterions.eq("aba6001reg", row.getCell(0).getStringCellValue()))
                            .setMaxResults(1)
                            .get(ColumnType.ENTITY)

                    if (!aah02) interromper("Linha " + row.getRowNum().plus(1) + " - Campo livre informado não consta no banco de dados!")

                    // Se o registro não existir no alinhamento, faz o persist
                    if (!aba6001existe) {
                        Aba6001 aba6001 = new Aba6001()
                        aba6001.setAba6001alin(aba60)
                        aba6001.setAba6001reg(row.getCell(0).getStringCellValue())
                        aba6001.setAba6001campo(row.getCell(1).getStringCellValue())
                        aba6001.setAba6001descr(row.getCell(2).getStringCellValue())
                        aba6001.setAba6001cpoVlr(aah02)
                        getSession().persist(aba6001)
                    }
                }
            }
        }
    }
}
