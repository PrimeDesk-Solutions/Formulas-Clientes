package Silcon.relatorios.scf

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;

import java.util.Map;
import java.util.HashMap;

public class SCF_Cashback_Clientes extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Cashback Clientes";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsEntidades = getListLong("entidades");
        Boolean exibirLancamentos = getBoolean("lancamentos");

        List<TableMap> dados = buscarDadosRelatorio(idsEntidades);
        List<TableMap> listLctos = new ArrayList();

        for(dado in dados){
            Long idCashback = dado.getLong("dad01id");

            if(exibirLancamentos){
                List<TableMap> lctos = buscarLancamentosCashback(idCashback);
                for(lcto in lctos){

                    lcto.put("mov", lcto.getInteger("dad0101es") == 0 ? "0-Entrada" : "1-Saída");
                    lcto.put("key", idCashback);
                    listLctos.add(lcto);
                }

                dado.put("key", idCashback);
            }
        }

        if(exibirLancamentos){
            params.put("TITULO", "SCF - Cashback Clientes (Analítico)");
            params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na())

            // Cria os sub-relatórios
            TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
            dsPrincipal.addSubDataSource("dsLctos", listLctos, "key", "key");
            adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCF_Cashback_Clientes_Analitico_S1"));

            return gerarPDF("SCF_Cashback_Clientes_Analitico", dsPrincipal);
        }

        params.put("TITULO", "SCF - Cashback Clientes (Sintético)");
        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());

        return gerarPDF("SCF_Cashback_Clientes_Sintetico", dados);
    }
    private List<TableMap> buscarDadosRelatorio(List<Long> idsEntidades){
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "WHERE dad01ent IN (:idsEntidades) " : "";
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;

        String sql = "SELECT abe01codigo, abe01nome, dad01saldo, dad01id " +
                        "FROM dad01 " +
                        "INNER JOIN abe01 ON abe01id = dad01ent " +
                        whereEntidades +
                        "ORDER BY abe01codigo";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidades);

    }
    private List<TableMap> buscarLancamentosCashback(Long idCashback){
        String sql = "SELECT dad0101data, dad0101hist, dad0101es, " +
                    "dad0101valor, abb01num " +
                    "FROM dad0101 " +
                    "INNER JOIN abb01 ON abb01id = dad0101central " +
                    "WHERE dad0101cb = :idCashback " +
                    "ORDER BY dad0101id DESC"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idCashback", idCashback));
    }
}