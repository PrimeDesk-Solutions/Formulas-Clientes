package Profer.interceptadores;
import br.com.multiorm.ORMInterceptor
import br.com.multitec.utils.ValidacaoException
import sam.model.entities.ab.Abb01
import sam.model.entities.cb.Cbb01;
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.Utils;


import java.time.LocalDate;
import java.util.List;
import br.com.multiorm.Session;

public class GerarPreCotacao implements ORMInterceptor<sam.model.entities.cb.Cbb01> {

    @Override
    public Class<sam.model.entities.cb.Cbb01> getEntityClass() {
        return sam.model.entities.cb.Cbb01.class;
    }
    @Override
    public void prePersist(sam.model.entities.cb.Cbb01 entity, Session s) {
	   // Altera o Status da ordem de compra para 1- Cotado
        //entity.setCbb01status(2);

        // Define a data da cotação
        //entity.cbb01dtCot = LocalDate.now();
    }
    @Override
    public void posPersist(sam.model.entities.cb.Cbb01 entity, Session s) {
//        Integer numOrdem;
//        String descrOrdem;
//        LocalDate dtOrdem;
//        LocalDate dtVcto;
//
//	    if(Utils.campoEstaCarregado(entity, "cbb01central")){
//	    	// Central de Documentos
//	        Abb01 abb01 = s.createCriteria(Abb01.class).addWhere(Criterions.eq("abb01id",entity.cbb01central.abb01id)).get();
//	
//	        numOrdem = abb01.abb01num;
//	        descrOrdem = "Pré-Cotação da OC: " + numOrdem.toString();
//	        dtOrdem = abb01.abb01data;
//	        dtVcto = abb01.abb01data.plusDays(30);
//            def idEmpresa = obterEmpresaAtiva().getAac10id()
//	
//	        String insert = "INSERT INTO cbb10 values(nextval('default_sequence'), "+numOrdem.toString()+","+"'"+dtOrdem + "',"+"'"+dtVcto+"',"+"0,"+idEmpresa+", "+ "'"+ descrOrdem + "')";
//	
//	        s.connection.prepareStatement(insert).execute()
//	    }
       
    }
    @Override
    public void preDelete(List<Long> ids, Session s) {
    }
}
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuY2IuQ2JiMDEifQ==
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuY2IuQ2JiMDEifQ==