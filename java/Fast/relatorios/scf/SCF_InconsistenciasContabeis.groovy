package Fast.relatorios.scf;

import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table
import org.apache.tomcat.jni.Local;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_InconsistenciasContabeis extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Inconsistências Contábeis";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("visualizacao", "0");

        
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        LocalDate[] periodo = getIntervaloDatas("periodo");
        List<Long> idsContasCorrentes = getListLong("contaCorrente");
        Integer visualizacao = getInteger("visualizacao");
        Integer impressao = getInteger("impressao");

        List<TableMap> dados = buscarDadosRelaorio(periodo, idsContasCorrentes, visualizacao);

        return gerarXLSX("SCF_InconsistenciasContabeis", dados)

    }

    private buscarDadosRelaorio(LocalDate[] periodo, List<Long> idsContasCorrentes, Integer visualizacao ){

        // Periodo Inicial e Final
        LocalDate periodoIni = null;
        LocalDate periodoFin = null;

        if(periodo != null){
            periodoIni = periodo[0];
            periodoFin = periodo[1];
        }

        String whereEmpresa = "where dab10eg = :idEmpresa ";
        String wherePeriodo = periodo != null ? "and dab10data between :periodoIni  and :periodoFin " : "";
        String whereCC = idsContasCorrentes != null && idsContasCorrentes.size() > 0 ? "and dab01id in (:idsContasCorrentes) " : "";

        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroPeriodoIni = periodo != null ? Parametro.criar("periodoIni", periodoIni ) : null;
        Parametro parametroPeriodoFin = periodo != null ? Parametro.criar("periodoFin", periodoFin ) : null;
        Parametro parametroContasCorrente = idsContasCorrentes != null && idsContasCorrentes.size() > 0 ? Parametro.criar("idsContasCorrentes", idsContasCorrentes) : null;



        String sql = "select dab10data as dtFinanc, abb01num as numDocFinanc, abb01parcela, abf10codigo as codNatFinan, dab01codigo as codCCFinanc, "+
                    "abc10financ.abc10codigo as codContaContabilFinanc, abc10deb.abc10codigo as contaDebito, abc10cred.abc10codigo as contaCredito, "+
                    "case when ebb05id is null then 'Não Localizado' else 'Localizado' end as idContabil, dab10id, ebb05data as dtContabil "+
                    "from dab10 "+
                    "inner join abb01 on dab10central = abb01id "+
                    "left join dab1001 on dab1001lct = dab10id "+
                    "left join dab10011 on dab10011depto = dab1001id "+
                    "left join abf10 on dab10011nat = abf10id "+
                    "left join abf1001 on abf1001nat = abf10id  "+
                    "left join abc10 as abc10financ on abc10financ.abc10id = abf1001cta "+
                    "left join dab1002 on dab1002lct = dab10id "+
                    "left join dab01 on dab01id = dab1002cc "+
                    "left join ebb05 on ebb05central = abb01id "+
                    "left join abc10 as abc10deb on ebb05deb = abc10deb.abc10id "+
                    "left join abc10 as abc10cred on ebb05cred = abc10cred.abc10id "+
                    whereEmpresa +
                    wherePeriodo +
                    whereCC +
                    "order by dab10data, abb01num"

                    

        List<TableMap> tmRegistros = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroPeriodoIni, parametroPeriodoFin, parametroContasCorrente );
        List<TableMap> tmGeral = new ArrayList<>();

        for(registro in tmRegistros){
            if(registro.getString("codContaContabilFinanc") == registro.getString("contaDebito") || registro.getString("codContaContabilFinanc") == registro.getString("contaCredito") ){
                registro.put("status", "Correto" );
            }else{
                registro.put("status", "Verificar");
            }

            if(registro.getDate("dtFinanc") == registro.getDate("dtContabil")){
                registro.put("status", "Correto");
            }else{
                registro.put("status", "Verificar");
            }

            if(visualizacao == 0){
                if(registro.getString("status") == "Correto" ){
                    tmGeral.add(registro)
                }
            }else if(visualizacao == 1){
                if(registro.getString("status") == "Verificar" ){
                    tmGeral.add(registro)
                }
            }else{
                tmGeral.add(registro)
            }
        }

        return  tmGeral;
        

    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEluY29uc2lzdMOqbmNpYXMgQ29udMOhYmVpcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==