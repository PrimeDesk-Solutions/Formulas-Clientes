package Atilatte.relatorios.customizados;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.ValidacaoException

public class CST_B2b extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - B2b"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		String itemIni = getString("itemIni");
		String itemFim = getString("itemFim");
		List<Integer> mp = getListInteger("mpms");
		String codigoEmpresa =  getVariaveis().getAac10().getAac10codigo();

		List<TableMap> dados = obterDadosRelatorio(codigoEmpresa, mp);
		
		for(TableMap teste : dados){

			String codigoGrupo = teste.getString("codgrupo");
			Integer tipoItem = teste.getInteger("mps");
			
			

			TableMap descrGrupo = buscarGrupoPorItem(codigoGrupo,codigoEmpresa,tipoItem);
			
			if(descrGrupo != null) teste.putAll(descrGrupo);
			
			//listTMItens.add(teste)
		}

		return gerarPDF("CST_B2B000", dados);
	}
	public List<TableMap> obterDadosRelatorio(String codigoEmpresa, List<Integer>mp){
		
		String whereTipos = (!mp.contains(-1)) ? " AND abm01tipo IN (:mp) " : "";
		String whereItens = itemIni != null && itemFim != null ? " AND abm01codigo BETWEEN :itemIni AND :itemFim " : itemIni != null && itemFim == null ? " AND abm01codigo >= :itemIni " : itemIni == null && itemFim != null ? " AND abm01codigo <= :itemFim " : "";
		Parametro parametroTipos = (!mp.contains(-1)) ? Parametro.criar("mp", mp) : null;
		Parametro parametroItemIni = itemIni != null ? criarParametroSql("itemIni", itemIni) : null;
		Parametro parametroItemFim = itemFim != null ? criarParametroSql("itemFim", itemFim) : null;

		String sql =  "select distinct cba01ent as identidade, abe01codigo as codentidade,abe01na as nomecliente,aac10codigo as empresa,cba0101fh as fornechomol,abm01tipo as mps, cba0101item , abm01codigo, abm01descr as descrItem, abg02codigo, aam06codigo, abm1301fcCU, abm12ativCP, aaj10codigo ,aaj12codigo ,aaj13codigo, " +
			        (codigoEmpresa == "000" ? "substring(abm01codigo,0,6) as codgrupo from cba0101 " : codigoEmpresa == "001" ? "substring(abm01codigo,0,5) as codgrupo from cba0101 ":  "substring(abm01codigo,0,5) as codgrupo from cba0101 " )+
				   "INNER join cba01 on cba01.cba01id = cba0101.cba0101fh " +
				   "INNER join abe01 ON abe01.abe01id = cba01.cba01ent " +
				   "INNER join abm01 ON abm01.abm01id = cba0101.cba0101item " +
				   "INNER join abm0101 ON abm0101.abm0101item = abm01.abm01id " +
				   "INNER join aac10 on aac10.aac10id = abm0101.abm0101empresa " +
				   "INNER join abm12 on abm12.abm12id = abm0101.abm0101fiscal " +
				   "FULL join abg02 on abg02.abg02id = abm12.abm12ativcp " +
				   "INNER join aaj10 on aaj10.aaj10id = abm12.abm12csticms " +
				   "INNER join aaj12 on aaj12.aaj12id = abm12.abm12cstpissai " +
				   "INNER join aaj13 on aaj13.aaj13id = abm12.abm12cstcofinssai " +
				   "INNER join aam06 ON aam06.aam06id = cba0101.cba0101umc " +
				   "INNER join abm1301 ON abm1301.abm1301umc = aam06.aam06id " +
				   "where aac10codigo = '"+codigoEmpresa+"'  " +
				   whereTipos +
				   whereItens

				  

		   List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql,  parametroTipos, parametroItemIni, parametroItemFim);
		   return receberDadosRelatorio;
				   
	}

	private TableMap buscarGrupoPorItem(String codigoGrupo,String codigoEmpresa,Integer tipoItem) {
		return getSession().createQuery(
				"select abm01na from abm01 ", 
				"inner join abm0101 on abm0101item = abm01id ",
				"inner join aac10 on aac10id = abm0101empresa ",
				"where abm01codigo = :abm01codigo ",
				"and aac10codigo = :aac10codigo ",
				"and abm01tipo = :abm01tipo ")
			.setParameters("abm01codigo", codigoGrupo, "aac10codigo", codigoEmpresa,"abm01tipo", tipoItem )
			.setMaxResult(1)
			.getUniqueTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEIyYiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==