package Inova.formulas.cas

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abe01
import org.apache.poi.xssf.usermodel.XSSFSheet
import sam.model.entities.aa.Aac01
import sam.model.entities.ab.Abm0101
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase
import java.time.LocalDate

public class CAS_Importar_Abm0101json extends FormulaBase{
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
        while (txt.nextLine()){
            Long idItem = Long.parseLong(txt.getCampo(1))
            Abm0101 abm0101 = getSession().get(Abm0101.class, Criterions.eq("abm0101item", idItem))
            if (abm0101 == null ) continue;
            TableMap jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();

            jsonAbm0101.put("aliq_ir", txt.getCampo(2));
            jsonAbm0101.put("aliq_iss", txt.getCampo(3));
            jsonAbm0101.put("aliq_carga_trib_nac", txt.getCampo(4));
            jsonAbm0101.put("aliq_carga_trib_imp", txt.getCampo(5));
            jsonAbm0101.put("aliq_csll", txt.getCampo(6));
            jsonAbm0101.put("preco_presumido_antigo", txt.getCampo(7));
            jsonAbm0101.put("medida_trib", txt.getCampo(8));
            jsonAbm0101.put("ultimo_importacao", txt.getCampo(9));
            jsonAbm0101.put("preco_max_real", txt.getCampo(10));
            jsonAbm0101.put("aliq_importacao", txt.getCampo(11));
            jsonAbm0101.put("aliq_pis", txt.getCampo(12));
            jsonAbm0101.put("aliq_cofins", txt.getCampo(13));
            jsonAbm0101.put("aliq_red_bc_icms", txt.getCampo(14));
            jsonAbm0101.put("aliq_icms", txt.getCampo(15));
            jsonAbm0101.put("preco_max_dolar", txt.getCampo(16));
            jsonAbm0101.put("aliq_fixa_icms_sub", txt.getCampo(17));
            jsonAbm0101.put("q_venda_q_conv", txt.getCampo(18));
            jsonAbm0101.put("preco_medio_anterior", txt.getCampo(19));
            jsonAbm0101.put("preco_medio_atual", txt.getCampo(20));
            jsonAbm0101.put("preco_diverso_custo", txt.getCampo(21));
            jsonAbm0101.put("preco_diverso_maior", txt.getCampo(22));
            jsonAbm0101.put("preco_diverso_menor", txt.getCampo(23));
            jsonAbm0101.put("preco_diverso_ultimo", txt.getCampo(24));
            jsonAbm0101.put("lote_min", txt.getCampo(25));
            jsonAbm0101.put("estoque_max", txt.getCampo(26));
            jsonAbm0101.put("estoque_min", txt.getCampo(27));
            jsonAbm0101.put("custo_presumido", txt.getCampo(28));
            jsonAbm0101.put("versao", txt.getCampo(29) != null ? txt.getCampo(29).replace('"', "") : null);

            abm0101.setAbm0101json(jsonAbm0101)
            getSession().persist(abm0101);

        }
    }
}