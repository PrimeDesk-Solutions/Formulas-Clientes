package Atilatte.dashboards.slm;

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

public class servlet_volumetriaPorCategoria extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		// TODO Auto-generated method stub
		return "Atilatte - Volumetria por Categoria";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 7, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_pagamentoFreteMensal.html");
		List<LocalDate> datas = new ArrayList();
		LocalDate dataAtual = LocalDate.now();
		List<BigDecimal> valoresFreteAgregado = new ArrayList();
		List<BigDecimal> valoresFreteRedesp = new ArrayList();
		List<BigDecimal> meses = new ArrayList();
		
		for(int i = 1; i <=12; i++){
			datas.add(LocalDate.of(dataAtual.year,i,1));
		}

		for(data in datas){
			LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(data);
			def freteAgregado = buscarValorFreteAgregado(mesIniFim);
			def freteRedesp = buscarValorFreteRedespacho(mesIniFim);

			String nomeMes = buscarNomeMes(data.getMonthValue())

			valoresFreteAgregado.add(freteAgregado);
			valoresFreteRedesp.add(freteRedesp);
			meses.add("'"+nomeMes+"'")
	
		}
		
		
	
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"agregados",valoresFreteAgregado,
				"redespachos",valoresFreteRedesp,
				"meses", meses
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}

	private BigDecimal buscarValorFreteAgregado(LocalDate[] mesIniFim){
		String sql = "select sum(bfc1001valor) as freteAgregado from bfc10 "+
 					"inner join bfc1001 on bfc1001carga = bfc10id "+
 					"where bfc10ckodata between :dtInicial and :dtFinal ";
 					
 		Parametro p1 = Parametro.criar("dtInicial",mesIniFim[0]);
 		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);

 		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2);
 		
	}

	private BigDecimal buscarValorFreteRedespacho(LocalDate[] mesIniFim){
		String sql = "select SUM(cast(eaa01json ->> 'valor_frete_redesp' as numeric(18,6))) as freteRedesp "+
					"from bfc10 "+
					"inner join bfc1002 on bfc1002carga = bfc10id "+
					"inner join abb01 on abb01id = bfc1002central "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where abb01data between :dtInicial and :dtFinal ";
		
		Parametro p1 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);

		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2);
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
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gUGFnYW1lbnRvIEZyZXRlIE1lbnNhbCIsInRpcG8iOiJzZXJ2bGV0IiwidyI6NywiaCI6MTIsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9