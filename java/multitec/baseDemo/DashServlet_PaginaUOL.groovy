package multitec.baseDemo;

import org.springframework.http.ResponseEntity

import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase

public class DashServlet_PaginaUOL extends ServletBase {
	@Override
	public String getNome() throws Exception {
		return "Multitec-Teste-Google";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.PAGINA_EXTERNA, 12, 8, true, "http://www.uol.com.br");
	}

	@Override
	public ResponseEntity<Object> executar() {
		return null;
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLVRlc3RlLUdvb2dsZSIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjgsInJlc2l6ZSI6dHJ1ZSwidGlwb2Rhc2hib2FyZCI6InBhZ2luYV9leHRlcm5hIiwidXJsIjoiaHR0cDovL3d3dy51b2wuY29tLmJyIn0=