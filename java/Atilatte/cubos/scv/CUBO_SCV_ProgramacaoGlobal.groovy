package Atilatte.cubos.scv;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import sam.core.variaveis.MDate
import br.com.multitec.utils.DateUtils

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sam.model.entities.aa.Aac10;

public class CUBO_SCV_ProgramacaoGlobal extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CUBO - SCV - Programação Global";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {


		return criarFiltros(
				"operacao",0,
				"tipoOperacao",0,
				"atendimento",0,
				"cancelados", 1,

		);
	}
	@Override
	public DadosParaDownload executar() {
		Integer operacao = getInteger("operacao");
		Integer tipoOperacao = getInteger("tipoOperacao");
		LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
		LocalDate[] dtEntrega =   getIntervaloDatas("dataEntrega");
		List<Long> entidades = getListLong("entidade");
		Integer atendimento = getInteger("atendimento");
		Integer cancelamento = getInteger("cancelamento");
		LocalDate[] dtEmissao =   getIntervaloDatas("dataEmissao");
		List<Long> pcd = getListLong("pcd");
		List<Long> tipoDocumento = getListLong("tipoDoc");
		List<Long> itens = getListLong("itens");
		String hora = getString("hora");
		Aac10 empresa = obterEmpresaAtiva();
		Long idEmpresa = empresa.aac10id;

		List<TableMap> dadosRelatorio = buscarDadosRelatorio(operacao,tipoOperacao,dtEntradaSaida,dtEntrega,entidades,atendimento,cancelamento,dtEmissao,
				pcd,tipoDocumento,itens,idEmpresa,hora);

		for(dados in dadosRelatorio){
			def volumeCaixa = dados.getBigDecimal_Zero("volumeCaixa");
			if(volumeCaixa == 0) interromper("O campo Volume Caixa no cadastro do item "+ dados.getString("codItem") + " - " + dados.getString("naItem") +" para conversão de caixas é inválido.")
		
			def qtd = dados.getBigDecimal_Zero("qtd");
			if(qtd > 0){
				dados.put("caixa", qtd / volumeCaixa);
			}

		}

		return gerarXLSX("CUBO_SCV_ProgramacaoCarregamentoGlobal",dadosRelatorio);

	}

	private List<TableMap> buscarDadosRelatorio(Integer operacao,Integer tipoOperacao,LocalDate[] dtEntradaSaida,LocalDate[] dtEntrega,List<Long>entidades,Integer atendimento,Integer cancelamento,LocalDate[] dtEmissao,
												List<Long> pcd,List<Long> tipoDocumento, List<Long> itens, Long idEmpresa, String hora){

		LocalDate dtEntradaSaidaIni = null;
		LocalDate dtEntradaSaidaFin = null;
		LocalDate dtEntregaIni = null;
		LocalDate dtEntregaFin = null;
		LocalDate dtEmissaoIni = null;
		LocalDate dtEmissaoFin = null;

		if(dtEntradaSaida != null){
			dtEntradaSaidaIni = dtEntradaSaida[0];
			dtEntradaSaidaFin = dtEntradaSaida[1];
		}

		if(dtEntrega != null){
			dtEntregaIni = dtEntrega[0];
			dtEntregaFin = dtEntrega[1];
		}

		if(dtEmissao != null){
			dtEmissaoIni = dtEmissao[0];
			dtEmissaoFin = dtEmissao[1];
		}



		String whereOperDocumento = operacao == 0 ? "and eaa01esMov = 0 " : "and eaa01esMov = 1 ";
		String whereTipoOper = tipoOperacao == 0 ? "and eaa01clasDoc = 0 " : "and eaa01clasDoc = 1 ";
		String whereEmissao = dtEmissaoIni != null && dtEmissaoFin != null ? "AND abb01data between :dtEmissaoIni  and :dtEmissaoFin " : ""
		String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : ""
		String whereEntidade = entidades != null && entidades.size() > 0 ? "and abe01id in (:entidades) " : ""
		String whereDtEntrega = dtEntregaIni != null && dtEntregaFin != null ? "and eaa0103dtentrega between :dtEntregaIni and :dtEntregaFin " : ""
		String whereTipoDoc = tipoDocumento != null && tipoDocumento.size() > 0 ? "and aah01id in (:tipoDocumento) " : ""
		String wherePcd = pcd != null && pcd.size() > 0 ? "and abd01id in (:pcd) " : ""
		String whereItem = itens != null && itens.size() > 0  ? "and abm01id in (:itens) " : "";
		String whereHora = hora != null ? "and substring(abb01operhora::text,0,6) <= :hora " : ""


		Parametro ParametroEmissaoIni = dtEmissaoIni != null && dtEmissaoFin != null ? Parametro.criar("dtEmissaoIni",dtEmissaoIni) : null
		Parametro ParametroEmissaoFin = dtEmissaoIni != null && dtEmissaoFin != null ? Parametro.criar("dtEmissaoFin",dtEmissaoFin) : null
		Parametro ParametroDtEntradaSaidaIni = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? Parametro.criar("dtEntradaSaidaIni",dtEntradaSaidaIni) : null
		Parametro ParametroDtEntradaSaidaFin = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? Parametro.criar("dtEntradaSaidaFin",dtEntradaSaidaFin) : null
		Parametro ParametroEntidade = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades",entidades) : null
		Parametro ParametroDtEntregaIni = dtEntregaIni != null && dtEntregaFin != null ? Parametro.criar("dtEntregaIni",dtEntregaIni) : null
		Parametro ParametroDtEntregaFin = dtEntregaIni != null && dtEntregaFin != null ? Parametro.criar("dtEntregaFin",dtEntregaFin) : null
		Parametro ParametroTipoDoc = tipoDocumento != null && tipoDocumento.size() > 0 ? Parametro.criar("tipoDocumento",tipoDocumento) : null
		Parametro ParametroPcd = pcd != null && pcd.size() > 0 ? Parametro.criar("pcd",pcd) : null
		Parametro ParametroItem = itens != null && itens.size() > 0  ? Parametro.criar("itens",itens) : null
		Parametro ParametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
		Parametro ParametroHora = hora != null ?  Parametro.criar("hora",hora) : null;

		String sql = "SELECT abm01codigo as codItem, abm01na as naItem,eaa0103dtentrega as dtEntrega, aam06codigo as umu,CAST(abm0101json ->> 'volume_caixa' as numeric(18,6)) as volumeCaixa, SUM(eaa0103qtComl) AS qtd "+
				"FROM eaa01 "+
				"INNER JOIN abb01 ON abb01id = eaa01central "+
				"INNER JOIN abe01 ON abe01id = abb01ent "+
				"INNER JOIN aah01 ON aah01id = abb01tipo "+
				"INNER JOIN abd01 ON abd01id = eaa01pcd "+
				"INNER JOIN eaa0103 ON eaa0103doc = eaa01id "+
				"INNER JOIN abm01 ON abm01id = eaa0103item "+
				"INNER JOIN abm0101 ON abm0101item = abm01id "+
				"INNER JOIN aam06 on aam06id = abm01umu "+
				"WHERE TRUE " +
				whereEmissao +
				whereDtEntradaSaida +
				whereEntidade +
				whereDtEntrega +
				whereTipoDoc +
				wherePcd +
				whereItem +
				whereOperDocumento +
				whereTipoOper +
				whereHora +
				"and eaa01gc = :idEmpresa "+
				"and eaa01scvatend = 0 "+
				"and eaa01cancdata is null "+
				"and abm01tipo = 1 "+
				"GROUP BY abm01codigo, abm01na,eaa0103dtentrega,aam06codigo,volumeCaixa "+
				"ORDER BY abm01codigo";




		return getAcessoAoBanco().buscarListaDeTableMap(sql,ParametroEmissaoIni,ParametroEmissaoFin,ParametroDtEntradaSaidaIni,ParametroDtEntradaSaidaFin,ParametroEntidade,ParametroDtEntregaIni,ParametroDtEntregaFin,
				ParametroTipoDoc,ParametroPcd,ParametroItem,ParametroEmpresa,ParametroHora)
	}
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ1YgLSBQcm9ncmFtYcOnw6NvIEdsb2JhbCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==