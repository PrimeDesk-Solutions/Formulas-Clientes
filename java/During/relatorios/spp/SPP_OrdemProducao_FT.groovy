package During.relatorios.spp

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;

import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.Utils


public class SPP_OrdemProducao_FT extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPP - Ordem De Produção FT";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
       
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("numeroInicial", "0000001")
		filtrosDefault.put("numeroFinal", "99999999")
		
		return Utils.map("filtros", filtrosDefault);

    }
    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");
        List<TableMap> dados =  buscarDocumentosProducao(numDocIni, numDocFin);

        List<TableMap> listProcessos = new ArrayList();
        List<TableMap> listAtividades = new ArrayList();
        List<TableMap> listComponentes = new ArrayList();


        for(dado in dados){
            Long idDoc = dado.getLong("idOrdem");
            Long idPlano = dado.getLong("idPlano");
            List<TableMap> tmProcessos = buscarProcessosProdutivos(idDoc);
            List<TableMap> tmListAtividades = buscarAtividades(idDoc);
            List<TableMap> tmListComponentes = buscarComponentes(idDoc);

            Integer numPedido = buscarNumeroPedido(idPlano)

            for(processo in tmProcessos){
                processo.put("key",idDoc);
                dado.putAll(processo)
                listProcessos.add(processo);
            }

            for(atividade in tmListAtividades){
                atividade.put("key",idDoc);
                listAtividades.add(atividade);

            }

            for(componente in tmListComponentes){
                componente.put("key",idDoc);
                listComponentes.add(componente);
            }

		  dado.put("numPedido",numPedido)
            dado.put("key",idDoc);

        }

        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("DsSub1", listAtividades, "key", "key");
        dsPrincipal.addSubDataSource("DsSub2", listComponentes, "key", "key");

        adicionarParametro("SUBREPORT_DIR1", carregarArquivoRelatorio("SPP_OrdemProducao_FT_subreport1"))
        adicionarParametro("SUBREPORT_DIR2", carregarArquivoRelatorio("SPP_OrdemProducao_FT_subreport2"))


        return gerarPDF("SPP_OrdemProducao_FT",dsPrincipal )
    }
    private List<TableMap> buscarDocumentosProducao(Integer numDocIni, Integer numDocFin){
        String whereNumDoc = numDocIni != null && numDocFin != null ? "where abb01ordem.abb01num between :numDocIni and :numDocFin " : numDocIni != null && numDocFin == null ? "where abb01ordem.abb01num >= :numDocIni " : numDocIni == null && numDocFin != null ? "where abb01ordem.abb01num <= :numDocFin " : "";

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;

        String sql = "SELECT distinct baa01id as idPlano, bab01id as idOrdem, abb01ordem.abb01num AS numOrdem, bab01qt as qtd, abm01codigo AS codItemFin, abm01descr as descrItemFin,baa01descr as nomePlano, abb01pedido.abb01num as numPedido, " +
                    "abe01codigo as codEntidade, abe01na as naEntidade, aag0201nome as municipio, aag02uf as uf, abe0101endereco as endereco, abe0101numero as numero, " +
                    "coalesce((select string_agg(eaa0103pcnum, ',') from eaa0103 where eaa0103doc = eaa01id and eaa0103item = abm01id), '0') as pedCliente, baa0101opDtE as dataentrega " +
                    "FROM bab01 " +
                    "INNER JOIN abb01 AS abb01ordem ON abb01ordem.abb01id = bab01central " +
                    "INNER JOIN abp20 ON abp20id = bab01comp " +
                    "INNER JOIN abm01 AS prodFin on prodFin.abm01id = abp20item " +
                    "INNER JOIN bab0103 ON bab0103op = bab01id " +
                    "INNER JOIN baa0101 ON baa0101id = bab0103itempp " +
                    "INNER JOIN baa01 ON baa01id = baa0101plano " +
                    "INNER JOIN abb01 AS abb01plano ON abb01plano.abb01id = baa01central " +
                    "LEFT JOIN baa01011 ON baa01011pp = baa0101id " +
                    "LEFT JOIN eaa0103 ON eaa0103id = baa01011itemdoc " +
                    "LEFT JOIN eaa01 ON eaa01id = eaa0103doc " +
                    "LEFT JOIN abb01 AS abb01pedido ON abb01pedido.abb01id = eaa01central " +
                    "LEFT JOIN abe01 ON abe01id = abb01pedido.abb01ent " +
                    "LEFT JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                    "LEFT JOIN aag0201 on aag0201id = abe0101municipio " +
                    "LEFT JOIN aag02 ON aag0201uf = aag02id " +
                    whereNumDoc;
                    "ORDER BY abb01ordem.abb01num "

                    

                    
        return  getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumIni, parametroNumFin);
    }

    private  List<TableMap> buscarProcessosProdutivos(Long idDocProd){

        // busca processos produção dentro dos componentes da composição (abp20)

        String sql = "select abp2001seq as seqProc, abp10codigo as codProcesso, abp10descr as descrProcesso " +
                    "from abp20 " +
                    "inner join abp2001 on abp2001comp = abp20id " +
                    "inner join bab01 on bab01comp = abp20id " +
                    "inner join abp10 on abp10id = abp2001proc " +
                    "where bab01id = :idDocProd " +
                    "order by abp2001seq "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDocProd",idDocProd));
    }

    private List<TableMap> buscarAtividades(Long idDocProd){
        String sql = "select abp1001seq as seqAtividade,abp01codigo as codAtividade, abp01descr as descrAtividade " +
                    "from abp20 " +
                    "inner join abp2001 on abp2001comp = abp20id " +
                    "inner join bab01 on bab01comp = abp20id " +
                    "inner join abp10 on abp10id = abp2001proc " +
                    "inner join abp1001 on abp1001proc = abp10id " +
                    "inner join abp01 on abp01id = abp1001ativ " +
                    "where bab01id = :idDocProd " +
                    "order by abp1001seq";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDocProd",idDocProd));

    }

    private List<TableMap> buscarComponentes(Long idDocProd){
        String sql = "select distinct bab0101seq as seqComponente, abm01codigo as codComponente, abm01na as naComponente, aam06codigo as umuComponente," +
                        " bab0101qta / bab01qt as qtUnit, bab0101qta as qtdTotal " +
                        "from bab01 " +
                        "inner join bab0101 on bab0101op = bab01id " +
                        "inner join abm01 on abm01id = bab0101item " +
                        "inner join aam06 on aam06id = abm01umu " +
                        "where bab01id = :idDocProd"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDocProd",idDocProd));

    }

    private Integer buscarNumeroPedido(BigDecimal idPlano){
    		String sql = "select abb01num "+ 
					"from baa01 "+
					"inner join baa0101 on baa0101plano = baa01id "+
					"inner join baa01011 on baa01011pp = baa0101id "+
					"inner join eaa0103 on eaa0103id = baa01011itemdoc "+
					"inner join eaa01 on eaa01id = eaa0103doc "+
					"inner join abb01 on abb01id = eaa01central "+
					"where baa01id = :idPlano " + 
					"and abb01num is not null "



		return getAcessoAoBanco().obterInteger(sql, Parametro.criar("idPlano", idPlano));
    }
}
//meta-sis-eyJkZXNjciI6IlNQUCAtIE9yZGVtIERlIFByb2R1w6fDo28gRlQiLCJ0aXBvIjoicmVsYXRvcmlvIn0=