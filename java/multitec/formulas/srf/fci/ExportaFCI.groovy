package multitec.formulas.srf.fci

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.ValidacaoException
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aag0201
import sam.model.entities.ea.Eab0101
import sam.server.samdev.formula.FormulaBase

class ExportaFCI extends FormulaBase {
	
	private List<Eab0101> eab0101s;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_EXPORTAR_FCI;
	}

	@Override
	public void executar() {
		eab0101s = get("eab0101s");
		
		if(eab0101s == null || eab0101s.size() == 0) {
			throw new ValidacaoException("Não há FCI na lista para se efetuar a exportação.");
		}
		
		TextFile txt = new TextFile("|", true);
		
		Aac10 aac10 = variaveis.aac10;		
		
		/**
		 * 0000: Identificação do Contribuinte
		 */
		txt.print("0000");
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);
		txt.print(aac10.aac10rs);
		txt.print("1.0");
		txt.newLine();
		
		/**
		 * 0001: Identificação do Início do Bloco
		 */
		txt.print("0001");
		txt.print("Texto em caracteres UTF-8: (dígrafo BR)'ção',(dígrafo espanhol-enhe)'ñ',(trema)'Ü',(ordinais)'ªº',(ligamento s+z alemão)'ß'.");
		txt.newLine();
		
		/**
		 * 0010: Informações do estabelecimento/Contribuinte informante
		 */
		
		Aag0201 aag0201 = aac10.getAac10municipio() == null ? null : obterMunicipio(aac10.getAac10municipio().aag0201id);
		String ie = obterIE(aag0201 == null ? null : aag0201.aag0201uf.aag02id, aac10.aac10id);
		
		txt.print("0010");
		txt.print(StringUtils.extractNumbers(aac10.aac10ni), 14);
		txt.print(aac10.aac10rs);
		txt.print(StringUtils.extractNumbers(ie));
		txt.print(aac10.aac10numero != null ? aac10.aac10endereco + ", " + aac10.aac10numero : aac10.aac10endereco);
		txt.print(aac10.aac10cep == null ? 0 : aac10.aac10cep, 8);
		txt.print(aag0201 == null ? null : aag0201.aag0201nome);
		txt.print(aag0201 == null ? null : aag0201.aag0201uf.aag02uf);
		txt.newLine();

		/**
		 * 0990: Finalização do Bloco 0
		 */
		txt.print("0990");
		txt.print("4");
		txt.newLine();
		
		/**
		 * Bloco 5
		 * 5001: Início do bloco de produtos e mercadorias
		 */
		txt.print("5001");
		txt.newLine();
		
		/**
		 * 5020: Informações dos produtos/mercadorias
		 */
		for(Eab0101 eab0101 : eab0101s) {
			txt.print("5020");
			txt.print(eab0101.eab0101descr);
			txt.print(eab0101.eab0101ncm);
			txt.print(eab0101.eab0101codigo);
			txt.print(eab0101.eab0101ean);
			txt.print(eab0101.eab0101umv);
			txt.print(formatarValor(eab0101.eab0101unitSai));
			txt.print(formatarValor(eab0101.eab0101unitImp));
			
			def ci = eab0101.eab0101ci;
			txt.print(formatarValor(ci));
			
			txt.newLine();	
		}
		
		/**
		 * 5990: Finalização do Bloco 5
		 */
		txt.print("5990");
		txt.print(eab0101s.size() + 2);
		txt.newLine();
		
		/**
		 * 9001: Identificação do início do bloco 9
		 */
		txt.print("9001");
		txt.newLine();
		
		/**
		 * 9900: Bloco 9
		 */
		txt.print("9900");
		txt.print("0000");
		txt.print(1);
		txt.newLine();
		
		txt.print("9900");
		txt.print("0010");
		txt.print(1);
		txt.newLine();
		
		txt.print("9900");
		txt.print("5020");
		txt.print(eab0101s.size());
		txt.newLine();
		
		/**
		 * 9990: Encerramento do bloco 9
		 */
		txt.print("9990");
		txt.print("5");
		txt.newLine();
		
		/**
		 * 9999: Encerramento do arquivo digital
		 */
		txt.print("9999");
		txt.print(12 + eab0101s.size());
		txt.newLine();
		
		put("txt", txt);
	}
	
	/**
	 * Formata os campos de valores decimais, Ex: 1.200,00 fica 1200,00
	 * @param valor Decimal valor
	 * @return String
	 */
	private String formatarValor(BigDecimal valor) {
		NumberFormat qtDec = NumberFormat.getIntegerInstance();
		qtDec.setGroupingUsed(false);
		qtDec.setMinimumIntegerDigits(2);
		
		DecimalFormatSymbols d = DecimalFormatSymbols.getInstance();
		char c = ',';
		d.setDecimalSeparator(c);
		NumberFormat format = new DecimalFormat("##0." + qtDec.format(0), d);
		
		return format.format(valor);
	}
	
	private Aag0201 obterMunicipio(Long aag0201id) {		
		return getSession().createCriteria(Aag0201.class)
			.addFields("aag0201id, aag0201nome, aag0201uf, aag02id, aag02uf")
			.addJoin(Joins.part("aag0201uf"))
			.addWhere(Criterions.eq("aag0201id", aag0201id))
			.get();
	}
	
	private String obterIE(Long aag02id, Long aac10id) {
		if(aag02id == null || aac10id == null) return null;
		return getSession().createCriteria(Aac1002.class)
			.addFields("aac1002ie")
			.addWhere(Criterions.eq("aac1002uf", aag02id))
			.addWhere(Criterions.eq("aac1002empresa", aac10id))
			.get(ColumnType.STRING);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODYifQ==