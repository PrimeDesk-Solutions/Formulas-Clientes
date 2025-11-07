package Profer.relatorios.srf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aag0201
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SRF_CartaCorrecao extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SRF - Carta Correção";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap();
		filtrosDefault.put("dataRetorno", DateUtils.getStartAndEndMonth(MDate.date()));
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("resumoOperacao", "0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		
		Integer resumoOperacao = getInteger("resumoOperacao");
		List<Long> idEntidade = getListLong("entidade");
		List<Long> idTipoDocumento = getListLong("tipo");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		LocalDate[] dataRetorno = getIntervaloDatas("dataRetorno");
		String dataCarta = get("dataCarta");

		String endereco = null;
		if(getVariaveis().getAac10().getAac10endereco() != null){
			if(getVariaveis().getAac10().getAac10numero() != null){
				endereco = StringUtils.concat(getVariaveis().getAac10().getAac10endereco(), ", ", getVariaveis().getAac10().getAac10numero());
			}else{
				endereco = getVariaveis().getAac10().getAac10endereco();
			}
			
			if(getVariaveis().getAac10().getAac10complem() != null) {
				endereco += " - " + getVariaveis().getAac10().getAac10complem();
			}
		}
		
		String dddfone = "(" + getVariaveis().getAac10().getAac10dddFone() + ")";
		String telefone = getVariaveis().getAac10().getAac10fone();
		String CEP = StringUtils.toNumberMask("#####-###", getVariaveis().getAac10().getAac10cep());
		String bairro = getVariaveis().getAac10().getAac10bairro();
		String cidade = getVariaveis().getAac10().getAac10municipio() != null ? obterMunicipio(getVariaveis().getAac10().getAac10municipio().getIdValue()).getAag0201nome() : null;
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10rs());
		params.put("ENDERECO", endereco);
		params.put("BAIRRO", bairro);
		params.put("CEP", "CEP: " + CEP);
		params.put("TELEFONE", telefone);
		params.put("DDDFONE", dddfone);
		
		LocalDate data = MDate.date();
		
		if (dataCarta == null) {
			params.put("DATA", cidade + ", " + data.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")).toString());
		} else {
			params.put("DATA", dataCarta);
		}

		List<TableMap> dados = obterDadosRelatorio(idEntidade, idTipoDocumento, numeroInicial, numeroFinal, resumoOperacao, dataRetorno);

		return gerarPDF("SRF_CartaCorrecao", dados);	
	}
	
	public List<TableMap> obterDadosRelatorio (List<Long> idEntidade, List<Long> idTipoDocumento, Integer numeroInicial, Integer numeroFinal, Integer resumoOperacao, LocalDate[] dataRetorno)  {
				
		String whereResumoOperacao = null;	
		if (resumoOperacao.equals(1)) {
			whereResumoOperacao = " and eaa01.eaa01esMov = " + Eaa01.ESMOV_SAIDA;	
		} else {
			whereResumoOperacao = " and eaa01.eaa01esMov = " + Eaa01.ESMOV_ENTRADA;
		}
		
		String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= '" + numeroInicial + "' and abb01.abb01num <= '" + numeroFinal + "'": "";
		String whereTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? " and aah01.aah01id IN (:idTipoDocumento)": "";
		String whereIdEntidade = idEntidade != null && idEntidade.size() > 0 ? " and abe01.abe01id IN (:idEntidade)": "";
		String whereDataRetorno = dataRetorno[0] != null && dataRetorno[1] != null ? " and eaa0114.eaa0114retData >= '" + dataRetorno[0] + "' and eaa0114.eaa0114retData <= '" + dataRetorno[1] + "'": "";
		String whereStatus = "and EAA0114status = 2 ";
		
		Parametro parametroEntidade = idEntidade != null && idEntidade.size() > 0 ? Parametro.criar("idEntidade", idEntidade) : null;
		Parametro parametroTipoDocumento = idTipoDocumento != null && idTipoDocumento.size() > 0 ? Parametro.criar("idTipoDocumento", idTipoDocumento) : null;
		
		String sql = " SELECT DISTINCT eaa01.eaa01id, abe01.abe01ni, eaa0114.eaa0114correcao, eaa0114.eaa0114retData, " +
				" eaa0114.eaa0114retHora, eaa0114.eaa0114retProt, eaa0114.eaa0114seq, abb01.abb01num, eaa01.eaa01nfeChave, eaa01.eaa01nfeData, eaa01.eaa01nfeHora, eaa01.eaa01nfeProt " +
				" FROM eaa01 eaa01 " +
				" INNER JOIN eaa0114 eaa0114 ON eaa0114.eaa0114doc = eaa01.eaa01id " +
				" INNER JOIN abb01 abb01 ON abb01.abb01id = eaa01.eaa01central " +
				" INNER JOIN aah01 aah01 ON aah01id = abb01.abb01tipo " +
				" INNER JOIN abe01 abe01 ON abe01id = abb01.abb01ent " + 
				getSamWhere().getWherePadrao(" WHERE ", Eaa01.class) +
				" AND eaa01.eaa01clasDoc = " + Eaa01.CLASDOC_SRF +
				whereNumero +
				whereTipoDocumento +
				whereIdEntidade +
				whereResumoOperacao +
				whereDataRetorno+
				whereStatus;


		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEntidade, parametroTipoDocumento); 
		return receberDadosRelatorio;
	}
	
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addJoin(Joins.fetch("aag0201uf"))
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
}
//meta-sis-eyJkZXNjciI6IlNSRiAtIENhcnRhIENvcnJlw6fDo28gIiwidGlwbyI6InJlbGF0b3JpbyJ9