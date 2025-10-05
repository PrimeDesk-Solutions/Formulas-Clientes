package multitec.baseDemo

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.Utils
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.da.Daa01
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase

class SCF_SerasaRemessa extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_SERVICO_DE_PROTECAO_AO_CREDITO;
	}

	@Override
	public void executar() {
		List<Long> daa01ids = getListLong("daa01ids");
		def periodicidade = get("periodicidade");
		def nomeInstituto = get("nomeInstituto");
		LocalDate dataInicial = get("dataInicial");
		LocalDate dataFinal = get("dataFinal");
		
		String sql = " SELECT daa01id, daa01valor, daa01dtVctoN, daa01dtPgto, daa01central " + 
					 " FROM Daa01 " +
					 " WHERE daa01id IN (:daa01ids) " +
					 getSamWhere().getWherePadrao("AND", Daa01.class);
					 
		List<Daa01> daa01s = getAcessoAoBanco().buscarListaDeRegistros(sql, criarParametroSql("daa01ids", daa01ids));

		/**
		 * Selecionando as entidades que serão enviadas no registro de "TEMPO DE RELACIONAMENTO"
		 * Neste registro devem ser enviados somente as entidades que nunca foram informadas ao SPC
		 */
		Integer qtEntidades = 0;
		Set<Long> entidades = new HashSet<>();
		for(Daa01 daa01 : daa01s){
			if (Utils.campoEstaCarregado(daa01, "daa01central")) {
				String sqlAbe01 = " SELECT abe01id FROM Abe01 " + 
								  " WHERE abe01id IN (SELECT abb01ent FROM Abb01 WHERE abb01id = :daa01central " + getSamWhere().getWherePadrao("AND", Abb01.class) + ") " +
								  getSamWhere().getWherePadrao("AND", Abe01.class);
								  
				Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnico(sqlAbe01, criarParametroSql("daa01central", daa01.daa01central.abb01id));
				
				if(entidades.size() == 0 || !entidades.contains(abe01.abe01id)) {
					if(!existeDocumentoEnviadoAoSPC(abe01.abe01id)) {
						entidades.add(abe01.abe01id)
						qtEntidades++;
					}
				}
			}
			
		}
		
		TextFile txt = new TextFile();
		
		/**
		 * HEADER
		 */
		txt.print("00");																											//001-002
		txt.print("RELATO COMP NEGOCIOS");																							//003-022
		txt.print(StringUtils.extractNumbers(obterEmpresaAtiva().aac10ni), 14);														//023-036
		txt.print(dataInicial.format(DateTimeFormatter.ofPattern("yyyyMMdd")));														//037-044
		txt.print(dataFinal.format(DateTimeFormatter.ofPattern("yyyyMMdd")));														//045-052
		txt.print(periodicidade == 0 ? "S" : "D");																					//053-053
		txt.print(StringUtils.space(15));																							//054-068
		txt.print(StringUtils.space(3));																							//069-071
		txt.print(StringUtils.space(29));																							//072-100
		txt.print("V.");																											//101-102
		txt.print("01");																											//103-104
		txt.print(StringUtils.space(26));																							//105-130
		txt.newLine();
		
		/**
		 * DETALHE - TEMPO DE RELACIONAMENTO
		 */
		for(Long abe01id : entidades){
			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnico("SELECT abe01id, abe01codigo, abe01ni, abe01cli, abe01di FROM Abe01 WHERE abe01id = :abe01id", criarParametroSql("abe01id", abe01id));
			LocalDate dtPrimeiraCompra = buscaDataDaPrimeiraVenda(abe01id);
			if(dtPrimeiraCompra == null) interromper("Não foi encontrada a data da primeira venda da entidade " + abe01.abe01codigo + ".");
			
			txt.print("01");																									//001-002
			txt.print(StringUtils.extractNumbers(abe01.abe01ni), 14);															//003-016
			txt.print("01");																									//017-018
			txt.print(dtPrimeiraCompra.format(DateTimeFormatter.ofPattern("yyyyMMdd")));										//019-026

			if(abe01.abe01di != null || abe01.abe01cli == 0) {                         			 //Inativo						//027-027
				txt.print("3");
			}else if(DateUtils.dateDiff(MDate.date(), dtPrimeiraCompra, ChronoUnit.YEARS) >= 1) { //Um ano ou mais
				txt.print("1");
			}else if(DateUtils.dateDiff(MDate.date(), dtPrimeiraCompra, ChronoUnit.YEARS) <= 0) { //Menos de um ano
				txt.print("2");
			}

			txt.print(StringUtils.space(38));																					//028-065
			txt.print(StringUtils.space(34));																					//066-099
			txt.print(StringUtils.space(1));																					//100-100
			txt.print(StringUtils.space(30));																					//101-130
			txt.newLine();
		}
		
		/**
		 * DETALHE - TÍTULOS
		 */
		Integer qtTitulos = 0;
		for(Daa01 daa01 : daa01s){
			def abe01ni = null;
			Abb01 daa01central = null;
			if (Utils.campoEstaCarregado(daa01, "daa01central")) {
				String sqlAbe01 = " SELECT abe01ni FROM Abe01 " +
								  " WHERE abe01id IN (SELECT abb01ent FROM Abb01 WHERE abb01id = :daa01central " + getSamWhere().getWherePadrao("AND", Abb01.class) + ") " +
								  getSamWhere().getWherePadrao("AND", Abe01.class);
				
				abe01ni = getAcessoAoBanco().obterString(sqlAbe01, criarParametroSql("daa01central", daa01.daa01central.abb01id));
				
				String sqlAbb01 = " SELECT abb01id, abb01num, abb01data, abb01parcela, abb01quita " +
								  " FROM Abb01 WHERE abb01id = :daa01central " + getSamWhere().getWherePadrao("AND", Abb01.class);
				daa01central = getAcessoAoBanco().buscarRegistroUnico(sqlAbb01, criarParametroSql("daa01central", daa01.daa01central.abb01id));
			}

			txt.print("01");																										//001-002
			txt.print(StringUtils.extractNumbers(abe01ni), 14)	;																	//003-016
			txt.print("05");																										//017-018
			txt.print(StringUtils.space(10));																						//019-028
			txt.print(daa01central != null ? daa01central.abb01data.format(DateTimeFormatter.ofPattern("yyyyMMdd")) : null);		//029-036
			BigDecimal valor = daa01.daa01valor * 100;
			txt.print(valor.intValue(), 13);																						//037-049
			txt.print(daa01.daa01dtVctoN.format(DateTimeFormatter.ofPattern("yyyyMMdd")));											//050-057
			txt.print(daa01.daa01dtPgto == null ? null : daa01.daa01dtPgto.format(DateTimeFormatter.ofPattern("yyyyMMdd")), 8);		//058-065

			StringBuilder numero = new StringBuilder("#D");
			numero.append(daa01central == null || daa01central.abb01num == null ? "" : daa01central.abb01num);
			numero.append(daa01central == null || daa01central.abb01parcela == null ? "" : daa01central.abb01parcela);
			numero.append(daa01central == null || daa01central.abb01quita == null ? "" : daa01central.abb01quita);
			numero.append(";");
			numero.append(daa01.daa01id);
			txt.print(numero.toString(), 34);																						//066-099

			txt.print(StringUtils.space(1));																						//100-100
			txt.print(StringUtils.space(24));																						//101-124
			txt.print(StringUtils.space(2));																						//125-126
			txt.print(StringUtils.space(1));																						//127-127
			txt.print(StringUtils.space(3));																						//128-130
			txt.newLine();

			qtTitulos++;
		}

		/**
		 * TRAILLER
		 */
		txt.print("99");																											//001-002
		txt.print(qtEntidades, 11);																									//003-013
		txt.print(StringUtils.space(44));																							//014-057
		txt.print(qtTitulos, 11);																									//058-068
		txt.print(StringUtils.space(11));																							//069-079
		txt.print(StringUtils.space(11));																							//080-090
		txt.print(StringUtils.space(10));																							//091-100
		txt.print(StringUtils.space(30));																							//101-130
		txt.newLine();
		
		put("txt", txt);
	}
	
	/**
	 * Verifica se existe ao menos um documentos financeiro, da entidade informada, que já tenha sido enviado ao SPC 
	 * @param abe01id Long Id da entidade
	 * @return boolean True existe e False não existe
	 */
	public boolean existeDocumentoEnviadoAoSPC(Long abe01id) {
		String sql = " SELECT COUNT(*) FROM Daa01 " +
					 " INNER JOIN Abb01 ON abb01id = daa01central " +
					 " WHERE daa01spcEnvio > 1 AND abb01ent = :abe01id " + getSamWhere().getWherePadrao("AND", Daa01.class);

		long count = getAcessoAoBanco().obterLong(sql, criarParametroSql("abe01id", abe01id));
		return count > 0;
	}
	
	/**
	 * Busca a data da primeira venda pelo ID da entidade 
	 * @param abe01id Long Id da entidade
	 * @return LocalDate data
	 */
	public LocalDate buscaDataDaPrimeiraVenda(Long abe01id) {
		String sql = " SELECT abb01data FROM Eaa01 " +
					 " INNER JOIN Abb01 ON abb01id = eaa01central " +
					 " WHERE eaa01clasDoc = :eaa01clasDoc AND abb01ent = :abe01id " +
					 " AND eaa01esMov = :eaa01esMov " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class) +
					 " ORDER BY abb01data ASC ";

		LocalDate dataPrimeiraVenda = getAcessoAoBanco().obterDate(sql, criarParametroSql("abe01id", abe01id), 
													  					criarParametroSql("eaa01clasDoc", Eaa01.CLASDOC_SCV), 
																		criarParametroSql("eaa01esMov", Eaa01.ESMOV_SAIDA));

		return dataPrimeiraVenda;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDcifQ==