package Atilatte.relatorios.scf

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_MovimentoDeAdiantamentos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Movimento de Adiantamentos/Amortizações";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> adiantamentos = getListLong("adiantamentos");
        Integer tipo = getInteger("tipo");
        List<Long> entidades = getListLong("entidades");
        LocalDate[] periodo = getIntervaloDatas("periodo");
        Long idEmpresa = obterEmpresaAtiva().getAac10id();
        Integer impressao = getInteger("impressao")

        List<TableMap> dados = buscarDadosRelatorio(adiantamentos,tipo,entidades,periodo, idEmpresa);
        
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        params.put("titulo","SCF - Movimento de Adiantamentos");

	   if(impressao == 1) return gerarXLSX("SCF_MovimentoDeAdiantamentos",dados);
	   
        gerarPDF("SCF_MovimentoDeAdiantamentos",dados);

    }

    private List<TableMap> buscarDadosRelatorio(List<Long> adiantamentos, Integer tipo, List<Long> entidades, LocalDate[] periodo, Long idEmpresa){

        List<TableMap> tmAdiantamentos = buscarAdiantamentos(adiantamentos,entidades,idEmpresa);
        List<TableMap> registros = new ArrayList<>()


        for(adiantamento in tmAdiantamentos){

            Long idAdiantamento = adiantamento.getLong("dad01id");

            List<TableMap> lancamentos = buscarLancamentos(idAdiantamento, tipo,periodo);

            BigDecimal totalEntradas = buscarTotalEntrada(idAdiantamento,periodo);
            BigDecimal totalSaidas = buscarTotalSaida(idAdiantamento,periodo);
            BigDecimal saldo = totalEntradas - totalSaidas;

            adiantamento.put("saldoAnterior", saldo);


            for(lancamento in lancamentos){

                Integer esMov = lancamento.getInteger("dad0101es")
                if(esMov == 0){
                    lancamento.put("adiantamento", lancamento.getBigDecimal_Zero("dad0101valor"));
                    lancamento.put("amortizacao",new BigDecimal(0));
                }else{
                    lancamento.put("adiantamento", new BigDecimal(0));
                    lancamento.put("amortizacao",lancamento.getBigDecimal_Zero("dad0101valor") * -1);
                }

                saldo = (saldo + lancamento.getBigDecimal_Zero("adiantamento")) - (lancamento.getBigDecimal_Zero("amortizacao") * -1);

                lancamento.put("saldoAtual",saldo);

                lancamento.putAll(adiantamento)

                registros.add(lancamento);
            }

        }
        return registros;
    }

    private List<TableMap> buscarAdiantamentos(List<Long>adiantamentos, List<Long> entidades, Long idEmpresa){
        String whereAdiantamentos = adiantamentos != null && adiantamentos.size() > 0 ? "and dad01id in (:adiantamentos) " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "and abe01id in (:entidades) " : "";
        String whereEmpresa = "and dad01gc = :idEmpresa "

        Parametro parametroAdiantamento = adiantamentos != null && adiantamentos.size() > 0 ? Parametro.criar("adiantamentos",adiantamentos) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades",entidades) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);

        String sql = "select dad01id, dad01nome, abe01codigo, abe01na " +
                    "from dad01  " +
                    "inner join abe01 on abe01id = dad01ent " +
                    "where true " +
                    whereAdiantamentos+
                    whereEntidades+
                    whereEmpresa;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroAdiantamento,parametroEntidades,parametroEmpresa);

    }

    private BigDecimal buscarTotalEntrada(Long idAdiantamento, LocalDate[] periodo){

        // Periodos
        def periodoIni = null;
        def periodoFin = null;

        if(periodo != null){
            periodoIni = periodo[0];
            periodoFin = periodo[1];
        }

        String wherePeriodo = periodoIni != null && periodoFin != null ? "and dad0101data < :periodoIni " : "";

        Parametro parametroAdiantamento = Parametro.criar("idAdiantamento",idAdiantamento);
        Parametro parametroPeriodoIni = periodoIni && periodoFin != null ? Parametro.criar("periodoIni",periodoIni) : null;
        Parametro parametroPeriodoFin = periodoIni && periodoFin != null ? Parametro.criar("periodoFin",periodoFin) : null;

        String sql = "SELECT SUM(dad0101valor) as totEntradas " +
                    "FROM dad0101 " +
                    "WHERE dad0101cb = :idAdiantamento  " +
                    wherePeriodo +
                    "and dad0101es = 0";

        return getAcessoAoBanco().obterBigDecimal(sql,parametroAdiantamento,parametroPeriodoIni,parametroPeriodoFin);
    }
    private BigDecimal buscarTotalSaida(Long idAdiantamento, LocalDate[] periodo){

        // Periodos
        def periodoIni = null;
        def periodoFin = null;

        if(periodo != null){
            periodoIni = periodo[0];
            periodoFin = periodo[1];
        }

        String wherePeriodo = periodoIni != null && periodoFin != null ? "and dad0101data < :periodoIni " : "";

        Parametro parametroAdiantamento = Parametro.criar("idAdiantamento",idAdiantamento);
        Parametro parametroPeriodoIni = periodoIni && periodoFin != null ? Parametro.criar("periodoIni",periodoIni) : null;
        Parametro parametroPeriodoFin = periodoIni && periodoFin != null ? Parametro.criar("periodoFin",periodoFin) : null;

        String sql = "SELECT SUM(dad0101valor) as totEntradas " +
                "FROM dad0101 " +
                "WHERE dad0101cb = :idAdiantamento  " +
                wherePeriodo +
                "and dad0101es = 1";

        return getAcessoAoBanco().obterBigDecimal(sql,parametroAdiantamento,parametroPeriodoIni,parametroPeriodoFin);
    }

    private buscarLancamentos(Long idAdiantamento, Integer tipo, LocalDate[] periodo){

        // Periodos
        def periodoIni = null;
        def periodoFin = null;

        if(periodo != null){
            periodoIni = periodo[0];
            periodoFin = periodo[1];
        }

        String whereAdiantamento = "where dad0101cb = :idAdiantamento ";
        String whereTipo = tipo == 0 ? "and dad0101es in (0,1) " : tipo == 1 ? "and dad0101es = 1 " : "and dad0101es = 0";
        String wherePeriodo = periodoIni != null && periodoFin != null ? "and dad0101data between :periodoIni and :periodoFin " : "";

        Parametro parametroAdiantamento = Parametro.criar("idAdiantamento",idAdiantamento);
        Parametro parametroPeriodoIni = periodoIni && periodoFin != null ? Parametro.criar("periodoIni",periodoIni) : null;
        Parametro parametroPeriodoFin = periodoIni && periodoFin != null ? Parametro.criar("periodoFin",periodoFin) : null;

        String sql = "select dad0101data,dad0101hist,dad0101valor,dad0101es " +
                        "from dad0101 " +
                        whereAdiantamento+
                        whereTipo +
                        wherePeriodo+
                "order by dad0101data ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroAdiantamento,parametroPeriodoIni,parametroPeriodoFin );
    }


}
//meta-sis-eyJkZXNjciI6IlNDRiAtIE1vdmltZW50byBkZSBBZGlhbnRhbWVudG9zL0Ftb3J0aXphw6fDtWVzIiwidGlwbyI6InJlbGF0b3JpbyJ9