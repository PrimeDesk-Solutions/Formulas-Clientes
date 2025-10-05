package multitec.baseDemo;

import java.nio.charset.Charset
import java.time.Instant
import java.time.ZoneId

import javax.swing.filechooser.FileFilter

import org.apache.commons.io.FileUtils
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.dto.samdev.DashboardMetadata
import sam.dto.samdev.DashboardMetadata.TipoDashboard
import sam.server.samdev.relatorio.ServletBase
import sam.server.samdev.relatorio.UiDto

public class DashServlet_ListarErros extends ServletBase {
	@Override
	public String getNome() throws Exception {
		return "Multitec-Erros do Servidor";
	}

	@Override
	public DashboardMetadata getMetadata() throws Exception {
		return new DashboardMetadata(TipoDashboard.COMPONENTE, 6, 12, true, null);
	}

	@Override
	public ResponseEntity<Object> executar() throws Exception {
		Map<String, Object> valoresDefault = Utils.map(
			"logs", this.buscarUltimosLogs()
		);
		
		UiDto dto = buscarComponenteCustomizado("multitec.baseDemo.DashRecurso_ListarErros.html", valoresDefault);
				
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_JSON)
			.body(dto);
	}
	
	public List<TableMap> buscarUltimosLogs(){
		List<TableMap> logs = new ArrayList<>();
		File diretorio = new File("." + File.separator + "errors/");
		List<File> ultimos50Erros = new ArrayList();
		
		File[] dirArray = diretorio.listFiles();
		List<File> diretorios = Arrays.asList(dirArray);
		
		diretorios.sort(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o2.getName().compareTo(o1.getName());
			}
		});
	
		for(File subDiretorio : diretorios) {
			File[] arquivos = subDiretorio.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".log");
				}
			});
		
			List<File> arquivosOrdenados = Arrays.asList(arquivos);
			arquivosOrdenados.sort(new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o2.getName().compareTo(o1.getName());
				}
			});
		
			for(File f : arquivosOrdenados) {
				ultimos50Erros.add(f);
				if(ultimos50Erros.size() >= 50)break;
			}
			if(ultimos50Erros.size() >= 50)break;
		}
		
		for (File file : ultimos50Erros) {
			
			TableMap tm = new TableMap();
			tm.put("protocolo", StringUtils.substringBeforeFirst(file.getName(), "."));
			tm.put("data", Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate());
			tm.put("hora", Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalTime());
			tm.put("texto", FileUtils.readFileToString(file, Charset.forName("UTF-8")))
			logs.add(tm);
		}
		
		return logs;
	}
}
//meta-sis-eyJkZXNjciI6Ik11bHRpdGVjLUVycm9zIGRvIFNlcnZpZG9yIiwidGlwbyI6InNlcnZsZXQiLCJ3Ijo2LCJoIjoxMiwicmVzaXplIjp0cnVlLCJ0aXBvZGFzaGJvYXJkIjoiY29tcG9uZW50ZSJ9