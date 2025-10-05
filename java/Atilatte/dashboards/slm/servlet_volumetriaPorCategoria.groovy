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
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_volumetriaPorCategoria.html");
		LocalDate dataAtual = LocalDate.now();
		LocalDate[] mesIniFim =  DateUtils.getStartAndEndMonth(dataAtual);
		List<BigDecimal> qtdCaixas = new ArrayList();
		List<String> categorias = new ArrayList();
		List<String> listCategorias = buscarCategoriaDocumentos(mesIniFim);

		for(categoria in listCategorias){
			Integer qtdCaixa = buscarQuantidadeDeCaixasPorCategoria(categoria,mesIniFim);
			qtdCaixas.add(qtdCaixa)
			categorias.add("'"+categoria+"'");
		}
		
		
	
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"caixas",qtdCaixas,
				"categorias", categorias
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}
	private List<String>buscarCategoriaDocumentos(LocalDate[] mesIniFim){
		String sql = "select aba3001descr from aba30 "+
					"inner join aba3001 on aba3001criterio = aba30id "+
					"where aba30id = 542858 ";
					
		return getAcessoAoBanco().obterListaDeString(sql)
	}

	private Integer buscarQuantidadeDeCaixasPorCategoria(String categoria ,LocalDate[] mesIniFim){
		String sql = "select sum(cast(eaa0103json ->> 'caixa' as numeric(18))::int) as caixa from eaa0103  "+
					"inner join eaa01 on eaa01id = eaa0103doc   "+
					"inner join abb01 on abb01id = eaa01central   "+
					"inner join abm01 on abm01id = eaa0103item  "+
					"inner join abm0102 on abm0102item = abm01id "+
					"inner join aba3001 on aba3001id = abm0102criterio "+
					"where aba3001descr = :categoria "+
					"and abb01data between :dtInicial and :dtFinal "+
					"and eaa01clasdoc = 1  "+
					"and eaa01esmov = 1  "+
					"and eaa01gc = 1322578  "+
					"and abm01tipo = 1  "
					
		Parametro p1 = Parametro.criar("categoria",categoria);
		Parametro p2 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p3 = Parametro.criar("dtFinal",mesIniFim[1]);

		return getAcessoAoBanco().obterInteger(sql,p1,p2,p3);
		
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gVm9sdW1ldHJpYSBwb3IgQ2F0ZWdvcmlhIiwidGlwbyI6InNlcnZsZXQiLCJ3Ijo3LCJoIjoxMiwicmVzaXplIjoidHJ1ZSIsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=