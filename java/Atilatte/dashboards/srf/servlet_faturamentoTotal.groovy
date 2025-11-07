package Atilatte.dashboards.srf;

import org.apache.commons.text.StringSubstitutor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.apache.commons.text.StringSubstitutor

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto
import sam.server.samdev.utils.Parametro
import java.text.NumberFormat;
import java.util.Locale;

import java.time.LocalDate;
import java.util.ArrayList;


public class servlet_faturamentoTotal extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Atilatte - Faturamento Total";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 3, 7, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() {

		List<LocalDate> listDatas = new ArrayList();
		List<BigDecimal> faturamentos = new ArrayList();
		def faturamentoTotal = 0
		int anoAtual = LocalDate.now().getYear();
		LocalDate dataAtual = LocalDate.now();

		for(int i = 1; i <= 12; i++){
			LocalDate novaData = LocalDate.of(anoAtual,i,1);
			listDatas.add(novaData);
		}
		for(datas in listDatas){
			LocalDate[] periodoMes = DateUtils.getStartAndEndMonth(datas);
			def faturamento = buscarfaturamentoPorMes(periodoMes);
			def devolucao = buscardevolucaoPorMes(periodoMes)
			faturamento = (faturamento == null ? 0 : faturamento ) - (devolucao == null ? 0 : devolucao);
			if(datas.getMonthValue() == dataAtual.getMonthValue()){
				faturamentoTotal = faturamento;
			}
			
			faturamentos.add(faturamento);
			
		}

		Locale local = new Locale("pt", "BR");
		NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(local);
		String faturamentoFormatado = formatoMoeda.format(faturamentoTotal);
		
		
		
		
		Map<String, Object> valoresDefault = Utils.map("faturamento",faturamentoFormatado)
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.srf.recurso_faturamentoTotal.html",valoresDefault);

		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"faturamento",faturamentos,
				))

		def resolvedString = sub.replace(dto.getScript())
		dto.setScript(resolvedString)
		
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto);
				
		
	}

	private BigDecimal buscarfaturamentoPorMes(LocalDate[] periodoMes){
		String sql = "select sum(eaa0103total) from eaa01 " +
					"inner join eaa0103 on eaa0103doc = eaa01id "+
					"inner join abb01 on abb01id = eaa01central "+
					"inner join abd01 on abd01id = eaa01pcd "+
					"inner join abb10 on abb10id = abd01operCod "+
					"where eaa01clasdoc = 1 "+
					"and abb01data between :dtInicio and :dtFim "+
					"and abb10tipocod = 1 "+
					"and eaa01gc = 1322578 "+
					"and eaa01cancdata is null ";
					
		Parametro p1 = Parametro.criar("dtInicio",periodoMes[0]);
		Parametro p2 = Parametro.criar("dtFim",periodoMes[1]);

		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2);
					
	}

	private BigDecimal buscardevolucaoPorMes(LocalDate[] periodoMes){
		String sql = "select sum(eaa0103total) from eaa01 " +
					"inner join eaa0103 on eaa0103doc = eaa01id "+
					"inner join abb01 on abb01id = eaa01central "+
					"inner join abd01 on abd01id = eaa01pcd "+
					"inner join abb10 on abb10id = abd01operCod "+
					"where eaa01clasdoc = 1 "+
					"and abb01data between :dtInicio and :dtFim "+
					"and abb10tipocod = 4 "+
					"and eaa01gc = 1322578 "+
					"and eaa01cancdata is null ";
					
		Parametro p1 = Parametro.criar("dtInicio",periodoMes[0]);
		Parametro p2 = Parametro.criar("dtFim",periodoMes[1]);

		return getAcessoAoBanco().obterBigDecimal(sql,p1,p2);
					
	}
}
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gRmF0dXJhbWVudG8gVG90YWwiLCJ0aXBvIjoic2VydmxldCIsInciOjMsImgiOjcsInJlc2l6ZSI6InRydWUiLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9