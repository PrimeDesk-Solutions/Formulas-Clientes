package Atilatte.paineis.srf;


import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.UiDto
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

public class SRF_ServletLotesAFaturar extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "SRF - Lotes a Faturar"
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12,12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		List<String> mensagens = new ArrayList();

		mensagens.add("Lotes a Serem faturados: ");

		List<TableMap> idLotes = buscarLotesAFaturar();

		for(lotes in idLotes){
			mensagens.add("Lote: " + lotes.getString("bfb01lote"));
		}

		Map<String,Object> valores = Utils.map(
			"mensagens",mensagens
		)
		
		UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.srf.SRF_RecursoLotesAFaturar.html",valores);
		
		return ResponseEntity.ok()
			   .contentType(MediaType.APPLICATION_JSON)
			   .body(dto)
	}
	
	private List<TableMap> buscarLotesAFaturar(){
		
		String sql = "SELECT distinct bfb01id,bfb01lote FROM bfb01 "+
					"INNER JOIN bfb0101 ON bfb0101lote = bfb01id "+
					"INNER JOIN bfa01 on bfa01docscv = bfb0101central "+
					"WHERE bfb0101romproc = 1 "+ 
					"AND bfa01status <> 2 "+
					"ORDER BY bfb01id DESC";

		return getAcessoAoBanco().buscarListaDeTableMap(sql);
	}

//	public int minutosEmCache(){
//		return 1;
//	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIExvdGVzIGEgRmF0dXJhciIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==