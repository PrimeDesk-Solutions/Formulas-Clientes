package During.relatorios.cst

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class CST_EtiquetaPadrao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Etiqueta Padrão";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> ids = getListLong("abm70ids");

        Integer numeroEtiquetaInicial = getInteger("numeroEtiquetaInicial");
        Integer numeroEtiquetaFinal = getInteger("numeroEtiquetaFinal");
        LocalDate[] data = getIntervaloDatas("data");
        Integer imprimir = getInteger("imprimir");
        Long idTipoDocumento = getLong("tipoDocumento");
        Integer numeroDocumentoInicial = getInteger("numeroDocumentoInicial");
        Integer numeroDocumentoFinal = getInteger("numeroDocumentoFinal");
        List<Integer> mpm = getListInteger("mpm");
        String itemIni = getString("itemIni");
        String itemFim = getString("itemFim");

        List<TableMap> tmEtiquetas = new ArrayList();

        if(ids == null || ids.size() == 0){
            tmEtiquetas = buscarEtiquetasRelatorio(numeroEtiquetaInicial,numeroEtiquetaFinal, data, imprimir, idTipoDocumento, numeroDocumentoInicial, numeroDocumentoFinal, mpm, itemIni, itemFim)
        }else{
            tmEtiquetas = buscarEtiquetasID(ids);
        }

        for(etiqueta in tmEtiquetas){
            etiqueta.put("codBarSam", etiqueta.getString("abm70num") + "-" + etiqueta.getString("abm70dv"))
        }

        return gerarPDF("CST-Etiqueta_Embalagem_Final", tmEtiquetas);
    }

    private List<TableMap> buscarEtiquetasRelatorio(numeroEtiquetaInicial,numeroEtiquetaFinal, data, imprimir, idTipoDocumento, numeroDocumentoInicial, numeroDocumentoFinal, mpm, itemIni, itemFim){

        LocalDate dtIni = null;
        LocalDate dtFin = null;
        if(data != null) {
            dtIni = data[0];
            dtFin = data[1];
        }

        String whereNumEtiqueta = numeroEtiquetaInicial != null &&  numeroEtiquetaFinal != null ?  "and abm70num between :numeroEtiquetaInicial and :numeroEtiquetaFinal " : numeroEtiquetaInicial != null &&  numeroEtiquetaFinal == null ? "and abm70num >= :numeroEtiquetaInicial " : numeroEtiquetaInicial == null &&  numeroEtiquetaFinal != null ? "abm70num <= :numeroEtiquetaFinal " : "";
        String whereItem = itemIni != null &&  itemFim != null ?  "and abm01codigo between :itemIni and :itemFim " : itemIni != null &&  itemFim == null ? "and abm01codigo >= :itemIni " : itemIni == null && itemFim != null ? "abm01codigo <= :itemFim " : "";
        String whereData = dtIni != null && dtFin != null ?  "and abm70data between :dtIni and :dtFin " : "";
        String whereImpressao = "where abm70status = :imprimir ";
        String whereTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? "and aah01id in (:idTipoDocumento) " : "";
        String whereNumDoc = numeroDocumentoInicial != null &&  numeroDocumentoFinal != null ?  "and abb01num between :numeroDocumentoInicial and :numeroDocumentoFinal " : numeroDocumentoInicial != null &&  numeroDocumentoFinal == null ? "and abb01num >= :numeroDocumentoInicial " : numeroDocumentoInicial == null &&  numeroDocumentoFinal != null ? "abb01num <= :numeroDocumentoFinal " : "";
        String whereMpm = !mpm.contains(-1) ? "and abm01tipo in (:mpm) " : "";

        Parametro parametroNumEtiquetaIni = numeroEtiquetaInicial != null ? Parametro.criar("numeroEtiquetaInicial", numeroEtiquetaInicial) : null;
        Parametro parametroNumEtiquetaFin = numeroEtiquetaFinal != null ? Parametro.criar("numeroEtiquetaFinal", numeroEtiquetaFinal) : null;
        Parametro parametroItemIni = itemIni != null ? Parametro.criar("itemIni", itemIni) : null;
        Parametro parametroItemFin = itemFim != null ? Parametro.criar("itemFim", itemFim) : null;
        Parametro parametroDataIni = dtIni != null ? Parametro.criar("dtIni", dtIni) : null;
        Parametro parametroDataFin = dtFin != null ? Parametro.criar("dtFin", dtFin) : null;
        Parametro parametroImpressao = Parametro.criar("imprimir", imprimir);
        Parametro parametroMpm = Parametro.criar("mpm", mpm);
        Parametro parametroTipoDoc = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null;
        Parametro parametroNumDocIni = numeroDocumentoInicial != null ? Parametro.criar("numeroDocumentoInicial", numeroDocumentoInicial) : null;
        Parametro parametroNumDocFin = numeroDocumentoFinal != null ? Parametro.criar("numeroDocumentoFinal", numeroDocumentoFinal) : null;



        String sql = "select abm01codigo as coditem, abm01na as descrItem, abm70lote as lote, abm70num, abm70dv, abm70qt " +
                        "from abm70 " +
                        "inner join abm01 on abm01id = abm70item " +
                        "left join abb01 on abb01id = abm70central " +
                        "left join aah01 on aah01id = abb01tipo " +
                        whereImpressao +
                        whereNumEtiqueta +
                        whereItem +
                        whereData +
                        whereTipoDoc +
                        whereNumDoc +
                        whereMpm;
        
        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumEtiquetaIni, parametroNumEtiquetaFin, parametroItemIni, parametroItemFin, parametroDataIni, parametroDataFin, parametroImpressao, parametroTipoDoc, parametroNumDocIni, parametroNumDocFin,parametroMpm);
    }

    private  buscarEtiquetasID(List<Long> ids){
 
        String sql = "select abm01codigo as codItem, abm01descr as descrItem, cast(abm70json ->> 'ref_cliente' as text) as refCliente, abm70qt as qtd, "+
					"cast(abm70json ->> 'nr_pedido' as text) as numPed, cast(abm70json ->> 'nr_requisicao' as text) as nrRequisicao, abe01codigo as codCliente, abe01na as nomeCliente "+
					"from abm70 "+
					"inner join abm01 on abm01id = abm70item "+
					"left join abb01 on abb01id = abm70central "+
					"left join abe01 on abe01id = abb01ent "+
                		"where abm70id in (:ids)";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("ids", ids));
    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIEVtYmFsYWdlbSBGaW5hbCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==