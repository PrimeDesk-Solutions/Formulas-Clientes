package Atilatte.dashboards.scf

import org.apache.tomcat.jni.Local;
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType

import java.time.LocalDate
import org.apache.commons.text.StringSubstitutor
import org.springframework.http.ResponseEntity
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import sam.core.variaveis.MDate
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto
import sam.server.samdev.utils.Parametro
import java.text.NumberFormat;
import java.util.Locale;
public class SCF_Servlet_Documentos_Financeiros extends ServletBase {

    @Override
    public String getNome() throws Exception {
        return "Atilatte - Documentos Financeiros (A Vencer X Vencidos)";
    }

    @Override
    public DashboardMetadata getMetadata() throws Exception {
        return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);

    }

    @Override
    public ResponseEntity<Object> executar() {
        UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.scf.SCF_Recurso_Documentos_Financeiros.html");
        List<String> listTotalAvencer = new ArrayList();
        List<String> listTotalVencidos = new ArrayList();
        List<LocalDate> listDatas = new ArrayList();
        List<BigDecimal> meses = new ArrayList();
        LocalDate dataAtual = LocalDate.now();
        Integer anoAtual = dataAtual.year;


        for(int i = 1; i <= 12; i++){
            LocalDate data = LocalDate.of(anoAtual,i,1);
            listDatas.add(data)
        }

        Locale local = new Locale("pt", "BR");
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(local);
        LocalDate dtAtual = LocalDate.now();
        Long idEmpresa = obterEmpresaAtiva().getAac10id();

        for(data in listDatas ){
            LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(data);
            def docAVencer = buscarDocumentosAVencer(mesIniFim,dtAtual, idEmpresa);
            def docVencidos = buscarDocumentosVencidos(mesIniFim,dtAtual, idEmpresa);

            String nomeMes = buscarNomeMes(data.getMonthValue())

            listTotalAvencer.add(docAVencer);
            listTotalVencidos.add(docVencidos);

            meses.add("'"+nomeMes+"'")

        }

        StringSubstitutor sub = new StringSubstitutor(Utils.map(
                "vencer",listTotalAvencer,
                "vencidos",listTotalVencidos,
                "meses", meses
        ))

        def resolvedString = sub.replace(dto.getScript())
        dto.setScript(resolvedString)
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto);

    }

    private BigDecimal buscarDocumentosAVencer(LocalDate[] periodoMes, LocalDate dtAtual, Long idEmpresa){

        String sql = "select SUM(daa01valor) " +
                        "from daa01 " +
                        "inner join abb01 on abb01id = daa01central " +
                        "where abb01data between :dtInicio and :dtFim " +
                        "and daa01dtvcton >= :dtAtual " +
                        "and daa01gc = :idEmpresa " +
                        "and daa01dtpgto is null " +
                        "and daa01dtbaixa is null "

        Parametro paramDtInicio = Parametro.criar("dtInicio",periodoMes[0]);
        Parametro parametroDtFim = Parametro.criar("dtFim",periodoMes[1]);
        Parametro paramDtAtual = Parametro.criar("dtAtual",dtAtual);
        Parametro paramEmpresa = Parametro.criar("idEmpresa",idEmpresa);

        BigDecimal total = getAcessoAoBanco().obterBigDecimal(sql,paramDtInicio,parametroDtFim, paramDtAtual, paramEmpresa);

        return total.round(2)

    }

    private BigDecimal buscarDocumentosVencidos(LocalDate[] periodoMes, LocalDate dtAtual, Long idEmpresa){

        String sql = "select SUM(daa01valor) " +
                        "from daa01 " +
                        "inner join abb01 on abb01id = daa01central " +
                        "where abb01data between :dtInicio and :dtFim " +
                        "and daa01dtvcton < :dtAtual " +
                        "and daa01gc = :idEmpresa " +
                        "and daa01dtpgto is null " +
                        "and daa01dtbaixa is null "

        Parametro paramDtInicio = Parametro.criar("dtInicio",periodoMes[0]);
        Parametro parametroDtFim = Parametro.criar("dtFim",periodoMes[1]);
        Parametro paramDtAtual = Parametro.criar("dtAtual",dtAtual);
        Parametro paramEmpresa = Parametro.criar("idEmpresa",idEmpresa);

        BigDecimal total = getAcessoAoBanco().obterBigDecimal(sql,paramDtInicio,parametroDtFim, paramDtAtual, paramEmpresa);

        return total.round(2)

    }

    private String buscarNomeMes(Integer numMes){
        String nomeMes
        switch(numMes){
            case 1:
                nomeMes = "Janeiro";
                break;
            case 2:
                nomeMes = "Fevereiro";
                break;
            case 3:
                nomeMes = "Março";
                break;
            case 4:
                nomeMes = "Abril";
                break;
            case 5:
                nomeMes = "Maio";
                break;
            case 6:
                nomeMes = "Junho";
                break;
            case 7:
                nomeMes = "Julho";
                break;
            case 8:
                nomeMes = "Agosto";
                break;
            case 9:
                nomeMes = "Setembro";
                break;
            case 10:
                nomeMes = "Outubro";
                break;
            case 11:
                nomeMes = "Novemmbro";
                break;
            case 12:
                nomeMes = "Dezembro";
                break;
        }
    }
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gRG9jdW1lbnRvcyBGaW5hbmNlaXJvcyhBIFZlbmNlciBYIFZlbmNpZG9zKSIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==