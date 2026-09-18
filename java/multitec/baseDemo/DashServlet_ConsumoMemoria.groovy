package multitec.baseDemo;

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.Utils
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

public class DashServlet_ConsumoMemoria extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Consumo de memória";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 3, 10, false, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_ConsumoMemoria.html");
		
		String script = dto.getScript()
		long heapMaxSize = Runtime.getRuntime().maxMemory() / 1024 /1024;
		long heapSize = Runtime.getRuntime().totalMemory() / 1024 / 1024;
		long heapFreeSize = Runtime.getRuntime().freeMemory() / 1024 / 1024;
		
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
			"max", heapMaxSize,
			"atual", heapSize,
			"uso", heapFreeSize,
			));
		String resolvedString = sub.replace(dto.getScript());
		dto.setScript(resolvedString);
				
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLUNvbnN1bW8gZGUgbWVtw7NyaWEiLCJ0aXBvIjoic2VydmxldCIsInciOjMsImgiOjEwLCJyZXNpemUiOmZhbHNlLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9