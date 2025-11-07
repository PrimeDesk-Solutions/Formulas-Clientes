package Atilatte.cubos.sce

import org.apache.tomcat.jni.Local;
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter;

public class SCE_SaldoAtualEstoque extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "CUBO - SCE- Saldo Atual Estoque";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("impressao","0")
		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public DadosParaDownload executar() {
		//if(obterUsuarioLogado().getAab10id() != 1331927) interromper("Relatório em manutenção...")
		List<Long> idItem = getListLong("item");
		List<Long> status = getListLong("status");
		List<Long> local = getListLong("local");
		List<Integer> mps = getListInteger("mpm")
		Integer impressao = getInteger("impressao");

		List<TableMap> saldosProprios = buscarSaldosProprios(idItem,status,local,mps);
		List<TableMap> saldosTerceiros = buscarSaldosTerceiro(idItem,status,local,mps);

		List<TableMap> dadosRelatorio = new ArrayList();


		dadosRelatorio.addAll(saldosProprios);
		dadosRelatorio.addAll(saldosTerceiros);

		// Data Atual Sistema
		LocalDateTime now = LocalDateTime.now();

		// Formato de data e hora
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		// Formata a data e hora de acordo com o formato especificado
		String emissaoRelatorio = now.format(formatter);

		for (dado in dadosRelatorio){
			LocalDate dtFabric = dado.getDate("fabricacao");
			LocalDate dtValid = dado.getDate("validade");
			LocalDate dtAtual = LocalDate.now();

			if(dtFabric != null && dtValid != null && dtFabric != dtValid  ){
				def percent = ((dtAtual - dtFabric ) / (dtValid - dtFabric)) * 100
				dado.put("percentValid",percent )	
			}
			
			
		}
		//((current_date - bcc0201fabric)::numeric / (bcc0201validade - bcc0201fabric)::numeric) * 100

		params.put("emissaoRelatorio","Emissão: " + emissaoRelatorio );
		params.put("titulo","SCE - Saldo Atual Estoque");
		params.put("empresa",obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na())


		if(impressao == 1) return gerarXLSX("SaldoAtualEstoque(Excel)",dadosRelatorio);

		return gerarPDF("SaldoAtualEstoque(PDF)", dadosRelatorio);
	}

	private List<TableMap> buscarSaldosProprios(List<Long>idItem,List<Long>status,List<Long>local, List<Integer> mps){

		String whereItens = idItem != null && idItem.size() > 0 ? "abm01id in (:idItem) " : "";
		String whereStatus = status != null && status.size() > 0 ? "and aam04id in (:status) " : "";
		String whereLocal = local != null && local.size() > 0 ? "and abm15id in (:local) " : "";
		String tipo = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";

		Parametro parametroItens = idItem != null && idItem.size() > 0 ? Parametro.criar("idItem",idItem) : null;
		Parametro parametroStatus = status != null && status.size() > 0 ? Parametro.criar("status",status) : null;
		Parametro parametroLocal = local != null && local.size() > 0 ? Parametro.criar("local",local) : null;
		Parametro parametroTipo = !mps.contains(-1) ? Parametro.criar("mps",mps) : null;


		String sql = "select  abm01livre as codLivre, case when abm01tipo = 0 then 'M' else 'P' end MPS, abm01codigo as codProduto, abm01na as naProduto,bcc0201lote as lote, bcc0201serie as serie,aam04codigo as status, abm15nome as local, "+
				"bcc0201qt as qtd, bcc0201validade as validade, bcc0201fabric as fabricacao "+
				"from bcc02 "+
				"inner join bcc0201 on bcc0201saldo = bcc02id "+
				"inner join abm01 on abm01id = bcc02item "+
				"inner join aam04 on aam04id = bcc02status "+
				"inner join abm15 on abm15id = bcc02ctrl0 "+
				"where bcc0201qt > 0 "+
				"and bcc02centralest is null "+
				whereItens +
				whereStatus +
				whereLocal +
				tipo +
				"order by abm01livre";


		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens,parametroStatus,parametroLocal,parametroTipo)
	}

	private List<TableMap> buscarSaldosTerceiro(List<Long>idItem,List<Long>status,List<Long>local, List<Integer> mps){

		String whereItens = idItem != null && idItem.size() > 0 ? "abm01id in (:idItem) " : "";
		String whereStatus = status != null && status.size() > 0 ? "and aam04id in (:status) " : "";
		String whereLocal = local != null && local.size() > 0 ? "and abm15id in (:local) " : "";
		String tipo = !mps.contains(-1) ? "and abm01tipo in (:mps) " : "";

		Parametro parametroItens = idItem != null && idItem.size() > 0 ? Parametro.criar("idItem",idItem) : null;
		Parametro parametroStatus = status != null && status.size() > 0 ? Parametro.criar("status",status) : null;
		Parametro parametroLocal = local != null && local.size() > 0 ? Parametro.criar("local",local) : null;
		Parametro parametroTipo = !mps.contains(-1) ? Parametro.criar("mps",mps) : null;


		String sql = "select abm01livre as codLivre, case when abm01tipo = 0 then 'M' else 'P' end MPS, abm01codigo as codProduto, abm01na as naProduto,bcc0201lote as lote, bcc0201serie as serie,aam04codigo as status, abm15nome as local, "+
				"bcc0201qt as qtd "+
				"from bcc02 "+
				"inner join bcc0201 on bcc0201saldo = bcc02id "+
				"inner join abm01 on abm01id = bcc02item "+
				"inner join aam04 on aam04id = bcc02status "+
				"inner join abm15 on abm15id = bcc02ctrl0 "+
				"where bcc0201qt > 0 "+
				"and bcc02centralest is not null "+
				whereItens +
				whereStatus +
				whereLocal +
				tipo +
				"order by abm01livre";

		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens,parametroStatus,parametroLocal,parametroTipo)
	}


}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTQ0UtIFNhbGRvIEF0dWFsIEVzdG9xdWUiLCJ0aXBvIjoicmVsYXRvcmlvIn0=