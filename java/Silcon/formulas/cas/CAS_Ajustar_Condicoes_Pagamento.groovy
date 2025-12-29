package Silcon.formulas.cas.CAS_Ajustar_Condicoes_Pagamento

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
import sam.model.entities.ab.Abe30
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
    private void leituraePersistBanco() {
        for (Row row : sheet) {
            if (row.getRowNum() >= 1) {
                TableMap jsonAbe30 = new TableMap();
                Long idCondicao = Long.parseLong(row.getCell(0).getStringCellValue());
                BigDecimal txDesconto = new BigDecimal(row.getCell(2).getStringCellValue());
                Integer diasCalculoDataLimite = Integer.parseInt(row.getCell(3).getStringCellValue());
                BigDecimal txMulta = new BigDecimal(row.getCell(4).getStringCellValue());
                BigDecimal txJuros = new BigDecimal(row.getCell(5).getStringCellValue());

                jsonAbe30.put("taxa_multa_atraso", txMulta);
                jsonAbe30.put("taxa_juros_diario", txJuros);
                jsonAbe30.put("desconto_cond_taxa", txDesconto);
                jsonAbe30.put("dias_para_calculo", diasCalculoDataLimite);

                Abe30 abe30 = getSession().createCriteria(Abe30.class).addWhere(Criterions.eq("abe30id", idCondicao)).get();
                if(abe30 == null) interromper("Condição de pagamento não encontrada no sistema. Linha: " + (row.getRowNum() + 1).toString())

                abe30.setAbe30json(jsonAbe30);
                getSession().persist(abe30);
            }
        }
    }
}