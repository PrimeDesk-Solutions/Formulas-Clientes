package Atilatte.formulas.preGravar

import br.com.multitec.utils.ValidacaoException
import sam.model.entities.ab.Abm01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

public class SRF_ValidaDataEntrega extends FormulaBase{

    private Eaa01 eaa01;
    private Integer gravar = 1; //0-Não 1-Sim

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
    }

    @Override
    public void executar() {
        eaa01 = get("eaa01");

        alterarOrdecaoItensDocumento(eaa01);

        if(eaa01.eaa0103s.size() == 0) throw new ValidacaoException("Não é permitido salvar documento sem itens informado. Insira pelo menos um item para continuar ");

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

            if(eaa0103.eaa0103qtComl == 0 ) throw new ValidacaoException("Quantidade inválida para o item "+abm01.abm01codigo+" necessário um valor maior que zero")

            if(eaa0103.eaa0103dtEntrega == null ) throw new ValidacaoException("Necessário informar a data de entrega para o item "+abm01.abm01codigo);

        }

        for(String codigo : contagemItens.keySet()){
            if(contagemItens.get(codigo) >= 2) throw new ValidacaoException("Não é permitido a inclusão de itens repetidos no documento. Item: " + codigo.substring(1) )
        }

        put("gravar", gravar);
    }
    private void alterarOrdecaoItensDocumento(Eaa01 eaa01){

		for (Eaa0103 eaa0103 in eaa01.eaa0103s){
			// Itens
			Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);

			if (eaa0103.eaa0103json.getInteger("ordem_separacao") == null) throw new ValidacaoException("Não foi informado ordem de separação no item " + abm01.abm01codigo)
		}

		Collection<Eaa0103> eaa0103s = eaa01.getEaa0103s();
		List<Eaa0103> listaOrdenada = new ArrayList<>();
		if(eaa0103s != null && !eaa0103s.isEmpty()){
			listaOrdenada = new ArrayList<>(eaa0103s)
			Collections.sort(listaOrdenada, new Comparator<Eaa0103>() {
				@Override
				public int compare(Eaa0103 o1, Eaa0103 o2) {
					return Integer.compare(o1.getEaa0103json().getInteger("ordem_separacao"), o2.getEaa0103json().getInteger("ordem_separacao"));
				}
			});
		}

		for(int i = 0; i < listaOrdenada.size(); i++){
			listaOrdenada.get(i).setEaa0103seq(i + 1);
		}

		eaa01.setEaa0103s(listaOrdenada);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==