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


public class SRF_RotaPedidoDeVenda extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "SRF - Rota de Pedido de Venda";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 6, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		List<String> mensagens = new ArrayList();
		
		def user = obterUsuarioLogado();

	     Long idUser = user.aab10id;

	     List<TableMap> dados = buscarDocumentos(idUser); 

		for(dado in dados){
			Integer numPedido = dado.get("numPedido");
			Integer numNota = dado.getInteger("numNota");
			Long idDoc = dado.getLong("eaa01id");
			

			def dtEntrega = dado.getDate("dtEntrega")
			String mensagem = "O documento Nº "+numNota+" referente ao pedido Nº "+numPedido+" já foi emitido.";

			if(dtEntrega != null){
				mensagem = "O documento Nº "+numNota+" referente ao pedido Nº "+numPedido+" já foi entregue."
			}

			mensagens.add(mensagem);

			List<Long>itens = buscarItensDoc(idDoc);

			for(item in itens){
				TableMap tmDev =  buscarDevolucoes(item)
				String codItem = "";
				String descrItem = "";
				def qtd;
				Integer docDev;
				if(tmDev != null){
					codItem = tmDev.getString("abm01codigo");
					descrItem = tmDev.getString("abm01na");
					qtd = tmDev.getBigDecimal_Zero("eaa0103qtComl");
					docDev = tmDev.getInteger("numDocDev");
					mensagem = "O item "+codItem+" do documento Nº "+numNota+" foi devolvido "+qtd.round(2)+" quantidades.(Documento devolução Nº "+docDev+")"
					mensagens.add(mensagem);
				}
			}	
		}

		
		Map<String,Object> valores = Utils.map(
			"mensagens",mensagens
		)
		
		UiDto dto = buscarComponenteCustomizado("Atilatte.paineis.srf.RecursoRotaPedidoDeVenda.html",valores)
		return ResponseEntity.ok()
			   .contentType(MediaType.APPLICATION_JSON)
			   .body(dto)
	}
	private buscarDocumentos(Long idUser){
		String sql = "select max(abb01nota.abb01num) as numNota, abb01pedido.abb01num as numPedido,eaa01nota.eaa01id as eaa01id,eaa01nota.eaa01dtentrega as dtEntrega "+
					"from eaa01 as eaa01nota  "+
					"inner join abb01 as abb01nota on eaa01nota.eaa01central = abb01nota.abb01id "+
					"inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id "+
					"inner join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id "+
					"inner join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv "+
					"inner join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc "+
					"inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central  "+
					"where abb01pedido.abb01operUser = :idUser "+
					"group by eaa01nota.eaa01id,abb01nota.abb01num,abb01pedido.abb01num,eaa01nota.eaa01dtentrega "+
					"order by abb01nota.abb01num desc "+
					"limit 3";
		Parametro parametroUser = Parametro.criar("idUser",idUser) 
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroUser);
	}

	private List<Long> buscarItensDoc(Long idDoc){
		String sql = "select eaa0103id from eaa0103 where eaa0103doc = :idDoc";
		
		Parametro p1 = Parametro.criar("idDoc",idDoc);

		return getAcessoAoBanco().obterListaDeLong(sql,p1);
	}

	private TableMap buscarDevolucoes(Long idItem){
		String sql = "select eaa0103qtComl,abm01codigo, abm01na,abb01num as numDocDev "+
					"from eaa01033 "+
					"inner join eaa0103 on eaa0103id = eaa01033item "+
					"inner join abm01 on abm01id = eaa0103item "+
					"inner join eaa01 on eaa01id = eaa0103doc "+
					"inner join abb01 on abb01id = eaa01central "+
					"where eaa01033itemDoc = :idItem ";
					
		Parametro p1 = Parametro.criar("idItem",idItem);

		return getAcessoAoBanco().buscarUnicoTableMap(sql,p1);
	}

	
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJvdGEgZGUgUGVkaWRvIGRlIFZlbmRhIiwidGlwbyI6InNlcnZsZXQiLCJ3IjoxMiwiaCI6MTIsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9