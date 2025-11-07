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
import br.com.multitec.utils.collections.TableMap;

public class servlet_pagamentoFreteSemanal extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Atilatte - Pagamento Frete Semanal";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 7, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_pagamentoFreteSemanal.html");
		List<BigDecimal> valoresAgregados = new ArrayList();
		List<BigDecimal> valoresRedespacho = new ArrayList();
		List<String> semanas = new ArrayList();
		LocalDate dataAtual = LocalDate.now();
		LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(dataAtual);

		for(int i = 1; i <= 5; i++){
			TableMap tmFreteAgregado = buscarFreteAgregado(mesIniFim, i);
			TableMap tmFreteRedesp = buscarFreteRedespacho(mesIniFim,i);
			String nomeSemana = buscarNomeSemana(i);
			def vlrAgregado = tmFreteAgregado != null ? tmFreteAgregado.getBigDecimal_Zero("freteAgregado") : new BigDecimal(0);
			def vlrRedesp = tmFreteRedesp != null ? tmFreteRedesp.getBigDecimal_Zero("freteRedesp") : new BigDecimal(0);
			
			valoresAgregados.add(vlrAgregado);
			valoresRedespacho.add(vlrRedesp);
			semanas.add("'"+nomeSemana+"'");
			
		}
		
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"agregados",valoresAgregados,
				"redespachos",valoresRedespacho,
				"semanas", semanas
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}

	private TableMap buscarFreteAgregado(LocalDate[] mesIniFim,Integer i){
		String sql = "select EXTRACT(DOW FROM bfc10ckodata) as semana,sum(bfc1001valor) as freteAgregado from bfc10 "+
					"inner join bfc1001 on bfc1001carga = bfc10id "+
					"where bfc10ckodata between :dtInicial and :dtFinal "+
					"and EXTRACT(DOW FROM bfc10ckodata) = :numSemana "+
					"group by semana " 
					"order by semana " 
					
		Parametro p1 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);
		Parametro p3 = Parametro.criar("numSemana", i);

		return getAcessoAoBanco().buscarUnicoTableMap(sql,p1,p2,p3);
		
	
		
	}
	private TableMap buscarFreteRedespacho(LocalDate[] mesIniFim,Integer i){
		

		String sql = "select EXTRACT(DOW FROM abb01data) as semana ,SUM(cast(eaa01json ->> 'valor_frete_redesp' as numeric(18,6))) as freteRedesp "+
					"from bfc10 "+
					"inner join bfc1002 on bfc1002carga = bfc10id "+ 
					"inner join abb01 on abb01id = bfc1002central "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where abb01data between :dtInicial and :dtFinal "+
					"and EXTRACT(DOW FROM abb01data) = :numSemana "+
					"group by semana "+
					"order by semana ";
					
		Parametro p1 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);
		Parametro p3 = Parametro.criar("numSemana", i);

		return getAcessoAoBanco().buscarUnicoTableMap(sql,p1,p2,p3);
	}

	private String buscarNomeSemana(Integer i){
		String nomeSemana = "";
		switch(i){
			case 1 :
				nomeSemana = "Segunda";
				break;
			case 2 : 
				nomeSemana = "Terça"
				break;
			case 3 :
				nomeSemana = "Quarta";
				break;
			case 4 :
				nomeSemana = "Quinta";
				break;
			case 5 :
				nomeSemana = "Sexta";
				break;
		}
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gUGFnYW1lbnRvIEZyZXRlIFNlbWFuYWwiLCJ0aXBvIjoic2VydmxldCIsInciOjYsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==