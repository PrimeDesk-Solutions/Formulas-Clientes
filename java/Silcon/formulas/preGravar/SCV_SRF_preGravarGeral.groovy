package Silcon.formulas.preGravar

import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe02
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

import java.time.LocalDate

public class SCV_SRF_preGravarGeral extends FormulaBase{

    private Eaa01 eaa01;
    private Abb01 abb01;
    private Abe01 abe01;
    private Abe0101 abe0101;
    private Abe02 abe02;
    private Integer gravar = 1; //0-Não 1-Sim

    TableMap jsonAbe02;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
    }

    @Override
    public void executar() {
        eaa01 = get("eaa01");
        verificaItensRepetidos(eaa01);
        validarQuantidadeItem(eaa01);
        definirDataPrimeiraUltimaCompra(eaa01);
        definirMaiorFaturaCliente();

        put("gravar", gravar);
    }
    private verificaItensRepetidos(Eaa01 eaa01){
        /*
            Essa função verifica se foi inserido algum item repetido na spread dos pedidos de vendas;

            Parametro: Tabela eaa01;
         */

        Map<String,Integer> contagemItens = new HashMap<>();

        for(Eaa0103 eaa0103 : eaa01.eaa0103s){
            // Itens
            Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

            def codigoItem = abm01.abm01tipo.toString() + abm01.abm01codigo;

            if(contagemItens.containsKey(codigoItem)){
                contagemItens.put(codigoItem, contagemItens.get(codigoItem) + 1)
            }else{
                contagemItens.put(codigoItem, 1);
            }
        }
        for(String codigo : contagemItens.keySet()){
            if(contagemItens.get(codigo) >= 2) throw new ValidacaoException("Não é permitido a inclusão de itens repetidos no documento. Item: " + codigo.substring(1) )
        }
    }
    private validarQuantidadeItem(Eaa01 eaa01){
        if(eaa01.eaa0103s.size() == 0) throw new ValidacaoException("Não é permitido salvar documento sem itens informado. Insira pelo menos um item para continuar ");

        for(Eaa0103 eaa0103 : eaa01.eaa0103s){

            // Itens
            Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

            if(eaa0103.eaa0103unit == 0) throw new ValidacaoException("Unitário inválido para o item " + abm01.abm01codigo)

            if(eaa0103.eaa0103qtComl == 0 ) throw new ValidacaoException("Quantidade inválida para o item "+abm01.abm01codigo+" necessário um valor maior que zero")
        }
    }
    private definirDataPrimeiraUltimaCompra(Eaa01 eaa01){
        /*
            Função para definir a data da primeira venda e ultima venda de cada cliente;
            A data da primeira venda é definida caso não tenha nenhum valor no campo primeira venda no cadastro da entidade (Aba clientes);
            já a última venda é alterado o campo "Última Venda" no cadastro da entidade (Aba Clientes) sempre que é gravado um documento para determinada entidade;

            Parametro: Tabela eaa01;
         */

        // Data Atual
        LocalDate dataAtual = LocalDate.now()

        String txtData = dataAtual.toString();

        // Central de Documentos
        abb01 = eaa01.eaa01central;

        // Entidade
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

        // Entidade - Cliente
        abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

        // Campos Livres
        TableMap jsonAbe02 = abe02.abe02json != null ? abe02.abe02json : new TableMap();

        if(jsonAbe02.size() == 0){
            getSession().connection.prepareStatement("UPDATE abe02 SET abe02json = '{}' WHERE abe02ent = " + abe01.abe01id).execute();
        }

        // Define a data da primeira venda do cliente
        if(jsonAbe02.get("primeira_venda") == null){

            String data = '"'+txtData.replace("-","") + '"'

            String sql = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{primeira_venda}', '"+data+"', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql).execute()
        }

        // Define a data da ultima venda do cliente
        if(abb01.abb01data == dataAtual){

            String data = '"'+txtData.replace("-","") + '"'

            String sql = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{ultima_venda}', '"+data+"', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql).execute()
        }
    }
    private definirMaiorFaturaCliente(){
        // Central de Documentos
        abb01 = eaa01.eaa01central;

        // Entidade
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

        // Entidade - Cliente
        abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

        // Campos Livres
        TableMap jsonAbe02 = abe02.abe02json != null ? abe02.abe02json : new TableMap();

        if(eaa01.eaa01totDoc > jsonAbe02.getBigDecimal_Zero("maior_venda_numero")){
            def numDoc = abb01.abb01num;
            def dataNota = abb01.abb01data;
            def txtData = dataNota.toString();
            def valorDoc = eaa01.eaa01totDoc;

            // Retira os acentos da data
            txtData = '"'+txtData.replace("-","") + '"'

            // Número maior faturamento
            String sql1 = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{maior_venda_numero}', '"+numDoc.toString()+"', true) WHERE abe02ent = " + abe01.abe01id;

            // Data maior faturamento
            String sql2 = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{maior_venda_data}', '"+txtData+"', true) WHERE abe02ent = " + abe01.abe01id;

            String sql3 = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{maior_venda_valor}', '"+valorDoc.toString()+"', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql1).execute()
            getSession().connection.prepareStatement(sql2).execute()
            getSession().connection.prepareStatement(sql3).execute()
        }

    }

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==