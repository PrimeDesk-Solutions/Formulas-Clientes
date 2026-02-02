package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import sam.model.entities.aa.Aac01
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

import javax.swing.text.html.parser.Entity
import java.time.LocalDate
import java.time.LocalTime

public class CAS_Importar_Central_Documentos_Cashback extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {

        MultipartFile arquivo = get("arquivo");

        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        Integer linha = 0;
        while(txt.nextLine()){
            linha++;
            Integer numDoc = Integer.parseInt(txt.getCampo(1));
            String codEntidade = txt.getCampo(2);
            BigDecimal valor = new BigDecimal(txt.getCampo(3));
            String tipoDoc = txt.getCampo(4);

            // Entidade
            Abe01 abe01 = getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", codEntidade)).addWhere(Criterions.eq("abe01gc", 1075797)).get(ColumnType.ENTITY);
            if(abe01 == null) interromper("Não foi encontrado a entidade para o codigo " + codEntidade.toString() + ". Linha: " + linha);

            // Tipo Documento
            Aah01 aah01 = getSession().createCriteria(Aah01.class).addWhere(Criterions.eq("aah01codigo", tipoDoc)).get(ColumnType.ENTITY);
            if(aah01 == null) interromper("Tipo de documento não encontrado.");

            criarCentral(numDoc, abe01, valor, aah01)
        }
    }
    private void criarCentral(Integer numDoc, Abe01 abe01, BigDecimal valor, Aah01 aah01){
        try{
            Abb01 abb01 = new Abb01();

            abb01.abb01num = numDoc;
            abb01.abb01tipo = aah01;
            abb01.abb01parcela = null;
            abb01.abb01ent = abe01;
            abb01.abb01valor = valor;
            abb01.abb01data = LocalDate.now();
            abb01.abb01operData = LocalDate.now();
            abb01.abb01operHora = LocalTime.now();
            abb01.abb01operUser = obterUsuarioLogado();
            abb01.abb01operAutor = "IMPORTADO SAM 3";
            abb01.abb01intConc = 1;
            abb01.abb01aprovado = 1;
            abb01.abb01status = 0;
            abb01.abb01gc = getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01id", 1075797)).get(ColumnType.ENTITY);
            abb01.abb01eg = obterEmpresaAtiva();

            getSession().persist(abb01);

        } catch(Exception e){
            interromper("Falha ao salvar registro na central de documentos." + e.getMessage())
        }
    }
}

/*
Exemplo arquivo
Número Documento|Id Entidade|Valor|Tipo de Documento
|1|416595|107.24|001|
 */
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=