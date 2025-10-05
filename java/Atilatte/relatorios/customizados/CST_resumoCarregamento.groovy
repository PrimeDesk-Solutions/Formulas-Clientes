package Atilatte.relatorios.customizados;

import sam.model.entities.ea.Eaa01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

public class resumoCarregamento extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "CST - Resumo Carregamento "; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
		
		List<TableMap> dados = buscarDocumentos();

		

		gerarPDF("resumoCarregamento",dados)
		
	}

	private List<TableMap> buscarDocumentos(){
		LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dtEntradaSaida = getIntervaloDatas("dataSaida");
		List<Long> idsEntidades = getListLong("idsEntidades");
		List<Long> idsDespacho = getListLong("idsDespachos");
		List<Long> idsRedesp = getListLong("idsRedesp");

		

	
		//Data Emissao
		LocalDate dtEmissaoIni = null;
		LocalDate dtEmissaoFin = null;
		if(dtEmissao != null){
			dtEmissaoIni = dtEmissao[0];
			dtEmissaoFin = dtEmissao[1];
		}
		//Data Entrada / Saida
		LocalDate dtEntradaSaidaIni = null;
		LocalDate dtEntradaSaidaFin = null;
		if(dtEntradaSaida != null){
			dtEntradaSaidaIni = dtEntradaSaida[0];
			dtEntradaSaidaFin = dtEntradaSaida[1];
		}

		

		Query sql = getSession().createQuery("select desp.abe01na as despacho, redesp.abe01na as redespacho, abb01num, abb01data, eaa01esdata, cast(eaa01json ->> 'peso_bruto' as numeric(18,6)) peso, "+
										"ent.abe01na as entidade, sum(cast(eaa01json ->>'caixa' as numeric(18,6))+(cast(eaa01json ->> 'frasco' as numeric(18,6))/(cast(abm0101json ->> 'volume_caixa' as numeric(18,6))::int)))::real as qtd, "+
										"eaa01totdoc, aag0201nome "+
										"from eaa01 "+ 
										"inner join abb01 on abb01id = eaa01central "+ 
										"inner join abd01 on abd01id = eaa01pcd "+
										"inner join eaa0103 on eaa0103doc = eaa01id "+ 
										"inner join abm01 on abm01id = eaa0103item "+
										"inner join abm0101 on abm0101item = abm01id "+
										"inner join abe01 ent on ent.abe01id = abb01ent "+
										"inner join eaa0102 on eaa0102doc = eaa01id "+ 
										"inner join abe01 desp on desp.abe01id = eaa0102despacho "+ 
										"inner join abe01 redesp on redesp.abe01id = eaa0102redespacho "+
										"inner join eaa0101 on eaa0101doc = eaa01id and eaa0101cobranca = 1 "+
										"inner join aag0201 on aag0201id = eaa0101municipio "+
										getSamWhere().getWherePadrao("WHERE", Eaa01.class) +
										(idsRedesp != null && idsRedesp.size() > 0 ? "and redesp.abe01id in (:idsRedesp) " : "")+
										(dtEmissaoIni != null && dtEmissaoFin != null ? "and abb01data between :dtEmissaoIni and :dtEmissaoFin " : "") +
										(dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "")+
										(idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "")+
										(idsDespacho != null && idsDespacho.size() > 0 ? "and desp.abe01id in (:idsDespacho) " : "")+
										"and abd01es = 1 "+
										"and abd01aplic = 1 "+
										"and eaa01cancdata is null "+
										"group by despacho, redespacho,abb01num, abb01data, eaa01esdata, entidade, peso,aag0201nome,eaa01totdoc "+
										"order by abb01num ");

//										
		if(idsRedesp != null && idsRedesp.size() > 0){
			sql.setParameter("idsRedesp",idsRedesp);
		}

		if(dtEmissaoIni != null && dtEmissaoFin != null ){
			sql.setParameter("dtEmissaoIni",dtEmissaoIni);
			sql.setParameter("dtEmissaoFin",dtEmissaoFin);
		}

		if(dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ){
			sql.setParameter("dtEntradaSaidaIni",dtEntradaSaidaIni);
			sql.setParameter("dtEntradaSaidaFin",dtEntradaSaidaIni);
		}

		if(idsEntidades != null && idsEntidades.size() > 0){
			
			sql.setParameter("idsEntidades",idsEntidades);
		}

		if(idsDespacho != null && idsDespacho.size() > 0){
			sql.setParameter("idsDespacho",idsDespacho);
		}
		
		return sql.getListTableMap();
		

		
	}
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFJlc3VtbyBDYXJyZWdhbWVudG8gIiwidGlwbyI6InJlbGF0b3JpbyJ9