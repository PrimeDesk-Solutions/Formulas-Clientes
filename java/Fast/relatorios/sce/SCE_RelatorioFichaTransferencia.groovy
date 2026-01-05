package Fast.relatorios.sce;

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class SCE_RelatorioFichaTransferencia extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Relatório Ficha Transferência";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsItens = getListLong("itens");
        List<Long> idsPle = getListLong("ple");
        List<Long> idsDepartamento = getListLong("departamento");
        LocalDate[] datas = getIntervaloDatas("data");
        String loteInicial = getString("loteIni");
        String loteFinal = getString("loteFin");
        List<Integer> tipoItem = getListInteger("mps");


        List<TableMap> dados = buscarDadosRelatorio(idsItens, idsPle, datas, loteInicial, loteFinal, tipoItem, idsDepartamento );

        params.put("dtInicial", datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString())
        params.put("user", obterUsuarioLogado().getAab10nome())

        return gerarPDF("SCE_RelatorioFichaTransferencia", dados);

    }

    private List<TableMap> buscarDadosRelatorio(List<Long> idsItens, List<Long> idsPle, LocalDate[] datas,String loteInicial, String loteFinal, List<Integer> tipoItem, List<Long> idsDepartamento){
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String wherePle = idsPle != null && idsPle.size() > 0 ? "and abm20id in (:idsPle) " : "";
        String whereDepartamento = idsDepartamento != null && idsDepartamento.size() > 0 ? "and abb11id in (:idsDepartamento) " : "";
        String whereTipoItem = !tipoItem.contains(-1) ?  "and abm01tipo in (:tipoItem) " : "";
        String whereLoteIni =  loteInicial != null && !loteInicial.isEmpty() ? "and bcc01lote >= :loteInicial " : "";
        String whereLoteFin =  loteFinal != null && !loteFinal.isEmpty() ? "and bcc01lote <= :loteFinal " : "";
        String whereData = datas != null ?  "and bcc01data between :dtInicial and :dtFinal "  : "";


        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroPle = idsPle != null && idsPle.size() > 0 ? Parametro.criar("idsPle", idsPle) : null;
        Parametro parametroDepartamento = idsDepartamento != null && idsDepartamento.size() > 0 ? Parametro.criar("idsDepartamento", idsDepartamento) : null;
        Parametro parametroTipoItem = !tipoItem.contains(-1) ?  Parametro.criar("tipoItem", tipoItem) : null;
        Parametro parametroLoteIni = loteInicial != null && !loteInicial.isEmpty() ? Parametro.criar("loteInicial", loteInicial) : null;
        Parametro parametroLoteFin = loteFinal != null && !loteFinal.isEmpty() ? Parametro.criar("loteFinal", loteFinal) : null;
        Parametro parametroDataIni = datas != null ? Parametro.criar("dtInicial", datas[0]) : null;
        Parametro parametroDataFin = datas != null ? Parametro.criar("dtFinal", datas[1]) : null;

        String sql = "select abm01codigo as codigoItem, abm01na as descrItem, bcc01lote as lote, bcc01fabric as dtFabricacao, bcc01validade as dtValidade, " +
                    "abm20codigo as codPle, abm20descr as descrPle, bcc01qt as qtd " +
                    "from bcc01 " +
                    "inner join abm01 on abm01id = bcc01item " +
                    "inner join abm20 on abm20id = bcc01ple " +
                    "left join abb11 on bcc01depto = abb11id "+
                    "where true "+
                    whereItens +
                    wherePle +
                    whereDepartamento +
                    whereTipoItem +
                    whereLoteIni +
                    whereLoteFin +
                    whereData +
                    "order by abm01codigo, bcc01lote "



        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroItens, parametroPle,parametroDepartamento, parametroTipoItem, parametroLoteIni, parametroLoteFin, parametroDataIni, parametroDataFin)

    }
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJlbGF0w7NyaW8gRmljaGEgVHJhbnNmZXLDqm5jaWEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=