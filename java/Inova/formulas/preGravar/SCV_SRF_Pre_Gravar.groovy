package Inova.formulas.preGravar

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import jdk.jfr.Experimental
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe02
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0107;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

import java.time.LocalDate
import java.time.LocalTime

public class SCV_SRF_Pre_Gravar extends FormulaBase {

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
        validarItensDoc(eaa01);
        definirDataPrimeiraUltimaCompra(eaa01);
        definirMaiorFaturaCliente();

        put("gravar", gravar);
    }

    private void validarItensDoc(Eaa01 eaa01) {
        try {

            Abd01 abd01 = getSession().get(Abd01.class, Criterions.eq("abd01id", eaa01.eaa01pcd.abd01id));

            Map<Long, Integer> contagemItens = new HashMap<>();

            if (eaa01.eaa0103s.size() == 0) throw new ValidacaoException("Não é permitido salvar documento sem itens informado. Insira pelo menos um item para continuar. ");
            String msg = "";

            for (Eaa0103 eaa0103 : eaa01.eaa0103s) {
                Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
                Abm0101 abm0101 = getSession().get(Abm0101.class, Criterions.eq("abm0101item", abm01.abm01id));
                TableMap jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
                TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
                TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
                BigDecimal estqMax = jsonAbm0101.getBigDecimal_Zero("estoque_max");
                BigDecimal qtdPedido = eaa0103.eaa0103qtComl;


                // Acumula itens do pedido para verificar itens repetidos
                if (contagemItens.containsKey(abm01.abm01id)) {
                    contagemItens.put(abm01.abm01id, contagemItens.get(abm01.abm01id) + 1)
                } else {
                    contagemItens.put(abm01.abm01id, 1);
                }

                if (eaa0103.eaa0103unit == 0) throw new ValidacaoException("O unitário do item " + abm01.abm01codigo + " - " + abm01.abm01descr + " deve ser maior que zero.")
                if (eaa0103.eaa0103qtComl == 0) throw new ValidacaoException("A quantidade do item " + abm01.abm01codigo + " - " + abm01.abm01descr + " deve ser maior que zero.")
                if (jsonEaa01.getBigDecimal_Zero("volumes") == BigDecimal.ZERO && abd01.abd01aplic == 1 && abd01.abd01es == 1 ) throw new ValidacaoException("Documento sem volume informado.");
                if (jsonEaa01.getBigDecimal_Zero("peso_bruto") == BigDecimal.ZERO && abd01.abd01aplic == 1 && abd01.abd01es == 1) throw new ValidacaoException("Documento sem peso bruto informado.");

                if(abd01.abd01aplic == 0){
                    if (jsonAbm0101.getBigDecimal_Zero("preco_max_real") > 0 && eaa01.eaa01moeda == null) {
                        if (eaa0103.eaa0103unit > jsonAbm0101.getBigDecimal_Zero("preco_max_real")) {
                            msg = "O unitário do item " + abm01.abm01codigo + " " + abm01.abm01descr + " excedeu o limite de preço unitário real permitido que é: " + jsonAbm0101.getBigDecimal_Zero("preco_max_real");
                            bloquearDocumento(eaa01, msg, "PréGravar Unit Real");
                        }

                    }

                    if (eaa01.eaa01moeda != null && eaa01.eaa01moeda.aag10codigo == "01") {
                        if (jsonAbm0101.getBigDecimal_Zero("preco_max_dolar") > 0 && eaa0103.eaa0103unit > jsonAbm0101.getBigDecimal_Zero("preco_max_dolar")) {
                            msg = "O unitario do item " + abm01.abm01codigo + " " + abm01.abm01descr + " excedeu o limite de preço unitario dolar permitido que é: " + jsonAbm0101.getBigDecimal_Zero("preco_max_dolar");
                            bloquearDocumento(eaa01, msg, "PréGravar Unit Dólar");
                        }
                    }

                    if (eaa0103.eaa0103qtUso < jsonAbm0101.getBigDecimal_Zero("lote_min")) {
                        msg = "O item " + abm01.abm01codigo + " " + abm01.abm01descr + " não atingiu o lote mínimo de compra."
                        gravarInconsitencia(eaa01, msg, "PréGravar Lote Min");
                    }

                    BigDecimal saldoAtual = getSession().createQuery("SELECT SUM(bcc02qt) FROM bcc02 WHERE bcc02item = :idItem").setParameter("idItem", abm01.abm01id).getUniqueResult(ColumnType.BIG_DECIMAL);
                    saldoAtual = saldoAtual == null ? BigDecimal.ZERO : saldoAtual;
                    BigDecimal saldoLiquido = (saldoAtual + qtdPedido) - estqMax;

                    if (estqMax != 0 && saldoAtual > estqMax) {
                        msg = "Item - " + eaa0103.eaa0103seq + " " + abm01.abm01codigo + "\n";
                        msg += "A quantidade solicitada + Saldo do estoque não pode ser maior que o estoque máximo\n";
                        msg += "Estoque Máximo: " + estqMax + "\n";
                        msg += "Qtd Solicitada: " + qtdPedido + "\n";
                        msg += "Saldo Estoque: " + saldoAtual + "\n";
                        msg += "Total Excedido: " + saldoLiquido + "\n";

                        bloquearDocumento(eaa01, msg, "Pré-Gravar Est. Max");

                    }
                }
            }

            // Verifica se há itens repetidos no documento
            for (Long idItem : contagemItens.keySet()) {
                if (contagemItens.get(idItem) >= 2) throw new ValidacaoException("Não é permitido a inclusão de itens repetidos no documento.")
            }
        } catch (Exception e) {
            throw new ValidacaoException(e.getMessage())
        }
    }

    private definirDataPrimeiraUltimaCompra(Eaa01 eaa01) {

        // Data Atual
        LocalDate dataAtual = LocalDate.now()

        // Central de Documentos
        abb01 = eaa01.eaa01central;

        // Entidade
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

        // Entidade - Cliente
        abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

        // Campos Livres
        TableMap jsonAbe02 = abe02 != null && abe02.abe02json != null ? abe02.abe02json : new TableMap();

        if (jsonAbe02.size() == 0) {
            getSession().connection.prepareStatement("UPDATE abe02 SET abe02json = '{}' WHERE abe02ent = " + abe01.abe01id).execute();
        }

        // Define a data da primeira venda do cliente
        if (jsonAbe02.get("primeira_venda") == null) {

            String data = '"' + dataAtual.toString().replace("-", "") + '"'

            String sql = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{primeira_venda}', '" + data + "', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql).execute()
        }

        // Define a data da última venda do cliente
        if (jsonAbe02.getDate("ultima_venda") < dataAtual && eaa01.isNew()) {

            String data = '"' + dataAtual.toString().replace("-", "") + '"'

            String sql = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{ultima_venda}', '" + data + "', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql).execute()
        }
    }

    private definirMaiorFaturaCliente() {
        // Central de Documentos
        abb01 = eaa01.eaa01central;

        // Entidade
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

        // Entidade - Cliente
        abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

        // Campos Livres
        TableMap jsonAbe02 = abe02 != null && abe02.abe02json != null ? abe02.abe02json : new TableMap();

        if (eaa01.eaa01totDoc > jsonAbe02.getBigDecimal_Zero("maior_venda_valor")) {
            def numDoc = abb01.abb01num;
            def dataNota = abb01.abb01data;
            def txtData = dataNota.toString();
            def valorDoc = eaa01.eaa01totDoc;

            // Retira os acentos da data
            txtData = '"' + txtData.replace("-", "") + '"'

            // Número maior faturamento
            String sql1 = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{maior_venda_numero}', '" + numDoc.toString() + "', true) WHERE abe02ent = " + abe01.abe01id;

            // Data maior faturamento
            String sql2 = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{maior_venda_data}', '" + txtData + "', true) WHERE abe02ent = " + abe01.abe01id;

            String sql3 = "UPDATE abe02 SET abe02json = jsonb_set(abe02json, '{maior_venda_valor}', '" + valorDoc.toString() + "', true) WHERE abe02ent = " + abe01.abe01id;

            getSession().connection.prepareStatement(sql1).execute()
            getSession().connection.prepareStatement(sql2).execute()
            getSession().connection.prepareStatement(sql3).execute()
        }

    }

    private void bloquearDocumento(Eaa01 eaa01, String msg, String identificador) {
        if (eaa01.eaa0107s.size() > 0) {
            Boolean jaContemInconsistencia = false;

            for (Eaa0107 eaa0107 in eaa01.eaa0107s) {
                if (eaa0107.eaa0107ident.contains(identificador) && eaa0107.eaa0107justificativa == null) {
                    jaContemInconsistencia = true;
                }
            }

            if (jaContemInconsistencia) return;
        }

        Eaa0107 eaa0107 = new Eaa0107();
        eaa0107.eaa0107msg = msg;
        eaa0107.eaa0107user = obterUsuarioLogado();
        eaa0107.eaa0107data = LocalDate.now();
        eaa0107.eaa0107hora = LocalTime.now();
        eaa0107.eaa0107ident = identificador;
        eaa01.addToEaa0107s(eaa0107);
        eaa01.eaa01bloqueado = 1;
    }

    private void gravarInconsitencia(Eaa01 eaa01, String inconsistencia, String identificador) {
        if (eaa01.eaa0107s.size() > 0) {
            Boolean jaContemInconsistencia = false;

            for (Eaa0107 eaa0107 in eaa01.eaa0107s) {
                if (eaa0107.eaa0107ident.contains(identificador) && eaa0107.eaa0107justificativa == null) {
                    jaContemInconsistencia = true;
                }
            }

            if (jaContemInconsistencia) return;
        }

        Eaa0107 eaa0107 = new Eaa0107();
        eaa0107.eaa0107msg = inconsistencia;
        eaa0107.eaa0107user = obterUsuarioLogado();
        eaa0107.eaa0107data = LocalDate.now();
        eaa0107.eaa0107hora = LocalTime.now();
        eaa0107.eaa0107ident = identificador;
        eaa01.addToEaa0107s(eaa0107);
    }

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==