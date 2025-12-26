
package Silcon.formulas.cas

import sam.model.entities.aa.Aac10

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DecimalUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFileLeitura
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.dicdados.MultiEntity
import br.com.multitec.utils.jackson.JSonMapperCreator
import br.com.multitec.utils.validator.MultiValidationException
import br.com.multitec.utils.validator.ValidationMessage
import sam.dicdados.FormulaTipo
import sam.dicdados.SAM4DicDados
import sam.model.entities.aa.Aah01
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb10
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abf15
import sam.model.entities.ab.Abf16
import sam.model.entities.ab.Abf20
import sam.model.entities.da.Daa01
import sam.server.samdev.formula.FormulaBase

class SCF_ImportarDocs extends FormulaBase {

    private MultipartFile arquivo;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_IMPORTAR_DOCS;
    }

    public void executar() {
        arquivo = get("arquivo");
        Long abb10id = get("abb10id");
        Long aah01id = get("aah01id");
        LocalDate abb01data = get("abb01data");

        List<Daa01> daa01s = new ArrayList<>();

        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        int linha = 1;
        while(txt.nextLine()) {
            if (txt.getCampo(1).equals("0") || txt.getCampo(1).equals("1")) {
                if(txt.getRegistro().size() != 30) throw new ValidacaoException("A quantidade de campos está inválida. Linha: " + linha);

                Daa01 daa01 = importarDocumentos(txt, linha, abb10id, aah01id, abb01data);
                daa01s.add(daa01);
            }

            linha++;
        }

        put("daa01s", daa01s);
    }

    private Daa01 importarDocumentos(TextFileLeitura txt, int linha, Long abb10id, Long aah01id, LocalDate abb01data) {
        MultiValidationException validacoes = new MultiValidationException();

        if (txt.getCampo(1) == null || txt.getCampo(1).length() == 0) validacoes.addToValidations(new ValidationMessage("O tipo do documento financeiro não foi informado no txt. Linha: " + linha));
        if (txt.getCampo(12) == null || txt.getCampo(12).length() == 0) validacoes.addToValidations(new ValidationMessage("A data de vencimento nominal não foi informada no txt. Linha: " + linha));
        if (txt.getCampo(14) == null || txt.getCampo(14).length() == 0) validacoes.addToValidations(new ValidationMessage("O valor não foi informado no txt. Linha: " + linha));
        if (txt.getCampo(15) == null || txt.getCampo(15).length() == 0) validacoes.addToValidations(new ValidationMessage("Não foi informado no txt se o documento é ou não é uma previsão. Linha: " + linha));
        if ((txt.getCampo(8) == null || txt.getCampo(8).length() == 0) && (txt.getCampo(9) == null || txt.getCampo(9).length() == 0)) validacoes.addToValidations(new ValidationMessage("A entidade não foi informada no txt. Linha: " + linha));
        if ((txt.getCampo(28) == null || txt.getCampo(28).length() == 0) && (txt.getCampo(9) == null || txt.getCampo(9).length() == 0)) validacoes.addToValidations(new ValidationMessage("Não foi informado a empresa do documento: " + linha));

        if (validacoes.hasMessages()) throw validacoes;

        Abf20 abf20 = txt.getCampo(2) == null || txt.getCampo(2).length() <= 0 ? null
                : session.createCriteria(Abf20.class).addFields("abf20id, abf20oper").addWhere(samWhere.getCritPadrao(Abf20.class))
                .addWhere(Criterions.eq("abf20codigo", txt.getCampo(2))).get(ColumnType.ENTITY);

        Abb01 abb01 = new Abb01();
        Integer idEmpresa = Integer.parseInt(txt.getCampo(28))
        Aac10 eg = session.createCriteria(Aac10.class).addWhere(Criterions.eq("aac10id",idEmpresa)).get(ColumnType.ENTITY) // Empresa Documentos
        abb01.setAbb01eg(eg);
        Abb10 abb10 = abb10id != null ? session.get(Abb10.class, abb10id) : null;
        abb01.setAbb01operCod(abb10);
        Aah01 aah01 = null;
        if (txt.getCampo(3) != null && txt.getCampo(3).length() > 0) aah01 = session.get(Aah01.class, Criterions.and(samWhere.getCritPadrao(Aah01.class), Criterions.eq("aah01codigo", txt.getCampo(3))));
        else aah01 = session.get(Aah01.class, aah01id);
        abb01.setAbb01tipo(aah01);
        if ((aah01.getAah01numeracao().equals(Aah01.NUMERACAO_UNICA_E_MANUAL) || aah01.getAah01numeracao().equals(Aah01.NUMERACAO_NAO_UNICA_E_MANUAL)) && (txt.getCampo(4) == null || txt.getCampo(4).length() == 0)) throw new ValidacaoException("O número da central de documento não foi informado. Linha: " + linha);
        abb01.setAbb01num(aah01.getAah01numeracao().equals(Aah01.NUMERACAO_UNICA_E_AUTOMATICA) ? 0 : Integer.parseInt(txt.getCampo(4)));
        abb01.setAbb01parcela(txt.getCampo(6));
        abb01.setAbb01quita(0);
        LocalDate data = txt.getCampo(7) == null || txt.getCampo(7).length() == 0 ? abb01data : LocalDate.parse(txt.getCampo(7), DateTimeFormatter.ofPattern("ddMMyyyy"));
        abb01.setAbb01data(data);
        abb01.setAbb01serie(txt.getCampo(5));
        Abe01 abb01ent = null;
        if (txt.getCampo(8) != null && txt.getCampo(8).length() > 0) {
            abb01ent = session.createCriteria(Abe01.class).addWhere(samWhere.getCritPadrao(Abe01.class)).addWhere(Criterions.eq("abe01codigo", txt.getCampo(8))).get(ColumnType.ENTITY);
        } else if (txt.getCampo(9) != null || txt.getCampo(9).length() > 0) {
            abb01ent = session.createCriteria(Abe01.class).addWhere(samWhere.getCritPadrao(Abe01.class)).addWhere(Criterions.eq(Fields.regexReplace("abe01ni", "'[^0-9]'", "''"), StringUtils.extractNumbers(txt.getCampo(9)))).get(ColumnType.ENTITY);
        }
        if (abb01ent == null) throw new ValidacaoException("Não foi encontrado a entidade informada. Linha: " + linha);
        abb01.setAbb01ent(abb01ent);

        Daa01 daa01 = new Daa01();
        daa01.setDaa01central(abb01);
        daa01.setDaa01rp(Integer.parseInt(txt.getCampo(1)));
        daa01.setDaa01previsao(Integer.parseInt(txt.getCampo(15)));


        Abf15 abf15 = txt.getCampo(10) != null && txt.getCampo(10).length() > 0 ? session.createCriteria(Abf15.class).addWhere(Criterions.eq("abf15codigo", txt.getCampo(10))).addWhere(samWhere.getCritPadrao(Abf15.class)).get(ColumnType.ENTITY) : null;
        if (abf15 == null) abf15 = buscaPortador(abb01ent.getAbe01id(), daa01.getDaa01rp());
        daa01.setDaa01port(abf15);

        Abf16 abf16 = txt.getCampo(11) != null && txt.getCampo(11).length() > 0 ? session.createCriteria(Abf16.class).addWhere(Criterions.eq("abf16codigo", txt.getCampo(11))).addWhere(samWhere.getCritPadrao(Abf16.class)).get(ColumnType.ENTITY) : null;
        if (abf16 == null) abf16 = buscaOperacao(abb01ent.getAbe01id(), daa01.getDaa01rp());
        daa01.setDaa01oper(abf16);
        daa01.setDaa01dtVctoN(LocalDate.parse(txt.getCampo(12), DateTimeFormatter.ofPattern("ddMMyyyy")));
        daa01.setDaa01dtVctoR(txt.getCampo(13) == null || txt.getCampo(13).length() == 0 ? null : LocalDate.parse(txt.getCampo(13), DateTimeFormatter.ofPattern("ddMMyyyy")));

        BigDecimal daa01valor = DecimalUtils.create(txt.getCampo(14)).divide(100).round(2).get();
        daa01.setDaa01valor(daa01valor);
        daa01.setDaa01liquido(BigDecimal.ZERO);
        daa01.getDaa01central().setAbb01valor(daa01valor);

        daa01.setDaa01rep0(buscaEntidade(txt.getCampo(16)));
        daa01.setDaa01txComis0(extraiTxComis(txt.getCampo(21)));
        daa01.setDaa01rep1(buscaEntidade(txt.getCampo(17)));
        daa01.setDaa01txComis1(extraiTxComis(txt.getCampo(22)));
        daa01.setDaa01rep2(buscaEntidade(txt.getCampo(18)));
        daa01.setDaa01txComis2(extraiTxComis(txt.getCampo(23)));
        daa01.setDaa01rep3(buscaEntidade(txt.getCampo(19)));
        daa01.setDaa01txComis3(extraiTxComis(txt.getCampo(24)));
        daa01.setDaa01rep4(buscaEntidade(txt.getCampo(20)));
        daa01.setDaa01txComis4(extraiTxComis(txt.getCampo(25)));

        daa01.setDaa01obs(txt.getCampo(26));
        String json = txt.getCampo(27);
        daa01.setDaa01json(json != null && json.length() > 0 ? JSonMapperCreator.create().read(json, TableMap.class) : null);

        if (abf20 != null) daa01.abf20plf = abf20;

        return daa01;
    }

    private Abf15 buscaPortador(Long abe01id, Integer abf20oper) {
        String clazz = abf20oper.equals(Abf20.OPER_A_RECEBER) ? "Abe02" : "Abe03";
        Class<? extends MultiEntity> entity = SAM4DicDados.INSTANCE.getEntityClass(clazz);
        Long abf15id = session.createQuery("SELECT " + clazz.toLowerCase() + "port FROM " + clazz + " WHERE " + clazz.toLowerCase() + "ent = :abe01id " + samWhere.getWherePadrao("AND", entity)).setParameter("abe01id", abe01id).getUniqueResult(ColumnType.LONG);
        if (abf15id == null) return null;
        return session.get(Abf15.class, abf15id);
    }

    private Abf16 buscaOperacao(Long abe01id, Integer abf20oper) {
        String clazz = abf20oper.equals(Abf20.OPER_A_RECEBER) ? "Abe02" : "Abe03";
        Class<? extends MultiEntity> entity = SAM4DicDados.INSTANCE.getEntityClass(clazz);
        Long abf16id = session.createQuery("SELECT " + clazz.toLowerCase() + "oper FROM " + clazz + " WHERE " + clazz.toLowerCase() + "ent = :abe01id " + samWhere.getWherePadrao("AND", entity)).setParameter("abe01id", abe01id).getUniqueResult(ColumnType.LONG);
        if (abf16id == null) return null;
        return session.get(Abf16.class, abf16id);
    }

    private Abe01 buscaEntidade(String abe01codigo) {
        if (abe01codigo == null || abe01codigo.length() <= 0) return null;
        return session.createCriteria(Abe01.class).addWhere(Criterions.eq("abe01codigo", abe01codigo)).addWhere(samWhere.getCritPadrao(Abe01.class)).get(ColumnType.ENTITY);
    }

    private BigDecimal extraiTxComis(String txComis) {
        if (txComis == null || txComis.length() <= 0) return null;
        return DecimalUtils.create(txComis).divide(100).round(2).get();
    }

    /*
        |Tipo do Documento (Recebimento / Pagamento)|PLF|Tipo Documento|Número Doc|Série|Parcela|Data Doc|Entidade|NI Entidade(Opcional)|Portador|Carteira|Vcto Nominal|vcto Real|Valor|Previsao|representante0|representante1|representante2|
        representante3|representante4|Tx Comiss 0|Tx Comiss 1|Tx Comiss 2|Tx Comiss 3|Tx Comiss 4|Observações|Campos Livres|id empresa|

        |1|002|66|3749|2025|01|15012025|0200971000||0001|01|15012025|15012025|3749|0|||||||||||Teste de Observação|{"teste": "teste"}|1371888|
     */

}