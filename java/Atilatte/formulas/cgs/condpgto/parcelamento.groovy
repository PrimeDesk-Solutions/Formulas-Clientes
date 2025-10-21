package Atilatte.formulas.cgs.condpgto;
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.DayOfWeek
import java.time.LocalDate

import sam.model.entities.ab.Abf40
import sam.model.entities.ab.Abf4001
import sam.model.entities.ea.Eaa01131;
import sam.model.entities.ea.Eaa0113

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import sam.dicdados.FormulaTipo
import sam.dto.cgs.ParcelaDto
import sam.model.entities.ab.Abe02;
import sam.model.entities.ea.Eaa0113
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe3001
import sam.model.entities.ab.Abe3002
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.utils.Parametro;


public class parcelamento extends FormulaBase {

    private LocalDate dtBase;
    private Long abe30id;
    private BigDecimal valor;
    private Long abe01id;
    private Eaa01 eaa01;
    private Eaa0113 eaa0113
    private Eaa01131 eaa01131;
    private Abf40 abf40;
    private Abf4001 abf4001

    private TableMap jsonEaa0113 = new TableMap();
    private TableMap jsonAbe02  = new TableMap();

    @Override
    public void executar() {
        dtBase = (LocalDate)get("dtBase");
        abe30id = (Long)get("abe30id");
        valor = (BigDecimal)get("valor");
        abe01id = (BigDecimal)get("abe01id");
        eaa01 = (Eaa01)get("eaa01");

        if(dtBase == null) return;

        if(abe30id == null) return;

        if(valor == null) return;

        // Entidade - Cliente
        Abe02 abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent",abe01id));

        // Campo Livre Entidade - Cliente
        jsonAbe02 = abe02 != null && abe02.abe02json != null ? abe02.abe02json : new TableMap();

//		// Parcelamento
//		eaa0113 = getSession().get(Eaa0113.class, Criterions.eq("eaa0113doc", eaa01.eaa01id ));
//
//		// Pagamentos
//		eaa01131 = getSession().get(Eaa01131.class, Criterions.eq("eaa01131fin", eaa0113.eaa0113id ));
//
//		// Forma Pagamento
//		abf40 = eaa01131.eaa01131fp;
//
//		// Forma Pagamento - Condições de Pagamento
//		abf4001 = abf40 != null ? getSession().get(Abf4001.class, Criterions.eq("abf4001fp", abf40.abf40id )) : null;


        // Preenche os campos livres da parcela
        jsonEaa0113.put("desconto",jsonAbe02.getBigDecimal_Zero("tx_fixa"));

        List<ParcelaDto> listaParcelas = new ArrayList<>();

        Abe30 abe30 = buscarCondicaoPagamentoPorId(abe30id);
        if(abe30 == null) return;

        // Campos Livres Condição Pagamento
        TableMap jsonAbe30 = abe30.abe30json != null ? abe30.abe30json : new TableMap();

        //Verificando dia da semana para a data base
        def diasAdicionaisDtBase = obterDiasAdicionaisAData(dtBase, abe30, 0);
        //Adicionando dias a data base
        dtBase = dtBase.plusDays(diasAdicionaisDtBase);

        def vlrSaldo = valor;

        List<Abe3001> abe3001s = buscarParcelasPeloIdCondicaoPagamento(abe30id);

        BigDecimal multa = jsonAbe30.getBigDecimal_Zero("multa");
        BigDecimal juros = jsonAbe30.getBigDecimal_Zero("juros");

        if(abe3001s != null && abe3001s.size() > 0) {
            int i = 1;
            for(Abe3001 abe3001 : abe3001s) {

                //Valor da parcela
                def vlrParcela = 0.0;
                if(i == abe3001s.size()) { //Última parcela
                    vlrParcela = vlrSaldo;
                }else{
                    vlrParcela = round((valor * abe3001.getAbe3001perc_Zero()) / 100, 2);
                    vlrSaldo = vlrSaldo - vlrParcela;
                }

                //Data de vencimento nominal
                def vctoN = LocalDate.of(dtBase.getYear(), dtBase.getMonth(), dtBase.getDayOfMonth());

                //Verificando vencimento fixo
                vctoN = vctoN.plusDays(abe3001.getAbe3001dias());

                //Verificando Ajustes Complementares
                def diaVctoN = vctoN.dayOfMonth;
                List<Abe3002> abe3002s = buscarDiaComplementarPeloIdCondicaoPagamento(abe30id);
                for(Abe3002 abe3002 : abe3002s) {
                    TableMap tmAbe3002 = abe3002.abe3002json;
                    int diai = tmAbe3002.getInteger("diai");
                    int diaf = tmAbe3002.getInteger("diaf");
                    int dia = vctoN.getMonthValue() == 2 ? 28 : tmAbe3002.getInteger("diavcto");
                    int mesref = tmAbe3002.getInteger("refmes") == 0 ? 0 : 1;
                    if(diaVctoN >= diai && diaVctoN <= diaf) {
                        Integer mesAtual = vctoN.monthValue;
                        vctoN = LocalDate.of(vctoN.getYear(), vctoN.getMonth() + mesref, dia);
                        if (vctoN.monthValue < mesAtual) vctoN = LocalDate.of(vctoN.getYear() + 1, vctoN.getMonth(), dia);
                    }
                }

                // Verifica se a data de vencimento é feriado, se sim busca uma data no repositório que não seja feriado
                Boolean isFeriado = verificarDataFeriado(vctoN);
                LocalDate dtAux = vctoN
                if(isFeriado){
                    Boolean feriadoAux = true
                    def count = 0
                    while (feriadoAux){
                        dtAux = dtAux.plusDays(1)
                        if(!dtAux.dayOfWeek.weekday) continue
                        feriadoAux = verificarDataFeriado(dtAux)
                        count++
                        if (count > 10) interromper("Formula Parcelamento: Processo interrompido")
                    }
                }

                vctoN = dtAux

                //Verificando dia da semana para a data de vencimento nominal
                def diasAdicionaisVctoN = obterDiasAdicionaisAData(vctoN, abe30, 1);

                //Adicionando dias a data de vencimento nominal
                vctoN = vctoN.plusDays(diasAdicionaisVctoN);

//				def txFinanceira = abf4001 != null ? abf4001.abf4001txFinanc_Zero : new BigDecimal(0);
                ParcelaDto parcelaDto = new ParcelaDto();
                parcelaDto.vctoN = vctoN;
                //parcelaDto.valor = txFinanceira > 0 ? vlrParcela - (vlrParcela * txFinanceira ) : vlrParcela ;
                parcelaDto.valor = vlrParcela;
                parcelaDto.criaDoc = abe3001.getAbe3001docFinan();
                parcelaDto.abf15id = abe3001.getAbe3001port() != null ? abe3001.getAbe3001port().getAbf15id() : null
                parcelaDto.abf16id = abe3001.getAbe3001oper() != null ? abe3001.getAbe3001oper().getAbf16id() : null
                parcelaDto.abf40id = abe3001.getAbe3001fp() != null ? abe3001.getAbe3001fp().getAbf40id() : null

                if(jsonEaa0113 != null){
                    String txtDataVcto = vctoN.toString();

                    if(jsonEaa0113.getBigDecimal_Zero("desconto") > 0 ){
                        jsonEaa0113.put("desconto",(vlrParcela * (jsonEaa0113.getBigDecimal_Zero("desconto") / 100)) * -1)
                        jsonEaa0113.put("desconto",jsonEaa0113.getBigDecimal_Zero("desconto").round(2));

                    }
                    def valorMulta = vlrParcela * (multa / 100 );
                    def valorJuros =  vlrParcela * (juros / 100 );

                    jsonEaa0113.put("multa",valorMulta);
                    jsonEaa0113.put("juros",valorJuros);
                    jsonEaa0113.put("dt_limite_desc",(jsonEaa0113.getBigDecimal_Zero("desconto") != null && jsonEaa0113.getBigDecimal_Zero("desconto").abs() > 0)  ? txtDataVcto.replace("-","") : null);
                    parcelaDto.cposLivres = jsonEaa0113 ;
                }

                listaParcelas.add(parcelaDto);

                i++;
            }
        }

        put("listaParcelas", listaParcelas);
    }

    /**
     * Verifica o dia da semana e obtém os dias a serem adicionados a data
     * @param data LocalDate
     * @param abe30 Abe30
     * @param qualData 0-Data Base 1-Vencimento Nominal
     * @return int
     */
    private int obterDiasAdicionaisAData(LocalDate data, Abe30 abe30, int qualData) {
        int diasAdicionaisData = 0;

        DayOfWeek diaSemana = data.getDayOfWeek();

        switch (diaSemana) {
            case DayOfWeek.SUNDAY:
                diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase1() : abe30.getAbe30diasVctoN1(); break;
            case DayOfWeek.MONDAY:
                diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase2() : abe30.getAbe30diasVctoN2(); break;
            case DayOfWeek.TUESDAY:
                diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase3() : abe30.getAbe30diasVctoN3(); break;
            case DayOfWeek.WEDNESDAY:
                diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase4() : abe30.getAbe30diasVctoN4(); break;
            case DayOfWeek.THURSDAY:
                diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase5() : abe30.getAbe30diasVctoN5(); break;
            case DayOfWeek.FRIDAY:
                diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase6() : abe30.getAbe30diasVctoN6(); break;
            case DayOfWeek.SATURDAY:
                diasAdicionaisData = qualData == 0 ? abe30.getAbe30diasDtBase7() : abe30.getAbe30diasVctoN7(); break;
            default: break;
        }
    }

    private Abe30 buscarCondicaoPagamentoPorId(Long abe30id) {
        return getSession().createCriteria(Abe30.class)
                .addWhere(Criterions.eq("abe30id", abe30id))
                .get(ColumnType.ENTITY);
    }

    private List<Abe3001> buscarParcelasPeloIdCondicaoPagamento(Long abe30id) {
        return getSession().createCriteria(Abe3001.class)
                .addWhere(Criterions.eq("abe3001cp", abe30id))
                .setOrder("abe3001dias")
                .getList(ColumnType.ENTITY);
    }

    private List<Abe3002> buscarDiaComplementarPeloIdCondicaoPagamento(Long abe30id) {
        return getSession().createCriteria(Abe3002.class)
                .addWhere(Criterions.eq("abe3002cp", abe30id))
                .setOrder("abe3002id")
                .getList(ColumnType.ENTITY);
    }
    private Boolean verificarDataFeriado(LocalDate vctoN){

        String dtVcto = vctoN.toString().replace("-", "")

        String sql = "SELECT aba2001json " +
                "FROM aba2001 " +
                "WHERE aba2001rd = 30123851 " +
                "AND CAST(aba2001json ->> 'data' AS text) = :dtVcto"

        TableMap tmJson = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("dtVcto", dtVcto));

        return tmJson != null
    }

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CONDICAO_PAGAMENTO;
    }

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzEifQ==