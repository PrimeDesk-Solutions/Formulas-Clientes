package Atilatte.formulas.srf;

import sam.model.entities.ab.Abe03;

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.ab.Aba2001;
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb10
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abd02
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe02
import sam.model.entities.ab.Abe05
import sam.model.entities.ab.Abe40
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa01033
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro
import br.com.multiorm.criteria.criterion.Criterions
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;


class Documento extends FormulaBase {

	private Eaa01 eaa01;
	private Eaa0102 eaa0102;
	private Eaa0101 eaa0101princ;
	private String procInvoc;

	private Aag02 aag02;
	private Aag0201 aag0201;

	private Abb01 abb01;
	private Abb10 abb10;
	private Abe01 abe01;
	private Abd01 abd01;
	private Abe40 abe40;

	private Abe01 abe01rep0;
	private Abe01 abe01rep1;
	private Abe01 abe01rep2;
	private Abe01 abe01rep3;
	private Abe01 abe01rep4;

	@Override
	public void executar() {
		eaa01 = get("eaa01");
		procInvoc = get("procInvoc");


		abb01 = eaa01.eaa01central;
		if(abb01 == null) return;

		//Operação Comercial
		abb10 = abb01 != null &&  abb01.abb01operCod != null ? getSession().get(Abb10.class, abb01.abb01operCod.abb10id) : null;

		//PCD - Parâmetro de Cálculo de Documentos
		abd01 = eaa01.eaa01pcd;

		abe01 = abb01.abb01ent;

		abe40 = eaa01.eaa01tp;

		abe01rep0 = eaa01.eaa01rep0;
		abe01rep1 = eaa01.eaa01rep1;
		abe01rep2 = eaa01.eaa01rep2;
		abe01rep3 = eaa01.eaa01rep3;
		abe01rep4 = eaa01.eaa01rep4;

		setarObterTaxasComissaoDocumento();

		// Calculando campos json de visualização 0-Documento
		TableMap mapJson = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;

		if(eaa01.eaa01pcd != null && eaa01.eaa01pcd.abd01codigo == "10001") {
			def fidelidade = 0;
			fidelidade = eaa01.eaa01totDoc * 0.01;
			fidelidade = round(fidelidade, 2);
			mapJson.put("vlr_fidelidade", fidelidade);
		}

		// Cashback: Crédito lançado pela devolução de venda na emissão da NFe de Entrada
		if(eaa01.eaa01pcd != null && eaa01.eaa01pcd.abd01codigo == "11100") {
			def credito = mapJson.getBigDecimal("vlr_credito");
			credito = credito == null ? null : eaa01.eaa01totDoc;
			mapJson.put("vlr_credito", credito);
		}

		eaa01.eaa01json = mapJson;

		comporObservacoesDocumento();

		comporObsContribuinteComChaveNFeDocumentosReferenciados();

		calcularFrete()
	}

	private void setarObterTaxasComissaoDocumento() {
		if(eaa01.eaa01esMov == 0) return;

		if(abe01 == null) return;

		def txComis0 = eaa01.eaa01txComis0;
		def txComis1 = eaa01.eaa01txComis1;
		def txComis2 = eaa01.eaa01txComis2;
		def txComis3 = eaa01.eaa01txComis3;
		def txComis4 = eaa01.eaa01txComis4;

		//Obtendo taxas fixadas na entidade (cliente)
		def sql = " SELECT abe02txComis0, abe02txComis1, abe02txComis2, abe02txComis3, abe02txComis4" +
				" FROM Abe01" +
				" INNER JOIN Abe02 ON abe02ent = abe01id" +
				" WHERE abe01cli = 1" +
				" AND abe01id = :abe01id" +
				getSamWhere().getWherePadrao("AND", Abe02.class);

		TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abe01id", abe01.abe01id));

		if(tm != null) {
			if(txComis0 == null || txComis0 == 0) txComis0 = tm.getBigDecimal("abe02txComis0");
			if(txComis1 == null || txComis1 == 0) txComis1 = tm.getBigDecimal("abe02txComis1");
			if(txComis2 == null || txComis2 == 0) txComis2 = tm.getBigDecimal("abe02txComis2");
			if(txComis3 == null || txComis3 == 0) txComis3 = tm.getBigDecimal("abe02txComis3");
			if(txComis4 == null || txComis4 == 0) txComis4 = tm.getBigDecimal("abe02txComis4");
		}

		//Obtendo taxas fixadas na tabela de preço
		if(abe40 != null) {
			sql = " SELECT abe40txComis0, abe40txComis1, abe40txComis2, abe40txComis3, abe40txComis4" +
					" FROM Abe40" +
					" WHERE abe40id = :abe40id";

			tm = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abe40id", abe40.abe40id));

			if(tm != null) {
				if(txComis0 == null || txComis0 == 0) txComis0 = tm.getBigDecimal("abe40txComis0");
				if(txComis1 == null || txComis1 == 0) txComis1 = tm.getBigDecimal("abe40txComis1");
				if(txComis2 == null || txComis2 == 0) txComis2 = tm.getBigDecimal("abe40txComis2");
				if(txComis3 == null || txComis3 == 0) txComis3 = tm.getBigDecimal("abe40txComis3");
				if(txComis4 == null || txComis4 == 0) txComis4 = tm.getBigDecimal("abe40txComis4");
			}
		}

		//Obtendo taxas fixadas em cada representante do documento
		if(txComis0 == null || txComis0 == 0) txComis0 = obterTaxaDoRepresentante(abe01rep0);
		if(txComis1 == null || txComis1 == 0) txComis1 = obterTaxaDoRepresentante(abe01rep1);
		if(txComis2 == null || txComis2 == 0) txComis2 = obterTaxaDoRepresentante(abe01rep2);
		if(txComis3 == null || txComis3 == 0) txComis3 = obterTaxaDoRepresentante(abe01rep3);
		if(txComis4 == null || txComis4 == 0) txComis4 = obterTaxaDoRepresentante(abe01rep4);

		if(txComis0 == null) txComis0 = 0;
		if(txComis1 == null) txComis1 = 0;
		if(txComis2 == null) txComis2 = 0;
		if(txComis3 == null) txComis3 = 0;
		if(txComis4 == null) txComis4 = 0;

		eaa01.eaa01txComis0 = txComis0;
		eaa01.eaa01txComis1 = txComis1;
		eaa01.eaa01txComis2 = txComis2;
		eaa01.eaa01txComis3 = txComis3;
		eaa01.eaa01txComis4 = txComis4;
	}

	private def obterTaxaDoRepresentante(Abe01 abe01rep) {
		if(abe01rep == null) return 0;

		def sql = " SELECT abe05taxa" +
				" FROM Abe01" +
				" INNER JOIN Abe05 ON abe05ent = abe01id" +
				" WHERE abe01rep = 1" +
				" AND abe01id = :abe01id" +
				getSamWhere().getWherePadrao("AND", Abe05.class);

		def txComis = getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("abe01id", abe01rep.abe01id));
	}

	private void comporObservacoesDocumento() {

		if(abd01 == null) return;
		if(abd01 == null) return;

		//Endereço principal da entidade no documento
		for(Eaa0101 eaa0101 : eaa01.eaa0101s) {
			if (eaa0101.eaa0101principal == 1) {
				eaa0101princ = eaa0101;
			}
		}

		// Municpio da entidade
		aag0201 = eaa0101princ.eaa0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", eaa0101princ.eaa0101municipio.aag0201id)) : null;

		// UF da entidade
		aag02 = aag0201 != null ? getSession().get(Aag02.class, aag0201.aag0201uf.aag02id) : null;

		def sqlEntidade = " SELECT abe01id, abe02id, abe02obsUsoInt, abe02obsFisco, abe02obsContrib, abe02obsRetInd, abe02obsGerais, " +
				" abe03id, abe03obsUsoInt, abe03obsFisco, abe03obsContrib, abe03obsRetInd, abe03obsGerais " +
				" FROM Abe01 " +
				" LEFT JOIN Abe02 ON abe02ent = abe01id " +
				" LEFT JOIN Abe03 ON abe03ent = abe01id " +
				" WHERE abe01id = :abe01id " +
				getSamWhere().getWherePadrao("AND", Abe01.class);

		TableMap tmEntidade = getAcessoAoBanco().buscarUnicoTableMap(sqlEntidade, Parametro.criar("abe01id", abe01.abe01id));
		if(tmEntidade == null) return;

		def sqlPCD = " SELECT abd01id, abd01dce, abd01obsUsoInt, abd01obsGerais, " +
				" abd01ceFiscais, abd02id, abd02obsFisco, abd02obsContrib, " +
				" abd01ceIndustr, abd05id, abd05obsRetorno " +
				" FROM Abd01 " +
				" LEFT JOIN Abd02 ON abd01ceFiscais = abd02id " +
				" LEFT JOIN Abd05 ON abd01ceIndustr = abd05id " +
				" WHERE abd01id = :abd01id "
		getSamWhere().getWherePadrao("AND", Abd01.class);

		TableMap tmPCD = getAcessoAoBanco().buscarUnicoTableMap(sqlPCD, Parametro.criar("abd01id", abd01.abd01id));
		if(tmPCD == null) return;

		String obsUsoInt = tmPCD.getInteger("abd01dce") == 0 ? tmEntidade.getString("abe03obsUsoInt") : tmEntidade.getString("abe02obsUsoInt");
		String obsFisco = tmPCD.getInteger("abd01dce") == 0 ? tmEntidade.getString("abe03obsFisco") : tmEntidade.getString("abe02obsFisco");
		String obsContrib = tmPCD.getInteger("abd01dce") == 0 ? tmEntidade.getString("abe03obsContrib") : tmEntidade.getString("abe02obsContrib");
		String obsRetInd = tmPCD.getInteger("abd01dce") == 0 ? tmEntidade.getString("abe03obsRetInd") : tmEntidade.getString("abe02obsRetInd");
		String obsGerais = tmPCD.getInteger("abd01dce") == 0 ? tmEntidade.getString("abe03obsGerais") : tmEntidade.getString("abe02obsGerais");

		if(obsUsoInt == null) obsUsoInt = "";
		if(obsFisco == null) obsFisco = "";
		if(obsContrib == null) obsContrib = "";
		if(obsRetInd == null) obsRetInd = "";
		if(obsGerais == null) obsGerais = "";

		if(obsUsoInt.length() > 0) obsUsoInt = obsUsoInt + " ";
		if(obsFisco.length() > 0) obsFisco = obsFisco + " ";
		if(obsContrib.length() > 0) obsContrib = obsContrib + " ";
		if(obsRetInd.length() > 0) obsRetInd = obsRetInd + " ";
		if(obsGerais.length() > 0) obsGerais = obsGerais + " ";

		obsUsoInt = obsUsoInt + (tmPCD.getString("abd01obsUsoInt") != null ? tmPCD.getString("abd01obsUsoInt") : "");
		obsFisco = obsFisco + (tmPCD.getString("abd02obsFisco") != null ? tmPCD.getString("abd02obsFisco") : "");
		obsContrib = obsContrib + (tmPCD.getString("abd02obsContrib") != null ? tmPCD.getString("abd02obsContrib") : "");
		obsRetInd = obsRetInd + (tmPCD.getString("abd05obsRetorno") != null ? tmPCD.getString("abd05obsRetorno") : "");
		obsGerais = obsGerais + (tmPCD.getString("abd01obsGerais") != null ? tmPCD.getString("abd01obsGerais") : "");

		// Observação ICMS Dest
		if(eaa01.eaa01json.getBigDecimal_Zero("vlr_icms_dest") > 0){
			obsFisco =  obsFisco + "Valor referente ao ICMS Dest: R"+'$ '+ eaa01.eaa01json.getBigDecimal_Zero("vlr_icms_dest").round(2);
		}

		// Observação FCP
		if(eaa01.eaa01json.getBigDecimal_Zero("vlr_icms_fcp_") > 0){
			obsFisco =  obsFisco + "\nValor referente ao FCP: R"+'$ '+ eaa01.eaa01json.getBigDecimal_Zero("vlr_icms_fcp_").round(2);
		}

		if(eaa01.eaa01json.getBigDecimal_Zero("vlr_icms_dest") > 0){
			obsFisco = obsFisco + "\nValores totais do ICMS interestadual: DIFAL da UF Destino R" +'$ '+ eaa01.eaa01json.getBigDecimal_Zero("vlr_icms_dest").round(2)
		}

//		if(aag02.aag02uf == 'RJ'){
//			obsFisco = obsFisco + "\nSubstituição tributária suspensa conforme o Recurso extraordinário com agravo nº 1.487.482 do Decreto nº 48.039/2022. "
//		}


		if(eaa01.eaa01obsUsoInt == null || eaa01.eaa01obsUsoInt.length() == 0) eaa01.eaa01obsUsoInt = obsUsoInt;
		if(eaa01.eaa01obsFisco == null || eaa01.eaa01obsFisco.length() == 0) eaa01.eaa01obsFisco = obsFisco;
		if(eaa01.eaa01obsContrib == null || eaa01.eaa01obsContrib.length() == 0) eaa01.eaa01obsContrib = obsContrib;
		if(eaa01.eaa01obsRetInd == null || eaa01.eaa01obsRetInd.length() == 0) eaa01.eaa01obsRetInd = obsRetInd;
		if(eaa01.eaa01obsGerais == null || eaa01.eaa01obsGerais.length() == 0) eaa01.eaa01obsGerais = obsGerais;
	}

	private void comporObsContribuinteComChaveNFeDocumentosReferenciados() {
		String obsContrib = eaa01.eaa01obsContrib != null ? eaa01.eaa01obsContrib : "";

		if(obsContrib.contains("Documento(s) referenciado(s):")) return;

		Set<Long> eaa0103ids = new HashSet();
		if(eaa01.eaa0103s != null && eaa01.eaa0103s.size() > 0) {
			for(Eaa0103 eaa0103 : eaa01.eaa0103s) {
				for(Eaa01033 eaa01033 : eaa0103.eaa01033s) {
					eaa0103ids.add(eaa01033.eaa01033itemDoc.eaa0103id)
				}
			}
		}

		StringBuilder obs = new StringBuilder("");
		if(eaa0103ids.size() > 0) {
			def sql = " SELECT DISTINCT abb01num, abb01data, eaa01nfeChave " +
					" FROM Eaa0103 " +
					" INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
					" INNER JOIN Abb01 ON eaa01central = abb01id " +
					" WHERE eaa0103id IN (:eaa0103ids) " +
					getSamWhere().getWherePadrao("AND", Eaa01.class);

			List<TableMap> listTMDocs = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa0103ids", eaa0103ids));

			obs.append(" Documento(s) referenciado(s): ");
			for(TableMap tmDoc : listTMDocs) {
				obs.append("Nº: " + tmDoc.getString("abb01num"));
				obs.append(" Data: " + DateTimeFormatter.ofPattern("dd/MM/yyyy").format(tmDoc.getDate("abb01data")));
				if(tmDoc.getString("eaa01nfeChave") != null) {
					obs.append(" Chave de acesso: " + tmDoc.getString("eaa01nfeChave"));
				}
				obs.append(" ");
			}
		}

		eaa01.setEaa01obsContrib(obsContrib + obs.toString());
	}

	private calcularFrete(){

		//Dados Gerais do Documento
		for(Eaa0102 dadosGerais : eaa01.eaa0102s){
			eaa0102 = dadosGerais;
		}

		//Endereço principal da entidade no documento
		for(Eaa0101 eaa0101 : eaa01.eaa0101s) {
			if (eaa0101.eaa0101principal == 1) {
				eaa0101princ = eaa0101;
			}
		}

		// Municpio da entidade
		aag0201 = eaa0101princ.eaa0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", eaa0101princ.eaa0101municipio.aag0201id)) : null;

		// UF da entidade
		aag02 = aag0201 != null ? getSession().get(Aag02.class, aag0201.aag0201uf.aag02id) : null;

		// Campo Livre documento
		TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap()

		if(eaa0102.eaa0102redespacho != null){

			// Redespacho
			Abe01 abe01redesp = getSession().get(Abe01.class, eaa0102.eaa0102redespacho.abe01id);

			// Redespacho - Fornecedor
			Abe03 abe03 = getSession().get(Abe03.class, Criterions.eq("abe03ent", abe01redesp.abe01id));
			if(abe03 == null) interromper(abe01redesp.abe01codigo)
			if(eaa01.eaa01clasDoc == 1 && abe03.abe03frete != 1 ){
				// Calcula frete redespacho

				String codRedesp = abe01redesp.abe01codigo;
				String nomeRedesp = abe01redesp.abe01na;
				String municipioEntidade = aag0201.aag0201nome;

				def totalKgNF = jsonEaa01.getBigDecimal_Zero("peso_bruto");
				def totalNF = eaa01.eaa01totDoc;

				String descrRepositorio = "Frete - "+nomeRedesp.split()[0]+" Transp";
				descrRepositorio = descrRepositorio.toUpperCase().replace(" ","");


				// Formata o municpipio da transportadora para retirar os acentos
				municipioEntidade = formatarString(municipioEntidade.toUpperCase());

				// Busca campos livres do repositório da Transportadora
				String sql = " SELECT aba20id, aba2001json FROM aba2001 "+
						     " INNER JOIN aba20 ON aba2001rd = aba20id "+
						     " WHERE REPLACE(UPPER(aba20descr), ' ','') LIKE '%"+descrRepositorio+"%'"


				List<TableMap> listRepositorios = getAcessoAoBanco().buscarListaDeTableMap(sql);

				TableMap jsonRepositorio = new TableMap();

				if(listRepositorios.size() > 0){

					for(repositorio in listRepositorios){
						if(repositorio.getTableMap("aba2001json") != null){
							String municipioRepositorio = repositorio.getTableMap("aba2001json").getString("municipio");
							if(municipioRepositorio != null){
								municipioRepositorio = formatarString(municipioRepositorio.toUpperCase());
								if(municipioRepositorio == municipioEntidade){
									jsonRepositorio = repositorio.getTableMap("aba2001json");
								}
							}
						}
					}

					if(jsonRepositorio.size() == 0) interromper("Municío "+municipioEntidade+" não encontrado no repositório de dados " + descrRepositorio);

					def valorFrete = 0;
					def peso;
					def freteKg;
					def pedagioTotal = 0;

					def pesoMin = jsonRepositorio.getBigDecimal_Zero("peso_min");
					def freteFixo = jsonRepositorio.getBigDecimal_Zero("frete_kg_fixo") != null ? jsonRepositorio.getBigDecimal_Zero("frete_kg_fixo") : 0;
					def txEntrega = jsonRepositorio.getBigDecimal_Zero("tx_entrega") != null ? jsonRepositorio.getBigDecimal_Zero("tx_entrega") : 0;
					def adValorem = jsonRepositorio.getBigDecimal_Zero("ad_valorem") != null ? (jsonRepositorio.getBigDecimal_Zero("ad_valorem") / 100) * totalNF   : 0;
					def gris = jsonRepositorio.getBigDecimal_Zero("gris") != null ? jsonRepositorio.getBigDecimal_Zero("gris") / 100 : 0;
					def pedagio = jsonRepositorio.getBigDecimal_Zero("pedagio") != null ? jsonRepositorio.getBigDecimal_Zero("pedagio") : 0;
					def aliquota = jsonRepositorio.getBigDecimal_Zero("aliq_frete") != null ? jsonRepositorio.getBigDecimal_Zero("aliq_frete") / 100 : 0;
					def seguro = jsonRepositorio.getBigDecimal_Zero("seguro") != null ? jsonRepositorio.getBigDecimal_Zero("seguro") / 100 : 0;
					def icms = jsonRepositorio.getBigDecimal_Zero("icms") != null ? jsonRepositorio.getBigDecimal_Zero("icms") / 100 : 0;
					def frete1Bora = jsonRepositorio.getBigDecimal_Zero("frete_1_bora") != null ? jsonRepositorio.getBigDecimal_Zero("frete_1_bora") : 0;
					def frete2Bora = jsonRepositorio.getBigDecimal_Zero("frete_2_bora") != null ? jsonRepositorio.getBigDecimal_Zero("frete_2_bora") : 0;
					def frete3Bora = jsonRepositorio.getBigDecimal_Zero("frete_3_bora") != null ? jsonRepositorio.getBigDecimal_Zero("frete_3_bora") : 0;
					def frete1Centro = jsonRepositorio.getBigDecimal_Zero("frete_1_centro") != null ? jsonRepositorio.getBigDecimal_Zero("frete_1_centro") : 0;
					def frete2Centro = jsonRepositorio.getBigDecimal_Zero("frete_2_centro") != null ? jsonRepositorio.getBigDecimal_Zero("frete_2_centro") : 0;
					def frete3Centro = jsonRepositorio.getBigDecimal_Zero("frete_3_centro") != null ? jsonRepositorio.getBigDecimal_Zero("frete_3_centro") : 0;
					def freteMin = jsonRepositorio.getBigDecimal_Zero("frete_min") != null ?  jsonRepositorio.getBigDecimal_Zero("frete_min") : 0;
					def modalidade;

					freteKg = freteFixo;

					if(freteKg == 0){
						if(nomeRedesp.toUpperCase().contains("BORA")){
							if(totalKgNF < 1500){
								freteKg = frete1Bora;
							}else if(totalKgNF >= 1500 && totalKgNF < 2999){
								freteKg = frete2Bora;
							}else{
								freteKg = frete3Bora;
							}
						}else{
							if(totalKgNF <= 1000){
								freteKg = frete1Centro;
							}else if(totalKgNF >= 1001 && totalKgNF <= 2000){
								freteKg = frete2Centro;
							}else{
								freteKg = frete3Centro;
							}
						}
					}

					// Verifica se o peso da nota é maior que o peso mínimo da tabela de frete, caso maior, é considerado o da NF, caso menor, é considerado o peso minimo da tabela
					peso =  totalKgNF > pesoMin ? totalKgNF : pesoMin ;

					// Calculo pedágio
					pedagioTotal = pedagio * peso;

					// Calcula valor frete sem acrescentar as taxas
					valorFrete = peso * freteKg;

					// Acrescenta taxas ao valor do frete (txEntrega + adValorem + pedagio + aliquota + seguro + ICMS)
					valorFrete = valorFrete + txEntrega + adValorem + pedagioTotal +
							(valorFrete * aliquota)+
							(valorFrete * seguro)+
							(valorFrete * icms)+
							(valorFrete * gris);

					if(freteMin > 0){
						if(valorFrete < freteMin){
							valorFrete = freteMin;
						}
					}

					jsonEaa01.put("valor_frete_redesp",valorFrete);

					preencherFreteItens(eaa01,valorFrete);

					eaa01.eaa01json = jsonEaa01;
				}else{
					interromper("Não foi encontrado repositório de dados para calculo do frete ")
				}

			}
		}

	}

	private String formatarString(String municipio){

		if(municipio.contains("Ã")){
			municipio = municipio.replace("Ã","A");
		}
		if(municipio.contains("Á")){
			municipio = municipio.replace("Á","A");
		}
		if(municipio.contains("Â")){
			municipio = municipio.replace("Â","A");
		}
		if(municipio.contains("É")){
			municipio = municipio.replace("É","E");
		}
		if(municipio.contains("Ê")){
			municipio = municipio.replace("Ê","E");
		}
		if(municipio.contains("Í")){
			municipio = municipio.replace("Í","I");
		}
		if(municipio.contains("Õ")){
			municipio = municipio.replace("Õ","O");
		}
		if(municipio.contains("Ô")){
			municipio = municipio.replace("Ô","O");
		}
		if(municipio.contains("Ó")){
			municipio = municipio.replace("Ó","O");
		}
		if(municipio.contains("Ú")){
			municipio = municipio.replace("Ú","U");
		}
		if(municipio.contains("Ü")){
			municipio = municipio.replace("Ü","U");
		}
		if(municipio.contains("Ç")){
			municipio = municipio.replace("Ç","C");
		}



		return municipio;

	}

	private preencherFreteItens(Eaa01 eaa01, BigDecimal vlrFrete){

		BigDecimal vlrFreteDividido = vlrFrete / eaa01.eaa0103s.size();
		for(Eaa0103 eaa0103 in eaa01.eaa0103s){
			TableMap tmEaa0103 = eaa0103.eaa0103json;
			tmEaa0103.put("valor_frete_redesp", vlrFreteDividido)
		}
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_DOCUMENTOS;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjAifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjAifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjAifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjAifQ==