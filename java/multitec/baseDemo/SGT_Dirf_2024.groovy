package multitec.baseDemo

import java.text.Collator
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abh21
import sam.model.entities.ab.Abh2101
import sam.model.entities.ab.Abh80
import sam.model.entities.ab.Abh8002
import sam.model.entities.ab.Abh80021
import sam.model.entities.ed.Edd40
import sam.model.entities.ed.Edd4001
import sam.model.entities.fb.Fba01
import sam.model.entities.fb.Fba0101
import sam.model.entities.fb.Fba01011
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.ESocialUtils
import sam.server.samdev.utils.Parametro

class SGT_Dirf_2024 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_DIRF;
	}

	@Override
	public void executar() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss a");
		println("**** - inicio: " + LocalTime.now().format(formatter) + " - *****")
		Integer referencia = getInteger("referencia")
		Integer calendario = getInteger("calendario")
		def tipo = get("tipo")
		def recibo = get("recibo")
		
		TextFile txt = new TextFile("|", false, true);
		
		Aac10 empresaMatriz = buscarEmpresaMatrix();
		TableMap aac10json = empresaMatriz.aac10json != null ? empresaMatriz.aac10json : new TableMap()
		Long idGrupoCentralizador = buscarIdGruposCentralizador(obterEmpresaAtiva())
		
		//campos com conteudos fixos
		String planoSaude           = "S"																					//Empressa possui plano de saude coletivo
		String socioOstensivo 		= "N" 																					//indicador de socio ostensivo responsavel por sociedade em conta de participação
		String depositario 			= "N" 																					//indicade de depositario de crédito decorrente de decisão judicial
		String clubeInvestimento 	= "N" 																					//declarante de instituição administradora de fundos ou clube de investimento
		String domiciliadoExterior 	= "N" 																					//declarante residente no exterior
		String recebeRecursos 		= "N" 																					//Recebe recursos do tesouro nacional
		String funcaoPublica 		= "N" 																					//Empresa é fundação publica
		String situacaoEspecial 	= "N" 																					//Declaração de situação especial
		LocalDate dataEvento 		= null 																					//Data do evento, preencher se "situacaoEspecial" for igual a "S"
		Integer natDeclarante       = 0																						//Natureza do declarante
		
//DIRF - Declaração de imposto de renda retido na fonte
/*Ordem 01*/		txt.print("Dirf");
/*Ordem 02*/		txt.print(referencia);
/*Ordem 03*/		txt.print(calendario);
/*Ordem 04*/		txt.print(tipo);
/*Ordem 05*/		txt.print(recibo == null ? null : StringUtils.ajustString(recibo, 12, '0', true));
/*Ordem 06*/		txt.print("B3VH8RQ");
		txt.newLine();
		
//RESPO - Responsável pelo preenchimento
/*Ordem 01*/		txt.print("RESPO");
/*Ordem 02*/		txt.print(StringUtils.extractNumbers(empresaMatriz.aac10rCpf));
/*Ordem 03*/		txt.print(empresaMatriz.aac10rNome);
/*Ordem 04*/		txt.print(empresaMatriz.aac10rDddFone);
/*Ordem 05*/		txt.print(empresaMatriz.aac10rFone);
/*Ordem 06*/		txt.print(null);
/*Ordem 07*/		txt.print(null);
/*Ordem 08*/		txt.print(empresaMatriz.aac10rEmail);
		txt.newLine();
		
//DECPJ - Declarante pessoa juridica
/*Ordem 01*/		txt.print("DECPJ")
/*Ordem 02*/		txt.print(StringUtils.extractNumbers(empresaMatriz.aac10ni))
/*Ordem 03*/		txt.print(empresaMatriz.aac10rs)
/*Ordem 04*/		txt.print(natDeclarante)
/*Ordem 05*/		txt.print(StringUtils.extractNumbers(empresaMatriz.aac10rCpf))
/*Ordem 06*/		txt.print(socioOstensivo)
/*Ordem 07*/		txt.print(depositario)
/*Ordem 08*/		txt.print(clubeInvestimento)
/*Ordem 09*/		txt.print(domiciliadoExterior)
/*Ordem 10*/		txt.print(planoSaude)
/*Ordem 11*/		txt.print(recebeRecursos)
/*Ordem 12*/		txt.print(funcaoPublica)
/*Ordem 13*/		txt.print(situacaoEspecial)
/*Ordem 14*/		txt.print(dataEvento != null ? dataEvento.format("ddMMyyyy") : null)
		txt.newLine();
		
		List<String> idrecs = buscarIDREC(calendario)
		
		for(idrec in idrecs) {
//IDREC - Identificação do código de receita
/*Ordem 01*/		txt.print("IDREC")
/*Ordem 02*/		txt.print(idrec, 4, '0', true)
			txt.newLine()
			
			
			List<Abh80> abh80s = buscarTrabalhadoresDosValores(calendario, idrec);
			
			for(abh80 in abh80s) {
				String cae13 = "2101"
				String caeMes = "2001"
				Set<Integer> tipos = Set.of()
				if(idrec.equals("3562")) {
					cae13 = null
					caeMes = "2007"
					tipos = Set.of(6)
				}
				HashMap<Integer, BigDecimal> valoresRTRT = buscarValorCAE(idGrupoCentralizador, abh80.abh80id, caeMes, cae13, calendario, tipos)
				if(idrec.equals("3562") && valoresRTRT == null) {
					continue
				}
//BPJDEC – Beneficiário pessoa jurídica do declarante
/*Ordem 01*/  			txt.print("BPFDEC")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(abh80.abh80cpf))
/*Ordem 03*/  			txt.print(abh80.abh80nome)
/*Ordem 04*/  			txt.print(null)
/*Ordem 05*/  			txt.print("N")
/*Ordem 06*/  			txt.print("N")
				txt.newLine()
				
				if(valoresRTRT != null) {
/*Ordem 01*/  		txt.print("RTRT")
/*Ordem 02*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(1)?.toString()))
/*Ordem 03*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(2)?.toString()))
/*Ordem 04*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(3)?.toString()))
/*Ordem 05*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(4)?.toString()))
/*Ordem 06*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(5)?.toString()))
/*Ordem 07*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(6)?.toString()))
/*Ordem 08*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(7)?.toString()))
/*Ordem 09*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(8)?.toString()))
/*Ordem 10*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(9)?.toString()))
/*Ordem 11*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(10)?.toString()))
/*Ordem 12*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(11)?.toString()))
/*Ordem 13*/  		txt.print(StringUtils.extractNumbers(valoresRTRT.get(12)?.toString()))
/*Ordem 14*/		txt.print(StringUtils.extractNumbers(valoresRTRT.get(13)?.toString()))
					txt.newLine()
				}
				
				if(!idrec.equals("3562")) {
					
					HashMap<Integer, BigDecimal> valoresRTIPP = buscarValorCAE(idGrupoCentralizador, abh80.abh80id, "2003", "2103", calendario)
					if(valoresRTIPP != null) {
/*Ordem 01*/  			txt.print("RTPP")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(7)?.toString()))
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(10)?.toString()))
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRTIPP.get(13)?.toString()))
						txt.newLine()
					}
				
					HashMap<Integer, BigDecimal> valoresRTPO = buscarValorCAE(idGrupoCentralizador, abh80.abh80id, "2002", "2102", calendario)
					if(valoresRTPO != null) {
/*Ordem 01*/  			txt.print("RTPO")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(7)?.toString()))
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(10)?.toString()))
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRTPO.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRTPO.get(13)?.toString()))
						txt.newLine()
					}
					
					HashMap<Integer, BigDecimal> valoresRTDP = buscarValorCAERTDP(idGrupoCentralizador, abh80.abh80id, "2004", "2104", calendario)
					if(valoresRTDP != null) {
/*Ordem 01*/  			txt.print("RTDP")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(7)?.toString()))
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(10)?.toString()))
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRTDP.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRTDP.get(13)?.toString()))
						txt.newLine()
					}
				
					HashMap<Integer, BigDecimal> valoresRTDS = buscarValorCAERTDS(idGrupoCentralizador, abh80.abh80id, "2008", "2108", calendario)
					if(valoresRTDS != null) {
/*Ordem 01*/  			txt.print("RTDS")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(7)?.toString()))
/*Ordem 09*/	  		txt.print(StringUtils.extractNumbers(valoresRTDS.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(10)?.toString()))
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRTDS.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRTDS.get(13)?.toString()))
						txt.newLine()
					}
				
					HashMap<Integer, BigDecimal> valoresRTIRF = buscarValorCAE(idGrupoCentralizador, abh80.abh80id, "2006", "2106", calendario)
					if(valoresRTIRF != null) {
/*Ordem 01*/  			txt.print("RTIRF")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(7)?.toString()))
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(10)?.toString()))
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRTIRF.get(13)?.toString()))
						txt.newLine()
					}
				
					HashMap<Integer, BigDecimal> valoresRIDAC = buscarValorCAE(idGrupoCentralizador, abh80.abh80id, "2201", null, calendario)
					if(valoresRIDAC != null) {
/*Ordem 01*/  			txt.print("RIDAC")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(7)?.toString()))
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(10)?.toString()))
/*Ordem 12*/ 	 		txt.print(StringUtils.extractNumbers(valoresRIDAC.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRIDAC.get(13)?.toString()))
						txt.newLine()
					}
				
					HashMap<Integer, BigDecimal> valoresRIIRP = buscarValorCAE(idGrupoCentralizador, abh80.abh80id, "2202", null, calendario)
					if(valoresRIIRP != null) {
/*Ordem 01*/  			txt.print("RIIRP")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(7)?.toString()))
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(10)?.toString()))
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRIIRP.get(13)?.toString()))
						txt.newLine()				
					}
				
					HashMap<Integer, BigDecimal> valoresRIAP = buscarValorCAE(idGrupoCentralizador, abh80.abh80id, "2203", null, calendario)
					if(valoresRIAP != null) {
/*Ordem 01*/  			txt.print("RIAP")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(1)?.toString()))
/*Ordem 03*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(2)?.toString()))
/*Ordem 04*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(3)?.toString()))
/*Ordem 05*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(4)?.toString()))
/*Ordem 06*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(5)?.toString()))
/*Ordem 07*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(6)?.toString()))
/*Ordem 08*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(7)?.toString()))
/*Ordem 09*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(8)?.toString()))
/*Ordem 10*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(9)?.toString()))
/*Ordem 11*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(10)?.toString()))
/*Ordem 12*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(11)?.toString()))
/*Ordem 13*/  			txt.print(StringUtils.extractNumbers(valoresRIAP.get(12)?.toString()))
/*Ordem 14*/			txt.print(StringUtils.extractNumbers(valoresRIAP.get(13)?.toString()))
						txt.newLine()
					}
				
					BigDecimal valorTotalRIO = buscarValoTotalCAE(idGrupoCentralizador, abh80.abh80id, "2205", null, calendario)
					if(valorTotalRIO != null) {
/*Ordem 01*/  			txt.print("RIO")
/*Ordem 02*/  			txt.print(StringUtils.extractNumbers(valorTotalRIO.toString()))
/*Ordem 03*/  			txt.print("Outros rendimentos isentos")
						txt.newLine()
					}
				}
			}
		}
		
		List<TableMap> planosSaude = buscarPlanoSaude(calendario)
		
		txt.print("PSE")
		txt.newLine()
		
		for(plano in planosSaude) {
/*Ordem 01*/ 		txt.print("OPSE")
/*Ordem 02*/		txt.print(StringUtils.extractNumbers(plano.getString("abh21esOpsCnpj")))
/*Ordem 03*/		txt.print(plano.getString("abh21esOpsRS"))
/*Ordem 04*/		txt.print(plano.getString("abh21esOpsReg"))
			  txt.newLine();
			  
			  List<TableMap> fba0101s = buscarTrabalhadores(calendario, plano.get("abh21ids"))
			  for(fba0101 in fba0101s) {
				  	if(fba0101.get("fba01011valor") == null || fba0101.get("fba01011valor") == 0) {
						  continue;  
					}
/*Ordem 01*/ 		txt.print("TPSE")
/*Ordem 02*/		txt.print(StringUtils.extractNumbers(fba0101.get("abh80cpf") ))
/*Ordem 03*/		txt.print(fba0101.get("abh80nome"))
/*Ordem 04*/		txt.print(StringUtils.extractNumbers(fba0101.get("fba01011valor")?.toString()))
					txt.newLine();
					
					List<TableMap> abh8002s = buscarDependentesTrabalhadores(fba0101.get("abh80id"), calendario, plano.getString("abh21esOpsCnpj"))
					for(abh8002 in abh8002s) {
						txt.print("DTPSE")
						txt.print(StringUtils.extractNumbers(abh8002.get("abh8002cpf")))
						txt.print(ESocialUtils.formatarData(abh8002.get("abh8002dtNasc"), "yyyyMMdd") )
						txt.print(abh8002.get("abh8002nome"))
						txt.print(abh8002.get("aap09dirf"))
						txt.print(StringUtils.extractNumbers(abh8002.get("valor").toString()))
						txt.newLine();
					}
			  }
		}

		txt.print("FIMDirf")
		txt.newLine()
		//Gravar o arquivo TXT ora composto
		put("txt", txt);
		println("**** - Fim: " + LocalTime.now().format(formatter) + " - *****")
	}
	
	private List<TableMap> buscarDependentesTrabalhadores(Long abh80id, Integer ano, String abh21esOpsCnpj) {
		if(abh80id == 1276793) {
			String teste = ""
		}
		List<TableMap> dependentes =  getSession().createCriteria(Abh8002.class)
					.addFields("abh21id, abh8002cpf, abh8002dtNasc, abh8002nome, aap09dirf")
					.addJoin(Joins.join("abh80021", "abh80021dep = abh8002id"))
					.addJoin(Joins.join("aap09","abh8002parente = aap09id"))
					.addJoin(Joins.join("abh21", "abh21id = abh80021eve"))
					.addWhere(Criterions.eq("abh8002trab", abh80id))
					.addWhere(Criterions.eq("abh8002ps", 1))
					.addWhere(Criterions.eq("abh80021tipoCalc", 0))
					.addWhere(Criterions.eq("abh21esOpsCnpj", abh21esOpsCnpj))
					.addWhere(Criterions.not(Criterions.eq("abh80021vlr", 0)))
					.setOrder("abh8002cpf")
					.getListTableMap()
					
		List<Long> abh21ids = new ArrayList();
		for(dependente in dependentes) {
			abh21ids.add(dependente.getLong("abh21id"))
		}
		
		BigDecimal valorTotal = getSession().createCriteria(Fba0101.class)
		.addFields("SUM(fba01011s.fba01011valor) as fba01011valor")
		.addJoin(Joins.join("fba01", "fba01id = fba0101calculo"))
		.addJoin(Joins.join("fba01011", "fba01011vlr = fba0101id").alias("fba01011s"))
		.addJoin(Joins.join("abh80","abh80id = fba0101trab").alias("fba0101trab"))
		.addJoin(Joins.join("abh21", "abh21id = fba01011eve"))
		.addWhere(Criterions.eq("abh80id", abh80id))
		.addWhere(getSamWhere().getCritPadrao(Fba01.class))
		.addWhere(Criterions.in("abh21id", abh21ids))
		.addWhere(Criterions.between("fba0101dtPgto", new LocalDate(ano, 1, 1), new LocalDate(ano, 12, 31)))
		.get(ColumnType.BIG_DECIMAL);
		
		BigDecimal valorPorDependentes = dependentes == null || dependentes.size() == 0 ? 0 : valorTotal / dependentes.size();
		valorPorDependentes = valorPorDependentes.trunc(2)
		HashMap<String, TableMap> dependentesHash = new HashMap()
		List<TableMap> dependentesUnicos = new ArrayList()
		for(dependente in dependentes) {
			if(!dependentesHash.containsKey(dependente.get("abh8002cpf"))) {
				dependente.put("valor", valorPorDependentes)
				dependentesHash.put(dependente.get("abh8002cpf"), dependente)
				dependentesUnicos.add(dependente)
			}
		}
		
		return dependentesUnicos;		
	}
	
	private List<TableMap> buscarTrabalhadores(Integer ano, List<Long> abh21ids) {
		List<Long> abh21depIds = getSession().createCriteria(Abh21.class)
			.addFields("abh21id")
			.addJoin(Joins.join("abh80021", "abh21id = abh80021eve").left(false))
			.addWhere(Criterions.in("abh21id", abh21ids))
			.setGroupBy("GROUP BY abh21id")
			.getList(ColumnType.LONG);
			
		return getSession().createCriteria(Fba0101.class)
			.addFields("fba0101trab.abh80id, fba0101trab.abh80nome, fba0101trab.abh80cpf, SUM(fba01011s.fba01011valor) as fba01011valor")
			.addJoin(Joins.join("fba01", "fba01id = fba0101calculo"))
			.addJoin(Joins.join("fba01011", "fba01011vlr = fba0101id").alias("fba01011s"))
			.addJoin(Joins.join("abh80","abh80id = fba0101trab").alias("fba0101trab"))
			.addJoin(Joins.join("abh21", "abh21id = fba01011eve"))
			.addWhere(getSamWhere().getCritPadrao(Fba01.class))
			.addWhere(Criterions.in("abh21id", abh21ids))
			.addWhere(Criterions.between("fba0101dtPgto", new LocalDate(ano, 1, 1), new LocalDate(ano, 12, 31)))
			.addWhere(Criterions.notIn("abh21id", abh21depIds))
			.setGroupBy("GROUP BY fba0101trab.abh80id, fba0101trab.abh80cpf")
			.setOrder("abh80cpf")
			.getListTableMap();
	}

	private List<TableMap> buscarPlanoSaude(Integer ano) {
		List<TableMap> fba0101s = getSession().createCriteria(Fba0101.class)
					.addFields("abh21id, abh21esOpsCnpj, abh21esOpsRS, abh21esOpsReg")
					.addJoin(Joins.join("fba01", "fba01id = fba0101calculo"))
					.addJoin(Joins.join("fba01011", "fba01011vlr = fba0101id" ))
					.addJoin(Joins.join("abh21", "abh21id = fba01011eve"))
					.addJoin(Joins.join("abh2101", "abh2101evento = abh21id"))
					.addJoin(Joins.join("abh20", "abh20id = abh2101cae"))
					.addWhere(getSamWhere().getCritPadrao(Fba01.class))
					//.addWhere(Criterions.in("abh20codigo", ["1803","1802"]))
					.addWhere(Criterions.between("fba0101dtPgto", new LocalDate(ano, 1, 1), new LocalDate(ano, 12, 31)))
					.addWhere(Criterions.isNotNull("abh21esOpsCnpj"))
					.setOrder("abh21esOpsCnpj ASC")
					.setGroupBy("GROUP BY abh21id, abh21esOpsCnpj, abh21esOpsRS, abh21esOpsReg")
					.getListTableMap();
					
		HashMap<String, TableMap> hashfba0101s = new HashMap();
		
		for(fba0101 in fba0101s) {
			if(!hashfba0101s.containsKey(fba0101.get("abh21esOpsCnpj"))) {
				List<Long> abh21ids = new ArrayList()
				abh21ids.add(fba0101.get("abh21id"))
				fba0101.put("abh21ids", abh21ids)
				hashfba0101s.put(fba0101.get("abh21esOpsCnpj"), fba0101)
			}else {
				List<Long> abh21ids = hashfba0101s.get(fba0101.get("abh21esOpsCnpj")).get("abh21ids")
				abh21ids.add(fba0101.get("abh21id"))
				fba0101.put("abh21ids", abh21ids)
				hashfba0101s.put(fba0101.get("abh21esOpsCnpj"), fba0101)
			}
		}
		
		List<TableMap> listTM = new ArrayList()
		for(hash in hashfba0101s) {
			listTM.add(hash.getValue())
		}
		
		Collections.sort(listTM, new Comparator<TableMap>() {
			@Override
			public int compare(TableMap o1, TableMap o2) {
				return Long.compare(Long.parseLong(StringUtils.extractNumbers(o1.getString("abh21esOpsCnpj"))), Long.parseLong(StringUtils.extractNumbers(o2.getString("abh21esOpsCnpj"))));
			}
		});
		
		return listTM;
	}
	
	private HashMap<Integer, BigDecimal> buscarValorCAERTDP(Long aac01id, Long abh80id, String abh20codigoMes, String abh20codigo13, Integer ano){
		HashMap<Integer, BigDecimal> hashmap = new HashMap();
		BigDecimal valor13Total = 0
		for(int mes=1; mes <= 12; mes++ ) {
			BigDecimal valorFolha = calcularCAE(aac01id, abh80id, abh20codigoMes, mes, ano,  Set.of(0))
			BigDecimal valorAdiantamento = calcularCAE(aac01id, abh80id, abh20codigoMes, mes, ano,  Set.of(1))
			BigDecimal valorRescisao = calcularCAE(aac01id, abh80id, abh20codigoMes, mes, ano,  Set.of(4))
			BigDecimal valor13 = calcularCAE(aac01id, abh80id, abh20codigo13, mes, ano,  Set.of(0,2,4))
			valor13 = valor13 == null ? 0 : valor13
			valor13Total += valor13
			List<BigDecimal> valores = [valorFolha, valorAdiantamento, valorRescisao];
			Collections.sort(valores, Collections.reverseOrder());
			BigDecimal valor = valores[0] == 0 || valores[0] == null? null : valores[0]
			hashmap.put(mes, valor)
		}
		
		
		hashmap.put(13, valor13Total == 0 ? null : valor13Total)
		
		Boolean naoTemValor = true
		for(int mes=1; mes <= 13; mes++ ) {
			if(hashmap.get(mes) != null) {
				naoTemValor = false
			}
		}
		
		if(naoTemValor) {
			return null;
		}
		
		return hashmap;
	}
	
	private HashMap<Integer, BigDecimal> buscarValorCAERTDS(Long aac01id, Long abh80id, String abh20codigoMes, String abh20codigo13, Integer ano){
		HashMap<Integer, BigDecimal> hashmap = new HashMap();
		BigDecimal valor13Total = 0

		for(int mes=1; mes <= 12; mes++ ) {
			if(mes < 5) {
				hashmap.put(mes, null)
				continue
			}
			BigDecimal valor = calcularCAE(aac01id, abh80id, abh20codigoMes, mes, ano,  Set.of(0,3,4,5,6,7,8,9))
			BigDecimal deducoesInss = calcularCAE(aac01id, abh80id, "2002", mes, ano, Set.of(0,3,4))
			BigDecimal deducoesDepen = calcularCAE(aac01id, abh80id, "2004", mes, ano, Set.of(0,3,4))
			BigDecimal deducoespensao = calcularCAE(aac01id, abh80id, "2005", mes, ano, Set.of(0,3,4))
			BigDecimal valor13 = calcularCAE(aac01id, abh80id, abh20codigo13, mes, ano,  Set.of(2,4))

			valor13 = valor13 == null ? 0 : valor13
			valor13Total += valor13
			BigDecimal deducoes = (deducoesInss == null ? 0 : deducoesInss) + 
			(deducoesDepen == null ? 0 : deducoesDepen) + 
			(deducoespensao == null ? 0 : deducoespensao);
			
			if( valor == 0 || deducoes.compareTo(528) == 1) {
				valor = null
			}
			hashmap.put(mes, valor)
		}
		
		BigDecimal deducoesInss13 =  calcularCAE(aac01id, abh80id, "2102", 12, ano, Set.of(0,3,4)) //buscarValorCAE(aac01id, abh80id, "2002", "2102", ano, Set.of(0,3,4))?.get(13)
		BigDecimal deducoesDepen13 = calcularCAE(aac01id, abh80id, "2104", 12, ano, Set.of(0,3,4)) //buscarValorCAE(aac01id, abh80id, "2004", "2104", ano, Set.of(0,3,4))?.get(13)
		BigDecimal deducoesPensao13 = calcularCAE(aac01id, abh80id, "2105", 12, ano, Set.of(0,3,4)) //buscarValorCAE(aac01id, abh80id, "2005", "2105", ano, Set.of(0,3,4))?.get(13)
		
		
		BigDecimal deducoes13 = (deducoesInss13 == null ? 0 : deducoesInss13) +
		(deducoesDepen13 == null ? 0 : deducoesDepen13) +
		(deducoesPensao13 == null ? 0 : deducoesPensao13);
		
		if( valor13Total == 0 || deducoes13.compareTo(528) == 1) {
			valor13Total = null
		}
		
		hashmap.put(13, valor13Total == 0 ? null : valor13Total)
		
		Boolean naoTemValor = true
		for(int mes=1; mes <= 13; mes++ ) {
			if(hashmap.get(mes) != null) {
				naoTemValor = false
			}
		}
		
		if(naoTemValor) {
			return null;
		}
		
		return hashmap;
	}
	
	private BigDecimal buscarValoresDeducoesLegais(Long aac01id, Long abh80id, Integer mes, Integer ano, Boolean decimoTerceiro) {	
			List<String> eventos = decimoTerceiro ? ["4501", "4504", "4506", "9042"] : ["2001", "2011", "2501", "8501", "9040", "3501","3504","9041", "9042", "8504"]
			List<Integer> fba0101tpVlrs = decimoTerceiro ? [2] : [0,3,4]
			Query query = getSession().createQuery(" SELECT SUM(fba01011valor) ",
														" FROM Fba01011 ",
														" INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
														" INNER JOIN Fba01 ON fba01id = fba0101calculo ",
														" INNER JOIN Abh21 ON abh21id = fba01011eve ",
														" WHERE abh21codigo in (:eventos) ",
														" AND DATE_PART('MONTH', fba0101dtPgto) = :mes ",
														" AND DATE_PART('YEAR', fba0101dtPgto) = :ano ",
														" AND fba0101tpVlr in (:fba0101tpVlr) ",
														" AND fba0101trab = :abh80id ");
													
				query.setParameter("eventos", eventos);
				query.setParameter("mes", mes);
				query.setParameter("ano", ano);
				query.setParameter("abh80id", abh80id);
				query.setParameter("fba0101tpVlr", fba0101tpVlrs);
				return query.getUniqueResult(ColumnType.BIG_DECIMAL)
	}
	
	private Long buscarIdGruposCentralizador(Aac10 empresaAtiva){

		String whereEmpresas = " and aac1001empresa =  :idsEmpresas "

		String sql = " select " +
							" aac1001gc " +
					 " from aac1001 " +
					 " where " +
							" Lower(aac1001tabela) = 'fb' " +
					 whereEmpresas

		Parametro parametroEmpresas = Parametro.criar("idsEmpresas", empresaAtiva.aac10id)

		return getAcessoAoBanco().obterLong(sql, parametroEmpresas )
	}

	private BigDecimal buscarValoTotalCAE(Long aac01id, Long abh80id, String abh20codigoMes, String abh20codigo13, Integer ano){
		BigDecimal valorTotal = BigDecimal.ZERO
		for(int mes=1; mes <= 12; mes++ ) {
			BigDecimal valor = calcularCAE(aac01id, abh80id, abh20codigoMes, mes, ano,  Set.of(0,1,3,4,5,6,7,8,9))
			valor = valor == null ? 0 : valor
			valorTotal += valor
		}
		
		if(abh20codigo13 != null) {
			BigDecimal valor = calcularCAE(aac01id, abh80id, abh20codigoMes, 12, ano,  Set.of(2))
			valor =  valor == null ? 0 : valor
			valorTotal += valor
		}
		
		if(valorTotal == 0) {
			return null;
		}
		return valorTotal;
	}
	private HashMap<Integer, BigDecimal> buscarValorCAE(Long aac01id, Long abh80id, String abh20codigoMes, String abh20codigo13, Integer ano){
		return buscarValorCAE(aac01id,abh80id,abh20codigoMes,abh20codigo13,ano, Set.of())
	}
	private HashMap<Integer, BigDecimal> buscarValorCAE(Long aac01id, Long abh80id, String abh20codigoMes, String abh20codigo13, Integer ano, Set<Integer> tiposFba0101){
		if(abh80id == 1688184) { //2718733
			String teste = "";
		}
		HashMap<Integer, BigDecimal> hashmap = new HashMap();
		Set<Integer> tipos = tiposFba0101.isEmpty() ? Set.of(0,1,3,4,5,6,7,8,9) : tiposFba0101
		BigDecimal valor13Total = 0
		for(int mes=1; mes <= 12; mes++ ) {
			BigDecimal valor = calcularCAE(aac01id, abh80id, abh20codigoMes, mes, ano,  tipos)
			BigDecimal valor13 = calcularCAE(aac01id, abh80id, abh20codigo13, mes, ano,  Set.of(0,2,4))
			valor13 = valor13 == null ? 0 : valor13
			valor13Total += valor13
			valor = valor == 0 || valor == null ? null : valor
			hashmap.put(mes, valor)
		}
		
		if(abh20codigo13 != null) {		

			hashmap.put(13, valor13Total == 0 ? null : valor13Total)
		}else {
			hashmap.put(13, null)
		}
		
		Boolean naoTemValor = true
		for(int mes=1; mes <= 13; mes++ ) {
			if(hashmap.get(mes) != null) {
				naoTemValor = false
			}
		}
		
		if(naoTemValor) {
			return null;
		}
		
		return hashmap;
	}
	
	private BigDecimal calcularCAE(Long aac01id, Long abh80id, String abh20codigo, int mes, int ano, Set<Integer> tiposFba0101) {
		BigDecimal valor = BigDecimal.ZERO;

		List<TableMap> tms = buscarValoresDosEventosParaCalculoDoCAE(aac01id, abh80id, abh20codigo, mes, ano, tiposFba0101);
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
	
	private List<TableMap> buscarValoresDosEventosParaCalculoDoCAE(Long aac01id, Long abh80id, String abh20codigo, int mes, int ano, Set<Integer> tiposFba0101) {
		String whereAbh80 = abh80id != null ? "AND fba0101trab = :abh80id " : "";
		
		Query query = getSession().createQuery("SELECT abh2101cvr, fba01011valor ",
											   "FROM Fba01011 ",
											   "INNER JOIN Fba0101 ON fba0101id = fba01011vlr ",
											   "INNER JOIN Fba01 ON fba01id = fba0101calculo ",
											   "INNER JOIN Abh21 ON abh21id = fba01011eve ",
											   "INNER JOIN Abh2101 ON abh2101evento = abh21id ",
											   "INNER JOIN Abh20 ON abh20id = abh2101cae ",
											   "WHERE fba01gc  = :aac01id ",
											   "AND DATE_PART('MONTH', fba0101dtPgto) = :mes ",
											   "AND DATE_PART('YEAR', fba0101dtPgto) = :ano ",
											   "AND fba0101tpVlr IN (:tiposFba0101) ",
											   "AND abh20codigo = :abh20codigo ",
											   whereAbh80,);
		
		query.setParameter("aac01id", aac01id);
		if(abh80id != null) query.setParameter("abh80id", abh80id);
		query.setParameter("abh20codigo", abh20codigo);
		query.setParameter("mes", mes);
		query.setParameter("ano", ano);
		query.setParameter("tiposFba0101", tiposFba0101);
		return query.getListTableMap();
	}
	
	private Aac10 buscarEmpresaMatrix() {
		Aac10 aac10 = getSession().get(Aac10.class, getVariaveis().aac10.aac10id);
		Boolean isMatriz = aac10.aac10matriz == null ? true : false
		
		if(isMatriz) {
			return aac10;
		}
		
		aac10 = getSession().get(Aac10.class, aac10.aac10matriz.aac10id)
		
		return aac10;
	}
	
	private List<Abh80> buscarTrabalhadoresDosValores(Integer ano, String idrec){
		Criterion criterion = idrec == "0588" ? Criterions.eq("abh80tipo", 1) : Criterions.not(Criterions.eq("abh80tipo", 1))
		
		List<Abh80> abh80s = getSession().createCriteria(Abh80.class)
		.addFields("abh80id, abh80nome, abh80cpf")
		.addJoin(Joins.join("fba0101", "fba0101trab = abh80id"))
		.addJoin(Joins.join("fba01", "fba01id = fba0101calculo"))
		.addWhere(getSamWhere().getCritPadrao(Fba01.class))
		.addWhere(Criterions.between("fba0101dtPgto", new LocalDate(ano, 1, 1), new LocalDate(ano, 12, 31)))
		.addWhere(criterion)
		.setOrder("abh80cpf")
		.setGroupBy("GROUP BY abh80id, abh80nome, abh80cpf")
		.getList(ColumnType.ENTITY)
		
		return abh80s
	}
	
	private List<Fba0101> buscarValoresPeriodoPorTrabalhador(Integer ano, Long abh80id){
		
		List<Fba0101> fba0101s = getSession().createCriteria(Fba0101.class)
				.addJoin(Joins.fetch("fba0101calculo"))
				.addJoin(Joins.fetch("fba0101trab"))
				.addWhere(getSamWhere().getCritPadrao(Fba01.class))
				.addWhere(Criterions.between("fba0101dtPgto", new LocalDate(ano, 1, 1), new LocalDate(ano, 12, 31)))
				.addWhere(Criterions.eq("abh80id", abh80id))
				.getList(ColumnType.ENTITY)
		
		return fba0101s;
	}
	
	private List<String> buscarIDREC(Integer ano) {
		List<Integer> abh80tipos = getSession().createCriteria(Fba0101.class)
				.addFields("abh80tipo")
				.addJoin(Joins.join("fba01", "fba01id = fba0101calculo"))
				.addJoin(Joins.join("abh80","abh80id = fba0101trab"))
				.addWhere(Criterions.between("fba0101dtPgto", new LocalDate(ano, 1, 1), new LocalDate(ano, 12, 31)))
				.addWhere(getSamWhere().getCritPadrao(Fba01.class))
				.setGroupBy("GROUP BY abh80tipo")
				.setOrder("abh80tipo")
				.getList(ColumnType.INTEGER)
				
		List<String> idrecs = new ArrayList();
		
		for(abh80tipo in abh80tipos) {
			String idrec = "0561"
			if(abh80tipo == 1) idrec = "0588"
			idrecs.add(idrec)
		}
		
		if(temPLR(ano)) {
			idrecs.add("3562")
		}
		
		Arrays.sort(idrecs);
		
		return idrecs;
	}
	
	private Boolean temPLR(Integer ano) {
		List<Integer> fba0101tpVlr = getSession().createCriteria(Fba0101.class)
		.addFields("fba0101tpVlr")
		.addJoin(Joins.join("fba01", "fba01id = fba0101calculo"))
		.addJoin(Joins.join("abh80","abh80id = fba0101trab"))
		.addWhere(Criterions.between("fba0101dtPgto", new LocalDate(ano, 1, 1), new LocalDate(ano, 12, 31)))
		.addWhere(getSamWhere().getCritPadrao(Fba01.class))
		.addWhere(Criterions.eq("fba0101tpVlr", 6))
		.setGroupBy("GROUP BY abh80tipo, fba0101tpVlr")
		.setOrder("abh80tipo")
		.getList(ColumnType.INTEGER)
		
		if(fba0101tpVlr == null || fba0101tpVlr.size() < 1) {
			return false
		}
		
		return true
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDkifQ==