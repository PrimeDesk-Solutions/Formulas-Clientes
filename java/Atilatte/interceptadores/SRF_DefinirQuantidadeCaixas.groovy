package Atilatte.interceptadores

import org.jfree.chart.renderer.xy.YIntervalRenderer
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ea.Eaa0103;

import sam.model.entities.ab.Abb01;
import sam.model.entities.ea.Eaa01;

import br.com.multiorm.ORMInterceptor
import sam.model.entities.ea.Eaa01032

import javax.el.EvaluationListener;
import java.util.List;
import br.com.multiorm.Session;
import br.com.multitec.utils.ValidacaoException
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.ColumnType;
import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.Utils;

public class SRF_DefinirQuantidadeCaixas implements ORMInterceptor<sam.model.entities.ea.Eaa01> {

    @Override
    public Class<sam.model.entities.ea.Eaa01> getEntityClass() {
        return sam.model.entities.ea.Eaa01.class;
    }
    @Override
    public void prePersist(sam.model.entities.ea.Eaa01 entity, Session s) {

        /*
            Interceptador desenvolvido para corrigir a quantidade de caixa e volumes dos documentos de acordo com a quanitdade de etiquetas lidas para cada produto:

            Ao salvar um pedido de venda, o interceptador é disparado na tabela Eaa01 para inserir um campo livre na linha de cada item com o número do pedido de venda,
            que será utilizado para buscar o número de etiqueta no faturamento;

            Ao ser faturado o pedido, o sistema busca o número do pedido interno em cada item da nota, e caso o item esteja configurado no cadastro como "1 - Realiza Coleta", é executada uma SQL para buscar o número de etiquetas lidas para aquele item do pedido,
            Se o item não estiver no cadastro para realizar coleta, o sistema irá considerar o calculo de volume e caixa do pedido;

         */


        if(Utils.campoEstaCarregado(entity, "eaa01central") && Utils.campoEstaCarregado(entity, "eaa01esMov")){
            Abb01 abb01 = s.createCriteria(Abb01.class).addWhere(Criterions.eq("abb01id",entity.eaa01central.abb01id)).get();
            Aah01 aah01 = s.createCriteria(Aah01.class).addWhere(Criterions.eq("aah01id",abb01.abb01tipo.aah01id)).get();
            
            if(abb01.abb01operAutor != "SRF1003"){ // Pedido de venda
                for(Eaa0103 eaa0103 : entity.eaa0103s){
                    TableMap mapJson = eaa0103.eaa0103json;

                    mapJson.put("pedido_interno", abb01.abb01num);
                    mapJson.put("tipo_doc_ped_interno", aah01.aah01codigo);

                    eaa0103.eaa0103json = mapJson;
                }
            }
            if(entity.eaa01id == null){ //Verificar com Wilson, quando der problema na manutenção
                // Central de Documentos
                def validaColeta = s.createQuery("select cast(abd01camposcustom ->> 'validar_coleta' as integer) from abd01 where abd01id = :idPcd ").setParameter("idPcd", entity.eaa01pcd.abd01id).getUniqueResult(ColumnType.INTEGER);

			 if(abb01.abb01operAutor == "SRF1002" || abb01.abb01operAutor == "SRF1003") validaRomaneio(entity, s);

                def somaCaixa = 0;
                def somaVolumes = 0;
                Integer qtdCaixa;

                if(abb01.abb01operAutor == "SCV3002" || abb01.abb01operAutor == "SCV2002" || abb01.abb01operAutor == "SRF1003" || abb01.abb01operAutor == 'SAMPalm4001'){ // && entity.eaa01id == null
                    if(entity.eaa01esMov == 1){
                        TableMap jsonEaa01 = entity.eaa01json;
                        if(validaColeta == 1){
                            if(abb01.abb01operAutor == "SRF1003"){ // Faturamento
                                for(Eaa0103 eaa0103 : entity.eaa0103s){

                                    TableMap jsonEaa0103 = eaa0103.eaa0103json;

                                    Integer numPedido = jsonEaa0103.getInteger("pedido_interno");

                                    if(numPedido == null) throw new ValidacaoException("Interceptador: Não foi encontrado o número do pedido interno no documento ")

                                    Abm01 abm01 = s.createCriteria(Abm01.class).addWhere(Criterions.eq("abm01id",eaa0103.eaa0103item.abm01id)).get();

                                    Abm0101 abm0101 = s.createCriteria(Abm0101.class).addWhere(Criterions.eq("abm0101item",abm01.abm01id)).get();

                                    TableMap jsonAbm0101 = abm0101.abm0101json;

                                    if(jsonAbm0101.getInteger("realiza_coleta") == 1){

                                        // Busca quantidade de caixa por item
                                        Query query = s.createQuery("select count(abm01codigo) as numEtiqueta from bfa01 " +
                                                "inner join abb01 as romaneio on romaneio.abb01id = bfa01central " +
                                                "inner join bfa0101 on bfa0101rom = bfa01id " +
                                                "inner join bfa01011 on bfa01011item = bfa0101id " +
                                                "inner join eaa0103 on eaa0103id = bfa0101item " +
                                                "inner join abm01 on abm01id = eaa0103item " +
                                                "inner join abm70 on abm70idunidrom = bfa01011id " +
                                                "inner join  abb01 as pedido on pedido.abb01id= bfa01docscv " +
                                                "where pedido.abb01num = :numPedido "+
                                                "and abm01id = :idItem ");

                                        query.setParameter("numPedido", numPedido);
                                        query.setParameter("idItem", abm01.abm01id);

                                        qtdCaixa = query.getUniqueResult(ColumnType.INTEGER);

                                        if(qtdCaixa == 0 ) throw new ValidacaoException("Interceptador: Não foi encontrada etiqueta de coleta para o item "+ abm01.abm01codigo + " " + abm01.abm01na);

                                        // Caixa e Volume
                                        jsonEaa0103.put("caixa", qtdCaixa);
                                        jsonEaa0103.put("volumes", qtdCaixa);

                                        somaCaixa = somaCaixa + jsonEaa0103.getBigDecimal_Zero("caixa");
                                        somaVolumes = somaVolumes + jsonEaa0103.getBigDecimal_Zero("volumes");

                                        eaa0103.setEaa0103json(jsonEaa0103);
                                    }else{
                                        qtdCaixa = jsonEaa0103.getInteger("caixa");
                                        somaCaixa += qtdCaixa;
                                        somaVolumes += qtdCaixa;
                                    }

                                    List<TableMap> etiquetas = buscarEtiquetaItens(numPedido, abm01.abm01id, s);

                                    if(etiquetas != null && etiquetas.size() > 0){
                                        for(etiqueta in etiquetas){
                                            Long idUser = etiqueta.getLong("idUser");
                                            Long idItem = etiqueta.getLong("idItem");
                                            Long idEntidade = etiqueta.getLong("idEntidade");
                                            etiqueta.put("num_nota", abb01.abb01num);
                                            etiqueta.put("id_nota", abb01.abb01id);
                                            gravarInformacoesEtiquetas(s,idUser, idItem, etiqueta, idEntidade)
                                        }
                                    }
                                }

                                //if(entity.eaa01json.getInteger("caixa") == 0 || entity.eaa01json.getInteger("volumes") == 0 ) {
                                    jsonEaa01.put("caixa",somaCaixa);
                                    jsonEaa01.put("volumes",somaVolumes);

                                    entity.setEaa01json(jsonEaa01);
                                //}
                            }
                        }

                    }
                }
            }
        }



    }

    private List<TableMap> buscarEtiquetaItens(Integer numPedido, Long idItem, Session s){
        Query query = s.createQuery("select abb01rom.abb01num as num_romaneio, abm70num as num_etiqueta, abm70uldata as data_coleta, abm70ulhora as hora_coleta, aab10id as idUser, abm01id as idItem, abm70id as id_etiquetas, "+
                "bfb01lote as nome_lote, abe01ent.abe01id as idEntidade, abe01desp.abe01codigo as cod_despacho, abe01desp.abe01na as na_despacho, abe01redesp.abe01codigo as cod_redesp, abe01redesp.abe01na as na_redesp, "+
                "abm70qt as qtd_etiqueta, abm70lote as lote_etiqueta, abm70data as dt_criacao_etiqueta, abm70validade as validade_etiqueta, abm70fabric as fabric_etiqueta "+
                "from bfa01   "+
                "inner join abb01 as abb01rom on abb01rom.abb01id = bfa01central  "+
                "inner join bfa0101 on bfa0101rom = bfa01id  "+
                "inner join bfa01011 on bfa01011item = bfa0101id  "+
                "inner join eaa0103 on bfa0101item = eaa0103id  "+
                "inner join eaa01 on eaa01id = eaa0103doc "+
                "inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01central "+
                "inner join abe01 as abe01ent on abe01ent.abe01id = abb01pedido.abb01ent "+
                "inner join bfb0101 on bfb0101central = abb01pedido.abb01id "+
                "inner join bfb01 on bfb01id = bfb0101lote "+
                "inner join abm01 on abm01id = eaa0103item  "+
                "inner join abm70 on abm70idunidrom = bfa01011id  "+
                "inner join aab10 on aab10id = abm70uluser  "+
                "left join abe01 as abe01desp on abe01desp.abe01id = bfb01despacho "+
                "left join abe01 as abe01redesp on abe01redesp.abe01id = bfb01redespacho "+
                "where cast(eaa0103json ->> 'pedido_interno' as integer) = :numPedido "+
                "and abm01id = :idItem");

        query.setParameter("numPedido",numPedido);
        query.setParameter("idItem",idItem);

        return query.getListTableMap()
    }

    private void gravarInformacoesEtiquetas(Session s, Long idUser, Long idItem, TableMap etiqueta, Long idEntidade){

        String sql = "insert into aba2001 values (nextval('default_sequence'), 40347419, 1, 'Gravação etiqueta', " + idUser.toString() +","+idEntidade.toString()+"," +idItem.toString()+", null,null,null,'"+etiqueta.toString()+"',null,null,null,null ) "

        s.connection.prepareStatement(sql).execute()

    }

	private void validaRomaneio(Eaa01 eaa01, Session s){
        def contemRomaneio = s.createQuery("select cast(abd01camposcustom ->> 'contem_romaneio' as integer) from abd01 where abd01id = :idPcd ").setParameter("idPcd", eaa01.eaa01pcd.abd01id).getUniqueResult(ColumnType.INTEGER);
        if(contemRomaneio == 1){

            for(Eaa0103 eaa0103 in eaa01.eaa0103s){

                // Item
                Abm01 abm01 = s.createCriteria(Abm01.class).addWhere(Criterions.eq("abm01id", eaa0103.eaa0103item.abm01id)).get();

                Integer numPedido = eaa0103.eaa0103json.getInteger("pedido_interno");
                String tipoDocPedido = eaa0103.eaa0103json.getString("tipo_doc_ped_interno");

                if (tipoDocPedido == null) throw new ValidacaoException("Interceptador: Não foi informado o tipo de documento do pedido interno no item " + abm01.abm01codigo + " do pedido " + numPedido.toString())


                Query query = s.createQuery("select bfa01id " +
                                            "from bfa01 " +
                                            "inner join abb01 on abb01id = bfa01docscv " +
                                            "inner join aah01 on aah01id = abb01tipo "+
                                            "where abb01num = :numPedido "+
                                            "and aah01codigo = :tipoDocPedido ");


                query.setParameter("numPedido", numPedido);
                query.setParameter("tipoDocPedido", tipoDocPedido);

                Long idRomaneio = query.getUniqueResult(ColumnType.LONG);

                if(idRomaneio == null) throw new ValidacaoException("Interceptador: Não foi encontrado romaneio para geração do documento " +numPedido.toString())

            }

        }
    }

    @Override
    public void posPersist(sam.model.entities.ea.Eaa01 entity, Session s) {

    }




    @Override
    public void preDelete(List<Long> ids, Session s) {
    }
}
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuZWEuRWFhMDEifQ==