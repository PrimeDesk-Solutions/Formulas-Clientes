package Atilatte.dashboards.srf;


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
public class servletFaturamentoMensal extends ServletBase {
	
	@Override
	public String getNome() throws Exception {
		return "Atilatte - Vendas X Devoluções";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 12, 12, true, null);

	}

	@Override
	public ResponseEntity<Object> executar() {
		UiDto dto = buscarComponenteCustomizado("Atilatte.dashboards.srf.recursoAnaliseDeVendas.html");
		List<String> listTotalSaida = new ArrayList();
		List<String> listTotalEntrada = new ArrayList();
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

		for(data in listDatas ){
			LocalDate[] mesIniFim = DateUtils.getStartAndEndMonth(data);
			def faturamento = buscarfaturamentoPorMes(mesIniFim);
			def devolucao = buscardevolucaoPorMes(mesIniFim);

			String nomeMes = buscarNomeMes(data.getMonthValue())
			
			listTotalSaida.add(faturamento);
			listTotalEntrada.add(devolucao);
			meses.add("'"+nomeMes+"'")
			
		}

		StringSubstitutor sub = new StringSubstitutor(Utils.map(
				"saidas",listTotalSaida,
				"entradas",listTotalEntrada,
				"meses", meses
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

		BigDecimal total =  getAcessoAoBanco().obterBigDecimal(sql,p1,p2);

		return total.round(2)
					
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

		BigDecimal total = getAcessoAoBanco().obterBigDecimal(sql,p1,p2);

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
//meta-sis-eyJkZXNjciI6IkF0aWxhdHRlIC0gVmVuZGFzIFggRGV2b2x1w6fDtWVzICIsInRpcG8iOiJzZXJ2bGV0IiwidyI6MTIsImgiOjEyLCJyZXNpemUiOiJ0cnVlIiwidGlwb2Rhc2hib2FyZCI6ImNvbXBvbmVudGUifQ==