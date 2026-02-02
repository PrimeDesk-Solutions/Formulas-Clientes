/*
    Ficar atento ás observações, na Silcon não da pra exportar as observações devido a formatação do campo
 */
package Silcon.formulas.cas


import sam.model.entities.ab.Aba20;
import sam.model.entities.ab.Aba2001;
import sam.model.entities.ab.Abe02;
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abe01
import org.apache.commons.io.FileUtils
import org.springframework.web.multipart.MultipartFile
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.TextFileLeitura
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

public class CAS_Atualizar_Dados_Entidades extends FormulaBase{
    private MultipartFile arquivo;

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

        List<String> registros = FileUtils.readLines(file, "UTF-8");

        TextFileLeitura txt = new TextFileLeitura(registros, "|");
        int linha = 0;
        List<String> mensagens = new ArrayList<>();

        try {
            while (txt.nextLine()) {
                linha++

                String codEntidade = txt.getCampo(2);

                // Entidade
                Abe01 abe01 = buscarEntidadePorCodigo(codEntidade);

                if(abe01 == null){
                    mensagens.add("Registro não atualizado: Entidade com o código " + codEntidade + " não encontrada no sistema.");

                    continue;
                }

                // Cliente
                Abe02 abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

                if(abe02 == null) mensagens.add("Registro não atualizado: Aba Clientes da entidade " + codEntidade + " não encontrada no sistema.")

                //abe01.setAbe01obs(txt.getCampo(3).contains("SEM VALOR") ? null : txt.getCampo(3));
                TableMap jsonAbe01 = montarJsonEntidade(txt);
                abe01.setAbe01json(jsonAbe01);
                abe02.setAbe02obsUsoInt(txt.getCampo(7).contains("SEM VALOR") ? null : txt.getCampo(7));

                getSession().persist(abe01);
                getSession().persist(abe02);
            }

            if(mensagens.size() > 0){
                gravarInconsistenciasRepositorio(mensagens);
            }
        } catch (Exception e){
            interromper("Falha ao gravar registro. Linha: " + linha + " " + e.getMessage())
        }
    }
    private Abe01 buscarEntidadePorCodigo(String codEntidade){

        return getSession().get(Abe01.class, Criterions.eq("abe01codigo", codEntidade));
    }
    private TableMap montarJsonEntidade(TextFileLeitura txt){
        TableMap jsonAbe01 = new TableMap();

        jsonAbe01.put("vlr_lim_credito", new BigDecimal(txt.getCampo(4)));
        if(!txt.getCampo(5).contains("SEM VALOR")) jsonAbe01.put("dt_vcto_lim_credito",txt.getCampo(5).replace("-", ""))
        if(!txt.getCampo(6).contains("SEM VALOR")) jsonAbe01.put("obs_lim_credito", txt.getCampo(6));


        return jsonAbe01 != null && jsonAbe01.size() > 0 ? jsonAbe01 : new TableMap();
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