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
		return "Atilatte - Reclamacao Por Mes";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.sac.recurso_reclamacoesPorMes.html");

		LocalDate dataAtual = LocalDate.now();
		Integer anoAtual = dataAtual.getYear();
		List<LocalDate> datas = new ArrayList();

		
		List<String> listMeses = new ArrayList();
		List<Integer> qtdAno1 = new ArrayList();
		List<Integer> qtdAno2 = new ArrayList();
		List<Integer> qtdAno3 = new ArrayList();
		List<Long> listAnos = new ArrayList();
		
		for(int i = 0; i <=2; i++){
			listAnos.add(anoAtual - i)
		}

		for(int mes = 1; mes <= 12; mes++){
			Integer qtd1 = buscarQuantidadePorMeseAno(mes,listAnos[0]);
			Integer qtd2 = buscarQuantidadePorMeseAno(mes,listAnos[1]);
			Integer qtd3 = buscarQuantidadePorMeseAno(mes,listAnos[2]);

			String nomeMes = buscarNomeMes(mes);

			listMeses.add("'"+nomeMes+"'")
			
			qtdAno1.add(qtd1);
			qtdAno2.add(qtd2);
			qtdAno3.add(qtd3);
			
		}

		
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"ano1",listAnos[0],
				"ano2",listAnos[1],
				"ano3",listAnos[2],
				"qtdAno1",qtdAno1,
				"qtdAno2",qtdAno2,
				"qtdAno3",qtdAno3,
				"meses",listMeses
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}
	private Integer buscarQuantidadePorMeseAno(Integer mes,Integer ano){
		String sql = "select count(*) from caa10 "+
					"where extract(month from caa10data) = :mes "+
					"and extract(year from caa10data) = :ano "
					
		Parametro p1 = Parametro.criar("mes",mes);
		Parametro p2 = Parametro.criar("ano",ano);

		return getAcessoAoBanco().obterInteger(sql,p1,p2);
	}
	
	private String buscarNomeMes(Integer numMes){
		
		String nomeMes
		switch(numMes){
			case 1:
				nomeMes = "Janeiro";
				break;
			case 2: 
				nomeMes = "Fevereiro";
				break;
			case 3: 
				nomeMes = "Março";
				break;
			case 4: 
				nomeMes = "Abril";
				break;
			case 5: 
				nomeMes = "Maio";
				break;
			case 6: 
				nomeMes = "Junho";
				break;
			case 7: 
				nomeMes = "Julho";
				break;
			case 8:
				nomeMes = "Agosto";
				break;
			case 9:
				nomeMes = "Setembro";
				break;
			case 10: 
				nomeMes = "Outubro";
				break;
			case 11:
				nomeMes = "Novemmbro";
				break;
			case 12: 
				nomeMes = "Dezembro";
				break;				
		}
		return nomeMes;
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gUmVjbGFtYWNhbyBQb3IgTWVzIiwidGlwbyI6InNlcnZsZXQiLCJ3IjoxMiwiaCI6MTIsInJlc2l6ZSI6ImZhbHNlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==