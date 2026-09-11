package Atilatte.servlets

import br.com.multiorm.criteria.criterion.Criterions;
import sam.dto.samdev.DashboardMetadata
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.util.stream.Collectors
import com.fasterxml.jackson.core.type.TypeReference;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.model.entities.ab.Aba2001


public class CGS_Atuaizar_Registro_Ruptura extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "CGS - Atualizar Registro Ruptura";
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
            for(registro in body){
                String tipo = registro.getString("tipo");
                Long idRepositorio = registro.getLong("aba2001id");

                if(tipo == "atualizar"){
                    Aba2001 aba2001 = getSession().get(Aba2001.class, Criterions.eq("aba2001id", idRepositorio));
                    aba2001.setAba2001json(registro);

                    getSession().persist(aba2001);
                }else{
                    Aba2001 aba2001 = new Aba2001();
                    aba2001.setAba2001json(registro);

                    getSession().persist(aba2001);
                }

            }

        } catch (Exception e){
            interromper(e.getMessage());
        }
    }
}