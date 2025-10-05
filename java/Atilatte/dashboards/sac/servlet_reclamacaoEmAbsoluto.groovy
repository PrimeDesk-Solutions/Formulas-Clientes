package Atilatte.dashboards.sac;


import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity

import org.springframework.http.MediaType

import java.time.LocalDate
import org.apache.commons.text.StringSubstitutor
import org.springframework.http.ResponseEntity
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import sam.core.variaveis.MDate
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;


public class servlet_reclamacoesPorMotivo extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Atilatte - Reclamacao Em Absoluto";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.sac.recurso_reclamacaoEmAbsoluto.html");

		LocalDate dataAtual = LocalDate.now();
		Integer anoAtual = dataAtual.getYear();

		
		List<String> listItens = new ArrayList();
		List<Integer> qtdAno1 = new ArrayList();
		List<Integer> qtdAno2 = new ArrayList();
		List<Integer> qtdAno3 = new ArrayList();
		List<Long> listAnos = new ArrayList();
		
		for(int i = 0; i <=2; i++){
			listAnos.add(anoAtual - i)
		}

		List<TableMap> itens = buscarItens(listAnos);

		for(item in itens){
			String descrItem = item.getString("abm01na");
			Long idItem = item.getLong("abm01id");
			def qt1 = buscarQuantidadePorItem(idItem, listAnos[0]);
			def qt2 = buscarQuantidadePorItem(idItem, listAnos[1]);
			def qt3 = buscarQuantidadePorItem(idItem, listAnos[2]);
			
			qtdAno1.add(qt1);
			qtdAno2.add(qt2);
			qtdAno3.add(qt3);
			

			listItens.add("'"+descrItem+"'")

		}

		
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"ano1",listAnos[0],
				"ano2",listAnos[1],
				"ano3",listAnos[2],
				"qtdAno1",qtdAno1,
				"qtdAno2",qtdAno2,
				"qtdAno3",qtdAno3,
				"itens",listItens
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}
	private Integer buscarQuantidadePorItem(Long idItem, Integer ano){
		String sql = "select count(abm01codigo) as qtd from caa10 "+
						"inner join abm01 on abm01id = caa10item "+
						"where extract('year' from caa10data) = :ano "+
						"and abm01id = :idItem "+
						"group by abm01codigo "+
						"order by abm01codigo "
					
		Parametro p1 = Parametro.criar("ano",ano);
		Parametro p2 = Parametro.criar("idItem",idItem);

		return getAcessoAoBanco().obterInteger(sql,p1,p2);
	}
	
	private List<TableMap> buscarItens(List<Long> listAnos){
		
		String sql = "select abm01codigo,abm01na, abm01id from caa10 "+
						"inner join abm01 on abm01id = caa10item "+
						"where extract('year' from caa10data) in (:listAnos) "+
						"group by abm01codigo,abm01na,abm01id "+
						"order by abm01codigo "
						
		Parametro p1 = Parametro.criar("listAnos", listAnos)

		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1);
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gUmVjbGFtYWNhbyBFbSBBYnNvbHV0byIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==