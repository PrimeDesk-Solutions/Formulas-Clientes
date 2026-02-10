package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions;
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile
import sam.dicdados.FormulaTipo
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abe01
import sam.server.samdev.utils.Parametro

public class CAS_Atualizar_Mensagens_Registros extends FormulaBase{
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
                String nomeTarefa = row.getCell(0).getStringCellValue();
                String idRegistro = row.getCell(1).getStringCellValue();
                String observacao = row.getCell(2).getStringCellValue();

                if(nomeTarefa.toUpperCase() == "SCF"){
                    Long idDoc = buscarIDDocSAM4(idRegistro);

                    if(idDoc == null) interromper("Não foi encontrado documento com o ID do SAM 3: " + idRegistro);

                    Daa01 daa01 = getSession().createCriteria(Daa01.class).addWhere(Criterions.eq("daa01id", idDoc)).get(ColumnType.ENTITY);
                    daa01.setDaa01obs(observacao);

                    getSession().persist(daa01);
                }else{
                    Abe01 abe01 = getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", idRegistro)).get(ColumnType.ENTITY)
                    if(abe01 == null) interromper("Não foi encontrada entidade com o código " + idRegistro);

                    abe01.setAbe01obs(observacao);

                    getSession().persist(abe01);
                }
            }
        }
    }
    private Long buscarIDDocSAM4(String idRegistro){
        String sql = "SELECT daa01id FROM daa01 WHERE CAST(daa01json ->> 'id_documento' AS BIGINT) = :idRegistro";

        return getAcessoAoBanco().obterLong(sql, Parametro.criar("idRegistro", Long.parseLong(idRegistro)));
    }
}