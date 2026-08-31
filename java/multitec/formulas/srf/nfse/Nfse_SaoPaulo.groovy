package multitec.formulas.srf.nfse;

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFileEscrita
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.NFeUtils

public class Nfse_SaoPaulo extends FormulaBase {
	
	public final static String PATTERN_YYYYMMDD = "yyyyMMdd";

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.NFSE;
	}
	
	@Override
	public void executar() {
		Aac10 aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);
		List<Eaa01> eaa01s = get("eaa01s");
		TextFileEscrita txt = new TextFileEscrita();
		
		selecionarAlinhamento("0011");
		
		def contador = 0;
		def valorTotalServicosContidasNoArquivo = 0;
		def valorTotalDeducoesContidasNoArquivo = 0;
		
		/**
		 * HEADER do arquivo
		 **/
		txt.print(1);							 																										//tipo registro
		txt.print("002", 3);              																												//versão do arquivo
		txt.print(NFeUtils.formatarIE(aac10.getAac10im()), 8);																							//inscrição municipal do prestador
		txt.print(MDate.date().format(PATTERN_YYYYMMDD), 8);																							//data inicio
		txt.print(MDate.date().format(PATTERN_YYYYMMDD), 8);																							//data fim
		txt.newLine();
		
		/**
		 * DETALHE do arquivo
		 **/
		for(Eaa01 eaa01 : eaa01s) {
			Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
			
			txt.print(6);																																//tipo registro
			txt.print("RPS", 5);																														//tipo do rps
			txt.print(abb01.abb01serie != null ? abb01.abb01serie: "", 5, ' ', false);																	//série
			txt.print(abb01.abb01num, 12, '0', true); 																									//número
			txt.print(abb01.abb01data.format(PATTERN_YYYYMMDD), 8);																						//data emissão
			txt.print("T");																																//situação RPS T-Trib.em SP/F-Trib.Fora de SP

			Eaa0103 eaa0103 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0103", Criterions.eq("eaa0103doc", eaa01.getEaa01id()));
			TableMap eaa0103json = eaa0103.eaa0103json ?: new TableMap();

			valorTotalDeducoesContidasNoArquivo = valorTotalDeducoesContidasNoArquivo +	NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0", "ValorDeducoes")), 2, false);
			 
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0", "ValorServicos")), 2, false)), 15, '0', true);	//valor dos serviços
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0", "ValorDeducoes")), 2, false)), 15, '0', true);	//valor das deduções
			txt.print(eaa0103.getEaa0103codServ() != null ? StringUtils.extractNumbers(eaa0103.getEaa0103codServ().aaj05codigo): "0", 5, '0', true);	//código de serviço prestado
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa0103json.get(getCampo("0","Aliquota")), 2, false)), 4, '0', true);			//aliquota
			txt.print(2);																																//ISS Retido
			
			Eaa0102 eaa0102 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0102", Criterions.eq("eaa0102doc", eaa01.getEaa01id()));
			
			txt.print(eaa0102.eaa0102ti == 0 ? 2 : eaa0102.eaa0102ti == 1 ? 1 : 3);																		//indicador cpf-cnpj
			txt.print(StringUtils.extractNumbers(eaa0102.eaa0102ni), 14, '0', true);																	//CPF ou CNPJ do tomador
			txt.print(StringUtils.extractNumbers(eaa0102.eaa0102im), 8, '0', true);																		//inscrição municipal do tomador
			txt.print(StringUtils.extractNumbers(eaa0102.eaa0102ie), 12, '0', true);																	//inscrição estadual do tomador
			txt.print(eaa0102.eaa0102nome, 75);																											//nome-razao social do tomador
			
			Eaa0101 eaa0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", eaa01.getEaa01id()), Criterions.eq("eaa0101principal", 1));
			Aag02 uf = eaa0101.eaa0101municipio == null ? null : getSession().get(Aag02.class, eaa0101.eaa0101municipio.aag0201uf.aag02id);
			txt.print(eaa0101.eaa0101tpLog, 3);	 																										//tipo do endereço
			txt.print(eaa0101.eaa0101endereco, 50);																										//endereço tomador
			txt.print(eaa0101.eaa0101numero, 10);																										//número endereço tomador
			txt.print(eaa0101.eaa0101complem, 30);																										//complemento do endereço tomador
			txt.print(eaa0101.eaa0101bairro, 30);																										//bairro do tomador
			txt.print(eaa0101.getEaa0101municipio().aag0201nome, 50);																					//Cidade do tomador
			txt.print(uf.aag02uf, 2);																													//UF do tomador
			txt.print(eaa0101.eaa0101cep, 8, '0', true);																								//cep tomador
			txt.print(eaa0101.eaa0101eMail, 75); 																										//eMail tomador
			
			TableMap eaa01json = eaa01.eaa01json ?: new TableMap();
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("0","ValorPis")), 2, false)), 15, '0', true);					//PIS-PASEP
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("0","ValorCofins")), 2, false)), 15, '0', true);				//COFINS
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("0","ValorInss")), 2, false)), 15, '0', true);					//INSS
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("0","ValorIr")), 2, false)), 15, '0', true);					//IR
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("0","ValorCssl")), 2, false)), 15, '0', true);					//CSSL
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("0","ValorCargaTributaria")), 2, false)), 15, '0', true);		//Carga tributaria: Valor
			txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(eaa01json.getBigDecimal_Zero(getCampo("0","PorcentagemCargaTributaria")), 2, false)), 5, '0', true);	//Carga tributaria: Porcentagem
			txt.print(eaa01json.get(getCampo("0","FonteCargaTributaria")), 10, ' ', false);																						//Carga tributaria: fonte

			txt.print(StringUtils.space(12));																										//CEI
			txt.print(StringUtils.space(12));																										//Matricula da Obra
			txt.print(StringUtils.space(7)); 																										//Municipio prestação - cod IBGE
			txt.print(StringUtils.space(10));																										//numero de encapsulamento
			txt.print(StringUtils.space(10));																										//campo reservado
			txt.print(StringUtils.space(15));	 																									//Valor total recebido
			txt.print(StringUtils.space(175));																										//reservado
			txt.print(eaa0103.eaa0103descr != null ? eaa0103.eaa0103descr: "", 120, ' ', true);
			
			valorTotalServicosContidasNoArquivo = valorTotalServicosContidasNoArquivo + eaa01.getEaa01totItens();
																																
			txt.newLine();
			contador++;
		}
		
		/**
		 * RODAPÉ do arquivo
		 **/
		txt.print(9);																																//tipo do registro
		txt.print(contador, 7); 																													//numero de linhas no detalhe do arquivo
		txt.print(StringUtils.extractNumbers(NFeUtils.formatarDecimal(valorTotalServicosContidasNoArquivo, 2, false)), 15, (char) '0', true);		//valor total dos serviços contidos no arquivo
		txt.print(StringUtils.extractNumbers(valorTotalDeducoesContidasNoArquivo), 15, '0', true);													//valor total das deduções contidas no arquivo
		txt.newLine();
		
		put("dados", txt.getTexto());
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzIifQ==