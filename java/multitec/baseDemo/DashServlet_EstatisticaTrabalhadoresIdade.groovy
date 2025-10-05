package multitec.baseDemo

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import java.time.LocalDate
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

class DashServlet_EstatisticaTrabalhadoresIdade extends ServletBase{

	@Override
	public String getNome() throws Exception {
		return "Multitec - Estatistica Trabalhadores Idade";
	}
	
	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 5, 8, true, null);
	}
	
	@Override
	public ResponseEntity<Object> executar() throws Exception {
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_EstatisticaTrabalhadoresIdade.html")
		
		Integer faixaIdade1 = 0
		Integer faixaIdade2 = 0
		Integer faixaIdade3 = 0
		Integer faixaIdade4 = 0
		Integer faixaIdade5 = 0
		Integer idadeTrabalhador = 0

		List<TableMap> idades = buscarAnos()
		
		for(idade in idades) {
			Integer anoAtual = idade.getDate("dataatual").year
			Integer anoNasc = idade.getDate("abh80nascData").year
			idadeTrabalhador = (anoAtual - anoNasc)
			
			if(idadeTrabalhador >= 18 && idadeTrabalhador <= 30) {
				faixaIdade1++
			}else if(idadeTrabalhador >= 31 && idadeTrabalhador <= 40) {
				faixaIdade2++
			}else if(idadeTrabalhador >= 41 && idadeTrabalhador <= 50) {
				faixaIdade3++
			}else if(idadeTrabalhador >= 51 && idadeTrabalhador <= 60) {
				faixaIdade4++
			}else if(idadeTrabalhador > 60) {
				faixaIdade5++
			}
			idadeTrabalhador = 0	
		}
	
				StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"faixa",[faixaIdade1, faixaIdade2, faixaIdade3, faixaIdade4, faixaIdade5]
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}
	
	private List<TableMap> buscarAnos() {
		
		String sql = " select CURRENT_DATE as dataatual, abh80nascData " +
				  " from abh80 " + obterWherePadrao("Abh80", "WHERE");
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql)
		
	}
	
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjIC0gRXN0YXRpc3RpY2EgVHJhYmFsaGFkb3JlcyBJZGFkZSIsInRpcG8iOiJzZXJ2bGV0IiwidyI6NSwiaCI6OCwicmVzaXplIjp0cnVlLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9