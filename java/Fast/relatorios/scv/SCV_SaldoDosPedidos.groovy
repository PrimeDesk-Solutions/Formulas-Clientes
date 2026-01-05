package Fast.relatorios.scv;

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

		Query sql = getSession().createQuery("select aah01codigo,abb01num,abb01data, ent.abe01codigo as codEntidade,ent.abe01na as naEntidade,eaa0103seq, " +
				"case when abm01tipo = 0 then 'M' "+
				"when abm01tipo = 1 then 'P' "+
				"when abm01tipo = 2 then 'MER' "+
				"else 'S' "+
				"end as MPS, abm01codigo as codItem, abm01na as naItem,eaa0103dtEntrega as dtEntrega, "+
				"cast(eaa0103json ->> 'umv' as character varying(3)) as umv,eaa0103qtcoml as comercial, eaa01032qtcoml as comercialEntregue, eaa0103pcnum as pedCliente, "+
				"desp.abe01codigo as codDesp, desp.abe01na as naDesp,redesp.abe01codigo as codRedesp, redesp.abe01na as naRedesp, eaa01scvatend " +
				"from eaa01 "+
				"inner join abb01 on eaa01central = abb01id "+
				"inner join abd01 on eaa01pcd = abd01id "+
				"inner join eaa0103 on eaa01id = eaa0103doc "+
				"left join eaa01032 on eaa0103id = eaa01032itemscv "+
				"inner join abm01 on eaa0103item = abm01id "+
				"inner join aah01 on abb01tipo = aah01id "+
				"inner join abe01 as ent on abb01ent = ent.abe01id "+
				"inner join aam06 on abm01umu = aam06id "+
				"left join eaa0102 on eaa0102doc = eaa01id "+
				"left join abe01 as desp on desp.abe01id = eaa0102despacho "+
				"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
				"left join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 "+
				"left join aag0201 on aag0201id = abe0101municipio "+
				"left join aag02 on aag02id = aag0201uf "+
				"where abb01num between :numIni and :numFin "+
				(idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "")+
				(dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "")+
				(dtEntradaIni != null && dtEntradaFin != null ? "and eaa01esdata between :dtEntradaIni and :dtEntradaFin " : "")+
				(idItens != null && idItens.size() > 0 ? "and abm01id in (:idItens) " : "")+
				(idEntidades != null ? "and ENT.abe01id in (:idEntidades) " : "")+
				(idRepresentantes != null ? "and (eaa01rep0 in (:idRepresentantes) or eaa01rep1 in (:idRepresentantes) or eaa01rep2 in (:idRepresentantes) or eaa01rep3 in (:idRepresentantes) or eaa01rep4 in (:idRepresentantes)) " : "")+
				(idPcd != null ? "and abd01id in (:idPcd) " : "")+
				(atendimentos != null && atendimentos.size() > 0 ? "and eaa01scvatend in (:atendimentos) " : "")+
				( resumoOperacao == 0 ? "and abd01es = 0 " :  "and abd01es = 1 " ) + 
				"and abd01aplic = 0 "+
				"and eaa01gc = :idEmpresa "+
				"order by abb01data,abb01num ");

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
	
				//if(dtAtual > dataEntrega ){
				//Calcula a diferença de dias da data atual e a data de entrega
				Long diferencaEmDias = ChronoUnit.DAYS.between(dtAtual, dataEntrega);
	
				pedidos.put("calculoDias",diferencaEmDias);
				//}
	
				//Define o valor entregue para zero, caso não entrege
				if(pedidos.getBigDecimal_Zero("comercialEntregue") == null){
					pedidos.put("entregue",0)
				}else{
					pedidos.put("entregue",pedidos.getBigDecimal_Zero("comercialEntregue"))
				}
	
				// Define Status de Atendimento
				if(pedidos.getInteger("comercialEntregue") == null ){
					pedidos.put("atendido", "0-Não Atendido");
				}else if(pedidos.getInteger("comercialEntregue") != null && pedidos.getInteger("comercialEntregue") < pedidos.getInteger("comercial")  ){
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

		Query sql = getSession().createQuery("select aah01codigo,abb01num,abb01data, ent.abe01codigo as codEntidade,ent.abe01na as naEntidade, " +
				"eaa0103dtEntrega as dtEntrega,SUM(eaa0103totDoc) as totDoc, SUM(CAST(eaa0103json ->> 'peso_bruto' as numeric(18,2))) as pesoBruto, SUM(CAST(eaa0103json ->> 'peso_liquido' as numeric(18,6))) as pesoLiquido, "+
				"SUM(eaa0103qtcoml) as comercial, SUM(eaa01032qtcoml) as comercialEntregue, eaa0103pcnum as pedCliente, "+
				"desp.abe01codigo as codDesp, desp.abe01na as naDesp,redesp.abe01codigo as codRedesp, redesp.abe01na as naRedesp, aag02uf as uf, aag0201nome as municipio,eaa01scvatend " +
				"from eaa01 "+
				"inner join abb01 on eaa01central = abb01id "+
				"inner join abd01 on eaa01pcd = abd01id "+
				"inner join eaa0103 on eaa01id = eaa0103doc "+
				"left join eaa01032 on eaa0103id = eaa01032itemscv "+
				"inner join abm01 on eaa0103item = abm01id "+
				"inner join aah01 on abb01tipo = aah01id "+
				"inner join abe01 as ent on abb01ent = ent.abe01id "+
				"inner join aam06 on abm01umu = aam06id "+
				"left join eaa0102 on eaa0102doc = eaa01id "+
				"left join abe01 as desp on desp.abe01id = eaa0102despacho "+
				"left join abe01 as redesp on redesp.abe01id = eaa0102redespacho "+
				"left join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 "+
				"left join aag0201 on aag0201id = abe0101municipio "+
				"left join aag02 on aag02id = aag0201uf "+
				"where abb01num between :numIni and :numFin "+
				(idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "")+
				(dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "")+
				(dtEntradaIni != null && dtEntradaFin != null ? "and eaa01esdata between :dtEntradaIni and :dtEntradaFin " : "")+
				(idItens != null && idItens.size() > 0 ? "and abm01id in (:idItens) " : "")+
				(idEntidades != null ? "and ent.abe01id in (:idEntidades) " : "")+
				(idRepresentantes != null ? "and (eaa01rep0 in (:idRepresentantes) or eaa01rep1 in (:idRepresentantes) or eaa01rep2 in (:idRepresentantes) or eaa01rep3 in (:idRepresentantes) or eaa01rep4 in (:idRepresentantes)) " : "")+
				(idPcd != null ? "and abd01id in (:idPcd) " : "")+
				(atendimentos != null && atendimentos.size() > 0 ? "and eaa01scvatend in (:atendimentos) " : "")+
				( resumoOperacao == 0 ? "and abd01es = 0 " :  "and abd01es = 1 " ) + 
				"and abd01aplic = 0 "+
				"and eaa01gc = :idEmpresa "+
				"group by aah01codigo,abb01num,abb01data, ent.abe01codigo,ent.abe01na,eaa0103dtEntrega, eaa0103pcnum,eaa01scvatend, "+
				"desp.abe01codigo, desp.abe01na,redesp.abe01codigo, redesp.abe01na, aag02uf, aag0201nome "+
				"order by abb01data,abb01num ");

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
	
				//if(dtAtual > dataEntrega ){
				//Calcula a diferença de dias da data atual e a data de entrega
				Long diferencaEmDias = ChronoUnit.DAYS.between(dtAtual, dataEntrega);
	
				pedidos.put("calculoDias",diferencaEmDias);
				//}
	
				//Define o valor entregue para zero, caso não entrege
				if(pedidos.getBigDecimal_Zero("comercialEntregue") == null){
					pedidos.put("entregue",0)
				}else{
					pedidos.put("entregue",pedidos.getBigDecimal_Zero("comercialEntregue"))
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