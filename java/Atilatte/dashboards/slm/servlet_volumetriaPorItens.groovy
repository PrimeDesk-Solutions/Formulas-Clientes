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

public class servlet_volumetriaPorItens extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		// TODO Auto-generated method stub
		return "Atilatte - Volumetria por Itens";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 7, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_volumetriaPorItens.html");
		LocalDate dataAtual = LocalDate.now();
		LocalDate[] mesIniFim =  DateUtils.getStartAndEndMonth(dataAtual);
		List<BigDecimal> qtdCaixas = new ArrayList();
		List<String> itens = new ArrayList();
		List<String> listItens = buscarItensDocumentos(mesIniFim);

		for(item in listItens){
			Integer qtdCaixa = buscarQuantidadeDeCaixasPorItem(item,mesIniFim);
			qtdCaixas.add(qtdCaixa)
			itens.add("'"+item+"'");
		}
		
		
	
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"caixas",qtdCaixas,
				"itens", itens
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}
	private List<String> buscarItensDocumentos(LocalDate[] mesIniFim){
		String sql = "select distinct abm01na from eaa0103 "+
					"inner join eaa01 on eaa01id = eaa0103doc  "+
					"inner join abb01 on abb01id = eaa01central  "+
					"inner join abm01 on abm01id = eaa0103item "+
					"where eaa01clasdoc = 1 "+
					"and eaa01esmov = 1 "+
					"and eaa01gc = 1322578 "+
					"and abm01tipo = 1 "+
					"and abb01data between :dtInicial and :dtFinal ";
					
		Parametro p1 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);

		return getAcessoAoBanco().obterListaDeString(sql,p1,p2)
	}

	private Integer buscarQuantidadeDeCaixasPorItem(String item,LocalDate[] mesIniFim){
		String sql = "select sum(cast(eaa0103json ->> 'caixa' as numeric(18))::int) as caixa from eaa0103 "+
					"inner join eaa01 on eaa01id = eaa0103doc  "+
					"inner join abb01 on abb01id = eaa01central  "+
					"inner join abm01 on abm01id = eaa0103item "+
					"where abm01na = :item "+
					"and abb01data between :dtInicial and :dtFinal "+
					"and eaa01clasdoc = 1 "+
					"and eaa01esmov = 1 "+
					"and eaa01gc = 1322578 "+
					"and abm01tipo = 1 ";
		Parametro p1 = Parametro.criar("item",item);
		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);
		Parametro p3 = Parametro.criar("dtInicial",mesIniFim[0]);

		return getAcessoAoBanco().obterInteger(sql,p1,p2,p3);
		
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxsYXRlIC0gVm9sdW1ldHJpYSBwb3IgSXRlbnMiLCJ0aXBvIjoic2VydmxldCIsInciOjcsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==