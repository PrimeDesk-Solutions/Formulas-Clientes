package Atilatte.formulas.neogrid;

import br.com.multiorm.Query
import java.time.LocalDate
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import sam.server.samdev.utils.Parametro
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo;
import sam.dto.srf.FormulaSRFCalculoDocumentoDto
import sam.server.samdev.formula.FormulaBase;
import sam.server.srf.service.SRFService
import java.text.SimpleDateFormat
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0113
import sam.model.entities.ab.Abm13;
import sam.model.entities.aa.Aam06;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;



import java.io.BufferedReader;
import java.io.FileReader;

public class importacaoNeoGrid extends FormulaBase {

	private Abm01 abm01;
	private Abm0101 abm0101;
	private Eaa0102 eaa0102;


	private Long abd01id;
	private LocalDate data;
	private MultipartFile arquivo;
	LocalDate eaa01dtEntrega;

	//Campos Livres
	private TableMap jsonAbe01;
	private TableMap jsonAbm0101;
	private TableMap jsonEaa0103;
	private TableMap jsonImport;




	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.IMPORTAR_DOCUMENTOS;
	}

	@Override
	public void executar() {

		abd01id = get("abd01id");
		data = get("data");
		arquivo = get("arquivo");
		jsonImport = get("json");

		List<Eaa01> eaa01s = new ArrayList<>();

		SRFService srfService = instanciarService(SRFService.class);

		File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
		arquivo.transferTo(file);

		FileReader arquivoLido = new FileReader(file);

		BufferedReader buffer = new BufferedReader(arquivoLido);

		//Armazena as linhas do arquivo para buscar informções nela mesma
		String line;

		//Numero do pedido do cliente
		String numeroPedido = "";

		//Armazena a mensagem que será exibida no documento
		String mensagem = "";

		//Define se a entidade é Caixa
		Integer unidadeCaixa;

		//Formato da Data a partir do TXT
		SimpleDateFormat formatoRecebido = new SimpleDateFormat("yyyyMMdd");

		//Formatador do tipo data
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

		//Define um formato para hora
		DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");

		int indexDoc = -1;

		while((line = buffer.readLine()) != null) {
			String codReg = line.substring(0,2);

			if(codReg == "01") { // 01 - "cabeça" do documento

				if(indexDoc >= 0) {
					Eaa01 eaa01 = eaa01s.get(indexDoc);
					srfService.executarFormulaSRFCalculoDocumento(new FormulaSRFCalculoDocumentoDto(eaa01.eaa01pcd.abd01frmItem, eaa01.eaa01pcd.abd01frmDoc, eaa01, null, "SCV3002", true));
					srfService.comporFinanceiroContabilidadeSeVazios(eaa01);
				}

				indexDoc++;

				//Pedido cliente 8 - 27
				numeroPedido = line.substring(8,27);

				//Tipo do pedido 5 - 7
				Integer tipo = Integer.valueOf(Integer.parseInt(line.substring(5,8)));
				switch(tipo.intValue()){
					case 0:
						mensagem = "000-Pedido com condições especiais";
						break;
					case 1:
						mensagem = "001-Pedido Normal";
						break;
					case 2:
						mensagem = "002-Pedido de Mercadoria Bonificada";
						break;
					case 3:
						mensagem = "003-Pedido de Consignação";
						break;
					case 4:
						mensagem = "004-Pedido Vendedor";
						break;
					case 5:
						mensagem = "005-Pedido Comprado";
						break;
					case 6:
						mensagem = "006-Pedido de Demonstração";
						break;
					default:
						mensagem = "";

				}

				//Hora Emissão
				String horaEmissaoTxt = line.substring(56,58) +":"+line.substring(58,60)+":00";
				LocalTime hora = null;

				try{
					//Converte a hora do txt para o formato localTime
					hora = LocalTime.parse(horaEmissaoTxt, formatoHora);
				}catch(Exception e){
					interromper(e.getMessage())
				}

				//Data Emissao 48-59
				String dataEmissaoTxt = line.substring(48, 56);
				Date dataEmissao = formatoRecebido.parse(dataEmissaoTxt);
				LocalDate abb01data = DateUtils.parseDate(formato.format(dataEmissao), "dd/MM/yyyy");

				//Data Entrega Final 72-83
				String dataEntregaTxt = line.substring(72, 80);
				Date dataEntrega = formatoRecebido.parse(dataEntregaTxt);
				eaa01dtEntrega = DateUtils.parseDate(formato.format(dataEntrega), "dd/MM/yyyy");

				//Entidade 180-194
				String numInscricaoTxt = line.substring(180, 194);
				String numInscricaoFormatado = formatarNI(numInscricaoTxt);

				//Buscar Entidade por Código
				Abe01 abe01 = getSession().createCriteria(Abe01.class)
						.addWhere(Criterions.eq("abe01ni", numInscricaoFormatado))
						.addWhere(getSamWhere().getCritPadrao(Abe01.class))
						.get();
				if(abe01 == null) interromper("Não foi possível localizar a entidade a partir do CNPJ " + numInscricaoFormatado);

				//Campos Livre Entidade
				jsonAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();

				//Define o valor do campo unidade caixa no cadastro do item
				unidadeCaixa = jsonAbe01.getBigDecimal_Zero("unidade_caixa");

				Eaa01 eaa01 = srfService.comporDocumentoPadrao(abe01.abe01id, abd01id, null);

				//Tipo do frete no arquivo txt
				String tipoFrete = line.substring(269,272);

				//Dados da Entidade no documento
				for(Eaa0102 dadosGerais : eaa01.eaa0102s) {
					eaa0102 = dadosGerais;
				}

				//Define o tipo do frete
				if(tipoFrete == "CIF"){
					eaa0102.eaa0102frete = 0;
				}else{
					eaa0102.eaa0102frete = 1;
				}

				//Define como observação do documento o tipo do pedido
				eaa01.eaa01obsUsoInt = mensagem;

				eaa01.eaa01dtEntrega = eaa01dtEntrega;

				Abb01 abb01 = eaa01.eaa01central;

				//abb01.abb01num = abb01num;
				abb01.abb01data = abb01data;
				abb01.abb01operHora = hora;
				abb01.abb01operAutor = "SCV3002";


				eaa01s.add(eaa01);

			}
			//REGISTRO 02 - financeiro Documento
//			if(codReg.equals("02")){
//				Eaa01 eaa01 = eaa01s.get(indexDoc);

//			}

			//REGISTRO 04 - Itens do Documento
			if(codReg.equals("04")){

				Eaa01 eaa01 = eaa01s.get(indexDoc);

				//Tipo do codigo do Produto - EN ou UP
				String tipoCodProduto = line.substring(14,17).trim();
				if(tipoCodProduto != null){
					//Código EAN do produto
					String codigoEanProduto = line.substring(17,31).trim();


					if(tipoCodProduto.equals("EN")){
						//Buscar Item Pelo EAN
						abm01 = getSession().get(Abm01.class, Criterions.where("abm01gtin like '%"+codigoEanProduto+"' and abm01tipo = 1 "));
						
//						if(abm01 == null){
//							if(jsonImport.getInteger("desconsidera_item") == 1) continue;
//							throw new ValidacaoException("Não foi possível localizar o item pelo EAN "+codigoEanProduto+ " importado")
//						}
						if(abm01 == null){
							abm0101 = getSession().get(Abm0101.class, Criterions.where("abm0101gtintrib like '%" + codigoEanProduto+"' "));

							if(abm0101 == null) abm0101 = getSession().get(Abm0101.class, Criterions.where("cast(abm0101json ->> 'descricao_livre' as text) like '%" + codigoEanProduto+"' "));

							if(abm0101 == null){
								if(jsonImport.getInteger("desconsidera_item") == 1) continue;
								throw new ValidacaoException("Não foi possível localizar o item pelo EAN "+codigoEanProduto+ " importado")
							}
							abm01 = getSession().get(Abm01.class, abm0101.abm0101item.abm01id);
						}else{
							abm0101 = getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " +obterEmpresaAtiva().aac10id ));
						}
						
					}

					//Dados Comerciais do item
					Abm13 abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

					//Unidade de Medida
					Aam06 aam06 = abm13 != null &&  abm13.abm13umv != null ? getSession().get(Aam06.class, abm13.abm13umv.aam06id) : null;


					if(abm01.abm01di == null){

						//Configurando Spred dos itens
						Eaa0103 eaa0103 = srfService.comporItemDoDocumentoPadrao(eaa01, abm01.abm01id);

						//Campos Livres na Spread dos Itens
						jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();

						//Campo Livre itens
						jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();

						//Define o grupo do item
						def codItem = abm01.abm01codigo;
						Query descrCriterios = getSession().createQuery("select aba3001descr from aba3001 "+
								"inner join abm0102 on abm0102criterio = aba3001id and aba3001criterio = 542858 " +
								"inner join abm01 on abm0102item = abm01id "+
								"where abm01codigo = '"+codItem+"'" +
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

						//Quantidade
						BigDecimal eaa0103qtComl = new BigDecimal(line.substring(99,114)) / 100;

						//Converte Quantidade comercial para caixa, caso a entidade estiver caraterizada como unidade caixa
						if(unidadeCaixa == 1){
							if(grupo == "Iogurte"){
								eaa0103qtComl = eaa0103qtComl * jsonAbm0101.getBigDecimal_Zero("cvdnf");
							}
							if(grupo == "Leite"){
								if(abm01.abm01codigo == "0101002"){
									if(jsonAbm0101.getBigDecimal_Zero("volume_caixa") == 0 || jsonAbm0101.getBigDecimal_Zero("volume_caixa") == null) throw new ValidacaoException("O volume caixa no cadastro do item " + abm01.abm01codigo + " é inválido. ");
									eaa0103qtComl = eaa0103qtComl * jsonAbm0101.getBigDecimal_Zero("volume_caixa");
								}else{
									if(jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0 || jsonAbm0101.getBigDecimal_Zero("cvdnf") == null) throw new ValidacaoException("A capacidade volumétrica no cadastro do item " + abm01.abm01codigo + " é inválido.");
									eaa0103qtComl = eaa0103qtComl * jsonAbm0101.getBigDecimal_Zero("cvdnf");
								}
							}

							if(grupo == "Queijo"){
								if(aam06.aam06codigo == 'UN'){
									if(jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0 || jsonAbm0101.getBigDecimal_Zero("cvdnf") == null) throw new ValidacaoException("A capacidade volumétrica no cadastro do item " + abm01.abm01codigo + " é inválida.")
									eaa0103qtComl = eaa0103qtComl * jsonAbm0101.getBigDecimal_Zero("cvdnf");
								}
//								if(aam06.aam06codigo == 'KG'){
//									if(jsonAbm0101.getBigDecimal_Zero("peso_caixa") == 0 || jsonAbm0101.getBigDecimal_Zero("peso_caixa") == null) throw new ValidacaoException("O peso caixa no cadastro do item " + abm01.abm01codigo + " é inválido.");
//									eaa0103qtComl = eaa0103qtComl * jsonAbm0101.getBigDecimal_Zero("peso_caixa");
//								}
							}
						}

						//Sequencia dos Itens
						Integer eaa0103seq = Integer.parseInt(line.substring(2,6).trim());

						//Define sequencia dos itens
						eaa0103.eaa0103seq = eaa0103seq;
						//Define número do pedido do cliente
						eaa0103.eaa0103pcNum = numeroPedido;
						//Define a sequencia do pedido do cliente
						eaa0103.eaa0103pcSeq = eaa0103seq;
						//Quantidade Faturamento
						eaa0103.eaa0103qtComl = eaa0103qtComl;
						//Define a data de entrega nos Itens
						eaa0103.eaa0103dtEntrega = eaa01dtEntrega;

						eaa01.addToEaa0103s(eaa0103);
					}
				}
			}
		}
		buffer.close();
		arquivoLido.close();

		if(indexDoc >= 0) {
			Eaa01 eaa01 = eaa01s.get(indexDoc);
			srfService.executarFormulaSRFCalculoDocumento(new FormulaSRFCalculoDocumentoDto(eaa01.eaa01pcd.abd01frmItem, eaa01.eaa01pcd.abd01frmDoc, eaa01, null, "SCV3002", true));
			srfService.comporFinanceiroContabilidadeSeVazios(eaa01);
		}

		put("eaa01s", eaa01s);

	}

	private String formatarNI(String ni){
//		String numInscricaoFormatado = "";
		if(ni.length() == 11){
			return ni = ni.substring(0, 3) + "."+ ni.substring(3,6)+"."+ni.substring(6,9) +"-"+ni.substring(9,11);
		}

		if(ni.length() == 14){
			return ni = ni.substring(0,2)+"." + ni.substring(2,5) + "." + ni.substring(5,8) + "/" + ni.substring(8,12) + "-" + ni.substring(12,14)
		}
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjYifQ==