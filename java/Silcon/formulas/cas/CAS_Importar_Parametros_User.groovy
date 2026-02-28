package Silcon.formulas.cas;

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abe01
import org.apache.poi.xssf.usermodel.XSSFSheet
import sam.model.entities.aa.Aab1001
import sam.model.entities.aa.Aab10
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase
import java.time.LocalDate

public class CAS_Importar_Parametros_User extends FormulaBase {
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
                Aab1001 aab1001 = new Aab1001();

                Aab10 aab10 = getSession().createCriteria(Aab10.class).addWhere(Criterions.eq("aab10id", Long.parseLong(txt.getCampo(2)))).get(ColumnType.ENTITY);
                if(aab10 == null) continue;

                aab1001.setAab1001user(aab10);
                aab1001.setAab1001aplic(txt.getCampo(3))
                aab1001.setAab1001param(txt.getCampo(4))
                aab1001.setAab1001conteudo(txt.getCampo(5).isEmpty() ? null : txt.getCampo(5) )

                getSession().persist(aab1001);
            }catch (Exception e){
                interromper(e.getMessage())
            }

        }

    }

    private montarJsonEntidade(TextFileLeitura txt, TableMap jsonAbe01) {

        String txtVlrLimCredito = txt.getCampo(30).toString().replace(",", '.');
        String txtDtVctoLimCredito = !txt.getCampo(31).isEmpty() ? txt.getCampo(31).replace("-", "") : null;

        jsonAbe01.put("vlr_lim_credito", new BigDecimal(txtVlrLimCredito));
        if (txtDtVctoLimCredito) jsonAbe01.put("dt_vcto_lim_credito", txtDtVctoLimCredito)
        jsonAbe01.put("obs_lim_credito", txt.getCampo(32).replace("|", ""));
    }
}