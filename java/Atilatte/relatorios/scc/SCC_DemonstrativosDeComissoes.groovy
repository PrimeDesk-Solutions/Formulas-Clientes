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


public class SITE_DemonstrativoDeComissoes extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCC - Demonstrativo de Comissões";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
       
        Map<String, Object> filtrosDefault = new HashMap<>();
        LocalDate data = MDate.date()
       filtrosDefault.put("dataCalculo",data);
       filtrosDefault.put("ordem","0");
       filtrosDefault.put("impressao","0");
	  
        return Utils.map("filtros", filtrosDefault);;
    }
    @Override
    public DadosParaDownload executar() {
    	   String codRepIni = getString("representanteIni");
        String codRepFin = getString("representanteFin");
        Integer impressao = getInteger("impressao");
        Integer ordem = getInteger("ordem");
        LocalDate dtCalculo = getLocalDate("dataCalculo");
        
        List<TableMap> dados = buscarDocumentosComissao(codRepIni,codRepFin,ordem,dtCalculo);

        if(impressao == 1) gerarXLSX("SCC_DemonstrativoDasComissoes_Excel",dados);
        gerarPDF("SCC_DemonstrativoDasComissoes_PDF",dados);
    }
    private List<TableMap> buscarDocumentosComissao(String codRepIni, String codRepFin,Integer ordem, LocalDate dtCalculo){
       

        String orderBy = ordem == 0 ? "order by nota.abb01num " : "order by ent.abe01na ";


        Query sql = getSession().createQuery("select aah01codigo as codTipoDoc, aah01nome as descrTipoDoc, nota.abb01num as numDoc, financ.abb01parcela as parcela, financ.abb01quita as quita,nota.abb01data as dtEmissao, ent.abe01codigo as codEntidade, " +
                                                "ent.abe01na as naEntidade,daa01dtvctor as vencimento,calculo.abb01data as dtCalculo,dcb01dtcredito as credito,dcb01bc as bcComissao, dcb01taxa as taxa, dcb01valor as comissao, rep.abe01codigo as codRep, rep.abe01na as naRep, " +
                                                "to_char(dcd01dtcredi,'dd/mm/yyyy') as dtInicio, to_char(dcd01dtcredf,'dd/mm/yyyy') as dtFim\n" +
                                                "from dcb01 " +
                                                "inner join abb01 as nota on dcb01central = nota.abb01id " +
                                                "inner join aah01 on aah01id = abb01tipo " +
                                                "inner join abb0102 on abb0102central = nota.abb01id " +
                                                "inner join abb01 as financ on financ.abb01id = abb0102doc " +
                                                "inner join abe01 as ent on abe01id = nota.abb01ent " +
                                                "inner join daa01 on daa01central = financ.abb01id " +
                                                "inner join dcd01 on dcd01id = dcb01calculo " +
                                                "inner join abb01 as calculo on calculo.abb01id = dcd01central " +
                                                "inner join abe01 as rep on rep.abe01id = dcb01rep "+
                                                (codRepIni != null && codRepFin != null ? "where rep.abe01codigo between :codRepIni and :codRepFin " : "") +
                                                (dtCalculo != null ? "and calculo.abb01data = :dtCalculo " : "")+
                                                orderBy);
                                                
        if(codRepIni != null && codRepFin != null){
            sql.setParameter("codRepIni",codRepIni);
            sql.setParameter("codRepFin",codRepFin);
        }
        if(dtCalculo != null ){
            sql.setParameter("dtCalculo",dtCalculo);
        }

        List<TableMap> documentos =  sql.getListTableMap();
        return documentos;
    }
}
//meta-sis-eyJkZXNjciI6IlNDQyAtIERlbW9uc3RyYXRpdm9zIGRhcyBDb21pc3PDtWVzIiwidGlwbyI6InJlbGF0b3JpbyJ9