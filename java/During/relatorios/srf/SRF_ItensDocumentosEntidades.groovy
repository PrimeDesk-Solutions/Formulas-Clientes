package During.relatorios.srf
import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;
import java.time.LocalDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.ea.Eaa01
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.relatorio.TableMapDataSource

public class SRF_ItensDocumentosEntidades extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SRF Itens Por Documentos e Entidades"; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
        filtrosDefault.put("devolucao", true);
        filtrosDefault.put("chkDevolucao",true);
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("tipoOperacao", "0");
        filtrosDefault.put("impressao","0");
        filtrosDefault.put("resumo","0");
        filtrosDefault.put("resumoOperacao","0")
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
        List<Long> idsItens = getListLong("itens")
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

        String periodo = ""
        if(dtEmissao != null) {
            periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }else if(dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("totalizar1",totalizar1);
        params.put("totalizar2",totalizar2);
        params.put("totalizar3",totalizar3);
        params.put("title","Itens Por Documentos");
        params.put("empresa",empresa.aac10codigo +"-"+ empresa.aac10na);
        params.put("periodo",periodo);

		Map<String,String> campos = new HashMap();
		campos.put("1",campoLivre1 != null ? campoLivre1 : campoFixo1 != null ? campoFixo1 : null);
		campos.put("2",campoLivre2 != null ? campoLivre2 : campoFixo2 != null ? campoFixo2 : null);
		campos.put("3",campoLivre3 != null ? campoLivre3 : campoFixo3 != null ? campoFixo3 : null);


		if(campoLivre1 != null && campoFixo1 != null ) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre2 != null && campoFixo2 != null ) interromper("Selecione apenas 1 valor por campo!")
		if(campoLivre3 != null && campoFixo3 != null ) interromper("Selecione apenas 1 valor por campo!")

		List<TableMap> dados = buscarItensDoc(numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades,optionResumo,idsItens,idEmpresa);
		for(dado in dados){
            Long idItem = dado.getLong("abm01id");
            Long idEnt = dado.getLong("idEnt")
			List<TableMap> tmLivres = buscarCamposLivres(idEnt,idItem,numDocIni, numDocFin, idsTipoDoc, idsPcd,resumoOperacao, dtEmissao, dtEntradaSaida,optionResumo,idEmpresa);
            TableMap jsonTotal = new TableMap();
            for(tm in tmLivres){
                    TableMap json = tm.getTableMap("eaa0103json");
                    Long idItemNota = tm.getLong("eaa0103id");
                    TableMap devolucao = buscarDevolucoes(idItemNota);
                    if(campoLivre1 != null ) jsonTotal.put(campoLivre1,jsonTotal.getBigDecimal_Zero(campoLivre1) + json.getBigDecimal_Zero(campoLivre1));
                    if(campoLivre2 != null ) jsonTotal.put(campoLivre2,jsonTotal.getBigDecimal_Zero(campoLivre2) + json.getBigDecimal_Zero(campoLivre2));
                    if(campoLivre3 != null ) jsonTotal.put(campoLivre3,jsonTotal.getBigDecimal_Zero(campoLivre3) + json.getBigDecimal_Zero(campoLivre3));
                    if(chkDevolucao && devolucao != null){
                        comporDevolucao(dado,devolucao,jsonTotal,[campoLivre1,campoLivre2,campoLivre3]);
                    }
            }
            dado.putAll(jsonTotal)

            buscarValores(dado,campos);
		}
        if(impressao == 0) return gerarPDF("SRF_ItensEntidadesDocumentos(PDF)",dados);
        return gerarXLSX("SRF_ItensEntidadesDocumentos(Excel)",dados);

	}

	private List<TableMap> buscarItensDoc(Integer numDocIni, Integer numDocFin, List<Long>idsTipoDoc, List<Long>idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long>idsEntidades,Integer optionResumo,List<Long>idsItens,Long idEmpresa){
		
		String whereNumIni = numDocIni != null ? "and abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and ent.abe01id in (:idsEntidades) " : "";
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
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens",idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);

		String sql = "select ent.abe01id as idEnt, ent.abe01codigo as codEnt,ent.abe01na as naEnt, abm01id, abm01codigo,abm01na,aah01codigo, abb01num as numNota,abb01data as dtNota, "+
						"case when abm01tipo = 0 then 'M' when abm01tipo = 1 then 'P' when abm01tipo = 2 then 'S' else 'MER' end as mps, "+
                        "aaj15codigo, abb01data, "+
                        "eaa0103qtuso,eaa0103qtcoml,eaa0103unit,eaa0103total,eaa0103totdoc,eaa0103totfinanc "+
						"from eaa01 "+
						"inner join eaa0103 on eaa0103doc = eaa01id "+
						"inner join aaj15 on aaj15id = eaa0103cfop "+
						"inner join abm01 on abm01id = eaa0103item "+
						"inner join aam06 on aam06id = abm01umu "+
						"inner join abb01 on abb01id = eaa01central  "+
						"inner join aah01 on aah01id = abb01tipo "+
						"inner join abe01 as ent on ent.abe01id = abb01ent "+
						"where eaa01clasdoc = 1 "+
						whereEmpresa+
                        whereNumIni+
                        whereNumFin+
                        whereTipoDoc+
                        wherePcd+
                        whereDtEmissao+
                        whereDtEntradaSaida+
                        whereEntidade+
                        whereES+
                        whereItens
						//"group by ent.abe01id,ent.abe01codigo,ent.abe01na, abm01id, abm01codigo,abm01na,mps,aah01codigo,abb01data, abb01num,aaj15codigo, abb01data"
		return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEmpresa,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin,
                                                        parametroEntidade,parametroItens)
	}

    private buscarCamposLivres(Long idEnt,Long idItem,Integer numDocIni, Integer numDocFin, List<Long>idsTipoDoc, List<Long>idsPcd,Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida,Integer optionResumo,Long idEmpresa){
        String whereNumIni = numDocIni != null ? "and abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissao != null && dtEmissao.size() > 0 ? "and abb01data between :dtEmissIni and :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereItem = "and abm01id = :idItem ";
        String whereEnt = "and ent.abe01id = :idEnt ";

       

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroEnt = Parametro.criar("idEnt",idEnt);


		String sql = "select eaa0103id, eaa0103json "+
						"from eaa01 "+
						"inner join eaa0103 on eaa0103doc = eaa01id "+
						"inner join aaj15 on aaj15id = eaa0103cfop "+
						"inner join abm01 on abm01id = eaa0103item "+
						"inner join aam06 on aam06id = abm01umu "+
						"inner join abb01 on abb01id = eaa01central  "+
						"inner join aah01 on aah01id = abb01tipo "+
						"inner join abe01 as ent on ent.abe01id = abb01ent "+
						"where eaa01clasdoc = 1 "+
                        whereItem+
                        whereEnt+
						whereEmpresa+
                        whereNumIni+
                        whereNumFin+
                        whereTipoDoc+
                        wherePcd+
                        whereDtEmissao+
                        whereDtEntradaSaida+
                        whereES
                        
                        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItem,parametroEnt,parametroEmpresa,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroDtEmissaoIni,parametroDtEmissaoFin,parametroDtEntradaSaidaIni,parametroDtEntradaSaidaFin)
    }

    private TableMap buscarDevolucoes(Long idItemNota){
        String whereItem = "where eaa01033itemDoc = :idItemNota ";

        String sql = "select eaa0103qtcoml, eaa01033qtuso, eaa0103qtuso, eaa0103qtcoml, eaa0103unit, eaa0103total, eaa0103totdoc, eaa0103json "+
                        "from eaa01033 "+
                        "inner join eaa0103 on eaa0103id = eaa01033item "+
                        whereItem+
                        "and (eaa01033qtComl > 0 or eaa01033qtUso > 0) ";
        Parametro p1 = Parametro.criar("idItemNota",idItemNota);

        return getAcessoAoBanco().buscarUnicoTableMap(sql,p1);

    }

    private void comporDevolucao(TableMap dado,TableMap devolucao,TableMap jsonTotal,List<String>campos){
        for(campo in campo){
            if(campo != null){
                jsonTotal.put(campo,jsonTotal.getBigDecimal_Zero(campo) - devolucao.getBigDecimal_Zero(campo));
            }
        }

        for(campo in ["eaa0103qtuso","eaa0103qtcoml","eaa0103unit","eaa0103total","eaa0103totdoc","eaa0103totfinanc"]){
            dado.put(campo,dado.getBigDecimal_Zero(campo) - devolucao.getBigDecimal_Zero(campo))
        }
    }

    private void buscarValores(TableMap dado, Map<String,String>campos){
        for(campo in campos){
            if(campo.value != null){
                String nomeCampo = buscarCampoFixo(campo.value);
                if(nomeCampo != null){
                    dado.put("nomeCampo"+campo.key, campo.value);
                    dado.put("valorCampo"+campo.key,dado.getBigDecimal_Zero(nomeCampo));
                }else{
                    nomeCampo = buscarNomeCampoLivre(campo.value)
                    dado.put("nomeCampo"+campo.key, nomeCampo);
                    dado.put("valorCampo"+campo.key,dado.getBigDecimal_Zero(campo.value));
                }
            }
        }
    }
    private buscarCampoFixo(String campo){
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
//meta-sis-eyJkZXNjciI6IlNSRiBJdGVucyBQb3IgRG9jdW1lbnRvcyBlIEVudGlkYWRlcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==