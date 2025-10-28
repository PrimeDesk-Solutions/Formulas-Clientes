package multitec.formulas.sfp;

import java.time.format.DateTimeFormatter;

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.criteria.client.ClientCriterion;
import sam.core.criteria.ClientCriteriaConvert;
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFP8506Dto;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac1001;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.formula.FormulaBase;

public class NIS  extends FormulaBase {
	private SFP8506Dto sfp8506Dto;
	
	private int ordenacao = 1;
	private int seqLog = 0;
	private int totalLote = 1;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.NIS;
	}
	
	@Override
	public void executar() {
		this.sfp8506Dto = get("sfp8506Dto");
		
		TextFile txt = new TextFile();
		
		DateTimeFormatter sdfData = DateTimeFormatter.ofPattern("ddMMyyyy");
		DateTimeFormatter sdfNIS = DateTimeFormatter.ofPattern("yyMMdd");
		
		//**************************************************************************************************************************************************************************************
		//******************************************************************************* NIS SIMPLIFICADO *************************************************************************************
		//**************************************************************************************************************************************************************************************
		if(sfp8506Dto.isSimplificado()) {
			Aac10 aac10 = getVariaveis().aac10;
			
			//Header Geral
			criarLinha(txt, "00", "0900", "00", "C", true);																												//Tipo de arquivo	
			criarLinha(txt, "00", "0829", "00", StringUtils.extractNumbers(aac10.aac10ni), true);																	//CNPJ
			criarLinha(txt, "00", "0313", "00", aac10.aac10fantasia, true);																							//Nome fantasia
			criarLinha(txt, "00", "0413", "00", sfp8506Dto.original ? "O" : "R", true);																				//Tipo de remessa
			criarLinha(txt, "00", "0903", "00", sdfData.format(sfp8506Dto.dataRemessa), true);																		//Data da remessa
			criarLinha(txt, "00", "0913", "00", "0013", true); 																											//Código do processo
			
			List<Abh80> abh80s = buscarDadosAbh80sPeloNIS(aac10.aac10id, sfp8506Dto.tiposDeTrabalhadores);
			if(abh80s != null && abh80s.size() > 0) {
				for(int i = 0; i < abh80s.size(); i++) {
					Abh80 abh80 = abh80s.get(i);
					seqLog = 0;
					def codUFMun = abh80.abh80municipio != null ? abh80.abh80municipio.aag0201ibge : "";
					def gi = abh80.abh80gi != null ? abh80.abh80gi.aap06nis : "";
					def rc = abh80.abh80rc != null ? abh80.abh80rc.aap07nis : "";
					def estCivil = abh80.abh80estCivil != null ? abh80.abh80estCivil.aap08nis : "";
					def nac = abh80.abh80nascPais != null ? ajustarTamanho(abh80.abh80nascPais.aag01nis, 4, '0', true) : null;
					def nacTipo = abh80.abh80tipoNac < 3 ? abh80.abh80tipoNac +1 : 0;
					def paisOrigem = abh80.abh80paisOrigem != null ? ajustarTamanho(abh80.abh80paisOrigem.aag01nis, 4, '0', true) : null;
					def teNum = abh80.abh80teNum != null ? StringUtils.extractNumbers(abh80.abh80teNum) : null;
					def rgNum = abh80.abh80rgNum != null ? StringUtils.extractNumbers(abh80.abh80rgNum) : null;
					def rgComp = abh80.abh80rgComplem != null ? StringUtils.extractNumbers(abh80.abh80rgComplem) : " ";
					def dtEmisRG = abh80.abh80rgDtExped != null ? sdfData.format(abh80.abh80rgDtExped) : "";
					def dtEmisCTPS = abh80.abh80ctpsDtEmis != null ? sdfData.format(abh80.abh80ctpsDtEmis) : "";
					def dtChegada = abh80.abh80dtChegBr != null ? sdfData.format(abh80.abh80dtChegBr) : "";
					def dtNatura = abh80.abh80dtNatu != null ? sdfData.format(abh80.abh80dtNatu) : "";
					def ddd = abh80.abh80ddd1 != null ? ("0" + StringUtils.extractNumbers(abh80.abh80ddd1)) : null;
					def cp =  abh80.abh80cp != null && abh80.abh80cp > 0 ? ajustarTamanho(abh80.abh80cp, 15, '0', true) : null;
					def tpVinculo = "59";
					def ni = StringUtils.extractNumbers(aac10.aac10ni);
					
					//Registros detalhe
					criarLinha(txt, "02", "0902", "00", "I", true);																										//Tipo de operação
					criarLinha(txt, "02", sfp8506Dto.cpf ? "0419" : "0418", "00", sfp8506Dto.niResp, true);														//PIS ou CPF do responsável
					criarLinha(txt, "02", "0422", "00", abh80.abh80id, true);	 																					//Controle da empresa - id do trabalhador
					criarLinha(txt, "02", "0195", "00", abh80.abh80nome, true);	 																					//Nome do trabalhador
					criarLinha(txt, "02", "0197", "00", sdfData.format(abh80.abh80nascData), true);		 															//Data de nascimento
					criarLinha(txt, "02", "0200", "00", abh80.abh80mae, true);	 																					//Nome da mãe
					criarLinha(txt, "02", "0199", "00", abh80.abh80pai, true);  																					//Nome do pai
					criarLinha(txt, "02", "0390", "00", codUFMun, true);																									//Código UF/Município
					criarLinha(txt, "02", "0201", "00", abh80.abh80sexo == 0 ? "M" : "F", true);	 																//Sexo
					criarLinha(txt, "02", "0206", "00", rc != null && rc.length() == 1 ? "0" + rc : rc, true); 															//Raça/Cor
					criarLinha(txt, "02", "0008", "00", gi != null && gi.length() == 1 ? "0" + gi : gi, true); 															//Grau de instrução
					criarLinha(txt, "02", "0389", "00", estCivil, true);																									//Estado civil
					criarLinha(txt, "02", "0386", "00", nac, true);																										//Nacionalidade
					if(nac != null) criarLinha(txt, "02", "0386", "01", nacTipo+"", true); 																				//Nacionalidade - detalhamento
					criarLinha(txt, "02", "0387", "00", paisOrigem, true);																								//País origem
					criarLinha(txt, "02", "0370", "00", StringUtils.extractNumbers(abh80.abh80cpf), true);		 													//Cpf
					if(teNum != null) {
						criarLinha(txt, "02", "0371", "00", teNum, true); 																								//Título eleitoral - número e dv
						criarLinha(txt, "02", "0371", "01", StringUtils.extractNumbers(abh80.abh80teZona), true); 													//Título eleitoral - zona
						criarLinha(txt, "02", "0371", "02", StringUtils.extractNumbers(abh80.abh80teSecao), true); 													//Título eleitoral - seção
					}
					if(rgNum != null) {
						criarLinha(txt, "02", "0372", "00", rgNum, true); 																								//RG - número
						criarLinha(txt, "02", "0372", "01", rgComp, true); 																								//RG - complemento
						criarLinha(txt, "02", "0372", "02", abh80.abh80rgEe, true); 																				//RG - UF
						criarLinha(txt, "02", "0372", "03", dtEmisRG, true); 																							//RG - Data de emissão
						criarLinha(txt, "02", "0372", "04", abh80.abh80rgOe, true); 																				//RG - Orgão expedidor
					}
					criarLinha(txt, "02", "0373", "00", StringUtils.extractNumbers(abh80.abh80ctpsNum), true);		 												//CTPS - número
					criarLinha(txt, "02", "0373", "01", StringUtils.extractNumbers(abh80.abh80ctpsSerie), true);		 											//CTPS - série
					criarLinha(txt, "02", "0373", "02", abh80.abh80ctpsEe, true); 																					//CTPS - UF
					criarLinha(txt, "02", "0373", "03", dtEmisCTPS, true); 																								//CTPS - Data de emissão
					//Dados de certidão (nascimento, casamento, óbito, índio) e passaporte não serão enviados.
					if(nac != null && nacTipo == 0) criarLinha(txt, "02", "0391", "00", dtChegada, true);																//Data da chegada
					if(nac != null && nacTipo == 2) criarLinha(txt, "02", "0401", "00", StringUtils.extractNumbers(abh80.abh80portNatu), true);						//Portaria da naturalização
					if(nac != null && nacTipo == 2) criarLinha(txt, "02", "0815", "00", dtNatura, true);																	//Data da naturalização
					criarLinha(txt, "02", "0809", "00", ddd, true);																										//DDD
					criarLinha(txt, "02", "0809", "01", ddd != null ? StringUtils.extractNumbers(abh80.abh80fone1) : null, true);									//Telefone
					criarLinha(txt, "02", "0809", "02", ddd != null ? (1+"") : null, true);																				//Telefone - tipo (1 fixo)
					criarLinha(txt, "02", "0810", "00", abh80.abh80eMail, true);  																					//Email
					criarLinha(txt, "02", "0911", "00", abh80.abh80cep, true);  																					//Cep
					criarLinha(txt, "02", "0911", "01", "1", true);  																									//Tipo de endereço
					criarLinha(txt, "02", "0911", "02", abh80.abh80tpLog != null ? abh80.abh80tpLog.aap15nis : "", true); 								//Tipo de logradouro
					criarLinha(txt, "02", "0911", "03", abh80.abh80endereco, true);  																				//Logradouro
					criarLinha(txt, "02", "0911", "04", "Num", true);  																									//Sigla da posição determinante (Num fixo)
					criarLinha(txt, "02", "0911", "05", abh80.abh80numero, true);  																					//Posição determinante
					criarLinha(txt, "02", "0911", "06", abh80.abh80complem != null ? abh80.abh80complem : " ", true);  										//Complemento do endereço
					criarLinha(txt, "02", "0911", "07", abh80.abh80bairro, true);	 																				//Bairro
					criarLinha(txt, "02", "0911", "08", codUFMun, true);																									//Código UF/Município
					criarLinha(txt, "02", "0938", "00", cp, true);  																										//Caixa postal
					if(cp != null) criarLinha(txt, "02", "0938", "01", abh80.abh80cep, true);  																		//Caixa postal - Cep
					criarLinha(txt, "02", "0292", "00", tpVinculo, true); 																								//Tipo de vínculo
					criarLinha(txt, "02", "0292", "01",  ni, true);																										//CNPJ/CEI
					criarLinha(txt, "02", "0292", "02", sdfData.format(abh80.abh80dtAdmis), true);   																//Data de admissão
				}
			}
			
			//Trailer Geral
			seqLog = 0;
			criarLinha(txt, "99", "0908", "00", "CADASTRONIS.D" + sdfNIS.format(sfp8506Dto.dataRemessa) + ".S01", true);											//Nome do arquivo
			criarLinha(txt, "99", "0912", "00", ajustarTamanho(ordenacao, 9, '0', true), false); 																		//Total de registros

		//**************************************************************************************************************************************************************************************
		//********************************************************************************* NIS COMPLETO ***************************************************************************************
		//**************************************************************************************************************************************************************************************
		}else {
			Aac10 aac10 = getVariaveis().aac10;
			
			//Header Geral
			criarLinha(txt, "00", "0900", "00", "C", true);																												//Tipo de arquivo	
			criarLinha(txt, "00", "0829", "00", StringUtils.extractNumbers(aac10.aac10ni), true);																	//CNPJ
			criarLinha(txt, "00", "0313", "00", aac10.aac10fantasia, true);																							//Nome fantasia
			criarLinha(txt, "00", "0413", "00", sfp8506Dto.original ? "O" : "R", true);																				//Tipo de remessa
			criarLinha(txt, "00", "0903", "00", sdfData.format(sfp8506Dto.dataRemessa), true);																		//Data da remessa
			criarLinha(txt, "00", "0913", "00", "0003", true); 																											//Código do processo
			
			def index = 1;
			List<Aac10> aac10s = buscarEmpresasByNIS(sfp8506Dto.getCritAac10());
			if(aac10s != null && aac10s.size() > 0) {
				for(Aac10 aac10Emp : aac10s) {
					//Header Parcial
					seqLog = 0;
					criarLinha(txt, "01", "0378", "00", StringUtils.extractNumbers(aac10Emp.aac10ni), true);  														//CNPJ da empresa
					criarLinha(txt, "01", "0379", "00", "", true); 														//CEI da empresa
					criarLinha(txt, "01", "0104", "00", aac10Emp.aac10fantasia, true); 																				//Nome da empresa
					
					List<Abh80> abh80s = buscarDadosAbh80sPeloNIS(aac10Emp.aac10id, sfp8506Dto.tiposDeTrabalhadores);
					if(abh80s != null && abh80s.size() > 0) {
						for(int i = 0; i < abh80s.size(); i++) {
							Abh80 abh80 = abh80s.get(i);
							seqLog = 0;
							def codUFMun = abh80.abh80municipio != null ? abh80.abh80municipio.aag0201ibge : "";
							def gi = abh80.abh80gi != null ? abh80.abh80gi.aap06nis : "";
							def rc = abh80.abh80rc != null ? abh80.abh80rc.aap07nis : "";
							def estCivil = abh80.abh80estCivil != null ? abh80.abh80estCivil.aap08nis : "";
							def nac = abh80.abh80nascPais != null ? ajustarTamanho(abh80.abh80nascPais.aag01nis, 4, '0', true) : null;
							def nacTipo = abh80.abh80tipoNac < 3 ? abh80.abh80tipoNac +1 : 0;
							def paisOrigem = abh80.abh80paisOrigem != null ? ajustarTamanho(abh80.abh80paisOrigem.aag01nis, 4, '0', true) : null;
							def teNum = abh80.abh80teNum != null ? StringUtils.extractNumbers(abh80.abh80teNum) : null;
							def rgNum = abh80.abh80rgNum != null ? StringUtils.extractNumbers(abh80.abh80rgNum) : null;
							def rgComp = abh80.abh80rgComplem != null ? StringUtils.extractNumbers(abh80.abh80rgComplem) : " ";
							def dtEmisRG = abh80.abh80rgDtExped != null ? sdfData.format(abh80.abh80rgDtExped) : "";
							def dtEmisCTPS = abh80.abh80ctpsDtEmis != null ? sdfData.format(abh80.abh80ctpsDtEmis) : "";
							def dtChegada = abh80.abh80dtChegBr != null ? sdfData.format(abh80.abh80dtChegBr) : "";
							def dtNatura = abh80.abh80dtNatu != null ? sdfData.format(abh80.abh80dtNatu) : "";
							def ddd = abh80.abh80ddd1 != null ? ("0" + StringUtils.extractNumbers(abh80.abh80ddd1())) : null;
							def cp = abh80.abh80cp > 0 ? ajustarTamanho(abh80.abh80cp, 15, '0', true) : null;
							def tpVinculo = "59";
							def ni = StringUtils.extractNumbers(aac10Emp.aac10ni);
							
							//Registros detalhe
							criarLinha(txt, "02", "0902", "00", "I", true);																								//Tipo de operação
							criarLinha(txt, "02", sfp8506Dto.cpf ? "0419" : "0418", "00", sfp8506Dto.niResp, true);												//PIS ou CPF do responsável
							criarLinha(txt, "02", "0422", "00", abh80.abh80id, true);	 																			//Controle da empresa - id do trabalhador
							criarLinha(txt, "02", "0195", "00", abh80.abh80nome, true);	 																			//Nome do trabalhador
							criarLinha(txt, "02", "0197", "00", sdfData.format(abh80.abh80nascData), true);		 													//Data de nascimento
							criarLinha(txt, "02", "0200", "00", abh80.abh80mae, true);	 																			//Nome da mãe
							criarLinha(txt, "02", "0199", "00", abh80.abh80pai, true);  																			//Nome do pai
							criarLinha(txt, "02", "0390", "00", codUFMun, true);																							//Código UF/Município
							criarLinha(txt, "02", "0201", "00", abh80.abh80sexo == 0 ? "M" : "F", true);	 														//Sexo
							criarLinha(txt, "02", "0206", "00", rc != null && rc.length() == 1 ? "0" + rc : rc, true); 													//Raça/Cor
							criarLinha(txt, "02", "0008", "00", gi != null && gi.length() == 1 ? "0" + gi : gi, true); 													//Grau de instrução
							criarLinha(txt, "02", "0389", "00", estCivil, true);																							//Estado civil
							criarLinha(txt, "02", "0386", "00", nac, true);																								//Nacionalidade
							if(nac != null) criarLinha(txt, "02", "0386", "01", nacTipo+"", true); 																		//Nacionalidade - detalhamento
							criarLinha(txt, "02", "0387", "00", paisOrigem, true);																						//País origem
							criarLinha(txt, "02", "0370", "00", StringUtils.extractNumbers(abh80.abh80cpf), true);		 											//Cpf
							if(teNum != null) {
								criarLinha(txt, "02", "0371", "00", teNum, true); 																						//Título eleitoral - número e dv
								criarLinha(txt, "02", "0371", "01", StringUtils.extractNumbers(abh80.abh80teZona), true); 											//Título eleitoral - zona
								criarLinha(txt, "02", "0371", "02", StringUtils.extractNumbers(abh80.abh80teSecao), true); 											//Título eleitoral - seção
							}
							if(rgNum != null) {
								criarLinha(txt, "02", "0372", "00", rgNum, true); 																						//RG - número
								criarLinha(txt, "02", "0372", "01", rgComp, true); 																						//RG - complemento
								criarLinha(txt, "02", "0372", "02", abh80.abh80rgEe, true); 																		//RG - UF
								criarLinha(txt, "02", "0372", "03", dtEmisRG, true); 																					//RG - Data de emissão
								criarLinha(txt, "02", "0372", "04", abh80.abh80rgOe, true); 																		//RG - Orgão expedidor
							}
							criarLinha(txt, "02", "0373", "00", StringUtils.extractNumbers(abh80.abh80ctpsNum), true);		 										//CTPS - número
							criarLinha(txt, "02", "0373", "01", StringUtils.extractNumbers(abh80.abh80ctpsSerie), true);		 									//CTPS - série
							criarLinha(txt, "02", "0373", "02", abh80.abh80ctpsEe, true); 																			//CTPS - UF
							criarLinha(txt, "02", "0373", "03", dtEmisCTPS, true); 																						//CTPS - Data de emissão
							//Dados de certidão (nascimento, casamento, óbito, índio) e passaporte não serão enviados.
							if(nac != null && nacTipo == 0) criarLinha(txt, "02", "0391", "00", dtChegada, true);														//Data da chegada
							if(nac != null && nacTipo == 2) criarLinha(txt, "02", "0401", "00", StringUtils.extractNumbers(abh80.abh80portNatu), true);				//Portaria da naturalização
							if(nac != null && nacTipo == 2) criarLinha(txt, "02", "0815", "00", dtNatura, true);															//Data da naturalização
							criarLinha(txt, "02", "0809", "00", ddd, true);																								//DDD
							criarLinha(txt, "02", "0809", "01", ddd != null ? StringUtils.extractNumbers(abh80.abh80fone1) : null, true);							//Telefone
							criarLinha(txt, "02", "0809", "02", ddd != null ? (1+"") : null, true);																		//Telefone - tipo (1 fixo)
							criarLinha(txt, "02", "0810", "00", abh80.abh80eMail, true);  																			//Email
							criarLinha(txt, "02", "0911", "00", abh80.abh80cep, true);  																			//Cep
							criarLinha(txt, "02", "0911", "01", "1", true);  																							//Tipo de endereço
							criarLinha(txt, "02", "0911", "02", abh80.abh80tpLog != null ? abh80.abh80tpLog.aap15nis : "", true); 						//Tipo de logradouro
							criarLinha(txt, "02", "0911", "03", abh80.abh80endereco, true);  																		//Logradouro
							criarLinha(txt, "02", "0911", "04", "Num", true);  																							//Sigla da posição determinante (Num fixo)
							criarLinha(txt, "02", "0911", "05", abh80.abh80numero, true);  																			//Posição determinante
							criarLinha(txt, "02", "0911", "06", abh80.abh80complem != null ? abh80.abh80complem : " ", true);  								//Complemento do endereço
							criarLinha(txt, "02", "0911", "07", abh80.abh80bairro, true);	 																		//Bairro
							criarLinha(txt, "02", "0911", "08", codUFMun, true);																							//Código UF/Município
							criarLinha(txt, "02", "0938", "00", cp, true);  																								//Caixa postal
							if(cp != null) criarLinha(txt, "02", "0938", "01", abh80.abh80cep, true);  																//Caixa postal - Cep
							criarLinha(txt, "02", "0292", "00", tpVinculo, true); 																						//Tipo de vínculo
							criarLinha(txt, "02", "0292", "01",  ni, true);																								//CNPJ/CEI
							criarLinha(txt, "02", "0292", "02", sdfData.format(abh80.abh80dtAdmis), true);   														//Data de admissão
						}
					}
					
					//Trailer Parcial
					seqLog = 0;
					criarLinha(txt, "98", "0378", "00", StringUtils.extractNumbers(aac10Emp.aac10ni), true);  														//CNPJ da empresa
					criarLinha(txt, "98", "0379", "00", "", true); 														//CEI da empresa
					criarLinha(txt, "98", "0912", "00", ajustarTamanho(totalLote, 9, '0', true), true); 																	//Total de registros
					if(index == aac10s.size()) criarLinha(txt, "98", "0420", "00", ajustarTamanho(aac10s, 9, '0', true), true);  										//Total de empresas
					index++;
					totalLote = 1;
				}
			}
			
			//Trailer Geral
			seqLog = 0;
			criarLinha(txt, "99", "0908", "00", "CADASTRONIS.D" + sdfNIS.format(sfp8506Dto.dataRemessa) + ".S01", true);											//Nome do arquivo
			criarLinha(txt, "99", "0912", "00", ajustarTamanho(ordenacao, 9, '0', true), false); 																		//Total de registros
		}
		
		put("txt", txt);
	}
	
	private List<Aac10> buscarEmpresasByNIS(ClientCriterion critAac10) {
		return getSession().createCriteria(Aac10.class).addWhere(ClientCriteriaConvert.convertCriterion(critAac10)).setOrder("aac10codigo").getList(ColumnType.ENTITY);
	}

	private List<Abh80> buscarDadosAbh80sPeloNIS(Long aac10id, Set<Integer> tiposDeTrabalhadores) {
		Long abh80gc = getSession().createCriteria(Aac1001.class)
		.addFields("aac1001gc")
		.addWhere(Criterions.eq("aac1001empresa", aac10id))
		.addWhere(Criterions.eq("UPPER(aac1001tabela)", "ABH80"))
		.get(ColumnType.LONG);
		
		return getSession().createCriteria(Abh80.class)
		.addJoin(Joins.fetch("abh80municipio"))
		.addJoin(Joins.fetch("abh80rc"))
		.addJoin(Joins.fetch("abh80gi"))
		.addJoin(Joins.fetch("abh80estCivil"))
		.addJoin(Joins.fetch("abh80nascPais").alias("aag01N"))
		.addJoin(Joins.fetch("abh80paisOrigem").alias("aag01O"))
		.addJoin(Joins.fetch("abh80tpLog"))
		.addWhere(Criterions.in("abh80tipo", tiposDeTrabalhadores))
		.addWhere(Criterions.isNull("abh80pis"))
		.addWhere(Criterions.eq("abh80gc", abh80gc))
		.getList(ColumnType.ENTITY);
	}

	private void criarLinha(TextFile txt, String tipoReg, String codCampo, String seqCampo, Object contCampo, boolean quebraLinha) {
		if(contCampo != null) {
			txt.print(ordenacao, 11, '0', true);																										//001 a 011
			txt.print(1, 18, '0', true);																												//012 a 029
			txt.print(tipoReg.equals("98") || tipoReg.equals("99") ? "99999999999" : "00000000000", 11);												//030 a 040
			txt.print(tipoReg, 2);																														//041 a 042
			txt.print(seqLog, 3, '0', true);																											//043 a 045
			txt.print(codCampo, 4);																														//046 a 049
			txt.print(seqCampo, 2);																														//050 a 051
			txt.print("00", 2);																															//052 a 053
			txt.print(contCampo, 180);																													//054 a 233
			txt.print(StringUtils.space(32));	 																														//234 a 265
			txt.print(0, 11, '0', true);																												//266 a 276
			txt.print(0, 4, '0', true);																													//277 a 280
			if(quebraLinha) txt.newLine();
			ordenacao++;
			seqLog++;
			if(!tipoReg.equals("00") && !tipoReg.equals("99")) totalLote++;
		}
	}

	private String ajustarTamanho(Object string, int tamanho) {
		return StringUtils.ajustString(string, tamanho);
	}

	private String ajustarTamanho(Object string, int tamanho, char character, boolean concatAEsquerda) {
		return StringUtils.ajustString(string, tamanho, character, concatAEsquerda);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTcifQ==