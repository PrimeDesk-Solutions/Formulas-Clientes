// Oficial
package Atilatte.interceptador;

import sam.model.entities.ab.Abm70;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ba.Bab01;
import sam.model.entities.ab.Abb01;
import sam.model.entities.ab.Abm01;
import sam.model.entities.aa.Aam06;
import br.com.multiorm.ORMInterceptor

import javax.xml.validation.Validator;
import java.util.List;
import br.com.multiorm.Session;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multiorm.Query
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.Utils;



public class CGS_Etiquetas implements ORMInterceptor<sam.model.entities.ab.Abm70> {

	@Override
	public Class<sam.model.entities.ab.Abm70> getEntityClass() {
		return sam.model.entities.ab.Abm70.class;
	}
	@Override
	public void prePersist(sam.model.entities.ab.Abm70 tabela, Session s) {

		// Valida se a quantidade da etiqueta é correspondente a campacitdade de caixa do item
		if(Utils.campoEstaCarregado(tabela, "abm70item")) validarQtdEtiqueta(tabela, s);

		Abm70 abm70 = s.createCriteria(Abm70.class).addWhere(Criterions.eq("abm70id",tabela.abm70id)).get();
		if(tabela.abm70id == null){
			Abb01 abb01;
			Bab01 bab01;
			Abm01 abm01;
			Abm0101 abm0101;
			if(tabela.abm70validade == null){

				if(tabela.abm70central != null ){

					//Central de documentos
					abb01 = tabela.abm70central;

					//Documento de Produção
					bab01 = s.createCriteria(Bab01.class).addWhere(Criterions.eq("bab01central",abb01.abb01id)).get();

					//Item 
					abm01 = s.createCriteria(Abm01.class).addWhere(Criterions.eq("abm01id",tabela.abm70item.abm01id)).get();

					//Dias Validade Item
					Query query = s.createQuery("select abm14validdias "+
							"from abm01 "+
							"inner join abm0101 on abm0101item = abm01id "+
							"inner join abm14 on abm0101producao = abm14id "+
							"and abm01codigo = :codItem "+
							"and abm01tipo = 1");

					query.setParameter("codItem", abm01.abm01codigo);

					def diasValid = query.getUniqueResult(ColumnType.INTEGER);

					if(diasValid == null)  throw new ValidacaoException("O Item "+ abm01.abm01codigo + " encontra-se sem dias para calculo de validade informado no cadastro.")

					//Formatador para Date
					SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

					//Formatador para LocalDate
					DateTimeFormatter formatterLocalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

					//Converte a data de fabricacao para o objeto Date
					if(bab01.bab01dtE == null) throw new ValidacaoException("A ordem selecionada encontra-se sem uma data de fabricação informada")
					Date dataInicial = formato.parse(bab01.bab01dtE.toString());

					//Criar uma instancia calndario e define a data inicial
					Calendar calendario = Calendar.getInstance();

					calendario.setTime(dataInicial);

					//Soma a data de fabricação com os dias de validade do item
					calendario.add(Calendar.DAY_OF_MONTH, diasValid);

					//Obtém a nova data calculada
					Date dataFinal = calendario.getTime();

					//Converte a nova data calculada para String
					String dataFinalString = formato.format(dataFinal);

					//Define a data de validade da etiqueta
					LocalDate dtValidade = LocalDate.parse(dataFinalString, formatterLocalDate);

					tabela.abm70fabric = bab01.bab01dtE;

					tabela.abm70validade = dtValidade;
				}

			}

			//Item 
			abm01 = s.createCriteria(Abm01.class).addWhere(Criterions.eq("abm01id",tabela.abm70item.abm01id)).get();

			// Unidade de Medida do Item
			Aam06 aam06 = abm01.abm01umu != null ? s.createCriteria(Aam06.class).addWhere(Criterions.eq("aam06id",abm01.abm01umu.aam06id)).get() : null;

			// ITens Valores
			abm0101 = s.createCriteria(Abm0101.class).addWhere(Criterions.eq("abm0101item",abm01.abm01id)).get();

			// Campos Livres Itens
			TableMap jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();

			//def taraTotal = (jsonAbm0101.getBigDecimal_Zero("cvdnf")  * jsonAbm0101.getBigDecimal_Zero("tara_emb_")) + jsonAbm0101.getBigDecimal_Zero("tara_caixa");

			def codItem = abm01.abm01codigo;

			Query descrCriterios = s.createQuery("select aba3001descr from aba3001 "+
					"inner join abm0102 on abm0102criterio = aba3001id and aba3001criterio = 542858 " +
					"inner join abm01 on abm0102item = abm01id "+
					"where abm01codigo = '"+codItem+"'"+
					"and abm01tipo = 1 ");

			List<TableMap> listCriterios = descrCriterios.getListTableMap();

			String grupo = "";

			for(TableMap criterio : listCriterios){
				if(criterio.getString("aba3001descr").contains("Queijo") ){
					grupo = criterio.getString("aba3001descr");
				}
				if(criterio.getString("aba3001descr").contains("Leite")){
					grupo = criterio.getString("aba3001descr");
				}

				if(criterio.getString("aba3001descr").contains("Iogurte") || criterio.getString("aba3001descr").contains("Baunilha")){
					grupo = criterio.getString("aba3001descr");
				}

			}


//			if(grupo.toUpperCase() == "QUEIJO" && aam06.aam06codigo == 'KG'){
//				// TableMap com os campos livres da tabela
//				TableMap mapJson = new TableMap()
//
//				mapJson.put("peso_bruto", tabela.abm70qt);
//
//				tabela.setAbm70json(mapJson);
//
//				tabela.abm70qt = tabela.abm70qt - taraTotal;
//			}

			if(grupo.toUpperCase() == "QUEIJO" && aam06.aam06codigo == 'KG'){
				def taraEmb = jsonAbm0101.getBigDecimal_Zero("tara_emb_") * jsonAbm0101.getBigDecimal_Zero("cvdnf");
				def taraCaixa = jsonAbm0101.getBigDecimal_Zero("tara_caixa");
				def taraTotal = taraEmb + taraCaixa;

				// TableMap com os campos livres da tabela
				TableMap mapJson = tabela.abm70json != null ? tabela.abm70json : new TableMap()

				mapJson.put("peso_bruto", tabela.abm70qt);

				tabela.abm70qt = tabela.abm70qt - taraTotal;

				tabela.setAbm70json(mapJson);
			}
		}


	}

	private void validarQtdEtiqueta(Abm70 abm70, Session s){

		// Item da etiqueta
		Abm01 abm01 = s.createCriteria(Abm01.class).addWhere(Criterions.eq("abm01id", abm70.abm70item.abm01id)).get();

		if(abm01 == null) throw new ValidacaoException("Interceptador: Não é permitido salvar etiqueta sem o item informado.");

		// Itens Configurações
		Abm0101 abm0101 = s.createCriteria(Abm0101.class).addWhere(Criterions.eq("abm0101item", abm01.abm01id )).get();

		// Unidade de Medida
		Aam06 aam06 = s.createCriteria(Aam06.class).addWhere(Criterions.eq("aam06id", abm01.abm01umu.aam06id)).get();

		// Campos Livres
		TableMap jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		TableMap jsonAbm70 = abm70.abm70json != null ? abm70.abm70json : new TableMap();


		BigDecimal quantidadeEtiqueta = abm70.abm70qt;

		Integer capacidadeItem = jsonAbm0101.getInteger("cvdnf");

		if(jsonAbm70.getString("modelo_etiqueta") != "005"){ // Etiqueta de pallet avulsa, permite quantidade acima da capacidade do item
			if(!aam06.aam06codigo.equals("KG")){
				if(quantidadeEtiqueta > capacidadeItem ) throw new ValidacaoException("A quantidade da etiqueta não pode ser maior que a capacidade da caixa do item. CAPACIDADE DO ITEM: " + capacidadeItem.toString())
			}
		}
	}
	@Override
	public void posPersist(sam.model.entities.ab.Abm70 entity, Session s) {
	}
	@Override
	public void preDelete(List<Long> ids, Session s) {
	}
}
//meta-sis-eyJ0aXBvIjoiaW50ZXJjZXB0b3IiLCJlbnRpdHkiOiJzYW0ubW9kZWwuZW50aXRpZXMuYWIuQWJtNzAifQ==