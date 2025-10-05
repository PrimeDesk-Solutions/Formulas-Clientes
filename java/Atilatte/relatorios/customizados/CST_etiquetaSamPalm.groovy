package Atilatte.relatorios.customizados;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;


import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abm70
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class CST_etiquetaSamPalm extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Etiqueta SamPalm "; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("numeroEtiquetaInicial","000000001",
						"numeroEtiquetaFinal","999999999",
						"data",DateUtils.getStartAndEndMonth(MDate.date()),
						"imprimir","0",
						"numeroDocumentoInicial","000000001",
						"numeroDocumentoFinal", "999999999"
						
		)
	}
	@Override 
	public DadosParaDownload executar() {
		Integer numEtiquetaIni = getInteger("numeroEtiquetaInicial");
		Integer numEtiquetaFin = getInteger("numeroEtiquetaFinal");
		LocalDate[] dtCriacao = getIntervaloDatas("data");
		Integer status = getInteger("imprimir");
		List<Integer> mpm = getListInteger("mpm");
		String itemIni = getString("itemIni");
		String itemFin = getString("itemFim");
		Long tipoDoc = getLong("tipoDocumento");
		Integer numDocIni = getInteger("numeroDocumentoInicial");
		Integer numDocFin = getInteger("numeroDocumentoFinal");

		List<Long> ids = get("abm70ids");
		List<TableMap> listTMDados = null;


		if(ids == null){
			listTMDados = buscarEtiquetasrelatorio(numEtiquetaIni, numEtiquetaFin, dtCriacao, status, mpm,
											 itemIni, itemFin, tipoDoc, numDocIni, numDocFin);
		}else{
			listTMDados = buscarEtiquetasPorId(ids);
		}

		List<Long> abm70ids = new ArrayList<Long>();
		for(TableMap tm : listTMDados){
			Long abm70id = tm.getLong("abm70id");
			abm70ids.add(abm70id);
		}

		//TableMapDataSource dsPrincipal = new TableMapDataSource(listTMDados);

		gravarStatusImpressaoEtiquetas(abm70ids);

		params.put("CNPJ", getVariaveis().getAac10().getAac10ni());
		params.put("enderecoEmpresa", getVariaveis().getAac10().getAac10endereco());
		params.put("bairroEmpresa","Bairro " +getVariaveis().getAac10().getAac10bairro()+" - Itatiba/SP");
		params.put("nomeEmpresa", getVariaveis().getAac10().getAac10na());
		params.put("tipo", "CAIXA");

		return gerarPDF("CST_Etiqueta10x7_Sam4",listTMDados);

		

	}

	private List<TableMap> buscarEtiquetasrelatorio(Integer numEtiquetaIni, Integer numEtiquetaFin, LocalDate[] dtCriacao, Integer status, List<Integer> mpm,
											String itemIni,String itemFin, Long tipoDoc, Integer numDocIni, Integer numDocFin){
		


		LocalDate dataIni = null;
		LocalDate dataFin = null;

		if(dtCriacao != null){
			dataIni = dtCriacao[0];
			dataFin = dtCriacao[1];
		}

		if(mpm == null || mpm.size() == 0) mpm = Arrays.asList(-1);

		String whereItens = itemIni != null && itemFin != null ? "and abm01codigo between :itemIni and :itemFin " : itemIni != null && itemFin == null ? "and abm01codigo >= :itemIni " : itemIni == null && itemFin != null ? "and abm01codigo <= itemFin " : "" 
		
		Query sql = getSession().createQuery("select abm70id,abb01num as numPedido, ent.abe01nome as entidade,abe0101endereco as endereco, abe0101numero as numero, "+
										"abe0101bairro as bairro, aag0201nome as municipio, aag02uf as uf,abe0101cep as cep,abm01na as item,abm70data,eaa01operdescr as operacao,abm70qt, "+
										"desp.abe01na as despacho "+
										"from abm70 "+
										"inner join abm01 on abm01id = abm70item "+
										"inner join abb01 on abm70central = abb01id "+ 
										"inner join aah01 on aah01id = abb01tipo "+
										"inner join eaa01 on eaa01central = abb01id "+
										"inner join abe01 as ent on abb01ent = abe01id "+ 
										"inner join abe0101 on ent.abe01id = abe0101ent and abe0101principal = 1 "+
										"inner join eaa0102 on eaa0102doc = eaa01id "+
										"inner join abe01 desp on desp.abe01id = eaa0102despacho "+
										"left join abe01 redesp on redesp.abe01id = eaa0102redespacho "+
										"inner join aag0201 on aag0201id = abe0101municipio "+
										"inner join aag02 on aag02id = aag0201uf "+
										"where abm70num between :numEtiquetaIni and :numEtiquetaFin "+
										"and abm70data between :dataIni and :dataFin "+
										"and abm70status = :status  "+ 
										"and abm01tipo in (:mpm) "+
										whereItens +
										(tipoDoc != null ? "and aah01id = :tipoDoc " : "")+
										(tipoDoc != null ? "and abb01num between :numDocIni and :numDocFin " : "") +
										"order by abm70num");
										
		if(tipoDoc != null ){
			sql.setParameter("tipoDoc",tipoDoc);
			sql.setParameter("numDocIni",numDocIni);
			sql.setParameter("numDocFin",numDocFin);
			
		}

		if(whereItens.length() > 0 ){
			if(itemIni != null) sql.setParameter("itemIni",itemIni);
			if(itemFin != null) sql.setParameter("itemFin",itemFin);
		}

		sql.setParameters("numEtiquetaIni",numEtiquetaIni,
				     	"numEtiquetaFin",numEtiquetaFin,
				     	"dataIni",dataIni,
				     	"dataFin",dataFin,
				     	"status",status,
				     	"mpm",mpm);

		
		
		return sql.getListTableMap();
	}

	private List<TableMap> buscarEtiquetasPorId(List<Long>ids){
		Query sql = getSession().createQuery("select abm70id,abb01num as nf, ent.abe01nome as entidade,abe0101endereco as endereco, abe0101numero as numero, "+
										"abe0101bairro as bairro, aag0201nome as municipio, aag02uf as uf,abe0101cep as cep,abm01na as item,abm70data,eaa01operdescr as operacao,abm70qt, "+
										"desp.abe01na as despacho "+
										"from abm70 "+
										"inner join abm01 on abm01id = abm70item "+
										"inner join abb01 on abm70central = abb01id "+ 
										"inner join aah01 on aah01id = abb01tipo "+
										"inner join eaa01 on eaa01central = abb01id "+
										"inner join abe01 as ent on abb01ent = abe01id "+ 
										"inner join abe0101 on ent.abe01id = abe0101ent and abe0101principal = 1 "+
										"inner join eaa0102 on eaa0102doc = eaa01id "+
										"inner join abe01 desp on desp.abe01id = eaa0102despacho "+
										"left join abe01 redesp on redesp.abe01id = eaa0102redespacho "+
										"inner join aag0201 on aag0201id = abe0101municipio "+
										"inner join aag02 on aag02id = aag0201uf "+
										"where abm70id in (:ids)" +
										"order by abm70num");
		sql.setParameter("ids",ids);

		return sql.getListTableMap();
	}

	private void gravarStatusImpressaoEtiquetas(List<Long> abm70ids) {
		try {
			if(abm70ids == null || abm70ids.size() == 0) return;
			
			for(Long abm70id : abm70ids) {
				Abm70 abm70 = getSession().get(Abm70.class, "abm70id, abm70status", abm70id);
				abm70.setAbm70status(1);
				getSession().persist(abm70);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIFNhbVBhbG0gIiwidGlwbyI6InJlbGF0b3JpbyJ9