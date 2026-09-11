package Atilatte.servlets

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions;
import sam.dto.samdev.DashboardMetadata
import sam.model.entities.ab.Aba20
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import sam.server.samdev.utils.Parametro

import java.util.stream.Collectors
import com.fasterxml.jackson.core.type.TypeReference;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.model.entities.ab.Aba2001


public class CGS_Atualizar_Incluir_Registro_Ruptura extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "CGS - Atualizar/Incluir Registro Ruptura";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Object> executar() {
        String req = httpServletRequest.getReader().lines().collect(Collectors.joining());
        List<TableMap> body = JSonMapperCreator.create().read(req, new TypeReference<List<TableMap>>() {}) as List<TableMap>;

        atualizarDadosRuptura(body);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(null);
    }
    private void atualizarDadosRuptura(List<TableMap> body){
        try{
            session.beginTransaction();
            Integer numLcto = 0;
            for(registro in body){
                String tipo = registro.getString("tipo");

                if(tipo == "atualizar"){
                    Long aba2001id = registro.getLong("aba2001id");
                    Aba2001 aba2001 = getSession().get(Aba2001.class, Criterions.eq("aba2001id", aba2001id));
                    aba2001.setAba2001json(registro);

                    getSession().persist(aba2001);
                }else{
                    Long aba20id = registro.getLong("aba20id");
                    Aba20 aba20 = getSession().get(Aba20.class, Criterions.eq("aba20id", aba20id));
                    Integer ultimoLcto = getSession().createQuery("SELECT COALESCE(MAX(aba2001lcto), 0) AS max FROM aba2001 WHERE aba2001rd = :aba20id").setParameter("aba20id", aba20id).getUniqueResult(ColumnType.INTEGER);
                    Aba2001 aba2001 = new Aba2001();
                    aba2001.setAba2001rd(aba20);
                    aba2001.setAba2001lcto(ultimoLcto + 1)
                    aba2001.setAba2001json(registro);

                    getSession().persist(aba2001);
                }

            }

        } catch (Exception e){
            interromper("Erro ao executar servelet: " + e.getMessage());
        }
    }
}