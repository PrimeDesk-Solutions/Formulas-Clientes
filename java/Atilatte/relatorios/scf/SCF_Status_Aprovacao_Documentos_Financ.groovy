package Atilatte.relatorios.scf

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_Status_Aprovacao_Documentos_Financ extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Status Aprovação Doc. Financeiros";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("status", "0");


        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        LocalDate[] periodo = getIntervaloDatas("periodo");
        List<Long> empresas = getListLong("empresa");
        Integer status = getInteger("status");
        Integer impressao = getInteger("impressao");
        LocalDate[] dtVencimento = getIntervaloDatas("vencimento");


        List<TableMap> dados = buscarDados(periodo, empresas, status, dtVencimento);

        for(dado in dados){
            LocalDate dtAprovacao = dado.getDate("dtAprovacao");

            if(dtAprovacao == null ){
                dado.put("status", "Não Aprovado");
            }else{
                dado.put("status", "Aprovado")
            }
        }

        params.put("titulo", "SCF - Status de Aprovação de Documentos");
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());

        if (impressao == 1) return gerarXLSX("SCF_Status_Aprovacao_Documentos_Financ_Excel", dados);

        return gerarPDF("SCF_Status_Aprovacao_Documentos_Financ_PDF", dados);

    }


    private List<TableMap> buscarDados(LocalDate[] periodo, List<Long> empresas, Integer status, LocalDate[] dtVencimento){
        String wherePeriodo = periodo != null ? "AND abb01data BETWEEN :dtInicial AND :dtFinal " : "";
        String whereEmpresa = empresas != null && empresas.size() > 0 ? "AND daa01gc IN (:empresas) " : "AND daa01gc = :empresa ";
        String whereStatus = status == 0 ? "AND abb0103data IS NOT NULL " :
                             status == 1 ? "AND abb0103data IS NULL " : "";
        String whereVencimento = dtVencimento != null ? "AND daa01dtVctoR BETWEEN :dtVctoIni AND :dtVctoFin " : "";



        Parametro parametroPeriosoIni = periodo != null ? Parametro.criar("dtInicial", periodo[0]) : null;
        Parametro parametroPeriosoFin = periodo != null ? Parametro.criar("dtFinal", periodo[1]) : null;
        Parametro parametroEmpresa = empresas != null && empresas.size() > 0 ? Parametro.criar("empresas", empresas) : Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroDtVctoIni = dtVencimento != null ? Parametro.criar("dtVctoIni", dtVencimento[0]) : null;
        Parametro parametroDtVctoFin = dtVencimento != null ? Parametro.criar("dtVctoFin", dtVencimento[1]) : null;



        String sql = "SELECT abb01num AS numDoc, abe01codigo AS codEntidade, abb01data AS dtEmissao, abe01na AS naEntidade, daa01dtvctor AS dtVcto, " +
                        "daa01valor AS valor, abb0103data AS dtAprovacao, abb0103hora AS horaAprov, aab10user AS user " +
                        "FROM daa01 " +
                        "INNER JOIN abb01 ON abb01id = daa01central " +
                        "INNER JOIN abe01 ON abe01id = abb01ent " +
                        "INNER JOIN abb0103 ON abb0103central = abb01id " +
                        "LEFT JOIN aab10 ON aab10id = abb0103user " +
                        "WHERE TRUE "+
                        wherePeriodo+
                        whereEmpresa+
                        whereStatus+
                        whereVencimento +
                        "ORDER BY abb01num";


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroPeriosoIni, parametroPeriosoFin, parametroEmpresa, parametroDtVctoIni, parametroDtVctoFin);

    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIFN0YXR1cyBBcHJvdmHDp8OjbyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNDRiAtIFN0YXR1cyBBcHJvdmHDp8OjbyBEb2MuIEZpbmFuY2Vpcm9zIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRiAtIFN0YXR1cyBBcHJvdmHDp8OjbyBEb2MuIEZpbmFuY2Vpcm9zIiwidGlwbyI6InJlbGF0b3JpbyJ9