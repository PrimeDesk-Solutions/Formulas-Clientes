package Atilatte.dashboards.srf;


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
import java.util.ArrayList;
import java.time.format.DateTimeFormatter


public class servlet_VolumetriaItemDiaria extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Atilatte - Volumetria Diaria Item";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		

		LocalDate dataAtual = LocalDate.now();

		LocalDate dataAuxiliar = LocalDate.of(dataAtual.year,dataAtual.getMonthValue(),dataAtual.day + 1)
		
		List<Integer> qtdCaixas1 = new ArrayList();
		List<Integer> qtdCaixas2 = new ArrayList();
		List<Integer> qtdCaixas3 = new ArrayList();
		List<String> dias = new ArrayList();
		List<LocalDate> datas = new ArrayList();
		
		
		for(int i = 5 ; i >= 1; i--){
			LocalDate data = dataAuxiliar.minusDays(i)
			Integer totalCaixa1 = buscarQuantidadeCaixaPorCategoria("LEITE",data);
			Integer totalCaixa2 = buscarQuantidadeCaixaPorCategoria("QUEIJO",data);
			Integer totalCaixa3 = buscarQuantidadeCaixaPorCategoria("IOGURTE",data);
			
			qtdCaixas1.add(totalCaixa1);
			qtdCaixas2.add(totalCaixa2);
			qtdCaixas3.add(totalCaixa3);
			
			dias.add(data.day.toString());
			datas.add(data);
			
		}

		
		

		String titulo = "Teste"
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"qtdCaixas1",qtdCaixas1,
				"qtdCaixas2",qtdCaixas2,
				"qtdCaixas3",qtdCaixas3,
				"dias",dias
				))
		String dtIni = datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
		String dtFin = dataAtual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
		
		Map<String, Object> valoresDefault = Utils.map("teste","("+dtIni +" - "+ dtFin+")")
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.srf.recurso_VolumetriaItemDiaria.html",valoresDefault);

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}
	private Integer buscarQuantidadeCaixaPorCategoria(String categoria,LocalDate data){
		
		String sql = 	"select sum(cast(eaa0103json ->> 'caixa' as numeric(18))::int) as caixa from eaa0103  "+
					"inner join eaa01 on eaa01id = eaa0103doc   "+
					"inner join abb01 on abb01id = eaa01central   "+
					"inner join abm01 on abm01id = eaa0103item  "+
					"inner join abm0102 on abm0102item = abm01id "+
					"inner join aba3001 on aba3001id = abm0102criterio "+ 
					"where UPPER(aba3001descr) = :categoria "+
					"and abb01data = :data "+
					"and eaa01clasdoc = 1  "+
					"and eaa01esmov = 1  "+
					"and eaa01gc = 1322578 "+ 
					"and abm01tipo = 1 "
		
	
		return getAcessoAoBanco().obterInteger(sql,Parametro.criar("categoria",categoria.toUpperCase()),Parametro.criar("data",data))
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gVm9sdW1ldHJpYSBEaWFyaWEgSXRlbSIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==