package multitec.baseDemo;

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.Utils
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

public class DashServlet_DocumentosVencidos extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Documentos vencidos";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 3, 7, false, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		Map<String, Object> valoresDefault = Utils.map("valorVencido", "R\$ 15.689")
		
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_DocumentosVencidos.html", valoresDefault);
						
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLURvY3VtZW50b3MgdmVuY2lkb3MiLCJ0aXBvIjoic2VydmxldCIsInciOjMsImgiOjcsInJlc2l6ZSI6ZmFsc2UsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=