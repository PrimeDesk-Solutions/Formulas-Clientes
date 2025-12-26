
package Silcon.formulas.cas

import sam.model.entities.ab.Abb11;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
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
import sam.model.entities.aa.Aac10
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abf10
import sam.model.entities.ab.Abg01

public class CAS_Importar_Abm0101 extends FormulaBase{
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
                for(int i = 0; i <= 3; i++){ // Cria o mesmo registro em todas as empresass
                    String codEmpresa = "00" + i;
                    Aac10 aac10 = buscarEmpresaPeloCodigo(codEmpresa);
                    Abm0101 abm0101 = new Abm0101();
                    Abm01 abm01 = buscarItemPeloCodigo(row.getCell(0).getStringCellValue());
                    if(abm01 == null) interromper("Item " + row.getCell(0).getStringCellValue() + " não encontrado no sistema.");
                    abm0101.setAbm0101item(abm01);
                    abm0101.setAbm0101empresa(aac10);
                    abm0101.setAbm0101unidDiv(Integer.parseInt(row.getCell(1).getStringCellValue()));
                    abm0101.setAbm0101montagem(Integer.parseInt(row.getCell(2).getStringCellValue()));
                    abm0101.setAbm0101sgpPrev(Integer.parseInt(row.getCell(3).getStringCellValue()));
                    abm0101.setAbm0101sgpCpra(Integer.parseInt(row.getCell(4).getStringCellValue()));
                    if(!row.getCell(5).getStringCellValue().isEmpty()){
                        Aam06 aam06 = buscarUnidadeMedidaPeloCodigo(row.getCell(5).getStringCellValue())

                        if(aam06 == null) interromper("Unidade de medida " + row.getCell(5).getStringCellValue() + " não encontrada no sistema.");

                        abm0101.setAbm0101umt(aam06);
                    }

                    if(!row.getCell(6).getStringCellValue().isEmpty()){
                        Abg01 abg01 = buscarNCMPeloCodigo(row.getCell(6).getStringCellValue());

                        if(abg01 == null) interromper("NCM " + row.getCell(6).getStringCellValue() + " não encontrado no sistema.");

                        abm0101.setAbm0101ncm(abg01);
                    }

                    abm0101.setAbm0101calcNiveis(Integer.parseInt(row.getCell(7).getStringCellValue()));
                    abm0101.setAbm0101itemTerc(Integer.parseInt(row.getCell(8).getStringCellValue()));
                    abm0101.setAbm0101wmsMassa(new BigDecimal(row.getCell(9).getStringCellValue()));

                    if(!row.getCell(10).getStringCellValue().isEmpty()){
                        Abb11 abb11 = buscarDepartamentoPeloCodigo(row.getCell(10).getStringCellValue());

                        if(abb11 == null) interromper("Departamento " + row.getCell(10).getStringCellValue() + " não encontrado no sistema.");

                        abm0101.setAbm0101eDepto(abb11);
                    }

                    if(!row.getCell(11).getStringCellValue().isEmpty()){
                        Abf10 abf10 = buscarNaturezaPeloCodigo(row.getCell(11).getStringCellValue());

                        if(abf10 == null) interromper("Natureza " + row.getCell(11).getStringCellValue() + " não encontrado no sistema.");

                        abm0101.setAbm0101eNat(abf10);
                    }

                    if(!row.getCell(12).getStringCellValue().isEmpty()){
                        Abb11 abb11 = buscarDepartamentoPeloCodigo(row.getCell(12).getStringCellValue());

                        if(abb11 == null) interromper("Departamento " + row.getCell(12).getStringCellValue() + " não encontrado no sistema.");

                        abm0101.setAbm0101sDepto(abb11);
                    }

                    if(!row.getCell(13).getStringCellValue().isEmpty()){
                        Abf10 abf10 = buscarNaturezaPeloCodigo(row.getCell(13).getStringCellValue());

                        if(abf10 == null) interromper("Natureza " + row.getCell(13).getStringCellValue() + " não encontrado no sistema.");

                        abm0101.setAbm0101sNat(abf10);
                    }

                    getSession().persist(abm0101);
                }
            }
        }
    }
    private Aac10 buscarEmpresaPeloCodigo(String codEmpresa){
        return getSession().createCriteria(Aac10.class).addWhere(Criterions.eq("aac10codigo", codEmpresa)).get();
    }
    private Abm01 buscarItemPeloCodigo(String codItem){
        return getSession().createCriteria(Abm01.class).addWhere(Criterions.eq("abm01codigo", codItem)).get();
    }
    private Aam06 buscarUnidadeMedidaPeloCodigo(String umu){
        return getSession().createCriteria(Aam06.class).addWhere(Criterions.eq("aam06codigo", umu)).get(ColumnType.ENTITY);
    }
    private Abg01 buscarNCMPeloCodigo(String codNcm){
        return getSession().createCriteria(Abg01.class).addWhere(Criterions.eq("abg01codigo", codNcm)).get()
    }
    private Abb11 buscarDepartamentoPeloCodigo(String codDepto){
        return getSession().createCriteria(Abb11.class).addWhere(Criterions.eq("abb11codigo", codDepto)).get();
    }
    private Abf10 buscarNaturezaPeloCodigo(String codNatureza){
        return getSession().createCriteria(Abf10.class).addWhere(Criterions.eq("abf10codigo", codNatureza)).get()
    }

}