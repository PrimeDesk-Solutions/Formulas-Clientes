package multitec.baseDemo

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro

class SamPalm_ResumoFaturamento extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SRF - Resumo Faturamento";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		LocalDate data = LocalDate.now()
		Map<String, Object> filtrosDefault = new HashMap()
		filtrosDefault.put("data", data.format(DateTimeFormatter.ofPattern("yyyy")))
		return Utils.map("filtros", filtrosDefault)
	}

	@Override
	public DadosParaDownload executar() {
		String ano = getString("data")

		LocalDate dataIni = DateUtils.parseDate("01/01/" + ano)
		LocalDate dataFim = DateUtils.parseDate("31/12/" + ano)

		adicionarParametro("ano", ano)
		adicionarParametro("aac10rs", obterEmpresaAtiva().aac10na)
		adicionarParametro("periodo", dataIni.format("dd/MM/yyyy") + " a "+ dataFim.format("dd/MM/yyyy"))
		
		
		BigDecimal realizadoTotal = 0.00
		BigDecimal projetadoTotal = 0.00
		for(Integer i = 1; i <=12; i++) {
			String numMes = i < 10 ? "0"+i : i
			LocalDate[] periodo = DateUtils.getStartAndEndMonth( DateUtils.parseDate("01/"+numMes+"/" + ano) )
			
			BigDecimal realizado = buscarDadosFaturamento(periodo[0],periodo[1]) 
			adicionarParametro("realizado"+i, realizado == null ? 0.00 : realizado)
			realizadoTotal += realizado == null ? 0.00 : realizado
			
			BigDecimal projecao = buscarProjecao(periodo[0])
			adicionarParametro("projetado"+i, projecao == null ? 0.00 : projecao)
			projetadoTotal += projecao == null ? 0.00 : projecao
			
			BigDecimal diferenca = (realizado == null ? 0.00 : realizado) - ( projecao == null ? 0.00 : projecao)
			adicionarParametro("diferenca"+i, diferenca == null ? 0.00 : diferenca)
		}
		
		List<TableMap> dados = new ArrayList()
		TableMap tmR = new TableMap()
		tmR.put("gName", "Realizado")
		tmR.put("gValor", realizadoTotal)
		dados.add(tmR)
		
		TableMap tmP = new TableMap()
		tmP.put("gName", "Projetado")
		tmP.put("gValor", projetadoTotal)
		dados.add(tmP)

		return gerarPDF(dados);
	}

	private BigDecimal buscarDadosFaturamento(LocalDate dtIni, LocalDate dtFim) {

		String whereDataIniFim = " and abb01data between :dtIni and :dtFim "

		Parametro dataIni = Parametro.criar("dtIni", dtIni)
		Parametro dataFim = Parametro.criar("dtFim", dtFim)

		String sql = " select sum(eaa0103totDoc) as Total" +
				" from eaa0103 " +
				" inner join eaa01 on eaa01id = eaa0103doc " +
				" inner join abb01 on abb01id = eaa01central " +
				" inner join abb10 on abb10id = abb01operCod " +
				" inner join Aaj15 on Aaj15id = eaa0103cfop " +
				" where eaa01clasDoc = 1 and (abb10tipocod = 1 or aaj15codigo like '%124')  and eaa01cancdata is null and eaa01esmov = 1 " +
				whereDataIniFim + obterWherePadrao("eaa01", "and") +
				" group by abb01data "

		return getAcessoAoBanco().obterBigDecimal(sql, dataIni, dataFim)
	}
	
	private BigDecimal buscarProjecao(LocalDate data) {
		String sql = " select eac01valor from eac01 " + obterWherePadrao("eac01", "WHERE") + " and eac01ano = :ano and eac01mes = :mes "
		Parametro parametroAno = Parametro.criar("ano", data.year)
		Parametro parametroMes = Parametro.criar("mes", data.monthValue)
		
		return getAcessoAoBanco().obterBigDecimal(sql, parametroAno, parametroMes)
	}

	private String verificaMes(Integer mes) {

		String Mes
		switch(mes) {
			case 1:
				Mes = "Janeiro"
				break
			case 2:
				Mes = "Fevereiro"
				break
			case 3:
				Mes = "Março"
				break
			case 4:
				Mes = "Abril"
				break
			case 5:
				Mes = "Maio"
				break
			case 6:
				Mes = "Junho"
				break
			case 7:
				Mes = "Julho"
				break
			case 8:
				Mes = "Agosto"
				break
			case 9:
				Mes = "Setembro"
				break
			case 10:
				Mes = "Outubro"
				break
			case 11:
				Mes = "Novembro"
				break
			case 12:
				Mes = "Dezembro"
				break
		}
		return Mes
	}

	private BigDecimal bucarTotalDevolucao(LocalDate[] data){

		String whereData = "and abb01data between :dataini and :datafim"

		Parametro dataIni = Parametro.criar("dataini", data[0])
		Parametro dataFim = Parametro.criar("datafim", data[1])

		def sql = " select sum(eaa0103totDoc) " +
				" from eaa0103 " +
				" inner join eaa01 on eaa01id = eaa0103doc " +
				" inner join abb01 on abb01id = eaa01central " +
				" inner join abb10 on abb10id = abb01operCod " +
				" inner join aaj15 on Aaj15id = eaa0103cfop " +
				" inner join aaj03 on aaj03id = eaa01sitDoc " +
				" where eaa01clasDoc = 1 and eaa01cancdata isnull and aaj03codigo = '09' and abb10tipocod = 4" + whereData +
				obterWherePadrao("eaa01", "and")
		return getAcessoAoBanco().obterBigDecimal(sql, dataIni, dataFim)
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIFJlc3VtbyBGYXR1cmFtZW50byIsInRpcG8iOiJyZWxhdG9yaW8ifQ==