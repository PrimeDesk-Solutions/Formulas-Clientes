package Atilatte.relatorios.cgs

//import org.exolab.castor.mapping.xml.Param
import sam.server.samdev.utils.Parametro;

import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.time.LocalDate

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abm70
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class CGS_AcompanhamentoColetaEtiqueta extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CGS - Acompanhamento de Coleta de Etiqueta";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroEtiquetaInicial", "000000001");
        filtrosDefault.put("numeroEtiquetaFinal", "999999999");
        filtrosDefault.put("data", DateUtils.getStartAndEndMonth(MDate.date()));
        filtrosDefault.put("impressaoEtiqueta", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("numeroDocumentoInicial", "000000001");
        filtrosDefault.put("numeroDocumentoFinal", "999999999");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numeroEtiquetaInicial = getInteger("numeroEtiquetaInicial");
        Integer numeroEtiquetaFinal = getInteger("numeroEtiquetaFinal");
        LocalDate[] data = getIntervaloDatas("data");
        Integer impressaoEtiqueta = getInteger("impressaoEtiqueta");
        Long idTipoDocumento = getLong("tipoDocumento");
        Integer numeroDocumentoInicial = getInteger("numeroDocumentoInicial");
        Integer numeroDocumentoFinal = getInteger("numeroDocumentoFinal");
        List<Integer> mpm = getListInteger("mpm");
        String itemIni = getString("itemIni");
        String itemFim = getString("itemFim");
        Integer impressao = getInteger("impressao");

        List<TableMap> dados = buscarDadosRelatorio(numeroEtiquetaInicial,numeroEtiquetaFinal,data,impressaoEtiqueta,idTipoDocumento,
                                                        numeroDocumentoInicial,numeroDocumentoFinal,mpm,itemIni,itemFim);

        params.put("empresa", obterEmpresaAtiva().aac10codigo +"-"+ obterEmpresaAtiva().aac10na);
        params.put("titulo", "CGS - Relatório Etiquetas");

        if(impressao == 1) return gerarXLSX("CGS_RealtorioEtiquetas_Excel", dados);

        return gerarPDF("CGS_RelatorioEtiquetas_PDF", dados);


    }

    private List<TableMap> buscarDadosRelatorio(Integer numeroEtiquetaInicial,Integer numeroEtiquetaFinal,LocalDate[] data,Integer impressaoEtiqueta,Long idTipoDocumento,
                                                Integer numeroDocumentoInicial,Integer numeroDocumentoFinal,List<Integer> mpm,String itemIni, String itemFim){
        // Datas Inicial - Final
        LocalDate dtIni = null;
        LocalDate dtFin = null;
        if(data != null) {
            dtIni = data[0];
            dtFin = data[1];
        }

        String whereNumEtiqueta = "where abm70num between :numeroEtiquetaInicial and :numeroEtiquetaFinal ";
        String whereData = dtIni != null && dtFin != null ? "and abm70data between :dtIni and :dtFin " : "";
        String whereImpressao = "and abm70status = :impressaoEtiqueta "
        String whereIdTipoDoc = idTipoDocumento != null ? "and aah01id = :idTipoDocumento " : "";
        String whereNumDoc = "and doc.abb01num between :numeroDocumentoInicial and :numeroDocumentoFinal ";
        String whereMpm =   !mpm.contains(-1) ? "and abm01tipo in (:mpm) " : "";
        String whereItem = itemIni != null && itemFim != null ? "and abm01codigo between :itemIni and :itemFim " : itemIni != null && itemFim == null ? "and abm01codigo >= :itemIni " : itemIni == null && itemFim != null ? "and abm01codigo <= :itemFim " : "";

        Parametro parametroNumEtiquetaIni = Parametro.criar("numeroEtiquetaInicial",numeroEtiquetaInicial);
        Parametro parametroNumEtiquetaFin = Parametro.criar("numeroEtiquetaFinal",numeroEtiquetaFinal);
        Parametro parametroDataIni = Parametro.criar("dtIni", dtIni);
        Parametro parametroDataFin = Parametro.criar("dtFin", dtFin);
        Parametro parametroImpressao = Parametro.criar("impressaoEtiqueta",impressaoEtiqueta);
        Parametro parametroTipoDoc = idTipoDocumento != null ? Parametro.criar("idTipoDocumento",idTipoDocumento) : null;
        Parametro parametroNumDocIni = Parametro.criar("numeroDocumentoInicial",numeroDocumentoInicial);
        Parametro parametroNumDocFin = Parametro.criar("numeroDocumentoFinal",numeroDocumentoFinal);
        Parametro parametroMpm = !mpm.contains(-1) ? Parametro.criar("mpm", mpm) : null;
        Parametro parametroItemIni = itemIni != null ? Parametro.criar("itemIni",itemIni) : null;
        Parametro parametroItemFin = itemFim != null ? Parametro.criar("itemFim",itemFim) : null;


        String sql =    "select abm70num, abm70dv, case when abm70status = 0 then '0-Não Impressa' else '1-Impressa' end as status, " +
                        "abm70data, abm70validade, abm70fabric, abm70qt, abm70lote, abm01codigo, abm01na, aam06codigo, doc.abb01num as numDocEtiqueta, aah01codigo as tipoDoc, " +
                        "abe01codigo, abe01na,romaneio.abb01num as numRomaneio, abb01pedido.abb01num as numPedido, abb01nota.abb01num as numNota " +
                        "from abm70 " +
                        "inner join abm01 on abm01id = abm70item " +
                        "inner join aam06 on aam06id = abm01umu " +
                        "left join abb01 as doc on doc.abb01id = abm70central " +
                        "left join  aah01 on aah01id = doc.abb01tipo " +
                        "left join abe01 on abe01id = abb01ent " +
                        "left join bfa01011 on bfa01011id = abm70idunidrom " +
                        "left join bfa0101 on  bfa01011item = bfa0101id " +
                        "left join bfa01 on bfa01id = bfa0101rom " +
                        "left join abb01 as romaneio on romaneio.abb01id = bfa01central " +
                        "left join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = bfa0101item " +
                        "left join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103doc " +
                        "left join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central " +
                        "left join eaa01032 on eaa01032itemscv = eaa0103pedido.eaa0103id " +
                        "left join eaa0103 as eaa0103nota on eaa0103nota.eaa0103id = eaa01032itemsrf " +
                        "left join eaa01 as eaa01nota on eaa01nota.eaa01id = eaa0103nota.eaa0103doc " +
                        "left join abb01 as abb01nota on abb01nota.abb01id = eaa01nota.eaa01central "+
                        whereNumEtiqueta+
                        whereData+
                        whereImpressao+
                        whereIdTipoDoc+
                        whereNumDoc+
                        whereMpm+
                        whereItem+
                        "order by abm70num "

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumEtiquetaIni, parametroNumEtiquetaFin, parametroDataIni, parametroDataFin, parametroImpressao, parametroTipoDoc, parametroNumDocIni, parametroNumDocFin, parametroMpm, parametroItemIni, parametroItemFin)
    }
}
//meta-sis-eyJkZXNjciI6IkNHUyAtIEFjb21wYW5oYW1lbnRvIGRlIENvbGV0YSBkZSBFdGlxdWV0YSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==