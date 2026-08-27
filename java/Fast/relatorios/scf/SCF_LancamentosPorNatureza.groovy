package Fast.relatorios.scf;

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils


import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_LancamentosPorNatureza extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Lançamentos Por Natureza";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("impressao", "0");
		return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");
        List<Long> tipoDoc = getListLong("tipo");
        List<Long> plf = getListLong("plf");
        List<Long> contasCorrentes = getListLong("contaCorrente");
        String codNatIni = getString("naturezaIni");
        String codNatFin = getString("naturezaFin");
        List<Long> entidades = getListLong("entidade");
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        Integer impressao = getInteger("impressao");
        Long idEmpresa = obterEmpresaAtiva().getAac10id();

        List<TableMap> dados = buscarLancamentos(numDocIni,numDocFin,tipoDoc,plf,codNatIni,codNatFin,entidades,dataEmissao,idEmpresa,contasCorrentes);

        if(impressao == 1 ) return gerarXLSX("SCF_LancamentosPorNatureza_Excel", dados);
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() +"-"+obterEmpresaAtiva().getAac10na());
        params.put("titulo", "SRF - Lançamentos Por Natureza");

        return gerarPDF("SCF_LancamentosPorNatureza_PDF", dados);



    }
    private List<TableMap> buscarLancamentos(Integer numDocIni,Integer numDocFin,List<Long> tipoDoc,List<Long> plf, String codNatIni,String codNatFin,List<Long> entidades, LocalDate[] dataEmissao,Long idEmpresa,List<Long> contasCorrentes){

        // Data Inicial - Final
        LocalDate dataIni = null;
        LocalDate dataFin = null;
        if(dataEmissao != null){
            dataIni = dataEmissao[0];
            dataFin = dataEmissao[1];
        }

        String whereNumDoc = "where abb01num between :numDocIni and :numDocFin ";
        String whereEmpresa = "and dab10gc = :idEmpresa ";
        String whereContas = contasCorrentes != null && contasCorrentes.size() > 0 ? "and dab01id in (:contasCorrentes)" : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? "and aah01id in (:tipoDoc)" : "";
        String wherePlf = plf != null && plf.size() > 0 ? "and dab10plf in (:plf) " : "";
        String whereNatureza = codNatIni != null && codNatFin != null ? "and abf10codigo between :codNatIni and :codNatFin "  : codNatIni != null && codNatFin == null ? "and abf10codigo >= :codNatIni " : codNatIni == null && codNatFin != null ? "and abf10codigo <= :codNatFin " : "";
        String whereEntidade = entidades != null && entidades.size() > 0 ? "and abe01id in (:entidades)" : "";
        String whereDataEmissao = dataIni != null && dataFin != null ? "and abb01data between :dataIni and :dataFin " : "";

        Parametro parametroNumDocIni = Parametro.criar("numDocIni",numDocIni);
        Parametro parametroNumDocFin = Parametro.criar("numDocFin",numDocFin);
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
        Parametro parametroContas = contasCorrentes != null && contasCorrentes.size() > 0 ? Parametro.criar("contasCorrentes",contasCorrentes) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc",tipoDoc) : null;
        Parametro parametroPlf = plf != null && plf.size() > 0 ? Parametro.criar("plf",plf) : null;
        Parametro parametroNaturezaIni= codNatIni != null ? Parametro.criar("codNatIni",codNatIni) : null;
        Parametro parametroNaturezaFin= codNatFin != null ? Parametro.criar("codNatFin",codNatFin) : null;
        Parametro parametroEntidade = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades",entidades) : null;
        Parametro parametroDataIni = dataIni != null ? Parametro.criar("dataIni",dataIni) : null;
        Parametro parametroDataFin = dataFin != null ? Parametro.criar("dataFin",dataFin) : null;

        String sql = "select dab01codigo as codCC, dab01nome as nomeCC, abf10codigo as codNatureza,abf10nome as nomeNatureza, " +
                            "dab10data as dtLancamento,abe01codigo as codEnt, abe01na as naEnt, aah01codigo as codTipoDoc, aah01nome as descrTipoDoc, " +
                            "abb01num as numDoc, abb01parcela as parcela, abb01quita as quita, \n" +
                            "case when dab10mov = 0 then '0-Entrada' else '1-Saída' end as movimentacao, dab10011valor as valorDoc, dab10valor as valorPago " +
                            "from dab10 " +
                            " LEFT JOIN dab1002 ON dab1002lct = dab10id "+
					   " LEFT JOIN dab01 ON dab01id = dab1002cc " +
                            "inner join dab1001 on dab1001lct = dab10id " +
                            "inner join dab10011 on dab10011depto = dab1001id " +
                            "inner join abf10 on abf10id = dab10011nat " +
                            "inner join abb01 on abb01id = dab10central " +
                            "inner join abe01 on abe01id = abb01ent " +
                            "inner join aah01 on aah01id = abb01tipo " +
                            whereNumDoc+
                            whereTipoDoc+
                            wherePlf+
                            whereNatureza+
                            whereEntidade+
                            whereDataEmissao+
                            whereEmpresa+
                            whereContas+
                            "order by abb01num,aah01codigo, abf10codigo,abb01parcela, abb01data";

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumDocIni,parametroNumDocFin,parametroEmpresa,parametroContas,parametroTipoDoc,parametroPlf,parametroNaturezaIni,parametroNaturezaFin,parametroEntidade,parametroDataIni,parametroDataFin);

    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyBQb3IgTmF0dXJlemEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=