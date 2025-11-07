package Atilatte.relatorios.spp;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;
import java.time.LocalDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.ea.Eaa01
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.relatorio.TableMapDataSource

public class SPP_onde_usa_componentes extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPP - Lista 'Onde Usa' Componentes";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("impressao", "0")

        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        String codItemIni = getString("itemIni");
        String codItemFin = getString("itemFim");
        List<Integer> mps = getListInteger("mps");
        Integer impressao = getInteger("impressao");

        List<TableMap> dados = buscarDadosRelatorio(codItemIni,codItemFin, mps);
        params.put("titulo", "Lista 'Onde Usa' Componentes");
        params.put("empresa", obterEmpresaAtiva().aac10codigo + "-" + obterEmpresaAtiva().aac10na);

        if(impressao == 1) return gerarXLSX("SPP_onde_usa_componentes_EXCEL",dados)
        gerarPDF("SPP_onde_usa_componentes_PDF",dados)
    }

    private List<TableMap> buscarDadosRelatorio(String codItemIni,String codItemFin,List<Integer> mps){
        String whereItens = codItemIni != null && codItemFin != null ? "AND abm01componente.abm01codigo between :codItemIni and :codItemFin " : "";
        String whereMps = mps.size() > 0 && !mps.contains(-1) ? "AND abm01componente.abm01tipo in (:mps) " : "";
        Parametro parametroItemIni = codItemIni != null && codItemFin != null ? Parametro.criar("codItemIni",codItemIni) : null;
        Parametro parametroItemFin = codItemIni != null && codItemFin != null ? Parametro.criar("codItemFin",codItemFin) : null;
        Parametro parametroMps = mps.size() > 0 && !mps.contains(-1) ? Parametro.criar("mps",mps) : null;

        String sql = "SELECT abm01componente.abm01codigo AS codComponente, abm01componente.abm01na as naComponente, "+
                "CASE WHEN abm01componente.abm01tipo = 0 THEN 'M' WHEN abm01componente.abm01tipo = 1 then 'P' else 'S' END AS MpsComponente, "+
                "aam06componente.aam06codigo as umuComponente,abp20bomcodigo as formula,abp20011seq AS sequencia,abp20011qt AS quantidade, abp20011perda as perda, "+
                "abm01principal.abm01codigo AS codPrincipal, abm01principal.abm01na AS naPrincipal, aam06principal.aam06codigo as umuPrincipal, "+
                "CASE WHEN abm01principal.abm01tipo = 0 THEN 'M' WHEN abm01principal.abm01tipo = 1 then 'P' else 'S' END AS MpsPrincipal "+
                "FROM abp20  "+
                "INNER JOIN abp2001 ON abp2001comp = abp20id "+
                "INNER JOIN abp20011 ON abp20011proc = abp2001id "+
                "INNER JOIN abm01 AS abm01componente ON abm01componente.abm01id = abp20011item "+
                "INNER JOIN aam06 as aam06componente ON aam06componente.aam06id = abm01componente.abm01umu "+
                "INNER JOIN abm01 AS abm01principal ON abm01principal.abm01id = abp20item "+
                "INNER JOIN aam06 AS aam06principal ON aam06principal.aam06id = abm01principal.abm01umu " +
                "WHERE TRUE "+
                whereItens +
                whereMps +
                "ORDER BY MpsComponente, codComponente, MpsPrincipal, codPrincipal  "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroItemFin,parametroItemIni, parametroMps);

    }
}
//meta-sis-eyJkZXNjciI6IlNQUCAtIExpc3RhICdPbmRlIFVzYScgQ29tcG9uZW50ZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=