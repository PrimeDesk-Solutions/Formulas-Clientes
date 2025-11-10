package multitec.baseDemo

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.Utils
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

class DashServlet_EstatisticaTrabalhadoresGenero extends ServletBase{

	@Override
	public String getNome() throws Exception {
		return "Multitec - Estatistica Trabalhadores Gênero";
	}
	
	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 5, 9, true, null);
	}
	
	@Override
	public ResponseEntity<Object> executar() throws Exception {
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_EstatisticaTrabalhadoresGenero.html")
		
		Integer qtdMasculino = buscarGeneroMasculino()
		Integer qtdFeminino = buscarGeneroFeminino()		
		
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
			"genero",[qtdMasculino, qtdFeminino]
			))

	def resolvedString = sub.replace(dto.getScript())
	dto.setScript(resolvedString)
	return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
	
	private Integer buscarGeneroMasculino(){
		
		String sql = " select count(abh80sexo) " +
					 " from abh80 " + 
					 " where abh80sexo = 0 " + obterWherePadrao("Abh80", "AND");
			
		return getAcessoAoBanco().obterInteger(sql)	
	}
	
	private Integer buscarGeneroFeminino(){
	
		String sql = "select count(abh80sexo) " +
					 " from abh80 " +
					 " where abh80sexo = 1 " + obterWherePadrao("Abh80", "AND");
					 
		return getAcessoAoBanco().obterInteger(sql)
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjIC0gRXN0YXRpc3RpY2EgVHJhYmFsaGFkb3JlcyBHw6puZXJvIiwidGlwbyI6InNlcnZsZXQiLCJ3Ijo1LCJoIjo5LCJyZXNpemUiOnRydWUsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=