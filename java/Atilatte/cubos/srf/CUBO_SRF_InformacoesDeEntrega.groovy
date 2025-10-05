package Atilatte.cubos.srf;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import sam.core.variaveis.MDate
import br.com.multitec.utils.DateUtils

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro
import java.time.LocalDate

import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sam.model.entities.aa.Aac10;


public class CUBO_SRF_InformacoesDeEntrega extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CUBO - SRF - Informações De Entrega";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        return criarFiltros(
                "operacao", 1,
                "naoEfetuada", true,
                "efetuada", true,
                "devolvida", true,
        );
    }

    @Override
    public DadosParaDownload executar() {
        List<TableMap> dados = dadosRelatorio();
        List<TableMap> documentos = new ArrayList();

        for (TableMap registro : dados) {

            String codRedesp = registro.getString("codigoRedespNota");
            def dataEntrega = codRedesp == null ? registro.getDate("dataEntregaNota") : registro.getDate("dtEntregaRedesp");
            def prazo = registro.getInteger("prazo");
            def dataEmissaoNota = registro.getDate("dataEmissaoNota");
            def dataEmissaoPed = registro.getDate("dataEmissaoPed");
            Integer numPed = getInteger("numPed");
            Long idCarga = registro.getLong("bfc10id");
            String ocorrencias = "";
            String situacao = "";
            LocalDate dataPrevista = null;
            String municipio = registro.getString("cidade");
            String estado = registro.getString("estado");
            Integer status = registro.getInteger("status");


            // Recupera o ano atual
            int anoAtual = LocalDate.now().getYear();

            List<LocalDate> feriados = new ArrayList<>();

            // Feriados do Ano
            feriados.add(LocalDate.of(anoAtual, Month.JANUARY, 1));
            feriados.add(LocalDate.of(anoAtual, Month.FEBRUARY, 20));
            feriados.add(LocalDate.of(anoAtual, Month.FEBRUARY, 21));
            feriados.add(LocalDate.of(anoAtual, Month.APRIL, 7));
            feriados.add(LocalDate.of(anoAtual, Month.APRIL, 21));
            feriados.add(LocalDate.of(anoAtual, Month.MAY, 1));
            feriados.add(LocalDate.of(anoAtual, Month.JUNE, 8));
            feriados.add(LocalDate.of(anoAtual, Month.SEPTEMBER, 7));
            feriados.add(LocalDate.of(anoAtual, Month.OCTOBER, 12));
            feriados.add(LocalDate.of(anoAtual, Month.NOVEMBER, 2));
            feriados.add(LocalDate.of(anoAtual, Month.NOVEMBER, 15));
            feriados.add(LocalDate.of(anoAtual, Month.DECEMBER, 25));

            // Busca quantidade de dias uteis da data do pedido até o faturamento da nota
            int diasPedido = contarDiasUteis(dataEmissaoPed, dataEmissaoNota, feriados, numPed);

            // Define o prazo de entrega da nota de acordo com a data de entrega
            if (prazo == null) interromper("Não foi encontrado prazo para calculo do prazo de entrega no município " + municipio + "-" + estado)

            // Data prevista de entrega da nota (soma a data de emissao mais os dias uteis no município)
            dataPrevista = somarDiasUteis(dataEmissaoNota, prazo, feriados);

            // Define o status do prazo de entrega da nota
            if (dataEntrega != null) {
                if (dataPrevista >= dataEntrega) {
                    situacao = "Dentro do Prazo"
                } else {
                    situacao = "Fora do Prazo"
                }
            }

            // Define o status da entrega
            String statusCarga;

            if (status == 0) {
                statusCarga = "0 - Não Efetuada";
            } else if (status == 1) {
                statusCarga = "1 - Efetuada";
            } else if (status == 2) {
                statusCarga = "2 - Reentrega";
            } else {
                statusCarga = "3 - Devolvido";
            }

            if (registro.getString("eaa0115arquivo") == null) {
                registro.put("arquivoExt", "Não Anexado");
            } else {
                registro.put("arquivoExt", "Anexado")
            }


            registro.put("dataPrevista", dataPrevista);
            int diasUteisEntrega = contarDiasUteis(dataPrevista, dataEntrega, feriados, numPed);

            List<TableMap> tmOcorrencias = buscarOcorrencias(idCarga);

            BigDecimal valor = new BigDecimal(0);

            if (tmOcorrencias != null && tmOcorrencias.size() > 0) {
                for (TableMap tmOcorrencia : tmOcorrencias) {
                    valor += tmOcorrencia.getBigDecimal_Zero("bfc1001valor");
                    ocorrencias += tmOcorrencia.getString("bfc01descr") + "; ";
                }
            }

            registro.put("tempoProcPed", diasPedido);
            registro.put("status", statusCarga);
            registro.put("situacao", situacao);
            registro.put("dataEntrega", dataEntrega);
            registro.put("prazoReal", diasUteisEntrega + prazo);
            registro.put("ocorrencia", ocorrencias);
            registro.put("freteAgregado", valor);
            documentos.add(registro);

        }
        return gerarXLSX("CUBO_SRF_InformacoesDeEntrega", documentos)
    }

    private List<TableMap> dadosRelatorio() {

        Integer operacao = getInteger("operacao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        LocalDate[] dtEntrega = getIntervaloDatas("dataEntrega");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao")
        List<Long> entidade = getListLong("entidade");
        List<Long> despacho = getListLong("despacho");
        List<Long> redespacho = getListLong("redespacho");
        List<Long> item = getListLong("itens");
        List<Integer> statusCarga = getListInteger("statusCarga")
        Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;

        LocalDate dtEntradaSaidaIni = null;
        LocalDate dtEntradaSaidaFin = null;
        LocalDate dtEntregaIni = null;
        LocalDate dtEntregaFin = null;
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;

        if (dtEntradaSaida != null) {
            dtEntradaSaidaIni = dtEntradaSaida[0];
            dtEntradaSaidaFin = dtEntradaSaida[1];
        }

        if (dtEntrega != null) {
            dtEntregaIni = dtEntrega[0];
            dtEntregaFin = dtEntrega[1];
        }

        if (dtEmissao != null) {
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }


        String whereOperDocumento = operacao == 0 ? "and eaa01pedido.eaa01esMov = 0 " : "and eaa01pedido.eaa01esMov = 1 ";


        Query sql = null;


        sql = getSession().createQuery("select sum(bfc1001valor) as freteAgregado,cast(eaa01nota.eaa01json ->> 'valor_frete_redesp' as numeric(18,6)) as freteRedesp,cast(eaa01nota.eaa01json ->> 'volumes' as numeric(18,6)) as volumes, TO_DATE(cast(eaa01nota.eaa01json ->> 'dt_entrega_redesp' as text), 'YYYYMMDD') as dtEntregaRedesp,eaa01nota.eaa01totdoc as totDocNota, cast(eaa01nota.eaa01json ->> 'peso_liquido' as numeric(16,2)) as pesoLiquidoNota,   " +
                "cast(eaa01nota.eaa01json ->> 'peso_bruto' as numeric(16,2)) as pesoBrutoNota,abe01nota.abe01codigo as codEntNota, abe01nota.abe01na as naEntNota, abb01nota.abb01data as dataEmissaoNota,TO_CHAR(abb01nota.abb01data,'YYYY/MM') as periodoNota,   " +
                "eaa01nota.eaa01dtentrega as dataEntregaNota, eaa01nota.eaa01esdata as dataentradaSaida, abb01nota.abb01num as numDoc, aah01nota.aah01codigo as tipoDocnota,   " +
                "aah01nota.aah01nome as descrTipoDocNota, abe01despNota.abe01codigo as codigoDespNota, abe01despNota.abe01na as naDespNota,abe01redespNota.abe01codigo as codigoRedespNota, abe01redespNota.abe01na as naRedespNota,aag0201nome as cidade, aag02uf as estado, aah01pedido.aah01codigo as tipoDocPed, aah01pedido.aah01nome as descrTipoDocPed, abb01pedido.abb01num as numPed,   " +
                "abb01pedido.abb01data as dataEmissaoPed, cast(aag0201json ->> 'prazo' as integer) as prazo,abb01nota.abb01id,bfc10id,  " +
                "abb01carga.abb01num as numCarga, case when bfc1002entrega = 0 then '0-Não Efetuada' when bfc1002entrega = 1 then '1-Efetuada' when bfc1002entrega = 2 then '2-Reentregar' when bfc1002entrega = 3 then '3-Devolvido' end as statusEntrega," +
                "abb11codigo as codDepto, abb11nome as descrDepto, aae10codigo as codMotivo, aae10descr as descrMotivo, bfc1002entrega as status, aag02uf as estado, eaa01nota.eaa01hrentrega::text as horaEntrega, eaa0115arquivo, " +
                "bfc10ckodata, bfc10ckidata, aag03nome as regiao, abb01pedido.abb01operhora as horaPedido " +
                "from eaa01 eaa01nota  " +
                "inner join eaa0103 eaa0103nota on eaa0103nota.eaa0103doc = eaa01nota.eaa01id    " +
                "inner join abb01 abb01nota on abb01nota.abb01id = eaa01nota.eaa01central    " +
                "inner join abd01 abd01nota on abd01nota.abd01id = eaa01nota.eaa01pcd    " +
                "inner join abe01 abe01nota on abe01nota.abe01id = abb01nota.abb01ent    " +
                "inner join abe0101 on abe0101ent = abe01nota.abe01id and abe0101principal = 1    " +
                "inner join aag0201 on aag0201id = abe0101municipio    " +
                "inner join aag02 on aag02id = aag0201uf    " +
                "inner join abe02 on abe02ent = abe01nota.abe01id    " +
                "inner join abm01 abm01nota on abm01nota.abm01id = eaa0103nota.eaa0103item    " +
                "inner join abb10 abb10nota on abb10nota.abb10id = abd01nota.abd01opercod    " +
                "inner join aah01 aah01nota on aah01nota.aah01id = abd01tipo    " +
                "inner join eaa0102 eaa0102nota on eaa0102nota.eaa0102doc = eaa01nota.eaa01id    " +
                "inner join abe01 abe01despNota on abe01despNota.abe01id = eaa0102nota.eaa0102despacho    " +
                "left join abe01 abe01redespNota on abe01redespNota.abe01id = eaa0102nota.eaa0102redespacho    " +
                "left join abe0103 on abe0103ent = abe01nota.abe01id    " +
                "left join aba3001 on aba3001id = abe0103criterio    " +
                "left join eaa01032 on eaa01032itemsrf = eaa0103nota.eaa0103id    " +
                "left join eaa0103 eaa0103pedido on eaa0103pedido.eaa0103id = eaa01032itemscv    " +
                "left join eaa01 eaa01pedido on eaa01pedido.eaa01id = eaa0103pedido.eaa0103doc    " +
                "left join abd01 abd01pedido on abd01pedido.abd01id = eaa01pedido.eaa01pcd    " +
                "left join aah01 aah01pedido on aah01pedido.aah01id = abd01pedido.abd01tipo    " +
                "left join abb01 abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central " +
                "left join bfc1002 on bfc1002central = abb01nota.abb01id " +
                "left join abb11 on abb11id = bfc1002devdepto " +
                "left join aae10 on aae10id = bfc1002devmotivo " +
                "left join bfc10 on bfc10id = bfc1002carga " +
                "left join abb01 as abb01carga on abb01carga.abb01id = bfc10central " +
                "left join bfc1001 on bfc1001carga = bfc10id " +
                "left join eaa0115 on eaa0115doc = eaa01nota.eaa01id " +
                "left join eaa0101 on eaa0101doc = eaa01nota.eaa01id " +
                "left join aag03 on eaa0101regiao = aag03id " +
                "where true " +
                (dtEmissaoIni != null && dtEmissaoFin != null ? "AND abb01nota.abb01data between :dtEmissaoIni  and :dtEmissaoFin " : "") +
                (dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "and eaa01nota.eaa01esdata between :dtEntradaSaidaIni and :dtEntradaSaidaFin " : "") +
                (entidade != null ? "and abe01nota.abe01id in (:entidade) " : "") +
                (dtEntregaIni != null && dtEntregaFin != null ? "and eaa0103nota.eaa0103dtentrega between :dtEntregaIni and :dtEntregaFin " : "") +
                (item != null ? "and abm01nota.abm01id in (:item) " : "") +
                (statusCarga != null && statusCarga.size() > 0 ? "and bfc1002entrega in (:statusCarga) " : "") +
                (despacho != null && despacho.size() > 0 ? "and abe01despNota.abe01id in (:despacho) " : "") +
                (redespacho != null && redespacho.size() > 0 ? "and abe01redespNota.abe01id in (:redespacho) " : "") +
                "and eaa01nota.eaa01gc = :idEmpresa " +
                whereOperDocumento +
                "group by dtEntregaRedesp,eaa01nota.eaa01totdoc, pesoLiquidoNota, volumes,   " +
                "pesoBrutoNota,abe01nota.abe01codigo, abe01nota.abe01na, abb01nota.abb01data,periodoNota,   " +
                "eaa01nota.eaa01dtentrega, eaa01nota.eaa01esdata, abb01nota.abb01num, aah01nota.aah01codigo,   " +
                "aah01nota.aah01nome, abe01despNota.abe01codigo, abe01despNota.abe01na,abe01redespNota.abe01codigo, abe01redespNota.abe01na,aag0201nome, aag02uf, aah01pedido.aah01codigo, aah01pedido.aah01nome, abb01pedido.abb01num,   " +
                "abb01pedido.abb01data,prazo,freteRedesp,abb01nota.abb01id,bfc10id,  " +
                " numCarga, statusEntrega, codDepto,descrDepto,codMotivo, descrMotivo,bfc1002entrega,aag02uf, eaa01nota.eaa01hrentrega, eaa0115arquivo, aag03nome, abb01pedido.abb01operhora")

        if (dtEmissaoIni != null && dtEmissaoFin != null) {
            sql.setParameter("dtEmissaoIni", dtEmissaoIni);
            sql.setParameter("dtEmissaoFin", dtEmissaoFin);
        }

        if (dtEntradaSaidaIni != null && dtEntradaSaidaFin != null) {
            sql.setParameter("dtEntradaSaidaIni", dtEntradaSaidaIni);
            sql.setParameter("dtEntradaSaidaFin", dtEntradaSaidaFin);
        }

        if (entidade != null && entidade.size() > 0) {
            sql.setParameter("entidade", entidade);
        }

        if (dtEntregaIni != null && dtEntregaFin != null) {
            sql.setParameter("dtEntregaIni", dtEntregaIni);
            sql.setParameter("dtEntregaFin", dtEntregaFin);
        }

        if (item != null) {
            sql.setParameter("item", item);
        }

        if (statusCarga != null && statusCarga.size() > 0) {
            sql.setParameter("statusCarga", statusCarga);
        }

        if (despacho != null && despacho.size() > 0) {
            sql.setParameter("despacho", despacho);
        }

        if (redespacho != null && redespacho.size() > 0) {
            sql.setParameter("redespacho", redespacho);
        }


        sql.setParameter("idEmpresa", idEmpresa);

        return sql.getListTableMap()


    }

    private List<TableMap> buscarOcorrencias(Long idCarga) {

        String ocorrencias = "";

        String sql = "select bfc01descr, bfc1001valor from bfc10 " +
                "inner join bfc1001 on bfc1001carga = bfc10id " +
                "inner join bfc01 on bfc01id = bfc1001oco " +
                "where bfc10id = :idCarga "

        Parametro parametroCarga = Parametro.criar("idCarga", idCarga);

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroCarga);

    }

    private LocalDate somarDiasUteis(LocalDate dataInicial, int diasASomar, feriados) {
        LocalDate dataAtual = dataInicial;
        int diasSomados = 0;

        while (diasSomados < diasASomar) {
            dataAtual = dataAtual.plusDays(1);

            if (dataAtual.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    dataAtual.getDayOfWeek() != DayOfWeek.SUNDAY && !feriados.contains(dataAtual)) {
                diasSomados++;
            }
        }
        return dataAtual;
    }

    private contarDiasUteis(LocalDate dataInicial, LocalDate dataFinal, feriados, Integer numPed) {
        LocalDate dataAtual = dataInicial;

        int diasUteis = 0
        while (dataAtual < dataFinal) {
            dataAtual = dataAtual.plusDays(1);
            if (dataAtual.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    dataAtual.getDayOfWeek() != DayOfWeek.SUNDAY && !feriados.contains(dataAtual)) {
                diasUteis++;
            }
        }

        return diasUteis;
    }
}
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBUcmFuc3BvcnRlOiBORnMgRW1pdGlkYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBJbmZvcm1hw6fDtWVzIERlIEVudHJlZ2EiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBJbmZvcm1hw6fDtWVzIERlIEVudHJlZ2EiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNVQk8gLSBTUkYgLSBJbmZvcm1hw6fDtWVzIERlIEVudHJlZ2EiLCJ0aXBvIjoicmVsYXRvcmlvIn0=