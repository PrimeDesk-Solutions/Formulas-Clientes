package multitec.formulas.sgt.ecf;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.TextFileEscrita;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aag0201
import sam.model.entities.ab.Abb01;
import sam.model.entities.ab.Abc10;
import sam.model.entities.eb.Ebb02;
import sam.model.entities.eb.Ebb03;
import sam.model.entities.eb.Ebb05;
import sam.model.entities.ed.Eda01;
import sam.model.entities.ed.Eda10;
import sam.model.entities.ed.Eda1001;
import sam.model.entities.ed.Eda1002;
import sam.model.entities.ed.Eda10021;
import sam.model.entities.ed.Eda12;
import sam.model.entities.ed.Eda13;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro;

public class Leiaute7 extends FormulaBase{
		  
	@Override
	public void executar() {
		
		//Iniciar objetos a serem utilizados
		LocalDate dtInicial = get("dtInicial");
		LocalDate dtFinal = get("dtFinal");
		String nroRecibo = getString("nroRecibo");
		Boolean isRetificadora = get("isRetificadora");
		Boolean gerarBlocoJ = get("gerarBlocoJ");
		Boolean gerarBlocoL = get("gerarBlocoL");
		Boolean gerarBlocoM = get("gerarBlocoM");
		Boolean gerarBlocoQ = get("gerarBlocoQ");
		Aac10 aac10 = getVariaveis().getAac10();
		    
		if(aac10 == null) throw new ValidacaoException("Necessário informar a empresa ativa.");
		  
	    if(aac10.getAac10municipio() == null) throw new ValidacaoException("Necessário infomar o município no cadastro da empresa ativa.");
	    
	    
		//IE da empresa ativa 
		Long idEmp = aac10.getAac10id();
		
		//UF da empresa ativa
		String uf = buscarUfEmpresa(idEmp);
			    
	    //Format que serão utilizadas
	    DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("ddMMyyyy");
		
		//Período
		if(dtInicial == null) throw new ValidacaoException("Necessário informar a data inicial do período.");
		if(dtFinal == null) throw new ValidacaoException("Necessário informar a data final do período.");
		if(dtFinal.compareTo(dtInicial) <= 0) throw new ValidacaoException("O período final é menor que o período inicial.");
		
		
	    LocalDate dataECF = dtInicial;
	    int mesInicial = dataECF.getMonthValue();
	    int anoECF = dataECF.getYear();
			
	    dataECF = dtFinal;
	    int mesFinal = dataECF.getMonthValue();
	    
	    
	    //Início da geração do arquivo 
	    
	    /**
		 * Guarda o mês/ano que tem encerramento
		 */
		Set<String> mesEncerramento = new HashSet<String>(); 
       
		String sqlEbb03 = " SELECT ebb03ano, ebb03mes, SUM(ebb03saldo) as ebb03saldo " +
						  " FROM Ebb03 INNER JOIN Abc10 ON abc10id = ebb03cta " +
						  " WHERE abc10class = 2 AND abc10reduzido > 0 AND " +
						  Fields.numMeses("ebb03mes", "ebb03ano") + " BETWEEN :numMesesIni AND :numMesesFin " +
						  getSamWhere().getWherePadrao("AND", Ebb03.class) + 
						  " GROUP BY ebb03ano, ebb03mes " +
						  " ORDER BY ebb03ano, ebb03mes ";
		List<TableMap> listEbb03s = getAcessoAoBanco().buscarListaDeTableMap(sqlEbb03, Parametro.criar("numMesesIni", Criterions.valNumMeses(mesInicial, anoECF)), Parametro.criar("numMesesFin", Criterions.valNumMeses(mesFinal, anoECF)));
		for(TableMap ebb03 : listEbb03s) {
			dataECF = LocalDate.of(ebb03.getInteger("ebb03ano"), ebb03.getInteger("ebb03mes"), 1);
			dataECF = dataECF.with(TemporalAdjusters.lastDayOfMonth());
            
			mesEncerramento.add(DateTimeFormatter.ofPattern("MM/yyyy").format(dataECF));
		}
	    
		/**
	     *Mapa com os parâmetros gerais da ECF
	     */
	    String sqlEda01 = "SELECT CONCAT(eda01registro, '-', eda0101campo) as param, eda0101conteudo FROM Eda01 " +
	    		   	      "INNER JOIN Eda0101 ON eda01id = eda0101param " +
	    		   	      getSamWhere().getWherePadrao("AND", Eda01.class) + 
	    		   	      " ORDER BY eda01registro ";
	    Map<String, String> mapParam = getAcessoAoBanco().obterMapDeRegistros(sqlEda01, "param", "eda0101conteudo");
	    
	    TextFile txt = new TextFile("|");
	    
	    
	    /*************************************************************************************************************
		 ***************************************************BLOCO 0***************************************************
		 *************************************************************************************************************
		 */
		Integer qtLinBloco0 = 0;	
    
		/**
		 * REGISTRO 0000: ABERTURA DO ARQUIVO DIGITAL E IDENTIFICAÇÃO DA PESSOA JURÍDICA
		 */
		txt.print("0000");
		txt.print("LECF");
		txt.print("0007");
		txt.print(StringUtils.extractNumbers(aac10.getAac10ni()));
		txt.print(aac10.getAac10rs());
		txt.print(mapParam.get("0000-IND_SIT_INI_PER"));
		txt.print(mapParam.get("0000-SIT_ESPECIAL"));
		txt.print(mapParam.get("0000-PAT_REMAN_CIS"));	
		txt.print(mapParam.get("0000-DT_SIT_ESP"));
		txt.print(dtfData.format(dtInicial));
		txt.print(dtfData.format(dtFinal));
		txt.print(isRetificadora ? "S" : "N");
		txt.print(nroRecibo);
		txt.print(mapParam.get("0000-TIP_ECF"));
		txt.print(null);
		txt.newLine();
		qtLinBloco0++;
		
		/**
		 * REGISTRO 0001: ABERTURA DO BLOCO 0
		 */
		txt.print("0001");
		txt.print("0");
		txt.newLine();
		qtLinBloco0++;
    
		/**
		 * REGISTRO 0010: PARÂMETROS DE TRIBUTAÇÃO
		 */
		txt.print("0010");
		txt.print(null);
		txt.print(mapParam.get("0010-OPT_REFIS"));
		txt.print(mapParam.get("0010-OPT_PAES"));
		txt.print(mapParam.get("0010-FORMA_TRIB"));
				
		String formaApur = mapParam.get("0010-FORMA_APUR");
		if(formaApur == null)formaApur = "";
		txt.print(formaApur);	
				
		txt.print(mapParam.get("0010-COD_QUALIF_PJ"));
		txt.print(mapParam.get("0010-FORMA_TRIB_PER"));
		
		String mesBalRed = mapParam.get("0010-MES_BAL_RED"); 
		txt.print(mesBalRed);
				
		txt.print(mapParam.get("0010-TIP_ESC_PRE"));
		txt.print(mapParam.get("0010-TIP_ENT"));
		txt.print(mapParam.get("0010-FORMA_APUR_I"));
		txt.print(mapParam.get("0010-APUR_CSLL"));
		txt.print(mapParam.get("0010-IND_REC_RECEITA"));
		txt.newLine();
		qtLinBloco0++;  
		
		/**
		 * REGISTRO 0020: PARÂMETROS COMPLEMENTARES
		 */
		txt.print("0020");
		txt.print(mapParam.get("0020-IND_ALIQ_CSLL"));
		txt.print(mapParam.get("0020-IND_QTE_SCP"));
		txt.print(mapParam.get("0020-IND_ADM_FUN_CLU"));
		txt.print(mapParam.get("0020-IND_PART_CONS"));
		txt.print(mapParam.get("0020-IND_OP_EXT"));
		txt.print(mapParam.get("0020-IND_OP_VINC"));
		txt.print(mapParam.get("0020-IND_PJ_ENQUAD"));
		txt.print(mapParam.get("0020-IND_PART_EXT"));
		txt.print(mapParam.get("0020-IND_ATIV_RURAL"));
		txt.print(mapParam.get("0020-IND_LUC_EXP"));
		txt.print(mapParam.get("0020-IND_RED_ISEN"));
		txt.print(mapParam.get("0020-IND_FIN"));
		txt.print(mapParam.get("0020-IND_DOA_ELEIT"));
		txt.print(mapParam.get("0020-IND_PART_COLIG"));
		txt.print(mapParam.get("0020-IND_VEND_EXP"));
		txt.print(mapParam.get("0020-IND_REC_EXT"));
		txt.print(mapParam.get("0020-IND_ATIV_EXT"));
		txt.print(mapParam.get("0020-IND_COM_EXP"));
		txt.print(mapParam.get("0020-IND_PGTO_EXT"));
		txt.print(mapParam.get("0020-IND_E-COM_TI"));
		txt.print(mapParam.get("0020-IND_ROY_REC"));
		txt.print(mapParam.get("0020-IND_ROY_PAG"));
		txt.print(mapParam.get("0020-IND_REND_SERV"));
		txt.print(mapParam.get("0020-IND_PGTO_REM"));
		txt.print(mapParam.get("0020-IND_INOV_TEC"));
		txt.print(mapParam.get("0020-IND_CAP_INF"));
		txt.print(mapParam.get("0020-IND_PJ_HAB"));
		txt.print(mapParam.get("0020-IND_POLO_AM"));
		txt.print(mapParam.get("0020-IND_ZON_EXP"));
		txt.print(mapParam.get("0020-IND_AREA_COM"));
		txt.print(mapParam.get("0020-IND_PAIS_A_PAIS"));
		txt.print(mapParam.get("0020-IND_DEREX"));
		txt.newLine();
		qtLinBloco0++;
		
		/**
		 * REGISTRO 0030: DADOS CADASTRAIS
		 */
		txt.print("0030");
		txt.print(mapParam.get("0030-COD_NAT"));
		txt.print(aac10.getAac10cnae());
		txt.print(aac10.getAac10endereco());
		txt.print(aac10.getAac10numero());
		txt.print(aac10.getAac10complem());
		txt.print(aac10.getAac10bairro());
		txt.print(uf);
		
		
		Aag0201 codMunicipio = getSession().createCriteria(Aag0201.class).addWhere(Criterions.eq("aag0201id", aac10.getAac10municipio().getAag0201id())).get();
		
		//aac10.getAac10municipio() != null ? aac10.getAac10municipio().getAag0201ibge() : "";
		txt.print(codMunicipio.getAag0201ibge());
				
		txt.print(aac10.getAac10cep());
				
		String fone = aac10.getAac10dddFone() != null && aac10.getAac10fone() != null ? StringUtils.extractNumbers(aac10.getAac10dddFone() + aac10.getAac10fone()) : "";
		txt.print(fone);
				
		txt.print(aac10.getAac10email());
		txt.newLine();
		qtLinBloco0++;
		
		
		/**
		 * REGISTRO 0930: IDENTIFICAÇÃO DOS SIGNATÁRIO DA ECF
		 */			
		// DADOS DO CONTADOR
		txt.print("0930");
		txt.print(aac10.getAac10cNome());
		txt.print(aac10.getAac10cCpf() != null ? StringUtils.extractNumbers(aac10.getAac10cCpf()) : null);
		txt.print(aac10.getAac10cQualifCod());
		txt.print(aac10.getAac10cCrc() != null ? StringUtils.extractNumbers(aac10.getAac10cCrc()) : null);
		txt.print(aac10.getAac10cEmail());
		
		String foneContador = aac10.getAac10cDddFone() != null && aac10.getAac10cFone() != null ? StringUtils.extractNumbers(aac10.getAac10cDddFone() + aac10.getAac10cFone()) : "";
		txt.print(foneContador);
				
		txt.newLine();
		qtLinBloco0++;
		
		
		//DADOS DO REPRESENTANTE LEGAL DA EMPRESA
		txt.print("0930");
		txt.print(aac10.getAac10rNome());
		txt.print(aac10.getAac10rCpf() != null ? StringUtils.extractNumbers(aac10.getAac10rCpf()) : null);
		txt.print(aac10.getAac10rQualifCod());
		txt.print(null);
		txt.print(aac10.getAac10rEmail());
						
		String foneRepresentante = aac10.getAac10rDddFone() != null && aac10.getAac10rFone() != null ? StringUtils.extractNumbers(aac10.getAac10rDddFone() + aac10.getAac10rFone()) : "";
		txt.print(foneRepresentante);
				
		txt.newLine();
		qtLinBloco0++;
						
		/** 
		 * REGISTRO 0990: ENCERRAMENTO DO BLOCO 0
		 */			
		txt.print("0990");
		txt.print(qtLinBloco0 + 1);
		txt.newLine();
		qtLinBloco0++;
		
		
		/**
		 * BLOCO C: INFORMAÇÕES RECUPERADAS DA ECD 
		 * Não implementado. Será gerado automaticamente pelo PVA
		 */
				
		/**
		 * BLOCO E: INFORMAÇÕES RECUPERADAS DAS ECF ANTERIOR E CÁLCULO FISCAL DOS DADOS RECUPERADOS DA ECD
		 * Não implementado. Será gerado automaticamente pelo PVA
		 */
    
    
		/*************************************************************************************************************
		 ***************************************************BLOCO J***************************************************
		 **************************************************************************************************************/
		Integer qtLinBlocoJ = 0;
		Integer qtLinJ050 = 0;
		Integer qtLinJ051 = 0;
		Integer qtLinJ053 = 0;
		
		
		/**
		 * REGISTRO J001: ABERTURA DO BLOCO J
		 */
		txt.print("J001");
		txt.print(gerarBlocoJ ? "0" : "1");
		txt.newLine();
		qtLinBlocoJ++;
    
		if(gerarBlocoJ){
			/**
			 * REGISTRO J050: PLANO DE CONTAS DO CONTRIBUINTE
			 */
			String sqlAbc10 = " SELECT princ.abc10id AS abc10id, princ.abc10ecdNat AS abc10ecdNat, princ.abc10codigo AS abc10codigo, princ.abc10nome AS abc10nome, princ.abc10reduzido AS abc10reduzido, princ.abc10sup AS abc10sup, sup.abc10codigo AS supCodigo, " +
							  " princ.abc10ctaRef AS abc10ctaRef, aaj20.aaj20codigo as aaj20codigo " +
							  " FROM Abc10 as princ " +
							  " LEFT JOIN Abc10 AS sup ON sup.abc10id = princ.abc10sup " +
							  " LEFT JOIN Aaj20 AS aaj20 ON aaj20.aaj20id = princ.abc10ctaRef " +
							  getSamWhere().getWherePadrao("WHERE", Abc10.class, "princ") +
							  " ORDER BY princ.abc10codigo";
			List<TableMap> listAbc10s = getAcessoAoBanco().buscarListaDeTableMap(sqlAbc10);
			String nome = null;
			for(TableMap abc10 : listAbc10s) {
				txt.print("J050");
				txt.print(dtfData.format(dtInicial));

				if(abc10.getInteger("abc10ecdNat") == null) throw new ValidacaoException("Necessário informar a natureza da ECD.");
				switch (abc10.getInteger("abc10ecdNat")) {
					case 1:  txt.print("01", 2); break;
					case 2:  txt.print("02", 2); break;
					case 3:  txt.print("03", 2); break;
					case 4:  txt.print("04", 2); break;
					case 5:  txt.print("04", 2); break;
					case 6:  txt.print("05", 2); break;
					case 9:  txt.print("09", 2); break;
					default: txt.print(null); break;
				}

				txt.print(abc10.getInteger("abc10reduzido") == 0 ? "S" : "A");

				switch (abc10.getString("abc10codigo").length()) {
					case 1:  txt.print("1", 1);  break;
					case 2:  txt.print("2", 1);  break;
					case 3:  txt.print("3", 1);  break;
					case 5:  txt.print("4", 1);  break;
					case 7:  txt.print("5", 1);  break;
					case 11: txt.print("6", 1);  break;
					default: txt.print(null); break;
				}

				txt.print(abc10.getString("abc10codigo"));
				txt.print(abc10.getString("supCodigo") == null ? null : abc10.getString("supCodigo"));

				if(abc10.getString("abc10nome") != null) nome = abc10.getString("abc10nome");
				txt.print(nome);
				txt.newLine();
						
				qtLinJ050++;
				qtLinBlocoJ++;
				
        
				/**
				 * REGISTRO J051: PLANO DE CONTAS REFERENCIAL
				 */
				if(abc10.getInteger("abc10reduzido") > 0) {
					if(abc10.getLong("abc10ctaRef") != null) {
						txt.print("J051");
						txt.print(null);
						txt.print(abc10.getString("aaj20codigo"));
						txt.newLine();
							
						qtLinJ051++;
						qtLinBlocoJ++;
					}
							
					/**
					 * REGISTRO J053: SUBCONTAS CORRELATAS
					 */
					String sqlAbc10Sup = " SELECT abc10id, abc10reduzido, abc10codigo, abc10ecdNatSub, aaj21codigo FROM Abc10 "+
										 " LEFT JOIN Aaj21 ON aaj21id = abc10ecdNatSub " +
										 " WHERE abc10ecdCtaPai = :abc10id" +
										 getSamWhere().getWherePadrao("AND", Abc10.class) + 
										 " ORDER BY abc10codigo";
					List<Abc10> abc10sSub = getAcessoAoBanco().buscarListaDeRegistros(sqlAbc10Sup, Parametro.criar("abc10id", abc10.getLong("abc10id")));			
					if(abc10sSub != null && abc10sSub.size() > 0) {
						for(Abc10 abc10sub : abc10sSub){
							txt.print("J053");
							txt.print(abc10sub.getAbc10reduzido());
							txt.print(abc10sub.getAbc10codigo());
							txt.print(abc10sub.getAbc10ecdNatSub() != null ? abc10sub.getAbc10ecdNatSub().getAaj21codigo() : null);
							txt.newLine(); 
									
							qtLinJ053++;
							qtLinBlocoJ++;
						}
					}
				}
			}
			listAbc10s = null;
		}
		
		/**
		 * REGISTRO J990: ENCERRAMENTO DO BLOCO J 
		 */			
		txt.print("J990");
		txt.print(qtLinBlocoJ + 1);
		txt.newLine();
		qtLinBlocoJ++;
				
		/** 
		 * BLOCO K: SALDOS DAS CONTAS CONTÁBEIS E REFERENCIAIS
		 * Não implementado
		 */
		
		/*************************************************************************************************************
		 ***************************************************BLOCO L***************************************************
		 *************************************************************************************************************/
		Integer qtLinBlocoL = 0;
		Integer qtLinL030 = 0;
		Integer qtLinL200 = 0;
		Integer qtLinL210 = 0;
			
		String indAvalEstoq = mapParam.get("L200-IND_AVAL_ESTOQ");
				
		/**
		 * REGISTRO L001: ABERTURA DO BLOCO L
		 */
		txt.print("L001");
		txt.print(!gerarBlocoL || indAvalEstoq == null ? "1" : "0");
		txt.newLine();
		qtLinBlocoL++;
		
		
		if(gerarBlocoL){
			/**
			 * REGISTRO L030: IDENTIFICAÇÃO DO PERÍODO E FORMAS DE APUR DO IRPJ E DA CSLL DO ANO-CALENDÁRIO
			 */
      
			Map<Integer, TableMap> mapL030 = new HashMap<Integer, TableMap>();
			int linha = 0;
			
			if(formaApur.equalsIgnoreCase("A")) {
				TableMap l030a = new TableMap();      
		        
		        l030a.put("dtInicial", dtInicial);
		        l030a.put("dtFinal", dtFinal);
		        l030a.put("Valor", "A00");
		            
		        mapL030.put(linha, l030a);
		        linha++;
		        
		        if(mesBalRed != null){
		        	for(int i = 0; i < mesBalRed.length(); i++){
		        		String letra = mesBalRed.substring(i, i+1);
		        		if(letra.equals("B")){
		        			LocalDate dtFin = LocalDate.of(dtInicial.getYear(), i+1, 1);
		        			dtFin = dtFin.with(TemporalAdjusters.lastDayOfMonth());
									
		        			TableMap l030b = new TableMap();
		              
		        			l030b.put("dtInicial", dtInicial);
		        			l030b.put("dtFinal", dtFin);
		        			l030b.put("valor", getCodigoPerApurAnual(i+1));
											
		        			mapL030.put(linha, l030b);
		        			linha++;
		        		}	
		        	}
		        }
			}else if(formaApur.equalsIgnoreCase("T")){
				LocalDate dtPer = dtInicial;
				
				while(DateUtils.dateDiff(dtPer, dtFinal, ChronoUnit.MONTHS) > 0) {
                    TableMap l030 = new TableMap();

                    l030.put("dtInicial", dtPer);                           						
					
                    setUltimoDiaDoTrimestre(dtPer);

                    l030.put("valor", getCodigoPerApurTrimestral(dtPer.getMonthValue()));

					
					LocalDate dtPerFinal = DateUtils.dateDiff(dtPer, dtFinal, ChronoUnit.DAYS) >= 0 ? dtPer : dtFinal;
					l030.put("dtFinal", dtPerFinal);

                    mapL030.put(linha, l030);
					
					linha++;
					
					dtPer = dtPer.plusMonths(1);
				}
	        }
			
			for(int i = 0; i < mapL030.size(); i++) {
				txt.print("L030");
				txt.print(dtfData.format(mapL030.get(i).get("dtInicial")));
				txt.print(dtfData.format(mapL030.get(i).get("dtFinal")));
				txt.print(mapL030.get(i).get("valor"));
				txt.newLine();
				
				qtLinL030++;
				qtLinBlocoL++;
				
				/**
				 * REGISTRO L200: MÉTODO DE AVALIAÇÃO DO ESTOQUE FINAL
				 */
				txt.print("L200");
				txt.print(indAvalEstoq);
				txt.newLine();
				qtLinL200++;
				qtLinBlocoL++;	
				
			}
			
		}
		
		
		/**
		 * REGISTRO L990: ENCERRAMENTO DO BLOCO L
		 */				
		txt.print("L990");
		txt.print(qtLinBlocoL + 1);
		txt.newLine();
		qtLinBlocoL++;
		
		/*************************************************************************************************************
		 ***************************************************BLOCO M***************************************************
		 *************************************************************************************************************
		 */
		Integer qtLinBlocoM = 0;
		Integer qtLinM010 = 0;
		Integer qtLinM030 = 0;
		Integer qtLinM300 = 0;
		Integer qtLinM305 = 0;
		Integer qtLinM310 = 0;
		Integer qtLinM312 = 0;
		Integer qtLinM350 = 0;
		Integer qtLinM355 = 0;
		Integer qtLinM360 = 0;
		Integer qtLinM362 = 0;
		Integer qtLinM410 = 0;
		
		
		/**
		 * REGISTRO M001: ABERTURA DO BLOCO M
		 */
		txt.print("M001");
		txt.print(gerarBlocoM && verificarMovimentoBlocoM(anoECF, dtInicial, dtFinal) ? "0" : "1");
		txt.newLine();
		qtLinBlocoM++;
		
		
		if(gerarBlocoM){
			/**
			 * REGISTRO M010: IDENTIFICAÇÃO DA CONTA NA PARTE B DO eLALUR E eLACS
			 */
            String sqlEda12 = "SELECT eda12id, eda12conta, aaj24codigo, aaj24descr, eda12dtf, eda12codLct, aaj23codigo, aaj23descr, eda12dtl, eda12indTrib, eda12saldo, eda12dc, eda12cnpj " +
				              "FROM Eda12 " +
				              "LEFT JOIN Aaj24 ON aaj24id = eda12conta " +
				              "LEFT JOIN Aaj23 ON aaj23id = eda12codLct " +
				              "WHERE eda12ano = :ano " + getSamWhere().getWherePadrao("AND", Eda12.class);
			
            List<TableMap> eda12s = getAcessoAoBanco().buscarListaDeTableMap(sqlEda12, Parametro.criar("ano", anoECF));
			for(TableMap eda12 : eda12s) {
				txt.print("M010");
				txt.print(eda12.getLong("eda12conta") != null ? eda12.getString("aaj24codigo") : null);
				txt.print(eda12.getLong("eda12conta") != null ? eda12.getString("aaj24descr") : null);
				txt.print(eda12.getDate("eda12dtf") != null ? dtfData.format(eda12.getDate("eda12dtf")) : null);
				txt.print(eda12.getLong("eda12codLct") != null ? eda12.getString("aaj23codigo") : "");
				txt.print(eda12.getLong("eda12codLct") != null ? eda12.getString("aaj23descr") : "");
				txt.print(eda12.getDate("eda12dtl") != null ? dtfData.format(eda12.getDate("eda12dtl")) : "");
				txt.print(eda12.getInteger("eda12indTrib") == 0 ? "I" : "C");
				txt.print(formatarValor(eda12.getBigDecimal("eda12saldo"), 2));
				txt.print(eda12.getInteger("eda12dc") == 0 ? 'D' : 'C');
				txt.print(eda12.getString("eda12cnpj") == null ? null : StringUtils.extractNumbers(eda12.getString("eda12cnpj")));
				txt.newLine();
				
				qtLinM010++;
				qtLinBlocoM++;
			}
			
			/**
			 * REGISTRO M030: IDENTIFICAÇÃO DOS PERÍODOS E FORMAS APURAÇÃO IRPJ E CSLL TRIB. LUCRO REAL
			 */
			Map<Integer, TableMap> mapM030 = new HashMap<>(); //("valor, dtInicial, dtFinal");
			LocalDate dtIniM410 = null;
			
			int linha = 0;
			if(formaApur.equalsIgnoreCase("A")) {
                TableMap m030a = new TableMap();

                m030a.put("dtInicial", dtInicial);
                m030a.put("dtFinal", dtFinal);
                m030a.put("valor", "A00");

				mapM030.put(linha, m030a);
				linha++;
				
				if(mesBalRed != null){
					for(int i = 0; i < mesBalRed.length(); i++){
						String letra = mesBalRed.substring(i, i+1);
						if(letra.equals("B")){
                            LocalDate dtFin = LocalDate.of(dtInicial.getYear(), i+1, 1);
							dtFin = dtFin.with(TemporalAdjusters.lastDayOfMonth());
							
							TableMap m030b = new TableMap();

                            m030b.put("dtInicial", dtInicial);
                            m030b.put("dtFinal", dtFin);
                            m030b.put("valor", getCodigoPerApurAnual(i+1));
                            
                            mapM030.put(linha, m030b);
							linha++;
						}	
					}
				}
				
			}else if(formaApur.equalsIgnoreCase("T")){
                LocalDate dtPer = dtInicial;
				while(DateUtils.dateDiff(dtPer, dtFinal, ChronoUnit.MONTHS) > 0) {
					TableMap m030 = new TableMap();

                    m030.put("dtInicial", dtPer);
					
					setUltimoDiaDoTrimestre(dtPer);
					m030.put("valor", getCodigoPerApurTrimestral(dtPer.getMonthValue()));
					
					LocalDate dtPerFinal = DateUtils.dateDiff(dtPer, dtFinal, ChronoUnit.DAYS) >= 0 ? dtPer : dtFinal;
					m030.put("dtFinal", dtPerFinal);
					
					mapM030.put(linha, m030);
					linha++;
					
					dtPer = dtPer.plusDays(1);
				}
			}
			
			for(int i = 0; i < mapM030.size(); i++) {
				txt.print("M030");
				txt.print(dtfData.format(mapM030.get(i).get("dtInicial")));
				txt.print(dtfData.format(mapM030.get(i).get("dtFinal")));
				txt.print(mapM030.get(i).get("valor"));
				txt.newLine();
				qtLinM030++;
				qtLinBlocoM++;
				
				LocalDate dt = mapM030.get(i).get("dtInicial");
				int mesI = dt.getMonthValue();
				int anoI = dt.getYear();
				
				dt = mapM030.get(i).get("dtFinal");
				int mesF = dt.getMonthValue();
				int anoF = dt.getYear();
				
				if(mapM030.get(i).get("valor").toString().substring(0, 1).equals("A")){
					mesI = mesF;
				}
				
				/**
				 * REGISTRO M300: LANÇAMENTOS DA PARTE A DO E-LALUR
				 */
				String sqlEda10Elalur= "SELECT * FROM Eda10 AS eda10 " +
									   "INNER JOIN FETCH eda10.eda10codLct AS aaj23 "  +
									   "LEFT JOIN FETCH eda10.eda1001s AS eda1001 " +
									   "LEFT JOIN FETCH eda1001.eda1001conta AS aaj24 " +
									   "LEFT JOIN FETCH eda10.eda1002s AS eda1002 " +
									   "LEFT JOIN FETCH eda1002.eda1002cta AS abc10 " +
									   "LEFT JOIN FETCH eda1002.eda10021s as eda10021 " +
									   "LEFT JOIN FETCH eda10021.eda10021lct as ebb05 " +
	                       			   "WHERE (eda10mes >= :mesI AND eda10mes <= :mesF) AND " +
	                       			   "(eda10ano >= :anoI AND eda10ano <= :anoF) AND " +
	                       			   "eda10elalur = 1 " + getSamWhere().getWherePadrao("AND", Eda10.class);
 
				
                List<Eda10> eda10sElalur = getAcessoAoBanco().buscarListaDeRegistros(sqlEda10Elalur, Parametro.criar("mesI", mesI), Parametro.criar("anoI", anoI), Parametro.criar("mesF", mesF), Parametro.criar("anoF", anoF));
                if(eda10sElalur != null && eda10sElalur.size() > 0) {
	                for(Eda10 eda10 : eda10sElalur) {
	                	txt.print("M300");
	                	txt.print(eda10.getEda10codLct().getAaj23codigo());
						txt.print(eda10.getEda10codLct().getAaj23descr());
						txt.print(eda10.getEda10tipo() == 0 ? "A" : eda10.getEda10tipo() == 1 ? "E" : eda10.getEda10tipo() == 2 ? "P" : "L");
						txt.print(eda10.getEda10indRel() == 0 ? "1" : eda10.getEda10indRel() == 1 ? "2" : eda10.getEda10indRel() == 2 ? "3" : "4");
						txt.print(eda10.getEda10valor() != null && eda10.getEda10valor() > 0 ? formatarValor(eda10.getEda10valor(), 2) : "");
						txt.print(eda10.getEda10hist() != null ? eda10.getEda10hist() : "");
						txt.newLine();
						
						qtLinM300++;
						qtLinBlocoM++;
						
						/**
						 * REGISTRO M305: CONTA DA PARTE B DO E-LALUR
						 */
						if(eda10.getEda1001s() != null && eda10.getEda1001s().size() > 0) {
							for(Eda1001 eda1001 : eda10.getEda1001s()) {
								txt.print("M305");
								txt.print(eda1001.getEda1001conta().getAaj24codigo());
								txt.print(eda1001.getEda1001valor() != null && eda1001.getEda1001valor() > 0 ? formatarValor(eda1001.getEda1001valor(), 2) : "");
								txt.print(eda1001.getEda1001indLucro() == 0 ? "D" : "C");
		 						txt.newLine();
		 						
								qtLinM305++;	
								qtLinBlocoM++;
							}
						}
						
						/**
						 * REGISTRO M310: CONTAS CONTÁBEIS RELACIONADAS AO LANÇAMENTO DA PARTE A DO E-LALUR
						 */
						if(eda10.getEda1002s() != null && eda10.getEda1002s().size() > 0) {
							for(Eda1002 eda1002 : eda10.getEda1002s()) {
								Abc10 abc10 = eda1002.getEda1002cta();
												
								BigDecimal totalDeb = new BigDecimal(0);
								BigDecimal totalCred = new BigDecimal(0);
								
								
								if(eda1002.getEda10021s() != null && eda1002.getEda10021s().size() > 0) {
									for(Eda10021 eda10021 : eda1002.getEda10021s()) {
										if(eda10021.getEda10021lct().getEbb05deb().getAbc10id().equals(abc10.getAbc10id())) {
											totalDeb = totalDeb.add(eda10021.getEda10021lct().getEbb05valor());
										}else {
											totalCred = totalCred.add(eda10021.getEda10021lct().getEbb05valor());
										}
									}
								}
								
								BigDecimal total = (totalDeb.subtract(totalCred));
								if(!total.equals(BigDecimal.ZERO)) {
									txt.print("M310");
									txt.print(abc10.getAbc10codigo());
									txt.print(null);
									txt.print(formatarValor(total.abs(), 2));
									txt.print(total < 0 ? "C" : "D");
									txt.newLine();
									
									qtLinM310++;
									qtLinBlocoM++;
									
									/**
									 * REGISTRO M312: NÚMEROS DOS LANÇAMENTOS RELACIONADOS A CONTA CONTÁBIL
									 */
									for(Eda10021 eda10021 : eda1002.getEda10021s()) {
		//								if(eb3021.getEb04().getAb21ByEb04ctadeb().getAb21id().equals(ab21.getAb21id())) {
											txt.print("M312");
											txt.print(eda10021.getEda10021lct().getEbb05num());
											txt.newLine();
											
											qtLinM312++;
											qtLinBlocoM++;
		//								}
									}
								}
							}
						}
	                }
                }
                
                /**
				 * REGISTRO M350: LANÇAMENTOS DA PARTE A DO E-LACS
				 */
                String sqlEda10Elacs = "SELECT * FROM Eda10 AS eda10 " +
									   "INNER JOIN FETCH eda10.eda10codLct AS aaj23 " +
									   "LEFT JOIN FETCH eda10.eda1001s AS eda1001 " +
									   "LEFT JOIN FETCH eda1001.eda1001conta AS aaj24 " +
									   "LEFT JOIN FETCH eda10.eda1002s AS eda1002 " +
									   "LEFT JOIN FETCH eda1002.eda1002cta AS abc10 " +
									   "LEFT JOIN FETCH eda1002.eda10021s as eda10021 " +
									   "LEFT JOIN FETCH eda10021.eda10021lct as ebb05 " +
				                       "WHERE (eda10mes >= :mesI AND eda10mes <= :mesF) AND " +
				                       "(eda10ano >= :anoI AND eda10ano <= :anoF) AND " +
				                       "eda10elacs = 1 " + getSamWhere().getWherePadrao("AND", Eda10.class);
                
                List<Eda10> eda10sElacs = getAcessoAoBanco().buscarListaDeRegistros(sqlEda10Elacs, Parametro.criar("mesI", mesI), Parametro.criar("anoI", anoI), Parametro.criar("mesF", mesF), Parametro.criar("anoF", anoF));
				for(Eda10 eda10 : eda10sElacs) { 
					txt.print("M350");
					txt.print(eda10.getEda10codLct().getAaj23codigo());
					txt.print(eda10.getEda10codLct().getAaj23descr());
					txt.print(eda10.getEda10tipo() == 0 ? "A" : eda10.getEda10tipo() == 1 ? "E" : eda10.getEda10tipo() == 2 ? "P" : "L");
					txt.print(eda10.getEda10indRel() == 0 ? "1" : eda10.getEda10indRel() == 1 ? "2" : eda10.getEda10indRel() == 2 ? "3" : "4");
					txt.print(eda10.getEda10valor() != null && eda10.getEda10valor() > 0 ? formatarValor(eda10.getEda10valor(), 2) : "");
					txt.print(eda10.getEda10hist() != null ? eda10.getEda10hist() : "");
					txt.newLine();
					
					qtLinM350++;
					qtLinBlocoM++;
					
					
					/**
					 * REGISTRO M355: CONTAS DA PARTE B DO E-LACS 
					 */
					for(Eda1001 eda1001 : eda10.getEda1001s()) {
						txt.print("M355");
						txt.print(eda1001.getEda1001conta().getAaj24codigo());
						txt.print(eda1001.getEda1001valor() != null && eda1001.getEda1001valor() > 0 ? formatarValor(eda1001.getEda1001valor(), 2) : "");
						txt.print(eda1001.getEda1001indLucro() == 0 ? "D" : "C");
						txt.newLine();
						
						qtLinM355++;
						qtLinBlocoM++;
					}
					
					/**
					 * REGISTRO M360: CONTAS CONTÁBEIS RELACIONADAS AO LANAÇMENTO DA PARTE A DO E-LACS 
					 */
					for(Eda1002 eda1002 : eda10.getEda1002s()) {
						Abc10 abc10 = eda1002.getEda1002cta();
						
						BigDecimal totalCred = new BigDecimal(0);
						BigDecimal totalDeb = new BigDecimal(0);
						for(Eda10021 eda10021 : eda1002.getEda10021s()) {
							if(eda10021.getEda10021lct().getEbb05cred().getAbc10id().equals(abc10.getAbc10id())) {
								totalCred = totalCred.add(eda10021.getEda10021lct().getEbb05valor());
							} else {
								totalDeb = totalDeb.add(eda10021.getEda10021lct().getEbb05valor());
							}
						}
						
						

						if(!totalCred.equals(BigDecimal.ZERO)) {
							txt.print("M360");
							txt.print(abc10.getAbc10codigo());
							txt.print(null);
							txt.print(formatarValor(totalCred, 2));
							txt.print("C");
							txt.newLine();
							
							qtLinM360++;
							qtLinBlocoM++;
							
							/**
							 * REGISTRO M362: NÚMEROS DOS LANÇAMENTOS RELACIONADOS A CONTA CONTÁBIL
							 */
							for(Eda10021 eda10021 : eda1002.getEda10021s()) {
								txt.print("M362");
								txt.print(eda10021.getEda10021lct().getEbb05num());
								txt.newLine();
								
								qtLinM362++;
								qtLinBlocoM++;
							}
						}
						
						if(!totalDeb.equals(BigDecimal.ZERO)) {
							txt.print("M360");
							txt.print(abc10.getAbc10codigo());
							txt.print(null);
							txt.print(formatarValor(totalDeb, 2));
							txt.print("D");
							txt.newLine();
							
							qtLinM360++;
							qtLinBlocoM++;
							
							/**
							 * REIGISTRO M362: NÚMEROS DOS LANÇAMENTOS RELACIONADOS A CONTA CONTÁBIL
							 */
							for(Eda10021 eda10021 : eda1002.getEda10021s()) {
								txt.print("M362");
								txt.print(eda10021.getEda10021lct().getEbb05num());
								txt.newLine();
								
								qtLinM362++;
								qtLinBlocoM++;
							}
						}	
					}
				}
				
				/**
				 * REGISTRO M410: LANÇAMENTOS NA CONTA DA PARTE B DO E-LALUR E E-LACS SEM REFLEXO NA PARTE A
				 */
				if(!"A00".equals(mapM030.get(i).get("valor"))) {
					
					if(dtIniM410 == null) {
						dtIniM410 = mapM030.get(i).get("dtInicial");
					}
					
                    String sqlEda13 = "SELECT * " +
				                      "FROM Eda13 AS eda13 " + 
				                      "LEFT JOIN FETCH eda13.eda13conta AS aaj24 " +
				                      "WHERE eda13dtl BETWEEN :dtInicial AND :dtFinal " + getSamWhere().getWherePadrao("AND", Eda13.class);

					List<Eda13> eda13s = getAcessoAoBanco().buscarListaDeRegistros(sqlEda13, Parametro.criar("dtInicial", dtIniM410), Parametro.criar("dtFinal", mapM030.get(i).get("dtFinal")));
					for(Eda13 eda13 : eda13s) {
						txt.print("M410");
						txt.print(eda13.getEda13conta().getAaj24codigo());
						txt.print(eda13.getEda13indTrib() == 0 ? "I" : "C");
						txt.print(eda13.getEda13valor() != null && eda13.getEda13valor() > 0 ? formatarValor(eda13.getEda13valor(), 2) : "");
						txt.print(eda13.getEda13dc() == 0 ? "DB" : eda13.getEda13dc() == 1 ? "CR" : eda13.getEda13dc() == 2 ? "PF" : "BC");
						txt.print(eda13.getEda13contaCp() == null ? null : eda13.getEda13contaCp().getAaj24codigo());
						txt.print(eda13.getEda13hist() != null ? eda13.getEda13hist() : "");
						txt.print(eda13.getEda13tribDif() == 0 ? "N" : "S");
						txt.newLine();
						
						qtLinM410++;
						qtLinBlocoM++;
					}
					
					dtIniM410 = mapM030.get(i).get("dtFinal");
					dtIniM410 = dtIniM410.plusDays(1);
				}
			}
		}
		
		/**
		 * REGISTRO M990: ENCERRAMENTO DO BLOCO M
		 */				
		txt.print("M990");
		txt.print(qtLinBlocoM + 1);
		txt.newLine();
		qtLinBlocoM++;
		
		
		/*************************************************************************************************************
		 ***************************************************BLOCO Q***************************************************
		 *************************************************************************************************************/
		Integer qtLinBlocoQ = 0;
		Integer qtLinQ100 = 0;
		
		boolean blocoQComDados = false;
		
		BigDecimal sdoAnterior = new BigDecimal(0);
		List<Ebb05> lctos = null;
		
		if(gerarBlocoQ){
			LocalDate dtIni = dtInicial;
			
            String sqlEbb02 = "SELECT abc10id, abc10codigo, abc10nome, SUM(ebb02deb - ebb02cred) AS saldo " +
							  "FROM Ebb02 " +
							  "INNER JOIN Abc10 ON abc10id = ebb02cta " +
							  "WHERE abc10iLivCx = 1 AND " +
							  Fields.numMeses("ebb02mes", "ebb02ano") + " < :numMeses  " +
							  getSamWhere().getWherePadrao("AND", Ebb02.class) +
							  " GROUP BY abc10id, abc10codigo, abc10nome";
			
            List<TableMap> listEbb02s = getAcessoAoBanco().buscarListaDeTableMap(sqlEbb02, Parametro.criar("numMeses", Criterions.valNumMeses(dtIni.getMonthValue(), dtIni.getYear())));
			for(int i = 0; i < listEbb02s.size(); i++) {
				BigDecimal sdo = listEbb02s.get(i).getBigDecimal("saldo");
				if(dtIni.getMonthValue() > 1){
					dtIni = dtIni.plusDays(1); //Seta o primeiro dia da data inicial

					LocalDate dtFin = dtInicial; //Data final, ou seja, data inicial - 1 dia
					dtFin = dtFin.plusDays(1); 

                    String sqlEbb05deb = "SELECT SUM(ebb05valor) as valor " +
			 				             "FROM Ebb05 " + 
                                         "INNER JOIN Abc10 abc10deb ON ebb05deb = abc10deb.abc10id " +
								         "WHERE ebb05data BETWEEN :dtInicial AND :dtFinal AND " +
								         "abc10deb.abc10iLivCx = 1 AND " +
								         "abc10deb.abc10id = :abc10id " + getSamWhere().getWherePadrao("AND", Ebb05.class);						
                    
                    sdo = getAcessoAoBanco().obterBigDecimal(sqlEbb05deb, Parametro.criar("dtInicial", dtIni), Parametro.criar("dtFinal", dtFin), Parametro.criar("abc10id", listEbb02s.get(i).get("abc10id")));
					

                    String sqlEbb05cred = "SELECT SUM(ebb05valor) as valor " +
			 				              "FROM Ebb05 " + 
                                          "INNER JOIN Abc10 abc10cred ON ebb05cred = abc10cred.abc10id " +
								          "WHERE ebb05data BETWEEN :dtInicial AND :dtFinal AND " +
								          "abc10cred.abc10iLivCx = 1 AND " +
								          "abc10cred.abc10id = :abc10id " + getSamWhere().getWherePadrao("AND", Ebb05.class);

                    sdo = sdo.subtract(getAcessoAoBanco().obterBigDecimal(sqlEbb05cred, Parametro.criar("dtInicial", dtIni), Parametro.criar("dtFinal", dtFin), Parametro.criar("abc10id", listEbb02s.get(i).get("abc10id"))));
				}

				sdoAnterior = sdoAnterior.add(sdo);
			}
			
			String sqlLctos = "SELECT * " +
					  		  "FROM Ebb05 AS ebb05 " +
					  		  "INNER JOIN FETCH ebb05.ebb05deb AS abc10deb " +
					  		  "INNER JOIN FETCH ebb05.ebb05cred AS abc10cred " +
					  		  "WHERE ebb05.ebb05data BETWEEN :dtInicial AND :dtFinal AND " +
					  		  "(abc10deb.abc10iLivCx = 1 OR abc10cred.abc10iLivCx = 1) "+ getSamWhere().getWherePadrao("AND", Ebb05.class) + 
					  		  " ORDER BY ebb05.ebb05data, ebb05.ebb05num";
			
			lctos = getAcessoAoBanco().buscarListaDeRegistros(sqlLctos, Parametro.criar("dtInicial", dtInicial), Parametro.criar("dtFinal", dtFinal));
			if(lctos != null && lctos.size() > 0){
				blocoQComDados = true;
			}
		}
		
		
		/**
		 * REGISTRO Q001: ABERTURA DO BLOCO Q
		 */
		txt.print("Q001");
		txt.print(blocoQComDados ? "0" : "1");
		txt.newLine();
		qtLinBlocoQ++;
		
		
		if(blocoQComDados){
			/**
			 * REGISTRO Q100: DEMONSTRATIVO DO LIVRO CAIXA
			 */
			BigDecimal saldoAtual = sdoAnterior;
			
			txt.print("Q100");
			txt.print(dtfData.format(dtInicial));
			txt.print(null);
			txt.print("SALDO ANTERIOR");
			txt.print(sdoAnterior >= 0 ? formatarValor(sdoAnterior, 2) : formatarValor(new BigDecimal(0), 2));
			txt.print(sdoAnterior < 0 ? formatarValor(sdoAnterior, 2) : formatarValor(new BigDecimal(0), 2));
			txt.print(formatarValor(sdoAnterior, 2));
			txt.newLine();
			qtLinQ100++;
			qtLinBlocoQ++;
			
			
			for(Ebb05 ebb05 : lctos){
				for(int i = 0; i <= 1; i++){
					if(i == 0 && ebb05.getEbb05deb().getAbc10iLivCx() == 0)continue;
					if(i == 1 && ebb05.getEbb05cred().getAbc10iLivCx() == 0)continue;
					
					txt.print("Q100");
					txt.print(dtfData.format(ebb05.getEbb05data()));
					
					Abb01 abb01 = null;
					if(ebb05.getEbb05central() != null) {
						String sqlCentral = "SELECT abb01num, abb01id FROM Abb01 "+
										    "WHERE abb01id = :abb01id " + 
										    getSamWhere().getWherePadrao("AND", Abb01.class);
						
						abb01 = getAcessoAoBanco().buscarRegistroUnico(sqlCentral, Parametro.criar("abb01id", ebb05.getEbb05central().getAbb01id()));
						
					}
					
					txt.print(abb01.getAbb01num() > 0 ? abb01.getAbb01num() : null);
					txt.print(ebb05.getEbb05historico());
					
					if(i == 0 && ebb05.getEbb05deb().getAbc10iLivCx() == 1){
						txt.print(formatarValor(ebb05.getEbb05valor(), 2));
						txt.print(formatarValor(new BigDecimal(0), 2));
						
						saldoAtual = saldoAtual.add(ebb05.getEbb05valor());
						
					}else{
						txt.print(formatarValor(new BigDecimal(0), 2));	
						txt.print(formatarValor(ebb05.getEbb05valor(), 2));
						
						saldoAtual = saldoAtual.subtract(ebb05.getEbb05valor());
					}
					
					txt.print(formatarValor(saldoAtual, 2));
					txt.newLine();
					
					qtLinQ100++;
					qtLinBlocoQ++;
				}
			}
		}
		
		/**
		 * REGISTRO Q990: ENCERRAMENTO DO BLOCO Q
		 */				
		txt.print("Q990");
		txt.print(qtLinBlocoQ + 1);
		txt.newLine();
		qtLinBlocoQ++;
		
		/*************************************************************************************************************
		 ***************************************************BLOCO 9***************************************************
		 *************************************************************************************************************
		 */
		Integer qtLinBloco9 = 0;
		Integer qtLin9900 = 0;
		
		
		/**
		 * REGISTRO 9001: ABERTURA DO BLOCO 9
		 */
		txt.print("9001");
		txt.print("0");
		txt.newLine();
		qtLinBloco9++;
		
		/** 
		 * REGISTRO 9100: AVISOS DE ESCRITURAÇÃO --- REGISTRO NÃO IMPLEMENTADO
		 */
		
		/**
		 * REGISTRO 9900: REGISTROS DO ARQUIVO 
		 */
		//BLOCO 0
		gerarRegistro9900(txt, "0000", 1); qtLin9900++;
		gerarRegistro9900(txt, "0001", 1); qtLin9900++;
		gerarRegistro9900(txt, "0010", 1); qtLin9900++;
		gerarRegistro9900(txt, "0020", 1); qtLin9900++;
		gerarRegistro9900(txt, "0030", 1); qtLin9900++;
		gerarRegistro9900(txt, "0930", 1); qtLin9900++;
		gerarRegistro9900(txt, "0990", 1); qtLin9900++;
		
		//BLOCO J
		gerarRegistro9900(txt, "J001", 1); qtLin9900++;
		if(qtLinJ050 > 0)qtLin9900++; gerarRegistro9900(txt, "J050", qtLinJ050);
		if(qtLinJ051 > 0)qtLin9900++; gerarRegistro9900(txt, "J051", qtLinJ051);
		if(qtLinJ053 > 0)qtLin9900++; gerarRegistro9900(txt, "J053", qtLinJ053);
		gerarRegistro9900(txt, "J990", 1); qtLin9900++;
		
		//BLOCO L
		gerarRegistro9900(txt, "L001", 1); qtLin9900++;
		if(qtLinL030 > 0)qtLin9900++; gerarRegistro9900(txt, "L030", qtLinL030);
		if(qtLinL200 > 0)qtLin9900++; gerarRegistro9900(txt, "L200", qtLinL200);
		if(qtLinL210 > 0)qtLin9900++; gerarRegistro9900(txt, "L210", qtLinL210);
		gerarRegistro9900(txt, "L900", 1); qtLin9900++;
		
		//BLOCO M
		gerarRegistro9900(txt, "M001", 1); qtLin9900++;
		if(qtLinM010 > 0)qtLin9900++; gerarRegistro9900(txt, "M010", qtLinM010);
		if(qtLinM030 > 0)qtLin9900++; gerarRegistro9900(txt, "M030", qtLinM030);
		if(qtLinM300 > 0)qtLin9900++; gerarRegistro9900(txt, "M300", qtLinM300);
		if(qtLinM305 > 0)qtLin9900++; gerarRegistro9900(txt, "M305", qtLinM305);
		if(qtLinM310 > 0)qtLin9900++; gerarRegistro9900(txt, "M310", qtLinM310);
		if(qtLinM312 > 0)qtLin9900++; gerarRegistro9900(txt, "M312", qtLinM312);
		if(qtLinM350 > 0)qtLin9900++; gerarRegistro9900(txt, "M350", qtLinM350);
		if(qtLinM355 > 0)qtLin9900++; gerarRegistro9900(txt, "M355", qtLinM355);
		if(qtLinM360 > 0)qtLin9900++; gerarRegistro9900(txt, "M360", qtLinM360);
		if(qtLinM362 > 0)qtLin9900++; gerarRegistro9900(txt, "M362", qtLinM362);
		if(qtLinM410 > 0)qtLin9900++; gerarRegistro9900(txt, "M410", qtLinM410);
		gerarRegistro9900(txt, "M990", 1); qtLin9900++;
		
		//BLOCO Q
		gerarRegistro9900(txt, "Q001", 1); qtLin9900++;
		if(qtLinQ100 > 0)qtLin9900++; gerarRegistro9900(txt, "Q100", qtLinQ100);
		gerarRegistro9900(txt, "Q990", 1); qtLin9900++;
		
		//BLOCO 9
		gerarRegistro9900(txt, "9001", 1); qtLin9900++;
		if(qtLin9900 > 0)qtLin9900++; gerarRegistro9900(txt, "9900", qtLin9900 + 2);//2 = 9990 e 9999
		gerarRegistro9900(txt, "9990", 1); qtLin9900++;
		gerarRegistro9900(txt, "9999", 1); qtLin9900++;
		
		qtLinBloco9 = qtLinBloco9 + qtLin9900;
		
		
		/**
		 * REGISTRO 9990: ENCERRAMENTO DO BLOCO 9
		 */				
		txt.print("9990");
		txt.print(qtLinBloco9 + 2); // 2 - 9990 e 9999
		txt.newLine();
		qtLinBloco9++;
		
		/**
		 * REGISTRO 9999: ENCERRAMENTO DO ARQUIVO DIGITAL
		 */			
		txt.print("9999");
		txt.print(qtLinBloco0 + qtLinBlocoJ + qtLinBlocoL + qtLinBlocoM + qtLinBlocoQ + qtLinBloco9 + 1);
		txt.newLine();
		
		
		// Adiciona o arquivo TXT no campo dadosArquivo para que o service da tarefa grave o arquivo no local informado
		put("dadosArquivo", txt);
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ECF;
	}
	
	private void gerarRegistro9900(TextFileEscrita txt, String registro, int qtdLinhas) {
		if(qtdLinhas > 0) {
			txt.print("9900");
			txt.print(registro);                      					                                
			txt.print(qtdLinhas);
			txt.newLine();	
		}
	}
	
	private String getCodigoPerApurTrimestral(int mes) {
		String codigo = null;
		
		if(mes <= 3) codigo = "T01";
		else if(mes <= 6) codigo = "T02";
		else if(mes <= 9) codigo = "T03";
		else codigo = "T04";
		
		return codigo;
	}
	
	private void setUltimoDiaDoTrimestre(LocalDate dtPer) {
		LocalDate dtTri = null;
		
		dtTri = LocalDate.of(dtPer.getYear(), Month.MARCH, 31);
		if(DateUtils.dateDiff(dtPer, dtTri, ChronoUnit.DAYS) >= 0) {
			dtPer = dtTri;
			return;
		}
		
		dtTri = LocalDate.of(dtPer.getYear(), Month.JUNE, 30);
		if(DateUtils.dateDiff(dtPer, dtTri, ChronoUnit.DAYS) >= 0) {
			dtPer = dtTri;
			return;
		}
		
		dtTri = LocalDate.of(dtPer.getYear(), Month.SEPTEMBER, 30);
		if(DateUtils.dateDiff(dtPer, dtTri, ChronoUnit.DAYS) >= 0) {
			dtPer = dtTri;
			return;
		}
		
		dtTri = LocalDate.of(dtPer.getYear(), Month.DECEMBER, 31);
		if(DateUtils.dateDiff(dtPer, dtTri, ChronoUnit.DAYS) >= 0) {
			dtPer = dtTri;
			return;
		}
	}
	
	private boolean verificarMovimentoBlocoM(int ano, LocalDate dtInicial, LocalDate dtFinal){
		String sqlEda12 = "SELECT * FROM Eda12 "+
						  "WHERE eda12ano = :ano " +
						  getSamWhere().getWherePadrao("AND", Eda12.class);
	
		List<Eda12> eb31s = getAcessoAoBanco().buscarListaDeRegistros(sqlEda12, Parametro.criar("ano", ano));
		if(eb31s != null && eb31s.size() > 0){
			return true;
		}

		LocalDate dt = dtFinal;
		String sqlCount = "SELECT COUNT(*) FROM Eda10 " + 
				 	 	  "WHERE eda10ano = :ano " + getSamWhere().getWherePadrao("AND", Eda10.class);
		
		Integer result = getAcessoAoBanco().obterCount(sqlCount, Parametro.criar("ano", dt.getYear()));
		if(result > 0){
			return true;
		}
		
		String sqlEda13 = "SELECT * FROM Eda13 " + 
				 		  "WHERE eda13dtl BETWEEN :dtInicial AND :dtFinal " + getSamWhere().getWherePadrao("AND", Eda13.class);
		List<Eda13> eb32s = getAcessoAoBanco().buscarListaDeRegistros(sqlEda13, Parametro.criar("dtInicial", dtInicial), Parametro.criar("dtFinal", dtFinal));
		if(eb32s != null && eb32s.size() > 0){
			return true;
		}
		
		return false;
	}
	
	private String getCodigoPerApurAnual(int mes) {
		String codigo = null;
		
		if(mes == 1) codigo = "A01";
		else if(mes == 2) codigo = "A02";
		else if(mes == 3) codigo = "A03";
		else if(mes == 4) codigo = "A04";
		else if(mes == 5) codigo = "A05";
		else if(mes == 6) codigo = "A06";
		else if(mes == 7) codigo = "A07";
		else if(mes == 8) codigo = "A08";
		else if(mes == 9) codigo = "A09";
		else if(mes == 10) codigo = "A10";
		else if(mes == 11) codigo = "A11";
		else codigo = "A12";
		
		return codigo;
	}
	
	/**
	 * Formata os campos de valores
	 * @param valor
	 * @param casasDecimais
	 * @return
	 */
	private String formatarValor(BigDecimal valor, int casasDecimais) {
		return formatarValor(valor, casasDecimais, true);
	}
	private String formatarValor(BigDecimal valor, int casasDecimais, boolean isRequerido) {
		if(valor == null) return null;

		if(valor.equals(new BigDecimal(0)) && !isRequerido) return null;
		
		valor = DecimalUtils.create(valor).round(casasDecimais).get();
		
		NumberFormat format = NumberFormat.getInstance(new Locale("pt", "BR"));
		format.setGroupingUsed(false);
		format.setMinimumFractionDigits(casasDecimais);
		format.setMaximumFractionDigits(casasDecimais);
		
		return format.format(valor.abs());
	}
	private String buscarUfEmpresa(Long idEmp) {
		Query query = getSession().createQuery("SELECT aag02uf FROM Aag02 INNER JOIN Aag0201 ON aag0201uf = aag02id INNER JOIN Aac10 ON aac10municipio = aag0201id WHERE Aac10id = :idEmp");
		query.setParameter("idEmp", idEmp);
		String retorno = query.getUniqueResult(ColumnType.STRING);
		return retorno;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTcifQ==