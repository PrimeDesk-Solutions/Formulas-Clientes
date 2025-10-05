package Atilatte.relatorios.scf

import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table
import org.apache.tomcat.jni.Local;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_InconsistenciasContabeis extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Inconsistências Contábeis";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("visualizacao", "0");

        
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        LocalDate[] periodo = getIntervaloDatas("periodo");
        List<Long> idsContasCorrentes = getListLong("contaCorrente");
        Integer visualizacao = getInteger("visualizacao");
        Integer impressao = getInteger("impressao");

        List<TableMap> dados = buscarDadosRelaorio(periodo, idsContasCorrentes, visualizacao);

	   //if(obterUsuarioLogado().getAab10user() != 'MASTER2') interromper("RELATÓRIO EM MANUTENÇÃO")
        return gerarXLSX("SCF_InconsistenciasContabeis", dados);

    }

    private buscarDadosRelaorio(LocalDate[] periodo, List<Long> idsContasCorrentes, Integer visualizacao ){

        // Periodo Inicial e Final
        LocalDate periodoIni = null;
        LocalDate periodoFin = null;

        if(periodo != null){
            periodoIni = periodo[0];
            periodoFin = periodo[1];
        }

        String whereEmpresa = "where dab10eg = :idEmpresa ";
        String wherePeriodo = periodo != null ? "and dab10data between :periodoIni  and :periodoFin " : "";
        String whereCC = idsContasCorrentes != null && idsContasCorrentes.size() > 0 ? "and dab01id in (:idsContasCorrentes) " : "";

        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroPeriodoIni = periodo != null ? Parametro.criar("periodoIni", periodoIni ) : null;
        Parametro parametroPeriodoFin = periodo != null ? Parametro.criar("periodoFin", periodoFin ) : null;
        Parametro parametroContasCorrente = idsContasCorrentes != null && idsContasCorrentes.size() > 0 ? Parametro.criar("idsContasCorrentes", idsContasCorrentes) : null;

		String sql =   "select distinct abb01id as idCentral, dab10data as dtFinanc, abb01num as numDocFinanc, abb01parcela, abf10codigo as codNatFinan, "+
					"dab01codigo as codCCFinanc, abc10financ.abc10codigo as codContaContabilFinanc,dab10id, dab10valor as valorDoFinanc, dab10011valor as valorContabil, "+
					"abe01codigo as codEntidade, abe01na as naEntidade "+
					"from dab10  "+
					"inner join abb01 on dab10central = abb01id  "+
					"inner join abe01 on abb01ent = abe01id  "+
					"left join dab1001 on dab1001lct = dab10id  "+
					"left join dab10011 on dab10011depto = dab1001id  "+
					"left join abf10 on dab10011nat = abf10id  "+
					"left join abf1001 on abf1001nat = abf10id  "+
					"left join abc10 as abc10financ on abc10financ.abc10id = abf1001cta  "+
					"left join dab1002 on dab1002lct = dab10id  "+
					"left join dab01 on dab01id = dab1002cc  "+
					whereEmpresa +
		               wherePeriodo +
		               whereCC +
		               "order by dab10data, abb01num";


        List<TableMap> tmRegistros = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroPeriodoIni, parametroPeriodoFin, parametroContasCorrente );
        List<TableMap> tmGeral = new ArrayList<>();

        String natureza;

	for(registro in tmRegistros){
		Long idCentral = registro.getLong("idCentral");
		String codigoContaContabilFinanc = registro.getString("codContaContabilFinanc");
		LocalDate dtFinanc = registro.getDate("dtFinanc");
		
		List<TableMap> lctContabeis = buscarLancamentosContabeis(idCentral);


		for(lancamento in lctContabeis ){
			if(lancamento.getString("contaDebito") == codigoContaContabilFinanc || lancamento.getString("contaCredito") == codigoContaContabilFinanc){
				registro.put("status", "Correto")
				registro.putAll(lancamento);				
			}
			
		}

		if(registro.getString("status") != "Correto"){
			if(lctContabeis.size() == 0){
				registro.put("status", "Sem Lançamento Contábil");
			}else{
				registro.put("status", "Verificar contas contábeis");
			}
			
		}

		if(visualizacao == 0){
                if(registro.getString("status") == "Correto" ){
                    tmGeral.add(registro)
                }
            }else if(visualizacao == 1){
                if(registro.getString("status") == "Verificar contas contábeis ou verificar as datas do lançamento" || registro.getString("status") == "Sem Lançamento Contábil" ){
                    tmGeral.add(registro)
                }
            }else{
                tmGeral.add(registro)
            }
            
	}

        return  tmGeral;
    }

    private List<TableMap> buscarLancamentosContabeis(Long idCentral){
    	String sql = "select ABC10DEB.ABC10CODIGO AS contaDebito, "+
				"ABC10CRED.ABC10CODIGO AS contaCredito , "+
				"CASE "+
				"WHEN EBB05ID IS NULL THEN 'Não Localizado' "+
				"ELSE 'Localizado' "+
				"END AS idContabil, "+
				"EBB05DATA AS dtContabil, "+
				"EBB05VALOR AS valorContabil "+
				"from ebb05 "+
				"LEFT JOIN ABC10 AS ABC10DEB ON EBB05DEB = ABC10DEB.ABC10ID "+
				"LEFT JOIN ABC10 AS ABC10CRED ON EBB05CRED = ABC10CRED.ABC10ID "+
				"where ebb05central = :idCentral "


		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idCentral", idCentral))
    	
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEluY29uc2lzdMOqbmNpYXMgQ29udMOhYmVpcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==