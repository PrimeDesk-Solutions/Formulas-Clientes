package multitec.baseDemo;

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase

public class DashServlet_PrevisaoTempoItatiba extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Previsão do Tempo em Itatiba";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.PAGINA_EXTERNA, 3, 6, true, "https://www.cptec.inpe.br/widget/widget.php?p=2620&w=h&c=767676&f=ffffff");
	}

	@Override
	public ResponseEntity<Object> executar() {
		return null;
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLVByZXZpc8OjbyBkbyBUZW1wbyBlbSBJdGF0aWJhIiwidGlwbyI6InNlcnZsZXQiLCJ3IjozLCJoIjo2LCJyZXNpemUiOnRydWUsInRpcG9kYXNoYm9hcmQiOiJwYWdpbmFfZXh0ZXJuYSIsInVybCI6Imh0dHBzOi8vd3d3LmNwdGVjLmlucGUuYnIvd2lkZ2V0L3dpZGdldC5waHA_cD0yNjIwJnc9aCZjPTc2NzY3NiZmPWZmZmZmZiJ9