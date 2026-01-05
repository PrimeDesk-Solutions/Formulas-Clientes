package Profer.interceptadores;

import br.com.multiorm.ColumnType;
import br.com.multiorm.ORMInterceptor
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import org.jfree.chart.renderer.xy.YIntervalRenderer
import sam.model.entities.ab.Abb01
import sam.model.entities.cb.Cbb01;

import java.util.List;
import br.com.multiorm.Session;

public class GerarItensPreCotacao implements ORMInterceptor<sam.model.entities.cb.Cbb0101> {

    @Override
    public Class<sam.model.entities.cb.Cbb0101> getEntityClass() {
        return sam.model.entities.cb.Cbb0101.class;
    }
    @Override
    public void prePersist(sam.model.entities.cb.Cbb0101 entity, Session s) {
    }
    @Override
    public void posPersist(sam.model.entities.cb.Cbb0101 entity, Session s) {
//        Long idOrdem = entity.cbb0101oc.cbb01id;
//        Long idItens = entity.cbb0101id;
//
//        // Ordem Compra
//        Cbb01 cbb01 = s.createCriteria(Cbb01.class).addWhere(Criterions.eq("cbb01id",entity.cbb0101oc.cbb01id)).get();
//
//        // Central de Documentos
//        Abb01 abb01 = s.createCriteria(Abb01.class).addWhere(Criterions.eq("abb01id",cbb01.cbb01central.abb01id)).get();
//
//        Long numOrdem = abb01.abb01num;
//
//        Long idPreCot = s.createQuery("SELECT cbb10id FROM cbb10 WHERE cbb10num = :numOrdem ").setParameter("numOrdem", numOrdem).getUniqueResult(ColumnType.LONG);
//
//        String sql = "INSERT INTO cbb1001 VALUES (nextval('default_sequence'), "+idPreCot.toString()+","+idItens.toString()+")"
//
//        s.connection.prepareStatement(sql).execute()
    }
    @Override
    public void preDelete(List<Long> ids, Session s) {
    }
}
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuY2IuQ2JiMDEwMSJ9
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuY2IuQ2JiMDEwMSJ9