package multitec.baseDemo;

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.Utils
import br.com.multitec.utils.http.HttpRequest
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.dto.samdoc.Licenca
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

public class DashServlet_ExibirLicenca extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Dados da licença";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 2, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		Licenca licenca = HttpRequest.create()
			.finalUrl("http://" + httpServletRequest.getServerName() + ":" + httpServletRequest.getServerPort() + "/licenca/buscarLicenca")
			.header("x-auth-token", variaveis.getAab10().getAab10hash())
			.post()
			.parseResponse(Licenca.class);
			
		Map<String, Object> valoresDefault = Utils.map(
			"bancoDados", session.getConnection().getMetaData().getURL().replace("jdbc:postgresql://", ""),
			"revenda", licenca.getNomerepresentante() != null ? licenca.getNomerepresentante() : "Multitec",
			"fone", licenca.getTelefonerepresentante() != null ? licenca.getTelefonerepresentante().substring(2, licenca.getTelefonerepresentante().length()) : "4524-9530",
			"ddd", licenca.getTelefonerepresentante() != null ? "("+licenca.getTelefonerepresentante().substring(0, 2)+")" : "(11)",
			"email", licenca.getEmailrepresentante() != null ? licenca.getEmailrepresentante() : "contato@multitecsistemas.com.br"
		)
		
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_ExibirLicenca.html", valoresDefault);
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLURhZG9zIGRhIGxpY2Vuw6dhIiwidGlwbyI6InNlcnZsZXQiLCJ3IjoxMiwiaCI6MiwicmVzaXplIjp0cnVlLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9