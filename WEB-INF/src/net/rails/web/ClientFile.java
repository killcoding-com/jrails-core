package net.rails.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import org.apache.commons.io.FileUtils;
import net.rails.support.Support;

@SuppressWarnings("serial")
public class ClientFile implements Serializable{
	
	private String name;
	private File file;
	
	public ClientFile(String name,InputStream input) throws IOException{
		super();
		this.name = name;
		if(input != null){
			FileOutputStream os = null;
			try{
				byte[] buf = new byte[1024];
				String suffix = name.substring(name.lastIndexOf("."));
				file = File.createTempFile("ClientFile_",suffix);
				String tmpdir = Support.env().getString("tmpdir");
				if(tmpdir != null){
					String filename = file.getName();
					file = new File(String.format("%s/%s",tmpdir,filename));
				}
				os = new FileOutputStream(file);
				int len = -1;
				while((len = input.read(buf)) > -1){
					os.write(buf,0,len);
					os.flush();
				}
			}finally{
				if(os != null){
					os.close();
				}
				input.close();
			}
		}
	}

	public String getName() {
		return name;
	}

	public byte[] getData() throws IOException {
		return FileUtils.readFileToByteArray(getFile());
	}
	
	public File getFile() {
		return file;
	}
	
	@Override
	public String toString(){
		String path = null;
		long size = 0;
		if(file != null){
			path = file.getAbsolutePath();
			size = file.length();
		}
		return String.format("%s;%s;%s",name,size,path);
	}
	
}
