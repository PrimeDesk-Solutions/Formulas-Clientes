package multitec.baseDemo

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abp01
import sam.model.entities.ab.Abp10
import sam.model.entities.ba.Bab01
import sam.model.entities.ba.Bab0102
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SPP_AtividadeOP extends FormulaBase {
	
	private Bab01 bab01;
	private Bab0102 bab0102;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPP_MINUTOS_PREVISTOS;
	}
	
	@Override
	public void executar() {
		bab01 = get("bab01");
		bab0102 = get("bab0102");
		
		if(bab0102.bab0102ativ == null) {
			bab0102.bab0102minPrev = 0.0;
			bab0102.bab0102minAplic = 0.0;
			bab0102.bab0102custo = 0.0;
			return;
		}
		
		def minAtiv = 0.0;
		def minSetup = 0.0;
		def custoCadAtividade = 0.0;
		
		//Buscando dados da atividade no processo
		def sql = " SELECT abp1001minAtiv, abp1001minSetup, abp01custo " +
				  " FROM Abp1001 " +
				  " INNER JOIN Abp10 ON abp1001proc = abp10id " +
				  " INNER JOIN Abp01 ON abp1001ativ = abp01id " +
				  " WHERE abp1001proc = :abp10id " +
				  " AND abp1001ativ = :abp01id " +
				  getSamWhere().getWherePadrao("AND", Abp10.class) +
				  " ORDER BY abp1001seq ";
		
		TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql,
															Parametro.criar("abp10id", bab01.bab01proc.abp10id),
															Parametro.criar("abp01id", bab0102.bab0102ativ.abp01id));
		
		if(tm != null) {
			minAtiv = tm.getBigDecimal_Zero("abp1001minAtiv");
			minSetup = tm.getBigDecimal_Zero("abp1001minSetup");
			custoCadAtividade = tm.getBigDecimal_Zero("abp01custo");
		}
		
		//Buscando dados no cadastro da atividade
		if(minAtiv == 0.0 && minSetup == 0.0) {
			sql = " SELECT abp01id, abp01minAtiv, abp01minSetup, abp01custo" +
				  " FROM Abp01" +
				  " WHERE abp01id = :abp01id" +
				  getSamWhere().getWherePadrao("AND", Abp01.class);
	
			tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abp01id", bab0102.bab0102ativ.abp01id));
			
			if(tm != null) {
				minAtiv = tm.getBigDecimal_Zero("abp01minAtiv");
				minSetup = tm.getBigDecimal_Zero("abp01minSetup");
				custoCadAtividade = tm.getBigDecimal_Zero("abp01custo");
			}
		}
		
		if(minAtiv == 0.0 && minSetup == 0.0) {
			bab0102.bab0102minPrev = 0.0;
			bab0102.bab0102minAplic = 0.0;
			bab0102.bab0102custo = 0.0;
			return;
		}
		
		//Quantidade
		def qt = bab01.bab01qtP;
		if(qt == 0.0) {
			qt = bab01.bab01qt;
		}
		
		//Minutos previstos
		def minutosPrevistos = (qt * minAtiv) + minSetup;
		minutosPrevistos = round(minutosPrevistos, 6);
		bab0102.bab0102minPrev = minutosPrevistos;
		
		//Minutos aplicados
		if(bab0102.bab0102dtI == null || bab0102.bab0102hrI == null || bab0102.bab0102dtF == null || bab0102.bab0102hrF == null) {
			bab0102.bab0102minAplic = bab0102.bab0102minPrev;
		}else {
			LocalDateTime dtHI = new LocalDateTime(bab0102.bab0102dtI, bab0102.bab0102hrI);
			LocalDateTime dtHF = new LocalDateTime(bab0102.bab0102dtF, bab0102.bab0102hrF);
			bab0102.bab0102minAplic = ChronoUnit.MINUTES.between(dtHI, dtHF);
		}
		
		//Custo da atividade
		def custo = bab0102.bab0102minAplic * (custoCadAtividade / 60);
		custo = round(custo, 2);
		bab0102.bab0102custo = custo;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODQifQ==