package Silcon.formulas.cas

import br.com.multiorm.criteria.criterion.Criterion;
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abe01
import org.apache.poi.xssf.usermodel.XSSFSheet
import sam.model.entities.aa.Aab1005
import sam.model.entities.aa.Aab10
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abe0104
import sam.server.samdev.formula.FormulaBase
import java.time.LocalDate

public class CAS_Importar_Email_Campo_Livre extends FormulaBase {
    private XSSFSheet sheet
    private MultipartFile arquivo;


    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        arquivo = get("arquivo");
        leituraePersistBanco()

    }

    private void leituraePersistBanco() {
        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        int linha = 1;
        while (txt.nextLine()) {
            try{
                Abe01 abe01 = getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", txt.getCampo(1))).get(ColumnType.ENTITY);
                if(abe01 == null) interromper("Não foi encontrada entidade com código " + txt.getCampo(1));

                String emails = txt.getCampo(2).trim().replaceAll("\\s*;\\s*", ";"); // remove espaços antes e depois do ;

                TableMap jsonAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();

                jsonAbe01.put("email_faturamento", emails);

                abe01.setAbe01json(jsonAbe01);

                getSession().persist(abe01);
            }catch (Exception e){
                interromper(e.getMessage())
            }
        }
    }
}