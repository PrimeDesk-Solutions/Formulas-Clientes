package multitec.baseDemo

import java.time.LocalDate

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.dto.spp.SPPItemBaseCRPDto
import sam.model.entities.ab.Abb20
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abp01
import sam.model.entities.ab.Abp10
import sam.model.entities.ab.Abp4001
import sam.model.entities.ba.Baa1001
import sam.server.samdev.formula.FormulaBase

class SPP_CalculoCRP extends FormulaBase {
	
	private Long baa01id; 						//id do plano de produção
	private Long abp50id; 						//id da carga hora/homem
	private LocalDate dataCalculo; 				//data do cálculo
	private Integer origemCalculo;				//origem do cálculo 0-Plano de Produção 1-Ordem de Produção
	private TableMap jsonCalculo;				//campos livres para o cálculo
	private List<SPPItemBaseCRPDto> listaBase;	//lista base (cada item da lista é um objeto SPPItemBaseCRPDto com os seguintes atributos: Integer seqOP; Abm01 abm01; BigDecimal qt; Abp10 abp10; Abp01 abp01; Integer seqAtiv; Integer seqAtivAnt; Integer minutos; Abb20 abb20; Long idProdPP; Long idOP; Long idAtivOP; )
	
	private List<Baa1001> baa1001s;				//Lista de detalhamento do CRP a ser composta e retornada pela fórmula
	
	private List<ItemMemoriaCalculo> listaMemoriaCalculo;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPP_CRP;
	}
	
	@Override
	public void executar() {
		baa01id = get("baa01id");
		abp50id = get("abp50id");
		dataCalculo = get("dataCalculo");
		origemCalculo = get("origemCalculo");
		jsonCalculo = get("jsonCalculo");
		listaBase = get("listaBase");
		
		listaMemoriaCalculo = new ArrayList<>();
		
		for(int i = 0; i < listaBase.size(); i++) {
			SPPItemBaseCRPDto itemBase = listaBase.get(i);
			ItemMemoriaCalculo itemMemoriaCalculoBase = comporItemMemoriaCalculoPeloItemBase(itemBase);
			
			itemMemoriaCalculoBase.ativRequerido = itemBase.minutos;
			
			def ativDisp = 0;
			ItemMemoriaCalculo itemMemoriaCalculoPrevioAtiv = buscarItemMemoriaCalculoPrevioAtiv(itemBase.seqOP, itemBase.seqAtivAnt, itemBase.abp01);
			if(itemMemoriaCalculoPrevioAtiv != null) {
				itemMemoriaCalculoBase.data = LocalDate.from(itemMemoriaCalculoPrevioAtiv.data);
				if(itemMemoriaCalculoPrevioAtiv.ativSaldo > 0) {
					if(itemMemoriaCalculoPrevioAtiv.abp01.abp01id == itemBase.abp01.abp01id) {
						ativDisp = itemMemoriaCalculoPrevioAtiv.ativSaldo;
					}
				}else {
					itemMemoriaCalculoBase.data = itemMemoriaCalculoBase.data.plusDays(1);
				}
			}
			if(ativDisp == 0) {
				Long abp40id = buscarAtividadeJornada(abp50id, itemBase.abp01.abp01id);
				if(abp40id == null) interromper("Não foi encontrada jornada para a atividade " + itemBase.abp01.abp01codigo);
				Abp4001 abp4001 = buscarJornada(abp40id, itemMemoriaCalculoBase.data);
				if(abp4001 == null) interromper("Não foi encontrada jornada para a atividade " + itemBase.abp01.abp01codigo + " com data igual ou maior a " + itemMemoriaCalculoBase.data);
				itemMemoriaCalculoBase.data = abp4001.abp4001data;
				ativDisp = abp4001.abp4001totalMin;
			}
			itemMemoriaCalculoBase.ativDisp = ativDisp;
			
			if(itemBase.abb20 != null) {
				itemMemoriaCalculoBase.maqRequerido = calcularMaqRequerido(itemBase.abb20, itemBase.qt);
				
				def maqDisp = 0;
				ItemMemoriaCalculo itemMemoriaCalculoPrevioMaq = buscarItemMemoriaCalculoPrevioMaq(itemBase.abb20, itemMemoriaCalculoBase.data);
				if(itemMemoriaCalculoPrevioMaq != null) {
					if(itemMemoriaCalculoPrevioMaq.maqSaldo > 0) {
						maqDisp = itemMemoriaCalculoPrevioMaq.maqSaldo;
					}
				}else {
					maqDisp = calcularMaqDisp(itemBase.abb20, itemMemoriaCalculoBase.data);
				}
				itemMemoriaCalculoBase.maqDisp = maqDisp;
			}
			
			def disp = itemMemoriaCalculoBase.ativDisp;
			if(itemMemoriaCalculoBase.maqDisp > 0 && itemMemoriaCalculoBase.maqDisp < disp) {
				disp = itemMemoriaCalculoBase.maqDisp;
			}
			
			def minutosUsados = 0;
			if(itemMemoriaCalculoBase.ativRequerido > disp) {
				minutosUsados = disp;
			}else {
				minutosUsados = itemMemoriaCalculoBase.ativRequerido;
			}
			itemMemoriaCalculoBase.ativUsada = minutosUsados;
			
			itemMemoriaCalculoBase.ativSaldo = itemMemoriaCalculoBase.ativDisp - itemMemoriaCalculoBase.ativUsada;
			
			itemMemoriaCalculoBase.ativRequer = itemMemoriaCalculoBase.ativRequerido - itemMemoriaCalculoBase.ativUsada;
			
			if(itemBase.abb20 != null) {
				itemMemoriaCalculoBase.maqUsada = minutosUsados;
				
				itemMemoriaCalculoBase.maqSaldo = itemMemoriaCalculoBase.maqDisp - itemMemoriaCalculoBase.maqUsada;
				
				itemMemoriaCalculoBase.maqRequer = itemMemoriaCalculoBase.maqRequerido - itemMemoriaCalculoBase.maqUsada;
			}
			
			listaMemoriaCalculo.add(itemMemoriaCalculoBase);
			
			adicionarAtividadesMaquinasAindaRequer(itemMemoriaCalculoBase, itemBase);
		}
		
		comporListaDetalhamentoCRPPelaMemoriaCalculo(); 
		
		put("baa1001s", baa1001s);
	}
	
	private ItemMemoriaCalculo comporItemMemoriaCalculoPeloItemBase(SPPItemBaseCRPDto itemBase) {
		ItemMemoriaCalculo itemMemoriaCalculo = new ItemMemoriaCalculo();
		itemMemoriaCalculo.seqOP = itemBase.seqOP;
		itemMemoriaCalculo.abm01 = itemBase.abm01;
		itemMemoriaCalculo.qt = itemBase.qt;
		itemMemoriaCalculo.abp10 = itemBase.abp10;
		itemMemoriaCalculo.data = LocalDate.from(dataCalculo);
		itemMemoriaCalculo.seqAtiv = itemBase.seqAtiv;
		itemMemoriaCalculo.abp01 = itemBase.abp01;
		itemMemoriaCalculo.ativRequerido = 0;
		itemMemoriaCalculo.ativDisp = 0;
		itemMemoriaCalculo.ativUsada = 0;
		itemMemoriaCalculo.ativSaldo = 0;
		itemMemoriaCalculo.ativRequer = 0;
		itemMemoriaCalculo.abb20 = itemBase.abb20;
		itemMemoriaCalculo.maqRequerido = 0;
		itemMemoriaCalculo.maqDisp = 0;
		itemMemoriaCalculo.maqUsada = 0;
		itemMemoriaCalculo.maqSaldo = 0;
		itemMemoriaCalculo.maqRequer = 0;
		itemMemoriaCalculo.idProdPP = itemBase.idProdPP;
		itemMemoriaCalculo.idOP = itemBase.idOP;
		itemMemoriaCalculo.idAtivOP = itemBase.idAtivOP;
		return itemMemoriaCalculo;
	}
	
	private def calcularMaqRequerido(Abb20 abb20, def qt) {
		if(abb20 == null) return 0;
		qt = round(qt, 0);
		def capacidade = buscarCapacidadeProducaoMaquina(abb20.abb20id);
		if(capacidade == 0) capacidade = 1;
		def maqRequerido = (1440 * qt) / capacidade;
		maqRequerido = round(maqRequerido, 0);
		return maqRequerido;
	}
	
	private def buscarCapacidadeProducaoMaquina(Long abb20id) {
		def sql = " SELECT abb20capacTempo FROM Abb20 " +
				  " WHERE abb20id = :id ";
		def param1 = criarParametroSql("id", abb20id);
		return getAcessoAoBanco().obterBigDecimal(sql, param1);
	}
	
	private ItemMemoriaCalculo buscarItemMemoriaCalculoPrevioAtiv(Integer seqOP, Integer seqAnt, Abp01 abp01) {
		ItemMemoriaCalculo itemMemoriaCalculoPrevio = null;
		
		for(int i = listaMemoriaCalculo.size()-1; i >= 0; i--) {
			ItemMemoriaCalculo itemMemoriaCalculo = listaMemoriaCalculo.get(i);
			
			if(seqAnt != null) {
				if(itemMemoriaCalculo.seqOP == seqOP && itemMemoriaCalculo.seqAtiv == seqAnt && itemMemoriaCalculo.ativUsada != 0) {
					itemMemoriaCalculoPrevio = listaMemoriaCalculo.get(i);
					break;
				}
			}else {
				if(itemMemoriaCalculo.abp01.abp01id == abp01.abp01id && itemMemoriaCalculo.ativUsada != 0) {
					itemMemoriaCalculoPrevio = listaMemoriaCalculo.get(i);
					break;
				}
			}
		}
		return itemMemoriaCalculoPrevio;
	}
	
	private Long buscarAtividadeJornada(Long abp50id, Long abp01id) {
		def sql = " SELECT abp5001jorHoraHom FROM Abp5001 " +
				  " WHERE abp5001carga = :abp50id " + 
				  " AND abp5001ativOper = :abp01id ";
		def param1 = criarParametroSql("abp50id", abp50id);
		def param2 = criarParametroSql("abp01id", abp01id);
		return getAcessoAoBanco().obterBigDecimal(sql, param1, param2);
	}
	
	private Abp4001 buscarJornada(Long abp40id, LocalDate data) {
		def sql = " SELECT * FROM Abp4001 " +
				  " WHERE abp4001jornada = :abp40id " + 
				  " AND abp4001data >= :data " + 
				  " ORDER BY abp4001data";
		def param1 = criarParametroSql("abp40id", abp40id);
		def param2 = criarParametroSql("data", data);
		return getAcessoAoBanco().buscarRegistroUnico(sql, param1, param2);
	}
	
	private ItemMemoriaCalculo buscarItemMemoriaCalculoPrevioMaq(Abb20 abb20, LocalDate data) {
		if(abb20 == null) return null;
		ItemMemoriaCalculo itemMemoriaCalculoPrevio = null;
		
		for(int i = listaMemoriaCalculo.size()-1; i >= 0; i--) {
			ItemMemoriaCalculo itemMemoriaCalculo = listaMemoriaCalculo.get(i);
			
			if(itemMemoriaCalculo.abb20 == null) continue;
			
			if(itemMemoriaCalculo.abb20.abb20id == abb20.abb20id && itemMemoriaCalculo.data == data && itemMemoriaCalculo.ativUsada != 0) {
				itemMemoriaCalculoPrevio = listaMemoriaCalculo.get(i);
				break;
			}
		}
		return itemMemoriaCalculoPrevio;
	}
	
	private def calcularMaqDisp(Abb20 abb20, LocalDate data) {
		if(abb20 == null) return 0;
		def minutosJornadaIndisponivel = buscarMinutosJornadaIndisponivel(abb20.abb20id, data);
		return 1440 - minutosJornadaIndisponivel;
	}
	
	private def buscarMinutosJornadaIndisponivel(Long abb20id, LocalDate data) {
		def sql = " SELECT SUM(abb2001min) FROM Abb2001 " +
				  " WHERE abb2001bem = :abb20id " +
				  " AND abb2001data = :data ";
		def param1 = criarParametroSql("abb20id", abb20id);
		def param2 = criarParametroSql("data", data);
		return getAcessoAoBanco().obterInteger(sql, param1, param2);
	}
	
	private void adicionarAtividadesMaquinasAindaRequer(ItemMemoriaCalculo itemMemoriaCalculoBase, SPPItemBaseCRPDto itemBase) {
		ItemMemoriaCalculo itemMemoriaCalculoNova = null;
		
		def ativRequer = itemMemoriaCalculoBase.ativRequer;
		def maqRequer = itemMemoriaCalculoBase.maqRequer;
		
		//Verificando Atividade Requer
		if(ativRequer > 0) {
			LocalDate data = LocalDate.from(itemMemoriaCalculoBase.data);
			while(ativRequer > 0) {
				itemMemoriaCalculoNova = comporItemMemoriaCalculoPeloItemBase(itemBase);
				
				data = data.plusDays(1);
				itemMemoriaCalculoNova.data = data;
				
				itemMemoriaCalculoNova.ativRequerido = ativRequer;
				
				Long abp40id = buscarAtividadeJornada(abp50id, itemBase.abp01.abp01id);
				if(abp40id == null) interromper("Não foi encontrada jornada para a atividade " + itemBase.abp01.abp01codigo);
				Abp4001 abp4001 = buscarJornada(abp40id, itemMemoriaCalculoNova.data);
				if(abp4001 == null) interromper("Não foi encontrada jornada para a atividade " + itemBase.abp01.abp01codigo + " com data igual ou maior a " + itemMemoriaCalculoNova.data);
				itemMemoriaCalculoNova.data = abp4001.abp4001data;
				itemMemoriaCalculoNova.ativDisp = abp4001.abp4001totalMin;
				
				def minutosUsados = 0;
				if(itemMemoriaCalculoNova.ativRequerido > itemMemoriaCalculoNova.ativDisp) {
					minutosUsados = itemMemoriaCalculoNova.ativDisp;
				}else {
					minutosUsados = itemMemoriaCalculoNova.ativRequerido;
				}
				itemMemoriaCalculoNova.ativUsada = minutosUsados;
				
				itemMemoriaCalculoNova.ativSaldo = itemMemoriaCalculoNova.ativDisp - itemMemoriaCalculoNova.ativUsada;
				
				itemMemoriaCalculoNova.ativRequer = itemMemoriaCalculoNova.ativRequerido - itemMemoriaCalculoNova.ativUsada;
				
				if(itemBase.abb20 != null) {
					if(maqRequer > 0) {
						itemMemoriaCalculoNova.maqRequerido = maqRequer;
					}
					
					itemMemoriaCalculoNova.maqDisp = calcularMaqDisp(itemBase.abb20, itemMemoriaCalculoNova.data);
					
					itemMemoriaCalculoNova.maqUsada = minutosUsados;
				
					itemMemoriaCalculoNova.maqSaldo = itemMemoriaCalculoNova.maqDisp - itemMemoriaCalculoNova.maqUsada;
				
					itemMemoriaCalculoNova.maqRequer = itemMemoriaCalculoNova.maqRequerido - itemMemoriaCalculoNova.maqUsada;
				}
				
				listaMemoriaCalculo.add(itemMemoriaCalculoNova);
				
				ativRequer = itemMemoriaCalculoNova.ativRequer;
				
				maqRequer = itemMemoriaCalculoNova.maqRequer;
			}
		}
		
		//Verificando Máquina Requer
		if(maqRequer > 0 && itemBase.abb20 != null) {
			LocalDate data = null;
			if(itemMemoriaCalculoNova != null) {
				data = LocalDate.from(itemMemoriaCalculoNova.data);
			}else {
				data = LocalDate.from(itemMemoriaCalculoBase.data);
			}
			while(maqRequer > 0) {
				itemMemoriaCalculoNova = comporItemMemoriaCalculoPeloItemBase(itemBase);
				
				data = data.plusDays(1);
				itemMemoriaCalculoNova.data = data;
				
				if(maqRequer > 0) {
					itemMemoriaCalculoNova.maqRequerido = maqRequer;
				}
				
				itemMemoriaCalculoNova.maqDisp = calcularMaqDisp(itemBase.abb20, itemMemoriaCalculoNova.data);
				
				def minutosUsados = 0;
				if(itemMemoriaCalculoNova.maqRequerido > itemMemoriaCalculoNova.maqDisp) {
					minutosUsados = itemMemoriaCalculoNova.maqDisp;
				}else {
					minutosUsados = itemMemoriaCalculoNova.maqRequerido;
				}
				itemMemoriaCalculoNova.maqUsada = minutosUsados;
			
				itemMemoriaCalculoNova.maqSaldo = itemMemoriaCalculoNova.maqDisp - itemMemoriaCalculoNova.maqUsada;
			
				itemMemoriaCalculoNova.maqRequer = itemMemoriaCalculoNova.maqRequerido - itemMemoriaCalculoNova.maqUsada;
								
				itemMemoriaCalculoNova.ativUsada = minutosUsados;
				
				itemMemoriaCalculoNova.ativSaldo = itemMemoriaCalculoNova.ativDisp - itemMemoriaCalculoNova.ativUsada;
				
				listaMemoriaCalculo.add(itemMemoriaCalculoNova);
				
				maqRequer = itemMemoriaCalculoNova.maqRequer;
			}
		}
	}
	
	private void comporListaDetalhamentoCRPPelaMemoriaCalculo() {
		baa1001s = new ArrayList<>();
		
		if(listaMemoriaCalculo != null && listaMemoriaCalculo.size() > 0) {
			for(int i = 0; i < listaMemoriaCalculo.size(); i++) {
				ItemMemoriaCalculo itemMemoriaCalculo = listaMemoriaCalculo.get(i);
				
				Baa1001 baa1001 = new Baa1001();
				baa1001.baa1001seq = itemMemoriaCalculo.seqOP;
				baa1001.baa1001item = itemMemoriaCalculo.abm01;
				baa1001.baa1001qt = itemMemoriaCalculo.qt;
				baa1001.baa1001proc = itemMemoriaCalculo.abp10;
				baa1001.baa1001data = itemMemoriaCalculo.data;
				baa1001.baa1001seqAtiv = itemMemoriaCalculo.seqAtiv;
				baa1001.baa1001ativ = itemMemoriaCalculo.abp01;
				baa1001.baa1001minHom = itemMemoriaCalculo.ativUsada;
				baa1001.baa1001sdoMinHom = itemMemoriaCalculo.ativSaldo;
				baa1001.baa1001bem = itemMemoriaCalculo.abb20;
				baa1001.baa1001minMaq = itemMemoriaCalculo.maqUsada;
				baa1001.baa1001sdoMinMaq = itemMemoriaCalculo.maqSaldo;
				baa1001.baa1001idProdPP = itemMemoriaCalculo.idProdPP;
				baa1001.baa1001idOP = itemMemoriaCalculo.idOP;
				baa1001.baa1001idAtivOP = itemMemoriaCalculo.idAtivOP;
				baa1001s.add(baa1001);
			}
		}
	}
	
	class ItemMemoriaCalculo {
		Integer seqOP;
		Abm01 abm01;
		BigDecimal qt; 
		Abp10 abp10;
		LocalDate data;
		Integer seqAtiv;
		Abp01 abp01; 
		Integer ativRequerido;
		Integer ativDisp;
		Integer ativUsada;
		Integer ativSaldo;
		Integer ativRequer;
		Abb20 abb20;
		Integer maqRequerido;
		Integer maqDisp;
		Integer maqUsada;
		Integer maqSaldo;
		Integer maqRequer;
		Long idProdPP;
		Long idOP;
		Long idAtivOP;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTgifQ==