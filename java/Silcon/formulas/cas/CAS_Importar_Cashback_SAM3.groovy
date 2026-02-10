package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import sam.model.entities.aa.Aac01
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abf30
import sam.model.entities.da.Dad01
import sam.model.entities.da.Dad0101;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.server.samdev.utils.Parametro
import sam.server.srf.service.SRFService

import java.time.LocalDate

public class CAS_Importar_Cashback_SAM3 extends FormulaBase {

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        MultipartFile arquivo = get("arquivo");

        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        Integer linha = 0;

        while (txt.nextLine()) {
            linha++;
            String codEntidade = txt.getCampo(1);
            String codTipoCash = txt.getCampo(2);
            String nomeCashback = txt.getCampo(3);

            // Tipo Documento
            Abf30 abf30 = getSession().createCriteria(Abf30.class).addWhere(Criterions.eq("abf30codigo", codTipoCash)).addWhere(Criterions.eq("abf30gc", 1075797)).get(ColumnType.ENTITY);
            if (abf30 == null) interromper("Tipo de documento de cashback com o número " + codTipoCash + " não encontrado. Linha: " + linha);

            // Entidade
            Abe01 abe01 = getSession().createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", codEntidade)).addWhere(Criterions.eq("abe01gc", 1075797)).get(ColumnType.ENTITY);
            if (abe01 == null) interromper("Não foi encontrado a entidade com o código " + codEntidade.toString() + ". Linha: " + linha);

            criarCashback(abe01, abf30, nomeCashback, LocalDate.now());
        }
    }
    private void criarCashback(Abe01 abe01, Abf30 abf30, String nomeCashback, LocalDate data) {
        Dad01 dad01 = new Dad01();
        dad01.dad01ent = abe01;
        dad01.dad01tipo = abf30;
        dad01.dad01nome = nomeCashback.length() > 25 ? nomeCashback.substring(0, 24) : nomeCashback;
        dad01.dad01saldo = somarSaldoCashback(abe01.abe01id);
        dad01.dad01prop = 1;
        dad01.dad01dti = data;
        dad01.dad01dtf = data;
        dad01.dad01neg = 0;
        dad01.dad01eg = obterEmpresaAtiva();
        dad01.dad01gc = getSession().createCriteria(Aac01.class).addWhere(Criterions.eq("aac01id", 1075797)).get(ColumnType.ENTITY);

        getSession().persist(dad01);

        criarLancamentosVale(dad01, abe01.abe01id);
    }
    private BigDecimal somarSaldoCashback(Long idEntidade) {
        Aah01 aah01 = getSession().createCriteria(Aah01.class).addWhere(Criterions.eq("aah01codigo", "001")).get(ColumnType.ENTITY);

        if(aah01 == null) interromper("Não foi encontrado tipo de documento com o código 001.");

        String sql = "SELECT COALESCE(SUM(abb01valor), 0) AS valor FROM abb01 WHERE abb01ent = :idEntidade AND abb01tipo = :idTipoDoc AND abb01gc = 1075797";

        Parametro parametroEntidade = Parametro.criar("idEntidade", idEntidade);
        Parametro parametroTipoDoc = Parametro.criar("idTipoDoc", aah01.aah01id);

        return getAcessoAoBanco().obterBigDecimal(sql, parametroEntidade, parametroTipoDoc)
    }
    private List<TableMap> buscarLancamentosCentral(Long idEntidade){
        Aah01 aah01 = getSession().createCriteria(Aah01.class).addWhere(Criterions.eq("aah01codigo", "001")).get(ColumnType.ENTITY);

        if(aah01 == null) interromper("Não foi encontrado tipo de documento com o código 001.");

        String sql = "SELECT abb01valor, abb01id FROM abb01 WHERE abb01ent = :idEntidade AND abb01tipo = :idTipoDoc AND abb01gc = 1075797";

        Parametro parametroNumero = Parametro.criar("idEntidade", idEntidade);
        Parametro parametroTipoDoc = Parametro.criar("idTipoDoc", aah01.aah01id);

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumero, parametroTipoDoc)
    }
    private void criarLancamentosVale(Dad01 dad01, Long idEntidade){
        List<TableMap> lancamentos = buscarLancamentosCentral(idEntidade);

        for(lancamento in lancamentos){
            BigDecimal valor = lancamento.getBigDecimal_Zero("abb01valor");
            Long idCentral = lancamento.getLong("abb01id");
            try{
                Dad0101 dad0101 = new Dad0101();
                dad0101.dad0101cb = getSession().createCriteria(Dad01.class).addWhere(Criterions.eq("dad01id", dad01.dad01id)).get(ColumnType.ENTITY);
                dad0101.dad0101data = LocalDate.now();
                dad0101.dad0101es = 0;
                dad0101.dad0101valor = valor;
                dad0101.dad0101central = getSession().createCriteria(Abb01.class).addWhere(Criterions.eq("abb01id", idCentral)).get(ColumnType.ENTITY);

                getSession().persist(dad0101)
            }catch (Exception e){
                throw new ValidacaoException(e.getMessage())
            }
        }
    }
}

/*
    Arquivo Exemplo

    Cod Entidade|Tipo Documento|Nome do Cashback
    |0100031000|53|MARCELO RICARDO MOURA|

 */
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=