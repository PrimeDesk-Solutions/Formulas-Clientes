package multitec.utils

import java.util.stream.Collectors

import org.springframework.http.ResponseEntity

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase

class Servlet_ExecutarConsulta extends ServletBase {

	@Override
	public ResponseEntity<Object> executar() throws Exception {
		TableMap map = converterCorpoRequisicaoParaObjeto(TableMap.class);
		
		String table = map.getString("table");
		if(table == null)throw new ValidacaoException("Necessário informar o nome da tabela");
		
		String fields = map.getString("fields") ?: "*";
		String joins = map.getString("join") ?: "";
		String where = map.getString("where") ?: "";
		String group = map.getString("group") ?: "";
		String order = map.getString("order") ?: "";
		String limit = map.getString("limit") ?: "";
		
		String sql = " SELECT " + fields + " FROM " + table + " " + joins + " " + where + " " + group + " " + order + " " + limit + ";";
		List<TableMap> result = getAcessoAoBanco().buscarListaDeTableMap(sql);
		
		return ResponseEntity.ok().body(result);
	}

	@Override
	public String getNome() throws Exception {
		return "Executar Consulta";
	}
	
	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 0, 0, false, null);
	}
}
//meta-sis-eyJkZXNjciI6IkV4ZWN1dGFyIENvbnN1bHRhIiwidGlwbyI6InNlcnZsZXQiLCJ3IjowLCJoIjowLCJyZXNpemUiOmZhbHNlLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9