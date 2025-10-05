package Atilatte.dashboards.slm;

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto
import java.time.LocalDate;
import sam.server.samdev.utils.Parametro

public class DashServlet_EstoqueAbaixoMinimo extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Valor do Estoque";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 3, 7, false, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		
		LocalDate dataAtual = LocalDate.now();
		LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(dataAtual);
		Integer totEntregas = buscarTotalEntrega(mesIniFim);
		Map<String, Object> valoresDefault = Utils.map("totalEntregas",totEntregas)
		
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_totalEntrega.html", valoresDefault);
						
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}

	private Integer buscarTotalEntrega(LocalDate[] mesIniFim ){
		String sql = "select count(*) "+
					"from eaa01 "+
					"inner join abb01 on abb01id = eaa01central "+
					//"inner join eaa0103 on eaa0103doc = eaa01id "+
					//"inner join eaa01032 on eaa01032itemsrf = eaa0103id "+
					"where (eaa01dtentrega is not null or cast(eaa01json ->> 'dt_entrega_redesp' as text) is not null) "+
					"and (eaa01dtentrega  between :dataInicio and :dataFim or cast(eaa01json ->> 'dt_entrega_redesp' as date) between :dataInicio and :dataFim) " 
					
		Parametro p1 = Parametro.criar("dataInicio",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dataFim",mesIniFim[1]);
		
					
		return getAcessoAoBanco().obterInteger(sql,p1,p2);
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gVG90YWwgZGUgRW50cmVnYXMiLCJ0aXBvIjoic2VydmxldCIsInciOjMsImgiOjcsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9