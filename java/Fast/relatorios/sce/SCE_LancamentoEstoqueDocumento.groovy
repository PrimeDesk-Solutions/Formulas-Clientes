package Fast.relatorios.sce;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.aa.Aac10;
import br.com.multitec.utils.Utils;

public class SCE_LancamentoEstoqueDocumento extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SCE - Lançamento de Estoque Documento"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", 1);
        filtrosDefault.put("numeroFinal", 99999999999);
   
        return Utils.map("filtros", filtrosDefault);
        
	}
	@Override 
	public DadosParaDownload executar() {
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		List<Long> idsItens = getListLong("itens");
		List<Integer> mps = getListInteger("mps");
		Aac10 empresa = obterEmpresaAtiva();

		List<TableMap> dados = buscarDadosRelatorio(numDocIni,numDocFin,idsItens,mps);

		 params.put("titulo","Lançamento de Estoque Por Documento");
		 params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);

		return gerarPDF("SCE_LancamentoEstoqueDocumento", dados);
	}

	private List<TableMap> buscarDadosRelatorio(Integer numDocIni,Integer numDocFin,List<Long> idsItens,List<Integer>mps){
		String whereNumDoc = numDocIni != null && numDocFin != null ? "and abb01num between :numDocIni and :numDocFin " : numDocIni != null && numDocFin == null ? "and abb01num >= :numDocIni " : numDocIni == null && numDocFin != null ? "and abb01num <= :numDocFin " : "";
		String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
		String whereTipo = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
		

		Parametro parametroNumDocIni = numDocIni != null ? Parametro.criar("numDocIni",numDocIni) : null;
		Parametro parametroNumDocFin = numDocFin != null ? Parametro.criar("numDocFin",numDocFin) : null;
		Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;
		Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps",mps) : null;

		String sql = "select abb01num, abm01codigo, abm01na, eaa01038lote,eaa01038serie, eaa01038fabric, eaa01038validade,eaa01038qt from eaa01 "+
					"inner join abb01 on abb01id = eaa01central "+
					"inner join eaa0103 on eaa0103doc = eaa01id  "+
					"inner join eaa01038 on eaa01038item = eaa0103id "+
					"inner join abm01 on abm01id = eaa0103item "+
					"where eaa01clasdoc = 1 " +
					whereNumDoc + 
					whereItens +
					whereTipo +
					"order by abb01num " 

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumDocIni,parametroNumDocFin,parametroItens,parametroMps );
			
		
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIExhbsOnYW1lbnRvIGRlIEVzdG9xdWUgRG9jdW1lbnRvIiwidGlwbyI6InJlbGF0b3JpbyJ9