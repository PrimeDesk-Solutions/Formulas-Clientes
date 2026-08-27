package Silcon.interceptadores

import br.com.multiorm.ColumnType;
import br.com.multiorm.ORMInterceptor
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import jdk.jfr.Experimental;

import java.util.List;
import br.com.multiorm.Session;
import sam.model.entities.da.Dab01;
import sam.model.entities.da.Dab1002;
import br.com.multiorm.criteria.criterion.Criterions



public class VerificarContasCorrentes implements ORMInterceptor<sam.model.entities.da.Dab10> {

    @Override
    public Class<sam.model.entities.da.Dab10> getEntityClass() {
        return sam.model.entities.da.Dab10.class;
    }
    @Override
    public void prePersist(sam.model.entities.da.Dab10 entity, Session s) {
        try{
            if(entity.dab10historico == "Fechamento de Caixa." || entity.dab10historico == "Abertura de Caixa PDV" || entity.dab10historico == "Saida do Caixa Central pela abertura do caixa do Usuario." ) return;

            for(Dab1002 dab1002 in entity.dab1002s){
                if(dab1002.dab1002cc == null) continue;
                Dab01 dab01 = s.createCriteria(Dab01.class).addWhere(Criterions.eq("dab01id", dab1002.dab1002cc.dab01id)).get();
                TableMap jsonDab01 = buscarCampoCustomCC(dab01.dab01id, s);
                if(jsonDab01.getInteger("requer_abertura") == 0) continue;

                Boolean isOpen = verificarAberturaConta(dab01.dab01id, s);

                if(!isOpen) throw new ValidacaoException("Não há abertura de conta para a conta " + dab01.dab01codigo + ". Necessário realizar abertura de conta antes de prosseguir.");
            }
        }catch (Exception e){
            throw new ValidacaoException(e.getMessage())
        }
    }
    private TableMap buscarCampoCustomCC(Long idConta, Session s){
        String sql = "SELECT dab01camposCustom AS custom FROM dab01 WHERE dab01id = :idConta";

        TableMap tmCamposCustom = s.createQuery(sql).setParameter("idConta", idConta).getUniqueResult(ColumnType.JSON);

        return tmCamposCustom == null ? new TableMap() : tmCamposCustom;
    }
    private Boolean verificarAberturaConta(Long idConta, Session s){
        try{
            String sql = "SELECT cca10id FROM cca10 WHERE cca10abertData IS NOT NULL AND cca10fechamdata IS NULL AND cca10cc = :idConta";

            Long idAbertura = s.createQuery(sql).setParameter("idConta", idConta).getUniqueResult(ColumnType.LONG);

            return idAbertura != null
        }catch(Exception e){
            throw new ValidacaoException("Erro ao buscar dados da conta de Entrada.")
        }
    }
    @Override
    public void posPersist(sam.model.entities.da.Dab10 entity, Session s) {
    }
    @Override
    public void preDelete(List<Long> ids, Session s) {
    }
}
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuZGEuRGFiMTAifQ==