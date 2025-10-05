package Atilatte.formulas.scf

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.ValidacaoException
import ch.qos.logback.core.util.FileUtil
import com.mchange.io.FileUtils
import groovy.swing.table.TableMap
import org.apache.poi.ss.formula.functions.Column
import sam.model.entities.ab.Aba20
import sam.model.entities.ab.Aba2001
import sam.model.entities.ab.Abe01
import sam.model.entities.da.Daa01;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import org.springframework.web.multipart.MultipartFile;
import br.com.multitec.utils.TextFileLeitura
import org.apache.commons.io.FileUtils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro;

import javax.swing.text.html.parser.Entity
import java.sql.SQLException
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



public class SCF_VarreduraItau extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_IMPORTAR_DOCS;
    }

    @Override
    public void executar() {
        MultipartFile arquivo = get("arquivo");
        String nomeArquivo = arquivo.getOriginalFilename();

        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, );
        txt.nextLine() // Pula a primeira linha
        txt.nextLine() // Pula a segunda linha
        Integer linha = 3;

        // Repositório de dados
        Aba20 aba20 = getSession().get(Aba20.class, Criterions.eq("aba20id", 78751404))
        if(aba20 == null) interromper("Repositório de dados não encontrado no sistema.");

        // Documentos no repositorio de dados
        List<Aba2001> aba2001s = getSession().createCriteria(Aba2001.class)
                                            .addFields("aba2001id, aba2001json")
                                            .addWhere(Criterions.eq("aba2001rd", aba20.aba20id))
                                            .getList(ColumnType.ENTITY);

        // Verifica se tem registros de um arquivo anterior no repositório de dados
        if(aba2001s != null && aba2001s.size() > 0){
            for (Aba2001 aba2001 : aba2001s){
                Long idLinhaRepositorio = aba2001.aba2001id;
                def tmAba2001 = aba2001.aba2001json;
                BigDecimal vlrDoc = tmAba2001.getBigDecimal_Zero("valor");
                String niEntidade = tmAba2001.getString("inscricao_entidade");
                String codBarras = tmAba2001.getString("cod_barras");
                String dtVencimento = tmAba2001.getString("dt_vencimento");
                LocalDate dtVencimentoFormatada = formatarData(dtVencimento, "yyyyMMdd");

                Daa01 daa01 = buscarDocumentoFinanceiro(vlrDoc, niEntidade, dtVencimentoFormatada);

                // Atualiza o código de barras do documento financeiro
                if(daa01 != null){
                    daa01.daa01codBarras = codBarras;
                    getSession().persist(daa01)
                    deletaLinhaRepositorio(idLinhaRepositorio);
                }
            }
        }

        while (txt.nextLine()){
            if(txt.getSubString(13,14) == "G"){
                String niEntidade = txt.getSubString(63,77); // 64 - 77
                String dtVencimento = txt.getSubString(107, 115) // 108 - 115
                String valorTitulo = txt.getSubString(115, 130) // 116 - 130
                String tipoInscricao = txt.getSubString(61, 62) // 61

                // Dados formatados
                String niEntidadeFormatado = formatarInscricaoEntidade(niEntidade, tipoInscricao);
                LocalDate dtVencimentoFormatado = formatarData(dtVencimento, "ddMMyyyy");
                BigDecimal valorTituloFormatado = Integer.parseInt(valorTitulo) / 100;

                Abe01 abe01 = session.createCriteria(Abe01.class).addWhere(samWhere.getCritPadrao(Abe01.class)).addWhere(Criterions.eq("abe01ni", niEntidadeFormatado)).get(ColumnType.ENTITY);

                // Monta o código de barras do documento
                String codBarras = txt.getSubString(17,61);

                // Busca o documento financeiro no banco
                Daa01 daa01 = buscarDocumentoFinanceiro(valorTituloFormatado, niEntidadeFormatado, dtVencimentoFormatado);

                // Documento não existente no sistema
                if(daa01 == null){
                    TableMap jsonRepositorio = new TableMap();
                    jsonRepositorio.put("codigo_entidade", abe01 != null ? abe01.abe01codigo : "CÓDIGO NÃO ENCONTRADO");
                    jsonRepositorio.put("na_entidade", abe01 != null ? abe01.abe01na : "NOME NÃO ENCONTRADO");
                    jsonRepositorio.put("inscricao_entidade", niEntidadeFormatado);
                    jsonRepositorio.put("valor", valorTituloFormatado);
                    jsonRepositorio.put("dt_vencimento", formatarData(dtVencimento, "ddMMyyyy"));
                    jsonRepositorio.put("nome_arquivo", nomeArquivo);
                    jsonRepositorio.put("cod_barras", codBarras);

                    gravarInformacoesRepositorio(jsonRepositorio);

                }else{
                    daa01.daa01codBarras = codBarras;
                    getSession().persist(daa01);
                }

                linha++
            }
        }

    }
    private LocalDate formatarData(String txtData, String formato){

        // Define o formatter com o padrão da string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formato);

        LocalDate data = LocalDate.parse(txtData, formato);

        return data;
    }

    private String formatarInscricaoEntidade(String inscricaoEntidade, String tipoInscricao) {
        // Retira os caracteres que não são números
        inscricaoEntidade = inscricaoEntidade.replaceAll("\\D", "");


        if (tipoInscricao.equals('2') && inscricaoEntidade.length() != 14){
            return "CNPJ Inválido"
        }else if(tipoInscricao.equals('1') && inscricaoEntidade.length() != 11){
            return "CPF Inválido"
        }

        if (tipoInscricao.equals('2')){
            // Formata CNPJ: 00.000.000/0000-00
            inscricaoEntidade = inscricaoEntidade.substring(0, 2) + "." +
                    inscricaoEntidade.substring(2, 5) + "." +
                    inscricaoEntidade.substring(5, 8) + "/" +
                    inscricaoEntidade.substring(8, 12) + "-"+
                    inscricaoEntidade.substring(12)
        }else{
            // Formata CPF: 000.000.000-00
            inscricaoEntidade = inscricaoEntidade.substring(0, 3) + "." +
                    inscricaoEntidade.substring(3, 6) + "." +
                    inscricaoEntidade.substring(6, 9) + "-" +
                    inscricaoEntidade.substring(9);
        }

        return inscricaoEntidade
    }

    private Daa01 buscarDocumentoFinanceiro(def vlrDoc, def niEntidade, def dtVencimentoFormatada ){

        return getSession().createCriteria(Daa01.class)
                .addFields("daa01id, abe01codigo, abe01na, abe01ni, abb01num")
                .addJoin(Joins.join("abb01", "daa01central = abb01id"))
                .addJoin(Joins.join("abe01", "abe01id = abb01ent"))
                .addWhere(Criterions.eq("daa01valor", vlrDoc))
                .addWhere(Criterions.eq("abe01ni",niEntidade))
                .addWhere(Criterions.eq("daa01dtVctoR", dtVencimentoFormatada))
                .get(ColumnType.ENTITY)
    }
    private void gravarInformacoesRepositorio(TableMap tmRepositorio){
        try{
            String sqlSeq = "SELECT COALESCE(MAX(aba2001lcto),0) + 1 FROM aba2001 WHERE aba2001rd = 75408676";

            Integer seq = getAcessoAoBanco().obterInteger(sqlSeq);

            String sql =  "INSERT INTO aba2001 (aba2001id, aba2001rd, aba2001lcto, aba2001json) VALUES (nextval('default_sequence'), "+ 78751404+"," +seq+ ",'"+tmRepositorio.toString()+"')"

            session.connection.prepareStatement(sql).execute()
        }catch (SQLException e){
            interromper(e.toString())
        }
    }
    private void deletaLinhaRepositorio(Long id){
        String sql = "DELETE FROM aba2001 WHERE aba2001id = :id ";

        getAcessoAoBanco().deletarRegistrosBySQL(sql, Parametro.criar("id", id) )

        session.connection.prepareStatement(sql)
    }
    private String montarCodigoBarras(TextFileLeitura txt){
        String inscricaoEntidade = txt.getSubString(63,77);
        String dtVencimento = txt.getSubString(107,115);
        String valorTitulo = txt.getSubString(115,130);

        String codBarras = inscricaoEntidade + dtVencimento + valorTitulo;

        return codBarras;
    }

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=