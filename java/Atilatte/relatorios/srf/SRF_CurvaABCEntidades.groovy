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

public class SRF_CurvaABCEntidades extends RelatorioBase {
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
            interromper("Preencha apenas 1 campo para prosseguir.")
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


        String whereResumoOperacao = resumoOperacao == 0 ? "and eaa01esMov = 0 " : "and eaa01esMov = 1 ";
        String whereClasDoc = "and eaa01clasDoc = 1 ";
        String whereNumDocIni = numInicial != null ? "and abb01num >= :numInicial " : "";
        String whereNumDocFin = numFinal != null ? "and abb01num <= :numFinal " : "";
        String whereTipoDoc = idsTipos != null && idsTipos.size() > 0 ? "and aah01id in (:idsTipos) " : "";
        String wherePcds = idsPcds != null && idsPcds.size() > 0 ? "and abd01id in (:idsPcds) " : "";
        String whereDataEmissao = dtEmissao != null ? "and abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "and abb01data between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "and eaa01gc = :empresa "
        String whereMps = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
        String whereCancData = "and eaa01cancdata is null ";
        String whereCriterio = criterios != null && criterios.size() > 0 ? "and aba3001id in (:criterios) " : "";
        String whereTipoOper = tipoOperacao != null && tipoOperacao.size() > 0 ? "and abb10tipoCod in (:tipoOperacao) " : "";

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
        String fields = visualizacao == 0 ? "aba3001descr as rede, " : "abe01codigo, abe01na,aba3001descr as rede,";
        String groupBy = visualizacao == 0 ? "group by aba3001descr " : "group by abe01codigo, abe01na, aba3001descr ";

        String sql = "select row_number() over () as numLinha, "+ fields +whereCampoFiltro+
                " from eaa01\n" +
                "inner join abb01 on abb01id = eaa01central\n" +
                "inner join abe01 on abe01id = abb01ent\n" +
                "left join abe0103 on abe0103ent = abe01id "+
                "left join aba3001 on aba3001id = abe0103criterio and aba3001criterio = 18145240 "+
                "inner join aah01 on aah01id = abb01tipo\n" +
                "inner join abd01 on abd01id = eaa01pcd\n" +
                "inner join eaa0103 on eaa0103doc = eaa01id\n" +
                "inner join abm01 on abm01id = eaa0103item\n" +
                "left join abb10 on abb10id = abd01operCod "+
                "where eaa01clasDoc = 1\n" +
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


        String whereResumoOperacao = resumoOperacao == 0 ? "and eaa01esMov = 0 " : "and eaa01esMov = 1 ";
        String whereClasDoc = "and eaa01clasDoc = 1 ";
        String whereNumDocIni = numInicial != null ? "and abb01num >= :numInicial " : "";
        String whereNumDocFin = numFinal != null ? "and abb01num <= :numFinal " : "";
        String whereTipoDoc = idsTipos != null && idsTipos.size() > 0 ? "and aah01id in (:idsTipos) " : "";
        String wherePcds = idsPcds != null && idsPcds.size() > 0 ? "and abd01id in (:idsPcds) " : "";
        String whereDataEmissao = dtEmissao != null ? "and abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereDataEntradaSaida = dtEntradaSaida != null ? "and abb01data between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "and eaa01gc = :empresa ";
        String whereMps = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
        String whereCancData = "and eaa01cancdata is null ";
        String whereCriterio = criterios != null && criterios.size() > 0 ? "and aba3001id in (:criterios) " : "";
        String whereTipoOper = tipoOperacao != null && tipoOperacao.size() > 0 ? "and abb10tipoCod in (:tipoOperacao) " : "";



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

        String sql = "select " +whereCampoFiltro+
                " from eaa01\n" +
                "inner join abb01 on abb01id = eaa01central\n" +
                "inner join abe01 on abe01id = abb01ent\n" +
                "left join abe0103 on abe0103ent = abe01id "+
                "left join aba3001 on aba3001id = abe0103criterio and aba3001criterio = 18145240 "+
                "inner join aah01 on aah01id = abb01tipo\n" +
                "inner join abd01 on abd01id = eaa01pcd\n" +
                "inner join eaa0103 on eaa0103doc = eaa01id\n" +
                "inner join abm01 on abm01id = eaa0103item\n" +
                "left join abb10 on abb10id = abd01operCod "+
                "where eaa01clasDoc = 1\n" +
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