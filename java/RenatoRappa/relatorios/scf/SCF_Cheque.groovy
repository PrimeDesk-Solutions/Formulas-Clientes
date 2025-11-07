package RenatoRappa.relatorios.scf;

import br.com.multitec.utils.Extenso
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.da.Dab20
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_Cheque extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Cheque";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<TableMap> dados = new ArrayList<>();
		
		List<Long> dab20ids = getListLong("dab20ids");
		if (dab20ids == null || dab20ids.size() == 0) interromper("Não foram informados cheques para impressão")
				
		List<Dab20> dab20s = buscarCheques(dab20ids);
		
		String municipio = "";
		if (obterEmpresaAtiva().aac10municipio != null) {
			String sql = " SELECT aag0201nome FROM Aag0201 WHERE aag0201id = :aag0201id ";
			municipio = getAcessoAoBanco().obterString(sql, criarParametroSql("aag0201id", obterEmpresaAtiva().aac10municipio.idValue));
		}
		
		for (Dab20 dab20 : dab20s) {
			
			TableMap tm = new TableMap();
			
			tm.put("valor", dab20.dab20valor)
			Extenso extenso = new Extenso(dab20.dab20valor);
			tm.put("valor_extenso", "("+ extenso.toString() +")")
			tm.put("municipio", municipio)
			tm.put("nominal", dab20.dab20pNome)
			tm.put("vencimento", dab20.dab20dtVcto)
			tm.put("emissao", dab20.dab20dtEmis)
			
			dados.add(tm)
		}
		
		return gerarPDF("SCF_Cheque", dados);
	}
	
	private List<Dab20> buscarCheques(List<Long> dab20ids) {
		String sql = " SELECT dab20id, dab20valor, dab20pNome, dab20dtEmis, dab20dtVcto " +
					 " FROM Dab20 " +
					 getSamWhere().getWherePadrao(" WHERE ", Dab20.class) +
					 " AND dab20id IN (:dab20ids) ";

		List<Dab20> dab20s = getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dab20ids", dab20ids)); 
		return dab20s;
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIENoZXF1ZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==