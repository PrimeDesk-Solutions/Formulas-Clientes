package multitec.baseDemo;

import java.time.LocalDate

import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.dto.sce.ItemSCEDto
import sam.model.entities.ab.Abm01
import sam.server.samdev.formula.FormulaBase;

public class SCE_ImportarItensLctos extends FormulaBase {

    private Long aam02id;
    private MultipartFile arquivo;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCE_ES_MULTIPLAS;
    }

    @Override
    public void executar() {
        aam02id = get("aam02id");
        arquivo = get("arquivo");

        List<ItemSCEDto> itensLctos = new ArrayList<>();

        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        while(txt.nextLine()) {
            Integer abm01tipo = Integer.parseInt(txt.getCampo(1));
            String abm01codigo = txt.getCampo(2);

            Long abm01id = getSession().createCriteria(Abm01.class)
                    .addWhere(Criterions.eq("abm01tipo", abm01tipo))
                    .addWhere(Criterions.eq("abm01codigo", abm01codigo))
                    .addWhere(getSamWhere().getCritPadrao(Abm01.class))
                    .get(ColumnType.LONG);

            if(abm01id == null) interromper("Não foi encontrado o item " + abm01codigo);

            BigDecimal quantidade = new BigDecimal(txt.getCampo(3));
            BigDecimal valor = new BigDecimal(txt.getCampo(4));
            String lote = txt.getCampo(5) != null && txt.getCampo(5).length() > 0 ? txt.getCampo(5) : null;
            String serie = txt.getCampo(6) != null && txt.getCampo(6).length() > 0 ? txt.getCampo(6) : null;
            LocalDate validade = txt.getCampo(7) != null && txt.getCampo(7).length() > 0 ? LocalDate.parse(txt.getCampo(7)) : null;
            LocalDate fabricacao = txt.getCampo(8) != null && txt.getCampo(8).length() > 0 ? LocalDate.parse(txt.getCampo(8)) : null;
            Long idStatus = Long.parseLong(txt.getCampo(9));
            Long idLocal = Long.parseLong(txt.getCampo(10));

            ItemSCEDto itemLcto = new ItemSCEDto();
            itemLcto.abm01id = abm01id;
            itemLcto.quantidade = quantidade;
            itemLcto.valor = valor;
            itemLcto.lote = lote;
            itemLcto.serie = serie;
            itemLcto.validade = validade;
            itemLcto.fabricacao = fabricacao;

            itemLcto.json = null; //receber um TableMap com campos e valores a partir do id da especificação (aam02id)

            //Ids de Status e Locais de armazenamento para saída (caso utilize)
            itemLcto.aam04idSaida = null;
            itemLcto.abm15idSaida0 = null;
            itemLcto.abm15idSaida1 = null;
            itemLcto.abm15idSaida2 = null;

            //Ids de Status e Locais de armazenamento para entrada (caso utilize)
            itemLcto.aam04idEntrada = idStatus;
            itemLcto.abm15idEntrada0 = idLocal;
            itemLcto.abm15idEntrada1 = null;
            itemLcto.abm15idEntrada2 = null;

            itensLctos.add(itemLcto);
        }

        put("itensLctos", itensLctos);
    }

    /*
     *
        TXT de Exemplo
        Campos separados por pipe "|"

        |abm01tipo|abm01codigo|quantidade|valor|lote|serie|validade|fabricacao|

        |0|0101001|5.000000|2.50|LRX|1|2023-07-01|2023-05-04|
        |0|0101001|4.000000|2.45|LRX|2|2023-07-01|2023-05-04|
        |0|0101001|3.000000|2.30|LRX|3|2023-07-01|2023-05-04|
        |0|0101002|6.000000|3.75|MTD||2023-08-01|2023-05-05|
        |0|0101003|7.00|4.10|||||

     *
     */
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzUifQ==