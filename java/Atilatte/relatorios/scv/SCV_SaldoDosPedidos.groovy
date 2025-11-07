package Atilatte.relatorios.scv;

import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDate
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import br.com.multiorm.Query;
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat
import br.com.multitec.utils.Utils;





public class SCV_SaldoDosPedidos extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCV - Saldo Dos Pedidos";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		LocalDate periodo = LocalDate.now();

		filtrosDefault.put("emissaoIni", periodo);
		filtrosDefault.put("emissaoFin", periodo);
		filtrosDefault.put("numeroInicial","1");
		filtrosDefault.put("numeroFinal","9999999");
		filtrosDefault.put("impressao","0");
		filtrosDefault.put("resumoOperacao", "0")

		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTipoDoc = getListLong("tipo");
		Integer numIni = getInteger("numeroInicial");
		Integer numFin = getInteger("numeroFinal");
		LocalDate dtEmissIni = getLocalDate("emissaoIni");
		LocalDate dtEmissFin = getLocalDate("emissaoFin");
		LocalDate dtEntradaIni = getLocalDate("dataEntSaiIni");
		LocalDate dtEntradaFin = getLocalDate("dataEntSaiFin");
		List<Long> idItens = getListLong("itens");
		List<Long> idEntidades = getListLong("entidades");
		List<Long> idRepresentantes = getListLong("representantes");
		List<Long> idPcd = getListLong("pcd");
		Boolean naoAtend = getBoolean("naoAtend");
		Boolean parcialAtend = getBoolean("parcialAtend");
		Boolean totalAtend = getBoolean("totalAtend");
		Boolean resumoItens = getBoolean("resumoItens");
		Integer resumoOperacao = getInteger("resumoOperacao");

		List<TableMap> dados = new ArrayList<>();

		dados = resumoItens ? buscarPedidosItens(idsTipoDoc,numIni,numFin,dtEmissIni,dtEmissFin,dtEntradaIni,dtEntradaFin,idItens,idEntidades,idRepresentantes,idPcd,naoAtend,parcialAtend, totalAtend, resumoOperacao) : buscarPedidos(idsTipoDoc,numIni,numFin,dtEmissIni,dtEmissFin,dtEntradaIni,dtEntradaFin,idItens,idEntidades,idRepresentantes,idPcd,naoAtend,parcialAtend, totalAtend, resumoOperacao);

		Integer impressao = getInteger("impressao");

		if(!resumoItens && impressao == 0 ){
			return gerarPDF("SCV_SaldoDosPedidos_PDF",dados);
		}else if(!resumoItens && impressao == 1){
			return gerarXLSX("SCV_SaldoDosPedidos_Excel", dados)
		}else if(resumoItens && impressao == 0){
			return gerarPDF("SCV_SaldoDosPedidosItens_PDF",dados);
		}else{
			return gerarXLSX("SCV_SaldoDosPedidosItens_Excel", dados)
		}

	}
	private buscarPedidosItens(List<Long> idsTipoDoc,Integer numIni,Integer numFin,LocalDate dtEmissIni,LocalDate dtEmissFin,LocalDate dtEntradaIni,LocalDate dtEntradaFin,List<Long> idItens,List<Long> idEntidades,List<Long> idRepresentantes,List<Long> idPcd,Boolean naoAtend,Boolean parcialAtend, Boolean totalAtend, Integer resumoOperacao){

		def atendimentos = [-1]
		if(naoAtend) atendimentos.add(0);
		if(parcialAtend) atendimentos.add(1);
		if(totalAtend) atendimentos.add(2);

		Query sql = getSession().createQuery("SELECT aah01codigo,abb01num,abb01data, ent.abe01codigo as codEntidade,ent.abe01na as naEntidade,eaa0103seq, " +
				"CASE WHEN abm01tipo = 0 THEN 'M' "+
				"WHEN abm01tipo = 1 THEN 'P' "+
				"WHEN abm01tipo = 2 THEN 'MER' "+
				"ELSE 'S' "+
				"END AS MPS, abm01codigo AS codItem, abm01na AS naItem,eaa0103dtEntrega AS dtEntrega, "+
				"CAST(eaa0103json ->> 'umv' AS CHARACTER VARYING(3)) AS umv,SUM(eaa0103qtuso) AS qtdUsoPedido, SUM(eaa01032qtUso) AS qtdUsoEntregue, eaa0103pcnum AS pedCliente, "+
				"desp.abe01codigo AS codDesp, desp.abe01na AS naDesp,redesp.abe01codigo AS codRedesp, redesp.abe01na AS naRedesp, eaa01scvatend " +
				"FROM eaa01 "+
				"INNER JOIN abb01 ON eaa01central = abb01id "+
				"INNER JOIN abd01 ON eaa01pcd = abd01id "+
				"INNER JOIN eaa0103 ON eaa01id = eaa0103doc "+
				"LEFT JOIN eaa01032 ON eaa0103id = eaa01032itemscv "+
				"INNER JOIN abm01 ON eaa0103item = abm01id "+
				"INNER JOIN aah01 ON abb01tipo = aah01id "+
				"INNER JOIN abe01 AS ent ON abb01ent = ent.abe01id "+
				"INNER JOIN aam06 ON abm01umu = aam06id "+
				"LEFT JOIN eaa0102 ON eaa0102doc = eaa01id "+
				"LEFT JOIN abe01 AS desp ON desp.abe01id = eaa0102despacho "+
				"LEFT JOIN abe01 AS redesp ON redesp.abe01id = eaa0102redespacho "+
				"LEFT JOIN abe0101 ON abe0101ent = ent.abe01id AND abe0101principal = 1 "+
				"LEFT JOIN aag0201 ON aag0201id = abe0101municipio "+
				"LEFT JOIN aag02 ON aag02id = aag0201uf "+
				"WHERE abb01num BETWEEN :numIni AND :numFin "+
				(idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "")+
				(dtEmissIni != null && dtEmissFin != null ? "AND abb01data BETWEEN :dtEmissIni AND :dtEmissFin " : "")+
				(dtEntradaIni != null && dtEntradaFin != null ? "AND eaa01esdata BETWEEN :dtEntradaIni AND :dtEntradaFin " : "")+
				(idItens != null && idItens.size() > 0 ? "AND abm01id IN (:idItens) " : "")+
				(idEntidades != null ? "AND ENT.abe01id IN (:idEntidades) " : "")+
				(idRepresentantes != null ? "AND (eaa01rep0 IN (:idRepresentantes) OR eaa01rep1 IN (:idRepresentantes) OR eaa01rep2 IN (:idRepresentantes) OR eaa01rep3 IN (:idRepresentantes) OR eaa01rep4 IN (:idRepresentantes)) " : "")+
				(idPcd != null ? "AND abd01id IN (:idPcd) " : "")+
				(atendimentos != null && atendimentos.size() > 0 ? "AND eaa01scvatend IN (:atendimentos) " : "")+
				( resumoOperacao == 0 ? "AND abd01es = 0 " :  "AND abd01es = 1 " ) +
				"AND eaa01cancData IS NULL "+
				"AND abd01aplic = 0 "+
				"AND eaa01gc = :idEmpresa "+
				"GROUP BY aah01codigo,abb01num,abb01data, ent.abe01codigo,ent.abe01na,eaa0103seq,MPS, abm01codigo, abm01na,eaa0103dtEntrega, cast(eaa0103json ->> 'umv' as character varying(3)), eaa0103pcnum,desp.abe01codigo, desp.abe01na,redesp.abe01codigo, redesp.abe01na, eaa01scvatend "+
				"ORDER BY abb01data,abb01num ");


		if(numIni != null && numFin != null){
			sql.setParameter("numIni",numIni);
			sql.setParameter("numFin",numFin);
		}
		if(idsTipoDoc != null){
			sql.setParameter("idsTipoDoc",idsTipoDoc)
		}
		if(dtEmissIni != null && dtEmissFin != null){
			sql.setParameter("dtEmissIni",dtEmissIni);
			sql.setParameter("dtEmissFin",dtEmissFin);
		}
		if(dtEntradaIni != null && dtEntradaFin != null){
			sql.setParameter("dtEntradaIni",dtEntradaIni);
			sql.setParameter("dtEntradaFin",dtEntradaFin);
		}
		if(idItens != null && idItens.size() > 0){
			sql.setParameter("idItens",idItens);
		}
		if(idEntidades != null){
			sql.setParameter("idEntidades",idEntidades);
		}
		if(idRepresentantes != null){
			sql.setParameter("idRepresentantes",idRepresentantes);
		}
		if(idPcd != null){
			sql.setParameter("idPcd",idPcd);
		}
		if(atendimentos != null && atendimentos.size() > 0){
			sql.setParameter("atendimentos",atendimentos);
		}

		sql.setParameter("idEmpresa", obterEmpresaAtiva().getAac10id())

		List<TableMap> tmPedidos = sql.getListTableMap();
		List<TableMap> tmDados = new ArrayList();



		for(TableMap pedidos : tmPedidos){

			if(pedidos.getDate("dtEntrega") != null){
				//Recupera a data atual
				LocalDate dtAtual = LocalDate.now();
	
				//Formatador de data
				DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
				if(pedidos.getDate("dtEntrega") == null) throw new ValidacaoException("Pedido Nº " +pedidos.getInteger("abb01num").toString()+" sem data de entrega.");
	
				//Define a data de entrega do pedido como texto
				String txtDataentrega = pedidos.getDate("dtEntrega").toString();
	
				//Define a data de entrega como localdate para calcular diferença de dias
				LocalDate dataEntrega = LocalDate.parse(txtDataentrega, formato);
	

				Long diferencaEmDias = ChronoUnit.DAYS.between(dtAtual, dataEntrega);
	
				pedidos.put("calculoDias",diferencaEmDias);

	
				//Define o valor entregue para zero, caso não entrege
				if(pedidos.getBigDecimal_Zero("qtdUsoEntregue") == null){
					pedidos.put("entregue",0)
				}else{
					pedidos.put("entregue",pedidos.getBigDecimal_Zero("qtdUsoEntregue"))
				}
	
				// Define Status de Atendimento
				if(pedidos.getInteger("qtdUsoEntregue") == null ){
					pedidos.put("atendido", "0-Não Atendido");
				}else if(pedidos.getInteger("qtdUsoEntregue") != null && pedidos.getInteger("qtdUsoEntregue") < pedidos.getInteger("qtdUsoPedido")  ){
					pedidos.put("atendido", "1-Parcialmente Atendido");
				}else{
					pedidos.put("atendido", "2-Totalmente Atendido");
				}
			}

			tmDados.add(pedidos);

		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String txtDataIni = ""
		String txtDataFin = ""
		if(dtEmissIni != null && dtEmissFin != null){
			txtDataIni = dtEmissIni.format(formatter);
			txtDataFin = dtEmissFin.format(formatter);

		}

		params.put("titulo", "Saldo dos Pedidos De Venda");
		params.put("periodo", "Emitido em: "+txtDataIni+" a "+txtDataFin)
		params.put("empresa", getVariaveis().getAac10().getAac10na());


		return tmDados;

	}
	private buscarPedidos(List<Long> idsTipoDoc,Integer numIni,Integer numFin,LocalDate dtEmissIni,LocalDate dtEmissFin,LocalDate dtEntradaIni,LocalDate dtEntradaFin,List<Long> idItens,List<Long> idEntidades,List<Long> idRepresentantes,List<Long> idPcd,Boolean naoAtend,Boolean parcialAtend, Boolean totalAtend, Integer resumoOperacao){
		def atendimentos = [-1]
		if(naoAtend) atendimentos.add(0);
		if(parcialAtend) atendimentos.add(1);
		if(totalAtend) atendimentos.add(2);

		Query sql = getSession().createQuery("SELECT aah01codigo,abb01num,abb01data, ent.abe01codigo AS codEntidade,ent.abe01na AS naEntidade, " +
				"eaa0103dtEntrega AS dtEntrega,SUM(eaa0103totDoc) AS totDoc, SUM(CAST(eaa0103json ->> 'peso_bruto' AS NUMERIC(18,2))) AS pesoBruto, SUM(CAST(eaa0103json ->> 'peso_liquido' AS NUMERIC(18,6))) AS pesoLiquido, "+
				"SUM(eaa0103qtUso) AS qtdUsoPedido, SUM(eaa01032qtUso) AS qtdUsoEntregue, eaa0103pcnum AS pedCliente, "+
				"desp.abe01codigo AS codDesp, desp.abe01na AS naDesp,redesp.abe01codigo AS codRedesp, redesp.abe01na AS naRedesp, aag02uf AS uf, aag0201nome AS municipio,eaa01scvatend " +
				"FROM eaa01 "+
				"INNER JOIN abb01 ON eaa01central = abb01id "+
				"INNER JOIN abd01 ON eaa01pcd = abd01id "+
				"INNER JOIN eaa0103 ON eaa01id = eaa0103doc "+
				"LEFT JOIN eaa01032 ON eaa0103id = eaa01032itemscv "+
				"INNER JOIN abm01 ON eaa0103item = abm01id "+
				"INNER JOIN aah01 ON abb01tipo = aah01id "+
				"INNER JOIN abe01 as ent ON abb01ent = ent.abe01id "+
				"INNER JOIN aam06 ON abm01umu = aam06id "+
				"LEFT JOIN eaa0102 ON eaa0102doc = eaa01id "+
				"LEFT JOIN abe01 AS desp ON desp.abe01id = eaa0102despacho "+
				"LEFT JOIN abe01 AS redesp ON redesp.abe01id = eaa0102redespacho "+
				"LEFT JOIN abe0101 ON abe0101ent = ent.abe01id and abe0101principal = 1 "+
				"LEFT JOIN aag0201 ON aag0201id = abe0101municipio "+
				"LEFT JOIN aag02 ON aag02id = aag0201uf "+
				"WHERE abb01num BETWEEN :numIni AND :numFin "+
				(idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "")+
				(dtEmissIni != null && dtEmissFin != null ? "AND abb01data BETWEEN :dtEmissIni AND :dtEmissFin " : "")+
				(dtEntradaIni != null && dtEntradaFin != null ? "AND eaa01esdata BETWEEN :dtEntradaIni AND :dtEntradaFin " : "")+
				(idItens != null && idItens.size() > 0 ? "AND abm01id IN (:idItens) " : "")+
				(idEntidades != null ? "AND ent.abe01id IN (:idEntidades) " : "")+
				(idRepresentantes != null ? "AND (eaa01rep0 IN (:idRepresentantes) or eaa01rep1 IN (:idRepresentantes) or eaa01rep2 IN (:idRepresentantes) or eaa01rep3 IN (:idRepresentantes) or eaa01rep4 IN (:idRepresentantes)) " : "")+
				(idPcd != null ? "AND abd01id IN (:idPcd) " : "")+
				(atendimentos != null && atendimentos.size() > 0 ? "AND eaa01scvatend IN (:atendimentos) " : "")+
				( resumoOperacao == 0 ? "AND abd01es = 0 " :  "AND abd01es = 1 " ) +
				"AND abd01aplic = 0 "+
				"AND eaa01gc = :idEmpresa "+
				"AND eaa01cancData IS NULL "+
				"GROUP BY aah01codigo,abb01num,abb01data, ent.abe01codigo,ent.abe01na,eaa0103dtEntrega, eaa0103pcnum,eaa01scvatend, "+
				"desp.abe01codigo, desp.abe01na,redesp.abe01codigo, redesp.abe01na, aag02uf, aag0201nome "+
				"ORDER BY abb01data,abb01num ");

		if(numIni != null && numFin != null){
			sql.setParameter("numIni",numIni);
			sql.setParameter("numFin",numFin);
		}
		if(idsTipoDoc != null){
			sql.setParameter("idsTipoDoc",idsTipoDoc)
		}
		if(dtEmissIni != null && dtEmissFin != null){
			sql.setParameter("dtEmissIni",dtEmissIni);
			sql.setParameter("dtEmissFin",dtEmissFin);
		}
		if(dtEntradaIni != null && dtEntradaFin != null){
			sql.setParameter("dtEntradaIni",dtEntradaIni);
			sql.setParameter("dtEntradaFin",dtEntradaFin);
		}
		if(idItens != null && idItens.size() > 0){
			sql.setParameter("idItens",idItens);
		}
		if(idEntidades != null){
			sql.setParameter("idEntidades",idEntidades);
		}
		if(idRepresentantes != null){
			sql.setParameter("idRepresentantes",idRepresentantes);
		}
		if(idPcd != null){
			sql.setParameter("idPcd",idPcd);
		}
		if(atendimentos != null && atendimentos.size() > 0){
			sql.setParameter("atendimentos",atendimentos);
		}

		sql.setParameter("idEmpresa", obterEmpresaAtiva().getAac10id())

		List<TableMap> tmPedidos = sql.getListTableMap();
		List<TableMap> tmDados = new ArrayList();

		for(TableMap pedidos : tmPedidos){

			if(pedidos.getDate("dtEntrega") != null){

				//Recupera a data atual
				LocalDate dtAtual = LocalDate.now();
	
				//Formatador de data
				DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
				if(pedidos.getDate("dtEntrega") == null) throw new ValidacaoException("Pedido Nº " +pedidos.getInteger("abb01num").toString()+" sem data de entrega.");
	
				//Define a data de entrega do pedido como texto
				String txtDataentrega = pedidos.getDate("dtEntrega").toString();
	
				//Define a data de entrega como localdate para calcular diferença de dias
				LocalDate dataEntrega = LocalDate.parse(txtDataentrega, formato);
	
				//Calcula a diferença de dias da data atual e a data de entrega
				Long diferencaEmDias = ChronoUnit.DAYS.between(dtAtual, dataEntrega);
	
				pedidos.put("calculoDias",diferencaEmDias);

				//Define o valor entregue para zero, caso não entrege
				if(pedidos.getBigDecimal_Zero("qtdUsoEntregue") == null){
					pedidos.put("entregue",0)
				}else{
					pedidos.put("entregue",pedidos.getBigDecimal_Zero("qtdUsoEntregue"))
				}
	
				// Define Status de Atendimento
				if(pedidos.getInteger("eaa01scvatend") == 0){
					pedidos.put("atendido", "0-Não Atendido");
				}else if(pedidos.getInteger("eaa01scvatend") == 1){
					pedidos.put("atendido", "1-Parcialmente Atendido");
				}else{
					pedidos.put("atendido", "2-Totalmente Atendido");
				}
			}
			tmDados.add(pedidos);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String txtDataIni = ""
		String txtDataFin = ""
		if(dtEmissIni != null && dtEmissFin != null){
			txtDataIni = dtEmissIni.format(formatter);
			txtDataFin = dtEmissFin.format(formatter);

		}

		params.put("titulo", "Saldo dos Pedidos De Venda");
		params.put("periodo", "Emitido em: "+txtDataIni+" a "+txtDataFin)
		params.put("empresa", getVariaveis().getAac10().getAac10na());


		return tmDados;

	}

}
//meta-sis-eyJkZXNjciI6IlNDViAtIFNhbGRvIERvcyBQZWRpZG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDViAtIFNhbGRvIERvcyBQZWRpZG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDViAtIFNhbGRvIERvcyBQZWRpZG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9