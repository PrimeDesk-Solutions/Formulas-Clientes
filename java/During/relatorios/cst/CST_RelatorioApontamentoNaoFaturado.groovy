package During.relatorios.cst;
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import java.time.LocalDate;
import java.sql.*;
import java.time.LocalTime;
import java.util.Map;
import java.util.HashMap;

public class CST_RelatorioApontamentoNaoFaturado extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "Relatório de Apontamento Não Faturado"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		return filtrosDefault;
	}
	@Override 
	public DadosParaDownload executar() {
	//Declaração de Variáveis 	
	Integer FTInicial = getInteger("FTInicial");
	Integer FTFinal = getInteger("FTFinal");	
	LocalDate[] data = getIntervaloDatas("data");
	
	List<TableMap> dados = buscarDados(FTInicial, FTFinal)
	List<TableMap> apontamentos = null

	List<TableMap> dadosRelatorio = new ArrayList();

		
		for (dado in dados) {
			
			apontamentos =  buscarApontamentosNaoFaturado(dado.getString("NUMERO"),dado.getString("CODITEM"),data);	
			//interromper(apontamentos.size().toString())
			for(apontamento in apontamentos){
				if(apontamento != null){
					//dado.putAll(apontamento)
					apontamento.put("NUMERO", dado.getLong("NUMERO"))
					dadosRelatorio.add(apontamento);
				}
			}
			//interromper(teste.toString())
		}

		//interromper(dados.toString())
		
		return gerarPDF("CST_RelatorioApontamentoNaoFaturado",dadosRelatorio)
	}


	private List<TableMap> buscarDados(Integer FTInicial, Integer FTFinal) {
	
		String whereFT = FTInicial != null &&  FTFinal != null ?  " ABB01NUM between ${FTInicial} and ${FTFinal} " : FTInicial != null &&  FTFinal == null ? " ABB01NUM >= ${FTInicial} " : FTInicial == null &&  FTFinal != null ? " ABB01NUM <= ${FTFinal} " : " 1==1 ";
		
		String sql = "SELECT DISTINCT "+
				"	CASE WHEN EAA01032QTCOML IS NULL THEN 0 "+
				"	WHEN (EAA0103QTCOML - COALESCE(EAA01032QTCOML,0)) > 0 THEN 1 ELSE 2 END AS ITEMATENDIDO, "+
				"	EAA0103QTCOML , EAA01032QTCOML, (EAA0103QTCOML - COALESCE(EAA01032QTCOML,0)) AS QTDSEMATEND, "+
				"	ABM01CODIGO AS CODITEM, ABB01NUM AS NUMERO "+
				"FROM EAA01 "+
				"	INNER JOIN ABB01 ON EAA01CENTRAL = ABB01ID "+
				"	INNER JOIN EAA0103 ON EAA0103DOC = EAA01ID "+
				"	LEFT JOIN EAA01032 ON EAA01032ITEMSCV = EAA0103ID "+
				"	INNER JOIN ABM01 ON EAA0103ITEM = ABM01ID "+
				"	INNER JOIN AAH01 ON ABB01TIPO = AAH01ID "+
				"WHERE "+
					whereFT +
				"	AND AAH01CODIGO = '80' "+
				"	AND (EAA0103QTCOML - COALESCE(EAA01032QTCOML,0)) > 0 "+
				"ORDER BY ABB01NUM";
		//interromper(sql.toString());
		return getAcessoAoBanco().buscarListaDeTableMap(sql);
	
	}

	private List<TableMap> buscarApontamentosNaoFaturado(String numero, String codItem, LocalDate[] data) {

	LocalDate dtIni = null;
	LocalDate dtFin = null;
	if(data != null) {
	  dtIni = data[0];
	  dtFin = data[1];
	}
	def formathour = 'HH24:MI';
	
		String whereFT = FTInicial != null &&  FTFinal != null ?  " ABB01NUM between ${FTInicial} and ${FTFinal} " : FTInicial != null &&  FTFinal == null ? " ABB01NUM >= ${FTInicial} " : FTInicial == null &&  FTFinal != null ? " ABB01NUM <= ${FTFinal} " : " 1==1 ";
        	String whereData = dtIni != null && dtFin != null ?  "and BAB0102DTI between '${dtIni}' and '${dtFin}' " : "";	
        		//interromper("Apontamento     "+ formathour)
		String sqlapont = "SELECT ABB01NUM, ABM01CODIGO, AAB10USER AS USUARIO, "+
					"	ABP01CODIGO, ABP01DESCR,  "+
					"	CAST(BAB0102JSON ->> 'ft' AS TEXT) AS FT,  "+
					"	CAST(BAB0102HRI AS TEXT), CAST(BAB0102HRF AS TEXT), BAB0102DTI, BAB0102DTF,  "+
					"	ABP01CUSTO,  "+
					//"	CONCAT(BAB0102DTI, ' ', (BAB0102HRI)) AS INICIO,  "+
					//"    CONCAT(BAB0102DTF, ' ', (BAB0102HRF)) AS FIM,  "+
					//"	CAST(BAB0102DTF || ' ' || (BAB0102HRF) AS TIMESTAMP),  "+
					//"    (CAST(BAB0102DTF || ' ' || (BAB0102HRF) AS TIMESTAMP) -  CAST(BAB0102DTI || ' ' || (BAB0102HRI) AS TIMESTAMP)) AS TEMPO,  "+
					//" 0 AS TEMPO,  "+
					"    EXTRACT(EPOCH FROM (CAST(BAB0102DTF || ' ' || (BAB0102HRF) AS TIMESTAMP) -  CAST(BAB0102DTI || ' ' || (BAB0102HRI) AS TIMESTAMP)))/60 AS MINUTOS  "+
				  " FROM BAB01  "+
					"	INNER JOIN ABB01 ON BAB01CENTRAL = ABB01ID  "+
					"	INNER JOIN BAB0102 ON BAB0102OP = BAB01ID  "+
					"	LEFT JOIN BAB01022 ON BAB01022ATIV = BAB0102ID  "+
					"	INNER JOIN ABP01 ON BAB0102ATIV = ABP01ID  "+
					"	INNER JOIN AAB10 ON BAB01022USER = AAB10ID  "+
					"	inner join abp20 on abp20id = bab01comp   "+
					"	INNER JOIN ABM01 ON abp20ITEM = ABM01ID   "+
				 " WHERE CAST(BAB0102JSON ->> 'ft' AS TEXT) = '"+numero+"' "+ 
					whereData +
					" AND ABM01CODIGO = '" + codItem + "' "+
				 " GROUP BY  CAST(BAB0102JSON ->> 'ft' AS TEXT), ABB01NUM, ABM01CODIGO,AAB10USER, ABP01CODIGO, ABP01DESCR, ABP01CUSTO, BAB0102HRI, BAB0102HRF, BAB0102DTI, BAB0102DTF "+  
				 " ORDER BY  CAST(BAB0102JSON ->> 'ft' AS TEXT) ";
				 //"order by ABP01CUSTO "
				
		Parametro p1 = numero != null && numero.size() > 0 ? Parametro.criar("numero", numero) : null
		Parametro p2 = codItem != null && codItem.size() > 0 ? Parametro.criar("codItem", codItem) : null
		Parametro p3 = data != null ? Parametro.criar("dtIni", data[0]) : null
		Parametro p4 = data != null ? Parametro.criar("dtFin", data[1]) : null
		//interromper("Apontamento "+sqlapont.toString())
		
		return getAcessoAoBanco().buscarListaDeTableMap(sqlapont);
	
	}	
}
//meta-sis-eyJkZXNjciI6IlJlbGF0w7NyaW8gZGUgQXBvbnRhbWVudG8gTsOjbyBGYXR1cmFkbyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==