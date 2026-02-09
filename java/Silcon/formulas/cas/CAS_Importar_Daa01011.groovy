package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Join
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.TextFileLeitura
import sam.model.entities.ab.Abb11
import sam.model.entities.ab.Abf10
import sam.model.entities.da.Daa01;
import sam.model.entities.da.Daa0101;
import sam.model.entities.da.Daa01011;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile

public class CAS_Importar_Daa01011 extends FormulaBase{
    private MultipartFile arquivo;


    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        arquivo = get("arquivo");

        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");
        List<TableMap> daa0101s = buscarDepartamentos();

        int linha = 1;
        while(txt.nextLine()) {
            Daa01011 daa01011 = new Daa01011();
            Long idDoc = Long.parseLong(txt.getCampo(1));
            String codDepartamento = txt.getCampo(2)
            String codNatureza = txt.getCampo(3);
            BigDecimal valor = new BigDecimal(txt.getCampo(4));

            if(txt.getCampo(1).isEmpty()) interromper("Não foi encontrado o ID do documento na linha " + linha);
            if(txt.getCampo(2).isEmpty()) interromper("Não foi encontrado o código do departamento na linha " + linha);
            if(txt.getCampo(3).isEmpty()) interromper("Não foi encontrado o código da natureza na linha " + linha);
            if(txt.getCampo(4).isEmpty()) interromper("Não foi encontrado o valor do documento na linha " + linha);

            Daa0101 daa0101
            for(tmDepto in daa0101s){
                if(tmDepto.getLong("idDocSam3") == idDoc && tmDepto.getString("abb11codigo") == codDepartamento) daa0101 = getSession().createCriteria(Daa0101.class).addWhere(Criterions.eq("daa0101id", tmDepto.getLong("daa0101id"))).get(ColumnType.ENTITY)
            }

            Abb11 abb11 = getSession().createCriteria(Abb11.class).addWhere(Criterions.where(" abb11codigo = '" + codDepartamento + "' AND abb11gc = " + obterEmpresaAtiva().getAac10id())).get(ColumnType.ENTITY);
            if(abb11 == null) interromper("Não foi encontrado departamento com o código " + codDepartamento + " cadastrado no sistema.");

            Abf10 abf10 = getSession().createCriteria(Abf10.class).addWhere(Criterions.where("abf10codigo = '" + codNatureza + "' AND abf10gc = " + obterEmpresaAtiva().getAac10id())).get(ColumnType.ENTITY);
            if(abf10 == null) interromper("Não foi encontrado natureza financeira com o código " + codNatureza);

            if(daa0101 == null) interromper("Não foi encontrado departamento para atrelar a natureza. Linha " + linha);

            daa01011.setDaa01011depto(daa0101);
            daa01011.setDaa01011nat(abf10);
            daa01011.setDaa01011valor(valor.round(2));

            getSession().persist(daa01011);

            linha++

        }
    }
    private List<TableMap> buscarDepartamentos(){
        String sql = "SELECT daa0101id, CAST(daa01json ->> 'id_documento' AS bigint) AS idDocSam3, abb11codigo " +
                    "FROM daa01 " +
                    "INNER JOIN daa0101 ON daa0101doc = daa01id "+
                    "INNER JOIN abb11 ON abb11id = daa0101depto ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql)

    }
}