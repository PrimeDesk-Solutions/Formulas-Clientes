package multitec.baseDemo;

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

public class DashServlet_FluxoCaixa extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Fluxo de caixa";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 6, 12, false, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_FluxoCaixa.html");
				
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLUZsdXhvIGRlIGNhaXhhIiwidGlwbyI6InNlcnZsZXQiLCJ3Ijo2LCJoIjoxMiwicmVzaXplIjpmYWxzZSwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==