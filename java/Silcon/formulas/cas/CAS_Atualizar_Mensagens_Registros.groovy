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
import sam.server.samdev.utils.Parametro
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins

public class CAS_Atualizar_Mensagens_Registros extends FormulaBase {
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

                if (nomeTarefa.toUpperCase() == "SCF") {
                    String idRegistro = row.getCell(1).getStringCellValue();
                    String observacao = row.getCell(2).getStringCellValue();
                    Long idDoc = buscarIDDocSAM4(idRegistro);

                    if (idDoc == null) interromper("Não foi encontrado documento com o ID do SAM 3: " + idRegistro);

                    Daa01 daa01 = getSession().createCriteria(Daa01.class).addWhere(Criterions.eq("daa01id", idDoc)).get(ColumnType.ENTITY);
                    daa01.setDaa01obs(observacao);

                    getSession().persist(daa01);
                } else {
                    String idRegistro = row.getCell(1).getStringCellValue();
                    String observacao = row.getCell(2).getStringCellValue();
                    String obsUsoInterno = row.getCell(3).getStringCellValue();

                    Abe01 abe01 = getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", idRegistro)).get(ColumnType.ENTITY)
                    if (abe01 == null) interromper("Não foi encontrada entidade com o código " + idRegistro);

                    Abe02 abe02 = getSession().createCriteria(Abe02.class).addWhere(Criterions.eq("abe02ent", abe01.abe01id)).get(ColumnType.ENTITY);

                    if (abe02 == null) abe02 = criarAbe02(abe01)

                    abe01.setAbe01obs(observacao);
                    abe02.setAbe02obsUsoInt(obsUsoInterno);


                    getSession().persist(abe01);
                    getSession().persist(abe02);
                }
            }
        }
    }

    private Long buscarIDDocSAM4(String idRegistro) {
        String sql = "SELECT daa01id FROM daa01 WHERE CAST(daa01json ->> 'id_documento' AS BIGINT) = :idRegistro";

        return getAcessoAoBanco().obterLong(sql, Parametro.criar("idRegistro", Long.parseLong(idRegistro)));
    }

    private Abe02 criarAbe02(Abe01 abe01) {
        Aac10 aac10 = obterEmpresaAtiva();
        Long idGc = buscarGrupoCentralizadorDaEmpresaAtiva(aac10, "abe02");
        Aac01 aac01 = getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01id", idGc)).get(ColumnType.ENTITY);
        if(aac01 == null) interromper("Não foi encontrado grupo centralizador da empresa ativa para a tabela Abe02.");

        Abe02 abe02 = new Abe02();
        abe02.setAbe02ent(abe01);
        abe02.setAbe02usoLote(0);
        abe02.setAbe02spc(0);
        abe02.setAbe02gc(aac01);

        return abe02;
    }

    private Long buscarGrupoCentralizadorDaEmpresaAtiva(Aac10 aac10, String tabela) {
        return getSession().createCriteria(Aac10.class)
                .addFields("aac1001gc")
                .addJoin(Joins.join("aac1001", "aac1001empresa = aac10id"))
                .addWhere(Criterions.eq("aac10id", aac10.aac10id))
                .addWhere(Criterions.eq("aac1001tabela", tabela))
                .get(ColumnType.LONG)
    }
}