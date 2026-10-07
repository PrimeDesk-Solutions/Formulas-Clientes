package Inova.relatorios.scv

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCV_Pedidos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Pedidos";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("pedEntSai", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("atendimento", true);
        filtrosDefault.put("atendimento2", true);
        filtrosDefault.put("liquido", true)
        filtrosDefault.put("agrupamento", "0");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> tipos = getListLong("tipos");
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> entidades = getListLong("entidades");
        LocalDate[] emissao = getIntervaloDatas("emissao");
        Integer pedEntSai = getInteger("pedEntSai");
        Integer impressao = getInteger("impressao")
        LocalDate[] entrega = getIntervaloDatas("entrega")
        def atendimento = [get("atendimento") ? 0 : null, get("atendimento2") ? 1 : null, get("atendimento3") ? 2 : null]
        atendimento.removeAll(Collections.singleton(null))
        String numeroCliente = getString("numeroCliente");
        Integer agrupamento = getInteger("agrupamento");
        List<Long> idsPcd = getListLong("pcds");
        List<Long> redespacho = getListLong("redespacho");
        String campoLivre1 = getString("campoLivre1");
        String campoLivre2 = getString("campoLivre2");
        String campoLivre3 = getString("campoLivre3");
        String campoLivre4 = getString("campoLivre4");
        Boolean total1 = getBoolean("total1");
        Boolean total2 = getBoolean("total2");
        Boolean total3 = getBoolean("total3");
        Boolean total4 = getBoolean("total4");


        Map<String, String> campos = new HashMap()

        campos.put("1", campoLivre1 != null ? campoLivre1 : "" );
        campos.put("2", campoLivre2 != null ? campoLivre2 : "" );
        campos.put("3", campoLivre3 != null ? campoLivre3 : "" );
        campos.put("4", campoLivre4 != null ? campoLivre4 : "" );

        adicionarParametro("aac10rs", obterEmpresaAtiva().getAac10rs())
        adicionarParametro("titulo", "SCV - Pedidos")
        adicionarParametro("total1", total1);
        adicionarParametro("total2", total2);
        adicionarParametro("total3", total3);
        adicionarParametro("total4", total4);

        List<TableMap> dados = buscarDocumentos(tipos, numeroInicial, numeroFinal, entidades, pedEntSai, emissao, entrega, atendimento, numeroCliente, redespacho,idsPcd, agrupamento, campoLivre1, campoLivre2, campoLivre3, campoLivre4);

        for(dado in dados){
            comporCamposLivres(dado, campos);
        }

        if(impressao == 1) return gerarXLSX("SCV_Pedidos_Excel", dados);

        if(impressao == 0 && agrupamento == 0) {
            return gerarPDF("SCV_Pedidos_Agrup_Data_Entrega_PDF", dados)
        } else {
            return gerarPDF("SCV_Pedidos_Agrup_Documento_PDF", dados)
        }

    }
    private List<TableMap> buscarDocumentos(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, List<Long> entidades, Integer pedEntSai, LocalDate[] emissao, LocalDate[] entrega, List<Integer> atendimento, String numeroCliente, List<Long> redespacho,List<Long>idsPcd, Integer agrupamento, String campoLivre1,String campoLivre2,String campoLivre3,String campoLivre4) {
        String whereTipos = tipos != null && tipos.size() > 0 ? " AND abb01tipo IN (:tipos) " : ""
        String whereEntidades = entidades != null && entidades.size() > 0 ? " AND abb01ent IN (:entidades) " : ""
        String whereCompVenda = pedEntSai == 0 ? " AND eaa01esmov = 0 " : " AND eaa01esmov = 1 "
        String whereEmissao = emissao != null && emissao.size() > 0 ? " AND abb01data BETWEEN :dataIni AND :dataFim " : ""
        String whereNumIni = " AND abb01num >= :numIni "
        String whereNumFim = " AND abb01num <= :numFim "
        String whereEntrega = entrega != null && entrega.size() > 0 ? " AND eaa0103pedido.eaa0103dtEntrega BETWEEN :dtIni AND :dtFim " : ""
        String whereAtendimento  = atendimento != null && atendimento.size() > 0 ? " AND eaa01scvAtend IN (:atendimento) " : ""
        String whereNumCliente = numeroCliente != null && numeroCliente.length() > 0 ? " AND eaa0103pcnum = :numCli ": "";
        String whereRedespacho = redespacho != null && redespacho.size() > 0 ? " AND redespacho.abe01id IN (:redespacho) ": "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id IN (:idsPcd) " : "";
        String orderBy = agrupamento == 0 ? "ORDER BY eaa0103dtEntrega, abb01num " : "ORDER BY abb01num, eaa0103dtEntrega";
        String campo1 = campoLivre1 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre1+"'"+" as NUMERIC(18,2)) AS " + campoLivre1 + ",  " : "";
        String campo2 = campoLivre2 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre2+"'"+" as NUMERIC(18,2)) AS " + campoLivre2 + ", " : "";
        String campo3 = campoLivre3 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre3+"'"+" as NUMERIC(18,2)) AS " + campoLivre3 + ", "  : "";
        String campo4 = campoLivre4 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre4+"'"+" as NUMERIC(18,2)) AS " + campoLivre4 + ", "  : "";

        String sql = "SELECT " + campo1 + campo2 + campo3 + campo4 +
                "eaa01id, eaa0103nota.eaa0103id, abb01num,abb01serie, abb01data, redespacho.abe01codigo AS codRedespacho, redespacho.abe01na AS nomeResdepacho, " +
                "entidade.abe01codigo AS codEntidade, entidade.abe01na AS nomeEntidade, abm01codigo, abm01descr, " +
                "CASE WHEN abm01tipo = 0 THEN 'M' WHEN abm01tipo = 1 THEN 'P' WHEN abm01tipo = 2 THEN 'S' ELSE 'MER' END AS mps,  "+
                "eaa0103pedido.eaa0103dtentrega, eaa0103pedido.eaa0103pcnum, uso.aam06codigo AS aam06descr_uso, eaa0103pedido.eaa0103qtComl AS eaa0103qtcoml," +
                "eaa0103pedido.eaa0103unit, eaa0103pedido.eaa0103total, eaa0103pedido.eaa0103totdoc, eaa0102doc, aah01codigo, eaa0103pedido.eaa0103qtUso AS eaa0103qtUso " +
                "FROM eaa01 " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN aah01 ON aah01id = abb01tipo "+
                "INNER JOIN abd01 ON abd01id = eaa01pcd "+
                "INNER JOIN abe01 AS entidade ON entidade.abe01id = abb01ent " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "LEFT JOIN abe01 AS redespacho ON eaa0102redespacho = redespacho.abe01id " +
                "INNER JOIN eaa0103 AS eaa0103pedido ON eaa0103pedido.eaa0103doc = eaa01id " +
                "LEFT JOIN eaa01032 ON eaa01032itemscv = eaa0103id " +
                "LEFT JOIN eaa0103 AS eaa0103nota ON eaa0103nota.eaa0103id = eaa01032itemsrf " +
                "INNER JOIN abm01 ON abm01id = eaa0103pedido.eaa0103item " +
                "LEFT JOIN aam06 AS comercial ON comercial.aam06id = eaa0103pedido.eaa0103umcoml " +
                "LEFT JOIN aam06 AS uso ON uso.aam06id = eaa0103pedido.eaa0103umu " +
                " WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SCV + " "+
                " AND eaa01cancData IS NULL "+
                obterWherePadrao("eaa01","AND") +
                whereTipos + whereEntidades + whereCompVenda + whereEmissao +
                whereNumIni + whereNumFim + whereEntrega + whereAtendimento +
                whereNumCliente + whereRedespacho + wherePcd +
                orderBy;

        Parametro parametroTipos = tipos != null && tipos.size() > 0 ? criarParametroSql("tipos", tipos) : null
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? criarParametroSql("entidades", entidades) : null
        Parametro parametroEmissaoIni = emissao != null ? criarParametroSql("dataIni", emissao[0]) : null
        Parametro parametroEmissaoFin = emissao != null ? criarParametroSql("dataFim", emissao[1]) : null
        Parametro parametroNumIni = criarParametroSql("numIni",numeroInicial)
        Parametro parametroNumFin = criarParametroSql("numFim",numeroFinal)
        Parametro parametroDeEntregaIni = entrega != null ? criarParametroSql("dtIni", entrega[0]) : null;
        Parametro parametroDeEntregaFin = entrega != null ? criarParametroSql("dtFim", entrega[1]) : null;
        Parametro parametroAtendimentos = atendimento != null && atendimento.size() > 0 ? criarParametroSql("atendimento", atendimento) : null;
        Parametro parametroNumPedCli = numeroCliente != null && numeroCliente.length() > 0 ? criarParametroSql("numCli", numeroCliente) : null;
        Parametro parametroRedesp = redespacho != null && redespacho.size() > 0 ? criarParametroSql("redespacho", redespacho) : null;
        Parametro parametroPcds = idsPcd != null && idsPcd.size() > 0 ? criarParametroSql("idsPcd", idsPcd) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipos, parametroEntidades, parametroEmissaoIni, parametroEmissaoFin, parametroNumIni, parametroNumFin, parametroDeEntregaIni, parametroDeEntregaFin, parametroAtendimentos, parametroNumPedCli, parametroRedesp, parametroPcds);
    }

    private void comporCamposLivres(TableMap dado, Map<String, String> campos){
        for(campo in campos){
            if(campo.value != null){
                String nomeCampo = buscarNomeCampoLivre(campo.value);
                dado.put("nomeCampo" + campo.key, nomeCampo );
                dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value));
            }
        }
    }

    private String buscarNomeCampoLivre(String campo) {
        def sql = " select aah02descr from aah02 where aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=