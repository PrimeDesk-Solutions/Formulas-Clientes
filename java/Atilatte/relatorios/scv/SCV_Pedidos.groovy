package Atilatte.relatorios.scv

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCV_Pedidos extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCV - Pedidos";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("pedEntSai", "0");
		filtrosDefault.put("impressao", "0");
		filtrosDefault.put("atendimento", true);
		filtrosDefault.put("atendimento2", true);
		filtrosDefault.put("liquido", true)
		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		def tipos = getListLong("tipos");
		def numeroInicial = getInteger("numeroInicial");
		def numeroFinal = getInteger("numeroFinal");
		def entidades = getListLong("entidades");
		def emissao = getIntervaloDatas("emissao");
		def pedEntSai = getInteger("pedEntSai");
		def impressao = getInteger("impressao")
		def entrega = getIntervaloDatas("entrega")
		def atendimento = [get("atendimento") ? 0 : null, get("atendimento2") ? 1 : null, get("atendimento3") ? 2 : null]
		atendimento.removeAll(Collections.singleton(null))
		def numeroCliente = getString("numeroCliente");
		List<Long> redespacho = getListLong("redespacho");
		String campoLivre1 = getString("campoLivre1");
		String campoLivre2 = getString("campoLivre2");
		String campoLivre3 = getString("campoLivre3");
		String campoLivre4 = getString("campoLivre4");
		Boolean total1 = getBoolean("total1");
		Boolean total2 = getBoolean("total2");
		Boolean total3 = getBoolean("total3");
		Boolean total4 = getBoolean("total4");


		Map<String, String> campos = new HashMap()

		campos.put("1", campoLivre1 != null ? campoLivre1 : "" );
		campos.put("2", campoLivre2 != null ? campoLivre2 : "" );
		campos.put("3", campoLivre3 != null ? campoLivre3 : "" );
		campos.put("4", campoLivre4 != null ? campoLivre4 : "" );

		adicionarParametro("aac10rs", obterEmpresaAtiva().getAac10rs())
		adicionarParametro("titulo", "SCV - Pedidos")
		adicionarParametro("total1", total1);
		adicionarParametro("total2", total2);
		adicionarParametro("total3", total3);
		adicionarParametro("total4", total4);

		List<TableMap> dados = buscarDocumentos(tipos, numeroInicial, numeroFinal, entidades, pedEntSai, emissao, entrega, atendimento, numeroCliente, redespacho, campoLivre1, campoLivre2, campoLivre3, campoLivre4);

		for(dado in dados){
			comporCamposLivres(dado, campos);
		}

		if(impressao == 1) return gerarXLSX("SCV_Pedidos_Excel", dados);

		return gerarPDF("SCV_Pedidos_PDF", dados);

	}
	private List<TableMap> buscarDocumentos(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, List<Long> entidades, Integer pedEntSai, LocalDate[] emissao, LocalDate[] entrega, List<Integer> atendimento, String numeroCliente, List<Long> redespacho,String campoLivre1,String campoLivre2,String campoLivre3,String campoLivre4) {
		def whereTipos = tipos != null && tipos.size() > 0 ? " AND abb01tipo IN (:tipos) " : ""
		def whereEntidades = entidades != null && entidades.size() > 0 ? " AND abb01ent IN (:entidades) " : ""
		def whereCompVenda = pedEntSai == 0 ? " AND eaa01esmov = 0 " : " AND eaa01esmov = 1 "
		def whereEmissao = emissao != null && emissao.size() > 0 ? " AND abb01data BETWEEN :dataIni AND :dataFim " : ""
		def whereNumIni = " AND abb01num >= :numIni "
		def whereNumFim = " AND abb01num <= :numFim "
		def whereEntrega = entrega != null && entrega.size() > 0 ? " AND eaa0103pedido.eaa0103dtEntrega BETWEEN :dtIni AND :dtFim " : ""
		def whereAtendimento  = atendimento != null && atendimento.size() > 0 ? " AND eaa01scvAtend IN (:atendimento) " : ""
		def whereNumCliente = numeroCliente != null && numeroCliente.length() > 0 ? " AND eaa0103pcnum = :numCli ": "";
		def whereRedespacho = redespacho != null && redespacho.size() > 0 ? " AND redespacho.abe01id IN (:redespacho) ": "";


		String campo1 = campoLivre1 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre1+"'"+" as NUMERIC(18,2)) AS " + campoLivre1 + ",  " : "";
		String campo2 = campoLivre2 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre2+"'"+" as NUMERIC(18,2)) AS " + campoLivre2 + ", " : "";
		String campo3 = campoLivre3 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre3+"'"+" as NUMERIC(18,2)) AS " + campoLivre3 + ", "  : "";
		String campo4 = campoLivre4 != null ? "CAST(eaa0103pedido.eaa0103json ->> '"+campoLivre4+"'"+" as NUMERIC(18,2)) AS " + campoLivre4 + ", "  : "";

		String sql = "SELECT " + campo1 + campo2 + campo3 + campo4 +
				"eaa01id, eaa0103nota.eaa0103id, abb01num,abb01serie, abb01data, redespacho.abe01codigo AS codRedespacho, redespacho.abe01na AS nomeResdepacho,  entidade.abe01codigo AS codEntidade, entidade.abe01na AS nomeEntidade, abm01codigo, abm01na, " +
				"eaa0103pedido.eaa0103dtentrega, eaa0103pedido.eaa0103pcnum, uso.aam06codigo AS aam06descr_uso, eaa0103pedido.eaa0103qtComl AS eaa0103qtcoml," +
				"eaa0103pedido.eaa0103unit, eaa0103pedido.eaa0103total," +
				"eaa0103pedido.eaa0103totdoc, eaa0102doc "+
				"FROM eaa01 " +
				"INNER JOIN abb01 ON abb01id = eaa01central " +
				"INNER JOIN abe01 AS entidade ON entidade.abe01id = abb01ent " +
				"INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
				"LEFT JOIN abe01 AS redespacho ON eaa0102redespacho = redespacho.abe01id " +
				"INNER JOIN eaa0103 AS eaa0103pedido ON eaa0103pedido.eaa0103doc = eaa01id " +
				"LEFT JOIN eaa01032 ON eaa01032itemscv = eaa0103id " +
				"LEFT JOIN eaa0103 AS eaa0103nota ON eaa0103nota.eaa0103id = eaa01032itemsrf " +
				"INNER JOIN abm01 ON abm01id = eaa0103pedido.eaa0103item " +
				"LEFT JOIN aam06 AS comercial ON comercial.aam06id = eaa0103pedido.eaa0103umcoml " +
				"LEFT JOIN aam06 AS uso ON uso.aam06id = eaa0103pedido.eaa0103umu " +
				" WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SCV + " "+
				" AND eaa01cancData IS NULL "+
				obterWherePadrao("eaa01","AND") +
				whereTipos + whereEntidades + whereCompVenda + whereEmissao + whereNumIni + whereNumFim + whereEntrega + whereAtendimento + whereNumCliente + whereRedespacho +
				"ORDER BY abb01num ";

		def p1 = tipos != null && tipos.size() > 0 ? criarParametroSql("tipos", tipos) : null
		def p2 = entidades != null && entidades.size() > 0 ? criarParametroSql("entidades", entidades) : null
		def p3 = emissao != null && emissao.size() > 0 ? criarParametroSql("dataIni", emissao[0]) : null
		def p4 = emissao != null && emissao.size() > 0 ? criarParametroSql("dataFim", emissao[1]) : null
		def p5 = criarParametroSql("numIni",numeroInicial)
		def p6 = criarParametroSql("numFim",numeroFinal)
		def p7 = entrega != null && entrega.size() > 0 ? criarParametroSql("dtIni", entrega[0]) : null;
		def p8 = entrega != null && entrega.size() > 0 ? criarParametroSql("dtFim", entrega[1]) : null;
		def p9 = atendimento != null && atendimento.size() > 0 ? criarParametroSql("atendimento", atendimento) : null;
		def p10 = numeroCliente != null && numeroCliente.length() > 0 ? criarParametroSql("numCli", numeroCliente) : null;
		def p11 = redespacho != null && redespacho.size() > 0 ? criarParametroSql("redespacho", redespacho) : null;
		return getAcessoAoBanco().buscarListaDeTableMap(sql,p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11);
	}

	private void comporCamposLivres(TableMap dado, Map<String, String> campos){
		for(campo in campos){
			if(campo.value != null){
				String nomeCampo = buscarNomeCampoLivre(campo.value);
				dado.put("nomeCampo" + campo.key, nomeCampo );
				dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value));
			}
		}
	}

	private String buscarNomeCampoLivre(String campo) {
		def sql = " select aah02descr from aah02 where aah02nome = :nome "
		return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))

	}
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=