package Atilatte.relatorios.customizados;

import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.ValidacaoException



import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

public class CST_Etiqueta_10x7_Pedido extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CST - Etiqueta 10x7 (Pedido)";
	}
 	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();

		String sql = "select distinct bfb01id from bfa01 "+
					"inner join bfa0101 on bfa0101rom = bfa01id  "+
					"inner join bfa01011 on bfa01011item = bfa0101id  "+
					"inner join abb01 on abb01id = bfa01docscv "+
					"inner join bfb0101 on bfb0101central = abb01id "+
					"inner join bfb01 on bfb01id = bfb0101lote "

		List<Long> listIds =  getAcessoAoBanco().obterListaDeLong(sql);


		filtrosDefault.put("listIds",listIds);

		return filtrosDefault;

	}
	@Override
	public DadosParaDownload executar() {

		Integer numInicio = getInteger("numeroInicial");
		Integer numFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Long idEntIni = getLong("entIni");
		Long idEntFin = getLong("entFin");
		List<Long> idDespacho = getListLong("despacho");
		List<Long> idRedesp = getListLong("redespacho");
		Long capaLoteIni = getLong("capaLoteIni");
		Long capaLoteFin = getLong("capaLoteFin");
		List<Long> itens = getListLong("itens");
		List<Integer> mps = getListLong("mps");
		List<Long> categoria = getListLong("categoria");
		Boolean imprimirDescrItem = getBoolean("imprimirDescrItem");

		LocalDate dataInicial = null;
		LocalDate dataFinal = null;
		if(dataEmissao != null){
			dataInicial = dataEmissao[0];
			dataFinal = dataEmissao[1];
		}

		params.put("CNPJ", getVariaveis().getAac10().getAac10ni());
		params.put("enderecoEmpresa", getVariaveis().getAac10().getAac10endereco());
		params.put("bairroEmpresa","Bairro " +getVariaveis().getAac10().getAac10bairro()+" - Itatiba/SP");
		params.put("nomeEmpresa", getVariaveis().getAac10().getAac10na());
		params.put("imprimirDescrItem", imprimirDescrItem);

		List<TableMap> dados = buscarDocumentos(numInicio, numFinal,dataInicial, dataFinal,idEntIni,idEntFin,idDespacho,idRedesp,capaLoteIni,capaLoteFin);

		List<TableMap> caixas = new ArrayList()

		for(dado in dados){
			Long idDocumento = dado.getLong("eaa01id");

			caixas += buscarEtiquetasPedido(idDocumento, itens, mps, categoria);

		}

		TableMapDataSource dsPrincipal = new TableMapDataSource(caixas);

		//return gerarPDF("CST_Etiqueta10x7_Sam4(Pedido)", dsPrincipal);
		return gerarPDF("CST_Etiqueta10x7_Sam4", dsPrincipal);
	}
	private List<TableMap> buscarDocumentos(Integer numInicio, Integer numFinal, LocalDate dataInicial, LocalDate dataFinal, Long idEntIni, Long idEntFin,List<Long> idDespacho, List<Long> idRedesp, Long capaLoteIni, Long capaLoteFin){

		String whereData = dataInicial != null && dataFinal != null  ? "and abb01data between cast(:dataInicial as date) and cast(:dataFinal as date)  " : dataInicial != null && dataFinal == null ? "and abb01data >= :dataInicial " : dataInicial == null && dataFinal != null ? "and abb01data <= :dataFinal " : ""
		String whereNumDoc = numInicio != null && numFinal != null ? "and abb01num BETWEEN :numInicio AND :numFinal " : numInicio != null && numFinal == null ? "WHERE abb01num >= :numInicio " : numInicio == null && numFinal != null ? "WHERE abb01num <= :numFinal " : ""
		String whereRedespacho = idRedesp != null && idRedesp.size() > 0 != null ? "and (redesp.abe01id in (:idRedesp)) " : ""
		String whereEntidade = idEntIni != null && idEntFin != null ? "and cliente.abe01id between :idEntIni and :idEntFin " : idEntIni != null && idEntFin == null ? "cliente.abe01id >= :idEntIni " : idEntIni == null && idEntFin != null ? "cliente.abe01id <= :idEntFin ": "";
		String whereDespacho = idDespacho != null && idDespacho.size() > 0 ? "and transp.abe01id in (:idTransp) " : ""
		String whereCapaLote = capaLoteIni != null && capaLoteFin != null ? "and bfb01id between :capaLoteIni and :capaLoteFin " : "";

		Parametro parametroNumDocIni = numInicio != null ? Parametro.criar("numInicio", numInicio) : null;
		Parametro parametroNumDocFin = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
		Parametro parametroDataIni = dataInicial != null ? Parametro.criar("dataInicial", dataInicial) : null;
		Parametro parametroDataFin = numFinal != null ? Parametro.criar("dataFinal", dataFinal) : null;
		Parametro parametroEntidadeIni = idEntIni != null ? Parametro.criar("idEntIni", idEntIni) : null;
		Parametro parametroEntidadeFin = idEntFin != null ? Parametro.criar("idEntFin", idEntFin) : null;
		Parametro parametroDespacho = idDespacho != null && idDespacho.size() > 0 ? Parametro.criar("idDespacho", idDespacho) : null;
		Parametro parametroRedespacho = idRedesp != null && idRedesp.size() > 0 ? Parametro.criar("idRedesp", idRedesp) : null;
		Parametro parametroCapaLoteIni = capaLoteIni != null ? Parametro.criar("capaLoteIni", capaLoteIni) : null;
		Parametro parametroCapaLoteFin = capaLoteFin != null ? Parametro.criar("capaLoteFin", capaLoteFin) : null;

		String sql = "select distinct eaa01id "+
					"from bfa01 "+
					"inner join bfa0101 on bfa0101rom = bfa01id "+
					"inner join bfa01011 on bfa01011item = bfa0101id "+
					"inner join abb01 on abb01id = bfa01docscv  "+
					"inner join bfb0101 on bfb0101central = abb01id "+
					"inner join bfb01 on bfb01id = bfb0101lote "+
					"inner join eaa01 on eaa01central = abb01id  "+
					"inner join eaa0102 on eaa0102doc = eaa01id  "+
					"inner join abe01 as desp on desp.abe01id = eaa0102despacho "+
					"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
					"inner join eaa0103 on eaa0103id = bfa0101item "+
					"inner join abe01 as cliente on cliente.abe01id = abb01ent "+
					"inner join abe02 on abe02ent = cliente.abe01id  "+
					"inner join abm01 on abm01id = eaa0103item "+
					"where true "+
					whereNumDoc +
					whereData+
					whereEntidade +
					whereDespacho +
					whereCapaLote +
					whereRedespacho +
					"and eaa01cancdata is null "+
					"order by eaa01id";

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumDocIni,parametroNumDocFin,parametroDataIni,parametroDataFin,parametroEntidadeIni,parametroEntidadeFin,parametroDespacho,parametroRedespacho,parametroCapaLoteIni,parametroCapaLoteFin )
	}
	private List<TableMap> buscarEtiquetasPedido(Long idDoc, List<Long> listItens, List<Integer> mps, List<Long> categoria ){

		String whereDoc = "where eaa01id = :idDoc ";
		String whereEmpresa = 	"and abm0101empresa = '1322578'";
		String whereItens = listItens != null && listItens.size() > 0 ? "and abm01id in (:itens) " : "";
		String whereMps = mps != null && !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";
		String whereCategoria = categoria != null && categoria.size() > 0 ? "and abm0102criterio in (:categoria) " : "";

		Parametro parametroDoc = Parametro.criar("idDoc", idDoc);
		Parametro parametroItens = listItens != null && listItens.size() > 0 ? Parametro.criar("itens", listItens) : null;
		Parametro parametroMPS = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;
		Parametro parametroCategoria = categoria != null && categoria.size() > 0 ? Parametro.criar("categoria", categoria) : null;

		String sql = "select distinct cast(abm0101json ->> 'cvdnf' as numeric(18,6)) as cvdnf,CAST(abm0101json ->>'volume_caixa' AS numeric(18,6)) as volumeCaixa, eaa0103id, abb01num,abm01na,abm01codigo,eaa0103qtComl,cast(eaa0103json ->> 'qt_convertida' as numeric(18,6)) as qtConvertida,cast(eaa0103json ->> 'frasco' as numeric(18,6)) as frasco,cast(eaa0103json ->> 'caixa' as numeric(18,6)) as caixa, "+
					"cast(abm0101json ->> 'volume_caixa' as numeric(18,6)) as volumeCaixa, eaa01operdescr, cliente.abe01nome as entidade, eaa0101endereco as endereco, "+
					"eaa0101numero as numero,eaa0101bairro as bairro, aag0201nome as municipio, aag02uf as uf, eaa0101cep as cep, desp.abe01codigo as codigoDespacho, desp.abe01na despacho, "+
					"redesp.abe01codigo as codigoRedespacho, redesp.abe01na as redespacho, cast(eaa0103json ->>'umv' as character varying(2)) as UMV,CAST(abm0101json ->>'ordem_separacao' AS numeric(18,2)) AS ordemSeparacao "+
					"from bfa01  "+
					"inner join bfa0101 on bfa0101rom = bfa01id  "+
					"inner join bfa01011 on bfa01011item = bfa0101id  "+
					"inner join abb01 on abb01id = bfa01docscv  "+
					"inner join bfb0101 on bfb0101central = abb01id "+
					"inner join bfb01 on bfb01id = bfb0101lote "+
					"inner join eaa01 on eaa01central = abb01id  "+
					"inner join eaa0101 on eaa0101doc = eaa01id and eaa0101principal = 1 "+
					"inner join aag0201 on aag0201id = eaa0101municipio  "+
					"inner join aag02 on aag02id = aag0201uf  "+
					"inner join eaa0102 on eaa0102doc = eaa01id  "+
					"inner join abe01 as desp on desp.abe01id = eaa0102despacho  "+
					"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho  "+
					"inner join eaa0103 on eaa0103id = bfa0101item  "+
					"inner join abe01 as cliente on cliente.abe01id = abb01ent  "+
					"inner join abe02 on abe02ent = cliente.abe01id  "+
					"inner join abm01 on abm01id = eaa0103item  "+
					"inner join abm0101 on abm0101item = abm01id  "+
					"inner join aam06 on aam06id = abm01umu  "+
					"left join abm0102 on abm0102item = abm01id "+
					whereDoc +
					whereEmpresa +
					whereItens +
					whereMps +
					whereCategoria +
					"order by ordemSeparacao desc";

		List<TableMap> registros = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDoc,parametroItens,parametroMPS,parametroCategoria);

		List<TableMap> etiquetas = new ArrayList()

		for(int i = 0; i < registros.size();i++){

			String umv = registros.get(i).getString("UMV");
			Integer capacidade = registros.get(i).getInteger("cvdnf");
			Integer cxs = umv == 'CX' || umv == 'KG'  ?  registros.get(i).getInteger("caixa") : registros.get(i).getInteger("eaa0103qtComl") / capacidade;

			for(int cx = 0; cx < cxs; cx++){
				TableMap itens = new TableMap();
				String codbarsam = registros.get(i).getInteger("abb01num");
				itens.put("item",(registros.get(i).getString("abm01na") + "                         ").substring(0,40) +capacidade);
				itens.put("nf",registros.get(i).getInteger("abb01num"));
				itens.put("tipo","CAIXA");
				itens.put("entidade", registros.get(i).getString("entidade"));
				itens.put("endereco", registros.get(i).getString("endereco"));
				itens.put("numero", registros.get(i).getString("numero"));
				itens.put("bairro", registros.get(i).getString("bairro"));
				itens.put("municipio", registros.get(i).getString("municipio"));
				itens.put("uf",registros.get(i).getString("uf"));
				itens.put("cep",registros.get(i).getString("cep"));
				itens.put("despacho",registros.get(i).getString("despacho"));
				itens.put("codigodespacho",registros.get(i).getString("codigoDespacho"));
				itens.put("ordemSeparacao",registros.get(i).getString("ordemSeparacao"));
				itens.put("redespacho",registros.get(i).getString("redespacho"));
				itens.put("qtd", registros.get(i).getInteger("frasco"));
				itens.put("codbarsam", codbarsam);

				etiquetas.add(itens)

			}
		}
		return etiquetas;
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgKENvbSBSb21hbmVpbykiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgKENvbSBSb21hbmVpbykiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgKFBlZGlkbykiLCJ0aXBvIjoicmVsYXRvcmlvIn0=