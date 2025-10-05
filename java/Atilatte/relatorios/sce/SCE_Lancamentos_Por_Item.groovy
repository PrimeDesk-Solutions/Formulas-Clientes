package Atilatte.relatorios.sce

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCE_Lancamentos_Por_Item extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Lançamentos Por Item";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<String, Object>();
        LocalDate[] datas = DateUtils.getStartAndEndMonth(LocalDate.now());
        filtrosDefault.put("impressao","0");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Integer> mps = getListInteger("mps");
        List<Long> itensInsumo = getListLong("itensInsumo");
        List<Long> itensAcabado = getListLong("itensAcabado");
        LocalDate[] data = getIntervaloDatas("data");
        List<Long> ple = getListLong("ple");
        List<Integer> movimentacao = getListInteger("movimentacao");
        Integer impressao = getInteger("impressao");
        Aac10 empresa = obterEmpresaAtiva();

        List<TableMap> dados = buscarDadosRelatorio(mps, itensInsumo, data, ple, movimentacao, empresa, itensAcabado );

        params.put("titulo", "SCE - Lançamentos Por Itens")
        params.put("empresa", empresa.getAac10codigo() + " - " + empresa.getAac10na())

        if(impressao == 1) return gerarXLSX("SCE_Lancamentos_Por_Item_Excel", dados);

        return gerarPDF("SCE_Lancamentos_Por_Item_PDF", dados)
    }

    private List<TableMap> buscarDadosRelatorio(List<Integer> mps,List<Long> itensInsumo,LocalDate[] data,List<Long> ple, List<Integer> movimentacao, Aac10 empresa, List<Long> itensAcabado ){
        String whereEmpresa = "WHERE bcc01gc IN (:idEmpresa) ";
        String whereMps = !mps.contains(-1) ? "AND principal.abm01tipo IN (:mps) " : "";
        String whereItensInsumo = itensInsumo != null && itensInsumo.size() > 0 ? "AND principal.abm01id IN (:itensInsumo) " : "";
        String whereItensAcabado = itensAcabado != null && itensAcabado.size() > 0 ? "AND acabado.abm01id IN (:itensAcabado) " : "";
        String whereData = data != null ? "AND bcc01data BETWEEN :dtInicial AND :dtFinal " : "";
        String wherePLE = ple != null && ple.size() > 0 ? "AND abm20id IN (:ple) " : ""
        String whereMovimentacao = movimentacao != null && movimentacao.size() > 0 ? "AND abm20rastrear IN (:movimentacao) " : "";

        Parametro parametroEmpresa = Parametro.criar("idEmpresa", empresa.getAac10id())
        Parametro parametroMPS = !mps.contains(-1) ? Parametro.criar("mps", mps) : null;
        Parametro parametroItensInsumo = itensInsumo != null && itensInsumo.size() > 0 ? Parametro.criar("itensInsumo", itensInsumo) : null;
        Parametro parametroItensAcabado = itensAcabado != null && itensAcabado.size() > 0 ? Parametro.criar("itensAcabado", itensAcabado) : null;
        Parametro parametroDataInicio = data != null ? Parametro.criar("dtInicial", data[0]) : null
        Parametro parametroDataFinal = data != null ? Parametro.criar("dtFinal", data[1]) : null
        Parametro parametroPLE = ple != null && ple.size() > 0 ? Parametro.criar("ple", ple) : null;
        Parametro parametroMovimentacao = movimentacao != null && movimentacao.size() > 0 ? Parametro.criar("movimentacao", movimentacao) : null;


        String sql = "SELECT principal.abm01codigo AS codItem, principal.abm01na AS naItem, aam06codigo AS umu, " +
                "abb01num AS numDocEstoque, abm20codigo AS codPle, abm20descr AS descrPle, acabado.abm01codigo as codItemAcab, " +
                "acabado.abm01na AS naProdAcab, aah01codigo as tipoDoc, bcc01data as data, aam04codigo as status, abm15nome as local, " +
                "bcc01lote as lote, bcc01qt as qtd "+
                "FROM bcc01 " +
                "INNER JOIN abm01 AS principal ON principal.abm01id = bcc01item " +
                "INNER JOIN aam06 ON aam06id = principal.abm01umu " +
                "INNER JOIN abm20 ON abm20id = bcc01ple " +
                "LEFT JOIN abb01 ON abb01id = bcc01central " +
                "LEFT JOIN bab01 ON bab01central = abb01id " +
                "LEFT JOIN abp20 ON abp20id = bab01comp " +
                "LEFT JOIN abm01 AS acabado ON acabado.abm01id = abp20item "+
                "LEFT JOIN aah01 ON aah01id = abb01tipo "+
                "INNER JOIN aam04 ON aam04id = bcc01status "+
                "LEFT JOIN abm15 ON abm15id = bcc01ctrl0 "+
                whereEmpresa +
                whereMps+
                whereItensInsumo+
                whereItensAcabado+
                whereData+
                wherePLE+
                whereMovimentacao +
                "ORDER BY principal.abm01codigo, bcc01data, acabado.abm01na  ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroMPS, parametroItensInsumo, parametroItensAcabado, parametroDataInicio, parametroDataFinal, parametroPLE, parametroMovimentacao, parametroEmpresa);

    }
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIExhbsOnYW1lbnRvcyBQb3IgSXRlbSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==