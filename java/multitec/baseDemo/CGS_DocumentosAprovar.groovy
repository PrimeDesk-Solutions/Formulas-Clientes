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
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.MediaType
import sam.server.samdev.relatorio.UiDto

class CGS_DocumentosAprovar extends ServletBase {

	@Override
	public ResponseEntity<Object> executar() throws Exception {						
		Integer numDoc = session.createCriteria(Abb0103.class)
			.addFields("count(abb01id)")
			.addJoin(Joins.join("abb0103central").alias("abb01"))
			.addJoin(Joins.join("abb01.abb01tipo").alias("aah01"))
			.addJoin(Joins.join("abb01.abb01ent").alias("abe01").left(true))
			.addJoin(Joins.join("abb01.abb01operCod").alias("abb10").left(true))
			.addJoin(Joins.join("abb0103user").alias("aab10").left(true))
			.addWhere(Criterions.eq("abb01status", Abb01.STATUS_NORMAL))
			.addWhere(Criterions.where(variaveis.getPermissoes().montarWherePelaColuna("abb0103ps", OperacaoDeSeguranca.APROVACAO_DE_DOCUMENTOS)))
			.addWhere(Criterions.where(" abb0103central NOT IN ( SELECT eaa01central FROM Eaa01 WHERE eaa01bloqueado = 1 OR eaa01gravParc = 1 " + samWhere.getWhereGc("AND", Eaa01.class) + " ) "))
			.addWhere(Criterions.where(" abb0103central NOT IN ( SELECT cbb01central FROM Cbb01 WHERE cbb01status > 1 " + samWhere.getWhereGc("AND", Cbb01.class) + " ) "))
			.addWhere(Criterions.where(" abb0103central NOT IN ( SELECT cbd01central FROM Cbd01 WHERE cbd01status > 1 " + samWhere.getWhereGc("AND", Cbd01.class) + " ) "))
			.addWhere(Criterions.where(" abb0103central NOT IN ( SELECT bfa01docScv FROM Bfa01 " + samWhere.getWhereGc("WHERE", Bfa01.class) + " ) "))
			.addWhere(samWhere.getCritPadrao(Abb01.class))
			.get(ColumnType.INTEGER)
		
		String mensagem ="você não tem documentos para aprovar."
		if(numDoc == 1) mensagem = "um documento aguarda sua aprovação."
		if(numDoc > 1) mensagem =  numDoc.toString() + " documentos aguardam sua aprovação."
		
		mensagem = getVariaveis().aab10.getAab10user() + ", " + mensagem
		String url = this.httpServletRequest.getRequestURL().toString().replace(this.httpServletRequest.getRequestURI(), "")
		Map<String, Object> valoresDefault = Utils.map(
			"urlServer",url+"/menu/open/runtask",
			"mensagem", mensagem
		)
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.CGS_DocumentosAprovarHtml.html", valoresDefault);
		
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}

	@Override
	public String getNome() throws Exception {
		return "Multitec-Documentos para Aprovação";
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
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLURvY3VtZW50b3MgcGFyYSBBcHJvdmHDp8OjbyIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MywiaCI6MywicmVzaXplIjp0cnVlLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9