package multitec.baseDemo

import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SamPalm_EstatisticasTrabalhadoresGenero extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "Estatística - Trabalhadores por Gênero";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		
		Integer qtdMasculino = buscarGeneroMasculino()
		Integer qtdFeminino = buscarGeneroFeminino()
		
		return criarFiltros("generos",[qtdMasculino,qtdFeminino])
	}

	@Override
	public DadosParaDownload executar() {
		return null;
	}

	private Integer buscarGeneroMasculino(){

		String sql = " select count(abh80sexo) " +
				" from abh80 " +
				" where abh80sexo = 0 " + obterWherePadrao("abh80","and")

		return getAcessoAoBanco().obterInteger(sql)
	}

	private Integer buscarGeneroFeminino(){

		String sql = "select count(abh80sexo) " +
				" from abh80 " +
				" where abh80sexo = 1 " + obterWherePadrao("abh80","and")

		return getAcessoAoBanco().obterInteger(sql)
	}
}
//meta-sis-eyJkZXNjciI6IkVzdGF0w61zdGljYSAtIFRyYWJhbGhhZG9yZXMgcG9yIEfDqm5lcm8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=