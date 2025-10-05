package Atilatte.relatorios.srf

import br.com.multitec.utils.Utils;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload

import java.sql.Time
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.utils.Parametro
import sam.model.entities.aa.Aac10;
import java.time.format.DateTimeFormatter

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SRF_RelacaoPedidosNotas extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SRF - Relação Pedidos e Notas";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		Integer numDocIni = getInteger("numeroInicial");
		Integer numDocFin = getInteger("numeroFinal");
		LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
		LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		List<Long> idsTransp = getListLong("transportadora");
		Aac10 empresa = obterEmpresaAtiva();
		Long idEmpresa = empresa.aac10id;

		// Data Atual Sistema
		LocalDateTime now = LocalDateTime.now();

		// Formato de data e hora
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// Formata a data e hora de acordo com o formato especificado
		String emissaoRelatorio = now.format(formatter);



		List<TableMap> dados = buscarDadosRelatorio(numDocIni,numDocFin,dtEmissao,idsTransp,dtEntradaSaida);

		String periodo = "";
		if(dtEmissao != null){
			periodo = "Período Emissão : " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
		}else if (dtEntradaSaida){
			periodo = "Período Entrada/Saída : " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
		}

		params.put("titulo","Relação de Pedidos e Notas");
		params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
		params.put("periodo", periodo);
		params.put("emissaoRelatorio","Emissão: " + emissaoRelatorio )

		return gerarPDF("SRF_RelacaoPedidosNotas", dados)

	}

	private buscarDadosRelatorio(Integer numDocIni,Integer numDocFin,LocalDate[] dtEmissao,List<Long>idsTransp,LocalDate[] dtEntradaSaida){
		// Data Emissao
		LocalDate dtEmissIni = null;
		LocalDate dtEmissFin = null;

		if(dtEmissao != null ){
			dtEmissIni = dtEmissao[0];
			dtEmissFin = dtEmissao[1]
		}

		// Data Entrada / Saida
		LocalDate dtEntradaSaidaIni = null;
		LocalDate dtEntradaSaidaFin = null;

		if(dtEntradaSaida != null ){
			dtEntradaSaidaIni = dtEntradaSaida[0];
			dtEntradaSaidaFin = dtEntradaSaida[1];
		}



		String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01nota.abb01data between :dtEmissIni and :dtEmissFin " : "";
		String whereDtSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01nota.eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
		String whereTransp = idsTransp != null && idsTransp.size() > 0 ? "and redesp.abe01id in (:idsTransp) " : "";
		String whereNumDoc = "and abb01nota.abb01num between :numDocIni and :numDocFin ";

		Parametro parametroDtEmissaoIni = dtEmissIni != null && dtEmissFin != null ? Parametro.criar("dtEmissIni",dtEmissIni) : null;
		Parametro parametroDtEmissaoFin = dtEmissIni != null && dtEmissFin != null ? Parametro.criar("dtEmissFin",dtEmissFin) : null;
		Parametro parametroDtEntradaSaidaIni = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? Parametro.criar("dtEntradaSaidaIni",dtEntradaSaidaIni) : null;
		Parametro parametroDtEntradaSaidaFin = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? Parametro.criar("dtEntradaSaidaFin",dtEntradaSaidaFin) : null;
		Parametro parametroRedesp = idsTransp != null && idsTransp.size() > 0 ? Parametro.criar("idsTransp",idsTransp) : null;
		Parametro parametroNumDocIni = Parametro.criar("numDocIni",numDocIni);
		Parametro parametroNumDocFin = Parametro.criar("numDocFin",numDocFin);

		String sql = "select distinct abb01nota.abb01data as dataNota, abb01nota.abb01num as numNota,abb01pedido.abb01num as numPed, cast(eaa01nota.eaa01json ->> 'peso_bruto' as numeric(18,6)) as pesoBruto, "+
				"cast(eaa01nota.eaa01json ->> 'peso_liquido' as numeric(18,6)) as pesoLiq,cast(eaa01nota.eaa01json ->> 'caixa' as numeric(18,6)) as volumes, eaa01nota.eaa01totDoc as totDoc, ent.abe01na as naEnt,aag0201nome as municipio, aag02uf as estado, "+
				"redesp.abe01na as redespacho "+
				"from eaa01 as eaa01nota "+
				"inner join eaa0102 as eaa0102nota on eaa0102nota.eaa0102doc = eaa01nota.eaa01id "+
				"inner join eaa0103 as eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id "+
				"inner join abb01 as abb01nota on abb01nota.abb01id = eaa01nota.eaa01central "+
				"inner join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
				"inner join abe01 as ent on ent.abe01id = abb01nota.abb01ent "+
				"inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 "+
				"inner join aag0201 on aag0201id = abe0101municipio "+
				"inner join aag02 on aag02id = aag0201uf "+
				"inner join eaa01032 on eaa01032itemSrf = eaa0103nota.eaa0103id "+
				"inner join eaa0103 as eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv "+
				"inner join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc "+
				"inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central "+
				"where true "+
				"and eaa01nota.eaa01esmov = 1 "+
				whereDtEmissao +
				whereDtSaida +
				whereTransp +
				whereNumDoc+
				"order by abb01nota.abb01num ";

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroRedesp,parametroNumDocIni,parametroNumDocFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin)

	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlbGHDp8OjbyBQZWRpZG9zIGUgTm90YXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=