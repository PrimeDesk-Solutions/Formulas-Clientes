package Fast.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query
import br.com.multitec.utils.Utils;
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aah20
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0113
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

public class SRF_Danfe extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SRF - Danfe";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> tipo = getListLong("tipo");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidade = getListLong("entidade");
		LocalDate[] emissao = getIntervaloDatas("emissao");
		LocalDate[] entraSai = getIntervaloDatas("entraSai");
		Long id = getLong("eaa01id");

		List<TableMap> dados = new ArrayList();
		List<TableMap> itens = new ArrayList();

		List<Long> eaa01ids = null;
		if(id == null) {
			eaa01ids = buscarIdsDocumentos(tipo, numeroInicial, numeroFinal, entidade, emissao, entraSai);
		}else {
			eaa01ids = new ArrayList<>();
			eaa01ids.add(id)

			;
		}

		for(eaa01id in eaa01ids) {
			Eaa01 eaa01 = getSession().get(Eaa01.class, eaa01id);
			TableMap dadosNfe = new TableMap();

			dadosNfe.put("key", eaa01id)

			comporDadosEmpresa(dadosNfe, eaa01);
			comporDadosCentral(dadosNfe, eaa01);
			comporDadosDocumento(dadosNfe, eaa01);
			comporDadosEndereco(dadosNfe, eaa01);
			comporDadosGerais(dadosNfe, eaa01);
			comporDuplicatas(dadosNfe, eaa01.getEaa01central(), eaa01);
			comporValores(dadosNfe, eaa01);
			buscarPedidoCliente(dadosNfe,eaa01id);
			buscarMensagemItem(dadosNfe,eaa01id);

			dados.add(dadosNfe);

			List<TableMap> itensNfe = buscarItensdaNfe(eaa01id);
			for(TableMap tmItem : itensNfe){
				tmItem.put("key", eaa01id);
				itens.add(tmItem);
			}
		}

		params.put("empresaAtiva", obterEmpresaAtiva().getAac10id());

		// Cria os sub-relatórios
		TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
		dsPrincipal.addSubDataSource("dsItens", itens, "key", "key");
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SRF_Danfe_S1"));

		return gerarPDF("SRF_Danfe", dsPrincipal);
	}

	private List<Long> buscarIdsDocumentos(List<Long> tipo, Integer numeroInicial, Integer numeroFinal, List<Long> entidade, LocalDate[] emissao, LocalDate[] entraSai) {
		Criterion abb01data = emissao == null ? null : Criterions.between("abb01data", emissao[0], emissao[1]);
		Criterion abb01operData = entraSai == null ? null : Criterions.between("abb01operData", entraSai[0], entraSai[1]);
		Criterion abb01num = numeroInicial == null &&  numeroFinal == null ? null : Criterions.between("abb01num", numeroInicial, numeroFinal);
		Criterion aah01id = tipo != null && tipo.size() > 0 ? Criterions.in("aah01id", tipo) : null;
		Criterion abe01id = entidade != null && entidade.size() > 0 ? Criterions.in("abe01id", entidade) : null;

		return getSession().createCriteria(Eaa01.class)
				.addFields("eaa01id")
				.addJoin(Joins.join("Abb01", "abb01id = eaa01central"))
				.addJoin(Joins.join("Abe01", "abe01id = abb01ent"))
				.addJoin(Joins.join("Aah01", "aah01id = abb01tipo"))
				.addWhere(abb01data).addWhere(abb01operData)
				.addWhere(abb01num).addWhere(aah01id)
				.addWhere(abe01id).setOrder("abb01num")
				.addWhere(Criterions.eq("eaa01clasDoc", 1))
		//.addWhere(Criterions.eq(Fields.length("eaa01nfeChave"), 44))
				.addWhere(getSamWhere().getCritPadrao(Eaa01.class))
				.getList(ColumnType.LONG);
	}

	private void comporDadosEmpresa(TableMap dadosNfe, Eaa01 eaa01) {
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());

		if(aac10 == null) throw new ValidacaoException("Não foi possivel localizar os dados da empresa ativa.");
		if(aac10.getAac10municipio() == null) throw new ValidacaoException("Necessário informar o município no cadastro da empresa.");

		dadosNfe.put("aac10bairro", aac10.getAac10bairro());
		dadosNfe.put("aac10cep", aac10.getAac10cep());
		dadosNfe.put("aac10dddfone", aac10.getAac10dddFone());
		dadosNfe.put("aac10email", aac10.getAac10email());
		dadosNfe.put("aac10endereco", aac10.getAac10endereco());
		dadosNfe.put("aac10fone", aac10.getAac10fone());
		dadosNfe.put("aac10municipio", aac10.getAac10municipio().getAag0201nome());
		dadosNfe.put("aac10ni", aac10.getAac10ni());
		dadosNfe.put("aac10numero", aac10.getAac10numero());
		dadosNfe.put("aac10rs", aac10.getAac10rs());
		dadosNfe.put("aac10uf", aac10.getAac10municipio().getAag0201uf().getAag02uf());

		def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id);
		dadosNfe.put("aac10ie", ie)
	}

	private void comporDadosCentral(TableMap dadosNfe, Eaa01 eaa01) {
		Abb01 abb01 = getSession().get(Abb01.class, eaa01.getEaa01central().getAbb01id());
		if(abb01 == null) throw new ValidacaoException("Não foi possivel localizar os dados da Central do Documento");

		dadosNfe.put("abb01data", abb01.getAbb01data());
		dadosNfe.put("abb01num", abb01.getAbb01num());
		dadosNfe.put("abb01serie", abb01.getAbb01serie());
	}

	private void comporDadosDocumento(TableMap dadosNfe, Eaa01 eaa01) {
		dadosNfe.put("eaa01esData", eaa01.getEaa01esData());
		dadosNfe.put("eaa01esHora", eaa01.getEaa01esHora() != null ? eaa01.getEaa01esHora().format(DateTimeFormatter.ofPattern("HH:mm")) : null);
		dadosNfe.put("eaa01esMov", eaa01.getEaa01esMov());
		dadosNfe.put("eaa01id", eaa01.getEaa01id());
		dadosNfe.put("eaa01nfeChave", eaa01.getEaa01nfeChave());
		dadosNfe.put("eaa01nfeData", eaa01.getEaa01nfeData());
		dadosNfe.put("eaa01nfeData", eaa01.getEaa01nfeData());
		dadosNfe.put("eaa01nfeHora", eaa01.getEaa01nfeHora() != null ?eaa01.getEaa01nfeHora().format(DateTimeFormatter.ofPattern("HH:mm")) : null);
		dadosNfe.put("eaa01nfeProt", eaa01.getEaa01nfeProt());
		dadosNfe.put("eaa01obsContrib", eaa01.getEaa01obsContrib());
		dadosNfe.put("eaa01obsFisco", eaa01.getEaa01obsFisco());
		dadosNfe.put("eaa01operDescr", eaa01.getEaa01operDescr());
		dadosNfe.put("eaa01totItens", eaa01.getEaa01totItens());
		dadosNfe.put("eaa01totDoc", eaa01.getEaa01totDoc());
	}

	private void comporDadosEndereco(TableMap dadosNfe, Eaa01 eaa01) {
		Eaa0101 eaa0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", eaa01.getEaa01id()), Criterions.eq("eaa0101principal", 1));
		if(eaa0101 == null) throw new ValidacaoException("Não foi possivel localizar o endereço principal do documento.");
		if(eaa0101.getEaa0101municipio() == null) throw new ValidacaoException("Necessário informar o municipio no endereço principal do documento.");

		Aag02 aag02 = getSession().get(Aag02.class, eaa0101.getEaa0101municipio().getAag0201uf().getAag02id());
		if(aag02 == null) throw new ValidacaoException("Não foi informado a Unidade Federativa no endereço principal do documento.");

		dadosNfe.put("eaa0101bairro", eaa0101.getEaa0101bairro());
		dadosNfe.put("eaa0101cep", eaa0101.getEaa0101cep());
		dadosNfe.put("eaa0101complem", eaa0101.getEaa0101complem());
		dadosNfe.put("eaa0101ddd", eaa0101.getEaa0101ddd());
		dadosNfe.put("eaa0101endereco", eaa0101.getEaa0101endereco());
		dadosNfe.put("eaa0101fone", eaa0101.getEaa0101fone());
		dadosNfe.put("eaa0101ie", eaa0101.getEaa0101ie());
		dadosNfe.put("eaa0101municipio", eaa0101.getEaa0101municipio().getAag0201nome());
		dadosNfe.put("eaa0101numero", eaa0101.getEaa0101numero());
		dadosNfe.put("eaa0101uf", aag02.getAag02uf());
	}

	private void comporDadosGerais(TableMap dadosNfe, Eaa01 eaa01) {
		Eaa0102 eaa0102 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0102", Criterions.eq("eaa0102doc", eaa01.getEaa01id()));
		if(eaa0102 == null) throw new ValidacaoException("Não foi possivel localizar os dados gerais do documento.");
		TableMap Eaa01json = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap()

//		if(Eaa01json.getBigDecimal_Zero("volumes") >= 0) Eaa01json.put("volumes", new BigDecimal(Eaa01json.getInteger("volumes")));
//		if(Eaa01json.getBigDecimal_Zero("ipi") >= 0) Eaa01json.put("ipi", new BigDecimal(Eaa01json.getInteger("ipi")))
//		if(Eaa01json.getBigDecimal_Zero("bc_icms") >= 0) Eaa01json.put("bc_icms", (new BigDecimal(Eaa01json.get("bc_icms"))).round(2))
//		if(Eaa01json.getBigDecimal_Zero("icms") >= 0) Eaa01json.put("icms", new BigDecimal(Eaa01json.getInteger("icms")))
//		if(Eaa01json.getBigDecimal_Zero("bc_icms_st") >= 0) Eaa01json.put("bc_icms_st", new BigDecimal(Eaa01json.getInteger("bc_icms_st")))
//		if(Eaa01json.getBigDecimal_Zero("icms_st") >= 0) Eaa01json.put("icms_st", new BigDecimal(Eaa01json.getInteger("icms_st")))

		if(Eaa01json.getBigDecimal_Zero("volumes") >= 0) Eaa01json.put("volumes", (new BigDecimal(Eaa01json.getBigDecimal_Zero("volumes"))).round(2));
		if(Eaa01json.getBigDecimal_Zero("ipi") >= 0) Eaa01json.put("ipi", (new BigDecimal(Eaa01json.getBigDecimal_Zero("ipi"))).round(2));
		if(Eaa01json.getBigDecimal_Zero("bc_icms") >= 0) Eaa01json.put("bc_icms", (new BigDecimal(Eaa01json.getBigDecimal_Zero("bc_icms"))).round(2))
		if(Eaa01json.getBigDecimal_Zero("icms") >= 0) Eaa01json.put("icms", (new BigDecimal(Eaa01json.getBigDecimal_Zero("icms"))).round(2))
		if(Eaa01json.getBigDecimal_Zero("bc_icms_st") >= 0) Eaa01json.put("bc_icms_st", (new BigDecimal(Eaa01json.getBigDecimal_Zero("bc_icms_st"))).round(2))
		if(Eaa01json.getBigDecimal_Zero("icms_st") >= 0) Eaa01json.put("icms_st", (new BigDecimal(Eaa01json.getBigDecimal_Zero("icms_st"))).round(2))

		dadosNfe.put("eaa0102especie", eaa0102.getEaa0102especie());
		String frete = buscarTipoFrete(eaa0102.getEaa0102frete());
		dadosNfe.put("eaa0102frete", frete);
		dadosNfe.put("eaa0102ie", eaa0102.getEaa0102ie());
		dadosNfe.put("eaa0102ieST", eaa0102.getEaa0102ieST());
		dadosNfe.put("eaa0102marca", eaa0102.getEaa0102marca());
		dadosNfe.put("eaa0102ni", eaa0102.getEaa0102ni());
		dadosNfe.put("eaa0102nome", eaa0102.getEaa0102nome());
		dadosNfe.put("desconto",Eaa01json.getBigDecimal_Zero("desconto"));
		dadosNfe.putAll(Eaa01json)




		if(eaa0102.getEaa0102despacho() != null) comporDadosDespacho(dadosNfe, eaa0102.getEaa0102despacho().getAbe01id());
		if(eaa0102.getEaa0102redespacho() != null) comporDadosRedespacho(dadosNfe, eaa0102.getEaa0102redespacho().getAbe01id());
		if(eaa0102.getEaa0102veiculo() != null) comporDadosVeiculo(dadosNfe, eaa0102.getEaa0102veiculo().getAah20id());
	}

	private void comporDadosDespacho(TableMap dadosNfe, Long abe01id) {
		Abe01 abe01 = getSession().get(Abe01.class, abe01id);
		if(abe01 == null) throw new ValidacaoException("Necessário informar a Entidade do Despacho.");

		Abe0101 abe0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe01id", abe01id), Criterions.eq("abe0101principal", 1));
		if(abe0101 == null) throw new ValidacaoException("A Entidade informada no despacho não possui um endereço principal.")

		Aag02 uf = getSession().get(Aag02.class, abe0101.getAbe0101municipio().getAag0201uf().getAag02id());
		if(uf == null) throw new ValidacaoException("Necessário informar a Unidade Federativa do municipio no endereço principal do despacho.")

		dadosNfe.put("eaa0102despacho_endereco", abe0101.getAbe0101endereco());
		dadosNfe.put("eaa0102despacho_ie", abe01.getAbe01ie());
		dadosNfe.put("eaa0102despacho_municipio", abe0101.getAbe0101municipio().getAag0201nome());
		dadosNfe.put("eaa0102despacho_ni", abe01.getAbe01ni());
		dadosNfe.put("eaa0102despacho_nome", abe01.getAbe01nome());
		dadosNfe.put("eaa0102despacho_numero", abe0101.getAbe0101numero());
		dadosNfe.put("eaa0102despacho_uf", uf.getAag02uf());
		dadosNfe.put("eaa0102despacho_complem", abe0101.getAbe0101complem());
	}
	private void comporDadosRedespacho(TableMap dadosNfe, Long abe01id) {
		Abe01 abe01 = getSession().get(Abe01.class, abe01id);
		if(abe01 == null) throw new ValidacaoException("Necessário informar a Entidade do ReDespacho.");

		Abe0101 abe0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe01id", abe01id), Criterions.eq("abe0101entrega", 1));
		if(abe0101 == null) throw new ValidacaoException("A Entidade informada no redespacho não possui um endereço principal.")

		Aag02 uf = getSession().get(Aag02.class, abe0101.getAbe0101municipio().getAag0201uf().getAag02id());
		if(uf == null) throw new ValidacaoException("Necessário informar a Unidade Federativa do municipio no endereço principal do despacho.")

		dadosNfe.put("eaa0102redespacho_endereco", abe0101.getAbe0101endereco());
		dadosNfe.put("eaa0102redespacho_ie", abe01.getAbe01ie());
		dadosNfe.put("eaa0102redespacho_municipio", abe0101.getAbe0101municipio().getAag0201nome());
		dadosNfe.put("eaa0102redespacho_ni", abe01.getAbe01ni());
		dadosNfe.put("eaa0102redespacho_nome", abe01.getAbe01nome());
		dadosNfe.put("eaa0102redespacho_numero", abe0101.getAbe0101numero());
		dadosNfe.put("eaa0102redespacho_uf", uf.getAag02uf());
		dadosNfe.put("eaa0102redespacho_complem", abe0101.getAbe0101complem());
	}

	private void comporDadosVeiculo(TableMap dadosNfe, Long aah20id) {
		Aah20 aah20 = getSession().get(Aah20.class, aah20id);
		dadosNfe.put("eaa0102veiculo_placa", aah20.getAah20placa());
		dadosNfe.put("eaa0102veiculo_rntrc", aah20.getAah20rntrc());
		dadosNfe.put("eaa0102veiculo_ufPlaca", aah20.getAah20ufPlaca());
	}

	private void comporValores(TableMap dadosNfe, Eaa01 eaa01) {
		TableMap eaa01Json = eaa01.getEaa01json() != null ? eaa01.getEaa01json() : new TableMap();
		def vlrCargaTrib = eaa01Json.getBigDecimal_Zero("VlrCargaTrib");
		def totItens = eaa01.getEaa01totItens()
		if(vlrCargaTrib > 0 && totItens > 0){

			def totalTrib = (vlrCargaTrib / totItens) * 100;

			eaa01Json.put("total_impostos", vlrCargaTrib);
			eaa01Json.put("percent_impostos",totalTrib);
			if(eaa01Json == null) return;
			dadosNfe.putAll(eaa01Json);
		}
	}

	private List<TableMap> buscarItensdaNfe(Long id){
		return getSession().createQuery(" SELECT distinct eaa0103qtComl,eaa0103unit, eaa0103total, eaa0103seq as seq, eaa0103codigo, eaa0103descr, abg01codigo AS eaa0103ncm_codigo, aaj10codigo AS eaa0103cstIcms_codigo, aam06codigo as eaa0103umComl_codigo, aaj15codigo AS eaa0103cfop_codigo,  ",
				" CAST(eaa0103json ->>'bc_icms' AS numeric(18,6)) AS bc_icms, CAST(eaa0103json ->>'icms' AS numeric(18,6)) AS vlr_icms, CAST(eaa0103json ->>'ipi' AS numeric(18,6)) AS vlr_ipi,CAST(eaa0103json ->>'aliq_icms' AS numeric(18,2)) AS aliq_icms, CAST(eaa0103json ->>'ipi' AS numeric(18,2)) AS vlr_ipi,CAST(eaa0103json ->>'aliq_ipi' AS numeric(18,2)) AS aliq_ipi, ",
				"CAST(eaa0103json ->>'qt_convertida' AS numeric(18,2)) AS qtConvertida,CAST(eaa0103json ->>'umv' AS character varying(3)) AS umv,CAST(eaa0103json ->>'unitario_conv' AS numeric(18,2)) AS unitConv,CAST(eaa0103json ->>'peso_liquido' AS numeric(18,2)) AS pesoLiq,CAST(eaa0103json ->>'desconto' AS numeric(18,6)) AS desconto, eaa0103pcNum as pedCliente, ",
				"abb01num as numDocRemessa "+
						" FROM eaa0103 ",
				" LEFT JOIN abg01 ON abg01id = eaa0103ncm ",
				" LEFT JOIN aaj10 on aaj10id = eaa0103cstIcms ",
				" LEFT JOIN aaj15 on aaj15id = eaa0103cfop ",
				" LEFT JOIN aam06 on aam06id = eaa0103umComl ",
				" LEFT JOIN eaa01038 on eaa01038item = eaa0103id "+
						" LEFT JOIN abb01 on abb01id = eaa01038centralest "+
						" WHERE eaa0103doc = :id ",
				" ORDER BY eaa0103seq").setParameters("id",id)
				.getListTableMap();

	}

	private List<TableMap> buscarPedidoCliente(TableMap dadosNfe,Long eaa01id){
		String sql =  "select distinct abb01pedido.abb01num as numPed, eaa0103nota.eaa0103pcNum as pedCliente from eaa01032 "+
				"inner join eaa0103 as eaa0103pedido on eaa01032itemscv = eaa0103pedido.eaa0103id "+
				"inner join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc "+
				"inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central "+
				"full join eaa0103 as eaa0103nota on eaa01032itemsrf = eaa0103nota.eaa0103id "+
				"full join eaa01 as eaa01nota on eaa01nota.eaa01id = eaa0103nota.eaa0103doc "+
				"where eaa01nota.eaa01id = :eaa01id ";

		TableMap numPedido = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("eaa01id",eaa01id));

		if(numPedido != null) dadosNfe.putAll(numPedido);
	}

	private List<TableMap> buscarMensagemItem(TableMap dadosNfe,Long eaa01id){
		String sql =  "select distinct cast(eaa0103nota.eaa0103json ->> 'mensagem' as character varying(250)) as mensagem from eaa01032 "+
				"inner join eaa0103 as eaa0103pedido on eaa01032itemscv = eaa0103pedido.eaa0103id "+
				"inner join eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc "+
				"inner join abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central "+
				"full join eaa0103 as eaa0103nota on eaa01032itemsrf = eaa0103nota.eaa0103id "+
				"full join eaa01 as eaa01nota on eaa01nota.eaa01id = eaa0103nota.eaa0103doc "+
				"where eaa01nota.eaa01id = :eaa01id "+
				"and cast(eaa0103nota.eaa0103json ->> 'mensagem' as character varying(250)) is not null"

		TableMap mensagem = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("eaa01id",eaa01id));

		if(mensagem != null) dadosNfe.putAll(mensagem);
	}

	private void comporDuplicatas(TableMap dadosNfe, Abb01 abb01, Eaa01 eaa01) {
		abb01 = getSession().get(Abb01.class, abb01.getAbb01id());
		List<Eaa0113> eaa0113s = getSession().createCriteria(Eaa0113.class)
				.addWhere(Criterions.eq("eaa0113doc", eaa01.getEaa01id()))
				.addWhere(Criterions.eq("eaa0113clasParc", Eaa0113.CLASPARC_PARCELA_DO_DOCUMENTO))
				.setOrder("eaa0113dtVctoN").getList(ColumnType.ENTITY);

		if (!Utils.isEmpty(eaa0113s)) {
			for (int i = 0 ; i < eaa0113s.size(); i++) {
				dadosNfe.put("abb01num", abb01.getAbb01num());
				dadosNfe.put("parcela" + i, i+1)
				dadosNfe.put("data" + i, eaa0113s.get(i).getEaa0113dtVctoN());
				dadosNfe.put("valor" + i, eaa0113s.get(i).getEaa0113valor());
			}
		}
	}

	private String buscarUfEntidade(Long abe01id) {
		return getSession().createCriteria(Abe01.class).addFields("aag02uf")
				.addJoin(Joins.join("Abe0101", "abe0101ent = abe01id"))
				.addJoin(Joins.join("Aag0201", "abe0101municipio = aag0201id"))
				.addJoin(Joins.join("Aag02", "aag0201uf = aag02id"))
				.addWhere(Criterions.eq("abe0101principal", 1))
				.addWhere(Criterions.eq("abe01id", abe01id))
				.get(ColumnType.STRING);
	}

	private String buscarTipoFrete(Integer frete) {
		switch(frete) {
			case 0:
				"0-Remetente (CIF)";
				break;
			case 1:
				"1-Destinatário (FOB)";
				break;
			case 2:
				"2-Terceiros";
				break;
			case 3:
				"3-Próprio por conta do remetente";
				break;
			default:
				"4-Próprio por conta do destinatário";
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIEltcHJlc3PDo28gRG9jdW1lbnRvIChEQU5GZSkiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIEltcHJlc3PDo28gRG9jdW1lbnRvIChEQU5GZSkiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNSRiAtIERhbmZlIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNSRiAtIERhbmZlIiwidGlwbyI6InJlbGF0b3JpbyJ9