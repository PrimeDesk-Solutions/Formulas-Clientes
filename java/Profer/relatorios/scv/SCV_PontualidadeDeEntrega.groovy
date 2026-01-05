package Profer.relatorios.scv

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;

public class SCV_PontualidadeDeEntrega extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Pontualidade de Entrega";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
      criarFiltros("pedido","0",
                    "numInicial",0001,
                    "numFinal",999999)
    }
    @Override
    public DadosParaDownload executar() {
        List<TableMap> dados = buscarDocumentos();
        Integer detalha = getInteger("detalha");

        if(detalha == 0){
            return gerarPDF("SCV_PontualidadeDeEntrega_Sintetico", dados);

        }else{
            return gerarPDF("SCV_PontualidadeDeEntrega_Analitico", dados);

        }
    }
    private buscarDocumentos(){
        Integer pedido = getInteger("pedido");
        List<Long> idsTipos = getListLong("idsTipo");
        LocalDate[] dtEmissao = getIntervaloDatas("dtEmissao");
        Integer numIni = getInteger("numInicial");
        Integer numFin = getInteger("numFinal");
        List<Integer> mps = getListInteger("mpms");
        List<Long> itens = getListLong("itens");
        LocalDate[] dtPrevista = getIntervaloDatas("dtEntregaPrevista");
        List<Long> representantes = getListLong("representantes");
        List<Long> pcd = getListLong("pcd");
        Integer entregaReal = getInteger("realEntrega");

        //Data Emissão
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;
        if(dtEmissao != null){
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        params.put("title","Pontualidade De Entrega - Vendas");
        params.put("empresa",obterEmpresaAtiva().aac10codigo +" - "+obterEmpresaAtiva().aac10na);
        params.put("periodoIni",dtEmissaoIni != null ? dtEmissaoIni.toDate() : null);
        params.put("periodoFin",dtEmissaoFin != null ? dtEmissaoFin.toDate() : null);

        //Data Prevista Entrega
        LocalDate dtPrevistaIni = null;
        LocalDate dtPrevistaFin = null;
        if(dtPrevista != null){
            dtPrevistaIni = dtPrevista[0];
            dtPrevistaFin = dtPrevista[1];
        }


        String queryDtEntrega  = ""
        if(entregaReal == 0 ){
            queryDtEntrega = "eaa01nota.eaa01esdata as dtEntrega ";
        }else{
            queryDtEntrega = "abb01nota.abb01data as dtEntrega ";
        }

        Query sql = getSession().createQuery("select abe01codigo as codEnt, abe01na as naEnt, abb01pedido.abb01num as numPed, " +
                "case when abm01tipo = 0 then 'M' " +
                "when abm01tipo = 1 then 'P' " +
                "when abm01tipo = 2 then 'MERC' " +
                "else 'S' end as MPS, abm01codigo as codItem, abm01na as naItem, cast(eaa0103pedido.eaa0103json ->> 'umv' as character varying(3)) as UMV, " +
                "cast(eaa0103pedido.eaa0103json ->> 'qt_convertida' as numeric(18,6)) as qtdPedido, abb01pedido.abb01data as dtEmissPed, " +
                "eaa0103pedido.eaa0103dtentrega as entregaPedido, abb01nota.abb01num as numNota, "+
                "cast(eaa0103nota.eaa0103json ->> 'qt_convertida' as numeric(18,6)) as entregue, " +
                queryDtEntrega +
                "from eaa01 eaa01pedido " +
                "inner join abd01 on abd01id = eaa01pedido.eaa01pcd " +
                "inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central " +
                "inner join aah01 on aah01id = abb01pedido.abb01tipo " +
                "inner join abe01 on abe01id = abb01pedido.abb01ent " +
                "inner join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103doc = eaa01pedido.eaa01id " +
                "inner join abm01 on abm01id = eaa0103pedido.eaa0103item " +
                "inner join eaa01032 on eaa01032itemscv = eaa0103pedido.eaa0103id " +
                "inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103id = eaa01032itemsrf " +
                "inner join eaa01 as eaa01nota on eaa01nota.eaa01id = eaa0103nota.eaa0103doc " +
                "inner join abb01 as abb01nota on abb01nota.abb01id = eaa01nota.eaa01central " +
                "where abd01aplic = 0 and abd01es = :pedido " +
                (idsTipos != null ? "and aah01id in (:idsTipos) " : "") +
                (dtEmissaoIni != null && dtEmissaoFin != null ? "and abb01pedido.abb01data between :dtEmissaoIni and :dtEmissaoFin " : "") +
                (numIni != null && numFin != null ? "and abb01pedido.abb01num between :numIni and :numFin " : "") +
                (itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "") +
                (mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "") +
                (dtPrevistaIni != null && dtPrevistaFin != null ? "and eaa0103pedido.eaa0103dtentrega between :dtPrevistaIni and :dtPrevistaFin " : "") +
                (representantes != null && representantes.size() > 0 ? "and (eaa01pedido.eaa01rep0 in (:representantes) or eaa01pedido.eaa01rep1 in (:representantes) or eaa01pedido.eaa01rep2 in (:representantes) or eaa01pedido.eaa01rep3 in (:representantes) or eaa01pedido.eaa01rep4 in (:representantes)) " : "") +
                (pcd != null ? "and abd01id in (:pcd) " : ""));

        if(pedido != null){
            sql.setParameter("pedido",pedido)
        }
        if(idsTipos != null){
            sql.setParameter("idsTipos",idsTipos);
        }

        if(dtEmissaoIni != null && dtEmissaoFin != null){
            sql.setParameter("dtEmissaoIni",dtEmissaoIni);
            sql.setParameter("dtEmissaoFin",dtEmissaoFin);
        }

        if(numIni != null && numFin != null){
            sql.setParameter("numIni",numIni);
            sql.setParameter("numFin",numFin);
        }

        if(itens != null){
            sql.setParameter("itens",itens);
        }

        if(mps != null){
            sql.setParameter("mps",mps);
        }

        if(dtPrevistaIni != null && dtPrevistaFin != null){
            sql.setParameter("dtPrevistaIni",dtPrevistaIni);
            sql.setParameter("dtPrevistaFin",dtPrevistaFin);
        }

        if(representantes != null){
            sql.setParameter("representantes",representantes);
        }

        if(pcd != null){
            sql.setParameter("pcd",pcd);
        }

        List<TableMap> tmDocumentos = sql.getListTableMap();
        List<TableMap> dadosRelatorio = new ArrayList<>();

        for(TableMap documento in tmDocumentos){
            //Formatador de data
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            //Define a data de emissão do pedido como texto
            String txtdtEmissao = documento.getDate("dtEmissPed").toString();

            //Define a data de emissão do pedido  como localdate para calcular diferença de dias
            LocalDate dtEmissaoPed = LocalDate.parse(txtdtEmissao, formato);

            //Define a data prevista de entrega do pedido como texto
            String txtdtPrevista = documento.getDate("entregaPedido").toString();

            //Define a data prevista de entrega do pedido  como localdate para calcular diferença de dias
            LocalDate dtPrevistaEntrega = LocalDate.parse(txtdtPrevista, formato);

            //Define a data de entrega do pedido como texto
            String txtDtEntrega = documento.getDate("dtEntrega").toString();

            //Define a data de emissão do pedido  como localdate para calcular diferença de dias
            LocalDate dtEntrega = LocalDate.parse(txtDtEntrega, formato);

            //Calcula a diferença de dias da data emissão e a data de entrega
            Long diferencaDiasEmissao = ChronoUnit.DAYS.between(dtEmissaoPed, dtEntrega);

            //Calcula a diferença de dias da data prevista de entrega e a data de entrega
            Long diferencaDiasPrevisto = ChronoUnit.DAYS.between(dtPrevistaEntrega, dtEntrega);

            documento.put("diasEmiss",diferencaDiasEmissao);

            documento.put("diasPrev",diferencaDiasPrevisto);

            dadosRelatorio.add(documento);

        }

        return dadosRelatorio;






    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBvbnR1YWxpZGFkZSBkZSBFbnRyZWdhIiwidGlwbyI6InJlbGF0b3JpbyJ9