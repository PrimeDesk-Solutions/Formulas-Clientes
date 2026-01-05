/*
    Desenvolvido por: Roger Robert
 */
package MM.relatorios.scf;

import groovy.swing.table.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

public class SCF_AgrupamentoNatureza extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Naturezas Sem Agrupamentos";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        String naturezaIni = getString("natIni");
        String naturezaFin = getString("natFin");
        List<Long> idsEmpresas = getListLong("empresa");

        if(idsEmpresas == [] || idsEmpresas == null   ) idsEmpresas = [obterEmpresaAtiva().getAac10id()]

        List<TableMap> dados = buscarAgrupNatureza(naturezaIni, naturezaFin, idsEmpresas);

        return gerarXLSX("SCF_NaturezasSemAgrupamentos", dados);
    }

    private buscarAgrupNatureza(String naturezaIni, String naturezaFin, List<Long> idsEmpresas){

        String whereEmpresa = "where abf10gc in (:idEmpresa) ";
        String whereNatureza = naturezaIni != null && naturezaFin != null ? "and abf10codigo between :naturezaIni and :naturezaFin " : "";

        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idsEmpresas);
        Parametro parametroNaturezaIni = naturezaIni != null ? Parametro.criar("naturezaIni", naturezaIni) : null;
        Parametro parametroNaturezaFin = naturezaFin != null ? Parametro.criar("naturezaFin", naturezaFin) : null;

        String sql = "select abf10codigo as codNatureza, abf10nome as nomeNatureza, abf11codigo as codAgrup," +
                        "abf11nome as nomeAgroup, aac10codigo as codigoEmpresa, aac10na as nomeEmpresa " +
                        " from abf10 " +
                        "left join abf1101 on abf1101nat = abf10id " +
                        "left join abf11 on abf1101agrup = abf11id " +
                        "inner join aac10 on aac10id = abf10gc "+
                        whereEmpresa +
                        whereNatureza +
                        "order by aac10codigo, abf10codigo";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroNaturezaIni, parametroNaturezaFin);

    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIE5hdHVyZXphcyBTZW0gQWdydXBhbWVudG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9