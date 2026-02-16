/*
    OBSERVAÇÕES: Não esquecer de ajustar o valor de limite de crédito (Dividir por 1000)
 */

package Silcon.formulas.cas;

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abe01
import org.apache.poi.xssf.usermodel.XSSFSheet
import sam.model.entities.aa.Aac01
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase
import java.time.LocalDate

public class CAS_Importar_Abe01 extends FormulaBase{
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
    private void leituraePersistBanco(){
        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        int linha = 1;
        while(txt.nextLine()) {
            Abe01 abe01 = new Abe01();
            TableMap jsonAbe01 = new TableMap();
            abe01.setAbe01codigo(txt.getCampo(1));
            abe01.setAbe01codAux(!txt.getCampo(2).isEmpty() ? txt.getCampo(2) : null);
            abe01.setAbe01na(txt.getCampo(3));
            abe01.setAbe01nome(txt.getCampo(4));
            abe01.setAbe01site(txt.getCampo(5).isEmpty() ? null : txt.getCampo(4));
            abe01.setAbe01ti(Integer.parseInt(txt.getCampo(6)));
            abe01.setAbe01ni(txt.getCampo(7));
            abe01.setAbe01ie(txt.getCampo(8));
            abe01.setAbe01im(txt.getCampo(9).isEmpty() ? null : txt.getCampo(9));
            abe01.setAbe01suframa(txt.getCampo(10).isEmpty() ? null : txt.getCampo(10));
            abe01.setAbe01regTrib(txt.getCampo(11).isEmpty() ? null : Integer.parseInt(txt.getCampo(11)));
            abe01.setAbe01tipoSoc(txt.getCampo(12).isEmpty() ? null : Integer.parseInt(txt.getCampo(12)));
            abe01.setAbe01porte(txt.getCampo(13).isEmpty() ? null : Integer.parseInt(txt.getCampo(13)));
            abe01.setAbe01dtNasc(LocalDate.parse(txt.getCampo(14).trim()));
            abe01.setAbe01pis(txt.getCampo(15).isEmpty() ? null : txt.getCampo(15));
            abe01.setAbe01rg(txt.getCampo(16).isEmpty() ? null : txt.getCampo(16));
            abe01.setAbe01rgDtExped(!txt.getCampo(17).isEmpty() ? LocalDate.parse(txt.getCampo(17)) : null);
            abe01.setAbe01rgEe(txt.getCampo(18).isEmpty() ? null : txt.getCampo(18));
            abe01.setAbe01rgOE(txt.getCampo(19).isEmpty() ? null : txt.getCampo(19));
            abe01.setAbe01obs(txt.getCampo(20));
            abe01.setAbe01cli(Integer.parseInt(txt.getCampo(21)));
            abe01.setAbe01for(Integer.parseInt(txt.getCampo(22)));
            abe01.setAbe01trans(Integer.parseInt(txt.getCampo(23)));
            abe01.setAbe01rep(Integer.parseInt(txt.getCampo(24)));
            Aac01 aac01 = getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01id", Long.parseLong(txt.getCampo(25)))).get(ColumnType.ENTITY)
            abe01.setAbe01gc(aac01);
            abe01.setAbe01contribIcms(Integer.parseInt(txt.getCampo(26)));
            abe01.setAbe01consOperCbk(new BigDecimal(txt.getCampo(27)))
            abe01.setAbe01govTipo(Integer.parseInt(txt.getCampo(28)));
            abe01.setAbe01govRed(new BigDecimal(txt.getCampo(29)));

            montarJsonEntidade(txt, jsonAbe01);

            abe01.setAbe01json(jsonAbe01);

            try{
                getSession().persist(abe01);
            }catch (Exception e){
                interromper(e.getMessage() + " Linha: " + linha)
            }

            linha++
        }
       
    }

    private montarJsonEntidade(TextFileLeitura txt,TableMap jsonAbe01){

        String txtVlrLimCredito = txt.getCampo(30).toString().replace(",", '.');
        String txtDtVctoLimCredito = !txt.getCampo(31).isEmpty() ? txt.getCampo(31).replace("-", "") : null;

        jsonAbe01.put("vlr_lim_credito", new BigDecimal(txtVlrLimCredito));
        if(txtDtVctoLimCredito) jsonAbe01.put("dt_vcto_lim_credito",txtDtVctoLimCredito)
        jsonAbe01.put("obs_lim_credito", txt.getCampo(32).replace("|", ""));
    }
}