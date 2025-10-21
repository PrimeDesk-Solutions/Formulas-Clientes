package Atilatte.relatorios.srf

import br.com.multitec.utils.collections.TableMap;
import org.apache.poi.ss.formula.functions.T;
import org.jfree.util.Log;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.utils.Parametro;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class ControleFrete extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "Controle - Frete";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("resumoOperacao", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        // -------- FILTROS DO RELATÓRIO --------//
        Integer operacao = getInteger("resumoOperacao");

        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");

        Integer imprimir = getInteger("impressao");

        List<Long> entidades = getListLong("entidade");

        List<Long> tipoDoc = getListLong("tipo");

        List<Long> despachos = getListLong("despacho");
        List<Long> redespachos = getListLong("redespacho");

        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");

        Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;

        // -------- CONDIÇÕES DO RELATÓRIO --------//
        String periodo = "";

        if (dataEmissao != null) {
            periodo = "Período: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();

        } else if (dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
        }

        String empresaCodNa = StringUtils.concat(empresa.aac10codigo + "-" + empresa.aac10na);

        params.put("empresa", empresaCodNa);
        params.put("periodo", periodo);
        params.put("operacao", operacao);

        List<TableMap> documentos = buscarDocumentos(operacao, numDocIni, numDocFin, entidades, tipoDoc, despachos, redespachos, dataEmissao, dtEntradaSaida, idEmpresa);

        for (doc in documentos) {

            Long idCarga = doc.getLong("idCarga");

            if (idCarga != null) {
                BigDecimal valorFrete = buscarValorFrete(idCarga);
                BigDecimal totalPesoBruto = buscarTotalPesoBruto(idCarga);
                BigDecimal vlrFreteKg = valorFrete / totalPesoBruto;
                BigDecimal pesoBrutoDoc = doc.getBigDecimal_Zero("pesoBrutoDoc");
                BigDecimal freteDoc = pesoBrutoDoc * vlrFreteKg;

                doc.put("valorFrete", freteDoc);

            } else {
                doc.put("valorFrete", new BigDecimal(0));
            }

        }

        if (imprimir === 1) {
            return gerarXLSX("SRF_ControleFrete(EXCEL)", documentos);

        } else {
            return gerarPDF("SRF_ControleFrete(PDF)", documentos);
        }
        
    }

    // -------- FUNÇÕES DO RELATÓRIO --------//
    private List<TableMap> buscarDocumentos(Integer operacao, Integer numDocIni, Integer numDocFin, List<Long> entidades,
                                            List<Long> tipoDoc, List<Long> despachos, List<Long> redespachos,
                                            LocalDate[] dataEmissao, LocalDate[] dtEntradaSaida, Long idEmpresa) {

        // Data Emissão Inicial e Final
        LocalDate dataEmissaoIni = null;
        LocalDate dataEmissaoFin = null;

        if (dataEmissao != null) {
            dataEmissaoIni = dataEmissao[0];
            dataEmissaoFin = dataEmissao[1];
        }

	   //Data De Entrada Inicial e Final
        LocalDate dataEntraSaidaIni = null;
        LocalDate dataEntraSaidaFin = null;

        if (dtEntradaSaida != null) {
            dataEntraSaidaIni = dataEmissao[0];
            dataEntraSaidaFin = dataEmissao[1];
        }

        String whereEmpresaAtiva = " where eaa01eg = :idEmpresa ";
        String whereNumIni = numDocIni != null ? " and doc.abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? " and doc.abb01num <= :numDocFin " : "";
        String whereEntidade = entidades != null && !entidades.isEmpty() ? " and ent.abe01id in (:entidade) " : "";
        String whereES = operacao.equals(1) ? " and eaa01esMov = 1 " : " and eaa01esMov = 0 ";
        String whereTipoDoc = tipoDoc != null && !tipoDoc.isEmpty() ? " and aah01id in (:tipoDoc) " : "";
        String whereDespacho = despachos != null && !despachos.isEmpty() ? " and desp.abe01id in (:despachos) " : "";
        String whereRedespacho = redespachos != null && !redespachos.isEmpty() ? " and redesp.abe01id in (:redespachos) " : "";
        String whereDataEmissao = dataEmissaoIni != null && dataEmissaoFin != null ? " and doc.abb01data between :dataEmissaoIni and :dataEmissaoFin " : "";
        String whereDataEntradaSaida = dataEntraSaidaIni != null && dataEntraSaidaFin != null ? " and eaa01esData between :dataEntraSaidaIni and :dataEntraSaidaFin " : "";

        Parametro parametroEmpresaAtiva = Parametro.criar("idEmpresa", idEmpresa);
        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroEntidade = entidades != null && !entidades.isEmpty() ? Parametro.criar("entidade", entidades) : null;
        Parametro parametroTipoDoc = tipoDoc != null && !tipoDoc.isEmpty() ? Parametro.criar("tipoDoc", tipoDoc) : null;
        Parametro parametroDespacho = despachos != null && !despachos.isEmpty() ? Parametro.criar("despachos", despachos) : null;
        Parametro parametroRedespacho = redespachos != null && !redespachos.isEmpty() ? Parametro.criar("redespachos", redespachos) : null;
        Parametro parametroDataEmissaoIni = dataEmissaoIni != null ? Parametro.criar("dataEmissaoIni", dataEmissaoIni) : null;
        Parametro parametroDataEmissFin = dataEmissaoFin != null ? Parametro.criar("dataEmissaoFin", dataEmissaoFin) : null;
        Parametro parametroEntraSaidaIni = dataEntraSaidaIni != null ? Parametro.criar("dataEntraSaidaIni", dataEntraSaidaIni) : null;
        Parametro parametroEntraSaidaFin = dataEntraSaidaFin != null ? Parametro.criar("dataEntraSaidaFin", dataEntraSaidaFin) : null;

        String sql = " SELECT doc.abb01data as dataEmissao, doc.abb01num as numDocumento, eaa01esData as dataEntraSaida, ent.abe01codigo as entidade, " +
                     " ent.abe01na as nomeEntidade, aah01codigo as tipoDoc, desp.abe01codigo as codDesp, desp.abe01na as nomeDesp,  COALESCE(redesp.abe01codigo,'') as codRedesp, " +
                     " COALESCE(redesp.abe01na,'') as nomeRedesp, aag02uf as sigla, eaa01totDoc as valor, " +
                     " COALESCE(cast(eaa01json ->> 'valor_frete_redesp' as numeric(18,6)), 0) as freteRedespacho, bfc10id as idCarga, carga.abb01num as numCarga, " +
                     " cast(eaa01json ->> 'peso_bruto' as numeric(18,6)) as pesoBrutoDoc " +
                     " from eaa01 " +
                     " inner join abb01 as doc on eaa01central = doc.abb01id " +
                     " inner join aah01 on doc.abb01tipo = aah01id " +
                     " inner join abe01 as ent on ent.abe01id = doc.abb01ent " +
                     " inner join eaa0102 on eaa0102doc = eaa01id " +
                     " inner join abe01 as desp on desp.abe01id = eaa0102despacho " +
                     " left join abe01 as redesp on redesp.abe01id = eaa0102redespacho " +
                     " inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 " +
                     " inner join aag0201 on aag0201id = abe0101municipio " +
                     " inner join aag02 on aag02id = aag0201uf " +
                     " left join bfc1002 on bfc1002central = doc.abb01id " +
                     " left join bfc10 on bfc10id = bfc1002carga " +
                     " left join abb01 as carga on carga.abb01id = bfc10central " +
                     " left join aah20 on aah20id = eaa0102veiculo " +
                     whereEmpresaAtiva +
                     whereNumIni +
                     whereNumFin +
                     whereEntidade +
                     whereES +
                     whereTipoDoc +
                     whereDespacho +
                     whereRedespacho +
                     whereDataEmissao +
                     whereDataEntradaSaida +
                     " order by doc.abb01num, desp.abe01codigo, redesp.abe01codigo";

        try {
            return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresaAtiva, parametroNumIni, parametroNumFin,
	                                                            parametroEntidade, parametroTipoDoc, parametroDespacho, parametroRedespacho,
	                                                            parametroDataEmissaoIni,
	                                                            parametroDataEmissFin, parametroEntraSaidaIni, parametroEntraSaidaFin);
        } catch (SQLException e) {
            Log.error("Erro ao buscar documentos: " + e.getMessage());

            throw new RuntimeException("Falha ao buscar documentos", e);
        }

    }

    // -------- FUNÇÃO PARA BUSCAR VALOR DO FRETE --------//
    private BigDecimal buscarValorFrete(Long idCarga) {

        String whereIdCarga = " where bfc10id = :idCarga ";

        Parametro parametroCarga = Parametro.criar("idCarga", idCarga);

        String sql = " select COALESCE(SUM(bfc1001valor), 0) as valorFrete " +
                     " from bfc10 " +
                     " inner join bfc1001 on bfc1001carga = bfc10id " +
                     whereIdCarga;

            return idCarga != null ? getAcessoAoBanco().obterBigDecimal(sql, parametroCarga) : new BigDecimal(0);
    }
    
    // -------- FUNÇÃO PARA BUSCAR TOTAL DO PESO BRUTO --------//
    private BigDecimal buscarTotalPesoBruto(Long idCarga) {

        String whereIdCarga = "where bfc10id = :idCarga ";

        Parametro parametroIdCarga = Parametro.criar("idCarga", idCarga);

        String sql = " select SUM(cast(eaa01json ->> 'peso_bruto' as numeric(18,6))) as totalBruto  " +
                     " from bfc10 " +
                     " inner join bfc1002 on bfc1002carga = bfc10id " +
                     " inner join abb01 on bfc1002central = abb01id " +
                     " inner join eaa01 on eaa01central = abb01id " +
                     whereIdCarga;

        return idCarga != null ? getAcessoAoBanco().obterBigDecimal(sql, parametroIdCarga) : new BigDecimal(0);
    }
}
//meta-sis-eyJkZXNjciI6IlNSRiBDb250cm9sZSBGcmV0ZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==