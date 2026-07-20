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
                                        Query query = s.createQuery("SELECT count(abm01codigo) AS numEtiqueta FROM bfa01 " +
                                                "INNER JOIN abb01 AS romaneio ON romaneio.abb01id = bfa01central " +
                                                "INNER JOIN bfa0101 ON bfa0101rom = bfa01id " +
                                                "INNER JOIN bfa01011 ON bfa01011item = bfa0101id " +
                                                "INNER JOIN eaa0103 ON eaa0103id = bfa0101item " +
                                                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                                                "INNER JOIN abm70 ON abm70idunidrom = bfa01011id " +
                                                "INNER JOIN  abb01 AS pedido ON pedido.abb01id= bfa01docscv " +
                                                "WHERE pedido.abb01num = :numPedido "+
                                                "AND abm01id = :idItem ");

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
                                }
                                jsonEaa01.put("caixa",somaCaixa);
                                jsonEaa01.put("volumes",somaVolumes);

                                entity.setEaa01json(jsonEaa01);
                            }
                        }

                    }
                }
            }
        }
    }
    private void validaRomaneio(Eaa01 eaa01, Session s){
        def contemRomaneio = s.createQuery("SELECT cast(abd01camposcustom ->> 'contem_romaneio' AS INTEGER) FROM abd01 WHERE abd01id = :idPcd ").setParameter("idPcd", eaa01.eaa01pcd.abd01id).getUniqueResult(ColumnType.INTEGER);
        if(contemRomaneio == 1){
            for(Eaa0103 eaa0103 in eaa01.eaa0103s){

                // Item
                Abm01 abm01 = s.createCriteria(Abm01.class).addWhere(Criterions.eq("abm01id", eaa0103.eaa0103item.abm01id)).get();

                Integer numPedido = eaa0103.eaa0103json.getInteger("pedido_interno");
                String tipoDocPedido = eaa0103.eaa0103json.getString("tipo_doc_ped_interno");

                if (tipoDocPedido == null) throw new ValidacaoException("Interceptador: Não foi informado o tipo de documento do pedido interno no item " + abm01.abm01codigo + " do pedido " + numPedido.toString())


                Query query = s.createQuery("SELECT bfa01id " +
                        "FROM bfa01 " +
                        "INNER JOIN abb01 on abb01id = bfa01docscv " +
                        "INNER JOIN aah01 on aah01id = abb01tipo "+
                        "WHERE abb01num = :numPedido "+
                        "AND aah01codigo = :tipoDocPedido ");


                query.setParameter("numPedido", numPedido);
                query.setParameter("tipoDocPedido", tipoDocPedido);

                Long idRomaneio = query.getUniqueResult(ColumnType.LONG);

                if(idRomaneio == null) throw new ValidacaoException("Interceptador: Não foi encontrado romaneio para geração do documento " + numPedido.toString())
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