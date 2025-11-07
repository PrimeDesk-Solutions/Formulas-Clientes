package Atilatte.cubos.scv;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;


import java.time.LocalDate
import br.com.multitec.utils.ValidacaoException
import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import sam.model.entities.ea.Eaa01;

public class CUBO_SCV_ResumoDeVendaPorEntidade extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CUBO - SCV - Resumo de Vendas por Entidade"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		return criarFiltros("operacao",0,"tipoOperacao",0)
	}
	@Override 
	public DadosParaDownload executar() {
		Integer operacao = getInteger("operacao");
		Integer tipoOperacao = getInteger("tipoOperacao")
		List<Long> idItens = getListLong("item");
		List<Long> idEntidades = getListLong("entidade");
		List<Long> idMunicipio = getListLong("municipio");
		LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");

		List<TableMap> dadosRelatorio = buscarDocumentos(operacao,tipoOperacao,dtEmissao,idItens,idEntidades,idMunicipio)

		return gerarXLSX("ResumoDeVendaPorEntidade", dadosRelatorio);
		
	}

	private buscarDocumentos(Integer operacao, Integer tipoOperacao, LocalDate[] dtEmissao, List<Long> idItens,List<Long> idEntidades,List<Long> idMunicipio ){
		String operDocumento = ""
		String tipoOper = ""

		LocalDate dtEmissIni = null;
		LocalDate dtEmissFin = null;
		if(dtEmissao != null){
			dtEmissIni = dtEmissao[0];
			dtEmissFin = dtEmissao[1];
		}
		
		
		if(operacao == 0){
			operDocumento = "AND abd01aplic = 1 and abd01es = 0 "
		}else if(operacao == 1){
			operDocumento = "AND abd01aplic = 1 and abd01es = 1 "
		}else if(operacao == 2 ){
			operDocumento = "AND abd01aplic = 0 and abd01es = 0 "
		}else{
			operDocumento = "AND abd01aplic = 0 and abd01es = 1 "
		}
		

		if(tipoOperacao == 99){
			tipoOper = "AND abb10tipocod in (0,1,2,3,4,5,6,7) "
		}else{
			tipoOper = "AND abb10tipocod = :tipoOperacao "
		}

		Query sql = getSession().createQuery("select distinct abe01codigo as codEnt, abe01na as nomeEntidade, abe0101endereco as enderecoEntidade,abe0101bairro as bairro, abe0101numero as numero,abe0101fone1  as fone, aag0201nome as municipio,abm01na as naItem, eaa0103qtcoml as sce "+
										"from eaa01 "+
										"inner join abd01 on abd01id = eaa01pcd "+
										"inner join abb10 on abb10id = abd01opercod "+
										"inner join abb01 on abb01id = eaa01central "+
										"inner join abe01 on abe01id = abb01ent "+
										"inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 "+
										"inner join aag0201 on aag0201id = abe0101municipio "+
										"inner join eaa0103 on eaa0103doc = eaa01id "+
										"inner join abm01 on abm01id = eaa0103item "+
										"inner join eaa0102 on eaa0102doc = eaa01id "+
										getSamWhere().getWherePadrao("WHERE", Eaa01.class) +
										(dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "") +
										(idEntidades != null ?"and abe01id in (:idEntidades) " : "") +
										"and abm01tipo = '1' "+
										(idItens != null ?"and abm01id in (:idItens) " : "")+
										(idMunicipio != null ? "and aag0201id in (:idMunicipio) " : "")+			
										operDocumento +
										tipoOper);
										
		if(dtEmissIni != null && dtEmissFin != null){
			sql.setParameter("dtEmissIni",dtEmissIni);
			sql.setParameter("dtEmissFin",dtEmissFin);
		}

		if(idEntidades != null){
			sql.setParameter("idEntidades",idEntidades);
		}

		if(idItens != null ){
			sql.setParameter("idItens",idItens);
		}

		if(idMunicipio != null){
			sql.setParameter("idMunicipio",idMunicipio);
		}

		if(tipoOper != null){
			sql.setParameter("tipoOperacao",tipoOperacao);
		}

		return sql.getListTableMap();

		
		
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBSZXN1bW8gZGUgVmVuZGFzIHBvciBFbnRpZGFkZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==