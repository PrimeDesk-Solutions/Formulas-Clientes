package Atilatte.dashboards.slm;

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.Utils
import br.com.multitec.utils.DateUtils
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto
import java.time.LocalDate
import br.com.multitec.utils.collections.TableMap;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import sam.server.samdev.utils.Parametro

public class servlet_entregasNoPrazo extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Multitec-Valor do Estoque";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 5, 7, false, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		LocalDate dataAtual = LocalDate.now();
		LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(dataAtual);
		Integer totEntregas = buscarTotalEntrega(mesIniFim);
		List<TableMap> documentos = buscarDocumentos(mesIniFim);
		Integer countPrazo = 0

		for(TableMap registro : documentos ){
			String codRedesp = registro.getString("codigoRedespNota");
			def dataEntrega = codRedesp == null ? registro.getDate("dataEntregaNota") : registro.getDate("dtEntregaRedesp") ;
			def prazo = registro.getInteger("prazo");
			def dataEmissaoNota = registro.getDate("dataEmissaoNota");
			def dataEmissaoPed =  registro.getDate("dataEmissaoPed");
			LocalDate dataPrevista = null;
			String situacao = ""; 
			

			// Recupera o ano atual
			int anoAtual = LocalDate.now().getYear();
				
			List<LocalDate> feriados = new ArrayList<>();

			// Feriados do Ano
			feriados.add(LocalDate.of(anoAtual,Month.JANUARY,1));
			feriados.add(LocalDate.of(anoAtual,Month.FEBRUARY,20));
			feriados.add(LocalDate.of(anoAtual,Month.FEBRUARY,21));
			feriados.add(LocalDate.of(anoAtual,Month.APRIL,7));
			feriados.add(LocalDate.of(anoAtual,Month.APRIL,21));
			feriados.add(LocalDate.of(anoAtual,Month.MAY	,1));
			feriados.add(LocalDate.of(anoAtual,Month.JUNE,8));
			feriados.add(LocalDate.of(anoAtual,Month.SEPTEMBER,7));
			feriados.add(LocalDate.of(anoAtual,Month.OCTOBER,12));
			feriados.add(LocalDate.of(anoAtual,Month.NOVEMBER,2));
			feriados.add(LocalDate.of(anoAtual,Month.NOVEMBER,15));
			feriados.add(LocalDate.of(anoAtual,Month.DECEMBER,25));

			// Busca quantidade de dias uteis da data do pedido até o faturamento da nota
			int diasPedido = contarDiasUteis(dataEmissaoPed,dataEmissaoNota, feriados);
			 
			// Define o prazo de entrega da nota de acordo com a data de entrega 
			if(prazo != null){
				// Data prevista de entrega da nota (soma a data de emissao da nota mais os dias uteis no município)
				dataPrevista = somarDiasUteis(dataEmissaoNota,prazo,feriados);

				if(dataEntrega != null){
					if(dataPrevista >= dataEntrega){
						countPrazo += 1;
					}	
				}
				
				registro.put("dataPrevista",dataPrevista);
			}
			int diasUteisEntrega = contarDiasUteis(dataPrevista,dataEntrega, feriados);

			
			registro.put("tempoProcPed",diasPedido);
			registro.put("dataEntrega",dataEntrega);
			registro.put("prazoReal",diasUteisEntrega + prazo)
		}

		int percentualNoPrazo = 0;
		
		if(countPrazo > 0 && totEntregas > 0  ){
			percentualNoPrazo = (countPrazo / totEntregas) * 100;
		}

		
		Map<String, Object> valoresDefault = Utils.map("totalEntregas",countPrazo,
												"noPrazo",percentualNoPrazo)
		
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_entregasNoPrazo.html", valoresDefault);
						
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

	private List<TableMap> buscarDocumentos(LocalDate[] mesIniFim){
		String sql = "select distinct abb01nota.abb01data as dataEmissaoNota,abb01nota.abb01num,aag02uf, aag0201nome,redesp.abe01codigo as codigoRedespNota,abb01pedido.abb01data as dataEmissaoPed, "+
					"cast(aag0201json ->> 'prazo' as integer) as prazo, eaa01nota.eaa01dtentrega as dataEntregaNota, TO_DATE(cast(eaa01nota.eaa01json ->> 'dt_entrega_redesp' as text), 'YYYYMMDD') as dtEntregaRedesp "+
					"from eaa01 as eaa01nota "+
					"inner join abb01 as abb01nota on abb01nota.abb01id = eaa01nota.eaa01central  "+
					"inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent "+
					"inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
					"inner join aag0201 on aag0201id = abe0101municipio "+
					"inner join aag02 on aag02id = aag0201uf "+
					"inner join eaa0102 on eaa0102doc = eaa01nota.eaa01id "+
					"inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id "+
					"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
					"inner join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id "+
					"inner join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv "+
					"inner join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc "+
					"inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01nota.eaa01central "+
					"where eaa01nota.eaa01clasdoc = 1 "+
					"and eaa01nota.eaa01cancdata is null "+
					"and (eaa01nota.eaa01dtentrega is not null or cast(eaa01nota.eaa01json ->> 'dt_entrega_redesp' as text) is not null ) "+
					"and (eaa01nota.eaa01dtentrega  between :dataInicio and :dataFim  or cast(eaa01nota.eaa01json ->> 'dt_entrega_redesp' as date) between :dataInicio and :dataFim) "

		Parametro p1 = Parametro.criar("dataInicio",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dataFim",mesIniFim[1]);
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2)





	}

	private LocalDate somarDiasUteis(LocalDate dataInicial,int diasASomar, feriados){
		LocalDate dataAtual = dataInicial;
		int diasSomados = 0;

		while(diasSomados < diasASomar){
			dataAtual = dataAtual.plusDays(1);

			if(dataAtual.getDayOfWeek() != DayOfWeek.SATURDAY &&
			   dataAtual.getDayOfWeek() != DayOfWeek.SUNDAY && !feriados.contains(dataAtual)){
				diasSomados ++;
			}

		}
		return dataAtual;		
	}

	private contarDiasUteis(LocalDate dataInicial,LocalDate dataFinal, feriados){
		LocalDate dataAtual = dataInicial;
		
		int diasUteis = 0
		while(dataAtual < dataFinal){
			dataAtual = dataAtual.plusDays(1);
			if(dataAtual.getDayOfWeek() != DayOfWeek.SATURDAY &&
			   dataAtual.getDayOfWeek() != DayOfWeek.SUNDAY && !feriados.contains(dataAtual)){
				diasUteis++;
			}
		}

		return diasUteis;	
	}
	
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gRW50cmVnYXMgTm8gUHJhem8iLCJ0aXBvIjoic2VydmxldCIsInciOjUsImgiOjcsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9