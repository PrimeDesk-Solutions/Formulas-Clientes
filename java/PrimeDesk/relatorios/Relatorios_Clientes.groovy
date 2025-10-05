package PrimeDesk.relatorios

import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.core.variaveis.MDate
import br.com.multitec.utils.Utils
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

public class Relatorios_Clientes extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "Relatório Clientes";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        String sql = "SELECT (COALESCE(MAX(numeroRelatorio)) + 1)::text FROM relatorios "
        //Próxima sequencia do relatório
        String proxNum = getAcessoAoBanco().obterString(sql);
        proxNum = proxNum.length() < 3 ? "00" + proxNum : proxNum.length() < 4 ? "0" + proxNum : proxNum;
        LocalDate data = MDate.date();
        filtrosDefault.put("numero", proxNum.toString())
        filtrosDefault.put("data", data)
        filtrosDefault.put("totalGrau", true)
        filtrosDefault.put("inativos", true)
        filtrosDefault.put("entidade3", "0")
        filtrosDefault.put("tecnico1", true);
        filtrosDefault.put("tecnico2", true);
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        String cliente = getString("cliente");
        String numRelatorio = getString("numero");
        String titulo = getString("titulo");
        String descricao = getString("descricao");
        String horaInicio = getString("horaInicio");
        String horaFim = getString("horaFim");
        String tecnicos = getBoolean("tecnico1") && getBoolean("tecnico2") ? "Leonardo/Wilson" : getBoolean("tecnico1") && !getBoolean("tecnico2") ? "Leonardo" : "Wilson";
        String responsavel = getString("responsavel");
        String observacao = getString("observacao");
        boolean reimpressao = getBoolean("reimpressao");
        LocalDate dataRelatorio = getLocalDate("data");
        LocalDate dataAtual = LocalDate.now();
        String horaAtual = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        TableMap registro = new TableMap();
        List<TableMap> registros = new ArrayList<>();

        registro.put("cliente", cliente);
        registro.put("numRelatorio", numRelatorio);
        registro.put("titulo", titulo);
        registro.put("descricao", descricao);
        registro.put("horaInicio", horaInicio);
        registro.put("horaFim", horaFim);
        registro.put("tecnicos", tecnicos);
        registro.put("responsavel", responsavel);
        registro.put("dataAtual", dataAtual);
        registro.put("horaAtual", horaAtual);
        registro.put("dataRelatorio", dataRelatorio);
        registro.put("observacao", observacao);

        registros.add(registro);



        if (!reimpressao) {
            try {
                verificaRelatorioExistente(numRelatorio);
                // Verifica se já tem um relatório com a numeração informada em tela
                String sql = "INSERT INTO relatorios VALUES ((SELECT MAX(numeroRelatorio) + 1 FROM relatorios) , '" + cliente + "')";
                getSession().connection.prepareStatement(sql).execute()
            } catch (Exception e) {
                interromper(e.getMessage())
            }
        }


        return gerarPDF("Relatorios_Clientes", registros)
    }

    private void verificaRelatorioExistente(String numRelatorio) {
        Integer numeroRelatorio = Integer.parseInt(numRelatorio)
        String sql = "SELECT * FROM relatorios WHERE numeroRelatorio = :numRelatorio";

        TableMap tmRelatorio = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("numRelatorio", numeroRelatorio))

        if (tmRelatorio != null && tmRelatorio.size() > 0) throw new ValidacaoException("Relatório de número " + numRelatorio + " já foi gerado. Atualize a página e tente novamente.")
    }
}