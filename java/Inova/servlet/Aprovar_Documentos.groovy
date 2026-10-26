package Inova.servlet

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.aa.Aab10
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb0103
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors
import com.fasterxml.jackson.core.type.TypeReference;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.model.entities.ea.Eaa01

public class Aprovar_Documentos extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "Aprovar Documentos"
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return null;
    }

    @Override
    public ResponseEntity<Object> executar() {
        session.beginTransaction();
        String req = httpServletRequest.getReader().lines().collect(Collectors.joining());
        TableMap body = JSonMapperCreator.create().read(req, new TypeReference<TableMap>() {});
        Long idDocumento = body.get("eaa01id");
        String obsAprovacao = body.get("obs");
        aprovarDocumentos(idDocumento, obsAprovacao)

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body();
    }
    private void aprovarDocumentos(Long idDocumento, String obsInterno){
        try {
            Eaa01 eaa01 = getSession().get(Eaa01.class, Criterions.eq("eaa01id", idDocumento));
            LocalDateTime agora = LocalDateTime.now();
            String dataHora = agora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String user = obterUsuarioLogado().getAab10user();
            String fraseAprovacao = "Aprovado por " + user + " em " + dataHora + " através do script de aprovações.";
            String obs1 = eaa01.eaa01obsUsoInt != null && !eaa01.eaa01obsUsoInt.isEmpty() ? eaa01.eaa01obsUsoInt + "\n" + obsInterno : obsInterno;
            String obs2 = eaa01.eaa01obsSAM != null && !eaa01.eaa01obsSAM.isEmpty() ? eaa01.eaa01obsSAM + "\n" + fraseAprovacao : fraseAprovacao;

            eaa01.setEaa01obsUsoInt(obs1);
            eaa01.setEaa01obsSAM(obs2);

            criarInformacoesAprovacao(eaa01);

            getSession().persist(eaa01);
        } catch (Exception e){
            throw new ValidacaoException("Erro ao aprovar documento: " + e.getMessage())
        }
    }
    private criarInformacoesAprovacao(Eaa01 eaa01){
        try {
            Abb01 abb01 = getSession().get(Abb01.class, Criterions.eq("abb01id", eaa01.eaa01central.abb01id));
            Aab10 aab10 = obterUsuarioLogado();

            if(abb01.abb0103s != null && abb01.abb0103s.size() > 0){
                for(Abb0103 abb0103 in abb01.abb0103s){
                    abb0103.abb0103user = aab10;
                    abb0103.abb0103data = LocalDate.now();
                    abb0103.abb0103hora = LocalTime.now();
                    getSession().persist(abb0103);
                }
            } else {
                Abb0103 abb0103 = new Abb0103();
                abb0103.abb0103user = aab10;
                abb0103.abb0103data = LocalDate.now();
                abb0103.abb0103hora = LocalTime.now();

                abb01.addToAbb0103s(abb0103);
                getSession().persist(abb01)

            }
        } catch (Exception e){
            throw new ValidacaoException("Não foi possível criar as informações de aprovação: " + e.getMessage())
        }


    }
}
//meta-sis-eyJkZXNjciI6IkJ1c2NhciBUaXR1bG9zIFZlbmNpZG9zIEVudGlkYWRlIiwidGlwbyI6InNlcnZsZXQifQ==