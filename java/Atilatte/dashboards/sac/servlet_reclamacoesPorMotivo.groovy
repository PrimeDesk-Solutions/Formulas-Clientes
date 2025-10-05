package Atilatte.dashboards.sac;


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


public class servlet_reclamacoesPorMotivo extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Atilatte - Reclamções Por Motivos";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 6, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.sac.recurso_ReclamacaoPorMotivo.html");

		LocalDate dataAtual = LocalDate.now();
		Integer anoAtual = dataAtual.getYear();
		Integer anoAnterior = anoAtual - 1;
		List<String> listReclamacoes = new ArrayList();
		List<Integer> qtdAnoAtual = new ArrayList();
		List<Integer> qtdAnoAnterior = new ArrayList();

		List<TableMap> reclamacoes = buscarReclamacoes();

		for(reclamacao in reclamacoes ){
			Long idReclamacao = reclamacao.getBigDecimal_Zero("caa01id");
			String descrReclamacao = reclamacao.getString("reclamacao");
			def qtdAtual = buscarQuantidadeAnoAtual(anoAtual,idReclamacao);
			def qtdAnterior = buscarQuantidadeAnoAnterior(anoAnterior,idReclamacao);

			qtdAnoAtual.add(qtdAtual);
			
			qtdAnoAnterior.add(qtdAnterior);

			listReclamacoes.add("'"+descrReclamacao+"'");
		}

		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"anoAnterior",anoAnterior,
				"anoAtual",anoAtual,
				"qtdAtual",qtdAnoAtual,
				"qtdAnterior",qtdAnoAnterior,
				"reclamacoes", listReclamacoes
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
	}
	private Integer buscarQuantidadeAnoAtual(Integer anoAtual, Long idReclamacao){
		String sql = "select count(caa01descr) as total from caa10 "+
					"inner join caa1001 on caa1001atend = caa10id "+
					"inner join caa01 on caa01id = caa1001rec "+
					"where extract('year' from caa10data) = :anoAtual "+
					"and caa01id = :idReclamacao " +
					"group by caa01descr "+
					"order by total "
					
		Parametro p1 = Parametro.criar("anoAtual",anoAtual);
		Parametro p2 = Parametro.criar("idReclamacao",idReclamacao);

		return getAcessoAoBanco().obterInteger(sql,p1,p2);
	}
	private Integer buscarQuantidadeAnoAnterior(Integer anoAnterior, Long idReclamacao){
		String sql = "select count(caa01descr) as total from caa10 "+
					"inner join caa1001 on caa1001atend = caa10id "+
					"inner join caa01 on caa01id = caa1001rec "+
					"where extract('year' from caa10data) = :anoAnterior "+
					"and caa01id = :idReclamacao " +
					"group by caa01descr "+
					"order by total "
					
		Parametro p1 = Parametro.criar("anoAnterior",anoAnterior);
		Parametro p2 = Parametro.criar("idReclamacao",idReclamacao);

		return getAcessoAoBanco().obterInteger(sql,p1,p2);
	}
	private List<TableMap> buscarReclamacoes(){
		String sql = "select distinct caa01descr as reclamacao,caa01id "+
					"from caa10 "+
					"inner join caa1001 on caa1001atend = caa10id "+
					"inner join caa01 on caa01id = caa1001rec";
		return getAcessoAoBanco().buscarListaDeTableMap(sql);
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gUmVjbGFtw6fDtWVzIFBvciBNb3Rpdm9zIiwidGlwbyI6InNlcnZsZXQiLCJ3Ijo2LCJoIjoxMiwicmVzaXplIjoidHJ1ZSIsInRpcG9kYXNoYm9hcmQiOiJjb21wb25lbnRlIn0=