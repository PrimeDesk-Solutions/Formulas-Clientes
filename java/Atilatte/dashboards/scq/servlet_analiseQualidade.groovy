package Atilatte.dashboards.scq;


import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity

import org.springframework.http.MediaType

import java.time.LocalDate
import org.apache.commons.text.StringSubstitutor
import org.springframework.http.ResponseEntity
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import sam.core.variaveis.MDate
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;


public class servlet_analiseQualidade extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "Atilatte - Análise de Qualidade";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
    }

    @Override
    public ResponseEntity<Object> executar() {
        UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.scq.recurso_analiseQualidade.html");


        // Busca os criterios de seleção das categoria de analise dos itens
        List<TableMap> criterios = buscarCriterios();

        // Busca todos os locais que tem quantidades no status 11.Em Analise
        List<TableMap> listLocal = buscarLocais();

        def qtd1 = new ArrayList();
        def qtd2 = new ArrayList();
        def qtd3 = new ArrayList();
        def qtd4 = new ArrayList();
        def qtd5 = new ArrayList();
        def qtd6 = new ArrayList();
        def qtd7 = new ArrayList();
        def qtd8 = new ArrayList();
        def qtd9 = new ArrayList();
        
        List<String> locais = new ArrayList();



        Integer controle = 0
        for(criterio in criterios){
            controle++

            Long idCriterio = criterio.getLong("idCriterio");

            for(local in listLocal){
                Long idLocal = local.getLong("idLocal");
                BigDecimal qtd = buscarQuantidadePorLocal(idCriterio, idLocal);

                if(controle == 1){
                    locais.add("'"+local.getString("local") +"'")
                    qtd1.add(qtd.round(2))
                }else if(controle == 2){
                    qtd2.add(qtd.round(2));
                }else if(controle == 3){
                    qtd3.add(qtd.round(2));
                }else if(controle == 4){
                    qtd4.add(qtd.round(2))
                }else if(controle == 5){
                    qtd5.add(qtd.round(2))
                }else if(controle == 6 ){
                    qtd6.add(qtd.round(2))
                }else if(controle == 7){
                    qtd7.add(qtd.round(2))
                }else if(controle == 8){
                    qtd8.add(qtd.round(2))
                }else{
                    qtd9.add(qtd.round(2))
                }
            }

        }

        StringSubstitutor sub = new StringSubstitutor(Utils.map(
                "locais", locais,
                              "qtd1", qtd1,
                              "qtd2", qtd2,
                              "qtd3", qtd3,
                              "qtd4", qtd4,
                              "qtd5", qtd5,
                              "qtd6", qtd6,
                              "qtd7", qtd7,
                              "qtd8", qtd8,
                              "qtd9", qtd9,
        ))

        def resolvedString = sub.replace(dto.getScript())

        dto.setScript(resolvedString)

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto);
    }

    private List<TableMap> buscarLocais(){
        String sql =   "select aam04codigo as status, abm15nome as local, abm15id as idLocal, SUM(bcc02qt) as soma " +
                        "from bcc02 " +
                        "inner join aam04 on aam04id = bcc02status " +
                        "inner join abm01 on abm01id = bcc02item " +
                        "inner join abm15 on abm15id = bcc02ctrl0 " +
                        "where bcc02status = 112004 " +
                        "group by aam04codigo, abm15nome, abm15id " +
                        "having SUM(bcc02qt) > 0"

        return getAcessoAoBanco().buscarListaDeTableMap(sql)
    }

    private List<TableMap> buscarCriterios(){
        String sql = "select aba3001descr as descricao, aba3001id as idCriterio " +
                        "from aba3001 " +
                        "where aba3001criterio = 39116359 " +
                        "order by aba3001descr "

        return getAcessoAoBanco().buscarListaDeTableMap(sql)
    }

    private BigDecimal buscarQuantidadePorLocal(Long idCriterio, Long idLocal ){


        String sql = "select COALESCE(SUM(bcc02qt),0.000) as quantidade " +
                        "from bcc02 " +
                        "inner join aam04 on aam04id = bcc02status " +
                        "inner join abm01 on abm01id = bcc02item " +
                        "inner join abm0102 on abm01id = abm0102item " +
                        "inner join aba3001 on aba3001id = abm0102criterio and aba3001criterio = 39116359 " +
                        "where bcc02status = 112004 " +
                        "and aba3001id = :idCriterio " +
                        "and bcc02ctrl0 = :idLocal ";

        return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idCriterio", idCriterio), Parametro.criar("idLocal", idLocal) )

    }

}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gQW7DoWxpc2UgZGUgUXVhbGlkYWRlIiwidGlwbyI6InNlcnZsZXQiLCJ3IjoxMiwiaCI6MTIsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9