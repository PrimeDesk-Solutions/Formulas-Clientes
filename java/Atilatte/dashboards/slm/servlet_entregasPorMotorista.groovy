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


class servlet_entregasPorMotorista extends ServletBase{

	@Override
	public String getNome() throws Exception {
		// TODO Auto-generated method stub
		return "Atilatte - Entrega por Motoristas";
	}
	
	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 7, 4, true, null);
	}
	
	@Override
	public ResponseEntity<Object> executar() throws Exception {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.slm.recurso_entregasPorMotorista.html");
		
		LocalDate dataAtual = LocalDate.now();
		LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(dataAtual)
		List<Integer> listQtdEntregas = new ArrayList();
		List<String> listMotoristas = buscarMotoristas(mesIniFim);
		List<String> motoristas = new ArrayList();


		for(motorista in listMotoristas){
			Integer qtdEntregas = buscarQuantidadeDeentregas(motorista,mesIniFim)
			listQtdEntregas.add(qtdEntregas)
			motoristas.add("'"+motorista+"'")
		}
		
		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"entregas",listQtdEntregas,
				"motoristas", motoristas
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}

	private List<String> buscarMotoristas(LocalDate[] mesIniFim){
		String sql = "select distinct aah21nome "+
					"from bfc10 "+
					"inner join bfc1002 on bfc1002carga = bfc10id "+
					"inner join abb01 on abb01id = bfc1002central "+
					"inner join aah21 on aah21id = bfc10motorista "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where bfc1002entrega = 1 "+
					"and eaa01dtentrega between :dtInicial and :dtFinal ";
					
		Parametro p1 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p2 = Parametro.criar("dtFinal",mesIniFim[1]);
					
		return getAcessoAoBanco().obterListaDeString(sql,p1,p2)
	}

	private Integer buscarQuantidadeDeentregas(String nomeMotorista,LocalDate[] mesIniFim){
		String sql = "select count(aah21nome) "+
					"from bfc10 "+
					"inner join bfc1002 on bfc1002carga = bfc10id "+
					"inner join abb01 on abb01id = bfc1002central "+
					"inner join aah21 on aah21id = bfc10motorista "+
					"inner join eaa01 on eaa01central = abb01id "+
					"where bfc1002entrega = 1 "+
					"and eaa01dtentrega between :dtInicial and :dtFinal "+
					"and aah21nome = :nomeMotorista "
					
		Parametro p1 = Parametro.criar("nomeMotorista",nomeMotorista);
		Parametro p2 = Parametro.criar("dtInicial",mesIniFim[0]);
		Parametro p3 = Parametro.criar("dtFinal",mesIniFim[1]);
					
		return getAcessoAoBanco().obterInteger(sql,p1,p2,p3)
	}
			
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gRW50cmVnYSBwb3IgTW90b3Jpc3RhcyIsInRpcG8iOiJzZXJ2bGV0IiwidyI6NywiaCI6NCwicmVzaXplIjoidHJ1ZSIsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=