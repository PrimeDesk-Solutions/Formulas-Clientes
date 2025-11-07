package Atilatte.relatorios.sce

import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro;

import java.util.Map;
import java.util.HashMap;

public class SCE_Retorno_Industrializacao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Retorno Industrialização";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        
        /*
            Verificar com o Wilson os demais campos do relatório
         */
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idsStatus = getListLong("status");
        List<Long> idsLocal = getListLong("local");
        Integer impressao = getInteger("impressao");

        List<TableMap> dados = buscarDadosRelatorio(idsTipoDoc, idsEntidades, idsStatus, idsLocal);

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " " + obterEmpresaAtiva().getAac10na())
        params.put("titulo", "SCE - Retorno de Industrialização");

        if(impressao == 1) return gerarXLSX("SCE_Retorno_Industrializacao_Excel", dados);

        return gerarPDF("SCE_Retorno_Industrializacao_PDF", dados)
    }

    private List<TableMap> buscarDadosRelatorio(List<Long>idsTipoDoc,List<Long> idsEntidades,List<Long> idsStatus,List<Long> idsLocal){
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "AND abe01id IN (:idsEntidades) " : "";
        String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND  bcc02status IN (:idsStatus) " : "";
        String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "AND bcc02ctrl0 IN (:idsLocal) " : "";

        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroStatus = idsStatus != null && idsStatus.size() > 0 ? Parametro.criar("idsStatus", idsStatus) : null;
        Parametro parametroLocal = idsLocal != null && idsLocal.size() > 0 ? Parametro.criar("idsLocal", idsLocal) : null;

        String sql = "SELECT abe01codigo AS codEntidade, abe01na AS naEntidade, " +
                "aah01codigo AS codTipoDoc,aah01na AS descrTipoDoc, abb01num AS numDoc, bcc0201qt AS qtd, " +
                "bcc0201lote AS lote, bcc0201serie AS serie,abm01codigo AS codItem, " +
                "abm01na AS naItem " +
                "FROM bcc02 " +
                "INNER JOIN bcc0201 ON bcc0201saldo = bcc02id " +
                "LEFT JOIN abb01 ON abb01id = bcc02centralEst " +
                "LEFT JOIN abe01 ON abe01id = abb01ent " +
                "LEFT JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN abm01 ON abm01id = bcc02item " +
                whereTipoDoc +
                whereEntidades +
                whereStatus +
                whereLocal +
                "ORDER BY abe01codigo, abe01na, abb01num"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipoDoc, parametroEntidades, parametroStatus, parametroLocal);

    }
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJldG9ybm8gSW5kdXN0cmlhbGl6YcOnw6NvIiwidGlwbyI6InJlbGF0b3JpbyJ9