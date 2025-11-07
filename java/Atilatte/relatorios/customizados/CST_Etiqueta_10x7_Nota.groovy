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

public class CST_Etiqueta_10x7_Nota extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Etiqueta 10x7 (Nota Fiscal)";
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
				
	}
	@Override 
	public DadosParaDownload executar() {
		
		
		Integer numInicio = getInteger("numeroInicial");
		Integer numFinal = getInteger("numeroFinal");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		List<Long> idEntidade = getListLong("entidades");
		List<Long> idDespacho = getListLong("despacho");
		List<Long> idRedesp = getListLong("redespachos");
		Boolean imprimirDescrItem = getBoolean("imprimirDescrItem");
		Aac10 empresa = obterEmpresaAtiva();

		LocalDate dataInicial = null;
		LocalDate dataFinal = null;
		if(dataEmissao != null){
			dataInicial = dataEmissao[0];
			dataFinal = dataEmissao[1];
		}

		params.put("CNPJ", empresa.getAac10ni());
		params.put("enderecoEmpresa", empresa.getAac10endereco());
		params.put("bairroEmpresa","Bairro " +empresa.getAac10bairro()+" - Itatiba/SP");
		params.put("nomeEmpresa", empresa.getAac10na());
		params.put("imprimirDescrItem", imprimirDescrItem);


		List<TableMap> registros = buscarDocumentos(numInicio, numFinal,dataInicial, dataFinal,idEntidade,idDespacho,idRedesp, empresa);
		
		List<TableMap> caixas = new ArrayList()
		for(registro in registros){
			Long idDoc = registro.getLong("eaa01id");
			caixas += buscarQuantidadeCaixaItens(idDoc);
		}

		TableMapDataSource dsPrincipal = new TableMapDataSource(caixas);
		//return gerarPDF("CST_Etiqueta10x7_Sam4(Nota)", dsPrincipal);
		return gerarPDF("CST_Etiqueta10x7_Sam4(Faturamento)", dsPrincipal);
	}
	private List<TableMap> buscarDocumentos(Integer numInicio, Integer numFinal, LocalDate dataInicial, LocalDate dataFinal, List<Long> idEntidade,List<Long> idDespacho, List<Long> idRedesp, Aac10 empresa) {

		String whereData = dataInicial != null && dataFinal != null ? "and abb01data between cast(:dataInicial as date) and cast(:dataFinal as date)  " : dataInicial != null && dataFinal == null ? "and abb01data >= :dataInicial " : dataInicial == null && dataFinal != null ? "and abb01data <= :dataFinal " : ""
		String whereNumDoc = numInicio != null && numFinal != null ? "and abb01num BETWEEN :numInicio AND :numFinal " : numInicio != null && numFinal == null ? "WHERE abb01num >= :numInicio " : numInicio == null && numFinal != null ? "WHERE abb01num <= :numFinal " : ""
		String whereRedespacho = idRedesp != null && idRedesp.size() > 0 ? "and redesp.abe01id in (:idRedesp) " : ""
		String whereEntidade = idEntidade != null && idEntidade.size() > 0 ? "and cliente.abe01id in (:idEntidade) " : "";
		String whereDespacho = idDespacho != null && idDespacho.size() > 0 ? "and desp.abe01id in (:idDespacho) " : ""
		String whereEmpresa = "and eaa01gc = :idEmpresa ";

		Parametro parametroDataIni = dataInicial != null ? Parametro.criar("dataInicial", dataInicial) : null;
		Parametro parametroDataFin = dataFinal != null ? Parametro.criar("dataFinal", dataFinal) : null;
		Parametro parametroNumDocIni = numInicio != null ? Parametro.criar("numInicio", numInicio) : null;
		Parametro parametroNumDocFin = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
		Parametro parametroRedespacho = idRedesp != null && idRedesp.size() > 0 ? Parametro.criar("idRedesp", idRedesp) : null;
		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null;
		Parametro parametroDespacho = idDespacho != null && idDespacho.size() > 0 ? Parametro.criar("idDespacho", idDespacho) : null;
		Parametro parametroEmpresa = Parametro.criar("idEmpresa", empresa.getAac10id());


		String sql = " select distinct abb01num,eaa01id " +
					"from eaa01 " +
					"inner join abb01 on abb01id = eaa01central  " +
					"inner join eaa0102 on eaa0102doc = eaa01id  " +
					"inner join abe01 as desp on desp.abe01id = eaa0102despacho " +
					"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho " +
					"inner join eaa0103 on eaa0103doc = eaa01id " +
					"inner join abe01 as cliente on cliente.abe01id = abb01ent " +
					"inner join abe02 on abe02ent = cliente.abe01id  " +
					"inner join abm01 on abm01id = eaa0103item " +
					"where true " +
					whereNumDoc +
					whereData +
					whereRedespacho +
					whereEntidade +
					whereDespacho +
					whereEmpresa +
					"and eaa01clasdoc = 1 " +
					"and eaa01esmov = 1 " +
					"and eaa01cancdata is null " +
					"order by abb01num";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDataIni, parametroDataFin, parametroNumDocIni, parametroNumDocFin, parametroRedespacho, parametroEntidade, parametroDespacho, parametroEmpresa)

	}
	private List<TableMap>buscarQuantidadeCaixaItens(Long idDoc){

		String whereDoc = "where eaa01id = :idDoc ";
		Parametro parametroDoc = Parametro.criar("idDoc", idDoc) ;

		String sql = "select distinct eaa0103id, abb01num,abm01na,abm01codigo,eaa0103qtComl,cast(eaa0103json ->> 'qt_convertida' as numeric(18,6)) as qtConvertida,cast(eaa0103json ->> 'frasco' as numeric(18,6)) as frasco,cast(eaa0103json ->> 'caixa' as numeric(18,6)) as caixa, "+
					"cast(abm0101json ->> 'volume_caixa' as numeric(18,6)) as volumeCaixa,cast(abm0101json ->> 'cvdnf' as numeric(18,6)) as cvdnf, eaa01operdescr, cliente.abe01nome as entidade, eaa0101endereco as endereco, "+
					"eaa0101numero as numero,eaa0101bairro as bairro, aag0201nome as municipio, aag02uf as uf, eaa0101cep as cep, desp.abe01codigo as codigoDespacho, desp.abe01na despacho, "+
					"redesp.abe01codigo as codigoRedespacho, redesp.abe01na as redespacho, cast(eaa0103json ->>'umv' as character varying(2)) as UMV,CAST(abm0101json ->>'ordem_separacao' AS numeric(18,2)) AS ordemSeparacao "+
					"from eaa01  "+
					"inner join abb01 on abb01id = eaa01central  "+
					"inner join eaa0101 on eaa0101doc = eaa01id and eaa0101principal = 1 "+
					"inner join aag0201 on aag0201id = eaa0101municipio  "+
					"inner join aag02 on aag02id = aag0201uf  "+
					"inner join eaa0102 on eaa0102doc = eaa01id  "+
					"inner join abe01 as desp on desp.abe01id = eaa0102despacho  "+
					"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho  "+
					"inner join eaa0103 on eaa0103doc = eaa01id  "+
					"inner join abe01 as cliente on cliente.abe01id = abb01ent  "+
					"inner join abe02 on abe02ent = cliente.abe01id  "+
					"inner join abm01 on abm01id = eaa0103item  "+
					"inner join abm0101 on abm0101item = abm01id  "+
					"inner join aam06 on aam06id = abm01umu  "+
					whereDoc+
					"order by ordemSeparacao desc ";

			List<TableMap> registros = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDoc )
			List<TableMap> itensEtiqueta = new ArrayList();


			for(int i = 0; i < registros.size();i++){

				String umv = registros.get(i).getString("UMV");
				Integer qtdCaixa = umv == 'UN' || umv == 'FR' ? registros.get(i).getInteger("eaa0103qtComl") / registros.get(i).getInteger("volumeCaixa") : registros.get(i).getInteger("caixa");
				Integer capacidade = registros.get(i).getInteger("cvdnf")
				for(int cx = 0; cx < qtdCaixa; cx++){
					TableMap itens = new TableMap()
					String codbarsam = registros.get(i).getInteger("abb01num");

					itens.put("item",(registros.get(i).getString("abm01na") + "                         ").substring(0,40) +capacidade);
					itens.put("nf",registros.get(i).getInteger("abb01num"));
					itens.put("tipo","CAIXA");
					itens.put("operacao", registros.get(i).getString("eaa01operdescr"));
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
					itens.put("codbarsam", codbarsam);

					itensEtiqueta.add(itens)

				}
			}
			return itensEtiqueta;
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgKEZhdHVyYW1lbnRvKSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IkNTVCAtIEV0aXF1ZXRhIDEweDcgKE5vdGEgRmlzY2FsKSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==