/*
    Cria o registro somente na empresa principal. Posteriormente utilizar a fórmula Replicar Abm0101
 */
package Silcon.formulas.cas

import sam.model.entities.ab.Abb11;
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abf10
import sam.model.entities.ab.Abg01
import org.apache.poi.xssf.usermodel.XSSFSheet
import sam.model.entities.ab.Abm01
import sam.model.entities.aa.Aam06
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

public class CAS_Importar_Abm0101 extends FormulaBase{
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
        List<Abm0101> abm0101s = new ArrayList<>()

        int linha = 1;
        while(txt.nextLine()) {
            //for(int i = 0; i <= 3; i++){ // Cria o mesmo registro em todas as empresass
                String codEmpresa = "001" //+ i;
                Aac10 aac10 = buscarEmpresaPeloCodigo(codEmpresa);
                Abm0101 abm0101 = new Abm0101();
                Abm01 abm01 = buscarItemPeloCodigo(txt.getCampo(2), txt.getCampo(1));
                if(abm01 == null) interromper("Item " + txt.getCampo(1) + " - " + txt.getCampo(2) + " não encontrado no sistema.");
                abm0101.setAbm0101item(abm01);
                abm0101.setAbm0101empresa(aac10);
                abm0101.setAbm0101unidDiv(Integer.parseInt(txt.getCampo(3)));
                abm0101.setAbm0101montagem(Integer.parseInt(txt.getCampo(4)));
                abm0101.setAbm0101sgpPrev(Integer.parseInt(txt.getCampo(5)));
                abm0101.setAbm0101sgpCpra(Integer.parseInt(txt.getCampo(6)));
                if(!txt.getCampo(7).isEmpty()){
                    Aam06 aam06 = buscarUnidadeMedidaPeloCodigo(txt.getCampo(7))

                    if(aam06 == null) interromper("Unidade de medida " + txt.getCampo(7) + " não encontrada no sistema.");

                    abm0101.setAbm0101umt(aam06);
                }

                if(!txt.getCampo(8).isEmpty()){
                    Abg01 abg01 = buscarNCMPeloCodigo(txt.getCampo(8));

                    if(abg01 == null) interromper("NCM " + txt.getCampo(8) + " não encontrado no sistema.");

                    abm0101.setAbm0101ncm(abg01);
                }

                abm0101.setAbm0101calcNiveis(Integer.parseInt(txt.getCampo(9)));
                abm0101.setAbm0101itemTerc(Integer.parseInt(txt.getCampo(10)));
                abm0101.setAbm0101wmsMassa(new BigDecimal(txt.getCampo(11)));

                if(!txt.getCampo(12).isEmpty()){
                    Abb11 abb11 = buscarDepartamentoPeloCodigo(txt.getCampo(12));

                    if(abb11 == null) interromper("Departamento " + txt.getCampo(12) + " não encontrado no sistema.");

                    abm0101.setAbm0101eDepto(abb11);
                }

                if(!txt.getCampo(13).isEmpty()){
                    Abf10 abf10 = buscarNaturezaPeloCodigo(txt.getCampo(13));

                    if(abf10 == null) interromper("Natureza " + txt.getCampo(13) + " não encontrado no sistema.");

                    abm0101.setAbm0101eNat(abf10);
                }

                if(!txt.getCampo(13).isEmpty()){
                    Abb11 abb11 = buscarDepartamentoPeloCodigo(txt.getCampo(14));

                    if(abb11 == null) interromper("Departamento " + txt.getCampo(14) + " não encontrado no sistema.");

                    abm0101.setAbm0101sDepto(abb11);
                }

                if(!txt.getCampo(15).isEmpty()){
                    Abf10 abf10 = buscarNaturezaPeloCodigo(txt.getCampo(15));

                    if(abf10 == null) interromper("Natureza " + txt.getCampo(15) + " não encontrado no sistema.");

                    abm0101.setAbm0101sNat(abf10);
                }

                getSession().persist(abm0101);
            //}

        }
    }
    private Aac10 buscarEmpresaPeloCodigo(String codEmpresa){
        return getSession().createCriteria(Aac10.class).addWhere(Criterions.eq("aac10codigo", codEmpresa)).get();
    }
    private Abm01 buscarItemPeloCodigo(String codItem, String tipo){
        if(tipo == 2) tipo = 3
        return getSession().get(Abm01.class, Criterions.where("abm01codigo = '" + codItem + "' AND abm01tipo = " + tipo + " AND abm01gc = 1075797"));
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