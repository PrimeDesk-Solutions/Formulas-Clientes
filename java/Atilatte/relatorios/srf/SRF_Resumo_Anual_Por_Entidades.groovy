package Atilatte.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils
import sam.model.entities.aa.Aac10;
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;



public class SRF_Resumo_Anual_Por_Entidades extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF Resumo Anual por Entidades ";
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
        filtrosDefault.put("ano",LocalDate.now().getYear().toString())
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial") == null || getInteger("numeroInicial") == "" ? 000000001 : getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal") == null || getInteger("numeroFinal") == "" ? 999999999 : getInteger("numeroFinal");
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer resumoOperacao = getInteger("resumoOperacao");
        List<Long> idsEntidades = getListLong("entidades");
        String campoFixo = getString("campoFixo");
        String campoLivre = getString("campoLivre");
        Integer ano = getInteger("ano");
        Boolean totalizar = getBoolean("totalizar");
        Integer impressao = getInteger("impressao");
        Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;
        boolean liquido = getBoolean("liquido");

        if(ano == null) interromper("Necessário informar o ano no filtro do relatório.");
        if(campoFixo == null && campoLivre == null) interromper("Necessário informar ao menos 1 campo no filtro do relatório.")

        String periodo = "Período Emissão: " + "01/01/" + ano.toString() + " à " + "31/12/" + ano.toString();

        String nomeCampo = buscarNomeCampoFixo(campoFixo) != null ? buscarNomeCampoFixo(campoFixo) : campoLivre.toString();

        params.put("TOTALIZAR", totalizar);
        params.put("TITULO","SRF - Resumo Documentos Anuais (Entidades)");
        params.put("TOTALIZAR",totalizar);
        params.put("EMPRESA", empresa.aac10codigo + "-" + empresa.aac10na);
        params.put("FILTRO",campoFixo != null ? campoFixo : buscarNomeCampoLivre(campoLivre));


        if (campoLivre != null && campoFixo != null) interromper("Selecione apenas 1 valor por campo!");

        Map<String, String> campos = new HashMap();

        campos.put("1", campoLivre != null ? campoLivre : campoFixo != null ? campoFixo : null);

        List<TableMap> dados = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, idsEntidades, ano, idEmpresa, campoLivre);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<TableMap> dadosRelatorio = new ArrayList();
        List<TableMap> listDevolucoesGeral = new ArrayList();
        List<TableMap> listDevolucoesAjustado = new ArrayList<>()
        TableMap dadosTmp = new TableMap();
        TableMap valoresTotais = new TableMap();

        Long idsEntAux = null;
        Integer mesAux = null;
        for (dado in dados) {

            if(idsEntAux == null){
                dadosTmp.putAll(dado);
                idsEntAux = dado.getLong("abe01id");
                mesAux = dado.getDate("abb01data").getMonthValue();
                valoresTotais.put("valor" + mesAux, dado.getBigDecimal_Zero(nomeCampo));
            }else if(idsEntAux == dado.getLong("abe01id")){
                if(mesAux == dado.getDate("abb01data").getMonthValue()){
                    valoresTotais.put("valor" + mesAux, valoresTotais.getBigDecimal_Zero("valor" + mesAux) + dado.getBigDecimal_Zero(nomeCampo));
                }else{
                    mesAux = dado.getDate("abb01data").getMonthValue();
                    valoresTotais.put("valor" + mesAux, dado.getBigDecimal_Zero(nomeCampo))
                }
            }else{
                TableMap tmp = new TableMap();
                tmp.putAll(dadosTmp);
                tmp.putAll(valoresTotais);
                dadosRelatorio.add(tmp);

                dadosTmp = new TableMap();
                dadosTmp.putAll(dado);
                valoresTotais = new TableMap();
                idsEntAux = dado.getLong("abe01id");
                mesAux = dado.getDate("abb01data").getMonthValue();
                valoresTotais.put("valor" + mesAux, dado.getBigDecimal_Zero(nomeCampo));
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);

        dadosRelatorio.add(tmp);

        if (impressao == 1) return gerarXLSX("SRF_Resumo_Anual_Por_Entidades_Excel",dadosRelatorio);
        return gerarPDF("SRF_Resumo_Anual_Por_Entidades_PDF",dadosRelatorio)

    }
    private List<TableMap> buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, List<Long> idsEntidades, Integer ano, Long idEmpresa, String campoLivre ){
        String whereNumIni = numDocIni != null ? "AND abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "AND abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id IN (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id IN (:idsPcd) " : "";
        String whereDtEmissao = "AND abb01data BETWEEN :dtInicial AND :dtFinal";
        String whereES = resumoOperacao == 1 ? " AND eaa01esMov = 1 " : " AND eaa01esMov = 0 ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "AND abe01id IN (:idsEntidades) " : "";


        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = Parametro.criar("dtInicial", LocalDate.of(ano, 1, 1));
        Parametro parametroDtEmissaoFin =Parametro.criar("dtFinal", LocalDate.of(ano, 12, 31));
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;

        String whereCampoLivre = campoLivre != null ? ", SUM(CAST(eaa0103json ->> '" + campoLivre + "' AS NUMERIC(18,6))) AS " + campoLivre : "";

        String sql = "SELECT abe01codigo, abe01nome, abe01id, abb01data, " +
                " SUM(eaa0103qtuso) AS eaa0103qtuso ,SUM(eaa0103qtcoml) AS eaa0103qtcoml, SUM(eaa0103unit) AS eaa0103unit, " +
                " SUM(eaa0103total) AS eaa0103total,SUM(eaa0103totdoc) AS eaa0103totdoc, SUM(eaa0103totfinanc) AS eaa0103totfinanc "+ whereCampoLivre +
                " FROM eaa01 " +
                " INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                " INNER JOIN abb01 ON abb01id = eaa01central " +
                " INNER JOIN abe01 ON abe01id = abb01ent " +
                " INNER JOIN abd01 ON abd01id = eaa01pcd "+
                " INNER JOIN aah01 ON aah01id = abb01tipo " +
                " WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
                " AND eaa01cancData IS NULL " +
                " AND eaa01nfestat <> 5 " +
                whereEmpresa +
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereES +
                whereEntidades +
                "GROUP BY abe01codigo, abe01nome, abb01data, abe01id " +
                "ORDER BY abe01codigo, abb01data"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroEntidades);

    }

    private buscarNomeCampoFixo(String campo) {
        switch (campo) {
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
        def sql = " SELECT aah02descr FROM aah02 WHERE aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql, criarParametroSql("nome", campo))
    }
}
//meta-sis-eyJkZXNjciI6IlNSRiBSZXN1bW8gRG9jdW1lbnRvcyBBbnVhaXMgIiwidGlwbyI6InJlbGF0b3JpbyJ9