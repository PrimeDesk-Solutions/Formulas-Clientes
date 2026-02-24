package Silcon.relatorios.scf

import groovy.swing.table.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro;

import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.collections.TableMap

public class SCF_Impressao_Movimento extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Impressão Movimento Financeiro";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        Long idMovimento = getLong("daa10id");

        List<TableMap> dados = buscarDadosRelatorio(idMovimento);

        for(dado in dados){
            Integer mov = dado.getInteger("mov");

            if(mov == 0){
                dado.put("movimentacao", "0-Entrada");
            }else{
                dado.put("movimentacao", "1-Saída");
            }
        }

        params.put("TITULO", "SCF - Movimento Financeiro");
        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        params.put("LOGO_EMPRESA", "C:\\SAM-Servidor\\samdev\\resources\\Silcon\\relatorios\\spv\\Logo Silcon.png");
        params.put("LOGO_REVENDA", "C:\\SAM-Servidor\\samdev\\resources\\Silcon\\relatorios\\spv\\logoPrimeDesk.png");


        return gerarPDF("SCF_Impressao_Movimento", dados);
    }
    private List<TableMap> buscarDadosRelatorio(Long idMovimento){
        String sql = "SELECT daa1001mov AS mov, daa1001valor AS valor, daa1001liquido AS liquido, " +
                "aah01codigo AS codTipoDoc, aah01nome AS tipoDoc, abb01num AS numDoc, abb01parcela AS parcela, " +
                "abe01codigo AS codEntidade, abe01nome AS nomeEntidade, CAST(daa1001json ->> 'desconto' AS NUMERIC(18,6)) AS desconto, " +
                "CAST(daa1001json ->> 'multa' AS NUMERIC(18,6)) AS multa, " +
                "CAST(daa1001json ->> 'juros' AS NUMERIC(18,6)) AS juros, " +
                "CAST(daa1001json ->> 'encargos' AS NUMERIC(18,6)) AS encargos, daa10nome AS nomeMovimento " +
                "FROM daa10 " +
                "INNER JOIN daa1001 ON daa1001movim = daa10id " +
                "INNER JOIN abb01 ON abb01id = daa1001central " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN abe01 ON abe01id = abb01ent " +
                "WHERE daa10id = :idMovimento " +
                "ORDER BY abb01num, abb01parcela ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idMovimento", idMovimento));
    }
}