package Atilatte.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.model.entities.aa.Aac10;

class SRF_ItensPorRepresentantes extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SRF - Itens Por Representantes";
	}
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
		filtrosDefault.put("total1", true)
		filtrosDefault.put("total2", true)
		filtrosDefault.put("total3", true)
		filtrosDefault.put("total4", true)
		filtrosDefault.put("total5", true)
		filtrosDefault.put("total6", true)
		filtrosDefault.put("chkDevolucao", true)
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("resumoOperacao", "0");
		filtrosDefault.put("impressao", "0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer resumoOperacao = getInteger("resumoOperacao");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idsTransps = getListLong("transportadora");
        List<Long> idsItens = getListLong("itens")
        Integer optionTransp = getInteger("optionTransp");
        Integer optionResumo = getInteger("resumo");
        boolean totalizar1 = getBoolean("total1");
        boolean totalizar2 = getBoolean("total2");
        boolean totalizar3 = getBoolean("total3");
        boolean totalizar4 = getBoolean("total4");
        boolean totalizar5 = getBoolean("total5");
        boolean totalizar6 = getBoolean("total6");
        String campoFixo1 = getString("campoFixo1");
        String campoFixo2 = getString("campoFixo2");
        String campoFixo3 = getString("campoFixo3");
        String campoFixo4 = getString("campoFixo4");
        String campoFixo5 = getString("campoFixo5");
        String campoFixo6 = getString("campoFixo6");
        String campoLivre1 = getString("campoLivre1");
        String campoLivre2 = getString("campoLivre2");
        String campoLivre3 = getString("campoLivre3");
        String campoLivre4 = getString("campoLivre4");
        String campoLivre5 = getString("campoLivre5");
        String campoLivre6 = getString("campoLivre6");
        Integer impressao = getInteger("impressao");
        Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;
        boolean chkDevolucao = getBoolean("chkDevolucao");

		if(campoLivre1 != null && campoFixo1 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre2 != null && campoFixo2 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre3 != null && campoFixo3 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre4 != null && campoFixo4 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre5 != null && campoFixo5 != null) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre6 != null && campoFixo6 != null) interromper("Selecione apenas 1 valor por campo!")
		
		Map<String,String> campos = new HashMap()
		campos.put("1", campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null   )
		campos.put("2", campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null   )
		campos.put("3", campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null   )
		campos.put("4", campoLivre4 != null ? campoLivre4 : campoFixo4 != null ? campoFixo4 : null   )
		campos.put("5", campoLivre5 != null ? campoLivre5 : campoFixo5 != null ? campoFixo5 : null   )
		campos.put("6", campoLivre6 != null ? campoLivre6 : campoFixo6 != null ? campoFixo6 : null   )
		
		String periodo = ""
		if(dataEmissao != null) {
			periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}else if(dataEntSai) {
			periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
		}
		
		params.put("empresa", obterEmpresaAtiva().getAac10codigo()+"-"+obterEmpresaAtiva().getAac10na())
        params.put("title","Itens Por Representantes")
		params.put("periodo",periodo)
		params.put("totalizar1", totalizar1)
		params.put("totalizar2", totalizar2)
		params.put("totalizar3", totalizar3)
		params.put("totalizar4", totalizar4)
		params.put("totalizar5", totalizar5)
		params.put("totalizar6", totalizar6)
		
        List<TableMap> dados = buscarItensDoc(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,optionResumo,idsItens,idEmpresa,idsTransps);		

		for(dado in dados) {
			Long itemId = dado.getLong("abm01id");
            Long idRep = dado.getLong("idRep");
			List<TableMap> tableMapJson= buscarCamposLivres(idRep,itemId,numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,optionResumo,idsItens,idEmpresa,idsTransps);		
			TableMap jsonTotal = new TableMap()
            
			
			for(tm in tableMapJson) {
				TableMap json = tm.getTableMap("eaa0103json")
				Long itemNotaId = tm.getLong("eaa0103id")
				TableMap devolucao = obterDevolucao(itemNotaId,idRep)
				if(campoLivre1 != null) jsonTotal.put(campoLivre1, jsonTotal.getBigDecimal_Zero(campoLivre1) + json.getBigDecimal_Zero(campoLivre1))
				if(campoLivre2 != null) jsonTotal.put(campoLivre2, jsonTotal.getBigDecimal_Zero(campoLivre2) + json.getBigDecimal_Zero(campoLivre2))
				if(campoLivre3 != null) jsonTotal.put(campoLivre3, jsonTotal.getBigDecimal_Zero(campoLivre3) + json.getBigDecimal_Zero(campoLivre3))
				if(campoLivre4 != null) jsonTotal.put(campoLivre4, jsonTotal.getBigDecimal_Zero(campoLivre4) + json.getBigDecimal_Zero(campoLivre4))
				if(campoLivre5 != null) jsonTotal.put(campoLivre5, jsonTotal.getBigDecimal_Zero(campoLivre5) + json.getBigDecimal_Zero(campoLivre5))
				if(campoLivre6 != null) jsonTotal.put(campoLivre6, jsonTotal.getBigDecimal_Zero(campoLivre6) + json.getBigDecimal_Zero(campoLivre6))
				if(devolucao != null && chkDevolucao) {
					comporDevolucao(dado, devolucao, jsonTotal, [campoLivre1,campoLivre2,campoLivre3,campoLivre4,campoLivre5,campoLivre6])
				}
			}
			dado.putAll(jsonTotal)
			buscarCampo(dado, campos)
		}
		
		if(impressao == 0) return gerarPDF("SRF_ItensPorRepresentantes(PDF)",dados);
        return gerarXLSX("SRF_ItensPorRepresentantes(Excel)",dados);

	}
	
	private List<TableMap> buscarItensDoc(Integer numDocIni, Integer numDocFin, List<Long>idsTipoDoc, List<Long>idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long>idsEntidades,Integer optionResumo,List<Long>idsItens,Long idEmpresa,List<Long>idsTransps){
		
		String whereNumIni = numDocIni != null ? "and abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereTransp = (idTransp != null && idTransp.size() > 0) && optionTransp == 0 ? "and desp.abe01id in (:idTransp) " : (idTransp != null && idTransp.size() > 0) && optionTransp == 1 ?"and redesp.abe01id in (:idTransp) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "and abm01id in (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
       

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTransp = idTransp != null && idTransp.size() > 0 ? Parametro.criar("idTransp", idTransp) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);

		
		
		String sql =   "select rep.abe01id as idRep,abm01id,rep.abe01codigo as codRep, rep.abe01na as naRep,aam06codigo,abm01codigo, abm01na,case when abm01tipo = 0 then 'M' when abm01tipo = 1 then 'P' when abm01tipo = 2 then 'S' else 'MER' end as mps, "+
                        "sum(eaa0103qtUso) as eaa0103qtuso,sum(eaa0103qtComl) as eaa0103qtcoml,sum(eaa0103unit) as eaa0103unit,sum(eaa0103total) as eaa0103total,sum(eaa0103qtComl) as eaa0103totdoc,sum(eaa0103totFinanc) as eaa0103totfinanc "+
                        "from eaa01 "+
                        "inner join abb01 on abb01id = eaa01central "+
                        "inner join eaa0103 on eaa0103doc = eaa01id "+
                        "inner join abm01 on abm01id = eaa0103item "+
                        "inner join aam06 on aam06id = abm01umu "+
                        "inner join abe01 as rep on (rep.abe01id = eaa01rep0 or rep.abe01id = eaa01rep1 or rep.abe01id = eaa01rep2 or rep.abe01id = eaa01rep3 or rep.abe01id = eaa01rep4) "+
                        "inner join abe01 as ent on ent.abe01id = abb01ent "+
                        "inner join aah01 on aah01id = abb01tipo "+
                        "inner join abd01 on abd01id = eaa01pcd  "+
                        "where eaa01clasDoc = 1 "+
                        "and eaa01cancData is null "+
                        "and eaa01nfestat <> 5 "+
                        whereEmpresa+
                        whereNumIni+
                        whereNumFin+
                        whereTipoDoc+
                        wherePcd+
                        whereDtEmissao+
                        whereDtEntradaSaida+
                        whereEntidade+
                        whereES+
                        whereItens+
                        whereTransp+
                        "group by rep.abe01id,abm01id,rep.abe01codigo, rep.abe01na,aam06codigo,abm01codigo, abm01na,mps ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEmpresa,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,
                                                        parametroEntidade,parametroItens,parametroTransp);
    }

	private List<TableMap> buscarCamposLivres(Long idRep,Long itemId,Integer numDocIni, Integer numDocFin, List<Long>idsTipoDoc, List<Long>idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long>idsEntidades,Integer optionResumo,List<Long>idsItens,Long idEmpresa,List<Long>idsTransps){
		String whereItem = " and abm01id = :itemId "
		String whereClassDoc = " and eaa01clasDoc = 1 ";
        String whereRep = "and rep.abe01id = :idRep ";		
				
        String whereNumIni = numDocIni != null ? "and abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
        String whereTransp = (idTransp != null && idTransp.size() > 0) && optionTransp == 0 ? "and desp.abe01id in (:idTransp) " : (idTransp != null && idTransp.size() > 0) && optionTransp == 1 ?"and redesp.abe01id in (:idTransp) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
       

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTransp = idTransp != null && idTransp.size() > 0 ? Parametro.criar("idTransp", idTransp) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
        Parametro parametroItem = Parametro.criar("itemId",itemId);
        Parametro parametroRep = Parametro.criar("idRep",idRep)

		
		
		String sql =   "select eaa0103id,eaa0103json "+
                        "from eaa01 "+
                        "inner join abb01 on abb01id = eaa01central "+
                        "inner join eaa0103 on eaa0103doc = eaa01id "+
                        "inner join abm01 on abm01id = eaa0103item "+
                        "inner join aam06 on aam06id = abm01umu "+
                        "inner join abe01 as rep on (rep.abe01id = eaa01rep0 or rep.abe01id = eaa01rep1 or rep.abe01id = eaa01rep2 or rep.abe01id = eaa01rep3 or rep.abe01id = eaa01rep4) "+
                        "inner join abe01 as ent on ent.abe01id = abb01ent "+
                        "inner join aah01 on aah01id = abb01tipo "+
                        "inner join abd01 on abd01id = eaa01pcd  "+
                        "where eaa01clasDoc = 1 "+
                        whereRep+
                        whereItem+
                        whereEmpresa+
                        whereNumIni+
                        whereNumFin+
                        whereTipoDoc+
                        wherePcd+
                        whereDtEmissao+
                        whereDtEntradaSaida+
                        whereEntidade+
                        whereES+
                        whereTransp

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroRep,parametroItem,parametroEmpresa,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,
                                                        parametroEntidade,parametroTransp);
						
	}
	private TableMap obterDevolucao(Long idItem, Long idRep){
		def whereItem = " where eaa01033itemdoc = :item ";
        def whereRep = "and (eaa01rep0 = :idRep or eaa01rep1 = :idRep or eaa01rep2 = :idRep or eaa01rep3 = :idRep or eaa01rep4 = :idRep) ";
		
		def sql = " select eaa01033qtComl, eaa01033qtUso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103total, eaa0103totdoc, eaa0103totdoc, eaa0103json "+
					" from eaa01033 "+
					" inner join eaa0103 on eaa0103id = eaa01033item  "+
                    "inner join eaa01 on eaa01id = eaa0103doc "+
					whereItem +
                    whereRep +
					" and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "
		
		def p1 = criarParametroSql("item", idItem);
        def p2 = criarParametroSql("idRep",idRep);
		return getAcessoAoBanco().buscarUnicoTableMap(sql,p1,p2)
	}
	
	private void comporDevolucao(TableMap dado, TableMap devolucao, TableMap ItensJson, List<String> campos ) {
			for(campo in campos) {
				if(campo != null) {
					ItensJson.put(campo, ItensJson.getBigDecimal_Zero(campo) - devolucao.getTableMap("eaa0103json").getBigDecimal_Zero(campo))
				}
			}
			for(campo in ["eaa0103qtuso","eaa0103qtcoml","eaa0103unit","eaa0103total","eaa0103totdoc","eaa0103totfinanc"]) {
				dado.put(campo, dado.getBigDecimal_Zero(campo) - devolucao.getBigDecimal_Zero(campo))
			}
			
	}
	
	private String buscarCampo(TableMap dado, Map<String, String> nome) {

		for(campo in nome) {

			if(campo.value != null) {
				String nomeBanco = buscarNomeCampoFixo(campo.value)
				if(nomeBanco != null) {
					dado.put("nomeCampo"+campo.key, campo.value )
					dado.put("valorCampo"+campo.key, dado.getBigDecimal_Zero(nomeBanco) )
				}else {
					nomeBanco = buscarNomeCampoLivre(campo.value)
					dado.put("nomeCampo"+campo.key, nomeBanco)
					dado.put("valorCampo"+campo.key, dado.getBigDecimal_Zero(campo.value))
					def t
				}
			}
		}
	}
	
	 private buscarNomeCampoFixo(String campo){
        switch(campo) {
            case "Qtde. de Uso":
                return "eaa0103qtuso"
                break
            case "Qtde. Comercial":
                return "eaa0103qtcoml"
                break
            case "Preço Unitário":
                return "eaa0103unit"
                break
            case "Total do Item":
                return "eaa0103total"
                break
            case "Total Documento":
                return "eaa0103totdoc"
                break
            case "Total Financeiro":
                return "eaa0103totfinanc"
                break
            default:
                return null
                break
        }
    }


    public String buscarNomeCampoLivre(String campo) {
		def sql = " select aah02descr from aah02 where aah02nome = :nome "
		return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))
		
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBQb3IgSXRlbSAtIExDUiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNSZiBJdGVucyBQb3IgUmVwcmVzZW50YW50ZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=