package Atilatte.relatorios.sif

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import java.time.format.DateTimeFormatter;


import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;


import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe02
import sam.model.entities.ea.Eaa01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.aa.Aam06;
import br.com.multiorm.criteria.criterion.Criterions
import sam.server.samdev.formula.FormulaBase
import br.com.multiorm.Query
import br.com.multiorm.ColumnType;
import java.time.LocalDate
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ea.Eaa0103
import br.com.multitec.utils.ValidacaoException

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SRF_VendaDeItensPorEstado extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Vendas de Itens Por Estado (SIF)";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true)
        filtrosDefault.put("total2", true)
        filtrosDefault.put("total3", true)
        filtrosDefault.put("total4", true)
        filtrosDefault.put("total5", true)
        filtrosDefault.put("total6", true)
        filtrosDefault.put("devolucao", true)
        filtrosDefault.put("resumoOperacao", "1");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("agrupaCategoria", false)
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer entradaSaida = getInteger("resumoOperacao");
        LocalDate[] emissao = getIntervaloDatas("dataEmissao");
        List<Long> idsEstados = getListLong("estado");
        List<Long> idsItens = getListLong("itens");
        List<Long> idsCategorias = getListLong("categoria");
        Boolean chkDevolucao = getBoolean("chkDevolucao");
        Integer impressao = getInteger("impressao");
        Boolean agroupCategoria = getBoolean("agrupaCategoria");
        Boolean imprimirEmQuilo = getBoolean("imprimeQuilo");
        def empresa = obterEmpresaAtiva();
        def idEmpresa = empresa.getAac10id();

        String periodo = ""
        if(emissao != null) {
            periodo = "Período Emissão: " + emissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + emissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
        params.put("periodo",periodo);

        List<TableMap> dados = buscarDadosRelatorioGeral(entradaSaida,emissao, idsEstados, idsItens, idsCategorias,idEmpresa,agroupCategoria)

        if(dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<TableMap> dadosRelatorio = new ArrayList<>();
        List<Long> idsItensDoc = obterIdsItensDoc(entradaSaida,emissao, idsEstados, idsItens, idsCategorias,idEmpresa,agroupCategoria);
        List<TableMap> listDevolucoesGeral = new ArrayList<>();
        List<TableMap> listDevolucoesAjustado = new ArrayList<>();
        String idControle = null;
        TableMap dadosTmp = new TableMap();
        TableMap valoresTotais = new TableMap();

        String idControleDev = null;
        TableMap dadosTmpDev = new TableMap();
        TableMap valoresTotaisDev = new TableMap();

        // Agrupa as devoluções por item
        if(chkDevolucao){
            listDevolucoesGeral = obterDevolucao(idsItensDoc);
            if(listDevolucoesGeral != null && listDevolucoesGeral.size() > 0){
                for(devolucao in listDevolucoesGeral){
                    if(idControleDev == null){
                        dadosTmpDev.putAll(devolucao);
                        idControleDev = devolucao.getLong("eaa01033itemdoc");
                        somarValores(devolucao, valoresTotaisDev);
                    }else if(idControleDev == devolucao.getLong("eaa01033itemdoc")){
                        somarValores(devolucao, valoresTotaisDev);
                    }else{
                        TableMap tmpDev = new TableMap();
                        tmpDev.putAll(dadosTmpDev);
                        tmpDev.putAll(valoresTotaisDev);
                        listDevolucoesAjustado.add(tmpDev);

                        dadosTmpDev = new TableMap()
                        dadosTmpDev.putAll(devolucao);
                        valoresTotaisDev = new TableMap();
                        idControleDev = devolucao.getLong("eaa01033itemdoc");

                        somarValores(devolucao, valoresTotaisDev)
                    }
                }

                TableMap tmpDev = new TableMap();
                tmpDev.putAll(dadosTmpDev);
                tmpDev.putAll(valoresTotaisDev);
                if(!tmpDev.isEmpty()) listDevolucoesAjustado.add(tmpDev);
            }
        }

        for(dado in dados){
            Long idItem = dado.getLong("eaa0103id");
            BigDecimal fatorQuilo = dado.getBigDecimal("fatorQuilo");
            String codItem = dado.getString("codItem");
            String naItem = dado.getString("naItem");

            if(fatorQuilo == null) interromper("Necessário informar o fator para conversão de quilo no item " + codItem + " - " + naItem );

            if(chkDevolucao){
                for(devolucao in listDevolucoesAjustado){
                    Long idItemDev = devolucao.getLong("eaa01033itemdoc");
                    if(idItem == idItemDev){
                        comporDevolucoes(dado, devolucao)
                    }
                }
            }

            String grupoControle = agroupCategoria ? dado.getString("categoria") : dado.getLong("idItem").toString(); // Define se será agrupado por categoria ou item
            grupoControle = grupoControle + dado.getString("estado");


            if(imprimirEmQuilo) dado.put("eaa0103qtComl", dado.getBigDecimal_Zero("eaa0103QtComl") * fatorQuilo);

            if(idControle == null){
                dadosTmp.putAll(dado);
                idControle = grupoControle;
                somarValores(dado, valoresTotais);
            }else if(idControle == grupoControle){
                somarValores(dado, valoresTotais);
            }else{
                TableMap tmp = new TableMap();
                tmp.putAll(dadosTmp);
                tmp.putAll(valoresTotais);
                dadosRelatorio.add(tmp);

                dadosTmp = new TableMap();
                dadosTmp.putAll(dado);
                valoresTotais = new TableMap();
                idControle = grupoControle;
                somarValores(dado, valoresTotais)
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);

        dadosRelatorio.add(tmp);


        if(impressao == 0 && !agroupCategoria){
            params.put("titulo","SRF - Vendas de Itens Por Estado (SIF) Analítico");
            return gerarPDF("SRF_VendaDeItensPorEstadoAnaliticoSIF(PDF)", dadosRelatorio)
        }else if(impressao == 0 && agroupCategoria){
            params.put("titulo","SRF - Vendas de Itens Por Estado (SIF) Sintético");
            return gerarPDF("SRF_VendaDeItensPorEstadoSinteticoSIF(PDF)", dadosRelatorio)
        }else if(impressao == 1 && !agroupCategoria){
            return gerarXLSX("SRF_VendaDeItensPorEstadoAnaliticoSIF(Excel)", dadosRelatorio)
        }else{
            return gerarXLSX("SRF_VendaDeItensPorEstadoSinteticoSIF(Excel)", dadosRelatorio)
        }

    }

    private List<TableMap> buscarDadosRelatorioGeral(Integer entradaSaida,LocalDate[] emissao, List<Long> idsEstados, List<Long> idsItens, List<Long> idsCategorias,Long idEmpresa, Boolean agroupCategoria){

        // Datas Emissão
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;

        if(emissao != null){
            dtEmissaoIni = emissao[0];
            dtEmissaoFin = emissao[1];
        }

        String whereClasDoc = "WHERE eaa01clasdoc = 1 ";
        String whereCriterio = "AND aba3001criterio = 35610617 ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereEsMov = entradaSaida == 0 ? "AND eaa01esMov = 0 " : "AND eaa01esMov = 1 ";
        String whereEmissao = emissao != null ? "AND abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereCategoria = idsCategorias != null && idsCategorias.size() > 0 ?  "and aba3001id IN (:idsCategorias) " : ""
        String whereEstados = idsEstados != null && idsEstados.size() > 0 ?  "and aag02id IN (:idsEstados) " : ""
        String whereItens = idsItens != null && idsItens.size() > 0 ?  "and abm01id IN (:idsItens) " : ""
        String orderBy = !agroupCategoria ? "order by abm01codigo, aag02uf  " : "order by aba3001descr, aag02uf "

        Parametro paramEmissaoIni = emissao != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro paramEmissaoFin = emissao != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro paramCategoria = idsCategorias != null && idsCategorias.size() > 0 ? Parametro.criar("idsCategorias", idsCategorias) : null;
        Parametro paramEstados = idsEstados != null && idsEstados.size() > 0 ? Parametro.criar("idsEstados", idsEstados) : null;
        Parametro paramItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro paramEmpresa = Parametro.criar("idEmpresa", idEmpresa);

        String sql = "SELECT eaa0103id, abm01id AS idItem, abm01codigo AS codItem, abm01na AS naItem, CAST(abm0101json ->> 'fator_litro' AS numeric(18,6)) as fatorQuilo, " +
                "aba3001descr AS categoria, aag02uf AS estado, eaa0103qtComl " +
                "FROM eaa01 " +
                "INNER JOIN abd01 on eaa01pcd = abd01id "+
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "INNER JOIN abm0101 ON abm0101item = abm01id "+
                "LEFT JOIN abm0102 ON abm0102item = abm01id " +
                "INNER JOIN aba3001 ON aba3001id = abm0102criterio " +
                "INNER JOIN abe01 ON abe01id = abb01ent " +
                "INNER JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                "INNER JOIN aag0201 ON aag0201id = abe0101municipio " +
                "INNER JOIN aag02 ON aag02id = aag0201uf " +
                whereClasDoc  +
                whereCriterio +
                whereEmpresa +
                whereEsMov +
                whereEmissao +
                whereCategoria +
                whereEstados +
                whereItens +
                "and eaa01cancdata is null "+
                "and abd01isce = 1 " +
                "and eaa01isce = 1 " +
                "and abb01tipo in (69744) "+
                orderBy

        return getAcessoAoBanco().buscarListaDeTableMap(sql, paramEmissaoIni, paramEmissaoFin, paramCategoria, paramEstados, paramItens, paramEmpresa );
    }

    private List<Long> obterIdsItensDoc(Integer entradaSaida,LocalDate[] emissao, List<Long> idsEstados, List<Long> idsItens, List<Long> idsCategorias,Long idEmpresa, Boolean agroupCategoria){
        // Datas Emissão
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;

        if(emissao != null){
            dtEmissaoIni = emissao[0];
            dtEmissaoFin = emissao[1];
        }

        String whereClasDoc = "WHERE eaa01clasdoc = 1 ";
        String whereCriterio = "AND aba3001criterio = 35610617 ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereEsMov = entradaSaida == 0 ? "AND eaa01esMov = 0 " : "AND eaa01esMov = 1 ";
        String whereEmissao = emissao != null ? "AND abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereCategoria = idsCategorias != null && idsCategorias.size() > 0 ?  "and aba3001id IN (:idsCategorias) " : ""
        String whereEstados = idsEstados != null && idsEstados.size() > 0 ?  "and aag02id IN (:idsEstados) " : ""
        String whereItens = idsItens != null && idsItens.size() > 0 ?  "and abm01id IN (:idsItens) " : ""

        Parametro paramEmissaoIni = emissao != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro paramEmissaoFin = emissao != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro paramCategoria = idsCategorias != null && idsCategorias.size() > 0 ? Parametro.criar("idsCategorias", idsCategorias) : null;
        Parametro paramEstados = idsEstados != null && idsEstados.size() > 0 ? Parametro.criar("idsEstados", idsEstados) : null;
        Parametro paramItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro paramEmpresa = Parametro.criar("idEmpresa", idEmpresa);

        String sql = "SELECT DISTINCT eaa0103id " +
                "FROM eaa01 " +
                "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "LEFT JOIN abm0102 ON abm0102item = abm01id " +
                "INNER JOIN aba3001 ON aba3001id = abm0102criterio " +
                "INNER JOIN abe01 ON abe01id = abb01ent " +
                "INNER JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                "INNER JOIN aag0201 ON aag0201id = abe0101municipio " +
                "INNER JOIN aag02 ON aag02id = aag0201uf " +
                whereClasDoc  +
                whereCriterio +
                whereEmpresa +
                whereEsMov +
                whereEmissao +
                whereCategoria +
                whereEstados +
                whereItens;

        return getAcessoAoBanco().obterListaDeLong(sql, paramEmissaoIni, paramEmissaoFin, paramCategoria, paramEstados, paramItens, paramEmpresa )
    }

    private void somarValores(TableMap valoresDocumento, TableMap valoresTotais){

        String nomeCampo = "eaa0103qtComl";

        if(valoresTotais.getBigDecimal(nomeCampo) == null){
            valoresTotais.put(nomeCampo, valoresDocumento.getBigDecimal_Zero(nomeCampo));
        }else{
            BigDecimal valorTotal = valoresTotais.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresTotais.getBigDecimal(nomeCampo);
            BigDecimal valorItem = valoresDocumento.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresDocumento.getBigDecimal(nomeCampo);
            BigDecimal soma = valorTotal + valorItem;
            valoresTotais.put(nomeCampo, soma)
        }

    }

    private List<TableMap> obterDevolucao(List<Long> idsItem){
        def whereItem = " where eaa01033itemdoc in (:item) "

        def sql = " select eaa01033itemdoc, eaa0103qtComl "+
                " from eaa01033 "+
                " inner join eaa0103 on eaa0103id = eaa01033item  "+
                whereItem +
                " and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "+
                " ORDER BY eaa01033itemdoc ";

        def p1 = criarParametroSql("item", idsItem)
        return getAcessoAoBanco().buscarListaDeTableMap(sql,p1)
    }

    private void comporDevolucoes(TableMap valores, TableMap devolucao){
        String nomeCampo = "eaa0103qtComl"
        if(valores.getBigDecimal(nomeCampo) != null){
            BigDecimal valor = valores.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valores.getBigDecimal(nomeCampo);
            BigDecimal valorDevolucao = devolucao.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : devolucao.getBigDecimal(nomeCampo);
            valores.put(nomeCampo, valor - valorDevolucao);
        }

    }


}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFZlbmRhcyBkZSBJdGVucyBQb3IgRXN0YWRvIChTSUYpIiwidGlwbyI6InJlbGF0b3JpbyJ9