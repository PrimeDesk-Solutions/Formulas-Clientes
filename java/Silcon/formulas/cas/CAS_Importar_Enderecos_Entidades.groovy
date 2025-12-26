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
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aag0201;
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101


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
        Integer linha = 0;
        for (Row row : sheet) {
            if (row.getRowNum() >= 1) {
                linha++
                Abe0101 abe0101 = new Abe0101();
                Abe01 abe01 = buscarEntidadePeloCodigo(row.getCell(0).getStringCellValue());
                if(abe01 == null) interromper("Entidade com o código " + row.getCell(0).getStringCellValue() + " não localizada no sistema. Linha: " + (linha + 1).toString());

                abe0101.setAbe0101ent(abe01);
                abe0101.setAbe0101local(row.getCell(1).getStringCellValue().isEmpty() ? null : row.getCell(1).getStringCellValue());
                abe0101.setAbe0101tpLog(row.getCell(2).getStringCellValue().isEmpty() ? null : row.getCell(2).getStringCellValue());
                abe0101.setAbe0101endereco(row.getCell(3).getStringCellValue().isEmpty() ? null : row.getCell(3).getStringCellValue());
                abe0101.setAbe0101numero(row.getCell(4).getStringCellValue().isEmpty() ? null : row.getCell(4).getStringCellValue());
                abe0101.setAbe0101bairro(row.getCell(5).getStringCellValue().isEmpty() ? null : row.getCell(5).getStringCellValue());
                abe0101.setAbe0101complem(row.getCell(6).getStringCellValue().isEmpty() ? null : row.getCell(6).getStringCellValue());
                abe0101.setAbe0101cep(row.getCell(7).getStringCellValue().isEmpty() ? null : row.getCell(7).getStringCellValue());
                abe0101.setAbe0101cp((int) row.getCell(8).getNumericCellValue());
                abe0101.setAbe0101cepCp(row.getCell(9).getStringCellValue().isEmpty() ? null : row.getCell(9).getStringCellValue());

                if(!row.getCell(10).getStringCellValue().isEmpty()){
                    Aag0201 aag0201 = buscarIDMunicipio(row.getCell(10).getStringCellValue());

                    abe0101.setAbe0101municipio(aag0201);
                }

                abe0101.setAbe0101regiao(row.getCell(12).getStringCellValue().isEmpty() ? null : row.getCell(12).getStringCellValue());
                abe0101.setAbe0101ddd1(row.getCell(13).getStringCellValue().isEmpty() ? null : row.getCell(13).getStringCellValue()); // Ajustar para string
                abe0101.setAbe0101fone1(row.getCell(14).getStringCellValue().isEmpty() ? null : row.getCell(14).getStringCellValue());
                abe0101.setAbe0101ddd2(row.getCell(15).getStringCellValue().isEmpty() ? null : row.getCell(15).getStringCellValue());
                abe0101.setAbe0101fone2(row.getCell(16).getStringCellValue().isEmpty() ? null : row.getCell(16).getStringCellValue());
                String email = row.getCell(17).getStringCellValue().isEmpty() ? null : row.getCell(17).getStringCellValue();
                email = email && email.length() > 60 ? email.substring(0,60) : email;
                abe0101.setAbe0101eMail(email);
                abe0101.setAbe0101rs(row.getCell(18).getStringCellValue().isEmpty() ? null : row.getCell(18).getStringCellValue());
                abe0101.setAbe0101ti(0);
                abe0101.setAbe0101ni(row.getCell(20).getStringCellValue().isEmpty() ? null : row.getCell(20).getStringCellValue());
                abe0101.setAbe0101ie(row.getCell(21).getStringCellValue().isEmpty() ? null : row.getCell(21).getStringCellValue());
                abe0101.setAbe0101obs(row.getCell(22).getStringCellValue().isEmpty() ? null : row.getCell(22).getStringCellValue());
                abe0101.setAbe0101principal((int) row.getCell(23).getNumericCellValue());
                abe0101.setAbe0101entrega((int) row.getCell(24).getNumericCellValue());
                abe0101.setAbe0101cobranca((int) row.getCell(25).getNumericCellValue());
                abe0101.setAbe0101outros((int) row.getCell(26).getNumericCellValue());

                try{
                    getSession().persist(abe0101);
                }catch(Exception e){
                    interromper("Falha ao importar registro da linha: " + (linha + 1).toString())
                }
            }
        }
    }

    private Abe01 buscarEntidadePeloCodigo(String codEntidade){

        return getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", codEntidade)).get(ColumnType.ENTITY);
    }

    private Aag0201 buscarIDMunicipio(String idMunicipio){
        Aag0201 aag0201 = getSession().createCriteria(Aag0201.class).addWhere(Criterions.eq("aag0201id", Long.parseLong(idMunicipio))).get(ColumnType.ENTITY);

        if(aag0201 == null) interromper("Municipio com o ID " + idMunicipio + " não localizado no sistema.");

        return aag0201;
    }

}