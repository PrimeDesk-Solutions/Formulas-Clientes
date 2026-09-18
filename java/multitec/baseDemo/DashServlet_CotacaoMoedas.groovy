package multitec.baseDemo;

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase

public class DashServlet_CotacaoMoedas extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Cotação moedas tempo real";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.PAGINA_EXTERNA, 6, 6, true, "https://br.widgets.investing.com/live-currency-cross-rates?theme=darkTheme");
	}

	@Override
	public ResponseEntity<Object> executar() {
		return null;
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLUNvdGHDp8OjbyBtb2VkYXMgdGVtcG8gcmVhbCIsInRpcG8iOiJzZXJ2bGV0IiwidyI6NiwiaCI6NiwicmVzaXplIjp0cnVlLCJ0aXBvZGFzaGJvYXJkIjoicGFnaW5hX2V4dGVybmEiLCJ1cmwiOiJodHRwczovL2JyLndpZGdldHMuaW52ZXN0aW5nLmNvbS9saXZlLWN1cnJlbmN5LWNyb3NzLXJhdGVzP3RoZW1lPWRhcmtUaGVtZSJ9