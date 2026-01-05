package Fast.formulas.cas

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.jackson.JSonMapperCreator
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.web.multipart.MultipartFile
import sam.model.entities.aa.Aah02
import sam.model.entities.aa.Aah0201;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

public class CAS_Impotar_CamposLivres extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.CAS_IMPORTAR_DADOS;
    }

    @Override
    public void executar() {
        MultipartFile arquivo = get("arquivo");

        // Verifica se o arquivo foi enviado
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RuntimeException("Nenhum arquivo JSON foi enviado");
        }
        String jsonContent = new String(arquivo.getBytes());
        List<TableMap> campos = JSonMapperCreator.create().read(jsonContent, new TypeReference<List<TableMap>>() {})

        for(campo in campos){
            TableMap aah02j = campo.get("aah02")
            List <TableMap> aah0201sj = campo.get("aah0201s")

            //verificando se já existe um campo com esse nome na base
            Aah02 aah02 = getSession().createCriteria(Aah02.class)
                    .addJoin(Joins.fetch("aah0201s"))
                    .addWhere(Criterions.in("aah02nome", aah02j.get("aah02nome")))
                    .get(ColumnType.ENTITY)

            if(!aah02){
                //cabeca do campo livre
                Aah02 aah02n = new Aah02();

                aah02n.aah02tam = aah02j.get("aah02tam")
                aah02n.aah02obs = aah02j.get("aah02obs")
                aah02n.aah02nome = aah02j.get("aah02nome")
                aah02n.aah02senha = aah02j.get("aah02senha")
                aah02n.aah02req = aah02j.get("aah02req")
                aah02n.aah02politicaEdicao = aah02j.get("aah02politicaEdicao")
                aah02n.aah02tipo = aah02j.get("aah02tipo")
                aah02n.aah02vlrInicial = aah02j.get("aah02vlrInicial")
                aah02n.aah02descr = aah02j.get("aah02descr")

                //verificando se o campo possui valores
                if (aah0201sj != null){

                    aah02n.aah0201s = new ArrayList<Aah0201>()

                    //valores
                    for(valores in aah0201sj){

                        Aah0201 aah0201 = new Aah0201()

                        aah0201.aah0201texto = valores.get("aah0201texto")
                        aah0201.aah0201campo = aah02n // gravando o pai
                        aah0201.aah0201ordem = valores.get("aah0201ordem")
                        aah0201.aah0201valor = valores.get("aah0201valor")

                        aah02n.aah0201s.add(aah0201)
                    }
                }

                //verificando se a transação está aberta
                if (!getSession().transactionOpened) {
                    getSession().beginTransaction();
                }
                //efetivamente gravando os valores
                getSession().persist(aah02n);
                getSession().commit();

            }else{
                //interromper("Já existe um campo livre com o nome: " +  aah02j.get("aah02nome"))
            }
        }
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAwIn0=