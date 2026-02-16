package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.model.entities.ab.Abb11
import sam.model.entities.da.Daa01
import sam.model.entities.da.Daa0101;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import java.time.LocalDate
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multitec.utils.collections.TableMap;


public class CAS_Importar_Daa0101 extends FormulaBase{
    private MultipartFile arquivo;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        arquivo = get("arquivo");

        List<Daa01> daa01s = buscarDocumentosFinanceiros();

        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        int linha = 1;
        while(txt.nextLine()) {
            Daa0101 daa0101 = new Daa0101();
            Long idDoc = Long.parseLong(txt.getCampo(1));
            String codDepartamento = txt.getCampo(2);
            BigDecimal valor = new BigDecimal(txt.getCampo(3));
            Daa01 daa01;
            for(daa01aux in daa01s){
                TableMap jsonDaa01 = daa01aux.daa01json;
                if(jsonDaa01 == null) continue;
                Long idDocSam3 = jsonDaa01.getLong("id_documento");
                if(idDocSam3 == null) continue // Trata somente os documentos que vieram do SAM 3
                if(idDocSam3 == idDoc) daa01 = daa01aux;
            }

            if(daa01 == null) interromper("Não foi encontrado documento para atrelar o departamento na linha: " + linha);
            if(codDepartamento == null || codDepartamento.isEmpty()) interromper("Não foi encontrado código do departamento na linha " + linha)
            if(valor == null || valor.compareTo(0) == 0) interromper("Valor inválido do departamento na linha " + linha);

            Abb11 abb11 = getSession().createCriteria(Abb11.class).addWhere(Criterions.where(" abb11codigo = '" + codDepartamento + "' AND abb11gc = " + obterEmpresaAtiva().getAac10id())).get(ColumnType.ENTITY);
            if(abb11 == null) interromper("Não foi encontrado departamento com o código " + codDepartamento + " cadastrado no sistema.");

            daa0101.setDaa0101doc(daa01);
            daa0101.setDaa0101depto(abb11);
            daa0101.setDaa0101valor(valor.round(2));

            getSession().persist(daa0101);

            linha++

        }
    }
    private List<Daa01> buscarDocumentosFinanceiros(){
        return getSession().createCriteria(Daa01.class)
                .addFields("daa01id, daa01json")
                .getList(ColumnType.ENTITY)
    }

}