package Atilatte.formulas.preGravar

import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aag0201
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe02
import sam.model.entities.ea.Eaa01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.aa.Aam06;
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0107
import sam.server.samdev.formula.FormulaBase
import br.com.multiorm.Query
import br.com.multiorm.ColumnType
import sam.server.samdev.utils.Parametro;

import java.time.LocalDate
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ea.Eaa0103
import br.com.multitec.utils.ValidacaoException

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SCV_SRF_PreGravar extends FormulaBase {

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
        gravarInconsistencias(eaa01)
        verificaItensRepetidos(eaa01)
        validarQuantidadeItem(eaa01);
        definirDataPrimeiraUltimaCompra(eaa01);
        alterarPoliticaSeguranca(eaa01);
        alterarOrdecaoItensDocumento(eaa01);

        put("gravar", gravar);
    }

    private gravarInconsistencias(Eaa01 eaa01) {
        Eaa0102 eaa0102;
        Eaa0101 eaa0101princ;
        Aag0201 aag0201;
        Parametro parametroPcd = Parametro.criar("idPcd", eaa01.eaa01pcd.abd01id);
        Integer validaPesoMinimo = getAcessoAoBanco().obterInteger("SELECT CAST(abd01camposCustom -> 'validar_peso_transp' AS INTEGER) AS validaPeso FROM abd01 WHERE abd01id = :idPcd ", parametroPcd)

        if(validaPesoMinimo == 1){
            // Dados Gerais
            for (Eaa0102 dadosGerais : eaa01.eaa0102s) {
                eaa0102 = dadosGerais;
            }

            // Endereço Principal Entidade
            for (Eaa0101 eaa0101 : eaa01.eaa0101s) {
                if (eaa0101.eaa0101principal == 1) eaa0101princ = eaa0101;
            }

            // Municipio da Entidade
            aag0201 = eaa0101princ.eaa0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", eaa0101princ.eaa0101municipio.aag0201id)) : null;

            // Campos Livres Documento
            TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();

            if (eaa0102.eaa0102redespacho == null) return;

            if (eaa01.eaa01clasDoc == 0 && eaa0102.eaa0102frete != 1) {
                // Redespacho
                Abe01 abe01redesp = getSession().get(Abe01.class, eaa0102.eaa0102redespacho.abe01id);

                String nomeRedespacho = abe01redesp.abe01na;
                String codRedespacho = abe01redesp.abe01codigo;
                String municipioEntidade = aag0201.aag0201nome;
                BigDecimal pesoBruto = jsonEaa01.getBigDecimal_Zero("peso_bruto");

                String descrRepositorio = "Frete - " + nomeRedespacho.split()[0] + " Transp";
                descrRepositorio = descrRepositorio.toUpperCase().replace(" ", "");

                // Formata o municipio da entidade para retirar os acentos
                municipioEntidade = formatarString(municipioEntidade.toUpperCase());

                // Busca campos livres do repositório da Transportadora
                String sql = " SELECT aba20id, aba2001json FROM aba2001 " +
                        " INNER JOIN aba20 ON aba2001rd = aba20id " +
                        " WHERE REPLACE(UPPER(aba20descr), ' ','') LIKE '%" + descrRepositorio + "%'"

                List<TableMap> listTmRepositorio = getAcessoAoBanco().buscarListaDeTableMap(sql);

                if (listTmRepositorio.size() == 0) throw new ValidacaoException("Não foi encontrado repositório de dados para a transportadora " + codRedespacho + " - " + nomeRedespacho)

                TableMap jsonRepositorio = new TableMap();

                for (repositorio in listTmRepositorio) {
                    if (repositorio.getTableMap("aba2001json") != null) {
                        String municipioRepositorio = repositorio.getTableMap("aba2001json").getString("municipio");
                        if (municipioRepositorio != null) {
                            municipioRepositorio = formatarString(municipioRepositorio.toUpperCase());
                            if (municipioRepositorio == municipioEntidade) {
                                jsonRepositorio = repositorio.getTableMap("aba2001json");
                            }
                        }
                    }
                }

                if (jsonRepositorio.size() == 0) throw new ValidacaoException("Município " + municipioEntidade + " não encontrado no repositório de dados " + descrRepositorio)

                BigDecimal pesoMinimo = jsonRepositorio.getBigDecimal_Zero("peso_min");

                if (pesoMinimo == 0) throw new ValidacaoException("Não foi informado o valor do peso mímino no município " + municipioEntidade + " do repositório de dados da transportadora " + codRedespacho + " - " + nomeRedespacho);

                // Bloqueia o pedido caso não atinja o peso mínimo do município da transportadora
                if (pesoBruto < pesoMinimo) {
                    if (eaa01.eaa0107s == null || eaa01.eaa0107s.size() == 0) {
                        Eaa0107 eaa0107 = new Eaa0107();
                        eaa0107.eaa0107msg = "Pedido bloqueado por não atingir peso mínimo da transportadora."
                        eaa0107.eaa0107user = obterUsuarioLogado();
                        eaa01.addToEaa0107s(eaa0107);

                        eaa01.eaa01bloqueado = 1
                    }
                }
            }
        }
    }

    private String formatarString(String municipio) {

        if (municipio.contains("Ã")) {
            municipio = municipio.replace("Ã", "A");
        }
        if (municipio.contains("Á")) {
            municipio = municipio.replace("Á", "A");
        }
        if (municipio.contains("Â")) {
            municipio = municipio.replace("Â", "A");
        }
        if (municipio.contains("É")) {
            municipio = municipio.replace("É", "E");
        }
        if (municipio.contains("Ê")) {
            municipio = municipio.replace("Ê", "E");
        }
        if (municipio.contains("Í")) {
            municipio = municipio.replace("Í", "I");
        }
        if (municipio.contains("Õ")) {
            municipio = municipio.replace("Õ", "O");
        }
        if (municipio.contains("Ô")) {
            municipio = municipio.replace("Ô", "O");
        }
        if (municipio.contains("Ó")) {
            municipio = municipio.replace("Ó", "O");
        }
        if (municipio.contains("Ú")) {
            municipio = municipio.replace("Ú", "U");
        }
        if (municipio.contains("Ü")) {
            municipio = municipio.replace("Ü", "U");
        }
        if (municipio.contains("Ç")) {
            municipio = municipio.replace("Ç", "C");
        }


        return municipio;

    }

    private verificaItensRepetidos(Eaa01 eaa01) {
        /*
            Essa função verifica se foi inserido algum item repetido na spread dos pedidos de vendas;

            Parametro: Tabela eaa01;
         */

        if (eaa01.eaa0103s.size() == 0) throw new ValidacaoException("Não é permitido salvar documento sem itens informado. Insira pelo menos um item para continuar ");

        Map<String, Integer> contagemItens = new HashMap<>();

        for (Eaa0103 eaa0103 : eaa01.eaa0103s) {
            // Itens
            Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

            def codigoItem = abm01.abm01tipo.toString() + abm01.abm01codigo;

            if (contagemItens.containsKey(codigoItem)) {
                contagemItens.put(codigoItem, contagemItens.get(codigoItem) + 1)
            } else {
                contagemItens.put(codigoItem, 1);
            }
        }

        for (String codigo : contagemItens.keySet()) {
            if (contagemItens.get(codigo) >= 2) throw new ValidacaoException("Não é permitido a inclusão de itens repetidos no documento. Item: " + codigo.substring(1))
        }
    }

    private validarQuantidadeItem(Eaa01 eaa01) {
        /*
            Essa função tem a finalidade de validar as quantidades de cada item dos pedidos de vendas,
            permitido inserir apenas uma quantidade múltipla da capacidade da caixa do item;

            ******** Quantidades: ***********

            Iogurte 180 e Iogurte 500: Múltiplo de 20;

            Iogurtes Copos 160 - 170: Múltiplo de 12;

            Creme de Leite e Kefir - Multiplo de 16;

            Leite Garrafa: Multiplo de 6;

            Queijos em unidade: Multiplo de 12;

            Parametro: Tabela eaa01;

         */

        for (Eaa0103 eaa0103 : eaa01.eaa0103s) {

            // Itens
            Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

            // Unidade de Medida do Item
            Aam06 aam06 = abm01.abm01umu != null ? getSession().get(Aam06.class, abm01.abm01umu.aam06id) : null;

            // Itens Valores
            Abm0101 abm0101 = abm01 != null ? getSession().get(Abm0101.class, Criterions.eq("abm0101item", abm01.abm01id)) : null;

            //Campo Livre itens
            TableMap jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();

            // Central de Documentos
            Abb01 abb01 = eaa01.eaa01central;

            // Entidade
            Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

            // Campos Livres Entidade
            TableMap jsonAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();

            if (eaa0103.eaa0103qtComl == 0) throw new ValidacaoException("Quantidade inválida para o item " + abm01.abm01codigo + " necessário um valor maior que zero")

            if (eaa0103.eaa0103dtEntrega == null) throw new ValidacaoException("Necessário informar a data de entrega para o item " + abm01.abm01codigo);


            //Define o grupo do item
            def codItem = abm01.abm01codigo;

            Query descrCriterios = getSession().createQuery("select aba3001descr from aba3001 " +
                    "inner join abm0102 on abm0102criterio = aba3001id and aba3001criterio = 542858 " +
                    "inner join abm01 on abm0102item = abm01id " +
                    "where abm01codigo = '" + codItem + "'" +
                    "and abm01tipo = 1 ");

            List<TableMap> listCriterios = descrCriterios.getListTableMap();

            String grupo = "";

            for (TableMap criterio : listCriterios) {
                if (criterio.getString("aba3001descr").contains("Queijo")) {
                    grupo = "Queijo";
                }
                if (criterio.getString("aba3001descr").contains("Leite")) {
                    grupo = "Leite";
                }

                if (criterio.getString("aba3001descr").contains("Iogurte")) {
                    grupo = "Iogurte";
                }

            }


            def quantidade;

            if (grupo == "Iogurte") {
                if (jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Capacidade Volumetrica no cadastro do item " + abm01.abm01codigo + " é inválida! Necessário um valor maior que zero!")
                quantidade = eaa0103.eaa0103qtComl.intValue() % jsonAbm0101.getBigDecimal_Zero("cvdnf").intValue();
                if (quantidade != 0) throw new ValidacaoException("Quantidade inválida para o item " + abm01.abm01codigo + " necessário quantidade múltipla de " + jsonAbm0101.getBigDecimal_Zero("cvdnf").intValue().toString());
            }

            if (grupo == "Leite") {
                if (abm01.abm01codigo == "0101002") {
                    if (jsonAbm0101.getBigDecimal_Zero("volume_caixa") == 0) throw new ValidacaoException("Volume Caixa no cadastro do item " + abm01.abm01codigo + " é inválida! Necessário um valor maior que zero!")
                    quantidade = eaa0103.eaa0103qtComl.intValue() % jsonAbm0101.getBigDecimal_Zero("volume_caixa").intValue();
                    if (quantidade != 0) throw new ValidacaoException("Quantidade inválida para o item " + abm01.abm01codigo + " necessário quantidade múltipla de " + jsonAbm0101.getBigDecimal_Zero("volume_caixa").intValue());
                } else {
                    if (jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Capacidade Volumetrica no cadastro do item " + abm01.abm01codigo + " é inválida! Necessário um valor maior que zero!")
                    quantidade = eaa0103.eaa0103qtComl.intValue() % jsonAbm0101.getBigDecimal_Zero("cvdnf").intValue();
                    if (quantidade != 0) throw new ValidacaoException("Quantidade inválida para o item " + abm01.abm01codigo + " necessário quantidade múltipla de " + jsonAbm0101.getBigDecimal_Zero("cvdnf").intValue());
                }
            }

            if (grupo == "Queijo") {
                if (aam06.aam06codigo == 'UN') {
                    if (jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Capacidade Volumetrica no cadastro do item " + abm01.abm01codigo + " é inválida! Necessário um valor maior que zero!")
                    quantidade = eaa0103.eaa0103qtComl.intValue() % jsonAbm0101.getBigDecimal_Zero("cvdnf").intValue();
                    if (quantidade != 0) throw new ValidacaoException("Quantidade inválida para o item " + abm01.abm01codigo + " necessário quantidade múltipla de " + jsonAbm0101.getBigDecimal_Zero("cvdnf").intValue());
                }
            }


        }
    }

    private definirDataPrimeiraUltimaCompra(Eaa01 eaa01) {
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
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id)

        // Configuração Cliente
        abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

        // Campos Livres
        TableMap jsonAbe02 = abe02.abe02json != null ? abe02.abe02json : new TableMap();

        if (jsonAbe02.size() == 0) {
            getSession().connection.prepareStatement("UPDATE abe02 SET abe02json = '{}' WHERE abe02ent = " + abe01.abe01id).execute();
        }

        // Define a data da primeira venda do cliente
        if (jsonAbe02.get("primeira_venda") == null) {

            String data = '"' + txtData.replace("-", "") + '"'

            String sql = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{primeira_venda}', '" + data + "', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql).execute()
        }

        // Define a data da ultima venda do cliente
        if (abb01.abb01data == dataAtual) {

            String data = '"' + txtData.replace("-", "") + '"'

            String sql = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{ultima_venda}', '" + data + "', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql).execute()
        }

    }

    private void alterarPoliticaSeguranca(Eaa01 eaa01) {
        // Central de Documentos
        Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);

        // Entidade
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

        if (eaa01.eaa01clasDoc == 1) {
            if (abb01.abb01operAutor == "SRF1003") {

                def politicaEnt = abe01.abe01psUso;

                // Define a Politica da entidade sendo a politica do documento
                eaa01.setEaa01psUso(politicaEnt);
            }
        }
    }

    private void alterarOrdecaoItensDocumento(Eaa01 eaa01) {

        for (Eaa0103 eaa0103 in eaa01.eaa0103s) {
            // Itens
            Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

            if (eaa0103.eaa0103json.getInteger("ordem_separacao") == null) throw new ValidacaoException("Não foi informado ordem de separação no item " + abm01.abm01codigo)
        }

        Collection<Eaa0103> eaa0103s = eaa01.getEaa0103s();
        List<Eaa0103> listaOrdenada = new ArrayList<>();
        if (eaa0103s != null && !eaa0103s.isEmpty()) {
            listaOrdenada = new ArrayList<>(eaa0103s)
            Collections.sort(listaOrdenada, new Comparator<Eaa0103>() {
                @Override
                public int compare(Eaa0103 o1, Eaa0103 o2) {
                    return Integer.compare(o1.getEaa0103json().getInteger("ordem_separacao"), o2.getEaa0103json().getInteger("ordem_separacao"));
                }
            });
        }

        for (int i = 0; i < listaOrdenada.size(); i++) {
            listaOrdenada.get(i).setEaa0103seq(i + 1);
        }

        eaa01.setEaa0103s(listaOrdenada);
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==