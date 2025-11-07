package Atilatte.relatorios.cgs

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro;

import java.util.Map;
import java.util.HashMap;

public class CGS_Agrupamentos_Natureza extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CGS - Agrupamentos de Naturezas";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("impressao","0")
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        String codNatIni = getString("natIni");
        String codNatFin = getString("natFin");
        String agroupIni = getString("agroupIni");
        String agroupFin = getString("agroupFin");
        Integer impressao = getInteger("impressao");
        Aac10 aac10 = obterEmpresaAtiva();
        Long idEmpresa = aac10.aac10id;

        List<TableMap> dados = buscarDadosRelatorio(codNatIni, codNatFin, agroupIni, agroupFin, idEmpresa);

        params.put("titulo", "CGS - Agrupamentos de Naturezas");
        params.put("empresa", aac10.aac10codigo + " - " + aac10.aac10na);

        if(impressao == 1 ) return gerarXLSX("CGS_Agrupamentos_Natureza_Excel", dados);

        return gerarPDF("CGS_Agrupamentos_Natureza_PDF", dados);
    }

    private List<TableMap> buscarDadosRelatorio(String codNatIni, String codNatFin, String agroupIni, String agroupFin, Long idEmpresa){
        String whereNat = codNatIni != null && codNatFin != null ?  "AND abf10codigo BETWEEN :codNatIni  AND :codNatFin " :
                             codNatIni != null && codNatFin == null ? "AND abf10codigo >= :codNatIni " :
                             codNatIni == null && codNatFin != null ? "AND abf10codigo <= :codNatFin " : "";

        String whereAgrup = agroupIni != null && agroupFin != null ?  "AND abf11codigo BETWEEN :agroupIni  AND :agroupFin " :
                            agroupIni != null && agroupFin == null ? "AND abf11codigo >= :agroupIni " :
                            agroupIni == null && agroupFin != null ? "AND abf11codigo <= :agroupFin " : "";
        String whereEmpresa = "AND abf10gc = :idEmpresa ";

        Parametro parametroNatIni = codNatIni != null ? Parametro.criar("codNatIni", codNatIni) : null;
        Parametro parametroNatFin = codNatFin != null ? Parametro.criar("codNatFin", codNatFin) : null;
        Parametro parametroAgrupIni = agroupIni != null ? Parametro.criar("agroupIni", agroupIni) : null;
        Parametro parametroAgrupFin = agroupFin != null ? Parametro.criar("agroupFin", agroupFin) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);

        String sql = "SELECT abf10codigo AS codNatureza, abf10nome AS nomeNatureza, abf11codigo as codGrupo, abf11nome as nomeGrupo " +
                        "FROM abf10 " +
                        "LEFT JOIN abf1101 ON abf1101nat = abf10id " +
                        "LEFT JOIN abf11 ON abf11id = abf1101agrup " +
                        "WHERE TRUE " +
                        whereNat +
                        whereAgrup +
                        whereEmpresa +
                        "ORDER BY abf10codigo"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNatIni, parametroNatFin, parametroAgrupIni, parametroAgrupFin, parametroEmpresa)
    }
}
//meta-sis-eyJkZXNjciI6IkNHUyAtIEFncnVwYW1lbnRvcyBkZSBOYXR1cmV6YXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=