package Silcon.relatorios.spv;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro;

import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.collections.TableMap;

public class SPV_Etiquetas extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPV - Etiquetas";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        Integer numPreVenda = getInteger("numero");

        List<TableMap> dados = buscarDadosRelatorio(numPreVenda);

        return gerarPDF("SPV_Etiquetas", dados);
    }
    private List<TableMap> buscarDadosRelatorio(Integer numPreVenda){
        String sql = "SELECT abm01codigo AS codigo, abm01descr AS descricao, ccb0101unit AS unit, CURRENT_DATE AS data, abm01gtin AS ean " +
                "FROM ccb01 " +
                "INNER JOIN ccb0101 ON ccb0101pv = ccb01id " +
                "INNER JOIN abm01 ON abm01id = ccb0101item " +
                "WHERE ccb01num = :numPreVenda " +
                "ORDER BY abm01codigo";

        Parametro parametroNumero = Parametro.criar("numPreVenda", numPreVenda);

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumero);
    }
}
//meta-sis-eyJkZXNjciI6IlNQViAtIEV0aXF1ZXRhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==