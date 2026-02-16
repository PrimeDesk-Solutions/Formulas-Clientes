package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abe4001
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Aba20
import sam.model.entities.ab.Aba2001
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import java.time.LocalDate
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multitec.utils.collections.TableMap;

public class CAS_Importar_Abe4001 extends FormulaBase{
    private MultipartFile arquivo;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        arquivo = get("arquivo");
        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");

        int linha = 0;
        int count = 0;
        List<String> mensagens = new ArrayList<>();

        List<Abe40> abe40s = buscarTabelasDePrecos();
        List<Abm01> abm01s = buscarItens();
        List<Abe30> abe30s = buscarCondicoesDePagamento();

        try{
            while (txt.nextLine()) {
                linha++;

                try {
                    String codTabela = txt.getCampo(1);
                    Integer tipoItem = Integer.parseInt(txt.getCampo(2)) == 2 ? 3 : Integer.parseInt(txt.getCampo(2));
                    String codItem = txt.getCampo(3);
                    Long condPgto = Long.parseLong(txt.getCampo(4));

                    BigDecimal txDesconto = new BigDecimal(txt.getCampo(5));
                    BigDecimal qtdMax = new BigDecimal(txt.getCampo(6));
                    BigDecimal preco = new BigDecimal(txt.getCampo(7));

                    Abe40 abe40 = abe40s.stream().filter(f -> f.abe40codigo.equalsIgnoreCase(codTabela)).findFirst().orElse(null)
                    if (abe40 == null) {
                        mensagens.add("Tabela de preço não encontrada. Linha: " + linha);
                        continue;
                    }

                    Abm01 abm01 = abm01s.stream().filter(f -> f.abm01codigo.equalsIgnoreCase(codItem)).filter(f -> f.abm01tipo == tipoItem).findFirst().orElse(null);
                    if (abm01 == null) {
                        mensagens.add("Item não encontrado: " + codItem + ". Linha: " + linha);
                        continue;
                    }

                    Abe30 abe30 = abe30s.stream().filter(f -> f.abe30id == condPgto).findFirst().orElse(null);
                    if (abe30 == null) {
                        mensagens.add("Condição de pagamento não encontrada. Linha: " + linha);
                        continue;
                    }

                    Abe4001 abe4001 = new Abe4001();
                    abe4001.setAbe4001tab(abe40);
                    abe4001.setAbe4001item(abm01);
                    abe4001.setAbe4001cp(abe30);
                    abe4001.setAbe4001txDesc(txDesconto);
                    abe4001.setAbe4001qtMax(qtdMax);
                    abe4001.setAbe4001preco(preco);

                    session.persist(abe4001);

                } catch (Exception e) {
                    //mensagens.add("Erro ao gravar registro " + linha + ": " + e.getMessage());
                    interromper(e.getMessage())
                }
            }

            if (!mensagens.isEmpty()) {
                gravarInconsistenciasRepositorio(mensagens);
            }

        }catch(Exception ex){
            interromper("Erro: " + ex.getMessage())
        }
    }
    private List<Abe40> buscarTabelasDePrecos(){
        return getSession().createCriteria(Abe40.class)
                .addFields("abe40id, abe40codigo")
                .addWhere(Criterions.eq("abe40gc", 1075797))
                .getList(ColumnType.ENTITY)
    }
    private List<Abm01> buscarItens(){
        return getSession().createCriteria(Abm01.class)
                .addFields("abm01id, abm01codigo, abm01tipo")
                .addWhere(Criterions.eq("abm01gc", 1075797))
                .getList(ColumnType.ENTITY)
    }
    private List<Abe30> buscarCondicoesDePagamento(){
        return getSession().createCriteria(Abe30.class)
                .addFields("abe30id, abe30codigo")
                .addWhere(Criterions.eq("abe30gc", 1075797))
                .getList(ColumnType.ENTITY)
    }
    private gravarInconsistenciasRepositorio(List<String> mensagens){
        Aba20 aba20 = session.createCriteria(Aba20.class).addWhere(Criterions.eq("aba20codigo", "Avisos")).get(ColumnType.ENTITY);

        for(String mensagem : mensagens){
            Aba2001 aba2001 = new Aba2001();
            aba2001.setAba2001rd(aba20)
            aba2001.setAba2001lcto(mensagens.indexOf(mensagem))
            aba2001.setAba2001prop("Aviso")
            TableMap json = new TableMap()
            json.put("msg", mensagem)
            aba2001.setAba2001json(json);

            session.persist(aba2001)
        }
    }
}