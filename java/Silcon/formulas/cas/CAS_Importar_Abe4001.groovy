
package Silcon.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multitec.utils.collections.TableMap;
import org.apache.poi.ss.usermodel.Row
import sam.model.entities.ab.Aba20
import sam.model.entities.ab.Aba2001
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abe4001
import sam.model.entities.ab.Abm01
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

public class CAS_Importar_Abe4001 extends FormulaBase{
    private XSSFSheet sheet
    private MultipartFile arquivo;
    private static final int BATCH_SIZE = 200;

    private Map<String, Abe40> cacheAbe40 = new HashMap<>();
    private Map<String, Abm01> cacheAbm01 = new HashMap<>();
    private Map<Long, Abe30> cacheAbe30 = new HashMap<>();

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        arquivo = get("arquivo");
        leituraePersistBanco()
    }
    private void leituraePersistBanco() {
        File file = File.createTempFile(UUID.randomUUID().toString(), "txt");
        arquivo.transferTo(file);

        TextFileLeitura txt = new TextFileLeitura(file, "|");

        int linha = 0;
        int count = 0;
        List<String> mensagens = new ArrayList<>();

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

                Abe40 abe40 = obterAbe40(codTabela);
                if (abe40 == null) {
                    mensagens.add("Tabela de preço não encontrada. Linha: " + linha);
                    continue;
                }

                Abm01 abm01 = obterAbm01(tipoItem, codItem);
                if (abm01 == null) {
                    mensagens.add("Item não encontrado: " + codItem + ". Linha: " + linha);
                    continue;
                }

                Abe30 abe30 = obterAbe30(condPgto);
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
                count++;

                if (count % BATCH_SIZE == 0) {
                    getSession().flush();
                    session.clear();
                }

            } catch (Exception e) {
                mensagens.add("Erro na linha " + linha + ": " + e.getMessage());
            }
        }

        session.flush();
        session.clear();

        if (!mensagens.isEmpty()) {
            gravarInconsistenciasRepositorio(mensagens);
        }
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