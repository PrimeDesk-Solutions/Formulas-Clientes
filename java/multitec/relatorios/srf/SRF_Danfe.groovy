package multitec.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType;
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
			eaa01ids.add(id);
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
			
			dados.add(dadosNfe);
			
			List<TableMap> itensNfe = buscarItensdaNfe(eaa01);
			for(item in itensNfe) {
				TableMap tmItens = new TableMap();
				tmItens.put("key", eaa01id);
				tmItens.putAll(item);
				itens.add(tmItens);
			}
		}
			
		// Cria os sub-relatórios
		TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
		dsPrincipal.addSubDataSource("dsItens", itens, "key", "key");
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SRF_Danfe_S1"))
						
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
						   .addWhere(Criterions.eq(Fields.length("eaa01nfeChave"), 44))
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
		
		dadosNfe.put("eaa0102especie", eaa0102.getEaa0102especie());
		String frete = buscarTipoFrete(eaa0102.getEaa0102frete());
		dadosNfe.put("eaa0102frete", frete);
		dadosNfe.put("eaa0102ie", eaa0102.getEaa0102ie());
		dadosNfe.put("eaa0102ieST", eaa0102.getEaa0102ieST());
		dadosNfe.put("eaa0102marca", eaa0102.getEaa0102marca());
		dadosNfe.put("eaa0102ni", eaa0102.getEaa0102ni());
		dadosNfe.put("eaa0102nome", eaa0102.getEaa0102nome());
		
		if(eaa0102.getEaa0102despacho() != null) comporDadosDespacho(dadosNfe, eaa0102.getEaa0102despacho().getAbe01id());
		
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
	
	private void comporDadosVeiculo(TableMap dadosNfe, Long aah20id) {
		Aah20 aah20 = getSession().get(Aah20.class, aah20id);
		dadosNfe.put("eaa0102veiculo_placa", aah20.getAah20placa());
		dadosNfe.put("eaa0102veiculo_rntrc", aah20.getAah20rntrc());
		dadosNfe.put("eaa0102veiculo_ufPlaca", aah20.getAah20ufPlaca());
	}
	
	private void comporValores(TableMap dadosNfe, Eaa01 eaa01) {
		TableMap eaa01Json = eaa01.getEaa01json();
		if(eaa01Json == null) return;
		dadosNfe.putAll(eaa01Json);
	}
	
	private List<TableMap> buscarItensdaNfe(Eaa01 eaa01){
		List<TableMap> dadosItens = new ArrayList();
		
		List<Eaa0103> eaa0103s = getSession().createCriteria(Eaa0103.class)
									.addJoin(Joins.fetch("eaa0103cfop"))
									.addJoin(Joins.fetch("eaa0103cstIcms"))
									.addJoin(Joins.fetch("eaa0103csosn").left(false))
									.addJoin(Joins.fetch("eaa0103ncm"))
									.addJoin(Joins.fetch("eaa0103umComl"))
									.addWhere(Criterions.lt("eaa0103retInd", 2))
									.addWhere(Criterions.eq("eaa0103doc", eaa01.getEaa01id()))
									.setOrder("eaa0103seq")
									.getList(ColumnType.ENTITY);
		
		for(Eaa0103 eaa0103 : eaa0103s) {
			TableMap dadosItensNfe = new TableMap();
			dadosItensNfe.put("seq", eaa0103.getEaa0103seq())
			dadosItensNfe.put("eaa0103cfop_codigo", eaa0103.getEaa0103cfop() == null ? null : eaa0103.getEaa0103cfop().getAaj15codigo());
			dadosItensNfe.put("eaa0103codigo", eaa0103.getEaa0103codigo());
			
			if(eaa0103.getEaa0103csosn() != null) {
				dadosItensNfe.put("eaa0103cstIcms_codigo", eaa0103.getEaa0103csosn().getAaj14codigo())
			}else{
				dadosItensNfe.put("eaa0103cstIcms_codigo", eaa0103.getEaa0103cstIcms() == null ? null : eaa0103.getEaa0103cstIcms().getAaj10codigo())
			}
			
			dadosItensNfe.put("eaa0103descr", eaa0103.getEaa0103descr());
			dadosItensNfe.put("eaa0103ncm_codigo", eaa0103.getEaa0103ncm() == null ? null : eaa0103.getEaa0103ncm().getAbg01codigo());
			dadosItensNfe.put("eaa0103qtComl", eaa0103.getEaa0103qtComl());
			dadosItensNfe.put("eaa0103total", eaa0103.getEaa0103total());
			dadosItensNfe.put("eaa0103umComl_codigo", eaa0103.getEaa0103umComl() == null ? null : eaa0103.getEaa0103umComl().getAam06codigo());
			dadosItensNfe.put("eaa0103unit", eaa0103.getEaa0103unit());
			if(eaa0103.getEaa0103json() != null)dadosItensNfe.putAll(eaa0103.getEaa0103json());
						
			dadosItens.add(dadosItensNfe);
		} 
		return dadosItens;
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
//meta-sis-eyJkZXNjciI6IlNSRiAtIERhbmZlIiwidGlwbyI6InJlbGF0b3JpbyJ9