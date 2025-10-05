package Atilatte.relatorios.scf

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_Documentos_Por_Tesouraria extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Documentos Por Tesouraria";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", "1");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("conciliacao", "2");
        filtrosDefault.put("classe", "0");
        filtrosDefault.put("emissao", "0");
        filtrosDefault.put("pagamento", true);
        filtrosDefault.put("operador", true);
        filtrosDefault.put("cheque", true);

        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numInicial = getInteger("numeroInicial");
        Integer numFinal = getInteger("numeroFinal");
        Integer fp = getInteger("")
        List<Long> idsContas = getListLong("ccs");
        Integer conciliacao = getInteger("conciliacao");
        Integer classe = getInteger("classe");
        LocalDate[] dtEmissao = getIntervaloDatas("emissao")
        Boolean pagamento = getBoolean("pagamento");
        Boolean operador = getBoolean("operador");
        Boolean cheque = getBoolean("cheque");

        Long idEmpresa = obterEmpresaAtiva().getAac10id();

        List<TableMap> dados = buscarDadosRelatorio(numInicial,numFinal,fp,idsContas,conciliacao,classe,dtEmissao, idEmpresa);

        params.put("titulo","SRF - Documentos Por Tesouraria");
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        params.put("pagamento",pagamento);
        params.put("operador",operador);
        params.put("cheque",cheque);
        
        return gerarPDF("SCF_Documentos_Por_Tesouraria",dados);
    }
    private List<TableMap> buscarDadosRelatorio(Integer numInicial,Integer numFinal,Long fp,List<Long>idsContas,conciliacao,classe,dtEmissao,Long idEmpresa){

        // Data Emissao
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;
        if(dtEmissao != null){
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        String whereNumero = "where dab20num between :numInicial and :numFinal ";
        String whereClasse = classe == 0 ? "and dab10mov = 0 " : "and dab10mov = 1";
        String whereConciliacao = conciliacao == 0 ? "and dab1002dtConc is not null " : conciliacao == 1 ? "and dab1002dtConc is null " : "and (dab1002dtConc is null or dab1002dtConc is not null) "
        String whereEmpresa = "and dab10gc = :idEmpresa ";
        String whereContaCorrente = idsContas != null && idsContas.size() > 0 ? "and dab01id in (:idsContas) " : "";
        String whereFp = fp != null ? "and dab1002fp = :fp " : "";
        String whereDtEmiss = dtEmissao != null ?  "and dab20dtemis between :dtEmissaoIni and :dtEmissaoFin " : "";

        Parametro ParametroNumeroIni = Parametro.criar("numInicial",numInicial);
        Parametro ParametroNumeroFin = Parametro.criar("numFinal",numFinal);
        Parametro ParametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
        Parametro ParametroContaCorrente = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas",idsContas) : null;
        Parametro ParametroFp = fp != null ? Parametro.criar("fp",fp) : null;
        Parametro ParametroDtEmissIni = dtEmissaoIni != null && dtEmissaoFin != null ? Parametro.criar("dtEmissaoIni",dtEmissaoIni) : null;
        Parametro ParametroDtEmissFin = dtEmissaoIni != null && dtEmissaoFin != null ?  Parametro.criar("dtEmissaoFin",dtEmissaoFin) : null;

        String sql = "select dab20num as numCheque, dab20dtemis as emissaoCheque, dab20dtvcto as vctoCheque,dab01codigo as codConta, " +
                    "dab01nome as nomeConta,aah01codigo as codTipoDoc,aah01nome as nomeTipoDoc, abb01num as numDoc, abb01data as emissao, " +
                    "daa01dtvcton,abb01valor as valor, dab01id, abe01codigo as codEnt, abe01na as naEnt " +
                    "from dab10 " +
                    "inner join dab1002 on dab1002lct = dab10id " +
                    "inner join dab20 on dab20id = dab1002cheque " +
                    "left join dab01 ON dab01id = dab1002cc " +
                    "left join abb01 on abb01id = dab10central " +
                    "left join aah01 on aah01id = abb01tipo " +
                    "left join daa01 on daa01central = abb01id " +
                    "left join abe01 on abe01id = abb01ent " +
                    whereNumero +
                    whereClasse +
                    whereConciliacao +
                    whereEmpresa+
                    whereContaCorrente +
                    whereFp +
                    whereDtEmiss;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,ParametroNumeroIni,ParametroNumeroFin,ParametroEmpresa,ParametroContaCorrente,ParametroFp,ParametroDtEmissIni,ParametroDtEmissFin)
    }

}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgUG9yIFRlc291cmFyaWEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgUG9yIFRlc291cmFyaWEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=