package Atilatte.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SRF_DocumentosCancelados extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Documentos Cancelados - LCR";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", "000000001")
        filtrosDefault.put("numeroFinal", "999999999")
        filtrosDefault.put("impressao", "0")
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        String NumIni = getString("numeroInicial")
        String NumFim = getString("numeroFinal")
        List<Long> tipos = getListLong("tipos")
        List<Long> entidades = getListLong("entidades")
        List<Long> motivos = getListLong("motivos")
        LocalDate[] dataCanc = getIntervaloDatas("dataCanc")
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao")
        Integer impressao = getInteger("impressao")

        params.put("aac10rs", obterEmpresaAtiva().getAac10rs())
        if (dataCanc != null) {
            params.put("periodo", "Periodo de cancelamento " + dataCanc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " á " + dataCanc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString())
        } else if (dataEmissao != null) {
            params.put("periodo", "Periodo de emissao " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " á " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString())
        }

        List<TableMap> documentos = buscarDados(NumIni, NumFim, tipos, entidades, motivos, dataCanc, dataEmissao)
        if (impressao == 1) return gerarXLSX(documentos)
        return gerarPDF(documentos)

    }

    private List<TableMap> buscarDados(String numIni, String numFim, List<Long> tipos, List<Long> entidades, List<Long> motivos, LocalDate[] dataCanc, LocalDate[] dataEmissao) {

        String whereNumIni = numIni != null ? " AND abb01num >= '" + numIni + "'" : ""
        String whereNumFim = numFim != null ? " AND abb01num <= '" + numFim + "'" : ""
        String whereTipos = tipos != null && tipos.size() > 0 ? " AND aah01id IN (:tipos) " : ""
        String whereEntidades = entidades != null && entidades.size() > 0 ? " AND abe01id IN (:ent) " : ""
        String whereMotivos = motivos != null && motivos.size() > 0 ? " AND aae11id IN (:mot) " : ""
        String whereDataCanc = dataCanc != null && dataCanc.size() > 0 ? " AND eaa01cancdata BETWEEN :dtcancini AND :dtcancfim " : ""
        String whereDataEmissao = dataEmissao != null && dataEmissao.size() > 0 ? " AND abb01data BETWEEN :dtemini AND :dtemfim " : ""


        Parametro parametroTipos = tipos != null && tipos.size() > 0 ? Parametro.criar("tipos", tipos) : null
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("ent", entidades) : null
        Parametro parametroMotivos = motivos != null && motivos.size() > 0 ? Parametro.criar("mot", motivos) : null
        Parametro parametroDataCancIni = dataCanc != null && dataCanc.size() > 0 ? Parametro.criar("dtcancini", dataCanc[0]) : null
        Parametro parametroDataCancFim = dataCanc != null && dataCanc.size() > 0 ? Parametro.criar("dtcancfim", dataCanc[1]) : null
        Parametro parametroDataEmissaoIni = dataEmissao != null && dataEmissao.size() > 0 ? Parametro.criar("dtemini", dataEmissao[0]) : null
        Parametro parametroDataEmissaoFim = dataEmissao != null && dataEmissao.size() > 0 ? Parametro.criar("dtemfim", dataEmissao[1]) : null

        String sql = " SELECT aah01codigo, abb01num, abb01data, abe01codigo, abe01na, aah01nome, " +
                " eaa01cancdata, aae11codigo, aae11descr, eaa01cancobs,eaa01totdoc " +
                " FROM eaa01 " +
                " INNER join abb01 on abb01id = eaa01central " +
                " INNER join aah01 on aah01id = abb01tipo " +
                " INNER join abe01 on abe01id = abb01ent " +
                " left join aae11 on aae11id = eaa01cancmotivo " +
                obterWherePadrao("eaa01", "where") + whereNumIni + whereNumFim +
                whereTipos + whereEntidades + whereMotivos + whereDataCanc + whereDataEmissao +
                "AND eaa01cancdata IS NOT NULL " +
                "AND eaa01clasDoc = 1 "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipos, parametroEntidades, parametroMotivos, parametroDataCancIni, parametroDataCancFim, parametroDataEmissaoIni, parametroDataEmissaoFim)
    }


}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgQ2FuY2VsYWRvcyAtIExDUiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==