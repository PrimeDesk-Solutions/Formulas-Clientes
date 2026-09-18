package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase

class SamPalm_EstatisticasTrabalhadoresIdade extends RelatorioBase {
	
	@Override
	public String getNomeTarefa() {
		return "Estatística - Trabalhadores por Idade";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Integer faixaIdade1 = 0
		Integer faixaIdade2 = 0
		Integer faixaIdade3 = 0
		Integer faixaIdade4 = 0
		Integer faixaIdade5 = 0
		Integer idadeTrabalhador = 0

		List<TableMap> idades = buscarAnos()
		
		for(idade in idades) {
			Integer anoAtual = idade.getDate("dataatual").year
			Integer anoNasc = idade.getDate("abh80nascData").year
			idadeTrabalhador = (anoAtual - anoNasc)
			
			if(idadeTrabalhador >= 18 && idadeTrabalhador <= 30) {
				faixaIdade1++
			}else if(idadeTrabalhador >= 31 && idadeTrabalhador <= 40) {
				faixaIdade2++
			}else if(idadeTrabalhador >= 41 && idadeTrabalhador <= 50) {
				faixaIdade3++
			}else if(idadeTrabalhador >= 51 && idadeTrabalhador <= 60) {
				faixaIdade4++
			}else if(idadeTrabalhador > 60) {
				faixaIdade5++
			}
			idadeTrabalhador = 0
		}
		return criarFiltros("idades",[faixaIdade1, faixaIdade2, faixaIdade3, faixaIdade4, faixaIdade5]);
	}

	@Override
	public DadosParaDownload executar() {
		return null;
	}
	
	private List<TableMap> buscarAnos() {
		
		String sql = " select CURRENT_DATE as dataatual, abh80nascData " +
				  " from abh80 " + obterWherePadrao("abh80","where")
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql)
		
	}

	
}
//meta-sis-eyJkZXNjciI6IkVzdGF0w61zdGljYSAtIFRyYWJhbGhhZG9yZXMgcG9yIElkYWRlIiwidGlwbyI6InJlbGF0b3JpbyJ9