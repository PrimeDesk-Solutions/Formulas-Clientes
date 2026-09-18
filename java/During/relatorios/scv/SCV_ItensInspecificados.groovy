package During.relatorios.scv

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCV_ItensInspecificados extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Itens Inspecificado (ISO)";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()

        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");

        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFinal = getInteger("numeroFinal");
        LocalDate[] dtEntrega = getIntervaloDatas("dataEntrega");
        List<Integer> mps = getListInteger("mps");
        List<Long> itens = getListLong("itens");
        List<Long> entidades = getListLong("entidade");

        List<TableMap> dados = buscarDadosRelatorio(numDocIni, numDocFinal, dtEntrega, mps, itens, entidades);

        return gerarPDF("SCV_ItensInspecificados",dados)
    }

    private List<TableMap> buscarDadosRelatorio(Integer numDocIni, Integer numDocFinal, LocalDate[] dtEntrega, List<Integer> mps, List<Integer> itens, List<Long> entidades){
        String whereEsMov = "where eaa01esMov = 0 ";
        String whereClasDoc = "and eaa01clasDoc = 0 ";
        String whereInspecao = "and cast(abm0101json ->> 'inspecao' as integer ) = 1 ";
        String whereNumDoc = numDocIni != null && numDocFinal != null ? "and abb01num between :numDocIni and :numDocFinal " : "";
        String whereData = dtEntrega != null ? "and eaa0103dtentrega between :dataInicial and :dataFinal " : "";
        String whereMps = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
        String whereItens = itens != null && itens.size() > 0 ? "and abm01id in (:itens) " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "and ent.abe01id in (:entidades) " : "";

        Parametro parametroNumDocIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumDocFin = numDocFinal != null ? Parametro.criar("numDocFinal", numDocFinal) : null;
        Parametro parametroDataIni = dtEntrega != null ? Parametro.criar("dataInicial", dtEntrega[0]) : null;
        Parametro parametroDataFin = dtEntrega != null ? Parametro.criar("dataFinal", dtEntrega[1]) : null;
        Parametro parametroMps = !mps.contains(-1) ? Parametro.criar("mps", mps)  : null;
        Parametro parametroItens =  itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
        Parametro parametroEntidades =  entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;


        String sql = "select cast(abm0101json ->> 'inspecao' as integer ),abb01num as numDoc,ent.abe01codigo as codEntidade, ent.abe01na as naEntidade, desp.abe01codigo as CodDespacho, desp.abe01na as naDespacho,abm01na as naItem,\n" +
                "abm01codigo as codItem,eaa0103dtentrega as dtEntrega\n" +
                "from eaa01\n" +
                "inner join abb01 on abb01id = eaa01central\n" +
                "inner join abe01 as ent on ent.abe01id = abb01ent\n" +
                "left join eaa0102 on eaa0102doc = eaa01id\n" +
                "left join abe01 as desp on desp.abe01id = eaa0102despacho\n" +
                "inner join eaa0103 on eaa0103doc = eaa01id\n" +
                "inner join abm01 on abm01id = eaa0103item\n" +
                "inner join abm0101 on abm0101item = abm01id "+
                whereEsMov +
                whereClasDoc +
                whereInspecao +
                whereNumDoc  +
                whereData    +
                whereMps     +
                whereItens   +
                whereEntidades +
                "order by desp.abe01codigo, abb01num, eaa0103dtentrega "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroDataIni,parametroDataFin,parametroMps,parametroItens,parametroEntidades)
    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIEl0ZW5zIEluc3BlY2lmaWNhZG8gKElTTykiLCJ0aXBvIjoicmVsYXRvcmlvIn0=