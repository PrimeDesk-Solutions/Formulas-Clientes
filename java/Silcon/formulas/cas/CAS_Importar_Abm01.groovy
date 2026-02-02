package Silcon.formulas.cas;
import org.apache.poi.xssf.usermodel.XSSFSheet
import sam.model.entities.aa.Aac01
import sam.model.entities.ab.Abm01
import sam.model.entities.aa.Aam06
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

public class CAS_Importar_Abm01 extends FormulaBase{

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

        TextFileLeitura txt = new TextFileLeitura(registros, ";");

        int linha = 1;
        while(txt.nextLine()) {
            Abm01 abm01 = new Abm01();
            abm01.setAbm01tipo(Integer.parseInt(txt.getCampo(1)));
            abm01.setAbm01codigo(txt.getCampo(2));
            abm01.setAbm01gtin(txt.getCampo(3));
            abm01.setAbm01livre(txt.getCampo(4));
            abm01.setAbm01reduzido(txt.getCampo(5).isEmpty() ? null : Integer.parseInt(txt.getCampo(5)));
            abm01.setAbm01grupo(Integer.parseInt(txt.getCampo(6)));
            abm01.setAbm01na(txt.getCampo(7));
            abm01.setAbm01descr(txt.getCampo(8));
            if(!txt.getCampo(9).isEmpty()){
                Aam06 aam06 = buscarUMUPeloCodigo(txt.getCampo(9))

                if(aam06 == null) interromper("Unidade de medida " + txt.getCampo(9) + " não encontrada no sistema.");

                abm01.setAbm01umu(aam06);
            }
            abm01.setAbm01arredUmu(txt.getCampo(10).isEmpty() ? null : Integer.parseInt(txt.getCampo(10)));
            abm01.setAbm01pesoBruto(txt.getCampo(11).isEmpty() ? null : new BigDecimal(txt.getCampo(11)));
            abm01.setAbm01pesoLiq(txt.getCampo(12).isEmpty() ? null : new BigDecimal(txt.getCampo(12)));
            Aac01 aac01 = buscarEmpresa()
            abm01.setAbm01gc(aac01);

            try{
                getSession().persist(abm01);
            }catch(Exception e){
                interromper("Erro ao importar registro. Linha: " + linha)
            }

            linha++
        }
    }
    private Aam06 buscarUMUPeloCodigo(String umu){
        return getSession().createCriteria(Aam06.class).addWhere(Criterions.eq("aam06codigo", umu)).get(ColumnType.ENTITY);
    }
    private Aac01 buscarEmpresa(){
        return getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01id", 1075797 )).get()
    }
}