package multitec.baseDemo;

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.Utils
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

public class DashServlet_SaldoEmCaixa extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Saldo em caixa";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 3, 7, false, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		Map<String, Object> valoresDefault = Utils.map("saldoEmCaixa", "R\$ 5.689")
		
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_SaldoEmCaixa.html", valoresDefault);
						
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLVNhbGRvIGVtIGNhaXhhIiwidGlwbyI6InNlcnZsZXQiLCJ3IjozLCJoIjo3LCJyZXNpemUiOmZhbHNlLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9