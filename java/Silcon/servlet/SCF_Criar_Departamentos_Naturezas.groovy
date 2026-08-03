package Silcon.servlet

import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.da.Daa01
import sam.model.entities.da.Daa0101
import sam.model.entities.da.Daa01011
import sam.model.entities.da.Dab10
import sam.model.entities.da.Dab1001
import sam.model.entities.da.Dab10011
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import br.com.multiorm.ColumnType;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.stream.Collectors
import com.fasterxml.jackson.core.type.TypeReference;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.jackson.JSonMapperCreator
import java.util.stream.Collectors

public class SCF_Criar_Departamentos_Naturezas extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return null;
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Object> executar() {
        try{
            String req = httpServletRequest.getReader().lines().collect(Collectors.joining());
            TableMap body = JSonMapperCreator.create().read(req, new TypeReference<TableMap>() {});
            Long idLcto = body.get("dab10id");
            Long idDoc = body.get("daa01id");
            session.beginTransaction();


            // Documento Financeiro
            Daa01 daa01 = getSession().createCriteria(Daa01.class).addWhere(Criterions.eq("daa01id", idDoc)).get();

            // Departamentos
            List<Daa0101> daa0101s = getSession().createCriteria(Daa0101.class).addWhere(Criterions.eq("daa0101doc", daa01.daa01id)).getList(ColumnType.ENTITY);

            // Lançamentos
            Dab10 dab10 = getSession().createCriteria(Dab10.class).addWhere(Criterions.eq("dab10id", idLcto)).get();

            deletarNaturezas(idLcto);
            deletarDepartamentos(idLcto);

            Boolean alterado = false;
            if(daa0101s != null && daa0101s.size() > 0){
                for(Daa0101 daa0101 in  daa0101s){
                    Dab1001 dab1001 = new Dab1001();
                    dab1001.setDab1001valor(daa0101.daa0101valor);
                    dab1001.setDab1001depto(daa0101.daa0101depto);
                    dab1001.setDab1001lct(dab10);

                    // Naturezas
                    List<Daa01011> daa01011s = getSession().createCriteria(Daa01011.class).addWhere(Criterions.eq("daa01011depto", daa0101.daa0101id)).getList(ColumnType.ENTITY);

                    if(daa01011s != null && daa01011s.size() > 0){
                        for(Daa01011 daa01011 in daa01011s){
                            Dab10011 dab10011 = new Dab10011();
                            dab1001.addToDab10011s(dab10011)
                            dab10011.setDab10011nat(daa01011.daa01011nat);
                            dab10011.setDab10011valor(daa01011.daa01011valor);
                        }
                    }

                    dab10.addToDab1001s(dab1001);
                }

                session.persist(dab10);
                alterado = Boolean.TRUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(alterado);

        }catch (Exception e){
            interromper("Erro servlet: " + e.getMessage());
        }
    }
    private void deletarNaturezas(Long idLcto){
        try{
            String sql = "DELETE FROM dab10011 " +
                    " WHERE dab10011id IN ( " +
                    " SELECT dab10011id " +
                    " FROM dab10011 " +
                    " INNER JOIN dab1001 ON dab1001id = dab10011depto " +
                    " INNER JOIN dab10 ON dab10id = dab1001lct " +
                    " WHERE dab10id = :idLcto" +
                    " )"

            getSession().createQuery(sql).setParameter("idLcto", idLcto).executeUpdate();
        }catch (Exception e){
            throw new ValidacaoException(e.getMessage());
        }

    }
    private void deletarDepartamentos(idLcto){
        try{
            String sql = "DELETE FROM dab1001 "+
                    " WHERE dab1001id IN ( " +
                    "SELECT dab1001id " +
                    "FROM dab1001 " +
                    "WHERE dab1001lct = :idLcto " +
                    " )"

            getSession().createQuery(sql).setParameter("idLcto", idLcto).executeUpdate();
        }catch (Exception e){
            throw new ValidacaoException(e.getMessage())
        }
    }
}