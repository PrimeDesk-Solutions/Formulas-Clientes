package Atilatte.relatorios.srf

import groovy.swing.table.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SRF_DocumentosSemAnexo extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Documentos Sem Anexos";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntrega = getIntervaloDatas("dataEntrega");
        List<Long> despacho = getListLong("despacho");
        Long idEmpresa = obterEmpresaAtiva().getAac10id();

        params.put("titulo", "SRF - Documentos Sem Anexos");
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() +" "+ obterEmpresaAtiva().getAac10na());

        List<TableMap> dados = buscarDadosRelatorio(dtEmissao, dtEntrega, despacho, idEmpresa);

        return gerarPDF("SRF_DocumentosSemAnexos", dados)
    }

    private List<TableMap> buscarDadosRelatorio(LocalDate[] dtEmissao, LocalDate[] dtEntrega, List<Long> despacho, Long idEmpresa){

        // Data Emissao
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;

        if(dtEmissao != null){
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        // Data Entrega
        LocalDate dtEntregaIni = null;
        LocalDate dtEntregaFin = null;

        if(dtEntrega != null){
            dtEntregaIni = dtEntrega[0];
            dtEntregaFin = dtEntrega[1];
        }

        String whereDtEmissao = dtEmissao != null ? "and abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereDtEntrega = dtEntrega != null ? "and eaa0103dtEntrega between :dtEntregaIni and :dtEntregaFin " : "";
        String whereDespachos = despacho != null && despacho.size() > 0 ? "and desp.abe01id in (:despacho) " : "";

        Parametro parametroDtEmissaoIni = dtEmissaoIni != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null;
        Parametro parametroDtEmissaoFin = dtEmissaoFin != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro parametroDtEntregaIni = dtEntregaIni != null ? Parametro.criar("dtEntregaIni", dtEntregaIni) : null;
        Parametro parametroDtEntregaFin = dtEntregaFin != null ? Parametro.criar("dtEntregaFin", dtEntregaFin) : null;
        Parametro parametroDespacho = despacho != null && despacho.size() > 0 ? Parametro.criar("despacho", despacho) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa)

        String sql = "select distinct abb01num as numDoc, abb01data as dtEmissao, eaa0103dtentrega as dtEntrega, ent.abe01codigo as codEntidade, ent.abe01na as nomeEntidade, " +
                        "desp.abe01codigo as codDesp, desp.abe01na as naDesp " +
                        "from eaa01 " +
                        "inner join abb01 on abb01id = eaa01central " +
                        "inner join abe01 as ent on ent.abe01id = abb01ent " +
                        "inner join eaa0102 on eaa0102doc = eaa01id " +
                        "inner join eaa0103 on eaa0103doc = eaa01id " +
                        "inner join abe01 as desp on desp.abe01id = eaa0102despacho " +
                        "left join eaa0115 on eaa0115doc = eaa01id " +
                        "where eaa01esmov = 1 " +
                        "and eaa01clasdoc = 1 " +
                        "and eaa0115id is null " +
                        "and eaa01gc = :idEmpresa " +
                        whereDtEmissao +
                        whereDtEntrega +
                        whereDespachos +
                        "order by desp.abe01codigo, abb01num ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntregaIni, parametroDtEntregaFin, parametroDespacho, parametroEmpresa)

    }
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIERvY3VtZW50b3MgU2VtIEFuZXhvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==