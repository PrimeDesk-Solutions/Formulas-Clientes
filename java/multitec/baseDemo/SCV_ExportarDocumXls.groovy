package multitec.baseDemo;

import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row

import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase;

public class SCV_ExportarDocumXls extends FormulaBase {
	
	private List<Long> eaa01ids;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.EXPORTAR_DOCUMENTOS;
	}
	
	@Override
	public void executar() {
		this.eaa01ids = get("eaa01ids");
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		
		HSSFSheet sheet = workbook.createSheet("Planilha 1");
		
		int rownum = 0;
		int cellnum = 0;
		for(Long eaa01id : this.eaa01ids) {
			//Documento
			Row row = sheet.createRow(rownum++);
			
			Eaa01 eaa01 = getAcessoAoBanco().buscarRegistroUnicoById("Eaa01", eaa01id);
			
			Abd01 abd01 = eaa01.eaa01pcd;
			Abe40 abe40 = eaa01.eaa01tp;
			Abe30 abe30 = eaa01.eaa01cp;
						
			Abb01 abb01 = eaa01.getEaa01central();
			Aah01 aah01 = getAcessoAoBanco().buscarRegistroUnicoById("Aah01", abb01.abb01tipo.aah01id);
			Abe01 abe01 = getAcessoAoBanco().buscarRegistroUnicoById("Abe01", abb01.abb01ent.abe01id);
			
			cellnum = 0;
			Cell cellRegistro = row.createCell(cellnum++);
			cellRegistro.setCellValue("01");
			
			Cell cellAah01codigo = row.createCell(cellnum++);
			cellAah01codigo.setCellValue(aah01.aah01codigo);
			
			Cell cellAbb01num = row.createCell(cellnum++);
			cellAbb01num.setCellValue(abb01.abb01num);
			
			Cell cellAbb01data = row.createCell(cellnum++);
			cellAbb01num.setCellValue(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(abb01.abb01data));
			
			Cell cellAbe01codigo = row.createCell(cellnum++);
			cellAbe01codigo.setCellValue(abe01.abe01codigo);
			
			Cell cellAbd01codigo = row.createCell(cellnum++);
			cellAbd01codigo.setCellValue(abd01.abd01codigo);
			
			if(abe40 != null) {
				Cell cellAbe40codigo = row.createCell(cellnum++);
				cellAbe40codigo.setCellValue(abe40.abe40codigo);
			}
			
			if(abe30 != null) {
				Cell cellAbe30codigo = row.createCell(cellnum++);
				cellAbe30codigo.setCellValue(abe30.abe30codigo);
			}			
			
			//Itens
			if(eaa01.eaa0103s != null && eaa01.eaa0103s.size() > 0) {
				List<Eaa0103> eaa0103s = eaa01.eaa0103s.stream().sorted({o1, o2 -> o1.eaa0103seq_Zero.compareTo(o2.eaa0103seq_Zero)}).collect(Collectors.toList());
				
				for(Eaa0103 eaa0103 : eaa0103s) {
					Row rowItem = sheet.createRow(rownum++);
					
					cellnum = 0;
					Cell cellRegistroItem = rowItem.createCell(cellnum++);
					cellRegistroItem.setCellValue("02");
					
					Cell cellEaa0103seq = rowItem.createCell(cellnum++);
					cellEaa0103seq.setCellValue(eaa0103.eaa0103seq);
					
					Cell cellEaa0103tipo = rowItem.createCell(cellnum++);
					cellEaa0103tipo.setCellValue(eaa0103.eaa0103tipo);
					
					Cell cellEaa0103codigo = rowItem.createCell(cellnum++);
					cellEaa0103codigo.setCellValue(eaa0103.eaa0103codigo);
					
					Cell cellEaa0103qtComl = rowItem.createCell(cellnum++);
					cellEaa0103qtComl.setCellValue(formatarDecimal(eaa0103.eaa0103qtComl_Zero, 6, false));
					
					Cell cellEaa0103unit = rowItem.createCell(cellnum++);
					cellEaa0103unit.setCellValue(formatarDecimal(eaa0103.eaa0103unit_Zero, 6, false));
				}
			}
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			workbook.write(bos);
		} finally {
			bos.close();
		}
		byte[] bytes = bos.toByteArray();
		
		put("dados", bytes);
	}
	
	private static String formatarDecimal(BigDecimal value, int casasDecimais, boolean vlrZeroRetornaNull) {
		if(value == null || value.compareTo(BigDecimal.ZERO) == 0) {
			if(vlrZeroRetornaNull) {
				return null;
			}
			value = BigDecimal.ZERO;
		}

		BigDecimal bigDecimal = value.setScale(casasDecimais, RoundingMode.HALF_EVEN);
		return bigDecimal.toString();
	}
	
	/*
	 * 
	   	XLS de Exemplo
	   		   
	   	Registro 01 - "cabeça" do documento
	   	|01|aah01codigo|abb01num|abb01data|abe01codigo|abd01codigo|abe40codigo|abe30codigo|
	   
	   	Registro 02 - itens
	   	|02|eaa0103seq|eaa0103tipo|eaa0103codigo|eaa0103qtComl|eaa0103unit|
	  	
	    |01|011|1|11/12/2020|0100001000|10001|003|103|
		|02|1|0|0101001|1.000000|5.000000|
		|01|011|14|14/05/2021|0100001000|10050|003|103|
		|02|1|0|0101001|5.000000|10.000000|
		|02|2|0|0101002|3.000000|3.000000|
		|01|011|15|14/05/2021|0100001000|10050|003|103|
		|02|1|0|0101001|6.000000|2.000000|
		|02|2|0|0101002|1.000000|1.000000|
		|01|011|21|21/10/2021|0100001000|10001|001|001|
		|02|1|1|30001|5.000000|13.000000|
		|01|011|22|21/10/2021|0100001000|10001|||
		|02|1|1|30001|3.000000|10.000000|
		|01|011|23|25/10/2021|0100001000|10050|003|103|
		|02|1|0|0102001|5.000000|5.200000|
		|02|2|0|0102002|5.000000|2.300000|
		|02|3|0|0102003|5.000000|3.500000|
		|02|4|0|0102004|5.000000|10.000000|
		|02|5|0|0102005|5.000000|8.000000|
		|02|6|0|0102006|5.000000|2.850000|
		|02|7|0|0102007|5.000000|3.000000|
		|02|8|0|0102008|5.000000|4.200000|
		|02|9|0|0101001|5.000000|2.500000|
		|02|10|0|0101002|5.000000|10.000000|
		|02|11|0|0101003|5.000000|3.200000|
		|02|12|0|0101004|5.000000|1.580000|
		|02|13|0|0101005|5.000000|2.950000|
		|01|011|24|25/10/2021|0100001000|10050|003|103|
		|02|1|2|0102|4.000000|18.650000|
		|02|2|2|0103|4.000000|3.500000|
		|02|3|2|0101|4.000000|6.700000|
		|01|011|148|04/11/2021|0100001000|10003|||
		|02|1|1|30001|1.000000|30.000000|
		|01|011|149|04/11/2021|0100001000|10003|||
		|02|1|1|30001|1.000000|35.000000|
		|01|011|152|29/11/2021|0100001000|10001|||
		|02|1|1|01001|10.000000|5.000000|
	 * 
	 */

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjUifQ==