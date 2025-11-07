package Atilatte.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils
import sam.model.entities.aa.Aac10;
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;



public class SSRF_ResumoDocumentosAnuais extends RelatorioBase {
	@Override 
	public String getNomeTarefa() { 
		return "SRF Resumo Documentos Anuais "; 
	}
 	@Override 
	public Map<String, Object> criarValoresIniciais() {
		Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("totalizar", true);
        filtrosDefault.put("liquido", true);
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
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
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> representantes = getListLong("representantes");
        List<Long> idsMunicipios = getListLong("municipios");
        String campoFixo = getString("campoFixo");
        String campoLivre = getString("campoLivre");
        Integer ano = getInteger("ano");
        Boolean totalizar = getBoolean("totalizar");
        Boolean devolucao = getBoolean("liquido");
        Integer opcaoResumo = getInteger("resumo");
        Integer impressao = getInteger("impressao");
        Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;

        params.put("title","Resumo Por Entidade");
        params.put("totalizar",totalizar);
        params.put("empresa",empresa.aac10codigo+"-"+empresa.aac10na);

        
        if(campoLivre){
            params.put("campoFiltro",campoLivre);
        }else{
            params.put("campoFiltro",campoFixo);
        }

        if(campoFixo != null && campoLivre != null) interromper("Insira apenas um valor de campo por filtro")
        List<TableMap> dados = buscarDocumentos(numDocIni,numDocFin,idsTipoDoc,idsPcd,resumoOperacao,idsEntidades,
        representantes,idsMunicipios,ano,idEmpresa,opcaoResumo);
       
       for(dado in dados){

            Long idFiltro = resumo == 0 ? dado.getLong("abe01id") : dado.getLong("abm01id")
            TableMap tmCamposLivres = new TableMap();
            TableMap tmCamposLivresDevolvido = new TableMap();
            TableMap tmCamposFixoDev = new TableMap();

            TableMap total = new TableMap();


            for(int i = 1; i <= 12; i++){

                if(campoFixo != null){
                    buscarTotalPorMes(dado,idFiltro,campoFixo,i,resumoOperacao,ano,idEmpresa,opcaoResumo);
                }

                if(campoLivre != null){
                	 buscarTotalLivreMes(idFiltro,campoLivre,i,tmCamposLivres,ano,resumoOperacao,idEmpresa,opcaoResumo);  

                }
               
                if(devolucao){
                    if(campoLivre != null){
                        obterDevolucaoLivre(idFiltro,campoLivre,i,tmCamposLivresDevolvido,resumoOperacao,ano,idEmpresa,opcaoResumo);
                        total.put("valor"+i,tmCamposLivres.getBigDecimal_Zero("valor"+i) - tmCamposLivresDevolvido.getBigDecimal_Zero("valor"+i))
                        dado.putAll(total);
                    }
                   if(campoFixo != null){
                        obterDevolucaoFixos(idFiltro,campoFixo,campoLivre,i,tmCamposFixoDev,resumoOperacao,ano,idEmpresa,opcaoResumo);
                        total.put("valor"+i,dado.getBigDecimal_Zero("valor"+i) - tmCamposFixoDev.getBigDecimal_Zero("valor"+i));
                        dado.putAll(total);
                    }
                   
                }
                if(campoLivre != null){
                    dado.putAll(tmCamposLivres)
                }
                total.put("total",total.getBigDecimal_Zero("total") + dado.getBigDecimal_Zero("valor"+i))
            }   
            dado.putAll(total)
       }

       if(opcaoResumo == 0){
            params.put("title","Resumo por Entidades");
            if (impressao == 1) return gerarXLSX("SRF_ResumoAnualPorEntidade(Excel)",dados);
            return gerarPDF("SRF_ResumoAnualPorEntidade(PDF)",dados);
        }else{
            params.put("title","Resumo por Itens");
            if (impressao == 1) return gerarXLSX("SRF_ResumoAnualPorItens(Excel)",dados);
            return gerarPDF("SRF_ResumoAnualPorItens(PDF)",dados)
        }

        
        
	}
    private List<TableMap> buscarDocumentos(Integer numDocIni,Integer numDocFin,List<Long>idsTipoDoc,List<Long>idsPcd,Integer resumoOperacao,List<Long>idsEntidades,
        List<Long> representantes,List<Long> idsMunicipios,Integer ano,Long idEmpresa, Integer opcaoResumo){
            String whereNumIni = numDocIni != null ? "where abb01num >= :numDocIni " : "";
            String whereNumFin = numDocFin != null ? "and abb01num <= :numDocFin " : "";
            String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "and aah01id in (:idsTipoDoc) " : "";
            String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "and abd01id in (:idsPcd) " : "";
            String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
            String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
            String whereReps = representantes != null && representantes.size() > 0 ? "abe01id in (:representantes) " : ""
            String whereMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? "and aag0201id in (:idsMunicipios) " : "";
            String whereEmpresa = "and eaa01gc = :idEmpresa ";
            String whereAno = "and extract(year from abb01data) = :ano ";
  		  String whereClassDoc = " and eaa01clasDoc = 1 "


            Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
            Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
            Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc",idsTipoDoc) : null;
            Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
            Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
            Parametro parametroReps = representantes != null && representantes.size() > 0 ? Parametro.criar("representantes", representantes) : null;
            Parametro parametroMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? Parametro.criar("idsMunicipios", idsMunicipios) : null;
            Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);
            Parametro parametroAno = Parametro.criar("ano",ano);

            String resumo = opcaoResumo == 0 ? "abe01codigo,abe01na,abe01id " : "abm01id,abm01tipo, aam06codigo, abm01codigo,abm01descr,abm01id ";

           
            String sql = "select "+resumo+
                            "from eaa01 " +
                            "inner join abb01 on abb01id = eaa01central "+
                            "inner join eaa0103 on eaa0103doc = eaa01id "+
                            "inner join abm01 on abm01id = eaa0103item "+
                            "inner join aam06 on aam06id = abm01umu "+
                            "inner join abe01 on abe01id = abb01ent "+
                            whereNumIni +
                            whereNumFin +
                            whereTipoDoc +
                            wherePcd +
                            whereES+
                            whereEntidade+
                            whereReps+
                            whereMunicipios+
                            whereEmpresa+
                            whereAno+
                            whereClassDoc+
                            "group by "+ resumo;
            
            return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumIni,parametroNumFin,parametroTipoDoc,parametroPcd,parametroEntidade,parametroReps,parametroMunicipios,parametroEmpresa,parametroAno);
        }

        private TableMap buscarTotalPorMes(TableMap dado,Long idFiltro,String campoFixo,Integer numMes,Integer oper, Integer ano,Long idEmpresa,Integer opcaoResumo){
            
            String whereResumo = opcaoResumo == 0 ? "WHERE abe01id = :idFiltro " : "WHERE abm01id = :idFiltro ";
            String whereMes = "and extract(month from abb01data) = :numMes ";
            String whereES = oper == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
            String whereAno = "and extract(year from abb01data) = :ano ";
            String whereEmpresa = "and eaa01gc = :idEmpresa ";
            String whereClassDoc = " and eaa01clasDoc = 1 "



            Parametro parametroResumo = Parametro.criar("idFiltro",idFiltro);
            Parametro parametroMes = Parametro.criar("numMes",numMes);
            Parametro parametroAno = Parametro.criar("ano",ano);
            Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);


            String vlrCampoFixo = buscarNomeCampoFixo(campoFixo);


            String sql = "SELECT CAST(SUM("+vlrCampoFixo+") as numeric(18,2)) as "+"valor"+numMes+" "+
                            "FROM eaa01 "+
                            "INNER JOIN abb01 ON abb01id = eaa01central "+
                            "INNER JOIN abe01 on abe01id = abb01ent "+
                            "INNER JOIN eaa0103 ON eaa0103doc = eaa01id "+
                            "INNER JOIN abm01 on abm01id = eaa0103item "+
                            whereResumo+
                            whereMes +
                            whereES +
                            whereAno+
                            whereClassDoc;
                           

            TableMap registros = getAcessoAoBanco().buscarUnicoTableMap(sql,parametroResumo,parametroMes,parametroAno,parametroEmpresa);
            dado.putAll(registros);
        }
        private TableMap buscarTotalLivreMes(Long idFiltro,String campoLivre,Integer numMes,TableMap tmCamposLivres, Integer ano,Integer oper, Long idEmpresa,Integer opcaoResumo){

            //interromper(numMes.toString())

            String whereResumo = opcaoResumo == 0 ? "WHERE abe01id = :idFiltro " : "WHERE abm01id = :idFiltro ";
            String whereMes = "and extract(month from abb01data) = :numMes ";
            String whereAno = "and extract(year from abb01data) = :ano ";
            String whereES = oper == 1 ? " and eaa01esMov = 1 " : " and eaa01esMov = 0";
            String whereEmpresa = "and eaa01gc = :idEmpresa ";
            String whereClassDoc = " and eaa01clasDoc = 1 ";



            Parametro parametroResumo = Parametro.criar("idFiltro",idFiltro);
            Parametro parametroMes = Parametro.criar("numMes",numMes);
            Parametro parametroAno = Parametro.criar("ano",ano);
            Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);


            String nomeCampoLivre = buscarNomeCampoLivre(campoLivre)

            String sql = "SELECT SUM(CAST(eaa0103json ->> '"+campoLivre+"' as numeric(18,2))) as "+"valor"+numMes+" "+
                            "FROM eaa01 "+
                            "INNER JOIN abb01 ON abb01id = eaa01central "+
                            "INNER JOIN abe01 on abe01id = abb01ent "+
                            "INNER JOIN eaa0103 ON eaa0103doc = eaa01id "+
                            "INNER JOIN abm01 on abm01id = eaa0103item "+
                            whereResumo+
                            whereMes+
                            whereAno+
                            whereEmpresa+
                            whereES+
                            whereClassDoc;

            TableMap tmLivre = getAcessoAoBanco().buscarUnicoTableMap(sql,parametroResumo,parametroMes,parametroAno,parametroEmpresa);

            tmCamposLivres.putAll(tmLivre);


        }

        private TableMap obterDevolucaoLivre(Long idFiltro,String campoLivre,Integer numMes,TableMap tmCamposLivresDevolvido,Integer oper, Integer ano,Long idEmpresa,Integer opcaoResumo){
            String whereResumo = opcaoResumo == 0 ? "WHERE abe01id = :idFiltro " : "WHERE abm01id = :idFiltro ";
            String whereMes = "and extract(month from abb01data) = :numMes ";
            String whereEs = oper == 0 ? "and eaa01esMov = 1 " : "and eaa01esMov = 0";
            String whereAno = "and extract(year from abb01data) = :ano ";
            String whereEmpresa = "and eaa01gc = :idEmpresa ";
            String whereClassDoc = " and eaa01clasDoc = 1 ";



            Parametro parametroResumo = Parametro.criar("idFiltro",idFiltro);
            Parametro parametroMes = Parametro.criar("numMes",numMes);
            Parametro parametroAno = Parametro.criar("ano",ano);
            Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);


            String nomeCampoLivre = buscarNomeCampoLivre(campoLivre)



            
            def sql =   "SELECT SUM(CAST(eaa0103json ->> '"+campoLivre+"' as numeric(18,2))) as "+"valor"+numMes+" "+
                        " from eaa01033 "+
                        " inner join eaa0103 on eaa0103id = eaa01033item  "+
                        " inner join eaa01 on eaa01id = eaa0103doc "+
                        " inner join abb01 on abb01id = eaa01central "+
                        " inner join abe01 on abe01id = abb01ent "+
                        "INNER JOIN abm01 on abm01id = eaa0103item "+
                        whereResumo +
                        whereMes+
                        whereEs +
                        whereAno +
                        whereEmpresa+
                        whereClassDoc+
                        " and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "
            
            
            TableMap tmLivreDev = getAcessoAoBanco().buscarUnicoTableMap(sql,parametroResumo,parametroMes,parametroAno,parametroEmpresa);


            tmCamposLivresDevolvido.putAll(tmLivreDev);
	    }

        private TableMap obterDevolucaoFixos(Long idFiltro,String campoFixo,String campoLivre,Integer numMes,TableMap tmCamposFixos,Integer oper, Integer ano, Long idEmpresa,Integer opcaoResumo){
            String whereResumo = opcaoResumo == 0 ? "WHERE abe01id = :idFiltro " : "WHERE abm01id = :idFiltro ";
            String whereMes = "and extract(month from abb01data) = :numMes ";
            String whereEs = oper == 0 ? "and eaa01esMov = 1 " : "and eaa01esMov = 0";
            String whereAno = "and extract(year from abb01data) = :ano ";
            String whereEmpresa = "and eaa01gc = :idEmpresa ";
            String whereClassDoc = " and eaa01clasDoc = 1 ";


            Parametro parametroResumo = Parametro.criar("idFiltro",idFiltro);
            Parametro parametroMes = Parametro.criar("numMes",numMes);
            Parametro parametroAno = Parametro.criar("ano",ano);
            Parametro parametroEmpresa = Parametro.criar("idEmpresa",idEmpresa);


            String vlrCampoFixo = buscarNomeCampoFixo(campoFixo);


            
            def sql =    "SELECT CAST(SUM("+vlrCampoFixo+") as NUMERIC(18,2)) as "+"valor"+numMes+" "+
                        " from eaa01033 "+
                        " inner join eaa0103 on eaa0103id = eaa01033item  "+
                        " inner join eaa01 on eaa01id = eaa0103doc "+
                        " inner join abb01 on abb01id = eaa01central "+
                        " inner join abe01 on abe01id = abb01ent "+
                        "INNER JOIN abm01 on abm01id = eaa0103item "+
                        whereResumo +
                        whereMes+
                        whereEs +
                        whereAno+
                        whereEmpresa+
                        whereClassDoc+
                        " and (eaa01033qtComl > 0 or eaa01033qtUso > 0) "
            
            
            TableMap tmFixoDev = getAcessoAoBanco().buscarUnicoTableMap(sql,parametroResumo,parametroMes,parametroAno,parametroEmpresa);


            tmCamposFixos.putAll(tmFixoDev);
	    }

        
        private String buscarNomeCampoFixo(String campoFixo){
            switch(campoFixo){
                case "Qtde. de Uso":
                    return "eaa0103qtuso";
                    break;
                case "Qtde. Comercial": 
                    return "eaa0103qtcoml";
                    break;
                case "Preço Unitário":
                    return "eaa0103unit";
                    break;
                case "Total do Item":
                    return "eaa0103total";
                    break;
                case "Total Documento":
                    return "eaa0103totdoc";
                    break;
                case "Total Financeiro":
                    return "eaa0103totfinanc";
                    break;
                default:
                    return null;
                    break

            }
        }

        private String buscarNomeCampoLivre(String campo){
            def sql = " select aah02nome from aah02 where aah02nome = :nome "
		    return getAcessoAoBanco().obterString(sql,criarParametroSql("nome", campo))
        }
       
}
//meta-sis-eyJkZXNjciI6IlNSRiBSZXN1bW8gRG9jdW1lbnRvcyBBbnVhaXMgIiwidGlwbyI6InJlbGF0b3JpbyJ9