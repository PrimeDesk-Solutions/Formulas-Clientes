package Atilatte.relatorios.scv

import groovy.swing.table.TableMap
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import br.com.multiorm.Query;
import java.time.LocalDate;
import br.com.multitec.utils.Utils;


public class SCV_ListarSI extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Listar SI ";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
    	
       Map<String, Object> filtrosDefault = new HashMap<>();
       filtrosDefault.put("numeroInicial","000001");
       filtrosDefault.put("numeroFinal","99999999");	
    	  return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<TableMap> dados = buscarDocumentosSI();
        adicionarParametro("empresa", obterEmpresaAtiva().aac10codigo +"-"+obterEmpresaAtiva().aac10na)
        return gerarPDF("SCV_ListarSI",dados)
    }

    private List<TableMap> buscarDocumentosSI(){
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal")
        LocalDate dtIni = getLocalDate("dataIni");
        LocalDate dtFin = getLocalDate("dataFin");
        LocalDate dtUtilIni = getLocalDate("dataUtilIni");
        LocalDate dtUtilFin = getLocalDate("dataUtilFin");
        boolean statCriado = getBoolean("statCriado");
        boolean statConcluido = getBoolean("statConcluido");
        List<Integer>mps = getListInteger("mpms");
        String codItemIni = getString("itemIni");
        String codItemFin = getString("itemFin");

        def status = [statCriado ? 0 : null,statConcluido ? 1: null ]



        Query sql = getSession().createQuery("select abb01num as numDoc, abb01data as dataDoc, abb11codigo as coddpto, abb11nome as descrDpto, " +
                                                "aab10user as user, bcd01datauso as dtUso, " +
                                                "case when abm01tipo = 0 then 'M' when abm01tipo = 1 then 'P' else 'S' end as MPS, "+
                                                "abm01codigo as codItem, abm01na as naItem, aam06codigo as UMU, "+
                                                "bcd0101qts as solicitado, bcd0101qte as entregue,bcd0101qtn as negada "+
                                                "from bcd01 " +
                                                "inner join abb01 on bcd01central = abb01id " +
                                                "left join abb11 on bcd01depto = abb11id " +
                                                "inner join aab10 on bcd01user = aab10id " +
                                                "inner join bcd0101 on bcd01id = bcd0101si " +
                                                "inner join abm01 on bcd0101item = abm01id " +
                                                "inner join aam06 on abm01umu = aam06id " +
                                                (numDocIni != null && numDocFin != null ? "where abb01num between :numDocIni and :numDocFin " : "") +
                                                (dtIni != null && dtFin != null ? "and abb01data between :dtIni and :dtFin " : "") +
                                                (dtUtilIni != null && dtUtilFin != null ? "and bcd01datauso between :dtUtilIni and :dtUtilFin " : "") +
                                                (statCriado || statEmProcesso || statConcluido ? "and bcd01status in (:status) " : "") +
                                                (codItemIni != null && codItemFin != null ? "and abm01codigo between :codItemIni and :codItemFin " : "") +
                                                (!mps.contains(-1) ? "and abm01tipo in (:mps) " : "") +
                                                "ORDER BY abb01num ");

            if(numDocIni != null & numDocFin != null){
                sql.setParameter("numDocIni",numDocIni);
                sql.setParameter("numDocFin",numDocFin);
            }

            if(dtIni != null && dtFin != null){
                sql.setParameter("dtIni",dtIni);
                sql.setParameter("dtFin",dtFin);
            }

            if(dtUtilIni != null && dtUtilFin != null){
                sql.setParameter("dtUtilIni",dtUtilIni);
                sql.setParameter("dtUtilFin",dtUtilFin);
            }

            if(statCriado || statEmProcesso || statConcluido ){
                sql.setParameter("status",status);
            }

            if(codItemIni != null && codItemFin != null){
                sql.setParameter("codItemIni",codItemIni);
                sql.setParameter("codItemFin",codItemFin);
            }

            if(!mps.contains(-1)){
                sql.setParameter("mps",mps);
            }


            List<TableMap> documentos = sql.getListTableMap()

    }



}
//meta-sis-eyJkZXNjciI6IlNDViAtIExpc3RhciBTSSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==