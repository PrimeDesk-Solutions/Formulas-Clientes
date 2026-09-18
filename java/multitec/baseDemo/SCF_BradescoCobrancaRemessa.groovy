package multitec.baseDemo;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb0102
import sam.model.entities.ab.Abe0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.server.samdev.formula.FormulaBase;

class SCF_BradescoCobrancaRemessa extends FormulaBase{
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_REMESSA_DE_COBRANCA;
	}
	
	@Override
	public void executar() {
		Long abb01id = null; //Localizar id da central do documento financeiro (daa01central)
		TableMap tmEndereco = buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(abb01id);
		
		TextFile txt = new TextFile();

		txt.print("REMESSA");
		txt.print("DEMONSTRACAO");
		txt.newLine();
		
		put("txt", txt);
	}
	
	/**
	 * Buscar o endereço de cobrança a partir da central do documento financeiro
	 * @param abb01id Long Id da central do documento financeiro
	 * @return TableMap
	 */
	public TableMap buscarEnderecoCobrancaDocumentoPelaCentralFinanceiro(Long abb01id) {
		TableMap tm = new TableMap();
		if (abb01id == null) return null;
		Abb01 abb01 = getSession().get(Abb01.class, "abb01id, abb01ent", abb01id);
		if (abb01.getAbb01ent() != null) {
			Abe0101 abe0101 = getSession().createCriteria(Abe0101.class)
					.addFields("abe0101id, abe0101endereco, abe0101numero, abe0101cep, abe0101bairro, abe0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
					.addJoin(Joins.fetch("abe0101municipio").left(true).partial(true).alias("aag0201"))
					.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
					.addWhere(Criterions.eq("abe0101ent", abb01.getAbb01ent().getAbe01id()))
					.addWhere(Criterions.eq("abe0101cobranca", Abe0101.SIM))
					.setOrder("abe0101id ASC").setMaxResults(1).get(ColumnType.ENTITY);
			if (abe0101 != null) {
				tm.put("endereco", abe0101.getAbe0101endereco());
				tm.put("numero", abe0101.getAbe0101numero());
				tm.put("cep", abe0101.getAbe0101cep());
				tm.put("bairro", abe0101.getAbe0101bairro());
				tm.put("municipio", abe0101.getAbe0101municipio() != null ? abe0101.getAbe0101municipio().getAag0201nome() : "");
				tm.put("uf", abe0101.getAbe0101municipio() != null  && abe0101.getAbe0101municipio().getAag0201uf() != null ? abe0101.getAbe0101municipio().getAag0201uf().getAag02uf() : "");
			} else {
				abe0101 = getSession().createCriteria(Abe0101.class)
						.addFields("abe0101id, abe0101endereco, abe0101numero, abe0101cep, abe0101bairro, abe0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
						.addJoin(Joins.fetch("abe0101municipio").left(true).partial(true).alias("aag0201"))
						.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
						.addWhere(Criterions.eq("abe0101ent", abb01.getAbb01ent().getAbe01id()))
						.addWhere(Criterions.eq("abe0101principal", Abe0101.SIM))
						.setOrder("abe0101id ASC").setMaxResults(1).get(ColumnType.ENTITY);
				if (abe0101 != null) {
					tm.put("endereco", abe0101.getAbe0101endereco());
					tm.put("numero", abe0101.getAbe0101numero());
					tm.put("cep", abe0101.getAbe0101cep());
					tm.put("bairro", abe0101.getAbe0101bairro());
					tm.put("municipio", abe0101.getAbe0101municipio() != null ? abe0101.getAbe0101municipio().getAag0201nome() : "");
					tm.put("uf", abe0101.getAbe0101municipio() != null  && abe0101.getAbe0101municipio().getAag0201uf() != null ? abe0101.getAbe0101municipio().getAag0201uf().getAag02uf() : "");
				}
			}
		}
		
		Long eaa01idCentral = getSession().createCriteria(Abb0102.class).addFields("abb0102central")
				.addWhere(Criterions.eq("abb0102doc", abb01id)).setMaxResults(1).get(ColumnType.LONG);
		if (eaa01idCentral == null) return tm;

		Long eaa01id = getSession().createCriteria(Eaa01.class).addFields("eaa01id")
				.addWhere(Criterions.eq("eaa01central", eaa01idCentral)).addWhere(getSamWhere().getCritPadrao(Eaa01.class))
				.get(ColumnType.LONG);
		if (eaa01id == null) return tm;

		Eaa0101 eaa0101 = getSession().createCriteria(Eaa0101.class)
				.addFields("eaa0101id, eaa0101endereco, eaa0101numero, eaa0101cep, eaa0101bairro, eaa0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
				.addJoin(Joins.fetch("eaa0101municipio").left(true).partial(true).alias("aag0201"))
				.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02")).addWhere(Criterions.eq("eaa0101doc", eaa01id))
				.addWhere(Criterions.eq("eaa0101cobranca", Eaa0101.SIM)).setOrder("eaa0101id ASC").setMaxResults(1)
				.get(ColumnType.ENTITY);
		if (eaa0101 != null) {
			tm.put("endereco", eaa0101.getEaa0101endereco());
			tm.put("numero", eaa0101.getEaa0101numero());
			tm.put("cep", eaa0101.getEaa0101cep());
			tm.put("bairro", eaa0101.getEaa0101bairro());
			tm.put("municipio", eaa0101.getEaa0101municipio() != null ? eaa0101.getEaa0101municipio().getAag0201nome() : "");
			tm.put("uf", eaa0101.getEaa0101municipio() != null  && eaa0101.getEaa0101municipio().getAag0201uf() != null ? eaa0101.getEaa0101municipio().getAag0201uf().getAag02uf() : "");
		} else {
			eaa0101 = getSession().createCriteria(Eaa0101.class)
					.addFields("eaa0101id, eaa0101endereco, eaa0101numero, eaa0101cep, eaa0101bairro, eaa0101municipio, aag0201.aag0201id, aag0201.aag0201nome, aag0201.aag0201uf, aag02.aag02id, aag02.aag02uf")
					.addJoin(Joins.fetch("eaa0101municipio").left(true).partial(true).alias("aag0201"))
					.addJoin(Joins.fetch("aag0201.aag0201uf").left(true).partial(true).alias("aag02"))
					.addWhere(Criterions.eq("eaa0101doc", eaa01id))
					.addWhere(Criterions.eq("eaa0101principal", Eaa0101.SIM)).setOrder("eaa0101id ASC").setMaxResults(1)
					.get(ColumnType.ENTITY);
			if (eaa0101 == null) return tm;
			tm.put("endereco", eaa0101.getEaa0101endereco());
			tm.put("numero", eaa0101.getEaa0101numero());
			tm.put("cep", eaa0101.getEaa0101cep());
			tm.put("bairro", eaa0101.getEaa0101bairro());
			tm.put("municipio", eaa0101.getEaa0101municipio() != null ? eaa0101.getEaa0101municipio().getAag0201nome() : "");
			tm.put("uf", eaa0101.getEaa0101municipio() != null  && eaa0101.getEaa0101municipio().getAag0201uf() != null ? eaa0101.getEaa0101municipio().getAag0201uf().getAag02uf() : "");
		}
		return tm;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDIifQ==