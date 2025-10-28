package multitec.baseDemo

import org.springframework.http.ResponseEntity

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.Criteria
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.politica.OperacaoDeSeguranca
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb0103
import sam.model.entities.bf.Bfa01
import sam.model.entities.cb.Cbb01
import sam.model.entities.cb.Cbd01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ab.Abb0104
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.MediaType
import sam.server.samdev.relatorio.UiDto

class CGS_DocumentosWorkflow extends ServletBase {

	@Override
	public ResponseEntity<Object> executar() throws Exception {
		Integer numDoc = session.createCriteria(Abb0104.class)
				   .addFields("count(abb01id)")
				   .addJoin(Joins.join("abb0104wf").alias("aah40"))
				   .addJoin(Joins.join("abb0104central").alias("abb01"))
				   .addJoin(Joins.join("abb01.abb01tipo").alias("aah01"))
				   .addJoin(Joins.join("abb0104ent").alias("abe01exc").left(true))
				   .addJoin(Joins.join("abb0104item").alias("abm01").left(true))
				   .addJoin(Joins.join("abb01.abb01ent").alias("abe01doc").left(true))
				   .addJoin(Joins.join("abb01.abb01operCod").alias("abb10").left(true))
				   .addWhere(Criterions.eq("abb01status", Abb01.STATUS_NORMAL))
				   .addWhere(Criterions.lt("abb0104status", Abb0104.STATUS_CONCLUIDO))
				   .addWhere(Criterions.where(" abb0104id IN (SELECT abb01041tab1.abb01041wf FROM Abb01041 AS abb01041tab1 INNER JOIN Aah30 ON abb01041tab1.abb01041ativ = aah30id WHERE abb01041tab1.abb01041data IS NULL AND abb01041tab1.abb01041wf = abb0104id AND " 
				   + variaveis.getPermissoes().montarWherePelaColuna("aah30psExec", OperacaoDeSeguranca.FEEDBACK_WORKFLOW) 
				   + " AND ( abb01041tab1.abb01041seqAnt IS NULL OR (SELECT COUNT(*) FROM Abb01041 AS abb01041tab2 WHERE abb01041tab2.abb01041wf = abb0104id AND abb01041tab2.abb01041seq = abb01041tab1.abb01041seqAnt AND abb01041tab2.abb01041data IS NOT NULL) > 0 )) "))
				   .addWhere(samWhere.getCritPadrao(Abb01.class))
				   .get(ColumnType.INTEGER)
		
		String mensagem = "você não tem feedback para fazer."
		if(numDoc == 1) mensagem = "um documento aguarda seu feedback."
		if(numDoc > 1) mensagem = numDoc.toString() + " documentos aguardam seu feedback."		   
		
		mensagem = getVariaveis().aab10.getAab10user() + ", " + mensagem
		String url = this.httpServletRequest.getRequestURL().toString().replace(this.httpServletRequest.getRequestURI(), "")
		Map<String, Object> valoresDefault = Utils.map(
			"urlServer",url+"/menu/open/runtask",
			"mensagem", mensagem
		)
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.CGS_DocumentosWorkflowHtml.html", valoresDefault);
		
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}

	@Override
	public String getNome() throws Exception {
		return "Multitec - Documentos com Workflow";
	}
	
	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 3, 3, true, null);
	}
	
	@Override
	public int minutosEmCache() {
		return 0;
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjIC0gRG9jdW1lbnRvcyBjb20gV29ya2Zsb3ciLCJ0aXBvIjoic2VydmxldCIsInciOjMsImgiOjMsInJlc2l6ZSI6dHJ1ZSwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==