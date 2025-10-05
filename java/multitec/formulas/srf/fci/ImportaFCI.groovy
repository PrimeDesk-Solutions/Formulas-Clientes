package multitec.formulas.srf.fci

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ea.Eab01
import sam.model.entities.ea.Eab0101
import sam.server.samdev.formula.FormulaBase

class ImportaFCI extends FormulaBase {
	
	private List<String> registros;
	private List<Eab0101> eab0101s;
	private List<TableMap> ocorrencias;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_IMPORTAR_FCI;
	}

	@Override
	public void executar() {
		registros = get("registros");
		
		if(registros == null || registros.size() == 0) {
			throw new ValidacaoException("Não há registros no arquivo para se efetuar a importação.");
		}
		
		eab0101s = new ArrayList<>();
		ocorrencias = new ArrayList<>();
	
		TextFileLeitura txt = new TextFileLeitura(registros, "|");
		
		String protocolo = null;
		
		while(txt.nextLine()){
			if(txt.getSubString(0, 4).equals("0000")){
				protocolo = txt.getCampo(6);
			}else if(txt.getSubString(0, 4).equals("5020")){
				String codigo = txt.getCampo(3);
				
				Eab0101 eab0101 = buscarCalculoEnviadoPorCodItemCISemNumFCI(codigo);
				if(eab0101 != null) {
					eab0101.eab0101numFCI = txt.getCampo(9);
					eab0101.eab0101protocolo = protocolo;
					eab0101s.add(eab0101);
				}else {
					TableMap tmOcorrencia = new TableMap();
					tmOcorrencia.put("ocorrencia", "Não foi encontrado o cálculo de FCI com código: " + codigo + ".")
					ocorrencias.add(tmOcorrencia);
				}
			}
		}
		
		put("eab0101s", eab0101s);
		put("ocorrencias", ocorrencias);
	}
	
	private Eab0101 buscarCalculoEnviadoPorCodItemCISemNumFCI(String codigo) {
		if(codigo == null || codigo.length() == 0) return null;
		return getSession().createCriteria(Eab0101.class)
						   .addJoin(Joins.fetch("eab0101fci").alias("eab01"))
						   .addJoin(Joins.fetch("eab01.eab01item").alias("abm01"))
						   .addWhere(Criterions.eq("abm01codigo", codigo))
						   .addWhere(Criterions.eq("eab0101status", Eab0101.STATUS_ENVIADA))
						   .addWhere(Criterions.isNull("eab0101numFCI"))
						   .addWhere(getSamWhere().getCritPadrao(Eab01.class))
						   .setOrder("eab0101data DESC")
						   .setMaxResults(1)
						   .get();
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODcifQ==