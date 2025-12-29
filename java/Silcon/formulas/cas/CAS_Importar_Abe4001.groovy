
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
import sam.model.entities.aa.Aam06
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abe4001
import sam.model.entities.ab.Abm01


public class CAS_Importar_Abe4001 extends FormulaBase{
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
                Abe4001 abe4001 = new Abe4001();
                String codTabela = row.getCell(0).getStringCellValue();
                Integer tipoItem = Integer.parseInt(row.getCell(1).getStringCellValue()) == 2 ? 3 : Integer.parseInt(row.getCell(1).getStringCellValue());
                String codItem = row.getCell(2).getStringCellValue();
                Long condPgto = Long.parseLong(row.getCell(3).getStringCellValue());
                BigDecimal txDesconto = new BigDecimal(row.getCell(4).getStringCellValue());
                BigDecimal qtdMax = new BigDecimal(row.getCell(5).getStringCellValue());
                BigDecimal preco = new BigDecimal(row.getCell(6).getStringCellValue());

                Abe40 abe40 = getSession().createCriteria(Abe40.class).addWhere(Criterions.where("abe40codigo = '" + codTabela + "' AND abe40gc = 1075797 " )).get();
                if(abe40 == null) interromper("Tabela de preço não encontrada no sistema. Linha: " + (row.getRowNum() + 1).toString());

                Abm01 abm01 = getSession().createCriteria(Abm01.class).addWhere(Criterions.where("abm01tipo = " + tipoItem + " AND abm01codigo = '" + codItem + "' AND abm01gc = 1075797")).get();
                if(abm01 == null) interromper("O item " + tipoItem + " - " + codItem + " não foi encontrado no sistema. Linha: " + (row.getRowNum() + 1).toString())

                Abe30 abe30 = getSession().createCriteria(Abe30.class).addWhere(Criterions.eq("abe30id", condPgto)).get();
                if(abe30 == null) interromper("Condição de pagamento não encontrada no sistema. Linha: " + (row.getRowNum() + 1).toString());


                abe4001.setAbe4001tab(abe40);
                abe4001.setAbe4001item(abm01);
                abe4001.setAbe4001cp(abe30);
                abe4001.setAbe4001txDesc(txDesconto);
                abe4001.setAbe4001qtMax(qtdMax);
                abe4001.setAbe4001preco(preco);

                getSession().persist(abe4001);

            }
        }
    }
}