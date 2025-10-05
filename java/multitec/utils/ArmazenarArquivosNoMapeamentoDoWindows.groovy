package multitec.utils;

import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import sam.server.samdev.formula.SistemaArquivoBase;

/**
* Essa fórmula gerencia os arquivos do SAM-4 na rede mapeada no windows na letra M.
* Dentro de um diretório chamado ArquivosSAM4
*/
public class ArmazenarArquivosNoMapeamentoDoWindows extends SistemaArquivoBase{
	private static String diretorioPadrao = "M:/ArquivosSAM4";
	
	protected void salvar(String caminho, InputStream inputStream) throws IOException{
		File file = new File(diretorioPadrao + caminho);
		Files.createDirectories(file.getParentFile().toPath());
		FileUtils.copyInputStreamToFile(inputStream, file);
	}

	protected InputStream carregar(String caminho)throws IOException {
		return FileUtils.openInputStream(new File(diretorioPadrao + caminho));
	}

	protected void limparDiretorio(String diretorio)throws IOException {
		try{
			FileUtils.forceDelete(new File(diretorioPadrao+diretorio));
		}catch(FileNotFoundException ignore){
			//Diretório não existia, pode igorar o erro
		}
	}

	protected void deletar(String arquivo)throws IOException {
		try{
			FileUtils.forceDelete(new File(diretorioPadrao+arquivo));
		}catch(FileNotFoundException ignore){
			//Arquivo não existia, pode igorar o erro
		}

	}

	protected void renomear(String caminhoArquivoOrigem, String caminhoArquivoDestino)throws IOException {
		try{
			InputStream inputStream = carregar(caminhoArquivoOrigem);
			salvar(caminhoArquivoDestino, inputStream);
			deletar(caminhoArquivoOrigem);
		}catch(FileNotFoundException ignore){
			//Arquivo não existia, pode igorar o erro
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZmlsZXN5c3RlbSJ9