package Atilatte.paineis.srf;


import java.time.DayOfWeek

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multiorm.ColumnType
import br.com.multiorm.MultiResultSet
import br.com.multiorm.Query
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.aa.Aac01
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto
import sam.server.samdev.utils.Parametro


public class SRF_ServletPainelRomaneio extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "SRF - Painel Romaneio";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 6, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		List<String> mensagens = new ArrayList();
		
		List<TableMap> idLotes = buscarLotesAFaturar();

		for(lote in idLotes){
			List<TableMap> itensRomaneio = buscarItensRomaneio(lote.getLong("bfb01id"));
			String nomeLote = lote.getString("bfb01lote");
			def countItens = 0;
			def countColetado = 0;
			
			for(item in itensRomaneio){
				countItens++;

				// Leite de saquinho não tem coleta
				if(item.getInteger("coleta") == 0) countColetado = countColetado + 1
				
				if(item.getInteger("ajustado") == 1){
					countColetado++;
				}
			}
			
			if(countItens == countColetado){
				mensagens.add("O Lote "+nomeLote.toUpperCase()+ " pode ser faturado, todos os itens já foram coletados.")
			}
			
		}
		

		
		Map<String,Object> valores = Utils.map(
			"mensagens",mensagens
		)
		
		UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.srf.RecursoPainelRomaneio.html",valores)
		return ResponseEntity.ok()
			   .contentType(MediaType.APPLICATION_JSON)
			   .body(dto)
	}
	public int minutosEmCache(){
		return 4;
	}

	private List<TableMap> buscarLotesAFaturar(){
		
		String sql = "SELECT distinct bfb01id,bfb01lote FROM bfb01 "+
					"INNER JOIN bfb0101 ON bfb0101lote = bfb01id "+
					"INNER JOIN bfa01 on bfa01docscv = bfb0101central "+
					"WHERE bfb0101romproc = 1 "+ 
					"AND bfa01status <> 2 ";

		return getAcessoAoBanco().buscarListaDeTableMap(sql);
	}

	private List<TableMap> buscarItensRomaneio(Long idLote){
		
		String sql = "select abm01codigo, abm01na, bfa0101ajustado as ajustado,aba3001descr as categoria, cast(abm0101json ->> 'realiza_coleta' as integer) as coleta "+
					"from bfa01 "+
					"inner join bfa0101 on bfa0101rom = bfa01id "+
					"inner join bfb0101 on bfb0101central = bfa01docscv "+
					"inner join bfb01 on bfb01id = bfb0101lote "+
					"inner join eaa0103 on eaa0103id = bfa0101item "+
					"inner join abm01 on abm01id = eaa0103item "+
					"inner join abm0101 on abm0101item = abm01id "+
					"inner join abm0102 on abm0102item = abm01id " +
					"inner join aba3001 on aba3001id = abm0102criterio " +
					"WHERE bfb01id = :idLote "+
					"and  aba3001criterio = 542858 ";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idLote", idLote))
	}

	
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFBhaW5lbCBSb21hbmVpbyIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==
//meta-sis-eyJkZXNjciI6IlNSRiAtIFBhaW5lbCBSb21hbmVpbyIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==