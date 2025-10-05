package Atilatte.paineis.scv;



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

public class SRF_ServletPedidosSemRedespacho extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "SCV - Pedidos Sem Redespacho";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		interromper("teste")
		/*
		List<String> mensagens = new ArrayList();
		Long idEmpresa = obterEmpresaAtiva().getAac10id();
		
		List<TableMap> dados = getAcessoAoBanco().buscarListaDeTableMap("select abb01num, to_char(abb01data,'dd/mm/yyyy') as data, abe01codigo, abe01na " + 
															"FROM eaa01 "+
															"INNER JOIN abb01 ON abb01id = eaa01central "+
															"INNER JOIN abe01 ON abe01id = abb01ent "+
															"INNER JOIN eaa0102 on eaa0102doc = eaa01id "+
															"WHERE eaa0102redespacho IS NULL "+
															"AND eaa01clasDoc = 0 "+
															"AND eaa01esMov = 1 "+
															"AND abb01data >= '2024-04-22' "+
															"and eaa01gc = " + idEmpresa);
		for(dado in dados){
			mensagens.add("O Pedido Nº "+dado.getInteger("abb01num")+" encontra-se sem redespacho informado. Data: " + dado.getString("data") + " Cliente: "+ dado.getString("abe01codigo") +"-"+ dado.getString("abe01na")+".")
		}


		
		Map<String,Object> valores = Utils.map(
			"mensagens",mensagens
		)

		
		UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.scv.SCV_RecursoPedidosSemRedespacho.html",valores)
		
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
			*/
	}

}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MgU2VtIFJlZGVzcGFjaG9zIiwidGlwbyI6InNlcnZsZXQiLCJ3IjoxMiwiaCI6MTIsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9