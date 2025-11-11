package Silcon.relatorios.customizados

import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate

public class CST_Fechamento_Cliente extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Fechamento Cliente";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        LocalDate[] dtEmissao = getIntervaloDatas("dtEmissao")
        LocalDate[] dtVencimento = getIntervaloDatas("dtVencimento");
        List<Long> entidades = getListLong("entidades");

        List<TableMap> docsFinanc = buscarDocumentosFinanceiros(dtEmissao, dtVencimento, entidades);
        Long idEntidade = null;
        for(doc in docsFinanc){
            TableMap vlrVale = new TableMap()

            if(idEntidade != doc.getLong("idEnt")){
                idEntidade = doc.getLong("idEnt");
                vlrVale = buscarValorValeCliente(idEntidade) == null ? new TableMap() : buscarValorValeCliente(idEntidade);
            }

            doc.put("valorVale", vlrVale.getBigDecimal_Zero("dad01saldo"));
            doc.put("dataVale", vlrVale.getDate("dad01dtI"));

            idEntidade = doc.getLong("idEnt");
        }

        params.put("USUARIO", obterUsuarioLogado().getAab10user())

        return gerarPDF("CST_Fechamento_Cliente_PDF", docsFinanc);

    }
    private List<TableMap> buscarDocumentosFinanceiros(LocalDate[] dtEmissao,LocalDate[] dtVencimento, List<Long> entidades){

        String whereQuita = " WHERE abb01quita = 0 ";
        String whereRP = " AND daa01rp = 0 ";
        String whereDtEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDtVctoR = dtVencimento != null ? "AND daa01dtVctoR BETWEEN :dtVctoIni AND :dtVctoFin " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "AND abe01id IN (:entidades) " : "";


        Parametro parametroDtEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissao[1]) : null;
        Parametro parametroDtVctoIni = dtVencimento != null ? Parametro.criar("dtVctoIni", dtVencimento[0]) : null;
        Parametro parametroDtVctoFin = dtVencimento != null ? Parametro.criar("dtVctoFin", dtVencimento[1]) : null;
        Parametro parametroEntidade = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;


        String sql = "SELECT  " +
                    "    aah01codigo AS codTipoDoc,  " +
                    "    aah01nome AS descrTipoDoc,  " +
                    "    aac10id AS empresa,   " +
                    "    abe01codigo AS codEntidade,  " +
                    "    abe01na AS naEntidade,  " +
                    "    abe01id AS idEnt,  " +
                    "    abb01num AS numDoc,  " +
                    "    abb01parcela AS parcela,  " +
                    "    daa01dtLcto AS dtLcto,  " +
                    "    daa01dtVctoR AS dtVctoR,  " +
                    "    abb01valor AS valor,   " +
                    "    CASE  " +
                    "        WHEN NOW() > daa01dtVctoR  " +
                    "        THEN (CAST(daa01json ->> 'juros' AS numeric(18,6)) * CAST(TO_CHAR((NOW()) - (daa01dtVctoR),'DD') AS INT))  " +
                    "        ELSE 0.00  " +
                    "    END AS juros,  " +
                    "    (abb01valor +  " +
                    "        (CASE  " +
                    "            WHEN NOW() > daa01dtVctoR  " +
                    "            THEN (CAST(daa01json ->> 'juros' AS numeric(18,6)) * CAST(TO_CHAR((NOW()) - (daa01dtVctoR),'DD') AS INT))  " +
                    "            ELSE 0.00  " +
                    "        END) " +
                    "    ) AS vlrFinal,  " +
                    "    daa01obs AS observacao,  " +
                    "    abf15codigo AS codPort,  " +
                    "    abf15nome AS nomePort,  " +
                    "    abf16codigo AS codOper,  " +
                    "    abf16nome AS nomeOper  " +
                    " FROM daa01  " +
                    " INNER JOIN abb01 ON abb01id = daa01central  " +
                    " INNER JOIN aah01 ON aah01id = abb01tipo  " +
                    " INNER JOIN aac10 ON aac10id = daa01gc  " +
                    " INNER JOIN abe01 ON abe01id = abb01ent  " +
                    " LEFT JOIN abf15 ON abf15id = daa01port  " +
                    " LEFT JOIN abf16 ON abf16id = daa01oper   " +
                    whereQuita +
                    whereRP+
                    whereDtEmissao +
                    whereDtVctoR +
                    whereEntidades +
                    "ORDER BY abe01codigo, daa01dtVctoR, abb01num, abb01parcela ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtVctoIni, parametroDtVctoFin, parametroEntidade);
    }
    private TableMap buscarValorValeCliente(Long idEntidade){
        String sql = "SELECT dad01saldo, dad01dtI FROM dad01 WHERE dad01ent = :idEntidade";

        Parametro parametroEntidade = Parametro.criar("idEntidade", idEntidade);

        return getAcessoAoBanco().buscarUnicoTableMap(sql, parametroEntidade);
    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEZlY2hhbWVudG8gQ2xpZW50ZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IkNTVCAtIEZlY2hhbWVudG8gQ2xpZW50ZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==