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


class servlet_entregasPorVeiculo extends ServletBase{

	@Override
	public String getNome() throws Exception {
		// TODO Auto-generated method stub
		return "Atilatte - Entregas Por Veiculo";
	}
	
	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 7, 2, true, null);
	}
	
	@Override
	public ResponseEntity<Object> executar() throws Exception {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_entregasPorVeiculo.html");
		
		LocalDate dataAtual = LocalDate.now();
		LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(dataAtual)
		List<Integer> listQtdEntregas = new ArrayList();
		List<String> listVeiculos = buscarVeiculos(mesIniFim);
		List<String> veiculos = new ArrayList();


		for(veiculo in listVeiculos){
			Integer qtdEntregas = buscarQuantidadeDeentregas(veiculo,mesIniFim)
			listQtdEntregas.add(qtdEntregas)
			veiculos.add("'"+veiculo+"'")
		}
		
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"entregas",listQtdEntregas,
				"veiculos", veiculos
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}

	private List<String> buscarVeiculos(LocalDate[] mesIniFim){
		String sql = "select distinct aah20nome "+
					"from bfc10 "+
					"inner join bfc1002 on bfc1002carga = bfc10id "+
					"inner join abb01 on abb01id = bfc1002central "+
					"inner join aah20 on aah20id = bfc10veiculo "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where bfc1002entrega = 1 "+
					"and eaa01dtentrega between :dtInicial and :dtFinal ";
					
		Parametro p1 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);
					
		return getAcessoAoBanco().obterListaDeString(sql,p1,p2)
	}

	private Integer buscarQuantidadeDeentregas(String nomeVeiculo,LocalDate[] mesIniFim){
		String sql = "select count(aah20nome) "+
					"from bfc10 "+
					"inner join bfc1002 on bfc1002carga = bfc10id "+
					"inner join abb01 on abb01id = bfc1002central "+
					"inner join aah20 on aah20id = bfc10veiculo "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where bfc1002entrega = 1 "+
					"and eaa01dtentrega between :dtInicial and :dtFinal "+
					"and aah20nome = :nomeVeiculo "
					
		Parametro p1 = Parametro.criar("nomeVeiculo",nomeVeiculo);
		Parametro p2 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p3 = Parametro.criar("dtFinal",mesIniFim[1]);
					
		return getAcessoAoBanco().obterInteger(sql,p1,p2,p3)
	}
			
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gRW50cmVnYXMgUG9yIFZlaWN1bG8iLCJ0aXBvIjoic2VydmxldCIsInciOjcsImgiOjIsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9