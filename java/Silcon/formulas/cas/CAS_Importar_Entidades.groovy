/*
    OBSERVAÇÕES: Não esquecer de ajustar o valor de limite de crédito (Dividir por 1000)
 */

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
import sam.model.entities.ab.Abe01


public class CAS_Importar_Entidades extends FormulaBase{
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
                Abe01 abe01 = new Abe01();
                TableMap jsonAbe01 = new TableMap();

                abe01.setAbe01codigo(row.getCell(0).getStringCellValue());
                abe01.setAbe01codAux(row.getCell(1).getStringCellValue() != "null" ? row.getCell(1).getStringCellValue() : null);
                abe01.setAbe01na(row.getCell(2).getStringCellValue());
                abe01.setAbe01nome(row.getCell(3).getStringCellValue());
                abe01.setAbe01site(row.getCell(4).getStringCellValue().isEmpty() ? null : row.getCell(4).getStringCellValue().isEmpty() );
                abe01.setAbe01ti((int) row.getCell(5).getNumericCellValue());
                abe01.setAbe01ni(row.getCell(6).getStringCellValue());
                abe01.setAbe01ie(row.getCell(7).getStringCellValue());
                abe01.setAbe01im(row.getCell(8).getStringCellValue().isEmpty() ? null : row.getCell(8).getStringCellValue());
                abe01.setAbe01suframa(row.getCell(9).getStringCellValue().isEmpty() ? null : row.getCell(9).getStringCellValue() );
                abe01.setAbe01regTrib(row.getCell(10).getStringCellValue().isEmpty() ? null : row.getCell(10).getStringCellValue());
                abe01.setAbe01tipoSoc(row.getCell(11).getStringCellValue().isEmpty() ? null : row.getCell(11).getStringCellValue());
                abe01.setAbe01porte(row.getCell(12).getStringCellValue().isEmpty() ? null : row.getCell(12).getStringCellValue());
                abe01.setAbe01dtNasc(row.getCell(13).getLocalDateTimeCellValue().toLocalDate());
                abe01.setAbe01pis(row.getCell(14).getStringCellValue().isEmpty() ? null : row.getCell(14).getStringCellValue());
                abe01.setAbe01rg(row.getCell(15).getStringCellValue().isEmpty() ? null : row.getCell(15).getStringCellValue());
                abe01.setAbe01rgDtExped(row.getCell(16).getLocalDateTimeCellValue() != null ? row.getCell(16).getLocalDateTimeCellValue().toLocalDate() : null);
                abe01.setAbe01rgEe(row.getCell(17).getStringCellValue().isEmpty() ? null : row.getCell(17).getStringCellValue() );
                abe01.setAbe01rgOE(row.getCell(18).getStringCellValue().isEmpty() ? null : row.getCell(18).getStringCellValue());
                abe01.setAbe01obs(row.getCell(19).getStringCellValue());
                abe01.setAbe01cli((int) row.getCell(20).getNumericCellValue());
                abe01.setAbe01for((int) row.getCell(21).getNumericCellValue());
                abe01.setAbe01trans((int) row.getCell(22).getNumericCellValue());
                abe01.setAbe01rep((int) row.getCell(23).getNumericCellValue());
                Aac01 aac01 = getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01id", (int) row.getCell(24).getNumericCellValue())).get(ColumnType.ENTITY)
                abe01.setAbe01gc(aac01);
                abe01.setAbe01contribIcms((int) row.getCell(25).getNumericCellValue());
                abe01.setAbe01consOperCbk(100)
                abe01.setAbe01govTipo(0);
                abe01.setAbe01govRed(0.0);

                montarJsonEntidade(row, jsonAbe01);

                abe01.setAbe01json(jsonAbe01);

                getSession().persist(abe01);
            }
        }
    }

    private TableMap montarJsonEntidade(Row row,TableMap jsonAbe01){

        String txtVlrLimCredito = row.getCell(26).getNumericCellValue().toString().replace(",", '.');
        String txtDtVctoLimCredito = row.getCell(27).getLocalDateTimeCellValue() != null ? row.getCell(27).getLocalDateTimeCellValue().toLocalDate().toString().replace("-", "") : null;

        jsonAbe01.put("vlr_lim_credito", new BigDecimal(txtVlrLimCredito));
        if(txtDtVctoLimCredito) jsonAbe01.put("dt_vcto_lim_credito",txtDtVctoLimCredito)
        jsonAbe01.put("obs_lim_credito", row.getCell(28).getStringCellValue());
    }
}