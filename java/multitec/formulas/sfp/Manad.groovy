package multitec.formulas.sfp;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFP8504Dto;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac1002;
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Abb11;
import sam.model.entities.ab.Abc10;
import sam.model.entities.ab.Abh02;
import sam.model.entities.ab.Abh20;
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh2101;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba02;
import sam.model.entities.fb.Fba10;
import sam.server.samdev.formula.FormulaBase;

public class Manad extends FormulaBase {
	private SFP8504Dto sfp8504Dto;
	private int qtdeDig;
	
	private int linhas0000 = 0;
	private int totalBloco0 = 0;

	private int linhasI050 = 0;
	private int linhasI100 = 0;
	private int totalBlocoI = 0;

	private int linhasK050 = 0;
	private int linhasK100 = 0;
	private int linhasK150 = 0;
	private int linhasK200 = 0;
	private int linhasK250 = 0;
	private int linhasK300 = 0;
	private int totalBlocoK = 0;

	@Override
	public void executar() {
		this.sfp8504Dto = get("sfp8504Dto");
		this.qtdeDig = get("qtdeDig");
		
		Aac10 aac10 = getVariaveis().aac10;
		
		TextFile txt = new TextFile();
		
		gerarTipo0000(txt, aac10, sfp8504Dto);
		gerarTipo0001(txt);
		gerarTipo0050(txt, aac10);
		gerarTipo0100(txt, aac10);
		
		totalBloco0 = linhas0000 + 4;
		gerarTipo0990(txt);
		gerarTipoI001(txt, sfp8504Dto);
		
		if(sfp8504Dto.criarRegContab) {
			gerarTipoI050(txt, sfp8504Dto);
			gerarTipoI100(txt, sfp8504Dto, qtdeDig);
		}
		totalBlocoI = linhasI050 + linhasI100 + 2;
		
		gerarTipoI990(txt);
		gerarTipoK001(txt);
		gerarTipoK050(txt, sfp8504Dto, aac10);
		gerarTipoK100(txt, sfp8504Dto, aac10);
		gerarTipoK150(txt, sfp8504Dto, aac10);
		
		if(sfp8504Dto.criarRegContab) {
			gerarTipoK200(txt, sfp8504Dto, aac10);
		}
		
		gerarTipoK250(txt, sfp8504Dto, aac10, qtdeDig);
		gerarTipoK300(txt, sfp8504Dto, aac10, qtdeDig);
		gerarTipoK990(txt);
		
		gerarTipo9001(txt);
		gerarTipo9900(txt);
		gerarTipo9990(txt);
		
		int totalLinhas = totalBloco0 + totalBlocoI + totalBlocoK + 24;
		gerarTipo9999(txt, totalLinhas);
		
		put("txt", txt);
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.MANAD;
	}
	
	/**
	 * TIPO 0000
	 */
	private void gerarTipo0000(TextFile txt, Aac10 aac10, SFP8504Dto sfp8504Dto) {
		def cnpj0000 = "";
		def cpf0000 = "";
		if(aac10.aac10ti == Aac10.TI_CNPJ) {
			cnpj0000 = StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);
		}else {
			cpf0000 = StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 11, '0', true);
		}
		
		def cei0000 = StringUtils.space(12);
		def nit0000 = StringUtils.space(11);
		
		Aag0201 aag0201 = aac10.aac10municipio != null ? getSession().get(Aag0201.class, aac10.aac10municipio.aag0201id) : null;
		Aag02 aag02 = aag0201 != null ? buscarEstadoPorIdMunicipio(aag0201.aag0201id) : null;
		def aac1002ie = aag02 != null ? buscarInscricaoEstadualPorEmpresa(aac10.aac10id, aag02.aag02id) : "";
		
		txt.print("0000", 4);
		txt.print(aac10.aac10rs);
		txt.print(cnpj0000, 14);
		txt.print(cpf0000, 11);
		txt.print(cei0000, 12);
		txt.print(nit0000, 11);
		txt.print(aag02 != null ? aag02.aag02uf : "", 2);
		txt.print(StringUtils.extractNumbers(aac1002ie));
		txt.print(aag0201 != null ? aag0201.aag0201ibge : "", 7, '0', true);
		txt.print(aac10.aac10im);
		txt.print(aac10.aac10suframa, 9);
		txt.print(sfp8504Dto.centrEscrit, 1);
		txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
		txt.print(sfp8504Dto.dataFinal.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
		txt.print("003", 3);
		txt.print(sfp8504Dto.finalidadeArq, 2);
		txt.print("2", 1);
		txt.newLine();
		linhas0000++;
	}
	
	/**
	 * TIPO 0001
	 */
	private void gerarTipo0001(TextFile txt) {
		txt.print("0001", 4);
		txt.print("0", 1);
		txt.newLine();
	}
	
	/**
	 * TIPO 0050
	 */
	private void gerarTipo0050(TextFile txt, Aac10 aac10) {
		def cnpj0050 = aac10.aac10cCnpj != null ? StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10cCnpj), 14, '0', true) : "";
		def cpf0050 = aac10.aac10cCpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10cCpf), 11, '0', true) : "";
		def fone0050 = aac10.aac10cDddFone != null && aac10.aac10cFone != null ? aac10.aac10cDddFone + aac10.aac10cFone : aac10.aac10cFone;
		def fax0050 = "";
		Aag02 aag02 = aac10.aac10cMunicipio != null ? buscarEstadoPorIdMunicipio(aac10.aac10cMunicipio.aag0201id) : null;
		
		txt.print("0050", 4);
		txt.print(aac10.aac10cNome);
		txt.print(cnpj0050, 14);
		txt.print(cpf0050, 11);
		txt.print(aac10.aac10cCrc, 11);
		txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
		txt.print(sfp8504Dto.dataFinal.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
		txt.print(aac10.aac10cEndereco);
		txt.print(aac10.aac10cNumero);
		txt.print(aac10.aac10cComplem);
		txt.print(aac10.aac10cBairro);
		txt.print(aac10.aac10cCep, 8);
		txt.print(aag02 != null ? aag02.aag02uf : "", 2);
		txt.print(aac10.aac10cCp);
		txt.print(aac10.aac10cCepCp, 8);
		txt.print(fone0050);
		txt.print(fax0050);
		txt.print(aac10.aac10email);
		txt.newLine();
	}
	
	/**
	 * TIPO 0100
	 */
	private void gerarTipo0100(TextFile txt, Aac10 aac10) {
		def cnpj0100 = aac10.aac10aCnpj != null ? StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10aCnpj), 14, '0', true) : "";
		def cpf0100 = aac10.aac10aCpf != null ? StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10aCpf), 11, '0', true) : "";
		def fone0100 = aac10.aac10aDddFone != null && aac10.aac10aFone != null ? aac10.aac10aDddFone + aac10.aac10aFone : aac10.aac10aFone;
		def fax0100 = "";
		
		txt.print("0100", 4);
		txt.print(aac10.aac10aNome);
		txt.print("Técnico de Suporte");
		txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
		txt.print(sfp8504Dto.dataFinal.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
		txt.print(cnpj0100, 14);
		txt.print(cpf0100, 11);
		txt.print(fone0100);
		txt.print(fax0100);
		txt.print(aac10.aac10aEmail);
		txt.newLine();
	}
	
	/**
	 * TIPO 0990
	 */
	private void gerarTipo0990(TextFile txt) {
		txt.print("0990", 4);
		txt.print(totalBloco0);
		txt.newLine();
	}
	
	/**
	 * TIPO I001
	 */
	private void gerarTipoI001(TextFile txt, SFP8504Dto sfp8504Dto) {
		txt.print("I001", 4);
		txt.print(sfp8504Dto.criarRegContab ? 0 : 1, 1);
		txt.newLine();
	}
	
	/**
	 * TIPO I050
	 */
	private void gerarTipoI050(TextFile txt, SFP8504Dto sfp8504Dto) {
		List<TableMap> tmAbc10s = buscarDadosDePlanosDeContasParaGeracaoDoManad();
		if(tmAbc10s != null && tmAbc10s.size() > 0) {
			for(TableMap tm : tmAbc10s) {
				def cod = tm.getString("abc10codigo");
				def tamCod = cod.length();
				def nivelConta = tamCod == 11 ? 6 : tamCod == 7 ? 5 : tamCod == 5 ? 4 : tamCod;
				def nivelSuperior = tamCod == 11 ? cod.substring(0, 7) : tamCod == 7 ? cod.substring(0, 5) : tamCod == 5 ? cod.substring(0, 3) : tamCod == 3 ? cod.substring(0, 2) : tamCod == 2 ? cod.substring(0, 1) : "";
				
				txt.print("I050", 4);
				txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(tm.getInteger("abc10manadNat"), 1);
				txt.print(tm.getInteger("abc10reduzido") == 0 ? "S" : "A", 1);
				txt.print(nivelConta);
				txt.print(cod);
				txt.print(nivelSuperior);
				txt.print(tm.getString("abc10nome"));
				txt.newLine();
				linhasI050++;
			}
		}
	}
	
	/**
	 * TIPO I100
	 */
	private void gerarTipoI100(TextFile txt, SFP8504Dto sfp8504Dto, Integer tamCodAbb11) {
		List<TableMap> tmAbb11s = buscarDadosDeDepartamentosParaGeracaoDoManad(tamCodAbb11);
		if(tmAbb11s != null && tmAbb11s.size() > 0) {
			for(TableMap tm : tmAbb11s) {
				txt.print("I100", 4);
				txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(tm.getString("abb11codigo"));
				txt.print(tm.getString("abb11nome"));
				txt.newLine();
				linhasI100++;
			}
		}
	}
	
	/**
	 * TIPO I990
	 */
	private void gerarTipoI990(TextFile txt) {
		txt.print("I990", 4);
		txt.print(totalBlocoI);
		txt.newLine();
	}
	
	/**
	 * TIPO K001
	 */
	private void gerarTipoK001(TextFile txt) {
		txt.print("K001", 4);
		txt.print("0");
		txt.newLine();
	}
	
	/**
	 * TIPO K050
	 */
	private void gerarTipoK050(TextFile txt, SFP8504Dto sfp8504Dto, Aac10 aac10) {
		List<TableMap> tmFba0101s = buscarDadosDeTrabalhadoresParaGeracaoDoManad(sfp8504Dto.dataInicial, sfp8504Dto.dataFinal);
		if(tmFba0101s != null && tmFba0101s.size() > 0) {
			for(TableMap tm : tmFba0101s) {
				Fba02 fba02 = buscarDadosDoHistoricoDoCalculo(tm.getLong("abh80id"), sfp8504Dto.dataInicial);
				
				def cpfk050 = StringUtils.extractNumbers(tm.getString("abh80cpf"));
				def nitk050 = StringUtils.extractNumbers(fba02.fba02pis);
				def dtResk050 = tm.getInteger("abh80sit") == Abh80.SIT_DEMITIDO ? tm.getDate("abh80dtResTrans") : null;
				
				txt.print("K050", 4);
				txt.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(fba02.fba02codigo);
				txt.print(cpfk050, 11, '0', true);
				txt.print(nitk050, 11, '0', true);
				txt.print(fba02.fba02categ != null ? fba02.fba02categ.aap14manad : "");
				txt.print(fba02.fba02nome);
				txt.print(fba02.fba02dtNasc.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(fba02.fba02dtAdmis.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(dtResk050 != null ? dtResk050.format(DateTimeFormatter.ofPattern("ddMMyyyy")) : "");
				txt.print("", 0);
				txt.print("", 0);
				txt.print("", 0);
				txt.print("", 0);
				txt.newLine();
				linhasK050++;
			}
		}
	}
	
	/**
	 * TIPO K100
	 */
	private void gerarTipoK100(TextFile txt, SFP8504Dto sfp8504Dto, Aac10 aac10) {
		List<TableMap> tmAbh02s = buscarDadosDeLotacoesParaGeracaoDoManad();
		if(tmAbh02s != null && tmAbh02s.size() > 0) {
			for(TableMap tm : tmAbh02s) {
				txt.print("K100", 4);
				txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(tm.getString("abh02codigo"));
				txt.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt.print(tm.getString("abh02nome"));
				txt.print(StringUtils.extractNumbers(tm.getString("abh02ni")), 14);
				txt.newLine();
				linhasK100++;
			}
		}
	}
	
	/**
	 * TIPO K150
	 */
	private void gerarTipoK150(TextFile txt, SFP8504Dto sfp8504Dto, Aac10 aac10) {
		List<TableMap> tmAbh21s = buscarDadosDeEventosParaGeracaoDoManad();
		if(tmAbh21s != null && tmAbh21s.size() > 0) {
			for(TableMap tm : tmAbh21s) {
				txt.print("K150", 4);
				txt.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(tm.getString("abh21codigo"));
				txt.print(tm.getString("abh21nome"));
				txt.newLine();
				linhasK150++;
			}
		}
	}
	
	/**
	 * TIPO K200
	 */
	private void gerarTipoK200(TextFile txt, SFP8504Dto sfp8504Dto, Aac10 aac10) {
		List<TableMap> tmFba10s = buscarDadosDePlanilhasContabeisParaGeracaoDoManad();
		if(tmFba10s != null && tmFba10s.size() > 0) {
			Set<String> setKeys = new HashSet<String>();
			for(TableMap tm : tmFba10s) {
				def codContak200 = tm.getInteger("abh21tipo") == Abh21.TIPO_DESCONTO ? tm.getString("codCred") : tm.getString("codDeb");
				def key = tm.getString("abb11codigo")  + "/" + tm.getString("abh21codigo") + "/" + codContak200;
				if(setKeys.contains(key)) continue;
				
				txt.print("K200", 4);
				txt.print(sfp8504Dto.dataInicial.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt.print(tm.getString("abh21codigo"));
				txt.print(tm.getString("abb11codigo"));
				txt.print(tm.getString("abb11codigo"));
				txt.print(codContak200);
				txt.newLine();
				linhasK200++;
				setKeys.add(key);
			}
		}
	}
	
	/**
	 * TIPO K250
	 */
	private void gerarTipoK250(TextFile txt, SFP8504Dto sfp8504Dto, Aac10 aac10, Integer tamCodAbb11) {
		List<TableMap> tmFba01011s = buscarDadosDeEventosDeValoresParaGeracaoDoManadK250(sfp8504Dto.dataInicial, sfp8504Dto.dataFinal, tamCodAbb11);
		if(tmFba01011s != null && tmFba01011s.size() > 0) {
			NumberFormat nb = NumberFormat.getNumberInstance();
			nb.setMinimumFractionDigits(2);
			nb.setGroupingUsed(false);
			
			for(TableMap tm : tmFba01011s) {
				int tp = tm.getInteger("fba0101tpVlr");
				int tipok250 = tp == 0 ? 1 : tp == 1 ? 4 : tp == 4 ? 6 : tp == 9 ? 7 : tp;
				
				def data = LocalDate.of(tm.getInteger("fba01ano"), tm.getInteger("fba01mes"), 1);
				def fba0101 = buscarUltimoCalculoDoMesParaGeracaoDoManad(tm.getString("abh80codigo"), tm.getInteger("fba01mes"), tm.getInteger("fba01ano"), tm.getInteger("fba0101tpVlr"));
				def fba02 = buscarDadosDoHistoricoDoCalculo(tm.getLong("abh80id"), sfp8504Dto.dataInicial);
				
				def qtdeDepIR = sfp8504Dto.abh20BCIR != null ? calcularCAE(tm.getLong("abh80id"), tm.getInteger("fba01mes"), tm.getInteger("fba01ano"), tm.getInteger("fba0101tpVlr"), sfp8504Dto.abh20BCIR.abh20codigo, tm.getString("codAbb11")) : BigDecimal.ZERO;
				def qtdeDepSF = sfp8504Dto.abh20BCINSSSalFam != null ? calcularCAE(tm.getLong("abh80id"), tm.getInteger("fba01mes"), tm.getInteger("fba01ano"), tm.getInteger("fba0101tpVlr"), sfp8504Dto.abh20BCINSSSalFam.abh20codigo, tm.getString("codAbb11")) : BigDecimal.ZERO;
				def valorIR = sfp8504Dto.abh20BCIR != null ? calcularCAE(tm.getLong("abh80id"), tm.getInteger("fba01mes"), tm.getInteger("fba01ano"), tm.getInteger("fba0101tpVlr"), sfp8504Dto.abh20BCIR.abh20codigo, tm.getString("codAbb11")) : BigDecimal.ZERO;
				def valorSF = sfp8504Dto.abh20BCINSS != null ? calcularCAE(tm.getLong("abh80id"), tm.getInteger("fba01mes"), tm.getInteger("fba01ano"), tm.getInteger("fba0101tpVlr"), sfp8504Dto.abh20BCINSS.abh20codigo, tm.getString("codAbb11")) : BigDecimal.ZERO;
				
				txt.print("K250", 4);
				txt.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt.print(tipok250);
				txt.print(tm.getString("codAbb11"));
				txt.print(tm.getString("abh80codigo"));
				txt.print(data.format(DateTimeFormatter.ofPattern("MMyyyy")), 6);
				txt.print(fba0101.fba0101dtPgto.format(DateTimeFormatter.ofPattern("ddMMyyyy")), 8);
				txt.print(fba02.fba02cbo);
				txt.print(fba02 != null && fba02.fba02categ != null ? fba02.fba02categ.aap14manad : "");
				txt.print(fba0101.fba0101trab.abh80cargo.abh05nome);
				txt.print(qtdeDepIR.intValue());
				txt.print(qtdeDepSF.intValue());
				txt.print(nb.format(valorIR));
				txt.print(nb.format(valorSF));
				txt.newLine();
				linhasK250++;
			}
		}
	}
	
	/**
	 * TIPO K300
	 */
	private void gerarTipoK300(TextFile txt, SFP8504Dto sfp8504Dto, Aac10 aac10, Integer tamCodAbb11) {
		List<TableMap> tmFba01011s = buscarDadosDeEventosDeValoresParaGeracaoDoManadK300(sfp8504Dto.dataInicial, sfp8504Dto.dataFinal, tamCodAbb11);
		if(tmFba01011s != null && tmFba01011s.size() > 0) {
			NumberFormat nb = NumberFormat.getNumberInstance();
			nb.setMinimumFractionDigits(2);
			nb.setGroupingUsed(false);

			Set<String> setEveBCIRSalarios = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCIRSalMensal);
			Set<String> setEveBCIR13 = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCIR13Sal);
			Set<String> setEveBCINSSSalarios = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCINSSSalContrib);
			Set<String> setEveBCINSS13 = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCINSS13Sal);
			Set<String> setEveINSSDesc = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCINSSDescSeg);
			Set<String> setEveSalFamTrab = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCINSSSalFam);
			Set<String> setEveSalarioMater = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCINSSSalMat);
			Set<String> setEveBCFGTS = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCINSSExcFGTS);
			Set<String> setEveReducaoBC = buscarCodigosDeEventosParaGeracaoDoManad(sfp8504Dto.abh20BCINSSReducao);

			for(TableMap tm : tmFba01011s) {
				def tp = tm.getInteger("fba0101tpVlr");
				def tipok300 = tp == 0 ? 1 : tp == 1 ? 4 : tp == 4 ? 6 : tp == 9 ? 7 : tp;

				def data = LocalDate.of(tm.getInteger("fba01ano"), tm.getInteger("fba01mes"), 1);
				def codEvek300 = tm.getString("abh21codigo");

				int tpEve = tm.getInteger("abh21tipo");
				def tipoEvek300 = tpEve == 0 ? "P" : tpEve == 1 ? "D" : "O";

				int indBCIRk300 = setEveBCIRSalarios.contains(codEvek300) ? 1 : setEveBCIR13.contains(codEvek300) ? 2 : 3;
				int indBCINSSk300 = setEveBCINSSSalarios.contains(codEvek300) ? 1 : 
					setEveBCINSS13.contains(codEvek300) ? 2 : 
						setEveINSSDesc.contains(codEvek300) ? 3 : 
							setEveSalFamTrab.contains(codEvek300) ? 4 : 
								setEveSalarioMater.contains(codEvek300) ? 5 : 
									setEveBCFGTS.contains(codEvek300) ? 6 : 
										setEveReducaoBC.contains(codEvek300) ? 7 : 8;

				txt.print("K300", 4);
				txt.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt.print(tipok300);
				txt.print(tm.getString("codAbb11"));
				txt.print(tm.getString("abh80codigo"));
				txt.print(data.format(DateTimeFormatter.ofPattern("MMyyyy")), 6);
				txt.print(codEvek300);
				txt.print(nb.format(tm.getBigDecimal("valorFba01011")));
				txt.print(tipoEvek300);
				txt.print(indBCIRk300);
				txt.print(indBCINSSk300);
				txt.newLine();
				linhasK300++;
			}
		}
	}
	
	/**
	 * TIPO K990
	 */
	private void gerarTipoK990(TextFile txt) {
		txt.print("K990", 4);
		txt.print(totalBlocoK);
		txt.newLine();
	}
	
	/**
	 * TIPO 9001
	 */
	private void gerarTipo9001(TextFile txt) {
		txt.print("9001", 4);
		txt.print(0);
		txt.newLine();
	}
	
	/**
	 * TIPO 9900
	 */
	private void gerarTipo9900(TextFile txt) {
		txt.print("9900", 4);
		txt.print("0000", 4);
		txt.print(linhas0000);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("0001", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("0050", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("0100", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("0990", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("I001", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("I050", 4);
		txt.print(linhasI050);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("I100", 4);
		txt.print(linhasI100);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("I990", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K001", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K050", 4);
		txt.print(linhasK050);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K100", 4);
		txt.print(linhasK100);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K150", 4);
		txt.print(linhasK150);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K200", 4);
		txt.print(linhasK200);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K250", 4);
		txt.print(linhasK250);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K300", 4);
		txt.print(linhasK300);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("K990", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("9001", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("9900", 4);
		txt.print(21);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("9990", 4);
		txt.print(1);
		txt.newLine();

		txt.print("9900", 4);
		txt.print("9999", 4);
		txt.print(1);
		txt.newLine();
	}
	
	/**
	 * TIPO 9990
	 */
	private void gerarTipo9990(TextFile txt) {
		txt.print("9990", 4);
		txt.print(24);
		txt.newLine();
	}
	
	/**
	 * TIPO 9999
	 */
	private void gerarTipo9999(TextFile txt, Integer totalLinhas) {
		txt.print("9999", 4);
		txt.print(totalLinhas);
	}
	
	private String ajustarCampo(Object string) {
		return string + "|";
	}
	
	private String ajustarCampo(Object string, int tamanho) {
		return StringUtils.ajustString(string, tamanho) + "|";
	}

	private String ajustarCampo(Object string, int tamanho, char character, boolean concatAEsquerda) {
		return StringUtils.ajustString(string, tamanho, character, concatAEsquerda) + "|";
	}
	
	private Aag02 buscarEstadoPorIdMunicipio(Long Aag0201id) {
		Long aag02id = getSession().createCriteria(Aag0201.class)
				.addFields("aag0201uf")
				.addWhere(Criterions.eq("Aag0201id", Aag0201id))
				.setMaxResults(1)
				.get(ColumnType.LONG);
		
		return getSession().createCriteria(Aag02.class)
				.addWhere(Criterions.eq("aag02id", aag02id))
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}
	
	private String buscarInscricaoEstadualPorEmpresa(Long aac10id, Long aag02id) {
		return getSession().createCriteria(Aac1002.class)
				.addFields("aac1002ie")
				.addWhere(Criterions.eq("aac1002empresa", aac10id))
				.addWhere(Criterions.eq("aac1002uf", aag02id))
				.setMaxResults(1)
				.get(ColumnType.STRING);
	}
	
	private List<TableMap> buscarDadosDePlanosDeContasParaGeracaoDoManad() {
		return getSession().createCriteria(Abc10.class)
				.addWhere(getSamWhere().getCritPadrao(Abc10.class))
				.setOrder("abc10codigo")
				.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDeDepartamentosParaGeracaoDoManad(Integer tamCodAbb11) {
		return getSession().createCriteria(Abb11.class)
				.addWhere(Criterions.eq("LENGTH(abb11codigo)", tamCodAbb11))
				.addWhere(getSamWhere().getCritPadrao(Abb11.class))
				.setOrder("abb11codigo")
				.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDeTrabalhadoresParaGeracaoDoManad(LocalDate dataInicial, LocalDate dataFinal) {
		return getSession().createCriteria(Fba0101.class)
				.addFields("abh80id, abh80codigo, abh80cpf, abh80sit, abh80dtResTrans, fba01id")
				.addJoin(Joins.join("fba0101calculo"))
				.addJoin(Joins.join("fba0101trab"))
				.addWhere(Criterions.between("fba0101dtCalc", dataInicial, dataFinal))
				.addWhere(getSamWhere().getCritPadrao(Fba01.class))
				.setOrder("abh80codigo")
				.getListTableMap();
	}
	
	private Fba02 buscarDadosDoHistoricoDoCalculo(Long abh80id, LocalDate fba02dtCalc) {
		return getSession().createCriteria(Fba02.class)
				.addFields("fba02id, aap14id, abh80id, fba02codigo, fba02nome, fba02pis, fba02dtNasc, fba02dtAdmis, aap14manad")
				.addJoin(Joins.part("fba02categ").partial(true).left(true))
				.addWhere(Criterions.eq("fba02dtCalc", fba02dtCalc))
				.addWhere(Criterions.eq("fba02trab", abh80id))
				.addWhere(getSamWhere().getCritPadrao(Fba02.class))
				.get(ColumnType.ENTITY);
	}
	
	private List<TableMap> buscarDadosDeLotacoesParaGeracaoDoManad() {
		return getSession().createCriteria(Abh02.class)
				.addFields("abh02codigo, abh02nome, abh02ni")
				.addWhere(getSamWhere().getCritPadrao(Abh02.class))
				.setOrder("abh02codigo")
				.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDeEventosParaGeracaoDoManad() {
		return getSession().createCriteria(Abh21.class)
				.addFields("abh21codigo, abh21nome")
				.addWhere(getSamWhere().getCritPadrao(Abh21.class))
				.setOrder("abh21codigo")
				.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDePlanilhasContabeisParaGeracaoDoManad() {
		return getSession().createQuery(
				"SELECT abb11codigo, abb11nome, abh21codigo, abh21nome, abh21tipo, abc10Deb.abc10codigo AS codDeb, abc10Cred.abc10codigo AS codCred ",
				"FROM Fba10 ",
				"INNER JOIN Abh21 ON abh21id = fba10eve ",
				"INNER JOIN Abb11 ON abb11id = fba10depto ",
				"INNER JOIN Abc20 ON abc20id = fba10lcp ",
				"LEFT JOIN Abc2001 ON abc2001lp = abc20id ",
				"LEFT JOIN Abc10 AS abc10Deb ON abc10Deb.abc10id = abc2001ctaDeb ",
				"LEFT JOIN Abc10 AS abc10Cred ON abc10Cred.abc10id = abc2001ctaCred ",
				getSamWhere().getWherePadrao("WHERE", Fba10.class),
				" GROUP BY abb11codigo, abb11nome, abh21codigo, abh21nome, abh21tipo, abc10Deb.abc10codigo, abc10Cred.abc10codigo")
				.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDeEventosDeValoresParaGeracaoDoManadK250(LocalDate dataInicial, LocalDate dataFinal, Integer tamCodAbb11) {
		return getSession().createQuery(
				"SELECT SUBSTR(abb11codigo, 1, ", tamCodAbb11, ") AS codAbb11, fba0101tpVlr, fba01ano, fba01mes, abh80id, abh80codigo ",
				"FROM Fba01011 ",
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
				"INNER JOIN Fba01 ON fba01id = fba0101calculo ",
				"INNER JOIN Abb11 ON abb11id = fba01011depto ",
				"INNER JOIN Abh80 ON abh80id = fba0101trab ",
				"WHERE fba0101dtCalc BETWEEN :dataInicial AND :dataFinal ",
				getSamWhere.getWherePadrao("AND", Fba01.class),
				" GROUP BY SUBSTR(abb11codigo, 1, ", tamCodAbb11, "), fba0101tpVlr, fba01ano, fba01mes, abh80id, abh80codigo ",
				" ORDER BY SUBSTR(abb11codigo, 1, ", tamCodAbb11, "), fba0101tpVlr, abh80codigo, fba01ano, fba01mes")
				.setParameter("dataInicial", dataInicial)
				.setParameter("dataFinal", dataFinal)
				.getListTableMap();
	}
	
	private Fba0101 buscarUltimoCalculoDoMesParaGeracaoDoManad(String abh80codigo, Integer fba01mes, Integer fba01ano, Integer fba0101tpVlr) {
		return getSession().createCriteria(Fba0101.class)
				.addFields("fba0101id, fba01id, abh80id, abh05id, fba0101dtPgto, abh05nome")
				.addJoin(Joins.join("fba0101calculo"))
				.addJoin(Joins.join("fba0101trab"))
				.addJoin(Joins.join("fba0101trab.abh80cargo"))
				.addWhere(Criterions.eq("abh80codigo", abh80codigo))
				.addWhere(Criterions.eq("fba01mes", fba01mes))
				.addWhere(Criterions.eq("fba01ano", fba01ano))
				.addWhere(Criterions.eq("fba0101tpVlr", fba0101tpVlr))
				.setMaxResults(1)
				.setOrder("fba0101dtCalc DESC, fba0101id DESC")
				.get(ColumnType.ENTITY);
	}
	
	private BigDecimal calcularCAE(Long abh80id, Integer fba01mes, Integer fba01ano, Integer fba0101tpVlr, String abh20codigo, String abb11codigo) {
		BigDecimal valor = BigDecimal.ZERO;

		List<TableMap> tms = buscarEventosDeValoresDoCAEParaGeracaoDoManad(abh80id, fba01mes, fba01ano, fba0101tpVlr, abh20codigo, abb11codigo);
		if(tms != null && tms.size() > 0) {
			for(int i = 0; i < tms.size(); i++) {
				TableMap tm = tms.get(i);

				if(tm.getInteger("abh2101cvr") == Abh2101.CVR_SOMA_VLR) {
					valor = valor + tm.getBigDecimal("fba01011valor");
				}else if(tm.getInteger("abh2101cvr") == Abh2101.CVR_DIMINUI_VLR) {
					valor = valor - tm.getBigDecimal("fba01011valor");
				}
			}
		}
		return valor;
	}
	
	private List<TableMap> buscarEventosDeValoresDoCAEParaGeracaoDoManad(Long abh80id, Integer fba01mes, Integer fba01ano, Integer fba0101tpVlr, String abh20codigo, String abb11codigo) {
		if(abh20codigo == null) return null;
		String whereAbh80 = abh80id != null ? "AND fba0101trab = :abh80id " : "";
		
		Query query = getSession().createQuery("SELECT abh2101cvr, fba01011valor ",
									           "FROM Fba01011 ",
									           "INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
									           "INNER JOIN Fba01 ON fba01id = fba0101calculo ",
									           "INNER JOIN Abh21 ON abh21id = fba01011eve ",
									           "INNER JOIN Abh2101 ON abh2101evento = abh21id ",
									           "INNER JOIN Abh20 ON abh20id = abh2101cae ",
									           "WHERE DATE_PART('MONTH', fba0101dtCalc) = :fba01mes ",
									           "AND DATE_PART('YEAR', fba0101dtCalc) = :fba01ano ",
									           "AND fba0101tpVlr = :fba0101tpVlr ",
									           "AND abh20codigo = :abh20codigo ", 
									           "AND abb11codigo LIKE :abb11codigo ",
									           whereAbh80);
		
		if(abh80id != null) query.setParameter("abh80id", abh80id);
		query.setParameter("fba01mes", fba01mes);
		query.setParameter("fba01ano", fba01ano);
		query.setParameter("fba0101tpVlr", fba0101tpVlr);
		query.setParameter("abh20codigo", abh20codigo);
		query.setParameter("abb11codigo", abb11codigo+"%");
		return query.getListTableMap();
	}
	
	private List<TableMap> buscarDadosDeEventosDeValoresParaGeracaoDoManadK300(LocalDate dataInicial, LocalDate dataFinal, Integer tamCodAbb11) {
		return getSession().createQuery(
				"SELECT SUBSTR(abb11codigo, 1, ", tamCodAbb11, ") AS codAbb11, fba0101tpVlr, fba01ano, fba01mes, abh80id, abh80codigo, abh21codigo, abh21tipo, SUM(fba01011valor) AS valorFba01011 ",
				"FROM Fba01011 ",
				"INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
				"INNER JOIN Fba01 ON fba01id = fba0101calculo ",
				"INNER JOIN Abh21 ON abh21id = fba01011eve ",
				"INNER JOIN Abb11 ON abb11id = fba01011depto ",
				"INNER JOIN Abh80 ON abh80id = fba0101trab ",
				"WHERE fba0101dtCalc BETWEEN :dataInicial AND :dataFinal ",
				getSamWhere().getWherePadrao("AND", Fba01.class),
				" GROUP BY SUBSTR(abb11codigo, 1, ", tamCodAbb11, "), fba0101tpVlr, fba01ano, fba01mes, abh80id, abh80codigo, abh21codigo, abh21tipo ",
				" ORDER BY SUBSTR(abb11codigo, 1, ", tamCodAbb11, "), fba0101tpVlr, abh80codigo, fba01ano, fba01mes, abh21codigo")
				.setParameter("dataInicial", dataInicial)
				.setParameter("dataFinal", dataFinal)
				.getListTableMap();
	}
	
	private Set<String> buscarCodigosDeEventosParaGeracaoDoManad(Abh20 abh20) {
		if(abh20 == null) return new HashSet<>();
		
		List<String> codigos = getSession().createCriteria(Abh2101.class)
				.addFields("abh21codigo")
				.addJoin(Joins.join("abh2101evento"))
				.addJoin(Joins.join("abh2101cae"))
				.addWhere(Criterions.eq("abh20codigo", abh20.abh20codigo))
				.addWhere(getSamWhere.getCritPadrao(Abh21.class))
				.setOrder("abh21codigo")
				.get(ColumnType.STRING);
		
		return new HashSet<>(codigos);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTUifQ==