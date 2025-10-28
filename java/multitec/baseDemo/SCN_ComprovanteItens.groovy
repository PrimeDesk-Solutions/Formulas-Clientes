package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource

class SCN_ComprovanteItens extends RelatorioBase {

	@Override
	public Map<String, Object> criarValoresIniciais() {
		return null;
	}

	@Override
	public DadosParaDownload executar() {
		String sql = " SELECT dbb10id, dbb10rsvNome, abb01num, abe21codigo, abe21descr, abm01codigo, abm01na, dbb1001qt, dbb1001unit, dbb1001total " +
					 " FROM Dbb1001 INNER JOIN Abm01 ON abm01id = dbb1001item " +
					 " INNER JOIN Dbb10 ON dbb10id = dbb1001reg " +
					 " INNER JOIN Abb01 ON abb01id = dbb10central " + 
					 " INNER JOIN Abe21 ON abe21id = dbb10regUn " +
					 " WHERE dbb1001id IN (:dbb1001ids) " +
					 " ORDER BY abb01num ASC, abm01codigo ASC";
		
		List<Long> dbb1001ids = getListLong("dbb1001ids");
		
		List<TableMap> reservas = getAcessoAoBanco().buscarListaDeTableMap(sql, criarParametroSql("dbb1001ids", dbb1001ids));;
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(reservas);
		return gerarPDF("SCN_ComprovanteItens", dsPrincipal);
	}

	@Override
	public String getNomeTarefa() {
		return "SCN-Comprovante de Itens da Reserva";
	}
}
//meta-sis-eyJkZXNjciI6IlNDTi1Db21wcm92YW50ZSBkZSBJdGVucyBkYSBSZXNlcnZhIiwidGlwbyI6InJlbGF0b3JpbyJ9