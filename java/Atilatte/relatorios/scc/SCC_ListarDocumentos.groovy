package Atilatte.relatorios.scc

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import groovy.swing.table.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import br.com.multitec.utils.Utils

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;


public class SCC_ListarDocumentos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCC - Listar Documentos";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
       
        Map<String, Object> filtrosDefault = new HashMap<>();
        LocalDate data = MDate.date()
       filtrosDefault.put("ordem","0");
       filtrosDefault.put("calculo","0");
       filtrosDefault.put("detalhamento","0");
	  
        return Utils.map("filtros", filtrosDefault);;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsRep = getListLong("representante");
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dataVctoNominal = getIntervaloDatas("dataNominal");
        LocalDate[] dataPagamento = getIntervaloDatas("dataPagamento");
        LocalDate[] dataBaixa = getIntervaloDatas("dataBaixa");
        LocalDate[] dataCredito = getIntervaloDatas("dataCredito");
        LocalDate[] dataCalculo = getIntervaloDatas("dataCalculo");
        List<Long> tipoDocumento = getListLong("tipo");
        List<Long> entidades = getListLong("entidades");
        Integer optionCalculo = getInteger("calculo");
        Integer detalhamento = getInteger("detalhamento");
        Integer impressao = getInteger("impressao");
        Integer ordem = getInteger("ordem");
        
        List<TableMap> dados = buscarDocumentosComissao(idsRep,dataEmissao, dataVctoNominal, dataPagamento,dataBaixa, dataCredito, dataCalculo, tipoDocumento,entidades, optionCalculo, ordem,detalhamento);

        String titulo = "";
        String nomeRelatorio = "";
        
        if(detalhamento == 0 ){
        	titulo = "SCC - Documentos Comissão (Sintético)";
        	nomeRelatorio = "SCC_Documentos_Sintetico";
        }else{
        	titulo = "SCC - Documentos Comissão (Analítico)";
        	nomeRelatorio = "SCC_Documentos_Analitico";
        }

        Params.put("titulo",titulo);
        Params.put("empresa",obterEmpresaAtiva().aac10codigo +"-"+obterEmpresaAtiva().aac10na)

        return gerarPDF(nomeRelatorio,dados);
        
    }
    private List<TableMap> buscarDocumentosComissao(List<Long> idsRep, LocalDate[] dataEmissao,LocalDate[] dataVctoNominal,LocalDate[] dataPagamento,LocalDate[] dataBaixa,LocalDate[] dataCredito,LocalDate[] dataCalculo, List<Long> tipoDocumento,List<Long> entidades,Integer optionCalculo,Integer ordem,Integer detalhamento){
       

        String orderBy = ordem == 0 ? "order by nota.abb01num " : "order by ent.abe01na ";
        String whereCalculo = optionCalculo == 0  ? "and dcb01calculo is not null " : optionCalculo == 1 ? "and dcb01calculo is null " : "";
	

        Query sql = null;
        
        if(detalhamento == 0){
        	sql = getSession().createQuery("select rep.abe01codigo as codRep, rep.abe01na as naRep, SUM(eaa01totDoc) as valorDoc, SUM(dcb01bc) as bcComissao,SUM(dcb01valor) as comissao "+ 
        								   "from dcb01 " +
                                                "inner join abb01 as nota on dcb01central = nota.abb01id " +
                                                "inner join eaa01 on eaa01central = nota.abb01id "+ 
                                                "inner join aah01 on aah01id = nota.abb01tipo " +
                                                "inner join abb0102 on abb0102central = nota.abb01id " +
                                                "inner join abb01 as financ on financ.abb01id = abb0102doc " +
                                                "inner join abe01 as ent on abe01id = nota.abb01ent " +
                                                "inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
                                                "inner join aag0201 on aag0201id = abe0101municipio "+
                                                "inner join aag02 on aag02id = aag0201uf "+
                                                "inner join daa01 on daa01central = financ.abb01id " +
                                                "left join dcd01 on dcd01id = dcb01calculo " +
                                                "left join abb01 as calculo on calculo.abb01id = dcd01central " +
                                                "inner join abe01 as rep on rep.abe01id = dcb01rep "+
                                                (idsRep != null && idsRep.size() > 0  ? "where rep.abe01id in (:idsRep) " : "") +
                                                (dataEmissao != null ? "nota.abb01data between :dtEmissIni and :dtEmissFin" : "")+
                                                (dataVctoNominal != null ? "daa01dtvctor between :dtVencimentoIni and :dtVencimentoFin " : "")+
                                                (dataPagamento != null ? "calculo.abb01data between :dtPagamentoIni and :dtPagamentoFin " : "")+
                                                (dataBaixa != null ? "daa01dtbaixa between :dtBaixaIni and :dtBaixaFin " : "")+
                                                (dataCredito != null ? "dcb01dtcredito  between :dtCreditoIni and :dtCreditoFin " : "")+
                                                (tipoDocumento != null && tipoDocumento.size() > 0 ? "aah01id in (:tipoDocumento) " : "" )+
                                                (entidades != null && entidades.size() > 0 ? "ent.abe01id in (:entidades) " : "" )+
                                                whereCalculo+
                                                "group by rep.abe01codigo,rep.abe01na ");
        }else{
        	sql =  getSession().createQuery("select aah01codigo as codTipoDoc, aah01nome as descrTipoDoc, nota.abb01num as numDoc, financ.abb01parcela as parcela, financ.abb01quita as quita,nota.abb01data as dtEmissao, ent.abe01codigo as codEntidade, " +
                                                "ent.abe01na as naEntidade,daa01dtvctor as vencimento,calculo.abb01data as dtCalculo,dcb01dtcredito as credito,dcb01bc as bcComissao, dcb01taxa as taxa, dcb01valor as comissao, rep.abe01codigo as codRep, rep.abe01na as naRep, " +
                                                "to_char(dcd01dtcredi,'dd/mm/yyyy') as dtInicio, to_char(dcd01dtcredf,'dd/mm/yyyy') as dtFim, aag02uf as ufEntidade, aag0201nome as municipioEntidade, eaa01totDoc as valorDoc " +
                                                "from dcb01 " +
                                                "inner join abb01 as nota on dcb01central = nota.abb01id " +
                                                "inner join eaa01 on eaa01central = nota.abb01id "+ 
                                                "inner join aah01 on aah01id = nota.abb01tipo " +
                                                "inner join abb0102 on abb0102central = nota.abb01id " +
                                                "inner join abb01 as financ on financ.abb01id = abb0102doc " +
                                                "inner join abe01 as ent on abe01id = nota.abb01ent " +
                                                "inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
                                                "inner join aag0201 on aag0201id = abe0101municipio "+
                                                "inner join aag02 on aag02id = aag0201uf "+
                                                "inner join daa01 on daa01central = financ.abb01id " +
                                                "left join dcd01 on dcd01id = dcb01calculo " +
                                                "left join abb01 as calculo on calculo.abb01id = dcd01central " +
                                                "inner join abe01 as rep on rep.abe01id = dcb01rep "+
                                                (idsRep != null && idsRep.size() > 0  ? "where rep.abe01id in (:idsRep) " : "") +
                                                (dataEmissao != null ? "nota.abb01data between :dtEmissIni and :dtEmissFin" : "")+
                                                (dataVctoNominal != null ? "daa01dtvctor between :dtVencimentoIni and :dtVencimentoFin " : "")+
                                                (dataPagamento != null ? "calculo.abb01data between :dtPagamentoIni and :dtPagamentoFin " : "")+
                                                (dataBaixa != null ? "daa01dtbaixa between :dtBaixaIni and :dtBaixaFin " : "")+
                                                (dataCredito != null ? "dcb01dtcredito  between :dtCreditoIni and :dtCreditoFin " : "")+
                                                (tipoDocumento != null && tipoDocumento.size() > 0 ? "aah01id in (:tipoDocumento) " : "" )+
                                                (entidades != null && entidades.size() > 0 ? "ent.abe01id in (:entidades) " : "" )+
                                                whereCalculo +
                                                orderBy);
        }
       
                    
                                                
        if(idsRep && idsRep.size() > 0){
            sql.setParameter("idsRep",idsRep)
        }

        if(dataEmissao != null){
            sql.setParameter("dtEmissIni",dataEmissao[0])
            sql.setParameter("dtEmissFin",dataEmissao[1])
        }

        if(dataVctoNominal != null){
            sql.setParameter("dtVencimentoIni",dataVctoNominal[0])
            sql.setParameter("dtVencimentoFin",dataVctoNominal[1])
        }

        if(dataPagamento != null){
            sql.setParameter("dtPagamentoIni",dataPagamento[0])
            sql.setParameter("dtPagamentoFin",dataPagamento[1])
        }

        if(dataBaixa != null){
            sql.setParameter("dtBaixaIni",dataBaixa[0])
            sql.setParameter("dtBaixaFin",dataBaixa[1])
        }

        if(dataCredito != null){
            sql.setParameter("dtCreditoIni",dataCredito[0])
            sql.setParameter("dtCreditoFin",dataCredito[1])
        }

        if(dataCalculo != null && optionCalculo == 0){
            sql.setParameter("dtCalculoIni",dataCalculo[0])
            sql.setParameter("dtCalculoFin",dataCalculo[1])
        }

        if(tipoDocumento != null && tipoDocumento.size() > 0){
            sql.setParameter("tipoDocumento",tipoDocumento)
        }

         if(entidades != null && entidades.size() > 0){
            sql.setParameter("entidades",entidades)
        }

        List<TableMap> documentos =  sql.getListTableMap();
        return documentos;
    }
}
//meta-sis-eyJkZXNjciI6IlNDQyAtIExpc3RhciBEb2N1bWVudG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9