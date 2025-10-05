package Atilatte.relatorios.srf

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
//import org.exolab.castor.mapping.xml.Param;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class SRF_Curva_ABC_Entidades extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Curva ABC Entidades";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("tipoOperacao", "0");
        filtrosDefault.put("impressao","0");
        filtrosDefault.put("visualizacao","0");
        filtrosDefault.put("resumo","0");
        filtrosDefault.put("resumoOperacao","0")
        filtrosDefault.put("valorA","0,00")
        filtrosDefault.put("valorB","0,00")
        filtrosDefault.put("valorC","100,0")
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer resumoOperacao = getInteger("resumoOperacao");
        Integer numInicial = getInteger("numeroInicial");
        Integer numFinal = getInteger("numeroFinal");
        List<Long> idsTipos = getListLong("tipo");
        List<Long> idsPcds = getListLong("pcd");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idsItens = getListLong("itens");
        String campoLivre = getString("campoLivre");
        String campoFixo = getString("campoFixo");
        List<Integer> mps = getListInteger("mps");
        List<Long> criterios = getListLong("criterios");
        Integer visualizacao = getInteger("visualizacao");
        List<Integer> tipoOperacao = getListInteger("tipoOper");
        Integer impressao = getInteger("impressao")
        def valorA = getBigDecimal("valorA");
        def valorB = getBigDecimal("valorB");
        def valorC = getBigDecimal("valorC");

        if(valorA == null || valorA == 0 || valorB == null || valorB == 0 ) interromper("Necesário preencher os valores da classificação ABC");

        if(valorB < valorA) interromper("O valor B deve ser maior que o valor A");

        if(valorA == valorB || valorA == valorC || valorB == valorC ) interromper("Não é permitido inserir valores repetidos na classificação ABC");

        Map<String,String> campos = new HashMap<>();

        if(campoLivre != null && campoFixo != null){
            interromper("Preencha apenas 1 CAMPO FIXO OU LIVRE para prosseguir.")
        }

        if(campoLivre == null && campoFixo == null)  interromper("Necessário preencher ao menos um campo para prosseguir.")

        List<TableMap> dados = buscarDadosRelatorio(resumoOperacao, numInicial, numFinal, idsTipos, idsPcds,
                dtEmissao, dtEntradaSaida, idsEntidades, idsItens, campoLivre,campoFixo, mps, criterios, visualizacao, tipoOperacao)
        def valorAcum  = 0;
        def percentValor = 0;
        def percentAcum = 0;
        def numLinha = 0;

        BigDecimal valorTotal = buscarValorTotal (resumoOperacao, numInicial, numFinal, idsTipos, idsPcds,
                dtEmissao, dtEntradaSaida, idsEntidades, idsItens, campoLivre,campoFixo, mps, criterios, tipoOperacao)

        for(dado in dados){
            numLinha++;
            valorAcum += dado.getBigDecimal_Zero("campo")
            percentValor = (valorAcum / valorTotal) * 100;

            percentAcum += percentValor

            dado.put("percentValor", percentValor);
            dado.put("percentAcum", percentAcum )
            dado.put("valorAcumulado", valorAcum);
            dado.put("numLinha", numLinha)

            // Buscar nome campo selecionado
            String campo = buscarNomeCampoFixo(campoFixo);

            if(campo != null){
                dado.put("nomeCampo", campo)
            }else{
                campo = buscarNomeCampoLivre(campoLivre);

                dado.put("nomeCampo", campo);
            }

            // Classifica A,B ou C
            if(percentAcum <= valorA){
                dado.put("classe", 'A');
            }else if(percentAcum <= valorB){
                dado.put("classe",'B')
            }else{
                dado.put("classe", 'C')
            }
        }

        String periodo = ""
        if(dtEmissao != null) {
            periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }else if(dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("titulo","Curva ABC Entidades");
        params.put("empresa",obterEmpresaAtiva().getAac10codigo() +"-"+ obterEmpresaAtiva().getAac10na());
        params.put("periodo",periodo);

        if(impressao == 0 && visualizacao == 0){
            return gerarPDF("SRF_CurvaABCRedePDF", dados);
        }else if(impressao == 0 && visualizacao == 1){
            return gerarPDF("SRF_CurvaABCEntidadesPDF", dados);
        }else if(impressao == 1 && visualizacao == 0){
            return gerarXLSX("SRF_CurvaABCRedeExcel", dados);
        }else{
            return gerarXLSX("SRF_CurvaABCEntidadesExcel", dados);
        }
    }

    private List<TableMap> buscarDadosRelatorio(Integer resumoOperacao,Integer numInicial,Integer numFinal, List<Long> idsTipos,List<Long> idsPcds,
                                                LocalDate[] dtEmissao,LocalDate[] dtEntradaSaida,List<Long> idsEntidades,List<Long> idsItens, String campoLivre, String campoFixo, List<Integer> mps, List<Long> criterios, Integer visualizacao, List<Integer> tipoOperacao){


        String whereResumoOperacao = resumoOperacao == 0 ? "AND eaa01esMov = 0 " : "AND eaa01esMov = 1 ";
        String whereClasDoc = "AND eaa01clasDoc = 1 ";
        String whereNumDocIni = numInicial != null ? "AND abb01num >= :numInicial " : "";
        String whereNumDocFin = numFinal != null ? "AND abb01num <= :numFinal " : "";
        String whereTipoDoc = idsTipos != null && idsTipos.size() > 0 ? "AND aah01id in (:idsTipos) " : "";
        String wherePcds = idsPcds != null && idsPcds.size() > 0 ? "AND abd01id in (:idsPcds) " : "";
        String whereDataEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "AND abb01data BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "AND abe01id IN (:idsEntidades) " : "";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :empresa "
        String whereMps = !mps.contains(-1) ? "AND abm01tipo in (:mps) " : "";
        String whereCancData = "AND eaa01cancdata IS NULL ";
        String whereCriterio = criterios != null && criterios.size() > 0 ? "AND aba3001id IN (:criterios) " : "";
        String whereTipoOper = tipoOperacao != null && tipoOperacao.size() > 0 ? "AND abb10tipoCod IN (:tipoOperacao) " : "";

        Parametro parametroNumDocIni = numInicial != null ? Parametro.criar("numInicial", numInicial) : null;
        Parametro parametroNumDocFin = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
        Parametro parametroTipoDoc = idsTipos != null && idsTipos.size() > 0 ? Parametro.criar("idsTipos",idsTipos) : null;
        Parametro parametroPcds = idsPcds != null && idsPcds.size() > 0 ? Parametro.criar("idsPcds",idsPcds) : null;
        Parametro parametroDataEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni",dtEmissao[0]) : null;
        Parametro parametroDataEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin",dtEmissao[1]) : null;
        Parametro parametroDataEntradaSaidaIni = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaIni",dtEntradaSaida[0]) : null;
        Parametro parametroDataEntradaSaidaFin = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaFin",dtEntradaSaida[1]) : null;
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroMps = !mps.contains(-1) ? Parametro.criar("mps", mps) : null;
        Parametro parametroCriterio = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios", criterios) : null;
        Parametro parametroTipoOper = tipoOperacao != null && tipoOperacao.size() > 0 ? Parametro.criar("tipoOperacao", tipoOperacao) : null;

        String whereCampoFiltro = campoLivre != null ? "SUM(CAST(eaa01json ->> '"+campoLivre+"'"+" as NUMERIC(18,2))) AS campo " : "SUM("+campoFixo+") AS campo "  ;
        String orderBy = campoLivre != null ? "ORDER BY SUM(CAST(eaa01json ->> '"+campoLivre+"'"+" as NUMERIC(18,2))) DESC ": "ORDER BY SUM("+campoFixo+") DESC ";
        String fields = visualizacao == 0 ? "aba3001descr AS rede, " : "abe01codigo, abe01na,aba3001descr AS rede,";
        String groupBy = visualizacao == 0 ? "GROUP BY aba3001descr " : "GROUP BY abe01codigo, abe01na, aba3001descr ";

        String sql = "SELECT row_number() over () AS numLinha, "+
                    fields +
                    whereCampoFiltro +
                    " FROM eaa01 " +
                    "INNER JOIN abb01 ON abb01id = eaa01central " +
                    "INNER JOIN abe01 ON abe01id = abb01ent " +
                    "LEFT JOIN abe0103 ON abe0103ent = abe01id "+
                    "LEFT JOIN aba3001 ON aba3001id = abe0103criterio AND aba3001criterio = 18145240 "+
                    "INNER JOIN aah01 ON aah01id = abb01tipo " +
                    "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                    "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                    "INNER JOIN abm01 ON abm01id = eaa0103item " +
                    "LEFT JOIN abb10 ON abb10id = abd01operCod "+
                    "WHERE eaa01clasDoc = 1 " +
                    whereResumoOperacao +
                    whereClasDoc +
                    whereNumDocIni +
                    whereNumDocFin +
                    whereTipoDoc +
                    wherePcds +
                    whereDataEmissao +
                    whereDataEntradaSaida +
                    whereEntidades +
                    whereItens +
                    whereEmpresa +
                    whereMps +
                    whereCancData +
                    whereCriterio +
                    whereTipoOper +
                    groupBy +
                    orderBy

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin,parametroTipoDoc,parametroPcds,parametroDataEmissaoIni,
                parametroDataEmissaoFin, parametroDataEntradaSaidaIni, parametroDataEntradaSaidaFin, parametroEntidades, parametroItens,parametroEmpresa, parametroMps, parametroCriterio, parametroTipoOper)

    }

    private BigDecimal buscarValorTotal(Integer resumoOperacao,Integer numInicial,Integer numFinal, List<Long> idsTipos,List<Long> idsPcds,
                                        LocalDate[] dtEmissao,LocalDate[] dtEntradaSaida,List<Long> idsEntidades,List<Long> idsItens, String campoLivre, String campoFixo, List<Integer> mps, List<Long> criterios, List<Integer> tipoOperacao){


        String whereResumoOperacao = resumoOperacao == 0 ? "AND eaa01esMov = 0 " : "AND eaa01esMov = 1 ";
        String whereClasDoc = "AND eaa01clasDoc = 1 ";
        String whereNumDocIni = numInicial != null ? "AND abb01num >= :numInicial " : "";
        String whereNumDocFin = numFinal != null ? "AND abb01num <= :numFinal " : "";
        String whereTipoDoc = idsTipos != null && idsTipos.size() > 0 ? "AND aah01id IN (:idsTipos) " : "";
        String wherePcds = idsPcds != null && idsPcds.size() > 0 ? "AND abd01id in (:idsPcds) " : "";
        String whereDataEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "AND abb01data BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "AND abe01id IN (:idsEntidades) " : "";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :empresa ";
        String whereMps = !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";
        String whereCancData = "AND eaa01cancdata IS NULL ";
        String whereCriterio = criterios != null && criterios.size() > 0 ? "AND aba3001id IN (:criterios) " : "";
        String whereTipoOper = tipoOperacao != null && tipoOperacao.size() > 0 ? "AND abb10tipoCod IN (:tipoOperacao) " : "";



        Parametro parametroNumDocIni = numInicial != null ? Parametro.criar("numInicial", numInicial) : null;
        Parametro parametroNumDocFin = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
        Parametro parametroTipoDoc = idsTipos != null && idsTipos.size() > 0 ? Parametro.criar("idsTipos",idsTipos) : null;
        Parametro parametroPcds = idsPcds != null && idsPcds.size() > 0 ? Parametro.criar("idsPcds",idsPcds) : null;
        Parametro parametroDataEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni",dtEmissao[0]) : null;
        Parametro parametroDataEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin",dtEmissao[1]) : null;
        Parametro parametroDataEntradaSaidaIni = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaIni",dtEntradaSaida[0]) : null;
        Parametro parametroDataEntradaSaidaFin = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaFin",dtEntradaSaida[1]) : null;
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroMps = !mps.contains(-1) ? Parametro.criar("mps", mps) : null;
        Parametro parametroCriterio = criterios != null && criterios.size() > 0 ? Parametro.criar("criterios", criterios) : null;
        Parametro parametroTipoOper = tipoOperacao != null && tipoOperacao.size() > 0 ? Parametro.criar("tipoOperacao", tipoOperacao) : null;


        String whereCampoFiltro = campoLivre != null ? "SUM(CAST(eaa01json ->> '"+campoLivre+"'"+" as NUMERIC(18,2))) AS campo " : "SUM("+campoFixo+") AS campo "  ;

        String sql = "SELECT " +
                    whereCampoFiltro+
                    "FROM eaa01 " +
                    "INNER JOIN abb01 ON abb01id = eaa01central " +
                    "INNER JOIN abe01 ON abe01id = abb01ent " +
                    "LEFT JOIN abe0103 ON abe0103ent = abe01id "+
                    "LEFT JOIN aba3001 ON aba3001id = abe0103criterio AND aba3001criterio = 18145240 "+
                    "INNER JOIN aah01 ON aah01id = abb01tipo " +
                    "INNER JOIN abd01 ON abd01id = eaa01pcd " +
                    "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                    "INNER JOIN abm01 ON abm01id = eaa0103item " +
                    "LEFT JOIN abb10 ON abb10id = abd01operCod "+
                    "WHERE eaa01clasDoc = 1 " +
                    whereResumoOperacao +
                    whereClasDoc +
                    whereNumDocIni +
                    whereNumDocFin +
                    whereTipoDoc +
                    wherePcds +
                    whereDataEmissao +
                    whereDataEntradaSaida +
                    whereEntidades +
                    whereItens +
                    whereEmpresa +
                    whereMps +
                    whereCriterio +
                    whereCancData+
                    whereTipoOper;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroNumDocIni, parametroNumDocFin,parametroTipoDoc,parametroPcds,parametroDataEmissaoIni,
                parametroDataEmissaoFin, parametroDataEntradaSaidaIni, parametroDataEntradaSaidaFin, parametroEntidades, parametroItens, parametroEmpresa, parametroMps, parametroCriterio, parametroTipoOper)

    }

    private String buscarNomeCampoFixo(String campo){
        switch(campo) {
            case "eaa0103qtuso" :
                return  "Qtde. de Uso";
                break
            case "eaa0103qtcoml":
                return "Qtde. Comercial"
                break
            case "eaa0103unit":
                return "Preço Unitário"
                break
            case "eaa0103total":
                return "Total do Item"
                break
            case "eaa0103totdoc" :
                return  "Total Documento"
                break
            case "eaa0103totfinanc":
                return "Total Financeiro"
                break
            default:
                return null
                break
        }
    }


    public String buscarNomeCampoLivre(String campo) {
        def sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

    }
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIEN1cnZhIEFCQyBFbnRpZGFkZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIEN1cnZhIEFCQyBFbnRpZGFkZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=