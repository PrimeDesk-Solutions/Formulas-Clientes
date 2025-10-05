package Atilatte.interceptadores;
import br.com.multiorm.ORMInterceptor
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb0102
import sam.model.entities.ab.Abm01
import sam.model.entities.ba.Bab01
import sam.model.entities.ba.Bab0104
import sam.model.entities.ba.Bab01041;

import java.util.List;
import br.com.multiorm.Session;

public class SCQ_GerarDataFabricacaoValidadeFicha implements ORMInterceptor<sam.model.entities.bb.Bbb01> {

    @Override
    public Class<sam.model.entities.bb.Bbb01> getEntityClass() {
        return sam.model.entities.bb.Bbb01.class;
    }
    @Override
    public void prePersist(sam.model.entities.bb.Bbb01 entity, Session s) {
        // Desdobramento
        Abb0102 abb0102 = s.createCriteria(Abb0102.class).addWhere(Criterions.eq("abb0102doc", entity.bbb01central.abb01id)).get();

        if(abb0102 == null) return;

        // Central Ordem Produção
        Abb01 abb01 = s.createCriteria(Abb01.class).addWhere(Criterions.eq("abb01id", abb0102.abb0102central.abb01id)).get();

        // Ordem Produção
        Bab01 bab01 = s.createCriteria(Bab01.class).addWhere(Criterions.eq("bab01central", abb01.abb01id)).get();

        // Produções Conluídas
        Bab0104 bab0104 = s.createCriteria(Bab0104.class).addWhere(Criterions.where("bab0104op = "+  bab01.bab01id.toString() + " and bab0104opc = 1")).get();

        if(bab0104 == null) return;

        // Produções Concluídas - Produtos
        Bab01041 bab01041 = s.createCriteria(Bab01041.class).addWhere(Criterions.eq("bab01041pc", bab0104.bab0104id)).get();

        // Campos Livres
        TableMap jsonBbb01 = entity.bbb01json != null ? entity.bbb01json : new TableMap();

        // Data Fabricação - Ordem
        String dataFabricacao = bab01.bab01ctDtI.toString().replace("-", "");

        // Data Validade - Ordem
        String dataValidade = bab01041.bab01041validade.toString().replace("-", "");

        jsonBbb01.put("data_fabricacao", dataFabricacao );
        jsonBbb01.put("data_de_validade", dataValidade );

        entity.setBbb01json(jsonBbb01);
    }
    @Override
    public void posPersist(sam.model.entities.bb.Bbb01 entity, Session s) {
    }
    @Override
    public void preDelete(List<Long> ids, Session s) {
    }
}
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuYmIuQmJiMDEifQ==