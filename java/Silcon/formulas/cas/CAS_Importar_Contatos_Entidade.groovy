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

public class CAS_Importar_Contatos_Entidade extends FormulaBase {
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
                Abe0104 abe0104 = new Abe0104();
                Abe01 abe01 = getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01id", Long.parseLong(txt.getCampo(1)))).get(ColumnType.ENTITY);

                abe0104.setAbe0104ent(abe01);
                abe0104.setAbe0104nome(txt.getCampo(2).isEmpty() ? null : txt.getCampo(2));
                abe0104.setAbe0104ramal(txt.getCampo(3).isEmpty() ? null : Integer.parseInt(txt.getCampo(3)));
                abe0104.setAbe0104ddd(txt.getCampo(4).isEmpty() ? null : txt.getCampo(4));
                abe0104.setAbe0104fone(txt.getCampo(5).isEmpty() ? null : txt.getCampo(5) );
                abe0104.setAbe0104eMail(txt.getCampo(6).isEmpty() ? null : txt.getCampo(6));
                abe0104.setAbe0104data(txt.getCampo(7).isEmpty() ? null : LocalDate.parse(txt.getCampo(7)));
                abe0104.setAbe0104obs(txt.getCampo(8).isEmpty() ? null : txt.getCampo(8));

                getSession().persist(abe0104);
            }catch (Exception e){
                interromper(e.getMessage())
            }

        }
    }
}